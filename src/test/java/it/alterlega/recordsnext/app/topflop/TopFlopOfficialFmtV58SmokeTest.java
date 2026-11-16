package it.alterlega.recordsnext.app.topflop;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.Map;

/** JDK-only tests plus optional SQLite integration using project runtime JDBC driver. */
public final class TopFlopOfficialFmtV58SmokeTest {
    private static void check(boolean condition, String why) {
        if (!condition) throw new AssertionError(why);
    }
    public static void main(String[] args) throws Exception {
        Map<String,Object> correct = TopFlopFcaOfficialFantasy.verifiedRow(
            "2012_2013",4,28,28,new BigDecimal("5.25"));
        check(correct != null, "Official FCA row missing");
        check("2012_2013:4".equals(correct.get("playerKey")), "Wrong player key");
        check(((BigDecimal) correct.get("sommaFantavotiReali")).compareTo(
            new BigDecimal("147.00")) == 0, "Official weighted FMT");
        check(new BigDecimal("5.25").compareTo((BigDecimal)correct.get("fantamediaReale")) == 0,
            "Official FMT cannot be reconstructed from FCM");
        check(TopFlopFcaOfficialFantasy.verifiedRow("2012_2013",4,28,27,
            new BigDecimal("5.259")) == null, "Outdated Storico must not be preferred");
        check(TopFlopFcaOfficialFantasy.verifiedRow("2012_2013",4,28,28,null) == null,
            "Missing FMT is not zero");
        check(TopFlopFcaOfficialFantasy.verifiedRow("2012_2013",4,0,0,
            BigDecimal.ZERO) == null, "No match-vote sample");
        if (args.length == 1) {
            try (var c=DriverManager.getConnection("jdbc:sqlite:"+Path.of(args[0]).toAbsolutePath().toUri()+"?mode=ro")) {
                var loaded=TopFlopJdbcArchiveAssembler.loadSeason(c,"2012_2013");
                var result=TopFlopFcaOfficialFantasy.extract(c,"2012_2013",loaded,null);
                var quality=result.quality();
                check(((Number)quality.get("votiValidiFCA")).intValue()==9936,
                    "Corrected FCA vote total missing");
                check(((Number)quality.get("discordanzePresenzeFCA")).intValue()==0,
                    "FCA Statistiche.PRESENZE not reconciled");
                var abbiati=result.rows().stream().filter(r ->
                    "2012_2013:4".equals(r.get("playerKey"))).findFirst().orElseThrow();
                check(((Number)abbiati.get("votiRealiValidi")).intValue()==28,
                    "2012 Abbiati should have 28 votes");
                check(((BigDecimal)abbiati.get("fantamediaReale")).compareTo(
                    new BigDecimal("5.25"))==0,"2012 FMT must come from official FCA");
                System.out.println("RN4_V58_DB_OK "+quality);
            }
        }
        System.out.println("RN4_V58_FCA_OFFICIAL_FMT_SMOKE_OK");
    }
}
