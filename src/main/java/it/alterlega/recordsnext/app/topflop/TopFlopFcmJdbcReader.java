package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.topflop.TopFlopFacts.CompetitionFamily;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.PlayerSeason;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Read-only FCM adapter. Joins match, team, competition and FCA season identity.
 * Tabellino slots are observed values, NOT authoritative proof of legal usage.
 * It never fabricates scorecard URLs, inferred absences or bonus coefficients.
 */
public final class TopFlopFcmJdbcReader {
    private TopFlopFcmJdbcReader() {}

    public record FormationLine(PlayerSeason player, int fantasyTeamId, String fantasyTeamName,
                                long matchId, int competitionId, String competitionName,
                                CompetitionFamily family, int serieARound, int fcmRoundId,
                                String fcmRoundDescription, int initialPosition,
                                int entered, int enteredAsReserve, int manual,
                                int tabellinoSlot, String tabellinoVote,
                                String tabellinoModifier, String tabellinoTotal,
                                boolean firstElevenSlot, int matchPlayed,
                                Map<String, String> sourceFields) {
        public FormationLine {
            Objects.requireNonNull(player);
            Objects.requireNonNull(fantasyTeamName);
            Objects.requireNonNull(competitionName);
            Objects.requireNonNull(family);
            sourceFields = Map.copyOf(sourceFields);
        }
    }

    /** One source tesseramento record, not guaranteed current for any matchday. */
    public record RosterLine(PlayerSeason player, int fantasyTeamId, long tesseratoId,
                             Map<String, String> sourceFields) {
        public RosterLine {
            Objects.requireNonNull(player);
            sourceFields = Map.copyOf(sourceFields);
        }
    }

    public interface Sink {
        void formation(FormationLine line) throws SQLException;
        void roster(RosterLine line) throws SQLException;
    }

    public record SeasonLoad(String seasonId, long sourceFormations, long allowedFormations,
                             long youthExcluded, long matchedSlots, long sourceTesserati,
                             long readTesserati) {}

    public static SeasonLoad loadSeason(Connection c, String seasonId, Sink sink) throws SQLException {
        Objects.requireNonNull(c, "connection");
        Objects.requireNonNull(sink, "sink");
        if (seasonId == null || !seasonId.matches("[0-9]{4}_[0-9]{4}"))
            throw new IllegalArgumentException("Invalid seasonId: " + seasonId);
        String fcm = "raw_" + seasonId + "_fcm_";
        String fca = "raw_" + seasonId + "_fca_";
        long sourceFormationCount = count(c, fcm + "formazione");
        long sourceRosterCount = count(c, fcm + "tesserato");
        long allowed = 0, youth = 0, matched = 0, totalRead = 0;
        // Join only non-secret fantasy-team name; never read passwords, contacts or email.
        String sql = "SELECT f.IDGIOC AS pid, a.CODICEGAZZA AS code, f.IDSQUADRA AS teamId," +
            " s.NOME AS teamName, f.IDINCONTRO AS matchId, c.ID AS competitionId," +
            " c.NOME AS competitionName, i.GIORNATADIA AS realRound," +
            " i.IDGIORNATA AS fcmRoundId, gr.DESC AS fcmRoundDescription," +
            " f.POS AS position, f.ENTRATO AS entered, f.ENTRATORISERVA AS enteredReserve," +
            " f.MANUALE AS manual, i.GIOCATO AS matchPlayed," +
            " t.LISTA AS slots, t.VOTO AS votes, t.MODIF AS modifiers, t.TOT AS totals," +
            " f.RIG AS formationRole, f.IDLEGA AS formationLeague, t.IDLEGA AS tabLeague," +
            " i.IDTIPO AS matchType, i.IDCASA AS homeTeam, i.IDFUORI AS awayTeam," +
            " i.IDGIRONE AS groupId, g.IDCOMPETIZIONE AS groupCompetitionId" +
            " FROM \"" + fcm + "formazione\" f" +
            " JOIN \"" + fcm + "incontro\" i ON i.ID=f.IDINCONTRO" +
            " JOIN \"" + fcm + "girone\" g ON g.ID=i.IDGIRONE" +
            " JOIN \"" + fcm + "competizione\" c ON c.ID=g.IDCOMPETIZIONE" +
            " JOIN \"" + fcm + "tabellino\" t" +
            " ON t.IDINCONTRO=f.IDINCONTRO AND t.IDSQUADRA=f.IDSQUADRA" +
            " JOIN \"" + fcm + "fantasquadra\" s ON s.ID=f.IDSQUADRA" +
            " JOIN \"" + fca + "giocatorea\" a ON a.ID=f.IDGIOC" +
            " JOIN \"" + fca + "giocain\" d" +
            " ON d.IDGIOCATORE=f.IDGIOC AND d.GIORNATA=i.GIORNATADIA" +
            " LEFT JOIN \"" + fcm + "giornata\" gr ON gr.ID=i.IDGIORNATA" +
            " ORDER BY f.IDINCONTRO, f.IDSQUADRA, f.IDGIOC";
        try (PreparedStatement statement = c.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) {
                totalRead++;
                String competitionName = rows.getString("competitionName");
                if (competitionName == null) throw new SQLException("Missing FCM competition");
                if (competitionName.trim().equalsIgnoreCase("Youth League")) {
                    youth++;
                    continue;
                }
                CompetitionFamily family = TopFlopCompetitionPolicy.requireFamily(competitionName);
                int fcaId = rows.getInt("pid");
                int code = rows.getInt("code");
                Integer codeOrNull = rows.wasNull() || code <= 0 ? null : code;
                int teamId = rows.getInt("teamId");
                long matchId = rows.getLong("matchId");
                String[] slots = split(rows.getString("slots"));
                String[] votes = split(rows.getString("votes"));
                String[] modifiers = split(rows.getString("modifiers"));
                String[] totals = split(rows.getString("totals"));
                if (slots.length != votes.length || slots.length != modifiers.length ||
                        slots.length != totals.length)
                    throw new SQLException("Tabellino array length mismatch: " + seasonId + "/" + matchId + "/" + teamId);
                int slot = 0;
                for (int index = 0; index < slots.length; index++) {
                    if (slots[index].equals(Integer.toString(fcaId))) {
                        if (slot != 0) throw new SQLException("Duplicate player in tabellino: " + seasonId + "/" + matchId + "/" + fcaId);
                        slot = index + 1;
                    }
                }
                if (slot == 0) throw new SQLException("Formazione player missing from tabellino: " + seasonId + "/" + matchId + "/" + fcaId);
                Map<String, String> origin = new LinkedHashMap<>();
                // Preserve source, not interpretation: the existing 3.2 normalizer owns links.
                for (String name : List.of("formationRole", "formationLeague", "tabLeague", "matchType",
                        "homeTeam", "awayTeam", "groupId", "groupCompetitionId")) {
                    String value = rows.getString(name);
                    if (value == null) origin.put("__SQL_NULL__." + name, "true");
                    else origin.put(name, value);
                }
                FormationLine line = new FormationLine(
                    new PlayerSeason(seasonId, fcaId, codeOrNull), teamId,
                    rows.getString("teamName"), matchId, rows.getInt("competitionId"),
                    competitionName, family, rows.getInt("realRound"),
                    rows.getInt("fcmRoundId"), rows.getString("fcmRoundDescription"),
                    rows.getInt("position"), rows.getInt("entered"),
                    rows.getInt("enteredReserve"), rows.getInt("manual"),
                    slot, votes[slot - 1], modifiers[slot - 1], totals[slot - 1],
                    slot <= 11, rows.getInt("matchPlayed"), origin);
                sink.formation(line);
                allowed++;
                matched++;
            }
        }
        if (totalRead != sourceFormationCount)
            throw new SQLException("FCM join lost/duplicated formation records: " + seasonId +
                " expected=" + sourceFormationCount + " got=" + totalRead);
        if (allowed + youth != sourceFormationCount || matched != allowed)
            throw new SQLException("FCM formation reconciliation failed: " + seasonId);
        long readRoster = 0;
        String rosterSql = "SELECT t.ID AS rosterId, t.IDGIOCATORE AS pid, " +
            " t.IDSQUADRA AS teamId, a.CODICEGAZZA AS code," +
            " t.RUOLO AS role, t.ANNODICONTRATTO AS contractYear, t.VENDUTO AS sold," +
            " t.INIZIOINFORTUNIO AS injuryStart, t.COSTOACQ AS boughtFor," +
            " t.COSTOCESS AS soldFor, t.IDSOSTITUITO AS replacedId" +
            " FROM \"" + fcm + "tesserato\" t" +
            " JOIN \"" + fca + "giocatorea\" a ON a.ID=t.IDGIOCATORE" +
            " ORDER BY t.ID";
        try (PreparedStatement statement = c.prepareStatement(rosterSql);
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) {
                int code = rows.getInt("code");
                Integer codeOrNull = rows.wasNull() || code <= 0 ? null : code;
                Map<String, String> fields = new LinkedHashMap<>();
                for (String field : List.of("role", "contractYear", "sold", "injuryStart", "boughtFor", "soldFor", "replacedId")) {
                    String value = rows.getString(field);
                    if (value == null) fields.put("__SQL_NULL__." + field, "true");
                    else fields.put(field, value);
                }
                sink.roster(new RosterLine(new PlayerSeason(seasonId, rows.getInt("pid"), codeOrNull),
                    rows.getInt("teamId"), rows.getLong("rosterId"), fields));
                readRoster++;
            }
        }
        if (readRoster != sourceRosterCount)
            throw new SQLException("FCM tesserato join lost rows: " + seasonId + ": " + readRoster + "/" + sourceRosterCount);
        return new SeasonLoad(seasonId, sourceFormationCount, allowed, youth, matched,
                              sourceRosterCount, readRoster);
    }

    private static String[] split(String value) {
        return value == null ? new String[0] : value.split("%", -1);
    }

    private static long count(Connection c, String table) throws SQLException {
        try (Statement statement = c.createStatement();
             ResultSet rows = statement.executeQuery("SELECT count(*) FROM \"" + table + "\"")) {
            return rows.getLong(1);
        }
    }
}
