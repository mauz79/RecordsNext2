package it.alterlega.recordsnext.app.config;

import it.alterlega.recordsnext.app.model.CoreRecordCatalog;
import it.alterlega.recordsnext.app.model.RecordFamily;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ProcessingConfigLoaderTest {
    @TempDir Path temp;

    @Test
    void loadsFamiliesAndKeepsCulometroDisabled() throws Exception {
        Path file = temp.resolve("processing.json");
        Files.writeString(file, """
                {"schemaVersion":"2.0","processing":{"families":{
                  "classics":{"enabled":true,"children":"ALL"},
                  "series":{"enabled":true,"children":"ALL"},
                  "ru":{"enabled":true,"children":"ALL"},
                  "modifiers":{"enabled":true,"children":{"defence":true,"captain":false,"homeField":true}},
                  "thresholdsLuck":{"enabled":true,"children":"ALL"}},
                  "culometro":{"enabled":false},
                  "output":{"writeManifest":true,"writeCore":true,"publishToSite":false}}}
                """);
        var options = ProcessingConfigLoader.load(file);
        assertEquals(5, options.selection().enabledFamilies().size());
        assertTrue(options.familyEnabled(RecordFamily.MODIFIERS));
        assertFalse(options.culometroEnabled());
        assertFalse(options.selection().enabledChildren().contains(CoreRecordCatalog.CULOMETRO_ID));
        assertTrue(options.selection().enabledChildren().contains("modifiers.defence"));
        assertFalse(options.selection().enabledChildren().contains("modifiers.captain"));
    }

    @Test
    void enablesCulometroOnlyWhenExplicitlyRequested() throws Exception {
        Path file = temp.resolve("processing.json");
        Files.writeString(file, """
                {"schemaVersion":"2.0","processing":{"families":{
                  "thresholdsLuck":{"enabled":true,"children":"ALL"}},
                  "culometro":{"enabled":true},
                  "output":{"writeManifest":true,"writeCore":true,"publishToSite":false}}}
                """);
        var options = ProcessingConfigLoader.load(file);
        assertTrue(options.culometroEnabled());
        assertTrue(options.selection().enabledChildren().contains(CoreRecordCatalog.CULOMETRO_ID));
    }

    @Test
    void rejectsUnsupportedSchema() throws Exception {
        Path file = temp.resolve("processing.json");
        Files.writeString(file, "{\"schemaVersion\":\"3.0\",\"processing\":{}}");
        assertThrows(IllegalArgumentException.class, () -> ProcessingConfigLoader.load(file));
    }
}
