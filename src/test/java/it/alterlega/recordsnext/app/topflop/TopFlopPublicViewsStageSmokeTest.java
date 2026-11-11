package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.topflop.TopFlopFacts.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Deterministic regression: goalkeeper can score, assist, save and concede. */
public final class TopFlopPublicViewsStageSmokeTest {
    private static BigDecimal n(int x) {return BigDecimal.valueOf(x);}
    private static void check(boolean yes,String what) {
        if(!yes)throw new AssertionError(what);
    }
    public static void main(String[] args) throws Exception {
        String season="2026_2027";
        PlayerSeason keeper=new PlayerSeason(season,7001,90001);
        TopFlopArchive archive=new TopFlopArchive();
        var profile=new TopFlopFcaJdbcReader.PlayerProfile(keeper,"PORTIERE TEST",
            Map.of("GiocatoreA.RUOLO","1"));
        Map<String,BigDecimal> events=Map.of("GOLFATTI1",n(1),
            "GOLFATTISURIGORE1",n(1),"ASSIST",n(1),
            "GOLSUBITI",n(2),"GOLSUBITISURIGORE",n(1),"RIGPAR",n(1));
        RealDayKey day1=new RealDayKey(keeper,7);
        RealDayKey day2=new RealDayKey(keeper,8);
        archive.addRealDay(new RealDay(day1,VoteState.VALID,n(8),events,Map.of()));
        archive.addRealDay(new RealDay(day2,VoteState.VALID,n(6),
            Map.of("GOLSUBITI",n(0)),Map.of()));
        var src=Map.of("FCM.Fantasquadra.Nome","Test Team",
            "FCM.Formazione.EntratoRiserva","0","FCM.Giornata.Desc","5a Camp.");
        archive.addFantasyFact(new FantasyFact(new FantasyKey(keeper,1,110,201),day1,
            "Serie A",CompetitionFamily.CAMPIONATO,5,true,Usage.COUNTED,n(14),events,null,src));
        archive.addFantasyFact(new FantasyFact(new FantasyKey(keeper,1,120,202),day1,
            "Serie A",CompetitionFamily.CAMPIONATO,5,true,Usage.COUNTED,n(14),events,null,
            Map.of("FCM.Fantasquadra.Nome","Second Team")));
        archive.addFantasyFact(new FantasyFact(new FantasyKey(keeper,1,130,201),day2,
            "Serie A",CompetitionFamily.CAMPIONATO,6,false,Usage.BENCH_UNUSED,
            null,Map.of(),null,src));
        var loaded=new TopFlopJdbcArchiveAssembler.LoadedSeason(archive,
            Map.of(keeper.fcaPlayerId(),profile),List.of(),
            new TopFlopFcaJdbcReader.SeasonLoad(season,Map.of(keeper.fcaPlayerId(),profile),2,2,2,0,0),
            new TopFlopFcmJdbcReader.SeasonLoad(season,3,3,0,3,0,0),0,2,0);
        var view=TopFlopPublicViewsStageExporter.build(loaded,Map.of(
            new TopFlopCanonicalScorecards.MatchTeam(110,201),
            "https://example.invalid/lega2026/ris.php?Gio=5"));
        check(view.careers.size()==1 && view.teams.size()==2,"player/team aggregates");
        var career=view.careers.get(0);
        @SuppressWarnings("unchecked") var real=(Map<String,Object>)career.get("reale");
        @SuppressWarnings("unchecked") var fantasy=(Map<String,Object>)career.get("fantasy");
        @SuppressWarnings("unchecked") var realEvents=(Map<String,BigDecimal>)real.get("eventi");
        @SuppressWarnings("unchecked") var apported=(Map<String,BigDecimal>)fantasy.get("eventiApportati");
        check(realEvents.get("GOLFATTI1").compareTo(n(1))==0,"one real goal (penalty subset)");
        check(realEvents.get("GOLSUBITI").compareTo(n(2))==0,"two conceded incl penalty");
        check(realEvents.get("RIGPAR").compareTo(n(1))==0,"one penalty save");
        check(realEvents.get("ASSIST").compareTo(n(1))==0,"goalkeeper assist");
        check(apported.get("GOLFATTI1").compareTo(n(2))==0,"two fantasy credited, real once");
        check(fantasy.get("conteggiati").equals(2),"two counted contexts");
        check(fantasy.get("panchinaNonConteggiata").equals(1),"bench unused");
        check(real.get("giornateZeroGolSubitiRegistrati").equals(1),"one explicitly zero goals day");
        check(view.records.stream().anyMatch(x->x.get("recordId").equals("TF20") &&
            x.get("ambito").equals("portieriGolStagione")),"keeper goal TF20");
        check(view.records.stream().anyMatch(x->x.get("recordId").equals("TF07")),"keeper goal TF07");
        check(view.records.stream().anyMatch(x->x.get("recordId").equals("TF08")),"keeper assist TF08");
        check(view.highlights.stream().anyMatch(x->x.get("tipo").equals("prestazioneReale") &&
            x.get("giornataSerieA").equals(7)),"keeper goal highlight");
        check(view.records.stream().anyMatch(x->x.get("recordId").equals("TF18") &&
            x.get("ambito").equals("portieriGolSubitiApportati")),"keeper conceded counted fantasy");
        check(view.records.stream().anyMatch(x->x.get("recordId").equals("TF20") &&
            x.get("ambito").equals("portieriAssistApportati")),"keeper credited assist");
        check(view.records.stream().anyMatch(x->x.get("recordId").equals("TF20") &&
            x.get("ambito").equals("portieriGolSuRigoreApportati")),"keeper credited penalty goal subset");
        @SuppressWarnings("unchecked") Map<String,Object> day2Highlight = view.highlights.stream()
            .filter(x -> x.get("tipo").equals("prestazioneReale") && x.get("giornataSerieA").equals(8))
            .findFirst().orElseThrow();
        @SuppressWarnings("unchecked") var contexts =
            (List<Map<String,Object>>)day2Highlight.get("contestiFantasy");
        check(contexts.size()==1 && contexts.get(0).get("schieramento").equals("RISERVA_NON_ENTRATA") &&
            contexts.get(0).get("conteggiato").equals(false),"unused bench must not say COUNTED");
        @SuppressWarnings("unchecked") Map<String,Object> day1Highlight=view.highlights.stream()
            .filter(x -> x.get("tipo").equals("prestazioneReale") && x.get("giornataSerieA").equals(7))
            .findFirst().orElseThrow();
        @SuppressWarnings("unchecked") List<Map<String,Object>> sourceContexts=
            (List<Map<String,Object>>)day1Highlight.get("contestiFantasy");
        check(sourceContexts.stream().anyMatch(x->
            "https://example.invalid/lega2026/ris.php?Gio=5".equals(x.get("urlTabellino"))),
            "only canonical scorecard URL is reused");
        java.nio.file.Path empty=java.nio.file.Files.createTempDirectory("rn4_tf_views_oversize_");
        try {
            boolean rejected=false;
            try {
                TopFlopPublicViewsStageExporter.shard(empty,season,"Careers",
                    List.of(Map.of("oversize","X".repeat(1_500_000))));
            } catch(java.io.IOException ex) {rejected=ex.getMessage().contains("Oversize");}
            check(rejected,"one oversized view entry must fail rather than truncate or publish");
        } finally {java.nio.file.Files.deleteIfExists(empty);}
        check(view.records.stream().noneMatch(x->List.of("TF11","TF12","TF13","TF14","TF15").contains(x.get("recordId"))),
            "unverified rankings must remain off");
        System.out.println("TOP_FLOP_PUBLIC_VIEWS_KEEPER_AND_DOUBLE_CONTEXT_TEST_OK");
    }
}
