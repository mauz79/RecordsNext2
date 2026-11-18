package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.config.MiniJson;
import it.alterlega.recordsnext.app.config.ProcessingConfigWriter;
import it.alterlega.recordsnext.app.model.ProcessingSelection;
import it.alterlega.recordsnext.app.model.RecordFamily;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSetMetaData;
import java.util.Locale;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Adattatore dell'exporter di VISTE approvate alla pipeline 3.2.
 * Nessuna pubblicazione diretta: aggiunge allo staging JS gia' validato.
 * La pipeline 3.2 mantiene la responsabilita' esclusiva di rollback e publish.
 */
public final class TopFlopViewsPublication {
    private TopFlopViewsPublication() {}

    private static final String PREFIX = "window.fcmRecordsNextTopFlopViews = ";
    private static final String BASENAME = "fcmRecordsNext_TopFlopViews.";

    public static Set<String> selectedIds(ProcessingSelection selection) {
        Objects.requireNonNull(selection);
        if (!selection.isFamilyEnabled(RecordFamily.TOP_FLOP)) return Set.of();
        Set<String> chosen = new LinkedHashSet<>();
        for (String child : ProcessingConfigWriter.TOP_FLOP) {
            boolean supported = ProcessingConfigWriter.topFlopImplemented(child);
            if (selection.enabledChildren().contains(child) && !supported) {
                throw new IllegalArgumentException("Top & Flop non ancora verificato: " + child);
            }
            if (supported && (selection.enabledChildren().isEmpty()
                    || selection.enabledChildren().contains(child))) {
                chosen.add(child.substring("topFlop.".length()).toUpperCase(java.util.Locale.ROOT));
            }
        }
        if (chosen.isEmpty()) {
            throw new IllegalArgumentException("Top & Flop attivo senza record disponibili selezionati");
        }
        return Set.copyOf(chosen);
    }

    /** Esegue il calcolo una volta sola, indipendentemente dal numero di siti. */
    public static Prepared prepare(Path sqlite, Path reports, List<String> seasonIds,
                                   Set<String> selectedIds, Path scratch) throws Exception {
        return prepare(sqlite,reports,seasonIds,selectedIds,scratch,TopFlopSettings.defaults());
    }

    public static Prepared prepare(Path sqlite, Path reports, List<String> seasonIds,
                                   Set<String> selectedIds, Path scratch,TopFlopSettings settings) throws Exception {
        return prepare(sqlite,reports,seasonIds,selectedIds,scratch,settings,null);
    }

    public static Prepared prepare(Path sqlite, Path reports, List<String> seasonIds,
                                   Set<String> selectedIds, Path scratch,TopFlopSettings settings,
                                   Path identitiesFile) throws Exception {
        if (selectedIds.isEmpty()) throw new IllegalArgumentException("Selezionare i record TF");
        if (seasonIds.isEmpty()) throw new IllegalArgumentException("Nessuna stagione per Top & Flop");
        if (!Files.isRegularFile(sqlite)) throw new IOException("SQLite mancante: " + sqlite);
        if (reports != null && !Files.isDirectory(reports)) {
            throw new IOException("Report canonici mancanti: " + reports);
        }
        Files.createDirectories(scratch);
        Path output = scratch.resolve("js");
        Class.forName("org.sqlite.JDBC");
        TopFlopPublicViewsStageExporter.Result result;
        try (Connection db = DriverManager.getConnection("jdbc:sqlite:" + sqlite.toUri() + "?mode=ro")) {
            TopFlopRealFantasyRules rulebook=TopFlopRealFantasyRules.load(identitiesFile==null
                ?null:identitiesFile.getParent().resolve("topflop-real-rules.json"));
            result = TopFlopPublicViewsStageExporter.export(db, seasonIds, output, reports,
                selectedIds, settings,rulebook);
        }
        for (var file : result.files()) {
            Path path = output.resolve(file.name());
            validate(path, file.bytes(), file.sha256());
        }
        Map<String,String> identities=Map.of(), sameSeasonAliases=Map.of();
        if(identitiesFile!=null) {
            TopFlopPlayerIdentities.Registry reg=TopFlopPlayerIdentities.load(identitiesFile);
            try(Connection db=DriverManager.getConnection("jdbc:sqlite:"+sqlite.toUri()+"?mode=ro")) {
                reg.bindAudit(TopFlopPlayerIdentities.scanAvailable(db));
                validateSourceAliases(db,reg.sameSeasonAliases());
            }
            sameSeasonAliases=reg.sameSeasonAliases();
            // A confirmed separation must also hold during publication, not only in GUI.
            identities=reg.approved();
        }
        // v83: a single native Java stage, shared by rankings and the historical player archive.
        // The same approved registry and same cutoff are used for both pages.
        try (Connection db = DriverManager.getConnection("jdbc:sqlite:" + sqlite.toUri() + "?mode=ro")) {
            TopFlopIntegratedV83.stage(db, reports, seasonIds, output, identities,
                TopFlopRealFantasyRules.load(identitiesFile == null ? null :
                    identitiesFile.getParent().resolve("topflop-real-rules.json")));
        }
        return new Prepared(output, Set.copyOf(seasonIds), result.files().size(),identities,
            Set.copyOf(selectedIds),settings,sameSeasonAliases);
    }

    /** FCA calendar rows contain 38 placeholders per profile, irrespective of votes.
     * Punteggio also contains numeric TECHNICAL fields that must not turn a
     * placeholder into an appearance. Check ONLY real VOTO1, VALORESPECIALE,
     * and the 10 event fields consumed by TopFlopHistoricalViews.
     */
    private static final Set<String> CAREER_EVENTS = Set.of(
        "GOLFATTI1","GOLFATTISURIGORE1","ASSIST","GOLSUBITI",
        "GOLSUBITISURIGORE","RIGPAR","RIGSBA","AUTOGOL1","AMM","ESP");
    public static Set<Integer> meaningfulFcaDays(Connection db, String season, int playerId)
            throws Exception {
        if (!season.matches("[0-9]{4}_[0-9]{4}") || playerId<=0)
            throw new IOException("Sorgente FCA non valida: "+season+":"+playerId);
        String prefix="raw_"+season+"_fca_";
        String sql="SELECT g.\"GIORNATA\" AS RN4_GIORNATA, p.* FROM \""+prefix+"giocain\" g " +
            "JOIN \""+prefix+"punteggio\" p ON p.\"ID\"=g.\"IDPUNTEGGIO\" " +
            "WHERE g.\"IDGIOCATORE\"=? ORDER BY g.\"GIORNATA\"";
        Set<Integer> days=new LinkedHashSet<>();
        try(var st=db.prepareStatement(sql)) {
            st.setInt(1,playerId);
            try(var rs=st.executeQuery()) {
                ResultSetMetaData meta=rs.getMetaData();
                Map<String,Integer> columns=new LinkedHashMap<>();
                for(int col=2;col<=meta.getColumnCount();col++)
                    columns.put(meta.getColumnLabel(col).toUpperCase(Locale.ROOT),col);
                if(!columns.containsKey("VOTO1") || !columns.containsKey("VALORESPECIALE"))
                    throw new IOException("Punteggio FCA senza VOTO1/VALORESPECIALE: "+season);
                while(rs.next()) {
                    int day=rs.getInt(1);
                    if(day<=0)throw new IOException("Giornata FCA non valida: "+season+":"+day);
                    BigDecimal vote=rs.getBigDecimal(columns.get("VOTO1"));
                    BigDecimal special=rs.getBigDecimal(columns.get("VALORESPECIALE"));
                    boolean meaningful=vote!=null && vote.signum()>0 ||
                        special!=null && special.signum()!=0;
                    for(String event:CAREER_EVENTS) {
                        Integer col=columns.get(event);
                        if(col!=null) {
                            BigDecimal value=rs.getBigDecimal(col);
                            if(value!=null && value.signum()!=0)meaningful=true;
                        }
                    }
                    if(meaningful && !days.add(day))throw new IOException(
                        "Due prestazioni FCA nella stessa scheda: "+season+":"+playerId+" giornata "+day);
                }
            }
        }
        return days;
    }

    /** Check possible double counting of ACTUAL evidence, not the ubiquitous
     * empty rows for 38 championship rounds. Apply on every publication.
     */
    public static void validateSourceAliases(Connection db, Map<String,String> aliases) throws Exception {
        for(var entry:aliases.entrySet()) {
            String sec=entry.getKey(), prim=entry.getValue();
            if(!sec.matches("[0-9]{4}_[0-9]{4}:[0-9]+") ||
               !prim.matches("[0-9]{4}_[0-9]{4}:[0-9]+") ||
               !sec.substring(0,9).equals(prim.substring(0,9)))
                throw new IOException("Alias FCA invalido: "+sec+" / "+prim);
            String season=sec.substring(0,9);
            Set<Integer> overlap=new LinkedHashSet<>(meaningfulFcaDays(db,season,
                Integer.parseInt(sec.substring(10))));
            overlap.retainAll(meaningfulFcaDays(db,season,
                Integer.parseInt(prim.substring(10))));
            if(!overlap.isEmpty())throw new IOException(
                "Prestazioni FCA effettive sovrapposte: "+sec+" / "+prim+" giornate="+overlap);
        }
    }

    /** Preserva esattamente le stagioni ammesse dalla 3.2 per il singolo sito. */
    public static int attachScoped(Prepared prepared, List<String> scope, Path generatedJs)
            throws Exception {
        Objects.requireNonNull(prepared);
        if (!Files.isDirectory(generatedJs)) throw new IOException("Staging 3.2 mancante: " + generatedJs);
        if (scope.isEmpty()) throw new IllegalArgumentException("Scope Top & Flop vuoto");
        if (!prepared.seasons().containsAll(scope) || scope.stream().distinct().count()!=scope.size()) {
            throw new IOException("Cutoff storico Top & Flop incoerente: " + scope);
        }
        Map<String,Object> facade = loadFacade(prepared.jsDirectory().resolve(
                TopFlopPublicViewsStageExporter.FACADE));
        @SuppressWarnings("unchecked") List<Map<String,Object>> metadata =
                (List<Map<String,Object>>) facade.get("stagioni");
        if(metadata==null)throw new IOException("Metadati Top & Flop mancanti");
        Map<String,Map<String,Object>> bySeason = new LinkedHashMap<>();
        for (Map<String,Object> m : metadata) {
            String season=String.valueOf(m.get("stagione"));
            if(bySeason.putIfAbsent(season,m)!=null)throw new IOException("Doppia stagione: "+season);
        }
        List<Map<String,Object>> scopedMetadata = new ArrayList<>();
        List<Path> sources = new ArrayList<>();
        for(String season:scope) {
            Map<String,Object> row=bySeason.get(season);
            if(row==null)throw new IOException("Stagione non presente nell'indice TF: "+season);
            scopedMetadata.add(row);
            @SuppressWarnings("unchecked") List<Map<String,Object>> files=
                    (List<Map<String,Object>>)row.get("files");
            if(files==null)throw new IOException("Elenco file mancante per "+season);
            for(var file:files) {
                String name=String.valueOf(file.get("nome"));
                if(!name.matches("fcmRecordsNext_TopFlopViews\\.(?:Careers|Teams|Records|Highlights|Economy|RealFantasy|Bench)\\."
                    +season+"\\.[0-9]{3}\\.js")) {
                    throw new IOException("Nome shard TF non valido: "+name);
                }
                Path src=prepared.jsDirectory().resolve(name);
                validate(src,((Number)file.get("byte")).longValue(),String.valueOf(file.get("sha256")));
                sources.add(src);
            }
        }
        if(sources.stream().map(p->p.getFileName().toString()).distinct().count()!=sources.size())
            throw new IOException("Shard Top & Flop duplicati");
        Map<String,Object> scoped = new LinkedHashMap<>(facade);
        scoped.put("stagioni",scopedMetadata);
        scoped.put("status","GENERATED_FOR_SITE_SCOPE");
        scoped.put("identity","SEASON_LOCAL_WITH_EXPLICITLY_APPROVED_HISTORIC_LINKS");
        // Historic career totals are computed in Java for THIS site's cutoff,
        // using only identities explicitly approved in the GUI. Never expose
        // future seasons to an older site, and never merge by CodiceGazza.
        List<Map<String,Object>> history = TopFlopHistoricalViews.aggregate(
            TopFlopHistoricalViews.readCareers(prepared.jsDirectory(),sources),
            TopFlopHistoricalViews.readTeams(prepared.jsDirectory(),sources,scopedMetadata),
            prepared.approvedIdentities(),prepared.settings(),prepared.selectedIds(),
            TopFlopHistoricalViews.readRealFantasy(prepared.jsDirectory(),sources),
            prepared.sameSeasonAliases());
        List<TopFlopPublicViewsStageExporter.FileInfo> historyFiles=history.isEmpty()
            ?List.of():TopFlopPublicViewsStageExporter.shard(generatedJs,"Storico","History",
                new ArrayList<Object>(history));
        if(!historyFiles.isEmpty()) {
            scoped.put("viste",List.of("Careers","Teams","Records","Highlights","Economy","RealFantasy","Bench","History"));
        }
        Map<String,Object> historicalStatus=new LinkedHashMap<>();
        historicalStatus.put("status",history.isEmpty()?"NESSUNA_IDENTITA_APPROVATA_NEL_CUTOFF":"IDENTITA_APPROVATE");
        historicalStatus.put("identita",history.size());
        historicalStatus.put("file",historyFiles.stream().map(f->Map.of(
            "nome",f.name(),"tipo",f.kind(),"righe",f.rows(),"byte",f.bytes(),"sha256",f.sha256())).toList());
        historicalStatus.put("fonte","config/player-identities.json");
        historicalStatus.put("soglieAnomalia",Map.of("votiStagione",prepared.settings().anomalySeasonVotes(),
            "votiBaseline",prepared.settings().anomalyBaselineVotes(),
            "stagioniBaseline",prepared.settings().anomalyBaselineSeasons(),
            "deltaMinimo",prepared.settings().anomalyDelta()));
        historicalStatus.put("recordStorici",TopFlopHistoricAnomalyWinners.winners(history,prepared.selectedIds()));
        scoped.put("storico",historicalStatus);
        byte[] facadeJs=(PREFIX+TopFlopCompactStageJsExporter.Json.write(scoped)+";\n")
                .getBytes(StandardCharsets.UTF_8);
        if(facadeJs.length>TopFlopPublicViewsStageExporter.LIMIT_BYTES)
            throw new IOException("Facade Top & Flop troppo grande: "+facadeJs.length);
        Path targetFacade = generatedJs.resolve(TopFlopPublicViewsStageExporter.FACADE);
        if(Files.exists(targetFacade))throw new IOException("Facade TF gia' presente: "+targetFacade);
        for (Path source:sources) {
            Path target=generatedJs.resolve(source.getFileName());
            if(Files.exists(target))throw new IOException("Collisione JS TF: "+target);
            Files.copy(source,target);
        }
        Files.write(targetFacade,facadeJs);
        return sources.size()+historyFiles.size()+1 + TopFlopIntegratedV83.attach(prepared.jsDirectory(),scope,generatedJs);
    }

    /** Controlla tutti i JS dello staging prima che parta il publish 3.2. */
    public static int validateStaging(Path staging) throws IOException {
        int count=0;
        try (var files=Files.list(staging)) {
            for(Path file:files.filter(Files::isRegularFile).toList()) {
                if(!file.getFileName().toString().endsWith(".js"))continue;
                long bytes=Files.size(file);
                if(bytes>TopFlopPublicViewsStageExporter.LIMIT_BYTES)
                    throw new IOException("JS oltre 1.500.000 byte: "+file.getFileName()+"="+bytes);
                if(bytes==0)throw new IOException("JS vuoto: "+file.getFileName());
                count++;
            }
        }
        return count;
    }

    @SuppressWarnings("unchecked")
    private static Map<String,Object> loadFacade(Path file) throws IOException {
        String content=Files.readString(file, StandardCharsets.UTF_8).strip();
        if(!content.startsWith(PREFIX) || !content.endsWith(";"))
            throw new IOException("Facade Top & Flop non canonica");
        Object parsed=MiniJson.parse(content.substring(PREFIX.length(),content.length()-1));
        if(!(parsed instanceof Map<?,?>))throw new IOException("Facade TF invalida");
        return (Map<String,Object>)parsed;
    }

    private static void validate(Path file,long size,String expectedHash) throws Exception {
        if(!Files.isRegularFile(file) || Files.size(file)!=size || size<=0
                || size>TopFlopPublicViewsStageExporter.LIMIT_BYTES)
            throw new IOException("Dimensione JS incoerente: "+file);
        byte[] hash=MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file));
        StringBuilder hex=new StringBuilder();
        for(byte b:hash)hex.append(String.format("%02x",b&255));
        if(!hex.toString().equalsIgnoreCase(expectedHash))
            throw new IOException("Hash JS differente: "+file);
    }

    public record Prepared(Path jsDirectory,Set<String> seasons,int files,
                           Map<String,String> approvedIdentities,Set<String> selectedIds,
                           TopFlopSettings settings,Map<String,String> sameSeasonAliases) {
        public Prepared {
            approvedIdentities=Map.copyOf(approvedIdentities);
            sameSeasonAliases=Map.copyOf(sameSeasonAliases);
            selectedIds=Set.copyOf(selectedIds);
            Objects.requireNonNull(settings);
        }
        public Prepared(Path jsDirectory,Set<String> seasons,int files,
                        Map<String,String> approvedIdentities,Set<String> selectedIds,
                        TopFlopSettings settings) {
            this(jsDirectory,seasons,files,approvedIdentities,selectedIds,settings,Map.of());
        }
        /** Backwards-compatible constructor for v45/v46 tests and callers. */
        public Prepared(Path jsDirectory,Set<String> seasons,int files,
                        Map<String,String> approvedIdentities) {
            this(jsDirectory,seasons,files,approvedIdentities,Set.of(),TopFlopSettings.defaults());
        }
    }
}
