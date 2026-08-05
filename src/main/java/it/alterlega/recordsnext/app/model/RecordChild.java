package it.alterlega.recordsnext.app.model;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Definizione di un singolo figlio elaborabile.
 */
public record RecordChild(
        String id,
        String displayName,
        RecordFamily family,
        Set<RecordDependency> dependencies,
        boolean optInOnly
) {
    public RecordChild {
        id = normalize(id, "id");
        displayName = normalize(displayName, "displayName");
        family = Objects.requireNonNull(family, "family");
        dependencies = Set.copyOf(new LinkedHashSet<>(
                Objects.requireNonNullElse(dependencies, Set.of())
        ));
    }

    public Set<RecordDependency> requiredDependencies() {
        return dependencies.stream()
                .filter(RecordDependency::required)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    public Set<RecordDependency> optionalDependencies() {
        return dependencies.stream()
                .filter(dependency -> !dependency.required())
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    private static String normalize(String value, String field) {
        Objects.requireNonNull(value, field);
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(field + " cannot be blank");
        }
        return normalized;
    }
}
