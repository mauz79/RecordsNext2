package it.alterlega.recordsnext.app.topflop;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Can run from target/test-classes with no SQLite JDBC. */
public final class TopFlopIdentityEvidenceSmokeTest {
    private static void ok(boolean value, String message) {if(!value)throw new AssertionError(message);}
    private static TopFlopPlayerIdentities.Source source(String season, int id, int code, String name, String birth) {
        return new TopFlopPlayerIdentities.Source(season,id,code,name,birth);
    }
    private static TopFlopPlayerIdentities.Collision collision(int code, TopFlopPlayerIdentities.Source... src) {
        return new TopFlopPlayerIdentities.Collision("a".repeat(64),code,List.of(src));
    }
    public static void main(String[] args) throws Exception {
        Path directory=args.length==0 ? Path.of("config/identity-evidence") : Path.of(args[0]);
        TopFlopIdentityEvidence lcs=TopFlopIdentityEvidence.load(directory);
        ok(lcs.aliasRows()==569,"Expected 569 LCS alias entries (windows-1252 CSV)");
        ok(lcs.checkedRows()==2133,"Expected 2133 LCS checked records (UTF-8 CSV)");
        ok(lcs.historicalRows()==1531,"Expected 401+561+569 historical alias rows");
        ok(lcs.historicalOnly()>=1,"Historical-only associations must remain available");
        ok(lcs.historicalConflicts()>0,"Conflicting historical changes must be flagged");
        var maggio=collision(500011,
            source("2006_2007",1,500011,"MAGGIO  Cristian","1982-02-11"),
            source("2007_2008",2,500011,"MAGGIO Christian","1982-11-02"));
        ok(lcs.classify(maggio).requiresIndividualReview(),"2015 vs 2020 changed correction is not bulk approvable");
        ok(lcs.classify(maggio).category().equals("FONTI LCS STORICHE DISCORDANTI"),"Historic divergent evidence visible");
        ok(lcs.evidenceDetails(maggio).contains("LCS 2015 old:"),"2015 evidence shown in detail");
        ok(lcs.evidenceDetails(maggio).contains("LCS 2019:"),"2019 evidence shown in detail");
        ok(lcs.evidenceDetails(maggio).contains("LCS 2020:"),"2020 evidence shown in detail");

        var marzoratti=collision(12849,
            source("2006_2007",1,12849,"MARZORATTI Lino","1986-12-10 00:00:00.0"),
            source("2007_2008",1,12849,"MARZORATTI Lino","1986-10-12 00:00:00.0"),
            source("2013_2014",1,12849,"MARZORATI Lino","1986-10-12 00:00:00.0"));
        ok(lcs.classify(marzoratti).category().equals("DATA GIORNO/MESE INVERTITI"),"Marzoratti swapped DOB");
        var iacoponi=collision(14055,
            source("2006_2007",1,14055,"IACOPINI Simone","1987-04-30"),
            source("2007_2008",1,14055,"IACOPONI Simone","1987-04-30"));
        ok(lcs.classify(iacoponi).category().equals("VARIANTE ORTOGRAFICA"),"Iacoponi typo");
        var hamsik=collision(12830,
            source("2007_2008",1,12830,"HAMSYK Marek","1987-07-27"),
            source("2008_2009",1,12830,"HAMSIK Marek","1987-07-27"));
        ok(lcs.classify(hamsik).category().equals("VARIANTE DOCUMENTATA LCS"),"Hamsik confirmed alias in LCS");
        var separate=collision(14085,
            source("2009_2010",1,14085,"FRISON Alberto","1988-01-22"),
            source("2015_2016",2,14085,"ZAPPINO Massimo","1981-06-12"));
        ok(lcs.classify(separate).category().equals("POSSIBILI PERSONE DIVERSE"),"Frison/Zappino collision");
        ok(lcs.classify(separate).requiresIndividualReview(),"severe collision must need individual review");
        var ascoli=collision(12302,
            source("2006_2007",1,12302,"ASCOLI Nicola","1979-11-09"),
            source("2007_2008",1,12302,"ASCOLI Nicola","1979-09-11"),
            source("2015_2016",2,12302,"CRIVELLO Roberto","1991-09-14"));
        ok(lcs.classify(ascoli).requiresIndividualReview(),"Ascoli/Crivello must not auto-merge");
        var reg=new TopFlopPlayerIdentities.Registry();
        ok(reg.approved().isEmpty(),"CSV must NOT grant automatic approval");
        reg.approveSame(iacoponi);
        var backup=reg.snapshot();
        reg.distinguish(separate);
        reg.restore(backup);
        ok(!reg.isDistinct(separate)&&reg.isMerged(iacoponi),"registry transaction snapshot/rollback");
        Path file=Files.createTempFile("identity-evidence-v38", ".json");
        try {
            TopFlopPlayerIdentities.save(file,reg);
            var restored=TopFlopPlayerIdentities.load(file);
            ok(restored.isMerged(iacoponi),"registry persistence");
            ok(!restored.isMerged(separate),"unapproved identities stay separate");
        } finally { Files.deleteIfExists(file); }
        System.out.println("RN4_IDENTITY_EVIDENCE_V39_OK aliases=569 historical=1531 checked=2133 conflicts classification approvals persistence");
    }
}
