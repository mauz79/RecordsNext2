package it.alterlega.recordsnext.app;

import it.alterlega.recordsnext.CanonicalViews;
import it.alterlega.recordsnext.ConfigurationSchema;
import it.alterlega.recordsnext.RawSqliteImporter;
import it.alterlega.recordsnext.SeasonNormalizedBatchExporter;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

final class RecordsNextPreparationService {
    record SeasonSource(String id, String type, String fcm, String fca,
                        String localSite, String onlineSite) {}

    private final Path root;
    private final Path database;
    private static final String NORMALIZER_CACHE_VERSION = "season-normalized-v21";

    private final Path stateFile;
    private final Path normalizationCacheFile;

    RecordsNextPreparationService(Path root, Path database) {
        this.root = root.toAbsolutePath().normalize();
        this.database = database.toAbsolutePath().normalize();
        this.stateFile = this.root.resolve("data/consolidation/recordsnext-consolidation.properties");
        this.normalizationCacheFile = this.root.resolve("data/consolidation/normalization-cache.properties");
    }

    List<String> prepare(ProcessingMode mode, List<String> selected,
                         RecordsNextPipeline.Listener listener) throws Exception {
        List<SeasonSource> seasons = loadConfigured(selected);
        if (seasons.isEmpty()) {
            throw new IllegalStateException("Nessuna stagione configurata da elaborare.");
        }
        List<SeasonSource> managed = seasons.stream()
            .filter(s -> "GESTITA".equals(s.type()))
            .sorted(Comparator.comparing(SeasonSource::id))
            .toList();
        if (managed.isEmpty()) {
            throw new IllegalStateException("Non esistono stagioni gestite da importare.");
        }
        SeasonSource current = managed.get(managed.size() - 1);

        List<SeasonSource> toImport;
        if (mode == ProcessingMode.CONSOLIDATED) {
            validateConsolidation(seasons, current.id());
            toImport = List.of(current);
            listener.phase("Aggiornamento della stagione attuale " + current.id(), 5);
        } else {
            toImport = managed;
            listener.phase("Importazione completa delle stagioni gestite", 5);
        }

        int index = 0;
        boolean imported = false;
        for (SeasonSource season : toImport) {
            validateManagedSource(season);
            int percent = 6 + (int) Math.round((index++ * 22.0) / Math.max(1, toImport.size()));
            if (sourceNeedsImport(season.id(), "FCM", season.fcm())) {
                listener.phase(season.id() + " — importazione FCM", percent);
                long started = System.nanoTime();
                RawSqliteImporter.main(new String[]{season.fcm(), "FCM", season.id(), database.toString()});
                listener.timing(season.id() + " — importazione FCM: " + elapsed(started));
                imported = true;
            } else {
                listener.phase(season.id() + " — FCM invariato", percent);
            }
            if (sourceNeedsImport(season.id(), "FCA", season.fca())) {
                listener.phase(season.id() + " — importazione FCA", Math.min(29, percent + 2));
                long started = System.nanoTime();
                RawSqliteImporter.main(new String[]{season.fca(), "FCA", season.id(), database.toString()});
                listener.timing(season.id() + " — importazione FCA: " + elapsed(started));
                imported = true;
            } else {
                listener.phase(season.id() + " — FCA invariato", Math.min(29, percent + 2));
            }
        }

        if (imported) {
            listener.phase("Aggiornamento configurazione e identità storiche", 30);
            long started = System.nanoTime();
            ConfigurationSchema.main(new String[]{database.toString(), current.id()});
            listener.timing("configurazione e identità: " + elapsed(started));
        } else {
            listener.phase("Sorgenti già importate; configurazione conservata", 30);
        }

        validateMappings(managed, current.id());

        listener.phase("Rigenerazione viste canoniche", 34);
        long canonicalStarted = System.nanoTime();
        CanonicalViews.main(new String[]{database.toString()});
        listener.timing("viste canoniche: " + elapsed(canonicalStarted));

        List<String> normalize = mode == ProcessingMode.CONSOLIDATED
            ? List.of(current.id())
            : managed.stream().map(SeasonSource::id).toList();
        Properties normalizationCache = loadNormalizationCache();
        if (mode == ProcessingMode.FULL) {
            listener.phase("Pulizia dati normalizzati delle stagioni gestite", 35);
            for (String season : normalize) {
                deleteNormalizationOutputs(season);
                normalizationCache.remove("season." + season + ".signature");
                normalizationCache.remove("season." + season + ".completedAt");
            }
            saveNormalizationCache(normalizationCache);
        }
        int done = 0;
        for (String season : normalize) {
            int percent = 36 + (int) Math.round((done++ * 14.0) / Math.max(1, normalize.size()));
            SeasonSource source = managed.stream()
                .filter(item -> item.id().equals(season))
                .findFirst()
                .orElseThrow();
            String signature = normalizationSignature(source);
            if (mode != ProcessingMode.FULL
                    && normalizationCacheValid(season, signature, normalizationCache)) {
                listener.phase(season + " — normalizzazione invariata, riutilizzata", percent);
                continue;
            }
            if (mode != ProcessingMode.FULL
                    && !normalizationCache.containsKey("season." + season + ".signature")
                    && canBootstrapNormalizationCache(source)) {
                normalizationCache.setProperty("season." + season + ".signature", signature);
                normalizationCache.setProperty("season." + season + ".completedAt", java.time.Instant.now().toString());
                saveNormalizationCache(normalizationCache);
                listener.phase(season + " — cache normalizzazione inizializzata, dati riutilizzati", percent);
                continue;
            }
            listener.phase(season + " — normalizzazione", percent);
            long normalizeStarted = System.nanoTime();
            SeasonNormalizedBatchExporter.export(database, season, root);
            listener.timing(season + " — normalizzazione: " + elapsed(normalizeStarted));
            normalizationCache.setProperty("season." + season + ".signature", signature);
            normalizationCache.setProperty("season." + season + ".completedAt", java.time.Instant.now().toString());
            saveNormalizationCache(normalizationCache);
        }
        return normalize;
    }


    private void deleteNormalizationOutputs(String season) throws Exception {
        Path outputDir = root.resolve("data/reports").resolve(season);
        if (!Files.isDirectory(outputDir)) return;
        try (var files = Files.list(outputDir)) {
            for (Path path : files.filter(Files::isRegularFile)
                    .filter(item -> item.getFileName().toString().startsWith("season_normalized_"))
                    .toList()) {
                Files.deleteIfExists(path);
            }
        }
    }

    private Properties loadNormalizationCache() throws Exception {
        Properties cache = new Properties();
        if (Files.isRegularFile(normalizationCacheFile)) {
            try (InputStream in = Files.newInputStream(normalizationCacheFile)) {
                cache.load(in);
            }
        }
        return cache;
    }

    private void saveNormalizationCache(Properties cache) throws Exception {
        Files.createDirectories(normalizationCacheFile.getParent());
        try (OutputStream out = Files.newOutputStream(normalizationCacheFile)) {
            cache.store(out, "RecordsNext normalized season cache");
        }
    }

    private String normalizationSignature(SeasonSource season) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        updateDigest(digest, NORMALIZER_CACHE_VERSION);
        updateDigest(digest, season.id());
        updateDigest(digest, season.type());
        updateFileDigest(digest, season.fcm());
        updateFileDigest(digest, season.fca());
        updateDigest(digest, season.localSite());
        updateDigest(digest, season.onlineSite());
        updateDigest(digest, mappingStamp(season.id()));
        return toHex(digest.digest());
    }

    private static void updateFileDigest(MessageDigest digest, String value) throws Exception {
        updateDigest(digest, value == null ? "" : value);
        if (value == null || value.isBlank()) return;
        Path file = Path.of(value).toAbsolutePath().normalize();
        if (!Files.isRegularFile(file)) return;
        updateDigest(digest, Long.toString(Files.size(file)));
        updateDigest(digest, Files.getLastModifiedTime(file).toInstant().toString());
    }

    private static void updateDigest(MessageDigest digest, String value) {
        digest.update((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
        digest.update((byte) 0);
    }

    private boolean canBootstrapNormalizationCache(SeasonSource season) throws Exception {
        if (!Files.isRegularFile(stateFile) || !normalizationOutputsComplete(season.id())) {
            return false;
        }
        Properties old = new Properties();
        try (InputStream in = Files.newInputStream(stateFile)) {
            old.load(in);
        }
        String prefix = "season." + season.id() + ".";
        Properties now = snapshot(List.of(season));
        for (String suffix : List.of("type", "fcm", "fcm.size", "fcm.mtime",
                "fca", "fca.size", "fca.mtime", "site", "online", "mapping")) {
            String key = prefix + suffix;
            if (!old.getProperty(key, "").equals(now.getProperty(key, ""))) {
                return false;
            }
        }
        return true;
    }

    private boolean normalizationCacheValid(String season, String signature, Properties cache) throws Exception {
        if (!signature.equals(cache.getProperty("season." + season + ".signature", ""))) {
            return false;
        }
        return normalizationOutputsComplete(season);
    }

    private boolean normalizationOutputsComplete(String season) throws Exception {
        Path outputDir = root.resolve("data/reports").resolve(season);
        if (!Files.isDirectory(outputDir)) return false;
        long actual;
        try (var files = Files.list(outputDir)) {
            actual = files.filter(path -> {
                String name = path.getFileName().toString();
                return name.startsWith("season_normalized_") && name.endsWith(".json")
                    && !name.contains(".stage") && !name.contains(".final");
            }).count();
        }
        return actual >= expectedCompetitionCount(season) && actual > 0;
    }

    private long expectedCompetitionCount(String season) throws Exception {
        String sql = """
            SELECT COUNT(DISTINCT competition_name)
            FROM rn_team_match
            WHERE season_id=? AND competition_name IS NOT NULL AND TRIM(competition_name)<>''
            """;
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + database);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, season);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        }
    }

    void saveConsolidation(List<String> selected) throws Exception {
        List<SeasonSource> seasons = loadConfigured(selected);
        Properties p = snapshot(seasons);
        Files.createDirectories(stateFile.getParent());
        try (OutputStream out = Files.newOutputStream(stateFile)) {
            p.store(out, "RecordsNext consolidation state");
        }
    }

    boolean hasConsolidation() {
        return Files.isRegularFile(stateFile);
    }

    private void validateConsolidation(List<SeasonSource> seasons, String currentId) throws Exception {
        if (!Files.isRegularFile(stateFile)) {
            throw new IllegalStateException("Nessun consolidamento disponibile. Eseguire prima un'elaborazione completa.");
        }
        Properties old = new Properties();
        try (InputStream in = Files.newInputStream(stateFile)) { old.load(in); }
        Properties now = snapshot(seasons);
        String oldIds = old.getProperty("seasons", "");
        String nowIds = now.getProperty("seasons", "");
        if (!oldIds.equals(nowIds)) {
            throw invalid("è cambiato l'elenco delle stagioni");
        }
        for (SeasonSource season : seasons) {
            if (season.id().equals(currentId)) continue;
            String prefix = "season." + season.id() + ".";
            for (String suffix : List.of("type", "fcm", "fcm.size", "fcm.mtime", "fca", "fca.size", "fca.mtime", "site", "online", "mapping")) {
                String key = prefix + suffix;
                if (!old.getProperty(key, "").equals(now.getProperty(key, ""))) {
                    throw invalid("è cambiata la stagione storica " + season.id() + " (" + suffix + ")");
                }
            }
        }
    }

    private static IllegalStateException invalid(String reason) {
        return new IllegalStateException("Il consolidamento non è più valido: " + reason
            + ". Eseguire una nuova elaborazione completa.");
    }

    private Properties snapshot(List<SeasonSource> seasons) throws Exception {
        Properties p = new Properties();
        p.setProperty("seasons", String.join(",", seasons.stream().map(SeasonSource::id).sorted().toList()));
        for (SeasonSource s : seasons) {
            String k = "season." + s.id() + ".";
            p.setProperty(k + "type", s.type());
            fileSnapshot(p, k + "fcm", s.fcm());
            fileSnapshot(p, k + "fca", s.fca());
            p.setProperty(k + "site", s.localSite());
            p.setProperty(k + "online", s.onlineSite());
            p.setProperty(k + "mapping", mappingStamp(s.id()));
        }
        return p;
    }

    private static void fileSnapshot(Properties p, String key, String value) throws Exception {
        p.setProperty(key, value == null ? "" : value);
        if (value != null && !value.isBlank() && Files.isRegularFile(Path.of(value))) {
            Path file = Path.of(value);
            p.setProperty(key + ".size", Long.toString(Files.size(file)));
            p.setProperty(key + ".mtime", Long.toString(Files.getLastModifiedTime(file).toMillis()));
        } else {
            p.setProperty(key + ".size", "");
            p.setProperty(key + ".mtime", "");
        }
    }

    private String mappingStamp(String seasonId) throws Exception {
        // The consolidation signature must describe mapping decisions, not timestamps.
        // Only entities belonging to the latest FCM import of the season are relevant.
        String competitionSql = """
            SELECT s.source_competition_id,
                   s.normalized_name,
                   m.mapping_status,
                   COALESCE(m.competition_identity_id,0)
            FROM rn_competition_season s
            JOIN rn_competition_mapping m
              ON m.competition_season_id=s.competition_season_id
            JOIN rn_source_file sf ON sf.source_file_id=s.source_file_id
            WHERE s.season_id=?
              AND sf.source_type='FCM'
              AND sf.import_id=(
                  SELECT MAX(sf2.import_id)
                  FROM rn_source_file sf2
                  WHERE sf2.season_id=s.season_id AND sf2.source_type='FCM'
              )
            ORDER BY s.source_competition_id, s.normalized_name,
                     m.mapping_status, COALESCE(m.competition_identity_id,0)
            """;
        String teamSql = """
            SELECT s.source_team_id,
                   s.normalized_name,
                   COALESCE(s.source_division_id,-1),
                   COALESCE(s.source_team_number,-1),
                   m.mapping_status,
                   COALESCE(m.team_identity_id,0)
            FROM rn_team_season s
            JOIN rn_team_mapping m ON m.team_season_id=s.team_season_id
            JOIN rn_source_file sf ON sf.source_file_id=s.source_file_id
            WHERE s.season_id=?
              AND sf.source_type='FCM'
              AND sf.import_id=(
                  SELECT MAX(sf2.import_id)
                  FROM rn_source_file sf2
                  WHERE sf2.season_id=s.season_id AND sf2.source_type='FCM'
              )
            ORDER BY s.source_team_id, s.normalized_name,
                     COALESCE(s.source_division_id,-1), COALESCE(s.source_team_number,-1),
                     m.mapping_status, COALESCE(m.team_identity_id,0)
            """;

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + database)) {
            updateMappingDigest(c, competitionSql, seasonId, "C", digest);
            updateMappingDigest(c, teamSql, seasonId, "T", digest);
        }
        return toHex(digest.digest());
    }

    private static void updateMappingDigest(Connection connection, String sql,
                                            String seasonId, String prefix,
                                            MessageDigest digest) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, seasonId);
            try (ResultSet rs = ps.executeQuery()) {
                int columns = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    digest.update(prefix.getBytes(StandardCharsets.UTF_8));
                    digest.update((byte) 0);
                    for (int column = 1; column <= columns; column++) {
                        String value = rs.getString(column);
                        digest.update((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
                        digest.update((byte) 0);
                    }
                    digest.update((byte) '\n');
                }
            }
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder result = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            result.append(Character.forDigit((value >>> 4) & 0x0f, 16));
            result.append(Character.forDigit(value & 0x0f, 16));
        }
        return result.toString();
    }


    private boolean sourceNeedsImport(String seasonId, String sourceType, String configuredPath) throws Exception {
        Path file = Path.of(configuredPath).toAbsolutePath().normalize();
        String sql = """
            SELECT source_path,source_size_bytes,source_last_modified
            FROM rn_import
            WHERE season_id=? AND source_type=? AND status='COMPLETED'
            ORDER BY import_id DESC
            LIMIT 1
            """;
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + database);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, seasonId);
            ps.setString(2, sourceType);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return true;
                String previousPath = Path.of(rs.getString(1)).toAbsolutePath().normalize().toString();
                long previousSize = rs.getLong(2);
                String previousModified = rs.getString(3);
                return !previousPath.equalsIgnoreCase(file.toString())
                    || previousSize != Files.size(file)
                    || !previousModified.equals(Files.getLastModifiedTime(file).toInstant().toString());
            }
        }
    }
    private List<SeasonSource> loadConfigured(List<String> selected) throws Exception {
        if (!Files.isRegularFile(database)) {
            throw new IllegalStateException("Database RecordsNext non trovato: " + database);
        }
        String sql = """
            SELECT s.season_id,
                   COALESCE(c.management_type,'GESTITA'),
                   COALESCE(c.configured_fcm_path,''),
                   COALESCE(c.configured_fca_path,''),
                   COALESCE(c.local_site_path,''),
                   COALESCE(c.online_site_url,'')
            FROM rn_season s
            LEFT JOIN rn_season_configuration c ON c.season_id=s.season_id
            ORDER BY s.season_id
            """;
        List<SeasonSource> result = new ArrayList<>();
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + database);
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String id = rs.getString(1);
                if (selected.contains(id)) {
                    result.add(new SeasonSource(id, rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getString(5), rs.getString(6)));
                }
            }
        }
        return result;
    }

    private void validateMappings(List<SeasonSource> managed, String currentId) throws Exception {
        String sql = """
            SELECT
              (SELECT COUNT(*) FROM rn_competition_mapping m
               JOIN rn_competition_season s ON s.competition_season_id=m.competition_season_id
               JOIN rn_source_file sf ON sf.source_file_id=s.source_file_id
               WHERE s.season_id=? AND m.mapping_status='DA_CONFIGURARE'
                 AND sf.import_id=(SELECT MAX(sf2.import_id) FROM rn_source_file sf2 WHERE sf2.season_id=s.season_id AND sf2.source_type='FCM')) +
              (SELECT COUNT(*) FROM rn_team_mapping m
               JOIN rn_team_season s ON s.team_season_id=m.team_season_id
               JOIN rn_source_file sf ON sf.source_file_id=s.source_file_id
               WHERE s.season_id=? AND m.mapping_status='DA_CONFIGURARE'
                 AND sf.import_id=(SELECT MAX(sf2.import_id) FROM rn_source_file sf2 WHERE sf2.season_id=s.season_id AND sf2.source_type='FCM'))
            """;
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + database);
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (SeasonSource season : managed) {
                if (season.id().equals(currentId)) continue;
                ps.setString(1, season.id()); ps.setString(2, season.id());
                try (ResultSet rs = ps.executeQuery()) {
                    int pending = rs.next() ? rs.getInt(1) : 0;
                    if (pending > 0) {
                        throw new IllegalStateException(season.id() + ": restano " + pending
                            + " associazioni da configurare. Aprire Configurazione prima di elaborare.");
                    }
                }
            }
        }
    }

    private static void validateManagedSource(SeasonSource s) {
        if (s.fcm().isBlank() || !Files.isRegularFile(Path.of(s.fcm()))) {
            throw new IllegalStateException(s.id() + ": file FCM non trovato: " + s.fcm());
        }
        if (s.fca().isBlank() || !Files.isRegularFile(Path.of(s.fca()))) {
            throw new IllegalStateException(s.id() + ": file FCA non trovato: " + s.fca());
        }
    }
    private static String elapsed(long started) {
        double seconds = (System.nanoTime() - started) / 1_000_000_000.0;
        return String.format(java.util.Locale.ROOT, "%.3f s", seconds);
    }

}
