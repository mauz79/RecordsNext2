package it.alterlega.recordsnext;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class DatabaseInspector {

    private DatabaseInspector() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println(
                "Uso: DatabaseInspector <file.fcm|file.fca> <output.json>"
            );
            System.exit(2);
        }

        Path database = Path.of(args[0]).toAbsolutePath().normalize();
        Path output = Path.of(args[1]).toAbsolutePath().normalize();

        if (!Files.isRegularFile(database)) {
            throw new IllegalArgumentException(
                "Database non trovato: " + database
            );
        }

        Path outputParent = output.getParent();

        if (outputParent != null) {
            Files.createDirectories(outputParent);
        }

        Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");

        long totalStarted = System.nanoTime();
        String jdbcUrl = "jdbc:ucanaccess://" + database;

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("schemaVersion", 1);
        report.put("generatedAt", Instant.now().toString());
        report.put("source", inspectSource(database));

        try (Connection connection = DriverManager.getConnection(jdbcUrl)) {
            long openedAt = System.nanoTime();

            connection.setReadOnly(true);

            DatabaseMetaData metadata = connection.getMetaData();

            Map<String, Object> driver = new LinkedHashMap<>();
            driver.put("name", metadata.getDriverName());
            driver.put("version", metadata.getDriverVersion());
            driver.put("jdbcMajorVersion", metadata.getJDBCMajorVersion());
            driver.put("jdbcMinorVersion", metadata.getJDBCMinorVersion());
            driver.put("databaseProductName", metadata.getDatabaseProductName());
            driver.put(
                "databaseProductVersion",
                metadata.getDatabaseProductVersion()
            );
            report.put("driver", driver);

            List<String> tableNames = readTableNames(metadata);
            List<Map<String, Object>> tables = new ArrayList<>();

            long totalRows = 0;
            long totalColumns = 0;

            for (String tableName : tableNames) {
                Map<String, Object> table = inspectTable(
                    connection,
                    metadata,
                    tableName
                );

                totalRows += ((Number) table.get("rowCount")).longValue();
                totalColumns += ((Number) table.get("columnCount")).longValue();

                tables.add(table);
            }

            long finishedAt = System.nanoTime();

            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("tableCount", tables.size());
            summary.put("columnCount", totalColumns);
            summary.put("rowCount", totalRows);
            report.put("summary", summary);
            report.put("tables", tables);

            Map<String, Object> timings = new LinkedHashMap<>();
            timings.put(
                "openMilliseconds",
                nanosToMilliseconds(openedAt - totalStarted)
            );
            timings.put(
                "inspectionMilliseconds",
                nanosToMilliseconds(finishedAt - openedAt)
            );
            timings.put(
                "totalMilliseconds",
                nanosToMilliseconds(finishedAt - totalStarted)
            );
            report.put("timings", timings);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(
                output,
                StandardCharsets.UTF_8)) {

            writeJson(report, writer, 0);
            writer.write(System.lineSeparator());
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> summary =
            (Map<String, Object>) report.get("summary");

        @SuppressWarnings("unchecked")
        Map<String, Object> timings =
            (Map<String, Object>) report.get("timings");

        System.out.println();
        System.out.println("Inventario completato");
        System.out.println("Database : " + database);
        System.out.println("Output   : " + output);
        System.out.println("Tabelle  : " + summary.get("tableCount"));
        System.out.println("Colonne  : " + summary.get("columnCount"));
        System.out.println("Righe    : " + summary.get("rowCount"));
        System.out.printf(
            Locale.ROOT,
            "Apertura : %.3f s%n",
            ((Number) timings.get("openMilliseconds")).doubleValue() / 1000.0
        );
        System.out.printf(
            Locale.ROOT,
            "Ispezione: %.3f s%n",
            ((Number) timings.get("inspectionMilliseconds")).doubleValue()
                / 1000.0
        );
        System.out.printf(
            Locale.ROOT,
            "Totale   : %.3f s%n",
            ((Number) timings.get("totalMilliseconds")).doubleValue() / 1000.0
        );
    }

    private static Map<String, Object> inspectSource(Path database)
            throws Exception {

        Map<String, Object> source = new LinkedHashMap<>();

        source.put("path", database.toString());
        source.put("fileName", database.getFileName().toString());
        source.put("sourceType", detectSourceType(database));
        source.put("sizeBytes", Files.size(database));

        FileTime modified = Files.getLastModifiedTime(database);
        source.put("lastModified", modified.toInstant().toString());
        source.put("sha256", sha256(database));

        return source;
    }

    private static String detectSourceType(Path database) {
        String name = database.getFileName()
            .toString()
            .toLowerCase(Locale.ROOT);

        if (name.endsWith(".fcm")) {
            return "FCM";
        }

        if (name.endsWith(".fca")) {
            return "FCA";
        }

        return "UNKNOWN";
    }

    private static List<String> readTableNames(DatabaseMetaData metadata)
            throws Exception {

        List<String> tables = new ArrayList<>();

        try (ResultSet rs = metadata.getTables(
                null,
                null,
                "%",
                new String[]{"TABLE"})) {

            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");

                if (tableName != null && !tableName.isBlank()) {
                    tables.add(tableName);
                }
            }
        }

        tables.sort(String.CASE_INSENSITIVE_ORDER);
        return tables;
    }

    private static Map<String, Object> inspectTable(
            Connection connection,
            DatabaseMetaData metadata,
            String tableName) throws Exception {

        Map<String, Object> table = new LinkedHashMap<>();

        long rowCount = countRows(connection, tableName);
        List<Map<String, Object>> columns = readColumns(metadata, tableName);
        List<Map<String, Object>> primaryKeys =
            readPrimaryKeys(metadata, tableName);
        List<Map<String, Object>> indexes = readIndexes(metadata, tableName);

        table.put("name", tableName);
        table.put("rowCount", rowCount);
        table.put("columnCount", columns.size());
        table.put("columns", columns);
        table.put("primaryKeys", primaryKeys);
        table.put("indexes", indexes);

        return table;
    }

    private static long countRows(
            Connection connection,
            String tableName) throws Exception {

        String escapedName = tableName.replace("]", "]]");
        String sql = "SELECT COUNT(*) FROM [" + escapedName + "]";

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            rs.next();
            return rs.getLong(1);
        }
    }

    private static List<Map<String, Object>> readColumns(
            DatabaseMetaData metadata,
            String tableName) throws Exception {

        List<Map<String, Object>> columns = new ArrayList<>();

        try (ResultSet rs = metadata.getColumns(
                null,
                null,
                tableName,
                "%")) {

            while (rs.next()) {
                Map<String, Object> column = new LinkedHashMap<>();

                int nullableCode = rs.getInt("NULLABLE");

                column.put("name", rs.getString("COLUMN_NAME"));
                column.put("ordinalPosition", rs.getInt("ORDINAL_POSITION"));
                column.put("jdbcType", rs.getInt("DATA_TYPE"));
                column.put("typeName", rs.getString("TYPE_NAME"));
                column.put("columnSize", rs.getInt("COLUMN_SIZE"));
                column.put("decimalDigits", nullableInteger(
                    rs,
                    "DECIMAL_DIGITS"
                ));
                column.put("numericPrecisionRadix", nullableInteger(
                    rs,
                    "NUM_PREC_RADIX"
                ));
                column.put("nullableCode", nullableCode);
                column.put(
                    "nullable",
                    nullableCode == DatabaseMetaData.columnNullable
                );
                column.put("defaultValue", rs.getString("COLUMN_DEF"));
                column.put("remarks", rs.getString("REMARKS"));
                column.put(
                    "autoIncrement",
                    safeGetString(rs, "IS_AUTOINCREMENT")
                );
                column.put(
                    "generatedColumn",
                    safeGetString(rs, "IS_GENERATEDCOLUMN")
                );

                columns.add(column);
            }
        }

        columns.sort(Comparator.comparingInt(
            item -> ((Number) item.get("ordinalPosition")).intValue()
        ));

        return columns;
    }

    private static List<Map<String, Object>> readPrimaryKeys(
            DatabaseMetaData metadata,
            String tableName) throws Exception {

        List<Map<String, Object>> primaryKeys = new ArrayList<>();

        try (ResultSet rs = metadata.getPrimaryKeys(
                null,
                null,
                tableName)) {

            while (rs.next()) {
                Map<String, Object> key = new LinkedHashMap<>();

                key.put("name", rs.getString("PK_NAME"));
                key.put("columnName", rs.getString("COLUMN_NAME"));
                key.put("keySequence", rs.getInt("KEY_SEQ"));

                primaryKeys.add(key);
            }
        }

        primaryKeys.sort(Comparator.comparingInt(
            item -> ((Number) item.get("keySequence")).intValue()
        ));

        return primaryKeys;
    }

    private static List<Map<String, Object>> readIndexes(
            DatabaseMetaData metadata,
            String tableName) throws Exception {

        List<Map<String, Object>> indexes = new ArrayList<>();

        try (ResultSet rs = metadata.getIndexInfo(
                null,
                null,
                tableName,
                false,
                false)) {

            while (rs.next()) {
                String indexName = rs.getString("INDEX_NAME");

                if (indexName == null || indexName.isBlank()) {
                    continue;
                }

                Map<String, Object> index = new LinkedHashMap<>();

                index.put("name", indexName);
                index.put("unique", !rs.getBoolean("NON_UNIQUE"));
                index.put("type", rs.getShort("TYPE"));
                index.put(
                    "ordinalPosition",
                    rs.getShort("ORDINAL_POSITION")
                );
                index.put("columnName", rs.getString("COLUMN_NAME"));
                index.put("sortDirection", rs.getString("ASC_OR_DESC"));
                index.put("filterCondition", rs.getString("FILTER_CONDITION"));

                indexes.add(index);
            }
        }

indexes.sort(
    Comparator
        .comparing(
            (Map<String, Object> item) ->
                String.valueOf(item.get("name")),
            String.CASE_INSENSITIVE_ORDER
        )
        .thenComparingInt(
            item ->
                ((Number) item.get("ordinalPosition")).intValue()
        )
);

        return indexes;
    }

    private static Integer nullableInteger(
            ResultSet rs,
            String columnName) throws Exception {

        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }

    private static String safeGetString(
            ResultSet rs,
            String columnName) {

        try {
            return rs.getString(columnName);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static double nanosToMilliseconds(long nanos) {
        return Math.round((nanos / 1_000_000.0) * 1000.0) / 1000.0;
    }

    private static String sha256(Path path) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        try (var input = Files.newInputStream(path)) {
            byte[] buffer = new byte[1024 * 1024];
            int read;

            while ((read = input.read(buffer)) >= 0) {
                digest.update(buffer, 0, read);
            }
        }

        return HexFormat.of().formatHex(digest.digest());
    }

    private static void writeJson(
            Object value,
            Writer writer,
            int indent) throws IOException {

        if (value == null) {
            writer.write("null");
            return;
        }

        if (value instanceof String text) {
            writeJsonString(text, writer);
            return;
        }

        if (value instanceof Number || value instanceof Boolean) {
            writer.write(String.valueOf(value));
            return;
        }

        if (value instanceof Map<?, ?> map) {
            writeJsonMap(map, writer, indent);
            return;
        }

        if (value instanceof Iterable<?> iterable) {
            writeJsonArray(iterable, writer, indent);
            return;
        }

        writeJsonString(String.valueOf(value), writer);
    }

    private static void writeJsonMap(
            Map<?, ?> map,
            Writer writer,
            int indent) throws IOException {

        writer.write("{");

        if (!map.isEmpty()) {
            writer.write(System.lineSeparator());

            int index = 0;

            for (Map.Entry<?, ?> entry : map.entrySet()) {
                writeIndent(writer, indent + 1);
                writeJsonString(String.valueOf(entry.getKey()), writer);
                writer.write(": ");
                writeJson(entry.getValue(), writer, indent + 1);

                if (++index < map.size()) {
                    writer.write(",");
                }

                writer.write(System.lineSeparator());
            }

            writeIndent(writer, indent);
        }

        writer.write("}");
    }

    private static void writeJsonArray(
            Iterable<?> iterable,
            Writer writer,
            int indent) throws IOException {

        List<Object> values = new ArrayList<>();

        for (Object value : iterable) {
            values.add(value);
        }

        writer.write("[");

        if (!values.isEmpty()) {
            writer.write(System.lineSeparator());

            for (int index = 0; index < values.size(); index++) {
                writeIndent(writer, indent + 1);
                writeJson(values.get(index), writer, indent + 1);

                if (index + 1 < values.size()) {
                    writer.write(",");
                }

                writer.write(System.lineSeparator());
            }

            writeIndent(writer, indent);
        }

        writer.write("]");
    }

    private static void writeJsonString(
            String text,
            Writer writer) throws IOException {

        writer.write("\"");

        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);

            switch (character) {
                case '"' -> writer.write("\\\"");
                case '\\' -> writer.write("\\\\");
                case '\b' -> writer.write("\\b");
                case '\f' -> writer.write("\\f");
                case '\n' -> writer.write("\\n");
                case '\r' -> writer.write("\\r");
                case '\t' -> writer.write("\\t");
                default -> {
                    if (character < 0x20) {
                        writer.write(
                            String.format(
                                Locale.ROOT,
                                "\\u%04x",
                                (int) character
                            )
                        );
                    } else {
                        writer.write(character);
                    }
                }
            }
        }

        writer.write("\"");
    }

    private static void writeIndent(
            Writer writer,
            int indent) throws IOException {

        writer.write("  ".repeat(indent));
    }
}