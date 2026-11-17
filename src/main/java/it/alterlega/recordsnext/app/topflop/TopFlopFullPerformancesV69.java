package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.topflop.TopFlopFacts.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * v69: complete, compact and separately sharded REAL FCA performances and
 * actually COUNTED fantasy performances. This is a staging export, not the
 * historical identity reconciler. All sites must still enforce their cutoff.
 *
 * REAL: [localFcaId,serieARound,voteOrEmpty,eventPairs,keeperFlag]
 * COUNTED: [localFcaId,serieARound,fantasyRoundOr0,teamId,competitionId,
 *           matchId,actualFCMTotal,realVoteOrEmpty,eventPairs,canonicalUrlOrEmpty]
 * The same REAL day is exported exactly once even when used in several FCM
 * fixtures. The context rows remain distinct. Empty != 0 for missing votes.
 */
public final class TopFlopFullPerformancesV69 {
    private TopFlopFullPerformancesV69() {}
    static final String MANIFEST = "fcmRecordsNext_TopFlopFull_v69.js";
    static final List<String> EVENTS = List.of("GOLFATTI1", "GOLFATTISURIGORE1", "ASSIST",
        "GOLSUBITI", "GOLSUBITISURIGORE", "RIGPAR", "RIGSBA", "AUTOGOL1", "AMM", "ESP");

    static List<Object> eventPairs(Map<String,BigDecimal> events) {
        List<Object> vector = new ArrayList<>();
        for (int i=0; i<EVENTS.size(); i++) {
            BigDecimal value = events.get(EVENTS.get(i));
            if (value!=null && value.signum()!=0) {vector.add(i);vector.add(value);}
        }
        return vector;
    }

    static List<Object> real(TopFlopArchive archive, Map<Integer,Integer> roles) {
        List<Object> result = new ArrayList<>();
        Set<String> uniqueness = new LinkedHashSet<>();
        for (RealDay day : archive.allRealDays()) {
            List<Object> events = eventPairs(day.events());
            if (day.voteState()!=VoteState.VALID && events.isEmpty()) continue;
            int pid=day.key().player().fcaPlayerId();
            int round=day.key().serieARound();
            String key=pid+":"+round;
            if (!uniqueness.add(key)) throw new IllegalArgumentException("Duplicate REAL day: "+key);
            result.add(List.of(pid,round,
                day.voteState()==VoteState.VALID?day.vote():"",events,
                roles.getOrDefault(pid,-1)==1?1:0));
        }
        return result;
    }

    static List<Object> counted(TopFlopArchive archive,
          Map<TopFlopCanonicalScorecards.MatchTeam,String> canonicalLinks) {
        List<Object> result = new ArrayList<>();
        Set<String> uniqueness = new LinkedHashSet<>();
        for (FantasyFact fact : archive.allFantasyFacts()) {
            if (fact.usage()!=Usage.COUNTED) continue;
            if (fact.fantasyVote()==null)
                throw new IllegalArgumentException("COUNTED has no FCM TOT: "+fact.key());
            RealDay original = archive.realDay(fact.realDayKey());
            if (original==null) throw new IllegalArgumentException("Missing REAL performance");
            int pid=fact.key().player().fcaPlayerId();
            String key=pid+":"+fact.key().sourceCompetitionId()+":"+
                fact.key().matchId()+":"+fact.key().fantasyTeamId();
            if (!uniqueness.add(key)) throw new IllegalArgumentException("Duplicate COUNTED context: "+key);
            String url=canonicalLinks.get(new TopFlopCanonicalScorecards.MatchTeam(
                fact.key().matchId(),fact.key().fantasyTeamId()));
            result.add(List.of(pid,fact.realDayKey().serieARound(),
                fact.fantasyRound()==null?0:fact.fantasyRound(),
                fact.key().fantasyTeamId(),fact.key().sourceCompetitionId(),
                fact.key().matchId(),fact.fantasyVote(),
                original.voteState()==VoteState.VALID?original.vote():"",
                eventPairs(fact.fantasyEvents()),url==null?"":url));
        }
        return result;
    }

    private static Map<String,Object> object(Object... pairs) {
        Map<String,Object> map=new LinkedHashMap<>();
        for(int i=0;i<pairs.length;i+=2)map.put((String)pairs[i],pairs[i+1]);
        return map;
    }

    public static void main(String[] args) throws Exception {
        if(args.length<4)throw new IllegalArgumentException(
            "Usage: <sqlite> <NEW-stage-dir> <reports-dir-or-dash> <season>...");
        Path db=Path.of(args[0]).toAbsolutePath().normalize();
        Path out=Path.of(args[1]).toAbsolutePath().normalize();
        Path reports=args[2].equals("-")?null:Path.of(args[2]).toAbsolutePath().normalize();
        if(!Files.isRegularFile(db))throw new IOException("SQLite missing: "+db);
        if(Files.exists(out))throw new IOException("Staging must be a NEW directory: "+out);
        if(reports!=null && !Files.isDirectory(reports))throw new IOException("Reports missing: "+reports);
        Set<String> seasons=new LinkedHashSet<>();
        for(int i=3;i<args.length;i++) {
            if(!args[i].matches("[0-9]{4}_[0-9]{4}") || !seasons.add(args[i]))
                throw new IllegalArgumentException("Invalid/duplicate season "+args[i]);
        }
        Files.createDirectories(out);
        Class.forName("org.sqlite.JDBC");
        List<Object> manifestSeasons=new ArrayList<>();
        long totalReal=0,totalCounted=0;
        try(Connection c=DriverManager.getConnection("jdbc:sqlite:"+db.toUri()+"?mode=ro")) {
            for(String season : seasons) {
                var loaded=TopFlopJdbcArchiveAssembler.loadSeason(c,season);
                var urls=reports==null?Map.<TopFlopCanonicalScorecards.MatchTeam,String>of()
                    :TopFlopCanonicalScorecards.load(reports,season);
                Map<Integer,Integer> roles=new LinkedHashMap<>();
                loaded.profiles().forEach((id,p)->{
                    int role;
                    try { role=Integer.parseInt(p.originalFields().get("GiocatoreA.RUOLO")); }
                    catch(RuntimeException ignored){role=-1;}
                    roles.put(id,role);
                });
                List<Object> real=real(loaded.archive(),roles);
                List<Object> counted=counted(loaded.archive(),urls);
                var files=new ArrayList<TopFlopPublicViewsStageExporter.FileInfo>();
                files.addAll(TopFlopPublicViewsStageExporter.shard(out,season,"RealPerformances",real));
                files.addAll(TopFlopPublicViewsStageExporter.shard(out,season,"CountedPerformances",counted));
                manifestSeasons.add(object("stagione",season,"reali",real.size(),
                    "conteggiati",counted.size(),"files",files.stream().map(f->
                    object("nome",f.name(),"tipo",f.kind(),"righe",f.rows(),
                           "byte",f.bytes(),"sha256",f.sha256())).toList()));
                totalReal+=real.size();totalCounted+=counted.size();
                System.out.println("V69_STAGIONE_OK "+season+" real="+real.size()+
                    " counted="+counted.size()+" shards="+files.size());
            }
        }
        Map<String,Object> manifest=object("schemaVersion","4.0-full-performances-v69",
            "eventi",EVENTS,"stagioni",manifestSeasons,
            "garanzia", "VOTO1_FCA_UNICO_PER_GIORNATA_E_TOT_FCM_SOLO_COUNTED",
            "limiteByteJs",TopFlopPublicViewsStageExporter.LIMIT_BYTES);
        byte[] data=("window.fcmRecordsNextTopFlopFullV69 = "+
            TopFlopCompactStageJsExporter.Json.write(manifest)+";\n").getBytes(StandardCharsets.UTF_8);
        if(data.length>TopFlopPublicViewsStageExporter.LIMIT_BYTES)
            throw new IOException("v69 manifest oversize");
        Files.write(out.resolve(MANIFEST),data,StandardOpenOption.CREATE_NEW);
        System.out.println("RN4_V69_STAGE_OK seasons="+seasons.size()+
            " real="+totalReal+" counted="+totalCounted);
    }
}
