package it.alterlega.recordsnext;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Importa le date delle giornate dai DataA-AAAA.js della configurazione
 * di ConfrontiStorici. Non apre file FCM/FCA e non modifica gli export.
 */
public final class ConfrontiStoriciCalendarImporter {

    private static final String CONFIG_KEY = "confrontistorici_data_directory";
    private static final Pattern SEASON_PATTERN = Pattern.compile("^(\\d{4})_(\\d{4})$");
    private static final Pattern DATE_LINE_PATTERN = Pattern.compile(
        "(?m)^\\s*dataGiornata\\s*\\[\\s*(\\d+)\\s*]\\s*=\\s*[\"']([^\"']+)[\"']\\s*;?\\s*$"
    );
    private static final DateTimeFormatter DATA_A_DATE_FORMAT = new DateTimeFormatterBuilderSafe()
        .dateFormatter();
    private static final DateTimeFormatter DATA_A_DATE_TIME_FORMAT = new DateTimeFormatterBuilderSafe()
        .dateTimeFormatter();

    private ConfrontiStoriciCalendarImporter() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            usage();
            System.exit(2);
        }

        Path database = Path.of(args[0]).toAbsolutePath().normalize();
        String command = args[1].trim().toLowerCase(Locale.ROOT);

        Class.forName("org.sqlite.JDBC");
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + database)) {
            configure(connection);
            installSchema(connection);

            switch (command) {
                case "set-directory" -> setDirectory(connection, args);
                case "resolve" -> resolveCommand(connection, args);
                case "inspect" -> inspectCommand(connection, args);
                case "import" -> importCommand(connection, args);
                case "show" -> showCommand(connection, args);
                case "validate" -> validateCommand(connection, args);
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
                CREATE TABLE IF NOT EXISTS rn_matchday_date (
                    season_id TEXT NOT NULL,
                    serie_a_round INTEGER NOT NULL CHECK (serie_a_round > 0),
                    match_date TEXT NOT NULL,
                    source_path TEXT NOT NULL,
                    source_sha256 TEXT NOT NULL,
                    imported_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (season_id, serie_a_round),
                    FOREIGN KEY (season_id) REFERENCES rn_season(season_id)
                )
                """);

            statement.execute("""
                CREATE INDEX IF NOT EXISTS ix_rn_matchday_date_date
                ON rn_matchday_date(match_date)
                """);
        }

        addColumnIfMissing(connection, "rn_matchday_date", "match_time", "TEXT");
        addColumnIfMissing(connection, "rn_matchday_date", "match_datetime", "TEXT");

        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE INDEX IF NOT EXISTS ix_rn_matchday_date_datetime
                ON rn_matchday_date(match_datetime)
                """);
        }
    }

    private static void addColumnIfMissing(
            Connection connection,
            String table,
            String column,
            String definition) throws Exception {

        boolean present = false;
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (result.next()) {
                if (column.equalsIgnoreCase(result.getString("name"))) {
                    present = true;
                    break;
                }
            }
        }

        if (!present) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("ALTER TABLE " + table + " ADD COLUMN "
                    + column + " " + definition);
            }
        }
    }

    private static void setDirectory(Connection connection, String[] args) throws Exception {
        requireArgCount(args, 3, "<db> set-directory <directory-config-ConfrontiStorici>");
        Path directory = Path.of(args[2]).toAbsolutePath().normalize();
        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException("Directory non trovata: " + directory);
        }

        try (PreparedStatement statement = connection.prepareStatement("""
            INSERT INTO rn_global_configuration(config_key, config_value, updated_at)
            VALUES (?, ?, CURRENT_TIMESTAMP)
            ON CONFLICT(config_key) DO UPDATE SET
                config_value = excluded.config_value,
                updated_at = CURRENT_TIMESTAMP
            """)) {
            statement.setString(1, CONFIG_KEY);
            statement.setString(2, directory.toString());
            statement.executeUpdate();
        }

        System.out.println("Directory ConfrontiStorici configurata: " + directory);
    }

    private static void resolveCommand(Connection connection, String[] args) throws Exception {
        requireArgCount(args, 3, "<db> resolve <stagione>");
        String season = requireSeason(connection, args[2]);
        Path file = resolveFile(connection, season);
        System.out.println(file);
        System.out.println(Files.isRegularFile(file) ? "TROVATO" : "MANCANTE");
    }

    private static void inspectCommand(Connection connection, String[] args) throws Exception {
        requireArgCount(args, 3, "<db> inspect <stagione>");
        String season = requireSeason(connection, args[2]);
        Inspection inspection = inspect(resolveExistingFile(connection, season), season);
        printInspection(inspection);
    }

    private static void importCommand(Connection connection, String[] args) throws Exception {
        requireArgCount(args, 3, "<db> import <stagione>");
        String season = requireSeason(connection, args[2]);
        Inspection inspection = inspect(resolveExistingFile(connection, season), season);

        connection.setAutoCommit(false);
        try {
            try (PreparedStatement delete = connection.prepareStatement(
                    "DELETE FROM rn_matchday_date WHERE season_id = ?")) {
                delete.setString(1, season);
                delete.executeUpdate();
            }

            try (PreparedStatement insert = connection.prepareStatement("""
                INSERT INTO rn_matchday_date (
                    season_id, serie_a_round, match_date,
                    match_time, match_datetime,
                    source_path, source_sha256, imported_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """)) {
                for (MatchdayDate item : inspection.dates()) {
                    insert.setString(1, season);
                    insert.setInt(2, item.round());
                    insert.setString(3, item.date().toString());
                    insert.setString(4, item.time() == null ? null : item.time().toString());
                    insert.setString(5, item.dateTime() == null
                        ? null : item.dateTime().toString());
                    insert.setString(6, inspection.file().toString());
                    insert.setString(7, inspection.sha256());
                    insert.addBatch();
                }
                insert.executeBatch();
            }

            connection.commit();
        } catch (Exception exception) {
            connection.rollback();
            throw exception;
        } finally {
            connection.setAutoCommit(true);
        }

        System.out.printf(
            Locale.ROOT,
            "Importate %d giornate per %s da %s%n",
            inspection.dates().size(), season, inspection.file()
        );
    }

    private static void showCommand(Connection connection, String[] args) throws Exception {
        requireArgCount(args, 3, "<db> show <stagione>");
        String season = requireSeason(connection, args[2]);

        try (PreparedStatement statement = connection.prepareStatement("""
            SELECT serie_a_round, match_date, match_time,
                   match_datetime, source_path, source_sha256
            FROM rn_matchday_date
            WHERE season_id = ?
            ORDER BY serie_a_round
            """)) {
            statement.setString(1, season);
            try (ResultSet result = statement.executeQuery()) {
                int count = 0;
                while (result.next()) {
                    count++;
                    System.out.printf(
                        Locale.ROOT,
                        "%2d  %s%n",
                        result.getInt("serie_a_round"),
                        result.getString("match_datetime") != null
                            ? result.getString("match_datetime")
                            : result.getString("match_date")
                    );
                }
                if (count == 0) {
                    System.out.println("Nessuna data importata per " + season);
                }
            }
        }
    }

    private static void validateCommand(Connection connection, String[] args) throws Exception {
        requireArgCount(args, 3, "<db> validate <stagione>");
        String season = requireSeason(connection, args[2]);
        Path file = resolveExistingFile(connection, season);
        Inspection current = inspect(file, season);

        String sql = """
            SELECT COUNT(*) AS total,
                   COUNT(DISTINCT serie_a_round) AS distinct_rounds,
                   MIN(serie_a_round) AS first_round,
                   MAX(serie_a_round) AS last_round,
                   MIN(source_sha256) AS min_hash,
                   MAX(source_sha256) AS max_hash
            FROM rn_matchday_date
            WHERE season_id = ?
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, season);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                int total = result.getInt("total");
                int distinct = result.getInt("distinct_rounds");
                int first = result.getInt("first_round");
                int last = result.getInt("last_round");
                String minHash = result.getString("min_hash");
                String maxHash = result.getString("max_hash");

                List<String> errors = new ArrayList<>();
                if (total == 0) {
                    errors.add("nessuna data importata");
                }
                if (total != distinct) {
                    errors.add("giornate duplicate nel database");
                }
                if (total > 0 && (first != 1 || last != total)) {
                    errors.add("sequenza database non continua: " + first + ".." + last);
                }
                if (total != current.dates().size()) {
                    errors.add("numero date diverso dal file: db=" + total
                        + ", file=" + current.dates().size());
                }
                if (minHash != null && (!minHash.equals(maxHash)
                        || !minHash.equals(current.sha256()))) {
                    errors.add("file DataA.js cambiato dopo l'importazione");
                }

                if (!errors.isEmpty()) {
                    System.out.println(season + " NON VALIDA");
                    errors.forEach(error -> System.out.println("- " + error));
                    System.exit(1);
                }
                System.out.println(season + " VALIDA");
                System.out.println("Giornate: " + total);
                System.out.println("SHA-256 : " + current.sha256());
            }
        }
    }

    private static Inspection inspect(Path file, String season) throws Exception {
        byte[] bytes = Files.readAllBytes(file);
        String text = decode(bytes);
        Map<Integer, MatchdayDate> parsed = new TreeMap<>();
        Matcher matcher = DATE_LINE_PATTERN.matcher(text);

        while (matcher.find()) {
            int round = Integer.parseInt(matcher.group(1));
            String rawValue = matcher.group(2).trim();
            MatchdayDate parsedValue;
            try {
                LocalDateTime dateTime = LocalDateTime.parse(
                    rawValue,
                    DATA_A_DATE_TIME_FORMAT
                );
                parsedValue = new MatchdayDate(
                    round,
                    dateTime.toLocalDate(),
                    dateTime.toLocalTime(),
                    dateTime
                );
            } catch (DateTimeParseException dateTimeException) {
                try {
                    LocalDate date = LocalDate.parse(rawValue, DATA_A_DATE_FORMAT);
                    parsedValue = new MatchdayDate(round, date, null, null);
                } catch (DateTimeParseException dateException) {
                    throw new IllegalArgumentException(
                        "Data/ora non valida alla giornata " + round + ": " + rawValue,
                        dateTimeException
                    );
                }
            }
            MatchdayDate previous = parsed.putIfAbsent(round, parsedValue);
            if (previous != null) {
                throw new IllegalArgumentException("Giornata duplicata nel file: " + round);
            }
        }

        if (parsed.isEmpty()) {
            throw new IllegalArgumentException(
                "Nessuna assegnazione dataGiornata[n] trovata in " + file
            );
        }

        int expected = 1;
        for (int round : parsed.keySet()) {
            if (round != expected) {
                throw new IllegalArgumentException(
                    "Sequenza giornate non continua: attesa " + expected + ", trovata " + round
                );
            }
            expected++;
        }

        SeasonYears years = parseSeason(season);
        List<MatchdayDate> dates = parsed.values().stream()
            .sorted(Comparator.comparingInt(MatchdayDate::round))
            .toList();

        for (MatchdayDate item : dates) {
            int year = item.date().getYear();
            if (year != years.startYear() && year != years.endYear()) {
                throw new IllegalArgumentException(
                    "Data fuori stagione alla giornata " + item.round() + ": " + item.date()
                );
            }
        }

        return new Inspection(file, sha256(bytes), dates);
    }

    private static void printInspection(Inspection inspection) {
        MatchdayDate first = inspection.dates().getFirst();
        MatchdayDate last = inspection.dates().getLast();
        System.out.println("File     : " + inspection.file());
        System.out.println("Giornate : " + inspection.dates().size());
        System.out.println("Prima    : " + first.round() + " -> " + first.displayValue());
        System.out.println("Ultima   : " + last.round() + " -> " + last.displayValue());
        System.out.println("SHA-256  : " + inspection.sha256());
    }

    private static Path resolveExistingFile(Connection connection, String season)
            throws Exception {
        Path file = resolveFile(connection, season);
        if (!Files.isRegularFile(file)) {
            throw new IllegalArgumentException("DataA non trovato: " + file);
        }
        return file;
    }

    private static Path resolveFile(Connection connection, String season) throws Exception {
        SeasonYears years = parseSeason(season);
        Path directory = configuredDirectory(connection);
        return directory.resolve("DataA-" + years.startYear() + ".js")
            .toAbsolutePath().normalize();
    }

    private static Path configuredDirectory(Connection connection) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement("""
            SELECT config_value
            FROM rn_global_configuration
            WHERE config_key = ?
            """)) {
            statement.setString(1, CONFIG_KEY);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalStateException(
                        "Directory ConfrontiStorici non configurata. "
                            + "Usare set-directory."
                    );
                }
                Path directory = Path.of(result.getString(1))
                    .toAbsolutePath().normalize();
                if (!Files.isDirectory(directory)) {
                    throw new IllegalStateException(
                        "Directory ConfrontiStorici non disponibile: " + directory
                    );
                }
                return directory;
            }
        }
    }

    private static String requireSeason(Connection connection, String value) throws Exception {
        String season = value.trim();
        parseSeason(season);
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

    private static SeasonYears parseSeason(String season) {
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
        return new SeasonYears(start, end);
    }

    private static String decode(byte[] bytes) throws IOException {
        try {
            return StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(java.nio.ByteBuffer.wrap(bytes))
                .toString();
        } catch (java.nio.charset.CharacterCodingException exception) {
            return Charset.forName("windows-1252").decode(
                java.nio.ByteBuffer.wrap(bytes)
            ).toString();
        }
    }

    private static String sha256(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
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
        System.err.println("  <db> set-directory <directory-config-ConfrontiStorici>");
        System.err.println("  <db> resolve <stagione>");
        System.err.println("  <db> inspect <stagione>");
        System.err.println("  <db> import <stagione>");
        System.err.println("  <db> show <stagione>");
        System.err.println("  <db> validate <stagione>");
    }

    private record MatchdayDate(
        int round,
        LocalDate date,
        LocalTime time,
        LocalDateTime dateTime
    ) {
        String displayValue() {
            return dateTime == null ? date.toString() : dateTime.toString();
        }
    }

    private record Inspection(Path file, String sha256, List<MatchdayDate> dates) {
    }

    private record SeasonYears(int startYear, int endYear) {
    }

    /** Isola la costruzione dei formatter per i DataA.js storici. */
    private static final class DateTimeFormatterBuilderSafe {
        DateTimeFormatter dateFormatter() {
            return new java.time.format.DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("MMMM d uuuu")
                .toFormatter(Locale.ENGLISH);
        }

        DateTimeFormatter dateTimeFormatter() {
            return new java.time.format.DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("MMMM d uuuu H:mm")
                .toFormatter(Locale.ENGLISH);
        }
    }
}
