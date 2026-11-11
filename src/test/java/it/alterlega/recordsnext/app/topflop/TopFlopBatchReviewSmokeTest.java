package it.alterlega.recordsnext.app.topflop;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Backend regression for the v41 GUI approval flow (Swing click tested on Windows). */
public final class TopFlopBatchReviewSmokeTest {
    private static void ok(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
    private static TopFlopPlayerIdentities.Source s(String season, int id, int code, String name, String birth) {
        return new TopFlopPlayerIdentities.Source(season,id,code,name,birth);
    }
    private static TopFlopPlayerIdentities.Collision collision(int code, TopFlopPlayerIdentities.Source... members) {
        return new TopFlopPlayerIdentities.Collision(String.format("%064x",code),code,List.of(members));
    }
    public static void main(String[] args) throws Exception {
        Path evidenceDir = args.length==0 ? Path.of("config/identity-evidence") : Path.of(args[0]);
        var evidence = TopFlopIdentityEvidence.load(evidenceDir);
        var iacoponi=collision(14055,
            s("2006_2007",11,14055,"IACOPINI Simone","1987-04-30"),
            s("2007_2008",12,14055,"IACOPONI Simone","1987-04-30"));
        var hamsik=collision(12830,
            s("2007_2008",23,12830,"HAMSYK Marek","1987-07-27"),
            s("2008_2009",24,12830,"HAMSIK Marek","1987-07-27"));
        var frisonZappino=collision(14085,
            s("2009_2010",35,14085,"FRISON Alberto","1988-01-22"),
            s("2015_2016",36,14085,"ZAPPINO Massimo","1981-06-12"));
        ok(!evidence.classify(iacoponi).requiresIndividualReview(),"Iacoponi may be approved by an explicit batch click");
        ok(!evidence.classify(hamsik).requiresIndividualReview(),"Hamsik alias may be approved by an explicit batch click");
        ok(evidence.classify(frisonZappino).requiresIndividualReview(),"Real collision always needs an individual confirmation");
        var registry=new TopFlopPlayerIdentities.Registry();
        ok(registry.approvedPlayers()==0,"Evidence NEVER authorizes approval");
        var snapshot=registry.snapshot();
        registry.approveSame(iacoponi);
        registry.approveSame(hamsik);
        registry.restore(snapshot);
        ok(registry.approvedPlayers()==0,"Canceled group is entirely rolled back");
        registry.approveSame(iacoponi);
        registry.approveSame(hamsik);
        registry.distinguish(frisonZappino);
        Path file=Files.createTempFile("rn4-v41-approval", ".json");
        try {
            TopFlopPlayerIdentities.save(file,registry);
            var reloaded=TopFlopPlayerIdentities.load(file);
            reloaded.bindAudit(new TopFlopPlayerIdentities.Audit(List.of(),List.of(frisonZappino),6));
            ok(reloaded.isMerged(iacoponi) && reloaded.isMerged(hamsik),"Approved links persist");
            ok(reloaded.isDistinct(frisonZappino),"Distinct persons persist");
        } finally { Files.deleteIfExists(file); }
        System.out.println("RN4_BATCH_REVIEW_V41_OK safe-batch critical-individual rollback persistence");
    }
}
