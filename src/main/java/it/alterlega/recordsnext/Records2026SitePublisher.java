package it.alterlega.recordsnext;

import it.alterlega.recordsnext.app.PipelinePreflight;
import it.alterlega.recordsnext.app.ProcessingOptions;
import it.alterlega.recordsnext.app.manifest.ManifestJsWriter;
import it.alterlega.recordsnext.app.manifest.ManifestMetadata;
import it.alterlega.recordsnext.app.manifest.ManifestPublishingSupport;
import it.alterlega.recordsnext.app.core.CoreJsExporter;
import it.alterlega.recordsnext.app.core.LeagueMetadata;
import it.alterlega.recordsnext.app.classics.ClassicsFamilyJsExporter;
import it.alterlega.recordsnext.app.ru.RuFamilyJsExporter;
import it.alterlega.recordsnext.app.series.SeriesFamilyJsExporter;
import it.alterlega.recordsnext.app.modifiers.ModifiersFamilyJsExporter;
import it.alterlega.recordsnext.app.thresholds.ThresholdsLuckFamilyJsExporter;
import it.alterlega.recordsnext.app.culometro.CulometroFamilyJsExporter;
import it.alterlega.recordsnext.app.matches.MatchesJsExporter;
import it.alterlega.recordsnext.app.model.RecordFamily;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Coordina la generazione e la pubblicazione degli output JS pubblici
 * di RecordsNext.
 *
 * Flusso:
 *  1. genera tutto in una staging isolata;
 *  2. valida nomi, quantità, prefissi e dimensioni minime;
 *  3. pubblica ogni file mediante file temporaneo + move atomica;
 *  4. ripristina i file precedenti se una pubblicazione fallisce.
 */
public final class Records2026SitePublisher {

    private static final String CORE_FILE = "fcmRecordsNext_Core.js";
    private static final String CLASSICS_2_FILE = ClassicsFamilyJsExporter.FILE_NAME;
    private static final String RU_2_FILE = RuFamilyJsExporter.FILE_NAME;
    private static final String SERIES_2_FILE = SeriesFamilyJsExporter.FILE_NAME;
    private static final String MODIFIERS_2_FILE = ModifiersFamilyJsExporter.FILE_NAME;
    private static final String THRESHOLDS_2_FILE = ThresholdsLuckFamilyJsExporter.FILE_NAME;
    private static final String CULOMETRO_2_FILE = CulometroFamilyJsExporter.FILE_NAME;
    private static final String MATCHES_2_FILE = MatchesJsExporter.FILE_NAME;

    private Records2026SitePublisher() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 4 || args.length > 5) {
            System.err.println("Uso:");
            System.err.println("  Records2026SitePublisher <classicArchive> <ruArchive> <stagingRoot> <siteJsDir> [--generate-only]");
            System.exit(2);
        }

        Path classicArchive = Path.of(args[0]).toAbsolutePath().normalize();
        Path ruArchive = Path.of(args[1]).toAbsolutePath().normalize();
        Path stagingRoot = Path.of(args[2]).toAbsolutePath().normalize();
        Path siteJsDir = Path.of(args[3]).toAbsolutePath().normalize();
        boolean generateOnly = args.length == 5 && "--generate-only".equalsIgnoreCase(args[4]);

        PublishResult result = run(classicArchive, ruArchive, stagingRoot, siteJsDir, generateOnly);

        System.out.println("Classic     : " + result.classicEntries() + " recordset");
        System.out.println("RU stagioni : " + result.ruSeasons());
        System.out.println("RU annuali  : " + result.annualFiles());
        System.out.println("File validi : " + result.validatedFiles());
        System.out.println("Staging     : " + result.stagingDirectory());
        System.out.println(generateOnly
                ? "Pubblicazione: NON ESEGUITA (--generate-only)"
                : "Pubblicati   : " + result.publishedFiles() + " file in " + siteJsDir);
    }

    public static PublishResult run(
            Path classicArchive,
            Path ruArchive,
            Path stagingRoot,
            Path siteJsDir,
            boolean generateOnly) throws IOException {
        return run(classicArchive, ruArchive, stagingRoot, siteJsDir, generateOnly, true, true);
    }

    public static PublishResult run(
            Path classicArchive,
            Path ruArchive,
            Path stagingRoot,
            Path siteJsDir,
            boolean generateOnly,
            boolean includeClassic,
            boolean includeRu) throws IOException {
        return runInternal(
                classicArchive,
                ruArchive,
                stagingRoot,
                siteJsDir,
                generateOnly,
                includeClassic,
                includeRu,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    public static PublishResult run(
            Path classicArchive,
            Path ruArchive,
            Path stagingRoot,
            Path siteJsDir,
            boolean generateOnly,
            boolean includeClassic,
            boolean includeRu,
            ProcessingOptions options,
            PipelinePreflight.Result preflight,
            ManifestMetadata manifestMetadata) throws IOException {
        return runInternal(
                classicArchive,
                ruArchive,
                stagingRoot,
                siteJsDir,
                generateOnly,
                includeClassic,
                includeRu,
                options,
                preflight,
                manifestMetadata,
                null,
                null,
                null
        );
    }

    public static PublishResult run(
            Path classicArchive,
            Path ruArchive,
            Path stagingRoot,
            Path siteJsDir,
            boolean generateOnly,
            boolean includeClassic,
            boolean includeRu,
            ProcessingOptions options,
            PipelinePreflight.Result preflight,
            ManifestMetadata manifestMetadata,
            Path database,
            LeagueMetadata leagueMetadata,
            Path reportsRoot) throws IOException {
        return runInternal(
                classicArchive,
                ruArchive,
                stagingRoot,
                siteJsDir,
                generateOnly,
                includeClassic,
                includeRu,
                options,
                preflight,
                manifestMetadata,
                database,
                leagueMetadata,
                reportsRoot
        );
    }

    private static PublishResult runInternal(
            Path classicArchive,
            Path ruArchive,
            Path stagingRoot,
            Path siteJsDir,
            boolean generateOnly,
            boolean includeClassic,
            boolean includeRu,
            ProcessingOptions options,
            PipelinePreflight.Result preflight,
            ManifestMetadata manifestMetadata,
            Path database,
            LeagueMetadata leagueMetadata,
            Path reportsRoot) throws IOException {

        boolean includeSeries = options != null && options.familyEnabled(RecordFamily.SERIES);
        boolean includeModifiers = options != null && options.familyEnabled(RecordFamily.MODIFIERS);
        boolean includeThresholds = options != null && options.familyEnabled(RecordFamily.THRESHOLDS_LUCK);
        boolean includeCulometro = options != null && options.culometroEnabled();
        boolean includeRecordsNextManifest = options != null
                && preflight != null
                && manifestMetadata != null;
        boolean includeRecordsNextCore = includeRecordsNextManifest
                && database != null
                && leagueMetadata != null;
        boolean includeMatches = includeRecordsNextManifest && reportsRoot != null;

        if (!includeClassic && !includeRu && !includeSeries && !includeModifiers && !includeThresholds && !includeCulometro && !includeRecordsNextManifest && !includeRecordsNextCore && !includeMatches) {
            throw new IOException("Nessun modulo selezionato per la generazione JS");
        }
        if (includeClassic || includeSeries || includeModifiers) requireDirectory(classicArchive, "Archivio classic");
        if (includeThresholds || includeCulometro || includeMatches) requireDirectory(reportsRoot, "Report normalizzati");
        if (includeRu) requireDirectory(ruArchive, "Archivio RU");
        Files.createDirectories(stagingRoot);

        String runId = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                + "_" + UUID.randomUUID().toString().substring(0, 8);
        Path runDir = stagingRoot.resolve("records2026_" + runId);
        Path generatedDir = runDir.resolve("js");
        Files.createDirectories(generatedDir);

        int classicEntries = 0;
        int ruSeasons = 0;
        int annualFiles = 0;
        if (includeClassic) {
            var classic = ClassicsFamilyJsExporter.export(
                    classicArchive, generatedDir.resolve(CLASSICS_2_FILE));
            classicEntries = classic.entryCount();
        }
        if (includeSeries) {
            SeriesFamilyJsExporter.export(classicArchive, generatedDir.resolve(SERIES_2_FILE));
        }
        if (includeModifiers) {
            ModifiersFamilyJsExporter.export(classicArchive, generatedDir.resolve(MODIFIERS_2_FILE));
        }
        if (includeThresholds) {
            ThresholdsLuckFamilyJsExporter.export(reportsRoot, generatedDir.resolve(THRESHOLDS_2_FILE));
        }
        if (includeRu) {
            var ru = RuFamilyJsExporter.export(
                    ruArchive, generatedDir.resolve(RU_2_FILE));
            ruSeasons = ru.seasonCount();
            annualFiles = ru.annualFileCount();
        }
        if (includeCulometro) {
            Path projectRoot = reportsRoot.toAbsolutePath().normalize().getParent().getParent();
            CulometroFamilyJsExporter.export(
                    generatedDir.resolve(THRESHOLDS_2_FILE),
                    generatedDir.resolve(RU_2_FILE),
                    projectRoot.resolve("config/culometro.json"),
                    generatedDir.resolve(CULOMETRO_2_FILE)
            );
        }
        if (includeMatches) {
            MatchesJsExporter.export(reportsRoot, generatedDir.resolve(MATCHES_2_FILE));
        }

        if (includeRecordsNextCore) {
            try {
                CoreJsExporter.export(
                        database,
                        generatedDir.resolve(CORE_FILE),
                        leagueMetadata.leagueId(),
                        leagueMetadata.leagueName()
                );
            } catch (Exception ex) {
                if (ex instanceof IOException io) throw io;
                throw new IOException("Generazione Core 2.0 fallita", ex);
            }
        }

        if (includeRecordsNextManifest) {
            ManifestPublishingSupport.write(
                    generatedDir,
                    options,
                    preflight,
                    manifestMetadata
            );
        }

        ValidationResult validation = validateGenerated(
                generatedDir,
                includeClassic,
                includeRu,
                includeSeries,
                includeModifiers,
                includeThresholds,
                includeCulometro,
                includeMatches,
                includeRecordsNextManifest,
                includeRecordsNextCore
        );
        int published = 0;
        if (!generateOnly) {
            Files.createDirectories(siteJsDir);
            published = publishWithRollback(generatedDir, siteJsDir, validation.files());
        }
        return new PublishResult(classicEntries, ruSeasons, annualFiles,
                validation.files().size(), published, runDir);
    }

    private static ValidationResult validateGenerated(
            Path generatedDir,
            boolean includeClassic,
            boolean includeRu,
            boolean includeSeries,
            boolean includeModifiers,
            boolean includeThresholds,
            boolean includeCulometro,
            boolean includeMatches,
            boolean includeRecordsNextManifest,
            boolean includeRecordsNextCore) throws IOException {

        List<Path> files;
        try (var stream = Files.list(generatedDir)) {
            files = stream
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .toList();
        }

        Map<String, Path> byName = new LinkedHashMap<>();
        for (Path file : files) {
            byName.put(file.getFileName().toString(), file);
        }

        if (includeClassic) {
            requireFile(byName, CLASSICS_2_FILE);
            validatePrefix(byName.get(CLASSICS_2_FILE), "window.fcmRecordsNextClassics");
        }
        if (includeSeries) {
            requireFile(byName, SERIES_2_FILE);
            validatePrefix(byName.get(SERIES_2_FILE), "window.fcmRecordsNextSeries");
        }
        if (includeModifiers) {
            requireFile(byName, MODIFIERS_2_FILE);
            validatePrefix(byName.get(MODIFIERS_2_FILE), "window.fcmRecordsNextModifiers");
        }
        if (includeThresholds) {
            requireFile(byName, THRESHOLDS_2_FILE);
            validatePrefix(byName.get(THRESHOLDS_2_FILE), "window.fcmRecordsNextThresholdsLuck");
        }
        if (includeCulometro) {
            requireFile(byName, CULOMETRO_2_FILE);
            validatePrefix(byName.get(CULOMETRO_2_FILE), "window.fcmRecordsNextCulometro");
        }
        if (includeMatches) {
            requireFile(byName, MATCHES_2_FILE);
            validatePrefix(byName.get(MATCHES_2_FILE), "window.fcmRecordsNextMatches");
        }
        if (includeRu) {
            requireFile(byName, RU_2_FILE);
            validatePrefix(byName.get(RU_2_FILE), "window.fcmRecordsNextRU");
        }
        if (includeRecordsNextCore) {
            requireFile(byName, CORE_FILE);
            validatePrefix(byName.get(CORE_FILE), "window.fcmRecordsNextCore");
        }
        if (includeRecordsNextManifest) {
            requireFile(byName, ManifestJsWriter.FILE_NAME);
            validatePrefix(
                    byName.get(ManifestJsWriter.FILE_NAME),
                    "window.fcmRecordsNextManifest"
            );
        }
        int expectedTotal = (includeClassic ? 1 : 0)
                + (includeSeries ? 1 : 0)
                + (includeModifiers ? 1 : 0)
                + (includeThresholds ? 1 : 0)
                + (includeCulometro ? 1 : 0)
                + (includeMatches ? 1 : 0)
                + (includeRu ? 1 : 0)
                + (includeRecordsNextCore ? 1 : 0)
                + (includeRecordsNextManifest ? 1 : 0);
        if (files.size() != expectedTotal) {
            throw new IOException("Numero file JS inatteso: " + files.size()
                    + ", attesi " + expectedTotal);
        }

        return new ValidationResult(files);
    }

    private static int publishWithRollback(Path generatedDir, Path siteJsDir, List<Path> generatedFiles)
            throws IOException {

        Path transactionDir = generatedDir.getParent().resolve("publish-transaction");
        Path backupDir = transactionDir.resolve("backup");
        Files.createDirectories(backupDir);

        List<String> replacedNames = new ArrayList<>();
        List<String> newlyCreatedNames = new ArrayList<>();

        try {
            for (Path source : generatedFiles) {
                String name = source.getFileName().toString();
                Path target = siteJsDir.resolve(name);
                Path backup = backupDir.resolve(name);

                if (Files.exists(target)) {
                    Files.copy(target, backup,
                            StandardCopyOption.REPLACE_EXISTING,
                            StandardCopyOption.COPY_ATTRIBUTES);
                    replacedNames.add(name);
                } else {
                    newlyCreatedNames.add(name);
                }

                Path temp = siteJsDir.resolve("." + name + ".recordsnext-" + UUID.randomUUID() + ".tmp");
                Files.copy(source, temp, StandardCopyOption.REPLACE_EXISTING);
                moveReplace(temp, target);
            }
        } catch (Exception publicationFailure) {
            IOException rollbackFailure = rollback(siteJsDir, backupDir, replacedNames, newlyCreatedNames);
            if (rollbackFailure != null) {
                publicationFailure.addSuppressed(rollbackFailure);
            }
            if (publicationFailure instanceof IOException io) {
                throw io;
            }
            throw new IOException("Pubblicazione fallita", publicationFailure);
        }

        return generatedFiles.size();
    }

    private static IOException rollback(
            Path siteJsDir,
            Path backupDir,
            List<String> replacedNames,
            List<String> newlyCreatedNames) {

        IOException firstFailure = null;

        for (String name : newlyCreatedNames) {
            try {
                Files.deleteIfExists(siteJsDir.resolve(name));
            } catch (IOException ex) {
                if (firstFailure == null) firstFailure = ex;
                else firstFailure.addSuppressed(ex);
            }
        }

        for (String name : replacedNames) {
            try {
                Path backup = backupDir.resolve(name);
                Path temp = siteJsDir.resolve("." + name + ".rollback-" + UUID.randomUUID() + ".tmp");
                Files.copy(backup, temp, StandardCopyOption.REPLACE_EXISTING);
                moveReplace(temp, siteJsDir.resolve(name));
            } catch (IOException ex) {
                if (firstFailure == null) firstFailure = ex;
                else firstFailure.addSuppressed(ex);
            }
        }

        return firstFailure;
    }

    private static void moveReplace(Path source, Path target) throws IOException {
        try {
            Files.move(source, target,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void requireDirectory(Path path, String label) throws IOException {
        if (!Files.isDirectory(path)) {
            throw new IOException(label + " inesistente o non valida: " + path);
        }
    }

    private static void requireFile(Map<String, Path> files, String name) throws IOException {
        if (!files.containsKey(name)) {
            throw new IOException("File generato mancante: " + name);
        }
    }

    private static void validatePrefix(Path path, String expectedPrefix) throws IOException {
        String sample = readStart(path, 4096);
        if (!stripBom(sample).stripLeading().startsWith(expectedPrefix)) {
            throw new IOException("Prefisso JS non valido in " + path.getFileName()
                    + ": atteso " + expectedPrefix);
        }
    }

    private static String readStart(Path path, int maxBytes) throws IOException {
        long size = Files.size(path);
        if (size <= 16) {
            throw new IOException("File generato vuoto o troppo corto: " + path);
        }
        byte[] bytes = new byte[(int) Math.min(size, maxBytes)];
        try (var input = Files.newInputStream(path)) {
            int offset = 0;
            while (offset < bytes.length) {
                int read = input.read(bytes, offset, bytes.length - offset);
                if (read < 0) break;
                offset += read;
            }
            return new String(bytes, 0, offset, StandardCharsets.UTF_8);
        }
    }

    private static String stripBom(String value) {
        return !value.isEmpty() && value.charAt(0) == '\uFEFF' ? value.substring(1) : value;
    }

    public record PublishResult(
            int classicEntries,
            int ruSeasons,
            int annualFiles,
            int validatedFiles,
            int publishedFiles,
            Path stagingDirectory) {
    }

    private record ValidationResult(List<Path> files) {
    }
}
