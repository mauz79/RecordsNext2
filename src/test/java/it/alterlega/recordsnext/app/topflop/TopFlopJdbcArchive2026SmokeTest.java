package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.topflop.TopFlopFacts.PlayerSeason;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/** Read-only, fixed-source checks for the 2026/27 archive imported by the 3.2 RAW importer. */
public final class TopFlopJdbcArchive2026SmokeTest {
    private TopFlopJdbcArchive2026SmokeTest() {}
    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("updated 2026/27 SQLite path required");
        Path database = Path.of(args[0]).toAbsolutePath();
        if (!database.toFile().isFile()) throw new IllegalArgumentException("Missing DB: " + database);
        Class.forName("org.sqlite.JDBC");
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + database.toUri() + "?mode=ro")) {
            var data = TopFlopJdbcArchiveAssembler.loadSeason(connection, "2026_2027");
            if (data.archive().realDayCount() != 28956 ||
                data.archive().fantasyFactCount() != 7650 ||
                data.rosterEvents().size() != 813 ||
                data.distinctRosterMemberships() != 812 ||
                data.fcm().youthExcluded() != 0)
                throw new AssertionError("Unexpected current 2026/27 import snapshot; verify source file, do not modify it");
            var profile = data.profiles().get(629);
            if (profile == null || !Integer.valueOf(102681).equals(profile.player().gazzaCode()))
                throw new AssertionError("Varela identity mismatch");
            PlayerSeason varela = profile.player();
            long registrations = data.rosterEvents().stream()
                .filter(x -> x.fantasyTeamId() == 101 && x.player().equals(varela))
                .count();
            boolean old = data.rosterEvents().stream().anyMatch(x -> x.fantasyTeamId() == 101 &&
                x.player().equals(varela) && x.tesseratoId() == 699);
            boolean current = data.rosterEvents().stream().anyMatch(x -> x.fantasyTeamId() == 101 &&
                x.player().equals(varela) && x.tesseratoId() == 752);
            if (registrations != 2 || !old || !current || data.archive().rostersFor(varela).stream().filter(x -> x.fantasyTeamId() == 101).count() != 1)
                throw new AssertionError("Varela/FortisSIMY original registration events not preserved");
            try (Statement statement = connection.createStatement();
                ResultSet r = statement.executeQuery("SELECT min(i.GIORNATADIA),max(i.GIORNATADIA) " +
                    "FROM raw_2026_2027_fcm_formazione f JOIN raw_2026_2027_fcm_incontro i ON i.ID=f.IDINCONTRO")) {
                if (!r.next() || r.getInt(1) != 3 || r.getInt(2) != 9)
                    throw new AssertionError("AlterLega formation rounds must be 3..9 in the supplied test snapshot");
            }
            System.out.println("TOP_FLOP_2026_UPDATED_OK realDays=" + data.archive().realDayCount() +
                " fantasyFacts=" + data.archive().fantasyFactCount() + " rosterEvents=" +
                data.rosterEvents().size() + " varelaRegistrations=" + registrations +
                " fantasySerieARounds=3..9");
        }
    }
}
