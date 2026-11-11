# RecordsNext 4.0 — Bibbia v24 — Exporter Top & Flop compatto

Base: RecordsNext 3.2.0. Fonte completa SQLite; JS staging sotto 1.500.000 byte ciascuno.
Pipeline, CSS, GUI, database e siti operativi invariati.
Bibbia precedente: `docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v23.md`; SHA-256: `50347f518d7c02e2f065a464378117d0432877e79b44904f37fcaa82790da4d1`

## `src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopCompactStageJsExporter.java`

SHA-256: `075abe5f3ecfa671843e857185ddb50a0d3c966308e2fc854641bf5a75a1b530`

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
public final class TopFlopCompactStageJsExporter {
    public static final long MAX_BYTES = 1_500_000L;
    public static final long TARGET_BYTES = 950_000L;
    public static final String FACADE_NAME = "fcmRecordsNext_TopFlop.js";
    private static final MathContext MC = MathContext.DECIMAL64;

    private TopFlopCompactStageJsExporter() {}

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
            SeasonDictionary dict = new SeasonDictionary(archive);
            List<String> detailRows = new ArrayList<>(players.size());
            for (var profile : players) {
                PlayerSeason p = profile.player();
                Map<String,Object> legacy = detail(profile,
                    realByPlayer.getOrDefault(p, List.of()),
                    fantasyByPlayer.getOrDefault(p, List.of()), canonicalLinks);
                detailRows.add(Json.write(compactDetail(legacy,dict)));
            }
            List<FileInfo> detailShards = shard(newJsDirectory, season, "Detail", detailRows);
            Map<Integer,Integer> detailPartByLocalPlayerId = new LinkedHashMap<>();
            int nextPlayerIndex = 0;
            for (int part = 0; part < detailShards.size(); part++) {
                for (int row = 0; row < detailShards.get(part).rows(); row++) {
                    detailPartByLocalPlayerId.put(players.get(nextPlayerIndex++).player().fcaPlayerId(), part);
                }
            }
            if (nextPlayerIndex != players.size())
                throw new IOException("Details player coverage mismatch: " + season);
            List<String> summaryRows = new ArrayList<>(players.size());
            for (var profile : players) {
                PlayerSeason p = profile.player();
                Integer part = detailPartByLocalPlayerId.get(p.fcaPlayerId());
                Map<String,Object> legacy=summary(profile,
                    realByPlayer.getOrDefault(p, List.of()),
                    fantasyByPlayer.getOrDefault(p, List.of()),
                    detailShards.get(part).name());
                summaryRows.add(Json.write(compactSummary(legacy,dict,part)));
            }
            List<FileInfo> summaryShards = shard(newJsDirectory, season, "Summary", summaryRows);
            List<String> linkRows = new ArrayList<>(dict.links.size());
            for (var link : dict.links.entrySet()) {
                linkRows.add(Json.write(List.of(link.getKey().matchId(),
                    link.getKey().teamId(), link.getValue())));
            }
            List<FileInfo> linkShards = shard(newJsDirectory, season, "Links", linkRows);
            infos.addAll(summaryShards);
            infos.addAll(detailShards);
            infos.addAll(linkShards);
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
            metadata.put("summaryFiles",summaryShards.stream().map(FileInfo::name).toList());
            metadata.put("detailFiles",detailShards.stream().map(FileInfo::name).toList());
            metadata.put("linksFiles",linkShards.stream().map(FileInfo::name).toList());
            metadata.put("teams",dict.teams.entrySet().stream()
                .map(e->List.of(e.getKey(),e.getValue())).toList());
            metadata.put("competitions",dict.competitions.entrySet().stream()
                .map(e->List.of(e.getKey(),e.getValue().get(0),e.getValue().get(1))).toList());
            metadata.put("eventNames",dict.eventNames);
            metadata.put("roundDescriptions",dict.roundDescriptions);
            metadata.put("usageEvidence",dict.usageEvidence);
            indexSeasons.add(metadata);
        }
        Map<String,Object> facade = new LinkedHashMap<>();
        facade.put("schemaVersion", "4.0-stage-v2-compact");
        facade.put("familyId", "top_flop");
        facade.put("status", "STAGING_ONLY_NOT_PUBLISHED");
        facade.put("projection", "DICTIONARY_AND_POSITIONAL_V2");
        facade.put("maxJsBytes", MAX_BYTES);
        facade.put("seasons", indexSeasons);
        String facadeJs = "window.fcmRecordsNextTopFlop = " + Json.write(facade) + ";\n";
        infos.add(writeChecked(newJsDirectory, FACADE_NAME, "", "Facade", 1, facadeJs));
        for (FileInfo info : infos)
            if (info.bytes() > MAX_BYTES) throw new IOException("Oversize file: " + info.name());
        return new Result(seasonIds.size(), playersTotal, realTotal, fantasyTotal,
                List.copyOf(infos), newJsDirectory);
    }

    /** Compact site projection: original FCA/FCM facts remain in SQLite. */
    private static final class SeasonDictionary {
        final Map<Integer,String> teams = new TreeMap<>();
        final Map<Integer,List<String>> competitions = new TreeMap<>();
        final List<String> eventNames;
        final List<String> roundDescriptions;
        final List<String> usageEvidence;
        final Map<String,Integer> eventIndex = new LinkedHashMap<>();
        final Map<String,Integer> descriptionIndex = new LinkedHashMap<>();
        final Map<String,Integer> evidenceIndex = new LinkedHashMap<>();
        final Map<TopFlopCanonicalScorecards.MatchTeam,String> links = new LinkedHashMap<>();

        SeasonDictionary(TopFlopArchive archive) {
            java.util.TreeSet<String> events = new java.util.TreeSet<>();
            java.util.TreeSet<String> descriptions = new java.util.TreeSet<>();
            java.util.TreeSet<String> evidence = new java.util.TreeSet<>();
            for (RealDay d : archive.allRealDays())
                for (var e : d.events().entrySet())
                    if (e.getValue().signum()!=0) events.add(e.getKey());
            for (FantasyFact f: archive.allFantasyFacts()) {
                teams.putIfAbsent(f.key().fantasyTeamId(),
                    f.sourceFields().getOrDefault("FCM.Fantasquadra.Nome", ""));
                competitions.putIfAbsent(f.key().sourceCompetitionId(),
                    List.of(f.competitionName(),f.family().name()));
                String description = f.sourceFields().get("FCM.Giornata.Desc");
                if (description!=null) descriptions.add(description);
                String usage = f.sourceFields().get("TopFlop.UsageEvidence");
                if (usage!=null) evidence.add(usage);
                for (var e : f.fantasyEvents().entrySet())
                    if (e.getValue().signum()!=0) events.add(e.getKey());
            }
            eventNames=List.copyOf(events);
            roundDescriptions=List.copyOf(descriptions);
            usageEvidence=List.copyOf(evidence);
            for(int i=0;i<eventNames.size();i++) eventIndex.put(eventNames.get(i),i);
            for(int i=0;i<roundDescriptions.size();i++) descriptionIndex.put(roundDescriptions.get(i),i);
            for(int i=0;i<usageEvidence.size();i++) evidenceIndex.put(usageEvidence.get(i),i);
        }
        Object sparse(Object raw) throws IOException {
            if (!(raw instanceof Map<?,?> map) || map.isEmpty()) return null;
            List<Object> packed=new ArrayList<>(map.size()*2);
            for(var e:map.entrySet()) {
                Integer idx=eventIndex.get(String.valueOf(e.getKey()));
                if(idx==null)throw new IOException("Undeclared source event: "+e.getKey());
                packed.add(idx);packed.add(e.getValue());
            }
            return packed;
        }
        void addLink(long matchId,int teamId,String url) throws IOException {
            if(url==null) return;
            var key=new TopFlopCanonicalScorecards.MatchTeam(matchId,teamId);
            String prior=links.putIfAbsent(key,url);
            if(prior!=null && !prior.equals(url))
                throw new IOException("Conflicting canonical URL for match/team: "+matchId+"/"+teamId);
        }
    }

    private static List<Object> row(Object... parts) {
        List<Object> result=new ArrayList<>(parts.length);
        java.util.Collections.addAll(result,parts);
        return result;
    }

    @SuppressWarnings("unchecked")
    private static List<Object> compactDetail(Map<String,Object> legacy,SeasonDictionary dict)
            throws IOException {
        List<Object> real=new ArrayList<>();
        for(Map<String,Object> d:(List<Map<String,Object>>)legacy.get("realDays")) {
            String state=(String)d.get("voteState");
            Object events=dict.sparse(d.get("sourceEvents"));
            if (events==null && d.get("vote")==null)
                real.add(row(d.get("serieARound"),TopFlopFacts.VoteState.valueOf(state).ordinal()));
            else real.add(row(d.get("serieARound"),TopFlopFacts.VoteState.valueOf(state).ordinal(),
                d.get("vote"),events));
        }
        List<Object> fantasy=new ArrayList<>();
        for(Map<String,Object> f:(List<Map<String,Object>>)legacy.get("fantasy")) {
            long matchId=((Number)f.get("matchId")).longValue();
            int teamId=((Number)f.get("teamId")).intValue();
            dict.addLink(matchId,teamId,(String)f.get("scorecardUrl"));
            String desc=(String)f.get("roundDescription");
            String evidence=(String)f.get("usageEvidence");
            fantasy.add(row(
                matchId, f.get("competitionId"),f.get("competitionRound"),
                desc==null?null:dict.descriptionIndex.get(desc),
                f.get("serieARound"),teamId,
                TopFlopFacts.Usage.valueOf((String)f.get("usage")).ordinal(),
                Boolean.TRUE.equals(f.get("initialStarter"))?1:0,
                f.get("tabellinoVote"),f.get("fantasyVote"),
                dict.sparse(f.get("sourceEvents")),
                f.get("position"),f.get("entered"),f.get("enteredAsReserve"),
                f.get("firstEleven"),f.get("modifier"),
                evidence==null?null:dict.evidenceIndex.get(evidence)
            ));
        }
        return row(legacy.get("localFcaId"),legacy.get("gazzaCode"),legacy.get("name"),real,fantasy);
    }

    @SuppressWarnings("unchecked")
    private static List<Object> compactSummary(Map<String,Object> legacy,SeasonDictionary dict,int part)
            throws IOException {
        Map<String,Object> real=(Map<String,Object>)legacy.get("real");
        List<Object> contributions=new ArrayList<>();
        for(Map<String,Object> g:(List<Map<String,Object>>)legacy.get("contributions")) {
            contributions.add(row(g.get("teamId"),g.get("competitionId"),
                g.get("contexts"),g.get("counted"),g.get("countedWithRealVote"),
                g.get("voteSum"),g.get("meanVote"),g.get("fantasySum"),g.get("meanFantasy"),
                dict.sparse(g.get("sourceEventsApportioned"))));
        }
        return row(legacy.get("localFcaId"),legacy.get("gazzaCode"),legacy.get("name"),
            part,real.get("days"),real.get("withVote"),real.get("voteSum"),real.get("meanVote"),
            dict.sparse(real.get("sourceEvents")),legacy.get("fantasyContexts"),contributions);
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
        String store = kind.equals("Summary") ? "summaries" : kind.equals("Detail") ? "details" : "links";
        String prefix="(function(){var d=window.fcmRecordsNextTopFlopData||(window.fcmRecordsNextTopFlopData={});"
                +"var a=d."+store+"||(d."+store+"={});(a[\""+season+"\"]||(a[\""+season+"\"]=[])).push(...[";
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
            System.out.println("TOP_FLOP_COMPACT_STAGE_JS_OK seasons="+result.seasons()+" players="+result.players()
                +" realDays="+result.realDays()+" fantasy="+result.fantasyFacts()
                +" jsFiles="+result.files().size()
                +" maxBytes="+result.files().stream().mapToLong(FileInfo::bytes).max().orElse(0));
        }
    }
}
~~~~

## `src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopCompactStageSmokeTest.java`

SHA-256: `1f22e744f250345d520345982c064bc1c0deb1f6054ff8868c2c0be376bb1fb4`

~~~~java
package it.alterlega.recordsnext.app.topflop;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Staging-only no-database regression: shard size, link store, no silent truncation. */
public final class TopFlopCompactStageSmokeTest {
    private TopFlopCompactStageSmokeTest() {}
    public static void main(String[] args) throws Exception {
        Path root=Files.createTempDirectory("topflop-compact-smoke-");
        try {
            var summary=TopFlopCompactStageJsExporter.shard(root,"2026_2027","Summary",
                List.of("[1,100843,\"Lautaro\",0]","[2,123,\"Altro\",0]"));
            if(summary.size()!=1 || summary.getFirst().rows()!=2)throw new AssertionError("summary rows lost");
            if(Files.size(root.resolve(summary.getFirst().name()))!=summary.getFirst().bytes())
                throw new AssertionError("invalid real byte count");
            var links=TopFlopCompactStageJsExporter.shard(root,"2026_2027","Links",
                List.of("[100,12,\"https://example.invalid/ris.php?Gio=4\"]"));
            if(links.size()!=1 || !Files.readString(root.resolve(links.getFirst().name()),
                StandardCharsets.UTF_8).contains("d.links"))throw new AssertionError("link dictionary missing");
            boolean refused=false;
            try {
                TopFlopCompactStageJsExporter.shard(root,"2025_2026","Detail",
                    List.of("\""+"x".repeat(1_500_001)+"\""));
            }catch(IOException expected){refused=true;}
            if(!refused)throw new AssertionError("oversized individual row was not blocked");
            System.out.println("TOP_FLOP_COMPACT_SMOKE_OK checks=4");
        }finally{
            try(var paths=Files.walk(root)) {
                paths.sorted(java.util.Comparator.reverseOrder()).forEach(p->{try{Files.deleteIfExists(p);}catch(IOException ignored){}});
            }
        }
    }
}
~~~~

## `visualizzatori/js/fcmRecordsNextTopFlopCompactDecoder-v2.js`

SHA-256: `cbfc512cef67fcbfaba5a192226b4f33a959dcd80570e8a2ffa805210fc7b93c`

~~~~javascript
/* RecordsNext 4.0 Top & Flop — reversible, lazy browser decoder for staging v2.
 * Does not download all historical player details on page load.
 * No publication logic, CSS changes or inferred scorecard URLs.
 */
(function (global) {
  'use strict';
  var VoteState = ['VALID', 'NO_VOTE', 'SPECIAL', 'NOT_PLAYED', 'UNKNOWN'];
  var Usage = ['COUNTED', 'BENCH_UNUSED', 'OUTSIDE_LIST', 'EXCLUDED_CONFIRMED', 'UNKNOWN'];
  var pending = Object.create(null);
  function facade() {
    var f = global.fcmRecordsNextTopFlop;
    if (!f || f.schemaVersion !== '4.0-stage-v2-compact') {
      throw new Error('Top & Flop compact v2: missing or incompatible facade');
    }
    return f;
  }
  function seasonMetadata(season) {
    var match = facade().seasons.find(function (s) { return s.seasonId === season; });
    if (!match) throw new Error('Unknown Top & Flop season: ' + season);
    return match;
  }
  function pairs(values, names) {
    var result = {};
    if (values === null || values === undefined) return result;
    if (values.length % 2 !== 0) throw new Error('Invalid compact event vector');
    for (var i = 0; i < values.length; i += 2) {
      var name = names[values[i]];
      if (name === undefined || Object.prototype.hasOwnProperty.call(result, name)) {
        throw new Error('Invalid compact event index');
      }
      result[name] = values[i + 1];
    }
    return result;
  }
  function lookups(meta) {
    var teams = Object.create(null), competitions = Object.create(null);
    meta.teams.forEach(function (r) { teams[r[0]] = r[1]; });
    meta.competitions.forEach(function (r) { competitions[r[0]] = r; });
    return { teams: teams, competitions: competitions };
  }
  function decodeSummary(row, meta) {
    var l = lookups(meta);
    var contributors = row[10].map(function (c) {
      var comp = l.competitions[c[1]];
      return {
        teamId: c[0], team: l.teams[c[0]], competitionId: c[1],
        competition: comp[1], family: comp[2], contexts: c[2], counted: c[3],
        countedWithRealVote: c[4], voteSum: c[5], meanVote: c[6],
        fantasySum: c[7], meanFantasy: c[8],
        sourceEventsApportioned: pairs(c[9], meta.eventNames)
      };
    });
    return {
      seasonId: meta.seasonId, localFcaId: row[0], gazzaCode: row[1],
      name: row[2], detailFile: meta.detailFiles[row[3]],
      real: { days: row[4], withVote: row[5], voteSum: row[6],
              meanVote: row[7], sourceEvents: pairs(row[8], meta.eventNames) },
      fantasyContexts: row[9], contributions: contributors
    };
  }
  function indexLinks(rows) {
    var index = Object.create(null);
    (rows || []).forEach(function (r) {
      var key = r[0] + '|' + r[1];
      if (index[key] !== undefined && index[key] !== r[2]) {
        throw new Error('Conflicting Top & Flop scorecard URL: ' + key);
      }
      index[key] = r[2];
    });
    return index;
  }
  function decodeDetail(row, meta, linkIndex) {
    var l = lookups(meta), urls = linkIndex || Object.create(null);
    var real = row[3].map(function (r) {
      return { serieARound: r[0], voteState: VoteState[r[1]],
               vote: r.length > 2 ? r[2] : null,
               sourceEvents: pairs(r.length > 3 ? r[3] : null, meta.eventNames) };
    });
    var fantasy = row[4].map(function (r) {
      var comp = l.competitions[r[1]], url = urls[r[0] + '|' + r[5]];
      return {
        matchId: r[0], competitionId: r[1], competition: comp[1], family: comp[2],
        competitionRound: r[2],
        roundDescription: r[3] === null ? null : meta.roundDescriptions[r[3]],
        serieARound: r[4], teamId: r[5], team: l.teams[r[5]],
        usage: Usage[r[6]], initialStarter: r[7] === 1,
        tabellinoVote: r[8], fantasyVote: r[9], sourceEvents: pairs(r[10], meta.eventNames),
        scorecardUrl: url === undefined ? null : url,
        position: r[11], entered: r[12], enteredAsReserve: r[13],
        firstEleven: r[14], modifier: r[15],
        usageEvidence: r[16] === null ? null : meta.usageEvidence[r[16]]
      };
    });
    return {seasonId: meta.seasonId, localFcaId: row[0], gazzaCode: row[1],
            name: row[2], realDays: real, fantasy: fantasy};
  }
  function dataStore() {
    return global.fcmRecordsNextTopFlopData || (global.fcmRecordsNextTopFlopData = {});
  }
  function normalizeBase(base) { return String(base || '.').replace(/\/+$/, ''); }
  function loadScript(url) {
    if (pending[url]) return pending[url];
    pending[url] = new Promise(function (resolve, reject) {
      var element = document.createElement('script');
      element.async = true;
      element.src = url;
      element.onload = function () { resolve(); };
      element.onerror = function () { delete pending[url]; reject(new Error('JS load failed: ' + url)); };
      document.head.appendChild(element);
    });
    return pending[url];
  }
  function loadFiles(base, files) {
    return Promise.all(files.map(function (name) {
      return loadScript(normalizeBase(base) + '/' + name);
    }));
  }
  function loadFacade(base) {
    if (global.fcmRecordsNextTopFlop) return Promise.resolve(facade());
    return loadFiles(base, ['fcmRecordsNext_TopFlop.js']).then(facade);
  }
  function loadSummaries(base, season) {
    return loadFacade(base).then(function () {
      var md = seasonMetadata(season);
      return loadFiles(base, md.summaryFiles).then(function () {
        return (dataStore().summaries || {})[season] || [];
      });
    });
  }
  function loadLinks(base, season) {
    return loadFacade(base).then(function () {
      var md = seasonMetadata(season);
      return loadFiles(base, md.linksFiles).then(function () {
        return indexLinks((dataStore().links || {})[season] || []);
      });
    });
  }
  function loadPlayer(base, season, localFcaId) {
    return loadSummaries(base, season).then(function (rows) {
      var row = rows.find(function (r) { return r[0] === localFcaId; });
      if (!row) return null;
      var md = seasonMetadata(season);
      return Promise.all([loadFiles(base, [md.detailFiles[row[3]]]), loadLinks(base, season)])
        .then(function (result) {
          var details = (dataStore().details || {})[season] || [];
          var player = details.find(function (r) { return r[0] === localFcaId; });
          if (!player) throw new Error('Missing player from declared detail shard');
          return decodeDetail(player, md, result[1]);
        });
    });
  }
  global.RecordsNextTopFlopCompactV2 = Object.freeze({
    loadFacade: loadFacade, loadSummaries: loadSummaries, loadLinks: loadLinks,
    loadPlayer: loadPlayer, seasonMetadata: seasonMetadata,
    decodeSummary: decodeSummary, decodeDetail: decodeDetail, indexLinks: indexLinks
  });
}(window));
~~~~

## `tools/Validate-RecordsNext4TopFlopCompact-v2.py`

SHA-256: `b62248f116e6bbb7b485e141cca1f9f9a2969c21c0e2519de46c6fe90d4f4908`

~~~~python
#!/usr/bin/env python3
"""Read-only verifier for RecordsNext4 compact TopFlop staging JS.

Checks cardinalities, player identity, event integrity, canonical link lookup,
physical byte sizes, and (when provided) exact semantic parity with v1 JS.
"""
from __future__ import annotations
import argparse
from collections import Counter
from pathlib import Path
import hashlib
import json

MAX_BYTES = 1_500_000
STATES = ['VALID','NO_VOTE','SPECIAL','NOT_PLAYED','UNKNOWN']
USES = ['COUNTED','BENCH_UNUSED','OUTSIDE_LIST','EXCLUDED_CONFIRMED','UNKNOWN']

def parse(path):
    data=path.read_bytes()
    assert len(data)<=MAX_BYTES, (path.name,len(data))
    js=data.decode('utf-8')
    if path.name=='fcmRecordsNext_TopFlop.js':
        p='window.fcmRecordsNextTopFlop = '
        assert js.startswith(p) and js.rstrip().endswith(';'),path
        return json.loads(js[len(p):].strip()[:-1])
    p='.push(...['
    i=js.index(p)+len(p)
    end=js.rfind(']);})();')
    assert end>=i and js.startswith('(function(){'),path
    return json.loads('['+js[i:end]+']')

def files_data(folder,names):
    for name in names:
        for rec in parse(folder/name):yield name,rec

def event_map(data,events):
    if data is None:return {}
    assert len(data)%2==0
    result={}
    for i in range(0,len(data),2):
        k=events[data[i]]
        assert k not in result
        result[k]=data[i+1]
    return result

def unpack_real_day(row,events):
    assert 2<=len(row)<=4
    return {'serieARound':row[0],'voteState':STATES[row[1]],
            'vote':row[2] if len(row)>2 else None,
            'sourceEvents':event_map(row[3] if len(row)>3 else None,events)}

def unpack_summary(row,season,metadata):
    assert len(row)==11
    teams=dict(metadata['teams'])
    competitions={x[0]:x[1:] for x in metadata['competitions']}
    events=metadata['eventNames']
    groups=[]
    for a in row[10]:
        assert len(a)==10
        competition=competitions[a[1]]
        groups.append({'teamId':a[0],'team':teams[a[0]],'competitionId':a[1],
            'competition':competition[0],'family':competition[1],
            'contexts':a[2],'counted':a[3],'countedWithRealVote':a[4],
            'voteSum':a[5],'meanVote':a[6],'fantasySum':a[7],
            'meanFantasy':a[8],'sourceEventsApportioned':event_map(a[9],events)})
    assert 0<=row[3]<len(metadata['detailFiles'])
    return {'seasonId':season,'localFcaId':row[0],'gazzaCode':row[1],'name':row[2],
        'detailFile':metadata['detailFiles'][row[3]],
        'real':{'days':row[4],'withVote':row[5],'voteSum':row[6],
                'meanVote':row[7],'sourceEvents':event_map(row[8],events)},
        'fantasyContexts':row[9],'contributions':groups}

def unpack_detail(row,season,metadata,links):
    assert len(row)==5
    teams=dict(metadata['teams']); comps={x[0]:x[1:] for x in metadata['competitions']}
    events=metadata['eventNames'];desc=metadata['roundDescriptions']; evidence=metadata['usageEvidence']
    contexts=[]
    for f in row[4]:
        assert len(f)==17,(season,row[0],f)
        comp=comps[f[1]]
        contexts.append({'matchId':f[0],'competitionId':f[1],'competition':comp[0],
           'family':comp[1],'competitionRound':f[2],
           'roundDescription':None if f[3] is None else desc[f[3]],
           'serieARound':f[4],'teamId':f[5],'team':teams[f[5]],
           'usage':USES[f[6]],'initialStarter':bool(f[7]),
           'tabellinoVote':f[8],'fantasyVote':f[9],
           'sourceEvents':event_map(f[10],events),
           'scorecardUrl':links.get((f[0],f[5])),
           'position':f[11],'entered':f[12],'enteredAsReserve':f[13],
           'firstEleven':f[14],'modifier':f[15],
           'usageEvidence':None if f[16] is None else evidence[f[16]]})
    return {'seasonId':season,'localFcaId':row[0],'gazzaCode':row[1],
            'name':row[2],'realDays':[unpack_real_day(x,events) for x in row[3]],
            'fantasy':contexts}

def audit(path,old=None):
    index=parse(path/'fcmRecordsNext_TopFlop.js')
    assert index['schemaVersion']=='4.0-stage-v2-compact'
    assert index['status']=='STAGING_ONLY_NOT_PUBLISHED'
    assert index['maxJsBytes']==MAX_BYTES
    old_facade=parse(old/'fcmRecordsNext_TopFlop.js') if old else None
    old_seasons={x['seasonId']:x for x in old_facade['seasons']} if old_facade else {}
    result=Counter()
    declared={'fcmRecordsNext_TopFlop.js'}
    details_by_season={}
    for md in index['seasons']:
        season=md['seasonId'];result['seasons']+=1
        links={}
        for _,l in files_data(path,md['linksFiles']):
            assert len(l)==3
            k=tuple(l[:2]); assert k not in links
            assert isinstance(l[2],str) and l[2].startswith(('https://','http://'))
            links[k]=l[2]
        result['uniqueScorecardLinks']+=len(links)
        declared.update(md['linksFiles']+md['detailFiles']+md['summaryFiles'])
        detail={}
        detail_file={}
        for filename,packed in files_data(path,md['detailFiles']):
            d=unpack_detail(packed,season,md,links)
            pid=d['localFcaId'];assert pid not in detail
            detail[pid]=d;detail_file[pid]=filename
            result['realDays']+=len(d['realDays'])
            result['fantasyContexts']+=len(d['fantasy'])
            result['resolvedContextLinks']+=sum(f['scorecardUrl'] is not None for f in d['fantasy'])
            assert len({(f['matchId'],f['competitionId'],f['teamId']) for f in d['fantasy']})==len(d['fantasy'])
            assert all(f['competition']!='Youth League' for f in d['fantasy'])
            assert all(f['competitionRound'] is None or f['competitionRound']>0 for f in d['fantasy'])
        summary={}
        for _,packed in files_data(path,md['summaryFiles']):
            r=unpack_summary(packed,season,md)
            pid=r['localFcaId'];assert pid not in summary
            summary[pid]=r
        assert len(summary)==len(detail)==md['players']
        assert sum(len(d['realDays']) for d in detail.values())==md['realDays']
        assert sum(len(d['fantasy']) for d in detail.values())==md['fantasyContexts']
        for pid,d in detail.items():
            r=summary[pid]
            assert r['detailFile']==detail_file[pid]
            assert (r['name'],r['gazzaCode'])==(d['name'],d['gazzaCode'])
        if old is not None:
            older=old_seasons[season]
            original_details={d['localFcaId']:d for _,d in files_data(old,older['detailFiles'])}
            original_summary={d['localFcaId']:d for _,d in files_data(old,older['summaryFiles'])}
            for pid,d in detail.items():
                assert d==original_details[pid],f'V1/V2 DETAIL MISMATCH {season}/{pid}'
                assert {k:v for k,v in summary[pid].items() if k!='detailFile'}=={k:v for k,v in original_summary[pid].items() if k!='detailFile'},f'V1/V2 SUMMARY MISMATCH {season}/{pid}'
            result['semanticallyComparedPlayers']+=len(detail)
        result['players']+=len(detail)
    actual={x.name for x in path.glob('*.js')}
    assert actual==declared, f'file mismatch {actual^declared}'
    result['jsFiles']=len(actual)
    result['totalJsBytes']=sum(p.stat().st_size for p in path.glob('*.js'))
    result['maxJsBytes']=max(p.stat().st_size for p in path.glob('*.js'))
    result['sha256Facade']=hashlib.sha256((path/'fcmRecordsNext_TopFlop.js').read_bytes()).hexdigest()
    return dict(result)

def main():
    p=argparse.ArgumentParser()
    p.add_argument('stage',type=Path)
    p.add_argument('--compare-v1',type=Path)
    p.add_argument('--json-output',type=Path)
    args=p.parse_args()
    result=audit(args.stage,args.compare_v1)
    if args.json_output:
        if args.json_output.exists():raise RuntimeError('Refuse overwrite '+str(args.json_output))
        args.json_output.write_text(json.dumps(result,indent=2,ensure_ascii=False)+'\n',encoding='utf-8')
    print('TOP_FLOP_COMPACT_V2_VALIDATED',json.dumps(result,ensure_ascii=False))

if __name__=='__main__':main()
~~~~

## `tools/Test-RecordsNext4TopFlopCompactBrowser-v2.js`

SHA-256: `ce877d1cabaef233d46b707b8e5ae9db85a25fa5d42da4930e99eaae32b706ab`

~~~~javascript
'use strict';
const fs = require('fs');
const path = require('path');
const vm = require('vm');
const assert = require('assert');
const stage = process.argv[2];
const decoder = process.argv[3];
if (!stage || !decoder) throw new Error('Usage: node test.js <stage> <decoder>');
const context = {window:{}, console};
vm.createContext(context);
function load(name) { vm.runInContext(fs.readFileSync(path.join(stage,name),'utf8'),context,{filename:name}); }
load('fcmRecordsNext_TopFlop.js');
vm.runInContext(fs.readFileSync(decoder,'utf8'),context,{filename:'decoder'});
const api = context.window.RecordsNextTopFlopCompactV2;
let players = 0, realDays = 0, fantasy = 0;
for (const season of context.window.fcmRecordsNextTopFlop.seasons) {
  for (const name of season.summaryFiles) load(name);
  for (const name of season.detailFiles) load(name);
  for (const name of season.linksFiles) load(name);
  const scorecards = api.indexLinks(context.window.fcmRecordsNextTopFlopData.links[season.seasonId] || []);
  const summaries = context.window.fcmRecordsNextTopFlopData.summaries[season.seasonId].map(r => api.decodeSummary(r,season));
  const details = context.window.fcmRecordsNextTopFlopData.details[season.seasonId].map(r => api.decodeDetail(r,season,scorecards));
  assert.strictEqual(summaries.length,season.players);
  assert.strictEqual(details.length,season.players);
  players += details.length;
  for (const d of details) {
    realDays += d.realDays.length;
    fantasy += d.fantasy.length;
    assert.strictEqual(summaries.find(r=>r.localFcaId === d.localFcaId).name,d.name);
    assert.ok(d.fantasy.every(c=>c.competition !== 'Youth League'));
  }
}
assert.ok(players>0);
console.log('TOP_FLOP_COMPACT_BROWSER_OK',JSON.stringify({players,realDays,fantasy}));
~~~~

## `tools/Invoke-RecordsNext4TopFlopCompact-v2.py`

SHA-256: `d518494665a67f66041772a9a0986eaae7a67756cabfe108c4a6cc18fa21c0f7`

~~~~python
#!/usr/bin/env python3
"""Install and validate compact native TopFlop v2 in 4.0 staging ONLY.

Windows PowerShell 5.1 usage: py -3 <this-script.py>.
Never modifies the 3.2 project, databases, published JS, or existing CSS.
The stage folder is keyed to source DB + normalized report hashes and reused
if identical; old v1 staging is never deleted automatically.
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
SOURCE_CLASS='it.alterlega.recordsnext.app.topflop.TopFlopCompactStageJsExporter'
TEST_CLASS='it.alterlega.recordsnext.app.topflop.TopFlopCompactStageSmokeTest'
FILES=[
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopCompactStageJsExporter.java',
 'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopCompactStageSmokeTest.java',
 'visualizzatori/js/fcmRecordsNextTopFlopCompactDecoder-v2.js',
 'tools/Validate-RecordsNext4TopFlopCompact-v2.py',
 'tools/Test-RecordsNext4TopFlopCompactBrowser-v2.js',
 'tools/Invoke-RecordsNext4TopFlopCompact-v2.py',
 'tools/Create-RecordsNext4WorkingCodeMd-v24.py',
 'docs/SPECIFICA_EXPORT_JS_TOP_FLOP_v2.md',
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v25.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v24.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v24.md',
]

def sha(path:Path)->str:
    h=hashlib.sha256()
    with path.open('rb') as stream:
        for block in iter(lambda:stream.read(1048576),b''):h.update(block)
    return h.hexdigest()

def reports_signature(folder:Path)->str:
    files=sorted(folder.rglob('season_normalized_*.json'))
    if not files:raise RuntimeError('No canonical 3.2 season_normalized reports: '+str(folder))
    h=hashlib.sha256()
    for file in files:
        h.update(str(file.relative_to(folder)).replace('\\','/').encode())
        h.update(bytes.fromhex(sha(file)))
    return h.hexdigest()

def invoke(args,logs)->str:
    command=list(map(str,args))
    res=subprocess.run(command,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,
                       text=True,encoding='utf-8',errors='replace')
    logs.append('COMMAND: '+Path(command[0]).name+' (parameters in versioned script)')
    logs.append((res.stdout or '')[-12000:])
    if res.returncode:
        raise RuntimeError('Command failed '+str(res.returncode)+'\n'+(res.stdout or '')[-8000:])
    return res.stdout or ''

def main()->int:
    cli=argparse.ArgumentParser()
    cli.add_argument('--project-dir',default=r'D:\DEV_APPS\RecordsNext4.0')
    cli.add_argument('--downloads-dir',default=r'D:\DEV_APPS\downloads')
    cli.add_argument('--database')
    cli.add_argument('--jar')
    cli.add_argument('--reports-dir')
    cli.add_argument('--seasons',help='Comma-separated test seasons; default all available')
    args=cli.parse_args()
    root=Path(args.project_dir).resolve()
    downloads=Path(args.downloads_dir).resolve()
    downloads.mkdir(parents=True,exist_ok=True)
    stamp=datetime.now().strftime('%Y%m%d_%H%M%S_%f')
    report=downloads/('RecordsNext4_TopFlopCompact_OUTPUT_v2_'+stamp+'.zip')
    build=downloads/('RecordsNext4_TopFlopCompact_BUILD_v2_'+stamp)
    state={'status':'ERROR','checks':[],'installed':[],'alreadyIdentical':[],
           'project':str(root),'version':'v2-compact'}
    logs=[]
    changes=[]
    partial=None
    try:
        if not (root/'pom.xml').is_file():
            raise RuntimeError('Missing RecordsNext 4.0 worktree: '+str(root))
        if not (root/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v23.md').is_file():
            raise RuntimeError('Bibbia v23 missing; verify that v1 exporter installation completed')
        if (root/'.git').exists() and shutil.which('git'):
            branch=invoke(['git','-C',root,'branch','--show-current'],logs).strip()
            if branch!='feature/recordsnext4-players':
                raise RuntimeError('Unexpected Git branch: '+branch)
        manifest=json.loads((KIT/'manifest-sha256.json').read_text(encoding='utf-8'))
        if set(manifest['files'])!=set(FILES):
            raise RuntimeError('Kit manifest file list differs')
        for rel in FILES:
            source=KIT/rel
            dest=root/rel
            if not source.is_file() or manifest['files'][rel]!=sha(source):
                raise RuntimeError('Kit file SHA-256 mismatch: '+rel)
            if dest.exists():
                if not dest.is_file():raise RuntimeError('Destination is not a file: '+rel)
                if sha(source)!=sha(dest):
                    raise RuntimeError('Existing file differs; not overwriting: '+rel)
                state['alreadyIdentical'].append(rel)
        javac=shutil.which('javac');java=shutil.which('java')
        if not javac or not java:raise RuntimeError('Java 21 JDK not found in PATH')
        version=invoke([javac,'-version'],logs).strip()
        match=re.search(r'javac\s+(\d+)',version)
        if not match or int(match.group(1))<21:
            raise RuntimeError('JDK 21+ required, found: '+version)
        db=(Path(args.database).resolve() if args.database else
            next(iter(sorted((root/'data/database').glob('recordsnext-2026_2027-verifica-v1_*.db'))),None))
        if db is None or not db.is_file():
            raise RuntimeError('Updated SQLite not found: supply --database')
        jar=(Path(args.jar).resolve() if args.jar else next((f for f in [
            root/'target/RecordsNext.jar',
            root.parent/'RecordsNext2.0/target/RecordsNext.jar',
            root.parent/'RecordsNext2.0/release/RecordsNext_3.2.0_FULL/payload/RecordsNext.jar'
        ] if f.is_file()),None))
        if jar is None:raise RuntimeError('Original 3.2 runtime JAR missing: supply --jar')
        normalized=(Path(args.reports_dir).resolve() if args.reports_dir else
                    (root.parent/'RecordsNext2.0/data/reports').resolve())
        if not normalized.is_dir():raise RuntimeError('3.2 normalized reports missing: '+str(normalized))
        db_before=sha(db)
        normsha=reports_signature(normalized)
        state['database']=str(db)
        state['databaseSha256Before']=db_before
        state['normalizedReportsFingerprint']=normsha
        stage=root/'data/site-export-staging'/('topflop-compact-v2_'+db_before[:12]+'_'+normsha[:12])/'js'
        state['stage']=str(stage)
        with sqlite3.connect(db.as_uri()+'?mode=ro',uri=True) as c:
            tables=[row[0] for row in c.execute("SELECT name FROM sqlite_master WHERE type='table'")]
        seasons=sorted({m.group(1) for x in tables if (m:=re.fullmatch(
            r'raw_(\d{4}_\d{4})_fcm_formazione',x))})
        if args.seasons:
            selected=args.seasons.split(',')
            if not selected or len(set(selected))!=len(selected) or any(x not in seasons for x in selected):
                raise RuntimeError('Invalid seasons subset')
            seasons=selected
            # A season-scoped pilot must not reuse the full deterministic stage.
            stage=root/'data/site-export-staging'/('topflop-compact-v2_PILOT_'+
                '_'.join(seasons)+'_'+db_before[:12]+'_'+normsha[:12])/'js'
            state['stage']=str(stage)
        if not seasons:raise RuntimeError('No FCM seasons in database')
        state['seasons']=seasons
        classes=build/'classes'
        classes.mkdir(parents=True,exist_ok=True)
        new_class=FILES[0]
        existing=[f for f in (root/'src/main/java').rglob('*.java')
                  if f.relative_to(root).as_posix()!=new_class]
        invoke([javac,'--release','21','-encoding','UTF-8','-d',classes,
                *existing,KIT/new_class],logs)
        state['checks'].append('Full Java 3.2 + TopFlop compact v2 compile')
        baseline=root/'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopArchiveSmokeTest.java'
        if not baseline.is_file():raise RuntimeError('Baseline 52-test model unavailable')
        invoke([javac,'--release','21','-encoding','UTF-8','-cp',classes,'-d',classes,
                baseline,KIT/FILES[1]],logs)
        baseline_output=invoke([java,'-ea','-cp',classes,
            'it.alterlega.recordsnext.app.topflop.TopFlopArchiveSmokeTest'],logs)
        if 'checks=52' not in baseline_output:raise RuntimeError('Baseline 52-test regression missing')
        smoke=invoke([java,'-ea','-cp',classes,TEST_CLASS],logs)
        if 'TOP_FLOP_COMPACT_SMOKE_OK checks=4' not in smoke:
            raise RuntimeError('Compact smoke test missing')
        state['checks'].append('52 baseline checks and 4 compact smoke assertions OK')
        if stage.exists():
            state['reusedStage']=True
            if not stage.is_dir():raise RuntimeError('Stage path exists but not a directory')
        else:
            state['reusedStage']=False
            partial=stage.parent/'js.partial'
            if partial.exists():
                if not partial.is_dir():
                    raise RuntimeError('Incomplete staging path is not a directory: '+str(partial))
                # A prior interrupted runner may have finished writing every JS.
                # Recover ONLY when the mandatory full validator proves it complete.
                invoke([sys.executable,KIT/FILES[3],partial],logs)
                state['checks'].append('Complete prior partial stage recovered by full validation')
            else:
                partial.parent.mkdir(parents=True,exist_ok=True)
                output=invoke([java,'-Xmx1700m','-cp',str(classes)+os.pathsep+str(jar),
                    SOURCE_CLASS,db,partial,normalized,*seasons],logs)
                if 'TOP_FLOP_COMPACT_STAGE_JS_OK' not in output:
                    raise RuntimeError('Compact Java export signature absent')
                state['checks'].append('Native compact JS staged')
        validation=build/'validation.json'
        out=invoke([sys.executable,KIT/FILES[3],
            stage if state['reusedStage'] else partial,'--json-output',validation],logs)
        if 'TOP_FLOP_COMPACT_V2_VALIDATED' not in out:
            raise RuntimeError('Full compact staging validation missing')
        measured=json.loads(validation.read_text(encoding='utf-8'))
        if measured['seasons']!=len(seasons):
            raise RuntimeError('Season coverage differs')
        if measured['maxJsBytes']>950000 or measured['maxJsBytes']>1500000:
            raise RuntimeError('Compact JS exceeds intended safety target')
        if not measured['players'] or not measured['realDays']:
            raise RuntimeError('Empty JS projection')
        state['validation']=measured
        state['checks'].append('Full JS rows, link integrity, file limits and lazy pointers verified')
        node=shutil.which('node')
        if node:
            res=invoke([node,KIT/FILES[4],
                stage if state['reusedStage'] else partial,KIT/FILES[2]],logs)
            if 'TOP_FLOP_COMPACT_BROWSER_OK' not in res:
                raise RuntimeError('JS browser decoder test missing')
            state['checks'].append('Browser JavaScript decoder tested by Node')
        else:
            state['checks'].append('Node not present: JS decoder preverified externally; Python mandatory checks passed')
        if sha(db)!=db_before:raise RuntimeError('Database SHA-256 changed')
        state['databaseSha256After']=sha(db)
        state['checks'].append('SQLite SHA-256 unchanged')
        # Promote only this runner's new stage, after successful validation.
        if not state['reusedStage']:
            partial.rename(stage)
            partial=None
        for name in FILES:
            src=KIT/name;dest=root/name
            if dest.exists():continue
            dest.parent.mkdir(parents=True,exist_ok=True)
            with dest.open('xb') as f:f.write(src.read_bytes())
            changes.append(name)
        generator=root/'tools/Create-RecordsNext4WorkingCodeMd-v24.py'
        bible=root/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v24.md'
        bible_existed=bible.exists()
        try:
            invoke([sys.executable,generator,'--project-dir',root],logs)
        except Exception:
            for name in reversed(changes):(root/name).unlink(missing_ok=True)
            if not bible_existed:bible.unlink(missing_ok=True)
            raise
        state['installed']=changes
        state['checks'].append('Versioned sources, documents, bible v24 installed')
        state['status']='OK'
        print('ESITO: OK')
        print('STAGING (NON PUBBLICATO):',stage)
        print('FILE JS:',measured['jsFiles'])
        print('BYTE TOTALI:',measured['totalJsBytes'])
        print('FILE PIU GRANDE:',measured['maxJsBytes'])
        print('STAGING RIUTILIZZATO:',state['reusedStage'])
        print('BIBBIA: docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v24.md')
    except Exception as e:
        state['error']=repr(e)
        logs.append(traceback.format_exc())
        print('ESITO: ERRORE',file=sys.stderr)
        print(str(e),file=sys.stderr)
    finally:
        with zipfile.ZipFile(report,'w',zipfile.ZIP_DEFLATED) as out:
            out.writestr('result.json',json.dumps(state,indent=2,ensure_ascii=False))
            out.writestr('execution.txt','\n'.join(logs))
            if (build/'validation.json').exists():
                out.write(build/'validation.json','validation.json')
        shutil.rmtree(build,ignore_errors=True)
        # Do not remove a partial stage on failure: keep it for diagnosis; rerun
        # blocks until user explicitly resolves that particular failed directory.
        print('ZIP DA CARICARE:',report)
    return 0 if state['status']=='OK' else 1

if __name__=='__main__':sys.exit(main())
~~~~

## `tools/Create-RecordsNext4WorkingCodeMd-v24.py`

SHA-256: `bfab7aa1be5110095bdb032265c9043af7a36188c18dcf95411fefe27ea0dda4`

~~~~python
#!/usr/bin/env python3
"""Append-only bible v24: complete compact exporter code with per-file SHA-256."""
from __future__ import annotations
import argparse
import hashlib
from pathlib import Path

PREVIOUS='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v23.md'
DEST='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v24.md'
FILES=[
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopCompactStageJsExporter.java',
 'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopCompactStageSmokeTest.java',
 'visualizzatori/js/fcmRecordsNextTopFlopCompactDecoder-v2.js',
 'tools/Validate-RecordsNext4TopFlopCompact-v2.py',
 'tools/Test-RecordsNext4TopFlopCompactBrowser-v2.js',
 'tools/Invoke-RecordsNext4TopFlopCompact-v2.py',
 'tools/Create-RecordsNext4WorkingCodeMd-v24.py',
 'docs/SPECIFICA_EXPORT_JS_TOP_FLOP_v2.md',
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v25.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v24.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v24.md',
]
def sha(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def main():
    p=argparse.ArgumentParser()
    p.add_argument('--project-dir',required=True)
    args=p.parse_args()
    root=Path(args.project_dir).resolve()
    for name in [PREVIOUS,*FILES]:
        if not (root/name).is_file():raise RuntimeError('Missing installed file: '+name)
    lines=['# RecordsNext 4.0 — Bibbia v24 — Exporter Top & Flop compatto','',
        'Base: RecordsNext 3.2.0. Fonte completa SQLite; JS staging sotto 1.500.000 byte ciascuno.',
        'Pipeline, CSS, GUI, database e siti operativi invariati.',
        'Bibbia precedente: `'+PREVIOUS+'`; SHA-256: `'+sha(root/PREVIOUS)+'`','']
    for name in FILES:
        file=root/name
        lang={'.java':'java','.py':'python','.md':'markdown','.js':'javascript'}.get(file.suffix,'text')
        lines.extend(['## `'+name+'`','', 'SHA-256: `'+sha(file)+'`','',
                      '~~~~'+lang,file.read_text(encoding='utf-8-sig').rstrip(),'~~~~',''])
    output=root/DEST
    body='\n'.join(lines).rstrip()+'\n'
    if output.exists():
        if output.read_text(encoding='utf-8')!=body:
            raise RuntimeError('Existing bible differs, refusing overwrite: '+str(output))
        print('BIBBIA v24: GIA IDENTICA')
    else:
        with output.open('x',encoding='utf-8') as out:out.write(body)
        print('BIBBIA v24: OK',output)
if __name__=='__main__':main()
~~~~

## `docs/SPECIFICA_EXPORT_JS_TOP_FLOP_v2.md`

SHA-256: `6566a5b2b596468f7b3bc462765a7b5be5fbcb99c3fc441147ee5889397a4a1f`

~~~~markdown
# RecordsNext 4.0 — Records Top & Flop — Specifica JS compatto v2

## Scopo e principio

La copia AUTORITATIVA dei fatti storici resta il database SQLite derivato dagli archivi FCM/FCA, tramite l'importazione della 3.2. I JS pubblici sono solo una proiezione per la consultazione web. Non creare una seconda banca dati completa e permanente né nuove logiche di pubblicazione.

La v2 ottimizza la codifica dello staging nativo Java già presente, senza cambiare i record o l'interpretazione dei campi. Non è ancora collegata al manifest/publisher multisito della 3.2 e quindi NON si pubblica direttamente.

## Struttura dei file

- `fcmRecordsNext_TopFlop.js` — facade leggera, per ogni stagione con `summaryFiles`, `detailFiles`, `linksFiles` e dizionari `teams`, `competitions`, `eventNames`, `roundDescriptions`, `usageEvidence`.
- `fcmRecordsNext_TopFlop.Summary.AAAA_AAAA.NNN.js` — una riga per giocatore stagionale, recap di voti reali, apporto per squadra e competizione, indice dello shard individuale. Usare i metadati della stessa stagione per decodificare i nomi.
- `fcmRecordsNext_TopFlop.Detail.AAAA_AAAA.NNN.js` — UNA riga compatta per giocatore, con tutte le giornate reali e tutti i contesti FCM ammessi. Stato del voto conservato anche per giornate senza voto; mai sostituirlo con zero.
- `fcmRecordsNext_TopFlop.Links.AAAA_AAAA.NNN.js` — ogni URL canonico unico per `(matchId,teamId)` nella stagione, invece di ripeterlo per ogni calciatore dello stesso incontro.
- `visualizzatori/js/fcmRecordsNextTopFlopCompactDecoder-v2.js` — decoder e caricatore differito (facade → riepilogo stagione → solo dettaglio calciatore → link della stagione su richiesta).

I valori giornalieri e fantasy sono array posizionali e gli eventi non-zero sono vettori alternati `[indiceEvento, valore, ...]`. Gli indici sono relativi ai dizionari della propria stagione. L'identità storica (`gazzaCode`) e l'ID FCA locale restano distinti.

### Contract degli array

Dettaglio: `[localFcaId,gazzaCode,nome,realDays,fantasy]`.

Giornata FCA: `[giornataSerieA,statoVotoOrdinal,voto?,eventi?]`; se non ha voto e non ha eventi sono presenti solo i primi due valori. Stati: `VALID,NO_VOTE,SPECIAL,NOT_PLAYED,UNKNOWN`.

Contesto FCM: `[matchId,competitionId,competitionRound,roundDescriptionIndex,giornataSerieA,teamId,usageOrdinal,titolareIniziale,tabellinoVote,fantasyVote,eventi,pos,entrato,entratoRiserva,firstEleven,modificatore,usageEvidenceIndex]`.

Riepilogo: `[localFcaId,gazzaCode,nome,detailFileIndex,days,withVote,voteSum,meanVote,eventi,contexts,contributions]`; ciascuna contribuzione `[teamId,competitionId,contexts,counted,voted,voteSum,meanVote,fantasySum,meanFantasy,eventi]`.

**Non usare gli ordini o gli indici di una stagione per decodificare file di un'altra stagione.** Il decoder è versione 2; il sito non deve mescolare shard v1 e v2.

## Identità e numerazioni

- Giornata reale: `serieARound` FCA/FCM reale; giornata della competizione: `competitionRound` canonica ricavata da `rn_event` della 3.2, mai `IDGIORNATA` FCM. Incontri non mappati: `competitionRound=null` e descrizione FCM originale.
- Nome squadra e nome competizione per anno sono metadati, non colonne ripetute in ogni partita.
- I link si risolvono per `(stagione,matchId,teamId)` esclusivamente dai report normalizzati della 3.2. Nessun URL dedotto.
- Youth League esclusa; Play Off/Play Out e Spareggio Serie B nel perimetro CAMPIONATO come deciso. Il problema canonico storico delle 100 righe 2022/23 resta aperto, senza alterare il mapping esistente.

## Limiti e caricamento

- TARGET 950.000 byte UTF-8 per singolo JS, LIMITE BLOCCANTE 1.500.000 byte; ogni file scritto viene ricontrollato su disco. Nessuna riga troncata.
- Una pagina web iniziale carica la facade e i riepiloghi delle sole stagioni richieste. Per la scheda del giocatore carica solo il relativo shard Detail e i link della stagione interessata.
- Lo staging compatto è deterministico rispetto all'hash del database e dei report normalizzati: eseguire di nuovo il runner sugli stessi dati riutilizza i file già verificati, senza creare copie di altri 60 MB.
- I vecchi staging v1 non vengono cancellati automaticamente. Prima ispezionarli e, solo dopo conferma dei dati, eliminarli esplicitamente; mai cancellare DB, export pubblicati o archivi storici.

## Contabilità e test obbligatori

Il v2 conserva esattamente il contratto semantico v1: voti e stati, fonti bonus/malus, apporto `COUNTED`, partecipazioni distinte in campionato/coppe, scorecard links e assenza di esclusioni inventate.

Test da effettuare: (1) confronto esaustivo v1/v2 decodificato sulle stagioni 2006/07 e 2026/27; unica differenza ammessa `detailFile` per il nuovo shard; (2) cardinalità sui 21 anni; (3) decodifica JavaScript del browser; (4) URL unici e invariati per la stessa partita/squadra; (5) file fisici <=1.500.000 byte e <=950.000 target; (6) database con hash identico; (7) nessuna modifica a pipeline, publisher, CSS, GUI o sito della 3.2.

## Esito sulla copia di riferimento del 6 novembre 2026

| Metrica | Staging v1 | Staging v2 |
|---|---:|---:|
| JS per 21 stagioni | 430 | 106 |
| Dimensione totale | 372.470.000 byte circa | **59.898.532 byte** |
| File più grande | 949.991 byte | **949.971 byte** |
| Giocatori stagionali | 16.262 | 16.262 |
| Giornate reali | 617.956 | 617.956 |
| Contesti fantasy | 554.425 | 554.425 |
| Riferimenti URL risolti nei contesti | 546.101 | 546.101 |

Riduzione totale circa 83,9%; confronto semantico esatto su 1.419 calciatori delle due stagioni campione e controllo di cardinalità integrale sui 21 anni. **Stato: staging verificato localmente, non ancora pubblicato; verifica Windows da effettuare.**
~~~~

## `docs/PIANO_RECORDSNEXT4_GIOCATORI_v25.md`

SHA-256: `c6b335002cf8d0b8211b515e2f8955e41abe86c70a8c0bad81f6df4415a4c5f8`

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


## Fase 5B — Proiezione JS compatta v2 (6 novembre 2026)

- Archivio SQLite della 3.2 resta l’unica fonte completa, con export JS leggero per il sito.
- Codifica array+dizionari per stagione e URL unici per incontro/squadra; target 950kB, massimo bloccante 1.500.000 byte.
- Verifica integrale: 21 stagioni, 106 JS, 59.898.532 byte, stesso numero di giocatori/giornate/contesti del v1.
- Decoder browser disponibile; test semantico v1/v2 esatto su 2 stagioni.
- In questa fase staging soltanto: catalogo/pipeline/publisher/gui 3.2 ancora da integrare e testare.
- Evitare copie persistenti inutili: stessa fingerprint di DB+report => riutilizzo staging v2; vecchio v1 da eliminare solo esplicitamente.
~~~~

## `docs/REGISTRO_DECISIONI_RECORDSNEXT4_v24.md`

SHA-256: `5011b1480bd9a2c70310abf7d746b5a72ef5cd9d4abfe428f82badcb0cb57591`

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


## Decisioni D-164–D-171 — Ottimizzazione JS Top & Flop

| ID | Stato | Decisione |
|---|---|---|
| D-164 | APPROVATA UTENTE | Non replicare il database storico in output JS verbosi: SQLite FCM/FCA resta fonte completa, la pubblicazione contiene solo dati di consultazione. |
| D-165 | APPROVATA UTENTE | Prima ottimizzare senza perdita di informazioni (array compatti, dizionari, URL deduplicati); eventuali viste più specifiche soltanto dopo misura. |
| D-166 | TESTATA | v2 riduce l’export 21 stagioni a 59.898.532 byte / 106 file; massimo 949.971 byte per JS. |
| D-167 | VINCOLANTE | Ogni file JS UTF-8 non può superare 1.500.000 byte; target 950.000, errore prima di scrivere un giocatore troppo grande. |
| D-168 | VINCOLANTE | Nessun dato reale, contesto FCM, stato voto, evento bonus/malus o URL di tabellino può essere perso per compattare; decodifica semantica controllata contro v1. |
| D-169 | REGOLA | URL unici per matchId e teamId dentro la stagione, ottenuti solo dalla 3.2; dettagli e riepiloghi richiesti in modo differito dal browser. |
| D-170 | REGOLA | Staging v2 riutilizzato se gli hash delle fonti coincidono; non cancellare v1 o dati preesistenti senza autorizzazione. |
| D-171 | NON ANCORA FATTO | Catalogo nativo, GUI, publisher 3.2, cutoff e visualizzatori definitivi ancora da integrare: i JS v2 NON sono pubblicati. |
~~~~

## `docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v24.md`

SHA-256: `3a696cf22e4713c6eb7e2b25318d39329dd9d75349ea2f78abc206288fb70865`

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


## 6 novembre 2026 — Exporter compatto JS Top & Flop v2

- Esaminato sorgente exporter v1 installato e copie aggiornate FCM/FCA/SQLite.
- Implementato exporter Java **additivo** `TopFlopCompactStageJsExporter` e decoder browser v2, senza sovrascrivere la v1 o modificare CSS, GUI e pipeline.
- Test Java di shard e blocco oversized; Python verifica limiti, cardinalità e ricostruzione semantica; Node.js testa decoder browser.
- Due stagioni campione 2006/07 e 2026/27: 1.419 calciatori, 53.922 giornate, 13.700 contesti: 12+ MB v1 -> 2.214.884 byte v2 e confronto semantico completo.
- 21 stagioni: 16.262 calciatori, 617.956 giornate, 554.425 contesti, 546.101 scorecard-link nei contesti, 106 JS per 59.898.532 byte; max 949.971.
- Anche le giornate senza voto restano esplicite e i dettagli non vengono caricati tutti in apertura.
- Nessun sito aggiornato; esecuzione Windows e pubblicazione multisito ancora da completare.
~~~~
