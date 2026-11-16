package it.alterlega.recordsnext.app.topflop;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class TopFlopHistoricalAnomalyV47SmokeTest {
    private static void ok(boolean x,String message){if(!x)throw new AssertionError(message);}
    private static Map<String,Object> season(String id,int n,int sum){
        return Map.of("playerKey",id+":7","nome","GIACOMO TEST",
            "reale",Map.of("votiValidi",n,"sommaVoti",sum,"eventi",Map.of()));
    }
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        var careers=List.of(season("2015_2016",10,80),season("2016_2017",20,120),
            season("2017_2018",20,120),season("2018_2019",10,40));
        var approved=Map.of("2015_2016:7","same","2016_2017:7","same",
            "2017_2018:7","same","2018_2019:7","same");
        var scope=TopFlopHistoricalViews.aggregate(careers,List.of(),approved,
            TopFlopSettings.defaults(),Set.of("TF14","TF15"));
        ok(scope.size()==1,"identity");
        var exceptions=(List<Map<String,Object>>)scope.get(0).get("anomalieStagionali");
        var positive=exceptions.stream().filter(x->"TF14".equals(x.get("recordId"))).findFirst().orElseThrow();
        var negative=exceptions.stream().filter(x->"TF15".equals(x.get("recordId")))
            .filter(x->"2018_2019".equals(x.get("stagione"))).findFirst().orElseThrow();
        ok(((Integer)positive.get("votiValidiBaseline"))==50,"candidate must be excluded from baseline");
        ok(((BigDecimal)positive.get("mediaBaseline")).compareTo(new BigDecimal("5.6"))==0,"baseline weighted mean");
        ok(((BigDecimal)positive.get("delta")).compareTo(new BigDecimal("2.4"))==0,"real positive delta");
        ok(((BigDecimal)negative.get("delta")).compareTo(new BigDecimal("-2.4"))==0,"negative delta");
        var winners=TopFlopHistoricAnomalyWinners.winners(scope,Set.of("TF14","TF15"));
        ok(winners.size()==2,"historical winners");
        ok(winners.stream().anyMatch(x->"TF14".equals(x.get("recordId")) &&
            ((BigDecimal)x.get("valore")).compareTo(new BigDecimal("2.4"))==0),"TF14 winner");
        var cutoff=TopFlopHistoricalViews.aggregate(careers.subList(0,2),List.of(),
            Map.of("2015_2016:7","same","2016_2017:7","same"),
            TopFlopSettings.defaults(),Set.of("TF14","TF15"));
        ok(((List<?>)cutoff.get(0).get("anomalieStagionali")).isEmpty(),"cutoff needs 2 other seasons");
        ok(TopFlopHistoricalViews.aggregate(careers,List.of(),Map.of(),TopFlopSettings.defaults(),
            Set.of("TF14","TF15")).isEmpty(),"unapproved never linked");
        var onlyTF14=TopFlopHistoricalViews.aggregate(careers,List.of(),approved,
            TopFlopSettings.defaults(),Set.of("TF14"));
        ok(((List<Map<String,Object>>)onlyTF14.get(0).get("anomalieStagionali")).stream()
            .noneMatch(x->"TF15".equals(x.get("recordId"))),"respect selected record ids");
        ok(scope.get(0).get("fantamediaReale")==null,"missing rulebook must not score");
        System.out.println("RN4_V47_HISTORIC_TF14_TF15_OK weighted-leave-one-out approved-only cutoff selection");
    }
}
