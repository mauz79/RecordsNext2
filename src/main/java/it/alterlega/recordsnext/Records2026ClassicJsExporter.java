package it.alterlega.recordsnext;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Genera records2026.recordstagionali.classic.js mantenendo il contratto
 * pubblico di Records2026 e pubblicando soltanto le sezioni/campi previsti.
 */
public final class Records2026ClassicJsExporter {

    private static final String PREFIX = "season_records_";
    private static final String SUFFIX = ".json";

    private static final Map<String, Set<String>> PUBLIC_FIELDS = buildPublicFields();

    private Records2026ClassicJsExporter() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            printUsage();
            System.exit(2);
        }

        Path archiveRoot = Path.of(args[0]).toAbsolutePath().normalize();
        Path outputFile = Path.of(args[1]).toAbsolutePath().normalize();
        List<String> requestedSeasons = new ArrayList<>();
        for (int i = 2; i < args.length; i++) {
            String value = args[i].trim();
            if (!value.isEmpty()) {
                requestedSeasons.add(value);
            }
        }

        ExportResult result = export(archiveRoot, outputFile, requestedSeasons);
        System.out.println("Archivio : " + archiveRoot);
        System.out.println("Output   : " + outputFile);
        System.out.println("Stagioni : " + result.seasonCount());
        System.out.println("Recordset: " + result.entryCount());
    }

    public static ExportResult export(Path archiveRoot, Path outputFile, List<String> requestedSeasons)
            throws IOException {
        if (!Files.isDirectory(archiveRoot)) {
            throw new IOException("Archivio stagioni non trovato: " + archiveRoot);
        }

        List<Path> seasonDirectories = resolveSeasonDirectories(archiveRoot, requestedSeasons);
        if (seasonDirectories.isEmpty()) {
            throw new IOException("Nessuna stagione trovata in: " + archiveRoot);
        }

        List<Entry> entries = new ArrayList<>();
        int seasonsWithRecords = 0;

        for (Path seasonDirectory : seasonDirectories) {
            List<Path> recordFiles = listRecordFiles(seasonDirectory);
            if (recordFiles.isEmpty()) {
                continue;
            }
            seasonsWithRecords++;
            String season = seasonDirectory.getFileName().toString();

            for (Path recordFile : recordFiles) {
                String fileName = recordFile.getFileName().toString();
                String competitionId = fileName.substring(PREFIX.length(), fileName.length() - SUFFIX.length());
                String sourceText = normalizeJsonText(Files.readString(recordFile, StandardCharsets.UTF_8));
                Object parsed = new JsonParser(sourceText, recordFile).parse();
                Map<String, Object> root = requireObject(parsed, recordFile, "radice");
                Map<String, Object> sourceRecords = requireObject(root.get("records"), recordFile, "records");
                Map<String, Object> publicRecords = projectRecords(sourceRecords, recordFile);

                Map<String, Object> publicData = new LinkedHashMap<>();
                publicData.put("records", publicRecords);
                String json = escapeScriptTerminator(JsonWriter.write(publicData));
                entries.add(new Entry(season, competitionId, fileName, json));
            }
        }

        if (entries.isEmpty()) {
            throw new IOException("Nessun file season_records_*.json trovato in: " + archiveRoot);
        }

        Path parent = outputFile.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(outputFile, buildJavascript(entries), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        return new ExportResult(seasonsWithRecords, entries.size());
    }

    private static Map<String, Object> projectRecords(Map<String, Object> sourceRecords, Path source)
            throws IOException {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> sectionEntry : sourceRecords.entrySet()) {
            String section = sectionEntry.getKey();
            Set<String> allowedFields = PUBLIC_FIELDS.get(section);
            if (allowedFields == null) {
                continue;
            }

            Object value = sectionEntry.getValue();
            if (!(value instanceof List<?> sourceRows)) {
                continue;
            }
            if (sourceRows.isEmpty()) {
                continue;
            }

            List<Object> publicRows = new ArrayList<>(sourceRows.size());
            for (Object row : sourceRows) {
                if (!(row instanceof Map<?, ?> rawMap)) {
                    throw new IOException("Riga non oggetto nella sezione '" + section + "': " + source);
                }
                Map<String, Object> projected = new LinkedHashMap<>();
                for (Map.Entry<?, ?> fieldEntry : rawMap.entrySet()) {
                    String fieldName = String.valueOf(fieldEntry.getKey());
                    if (allowedFields.contains(fieldName)) {
                        projected.put(fieldName, fieldEntry.getValue());
                    }
                }

                // Nei JSON sorgente il dettaglio completo e' spesso conservato
                // nell'array "dettagli". Il file pubblico espone soltanto il
                // relativo conteggio, calcolato dal generatore legacy.
                if (allowedFields.contains("dettagliCount") && !projected.containsKey("dettagliCount")) {
                    Object details = rawMap.get("dettagli");
                    if (details instanceof List<?> detailRows) {
                        projected.put("dettagliCount", detailRows.size());
                    }
                }
                publicRows.add(projected);
            }
            result.put(section, publicRows);
        }
        return result;
    }

    private static Map<String, Set<String>> buildPublicFields() {
        Map<String, Set<String>> fields = new LinkedHashMap<>();
        fields.put("puntiSquadraMax", orderedSet(
                "recordId", "nome", "stagione", "competizioneStoricaId", "competizioneNome", "valore",
                "squadra", "avversaria", "idIncontro", "giornata", "giornataDiA", "urlTabellino",
                "risultato", "punteggio"));
        fields.put("espulsioniSquadre", compactTeamFields());
        fields.put("espulsioniGiocatori", orderedSet(
                "recordId", "nome", "valore", "idGiocatore", "giocatore", "dettagliCount"));
        fields.put("ammonizioniSquadre", compactTeamFields());
        fields.put("assistSquadre", compactTeamFields());
        fields.put("autogolSquadre", compactTeamFields());
        fields.put("rigoriSbagliatiSquadre", compactTeamFields());
        fields.put("rigoriParatiSquadre", compactTeamFields());
        fields.put("golRigoreSquadre", compactTeamFields());
        fields.put("cleanSheetPortiereVolteSquadre", compactTeamFields());
        fields.put("cleanSheetPortiereTotaleSquadre", compactTeamFields());
        return fields;
    }

    private static Set<String> compactTeamFields() {
        return orderedSet("recordId", "nome", "valore", "idSquadra", "squadra", "dettagliCount");
    }

    private static Set<String> orderedSet(String... values) {
        Set<String> result = new LinkedHashSet<>();
        for (String value : values) {
            result.add(value);
        }
        return Set.copyOf(result);
    }

    private static Map<String, Object> requireObject(Object value, Path source, String label) throws IOException {
        if (!(value instanceof Map<?, ?> raw)) {
            throw new IOException("Oggetto JSON '" + label + "' mancante o non valido: " + source);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : raw.entrySet()) {
            result.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return result;
    }

    private static List<Path> resolveSeasonDirectories(Path archiveRoot, List<String> requestedSeasons)
            throws IOException {
        List<Path> result = new ArrayList<>();
        if (requestedSeasons == null || requestedSeasons.isEmpty()) {
            try (Stream<Path> stream = Files.list(archiveRoot)) {
                stream.filter(Files::isDirectory)
                        .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                        .forEach(result::add);
            }
            return result;
        }
        requestedSeasons.stream().distinct().sorted().map(archiveRoot::resolve)
                .filter(Files::isDirectory).forEach(result::add);
        return result;
    }

    private static List<Path> listRecordFiles(Path seasonDirectory) throws IOException {
        try (Stream<Path> stream = Files.list(seasonDirectory)) {
            return stream.filter(Files::isRegularFile)
                    .filter(path -> {
                        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
                        return name.startsWith(PREFIX) && name.endsWith(SUFFIX);
                    })
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .toList();
        }
    }

    private static String normalizeJsonText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        String normalized = text;
        if (normalized.charAt(0) == '\uFEFF') {
            normalized = normalized.substring(1);
        }
        return normalized.trim();
    }

    private static String buildJavascript(List<Entry> entries) {
        StringBuilder output = new StringBuilder();
        output.append("window.RECORDS2026_PREVIEW_CLASSIC = [");
        for (int i = 0; i < entries.size(); i++) {
            if (i > 0) {
                output.append(',');
            }
            Entry entry = entries.get(i);
            output.append("{\"stagione\":\"").append(JsonWriter.escape(entry.season()))
                    .append("\",\"id\":\"").append(JsonWriter.escape(entry.competitionId()))
                    .append("\",\"file\":\"").append(JsonWriter.escape(entry.fileName()))
                    .append("\",\"data\":").append(entry.json()).append('}');
        }
        output.append("];\n");
        return output.toString();
    }

    private static String escapeScriptTerminator(String json) {
        return json.replace("</script>", "<\\/script>");
    }

    private static void printUsage() {
        System.err.println("Uso:");
        System.err.println("  Records2026ClassicJsExporter <archiveRoot> <outputFile> [stagione ...]");
    }

    private record Entry(String season, String competitionId, String fileName, String json) {
    }

    public record ExportResult(int seasonCount, int entryCount) {
    }

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
