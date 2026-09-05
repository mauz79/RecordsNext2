package it.alterlega.recordsnext.app.output;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SeasonFamilyShardPublisherFlatTest {

    @TempDir
    Path temp;

    @Test
    void createsFlatSeasonShardsBesideFacadeWithoutSubdirectories() throws Exception {
        Path generated = temp.resolve("js");
        Files.createDirectories(generated);

        Files.writeString(
                generated.resolve("fcmRecordsNext_Matches.js"),
                "window.fcmRecordsNextMatches = {\"matches\":["
                        + "{\"seasonId\":\"2024_2025\",\"matchId\":1},"
                        + "{\"seasonId\":\"2025_2026\",\"matchId\":2}]};\n"
        );

        var plan = SeasonFamilyShardPublisher.prepareFlat(generated);

        assertEquals(2, plan.shards().size());
        assertTrue(Files.isRegularFile(generated.resolve(
                "fcmRecordsNext_Matches.2024_2025.js")));
        assertTrue(Files.isRegularFile(generated.resolve(
                "fcmRecordsNext_Matches.2025_2026.js")));

        try (var children = Files.list(generated)) {
            assertFalse(children.anyMatch(Files::isDirectory));
        }

        String facade = Files.readString(generated.resolve("fcmRecordsNext_Matches.js"));
        assertTrue(facade.contains("fcmRecordsNext_Matches.2024_2025.js"));
        assertTrue(facade.contains("fcmRecordsNext_Matches.2025_2026.js"));
        assertFalse(facade.contains("recordsnext-data"));
        assertTrue(facade.contains("document.currentScript"));
        assertTrue(plan.maxShardBytes() <= SeasonFamilyShardPublisher.DEFAULT_FLAT_MAX_SHARD_BYTES);
    }

    @Test
    void recognizesOnlyFlatShardNamesNotFacadeNames() {
        assertTrue(SeasonFamilyShardPublisher.isFlatShardFileName(
                "fcmRecordsNext_Series.2026_2027.js"));
        assertFalse(SeasonFamilyShardPublisher.isFlatShardFileName(
                "fcmRecordsNext_Series.js"));
        assertFalse(SeasonFamilyShardPublisher.isFlatShardFileName(
                "fcmRecordsNext_Core.js"));
    }
}
