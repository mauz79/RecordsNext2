package it.alterlega.recordsnext.app;

public record ProcessingOptions(boolean classic, boolean ru, boolean generateJs, boolean publish) {
    public ProcessingOptions {
        if (!classic && !ru) {
            throw new IllegalArgumentException("Selezionare almeno un'elaborazione");
        }
        if (publish && !generateJs) {
            throw new IllegalArgumentException("Per pubblicare nel sito occorre generare i file JavaScript");
        }
    }
}
