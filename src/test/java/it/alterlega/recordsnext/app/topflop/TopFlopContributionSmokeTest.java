package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.topflop.TopFlopFacts.*;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.Map;

/** Execute with java -ea; data coverage never inferred from roster snapshots. */
public final class TopFlopContributionSmokeTest {
    private static int checks;
    private static void check(boolean ok, String name) {
        checks++;
        if (!ok) throw new AssertionError(name);
    }
    private static BigDecimal n(String v) {return new BigDecimal(v);}
    private static void eq(BigDecimal x,String v,String label) {
        check(x!=null && x.compareTo(n(v))==0,label+" actual="+x);
    }
    public static void main(String[] args) throws Exception {
        var p = new PlayerSeason("2026_2027",411,100843);
        var archive = new TopFlopArchive();
        var day = new RealDayKey(p,7);
        archive.addRealDay(new RealDay(day, VoteState.VALID,n("7"),Map.of("ASSIST",n("2")),Map.of()));
        archive.addFantasyFact(new FantasyFact(new FantasyKey(p,1,101,1),day,"Serie A",CompetitionFamily.CAMPIONATO,
            5,true,Usage.COUNTED,n("9"),Map.of("ASSIST",n("2")),null,Map.of("FCM.Tabellino.Voto","7")));
        archive.addFantasyFact(new FantasyFact(new FantasyKey(p,2,102,1),day,"Coppa Serie A",CompetitionFamily.COPPE,
            2,false,Usage.BENCH_UNUSED,n("9"),Map.of(),null,Map.of("FCM.Tabellino.Voto","7")));
        archive.addFantasyFact(new FantasyFact(new FantasyKey(p,1,103,2),day,"Serie B",CompetitionFamily.CAMPIONATO,
            5,true,Usage.COUNTED,n("9"),Map.of("ASSIST",n("2")),null,Map.of("FCM.Tabellino.Voto","7")));
        var all = TopFlopContributionSummary.summarize(archive,p,null,null,null);
        check(all.fantasyContexts()==3 && all.countedMatches()==2,"one actual day, two counted fantasy games");
        eq(all.realVoteSum(),"14","two scorecard contexts, not real career count");
        eq(all.fantasyTotalSum(),"18","two fantasy scorecard totals");
        eq(all.sourceEventsApportioned().get("ASSIST"),"4","credited events only for counted contexts");
        eq(all.normalizedEventsApportioned().get("assist"),"4","normalized assist counted only when used");
        check(archive.realDayCount()==1,"real appearance not duplicated");
        var team1 = TopFlopContributionSummary.summarize(archive,p,1,null,null);
        check(team1.fantasyContexts()==2 && team1.countedMatches()==1,"team 1 did not use cup reserve");
        eq(team1.fantasyTotalSum(),"9","team 1 apportioned only league total");
        var cup = TopFlopContributionSummary.summarize(archive,p,1,CompetitionFamily.COPPE,null);
        check(cup.fantasyContexts()==1 && cup.countedMatches()==0 && cup.meanFantasyTotal()==null,
            "bench vote is not apportioned");
        var team2 = TopFlopContributionSummary.summarize(archive,p,2,null,null);
        eq(team2.sourceEventsApportioned().get("ASSIST"),"2","team 2 credited independently");
        check(TopFlopContributionSummary.summarize(archive,p,2,null,"Coppa Serie A").fantasyContexts()==0,
            "specific competition filter");
        if (args.length==1) {
            Class.forName("org.sqlite.JDBC");
            try(var db=DriverManager.getConnection("jdbc:sqlite:"+
                Path.of(args[0]).toAbsolutePath().toUri()+"?mode=ro")) {
                // 2026/27 updated and 2006/07, including unusual historical substitutions.
                for (String season: new String[]{"2006_2007","2026_2027"}) {
                    var loaded = TopFlopJdbcArchiveAssembler.loadSeason(db,season);
                    int counted=0, benchNumeric=0, withoutEvidence=0;
                    for (var profile:loaded.profiles().values())
                        for(var fact:loaded.archive().fantasyFactsFor(profile.player())) {
                            if (fact.usage()==Usage.COUNTED) {
                                counted++;
                                check("FCM_PLAYED_TABELLINO_FIRST11_NUMERIC_TOT".equals(
                                    fact.sourceFields().get("TopFlop.UsageEvidence")),"counted is evidence backed");
                            } else if(fact.usage()==Usage.BENCH_UNUSED && fact.fantasyVote()!=null)benchNumeric++;
                            else if(fact.usage()==Usage.UNKNOWN)withoutEvidence++;
                        }
                    check(counted>0 && benchNumeric>0,"counted and unused numeric bench in "+season);
                    if(season.equals("2026_2027"))check(counted==3327,"2026 FCM first eleven expected 3327");
                    System.out.println("SEASON "+season+" counted="+counted+" benchNumeric="+benchNumeric+
                        " unknown="+withoutEvidence);
                }
            }
        }
        System.out.println("TOP_FLOP_CONTRIBUTION_OK checks="+checks);
    }
}
