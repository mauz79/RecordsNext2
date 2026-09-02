package it.alterlega.recordsnext.app.output;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SeasonPublicationTargetRepositoryTest {
    @TempDir
    Path tempDir;

    @Test
    void returnsOnlyConfiguredSelectedSitesAndMarksMissingDirectories() throws Exception {
        Path db = tempDir.resolve("recordsnext.db");
        Path site2025 = tempDir.resolve("Lega2025");
        Files.createDirectories(site2025);
        Path missing2026 = tempDir.resolve("Lega2026");

        Class.forName("org.sqlite.JDBC");
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + db);
             Statement s = c.createStatement()) {
            s.execute("CREATE TABLE rn_season(season_id TEXT PRIMARY KEY, sort_order INTEGER)");
            s.execute("CREATE TABLE rn_season_configuration(season_id TEXT PRIMARY KEY, management_type TEXT, local_site_path TEXT)");

            s.execute("INSERT INTO rn_season VALUES('2023_2024',19)");
            s.execute("INSERT INTO rn_season VALUES('2024_2025',20)");
            s.execute("INSERT INTO rn_season VALUES('2025_2026',21)");

            s.execute("INSERT INTO rn_season_configuration VALUES('2023_2024','GESTITA',NULL)");
            s.execute("INSERT INTO rn_season_configuration VALUES('2024_2025','GESTITA','" + sql(site2025) + "')");
            s.execute("INSERT INTO rn_season_configuration VALUES('2025_2026','GESTITA','" + sql(missing2026) + "')");
        }

        var repo = new SeasonPublicationTargetRepository(db);
        var targets = repo.load(List.of("2023_2024", "2024_2025", "2025_2026"));

        assertEquals(2, targets.size());
        assertEquals("2024_2025", targets.get(0).seasonId());
        assertEquals(site2025.toAbsolutePath().normalize(), targets.get(0).siteRoot());
        assertTrue(targets.get(0).available());
        assertEquals("2025_2026", targets.get(1).seasonId());
        assertFalse(targets.get(1).available());
    }

    private static String sql(Path path) {
        return path.toAbsolutePath().normalize().toString().replace("'", "''");
    }
}
