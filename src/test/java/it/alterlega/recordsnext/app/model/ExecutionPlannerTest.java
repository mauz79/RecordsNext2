package it.alterlega.recordsnext.app.model;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExecutionPlannerTest {
    @Test
    void missingCaptainSkipsOnlyCaptainSeries() {
        ProcessingSelection selection = new ProcessingSelection(
                EnumSet.of(RecordFamily.CLASSICS, RecordFamily.SERIES),
                Set.of(),
                false,
                true,
                false
        );

        ExecutionPlan plan = ExecutionPlanner.plan(
                selection,
                DependencyInventory.legacyCapabilities(false, true, true, false)
        );

        ExecutionPlanItem captain = plan.items().stream()
                .filter(item -> item.child().id().equals("series.captain-bonus"))
                .findFirst()
                .orElseThrow();

        ExecutionPlanItem classic = plan.items().stream()
                .filter(item -> item.child().id().equals("classics.highest-match-score"))
                .findFirst()
                .orElseThrow();

        assertEquals(OutputStatus.SKIPPED_REQUIRED_DEPENDENCY, captain.status());
        assertEquals(OutputStatus.GENERATED_COMPLETE, classic.status());
    }

    @Test
    void culometroRemainsNotSelectedByDefault() {
        ProcessingSelection selection = new ProcessingSelection(
                EnumSet.of(RecordFamily.THRESHOLDS_LUCK),
                Set.of(),
                false,
                true,
                false
        );

        ExecutionPlan plan = ExecutionPlanner.plan(
                selection,
                DependencyInventory.legacyCapabilities(false, true, true, true)
        );

        ExecutionPlanItem culometro = plan.items().stream()
                .filter(item -> item.child().id().equals(CoreRecordCatalog.CULOMETRO_ID))
                .findFirst()
                .orElseThrow();

        assertEquals(OutputStatus.SKIPPED_NOT_SELECTED, culometro.status());
    }

    @Test
    void explicitCulometroCanBePlanned() {
        ProcessingSelection selection = new ProcessingSelection(
                EnumSet.of(RecordFamily.THRESHOLDS_LUCK),
                Set.of(CoreRecordCatalog.CULOMETRO_ID),
                true,
                true,
                false
        );

        ExecutionPlan plan = ExecutionPlanner.plan(
                selection,
                DependencyInventory.legacyCapabilities(false, true, true, true)
        );

        assertTrue(plan.executableItems().stream()
                .anyMatch(item -> item.child().id().equals(CoreRecordCatalog.CULOMETRO_ID)));
        assertFalse(plan.hasFailures());
    }

    @Test
    void planGroupsItemsByFamily() {
        ProcessingSelection selection = new ProcessingSelection(
                EnumSet.allOf(RecordFamily.class),
                Set.of(),
                false,
                true,
                false
        );

        ExecutionPlan plan = ExecutionPlanner.plan(
                selection,
                DependencyInventory.legacyCapabilities(false, true, false, false)
        );

        assertEquals(RecordFamily.values().length, plan.byFamily().size());
    }
}
