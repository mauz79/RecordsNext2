package it.alterlega.recordsnext.app;

import it.alterlega.recordsnext.app.model.ProcessingSelection;
import it.alterlega.recordsnext.app.model.RecordFamily;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProcessingOptionsIntegrationTest {
    @Test
    void legacyConstructorBuildsEquivalentModularSelection() {
        ProcessingOptions options = new ProcessingOptions(true, false, true, false);

        assertTrue(options.classic());
        assertFalse(options.ru());
        assertTrue(options.familyEnabled(RecordFamily.CLASSICS));
        assertFalse(options.familyEnabled(RecordFamily.RU));
        assertFalse(options.culometroEnabled());
    }

    @Test
    void modularFactoryPreservesAllSelectedFamilies() {
        ProcessingSelection selection = new ProcessingSelection(
                Set.of(RecordFamily.CLASSICS, RecordFamily.SERIES),
                Set.of(),
                false,
                true,
                false
        );

        ProcessingOptions options = ProcessingOptions.modular(selection);

        assertTrue(options.classic());
        assertFalse(options.ru());
        assertTrue(options.familyEnabled(RecordFamily.SERIES));
    }

    @Test
    void pipelineRejectsFamiliesNotYetImplementedInsteadOfIgnoringThem() {
        ProcessingOptions options = ProcessingOptions.modular(
                new ProcessingSelection(
                        Set.of(RecordFamily.SERIES),
                        Set.of(),
                        false,
                        false,
                        false
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> RecordsNextPipeline.validateImplementedFamilies(options)
        );
    }

    @Test
    void pipelineAcceptsCurrentClassicAndRuBridge() {
        ProcessingOptions options = ProcessingOptions.modular(
                new ProcessingSelection(
                        Set.of(RecordFamily.CLASSICS, RecordFamily.RU),
                        Set.of(),
                        false,
                        true,
                        false
                )
        );

        RecordsNextPipeline.validateImplementedFamilies(options);
    }

    @Test
    void pipelineRejectsCulometroUntilDedicatedExecutorExists() {
        ProcessingOptions options = ProcessingOptions.modular(
                new ProcessingSelection(
                        Set.of(RecordFamily.THRESHOLDS_LUCK),
                        Set.of("easter-egg.culometro"),
                        true,
                        false,
                        false
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> RecordsNextPipeline.validateImplementedFamilies(options)
        );
    }
}
