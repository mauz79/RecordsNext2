package it.alterlega.recordsnext.app.model;

/**
 * Stato finale di un figlio o di un output.
 */
public enum OutputStatus {
    GENERATED_COMPLETE,
    GENERATED_PARTIAL,
    SKIPPED_REQUIRED_DEPENDENCY,
    SKIPPED_NOT_SELECTED,
    SKIPPED_NO_DATA,
    FAILED
}
