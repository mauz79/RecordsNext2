package it.alterlega.recordsnext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Risolve i file DataA-AAAA.js senza dipendere da ConfrontiStorici.
 *
 * <p>Priorita: cartella esterna configurata, poi data/calendars del progetto.
 * L'importazione effettiva resta affidata a ConfrontiStoriciCalendarImporter,
 * gia validato. Questa classe registra la provenienza per stagione.</p>
 */
public final class CalendarSourceManager {

    private static final String EXTERNAL_DIRECTORY_KEY = "dataa_external_directory";
    private static final Pattern SEASON_PATTERN =
        Pattern.compile("^(\\d{4})_(\\d{4})$");

    private CalendarSourceManager() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            usage();
            System.exit(2);
        }

        Path database = Path.of(args[0]).toAbsolutePath().normalize();
        if (!Files.isRegularFile(database)) {
            throw new IllegalArgumentException("Database SQLite non trovato: " + database);
        }

        String command = args[1].trim().toLowerCase(Locale.ROOT);
        Class.forName("org.sqlite.JDBC");

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + database)) {
            configure(connection);
            installSchema(connection);

            switch (command) {
                case "set-directory" -> setDirectory(connection, args);
                case "clear-directory" -> clearDirectory(connection, args);
                case "resolve" -> resolveCommand(connection, args);
                case "import" -> importCommand(connection, database, args);
                case "validate" -> validateCommand(connection, database, args);
                case "show" -> showCommand(connection, args);
                default -> {
                    usage();
                    System.exit(2);
                }
            }
        }
    }

    static void installSchema(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE IF NOT EXISTS rn_global_configuration (
                    config_key TEXT PRIMARY KEY,
                    config_value TEXT NOT NULL,
                    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS rn_calendar_source (
                    season_id TEXT PRIMARY KEY,
                    source_type TEXT NOT NULL
                        CHECK (source_type IN ('USER_DIRECTORY', 'BUNDLED')),
                    source_directory TEXT NOT NULL,
                    source_file TEXT NOT NULL,
                    source_sha256 TEXT NOT NULL,
                    imported_at TEXT NOT NULL,
                    FOREIGN KEY (season_id) REFERENCES rn_season(season_id)
                )
                """);
        }
    }

    private static void setDirectory(Connection connection, String[] args) throws Exception {
        requireArgCount(args, 3, "<db> set-directory <cartella-DataA>");
        Path directory = Path.of(args[2]).toAbsolutePath().normalize();
        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException("Cartella DataA non trovata: " + directory);
        }

        try (PreparedStatement statement = connection.prepareStatement("""
            INSERT INTO rn_global_configuration(config_key, config_value, updated_at)
            VALUES (?, ?, CURRENT_TIMESTAMP)
            ON CONFLICT(config_key) DO UPDATE SET
                config_value = excluded.config_value,
                updated_at = CURRENT_TIMESTAMP
            """)) {
            statement.setString(1, EXTERNAL_DIRECTORY_KEY);
            statement.setString(2, directory.toString());
            statement.executeUpdate();
        }

        System.out.println("Cartella DataA esterna configurata: " + directory);
    }

    private static void clearDirectory(Connection connection, String[] args) throws Exception {
        requireArgCount(args, 2, "<db> clear-directory");
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM rn_global_configuration WHERE config_key = ?")) {
            statement.setString(1, EXTERNAL_DIRECTORY_KEY);
            statement.executeUpdate();
        }
        System.out.println("Cartella DataA esterna rimossa. Verra usato il fallback distribuito.");
    }

    private static void resolveCommand(Connection connection, String[] args) throws Exception {
        requireArgCount(args, 4, "<db> resolve <stagione> <project-root>");
        ResolvedSource source = resolve(connection, args[2], Path.of(args[3]));
        printSource(source);
    }

    private static void importCommand(
            Connection connection,
            Path database,
            String[] args) throws Exception {

        requireArgCount(args, 4, "<db> import <stagione> <project-root>");
        String season = requireSeason(connection, args[2]);
        ResolvedSource source = resolve(connection, season, Path.of(args[3]));

        ConfrontiStoriciCalendarImporter.main(new String[] {
            database.toString(), "set-directory", source.directory().toString()
        });
        ConfrontiStoriciCalendarImporter.main(new String[] {
            database.toString(), "import", season
        });

        recordSource(connection, season, source);
        printSource(source);
    }

    private static void validateCommand(
            Connection connection,
            Path database,
            String[] args) throws Exception {

        requireArgCount(args, 4, "<db> validate <stagione> <project-root>");
        String season = requireSeason(connection, args[2]);
        ResolvedSource source = resolve(connection, season, Path.of(args[3]));

        ConfrontiStoriciCalendarImporter.main(new String[] {
            database.toString(), "set-directory", source.directory().toString()
        });
        ConfrontiStoriciCalendarImporter.main(new String[] {
            database.toString(), "validate", season
        });

        verifyRecordedSource(connection, season, source);
        printSource(source);
    }

    private static void showCommand(Connection connection, String[] args) throws Exception {
        requireArgCount(args, 3, "<db> show <stagione>");
        String season = requireSeason(connection, args[2]);

        try (PreparedStatement statement = connection.prepareStatement("""
            SELECT source_type, source_directory, source_file,
                   source_sha256, imported_at
            FROM rn_calendar_source
            WHERE season_id = ?
            """)) {
            statement.setString(1, season);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    System.out.println("Nessuna sorgente calendario registrata per " + season);
                    return;
                }
                System.out.println("Stagione : " + season);
                System.out.println("Origine  : " + result.getString("source_type"));
                System.out.println("Cartella : " + result.getString("source_directory"));
                System.out.println("File     : " + result.getString("source_file"));
                System.out.println("SHA-256  : " + result.getString("source_sha256"));
                System.out.println("Importato: " + result.getString("imported_at"));
            }
        }
    }

    private static ResolvedSource resolve(
            Connection connection,
            String seasonValue,
            Path projectRootValue) throws Exception {

        String season = requireSeason(connection, seasonValue);
        int startYear = startYear(season);
        String fileName = "DataA-" + startYear + ".js";

        Path external = readExternalDirectory(connection);
        if (external != null) {
            Path candidate = external.resolve(fileName).toAbsolutePath().normalize();
            if (Files.isRegularFile(candidate)) {
                return new ResolvedSource(
                    "USER_DIRECTORY", external, candidate, sha256(candidate)
                );
            }
        }

        Path projectRoot = projectRootValue.toAbsolutePath().normalize();
        Path bundledDirectory = projectRoot.resolve("data").resolve("calendars");
        Path bundled = bundledDirectory.resolve(fileName).normalize();
        if (Files.isRegularFile(bundled)) {
            return new ResolvedSource(
                "BUNDLED", bundledDirectory, bundled, sha256(bundled)
            );
        }

        StringBuilder message = new StringBuilder("DataA non trovato per ")
            .append(season).append(". Atteso: ").append(fileName);
        if (external != null) {
            message.append(" in ").append(external);
        }
        message.append(" oppure in ").append(bundledDirectory);
        throw new IllegalArgumentException(message.toString());
    }

    private static Path readExternalDirectory(Connection connection) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement("""
            SELECT config_value
            FROM rn_global_configuration
            WHERE config_key = ?
            """)) {
            statement.setString(1, EXTERNAL_DIRECTORY_KEY);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return null;
                }
                Path directory = Path.of(result.getString(1))
                    .toAbsolutePath().normalize();
                return Files.isDirectory(directory) ? directory : null;
            }
        }
    }

    private static void recordSource(
            Connection connection,
            String season,
            ResolvedSource source) throws Exception {

        try (PreparedStatement statement = connection.prepareStatement("""
            INSERT INTO rn_calendar_source (
                season_id, source_type, source_directory,
                source_file, source_sha256, imported_at
            ) VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT(season_id) DO UPDATE SET
                source_type = excluded.source_type,
                source_directory = excluded.source_directory,
                source_file = excluded.source_file,
                source_sha256 = excluded.source_sha256,
                imported_at = excluded.imported_at
            """)) {
            statement.setString(1, season);
            statement.setString(2, source.type());
            statement.setString(3, source.directory().toString());
            statement.setString(4, source.file().toString());
            statement.setString(5, source.sha256());
            statement.setString(6, Instant.now().toString());
            statement.executeUpdate();
        }
    }

    private static void verifyRecordedSource(
            Connection connection,
            String season,
            ResolvedSource source) throws Exception {

        try (PreparedStatement statement = connection.prepareStatement("""
            SELECT source_type, source_file, source_sha256
            FROM rn_calendar_source
            WHERE season_id = ?
            """)) {
            statement.setString(1, season);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalStateException(
                        "Sorgente calendario non registrata per " + season
                    );
                }
                if (!source.type().equals(result.getString("source_type"))
                        || !source.file().toString().equals(result.getString("source_file"))
                        || !source.sha256().equals(result.getString("source_sha256"))) {
                    throw new IllegalStateException(
                        "La sorgente calendario corrente differisce da quella importata per "
                            + season
                    );
                }
            }
        }
    }

    private static String requireSeason(Connection connection, String value) throws Exception {
        String season = value.trim();
        Matcher matcher = SEASON_PATTERN.matcher(season);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                "Formato stagione non valido, atteso AAAA_AAAA: " + season
            );
        }
        int start = Integer.parseInt(matcher.group(1));
        int end = Integer.parseInt(matcher.group(2));
        if (end != start + 1) {
            throw new IllegalArgumentException("Stagione non consecutiva: " + season);
        }

        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT COUNT(*) FROM rn_season WHERE season_id = ?")) {
            statement.setString(1, season);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                if (result.getInt(1) != 1) {
                    throw new IllegalArgumentException("Stagione non trovata: " + season);
                }
            }
        }
        return season;
    }

    private static int startYear(String season) {
        return Integer.parseInt(season.substring(0, 4));
    }

    private static String sha256(Path file) throws Exception {
        byte[] bytes = Files.readAllBytes(file);
        return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(bytes)
        );
    }

    private static void printSource(ResolvedSource source) {
        System.out.println("Origine  : " + source.type());
        System.out.println("Cartella : " + source.directory());
        System.out.println("File     : " + source.file());
        System.out.println("SHA-256  : " + source.sha256());
    }

    private static void configure(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("PRAGMA busy_timeout = 10000");
        }
    }

    private static void requireArgCount(String[] args, int expected, String usage) {
        if (args.length != expected) {
            throw new IllegalArgumentException("Uso: " + usage);
        }
    }

    private static void usage() {
        System.err.println("Comandi:");
        System.err.println("  <db> set-directory <cartella-DataA>");
        System.err.println("  <db> clear-directory");
        System.err.println("  <db> resolve <stagione> <project-root>");
        System.err.println("  <db> import <stagione> <project-root>");
        System.err.println("  <db> validate <stagione> <project-root>");
        System.err.println("  <db> show <stagione>");
    }

    private record ResolvedSource(
        String type,
        Path directory,
        Path file,
        String sha256
    ) {
    }
}
