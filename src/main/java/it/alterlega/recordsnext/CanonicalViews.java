package it.alterlega.recordsnext;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class CanonicalViews {

    private CanonicalViews() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println(
                "Uso: CanonicalViews <recordsnext.db>"
            );
            System.exit(2);
        }

        Path database = Path.of(args[0])
            .toAbsolutePath()
            .normalize();

        Class.forName("org.sqlite.JDBC");

        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + database)) {

            connection.setAutoCommit(false);

            try {
                createViews(connection);
                connection.commit();
                printAudit(connection);
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    private static void createViews(Connection connection)
            throws Exception {

        dropCanonicalViews(connection);
        createConfiguredEntityViews(connection);

        List<String> seasonEventViews =
            createSeasonEventViews(connection);

        createUnionViews(
            connection,
            seasonEventViews
        );
    }

    private static void dropCanonicalViews(
            Connection connection) throws Exception {

        List<String> generatedViews = new ArrayList<>();

        try (
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("""
                SELECT name
                FROM sqlite_master
                WHERE type = 'view'
                  AND (
                      name LIKE 'rn_event_%'
                      OR name LIKE 'rn_match_%'
                  )
                ORDER BY name
                """)
        ) {
            while (result.next()) {
                generatedViews.add(
                    result.getString("name")
                );
            }
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute(
                "DROP VIEW IF EXISTS rn_playoff_result"
            );

            statement.execute(
                "DROP VIEW IF EXISTS rn_team_match"
            );

            statement.execute(
                "DROP VIEW IF EXISTS rn_team_event"
            );

            statement.execute(
                "DROP VIEW IF EXISTS rn_match"
            );

            statement.execute(
                "DROP VIEW IF EXISTS rn_event"
            );

            for (String viewName : generatedViews) {
                statement.execute(
                    "DROP VIEW IF EXISTS "
                        + quoteIdentifier(viewName)
                );
            }

            statement.execute(
                "DROP VIEW IF EXISTS rn_configured_team"
            );

            statement.execute(
                "DROP VIEW IF EXISTS rn_configured_competition"
            );
        }
    }

    private static void createConfiguredEntityViews(
            Connection connection) throws Exception {

        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE VIEW rn_configured_competition AS
                SELECT
                    cs.competition_season_id,
                    cs.season_id,
                    cs.source_file_id,
                    cs.source_competition_id,
                    cs.source_name,
                    cs.normalized_name,
                    cm.competition_identity_id,
                    ci.canonical_name,
                    cm.mapping_status,
                    cm.mapping_method,
                    cm.notes
                FROM rn_competition_season cs
                JOIN rn_competition_mapping cm
                  ON cm.competition_season_id =
                     cs.competition_season_id
                LEFT JOIN rn_competition_identity ci
                  ON ci.competition_identity_id =
                     cm.competition_identity_id
                """);

            statement.execute("""
                CREATE VIEW rn_configured_team AS
                SELECT
                    ts.team_season_id,
                    ts.season_id,
                    ts.source_file_id,
                    ts.source_team_id,
                    ts.source_name,
                    ts.normalized_name,
                    ts.source_division_id,
                    ts.source_team_number,
                    tm.team_identity_id,
                    ti.canonical_name,
                    tm.mapping_status,
                    tm.mapping_method,
                    tm.notes
                FROM rn_team_season ts
                JOIN rn_team_mapping tm
                  ON tm.team_season_id =
                     ts.team_season_id
                LEFT JOIN rn_team_identity ti
                  ON ti.team_identity_id =
                     tm.team_identity_id
                """);
        }
    }

    private static List<String> createSeasonEventViews(
            Connection connection) throws Exception {

        List<FcmSource> sources = readFcmSources(connection);
        List<String> generatedViews = new ArrayList<>();

        for (FcmSource source : sources) {
            String incontroTable = rawTable(
                connection,
                source.importId(),
                "INCONTRO"
            );

            String gironeTable = rawTable(
                connection,
                source.importId(),
                "GIRONE"
            );

            String giornataTable = rawTable(
                connection,
                source.importId(),
                "GIORNATA"
            );

            String viewName =
                "rn_event_"
                    + normalizeIdentifier(
                        source.seasonId()
                    )
                    + "_"
                    + source.importId();

            createSeasonEventView(
                connection,
                source,
                viewName,
                incontroTable,
                gironeTable,
                giornataTable
            );

            generatedViews.add(viewName);
        }

        if (generatedViews.isEmpty()) {
            throw new IllegalStateException(
                "Nessuna sorgente FCM configurata."
            );
        }

        return generatedViews;
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

        try (
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(sql)
        ) {
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

    private static void createSeasonEventView(
            Connection connection,
            FcmSource source,
            String viewName,
            String incontroTable,
            String gironeTable,
            String giornataTable) throws Exception {

        String sql = """
            CREATE VIEW %s AS
            WITH rounds AS (
                SELECT
                    g.ID AS source_group_id,
                    i.IDGIORNATA AS source_round_id,
                    MIN(i.ID) AS first_event_id,
                    ROW_NUMBER() OVER (
                        PARTITION BY g.ID
                        ORDER BY MIN(i.ID)
                    ) AS competition_round
                FROM %s i
                JOIN %s g
                  ON g.ID = i.IDGIRONE
                JOIN rn_configured_competition cc
                  ON cc.source_file_id = %d
                 AND cc.source_competition_id =
                     g.IDCOMPETIZIONE
                 AND cc.mapping_status = 'ASSOCIATA'
                WHERE i.GIOCATO <> 0
                  AND i.IDCASA <> 0
                GROUP BY
                    g.ID,
                    i.IDGIORNATA
            )
            SELECT
                '%s' AS season_id,
                %d AS source_file_id,

                cc.competition_identity_id,
                cc.canonical_name AS competition_name,
                cc.source_competition_id,

                g.ID AS source_group_id,
                g.NOME AS source_group_name,

                i.ID AS source_event_id,

                r.competition_round,
                i.GIORNATADIA AS serie_a_round,
                i.IDGIORNATA AS source_round_id,
                gio."DESC" AS round_description,

                i.IDTIPO AS source_match_type_id,

                i.IDCASA AS home_source_team_id,
                home.team_identity_id
                    AS home_team_identity_id,
                COALESCE(
                    home.canonical_name,
                    home.source_name
                ) AS home_team_name,

                i.IDFUORI AS away_source_team_id,
                away.team_identity_id
                    AS away_team_identity_id,
                CASE
                    WHEN i.IDFUORI = 0 THEN NULL
                    ELSE COALESCE(
                        away.canonical_name,
                        away.source_name
                    )
                END AS away_team_name,

                i.PARZCASA AS home_partial_score,
                i.PARZFUORI AS away_partial_score,

                i.TOTCASA AS home_total_score,
                i.TOTFUORI AS away_total_score,

                i.GOLCASA AS home_goals,
                i.GOLFUORI AS away_goals,

                i.GIOCATO AS played,

                CASE
                    WHEN i.IDFUORI <> 0
                        THEN 'HEAD_TO_HEAD'

                    WHEN i.TOTCASA <> 0
                      OR i.PARZCASA <> 0
                        THEN 'SCORE_ONLY'

                    ELSE 'REST'
                END AS event_type

            FROM %s i

            JOIN %s g
              ON g.ID = i.IDGIRONE

            JOIN rn_configured_competition cc
              ON cc.source_file_id = %d
             AND cc.source_competition_id =
                 g.IDCOMPETIZIONE
             AND cc.mapping_status = 'ASSOCIATA'

            JOIN rn_configured_team home
              ON home.source_file_id = %d
             AND home.source_team_id = i.IDCASA
             AND home.mapping_status = 'ASSOCIATA'

            LEFT JOIN rn_configured_team away
              ON away.source_file_id = %d
             AND away.source_team_id = i.IDFUORI
             AND away.mapping_status = 'ASSOCIATA'

            LEFT JOIN %s gio
              ON gio.ID = i.IDGIORNATA

            JOIN rounds r
              ON r.source_group_id = g.ID
             AND r.source_round_id = i.IDGIORNATA

            WHERE i.GIOCATO <> 0
              AND i.IDCASA <> 0
              AND (
                  i.IDFUORI = 0
                  OR away.team_identity_id IS NOT NULL
              )
            """.formatted(
                quoteIdentifier(viewName),
                quoteIdentifier(incontroTable),
                quoteIdentifier(gironeTable),
                source.sourceFileId(),
                escapeSqlLiteral(source.seasonId()),
                source.sourceFileId(),
                quoteIdentifier(incontroTable),
                quoteIdentifier(gironeTable),
                source.sourceFileId(),
                source.sourceFileId(),
                source.sourceFileId(),
                quoteIdentifier(giornataTable)
            );

        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private static void createUnionViews(
            Connection connection,
            List<String> seasonEventViews) throws Exception {

        StringBuilder eventUnion = new StringBuilder();

        for (String viewName : seasonEventViews) {
            if (!eventUnion.isEmpty()) {
                eventUnion.append("\nUNION ALL\n");
            }

            eventUnion.append(
                "SELECT * FROM "
                    + quoteIdentifier(viewName)
            );
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE VIEW rn_event AS
                %s
                """.formatted(eventUnion));

            statement.execute("""
                CREATE VIEW rn_match AS
                SELECT
                    season_id,
                    source_file_id,
                    competition_identity_id,
                    competition_name,
                    source_competition_id,
                    source_group_id,
                    source_group_name,

                    source_event_id,
                    source_event_id AS source_match_id,

                    competition_round,
                    serie_a_round,
                    source_round_id,
                    round_description,
                    source_match_type_id,

                    home_source_team_id,
                    home_team_identity_id,
                    home_team_name,

                    away_source_team_id,
                    away_team_identity_id,
                    away_team_name,

                    home_partial_score,
                    away_partial_score,
                    home_total_score,
                    away_total_score,
                    home_goals,
                    away_goals,

                    played
                FROM rn_event
                WHERE event_type = 'HEAD_TO_HEAD'
                """);

            statement.execute("""
                CREATE VIEW rn_team_event AS

                SELECT
                    season_id,
                    source_file_id,

                    competition_identity_id,
                    competition_name,
                    source_competition_id,

                    source_group_id,
                    source_group_name,

                    source_event_id,

                    competition_round,
                    serie_a_round,
                    source_round_id,
                    round_description,
                    source_match_type_id,

                    event_type,

                    home_source_team_id
                        AS source_team_id,

                    home_team_identity_id
                        AS team_identity_id,

                    home_team_name
                        AS team_name,

                    CASE
                        WHEN event_type = 'HEAD_TO_HEAD'
                        THEN away_source_team_id
                        ELSE NULL
                    END AS opponent_source_team_id,

                    CASE
                        WHEN event_type = 'HEAD_TO_HEAD'
                        THEN away_team_identity_id
                        ELSE NULL
                    END AS opponent_team_identity_id,

                    CASE
                        WHEN event_type = 'HEAD_TO_HEAD'
                        THEN away_team_name
                        ELSE NULL
                    END AS opponent_name,

                    CASE
                        WHEN event_type = 'HEAD_TO_HEAD'
                        THEN 'HOME'
                        ELSE 'NEUTRAL'
                    END AS venue,

                    home_goals AS goals_for,

                    CASE
                        WHEN event_type = 'HEAD_TO_HEAD'
                        THEN away_goals
                        ELSE NULL
                    END AS goals_against,

                    home_partial_score AS partial_score_for,

                    CASE
                        WHEN event_type = 'HEAD_TO_HEAD'
                        THEN away_partial_score
                        ELSE NULL
                    END AS partial_score_against,

                    home_total_score AS score_for,

                    CASE
                        WHEN event_type = 'HEAD_TO_HEAD'
                        THEN away_total_score
                        ELSE NULL
                    END AS score_against,

                    CASE
                        WHEN event_type <> 'HEAD_TO_HEAD'
                            THEN NULL

                        WHEN home_goals > away_goals
                            THEN 'W'

                        WHEN home_goals = away_goals
                            THEN 'D'

                        ELSE 'L'
                    END AS result

                FROM rn_event

                UNION ALL

                SELECT
                    season_id,
                    source_file_id,

                    competition_identity_id,
                    competition_name,
                    source_competition_id,

                    source_group_id,
                    source_group_name,

                    source_event_id,

                    competition_round,
                    serie_a_round,
                    source_round_id,
                    round_description,
                    source_match_type_id,

                    event_type,

                    away_source_team_id
                        AS source_team_id,

                    away_team_identity_id
                        AS team_identity_id,

                    away_team_name
                        AS team_name,

                    home_source_team_id
                        AS opponent_source_team_id,

                    home_team_identity_id
                        AS opponent_team_identity_id,

                    home_team_name
                        AS opponent_name,

                    'AWAY' AS venue,

                    away_goals AS goals_for,
                    home_goals AS goals_against,

                    away_partial_score AS partial_score_for,
                    home_partial_score AS partial_score_against,

                    away_total_score AS score_for,
                    home_total_score AS score_against,

                    CASE
                        WHEN away_goals > home_goals
                            THEN 'W'

                        WHEN away_goals = home_goals
                            THEN 'D'

                        ELSE 'L'
                    END AS result

                FROM rn_event
                WHERE event_type = 'HEAD_TO_HEAD'
                """);

            statement.execute("""
                CREATE VIEW rn_team_match AS
                SELECT
                    season_id,
                    source_file_id,

                    competition_identity_id,
                    competition_name,
                    source_competition_id,

                    source_group_id,
                    source_group_name,

                    source_event_id,
                    source_event_id AS source_match_id,

                    competition_round,
                    serie_a_round,
                    source_round_id,
                    round_description,
                    source_match_type_id,

                    source_team_id,
                    team_identity_id,
                    team_name,

                    opponent_source_team_id,
                    opponent_team_identity_id,
                    opponent_name,

                    venue,

                    goals_for,
                    goals_against,

                    partial_score_for,
                    partial_score_against,

                    score_for,
                    score_against,

                    result
                FROM rn_team_event
                WHERE event_type = 'HEAD_TO_HEAD'
                """);

            statement.execute("""
                CREATE VIEW rn_playoff_result AS
                SELECT
                    current.season_id,
                    current.source_file_id,

                    current.competition_identity_id,
                    current.competition_name,
                    current.source_competition_id,

                    current.source_group_id,
                    current.source_group_name,

                    current.source_round_id,
                    current.round_description,
                    current.serie_a_round,
                    current.competition_round,

                    current.source_event_id,
                    current.home_source_team_id
                        AS source_team_id,
                    current.home_team_identity_id
                        AS team_identity_id,
                    current.home_team_name
                        AS team_name,

                    opponent.source_event_id
                        AS opponent_source_event_id,
                    opponent.home_source_team_id
                        AS opponent_source_team_id,
                    opponent.home_team_identity_id
                        AS opponent_team_identity_id,
                    opponent.home_team_name
                        AS opponent_name,

                    current.home_total_score
                        AS score_for,
                    opponent.home_total_score
                        AS score_against,

                    CASE
                        WHEN current.home_total_score >
                             opponent.home_total_score
                            THEN 'W'

                        WHEN current.home_total_score <
                             opponent.home_total_score
                            THEN 'L'

                        ELSE 'D'
                    END AS result

                FROM rn_event current

                JOIN rn_event opponent
                  ON opponent.season_id =
                     current.season_id
                 AND opponent.source_file_id =
                     current.source_file_id
                 AND opponent.competition_identity_id =
                     current.competition_identity_id
                 AND opponent.source_group_id =
                     current.source_group_id
                 AND opponent.source_round_id =
                     current.source_round_id
                 AND opponent.source_event_id <>
                     current.source_event_id
                 AND opponent.event_type = 'SCORE_ONLY'

                WHERE current.event_type = 'SCORE_ONLY'
                  AND UPPER(current.competition_name) =
                      'PLAY OFF - PLAY OUT'
                """);
        }
    }

    private static String rawTable(
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
                        "Tabella raw mancante: "
                            + sourceTableName
                            + ", import_id="
                            + importId
                    );
                }

                return result.getString("raw_table_name");
            }
        }
    }

    private static void printAudit(
            Connection connection) throws Exception {

        System.out.println(
            "Viste canoniche create"
        );

        System.out.println();

        printCount(
            connection,
            "Stagioni",
            """
            SELECT COUNT(DISTINCT season_id)
            FROM rn_event
            """
        );

        printCount(
            connection,
            "Competizioni con eventi",
            """
            SELECT COUNT(
                DISTINCT competition_identity_id
            )
            FROM rn_event
            """
        );

        printCount(
            connection,
            "Eventi totali",
            """
            SELECT COUNT(*)
            FROM rn_event
            """
        );

        printCount(
            connection,
            "Scontri diretti",
            """
            SELECT COUNT(*)
            FROM rn_event
            WHERE event_type = 'HEAD_TO_HEAD'
            """
        );

        printCount(
            connection,
            "Riposi",
            """
            SELECT COUNT(*)
            FROM rn_event
            WHERE event_type = 'REST'
            """
        );

        printCount(
            connection,
            "Punteggi puri",
            """
            SELECT COUNT(*)
            FROM rn_event
            WHERE event_type = 'SCORE_ONLY'
            """
        );

        printCount(
            connection,
            "Partecipazioni",
            """
            SELECT COUNT(*)
            FROM rn_team_event
            """
        );

        printCount(
            connection,
            "Righe squadra match",
            """
            SELECT COUNT(*)
            FROM rn_team_match
            """
        );

        printCount(
            connection,
            "Righe play off/out",
            """
            SELECT COUNT(*)
            FROM rn_playoff_result
            """
        );

        System.out.println();
        System.out.println("=== ESITI SCONTRI DIRETTI ===");

        printCount(
            connection,
            "Vittorie",
            """
            SELECT COUNT(*)
            FROM rn_team_match
            WHERE result = 'W'
            """
        );

        printCount(
            connection,
            "Pareggi",
            """
            SELECT COUNT(*)
            FROM rn_team_match
            WHERE result = 'D'
            """
        );

        printCount(
            connection,
            "Sconfitte",
            """
            SELECT COUNT(*)
            FROM rn_team_match
            WHERE result = 'L'
            """
        );

        System.out.println();
        System.out.println("=== ESITI PLAY OFF / PLAY OUT ===");

        printCount(
            connection,
            "Vinti",
            """
            SELECT COUNT(*)
            FROM rn_playoff_result
            WHERE result = 'W'
            """
        );

        printCount(
            connection,
            "Persi",
            """
            SELECT COUNT(*)
            FROM rn_playoff_result
            WHERE result = 'L'
            """
        );

        printCount(
            connection,
            "Pari",
            """
            SELECT COUNT(*)
            FROM rn_playoff_result
            WHERE result = 'D'
            """
        );

        System.out.println();
        System.out.println("=== EVENTI PER TIPO ===");

        try (
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("""
                SELECT
                    event_type,
                    COUNT(*) AS event_count
                FROM rn_event
                GROUP BY event_type
                ORDER BY event_type
                """)
        ) {
            while (result.next()) {
                System.out.printf(
                    Locale.ROOT,
                    "%-16s: %d%n",
                    result.getString("event_type"),
                    result.getLong("event_count")
                );
            }
        }
    }

    private static void printCount(
            Connection connection,
            String label,
            String sql) throws Exception {

        try (
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(sql)
        ) {
            result.next();

            System.out.printf(
                Locale.ROOT,
                "%-24s: %d%n",
                label,
                result.getLong(1)
            );
        }
    }

    private static String normalizeIdentifier(String value) {
        String normalized = value
            .trim()
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "_")
            .replaceAll("^_+|_+$", "");

        if (normalized.isBlank()) {
            throw new IllegalArgumentException(
                "Identificatore non valido: " + value
            );
        }

        return normalized;
    }

    private static String quoteIdentifier(String value) {
        return "\""
            + value.replace("\"", "\"\"")
            + "\"";
    }

    private static String escapeSqlLiteral(String value) {
        return value.replace("'", "''");
    }

    private record FcmSource(
        long sourceFileId,
        long importId,
        String seasonId
    ) {
    }
}