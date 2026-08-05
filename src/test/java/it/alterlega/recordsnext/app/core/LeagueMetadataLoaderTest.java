package it.alterlega.recordsnext.app.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LeagueMetadataLoaderTest {
    @TempDir
    Path temp;

    @Test
    void readsLeagueMetadataFromNestedConfiguration() throws Exception {
        Path file = temp.resolve("league.json");
        Files.writeString(file, """
                {
                  "schemaVersion": "2.0",
                  "league": {
                    "leagueId": "alterlega",
                    "leagueName": "AlterLega",
                    "currentSeasonId": "2025_2026"
                  }
                }
                """);

        LeagueMetadata metadata = LeagueMetadataLoader.load(file);

        assertEquals("alterlega", metadata.leagueId());
        assertEquals("AlterLega", metadata.leagueName());
        assertEquals("2025_2026", metadata.currentSeasonId());
    }

    @Test
    void rejectsMissingRequiredFields() throws Exception {
        Path file = temp.resolve("league.json");
        Files.writeString(file, "{\"leagueId\":\"alterlega\"}");

        assertThrows(Exception.class, () -> LeagueMetadataLoader.load(file));
    }
}
