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
