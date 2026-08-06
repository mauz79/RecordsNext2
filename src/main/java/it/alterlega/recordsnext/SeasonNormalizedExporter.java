package it.alterlega.recordsnext;

import java.io.BufferedWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class SeasonNormalizedExporter {

    private SeasonNormalizedExporter() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 5) {
            System.err.println(
                "Uso: SeasonNormalizedExporter "
                    + "<recordsnext.db> "
                    + "<stagione> "
                    + "<competizione-canonica> "
                    + "<project-dir> "
                    + "<output.json>"
            );
            System.exit(2);
        }

        Path database = Path.of(args[0])
            .toAbsolutePath()
            .normalize();

        String seasonId = args[1].trim();
        String competitionName = args[2].trim();

        Path projectDir = Path.of(args[3])
            .toAbsolutePath()
            .normalize();

        Path output = Path.of(args[4])
            .toAbsolutePath()
            .normalize();

        if (output.getParent() != null) {
            Files.createDirectories(output.getParent());
        }

        Class.forName("org.sqlite.JDBC");

        long started = System.nanoTime();

        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + database)) {

            ExportData data = readExportData(
                connection,
                seasonId,
                competitionName,
                projectDir
            );

            writeJson(output, data);

            long finished = System.nanoTime();

            System.out.println("Normalized stage 1 completato");
            System.out.println("Stagione    : " + seasonId);
            System.out.println("Competizione: " + competitionName);
            System.out.println("Incontri    : " + data.meta().matchesAnalyzed());
            System.out.println("Righe squadra: " + data.teamMatches().size());
            System.out.println("Output      : " + output);

            System.out.printf(
                Locale.ROOT,
                "Tempo       : %.3f ms%n",
                (finished - started) / 1_000_000.0
            );
        }
    }

    private static ExportData readExportData(
            Connection connection,
            String seasonId,
            String competitionName,
            Path projectDir) throws Exception {

        CompetitionInfo competition = readCompetition(
            connection,
            seasonId,
            competitionName
        );

        List<Integer> groupIds = readGroupIds(
            connection,
            seasonId,
            competition.identityId()
        );

        List<TeamMatch> teamMatches = readTeamMatches(
            connection,
            seasonId,
            competition.identityId(),
            competitionName
        );

        List<ExpulsionDetail> expulsionDetails =
            readExpulsionDetails(
                connection,
                seasonId,
                competition.identityId()
            );

        List<EventDetail> eventDetails =
            readEventDetails(
                connection,
                seasonId,
                competition.identityId()
            );

        List<ModifierDetail> modifierDetails =
            readModifierDetails(
                connection,
                seasonId,
                competition.identityId()
            );

        List<CleanSheetDetail> cleanSheetDetails =
            readCleanSheetDetails(
                connection,
                seasonId,
                competition.identityId()
            );

        List<ReserveOfficeDetail> reserveOfficeDetails =
            readReserveOfficeDetails(
                connection,
                seasonId,
                competition.identityId()
            );

        List<GoalBandDetail> goalBandDetails =
            readGoalBandDetails(
                connection,
                seasonId,
                competition.sourceCompetitionId()
            );

        int matchesAnalyzed = teamMatches.size() / 2;

        Meta meta = new Meta(
            Instant.now().toString(),
            projectDir.toString(),
            seasonId,
            outputHistoricalCompetitionId(competitionName),
            outputCompetitionName(competitionName),
            competition.sourceCompetitionId(),
            null,
            groupIds,
            "SQLite: " + connection.getMetaData().getURL(),
            "SQLite: " + connection.getMetaData().getURL(),
            matchesAnalyzed,
            teamMatches.size()
        );

        return new ExportData(
            meta,
            teamMatches,
            expulsionDetails,
            eventDetails,
            modifierDetails,
            cleanSheetDetails,
            reserveOfficeDetails,
            goalBandDetails
        );
    }

    private static CompetitionInfo readCompetition(
            Connection connection,
            String seasonId,
            String competitionName) throws Exception {

        String sql = """
            SELECT DISTINCT
                competition_identity_id,
                source_competition_id
            FROM rn_match
            WHERE season_id = ?
              AND competition_name = ?
            """;

        try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setString(1, seasonId);
            statement.setString(2, competitionName);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalArgumentException(
                        "Competizione non trovata: "
                            + seasonId
                            + " / "
                            + competitionName
                    );
                }

                CompetitionInfo info = new CompetitionInfo(
                    result.getLong("competition_identity_id"),
                    result.getInt("source_competition_id")
                );

                if (result.next()) {
                    throw new IllegalStateException(
                        "PiÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¹ identitÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â  trovate per "
                            + seasonId
                            + " / "
                            + competitionName
                    );
                }

                return info;
            }
        }
    }

    private static List<Integer> readGroupIds(
            Connection connection,
            String seasonId,
            long competitionIdentityId) throws Exception {

        String sql = """
            SELECT DISTINCT source_group_id
            FROM rn_match
            WHERE season_id = ?
              AND competition_identity_id = ?
            ORDER BY source_group_id
            """;

        List<Integer> values = new ArrayList<>();

        try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setString(1, seasonId);
            statement.setLong(2, competitionIdentityId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    values.add(
                        result.getInt("source_group_id")
                    );
                }
            }
        }

        return values;
    }

    private static List<TeamMatch> readTeamMatches(
            Connection connection,
            String seasonId,
            long competitionIdentityId,
            String competitionName) throws Exception {

        String outputHistoricalId =
            outputHistoricalCompetitionId(competitionName);

        String outputCompetitionName =
            outputCompetitionName(competitionName);

        SourceInfo source = readFcmSource(
            connection,
            seasonId
        );

        String tabellinoTable = rawTable(
            connection,
            source.importId(),
            "TABELLINO"
        );

        String gironeTable = rawTable(
            connection,
            source.importId(),
            "GIRONE"
        );

        boolean calendarAvailable = tableExists(connection, "rn_matchday_date");

        String calendarColumns = calendarAvailable
            ? "md.match_date, md.match_time, md.match_datetime,"
            : "NULL AS match_date, NULL AS match_time, NULL AS match_datetime,";

        String calendarJoin = calendarAvailable
            ? "LEFT JOIN rn_matchday_date md "
                + "ON md.season_id = e.season_id "
                + "AND md.serie_a_round = e.serie_a_round"
            : "";

        String sql = """
            SELECT
                e.season_id,
                %s
                e.competition_name,
                e.source_competition_id,
                e.source_group_id,
                g.NOME AS source_group_name,
                e.source_round_id,
                e.round_description,
                e.serie_a_round,
                e.source_event_id,
                e.event_type,
                e.venue,
                e.source_team_id,
                e.team_name,
                e.opponent_source_team_id,
                e.opponent_name,
                e.score_for,
                e.score_against,
                e.partial_score_for,
                e.partial_score_against,
                e.goals_for,
                e.goals_against,
                e.result,

                CASE
                    WHEN e.event_type = 'REST' THEN 0
                    WHEN tf.IDINCONTRO IS NULL THEN e.goals_for
                    ELSE
                        CAST(COALESCE(tf.GOL, 0) AS INTEGER)
                        - CAST(COALESCE(tf.GOLSUPPLEMENTARI, 0) AS INTEGER)
                        - CAST(COALESCE(tf.GOLRIGORI, 0) AS INTEGER)
                END AS regulation_goals_for,

                CASE
                    WHEN e.event_type = 'REST' THEN 0
                    WHEN ta.IDINCONTRO IS NULL THEN e.goals_against
                    ELSE
                        CAST(COALESCE(ta.GOL, 0) AS INTEGER)
                        - CAST(COALESCE(ta.GOLSUPPLEMENTARI, 0) AS INTEGER)
                        - CAST(COALESCE(ta.GOLRIGORI, 0) AS INTEGER)
                END AS regulation_goals_against,

                CASE
                    WHEN e.event_type = 'REST' THEN 0
                    WHEN tf.IDINCONTRO IS NULL THEN 0
                    ELSE 1
                END AS regulation_goals_found

            FROM rn_team_event e

            JOIN %s g
              ON g.ID = e.source_group_id

            LEFT JOIN %s tf
              ON e.event_type = 'HEAD_TO_HEAD'
             AND tf.IDINCONTRO = e.source_event_id
             AND tf.IDSQUADRA = e.source_team_id

            LEFT JOIN %s ta
              ON e.event_type = 'HEAD_TO_HEAD'
             AND ta.IDINCONTRO = e.source_event_id
             AND ta.IDSQUADRA = e.opponent_source_team_id

            %s

            WHERE e.season_id = ?
              AND e.competition_identity_id = ?
              AND e.event_type IN ('HEAD_TO_HEAD', 'REST')

            ORDER BY
                e.source_event_id,
                CASE e.venue
                    WHEN 'HOME' THEN 0
                    WHEN 'AWAY' THEN 1
                    ELSE 0
                END
            """.formatted(
                calendarColumns,
                quoteIdentifier(gironeTable),
                quoteIdentifier(tabellinoTable),
                quoteIdentifier(tabellinoTable),
                calendarJoin
            );

        ScorecardBases scorecardBases = readScorecardBases(
            connection,
            seasonId
        );

        List<TeamMatch> rows = new ArrayList<>();

        try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setString(1, seasonId);
            statement.setLong(2, competitionIdentityId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    String eventType =
                        result.getString("event_type");

                    boolean rest =
                        "REST".equals(eventType);

                    String venue =
                        result.getString("venue");

                    String side;

                    if (rest) {
                        side = "casa";
                    } else {
                        side = switch (venue) {
                            case "HOME" -> "casa";
                            case "AWAY" -> "fuori";
                            default -> throw new IllegalStateException(
                                "Lato non previsto: "
                                    + venue
                                    + " / evento "
                                    + result.getLong(
                                        "source_event_id"
                                    )
                            );
                        };
                    }

                    int goalsFor = rest
                        ? 0
                        : result.getInt("goals_for");

                    int goalsAgainst = rest
                        ? 0
                        : result.getInt("goals_against");

                    int regulationGoalsFor = rest
                        ? 0
                        : result.getInt(
                            "regulation_goals_for"
                        );

                    int regulationGoalsAgainst = rest
                        ? 0
                        : result.getInt(
                            "regulation_goals_against"
                        );

                    BigDecimal scoreFor = zeroIfNull(
                        result.getBigDecimal("score_for")
                    );

                    BigDecimal scoreAgainst = rest
                        ? BigDecimal.ZERO
                        : zeroIfNull(
                            result.getBigDecimal(
                                "score_against"
                            )
                        );

                    BigDecimal partialFor = zeroIfNull(
                        result.getBigDecimal(
                            "partial_score_for"
                        )
                    );

                    BigDecimal partialAgainst = rest
                        ? BigDecimal.ZERO
                        : zeroIfNull(
                            result.getBigDecimal(
                                "partial_score_against"
                            )
                        );

                    int opponentId = rest
                        ? 0
                        : result.getInt(
                            "opponent_source_team_id"
                        );

                    String opponentName = rest
                        ? ""
                        : emptyIfNull(
                            result.getString(
                                "opponent_name"
                            )
                        );

                    int homeGoals;
                    int awayGoals;
                    int regulationHomeGoals;
                    int regulationAwayGoals;
                    BigDecimal homeScore;
                    BigDecimal awayScore;

                    if (rest || "HOME".equals(venue)) {
                        homeGoals = goalsFor;
                        awayGoals = goalsAgainst;

                        regulationHomeGoals =
                            regulationGoalsFor;

                        regulationAwayGoals =
                            regulationGoalsAgainst;

                        homeScore = scoreFor;
                        awayScore = scoreAgainst;
                    } else {
                        homeGoals = goalsAgainst;
                        awayGoals = goalsFor;

                        regulationHomeGoals =
                            regulationGoalsAgainst;

                        regulationAwayGoals =
                            regulationGoalsFor;

                        homeScore = scoreAgainst;
                        awayScore = scoreFor;
                    }

                    String resultCode;

                    if (rest) {
                        resultCode = "P";
                    } else {
                        resultCode = switch (
                            result.getString("result")
                        ) {
                            case "W" -> "V";
                            case "D" -> "P";
                            case "L" -> "S";
                            default -> throw new IllegalStateException(
                                "Esito non previsto: "
                                    + result.getString(
                                        "result"
                                    )
                            );
                        };
                    }

                    int serieARound =
                        result.getInt("serie_a_round");

                    String regulationSource;

                    if (rest) {
                        regulationSource =
                            "GolCasa/GolFuori fallback";
                    } else if (
                        result.getInt(
                            "regulation_goals_found"
                        ) != 0
                    ) {
                        regulationSource =
                            "GolRegoCasa/GolRegoFuori";
                    } else {
                        regulationSource =
                            "GolCasa/GolFuori fallback";
                    }

                    rows.add(
                        new TeamMatch(
                            result.getString("season_id"),
                            outputHistoricalId,
                            outputCompetitionName,
                            result.getInt(
                                "source_competition_id"
                            ),
                            null,
                            result.getInt(
                                "source_group_id"
                            ),
                            result.getString(
                                "source_group_name"
                            ),
                            result.getInt(
                                "source_round_id"
                            ),
                            result.getString(
                                "round_description"
                            ),
                            serieARound,
                            result.getString("match_date"),
                            result.getString("match_time"),
                            result.getString("match_datetime"),
                            result.getInt(
                                "source_round_id"
                            ),
                            result.getLong(
                                "source_event_id"
                            ),
                            scorecardUrl(result.getString("season_id"), serieARound),
                            scorecardBases.localUrl(serieARound),
                            scorecardBases.onlineUrl(serieARound),
                            side,
                            result.getInt(
                                "source_team_id"
                            ),
                            result.getString(
                                "team_name"
                            ),
                            opponentId,
                            opponentName,
                            scoreFor,
                            scoreAgainst,
                            partialFor,
                            partialAgainst,
                            goalsFor,
                            goalsAgainst,
                            regulationGoalsFor,
                            regulationGoalsAgainst,
                            regulationHomeGoals
                                + "-"
                                + regulationAwayGoals,
                            regulationSource,
                            resultCode,
                            homeGoals + "-" + awayGoals,
                            decimalText(homeScore)
                                + "-"
                                + decimalText(awayScore)
                        )
                    );

                    if (rest) {
                        rows.add(
                            new TeamMatch(
                                result.getString("season_id"),
                                outputHistoricalId,
                                outputCompetitionName,
                                result.getInt(
                                    "source_competition_id"
                                ),
                                null,
                                result.getInt(
                                    "source_group_id"
                                ),
                                result.getString(
                                    "source_group_name"
                                ),
                                result.getInt(
                                    "source_round_id"
                                ),
                                result.getString(
                                    "round_description"
                                ),
                                serieARound,
                            result.getString("match_date"),
                            result.getString("match_time"),
                            result.getString("match_datetime"),
                                result.getInt(
                                    "source_round_id"
                                ),
                                result.getLong(
                                    "source_event_id"
                                ),
                                scorecardUrl(result.getString("season_id"), serieARound),
                                scorecardBases.localUrl(serieARound),
                                scorecardBases.onlineUrl(serieARound),
                                "fuori",
                                0,
                                "",
                                result.getInt(
                                    "source_team_id"
                                ),
                                result.getString(
                                    "team_name"
                                ),
                                BigDecimal.ZERO,
                                BigDecimal.ZERO,
                                BigDecimal.ZERO,
                                BigDecimal.ZERO,
                                0,
                                0,
                                0,
                                0,
                                "0-0",
                                "GolCasa/GolFuori fallback",
                                "P",
                                "0-0",
                                "0-0"
                            )
                        );
                    }
                }
            }
        }

        return rows;
    }
    private static List<ExpulsionDetail> readExpulsionDetails(
            Connection connection,
            String seasonId,
            long competitionIdentityId) throws Exception {

        SourceInfo fcmSource = readSource(
            connection,
            seasonId,
            "FCM"
        );

        SourceInfo fcaSource = readSource(
            connection,
            seasonId,
            "FCA"
        );

        String formazioneTable = rawTable(
            connection,
            fcmSource.importId(),
            "FORMAZIONE"
        );

        String giocaInTable = rawTable(
            connection,
            fcaSource.importId(),
            "GIOCAIN"
        );

        String punteggioTable = rawTable(
            connection,
            fcaSource.importId(),
            "PUNTEGGIO"
        );

        String giocatoreTable = rawTable(
            connection,
            fcaSource.importId(),
            "GIOCATOREA"
        );

        String sql = """
            SELECT
                tm.source_match_id,
                tm.serie_a_round,
                tm.source_team_id,
                tm.team_name,
                f.IDGIOC AS player_id,
                ga.NOME AS player_name,
                gi.IDPUNTEGGIO AS score_id

            FROM rn_team_match tm

            JOIN %s f
              ON f.IDINCONTRO = tm.source_match_id
             AND f.IDSQUADRA = tm.source_team_id

            JOIN %s gi
              ON gi.IDGIOCATORE = f.IDGIOC
             AND gi.GIORNATA = tm.serie_a_round

            JOIN %s p
              ON p.ID = gi.IDPUNTEGGIO

            JOIN %s ga
              ON ga.ID = f.IDGIOC

            WHERE tm.season_id = ?
              AND tm.competition_identity_id = ?
              AND f.ENTRATO <> 0
              AND p.ESP <> 0

            ORDER BY
                tm.source_match_id,
                f.IDGIOC
            """.formatted(
                quoteIdentifier(formazioneTable),
                quoteIdentifier(giocaInTable),
                quoteIdentifier(punteggioTable),
                quoteIdentifier(giocatoreTable)
            );

        List<ExpulsionDetail> rows = new ArrayList<>();

        try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setString(1, seasonId);
            statement.setLong(2, competitionIdentityId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    rows.add(
                        new ExpulsionDetail(
                            result.getLong("source_match_id"),
                            result.getInt("serie_a_round"),
                            result.getInt("source_team_id"),
                            result.getString("team_name"),
                            result.getInt("player_id"),
                            result.getString("player_name"),
                            result.getInt("score_id")
                        )
                    );
                }
            }
        }

        return rows;
    }

    private static List<EventDetail> readEventDetails(
            Connection connection,
            String seasonId,
            long competitionIdentityId) throws Exception {

        SourceInfo fcmSource = readSource(
            connection,
            seasonId,
            "FCM"
        );

        SourceInfo fcaSource = readSource(
            connection,
            seasonId,
            "FCA"
        );

        String formazioneTable = rawTable(
            connection,
            fcmSource.importId(),
            "FORMAZIONE"
        );

        String giocaInTable = rawTable(
            connection,
            fcaSource.importId(),
            "GIOCAIN"
        );

        String punteggioTable = rawTable(
            connection,
            fcaSource.importId(),
            "PUNTEGGIO"
        );

        String giocatoreTable = rawTable(
            connection,
            fcaSource.importId(),
            "GIOCATOREA"
        );

        String sql = """
            SELECT
                e.record_key,
                e.event_type,
                e.event_name,
                e.source_field,
                tm.source_match_id,
                tm.serie_a_round,
                tm.source_team_id,
                tm.team_name,
                f.IDGIOC AS player_id,
                ga.NOME AS player_name,
                gi.IDPUNTEGGIO AS score_id,
                CASE e.event_number
                    WHEN 1 THEN p.AMM
                    WHEN 2 THEN p.ASSIST
                    WHEN 3 THEN p.GOLFATTISURIGORE1
                    WHEN 4 THEN p.RIGPAR
                    WHEN 5 THEN p.RIGSBA
                    WHEN 6 THEN p.AUTOGOL1
                END AS event_value,
                e.event_number

            FROM rn_team_match tm

            JOIN %s f
              ON f.IDINCONTRO = tm.source_match_id
             AND f.IDSQUADRA = tm.source_team_id

            JOIN %s gi
              ON gi.IDGIOCATORE = f.IDGIOC
             AND gi.GIORNATA = tm.serie_a_round

            JOIN %s p
              ON p.ID = gi.IDPUNTEGGIO

            JOIN %s ga
              ON ga.ID = f.IDGIOC

            CROSS JOIN (
                SELECT
                    1 AS event_number,
                    'ammonizioniSquadre' AS record_key,
                    'ammonizione' AS event_type,
                    'Maggiori ammonizioni' AS event_name,
                    'Amm' AS source_field

                UNION ALL

                SELECT
                    2,
                    'assistSquadre',
                    'assist',
                    'Maggiori assist',
                    'Assist'

                UNION ALL

                SELECT
                    3,
                    'golRigoreSquadre',
                    'gol_su_rigore',
                    'Maggiori gol fatti su rigore',
                    'GolFattiSuRigore1'

                UNION ALL

                SELECT
                    4,
                    'rigoriParatiSquadre',
                    'rigore_parato',
                    'Maggiori rigori parati',
                    'RigPar'

                UNION ALL

                SELECT
                    5,
                    'rigoriSbagliatiSquadre',
                    'rigore_sbagliato',
                    'Maggiori rigori sbagliati',
                    'RigSba'

                UNION ALL

                SELECT
                    6,
                    'autogolSquadre',
                    'autogol',
                    'Maggiori autogol',
                    'Autogol1'
            ) e

            WHERE tm.season_id = ?
              AND tm.competition_identity_id = ?
              AND f.ENTRATO <> 0
              AND CASE e.event_number
                    WHEN 1 THEN p.AMM
                    WHEN 2 THEN p.ASSIST
                    WHEN 3 THEN p.GOLFATTISURIGORE1
                    WHEN 4 THEN p.RIGPAR
                    WHEN 5 THEN p.RIGSBA
                    WHEN 6 THEN p.AUTOGOL1
                  END <> 0

            ORDER BY
                tm.source_match_id,
                f.rowid,
                e.event_number
            """.formatted(
                quoteIdentifier(formazioneTable),
                quoteIdentifier(giocaInTable),
                quoteIdentifier(punteggioTable),
                quoteIdentifier(giocatoreTable)
            );

        List<EventDetail> rows = new ArrayList<>();

        try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setString(1, seasonId);
            statement.setLong(2, competitionIdentityId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    rows.add(
                        new EventDetail(
                            result.getString("record_key"),
                            result.getString("event_type"),
                            result.getString("event_name"),
                            result.getString("source_field"),
                            result.getLong("source_match_id"),
                            result.getInt("serie_a_round"),
                            result.getInt("source_team_id"),
                            result.getString("team_name"),
                            result.getInt("player_id"),
                            result.getString("player_name"),
                            result.getInt("score_id"),
                            result.getInt("event_value")
                        )
                    );
                }
            }
        }

        return rows;
    }

    private static List<CleanSheetDetail> readCleanSheetDetails(
            Connection connection,
            String seasonId,
            long competitionIdentityId) throws Exception {

        SourceInfo fcmSource = readSource(
            connection,
            seasonId,
            "FCM"
        );

        SourceInfo fcaSource = readSource(
            connection,
            seasonId,
            "FCA"
        );

        String formazioneTable = rawTable(
            connection,
            fcmSource.importId(),
            "FORMAZIONE"
        );

        String giocaInTable = rawTable(
            connection,
            fcaSource.importId(),
            "GIOCAIN"
        );

        String punteggioTable = rawTable(
            connection,
            fcaSource.importId(),
            "PUNTEGGIO"
        );

        String giocatoreTable = rawTable(
            connection,
            fcaSource.importId(),
            "GIOCATOREA"
        );

        String sql = """
            SELECT
                tm.source_match_id,
                tm.serie_a_round,
                tm.source_team_id,
                tm.team_name,
                tm.opponent_source_team_id,
                tm.opponent_name,
                f.IDGIOC AS player_id,
                ga.NOME AS player_name,
                gi.IDPUNTEGGIO AS score_id,
                p.GOLSUBITI AS goals_conceded

            FROM rn_team_match tm

            JOIN %s f
              ON f.IDINCONTRO = tm.source_match_id
             AND f.IDSQUADRA = tm.source_team_id

            JOIN %s gi
              ON gi.IDGIOCATORE = f.IDGIOC
             AND gi.GIORNATA = tm.serie_a_round

            JOIN %s p
              ON p.ID = gi.IDPUNTEGGIO

            JOIN %s ga
              ON ga.ID = f.IDGIOC

            WHERE tm.season_id = ?
              AND tm.competition_identity_id = ?
              AND f.ENTRATO <> 0
              AND ga.RUOLO = 1
              AND p.GOLSUBITI = 0

            ORDER BY
                f.IDGIOC,
                tm.source_match_id
            """.formatted(
                quoteIdentifier(formazioneTable),
                quoteIdentifier(giocaInTable),
                quoteIdentifier(punteggioTable),
                quoteIdentifier(giocatoreTable)
            );

        List<CleanSheetDetail> rows = new ArrayList<>();

        try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setString(1, seasonId);
            statement.setLong(2, competitionIdentityId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    rows.add(
                        new CleanSheetDetail(
                            result.getLong("source_match_id"),
                            result.getInt("serie_a_round"),
                            result.getInt("source_team_id"),
                            result.getString("team_name"),
                            result.getInt("opponent_source_team_id"),
                            result.getString("opponent_name"),
                            result.getInt("player_id"),
                            result.getString("player_name"),
                            result.getInt("score_id"),
                            result.getInt("goals_conceded"),
                            new BigDecimal("0.5")
                        )
                    );
                }
            }
        }

        return rows;
    }

    private static List<ModifierDetail> readModifierDetails(
            Connection connection,
            String seasonId,
            long competitionIdentityId) throws Exception {

        SourceInfo source = readFcmSource(
            connection,
            seasonId
        );

        String tabellinoTable = rawTable(
            connection,
            source.importId(),
            "TABELLINO"
        );

        String gironeTable = rawTable(
            connection,
            source.importId(),
            "GIRONE"
        );

        String sql = """
            SELECT
                x.modifier_type,
                x.source_field,
                m.source_match_id,
                m.serie_a_round,
                tm.source_team_id,
                tm.team_name,
                tm.opponent_source_team_id,
                tm.opponent_name,

                CASE x.modifier_number
                    WHEN 1 THEN t.MODM1PERS
                    WHEN 2 THEN t.MODM2PERS
                    WHEN 3 THEN t.MODM3PERS
                    WHEN 4 THEN t.MODPORTIERE
                    WHEN 5 THEN t.MODDIFESA
                    WHEN 6 THEN t.MODCENTROCAMPO
                    WHEN 7 THEN t.MODATTACCO
                    WHEN 8 THEN t.MODMODULO
                END AS modifier_value

            FROM %s t

            JOIN rn_match m
              ON m.source_file_id = ?
             AND m.source_match_id = t.IDINCONTRO

            JOIN rn_team_match tm
              ON tm.source_file_id = m.source_file_id
             AND tm.source_match_id = m.source_match_id
             AND tm.source_team_id = t.IDSQUADRA

            CROSS JOIN (
                SELECT 1 AS modifier_number, 'modDifesa' AS modifier_type, 'MODM1PERS' AS source_field
                UNION ALL
                SELECT 2, 'capitano', 'MODM2PERS'
                UNION ALL
                SELECT 3, 'personalizzato3', 'MODM3PERS'
                UNION ALL
                SELECT 4, 'fcmPortiere', 'MODPORTIERE'
                UNION ALL
                SELECT 5, 'fcmDifesa', 'MODDIFESA'
                UNION ALL
                SELECT 6, 'fcmCentrocampo', 'MODCENTROCAMPO'
                UNION ALL
                SELECT 7, 'fcmAttacco', 'MODATTACCO'
                UNION ALL
                SELECT 8, 'fcmModulo', 'MODMODULO'
            ) x

            WHERE m.season_id = ?
              AND m.competition_identity_id = ?

              AND CASE x.modifier_number
                    WHEN 1 THEN t.MODM1PERSESISTE
                    WHEN 2 THEN t.MODM2PERSESISTE
                    WHEN 3 THEN t.MODM3PERSESISTE
                    WHEN 4 THEN t.MODPORTIEREESISTE
                    WHEN 5 THEN t.MODDIFESAESISTE
                    WHEN 6 THEN t.MODCENTROCAMPOESISTE
                    WHEN 7 THEN t.MODATTACCOESISTE
                    WHEN 8 THEN t.MODMODULOESISTE
                  END <> 0

              AND CASE x.modifier_number
                    WHEN 1 THEN t.MODM1PERS
                    WHEN 2 THEN t.MODM2PERS
                    WHEN 3 THEN t.MODM3PERS
                    WHEN 4 THEN t.MODPORTIERE
                    WHEN 5 THEN t.MODDIFESA
                    WHEN 6 THEN t.MODCENTROCAMPO
                    WHEN 7 THEN t.MODATTACCO
                    WHEN 8 THEN t.MODMODULO
                  END <> 0

            ORDER BY
                t.rowid,
                x.modifier_number
            """.formatted(
                quoteIdentifier(tabellinoTable)
            );

        List<ModifierDetail> rows = new ArrayList<>();

        try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setLong(1, source.sourceFileId());
            statement.setString(2, seasonId);
            statement.setLong(3, competitionIdentityId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    rows.add(
                        new ModifierDetail(
                            result.getString("modifier_type"),
                            result.getLong("source_match_id"),
                            result.getInt("serie_a_round"),
                            result.getInt("source_team_id"),
                            result.getString("team_name"),
                            result.getInt(
                                "opponent_source_team_id"
                            ),
                            result.getString("opponent_name"),
                            result.getBigDecimal(
                                "modifier_value"
                            ),
                            result.getString("source_field")
                        )
                    );
                }
            }
        }

        return rows;
    }

    private static List<ReserveOfficeDetail> readReserveOfficeDetails(
            Connection connection,
            String seasonId,
            long competitionIdentityId) throws Exception {

        SourceInfo source = readFcmSource(connection, seasonId);
        String tabellinoTable = rawTable(connection, source.importId(), "TABELLINO");

        String sql = """
            SELECT
                m.source_match_id,
                m.serie_a_round,
                tm.source_team_id,
                tm.team_name,
                tm.opponent_source_team_id,
                tm.opponent_name,
                t.LISTA,
                t.RUOLO,
                t.VOTO,
                t.MODIF,
                t.TOT
            FROM %s t
            JOIN rn_match m
              ON m.source_file_id = ?
             AND m.source_match_id = t.IDINCONTRO
            JOIN rn_team_match tm
              ON tm.source_file_id = m.source_file_id
             AND tm.source_match_id = m.source_match_id
             AND tm.source_team_id = t.IDSQUADRA
            WHERE m.season_id = ?
              AND m.competition_identity_id = ?
            ORDER BY t.rowid
            """.formatted(quoteIdentifier(tabellinoTable));

        List<ReserveOfficeDetail> rows = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, source.sourceFileId());
            statement.setString(2, seasonId);
            statement.setLong(3, competitionIdentityId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    String[] players = splitPercent(result.getString("LISTA"));
                    String[] roles = splitPercent(result.getString("RUOLO"));
                    String[] votes = splitPercent(result.getString("VOTO"));
                    String[] modifiers = splitPercent(result.getString("MODIF"));
                    String[] totals = splitPercent(result.getString("TOT"));
                    int max = Math.max(players.length,
                        Math.max(roles.length,
                        Math.max(votes.length,
                        Math.max(modifiers.length, totals.length))));

                    for (int index = 0; index < max; index++) {
                        if (!"-1".equals(item(players, index))) {
                            continue;
                        }

                        int role = parseInteger(item(roles, index));
                        rows.add(new ReserveOfficeDetail(
                            result.getLong("source_match_id"),
                            result.getInt("serie_a_round"),
                            result.getInt("source_team_id"),
                            result.getString("team_name"),
                            result.getInt("opponent_source_team_id"),
                            result.getString("opponent_name"),
                            roleCode(role),
                            roleName(role),
                            index + 1,
                            item(votes, index),
                            item(modifiers, index),
                            item(totals, index),
                            parseDecimal(item(totals, index))
                        ));
                    }
                }
            }
        }

        return rows;
    }

    private static List<GoalBandDetail> readGoalBandDetails(
            Connection connection,
            String seasonId,
            int sourceCompetitionId) throws Exception {

        SourceInfo source = readFcmSource(connection, seasonId);
        String goalTable = rawTable(connection, source.importId(), "TABELLAGOL");
        String bandTable = rawTable(connection, source.importId(), "FASCIA");

        String sql = """
            SELECT
                tg.IDCOMPETIZIONE AS source_competition_id,
                tg.IDFASCIA AS source_band_id,
                f.MIN AS min_score,
                f.MAX AS max_score,
                f.VALORE AS goals
            FROM %s tg
            JOIN %s f
              ON f.ID = tg.IDFASCIA
            WHERE tg.IDCOMPETIZIONE = ?
            ORDER BY CAST(f.MIN AS REAL), CAST(f.VALORE AS INTEGER), tg.IDFASCIA
            """.formatted(
                quoteIdentifier(goalTable),
                quoteIdentifier(bandTable)
            );

        List<GoalBandDetail> rows = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, sourceCompetitionId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    rows.add(new GoalBandDetail(
                        result.getInt("source_competition_id"),
                        Integer.toString(result.getInt("source_band_id")),
                        zeroIfNull(result.getBigDecimal("min_score")),
                        zeroIfNull(result.getBigDecimal("max_score")),
                        result.getInt("goals")
                    ));
                }
            }
        }

        if (rows.isEmpty()) {
            throw new IllegalStateException(
                "Nessuna fascia gol trovata per "
                    + seasonId
                    + " / competizione FCM "
                    + sourceCompetitionId
            );
        }

        return rows;
    }

    private static String[] splitPercent(String value) {
        if (value == null || value.isBlank()) {
            return new String[0];
        }
        return value.split("%", -1);
    }

    private static String item(String[] values, int index) {
        return index >= 0 && index < values.length ? values[index].trim() : "";
    }

    private static int parseInteger(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static BigDecimal parseDecimal(String value) {
        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value.trim().replace(',', '.'));
        } catch (NumberFormatException ignored) {
            return BigDecimal.ZERO;
        }
    }

    private static String roleCode(int role) {
        return switch (role) {
            case 1 -> "PU";
            case 2 -> "DU";
            case 3 -> "CU";
            case 4 -> "AU";
            default -> "";
        };
    }

    private static String roleName(int role) {
        return switch (role) {
            case 1 -> "Portiere";
            case 2 -> "Difensore";
            case 3 -> "Centrocampista";
            case 4 -> "Attaccante";
            default -> "";
        };
    }

    private static SourceInfo readFcmSource(
            Connection connection,
            String seasonId) throws Exception {

        return readSource(
            connection,
            seasonId,
            "FCM"
        );
    }

    private static SourceInfo readSource(
            Connection connection,
            String seasonId,
            String sourceType) throws Exception {

        String sql = """
            SELECT
                source_file_id,
                import_id
            FROM rn_source_file
            WHERE season_id = ?
              AND source_type = ?
            ORDER BY import_id DESC
            LIMIT 1
            """;

        try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setString(1, seasonId);
            statement.setString(2, sourceType);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalArgumentException(
                        "Sorgente "
                            + sourceType
                            + " non trovata: "
                            + seasonId
                    );
                }

                return new SourceInfo(
                    result.getLong("source_file_id"),
                    result.getLong("import_id")
                );
            }
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
                        "Tabella raw non trovata: "
                            + sourceTableName
                    );
                }

                return result.getString("raw_table_name");
            }
        }
    }

    private static String quoteIdentifier(String value) {
        return "\""
            + value.replace("\"", "\"\"")
            + "\"";
    }

    private static void writeJson(
            Path output,
            ExportData data) throws Exception {

        try (BufferedWriter writer = Files.newBufferedWriter(
                output,
                StandardCharsets.UTF_8)) {

            writer.write("{\n");

            writeMeta(
                writer,
                data.meta()
            );

            writer.write(",\n");

            writeTeamMatches(
                writer,
                data.teamMatches()
            );

            writer.write(",\n");

            writeExpulsionDetails(
                writer,
                data.expulsionDetails()
            );

            writer.write(",\n");

            writeEventDetails(
                writer,
                data.eventDetails()
            );

            writer.write(",\n");

            writeModifierDetails(
                writer,
                data.modifierDetails()
            );

            writer.write(",\n");

            writeCleanSheetDetails(
                writer,
                data.cleanSheetDetails()
            );

            writer.write(",\n");

            writeReserveOfficeDetails(
                writer,
                data.reserveOfficeDetails()
            );

            writer.write(",\n");

            writeGoalBandDetails(
                writer,
                data.goalBandDetails()
            );

            writer.write("}\n");
        }
    }

    private static void writeMeta(
            BufferedWriter writer,
            Meta meta) throws Exception {

        writer.write("  \"meta\": {\n");

        writeStringProperty(
            writer,
            "generatedAt",
            meta.generatedAt(),
            true,
            4
        );

        writeStringProperty(
            writer,
            "projectDir",
            meta.projectDir(),
            true,
            4
        );

        writeStringProperty(
            writer,
            "stagione",
            meta.seasonId(),
            true,
            4
        );

        writeStringProperty(
            writer,
            "competizioneStoricaId",
            meta.historicalCompetitionId(),
            true,
            4
        );

        writeStringProperty(
            writer,
            "competizioneNome",
            meta.competitionName(),
            true,
            4
        );

        writeNumberProperty(
            writer,
            "idCompetizioneFcm",
            meta.sourceCompetitionId(),
            true,
            4
        );

        writeStringProperty(
            writer,
            "nomeCompetizioneDb",
            meta.databaseCompetitionName(),
            true,
            4
        );

        writer.write("    \"idGironiInclusi\": [");

        for (
            int index = 0;
            index < meta.groupIds().size();
            index++
        ) {
            if (index > 0) {
                writer.write(", ");
            }

            writer.write(
                Integer.toString(
                    meta.groupIds().get(index)
                )
            );
        }

        writer.write("],\n");

        writeStringProperty(
            writer,
            "fcmTablesDir",
            meta.fcmSource(),
            true,
            4
        );

        writeStringProperty(
            writer,
            "fcaTablesDir",
            meta.fcaSource(),
            true,
            4
        );

        writeNumberProperty(
            writer,
            "incontriAnalizzati",
            meta.matchesAnalyzed(),
            true,
            4
        );

        writeNumberProperty(
            writer,
            "partiteSquadra",
            meta.teamMatches(),
            false,
            4
        );

        writer.write("  }");
    }

    private static void writeTeamMatches(
            BufferedWriter writer,
            List<TeamMatch> rows) throws Exception {

        writer.write("  \"partiteSquadra\": [\n");

        for (int index = 0; index < rows.size(); index++) {
            TeamMatch row = rows.get(index);

            writer.write("    {\n");

            writeStringProperty(
                writer,
                "stagione",
                row.seasonId(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "competizioneStoricaId",
                row.historicalCompetitionId(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "competizioneNome",
                row.competitionName(),
                true,
                6
            );

            writeNumberProperty(
                writer,
                "idCompetizioneFcm",
                row.sourceCompetitionId(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "nomeCompetizioneDb",
                row.databaseCompetitionName(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "idGirone",
                Integer.toString(row.groupId()),
                true,
                6
            );

            writeStringProperty(
                writer,
                "gironeNome",
                row.groupName(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "idGiornata",
                Integer.toString(row.roundId()),
                true,
                6
            );

            writeStringProperty(
                writer,
                "giornata",
                row.roundDescription(),
                true,
                6
            );

            writeNumberProperty(
                writer,
                "giornataDiA",
                row.serieARound(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "dataGiornata",
                row.matchDate(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "oraGiornata",
                row.matchTime(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "dataOraGiornata",
                row.matchDateTime(),
                true,
                6
            );

            writeNumberProperty(
                writer,
                "ordineGiornata",
                row.roundOrder(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "idIncontro",
                Long.toString(row.matchId()),
                true,
                6
            );

            writeStringProperty(
                writer,
                "urlTabellino",
                row.scorecardUrl(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "urlTabellinoLocale",
                row.localScorecardUrl(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "urlTabellinoOnline",
                row.onlineScorecardUrl(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "lato",
                row.side(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "idSquadra",
                Integer.toString(row.teamId()),
                true,
                6
            );

            writeStringProperty(
                writer,
                "squadra",
                row.teamName(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "idAvversaria",
                Integer.toString(row.opponentId()),
                true,
                6
            );

            writeStringProperty(
                writer,
                "avversaria",
                row.opponentName(),
                true,
                6
            );

            writeDecimalProperty(
                writer,
                "puntiFatti",
                row.scoreFor(),
                true,
                6
            );

            writeDecimalProperty(
                writer,
                "puntiSubiti",
                row.scoreAgainst(),
                true,
                6
            );

            writeDecimalProperty(
                writer,
                "parzialeFatto",
                row.partialFor(),
                true,
                6
            );

            writeDecimalProperty(
                writer,
                "parzialeSubito",
                row.partialAgainst(),
                true,
                6
            );

            writeNumberProperty(
                writer,
                "golFatti",
                row.goalsFor(),
                true,
                6
            );

            writeNumberProperty(
                writer,
                "golSubiti",
                row.goalsAgainst(),
                true,
                6
            );

            writeNumberProperty(
                writer,
                "golRegolamentariFatti",
                row.regulationGoalsFor(),
                true,
                6
            );

            writeNumberProperty(
                writer,
                "golRegolamentariSubiti",
                row.regulationGoalsAgainst(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "risultatoRegolamentari",
                row.regulationResult(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "fonteGolRegolamentari",
                row.regulationGoalsSource(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "esito",
                row.resultCode(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "risultato",
                row.resultText(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "punteggio",
                row.scoreText(),
                false,
                6
            );

            writer.write("    }");

            if (index + 1 < rows.size()) {
                writer.write(",");
            }

            writer.write("\n");
        }

        writer.write("  ]");
    }

    private static void writeExpulsionDetails(
            BufferedWriter writer,
            List<ExpulsionDetail> rows) throws Exception {

        writer.write("  \"espulsioniDettaglio\": [\n");

        for (int index = 0; index < rows.size(); index++) {
            ExpulsionDetail row = rows.get(index);

            writer.write("    {\n");

            writeStringProperty(
                writer,
                "idIncontro",
                Long.toString(row.matchId()),
                true,
                6
            );

            writeNumberProperty(
                writer,
                "giornataDiA",
                row.serieARound(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "idSquadra",
                Integer.toString(row.teamId()),
                true,
                6
            );

            writeStringProperty(
                writer,
                "squadra",
                row.teamName(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "idGiocatore",
                Integer.toString(row.playerId()),
                true,
                6
            );

            writeStringProperty(
                writer,
                "giocatore",
                row.playerName(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "idPunteggio",
                Integer.toString(row.scoreId()),
                false,
                6
            );

            writer.write("    }");

            if (index + 1 < rows.size()) {
                writer.write(",");
            }

            writer.write("\n");
        }

        writer.write("  ]");
    }

    private static void writeEventDetails(
            BufferedWriter writer,
            List<EventDetail> rows) throws Exception {

        writer.write("  \"eventiSquadraDettaglio\": [\n");

        for (int index = 0; index < rows.size(); index++) {
            EventDetail row = rows.get(index);

            writer.write("    {\n");

            writeStringProperty(writer, "recordKey", row.recordKey(), true, 6);
            writeStringProperty(writer, "tipoEvento", row.eventType(), true, 6);
            writeStringProperty(writer, "nomeEvento", row.eventName(), true, 6);
            writeStringProperty(writer, "campoOrigine", row.sourceField(), true, 6);
            writeStringProperty(writer, "idIncontro", Long.toString(row.matchId()), true, 6);
            writeNumberProperty(writer, "giornataDiA", row.serieARound(), true, 6);
            writeStringProperty(writer, "idSquadra", Integer.toString(row.teamId()), true, 6);
            writeStringProperty(writer, "squadra", row.teamName(), true, 6);
            writeStringProperty(writer, "idGiocatore", Integer.toString(row.playerId()), true, 6);
            writeStringProperty(writer, "giocatore", row.playerName(), true, 6);
            writeStringProperty(writer, "idPunteggio", Integer.toString(row.scoreId()), true, 6);
            writeNumberProperty(writer, "valore", row.value(), false, 6);

            writer.write("    }");

            if (index + 1 < rows.size()) {
                writer.write(",");
            }

            writer.write("\n");
        }

        writer.write("  ]");
    }

    private static void writeModifierDetails(
            BufferedWriter writer,
            List<ModifierDetail> rows) throws Exception {

        writer.write("  \"modificatoriB2Dettaglio\": [\n");

        for (int index = 0; index < rows.size(); index++) {
            ModifierDetail row = rows.get(index);

            writer.write("    {\n");

            writeStringProperty(
                writer,
                "tipo",
                row.type(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "idIncontro",
                Long.toString(row.matchId()),
                true,
                6
            );

            writeNumberProperty(
                writer,
                "giornataDiA",
                row.serieARound(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "idSquadra",
                Integer.toString(row.teamId()),
                true,
                6
            );

            writeStringProperty(
                writer,
                "squadra",
                row.teamName(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "idAvversaria",
                Integer.toString(row.opponentId()),
                true,
                6
            );

            writeStringProperty(
                writer,
                "avversaria",
                row.opponentName(),
                true,
                6
            );

            writeDecimalProperty(
                writer,
                "valore",
                row.value(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "campoOrigine",
                row.sourceField(),
                false,
                6
            );

            writer.write("    }");

            if (index + 1 < rows.size()) {
                writer.write(",");
            }

            writer.write("\n");
        }

        writer.write("  ]");
    }

    private static void writeCleanSheetDetails(
            BufferedWriter writer,
            List<CleanSheetDetail> rows) throws Exception {

        writer.write("  \"cleanSheetB3Dettaglio\": [\n");

        for (int index = 0; index < rows.size(); index++) {
            CleanSheetDetail row = rows.get(index);

            writer.write("    {\n");

            writeStringProperty(writer, "tipo", "cleanSheetPortiere", true, 6);
            writeStringProperty(writer, "idIncontro", Long.toString(row.matchId()), true, 6);
            writeNumberProperty(writer, "giornataDiA", row.serieARound(), true, 6);
            writeStringProperty(writer, "idSquadra", Integer.toString(row.teamId()), true, 6);
            writeStringProperty(writer, "squadra", row.teamName(), true, 6);
            writeStringProperty(writer, "idAvversaria", Integer.toString(row.opponentId()), true, 6);
            writeStringProperty(writer, "avversaria", row.opponentName(), true, 6);
            writeStringProperty(writer, "idGiocatore", Integer.toString(row.playerId()), true, 6);
            writeStringProperty(writer, "giocatore", row.playerName(), true, 6);
            writeStringProperty(writer, "idPunteggio", Integer.toString(row.scoreId()), true, 6);
            writeNumberProperty(writer, "golSubiti", row.goalsConceded(), true, 6);
            writeDecimalProperty(writer, "valore", row.value(), true, 6);
            writeStringProperty(
                writer,
                "campoOrigine",
                "GiocatoreA.Ruolo=1 + Punteggio.GolSubiti=0",
                false,
                6
            );

            writer.write("    }");

            if (index + 1 < rows.size()) {
                writer.write(",");
            }

            writer.write("\n");
        }

        writer.write("  ]");
    }

    private static void writeStringProperty(
            BufferedWriter writer,
            String name,
            String value,
            boolean comma,
            int indent) throws Exception {

        writer.write(" ".repeat(indent));
        writer.write("\"");
        writer.write(jsonEscape(name));
        writer.write("\": ");

        if (value == null) {
            writer.write("null");
        } else {
            writer.write("\"");
            writer.write(jsonEscape(value));
            writer.write("\"");
        }

        if (comma) {
            writer.write(",");
        }

        writer.write("\n");
    }

    private static void writeNumberProperty(
            BufferedWriter writer,
            String name,
            long value,
            boolean comma,
            int indent) throws Exception {

        writer.write(" ".repeat(indent));
        writer.write("\"");
        writer.write(jsonEscape(name));
        writer.write("\": ");
        writer.write(Long.toString(value));

        if (comma) {
            writer.write(",");
        }

        writer.write("\n");
    }

    private static void writeDecimalProperty(
            BufferedWriter writer,
            String name,
            BigDecimal value,
            boolean comma,
            int indent) throws Exception {

        writer.write(" ".repeat(indent));
        writer.write("\"");
        writer.write(jsonEscape(name));
        writer.write("\": ");

        if (value == null) {
            writer.write("null");
        } else {
            writer.write(decimalText(value));
        }

        if (comma) {
            writer.write(",");
        }

        writer.write("\n");
    }

    private static String decimalText(BigDecimal value) {
        if (value == null) {
            return "";
        }

        return value
            .stripTrailingZeros()
            .toPlainString();
    }

    private static BigDecimal zeroIfNull(
            BigDecimal value) {

        return value == null
            ? BigDecimal.ZERO
            : value;
    }

    private static String emptyIfNull(
            String value) {

        return value == null
            ? ""
            : value;
    }

    private static boolean tableExists(
            Connection connection,
            String tableName) throws Exception {

        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM sqlite_master "
                    + "WHERE type = 'table' AND name = ?")) {
            statement.setString(1, tableName);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private static ScorecardBases readScorecardBases(
            Connection connection,
            String seasonId) throws Exception {

        if (!tableExists(connection, "rn_season_configuration")) {
            return new ScorecardBases(null, null);
        }

        try (PreparedStatement statement = connection.prepareStatement("""
            SELECT local_site_path, online_site_url
            FROM rn_season_configuration
            WHERE season_id = ?
            """)) {
            statement.setString(1, seasonId);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return new ScorecardBases(null, null);
                }

                String localPath = result.getString("local_site_path");
                String onlineRoot = result.getString("online_site_url");

                String localBase = null;
                if (localPath != null && !localPath.isBlank()) {
                    Path fileName = Path.of(localPath).normalize().getFileName();
                    if (fileName != null && !fileName.toString().isBlank()) {
                        localBase = "../" + fileName + "/ris.htm?Gio=";
                    }
                }

                String onlineBase = null;
                if (onlineRoot != null && !onlineRoot.isBlank()) {
                    onlineBase = onlineRoot.replaceAll("/+$", "")
                        + "/ris.htm?Gio=";
                }

                return new ScorecardBases(localBase, onlineBase);
            }
        }
    }

    private static String scorecardUrl(
            String seasonId,
            int serieARound) {

        String[] parts = seasonId.split("_", -1);

        if (parts.length != 2 || !parts[0].matches("\\d{4}")) {
            throw new IllegalArgumentException(
                "Stagione non valida per URL tabellino: "
                    + seasonId
            );
        }

        return "../lega"
            + parts[0]
            + "/ris.htm?Gio="
            + serieARound;
    }
    private static String outputHistoricalCompetitionId(
            String competitionName) {

        return switch (competitionName) {
            case "Coppa Serie A" ->
                "coppa_lega_serie_a";

            case "Coppa Serie B" ->
                "coppa_lega_serie_b";

            case "Coppa Serie C" ->
                "coppa_lega_serie_c";

            default ->
                historicalCompetitionId(
                    competitionName
                );
        };
    }

    private static String outputCompetitionName(
            String competitionName) {

        return switch (competitionName) {
            case "Coppa Serie A" ->
                "Coppa di Lega Serie A";

            case "Coppa Serie B" ->
                "Coppa di Lega Serie B";

            case "Coppa Serie C" ->
                "Coppa di Lega Serie C";

            default -> competitionName;
        };
    }

    private static String historicalCompetitionId(
            String competitionName) {

        return competitionName
            .trim()
            .toLowerCase(Locale.ROOT)
            .replace('\u00e0', 'a')
            .replace('\u00e8', 'e')
            .replace('\u00e9', 'e')
            .replace('\u00ec', 'i')
            .replace('\u00f2', 'o')
            .replace('\u00f9', 'u')
            .replaceAll("[^a-z0-9]+", "_")
            .replaceAll("^_+|_+$", "");
    }

    private static String jsonEscape(String value) {
        StringBuilder escaped = new StringBuilder();

        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);

            switch (current) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");

                default -> {
                    if (current < 0x20) {
                        escaped.append(
                            String.format(
                                Locale.ROOT,
                                "\\u%04x",
                                (int) current
                            )
                        );
                    } else {
                        escaped.append(current);
                    }
                }
            }
        }

        return escaped.toString();
    }

    private record CompetitionInfo(
        long identityId,
        int sourceCompetitionId
    ) {
    }

    private record Meta(
        String generatedAt,
        String projectDir,
        String seasonId,
        String historicalCompetitionId,
        String competitionName,
        int sourceCompetitionId,
        String databaseCompetitionName,
        List<Integer> groupIds,
        String fcmSource,
        String fcaSource,
        int matchesAnalyzed,
        int teamMatches
    ) {
    }

    private record ScorecardBases(
        String localBase,
        String onlineBase
    ) {
        String localUrl(int serieARound) {
            return localBase == null ? null : localBase + serieARound;
        }

        String onlineUrl(int serieARound) {
            return onlineBase == null ? null : onlineBase + serieARound;
        }
    }

    private record TeamMatch(
        String seasonId,
        String historicalCompetitionId,
        String competitionName,
        int sourceCompetitionId,
        String databaseCompetitionName,
        int groupId,
        String groupName,
        int roundId,
        String roundDescription,
        int serieARound,
        String matchDate,
        String matchTime,
        String matchDateTime,
        int roundOrder,
        long matchId,
        String scorecardUrl,
        String localScorecardUrl,
        String onlineScorecardUrl,
        String side,
        int teamId,
        String teamName,
        int opponentId,
        String opponentName,
        BigDecimal scoreFor,
        BigDecimal scoreAgainst,
        BigDecimal partialFor,
        BigDecimal partialAgainst,
        int goalsFor,
        int goalsAgainst,
        int regulationGoalsFor,
        int regulationGoalsAgainst,
        String regulationResult,
        String regulationGoalsSource,
        String resultCode,
        String resultText,
        String scoreText
    ) {
    }

    private record SourceInfo(
        long sourceFileId,
        long importId
    ) {
    }

    private record ExpulsionDetail(
        long matchId,
        int serieARound,
        int teamId,
        String teamName,
        int playerId,
        String playerName,
        int scoreId
    ) {
    }

    private static void writeGoalBandDetails(
            BufferedWriter writer,
            List<GoalBandDetail> rows) throws Exception {

        writer.write("  \"fasceGolDettaglio\": [\n");

        for (int index = 0; index < rows.size(); index++) {
            GoalBandDetail row = rows.get(index);
            writer.write("    {\n");
            writeNumberProperty(writer, "idCompetizioneFcm", row.sourceCompetitionId(), true, 6);
            writeStringProperty(writer, "idFascia", row.sourceBandId(), true, 6);
            writeDecimalProperty(writer, "min", row.minScore(), true, 6);
            writeDecimalProperty(writer, "max", row.maxScore(), true, 6);
            writeNumberProperty(writer, "gol", row.goals(), false, 6);
            writer.write("    }");
            if (index + 1 < rows.size()) {
                writer.write(",");
            }
            writer.write("\n");
        }

        writer.write("  ]");
    }

    private record GoalBandDetail(
        int sourceCompetitionId,
        String sourceBandId,
        BigDecimal minScore,
        BigDecimal maxScore,
        int goals
    ) {
    }

    private static void writeReserveOfficeDetails(
            BufferedWriter writer,
            List<ReserveOfficeDetail> rows) throws Exception {

        writer.write("  \"riserveUfficioDettaglio\": [\n");

        for (int index = 0; index < rows.size(); index++) {
            ReserveOfficeDetail row = rows.get(index);
            writer.write("    {\n");
            writeStringProperty(writer, "idIncontro", Long.toString(row.matchId()), true, 6);
            writeNumberProperty(writer, "giornataDiA", row.serieARound(), true, 6);
            writeStringProperty(writer, "idSquadra", Integer.toString(row.teamId()), true, 6);
            writeStringProperty(writer, "squadra", row.teamName(), true, 6);
            writeStringProperty(writer, "idAvversaria", Integer.toString(row.opponentId()), true, 6);
            writeStringProperty(writer, "avversaria", row.opponentName(), true, 6);
            writeStringProperty(writer, "tipoRU", row.roleCode(), true, 6);
            writeStringProperty(writer, "ruoloRU", row.roleName(), true, 6);
            writeNumberProperty(writer, "ordine", row.order(), true, 6);
            writeStringProperty(writer, "votoTabellino", row.vote(), true, 6);
            writeStringProperty(writer, "modifTabellino", row.modifier(), true, 6);
            writeStringProperty(writer, "totTabellino", row.total(), true, 6);
            writeDecimalProperty(writer, "valoreRU", row.value(), false, 6);
            writer.write("    }");
            if (index + 1 < rows.size()) {
                writer.write(",");
            }
            writer.write("\n");
        }

        writer.write("  ]");
    }

    private record ReserveOfficeDetail(
        long matchId,
        int serieARound,
        int teamId,
        String teamName,
        int opponentId,
        String opponentName,
        String roleCode,
        String roleName,
        int order,
        String vote,
        String modifier,
        String total,
        BigDecimal value
    ) {
    }

    private record EventDetail(
        String recordKey,
        String eventType,
        String eventName,
        String sourceField,
        long matchId,
        int serieARound,
        int teamId,
        String teamName,
        int playerId,
        String playerName,
        int scoreId,
        int value
    ) {
    }

    private record CleanSheetDetail(
        long matchId,
        int serieARound,
        int teamId,
        String teamName,
        int opponentId,
        String opponentName,
        int playerId,
        String playerName,
        int scoreId,
        int goalsConceded,
        BigDecimal value
    ) {
    }

    private record ModifierDetail(
        String type,
        long matchId,
        int serieARound,
        int teamId,
        String teamName,
        int opponentId,
        String opponentName,
        BigDecimal value,
        String sourceField
    ) {
    }

    private record ExportData(
        Meta meta,
        List<TeamMatch> teamMatches,
        List<ExpulsionDetail> expulsionDetails,
        List<EventDetail> eventDetails,
        List<ModifierDetail> modifierDetails,
        List<CleanSheetDetail> cleanSheetDetails,
        List<ReserveOfficeDetail> reserveOfficeDetails,
        List<GoalBandDetail> goalBandDetails
    ) {
    }
}
