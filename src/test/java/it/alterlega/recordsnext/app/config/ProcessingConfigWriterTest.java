package it.alterlega.recordsnext.app.config;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ProcessingConfigWriterTest {
    @Test
    void roundTripsDashboardSelections() throws Exception {
        Path file = Files.createTempFile("processing-gui-", ".json");

        try {
            var requested = new ProcessingConfigWriter.State(
                    true,
                    true,
                    false,
                    true,
                    true,
                    true,
                    false,
                    java.util.Map.of(),
                    java.util.Map.of(
                            "MODM1PERS", "Difesa personalizzato",
                            "MODM2PERS", "Capitano",
                            "MODM3PERS", "Bonus fair play"
                    )
            );

            ProcessingConfigWriter.save(file, requested);

            ProcessingConfigWriter.State loaded =
                    ProcessingConfigWriter.load(file);

            assertTrue(loaded.classics());
            assertTrue(loaded.series());
            assertFalse(loaded.ru());
            assertTrue(loaded.modifiers());
            assertTrue(loaded.thresholdsLuck());
            assertTrue(loaded.culometro());
            assertFalse(loaded.publishToSite());
            assertEquals("Difesa personalizzato", loaded.modifierName("MODM1PERS"));
            assertEquals("Capitano", loaded.modifierName("MODM2PERS"));
            assertEquals("Bonus fair play", loaded.modifierName("MODM3PERS"));

            assertFalse(loaded.children().isEmpty());

            assertEquals(
                    true,
                    loaded.children().get("classics.highest-match-score")
            );
            assertEquals(
                    true,
                    loaded.children().get("series.wins")
            );
            assertEquals(
                    true,
                    loaded.children().get("modifiers.home-field-deciding")
            );
            assertEquals(
                    true,
                    loaded.children().get("thresholds.exact-threshold")
            );
            assertEquals(
                    true,
                    loaded.children().get("luck.balance")
            );

            String json = Files.readString(file);

            assertTrue(json.contains("\"home-field-deciding\": true"));
            assertTrue(json.contains("\"children\""));
            assertTrue(json.contains("\"highest-match-score\": true"));
            assertTrue(json.contains("\"wins\": true"));
            assertTrue(json.contains("\"exact-threshold\": true"));
            assertTrue(json.contains("\"modifierNames\""));
            assertTrue(json.contains("\"MODM1PERS\": \"Difesa personalizzato\""));
        } finally {
            Files.deleteIfExists(file);
        }
    }
}
