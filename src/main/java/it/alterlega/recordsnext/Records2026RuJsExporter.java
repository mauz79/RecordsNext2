package it.alterlega.recordsnext;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/** Genera gli output RU pubblici compatibili con Records2026. */
public final class Records2026RuJsExporter {
    private Records2026RuJsExporter() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Uso: Records2026RuJsExporter <archive-riserveufficio> <output-js-dir>");
            System.exit(2);
        }
        ExportResult result = export(Path.of(args[0]), Path.of(args[1]));
        System.out.println("Archivio : " + Path.of(args[0]).toAbsolutePath().normalize());
        System.out.println("Output   : " + Path.of(args[1]).toAbsolutePath().normalize());
        System.out.println("Stagioni : " + result.seasons());
        System.out.println("Annuali  : " + result.annualFiles());
    }

    public static ExportResult export(Path archiveRoot, Path outputDir) throws IOException {
        archiveRoot = archiveRoot.toAbsolutePath().normalize();
        outputDir = outputDir.toAbsolutePath().normalize();
        if (!Files.isDirectory(archiveRoot)) throw new IOException("Archivio RU non trovato: " + archiveRoot);
        Files.createDirectories(outputDir);

        List<Path> seasonDirs;
        try (Stream<Path> s = Files.list(archiveRoot)) {
            seasonDirs = s.filter(Files::isDirectory)
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .toList();
        }

        List<Object> compactItems = new ArrayList<>();
        List<Object> manifestItems = new ArrayList<>();
        int annualFiles = 0;

        for (Path seasonDir : seasonDirs) {
            String season = seasonDir.getFileName().toString();
            Path jsonPath = seasonDir.resolve("riserveufficio.json");
            if (!Files.isRegularFile(jsonPath)) continue;

            String rawSource = readUtf8WithoutBom(jsonPath);
            String source = normalizeJsonText(rawSource);
            Object parsed = new JsonParser(source, jsonPath).parse();
            Map<String,Object> root = asObject(parsed, jsonPath, "radice");

            String annualJson = escapeScriptTerminator(stripTrailingLineBreaks(rawSource));
            String annual = "window.RECORDS2026_STORICO_RU = window.RECORDS2026_STORICO_RU || {};\r\n"
                    + "window.RECORDS2026_STORICO_RU['" + JsonWriter.escape(season) + "'] = " + annualJson + ";\r\n";
            Path annualPath = outputDir.resolve("records2026.storico.ru." + season.replaceAll("[^\\w]+", "_") + ".js");
            writeUtf8Bom(annualPath, annual);
            annualFiles++;

            Map<String,Object> data = new LinkedHashMap<>();
            data.put("views", compactArrayMap(root.get("views")));
            data.put("dettaglio", compactArrayMap(root.get("dettaglio")));
            data.put("curiosita", root.get("curiosita"));
            Map<String,Object> compact = new LinkedHashMap<>();
            compact.put("stagione", season);
            compact.put("data", data);
            compactItems.add(compact);

            Map<String,Object> detail = objectOrEmpty(root.get("dettaglio"));
            int ruRows = listSize(detail.get("ruDettaglio"));
            Map<String,Object> meta = objectOrEmpty(root.get("meta"));
            Map<String,Object> manifestItem = new LinkedHashMap<>();
            manifestItem.put("stagione", season);
            manifestItem.put("jsFile", annualPath.getFileName().toString());
            manifestItem.put("ruDettaglio", ruRows);
            manifestItem.put("generated", stringValue(meta.get("generato")));
            manifestItems.add(manifestItem);
        }

        String compactJs = "window.RECORDS2026_PREVIEW_RU = "
                + escapeScriptTerminator(JsonWriter.write(compactItems)) + ";";
        writeUtf8(outputDir.resolve("records2026.recordstagionali.ru.js"), compactJs);

        Map<String,Object> manifestMeta = new LinkedHashMap<>();
        manifestMeta.put("titolo", "Records2026 Storico Riserve d'Ufficio");
        manifestMeta.put("generato", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        manifestMeta.put("modello", "manifest + js annuale");
        Map<String,Object> manifest = new LinkedHashMap<>();
        manifest.put("meta", manifestMeta);
        manifest.put("stagioni", manifestItems);
        String manifestJs = "window.RECORDS2026_STORICO_RU_MANIFEST = "
                + JsonWriter.writePretty(manifest) + ";\r\n";
        writeUtf8Bom(outputDir.resolve("records2026.storico.ru.manifest.js"), manifestJs);

        return new ExportResult(compactItems.size(), annualFiles);
    }

    private static Map<String,Object> compactArrayMap(Object value) {
        Map<String,Object> out = new LinkedHashMap<>();
        for (Map.Entry<String,Object> e : objectOrEmpty(value).entrySet()) {
            if (e.getValue() instanceof List<?> rows) {
                List<Object> compactRows = new ArrayList<>(rows.size());
                for (Object row : rows) compactRows.add(compactRow(row));
                out.put(e.getKey(), compactRows);
            } else if (e.getValue() == null) {
                out.put(e.getKey(), List.of());
            }
        }
        return out;
    }

    private static Map<String,Object> compactRow(Object value) {
        Map<String,Object> out = new LinkedHashMap<>();
        if (!(value instanceof Map<?,?> raw)) return out;
        for (Map.Entry<?,?> e : raw.entrySet()) {
            String name = String.valueOf(e.getKey());
            Object v = e.getValue();
            if (name.equals("dettagli")) {
                if (v instanceof List<?> rows) out.put("dettagliCount", rows.size());
                continue;
            }
            if (name.matches("^(dettaglio|dettagliPartite|partiteDettaglio|rows|raw|sourceRows)$")) continue;
            if (v == null || v instanceof String || v instanceof Number || v instanceof Boolean) out.put(name, v);
        }
        return out;
    }

    private static Map<String,Object> asObject(Object value, Path source, String label) throws IOException {
        if (!(value instanceof Map<?,?> raw)) throw new IOException("Oggetto JSON '" + label + "' non valido: " + source);
        Map<String,Object> out = new LinkedHashMap<>();
        for (Map.Entry<?,?> e : raw.entrySet()) out.put(String.valueOf(e.getKey()), e.getValue());
        return out;
    }
    private static Map<String,Object> objectOrEmpty(Object value) {
        Map<String,Object> out = new LinkedHashMap<>();
        if (value instanceof Map<?,?> raw) for (Map.Entry<?,?> e : raw.entrySet()) out.put(String.valueOf(e.getKey()), e.getValue());
        return out;
    }
    private static int listSize(Object value) { return value instanceof List<?> l ? l.size() : 0; }
    private static String stringValue(Object value) { return value == null ? "" : String.valueOf(value); }
    private static String readUtf8WithoutBom(Path path) throws IOException {
        String text = Files.readString(path, StandardCharsets.UTF_8);
        return (!text.isEmpty() && text.charAt(0) == '\uFEFF') ? text.substring(1) : text;
    }
    private static String stripTrailingLineBreaks(String text) {
        int end = text.length();
        while (end > 0 && (text.charAt(end - 1) == '\r' || text.charAt(end - 1) == '\n')) end--;
        return text.substring(0, end);
    }
    private static String normalizeJsonText(String text) {
        if (text == null || text.isEmpty()) return "";
        if (text.charAt(0) == '\uFEFF') text = text.substring(1);
        return text.trim();
    }
    private static String escapeScriptTerminator(String json) { return json.replace("</script>", "<\\/script>"); }
    private static void writeUtf8(Path path, String text) throws IOException {
        Files.writeString(path, text, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
    private static void writeUtf8Bom(Path path, String text) throws IOException {
        byte[] body = text.getBytes(StandardCharsets.UTF_8);
        byte[] out = new byte[body.length + 3];
        out[0] = (byte)0xEF; out[1] = (byte)0xBB; out[2] = (byte)0xBF;
        System.arraycopy(body, 0, out, 3, body.length);
        Files.write(path, out, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
    public record ExportResult(int seasons, int annualFiles) {}

    private static final class JsonParser {
        private final String text;
        private final Path source;
        private int index;

        JsonParser(String text, Path source) {
            this.text = text;
            this.source = source;
        }

        Object parse() throws IOException {
            skipWhitespace();
            Object value = parseValue();
            skipWhitespace();
            if (index != text.length()) {
                fail("Contenuto dopo la fine del JSON");
            }
            return value;
        }

        private Object parseValue() throws IOException {
            skipWhitespace();
            if (index >= text.length()) fail("Valore mancante");
            return switch (text.charAt(index)) {
                case '{' -> parseObject();
                case '[' -> parseArray();
                case '"' -> parseString();
                case 't' -> parseLiteral("true", Boolean.TRUE);
                case 'f' -> parseLiteral("false", Boolean.FALSE);
                case 'n' -> parseLiteral("null", null);
                default -> parseNumber();
            };
        }

        private Map<String, Object> parseObject() throws IOException {
            expect('{');
            Map<String, Object> result = new LinkedHashMap<>();
            skipWhitespace();
            if (peek('}')) { index++; return result; }
            while (true) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                expect(':');
                result.put(key, parseValue());
                skipWhitespace();
                if (peek('}')) { index++; return result; }
                expect(',');
            }
        }

        private List<Object> parseArray() throws IOException {
            expect('[');
            List<Object> result = new ArrayList<>();
            skipWhitespace();
            if (peek(']')) { index++; return result; }
            while (true) {
                result.add(parseValue());
                skipWhitespace();
                if (peek(']')) { index++; return result; }
                expect(',');
            }
        }

        private String parseString() throws IOException {
            expect('"');
            StringBuilder result = new StringBuilder();
            while (index < text.length()) {
                char ch = text.charAt(index++);
                if (ch == '"') return result.toString();
                if (ch != '\\') { result.append(ch); continue; }
                if (index >= text.length()) fail("Escape incompleto");
                char esc = text.charAt(index++);
                switch (esc) {
                    case '"', '\\', '/' -> result.append(esc);
                    case 'b' -> result.append('\b');
                    case 'f' -> result.append('\f');
                    case 'n' -> result.append('\n');
                    case 'r' -> result.append('\r');
                    case 't' -> result.append('\t');
                    case 'u' -> result.append(parseUnicode());
                    default -> fail("Escape non valido: \\" + esc);
                }
            }
            fail("Stringa non terminata");
            return null;
        }

        private char parseUnicode() throws IOException {
            if (index + 4 > text.length()) fail("Escape unicode incompleto");
            String hex = text.substring(index, index + 4);
            index += 4;
            try { return (char) Integer.parseInt(hex, 16); }
            catch (NumberFormatException ex) { fail("Escape unicode non valido: " + hex); return 0; }
        }

        private Object parseLiteral(String literal, Object value) throws IOException {
            if (!text.startsWith(literal, index)) fail("Token non valido");
            index += literal.length();
            return value;
        }

        private BigDecimal parseNumber() throws IOException {
            int start = index;
            if (peek('-')) index++;
            while (index < text.length() && Character.isDigit(text.charAt(index))) index++;
            if (peek('.')) {
                index++;
                while (index < text.length() && Character.isDigit(text.charAt(index))) index++;
            }
            if (peek('e') || peek('E')) {
                index++;
                if (peek('+') || peek('-')) index++;
                while (index < text.length() && Character.isDigit(text.charAt(index))) index++;
            }
            if (start == index) fail("Numero non valido");
            try { return new BigDecimal(text.substring(start, index)); }
            catch (NumberFormatException ex) { fail("Numero non valido"); return null; }
        }

        private void expect(char expected) throws IOException {
            skipWhitespace();
            if (index >= text.length() || text.charAt(index) != expected) {
                fail("Atteso '" + expected + "'");
            }
            index++;
        }

        private boolean peek(char value) {
            return index < text.length() && text.charAt(index) == value;
        }

        private void skipWhitespace() {
            while (index < text.length() && Character.isWhitespace(text.charAt(index))) index++;
        }

        private void fail(String message) throws IOException {
            throw new IOException(message + " in " + source + " alla posizione " + index);
        }
    }

    private static final class JsonWriter {
        static String write(Object value) {
            StringBuilder out = new StringBuilder();
            append(out, value);
            return out.toString();
        }

        static String writePretty(Object value) {
            StringBuilder out = new StringBuilder();
            appendPretty(out, value, 0);
            return out.toString();
        }

        private static void appendPretty(StringBuilder out, Object value, int depth) {
            if (value == null || value instanceof String || value instanceof Boolean || value instanceof Number) { append(out, value); return; }
            String indent = "    ".repeat(depth);
            String childIndent = "    ".repeat(depth + 1);
            if (value instanceof Map<?, ?> map) {
                if (map.isEmpty()) { out.append("{}"); return; }
                out.append("{\r\n");
                boolean first = true;
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    if (!first) out.append(",\r\n");
                    first = false;
                    out.append(childIndent).append('"').append(escape(String.valueOf(entry.getKey()))).append("\": ");
                    appendPretty(out, entry.getValue(), depth + 1);
                }
                out.append("\r\n").append(indent).append("}");
                return;
            }
            if (value instanceof List<?> list) {
                if (list.isEmpty()) { out.append("[]"); return; }
                out.append("[\r\n");
                for (int i = 0; i < list.size(); i++) {
                    if (i > 0) out.append(",\r\n");
                    out.append(childIndent);
                    appendPretty(out, list.get(i), depth + 1);
                }
                out.append("\r\n").append(indent).append("]");
                return;
            }
            append(out, value);
        }

        private static void append(StringBuilder out, Object value) {
            if (value == null) { out.append("null"); return; }
            if (value instanceof String string) { out.append('"').append(escape(string)).append('"'); return; }
            if (value instanceof Boolean || value instanceof BigDecimal) { out.append(value); return; }
            if (value instanceof Number number) { out.append(number); return; }
            if (value instanceof Map<?, ?> map) {
                out.append('{');
                boolean first = true;
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    if (!first) out.append(',');
                    first = false;
                    out.append('"').append(escape(String.valueOf(entry.getKey()))).append("\":");
                    append(out, entry.getValue());
                }
                out.append('}');
                return;
            }
            if (value instanceof List<?> list) {
                out.append('[');
                for (int i = 0; i < list.size(); i++) {
                    if (i > 0) out.append(',');
                    append(out, list.get(i));
                }
                out.append(']');
                return;
            }
            throw new IllegalArgumentException("Tipo JSON non supportato: " + value.getClass());
        }

        static String escape(String value) {
            StringBuilder escaped = new StringBuilder(value.length() + 16);
            for (int i = 0; i < value.length(); i++) {
                char ch = value.charAt(i);
                switch (ch) {
                    case '\\' -> escaped.append("\\\\");
                    case '"' -> escaped.append("\\\"");
                    case '\b' -> escaped.append("\\b");
                    case '\f' -> escaped.append("\\f");
                    case '\n' -> escaped.append("\\n");
                    case '\r' -> escaped.append("\\r");
                    case '\t' -> escaped.append("\\t");
                    default -> {
                        if (ch < 0x20) escaped.append(String.format("\\u%04x", (int) ch));
                        else escaped.append(ch);
                    }
                }
            }
            return escaped.toString();
        }
    }
}
