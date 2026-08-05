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
            data = read(c, leagueId.trim(), leagueName.trim());
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

    private static CoreData read(Connection c, String leagueId, String leagueName)
            throws Exception {
        List<Map<String, Object>> seasons = readRows(c, """
            SELECT
                s.season_id,
                s.display_name,
                s.sort_order,
                s.is_anchor,
                COALESCE(sc.management_type, 'NON_CONFIGURATA') AS management_type,
                COALESCE(sc.configuration_status, 'DA_CONFIGURARE') AS configuration_status,
                sc.local_site_path,
                sc.online_site_url,
                sc.dataa_path
            FROM rn_season s
            LEFT JOIN rn_season_configuration sc
              ON sc.season_id = s.season_id
            ORDER BY COALESCE(s.sort_order, 999999), s.season_id
            """);

        List<Map<String, Object>> canonicalTeams = readRows(c, """
            SELECT
                team_identity_id AS canonical_team_id,
                canonical_name,
                anchor_season_id,
                anchor_team_season_id
            FROM rn_team_identity
            ORDER BY canonical_name COLLATE NOCASE, team_identity_id
            """);

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
            ORDER BY season_id, canonical_name COLLATE NOCASE, source_name COLLATE NOCASE
            """);

        List<Map<String, Object>> canonicalCompetitions = readRows(c, """
            SELECT
                competition_identity_id AS canonical_competition_id,
                canonical_name,
                anchor_season_id,
                anchor_competition_season_id
            FROM rn_competition_identity
            ORDER BY canonical_name COLLATE NOCASE, competition_identity_id
            """);

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
            ORDER BY season_id, canonical_name COLLATE NOCASE, source_name COLLATE NOCASE
            """);

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
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
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
