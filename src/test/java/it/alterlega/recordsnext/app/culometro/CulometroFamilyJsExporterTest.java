package it.alterlega.recordsnext.app.culometro;

import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class CulometroFamilyJsExporterTest {
    @Test void exportsRankingWithEditableLabels() throws Exception {
        Path dir=Files.createTempDirectory("culometro-test-");
        try {
            Path thresholds=dir.resolve("fcmRecordsNext_ThresholdsLuck.js");
            Files.writeString(thresholds,"window.fcmRecordsNextThresholdsLuck = {\"events\":[{\"eventType\":\"JUST_ENOUGH\",\"direction\":\"FAVOURABLE\",\"seasonId\":\"2025_2026\",\"competitionId\":\"serie_a\",\"matchId\":\"1\",\"teamId\":\"10\",\"team\":\"A\",\"opponent\":\"B\"},{\"eventType\":\"LOSS_BY_A_WHISKER\",\"direction\":\"UNFAVOURABLE\",\"seasonId\":\"2025_2026\",\"competitionId\":\"serie_a\",\"matchId\":\"2\",\"teamId\":\"11\",\"team\":\"B\",\"opponent\":\"A\"}]};\n");
            Path config=dir.resolve("culometro.json");
            String source=Files.readString(Path.of("config/culometro.json")).replace("\"enabled\": false","\"enabled\": true"); Files.writeString(config,source);
            Path out=dir.resolve(CulometroFamilyJsExporter.FILE_NAME);
            CulometroFamilyJsExporter.ExportResult result=CulometroFamilyJsExporter.export(thresholds,dir.resolve("missing-ru.js"),config,out);
            String js=Files.readString(out); assertTrue(js.startsWith(CulometroFamilyJsExporter.GLOBAL_NAME)); assertTrue(js.contains("\"ranking\"")); assertTrue(js.contains("Fortuna eccezionale")); assertEquals(2,result.teamCount());
        } finally { try(var s=Files.walk(dir)){for(Path p:s.sorted((a,b)->b.compareTo(a)).toList())Files.deleteIfExists(p);} }
    }
}
