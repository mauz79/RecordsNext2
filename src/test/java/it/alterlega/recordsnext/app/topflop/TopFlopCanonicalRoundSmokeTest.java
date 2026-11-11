package it.alterlega.recordsnext.app.topflop;

import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.Map;

/** Read-only integration smoke test against a 3.2 SQLite database. */
public final class TopFlopCanonicalRoundSmokeTest {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Database path required");
        Class.forName("org.sqlite.JDBC");
        try (var db = DriverManager.getConnection("jdbc:sqlite:" +
                 Path.of(args[0]).toAbsolutePath().toUri() + "?mode=ro")) {
            var m2026 = TopFlopCanonicalRoundResolver.loadSeason(db, "2026_2027");
            var real = m2026.get(33814L);
            if (real == null || real.competitionRound() != 5 ||
                real.serieARound() != 7 || real.sourceRoundId() != 2056)
                throw new AssertionError("Canonical 2026 round/real round/source ID mismatch: " + real);
            var m2022 = TopFlopCanonicalRoundResolver.loadSeason(db, "2022_2023");
            if (m2022.containsKey(26002L) || m2022.containsKey(26004L))
                throw new AssertionError("An explicitly excluded 2022 competition was treated as mapped");
            int seasons = 0;
            for (int y=2006;y<=2026;y++) {
                var season = y + "_" + (y+1);
                var rounds = TopFlopCanonicalRoundResolver.loadSeason(db, season);
                if (rounds.isEmpty()) throw new AssertionError("No canonical data for " + season);
                seasons++;
            }
            // Validate that TopFlop uses the mapped ordinal, while preserving the
            // unmapped 2022 FCM facts rather than faking a competition round.
            var bundle2026 = TopFlopJdbcArchiveAssembler.loadSeason(db, "2026_2027");
            boolean found2026 = false;
            for (var profile : bundle2026.profiles().values()) {
                for (var fact : bundle2026.archive().fantasyFactsFor(profile.player())) {
                    if (fact.key().matchId() == 33814L) {
                        if (!Integer.valueOf(5).equals(fact.fantasyRound()) ||
                            !"CANONICAL_3_2".equals(fact.sourceFields().get("TopFlop.CompetitionRoundStatus")))
                            throw new AssertionError("The assembler still uses the FCM source ID instead of competition round");
                        found2026 = true;
                    }
                }
                if (found2026) break;
            }
            if (!found2026) throw new AssertionError("No player for 2026 match 33814");
            var bundle2022 = TopFlopJdbcArchiveAssembler.loadSeason(db, "2022_2023");
            long unmapped = 0;
            for (var profile : bundle2022.profiles().values()) {
                for (var fact : bundle2022.archive().fantasyFactsFor(profile.player())) {
                    if (fact.key().matchId() == 26002L || fact.key().matchId() == 26004L) {
                        if (fact.fantasyRound() != null ||
                            !"UNMAPPED_SOURCE_ONLY".equals(fact.sourceFields().get("TopFlop.CompetitionRoundStatus")))
                            throw new AssertionError("Unmapped 2022 round was fabricated");
                        unmapped++;
                    }
                }
            }
            if (unmapped != 100) throw new AssertionError("Expected 100 preserved unmapped fantasy contexts, got " + unmapped);
            System.out.println("TOP_FLOP_CANONICAL_ROUNDS_OK seasons=" + seasons +
                " 2026Round=" + real.competitionRound() + " serieARound=" + real.serieARound() +
                " sourceId=" + real.sourceRoundId() + " unmapped2022Facts=" + unmapped);
        }
    }
}
