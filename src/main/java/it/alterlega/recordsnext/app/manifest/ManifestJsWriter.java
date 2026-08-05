package it.alterlega.recordsnext.app.manifest;

import it.alterlega.recordsnext.app.PipelinePreflight;
import it.alterlega.recordsnext.app.ProcessingOptions;
import it.alterlega.recordsnext.app.model.ExecutionPlanItem;
import it.alterlega.recordsnext.app.model.RecordFamily;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class ManifestJsWriter {
    public static final String FILE_NAME = "fcmRecordsNext_Manifest.js";

    private ManifestJsWriter() {
    }

    public static Path write(
            Path outputDirectory,
            ProcessingOptions options,
            PipelinePreflight.Result preflight,
            ManifestMetadata metadata
    ) throws IOException {
        Objects.requireNonNull(outputDirectory, "outputDirectory");
        Objects.requireNonNull(options, "options");
        Objects.requireNonNull(preflight, "preflight");
        Objects.requireNonNull(metadata, "metadata");

        Files.createDirectories(outputDirectory);
        Path outputFile = outputDirectory.resolve(FILE_NAME);
        Files.writeString(outputFile, render(options, preflight, metadata), StandardCharsets.UTF_8);
        return outputFile;
    }

    public static String render(
            ProcessingOptions options,
            PipelinePreflight.Result preflight,
            ManifestMetadata metadata
    ) {
        Objects.requireNonNull(options, "options");
        Objects.requireNonNull(preflight, "preflight");
        Objects.requireNonNull(metadata, "metadata");

        List<String> requestedFamilies = options.selection().enabledFamilies().stream()
                .sorted(Comparator.comparing(RecordFamily::id))
                .map(RecordFamily::id)
                .toList();

        List<String> generatedFamilies = preflight.plan().executableItems().stream()
                .map(item -> item.child().family())
                .distinct()
                .sorted(Comparator.comparing(RecordFamily::id))
                .map(RecordFamily::id)
                .toList();

        List<ExecutionPlanItem> selected = preflight.plan().selectedItems().stream()
                .sorted(Comparator.comparing(item -> item.child().id()))
                .toList();

        List<String> generatedChildren = selected.stream()
                .filter(ExecutionPlanItem::executable)
                .map(item -> item.child().id())
                .toList();

        List<ExecutionPlanItem> skippedChildren = selected.stream()
                .filter(item -> !item.executable())
                .toList();

        StringBuilder out = new StringBuilder();
        out.append("window.fcmRecordsNextManifest = {\n");
        property(out, 1, "program", metadata.program(), true);
        property(out, 1, "programVersion", metadata.programVersion(), true);
        property(out, 1, "schemaVersion", metadata.schemaVersion(), true);
        property(out, 1, "generatedAt", metadata.generatedAt().toString(), true);
        property(out, 1, "leagueId", metadata.leagueId(), true);
        property(out, 1, "currentSeasonId", metadata.currentSeasonId(), true);
        stringArray(out, 1, "processedSeasons", metadata.processedSeasons(), true);
        stringArray(out, 1, "requestedFamilies", requestedFamilies, true);
        stringArray(out, 1, "generatedFamilies", generatedFamilies, true);
        stringArray(out, 1, "generatedChildren", generatedChildren, true);
        skippedArray(out, 1, skippedChildren, true);
        stringArray(out, 1, "generatedFiles", metadata.generatedFiles(), true);
        out.append("  culometroGenerated: ")
                .append(options.culometroEnabled() && generatedChildren.contains("easter-egg.culometro"))
                .append(",\n");
        out.append("  preflight: {\n");
        numberProperty(out, 2, "selected", preflight.selectedCount(), true);
        numberProperty(out, 2, "executable", preflight.executableCount(), true);
        numberProperty(out, 2, "complete", preflight.completeCount(), true);
        numberProperty(out, 2, "partial", preflight.partialCount(), true);
        numberProperty(out, 2, "skippedRequiredDependency", preflight.skippedDependencyCount(), false);
        out.append("  }\n");
        out.append("};\n");
        return out.toString();
    }

    private static void skippedArray(
            StringBuilder out,
            int indent,
            List<ExecutionPlanItem> items,
            boolean comma
    ) {
        pad(out, indent).append("skippedChildren: [");
        if (!items.isEmpty()) {
            out.append("\n");
            for (int i = 0; i < items.size(); i++) {
                ExecutionPlanItem item = items.get(i);
                pad(out, indent + 1).append("{\n");
                property(out, indent + 2, "id", item.child().id(), true);
                property(out, indent + 2, "status", item.status().name(), true);
                stringArray(out, indent + 2, "missingRequired", sorted(item.missingRequired()), true);
                stringArray(out, indent + 2, "missingOptional", sorted(item.missingOptional()), false);
                pad(out, indent + 1).append("}");
                if (i < items.size() - 1) out.append(",");
                out.append("\n");
            }
            pad(out, indent);
        }
        out.append("]");
        if (comma) out.append(",");
        out.append("\n");
    }

    private static List<String> sorted(Iterable<String> values) {
        List<String> result = new ArrayList<>();
        for (String value : values) result.add(value);
        result.sort(String::compareTo);
        return result;
    }

    private static void property(StringBuilder out, int indent, String name, String value, boolean comma) {
        pad(out, indent).append(name).append(": \"").append(escape(value)).append("\"");
        if (comma) out.append(",");
        out.append("\n");
    }

    private static void numberProperty(StringBuilder out, int indent, String name, int value, boolean comma) {
        pad(out, indent).append(name).append(": ").append(value);
        if (comma) out.append(",");
        out.append("\n");
    }

    private static void stringArray(
            StringBuilder out,
            int indent,
            String name,
            List<String> values,
            boolean comma
    ) {
        pad(out, indent).append(name).append(": [");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) out.append(", ");
            out.append("\"").append(escape(values.get(i))).append("\"");
        }
        out.append("]");
        if (comma) out.append(",");
        out.append("\n");
    }

    private static StringBuilder pad(StringBuilder out, int indent) {
        return out.append("  ".repeat(Math.max(0, indent)));
    }

    private static String escape(String value) {
        String safe = value == null ? "" : value;
        return safe
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}
