package it.alterlega.recordsnext.app.model;

import java.util.Objects;
import java.util.Set;

/**
 * Riga del piano di elaborazione di un singolo figlio.
 */
public record ExecutionPlanItem(
        RecordChild child,
        OutputStatus status,
        Set<String> missingRequired,
        Set<String> missingOptional
) {
    public ExecutionPlanItem {
        child = Objects.requireNonNull(child, "child");
        status = Objects.requireNonNull(status, "status");
        missingRequired = Set.copyOf(Objects.requireNonNullElse(missingRequired, Set.of()));
        missingOptional = Set.copyOf(Objects.requireNonNullElse(missingOptional, Set.of()));
    }

    public boolean selected() {
        return status != OutputStatus.SKIPPED_NOT_SELECTED;
    }

    public boolean executable() {
        return status == OutputStatus.GENERATED_COMPLETE
                || status == OutputStatus.GENERATED_PARTIAL;
    }
}
