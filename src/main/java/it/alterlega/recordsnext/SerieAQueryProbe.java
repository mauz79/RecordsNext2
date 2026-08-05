package it.alterlega.recordsnext;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Locale;

public final class SerieAQueryProbe {

    private SerieAQueryProbe() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Uso: SerieAQueryProbe <recordsnext.db>");
            System.exit(2);
        }

        Path database = Path.of(args[0]).toAbsolutePath().normalize();

        Class.forName("org.sqlite.JDBC");

        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + database)) {

            printCompetition(connection);
            printGironi(connection);
            printCounts(connection);
            printMatches(connection);
        }
    }

    private static void printCompetition(Connection connection)
            throws Exception {

        String sql = """
            SELECT ID, NOME
            FROM raw_2025_2026_fcm_competizione
            WHERE ID = 4
            """;

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {

            if (!result.next()) {
                throw new IllegalStateException(
                    "Competizione Serie A con ID 4 non trovata."
                );
            }

            System.out.println("=== COMPETIZIONE ===");
            System.out.println("ID   : " + result.getInt("ID"));
            System.out.println("Nome : " + result.getString("NOME"));
        }
    }

    private static void printGironi(Connection connection)
            throws Exception {

        String sql = """
            SELECT
                g.ID,
                g.NOME,
                COUNT(i.ID) AS incontri,
                SUM(CASE WHEN i.GIOCATO <> 0 THEN 1 ELSE 0 END) AS giocati,
                SUM(
                    CASE
                        WHEN i.GIOCATO <> 0
                         AND i.IDCASA <> 0
                         AND i.IDFUORI <> 0
                        THEN 1
                        ELSE 0
                    END
                ) AS validi
            FROM raw_2025_2026_fcm_girone g
            LEFT JOIN raw_2025_2026_fcm_incontro i
                ON i.IDGIRONE = g.ID
            WHERE g.IDCOMPETIZIONE = 4
            GROUP BY g.ID, g.NOME
            ORDER BY g.ID
            """;

        System.out.println();
        System.out.println("=== GIRONI SERIE A ===");

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {

            while (result.next()) {
                System.out.printf(
                    Locale.ROOT,
                    "ID=%d nome=%s incontri=%d giocati=%d validi=%d%n",
                    result.getInt("ID"),
                    result.getString("NOME"),
                    result.getLong("incontri"),
                    result.getLong("giocati"),
                    result.getLong("validi")
                );
            }
        }
    }

    private static void printCounts(Connection connection)
            throws Exception {

        String sql = """
            SELECT
                COUNT(*) AS tutti,
                SUM(CASE WHEN i.GIOCATO <> 0 THEN 1 ELSE 0 END) AS giocati,
                SUM(
                    CASE
                        WHEN i.GIOCATO <> 0
                         AND i.IDCASA <> 0
                         AND i.IDFUORI <> 0
                        THEN 1
                        ELSE 0
                    END
                ) AS validi
            FROM raw_2025_2026_fcm_incontro i
            JOIN raw_2025_2026_fcm_girone g
                ON g.ID = i.IDGIRONE
            WHERE g.IDCOMPETIZIONE = 4
            """;

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {

            result.next();

            System.out.println();
            System.out.println("=== CONTEGGI ===");
            System.out.println("Tutti   : " + result.getLong("tutti"));
            System.out.println("Giocati : " + result.getLong("giocati"));
            System.out.println("Validi  : " + result.getLong("validi"));
        }
    }

    private static void printMatches(Connection connection)
            throws Exception {

        String sql = """
            SELECT
                i.ID AS id_incontro,
                g.ID AS id_girone,
                g.NOME AS girone,
                i.GIORNATADIA AS giornata_di_a,
                i.IDGIORNATA AS id_giornata,
                gio.DESC AS descrizione_giornata,
                i.IDCASA AS id_casa,
                casa.NOME AS squadra_casa,
                i.IDFUORI AS id_fuori,
                fuori.NOME AS squadra_fuori,
                i.GOLCASA AS gol_casa,
                i.GOLFUORI AS gol_fuori,
                i.TOTCASA AS punti_casa,
                i.TOTFUORI AS punti_fuori
            FROM raw_2025_2026_fcm_incontro i
            JOIN raw_2025_2026_fcm_girone g
                ON g.ID = i.IDGIRONE
            LEFT JOIN raw_2025_2026_fcm_giornata gio
                ON gio.ID = i.IDGIORNATA
            JOIN raw_2025_2026_fcm_fantasquadra casa
                ON casa.ID = i.IDCASA
            JOIN raw_2025_2026_fcm_fantasquadra fuori
                ON fuori.ID = i.IDFUORI
            WHERE g.IDCOMPETIZIONE = 4
              AND i.GIOCATO <> 0
              AND i.IDCASA <> 0
              AND i.IDFUORI <> 0
            ORDER BY
                i.GIORNATADIA,
                i.ID
            LIMIT 15
            """;

        long started = System.nanoTime();

        System.out.println();
        System.out.println("=== PRIME 15 PARTITE ===");

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {

            while (result.next()) {
                System.out.printf(
                    Locale.ROOT,
                    "%d | %2d | %-25s - %-25s | %d-%d | %.1f-%.1f | %s%n",
                    result.getLong("id_incontro"),
                    result.getInt("giornata_di_a"),
                    result.getString("squadra_casa"),
                    result.getString("squadra_fuori"),
                    result.getInt("gol_casa"),
                    result.getInt("gol_fuori"),
                    result.getDouble("punti_casa"),
                    result.getDouble("punti_fuori"),
                    result.getString("descrizione_giornata")
                );
            }
        }

        long finished = System.nanoTime();

        System.out.printf(
            Locale.ROOT,
            "%nTempo query e lettura campione: %.3f ms%n",
            (finished - started) / 1_000_000.0
        );
    }
}