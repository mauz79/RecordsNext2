package it.alterlega.recordsnext.app.manifest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

public record ManifestMetadata(
        String program,
        String programVersion,
        String schemaVersion,
        OffsetDateTime generatedAt,
        String leagueId,
        String currentSeasonId,
        List<String> processedSeasons,
        List<String> generatedFiles
) {
    public ManifestMetadata {
        program = required(program, "program");
        programVersion = required(programVersion, "programVersion");
        schemaVersion = required(schemaVersion, "schemaVersion");
        generatedAt = Objects.requireNonNull(generatedAt, "generatedAt");
        leagueId = optional(leagueId);
        currentSeasonId = optional(currentSeasonId);
        processedSeasons = List.copyOf(Objects.requireNonNullElse(processedSeasons, List.of()));
        generatedFiles = List.copyOf(Objects.requireNonNullElse(generatedFiles, List.of()));
    }

    public static ManifestMetadata minimal(String programVersion) {
        return new ManifestMetadata(
                "RecordsNext by mauz79",
                programVersion,
                "2.0",
                OffsetDateTime.now(),
                "",
                "",
                List.of(),
                List.of()
        );
    }

    private static String required(String value, String field) {
        Objects.requireNonNull(value, field);
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(field + " non puo essere vuoto");
        }
        return normalized;
    }

    private static String optional(String value) {
        return value == null ? "" : value.trim();
    }
}
