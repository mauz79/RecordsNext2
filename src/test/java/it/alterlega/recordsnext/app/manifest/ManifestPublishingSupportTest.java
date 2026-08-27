package it.alterlega.recordsnext.app.manifest;

import it.alterlega.recordsnext.app.PipelinePreflight;
import it.alterlega.recordsnext.app.ProcessingOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ManifestPublishingSupportTest {
    @TempDir
    Path tempDir;

    @Test
    void manifestIncludesAlreadyGeneratedFilesAndItself() throws Exception {
        Files.writeString(tempDir.resolve("fcmRecordsNext_Classics.js"), "window.TEST = {};\n");

        ProcessingOptions options = new ProcessingOptions(true, false, true, false);
        PipelinePreflight.Result preflight = PipelinePreflight.evaluate(options);
        ManifestMetadata metadata = new ManifestMetadata(
                "RecordsNext by mauz79",
                "2.0.0",
                "2.0",
                OffsetDateTime.parse("2026-08-05T15:30:00+02:00"),
                "alterlega",
                "2025_2026",
                List.of("2025_2026"),
                List.of()
        );

        Path manifest = ManifestPublishingSupport.write(tempDir, options, preflight, metadata);
        String js = Files.readString(manifest);

        assertTrue(js.contains("fcmRecordsNext_Classics.js"));
        assertTrue(js.contains("fcmRecordsNext_Manifest.js"));
        assertTrue(js.contains("window.fcmRecordsNextManifest"));
    }
}
