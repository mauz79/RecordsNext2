package it.alterlega.recordsnext;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ConfigurationSchema {

    private ConfigurationSchema() {
    }

    /**
     * Crea lo schema RecordsNext vuoto per una nuova installazione.
     * Non richiede ancora una stagione-ancora e non importa dati.
     */
    public static void initializeEmpty(Path database) throws Exception {
        Path normalized = database.toAbsolutePath().normalize();
        Path parent = normalized.getParent();
        if (parent != null) {
            java.nio.file.Files.createDirectories(parent);
        }

        Class.forName("org.sqlite.JDBC");
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + normalized)) {
            connection.setAutoCommit(false);
            try {
                configureConnection(connection);
                createSchema(connection);
                connection.commit();
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println(
                "Uso: ConfigurationSchema <recordsnext.db> <stagione-ancora>"
            );
            System.exit(2);
        }

        Path database = Path.of(args[0]).toAbsolutePath().normalize();
        String anchorSeason = args[1].trim();

        if (anchorSeason.isBlank()) {
            throw new IllegalArgumentException(
                "La stagione-ancora non può essere vuota."
            );
        }

        Class.forName("org.sqlite.JDBC");

        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + database)) {

            connection.setAutoCommit(false);

            try {
                configureConnection(connection);
                createSchema(connection);
                importSeasonsAndSources(connection);
                setAnchorSeason(connection, anchorSeason);
                importSeasonEntities(connection);
                createAnchorIdentities(connection, anchorSeason);
                initializeHistoricalMappings(connection, anchorSeason);

                connection.commit();

                printSummary(connection, anchorSeason);
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    private static void configureConnection(Connection connection)
            throws Exception {

        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("PRAGMA busy_timeout = 10000");
        }
    }

    private static void createSchema(Connection connection)
            throws Exception {

        try (Statement statement = connection.createStatement()) {

            statement.execute("""
                CREATE TABLE IF NOT EXISTS rn_season (
                    season_id TEXT PRIMARY KEY,
                    display_name TEXT NOT NULL,
                    sort_order INTEGER,
                    is_anchor INTEGER NOT NULL DEFAULT 0
                        CHECK (is_anchor IN (0, 1)),
                    created_at TEXT NOT NULL,
                    updated_at TEXT NOT NULL
                )
                """);

            statement.execute("""
                CREATE UNIQUE INDEX IF NOT EXISTS
                    ux_rn_season_anchor
                ON rn_season(is_anchor)
                WHERE is_anchor = 1
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS rn_source_file (
                    source_file_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    import_id INTEGER NOT NULL UNIQUE,
                    season_id TEXT NOT NULL,
                    source_type TEXT NOT NULL
                        CHECK (source_type IN ('FCM', 'FCA')),
                    source_path TEXT NOT NULL,
                    source_file_name TEXT NOT NULL,
                    source_size_bytes INTEGER NOT NULL,
                    source_last_modified TEXT NOT NULL,
                    source_sha256 TEXT NOT NULL,
                    imported_at TEXT NOT NULL,
                    FOREIGN KEY (season_id)
                        REFERENCES rn_season(season_id)
                )
                """);

            statement.execute("""
                CREATE INDEX IF NOT EXISTS
                    ix_rn_source_file_season_type
                ON rn_source_file(season_id, source_type)
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS rn_competition_season (
                    competition_season_id INTEGER
                        PRIMARY KEY AUTOINCREMENT,
                    season_id TEXT NOT NULL,
                    source_file_id INTEGER NOT NULL,
                    source_competition_id INTEGER NOT NULL,
                    source_name TEXT NOT NULL,
                    normalized_name TEXT NOT NULL,
                    discovered_at TEXT NOT NULL,
                    UNIQUE (
                        source_file_id,
                        source_competition_id
                    ),
                    FOREIGN KEY (season_id)
                        REFERENCES rn_season(season_id),
                    FOREIGN KEY (source_file_id)
                        REFERENCES rn_source_file(source_file_id)
                )
                """);

            statement.execute("""
                CREATE INDEX IF NOT EXISTS
                    ix_rn_competition_season
                ON rn_competition_season(season_id)
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS rn_team_season (
                    team_season_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    season_id TEXT NOT NULL,
                    source_file_id INTEGER NOT NULL,
                    source_team_id INTEGER NOT NULL,
                    source_name TEXT NOT NULL,
                    normalized_name TEXT NOT NULL,
                    source_division_id INTEGER,
                    source_team_number INTEGER,
                    discovered_at TEXT NOT NULL,
                    UNIQUE (
                        source_file_id,
                        source_team_id
                    ),
                    FOREIGN KEY (season_id)
                        REFERENCES rn_season(season_id),
                    FOREIGN KEY (source_file_id)
                        REFERENCES rn_source_file(source_file_id)
                )
                """);

            statement.execute("""
                CREATE INDEX IF NOT EXISTS
                    ix_rn_team_season
                ON rn_team_season(season_id)
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS rn_competition_identity (
                    competition_identity_id INTEGER
                        PRIMARY KEY AUTOINCREMENT,
                    anchor_season_id TEXT NOT NULL,
                    anchor_competition_season_id INTEGER NOT NULL UNIQUE,
                    canonical_name TEXT NOT NULL,
                    created_at TEXT NOT NULL,
                    FOREIGN KEY (anchor_season_id)
                        REFERENCES rn_season(season_id),
                    FOREIGN KEY (anchor_competition_season_id)
                        REFERENCES rn_competition_season(
                            competition_season_id
                        )
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS rn_team_identity (
                    team_identity_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    anchor_season_id TEXT NOT NULL,
                    anchor_team_season_id INTEGER NOT NULL UNIQUE,
                    canonical_name TEXT NOT NULL,
                    created_at TEXT NOT NULL,
                    FOREIGN KEY (anchor_season_id)
                        REFERENCES rn_season(season_id),
                    FOREIGN KEY (anchor_team_season_id)
                        REFERENCES rn_team_season(team_season_id)
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS rn_competition_mapping (
                    competition_season_id INTEGER PRIMARY KEY,
                    competition_identity_id INTEGER,
                    mapping_status TEXT NOT NULL
                        CHECK (
                            mapping_status IN (
                                'DA_CONFIGURARE',
                                'ASSOCIATA',
                                'NON_ASSOCIATA',
                                'ESCLUSA'
                            )
                        ),
                    mapping_method TEXT,
                    notes TEXT,
                    updated_at TEXT NOT NULL,
                    CHECK (
                        (
                            mapping_status = 'ASSOCIATA'
                            AND competition_identity_id IS NOT NULL
                        )
                        OR
                        (
                            mapping_status <> 'ASSOCIATA'
                            AND competition_identity_id IS NULL
                        )
                    ),
                    FOREIGN KEY (competition_season_id)
                        REFERENCES rn_competition_season(
                            competition_season_id
                        ),
                    FOREIGN KEY (competition_identity_id)
                        REFERENCES rn_competition_identity(
                            competition_identity_id
                        )
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS rn_team_mapping (
                    team_season_id INTEGER PRIMARY KEY,
                    team_identity_id INTEGER,
                    mapping_status TEXT NOT NULL
                        CHECK (
                            mapping_status IN (
                                'DA_CONFIGURARE',
                                'ASSOCIATA',
                                'NON_ASSOCIATA',
                                'ESCLUSA'
                            )
                        ),
                    mapping_method TEXT,
                    notes TEXT,
                    updated_at TEXT NOT NULL,
                    CHECK (
                        (
                            mapping_status = 'ASSOCIATA'
                            AND team_identity_id IS NOT NULL
                        )
                        OR
                        (
                            mapping_status <> 'ASSOCIATA'
                            AND team_identity_id IS NULL
                        )
                    ),
                    FOREIGN KEY (team_season_id)
                        REFERENCES rn_team_season(team_season_id),
                    FOREIGN KEY (team_identity_id)
                        REFERENCES rn_team_identity(team_identity_id)
                )
                """);
        }
    }

    private static void importSeasonsAndSources(
            Connection connection) throws Exception {

        String now = Instant.now().toString();

        String seasonSql = """
            INSERT INTO rn_season (
                season_id,
                display_name,
                sort_order,
                is_anchor,
                created_at,
                updated_at
            )
            SELECT DISTINCT
                season_id,
                season_id,
                NULL,
                0,
                ?,
                ?
            FROM rn_import
            WHERE status = 'COMPLETED'
            ON CONFLICT(season_id) DO UPDATE SET
                updated_at = excluded.updated_at
            """;

        try (PreparedStatement statement =
                 connection.prepareStatement(seasonSql)) {

            statement.setString(1, now);
            statement.setString(2, now);
            statement.executeUpdate();
        }

        String sourceSql = """
            INSERT INTO rn_source_file (
                import_id,
                season_id,
                source_type,
                source_path,
                source_file_name,
                source_size_bytes,
                source_last_modified,
                source_sha256,
                imported_at
            )
            SELECT
                import_id,
                season_id,
                source_type,
                source_path,
                source_file_name,
                source_size_bytes,
                source_last_modified,
                source_sha256,
                COALESCE(completed_at, started_at)
            FROM rn_import i
            WHERE i.status = 'COMPLETED'
              AND i.import_id = (
                  SELECT MAX(i2.import_id)
                  FROM rn_import i2
                  WHERE i2.season_id=i.season_id
                    AND i2.source_type=i.source_type
                    AND i2.status='COMPLETED'
              )
            ON CONFLICT(import_id) DO UPDATE SET
                season_id = excluded.season_id,
                source_type = excluded.source_type,
                source_path = excluded.source_path,
                source_file_name = excluded.source_file_name,
                source_size_bytes = excluded.source_size_bytes,
                source_last_modified = excluded.source_last_modified,
                source_sha256 = excluded.source_sha256,
                imported_at = excluded.imported_at
            """;

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sourceSql);
        }
    }

    private static void setAnchorSeason(
            Connection connection,
            String anchorSeason) throws Exception {

        try (PreparedStatement check = connection.prepareStatement(
                "SELECT COUNT(*) FROM rn_season WHERE season_id = ?")) {

            check.setString(1, anchorSeason);

            try (ResultSet result = check.executeQuery()) {
                result.next();

                if (result.getInt(1) != 1) {
                    throw new IllegalArgumentException(
                        "Stagione-ancora non trovata: " + anchorSeason
                    );
                }
            }
        }

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                "UPDATE rn_season SET is_anchor = 0"
            );
        }

        try (PreparedStatement statement = connection.prepareStatement(
                """
                UPDATE rn_season
                SET is_anchor = 1,
                    updated_at = ?
                WHERE season_id = ?
                """)) {

            statement.setString(1, Instant.now().toString());
            statement.setString(2, anchorSeason);
            statement.executeUpdate();
        }
    }

    private static void importSeasonEntities(
            Connection connection) throws Exception {

        List<FcmSource> sources = readFcmSources(connection);

        for (FcmSource source : sources) {
            String competitionTable = findRawTable(
                connection,
                source.importId(),
                "COMPETIZIONE"
            );

            String teamTable = findRawTable(
                connection,
                source.importId(),
                "FANTASQUADRA"
            );

            importCompetitions(
                connection,
                source,
                competitionTable
            );

            importTeams(
                connection,
                source,
                teamTable
            );
        }
    }

    private static List<FcmSource> readFcmSources(
            Connection connection) throws Exception {

        List<FcmSource> sources = new ArrayList<>();

        String sql = """
            SELECT
                source_file_id,
                import_id,
                season_id
            FROM rn_source_file sf
            WHERE source_type = 'FCM'
              AND sf.import_id = (
                  SELECT MAX(sf2.import_id)
                  FROM rn_source_file sf2
                  WHERE sf2.season_id=sf.season_id
                    AND sf2.source_type='FCM'
              )
            ORDER BY season_id, import_id
            """;

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {

            while (result.next()) {
                sources.add(
                    new FcmSource(
                        result.getLong("source_file_id"),
                        result.getLong("import_id"),
                        result.getString("season_id")
                    )
                );
            }
        }

        return sources;
    }

    private static String findRawTable(
            Connection connection,
            long importId,
            String sourceTableName) throws Exception {

        String sql = """
            SELECT raw_table_name
            FROM rn_table_catalog
            WHERE import_id = ?
              AND UPPER(source_table_name) = ?
            """;

        try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setLong(1, importId);
            statement.setString(
                2,
                sourceTableName.toUpperCase(Locale.ROOT)
            );

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalStateException(
                        "Tabella raw non trovata per import "
                            + importId
                            + ": "
                            + sourceTableName
                    );
                }

                return result.getString("raw_table_name");
            }
        }
    }

    private static void importCompetitions(
            Connection connection,
            FcmSource source,
            String rawTable) throws Exception {

        String sql = """
            INSERT INTO rn_competition_season (
                season_id,
                source_file_id,
                source_competition_id,
                source_name,
                normalized_name,
                discovered_at
            )
            SELECT
                ?,
                ?,
                ID,
                NOME,
                LOWER(TRIM(NOME)),
                ?
            FROM %s
            WHERE ID IS NOT NULL
              AND NOME IS NOT NULL
              AND TRIM(NOME) <> ''
            ON CONFLICT(
                source_file_id,
                source_competition_id
            ) DO UPDATE SET
                source_name = excluded.source_name,
                normalized_name = excluded.normalized_name
            """.formatted(quoteIdentifier(rawTable));

        try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setString(1, source.seasonId());
            statement.setLong(2, source.sourceFileId());
            statement.setString(3, Instant.now().toString());
            statement.executeUpdate();
        }
    }

    private static void importTeams(
            Connection connection,
            FcmSource source,
            String rawTable) throws Exception {

        String sql = """
            INSERT INTO rn_team_season (
                season_id,
                source_file_id,
                source_team_id,
                source_name,
                normalized_name,
                source_division_id,
                source_team_number,
                discovered_at
            )
            SELECT
                ?,
                ?,
                ID,
                NOME,
                LOWER(TRIM(NOME)),
                IDDIVISIONE,
                NUMEROSQUADRA,
                ?
            FROM %s
            WHERE ID IS NOT NULL
              AND NOME IS NOT NULL
              AND TRIM(NOME) <> ''
            ON CONFLICT(
                source_file_id,
                source_team_id
            ) DO UPDATE SET
                source_name = excluded.source_name,
                normalized_name = excluded.normalized_name,
                source_division_id = excluded.source_division_id,
                source_team_number = excluded.source_team_number
            """.formatted(quoteIdentifier(rawTable));

        try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setString(1, source.seasonId());
            statement.setLong(2, source.sourceFileId());
            statement.setString(3, Instant.now().toString());
            statement.executeUpdate();
        }
    }

    private static void createAnchorIdentities(
            Connection connection,
            String anchorSeason) throws Exception {

        String now = Instant.now().toString();

        String competitionIdentitySql = """
            INSERT INTO rn_competition_identity (
                anchor_season_id,
                anchor_competition_season_id,
                canonical_name,
                created_at
            )
            SELECT
                season_id,
                competition_season_id,
                source_name,
                ?
            FROM rn_competition_season
            WHERE season_id = ?
            ON CONFLICT(anchor_competition_season_id)
            DO UPDATE SET
                canonical_name = excluded.canonical_name
            """;

        try (PreparedStatement statement =
                 connection.prepareStatement(competitionIdentitySql)) {

            statement.setString(1, now);
            statement.setString(2, anchorSeason);
            statement.executeUpdate();
        }

        String teamIdentitySql = """
            INSERT INTO rn_team_identity (
                anchor_season_id,
                anchor_team_season_id,
                canonical_name,
                created_at
            )
            SELECT
                season_id,
                team_season_id,
                source_name,
                ?
            FROM rn_team_season
            WHERE season_id = ?
            ON CONFLICT(anchor_team_season_id)
            DO UPDATE SET
                canonical_name = excluded.canonical_name
            """;

        try (PreparedStatement statement =
                 connection.prepareStatement(teamIdentitySql)) {

            statement.setString(1, now);
            statement.setString(2, anchorSeason);
            statement.executeUpdate();
        }

        String anchorCompetitionMappingSql = """
            INSERT INTO rn_competition_mapping (
                competition_season_id,
                competition_identity_id,
                mapping_status,
                mapping_method,
                notes,
                updated_at
            )
            SELECT
                cs.competition_season_id,
                ci.competition_identity_id,
                'ASSOCIATA',
                'ANCHOR_SELF',
                NULL,
                ?
            FROM rn_competition_season cs
            JOIN rn_competition_identity ci
              ON ci.anchor_competition_season_id =
                 cs.competition_season_id
            WHERE cs.season_id = ?
            ON CONFLICT(competition_season_id)
            DO UPDATE SET
                competition_identity_id =
                    excluded.competition_identity_id,
                mapping_status = 'ASSOCIATA',
                mapping_method = 'ANCHOR_SELF',
                notes = NULL,
                updated_at = excluded.updated_at
            """;

        try (PreparedStatement statement =
                 connection.prepareStatement(
                     anchorCompetitionMappingSql
                 )) {

            statement.setString(1, now);
            statement.setString(2, anchorSeason);
            statement.executeUpdate();
        }

        String anchorTeamMappingSql = """
            INSERT INTO rn_team_mapping (
                team_season_id,
                team_identity_id,
                mapping_status,
                mapping_method,
                notes,
                updated_at
            )
            SELECT
                ts.team_season_id,
                ti.team_identity_id,
                'ASSOCIATA',
                'ANCHOR_SELF',
                NULL,
                ?
            FROM rn_team_season ts
            JOIN rn_team_identity ti
              ON ti.anchor_team_season_id =
                 ts.team_season_id
            WHERE ts.season_id = ?
            ON CONFLICT(team_season_id)
            DO UPDATE SET
                team_identity_id = excluded.team_identity_id,
                mapping_status = 'ASSOCIATA',
                mapping_method = 'ANCHOR_SELF',
                notes = NULL,
                updated_at = excluded.updated_at
            """;

        try (PreparedStatement statement =
                 connection.prepareStatement(anchorTeamMappingSql)) {

            statement.setString(1, now);
            statement.setString(2, anchorSeason);
            statement.executeUpdate();
        }
    }

    private static void initializeHistoricalMappings(
            Connection connection,
            String anchorSeason) throws Exception {

        String now = Instant.now().toString();

        String competitionSql = """
            INSERT INTO rn_competition_mapping (
                competition_season_id,
                competition_identity_id,
                mapping_status,
                mapping_method,
                notes,
                updated_at
            )
            SELECT
                competition_season_id,
                NULL,
                'DA_CONFIGURARE',
                NULL,
                NULL,
                ?
            FROM rn_competition_season
            WHERE season_id <> ?
            ON CONFLICT(competition_season_id) DO NOTHING
            """;

        try (PreparedStatement statement =
                 connection.prepareStatement(competitionSql)) {

            statement.setString(1, now);
            statement.setString(2, anchorSeason);
            statement.executeUpdate();
        }

        String teamSql = """
            INSERT INTO rn_team_mapping (
                team_season_id,
                team_identity_id,
                mapping_status,
                mapping_method,
                notes,
                updated_at
            )
            SELECT
                team_season_id,
                NULL,
                'DA_CONFIGURARE',
                NULL,
                NULL,
                ?
            FROM rn_team_season
            WHERE season_id <> ?
            ON CONFLICT(team_season_id) DO NOTHING
            """;

        try (PreparedStatement statement =
                 connection.prepareStatement(teamSql)) {

            statement.setString(1, now);
            statement.setString(2, anchorSeason);
            statement.executeUpdate();
        }
    }

    private static void printSummary(
            Connection connection,
            String anchorSeason) throws Exception {

        System.out.println();
        System.out.println("Configurazione multistagione installata");
        System.out.println("Database       : "
            + connection.getMetaData().getURL());
        System.out.println("Stagione ancora: " + anchorSeason);
        System.out.println();

        printCount(
            connection,
            "Stagioni",
            "SELECT COUNT(*) FROM rn_season"
        );

        printCount(
            connection,
            "Sorgenti",
            "SELECT COUNT(*) FROM rn_source_file"
        );

        printCount(
            connection,
            "Competizioni locali",
            "SELECT COUNT(*) FROM rn_competition_season"
        );

        printCount(
            connection,
            "Squadre locali",
            "SELECT COUNT(*) FROM rn_team_season"
        );

        printCount(
            connection,
            "Identità competizioni",
            "SELECT COUNT(*) FROM rn_competition_identity"
        );

        printCount(
            connection,
            "Identità squadre",
            "SELECT COUNT(*) FROM rn_team_identity"
        );

        printCount(
            connection,
            "Competizioni da configurare",
            """
            SELECT COUNT(*)
            FROM rn_competition_mapping
            WHERE mapping_status = 'DA_CONFIGURARE'
            """
        );

        printCount(
            connection,
            "Squadre da configurare",
            """
            SELECT COUNT(*)
            FROM rn_team_mapping
            WHERE mapping_status = 'DA_CONFIGURARE'
            """
        );
    }

    private static void printCount(
            Connection connection,
            String label,
            String sql) throws Exception {

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {

            result.next();

            System.out.printf(
                Locale.ROOT,
                "%-28s: %d%n",
                label,
                result.getLong(1)
            );
        }
    }

    private static String quoteIdentifier(String value) {
        return "\""
            + value.replace("\"", "\"\"")
            + "\"";
    }

    private record FcmSource(
        long sourceFileId,
        long importId,
        String seasonId
    ) {
    }
}