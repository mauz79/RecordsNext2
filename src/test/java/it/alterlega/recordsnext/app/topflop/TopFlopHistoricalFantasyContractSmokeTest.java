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
