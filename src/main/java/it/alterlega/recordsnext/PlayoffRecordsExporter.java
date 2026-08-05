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

public final class PlayoffRecordsExporter {

    private PlayoffRecordsExporter() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.err.println(
                "Uso: PlayoffRecordsExporter "
                    + "<recordsnext.db> <stagione> <output.json>"
            );
            System.exit(2);
        }

        Path database = Path.of(args[0])
            .toAbsolutePath()
            .normalize();

        String seasonId = args[1].trim();

        Path output = Path.of(args[2])
            .toAbsolutePath()
            .normalize();

        if (seasonId.isBlank()) {
            throw new IllegalArgumentException(
                "La stagione non può essere vuota."
            );
        }

        if (output.getParent() != null) {
            Files.createDirectories(output.getParent());
        }

        Class.forName("org.sqlite.JDBC");

        long started = System.nanoTime();

        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + database)) {

            ensureViewExists(connection);

            List<TeamSummary> summaries = readSummaries(
                connection,
                seasonId
            );

            List<PlayoffDetail> wins = readDetails(
                connection,
                seasonId,
                "W"
            );

            List<PlayoffDetail> losses = readDetails(
                connection,
                seasonId,
                "L"
            );

            writeJson(
                output,
                new ExportData(
                    new Meta(
                        Instant.now().toString(),
                        seasonId,
                        summaries.size(),
                        wins.size(),
                        losses.size()
                    ),
                    summaries,
                    wins,
                    losses
                )
            );

            long finished = System.nanoTime();

            System.out.println("Record play off / play out esportati");
            System.out.println("Stagione       : " + seasonId);
            System.out.println("Squadre        : " + summaries.size());
            System.out.println("Play off vinti : " + wins.size());
            System.out.println("Play off persi : " + losses.size());
            System.out.println("Output         : " + output);

            System.out.printf(
                Locale.ROOT,
                "Tempo          : %.3f ms%n",
                (finished - started) / 1_000_000.0
            );
        }
    }

    private static void ensureViewExists(
            Connection connection) throws Exception {

        String sql = """
            SELECT COUNT(*)
            FROM sqlite_master
            WHERE type = 'view'
              AND name = 'rn_playoff_result'
            """;

        try (
            PreparedStatement statement =
                connection.prepareStatement(sql);
            ResultSet result = statement.executeQuery()
        ) {
            result.next();

            if (result.getInt(1) != 1) {
                throw new IllegalStateException(
                    "Vista rn_playoff_result non trovata. "
                        + "Eseguire prima CanonicalViews."
                );
            }
        }
    }

    private static List<TeamSummary> readSummaries(
            Connection connection,
            String seasonId) throws Exception {

        String sql = """
            SELECT
                source_team_id,
                team_identity_id,
                team_name,
                SUM(CASE WHEN result = 'W' THEN 1 ELSE 0 END)
                    AS playoff_wins,
                SUM(CASE WHEN result = 'L' THEN 1 ELSE 0 END)
                    AS playoff_losses
            FROM rn_playoff_result
            WHERE season_id = ?
            GROUP BY
                source_team_id,
                team_identity_id,
                team_name
            ORDER BY
                playoff_wins DESC,
                playoff_losses ASC,
                team_name COLLATE NOCASE
            """;

        List<TeamSummary> rows = new ArrayList<>();

        try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setString(1, seasonId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    rows.add(
                        new TeamSummary(
                            result.getInt("source_team_id"),
                            result.getLong("team_identity_id"),
                            result.getString("team_name"),
                            result.getInt("playoff_wins"),
                            result.getInt("playoff_losses")
                        )
                    );
                }
            }
        }

        return rows;
    }

    private static List<PlayoffDetail> readDetails(
            Connection connection,
            String seasonId,
            String resultCode) throws Exception {

        String sql = """
            SELECT
                season_id,
                competition_name,
                source_competition_id,
                source_group_id,
                source_group_name,
                source_round_id,
                round_description,
                serie_a_round,
                source_event_id,
                source_team_id,
                team_identity_id,
                team_name,
                opponent_source_event_id,
                opponent_source_team_id,
                opponent_team_identity_id,
                opponent_name,
                score_for,
                score_against,
                result
            FROM rn_playoff_result
            WHERE season_id = ?
              AND result = ?
            ORDER BY
                serie_a_round,
                source_group_id,
                source_round_id,
                source_event_id
            """;

        List<PlayoffDetail> rows = new ArrayList<>();

        try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setString(1, seasonId);
            statement.setString(2, resultCode);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    rows.add(
                        new PlayoffDetail(
                            result.getString("season_id"),
                            result.getString("competition_name"),
                            result.getInt("source_competition_id"),
                            result.getInt("source_group_id"),
                            result.getString("source_group_name"),
                            result.getInt("source_round_id"),
                            result.getString("round_description"),
                            result.getInt("serie_a_round"),
                            result.getLong("source_event_id"),
                            result.getInt("source_team_id"),
                            result.getLong("team_identity_id"),
                            result.getString("team_name"),
                            result.getLong("opponent_source_event_id"),
                            result.getInt("opponent_source_team_id"),
                            result.getLong("opponent_team_identity_id"),
                            result.getString("opponent_name"),
                            result.getBigDecimal("score_for"),
                            result.getBigDecimal("score_against"),
                            result.getString("result")
                        )
                    );
                }
            }
        }

        return rows;
    }

    private static void writeJson(
            Path output,
            ExportData data) throws Exception {

        try (BufferedWriter writer = Files.newBufferedWriter(
                output,
                StandardCharsets.UTF_8)) {

            writer.write("{\n");

            writeMeta(writer, data.meta());
            writer.write(",\n");

            writeSummaries(writer, data.summaries());
            writer.write(",\n");

            writeDetails(
                writer,
                "playOffVinti",
                data.wins()
            );
            writer.write(",\n");

            writeDetails(
                writer,
                "playOffPersi",
                data.losses()
            );

            writer.write("\n}\n");
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
            "stagione",
            meta.seasonId(),
            true,
            4
        );
        writeNumberProperty(
            writer,
            "squadreCoinvolte",
            meta.teams(),
            true,
            4
        );
        writeNumberProperty(
            writer,
            "playOffVinti",
            meta.wins(),
            true,
            4
        );
        writeNumberProperty(
            writer,
            "playOffPersi",
            meta.losses(),
            false,
            4
        );
        writer.write("  }");
    }

    private static void writeSummaries(
            BufferedWriter writer,
            List<TeamSummary> rows) throws Exception {

        writer.write("  \"riepilogoSquadre\": [\n");

        for (int index = 0; index < rows.size(); index++) {
            TeamSummary row = rows.get(index);

            writer.write("    {\n");
            writeStringProperty(
                writer,
                "idSquadra",
                Integer.toString(row.sourceTeamId()),
                true,
                6
            );
            writeStringProperty(
                writer,
                "idIdentitaSquadra",
                Long.toString(row.teamIdentityId()),
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
            writeNumberProperty(
                writer,
                "playOffVinti",
                row.wins(),
                true,
                6
            );
            writeNumberProperty(
                writer,
                "playOffPersi",
                row.losses(),
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

    private static void writeDetails(
            BufferedWriter writer,
            String propertyName,
            List<PlayoffDetail> rows) throws Exception {

        writer.write("  \"");
        writer.write(jsonEscape(propertyName));
        writer.write("\": [\n");

        for (int index = 0; index < rows.size(); index++) {
            PlayoffDetail row = rows.get(index);

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
                "competizione",
                row.competitionName(),
                true,
                6
            );
            writeStringProperty(
                writer,
                "idCompetizioneFcm",
                Integer.toString(row.sourceCompetitionId()),
                true,
                6
            );
            writeStringProperty(
                writer,
                "idGirone",
                Integer.toString(row.sourceGroupId()),
                true,
                6
            );
            writeStringProperty(
                writer,
                "girone",
                row.sourceGroupName(),
                true,
                6
            );
            writeStringProperty(
                writer,
                "idGiornata",
                Integer.toString(row.sourceRoundId()),
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
                "idEvento",
                Long.toString(row.sourceEventId()),
                true,
                6
            );
            writeStringProperty(
                writer,
                "idSquadra",
                Integer.toString(row.sourceTeamId()),
                true,
                6
            );
            writeStringProperty(
                writer,
                "idIdentitaSquadra",
                Long.toString(row.teamIdentityId()),
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
                "idEventoAvversaria",
                Long.toString(row.opponentSourceEventId()),
                true,
                6
            );
            writeStringProperty(
                writer,
                "idAvversaria",
                Integer.toString(row.opponentSourceTeamId()),
                true,
                6
            );
            writeStringProperty(
                writer,
                "idIdentitaAvversaria",
                Long.toString(row.opponentTeamIdentityId()),
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
            writeStringProperty(
                writer,
                "esito",
                row.result(),
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
            writer.write(
                value.stripTrailingZeros().toPlainString()
            );
        }

        if (comma) {
            writer.write(",");
        }

        writer.write("\n");
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

    private record Meta(
        String generatedAt,
        String seasonId,
        int teams,
        int wins,
        int losses
    ) {
    }

    private record TeamSummary(
        int sourceTeamId,
        long teamIdentityId,
        String teamName,
        int wins,
        int losses
    ) {
    }

    private record PlayoffDetail(
        String seasonId,
        String competitionName,
        int sourceCompetitionId,
        int sourceGroupId,
        String sourceGroupName,
        int sourceRoundId,
        String roundDescription,
        int serieARound,
        long sourceEventId,
        int sourceTeamId,
        long teamIdentityId,
        String teamName,
        long opponentSourceEventId,
        int opponentSourceTeamId,
        long opponentTeamIdentityId,
        String opponentName,
        BigDecimal scoreFor,
        BigDecimal scoreAgainst,
        String result
    ) {
    }

    private record ExportData(
        Meta meta,
        List<TeamSummary> summaries,
        List<PlayoffDetail> wins,
        List<PlayoffDetail> losses
    ) {
    }
}
