package it.alterlega.recordsnext.app;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecordsNextPipelineSeasonScopeTest {

    @Test
    void seasonsUpToTargetExcludesFutureSeasons() {
        List<String> selected = List.of(
                "2026_2027",
                "2025_2026",
                "2024_2025",
                "2023_2024"
        );

        List<String> scoped = selected.stream()
                .filter(season -> season.compareTo("2024_2025") <= 0)
                .toList();

        assertEquals(
                List.of("2024_2025", "2023_2024"),
                scoped
        );
    }
}
