package it.alterlega.recordsnext.app;

import it.alterlega.recordsnext.app.model.ProcessingSelection;
import it.alterlega.recordsnext.app.model.RecordFamily;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProcessingOptionsIntegrationTest {
    @Test void legacyConstructorBuildsEquivalentModularSelection(){ProcessingOptions o=new ProcessingOptions(true,false,true,false);assertTrue(o.classic());assertFalse(o.ru());assertTrue(o.familyEnabled(RecordFamily.CLASSICS));assertFalse(o.culometroEnabled());}
    @Test void modularFactoryPreservesAllSelectedFamilies(){ProcessingOptions o=ProcessingOptions.modular(new ProcessingSelection(Set.of(RecordFamily.CLASSICS,RecordFamily.THRESHOLDS_LUCK),Set.of(),false,true,false));assertTrue(o.familyEnabled(RecordFamily.THRESHOLDS_LUCK));}
    @Test void pipelineAcceptsAllFiveImplementedFamilies(){ProcessingOptions o=ProcessingOptions.modular(new ProcessingSelection(Set.of(RecordFamily.CLASSICS,RecordFamily.RU,RecordFamily.SERIES,RecordFamily.MODIFIERS,RecordFamily.THRESHOLDS_LUCK),Set.of(),false,true,false));RecordsNextPipeline.validateImplementedFamilies(o);}
    @Test void pipelineAcceptsConfiguredCulometro(){ProcessingOptions o=ProcessingOptions.modular(new ProcessingSelection(Set.of(RecordFamily.THRESHOLDS_LUCK),Set.of("easter-egg.culometro"),true,true,false));RecordsNextPipeline.validateImplementedFamilies(o);assertTrue(o.culometroEnabled());}
    @Test void thresholdsFamilyDoesNotImplicitlyEnableCulometro(){ProcessingOptions o=ProcessingOptions.modular(new ProcessingSelection(Set.of(RecordFamily.THRESHOLDS_LUCK),Set.of(),false,true,false));RecordsNextPipeline.validateImplementedFamilies(o);assertFalse(o.culometroEnabled());}
}
