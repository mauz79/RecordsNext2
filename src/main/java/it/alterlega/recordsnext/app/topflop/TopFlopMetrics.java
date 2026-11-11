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
