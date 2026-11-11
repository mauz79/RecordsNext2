package it.alterlega.recordsnext.app.topflop;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

/** No JDBC dependency: approved identities, collisions, cutoff, weighted averages. */
public final class TopFlopHistoricIdentitySmokeTest {
    static void ok(boolean yes,String why){if(!yes)throw new AssertionError(why);}
    static Map<String,Object> row(String key,int n,int votes,int gol) {
        return Map.of("playerKey",key,"nome","Test player", "reale",
            Map.of("votiValidi",n,"sommaVoti",votes,"eventi",Map.of("GOLFATTI1",gol)));
    }
    public static void main(String[] args)throws Exception {
        var src=List.of(row("2014_2015:4",10,60,2),row("2015_2016:9",20,140,5),
                        row("2016_2017:2",5,10,1),row("2015_2016:15",10,60,0));
        var missing=TopFlopHistoricalViews.aggregate(src,Map.of());
        ok(missing.isEmpty(),"missing approval must NOT merge");
        var approval=Map.of("2014_2015:4","rn4p-2014_2015-4",
            "2015_2016:9","rn4p-2014_2015-4", "2016_2017:2","rn4p-2016_2017-2");
        var grouped=TopFlopHistoricalViews.aggregate(src,approval);
        ok(grouped.size()==1,"only approved 2-season identity");
        var one=grouped.get(0);
        ok(one.get("votiRealiValidi").equals(30),"real votes must count only once");
        ok(((BigDecimal)one.get("mediaVotoReale")).compareTo(new BigDecimal("6.666666666666667"))==0,
            "weighted real average, not average of seasonal averages");
        ok(one.get("gol").equals(BigDecimal.valueOf(7)),"sum real goals, no fantasy duplication");
        ok(((List<?>)one.get("stagioni")).size()==2,"no future season in limited cutoff");
        boolean rejected=false;
        try{TopFlopHistoricalViews.aggregate(src,Map.of(
           "2015_2016:9","rn4p-2014_2015-4", "2015_2016:15","rn4p-2014_2015-4"));}
        catch(IllegalArgumentException expected){rejected=true;}
        ok(rejected,"two distinct source players same season must NOT be merged");

        var r=new TopFlopPlayerIdentities.Registry();
        var p=new TopFlopPlayerIdentities.Proposal("a".repeat(64),List.of(
            new TopFlopPlayerIdentities.Source("2006_2007",224,12302,"ASCOLI Nicola","1979-11-09"),
            new TopFlopPlayerIdentities.Source("2007_2008",364,12302,"ASCOLI Nicola","1979-09-11")),
            false,"Date discordanti");
        ok(r.approved().isEmpty(),"no silent approval");
        r.approve(p);
        var file=Files.createTempFile("rn4_identity",".json");
        try {
            TopFlopPlayerIdentities.save(file,r);
            var restored=TopFlopPlayerIdentities.load(file);
            ok(restored.approvedPlayers()==2 && restored.approvedIdentities()==1,
                "persist and restore explicit approval");
            ok(restored.approved().get("2015_2016:175")==null,
                "Crivello 12302 never inherits Ascoli approval");
        } finally {Files.deleteIfExists(file);}
        System.out.println("RN4_HISTORY_IDENTITY_OK weighted-votes cutoff collisions persistence");
    }
}
