package it.alterlega.recordsnext;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RecordsNextApplicationTest {

    @Test
    void usesJava21() {
        assertEquals(21, Runtime.version().feature());
    }
}