package it.alterlega.recordsnext.app.core;

public record LeagueMetadata(
        String leagueId,
        String leagueName,
        String currentSeasonId
) {
    public LeagueMetadata {
        leagueId = require(leagueId, "leagueId");
        leagueName = require(leagueName, "leagueName");
        currentSeasonId = require(currentSeasonId, "currentSeasonId");
    }

    private static String require(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " obbligatorio");
        }
        return value.trim();
    }
}
