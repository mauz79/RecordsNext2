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
                              Integer fantasyRound, boolean initialStarter,
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
            if (fantasyRound != null && fantasyRound <= 0) throw new IllegalArgumentException("fantasyRound must be positive when mapped");
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
