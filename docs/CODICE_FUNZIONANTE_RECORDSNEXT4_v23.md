# RecordsNext 4.0 — Bibbia v23 — JS Top & Flop in staging

Base: RecordsNext 3.2.0. Publisher, GUI, dati e siti operativi invariati.
Bibbia precedente: `docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v22.md`
SHA-256 bibbia precedente: `723b3f127f359bd9abaf1093b18edc94b6f153f54d58239b96197c877c66c560`

## `src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopArchive.java`

SHA-256: `e1638154bccdc9605e7172117cb4412aa6435e8b30d3e40452179a0fc2260f9f`

~~~~java
package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.topflop.TopFlopFacts.FantasyFact;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.FantasyKey;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.PlayerSeason;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.RealDay;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.RealDayKey;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.RosterSnapshot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Non-persistent staging model. Does not write SQLite or install JS.
 * Same real performance is stored once; independent fantasy contexts remain distinct.
 */
public final class TopFlopArchive {
    private record RosterKey(PlayerSeason player, int teamId) {}
    private final Map<RosterKey, RosterSnapshot> rosters = new LinkedHashMap<>();
    private final Map<RealDayKey, RealDay> realDays = new LinkedHashMap<>();
    private final Map<FantasyKey, FantasyFact> fantasyFacts = new LinkedHashMap<>();

    public void addRosterSnapshot(RosterSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        RosterKey key = new RosterKey(snapshot.player(), snapshot.fantasyTeamId());
        RosterSnapshot existing = rosters.putIfAbsent(key, snapshot);
        if (existing != null && !existing.equals(snapshot))
            throw new IllegalArgumentException("Conflicting roster snapshot: " + key);
    }

    public int rosterCount() { return rosters.size(); }

    /** Read-only iteration for season exports; avoids per-player full map scans. */
    public List<RosterSnapshot> allRosters() { return List.copyOf(rosters.values()); }

    public List<RosterSnapshot> rostersFor(PlayerSeason player) {
        Objects.requireNonNull(player, "player");
        List<RosterSnapshot> result = new ArrayList<>();
        for (RosterSnapshot snapshot : rosters.values())
            if (snapshot.player().equals(player)) result.add(snapshot);
        return List.copyOf(result);
    }

    public void addRealDay(RealDay day) {
        Objects.requireNonNull(day, "day");
        RealDay existing = realDays.putIfAbsent(day.key(), day);
        if (existing != null && !existing.equals(day))
            throw new IllegalArgumentException("Conflicting real day: " + day.key());
    }

    public void addFantasyFact(FantasyFact fact) {
        Objects.requireNonNull(fact, "fact");
        if (!realDays.containsKey(fact.realDayKey()))
            throw new IllegalArgumentException("Real day not loaded: " + fact.realDayKey());
        FantasyFact existing = fantasyFacts.putIfAbsent(fact.key(), fact);
        if (existing != null && !existing.equals(fact))
            throw new IllegalArgumentException("Conflicting fantasy match/player key: " + fact.key());
    }

    public RealDay realDay(RealDayKey key) { return realDays.get(key); }
    public int realDayCount() { return realDays.size(); }

    /** Every FCA real day exactly once, including no-vote days. */
    public List<RealDay> allRealDays() { return List.copyOf(realDays.values()); }
    public int fantasyFactCount() { return fantasyFacts.size(); }

    /** Every distinct FCM context exactly once. */
    public List<FantasyFact> allFantasyFacts() { return List.copyOf(fantasyFacts.values()); }

    public List<RealDay> realDaysFor(PlayerSeason player) {
        Objects.requireNonNull(player, "player");
        List<RealDay> result = new ArrayList<>();
        for (RealDay day : realDays.values())
            if (day.key().player().equals(player)) result.add(day);
        return List.copyOf(result);
    }

    public List<FantasyFact> fantasyFactsFor(PlayerSeason player) {
        Objects.requireNonNull(player, "player");
        List<FantasyFact> result = new ArrayList<>();
        for (FantasyFact fact : fantasyFacts.values())
            if (fact.key().player().equals(player)) result.add(fact);
        return List.copyOf(result);
    }
}
~~~~

## `src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopStageJsExporter.java`

SHA-256: `81940e865614fa3b9d43eaebdad38d55676dd7d1b7d7464aaa5c406c585ebde9`

~~~~java
package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.topflop.TopFlopFacts.FantasyFact;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.PlayerSeason;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.RealDay;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.Usage;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.VoteState;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Staging-only native Java exporter for Records Top & Flop.
 *
 * <p>It consumes the SAME 3.2-derived canonical facts and the previously tested
 * JDBC assembler, never rewrites SQLite, never guesses a scorecard URL and does
 * not publish to a website. File names are flat, matching the 3.2 JS directory.
 * Every physical JS file is strictly at or under MAX_BYTES. A page can load
 * season summaries before requesting per-player details on demand.
 *
 * <p>Integration into Records2026SitePublisher/manifest is a separate gate:
 * do NOT simply copy these files into an existing publisher's generatedDir.
 */
public final class TopFlopStageJsExporter {
    public static final long MAX_BYTES = 1_500_000L;
    public static final long TARGET_BYTES = 950_000L;
    public static final String FACADE_NAME = "fcmRecordsNext_TopFlop.js";
    private static final MathContext MC = MathContext.DECIMAL64;

    private TopFlopStageJsExporter() {}

    public record FileInfo(String name, String seasonId, String kind, int rows, long bytes, String sha256) {}
    public record Result(int seasons, int players, long realDays, long fantasyFacts,
                         List<FileInfo> files, Path stagingDirectory) {}

    /** Caller owns the connection, exactly as in RecordsNext 3.2. */
    public static Result export(Connection connection, List<String> seasonIds, Path newJsDirectory)
            throws SQLException, IOException {
        return export(connection, seasonIds, newJsDirectory, null);
    }

    public static Result export(Connection connection, List<String> seasonIds, Path newJsDirectory,
                                Path normalizedReportsRoot) throws SQLException, IOException {
        Objects.requireNonNull(connection, "connection");
        Objects.requireNonNull(seasonIds, "seasonIds");
        Objects.requireNonNull(newJsDirectory, "newJsDirectory");
        if (Files.exists(newJsDirectory))
            throw new IOException("Staging already exists: " + newJsDirectory);
        if (seasonIds.isEmpty() || seasonIds.stream().distinct().count() != seasonIds.size())
            throw new IllegalArgumentException("Season list empty or duplicated");
        for (String season : seasonIds)
            if (season == null || !season.matches("[0-9]{4}_[0-9]{4}"))
                throw new IllegalArgumentException("Invalid season: " + season);
        Files.createDirectories(newJsDirectory);
        List<FileInfo> infos = new ArrayList<>();
        List<Object> indexSeasons = new ArrayList<>();
        int playersTotal = 0;
        long realTotal = 0, fantasyTotal = 0;
        for (String season : seasonIds) {
            TopFlopJdbcArchiveAssembler.LoadedSeason loaded =
                    TopFlopJdbcArchiveAssembler.loadSeason(connection, season);
            TopFlopArchive archive = loaded.archive();
            Map<TopFlopCanonicalScorecards.MatchTeam,String> canonicalLinks =
                normalizedReportsRoot == null ? Map.of() :
                TopFlopCanonicalScorecards.load(normalizedReportsRoot,season);
            playersTotal += loaded.profiles().size();
            realTotal += archive.realDayCount();
            fantasyTotal += archive.fantasyFactCount();
            Map<PlayerSeason, List<RealDay>> realByPlayer = new LinkedHashMap<>();
            for (RealDay day : archive.allRealDays())
                realByPlayer.computeIfAbsent(day.key().player(), ignored -> new ArrayList<>()).add(day);
            Map<PlayerSeason, List<FantasyFact>> fantasyByPlayer = new LinkedHashMap<>();
            for (FantasyFact fact : archive.allFantasyFacts())
                fantasyByPlayer.computeIfAbsent(fact.key().player(), ignored -> new ArrayList<>()).add(fact);
            List<TopFlopFcaJdbcReader.PlayerProfile> players = loaded.profiles().values().stream()
                .sorted(Comparator.comparingInt(p -> p.player().fcaPlayerId())).toList();
            List<String> detailRows = new ArrayList<>(players.size());
            for (var profile : players) {
                PlayerSeason p = profile.player();
                detailRows.add(Json.write(detail(profile,
                    realByPlayer.getOrDefault(p, List.of()),
                    fantasyByPlayer.getOrDefault(p, List.of()),canonicalLinks)));
            }
            List<FileInfo> detailShards = shard(newJsDirectory, season, "Detail", detailRows);
            Map<Integer,String> detailFileByLocalPlayerId = new LinkedHashMap<>();
            int nextPlayerIndex = 0;
            for (FileInfo part : detailShards) {
                for (int row = 0; row < part.rows(); row++) {
                    detailFileByLocalPlayerId.put(players.get(nextPlayerIndex++).player().fcaPlayerId(), part.name());
                }
            }
            if (nextPlayerIndex != players.size())
                throw new IOException("Details player coverage mismatch: " + season);
            List<String> summaryRows = new ArrayList<>(players.size());
            for (var profile : players) {
                PlayerSeason p = profile.player();
                summaryRows.add(Json.write(summary(profile,
                    realByPlayer.getOrDefault(p, List.of()),
                    fantasyByPlayer.getOrDefault(p, List.of()),
                    detailFileByLocalPlayerId.get(p.fcaPlayerId()))));
            }
            List<FileInfo> summaryShards = shard(newJsDirectory, season, "Summary", summaryRows);
            infos.addAll(summaryShards);
            infos.addAll(detailShards);
            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("seasonId", season);
            metadata.put("players", players.size());
            metadata.put("realDays", archive.realDayCount());
            metadata.put("fantasyContexts", archive.fantasyFactCount());
            metadata.put("youthExcluded", loaded.fcm().youthExcluded());
            long links=archive.allFantasyFacts().stream().filter(f->
                canonicalLinks.containsKey(new TopFlopCanonicalScorecards.MatchTeam(
                    f.key().matchId(),f.key().fantasyTeamId()))).count();
            metadata.put("contextLinksResolved",links);
            metadata.put("summaryFiles", summaryShards.stream().map(FileInfo::name).toList());
            metadata.put("detailFiles", detailShards.stream().map(FileInfo::name).toList());
            indexSeasons.add(metadata);
        }
        Map<String,Object> facade = new LinkedHashMap<>();
        facade.put("schemaVersion", "4.0-stage-v1");
        facade.put("familyId", "top_flop");
        facade.put("status", "STAGING_ONLY_NOT_PUBLISHED");
        facade.put("maxJsBytes", MAX_BYTES);
        facade.put("seasons", indexSeasons);
        String facadeJs = "window.fcmRecordsNextTopFlop = " + Json.write(facade) + ";\n";
        infos.add(writeChecked(newJsDirectory, FACADE_NAME, "", "Facade", 1, facadeJs));
        for (FileInfo info : infos)
            if (info.bytes() > MAX_BYTES) throw new IOException("Oversize file: " + info.name());
        return new Result(seasonIds.size(), playersTotal, realTotal, fantasyTotal,
                List.copyOf(infos), newJsDirectory);
    }

    private static Map<String,Object> summary(TopFlopFcaJdbcReader.PlayerProfile profile,
                                               List<RealDay> realDays, List<FantasyFact> contexts,
                                               String detailFile) {
        PlayerSeason p = profile.player();
        Map<String,Object> out = playerHeader(profile);
        out.put("detailFile", Objects.requireNonNull(detailFile));
        BigDecimal sum = BigDecimal.ZERO;
        int voted=0;
        Map<String,BigDecimal> realEvents = new TreeMap<>();
        for (RealDay day: realDays) {
            if (day.voteState()== VoteState.VALID) { sum=sum.add(day.vote()); voted++; }
            for (var event:day.events().entrySet())
                if (event.getValue().signum() != 0)
                    realEvents.merge(event.getKey(), event.getValue(), BigDecimal::add);
        }
        Map<String,Object> real = new LinkedHashMap<>();
        real.put("days", realDays.size()); real.put("withVote", voted);
        real.put("voteSum", sum); real.put("meanVote", voted==0?null:sum.divide(BigDecimal.valueOf(voted),MC));
        real.put("sourceEvents", realEvents);
        out.put("real",real);
        Map<String,Contribution> byTeam = new TreeMap<>();
        for (FantasyFact fact : contexts) {
            String team = fact.sourceFields().getOrDefault("FCM.Fantasquadra.Nome", "");
            String key = fact.key().fantasyTeamId()+"|"+fact.key().sourceCompetitionId();
            Contribution group = byTeam.computeIfAbsent(key, ignored ->
                new Contribution(fact.key().fantasyTeamId(),team,fact.key().sourceCompetitionId(),
                        fact.competitionName(), fact.family().name()));
            group.contexts++;
            if (fact.usage()==Usage.COUNTED) {
                group.counted++;
                group.fantasySum=group.fantasySum.add(fact.fantasyVote());
                var realDay = realDays.stream().filter(d -> d.key().equals(fact.realDayKey())).findFirst();
                if (realDay.isPresent() && realDay.get().voteState()==VoteState.VALID) {
                    group.voted++;
                    group.voteSum=group.voteSum.add(realDay.get().vote());
                }
                for(var e:fact.fantasyEvents().entrySet())
                    if (e.getValue().signum() != 0)
                        group.events.merge(e.getKey(),e.getValue(),BigDecimal::add);
            }
        }
        out.put("fantasyContexts", contexts.size());
        out.put("contributions", byTeam.values().stream().map(Contribution::asMap).toList());
        return out;
    }

    private static final class Contribution {
        final int teamId, competitionId; final String team, competition, family;
        int contexts, counted, voted;
        BigDecimal voteSum=BigDecimal.ZERO, fantasySum=BigDecimal.ZERO;
        final Map<String,BigDecimal> events=new TreeMap<>();
        Contribution(int teamId,String team,int competitionId,String competition,String family) {
            this.teamId=teamId;this.team=team;this.competitionId=competitionId;
            this.competition=competition;this.family=family;
        }
        Map<String,Object> asMap() {
            Map<String,Object> m=new LinkedHashMap<>();
            m.put("teamId",teamId);m.put("team",team);m.put("competitionId",competitionId);
            m.put("competition",competition);m.put("family",family);
            m.put("contexts",contexts);m.put("counted",counted);m.put("countedWithRealVote",voted);
            m.put("voteSum",voteSum);m.put("meanVote",voted==0?null:voteSum.divide(BigDecimal.valueOf(voted),MC));
            m.put("fantasySum",fantasySum);m.put("meanFantasy",counted==0?null:fantasySum.divide(BigDecimal.valueOf(counted),MC));
            m.put("sourceEventsApportioned",events);
            return m;
        }
    }

    private static Map<String,Object> detail(TopFlopFcaJdbcReader.PlayerProfile profile,
                                              List<RealDay> realDays, List<FantasyFact> fantasyFacts,
                                              Map<TopFlopCanonicalScorecards.MatchTeam,String> canonicalLinks) {
        Map<String,Object> out=playerHeader(profile);
        List<Object> real=new ArrayList<>(realDays.size());
        for (RealDay day:realDays) {
            Map<String,Object> v=new LinkedHashMap<>();
            v.put("serieARound",day.key().serieARound());
            v.put("voteState",day.voteState().name());v.put("vote",day.vote());
            // Site projection keeps nonzero source events; the complete FCA originals
            // are still retained in SQLite and in the native Java facts.
            v.put("sourceEvents",nonzero(day.events()));
            real.add(v);
        }
        out.put("realDays",real);
        List<Object> contexts=new ArrayList<>(fantasyFacts.size());
        for (FantasyFact fact:fantasyFacts) {
            Map<String,Object> v=new LinkedHashMap<>();
            v.put("matchId",fact.key().matchId());
            v.put("competitionId",fact.key().sourceCompetitionId());
            v.put("competition",fact.competitionName());
            v.put("family",fact.family().name());
            v.put("competitionRound",fact.fantasyRound());
            v.put("roundDescription",fact.sourceFields().get("FCM.Giornata.Desc"));
            v.put("serieARound",fact.realDayKey().serieARound());
            v.put("teamId",fact.key().fantasyTeamId());
            v.put("team",fact.sourceFields().get("FCM.Fantasquadra.Nome"));
            v.put("usage",fact.usage().name());
            v.put("initialStarter",fact.initialStarter());
            v.put("tabellinoVote",numeric(fact.sourceFields().get("FCM.Tabellino.Voto")));
            v.put("fantasyVote",fact.fantasyVote());
            v.put("sourceEvents",nonzero(fact.fantasyEvents()));
            v.put("scorecardUrl",canonicalLinks.getOrDefault(
                new TopFlopCanonicalScorecards.MatchTeam(fact.key().matchId(),
                fact.key().fantasyTeamId()),fact.scorecardUrl())); // never guessed
            v.put("position",fact.sourceFields().get("FCM.Formazione.Pos"));
            v.put("entered",fact.sourceFields().get("FCM.Formazione.Entrato"));
            v.put("enteredAsReserve",fact.sourceFields().get("FCM.Formazione.EntratoRiserva"));
            v.put("firstEleven",fact.sourceFields().get("FCM.Tabellino.FirstEleven"));
            v.put("modifier",fact.sourceFields().get("FCM.Tabellino.Modif"));
            v.put("usageEvidence",fact.sourceFields().get("TopFlop.UsageEvidence"));
            contexts.add(v);
        }
        out.put("fantasy",contexts);
        return out;
    }

    private static Map<String,BigDecimal> nonzero(Map<String,BigDecimal> events) {
        Map<String,BigDecimal> out=new TreeMap<>();
        for (var e:events.entrySet())
            if (e.getValue().signum()!=0) out.put(e.getKey(),e.getValue());
        return out;
    }

    private static Map<String,Object> playerHeader(TopFlopFcaJdbcReader.PlayerProfile p) {
        Map<String,Object> m=new LinkedHashMap<>();
        m.put("seasonId",p.player().seasonId());
        m.put("localFcaId",p.player().fcaPlayerId());
        m.put("gazzaCode",p.player().gazzaCode());
        m.put("name",p.name());
        return m;
    }
    private static BigDecimal numeric(String value) {
        if(value==null || !value.matches("[+-]?[0-9]+(?:[,.][0-9]+)?"))return null;
        return new BigDecimal(value.replace(',','.'));
    }

    static List<FileInfo> shard(Path dir, String season, String kind, List<String> rows) throws IOException {
        List<FileInfo> result=new ArrayList<>();
        String prefix="(function(){var d=window.fcmRecordsNextTopFlopData||(window.fcmRecordsNextTopFlopData={});"
                +"var a=d."+(kind.equals("Summary")?"summaries":"details")+"||(d."+
                (kind.equals("Summary")?"summaries":"details")+"={});(a[\""+season+"\"]||(a[\""+season+"\"]=[])).push(...[";
        String suffix="]);})();\n";
        StringBuilder body=new StringBuilder(prefix);
        int fixedBytes=(prefix+suffix).getBytes(StandardCharsets.UTF_8).length;
        int usedBytes=fixedBytes;
        int count=0, part=1;
        for(String row:rows) {
            int additional=row.getBytes(StandardCharsets.UTF_8).length+(count==0?0:1);
            if(count>0 && usedBytes+additional>TARGET_BYTES) {
                result.add(writeChecked(dir,fileName(season,kind,part++),season,kind,count,body+suffix));
                body=new StringBuilder(prefix);count=0;usedBytes=fixedBytes;
                additional=row.getBytes(StandardCharsets.UTF_8).length;
            }
            if(usedBytes+additional>MAX_BYTES)
                throw new IOException("One player too large for a JS shard: "+season+" / "+kind);
            if (count>0)body.append(',');
            body.append(row);
            usedBytes+=additional;count++;
        }
        if(count>0)result.add(writeChecked(dir,fileName(season,kind,part),season,kind,count,body+suffix));
        return result;
    }
    private static String fileName(String season,String kind,int part){
        return "fcmRecordsNext_TopFlop."+kind+"."+season+"."+String.format("%03d",part)+".js";
    }
    private static FileInfo writeChecked(Path directory,String name,String season,String kind,int rows,String value)
            throws IOException {
        Path file=directory.resolve(name);
        byte[] bytes=value.getBytes(StandardCharsets.UTF_8);
        if(bytes.length>MAX_BYTES)throw new IOException("JS exceeds 1,500,000 bytes: "+name+" = "+bytes.length);
        Files.write(file,bytes,java.nio.file.StandardOpenOption.CREATE_NEW);
        if(Files.size(file)!=bytes.length)throw new IOException("JS size verification failed: "+name);
        return new FileInfo(name,season,kind,rows,bytes.length,sha(bytes));
    }
    private static String sha(byte[] data) {
        try {
            byte[] digest=MessageDigest.getInstance("SHA-256").digest(data);
            StringBuilder b=new StringBuilder();
            for(byte x:digest)b.append(String.format("%02x",x&0xff));
            return b.toString();
        } catch(Exception ex) { throw new IllegalStateException(ex); }
    }

    /** No external dependency, deterministic JSON values and UTF-8 string escaping. */
    static final class Json {
        static String write(Object value){StringBuilder b=new StringBuilder();append(value,b);return b.toString();}
        private static void append(Object v,StringBuilder b){
            if(v==null){b.append("null");return;}
            if(v instanceof String s){quote(s,b);return;}
            if(v instanceof BigDecimal n){b.append(n.stripTrailingZeros().toPlainString());return;}
            if(v instanceof Number || v instanceof Boolean){b.append(v);return;}
            if(v instanceof Map<?,?> map){b.append('{');boolean first=true;for(var e:map.entrySet()){
                if(!first)b.append(',');first=false;quote(String.valueOf(e.getKey()),b);b.append(':');append(e.getValue(),b);
            }b.append('}');return;}
            if(v instanceof Iterable<?> list){b.append('[');boolean first=true;for(var item:list){
                if(!first)b.append(',');first=false;append(item,b);
            }b.append(']');return;}
            quote(String.valueOf(v),b);
        }
        private static void quote(String value,StringBuilder b){b.append('"');for(int i=0;i<value.length();i++){
            char c=value.charAt(i);switch(c){case '"'->b.append("\\\"");case '\\'->b.append("\\\\");
                case '\b'->b.append("\\b");case '\f'->b.append("\\f");case '\n'->b.append("\\n");
                case '\r'->b.append("\\r");case '\t'->b.append("\\t");
                default->{if(c<0x20 || c=='\u2028'||c=='\u2029')b.append(String.format("\\u%04x",(int)c));else b.append(c);}
            }
        }b.append('"');}
    }

    /** CLI: <readonly-db> <new-staging-js-dir> <normalizedReportsRoot|- > <seasonId>... */
    public static void main(String[] args) throws Exception {
        if(args.length<4)throw new IllegalArgumentException("Usage: <database> <new-stage-dir> <reports-root-or-dash> <seasonId>...");
        Path db=Path.of(args[0]).toAbsolutePath().normalize();
        Path output=Path.of(args[1]).toAbsolutePath().normalize();
        if(!Files.isRegularFile(db))throw new IOException("Database missing: "+db);
        Class.forName("org.sqlite.JDBC");
        Path reports=args[2].equals("-")?null:Path.of(args[2]).toAbsolutePath().normalize();
        if(reports!=null && !Files.isDirectory(reports))throw new IOException("Reports directory missing: "+reports);
        try(Connection connection=DriverManager.getConnection("jdbc:sqlite:"+db.toUri()+"?mode=ro")) {
            Result result=export(connection,List.of(args).subList(3,args.length),output,reports);
            System.out.println("TOP_FLOP_STAGE_JS_OK seasons="+result.seasons()+" players="+result.players()
                +" realDays="+result.realDays()+" fantasy="+result.fantasyFacts()
                +" jsFiles="+result.files().size()
                +" maxBytes="+result.files().stream().mapToLong(FileInfo::bytes).max().orElse(0));
        }
    }
}
~~~~

## `src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopCanonicalScorecards.java`

SHA-256: `61e25f80b6aa58146ad5152574ca08e71bf5492f715a8b801a29847c8b3795f4`

~~~~java
package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.config.TopFlopExistingJson;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Reads the 3.2 normalized season reports, never computes a scorecard URL.
 * A link is keyed by season + actual encounter + fantasy team, not round alone.
 * Missing reports or missing links legitimately result in null.
 */
public final class TopFlopCanonicalScorecards {
    private TopFlopCanonicalScorecards() {}
    public record MatchTeam(long matchId, int teamId) {}

    public static Map<MatchTeam,String> load(Path reportsRoot,String seasonId) throws IOException {
        Objects.requireNonNull(reportsRoot);Objects.requireNonNull(seasonId);
        if (!seasonId.matches("[0-9]{4}_[0-9]{4}")) throw new IllegalArgumentException("Invalid season");
        Path seasonDir=reportsRoot.resolve(seasonId);
        if (!Files.isDirectory(seasonDir)) return Map.of();
        List<Path> reports;
        try(Stream<Path> files=Files.list(seasonDir)) {
            reports=files.filter(Files::isRegularFile)
                .filter(f->f.getFileName().toString().startsWith("season_normalized_"))
                .filter(f->f.getFileName().toString().endsWith(".json"))
                .sorted().toList();
        }
        Map<MatchTeam,String> links=new LinkedHashMap<>();
        for(Path report:reports) {
            final Object root;
            try {root=TopFlopExistingJson.parse(Files.readString(report,StandardCharsets.UTF_8));}
            catch(RuntimeException ex) {throw new IOException("Invalid canonical JSON: "+report,ex);}
            if (!(root instanceof Map<?,?> obj) || !(obj.get("partiteSquadra") instanceof List<?> rows))
                throw new IOException("Expected partiteSquadra in canonical report: "+report);
            for(Object item:rows) {
                if (!(item instanceof Map<?,?> row))continue;
                Long match=integer(row.get("idIncontro"));
                Long team=integer(row.get("idSquadra"));
                if(match==null||team==null||match<=0||team<=0||team>Integer.MAX_VALUE)continue;
                String link=cleanUrl(row.get("urlTabellinoOnline"));
                if(link==null)continue;
                MatchTeam key=new MatchTeam(match,team.intValue());
                String old=links.putIfAbsent(key,link);
                if(old!=null&&!old.equals(link))
                    throw new IOException("Conflicting canonical scorecard links: "+seasonId+" / "+key);
            }
        }
        return Map.copyOf(links);
    }
    private static Long integer(Object o){
        if(o==null)return null;
        try{return Long.parseLong(o.toString());}catch(NumberFormatException ex){return null;}
    }
    private static String cleanUrl(Object value){
        if(value==null)return null;
        String s=value.toString().trim();
        if(s.isEmpty()||!(s.startsWith("https://")||s.startsWith("http://")))return null;
        return s;
    }
}
~~~~

## `src/main/java/it/alterlega/recordsnext/app/config/TopFlopExistingJson.java`

SHA-256: `93294ab38e258acec74703d8286296e8e3a196b1cc40aae5ad5129c0ec5eb3ff`

~~~~java
package it.alterlega.recordsnext.app.config;

/** Reuse the JSON parser already shipped with RecordsNext 3.2. */
public final class TopFlopExistingJson {
    private TopFlopExistingJson() {}
    public static Object parse(String original) {
        return MiniJson.parse(original);
    }
}
~~~~

## `src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopStageContractSmokeTest.java`

SHA-256: `1b532191a69e0cf6755c4a1271a8010ed3a454c4a59f0cd8f88e0d38c14e4d82`

~~~~java
package it.alterlega.recordsnext.app.topflop;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/** Execute without JUnit; full package tests remain in the existing suite. */
public final class TopFlopStageContractSmokeTest {
    private TopFlopStageContractSmokeTest() {}
    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new IllegalArgumentException("Expected <reportsRoot> <testOutputDir>");
        Map<String,Object> row=new LinkedHashMap<>();
        row.put("name","Nome \"accentato\" à \\ \n test");
        row.put("vote",new java.math.BigDecimal("7.50"));
        row.put("nullable",null);
        String js=TopFlopStageJsExporter.Json.write(row);
        if (!js.contains("\\\"accentato\\\"") || !js.contains("7.5") || !js.contains("null"))
            throw new AssertionError("JSON escaping and decimals failed: " + js);
        if (TopFlopStageJsExporter.MAX_BYTES!=1500000L || TopFlopStageJsExporter.TARGET_BYTES>1000000L)
            throw new AssertionError("Incorrect byte budget");
        var links=TopFlopCanonicalScorecards.load(Path.of(args[0]),"2006_2007");
        String url=links.get(new TopFlopCanonicalScorecards.MatchTeam(271L,7));
        if(url==null || !url.endsWith("/lega2006/ris.htm?Gio=4"))
            throw new AssertionError("Missing original 3.2 historical scorecard: " + url);
        Path testDir=Path.of(args[1]);
        java.nio.file.Files.createDirectories(testDir);
        boolean rejected=false;
        try {
            TopFlopStageJsExporter.shard(testDir,"2099_2100","Detail",
                java.util.List.of("{\"huge\":\""+"x".repeat(1_500_200)+"\"}"));
        } catch (java.io.IOException expected) {
            rejected=expected.getMessage().contains("too large");
        }
        boolean wroteFile;
        try (var files=java.nio.file.Files.list(testDir)) { wroteFile=files.findAny().isPresent(); }
        if(!rejected || wroteFile)
            throw new AssertionError("Oversized single-player shard was not rejected before write");
        System.out.println("TOP_FLOP_STAGE_CONTRACT_OK links2006="+links.size()+
                " maxBytes="+TopFlopStageJsExporter.MAX_BYTES+" oversizedPlayerRejected=true");
    }
}
~~~~

## `tools/Test-RecordsNext4TopFlopJs-v1.py`

SHA-256: `62e2fa6e8d19f47997d1f57317a0673136e99e66629358d0214868f61edec106`

~~~~python
#!/usr/bin/env python3
"""Audit physical JS staging bytes, per-season coverage, player identity, shard navigation.
No database writes, no site publication; works with Python standard library only.
"""
from __future__ import annotations
import argparse
from collections import Counter
import hashlib
import json
from pathlib import Path
import sys

PREFIX='window.fcmRecordsNextTopFlop = '
SHARD_OPEN='.push(...['
SHARD_CLOSE=']);})();'
MAX_BYTES=1_500_000


def parse(jsfile:Path):
    raw=jsfile.read_bytes()
    if len(raw)>MAX_BYTES:raise AssertionError(f'OVER LIMIT {jsfile.name}: {len(raw)}')
    data=raw.decode('utf-8')
    if jsfile.name=='fcmRecordsNext_TopFlop.js':
        if not data.startswith(PREFIX) or not data.rstrip().endswith(';'):
            raise AssertionError('Bad facade assignment')
        return json.loads(data[len(PREFIX):].strip()[:-1])
    start=data.find(SHARD_OPEN)
    end=data.rfind(SHARD_CLOSE)
    if not data.startswith('(function(){') or start<0 or end<start:
        raise AssertionError('Bad shard JS wrapper: '+jsfile.name)
    rows=json.loads('['+data[start+len(SHARD_OPEN):end]+']')
    if not isinstance(rows,list):raise AssertionError('Bad shard payload')
    return rows


def audit(folder:Path)->dict:
    index=parse(folder/'fcmRecordsNext_TopFlop.js')
    if index['maxJsBytes']!=MAX_BYTES or index['status']!='STAGING_ONLY_NOT_PUBLISHED':
        raise AssertionError('Unexpected staging contract')
    declared={'fcmRecordsNext_TopFlop.js'}
    total=Counter();max_bytes=0;links=0
    for season in index['seasons']:
        season_id=season['seasonId']
        for k in ('summaryFiles','detailFiles'):
            declared.update(season[k])
        sums=[];details={};part_counts=Counter()
        for name in season['summaryFiles']:
            if not name.startswith('fcmRecordsNext_TopFlop.Summary.'+season_id+'.'):
                raise AssertionError('Wrong summary season '+name)
            sums.extend(parse(folder/name))
        for name in season['detailFiles']:
            if not name.startswith('fcmRecordsNext_TopFlop.Detail.'+season_id+'.'):
                raise AssertionError('Wrong detail season '+name)
            for item in parse(folder/name):
                key=item['localFcaId']
                if key in details:raise AssertionError('Duplicate detail player '+str(key))
                details[key]=(name,item)
                part_counts[name]+=1
                total['realDays']+=len(item['realDays'])
                total['fantasyContexts']+=len(item['fantasy'])
                for context in item['fantasy']:
                    if context['scorecardUrl'] is not None:
                        if not context['scorecardUrl'].startswith(('http://','https://')):
                            raise AssertionError('Unverified scorecard URL')
                        links+=1
                    if context['competition']=='Youth League':
                        raise AssertionError('Youth League leaked into public detail')
                    if context['competitionRound'] is not None and context['competitionRound']<=0:
                        raise AssertionError('FCM internal round leaked')
        if len(sums)!=season['players'] or len(details)!=season['players']:
            raise AssertionError('Player coverage '+season_id)
        seen=set()
        for s in sums:
            p=s['localFcaId']
            if (s['seasonId'],p) in seen:raise AssertionError('Duplicate summary')
            seen.add((s['seasonId'],p))
            if s['detailFile']!=details[p][0]:raise AssertionError('Wrong lazy detail locator')
            if any(s[k]!=details[p][1][k] for k in ['name','gazzaCode','seasonId']):
                raise AssertionError('Player identity mismatch')
        if sum(len(x['realDays']) for _,x in details.values())!=season['realDays']:
            raise AssertionError('Real day count mismatch '+season_id)
        if sum(len(x['fantasy']) for _,x in details.values())!=season['fantasyContexts']:
            raise AssertionError('Fantasy count mismatch '+season_id)
        seen_contexts=set()
        for _,rec in details.values():
            for f in rec['fantasy']:
                key=(rec['localFcaId'],f['competitionId'],f['matchId'],f['teamId'])
                if key in seen_contexts:raise AssertionError('Duplicated fantasy context '+str(key))
                seen_contexts.add(key)
        total['players']+=len(details)
        total['seasons']+=1
    actual={p.name for p in folder.glob('*.js')}
    if actual!=declared:raise AssertionError('Undeclared / missing files: '+str(actual^declared))
    for f in folder.glob('*.js'):
        max_bytes=max(max_bytes,f.stat().st_size)
    total['files']=len(actual)
    total['scorecardLinks']=links
    total['maxJsBytes']=max_bytes
    total['totalJsBytes']=sum(f.stat().st_size for f in folder.glob('*.js'))
    total['sha256Facade']=hashlib.sha256((folder/'fcmRecordsNext_TopFlop.js').read_bytes()).hexdigest()
    return dict(total)


def main():
    p=argparse.ArgumentParser();p.add_argument('staging_js_dir');p.add_argument('--json-output');a=p.parse_args()
    folder=Path(a.staging_js_dir).resolve()
    summary=audit(folder)
    if a.json_output:
        output=Path(a.json_output)
        if output.exists():raise RuntimeError('Refuse overwrite: '+str(output))
        output.write_text(json.dumps(summary,indent=2,ensure_ascii=False)+'\n',encoding='utf-8')
    print('TOP_FLOP_STAGE_JS_VALIDATE_OK '+json.dumps(summary,ensure_ascii=False))

if __name__=='__main__':
    try:main()
    except Exception as exc:
        print('TOP_FLOP_STAGE_JS_VALIDATE_FAIL '+repr(exc),file=sys.stderr)
        sys.exit(1)
~~~~

## `tools/Invoke-RecordsNext4TopFlopJs-v1.py`

SHA-256: `e72bc610497cc3cc2c73d882c6c225d7cbd5842787abd3cca9795b90ff00ef87`

~~~~python
#!/usr/bin/env python3
"""Conservative, staging-only native TopFlop JS exporter installer.
Works in Windows PowerShell 5.1 via `py -3 this-script.py`.
No CSS, site, publisher, manifest or operational database writes.
"""
from __future__ import annotations
import argparse
from datetime import datetime
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import sqlite3
import subprocess
import sys
import traceback
import zipfile

KIT=Path(__file__).resolve().parent.parent
PATCH=[
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopArchive.java',
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopStageJsExporter.java',
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopCanonicalScorecards.java',
 'src/main/java/it/alterlega/recordsnext/app/config/TopFlopExistingJson.java',
 'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopStageContractSmokeTest.java',
 'tools/Test-RecordsNext4TopFlopJs-v1.py',
 'tools/Create-RecordsNext4WorkingCodeMd-v23.py',
 'tools/Invoke-RecordsNext4TopFlopJs-v1.py',
 'docs/SPECIFICA_EXPORT_JS_TOP_FLOP_v1.md',
 'docs/VERIFICA_EXPORT_JS_TOP_FLOP_v1.md',
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v24.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v23.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v23.md'
]
ARCHIVE='src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopArchive.java'
STAGE_CLASS='it.alterlega.recordsnext.app.topflop.TopFlopStageJsExporter'
TEST_CLASS='it.alterlega.recordsnext.app.topflop.TopFlopStageContractSmokeTest'


def sha(path:Path)->str:
 h=hashlib.sha256()
 with path.open('rb') as inp:
  for part in iter(lambda:inp.read(1048576),b''):h.update(part)
 return h.hexdigest()


def run(args,logs, max_stdout=30000):
 command=[str(x) for x in args]
 proc=subprocess.run(command,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,
                     text=True,encoding='utf-8',errors='replace')
 output=proc.stdout or ''
 logs.append('COMMAND: '+Path(command[0]).name+' (args in installed runner source)')
 logs.append(output[-max_stdout:])
 if proc.returncode:raise RuntimeError('Command failed '+str(proc.returncode)+'\n'+output[-5000:])
 return output


def main()->int:
 p=argparse.ArgumentParser()
 p.add_argument('--project-dir',default=r'D:\DEV_APPS\RecordsNext4.0')
 p.add_argument('--downloads-dir',default=r'D:\DEV_APPS\downloads')
 p.add_argument('--database')
 p.add_argument('--jar')
 p.add_argument('--reports-dir')
 p.add_argument('--seasons',help='Optional comma-separated pilot; default ALL seasons')
 a=p.parse_args()
 root=Path(a.project_dir).resolve()
 downloads=Path(a.downloads_dir).resolve()
 downloads.mkdir(parents=True,exist_ok=True)
 stamp=datetime.now().strftime('%Y%m%d_%H%M%S_%f')
 report=downloads/('RecordsNext4_TopFlopJs_OUTPUT_v1_'+stamp+'.zip')
 build=downloads/('RecordsNext4_TopFlopJs_BUILD_v1_'+stamp)
 staging=root/'data/site-export-staging'/('topflop-stage-v1_'+stamp)/'js'
 state={'status':'ERROR','project':str(root),'stage':str(staging),
        'installed':[],'alreadyIdentical':[],'checks':[], 'historicalCount':None}
 logs=[];changed=[];preexisting={};was_there=set()
 manifest=None
 try:
  if not (root/'pom.xml').is_file() or not (root/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v22.md').is_file():
   raise RuntimeError('Worktree 4.0 with bible v22 not found: '+str(root))
  if (root/'.git').exists() and shutil.which('git'):
   branch=run(['git','-C',root,'branch','--show-current'],logs).strip()
   if branch!='feature/recordsnext4-players':
    raise RuntimeError('Unexpected Git branch: '+branch)
  manifest=json.loads((KIT/'manifest-sha256.json').read_text(encoding='utf-8'))
  if manifest.get('baseArchiveSha256') is None:raise RuntimeError('Invalid kit manifest')
  for rel in PATCH:
   src=KIT/rel;dst=root/rel
   if not src.is_file() or manifest['files'].get(rel)!=sha(src):
    raise RuntimeError('Kit integrity error: '+rel)
   if dst.exists():
    was_there.add(rel)
    if not dst.is_file():raise RuntimeError('Destination is not a file: '+rel)
    if sha(dst)==sha(src):state['alreadyIdentical'].append(rel)
    elif rel==ARCHIVE and sha(dst)==manifest['baseArchiveSha256']:
     preexisting[rel]=dst.read_bytes()
    else:raise RuntimeError('Different existing file, refusing overwrite: '+rel)
  java=shutil.which('java');javac=shutil.which('javac')
  if not java or not javac:raise RuntimeError('JDK 21+ not found in PATH')
  if '21' not in run([javac,'-version'],logs):
   raise RuntimeError('JDK 21 required for this tested build')
  db=Path(a.database) if a.database else next(iter(sorted(
   (root/'data/database').glob('recordsnext-2026_2027-verifica-v1_*.db'))),None)
  if db is None or not db.is_file():
   raise RuntimeError('Updated 2026/27 test SQLite missing: use --database')
  db=db.resolve();db_before=sha(db);state['database']=str(db)
  jar=Path(a.jar) if a.jar else next((x for x in [
   root/'target/RecordsNext.jar',
   root.parent/'RecordsNext2.0/target/RecordsNext.jar',
   root.parent/'RecordsNext2.0/release/RecordsNext_3.2.0_FULL/payload/RecordsNext.jar'
  ] if x.is_file()),None)
  if jar is None:raise RuntimeError('Original 3.2 runtime JAR not found: use --jar')
  reports=Path(a.reports_dir) if a.reports_dir else root.parent/'RecordsNext2.0/data/reports'
  reports=reports.resolve()
  if not reports.is_dir():raise RuntimeError('Official 3.2 normalized reports not found: '+str(reports)+' ; use --reports-dir')
  reports_arg=str(reports)
  state['normalizedReportsRoot']=reports_arg
  with sqlite3.connect(db.as_uri()+'?mode=ro',uri=True) as con:
   tables=[r[0] for r in con.execute("select name from sqlite_master where type='table'")]
  all_seasons=sorted({x[4:13] for x in tables if re.fullmatch(r'raw_\d{4}_\d{4}_fcm_formazione',x)})
  if a.seasons:
   seasons=a.seasons.split(',')
   if not seasons or len(set(seasons))!=len(seasons) or any(x not in all_seasons for x in seasons):
    raise RuntimeError('Invalid --seasons list')
  else:seasons=all_seasons
  if not seasons:raise RuntimeError('No FCM seasons in updated SQLite')
  state['seasonIds']=seasons
  classes=build/'classes';classes.mkdir(parents=True,exist_ok=True)
  replacements={x for x in PATCH if x.startswith('src/main/java/')}
  all_sources=[x for x in (root/'src/main/java').rglob('*.java') if x.relative_to(root).as_posix() not in replacements]
  all_sources.extend(KIT/x for x in sorted(replacements))
  run([javac,'--release','21','-encoding','UTF-8','-d',classes,*all_sources],logs)
  state['checks'].append('Full Java compile OK')
  tests=[root/'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopArchiveSmokeTest.java',
         KIT/'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopStageContractSmokeTest.java']
  run([javac,'--release','21','-encoding','UTF-8','-cp',classes,'-d',classes,*tests],logs)
  output=run([java,'-ea','-cp',classes,'it.alterlega.recordsnext.app.topflop.TopFlopArchiveSmokeTest'],logs)
  if 'checks=52' not in output:raise RuntimeError('52-check regression signature absent')
  state['checks'].append('TopFlop baseline 52 assertions OK')
  output=run([java,'-ea','-cp',classes,TEST_CLASS,reports_arg,build/'oversize_negative_test'],logs)
  if 'TOP_FLOP_STAGE_CONTRACT_OK' not in output:
   raise RuntimeError('New exporter smoke test incomplete')
  state['checks'].append('Canonical 3.2 links and JSON contract test OK')
  # ALWAYS staging only; never pass a real site's js directory.
  output=run([java,'-Xmx1700m','-cp',str(classes)+os.pathsep+str(jar),
      STAGE_CLASS,db,staging,reports_arg,*seasons],logs)
  if 'TOP_FLOP_STAGE_JS_OK' not in output:raise RuntimeError('Exporter signature absent')
  state['checks'].append('Native JS staging export OK')
  validation=build/'validation.json'
  output=run([sys.executable,KIT/'tools/Test-RecordsNext4TopFlopJs-v1.py',
        staging,'--json-output',validation],logs)
  if 'TOP_FLOP_STAGE_JS_VALIDATE_OK' not in output:
   raise RuntimeError('JS content validation incomplete')
  state['historicalCount']=json.loads(validation.read_text(encoding='utf-8'))
  if state['historicalCount']['seasons']!=len(seasons):
   raise RuntimeError('Stage season scope mismatch')
  state['checks'].append('Every JS file <=1,500,000 bytes, lazy indexes and row coverage OK')
  if sha(db)!=db_before:raise RuntimeError('Source SQLite changed during read-only export')
  state['checks'].append('SQLite SHA-256 unchanged')
  backup=root/'backups/topflop-js-exporter-v23'/ARCHIVE
  if ARCHIVE in preexisting:
   if backup.exists() and backup.read_bytes()!=preexisting[ARCHIVE]:
    raise RuntimeError('Conflicting backup for original TopFlopArchive.java')
   if not backup.exists():
    backup.parent.mkdir(parents=True,exist_ok=True)
    with backup.open('xb') as out:out.write(preexisting[ARCHIVE])
  bible=root/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v23.md'
  bible_existed=bible.exists()
  try:
   for rel in PATCH:
    src=KIT/rel;dest=root/rel
    if dest.is_file() and sha(src)==sha(dest):continue
    dest.parent.mkdir(parents=True,exist_ok=True)
    dest.write_bytes(src.read_bytes())
    changed.append(rel)
   run([sys.executable,root/'tools/Create-RecordsNext4WorkingCodeMd-v23.py',
        '--project-dir',root],logs)
  except Exception:
   for rel in reversed(changed):
    dest=root/rel
    if rel in preexisting:dest.write_bytes(preexisting[rel])
    elif rel not in was_there:dest.unlink(missing_ok=True)
   if not bible_existed:bible.unlink(missing_ok=True)
   raise
  state['installed']=list(changed)
  state['checks'].append('Code docs and bible v23 installed, SHA-256 validated')
  state['status']='OK'
  print('ESITO: OK')
  print('STAGING (NON PUBBLICATO):',staging)
  print('JS:',state['historicalCount']['files'],'MAX_BYTES:',state['historicalCount']['maxJsBytes'])
  print('BIBBIA: docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v23.md')
 except Exception as ex:
  state['error']=repr(ex)
  logs.append(traceback.format_exc())
  print('ESITO: ERRORE',file=sys.stderr)
  print(str(ex),file=sys.stderr)
 finally:
  with zipfile.ZipFile(report,'w',zipfile.ZIP_DEFLATED) as out:
   out.writestr('result.json',json.dumps(state,indent=2,ensure_ascii=False))
   out.writestr('execution.txt','\n'.join(logs))
   if (build/'validation.json').is_file():
    out.write(build/'validation.json','validation.json')
   if (staging/'fcmRecordsNext_TopFlop.js').is_file():
    out.write(staging/'fcmRecordsNext_TopFlop.js','fcmRecordsNext_TopFlop.js')
  shutil.rmtree(build,ignore_errors=True)
  print('ZIP DA CARICARE:',report)
 return 0 if state['status']=='OK' else 1

if __name__=='__main__':sys.exit(main())
~~~~

## `tools/Create-RecordsNext4WorkingCodeMd-v23.py`

SHA-256: `3f1ec144aa9fb8e312a5d4e71e19f4452d5a0a2a64c6795014072522730605ae`

~~~~python
#!/usr/bin/env python3
"""Append-only TopFlop code bible v23: exact installed sources and SHA-256."""
from __future__ import annotations
import argparse
import hashlib
from pathlib import Path

PREVIOUS='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v22.md'
DEST='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v23.md'
FILES=[
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopArchive.java',
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopStageJsExporter.java',
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopCanonicalScorecards.java',
 'src/main/java/it/alterlega/recordsnext/app/config/TopFlopExistingJson.java',
 'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopStageContractSmokeTest.java',
 'tools/Test-RecordsNext4TopFlopJs-v1.py',
 'tools/Invoke-RecordsNext4TopFlopJs-v1.py',
 'tools/Create-RecordsNext4WorkingCodeMd-v23.py',
 'docs/SPECIFICA_EXPORT_JS_TOP_FLOP_v1.md',
 'docs/VERIFICA_EXPORT_JS_TOP_FLOP_v1.md',
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v24.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v23.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v23.md'
]

def digest(p):return hashlib.sha256(p.read_bytes()).hexdigest()

def main():
 ap=argparse.ArgumentParser();ap.add_argument('--project-dir',required=True);a=ap.parse_args()
 root=Path(a.project_dir).resolve()
 for rel in [PREVIOUS,*FILES]:
  if not (root/rel).is_file():raise RuntimeError('Required file missing: '+rel)
 lines=['# RecordsNext 4.0 — Bibbia v23 — JS Top & Flop in staging','',
        'Base: RecordsNext 3.2.0. Publisher, GUI, dati e siti operativi invariati.',
        'Bibbia precedente: `'+PREVIOUS+'`',
        'SHA-256 bibbia precedente: `'+digest(root/PREVIOUS)+'`','']
 for rel in FILES:
  p=root/rel
  language={'.java':'java','.py':'python','.md':'markdown'}.get(p.suffix,'text')
  lines.extend(['## `'+rel+'`','','SHA-256: `'+digest(p)+'`','','~~~~'+language,
                p.read_text(encoding='utf-8-sig').rstrip(),'~~~~',''])
 final='\n'.join(lines).rstrip()+'\n'
 target=root/DEST
 if target.exists():
  if target.read_text(encoding='utf-8')!=final:raise RuntimeError('Existing bible v23 differs; no overwrite')
  print('BIBBIA v23: GIA IDENTICA')
 else:
  with target.open('x',encoding='utf-8') as stream:stream.write(final)
  print('BIBBIA v23: OK',target)

if __name__=='__main__':main()
~~~~

## `docs/SPECIFICA_EXPORT_JS_TOP_FLOP_v1.md`

SHA-256: `acab79f4fd55fbc8bd8abb566cf51d5dd34bed30c6f842d89ac2d05a41120659`

~~~~markdown
# Top & Flop — Specifica exporter JS nativo v1

## Schema

- `fcmRecordsNext_TopFlop.js`: facade `window.fcmRecordsNextTopFlop` con metadati e nomi shard per stagione. **Non carica automaticamente** i dettagli.
- `fcmRecordsNext_TopFlop.Summary.<seasonId>.<nnn>.js`: aggiunge righe a `window.fcmRecordsNextTopFlopData.summaries[seasonId]`. Ogni riga: identificatori stagionali, codice storico, nome, recap reale, medie di apporti alle fantasquadre/competizioni, nome esatto dello shard dettaglio.
- `fcmRecordsNext_TopFlop.Detail.<seasonId>.<nnn>.js`: aggiunge righe a `window.fcmRecordsNextTopFlopData.details[seasonId]`, ciascuna con giornate FCA (voto, stato, eventi sorgente non-zero) e contesti FCM (squadra, competizione, famiglia, giornata competizione, giornata Serie A, schieramento, uso effettivo, voto tabellino, fantavoto, eventi FCA conteggiati, link tabellino canonico se presente).

**Navigazione browser:** caricare facade, i soli shard Summary della stagione interessata, selezionare un calciatore, quindi caricare solo il suo `detailFile`. Per carriera multipla iterare stagioni senza unificare automaticamente codici storici ambigui.

## Dimensioni e pubblicazione

- Target massimo consigliato 950.000 byte; **limite di sicurezza 1.500.000 byte reali** su ogni JS (UTF-8). Nessuna riga giocatore può essere spezzata: se troppo grande, l'export fallisce invece di troncarla.
- Nomi di file **flat**, preparati per la cartella `js` dei siti. Non esistono sottocartelle JS.
- Questa fase scrive solo in `data/site-export-staging/topflop-stage-v1_<timestamp>/js` nel worktree 4.0. I file non sono ancora compatibili con la validazione di conteggio/prefissi del publisher esistente fino all'integrazione prevista.
- Per pubblicare serviranno estensioni coordinate delle classi esistenti `RecordFamily`, `CoreRecordCatalog`, `ProcessingOptions`, `RecordsNextPipeline`, `Records2026SitePublisher`, `SeasonFamilyShardPublisher`, manifest e GUI, senza bypass e con cutoff multisito e rollback.

## Dati, provenienza e limiti

- La fonte dei fatti è `TopFlopJdbcArchiveAssembler` che riusa il RAW FCM/FCA importato dalla 3.2 e i round canonici di `rn_event`.
- Il `scorecardUrl` proviene SOLO dai `season_normalized_*.json` 3.2 per `idIncontro` + `idSquadra` quando esiste `urlTabellinoOnline` valido. Non costruire URL da `matchId` o `serieARound`.
- I report 2026/27 aggiornati non sono ancora disponibili: link `null`, senza fallback inventati. Si risolvono dopo la normalizzazione 3.2 della stagione aggiornata.
- Youth League non viene esportata. Spareggio Serie B, Play Off - Play Out sono CAMPIONATO.
- Lo stesso gol reale può apportare un gol in due incontri fantasy distinti (coppa/campionato): carriera reale deduplicata per giornata; carriera fantasy conteggia gli incontri distinti.
- Le medie individuali sono derivate dai soli casi `Usage.COUNTED` verificati nei tabellini; per `OUTSIDE_LIST`/`BENCH_UNUSED` non si attribuisce automaticamente un'esclusione certa. Periodi di tesseramento e quattro indici voto sprecato ancora da completare.
- `CodiceGazza` non è una chiave globale infallibile per tutti gli anni. Le righe mantengono sempre `seasonId` + `localFcaId` e codice storico.
- La proiezione JS compatta conserva gli eventi non-zero (senza duplicare i 3 quotidiani) mentre **tutti gli originali** restano accessibili nel database SQLite e nei fatti Java.

## Test obbligatori

1. Compilazione Java 21, regressione modello 52/52 e test parser/link canonici.
2. Contabilità righe FCA/FCM e anagrafiche per ogni stagione.
3. Unicità scheda calciatore (stagione + ID locale) e chiave FCM (calciatore, competizione, incontro, squadra), risoluzione `detailFile`.
4. Limite byte su TUTTI i `.js`, non solo sui dettagli; parser JSON del payload e caricamento come JS.
5. Controllo SHA-256 database originale invariato.
6. Nessuna pubblicazione reale; pipeline 3.2 invariata.
~~~~

## `docs/VERIFICA_EXPORT_JS_TOP_FLOP_v1.md`

SHA-256: `f0d0965faaeba513e6c637a466c7f7ccce1be5df48b0df4accebb1871b511c85`

~~~~markdown
# Verifiche esportatore JS Top & Flop — fase di staging v1

Stato: verificato in ambiente di sviluppo su copie ricevute, attesa validazione Windows nel worktree 4.0.

- JDK 21: compilazione completa di sorgenti 3.2 + codice Top & Flop.
- Due stagioni: 2006/07 e 2026/27 con dati reali, 1419 giocatori stagionali, 53922 giornate reali, 13700 contesti FCM, 17 JS, file massimo 949798 byte.
- Link 2006/07: 6050 contesti con scorecard URL preso dai report normalizzati 3.2 e validato per match/team; 2026/27: nessun report aggiornato disponibile, link correttamente non risolti.
- Test 21 stagioni, senza report normativi: 16262 giocatori stagionali, 617956 giornate, 554425 contesti fantasy ammessi, 389 JS, massimo 949991 byte. (Il numero di file cambia con URL disponibili, e questo è normale.)
- Verifica parser su tutti i file JSON-wrapped: conteggi e identità esatti, nessuna perdita/duplicazione dei contesti, nessuna Youth League, dimensioni inferiori a 1.500.000 byte.
- Prova integrale con tutti i report normalizzati 3.2: 21 stagioni, 430 JS, 546101 collegamenti canonici trovati, massimo 949991 byte e 372471522 byte complessivi nello staging; 8324 contesti senza link, non sintetizzati.
- Test negativo sul budget: una riga giocatore sintetica >1500000 byte viene respinta prima della scrittura.
- Test browser con Node `vm`: facade -> shard Summary -> dettaglio specifico, URL storico 2006/07 corretto, 2026/27 link non inventato.
- Nessun file pubblicato, nessun CSS/GUI/publisher modificato.

Il test Windows locale produrrà conteggi e `result.json` aggiuntivi; non dichiarare la fase completamente convalidata finché non si riceve e verifica lo ZIP diagnostico.
~~~~

## `docs/PIANO_RECORDSNEXT4_GIOCATORI_v24.md`

SHA-256: `63e8addbe73a33d6e727d56f1e325b1b79ac9d7966d488aafdd17fd8bc1b2676`

~~~~markdown
# Piano RecordsNext 4.0 v24 — JS Top & Flop, staging isolato

**Obiettivo operativo di questa revisione:** generazione JS nativa con budget rigoroso per ogni file, indice e dettagli caricabili su richiesta. Non è ancora attivata nella GUI, nella pipeline di pubblicazione né nel sito.

**Stato test:** Java compilato, 52 test preesistenti e prove dati reali in Linux. Esecuzione su Windows da verificare tramite ZIP diagnostico.

**Passaggi per il rilascio 4.0, in ordine:**
1. [x] Modello FCA/FCM, contribuzioni e giornate canoniche dalla 3.2.
2. [x] Exporter Java nativo in STAGING, 21 stagioni testate, file < 1.500.000 byte, indice per stagione e dettagli lazy; link solo dai report canonici.
3. [ ] Interfaccia Top/Flop/Incompresi, scheda giocatore e demo HTML ReNewo, utilizzando le quattro skin funzionanti.
4. [ ] Completamento periodo tesseramento, esclusioni certe, quattro indicatori sprecato, record Top/Flop definitivi (nessuna classificazione senza evidenza).
5. [ ] Registrazione Catalogo/ProcessingOptions/GUI, generazione JS nell'attuale flusso FULL/CONSOLIDATED, validazioni del publisher, manifest, cutoff multisito, rollback; NON bypassare le guardie della 3.2.
6. [ ] Test locali Windows dell'intera pipeline e dei visualizzatori; packaging, bibbia e release 4.0.0.

**Regola vincolante:** non pubblicare gli output di staging finché l'integrazione nel publisher 3.2 non sia completata e verificata. Dimensione ideale per singolo JS <= 1.000.000 byte; limite bloccante 1.500.000 byte fisici.

---

# RecordsNext 4.0 - Piano operativo v23

Data: 2026-11-06. Implementazione nativa dell'apporto registrato nei tabellini.

**Verificato nel mio ambiente**: compilazione Java 21 completa, regressione 52 test, analisi 2006/07 e 2026/27, CSV reale Lautaro per fantasquadra/competizione, evento `GOLFATTI1` senza triplicazione 1/2/3. **Da confermare** con runner su Windows.

**Già implementato in Top & Flop**: lettori FCA/FCM, archivio Java stagionale, giornata competizione canonica 3.2, nuovo riconoscimento dei contributi dai primi 11 slot con `TOTALE` numerico, riepilogo nativo per squadra/competizione/famiglia, dettaglio originale degli eventi.

**Passaggi per concludere senza deviazioni**: (1) completare disponibilità storica per giornata con le operazioni confermate e le esclusioni effettive; (2) calcolare le quattro metriche Incompresi; (3) creare l'exporter familiare nativo usando `RecordsNextPipeline`, `Records2026SitePublisher`, `SeasonFamilyShardPublisher` e risoluzione URL 3.2, con shard e manifest; (4) HTML completo, ricerca carriera, skin ReNewo, GUI; (5) test FULL/CONSOLIDATED e multisito, documentazione e release.

Non chiamare "pronto per il sito" un CSV di verifica: il publisher originale della 3.2 non è ancora stato modificato.

---

# Piano v20 — Demo Top & Flop e compatibilità storica


**Stato 2026-11-02.** Implementata una demo reale ma circoscritta a 2006/07 e 2026/27. Conservare pipeline e publisher 3.2; l'integrazione nativa non è ancora eseguita. Vedere `SPECIFICA_DEMO_TOP_FLOP_RECORDSNEXT4_v1.md`.

**Via alla conclusione:** disponibili HTML, CSS 3.2 con scelta skin, facade e shard campione; prossime tappe: stato tesseramento giornaliero, metriche, catalogo, exporter nativo, manifest/GUI, demo finale e deploy di test. Nessun rilascio 4.0 finché gli shard completi all-time non sono pubblicabili con la 3.2.

---

# RecordsNext 4.0 - Piano di continuita v19

# RecordsNext 4.0 - Piano v17: collegamento FCA JDBC al modello Top & Flop

Data 2026-11-01. Prima connessione JAVA di sola lettura ai raw FCA importati dalla 3.2; 21 stagioni verificate sul DB principale e sul sandbox, che differiscono nel 2026/27. Non attivare la famiglia ne modificare la pipeline 3.2.

**Fatto in questa fase**: modello `TopFlopFcaJdbcReader` con profilo giocatore, anagrafica, voti e eventi originali, un fatto per giornata FCA; test Java su due DB e contatori auto-calibrati. **Da fare**: collegamento FCM (partita, fantasquadra, competizione, scorecard canonica, uso titolare/panchinaro/escluso), validazione piena degli eventi, tesseramento temporale, storico e statistiche aggregate, catalogo/exporter/GUI/JS.

**Regola di sicurezza**: gli stessi test possono trovare 616018 giornate nel sandbox e 617310 nel principale; e' una differenza di snapshot, non motivo per riscrivere i database.


---

# RecordsNext 4.0 - Piano v16: implementazione Java Records Top & Flop

**Decisioni precedenti preservate.** Nuova tappa: modello canonico Java `app/topflop` con real day, roster snapshot, fantasy match, politiche di competizione e indicatori base. Test Java 21 e bibbia v15. Non attivare la famiglia `TOP_FLOP` finche pipeline/JS/GUI/manifest/shard non funzionano.

**Prossima fase:** adattatore Java di sola lettura ai raw FCA/FCM per l'archivio individuale completo (compresi voti, fantavoti, eventi e prove di utilizzo), con confronto sui 21 archivi; solo dopo: motore/archiviazione/catalogo/exporter/publication 3.2. Rimangono aperte la semantica delle esclusioni non in `Formazione`, il fantavoto dell'escluso, i codici storici riutilizzati, soglie e classifica definitiva.

---

# RecordsNext 4.0 - Piano operativo v15

# RecordsNext 4.0 - Piano operativo v14

# RecordsNext 4.0 - Continuita documentale PIANO_RECORDSNEXT4_GIOCATORI_v13

Versione additiva del 2026-11-01. La cronologia integrale precedente e preservata sotto.

# RecordsNext 4.0 - Piano operativo v11

Data: 2026-11-01. Base produttiva 3.2.0 immutata. Branch `feature/recordsnext4-players`; commit di riferimento `f9bf30323d9204d70e91e242bddfc72c2060ecf9`.

## Fasi completate e evidenze

- Fase 0: identita e fonti; eccezioni sui codici storici ancora aperte.
- Fase 1: estrattore FCA sperimentale; output Windows verificato, 7 test. Non integrato nel Java.
- Fase 2: estrattore FCM fantasy sperimentale; output Windows verificato, 8 test. Non integrato nel Java.
- Fase 3A: audit stati `Formazione`, output Windows verificato, 9 test.
- Fase 3B1: audit `Tabellino.LISTA` + `Formazione`, output Windows `RecordsNext4_Fase3B1_OUTPUT_v1_20261101_001744_607896.zip` verificato: 7 test, 21 stagioni, 22.265 tabellini, 552.025 righe `Formazione`, tre SHA-256 dei report corretti, sandbox invariato, bibbia v8 generata. Non equivale a prova definitiva di voto conteggiato.
- Fase 3B2: censimento osservazionale delle regole `Competizione` per stagione e competizione, legame `Incontro.IDGIRONE -> Girone.IDCOMPETIZIONE`, moduli ammessi e tabellini osservati. Strumento prototipo con 8 test locali; lo ZIP Windows deve ancora certificarne l'esecuzione sul PC.

## Passaggi successivi

- Fase 3B3: confrontare configurazioni di competizione, tabellini reali, RU, sostituzioni, eventuali voti speciali e regole per ruolo. Stabilire cosa significa realmente `conteggiato`, `disponibile`, `non sfruttato`, `recuperabile`; distinguere prove osservate, inferenze e casi non simulabili.
- Fase 3C: solo dopo approvazione della semantica: prototipo per metriche individuali fantasy (panchina non impiegata, fuori formazione, sostituzioni, eventuali controfattuali), test su casi reali verificati.
- Fase 4: integrare fonti canoniche in SQLite/Java, esportazione JS stagionale/sito, HTML e GUI. Tutte le componenti richieste fanno parte del traguardo 4.0.0.
- Fase 5: regressioni, documentazione, commit, release 4.0.0.

## Vincoli costanti

1. Prestazione FCA unica per giocatore storico, stagione, giornata: non duplicare se usata in competizioni fantasy diverse.
2. `GiocatoreA.ID` e annuale; `CodiceGazza` contiene il codice storico nella FCA. Le collisioni storiche non sono risolte automaticamente.
3. I codici speciali 0/-1 in `Tabellino.LISTA` non sono calciatori.
4. `ENTRATO`, `ENTRATORISERVA` e posizione nei primi undici slot non bastano, isolatamente, a certificare un voto conteggiato.
5. Il regolamento va ricavato per stagione e competizione, senza assumere tre sostituzioni ovunque. Un archivio FCM e uno snapshot delle impostazioni, non prova di eventuali variazioni avvenute durante la stagione.
6. Conservare integralmente le analisi RU personali della 3.2; non inserirle nei record pubblici senza decisione espressa.
7. Tabellini non collegati, incontri con squadra 0 e riposi: casistiche distinte, nessuna cancellazione.
8. Nessuna scrittura nel DB operativo/sandbox, nessuna modifica a Java, pipeline, siti, GUI, manifest o branch principale durante gli audit.
9. I file nuovi vanno in `D:\DEV_APPS\RecordsNext4.0`, i risultati in `D:\DEV_APPS\downloads`, con nomi versionati.
10. Ogni fase mantiene piano, registro decisioni, diario, specifiche, test e bibbia incrementale con codice integrale e SHA-256. Le versioni pregresse restano intatte.


## Aggiornamento dopo verifica Windows 3B2 e decisione sul record individuale

- 3B2 completata anche su Windows (`ESITO: OK`, 8/8 test): audit documentale delle regole. Non sono state apportate modifiche al database o al motore Java.
- Priorita della fase successiva: **voti non utilizzati dal singolo calciatore**. Niente simulazione del punteggio di fantasquadra e niente ottimizzazione delle sostituzioni.
- Dati minimi da raccogliere per un calciatore: codice storico, ID locale FCA, stagione, fantasquadra, partita e competizione fantasy, giornata reale, indicatore di effettivo utilizzo nei primi 11 del tabellino, presenza di voto reale valido, valore sorgente del voto, riferimento al tabellino.
- Metrica stabilita: somma dei soli voti reali validi nelle esclusioni / **tutte le esclusioni**; conservare separati i due conteggi, non dividerli fra loro.
- Implementazione successiva: un campione reale verificato a mano, poi test automatici sul campione, quindi calcolo storico. Registrare i casi ambigui anziche correggerli arbitrariamente.
- Il campo "valore massimo" menzionato dall'utente rimane **aperto** (nessun cap numerico assunto).
- Continuano a valere l'identita per `CodiceGazza` con verifica collisioni, l'ID FCA locale, l'assenza di duplicazioni delle prestazioni reali e la conservazione dei record precedenti.


## Nuovo assetto approvato per il record individuale (2026-11-01)

- Conservare i quattro indicatori con due numeratori distinti (voto reale valido, fantavoto regolamentare completo) e due denominatori distinti (esclusioni totali, esclusioni con voto valido).
- La Fase 3C si articola in: (a) campioni di esclusioni FCM/FCA con provenienza voto/fantavoto e bonus-malus; (b) audit di tesseramento e partecipazione effettiva per stabilire `E` e `P`; (c) calcolo su casi reali e confronto con tabellino, senza simulare cambi di formazione.
- La stessa prestazione FCA resta un solo fatto reale; un'esclusione e invece un fatto fantasy legato a stagione, fantasquadra, competizione e incontro. Non aggregare ciecamente incontri di competizioni parallele.
- Implementare prima unit test sintetici per i quattro indicatori (v2); i test sintetici NON dimostrano ancora la classificazione delle esclusioni negli archivi.
- Se `E > 0, P = 0`, gli indici pesati valgono zero; le medie sulle gare valide sono non definite. Se `E = 0`, tutte le metriche sono non definite.
- Le RU non sono calciatori e non devono generare un voto individuale sprecato; le regole di sostituzione per competizione restano variabili e indipendenti da questo calcolo.
- Nessun limite artificiale al voto o fantavoto; la voce 'massimo' resta proposta separata e non adottata come soglia.


## Nuova fase 3C0 - Modello archivio completo e audit contesti paralleli

- Definita specifica `SPECIFICA_ARCHIVIO_COMPLETO_GIOCATORE_v1.md` con livello reale, tesseramento e livello evento fantasy, valido anche per titolari.
- Documentate due famiglie, competizioni singole e sovrapposizioni nella stessa giornata; conservazione dei dati originali prima dei filtri.
- Audit `Audit-RecordsNext4Contesti-v1.py` READ-ONLY: report per competizione, copertura, giocatore-squadra-competizione, sovrapposizioni e caso 100843.
- **Prossimo sviluppo**: storicizzare il tesseramento e definire le assenze da Formazione in modo dimostrabile; quindi costruire il dataset sorgente completo e l'estrazione simmetrica voti/fantavoti per utilizzati ed esclusi.
- La verifica Windows del nuovo runner rimane pendente; non toccare Java/GUI/sito.

## Aggiornamento v14 - fase 3C1

- Validazione Windows della 3C0: ZIP `RecordsNext4_Contesti_OUTPUT_v1_20261101_004840_254541.zip`, stato OK, 10 test, 21 stagioni, 552025 righe, SHA256 sandbox uguale prima/dopo, bibbia v12 presente. Non ancora dataset canonico.
- Classificare Play Off - Play Out e Spareggio Serie B CAMPIONATO; escludere Youth League da ogni output record/scheda.
- Audit v2: verificare nuovamente le 21 stagioni, separare originali/ammessi/esclusi, non eliminare dati sorgente.
- Prossima implementazione: associare a ogni evento fantasy la partita FCM e la `scorecardUrl` canonica della stagione corretta, verificata sui file normalizzati 3.2. URL non verificati restano null.
- Solo successivamente generare il dataset completo dei giocatori in Java/SQLite e i record con prova del singolo episodio.


## Aggiornamento v15 - Records Top & Flop come famiglia nativa

**Decisione vincolante dell'utente (2026-11-01):** tutti gli audit dei giocatori delle fasi 0-3C1 sono lavori preparatori di una nuova famiglia nativa **Records Top & Flop**, integrata in RecordsNext 4.0. La pipeline 3.2.0 resta la base esclusiva da estendere. Non costruire sistemi di export, pubblicazione, configurazione, manifest o visualizzazione paralleli.

**Stato attuale:** 3C1 eseguita su Windows, ZIP `RecordsNext4_Contesti_OUTPUT_v2_20261101_005413_578565.zip` verificato: stato OK; 13 test; 21 stagioni; 552025 righe origine, 546775 ammesse, 5250 Youth League escluse; SHA256 database sandbox prima=dopo; bibbia v13 generata. Non e ancora implementata una famiglia Java Top & Flop.

**Ordine vincolato di implementazione**, rispettando la 3.2:
1. Definire fatti canonici: giocatore storico + prestazione FCA unica e contesti per fantasquadra, incontro, competizione; provenienza del voto e dei bonus/malus; link canonico tabellino. Produrre test sui casi 100843, competizioni parallele, Youth League e anomalie.
2. Implementare una sola volta in Java/SQLite i fatti riutilizzabili da scheda giocatore e records; mantenere gli audit Python come verificatori, non produttori operativi permanenti.
3. Inserire `TOP_FLOP` nel catalogo (`RecordFamily`, `CoreRecordCatalog`, dipendenze, selezione e configurazione), senza modificare valori preesistenti e mantenendo la lettura delle configurazioni 3.2.
4. Collegare il calcolo alla `RecordsNextPipeline` con logiche FULL/CONSOLIDATED e archivio storico separato per stagione; eseguire regressione su output invariati delle altre famiglie.
5. Collegare `TopFlopFamilyJsExporter` a `Records2026SitePublisher`, relativa validazione, manifest, `SeasonFamilyShardPublisher`, publisher multisito e link tabellini canonici. Nessun nuovo flusso di deploy.
6. GUI: aggiungere selezione famiglia e singoli records in `RecordsNext2Dashboard` / configurazione vigente. HTML sito: nuova sezione e schede individuali derivate dagli stessi dati senza duplicazione dei calcoli.
7. Eseguire test integrati: nessun impatto su famiglie preesistenti, non oltre 1.5 MiB per shard, siti storici corretti, FULL e CONSOLIDATED, JS valido, nessuna Youth League. Aggiornare bibbia e rilasciare come 4.0.0 soltanto dopo approvazione.

La classificazione CAMPIONATO/COPPE e distinta dalla classificazione **famiglie di record**: `TOP_FLOP` e una famiglia records; CAMPIONATO/COPPE sono filtri del dominio sportivo. Non confonderle nel codice.

**Prossimo passo di implementazione:** realizzazione nel Java 4.0 del modello dati canonico individuale e test di regressione, senza ancora selezionare/pubblicare la famiglia finche il suo calcolo non esiste.


## Aggiornamento v18 - lettore Java FCM JDBC

- Output Windows `RecordsNext4_FcaJdbc_OUTPUT_v1_20261101_205632_731016.zip` verificato, `stato=OK`, compilazione e 52 verifiche; letti tre campioni per database, hash DB invariati, bibbia v16.
- Additivo `TopFlopFcmJdbcReader`: contesti Formazione/Incontro/Girone/Competizione/Tabellino/FCA/Team e Tesserato snapshot, tutti in sola lettura; controlli integrita sui 21 anni.
- Non usare IDGIORNATA come giornata progressiva; non attribuire esclusioni da assenze; Youth League solo nel conteggio tecnico.
- Successivo: collegamento tra lettori FCA/FCM e archivio dei fatti `TopFlopArchive`, risoluzione URL tramite 3.2, voto/fantavoto e bonus/malus verificati, poi classificazione degli stati. Solo dopo famiglia record selezionabile e pubblicabile.
- Java/GUI/publisher/DB originari della 3.2 rimangono immutati.


## Fase 4A - Assemblatore Java FCA + FCM (2026-11-02)

- Da `TopFlopFcaJdbcReader.loadSeason` creare l'unico archivio `RealDay` per calciatore/giornata FCA; poi collegare `TopFlopFcmJdbcReader.loadSeason` tramite lo stesso `PlayerSeason` e `RealDayKey`, mantenendo i contesti fantasy per team/competizione/incontro senza duplicare i dati reali.
- Non sostituire gli ID FCA annuali con CodiceGazza durante le JOIN; CodiceGazza e' un riferimento storico da verificare in caso di collisioni.
- `Tesserato` puo' avere piu righe per lo stesso giocatore/team/stagione (es. venduto e successivamente riacquistato). Conservare **tutte** le righe con l'ID sorgente nella vista `LoadedSeason.rosterEvents`, oltre alla vista riepilogativa univoca di `TopFlopArchive`. Non datare il tesseramento per inferenza.
- Il `Tabellino.Tot` inizia a essere disponibile come valore numerico solo quando il token originale e' numerico (virgola o punto); `sv/ng`, null e testi speciali rimangono senza fantavoto numerico, con stringa originale intatta.
- `Formazione.POS=0` registra il titolare iniziale, ma gli undici slot del tabellino non costituiscono una certificazione indipendente di contribuzione regolamentare; nessun `EXCLUDED_CONFIRMED` prodotto in automatico.
- I link scorecard rimangono null finche non sono ricavati dal normalizzatore/publisher della 3.2, preservando la corretta stagione storica.
- Le Riserve d'Ufficio, i coefficienti bonus/malus e la validita temporale dei tesseramenti non vengono simulati. Youth League continua a essere esclusa.
- **Sviluppo non abilitato** nel catalogo record, pipeline FULL/CONSOLIDATED, GUI o publisher. Prima completare e verificare la costruzione canonica e l'utilizzo dati su un'intera stagione, poi integrare nella famiglia esistente.
- Test pilota: 2006/07 (riacquisti), 2023/24 (Youth), 2025/26 (Lautaro in squadre multiple), 2026/27 (stagione senza formazioni) e ulteriori stagioni tramite test selettivi. Controlli per entrambi i DB con conteggi relativi alla sorgente, non valori assoluti fissati per 2026/27.


## Precisazioni snapshot aggiornato 2026/27 (2026-11-02)

- Serie A: 9 giornate concluse; AlterLega: giornate REALI 3-9, confermato dall'utente. Nessuna partita fantasy attesa nelle prime due giornate.
- Integrato test dello snapshot RAW 3.2 con 28.956 righe FCA, 7.650 formazioni FCM, 813 tesseramenti.
- La cronologia dei movimenti `EntryInBilancio` e' fonte da conservare, ma non e' ancora una tabella di finestre di possesso dimostrate.
- Pacchetto fase 4A = solo assemblatore read-only e test; disabilitata famiglia nella GUI/publisher finche' mancano record e output nativi.


---

## Addendum correttivo demo v2 — 2 novembre 2026

Installazione della demo Top & Flop con skin originali riutilizzate. Prima prova Windows v1 arrestata preventivamente sul CSS neutral diverso. Runner v2: non altera i CSS delle quattro skin esistenti, installa quelli assenti, verifica gli hash; documenta v20 della bibbia. Demo ancora limitata a due stagioni e non pubblicabile.

## Aggiornamento v22 — Giornata competizione canonica (2026-11-06)

- Database aggiornato 2026/27: **559.675** righe Formazione complessive nelle 21 stagioni; le **552.025** precedenti si riferivano allo snapshot antecedente l’importazione di 7.650 formazioni 2026/27.

- **Implementato e testato su copia:** `TopFlopCanonicalRoundResolver` legge `rn_event.competition_round` e non `FCM.Incontro.IDGIORNATA` come numero di giornata; l'assemblatore conserva sia la descrizione originaria FCM sia l'ID sorgente tecnico in campi separati.
- `FantasyFact.fantasyRound` può essere `null` quando non esiste un incontro nella mappatura canonica della 3.2. Nessun numero inventato e nessun contesto FCM scartato per questo motivo.
- **Eccezione verificata:** stagione 2022/23, 50 formazioni in Spareggio Serie B e 50 in Vera finale di Coppa tra le Coppe: 3.2 le marca `ESCLUSA`; Top & Flop le conserva come fonte, con giornata canonica non disponibile. I 5.250 contesti Youth League rimangono esclusi.
- Test: compilazione Java 21 + 52 smoke checks + lettura di 21 stagioni `rn_event` + assemblaggio 2022/23 e 2026/27 aggiornati.
- **Ancora da completare:** motore record nativo, calcolo apporto effettivo e tesseramenti giornalieri, JS integrati nel publisher 3.2, HTML finale con scheda giocatore e skin Renewo, GUI, verifica multistagione di tutti gli output e rilascio 4.0.0. Nessuna pubblicazione automatica.
~~~~

## `docs/REGISTRO_DECISIONI_RECORDSNEXT4_v23.md`

SHA-256: `f8367bb4da39b9d66d8701694e09853a6fd337a43760d389d31568dec9c20cf7`

~~~~markdown
# Registro decisioni RecordsNext 4.0 v23 — Export JS Top & Flop

| ID | Stato | Decisione / conseguenza |
|---|---|---|
| D-172 | APPROVATA | Ogni file JS pubblico di Top & Flop deve essere idealmente <=1 MB; **mai >1.500.000 byte reali**. Validazione preventiva e blocco dello staging se eccede. Nessun troncamento di righe. |
| D-173 | IMPLEMENTATA IN STAGING | Esportazione tramite `TopFlopStageJsExporter` nella stessa base Java 3.2: facade leggera, riepiloghi per stagione e shard di dettaglio per calciatore. Ogni riga di riepilogo indica direttamente il file di dettaglio da caricare. |
| D-174 | IMPLEMENTATA IN STAGING | Prestazioni reali una sola volta per `(season, FCA.ID, Serie A day)`; contesti fantasy distinti per competizione, incontro, fantasquadra. Nessuna fusione automatica per `CodiceGazza` in presenza di collisioni storiche. |
| D-175 | IMPLEMENTATA IN STAGING | Riepiloghi di apporto solo per slot conteggiati FCM già verificati. Somme gol su rigore incluse nei gol Gazzetta fonte 1: nessuna duplicazione negli aggregati. Esclusioni senza prova restano escluse dai record. |
| D-176 | IMPLEMENTATA IN STAGING | Link ai tabellini copiati SOLO dai `season_normalized_*.json` della 3.2, indicizzati per incontro e fantasquadra; nessun URL costruito dalla giornata/ID interno. Se non esistono report (es. RAW 2026/27) il link resta `null`. |
| D-177 | IMPLEMENTATA IN STAGING | Gli originali integrali rimangono nel SQLite e nei fatti Java; nella proiezione JS si eliminano solo zeri ripetitivi e i campi di origine non necessari alla visualizzazione, senza perdere eventi non nulli o voti. |
| D-178 | REGOLA DI PROTEZIONE | La 3.2 operativa, gli HTML/CSS e la GUI restano immutati. Non copiare gli shard nel target 3.2 senza aggiornare conteggio/prefissi/manifest/publisher/rollback/scope e test. |
| D-179 | APERTA | Le esclusioni dei calciatori non presenti in `Formazione` richiedono cronologia del tesseramento verificata per giornata; non attribuire prestazioni non sfruttate senza prova. |
| D-180 | APERTA | Integrazione familiare `TOP_FLOP` nel catalogo 3.2, generazione FULL/CONSOLIDATED, cutoff storico, visualizzatori HTML ReNewo e scheda calciatore. |
| D-181 | TEST SU COPIA COMPLETATO; WINDOWS DA CONFERMARE | Compilazione Java completa e verifica dei JS su database updated 2026/27 e 21 stagioni, bilancio conteggi ed hash SHA-256 database. |

---

# RecordsNext 4.0 - Registro decisioni v22 - Apporto registrato dal tabellino

Data: 2026-11-06. Revisione ADDITIVA alla v21, preservata integralmente sotto.

| ID | Stato | Decisione / verifica |
|---|---|---|
| D-165 | IMPLEMENTATA NEL NUOVO CODICE, PROVA WINDOWS PENDENTE | `Usage.COUNTED` per formazione presente nei primi 11 slot di `Tabellino.LISTA`, incontro `GIOCATO=1`, `Tabellino.TOT` numerico. È l'apporto *registrato dal tabellino FCM*, non una simulazione. I flag `ENTRATO` e `ENTRATORISERVA` restano separati. |
| D-166 | IMPLEMENTATA | Le prestazioni in panchina con totale numerico ma oltre i primi undici slot non entrano nei totali **apportati**. Le posizioni ambigue restano `UNKNOWN`, nessun azzeramento o attribuzione inventata. |
| D-167 | IMPLEMENTATA | Aggregazioni per `PlayerSeason`, fantasquadra, famiglia CAMPIONATO/COPPE, competizione, una volta per incontro fantasy. Prestazione reale unica nell'archivio FCA. Il codice storico non unisce automaticamente omonimie/collisioni. |
| D-168 | IMPLEMENTATA | Per la media voto reale usare `FCA.Punteggio.Voto1` e conservarne distinto il valore `FCM.Tabellino.Voto`. Per la fantamedia apportata usare `FCM.Tabellino.Tot` effettivamente registrato; nessun coefficiente inventato. |
| D-169 | IMPLEMENTATA | Eventi apportati sono quantità originali FCA associate SOLO a contesti `COUNTED`. Le tre fonti di `GOLFATTI1/2/3` non si sommano. Normalizzare per visualizzazione solo la fonte 1 (Gazzetta); `GOLFATTISURIGORE1` è una sotto-categoria dei gol, non una somma aggiuntiva. Conservare anche tutte le colonne grezze. |
| D-170 | APERTA / DA VERIFICARE IN LOCALE | Completamento esclusioni certe e periodi di tesseramento; nessun dato fuori `Formazione` trasformato automaticamente in esclusione. Le 4 formule dei voti sprecati restano separate dal contributo verificato. |
| D-171 | APERTA | Collegare questo modello al catalogo `TOP_FLOP`, all'exporter e agli shard flat della pipeline 3.2, con manifest, GUI e HTML ReNewo. Il CSV ora è solo verifica dei dati Java, NON il publisher finale. |

---

# Registro decisioni v19 — Demo integrativa


| ID | Stato | Decisione |
|---|---|---|
| D-158 | APPROVATA | Records Top & Flop è famiglia **nativa** dei records RecordsNext; stesso motore e stesso publisher 3.2, non secondo programma. |
| D-159 | APPROVATA | HTML demo con selezione skin e layout RecordsNext; profilo neutral ispirato a ReNewo, oltre alle altre skin 3.2. |
| D-160 | APPROVATA | Dati JS nella `js` del sito, con facade, shard flat per stagione, cutoff storico, rollback e link canonici; nessun URL inventato. |
| D-161 | APPROVATA | Varela: se fra cessione e reinserimento non passa una giornata di reale assenza dalla rosa, appartenenza continua. |
| D-162 | IMPLEMENTATA SOLO DEMO | Preview 2006/07 + 2026/27, nomi JS e limiti coerenti 3.2; nessuna abilitazione catalogo, GUI o publisher. |
| D-163 | APERTA | Strategie per shard di stagioni oltre 1536 KiB: non accettare troncamenti; usare estensione controllata publisher. |
| D-164 | APERTA | Calcolo esclusioni certe e loro quattro metriche: nessuna inferenza da sola mancanza in formazione o tesseramento snapshot. |

---

# RecordsNext 4.0 - Registro decisioni v18

# RecordsNext 4.0 - Registro decisioni v16

| ID | Stato | Decisione / evidenza |
|---|---|---|
| D-124 | IMPLEMENTATA E VERIFICATA | L'adattatore FCA Top & Flop usa JDBC SQLite e le **stesse tabelle raw importate dalla 3.2**, senza copie o nuove pipeline. |
| D-125 | VERIFICATA SU FONTI | `recordsnext.db`: 617310 GiocaIn e 16245 GiocatoreA stagionali; `recordsnext-sandbox.db`: 616018 e 16211. Differenza tutta nel 2026/27: usare test di consistenza dinamica. |
| D-126 | REGOLA TECNICA | Conservare nei fatti la distinzione `GiocatoreA.ID` locale vs `CodiceGazza` storico, senza attribuire identita canoniche nei casi dubbi. |
| D-127 | REGOLA TECNICA | Un record `GiocaIn` non prova da solo una presenza reale: interpretare `VOTO1>0` come voto valido, valori negativi come senza voto e zero/NULL come sconosciuti; preservare i valori originali. |
| D-128 | IMPLEMENTATA | Conservare integralmente campi GiocatoreA/GiocaIn/Punteggio nel campo diagnostico `sourceFields`, con marcatore esplicito per SQL NULL; nessuna interpretazione dei coefficienti bonus/malus prima della fonte FCM. |
| D-129 | APERTA | Completare la lettura di Statistiche/Storico/IncontroInA e gli eventi FCM; definire il modello temporale dei tesseramenti e la validita degli esclusi. |
| D-130 | REGOLA DI PROTEZIONE | La 3.2 operativa, il DB, la GUI, la selezione catalogo, i publisher e i siti non si modificano in questa fase. |


---

# RecordsNext 4.0 - Registro decisioni v15

Le decisioni D-001--D-112 sono preservate integralmente dalla v14. Nuove D-113--D-123:

| ID | Stato | Decisione / evidenza |
|---|---|---|
| D-113 | IMPLEMENTATA: MODELLO | Aggiungere la famiglia `app/topflop` al codice Java esistente, senza modificare le classi 3.2 ne attivare prematuramente `RecordFamily.TOP_FLOP`. |
| D-114 | APPROVATA/IMPLEMENTATA | Prestazione reale unica per stagione+ID FCA+giornata Serie A. Eventi fantasy separati per stagione+competizione FCM+incontro+fantasquadra+ID FCA. |
| D-115 | APPROVATA/IMPLEMENTATA | Conservare `CodiceGazza` storico e ID FCA stagionale distinti; non fondere automaticamente collisioni storiche. |
| D-116 | APPROVATA/IMPLEMENTATA | `Tesserato` e una fotografia della rosa, non dimostra tesseramento in una giornata non documentata. Nessuna esclusione inferita dal silenzio FCM. |
| D-117 | APPROVATA/IMPLEMENTATA | Youth League esclusa categoricamente; Play Off - Play Out e Spareggio Serie B = CAMPIONATO; 24 nomi ammessi codificati esplicitamente, altri nomi in revisione obbligatoria. |
| D-118 | APPROVATA/IMPLEMENTATA | Dati per titolari, conteggiati, panchinari e fuori lista; stato `EXCLUDED_CONFIRMED` utilizzabile solo dopo prova sorgente. |
| D-119 | APPROVATA/IMPLEMENTATA | Quattro metriche personali calcolate da voti/fantavoti integrali, `SV/P`, `SV/E`, `SF/P`, `SF/E`. Sulle fonti incomplete mantenere separati contatori voto/fantavoto, non inventare valori. |
| D-120 | APPROVATA/IMPLEMENTATA | Non sintetizzare mai URL tabellini: copiare dal normalizzato/link canonico 3.2; conservare ID incontro e giornata distinti. |
| D-121 | IMPLEMENTATA: TEST | Test Java standard con 52 assert e wrapper JUnit 5; compilazione completa codice Java 3.2+nuovo modulo verificata localmente con JDK 21, non suite Maven completa. |
| D-122 | REGOLA OPERATIVA | Il runner deve verificare JDK 21, git branch, file preesistenti e collisioni prima dell'installazione; output diagnostico sotto `D:\DEV_APPS\downloads`, mai `%TEMP%`; niente sovrascritture. |
| D-123 | APERTA | Realizzazione estrattore JDBC, identificazione esclusioni provate, ambito tesseramento, classifiche e soglie, integrazione piena RecordFamily/pipeline/exporter/GUI. Non segnare come implementati in v15. |

---

# RecordsNext 4.0 - Registro decisioni v14

# RecordsNext 4.0 - Registro decisioni v13

# RecordsNext 4.0 - Continuita documentale REGISTRO_DECISIONI_RECORDSNEXT4_v12

Versione additiva del 2026-11-01. La cronologia integrale precedente e preservata sotto.

# RecordsNext 4.0 - Registro decisioni v10

Data: 2026-11-01. Registro additivo: D-001--D-053 restano documentate nelle versioni v2--v8; i relativi stati non cambiano tacitamente.

| ID | Stato | Decisione / evidenza |
|---|---|---|
| D-054 | CONFERMATA: verifica Windows | ZIP 3B1 `RecordsNext4_Fase3B1_OUTPUT_v1_20261101_001744_607896.zip`: stato OK, sette test superati, 552.025 righe `Formazione`, 22.265 tabellini, tre SHA-256 dati verificati, SHA sandbox invariato. Bibbia v8 generata; il runner dichiara nessun commit/deploy. |
| D-055 | CONFERMATA: osservazione FCM | La catena per individuare il regolamento e `Incontro.IDGIRONE -> Girone.ID -> Girone.IDCOMPETIZIONE -> Competizione.ID` **della stessa stagione**. `Competizione.ID` non costituisce da solo identita storica. |
| D-056 | CONFERMATA: fonte | `Competizione` registra anche `NUMEROSOSTITUZIONI`, `NUMERORISUFFICIO`, voti d'ufficio, limiti panchina, ruoli sostituibili, regole speciali; `ModuloAmmesso` collega i moduli alla competizione. Nessun valore e convertito in punteggio recuperato. |
| D-057 | CONFERMATA: osservazione locale 3B2 | Sono presenti 180 righe `Competizione` su 21 stagioni. Nel 2019/20 tutte le 12 riportano `NUMEROSOSTITUZIONI=5` e `NUMERORISUFFICIO=3`; nel 2007/08 le tre riportano 3, 8, 11 sostituzioni e 0, 99, 99 RU. Valori non normalizzati arbitrariamente. |
| D-058 | CONFERMATA: separazione | Nel campione SQLite ci sono 168 tabellini senza incontro, 23 collegati a incontri con una squadra zero: non dichiararli automaticamente anomalie o riposi. I controlli si svolgono separatamente. |
| D-059 | APERTA | Interpretazione delle configurazioni elevate (8, 11, RU 99), possibili convenzioni per assenza di limite, rapporto con tabellini e modifiche al regolamento durante la stagione: da verificare sul modello FCM e su esempi reali. |
| D-060 | APERTA | Definizione rigorosa di prestazione conteggiata, prestazione disponibile in panchina e punti effettivamente recuperabili, per competizione, stagione e regole del ruolo. Niente calcoli prima dell'approvazione. |
| D-061 | IMPLEMENTATA: prototipo di sola lettura | Audit 3B2 con dati raw completi delle configurazioni, aggregati per competizione, moduli ammessi e verifiche integrita; 8 test locali superati. Certificazione Windows ancora da eseguire. |

Nota: non sono state prese nuove decisioni sui pesi dei record o sulle formule per i punti non sfruttati.


## Integrazione successiva a 3B2: metrica individuale (2026-11-01)

| ID | Stato | Decisione / motivazione |
|---|---|---|
| D-062 | CONFERMATA: verifica Windows 3B2 | ZIP `RecordsNext4_Fase3B2_OUTPUT_v1_20261101_002713_328984.zip`, stato OK; 8 test superati, 21 stagioni, 180 configurazioni, 13.072 incontri e 22.265 tabellini. SHA-256 del sandbox prima e dopo identico: `57923f4e75986c2b81533e4beef15b9133635258d7de1d36827a1bdbe576671b`. Bibbia v9 generata; nessun commit/deploy dichiarato. |
| D-063 | APPROVATA DALL'UTENTE: obiettivo | Il voto sprecato e un indicatore riferito a **uno specifico calciatore** lasciato fuori da una fantasquadra, non il vantaggio teorico che la fantasquadra avrebbe potuto ottenere sostituendo altri giocatori. Nessuna simulazione delle sostituzioni necessaria per questa metrica. |
| D-064 | APPROVATA DALL'UTENTE: formula | `mediaVotoSprecato = somma(voti validi del calciatore nelle occasioni in cui e rimasto fuori) / numero totale di partite in cui e stato lasciato fuori`. Le esclusioni senza prestazione utile contano nel denominatore e zero nel numeratore. Esempio: 23 esclusioni, 3 voti pari a 6: `18/23 = 0,782608...`. Non usare come media primaria `18/3 = 6`. |
| D-065 | CONFERMATA: RU e fonti 3.2 | La Riserva d'Ufficio e un voto regolamentare assegnato per un posto scoperto quando le sostituzioni ordinarie non bastano. Non e un calciatore ne un voto del calciatore lasciato fuori; non entra nella somma dei suoi voti sprecati. Preservare il sistema RU e gli strumenti personali esistenti. Fonti: `docs/ANALISI_PERSONALE_RU.md`, `docs/MODELLO_DATI_RECORDSNEXT2.md` e `tools/Report_RU_Storico_Completo_v8_4.ps1` (glossario). |
| D-066 | APPROVATA DALL'UTENTE: sostituzioni | Non esiste una regola generale di tre sostituzioni: si osservano anche 5 e, per recuperi a distanza di mesi e configurazioni eccezionali, anche 11. Mantenere il valore FCM della competizione senza normalizzarlo. Per la media individuale non simulare sostituzioni alternative. |
| D-067 | DA VERIFICARE PRIMA DEL CALCOLO | Stabilire con esempi reali il significato operativo di "lasciato fuori" (non nei primi 11 del tabellino finale, riserva non utilizzata, non schierato), il tipo di voto da sommare (voto Gazzetta base vs fantavoto totale), il trattamento di SV e voti speciali e il perimetro delle sole partite valide. Non inventare questi criteri. |
| D-068 | DA VERIFICARE PRIMA DEI RANKING | Evitare conteggi erronei di una stessa prestazione reale se ci sono piu competizioni fantasy nella stessa giornata. Conservare il dettaglio delle partite fantasy ed esplicitare la chiave di aggregazione del record personale. |
| D-069 | APERTA | L'utente ha menzionato un "valore massimo" senza definirne numericamente l'eventuale soglia o se si riferisca al massimo voto sprecato. Non fissare cap o limiti arbitrari. |
| D-070 | PROCEDURA | Comandi pronti per copia-incolla PowerShell 5.1, senza blocchi `else` separabili; file sempre versionati; ogni fase aggiorna piano, registro, diario, codice verificato e bibbia con SHA-256. |

### Nota di compatibilita

Le D-060 e D-061 riguardano anche un **possibile** studio dei punti recuperabili della fantasquadra. Per il nuovo record individuale quell'analisi non costituisce un prerequisito: questa decisione ne restringe il perimetro, senza cancellare la cronologia.


## Revisione metriche individuali (2026-11-01) - decisioni D-071--D-079

| ID | Stato | Decisione / motivazione |
|---|---|---|
| D-071 | APPROVATA DALL'UTENTE | Il voto di un calciatore non utilizzato e il **valore integrale del voto** (es. 7 resta 7), non il voto meno 6 e non il vantaggio potenziale della fantasquadra. |
| D-072 | APPROVATA DALL'UTENTE | Quattro indicatori distinti: media voto sprecato, errore pesato sul voto, fantamedia sprecata, errore pesato sul fantavoto. La formula v1 `S/E` rimane valida come **errore pesato**, ma non come unica misura principale. |
| D-073 | APPROVATA DALL'UTENTE | Con `E` esclusioni, `P` prestazioni valide, `SV` somma voti e `SF` somma fantavoti: `mediaVoto = SV/P`, `pesatoVoto = SV/E`, `mediaFantavoto = SF/P`, `pesatoFantavoto = SF/E`. Dove il denominatore e zero il risultato e non definito. |
| D-074 | APPROVATA DALL'UTENTE | Una gara in cui il calciatore non ha giocato/non ha voto non produce errore: somma zero al numeratore; se e una esclusione effettiva valida, rimane nel denominatore `E` del peso stagionale. |
| D-075 | APPROVATA DALL'UTENTE | Fantavoto comprensivo di eventi e relativi bonus/malus: gol normali, gol su rigore, assist, ammonizioni e altre categorie disponibili. Conservare separatamente voto, fantavoto ed eventi originali. Valori regolamentari da sorgenti reali, mai coefficienti unificati arbitrariamente. |
| D-076 | DERIVAZIONE MATEMATICA | `quotaConVoto=P/E`, `errorePesatoVoto=mediaVoto*quotaConVoto` e `errorePesatoFantavoto=fantamediaSprecata*quotaConVoto`, quando denominatori disponibili. Se `E>0, P=0`: indici pesati pari a zero, medie non definite. |
| D-077 | PROPOSTA, DA APPROVARE | Aggiungere massimo voto/fantavoto in singola esclusione, numero di esclusioni e quota con voto come indicatori di dettaglio, senza applicare alcun cap artificiale. |
| D-078 | APERTA - FCM/FCA | Definire con casi reali l'esclusione (panchinaro non usato, fuori lista, non schierato ma tesserato), la provenienza del fantavoto per giocatore lasciato fuori e la gestione di `SV`, voti d'ufficio e competizioni parallele. Non calcolare record reali prima della verifica. |
| D-079 | PROCEDURA / IMPLEMENTAZIONE | Aggiunto solo un test sintetico e la specifica v2. Nessuna modifica a database, Java, GUI, regolamenti, competizioni o siti. Bibbia incrementale v11 e SHA-256 dei nuovi file; vecchie versioni conservate. |

**Cronologia invariata:** D-064 `S/E` non e annullata: assume il nome corretto di **errore pesato stagionale** e si affianca alla nuova media `S/P` richiesta dall'utente.


## Decisioni D-080--D-090: archivio completo per giocatore e contesti paralleli

| ID | Stato | Decisione / evidenza |
|---|---|---|
| D-080 | APPROVATA DALL'UTENTE | Il prodotto deve conservare un set completo di dati per OGNI calciatore, con scheda individuale e record derivati dallo stesso archivio. Non estrarre soltanto le statistiche utili a record gia previsti. |
| D-081 | APPROVATA DALL'UTENTE | Un calciatore puo essere tesserato nello stesso anno da 2-3 fantasquadre diverse (serie distinte). Separare rigorosamente le osservazioni fantasy per squadra. |
| D-082 | APPROVATA DALL'UTENTE | Lo stesso calciatore nella stessa giornata puo essere usato in campionato ed escluso in coppa, e viceversa. Ogni incontro fantasy e un contesto indipendente; la prestazione reale e unica. |
| D-083 | APPROVATA DALL'UTENTE | Esattamente due famiglie per il filtro richiesto: CAMPIONATO e COPPE; conservare sempre anche la singola competizione e la sua identita stagionale. |
| D-084 | APPROVATA DALL'UTENTE | Dati individuali completi anche per titolari, giocatori conteggiati e subentrati, non soltanto per gli esclusi: voto, fantavoto, dettaglio bonus/malus, presenze, medie e contesti. |
| D-085 | CONFERMATA: FCM 2025/26 | `Tesserato` mostra il calciatore codice 100843 in tre squadre: 8 (River Pino), 49 (F.C. Pitbull), 77 (F.C. Boliverz). In giornata Serie A 4 River Pino lo ha in Serie B e Coppa Serie B (due incontri diversi). |
| D-086 | INFERENZA DA VERIFICARE | `Tesserato` e uno snapshot senza campi data di validita; non considerare automaticamente escluso il calciatore mancante da `Formazione` in ogni incontro di una squadra. |
| D-087 | PROPOSTA OPERATIVA | Mappa esplicita delle denominazioni FCM alle due famiglie, per stagione e competizione, invece di classificazione runtime con parole chiave. Per ora tre nomi senza assegnazione (Play Off - Play Out, Spareggio Serie B, Youth League), da approvare. |
| D-088 | REGOLA DI INTEGRITA | Chiave prestazione reale diversa dalla chiave evento fantasy; non moltiplicare gol/assist reali nelle aggregazioni all-time quando la stessa prestazione alimenta piu eventi fantasy. |
| D-089 | IMPLEMENTATA: AUDIT PROTOTIPO | Nuovo audit Fase 3C0 di sola lettura sui contesti. Verifica 552.025 righe Formazione, 21 stagioni, mappa delle competizioni, sovrapposizioni e caso reale codice 100843. Non crea ancora il dataset completo canonico. |
| D-090 | APERTA | Regole affidabili per esclusioni senza Formazione, coesistenza di piu partite della stessa famiglia nella giornata, disponibilita storica tesseramento, fantavoto del non schierato e trattamento Play Off/Spareggio/Youth League. |

## Decisioni D-091--D-098: classificazione completa e link ai tabellini

| ID | Stato | Decisione e verifica |
|---|---|---|
| D-091 | APPROVATA DALL'UTENTE | `Play Off - Play Out` appartiene alla famiglia CAMPIONATO. |
| D-092 | APPROVATA DALL'UTENTE | `Spareggio Serie B` appartiene alla famiglia CAMPIONATO. |
| D-093 | APPROVATA DALL'UTENTE | `Youth League` non deve essere considerata MAI nei record/statistiche/schede individuali. Nei soli audit fonti conservare l'evidenza della esclusione. |
| D-094 | APPROVATA DALL'UTENTE | I record individuali di singola partita devono contenere stagione, competizione, fantasquadra, ID incontro e link del tabellino di quella partita sul sito della stagione corrispondente. |
| D-095 | VERIFICATA NEL CODICE 3.2 | Esistono `urlTabellino`, `urlTabellinoLocale`, `urlTabellinoOnline`, `scorecardUrl`; la risoluzione della URL e gia implementata nel normalized exporter 3.2. Non fare concatenazioni inventate con la root del sito corrente. |
| D-096 | APERTA - LINK REALI | Prima della pubblicazione, verificare per ciascun sito/anno la rotta reale e la eventuale possibilita di puntare direttamente al singolo incontro. I link delle pagine di giornata non garantiscono un'ancora per il match. |
| D-097 | REGOLA DATI | L'ID incontro FCM e distinto dalla giornata Serie A e dalla competizione. Conservare tutti i campi per il record e per la scheda giocatore, anche in competizioni parallele. |
| D-098 | IMPLEMENTAZIONE AUDIT | L'audit v2 classifica Play Off/Spareggio, esclude Youth League dalle osservazioni utilizzabili e conserva contatori di esclusione. I riferimenti URL negli esempi rimangono null finche non risolti tramite 3.2. Nessun DB/Java/sito modificato. |


## Decisioni D-099--D-112: integrazione nativa Records Top & Flop

| ID | Stato | Decisione / evidenza |
|---|---|---|
| D-099 | APPROVATA DALL'UTENTE | La nuova famiglia records si chiama **Records Top & Flop**. E parte di RecordsNext 4.0, non un progetto o publisher distinto. |
| D-100 | APPROVATA DALL'UTENTE | Riutilizzare le logiche 3.2.0 FUNZIONANTI per elaborazione, archivio, configurazione, GUI, esportazione, manifest, link e pubblicazione multisito; la 3.2 produttiva rimane intatta. |
| D-101 | APPROVATA DALL'UTENTE | Tutte le statistiche individuali complete costituiscono la base riutilizzabile per schede e Records Top & Flop: calcolo dei record come aggregazione derivata, non raccolta dati autonoma. |
| D-102 | VERIFICATA SORGENTE 3.2 | Il catalogo record e in `RecordFamily`, `CoreRecordCatalog`; configurazione in `ProcessingConfigWriter` e `ProcessingConfigLoader`; `PipelinePreflight` e `DependencyInventory` gestiscono i prerequisiti. |
| D-103 | VERIFICATA SORGENTE 3.2 | La pipeline centrale e `RecordsNextPipeline` e il publisher JS e `Records2026SitePublisher`, con controllo esplicito del numero e dei prefissi dei JS prodotti. |
| D-104 | VERIFICATA SORGENTE 3.2 | `SeasonFamilyShardPublisher` divide per stagione, crea facade e shard JS flat nella cartella js con massimo predefinito 1536 KiB. Estendere la lista FAMILIES e i relativi test. |
| D-105 | VERIFICATA SORGENTE 3.2 | `SeasonNormalizedExporter` produce i riferimenti canonici al tabellino; `MatchesJsExporter` li espone tramite `scorecardUrl`. Nessun URL storico sintetizzato a mano. |
| D-106 | REGOLA IMPLEMENTATIVA | Il filtro sportivo CAMPIONATO/COPPE e separato dalla famiglia catalogo `TOP_FLOP`. Play Off - Play Out e Spareggio Serie B=CAMPIONATO; Youth League sempre esclusa dai dati pubblici. |
| D-107 | REGOLA IMPLEMENTATIVA | Stessa prestazione reale FCA non conteggiata piu volte nei record reali, anche se utilizzata in molte partite/competizioni fantasy; ogni match fantasy resta distinto nei record fantasy. |
| D-108 | REGOLA IMPLEMENTATIVA | Prevedere FULL e CONSOLIDATED, multisito, rollback e validazioni JS 3.2: nessuna scorciatoia per i nuovi dati e nessun deploy autonomo. |
| D-109 | REGOLA IMPLEMENTATIVA | Non attivare la nuova famiglia nel catalogo selezionabile o in pubblicazione finche archivio e exporter non sono funzionanti e testati; impedire successo apparente con output vuoti. |
| D-110 | VERIFICATA FASE 3C1 | ZIP Windows 3C1: `stato=OK`, 13 test, 552025 originali, 546775 ammessi, 5250 Youth esclusi, DB sandbox invariato, bibbia v13 generata. |
| D-111 | APERTA | Elenco definitivo e soglie dei singoli records Top & Flop, definizione formale delle esclusioni senza Formazione e validita storica dei tesseramenti. Non inventare classifiche o soglie. |
| D-112 | IMPLEMENTATA: DOCUMENTAZIONE | Questa fase aggiunge mappa integrazione e regole alla continuita documentale, senza modificare la pipeline Java, la GUI, gli output o il database. La famiglia non e ancora implementata. |


## Decisioni D-131--D-139 - Collegamento JDBC FCM v1

| ID | Stato | Decisione / verifica |
|---|---|---|
| D-131 | APPROVATA DALL'UTENTE | Il modulo e' la famiglia nativa Records Top & Flop, estesa dalla 3.2, non una pipeline separata. |
| D-132 | IMPLEMENTATA E VERIFICATA | Il lettore FCM usa le relazioni originali Formazione/Incontro/Girone/Competizione/Tabellino e ID FCA della medesima stagione. |
| D-133 | IMPLEMENTATA E VERIFICATA | Competizioni parallele e piu fantasquadre non sono aggregate: una riga per giocatore/incontro/team; 552025 contesti originari. |
| D-134 | APPROVATA DALL'UTENTE | Youth League esclusa dai dati utilizzabili per record/schede; 5250 righe sono comunque contate nell'audit. Play Off e Spareggio Serie B restano CAMPIONATO. |
| D-135 | REGOLA TECNICA | `IDGIORNATA` FCM resta chiave dell'archivio, distinta da `GIORNATADIA`; non dedurre un ordinale e non costruire URL arbitrari. |
| D-136 | REGOLA TECNICA | Il posto nei primi undici slot del Tabellino non certifica da solo la prestazione legalmente conteggiata; ENTRATO/ENTRATORISERVA restano campi fonte distinti. |
| D-137 | REGOLA TECNICA | Tesserato FCM e' snapshot, non archivio cronologico; non inventare esclusioni dove manca una riga Formazione. |
| D-138 | IMPLEMENTATA | Fonte JSON/JS pubblica non ancora generata: lettore Java FCM in streaming e test JDBC senza modifica a DB, pipeline o GUI. |
| D-139 | APERTA | Integrare nella stessa istanza `TopFlopArchive` FCA+FCM, provenienza del fantavoto per esclusi, regole voti speciali e URL tabellini con normalizzatore 3.2. |


## Decisioni D-140--D-147 - Assemblaggio nativo per stagione

| ID | Stato | Decisione / verifica |
|---|---|---|
| D-140 | IMPLEMENTATA IN FASE 4A | Assemblare fatti FCA e FCM nella stessa istanza `TopFlopArchive`, partendo da FCA e collegando ogni formazione mediante ID locale e giornata reali. |
| D-141 | VERIFICATA SU FONTE | `Tesserato` presenta righe ripetute per stessa fantasquadra/giocatore: vendite e successivi riacquisti possibili. Chiave distinta dell'evento: `Tesserato.ID` per stagione; non usare solo giocatore+squadra per conservare la cronologia sorgente. |
| D-142 | IMPLEMENTATA IN FASE 4A | `LoadedSeason.rosterEvents` conserva tutte le righe originali e i rispettivi ID; `TopFlopArchive.rosters` rimane vista sintetica di membership. Non inferire intervalli di appartenenza. |
| D-143 | IMPLEMENTATA IN FASE 4A | I token di voto/fantavoto dei tabellini FCM restano nei campi sorgente; convertire in BigDecimal solo i numerici con virgola/punto, senza convertire `sv/ng` in zero. |
| D-144 | REGOLA CONSERVATIVA | Gli slot del tabellino registrano evidenza ma non certificano utilizzo legalmente conteggiato: non produrre automaticamente `EXCLUDED_CONFIRMED` e non gonfiare i record. |
| D-145 | REGOLA CONSERVATIVA | `scorecardUrl` rimane null durante l'assemblaggio JDBC; risolverlo esclusivamente con le logiche normalizzate 3.2, al momento dell'integrazione nella pipeline. |
| D-146 | DA VERIFICARE | La provenienza della fantamedia per giocatori fuori da Formazione, l'esclusione certa durante un tesseramento storico e le regole RU/sostituzione sono ancora aperte. |
| D-147 | REGOLA DI REGRESSIONE | La nuova classe e i test non modificano pipeline, catalogo, GUI, sito o DB; testare gli hash e mantenere separati dati reali e contesti fantasy. |


## D-148--D-157 - Conferme 2026/27 e cronologia mercato

| ID | Stato | Regola o evidenza |
|---|---|---|
| D-148 | CONFERMATA DALL'UTENTE | 9 giornate reali di Serie A disponibili nello snapshot 2026/27. |
| D-149 | CONFERMATA DALL'UTENTE | La AlterLega 2026/27 ha cominciato dalla giornata reale 3; assenza di incontri nelle giornate 1 e 2 NON e' errore. |
| D-150 | IPOTESI DELL'UTENTE - DA VERIFICARE | La cronologia dei tesseramenti potrebbe essere legata a giornate della Serie A vera. Non assumere che `ULTIMAGIORNATA` sia la prima giornata eleggibile. |
| D-151 | FONTE VERIFICATA | `Tesserato` non contiene date di validita'. `EntryInBilancio` contiene `DATATRANSAZIONE` e `ULTIMAGIORNATA` e deve restare distinto dalla membership. |
| D-152 | CONFERMATA DALL'UTENTE | Calciatore ceduto e riacquistato in un'asta successiva e' operazione normale; riacquisti nella stessa sessione possono essere correzioni. Le sole date non provano la sessione. |
| D-153 | CONFERMATA DALL'UTENTE | Varela/FortisSIMY 2026/27 e' una correzione da svincolo accidentale; preservare tesseramenti 699 e 752 e relativi movimenti contabili. |
| D-154 | VERIFICATA: SNAPSHOT IMPORTATO 3.2 | 2026/27 aggiornato: 28956 righe reali FCA, 7650 `Formazione`, 813 `Tesserato`, 876 `EntryInBilancio`; 3..9 giornate reali con formazioni fantasy. |
| D-155 | REGOLA IMPLEMENTATIVA | Il nuovo assemblatore conserva tutte le righe `Tesserato` con ID sorgente e usa vista membership sintetica; nessuna esclusione senza prova di tesseramento in quel giorno. |
| D-156 | TEST AGGIUNTIVO | Verifica specifica sul DB 2026/27 aggiornato, compreso Varela e l'inizio della fantasy alla giornata 3. I dati di output test non sono regole eterne per altre stagioni. |
| D-157 | RIMANE APERTO | Non integrare automaticamente la datazione del possesso e il dettaglio `EntryInBilancio` in record pubblico fino a chiarimento dei campi originali FCM. |


---

## Addendum correttivo demo v2 — 2 novembre 2026

**D-158 (approvazione tecnica conservativa):** i quattro `release/visualizzatori/profiles/*/recordsnext.css` della 3.2, eventualmente modificati nel worktree utente, sono la fonte autoritativa di stile. La demo non li sovrascrive e deve utilizzarli dal loro percorso originale. Un CSS mancante puo essere installato dal kit come fallback. Ogni differenza viene registrata nel report; non richiede convergenza dell'hash con la copia del kit. Nessuna modifica al sito operativo. **Stato:** implementato nel runner v2, verifica Windows da completare.

## Decisioni e anomalie — 2026-11-06

| ID | Stato | Decisione / evidenza |
|---|---|---|
| D-158 | CONFERMATA DAL CODICE 3.2 | La giornata di competizione è `rn_event.competition_round`; `IDGIORNATA` è ID tecnico FCM e non deve essere mostrato come giornata. |
| D-159 | CONFERMATA | Conservare `FCM.Giornata.DESC` e `FCM.Incontro.IDGIORNATA` separatamente, senza normalizzare arbitrariamente etichette speciali. |
| D-160 | VINCOLANTE | Se `rn_event` non contiene una partita, il numero di giornata canonico resta nullo. Conservare la partita FCM, la descrizione e il motivo `UNMAPPED_SOURCE_ONLY`. |
| D-161 | ANOMALIA VERIFICATA, NON CORRETTA | Nel 2022/23 il mapping 3.2 marca `ESCLUSA` Spareggio Serie B (50 formazioni) e Vera finale di Coppa tra le Coppe (50): esiste un conflitto con il perimetro Top & Flop approvato. Non modificare il mapping 3.2 automaticamente. |
| D-162 | CONFERMATA | Le 5250 formazioni Youth League del 2023/24 rimangono escluse da Top & Flop, come richiesto dall'utente. |
| D-163 | STATO | Patch limitata al modello/assemblatore Java; non abilita la famiglia nel catalogo, pipeline, GUI, publisher o skin. |
~~~~

## `docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v23.md`

SHA-256: `5efc42cc90b690e80b3267619c797010dd9a5d8221ad198d3060ebbf76adcd82`

~~~~markdown
# Diario RecordsNext 4.0 v23 — Esportazione JS staging

Data 2026-11-06. Da sorgenti Java v22 realmente installati e database 2026/27 aggiornato.

- Aggiunta lettura stagionale efficiente di tutti i fatti `TopFlopArchive` senza scansioni per giocatore O(N²).
- Creati `TopFlopStageJsExporter`, `TopFlopCanonicalScorecards` e wrapper parser `TopFlopExistingJson` che riusa il JSON della 3.2.
- Generati shard ordinati per giocatore. Facade per stagione, riepiloghi con collegamento `detailFile`, dettagli lazy e link canonici originali.
- Scrittura binaria UTF-8 e verifica dimensione fisica ogni file; budget target 950.000, hard 1.500.000 byte.
- Su due stagioni pilota 2006/07 e 2026/27: 1419 anagrafiche, 53922 giornate FCA, 13700 contesti fantasy, 17 file e scorecard 2006 dal normalizzato 3.2.
- Su 21 stagioni e database aggiornato: 16262 anagrafiche stagionali, 617956 righe reali, 554425 contesti FCM ammessi, 389 file (senza report canonici nel test integrale), massimo 949991 byte.
- L'esportazione integrale con i 156 report normalizzati 3.2 ha prodotto 430 JS, 546101 link canonici e un totale staging di circa 372 MB (limite sempre per singolo file, non sull'insieme). Il caricamento online non è stato attivato.
- Parser di verifica standard Python per integrità di tutti i JS: 1 riga riepilogo/dettaglio per giocatore, esattezza locator, assenza Youth League, identificativi univoci, conteggi cumulati, limite byte, URL http(s).
- Controllo JavaScript nativo in Node del caricamento selettivo per stagione+calciatore con corrispondenza dettagli riuscito.
- NON modificati catalogo, pipeline, GUI, publisher, manifest o sito. NON completate esclusioni certe, dettaglio HTML definitivo o link per 2026/27 senza normalizzato aggiornato.

---

# RecordsNext 4.0 - Diario v22

2026-11-06. Fase apporto FCM: in `TopFlopJdbcArchiveAssembler`, first-eleven+incontro giocato+Tot numerico => `COUNTED`; evidenza originale in `TopFlop.UsageEvidence`. Nuovi `TopFlopContributionSummary` e `TopFlopContributionReport`: medie e somme voto reale, voto tabellino, totale fantasy, conteggi per contesto, eventi originali e normalizzati solo fonte 1. Le esclusioni non sono dedotte.

Prova sulla copia SQLite aggiornata 2026/27: 3327 slot COUNTED, 2006/07: 2585. Report di Lautaro (100843) 2026/27: 9 combinazioni squadra/competizione, divise fra tre fantasquadre; nessun doppio conteggio nella carriera reale. Prima di applicare il kit in Windows verificheremo file e SHA-256 del v21.

Non modificate pipeline 3.2, publisher, schema DB, CSS, GUI e configurazioni dei siti.

---

# Diario sviluppo v19 — Demo HTML e JS


2026-11-02: consultata direttamente la struttura `release/visualizzatori` della 3.2 con i profili CSS; esaminati `SeasonFamilyShardPublisher`, `MatchesJsExporter`, normalizzazione URL e struttura SQLite aggiornata. Implementato **solo kit demo**: HTML Top & Flop, viewer JS, facade e due shard reali. Restano inalterati catalogue Java, manifest, pipeline, GUI, database e siti. Il generatore Python è uno strumento preview read-only e **NON** il publisher nativo. JS e hash verificati localmente, prova browser Windows ancora richiesta.

---

# RecordsNext 4.0 - Diario di sviluppo v18

# RecordsNext 4.0 - Diario v16: lettore FCA JDBC

2026-11-01: confrontati realmente i due file SQLite della 3.2 allegati. Rilevata differenza confinata al 2026/27, esplicitata in piano, registro, specifica e test. Implementato `TopFlopFcaJdbcReader` come nuova classe nel package Java esistente `app/topflop`. Prove Java 21 su 21 stagioni di ciascuno dei due DB, con controllo contro conteggi SQL indipendenti, e test filtrato codici 100843/12302. Source e database produttivi invariati. Validazione finale della installazione sul PC ancora da ricevere. Non vengono fabbricati `FantasyFact` o esclusioni.


---

# RecordsNext 4.0 - Diario implementazione v15

## Intervento: primo codice Java Top & Flop

- Riletti `RecordFamily`, `CoreRecordCatalog`, `SeasonNormalizedExporter`, `MatchesJsExporter`, specifica e mappa integrazione v14, sorgenti 3.2 contenuti nell'archivio dell'utente.
- Aggiunte classi nuove `TopFlopFacts`, `TopFlopArchive`, `TopFlopMetrics`, `TopFlopCompetitionPolicy` in `app/topflop`, senza riscrivere le classi precedenti.
- Aggiunti test autonomo `TopFlopArchiveSmokeTest` (52 verifiche) e wrapper `TopFlopArchiveJUnitTest` per la suite JUnit 5 esistente.
- Eseguita compilazione dei 106 sorgenti Java originali + quattro nuove classi col compilatore Java 21.0.12.1: OK. Eseguito smoke test Java: 52 verifiche OK. Maven/JUnit integrale non eseguito nell'ambiente di analisi; la verifica sul PC utente resta da completare.
- Predisposti runner conservativo, nuova bibbia incrementale v15 e ZIP diagnostico in `downloads`.
- La prova della seconda esecuzione ha inizialmente evidenziato compilazione duplicata dei quattro file gia installati; corretto il runner per escludere le classi del kit dall'elenco dei sorgenti preesistenti. Nuova prova integrale eseguita da zero con due passaggi consecutivi (vedi log di verifica).
- Nessuna modifica a database, GUI, publisher, archivio siti, configurazione o versione Maven. Nessun commit o deploy automatico.

---

# RecordsNext 4.0 - Diario implementazione v14

# RecordsNext 4.0 - Diario implementazione v13

# RecordsNext 4.0 - Continuita documentale DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v12

Versione additiva del 2026-11-01. La cronologia integrale precedente e preservata sotto.

# RecordsNext 4.0 - Diario implementazione v10

Data: 2026-11-01.

## Revisione dello ZIP Windows 3B1

- ZIP `RecordsNext4_Fase3B1_OUTPUT_v1_20261101_001744_607896.zip`: 14 voci; `MANIFEST_FASE3B1_v1.json` con `stato=OK`.
- Log `LOG_TEST_CONTRIBUZIONI_v1.txt`: sette test su Windows, tutti superati.
- SHA-256 dei file `combinazioni.csv`, `campioni.csv`, `sintesi.json` controllati confrontando gli hash dichiarati nel manifest con i byte nello ZIP.
- Invariato lo SHA del database sandbox: `57923f4e75986c2b81533e4beef15b9133635258d7de1d36827a1bdbe576671b`.
- 22.265 tabellini, 552.025 righe Formazione, 237.686 giocatori nei primi undici slot, 22.028 panchinari nei primi undici, 27.319 titolari iniziali fuori dai primi undici; nessuna attribuzione di punti persi.
- `CODICE_FUNZIONANTE_RECORDSNEXT4_v8.md` generato; nessun commit/deploy eseguito dal runner.

## Sviluppo Fase 3B2

- Riletto lo schema raw FCM originale della 3.2: `Competizione`, `Girone`, `Incontro`, `Tabellino`, `Modulo`, `ModuloAmmesso` e analisi RU personali.
- Implementato `Audit-RecordsNext4Regole-v1.py` in modalita SQLite `mode=ro` + `query_only=ON`.
- Il censimento e per coppia stagione/competizione, con mantenimento di tutte le colonne originali `Competizione` e dei moduli ammessi. La tabella di dettaglio non contiene dati personali delle fantasquadre.
- Osservati 21 archivi, 180 configurazioni, 13.072 incontri (compresi quelli programmati/riposi), 22.265 tabellini; 168 senza ID incontro valido e 23 collegati a incontri con una squadra 0.
- Test automatici `Test-RecordsNext4Regole-v1.py`: otto test sintetici e su DB reale, superati in ambiente di prova. Il database sintetico temporaneo viene generato sotto la cartella diagnostica `downloads`, mai in `%TEMP%`.
- Progettati runner versionato v1, specifica e verifica v1, piano v10, decisioni v9 e bibbia v9 con SHA-256 dei nuovi file.
- La verifica esecutiva del nuovo runner su PowerShell 5.1/Windows e ancora da effettuare; non presentarla come completata.

## Esclusioni

Nessuna modifica ai dati, alle impostazioni delle competizioni, ai 4 script RU personali, al Java, al sito o alla GUI. Nessun calcolo di voti recuperabili, nessun commit.


## Verifica dell'output Fase 3B2 sul PC e decisione operativa

- Esaminato ZIP Windows `RecordsNext4_Fase3B2_OUTPUT_v1_20261101_002713_328984.zip`.
- `MANIFEST_FASE3B2_v1.json`: `stato=OK`, branch `feature/recordsnext4-players`, base `f9bf30323d9204d70e91e242bddfc72c2060ecf9`; stessa impronta SHA-256 del sandbox prima/dopo.
- `LOG_TEST_REGOLE_v1.txt`: otto test superati. `LOG_AUDIT_REGOLE_v1.txt`: 21 stagioni, 180 competizioni, 13.072 incontri, 22.265 tabellini; 168 riferimenti senza incontro rimangono non classificati.
- Riletto `docs/ANALISI_PERSONALE_RU.md` e il glossario di `tools/Report_RU_Storico_Completo_v8_4.ps1`: RU = voto assegnato d'ufficio a copertura di una mancanza. Non associare una RU a un giocatore storico reale.
- L'utente ha approvato la metrica **individuale**, non il controfattuale di squadra: 23 occasioni fuori, 3 gare con voto 6 -> 18/23. Nessun calcolo effettivo sui giocatori e nessun cap implementati in questa revisione.
- Creato test unitario della **sola formula** su dati sintetici. Prima della successiva implementazione su SQLite occorre verificare su esempi reali quali righe costituiscano esclusioni e quale voto sia valido.
- Questa revisione installa soltanto documentazione, un test di formula, il generatore v10 e un runner conservativo. Non modifica Java, GUI, pipeline, archivi 3.2 o siti.


## Revisione della metrica su richiesta utente (2026-11-01)

- Esaminato lo ZIP di esecuzione Windows `RecordsNext4_DecisioniGiocatori_OUTPUT_v1_20261101_003202_168980.zip`: contiene bibbia v10, registro v10, test formula v1 e manifest `stato=OK`.
- Distinti il rendimento medio nelle sole esclusioni con voto (`S/P`) e la misura stagionale su tutte le esclusioni (`S/E`), per voto reale e fantavoto completo di bonus/malus.
- Creati `SPECIFICA_METRICA_VOTO_SPRECATO_GIOCATORE_v2.md` e `Test-RecordsNext4MetricheIndividuali-v2.py` per il contratto puramente aritmetico; test su dati sintetici, nessuna lettura FCM/FCA in questo intervento.
- Creati piano v12, registro decisioni v11, diario v11 e generatore bibbia v11. Conservate le versioni precedenti.
- Restano da verificare sui dati reali la definizione operativa di esclusione, voto valido, fantavoto disponibile per esclusi, bonus/malus storici e deduplicazione delle partite fantasy parallele.
- Nessuna implementazione di record nei sorgenti Java e nessun deploy; database invariato perche non viene aperto.


## Fase 3C0 - 2026-11-01

- Verificato lo ZIP Windows `RecordsNext4_Metriche_OUTPUT_v1_20261101_004055_490269.zip`: manifest OK, 8 test metriche superati, bibbia v11 prodotta; nessun deploy/commit dichiarato.
- Esaminato il database sandbox 3.2 in sola lettura: 21 stagioni, 552.025 righe Formazione, 180 righe Competizione con 25 nomi distinti; la stessa giornata puo contenere piu incontri fantasy per giocatore-squadra.
- Caso campione 2025/26, codice 100843: tre tesseramenti snapshot, presenza nello stesso giorno in campionato e coppa per River Pino.
- Il prototipo di audit Fase 3C0 verifica l'integrita delle join ed elenca le competizioni non assegnate, senza interpretare le esclusioni o sommare i voti.
- Stato: audit testato localmente, codice integrale nella bibbia incrementale v12; installazione e test Windows ancora da confermare.

## Fase 3C1 - revisione classificazione e riferimenti tabellino

- Ricevuto e verificato output 3C0 Windows: `ESITO OK`, 10/10 test, 552025 righe Formazione; SHA256 del sandbox invariato, bibbia v12. Le 159678 sovrapposizioni sono contesti fantasy distinti, non duplicazioni reali.
- L'utente ha classificato `Play Off - Play Out` e `Spareggio Serie B` come campionati e richiesto l'esclusione completa di `Youth League` da record/schede.
- Fonte Java 3.2 esaminata: `SeasonNormalizedExporter.java` espone `urlTabellino`, `urlTabellinoLocale`, `urlTabellinoOnline` e conserva `source_event_id`; `MatchesJsExporter.java` espone `matchId`, `competitionId`, `seasonId`, `scorecardUrl`.
- L'audit v2 mantiene ID incontro e giornata; aggiunge `idGiornataFcm` e `scorecardLinkStatus` negli esempi. URL effettivo assente nell'audit raw: resta null.
- Nessuna simulazione RU/sostituzioni, nessun calcolo degli esclusi mancanti da Formazione, nessuna modifica alla pipeline o al DB.


## Revisione 2026-11-01 - nuova famiglia Records Top & Flop

- L'utente ha chiarito che l'intero modulo giocatori e **una famiglia record del RecordsNext esistente**, con il nome pubblico **Records Top & Flop**.
- Rianalizzati gli effettivi sorgenti 3.2.0 forniti (`RecordsNext2.0.zip`), in particolare `RecordFamily`, `CoreRecordCatalog`, `ProcessingConfigWriter`, `ProcessingConfigLoader`, `RecordsNextPipeline`, `Records2026SitePublisher`, `SeasonFamilyShardPublisher`, `MatchesJsExporter`, `SeasonNormalizedExporter`, `RecordsNext2Dashboard`.
- `Records2026SitePublisher.validateGenerated` valida anche **il conteggio totale** dei file JS e i loro prefissi: richiede modifica coordinata prima di aggiungere un nuovo exporter. `SeasonFamilyShardPublisher.FAMILIES` e la lista esplicita delle famiglie sharded: richiede aggiunta con test flat.
- La 3.2 usa facade/shard per stagione nei js dei siti, con scope multisito e protezione rollback. Va **estesa**, non replicata con tool dedicati.
- Verificato output Windows Fase 3C1: 13/13 test, 21 stagioni, 552025 Formazione, 5250 Youth League escluse, 546775 ammesse, DB immutato.
- Stato codice: **nessun Java/SQLite/GUI/sito modificato in questa revisione**. Restano prototipi audit; prossimo sviluppo sul codice Java nel worktree 4.0. Documenti e script presenti nella bibbia incrementale v14.


## Sviluppo 2026-11-01 - Lettore FCM JDBC v1

- Verificato il report Windows FCA v16: esito OK, nessuna variazione hash database o codice 3.2.
- Ispezionato schema completo raw FCM 2006/07-2026/27 e joins originali; nessuna variazione delle colonne principali tra le 21 stagioni.
- Creato `TopFlopFcmJdbcReader.java` con scansione streaming delle Formazione, Tabellino, Incontro, Girone, Competizione, Fantasquadra, anagrafica FCA e GiocaIn. Separato il censimento Tesserato.
- Preservati testi originari del Tabellino e ID di giornata FCM; esclusi Youth League, inclusi Play Off e Spareggio nella corretta famiglia.
- Completati test integrali su entrambe le copie SQLite disponibili: 552025 Formazione, 546775 ammesse, 5250 Youth League, 14320 Tesserato, tutte le join e gli slot verificati.
- Preparati installer non distruttivo, verifica doppia esecuzione, regressione 52 test, controllo hash DB e bibbia incrementale v17.
- Nessuna modifica a 3.2, export, GUI o pubblicazione; verifica Windows sul PC reale in attesa di ZIP diagnostico.


## 2026-11-02 - Collegamento Java FCA/FCM fase 4A

- Verificato lo ZIP Windows `RecordsNext4_FcmJdbc_OUTPUT_v1_20261102_143437_776784.zip`: manifest `OK`, compilazione e regressioni 52/FCA/FCM superate, hash di entrambi i DB invariati, bibbia v17 generata.
- Rilevati e verificati tesseramenti con stessa coppia giocatore+fantasquadra ma ID `Tesserato` differenti; il modello sintetico precedente non era sufficiente a conservare gli eventi sorgente.
- Aggiunta la classe additiva `TopFlopJdbcArchiveAssembler.java`: costruisce `TopFlopArchive` stagionale dagli adapter esistenti e restituisce separatamente tutti gli eventi Tesserato.
- Test con JDBC SQLite in sola lettura, con riconciliazione di giorni reali, formazioni ammesse, Youth esclusa, tesseramenti sorgente e membership univoche; nessuna inferenza di esclusioni, URL, bonus o malus.
- Stato: test pilota locali superati. Il runner Windows produce ZIP diagnostico e bibbia v18; verifica finale Windows attesa.


## 2026-11-02 - Nuovi FCA/FCM e validazione stagione 2026/27

- Ricevuti FCM/FCA aggiornati e DB di prova importato con importatore RAW originale RecordsNext 3.2; validati con `integrity_check`.
- L'utente conferma: 9 giornate reali, AlterLega iniziata alla terza.
- Varela/FortisSIMY: due ID tesseramento 699/752 per svincolo accidentale e reinserimento; mantiene singola identita' reale.
- Audit su `EntryInBilancio` con date e `ULTIMAGIORNATA`, significato preciso ancora non certificato.
- Aggiunto test aggiornato `TopFlopJdbcArchive2026SmokeTest` al kit dell'assemblatore.
- Test Windows e installazione in worktree 4.0 da effettuare; nessuna modifica al database operativo 3.2.


---

## Addendum correttivo demo v2 — 2 novembre 2026

**2 novembre 2026:** il runner della demo v1 si e interrotto nel preflight, rilevando un CSS neutral esistente con hash diverso. Nessun file era stato copiato. Runner v2 sviluppato per riutilizzare CSS preesistenti senza sovrascriverli, mantenendo la verifica stringente per tutti gli altri file e la generazione di bibbia v19 e v20. Prova Linux ripetuta in worktree con CSS diversi; prova PowerShell/Windows ancora da confermare.

## 2026-11-06 — Patch canonica giornate Top & Flop v1

Esaminato lo ZIP `RecordsNext4_SORGENTI_ATTUALI_20261106.zip` (codice e documentazione correnti). Confrontate `CanonicalViews`, `TopFlopFcmJdbcReader`, `TopFlopFacts`, `TopFlopJdbcArchiveAssembler` e le viste SQLite 3.2 del database aggiornato. Rilevato uso errato di IDGIORNATA come fantasyRound; introdotto resolver canonico con ordinamento della 3.2 e fallback nullo per mapping esclusi. Nessun aggiornamento di RAW, mapping, database operativo, GUI, CSS o pubblicazione. Test su copia e installazione conservativa versionata. Prossimo step motore Top & Flop + JS nativi, usando gli altri publisher esistenti.

- Conteggi aggiornati: 559675 Formazione nell’SQLite con 2026/27 importato (contro 552025 nel precedente snapshot).
~~~~
