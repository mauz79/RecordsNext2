package it.alterlega.recordsnext.app.ru;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuFamilyJsExporterTest {
    @Test
    void writesNativeRuFamilyOutput() throws Exception {
        Path root = Files.createTempDirectory("rn2-ru-test-");
        Path archive = root.resolve("archive");
        Path season = archive.resolve("2025_2026");
        Files.createDirectories(season);
        Files.writeString(
                season.resolve("riserveufficio.json"),
                "{\"meta\":{\"generato\":\"2026-08-05\"},"
                        + "\"views\":{\"ru\":[{\"squadra\":\"Test\",\"totale\":2}]},"
                        + "\"dettaglio\":{\"ruDettaglio\":[]},\"curiosita\":[]}",
                StandardCharsets.UTF_8
        );

        Path output = root.resolve(RuFamilyJsExporter.FILE_NAME);
        RuFamilyJsExporter.ExportResult result = RuFamilyJsExporter.export(archive, output);

        String js = Files.readString(output, StandardCharsets.UTF_8);
        assertEquals(1, result.seasonCount());
        assertEquals(1, result.annualFileCount());
        assertTrue(js.startsWith("window.fcmRecordsNextRU = "));
        assertTrue(js.contains("\"familyId\":\"office-reserves\""));
        assertTrue(js.contains("\"stagione\":\"2025_2026\""));
        assertTrue(js.contains("\"status\":\"GENERATED_COMPLETE\""));
    }
}
