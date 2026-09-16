package it.alterlega.recordsnext.app.model;

import it.alterlega.recordsnext.app.config.ProcessingConfigWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Catalogo canonico dei record elaborabili.
 *
 * Deve restare allineato 1:1 con gli ID granulari esposti da
 * ProcessingConfigWriter, più gli eventuali record opt-in che non appartengono
 * alle checkbox granulari delle famiglie (attualmente il Culometro).
 */
public final class CoreRecordCatalog {
    public static final String CULOMETRO_ID = "easter-egg.culometro";

    private CoreRecordCatalog() {
    }

    public static List<RecordChild> children() {
        List<RecordChild> children = new ArrayList<>();

        addFamilyChildren(
                children,
                ProcessingConfigWriter.CLASSICS,
                RecordFamily.CLASSICS
        );

        addFamilyChildren(
                children,
                ProcessingConfigWriter.SERIES,
                RecordFamily.SERIES
        );

        addFamilyChildren(
                children,
                ProcessingConfigWriter.RU,
                RecordFamily.RU
        );

        addFamilyChildren(
                children,
                ProcessingConfigWriter.MODIFIERS,
                RecordFamily.MODIFIERS
        );

        addFamilyChildren(
                children,
                ProcessingConfigWriter.THRESHOLDS,
                RecordFamily.THRESHOLDS_LUCK
        );

        replace(
                children,
                new RecordChild(
                        "classics.highest-match-score",
                        "Maggior punteggio in una partita",
                        RecordFamily.CLASSICS,
                        Set.of(
                                RecordDependency.required("data.matches", DependencyType.DATA),
                                RecordDependency.required("data.scores", DependencyType.DATA)
                        ),
                        false
                )
        );

        replace(
                children,
                new RecordChild(
                        "ru.deciding",
                        "Riserve d'ufficio decisive",
                        RecordFamily.RU,
                        Set.of(
                                RecordDependency.required("ru.events", DependencyType.DATA),
                                RecordDependency.required("simulation.without-ru", DependencyType.SIMULATION)
                        ),
                        false
                )
        );

        replace(
                children,
                new RecordChild(
                        "modifiers.home-field-deciding",
                        "Fattore Campo decisivo",
                        RecordFamily.MODIFIERS,
                        Set.of(
                                RecordDependency.required("modifier.home-field", DependencyType.MODULE),
                                RecordDependency.required("configuration.goal-bands", DependencyType.CONFIGURATION)
                        ),
                        false
                )
        );

        replace(
                children,
                new RecordChild(
                        "thresholds.surgical-win",
                        "Vittoria chirurgica",
                        RecordFamily.THRESHOLDS_LUCK,
                        Set.of(
                                RecordDependency.required("data.scores", DependencyType.DATA),
                                RecordDependency.required("configuration.goal-bands", DependencyType.CONFIGURATION)
                        ),
                        false
                )
        );

        children.add(
                new RecordChild(
                        CULOMETRO_ID,
                        "Culometro",
                        RecordFamily.THRESHOLDS_LUCK,
                        Set.of(
                                RecordDependency.required(
                                        "configuration.culometro",
                                        DependencyType.CONFIGURATION
                                ),
                                RecordDependency.optional(
                                        "modifier.home-field",
                                        DependencyType.FAMILY_CHILD
                                )
                        ),
                        true
                )
        );

        return List.copyOf(children);
    }

    private static void addFamilyChildren(
            List<RecordChild> target,
            String[] ids,
            RecordFamily family
    ) {
        for (String id : ids) {
            target.add(
                    new RecordChild(
                            id,
                            id,
                            family,
                            Set.of(),
                            false
                    )
            );
        }
    }

    private static void replace(
            List<RecordChild> children,
            RecordChild replacement
    ) {
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).id().equals(replacement.id())) {
                children.set(i, replacement);
                return;
            }
        }

        throw new IllegalStateException(
                "Record da specializzare assente dal catalogo base: "
                        + replacement.id()
        );
    }
}