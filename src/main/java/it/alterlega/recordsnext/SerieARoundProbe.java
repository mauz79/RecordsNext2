package it.alterlega.recordsnext;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public final class SerieARoundProbe {

    private SerieARoundProbe() {
    }

    public static void main(String[] args) throws Exception {
        Path database = Path.of(args[0]).toAbsolutePath().normalize();

        Class.forName("org.sqlite.JDBC");

        String sql = """
            WITH giornate AS (
                SELECT
                    i.IDGIORNATA,
                    i.GIORNATADIA,
                    gio."DESC" AS descrizione,
                    MIN(i.ID) AS primo_incontro,
                    COUNT(*) AS incontri
                FROM raw_2025_2026_fcm_incontro i
                JOIN raw_2025_2026_fcm_girone g
                    ON g.ID = i.IDGIRONE
                LEFT JOIN raw_2025_2026_fcm_giornata gio
                    ON gio.ID = i.IDGIORNATA
                WHERE g.IDCOMPETIZIONE = 4
                  AND i.GIOCATO <> 0
                  AND i.IDCASA <> 0
                  AND i.IDFUORI <> 0
                GROUP BY
                    i.IDGIORNATA,
                    i.GIORNATADIA,
                    gio."DESC"
            )
            SELECT
                ROW_NUMBER() OVER (
                    ORDER BY primo_incontro
                ) AS giornata_competizione,
                IDGIORNATA,
                GIORNATADIA AS giornata_serie_a,
                descrizione,
                incontri,
                primo_incontro
            FROM giornate
            ORDER BY primo_incontro
            """;

        try (Connection connection = DriverManager.getConnection(
                    "jdbc:sqlite:" + database);
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {

            int giornate = 0;
            int incontri = 0;

            while (result.next()) {
                giornate++;
                incontri += result.getInt("incontri");

                System.out.printf(
                    "%2d | IDGIORNATA=%4d | Serie A=%2d | incontri=%d | %s%n",
                    result.getInt("giornata_competizione"),
                    result.getInt("IDGIORNATA"),
                    result.getInt("giornata_serie_a"),
                    result.getInt("incontri"),
                    result.getString("descrizione")
                );
            }

            System.out.println();
            System.out.println("Giornate : " + giornate);
            System.out.println("Incontri : " + incontri);
        }
    }
}