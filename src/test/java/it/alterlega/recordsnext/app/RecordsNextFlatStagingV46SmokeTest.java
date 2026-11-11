package it.alterlega.recordsnext.app;

import it.alterlega.recordsnext.app.output.SeasonFamilyShardPublisher;
import it.alterlega.recordsnext.app.topflop.TopFlopPublicViewsStageExporter;
import it.alterlega.recordsnext.app.topflop.TopFlopViewsPublication;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Regressione sull'errore dell'11/11/2026: Classics cumulativo di 8.398.766
 * byte rifiutato prima della suddivisione nello staging senza publish.
 * Non accede a database, siti o configurazioni personali.
 */
public final class RecordsNextFlatStagingV46SmokeTest {
    private static final long LIMIT = TopFlopPublicViewsStageExporter.LIMIT_BYTES;
    private RecordsNextFlatStagingV46SmokeTest() { }

    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("recordsnext-v46-shard-check-");
        try {
            Path js = Files.createDirectories(root.resolve("staging/js"));
            List<String> seasons = new ArrayList<>();
            StringBuilder cumulative = new StringBuilder("window.fcmRecordsNextClassics = {\"seasonAggregates\":[");
            boolean first = true;
            String blob = "X".repeat(1750);
            for (int year = 2006; year <= 2025; year++) {
                String season = year + "_" + (year + 1);
                seasons.add(season);
                for (int index = 0; index < 250; index++) {
                    if (!first) cumulative.append(',');
                    first = false;
                    cumulative.append("{\"stagione\":\"").append(season)
                            .append("\",\"recordId\":").append(index)
                            .append(",\"payload\":\"").append(blob).append("\"}");
                }
            }
            cumulative.append("]};\n");
            Path classics = js.resolve("fcmRecordsNext_Classics.js");
            Files.writeString(classics, cumulative, StandardCharsets.UTF_8);
            long original = Files.size(classics);
            require(original > 8_398_766, "fixture non rappresentativa: " + original);
            require(original > LIMIT, "raw sotto il limite");

            // La stessa sequenza che ora esegue la pipeline prima del controllo.
            SeasonFamilyShardPublisher.FlatPlan plan =
                    SeasonFamilyShardPublisher.prepareFlat(js, LIMIT);
            int validated = TopFlopViewsPublication.validateStaging(js);
            require(plan.shards().size() == 20, "shard inattesi: " + plan.shards().size());
            require(validated == 21, "numero JS inatteso: " + validated);
            require(Files.size(classics) < LIMIT, "facade troppo grande");
            for (Path file : plan.facades()) {
                require(Files.size(file) <= LIMIT, "facade fuori limite " + file);
            }
            for (var item : plan.shards()) {
                require(item.bytes() > 0 && item.bytes() <= LIMIT, "shard fuori limite: " + item.file());
                require(Files.readString(item.file()).split("\\\"payload\\\"", -1).length - 1 == 250,
                        "contenuto shard alterato: " + item.file());
            }
            require(Files.readString(classics).contains("fcmRecordsNext_Classics.2006_2007.js"),
                    "facade non collega i dati storici");

            // Un file non suddivisibile non puo' essere pubblicato per errore.
            Path oversize = js.resolve("fcmRecordsNext_Unknown.js");
            Files.writeString(oversize, "Z".repeat((int)LIMIT + 1), StandardCharsets.UTF_8);
            boolean blocked = false;
            try {
                TopFlopViewsPublication.validateStaging(js);
            } catch (IOException expected) {
                blocked = expected.getMessage().contains("1.500.000");
            }
            require(blocked, "limite totale aggirato su file non shardabile");
            Files.delete(oversize);

            // I singoli shard sopra il limite vengono sempre respinti.
            Path oversizedSingleSeason = root.resolve("oversized-single/js");
            Files.createDirectories(oversizedSingleSeason);
            Files.writeString(oversizedSingleSeason.resolve("fcmRecordsNext_Classics.js"),
                    "window.fcmRecordsNextClassics = {\"seasonAggregates\":[{\"stagione\":\"2025_2026\",\"payload\":\""
                    + "a".repeat((int)LIMIT) + "\"}]};\n");
            blocked = false;
            try {
                SeasonFamilyShardPublisher.prepareFlat(oversizedSingleSeason, LIMIT);
            } catch (IOException expected) {
                blocked = expected.getMessage().contains("oltre il limite");
            }
            require(blocked, "shard troppo grande non bloccato");
            System.out.println("RN4_FLAT_STAGING_V46_OK original=" + original
                    + " shards=" + plan.shards().size() + " validated=" + validated
                    + " max=" + plan.maxShardBytes() + " limit=" + LIMIT);
        } finally {
            // Togliamo solo il fixture temporaneo creato da questo test.
            try (var walk = Files.walk(root)) {
                for (Path path : walk.sorted(java.util.Comparator.reverseOrder()).toList()) {
                    Files.deleteIfExists(path);
                }
            }
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
