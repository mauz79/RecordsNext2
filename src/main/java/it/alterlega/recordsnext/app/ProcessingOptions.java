package it.alterlega.recordsnext.app;

import it.alterlega.recordsnext.app.model.ProcessingSelection;
import it.alterlega.recordsnext.app.model.RecordChild;
import it.alterlega.recordsnext.app.model.RecordFamily;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Opzioni di elaborazione compatibili con RecordsNext 1.0.2 e con il modello
 * modulare di RecordsNext 2.0.
 */
public record ProcessingOptions(
        boolean classic,
        boolean ru,
        boolean generateJs,
        boolean publish,
        ProcessingSelection selection
) {
    /**
     * Costruttore compatibile con la GUI e la pipeline RecordsNext 1.0.2.
     */
    public ProcessingOptions(
            boolean classic,
            boolean ru,
            boolean generateJs,
            boolean publish
    ) {
        this(
                classic,
                ru,
                generateJs,
                publish,
                legacySelection(classic, ru, generateJs, publish)
        );
    }

    public ProcessingOptions {
        selection = Objects.requireNonNull(selection, "selection");

        if (!classic && !ru && selection.enabledFamilies().isEmpty()) {
            throw new IllegalArgumentException("Selezionare almeno un'elaborazione");
        }
        if (publish && !generateJs) {
            throw new IllegalArgumentException(
                    "Per pubblicare nel sito occorre generare i file JavaScript"
            );
        }
        if (selection.generateJs() != generateJs) {
            throw new IllegalArgumentException(
                    "La selezione modulare e le opzioni legacy discordano su generateJs"
            );
        }
        if (selection.publish() != publish) {
            throw new IllegalArgumentException(
                    "La selezione modulare e le opzioni legacy discordano su publish"
            );
        }
        if (classic != selection.isFamilyEnabled(RecordFamily.CLASSICS)) {
            throw new IllegalArgumentException(
                    "La selezione modulare e le opzioni legacy discordano sui Classici"
            );
        }
        if (ru != selection.isFamilyEnabled(RecordFamily.RU)) {
            throw new IllegalArgumentException(
                    "La selezione modulare e le opzioni legacy discordano sulle RU"
            );
        }
    }

    /**
     * Crea opzioni 2.0 partendo dalla selezione modulare.
     */
    public static ProcessingOptions modular(ProcessingSelection selection) {
        Objects.requireNonNull(selection, "selection");
        return new ProcessingOptions(
                selection.isFamilyEnabled(RecordFamily.CLASSICS),
                selection.isFamilyEnabled(RecordFamily.RU),
                selection.generateJs(),
                selection.publish(),
                selection
        );
    }

    public boolean familyEnabled(RecordFamily family) {
        return selection.isFamilyEnabled(family);
    }

    public boolean childSelected(RecordChild child) {
        return selection.isChildSelected(child);
    }

    public boolean culometroEnabled() {
        return selection.culometroEnabled();
    }

    private static ProcessingSelection legacySelection(
            boolean classic,
            boolean ru,
            boolean generateJs,
            boolean publish
    ) {
        EnumSet<RecordFamily> families = EnumSet.noneOf(RecordFamily.class);
        if (classic) {
            families.add(RecordFamily.CLASSICS);
        }
        if (ru) {
            families.add(RecordFamily.RU);
        }
        return new ProcessingSelection(
                Set.copyOf(families),
                Set.of(),
                false,
                generateJs,
                publish
        );
    }
}
