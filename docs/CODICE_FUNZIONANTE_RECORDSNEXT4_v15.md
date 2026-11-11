# RecordsNext 4.0 - Bibbia incrementale v15

Primo modello canonico Java della famiglia Records Top & Flop.
**Non integrato ancora nel motore di elaborazione/pubblicazione.**

## Provenienza

- Branch: `feature/recordsnext4-players`
- HEAD: `f9bf30323d9204d70e91e242bddfc72c2060ecf9`
- Riferimento preservato: `docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v14.md`
- SHA-256 bibbia v14: `1b23a918720477e06996eb630d7f099b9b5b62458db688dc1e83e6ed77263283`
- Codice Java 3.2 esistente: invariato in questa installazione.

## File integrali e impronte SHA-256

### `src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopFacts.java`

SHA-256: `6a807620e8a3b326ff7d08a8e24f8f6ff4f3fe44c52f615b4d3d08b443dd516c`

~~~~java
package it.alterlega.recordsnext.app.topflop;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

/**
 * Canonical, immutable facts for the native Records Top & Flop family.
 * These are facts, not the public record catalogue or a replacement publisher.
 * Season-local FCA IDs never stand in for cross-season player identities.
 */
public final class TopFlopFacts {
    private TopFlopFacts() {}

    public enum CompetitionFamily { CAMPIONATO, COPPE }

    /** No vote / special / not played are NOT silently converted to a zero vote. */
    public enum VoteState { VALID, NO_VOTE, SPECIAL, NOT_PLAYED, UNKNOWN }

    /**
     * Only EXCLUDED_CONFIRMED is eligible for a wasted-vote denominator.
     * BENCH_UNUSED and OUTSIDE_LIST are descriptive until eligibility is proven.
     */
    public enum Usage { COUNTED, BENCH_UNUSED, OUTSIDE_LIST, EXCLUDED_CONFIRMED, UNKNOWN }

    public record PlayerSeason(String seasonId, int fcaPlayerId, Integer gazzaCode) {
        public PlayerSeason {
            requireText(seasonId, "seasonId");
            if (fcaPlayerId <= 0) throw new IllegalArgumentException("fcaPlayerId must be positive");
            if (gazzaCode != null && gazzaCode <= 0)
                throw new IllegalArgumentException("gazzaCode must be positive when known");
        }
    }

    /** One actual Serie A day, regardless of how many fantasy matches reference it. */
    public record RealDayKey(PlayerSeason player, int serieARound) {
        public RealDayKey {
            Objects.requireNonNull(player, "player");
            if (serieARound <= 0) throw new IllegalArgumentException("serieARound must be positive");
        }
    }

    public record RealDay(RealDayKey key, VoteState voteState,
                          BigDecimal vote, Map<String, BigDecimal> events,
                          Map<String, String> sourceFields) {
        public RealDay {
            Objects.requireNonNull(key, "key");
            Objects.requireNonNull(voteState, "voteState");
            events = Map.copyOf(Objects.requireNonNull(events, "events"));
            sourceFields = Map.copyOf(Objects.requireNonNull(sourceFields, "sourceFields"));
            if ((voteState == VoteState.VALID) != (vote != null)) {
                throw new IllegalArgumentException("VALID requires a vote; other states require null vote");
            }
            for (var entry : events.entrySet()) {
                requireText(entry.getKey(), "event name");
                Objects.requireNonNull(entry.getValue(), "event value");
            }
        }
    }

    /** Specific FCM team match, not a real matchday or a generic round number. */
    public record FantasyKey(PlayerSeason player, int sourceCompetitionId,
                             long matchId, int fantasyTeamId) {
        public FantasyKey {
            Objects.requireNonNull(player, "player");
            if (sourceCompetitionId <= 0 || matchId <= 0 || fantasyTeamId <= 0)
                throw new IllegalArgumentException("competition/match/team IDs must be positive");
        }
    }

    /**
     * One actual fantasy context. Same player + real round may legitimately
     * appear in several teams and competitions. A scorecard URL is copied only
     * from the existing 3.2 normalizer; it must never be guessed from matchId.
     */
    public record FantasyFact(FantasyKey key, RealDayKey realDayKey,
                              String competitionName, CompetitionFamily family,
                              int fantasyRound, boolean initialStarter,
                              Usage usage, BigDecimal fantasyVote,
                              Map<String, BigDecimal> fantasyEvents,
                              String scorecardUrl, Map<String, String> sourceFields) {
        public FantasyFact {
            Objects.requireNonNull(key, "key");
            Objects.requireNonNull(realDayKey, "realDayKey");
            if (!key.player().equals(realDayKey.player()))
                throw new IllegalArgumentException("Fantasy and real day refer to different season players");
            requireText(competitionName, "competitionName");
            Objects.requireNonNull(family, "family");
            if (TopFlopCompetitionPolicy.requireFamily(competitionName) != family)
                throw new IllegalArgumentException("Wrong family for source competition: " + competitionName);
            if (fantasyRound <= 0) throw new IllegalArgumentException("fantasyRound must be positive");
            Objects.requireNonNull(usage, "usage");
            fantasyEvents = Map.copyOf(Objects.requireNonNull(fantasyEvents, "fantasyEvents"));
            sourceFields = Map.copyOf(Objects.requireNonNull(sourceFields, "sourceFields"));
            for (var entry : fantasyEvents.entrySet()) {
                requireText(entry.getKey(), "fantasy event name");
                Objects.requireNonNull(entry.getValue(), "fantasy event value");
            }
            if (usage == Usage.EXCLUDED_CONFIRMED && initialStarter)
                throw new IllegalArgumentException("Confirmed excluded player cannot be an initial starter");
            // Null URL is allowed. Never manufacture a fallback URL here.
            if (scorecardUrl != null && scorecardUrl.isBlank()) scorecardUrl = null;
        }
    }

    /** Snapshot only. Never assert the player was owned on every round. */
    public record RosterSnapshot(PlayerSeason player, int fantasyTeamId,
                                 Map<String, String> sourceFields) {
        public RosterSnapshot {
            Objects.requireNonNull(player, "player");
            if (fantasyTeamId <= 0) throw new IllegalArgumentException("fantasyTeamId must be positive");
            sourceFields = Map.copyOf(Objects.requireNonNull(sourceFields, "sourceFields"));
        }
    }

    private static void requireText(String value, String label) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException(label + " cannot be empty");
    }
}
~~~~

### `src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopArchive.java`

SHA-256: `b5ac37cc38c9e414c859a56acf8d6712ca81007c87235789f716c61913e50b17`

~~~~java
package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.topflop.TopFlopFacts.FantasyFact;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.FantasyKey;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.PlayerSeason;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.RealDay;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.RealDayKey;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.RosterSnapshot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Non-persistent staging model. Does not write SQLite or install JS.
 * Same real performance is stored once; independent fantasy contexts remain distinct.
 */
public final class TopFlopArchive {
    private record RosterKey(PlayerSeason player, int teamId) {}
    private final Map<RosterKey, RosterSnapshot> rosters = new LinkedHashMap<>();
    private final Map<RealDayKey, RealDay> realDays = new LinkedHashMap<>();
    private final Map<FantasyKey, FantasyFact> fantasyFacts = new LinkedHashMap<>();

    public void addRosterSnapshot(RosterSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        RosterKey key = new RosterKey(snapshot.player(), snapshot.fantasyTeamId());
        RosterSnapshot existing = rosters.putIfAbsent(key, snapshot);
        if (existing != null && !existing.equals(snapshot))
            throw new IllegalArgumentException("Conflicting roster snapshot: " + key);
    }

    public int rosterCount() { return rosters.size(); }

    public List<RosterSnapshot> rostersFor(PlayerSeason player) {
        Objects.requireNonNull(player, "player");
        List<RosterSnapshot> result = new ArrayList<>();
        for (RosterSnapshot snapshot : rosters.values())
            if (snapshot.player().equals(player)) result.add(snapshot);
        return List.copyOf(result);
    }

    public void addRealDay(RealDay day) {
        Objects.requireNonNull(day, "day");
        RealDay existing = realDays.putIfAbsent(day.key(), day);
        if (existing != null && !existing.equals(day))
            throw new IllegalArgumentException("Conflicting real day: " + day.key());
    }

    public void addFantasyFact(FantasyFact fact) {
        Objects.requireNonNull(fact, "fact");
        if (!realDays.containsKey(fact.realDayKey()))
            throw new IllegalArgumentException("Real day not loaded: " + fact.realDayKey());
        FantasyFact existing = fantasyFacts.putIfAbsent(fact.key(), fact);
        if (existing != null && !existing.equals(fact))
            throw new IllegalArgumentException("Conflicting fantasy match/player key: " + fact.key());
    }

    public RealDay realDay(RealDayKey key) { return realDays.get(key); }
    public int realDayCount() { return realDays.size(); }
    public int fantasyFactCount() { return fantasyFacts.size(); }

    public List<RealDay> realDaysFor(PlayerSeason player) {
        Objects.requireNonNull(player, "player");
        List<RealDay> result = new ArrayList<>();
        for (RealDay day : realDays.values())
            if (day.key().player().equals(player)) result.add(day);
        return List.copyOf(result);
    }

    public List<FantasyFact> fantasyFactsFor(PlayerSeason player) {
        Objects.requireNonNull(player, "player");
        List<FantasyFact> result = new ArrayList<>();
        for (FantasyFact fact : fantasyFacts.values())
            if (fact.key().player().equals(player)) result.add(fact);
        return List.copyOf(result);
    }
}
~~~~

### `src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopMetrics.java`

SHA-256: `3378b2f2da9cd64272e9b882c6a991a3e37079c5e6485846b8ff0e3f53865e12`

~~~~java
package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.topflop.TopFlopFacts.CompetitionFamily;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.FantasyFact;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.PlayerSeason;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.RealDay;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.Usage;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.VoteState;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Objects;

/** Individual-only vote/fantasy-vote indicators, never team counterfactuals. */
public final class TopFlopMetrics {
    private static final MathContext PRECISION = MathContext.DECIMAL64;
    private TopFlopMetrics() {}

    /** Nullable means undefined, not zero (e.g. no valid voted exclusions). */
    public record Summary(int contexts, int voteCount, int fantasyVoteCount,
                          BigDecimal voteSum, BigDecimal fantasyVoteSum,
                          BigDecimal averageVote, BigDecimal weightedVote,
                          BigDecimal averageFantasyVote, BigDecimal weightedFantasyVote) {}

    /**
     * Team and competition scope are explicit. Null family means all competition
     * families, but a single fantasy event is never copied or inferred.
     */
    public static Summary summarize(TopFlopArchive archive, PlayerSeason player,
                                    int fantasyTeamId, CompetitionFamily family,
                                    Usage usage) {
        Objects.requireNonNull(archive, "archive");
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(usage, "usage");
        if (fantasyTeamId <= 0) throw new IllegalArgumentException("fantasyTeamId must be positive");
        int count = 0, voted = 0, fVoted = 0;
        BigDecimal votes = BigDecimal.ZERO, fantasyVotes = BigDecimal.ZERO;
        for (FantasyFact fact : archive.fantasyFactsFor(player)) {
            if (fact.key().fantasyTeamId() != fantasyTeamId || fact.usage() != usage
                    || (family != null && fact.family() != family)) continue;
            count++;
            RealDay day = archive.realDay(fact.realDayKey());
            if (day.voteState() != VoteState.VALID) continue;
            voted++;
            votes = votes.add(day.vote());
            if (fact.fantasyVote() != null) {
                fVoted++;
                fantasyVotes = fantasyVotes.add(fact.fantasyVote());
            }
        }
        return new Summary(count, voted, fVoted, votes, fantasyVotes,
                ratio(votes, voted), ratio(votes, count),
                ratio(fantasyVotes, fVoted), ratio(fantasyVotes, count));
    }

    private static BigDecimal ratio(BigDecimal value, int denominator) {
        return denominator == 0 ? null : value.divide(BigDecimal.valueOf(denominator), PRECISION);
    }
}
~~~~

### `src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopCompetitionPolicy.java`

SHA-256: `674dfeca4c6472b2a8dc4b3c01fc6eca500802ca3938d694e07d96d7e95806c0`

~~~~java
package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.topflop.TopFlopFacts.CompetitionFamily;
import java.util.Locale;
import java.util.Map;

/**
 * Approved mapping from the 25 distinct competition names in the 3C1 audit.
 * Unknown names fail closed: they must be reviewed, never silently published.
 * The source competition ID and season remain part of every fantasy key.
 */
public final class TopFlopCompetitionPolicy {
    private TopFlopCompetitionPolicy() {}

    private static final Map<String, CompetitionFamily> FAMILIES = Map.ofEntries(
            Map.entry("campionato", CompetitionFamily.CAMPIONATO),
            Map.entry("serie a", CompetitionFamily.CAMPIONATO),
            Map.entry("serie b", CompetitionFamily.CAMPIONATO),
            Map.entry("serie c", CompetitionFamily.CAMPIONATO),
            Map.entry("play off - play out", CompetitionFamily.CAMPIONATO),
            Map.entry("spareggio serie b", CompetitionFamily.CAMPIONATO),
            Map.entry("coppa alterego", CompetitionFamily.COPPE),
            Map.entry("coppa reame perduto", CompetitionFamily.COPPE),
            Map.entry("coppa serie a", CompetitionFamily.COPPE),
            Map.entry("coppa serie b", CompetitionFamily.COPPE),
            Map.entry("coppa serie c", CompetitionFamily.COPPE),
            Map.entry("coppa uefa", CompetitionFamily.COPPE),
            Map.entry("coppa dei campioni", CompetitionFamily.COPPE),
            Map.entry("coppa di lega", CompetitionFamily.COPPE),
            Map.entry("coppa tra le coppe", CompetitionFamily.COPPE),
            Map.entry("europa pipps", CompetitionFamily.COPPE),
            Map.entry("mitropa cup", CompetitionFamily.COPPE),
            Map.entry("super coppa reame perduto", CompetitionFamily.COPPE),
            Map.entry("supercoppa alterego", CompetitionFamily.COPPE),
            Map.entry("supercoppa serie a", CompetitionFamily.COPPE),
            Map.entry("supercoppa serie b", CompetitionFamily.COPPE),
            Map.entry("supercoppa serie c", CompetitionFamily.COPPE),
            Map.entry("supercoppa di lega", CompetitionFamily.COPPE),
            Map.entry("vera finale di coppa tra le coppe", CompetitionFamily.COPPE)
    );

    public static CompetitionFamily requireFamily(String competitionName) {
        if (competitionName == null || competitionName.isBlank())
            throw new IllegalArgumentException("Competition name missing");
        String key = competitionName.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
        if (key.equals("youth league"))
            throw new IllegalArgumentException("Youth League must never enter Top & Flop");
        CompetitionFamily family = FAMILIES.get(key);
        if (family == null)
            throw new IllegalArgumentException("Unknown competition, explicit classification required: " + competitionName);
        return family;
    }

    public static int mappedNameCount() { return FAMILIES.size(); }
}
~~~~

### `src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopArchiveSmokeTest.java`

SHA-256: `840cba5373e1d351bb06d124c8db811b0e73c84d600c6547c70ac3c77bbe89fd`

~~~~java
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
~~~~

### `src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopArchiveJUnitTest.java`

SHA-256: `3f0a3e237e339c6e588c58bd902fccbb2aea1e29d3266b71293494d890c06eea`

~~~~java
package it.alterlega.recordsnext.app.topflop;

import org.junit.jupiter.api.Test;

/** Makes the executable contract test part of the existing Maven/JUnit 5 suite. */
class TopFlopArchiveJUnitTest {
    @Test
    void canonicalFactsAndIndividualMetrics() {
        TopFlopArchiveSmokeTest.main(new String[0]);
    }
}
~~~~

### `docs/SPECIFICA_MODELLO_JAVA_TOP_FLOP_v1.md`

SHA-256: `0d54b8ee7d027ec371a42d6ee18855b80d9a520f5eed768dbb4c516dd674d10c`

~~~~markdown
# RecordsNext 4.0 - Records Top & Flop - Modello Java v1

Data: 2026-11-01. Prima implementazione del **modello di dominio nativo Java**; ancora NON connessa a configurazione, catalogo, pipeline, exporter, GUI, publisher o siti.

## Provenienza e compatibilita 3.2

Progetto di partenza: `RecordsNext2.0.zip`, commit `f9bf30323d9204d70e91e242bddfc72c2060ecf9`, Java 21. Nessuna classe esistente alterata: sono aggiunti file in `app/topflop`. Il resto di `src/main/java` viene compilato **insieme** ai nuovi sorgenti per controllarne la compatibilita, senza cambiare il ciclo della 3.2.

## Chiavi e fatti

- `PlayerSeason(seasonId, fcaPlayerId, gazzaCode)`: identificativo **locale all'FCA** e codice storico Gazzetta separati. `gazzaCode` puo essere null se non certo. Non si fondono automaticamente annate diverse basandosi sul solo codice: le collisioni storiche restano da verificare.
- `RealDayKey(PlayerSeason, serieARound)`: una prestazione reale per calciatore stagionale/giornata di Serie A. `RealDay` contiene `VoteState`, voto, eventi fonte e `sourceFields`. `NOT_PLAYED`, `NO_VOTE`, `SPECIAL`, `UNKNOWN` NON sono trasformati in voto 0.
- `RosterSnapshot`: fotografia FCM di un tesseramento con fantasquadra e campi originali; **non** dimostra disponibilita per tutte le giornate e non crea automaticamente esclusioni.
- `FantasyKey(PlayerSeason, sourceCompetitionId, matchId, fantasyTeamId)`: identificativo univoco del contesto FCM, non solo giornata o giocatore.
- `FantasyFact`: partita e giornata sportiva, stato (`COUNTED`, `BENCH_UNUSED`, `OUTSIDE_LIST`, `EXCLUDED_CONFIRMED`, `UNKNOWN`), titolarita iniziale indipendente, fantavoto di quel **contesto**, bonus/malus, campi originari, link al tabellino **solo** se gia fornito dalle logiche 3.2. URL sconosciuta = null. Diversi incontri, coppe e squadre possono utilizzare la medesima `RealDay` senza duplicarla.
- `TopFlopArchive`: raccolta in memoria con chiavi e validazione di duplicati incompatibili. Inserimento ripetuto identico e idempotente. Non persiste ne pubblica dati.

I campi `sourceFields` hanno funzione di conservazione progressiva e non costituiscono ancora una importazione integrale dei campi FCM/FCA: l'adattatore/estrattore Java e il collegamento alle tabelle originali sono il **prossimo intervento**.

## Famiglie di competizioni e esclusioni

`TopFlopCompetitionPolicy` codifica **esplicitamente** le 24 denominazioni ammesse emerse nell'audit 3C1 (180 configurazioni in 21 stagioni); la venticinquesima, **Youth League**, e categoricamente esclusa. `Play Off - Play Out` e `Spareggio Serie B` sono `CAMPIONATO`. Altri campionati e coppe mantengono la propria denominazione e il proprio ID FCM stagionale. Una competizione nuova/sconosciuta produce errore (fail-closed), non una classificazione indovinata. Questa famiglia sportiva e distinta dalla futura famiglia record `TOP_FLOP`.

## Metriche individuali gia implementate sul modello

`TopFlopMetrics.summarize` richiede **giocatore stagionale, fantasquadra, famiglia sportiva (oppure tutte), stato d'utilizzo**. Per ogni aggregazione conserva `E` contesti, `P_voto` e `P_fantavoto` validi separati, `SV` e `SF` e calcola:

- `mediaVoto = SV/P_voto` e `indicePesatoVoto = SV/E`;
- `fantamedia = SF/P_fantavoto` e `indicePesatoFantavoto = SF/E`.

Le medie con denominatore zero sono **null** (non 0). Una prestazione senza voto nel contesto accertato contribuisce al denominatore `E`, ma non a `SV`/`SF`. `fantasyVote` null indica valore non conosciuto, mai una conversione implicita del voto reale in fantavoto. I valori originali restano `BigDecimal`, divisione con precisione `DECIMAL64`. Il campo `Usage.EXCLUDED_CONFIRMED` non va assegnato a dati FCM **senza evidenza**: `BENCH_UNUSED` e `OUTSIDE_LIST` rimangono stati distinti.

La funzione non impone soglie massime o minime, non calcola controfattuali della fantasquadra e non applica sostituzioni 3/5/11 o RU: sono regole della fonte da preservare, non da simulare nel modello canonico.

## Verifica

- Test sintetico Java 21 eseguibile in console con assert: **52 verifiche**, inclusi voto 5.5/7/8, 5 esclusioni e 3 voti, fantavoti con bonus/malus, piu fantasquadre, campionato/coppa nello stesso turno, tutte le 24 denominazioni ammesse, Youth League, mancato collegamento reale, collisioni, rosters.
- Compilazione di **tutti** i sorgenti `src/main/java` della 3.2 assieme ai nuovi file riuscita in ambiente di verifica con `javac 21.0.12.1`. Non sono stati eseguiti i test JUnit della 3.2 nell'ambiente di verifica, perche Maven non e installato. Il wrapper `TopFlopArchiveJUnitTest` rende il test disponibile anche a JUnit 5 quando Maven e disponibile sul PC.
- Il runner sul PC compila i sorgenti in una cartella diagnostica sotto `D:\DEV_APPS\downloads`, esegue i 52 assert, conserva i log, genera bibbia v15 e ZIP. Cancella la sola cartella temporanea da esso creata.

## Non implementato in questa fase

Lettura JDBC integrale dei dati individuali e caricamento `RealDay`/`FantasyFact` dai raw FCM/FCA; validita temporale tesseramento; classificazione certa delle esclusioni; formula finale dei record; catalogo `TOP_FLOP` nel `RecordFamily`; GUI; export JS; manifest; shard; URL storici risolti dai normalizzati; pubblicazione. **Nessun record Top & Flop e ancora selezionabile o pubblicabile.**

## Vincoli operativi

La 3.2 operativa resta intatta. Non alterare Java preesistente, SQLite, configurazione o file dei siti in questo intervento. Ogni successivo collegamento al motore deve essere trattato con test completi della pipeline, non con nuove logiche parallele.
~~~~

### `docs/PIANO_RECORDSNEXT4_GIOCATORI_v16.md`

SHA-256: `2a26ecfc71612a4e7d69fc8634040086591e972697b0b1802d1a4935f535203c`

~~~~markdown
# RecordsNext 4.0 - Piano v16: implementazione Java Records Top & Flop

**Decisioni precedenti preservate.** Nuova tappa: modello canonico Java `app/topflop` con real day, roster snapshot, fantasy match, politiche di competizione e indicatori base. Test Java 21 e bibbia v15. Non attivare la famiglia `TOP_FLOP` finche pipeline/JS/GUI/manifest/shard non funzionano.

**Prossima fase:** adattatore Java di sola lettura ai raw FCA/FCM per l'archivio individuale completo (compresi voti, fantavoti, eventi e prove di utilizzo), con confronto sui 21 archivi; solo dopo: motore/archiviazione/catalogo/exporter/publication 3.2. Rimangono aperte la semantica delle esclusioni non in `Formazione`, il fantavoto dell'escluso, i codici storici riutilizzati, soglie e classifica definitiva.

---

# RecordsNext 4.0 - Piano operativo v15

# RecordsNext 4.0 - Piano operativo v14

# RecordsNext 4.0 - Continuita documentale PIANO_RECORDSNEXT4_GIOCATORI_v13

Versione additiva del 2026-11-01. La cronologia integrale precedente e preservata sotto.

# RecordsNext 4.0 - Piano operativo v11

Data: 2026-11-01. Base produttiva 3.2.0 immutata. Branch `feature/recordsnext4-players`; commit di riferimento `f9bf30323d9204d70e91e242bddfc72c2060ecf9`.

## Fasi completate e evidenze

- Fase 0: identita e fonti; eccezioni sui codici storici ancora aperte.
- Fase 1: estrattore FCA sperimentale; output Windows verificato, 7 test. Non integrato nel Java.
- Fase 2: estrattore FCM fantasy sperimentale; output Windows verificato, 8 test. Non integrato nel Java.
- Fase 3A: audit stati `Formazione`, output Windows verificato, 9 test.
- Fase 3B1: audit `Tabellino.LISTA` + `Formazione`, output Windows `RecordsNext4_Fase3B1_OUTPUT_v1_20261101_001744_607896.zip` verificato: 7 test, 21 stagioni, 22.265 tabellini, 552.025 righe `Formazione`, tre SHA-256 dei report corretti, sandbox invariato, bibbia v8 generata. Non equivale a prova definitiva di voto conteggiato.
- Fase 3B2: censimento osservazionale delle regole `Competizione` per stagione e competizione, legame `Incontro.IDGIRONE -> Girone.IDCOMPETIZIONE`, moduli ammessi e tabellini osservati. Strumento prototipo con 8 test locali; lo ZIP Windows deve ancora certificarne l'esecuzione sul PC.

## Passaggi successivi

- Fase 3B3: confrontare configurazioni di competizione, tabellini reali, RU, sostituzioni, eventuali voti speciali e regole per ruolo. Stabilire cosa significa realmente `conteggiato`, `disponibile`, `non sfruttato`, `recuperabile`; distinguere prove osservate, inferenze e casi non simulabili.
- Fase 3C: solo dopo approvazione della semantica: prototipo per metriche individuali fantasy (panchina non impiegata, fuori formazione, sostituzioni, eventuali controfattuali), test su casi reali verificati.
- Fase 4: integrare fonti canoniche in SQLite/Java, esportazione JS stagionale/sito, HTML e GUI. Tutte le componenti richieste fanno parte del traguardo 4.0.0.
- Fase 5: regressioni, documentazione, commit, release 4.0.0.

## Vincoli costanti

1. Prestazione FCA unica per giocatore storico, stagione, giornata: non duplicare se usata in competizioni fantasy diverse.
2. `GiocatoreA.ID` e annuale; `CodiceGazza` contiene il codice storico nella FCA. Le collisioni storiche non sono risolte automaticamente.
3. I codici speciali 0/-1 in `Tabellino.LISTA` non sono calciatori.
4. `ENTRATO`, `ENTRATORISERVA` e posizione nei primi undici slot non bastano, isolatamente, a certificare un voto conteggiato.
5. Il regolamento va ricavato per stagione e competizione, senza assumere tre sostituzioni ovunque. Un archivio FCM e uno snapshot delle impostazioni, non prova di eventuali variazioni avvenute durante la stagione.
6. Conservare integralmente le analisi RU personali della 3.2; non inserirle nei record pubblici senza decisione espressa.
7. Tabellini non collegati, incontri con squadra 0 e riposi: casistiche distinte, nessuna cancellazione.
8. Nessuna scrittura nel DB operativo/sandbox, nessuna modifica a Java, pipeline, siti, GUI, manifest o branch principale durante gli audit.
9. I file nuovi vanno in `D:\DEV_APPS\RecordsNext4.0`, i risultati in `D:\DEV_APPS\downloads`, con nomi versionati.
10. Ogni fase mantiene piano, registro decisioni, diario, specifiche, test e bibbia incrementale con codice integrale e SHA-256. Le versioni pregresse restano intatte.


## Aggiornamento dopo verifica Windows 3B2 e decisione sul record individuale

- 3B2 completata anche su Windows (`ESITO: OK`, 8/8 test): audit documentale delle regole. Non sono state apportate modifiche al database o al motore Java.
- Priorita della fase successiva: **voti non utilizzati dal singolo calciatore**. Niente simulazione del punteggio di fantasquadra e niente ottimizzazione delle sostituzioni.
- Dati minimi da raccogliere per un calciatore: codice storico, ID locale FCA, stagione, fantasquadra, partita e competizione fantasy, giornata reale, indicatore di effettivo utilizzo nei primi 11 del tabellino, presenza di voto reale valido, valore sorgente del voto, riferimento al tabellino.
- Metrica stabilita: somma dei soli voti reali validi nelle esclusioni / **tutte le esclusioni**; conservare separati i due conteggi, non dividerli fra loro.
- Implementazione successiva: un campione reale verificato a mano, poi test automatici sul campione, quindi calcolo storico. Registrare i casi ambigui anziche correggerli arbitrariamente.
- Il campo "valore massimo" menzionato dall'utente rimane **aperto** (nessun cap numerico assunto).
- Continuano a valere l'identita per `CodiceGazza` con verifica collisioni, l'ID FCA locale, l'assenza di duplicazioni delle prestazioni reali e la conservazione dei record precedenti.


## Nuovo assetto approvato per il record individuale (2026-11-01)

- Conservare i quattro indicatori con due numeratori distinti (voto reale valido, fantavoto regolamentare completo) e due denominatori distinti (esclusioni totali, esclusioni con voto valido).
- La Fase 3C si articola in: (a) campioni di esclusioni FCM/FCA con provenienza voto/fantavoto e bonus-malus; (b) audit di tesseramento e partecipazione effettiva per stabilire `E` e `P`; (c) calcolo su casi reali e confronto con tabellino, senza simulare cambi di formazione.
- La stessa prestazione FCA resta un solo fatto reale; un'esclusione e invece un fatto fantasy legato a stagione, fantasquadra, competizione e incontro. Non aggregare ciecamente incontri di competizioni parallele.
- Implementare prima unit test sintetici per i quattro indicatori (v2); i test sintetici NON dimostrano ancora la classificazione delle esclusioni negli archivi.
- Se `E > 0, P = 0`, gli indici pesati valgono zero; le medie sulle gare valide sono non definite. Se `E = 0`, tutte le metriche sono non definite.
- Le RU non sono calciatori e non devono generare un voto individuale sprecato; le regole di sostituzione per competizione restano variabili e indipendenti da questo calcolo.
- Nessun limite artificiale al voto o fantavoto; la voce 'massimo' resta proposta separata e non adottata come soglia.


## Nuova fase 3C0 - Modello archivio completo e audit contesti paralleli

- Definita specifica `SPECIFICA_ARCHIVIO_COMPLETO_GIOCATORE_v1.md` con livello reale, tesseramento e livello evento fantasy, valido anche per titolari.
- Documentate due famiglie, competizioni singole e sovrapposizioni nella stessa giornata; conservazione dei dati originali prima dei filtri.
- Audit `Audit-RecordsNext4Contesti-v1.py` READ-ONLY: report per competizione, copertura, giocatore-squadra-competizione, sovrapposizioni e caso 100843.
- **Prossimo sviluppo**: storicizzare il tesseramento e definire le assenze da Formazione in modo dimostrabile; quindi costruire il dataset sorgente completo e l'estrazione simmetrica voti/fantavoti per utilizzati ed esclusi.
- La verifica Windows del nuovo runner rimane pendente; non toccare Java/GUI/sito.

## Aggiornamento v14 - fase 3C1

- Validazione Windows della 3C0: ZIP `RecordsNext4_Contesti_OUTPUT_v1_20261101_004840_254541.zip`, stato OK, 10 test, 21 stagioni, 552025 righe, SHA256 sandbox uguale prima/dopo, bibbia v12 presente. Non ancora dataset canonico.
- Classificare Play Off - Play Out e Spareggio Serie B CAMPIONATO; escludere Youth League da ogni output record/scheda.
- Audit v2: verificare nuovamente le 21 stagioni, separare originali/ammessi/esclusi, non eliminare dati sorgente.
- Prossima implementazione: associare a ogni evento fantasy la partita FCM e la `scorecardUrl` canonica della stagione corretta, verificata sui file normalizzati 3.2. URL non verificati restano null.
- Solo successivamente generare il dataset completo dei giocatori in Java/SQLite e i record con prova del singolo episodio.


## Aggiornamento v15 - Records Top & Flop come famiglia nativa

**Decisione vincolante dell'utente (2026-11-01):** tutti gli audit dei giocatori delle fasi 0-3C1 sono lavori preparatori di una nuova famiglia nativa **Records Top & Flop**, integrata in RecordsNext 4.0. La pipeline 3.2.0 resta la base esclusiva da estendere. Non costruire sistemi di export, pubblicazione, configurazione, manifest o visualizzazione paralleli.

**Stato attuale:** 3C1 eseguita su Windows, ZIP `RecordsNext4_Contesti_OUTPUT_v2_20261101_005413_578565.zip` verificato: stato OK; 13 test; 21 stagioni; 552025 righe origine, 546775 ammesse, 5250 Youth League escluse; SHA256 database sandbox prima=dopo; bibbia v13 generata. Non e ancora implementata una famiglia Java Top & Flop.

**Ordine vincolato di implementazione**, rispettando la 3.2:
1. Definire fatti canonici: giocatore storico + prestazione FCA unica e contesti per fantasquadra, incontro, competizione; provenienza del voto e dei bonus/malus; link canonico tabellino. Produrre test sui casi 100843, competizioni parallele, Youth League e anomalie.
2. Implementare una sola volta in Java/SQLite i fatti riutilizzabili da scheda giocatore e records; mantenere gli audit Python come verificatori, non produttori operativi permanenti.
3. Inserire `TOP_FLOP` nel catalogo (`RecordFamily`, `CoreRecordCatalog`, dipendenze, selezione e configurazione), senza modificare valori preesistenti e mantenendo la lettura delle configurazioni 3.2.
4. Collegare il calcolo alla `RecordsNextPipeline` con logiche FULL/CONSOLIDATED e archivio storico separato per stagione; eseguire regressione su output invariati delle altre famiglie.
5. Collegare `TopFlopFamilyJsExporter` a `Records2026SitePublisher`, relativa validazione, manifest, `SeasonFamilyShardPublisher`, publisher multisito e link tabellini canonici. Nessun nuovo flusso di deploy.
6. GUI: aggiungere selezione famiglia e singoli records in `RecordsNext2Dashboard` / configurazione vigente. HTML sito: nuova sezione e schede individuali derivate dagli stessi dati senza duplicazione dei calcoli.
7. Eseguire test integrati: nessun impatto su famiglie preesistenti, non oltre 1.5 MiB per shard, siti storici corretti, FULL e CONSOLIDATED, JS valido, nessuna Youth League. Aggiornare bibbia e rilasciare come 4.0.0 soltanto dopo approvazione.

La classificazione CAMPIONATO/COPPE e distinta dalla classificazione **famiglie di record**: `TOP_FLOP` e una famiglia records; CAMPIONATO/COPPE sono filtri del dominio sportivo. Non confonderle nel codice.

**Prossimo passo di implementazione:** realizzazione nel Java 4.0 del modello dati canonico individuale e test di regressione, senza ancora selezionare/pubblicare la famiglia finche il suo calcolo non esiste.
~~~~

### `docs/REGISTRO_DECISIONI_RECORDSNEXT4_v15.md`

SHA-256: `f7d9c0c632cd440f010d76c7df02c3c2168ba47fb978b287732718565e4009db`

~~~~markdown
# RecordsNext 4.0 - Registro decisioni v15

Le decisioni D-001--D-112 sono preservate integralmente dalla v14. Nuove D-113--D-123:

| ID | Stato | Decisione / evidenza |
|---|---|---|
| D-113 | IMPLEMENTATA: MODELLO | Aggiungere la famiglia `app/topflop` al codice Java esistente, senza modificare le classi 3.2 ne attivare prematuramente `RecordFamily.TOP_FLOP`. |
| D-114 | APPROVATA/IMPLEMENTATA | Prestazione reale unica per stagione+ID FCA+giornata Serie A. Eventi fantasy separati per stagione+competizione FCM+incontro+fantasquadra+ID FCA. |
| D-115 | APPROVATA/IMPLEMENTATA | Conservare `CodiceGazza` storico e ID FCA stagionale distinti; non fondere automaticamente collisioni storiche. |
| D-116 | APPROVATA/IMPLEMENTATA | `Tesserato` e una fotografia della rosa, non dimostra tesseramento in una giornata non documentata. Nessuna esclusione inferita dal silenzio FCM. |
| D-117 | APPROVATA/IMPLEMENTATA | Youth League esclusa categoricamente; Play Off - Play Out e Spareggio Serie B = CAMPIONATO; 24 nomi ammessi codificati esplicitamente, altri nomi in revisione obbligatoria. |
| D-118 | APPROVATA/IMPLEMENTATA | Dati per titolari, conteggiati, panchinari e fuori lista; stato `EXCLUDED_CONFIRMED` utilizzabile solo dopo prova sorgente. |
| D-119 | APPROVATA/IMPLEMENTATA | Quattro metriche personali calcolate da voti/fantavoti integrali, `SV/P`, `SV/E`, `SF/P`, `SF/E`. Sulle fonti incomplete mantenere separati contatori voto/fantavoto, non inventare valori. |
| D-120 | APPROVATA/IMPLEMENTATA | Non sintetizzare mai URL tabellini: copiare dal normalizzato/link canonico 3.2; conservare ID incontro e giornata distinti. |
| D-121 | IMPLEMENTATA: TEST | Test Java standard con 52 assert e wrapper JUnit 5; compilazione completa codice Java 3.2+nuovo modulo verificata localmente con JDK 21, non suite Maven completa. |
| D-122 | REGOLA OPERATIVA | Il runner deve verificare JDK 21, git branch, file preesistenti e collisioni prima dell'installazione; output diagnostico sotto `D:\DEV_APPS\downloads`, mai `%TEMP%`; niente sovrascritture. |
| D-123 | APERTA | Realizzazione estrattore JDBC, identificazione esclusioni provate, ambito tesseramento, classifiche e soglie, integrazione piena RecordFamily/pipeline/exporter/GUI. Non segnare come implementati in v15. |

---

# RecordsNext 4.0 - Registro decisioni v14

# RecordsNext 4.0 - Registro decisioni v13

# RecordsNext 4.0 - Continuita documentale REGISTRO_DECISIONI_RECORDSNEXT4_v12

Versione additiva del 2026-11-01. La cronologia integrale precedente e preservata sotto.

# RecordsNext 4.0 - Registro decisioni v10

Data: 2026-11-01. Registro additivo: D-001--D-053 restano documentate nelle versioni v2--v8; i relativi stati non cambiano tacitamente.

| ID | Stato | Decisione / evidenza |
|---|---|---|
| D-054 | CONFERMATA: verifica Windows | ZIP 3B1 `RecordsNext4_Fase3B1_OUTPUT_v1_20261101_001744_607896.zip`: stato OK, sette test superati, 552.025 righe `Formazione`, 22.265 tabellini, tre SHA-256 dati verificati, SHA sandbox invariato. Bibbia v8 generata; il runner dichiara nessun commit/deploy. |
| D-055 | CONFERMATA: osservazione FCM | La catena per individuare il regolamento e `Incontro.IDGIRONE -> Girone.ID -> Girone.IDCOMPETIZIONE -> Competizione.ID` **della stessa stagione**. `Competizione.ID` non costituisce da solo identita storica. |
| D-056 | CONFERMATA: fonte | `Competizione` registra anche `NUMEROSOSTITUZIONI`, `NUMERORISUFFICIO`, voti d'ufficio, limiti panchina, ruoli sostituibili, regole speciali; `ModuloAmmesso` collega i moduli alla competizione. Nessun valore e convertito in punteggio recuperato. |
| D-057 | CONFERMATA: osservazione locale 3B2 | Sono presenti 180 righe `Competizione` su 21 stagioni. Nel 2019/20 tutte le 12 riportano `NUMEROSOSTITUZIONI=5` e `NUMERORISUFFICIO=3`; nel 2007/08 le tre riportano 3, 8, 11 sostituzioni e 0, 99, 99 RU. Valori non normalizzati arbitrariamente. |
| D-058 | CONFERMATA: separazione | Nel campione SQLite ci sono 168 tabellini senza incontro, 23 collegati a incontri con una squadra zero: non dichiararli automaticamente anomalie o riposi. I controlli si svolgono separatamente. |
| D-059 | APERTA | Interpretazione delle configurazioni elevate (8, 11, RU 99), possibili convenzioni per assenza di limite, rapporto con tabellini e modifiche al regolamento durante la stagione: da verificare sul modello FCM e su esempi reali. |
| D-060 | APERTA | Definizione rigorosa di prestazione conteggiata, prestazione disponibile in panchina e punti effettivamente recuperabili, per competizione, stagione e regole del ruolo. Niente calcoli prima dell'approvazione. |
| D-061 | IMPLEMENTATA: prototipo di sola lettura | Audit 3B2 con dati raw completi delle configurazioni, aggregati per competizione, moduli ammessi e verifiche integrita; 8 test locali superati. Certificazione Windows ancora da eseguire. |

Nota: non sono state prese nuove decisioni sui pesi dei record o sulle formule per i punti non sfruttati.


## Integrazione successiva a 3B2: metrica individuale (2026-11-01)

| ID | Stato | Decisione / motivazione |
|---|---|---|
| D-062 | CONFERMATA: verifica Windows 3B2 | ZIP `RecordsNext4_Fase3B2_OUTPUT_v1_20261101_002713_328984.zip`, stato OK; 8 test superati, 21 stagioni, 180 configurazioni, 13.072 incontri e 22.265 tabellini. SHA-256 del sandbox prima e dopo identico: `57923f4e75986c2b81533e4beef15b9133635258d7de1d36827a1bdbe576671b`. Bibbia v9 generata; nessun commit/deploy dichiarato. |
| D-063 | APPROVATA DALL'UTENTE: obiettivo | Il voto sprecato e un indicatore riferito a **uno specifico calciatore** lasciato fuori da una fantasquadra, non il vantaggio teorico che la fantasquadra avrebbe potuto ottenere sostituendo altri giocatori. Nessuna simulazione delle sostituzioni necessaria per questa metrica. |
| D-064 | APPROVATA DALL'UTENTE: formula | `mediaVotoSprecato = somma(voti validi del calciatore nelle occasioni in cui e rimasto fuori) / numero totale di partite in cui e stato lasciato fuori`. Le esclusioni senza prestazione utile contano nel denominatore e zero nel numeratore. Esempio: 23 esclusioni, 3 voti pari a 6: `18/23 = 0,782608...`. Non usare come media primaria `18/3 = 6`. |
| D-065 | CONFERMATA: RU e fonti 3.2 | La Riserva d'Ufficio e un voto regolamentare assegnato per un posto scoperto quando le sostituzioni ordinarie non bastano. Non e un calciatore ne un voto del calciatore lasciato fuori; non entra nella somma dei suoi voti sprecati. Preservare il sistema RU e gli strumenti personali esistenti. Fonti: `docs/ANALISI_PERSONALE_RU.md`, `docs/MODELLO_DATI_RECORDSNEXT2.md` e `tools/Report_RU_Storico_Completo_v8_4.ps1` (glossario). |
| D-066 | APPROVATA DALL'UTENTE: sostituzioni | Non esiste una regola generale di tre sostituzioni: si osservano anche 5 e, per recuperi a distanza di mesi e configurazioni eccezionali, anche 11. Mantenere il valore FCM della competizione senza normalizzarlo. Per la media individuale non simulare sostituzioni alternative. |
| D-067 | DA VERIFICARE PRIMA DEL CALCOLO | Stabilire con esempi reali il significato operativo di "lasciato fuori" (non nei primi 11 del tabellino finale, riserva non utilizzata, non schierato), il tipo di voto da sommare (voto Gazzetta base vs fantavoto totale), il trattamento di SV e voti speciali e il perimetro delle sole partite valide. Non inventare questi criteri. |
| D-068 | DA VERIFICARE PRIMA DEI RANKING | Evitare conteggi erronei di una stessa prestazione reale se ci sono piu competizioni fantasy nella stessa giornata. Conservare il dettaglio delle partite fantasy ed esplicitare la chiave di aggregazione del record personale. |
| D-069 | APERTA | L'utente ha menzionato un "valore massimo" senza definirne numericamente l'eventuale soglia o se si riferisca al massimo voto sprecato. Non fissare cap o limiti arbitrari. |
| D-070 | PROCEDURA | Comandi pronti per copia-incolla PowerShell 5.1, senza blocchi `else` separabili; file sempre versionati; ogni fase aggiorna piano, registro, diario, codice verificato e bibbia con SHA-256. |

### Nota di compatibilita

Le D-060 e D-061 riguardano anche un **possibile** studio dei punti recuperabili della fantasquadra. Per il nuovo record individuale quell'analisi non costituisce un prerequisito: questa decisione ne restringe il perimetro, senza cancellare la cronologia.


## Revisione metriche individuali (2026-11-01) - decisioni D-071--D-079

| ID | Stato | Decisione / motivazione |
|---|---|---|
| D-071 | APPROVATA DALL'UTENTE | Il voto di un calciatore non utilizzato e il **valore integrale del voto** (es. 7 resta 7), non il voto meno 6 e non il vantaggio potenziale della fantasquadra. |
| D-072 | APPROVATA DALL'UTENTE | Quattro indicatori distinti: media voto sprecato, errore pesato sul voto, fantamedia sprecata, errore pesato sul fantavoto. La formula v1 `S/E` rimane valida come **errore pesato**, ma non come unica misura principale. |
| D-073 | APPROVATA DALL'UTENTE | Con `E` esclusioni, `P` prestazioni valide, `SV` somma voti e `SF` somma fantavoti: `mediaVoto = SV/P`, `pesatoVoto = SV/E`, `mediaFantavoto = SF/P`, `pesatoFantavoto = SF/E`. Dove il denominatore e zero il risultato e non definito. |
| D-074 | APPROVATA DALL'UTENTE | Una gara in cui il calciatore non ha giocato/non ha voto non produce errore: somma zero al numeratore; se e una esclusione effettiva valida, rimane nel denominatore `E` del peso stagionale. |
| D-075 | APPROVATA DALL'UTENTE | Fantavoto comprensivo di eventi e relativi bonus/malus: gol normali, gol su rigore, assist, ammonizioni e altre categorie disponibili. Conservare separatamente voto, fantavoto ed eventi originali. Valori regolamentari da sorgenti reali, mai coefficienti unificati arbitrariamente. |
| D-076 | DERIVAZIONE MATEMATICA | `quotaConVoto=P/E`, `errorePesatoVoto=mediaVoto*quotaConVoto` e `errorePesatoFantavoto=fantamediaSprecata*quotaConVoto`, quando denominatori disponibili. Se `E>0, P=0`: indici pesati pari a zero, medie non definite. |
| D-077 | PROPOSTA, DA APPROVARE | Aggiungere massimo voto/fantavoto in singola esclusione, numero di esclusioni e quota con voto come indicatori di dettaglio, senza applicare alcun cap artificiale. |
| D-078 | APERTA - FCM/FCA | Definire con casi reali l'esclusione (panchinaro non usato, fuori lista, non schierato ma tesserato), la provenienza del fantavoto per giocatore lasciato fuori e la gestione di `SV`, voti d'ufficio e competizioni parallele. Non calcolare record reali prima della verifica. |
| D-079 | PROCEDURA / IMPLEMENTAZIONE | Aggiunto solo un test sintetico e la specifica v2. Nessuna modifica a database, Java, GUI, regolamenti, competizioni o siti. Bibbia incrementale v11 e SHA-256 dei nuovi file; vecchie versioni conservate. |

**Cronologia invariata:** D-064 `S/E` non e annullata: assume il nome corretto di **errore pesato stagionale** e si affianca alla nuova media `S/P` richiesta dall'utente.


## Decisioni D-080--D-090: archivio completo per giocatore e contesti paralleli

| ID | Stato | Decisione / evidenza |
|---|---|---|
| D-080 | APPROVATA DALL'UTENTE | Il prodotto deve conservare un set completo di dati per OGNI calciatore, con scheda individuale e record derivati dallo stesso archivio. Non estrarre soltanto le statistiche utili a record gia previsti. |
| D-081 | APPROVATA DALL'UTENTE | Un calciatore puo essere tesserato nello stesso anno da 2-3 fantasquadre diverse (serie distinte). Separare rigorosamente le osservazioni fantasy per squadra. |
| D-082 | APPROVATA DALL'UTENTE | Lo stesso calciatore nella stessa giornata puo essere usato in campionato ed escluso in coppa, e viceversa. Ogni incontro fantasy e un contesto indipendente; la prestazione reale e unica. |
| D-083 | APPROVATA DALL'UTENTE | Esattamente due famiglie per il filtro richiesto: CAMPIONATO e COPPE; conservare sempre anche la singola competizione e la sua identita stagionale. |
| D-084 | APPROVATA DALL'UTENTE | Dati individuali completi anche per titolari, giocatori conteggiati e subentrati, non soltanto per gli esclusi: voto, fantavoto, dettaglio bonus/malus, presenze, medie e contesti. |
| D-085 | CONFERMATA: FCM 2025/26 | `Tesserato` mostra il calciatore codice 100843 in tre squadre: 8 (River Pino), 49 (F.C. Pitbull), 77 (F.C. Boliverz). In giornata Serie A 4 River Pino lo ha in Serie B e Coppa Serie B (due incontri diversi). |
| D-086 | INFERENZA DA VERIFICARE | `Tesserato` e uno snapshot senza campi data di validita; non considerare automaticamente escluso il calciatore mancante da `Formazione` in ogni incontro di una squadra. |
| D-087 | PROPOSTA OPERATIVA | Mappa esplicita delle denominazioni FCM alle due famiglie, per stagione e competizione, invece di classificazione runtime con parole chiave. Per ora tre nomi senza assegnazione (Play Off - Play Out, Spareggio Serie B, Youth League), da approvare. |
| D-088 | REGOLA DI INTEGRITA | Chiave prestazione reale diversa dalla chiave evento fantasy; non moltiplicare gol/assist reali nelle aggregazioni all-time quando la stessa prestazione alimenta piu eventi fantasy. |
| D-089 | IMPLEMENTATA: AUDIT PROTOTIPO | Nuovo audit Fase 3C0 di sola lettura sui contesti. Verifica 552.025 righe Formazione, 21 stagioni, mappa delle competizioni, sovrapposizioni e caso reale codice 100843. Non crea ancora il dataset completo canonico. |
| D-090 | APERTA | Regole affidabili per esclusioni senza Formazione, coesistenza di piu partite della stessa famiglia nella giornata, disponibilita storica tesseramento, fantavoto del non schierato e trattamento Play Off/Spareggio/Youth League. |

## Decisioni D-091--D-098: classificazione completa e link ai tabellini

| ID | Stato | Decisione e verifica |
|---|---|---|
| D-091 | APPROVATA DALL'UTENTE | `Play Off - Play Out` appartiene alla famiglia CAMPIONATO. |
| D-092 | APPROVATA DALL'UTENTE | `Spareggio Serie B` appartiene alla famiglia CAMPIONATO. |
| D-093 | APPROVATA DALL'UTENTE | `Youth League` non deve essere considerata MAI nei record/statistiche/schede individuali. Nei soli audit fonti conservare l'evidenza della esclusione. |
| D-094 | APPROVATA DALL'UTENTE | I record individuali di singola partita devono contenere stagione, competizione, fantasquadra, ID incontro e link del tabellino di quella partita sul sito della stagione corrispondente. |
| D-095 | VERIFICATA NEL CODICE 3.2 | Esistono `urlTabellino`, `urlTabellinoLocale`, `urlTabellinoOnline`, `scorecardUrl`; la risoluzione della URL e gia implementata nel normalized exporter 3.2. Non fare concatenazioni inventate con la root del sito corrente. |
| D-096 | APERTA - LINK REALI | Prima della pubblicazione, verificare per ciascun sito/anno la rotta reale e la eventuale possibilita di puntare direttamente al singolo incontro. I link delle pagine di giornata non garantiscono un'ancora per il match. |
| D-097 | REGOLA DATI | L'ID incontro FCM e distinto dalla giornata Serie A e dalla competizione. Conservare tutti i campi per il record e per la scheda giocatore, anche in competizioni parallele. |
| D-098 | IMPLEMENTAZIONE AUDIT | L'audit v2 classifica Play Off/Spareggio, esclude Youth League dalle osservazioni utilizzabili e conserva contatori di esclusione. I riferimenti URL negli esempi rimangono null finche non risolti tramite 3.2. Nessun DB/Java/sito modificato. |


## Decisioni D-099--D-112: integrazione nativa Records Top & Flop

| ID | Stato | Decisione / evidenza |
|---|---|---|
| D-099 | APPROVATA DALL'UTENTE | La nuova famiglia records si chiama **Records Top & Flop**. E parte di RecordsNext 4.0, non un progetto o publisher distinto. |
| D-100 | APPROVATA DALL'UTENTE | Riutilizzare le logiche 3.2.0 FUNZIONANTI per elaborazione, archivio, configurazione, GUI, esportazione, manifest, link e pubblicazione multisito; la 3.2 produttiva rimane intatta. |
| D-101 | APPROVATA DALL'UTENTE | Tutte le statistiche individuali complete costituiscono la base riutilizzabile per schede e Records Top & Flop: calcolo dei record come aggregazione derivata, non raccolta dati autonoma. |
| D-102 | VERIFICATA SORGENTE 3.2 | Il catalogo record e in `RecordFamily`, `CoreRecordCatalog`; configurazione in `ProcessingConfigWriter` e `ProcessingConfigLoader`; `PipelinePreflight` e `DependencyInventory` gestiscono i prerequisiti. |
| D-103 | VERIFICATA SORGENTE 3.2 | La pipeline centrale e `RecordsNextPipeline` e il publisher JS e `Records2026SitePublisher`, con controllo esplicito del numero e dei prefissi dei JS prodotti. |
| D-104 | VERIFICATA SORGENTE 3.2 | `SeasonFamilyShardPublisher` divide per stagione, crea facade e shard JS flat nella cartella js con massimo predefinito 1536 KiB. Estendere la lista FAMILIES e i relativi test. |
| D-105 | VERIFICATA SORGENTE 3.2 | `SeasonNormalizedExporter` produce i riferimenti canonici al tabellino; `MatchesJsExporter` li espone tramite `scorecardUrl`. Nessun URL storico sintetizzato a mano. |
| D-106 | REGOLA IMPLEMENTATIVA | Il filtro sportivo CAMPIONATO/COPPE e separato dalla famiglia catalogo `TOP_FLOP`. Play Off - Play Out e Spareggio Serie B=CAMPIONATO; Youth League sempre esclusa dai dati pubblici. |
| D-107 | REGOLA IMPLEMENTATIVA | Stessa prestazione reale FCA non conteggiata piu volte nei record reali, anche se utilizzata in molte partite/competizioni fantasy; ogni match fantasy resta distinto nei record fantasy. |
| D-108 | REGOLA IMPLEMENTATIVA | Prevedere FULL e CONSOLIDATED, multisito, rollback e validazioni JS 3.2: nessuna scorciatoia per i nuovi dati e nessun deploy autonomo. |
| D-109 | REGOLA IMPLEMENTATIVA | Non attivare la nuova famiglia nel catalogo selezionabile o in pubblicazione finche archivio e exporter non sono funzionanti e testati; impedire successo apparente con output vuoti. |
| D-110 | VERIFICATA FASE 3C1 | ZIP Windows 3C1: `stato=OK`, 13 test, 552025 originali, 546775 ammessi, 5250 Youth esclusi, DB sandbox invariato, bibbia v13 generata. |
| D-111 | APERTA | Elenco definitivo e soglie dei singoli records Top & Flop, definizione formale delle esclusioni senza Formazione e validita storica dei tesseramenti. Non inventare classifiche o soglie. |
| D-112 | IMPLEMENTATA: DOCUMENTAZIONE | Questa fase aggiunge mappa integrazione e regole alla continuita documentale, senza modificare la pipeline Java, la GUI, gli output o il database. La famiglia non e ancora implementata. |
~~~~

### `docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v15.md`

SHA-256: `8ccdc4c73abd8ac17e58a92918e50bb769937ae54db879bc0961256f6413dcd9`

~~~~markdown
# RecordsNext 4.0 - Diario implementazione v15

## Intervento: primo codice Java Top & Flop

- Riletti `RecordFamily`, `CoreRecordCatalog`, `SeasonNormalizedExporter`, `MatchesJsExporter`, specifica e mappa integrazione v14, sorgenti 3.2 contenuti nell'archivio dell'utente.
- Aggiunte classi nuove `TopFlopFacts`, `TopFlopArchive`, `TopFlopMetrics`, `TopFlopCompetitionPolicy` in `app/topflop`, senza riscrivere le classi precedenti.
- Aggiunti test autonomo `TopFlopArchiveSmokeTest` (52 verifiche) e wrapper `TopFlopArchiveJUnitTest` per la suite JUnit 5 esistente.
- Eseguita compilazione dei 106 sorgenti Java originali + quattro nuove classi col compilatore Java 21.0.12.1: OK. Eseguito smoke test Java: 52 verifiche OK. Maven/JUnit integrale non eseguito nell'ambiente di analisi; la verifica sul PC utente resta da completare.
- Predisposti runner conservativo, nuova bibbia incrementale v15 e ZIP diagnostico in `downloads`.
- La prova della seconda esecuzione ha inizialmente evidenziato compilazione duplicata dei quattro file gia installati; corretto il runner per escludere le classi del kit dall'elenco dei sorgenti preesistenti. Nuova prova integrale eseguita da zero con due passaggi consecutivi (vedi log di verifica).
- Nessuna modifica a database, GUI, publisher, archivio siti, configurazione o versione Maven. Nessun commit o deploy automatico.

---

# RecordsNext 4.0 - Diario implementazione v14

# RecordsNext 4.0 - Diario implementazione v13

# RecordsNext 4.0 - Continuita documentale DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v12

Versione additiva del 2026-11-01. La cronologia integrale precedente e preservata sotto.

# RecordsNext 4.0 - Diario implementazione v10

Data: 2026-11-01.

## Revisione dello ZIP Windows 3B1

- ZIP `RecordsNext4_Fase3B1_OUTPUT_v1_20261101_001744_607896.zip`: 14 voci; `MANIFEST_FASE3B1_v1.json` con `stato=OK`.
- Log `LOG_TEST_CONTRIBUZIONI_v1.txt`: sette test su Windows, tutti superati.
- SHA-256 dei file `combinazioni.csv`, `campioni.csv`, `sintesi.json` controllati confrontando gli hash dichiarati nel manifest con i byte nello ZIP.
- Invariato lo SHA del database sandbox: `57923f4e75986c2b81533e4beef15b9133635258d7de1d36827a1bdbe576671b`.
- 22.265 tabellini, 552.025 righe Formazione, 237.686 giocatori nei primi undici slot, 22.028 panchinari nei primi undici, 27.319 titolari iniziali fuori dai primi undici; nessuna attribuzione di punti persi.
- `CODICE_FUNZIONANTE_RECORDSNEXT4_v8.md` generato; nessun commit/deploy eseguito dal runner.

## Sviluppo Fase 3B2

- Riletto lo schema raw FCM originale della 3.2: `Competizione`, `Girone`, `Incontro`, `Tabellino`, `Modulo`, `ModuloAmmesso` e analisi RU personali.
- Implementato `Audit-RecordsNext4Regole-v1.py` in modalita SQLite `mode=ro` + `query_only=ON`.
- Il censimento e per coppia stagione/competizione, con mantenimento di tutte le colonne originali `Competizione` e dei moduli ammessi. La tabella di dettaglio non contiene dati personali delle fantasquadre.
- Osservati 21 archivi, 180 configurazioni, 13.072 incontri (compresi quelli programmati/riposi), 22.265 tabellini; 168 senza ID incontro valido e 23 collegati a incontri con una squadra 0.
- Test automatici `Test-RecordsNext4Regole-v1.py`: otto test sintetici e su DB reale, superati in ambiente di prova. Il database sintetico temporaneo viene generato sotto la cartella diagnostica `downloads`, mai in `%TEMP%`.
- Progettati runner versionato v1, specifica e verifica v1, piano v10, decisioni v9 e bibbia v9 con SHA-256 dei nuovi file.
- La verifica esecutiva del nuovo runner su PowerShell 5.1/Windows e ancora da effettuare; non presentarla come completata.

## Esclusioni

Nessuna modifica ai dati, alle impostazioni delle competizioni, ai 4 script RU personali, al Java, al sito o alla GUI. Nessun calcolo di voti recuperabili, nessun commit.


## Verifica dell'output Fase 3B2 sul PC e decisione operativa

- Esaminato ZIP Windows `RecordsNext4_Fase3B2_OUTPUT_v1_20261101_002713_328984.zip`.
- `MANIFEST_FASE3B2_v1.json`: `stato=OK`, branch `feature/recordsnext4-players`, base `f9bf30323d9204d70e91e242bddfc72c2060ecf9`; stessa impronta SHA-256 del sandbox prima/dopo.
- `LOG_TEST_REGOLE_v1.txt`: otto test superati. `LOG_AUDIT_REGOLE_v1.txt`: 21 stagioni, 180 competizioni, 13.072 incontri, 22.265 tabellini; 168 riferimenti senza incontro rimangono non classificati.
- Riletto `docs/ANALISI_PERSONALE_RU.md` e il glossario di `tools/Report_RU_Storico_Completo_v8_4.ps1`: RU = voto assegnato d'ufficio a copertura di una mancanza. Non associare una RU a un giocatore storico reale.
- L'utente ha approvato la metrica **individuale**, non il controfattuale di squadra: 23 occasioni fuori, 3 gare con voto 6 -> 18/23. Nessun calcolo effettivo sui giocatori e nessun cap implementati in questa revisione.
- Creato test unitario della **sola formula** su dati sintetici. Prima della successiva implementazione su SQLite occorre verificare su esempi reali quali righe costituiscano esclusioni e quale voto sia valido.
- Questa revisione installa soltanto documentazione, un test di formula, il generatore v10 e un runner conservativo. Non modifica Java, GUI, pipeline, archivi 3.2 o siti.


## Revisione della metrica su richiesta utente (2026-11-01)

- Esaminato lo ZIP di esecuzione Windows `RecordsNext4_DecisioniGiocatori_OUTPUT_v1_20261101_003202_168980.zip`: contiene bibbia v10, registro v10, test formula v1 e manifest `stato=OK`.
- Distinti il rendimento medio nelle sole esclusioni con voto (`S/P`) e la misura stagionale su tutte le esclusioni (`S/E`), per voto reale e fantavoto completo di bonus/malus.
- Creati `SPECIFICA_METRICA_VOTO_SPRECATO_GIOCATORE_v2.md` e `Test-RecordsNext4MetricheIndividuali-v2.py` per il contratto puramente aritmetico; test su dati sintetici, nessuna lettura FCM/FCA in questo intervento.
- Creati piano v12, registro decisioni v11, diario v11 e generatore bibbia v11. Conservate le versioni precedenti.
- Restano da verificare sui dati reali la definizione operativa di esclusione, voto valido, fantavoto disponibile per esclusi, bonus/malus storici e deduplicazione delle partite fantasy parallele.
- Nessuna implementazione di record nei sorgenti Java e nessun deploy; database invariato perche non viene aperto.


## Fase 3C0 - 2026-11-01

- Verificato lo ZIP Windows `RecordsNext4_Metriche_OUTPUT_v1_20261101_004055_490269.zip`: manifest OK, 8 test metriche superati, bibbia v11 prodotta; nessun deploy/commit dichiarato.
- Esaminato il database sandbox 3.2 in sola lettura: 21 stagioni, 552.025 righe Formazione, 180 righe Competizione con 25 nomi distinti; la stessa giornata puo contenere piu incontri fantasy per giocatore-squadra.
- Caso campione 2025/26, codice 100843: tre tesseramenti snapshot, presenza nello stesso giorno in campionato e coppa per River Pino.
- Il prototipo di audit Fase 3C0 verifica l'integrita delle join ed elenca le competizioni non assegnate, senza interpretare le esclusioni o sommare i voti.
- Stato: audit testato localmente, codice integrale nella bibbia incrementale v12; installazione e test Windows ancora da confermare.

## Fase 3C1 - revisione classificazione e riferimenti tabellino

- Ricevuto e verificato output 3C0 Windows: `ESITO OK`, 10/10 test, 552025 righe Formazione; SHA256 del sandbox invariato, bibbia v12. Le 159678 sovrapposizioni sono contesti fantasy distinti, non duplicazioni reali.
- L'utente ha classificato `Play Off - Play Out` e `Spareggio Serie B` come campionati e richiesto l'esclusione completa di `Youth League` da record/schede.
- Fonte Java 3.2 esaminata: `SeasonNormalizedExporter.java` espone `urlTabellino`, `urlTabellinoLocale`, `urlTabellinoOnline` e conserva `source_event_id`; `MatchesJsExporter.java` espone `matchId`, `competitionId`, `seasonId`, `scorecardUrl`.
- L'audit v2 mantiene ID incontro e giornata; aggiunge `idGiornataFcm` e `scorecardLinkStatus` negli esempi. URL effettivo assente nell'audit raw: resta null.
- Nessuna simulazione RU/sostituzioni, nessun calcolo degli esclusi mancanti da Formazione, nessuna modifica alla pipeline o al DB.


## Revisione 2026-11-01 - nuova famiglia Records Top & Flop

- L'utente ha chiarito che l'intero modulo giocatori e **una famiglia record del RecordsNext esistente**, con il nome pubblico **Records Top & Flop**.
- Rianalizzati gli effettivi sorgenti 3.2.0 forniti (`RecordsNext2.0.zip`), in particolare `RecordFamily`, `CoreRecordCatalog`, `ProcessingConfigWriter`, `ProcessingConfigLoader`, `RecordsNextPipeline`, `Records2026SitePublisher`, `SeasonFamilyShardPublisher`, `MatchesJsExporter`, `SeasonNormalizedExporter`, `RecordsNext2Dashboard`.
- `Records2026SitePublisher.validateGenerated` valida anche **il conteggio totale** dei file JS e i loro prefissi: richiede modifica coordinata prima di aggiungere un nuovo exporter. `SeasonFamilyShardPublisher.FAMILIES` e la lista esplicita delle famiglie sharded: richiede aggiunta con test flat.
- La 3.2 usa facade/shard per stagione nei js dei siti, con scope multisito e protezione rollback. Va **estesa**, non replicata con tool dedicati.
- Verificato output Windows Fase 3C1: 13/13 test, 21 stagioni, 552025 Formazione, 5250 Youth League escluse, 546775 ammesse, DB immutato.
- Stato codice: **nessun Java/SQLite/GUI/sito modificato in questa revisione**. Restano prototipi audit; prossimo sviluppo sul codice Java nel worktree 4.0. Documenti e script presenti nella bibbia incrementale v14.
~~~~

### `tools/Create-RecordsNext4WorkingCodeMd-v15.py`

SHA-256: `23579812c4d459e2748f4a8949cf26fc68fc540896e0199e9ab82173fd78b5a9`

~~~~python
#!/usr/bin/env python3
"""Append-only v15 bible; includes exact Java/Python/Markdown code and SHA-256."""
from __future__ import annotations
import argparse
import hashlib
from pathlib import Path
import subprocess
import sys

PREVIOUS = 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v14.md'
DEST = 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v15.md'
NEW_FILES = [
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopFacts.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopArchive.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopMetrics.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopCompetitionPolicy.java',
    'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopArchiveSmokeTest.java',
    'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopArchiveJUnitTest.java',
    'docs/SPECIFICA_MODELLO_JAVA_TOP_FLOP_v1.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v16.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v15.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v15.md',
    'tools/Create-RecordsNext4WorkingCodeMd-v15.py',
    'tools/Invoke-RecordsNext4TopFlopJava-v1.py',
]


def sha(path: Path) -> str:
    h = hashlib.sha256()
    with path.open('rb') as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b''):
            h.update(block)
    return h.hexdigest()


def git(root: Path, *args: str) -> str:
    p = subprocess.run(['git', '-C', str(root), *args], capture_output=True,
                       text=True, encoding='utf-8', errors='replace')
    if p.returncode:
        raise RuntimeError('Git: ' + p.stderr.strip())
    return p.stdout.strip()


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument('--project-dir', required=True, type=Path)
    root = ap.parse_args().project_dir.resolve(strict=True)
    for name in [PREVIOUS, *NEW_FILES]:
        if not (root / name).is_file():
            raise RuntimeError('Prerequisito bibbia non trovato: ' + name)
    lines = [
        '# RecordsNext 4.0 - Bibbia incrementale v15', '',
        'Primo modello canonico Java della famiglia Records Top & Flop.',
        '**Non integrato ancora nel motore di elaborazione/pubblicazione.**', '',
        '## Provenienza', '',
        '- Branch: `' + git(root, 'branch', '--show-current') + '`',
        '- HEAD: `' + git(root, 'rev-parse', 'HEAD') + '`',
        '- Riferimento preservato: `'+ PREVIOUS +'`',
        '- SHA-256 bibbia v14: `' + sha(root / PREVIOUS) + '`',
        '- Codice Java 3.2 esistente: invariato in questa installazione.', '',
        '## File integrali e impronte SHA-256', '',
    ]
    for name in NEW_FILES:
        path = root / name
        language = 'java' if name.endswith('.java') else ('python' if name.endswith('.py') else 'markdown')
        lines += ['### `' + name + '`', '', 'SHA-256: `' + sha(path) + '`', '',
                  '~~~~' + language, path.read_text(encoding='utf-8-sig').rstrip('\n'),
                  '~~~~', '']
    content = ('\n'.join(lines).rstrip() + '\n').encode('utf-8')
    target = root / DEST
    if target.exists():
        if not target.is_file() or target.read_bytes() != content:
            raise RuntimeError('Bibbia v15 esistente ma diversa, non sovrascrivo')
        print('BIBBIA v15 GIA PRESENTE IDENTICA:', target, flush=True)
    else:
        with target.open('xb') as stream:
            stream.write(content)
        print('BIBBIA v15 GENERATA:', target, flush=True)
    return 0


if __name__ == '__main__':
    try:
        sys.exit(main())
    except Exception as exc:
        print('ERRORE BIBBIA v15:', exc, file=sys.stderr)
        sys.exit(1)
~~~~

### `tools/Invoke-RecordsNext4TopFlopJava-v1.py`

SHA-256: `b855dcb22431abe9127998bec0999d06015e34d6d4364d34f662deeaf7bce741`

~~~~python
#!/usr/bin/env python3
"""Conservative Windows-friendly installer: verify, compile and test before copying."""
from __future__ import annotations
import argparse
from datetime import datetime
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
import traceback
from zipfile import ZipFile, ZIP_DEFLATED

from pathlib import Path as _P

NEW_FILES = [
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopFacts.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopArchive.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopMetrics.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopCompetitionPolicy.java',
    'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopArchiveSmokeTest.java',
    'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopArchiveJUnitTest.java',
    'docs/SPECIFICA_MODELLO_JAVA_TOP_FLOP_v1.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v16.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v15.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v15.md',
    'tools/Create-RecordsNext4WorkingCodeMd-v15.py',
    'tools/Invoke-RecordsNext4TopFlopJava-v1.py',
]
REQUIRED = [
    'pom.xml',
    'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v14.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v15.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v14.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v14.md',
    'src/main/java/it/alterlega/recordsnext/app/model/RecordFamily.java',
    'src/main/java/it/alterlega/recordsnext/app/model/CoreRecordCatalog.java',
]
PRESERVE = [
    'pom.xml',
    'src/main/java/it/alterlega/recordsnext/app/model/RecordFamily.java',
    'src/main/java/it/alterlega/recordsnext/app/model/CoreRecordCatalog.java',
    'src/main/java/it/alterlega/recordsnext/app/RecordsNextPipeline.java',
    'src/main/java/it/alterlega/recordsnext/Records2026SitePublisher.java',
    'src/main/java/it/alterlega/recordsnext/app/output/SeasonFamilyShardPublisher.java',
]
SMOKE = 'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopArchiveSmokeTest.java'
MAIN = 'it.alterlega.recordsnext.app.topflop.TopFlopArchiveSmokeTest'


def sha(path: Path) -> str:
    h = hashlib.sha256()
    with path.open('rb') as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b''):
            h.update(chunk)
    return h.hexdigest()


def git(root: Path, *args: str) -> str:
    p = subprocess.run(['git', '-C', str(root), *args], capture_output=True,
                       text=True, encoding='utf-8', errors='replace')
    if p.returncode:
        raise RuntimeError('Git error: ' + p.stderr.strip())
    return p.stdout.strip()


def call(command: list[str], cwd: Path | None = None) -> tuple[int, str]:
    process = subprocess.run(command, cwd=str(cwd) if cwd else None,
                             capture_output=True, text=True,
                             encoding='utf-8', errors='replace',
                             env=dict(os.environ, PYTHONDONTWRITEBYTECODE='1'))
    return process.returncode, process.stdout + process.stderr


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument('--work-dir', type=Path, default=Path('D:/DEV_APPS/RecordsNext4.0'))
    ap.add_argument('--output-dir', type=Path, default=Path('D:/DEV_APPS/downloads'))
    opts = ap.parse_args()
    kit = Path(__file__).resolve().parent.parent
    outputs = opts.output_dir.resolve(strict=True)
    stamp = datetime.now().strftime('%Y%m%d_%H%M%S_%f')
    archive = outputs / ('RecordsNext4_TopFlopJava_OUTPUT_v1_' + stamp + '.zip')
    build = outputs / ('RecordsNext4_TopFlopJava_BUILD_v1_' + stamp)
    report = {'fase': 'top-flop-java-model-v1', 'stato': 'ERRORE',
              'javaNuovo': True, 'javaEsistenteModificato': False,
              'databaseModificato': False, 'pubblicazioneEseguita': False,
              'mavenTestCompleto': False, 'bibbia': 'v15', 'filesInstallati': []}
    logs: list[tuple[str, int, str]] = []
    work = None
    succeeded = False
    try:
        work = opts.work_dir.resolve(strict=True)
        if kit == work:
            raise RuntimeError('Il kit non puo coincidere con il worktree')
        for name in REQUIRED:
            if not (work / name).is_file():
                raise RuntimeError('Prerequisito 4.0 mancante: ' + name)
        root = Path(git(work, 'rev-parse', '--show-toplevel')).resolve(strict=True)
        if os.path.normcase(str(root)).casefold() != os.path.normcase(str(work)).casefold():
            raise RuntimeError('Worktree non corrispondente a Git root: ' + str(root))
        branch = git(work, 'branch', '--show-current')
        if branch != 'feature/recordsnext4-players':
            raise RuntimeError('Branch inatteso: ' + branch)
        report['branch'] = branch
        report['head'] = git(work, 'rev-parse', 'HEAD')
        preserved = {name: sha(work / name) for name in PRESERVE}
        report['preservedSha256Before'] = preserved
        for name in NEW_FILES:
            source, destination = kit / name, work / name
            if not source.is_file():
                raise RuntimeError('File mancante nel kit: ' + name)
            if destination.exists() and (not destination.is_file() or sha(destination) != sha(source)):
                raise RuntimeError('Destinazione diversa; fermo senza sovrascrivere: ' + name)
        javac = shutil.which('javac')
        java = shutil.which('java')
        if not javac or not java:
            raise RuntimeError('JDK 21 o successivo richiesto: java/javac non trovati. Nessun file installato.')
        status, version = call([javac, '-version'])
        logs.append(('JDK', status, version))
        match = re.search(r'\bjavac\s+(\d+)(?:\.|\s|$)', version)
        if status or match is None or int(match.group(1)) < 21:
            raise RuntimeError('Richiesto JDK 21 o successivo, trovato: ' + version.strip())
        build.mkdir(parents=False, exist_ok=False)
        kit_sources = sorted((kit / 'src/main/java').rglob('*.java'))
        kit_source_names = {path.relative_to(kit).as_posix() for path in kit_sources}
        base_sources = sorted(path for path in (work / 'src/main/java').rglob('*.java')
                              if path.relative_to(work).as_posix() not in kit_source_names)
        if len(base_sources) < 50 or len(kit_sources) != 4:
            raise RuntimeError('Sorgenti Java inattesi: ' + str((len(base_sources), len(kit_sources))))
        command = [javac, '-encoding', 'UTF-8', '--release', '21', '-d', str(build)]
        command += [str(s) for s in [*base_sources, *kit_sources, kit / SMOKE]]
        status, output = call(command)
        logs.append(('COMPILA_JAVA21_COMPLETO', status, output))
        print('Compilazione Java 3.2 + TopFlop:', 'OK' if status == 0 else 'ERRORE', flush=True)
        if status:
            raise RuntimeError('Compilazione Java fallita: ' + output[-3000:])
        status, output = call([java, '-ea', '-cp', str(build), MAIN])
        logs.append(('TEST_TOPFLOP_52_ASSERT', status, output))
        print('Test TopFlop:', output.strip() if status == 0 else 'ERRORE', flush=True)
        if status or 'TOP_FLOP_JAVA_TEST_OK checks=52' not in output:
            raise RuntimeError('Test Java non superato: ' + output[-2000:])
        for name in NEW_FILES:
            source, destination = kit / name, work / name
            if destination.is_file():
                print('GIA PRESENTE IDENTICO:', name, flush=True)
            else:
                destination.parent.mkdir(parents=True, exist_ok=True)
                with destination.open('xb') as stream:
                    stream.write(source.read_bytes())
                report['filesInstallati'].append(name)
                print('INSTALLATO:', name, flush=True)
        command = [sys.executable, str(work / 'tools/Create-RecordsNext4WorkingCodeMd-v15.py'),
                   '--project-dir', str(work)]
        status, output = call(command)
        logs.append(('BIBBIA_V15', status, output))
        print('Bibbia v15:', 'OK' if status == 0 else 'ERRORE', flush=True)
        if status:
            raise RuntimeError('Bibbia non generata: ' + output[-2000:])
        after = {name: sha(work / name) for name in PRESERVE}
        if after != preserved:
            raise RuntimeError('Un file Java/pom 3.2 protetto e stato alterato: STOP')
        report['preservedSha256After'] = after
        report['bibbiaSha256'] = sha(work / 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v15.md')
        report['javaChecks'] = 52
        report['javaBaseSources'] = len(base_sources)
        report['javaNewSources'] = len(kit_sources)
        report['stato'] = 'OK'
        succeeded = True
    except Exception:
        error = traceback.format_exc()
        logs.append(('ERRORE', 1, error))
        print('Fase Java TopFlop fallita:\n' + error[-3500:], file=sys.stderr, flush=True)
    finally:
        if build.exists():
            shutil.rmtree(build)
        with ZipFile(archive, 'x', compression=ZIP_DEFLATED) as output_zip:
            output_zip.writestr('MANIFEST.json', json.dumps(report, indent=2, ensure_ascii=False) + '\n')
            for label, code, body in logs:
                output_zip.writestr('log/' + label + '.txt', 'exit_code=' + str(code) + '\n' + body)
            for name in NEW_FILES:
                file = work / name if work is not None else None
                if file is not None and file.is_file():
                    output_zip.write(file, name)
            if work is not None:
                bible = work / 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v15.md'
                if bible.is_file():
                    output_zip.write(bible, 'docs/' + bible.name)
        print('ESITO:', 'OK' if succeeded else 'ERRORE', flush=True)
        print('ZIP DA CARICARE:', archive, flush=True)
    return 0 if succeeded else 1


if __name__ == '__main__':
    sys.exit(main())
~~~~
