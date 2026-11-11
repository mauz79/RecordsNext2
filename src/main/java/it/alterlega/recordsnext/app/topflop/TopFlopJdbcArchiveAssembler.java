package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.topflop.TopFlopFacts.FantasyFact;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.FantasyKey;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.RealDayKey;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.RosterSnapshot;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.Usage;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Read-only, season-scoped bridge from existing FCA/FCM readers to TopFlopArchive.
 * It preserves all Tesserato rows separately, even repeated registrations of the
 * same player in the same fantasy team. It never infers ownership dates, exclusions,
 * played minutes, scorecard URLs or legally counted substitutions.
 */
public final class TopFlopJdbcArchiveAssembler {
    private TopFlopJdbcArchiveAssembler() {}

    public record LoadedSeason(TopFlopArchive archive,
                               Map<Integer, TopFlopFcaJdbcReader.PlayerProfile> profiles,
                               List<TopFlopFcmJdbcReader.RosterLine> rosterEvents,
                               TopFlopFcaJdbcReader.SeasonLoad fca,
                               TopFlopFcmJdbcReader.SeasonLoad fcm,
                               int distinctRosterMemberships,
                               long numericTabellinoTotals,
                               long nonNumericTabellinoTotals) {
        public LoadedSeason {
            Objects.requireNonNull(archive);
            profiles = Map.copyOf(profiles);
            rosterEvents = List.copyOf(rosterEvents);
        }
    }

    private record RosterMembership(TopFlopFacts.PlayerSeason player, int teamId) {}

    public static LoadedSeason loadSeason(Connection connection, String seasonId)
            throws SQLException {
        Objects.requireNonNull(connection, "connection");
        Objects.requireNonNull(seasonId, "seasonId");
        TopFlopArchive archive = new TopFlopArchive();
        var fca = TopFlopFcaJdbcReader.loadSeason(connection, archive, seasonId, null);
        // Use the canonical 3.2 competition round, not source IDGIORNATA.
        var canonicalRounds = TopFlopCanonicalRoundResolver.loadSeason(connection, seasonId);
        List<TopFlopFcmJdbcReader.RosterLine> rosterEvents = new ArrayList<>();
        Set<RosterMembership> membershipKeys = new LinkedHashSet<>();
        long[] numericTotals = {0};
        long[] nonNumericTotals = {0};
        var fcm = TopFlopFcmJdbcReader.loadSeason(connection, seasonId,
            new TopFlopFcmJdbcReader.Sink() {
                @Override
                public void formation(TopFlopFcmJdbcReader.FormationLine line)
                        throws SQLException {
                    try {
                        RealDayKey realKey = new RealDayKey(line.player(), line.serieARound());
                        if (archive.realDay(realKey) == null) {
                            throw new SQLException("Missing FCA day for FCM formation: " +
                                seasonId + "/" + line.matchId() + "/" + line.player());
                        }
                        var canonicalRound = canonicalRounds.get(line.matchId());
                        if (canonicalRound != null &&
                            (canonicalRound.serieARound() != line.serieARound() ||
                             canonicalRound.sourceRoundId() != line.fcmRoundId() ||
                             canonicalRound.sourceCompetitionId() != line.competitionId())) {
                            throw new SQLException("Canonical 3.2 match metadata mismatch: " +
                                seasonId + "/" + line.matchId());
                        }
                        BigDecimal total = numericTabellino(line.tabellinoTotal());
                        if (total == null) nonNumericTotals[0]++;
                        else numericTotals[0]++;
                        Map<String, String> raw = new LinkedHashMap<>(line.sourceFields());
                        raw.put("FCM.Fantasquadra.Nome", line.fantasyTeamName());
                        raw.put("FCM.Formazione.Pos", Integer.toString(line.initialPosition()));
                        raw.put("FCM.Formazione.Entrato", Integer.toString(line.entered()));
                        raw.put("FCM.Formazione.EntratoRiserva", Integer.toString(line.enteredAsReserve()));
                        raw.put("FCM.Formazione.Manuale", Integer.toString(line.manual()));
                        raw.put("FCM.Tabellino.Slot", Integer.toString(line.tabellinoSlot()));
                        raw.put("FCM.Tabellino.FirstEleven", Boolean.toString(line.firstElevenSlot()));
                        raw.put("FCM.Tabellino.Voto", orNullMarker(line.tabellinoVote()));
                        raw.put("FCM.Tabellino.Modif", orNullMarker(line.tabellinoModifier()));
                        raw.put("FCM.Tabellino.Tot", orNullMarker(line.tabellinoTotal()));
                        raw.put("FCM.Incontro.Giocato", Integer.toString(line.matchPlayed()));
                        raw.put("FCM.Incontro.GiornataDiA", Integer.toString(line.serieARound()));
                        raw.put("FCM.Incontro.IDGiornata", Integer.toString(line.fcmRoundId()));
                        raw.put("FCM.Giornata.Desc", orNullMarker(line.fcmRoundDescription()));
                        raw.put("FCM.Incontro.IDGiornata", Integer.toString(line.fcmRoundId()));
                        raw.put("TopFlop.CompetitionRoundStatus",
                            canonicalRound == null ? "UNMAPPED_SOURCE_ONLY" : "CANONICAL_3_2");
                        // The finalized FCM tabellino is the scoring source:
                        // its first eleven player slots with numeric Tot in a played
                        // match are contributions recorded for that fantasy team.
                        // ENTRATO alone is unreliable (historical exceptions).
                        // Never call a missing formation an excluded player.
                        boolean countedInTabellino = line.firstElevenSlot() &&
                            line.matchPlayed() == 1 && total != null;
                        Usage usage = countedInTabellino ? Usage.COUNTED :
                            (line.firstElevenSlot() ? Usage.UNKNOWN :
                            (line.initialPosition() > 0 ? Usage.BENCH_UNUSED :
                            (line.initialPosition() < 0 ? Usage.OUTSIDE_LIST : Usage.UNKNOWN)));
                        raw.put("TopFlop.UsageEvidence", countedInTabellino ?
                            "FCM_PLAYED_TABELLINO_FIRST11_NUMERIC_TOT" :
                            (line.firstElevenSlot() ? "FCM_FIRST11_UNVERIFIED" : "FCM_OUTSIDE_FIRST11"));
                        // Events retain original FCA field names/quantities. No
                        // arbitrary bonus coefficients are applied here.
                        Map<String, BigDecimal> creditedOriginalEvents = countedInTabellino ?
                            archive.realDay(realKey).events() : Map.of();
                        FantasyFact fact = new FantasyFact(
                            new FantasyKey(line.player(), line.competitionId(),
                                           line.matchId(), line.fantasyTeamId()),
                            realKey, line.competitionName(), line.family(),
                            canonicalRound == null ? null : canonicalRound.competitionRound(),
                            line.initialPosition() == 0,
                            usage, total, creditedOriginalEvents, null, raw);
                        archive.addFantasyFact(fact);
                    } catch (IllegalArgumentException ex) {
                        throw new SQLException("Invalid TopFlop FCM fact: " + seasonId +
                            "/" + line.matchId() + "/" + line.player(), ex);
                    }
                }

                @Override
                public void roster(TopFlopFcmJdbcReader.RosterLine line)
                        throws SQLException {
                    rosterEvents.add(line); // All distinct source tesserato IDs retained.
                    RosterMembership key = new RosterMembership(line.player(), line.fantasyTeamId());
                    if (membershipKeys.add(key)) {
                        Map<String, String> raw = new LinkedHashMap<>(line.sourceFields());
                        raw.put("FCM.Tesserato.IDRappresentativo", Long.toString(line.tesseratoId()));
                        raw.put("FCM.Tesserato.AltriEventi", "Vedere LoadedSeason.rosterEvents");
                        archive.addRosterSnapshot(new RosterSnapshot(line.player(),
                                                   line.fantasyTeamId(), raw));
                    }
                }
            });
        if (archive.realDayCount() != fca.realDaysAdded() ||
            archive.fantasyFactCount() != fcm.allowedFormations() ||
            rosterEvents.size() != fcm.readTesserati() ||
            archive.rosterCount() != membershipKeys.size() ||
            numericTotals[0] + nonNumericTotals[0] != fcm.allowedFormations()) {
            throw new SQLException("TopFlop archive reconciliation failed: " + seasonId);
        }
        return new LoadedSeason(archive, fca.playersByLocalId(), rosterEvents,
            fca, fcm, membershipKeys.size(), numericTotals[0], nonNumericTotals[0]);
    }

    /** Null means SV/NG, missing or an unrecognized original token, never zero. */
    static BigDecimal numericTabellino(String original) {
        if (original == null) return null;
        String value = original.trim();
        if (!value.matches("[+-]?[0-9]+(?:[,.][0-9]+)?")) return null;
        return new BigDecimal(value.replace(',', '.'));
    }

    private static String orNullMarker(String original) {
        return original == null ? "__SQL_NULL__" : original;
    }
}
