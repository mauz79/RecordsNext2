package it.alterlega.recordsnext.app.model;

import java.util.Objects;

/**
 * Dipendenza dichiarata da un figlio.
 */
public record RecordDependency(
        String id,
        DependencyType type,
        boolean required
) {
    public RecordDependency {
        id = normalizeId(id);
        type = Objects.requireNonNull(type, "type");
    }

    public static RecordDependency required(String id, DependencyType type) {
        return new RecordDependency(id, type, true);
    }

    public static RecordDependency optional(String id, DependencyType type) {
        return new RecordDependency(id, type, false);
    }

    private static String normalizeId(String value) {
        Objects.requireNonNull(value, "id");
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Dependency id cannot be blank");
        }
        return normalized;
    }
}
