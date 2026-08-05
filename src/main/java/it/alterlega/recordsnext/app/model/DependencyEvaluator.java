package it.alterlega.recordsnext.app.model;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Valuta le dipendenze senza bloccare l'intera famiglia.
 */
public final class DependencyEvaluator {
    private DependencyEvaluator() {
    }

    public static DependencyEvaluation evaluate(
            RecordChild child,
            ProcessingSelection selection,
            Set<String> availableDependencies
    ) {
        Objects.requireNonNull(child, "child");
        Objects.requireNonNull(selection, "selection");
        Set<String> available = Set.copyOf(
                Objects.requireNonNullElse(availableDependencies, Set.of())
        );

        if (!selection.isChildSelected(child)) {
            return new DependencyEvaluation(
                    OutputStatus.SKIPPED_NOT_SELECTED,
                    Set.of(),
                    Set.of()
            );
        }

        Set<String> missingRequired = new LinkedHashSet<>();
        Set<String> missingOptional = new LinkedHashSet<>();

        for (RecordDependency dependency : child.dependencies()) {
            if (available.contains(dependency.id())) {
                continue;
            }
            if (dependency.required()) {
                missingRequired.add(dependency.id());
            } else {
                missingOptional.add(dependency.id());
            }
        }

        if (!missingRequired.isEmpty()) {
            return new DependencyEvaluation(
                    OutputStatus.SKIPPED_REQUIRED_DEPENDENCY,
                    missingRequired,
                    missingOptional
            );
        }

        if (!missingOptional.isEmpty()) {
            return new DependencyEvaluation(
                    OutputStatus.GENERATED_PARTIAL,
                    Set.of(),
                    missingOptional
            );
        }

        return new DependencyEvaluation(
                OutputStatus.GENERATED_COMPLETE,
                Set.of(),
                Set.of()
        );
    }
}
