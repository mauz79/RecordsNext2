package it.alterlega.recordsnext;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.List;

public final class CanonicalSchemaProbe {

    private static final List<String> TABLES = List.of(
        "raw_2025_2026_fcm_competizione",
        "raw_2025_2026_fcm_girone",
        "raw_2025_2026_fcm_giornata",
        "raw_2025_2026_fcm_fantasquadra",
        "raw_2025_2026_fcm_incontro",
        "raw_2025_2026_fcm_formazione",
        "raw_2025_2026_fcm_tabellino",
        "raw_2025_2026_fca_giocatorea",
        "raw_2025_2026_fca_giocain",
        "raw_2025_2026_fca_punteggio"
    );

    private CanonicalSchemaProbe() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Uso: CanonicalSchemaProbe <recordsnext.db>");
            System.exit(2);
        }

        Path database = Path.of(args[0]).toAbsolutePath().normalize();

        Class.forName("org.sqlite.JDBC");

        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + database)) {

            for (String table : TABLES) {
                printTable(connection, table);
            }
        }
    }

    private static void printTable(
            Connection connection,
            String table) throws Exception {

        System.out.println();
        System.out.println("==================================================");
        System.out.println(table);
        System.out.println("==================================================");

        try (Statement statement = connection.createStatement();
             ResultSet columns = statement.executeQuery(
                 "PRAGMA table_info(\"" + table.replace("\"", "\"\"") + "\")"
             )) {

            System.out.println("COLONNE:");

            while (columns.next()) {
                System.out.printf(
                    "%3d  %-35s %s%n",
                    columns.getInt("cid"),
                    columns.getString("name"),
                    columns.getString("type")
                );
            }
        }

        String sql = "SELECT * FROM \""
            + table.replace("\"", "\"\"")
            + "\" LIMIT 1";

        try (Statement statement = connection.createStatement();
             ResultSet row = statement.executeQuery(sql)) {

            if (!row.next()) {
                System.out.println("TABELLA VUOTA");
                return;
            }

            ResultSetMetaData metadata = row.getMetaData();

            System.out.println();
            System.out.println("PRIMA RIGA:");

            for (int index = 1; index <= metadata.getColumnCount(); index++) {
                Object value = row.getObject(index);

                System.out.printf(
                    "%-35s = %s%n",
                    metadata.getColumnName(index),
                    value == null ? "<NULL>" : String.valueOf(value)
                );
            }
        }
    }
}