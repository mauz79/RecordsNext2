package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.topflop.TopFlopFcaJdbcReader.SeasonLoad;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.PlayerSeason;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.RealDay;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.VoteState;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Read-only integration checks on the actual RecordsNext 3.2 SQLite schema. */
public final class TopFlopFcaJdbcSmokeTest {
    private TopFlopFcaJdbcSmokeTest() {}

    public static void main(String[] args) throws Exception {
        if (args.length < 1 || args.length > 2)
            throw new IllegalArgumentException("Usage: <recordsnext.db> [--quick]");
        boolean quick = args.length == 2 && "--quick".equals(args[1]);
        if (args.length == 2 && !quick) throw new IllegalArgumentException("Unknown option: " + args[1]);
        Path file = Path.of(args[0]).toAbsolutePath().normalize();
        String url = "jdbc:sqlite:" + file.toUri() + "?mode=ro";
        Class.forName("org.sqlite.JDBC");
        try (Connection c = DriverManager.getConnection(url)) {
            List<String> seasons = new ArrayList<>();
            try (Statement s = c.createStatement(); ResultSet r = s.executeQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' " +
                    "AND name LIKE 'raw_%_fca_giocain' ORDER BY name")) {
                while (r.next()) {
                    String name = r.getString(1);
                    String season = name.substring(4, 13);
                    if (name.equals("raw_" + season + "_fca_giocain") && season.matches("[0-9]{4}_[0-9]{4}"))
                        seasons.add(season);
                }
            }
            check(seasons.size() == 21, "Expected 21 imported FCA seasons, got " + seasons.size());
            int total = 0, valid = 0, zero = 0, players = 0;
            int independentlyCounted = 0, independentlyCountedPlayers = 0;
            int seasonsFullyRead = 0;
            for (String season : seasons) {
                if (quick && !(season.equals("2006_2007") || season.equals("2025_2026") ||
                               season.equals("2026_2027"))) continue;
                seasonsFullyRead++;
                TopFlopArchive archive = new TopFlopArchive();
                SeasonLoad result = TopFlopFcaJdbcReader.loadSeason(c, archive, season, null);
                check(result.realDaysAdded() == archive.realDayCount(), "Wrong real day count " + season);
                check(result.sourceGiocaInRows() == result.realDaysAdded(), "Loss of rows " + season);
                check(result.validVotes() + result.nonValidVotes() == result.realDaysAdded(), "Vote status mismatch " + season);
                check(result.zeroOrUnclassifiedVotes() <= result.nonValidVotes(), "Invalid unknown count " + season);
                if (season.equals("2006_2007")) {
                    check(result.realDaysAdded() == 24966, "Wrong 2006 day count");
                    check(result.playersByLocalId().size() == 657, "Wrong 2006 player count");
                }
                if (season.equals("2025_2026")) {
                    check(result.realDaysAdded() == 29754, "Wrong 2025 day count");
                    check(result.playersByLocalId().size() == 783, "Wrong 2025 player count");
                    PlayerSeason lautaro = result.playersByLocalId().values().stream()
                        .map(TopFlopFcaJdbcReader.PlayerProfile::player)
                        .filter(p -> Integer.valueOf(100843).equals(p.gazzaCode()))
                        .findFirst().orElseThrow();
                    List<RealDay> days = archive.realDaysFor(lautaro);
                    check(days.size() == 38, "Wrong Lautaro day count");
                    check(days.stream().filter(d -> d.voteState() == VoteState.VALID).count() == 30,
                        "Wrong Lautaro valid votes");
                    check(days.get(0).sourceFields().containsKey("GiocaIn.IDPUNTEGGIO"), "Missing GiocaIn provenance");
                    check(days.get(0).sourceFields().containsKey("Punteggio.FINDER1"), "Missing source flags");
                    check(days.get(0).sourceFields().containsKey("GiocatoreA.CODICEGAZZA"), "Missing player code");
                }
                try (Statement independent = c.createStatement(); ResultSet source = independent.executeQuery(
                        "SELECT count(*) FROM raw_" + season + "_fca_giocain")) {
                    independentlyCounted += source.getInt(1);
                }
                try (Statement independent = c.createStatement(); ResultSet source = independent.executeQuery(
                        "SELECT count(*) FROM raw_" + season + "_fca_giocatorea")) {
                    independentlyCountedPlayers += source.getInt(1);
                }
                total += result.realDaysAdded(); valid += result.validVotes();
                zero += result.zeroOrUnclassifiedVotes(); players += result.playersByLocalId().size();
                System.out.println("STAGIONE " + season + " GIOCATORI " + result.playersByLocalId().size() +
                        " GIORNATE " + result.realDaysAdded() + " VOTI " + result.validVotes());
            }
            check(total == independentlyCounted, "Wrong overall GiocaIn count: " + total);
            check(players == independentlyCountedPlayers, "Wrong overall players count: " + players);
            int lautaroSeasons = 0, collisionSeasons = 0;
            for (String season : seasons) {
                SeasonLoad one = TopFlopFcaJdbcReader.loadSeason(c, new TopFlopArchive(), season, 100843);
                if (!one.playersByLocalId().isEmpty()) {
                    lautaroSeasons++;
                    check(one.playersByLocalId().size() == 1, "Lautaro duplicate in " + season);
                }
                SeasonLoad other = TopFlopFcaJdbcReader.loadSeason(c, new TopFlopArchive(), season, 12302);
                if (!other.playersByLocalId().isEmpty()) collisionSeasons++;
            }
            check(lautaroSeasons == 9, "Wrong Lautaro season count " + lautaroSeasons);
            check(collisionSeasons == 3, "Wrong collision code count " + collisionSeasons);
            boolean rejected = false;
            try { TopFlopFcaJdbcReader.loadSeason(c, new TopFlopArchive(), "2025_2026;DROP TABLE X", null); }
            catch (IllegalArgumentException ok) { rejected = true; }
            check(rejected, "Season SQL injection was accepted");
            System.out.println("TOP_FLOP_FCA_JDBC_OK seasons=" + seasons.size() +
                " readSeasons=" + seasonsFullyRead + " players=" + players + " days=" + total + " validVotes=" + valid +
                " zeroOrUnknown=" + zero + " lautaroSeasons=" + lautaroSeasons);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
