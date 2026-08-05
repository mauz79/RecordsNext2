package it.alterlega.recordsnext.app.ru;

import it.alterlega.recordsnext.Records2026RuJsExporter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/** Genera l'output familiare nativo RecordsNext 2.0 per le Riserve d'Ufficio. */
public final class RuFamilyJsExporter {
    public static final String FILE_NAME = "fcmRecordsNext_RU.js";
    public static final String GLOBAL_NAME = "window.fcmRecordsNextRU";

    private static final String LEGACY_FILE = "records2026.recordstagionali.ru.js";
    private static final String LEGACY_PREFIX = "window.RECORDS2026_PREVIEW_RU = ";

    private RuFamilyJsExporter() {
    }

    public static ExportResult export(Path archiveRoot, Path outputFile) throws IOException {
        Path parent = outputFile.toAbsolutePath().normalize().getParent();
        if (parent == null) {
            throw new IOException("Directory output RU non determinabile: " + outputFile);
        }
        Files.createDirectories(parent);

        Path temporaryDir = Files.createTempDirectory(parent, "recordsnext-ru-legacy-");
        try {
            Records2026RuJsExporter.ExportResult legacy =
                    Records2026RuJsExporter.export(archiveRoot, temporaryDir);

            Path legacyCompact = temporaryDir.resolve(LEGACY_FILE);
            String legacyJs = Files.readString(legacyCompact, StandardCharsets.UTF_8).trim();
            if (!legacyJs.startsWith(LEGACY_PREFIX) || !legacyJs.endsWith(";")) {
                throw new IOException("Formato RU legacy inatteso: " + legacyCompact);
            }

            String seasonsJson = legacyJs.substring(
                    LEGACY_PREFIX.length(),
                    legacyJs.length() - 1
            ).trim();

            String javascript = GLOBAL_NAME + " = {"
                    + "\"schemaVersion\":\"2.0\","
                    + "\"familyId\":\"office-reserves\","
                    + "\"metadata\":{"
                    + "\"source\":\"RecordsNext 1.0.2 normalized RU archive\","
                    + "\"seasonCount\":" + legacy.seasons() + ","
                    + "\"annualFileCount\":" + legacy.annualFiles()
                    + "},"
                    + "\"events\":[],"
                    + "\"seasonAggregates\":" + seasonsJson + ","
                    + "\"globalAggregates\":[],"
                    + "\"absoluteOccurrences\":[],"
                    + "\"outputStatus\":[{"
                    + "\"status\":\"GENERATED_COMPLETE\","
                    + "\"detail\":\"Migrazione compatibile dai dataset RU consolidati\""
                    + "}]"
                    + "};\n";

            Files.writeString(
                    outputFile,
                    javascript,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );

            return new ExportResult(legacy.seasons(), legacy.annualFiles(), outputFile);
        } finally {
            deleteTree(temporaryDir);
        }
    }

    private static void deleteTree(Path root) throws IOException {
        if (!Files.exists(root)) return;
        try (var stream = Files.walk(root)) {
            for (Path path : stream.sorted((a, b) -> b.compareTo(a)).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }

    public record ExportResult(int seasonCount, int annualFileCount, Path outputFile) {
    }
}
