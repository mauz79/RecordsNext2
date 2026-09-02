package it.alterlega.recordsnext.app;

import it.alterlega.recordsnext.app.output.SeasonPublicationTargetRepository;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecordsNextPipelinePublicationModeTest {

    private static SeasonPublicationTargetRepository.Target target(String season) {
        Path root = Path.of("C:/test/" + season);
        return new SeasonPublicationTargetRepository.Target(
                season,
                Integer.parseInt(season.substring(0, 4)),
                root,
                root.resolve("js"),
                true
        );
    }

    @Test
    void currentSiteModeSelectsOnlyCurrentSeasonTarget() {
        List<SeasonPublicationTargetRepository.Target> targets = List.of(
                target("2024_2025"),
                target("2025_2026"),
                target("2026_2027")
        );

        var selected = RecordsNextPipeline.selectPublicationTargets(
                targets,
                "2025_2026",
                RecordsNextPipeline.PublicationMode.CURRENT_SITE
        );

        assertEquals(
                List.of("2025_2026"),
                selected.stream()
                        .map(SeasonPublicationTargetRepository.Target::seasonId)
                        .toList()
        );
    }

    @Test
    void allSitesModeSelectsEveryConfiguredTarget() {
        List<SeasonPublicationTargetRepository.Target> targets = List.of(
                target("2024_2025"),
                target("2025_2026"),
                target("2026_2027")
        );

        var selected = RecordsNextPipeline.selectPublicationTargets(
                targets,
                "2026_2027",
                RecordsNextPipeline.PublicationMode.ALL_CONFIGURED_SITES
        );

        assertEquals(3, selected.size());
    }
}
