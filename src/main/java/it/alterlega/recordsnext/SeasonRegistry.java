package it.alterlega.recordsnext;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Registro autonomo delle stagioni e delle relative risorse esterne.
 *
 * <p>Opera esclusivamente sul database SQLite gia importato. Non apre e non
 * modifica file FCM/FCA. Lo schema viene installato in modo idempotente solo
 * quando questa classe viene eseguita.</p>
 */
public final class SeasonRegistry {

    private static final Pattern SEASON_PATTERN =
        Pattern.compile("^(\\d{4})_(\\d{4})$");

    private SeasonRegistry() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            printUsage();
            System.exit(2);
        }

        Path database = Path.of(args[0]).toAbsolutePath().normalize();
        if (!Files.isRegularFile(database)) {
            throw new IllegalArgumentException(
                "Database SQLite non trovato: " + database
            );
        }

        String command = args[1].trim().toLowerCase(Locale.ROOT);
        Class.forName("org.sqlite.JDBC");

        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + database)) {

            configureConnection(connection);
            installSchema(connection);

            switch (command) {
                case "show" -> show(connection);
                case "set-managed" -> setManaged(connection, args);
                case "set-manual" -> setManual(connection, args);
                case "set-sites" -> setSites(connection, args);
                case "validate" -> validateCommand(connection, args);
                default -> {
                    printUsage();
                    System.exit(2);
                }
            }
        }
    }

    static void installSchema(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE IF NOT EXISTS rn_season_configuration (
                    season_id TEXT PRIMARY KEY,
                    management_type TEXT NOT NULL
                        CHECK (management_type IN ('GESTITA', 'MANUALE')),
                    local_site_path TEXT,
                    online_site_url TEXT,
                    dataa_path TEXT,
                    configuration_status TEXT NOT NULL DEFAULT 'DA_CONFIGURARE'
                        CHECK (
                            configuration_status IN (
                                'DA_CONFIGURARE',
                                'IN_CORSO',
                                'COMPLETA'
                            )
                        ),
                    created_at TEXT NOT NULL,
                    updated_at TEXT NOT NULL,
                    FOREIGN KEY (season_id)
                        REFERENCES rn_season(season_id)
                )
                """);

            statement.execute("""
                CREATE INDEX IF NOT EXISTS ix_rn_season_configuration_status
                ON rn_season_configuration(configuration_status)
                """);
        }
    }

    private static void show(Connection connection) throws Exception {
        String sql = """
            SELECT
                s.season_id,
                s.is_anchor,
                COALESCE(c.management_type, 'NON_CONFIGURATA') AS tipo,
                COALESCE(c.configuration_status, 'DA_CONFIGURARE') AS stato,
                c.local_site_path,
                c.online_site_url,
                c.dataa_path,
                (SELECT COUNT(*)
                 FROM rn_source_file f
                 WHERE f.season_id = s.season_id
                   AND f.source_type = 'FCM') AS fcm,
                (SELECT COUNT(*)
                 FROM rn_source_file f
                 WHERE f.season_id = s.season_id
                   AND f.source_type = 'FCA') AS fca
            FROM rn_season s
            LEFT JOIN rn_season_configuration c
              ON c.season_id = s.season_id
            ORDER BY CAST(SUBSTR(s.season_id, 1, 4) AS INTEGER) DESC,
                     s.season_id DESC
            """;

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {

            System.out.printf(
                Locale.ROOT,
                "%-11s %-6s %-17s %-15s %3s %3s  %s%n",
                "STAGIONE", "ANCORA", "TIPO", "STATO", "FCM", "FCA",
                "RISORSE"
            );

            while (result.next()) {
                String resources = resourcesSummary(result);
                System.out.printf(
                    Locale.ROOT,
                    "%-11s %-6s %-17s %-15s %3d %3d  %s%n",
                    result.getString("season_id"),
                    result.getInt("is_anchor") == 1 ? "SI" : "NO",
                    result.getString("tipo"),
                    result.getString("stato"),
                    result.getInt("fcm"),
                    result.getInt("fca"),
                    resources
                );
            }
        }
    }

    private static void setManaged(Connection connection, String[] args)
            throws Exception {

        requireArgumentCount(
            args,
            6,
            "<db> set-managed <stagione> <sito-locale|-> "
                + "<sito-online|-> <DataA.js|AUTO|->"
        );

        String seasonId = requireExistingSeason(connection, args[2]);
        SiteValues sites = parseSites(args[3], args[4], args[5]);

        Validation validation = validateManagedSources(connection, seasonId);
        if (!validation.valid()) {
            throw new IllegalStateException(validation.message());
        }

        inTransaction(connection, () -> {
            upsertConfiguration(
                connection,
                seasonId,
                "GESTITA",
                sites,
                calculateStatus(connection, seasonId, "GESTITA")
            );
        });

        System.out.println("Stagione gestita registrata: " + seasonId);
        printSites(sites);
    }

    private static void setManual(Connection connection, String[] args)
            throws Exception {

        requireArgumentCount(
            args,
            6,
            "<db> set-manual <stagione> <sito-locale|-> "
                + "<sito-online|-> <DataA.js|AUTO|->"
        );

        String seasonId = requireValidSeasonId(args[2]);
        SiteValues sites = parseSites(args[3], args[4], args[5]);

        inTransaction(connection, () -> {
            ensureManualSeasonCanBeUsed(connection, seasonId);
            insertSeasonIfMissing(connection, seasonId);
            upsertConfiguration(
                connection,
                seasonId,
                "MANUALE",
                sites,
                "COMPLETA"
            );
        });

        System.out.println("Stagione manuale registrata: " + seasonId);
        printSites(sites);
    }

    private static void setSites(Connection connection, String[] args)
            throws Exception {

        requireArgumentCount(
            args,
            6,
            "<db> set-sites <stagione> <sito-locale|-> "
                + "<sito-online|-> <DataA.js|AUTO|->"
        );

        String seasonId = requireExistingSeason(connection, args[2]);
        SiteValues sites = parseSites(args[3], args[4], args[5]);
        String managementType = requireConfiguredType(connection, seasonId);

        inTransaction(connection, () -> {
            upsertConfiguration(
                connection,
                seasonId,
                managementType,
                sites,
                calculateStatus(connection, seasonId, managementType)
            );
        });

        System.out.println("Risorse stagione aggiornate: " + seasonId);
        printSites(sites);
    }

    private static void validateCommand(Connection connection, String[] args)
            throws Exception {

        requireArgumentCount(args, 3, "<db> validate <stagione>");
        String seasonId = requireExistingSeason(connection, args[2]);
        String managementType = requireConfiguredType(connection, seasonId);

        Validation validation = validateSeason(
            connection,
            seasonId,
            managementType
        );

        if (!validation.valid()) {
            System.out.println(seasonId + "  NON VALIDA");
            System.out.println(validation.message());
            System.exit(1);
        }

        String status = calculateStatus(connection, seasonId, managementType);
        updateStoredStatus(connection, seasonId, status);

        System.out.println(seasonId + "  VALIDA");
        System.out.println("Tipo  : " + managementType);
        System.out.println("Stato : " + status);
    }

    private static Validation validateSeason(
            Connection connection,
            String seasonId,
            String managementType) throws Exception {

        if (managementType.equals("GESTITA")) {
            Validation sources = validateManagedSources(connection, seasonId);
            if (!sources.valid()) {
                return sources;
            }
        } else if (countSources(connection, seasonId) != 0) {
            return Validation.error(
                "La stagione manuale " + seasonId
                    + " possiede sorgenti FCM/FCA importate."
            );
        }

        SiteValues sites = readSites(connection, seasonId);
        try {
            validateStoredSites(sites);
        } catch (IllegalArgumentException exception) {
            return Validation.error(exception.getMessage());
        }

        return Validation.ok();
    }

    private static Validation validateManagedSources(
            Connection connection,
            String seasonId) throws Exception {

        SourceCount fcm = readSourceCount(connection, seasonId, "FCM");
        SourceCount fca = readSourceCount(connection, seasonId, "FCA");

        if (fcm.configured() != 1 || fca.configured() != 1) {
            return Validation.error(
                "La stagione " + seasonId
                    + " deve avere esattamente un FCM e un FCA in "
                    + "rn_source_file; trovati FCM=" + fcm.configured()
                    + ", FCA=" + fca.configured() + "."
            );
        }

        if (fcm.completedImports() != 1 || fca.completedImports() != 1) {
            return Validation.error(
                "Le sorgenti della stagione " + seasonId
                    + " non corrispondono a importazioni COMPLETED univoche; "
                    + "FCM=" + fcm.completedImports()
                    + ", FCA=" + fca.completedImports() + "."
            );
        }

        return Validation.ok();
    }

    private static SourceCount readSourceCount(
            Connection connection,
            String seasonId,
            String sourceType) throws Exception {

        String sql = """
            SELECT
                COUNT(*) AS configured_count,
                SUM(CASE WHEN i.status = 'COMPLETED' THEN 1 ELSE 0 END)
                    AS completed_count
            FROM rn_source_file f
            LEFT JOIN rn_import i
              ON i.import_id = f.import_id
            WHERE f.season_id = ?
              AND f.source_type = ?
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, seasonId);
            statement.setString(2, sourceType);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return new SourceCount(
                    result.getInt("configured_count"),
                    result.getInt("completed_count")
                );
            }
        }
    }

    private static int countSources(
            Connection connection,
            String seasonId) throws Exception {

        try (PreparedStatement statement = connection.prepareStatement("""
            SELECT COUNT(*)
            FROM rn_source_file
            WHERE season_id = ?
            """)) {
            statement.setString(1, seasonId);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1);
            }
        }
    }

    private static String calculateStatus(
            Connection connection,
            String seasonId,
            String managementType) throws Exception {

        if (managementType.equals("MANUALE")) {
            return "COMPLETA";
        }

        Validation sources = validateManagedSources(connection, seasonId);
        if (!sources.valid()) {
            return "DA_CONFIGURARE";
        }

        long pendingMappings = countPendingMappings(connection, seasonId);
        return pendingMappings == 0 ? "COMPLETA" : "IN_CORSO";
    }

    private static long countPendingMappings(
            Connection connection,
            String seasonId) throws Exception {

        String sql = """
            SELECT
                (SELECT COUNT(*)
                 FROM rn_competition_mapping cm
                 JOIN rn_competition_season cs
                   ON cs.competition_season_id = cm.competition_season_id
                 WHERE cs.season_id = ?
                   AND cm.mapping_status = 'DA_CONFIGURARE')
                +
                (SELECT COUNT(*)
                 FROM rn_team_mapping tm
                 JOIN rn_team_season ts
                   ON ts.team_season_id = tm.team_season_id
                 WHERE ts.season_id = ?
                   AND tm.mapping_status = 'DA_CONFIGURARE')
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, seasonId);
            statement.setString(2, seasonId);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getLong(1);
            }
        }
    }

    private static void ensureManualSeasonCanBeUsed(
            Connection connection,
            String seasonId) throws Exception {

        if (countSources(connection, seasonId) != 0) {
            throw new IllegalStateException(
                "La stagione " + seasonId
                    + " possiede gia sorgenti importate e non puo essere "
                    + "registrata come MANUALE."
            );
        }

        try (PreparedStatement statement = connection.prepareStatement("""
            SELECT is_anchor
            FROM rn_season
            WHERE season_id = ?
            """)) {
            statement.setString(1, seasonId);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next() && result.getInt("is_anchor") == 1) {
                    throw new IllegalStateException(
                        "La stagione ancora non puo essere MANUALE: "
                            + seasonId
                    );
                }
            }
        }
    }

    private static void insertSeasonIfMissing(
            Connection connection,
            String seasonId) throws Exception {

        int startYear = startYear(seasonId);
        String now = Instant.now().toString();

        try (PreparedStatement statement = connection.prepareStatement("""
            INSERT INTO rn_season (
                season_id,
                display_name,
                sort_order,
                is_anchor,
                created_at,
                updated_at
            ) VALUES (?, ?, ?, 0, ?, ?)
            ON CONFLICT(season_id) DO NOTHING
            """)) {
            statement.setString(1, seasonId);
            statement.setString(2, seasonId.replace('_', '/'));
            statement.setInt(3, startYear);
            statement.setString(4, now);
            statement.setString(5, now);
            statement.executeUpdate();
        }
    }

    private static void upsertConfiguration(
            Connection connection,
            String seasonId,
            String managementType,
            SiteValues sites,
            String status) throws Exception {

        String now = Instant.now().toString();

        try (PreparedStatement statement = connection.prepareStatement("""
            INSERT INTO rn_season_configuration (
                season_id,
                management_type,
                local_site_path,
                online_site_url,
                dataa_path,
                configuration_status,
                created_at,
                updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(season_id) DO UPDATE SET
                management_type = excluded.management_type,
                local_site_path = excluded.local_site_path,
                online_site_url = excluded.online_site_url,
                dataa_path = excluded.dataa_path,
                configuration_status = excluded.configuration_status,
                updated_at = excluded.updated_at
            """)) {
            statement.setString(1, seasonId);
            statement.setString(2, managementType);
            statement.setString(3, sites.localSite());
            statement.setString(4, sites.onlineSite());
            statement.setString(5, sites.dataA());
            statement.setString(6, status);
            statement.setString(7, now);
            statement.setString(8, now);
            statement.executeUpdate();
        }
    }

    private static void updateStoredStatus(
            Connection connection,
            String seasonId,
            String status) throws Exception {

        try (PreparedStatement statement = connection.prepareStatement("""
            UPDATE rn_season_configuration
            SET configuration_status = ?,
                updated_at = ?
            WHERE season_id = ?
            """)) {
            statement.setString(1, status);
            statement.setString(2, Instant.now().toString());
            statement.setString(3, seasonId);
            statement.executeUpdate();
        }
    }

    private static String requireConfiguredType(
            Connection connection,
            String seasonId) throws Exception {

        try (PreparedStatement statement = connection.prepareStatement("""
            SELECT management_type
            FROM rn_season_configuration
            WHERE season_id = ?
            """)) {
            statement.setString(1, seasonId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalStateException(
                        "Stagione non ancora registrata: " + seasonId
                    );
                }
                return result.getString(1);
            }
        }
    }

    private static SiteValues readSites(
            Connection connection,
            String seasonId) throws Exception {

        try (PreparedStatement statement = connection.prepareStatement("""
            SELECT local_site_path, online_site_url, dataa_path
            FROM rn_season_configuration
            WHERE season_id = ?
            """)) {
            statement.setString(1, seasonId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalStateException(
                        "Stagione non ancora registrata: " + seasonId
                    );
                }
                return new SiteValues(
                    result.getString(1),
                    result.getString(2),
                    result.getString(3)
                );
            }
        }
    }

    private static SiteValues parseSites(
            String localArgument,
            String onlineArgument,
            String dataAArgument) {

        String local = nullable(localArgument);
        String online = nullable(onlineArgument);

        if (local != null) {
            Path localPath = Path.of(local).toAbsolutePath().normalize();
            if (!Files.isDirectory(localPath)) {
                throw new IllegalArgumentException(
                    "Cartella sito locale non trovata: " + localPath
                );
            }
            local = localPath.toString();
        }

        validateOnlineUrl(online);

        String dataA;
        if (dataAArgument.trim().equalsIgnoreCase("AUTO")) {
            if (local == null) {
                throw new IllegalArgumentException(
                    "AUTO richiede il percorso del sito locale."
                );
            }
            Path detected = Path.of(local, "js", "DataA.js")
                .toAbsolutePath().normalize();
            if (!Files.isRegularFile(detected)) {
                throw new IllegalArgumentException(
                    "DataA.js non trovato automaticamente: " + detected
                );
            }
            dataA = detected.toString();
        } else {
            dataA = nullable(dataAArgument);
            if (dataA != null) {
                Path dataAPath = Path.of(dataA).toAbsolutePath().normalize();
                if (!Files.isRegularFile(dataAPath)) {
                    throw new IllegalArgumentException(
                        "File DataA.js non trovato: " + dataAPath
                    );
                }
                dataA = dataAPath.toString();
            }
        }

        return new SiteValues(local, online, dataA);
    }

    private static void validateStoredSites(SiteValues sites) {
        if (sites.localSite() != null
                && !Files.isDirectory(Path.of(sites.localSite()))) {
            throw new IllegalArgumentException(
                "Cartella sito locale non piu disponibile: "
                    + sites.localSite()
            );
        }

        validateOnlineUrl(sites.onlineSite());

        if (sites.dataA() != null
                && !Files.isRegularFile(Path.of(sites.dataA()))) {
            throw new IllegalArgumentException(
                "File DataA.js non piu disponibile: " + sites.dataA()
            );
        }
    }

    private static void validateOnlineUrl(String value) {
        if (value == null) {
            return;
        }

        URI uri;
        try {
            uri = URI.create(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                "URL sito online non valido: " + value,
                exception
            );
        }

        String scheme = uri.getScheme();
        if (scheme == null
                || !(scheme.equalsIgnoreCase("http")
                    || scheme.equalsIgnoreCase("https"))
                || uri.getHost() == null) {
            throw new IllegalArgumentException(
                "URL sito online non valido: " + value
            );
        }
    }

    private static String requireExistingSeason(
            Connection connection,
            String value) throws Exception {

        String seasonId = requireValidSeasonId(value);

        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT COUNT(*) FROM rn_season WHERE season_id = ?")) {
            statement.setString(1, seasonId);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                if (result.getInt(1) != 1) {
                    throw new IllegalArgumentException(
                        "Stagione non trovata: " + seasonId
                    );
                }
            }
        }

        return seasonId;
    }

    private static String requireValidSeasonId(String value) {
        String seasonId = value.trim();
        Matcher matcher = SEASON_PATTERN.matcher(seasonId);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                "Formato stagione non valido: " + seasonId
                    + ". Atteso AAAA_AAAA."
            );
        }

        int start = Integer.parseInt(matcher.group(1));
        int end = Integer.parseInt(matcher.group(2));
        if (end != start + 1) {
            throw new IllegalArgumentException(
                "Stagione non consecutiva: " + seasonId
            );
        }
        return seasonId;
    }

    private static int startYear(String seasonId) {
        return Integer.parseInt(seasonId.substring(0, 4));
    }

    private static String resourcesSummary(ResultSet result) throws Exception {
        StringBuilder value = new StringBuilder();
        appendResource(value, "locale", result.getString("local_site_path"));
        appendResource(value, "online", result.getString("online_site_url"));
        appendResource(value, "DataA", result.getString("dataa_path"));
        return value.length() == 0 ? "-" : value.toString();
    }

    private static void appendResource(
            StringBuilder builder,
            String label,
            String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (builder.length() > 0) {
            builder.append(" | ");
        }
        builder.append(label).append('=').append(value);
    }

    private static void printSites(SiteValues sites) {
        System.out.println(
            "Locale: " + displayNullable(sites.localSite())
        );
        System.out.println(
            "Online: " + displayNullable(sites.onlineSite())
        );
        System.out.println(
            "DataA : " + displayNullable(sites.dataA())
        );
    }

    private static String displayNullable(String value) {
        return value == null ? "-" : value;
    }

    private static String nullable(String value) {
        String trimmed = value.trim();
        return trimmed.isBlank() || trimmed.equals("-") ? null : trimmed;
    }

    private static void requireArgumentCount(
            String[] args,
            int expected,
            String usage) {
        if (args.length != expected) {
            throw new IllegalArgumentException("Uso: " + usage);
        }
    }

    private static void configureConnection(Connection connection)
            throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("PRAGMA busy_timeout = 10000");
        }
    }

    private static void inTransaction(
            Connection connection,
            SqlOperation operation) throws Exception {

        boolean originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            operation.run();
            connection.commit();
        } catch (Exception exception) {
            connection.rollback();
            throw exception;
        } finally {
            connection.setAutoCommit(originalAutoCommit);
        }
    }

    private static void printUsage() {
        System.err.println("Comandi:");
        System.err.println("  <db> show");
        System.err.println(
            "  <db> set-managed <stagione> <sito-locale|-> "
                + "<sito-online|-> <DataA.js|AUTO|->"
        );
        System.err.println(
            "  <db> set-manual <stagione> <sito-locale|-> "
                + "<sito-online|-> <DataA.js|AUTO|->"
        );
        System.err.println(
            "  <db> set-sites <stagione> <sito-locale|-> "
                + "<sito-online|-> <DataA.js|AUTO|->"
        );
        System.err.println("  <db> validate <stagione>");
    }

    @FunctionalInterface
    private interface SqlOperation {
        void run() throws Exception;
    }

    private record SiteValues(
        String localSite,
        String onlineSite,
        String dataA
    ) {
    }

    private record SourceCount(int configured, int completedImports) {
    }

    private record Validation(boolean valid, String message) {
        static Validation ok() {
            return new Validation(true, "OK");
        }

        static Validation error(String message) {
            return new Validation(false, message);
        }
    }
}
