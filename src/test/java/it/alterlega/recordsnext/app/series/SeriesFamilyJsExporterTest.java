package it.alterlega.recordsnext.app.series;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SeriesFamilyJsExporterTest {
    @TempDir Path temp;

    @Test
    void exportsAvailableSeriesSectionsAsCompleteFamily() throws Exception {
        Path season = temp.resolve("archive/2025_2026");
        Files.createDirectories(season);
        Files.writeString(season.resolve("season_records_serie_a.json"), """
            {"records":{
              "puntiSquadraMax":[{"recordId":"x","valore":99}],
              "serieVittorie":[{"recordId":"v","valore":4,"squadra":"A"}],
              "seriePareggi":[{"recordId":"p","valore":2,"squadra":"A"}],
              "serieSconfitte":[{"recordId":"s","valore":3,"squadra":"A"}],
              "serieSenzaSconfitte":[{"recordId":"i","valore":10,"squadra":"A"}],
              "serieSenzaVittorie":[{"recordId":"n","valore":5,"squadra":"A"}],
              "capitanoSerieSquadre":[{"recordId":"c","valore":3,"squadra":"A"}],
              "cleanSheetPortiereSerieSquadre":[{"recordId":"g","valore":4,"squadra":"A"}]
            }}
            """, StandardCharsets.UTF_8);

        Path output = temp.resolve("fcmRecordsNext_Series.js");
        SeriesFamilyJsExporter.export(temp.resolve("archive"), output);
        String js = Files.readString(output, StandardCharsets.UTF_8);

        assertTrue(js.startsWith("window.fcmRecordsNextSeries = "));
        assertTrue(js.contains("serieVittorie"));
        assertTrue(js.contains("seriePareggi"));
        assertTrue(js.contains("serieSconfitte"));
        assertTrue(js.contains("serieSenzaSconfitte"));
        assertTrue(js.contains("serieSenzaVittorie"));
        assertTrue(js.contains("capitanoSerieSquadre"));
        assertTrue(js.contains("cleanSheetPortiereSerieSquadre"));
        assertFalse(js.contains("puntiSquadraMax"));
        assertTrue(js.contains("GENERATED_COMPLETE"));
        assertFalse(js.contains("GENERATED_PARTIAL"));
    }
}
