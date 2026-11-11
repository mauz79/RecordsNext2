package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.topflop.TopFlopFacts.CompetitionFamily;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.FantasyFact;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.PlayerSeason;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.RealDay;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.Usage;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.VoteState;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Native per-player/fantasy-team contribution aggregates; never recomputes
 * bonus coefficients, never double-counts one real matchday as a real career
 * appearance, and never fabricates excluded roster days.
 *
 * A counted fantasy contribution is an entry in the first eleven player slots
 * of the FCM scorecard for a played match, with a numeric scorecard total.
 * This is source-based scoring evidence, not a counterfactual team result.
 */
public final class TopFlopContributionSummary {
    private TopFlopContributionSummary() {}
    private static final MathContext PRECISION = MathContext.DECIMAL64;

    public record Summary(int fantasyContexts, int countedMatches, int countedWithValidRealVote,
                          int countedWithNumericTabellinoVote,
                          BigDecimal realVoteSum, BigDecimal tabellinoVoteSum,
                          BigDecimal fantasyTotalSum, BigDecimal meanRealVote,
                          BigDecimal meanTabellinoVote, BigDecimal meanFantasyTotal,
                          Map<String, BigDecimal> sourceEventsApportioned,
                          Map<String, BigDecimal> normalizedEventsApportioned) {
        public Summary {
            sourceEventsApportioned = Map.copyOf(sourceEventsApportioned);
            normalizedEventsApportioned = Map.copyOf(normalizedEventsApportioned);
        }
    }

    /**
     * The scope is always one season-specific player identity. For all-time
     * comparisons, resolve and verify historic identities BEFORE combining
     * season summaries (CodiceGazza can have historical collisions).
     *
     * A null teamId/family/competitionName means no filter on that dimension.
     */
    public static Summary summarize(TopFlopArchive archive, PlayerSeason player,
                                    Integer teamId, CompetitionFamily family,
                                    String competitionName) {
        Objects.requireNonNull(archive, "archive");
        Objects.requireNonNull(player, "player");
        if (teamId != null && teamId <= 0)
            throw new IllegalArgumentException("Invalid fantasyTeamId: " + teamId);
        int contexts = 0, counted = 0, validReal = 0, numericTabVote = 0;
        BigDecimal realSum = BigDecimal.ZERO;
        BigDecimal tabVoteSum = BigDecimal.ZERO;
        BigDecimal fantasySum = BigDecimal.ZERO;
        Map<String,BigDecimal> events = new LinkedHashMap<>();
        for (FantasyFact fact : archive.fantasyFactsFor(player)) {
            if (teamId != null && fact.key().fantasyTeamId() != teamId) continue;
            if (family != null && fact.family() != family) continue;
            if (competitionName != null && !competitionName.equals(fact.competitionName())) continue;
            contexts++;
            if (fact.usage() != Usage.COUNTED) continue;
            counted++;
            if (fact.fantasyVote() == null)
                throw new IllegalStateException("COUNTED context without numeric FCM total: " + fact.key());
            fantasySum = fantasySum.add(fact.fantasyVote());
            RealDay real = archive.realDay(fact.realDayKey());
            if (real.voteState() == VoteState.VALID) {
                validReal++;
                realSum = realSum.add(real.vote());
            }
            BigDecimal sourceTabVote = numericToken(fact.sourceFields().get("FCM.Tabellino.Voto"));
            if (sourceTabVote != null) {
                numericTabVote++;
                tabVoteSum = tabVoteSum.add(sourceTabVote);
            }
            for (var event : fact.fantasyEvents().entrySet()) {
                if (event.getValue().signum() != 0)
                    events.merge(event.getKey(), event.getValue(), BigDecimal::add);
            }
        }
        Map<String,BigDecimal> normalized = new LinkedHashMap<>();
        // FCA contains three parallel vote-source columns. Use ONLY source 1
        // (Gazzetta) for the public event categories, preserving all originals.
        // Goals from penalty are already part of GOLFATTI1: never sum both.
        for (var entry : EVENT_FIELDS.entrySet()) {
            BigDecimal amount = events.get(entry.getKey());
            if (amount != null && amount.signum() != 0)
                normalized.put(entry.getValue(),amount);
        }
        return new Summary(contexts,counted,validReal,numericTabVote,realSum,
            tabVoteSum,fantasySum,mean(realSum,validReal),mean(tabVoteSum,numericTabVote),
            mean(fantasySum,counted),events,normalized);
    }

    private static final Map<String,String> EVENT_FIELDS = Map.ofEntries(
        Map.entry("GOLFATTI1","gol"),
        Map.entry("GOLFATTISURIGORE1","golSuRigore"),
        Map.entry("ASSIST","assist"),
        Map.entry("AMM","ammonizioni"),
        Map.entry("ESP","espulsioni"),
        Map.entry("RIGPAR","rigoriParati"),
        Map.entry("RIGSBA","rigoriSbagliati"),
        Map.entry("AUTOGOL1","autogol"),
        Map.entry("GOLSUBITI","golSubiti"),
        Map.entry("GOLSUBITISURIGORE","golSubitiSuRigore"),
        Map.entry("GOLVITTORIA","golVittoria"),
        Map.entry("GOLPAREGGIO","golPareggio")
    );

    private static BigDecimal numericToken(String token) {
        if (token == null) return null;
        String cleaned = token.trim();
        if (!cleaned.matches("[+-]?[0-9]+(?:[,.][0-9]+)?")) return null;
        return new BigDecimal(cleaned.replace(',','.'));
    }

    private static BigDecimal mean(BigDecimal value,int denominator) {
        return denominator==0 ? null : value.divide(BigDecimal.valueOf(denominator),PRECISION);
    }
}
