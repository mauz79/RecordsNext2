package it.alterlega.recordsnext.app.manifest;

import it.alterlega.recordsnext.app.PipelinePreflight;
import it.alterlega.recordsnext.app.ProcessingOptions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class ManifestPublishingSupport {
    private ManifestPublishingSupport() {
    }

    public static Path write(
            Path generatedDirectory,
            ProcessingOptions options,
            PipelinePreflight.Result preflight,
            ManifestMetadata metadata
    ) throws IOException {
        Objects.requireNonNull(generatedDirectory, "generatedDirectory");
        Objects.requireNonNull(options, "options");
        Objects.requireNonNull(preflight, "preflight");
        Objects.requireNonNull(metadata, "metadata");

        List<String> generatedFiles = new ArrayList<>();
        if (Files.isDirectory(generatedDirectory)) {
            try (var stream = Files.list(generatedDirectory)) {
                stream.filter(Files::isRegularFile)
                        .map(path -> path.getFileName().toString())
                        .sorted()
                        .forEach(generatedFiles::add);
            }
        }
        generatedFiles.add(ManifestJsWriter.FILE_NAME);
        generatedFiles = generatedFiles.stream().distinct().sorted().toList();

        ManifestMetadata effectiveMetadata = new ManifestMetadata(
                metadata.program(),
                metadata.programVersion(),
                metadata.schemaVersion(),
                metadata.generatedAt(),
                metadata.leagueId(),
                metadata.currentSeasonId(),
                metadata.processedSeasons(),
                generatedFiles
        );

        return ManifestJsWriter.write(
                generatedDirectory,
                options,
                preflight,
                effectiveMetadata
        );
    }
}
