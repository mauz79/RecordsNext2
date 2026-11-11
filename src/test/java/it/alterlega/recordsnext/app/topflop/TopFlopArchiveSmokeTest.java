package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.topflop.TopFlopFacts.*;
import java.math.BigDecimal;
import java.util.Map;

/** Dependency-free executable contract test; run with Java 21, -ea. */
public final class TopFlopArchiveSmokeTest {
    private static int checks;
    private static BigDecimal n(String v) { return new BigDecimal(v); }
    private static void check(boolean ok, String label) {
        checks++;
        if (!ok) throw new AssertionError(label);
    }
    private static void equal(BigDecimal actual, String expected, String label) {
        check(actual != null && actual.compareTo(n(expected)) == 0, label + ": " + actual);
    }
    private static void throwsIllegal(Runnable action, String label) {
        boolean threw = false;
        try { action.run(); } catch (IllegalArgumentException expected) { threw = true; }
        check(threw, label);
    }
    public static void main(String[] args) {
        var player = new PlayerSeason("2025_2026", 366, 100843);
        var archive = new TopFlopArchive();
        for (int day = 1; day <= 5; day++) {
            var vote = switch (day) { case 1 -> n("5.5"); case 2 -> n("7"); case 3 -> n("8"); default -> null; };
            archive.addRealDay(new RealDay(new RealDayKey(player, day),
                    vote == null ? VoteState.NOT_PLAYED : VoteState.VALID, vote,
                    day == 2 ? Map.of("gol", n("1")) : Map.of(), Map.of("source", "FCA")));
        }
        check(archive.realDayCount() == 5, "Five real days");
        var team = 111;
        for (int day = 1; day <= 5; day++) {
            var p = new FantasyFact(new FantasyKey(player, 4, day + 1000, team),
                    new RealDayKey(player, day), "Serie B", CompetitionFamily.CAMPIONATO,
                    day, false, Usage.EXCLUDED_CONFIRMED,
                    switch(day) { case 1 -> n("5.5"); case 2 -> n("10"); case 3 -> n("7.5"); default -> null; },
                    Map.of(), "https://example.invalid/lega2025/ris.php?Gio=" + day, Map.of("source", "FCM"));
            archive.addFantasyFact(p);
        }
        check(archive.fantasyFactCount() == 5, "Five fantasy contexts");
        archive.addFantasyFact(archive.fantasyFactsFor(player).getFirst());
        check(archive.fantasyFactCount() == 5, "Idempotent identical fantasy context");
        archive.addRealDay(archive.realDaysFor(player).getFirst());
        check(archive.realDayCount() == 5, "Idempotent identical real day");
        var result = TopFlopMetrics.summarize(archive, player, team,
                CompetitionFamily.CAMPIONATO, Usage.EXCLUDED_CONFIRMED);
        check(result.contexts() == 5 && result.voteCount() == 3 && result.fantasyVoteCount() == 3,
                "5 excluded, 3 valid votes");
        equal(result.voteSum(), "20.5", "Vote sum");
        equal(result.weightedVote(), "4.1", "Weighted vote");
        check(result.averageVote().subtract(n("6.833333333333333")).abs().compareTo(n("0.00000000000001")) < 0,
                "Mean valid vote");
        equal(result.fantasyVoteSum(), "23", "Fantasy vote total with bonuses/maluses");
        equal(result.weightedFantasyVote(), "4.6", "Weighted fantasy vote");
        check(result.averageFantasyVote().subtract(n("7.666666666666667")).abs().compareTo(n("0.00000000000001")) < 0,
                "Fantasy vote mean");

        // Same real day, different team and cup: distinct legitimate contexts.
        var cup = new FantasyFact(new FantasyKey(player, 5, 2002, team),
                new RealDayKey(player, 2), "Coppa Serie B", CompetitionFamily.COPPE,
                2, true, Usage.COUNTED, n("10"), Map.of("gol", n("3")), null, Map.of());
        var anotherTeam = new FantasyFact(new FantasyKey(player, 4, 3002, 222),
                new RealDayKey(player, 2), "Serie A", CompetitionFamily.CAMPIONATO,
                2, true, Usage.COUNTED, n("10"), Map.of(), null, Map.of());
        archive.addFantasyFact(cup);
        archive.addFantasyFact(anotherTeam);
        check(archive.realDayCount() == 5 && archive.fantasyFactCount() == 7,
                "No doubled real performances across teams/competitions");
        check(TopFlopMetrics.summarize(archive, player, team, CompetitionFamily.COPPE,
                Usage.COUNTED).contexts() == 1, "Cup separate from league");
        check(TopFlopMetrics.summarize(archive, player, 222, null,
                Usage.EXCLUDED_CONFIRMED).contexts() == 0, "Other team has no exclusions");
        check(TopFlopMetrics.summarize(archive, player, 222, null,
                Usage.EXCLUDED_CONFIRMED).averageVote() == null, "Undefined mean, not zero");
        check(archive.fantasyFactsFor(player).get(5).scorecardUrl() == null,
                "No synthetic scorecard URL");

        throwsIllegal(() -> new FantasyFact(new FantasyKey(player, 10, 5000, 123),
                new RealDayKey(player, 1), "Youth League", CompetitionFamily.COPPE,
                1, false, Usage.UNKNOWN, null, Map.of(), null, Map.of()), "Youth League rejected");
        throwsIllegal(() -> archive.addFantasyFact(new FantasyFact(
                new FantasyKey(player, 3, 909, 222), new RealDayKey(player, 100),
                "Serie C", CompetitionFamily.CAMPIONATO, 1, false, Usage.UNKNOWN,
                null, Map.of(), null, Map.of())), "Missing real day rejected");
        throwsIllegal(() -> archive.addRealDay(new RealDay(new RealDayKey(player, 1),
                VoteState.VALID, n("9"), Map.of(), Map.of())), "Conflicting real day rejected");
        var otherSeason = new PlayerSeason("2024_2025", 392, 100843);
        var otherPlayer = new PlayerSeason("2025_2026", 392, 99999);
        check(!otherSeason.equals(player) && !otherPlayer.equals(player),
                "No cross-season or same-ID identity blending");
        check(TopFlopCompetitionPolicy.mappedNameCount() == 24, "24 approved competition names");
        check(TopFlopCompetitionPolicy.requireFamily("Play Off - Play Out") == CompetitionFamily.CAMPIONATO,
                "Play Off maps to league");
        check(TopFlopCompetitionPolicy.requireFamily("Spareggio Serie B") == CompetitionFamily.CAMPIONATO,
                "Spareggio maps to league");
        throwsIllegal(() -> TopFlopCompetitionPolicy.requireFamily("New unexplained competition"),
                "Unmapped competition fails closed");
        for (String name : new String[] { "Campionato", "Serie A", "Serie B", "Serie C",
                "Play Off - Play Out", "Spareggio Serie B" }) {
            check(TopFlopCompetitionPolicy.requireFamily(name) == CompetitionFamily.CAMPIONATO,
                    "League category: " + name);
        }
        for (String name : new String[] { "Coppa AlterEgo", "Coppa Reame Perduto", "Coppa Serie A",
                "Coppa Serie B", "Coppa Serie C", "Coppa UEFA", "Coppa dei Campioni",
                "Coppa di Lega", "Coppa tra le Coppe", "Europa Pipps", "Mitropa Cup",
                "Super Coppa Reame Perduto", "Supercoppa AlterEgo", "Supercoppa Serie A",
                "Supercoppa Serie B", "Supercoppa Serie C", "Supercoppa di Lega",
                "Vera finale di Coppa tra le Coppe" }) {
            check(TopFlopCompetitionPolicy.requireFamily(name) == CompetitionFamily.COPPE,
                    "Cup category: " + name);
        }
        throwsIllegal(() -> TopFlopCompetitionPolicy.requireFamily("  youth league  "),
                "Youth League excluded with spaces");
        var roster = new RosterSnapshot(player, 111, Map.of("source", "Tesserato"));
        archive.addRosterSnapshot(roster);
        archive.addRosterSnapshot(new RosterSnapshot(player, 222, Map.of("source", "Tesserato")));
        archive.addRosterSnapshot(new RosterSnapshot(player, 333, Map.of("source", "Tesserato")));
        check(roster.fantasyTeamId() == 111, "Roster snapshot preserved without dates");
        check(archive.rosterCount() == 3 && archive.rostersFor(player).size() == 3,
                "Simultaneous three-team roster snapshots");
        archive.addRosterSnapshot(roster);
        check(archive.rosterCount() == 3, "Idempotent roster snapshot");
        System.out.println("TOP_FLOP_JAVA_TEST_OK checks=" + checks);
    }
}
