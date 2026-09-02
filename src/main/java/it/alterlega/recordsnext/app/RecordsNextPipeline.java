package it.alterlega.recordsnext.app;

import it.alterlega.recordsnext.Records2026SitePublisher;
import it.alterlega.recordsnext.RiserveUfficioArchiveBuilder;
import it.alterlega.recordsnext.SeasonRecordsArchiveBuilder;
import it.alterlega.recordsnext.app.manifest.ManifestMetadata;
import it.alterlega.recordsnext.app.core.LeagueMetadata;
import it.alterlega.recordsnext.app.core.LeagueMetadataLoader;
import it.alterlega.recordsnext.app.model.RecordFamily;
import it.alterlega.recordsnext.app.core.CoreJsExporter;
import it.alterlega.recordsnext.app.output.SeasonPublicationTargetRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Comparator;
import java.util.Set;

public final class RecordsNextPipeline {
    private static final Set<RecordFamily> IMPLEMENTED_FAMILIES = Set.copyOf(
            EnumSet.of(
                    RecordFamily.CLASSICS,
                    RecordFamily.SERIES,
                    RecordFamily.RU,
                    RecordFamily.MODIFIERS,
                    RecordFamily.THRESHOLDS_LUCK
            )
    );

    public interface Listener {
        void phase(String text, int percent);

        default void timing(String text) {
            phase("TEMPO " + text, -1);
        }
    }

    public record Result(
            int classicEntries,
            int ruSeasons,
            int files,
            int published
    ) {
    }

    public enum PublicationMode {
        CURRENT_SITE,
        ALL_CONFIGURED_SITES
    }

    public Result run(
            PipelineConfig c,
            ProcessingOptions o,
            ProcessingMode mode,
            Listener l
    ) throws Exception {
        return run(c, o, mode, l, PublicationMode.CURRENT_SITE);
    }

    public Result run(
            PipelineConfig c,
            ProcessingOptions o,
            ProcessingMode mode,
            Listener l,
            PublicationMode publicationMode
    ) throws Exception {
        PipelinePreflight.Result preflight = preflight(o);

        l.phase(preflight.summary(), 2);

        for (String message : preflight.messages()) {
            l.phase("PREFLIGHT " + message, -1);
        }

        validateImplementedFamilies(o);

        long totalStarted = System.nanoTime();

        Path database = c.projectRoot()
                .resolve("data/database/recordsnext.db")
                .normalize();

        RecordsNextPreparationService preparation =
                new RecordsNextPreparationService(
                        c.projectRoot(),
                        database
                );

        long preparationStarted = System.nanoTime();

        List<String> changedSeasons =
                preparation.prepare(
                        mode,
                        c.seasons(),
                        l
                );

        l.timing(
                "preparazione complessiva: "
                        + elapsed(preparationStarted)
        );

        /*
         * Una stagione puo' risultare modificata/elaborata senza produrre
         * alcun season_normalized_*.json.
         *
         * Caso reale:
         * 2026_2027 e' correttamente la stagione corrente, ma prima della
         * prima partita non esiste ancora alcuna competizione testa-a-testa
         * elaborabile.
         *
         * In modalita' CONSOLIDATED questo NON deve essere considerato
         * un errore e gli archivi storici gia' consolidati non devono
         * essere ricostruiti.
         */
        List<String> reportSeasons = new ArrayList<>();

        for (String season : changedSeasons) {
            Path seasonReports = c.reports()
                    .resolve(season)
                    .normalize();

            if (!Files.isDirectory(seasonReports)) {
                continue;
            }

            try (var files = Files.list(seasonReports)) {
                boolean hasNormalized = files.anyMatch(path -> {
                    if (!Files.isRegularFile(path)) {
                        return false;
                    }

                    String name = path.getFileName().toString();

                    return name.startsWith("season_normalized_")
                            && name.endsWith(".json");
                });

                if (hasNormalized) {
                    reportSeasons.add(season);
                }
            }
        }

        if (mode == ProcessingMode.FULL) {
            l.phase(
                    "Pulizia archivi derivati delle stagioni gestite",
                    52
            );

            for (String season : changedSeasons) {
                deleteTree(
                        c.classicArchive().resolve(season)
                );

                deleteTree(
                        c.ruArchive().resolve(season)
                );
            }
        }

        if (o.familyEnabled(RecordFamily.CLASSICS)) {
            if (reportSeasons.isEmpty()) {
                l.phase(
                        "Record classici invariati: nessun nuovo report normalizzato",
                        55
                );
            } else {
                l.phase(
                        "Generazione record classici",
                        55
                );

                long started = System.nanoTime();

                SeasonRecordsArchiveBuilder.build(
                        c.reports(),
                        c.classicArchive(),
                        reportSeasons
                );

                l.timing(
                        "record classici: "
                                + elapsed(started)
                );
            }
        }

        if (o.familyEnabled(RecordFamily.RU)) {
            if (reportSeasons.isEmpty()) {
                l.phase(
                        "Riserve d'ufficio invariate: nessun nuovo report normalizzato",
                        68
                );
            } else {
                l.phase(
                        "Generazione riserve d'ufficio",
                        68
                );

                long started = System.nanoTime();

                RiserveUfficioArchiveBuilder.build(
                        c.reports(),
                        c.ruArchive(),
                        reportSeasons
                );

                l.timing(
                        "riserve d'ufficio: "
                                + elapsed(started)
                );
            }
        }

        Result result;

        if (!o.generateJs()) {
            l.phase(
                    "Archivi elaborati; generazione JavaScript non richiesta",
                    96
            );

            result = new Result(
                    0,
                    0,
                    0,
                    0
            );
        } else {
            l.phase(
                    o.publish()
                            ? "Generazione e pubblicazione JavaScript"
                            : "Generazione JavaScript",
                    82
            );

            long started = System.nanoTime();

            LeagueMetadata leagueMetadata =
                    LeagueMetadataLoader.load(
                            c.projectRoot()
                                    .resolve("config/league.json")
                    );

            if (!o.publish()) {
                ManifestMetadata manifestMetadata =
                        new ManifestMetadata(
                                "RecordsNext by mauz79",
                                "3.1.0",
                                "2.0",
                                OffsetDateTime.now(),
                                leagueMetadata.leagueId(),
                                leagueMetadata.currentSeasonId(),
                                c.seasons(),
                                List.of()
                        );

                var r = Records2026SitePublisher.run(
                        c.classicArchive(),
                        c.ruArchive(),
                        c.staging(),
                        c.siteJs(),
                        true,
                        o.familyEnabled(RecordFamily.CLASSICS),
                        o.familyEnabled(RecordFamily.RU),
                        o,
                        preflight,
                        manifestMetadata,
                        database,
                        leagueMetadata,
                        c.reports()
                );

                result = new Result(
                        r.classicEntries(),
                        r.ruSeasons(),
                        r.validatedFiles(),
                        0
                );
            } else {
                SeasonPublicationTargetRepository targetRepository =
                        new SeasonPublicationTargetRepository(database);

                List<SeasonPublicationTargetRepository.Target> targets =
                        targetRepository.load(c.seasons());

                List<SeasonPublicationTargetRepository.Target> selectedTargets =
                        selectPublicationTargets(
                                targets,
                                leagueMetadata.currentSeasonId(),
                                publicationMode
                        );

                List<SeasonPublicationTargetRepository.Target> availableTargets =
                        selectedTargets.stream()
                                .filter(SeasonPublicationTargetRepository.Target::available)
                                .toList();

                for (SeasonPublicationTargetRepository.Target target : selectedTargets) {
                    if (!target.available()) {
                        l.phase(
                                "PUBBLICAZIONE saltata per "
                                        + target.seasonId()
                                        + ": sito locale non disponibile: "
                                        + target.siteRoot(),
                                -1
                        );
                    }
                }

                if (availableTargets.isEmpty()) {
                    if (publicationMode == PublicationMode.CURRENT_SITE) {
                        throw new IllegalStateException(
                                "Nessun sito locale configurato e disponibile per la stagione corrente "
                                        + leagueMetadata.currentSeasonId()
                                        + ". Configurare il sito della stagione oppure disattivare la pubblicazione."
                        );
                    }
                    throw new IllegalStateException(
                            "Nessun sito locale configurato e disponibile per la pubblicazione multisito."
                    );
                }

                int totalClassicEntries = 0;
                int totalRuSeasons = 0;
                int totalFiles = 0;
                int totalPublished = 0;

                Path scopesRoot = c.staging()
                        .resolve("multisite-scopes")
                        .normalize();

                deleteTree(scopesRoot);
                Files.createDirectories(scopesRoot);

                Path culometroConfig = c.projectRoot()
                        .resolve("config/culometro.json")
                        .normalize();

                if (o.culometroEnabled()) {
                    if (!Files.isRegularFile(culometroConfig)) {
                        throw new IllegalStateException(
                                "Configurazione Culometro non trovata: "
                                        + culometroConfig
                        );
                    }

                    Path scopedConfigDir = scopesRoot.resolve("config");
                    Files.createDirectories(scopedConfigDir);
                    Files.copy(
                            culometroConfig,
                            scopedConfigDir.resolve("culometro.json"),
                            StandardCopyOption.REPLACE_EXISTING,
                            StandardCopyOption.COPY_ATTRIBUTES
                    );
                }

                try {
                    for (SeasonPublicationTargetRepository.Target target : availableTargets) {
                        List<String> targetSeasons =
                                targetRepository.scope(
                                        c.seasons(),
                                        target
                                );

                        if (targetSeasons.isEmpty()) {
                            l.phase(
                                    "PUBBLICAZIONE saltata per "
                                            + target.seasonId()
                                            + ": nessuna stagione selezionata nello scope.",
                                    -1
                            );
                            continue;
                        }

                        l.phase(
                                "Pubblicazione sito "
                                        + target.seasonId()
                                        + " -> "
                                        + target.siteJs(),
                                84
                        );

                        Path targetScope = scopesRoot.resolve(target.seasonId());
                        Path scopedClassic = targetScope.resolve("classic");
                        Path scopedRu = targetScope.resolve("ru");
                        Path scopedReports = targetScope.resolve("reports");

                        createSeasonScopedView(
                                c.classicArchive(),
                                scopedClassic,
                                targetSeasons
                        );
                        createSeasonScopedView(
                                c.ruArchive(),
                                scopedRu,
                                targetSeasons
                        );
                        createSeasonScopedView(
                                c.reports(),
                                scopedReports,
                                targetSeasons
                        );

                        ManifestMetadata targetManifest =
                                new ManifestMetadata(
                                        "RecordsNext by mauz79",
                                        "3.1.0",
                                        "2.0",
                                        OffsetDateTime.now(),
                                        leagueMetadata.leagueId(),
                                        target.seasonId(),
                                        targetSeasons,
                                        List.of()
                                );

                        var generated = Records2026SitePublisher.run(
                                scopedClassic,
                                scopedRu,
                                c.staging(),
                                target.siteJs(),
                                true,
                                o.familyEnabled(RecordFamily.CLASSICS),
                                o.familyEnabled(RecordFamily.RU),
                                o,
                                preflight,
                                targetManifest,
                                null,
                                null,
                                scopedReports
                        );

                        Path generatedDir =
                                generated.stagingDirectory().resolve("js");

                        CoreJsExporter.export(
                                database,
                                generatedDir.resolve("fcmRecordsNext_Core.js"),
                                leagueMetadata.leagueId(),
                                leagueMetadata.leagueName(),
                                target.seasonId()
                        );

                        int published = publishGeneratedDirectory(
                                generatedDir,
                                target.siteJs()
                        );

                        totalClassicEntries += generated.classicEntries();
                        totalRuSeasons += generated.ruSeasons();
                        totalFiles += published;
                        totalPublished += published;
                    }
                } finally {
                    deleteTree(scopesRoot);
                }

                result = new Result(
                        totalClassicEntries,
                        totalRuSeasons,
                        totalFiles,
                        totalPublished
                );
            }

            l.timing(
                    (
                            o.publish()
                                    ? "generazione e pubblicazione JavaScript multisito: "
                                    : "generazione JavaScript: "
                    )
                            + elapsed(started)
            );
        }

        preparation.saveConsolidation(
                c.seasons()
        );

        l.timing(
                "totale elaborazione: "
                        + elapsed(totalStarted)
        );

        l.phase(
                "Elaborazione completata e consolidamento aggiornato",
                100
        );

        return result;
    }

    public PipelinePreflight.Result preflight(
            ProcessingOptions options
    ) {
        return PipelinePreflight.evaluate(options);
    }

    public boolean hasConsolidation(
            PipelineConfig c
    ) {
        Path database = c.projectRoot()
                .resolve("data/database/recordsnext.db")
                .normalize();

        return new RecordsNextPreparationService(
                c.projectRoot(),
                database
        ).hasConsolidation();
    }

    static void validateImplementedFamilies(
            ProcessingOptions options
    ) {
        Set<RecordFamily> unsupported =
                EnumSet.copyOf(
                        options.selection().enabledFamilies()
                );

        unsupported.removeAll(
                IMPLEMENTED_FAMILIES
        );

        if (!unsupported.isEmpty()) {
            throw new IllegalArgumentException(
                    "Famiglie selezionate ma non ancora collegate alla pipeline 2.0: "
                            + unsupported
            );
        }
    }

    static List<SeasonPublicationTargetRepository.Target> selectPublicationTargets(
            List<SeasonPublicationTargetRepository.Target> targets,
            String currentSeasonId,
            PublicationMode mode) {

        if (mode == PublicationMode.ALL_CONFIGURED_SITES) {
            return List.copyOf(targets);
        }

        return targets.stream()
                .filter(target -> target.seasonId().equals(currentSeasonId))
                .toList();
    }

    private static void createSeasonScopedView(
            Path sourceRoot,
            Path targetRoot,
            List<String> seasons) throws Exception {

        Files.createDirectories(targetRoot);

        for (String season : seasons) {
            Path source = sourceRoot.resolve(season).normalize();
            if (!Files.isDirectory(source)) {
                continue;
            }

            copyTree(source, targetRoot.resolve(season));
        }
    }

    private static void copyTree(
            Path source,
            Path target) throws Exception {

        try (var paths = Files.walk(source)) {
            for (Path path : paths.toList()) {
                Path relative = source.relativize(path);
                Path destination = target.resolve(relative);

                if (Files.isDirectory(path)) {
                    Files.createDirectories(destination);
                } else {
                    Files.createDirectories(destination.getParent());
                    Files.copy(
                            path,
                            destination,
                            StandardCopyOption.REPLACE_EXISTING,
                            StandardCopyOption.COPY_ATTRIBUTES
                    );
                }
            }
        }
    }

    private static int publishGeneratedDirectory(
            Path generatedDir,
            Path siteJsDir) throws Exception {

        Files.createDirectories(siteJsDir);

        List<Path> files;
        try (var stream = Files.list(generatedDir)) {
            files = stream
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(
                            path -> path.getFileName().toString()
                    ))
                    .toList();
        }

        Path backupRoot = generatedDir.getParent()
                .resolve("multisite-publish-backup");
        Files.createDirectories(backupRoot);

        List<Path> replaced = new ArrayList<>();
        List<Path> created = new ArrayList<>();

        try {
            for (Path source : files) {
                Path target = siteJsDir.resolve(source.getFileName());

                if (Files.exists(target)) {
                    Files.copy(
                            target,
                            backupRoot.resolve(source.getFileName()),
                            StandardCopyOption.REPLACE_EXISTING,
                            StandardCopyOption.COPY_ATTRIBUTES
                    );
                    replaced.add(target);
                } else {
                    created.add(target);
                }

                Path temp = siteJsDir.resolve(
                        "." + source.getFileName()
                                + ".recordsnext-multisite.tmp"
                );

                Files.copy(
                        source,
                        temp,
                        StandardCopyOption.REPLACE_EXISTING
                );

                try {
                    Files.move(
                            temp,
                            target,
                            StandardCopyOption.ATOMIC_MOVE,
                            StandardCopyOption.REPLACE_EXISTING
                    );
                } catch (java.nio.file.AtomicMoveNotSupportedException ex) {
                    Files.move(
                            temp,
                            target,
                            StandardCopyOption.REPLACE_EXISTING
                    );
                }
            }
        } catch (Exception ex) {
            for (Path target : created) {
                Files.deleteIfExists(target);
            }

            for (Path target : replaced) {
                Path backup = backupRoot.resolve(target.getFileName());
                if (Files.isRegularFile(backup)) {
                    Files.copy(
                            backup,
                            target,
                            StandardCopyOption.REPLACE_EXISTING
                    );
                }
            }

            throw ex;
        }

        return files.size();
    }

    private static void deleteTree(
            Path root
    ) throws Exception {
        if (!Files.exists(root)) {
            return;
        }

        try (var paths = Files.walk(root)) {
            for (Path path :
                    paths.sorted(
                            java.util.Comparator.reverseOrder()
                    ).toList()) {

                Files.deleteIfExists(path);
            }
        }
    }

    private static String elapsed(
            long started
    ) {
        double seconds =
                (System.nanoTime() - started)
                        / 1_000_000_000.0;

        return String.format(
                Locale.ROOT,
                "%.3f s",
                seconds
        );
    }
}
