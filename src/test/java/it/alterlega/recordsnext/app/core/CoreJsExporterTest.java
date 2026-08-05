package it.alterlega.recordsnext.app.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CoreJsExporterTest {
    @TempDir Path temp;

    @Test
    void exportsCanonicalCoreData() throws Exception {
        Path db = temp.resolve("recordsnext.db");
        Class.forName("org.sqlite.JDBC");
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + db);
             Statement s = c.createStatement()) {
            s.execute("CREATE TABLE rn_season(season_id TEXT,display_name TEXT,sort_order INTEGER,is_anchor INTEGER)");
            s.execute("CREATE TABLE rn_season_configuration(season_id TEXT,management_type TEXT,configuration_status TEXT,local_site_path TEXT,online_site_url TEXT,dataa_path TEXT)");
            s.execute("CREATE TABLE rn_team_identity(team_identity_id INTEGER,canonical_name TEXT,anchor_season_id TEXT,anchor_team_season_id INTEGER)");
            s.execute("CREATE TABLE rn_competition_identity(competition_identity_id INTEGER,canonical_name TEXT,anchor_season_id TEXT,anchor_competition_season_id INTEGER)");
            s.execute("CREATE TABLE rn_configured_team(team_season_id INTEGER,season_id TEXT,source_file_id INTEGER,source_team_id INTEGER,source_name TEXT,normalized_name TEXT,source_division_id INTEGER,source_team_number INTEGER,team_identity_id INTEGER,canonical_name TEXT,mapping_status TEXT,mapping_method TEXT,notes TEXT)");
            s.execute("CREATE TABLE rn_configured_competition(competition_season_id INTEGER,season_id TEXT,source_file_id INTEGER,source_competition_id INTEGER,source_name TEXT,normalized_name TEXT,competition_identity_id INTEGER,canonical_name TEXT,mapping_status TEXT,mapping_method TEXT,notes TEXT)");
            s.execute("INSERT INTO rn_season VALUES('2025_2026','2025/2026',1,1)");
            s.execute("INSERT INTO rn_season_configuration VALUES('2025_2026','GESTITA','COMPLETA','E:/fantacalcio/Lega2025','https://example.test/lega2025','js/DataA.js')");
            s.execute("INSERT INTO rn_team_identity VALUES(10,'River Pino','2025_2026',100)");
            s.execute("INSERT INTO rn_competition_identity VALUES(20,'Serie A','2025_2026',200)");
            s.execute("INSERT INTO rn_configured_team VALUES(100,'2025_2026',1,7,'River Pino F.C.','river pino fc',1,7,10,'River Pino','ASSOCIATA','ANCHOR',NULL)");
            s.execute("INSERT INTO rn_configured_competition VALUES(200,'2025_2026',1,1,'Serie A','serie a',20,'Serie A','ASSOCIATA','ANCHOR',NULL)");
        }

        Path out = temp.resolve("fcmRecordsNext_Core.js");
        var result = CoreJsExporter.export(db, out, "alterlega", "AlterLega");
        String js = Files.readString(out);

        assertEquals(1, result.seasons());
        assertEquals(1, result.canonicalTeams());
        assertEquals(1, result.seasonTeams());
        assertEquals(1, result.canonicalCompetitions());
        assertEquals(1, result.seasonCompetitions());
        assertTrue(js.startsWith("window.fcmRecordsNextCore = "));
        assertTrue(js.contains("\"canonicalName\":\"River Pino\""));
        assertTrue(js.contains("\"onlineSiteUrl\":\"https://example.test/lega2025\""));
    }
}
