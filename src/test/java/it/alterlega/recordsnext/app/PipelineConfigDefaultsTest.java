package it.alterlega.recordsnext.app;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PipelineConfigDefaultsTest {
    @Test
    void usesDocumentedDefaultDirectoriesWithoutPropertiesFile() {
        Path root = Path.of("D:/DEV_APPS/RecordsNext2.0").toAbsolutePath().normalize();

        PipelineConfig config = PipelineConfig.defaults(root);

        assertEquals(root.resolve("data/reports"), config.reports());
        assertEquals(
            root.resolve("data/records-archive/stagioni"),
            config.classicArchive()
        );
        assertEquals(
            root.resolve("data/records-archive/riserveufficio"),
            config.ruArchive()
        );
        assertEquals(
            root.resolve("data/site-export-staging"),
            config.staging()
        );
        assertTrue(config.seasons().isEmpty());
    }
}
