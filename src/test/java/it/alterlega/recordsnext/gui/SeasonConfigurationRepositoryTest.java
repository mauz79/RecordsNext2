package it.alterlega.recordsnext.gui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SeasonConfigurationRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void removeConfigurationDeletesImportedSeasonCompletely() throws Exception {
        Class.forName("org.sqlite.JDBC");

        Path db = tempDir.resolve("recordsnext.db");

        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + db)) {
            try (Statement s = c.createStatement()) {
                s.execute("PRAGMA foreign_keys=ON");

                s.execute("""
                    CREATE TABLE rn_season(
                      season_id TEXT PRIMARY KEY,
                      display_name TEXT,
                      sort_order INTEGER,
                      is_anchor INTEGER,
                      created_at TEXT,
                      updated_at TEXT
                    )
                    """);

                s.execute("""
                    CREATE TABLE rn_source_file(
                      source_file_id INTEGER PRIMARY KEY AUTOINCREMENT,
                      season_id TEXT NOT NULL,
                      source_type TEXT NOT NULL,
                      source_path TEXT NOT NULL,
                      FOREIGN KEY(season_id) REFERENCES rn_season(season_id)
                    )
                    """);
            }

            try (PreparedStatement p = c.prepareStatement("""
                INSERT INTO rn_season(
                  season_id, display_name, sort_order,
                  is_anchor, created_at, updated_at
                )
                VALUES('2025_2026','2025_2026',21,1,'now','now')
                """)) {
                p.executeUpdate();
            }

            try (PreparedStatement p = c.prepareStatement("""
                INSERT INTO rn_source_file(
                  season_id, source_type, source_path
                )
                VALUES('2025_2026','FCM','E:\\FCM\\data\\Archivio2026.fcm')
                """)) {
                p.executeUpdate();
            }
        }

        SeasonConfigurationRepository repository =
            new SeasonConfigurationRepository(db);

        repository.save(repository.load());

        assertEquals(1, count(db,
            "SELECT COUNT(*) FROM rn_season WHERE season_id='2025_2026'"));

        assertEquals(1, count(db,
            "SELECT COUNT(*) FROM rn_source_file WHERE season_id='2025_2026'"));

        assertEquals(1, count(db,
            "SELECT COUNT(*) FROM rn_season_configuration WHERE season_id='2025_2026'"));

        repository.removeConfiguration("2025_2026");

        assertEquals(0, count(db,
            "SELECT COUNT(*) FROM rn_season WHERE season_id='2025_2026'"));

        assertEquals(0, count(db,
            "SELECT COUNT(*) FROM rn_source_file WHERE season_id='2025_2026'"));

        assertEquals(0, count(db,
            "SELECT COUNT(*) FROM rn_season_configuration WHERE season_id='2025_2026'"));

        assertEquals(0, repository.load().size());
    }

    private static int count(Path db, String sql) throws Exception {
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + db);
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery(sql)) {
            r.next();
            return r.getInt(1);
        }
    }
}
