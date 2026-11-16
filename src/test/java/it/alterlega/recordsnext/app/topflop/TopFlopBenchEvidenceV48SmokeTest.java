package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.topflop.TopFlopFacts.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class TopFlopBenchEvidenceV48SmokeTest {
    private static void check(boolean v,String message){if(!v)throw new AssertionError(message);}
    private static BigDecimal n(String s){return new BigDecimal(s);}
    private static Map<String,String> source(int pos,int slot,int played,int entered,int reserve) {
        return Map.of("FCM.Fantasquadra.Nome","SCARSI FC", "FCM.Formazione.Pos",String.valueOf(pos),
            "FCM.Tabellino.Slot",String.valueOf(slot),"FCM.Incontro.Giocato",String.valueOf(played),
            "FCM.Formazione.Entrato",String.valueOf(entered),
            "FCM.Formazione.EntratoRiserva",String.valueOf(reserve));
    }
    private static FantasyFact fact(PlayerSeason player,int round,int team,long match,
                                    Usage usage,Map<String,String> src,BigDecimal total) {
        return new FantasyFact(new FantasyKey(player,1,match,team),new RealDayKey(player,round),
            "Serie A",CompetitionFamily.CAMPIONATO,round,false,usage,total,Map.of(),null,src);
    }
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        String season="2025_2026";
        PlayerSeason player=new PlayerSeason(season,17,77017);
        TopFlopArchive archive=new TopFlopArchive();
        var profile=new TopFlopFcaJdbcReader.PlayerProfile(player,"CALCIATORE TEST",
            Map.of("GiocatoreA.RUOLO","3"));
        String[] votes={"7","8","6",null};
        String[] totals={"10","11","8",null};
        for(int i=0;i<4;i++) {
            RealDayKey key=new RealDayKey(player,i+1);
            archive.addRealDay(new RealDay(key,votes[i]==null?VoteState.NO_VOTE:VoteState.VALID,
                votes[i]==null?null:n(votes[i]),Map.of(),Map.of()));
            archive.addFantasyFact(fact(player,i+1,111,100+i,Usage.BENCH_UNUSED,
                source(i+1,12+i,1,0,0),totals[i]==null?null:n(totals[i])));
        }
        // The same real day can serve another team, but it is NOT an unused bench there.
        archive.addFantasyFact(fact(player,1,222,200,Usage.COUNTED,
            source(0,1,1,1,0),n("10")));
        // Unplayed, entered or outside-list lines are never 'certain bench exclusions'.
        archive.addFantasyFact(fact(player,2,333,201,Usage.BENCH_UNUSED,
            source(2,13,0,0,0),n("11")));
        archive.addFantasyFact(fact(player,3,444,202,Usage.BENCH_UNUSED,
            source(3,14,1,1,0),n("8")));
        archive.addFantasyFact(fact(player,4,555,203,Usage.OUTSIDE_LIST,
            source(-1,22,1,0,0),null));
        var loaded=new TopFlopJdbcArchiveAssembler.LoadedSeason(archive,
            Map.of(17,profile),List.of(),new TopFlopFcaJdbcReader.SeasonLoad(season,
                Map.of(17,profile),4,4,3,1,0),
            new TopFlopFcmJdbcReader.SeasonLoad(season,8,8,0,8,0,0),0,7,1);
        Map<TopFlopCanonicalScorecards.MatchTeam,String> links=Map.of(
            new TopFlopCanonicalScorecards.MatchTeam(100,111),
            "https://example.invalid/season/ris.php?Gio=1");
        var result=TopFlopBenchEvidence.analyze(loaded,links,TopFlopSettings.defaults());
        check(result.aggregates().size()==1,"one real documented bench group");
        Map<String,Object> r=result.aggregates().get(0);
        check(r.get("esclusioniCerte").equals(4),"E must include valid and no vote");
        check(r.get("esclusioniConVotoReale").equals(3),"P_V");
        check(r.get("esclusioniConFantavotoTabellino").equals(3),"P_F");
        check(((BigDecimal)r.get("sommaVotiReali")).compareTo(n("21"))==0,"S_V");
        check(((BigDecimal)r.get("sommaFantavotiTabellino")).compareTo(n("29"))==0,"S_F");
        check(((BigDecimal)r.get("mediaVotoReale")).compareTo(n("7"))==0,"TF11 mean");
        check(((BigDecimal)r.get("errorePesatoVoto")).compareTo(n("5.25"))==0,"TF12 weighted");
        check(((BigDecimal)r.get("errorePesatoFantavoto")).compareTo(n("7.25"))==0,"TF13 weighted");
        check(result.records().size()==4,"four TF11 TF12 TF13 rankings");
        check(result.records().stream().map(x->(String)x.get("recordId")).distinct().count()==3,"three distinct IDs");
        check(result.records().stream().allMatch(x->x.get("copertura").equals(TopFlopBenchEvidence.COVERAGE)),
            "status must be explicit");
        check(result.highlights().size()==1,"one selected highlight");
        var observations=(List<Map<String,Object>>)result.highlights().get(0).get("evidenze");
        check(observations.size()==4,"all four source fixtures retained");
        check(observations.get(0).get("urlTabellino").equals("https://example.invalid/season/ris.php?Gio=1"),
            "do not fabricate URL; use canonical link");
        check(observations.get(1).get("urlTabellino")==null,"missing scorecard remains null");
        check(result.counters().get("panchineSenzaProvaSufficiente").equals(2),
            "unplayed and contradictory entered must be excluded");
        check(result.counters().get("fuoriListaNonCandidabili").equals(1),
            "outside list not ownership proof");
        var tooHigh=new TopFlopSettings(5,20,5,3,20,5,2,8,20,2,n("0.75"));
        check(TopFlopBenchEvidence.analyze(loaded,links,tooHigh).records().isEmpty(),
            "thresholds must suppress rankings");
        check(!TopFlopBenchEvidence.verifiedUnusedBench(fact(player,1,111,300,
            Usage.BENCH_UNUSED,source(2,10,1,0,0),n("10"))),"counted slot cannot be bench evidence");
        System.out.println("RN4_V48_TF11_TF13_BENCH_OK E=4 PV=3 PF=3 SV=21 SF=29 weighted=5.25/7.25 guarded");
    }
}
