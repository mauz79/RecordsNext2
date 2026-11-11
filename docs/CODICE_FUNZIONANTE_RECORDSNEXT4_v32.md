# RecordsNext 4.0 - CODICE FUNZIONANTE v31

Appendice sorgenti alla v29: LCS storico per riconciliazione esplicita.

La v30 preesistente, se presente, NON viene sovrascritta.

Bibbia precedente: `docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v29.md` SHA256 `62fa08adc54ace325cf3cefd4a87b592e74e16fb23ca42405dccde99adad2e72`

## `src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopPlayerIdentities.java`

SHA256: `3a5ce0bffe4f92b5df8f54cd301fa810f25d36a1c27536cb3b519b5111d9bd17`

~~~~java
package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.config.MiniJson;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Explicitly approved player identities. Only player IDs and decisions are stored here;
 * the original FCA/FCM IDs, names and rows are never rewritten. In particular, the
 * Gazzetta code is a hint, not a unique person identifier.
 */
public final class TopFlopPlayerIdentities {
    private TopFlopPlayerIdentities() {}
    public static final String FILE_NAME = "player-identities.json";

    public record Source(String season, int id, Integer code, String name, String birth) {
        public String key() { return season + ":" + id; }
    }
    public record Proposal(String fingerprint, List<Source> members, boolean safe, String reason) {
        public String description() {
            return members.stream().map(s -> s.season() + " " + s.name() + " (#" + s.code() + ")")
                    .collect(Collectors.joining(" | "));
        }
    }
    public record Collision(String fingerprint, int code, List<Source> members) {
        public String description() {
            return "Codice " + code + ": " + members.stream()
                    .map(s -> s.season() + " " + s.name())
                    .collect(Collectors.joining(" | "));
        }
    }
    public record Audit(List<Proposal> proposals, List<Collision> collisions, int sources) {}

    public static final class Registry {
        private final Map<String,String> approved = new LinkedHashMap<>();
        private final Set<String> ignored = new LinkedHashSet<>();
        private final Set<String> distinct = new LinkedHashSet<>();
        public Map<String,String> approved() {return Map.copyOf(approved);}
        /** In-memory transaction snapshot: failed bulk actions must never leave partial changes. */
        public Registry snapshot() {
            Registry copy=new Registry();
            copy.approved.putAll(approved);
            copy.ignored.addAll(ignored);
            copy.distinct.addAll(distinct);
            return copy;
        }
        public void restore(Registry snapshot) {
            approved.clear(); approved.putAll(snapshot.approved);
            ignored.clear(); ignored.addAll(snapshot.ignored);
            distinct.clear(); distinct.addAll(snapshot.distinct);
        }
        public boolean isIgnored(Proposal p) {return ignored.contains(p.fingerprint());}
        public boolean isDistinct(Collision c) {return distinct.contains(c.fingerprint());}
        public boolean isMerged(Collision c) {
            String id=null;
            for(Source source:c.members()) {
                String assigned=approved.get(source.key());
                if(assigned==null)return false;
                if(id!=null&&!id.equals(assigned))return false;
                id=assigned;
            }
            return true;
        }
        public void approveSame(Collision collision) {
            if(isDistinct(collision))distinct.remove(collision.fingerprint());
            approve(new Proposal(collision.fingerprint(),collision.members(),false,
                "Stesso calciatore, approvato manualmente"));
        }
        public void undo(Proposal proposal) {
            proposal.members().forEach(s->approved.remove(s.key()));
            ignored.remove(proposal.fingerprint());
        }
        public void undo(Collision collision) {
            distinct.remove(collision.fingerprint());
            collision.members().forEach(s->approved.remove(s.key()));
        }
        public int approvedPlayers() {return approved.size();}
        public int approvedIdentities() {return new HashSet<>(approved.values()).size();}
        public void ignore(Proposal p) {ignored.add(p.fingerprint());}
        public void distinguish(Collision c) {distinct.add(c.fingerprint());}
        public void approve(Proposal p) {
            if(p.members().size()<2)throw new IllegalArgumentException("Gruppo non storico");
            if(p.members().stream().map(Source::season).distinct().count()!=p.members().size())
                throw new IllegalArgumentException("Identita' diverse nella stessa stagione: " + p.description());
            String chosen=null;
            for(Source s:p.members()) {
                String old=approved.get(s.key());
                if(old!=null) {
                    if(chosen!=null && !chosen.equals(old))
                        throw new IllegalArgumentException("Associazioni approvate in conflitto: " + p.description());
                    chosen=old;
                }
            }
            if(chosen==null) chosen="rn4p-"+p.members().stream().map(Source::key).sorted().findFirst().orElseThrow().replace(':','-');
            for(Source s:p.members())approved.put(s.key(),chosen);
            ignored.remove(p.fingerprint());
        }
        public boolean complete(Proposal p) {
            String id=null;
            for(Source s:p.members()) {
                String assigned=approved.get(s.key());
                if(assigned==null)return false;
                if(id!=null && !id.equals(assigned))return false;
                id=assigned;
            }
            return true;
        }
        /** Expired decisions are not auto-reapproved; new evidence is a new fingerprint. */
        public boolean requiresReview(Audit audit) {
            return audit.proposals().stream().anyMatch(p->!complete(p)&&!isIgnored(p))
                 || audit.collisions().stream().anyMatch(c->!isDistinct(c)&&!isMerged(c));
        }
    }

    public static Registry load(Path file) throws IOException {
        Registry r=new Registry();
        if(!Files.isRegularFile(file))return r;
        Object parsed;
        try {parsed=MiniJson.parse(Files.readString(file,StandardCharsets.UTF_8));}
        catch(RuntimeException ex){throw new IOException("Identita' storiche: JSON non valido",ex);}
        if(!(parsed instanceof Map<?,?> map) || !"rn4-player-identities-v1".equals(map.get("schemaVersion")))
            throw new IOException("Schema non riconosciuto per " + file);
        Object entries=map.get("approved");
        if(!(entries instanceof Map<?,?> m))throw new IOException("Mappa approved mancante");
        for(var entry:m.entrySet()) {
            String key=String.valueOf(entry.getKey()), id=String.valueOf(entry.getValue());
            if(!key.matches("[0-9]{4}_[0-9]{4}:[0-9]+") || !id.matches("rn4p-[0-9]{4}_[0-9]{4}-[0-9]+"))
                throw new IOException("Identita' non valida: " + key + "/" + id);
            r.approved.put(key,id);
        }
        readFingerprints(map.get("ignored"),r.ignored);
        readFingerprints(map.get("distinct"),r.distinct);
        return r;
    }
    private static void readFingerprints(Object raw,Set<String> out) throws IOException {
        if(raw==null)return;
        if(!(raw instanceof List<?> list))throw new IOException("Elenco fingerprint invalido");
        for(Object x:list){String s=String.valueOf(x);if(!s.matches("[0-9a-f]{64}"))throw new IOException("Fingerprint invalido");out.add(s);}
    }
    public static void save(Path file,Registry r) throws IOException {
        Files.createDirectories(file.toAbsolutePath().getParent());
        Map<String,Object> data=new LinkedHashMap<>();
        data.put("schemaVersion","rn4-player-identities-v1");
        data.put("approved",new TreeMap<>(r.approved));
        data.put("ignored",r.ignored.stream().sorted().toList());
        data.put("distinct",r.distinct.stream().sorted().toList());
        String json=TopFlopCompactStageJsExporter.Json.write(data)+"\n";
        Path tmp=Files.createTempFile(file.getParent(),".player-identities-",".tmp");
        try{
            Files.writeString(tmp,json,StandardCharsets.UTF_8);
            try{Files.move(tmp,file,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}
            catch(java.nio.file.AtomicMoveNotSupportedException ex){Files.move(tmp,file,StandardCopyOption.REPLACE_EXISTING);}
        }finally{Files.deleteIfExists(tmp);}
    }

    /** All imported FCA seasons in the current canonical database. */
    public static Audit scanAvailable(Connection c) throws SQLException {
        List<String> seasons=new ArrayList<>();
        try(Statement st=c.createStatement();ResultSet rows=st.executeQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name LIKE 'raw_%_fca_giocatorea'")) {
            while(rows.next()) {
                String name=rows.getString(1);
                if(name.matches("raw_[0-9]{4}_[0-9]{4}_fca_giocatorea"))
                    seasons.add(name.substring(4,13));
            }
        }
        seasons.sort(String::compareTo);
        return scan(c,seasons);
    }

    public static Audit scan(Connection c,List<String> seasons) throws SQLException {
        List<Source> all=new ArrayList<>();
        for(String season:seasons) {
            if(!season.matches("[0-9]{4}_[0-9]{4}"))throw new IllegalArgumentException("Stagione invalida");
            String table="raw_"+season+"_fca_giocatorea";
            String sql="SELECT ID,CODICEGAZZA,NOME,DATADINASCITA FROM \""+table+"\" ORDER BY ID";
            try(Statement st=c.createStatement();ResultSet rs=st.executeQuery(sql)) {
                while(rs.next()) {
                    int code=rs.getInt(2);
                    all.add(new Source(season,rs.getInt(1),rs.wasNull()||code<=0?null:code,
                        rs.getString(3),rs.getString(4)));
                }
            }
        }
        return scanSources(all);
    }

    /** Same audit on provided FCA rows; useful for regression testing without a JDBC driver. */
    public static Audit scanSources(List<Source> all) {
        // Candidate links are deliberately conservative. Same Gazzetta code with
        // different names and birth dates is NOT enough to join two people.
        Map<String,List<Source>> byCodeName=new LinkedHashMap<>();
        Map<String,List<Source>> byBirthName=new LinkedHashMap<>();
        Map<Integer,List<Source>> byCode=new TreeMap<>();
        for(Source s:all) {
            String n=normalize(s.name());
            if(s.code()!=null) {
                byCode.computeIfAbsent(s.code(),x->new ArrayList<>()).add(s);
                byCodeName.computeIfAbsent(s.code()+"/"+n,x->new ArrayList<>()).add(s);
            }
            String birth=dob(s.birth());
            if(birth!=null && !n.isEmpty()) {
                // Keep surname + first given name; do not merge common surnames
                // on a birthday alone, nor invent reversed-name permutations.
                String[] tokens=n.split(" ");
                if(tokens.length>=2) byBirthName.computeIfAbsent(birth+"/"+tokens[0]+"/"+tokens[1],x->new ArrayList<>()).add(s);
            }
        }
        Map<String,String> parents=new HashMap<>();
        Map<String,Set<String>> rootSeasons=new HashMap<>();
        for(Source s:all){parents.put(s.key(),s.key());rootSeasons.put(s.key(),new HashSet<>(Set.of(s.season())));}
        for(List<Source> group:byCodeName.values())unionGroup(group,parents,rootSeasons);
        for(List<Source> group:byBirthName.values())unionGroup(group,parents,rootSeasons);
        Map<String,List<Source>> grouped=new TreeMap<>();
        for(Source s:all)grouped.computeIfAbsent(find(parents,s.key()),x->new ArrayList<>()).add(s);
        Set<Integer> disputedCodes=new HashSet<>();
        List<Collision> collisions=new ArrayList<>();
        for(var e:byCode.entrySet()) {
            List<Source> members=e.getValue();
            if(members.stream().map(s->find(parents,s.key())).distinct().count()>1 &&
                    members.stream().map(s->normalize(s.name())).distinct().count()>1) {
                disputedCodes.add(e.getKey());
                collisions.add(new Collision(fingerprint(members),e.getKey(),List.copyOf(members)));
            }
        }
        List<Proposal> proposals=new ArrayList<>();
        for(List<Source> group:grouped.values()) {
            if(group.size()<2)continue;
            group.sort(Comparator.comparing(Source::key));
            long seasonsCount=group.stream().map(Source::season).distinct().count();
            boolean birthConflict=group.stream().map(s->dob(s.birth())).filter(x->x!=null).distinct().count()>1;
            boolean codeCollision=group.stream().anyMatch(s->s.code()!=null&&disputedCodes.contains(s.code()));
            boolean namesEqual=group.stream().map(s->normalize(s.name())).distinct().count()==1;
            boolean safe=seasonsCount==group.size()&&!birthConflict&&!codeCollision&&namesEqual;
            String reason=birthConflict?"Date di nascita discordanti":
                (seasonsCount<group.size()?"Piu' persone nella stessa stagione":
                (codeCollision?"Codice storico riutilizzato":"Nomi/codici da verificare"));
            proposals.add(new Proposal(fingerprint(group),List.copyOf(group),safe,safe?"Corrispondenza esatta":reason));
        }
        proposals.sort(Comparator.comparing((Proposal p)->p.safe()?1:0)
            .thenComparing(p->p.members().get(0).name(),String.CASE_INSENSITIVE_ORDER));
        return new Audit(List.copyOf(proposals),List.copyOf(collisions),all.size());
    }
    private static void unionGroup(List<Source> group,Map<String,String> p,Map<String,Set<String>> roots) {
        if(group.size()<2)return;
        String first=group.get(0).key();
        for(int i=1;i<group.size();i++) {
            String other=group.get(i).key();
            // No two different player records in the same season may be joined.
            if(group.get(i).season().equals(group.get(0).season()))continue;
            String a=find(p,first),b=find(p,other);
            if(a.equals(b))continue;
            Set<String> firstSeasons=roots.get(a), secondSeasons=roots.get(b);
            boolean conflict=secondSeasons.stream().anyMatch(firstSeasons::contains);
            if(!conflict){p.put(b,a);firstSeasons.addAll(secondSeasons);roots.remove(b);}
        }
    }
    private static String find(Map<String,String> p,String key) {
        String parent=p.get(key);if(parent.equals(key))return key;
        String root=find(p,parent);p.put(key,root);return root;
    }
    private static String normalize(String raw) {
        if(raw==null)return "";
        return Normalizer.normalize(raw,Normalizer.Form.NFD).replaceAll("\\p{M}","")
            .toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+"," ").trim().replaceAll(" +"," ");
    }
    private static String dob(String raw) {
        if(raw==null||raw.length()<10||!raw.substring(0,10).matches("[0-9]{4}-[0-9]{2}-[0-9]{2}"))return null;
        return raw.substring(0,10);
    }
    private static String fingerprint(Collection<Source> src) {
        try {
            String evidence=src.stream().sorted(Comparator.comparing(Source::key))
                .map(s->s.key()+"/"+s.code()+"/"+s.name()+"/"+s.birth()).collect(Collectors.joining("|"));
            byte[] hash=MessageDigest.getInstance("SHA-256").digest(evidence.getBytes(StandardCharsets.UTF_8));
            StringBuilder h=new StringBuilder();for(byte b:hash)h.append(String.format("%02x",b&255));return h.toString();
        }catch(Exception e){throw new IllegalStateException(e);}
    }
}
~~~~

## `src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopIdentityEvidence.java`

SHA256: `05e8d8dbba2b961570b9f42ea495a6bdc863e5a366a75c684447ec614d2c2bcb`

~~~~java
package it.alterlega.recordsnext.app.topflop;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * External LCS player-name evidence used ONLY to explain approval proposals.
 * Neither these CSVs nor this class can modify the identity registry, SQLite or
 * FCA/FCM source rows. A decision is always made explicitly by the operator.
 */
public final class TopFlopIdentityEvidence {
    private record Identity(String name, String birth) {}
    private final Map<Identity, Identity> aliases = new LinkedHashMap<>();
    private final Set<Identity> checked = new HashSet<>();
    private final Set<Identity> conflictingAliases = new HashSet<>();
    /** Separate historical evidence: never used to approve or silently override current LCS. */
    private final Map<Identity, Map<String, Identity>> historicalAliases = new LinkedHashMap<>();
    private final Set<Identity> historicalConflicts = new HashSet<>();
    private int historicalRows;
    private int aliasRows;
    private int checkedRows;

    public record Assessment(String category, String explanation, String lcsEvidence,
                             boolean requiresIndividualReview) {}

    public static TopFlopIdentityEvidence load(Path directory) throws IOException {
        TopFlopIdentityEvidence evidence = new TopFlopIdentityEvidence();
        Path aliasFile = directory.resolve("LCS_GiocatoriAccoppiamenti.csv");
        Path checkFile = directory.resolve("LCS_ControlloGiocatori.csv");
        if (Files.exists(aliasFile)) {
            for (List<String> row : parse(aliasFile)) {
                if (row.size() < 4 || row.get(0).equalsIgnoreCase("Nome Giocatore")) continue;
                Identity original = identity(row.get(0),row.get(1));
                Identity replacement = identity(row.get(2),row.get(3));
                if (original.name().isBlank() || replacement.name().isBlank()) continue;
                Identity previous = evidence.aliases.putIfAbsent(original,replacement);
                if (previous != null && !previous.equals(replacement)) evidence.conflictingAliases.add(original);
                evidence.aliasRows++;
            }
        }
        // Keep the three older versions separate. The 2020 file may be byte-identical
        // to the current export: it is still useful as a dated provenance record.
        evidence.loadHistorical(directory.resolve("LCS_GiocatoriAccoppiamenti_2015_old.csv"), "2015 old");
        evidence.loadHistorical(directory.resolve("LCS_GiocatoriAccoppiamenti_2019.csv"), "2019");
        evidence.loadHistorical(directory.resolve("LCS_GiocatoriAccoppiamenti_2020.csv"), "2020");
        evidence.reconcileHistoricalDifferences();
        if (Files.exists(checkFile)) {
            for (List<String> row : parse(checkFile)) {
                if (row.size() < 2 || row.get(0).equalsIgnoreCase("NomeGiocatore")) continue;
                Identity person = identity(row.get(0),row.get(1));
                if (!person.name().isBlank()) { evidence.checked.add(person); evidence.checkedRows++; }
            }
        }
        return evidence;
    }
    public int aliasRows() { return aliasRows; }
    public int checkedRows() { return checkedRows; }
    private void loadHistorical(Path file, String version) throws IOException {
        if (!Files.isRegularFile(file)) return;
        for (List<String> row : parse(file)) {
            if (row.size() < 4 || row.get(0).equalsIgnoreCase("Nome Giocatore")) continue;
            Identity original=identity(row.get(0),row.get(1));
            Identity replacement=identity(row.get(2),row.get(3));
            if(original.name().isBlank()||replacement.name().isBlank())continue;
            Map<String,Identity> versions=historicalAliases.computeIfAbsent(original,k->new LinkedHashMap<>());
            Identity previous=versions.putIfAbsent(version,replacement);
            if(previous!=null && !previous.equals(replacement)) historicalConflicts.add(original);
            historicalRows++;
        }
    }
    private void reconcileHistoricalDifferences() {
        for (var entry : historicalAliases.entrySet()) {
            Set<Identity> targets=new HashSet<>(entry.getValue().values());
            Identity latest=aliases.get(entry.getKey());
            if(latest!=null) targets.add(latest);
            if(targets.size()>1) historicalConflicts.add(entry.getKey());
        }
    }
    public int historicalRows() { return historicalRows; }
    public int historicalConflicts() { return historicalConflicts.size(); }
    public int historicalOnly() {
        int count=0;
        for(Identity key:historicalAliases.keySet()) if(!aliases.containsKey(key))count++;
        return count;
    }

    /** Assessment of one duplicated Gazzetta code; never an authorization to merge. */
    public Assessment classify(TopFlopPlayerIdentities.Collision collision) {
        List<TopFlopPlayerIdentities.Source> sources = collision.members();
        Set<String> names = new HashSet<>();
        Set<String> dates = new HashSet<>();
        Set<Identity> resolved = new HashSet<>();
        boolean anyAlias = false;
        boolean aliasConflict = false;
        boolean allKnownDates = true;
        boolean controlMatch = false;
        for (var source : sources) {
            Identity orig = identity(source.name(),source.birth());
            names.add(orig.name());
            if (orig.birth().isBlank() || orig.birth().startsWith("1900-")) allKnownDates = false;
            else dates.add(orig.birth());
            var canonical = canonical(orig);
            resolved.add(canonical);
            if (!canonical.equals(orig)) anyAlias = true;
            if (conflictingAliases.contains(orig)) aliasConflict = true;
            if (checked.contains(canonical)) controlMatch = true;
        }
        boolean historicalConflict=false;
        boolean historicalMatch=false;
        for(var source : sources) {
            Identity orig=identity(source.name(),source.birth());
            if(historicalConflicts.contains(orig)) historicalConflict=true;
            if(historicalAliases.containsKey(orig)) historicalMatch=true;
        }
        String lcs = aliasConflict ? "LCS: correzioni discordanti, verificare" :
            anyAlias ? ("LCS Accoppiamenti: alias documentati" +
                       (controlMatch ? "; riscontro ControlloGiocatori" : "")) :
            (controlMatch ? "LCS ControlloGiocatori: anagrafica presente" : "Nessuna correzione LCS pertinente");
        if (historicalConflict) return new Assessment("FONTI LCS STORICHE DISCORDANTI",
            "Le versioni 2015/2019/2020 riportano correzioni diverse per almeno una anagrafica: esaminare individualmente.",
            "LCS: correzioni discordanti tra versioni; leggere il dettaglio prima di decidere.",true);
        if (aliasConflict) return new Assessment("LCS IN CONFLITTO",
            "La stessa anagrafica ha correzioni LCS discordanti. Non fondere in blocco.",lcs,true);
        if(historicalMatch && !anyAlias) lcs="Riscontri LCS anche nelle versioni 2015/2019/2020; nessuna fusione automatica";
        if (anyAlias && resolved.size() == 1 && noSeasonDuplicates(sources)) {
            return new Assessment("VARIANTE DOCUMENTATA LCS",
                "Le anagrafiche convergono nella stessa forma corretta nel CSV LCS. Richiede comunque approvazione.",lcs,false);
        }
        boolean similar = similarNames(names);
        boolean sameBirth = dates.size() == 1 && allKnownDates;
        if (sameBirth && similar) return new Assessment("VARIANTE ORTOGRAFICA",
            "I nomi sono simili e la data di nascita e' identica. Proposta da approvare.",lcs,false);
        if (sameBirth) return new Assessment("NOMI DIVERSI, STESSA DATA",
            "La data coincide, ma i nomi differiscono troppo: controllare che sia la stessa persona.",lcs,true);
        if (dates.size() == 2 && swappedDayMonth(dates) && similar)
            return new Assessment("DATA GIORNO/MESE INVERTITI",
                "Nomi simili; le date sembrano avere giorno e mese scambiati.",lcs,false);
        if (dates.size() > 1 && similar) return new Assessment("NASCITA DISCORDANTE",
            "I nomi suggeriscono la stessa persona, ma le date di nascita differiscono.",lcs,true);
        if (dates.size() > 1 && !similar) return new Assessment("POSSIBILI PERSONE DIVERSE",
            "Nomi e date differenti: probabile riutilizzo del codice Gazzetta. Esaminare individualmente.",lcs,true);
        if (similar) return new Assessment("VARIANTE DA VERIFICARE",
            "Nomi simili; nascita incompleta o non utilizzabile come conferma.",lcs,true);
        return new Assessment("CODICE DA VERIFICARE",
            "Identita' non dimostrabile dai nomi e dalle date disponibili.",lcs,true);
    }
    public String evidenceDetails(TopFlopPlayerIdentities.Collision collision) {
        StringBuilder b=new StringBuilder();
        Set<Identity> seen=new HashSet<>();
        for(var source:collision.members()) {
            Identity orig=identity(source.name(),source.birth());
            if(!seen.add(orig))continue;
            Identity latest=aliases.get(orig);
            if(latest!=null) {
                b.append("LCS corrente: ").append(orig.name()).append(" [").append(orig.birth())
                 .append("] -> ").append(latest.name()).append(" [").append(latest.birth()).append("]\n");
            }
            Map<String,Identity> historical=historicalAliases.get(orig);
            if(historical!=null) {
                for(var entry:historical.entrySet()) {
                    b.append("LCS ").append(entry.getKey()).append(": ")
                     .append(orig.name()).append(" [").append(orig.birth()).append("] -> ")
                     .append(entry.getValue().name()).append(" [").append(entry.getValue().birth()).append("]\n");
                }
            }
            if(historicalConflicts.contains(orig))
                b.append("ATTENZIONE: DESTINAZIONI LCS STORICHE DISCORDANTI; approvazione individuale.\n");
        }
        return b.length()==0 ? "Nessun accoppiamento LCS specifico per queste anagrafiche." : b.toString();
    }
    private Identity canonical(Identity original) {
        Identity current = original;
        Set<Identity> seen = new HashSet<>();
        while (seen.add(current) && !conflictingAliases.contains(current)) {
            Identity next = aliases.get(current);
            if (next == null || next.equals(current)) break;
            current = next;
        }
        return current;
    }
    private static boolean noSeasonDuplicates(List<TopFlopPlayerIdentities.Source> group) {
        return group.stream().map(TopFlopPlayerIdentities.Source::season).distinct().count()==group.size();
    }
    private static boolean swappedDayMonth(Set<String> dates) {
        var v = List.copyOf(dates);
        if (v.size()!=2 || v.get(0).length()!=10 || v.get(1).length()!=10) return false;
        String a=v.get(0), b=v.get(1);
        return a.substring(0,4).equals(b.substring(0,4)) &&
            a.substring(5,7).equals(b.substring(8,10)) && a.substring(8,10).equals(b.substring(5,7));
    }
    private static boolean similarNames(Set<String> names) {
        List<String> n = new ArrayList<>(names);
        if(n.size()<2) return true;
        for(int i=1;i<n.size();i++) {
            boolean near=false;
            for(int j=0;j<i;j++) if(similar(n.get(i),n.get(j))) {near=true;break;}
            if(!near) return false;
        }
        return true;
    }
    private static boolean similar(String a,String b) {
        if(a.equals(b))return true;
        if(a.isEmpty()||b.isEmpty())return false;
        int len = Math.max(a.length(),b.length());
        if (levenshtein(a,b) <= Math.max(1,(int)Math.floor(len*0.16)))return true;
        Set<String> as=new HashSet<>(List.of(a.split(" ")));
        Set<String> bs=new HashSet<>(List.of(b.split(" ")));
        int shared=0;
        for(String token:as)if(bs.contains(token))shared++;
        return shared>=2 && shared*1.0/Math.max(as.size(),bs.size())>=0.5;
    }
    private static int levenshtein(String a,String b) {
        if(Math.abs(a.length()-b.length())>Math.max(3,(int)(Math.max(a.length(),b.length())*0.25)))return 999;
        int[] prev=new int[b.length()+1],curr=new int[b.length()+1];
        for(int j=0;j<=b.length();j++)prev[j]=j;
        for(int i=1;i<=a.length();i++){
            curr[0]=i;
            for(int j=1;j<=b.length();j++)curr[j]=Math.min(Math.min(curr[j-1]+1,prev[j]+1),prev[j-1]+(a.charAt(i-1)==b.charAt(j-1)?0:1));
            int[] t=prev;prev=curr;curr=t;
        }
        return prev[b.length()];
    }
    private static Identity identity(String name,String birth) {
        return new Identity(normalize(name),normalizeDate(birth));
    }
    private static String normalize(String name) {
        if(name==null)return "";
        return Normalizer.normalize(name,Normalizer.Form.NFD).replaceAll("\\p{M}","")
            .toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+"," ").trim().replaceAll(" +"," ");
    }
    private static String normalizeDate(String raw) {
        if(raw==null)return "";
        String s=raw.trim();
        if(s.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}.*"))return s.substring(0,10);
        if(s.matches("[0-9]{2}[-/][0-9]{2}[-/][0-9]{4}"))return s.substring(6)+"-"+s.substring(3,5)+"-"+s.substring(0,2);
        return "";
    }
    private static List<List<String>> parse(Path file) throws IOException {
        byte[] bytes=Files.readAllBytes(file);
        String txt;
        try {
            var decoder=StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT);
            txt=decoder.decode(ByteBuffer.wrap(bytes)).toString();
        } catch(CharacterCodingException ex) {txt=new String(bytes,Charset.forName("windows-1252"));}
        if(txt.startsWith("\uFEFF"))txt=txt.substring(1);
        List<List<String>> rows=new ArrayList<>();
        List<String> cells=new ArrayList<>();
        StringBuilder item=new StringBuilder();
        boolean quoted=false;
        for(int i=0;i<txt.length();i++) {
            char ch=txt.charAt(i);
            if(ch=='"') {if(quoted && i+1<txt.length() && txt.charAt(i+1)=='"') {item.append('"');i++;} else quoted=!quoted;}
            else if(ch==';'&&!quoted){cells.add(item.toString().trim());item.setLength(0);}
            else if((ch=='\n'||ch=='\r')&&!quoted){
                if(ch=='\r'&&i+1<txt.length()&&txt.charAt(i+1)=='\n')i++;
                cells.add(item.toString().trim());item.setLength(0);
                if(cells.stream().anyMatch(v->!v.isBlank()))rows.add(List.copyOf(cells));
                cells=new ArrayList<>();
            } else item.append(ch);
        }
        cells.add(item.toString().trim());
        if(cells.stream().anyMatch(v->!v.isBlank()))rows.add(List.copyOf(cells));
        return rows;
    }
}
~~~~

## `src/main/java/it/alterlega/recordsnext/gui/TopFlopIdentityReviewDialog.java`

SHA256: `ec9ab7ef3ca33029ece6cf36215638693fdcccb3c02bf6109a78ccd19524f910`

~~~~java
package it.alterlega.recordsnext.gui;

import it.alterlega.recordsnext.app.topflop.TopFlopIdentityEvidence;
import it.alterlega.recordsnext.app.topflop.TopFlopPlayerIdentities;
import it.alterlega.recordsnext.app.topflop.TopFlopPlayerIdentities.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Approval screen for player identities: no automatic changes, ever.
 * LCS CSVs only help explain choices and are never a source of approved IDs.
 */
public final class TopFlopIdentityReviewDialog extends JDialog {
    private record Row(Proposal proposal, Collision collision, TopFlopIdentityEvidence.Assessment assessment) {
        String kind() {
            if (collision != null) return assessment.category();
            return proposal.safe() ? "CORRISPONDENZA ESATTA" : "PROPOSTA DA VERIFICARE";
        }
        String detail() { return collision!=null ? collision.description() : proposal.description(); }
        String evidence() {
            return collision!=null ? assessment.lcsEvidence() : proposal.reason();
        }
    }
    private final Registry registry;
    private final Audit audit;
    private final Path file;
    private final TopFlopIdentityEvidence evidence;
    private final List<Row> rows = new ArrayList<>();
    private final JLabel status = new JLabel();
    private final JTable table;
    private final TableRowSorter<AbstractTableModel> sorter;
    private final JTextArea detail = new JTextArea(7,100);
    private final JTextField search = new JTextField(24);
    private final JComboBox<String> filter = new JComboBox<>(new String[]{
        "Anomalie codici", "Proposte da verificare", "Proposte esatte", "Tutte"
    });

    public TopFlopIdentityReviewDialog(Frame owner, Path projectRoot) throws Exception {
        super(owner,"Identita' storiche calciatori - approvazione",true);
        file=projectRoot.resolve("config").resolve(TopFlopPlayerIdentities.FILE_NAME);
        registry=TopFlopPlayerIdentities.load(file);
        evidence=TopFlopIdentityEvidence.load(projectRoot.resolve("config").resolve("identity-evidence"));
        Path db=projectRoot.resolve("data/database/recordsnext.db");
        if (!Files.isRegularFile(db)) throw new IllegalStateException("SQLite non presente: " + db);
        Class.forName("org.sqlite.JDBC");
        try(Connection c=DriverManager.getConnection("jdbc:sqlite:"+db.toUri()+"?mode=ro")) {
            audit=TopFlopPlayerIdentities.scanAvailable(c);
            if(audit.sources()==0)throw new IllegalStateException(
                "Archivio FCA non ancora importato. Eseguire prima l'elaborazione completa, poi riconciliare.");
        }
        audit.collisions().forEach(c->rows.add(new Row(null,c,evidence.classify(c))));
        audit.proposals().forEach(p->rows.add(new Row(p,null,null)));

        var model=new AbstractTableModel() {
            public int getRowCount(){return rows.size();}
            public int getColumnCount(){return 5;}
            public String getColumnName(int col){return switch(col){
                case 0->"Categoria";case 1->"Stato";case 2->"Occ.";
                case 3->"Evidenza";default->"Stagioni / nomi / codici";};}
            public Object getValueAt(int r,int c){
                Row row=rows.get(r);
                return switch(c){
                    case 0->row.kind();
                    case 1->row.collision()!=null?(registry.isDistinct(row.collision())?"PERSONE DISTINTE":
                            registry.isMerged(row.collision())?"UNIONE APPROVATA":"DA DECIDERE"):
                        registry.complete(row.proposal())?"APPROVATO":
                        registry.isIgnored(row.proposal())?"RINVIATO":"DA DECIDERE";
                    case 2->row.collision()!=null?row.collision().members().size():row.proposal().members().size();
                    case 3->row.evidence();
                    default->row.detail();
                };
            }
        };
        setLayout(new BorderLayout(8,8));
        JPanel top=new JPanel(new BorderLayout(4,6));
        top.setBorder(BorderFactory.createEmptyBorder(10,12,6,12));
        top.add(new JLabel("<html><b>Nessuna fusione automatica.</b> Le fonti LCS sono indizi, non approvazioni. " +
            "Le versioni 2015, 2019 e 2020 restano distinte. Ogni decisione richiede il tuo clic.</html>"),BorderLayout.NORTH);
        top.add(status,BorderLayout.CENTER);
        JPanel selectors=new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectors.add(new JLabel("Visualizza:"));selectors.add(filter);
        selectors.add(new JLabel("Cerca nome / codice:"));selectors.add(search);
        selectors.add(new JLabel("Seleziona una riga per leggere tutti i dettagli."));
        top.add(selectors,BorderLayout.SOUTH);
        add(top,BorderLayout.NORTH);

        table=new JTable(model);
        sorter=new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setRowHeight(26);
        table.getColumnModel().getColumn(0).setPreferredWidth(190);
        table.getColumnModel().getColumn(1).setPreferredWidth(125);
        table.getColumnModel().getColumn(2).setPreferredWidth(45);
        table.getColumnModel().getColumn(3).setPreferredWidth(250);
        table.getColumnModel().getColumn(4).setPreferredWidth(600);
        table.getSelectionModel().addListSelectionListener(e-> {if(!e.getValueIsAdjusting()) updateDetails();});
        filter.addActionListener(e->updateFilter());
        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e){updateFilter();}
            public void removeUpdate(DocumentEvent e){updateFilter();}
            public void changedUpdate(DocumentEvent e){updateFilter();}
        });
        detail.setEditable(false);
        detail.setLineWrap(true);detail.setWrapStyleWord(true);
        detail.setText("Seleziona una riga per vedere nomi, stagioni e date di nascita complete.");
        JScrollPane detailScroll=new JScrollPane(detail);
        detailScroll.setBorder(BorderFactory.createTitledBorder("Dettaglio delle anagrafiche e riscontri LCS"));
        JSplitPane body=new JSplitPane(JSplitPane.VERTICAL_SPLIT,new JScrollPane(table),detailScroll);
        body.setResizeWeight(0.75);
        add(body,BorderLayout.CENTER);

        JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT,6,5));
        JButton exact=new JButton("Approva le proposte esatte");
        exact.addActionListener(e->{
            long pending=audit.proposals().stream().filter(p->p.safe()&&!registry.complete(p)).count();
            if(pending==0){JOptionPane.showMessageDialog(this,"Nessuna proposta esatta ancora da approvare.");return;}
            int reply=JOptionPane.showConfirmDialog(this,
                "Approvare esplicitamente "+pending+" corrispondenze esatte?\n"+
                "I codici ambigui non saranno inclusi.","Approvazione collettiva",JOptionPane.YES_NO_OPTION);
            if(reply==JOptionPane.YES_OPTION)act(()->{
                for(Proposal p:audit.proposals())if(p.safe()&&!registry.complete(p))registry.approve(p);
            });
        });
        JButton approve=new JButton("Approva proposte selezionate");
        approve.addActionListener(e->{
            if(selected().isEmpty()){JOptionPane.showMessageDialog(this,"Seleziona una proposta.");return;}
            act(()->{
            for(Row r:selected())if(r.proposal()!=null){
                for(Collision c:audit.collisions()) {
                    if(!registry.isDistinct(c) && r.proposal().members().stream()
                        .anyMatch(x->x.code()!=null&&x.code()==c.code()))
                        throw new IllegalArgumentException("Risolvere prima il codice ambiguo " + c.code());
                }
                registry.approve(r.proposal());
            }
        });});
        JButton distinct=new JButton("Conferma persone diverse");
        distinct.addActionListener(e->{
            List<Row> chosen=selected();
            if(chosen.stream().noneMatch(r->r.collision()!=null)){
                JOptionPane.showMessageDialog(this,"Seleziona almeno un codice ambiguo.");return;
            }
            int ok=JOptionPane.showConfirmDialog(this,
                "Confermi che le identita' collegate ai codici selezionati NON devono essere unite?\n"+
                "Le proposte interne resteranno esaminabili separatamente.",
                "Separazione identita'",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
            if(ok==JOptionPane.YES_OPTION)act(()->{
                for(Row r:chosen)if(r.collision()!=null)registry.distinguish(r.collision());
            });
        });
        JButton same=new JButton("Conferma stessa persona");
        same.addActionListener(e->{
            List<Row> selected=selected().stream().filter(r->r.collision()!=null).toList();
            if(selected.isEmpty()){JOptionPane.showMessageDialog(this,"Seleziona almeno un codice ambiguo.");return;}
            boolean serious=selected.stream().anyMatch(r->r.assessment().requiresIndividualReview());
            if(serious && selected.size()!=1){
                JOptionPane.showMessageDialog(this,"I casi incerti o gravi vanno confermati uno alla volta.");return;
            }
            if(serious) {
                int code=selected.get(0).collision().code();
                String confirmation=JOptionPane.showInputDialog(this,
                    "Attenzione: identita' incerta. Verifica nomi e date.\n"+
                    "Per unirle davvero scrivi UNISCI " + code + ":",
                    "Conferma eccezionale",JOptionPane.WARNING_MESSAGE);
                if(!("UNISCI "+code).equals(confirmation))return;
            } else {
                int ok=JOptionPane.showConfirmDialog(this,
                    "Confermare che tutte le occorrenze dei codici selezionati sono la stessa persona?\n"+
                    "Anche le varianti suggerite richiedono approvazione esplicita.",
                    "Approvazione varianti",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
                if(ok!=JOptionPane.YES_OPTION)return;
            }
            act(()->{for(Row r:selected)registry.approveSame(r.collision());});
        });
        JButton undo=new JButton("Revoca decisioni selezionate");
        undo.addActionListener(e->{
            if(selected().isEmpty()){JOptionPane.showMessageDialog(this,"Seleziona una decisione.");return;}
            act(()->{
            for(Row r:selected()){
                if(r.collision()!=null)registry.undo(r.collision());
                else registry.undo(r.proposal());
            }
        });});
        JButton ignore=new JButton("Rimanda proposte selezionate");
        ignore.addActionListener(e->{
            if(selected().stream().noneMatch(r->r.proposal()!=null)){
                JOptionPane.showMessageDialog(this,"Seleziona una proposta da rimandare.");return;
            }
            act(()->{
                for(Row r:selected())if(r.proposal()!=null)registry.ignore(r.proposal());
            });
        });
        JButton close=new JButton("Chiudi");close.addActionListener(e->dispose());
        for(JButton b:new JButton[]{exact,approve,distinct,same,undo,ignore,close})actions.add(b);
        add(actions,BorderLayout.SOUTH);
        setSize(1460,790);setLocationRelativeTo(owner);
        updateFilter();updateStatus();
    }
    private void updateFilter(){
        String needle=search.getText().strip().toUpperCase(Locale.ROOT);
        int mode=filter.getSelectedIndex();
        sorter.setRowFilter(new RowFilter<>(){
            public boolean include(Entry<? extends AbstractTableModel,? extends Integer> e){
                Row r=rows.get(e.getIdentifier());
                if(mode==0 && r.collision()==null)return false;
                if(mode==1 && (r.proposal()==null || r.proposal().safe()))return false;
                if(mode==2 && (r.proposal()==null || !r.proposal().safe()))return false;
                String text=(r.kind()+" "+r.detail()+" "+r.evidence()).toUpperCase(Locale.ROOT);
                return needle.isEmpty()||text.contains(needle);
            }
        });
        updateDetails();
    }
    private void updateDetails(){
        int selected=table.getSelectedRow();
        if(selected<0){detail.setText("Seleziona un codice per esaminare tutte le stagioni, i nomi e le date.");return;}
        Row r=rows.get(table.convertRowIndexToModel(selected));
        var b=new StringBuilder(r.kind()+" - "+r.evidence()+"\n");
        if(r.collision()!=null){
            b.append(r.assessment().explanation()).append("\n\n");
            for(Source s:r.collision().members())b.append(s.season()).append(" | ")
                .append(s.name()).append(" | codice ").append(s.code())
                .append(" | nascita ").append(s.birth()).append("\n");
            b.append("\n").append(evidence.evidenceDetails(r.collision()));
        } else {
            b.append(r.proposal().reason()).append("\n\n");
            for(Source s:r.proposal().members())b.append(s.season()).append(" | ")
                .append(s.name()).append(" | codice ").append(s.code())
                .append(" | nascita ").append(s.birth()).append("\n");
        }
        detail.setText(b.toString());detail.setCaretPosition(0);
    }
    private List<Row> selected(){
        List<Row> out=new ArrayList<>();
        for(int idx:table.getSelectedRows())out.add(rows.get(table.convertRowIndexToModel(idx)));
        return out;
    }
    private void act(Runnable operation){
        Registry before=registry.snapshot();
        try {
            operation.run();
            TopFlopPlayerIdentities.save(file,registry);
            ((AbstractTableModel)table.getModel()).fireTableDataChanged();
            updateStatus();updateFilter();
        } catch(Exception ex){
            registry.restore(before);
            ((AbstractTableModel)table.getModel()).fireTableDataChanged();
            JOptionPane.showMessageDialog(this,ex.getMessage(),"Verifica identita'",JOptionPane.ERROR_MESSAGE);
        }
    }
    private void updateStatus(){
        long pending=audit.proposals().stream().filter(p->!registry.complete(p)&&!registry.isIgnored(p)).count();
        long collisions=audit.collisions().stream().filter(c->!registry.isDistinct(c)&&!registry.isMerged(c)).count();
        status.setText("Anagrafiche: "+audit.sources()+"  |  proposte: "+audit.proposals().size()+
            "  |  approvazioni: "+registry.approvedIdentities()+
            "  |  in sospeso: "+pending+"  |  codici da decidere: "+collisions+
            "  |  LCS: "+evidence.aliasRows()+" correnti, "+evidence.historicalRows()+" storici"
            +" ("+evidence.historicalConflicts()+" chiavi discordanti), "+evidence.checkedRows()+" controlli");
    }
}
~~~~

## `src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopIdentityEvidenceSmokeTest.java`

SHA256: `8d881e51e9cdec22192d4b35d52c2dd83859660f3cc932149aa0009b5b0ba545`

~~~~java
package it.alterlega.recordsnext.app.topflop;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Can run from target/test-classes with no SQLite JDBC. */
public final class TopFlopIdentityEvidenceSmokeTest {
    private static void ok(boolean value, String message) {if(!value)throw new AssertionError(message);}
    private static TopFlopPlayerIdentities.Source source(String season, int id, int code, String name, String birth) {
        return new TopFlopPlayerIdentities.Source(season,id,code,name,birth);
    }
    private static TopFlopPlayerIdentities.Collision collision(int code, TopFlopPlayerIdentities.Source... src) {
        return new TopFlopPlayerIdentities.Collision("a".repeat(64),code,List.of(src));
    }
    public static void main(String[] args) throws Exception {
        Path directory=args.length==0 ? Path.of("config/identity-evidence") : Path.of(args[0]);
        TopFlopIdentityEvidence lcs=TopFlopIdentityEvidence.load(directory);
        ok(lcs.aliasRows()==569,"Expected 569 LCS alias entries (windows-1252 CSV)");
        ok(lcs.checkedRows()==2133,"Expected 2133 LCS checked records (UTF-8 CSV)");
        ok(lcs.historicalRows()==1531,"Expected 401+561+569 historical alias rows");
        ok(lcs.historicalOnly()>=1,"Historical-only associations must remain available");
        ok(lcs.historicalConflicts()>0,"Conflicting historical changes must be flagged");
        var maggio=collision(500011,
            source("2006_2007",1,500011,"MAGGIO  Cristian","1982-02-11"),
            source("2007_2008",2,500011,"MAGGIO Christian","1982-11-02"));
        ok(lcs.classify(maggio).requiresIndividualReview(),"2015 vs 2020 changed correction is not bulk approvable");
        ok(lcs.classify(maggio).category().equals("FONTI LCS STORICHE DISCORDANTI"),"Historic divergent evidence visible");
        ok(lcs.evidenceDetails(maggio).contains("LCS 2015 old:"),"2015 evidence shown in detail");
        ok(lcs.evidenceDetails(maggio).contains("LCS 2019:"),"2019 evidence shown in detail");
        ok(lcs.evidenceDetails(maggio).contains("LCS 2020:"),"2020 evidence shown in detail");

        var marzoratti=collision(12849,
            source("2006_2007",1,12849,"MARZORATTI Lino","1986-12-10 00:00:00.0"),
            source("2007_2008",1,12849,"MARZORATTI Lino","1986-10-12 00:00:00.0"),
            source("2013_2014",1,12849,"MARZORATI Lino","1986-10-12 00:00:00.0"));
        ok(lcs.classify(marzoratti).category().equals("DATA GIORNO/MESE INVERTITI"),"Marzoratti swapped DOB");
        var iacoponi=collision(14055,
            source("2006_2007",1,14055,"IACOPINI Simone","1987-04-30"),
            source("2007_2008",1,14055,"IACOPONI Simone","1987-04-30"));
        ok(lcs.classify(iacoponi).category().equals("VARIANTE ORTOGRAFICA"),"Iacoponi typo");
        var hamsik=collision(12830,
            source("2007_2008",1,12830,"HAMSYK Marek","1987-07-27"),
            source("2008_2009",1,12830,"HAMSIK Marek","1987-07-27"));
        ok(lcs.classify(hamsik).category().equals("VARIANTE DOCUMENTATA LCS"),"Hamsik confirmed alias in LCS");
        var separate=collision(14085,
            source("2009_2010",1,14085,"FRISON Alberto","1988-01-22"),
            source("2015_2016",2,14085,"ZAPPINO Massimo","1981-06-12"));
        ok(lcs.classify(separate).category().equals("POSSIBILI PERSONE DIVERSE"),"Frison/Zappino collision");
        ok(lcs.classify(separate).requiresIndividualReview(),"severe collision must need individual review");
        var ascoli=collision(12302,
            source("2006_2007",1,12302,"ASCOLI Nicola","1979-11-09"),
            source("2007_2008",1,12302,"ASCOLI Nicola","1979-09-11"),
            source("2015_2016",2,12302,"CRIVELLO Roberto","1991-09-14"));
        ok(lcs.classify(ascoli).requiresIndividualReview(),"Ascoli/Crivello must not auto-merge");
        var reg=new TopFlopPlayerIdentities.Registry();
        ok(reg.approved().isEmpty(),"CSV must NOT grant automatic approval");
        reg.approveSame(iacoponi);
        var backup=reg.snapshot();
        reg.distinguish(separate);
        reg.restore(backup);
        ok(!reg.isDistinct(separate)&&reg.isMerged(iacoponi),"registry transaction snapshot/rollback");
        Path file=Files.createTempFile("identity-evidence-v38", ".json");
        try {
            TopFlopPlayerIdentities.save(file,reg);
            var restored=TopFlopPlayerIdentities.load(file);
            ok(restored.isMerged(iacoponi),"registry persistence");
            ok(!restored.isMerged(separate),"unapproved identities stay separate");
        } finally { Files.deleteIfExists(file); }
        System.out.println("RN4_IDENTITY_EVIDENCE_V39_OK aliases=569 historical=1531 checked=2133 conflicts classification approvals persistence");
    }
}
~~~~

## `docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v31.md`

SHA256: `cd62d3bf4cddacc02a88ceac713ee1fb38549f43db1a12a221f6e14e303a842f`

~~~~markdown
# RecordsNext 4.0 - Diario v31 / evidenze LCS storiche e installazione sicura

Data: 2026-11-11. Destinazione: esclusivamente RecordsNext4.0_TEST_v37.

## Intervento

- Correzione errore v38: diario e bibbia v30 gia esistenti nella v37 NON devono essere sovrascritti.
- Evidenze di origine archiviate separatamente: 2015 (401), 2019 (561), 2020 (569), piu file attuale (569) e controllo (2133).
- Il CSV 2020 risulta IDENTICO byte per byte al CSV attuale; anche la copia conservata mantiene la provenienza.
- Le versioni vecchie sono informazioni di confronto; non diventano automaticamente decisioni.
- In caso di destinazioni storiche discordanti la GUI mostra `FONTI LCS STORICHE DISCORDANTI` e richiede verifica individuale.
- Non cambiare la base FCA/FCM, gli ID originali, le decisioni persistenti, il motore squadre 3.2 o i JS pubblicati.
- La fonte corrente formula proposte, ma tutte le decisioni restano manuali e reversibili.

## Esempi di divergenza esaminati

- Maggio: cambiamenti sia nel nome sia nel giorno/mese di nascita tra 2015 e 2020.
- Okaka: normalizzazione differente del nome completo e data incerta in una versione vecchia.
- Felipe Dal Belo: due forme canoniche del nome tra 2015 e 2020.
- Caceres: differenze di apostrofo/accento tra 2019 e 2020, equivalenti dopo normalizzazione: non sono automaticamente una collisione di persone.

## Limiti

Il confronto LCS e' solo documentale: non autorizza fusioni. Se il target richiede un record di possesso o l'accorpamento di codici, decide sempre l'utente nella GUI.

## QA

- Test Java delle 21 stagioni e smoke test di riconciliazione da eseguire su copia di test.
- Installazione protetta con manifest SHA256, backup e rollback dei file patch.
- Nessun comando di elaborazione o pubblicazione viene avviato dall'installatore.
~~~~

## Evidenze CSV esterne (mai approvazioni automatiche)

- `config/identity-evidence/LCS_GiocatoriAccoppiamenti.csv`: 33132 byte, SHA256 `ea00b613be1416cc1e07951b0227a1c77a652fb5ceafe78086d6ca217c3defc7`
- `config/identity-evidence/LCS_GiocatoriAccoppiamenti_2020.csv`: 33132 byte, SHA256 `ea00b613be1416cc1e07951b0227a1c77a652fb5ceafe78086d6ca217c3defc7`
- `config/identity-evidence/LCS_GiocatoriAccoppiamenti_2019.csv`: 32652 byte, SHA256 `099c7ce3650938c7ad16c3733a5ca3ce118510fdc7e96acbe9220915cb210a90`
- `config/identity-evidence/LCS_GiocatoriAccoppiamenti_2015_old.csv`: 23051 byte, SHA256 `5c426998d12dfde862bd3dec290824cfd434168b514caeaa6e5e702bda446935`
- `config/identity-evidence/LCS_ControlloGiocatori.csv`: 69066 byte, SHA256 `cc3cea2e9b419be681d2ebb34150667f5ff84c3c7a08bbcd64d0fd16eca4e86c`


# AGGIORNAMENTO v32 - 11 novembre 2026

# RecordsNext 4.0 - Diario implementazione v32 (identita distinte, 2026-11-11)

## Vincoli invariati
- Nessuna fusione di calciatori senza approvazione esplicita in GUI.
- FCA, FCM, SQLite, motore squadre/competizioni RecordsNext 3.2 e JS legacy immutati.
- Lavoro sulla copia isolata TEST_v37 gia aggiornata a v39.
- `config/player-identities.json` NON e modificato dall'installazione.
- Nessun sito pubblicato dall'installatore.

## Bug v39 risolto
La decisione `PERSONE DISTINTE` per un codice Gazzetta riutilizzato era persistente,
ma non imponeva un vincolo in fase di collegamenti successivi. Inoltre non era chiaro
come approvare una carriera interna al codice, ad esempio:
- `2006_2007:224`: ASCOLI Nicola, data 1979-11-09
- `2007_2008:364`: ASCOLI Nicola, data 1979-09-11 (giorno e mese invertiti)
- `2015_2016:175`: CRIVELLO Roberto, data 1991-09-14

La v40 conserva la separazione, offre il pulsante `Approva gruppi interni` dopo
la scelta `PERSONE DISTINTE`, con anteprima e conferma esplicita. I due Ascoli
formano un gruppo proposto, senza includere Crivello. Prima del click nessuna
nuova identita viene approvata.

`Registry.bindAudit` ricostruisce in sola memoria i vincoli delle separazioni
gia decise a partire dal fingerprint persistente e dai dati FCA correnti.
`Registry.approve` rifiuta e annulla fusioni che attraversano gruppi diversi
in un codice dichiarato distinto. Lo stesso controllo e richiamato dal publisher
prima di produrre le viste storiche per Renewo. Nessun cambio di schema del JSON.

Revocando la separazione non si cancellano i gruppi interni approvati; per
revocarli occorre selezionare e revocare la proposta corrispondente.

## Verifiche eseguite
- Compilazione completa Java 21 di tutti i sorgenti `src/main/java` ricostruiti
  da progetto v28 + patch v37 + patch v39 + v40.
- `TopFlopSeparatedIdentitySmokeTest`: OK.
- `TopFlopHistoricIdentitySmokeTest`: OK.
- `TopFlopIdentityEvidenceSmokeTest`: OK, CSV: 569/1531/2133.
- Audit integrale su 16.262 anagrafiche: 3165 proposte, 71 codici ambigui.
- Caso 12302: un solo sottogruppo Ascoli, due annate, Crivello escluso.

## Ambito e collaudo rimanente
Non e stato possibile provare fisicamente il click Swing su Windows ne la build
Maven con driver SQLite nell'ambiente Windows dell'utente: li verifica
l'installatore nella copia di test. Non installare sulla 4.0 principale.


## File aggiornato: src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopPlayerIdentities.java

```java
package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.config.MiniJson;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Explicitly approved player identities. Only player IDs and decisions are stored here;
 * the original FCA/FCM IDs, names and rows are never rewritten. In particular, the
 * Gazzetta code is a hint, not a unique person identifier.
 */
public final class TopFlopPlayerIdentities {
    private TopFlopPlayerIdentities() {}
    public static final String FILE_NAME = "player-identities.json";

    public record Source(String season, int id, Integer code, String name, String birth) {
        public String key() { return season + ":" + id; }
    }
    public record Proposal(String fingerprint, List<Source> members, boolean safe, String reason) {
        public String description() {
            return members.stream().map(s -> s.season() + " " + s.name() + " (#" + s.code() + ")")
                    .collect(Collectors.joining(" | "));
        }
    }
    public record Collision(String fingerprint, int code, List<Source> members) {
        public String description() {
            return "Codice " + code + ": " + members.stream()
                    .map(s -> s.season() + " " + s.name())
                    .collect(Collectors.joining(" | "));
        }
    }
    public record Audit(List<Proposal> proposals, List<Collision> collisions, int sources) {}

    public static final class Registry {
        private final Map<String,String> approved = new LinkedHashMap<>();
        private final Set<String> ignored = new LinkedHashSet<>();
        private final Set<String> distinct = new LinkedHashSet<>();
        // Derived from explicit, persisted distinct fingerprints and current FCA evidence.
        // Not stored as new identity assignments: no implicit reconciliation.
        private final Map<String,Collision> distinctEvidence = new LinkedHashMap<>();
        public Map<String,String> approved() {return Map.copyOf(approved);}
        /** In-memory transaction snapshot: failed bulk actions must never leave partial changes. */
        public Registry snapshot() {
            Registry copy=new Registry();
            copy.approved.putAll(approved);
            copy.ignored.addAll(ignored);
            copy.distinct.addAll(distinct);
            copy.distinctEvidence.putAll(distinctEvidence);
            return copy;
        }
        public void restore(Registry snapshot) {
            approved.clear(); approved.putAll(snapshot.approved);
            ignored.clear(); ignored.addAll(snapshot.ignored);
            distinct.clear(); distinct.addAll(snapshot.distinct);
            distinctEvidence.clear(); distinctEvidence.putAll(snapshot.distinctEvidence);
        }
        public boolean isIgnored(Proposal p) {return ignored.contains(p.fingerprint());}
        public boolean isDistinct(Collision c) {return distinct.contains(c.fingerprint());}
        public boolean isMerged(Collision c) {
            String id=null;
            for(Source source:c.members()) {
                String assigned=approved.get(source.key());
                if(assigned==null)return false;
                if(id!=null&&!id.equals(assigned))return false;
                id=assigned;
            }
            return true;
        }
        /** Rebuild guards for PREVIOUSLY confirmed separations after reopening the GUI
         *  or preparing the Renewo export. Reuses decisions, never approves new links. */
        public void bindAudit(Audit audit) {
            distinctEvidence.clear();
            for(Collision c:audit.collisions())if(isDistinct(c))distinctEvidence.put(c.fingerprint(),c);
            validateDistinctAssignments();
        }
        public void approveSame(Collision collision) {
            boolean previous=distinct.remove(collision.fingerprint());
            Collision old=distinctEvidence.remove(collision.fingerprint());
            try {
                approve(new Proposal(collision.fingerprint(),collision.members(),false,
                    "Stesso calciatore, approvato manualmente"));
            } catch(RuntimeException ex) {
                if(previous)distinct.add(collision.fingerprint());
                if(old!=null)distinctEvidence.put(collision.fingerprint(),old);
                throw ex;
            }
        }
        public void undo(Proposal proposal) {
            proposal.members().forEach(s->approved.remove(s.key()));
            ignored.remove(proposal.fingerprint());
        }
        public void undo(Collision collision) {
            if(distinct.remove(collision.fingerprint())) {
                // Revoking the separation must NOT erase separately approved
                // internal careers (e.g. Ascoli 2006/07 + 2007/08).
                distinctEvidence.remove(collision.fingerprint());
            } else {
                collision.members().forEach(s->approved.remove(s.key()));
            }
        }
        public int approvedPlayers() {return approved.size();}
        public int approvedIdentities() {return new HashSet<>(approved.values()).size();}
        public void ignore(Proposal p) {ignored.add(p.fingerprint());}
        public void distinguish(Collision c) {
            boolean added=distinct.add(c.fingerprint());
            Collision prior=distinctEvidence.put(c.fingerprint(),c);
            try{validateDistinctAssignments();}
            catch(RuntimeException ex){
                if(added)distinct.remove(c.fingerprint());
                if(prior==null)distinctEvidence.remove(c.fingerprint());
                else distinctEvidence.put(c.fingerprint(),prior);
                throw ex;
            }
        }
        /** Reject transitive mergers across groups the user marked as different. */
        public void validateDistinctAssignments() {
            for(Collision c:distinctEvidence.values()) {
                Map<String,Integer> identityClusters=new HashMap<>();
                List<List<Source>> clusters=internalClusters(c);
                for(int i=0;i<clusters.size();i++)for(Source source:clusters.get(i)) {
                    String assigned=approved.get(source.key());
                    if(assigned==null)continue;
                    Integer previous=identityClusters.putIfAbsent(assigned,i);
                    if(previous!=null && previous!=i)
                        throw new IllegalArgumentException("Identita' gia' dichiarate distinte, codice "
                            +c.code()+": revocare prima la separazione per unirle.");
                }
            }
        }
        public void approve(Proposal p) {
            if(p.members().size()<2)throw new IllegalArgumentException("Gruppo non storico");
            if(p.members().stream().map(Source::season).distinct().count()!=p.members().size())
                throw new IllegalArgumentException("Identita' diverse nella stessa stagione: " + p.description());
            String chosen=null;
            for(Source s:p.members()) {
                String old=approved.get(s.key());
                if(old!=null) {
                    if(chosen!=null && !chosen.equals(old))
                        throw new IllegalArgumentException("Associazioni approvate in conflitto: " + p.description());
                    chosen=old;
                }
            }
            if(chosen==null) chosen="rn4p-"+p.members().stream().map(Source::key).sorted().findFirst().orElseThrow().replace(':','-');
            Map<String,String> previous=new LinkedHashMap<>(approved);
            try {
                for(Source s:p.members())approved.put(s.key(),chosen);
                validateDistinctAssignments();
            } catch(RuntimeException ex) {
                approved.clear();approved.putAll(previous);
                throw ex;
            }
            ignored.remove(p.fingerprint());
        }
        public boolean complete(Proposal p) {
            String id=null;
            for(Source s:p.members()) {
                String assigned=approved.get(s.key());
                if(assigned==null)return false;
                if(id!=null && !id.equals(assigned))return false;
                id=assigned;
            }
            return true;
        }
        /** Expired decisions are not auto-reapproved; new evidence is a new fingerprint. */
        public boolean requiresReview(Audit audit) {
            return audit.proposals().stream().anyMatch(p->!complete(p)&&!isIgnored(p))
                 || audit.collisions().stream().anyMatch(c->!isDistinct(c)&&!isMerged(c));
        }
    }

    public static Registry load(Path file) throws IOException {
        Registry r=new Registry();
        if(!Files.isRegularFile(file))return r;
        Object parsed;
        try {parsed=MiniJson.parse(Files.readString(file,StandardCharsets.UTF_8));}
        catch(RuntimeException ex){throw new IOException("Identita' storiche: JSON non valido",ex);}
        if(!(parsed instanceof Map<?,?> map) || !"rn4-player-identities-v1".equals(map.get("schemaVersion")))
            throw new IOException("Schema non riconosciuto per " + file);
        Object entries=map.get("approved");
        if(!(entries instanceof Map<?,?> m))throw new IOException("Mappa approved mancante");
        for(var entry:m.entrySet()) {
            String key=String.valueOf(entry.getKey()), id=String.valueOf(entry.getValue());
            if(!key.matches("[0-9]{4}_[0-9]{4}:[0-9]+") || !id.matches("rn4p-[0-9]{4}_[0-9]{4}-[0-9]+"))
                throw new IOException("Identita' non valida: " + key + "/" + id);
            r.approved.put(key,id);
        }
        readFingerprints(map.get("ignored"),r.ignored);
        readFingerprints(map.get("distinct"),r.distinct);
        return r;
    }
    private static void readFingerprints(Object raw,Set<String> out) throws IOException {
        if(raw==null)return;
        if(!(raw instanceof List<?> list))throw new IOException("Elenco fingerprint invalido");
        for(Object x:list){String s=String.valueOf(x);if(!s.matches("[0-9a-f]{64}"))throw new IOException("Fingerprint invalido");out.add(s);}
    }
    public static void save(Path file,Registry r) throws IOException {
        Files.createDirectories(file.toAbsolutePath().getParent());
        Map<String,Object> data=new LinkedHashMap<>();
        data.put("schemaVersion","rn4-player-identities-v1");
        data.put("approved",new TreeMap<>(r.approved));
        data.put("ignored",r.ignored.stream().sorted().toList());
        data.put("distinct",r.distinct.stream().sorted().toList());
        String json=TopFlopCompactStageJsExporter.Json.write(data)+"\n";
        Path tmp=Files.createTempFile(file.getParent(),".player-identities-",".tmp");
        try{
            Files.writeString(tmp,json,StandardCharsets.UTF_8);
            try{Files.move(tmp,file,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}
            catch(java.nio.file.AtomicMoveNotSupportedException ex){Files.move(tmp,file,StandardCopyOption.REPLACE_EXISTING);}
        }finally{Files.deleteIfExists(tmp);}
    }

    /** Explicitly reviewable subgroups of a collision: SAME normalized name and
     *  compatible birth date (including inverted day/month), never other names.
     *  They remain unapplied until the user confirms them in the GUI. */
    public static List<Proposal> internalGroups(Collision collision) {
        List<Proposal> result=new ArrayList<>();
        for(List<Source> group:internalClusters(collision)) {
            if(group.size()<2 || group.stream().map(Source::season).distinct().count()!=group.size())continue;
            group.sort(Comparator.comparing(Source::key));
            boolean inverted=group.stream().anyMatch(a->group.stream().anyMatch(b->
                !a.equals(b) && !java.util.Objects.equals(dob(a.birth()),dob(b.birth())) && birthSwapped(a.birth(),b.birth())));
            result.add(new Proposal(fingerprint(group),List.copyOf(group),false,
                inverted?"Nome e codice uguali; nascita con giorno/mese invertiti (conferma necessaria)":
                    "Nome, codice e nascita corrispondenti (conferma necessaria)"));
        }
        return List.copyOf(result);
    }
    private static List<List<Source>> internalClusters(Collision collision) {
        List<List<Source>> groups=new ArrayList<>();
        for(Source source:collision.members()) {
            List<Source> candidate=null;
            for(List<Source> group:groups) {
                if(group.stream().anyMatch(other->sameWithinCollision(source,other))) {
                    candidate=group;break;
                }
            }
            if(candidate==null){candidate=new ArrayList<>();groups.add(candidate);}
            candidate.add(source);
        }
        return groups;
    }
    private static boolean sameWithinCollision(Source a,Source b) {
        if(!normalize(a.name()).equals(normalize(b.name())))return false;
        String x=dob(a.birth()),y=dob(b.birth());
        return x!=null&&y!=null&&(x.equals(y)||birthSwapped(x,y));
    }
    private static boolean birthSwapped(String first,String second) {
        String a=dob(first),b=dob(second);
        return a!=null&&b!=null&&a.substring(0,4).equals(b.substring(0,4))
            &&a.substring(5,7).equals(b.substring(8,10))
            &&a.substring(8,10).equals(b.substring(5,7));
    }

    /** All imported FCA seasons in the current canonical database. */
    public static Audit scanAvailable(Connection c) throws SQLException {
        List<String> seasons=new ArrayList<>();
        try(Statement st=c.createStatement();ResultSet rows=st.executeQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name LIKE 'raw_%_fca_giocatorea'")) {
            while(rows.next()) {
                String name=rows.getString(1);
                if(name.matches("raw_[0-9]{4}_[0-9]{4}_fca_giocatorea"))
                    seasons.add(name.substring(4,13));
            }
        }
        seasons.sort(String::compareTo);
        return scan(c,seasons);
    }

    public static Audit scan(Connection c,List<String> seasons) throws SQLException {
        List<Source> all=new ArrayList<>();
        for(String season:seasons) {
            if(!season.matches("[0-9]{4}_[0-9]{4}"))throw new IllegalArgumentException("Stagione invalida");
            String table="raw_"+season+"_fca_giocatorea";
            String sql="SELECT ID,CODICEGAZZA,NOME,DATADINASCITA FROM \""+table+"\" ORDER BY ID";
            try(Statement st=c.createStatement();ResultSet rs=st.executeQuery(sql)) {
                while(rs.next()) {
                    int code=rs.getInt(2);
                    all.add(new Source(season,rs.getInt(1),rs.wasNull()||code<=0?null:code,
                        rs.getString(3),rs.getString(4)));
                }
            }
        }
        return scanSources(all);
    }

    /** Same audit on provided FCA rows; useful for regression testing without a JDBC driver. */
    public static Audit scanSources(List<Source> all) {
        // Candidate links are deliberately conservative. Same Gazzetta code with
        // different names and birth dates is NOT enough to join two people.
        Map<String,List<Source>> byCodeName=new LinkedHashMap<>();
        Map<String,List<Source>> byBirthName=new LinkedHashMap<>();
        Map<Integer,List<Source>> byCode=new TreeMap<>();
        for(Source s:all) {
            String n=normalize(s.name());
            if(s.code()!=null) {
                byCode.computeIfAbsent(s.code(),x->new ArrayList<>()).add(s);
                byCodeName.computeIfAbsent(s.code()+"/"+n,x->new ArrayList<>()).add(s);
            }
            String birth=dob(s.birth());
            if(birth!=null && !n.isEmpty()) {
                // Keep surname + first given name; do not merge common surnames
                // on a birthday alone, nor invent reversed-name permutations.
                String[] tokens=n.split(" ");
                if(tokens.length>=2) byBirthName.computeIfAbsent(birth+"/"+tokens[0]+"/"+tokens[1],x->new ArrayList<>()).add(s);
            }
        }
        Map<String,String> parents=new HashMap<>();
        Map<String,Set<String>> rootSeasons=new HashMap<>();
        for(Source s:all){parents.put(s.key(),s.key());rootSeasons.put(s.key(),new HashSet<>(Set.of(s.season())));}
        for(List<Source> group:byCodeName.values())unionGroup(group,parents,rootSeasons);
        for(List<Source> group:byBirthName.values())unionGroup(group,parents,rootSeasons);
        Map<String,List<Source>> grouped=new TreeMap<>();
        for(Source s:all)grouped.computeIfAbsent(find(parents,s.key()),x->new ArrayList<>()).add(s);
        Set<Integer> disputedCodes=new HashSet<>();
        List<Collision> collisions=new ArrayList<>();
        for(var e:byCode.entrySet()) {
            List<Source> members=e.getValue();
            if(members.stream().map(s->find(parents,s.key())).distinct().count()>1 &&
                    members.stream().map(s->normalize(s.name())).distinct().count()>1) {
                disputedCodes.add(e.getKey());
                collisions.add(new Collision(fingerprint(members),e.getKey(),List.copyOf(members)));
            }
        }
        List<Proposal> proposals=new ArrayList<>();
        for(List<Source> group:grouped.values()) {
            if(group.size()<2)continue;
            group.sort(Comparator.comparing(Source::key));
            long seasonsCount=group.stream().map(Source::season).distinct().count();
            boolean birthConflict=group.stream().map(s->dob(s.birth())).filter(x->x!=null).distinct().count()>1;
            boolean codeCollision=group.stream().anyMatch(s->s.code()!=null&&disputedCodes.contains(s.code()));
            boolean namesEqual=group.stream().map(s->normalize(s.name())).distinct().count()==1;
            boolean safe=seasonsCount==group.size()&&!birthConflict&&!codeCollision&&namesEqual;
            String reason=birthConflict?"Date di nascita discordanti":
                (seasonsCount<group.size()?"Piu' persone nella stessa stagione":
                (codeCollision?"Codice storico riutilizzato":"Nomi/codici da verificare"));
            proposals.add(new Proposal(fingerprint(group),List.copyOf(group),safe,safe?"Corrispondenza esatta":reason));
        }
        proposals.sort(Comparator.comparing((Proposal p)->p.safe()?1:0)
            .thenComparing(p->p.members().get(0).name(),String.CASE_INSENSITIVE_ORDER));
        return new Audit(List.copyOf(proposals),List.copyOf(collisions),all.size());
    }
    private static void unionGroup(List<Source> group,Map<String,String> p,Map<String,Set<String>> roots) {
        if(group.size()<2)return;
        String first=group.get(0).key();
        for(int i=1;i<group.size();i++) {
            String other=group.get(i).key();
            // No two different player records in the same season may be joined.
            if(group.get(i).season().equals(group.get(0).season()))continue;
            String a=find(p,first),b=find(p,other);
            if(a.equals(b))continue;
            Set<String> firstSeasons=roots.get(a), secondSeasons=roots.get(b);
            boolean conflict=secondSeasons.stream().anyMatch(firstSeasons::contains);
            if(!conflict){p.put(b,a);firstSeasons.addAll(secondSeasons);roots.remove(b);}
        }
    }
    private static String find(Map<String,String> p,String key) {
        String parent=p.get(key);if(parent.equals(key))return key;
        String root=find(p,parent);p.put(key,root);return root;
    }
    private static String normalize(String raw) {
        if(raw==null)return "";
        return Normalizer.normalize(raw,Normalizer.Form.NFD).replaceAll("\\p{M}","")
            .toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+"," ").trim().replaceAll(" +"," ");
    }
    private static String dob(String raw) {
        if(raw==null||raw.length()<10||!raw.substring(0,10).matches("[0-9]{4}-[0-9]{2}-[0-9]{2}"))return null;
        return raw.substring(0,10);
    }
    private static String fingerprint(Collection<Source> src) {
        try {
            String evidence=src.stream().sorted(Comparator.comparing(Source::key))
                .map(s->s.key()+"/"+s.code()+"/"+s.name()+"/"+s.birth()).collect(Collectors.joining("|"));
            byte[] hash=MessageDigest.getInstance("SHA-256").digest(evidence.getBytes(StandardCharsets.UTF_8));
            StringBuilder h=new StringBuilder();for(byte b:hash)h.append(String.format("%02x",b&255));return h.toString();
        }catch(Exception e){throw new IllegalStateException(e);}
    }
}
```

## File aggiornato: src/main/java/it/alterlega/recordsnext/gui/TopFlopIdentityReviewDialog.java

```java
package it.alterlega.recordsnext.gui;

import it.alterlega.recordsnext.app.topflop.TopFlopIdentityEvidence;
import it.alterlega.recordsnext.app.topflop.TopFlopPlayerIdentities;
import it.alterlega.recordsnext.app.topflop.TopFlopPlayerIdentities.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Approval screen for player identities: no automatic changes, ever.
 * LCS CSVs only help explain choices and are never a source of approved IDs.
 */
public final class TopFlopIdentityReviewDialog extends JDialog {
    private record Row(Proposal proposal, Collision collision, TopFlopIdentityEvidence.Assessment assessment) {
        String kind() {
            if (collision != null) return assessment.category();
            return proposal.safe() ? "CORRISPONDENZA ESATTA" : "PROPOSTA DA VERIFICARE";
        }
        String detail() { return collision!=null ? collision.description() : proposal.description(); }
        String evidence() {
            return collision!=null ? assessment.lcsEvidence() : proposal.reason();
        }
    }
    private final Registry registry;
    private final Audit audit;
    private final Path file;
    private final TopFlopIdentityEvidence evidence;
    private final List<Row> rows = new ArrayList<>();
    private final JLabel status = new JLabel();
    private final JTable table;
    private final TableRowSorter<AbstractTableModel> sorter;
    private final JTextArea detail = new JTextArea(7,100);
    private final JTextField search = new JTextField(24);
    private final JComboBox<String> filter = new JComboBox<>(new String[]{
        "Anomalie codici", "Proposte da verificare", "Proposte esatte", "Tutte"
    });

    public TopFlopIdentityReviewDialog(Frame owner, Path projectRoot) throws Exception {
        super(owner,"Identita' storiche calciatori - approvazione",true);
        file=projectRoot.resolve("config").resolve(TopFlopPlayerIdentities.FILE_NAME);
        registry=TopFlopPlayerIdentities.load(file);
        evidence=TopFlopIdentityEvidence.load(projectRoot.resolve("config").resolve("identity-evidence"));
        Path db=projectRoot.resolve("data/database/recordsnext.db");
        if (!Files.isRegularFile(db)) throw new IllegalStateException("SQLite non presente: " + db);
        Class.forName("org.sqlite.JDBC");
        try(Connection c=DriverManager.getConnection("jdbc:sqlite:"+db.toUri()+"?mode=ro")) {
            audit=TopFlopPlayerIdentities.scanAvailable(c);
            if(audit.sources()==0)throw new IllegalStateException(
                "Archivio FCA non ancora importato. Eseguire prima l'elaborazione completa, poi riconciliare.");
        }
        // Restore the effective constraints of decisions already confirmed in v39.
        // This does not merge or approve anything and does not write the registry.
        registry.bindAudit(audit);
        audit.collisions().forEach(c->rows.add(new Row(null,c,evidence.classify(c))));
        audit.proposals().forEach(p->rows.add(new Row(p,null,null)));

        var model=new AbstractTableModel() {
            public int getRowCount(){return rows.size();}
            public int getColumnCount(){return 5;}
            public String getColumnName(int col){return switch(col){
                case 0->"Categoria";case 1->"Stato";case 2->"Occ.";
                case 3->"Evidenza";default->"Stagioni / nomi / codici";};}
            public Object getValueAt(int r,int c){
                Row row=rows.get(r);
                return switch(c){
                    case 0->row.kind();
                    case 1->row.collision()!=null?(registry.isDistinct(row.collision())?"PERSONE DISTINTE":
                            registry.isMerged(row.collision())?"UNIONE APPROVATA":"DA DECIDERE"):
                        registry.complete(row.proposal())?"APPROVATO":
                        registry.isIgnored(row.proposal())?"RINVIATO":"DA DECIDERE";
                    case 2->row.collision()!=null?row.collision().members().size():row.proposal().members().size();
                    case 3->row.evidence();
                    default->row.detail();
                };
            }
        };
        setLayout(new BorderLayout(8,8));
        JPanel top=new JPanel(new BorderLayout(4,6));
        top.setBorder(BorderFactory.createEmptyBorder(10,12,6,12));
        top.add(new JLabel("<html><b>Nessuna fusione automatica.</b> Le fonti LCS sono indizi, non approvazioni. " +
            "Le versioni 2015, 2019 e 2020 restano distinte. Ogni decisione richiede il tuo clic.</html>"),BorderLayout.NORTH);
        top.add(status,BorderLayout.CENTER);
        JPanel selectors=new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectors.add(new JLabel("Visualizza:"));selectors.add(filter);
        selectors.add(new JLabel("Cerca nome / codice:"));selectors.add(search);
        selectors.add(new JLabel("Seleziona una riga per leggere tutti i dettagli."));
        top.add(selectors,BorderLayout.SOUTH);
        add(top,BorderLayout.NORTH);

        table=new JTable(model);
        sorter=new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setRowHeight(26);
        table.getColumnModel().getColumn(0).setPreferredWidth(190);
        table.getColumnModel().getColumn(1).setPreferredWidth(125);
        table.getColumnModel().getColumn(2).setPreferredWidth(45);
        table.getColumnModel().getColumn(3).setPreferredWidth(250);
        table.getColumnModel().getColumn(4).setPreferredWidth(600);
        table.getSelectionModel().addListSelectionListener(e-> {if(!e.getValueIsAdjusting()) updateDetails();});
        filter.addActionListener(e->updateFilter());
        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e){updateFilter();}
            public void removeUpdate(DocumentEvent e){updateFilter();}
            public void changedUpdate(DocumentEvent e){updateFilter();}
        });
        detail.setEditable(false);
        detail.setLineWrap(true);detail.setWrapStyleWord(true);
        detail.setText("Seleziona una riga per vedere nomi, stagioni e date di nascita complete.");
        JScrollPane detailScroll=new JScrollPane(detail);
        detailScroll.setBorder(BorderFactory.createTitledBorder("Dettaglio delle anagrafiche e riscontri LCS"));
        JSplitPane body=new JSplitPane(JSplitPane.VERTICAL_SPLIT,new JScrollPane(table),detailScroll);
        body.setResizeWeight(0.75);
        add(body,BorderLayout.CENTER);

        JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT,6,5));
        JButton exact=new JButton("Approva le proposte esatte");
        exact.addActionListener(e->{
            long pending=audit.proposals().stream().filter(p->p.safe()&&!registry.complete(p)).count();
            if(pending==0){JOptionPane.showMessageDialog(this,"Nessuna proposta esatta ancora da approvare.");return;}
            int reply=JOptionPane.showConfirmDialog(this,
                "Approvare esplicitamente "+pending+" corrispondenze esatte?\n"+
                "I codici ambigui non saranno inclusi.","Approvazione collettiva",JOptionPane.YES_NO_OPTION);
            if(reply==JOptionPane.YES_OPTION)act(()->{
                for(Proposal p:audit.proposals())if(p.safe()&&!registry.complete(p))registry.approve(p);
            });
        });
        JButton approve=new JButton("Approva proposte selezionate");
        approve.addActionListener(e->{
            if(selected().isEmpty()){JOptionPane.showMessageDialog(this,"Seleziona una proposta.");return;}
            act(()->{
            for(Row r:selected())if(r.proposal()!=null){
                for(Collision c:audit.collisions()) {
                    if(!registry.isDistinct(c) && r.proposal().members().stream()
                        .anyMatch(x->x.code()!=null&&x.code()==c.code()))
                        throw new IllegalArgumentException("Risolvere prima il codice ambiguo " + c.code());
                }
                registry.approve(r.proposal());
            }
        });});
        JButton distinct=new JButton("Conferma persone diverse");
        distinct.addActionListener(e->{
            List<Row> chosen=selected();
            if(chosen.stream().noneMatch(r->r.collision()!=null)){
                JOptionPane.showMessageDialog(this,"Seleziona almeno un codice ambiguo.");return;
            }
            int ok=JOptionPane.showConfirmDialog(this,
                "Confermi che le identita' collegate ai codici selezionati NON devono essere unite?\n"+
                "Le proposte interne resteranno esaminabili separatamente.",
                "Separazione identita'",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
            if(ok==JOptionPane.YES_OPTION)act(()->{
                for(Row r:chosen)if(r.collision()!=null)registry.distinguish(r.collision());
            });
        });
        JButton same=new JButton("Conferma stessa persona");
        same.addActionListener(e->{
            List<Row> selected=selected().stream().filter(r->r.collision()!=null).toList();
            if(selected.isEmpty()){JOptionPane.showMessageDialog(this,"Seleziona almeno un codice ambiguo.");return;}
            boolean serious=selected.stream().anyMatch(r->r.assessment().requiresIndividualReview());
            if(serious && selected.size()!=1){
                JOptionPane.showMessageDialog(this,"I casi incerti o gravi vanno confermati uno alla volta.");return;
            }
            if(serious) {
                int code=selected.get(0).collision().code();
                String confirmation=JOptionPane.showInputDialog(this,
                    "Attenzione: identita' incerta. Verifica nomi e date.\n"+
                    "Per unirle davvero scrivi UNISCI " + code + ":",
                    "Conferma eccezionale",JOptionPane.WARNING_MESSAGE);
                if(!("UNISCI "+code).equals(confirmation))return;
            } else {
                int ok=JOptionPane.showConfirmDialog(this,
                    "Confermare che tutte le occorrenze dei codici selezionati sono la stessa persona?\n"+
                    "Anche le varianti suggerite richiedono approvazione esplicita.",
                    "Approvazione varianti",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
                if(ok!=JOptionPane.YES_OPTION)return;
            }
            act(()->{for(Row r:selected)registry.approveSame(r.collision());});
        });
        JButton internal=new JButton("Approva gruppi interni");
        internal.addActionListener(e->{
            List<Row> chosen=selected().stream().filter(r->r.collision()!=null).toList();
            if(chosen.isEmpty()){
                JOptionPane.showMessageDialog(this,"Seleziona un codice gia' segnato PERSONE DISTINTE.");return;
            }
            List<Proposal> groups=new ArrayList<>();
            for(Row r:chosen) {
                if(!registry.isDistinct(r.collision())){
                    JOptionPane.showMessageDialog(this,"Prima conferma PERSONE DISTINTE per il codice "
                        +r.collision().code());return;
                }
                for(Proposal p:TopFlopPlayerIdentities.internalGroups(r.collision()))
                    if(!registry.complete(p))groups.add(p);
            }
            if(groups.isEmpty()){
                JOptionPane.showMessageDialog(this,"Non ci sono gruppi interni verificabili da approvare.");return;
            }
            StringBuilder summary=new StringBuilder();
            for(Proposal p:groups)summary.append(p.reason()).append("\n")
                .append(p.description()).append("\n\n");
            JTextArea preview=new JTextArea(summary.toString(),Math.min(groups.size()*3+3,19),85);
            preview.setEditable(false);preview.setLineWrap(true);preview.setWrapStyleWord(true);
            int ok=JOptionPane.showConfirmDialog(this,new JScrollPane(preview),
                "Approva ESPLICITAMENTE "+groups.size()+" gruppi interni (nessuna fusione automatica)",
                JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
            if(ok==JOptionPane.YES_OPTION)act(()->{
                for(Proposal p:groups)registry.approve(p);
            });
        });
        JButton undo=new JButton("Revoca decisioni selezionate");
        undo.addActionListener(e->{
            if(selected().isEmpty()){JOptionPane.showMessageDialog(this,"Seleziona una decisione.");return;}
            act(()->{
            for(Row r:selected()){
                if(r.collision()!=null)registry.undo(r.collision());
                else registry.undo(r.proposal());
            }
        });});
        JButton ignore=new JButton("Rimanda proposte selezionate");
        ignore.addActionListener(e->{
            if(selected().stream().noneMatch(r->r.proposal()!=null)){
                JOptionPane.showMessageDialog(this,"Seleziona una proposta da rimandare.");return;
            }
            act(()->{
                for(Row r:selected())if(r.proposal()!=null)registry.ignore(r.proposal());
            });
        });
        JButton close=new JButton("Chiudi");close.addActionListener(e->dispose());
        for(JButton b:new JButton[]{exact,approve,distinct,internal,same,undo,ignore,close})actions.add(b);
        add(actions,BorderLayout.SOUTH);
        setSize(1460,790);setLocationRelativeTo(owner);
        updateFilter();updateStatus();
    }
    private void updateFilter(){
        String needle=search.getText().strip().toUpperCase(Locale.ROOT);
        int mode=filter.getSelectedIndex();
        sorter.setRowFilter(new RowFilter<>(){
            public boolean include(Entry<? extends AbstractTableModel,? extends Integer> e){
                Row r=rows.get(e.getIdentifier());
                if(mode==0 && r.collision()==null)return false;
                if(mode==1 && (r.proposal()==null || r.proposal().safe()))return false;
                if(mode==2 && (r.proposal()==null || !r.proposal().safe()))return false;
                String text=(r.kind()+" "+r.detail()+" "+r.evidence()).toUpperCase(Locale.ROOT);
                return needle.isEmpty()||text.contains(needle);
            }
        });
        updateDetails();
    }
    private void updateDetails(){
        int selected=table.getSelectedRow();
        if(selected<0){detail.setText("Seleziona un codice per esaminare tutte le stagioni, i nomi e le date.");return;}
        Row r=rows.get(table.convertRowIndexToModel(selected));
        var b=new StringBuilder(r.kind()+" - "+r.evidence()+"\n");
        if(r.collision()!=null){
            b.append(r.assessment().explanation()).append("\n\n");
            for(Source s:r.collision().members())b.append(s.season()).append(" | ")
                .append(s.name()).append(" | codice ").append(s.code())
                .append(" | nascita ").append(s.birth()).append("\n");
            b.append("\n").append(evidence.evidenceDetails(r.collision()));
            if(registry.isDistinct(r.collision())) {
                var groups=TopFlopPlayerIdentities.internalGroups(r.collision());
                if(groups.isEmpty())b.append("\nNessun sottogruppo verificabile; mantenere le carriere separate.");
                for(Proposal p:groups)b.append("\nGRUPPO INTERNO ")
                    .append(registry.complete(p)?"GIA' APPROVATO: ":"DA APPROVARE: ")
                    .append(p.reason()).append("\n").append(p.description()).append("\n");
            }
        } else {
            b.append(r.proposal().reason()).append("\n\n");
            for(Source s:r.proposal().members())b.append(s.season()).append(" | ")
                .append(s.name()).append(" | codice ").append(s.code())
                .append(" | nascita ").append(s.birth()).append("\n");
        }
        detail.setText(b.toString());detail.setCaretPosition(0);
    }
    private List<Row> selected(){
        List<Row> out=new ArrayList<>();
        for(int idx:table.getSelectedRows())out.add(rows.get(table.convertRowIndexToModel(idx)));
        return out;
    }
    private void act(Runnable operation){
        Registry before=registry.snapshot();
        try {
            operation.run();
            TopFlopPlayerIdentities.save(file,registry);
            ((AbstractTableModel)table.getModel()).fireTableDataChanged();
            updateStatus();updateFilter();
        } catch(Exception ex){
            registry.restore(before);
            ((AbstractTableModel)table.getModel()).fireTableDataChanged();
            JOptionPane.showMessageDialog(this,ex.getMessage(),"Verifica identita'",JOptionPane.ERROR_MESSAGE);
        }
    }
    private void updateStatus(){
        long pending=audit.proposals().stream().filter(p->!registry.complete(p)&&!registry.isIgnored(p)).count();
        long collisions=audit.collisions().stream().filter(c->!registry.isDistinct(c)&&!registry.isMerged(c)).count();
        status.setText("Anagrafiche: "+audit.sources()+"  |  proposte: "+audit.proposals().size()+
            "  |  approvazioni: "+registry.approvedIdentities()+
            "  |  in sospeso: "+pending+"  |  codici da decidere: "+collisions+
            "  |  LCS: "+evidence.aliasRows()+" correnti, "+evidence.historicalRows()+" storici"
            +" ("+evidence.historicalConflicts()+" chiavi discordanti), "+evidence.checkedRows()+" controlli");
    }
}
```

## File aggiornato: src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopViewsPublication.java

```java
package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.config.MiniJson;
import it.alterlega.recordsnext.app.config.ProcessingConfigWriter;
import it.alterlega.recordsnext.app.model.ProcessingSelection;
import it.alterlega.recordsnext.app.model.RecordFamily;

import java.io.IOException;
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
            result = TopFlopPublicViewsStageExporter.export(db, seasonIds, output, reports, selectedIds, settings);
        }
        for (var file : result.files()) {
            Path path = output.resolve(file.name());
            validate(path, file.bytes(), file.sha256());
        }
        Map<String,String> identities=Map.of();
        if(identitiesFile!=null) {
            TopFlopPlayerIdentities.Registry reg=TopFlopPlayerIdentities.load(identitiesFile);
            try(Connection db=DriverManager.getConnection("jdbc:sqlite:"+sqlite.toUri()+"?mode=ro")) {
                reg.bindAudit(TopFlopPlayerIdentities.scanAvailable(db));
            }
            // A confirmed separation must also hold during publication, not only in GUI.
            identities=reg.approved();
        }
        return new Prepared(output, Set.copyOf(seasonIds), result.files().size(),identities);
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
                if(!name.matches("fcmRecordsNext_TopFlopViews\\.(?:Careers|Teams|Records|Highlights)\\."
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
            prepared.approvedIdentities());
        List<TopFlopPublicViewsStageExporter.FileInfo> historyFiles=history.isEmpty()
            ?List.of():TopFlopPublicViewsStageExporter.shard(generatedJs,"Storico","History",
                new ArrayList<Object>(history));
        if(!historyFiles.isEmpty()) {
            scoped.put("viste",List.of("Careers","Teams","Records","Highlights","History"));
        }
        scoped.put("storico",Map.of("status",history.isEmpty()?"NESSUNA_IDENTITA_APPROVATA_NEL_CUTOFF":"IDENTITA_APPROVATE",
            "identita",history.size(),"file",historyFiles.stream().map(f->Map.of(
                "nome",f.name(),"tipo",f.kind(),"righe",f.rows(),"byte",f.bytes(),"sha256",f.sha256())).toList(),
            "fonte","config/player-identities.json"));
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
        return sources.size()+historyFiles.size()+1;
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
                           Map<String,String> approvedIdentities) {
        public Prepared {
            approvedIdentities=Map.copyOf(approvedIdentities);
        }
    }
}
```

## File aggiornato: src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopSeparatedIdentitySmokeTest.java

```java
package it.alterlega.recordsnext.app.topflop;

import java.nio.file.Files;
import java.nio.file.Path;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Regression: one historic code used by two people; one person's seasons stay linked. */
public final class TopFlopSeparatedIdentitySmokeTest {
    private static void ok(boolean value,String msg){if(!value)throw new AssertionError(msg);}
    private static TopFlopPlayerIdentities.Source s(String season,int local,String name,String born){
        return new TopFlopPlayerIdentities.Source(season,local,12302,name,born);
    }
    private static Map<String,Object> career(String key,int votes,int total){
        return Map.of("playerKey",key,"nome","Test", "reale",
            Map.of("votiValidi",votes,"sommaVoti",total,"eventi",Map.of("GOLFATTI1",0)));
    }
    public static void main(String[] args)throws Exception {
        var ascoli06=s("2006_2007",224,"ASCOLI Nicola","1979-11-09 00:00:00.0");
        var ascoli07=s("2007_2008",364,"ASCOLI Nicola","1979-09-11 00:00:00.0");
        var crivello15=s("2015_2016",175,"CRIVELLO Roberto","1991-09-14 00:00:00.0");
        var audit=TopFlopPlayerIdentities.scanSources(List.of(ascoli06,ascoli07,crivello15));
        ok(audit.collisions().size()==1,"one disputed historical code");
        var collision=audit.collisions().get(0);
        ok(collision.code()==12302,"Ascoli/Crivello code");
        var reg=new TopFlopPlayerIdentities.Registry();
        reg.distinguish(collision);  // user's actual decision: different persons
        reg.bindAudit(audit);
        ok(reg.approved().isEmpty(),"distinguishing must not automatically approve internal links");
        var internal=TopFlopPlayerIdentities.internalGroups(collision);
        ok(internal.size()==1,"Ascoli two seasons are an internal group even with date inversion");
        var proposal=internal.get(0);
        ok(proposal.members().size()==2,"Ascoli group has exactly two members");
        ok(proposal.reason().contains("invertiti"),"inverted birth date shown to user");
        ok(proposal.members().stream().noneMatch(x->x.key().equals(crivello15.key())),"Crivello excluded");
        reg.approve(proposal);     // separate second explicit GUI confirmation
        ok(reg.approvedPlayers()==2&&reg.approvedIdentities()==1,"two seasons, one Ascoli identity");
        ok(!reg.approved().containsKey(crivello15.key()),"Crivello remains a separate person");
        var history=TopFlopHistoricalViews.aggregate(List.of(
            career(ascoli06.key(),7,42),career(ascoli07.key(),3,21),career(crivello15.key(),10,65)),reg.approved());
        ok(history.size()==1,"one Ascoli historical career, no Crivello merge");
        var h=history.get(0);
        ok(h.get("numeroStagioni").equals(2),"Ascoli keeps both seasons");
        ok(h.get("votiRealiValidi").equals(10),"no Crivello votes included");
        ok(((BigDecimal)h.get("mediaVotoReale")).compareTo(new BigDecimal("6.3"))==0,"weighted mean Ascoli");
        var dangerous=new TopFlopPlayerIdentities.Proposal("b".repeat(64),
            List.of(ascoli07,crivello15),false,"erroneous manual association");
        boolean rejected=false;
        try{reg.approve(dangerous);}catch(IllegalArgumentException ex){rejected=true;}
        ok(rejected,"previously separated people must NEVER merge indirectly");
        ok(reg.approvedPlayers()==2,"failed approval rolls back without side effects");
        Path file=Files.createTempFile("rn4-ascoli-split", ".json");
        try {
            TopFlopPlayerIdentities.save(file,reg);
            var restored=TopFlopPlayerIdentities.load(file);
            restored.bindAudit(audit);
            ok(restored.isDistinct(collision),"separation persists after reload");
            ok(restored.complete(proposal),"approved Ascoli group persists");
            rejected=false;
            try{restored.approve(dangerous);}catch(IllegalArgumentException ex){rejected=true;}
            ok(rejected,"loaded separation still protects Renewo links");
            restored.undo(collision);
            ok(restored.complete(proposal),"revoking separation does not erase Ascoli link");
        }finally {Files.deleteIfExists(file);}
        System.out.println("RN4_SEPARATED_IDENTITY_V40_OK Ascoli=2 seasons Crivello=separate guarded persisted historical=2");
    }
}
```
