package it.alterlega.recordsnext.app.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Piano completo delle elaborazioni richieste.
 */
public record ExecutionPlan(List<ExecutionPlanItem> items) {
    public ExecutionPlan {
        items = List.copyOf(Objects.requireNonNullElse(items, List.of()));
    }

    public List<ExecutionPlanItem> selectedItems() {
        return items.stream().filter(ExecutionPlanItem::selected).toList();
    }

    public List<ExecutionPlanItem> executableItems() {
        return items.stream().filter(ExecutionPlanItem::executable).toList();
    }

    public List<ExecutionPlanItem> skippedItems() {
        return items.stream().filter(item -> !item.executable()).toList();
    }

    public Map<RecordFamily, List<ExecutionPlanItem>> byFamily() {
        return items.stream().collect(Collectors.groupingBy(
                item -> item.child().family(),
                java.util.LinkedHashMap::new,
                Collectors.toList()
        ));
    }

    public boolean hasFailures() {
        return items.stream().anyMatch(item -> item.status() == OutputStatus.FAILED);
    }
}
