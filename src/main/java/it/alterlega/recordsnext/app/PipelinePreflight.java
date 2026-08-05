package it.alterlega.recordsnext.app;

import it.alterlega.recordsnext.app.model.DependencyInventory;
import it.alterlega.recordsnext.app.model.ExecutionPlan;
import it.alterlega.recordsnext.app.model.ExecutionPlanItem;
import it.alterlega.recordsnext.app.model.ExecutionPlanner;
import it.alterlega.recordsnext.app.model.OutputStatus;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Costruisce e riassume il piano prima dell'esecuzione della pipeline.
 */
public final class PipelinePreflight {
    private PipelinePreflight() {
    }

    public static Result evaluate(ProcessingOptions options) {
        Objects.requireNonNull(options, "options");

        Set<String> availableDependencies = DependencyInventory.legacyCapabilities(
                false,
                false,
                options.familyEnabled(it.alterlega.recordsnext.app.model.RecordFamily.RU),
                options.culometroEnabled()
        );

        ExecutionPlan plan = ExecutionPlanner.plan(
                options.selection(),
                availableDependencies
        );

        return new Result(plan, availableDependencies);
    }

    public record Result(
            ExecutionPlan plan,
            Set<String> availableDependencies
    ) {
        public Result {
            plan = Objects.requireNonNull(plan, "plan");
            availableDependencies = Set.copyOf(
                    Objects.requireNonNullElse(availableDependencies, Set.of())
            );
        }

        public int selectedCount() {
            return plan.selectedItems().size();
        }

        public int executableCount() {
            return plan.executableItems().size();
        }

        public int completeCount() {
            return count(OutputStatus.GENERATED_COMPLETE);
        }

        public int partialCount() {
            return count(OutputStatus.GENERATED_PARTIAL);
        }

        public int skippedDependencyCount() {
            return count(OutputStatus.SKIPPED_REQUIRED_DEPENDENCY);
        }

        public List<ExecutionPlanItem> relevantItems() {
            return plan.selectedItems();
        }

        public List<String> messages() {
            return relevantItems().stream()
                    .map(PipelinePreflight.Result::message)
                    .toList();
        }

        public String summary() {
            return "Preflight: selezionati=" + selectedCount()
                    + ", eseguibili=" + executableCount()
                    + ", completi=" + completeCount()
                    + ", parziali=" + partialCount()
                    + ", saltati per dipendenze=" + skippedDependencyCount();
        }

        private int count(OutputStatus status) {
            return (int) plan.items().stream()
                    .filter(item -> item.status() == status)
                    .count();
        }

        private static String message(ExecutionPlanItem item) {
            StringBuilder value = new StringBuilder()
                    .append(item.child().id())
                    .append(" -> ")
                    .append(item.status());

            if (!item.missingRequired().isEmpty()) {
                value.append("; richieste mancanti=")
                        .append(item.missingRequired());
            }
            if (!item.missingOptional().isEmpty()) {
                value.append("; opzionali mancanti=")
                        .append(item.missingOptional());
            }
            return value.toString();
        }
    }
}
