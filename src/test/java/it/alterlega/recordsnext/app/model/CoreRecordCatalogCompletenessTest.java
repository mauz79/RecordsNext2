package it.alterlega.recordsnext.app.model;

import it.alterlega.recordsnext.app.config.ProcessingConfigWriter;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoreRecordCatalogCompletenessTest {

    @Test
    void catalogContainsEveryGranularProcessingId() {
        Set<String> configuredIds = new LinkedHashSet<>();

        Stream.of(
                ProcessingConfigWriter.CLASSICS,
                ProcessingConfigWriter.SERIES,
                ProcessingConfigWriter.RU,
                ProcessingConfigWriter.MODIFIERS,
                ProcessingConfigWriter.THRESHOLDS
        ).flatMap(Arrays::stream).forEach(configuredIds::add);

        Set<String> catalogIds = CoreRecordCatalog.children().stream()
                .map(RecordChild::id)
                .collect(java.util.stream.Collectors.toSet());

        Set<String> missing = new LinkedHashSet<>(configuredIds);
        missing.removeAll(catalogIds);

        assertTrue(
                missing.isEmpty(),
                "ID presenti in ProcessingConfigWriter ma assenti da CoreRecordCatalog: " + missing
        );
    }

    @Test
    void catalogContainsAllGranularRecordsPlusCulometroExactlyOnce() {
        Set<String> configuredIds = new LinkedHashSet<>();

        Stream.of(
                ProcessingConfigWriter.CLASSICS,
                ProcessingConfigWriter.SERIES,
                ProcessingConfigWriter.RU,
                ProcessingConfigWriter.MODIFIERS,
                ProcessingConfigWriter.THRESHOLDS
        ).flatMap(Arrays::stream).forEach(configuredIds::add);

        assertEquals(
                93,
                configuredIds.size(),
                "Il numero degli ID granulari configurabili è cambiato: aggiornare il contratto del catalogo"
        );

        Set<String> catalogIds = CoreRecordCatalog.children().stream()
                .map(RecordChild::id)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        assertEquals(
                CoreRecordCatalog.children().size(),
                catalogIds.size(),
                "CoreRecordCatalog contiene ID duplicati"
        );

        assertEquals(
                94,
                catalogIds.size(),
                "Il catalogo deve contenere i 93 record granulari più il Culometro"
        );

        assertTrue(
                catalogIds.contains(CoreRecordCatalog.CULOMETRO_ID),
                "Il Culometro deve essere presente nel catalogo"
        );
    }
}
