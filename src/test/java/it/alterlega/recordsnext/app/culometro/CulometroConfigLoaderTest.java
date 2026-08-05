package it.alterlega.recordsnext.app.culometro;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CulometroConfigLoaderTest {
    @Test
    void loadsResettableGoliardicPresetAndRejectsWeightOutsideRange() throws Exception {
        Path ok = Path.of("config/culometro.json");
        CulometroConfig c = CulometroConfigLoader.load(ok);

        assertTrue(c.enabled());
        assertEquals(20, c.minimumMatches());
        assertEquals("GOLIARDICO", c.labelConfiguration().preset());
        assertFalse(c.labelConfiguration().customized());
        assertEquals("GOLIARDICO_DEFAULT", c.labelConfiguration().resetSource());
        assertEquals("Co' 'sso culo puoi andare a cazzi", c.labels().get(0).label());
        assertEquals(c.labels(), c.labelConfiguration().resetBands());
        assertTrue(c.labelConfiguration().presetDefaults().containsKey("NEUTRAL_DEFAULT"));

        String bad = Files.readString(ok)
                .replace("\"weight\": 1.15", "\"weight\": 9.15");

        Path temp = Files.createTempFile("culometro-bad-", ".json");
        Files.writeString(temp, bad);

        try {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> CulometroConfigLoader.load(temp)
            );
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    @Test
    void requiresCustomizedFlagWhenActiveLabelsDifferFromResetPreset() throws Exception {
        Path ok = Path.of("config/culometro.json");
        String original = Files.readString(ok);

        String label = "Co' 'sso culo puoi andare a cazzi";
        int firstOccurrence = original.indexOf(label);

        assertTrue(
                firstOccurrence >= 0,
                "Etichetta goliardica predefinita non trovata nel file di configurazione"
        );

        String changed =
                original.substring(0, firstOccurrence)
                        + "Etichetta modificata"
                        + original.substring(firstOccurrence + label.length());

        changed = changed.replace(
                "\"customized\": false",
                "\"customized\": true"
        );

        Path temp = Files.createTempFile("culometro-custom-", ".json");
        Files.writeString(temp, changed);

        try {
            CulometroConfig config = CulometroConfigLoader.load(temp);

            assertTrue(config.labelConfiguration().customized());
            assertEquals("GOLIARDICO", config.labelConfiguration().preset());
            assertEquals(
                    "GOLIARDICO_DEFAULT",
                    config.labelConfiguration().resetSource()
            );
            assertEquals(
                    "Etichetta modificata",
                    config.labels().get(0).label()
            );
            assertEquals(
                    "Co' 'sso culo puoi andare a cazzi",
                    config.labelConfiguration().resetBands().get(0).label()
            );
        } finally {
            Files.deleteIfExists(temp);
        }
    }
}
