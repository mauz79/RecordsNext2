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
