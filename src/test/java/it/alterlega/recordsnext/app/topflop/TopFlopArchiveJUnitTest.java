package it.alterlega.recordsnext.app.topflop;

import org.junit.jupiter.api.Test;

/** Makes the executable contract test part of the existing Maven/JUnit 5 suite. */
class TopFlopArchiveJUnitTest {
    @Test
    void canonicalFactsAndIndividualMetrics() {
        TopFlopArchiveSmokeTest.main(new String[0]);
    }
}
