package it.alterlega.recordsnext.app.series;

import it.alterlega.recordsnext.SeasonRecordsArchiveBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SeriesCompleteIntegrationTest {
    @TempDir
    Path temp;

    @Test
    void exportsAllResultSeries() throws Exception {
        Path reports = temp.resolve("reports");
        Path season = reports.resolve("2025_2026");
        Files.createDirectories(season);
        Files.writeString(season.resolve("season_normalized_serie_a.json"), normalized(), StandardCharsets.UTF_8);

        Path archive = temp.resolve("archive");
        SeasonRecordsArchiveBuilder.build(reports, archive, List.of("2025_2026"));

        Path output = temp.resolve(SeriesFamilyJsExporter.FILE_NAME);
        SeriesFamilyJsExporter.export(archive, output);
        String js = Files.readString(output, StandardCharsets.UTF_8);

        assertTrue(js.startsWith(SeriesFamilyJsExporter.GLOBAL_NAME));
        assertTrue(js.contains("serieVittorie"));
        assertTrue(js.contains("seriePareggi"));
        assertTrue(js.contains("serieSconfitte"));
        assertTrue(js.contains("serieSenzaVittorie"));
        assertTrue(js.contains("GENERATED_COMPLETE"));
    }

    private static String normalized() {
        return """
            {
              "meta": {
                "stagione": "2025_2026",
                "competizioneStoricaId": "serie_a",
                "competizioneNome": "Serie A"
              },
              "partiteSquadra": [
                {"stagione":"2025_2026","competizioneStoricaId":"serie_a","competizioneNome":"Serie A","idSquadra":"1","squadra":"Alpha","idIncontro":"1","ordineGiornata":1,"giornata":"1","giornataDiA":1,"urlTabellino":"ris.htm?Gio=1","avversaria":"Beta","esito":"V","risultato":"1-0","punteggio":"66-65","puntiFatti":66},
                {"stagione":"2025_2026","competizioneStoricaId":"serie_a","competizioneNome":"Serie A","idSquadra":"1","squadra":"Alpha","idIncontro":"2","ordineGiornata":2,"giornata":"2","giornataDiA":2,"urlTabellino":"ris.htm?Gio=2","avversaria":"Gamma","esito":"V","risultato":"2-0","punteggio":"72-60","puntiFatti":72},
                {"stagione":"2025_2026","competizioneStoricaId":"serie_a","competizioneNome":"Serie A","idSquadra":"1","squadra":"Alpha","idIncontro":"3","ordineGiornata":3,"giornata":"3","giornataDiA":3,"urlTabellino":"ris.htm?Gio=3","avversaria":"Delta","esito":"N","risultato":"1-1","punteggio":"66-66","puntiFatti":66},
                {"stagione":"2025_2026","competizioneStoricaId":"serie_a","competizioneNome":"Serie A","idSquadra":"1","squadra":"Alpha","idIncontro":"4","ordineGiornata":4,"giornata":"4","giornataDiA":4,"urlTabellino":"ris.htm?Gio=4","avversaria":"Epsilon","esito":"S","risultato":"0-1","punteggio":"65-66","puntiFatti":65}
              ],
              "espulsioniDettaglio": [],
              "eventiSquadraDettaglio": [],
              "modificatoriB2Dettaglio": [],
              "cleanSheetB3Dettaglio": []
            }
            """;
    }
}
