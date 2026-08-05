package it.alterlega.recordsnext.app.classics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassicsFamilyJsExporterTest {
    @TempDir
    Path temp;

    @Test
    void writesNativeFamilyContractFromConsolidatedArchive() throws Exception {
        Path archive = temp.resolve("archive");
        Path season = archive.resolve("2025_2026");
        Files.createDirectories(season);
        Files.writeString(
                season.resolve("season_records_serie_a.json"),
                "{\"records\":{\"puntiSquadraMax\":[{"
                        + "\"recordId\":\"classics.highest-match-score\","
                        + "\"nome\":\"Maggior punteggio\","
                        + "\"valore\":99.5,"
                        + "\"squadra\":\"Test\"}]}}"
        );

        Path output = temp.resolve(ClassicsFamilyJsExporter.FILE_NAME);
        var result = ClassicsFamilyJsExporter.export(archive, output);
        String js = Files.readString(output);

        assertEquals(1, result.seasonCount());
        assertEquals(1, result.entryCount());
        assertTrue(js.startsWith("window.fcmRecordsNextClassics = {"));
        assertTrue(js.contains("\"familyId\":\"classics\""));
        assertTrue(js.contains("\"seasonAggregates\":["));
        assertTrue(js.contains("classics.highest-match-score"));
        assertTrue(js.contains("GENERATED_COMPLETE"));
    }
}
