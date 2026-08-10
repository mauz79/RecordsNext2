package it.alterlega.recordsnext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class SeasonNormalizedBatchExporter {

    private SeasonNormalizedBatchExporter() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.err.println(
                "Uso: SeasonNormalizedBatchExporter "
                    + "<recordsnext.db> <stagione> <project-dir>"
            );
            System.exit(2);
        }

        Path database = Path.of(args[0]).toAbsolutePath().normalize();
        String seasonId = args[1].trim();
        Path projectDir = Path.of(args[2]).toAbsolutePath().normalize();
        export(database, seasonId, projectDir);
    }

    public static void export(Path database, String seasonId, Path projectDir) throws Exception {
        Path outputDir = projectDir
            .resolve("data")
            .resolve("reports")
            .resolve(seasonId);

        Files.createDirectories(outputDir);

        Class.forName("org.sqlite.JDBC");

        List<String> competitions = readCompetitions(
            database,
            seasonId
        );

        if (competitions.isEmpty()) {
            System.out.println(
                "Nessuna competizione testa-a-testa elaborabile per la stagione "
                    + seasonId
                    + "; stagione saltata (le competizioni Gran Premio non vengono processate)."
            );
            return;
        }

        int completed = 0;
        List<String> failures = new ArrayList<>();

        System.out.println(
            "Competizioni da esportare: "
                + competitions.size()
        );

        for (String competition : competitions) {
            Path output = outputDir.resolve(
                "season_normalized_"
                    + slug(competition)
                    + ".json"
            );

            System.out.println();
            System.out.println(
                "=== " + competition + " ==="
            );

            try {
                SeasonNormalizedExporter.main(
                    new String[] {
                        database.toString(),
                        seasonId,
                        competition,
                        projectDir.toString(),
                        output.toString()
                    }
                );

                completed++;
            } catch (Exception error) {
                failures.add(
                    competition
                        + ": "
                        + error.getClass().getSimpleName()
                        + " - "
                        + error.getMessage()
                );

                error.printStackTrace(System.err);
            }
        }

        System.out.println();
        System.out.println("=== RIEPILOGO BATCH ===");
        System.out.println(
            "Stagione     : " + seasonId
        );
        System.out.println(
            "Competizioni : " + competitions.size()
        );
        System.out.println(
            "Completate   : " + completed
        );
        System.out.println(
            "Fallite      : " + failures.size()
        );
        System.out.println(
            "Output       : " + outputDir
        );

        if (!failures.isEmpty()) {
            System.out.println();
            System.out.println("Errori:");
            for (String failure : failures) System.out.println(" - " + failure);
            throw new IllegalStateException("Normalizzazione fallita per " + failures.size() + " competizioni: " + String.join("; ", failures));
        }
    }

    private static List<String> readCompetitions(
            Path database,
            String seasonId) throws Exception {

        String sql = """
            SELECT DISTINCT competition_name
            FROM rn_team_match
            WHERE season_id = ?
              AND competition_name IS NOT NULL
              AND TRIM(competition_name) <> ''
            ORDER BY competition_name COLLATE NOCASE
            """;

        List<String> competitions = new ArrayList<>();

        try (
            Connection connection =
                DriverManager.getConnection(
                    "jdbc:sqlite:" + database
                );

            PreparedStatement statement =
                connection.prepareStatement(sql)
        ) {
            statement.setString(1, seasonId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    competitions.add(
                        result.getString("competition_name")
                    );
                }
            }
        }

        return competitions;
    }

    private static String slug(String value) {
        String normalized = Normalizer.normalize(
            value,
            Normalizer.Form.NFD
        );

        return normalized
            .replaceAll("\\p{M}+", "")
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "_")
            .replaceAll("^_+|_+$", "");
    }
}
