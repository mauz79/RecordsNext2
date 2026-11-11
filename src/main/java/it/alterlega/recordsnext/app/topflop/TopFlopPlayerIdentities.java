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
