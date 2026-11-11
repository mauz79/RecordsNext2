package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.topflop.TopFlopFacts.PlayerSeason;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.RealDay;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.RealDayKey;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.VoteState;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Read-only FCA source adapter for the native Top & Flop model.
 * Does not create tables, generate JS, infer fantasy exclusion, or alter the 3.2 pipeline.
 * The caller supplies the SAME SQLite JDBC connection strategy as RecordsNext 3.2.
 */
public final class TopFlopFcaJdbcReader {
    private TopFlopFcaJdbcReader() {}

    public record PlayerProfile(PlayerSeason player, String name, Map<String, String> originalFields) {
        public PlayerProfile {
            Objects.requireNonNull(player, "player");
            Objects.requireNonNull(name, "name");
            originalFields = Map.copyOf(Objects.requireNonNull(originalFields, "originalFields"));
        }
    }

    public record SeasonLoad(String seasonId, Map<Integer, PlayerProfile> playersByLocalId,
                             int sourceGiocaInRows, int realDaysAdded, int validVotes,
                             int nonValidVotes, int zeroOrUnclassifiedVotes) {
        public SeasonLoad {
            playersByLocalId = Map.copyOf(playersByLocalId);
        }
    }

    public static SeasonLoad loadSeason(Connection connection, TopFlopArchive archive,
                                        String seasonId, Integer gazzaCode) throws SQLException {
        Objects.requireNonNull(connection, "connection");
        Objects.requireNonNull(archive, "archive");
        if (seasonId == null || !seasonId.matches("[0-9]{4}_[0-9]{4}")) {
            throw new IllegalArgumentException("Invalid seasonId: " + seasonId);
        }
        if (gazzaCode != null && gazzaCode <= 0) {
            throw new IllegalArgumentException("gazzaCode filter must be positive");
        }
        String prefix = "raw_" + seasonId + "_fca_";
        String playerTable = prefix + "giocatorea";
        String dayTable = prefix + "giocain";
        String scoreTable = prefix + "punteggio";
        List<String> playerColumns = columns(connection, playerTable);
        List<String> dayColumns = columns(connection, dayTable);
        List<String> scoreColumns = columns(connection, scoreTable);
        requireColumns(playerColumns, playerTable, "ID", "CODICEGAZZA", "NOME");
        requireColumns(dayColumns, dayTable, "IDGIOCATORE", "GIORNATA", "IDPUNTEGGIO");
        requireColumns(scoreColumns, scoreTable, "ID", "VOTO1", "VALORESPECIALE");
        Map<Integer, PlayerProfile> profiles = new LinkedHashMap<>();
        String filter = gazzaCode == null ? "" : " WHERE a.\"CODICEGAZZA\" = ?";
        String playerSql = "SELECT " + projection("a", "a__", playerColumns) +
            " FROM \"" + playerTable + "\" a" + filter + " ORDER BY a.\"ID\"";
        try (PreparedStatement statement = connection.prepareStatement(playerSql)) {
            if (gazzaCode != null) statement.setInt(1, gazzaCode);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    int localId = rows.getInt("a__ID");
                    int rawCode = rows.getInt("a__CODICEGAZZA");
                    Integer code = rows.wasNull() || rawCode <= 0 ? null : rawCode;
                    PlayerSeason key = new PlayerSeason(seasonId, localId, code);
                    PlayerProfile profile = new PlayerProfile(key, rows.getString("a__NOME"),
                            sourceFields(rows, "GiocatoreA", "a__", playerColumns));
                    if (profiles.putIfAbsent(localId, profile) != null)
                        throw new SQLException("Duplicate FCA player ID " + localId + ": " + seasonId);
                }
            }
        }
        String joined = " FROM \"" + dayTable + "\" g " +
                "JOIN \"" + playerTable + "\" a ON a.\"ID\"=g.\"IDGIOCATORE\" " +
                "JOIN \"" + scoreTable + "\" p ON p.\"ID\"=g.\"IDPUNTEGGIO\"";
        String where = gazzaCode == null ? "" : " WHERE a.\"CODICEGAZZA\"=?";
        String sourceSql = "SELECT " + projection("g", "g__", dayColumns) + "," +
                projection("p", "p__", scoreColumns) + "," + projection("a", "a__", playerColumns) +
                joined + where + " ORDER BY g.\"IDGIOCATORE\",g.\"GIORNATA\"";
        int sourceCount = countSourceRows(connection, dayTable, playerTable, gazzaCode);
        if (gazzaCode == null) {
            int rawCount = countRawRows(connection, dayTable);
            if (sourceCount != rawCount) throw new SQLException(
                "Orphan FCA GiocaIn players in " + seasonId + ": raw=" + rawCount + " joined=" + sourceCount);
        }
        int count = 0, valid = 0, nonvalid = 0, zeros = 0;
        try (PreparedStatement statement = connection.prepareStatement(sourceSql)) {
            if (gazzaCode != null) statement.setInt(1, gazzaCode);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    int localId = rows.getInt("g__IDGIOCATORE");
                    PlayerProfile profile = profiles.get(localId);
                    if (profile == null) throw new SQLException("Missing FCA player " + localId);
                    int round = rows.getInt("g__GIORNATA");
                    BigDecimal originalVote = rows.getBigDecimal("p__VOTO1");
                    BigDecimal special = rows.getBigDecimal("p__VALORESPECIALE");
                    VoteState state;
                    BigDecimal vote = null;
                    if (originalVote != null && originalVote.compareTo(BigDecimal.ZERO) > 0) {
                        state = VoteState.VALID;
                        vote = originalVote;
                        valid++;
                    } else if (special != null && special.compareTo(BigDecimal.ZERO) != 0) {
                        state = VoteState.SPECIAL;
                        nonvalid++;
                    } else if (originalVote != null && originalVote.compareTo(BigDecimal.ZERO) < 0) {
                        state = VoteState.NO_VOTE;
                        nonvalid++;
                    } else {
                        state = VoteState.UNKNOWN; // 0 or SQL NULL: never infer played/not played.
                        nonvalid++;
                        zeros++;
                    }
                    Map<String, BigDecimal> events = new LinkedHashMap<>();
                    for (String column : scoreColumns) {
                        String upper = column.toUpperCase(Locale.ROOT);
                        if (upper.equals("ID") || upper.startsWith("VOTO") ||
                            upper.equals("VALORESPECIALE") || upper.startsWith("FINDER")) continue;
                        BigDecimal number = rows.getBigDecimal("p__" + column);
                        if (number != null) events.put(column, number); // Never apply bonus coefficients here.
                    }
                    Map<String, String> original = new LinkedHashMap<>(profile.originalFields());
                    original.putAll(sourceFields(rows, "GiocaIn", "g__", dayColumns));
                    original.putAll(sourceFields(rows, "Punteggio", "p__", scoreColumns));
                    RealDay fact = new RealDay(new RealDayKey(profile.player(), round),
                            state, vote, events, original);
                    archive.addRealDay(fact); // Fail closed on contradictory duplicate player/day.
                    count++;
                }
            }
        }
        if (count != sourceCount) throw new SQLException("FCA join lost records in " + seasonId +
                ": source=" + sourceCount + " joined=" + count);
        return new SeasonLoad(seasonId, profiles, sourceCount, count, valid, nonvalid, zeros);
    }

    private static int countRawRows(Connection c, String dayTable) throws SQLException {
        try (Statement s = c.createStatement(); ResultSet r = s.executeQuery(
                "SELECT count(*) FROM \"" + dayTable + "\"")) { return r.getInt(1); }
    }

    private static int countSourceRows(Connection c, String dayTable, String playerTable,
                                      Integer code) throws SQLException {
        String sql = "SELECT count(*) FROM \"" + dayTable + "\" g JOIN \"" + playerTable +
                "\" a ON a.\"ID\"=g.\"IDGIOCATORE\"" +
                (code == null ? "" : " WHERE a.\"CODICEGAZZA\"=?");
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            if (code != null) ps.setInt(1, code);
            try (ResultSet r = ps.executeQuery()) { return r.getInt(1); }
        }
    }

    private static List<String> columns(Connection c, String table) throws SQLException {
        List<String> columns = new ArrayList<>();
        try (Statement statement = c.createStatement();
             ResultSet result = statement.executeQuery("PRAGMA table_info(\"" + table + "\")")) {
            while (result.next()) {
                String column = result.getString("name");
                if (column == null || !column.matches("[A-Za-z][A-Za-z0-9_]*"))
                    throw new SQLException("Unsupported FCA source column: " + column);
                columns.add(column);
            }
        }
        if (columns.isEmpty()) throw new SQLException("Missing FCA table or columns: " + table);
        return List.copyOf(columns);
    }

    private static void requireColumns(List<String> actual, String table, String... required)
            throws SQLException {
        for (String column : required) if (!actual.contains(column))
            throw new SQLException("Missing column " + column + " in " + table);
    }

    private static String projection(String tableAlias, String prefix, List<String> columns) {
        List<String> parts = new ArrayList<>();
        for (String column : columns) parts.add(tableAlias + ".\"" + column + "\" AS \"" + prefix + column + "\"");
        return String.join(",", parts);
    }

    private static Map<String, String> sourceFields(ResultSet rows, String source,
                                      String alias, List<String> columns) throws SQLException {
        Map<String, String> result = new LinkedHashMap<>();
        for (String column : columns) {
            String value = rows.getString(alias + column);
            String key = source + "." + column;
            if (value == null) result.put("__SQL_NULL__." + key, "true");
            else result.put(key, value);
        }
        return result;
    }
}
