package it.alterlega.recordsnext.app.topflop;

import java.nio.file.Files;
import java.nio.file.Path;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Regression: one historic code used by two people; one person's seasons stay linked. */
public final class TopFlopSeparatedIdentitySmokeTest {
    private static void ok(boolean value,String msg){if(!value)throw new AssertionError(msg);}
    private static TopFlopPlayerIdentities.Source s(String season,int local,String name,String born){
        return new TopFlopPlayerIdentities.Source(season,local,12302,name,born);
    }
    private static Map<String,Object> career(String key,int votes,int total){
        return Map.of("playerKey",key,"nome","Test", "reale",
            Map.of("votiValidi",votes,"sommaVoti",total,"eventi",Map.of("GOLFATTI1",0)));
    }
    public static void main(String[] args)throws Exception {
        var ascoli06=s("2006_2007",224,"ASCOLI Nicola","1979-11-09 00:00:00.0");
        var ascoli07=s("2007_2008",364,"ASCOLI Nicola","1979-09-11 00:00:00.0");
        var crivello15=s("2015_2016",175,"CRIVELLO Roberto","1991-09-14 00:00:00.0");
        var audit=TopFlopPlayerIdentities.scanSources(List.of(ascoli06,ascoli07,crivello15));
        ok(audit.collisions().size()==1,"one disputed historical code");
        var collision=audit.collisions().get(0);
        ok(collision.code()==12302,"Ascoli/Crivello code");
        var reg=new TopFlopPlayerIdentities.Registry();
        reg.distinguish(collision);  // user's actual decision: different persons
        reg.bindAudit(audit);
        ok(reg.approved().isEmpty(),"distinguishing must not automatically approve internal links");
        var internal=TopFlopPlayerIdentities.internalGroups(collision);
        ok(internal.size()==1,"Ascoli two seasons are an internal group even with date inversion");
        var proposal=internal.get(0);
        ok(proposal.members().size()==2,"Ascoli group has exactly two members");
        ok(proposal.reason().contains("invertiti"),"inverted birth date shown to user");
        ok(proposal.members().stream().noneMatch(x->x.key().equals(crivello15.key())),"Crivello excluded");
        reg.approve(proposal);     // separate second explicit GUI confirmation
        ok(reg.approvedPlayers()==2&&reg.approvedIdentities()==1,"two seasons, one Ascoli identity");
        ok(!reg.approved().containsKey(crivello15.key()),"Crivello remains a separate person");
        var history=TopFlopHistoricalViews.aggregate(List.of(
            career(ascoli06.key(),7,42),career(ascoli07.key(),3,21),career(crivello15.key(),10,65)),reg.approved());
        ok(history.size()==1,"one Ascoli historical career, no Crivello merge");
        var h=history.get(0);
        ok(h.get("numeroStagioni").equals(2),"Ascoli keeps both seasons");
        ok(h.get("votiRealiValidi").equals(10),"no Crivello votes included");
        ok(((BigDecimal)h.get("mediaVotoReale")).compareTo(new BigDecimal("6.3"))==0,"weighted mean Ascoli");
        var dangerous=new TopFlopPlayerIdentities.Proposal("b".repeat(64),
            List.of(ascoli07,crivello15),false,"erroneous manual association");
        boolean rejected=false;
        try{reg.approve(dangerous);}catch(IllegalArgumentException ex){rejected=true;}
        ok(rejected,"previously separated people must NEVER merge indirectly");
        ok(reg.approvedPlayers()==2,"failed approval rolls back without side effects");
        Path file=Files.createTempFile("rn4-ascoli-split", ".json");
        try {
            TopFlopPlayerIdentities.save(file,reg);
            var restored=TopFlopPlayerIdentities.load(file);
            restored.bindAudit(audit);
            ok(restored.isDistinct(collision),"separation persists after reload");
            ok(restored.complete(proposal),"approved Ascoli group persists");
            rejected=false;
            try{restored.approve(dangerous);}catch(IllegalArgumentException ex){rejected=true;}
            ok(rejected,"loaded separation still protects Renewo links");
            restored.undo(collision);
            ok(restored.complete(proposal),"revoking separation does not erase Ascoli link");
        }finally {Files.deleteIfExists(file);}
        System.out.println("RN4_SEPARATED_IDENTITY_V40_OK Ascoli=2 seasons Crivello=separate guarded persisted historical=2");
    }
}
