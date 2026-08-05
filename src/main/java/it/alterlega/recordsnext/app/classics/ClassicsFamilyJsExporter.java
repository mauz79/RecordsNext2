package it.alterlega.recordsnext.app.classics;

import it.alterlega.recordsnext.Records2026ClassicJsExporter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * Genera l'output familiare nativo RecordsNext 2.0 per i record Classici.
 * Riusa l'exporter consolidato 1.0.2 come sorgente di verita durante la
 * migrazione, evitando di duplicare parser, proiezioni e regole pubbliche.
 */
public final class ClassicsFamilyJsExporter {
    public static final String FILE_NAME = "fcmRecordsNext_Classics.js";
    public static final String GLOBAL_NAME = "window.fcmRecordsNextClassics";

    private static final String LEGACY_PREFIX = "window.RECORDS2026_PREVIEW_CLASSIC = ";

    private ClassicsFamilyJsExporter() {
    }

    public static ExportResult export(Path archiveRoot, Path outputFile) throws IOException {
        Path parent = outputFile.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        Path temporaryLegacy = Files.createTempFile(
                parent != null ? parent : outputFile.toAbsolutePath().getParent(),
                "recordsnext-classics-legacy-",
                ".js"
        );

        try {
            Records2026ClassicJsExporter.ExportResult legacy =
                    Records2026ClassicJsExporter.export(archiveRoot, temporaryLegacy, List.of());

            String legacyJs = Files.readString(temporaryLegacy, StandardCharsets.UTF_8).trim();
            if (!legacyJs.startsWith(LEGACY_PREFIX) || !legacyJs.endsWith(";")) {
                throw new IOException("Formato Classic legacy inatteso: " + temporaryLegacy);
            }

            String entriesJson = legacyJs.substring(
                    LEGACY_PREFIX.length(),
                    legacyJs.length() - 1
            ).trim();

            String javascript = GLOBAL_NAME + " = {"
                    + "\"schemaVersion\":\"2.0\","
                    + "\"familyId\":\"classics\","
                    + "\"metadata\":{"
                    + "\"source\":\"RecordsNext 1.0.2 normalized archive\","
                    + "\"seasonCount\":" + legacy.seasonCount() + ","
                    + "\"entryCount\":" + legacy.entryCount()
                    + "},"
                    + "\"events\":[],"
                    + "\"seasonAggregates\":" + entriesJson + ","
                    + "\"globalAggregates\":[],"
                    + "\"absoluteOccurrences\":[],"
                    + "\"outputStatus\":[{"
                    + "\"status\":\"GENERATED_COMPLETE\","
                    + "\"detail\":\"Migrazione compatibile dai recordset Classici consolidati\""
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

            return new ExportResult(legacy.seasonCount(), legacy.entryCount(), outputFile);
        } finally {
            Files.deleteIfExists(temporaryLegacy);
        }
    }

    public record ExportResult(int seasonCount, int entryCount, Path outputFile) {
    }
}
