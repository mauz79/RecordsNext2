package it.alterlega.recordsnext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class SeasonRecordsArchiveBuilderPareggiTest {
    @TempDir
    Path tempDir;

    @Test
    void generaSeriePareggiUsandoEsitoP() throws Exception {
        Path reports = tempDir.resolve("reports");
        Path season = reports.resolve("2025_2026");
        Path archive = tempDir.resolve("archive");
        Files.createDirectories(season);

        String json = """
                {
                  "meta": {
                    "competizioneStoricaId": "serie_a",
                    "competizioneNome": "Serie A"
                  },
                  "partiteSquadra": [
                    {"stagione":"2025_2026","competizioneStoricaId":"serie_a","competizioneNome":"Serie A","idSquadra":"1","squadra":"Squadra Test","idIncontro":"1","ordineGiornata":1,"giornata":"1a","giornataDiA":1,"esito":"P","avversaria":"A","risultato":"1-1","punteggio":"66-66"},
                    {"stagione":"2025_2026","competizioneStoricaId":"serie_a","competizioneNome":"Serie A","idSquadra":"1","squadra":"Squadra Test","idIncontro":"2","ordineGiornata":2,"giornata":"2a","giornataDiA":2,"esito":"P","avversaria":"B","risultato":"2-2","punteggio":"72-72"}
                  ],
                  "espulsioniDettaglio": [],
                  "eventiSquadraDettaglio": [],
                  "modificatoriB2Dettaglio": [],
                  "cleanSheetB3Dettaglio": [],
                  "fasceGolDettaglio": []
                }
                """;
        Files.writeString(season.resolve("season_normalized_serie_a.json"), json, StandardCharsets.UTF_8);

        SeasonRecordsArchiveBuilder.build(reports, archive, List.of("2025_2026"));

        String output = Files.readString(
                archive.resolve("2025_2026/season_records_serie_a.json"),
                StandardCharsets.UTF_8
        );
        assertTrue(output.contains("\"seriePareggi\""));
        assertTrue(output.contains("\"recordId\": \"serie_pareggi\""));
        assertTrue(output.contains("\"valore\": 2"));
    }
}
