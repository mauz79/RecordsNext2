package it.alterlega.recordsnext.app.topflop;

import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.nio.file.Path;
import java.util.stream.Collectors;

/** Explicit cross-code identity mergers: existing links survive, split guards and rollback remain enforceable. */
public final class TopFlopCrossCodeIdentitySmokeTest {
    private static void check(boolean condition,String why){ if(!condition)throw new AssertionError(why); }
    private static TopFlopPlayerIdentities.Source s(String season,int id,int code,String name){
        return new TopFlopPlayerIdentities.Source(season,id,code,name,"1989-08-09 00:00:00.0");
    }
    private static TopFlopPlayerIdentities.Proposal p(String fp,TopFlopPlayerIdentities.Source... sources){
        return new TopFlopPlayerIdentities.Proposal(fp,List.of(sources),false,"Approva manualmente");
    }
    public static void main(String[] args) throws Exception {
        var a=s("2006_2007",11,13032,"OKAKA Stefano");
        var b=s("2008_2009",22,13032,"OKAKA Stefano");
        var c=s("2011_2012",33,73032,"OKAKA Stefano");
        var d=s("2013_2014",44,73032,"OKAKA CHUKA Stefano");
        var e=s("2018_2019",55,100981,"OKAKA Stefano Chuka");
        var f=s("2019_2020",66,100981,"OKAKA Stefano Chuka");
        var registry=new TopFlopPlayerIdentities.Registry();
        var group1=p("a".repeat(64),a,b);
        var group2=p("b".repeat(64),c,d);
        var group3=p("c".repeat(64),e,f);
        registry.approve(group1);registry.approve(group2);registry.approve(group3);
        check(registry.approvedIdentities()==3,"three previously confirmed histories");
        var cross=p("d".repeat(64),b,c,e);
        check(registry.existingCareerGroups(cross)==3,"three pre-existing approvals detected");
        registry.approve(cross);
        check(registry.approvedIdentities()==1,"all 3 codes belong to one approved historical player");
        check(registry.approvedPlayers()==6,"no stage/source lost in union");
        check(registry.approved().values().stream().distinct().count()==1,"all one person");
        Path tmp=Files.createTempFile("rn4-v42-cross-code-", ".json");
        try {
            TopFlopPlayerIdentities.save(tmp,registry);
            var restored=TopFlopPlayerIdentities.load(tmp);
            check(restored.approvedIdentities()==1,"merged identity survives JSON reload");
            restored.undo(cross);
            check(restored.approvedIdentities()==3,"revoking the cross-code link restores the 3 approved careers");
            check(restored.complete(group1)&&restored.complete(group2)&&restored.complete(group3),
                "all prior explicit approvals preserved on undo");
            restored.approve(cross);
            // New approval joining a previously merged group must not break reversible history.
            var extra=s("2020_2021",77,100981,"OKAKA Stefano Chuka");
            var next=p("e".repeat(64),f,extra);
            restored.approve(next);
            boolean denied=false;
            try{restored.undo(cross);}catch(IllegalArgumentException ex){denied=true;}
            check(denied,"old merger cannot be revoked when a later decision relies on it");
            restored.undo(next);
            restored.undo(cross);
            check(restored.approvedIdentities()==3,"proper LIFO undo restores all old groups");
        } finally { Files.deleteIfExists(tmp); }
        // Same-season collision in expanded groups must stop the merge transaction.
        var x=s("2009_2010",101,1111,"PLAYER A");
        var y=s("2010_2011",102,1111,"PLAYER A");
        var z=s("2009_2010",202,2222,"PLAYER B");
        var w=s("2012_2013",203,2222,"PLAYER B");
        var conflict=new TopFlopPlayerIdentities.Registry();
        conflict.approve(p("1".repeat(64),x,y));conflict.approve(p("2".repeat(64),z,w));
        var before=conflict.approved();
        boolean refused=false;
        try{conflict.approve(p("3".repeat(64),y,w));}catch(IllegalArgumentException ex){refused=true;}
        check(refused,"same-season conflict is blocked");
        check(before.equals(conflict.approved()),"failed union has no side effects");
        System.out.println("RN4_CROSS_CODE_IDENTITY_V42_OK Okaka=3codes 6seasons undo=persisted overlapping-seasons=blocked");
    }
}
