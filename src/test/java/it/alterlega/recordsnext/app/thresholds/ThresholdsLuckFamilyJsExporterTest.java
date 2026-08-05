package it.alterlega.recordsnext.app.thresholds;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThresholdsLuckFamilyJsExporterTest {
    @TempDir Path temp;

    @Test
    void exportsCompleteObjectiveThresholdAndLuckEventsWithoutCulometro() throws Exception {
        Path reports = temp.resolve("reports/2025_2026");
        Files.createDirectories(reports);
        Files.writeString(reports.resolve("season_normalized_serie_a.json"), """
            {
              "partiteSquadra": [
                {"stagione":"2025_2026","competizioneStoricaId":"serie_a","competizioneNome":"Serie A",
                 "idIncontro":"1","giornata":"1a","giornataDiA":1,"urlTabellino":"ris.htm?Gio=1",
                 "idSquadra":"10","squadra":"Alpha","idAvversaria":"11","avversaria":"Beta",
                 "puntiFatti":66,"puntiSubiti":65.5,"golFatti":1,"golSubiti":0,"esito":"V"},
                {"stagione":"2025_2026","competizioneStoricaId":"serie_a","competizioneNome":"Serie A",
                 "idIncontro":"2","giornata":"2a","giornataDiA":2,"urlTabellino":"ris.htm?Gio=2",
                 "idSquadra":"10","squadra":"Alpha","idAvversaria":"12","avversaria":"Gamma",
                 "puntiFatti":65.5,"puntiSubiti":64,"golFatti":0,"golSubiti":0,"esito":"P"},
                {"stagione":"2025_2026","competizioneStoricaId":"serie_a","competizioneNome":"Serie A",
                 "idIncontro":"3","giornata":"3a","giornataDiA":3,"urlTabellino":"ris.htm?Gio=3",
                 "idSquadra":"10","squadra":"Alpha","idAvversaria":"13","avversaria":"Delta",
                 "puntiFatti":71.5,"puntiSubiti":72,"golFatti":1,"golSubiti":2,"esito":"S"}
              ],
              "fasceGolDettaglio": [
                {"idCompetizioneFcm":1,"idFascia":"1","min":0,"max":65.5,"gol":0},
                {"idCompetizioneFcm":1,"idFascia":"2","min":66,"max":71.5,"gol":1},
                {"idCompetizioneFcm":1,"idFascia":"3","min":72,"max":77.5,"gol":2}
              ]
            }
            """);
        Path output = temp.resolve("fcmRecordsNext_ThresholdsLuck.js");
        ThresholdsLuckFamilyJsExporter.export(temp.resolve("reports"), output);
        String js = Files.readString(output);
        assertTrue(js.startsWith("window.fcmRecordsNextThresholdsLuck = "));
        assertTrue(js.contains("JUST_ENOUGH"));
        assertTrue(js.contains("TIGHT_DRAW"));
        assertTrue(js.contains("LOSS_BY_A_WHISKER"));
        assertTrue(js.contains("ONE_GOAL_WIN"));
        assertTrue(js.contains("ONE_GOAL_LOSS"));
        assertTrue(js.contains("UNUSED_BAND_POINTS"));
        assertTrue(js.contains("\"status\":\"GENERATED_COMPLETE\""));
        assertTrue(js.contains("\"culometroGenerated\":false"));
        assertFalse(js.contains("GENERATED_PARTIAL"));
    }
}
