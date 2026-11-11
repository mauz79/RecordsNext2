package it.alterlega.recordsnext.app.topflop;

import java.util.List;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;

/** Regression: resolved code collision must not block a cross-code proposal. */
public final class TopFlopResolvedCollisionGuardV43SmokeTest {
    private static void check(boolean ok,String message) {
        if(!ok)throw new AssertionError(message);
    }
    private static TopFlopPlayerIdentities.Source source(String year,int id,int code,String name) {
        return new TopFlopPlayerIdentities.Source(year,id,code,name,"1989-08-09 00:00:00.0");
    }
    private static TopFlopPlayerIdentities.Proposal proposal(String fingerprint,TopFlopPlayerIdentities.Source... sources) {
        return new TopFlopPlayerIdentities.Proposal(fingerprint,List.of(sources),false,"Cross-code manual proposal");
    }
    private static void mustBlock(TopFlopPlayerIdentities.Registry registry,
           TopFlopPlayerIdentities.Audit audit,TopFlopPlayerIdentities.Proposal p) {
        boolean blocked=false;
        try {TopFlopPlayerIdentities.requireResolvedCollisions(registry,audit,p);}
        catch(IllegalArgumentException ex){
            blocked=ex.getMessage().contains("13032");
        }
        check(blocked,"unresolved code 13032 must block");
    }
    public static void main(String[] args) throws Exception {
        var a=source("2009_2010",111,13032,"OKAKA Stefano");
        var b=source("2010_2011",222,13032,"OKAKA CHUKA Stefano");
        var c=source("2013_2014",333,73032,"OKAKA CHUKA Stefano");
        var d=source("2014_2015",444,73032,"OKAKA CHUKA Stefano");
        var collision=new TopFlopPlayerIdentities.Collision("a".repeat(64),13032,List.of(a,b));
        var cross=proposal("b".repeat(64),b,c,d);
        var audit=new TopFlopPlayerIdentities.Audit(List.of(cross),List.of(collision),4);

        var registry=new TopFlopPlayerIdentities.Registry();
        mustBlock(registry,audit,cross);
        check(registry.approvedPlayers()==0,"guard is read only");

        // Real-world situation: code 13032 was approved BEFORE proposing 13032 + 73032.
        registry.approve(proposal("c".repeat(64),a,b));
        check(registry.isMerged(collision),"code 13032 resolved by approved union");
        TopFlopPlayerIdentities.requireResolvedCollisions(registry,audit,cross);
        registry.approve(cross);
        check(registry.complete(cross),"cross-code proposal can now be approved");
        check(registry.approvedPlayers()==4,"four season-local rows retained");
        check(registry.approvedIdentities()==1,"one historical player");
        Path temp=Files.createTempFile("rn4-v43-okaka-", ".json");
        try {
            TopFlopPlayerIdentities.save(temp,registry);
            var restored=TopFlopPlayerIdentities.load(temp);
            restored.bindAudit(audit);
            check(restored.complete(cross),"approved union persists after reload");
            check(restored.isMerged(collision),"resolved collision persists after reload");
            TopFlopPlayerIdentities.requireResolvedCollisions(restored,audit,cross);
        } finally {Files.deleteIfExists(temp);}

        // Explicitly separated codes also count as resolved, but cannot bypass
        // the strict cluster check when merging people explicitly marked distinct.
        var sep=new TopFlopPlayerIdentities.Registry();
        sep.distinguish(collision);
        check(sep.isDistinct(collision),"explicit separation recorded");
        TopFlopPlayerIdentities.requireResolvedCollisions(sep,audit,cross);

        // A second, still unresolved collision MUST block a proposal touching it.
        var other=new TopFlopPlayerIdentities.Collision("d".repeat(64),73032,List.of(c,d));
        var mixed=new TopFlopPlayerIdentities.Audit(List.of(cross),List.of(collision,other),4);
        var pending=new TopFlopPlayerIdentities.Registry();
        pending.approve(proposal("e".repeat(64),a,b));
        boolean blocked=false;
        try{TopFlopPlayerIdentities.requireResolvedCollisions(pending,mixed,cross);}
        catch(IllegalArgumentException ex){blocked=ex.getMessage().contains("73032");}
        check(blocked,"unresolved second collision must still block");
        System.out.println("RN4_RESOLVED_COLLISION_GUARD_V43_OK Okaka 13032->73032, persistence, unresolved blocked");
    }
}
