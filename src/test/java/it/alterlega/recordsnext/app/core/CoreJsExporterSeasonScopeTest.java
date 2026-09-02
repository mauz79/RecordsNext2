package it.alterlega.recordsnext.app.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoreJsExporterSeasonScopeTest {

    @TempDir
    Path tempDir;

    @Test
    void exportScopedCoreExcludesFutureSeasons() throws Exception {
        Path db = tempDir.resolve("recordsnext.db");
        Path out = tempDir.resolve("fcmRecordsNext_Core.js");

        Class.forName("org.sqlite.JDBC");

        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + db);
             Statement s = c.createStatement()) {

            s.execute("""
                CREATE TABLE rn_season (
                    season_id TEXT PRIMARY KEY,
                    display_name TEXT,
                    sort_order INTEGER,
                    is_anchor INTEGER NOT NULL DEFAULT 0
                )
                """);

            s.execute("""
                CREATE TABLE rn_season_configuration (
                    season_id TEXT PRIMARY KEY,
                    management_type TEXT,
                    configuration_status TEXT,
                    local_site_path TEXT,
                    online_site_url TEXT,
                    dataa_path TEXT
                )
                """);

            s.execute("""
                CREATE TABLE rn_team_identity (
                    team_identity_id INTEGER PRIMARY KEY,
                    canonical_name TEXT,
                    anchor_season_id TEXT,
                    anchor_team_season_id INTEGER
                )
                """);

            s.execute("""
                CREATE TABLE rn_team_season (
                    team_season_id INTEGER PRIMARY KEY,
                    season_id TEXT
                )
                """);

            s.execute("""
                CREATE TABLE rn_team_mapping (
                    team_identity_id INTEGER,
                    team_season_id INTEGER
                )
                """);

            s.execute("""
                CREATE VIEW rn_configured_team AS
                SELECT
                    ts.team_season_id,
                    ts.season_id,
                    NULL AS source_file_id,
                    NULL AS source_team_id,
                    i.canonical_name AS source_name,
                    i.canonical_name AS normalized_name,
                    NULL AS source_division_id,
                    NULL AS source_team_number,
                    i.team_identity_id,
                    i.canonical_name,
                    'MAPPED' AS mapping_status,
                    'TEST' AS mapping_method,
                    NULL AS notes
                FROM rn_team_season ts
                JOIN rn_team_mapping tm ON tm.team_season_id = ts.team_season_id
                JOIN rn_team_identity i ON i.team_identity_id = tm.team_identity_id
                """);

            s.execute("""
                CREATE TABLE rn_competition_identity (
                    competition_identity_id INTEGER PRIMARY KEY,
                    canonical_name TEXT,
                    anchor_season_id TEXT,
                    anchor_competition_season_id INTEGER
                )
                """);

            s.execute("""
                CREATE TABLE rn_competition_season (
                    competition_season_id INTEGER PRIMARY KEY,
                    season_id TEXT
                )
                """);

            s.execute("""
                CREATE TABLE rn_competition_mapping (
                    competition_identity_id INTEGER,
                    competition_season_id INTEGER
                )
                """);

            s.execute("""
                CREATE VIEW rn_configured_competition AS
                SELECT
                    cs.competition_season_id,
                    cs.season_id,
                    NULL AS source_file_id,
                    NULL AS source_competition_id,
                    i.canonical_name AS source_name,
                    i.canonical_name AS normalized_name,
                    i.competition_identity_id,
                    i.canonical_name,
                    'MAPPED' AS mapping_status,
                    'TEST' AS mapping_method,
                    NULL AS notes
                FROM rn_competition_season cs
                JOIN rn_competition_mapping cm
                  ON cm.competition_season_id = cs.competition_season_id
                JOIN rn_competition_identity i
                  ON i.competition_identity_id = cm.competition_identity_id
                """);

            for (String season : new String[]{"2024_2025", "2025_2026", "2026_2027"}) {
                int order = Integer.parseInt(season.substring(0, 4));
                s.execute("INSERT INTO rn_season VALUES ('" + season + "','" + season + "'," + order + ",0)");
                s.execute("INSERT INTO rn_season_configuration VALUES ('" + season + "','GESTITA','COMPLETA',NULL,NULL,NULL)");
            }

            s.execute("""
                INSERT INTO rn_team_identity
                VALUES (1,'Squadra Storica','2026_2027',3)
                """);
            s.execute("INSERT INTO rn_team_season VALUES (1,'2024_2025')");
            s.execute("INSERT INTO rn_team_season VALUES (2,'2025_2026')");
            s.execute("INSERT INTO rn_team_season VALUES (3,'2026_2027')");
            s.execute("INSERT INTO rn_team_mapping VALUES (1,1)");
            s.execute("INSERT INTO rn_team_mapping VALUES (1,2)");
            s.execute("INSERT INTO rn_team_mapping VALUES (1,3)");

            s.execute("""
                INSERT INTO rn_competition_identity
                VALUES (1,'Serie A','2026_2027',3)
                """);
            s.execute("INSERT INTO rn_competition_season VALUES (1,'2024_2025')");
            s.execute("INSERT INTO rn_competition_season VALUES (2,'2025_2026')");
            s.execute("INSERT INTO rn_competition_season VALUES (3,'2026_2027')");
            s.execute("INSERT INTO rn_competition_mapping VALUES (1,1)");
            s.execute("INSERT INTO rn_competition_mapping VALUES (1,2)");
            s.execute("INSERT INTO rn_competition_mapping VALUES (1,3)");
        }

        CoreJsExporter.export(
                db,
                out,
                "alterlega",
                "AlterLega",
                "2024_2025"
        );

        String js = Files.readString(out);

        assertTrue(js.contains("2024_2025"));
        assertFalse(js.contains("2025_2026"));
        assertFalse(js.contains("2026_2027"));
        assertTrue(js.contains("\"isAnchor\":1"));
    }
}
