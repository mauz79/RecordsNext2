package it.alterlega.recordsnext.app.topflop;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

/** Executable read-only source test; no JUnit dependency. */
public final class TopFlopFcmJdbcSmokeTest {
    private TopFlopFcmJdbcSmokeTest() {}
    public static void main(String[] args) throws Exception {
        if (args.length < 1 || args.length > 2) throw new IllegalArgumentException("database path [--all]");
        boolean all = args.length == 2 && args[1].equals("--all");
        Path db = Path.of(args[0]).toAbsolutePath();
        if (!db.toFile().isFile()) throw new IllegalArgumentException("Database not found: " + db);
        String url = "jdbc:sqlite:" + db.toUri() + "?mode=ro";
        List<String> seasons = all ? List.of("2006_2007", "2007_2008", "2008_2009", "2009_2010",
            "2010_2011", "2011_2012", "2012_2013", "2013_2014", "2014_2015", "2015_2016",
            "2016_2017", "2017_2018", "2018_2019", "2019_2020", "2020_2021", "2021_2022",
            "2022_2023", "2023_2024", "2024_2025", "2025_2026", "2026_2027") :
            List.of("2006_2007", "2023_2024", "2025_2026", "2026_2027");
        long rows = 0, allowed = 0, youth = 0, rosters = 0;
        long[] lautaroContexts = {0};
        Class.forName("org.sqlite.JDBC");
        try (Connection c = DriverManager.getConnection(url)) {
            for (String season : seasons) {
                var data = TopFlopFcmJdbcReader.loadSeason(c, season, new TopFlopFcmJdbcReader.Sink() {
                    public void formation(TopFlopFcmJdbcReader.FormationLine line) {
                        if (line.tabellinoSlot() <= 0 || line.serieARound() <= 0 || line.matchId() <= 0)
                            throw new AssertionError("Malformed formation line: " + line);
                        if (line.player().gazzaCode() != null && line.player().gazzaCode() == 100843)
                            lautaroContexts[0]++;
                    }
                    public void roster(TopFlopFcmJdbcReader.RosterLine line) {
                        if (line.fantasyTeamId() <= 0) throw new AssertionError("Invalid team ID");
                    }
                });
                rows += data.sourceFormations();
                allowed += data.allowedFormations();
                youth += data.youthExcluded();
                rosters += data.readTesserati();
                System.out.println("STAGIONE " + season + " FORMAZIONI " + data.sourceFormations() +
                    " AMMESSE " + data.allowedFormations() + " YOUTH " + data.youthExcluded() +
                    " TESSERATI " + data.readTesserati());
            }
        }
        if (rows != allowed + youth) throw new AssertionError("Row accounting failed");
        if (all && rows <= 0) throw new AssertionError("No FCM formations loaded");
        System.out.println("TOP_FLOP_FCM_JDBC_OK seasons=" + seasons.size() +
            " formations=" + rows + " allowed=" + allowed + " youth=" + youth +
            " rosters=" + rosters + " lautaroContexts=" + lautaroContexts[0]);
    }
}
