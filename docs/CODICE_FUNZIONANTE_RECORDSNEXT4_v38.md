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


# AGGIORNAMENTO v33 - 11 novembre 2026

# RecordsNext 4.0 - Diario v33: riconciliazione GUI (11 novembre 2026)

## Base e regole immutate
- Patch incrementale sulla copia isolata `D:\DEV_APPS\RecordsNext4.0_TEST_v37` con v40 installata.
- Nessuna modifica a 3.2, FCA/FCM, SQLite, siti pubblicati, manifest/shard legacy o archivio approvazioni.
- **Nessuna approvazione automatica** delle identita calciatori. Le decisioni continuano a essere registrate soltanto su esplicita conferma dell'operatore.
- Le scelte `PERSONE DISTINTE` restano protette dal motore v40, comprese le fusioni indirette.

## Difetto v40 e correzione v41
- In v40 la selezione multipla con almeno un caso `requiresIndividualReview` veniva bloccata integralmente (`I casi incerti o gravi vanno confermati uno alla volta`).
- Nella stessa finestra era richiesto di digitare `UNISCI <codice>` prima della conferma.
- In v41 le varianti non critiche selezionate sono approvabili in un unico passo **previa anteprima e conferma SI esplicita**.
- I casi critici eventualmente compresi nella stessa selezione richiedono **ciascuno una conferma SI/NO distinta**, con nomi, stagioni, date di nascita, valutazione e fonti LCS a schermo. Annulla non apporta nessuna modifica, NO lascia sospeso soltanto quel caso.
- Non e piu richiesto digitare parole o codici. Il pulsante non puo unire un codice gia impostato `PERSONE DISTINTE`: per correggere la decisione bisogna revocarla esplicitamente.
- Le scelte approvate di una selezione vengono persistite in una sola operazione transazionale con rollback in caso di errore; non si salva una fusione parziale se un'altra e incompatibile.

## Verifiche locali
- Compilazione Java 21 di 91 sorgenti `src/main/java` ricostruiti dalla base v40.
- `TopFlopBatchReviewSmokeTest`: OK, separazione rischio, annullamento, salvataggio e ricaricamento.
- `TopFlopIdentityEvidenceSmokeTest`: OK, 569 alias correnti, 1531 storici, 2133 controlli.
- `TopFlopSeparatedIdentitySmokeTest`: OK, Nicola Ascoli 2 annate / Roberto Crivello separato.
- `TopFlopHistoricIdentitySmokeTest`: OK, media ponderata e cutoff storico.
- Colloquio Swing fisico su Windows e ricompilazione Maven da effettuare con lo script protetto nella copia TEST.

## Evidenze storiche controllate, non ancora correzioni canoniche attive
- Okaka Stefano Chuka: CodiceGazza `13032` (2006/07, 2008/09-2010/11), `73032` (2011/12-2014/15) e `100981` (2018/19-2021/22). Le connessioni fra i diversi codici devono essere APPROVATE esplicitamente nella GUI, senza alterare gli ID FCA originali. Il semplice riconoscimento delle varianti dentro ciascun codice non garantisce da solo un'unica identita storica.
- Eduardo Wilson Lino `15872`: presente in FCA 2008/09 come `LINO`, con la squadra `Lecce` in una giornata e 37 giornate `SERIE ESTERA`; nel 2011/12 come `LINO Eduardo Wilson`, associato al `Lecce`. Nessun tesseramento FCM nelle due stagioni. Gli FCA riportano nascita `01/01/1990`; data corretta fornita dall'utente `22/03/1991`. Non riscrivere FCA.
- Ragatzu (`14726`): il nome `Mauro` nel 2008/09 e errato, secondo l'utente e la data FCA 21/09/1991 corrisponde a Daniele.
- Rojas (`101381`): nome canonico dichiarato `Luis Jose Esteban Rojas Zamora`, nato `06/03/2002`.
- Cabrera (`116104`): nome canonico dichiarato `Jonni Zacarias Cabrera Mendez`, nato `14/06/1989`.
- Mbaye (`128901`): nome canonico dichiarato `Maodo Malick Mbaye`, non `Serigne`.
Queste evidenze sono registrate per la prossima funzione di approvazione dei NOMI e delle DATE canoniche. Non sono ancora modifiche ai JS, alle identita salvate o alla GUI.


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
            // A mixed multi-selection is valid: review every critical case separately,
            // while low-risk variants can be explicitly accepted as one batch.
            // A cancellation causes NO changes; a declined case remains pending.
            List<Row> chosen=selected().stream().filter(r->r.collision()!=null).toList();
            if(chosen.isEmpty()){
                JOptionPane.showMessageDialog(this,"Seleziona almeno un codice da riconciliare.");return;
            }
            for(Row r:chosen)if(registry.isDistinct(r.collision())){
                JOptionPane.showMessageDialog(this,
                    "Il codice "+r.collision().code()+" e' gia' PERSONE DISTINTE.\n"+
                    "Per cambiare questa scelta, revocala prima esplicitamente.");
                return;
            }
            List<Row> pending=chosen.stream().filter(r->!registry.isMerged(r.collision())).toList();
            if(pending.isEmpty()){
                JOptionPane.showMessageDialog(this,"Le identita' selezionate sono gia' approvate.");return;
            }
            List<Row> straightforward=pending.stream()
                .filter(r->!r.assessment().requiresIndividualReview()).toList();
            List<Row> critical=pending.stream()
                .filter(r->r.assessment().requiresIndividualReview()).toList();
            List<Row> accepted=new ArrayList<>();
            if(!straightforward.isEmpty()){
                StringBuilder names=new StringBuilder();
                for(Row r:straightforward)names.append("Codice ").append(r.collision().code())
                    .append(" - ").append(r.assessment().category()).append("\n")
                    .append(r.collision().description()).append("\n\n");
                int choice=confirmDetails(names.toString(),
                    "Approvare "+straightforward.size()+" varianti non critiche?",
                    "Ogni codice selezionato sara' unito soltanto dopo il tuo SI'.");
                if(choice==JOptionPane.CANCEL_OPTION||choice==JOptionPane.CLOSED_OPTION)return;
                if(choice==JOptionPane.YES_OPTION)accepted.addAll(straightforward);
            }
            for(Row r:critical){
                // Exactly one explicit Yes/No for each potentially ambiguous code,
                // without asking the operator to retype an arbitrary confirmation word.
                StringBuilder facts=new StringBuilder();
                for(Source s:r.collision().members())facts.append(s.season()).append(" | ")
                    .append(s.name()).append(" | nato ").append(s.birth())
                    .append(" | codice ").append(s.code()).append("\n");
                facts.append("\n").append(r.assessment().explanation()).append("\n")
                    .append(evidence.evidenceDetails(r.collision()));
                int choice=confirmDetails(facts.toString(),
                    "Verifica individuale codice "+r.collision().code(),
                    "Unire SOLO se tutte le anagrafiche sono della stessa persona.");
                if(choice==JOptionPane.CANCEL_OPTION||choice==JOptionPane.CLOSED_OPTION)return;
                if(choice==JOptionPane.YES_OPTION)accepted.add(r);
            }
            if(!accepted.isEmpty())act(()->{
                for(Row r:accepted)registry.approveSame(r.collision());
            });
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
    /** Read-only evidence preview; the action is authorized only by an explicit Yes. */
    private int confirmDetails(String facts,String title,String instruction) {
        JTextArea preview=new JTextArea(facts,Math.min(15,Math.max(5,facts.split("\n").length)),90);
        preview.setEditable(false);
        preview.setLineWrap(true);
        preview.setWrapStyleWord(true);
        JScrollPane scroll=new JScrollPane(preview);
        scroll.setPreferredSize(new Dimension(810,Math.min(300,Math.max(140,preview.getRows()*19))));
        JPanel message=new JPanel(new BorderLayout(0,8));
        message.add(new JLabel(instruction),BorderLayout.NORTH);
        message.add(scroll,BorderLayout.CENTER);
        return JOptionPane.showConfirmDialog(this,message,title,
            JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE);
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

## File aggiornato: src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopBatchReviewSmokeTest.java

```java
package it.alterlega.recordsnext.app.topflop;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Backend regression for the v41 GUI approval flow (Swing click tested on Windows). */
public final class TopFlopBatchReviewSmokeTest {
    private static void ok(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
    private static TopFlopPlayerIdentities.Source s(String season, int id, int code, String name, String birth) {
        return new TopFlopPlayerIdentities.Source(season,id,code,name,birth);
    }
    private static TopFlopPlayerIdentities.Collision collision(int code, TopFlopPlayerIdentities.Source... members) {
        return new TopFlopPlayerIdentities.Collision(String.format("%064x",code),code,List.of(members));
    }
    public static void main(String[] args) throws Exception {
        Path evidenceDir = args.length==0 ? Path.of("config/identity-evidence") : Path.of(args[0]);
        var evidence = TopFlopIdentityEvidence.load(evidenceDir);
        var iacoponi=collision(14055,
            s("2006_2007",11,14055,"IACOPINI Simone","1987-04-30"),
            s("2007_2008",12,14055,"IACOPONI Simone","1987-04-30"));
        var hamsik=collision(12830,
            s("2007_2008",23,12830,"HAMSYK Marek","1987-07-27"),
            s("2008_2009",24,12830,"HAMSIK Marek","1987-07-27"));
        var frisonZappino=collision(14085,
            s("2009_2010",35,14085,"FRISON Alberto","1988-01-22"),
            s("2015_2016",36,14085,"ZAPPINO Massimo","1981-06-12"));
        ok(!evidence.classify(iacoponi).requiresIndividualReview(),"Iacoponi may be approved by an explicit batch click");
        ok(!evidence.classify(hamsik).requiresIndividualReview(),"Hamsik alias may be approved by an explicit batch click");
        ok(evidence.classify(frisonZappino).requiresIndividualReview(),"Real collision always needs an individual confirmation");
        var registry=new TopFlopPlayerIdentities.Registry();
        ok(registry.approvedPlayers()==0,"Evidence NEVER authorizes approval");
        var snapshot=registry.snapshot();
        registry.approveSame(iacoponi);
        registry.approveSame(hamsik);
        registry.restore(snapshot);
        ok(registry.approvedPlayers()==0,"Canceled group is entirely rolled back");
        registry.approveSame(iacoponi);
        registry.approveSame(hamsik);
        registry.distinguish(frisonZappino);
        Path file=Files.createTempFile("rn4-v41-approval", ".json");
        try {
            TopFlopPlayerIdentities.save(file,registry);
            var reloaded=TopFlopPlayerIdentities.load(file);
            reloaded.bindAudit(new TopFlopPlayerIdentities.Audit(List.of(),List.of(frisonZappino),6));
            ok(reloaded.isMerged(iacoponi) && reloaded.isMerged(hamsik),"Approved links persist");
            ok(reloaded.isDistinct(frisonZappino),"Distinct persons persist");
        } finally { Files.deleteIfExists(file); }
        System.out.println("RN4_BATCH_REVIEW_V41_OK safe-batch critical-individual rollback persistence");
    }
}
```


# AGGIORNAMENTO v34 – 11 novembre 2026

# RecordsNext 4.0 – Diario v34 – 11 novembre 2026

## Requisiti consolidati
- Codice Gazzetta non univoco, dati FCA/FCM originali immutabili.
- LCS 2015/2019/2020 come evidenze e nessuna approvazione senza clic.
- Regole RecordsNext 3.2 squadre e competizioni inalterate.
- Registro decisioni persistente in `config/player-identities.json`.

## Implementazione v42
- `TopFlopPlayerIdentities.Registry.approve()` può associare con conferma esplicita gruppi già approvati con codici Gazzetta diversi.
- Verifica delle stagioni duplicate nell'intero insieme delle carriere, non solo nella proposta.
- Conserva le protezioni su persone già dichiarate distinte e la transazione atomica su errori.
- Memorizza in `mergeUndo` le associazioni precedenti delle fusioni sovrapposte per una revoca prudente; eventuali decisioni dipendenti richiedono revoca in ordine inverso.
- La finestra di conferma delle proposte mostra nomi, anni, codici, date e il numero delle carriere preesistenti da raccordare.
- L'audit distingue date giorno/mese invertiti da altri conflitti anagrafici.
- Nessuna pubblicazione, nessuna riconciliazione implicita, nessuna modifica alle identità già registrate da parte dell'installatore.

## Evidenze
L'audit del DB storico di riferimento, sui 16.262 record FCA, produce 3.165 proposte di cui 389 da verificare inizialmente. Tra queste ultime, 173 hanno semplicemente due date giorno/mese invertiti. Nella GUI Windows al 11/11/2026 le approvazioni registrate sono 2.840, le proposte sospese 353 e le collisioni ancora da decidere 0. Questa copia del registro non è presente nello ZIP del codice: resta sul PC e non viene sovrascritta.

## Test
- Compilation all main Java 21: OK.
- `TopFlopCrossCodeIdentitySmokeTest`: OK, tre codici Okaka, serializzazione, revoca, conflitto stessa stagione.
- `TopFlopSeparatedIdentitySmokeTest`: OK, protezione Ascoli/Crivello.
- `TopFlopIdentityEvidenceSmokeTest`: OK, fonti LCS e varianti.
- `TopFlopBatchReviewSmokeTest`: OK, conferme esplicite e persistenza.


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
        // Reversible cross-code mergers: fingerprint -> full pre-merge assignments.
        // A change is stored ONLY after explicit approval. No existing registry is rewritten on load.
        private final Map<String,Map<String,String>> mergeUndo = new LinkedHashMap<>();
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
            mergeUndo.forEach((k,v)->copy.mergeUndo.put(k,new LinkedHashMap<>(v)));
            copy.distinctEvidence.putAll(distinctEvidence);
            return copy;
        }
        public void restore(Registry snapshot) {
            approved.clear(); approved.putAll(snapshot.approved);
            ignored.clear(); ignored.addAll(snapshot.ignored);
            distinct.clear(); distinct.addAll(snapshot.distinct);
            mergeUndo.clear(); snapshot.mergeUndo.forEach((k,v)->mergeUndo.put(k,new LinkedHashMap<>(v)));
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
            Map<String,String> previous=mergeUndo.get(proposal.fingerprint());
            if(previous!=null) {
                // Undo a cross-code merge without destroying previously approved careers.
                String merged=approved.get(proposal.members().get(0).key());
                if(merged==null || previous.keySet().stream().anyMatch(k->!merged.equals(approved.get(k)))
                    || approved.entrySet().stream().anyMatch(e->merged.equals(e.getValue())&&!previous.containsKey(e.getKey())))
                    throw new IllegalArgumentException("Sono presenti approvazioni successive: revocarle prima della fusione fra codici.");
                Map<String,String> checkpoint=new LinkedHashMap<>(approved);
                try {
                    for(var e:previous.entrySet()) {
                        if(e.getValue()==null)approved.remove(e.getKey());
                        else approved.put(e.getKey(),e.getValue());
                    }
                    validateDistinctAssignments();
                    mergeUndo.remove(proposal.fingerprint());
                }catch(RuntimeException ex){approved.clear();approved.putAll(checkpoint);throw ex;}
            } else {
                proposal.members().forEach(s->approved.remove(s.key()));
            }
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
        /** Approval can join previously confirmed careers only after an explicit GUI action.
         *  Pre-existing groups are kept intact. Same-season overlap and prior separations
         *  reject the whole operation atomically, before any change is saved to disk. */
        public void approve(Proposal p) {
            if(p.members().size()<2)throw new IllegalArgumentException("Gruppo non storico");
            if(p.members().stream().map(Source::season).distinct().count()!=p.members().size())
                throw new IllegalArgumentException("Identita' diverse nella stessa stagione: " + p.description());
            if(complete(p))return;
            Set<String> existingIds=new LinkedHashSet<>();
            for(Source member:p.members()){
                String id=approved.get(member.key());
                if(id!=null)existingIds.add(id);
            }
            // The union includes ALL members of previously approved careers, not only
            // those explicitly listed in the new proposal.
            Map<String,String> previous=new LinkedHashMap<>();
            for(var entry:approved.entrySet())
                if(existingIds.contains(entry.getValue()))previous.put(entry.getKey(),entry.getValue());
            for(Source member:p.members())previous.putIfAbsent(member.key(),approved.get(member.key()));
            Map<String,String> seasonToKey=new HashMap<>();
            for(String key:previous.keySet()) {
                String season=key.substring(0,key.indexOf(':'));
                String existing=seasonToKey.putIfAbsent(season,key);
                if(existing!=null&&!existing.equals(key))
                    throw new IllegalArgumentException("Carriere sovrapposte nella stagione "+season+
                        ": non possono essere unite senza risolvere l'identita'.");
            }
            String chosen=existingIds.isEmpty()?
                "rn4p-"+p.members().stream().map(Source::key).sorted().findFirst().orElseThrow().replace(':','-'):
                existingIds.stream().sorted().findFirst().orElseThrow();
            Map<String,String> checkpoint=new LinkedHashMap<>(approved);
            try {
                for(String key:previous.keySet())approved.put(key,chosen);
                validateDistinctAssignments();
                if(!existingIds.isEmpty())mergeUndo.put(p.fingerprint(),new LinkedHashMap<>(previous));
            }catch(RuntimeException ex){approved.clear();approved.putAll(checkpoint);throw ex;}
            ignored.remove(p.fingerprint());
        }
        /** Read-only indicator for explicit cross-code merging confirmation. */
        public int existingCareerGroups(Proposal p) {
            return (int)p.members().stream().map(s->approved.get(s.key()))
                .filter(java.util.Objects::nonNull).distinct().count();
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
        Object history=map.get("mergeUndo");
        if(history!=null) {
            if(!(history instanceof Map<?,?> historicEntries))throw new IOException("mergeUndo non valido");
            for(var item:historicEntries.entrySet()) {
                String fingerprint=String.valueOf(item.getKey());
                if(!fingerprint.matches("[0-9a-f]{64}") || !(item.getValue() instanceof Map<?,?> details))
                    throw new IOException("Registrazione fusione non valida");
                Map<String,String> before=new LinkedHashMap<>();
                for(var e:details.entrySet()) {
                    String k=String.valueOf(e.getKey());
                    String v=e.getValue()==null?null:String.valueOf(e.getValue());
                    if(!k.matches("[0-9]{4}_[0-9]{4}:[0-9]+") ||
                        (v!=null&&!v.matches("rn4p-[0-9]{4}_[0-9]{4}-[0-9]+")))
                        throw new IOException("Riferimento storico invalido: "+k);
                    before.put(k,v);
                }
                r.mergeUndo.put(fingerprint,before);
            }
        }
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
        data.put("mergeUndo",new TreeMap<>(r.mergeUndo));
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
            Set<String> births=group.stream().map(s->dob(s.birth())).filter(x->x!=null).collect(Collectors.toSet());
            boolean invertedDates=births.size()==2 && birthSwapped(births.iterator().next(),
                births.stream().skip(1).findFirst().orElse(""));
            String reason=birthConflict?(invertedDates?"Giorno/mese di nascita invertiti":"Date di nascita discordanti"):
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
        "Anomalie codici", "Proposte da verificare", "Proposte esatte", "Tutte", "Date giorno/mese invertiti"
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
            List<Row> chosen=selected().stream().filter(r->r.proposal()!=null&&!registry.complete(r.proposal())).toList();
            if(chosen.isEmpty()){JOptionPane.showMessageDialog(this,"Nessuna proposta non approvata selezionata.");return;}
            // Always show the operator the evidence before changing any history.
            StringBuilder preview=new StringBuilder();
            int linkingPreviouslyApproved=0;
            for(Row r:chosen){
                int existing=registry.existingCareerGroups(r.proposal());
                if(existing>1)linkingPreviouslyApproved++;
                preview.append(r.proposal().reason()).append(" | carriere gia' approvate: ").append(existing).append("\n");
                for(Source source:r.proposal().members())preview.append("  ")
                    .append(source.season()).append(" | ").append(source.name())
                    .append(" | codice ").append(source.code())
                    .append(" | nascita ").append(source.birth()).append("\n");
                preview.append("\n");
            }
            String title="Approvare ESPRESSAMENTE "+chosen.size()+" proposte?";
            String note=linkingPreviouslyApproved>0
                ?"ATTENZIONE: "+linkingPreviouslyApproved+" proposte collegano carriere gia' approvate."
                :"Le proposte selezionate saranno associate solo dopo la conferma.";
            if(confirmDetails(preview.toString(),title,note)!=JOptionPane.YES_OPTION)return;
            act(()->{
                for(Row r:chosen){
                    for(Collision c:audit.collisions()) {
                        if(!registry.isDistinct(c) && r.proposal().members().stream()
                            .anyMatch(x->x.code()!=null&&x.code()==c.code()))
                            throw new IllegalArgumentException("Risolvere prima il codice ambiguo " + c.code());
                    }
                    registry.approve(r.proposal());
                }
            });
        });
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
            // A mixed multi-selection is valid: review every critical case separately,
            // while low-risk variants can be explicitly accepted as one batch.
            // A cancellation causes NO changes; a declined case remains pending.
            List<Row> chosen=selected().stream().filter(r->r.collision()!=null).toList();
            if(chosen.isEmpty()){
                JOptionPane.showMessageDialog(this,"Seleziona almeno un codice da riconciliare.");return;
            }
            for(Row r:chosen)if(registry.isDistinct(r.collision())){
                JOptionPane.showMessageDialog(this,
                    "Il codice "+r.collision().code()+" e' gia' PERSONE DISTINTE.\n"+
                    "Per cambiare questa scelta, revocala prima esplicitamente.");
                return;
            }
            List<Row> pending=chosen.stream().filter(r->!registry.isMerged(r.collision())).toList();
            if(pending.isEmpty()){
                JOptionPane.showMessageDialog(this,"Le identita' selezionate sono gia' approvate.");return;
            }
            List<Row> straightforward=pending.stream()
                .filter(r->!r.assessment().requiresIndividualReview()).toList();
            List<Row> critical=pending.stream()
                .filter(r->r.assessment().requiresIndividualReview()).toList();
            List<Row> accepted=new ArrayList<>();
            if(!straightforward.isEmpty()){
                StringBuilder names=new StringBuilder();
                for(Row r:straightforward)names.append("Codice ").append(r.collision().code())
                    .append(" - ").append(r.assessment().category()).append("\n")
                    .append(r.collision().description()).append("\n\n");
                int choice=confirmDetails(names.toString(),
                    "Approvare "+straightforward.size()+" varianti non critiche?",
                    "Ogni codice selezionato sara' unito soltanto dopo il tuo SI'.");
                if(choice==JOptionPane.CANCEL_OPTION||choice==JOptionPane.CLOSED_OPTION)return;
                if(choice==JOptionPane.YES_OPTION)accepted.addAll(straightforward);
            }
            for(Row r:critical){
                // Exactly one explicit Yes/No for each potentially ambiguous code,
                // without asking the operator to retype an arbitrary confirmation word.
                StringBuilder facts=new StringBuilder();
                for(Source s:r.collision().members())facts.append(s.season()).append(" | ")
                    .append(s.name()).append(" | nato ").append(s.birth())
                    .append(" | codice ").append(s.code()).append("\n");
                facts.append("\n").append(r.assessment().explanation()).append("\n")
                    .append(evidence.evidenceDetails(r.collision()));
                int choice=confirmDetails(facts.toString(),
                    "Verifica individuale codice "+r.collision().code(),
                    "Unire SOLO se tutte le anagrafiche sono della stessa persona.");
                if(choice==JOptionPane.CANCEL_OPTION||choice==JOptionPane.CLOSED_OPTION)return;
                if(choice==JOptionPane.YES_OPTION)accepted.add(r);
            }
            if(!accepted.isEmpty())act(()->{
                for(Row r:accepted)registry.approveSame(r.collision());
            });
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
    /** Read-only evidence preview; the action is authorized only by an explicit Yes. */
    private int confirmDetails(String facts,String title,String instruction) {
        JTextArea preview=new JTextArea(facts,Math.min(15,Math.max(5,facts.split("\n").length)),90);
        preview.setEditable(false);
        preview.setLineWrap(true);
        preview.setWrapStyleWord(true);
        JScrollPane scroll=new JScrollPane(preview);
        scroll.setPreferredSize(new Dimension(810,Math.min(300,Math.max(140,preview.getRows()*19))));
        JPanel message=new JPanel(new BorderLayout(0,8));
        message.add(new JLabel(instruction),BorderLayout.NORTH);
        message.add(scroll,BorderLayout.CENTER);
        return JOptionPane.showConfirmDialog(this,message,title,
            JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE);
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
                if(mode==4 && (r.proposal()==null || !r.proposal().reason().equals("Giorno/mese di nascita invertiti")))return false;
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

## File aggiornato: src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopCrossCodeIdentitySmokeTest.java

```java
package it.alterlega.recordsnext.app.topflop;

import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.nio.file.Path;
import java.util.stream.Collectors;

/** Explicit cross-code identity mergers: existing links survive, split guards and rollback remain enforceable. */
public final class TopFlopCrossCodeIdentitySmokeTest {
    private static void check(boolean condition,String why){ if(!condition)throw new AssertionError(why); }
    private static TopFlopPlayerIdentities.Source s(String season,int id,int code,String name){
        return new TopFlopPlayerIdentities.Source(season,id,code,name,"1989-08-09 00:00:00.0");
    }
    private static TopFlopPlayerIdentities.Proposal p(String fp,TopFlopPlayerIdentities.Source... sources){
        return new TopFlopPlayerIdentities.Proposal(fp,List.of(sources),false,"Approva manualmente");
    }
    public static void main(String[] args) throws Exception {
        var a=s("2006_2007",11,13032,"OKAKA Stefano");
        var b=s("2008_2009",22,13032,"OKAKA Stefano");
        var c=s("2011_2012",33,73032,"OKAKA Stefano");
        var d=s("2013_2014",44,73032,"OKAKA CHUKA Stefano");
        var e=s("2018_2019",55,100981,"OKAKA Stefano Chuka");
        var f=s("2019_2020",66,100981,"OKAKA Stefano Chuka");
        var registry=new TopFlopPlayerIdentities.Registry();
        var group1=p("a".repeat(64),a,b);
        var group2=p("b".repeat(64),c,d);
        var group3=p("c".repeat(64),e,f);
        registry.approve(group1);registry.approve(group2);registry.approve(group3);
        check(registry.approvedIdentities()==3,"three previously confirmed histories");
        var cross=p("d".repeat(64),b,c,e);
        check(registry.existingCareerGroups(cross)==3,"three pre-existing approvals detected");
        registry.approve(cross);
        check(registry.approvedIdentities()==1,"all 3 codes belong to one approved historical player");
        check(registry.approvedPlayers()==6,"no stage/source lost in union");
        check(registry.approved().values().stream().distinct().count()==1,"all one person");
        Path tmp=Files.createTempFile("rn4-v42-cross-code-", ".json");
        try {
            TopFlopPlayerIdentities.save(tmp,registry);
            var restored=TopFlopPlayerIdentities.load(tmp);
            check(restored.approvedIdentities()==1,"merged identity survives JSON reload");
            restored.undo(cross);
            check(restored.approvedIdentities()==3,"revoking the cross-code link restores the 3 approved careers");
            check(restored.complete(group1)&&restored.complete(group2)&&restored.complete(group3),
                "all prior explicit approvals preserved on undo");
            restored.approve(cross);
            // New approval joining a previously merged group must not break reversible history.
            var extra=s("2020_2021",77,100981,"OKAKA Stefano Chuka");
            var next=p("e".repeat(64),f,extra);
            restored.approve(next);
            boolean denied=false;
            try{restored.undo(cross);}catch(IllegalArgumentException ex){denied=true;}
            check(denied,"old merger cannot be revoked when a later decision relies on it");
            restored.undo(next);
            restored.undo(cross);
            check(restored.approvedIdentities()==3,"proper LIFO undo restores all old groups");
        } finally { Files.deleteIfExists(tmp); }
        // Same-season collision in expanded groups must stop the merge transaction.
        var x=s("2009_2010",101,1111,"PLAYER A");
        var y=s("2010_2011",102,1111,"PLAYER A");
        var z=s("2009_2010",202,2222,"PLAYER B");
        var w=s("2012_2013",203,2222,"PLAYER B");
        var conflict=new TopFlopPlayerIdentities.Registry();
        conflict.approve(p("1".repeat(64),x,y));conflict.approve(p("2".repeat(64),z,w));
        var before=conflict.approved();
        boolean refused=false;
        try{conflict.approve(p("3".repeat(64),y,w));}catch(IllegalArgumentException ex){refused=true;}
        check(refused,"same-season conflict is blocked");
        check(before.equals(conflict.approved()),"failed union has no side effects");
        System.out.println("RN4_CROSS_CODE_IDENTITY_V42_OK Okaka=3codes 6seasons undo=persisted overlapping-seasons=blocked");
    }
}
```


# AGGIORNAMENTO v35 - 11 novembre 2026

# RecordsNext 4.0 — Correzione v43 (bibbia v35)

Data: 11 novembre 2026. Destinazione esclusiva: copia `RecordsNext4.0_TEST_v37`.

## Anomalia riprodotta

Nella v42 la GUI indica `codici da decidere: 0`, ma tentando di approvare una proposta
intercodice (per esempio Okaka `13032` + `73032`) visualizza:
`Risolvere prima il codice ambiguo 13032`.
Il controllo nel pulsante "Approva proposte selezionate" considera risolto il codice
solo se marcato `PERSONE DISTINTE`; ignora `UNIONE APPROVATA`.

## Correzione

- `TopFlopPlayerIdentities.requireResolvedCollisions`: guardia unica, di sola lettura,
  identica alla regola del contatore: `isDistinct(collision) || isMerged(collision)`.
- La GUI invoca la guardia prima dell'approvazione della proposta. Un codice ancora
  ambiguo resta bloccante; un codice gia' approvato non viene esaminato di nuovo.
- Non si cambia la logica della persistente `config/player-identities.json`, ne'
  le scelte gia' registrate; non si modificano FCA/FCM, 3.2 o siti.
- `TopFlopResolvedCollisionGuardV43SmokeTest` verifica caso Okaka, caricamento dopo
  salvataggio e un codice realmente non risolto che resta bloccante.
- I vincoli gia' approvati fra persone distinte, sovrapposizioni di stagione e
  rollback restano quelli della v42.

## Collaudo su Windows

Preflight con SHA-256 e backup, compilazione Maven Java 21, test v43 e regressioni
v42/v41/v40/v39. Non e' previsto alcun aggiornamento automatico delle identita'.
Al primo avvio, filtrare `Proposte da verificare` per `okaka`, selezionare la
riga `Codice storico riutilizzato` e premere `Approva proposte selezionate`.
La conferma mostrera' le occorrenze; nulla viene applicato senza il SI' dell'utente.


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
        // Reversible cross-code mergers: fingerprint -> full pre-merge assignments.
        // A change is stored ONLY after explicit approval. No existing registry is rewritten on load.
        private final Map<String,Map<String,String>> mergeUndo = new LinkedHashMap<>();
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
            mergeUndo.forEach((k,v)->copy.mergeUndo.put(k,new LinkedHashMap<>(v)));
            copy.distinctEvidence.putAll(distinctEvidence);
            return copy;
        }
        public void restore(Registry snapshot) {
            approved.clear(); approved.putAll(snapshot.approved);
            ignored.clear(); ignored.addAll(snapshot.ignored);
            distinct.clear(); distinct.addAll(snapshot.distinct);
            mergeUndo.clear(); snapshot.mergeUndo.forEach((k,v)->mergeUndo.put(k,new LinkedHashMap<>(v)));
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
            Map<String,String> previous=mergeUndo.get(proposal.fingerprint());
            if(previous!=null) {
                // Undo a cross-code merge without destroying previously approved careers.
                String merged=approved.get(proposal.members().get(0).key());
                if(merged==null || previous.keySet().stream().anyMatch(k->!merged.equals(approved.get(k)))
                    || approved.entrySet().stream().anyMatch(e->merged.equals(e.getValue())&&!previous.containsKey(e.getKey())))
                    throw new IllegalArgumentException("Sono presenti approvazioni successive: revocarle prima della fusione fra codici.");
                Map<String,String> checkpoint=new LinkedHashMap<>(approved);
                try {
                    for(var e:previous.entrySet()) {
                        if(e.getValue()==null)approved.remove(e.getKey());
                        else approved.put(e.getKey(),e.getValue());
                    }
                    validateDistinctAssignments();
                    mergeUndo.remove(proposal.fingerprint());
                }catch(RuntimeException ex){approved.clear();approved.putAll(checkpoint);throw ex;}
            } else {
                proposal.members().forEach(s->approved.remove(s.key()));
            }
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
        /** Approval can join previously confirmed careers only after an explicit GUI action.
         *  Pre-existing groups are kept intact. Same-season overlap and prior separations
         *  reject the whole operation atomically, before any change is saved to disk. */
        public void approve(Proposal p) {
            if(p.members().size()<2)throw new IllegalArgumentException("Gruppo non storico");
            if(p.members().stream().map(Source::season).distinct().count()!=p.members().size())
                throw new IllegalArgumentException("Identita' diverse nella stessa stagione: " + p.description());
            if(complete(p))return;
            Set<String> existingIds=new LinkedHashSet<>();
            for(Source member:p.members()){
                String id=approved.get(member.key());
                if(id!=null)existingIds.add(id);
            }
            // The union includes ALL members of previously approved careers, not only
            // those explicitly listed in the new proposal.
            Map<String,String> previous=new LinkedHashMap<>();
            for(var entry:approved.entrySet())
                if(existingIds.contains(entry.getValue()))previous.put(entry.getKey(),entry.getValue());
            for(Source member:p.members())previous.putIfAbsent(member.key(),approved.get(member.key()));
            Map<String,String> seasonToKey=new HashMap<>();
            for(String key:previous.keySet()) {
                String season=key.substring(0,key.indexOf(':'));
                String existing=seasonToKey.putIfAbsent(season,key);
                if(existing!=null&&!existing.equals(key))
                    throw new IllegalArgumentException("Carriere sovrapposte nella stagione "+season+
                        ": non possono essere unite senza risolvere l'identita'.");
            }
            String chosen=existingIds.isEmpty()?
                "rn4p-"+p.members().stream().map(Source::key).sorted().findFirst().orElseThrow().replace(':','-'):
                existingIds.stream().sorted().findFirst().orElseThrow();
            Map<String,String> checkpoint=new LinkedHashMap<>(approved);
            try {
                for(String key:previous.keySet())approved.put(key,chosen);
                validateDistinctAssignments();
                if(!existingIds.isEmpty())mergeUndo.put(p.fingerprint(),new LinkedHashMap<>(previous));
            }catch(RuntimeException ex){approved.clear();approved.putAll(checkpoint);throw ex;}
            ignored.remove(p.fingerprint());
        }
        /** Read-only indicator for explicit cross-code merging confirmation. */
        public int existingCareerGroups(Proposal p) {
            return (int)p.members().stream().map(s->approved.get(s.key()))
                .filter(java.util.Objects::nonNull).distinct().count();
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

    /** A code-level collision is resolved by EITHER an approved merger or an
     *  explicit separation. The GUI counter and proposal approval must agree.
     *  This check never changes identity assignments. */
    public static void requireResolvedCollisions(Registry registry, Audit audit, Proposal proposal) {
        for(Collision collision:audit.collisions()) {
            boolean involved=proposal.members().stream().anyMatch(member ->
                member.code()!=null && member.code().intValue()==collision.code());
            if(involved && !registry.isDistinct(collision) && !registry.isMerged(collision))
                throw new IllegalArgumentException("Risolvere prima il codice ambiguo " + collision.code());
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
        Object history=map.get("mergeUndo");
        if(history!=null) {
            if(!(history instanceof Map<?,?> historicEntries))throw new IOException("mergeUndo non valido");
            for(var item:historicEntries.entrySet()) {
                String fingerprint=String.valueOf(item.getKey());
                if(!fingerprint.matches("[0-9a-f]{64}") || !(item.getValue() instanceof Map<?,?> details))
                    throw new IOException("Registrazione fusione non valida");
                Map<String,String> before=new LinkedHashMap<>();
                for(var e:details.entrySet()) {
                    String k=String.valueOf(e.getKey());
                    String v=e.getValue()==null?null:String.valueOf(e.getValue());
                    if(!k.matches("[0-9]{4}_[0-9]{4}:[0-9]+") ||
                        (v!=null&&!v.matches("rn4p-[0-9]{4}_[0-9]{4}-[0-9]+")))
                        throw new IOException("Riferimento storico invalido: "+k);
                    before.put(k,v);
                }
                r.mergeUndo.put(fingerprint,before);
            }
        }
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
        data.put("mergeUndo",new TreeMap<>(r.mergeUndo));
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
            Set<String> births=group.stream().map(s->dob(s.birth())).filter(x->x!=null).collect(Collectors.toSet());
            boolean invertedDates=births.size()==2 && birthSwapped(births.iterator().next(),
                births.stream().skip(1).findFirst().orElse(""));
            String reason=birthConflict?(invertedDates?"Giorno/mese di nascita invertiti":"Date di nascita discordanti"):
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
        "Anomalie codici", "Proposte da verificare", "Proposte esatte", "Tutte", "Date giorno/mese invertiti"
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
            List<Row> chosen=selected().stream().filter(r->r.proposal()!=null&&!registry.complete(r.proposal())).toList();
            if(chosen.isEmpty()){JOptionPane.showMessageDialog(this,"Nessuna proposta non approvata selezionata.");return;}
            // Always show the operator the evidence before changing any history.
            StringBuilder preview=new StringBuilder();
            int linkingPreviouslyApproved=0;
            for(Row r:chosen){
                int existing=registry.existingCareerGroups(r.proposal());
                if(existing>1)linkingPreviouslyApproved++;
                preview.append(r.proposal().reason()).append(" | carriere gia' approvate: ").append(existing).append("\n");
                for(Source source:r.proposal().members())preview.append("  ")
                    .append(source.season()).append(" | ").append(source.name())
                    .append(" | codice ").append(source.code())
                    .append(" | nascita ").append(source.birth()).append("\n");
                preview.append("\n");
            }
            String title="Approvare ESPRESSAMENTE "+chosen.size()+" proposte?";
            String note=linkingPreviouslyApproved>0
                ?"ATTENZIONE: "+linkingPreviouslyApproved+" proposte collegano carriere gia' approvate."
                :"Le proposte selezionate saranno associate solo dopo la conferma.";
            if(confirmDetails(preview.toString(),title,note)!=JOptionPane.YES_OPTION)return;
            act(()->{
                for(Row r:chosen){
                    TopFlopPlayerIdentities.requireResolvedCollisions(registry,audit,r.proposal());
                    registry.approve(r.proposal());
                }
            });
        });
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
            // A mixed multi-selection is valid: review every critical case separately,
            // while low-risk variants can be explicitly accepted as one batch.
            // A cancellation causes NO changes; a declined case remains pending.
            List<Row> chosen=selected().stream().filter(r->r.collision()!=null).toList();
            if(chosen.isEmpty()){
                JOptionPane.showMessageDialog(this,"Seleziona almeno un codice da riconciliare.");return;
            }
            for(Row r:chosen)if(registry.isDistinct(r.collision())){
                JOptionPane.showMessageDialog(this,
                    "Il codice "+r.collision().code()+" e' gia' PERSONE DISTINTE.\n"+
                    "Per cambiare questa scelta, revocala prima esplicitamente.");
                return;
            }
            List<Row> pending=chosen.stream().filter(r->!registry.isMerged(r.collision())).toList();
            if(pending.isEmpty()){
                JOptionPane.showMessageDialog(this,"Le identita' selezionate sono gia' approvate.");return;
            }
            List<Row> straightforward=pending.stream()
                .filter(r->!r.assessment().requiresIndividualReview()).toList();
            List<Row> critical=pending.stream()
                .filter(r->r.assessment().requiresIndividualReview()).toList();
            List<Row> accepted=new ArrayList<>();
            if(!straightforward.isEmpty()){
                StringBuilder names=new StringBuilder();
                for(Row r:straightforward)names.append("Codice ").append(r.collision().code())
                    .append(" - ").append(r.assessment().category()).append("\n")
                    .append(r.collision().description()).append("\n\n");
                int choice=confirmDetails(names.toString(),
                    "Approvare "+straightforward.size()+" varianti non critiche?",
                    "Ogni codice selezionato sara' unito soltanto dopo il tuo SI'.");
                if(choice==JOptionPane.CANCEL_OPTION||choice==JOptionPane.CLOSED_OPTION)return;
                if(choice==JOptionPane.YES_OPTION)accepted.addAll(straightforward);
            }
            for(Row r:critical){
                // Exactly one explicit Yes/No for each potentially ambiguous code,
                // without asking the operator to retype an arbitrary confirmation word.
                StringBuilder facts=new StringBuilder();
                for(Source s:r.collision().members())facts.append(s.season()).append(" | ")
                    .append(s.name()).append(" | nato ").append(s.birth())
                    .append(" | codice ").append(s.code()).append("\n");
                facts.append("\n").append(r.assessment().explanation()).append("\n")
                    .append(evidence.evidenceDetails(r.collision()));
                int choice=confirmDetails(facts.toString(),
                    "Verifica individuale codice "+r.collision().code(),
                    "Unire SOLO se tutte le anagrafiche sono della stessa persona.");
                if(choice==JOptionPane.CANCEL_OPTION||choice==JOptionPane.CLOSED_OPTION)return;
                if(choice==JOptionPane.YES_OPTION)accepted.add(r);
            }
            if(!accepted.isEmpty())act(()->{
                for(Row r:accepted)registry.approveSame(r.collision());
            });
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
    /** Read-only evidence preview; the action is authorized only by an explicit Yes. */
    private int confirmDetails(String facts,String title,String instruction) {
        JTextArea preview=new JTextArea(facts,Math.min(15,Math.max(5,facts.split("\n").length)),90);
        preview.setEditable(false);
        preview.setLineWrap(true);
        preview.setWrapStyleWord(true);
        JScrollPane scroll=new JScrollPane(preview);
        scroll.setPreferredSize(new Dimension(810,Math.min(300,Math.max(140,preview.getRows()*19))));
        JPanel message=new JPanel(new BorderLayout(0,8));
        message.add(new JLabel(instruction),BorderLayout.NORTH);
        message.add(scroll,BorderLayout.CENTER);
        return JOptionPane.showConfirmDialog(this,message,title,
            JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE);
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
                if(mode==4 && (r.proposal()==null || !r.proposal().reason().equals("Giorno/mese di nascita invertiti")))return false;
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

## File aggiornato: src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopResolvedCollisionGuardV43SmokeTest.java

```java
package it.alterlega.recordsnext.app.topflop;

import java.util.List;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;

/** Regression: resolved code collision must not block a cross-code proposal. */
public final class TopFlopResolvedCollisionGuardV43SmokeTest {
    private static void check(boolean ok,String message) {
        if(!ok)throw new AssertionError(message);
    }
    private static TopFlopPlayerIdentities.Source source(String year,int id,int code,String name) {
        return new TopFlopPlayerIdentities.Source(year,id,code,name,"1989-08-09 00:00:00.0");
    }
    private static TopFlopPlayerIdentities.Proposal proposal(String fingerprint,TopFlopPlayerIdentities.Source... sources) {
        return new TopFlopPlayerIdentities.Proposal(fingerprint,List.of(sources),false,"Cross-code manual proposal");
    }
    private static void mustBlock(TopFlopPlayerIdentities.Registry registry,
           TopFlopPlayerIdentities.Audit audit,TopFlopPlayerIdentities.Proposal p) {
        boolean blocked=false;
        try {TopFlopPlayerIdentities.requireResolvedCollisions(registry,audit,p);}
        catch(IllegalArgumentException ex){
            blocked=ex.getMessage().contains("13032");
        }
        check(blocked,"unresolved code 13032 must block");
    }
    public static void main(String[] args) throws Exception {
        var a=source("2009_2010",111,13032,"OKAKA Stefano");
        var b=source("2010_2011",222,13032,"OKAKA CHUKA Stefano");
        var c=source("2013_2014",333,73032,"OKAKA CHUKA Stefano");
        var d=source("2014_2015",444,73032,"OKAKA CHUKA Stefano");
        var collision=new TopFlopPlayerIdentities.Collision("a".repeat(64),13032,List.of(a,b));
        var cross=proposal("b".repeat(64),b,c,d);
        var audit=new TopFlopPlayerIdentities.Audit(List.of(cross),List.of(collision),4);

        var registry=new TopFlopPlayerIdentities.Registry();
        mustBlock(registry,audit,cross);
        check(registry.approvedPlayers()==0,"guard is read only");

        // Real-world situation: code 13032 was approved BEFORE proposing 13032 + 73032.
        registry.approve(proposal("c".repeat(64),a,b));
        check(registry.isMerged(collision),"code 13032 resolved by approved union");
        TopFlopPlayerIdentities.requireResolvedCollisions(registry,audit,cross);
        registry.approve(cross);
        check(registry.complete(cross),"cross-code proposal can now be approved");
        check(registry.approvedPlayers()==4,"four season-local rows retained");
        check(registry.approvedIdentities()==1,"one historical player");
        Path temp=Files.createTempFile("rn4-v43-okaka-", ".json");
        try {
            TopFlopPlayerIdentities.save(temp,registry);
            var restored=TopFlopPlayerIdentities.load(temp);
            restored.bindAudit(audit);
            check(restored.complete(cross),"approved union persists after reload");
            check(restored.isMerged(collision),"resolved collision persists after reload");
            TopFlopPlayerIdentities.requireResolvedCollisions(restored,audit,cross);
        } finally {Files.deleteIfExists(temp);}

        // Explicitly separated codes also count as resolved, but cannot bypass
        // the strict cluster check when merging people explicitly marked distinct.
        var sep=new TopFlopPlayerIdentities.Registry();
        sep.distinguish(collision);
        check(sep.isDistinct(collision),"explicit separation recorded");
        TopFlopPlayerIdentities.requireResolvedCollisions(sep,audit,cross);

        // A second, still unresolved collision MUST block a proposal touching it.
        var other=new TopFlopPlayerIdentities.Collision("d".repeat(64),73032,List.of(c,d));
        var mixed=new TopFlopPlayerIdentities.Audit(List.of(cross),List.of(collision,other),4);
        var pending=new TopFlopPlayerIdentities.Registry();
        pending.approve(proposal("e".repeat(64),a,b));
        boolean blocked=false;
        try{TopFlopPlayerIdentities.requireResolvedCollisions(pending,mixed,cross);}
        catch(IllegalArgumentException ex){blocked=ex.getMessage().contains("73032");}
        check(blocked,"unresolved second collision must still block");
        System.out.println("RN4_RESOLVED_COLLISION_GUARD_V43_OK Okaka 13032->73032, persistence, unresolved blocked");
    }
}
```


# AGGIORNAMENTO v37 (patch 4.0 v45)

# RecordsNext 4.0 — Diario v37 (patch di sviluppo v45)

Data: 11 novembre 2026. Destinazione: esclusivamente `RecordsNext4.0_TEST_v37` derivata dalla v43.

## Implementato nel programma Java

- `TopFlopViewsPublication.attachScoped`: genera la vista storica solo per le stagioni autorizzate dal sito, usando `Careers` e `Teams` del medesimo staging, senza pubblicare direttamente.
- `TopFlopHistoricalViews`: aggregazione reale basata esclusivamente sulle identita approvate e voti validi ponderati; aggiunge `fantasquadrePerStagione` con fantasquadra, competizione, stagione, gare conteggiate, media voto apportata, fantamedia apportata, panchina e fuori lista osservati.
- Nessuna somma universale di fantapunti o utilizzi fantasy tra squadre e competizioni: il rendimento rimane attribuito al singolo contesto.
- Dove presente nel formato piu recente, riporta i prezzi registrati FCA/FCM e le statistiche fuori campo osservate. Non deduce possesso continuativo ne punti recuperabili.
- I prezzi storici non sono normalizzati tra budget di stagioni diverse: metadato esplicito.
- `fantamediaReale` resta `null` finche non esiste un calcolo indipendente, verificato dalle regole di lega; NON si sostituisce con medie di apporto fantasy.
- ID Gazzetta e nomi non sono usati per fusioni automatiche. Il codice di approvazione resta quello installato nella v43; non si modifica `config/player-identities.json`.
- Limite per JS fisico: 1.500.000 byte; normalizzazione, SHA, rollback e pubblicazione rimangono governati dalla pipeline 3.2.

## Verifiche eseguite sul codice con Java 21

- Compilazione sorgenti e test smoke, compresi casi Ascoli/Crivello e Okaka cross-code, superata.
- Sorgenti JS reali del 6 novembre: 21 stagioni, 16.262 `Careers`, 45.787 `Teams`.
- Higuaín di test: 8 stagioni, 217 voti reali validi senza duplicati, 69 contesti fantasia distinti.
- Cutoff storico di due stagioni: nessuna informazione delle stagioni future.
- Integrazione reale `attachScoped`: shard `History` con rendimenti per fantasquadra e validazione limite byte.

## Non ancora concluso

- Registrazione effettiva della test-copy Windows e conferma di tutti i JS generati sulla mappatura locale dell'utente.
- Calcolo della fantamedia reale indipendente dalle fantasquadre, recupero foto e indice economico stagionale configurabile.
- Le 351 proposte ancora sospese restano soggette alle decisioni esplicite dell'utente, non vengono fuse.
- La vecchia v3/v29 e la patch v44 mai installata non sono base di questa modifica.

## Installazione e ripristino

Solo `TEST_v37` con preflight SHA-256, backup sorgenti e JAR, compilazione Maven, smoke test e ripristino in caso di errore. No pubblicazione siti.


## File aggiornato: src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopHistoricalViews.java

```java
package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.config.MiniJson;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Historical player facts for explicitly approved identities and a 3.2 site cutoff.
 * Real-life votes/events are counted once per FCA player and Serie A day (as
 * already consolidated in Careers). Fantasy contributions are NEVER summed
 * across teams/competitions into a fictitious player total; every row retains
 * its original fantasy team, competition, and season.
 */
public final class TopFlopHistoricalViews {
    private TopFlopHistoricalViews() {}
    private static final MathContext MC=MathContext.DECIMAL64;
    private static final String SHARD_PREFIX="a.push(...[";
    private static final List<String> EVENTS=List.of("GOLFATTI1","GOLFATTISURIGORE1","ASSIST",
        "GOLSUBITI","GOLSUBITISURIGORE","RIGPAR","RIGSBA","AUTOGOL1","AMM","ESP");

    static final class Career {
        final String identityId;
        final List<Map<String,Object>> seasons=new ArrayList<>();
        final List<Map<String,Object>> fantasyContexts=new ArrayList<>();
        final Set<String> seenContexts=new HashSet<>();
        BigDecimal votes=BigDecimal.ZERO;
        int valid=0;
        int costObservations=0;
        BigDecimal costSum=BigDecimal.ZERO,costMin=null,costMax=null;
        final Map<String,BigDecimal> events=new LinkedHashMap<>();
        Career(String id){identityId=id;}
        void add(Map<String,Object> row) {
            Map<String,Object> real=asObject(row.get("reale"));
            int validSeason=asInt(real.get("votiValidi"));
            BigDecimal sum=asDecimal(real.get("sommaVoti"));
            if(validSeason<0 || sum.signum()<0)throw new IllegalArgumentException("Voti reali invalidi");
            valid+=validSeason;votes=votes.add(sum);
            Map<String,Object> srcEvents=asObject(real.get("eventi"));
            for(var e:srcEvents.entrySet())events.merge(e.getKey(),asDecimal(e.getValue()),BigDecimal::add);
            String key=String.valueOf(row.get("playerKey"));
            String season=seasonOf(key);
            if(row.get("stagione")!=null && !season.equals(String.valueOf(row.get("stagione"))))
                throw new IllegalArgumentException("Stagione incoerente: " + key);
            Map<String,Object> seasonRow=new LinkedHashMap<>();
            seasonRow.put("stagione",season);seasonRow.put("playerKey",key);
            seasonRow.put("nome",String.valueOf(row.get("nome")));
            seasonRow.put("votiValidi",validSeason);seasonRow.put("sommaVoti",sum);
            seasonRow.put("mediaVoto",validSeason==0?null:sum.divide(BigDecimal.valueOf(validSeason),MC));
            seasonRow.put("eventi",srcEvents);
            if(row.get("costoAcquisto") instanceof Map<?,?> cost) {
                Map<String,Object> values=asObject(cost);
                seasonRow.put("costoAcquisto",values);
                int n=asInt(values.get("registrazioniConCosto"));
                if(n>0) {
                    BigDecimal costAverage=asDecimal(values.get("medio"));
                    BigDecimal minimum=asDecimal(values.get("minimo")),maximum=asDecimal(values.get("massimo"));
                    costObservations+=n;
                    costSum=costSum.add(costAverage.multiply(BigDecimal.valueOf(n)));
                    if(costMin==null||costMin.compareTo(minimum)>0)costMin=minimum;
                    if(costMax==null||costMax.compareTo(maximum)<0)costMax=maximum;
                }
            }
            seasons.add(seasonRow);
        }
        void addTeam(Map<String,Object> row) {
            String playerKey=String.valueOf(row.get("playerKey"));
            String season=seasonOf(playerKey);
            int team=asInt(row.get("idSquadraInterno"));
            int competition=asInt(row.get("idCompetizioneInterno"));
            String unique=playerKey+":"+team+":"+competition;
            if(!seenContexts.add(unique))throw new IllegalArgumentException("Contesto fantasy duplicato: "+unique);
            int counted=asInt(row.get("gareConteggiate"));
            int realValid=asInt(row.get("gareConVotoReale"));
            BigDecimal realVotes=asDecimal(row.get("sommaVotiRealiApportati"));
            BigDecimal fantasyVotes=asDecimal(row.get("sommaFantavotiApportati"));
            if(counted<0||realValid<0||realValid>counted)
                throw new IllegalArgumentException("Conteggi fantasy incoerenti: "+unique);
            Map<String,Object> context=new LinkedHashMap<>();
            context.put("stagione",season);
            context.put("playerKey",playerKey);
            context.put("idSquadraInterno",team);
            context.put("fantasquadra",row.get("fantasquadra"));
            context.put("idCompetizioneInterno",competition);
            context.put("competizione",row.get("competizione"));
            context.put("famiglia",row.get("famiglia"));
            context.put("gareConteggiate",counted);
            context.put("gareConVotoReale",realValid);
            context.put("mediaVotoApportata",realValid==0?null:realVotes.divide(BigDecimal.valueOf(realValid),MC));
            context.put("fantamediaApportata",counted==0?null:fantasyVotes.divide(BigDecimal.valueOf(counted),MC));
            context.put("sommaVotiRealiApportati",realVotes);
            context.put("sommaFantavotiApportati",fantasyVotes);
            context.put("panchinaNonConteggiata",asInt(row.get("panchinaNonConteggiata")));
            context.put("fuoriLista",asInt(row.get("fuoriLista")));
            if(row.containsKey("nonUtilizzatoOsservato"))
                context.put("nonUtilizzatoOsservato",row.get("nonUtilizzatoOsservato"));
            if(row.containsKey("costoAcquisto"))
                context.put("costoAcquisto",row.get("costoAcquisto"));
            fantasyContexts.add(context);
        }
        Map<String,Object> view() {
            Map<String,Object> result=new LinkedHashMap<>();
            result.put("playerIdentityId",identityId);
            result.put("identitaStoricaVerificata",true);
            result.put("stagioni",seasons);
            result.put("numeroStagioni",seasons.size());
            result.put("votiRealiValidi",valid);
            result.put("mediaVotoReale",valid==0?null:votes.divide(BigDecimal.valueOf(valid),MC));
            result.put("eventiReali",events);
            result.put("gol",events.getOrDefault("GOLFATTI1",BigDecimal.ZERO));
            result.put("assist",events.getOrDefault("ASSIST",BigDecimal.ZERO));
            result.put("ammonizioni",events.getOrDefault("AMM",BigDecimal.ZERO));
            result.put("espulsioni",events.getOrDefault("ESP",BigDecimal.ZERO));
            result.put("autogol",events.getOrDefault("AUTOGOL1",BigDecimal.ZERO));
            result.put("rigoriSbagliati",events.getOrDefault("RIGSBA",BigDecimal.ZERO));
            result.put("golSubiti",events.getOrDefault("GOLSUBITI",BigDecimal.ZERO));
            result.put("rigoriParati",events.getOrDefault("RIGPAR",BigDecimal.ZERO));
            result.put("fantamediaReale",null); // Never invent a score from FCM team contributions.
            Map<String,Object> costs=new LinkedHashMap<>();
            costs.put("registrazioniConCosto",costObservations);
            costs.put("minimo",costMin);costs.put("massimo",costMax);
            costs.put("medio",costObservations==0?null:costSum.divide(BigDecimal.valueOf(costObservations),MC));
            costs.put("confrontabilita","PREZZI_REGISTRATI_NON_NORMALIZZATI_PER_BUDGET_STAGIONALE");
            result.put("costoAcquistoStorico",costs);
            result.put("fantasquadrePerStagione",fantasyContexts);
            result.put("contestiFantasyDistinti",fantasyContexts.size());
            result.put("aggregazioneFantasy","PER_FANTASQUADRA_COMPETIZIONE_STAGIONE_NO_TOTALE_GLOBALE");
            return result;
        }
    }
    /** Old entrypoint retained for 3.2/4.0 tests and consumers. */
    public static List<Map<String,Object>> aggregate(List<Map<String,Object>> seasonCareers,
                                                       Map<String,String> approved) {
        return aggregate(seasonCareers,List.of(),approved);
    }
    public static List<Map<String,Object>> aggregate(List<Map<String,Object>> seasonCareers,
                                                       List<Map<String,Object>> teams,
                                                       Map<String,String> approved) {
        Objects.requireNonNull(seasonCareers);Objects.requireNonNull(teams);Objects.requireNonNull(approved);
        Map<String,Career> byIdentity=new LinkedHashMap<>();
        Map<String,String> seenKeys=new LinkedHashMap<>();
        for(Map<String,Object> row:seasonCareers) {
            String key=String.valueOf(row.get("playerKey"));
            String identity=approved.get(key);
            if(identity==null)continue; // Never guess identity by raw code or name.
            if(seenKeys.putIfAbsent(key,identity)!=null)
                throw new IllegalArgumentException("Doppia anagrafica nello stesso cutoff: " + key);
            byIdentity.computeIfAbsent(identity,Career::new).add(row);
        }
        for(Map<String,Object> row:teams) {
            String key=String.valueOf(row.get("playerKey"));
            String identity=approved.get(key);
            if(identity==null)continue;
            Career career=byIdentity.get(identity);
            if(career==null || !seenKeys.containsKey(key))
                throw new IllegalArgumentException("Contesto fantasy senza carriera FCA nello scope: "+key);
            career.addTeam(row);
        }
        List<Map<String,Object>> out=new ArrayList<>();
        for(Career c:byIdentity.values()) {
            c.seasons.sort(Comparator.comparing(x->String.valueOf(x.get("stagione"))));
            if(c.seasons.stream().map(s->s.get("stagione")).distinct().count()!=c.seasons.size())
                throw new IllegalArgumentException("Identita' approvata compare due volte nella stessa stagione: " + c.identityId);
            c.fantasyContexts.sort(Comparator.comparing((Map<String,Object> x)->String.valueOf(x.get("stagione")))
                .thenComparing(x->String.valueOf(x.get("fantasquadra")))
                .thenComparing(x->String.valueOf(x.get("competizione"))));
            if(c.seasons.size()>1)out.add(c.view());
        }
        out.sort(Comparator.comparing(x->String.valueOf(x.get("playerIdentityId"))));
        return List.copyOf(out);
    }

    /** Parse the same audited, compact JS staged by the Java 4.0 exporter. */
    public static List<Map<String,Object>> readCareers(Path directory,List<Path> files) throws IOException {
        List<Map<String,Object>> out=new ArrayList<>();
        for(Path source:files) {
            if(!source.getFileName().toString().contains(".Careers."))continue;
            for(Object raw:readRows(directory,source))out.add(decodeCareer(raw));
        }
        return out;
    }

    /** Per-season dictionaries are from the scoped 3.2 facade, NEVER guessed by name. */
    public static List<Map<String,Object>> readTeams(Path directory,List<Path> files,
                                                      List<Map<String,Object>> seasonMetadata) throws IOException {
        Map<String,Map<Integer,String>> teamNames=new LinkedHashMap<>(),competitionNames=new LinkedHashMap<>(),
            competitionFamilies=new LinkedHashMap<>();
        for(Map<String,Object> season:seasonMetadata) {
            String id=String.valueOf(season.get("stagione"));
            Map<String,Object> dict=asObject(season.get("dizionari"));
            Map<Integer,String> teams=new LinkedHashMap<>(),competitions=new LinkedHashMap<>(),families=new LinkedHashMap<>();
            for(Object raw:asList(dict.get("squadre"))) {
                List<?> row=asList(raw);
                if(row.size()!=2)throw new IOException("Dizionario squadre non valido: "+id);
                teams.put(asInt(row.get(0)),String.valueOf(row.get(1)));
            }
            for(Object raw:asList(dict.get("competizioni"))) {
                List<?> row=asList(raw);
                if(row.size()!=3)throw new IOException("Dizionario competizioni non valido: "+id);
                competitions.put(asInt(row.get(0)),String.valueOf(row.get(1)));
                families.put(asInt(row.get(0)),String.valueOf(row.get(2)));
            }
            if(teamNames.putIfAbsent(id,teams)!=null)throw new IOException("Stagione duplicata nei dizionari: "+id);
            competitionNames.put(id,competitions);competitionFamilies.put(id,families);
        }
        List<Map<String,Object>> out=new ArrayList<>();
        for(Path source:files) {
            if(!source.getFileName().toString().contains(".Teams."))continue;
            for(Object raw:readRows(directory,source)) {
                if(!(raw instanceof List<?> row)|| (row.size()!=4 && row.size()!=5))
                    throw new IOException("Riga Teams non valida in "+source);
                String key=String.valueOf(row.get(0)),season=seasonOf(key);
                int team=asInt(row.get(1)),competition=asInt(row.get(2));
                String teamName=teamNames.getOrDefault(season,Map.of()).get(team);
                String compName=competitionNames.getOrDefault(season,Map.of()).get(competition);
                String family=competitionFamilies.getOrDefault(season,Map.of()).get(competition);
                if(teamName==null||compName==null||family==null)
                    throw new IOException("ID squadra/competizione non risolti: "+key+"/"+team+"/"+competition);
                List<?> usage=asList(row.get(3));
                if(usage.size()<9 || usage.size()>10)
                    throw new IOException("Riga apporto Teams non valida: "+key);
                Map<String,Object> decoded=new LinkedHashMap<>();
                decoded.put("playerKey",key);
                decoded.put("idSquadraInterno",team);decoded.put("idCompetizioneInterno",competition);
                decoded.put("fantasquadra",teamName);decoded.put("competizione",compName);decoded.put("famiglia",family);
                decoded.put("gareConteggiate",usage.get(1));decoded.put("gareConVotoReale",usage.get(2));
                decoded.put("sommaVotiRealiApportati",usage.get(3));
                decoded.put("sommaFantavotiApportati",usage.get(4));
                decoded.put("panchinaNonConteggiata",usage.get(5));decoded.put("fuoriLista",usage.get(6));
                if(usage.size()==10)decoded.put("nonUtilizzatoOsservato",decodeUnused(usage.get(9)));
                if(row.size()==5)decoded.put("costoAcquisto",decodeCost(row.get(4)));
                out.add(decoded);
            }
        }
        return out;
    }
    private static Map<String,Object> decodeCost(Object raw) throws IOException {
        List<?> r=asList(raw);
        if(r.size()!=4)throw new IOException("Costo FCM non valido");
        int n=asInt(r.get(0));
        if(n<0)throw new IOException("Conteggio costi negativo");
        Map<String,Object> result=new LinkedHashMap<>();
        result.put("registrazioniConCosto",n);
        result.put("minimo",n==0?null:asDecimal(r.get(1)));
        result.put("massimo",n==0?null:asDecimal(r.get(2)));
        result.put("medio",n==0?null:asDecimal(r.get(3)));
        return result;
    }
    private static Map<String,Object> decodeUnused(Object raw) throws IOException {
        List<?> u=asList(raw);
        if(u.size()!=5)throw new IOException("Dettaglio fuori campo non valido");
        int realValid=asInt(u.get(0)),fantasyValid=asInt(u.get(2));
        BigDecimal votes=asDecimal(u.get(1)),fantasy=asDecimal(u.get(3));
        return Map.of("gareConVotoReale",realValid,"sommaVotiReali",votes,
            "mediaVotoReale",realValid==0?"":votes.divide(BigDecimal.valueOf(realValid),MC),
            "gareConFantavotoNumerico",fantasyValid,"sommaFantavotiTabellino",fantasy,
            "fantamediaTabellino",fantasyValid==0?"":fantasy.divide(BigDecimal.valueOf(fantasyValid),MC),
            "eventiRealiNonApportati",decodeEvents(asList(u.get(4))));
    }
    private static List<?> readRows(Path directory,Path source) throws IOException {
        if(!source.normalize().getParent().equals(directory.normalize()))throw new IOException("Shard fuori dalla directory");
        String js=Files.readString(source,StandardCharsets.UTF_8);
        int begin=js.indexOf(SHARD_PREFIX),end=js.lastIndexOf("]);})();");
        if(begin<0 || end<begin)throw new IOException("Formato shard non riconosciuto: " + source);
        String body=js.substring(begin+SHARD_PREFIX.length(),end);
        Object parsed=MiniJson.parse("["+body+"]");
        if(!(parsed instanceof List<?> rows))throw new IOException("Shard non-array: " + source);
        return rows;
    }
    private static Map<String,Object> decodeCareer(Object raw) {
        if(raw instanceof Map<?,?>)return asObject(raw); // controlled tests.
        if(!(raw instanceof List<?> r) || (r.size()!=6 && r.size()!=7))
            throw new IllegalArgumentException("Career compatta non valida: "+raw);
        if(!(r.get(4) instanceof List<?> real) || real.size()!=6)
            throw new IllegalArgumentException("Aggregato reale non valido");
        Map<String,Object> row=new LinkedHashMap<>();
        row.put("playerKey",r.get(0));row.put("nome",r.get(1));
        row.put("reale",Map.of("votiValidi",real.get(0),"sommaVoti",real.get(1),
            "eventi",decodeEvents(asList(real.get(2)))));
        if(r.size()==7) {
            try {row.put("costoAcquisto",decodeCost(r.get(6)));}
            catch(IOException ex){throw new IllegalArgumentException("Costo Career non valido",ex);}
        }
        return row;
    }
    private static Map<String,Object> decodeEvents(List<?> pairs) {
        if(pairs.size()%2!=0)throw new IllegalArgumentException("Eventi reali non validi");
        Map<String,Object> events=new LinkedHashMap<>();
        for(int i=0;i<pairs.size();i+=2) {
            int index=asInt(pairs.get(i));
            if(index<0||index>=EVENTS.size()||events.putIfAbsent(EVENTS.get(index),pairs.get(i+1))!=null)
                throw new IllegalArgumentException("Codice evento duplicato o fuori range");
        }
        return events;
    }
    private static String seasonOf(String key) {
        String season=key.length()>=9?key.substring(0,9):"";
        if(!season.matches("[0-9]{4}_[0-9]{4}") || key.length()<=10 || key.charAt(9)!=':')
            throw new IllegalArgumentException("PlayerKey non valido: "+key);
        return season;
    }
    @SuppressWarnings("unchecked")
    private static Map<String,Object> asObject(Object raw) {
        if(!(raw instanceof Map<?,?> m))throw new IllegalArgumentException("Formato non-oggetto: " + raw);
        return (Map<String,Object>)m;
    }
    private static List<?> asList(Object raw) {
        if(!(raw instanceof List<?> a))throw new IllegalArgumentException("Formato non-array: "+raw);
        return a;
    }
    private static int asInt(Object raw){if(!(raw instanceof Number n))throw new IllegalArgumentException("Contatore mancante");return n.intValue();}
    private static BigDecimal asDecimal(Object raw){if(raw==null)return BigDecimal.ZERO;return new BigDecimal(raw.toString());}
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
            TopFlopHistoricalViews.readTeams(prepared.jsDirectory(),sources,scopedMetadata),
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

## File aggiornato: src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopHistoricalFantasySmokeTest.java

```java
package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.config.MiniJson;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Real 21-season staging regression, never approves or publishes identities. */
public final class TopFlopHistoricalFantasySmokeTest {
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        Path js=Path.of(args[0]);
        String facade=Files.readString(js.resolve(TopFlopPublicViewsStageExporter.FACADE),StandardCharsets.UTF_8);
        int begin=facade.indexOf('{'),end=facade.lastIndexOf("};");
        Map<String,Object> meta=(Map<String,Object>)MiniJson.parse(facade.substring(begin,end+1));
        List<Map<String,Object>> seasons=(List<Map<String,Object>>)meta.get("stagioni");
        List<Path> files=new ArrayList<>();
        try(var f=Files.list(js)) {
            files.addAll(f.filter(p->p.getFileName().toString().matches(
                "fcmRecordsNext_TopFlopViews\\.(?:Careers|Teams)\\.[0-9]{4}_[0-9]{4}\\.[0-9]{3}\\.js"))
                .sorted().toList());
        }
        var careers=TopFlopHistoricalViews.readCareers(js,files);
        var teams=TopFlopHistoricalViews.readTeams(js,files,seasons);
        if(careers.size()!=16262)throw new AssertionError("Carriere="+careers.size());
        if(teams.size()!=45787)throw new AssertionError("Contesti="+teams.size());
        var higuain=careers.stream().filter(x->String.valueOf(x.get("nome")).startsWith("HIGUAIN Gonzalo")).toList();
        if(higuain.size()!=8)throw new AssertionError("Stagioni Higuaín="+higuain.size());
        Map<String,String> approved=new HashMap<>();
        for(var c:higuain)approved.put((String)c.get("playerKey"),"rn4p-higuain-test");
        var all=TopFlopHistoricalViews.aggregate(careers,teams,approved);
        if(all.size()!=1)throw new AssertionError("Identita storiche="+all.size());
        var player=all.get(0);
        if((int)player.get("numeroStagioni")!=8)throw new AssertionError("Stagioni incoerenti");
        if((int)player.get("votiRealiValidi")!=217)throw new AssertionError("Voti reali duplicati");
        List<Map<String,Object>> contexts=(List<Map<String,Object>>)player.get("fantasquadrePerStagione");
        long original=teams.stream().filter(t->approved.containsKey(t.get("playerKey"))).count();
        if(contexts.size()!=original || original<8)throw new AssertionError("Contesti non completi "+contexts.size()+" vs "+original);
        for(var t:contexts) {
            if(t.get("fantasquadra")==null||t.get("competizione")==null)throw new AssertionError("Nome assente");
            int n=((Number)t.get("gareConteggiate")).intValue();
            if(n>0) {
                BigDecimal avg=(BigDecimal)t.get("fantamediaApportata");
                BigDecimal sum=(BigDecimal)t.get("sommaFantavotiApportati");
                if(avg.multiply(BigDecimal.valueOf(n)).subtract(sum).abs().compareTo(new BigDecimal("0.00001"))>0)
                    throw new AssertionError("Fantamedia non ponderata correttamente");
            }
        }
        if(player.containsKey("sommaFantapuntiTotali") || player.containsKey("utilizziFantasyTotali"))
            throw new AssertionError("Totale fantasy improprio nella scheda storica");
        var cutCareers=careers.stream().filter(c->((String)c.get("playerKey")).startsWith("2015_2016:") ||
            ((String)c.get("playerKey")).startsWith("2016_2017:")).toList();
        var cutTeams=teams.stream().filter(c->((String)c.get("playerKey")).startsWith("2015_2016:") ||
            ((String)c.get("playerKey")).startsWith("2016_2017:")).toList();
        var cut=TopFlopHistoricalViews.aggregate(cutCareers,cutTeams,approved);
        if(cut.size()!=1|| (int)cut.get(0).get("numeroStagioni")!=2)
            throw new AssertionError("Cutoff storico non rispettato");
        for(var t:(List<Map<String,Object>>)cut.get(0).get("fantasquadrePerStagione"))
            if(!List.of("2015_2016","2016_2017").contains(t.get("stagione")))
                throw new AssertionError("Fantasia futura presente nel sito storico");
        // Full Java-publisher path: real staging -> site cutoff -> real JS History file.
        Path integrated=Files.createTempDirectory("rn4-history-publisher-smoke");
        try {
            var prepared=new TopFlopViewsPublication.Prepared(js,
                seasons.stream().map(m->String.valueOf(m.get("stagione"))).collect(java.util.stream.Collectors.toSet()),
                files.size(),approved);
            int attached=TopFlopViewsPublication.attachScoped(prepared,
                List.of("2015_2016","2016_2017"),integrated);
            if(attached<3 || TopFlopViewsPublication.validateStaging(integrated)!=attached)
                throw new AssertionError("Integrato: file staging mancanti");
            String siteFacade=Files.readString(integrated.resolve(TopFlopPublicViewsStageExporter.FACADE));
            int start=siteFacade.indexOf('{'),stop=siteFacade.lastIndexOf("};");
            Map<String,Object> siteMeta=(Map<String,Object>)MiniJson.parse(siteFacade.substring(start,stop+1));
            Map<String,Object> h=(Map<String,Object>)siteMeta.get("storico");
            if(((Number)h.get("identita")).intValue()!=1)throw new AssertionError("Integrato: identita mancante");
            List<Map<String,Object>> hfiles=(List<Map<String,Object>>)h.get("file");
            if(hfiles.size()!=1)throw new AssertionError("Integrato: numero shard storico errato");
            String historyJs=Files.readString(integrated.resolve((String)hfiles.get(0).get("nome")));
            if(!historyJs.contains("fantasquadrePerStagione") || !historyJs.contains("HIGUAIN"))
                throw new AssertionError("Integrato: storico senza rendimento fantasy");
            if(historyJs.contains("2017_2018:265"))throw new AssertionError("Integrato: leak cutoff futuro");
        }finally {
            try(var stream=Files.list(integrated)){for(Path f:stream.toList())Files.delete(f);}
            Files.delete(integrated);
        }
        Path dir=Files.createTempDirectory("rn4-history-fantasy-smoke");
        try {
            var staged=TopFlopPublicViewsStageExporter.shard(dir,"Storico","History",new ArrayList<Object>(all));
            if(staged.isEmpty())throw new AssertionError("Shard storico mancante");
            for(var shard:staged)if(shard.bytes()>1_500_000)throw new AssertionError("Shard troppo grande");
            String content=Files.readString(dir.resolve(staged.get(0).name()));
            if(!content.contains("fantasquadrePerStagione")||!content.contains("HIGUAIN"))
                throw new AssertionError("Rendimento fantasy non esposto nei JS");
        }finally {
            try(var stream=Files.list(dir)){for(Path f:stream.toList())Files.delete(f);}
            Files.delete(dir);
        }
        System.out.println("RN4_REAL_HISTORY_FANTASY_OK careers="+careers.size()+" teams="+teams.size()+
            " higuainSeasons="+higuain.size()+" contexts="+original+" cutoff=2 voteCount=217");
    }
}
```

## File aggiornato: src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopHistoricalFantasyContractSmokeTest.java

```java
package it.alterlega.recordsnext.app.topflop;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** No database/JS input: guarantees historical real vs fantasy separation. */
public final class TopFlopHistoricalFantasyContractSmokeTest {
    private static void ok(boolean yes,String why){if(!yes)throw new AssertionError(why);}
    private static Map<String,Object> career(String season,int votes,int sum) {
        return Map.of("playerKey",season+":7","nome","CALCIATORE X","reale",
            Map.of("votiValidi",votes,"sommaVoti",sum,"eventi",Map.of("GOLFATTI1",2)));
    }
    private static Map<String,Object> team(String season,int squad,int comp,int n,int total) {
        return Map.ofEntries(Map.entry("playerKey",season+":7"),Map.entry("idSquadraInterno",squad),
            Map.entry("idCompetizioneInterno",comp),Map.entry("fantasquadra","Squadra "+squad),
            Map.entry("competizione","Serie "+comp),Map.entry("famiglia","CAMPIONATO"),
            Map.entry("gareConteggiate",n),Map.entry("gareConVotoReale",n),
            Map.entry("sommaVotiRealiApportati",6*n),Map.entry("sommaFantavotiApportati",total),
            Map.entry("panchinaNonConteggiata",0),Map.entry("fuoriLista",0));
    }
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        var careers=List.of(career("2014_2015",10,65),career("2015_2016",20,130));
        var teams=List.of(team("2014_2015",1,4,5,40),team("2015_2016",1,4,10,80),
            team("2015_2016",2,8,5,20));
        var approved=Map.of("2014_2015:7","rn4p-test","2015_2016:7","rn4p-test");
        ok(TopFlopHistoricalViews.aggregate(careers,teams,Map.of()).isEmpty(),"No silent fusion");
        var result=TopFlopHistoricalViews.aggregate(careers,teams,approved);
        ok(result.size()==1,"Historic identity missing");
        var one=result.get(0);
        ok((int)one.get("numeroStagioni")==2,"Seasons");
        ok((int)one.get("votiRealiValidi")==30,"Real votes counted more than once");
        ok(((BigDecimal)one.get("mediaVotoReale")).compareTo(new BigDecimal("6.5"))==0,"Real average");
        List<Map<String,Object>> detail=(List<Map<String,Object>>)one.get("fantasquadrePerStagione");
        ok(detail.size()==3,"Fantasy contexts dropped or merged");
        for(var d:detail) {
            int squad=(int)d.get("idSquadraInterno"),n=(int)d.get("gareConteggiate");
            BigDecimal avg=(BigDecimal)d.get("fantamediaApportata");
            ok(avg.compareTo(new BigDecimal(squad==2?"4":"8"))==0,"Wrong fantasy average");
            ok(n>0,"Empty context should not become average");
        }
        ok(one.get("fantamediaReale")==null,"Real fantasy average must not be guessed");
        boolean duplicateBlocked=false;
        try {TopFlopHistoricalViews.aggregate(careers,List.of(teams.get(0),teams.get(0)),approved);}
        catch(IllegalArgumentException expected){duplicateBlocked=true;}
        ok(duplicateBlocked,"Duplicate fantasy context not rejected");
        boolean sameSeasonBlocked=false;
        try {TopFlopHistoricalViews.aggregate(List.of(career("2014_2015",10,65),
            Map.of("playerKey","2014_2015:8","nome","OTHER","reale",Map.of(
                "votiValidi",10,"sommaVoti",65,"eventi",Map.of()))),List.of(),
            Map.of("2014_2015:7","rn4p-test","2014_2015:8","rn4p-test"));}
        catch(IllegalArgumentException expected){sameSeasonBlocked=true;}
        ok(sameSeasonBlocked,"Collision within season not rejected");
        System.out.println("RN4_HISTORY_FANTASY_CONTRACT_OK verifiedReal=30 contexts=3 noGlobalFantasyTotals");
    }
}
```


# AGGIORNAMENTO SVILUPPO 4.0 - v38 / v46
# RecordsNext 4.0 — Diario v38 / correzione staging v46

Data: 11 novembre 2026. Base operativa: v45b installata nella copia `D:\DEV_APPS\RecordsNext4.0_TEST_v37`.

## Difetto riprodotto

Durante **Completa / Elabora senza pubblicare**, l'importazione e la normalizzazione di tutte le stagioni, i Classici e le 81 viste Top & Flop sono terminati. La generazione JS si e' poi fermata con:

`java.io.IOException: JS oltre 1.500.000 byte: fcmRecordsNext_Classics.js=8398766`

`Classics` e' un output 3.2 preesistente, non un output Top & Flop. Il ramo `RecordsNextPipeline` con `publish=false` validava l'output cumulativo prima di applicare il sistema 3.2 di shard flat. Il ramo `publish=true` gia' chiamava `SeasonFamilyShardPublisher.prepareFlat`.

## Correzione integrata (non una nuova pipeline)

- `RecordsNextPipeline.run`, ramo senza pubblicazione: dopo l'esportazione 3.2 invoca `SeasonFamilyShardPublisher.prepareFlat` sullo staging generato, prima del controllo sul peso.
- Aggiunta la facade Top & Flop dopo lo split dei Classici: nessuna duplicazione dei dati gia' presenti e nessun cambiamento ai contratti legacy.
- `ManifestPublishingSupport.write` rigenera il manifest con i nomi effettivi degli shard e delle facade anche nell'anteprima.
- `TopFlopViewsPublication.validateStaging` controlla **tutti** i file JS fisici presenti, anche senza Top & Flop.
- Il risultato `Result.files` descrive il numero di file realmente validati dopo lo split, non la somma inaccurata degli originali e dei soli Top & Flop.
- Il ramo multisito utilizza lo stesso limite di 1.500.000 byte e convalida gli output dopo lo sharding. Il cutoff, il rollback, le associazioni di squadre 3.2 e le regole di identita' 4.0 restano invariati.
- Il valore predefinito degli shard flat e' ora **1.500.000 byte esatti**. Il precedente margine tecnico 3.2 da 1536 KiB non e' equivalente.

## Test eseguiti localmente

- Compilazione Java 21 dei sorgenti 4.0 integrati, senza errori.
- `RecordsNextFlatStagingV46SmokeTest`: Classics cumulativo **9.012.856 byte** diviso in 20 shard e facade leggera; massimo shard **452.641 byte**, 21 file JS convalidati.
- I 250 record per ciascuna delle 20 stagioni sono conservati negli shard; la facade elenca i file stagionali, senza sottocartelle.
- Rifiutato sia un singolo JS non shardabile oltre limite, sia uno shard stagionale che oltrepassi 1.500.000 byte.
- Smoke test di identita' e fantasy 4.0 rieseguiti; il registro dell'utente non e' stato modificato.

Il test di 9 MB usa **dati sintetici nel formato Classici**, poiche' il cumulativo effettivamente generato da Windows non e' disponibile in questo ambiente. L'esecuzione end-to-end sulla copia Windows dell'utente resta un collaudo obbligatorio, non ancora certificato.

## Sicurezza e operativita'

- Patch valida solo per la copia TEST e solo se gli hash SHA-256 coincidono con la v45b realmente installata.
- Backup versionato di sorgenti, documentazione e JAR prima delle modifiche; rollback automatico se la compilazione/test falliscono.
- Non modifica `config/player-identities.json`, DB SQLite, file FCA/FCM, cartelle di pubblicazione o RecordsNext 3.2.
- Il collaudo si esegue sempre con `publish=false`. Avvio Java 21 con dipendenze UCanAccess 2.0.9.5 esterne in sola lettura.
- Nessuna pubblicazione GitHub e' attestata finche' non esiste un commit/push confermato dal repository remoto.

## Ancora da completare per Renewo

351 proposte di identita' sospese; fantamedia reale indipendente, fotografie storiche e indice tecnico/economico configurabile, oltre alla verifica di export completo e dei cutoff su siti di prova. La patch **non** dichiara RecordsNext 4.0 concluso.

## src/main/java/it/alterlega/recordsnext/app/RecordsNextPipeline.java

SHA-256: `ca31c1784f10636fab0cef6e6b6ccfc4a82a67dd348ed269c3b6bd989f7390f8`

```java
package it.alterlega.recordsnext.app;

import it.alterlega.recordsnext.Records2026SitePublisher;
import it.alterlega.recordsnext.RiserveUfficioArchiveBuilder;
import it.alterlega.recordsnext.SeasonRecordsArchiveBuilder;
import it.alterlega.recordsnext.app.manifest.ManifestMetadata;
import it.alterlega.recordsnext.app.core.LeagueMetadata;
import it.alterlega.recordsnext.app.core.LeagueMetadataLoader;
import it.alterlega.recordsnext.app.model.RecordFamily;
import it.alterlega.recordsnext.app.topflop.TopFlopViewsPublication;
import it.alterlega.recordsnext.app.topflop.TopFlopPublicViewsStageExporter;
import it.alterlega.recordsnext.app.manifest.ManifestPublishingSupport;
import it.alterlega.recordsnext.app.core.CoreJsExporter;
import it.alterlega.recordsnext.app.output.SeasonPublicationTargetRepository;
import it.alterlega.recordsnext.app.output.SeasonFamilyShardPublisher;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Comparator;
import java.util.Set;

public final class RecordsNextPipeline {
    private static final Set<RecordFamily> IMPLEMENTED_FAMILIES = Set.copyOf(
            EnumSet.of(
                    RecordFamily.CLASSICS,
                    RecordFamily.SERIES,
                    RecordFamily.RU,
                    RecordFamily.MODIFIERS,
                    RecordFamily.THRESHOLDS_LUCK,
                    RecordFamily.TOP_FLOP
            )
    );

    public interface Listener {
        void phase(String text, int percent);

        default void timing(String text) {
            phase("TEMPO " + text, -1);
        }
    }

    public record Result(
            int classicEntries,
            int ruSeasons,
            int files,
            int published
    ) {
    }

    public enum PublicationMode {
        CURRENT_SITE,
        ALL_CONFIGURED_SITES
    }

    public Result run(
            PipelineConfig c,
            ProcessingOptions o,
            ProcessingMode mode,
            Listener l
    ) throws Exception {
        return run(c, o, mode, l, PublicationMode.CURRENT_SITE);
    }

    public Result run(
            PipelineConfig c,
            ProcessingOptions o,
            ProcessingMode mode,
            Listener l,
            PublicationMode publicationMode
    ) throws Exception {
        PipelinePreflight.Result preflight = preflight(o);

        l.phase(preflight.summary(), 2);

        for (String message : preflight.messages()) {
            l.phase("PREFLIGHT " + message, -1);
        }

        validateImplementedFamilies(o);

        long totalStarted = System.nanoTime();

        Path database = c.projectRoot()
                .resolve("data/database/recordsnext.db")
                .normalize();

        RecordsNextPreparationService preparation =
                new RecordsNextPreparationService(
                        c.projectRoot(),
                        database
                );

        long preparationStarted = System.nanoTime();

        List<String> changedSeasons =
                preparation.prepare(
                        mode,
                        c.seasons(),
                        l
                );

        l.timing(
                "preparazione complessiva: "
                        + elapsed(preparationStarted)
        );

        /*
         * Una stagione puo' risultare modificata/elaborata senza produrre
         * alcun season_normalized_*.json.
         *
         * Caso reale:
         * 2026_2027 e' correttamente la stagione corrente, ma prima della
         * prima partita non esiste ancora alcuna competizione testa-a-testa
         * elaborabile.
         *
         * In modalita' CONSOLIDATED questo NON deve essere considerato
         * un errore e gli archivi storici gia' consolidati non devono
         * essere ricostruiti.
         */
        List<String> reportSeasons = new ArrayList<>();

        for (String season : changedSeasons) {
            Path seasonReports = c.reports()
                    .resolve(season)
                    .normalize();

            if (!Files.isDirectory(seasonReports)) {
                continue;
            }

            try (var files = Files.list(seasonReports)) {
                boolean hasNormalized = files.anyMatch(path -> {
                    if (!Files.isRegularFile(path)) {
                        return false;
                    }

                    String name = path.getFileName().toString();

                    return name.startsWith("season_normalized_")
                            && name.endsWith(".json");
                });

                if (hasNormalized) {
                    reportSeasons.add(season);
                }
            }
        }

        if (mode == ProcessingMode.FULL) {
            l.phase(
                    "Pulizia archivi derivati delle stagioni gestite",
                    52
            );

            for (String season : changedSeasons) {
                deleteTree(
                        c.classicArchive().resolve(season)
                );

                deleteTree(
                        c.ruArchive().resolve(season)
                );
            }
        }

        if (o.familyEnabled(RecordFamily.CLASSICS)) {
            if (reportSeasons.isEmpty()) {
                l.phase(
                        "Record classici invariati: nessun nuovo report normalizzato",
                        55
                );
            } else {
                l.phase(
                        "Generazione record classici",
                        55
                );

                long started = System.nanoTime();

                SeasonRecordsArchiveBuilder.build(
                        c.reports(),
                        c.classicArchive(),
                        reportSeasons
                );

                l.timing(
                        "record classici: "
                                + elapsed(started)
                );
            }
        }

        if (o.familyEnabled(RecordFamily.RU)) {
            if (reportSeasons.isEmpty()) {
                l.phase(
                        "Riserve d'ufficio invariate: nessun nuovo report normalizzato",
                        68
                );
            } else {
                l.phase(
                        "Generazione riserve d'ufficio",
                        68
                );

                long started = System.nanoTime();

                RiserveUfficioArchiveBuilder.build(
                        c.reports(),
                        c.ruArchive(),
                        reportSeasons
                );

                l.timing(
                        "riserve d'ufficio: "
                                + elapsed(started)
                );
            }
        }

        Result result;
        Path topFlopScratch = null;
        TopFlopViewsPublication.Prepared topFlopPrepared = null;
        if (o.generateJs() && o.familyEnabled(RecordFamily.TOP_FLOP)) {
            Files.createDirectories(c.staging());
            topFlopScratch = Files.createTempDirectory(c.staging(), "recordsnext4-topflop-");
            try {
                l.phase("Elaborazione delle viste Top & Flop selezionate (SQLite locale)", 79);
                topFlopPrepared = TopFlopViewsPublication.prepare(
                        database, c.reports(), c.seasons(),
                        TopFlopViewsPublication.selectedIds(o.selection()), topFlopScratch,
                        it.alterlega.recordsnext.app.topflop.TopFlopSettings.load(
                                c.projectRoot().resolve("config/topflop.json")),
                        c.projectRoot().resolve("config/player-identities.json")
                );
                l.phase("Top & Flop: " + topFlopPrepared.files()
                        + " JS verificati, una sola elaborazione per tutte le destinazioni", -1);
            } catch (Exception ex) {
                deleteTree(topFlopScratch);
                throw ex;
            }
        }

        try {
        if (!o.generateJs()) {
            l.phase(
                    "Archivi elaborati; generazione JavaScript non richiesta",
                    96
            );

            result = new Result(
                    0,
                    0,
                    0,
                    0
            );
        } else {
            l.phase(
                    o.publish()
                            ? "Generazione e pubblicazione JavaScript"
                            : "Generazione JavaScript",
                    82
            );

            long started = System.nanoTime();

            LeagueMetadata leagueMetadata =
                    LeagueMetadataLoader.load(
                            c.projectRoot()
                                    .resolve("config/league.json")
                    );

            if (!o.publish()) {
                ManifestMetadata manifestMetadata =
                        new ManifestMetadata(
                                "RecordsNext by mauz79",
                                "3.2.0",
                                "2.0",
                                OffsetDateTime.now(),
                                leagueMetadata.leagueId(),
                                leagueMetadata.currentSeasonId(),
                                c.seasons(),
                                List.of()
                        );

                var r = Records2026SitePublisher.run(
                        c.classicArchive(),
                        c.ruArchive(),
                        c.staging(),
                        c.siteJs(),
                        true,
                        o.familyEnabled(RecordFamily.CLASSICS),
                        o.familyEnabled(RecordFamily.RU),
                        o,
                        preflight,
                        manifestMetadata,
                        database,
                        leagueMetadata,
                        c.reports()
                );

                // In modalita solo generazione i sette JS legacy sono ancora cumulativi.
                // Applichiamo lo stesso sharding flat 3.2 del ramo multisito PRIMA del
                // controllo 1.500.000 byte, conservando facade e nomi pubblici.
                Path generatedJs = r.stagingDirectory().resolve("js");
                var flatPlan = SeasonFamilyShardPublisher.prepareFlat(
                        generatedJs, TopFlopPublicViewsStageExporter.LIMIT_BYTES);

                int addedTopFlop = 0;
                if (topFlopPrepared != null) {
                    addedTopFlop = TopFlopViewsPublication.attachScoped(
                            topFlopPrepared, c.seasons(), generatedJs);
                }
                // Il manifest descrive i file effettivamente generati, inclusi
                // gli shard. Validiamo TUTTI i JS, anche se Top & Flop e' disattivato.
                ManifestPublishingSupport.write(generatedJs, o, preflight, manifestMetadata);
                int validatedFiles = TopFlopViewsPublication.validateStaging(generatedJs);
                l.phase("Staging JS di prova: " + flatPlan.shards().size()
                        + " shard legacy flat, " + addedTopFlop + " file Top & Flop, "
                        + validatedFiles + " JS verificati (massimo 1.500.000 byte)", -1);
                result = new Result(
                        r.classicEntries(),
                        r.ruSeasons(),
                        validatedFiles,
                        0
                );
            } else {
                SeasonPublicationTargetRepository targetRepository =
                        new SeasonPublicationTargetRepository(database);

                List<SeasonPublicationTargetRepository.Target> targets =
                        targetRepository.load(c.seasons());

                List<SeasonPublicationTargetRepository.Target> selectedTargets =
                        selectPublicationTargets(
                                targets,
                                leagueMetadata.currentSeasonId(),
                                publicationMode
                        );

                List<SeasonPublicationTargetRepository.Target> availableTargets =
                        selectedTargets.stream()
                                .filter(SeasonPublicationTargetRepository.Target::available)
                                .toList();

                for (SeasonPublicationTargetRepository.Target target : selectedTargets) {
                    if (!target.available()) {
                        l.phase(
                                "PUBBLICAZIONE saltata per "
                                        + target.seasonId()
                                        + ": sito locale non disponibile: "
                                        + target.siteRoot(),
                                -1
                        );
                    }
                }

                if (availableTargets.isEmpty()) {
                    if (publicationMode == PublicationMode.CURRENT_SITE) {
                        throw new IllegalStateException(
                                "Nessun sito locale configurato e disponibile per la stagione corrente "
                                        + leagueMetadata.currentSeasonId()
                                        + ". Configurare il sito della stagione oppure disattivare la pubblicazione."
                        );
                    }
                    throw new IllegalStateException(
                            "Nessun sito locale configurato e disponibile per la pubblicazione multisito."
                    );
                }

                int totalClassicEntries = 0;
                int totalRuSeasons = 0;
                int totalFiles = 0;
                int totalPublished = 0;

                Path scopesRoot = c.staging()
                        .resolve("multisite-scopes")
                        .normalize();

                deleteTree(scopesRoot);
                Files.createDirectories(scopesRoot);

                Path culometroConfig = c.projectRoot()
                        .resolve("config/culometro.json")
                        .normalize();

                if (o.culometroEnabled()) {
                    if (!Files.isRegularFile(culometroConfig)) {
                        throw new IllegalStateException(
                                "Configurazione Culometro non trovata: "
                                        + culometroConfig
                        );
                    }

                    Path scopedConfigDir = scopesRoot.resolve("config");
                    Files.createDirectories(scopedConfigDir);
                    Files.copy(
                            culometroConfig,
                            scopedConfigDir.resolve("culometro.json"),
                            StandardCopyOption.REPLACE_EXISTING,
                            StandardCopyOption.COPY_ATTRIBUTES
                    );
                }

                try {
                    for (SeasonPublicationTargetRepository.Target target : availableTargets) {
                        List<String> targetSeasons =
                                targetRepository.scope(
                                        c.seasons(),
                                        target
                                );

                        if (targetSeasons.isEmpty()) {
                            l.phase(
                                    "PUBBLICAZIONE saltata per "
                                            + target.seasonId()
                                            + ": nessuna stagione selezionata nello scope.",
                                    -1
                            );
                            continue;
                        }

                        l.phase(
                                "Pubblicazione sito "
                                        + target.seasonId()
                                        + " -> "
                                        + target.siteJs(),
                                84
                        );

                        Path targetScope = scopesRoot.resolve(target.seasonId());
                        Path scopedClassic = targetScope.resolve("classic");
                        Path scopedRu = targetScope.resolve("ru");
                        Path scopedReports = targetScope.resolve("reports");

                        createSeasonScopedView(
                                c.classicArchive(),
                                scopedClassic,
                                targetSeasons
                        );
                        createSeasonScopedView(
                                c.ruArchive(),
                                scopedRu,
                                targetSeasons
                        );
                        createSeasonScopedView(
                                c.reports(),
                                scopedReports,
                                targetSeasons
                        );

                        ManifestMetadata targetManifest =
                                new ManifestMetadata(
                                        "RecordsNext by mauz79",
                                        "3.2.0",
                                        "2.0",
                                        OffsetDateTime.now(),
                                        leagueMetadata.leagueId(),
                                        target.seasonId(),
                                        targetSeasons,
                                        List.of()
                                );

                        var generated = Records2026SitePublisher.run(
                                scopedClassic,
                                scopedRu,
                                c.staging(),
                                target.siteJs(),
                                true,
                                o.familyEnabled(RecordFamily.CLASSICS),
                                o.familyEnabled(RecordFamily.RU),
                                o,
                                preflight,
                                targetManifest,
                                null,
                                null,
                                scopedReports
                        );

                        Path generatedDir =
                                generated.stagingDirectory().resolve("js");

                        CoreJsExporter.export(
                                database,
                                generatedDir.resolve("fcmRecordsNext_Core.js"),
                                leagueMetadata.leagueId(),
                                leagueMetadata.leagueName(),
                                target.seasonId()
                        );

                        if (topFlopPrepared != null) {
                            int appended = TopFlopViewsPublication.attachScoped(
                                    topFlopPrepared, targetSeasons, generatedDir);
                            l.phase("Top & Flop " + target.seasonId()
                                    + ": " + appended + " JS nel cutoff storico", -1);
                        }

                        var flatPlan = SeasonFamilyShardPublisher.prepareFlat(
                                generatedDir, TopFlopPublicViewsStageExporter.LIMIT_BYTES);
                        // Anche nei siti 3.2/4.0 senza Top & Flop la validazione
                        // si applica a facade, shard e altri JS dopo lo split.
                        ManifestPublishingSupport.write(generatedDir, o, preflight, targetManifest);
                        TopFlopViewsPublication.validateStaging(generatedDir);
                        l.phase(
                                "Output flat " + target.seasonId()
                                        + ": shard=" + flatPlan.shards().size()
                                        + ", max=" + String.format(Locale.ROOT, "%.2f MiB",
                                                flatPlan.maxShardBytes() / (1024.0 * 1024.0)),
                                -1
                        );

                        int published = publishGeneratedDirectory(
                                generatedDir,
                                target.siteJs()
                        );

                        totalClassicEntries += generated.classicEntries();
                        totalRuSeasons += generated.ruSeasons();
                        totalFiles += published;
                        totalPublished += published;
                    }
                } finally {
                    deleteTree(scopesRoot);
                }

                result = new Result(
                        totalClassicEntries,
                        totalRuSeasons,
                        totalFiles,
                        totalPublished
                );
            }

            l.timing(
                    (
                            o.publish()
                                    ? "generazione e pubblicazione JavaScript multisito: "
                                    : "generazione JavaScript: "
                    )
                            + elapsed(started)
            );
        }

        } finally {
            if (topFlopScratch != null) deleteTree(topFlopScratch);
        }

        preparation.saveConsolidation(
                c.seasons()
        );

        l.timing(
                "totale elaborazione: "
                        + elapsed(totalStarted)
        );

        l.phase(
                "Elaborazione completata e consolidamento aggiornato",
                100
        );

        return result;
    }

    public PipelinePreflight.Result preflight(
            ProcessingOptions options
    ) {
        return PipelinePreflight.evaluate(options);
    }

    public boolean hasConsolidation(
            PipelineConfig c
    ) {
        Path database = c.projectRoot()
                .resolve("data/database/recordsnext.db")
                .normalize();

        return new RecordsNextPreparationService(
                c.projectRoot(),
                database
        ).hasConsolidation();
    }

    static void validateImplementedFamilies(
            ProcessingOptions options
    ) {
        Set<RecordFamily> unsupported =
                EnumSet.copyOf(
                        options.selection().enabledFamilies()
                );

        unsupported.removeAll(
                IMPLEMENTED_FAMILIES
        );

        if (!unsupported.isEmpty()) {
            throw new IllegalArgumentException(
                    "Famiglie selezionate ma non ancora collegate alla pipeline 2.0: "
                            + unsupported
            );
        }
    }

    static List<SeasonPublicationTargetRepository.Target> selectPublicationTargets(
            List<SeasonPublicationTargetRepository.Target> targets,
            String currentSeasonId,
            PublicationMode mode) {

        if (mode == PublicationMode.ALL_CONFIGURED_SITES) {
            return List.copyOf(targets);
        }

        return targets.stream()
                .filter(target -> target.seasonId().equals(currentSeasonId))
                .toList();
    }

    private static void createSeasonScopedView(
            Path sourceRoot,
            Path targetRoot,
            List<String> seasons) throws Exception {

        Files.createDirectories(targetRoot);

        for (String season : seasons) {
            Path source = sourceRoot.resolve(season).normalize();
            if (!Files.isDirectory(source)) {
                continue;
            }

            copyTree(source, targetRoot.resolve(season));
        }
    }

    private static void copyTree(
            Path source,
            Path target) throws Exception {

        try (var paths = Files.walk(source)) {
            for (Path path : paths.toList()) {
                Path relative = source.relativize(path);
                Path destination = target.resolve(relative);

                if (Files.isDirectory(path)) {
                    Files.createDirectories(destination);
                } else {
                    Files.createDirectories(destination.getParent());
                    Files.copy(
                            path,
                            destination,
                            StandardCopyOption.REPLACE_EXISTING,
                            StandardCopyOption.COPY_ATTRIBUTES
                    );
                }
            }
        }
    }

    static int publishGeneratedDirectory(
            Path generatedDir,
            Path siteJsDir) throws Exception {

        Files.createDirectories(siteJsDir);

        List<Path> files;
        try (var stream = Files.list(generatedDir)) {
            files = stream
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(
                            path -> path.getFileName().toString()
                    ))
                    .toList();
        }

        Set<String> generatedNames = files.stream()
                .map(path -> path.getFileName().toString())
                .collect(java.util.stream.Collectors.toSet());

        List<Path> staleFlatShards;
        try (var stream = Files.list(siteJsDir)) {
            staleFlatShards = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> SeasonFamilyShardPublisher.isFlatShardFileName(
                            path.getFileName().toString())
                            || path.getFileName().toString().equals(
                                    "fcmRecordsNext_TopFlopViews.js")
                            || path.getFileName().toString().matches(
                                    "fcmRecordsNext_TopFlopViews\\.(?:Careers|Teams|Records|Highlights)\\."
                                    + "[0-9]{4}_[0-9]{4}\\.[0-9]{3}\\.js"))
                    .filter(path -> !generatedNames.contains(path.getFileName().toString()))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .toList();
        }

        Path backupRoot = generatedDir.getParent()
                .resolve("multisite-publish-backup");
        deleteTree(backupRoot);
        Files.createDirectories(backupRoot);

        List<Path> replaced = new ArrayList<>();
        List<Path> created = new ArrayList<>();
        List<Path> removedStale = new ArrayList<>();

        try {
            for (Path stale : staleFlatShards) {
                Path backup = backupRoot.resolve(stale.getFileName());
                Files.copy(
                        stale,
                        backup,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.COPY_ATTRIBUTES
                );
                Files.delete(stale);
                removedStale.add(stale);
            }

            for (Path source : files) {
                Path target = siteJsDir.resolve(source.getFileName());

                if (Files.exists(target)) {
                    Files.copy(
                            target,
                            backupRoot.resolve(source.getFileName()),
                            StandardCopyOption.REPLACE_EXISTING,
                            StandardCopyOption.COPY_ATTRIBUTES
                    );
                    replaced.add(target);
                } else {
                    created.add(target);
                }

                Path temp = siteJsDir.resolve(
                        "." + source.getFileName()
                                + ".recordsnext-multisite.tmp"
                );

                Files.copy(
                        source,
                        temp,
                        StandardCopyOption.REPLACE_EXISTING
                );

                try {
                    Files.move(
                            temp,
                            target,
                            StandardCopyOption.ATOMIC_MOVE,
                            StandardCopyOption.REPLACE_EXISTING
                    );
                } catch (java.nio.file.AtomicMoveNotSupportedException ex) {
                    Files.move(
                            temp,
                            target,
                            StandardCopyOption.REPLACE_EXISTING
                    );
                }
            }
        } catch (Exception ex) {
            for (Path target : created) {
                Files.deleteIfExists(target);
            }

            for (Path target : replaced) {
                Path backup = backupRoot.resolve(target.getFileName());
                if (Files.isRegularFile(backup)) {
                    Files.copy(
                            backup,
                            target,
                            StandardCopyOption.REPLACE_EXISTING
                    );
                }
            }

            for (Path target : removedStale) {
                Path backup = backupRoot.resolve(target.getFileName());
                if (Files.isRegularFile(backup)) {
                    Files.copy(
                            backup,
                            target,
                            StandardCopyOption.REPLACE_EXISTING
                    );
                }
            }

            throw ex;
        }

        return files.size();
    }

    private static void deleteTree(
            Path root
    ) throws Exception {
        if (!Files.exists(root)) {
            return;
        }

        try (var paths = Files.walk(root)) {
            for (Path path :
                    paths.sorted(
                            java.util.Comparator.reverseOrder()
                    ).toList()) {

                Files.deleteIfExists(path);
            }
        }
    }

    private static String elapsed(
            long started
    ) {
        double seconds =
                (System.nanoTime() - started)
                        / 1_000_000_000.0;

        return String.format(
                Locale.ROOT,
                "%.3f s",
                seconds
        );
    }
}
```

## src/main/java/it/alterlega/recordsnext/app/output/SeasonFamilyShardPublisher.java

SHA-256: `cb64c3d84db47c1e5e0afa5ebb2f2015f73717827ad8c6149c7e6f9e44e765a9`

```java
package it.alterlega.recordsnext.app.output;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;

/**
 * Trasforma gli output cumulativi RecordsNext in facade compatibili e shard stagionali.
 *
 * <p>Ogni shard appartiene fisicamente al sito della propria stagione. Le facade conservano
 * gli stessi nomi file e gli stessi globali pubblici usati dal frontend corrente. Gli shard
 * vengono caricati come normali script, quindi non richiedono PHP, rewrite, CORS o API server.
 *
 * <p>La disponibilita' e' progressiva: la stagione anchor corrente e' sempre caricabile;
 * le stagioni storiche entrano nelle facade online solo quando risultano esplicitamente
 * abilitate nello stato persistente. In questo modo un sito nuovo puo' partire con la sola
 * stagione corrente senza produrre richieste 404 verso siti storici non ancora pubblicati.
 */
public final class SeasonFamilyShardPublisher {
    public static final long DEFAULT_MAX_SHARD_BYTES = 1024L * 1024L;
    /**
     * Limite fisico 4.0: 1.500.000 byte (non 1.536 KiB).
     * Condiviso dai percorsi di generazione e pubblicazione; lo split deve
     * avvenire prima della validazione finale dei JS distribuiti.
     */
    public static final long DEFAULT_FLAT_MAX_SHARD_BYTES = 1_500_000L;
    public static final String DATA_DIR = "recordsnext-data";
    public static final String STATE_FILE_NAME = "recordsnext-shards.properties";

    private static final List<FamilySpec> FAMILIES = List.of(
            new FamilySpec("Classics", "fcmRecordsNext_Classics.js", "window.fcmRecordsNextClassics",
                    "stagione", List.of("seasonAggregates")),
            new FamilySpec("Series", "fcmRecordsNext_Series.js", "window.fcmRecordsNextSeries",
                    "stagione", List.of("seasonAggregates")),
            new FamilySpec("RU", "fcmRecordsNext_RU.js", "window.fcmRecordsNextRU",
                    "stagione", List.of("seasonAggregates")),
            new FamilySpec("Modifiers", "fcmRecordsNext_Modifiers.js", "window.fcmRecordsNextModifiers",
                    "stagione", List.of("seasonAggregates")),
            new FamilySpec("ThresholdsLuck", "fcmRecordsNext_ThresholdsLuck.js", "window.fcmRecordsNextThresholdsLuck",
                    "seasonId", List.of("events", "seasonAggregates")),
            new FamilySpec("Culometro", "fcmRecordsNext_Culometro.js", "window.fcmRecordsNextCulometro",
                    "seasonId", List.of("events", "ranking", "competitionRanking")),
            new FamilySpec("Matches", "fcmRecordsNext_Matches.js", "window.fcmRecordsNextMatches",
                    "seasonId", List.of("matches"))
    );

    private SeasonFamilyShardPublisher() {}

    /**
     * Trasforma le sette famiglie pesanti in facade leggere + shard stagionali
     * direttamente nella stessa cartella JS. Non crea sottocartelle, non usa
     * routing tra siti e non dipende dallo stato di disponibilita' legacy.
     *
     * <p>Questa e' la strategia usata dalla pubblicazione multisito 3.1.1:
     * ogni target contiene autonomamente tutti gli shard ammessi dal proprio
     * cutoff storico.</p>
     */
    public static FlatPlan prepareFlat(Path generatedDir) throws IOException {
        return prepareFlat(generatedDir, DEFAULT_FLAT_MAX_SHARD_BYTES);
    }

    public static FlatPlan prepareFlat(Path generatedDir, long maxShardBytes) throws IOException {
        Objects.requireNonNull(generatedDir, "generatedDir");
        if (maxShardBytes < 1) throw new IllegalArgumentException("maxShardBytes deve essere positivo");
        if (!Files.isDirectory(generatedDir)) {
            throw new IOException("Cartella JS generata non trovata: " + generatedDir);
        }

        deleteExistingFlatShards(generatedDir);

        List<FlatShard> shards = new ArrayList<>();
        List<Path> facades = new ArrayList<>();

        for (FamilySpec spec : FAMILIES) {
            Path familyFile = generatedDir.resolve(spec.fileName());
            if (!Files.isRegularFile(familyFile)) continue;

            Assignment assignment = readAssignment(familyFile, spec.globalName());
            Map<String, Object> root = assignment.root();
            Map<String, Object> facadeRoot = deepCopyMap(root);
            for (String field : spec.shardedFields()) facadeRoot.put(field, new ArrayList<>());

            Map<String, Map<String, List<Object>>> bySeason = splitBySeason(root, spec);
            List<FlatShard> familyShards = new ArrayList<>();

            for (Map.Entry<String, Map<String, List<Object>>> entry : bySeason.entrySet()) {
                String seasonId = entry.getKey();
                String shardName = "fcmRecordsNext_" + spec.id() + "." + seasonId + ".js";
                Path shardFile = generatedDir.resolve(shardName);

                String shardJs = renderShard(spec, entry.getValue());
                Files.writeString(shardFile, shardJs, StandardCharsets.UTF_8);

                long bytes = Files.size(shardFile);
                if (bytes > maxShardBytes) {
                    throw new IOException("Shard flat oltre il limite di sicurezza di " + maxShardBytes
                            + " byte: " + shardFile.getFileName() + " = " + bytes);
                }

                FlatShard shard = new FlatShard(spec.id(), seasonId, shardFile, bytes);
                shards.add(shard);
                familyShards.add(shard);
            }

            String facadeJs = renderFlatFacade(spec, facadeRoot, familyShards);
            Files.writeString(familyFile, facadeJs, StandardCharsets.UTF_8);
            facades.add(familyFile);
        }

        return new FlatPlan(List.copyOf(shards), List.copyOf(facades));
    }

    private static void deleteExistingFlatShards(Path generatedDir) throws IOException {
        try (var files = Files.list(generatedDir)) {
            for (Path file : files.filter(Files::isRegularFile).toList()) {
                if (isFlatShardFileName(file.getFileName().toString())) {
                    Files.deleteIfExists(file);
                }
            }
        }
    }

    public static boolean isFlatShardFileName(String name) {
        if (name == null) return false;
        for (FamilySpec spec : FAMILIES) {
            String prefix = "fcmRecordsNext_" + spec.id() + ".";
            if (name.startsWith(prefix) && name.endsWith(".js")
                    && name.length() > prefix.length() + 3) {
                String season = name.substring(prefix.length(), name.length() - 3);
                return season.matches("\\d{4}_\\d{4}");
            }
        }
        return false;
    }

    public static Plan prepare(Path generatedDir, Path shardStagingRoot) throws IOException {
        Path transientState = shardStagingRoot.resolve(STATE_FILE_NAME);
        return prepare(generatedDir, shardStagingRoot, transientState, DEFAULT_MAX_SHARD_BYTES);
    }

    public static Plan prepare(Path generatedDir, Path shardStagingRoot, Path stateFile) throws IOException {
        return prepare(generatedDir, shardStagingRoot, stateFile, DEFAULT_MAX_SHARD_BYTES);
    }

    public static Plan prepare(Path generatedDir, Path shardStagingRoot, Path stateFile, long maxShardBytes)
            throws IOException {
        Objects.requireNonNull(generatedDir, "generatedDir");
        Objects.requireNonNull(shardStagingRoot, "shardStagingRoot");
        Objects.requireNonNull(stateFile, "stateFile");
        if (maxShardBytes < 1) throw new IllegalArgumentException("maxShardBytes deve essere positivo");

        Path coreFile = generatedDir.resolve("fcmRecordsNext_Core.js");
        if (!Files.isRegularFile(coreFile)) {
            throw new IOException("Core richiesto per la distribuzione stagionale: " + coreFile);
        }
        Assignment core = readAssignment(coreFile, "window.fcmRecordsNextCore");
        Map<String, SeasonRoute> routes = readRoutes(core.root());
        String anchorSeasonId = routes.values().stream()
                .filter(SeasonRoute::anchor)
                .map(SeasonRoute::seasonId)
                .findFirst()
                .orElseThrow(() -> new IOException("Core senza stagione anchor"));
        ShardState state = ShardState.load(stateFile);
        Files.createDirectories(shardStagingRoot);

        List<Shard> shards = new ArrayList<>();
        List<Path> facades = new ArrayList<>();
        for (FamilySpec spec : FAMILIES) {
            Path familyFile = generatedDir.resolve(spec.fileName());
            if (!Files.isRegularFile(familyFile)) continue;
            Assignment assignment = readAssignment(familyFile, spec.globalName());
            Map<String, Object> root = assignment.root();
            Map<String, Object> facadeRoot = deepCopyMap(root);
            for (String field : spec.shardedFields()) facadeRoot.put(field, new ArrayList<>());

            Map<String, Map<String, List<Object>>> bySeason = splitBySeason(root, spec);
            for (Map.Entry<String, Map<String, List<Object>>> entry : bySeason.entrySet()) {
                String seasonId = entry.getKey();
                SeasonRoute route = routes.get(seasonId);
                if (route == null) {
                    throw new IOException("Nessun percorso sito nel Core per la stagione " + seasonId
                            + " richiesta da " + spec.fileName());
                }
                String shardName = "fcmRecordsNext_" + spec.id() + "." + seasonId + ".js";
                Path staged = shardStagingRoot.resolve(seasonId).resolve(DATA_DIR).resolve(shardName);
                Files.createDirectories(staged.getParent());
                String shardJs = renderShard(spec, entry.getValue());
                Files.writeString(staged, shardJs, StandardCharsets.UTF_8);
                long bytes = Files.size(staged);
                if (bytes >= maxShardBytes) {
                    throw new IOException("Shard oltre il limite di " + maxShardBytes + " byte: "
                            + staged.getFileName() + " = " + bytes);
                }
                Path target = route.localSitePath() == null ? null
                        : route.localSitePath().resolve("js").resolve(DATA_DIR).resolve(shardName);
                String onlineUrl = route.onlineSiteUrl() == null ? null
                        : joinUrl(route.onlineSiteUrl(), "js/" + DATA_DIR + "/" + shardName);
                String localUrl = target == null ? null : target.toUri().toString();
                boolean current = seasonId.equals(anchorSeasonId);
                boolean localActive = current || state.localEnabled(seasonId)
                        || (target != null && Files.isRegularFile(target));
                boolean onlineActive = current || state.onlineEnabled(seasonId);
                shards.add(new Shard(spec.id(), seasonId, staged, target, onlineUrl, localUrl, bytes,
                        sha256(staged), current, localActive, onlineActive));
            }

            String facadeJs = renderFacade(spec, facadeRoot, shardsForFamily(shards, spec.id()));
            Files.writeString(familyFile, facadeJs, StandardCharsets.UTF_8);
            facades.add(familyFile);
        }
        return new Plan(List.copyOf(shards), List.copyOf(facades), anchorSeasonId, stateFile);
    }

    /**
     * Pubblica solo shard attivi localmente. La stagione corrente e' sempre attiva; una storica
     * viene attivata quando era gia' presente o quando lo stato la abilita esplicitamente.
     */
    public static PublishStats publishShards(Plan plan) throws IOException {
        ShardState state = ShardState.load(plan.stateFile());
        int written = 0;
        int unchanged = 0;
        int skippedUnavailable = 0;
        for (Shard shard : plan.shards()) {
            if (!shard.localActive() || shard.target() == null) {
                skippedUnavailable++;
                continue;
            }
            Path siteRoot = shard.target().getParent().getParent().getParent();
            if (!shard.currentSeason() && !Files.isDirectory(siteRoot)) {
                skippedUnavailable++;
                continue;
            }
            Files.createDirectories(shard.target().getParent());
            String stateHash = state.fingerprint(shard.familyId(), shard.seasonId());
            if (Files.isRegularFile(shard.target())
                    && shard.sha256().equals(stateHash)
                    && Files.mismatch(shard.stagedFile(), shard.target()) == -1L) {
                unchanged++;
            } else if (Files.isRegularFile(shard.target())
                    && Files.mismatch(shard.stagedFile(), shard.target()) == -1L) {
                unchanged++;
            } else {
                Path temp = shard.target().resolveSibling("." + shard.target().getFileName()
                        + ".recordsnext-" + UUID.randomUUID() + ".tmp");
                Files.copy(shard.stagedFile(), temp, StandardCopyOption.REPLACE_EXISTING);
                moveReplace(temp, shard.target());
                written++;
            }
            state.setLocalEnabled(shard.seasonId(), true);
            state.setFingerprint(shard.familyId(), shard.seasonId(), shard.sha256());
            state.setBytes(shard.familyId(), shard.seasonId(), shard.bytes());
        }
        state.setLastAnchor(plan.anchorSeasonId());
        state.save(plan.stateFile());
        return new PublishStats(written, unchanged, skippedUnavailable);
    }

    /** Abilita una stagione storica nelle facade online dopo che i relativi shard sono stati caricati sul web. */
    public static void setOnlineAvailability(Path stateFile, String seasonId, boolean available) throws IOException {
        ShardState state = ShardState.load(stateFile);
        state.setOnlineEnabled(seasonId, available);
        state.save(stateFile);
    }

    /** Abilita/disabilita la materializzazione locale di una stagione storica. */
    public static void setLocalAvailability(Path stateFile, String seasonId, boolean available) throws IOException {
        ShardState state = ShardState.load(stateFile);
        state.setLocalEnabled(seasonId, available);
        state.save(stateFile);
    }

    private static List<Shard> shardsForFamily(List<Shard> shards, String familyId) {
        return shards.stream().filter(s -> s.familyId().equals(familyId))
                .sorted(Comparator.comparing(Shard::seasonId)).toList();
    }

    private static Map<String, Map<String, List<Object>>> splitBySeason(Map<String, Object> root, FamilySpec spec)
            throws IOException {
        Map<String, Map<String, List<Object>>> out = new LinkedHashMap<>();
        for (String field : spec.shardedFields()) {
            Object raw = root.get(field);
            if (!(raw instanceof List<?> list)) {
                throw new IOException("Campo array mancante in " + spec.fileName() + ": " + field);
            }
            for (int index = 0; index < list.size(); index++) {
                Object item = list.get(index);
                if (!(item instanceof Map<?, ?> map)) {
                    throw new IOException("Riga non-oggetto in " + spec.fileName() + ": " + field);
                }
                String seasonId = text(map.get(spec.seasonField()));
                if (seasonId.isBlank()) {
                    throw new IOException("Stagione mancante in " + spec.fileName() + ": " + field + "[" + index + "]");
                }
                Map<String, List<Object>> fields = out.computeIfAbsent(seasonId, ignored -> new LinkedHashMap<>());
                List<Object> rows = fields.computeIfAbsent(field, ignored -> new ArrayList<>());
                rows.add(List.of(new RawNumber(Integer.toString(index)), item));
            }
        }
        for (Map<String, List<Object>> fields : out.values()) {
            for (String field : spec.shardedFields()) fields.computeIfAbsent(field, ignored -> new ArrayList<>());
        }
        return out;
    }

    private static String renderShard(FamilySpec spec, Map<String, List<Object>> indexedRows) {
        return "(function(){var q=window.__recordsNextShardQueue=window.__recordsNextShardQueue||{};"
                + "var f=q[\"" + spec.id() + "\"]=q[\"" + spec.id() + "\"]||{};var d="
                + Json.write(indexedRows) + ";"
                + spec.shardedFields().stream()
                    .map(field -> "(f." + field + "=f." + field + "||[]).push.apply(f." + field + ",d." + field + ");")
                    .reduce("", String::concat)
                + "})();\n";
    }

    private static String renderFlatFacade(
            FamilySpec spec,
            Map<String, Object> facadeRoot,
            List<FlatShard> shards) {

        List<String> files = shards.stream()
                .sorted(Comparator.comparing(FlatShard::seasonId))
                .map(shard -> shard.file().getFileName().toString())
                .toList();

        String fields = Json.write(spec.shardedFields());
        String finalizer = "(function(){var q=window.__recordsNextShardQueue||{},f=q[\"" + spec.id()
                + "\"]||{},r=" + spec.globalName() + ",fields=" + fields
                + ";for(var x=0;x<fields.length;x++){var k=fields[x],a=f[k]||[];"
                + "a.sort(function(A,B){return A[0]-B[0]});r[k]=a.map(function(v){return v[1]});}"
                + "if(q[\"" + spec.id() + "\"])delete q[\"" + spec.id() + "\"];})();";
        String finalizerHtml = finalizer.replace("\\", "\\\\").replace("'", "\\'");

        return spec.globalName() + "=" + Json.write(facadeRoot) + ";\n"
                + "(function(){var u=" + Json.write(files)
                + ",c=document.currentScript,b=(c&&c.src)?c.src.substring(0,c.src.lastIndexOf('/')+1):'';"
                + "if(document.readyState!=='loading')throw new Error('RecordsNext shard loader deve essere caricato durante il parsing HTML');"
                + "for(var i=0;i<u.length;i++){var s=String(b+u[i]).replace(/&/g,'&amp;').replace(/\"/g,'&quot;');"
                + "document.write('<script src=\"'+s+'\"><\\/script>');}"
                + "document.write('<script>" + finalizerHtml + "<\\/script>');})();\n";
    }

    private static String renderFacade(FamilySpec spec, Map<String, Object> facadeRoot, List<Shard> shards)
            throws IOException {
        List<String> online = new ArrayList<>();
        List<String> local = new ArrayList<>();
        for (Shard shard : shards) {
            if (shard.onlineActive()) {
                if (shard.onlineUrl() == null || shard.onlineUrl().isBlank()) {
                    throw new IOException("URL online mancante per stagione online " + shard.seasonId() + " / " + spec.id());
                }
                online.add(shard.onlineUrl());
            }
            if (shard.localActive()) {
                if (shard.localUrl() != null && !shard.localUrl().isBlank()) local.add(shard.localUrl());
                else if (shard.onlineUrl() != null && !shard.onlineUrl().isBlank()) local.add(shard.onlineUrl());
            }
        }
        String fields = Json.write(spec.shardedFields());
        String finalizer = "(function(){var q=window.__recordsNextShardQueue||{},f=q[\"" + spec.id()
                + "\"]||{},r=" + spec.globalName() + ",fields=" + fields
                + ";for(var x=0;x<fields.length;x++){var k=fields[x],a=f[k]||[];"
                + "a.sort(function(A,B){return A[0]-B[0]});r[k]=a.map(function(v){return v[1]});}"
                + "if(q[\"" + spec.id() + "\"])delete q[\"" + spec.id() + "\"];})();";
        String finalizerHtml = finalizer.replace("\\", "\\\\").replace("'", "\\'");
        return spec.globalName() + "=" + Json.write(facadeRoot) + ";\n"
                + "(function(){var o=" + Json.write(online) + ",l=" + Json.write(local)
                + ",u=(location.protocol==='file:'?l:o);"
                + "if(document.readyState!=='loading')throw new Error('RecordsNext shard loader deve essere caricato durante il parsing HTML');"
                + "for(var i=0;i<u.length;i++){var s=String(u[i]).replace(/&/g,'&amp;').replace(/\"/g,'&quot;');"
                + "document.write('<script src=\"'+s+'\"><\\/script>');}"
                + "document.write('<script>" + finalizerHtml + "<\\/script>');})();\n";
    }

    private static Map<String, SeasonRoute> readRoutes(Map<String, Object> core) throws IOException {
        Object raw = core.get("seasons");
        if (!(raw instanceof List<?> list)) throw new IOException("Core senza seasons");
        Map<String, SeasonRoute> routes = new LinkedHashMap<>();
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> map)) continue;
            String seasonId = text(map.get("seasonId"));
            if (seasonId.isBlank()) continue;
            String local = text(map.get("localSitePath"));
            String online = text(map.get("onlineSiteUrl"));
            boolean anchor = bool(map.get("isAnchor"));
            Path localPath = local.isBlank() ? null : Path.of(local).toAbsolutePath().normalize();
            routes.put(seasonId, new SeasonRoute(seasonId, localPath, blankToNull(online), anchor));
        }
        return routes;
    }

    private static Assignment readAssignment(Path file, String expectedGlobal) throws IOException {
        String source = Files.readString(file, StandardCharsets.UTF_8);
        if (!source.isEmpty() && source.charAt(0) == '\uFEFF') source = source.substring(1);
        String trimmed = source.trim();
        String prefix = expectedGlobal + " = ";
        if (!trimmed.startsWith(prefix)) {
            prefix = expectedGlobal + "=";
            if (!trimmed.startsWith(prefix)) throw new IOException("Prefisso JS inatteso: " + file);
        }
        if (!trimmed.endsWith(";")) throw new IOException("Terminatore JS mancante: " + file);
        String json = trimmed.substring(prefix.length(), trimmed.length() - 1).trim();
        Object parsed = new Json.Parser(json).parse();
        if (!(parsed instanceof Map<?, ?> map)) throw new IOException("Radice JSON non-oggetto: " + file);
        Map<String, Object> root = new LinkedHashMap<>();
        for (Map.Entry<?, ?> e : map.entrySet()) root.put(String.valueOf(e.getKey()), e.getValue());
        return new Assignment(expectedGlobal, root);
    }

    private static Map<String, Object> deepCopyMap(Map<String, Object> source) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : source.entrySet()) out.put(e.getKey(), deepCopy(e.getValue()));
        return out;
    }
    private static Object deepCopy(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> out = new LinkedHashMap<>();
            for (Map.Entry<?, ?> e : map.entrySet()) out.put(String.valueOf(e.getKey()), deepCopy(e.getValue()));
            return out;
        }
        if (value instanceof List<?> list) {
            List<Object> out = new ArrayList<>(list.size());
            for (Object item : list) out.add(deepCopy(item));
            return out;
        }
        return value;
    }

    private static String sha256(Path file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream in = Files.newInputStream(file)) {
                byte[] buffer = new byte[8192];
                for (int n; (n = in.read(buffer)) >= 0;) digest.update(buffer, 0, n);
            }
            StringBuilder out = new StringBuilder(64);
            for (byte b : digest.digest()) out.append(String.format("%02x", b & 0xff));
            return out.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 non disponibile", ex);
        }
    }

    private static String joinUrl(String base, String relative) {
        String b = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        return b + "/" + relative;
    }
    private static String blankToNull(String value) { return value == null || value.isBlank() ? null : value; }
    private static String text(Object value) { return value == null ? "" : String.valueOf(value); }
    private static boolean bool(Object value) {
        if (value instanceof Boolean b) return b;
        if (value instanceof Number n) return n.intValue() != 0;
        String s = text(value);
        return "1".equals(s) || "true".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s);
    }

    private static void moveReplace(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private record FamilySpec(String id, String fileName, String globalName, String seasonField, List<String> shardedFields) {}
    private record Assignment(String globalName, Map<String, Object> root) {}
    private record SeasonRoute(String seasonId, Path localSitePath, String onlineSiteUrl, boolean anchor) {}
    private record RawNumber(String value) {}

    public record FlatShard(String familyId, String seasonId, Path file, long bytes) {}
    public record FlatPlan(List<FlatShard> shards, List<Path> facades) {
        public long maxShardBytes() { return shards.stream().mapToLong(FlatShard::bytes).max().orElse(0L); }
        public long totalShardBytes() { return shards.stream().mapToLong(FlatShard::bytes).sum(); }
    }

    public record Shard(String familyId, String seasonId, Path stagedFile, Path target,
                        String onlineUrl, String localUrl, long bytes, String sha256,
                        boolean currentSeason, boolean localActive, boolean onlineActive) {}
    public record Plan(List<Shard> shards, List<Path> facades, String anchorSeasonId, Path stateFile) {
        public long totalShardBytes() { return shards.stream().mapToLong(Shard::bytes).sum(); }
        public long maxShardBytes() { return shards.stream().mapToLong(Shard::bytes).max().orElse(0L); }
        public int onlineSeasonCount() { return (int) shards.stream().filter(Shard::onlineActive).map(Shard::seasonId).distinct().count(); }
        public int localSeasonCount() { return (int) shards.stream().filter(Shard::localActive).map(Shard::seasonId).distinct().count(); }
    }
    public record PublishStats(int written, int unchanged, int skippedUnavailable) {}

    private static final class ShardState {
        private final Properties properties = new Properties();

        static ShardState load(Path file) throws IOException {
            ShardState state = new ShardState();
            if (Files.isRegularFile(file)) {
                try (InputStream in = Files.newInputStream(file)) { state.properties.load(in); }
            }
            return state;
        }
        boolean localEnabled(String season) { return Boolean.parseBoolean(properties.getProperty("season." + season + ".local", "false")); }
        boolean onlineEnabled(String season) { return Boolean.parseBoolean(properties.getProperty("season." + season + ".online", "false")); }
        String fingerprint(String family, String season) { return properties.getProperty("season." + season + ".sha256." + family, ""); }
        void setLocalEnabled(String season, boolean value) { properties.setProperty("season." + season + ".local", Boolean.toString(value)); }
        void setOnlineEnabled(String season, boolean value) { properties.setProperty("season." + season + ".online", Boolean.toString(value)); }
        void setFingerprint(String family, String season, String hash) { properties.setProperty("season." + season + ".sha256." + family, hash); }
        void setBytes(String family, String season, long bytes) { properties.setProperty("season." + season + ".bytes." + family, Long.toString(bytes)); }
        void setLastAnchor(String season) { properties.setProperty("lastAnchorSeason", season); }
        void save(Path file) throws IOException {
            Path parent = file.toAbsolutePath().normalize().getParent();
            if (parent != null) Files.createDirectories(parent);
            properties.setProperty("updatedAt", Instant.now().toString());
            Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
            try (OutputStream out = Files.newOutputStream(tmp)) {
                properties.store(out, "RecordsNext season shard state");
            }
            moveReplace(tmp, file);
        }
    }

    /** Piccolo parser/writer JSON locale, per non aggiungere dipendenze al runtime. */
    private static final class Json {
        static String write(Object value) { StringBuilder out = new StringBuilder(); write(value, out); return out.toString(); }
        private static void write(Object value, StringBuilder out) {
            if (value == null) { out.append("null"); return; }
            if (value instanceof RawNumber n) { out.append(n.value()); return; }
            if (value instanceof String s) { string(s, out); return; }
            if (value instanceof Number || value instanceof Boolean) { out.append(value); return; }
            if (value instanceof Map<?, ?> map) {
                out.append('{'); boolean first = true;
                for (Map.Entry<?, ?> e : map.entrySet()) {
                    if (!first) out.append(','); first = false; string(String.valueOf(e.getKey()), out); out.append(':'); write(e.getValue(), out);
                }
                out.append('}'); return;
            }
            if (value instanceof Iterable<?> iterable) {
                out.append('['); boolean first = true;
                for (Object item : iterable) { if (!first) out.append(','); first = false; write(item, out); }
                out.append(']'); return;
            }
            string(String.valueOf(value), out);
        }
        private static void string(String s, StringBuilder out) {
            out.append('"');
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                switch (c) {
                    case '"' -> out.append("\\\"");
                    case '\\' -> out.append("\\\\");
                    case '\b' -> out.append("\\b");
                    case '\f' -> out.append("\\f");
                    case '\n' -> out.append("\\n");
                    case '\r' -> out.append("\\r");
                    case '\t' -> out.append("\\t");
                    default -> { if (c < 0x20) out.append(String.format("\\u%04x", (int)c)); else out.append(c); }
                }
            }
            out.append('"');
        }

        private static final class Parser {
            private final String text; private int index;
            Parser(String text) { this.text = text; }
            Object parse() { skip(); Object v = value(); skip(); if (index != text.length()) fail(); return v; }
            private Object value() {
                skip(); if (index >= text.length()) fail();
                return switch (text.charAt(index)) {
                    case '{' -> object(); case '[' -> array(); case '"' -> string();
                    case 't' -> literal("true", Boolean.TRUE); case 'f' -> literal("false", Boolean.FALSE);
                    case 'n' -> literal("null", null); default -> number();
                };
            }
            private Map<String,Object> object() {
                expect('{'); Map<String,Object> out = new LinkedHashMap<>(); skip();
                if (peek('}')) { index++; return out; }
                while (true) { String k = string(); expect(':'); out.put(k, value()); skip(); if (peek('}')) { index++; return out; } expect(','); }
            }
            private List<Object> array() {
                expect('['); List<Object> out = new ArrayList<>(); skip();
                if (peek(']')) { index++; return out; }
                while (true) { out.add(value()); skip(); if (peek(']')) { index++; return out; } expect(','); }
            }
            private String string() {
                expect('"'); StringBuilder out = new StringBuilder();
                while (index < text.length()) {
                    char c = text.charAt(index++); if (c == '"') return out.toString();
                    if (c != '\\') { out.append(c); continue; }
                    if (index >= text.length()) fail(); char e = text.charAt(index++);
                    switch (e) {
                        case '"' -> out.append('"'); case '\\' -> out.append('\\'); case '/' -> out.append('/');
                        case 'b' -> out.append('\b'); case 'f' -> out.append('\f'); case 'n' -> out.append('\n');
                        case 'r' -> out.append('\r'); case 't' -> out.append('\t');
                        case 'u' -> { if (index + 4 > text.length()) fail(); out.append((char)Integer.parseInt(text.substring(index,index+4),16)); index += 4; }
                        default -> fail();
                    }
                }
                fail(); return "";
            }
            private Object number() {
                int start = index; if (peek('-')) index++;
                while (index < text.length() && Character.isDigit(text.charAt(index))) index++;
                if (index < text.length() && text.charAt(index) == '.') { index++; while (index < text.length() && Character.isDigit(text.charAt(index))) index++; }
                if (index < text.length() && (text.charAt(index) == 'e' || text.charAt(index) == 'E')) {
                    index++; if (index < text.length() && (text.charAt(index) == '+' || text.charAt(index) == '-')) index++;
                    while (index < text.length() && Character.isDigit(text.charAt(index))) index++;
                }
                String n = text.substring(start,index); try { return new BigDecimal(n); } catch (Exception ex) { fail(); return null; }
            }
            private Object literal(String token,Object value) { if (!text.startsWith(token,index)) fail(); index += token.length(); return value; }
            private void skip() { while (index < text.length() && Character.isWhitespace(text.charAt(index))) index++; }
            private boolean peek(char c) { skip(); return index < text.length() && text.charAt(index) == c; }
            private void expect(char c) { skip(); if (index >= text.length() || text.charAt(index) != c) fail(); index++; }
            private void fail() { throw new IllegalArgumentException("JSON non valido a " + index); }
        }
    }
}
```

## src/test/java/it/alterlega/recordsnext/app/RecordsNextFlatStagingV46SmokeTest.java

SHA-256: `0ffee124e82bc6087fa339f682b341e120f9977f3976e1b0d029359c1cb6e9c9`

```java
package it.alterlega.recordsnext.app;

import it.alterlega.recordsnext.app.output.SeasonFamilyShardPublisher;
import it.alterlega.recordsnext.app.topflop.TopFlopPublicViewsStageExporter;
import it.alterlega.recordsnext.app.topflop.TopFlopViewsPublication;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Regressione sull'errore dell'11/11/2026: Classics cumulativo di 8.398.766
 * byte rifiutato prima della suddivisione nello staging senza publish.
 * Non accede a database, siti o configurazioni personali.
 */
public final class RecordsNextFlatStagingV46SmokeTest {
    private static final long LIMIT = TopFlopPublicViewsStageExporter.LIMIT_BYTES;
    private RecordsNextFlatStagingV46SmokeTest() { }

    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("recordsnext-v46-shard-check-");
        try {
            Path js = Files.createDirectories(root.resolve("staging/js"));
            List<String> seasons = new ArrayList<>();
            StringBuilder cumulative = new StringBuilder("window.fcmRecordsNextClassics = {\"seasonAggregates\":[");
            boolean first = true;
            String blob = "X".repeat(1750);
            for (int year = 2006; year <= 2025; year++) {
                String season = year + "_" + (year + 1);
                seasons.add(season);
                for (int index = 0; index < 250; index++) {
                    if (!first) cumulative.append(',');
                    first = false;
                    cumulative.append("{\"stagione\":\"").append(season)
                            .append("\",\"recordId\":").append(index)
                            .append(",\"payload\":\"").append(blob).append("\"}");
                }
            }
            cumulative.append("]};\n");
            Path classics = js.resolve("fcmRecordsNext_Classics.js");
            Files.writeString(classics, cumulative, StandardCharsets.UTF_8);
            long original = Files.size(classics);
            require(original > 8_398_766, "fixture non rappresentativa: " + original);
            require(original > LIMIT, "raw sotto il limite");

            // La stessa sequenza che ora esegue la pipeline prima del controllo.
            SeasonFamilyShardPublisher.FlatPlan plan =
                    SeasonFamilyShardPublisher.prepareFlat(js, LIMIT);
            int validated = TopFlopViewsPublication.validateStaging(js);
            require(plan.shards().size() == 20, "shard inattesi: " + plan.shards().size());
            require(validated == 21, "numero JS inatteso: " + validated);
            require(Files.size(classics) < LIMIT, "facade troppo grande");
            for (Path file : plan.facades()) {
                require(Files.size(file) <= LIMIT, "facade fuori limite " + file);
            }
            for (var item : plan.shards()) {
                require(item.bytes() > 0 && item.bytes() <= LIMIT, "shard fuori limite: " + item.file());
                require(Files.readString(item.file()).split("\\\"payload\\\"", -1).length - 1 == 250,
                        "contenuto shard alterato: " + item.file());
            }
            require(Files.readString(classics).contains("fcmRecordsNext_Classics.2006_2007.js"),
                    "facade non collega i dati storici");

            // Un file non suddivisibile non puo' essere pubblicato per errore.
            Path oversize = js.resolve("fcmRecordsNext_Unknown.js");
            Files.writeString(oversize, "Z".repeat((int)LIMIT + 1), StandardCharsets.UTF_8);
            boolean blocked = false;
            try {
                TopFlopViewsPublication.validateStaging(js);
            } catch (IOException expected) {
                blocked = expected.getMessage().contains("1.500.000");
            }
            require(blocked, "limite totale aggirato su file non shardabile");
            Files.delete(oversize);

            // I singoli shard sopra il limite vengono sempre respinti.
            Path oversizedSingleSeason = root.resolve("oversized-single/js");
            Files.createDirectories(oversizedSingleSeason);
            Files.writeString(oversizedSingleSeason.resolve("fcmRecordsNext_Classics.js"),
                    "window.fcmRecordsNextClassics = {\"seasonAggregates\":[{\"stagione\":\"2025_2026\",\"payload\":\""
                    + "a".repeat((int)LIMIT) + "\"}]};\n");
            blocked = false;
            try {
                SeasonFamilyShardPublisher.prepareFlat(oversizedSingleSeason, LIMIT);
            } catch (IOException expected) {
                blocked = expected.getMessage().contains("oltre il limite");
            }
            require(blocked, "shard troppo grande non bloccato");
            System.out.println("RN4_FLAT_STAGING_V46_OK original=" + original
                    + " shards=" + plan.shards().size() + " validated=" + validated
                    + " max=" + plan.maxShardBytes() + " limit=" + LIMIT);
        } finally {
            // Togliamo solo il fixture temporaneo creato da questo test.
            try (var walk = Files.walk(root)) {
                for (Path path : walk.sorted(java.util.Comparator.reverseOrder()).toList()) {
                    Files.deleteIfExists(path);
                }
            }
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
```

## tools/Start-RecordsNext4-Test-v46.ps1

SHA-256: `a39a8e63b034ef250f9c797f0be3199410ef028b73d58306405e355147764e74`

```powershell
param(
    [string]$ProjectRoot = 'D:\DEV_APPS\RecordsNext4.0_TEST_v37',
    [string]$UcanAccessRoot = 'D:\DEV_APPS\RecordsNext2.0\release\RecordsNext_3.2.0_FULL\payload\runtime\ucanaccess'
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$ProjectRoot = [System.IO.Path]::GetFullPath($ProjectRoot)
if ($ProjectRoot.TrimEnd('\') -ine 'D:\DEV_APPS\RecordsNext4.0_TEST_v37') {
    throw 'Per sicurezza questo launcher avvia soltanto RecordsNext4.0_TEST_v37.'
}
$jar = Join-Path $ProjectRoot 'target\RecordsNext.jar'
if (-not (Test-Path -LiteralPath $jar -PathType Leaf)) {throw "JAR di sviluppo non trovato: $jar"}
if (-not (Test-Path -LiteralPath $UcanAccessRoot -PathType Container)) {throw "Runtime UCanAccess non trovato: $UcanAccessRoot"}
$dependencies = @(Get-ChildItem -LiteralPath $UcanAccessRoot -File -Filter '*.jar' -Recurse)
if (-not (@($dependencies | Where-Object { $_.Name -eq 'ucanaccess-2.0.9.5.jar' }).Count -gt 0)) {
    throw 'ucanaccess-2.0.9.5.jar assente nel runtime selezionato.'
}
$cp = (@($jar) + @($dependencies | ForEach-Object { $_.FullName })) -join ';'
Write-Host "RecordsNext 4.0 TEST -> $jar"
Write-Host "UCanAccess -> $UcanAccessRoot ($($dependencies.Count) jar)"
Push-Location $ProjectRoot
try {
    & java -cp $cp it.alterlega.recordsnext.gui.RecordsNext2Dashboard
    if ($LASTEXITCODE -ne 0) {throw "GUI terminata con codice $LASTEXITCODE"}
} finally { Pop-Location }
```

## tools/Publish-RecordsNext4-v46-GitHub.ps1

SHA-256: `84441b31d12c7c80e0e3517294f36f93ddf8ab2cbf8353951907d9b79e859d0b`

```powershell
param(
    [string]$ProjectRoot = 'D:\DEV_APPS\RecordsNext4.0',
    [switch]$Publish
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$ProjectRoot = [System.IO.Path]::GetFullPath($ProjectRoot)
if ($ProjectRoot.TrimEnd('\') -ine 'D:\DEV_APPS\RecordsNext4.0') {
    throw 'Pubblicazione Git consentita soltanto dal worktree 4.0 ufficiale.'
}
if (-not (Test-Path -LiteralPath (Join-Path $ProjectRoot '.git'))) {
    throw 'Worktree Git 4.0 non trovato.'
}
Push-Location $ProjectRoot
try {
    $branch = (& git branch --show-current).Trim()
    if ($LASTEXITCODE -ne 0 -or $branch -ne 'feature/recordsnext4-players') {
        throw "Ramo inatteso: $branch. Usare solo feature/recordsnext4-players; non toccare main o 3.2."
    }
    $remote = (& git remote get-url origin).Trim()
    if ($LASTEXITCODE -ne 0 -or $remote -notmatch '(?i)github\.com[:/]mauz79/RecordsNext2(\.git)?$') {
        throw "Remote origin inatteso: $remote"
    }
    if ((& git diff --cached --name-only).Count -ne 0) {
        throw 'Esistono cambiamenti gia staged: verificare e svuotare lo staging Git prima di procedere.'
    }
    $paths = @(
        'src/main/java/it/alterlega/recordsnext/app/RecordsNextPipeline.java',
        'src/main/java/it/alterlega/recordsnext/app/output/SeasonFamilyShardPublisher.java',
        'src/test/java/it/alterlega/recordsnext/app/RecordsNextFlatStagingV46SmokeTest.java',
        'README.md', 'INSTALL.md', 'CHANGELOG.md',
        'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v38.md',
        'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v38.md',
        'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v31.md',
        'docs/VERIFICA_STAGING_JS_v46.md',
        'tools/Create-RecordsNext4WorkingCodeMd-v38.py',
        'tools/Start-RecordsNext4-Test-v46.ps1',
        'tools/Publish-RecordsNext4-v46-GitHub.ps1'
    )
    foreach ($relative in $paths) {
        if (-not (Test-Path -LiteralPath (Join-Path $ProjectRoot $relative.Replace('/','\')) -PathType Leaf)) {
            throw "File aggiornato mancante: $relative. Promuovere prima la 4.0 completa e collaudata dal TEST."
        }
    }
    Write-Host "GitHub preflight: $branch -> $remote"
    Write-Host 'File di codice e documentazione destinati al commit:'
    $paths | ForEach-Object { Write-Host "  $_" }
    if (-not $Publish) {
        Write-Host 'SOLO VERIFICA: nessun git add, commit o push. Per pubblicare usare -Publish dopo il collaudo.'
        return
    }
    & git add -- @paths
    if ($LASTEXITCODE -ne 0) {throw 'git add fallito'}
    & git diff --cached --check
    if ($LASTEXITCODE -ne 0) {throw 'git diff --check fallito'}
    & git diff --cached --stat
    if ($LASTEXITCODE -ne 0) {throw 'git diff --stat fallito'}
    & git commit -m 'RecordsNext4: fix flat JS staging limit and update docs (v46)'
    if ($LASTEXITCODE -ne 0) {throw 'git commit fallito'}
    & git push -u origin $branch
    if ($LASTEXITCODE -ne 0) {throw 'git push fallito: verificare autenticazione GitHub'}
    Write-Host "PUSH GITHUB COMPLETATO: $remote / $branch"
} finally {Pop-Location}
```

## docs/GITHUB_PUBBLICAZIONE_v46.md

SHA-256: `5ab1a9c710d71cead659ddc8270dde15e7eb8b4f40156be85066cd1de8bd4be4`

```markdown
# GitHub — aggiornamento RecordsNext 4.0 v46

**Non dichiarare Github aggiornato** finche' `git push` non ha restituito un successo confermato. Il connettore GitHub della sessione e' disabilitato dall'amministratore e l'ambiente di sviluppo non puo' raggiungere `github.com`; nessun commit o push remoto e' stato eseguito qui.

Repository destinatario: `mauz79/RecordsNext2`. Ramo di lavoro previsto: `feature/recordsnext4-players` (non `main`). La 3.2 operativa deve restare intatta.

1. Collaudare v46 nella copia TEST fino alla generazione JS completa, con `publish=false` e file fisici <= 1.500.000 byte.
2. Integrare le modifiche cumulative 4.0 nella **vera** directory Git `D:\DEV_APPS\RecordsNext4.0`. Non copiare il contenuto del TEST in maniera indiscriminata: contiene SQLite e dati personali; seguire il consolidamento del codice e dei documenti soltanto.
3. Eseguire `tools/Publish-RecordsNext4-v46-GitHub.ps1` senza parametri per il preflight. Rifiuta branch non `feature/recordsnext4-players`, remote diverso, staged files preesistenti e file mancanti.
4. Dopo verifica, eseguire lo stesso script con `-Publish`: stage dei soli file esplicitamente elencati, `git diff --cached --check`, commit e push. **Non** aggiunge database, log, siti, dati personali, config identita' o release 3.2.
5. Annotare commit SHA e confermare che compaia nel remoto.

Il pacchetto v46 installabile e' limitato a `RecordsNext4.0_TEST_v37` proprio per non mescolare una versione 4.0 ancora in collaudo con la release storica 3.2 pubblicata.
```

## README.md

SHA-256: `c2c0e9aecd959c5294f8df2476f1b242422f4ddaa1d57261a44b2318a79f19a5`

```markdown
# RecordsNext 4.0 — ramo di sviluppo Renewo (copia TEST)

> **Stato 11/11/2026:** il codice 4.0 e' ancora in collaudo e **non** sostituisce la release pubblica 3.2.0 descritta nel README originale sottostante. Il repository principale non va aggiornato con una release 4.0 non certificata. La v46 corregge la generazione senza pubblicazione: il cumulativo `fcmRecordsNext_Classics.js` (8.398.766 byte nel log Windows) viene trasformato in facade + shard 3.2 **prima** del controllo fisico da 1.500.000 byte per JS.

## Percorso operativo 4.0 TEST

- Copia di prova: `D:\DEV_APPS\RecordsNext4.0_TEST_v37` (base v45b installata).
- GUI globale: **Completa/Consolidata** nella barra inferiore; nessuna seconda elaborazione nella famiglia Top & Flop.
- Output preesistenti 3.2: **inalterati nei dati e nei nomi pubblici**; facade + shard flat accanto al file principale nella `js` dello staging/sito.
- Output aggiuntivi 4.0: Top & Flop, `History` per identita' approvate, rendimento reale e per fantasquadra/competizione con cutoff storico.
- Identita' calciatori: approvazione esplicita iniziale e decisioni persistenti (`config/player-identities.json`); nessuna fusione automatica o modifica degli FCA/FCM. Il motore squadre/competizioni rimane quello della 3.2.
- Soglia di pubblicazione: **1.500.000 byte reali** per ciascun `.js` (non 1536 KiB); lo staging senza publish applica lo stesso sharding multisito.
- Per collaudare l'importazione FCM/FCA su Windows e' richiesto Java 21 con UCanAccess 2.0.9.5 e dipendenze gia' presenti nel runtime 3.2, in sola lettura.
- Verifica tecnica: [diario v38](docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v38.md), [checklist JS v46](docs/VERIFICA_STAGING_JS_v46.md), [INSTALL](INSTALL.md).
- Non pubblicare sui siti reali ne' dichiarare conclusa la 4.0 prima del collaudo completo dell'esportazione e del rollback.

---

# RecordsNext 3.2.0

## RecordsNext 3.2.0

La 3.2.0 completa la modularizzazione dei record e porta il catalogo configurabile a copertura completa delle famiglie RecordsNext.

Principali novita:

- catalogo modulare completo: 94 record/figli complessivi;
- 93 record configurabili singolarmente piu il Culometro opzionale;
- dipendenze preflight valutate a livello di singolo record;
- gestione corretta del Fattore Campo come capability dei Modificatori;
- Culometro esteso con gli eventi `HOME_FIELD_DECISIVE`;
- dettaglio Culometro con frequenza storica evento, frequenza della configurazione, impatto e contributo;
- Manifest coerente con selezione, dipendenze ed esito del preflight;
- mantenuta l'architettura multisito 3.1 e la pubblicazione tramite shard stagionali flat introdotta in 3.1.1;
- collaudi semantici completati sulle famiglie Classici, Serie, RU, Modificatori, Soglie/Fortuna e Culometro;
- collaudo operativo finale in modalita Completa riuscito con 72 record selezionati, 72 eseguibili, 72 completi, 0 parziali e 0 saltati.
**RecordsNext by mauz79** è il generatore di record e statistiche storiche per leghe gestite con Fantacalcio Manager.

RecordsNext legge i dati delle stagioni dai file FCM/FCA, mantiene uno storico normalizzato nel proprio database e genera file JavaScript pronti per essere usati dal sito della lega. La 3.1 introduce la pubblicazione multistagione/multisito: ogni vecchio sito può essere riallineato con i record disponibili **fino a quella stagione**, senza ricevere dati futuri.

![Dashboard RecordsNext 3.1](docs/screenshots/01-dashboard.png)

## Indice

- [Cosa fa RecordsNext](#cosa-fa-recordsnext)
- [Installazione](#installazione)
- [Concetti fondamentali](#concetti-fondamentali)
- [Prima configurazione](#prima-configurazione)
- [Schermate e funzioni della GUI](#schermate-e-funzioni-della-gui)
- [Famiglie di record](#famiglie-di-record)
- [Culometro](#culometro)
- [Elaborazione completa e consolidata](#elaborazione-completa-e-consolidata)
- [Pubblicazione](#pubblicazione)
- [Output JavaScript](#output-javascript)
- [Calendari DataA](#calendari-dataa)
- [Tabellini storici](#tabellini-storici)
- [Flusso d'uso consigliato](#flusso-duso-consigliato)
- [Stato della release 3.2](#stato-della-release-32)

## Cosa fa RecordsNext

RecordsNext centralizza e rende riutilizzabile lo storico della lega. In particolare:

- importa le stagioni gestite da FCM/FCA;
- mantiene identità canoniche di squadre e competizioni anche quando i nomi cambiano negli anni;
- usa calendari DataA canonici interni;
- genera record classici e serie statistiche;
- genera statistiche sulle Riserve d'Ufficio;
- genera record e serie sui Modificatori;
- genera Soglie e indicatori di Fortuna;
- genera il Culometro, quando attivato;
- esporta un dataset partita-per-partita in `fcmRecordsNext_Matches.js`;
- collega i record ai tabellini storici quando disponibili;
- pubblica i JavaScript nel sito FCM corrente;
- può riallineare contemporaneamente più siti storici.

Il principio architetturale della 3.1 è:

> **FCM, FCA e DataA definiscono lo storico. I siti sono destinazioni opzionali di pubblicazione.**

Un vecchio sito quindi non deve esistere perché una stagione resti valida nello storico di RecordsNext.

## Installazione

La release pubblica 3.2 viene distribuita come **installazione pulita** tramite:

`RecordsNext_3.2.0_SETUP.exe`

Requisiti:

- Windows;
- Java 21 o superiore;
- Fantacalcio Manager;
- file FCM/FCA delle stagioni da importare.

Durante il setup viene sempre mostrata la cartella di destinazione. La convenzione consigliata è:

`<cartella FCM>\plugin\RecordsNext`

L'installazione include programma, runtime necessario, visualizzatori e archivio calendari DataA 1991-2026.

Non include:

- database personali;
- file FCM/FCA dell'utente;
- configurazioni della lega;
- pubblicazioni automatiche nei siti.

Per i dettagli vedere [INSTALL.md](INSTALL.md).

## Concetti fondamentali

### Stagione gestita

Una stagione **Gestita** è alimentata dai file FCM/FCA.

Richiede:

- FCM;
- FCA.

Può avere, ma non deve avere:

- sito locale;
- sito online.

Il sito locale serve solo come destinazione di pubblicazione. La sua cartella `js` deriva sempre da:

`<cartella sito>\js`

### Stagione manuale

Una stagione **Manuale** serve a rappresentare una stagione che deve esistere nello storico/configurazione ma non è importabile tramite FCM/FCA.

Richiede soltanto:

- anni stagione nel formato `AAAA/AAAA`;
- numero stagione.

### Stagione corrente

La stagione corrente è l'ancora operativa del sito. Nell'uso normale RecordsNext pubblica solo nel sito associato a questa stagione.

### Sito locale

È la root del sito FCM sul disco. Esempio:

`E:\fantacalcio\Lega2026`

RecordsNext pubblicherà i JS in:

`E:\fantacalcio\Lega2026\js`

### Sito online

Serve a costruire collegamenti web, in particolare verso i tabellini.

RecordsNext normalizza l'URL:

- converte `\` in `/`;
- se il protocollo manca aggiunge `http://`;
- conserva un eventuale `https://` inserito esplicitamente;
- rimuove slash finali inutili.

Esempio:

`www.example.org\lega2026`

diventa:

`http://www.example.org/lega2026`

## Prima configurazione

Aprire **Configurazione stagioni**.

![Configurazione stagione](docs/screenshots/02-configurazione-stagione.png)

Per ogni stagione gestita indicare FCM e FCA. Il sito locale può essere lasciato vuoto se quella stagione deve contribuire allo storico ma non deve ricevere file pubblicati.

Il nome lega può essere lasciato vuoto: RecordsNext prova a ricavarlo dal primo FCM gestito. L'identificativo tecnico della lega resta interno e non viene richiesto all'utente.

La GUI mostra anche lo stato del calendario e dei tabellini disponibili.

## Schermate e funzioni della GUI

### Panoramica

La dashboard riassume:

- lega corrente;
- numero di stagioni configurate;
- famiglie abilitate;
- stato di Soglie/Fortuna;
- stato del Culometro;
- modalità di elaborazione;
- opzioni di pubblicazione.

### Configurazione stagioni

Serve per:

- aggiungere stagioni gestite;
- aggiungere stagioni manuali;
- modificare sorgenti e siti;
- scegliere la stagione corrente;
- verificare calendario e tabellini;
- eliminare una stagione con pulizia coerente dei dati collegati.

La cancellazione di una stagione non è una semplice rimozione dalla GUI: RecordsNext elimina i dati stagionali collegati e riancora, quando necessario, le identità canoniche condivise.

### Famiglie record

La pagina consente di attivare o disattivare intere famiglie e, dove previsto, i singoli sottorecord.

![Famiglia Classici](docs/screenshots/03-famiglia-classici.png)

### Modificatori

La sezione Modificatori permette di configurare i nomi dei modificatori personalizzati e scegliere quali elaborazioni produrre, tra cui massimo, totale, media, utilizzi e relative serie quando disponibili.

### Soglie e Fortuna

Questa pagina abilita le elaborazioni basate sulle soglie dei punteggi e gli indicatori di fortuna.

![Soglie e Fortuna](docs/screenshots/04-soglie-fortuna.png)

### Debug / Pubblicazione

Questa pagina raccoglie le funzioni operative e di verifica, comprese le azioni di pubblicazione corrente e multisito.

![Pubblicazione multisito](docs/screenshots/06-pubblicazione-multisito.png)

## Famiglie di record

RecordsNext 3.1 produce le seguenti famiglie canoniche.

### Classici

Record tradizionali ricavati dalle partite e dai punteggi storici. La selezione è granulare: è possibile abilitare solo i record che interessano.

### Serie

Serie positive e negative ricavate dalla successione delle partite e dai risultati. Le serie relative ai modificatori seguono la configurazione della famiglia Modificatori.

### Riserve d'Ufficio

Statistiche e record relativi all'impiego delle RU. La famiglia mantiene una propria configurazione granulare.

### Modificatori

Record aggregati e serie sui modificatori disponibili nei dati FCM/FCA. I modificatori personalizzati possono essere rinominati in GUI.

### Soglie e Fortuna

Raggruppa elaborazioni che confrontano punteggi, esiti e soglie per evidenziare situazioni statisticamente favorevoli o sfavorevoli.

## Culometro

Il Culometro è un'elaborazione separata e opzionale.

![Configurazione Culometro](docs/screenshots/05-culometro-configurazione.png)

La configurazione permette di scegliere quali componenti partecipano al calcolo e con quale peso. I dettagli esportati comprendono, per ogni evento disponibile, i valori necessari alla spiegazione del risultato, comprese frequenze storiche e impatto.

Il Culometro può risultare **parziale** se mancano dipendenze opzionali. Questo non equivale a un errore dell'intera elaborazione.

## Elaborazione completa e consolidata

### Completa

Ricostruisce i dati gestiti e gli archivi derivati. È la modalità da usare:

- alla prima importazione;
- dopo modifiche strutturali;
- dopo cambi importanti nella configurazione;
- quando si vuole ricostruire integralmente lo storico.

### Consolidata

È la modalità ordinaria dopo un aggiornamento del sito/FCM. Riutilizza ciò che non è cambiato e aggiorna ciò che serve.

È la modalità consigliata per l'uso giornata per giornata.

## Pubblicazione

RecordsNext 3.1 distingue due operazioni.

### Pubblica nel sito della stagione corrente al termine

È l'opzione normale.

Usarla quando si aggiorna la stagione corrente dopo una nuova giornata. Al termine dell'elaborazione RecordsNext pubblica i JS nel solo sito configurato come corrente.

### Pubblica i siti delle stagioni selezionate

È un'operazione di manutenzione/riallineamento storico.

Usarla quando:

- cambia una logica globale;
- vengono corrette identità o competizioni;
- viene introdotto un nuovo dataset;
- si vuole riallineare lo storico dopo un aggiornamento di RecordsNext.

Le checkbox delle stagioni determinano i target da pubblicare.

La regola fondamentale è il **cutoff storico**: ogni sito riceve soltanto le stagioni con `sort_order` minore o uguale al proprio.

Esempio:

- sito 2024/25 → dati fino al 2024/25;
- sito 2025/26 → dati fino al 2025/26;
- sito 2026/27 → dati fino al 2026/27.

Un sito storico non può quindi ricevere squadre, competizioni o risultati futuri.

## Output JavaScript

RecordsNext pubblica nove dataset canonici:

| File | Contenuto |
| --- | --- |
| `fcmRecordsNext_Core.js` | metadati, stagioni, squadre, competizioni, identità canoniche e mapping |
| `fcmRecordsNext_Classics.js` | record classici |
| `fcmRecordsNext_Series.js` | serie statistiche |
| `fcmRecordsNext_RU.js` | record Riserve d'Ufficio |
| `fcmRecordsNext_Modifiers.js` | record e statistiche Modificatori |
| `fcmRecordsNext_ThresholdsLuck.js` | Soglie e indicatori di Fortuna |
| `fcmRecordsNext_Culometro.js` | dati del Culometro |
| `fcmRecordsNext_Matches.js` | dataset partita-per-partita |
| `fcmRecordsNext_Manifest.js` | manifest della pubblicazione e stagione target |

### Matches

`fcmRecordsNext_Matches.js` contiene una riga per squadra per incontro, quindi normalmente **due righe per partita**.

Tra i campi canonici sono previsti:

- stagione;
- competizione;
- match;
- giornata/round;
- squadra;
- avversario;
- gol fatti/subiti;
- risultato standardizzato `V/N/P`;
- informazioni necessarie ai record;
- collegamento al tabellino quando disponibile.

I turni di riposo non vengono rappresentati come partite.

## Calendari DataA

L'archivio canonico è interno a RecordsNext:

`data\calendars\DataA-YYYY.js`

La release 3.1 include i calendari:

`DataA-1991.js` … `DataA-2026.js`

I formati storici non vengono distruttivamente uniformati: RecordsNext conserva i file originali compatibili con le varie stagioni.

Se il DataA canonico manca e per quella stagione è configurato un sito locale con `js\DataA.js`, RecordsNext può recuperarlo e copiarlo nell'archivio interno.

## Tabellini storici

I tabellini possono essere individuati nei formati compatibili con i siti FCM, tra cui:

- `ris*.htm`;
- `ris*.html`;
- `ris*.php`.

Il collegamento online usa il sito online configurato per la stagione corretta, evitando che un risultato storico venga aperto sotto la cartella della stagione corrente.

## Flusso d'uso consigliato

### Prima installazione

1. Installare RecordsNext.
2. Avviare `RecordsNext.bat`.
3. Configurare la lega.
4. Aggiungere le stagioni gestite con FCM/FCA.
5. Associare i siti locali solo alle stagioni che devono essere pubblicate.
6. Configurare le famiglie record.
7. Eseguire una elaborazione **Completa**.
8. Verificare l'output.
9. Se necessario, pubblicare i siti storici selezionati.

### Aggiornamento dopo una giornata

1. Aggiornare normalmente FCM e il sito.
2. Avviare RecordsNext.
3. Usare **Consolidata**.
4. Selezionare **Pubblica nel sito della stagione corrente al termine**.
5. Premere **Elabora**.

### Dopo una modifica strutturale

1. Eseguire una elaborazione completa.
2. Verificare preflight e output.
3. Selezionare le stagioni che hanno un sito da riallineare.
4. Premere **Pubblica i siti delle stagioni selezionate**.

## Stato della release 3.1

La 3.1.0 è stata verificata con:

- suite automatica: **50 test, 0 failure, 0 errori**;
- build Maven completata con successo;
- pubblicazione multisito reale sulle stagioni gestite dal 2006/07 al 2026/27;
- **21 siti target**;
- **189 file validati e 189 pubblicati**;
- verifica del cutoff storico senza contaminazione da stagioni future;
- setup Windows compilato con Inno Setup 7.

SHA256 del setup 3.1.0 pubblicato:

`C39B0EA205F7D81660CD1FF09EC3F6014090932FF3E0D4DA721870D2740CA6EB`

## Versione

RecordsNext 3.1.0
RecordsNext by mauz79
```

## INSTALL.md

SHA-256: `d45341abda5689b94dd098494c5359d12ff1fd84c21a6261a7c53230b34d1178`

```markdown
# Addendum: collaudo sviluppo RecordsNext 4.0 / v46

La release 3.2.0 sottostante resta operativa; questa sezione riguarda **soltanto** la cartella test `D:\DEV_APPS\RecordsNext4.0_TEST_v37`.

1. Usare il pacchetto v46 con preflight SHA-256 e backup; non sovrascrivere sorgenti inattesi.
2. Non usare `RecordsNext.bat` della 3.2 per lanciare la build di sviluppo.
3. Per testare l'importazione con UCanAccess, usare `tools/Start-RecordsNext4-Test-v46.ps1` incluso nella patch: carica il JAR 4.0 e i JAR del runtime ufficiale 3.2 in **sola lettura**.
4. Tenere **Pubblica sui siti = No**, scegliere Completa e avviare la GUI; la suddivisione dei 7 output pesanti avviene anche sullo staging non pubblicato.
5. Verificare file fisici, facade, manifest, identita' e cutoff. Solo successivamente testare su siti clonati.
6. GitHub: le modifiche v46 restano locali fino a `git commit`/`git push` sul ramo di sviluppo 4.0, dopo le verifiche. Non committare database, siti o `player-identities.json`.

Riferimenti: `docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v38.md` e `docs/VERIFICA_STAGING_JS_v46.md`.

---

# Installazione RecordsNext 3.1.0

## Requisiti

- Windows
- Java 21 o superiore
- Fantacalcio Manager
- file FCM/FCA delle stagioni che si desidera importare

UCanAccess 2.0.9.5 e le dipendenze necessarie sono incluse nel setup.

## Pacchetto pubblico

La 3.1 viene distribuita come **installazione pulita**.

File:

`RecordsNext_3.1.0_SETUP.exe`

Non esiste un pacchetto UPDATE pubblico per la 3.1.

SHA256:

`C39B0EA205F7D81660CD1FF09EC3F6014090932FF3E0D4DA721870D2740CA6EB`

## Installazione

1. Avviare `RecordsNext_3.1.0_SETUP.exe`.
2. Confermare il controllo Java.
3. Scegliere la cartella di destinazione.
4. Completare il wizard.
5. Avviare RecordsNext dal collegamento creato oppure da `RecordsNext.bat`.

La schermata della cartella viene sempre mostrata. La convenzione consigliata è:

`<cartella FCM>\plugin\RecordsNext`

Esempio, se FCM è installato in `C:\FCM`:

`C:\FCM\plugin\RecordsNext`

La cartella non è hardcoded: ogni utente può avere FCM in un percorso diverso.

## Cosa installa il setup

Il setup installa:

- applicazione RecordsNext;
- launcher;
- runtime necessario;
- UCanAccess 2.0.9.5 e dipendenze;
- visualizzatori/asset distribuiti con RecordsNext;
- archivio calendari `DataA-1991.js` … `DataA-2026.js`;
- struttura delle directory dati.

Il setup non installa:

- database personali;
- file FCM/FCA;
- configurazioni della lega;
- dati provenienti da installazioni precedenti;
- file nei siti FCM dell'utente.

## Installazione pulita e vecchie versioni

La 3.1 è progettata per una installazione pulita. Una vecchia installazione può essere conservata separatamente finché la nuova non è stata verificata.

Non sovrascrivere alla cieca una vecchia installazione contenente configurazioni o dati personali.

## Primo avvio

Avviare:

`RecordsNext.bat`

Aprire **Configurazione stagioni**.

### Nome lega

Il nome lega può essere lasciato vuoto: RecordsNext prova a ricavarlo dal primo FCM gestito. L'ID tecnico della lega non viene richiesto nella GUI.

### Aggiungere una stagione gestita

Una stagione Gestita richiede:

- file FCM;
- file FCA.

Sono opzionali:

- sito locale;
- sito online.

Il sito locale serve esclusivamente come destinazione di pubblicazione. La cartella JS non si configura separatamente: è sempre:

`<sito locale>\js`

### Aggiungere una stagione manuale

Una stagione Manuale richiede soltanto:

- anni nel formato `AAAA/AAAA`;
- numero stagione.

Non richiede FCM/FCA.

## URL sito online

È possibile inserire, per esempio:

`www.example.org\lega2026`

Al salvataggio RecordsNext normalizza il valore in:

`http://www.example.org/lega2026`

Regole:

- `\` → `/`;
- protocollo mancante → `http://`;
- `https://` esplicito viene conservato;
- slash finali superflui vengono eliminati.

## Calendari

Il calendario storico canonico è:

`data\calendars\DataA-YYYY.js`

Esempi:

- `2024_2025` → `data\calendars\DataA-2024.js`
- `2025_2026` → `data\calendars\DataA-2025.js`
- `2026_2027` → `data\calendars\DataA-2026.js`

La 3.1 distribuisce i calendari dal 1991 al 2026.

Se il DataA canonico manca e il sito locale della stagione contiene `js\DataA.js`, RecordsNext prova a recuperarlo nell'archivio interno.

## Prima elaborazione

Dopo aver configurato le stagioni:

1. scegliere le famiglie record;
2. configurare Modificatori, Soglie/Fortuna e Culometro se necessari;
3. usare modalità **Completa**;
4. eseguire l'elaborazione;
5. controllare il preflight;
6. verificare i JS generati.

## Uso ordinario

Dopo ogni aggiornamento FCM:

1. avviare RecordsNext;
2. usare **Consolidata**;
3. selezionare **Pubblica nel sito della stagione corrente al termine**;
4. premere **Elabora**.

Questa modalità aggiorna soltanto il sito della stagione corrente.

## Riallineamento dei siti storici

Usare **Pubblica i siti delle stagioni selezionate** quando occorre aggiornare più vecchi siti.

Le checkbox di stagione identificano i target da pubblicare. Per ciascun target RecordsNext applica il cutoff storico: il sito riceve soltanto le stagioni disponibili fino a quella stagione.

## Output pubblicati

Nella cartella `js` del sito target vengono pubblicati:

- `fcmRecordsNext_Core.js`
- `fcmRecordsNext_Classics.js`
- `fcmRecordsNext_Series.js`
- `fcmRecordsNext_RU.js`
- `fcmRecordsNext_Modifiers.js`
- `fcmRecordsNext_ThresholdsLuck.js`
- `fcmRecordsNext_Culometro.js`
- `fcmRecordsNext_Matches.js`
- `fcmRecordsNext_Manifest.js`

## Problemi comuni

### Java non trovato o versione troppo vecchia

Installare Java 21 o superiore e verificare che `java` sia disponibile nel `PATH`.

### Una stagione non viene pubblicata

Controllare:

- che abbia un sito locale configurato;
- che la directory del sito esista;
- che la stagione sia selezionata quando si usa la pubblicazione multisito.

Una stagione senza sito può comunque contribuire allo storico.

### Il Culometro risulta parziale

Il preflight può indicare dipendenze opzionali mancanti. In questo caso il Culometro può essere generato parzialmente senza rendere fallita l'intera elaborazione.

### Il link al tabellino apre la stagione sbagliata

Controllare il sito online configurato sulla singola stagione e salvarlo nuovamente per applicare la normalizzazione URL.
```

## CHANGELOG.md

SHA-256: `360420af9ef059096aecd223f32bc87feb3c80fa0347319351ebaca3f71e80c6`

```markdown
# Modifiche di sviluppo 4.0 — v46 (11 novembre 2026, non pubblicata)

- Corretto `RecordsNextPipeline` in modalita generazione senza pubblicare: shard flat 3.2 prima del limite fisico JS.
- Validazione finale di tutti i JS anche senza la famiglia Top & Flop.
- Limite rigoroso **1.500.000 byte** applicato a facade, shard e altri JS, anche nel multisito; il precedente valore 1536 KiB non e' la soglia 4.0.
- Manifest staging riallineato ai file effettivi; conteggio output corretto.
- Regressione con Classics cumulativo di oltre 9 MB, verificati 20 shard (20 stagioni) sotto il limite.
- Documentazione 4.0 versionata v38, README e INSTALL aggiornati; nessuna modifica ai calcoli dei Classici o al motore storico squadre 3.2.
- Stato: build Java 21 testata; esecuzione integrale sulla copia Windows da confermare prima del push/release.

---

# Changelog

## RecordsNext 3.2.0 - 2026-09-16

### Catalogo modulare e preflight

- completato il catalogo modulare RecordsNext: 94 record/figli complessivi, di cui 93 configurabili e Culometro opzionale;
- completata la corrispondenza tra controlli GUI e viste generate;
- dipendenze valutate a livello di singolo record;
- corretta la capability `modifier.home-field` nel preflight;
- `modifiers.home-field-deciding` viene ora eseguito quando il Fattore Campo e selezionato.

### Culometro

- aggiunto il componente `HOME_FIELD_DECISIVE`;
- integrati gli eventi derivati da `fattoreCampoDecisivo`;
- aggiunti frequenza storica dell'evento, frequenza storica della configurazione, impatto e contributo;
- verifica reale: `homeFieldCandidateCount=1304` e 2608 occorrenze `HOME_FIELD_DECISIVE`.

### Verifiche

- contratto checkbox/vista: 96 controlli verificati, 0 problemi;
- audit semantici Classici, Serie, RU, Modificatori, Soglie/Fortuna e Culometro superati;
- suite Maven: 57 test, 0 failure, 0 errori, 0 skipped;
- collaudo operativo finale in modalita Completa: 72 selezionati, 72 eseguibili, 72 completi, 0 parziali, 0 saltati;
- storico `2006_2007-2025_2026` riconosciuto invariato;
- stagione corrente `2026_2027` reimportata e normalizzata;
- generazione finale: 9 file validi, 0 pubblicati;
- consolidamento aggiornato correttamente.

### Packaging 3.2.0

- `RecordsNext_3.2.0_FULL.zip` - SHA256 `BFE32FBBA14A5E45E3EA110C31A6C207F0E82048162B7572610D6E5A0A38AD71`;
- `RecordsNext_3.2.0_SETUP.exe` - SHA256 `760FF711920869BF3FFA031747A2BF192C46266A76C3698F520D55BE9FC14B22`;
- installer pubblico compilato con Inno Setup 7;
- JAR 3.2.0 copiato anche nell'installazione operativa usata da FCM.




## RecordsNext 3.1.1 â€” 2026-09-06

### Correzione output JavaScript

- corretta la regressione della 3.1.0 che, nel percorso multisito, pubblicava nuovamente file cumulativi da circa 6-11 MiB;
- introdotta pubblicazione shard stagionali **flat** direttamente nella cartella `js` del sito;
- eliminata la dipendenza operativa dalla sottocartella `js/recordsnext-data`;
- mantenuti invariati i nomi canonici `fcmRecordsNext_Classics.js`, `Series`, `RU`, `Modifiers`, `ThresholdsLuck`, `Culometro` e `Matches` come facade leggere;
- ogni sito resta autosufficiente e contiene localmente gli shard ammessi dal proprio cutoff storico;
- mantenuto il cutoff multisito basato su `rn_season.sort_order`;
- pulizia controllata degli shard flat obsoleti durante la ripubblicazione.

### Verifiche

- suite automatica: 52 test, 0 failure, 0 errori;
- pubblicazione sito corrente verificata: 140 shard + 9 file canonici = 149 file;
- shard massimo osservato: circa 0,93 MiB;
- nessun file RecordsNext oltre 1,5 MiB;
- pubblicazione multisito reale verificata su tutte le stagioni configurate;
- numero file progressivo coerente col cutoff storico (16 sul sito 2006, +7 per ogni stagione successiva con dati, 149 sui siti piÃ¹ recenti);
- nessuna sottocartella RecordsNext richiesta sotto `js`;
- vecchie cartelle `recordsnext-data` rimosse dai siti storici dopo il collaudo.
## RecordsNext 3.1.0 — 2026-09-02

### Architettura e storico
- separazione definitiva tra sorgenti storiche e destinazioni di pubblicazione;
- FCM/FCA/DataA definiscono lo storico;
- sito locale opzionale per stagione;
- `DataA` canonico in `data/calendars/DataA-YYYY.js`;
- archivio calendari 1991-2026 incluso nella distribuzione;
- fallback da `<sito>\js\DataA.js` solo quando il calendario canonico manca;
- nessuna normalizzazione distruttiva dei diversi formati DataA storici.

### Pubblicazione multisito
- pubblicazione ordinaria nel solo sito della stagione corrente;
- comando `Pubblica i siti delle stagioni selezionate`;
- ogni target riceve esclusivamente lo storico fino al proprio `sort_order`;
- `Core` e `Manifest` coerenti con la stagione target;
- identità canoniche riancorate allo scope storico disponibile;
- Classici, Serie, RU, Modificatori, Soglie/Fortuna, Culometro e Matches generati nello scope del target;
- rimosso il fallback hardcoded verso `E:/fantacalcio/Lega2025/js`;
- corretto lo staging di `culometro.json` nel flusso multisito.

### GUI
- versione GUI 3.1;
- semantica distinta fra pubblicazione corrente e pubblicazione multisito;
- sito locale non più obbligatorio;
- stato calendario mostrato esplicitamente;
- recupero automatico del DataA dal sito quando l'archivio canonico è assente;
- ID lega rimosso dai campi richiesti all'utente;
- nome lega opzionale, con tentativo di derivazione dal primo FCM;
- modalità di aggiunta stagione Manuale semplificata, senza controlli FCM/FCA inutili;
- testo informativo stagioni aggiornato al modello 3.1;
- normalizzazione URL online con `http://` come protocollo predefinito quando assente.

### Modello dati e robustezza
- introdotto `SeasonPublicationTargetRepository`;
- scope di pubblicazione basato su `sort_order`;
- cancellazione stagioni transazionale e coerente con foreign key;
- riancoraggio delle identità canoniche dopo cancellazione;
- promozione della stagione gestita più recente rimasta come anchor;
- `fcmRecordsNext_Matches.js` mantenuto come dataset canonico partita-per-partita.

### Packaging
- versione Maven 3.1.0;
- installer Windows realizzato con Inno Setup 7;
- scelta della directory sempre visibile;
- convenzione pubblica `<cartella FCM>\plugin\RecordsNext`;
- distribuzione pubblica **solo clean install**;
- eliminato il flusso UPDATE pubblico;
- UCanAccess 2.0.9.5 mantenuto nel runtime;
- setup senza database personali, FCM/FCA o configurazioni utente.

### Verifiche finali
- suite automatica: **50 test, 0 failure, 0 errori**;
- build Maven completata con successo;
- pubblicazione multisito reale verificata su **21 siti**, dalle stagioni 2006/07 a 2026/27;
- **189 file validati e 189 pubblicati**;
- verificato il cutoff storico senza contaminazione da stagioni future;
- setup finale compilato con successo.

SHA256 `RecordsNext_3.1.0_SETUP.exe`:

`C39B0EA205F7D81660CD1FF09EC3F6014090932FF3E0D4DA721870D2740CA6EB`

## RecordsNext 3.0.0 — 2026-08-28

### Architettura dati stagionale

- introdotti shard JavaScript separati per stagione per Classics, Series, RU, Modifiers, ThresholdsLuck, Culometro e Matches;
- ogni stagione conserva i propri sette shard nella cartella `js/recordsnext-data` del relativo sito FCM;
- il sito corrente mantiene gli stessi nomi pubblici `fcmRecordsNext_*.js`, ora trasformati in facade leggere;
- API JavaScript pubblica e strutture dati esposte ai visualizzatori restano compatibili con RecordsNext 2.1;
- caricamento locale e online degli shard basato sui percorsi configurati per ciascuna stagione, senza dipendenze specifiche da Altervista;
- introdotta la disponibilita' progressiva locale/online delle stagioni storiche;
- introdotto lo stato persistente `data/consolidation/recordsnext-shards.properties`;
- aggiunta `SeasonShardAvailabilityCli` per la gestione della migrazione progressiva;
- shard invariati non vengono riscritti;
- una stagione corrente ancora senza partite testa-a-testa non genera shard vuoti ne' riferimenti a file inesistenti.

### Elaborazione consolidata

- la modalita' `CONSOLIDATED` aggiorna soltanto la stagione corrente quando storico e configurazione risultano invariati;
- corretta la gestione di una nuova stagione senza `season_normalized_*.json`: Classic e RU restano invariati invece di terminare con errore;
- validato il passaggio da circa 103 secondi in FULL a circa 5-6 secondi in CONSOLIDATED sul dataset operativo corrente.

### Pubblicazione

- restano pubblicati soltanto i 9 output moderni RecordsNext;
- gli output pubblici legacy `records2026.*` non vengono piu' generati;
- validata la distribuzione di 20 stagioni storiche, dal 2006_2007 al 2025_2026;
- validati 140 shard complessivi, 7 per stagione;
- tutti i singoli shard restano sotto 1 MiB nel dataset operativo validato;
- le sette facade del sito corrente referenziano correttamente tutte le stagioni storiche abilitate;
- corretta la rotta online della stagione 2022_2023 verso `/lega2022/`.

### Versione

- versione Maven 3.0.0;
- manifest applicativo 3.0.0;
- runner console identificato come RecordsNext 3.0;
- Java 21 o superiore;
- UCanAccess 2.0.9.5 invariato.

## RecordsNext 2.1.0 â€” 2026-08-26

Aggiornamento della linea RecordsNext 2.x orientato alla completezza degli output,
alla paritÃ  dei dati disponibili sul sito e alla semplificazione dell'aggiornamento
delle installazioni esistenti.

### Output e dati

- aggiunto l'output canonico `fcmRecordsNext_Matches.js`;
- una riga per squadra per ogni incontro reale, quindi due righe per partita;
- esclusione dei turni di riposo dall'output Matches;
- risultati standardizzati `V/N/P`;
- link ai tabellini storici corretti anche per stagioni precedenti;
- arricchiti i dettagli delle Serie con stagione, competizione, giornata, incontro e tabellino;
- arricchiti gli eventi del Culometro con frequenza storica dell'evento,
  frequenza storica della configurazione e impatto;
- invariata la formula di calcolo e la classifica del Culometro.

### Bonifica output legacy

- rimossa dalla pubblicazione la famiglia di file `records2026.*` non piu consumata dal sito moderno;
- `Records2026ClassicJsExporter` e `Records2026RuJsExporter` restano usati solo internamente dai wrapper RecordsNext per preservare la compatibilita dei dati;
- `fcmRecordsNext_Classics.js` e `fcmRecordsNext_RU.js` verificati byte-per-byte invariati rispetto agli output 2.1 precedenti;
- eliminati circa 28 MiB di output legacy per sito senza modifiche al frontend.

### Configurazione storica

- dopo l'aggiunta di una stagione gestita, la finestra delle associazioni storiche
  si apre direttamente sulla stagione appena aggiunta;
- il comando generale di configurazione delle associazioni conserva invece
  il comportamento newest-first.

### Aggiornamento da RecordsNext 2.0

- introdotto un pacchetto UPDATE dedicato;
- l'aggiornamento sostituisce soltanto programma e launcher;
- configurazione, database, stagioni e associazioni storiche vengono conservati;
- backup automatico di sicurezza prima della sostituzione;
- aggiornamento 2.0 -> 2.1 verificato su un'installazione reale giÃ  configurata.

### Release e runtime

- versione Maven 2.1.0;
- manifest applicativo 2.1.0;
- GUI identificata come RecordsNext 2.1;
- runtime UCanAccess 2.0.9.5 invariato;
- Java 21 o superiore.

### Verifiche

- suite Maven completata con 41 test superati;
- regressione completa sui dati storici verificata;
- Matches verificato su 20 stagioni;
- 10.930 partite reali e 21.860 righe squadra verificate;
- nessun gruppo Matches diverso da due righe;
- nessuna coppia non speculare;
- confronto semantico delle famiglie preesistenti superato;
- aggiornamento reale 2.0 -> 2.1 verificato senza modifica di configurazione e database;
- generazione del sito tramite il flusso reale FCM verificata con RecordsNext 2.1.

## RecordsNext 2.0.0 â€” 2026-08-10

Prima release stabile della nuova linea RecordsNext 2.0.

### NovitÃ  principali

- nuova GUI Swing dedicata `RecordsNext by mauz79`;
- configurazione guidata di lega e stagioni;
- supporto stagioni GESTITE e MANUALI;
- associazioni storiche/canoniche di squadre e competizioni;
- lettura FCM/FCA tramite UCanAccess 2.0.9.5;
- pipeline modulare per famiglie di record;
- modalitÃ  di elaborazione Consolidata e Completa;
- output JavaScript separati per famiglia;
- pubblicazione verso il sito FCM;
- visualizzatori HTML statici;
- supporto ai link tabellini `.htm`, `.html` e `.php`;
- record di lega;
- Culometro opzionale.

### Famiglie disponibili

- Classici
- Serie
- Riserve d'Ufficio
- Modificatori
- Soglie e Fortuna
- Culometro

### Modificatori

- supporto modificatori standard FCM;
- supporto MODM1PERS, MODM2PERS e MODM3PERS;
- nomi personalizzabili dei modificatori personali;
- selezione granulare delle statistiche;
- supporto serie dei modificatori;
- supporto Fattore Campo.

### Storico

- associazioni canoniche delle squadre;
- associazioni canoniche delle competizioni;
- gestione differenze di denominazione tra stagioni;
- esclusione delle stagioni MANUALI dall'elaborazione FCM;
- ricostruzione completa delle stagioni GESTITE.

### Visualizzatori

- home RecordsNext;
- Classici;
- Serie;
- Riserve d'Ufficio;
- Modificatori;
- Soglie e Fortuna;
- Culometro;
- Record di lega;
- filtri per stagione e competizione;
- nomi competizioni leggibili;
- selezione eventi Culometro nella sola vista Eventi;
- profili `mauzstrom`, `maelstrom`, `fantablue2`, `neutral`.

### Release e runtime

- versione Maven 2.0.0;
- manifest applicativo 2.0.0;
- launcher `RecordsNext.bat`;
- runtime UCanAccess 2.0.9.5 distribuito con dipendenze e licenze;
- configurazione `league.json` generata automaticamente dalla GUI.

### Verifiche effettuate

- build Maven completato con successo;
- test automatici superati;
- audit contratto checkbox â†’ vista superato;
- audit semantici Classici, Serie, RU, Modificatori, Fattore Campo, Soglie/Fortuna e Culometro superati;
- smoke test GUI e visualizzatori superato;
- lettura FCM dalla distribuzione di prova verificata;
- generazione automatica `config/league.json` verificata.

### Note

Le stringhe interne che citano RecordsNext 1.0.2 negli exporter indicano la provenienza/compatibilitÃ  degli archivi normalizzati e non la versione corrente del prodotto.
```
