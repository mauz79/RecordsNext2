package it.alterlega.recordsnext.app.output;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SeasonFamilyShardPublisherTest {
    @TempDir Path temp;

    @Test
    void startsOnlineWithAnchorOnlyAndActivatesHistoryExplicitly() throws Exception {
        Path generated = temp.resolve("generated");
        Path site2006 = temp.resolve("Lega2006");
        Path site2007 = temp.resolve("Lega2007");
        Path state = temp.resolve("data/consolidation/recordsnext-shards.properties");
        Files.createDirectories(generated);
        Files.createDirectories(site2007);

        writeCore(generated, site2006, site2007);
        writeMatches(generated);

        var initial = SeasonFamilyShardPublisher.prepare(generated, temp.resolve("shards-1"), state);
        assertEquals("2007_2008", initial.anchorSeasonId());
        assertEquals(1, initial.onlineSeasonCount());
        assertEquals(1, initial.localSeasonCount());

        String facade = Files.readString(generated.resolve("fcmRecordsNext_Matches.js"));
        assertFalse(facade.contains("https://example.test/lega2006/js/recordsnext-data/"));
        assertTrue(facade.contains("https://example.test/lega2007/js/recordsnext-data/"));

        var firstPublish = SeasonFamilyShardPublisher.publishShards(initial);
        assertEquals(1, firstPublish.written());
        assertEquals(1, firstPublish.skippedUnavailable());
        assertFalse(Files.exists(site2006.resolve("js/recordsnext-data/fcmRecordsNext_Matches.2006_2007.js")));
        assertTrue(Files.isRegularFile(site2007.resolve("js/recordsnext-data/fcmRecordsNext_Matches.2007_2008.js")));

        Files.createDirectories(site2006);
        SeasonFamilyShardPublisher.setLocalAvailability(state, "2006_2007", true);
        SeasonFamilyShardPublisher.setOnlineAvailability(state, "2006_2007", true);
        writeMatches(generated);

        var migrated = SeasonFamilyShardPublisher.prepare(generated, temp.resolve("shards-2"), state);
        assertEquals(2, migrated.onlineSeasonCount());
        assertEquals(2, migrated.localSeasonCount());
        facade = Files.readString(generated.resolve("fcmRecordsNext_Matches.js"));
        assertTrue(facade.contains("https://example.test/lega2006/js/recordsnext-data/"));

        var secondPublish = SeasonFamilyShardPublisher.publishShards(migrated);
        assertEquals(1, secondPublish.written());
        assertEquals(1, secondPublish.unchanged());
        assertTrue(Files.isRegularFile(site2006.resolve("js/recordsnext-data/fcmRecordsNext_Matches.2006_2007.js")));

        writeMatches(generated);
        var cached = SeasonFamilyShardPublisher.prepare(generated, temp.resolve("shards-3"), state);
        var thirdPublish = SeasonFamilyShardPublisher.publishShards(cached);
        assertEquals(0, thirdPublish.written());
        assertEquals(2, thirdPublish.unchanged());
    }

    @Test
    void keepsOriginalIndexesForGloballySortedCulometroRows() throws Exception {
        Path generated = temp.resolve("generated-culometro");
        Path site2006 = temp.resolve("Culo2006");
        Path site2007 = temp.resolve("Culo2007");
        Files.createDirectories(generated);
        Files.createDirectories(site2007);
        writeCore(generated, site2006, site2007);

        String culometro = "window.fcmRecordsNextCulometro = {"
                + "\"schemaVersion\":\"2.0\",\"familyId\":\"culometro\","
                + "\"metadata\":{},\"configuration\":{},\"events\":["
                + "{\"seasonId\":\"2006_2007\",\"team\":\"A\"},"
                + "{\"seasonId\":\"2007_2008\",\"team\":\"B\"}],"
                + "\"ranking\":["
                + "{\"seasonId\":\"2007_2008\",\"team\":\"B\",\"index\":90},"
                + "{\"seasonId\":\"2006_2007\",\"team\":\"A\",\"index\":80}],"
                + "\"competitionRanking\":[],\"outputStatus\":[]};\n";
        Files.writeString(generated.resolve("fcmRecordsNext_Culometro.js"), culometro);

        var plan = SeasonFamilyShardPublisher.prepare(generated, temp.resolve("culometro-shards"),
                temp.resolve("culometro-state.properties"));
        assertEquals(2, plan.shards().size());
        assertTrue(plan.maxShardBytes() < SeasonFamilyShardPublisher.DEFAULT_MAX_SHARD_BYTES);

        String shard2006 = Files.readString(plan.shards().stream()
                .filter(s -> s.seasonId().equals("2006_2007")).findFirst().orElseThrow().stagedFile());
        String shard2007 = Files.readString(plan.shards().stream()
                .filter(s -> s.seasonId().equals("2007_2008")).findFirst().orElseThrow().stagedFile());
        assertTrue(shard2006.contains("\"ranking\":[[1,"));
        assertTrue(shard2007.contains("\"ranking\":[[0,"));
    }

    private static void writeCore(Path generated, Path site2006, Path site2007) throws Exception {
        String core = "window.fcmRecordsNextCore = {\"seasons\":["
                + "{\"seasonId\":\"2006_2007\",\"isAnchor\":0,\"localSitePath\":" + q(site2006.toString())
                + ",\"onlineSiteUrl\":\"https://example.test/lega2006\"},"
                + "{\"seasonId\":\"2007_2008\",\"isAnchor\":1,\"localSitePath\":" + q(site2007.toString())
                + ",\"onlineSiteUrl\":\"https://example.test/lega2007\"}]};\n";
        Files.writeString(generated.resolve("fcmRecordsNext_Core.js"), core);
    }

    private static void writeMatches(Path generated) throws Exception {
        Files.writeString(generated.resolve("fcmRecordsNext_Matches.js"),
                "window.fcmRecordsNextMatches = {\"matches\":["
                        + "{\"seasonId\":\"2006_2007\",\"x\":1},"
                        + "{\"seasonId\":\"2007_2008\",\"x\":2}]};\n");
    }

    private static String q(String text) {
        return "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
