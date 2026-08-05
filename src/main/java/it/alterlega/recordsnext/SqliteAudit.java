package it.alterlega.recordsnext;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public final class SqliteAudit {

    private SqliteAudit() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Uso: SqliteAudit <recordsnext.db>");
            System.exit(2);
        }

        Path database = Path.of(args[0]).toAbsolutePath().normalize();

        Class.forName("org.sqlite.JDBC");

        try (Connection connection =
                 DriverManager.getConnection("jdbc:sqlite:" + database);
             Statement statement = connection.createStatement()) {

            printValue(
                statement,
                "Importazioni completate",
                "SELECT COUNT(*) FROM rn_import WHERE status='COMPLETED'"
            );

            printValue(
                statement,
                "Tabelle catalogate",
                "SELECT COUNT(*) FROM rn_table_catalog"
            );

            printValue(
                statement,
                "Colonne catalogate",
                "SELECT COUNT(*) FROM rn_column_catalog"
            );

            printValue(
                statement,
                "Righe sorgente",
                "SELECT SUM(source_row_count) FROM rn_table_catalog"
            );

            printValue(
                statement,
                "Righe importate",
                "SELECT SUM(imported_row_count) FROM rn_table_catalog"
            );

            printValue(
                statement,
                "Audit falliti",
                "SELECT COUNT(*) FROM rn_table_catalog WHERE audit_ok<>1"
            );

            printValue(
                statement,
                "Tabelle raw reali",
                """
                SELECT COUNT(*)
                FROM sqlite_master
                WHERE type='table'
                  AND name LIKE 'raw_%'
                """
            );

            System.out.println();
            System.out.println("=== IMPORTAZIONI ===");

            try (ResultSet result = statement.executeQuery(
                    """
                    SELECT source_type,
                           table_count,
                           column_count,
                           row_count,
                           status
                    FROM rn_import
                    ORDER BY import_id
                    """)) {

                while (result.next()) {
                    System.out.printf(
                        "%s tabelle=%d colonne=%d righe=%d stato=%s%n",
                        result.getString("source_type"),
                        result.getInt("table_count"),
                        result.getInt("column_count"),
                        result.getLong("row_count"),
                        result.getString("status")
                    );
                }
            }

            System.out.println();
            System.out.println("Audit SQLite completato.");
        }
    }

    private static void printValue(
            Statement statement,
            String label,
            String sql) throws Exception {

        try (ResultSet result = statement.executeQuery(sql)) {
            result.next();
            System.out.printf("%-24s: %d%n", label, result.getLong(1));
        }
    }
}