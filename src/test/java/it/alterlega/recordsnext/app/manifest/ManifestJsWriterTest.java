package it.alterlega.recordsnext.app.manifest;

import it.alterlega.recordsnext.app.PipelinePreflight;
import it.alterlega.recordsnext.app.ProcessingOptions;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManifestJsWriterTest {
    @Test
    void legacyClassicsAndRuProduceManifestWithoutCulometro() {
        ProcessingOptions options = new ProcessingOptions(true, true, true, false);
        PipelinePreflight.Result preflight = PipelinePreflight.evaluate(options);
        ManifestMetadata metadata = new ManifestMetadata(
                "RecordsNext by mauz79",
                "2.0.0",
                "2.0",
                OffsetDateTime.parse("2026-08-05T15:30:00+02:00"),
                "alterlega",
                "2025_2026",
                List.of("2025_2026"),
                List.of("fcmRecordsNext_Classics.js", "fcmRecordsNext_RU.js")
        );

        String js = ManifestJsWriter.render(options, preflight, metadata);

        assertTrue(js.startsWith("window.fcmRecordsNextManifest = {"));
        assertTrue(js.contains("requestedFamilies: [\"classics\", \"ru\"]"));
        assertTrue(js.contains("culometroGenerated: false"));
        assertTrue(js.contains("fcmRecordsNext_Classics.js"));
        assertFalse(js.contains("null"));
    }
}
