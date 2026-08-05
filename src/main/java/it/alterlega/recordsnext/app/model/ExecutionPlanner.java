package it.alterlega.recordsnext.app.model;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Costruisce il piano senza eseguire alcun elaboratore.
 */
public final class ExecutionPlanner {
    private ExecutionPlanner() {
    }

    public static ExecutionPlan plan(
            ProcessingSelection selection,
            Set<String> availableDependencies
    ) {
        return plan(CoreRecordCatalog.children(), selection, availableDependencies);
    }

    public static ExecutionPlan plan(
            List<RecordChild> catalog,
            ProcessingSelection selection,
            Set<String> availableDependencies
    ) {
        Objects.requireNonNull(catalog, "catalog");
        Objects.requireNonNull(selection, "selection");

        List<ExecutionPlanItem> items = catalog.stream()
                .map(child -> toItem(
                        child,
                        DependencyEvaluator.evaluate(
                                child,
                                selection,
                                availableDependencies
                        )
                ))
                .toList();

        return new ExecutionPlan(items);
    }

    private static ExecutionPlanItem toItem(
            RecordChild child,
            DependencyEvaluation evaluation
    ) {
        return new ExecutionPlanItem(
                child,
                evaluation.status(),
                evaluation.missingRequired(),
                evaluation.missingOptional()
        );
    }
}
