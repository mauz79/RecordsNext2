package it.alterlega.recordsnext.app.model;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Selezione modulare richiesta dall'utente.
 */
public record ProcessingSelection(
        Set<RecordFamily> enabledFamilies,
        Set<String> enabledChildren,
        boolean culometroEnabled,
        boolean generateJs,
        boolean publish
) {
    public ProcessingSelection {
        enabledFamilies = Set.copyOf(new LinkedHashSet<>(
                Objects.requireNonNullElse(enabledFamilies, Set.of())
        ));
        Set<String> selectedChildren = enabledChildren == null ? Set.of() : enabledChildren;
        enabledChildren = selectedChildren.stream()
                .map(ProcessingSelection::normalizeChildId)
                .collect(Collectors.toUnmodifiableSet());

        if (publish && !generateJs) {
            throw new IllegalArgumentException(
                    "Publishing requires JavaScript generation"
            );
        }
    }

    public boolean isFamilyEnabled(RecordFamily family) {
        return enabledFamilies.contains(Objects.requireNonNull(family, "family"));
    }

    public boolean isChildSelected(RecordChild child) {
        Objects.requireNonNull(child, "child");
        if (!isFamilyEnabled(child.family())) {
            return false;
        }
        if (child.optInOnly()) {
            return enabledChildren.contains(child.id());
        }
        return enabledChildren.isEmpty() || enabledChildren.contains(child.id());
    }

    public OutputStatus selectionStatus(RecordChild child) {
        return isChildSelected(child)
                ? OutputStatus.GENERATED_COMPLETE
                : OutputStatus.SKIPPED_NOT_SELECTED;
    }

    private static String normalizeChildId(String value) {
        Objects.requireNonNull(value, "enabled child id");
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Enabled child id cannot be blank");
        }
        return normalized;
    }
}
