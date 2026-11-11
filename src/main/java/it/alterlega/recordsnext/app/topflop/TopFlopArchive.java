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

    /** Read-only iteration for season exports; avoids per-player full map scans. */
    public List<RosterSnapshot> allRosters() { return List.copyOf(rosters.values()); }

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

    /** Every FCA real day exactly once, including no-vote days. */
    public List<RealDay> allRealDays() { return List.copyOf(realDays.values()); }
    public int fantasyFactCount() { return fantasyFacts.size(); }

    /** Every distinct FCM context exactly once. */
    public List<FantasyFact> allFantasyFacts() { return List.copyOf(fantasyFacts.values()); }

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
