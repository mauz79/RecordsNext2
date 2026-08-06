package it.alterlega.recordsnext.app.modifiers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModifiersFamilyJsExporterTest {
    @TempDir
    Path temp;

    @Test
    void exportsDirectlyFromRecordsArchiveIncludingFcmAndCustomModifiers() throws Exception {
        Path season2006 = temp.resolve("archive/2006_2007");
        Path season2025 = temp.resolve("archive/2025_2026");
        Files.createDirectories(season2006);
        Files.createDirectories(season2025);

        Files.writeString(season2006.resolve("season_records_serie_a.json"), """
            {"records":{
              "modDifesaFcmMax":[{"recordId":"fcm-max","valore":4,"squadra":"A"}],
              "modDifesaFcmTotaleSquadre":[{"recordId":"fcm-total","valore":18,"squadra":"A"}],
              "modDifesaFcmMediaSquadre":[{"recordId":"fcm-average","valore":1.5,"squadra":"A"}],
              "modDifesaFcmUtilizziSquadre":[{"recordId":"fcm-uses","valore":12,"squadra":"A"}],
              "puntiSquadraMax":[{"recordId":"x","valore":99}]
            }}
            """, StandardCharsets.UTF_8);

        Files.writeString(season2025.resolve("season_records_serie_a.json"), """
            {"records":{
              "modDifesaMax":[{"recordId":"custom-max","valore":6,"squadra":"B"}],
              "modDifesaTotaleSquadre":[{"recordId":"custom-total","valore":22,"squadra":"B"}],
              "modDifesaMediaSquadre":[{"recordId":"custom-average","valore":1.1,"squadra":"B"}],
              "modDifesaUtilizziSquadre":[{"recordId":"custom-uses","valore":20,"squadra":"B"}],
              "capitanoMax":[{"recordId":"captain-max","valore":3,"squadra":"B"}],
              "capitanoTotaleSquadre":[{"recordId":"captain-total","valore":15,"squadra":"B"}],
              "capitanoMediaSquadre":[{"recordId":"captain-average","valore":0.75,"squadra":"B"}],
              "capitanoUtilizziSquadre":[{"recordId":"captain-uses","valore":20,"squadra":"B"}]
            }}
            """, StandardCharsets.UTF_8);

        Path output = temp.resolve("fcmRecordsNext_Modifiers.js");
        ModifiersFamilyJsExporter.ExportResult result = ModifiersFamilyJsExporter.export(
                temp.resolve("archive"),
                output,
                Map.of(
                        "MODM1PERS", "Difesa configurato",
                        "MODM2PERS", "Capitano configurato",
                        "MODM3PERS", "Terzo bonus"
                )
        );

        String js = Files.readString(output, StandardCharsets.UTF_8);

        assertEquals(2, result.seasonCount());
        assertEquals(2, result.entryCount());
        assertTrue(js.startsWith("window.fcmRecordsNextModifiers = "));
        assertTrue(js.contains("2006_2007"));
        assertTrue(js.contains("2025_2026"));
        assertTrue(js.contains("modDifesaFcmMax"));
        assertTrue(js.contains("modDifesaFcmTotaleSquadre"));
        assertTrue(js.contains("modDifesaFcmMediaSquadre"));
        assertTrue(js.contains("modDifesaFcmUtilizziSquadre"));
        assertTrue(js.contains("modDifesaMax"));
        assertTrue(js.contains("modDifesaTotaleSquadre"));
        assertTrue(js.contains("modDifesaMediaSquadre"));
        assertTrue(js.contains("modDifesaUtilizziSquadre"));
        assertTrue(js.contains("capitanoMax"));
        assertTrue(js.contains("capitanoTotaleSquadre"));
        assertTrue(js.contains("capitanoMediaSquadre"));
        assertTrue(js.contains("capitanoUtilizziSquadre"));
        assertFalse(js.contains("puntiSquadraMax"));
        assertTrue(js.contains("generatedSections"));
        assertTrue(js.contains("Difesa configurato"));
        assertTrue(js.contains("Capitano configurato"));
        assertTrue(js.contains("Modificatore Difesa FCM"));
        assertTrue(js.contains("GENERATED_COMPLETE"));
    }

    @Test
    void excludesDisabledModifierOutputs() throws Exception {
        Path season = temp.resolve("archive/2025_2026");
        Files.createDirectories(season);
        Files.writeString(season.resolve("season_records_serie_a.json"), """
            {"records":{
              "modDifesaMax":[{"recordId":"custom-max","valore":6}],
              "capitanoMax":[{"recordId":"captain-max","valore":3}],
              "modDifesaFcmMax":[{"recordId":"fcm-max","valore":4}]
            }}
            """, StandardCharsets.UTF_8);

        Path output = temp.resolve("filtered.js");
        ModifiersFamilyJsExporter.export(
                temp.resolve("archive"),
                output,
                Map.of(),
                Map.of(
                        "modifiers.modm1pers.max", true,
                        "modifiers.modm2pers.max", false,
                        "modifiers.moddifesa.max", true
                )
        );

        String js = Files.readString(output, StandardCharsets.UTF_8);
        assertTrue(js.contains("modDifesaMax"));
        assertTrue(js.contains("modDifesaFcmMax"));
        assertFalse(js.contains("\"capitanoMax\":["));
		assertTrue(js.contains("\"availableSections\""));
assertTrue(js.contains("\"generatedSections\""));
    }
}
