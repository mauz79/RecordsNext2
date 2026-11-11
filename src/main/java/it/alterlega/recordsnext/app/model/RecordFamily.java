package it.alterlega.recordsnext.app.model;

/**
 * Famiglie elaborabili di RecordsNext 2.0.
 */
public enum RecordFamily {
    CLASSICS("classics"),
    SERIES("series"),
    RU("ru"),
    MODIFIERS("modifiers"),
    THRESHOLDS_LUCK("thresholdsLuck"),
    TOP_FLOP("topFlop");

    private final String id;

    RecordFamily(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }
}
