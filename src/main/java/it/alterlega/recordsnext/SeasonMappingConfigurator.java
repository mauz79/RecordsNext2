package it.alterlega.recordsnext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Configuratore transazionale delle associazioni storiche di squadre e
 * competizioni. Opera esclusivamente sul database SQLite gia popolato da
 * RawSqliteImporter e ConfigurationSchema.
 */
public final class SeasonMappingConfigurator {

    private SeasonMappingConfigurator() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            printUsage();
            System.exit(2);
        }

        Path database = Path.of(args[0]).toAbsolutePath().normalize();
        if (!Files.isRegularFile(database)) {
            throw new IllegalArgumentException("Database non trovato: " + database);
        }

        Class.forName("org.sqlite.JDBC");

        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + database)) {

            configureConnection(connection);
            requireSchema(connection);

            String command = args[1].trim().toLowerCase(Locale.ROOT);
            switch (command) {
                case "show-seasons" -> showSeasons(connection, args);
                case "pending" -> showPending(connection, args);
                case "proposals" -> showProposals(connection, args);
                case "validate" -> validateSeason(connection, args, true);
                case "auto-exact" -> autoExact(connection, args);
                case "associate-team" -> associateTeam(connection, args);
                case "new-team" -> createTeamIdentity(connection, args);
                case "associate-competition" -> associateCompetition(connection, args);
                case "new-competition" -> createCompetitionIdentity(connection, args);
                default -> {
                    printUsage();
                    System.exit(2);
                }
            }
        }
    }

    private static void configureConnection(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("PRAGMA busy_timeout = 10000");
        }
    }

    private static void requireSchema(Connection connection) throws Exception {
        String[] required = {
            "rn_season",
            "rn_source_file",
            "rn_competition_season",
            "rn_team_season",
            "rn_competition_identity",
            "rn_team_identity",
            "rn_competition_mapping",
            "rn_team_mapping"
        };

        for (String table : required) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT COUNT(*) FROM sqlite_master "
                        + "WHERE type = 'table' AND name = ?")) {
                statement.setString(1, table);
                try (ResultSet result = statement.executeQuery()) {
                    result.next();
                    if (result.getInt(1) != 1) {
                        throw new IllegalStateException(
                            "Schema RecordsNext incompleto: tabella mancante " + table
                        );
                    }
                }
            }
        }
    }

    private static void showSeasons(Connection connection, String[] args)
            throws Exception {
        requireArgCount(args, 2, "<db> show-seasons");

        String sql = """
            SELECT
                s.season_id,
                s.is_anchor,
                (SELECT COUNT(*) FROM rn_source_file f
                 WHERE f.season_id = s.season_id AND f.source_type = 'FCM') AS fcm,
                (SELECT COUNT(*) FROM rn_source_file f
                 WHERE f.season_id = s.season_id AND f.source_type = 'FCA') AS fca,
                (SELECT COUNT(*)
                 FROM rn_competition_mapping cm
                 JOIN rn_competition_season cs
                   ON cs.competition_season_id = cm.competition_season_id
                 WHERE cs.season_id = s.season_id
                   AND cm.mapping_status = 'DA_CONFIGURARE') AS pending_comp,
                (SELECT COUNT(*)
                 FROM rn_team_mapping tm
                 JOIN rn_team_season ts
                   ON ts.team_season_id = tm.team_season_id
                 WHERE ts.season_id = s.season_id
                   AND tm.mapping_status = 'DA_CONFIGURARE') AS pending_team
            FROM rn_season s
            ORDER BY COALESCE(s.sort_order, 0) DESC, s.season_id DESC
            """;

        System.out.printf(
            Locale.ROOT,
            "%-11s %-6s %3s %3s %6s %6s %-10s%n",
            "STAGIONE", "ANCORA", "FCM", "FCA", "COMP", "TEAM", "ESITO"
        );

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            while (result.next()) {
                int fcm = result.getInt("fcm");
                int fca = result.getInt("fca");
                int pendingComp = result.getInt("pending_comp");
                int pendingTeam = result.getInt("pending_team");
                String outcome = fcm == 1 && fca == 1
                    && pendingComp == 0 && pendingTeam == 0
                    ? "COMPLETA"
                    : "IN_CORSO";

                System.out.printf(
                    Locale.ROOT,
                    "%-11s %-6s %3d %3d %6d %6d %-10s%n",
                    result.getString("season_id"),
                    result.getInt("is_anchor") == 1 ? "SI" : "NO",
                    fcm,
                    fca,
                    pendingComp,
                    pendingTeam,
                    outcome
                );
            }
        }
    }

    private static void showPending(Connection connection, String[] args)
            throws Exception {
        requireArgCount(args, 3, "<db> pending <stagione>");
        String seasonId = requireSeason(connection, args[2]);

        System.out.println("COMPETIZIONI DA CONFIGURARE");
        printPendingCompetitions(connection, seasonId);
        System.out.println();
        System.out.println("SQUADRE DA CONFIGURARE");
        printPendingTeams(connection, seasonId);
    }

    private static void showProposals(Connection connection, String[] args)
            throws Exception {
        requireArgCount(args, 3, "<db> proposals <stagione>");
        String seasonId = requireSeason(connection, args[2]);

        System.out.println("COMPETIZIONI");
        printCompetitionProposals(connection, seasonId);
        System.out.println();
        System.out.println("SQUADRE");
        printTeamProposals(connection, seasonId);
    }

    private static void autoExact(Connection connection, String[] args)
            throws Exception {
        requireArgCount(args, 3, "<db> auto-exact <stagione>");
        String seasonId = requireSeason(connection, args[2]);

        runTransaction(connection, () -> {
            int competitions = applyUnambiguousExactCompetitionMappings(
                connection, seasonId
            );
            int teams = applyUnambiguousExactTeamMappings(connection, seasonId);
            System.out.println("Associazioni esatte non ambigue applicate");
            System.out.println("Competizioni: " + competitions);
            System.out.println("Squadre     : " + teams);
        });
    }

    private static void associateTeam(Connection connection, String[] args)
            throws Exception {
        requireArgCount(
            args,
            4,
            "<db> associate-team <team-season-id> <team-identity-id>"
        );
        long teamSeasonId = parsePositiveLong(args[2], "team-season-id");
        long teamIdentityId = parsePositiveLong(args[3], "team-identity-id");

        runTransaction(connection, () -> {
            SeasonEntity team = requireTeamSeason(connection, teamSeasonId);
            Identity identity = requireTeamIdentity(connection, teamIdentityId);
            requireIdentityAvailableForTeam(
                connection, team.seasonId(), teamSeasonId, teamIdentityId
            );
            updateTeamMapping(
                connection,
                teamSeasonId,
                teamIdentityId,
                "MANUAL",
                null
            );
            System.out.println(
                "Squadra associata: " + team.name() + " -> " + identity.name()
            );
        });
    }

    private static void createTeamIdentity(Connection connection, String[] args)
            throws Exception {
        requireArgCount(args, 3, "<db> new-team <team-season-id>");
        long teamSeasonId = parsePositiveLong(args[2], "team-season-id");

        runTransaction(connection, () -> {
            SeasonEntity team = requireTeamSeason(connection, teamSeasonId);
            requirePendingTeam(connection, teamSeasonId);
            long identityId = insertTeamIdentity(connection, team);
            updateTeamMapping(
                connection,
                teamSeasonId,
                identityId,
                "NEW_HISTORICAL_IDENTITY",
                null
            );
            System.out.println(
                "Nuova identita squadra: " + identityId + " | " + team.name()
            );
        });
    }

    private static void associateCompetition(Connection connection, String[] args)
            throws Exception {
        requireArgCount(
            args,
            4,
            "<db> associate-competition "
                + "<competition-season-id> <competition-identity-id>"
        );
        long competitionSeasonId = parsePositiveLong(
            args[2], "competition-season-id"
        );
        long competitionIdentityId = parsePositiveLong(
            args[3], "competition-identity-id"
        );

        runTransaction(connection, () -> {
            SeasonEntity competition = requireCompetitionSeason(
                connection, competitionSeasonId
            );
            Identity identity = requireCompetitionIdentity(
                connection, competitionIdentityId
            );
            requireIdentityAvailableForCompetition(
                connection,
                competition.seasonId(),
                competitionSeasonId,
                competitionIdentityId
            );
            updateCompetitionMapping(
                connection,
                competitionSeasonId,
                competitionIdentityId,
                "MANUAL",
                null
            );
            System.out.println(
                "Competizione associata: " + competition.name()
                    + " -> " + identity.name()
            );
        });
    }

    private static void createCompetitionIdentity(
            Connection connection,
            String[] args) throws Exception {
        requireArgCount(
            args,
            3,
            "<db> new-competition <competition-season-id>"
        );
        long competitionSeasonId = parsePositiveLong(
            args[2], "competition-season-id"
        );

        runTransaction(connection, () -> {
            SeasonEntity competition = requireCompetitionSeason(
                connection, competitionSeasonId
            );
            requirePendingCompetition(connection, competitionSeasonId);
            long identityId = insertCompetitionIdentity(connection, competition);
            updateCompetitionMapping(
                connection,
                competitionSeasonId,
                identityId,
                "NEW_HISTORICAL_IDENTITY",
                null
            );
            System.out.println(
                "Nuova identita competizione: " + identityId
                    + " | " + competition.name()
            );
        });
    }

    private static boolean validateSeason(
            Connection connection,
            String[] args,
            boolean print) throws Exception {
        requireArgCount(args, 3, "<db> validate <stagione>");
        String seasonId = requireSeason(connection, args[2]);
        Validation validation = validate(connection, seasonId);

        if (print) {
            System.out.println("Stagione       : " + seasonId);
            System.out.println("Sorgenti FCM   : " + validation.fcmSources());
            System.out.println("Sorgenti FCA   : " + validation.fcaSources());
            System.out.println("Comp. pendenti : " + validation.pendingCompetitions());
            System.out.println("Team pendenti  : " + validation.pendingTeams());
            System.out.println("Dup. competiz. : " + validation.duplicateCompetitions());
            System.out.println("Dup. squadre   : " + validation.duplicateTeams());
            System.out.println("Mapping orfani : " + validation.orphanMappings());
            System.out.println("ESITO          : "
                + (validation.valid() ? "VALIDA" : "NON VALIDA"));
        }

        if (!validation.valid()) {
            throw new IllegalStateException(
                "Configurazione stagione non valida: " + seasonId
            );
        }
        return true;
    }

    private static Validation validate(Connection connection, String seasonId)
            throws Exception {
        int fcm = count(connection, """
            SELECT COUNT(*) FROM rn_source_file
            WHERE season_id = ? AND source_type = 'FCM'
            """, seasonId);
        int fca = count(connection, """
            SELECT COUNT(*) FROM rn_source_file
            WHERE season_id = ? AND source_type = 'FCA'
            """, seasonId);
        int pendingCompetitions = count(connection, """
            SELECT COUNT(*)
            FROM rn_competition_mapping cm
            JOIN rn_competition_season cs
              ON cs.competition_season_id = cm.competition_season_id
            WHERE cs.season_id = ?
              AND cm.mapping_status = 'DA_CONFIGURARE'
            """, seasonId);
        int pendingTeams = count(connection, """
            SELECT COUNT(*)
            FROM rn_team_mapping tm
            JOIN rn_team_season ts
              ON ts.team_season_id = tm.team_season_id
            WHERE ts.season_id = ?
              AND tm.mapping_status = 'DA_CONFIGURARE'
            """, seasonId);
        int duplicateCompetitions = count(connection, """
            SELECT COUNT(*) FROM (
                SELECT cm.competition_identity_id
                FROM rn_competition_mapping cm
                JOIN rn_competition_season cs
                  ON cs.competition_season_id = cm.competition_season_id
                WHERE cs.season_id = ?
                  AND cm.mapping_status = 'ASSOCIATA'
                GROUP BY cm.competition_identity_id
                HAVING COUNT(*) > 1
            )
            """, seasonId);
        int duplicateTeams = count(connection, """
            SELECT COUNT(*) FROM (
                SELECT tm.team_identity_id
                FROM rn_team_mapping tm
                JOIN rn_team_season ts
                  ON ts.team_season_id = tm.team_season_id
                WHERE ts.season_id = ?
                  AND tm.mapping_status = 'ASSOCIATA'
                GROUP BY tm.team_identity_id
                HAVING COUNT(*) > 1
            )
            """, seasonId);
        int orphanMappings = count(connection, """
            SELECT
                (SELECT COUNT(*)
                 FROM rn_competition_mapping cm
                 JOIN rn_competition_season cs
                   ON cs.competition_season_id = cm.competition_season_id
                 LEFT JOIN rn_competition_identity ci
                   ON ci.competition_identity_id = cm.competition_identity_id
                 WHERE cs.season_id = ?
                   AND cm.mapping_status = 'ASSOCIATA'
                   AND ci.competition_identity_id IS NULL)
                +
                (SELECT COUNT(*)
                 FROM rn_team_mapping tm
                 JOIN rn_team_season ts
                   ON ts.team_season_id = tm.team_season_id
                 LEFT JOIN rn_team_identity ti
                   ON ti.team_identity_id = tm.team_identity_id
                 WHERE ts.season_id = ?
                   AND tm.mapping_status = 'ASSOCIATA'
                   AND ti.team_identity_id IS NULL)
            """, seasonId, seasonId);

        return new Validation(
            fcm,
            fca,
            pendingCompetitions,
            pendingTeams,
            duplicateCompetitions,
            duplicateTeams,
            orphanMappings
        );
    }

    private static void printPendingCompetitions(
            Connection connection,
            String seasonId) throws Exception {
        String sql = """
            SELECT cs.competition_season_id, cs.source_competition_id, cs.source_name
            FROM rn_competition_season cs
            JOIN rn_competition_mapping cm
              ON cm.competition_season_id = cs.competition_season_id
            WHERE cs.season_id = ?
              AND cm.mapping_status = 'DA_CONFIGURARE'
            ORDER BY cs.source_name COLLATE NOCASE, cs.competition_season_id
            """;
        printPending(connection, sql, seasonId);
    }

    private static void printPendingTeams(
            Connection connection,
            String seasonId) throws Exception {
        String sql = """
            SELECT ts.team_season_id, ts.source_team_id, ts.source_name
            FROM rn_team_season ts
            JOIN rn_team_mapping tm
              ON tm.team_season_id = ts.team_season_id
            WHERE ts.season_id = ?
              AND tm.mapping_status = 'DA_CONFIGURARE'
            ORDER BY ts.source_name COLLATE NOCASE, ts.team_season_id
            """;
        printPending(connection, sql, seasonId);
    }

    private static void printPending(
            Connection connection,
            String sql,
            String seasonId) throws Exception {
        int rows = 0;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, seasonId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    rows++;
                    System.out.printf(
                        Locale.ROOT,
                        "%d | sorgente=%d | %s%n",
                        result.getLong(1),
                        result.getLong(2),
                        result.getString(3)
                    );
                }
            }
        }
        if (rows == 0) {
            System.out.println("- nessuna -");
        }
    }

    private static void printCompetitionProposals(
            Connection connection,
            String seasonId) throws Exception {
        List<Identity> identities = readCompetitionIdentities(connection);
        String sql = """
            SELECT cs.competition_season_id, cs.source_name
            FROM rn_competition_season cs
            JOIN rn_competition_mapping cm
              ON cm.competition_season_id = cs.competition_season_id
            WHERE cs.season_id = ?
              AND cm.mapping_status = 'DA_CONFIGURARE'
            ORDER BY cs.source_name COLLATE NOCASE
            """;
        printProposals(connection, sql, seasonId, identities);
    }

    private static void printTeamProposals(
            Connection connection,
            String seasonId) throws Exception {
        List<Identity> identities = readTeamIdentities(connection);
        String sql = """
            SELECT ts.team_season_id, ts.source_name
            FROM rn_team_season ts
            JOIN rn_team_mapping tm
              ON tm.team_season_id = ts.team_season_id
            WHERE ts.season_id = ?
              AND tm.mapping_status = 'DA_CONFIGURARE'
            ORDER BY ts.source_name COLLATE NOCASE
            """;
        printProposals(connection, sql, seasonId, identities);
    }

    private static void printProposals(
            Connection connection,
            String sql,
            String seasonId,
            List<Identity> identities) throws Exception {
        int rows = 0;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, seasonId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    rows++;
                    long localId = result.getLong(1);
                    String localName = result.getString(2);
                    System.out.println(localId + " | " + localName);

                    identities.stream()
                        .map(identity -> new ScoredIdentity(
                            identity,
                            similarity(localName, identity.name())
                        ))
                        .sorted(
                            Comparator.comparingDouble(ScoredIdentity::score)
                                .reversed()
                                .thenComparingLong(value -> value.identity().id())
                        )
                        .limit(5)
                        .forEach(candidate -> System.out.printf(
                            Locale.ROOT,
                            "    %.3f | %d | %s%n",
                            candidate.score(),
                            candidate.identity().id(),
                            candidate.identity().name()
                        ));
                    System.out.println("    [NON GESTITA -> nuova identita storica]");
                }
            }
        }
        if (rows == 0) {
            System.out.println("- nessuna -");
        }
    }

    private static int applyUnambiguousExactTeamMappings(
            Connection connection,
            String seasonId) throws Exception {
        List<ExactCandidate> candidates = new ArrayList<>();
        String sql = """
            SELECT
                ts.team_season_id,
                MIN(ti.team_identity_id) AS identity_id,
                COUNT(*) AS candidate_count
            FROM rn_team_season ts
            JOIN rn_team_mapping tm
              ON tm.team_season_id = ts.team_season_id
            JOIN rn_team_identity ti
              ON LOWER(TRIM(ti.canonical_name)) = LOWER(TRIM(ts.source_name))
            WHERE ts.season_id = ?
              AND tm.mapping_status = 'DA_CONFIGURARE'
              AND NOT EXISTS (
                    SELECT 1
                    FROM rn_team_mapping used
                    JOIN rn_team_season used_ts
                      ON used_ts.team_season_id = used.team_season_id
                    WHERE used_ts.season_id = ts.season_id
                      AND used.mapping_status = 'ASSOCIATA'
                      AND used.team_identity_id = ti.team_identity_id
                )
            GROUP BY ts.team_season_id
            HAVING COUNT(*) = 1
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, seasonId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    candidates.add(new ExactCandidate(
                        result.getLong("team_season_id"),
                        result.getLong("identity_id")
                    ));
                }
            }
        }

        for (ExactCandidate candidate : candidates) {
            updateTeamMapping(
                connection,
                candidate.seasonEntityId(),
                candidate.identityId(),
                "EXACT_NAME",
                null
            );
        }
        return candidates.size();
    }

    private static int applyUnambiguousExactCompetitionMappings(
            Connection connection,
            String seasonId) throws Exception {
        List<ExactCandidate> candidates = new ArrayList<>();
        String sql = """
            SELECT
                cs.competition_season_id,
                MIN(ci.competition_identity_id) AS identity_id,
                COUNT(*) AS candidate_count
            FROM rn_competition_season cs
            JOIN rn_competition_mapping cm
              ON cm.competition_season_id = cs.competition_season_id
            JOIN rn_competition_identity ci
              ON LOWER(TRIM(ci.canonical_name)) = LOWER(TRIM(cs.source_name))
            WHERE cs.season_id = ?
              AND cm.mapping_status = 'DA_CONFIGURARE'
              AND NOT EXISTS (
                    SELECT 1
                    FROM rn_competition_mapping used
                    JOIN rn_competition_season used_cs
                      ON used_cs.competition_season_id = used.competition_season_id
                    WHERE used_cs.season_id = cs.season_id
                      AND used.mapping_status = 'ASSOCIATA'
                      AND used.competition_identity_id = ci.competition_identity_id
                )
            GROUP BY cs.competition_season_id
            HAVING COUNT(*) = 1
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, seasonId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    candidates.add(new ExactCandidate(
                        result.getLong("competition_season_id"),
                        result.getLong("identity_id")
                    ));
                }
            }
        }

        for (ExactCandidate candidate : candidates) {
            updateCompetitionMapping(
                connection,
                candidate.seasonEntityId(),
                candidate.identityId(),
                "EXACT_NAME",
                null
            );
        }
        return candidates.size();
    }

    private static void requireIdentityAvailableForTeam(
            Connection connection,
            String seasonId,
            long teamSeasonId,
            long teamIdentityId) throws Exception {
        int used = count(connection, """
            SELECT COUNT(*)
            FROM rn_team_mapping tm
            JOIN rn_team_season ts
              ON ts.team_season_id = tm.team_season_id
            WHERE ts.season_id = ?
              AND tm.mapping_status = 'ASSOCIATA'
              AND tm.team_identity_id = ?
              AND tm.team_season_id <> ?
            """, seasonId, teamIdentityId, teamSeasonId);
        if (used != 0) {
            throw new IllegalStateException(
                "Identita squadra gia usata nella stagione " + seasonId
                    + ": " + teamIdentityId
            );
        }
    }

    private static void requireIdentityAvailableForCompetition(
            Connection connection,
            String seasonId,
            long competitionSeasonId,
            long competitionIdentityId) throws Exception {
        int used = count(connection, """
            SELECT COUNT(*)
            FROM rn_competition_mapping cm
            JOIN rn_competition_season cs
              ON cs.competition_season_id = cm.competition_season_id
            WHERE cs.season_id = ?
              AND cm.mapping_status = 'ASSOCIATA'
              AND cm.competition_identity_id = ?
              AND cm.competition_season_id <> ?
            """, seasonId, competitionIdentityId, competitionSeasonId);
        if (used != 0) {
            throw new IllegalStateException(
                "Identita competizione gia usata nella stagione " + seasonId
                    + ": " + competitionIdentityId
            );
        }
    }

    private static void requirePendingTeam(Connection connection, long id)
            throws Exception {
        int count = count(connection, """
            SELECT COUNT(*) FROM rn_team_mapping
            WHERE team_season_id = ? AND mapping_status = 'DA_CONFIGURARE'
            """, id);
        if (count != 1) {
            throw new IllegalStateException(
                "La squadra stagionale non e DA_CONFIGURARE: " + id
            );
        }
    }

    private static void requirePendingCompetition(Connection connection, long id)
            throws Exception {
        int count = count(connection, """
            SELECT COUNT(*) FROM rn_competition_mapping
            WHERE competition_season_id = ? AND mapping_status = 'DA_CONFIGURARE'
            """, id);
        if (count != 1) {
            throw new IllegalStateException(
                "La competizione stagionale non e DA_CONFIGURARE: " + id
            );
        }
    }

    private static long insertTeamIdentity(
            Connection connection,
            SeasonEntity team) throws Exception {
        String sql = """
            INSERT INTO rn_team_identity (
                anchor_season_id,
                anchor_team_season_id,
                canonical_name,
                created_at
            ) VALUES (?, ?, ?, ?)
            """;
        try (PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, team.seasonId());
            statement.setLong(2, team.id());
            statement.setString(3, team.name());
            statement.setString(4, Instant.now().toString());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new IllegalStateException(
                        "Identita squadra non creata per " + team.id()
                    );
                }
                return keys.getLong(1);
            }
        }
    }

    private static long insertCompetitionIdentity(
            Connection connection,
            SeasonEntity competition) throws Exception {
        String sql = """
            INSERT INTO rn_competition_identity (
                anchor_season_id,
                anchor_competition_season_id,
                canonical_name,
                created_at
            ) VALUES (?, ?, ?, ?)
            """;
        try (PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, competition.seasonId());
            statement.setLong(2, competition.id());
            statement.setString(3, competition.name());
            statement.setString(4, Instant.now().toString());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new IllegalStateException(
                        "Identita competizione non creata per " + competition.id()
                    );
                }
                return keys.getLong(1);
            }
        }
    }

    private static void updateTeamMapping(
            Connection connection,
            long teamSeasonId,
            long teamIdentityId,
            String method,
            String notes) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement("""
            UPDATE rn_team_mapping
            SET team_identity_id = ?,
                mapping_status = 'ASSOCIATA',
                mapping_method = ?,
                notes = ?,
                updated_at = ?
            WHERE team_season_id = ?
            """)) {
            statement.setLong(1, teamIdentityId);
            statement.setString(2, method);
            statement.setString(3, notes);
            statement.setString(4, Instant.now().toString());
            statement.setLong(5, teamSeasonId);
            if (statement.executeUpdate() != 1) {
                throw new IllegalStateException(
                    "Mapping squadra non trovato: " + teamSeasonId
                );
            }
        }
    }

    private static void updateCompetitionMapping(
            Connection connection,
            long competitionSeasonId,
            long competitionIdentityId,
            String method,
            String notes) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement("""
            UPDATE rn_competition_mapping
            SET competition_identity_id = ?,
                mapping_status = 'ASSOCIATA',
                mapping_method = ?,
                notes = ?,
                updated_at = ?
            WHERE competition_season_id = ?
            """)) {
            statement.setLong(1, competitionIdentityId);
            statement.setString(2, method);
            statement.setString(3, notes);
            statement.setString(4, Instant.now().toString());
            statement.setLong(5, competitionSeasonId);
            if (statement.executeUpdate() != 1) {
                throw new IllegalStateException(
                    "Mapping competizione non trovato: " + competitionSeasonId
                );
            }
        }
    }

    private static SeasonEntity requireTeamSeason(Connection connection, long id)
            throws Exception {
        return requireSeasonEntity(
            connection,
            "SELECT team_season_id, season_id, source_name "
                + "FROM rn_team_season WHERE team_season_id = ?",
            id,
            "Squadra stagionale"
        );
    }

    private static SeasonEntity requireCompetitionSeason(
            Connection connection,
            long id) throws Exception {
        return requireSeasonEntity(
            connection,
            "SELECT competition_season_id, season_id, source_name "
                + "FROM rn_competition_season WHERE competition_season_id = ?",
            id,
            "Competizione stagionale"
        );
    }

    private static SeasonEntity requireSeasonEntity(
            Connection connection,
            String sql,
            long id,
            String label) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalArgumentException(label + " non trovata: " + id);
                }
                return new SeasonEntity(
                    result.getLong(1),
                    result.getString(2),
                    result.getString(3)
                );
            }
        }
    }

    private static Identity requireTeamIdentity(Connection connection, long id)
            throws Exception {
        return requireIdentity(
            connection,
            "SELECT team_identity_id, canonical_name "
                + "FROM rn_team_identity WHERE team_identity_id = ?",
            id,
            "Identita squadra"
        );
    }

    private static Identity requireCompetitionIdentity(
            Connection connection,
            long id) throws Exception {
        return requireIdentity(
            connection,
            "SELECT competition_identity_id, canonical_name "
                + "FROM rn_competition_identity WHERE competition_identity_id = ?",
            id,
            "Identita competizione"
        );
    }

    private static Identity requireIdentity(
            Connection connection,
            String sql,
            long id,
            String label) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalArgumentException(label + " non trovata: " + id);
                }
                return new Identity(result.getLong(1), result.getString(2));
            }
        }
    }

    private static List<Identity> readTeamIdentities(Connection connection)
            throws Exception {
        return readIdentities(
            connection,
            "SELECT team_identity_id, canonical_name "
                + "FROM rn_team_identity ORDER BY canonical_name COLLATE NOCASE"
        );
    }

    private static List<Identity> readCompetitionIdentities(Connection connection)
            throws Exception {
        return readIdentities(
            connection,
            "SELECT competition_identity_id, canonical_name "
                + "FROM rn_competition_identity ORDER BY canonical_name COLLATE NOCASE"
        );
    }

    private static List<Identity> readIdentities(
            Connection connection,
            String sql) throws Exception {
        List<Identity> identities = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            while (result.next()) {
                identities.add(new Identity(result.getLong(1), result.getString(2)));
            }
        }
        return identities;
    }

    private static String requireSeason(Connection connection, String raw)
            throws Exception {
        String seasonId = raw.trim();
        int count = count(
            connection,
            "SELECT COUNT(*) FROM rn_season WHERE season_id = ?",
            seasonId
        );
        if (count != 1) {
            throw new IllegalArgumentException("Stagione non trovata: " + seasonId);
        }
        return seasonId;
    }

    private static int count(
            Connection connection,
            String sql,
            Object... parameters) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1);
            }
        }
    }

    private static long parsePositiveLong(String raw, String label) {
        try {
            long value = Long.parseLong(raw);
            if (value <= 0) {
                throw new NumberFormatException("non positivo");
            }
            return value;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                label + " non valido: " + raw,
                exception
            );
        }
    }

    private static void requireArgCount(
            String[] args,
            int expected,
            String usage) {
        if (args.length != expected) {
            throw new IllegalArgumentException("Uso: " + usage);
        }
    }

    private static double similarity(String left, String right) {
        String a = normalize(left);
        String b = normalize(right);
        if (a.equals(b)) {
            return 1.0d;
        }
        int max = Math.max(a.length(), b.length());
        return max == 0 ? 1.0d : 1.0d - ((double) levenshtein(a, b) / max);
    }

    private static String normalize(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "")
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", " ")
            .trim()
            .replaceAll("\\s+", " ");
    }

    private static int levenshtein(String left, String right) {
        int[] previous = new int[right.length() + 1];
        int[] current = new int[right.length() + 1];
        for (int j = 0; j <= right.length(); j++) {
            previous[j] = j;
        }
        for (int i = 1; i <= left.length(); i++) {
            current[0] = i;
            for (int j = 1; j <= right.length(); j++) {
                int cost = left.charAt(i - 1) == right.charAt(j - 1) ? 0 : 1;
                current[j] = Math.min(
                    Math.min(current[j - 1] + 1, previous[j] + 1),
                    previous[j - 1] + cost
                );
            }
            int[] swap = previous;
            previous = current;
            current = swap;
        }
        return previous[right.length()];
    }

    private static void runTransaction(Connection connection, SqlAction action)
            throws Exception {
        boolean oldAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            action.run();
            connection.commit();
        } catch (Exception exception) {
            connection.rollback();
            throw exception;
        } finally {
            connection.setAutoCommit(oldAutoCommit);
        }
    }

    private static void printUsage() {
        System.err.println("Comandi:");
        System.err.println("  <db> show-seasons");
        System.err.println("  <db> pending <stagione>");
        System.err.println("  <db> proposals <stagione>");
        System.err.println("  <db> validate <stagione>");
        System.err.println("  <db> auto-exact <stagione>");
        System.err.println(
            "  <db> associate-team <team-season-id> <team-identity-id>"
        );
        System.err.println("  <db> new-team <team-season-id>");
        System.err.println(
            "  <db> associate-competition "
                + "<competition-season-id> <competition-identity-id>"
        );
        System.err.println("  <db> new-competition <competition-season-id>");
    }

    @FunctionalInterface
    private interface SqlAction {
        void run() throws Exception;
    }

    private record SeasonEntity(long id, String seasonId, String name) {
    }

    private record Identity(long id, String name) {
    }

    private record ScoredIdentity(Identity identity, double score) {
    }

    private record ExactCandidate(long seasonEntityId, long identityId) {
    }

    private record Validation(
        int fcmSources,
        int fcaSources,
        int pendingCompetitions,
        int pendingTeams,
        int duplicateCompetitions,
        int duplicateTeams,
        int orphanMappings
    ) {
        boolean valid() {
            return fcmSources == 1
                && fcaSources == 1
                && pendingCompetitions == 0
                && pendingTeams == 0
                && duplicateCompetitions == 0
                && duplicateTeams == 0
                && orphanMappings == 0;
        }
    }
}
