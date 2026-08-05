package it.alterlega.recordsnext.app.modifiers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModifiersFamilyJsExporterTest {
    @TempDir Path temp;

    @Test
    void exportsCompleteModifierFamilyIncludingHomeField() throws Exception {
        Path season = temp.resolve("archive/2025_2026");
        Files.createDirectories(season);
        Files.writeString(season.resolve("season_records_serie_a.json"), """
            {"records":{
              "puntiSquadraMax":[{"recordId":"x","valore":99}],
              "modDifesaMax":[{"recordId":"d","valore":6,"squadra":"A"}],
              "capitanoTotaleSquadre":[{"recordId":"c","valore":3,"squadra":"A"}],
              "fattoreCampoDecisivo":[{"recordId":"fc","valore":1,"squadra":"A"}],
              "fattoreCampoTotaleSquadre":[{"recordId":"fct","valore":18,"squadra":"A"}],
              "fattoreCampoPuntiGuadagnatiSquadre":[{"recordId":"fcg","valore":3,"squadra":"A"}],
              "fattoreCampoPuntiPersiSquadre":[{"recordId":"fcp","valore":3,"squadra":"B"}]
            }}
            """, StandardCharsets.UTF_8);

        Path output = temp.resolve("fcmRecordsNext_Modifiers.js");
        ModifiersFamilyJsExporter.export(temp.resolve("archive"), output);
        String js = Files.readString(output, StandardCharsets.UTF_8);

        assertTrue(js.startsWith("window.fcmRecordsNextModifiers = "));
        assertTrue(js.contains("modDifesaMax"));
        assertTrue(js.contains("capitanoTotaleSquadre"));
        assertTrue(js.contains("fattoreCampoDecisivo"));
        assertTrue(js.contains("fattoreCampoTotaleSquadre"));
        assertTrue(js.contains("fattoreCampoPuntiGuadagnatiSquadre"));
        assertTrue(js.contains("fattoreCampoPuntiPersiSquadre"));
        assertFalse(js.contains("puntiSquadraMax"));
        assertTrue(js.contains("GENERATED_COMPLETE"));
        assertFalse(js.contains("GENERATED_PARTIAL"));
    }
}
