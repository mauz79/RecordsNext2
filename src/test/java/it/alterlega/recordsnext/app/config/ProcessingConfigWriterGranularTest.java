package it.alterlega.recordsnext.app.config;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProcessingConfigWriterGranularTest {

    @Test
    void persistsAndReloadsGranularChildSelection() throws Exception {
        Map<String, Boolean> children = new LinkedHashMap<>();
        children.put("classics.highest-match-score", true);
        children.put("classics.lowest-match-score", false);
        children.put("series.wins", true);
        children.put("thresholds.surgical-win", true);
        children.put("luck.balance", false);

        Map<String, String> modifierNames = new LinkedHashMap<>();
        modifierNames.put("MODM1PERS", "Modificatore Difesa");
        modifierNames.put("MODM2PERS", "Capitano");
        modifierNames.put("MODM3PERS", "");

        ProcessingConfigWriter.State state =
                new ProcessingConfigWriter.State(
                        true,
                        true,
                        false,
                        false,
                        true,
                        false,
                        false,
                        children,
                        modifierNames
                );

        Path file = Files.createTempFile(
                "processing-granular-",
                ".json"
        );

        try {
            ProcessingConfigWriter.save(file, state);

            String json = Files.readString(file);

            assertTrue(
                    json.contains("\"highest-match-score\": true")
            );
            assertTrue(
                    json.contains("\"lowest-match-score\": false")
            );
            assertTrue(
                    json.contains("\"surgical-win\": true")
            );
            assertTrue(
                    json.contains("\"balance\": false")
            );

            assertTrue(
                    json.contains(
                            "\"MODM1PERS\": \"Modificatore Difesa\""
                    )
            );
            assertTrue(
                    json.contains(
                            "\"MODM2PERS\": \"Capitano\""
                    )
            );
            assertTrue(
                    json.contains(
                            "\"MODM3PERS\": \"\""
                    )
            );

            ProcessingConfigWriter.State loaded =
                    ProcessingConfigWriter.load(file);

            assertTrue(
                    loaded.childEnabled(
                            "classics.highest-match-score"
                    )
            );
            assertFalse(
                    loaded.childEnabled(
                            "classics.lowest-match-score"
                    )
            );
            assertTrue(
                    loaded.childEnabled("series.wins")
            );
            assertTrue(
                    loaded.childEnabled(
                            "thresholds.surgical-win"
                    )
            );
            assertFalse(
                    loaded.childEnabled("luck.balance")
            );

            assertEquals(
                    "Modificatore Difesa",
                    loaded.modifierNames().get("MODM1PERS")
            );
            assertEquals(
                    "Capitano",
                    loaded.modifierNames().get("MODM2PERS")
            );
            assertEquals(
                    "",
                    loaded.modifierNames().get("MODM3PERS")
            );
        } finally {
            Files.deleteIfExists(file);
        }
    }
}