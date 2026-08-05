package it.alterlega.recordsnext.app.model;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Esito della valutazione delle dipendenze di un figlio.
 */
public record DependencyEvaluation(
        OutputStatus status,
        Set<String> missingRequired,
        Set<String> missingOptional
) {
    public DependencyEvaluation {
        status = Objects.requireNonNull(status, "status");
        missingRequired = Set.copyOf(new LinkedHashSet<>(
                Objects.requireNonNullElse(missingRequired, Set.of())
        ));
        missingOptional = Set.copyOf(new LinkedHashSet<>(
                Objects.requireNonNullElse(missingOptional, Set.of())
        ));
    }

    public boolean canGenerate() {
        return status == OutputStatus.GENERATED_COMPLETE
                || status == OutputStatus.GENERATED_PARTIAL;
    }
}
