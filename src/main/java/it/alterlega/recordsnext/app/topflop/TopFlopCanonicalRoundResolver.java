package it.alterlega.recordsnext.app.topflop;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Resolve round/description from the EXISTING RecordsNext 3.2 canonical views.
 * A source FCM IDGIORNATA is never treated as a competition-round ordinal.
 * Unmapped competitions remain absent here; their raw FCM facts are still kept.
 */
public final class TopFlopCanonicalRoundResolver {
    private TopFlopCanonicalRoundResolver() {}

    public record Round(int competitionRound, String roundDescription,
                        int serieARound, int sourceRoundId, int sourceCompetitionId) {
        public Round {
            if (competitionRound <= 0 || serieARound <= 0 || sourceRoundId <= 0 || sourceCompetitionId <= 0)
                throw new IllegalArgumentException("Invalid canonical round values");
        }
    }

    public static Map<Long, Round> loadSeason(Connection connection, String seasonId)
            throws SQLException {
        Objects.requireNonNull(connection, "connection");
        if (seasonId == null || !seasonId.matches("[0-9]{4}_[0-9]{4}"))
            throw new IllegalArgumentException("Invalid seasonId: " + seasonId);
        Map<Long, Round> rounds = new LinkedHashMap<>();
        String sql = "SELECT source_event_id, competition_round, round_description, " +
            "serie_a_round, source_round_id, source_competition_id " +
            "FROM rn_event WHERE season_id = ?";
        try (PreparedStatement query = connection.prepareStatement(sql)) {
            query.setString(1, seasonId);
            try (ResultSet rows = query.executeQuery()) {
                while (rows.next()) {
                    long id = rows.getLong("source_event_id");
                    Round value = new Round(rows.getInt("competition_round"),
                        rows.getString("round_description"),
                        rows.getInt("serie_a_round"),
                        rows.getInt("source_round_id"),
                        rows.getInt("source_competition_id"));
                    if (rounds.putIfAbsent(id, value) != null)
                        throw new SQLException("Duplicate canonical event: " + seasonId + "/" + id);
                }
            }
        }
        return Map.copyOf(rounds);
    }
}
