package it.alterlega.recordsnext.app.modifiers;

import it.alterlega.recordsnext.app.config.ProcessingConfigWriter;

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
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/** Genera l'output familiare RecordsNext 2.0 dedicato ai modificatori. */
public final class ModifiersFamilyJsExporter {
    public static final String FILE_NAME = "fcmRecordsNext_Modifiers.js";
    public static final String GLOBAL_NAME = "window.fcmRecordsNextModifiers";

    private static final String RECORD_PREFIX = "season_records_";
    private static final String RECORD_SUFFIX = ".json";

    private static final Map<String, String> SECTION_BY_SELECTION = Map.ofEntries(
            Map.entry("modifiers.modm1pers.max", "modDifesaMax"),
            Map.entry("modifiers.modm1pers.total", "modDifesaTotaleSquadre"),
            Map.entry("modifiers.modm1pers.average", "modDifesaMediaSquadre"),
            Map.entry("modifiers.modm1pers.uses", "modDifesaUtilizziSquadre"),
            Map.entry("modifiers.modm2pers.max", "capitanoMax"),
            Map.entry("modifiers.modm2pers.total", "capitanoTotaleSquadre"),
            Map.entry("modifiers.modm2pers.average", "capitanoMediaSquadre"),
            Map.entry("modifiers.modm2pers.uses", "capitanoUtilizziSquadre"),
            Map.entry("modifiers.modm3pers.max", "modPersonalizzato3Max"),
            Map.entry("modifiers.modm3pers.total", "modPersonalizzato3TotaleSquadre"),
            Map.entry("modifiers.modm3pers.average", "modPersonalizzato3MediaSquadre"),
            Map.entry("modifiers.modm3pers.uses", "modPersonalizzato3UtilizziSquadre"),
            Map.entry("modifiers.modportiere.max", "modPortiereFcmMax"),
            Map.entry("modifiers.modportiere.total", "modPortiereFcmTotaleSquadre"),
            Map.entry("modifiers.modportiere.average", "modPortiereFcmMediaSquadre"),
            Map.entry("modifiers.modportiere.uses", "modPortiereFcmUtilizziSquadre"),
            Map.entry("modifiers.moddifesa.max", "modDifesaFcmMax"),
            Map.entry("modifiers.moddifesa.total", "modDifesaFcmTotaleSquadre"),
            Map.entry("modifiers.moddifesa.average", "modDifesaFcmMediaSquadre"),
            Map.entry("modifiers.moddifesa.uses", "modDifesaFcmUtilizziSquadre"),
            Map.entry("modifiers.modcentrocampo.max", "modCentrocampoFcmMax"),
            Map.entry("modifiers.modcentrocampo.total", "modCentrocampoFcmTotaleSquadre"),
            Map.entry("modifiers.modcentrocampo.average", "modCentrocampoFcmMediaSquadre"),
            Map.entry("modifiers.modcentrocampo.uses", "modCentrocampoFcmUtilizziSquadre"),
            Map.entry("modifiers.modattacco.max", "modAttaccoFcmMax"),
            Map.entry("modifiers.modattacco.total", "modAttaccoFcmTotaleSquadre"),
            Map.entry("modifiers.modattacco.average", "modAttaccoFcmMediaSquadre"),
            Map.entry("modifiers.modattacco.uses", "modAttaccoFcmUtilizziSquadre"),
            Map.entry("modifiers.modmodulo.max", "modModuloFcmMax"),
            Map.entry("modifiers.modmodulo.total", "modModuloFcmTotaleSquadre"),
            Map.entry("modifiers.modmodulo.average", "modModuloFcmMediaSquadre"),
            Map.entry("modifiers.modmodulo.uses", "modModuloFcmUtilizziSquadre"),
            Map.entry("modifiers.home-field-deciding", "fattoreCampoDecisivo"),
            Map.entry("modifiers.home-field-points-gained", "fattoreCampoPuntiGuadagnatiSquadre"),
            Map.entry("modifiers.home-field-points-lost", "fattoreCampoPuntiPersiSquadre"),
            Map.entry("modifiers.home-field-balance", "fattoreCampoTotaleSquadre")
    );

    private ModifiersFamilyJsExporter() {
    }

    public static ExportResult export(Path archiveRoot, Path outputFile) throws IOException {
        Path processingFile = Path.of("").toAbsolutePath().normalize().resolve("config/processing.json");
        ProcessingConfigWriter.State config = Files.isRegularFile(processingFile)
                ? ProcessingConfigWriter.load(processingFile)
                : new ProcessingConfigWriter.State(true, true, true, true, true, false, false);
        return export(archiveRoot, outputFile, config.modifierNames(), config.children());
    }

    public static ExportResult export(Path archiveRoot, Path outputFile,
                                      Map<String, String> configuredNames) throws IOException {
        return export(archiveRoot, outputFile, configuredNames, Map.of());
    }

    public static ExportResult export(Path archiveRoot, Path outputFile,
                                      Map<String, String> configuredNames,
                                      Map<String, Boolean> selections) throws IOException {
        if (!Files.isDirectory(archiveRoot)) {
            throw new IOException("Archivio stagioni non trovato: " + archiveRoot);
        }

        Path parent = outputFile.toAbsolutePath().normalize().getParent();
        if (parent == null) {
            throw new IOException("Directory output Modificatori non determinabile: " + outputFile);
        }
        Files.createDirectories(parent);

        List<Object> filteredEntries = new ArrayList<>();
        Set<String> generatedSections = new LinkedHashSet<>();
        Set<String> seasonsWithSelectedRecords = new LinkedHashSet<>();
        int sectionCount = 0;

        for (Path seasonDirectory : listSeasonDirectories(archiveRoot)) {
            String season = seasonDirectory.getFileName().toString();
            for (Path recordFile : listRecordFiles(seasonDirectory)) {
                Object parsed = new JsonParser(
                        Files.readString(recordFile, StandardCharsets.UTF_8),
                        recordFile
                ).parse();
                if (!(parsed instanceof Map<?, ?> rawRoot)) {
                    throw new IOException("Radice JSON non valida: " + recordFile);
                }
                Map<String, Object> root = stringMap(rawRoot);
                Object recordsValue = root.get("records");
                if (!(recordsValue instanceof Map<?, ?> rawRecords)) {
                    continue;
                }
                Map<String, Object> records = stringMap(rawRecords);

                Map<String, Object> selected = new LinkedHashMap<>();
                for (Map.Entry<String, String> sectionEntry : SECTION_BY_SELECTION.entrySet()) {
                    if (!selections.isEmpty()
                            && !selections.getOrDefault(sectionEntry.getKey(), true)) {
                        continue;
                    }
                    String section = sectionEntry.getValue();
                    Object rows = records.get(section);
                    if (rows instanceof List<?> list && !list.isEmpty()) {
                        selected.put(section, list);
                        generatedSections.add(section);
                        sectionCount++;
                    }
                }

                if (selected.isEmpty()) {
                    continue;
                }

                String fileName = recordFile.getFileName().toString();
                String competitionId = fileName.substring(
                        RECORD_PREFIX.length(),
                        fileName.length() - RECORD_SUFFIX.length()
                );

                Map<String, Object> filteredData = new LinkedHashMap<>();
                filteredData.put("records", selected);

                Map<String, Object> filteredEntry = new LinkedHashMap<>();
                filteredEntry.put("stagione", season);
                filteredEntry.put("id", competitionId);
                filteredEntry.put("competizioneId", competitionId);
                filteredEntry.put("competizioneNome", competitionName(selected, competitionId));
                filteredEntry.put("file", fileName);
                filteredEntry.put("data", filteredData);
                filteredEntries.add(filteredEntry);
                seasonsWithSelectedRecords.add(season);
            }
        }

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("schemaVersion", "2.0");
        root.put("familyId", "modifiers");

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("source", "RecordsNext 1.0.2 records archive");
        metadata.put("seasonCount", seasonsWithSelectedRecords.size());
        metadata.put("entryCount", filteredEntries.size());
        metadata.put("sectionCount", sectionCount);
        metadata.put("availableSections", new ArrayList<>(SECTION_BY_SELECTION.values()));
        metadata.put("generatedSections", new ArrayList<>(generatedSections));
        metadata.put("modifierCatalog", modifierCatalog(configuredNames));
        root.put("metadata", metadata);
        root.put("events", List.of());
        root.put("seasonAggregates", filteredEntries);
        root.put("globalAggregates", List.of());
        root.put("absoluteOccurrences", List.of());
        root.put("outputStatus", List.of(Map.of(
                "status", "GENERATED_COMPLETE",
                "detail", "Modificatori personalizzati, standard FCM e Fattore Campo disponibili"
        )));

        Files.writeString(
                outputFile,
                GLOBAL_NAME + " = " + JsonWriter.write(root) + ";\n",
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        );

        return new ExportResult(
                seasonsWithSelectedRecords.size(),
                filteredEntries.size(),
                sectionCount,
                outputFile
        );
    }

    private static List<Path> listSeasonDirectories(Path archiveRoot) throws IOException {
        try (Stream<Path> stream = Files.list(archiveRoot)) {
            return stream
                    .filter(Files::isDirectory)
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .toList();
        }
    }

    private static List<Path> listRecordFiles(Path seasonDirectory) throws IOException {
        try (Stream<Path> stream = Files.list(seasonDirectory)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        String name = path.getFileName().toString();
                        return name.startsWith(RECORD_PREFIX) && name.endsWith(RECORD_SUFFIX);
                    })
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .toList();
        }
    }

    private static List<Object> modifierCatalog(Map<String, String> configuredNames) {
        Map<String, String> names = new LinkedHashMap<>(ProcessingConfigWriter.defaultModifierNames());
        if (configuredNames != null) {
            names.putAll(configuredNames);
        }
        return List.of(
                catalogItem("MODM1PERS", "CUSTOM", "Modificatore Difesa", names.get("MODM1PERS")),
                catalogItem("MODM2PERS", "CUSTOM", "Capitano", names.get("MODM2PERS")),
                catalogItem("MODM3PERS", "CUSTOM", "Modificatore personalizzato 3", names.get("MODM3PERS")),
                catalogItem("MODPORTIERE", "FCM_STANDARD", "Modificatore Portiere FCM", "Modificatore Portiere FCM"),
                catalogItem("MODDIFESA", "FCM_STANDARD", "Modificatore Difesa FCM", "Modificatore Difesa FCM"),
                catalogItem("MODCENTROCAMPO", "FCM_STANDARD", "Modificatore Centrocampo FCM", "Modificatore Centrocampo FCM"),
                catalogItem("MODATTACCO", "FCM_STANDARD", "Modificatore Attacco FCM", "Modificatore Attacco FCM"),
                catalogItem("MODMODULO", "FCM_STANDARD", "Modificatore Modulo FCM", "Modificatore Modulo FCM")
        );
    }

    private static Map<String, Object> catalogItem(String sourceField, String category,
                                                   String defaultName, String configuredName) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("sourceField", sourceField);
        item.put("category", category);
        item.put("defaultName", defaultName);
        item.put("configuredName", configuredName == null ? "" : configuredName);
        return item;
    }

    private static String competitionName(Map<String, Object> selected, String fallback) {
        for (Object sectionValue : selected.values()) {
            if (!(sectionValue instanceof List<?> rows)) continue;
            for (Object rowValue : rows) {
                if (!(rowValue instanceof Map<?, ?> row)) continue;
                Object value = row.get("competizioneNome");
                if (value != null && !String.valueOf(value).isBlank()) {
                    return String.valueOf(value);
                }
            }
        }
        return displayCompetitionName(fallback);
    }

    private static String displayCompetitionName(String competitionId) {
        if (competitionId == null || competitionId.isBlank()) return "";
        return switch (competitionId) {
            case "serie_a" -> "Serie A";
            case "serie_b" -> "Serie B";
            case "serie_c" -> "Serie C";
            case "coppa_tra_le_coppe" -> "Coppa tra le Coppe";
            case "europa_pipps" -> "Europa Pipps";
            case "coppa_lega_serie_a" -> "Coppa di Lega Serie A";
            case "coppa_lega_serie_b" -> "Coppa di Lega Serie B";
            case "coppa_lega_serie_c" -> "Coppa di Lega Serie C";
            case "supercoppa_serie_a" -> "Supercoppa Serie A";
            case "supercoppa_serie_b" -> "Supercoppa Serie B";
            case "supercoppa_serie_c" -> "Supercoppa Serie C";
            default -> {
                String[] parts = competitionId.split("_");
                StringBuilder result = new StringBuilder();
                for (String part : parts) {
                    if (part.isBlank()) continue;
                    if (!result.isEmpty()) result.append(' ');
                    result.append(Character.toUpperCase(part.charAt(0)));
                    if (part.length() > 1) result.append(part.substring(1));
                }
                yield result.toString();
            }
        };
    }

    private static Map<String, Object> stringMap(Map<?, ?> raw) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : raw.entrySet()) {
            result.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return result;
    }

    public record ExportResult(int seasonCount, int entryCount, int sectionCount, Path outputFile) {
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
            if (index >= text.length()) {
                fail("Valore mancante");
            }
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
            if (peek('}')) {
                index++;
                return result;
            }
            while (true) {
                String key = parseString();
                expect(':');
                result.put(key, parseValue());
                skipWhitespace();
                if (peek('}')) {
                    index++;
                    return result;
                }
                expect(',');
            }
        }

        private List<Object> parseArray() throws IOException {
            expect('[');
            List<Object> result = new ArrayList<>();
            skipWhitespace();
            if (peek(']')) {
                index++;
                return result;
            }
            while (true) {
                result.add(parseValue());
                skipWhitespace();
                if (peek(']')) {
                    index++;
                    return result;
                }
                expect(',');
            }
        }

        private String parseString() throws IOException {
            expect('"');
            StringBuilder result = new StringBuilder();
            while (index < text.length()) {
                char ch = text.charAt(index++);
                if (ch == '"') {
                    return result.toString();
                }
                if (ch != '\\') {
                    result.append(ch);
                    continue;
                }
                if (index >= text.length()) {
                    fail("Escape incompleto");
                }
                char escaped = text.charAt(index++);
                switch (escaped) {
                    case '"', '\\', '/' -> result.append(escaped);
                    case 'b' -> result.append('\b');
                    case 'f' -> result.append('\f');
                    case 'n' -> result.append('\n');
                    case 'r' -> result.append('\r');
                    case 't' -> result.append('\t');
                    case 'u' -> result.append(parseUnicode());
                    default -> fail("Escape non valido");
                }
            }
            fail("Stringa non terminata");
            return null;
        }

        private char parseUnicode() throws IOException {
            if (index + 4 > text.length()) {
                fail("Unicode incompleto");
            }
            String hexadecimal = text.substring(index, index + 4);
            index += 4;
            try {
                return (char) Integer.parseInt(hexadecimal, 16);
            } catch (NumberFormatException ex) {
                fail("Unicode non valido");
                return 0;
            }
        }

        private Object parseLiteral(String literal, Object value) throws IOException {
            if (!text.startsWith(literal, index)) {
                fail("Token non valido");
            }
            index += literal.length();
            return value;
        }

        private BigDecimal parseNumber() throws IOException {
            int start = index;
            if (peek('-')) {
                index++;
            }
            while (index < text.length() && Character.isDigit(text.charAt(index))) {
                index++;
            }
            if (peek('.')) {
                index++;
                while (index < text.length() && Character.isDigit(text.charAt(index))) {
                    index++;
                }
            }
            if (peek('e') || peek('E')) {
                index++;
                if (peek('+') || peek('-')) {
                    index++;
                }
                while (index < text.length() && Character.isDigit(text.charAt(index))) {
                    index++;
                }
            }
            if (start == index) {
                fail("Numero non valido");
            }
            try {
                return new BigDecimal(text.substring(start, index));
            } catch (NumberFormatException ex) {
                fail("Numero non valido");
                return null;
            }
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
            while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
                index++;
            }
        }

        private void fail(String message) throws IOException {
            throw new IOException(message + " in " + source + " alla posizione " + index);
        }
    }

    private static final class JsonWriter {
        static String write(Object value) {
            StringBuilder output = new StringBuilder();
            append(output, value);
            return output.toString();
        }

        private static void append(StringBuilder output, Object value) {
            if (value == null) {
                output.append("null");
                return;
            }
            if (value instanceof String text) {
                output.append('"').append(escape(text)).append('"');
                return;
            }
            if (value instanceof Boolean || value instanceof BigDecimal || value instanceof Number) {
                output.append(value);
                return;
            }
            if (value instanceof Map<?, ?> map) {
                output.append('{');
                boolean first = true;
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    if (!first) {
                        output.append(',');
                    }
                    first = false;
                    output.append('"')
                            .append(escape(String.valueOf(entry.getKey())))
                            .append("\":");
                    append(output, entry.getValue());
                }
                output.append('}');
                return;
            }
            if (value instanceof Iterable<?> iterable) {
                output.append('[');
                boolean first = true;
                for (Object item : iterable) {
                    if (!first) {
                        output.append(',');
                    }
                    first = false;
                    append(output, item);
                }
                output.append(']');
                return;
            }
            throw new IllegalArgumentException("Tipo JSON non supportato: " + value.getClass());
        }

        private static String escape(String value) {
            StringBuilder escaped = new StringBuilder(value.length() + 16);
            for (int index = 0; index < value.length(); index++) {
                char ch = value.charAt(index);
                switch (ch) {
                    case '\\' -> escaped.append("\\\\");
                    case '"' -> escaped.append("\\\"");
                    case '\b' -> escaped.append("\\b");
                    case '\f' -> escaped.append("\\f");
                    case '\n' -> escaped.append("\\n");
                    case '\r' -> escaped.append("\\r");
                    case '\t' -> escaped.append("\\t");
                    default -> {
                        if (ch < 0x20) {
                            escaped.append(String.format("\\u%04x", (int) ch));
                        } else {
                            escaped.append(ch);
                        }
                    }
                }
            }
            return escaped.toString();
        }
    }
}
