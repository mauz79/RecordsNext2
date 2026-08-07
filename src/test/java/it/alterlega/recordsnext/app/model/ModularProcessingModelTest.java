package it.alterlega.recordsnext.app.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ModularProcessingModelTest {

    @Test
    void legacyCaptainSeriesIsNotInCoreCatalog() {
        assertFalse(CoreRecordCatalog.children().stream()
                .anyMatch(item -> item.id().equals("series.captain-bonus")));
    }

    @Test
    void culometroIsNotSelectedAutomatically() {
        RecordChild child = CoreRecordCatalog.children().stream()
                .filter(item -> item.id().equals(CoreRecordCatalog.CULOMETRO_ID))
                .findFirst()
                .orElseThrow();

        ProcessingSelection selection = new ProcessingSelection(
                EnumSet.of(RecordFamily.THRESHOLDS_LUCK),
                Set.of(),
                false,
                true,
                false
        );

        assertFalse(selection.isChildSelected(child));
        assertEquals(OutputStatus.SKIPPED_NOT_SELECTED, selection.selectionStatus(child));
    }

    @Test
    void culometroCanBeExplicitlySelected() {
        RecordChild child = CoreRecordCatalog.children().stream()
                .filter(item -> item.id().equals(CoreRecordCatalog.CULOMETRO_ID))
                .findFirst()
                .orElseThrow();

        ProcessingSelection selection = new ProcessingSelection(
                EnumSet.of(RecordFamily.THRESHOLDS_LUCK),
                Set.of(CoreRecordCatalog.CULOMETRO_ID),
                true,
                true,
                false
        );

        DependencyEvaluation result = DependencyEvaluator.evaluate(
                child,
                selection,
                Set.of("configuration.culometro")
        );

        assertTrue(selection.isChildSelected(child));
        assertEquals(OutputStatus.GENERATED_PARTIAL, result.status());
        assertEquals(Set.of("modifier.home-field"), result.missingOptional());
    }

    @Test
    void publishingRequiresJavascriptGeneration() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ProcessingSelection(
                        EnumSet.of(RecordFamily.CLASSICS),
                        Set.of(),
                        false,
                        false,
                        true
                )
        );
    }
}
