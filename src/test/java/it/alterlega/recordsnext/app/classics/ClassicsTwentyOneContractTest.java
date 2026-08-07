package it.alterlega.recordsnext.app.classics;

import it.alterlega.recordsnext.Records2026ClassicJsExporter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassicsTwentyOneContractTest {
    @TempDir
    Path temp;

    @Test
    void exporterRecognizesAllTwentyOneClassicSections() throws Exception {
        String[] sections = {
                "puntiSquadraMax", "puntiSquadraMin",
                "partitePiuGolRegolamentari", "partitePiuScartoRegolamentari",
                "mediaPuntiSquadre", "totalePuntiSquadre", "puntiClassificaSquadre",
                "vittorieSquadre", "pareggiSquadre", "sconfitteSquadre",
                "golFattiSquadre", "golSubitiSquadre",
                "ammonizioniSquadre", "espulsioniSquadre", "espulsioniGiocatori",
                "assistSquadre", "autogolSquadre", "golRigoreSquadre",
                "rigoriSbagliatiSquadre", "rigoriParatiSquadre",
                "cleanSheetPortiereVolteSquadre"
        };

        Path archive = temp.resolve("archive");
        Path season = archive.resolve("2025_2026");
        Files.createDirectories(season);

        StringBuilder records = new StringBuilder();
        for (int i = 0; i < sections.length; i++) {
            if (i > 0) records.append(',');
            records.append('"').append(sections[i]).append('"')
                    .append(":[{\"recordId\":\"").append(sections[i])
                    .append("\",\"nome\":\"Test\",\"valore\":1,\"squadra\":\"A\"}]");
        }

        Files.writeString(
                season.resolve("season_records_serie_a.json"),
                "{\"records\":{" + records + "}}"
        );

        Path output = temp.resolve("classic.js");
        Records2026ClassicJsExporter.export(archive, output, List.of());
        String js = Files.readString(output);

        for (String section : sections) {
            assertTrue(js.contains("\"" + section + "\""), "Sezione Classici non esportata: " + section);
        }
    }
}
