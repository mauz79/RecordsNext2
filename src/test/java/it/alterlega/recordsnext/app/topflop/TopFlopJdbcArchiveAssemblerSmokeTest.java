package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.topflop.TopFlopFacts.PlayerSeason;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.Usage;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

/** Real SQLite, read-only integration regression for Records Top & Flop. */
public final class TopFlopJdbcArchiveAssemblerSmokeTest {
    private TopFlopJdbcArchiveAssemblerSmokeTest() {}
    public static void main(String[] args) throws Exception {
        if (args.length < 1 || args.length > 2) throw new IllegalArgumentException("database [--all]");
        boolean all = args.length == 2 && args[1].equals("--all");
        boolean one = args.length == 2 && args[1].matches("--season=[0-9]{4}_[0-9]{4}");
        if (args.length == 2 && !all && !one) throw new IllegalArgumentException("Unknown option: " + args[1]);
        if (TopFlopJdbcArchiveAssembler.numericTabellino(" 6,5 ").compareTo(new BigDecimal("6.5")) != 0 ||
            TopFlopJdbcArchiveAssembler.numericTabellino("-0,5").compareTo(new BigDecimal("-0.5")) != 0 ||
            TopFlopJdbcArchiveAssembler.numericTabellino("sv/ng") != null ||
            TopFlopJdbcArchiveAssembler.numericTabellino(null) != null)
            throw new AssertionError("Original tabellino token parser");
        Path db = Path.of(args[0]).toAbsolutePath();
        if (!db.toFile().isFile()) throw new IllegalArgumentException("Missing database: " + db);
        List<String> seasons = one ? List.of(args[1].substring("--season=".length())) : all ? List.of("2006_2007", "2007_2008", "2008_2009", "2009_2010",
            "2010_2011", "2011_2012", "2012_2013", "2013_2014", "2014_2015", "2015_2016",
            "2016_2017", "2017_2018", "2018_2019", "2019_2020", "2020_2021", "2021_2022",
            "2022_2023", "2023_2024", "2024_2025", "2025_2026", "2026_2027") :
            List.of("2006_2007", "2023_2024", "2025_2026", "2026_2027");
        long fantasy = 0, realDays = 0, rosters = 0, excludedYouth = 0;
        Class.forName("org.sqlite.JDBC");
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + db.toUri() + "?mode=ro")) {
            for (String season : seasons) {
                var bundle = TopFlopJdbcArchiveAssembler.loadSeason(c, season);
                var archive = bundle.archive();
                if (archive.fantasyFactCount() != bundle.fcm().allowedFormations() ||
                    archive.realDayCount() != bundle.fca().sourceGiocaInRows() ||
                    bundle.rosterEvents().size() != bundle.fcm().readTesserati() ||
                    bundle.distinctRosterMemberships() != archive.rosterCount())
                    throw new AssertionError("Bad reconciliation: " + season);
                if (bundle.numericTabellinoTotals() + bundle.nonNumericTabellinoTotals() !=
                    bundle.fcm().allowedFormations()) throw new AssertionError("Missing totals");
                if (season.equals("2023_2024") && bundle.fcm().youthExcluded() != 5250)
                    throw new AssertionError("Youth League exclusion changed");
                if (season.equals("2006_2007") &&
                    bundle.rosterEvents().size() <= bundle.distinctRosterMemberships())
                    throw new AssertionError("Repeated Tesserato source IDs were lost");
                if (season.equals("2025_2026")) {
                    var lautaro = bundle.profiles().values().stream()
                        .filter(p -> Integer.valueOf(100843).equals(p.player().gazzaCode()))
                        .findFirst().orElseThrow();
                    PlayerSeason player = lautaro.player();
                    if (archive.realDaysFor(player).size() != 38 ||
                        archive.fantasyFactsFor(player).isEmpty())
                        throw new AssertionError("Lautaro's real days or fantasy contexts");
                    for (var fact : archive.fantasyFactsFor(player)) {
                        if (fact.scorecardUrl() != null || fact.usage() == Usage.EXCLUDED_CONFIRMED ||
                            archive.realDay(fact.realDayKey()) == null)
                            throw new AssertionError("Invented scorecard/exclusion or lost FCA day");
                    }
                }
                fantasy += archive.fantasyFactCount();
                realDays += archive.realDayCount();
                rosters += bundle.rosterEvents().size();
                excludedYouth += bundle.fcm().youthExcluded();
                System.out.println("STAGIONE " + season + " REALI " + archive.realDayCount() +
                    " FANTASY " + archive.fantasyFactCount() + " YOUTH " + bundle.fcm().youthExcluded() +
                    " TESSERAMENTI " + bundle.rosterEvents().size() +
                    " MEMBRI_DISTINTI " + archive.rosterCount());
            }
        }
                System.out.println("TOP_FLOP_ASSEMBLER_OK seasons=" + seasons.size() + " realDays=" +
            realDays + " fantasyFacts=" + fantasy + " rosterEvents=" + rosters +
            " youthExcluded=" + excludedYouth);
    }
}
