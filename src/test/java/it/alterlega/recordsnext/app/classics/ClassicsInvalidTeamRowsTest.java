package it.alterlega.recordsnext.app.classics;

import it.alterlega.recordsnext.SeasonRecordsArchiveBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassicsInvalidTeamRowsTest {

    @TempDir
    Path temp;

    @Test
    void technicalRowsWithEmptyTeamOrIdZeroNeverReachClassicRecords() throws Exception {
        Path reports = temp.resolve("reports");
        Path season = reports.resolve("2025_2026");
        Path archive = temp.resolve("archive");
        Files.createDirectories(season);

        String json = """
                {
                  "meta": {
                    "stagione": "2025_2026",
                    "competizioneStoricaId": "serie_a",
                    "competizioneNome": "Serie A"
                  },
                  "partiteSquadra": [
                    {
                      "stagione": "2025_2026",
                      "competizioneStoricaId": "serie_a",
                      "competizioneNome": "Serie A",
                      "idIncontro": 1,
                      "idSquadra": 0,
                      "squadra": "",
                      "avversaria": "Squadra B",
                      "puntiFatti": 0,
                      "puntiSubiti": 68,
                      "golFatti": 0,
                      "golSubiti": 1,
                      "golRegolamentariFatti": 0,
                      "golRegolamentariSubiti": 1,
                      "esito": "P",
                      "ordineGiornata": 1
                    },
                    {
                      "stagione": "2025_2026",
                      "competizioneStoricaId": "serie_a",
                      "competizioneNome": "Serie A",
                      "idIncontro": 2,
                      "idSquadra": 10,
                      "squadra": "Squadra A",
                      "avversaria": "Squadra B",
                      "puntiFatti": 63,
                      "puntiSubiti": 68,
                      "golFatti": 0,
                      "golSubiti": 1,
                      "golRegolamentariFatti": 0,
                      "golRegolamentariSubiti": 1,
                      "esito": "S",
                      "ordineGiornata": 2
                    }
                  ],
                  "espulsioniDettaglio": [],
                  "eventiSquadraDettaglio": [],
                  "modificatoriB2Dettaglio": [],
                  "cleanSheetB3Dettaglio": [],
                  "fasceGolDettaglio": []
                }
                """;

        Files.writeString(season.resolve("season_normalized_serie_a.json"), json);

        SeasonRecordsArchiveBuilder.build(reports, archive, List.of("2025_2026"));

        String output = Files.readString(
                archive.resolve("2025_2026").resolve("season_records_serie_a.json")
        );

        assertFalse(output.contains("\"idSquadra\": 0"), output);
        assertFalse(output.contains("\"squadra\": \"\""), output);
        assertTrue(output.contains("\"squadra\": \"Squadra A\""), output);
        assertTrue(output.contains("\"puntiSquadraMin\""), output);
        assertTrue(output.contains("\"valore\": 63"), output);
    }
}
