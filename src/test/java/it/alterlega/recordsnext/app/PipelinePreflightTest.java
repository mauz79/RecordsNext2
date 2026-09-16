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

        assertEquals(31, result.selectedCount());
        assertEquals(31, result.executableCount());
        assertEquals(31, result.completeCount());
        assertEquals(0, result.skippedDependencyCount());
    }

    @Test
    void legacyCaptainSeriesIsNotPartOfPreflightCatalog() {
        assertFalse(CoreRecordCatalog.children().stream()
                .anyMatch(item -> item.id().equals("series.captain-bonus")));
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

    @Test
    void selectedHomeFieldDecidingIsExecutable() {
        var selection = new ProcessingSelection(
                Set.of(RecordFamily.MODIFIERS),
                Set.of("modifiers.home-field-deciding"),
                false,
                true,
                false
        );

        var result = PipelinePreflight.evaluate(
                ProcessingOptions.modular(selection)
        );

        assertEquals(1, result.selectedCount());
        assertEquals(1, result.executableCount());
        assertEquals(1, result.completeCount());
        assertEquals(0, result.skippedDependencyCount());
    }

    @Test
    void nonHomeFieldModifierDoesNotExposeHomeFieldCapability() {
        var selection = new ProcessingSelection(
                Set.of(
                        RecordFamily.MODIFIERS,
                        RecordFamily.THRESHOLDS_LUCK
                ),
                Set.of(
                        "modifiers.modm1pers.max",
                        CoreRecordCatalog.CULOMETRO_ID
                ),
                true,
                true,
                false
        );

        var result = PipelinePreflight.evaluate(
                ProcessingOptions.modular(selection)
        );

        assertFalse(
                result.availableDependencies().contains("modifier.home-field")
        );

        assertEquals(2, result.selectedCount());
        assertEquals(2, result.executableCount());
        assertEquals(1, result.completeCount());
        assertEquals(1, result.partialCount());
        assertEquals(0, result.skippedDependencyCount());

        var culometro = result.relevantItems().stream()
                .filter(item -> item.child().id().equals(CoreRecordCatalog.CULOMETRO_ID))
                .findFirst()
                .orElseThrow();

        assertEquals(OutputStatus.GENERATED_PARTIAL, culometro.status());
        assertTrue(culometro.missingOptional().contains("modifier.home-field"));
    }
}
