package it.alterlega.recordsnext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

public final class RawSqliteImporter {

    private static final int BATCH_SIZE = 1000;

    private RawSqliteImporter() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            System.err.println(
                "Uso: RawSqliteImporter "
                    + "<file.fcm|file.fca> <FCM|FCA> <stagione> <output.db>"
            );
            System.exit(2);
        }

        Path source = Path.of(args[0]).toAbsolutePath().normalize();
        String sourceType = args[1].trim().toUpperCase(Locale.ROOT);
        String seasonId = args[2].trim();
        Path sqliteFile = Path.of(args[3]).toAbsolutePath().normalize();

        if (!Files.isRegularFile(source)) {
            throw new IllegalArgumentException(
                "File sorgente non trovato: " + source
            );
        }

        if (!sourceType.equals("FCM") && !sourceType.equals("FCA")) {
            throw new IllegalArgumentException(
                "Tipo sorgente non valido: " + sourceType
            );
        }

        if (seasonId.isBlank()) {
            throw new IllegalArgumentException("Stagione non specificata.");
        }

        if (sqliteFile.getParent() != null) {
            Files.createDirectories(sqliteFile.getParent());
        }

        Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        Class.forName("org.sqlite.JDBC");

        String accessUrl = "jdbc:ucanaccess://" + source;
        String sqliteUrl = "jdbc:sqlite:" + sqliteFile;

        long totalStarted = System.nanoTime();

        try (
            Connection access = DriverManager.getConnection(accessUrl);
            Connection sqlite = DriverManager.getConnection(sqliteUrl)
        ) {
access.setReadOnly(true);

configureSqlite(sqlite);
sqlite.setAutoCommit(false);

createMetadataTables(sqlite);

            long importId = registerImport(
                sqlite,
                source,
                sourceType,
                seasonId
            );

            DatabaseMetaData metadata = access.getMetaData();
            List<String> tableNames = readTableNames(metadata);

            long importedRows = 0;
            long importedColumns = 0;

            for (String tableName : tableNames) {
                TableImportResult result = importTable(
                    access,
                    sqlite,
                    metadata,
                    importId,
                    sourceType,
                    seasonId,
                    tableName
                );

                importedRows += result.rows();
                importedColumns += result.columns();

                System.out.printf(
                    Locale.ROOT,
                    "%-40s colonne=%4d righe=%8d%n",
                    tableName,
                    result.columns(),
                    result.rows()
                );
            }

            finishImport(
                sqlite,
                importId,
                tableNames.size(),
                importedColumns,
                importedRows
            );

            sqlite.commit();

            long totalFinished = System.nanoTime();

            System.out.println();
            System.out.println("Importazione raw completata");
            System.out.println("Sorgente : " + source);
            System.out.println("Tipo     : " + sourceType);
            System.out.println("Stagione : " + seasonId);
            System.out.println("SQLite   : " + sqliteFile);
            System.out.println("Tabelle  : " + tableNames.size());
            System.out.println("Colonne  : " + importedColumns);
            System.out.println("Righe    : " + importedRows);
            System.out.printf(
                Locale.ROOT,
                "Totale   : %.3f s%n",
                (totalFinished - totalStarted) / 1_000_000_000.0
            );
        }
    }

    private static void configureSqlite(Connection sqlite) throws Exception {
        try (Statement statement = sqlite.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("PRAGMA journal_mode = WAL");
            statement.execute("PRAGMA synchronous = NORMAL");
            statement.execute("PRAGMA temp_store = MEMORY");
        }
    }

    private static void createMetadataTables(Connection sqlite)
            throws Exception {

        try (Statement statement = sqlite.createStatement()) {
            statement.execute("""
                CREATE TABLE IF NOT EXISTS rn_import (
                    import_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    season_id TEXT NOT NULL,
                    source_type TEXT NOT NULL,
                    source_path TEXT NOT NULL,
                    source_file_name TEXT NOT NULL,
                    source_size_bytes INTEGER NOT NULL,
                    source_last_modified TEXT NOT NULL,
                    source_sha256 TEXT NOT NULL,
                    started_at TEXT NOT NULL,
                    completed_at TEXT,
                    table_count INTEGER,
                    column_count INTEGER,
                    row_count INTEGER,
                    status TEXT NOT NULL
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS rn_table_catalog (
                    import_id INTEGER NOT NULL,
                    season_id TEXT NOT NULL,
                    source_type TEXT NOT NULL,
                    source_table_name TEXT NOT NULL,
                    raw_table_name TEXT NOT NULL,
                    source_row_count INTEGER NOT NULL,
                    imported_row_count INTEGER NOT NULL,
                    column_count INTEGER NOT NULL,
                    audit_ok INTEGER NOT NULL,
                    PRIMARY KEY (
                        import_id,
                        source_table_name
                    )
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS rn_column_catalog (
                    import_id INTEGER NOT NULL,
                    source_table_name TEXT NOT NULL,
                    column_name TEXT NOT NULL,
                    ordinal_position INTEGER NOT NULL,
                    jdbc_type INTEGER NOT NULL,
                    type_name TEXT,
                    column_size INTEGER,
                    decimal_digits INTEGER,
                    nullable_code INTEGER,
                    default_value TEXT,
                    PRIMARY KEY (
                        import_id,
                        source_table_name,
                        column_name
                    )
                )
                """);
        }
    }

    private static long registerImport(
            Connection sqlite,
            Path source,
            String sourceType,
            String seasonId) throws Exception {

        String sql = """
            INSERT INTO rn_import (
                season_id,
                source_type,
                source_path,
                source_file_name,
                source_size_bytes,
                source_last_modified,
                source_sha256,
                started_at,
                status
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (
            PreparedStatement statement = sqlite.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS
            )
        ) {
            statement.setString(1, seasonId);
            statement.setString(2, sourceType);
            statement.setString(3, source.toString());
            statement.setString(4, source.getFileName().toString());
            statement.setLong(5, Files.size(source));
            statement.setString(
                6,
                Files.getLastModifiedTime(source).toInstant().toString()
            );
            statement.setString(7, sha256(source));
            statement.setString(8, Instant.now().toString());
            statement.setString(9, "RUNNING");
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new IllegalStateException(
                        "Impossibile ottenere import_id."
                    );
                }

                return keys.getLong(1);
            }
        }
    }

    private static List<String> readTableNames(DatabaseMetaData metadata)
            throws Exception {

        List<String> tables = new ArrayList<>();

        try (
            ResultSet result = metadata.getTables(
                null,
                null,
                "%",
                new String[]{"TABLE"}
            )
        ) {
            while (result.next()) {
                String name = result.getString("TABLE_NAME");

                if (name != null && !name.isBlank()) {
                    tables.add(name);
                }
            }
        }

        tables.sort(String.CASE_INSENSITIVE_ORDER);
        return tables;
    }

    private static TableImportResult importTable(
            Connection access,
            Connection sqlite,
            DatabaseMetaData metadata,
            long importId,
            String sourceType,
            String seasonId,
            String sourceTableName) throws Exception {

        String rawTableName = rawTableName(
            sourceType,
            seasonId,
            sourceTableName
        );

        List<ColumnDefinition> columns = readColumns(
            metadata,
            sourceTableName
        );

        dropRawTable(sqlite, rawTableName);
        createRawTable(sqlite, rawTableName, columns);
        registerColumns(
            sqlite,
            importId,
            sourceTableName,
            columns
        );

        long sourceRowCount = countSourceRows(
            access,
            sourceTableName
        );

        long importedRowCount = copyRows(
            access,
            sqlite,
            sourceTableName,
            rawTableName,
            columns
        );

        registerTable(
            sqlite,
            importId,
            seasonId,
            sourceType,
            sourceTableName,
            rawTableName,
            sourceRowCount,
            importedRowCount,
            columns.size()
        );

        if (sourceRowCount != importedRowCount) {
            throw new IllegalStateException(
                "Audit fallito per " + sourceTableName
                    + ": sorgente=" + sourceRowCount
                    + ", importate=" + importedRowCount
            );
        }

        return new TableImportResult(
            columns.size(),
            importedRowCount
        );
    }

    private static List<ColumnDefinition> readColumns(
            DatabaseMetaData metadata,
            String tableName) throws Exception {

        List<ColumnDefinition> columns = new ArrayList<>();

        try (
            ResultSet result = metadata.getColumns(
                null,
                null,
                tableName,
                "%"
            )
        ) {
            while (result.next()) {
                columns.add(
                    new ColumnDefinition(
                        result.getString("COLUMN_NAME"),
                        result.getInt("ORDINAL_POSITION"),
                        result.getInt("DATA_TYPE"),
                        result.getString("TYPE_NAME"),
                        result.getInt("COLUMN_SIZE"),
                        nullableInteger(result, "DECIMAL_DIGITS"),
                        result.getInt("NULLABLE"),
                        result.getString("COLUMN_DEF")
                    )
                );
            }
        }

        columns.sort(
            (left, right) ->
                Integer.compare(
                    left.ordinalPosition(),
                    right.ordinalPosition()
                )
        );

        return columns;
    }

    private static void dropRawTable(
            Connection sqlite,
            String rawTableName) throws Exception {

        try (Statement statement = sqlite.createStatement()) {
            statement.execute(
                "DROP TABLE IF EXISTS " + quoteSqlite(rawTableName)
            );
        }
    }

    private static void createRawTable(
            Connection sqlite,
            String rawTableName,
            List<ColumnDefinition> columns) throws Exception {

        StringBuilder sql = new StringBuilder();

        sql.append("CREATE TABLE ");
        sql.append(quoteSqlite(rawTableName));
        sql.append(" (");

        for (int index = 0; index < columns.size(); index++) {
            if (index > 0) {
                sql.append(", ");
            }

            ColumnDefinition column = columns.get(index);

            sql.append(quoteSqlite(column.name()));
            sql.append(" ");
            sql.append(sqliteType(column.jdbcType()));
        }

        sql.append(")");

        try (Statement statement = sqlite.createStatement()) {
            statement.execute(sql.toString());
        }
    }

    private static long copyRows(
            Connection access,
            Connection sqlite,
            String sourceTableName,
            String rawTableName,
            List<ColumnDefinition> columns) throws Exception {

        String sourceSql =
            "SELECT * FROM " + quoteAccess(sourceTableName);

        StringBuilder insertSql = new StringBuilder();

        insertSql.append("INSERT INTO ");
        insertSql.append(quoteSqlite(rawTableName));
        insertSql.append(" (");

        for (int index = 0; index < columns.size(); index++) {
            if (index > 0) {
                insertSql.append(", ");
            }

            insertSql.append(
                quoteSqlite(columns.get(index).name())
            );
        }

        insertSql.append(") VALUES (");

        for (int index = 0; index < columns.size(); index++) {
            if (index > 0) {
                insertSql.append(", ");
            }

            insertSql.append("?");
        }

        insertSql.append(")");

        long importedRows = 0;
        int batchRows = 0;

        try (
            Statement sourceStatement = access.createStatement();
            ResultSet sourceRows =
                sourceStatement.executeQuery(sourceSql);
            PreparedStatement destination =
                sqlite.prepareStatement(insertSql.toString())
        ) {
            ResultSetMetaData rowMetadata =
                sourceRows.getMetaData();

            while (sourceRows.next()) {
                for (
                    int columnIndex = 1;
                    columnIndex <= columns.size();
                    columnIndex++
                ) {
                    setValue(
                        destination,
                        columnIndex,
                        sourceRows,
                        rowMetadata,
                        columnIndex
                    );
                }

                destination.addBatch();
                importedRows++;
                batchRows++;

                if (batchRows >= BATCH_SIZE) {
                    destination.executeBatch();
                    batchRows = 0;
                }
            }

            if (batchRows > 0) {
                destination.executeBatch();
            }
        }

        return importedRows;
    }

    private static void setValue(
            PreparedStatement destination,
            int destinationIndex,
            ResultSet source,
            ResultSetMetaData metadata,
            int sourceIndex) throws Exception {

        int jdbcType = metadata.getColumnType(sourceIndex);
        Object value = source.getObject(sourceIndex);

        if (value == null) {
            destination.setNull(
                destinationIndex,
                sqliteNullType(jdbcType)
            );
            return;
        }

        switch (jdbcType) {
            case Types.BINARY,
                 Types.VARBINARY,
                 Types.LONGVARBINARY,
                 Types.BLOB ->
                destination.setBytes(
                    destinationIndex,
                    source.getBytes(sourceIndex)
                );

            case Types.TINYINT,
                 Types.SMALLINT,
                 Types.INTEGER,
                 Types.BIGINT ->
                destination.setLong(
                    destinationIndex,
                    source.getLong(sourceIndex)
                );

            case Types.FLOAT,
                 Types.REAL,
                 Types.DOUBLE ->
                destination.setDouble(
                    destinationIndex,
                    source.getDouble(sourceIndex)
                );

            case Types.NUMERIC,
                 Types.DECIMAL ->
                destination.setBigDecimal(
                    destinationIndex,
                    source.getBigDecimal(sourceIndex)
                );

            case Types.BIT,
                 Types.BOOLEAN ->
                destination.setInt(
                    destinationIndex,
                    source.getBoolean(sourceIndex) ? 1 : 0
                );

            case Types.DATE,
                 Types.TIME,
                 Types.TIMESTAMP,
                 Types.TIMESTAMP_WITH_TIMEZONE ->
                destination.setString(
                    destinationIndex,
                    String.valueOf(value)
                );

            default ->
                destination.setString(
                    destinationIndex,
                    source.getString(sourceIndex)
                );
        }
    }

    private static long countSourceRows(
            Connection access,
            String tableName) throws Exception {

        String sql =
            "SELECT COUNT(*) FROM " + quoteAccess(tableName);

        try (
            Statement statement = access.createStatement();
            ResultSet result = statement.executeQuery(sql)
        ) {
            result.next();
            return result.getLong(1);
        }
    }

    private static void registerColumns(
            Connection sqlite,
            long importId,
            String tableName,
            List<ColumnDefinition> columns) throws Exception {

        String sql = """
            INSERT INTO rn_column_catalog (
                import_id,
                source_table_name,
                column_name,
                ordinal_position,
                jdbc_type,
                type_name,
                column_size,
                decimal_digits,
                nullable_code,
                default_value
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement =
                 sqlite.prepareStatement(sql)) {

            for (ColumnDefinition column : columns) {
                statement.setLong(1, importId);
                statement.setString(2, tableName);
                statement.setString(3, column.name());
                statement.setInt(4, column.ordinalPosition());
                statement.setInt(5, column.jdbcType());
                statement.setString(6, column.typeName());
                statement.setInt(7, column.columnSize());

                if (column.decimalDigits() == null) {
                    statement.setNull(8, Types.INTEGER);
                } else {
                    statement.setInt(
                        8,
                        column.decimalDigits()
                    );
                }

                statement.setInt(9, column.nullableCode());
                statement.setString(10, column.defaultValue());
                statement.addBatch();
            }

            statement.executeBatch();
        }
    }

    private static void registerTable(
            Connection sqlite,
            long importId,
            String seasonId,
            String sourceType,
            String sourceTableName,
            String rawTableName,
            long sourceRowCount,
            long importedRowCount,
            int columnCount) throws Exception {

        String sql = """
            INSERT INTO rn_table_catalog (
                import_id,
                season_id,
                source_type,
                source_table_name,
                raw_table_name,
                source_row_count,
                imported_row_count,
                column_count,
                audit_ok
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement =
                 sqlite.prepareStatement(sql)) {

            statement.setLong(1, importId);
            statement.setString(2, seasonId);
            statement.setString(3, sourceType);
            statement.setString(4, sourceTableName);
            statement.setString(5, rawTableName);
            statement.setLong(6, sourceRowCount);
            statement.setLong(7, importedRowCount);
            statement.setInt(8, columnCount);
            statement.setInt(
                9,
                sourceRowCount == importedRowCount ? 1 : 0
            );
            statement.executeUpdate();
        }
    }

    private static void finishImport(
            Connection sqlite,
            long importId,
            int tableCount,
            long columnCount,
            long rowCount) throws Exception {

        String sql = """
            UPDATE rn_import
            SET completed_at = ?,
                table_count = ?,
                column_count = ?,
                row_count = ?,
                status = ?
            WHERE import_id = ?
            """;

        try (PreparedStatement statement =
                 sqlite.prepareStatement(sql)) {

            statement.setString(1, Instant.now().toString());
            statement.setInt(2, tableCount);
            statement.setLong(3, columnCount);
            statement.setLong(4, rowCount);
            statement.setString(5, "COMPLETED");
            statement.setLong(6, importId);
            statement.executeUpdate();
        }
    }

    private static String rawTableName(
            String sourceType,
            String seasonId,
            String sourceTableName) {

        return "raw_"
            + normalizeIdentifier(seasonId)
            + "_"
            + sourceType.toLowerCase(Locale.ROOT)
            + "_"
            + normalizeIdentifier(sourceTableName);
    }

    private static String normalizeIdentifier(String value) {
        String normalized = value
            .trim()
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "_")
            .replaceAll("^_+|_+$", "");

        if (normalized.isBlank()) {
            throw new IllegalArgumentException(
                "Identificatore non normalizzabile: " + value
            );
        }

        return normalized;
    }

    private static String quoteAccess(String name) {
        return "[" + name.replace("]", "]]") + "]";
    }

    private static String quoteSqlite(String name) {
        return "\"" + name.replace("\"", "\"\"") + "\"";
    }

    private static String sqliteType(int jdbcType) {
        return switch (jdbcType) {
            case Types.BIT,
                 Types.BOOLEAN,
                 Types.TINYINT,
                 Types.SMALLINT,
                 Types.INTEGER,
                 Types.BIGINT -> "INTEGER";

            case Types.FLOAT,
                 Types.REAL,
                 Types.DOUBLE -> "REAL";

            case Types.NUMERIC,
                 Types.DECIMAL -> "NUMERIC";

            case Types.BINARY,
                 Types.VARBINARY,
                 Types.LONGVARBINARY,
                 Types.BLOB -> "BLOB";

            default -> "TEXT";
        };
    }

    private static int sqliteNullType(int jdbcType) {
        return switch (sqliteType(jdbcType)) {
            case "INTEGER" -> Types.INTEGER;
            case "REAL" -> Types.REAL;
            case "NUMERIC" -> Types.NUMERIC;
            case "BLOB" -> Types.BLOB;
            default -> Types.VARCHAR;
        };
    }

    private static Integer nullableInteger(
            ResultSet result,
            String columnName) throws Exception {

        int value = result.getInt(columnName);
        return result.wasNull() ? null : value;
    }

    private static String sha256(Path path) throws Exception {
        MessageDigest digest =
            MessageDigest.getInstance("SHA-256");

        try (var input = Files.newInputStream(path)) {
            byte[] buffer = new byte[1024 * 1024];
            int read;

            while ((read = input.read(buffer)) >= 0) {
                digest.update(buffer, 0, read);
            }
        }

        return HexFormat.of().formatHex(digest.digest());
    }

    private record ColumnDefinition(
        String name,
        int ordinalPosition,
        int jdbcType,
        String typeName,
        int columnSize,
        Integer decimalDigits,
        int nullableCode,
        String defaultValue
    ) {
    }

    private record TableImportResult(
        int columns,
        long rows
    ) {
    }
}