package it.alterlega.recordsnext;

import it.alterlega.recordsnext.app.PipelinePreflight;
import it.alterlega.recordsnext.app.ProcessingOptions;
import it.alterlega.recordsnext.app.manifest.ManifestJsWriter;
import it.alterlega.recordsnext.app.manifest.ManifestMetadata;
import it.alterlega.recordsnext.app.manifest.ManifestPublishingSupport;
import it.alterlega.recordsnext.app.core.CoreJsExporter;
import it.alterlega.recordsnext.app.core.LeagueMetadata;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
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
 * Coordina la generazione e la pubblicazione degli output JS compatibili
 * con Records2026.
 *
 * Flusso:
 *  1. genera tutto in una staging isolata;
 *  2. valida nomi, quantità, prefissi e dimensioni minime;
 *  3. pubblica ogni file mediante file temporaneo + move atomica;
 *  4. ripristina i file precedenti se una pubblicazione fallisce.
 */
public final class Records2026SitePublisher {

    private static final String CORE_FILE = "fcmRecordsNext_Core.js";
    private static final String CLASSIC_FILE = "records2026.recordstagionali.classic.js";
    private static final String RU_FILE = "records2026.recordstagionali.ru.js";
    private static final String MANIFEST_FILE = "records2026.storico.ru.manifest.js";
    private static final String ANNUAL_PREFIX = "records2026.storico.ru.";
    private static final String ANNUAL_SUFFIX = ".js";

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
            LeagueMetadata leagueMetadata) throws IOException {
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
                leagueMetadata
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
            LeagueMetadata leagueMetadata) throws IOException {

        boolean includeRecordsNextManifest = options != null
                && preflight != null
                && manifestMetadata != null;
        boolean includeRecordsNextCore = includeRecordsNextManifest
                && database != null
                && leagueMetadata != null;

        if (!includeClassic && !includeRu && !includeRecordsNextManifest && !includeRecordsNextCore) {
            throw new IOException("Nessun modulo selezionato per la generazione JS");
        }
        if (includeClassic) requireDirectory(classicArchive, "Archivio classic");
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
            var classic = Records2026ClassicJsExporter.export(
                    classicArchive, generatedDir.resolve(CLASSIC_FILE), List.of());
            classicEntries = classic.entryCount();
        }
        if (includeRu) {
            var ru = Records2026RuJsExporter.export(ruArchive, generatedDir);
            ruSeasons = ru.seasons();
            annualFiles = ru.annualFiles();
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
                annualFiles,
                includeClassic,
                includeRu,
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
            int expectedAnnualFiles,
            boolean includeClassic,
            boolean includeRu,
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
            requireFile(byName, CLASSIC_FILE);
            validatePrefix(byName.get(CLASSIC_FILE), "window.RECORDS2026_PREVIEW_CLASSIC");
        }
        List<Path> annuals = files.stream().filter(Records2026SitePublisher::isAnnualFile).toList();
        if (includeRu) {
            requireFile(byName, RU_FILE);
            requireFile(byName, MANIFEST_FILE);
            if (annuals.size() != expectedAnnualFiles) {
                throw new IOException("Numero file annuali inatteso: " + annuals.size()
                        + ", attesi " + expectedAnnualFiles);
            }
            validatePrefix(byName.get(RU_FILE), "window.RECORDS2026_PREVIEW_RU");
            validatePrefix(byName.get(MANIFEST_FILE), "window.RECORDS2026_STORICO_RU_MANIFEST");
            for (Path annual : annuals) validateContains(annual, "window.RECORDS2026_STORICO_RU");
        } else if (!annuals.isEmpty()) {
            throw new IOException("File RU annuali generati nonostante il modulo RU sia disattivato");
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
                + (includeRu ? expectedAnnualFiles + 2 : 0)
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

    private static boolean isAnnualFile(Path path) {
        String name = path.getFileName().toString();
        return name.startsWith(ANNUAL_PREFIX)
                && name.endsWith(ANNUAL_SUFFIX)
                && !name.equals(MANIFEST_FILE);
    }

    private static void validatePrefix(Path path, String expectedPrefix) throws IOException {
        String sample = readStart(path, 4096);
        if (!stripBom(sample).stripLeading().startsWith(expectedPrefix)) {
            throw new IOException("Prefisso JS non valido in " + path.getFileName()
                    + ": atteso " + expectedPrefix);
        }
    }

    private static void validateContains(Path path, String expectedToken) throws IOException {
        String sample = readStart(path, 8192);
        if (!stripBom(sample).contains(expectedToken)) {
            throw new IOException("Token JS non trovato in " + path.getFileName()
                    + ": " + expectedToken);
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
