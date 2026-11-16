package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.topflop.TopFlopFacts.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.Map;

public final class TopFlopRealFantasyV47SmokeTest {
    private static void ok(boolean x,String why){if(!x)throw new AssertionError(why);}
    public static void main(String[] args) throws Exception {
        var player=new PlayerSeason("2020_2021",1,42);
        var day=new RealDay(new RealDayKey(player,10),VoteState.VALID,new BigDecimal("6"),
            Map.of("GOLFATTI1",BigDecimal.ONE,"GOLFATTISURIGORE1",BigDecimal.ONE,
                "ASSIST",BigDecimal.ONE,"AMM",BigDecimal.ONE),Map.of());
        var coeff=Map.of("GOLFATTI1",new BigDecimal("3"),"ASSIST",BigDecimal.ONE,
            "GOLSUBITI",new BigDecimal("-1"),"RIGPAR",new BigDecimal("3"),
            "RIGSBA",new BigDecimal("-3"),"AUTOGOL1",new BigDecimal("-2"),
            "AMM",new BigDecimal("-0.5"),"ESP",new BigDecimal("-1"));
        var rule=new TopFlopRealFantasyRules.Rule("regolamento-verificato",coeff);
        ok(TopFlopRealFantasyRules.calculate(day,rule).compareTo(new BigDecimal("9.5"))==0,
            "penalty goal must not be counted twice");
        ok(TopFlopRealFantasyRules.calculate(new RealDay(new RealDayKey(player,11),VoteState.NO_VOTE,
            null,Map.of(),Map.of()),rule)==null,"SV never numeric");
        ok(TopFlopRealFantasyRules.empty().forSeason("2020_2021")==null,"no guessed rules");
        var file=Files.createTempFile("rn4-v47-rules-", ".json");
        try {
            String json="{\"schemaVersion\":\"4.0-real-fantasy-rules-v1\","
                +"\"seasons\":{\"2020_2021\":{\"verified\":true,\"source\":\"manual checked rulebook\","
                +"\"penaltyGoalsIncludedInGoals\":true,\"penaltyConcededIncludedInGoalsAgainst\":true,"
                +"\"coefficients\":{\"GOLFATTI1\":3,\"ASSIST\":1,\"GOLSUBITI\":-1,"
                +"\"RIGPAR\":3,\"RIGSBA\":-3,\"AUTOGOL1\":-2,\"AMM\":-0.5,\"ESP\":-1}}}}";
            Files.writeString(file,json);
            ok(TopFlopRealFantasyRules.load(file).forSeason("2020_2021")!=null,"read verified rulebook");
            Files.writeString(file,json.replace("\"verified\":true","\"verified\":false"));
            boolean rejected=false;
            try {TopFlopRealFantasyRules.load(file);}catch(java.io.IOException ex){rejected=true;}
            ok(rejected,"reject unverified rulebook");
        } finally{Files.deleteIfExists(file);}
        System.out.println("RN4_V47_REAL_FANTASY_RULES_OK verified-only penalty-no-double-count SV-null");
    }
}
