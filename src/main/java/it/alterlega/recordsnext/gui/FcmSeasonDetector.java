package it.alterlega.recordsnext.gui;

import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import java.util.regex.*;

final class FcmSeasonDetector {
    record Detection(String seasonId, int seasonNumber, String evidence) {}

    private static final Pattern RANGE = Pattern.compile("(?<!\\d)(20\\d{2})[^0-9]{0,5}(20\\d{2})(?!\\d)");
    private static final Pattern SINGLE = Pattern.compile("(?<!\\d)(20\\d{2})(?!\\d)");

    Detection detect(Path fcm) throws Exception {
        Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        try (Connection c = DriverManager.getConnection("jdbc:ucanaccess://" + fcm.toAbsolutePath())) {
            Detection fromLeague = detectFromLeague(c);
            if (fromLeague != null) {
                return fromLeague;
            }
        }
        throw new IllegalArgumentException(
            "Impossibile ricavare stagione e numero stagione dalla tabella LEGA del file FCM selezionato."
        );
    }

    private Detection detectFromLeague(Connection c) throws SQLException {
        String table = findTable(c, "LEGA");
        if (table == null) {
            return null;
        }

        Set<String> columns = columns(c, table);
        if (!containsIgnoreCase(columns, "STAGIONE")) {
            return null;
        }

        String seasonColumn = actualName(columns, "STAGIONE");
        String yearColumn = actualName(columns, "ANNOARCHIVIO");
        String nameColumn = actualName(columns, "NOME");

        StringBuilder sql = new StringBuilder("SELECT TOP 1 [")
            .append(escape(seasonColumn)).append("]");
        if (yearColumn != null) sql.append(", [").append(escape(yearColumn)).append("]");
        if (nameColumn != null) sql.append(", [").append(escape(nameColumn)).append("]");
        sql.append(" FROM [").append(escape(table)).append("]");

        try (Statement st = c.createStatement(); ResultSet r = st.executeQuery(sql.toString())) {
            if (!r.next()) {
                return null;
            }

            int seasonNumber = toPositiveInt(r.getObject(1));
            if (seasonNumber < 1) {
                throw new IllegalArgumentException("Il campo LEGA.STAGIONE non contiene un numero stagione valido.");
            }

            int index = 2;
            Integer archiveYear = null;
            if (yearColumn != null) {
                int value = toPositiveInt(r.getObject(index++));
                if (value >= 1900 && value <= 2200) archiveYear = value;
            }

            String leagueName = null;
            if (nameColumn != null) {
                Object value = r.getObject(index);
                if (value != null) leagueName = value.toString();
            }

            String seasonId = archiveYear == null ? parse(leagueName) : archiveYear + "_" + (archiveYear + 1);
            if (seasonId == null) {
                throw new IllegalArgumentException(
                    "Il file FCM contiene LEGA.STAGIONE=" + seasonNumber
                        + " ma non consente di ricavare gli anni della stagione."
                );
            }

            return new Detection(
                seasonId,
                seasonNumber,
                "LEGA.STAGIONE=" + seasonNumber
                    + (archiveYear == null ? "" : ", LEGA.ANNOARCHIVIO=" + archiveYear)
            );
        }
    }

    private static String findTable(Connection c, String expected) throws SQLException {
        DatabaseMetaData md = c.getMetaData();
        try (ResultSet tables = md.getTables(null, null, "%", new String[]{"TABLE"})) {
            while (tables.next()) {
                String name = tables.getString("TABLE_NAME");
                if (expected.equalsIgnoreCase(name)) return name;
            }
        }
        return null;
    }

    private static Set<String> columns(Connection c, String table) throws SQLException {
        Set<String> out = new LinkedHashSet<>();
        try (ResultSet cols = c.getMetaData().getColumns(null, null, table, "%")) {
            while (cols.next()) out.add(cols.getString("COLUMN_NAME"));
        }
        return out;
    }

    private static boolean containsIgnoreCase(Collection<String> values, String expected) {
        return actualName(values, expected) != null;
    }

    private static String actualName(Collection<String> values, String expected) {
        for (String value : values) {
            if (expected.equalsIgnoreCase(value)) return value;
        }
        return null;
    }

    private static int toPositiveInt(Object value) {
        if (value instanceof Number n) return n.intValue();
        if (value == null) return -1;
        try { return Integer.parseInt(value.toString().trim()); }
        catch (NumberFormatException ex) { return -1; }
    }

    private static String escape(String identifier) {
        return identifier.replace("]", "]]");
    }

    private static String parse(String value) {
        if (value == null) return null;
        Matcher range = RANGE.matcher(value);
        while (range.find()) {
            int a = Integer.parseInt(range.group(1));
            int b = Integer.parseInt(range.group(2));
            if (b == a + 1) return a + "_" + b;
        }
        Matcher single = SINGLE.matcher(value);
        if (single.find()) {
            int year = Integer.parseInt(single.group(1));
            return year + "_" + (year + 1);
        }
        return null;
    }
}
