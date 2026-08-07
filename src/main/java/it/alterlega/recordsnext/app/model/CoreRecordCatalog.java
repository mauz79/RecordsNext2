package it.alterlega.recordsnext.app.model;

import java.util.List;
import java.util.Set;

/**
 * Primo catalogo minimo usato per validare il modello modulare.
 */
public final class CoreRecordCatalog {
    public static final String CULOMETRO_ID = "easter-egg.culometro";

    private CoreRecordCatalog() {
    }

    public static List<RecordChild> children() {
        return List.of(
                new RecordChild(
                        "classics.highest-match-score",
                        "Maggior punteggio in una partita",
                        RecordFamily.CLASSICS,
                        Set.of(
                                RecordDependency.required("data.matches", DependencyType.DATA),
                                RecordDependency.required("data.scores", DependencyType.DATA)
                        ),
                        false
                ),
                new RecordChild(
                        "ru.deciding",
                        "Riserve d'ufficio decisive",
                        RecordFamily.RU,
                        Set.of(
                                RecordDependency.required("ru.events", DependencyType.DATA),
                                RecordDependency.required("simulation.without-ru", DependencyType.SIMULATION)
                        ),
                        false
                ),
                new RecordChild(
                        "modifiers.home-field-deciding",
                        "Fattore Campo decisivo",
                        RecordFamily.MODIFIERS,
                        Set.of(
                                RecordDependency.required("modifier.home-field", DependencyType.MODULE),
                                RecordDependency.required("configuration.goal-bands", DependencyType.CONFIGURATION)
                        ),
                        false
                ),
                new RecordChild(
                        "thresholds.surgical-win",
                        "Vittoria chirurgica",
                        RecordFamily.THRESHOLDS_LUCK,
                        Set.of(
                                RecordDependency.required("data.scores", DependencyType.DATA),
                                RecordDependency.required("configuration.goal-bands", DependencyType.CONFIGURATION)
                        ),
                        false
                ),
                new RecordChild(
                        CULOMETRO_ID,
                        "Culometro",
                        RecordFamily.THRESHOLDS_LUCK,
                        Set.of(
                                RecordDependency.required("configuration.culometro", DependencyType.CONFIGURATION),
                                RecordDependency.optional("modifier.home-field", DependencyType.FAMILY_CHILD)
                        ),
                        true
                )
        );
    }
}
