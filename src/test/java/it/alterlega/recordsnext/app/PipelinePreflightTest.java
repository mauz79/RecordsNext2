package it.alterlega.recordsnext.app;

import it.alterlega.recordsnext.app.model.CoreRecordCatalog;
import it.alterlega.recordsnext.app.model.OutputStatus;
import it.alterlega.recordsnext.app.model.ProcessingSelection;
import it.alterlega.recordsnext.app.model.RecordFamily;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PipelinePreflightTest {
    @Test
    void legacyClassicsAndRuAreExecutable() {
        var result = PipelinePreflight.evaluate(
                new ProcessingOptions(true, true, true, false)
        );

        assertEquals(2, result.selectedCount());
        assertEquals(2, result.executableCount());
        assertEquals(2, result.completeCount());
        assertEquals(0, result.skippedDependencyCount());
    }

    @Test
    void captainSeriesIsSkippedWithoutCaptainDependency() {
        var selection = new ProcessingSelection(
                Set.of(RecordFamily.SERIES),
                Set.of("series.captain-bonus"),
                false,
                true,
                false
        );

        var result = PipelinePreflight.evaluate(
                ProcessingOptions.modular(selection)
        );

        assertEquals(1, result.selectedCount());
        assertEquals(0, result.executableCount());
        assertEquals(OutputStatus.SKIPPED_REQUIRED_DEPENDENCY,
                result.relevantItems().getFirst().status());
        assertTrue(result.relevantItems().getFirst()
                .missingRequired().contains("modifier.captain"));
    }

    @Test
    void culometroRemainsOptIn() {
        var ordinary = new ProcessingSelection(
                Set.of(RecordFamily.THRESHOLDS_LUCK),
                Set.of(),
                false,
                true,
                false
        );
        var ordinaryResult = PipelinePreflight.evaluate(
                ProcessingOptions.modular(ordinary)
        );

        assertFalse(ordinaryResult.relevantItems().stream()
                .anyMatch(item -> item.child().id().equals(CoreRecordCatalog.CULOMETRO_ID)));

        var easterEgg = new ProcessingSelection(
                Set.of(RecordFamily.THRESHOLDS_LUCK),
                Set.of(CoreRecordCatalog.CULOMETRO_ID),
                true,
                true,
                false
        );
        var easterEggResult = PipelinePreflight.evaluate(
                ProcessingOptions.modular(easterEgg)
        );

        assertTrue(easterEggResult.relevantItems().stream()
                .anyMatch(item -> item.child().id().equals(CoreRecordCatalog.CULOMETRO_ID)));
    }
}
