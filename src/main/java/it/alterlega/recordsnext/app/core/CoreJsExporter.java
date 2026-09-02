package it.alterlega.recordsnext.app.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CoreJsExporter {
    private CoreJsExporter() {
    }

    public static ExportResult export(
            Path database,
            Path outputFile,
            String leagueId,
            String leagueName) throws Exception {
        return export(database, outputFile, leagueId, leagueName, null);
    }

    public static ExportResult export(
            Path database,
            Path outputFile,
            String leagueId,
            String leagueName,
            String maxSeasonId) throws Exception {

        Path db = database.toAbsolutePath().normalize();
        Path out = outputFile.toAbsolutePath().normalize();
        if (!Files.isRegularFile(db)) {
            throw new IllegalArgumentException("Database SQLite non trovato: " + db);
        }
        if (leagueId == null || leagueId.isBlank()) {
            throw new IllegalArgumentException("leagueId obbligatorio");
        }
        if (leagueName == null || leagueName.isBlank()) {
            throw new IllegalArgumentException("leagueName obbligatorio");
        }

        Class.forName("org.sqlite.JDBC");
        CoreData data;
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + db)) {
            data = read(c, leagueId.trim(), leagueName.trim(), maxSeasonId);
        }

        Path parent = out.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(out, toJavascript(data), StandardCharsets.UTF_8);
        return new ExportResult(
            data.seasons().size(),
            data.canonicalTeams().size(),
            data.seasonTeams().size(),
            data.canonicalCompetitions().size(),
            data.seasonCompetitions().size(),
            out
        );
    }

    private static CoreData read(
            Connection c,
            String leagueId,
            String leagueName,
            String maxSeasonId) throws Exception {

        boolean scoped = maxSeasonId != null && !maxSeasonId.isBlank();
        String seasonFilter = scoped ? " WHERE s.season_id <= ?" : "";
        String configuredTeamFilter = scoped ? " WHERE season_id <= ?" : "";
        String configuredCompetitionFilter = scoped ? " WHERE season_id <= ?" : "";

        List<Map<String, Object>> seasons = readRows(c, """
            SELECT
                s.season_id,
                s.display_name,
                s.sort_order,
                CASE
                    WHEN ? IS NOT NULL AND s.season_id = ? THEN 1
                    WHEN ? IS NOT NULL THEN 0
                    ELSE s.is_anchor
                END AS is_anchor,
                COALESCE(sc.management_type, 'NON_CONFIGURATA') AS management_type,
                COALESCE(sc.configuration_status, 'DA_CONFIGURARE') AS configuration_status,
                sc.local_site_path,
                sc.online_site_url,
                sc.dataa_path
            FROM rn_season s
            LEFT JOIN rn_season_configuration sc
              ON sc.season_id = s.season_id
            """ + seasonFilter + """
            ORDER BY COALESCE(s.sort_order, 999999), s.season_id
            """,
            scoped
                ? List.of(maxSeasonId, maxSeasonId, maxSeasonId, maxSeasonId)
                : java.util.Arrays.asList(null, null, null));

        List<Map<String, Object>> canonicalTeams;
        if (scoped) {
            canonicalTeams = readRows(c, """
                SELECT
                    i.team_identity_id AS canonical_team_id,
                    i.canonical_name,
                    (
                        SELECT ts.season_id
                        FROM rn_team_mapping tm
                        JOIN rn_team_season ts
                          ON ts.team_season_id = tm.team_season_id
                        WHERE tm.team_identity_id = i.team_identity_id
                          AND ts.season_id <= ?
                        ORDER BY ts.season_id DESC, ts.team_season_id DESC
                        LIMIT 1
                    ) AS anchor_season_id,
                    (
                        SELECT ts.team_season_id
                        FROM rn_team_mapping tm
                        JOIN rn_team_season ts
                          ON ts.team_season_id = tm.team_season_id
                        WHERE tm.team_identity_id = i.team_identity_id
                          AND ts.season_id <= ?
                        ORDER BY ts.season_id DESC, ts.team_season_id DESC
                        LIMIT 1
                    ) AS anchor_team_season_id
                FROM rn_team_identity i
                WHERE EXISTS (
                    SELECT 1
                    FROM rn_team_mapping tm
                    JOIN rn_team_season ts
                      ON ts.team_season_id = tm.team_season_id
                    WHERE tm.team_identity_id = i.team_identity_id
                      AND ts.season_id <= ?
                )
                ORDER BY i.canonical_name COLLATE NOCASE, i.team_identity_id
                """, List.of(maxSeasonId, maxSeasonId, maxSeasonId));
        } else {
            canonicalTeams = readRows(c, """
                SELECT
                    team_identity_id AS canonical_team_id,
                    canonical_name,
                    anchor_season_id,
                    anchor_team_season_id
                FROM rn_team_identity
                ORDER BY canonical_name COLLATE NOCASE, team_identity_id
                """);
        }

        List<Map<String, Object>> seasonTeams = readRows(c, """
            SELECT
                team_season_id,
                season_id,
                source_file_id,
                source_team_id,
                source_name,
                normalized_name,
                source_division_id,
                source_team_number,
                team_identity_id AS canonical_team_id,
                canonical_name,
                mapping_status,
                mapping_method,
                notes
            FROM rn_configured_team
            """ + configuredTeamFilter + """
            ORDER BY season_id, canonical_name COLLATE NOCASE, source_name COLLATE NOCASE
            """, scoped ? List.of(maxSeasonId) : List.of());

        List<Map<String, Object>> canonicalCompetitions;
        if (scoped) {
            canonicalCompetitions = readRows(c, """
                SELECT
                    i.competition_identity_id AS canonical_competition_id,
                    i.canonical_name,
                    (
                        SELECT cs.season_id
                        FROM rn_competition_mapping cm
                        JOIN rn_competition_season cs
                          ON cs.competition_season_id = cm.competition_season_id
                        WHERE cm.competition_identity_id = i.competition_identity_id
                          AND cs.season_id <= ?
                        ORDER BY cs.season_id DESC, cs.competition_season_id DESC
                        LIMIT 1
                    ) AS anchor_season_id,
                    (
                        SELECT cs.competition_season_id
                        FROM rn_competition_mapping cm
                        JOIN rn_competition_season cs
                          ON cs.competition_season_id = cm.competition_season_id
                        WHERE cm.competition_identity_id = i.competition_identity_id
                          AND cs.season_id <= ?
                        ORDER BY cs.season_id DESC, cs.competition_season_id DESC
                        LIMIT 1
                    ) AS anchor_competition_season_id
                FROM rn_competition_identity i
                WHERE EXISTS (
                    SELECT 1
                    FROM rn_competition_mapping cm
                    JOIN rn_competition_season cs
                      ON cs.competition_season_id = cm.competition_season_id
                    WHERE cm.competition_identity_id = i.competition_identity_id
                      AND cs.season_id <= ?
                )
                ORDER BY i.canonical_name COLLATE NOCASE, i.competition_identity_id
                """, List.of(maxSeasonId, maxSeasonId, maxSeasonId));
        } else {
            canonicalCompetitions = readRows(c, """
                SELECT
                    competition_identity_id AS canonical_competition_id,
                    canonical_name,
                    anchor_season_id,
                    anchor_competition_season_id
                FROM rn_competition_identity
                ORDER BY canonical_name COLLATE NOCASE, competition_identity_id
                """);
        }

        List<Map<String, Object>> seasonCompetitions = readRows(c, """
            SELECT
                competition_season_id,
                season_id,
                source_file_id,
                source_competition_id,
                source_name,
                normalized_name,
                competition_identity_id AS canonical_competition_id,
                canonical_name,
                mapping_status,
                mapping_method,
                notes
            FROM rn_configured_competition
            """ + configuredCompetitionFilter + """
            ORDER BY season_id, canonical_name COLLATE NOCASE, source_name COLLATE NOCASE
            """, scoped ? List.of(maxSeasonId) : List.of());

        return new CoreData(
            "2.0",
            Instant.now().toString(),
            leagueId,
            leagueName,
            seasons,
            canonicalTeams,
            seasonTeams,
            canonicalCompetitions,
            seasonCompetitions
        );
    }

    private static List<Map<String, Object>> readRows(Connection c, String sql)
            throws Exception {
        return readRows(c, sql, List.of());
    }

    private static List<Map<String, Object>> readRows(
            Connection c,
            String sql,
            List<?> parameters) throws Exception {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (java.sql.PreparedStatement s = c.prepareStatement(sql)) {
            for (int i = 0; i < parameters.size(); i++) {
                s.setObject(i + 1, parameters.get(i));
            }
            try (ResultSet rs = s.executeQuery()) {
                int columns = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= columns; i++) {
                        String name = rs.getMetaData().getColumnLabel(i);
                        Object value = rs.getObject(i);
                        row.put(toCamelCase(name), value);
                    }
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    static String toJavascript(CoreData data) {
        return "window.fcmRecordsNextCore = " + toJson(data) + ";\n";
    }

    private static String toJson(Object value) {
        if (value == null) return "null";
        if (value instanceof String s) return quote(s);
        if (value instanceof Boolean || value instanceof Number) return value.toString();
        if (value instanceof CoreData d) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("schemaVersion", d.schemaVersion());
            map.put("generatedAt", d.generatedAt());
            map.put("league", Map.of("leagueId", d.leagueId(), "leagueName", d.leagueName()));
            map.put("seasons", d.seasons());
            map.put("canonicalTeams", d.canonicalTeams());
            map.put("seasonTeams", d.seasonTeams());
            map.put("canonicalCompetitions", d.canonicalCompetitions());
            map.put("seasonCompetitions", d.seasonCompetitions());
            return toJson(map);
        }
        if (value instanceof Map<?, ?> map) {
            StringBuilder b = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> e : map.entrySet()) {
                if (!first) b.append(',');
                first = false;
                b.append(quote(String.valueOf(e.getKey()))).append(':').append(toJson(e.getValue()));
            }
            return b.append('}').toString();
        }
        if (value instanceof Iterable<?> values) {
            StringBuilder b = new StringBuilder("[");
            boolean first = true;
            for (Object item : values) {
                if (!first) b.append(',');
                first = false;
                b.append(toJson(item));
            }
            return b.append(']').toString();
        }
        throw new IllegalArgumentException("Tipo JSON non supportato: " + value.getClass());
    }

    private static String quote(String value) {
        StringBuilder b = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '\\' -> b.append("\\\\");
                case '"' -> b.append("\\\"");
                case '\n' -> b.append("\\n");
                case '\r' -> b.append("\\r");
                case '\t' -> b.append("\\t");
                default -> {
                    if (ch < 0x20) b.append(String.format("\\u%04x", (int) ch));
                    else b.append(ch);
                }
            }
        }
        return b.append('"').toString();
    }

    private static String toCamelCase(String value) {
        StringBuilder b = new StringBuilder();
        boolean upper = false;
        for (char ch : value.toLowerCase().toCharArray()) {
            if (ch == '_') {
                upper = true;
            } else if (upper) {
                b.append(Character.toUpperCase(ch));
                upper = false;
            } else {
                b.append(ch);
            }
        }
        return b.toString();
    }

    public record ExportResult(
        int seasons,
        int canonicalTeams,
        int seasonTeams,
        int canonicalCompetitions,
        int seasonCompetitions,
        Path outputFile
    ) {}

    record CoreData(
        String schemaVersion,
        String generatedAt,
        String leagueId,
        String leagueName,
        List<Map<String, Object>> seasons,
        List<Map<String, Object>> canonicalTeams,
        List<Map<String, Object>> seasonTeams,
        List<Map<String, Object>> canonicalCompetitions,
        List<Map<String, Object>> seasonCompetitions
    ) {}
}
