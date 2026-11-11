# RecordsNext 4.0 — CODICE FUNZIONANTE v26 — GUI e pubblicazione Top & Flop (fix argfile v2)

Aggiornamento additivo. Nessun publisher parallelo e nessuna alterazione dei dati originali.

Bibbia precedente: `docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v25.md` SHA-256: `3669573e8a4b6517eeedbb47aca57597da48d1d47b3064ab6136a5baec9429be`

## `src/main/java/it/alterlega/recordsnext/app/model/RecordFamily.java`

SHA-256: `2304d6a223ad13d27b6f3b76eb7c8f9c7bd2e012e632f3e25baf49e1203eef92`

~~~~java
package it.alterlega.recordsnext.app.model;

/**
 * Famiglie elaborabili di RecordsNext 2.0.
 */
public enum RecordFamily {
    CLASSICS("classics"),
    SERIES("series"),
    RU("ru"),
    MODIFIERS("modifiers"),
    THRESHOLDS_LUCK("thresholdsLuck"),
    TOP_FLOP("topFlop");

    private final String id;

    RecordFamily(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }
}
~~~~

## `src/main/java/it/alterlega/recordsnext/app/model/CoreRecordCatalog.java`

SHA-256: `829657a0d3630d02cfbdc245b6c2495dad95e84356255ef90a8867e9c3376a99`

~~~~java
package it.alterlega.recordsnext.app.model;

import it.alterlega.recordsnext.app.config.ProcessingConfigWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Catalogo canonico dei record elaborabili.
 *
 * Deve restare allineato 1:1 con gli ID granulari esposti da
 * ProcessingConfigWriter, più gli eventuali record opt-in che non appartengono
 * alle checkbox granulari delle famiglie (attualmente il Culometro).
 */
public final class CoreRecordCatalog {
    public static final String CULOMETRO_ID = "easter-egg.culometro";

    private CoreRecordCatalog() {
    }

    public static List<RecordChild> children() {
        List<RecordChild> children = new ArrayList<>();

        addFamilyChildren(
                children,
                ProcessingConfigWriter.CLASSICS,
                RecordFamily.CLASSICS
        );

        addFamilyChildren(
                children,
                ProcessingConfigWriter.SERIES,
                RecordFamily.SERIES
        );

        addFamilyChildren(
                children,
                ProcessingConfigWriter.RU,
                RecordFamily.RU
        );

        addFamilyChildren(
                children,
                ProcessingConfigWriter.MODIFIERS,
                RecordFamily.MODIFIERS
        );

        addFamilyChildren(
                children,
                ProcessingConfigWriter.THRESHOLDS,
                RecordFamily.THRESHOLDS_LUCK
        );

        addFamilyChildren(children, ProcessingConfigWriter.TOP_FLOP, RecordFamily.TOP_FLOP);

        replace(
                children,
                new RecordChild(
                        "classics.highest-match-score",
                        "Maggior punteggio in una partita",
                        RecordFamily.CLASSICS,
                        Set.of(
                                RecordDependency.required("data.matches", DependencyType.DATA),
                                RecordDependency.required("data.scores", DependencyType.DATA)
                        ),
                        false
                )
        );

        replace(
                children,
                new RecordChild(
                        "ru.deciding",
                        "Riserve d'ufficio decisive",
                        RecordFamily.RU,
                        Set.of(
                                RecordDependency.required("ru.events", DependencyType.DATA),
                                RecordDependency.required("simulation.without-ru", DependencyType.SIMULATION)
                        ),
                        false
                )
        );

        replace(
                children,
                new RecordChild(
                        "modifiers.home-field-deciding",
                        "Fattore Campo decisivo",
                        RecordFamily.MODIFIERS,
                        Set.of(
                                RecordDependency.required("modifier.home-field", DependencyType.MODULE),
                                RecordDependency.required("configuration.goal-bands", DependencyType.CONFIGURATION)
                        ),
                        false
                )
        );

        replace(
                children,
                new RecordChild(
                        "thresholds.surgical-win",
                        "Vittoria chirurgica",
                        RecordFamily.THRESHOLDS_LUCK,
                        Set.of(
                                RecordDependency.required("data.scores", DependencyType.DATA),
                                RecordDependency.required("configuration.goal-bands", DependencyType.CONFIGURATION)
                        ),
                        false
                )
        );

        children.add(
                new RecordChild(
                        CULOMETRO_ID,
                        "Culometro",
                        RecordFamily.THRESHOLDS_LUCK,
                        Set.of(
                                RecordDependency.required(
                                        "configuration.culometro",
                                        DependencyType.CONFIGURATION
                                ),
                                RecordDependency.optional(
                                        "modifier.home-field",
                                        DependencyType.FAMILY_CHILD
                                )
                        ),
                        true
                )
        );

        return List.copyOf(children);
    }

    private static void addFamilyChildren(
            List<RecordChild> target,
            String[] ids,
            RecordFamily family
    ) {
        for (String id : ids) {
            target.add(
                    new RecordChild(
                            id,
                            id,
                            family,
                            Set.of(),
                            false
                    )
            );
        }
    }

    private static void replace(
            List<RecordChild> children,
            RecordChild replacement
    ) {
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).id().equals(replacement.id())) {
                children.set(i, replacement);
                return;
            }
        }

        throw new IllegalStateException(
                "Record da specializzare assente dal catalogo base: "
                        + replacement.id()
        );
    }
}
~~~~

## `src/main/java/it/alterlega/recordsnext/app/config/ProcessingConfigWriter.java`

SHA-256: `3c9ad837ec3ce32f9001df54d86ed0d4dfe7ecbdf1b28e6f17417babfc999ff7`

~~~~java
package it.alterlega.recordsnext.app.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ProcessingConfigWriter {
    public record State(
            boolean classics,
            boolean series,
            boolean ru,
            boolean modifiers,
            boolean thresholdsLuck,
            boolean topFlop,
            boolean culometro,
            boolean publishToSite,
            Map<String, Boolean> children,
            Map<String, String> modifierNames
    ) {
        public State {
            children = Map.copyOf(children == null ? Map.of() : children);
            modifierNames = Map.copyOf(modifierNames == null ? defaultModifierNames() : modifierNames);
        }

        /** Costruttore 3.2 invariato: Top & Flop non si attiva mai implicitamente. */
        public State(
                boolean classics,
                boolean series,
                boolean ru,
                boolean modifiers,
                boolean thresholdsLuck,
                boolean culometro,
                boolean publishToSite
        ) {
            this(classics, series, ru, modifiers, thresholdsLuck, false, culometro,
                    publishToSite, Map.of(), defaultModifierNames());
        }

        /** Compatibilita' con le precedenti GUI e suite di test 3.2. */
        public State(
                boolean classics, boolean series, boolean ru, boolean modifiers,
                boolean thresholdsLuck, boolean culometro, boolean publishToSite,
                Map<String, Boolean> children, Map<String, String> modifierNames
        ) {
            this(classics, series, ru, modifiers, thresholdsLuck, false, culometro,
                    publishToSite, children, modifierNames);
        }

        public boolean childEnabled(String id) {
            if (id.startsWith("topFlop.")) {
                return topFlopImplemented(id) && children.getOrDefault(id, true);
            }
            return children.getOrDefault(id, true);
        }

        public String modifierName(String sourceField) {
            return modifierNames.getOrDefault(sourceField, "");
        }
    }

    private ProcessingConfigWriter() {}

    public static Map<String, String> defaultModifierNames() {
        Map<String, String> names = new LinkedHashMap<>();
        names.put("MODM1PERS", "Modificatore Difesa");
        names.put("MODM2PERS", "Capitano");
        names.put("MODM3PERS", "");
        return names;
    }

    public static State load(Path file) throws IOException {
        String json = Files.readString(file, StandardCharsets.UTF_8).replace("\uFEFF", "");
        Map<String, Boolean> children = new LinkedHashMap<>();
        readChildren(json, "classics", CLASSICS, children);
        readChildren(json, "series", SERIES, children);
        readChildren(json, "ru", RU, children);
        readChildren(json, "modifiers", MODIFIERS, children);
        readChildren(json, "thresholdsLuck", THRESHOLDS, children);
        readChildren(json, "topFlop", TOP_FLOP, children);

        return new State(
                enabled(json, "classics"),
                enabled(json, "series"),
                enabled(json, "ru"),
                enabled(json, "modifiers"),
                enabled(json, "thresholdsLuck"),
                enabled(json, "topFlop"),
                blockBoolean(json, "culometro", "enabled"),
                blockBoolean(json, "output", "publishToSite"),
                children,
                readModifierNames(json)
        );
    }

    public static void save(Path file, State state) throws IOException {
        Files.createDirectories(file.toAbsolutePath().normalize().getParent());
        String json = """
                {
                  "schemaVersion": "2.0",
                  "processing": {
                    "families": {
                      "classics": {
                        "enabled": %s,
                        "children": %s
                      },
                      "series": {
                        "enabled": %s,
                        "children": %s
                      },
                      "ru": {
                        "enabled": %s,
                        "children": %s
                      },
                      "modifiers": {
                        "enabled": %s,
                        "children": %s
                      },
                      "thresholdsLuck": {
                        "enabled": %s,
                        "children": %s
                      },
                      "topFlop": {
                        "enabled": %s,
                        "children": %s
                      }
                    },
                    "modifierNames": {
                      "MODM1PERS": "%s",
                      "MODM2PERS": "%s",
                      "MODM3PERS": "%s"
                    },
                    "culometro": {
                      "enabled": %s,
                      "configFile": "config\\\\culometro.json"
                    },
                    "output": {
                      "writeManifest": true,
                      "writeCore": true,
                      "publishToSite": %s
                    }
                  }
                }
                """.formatted(
                state.classics(), childrenJson(state, CLASSICS),
                state.series(), childrenJson(state, SERIES),
                state.ru(), childrenJson(state, RU),
                state.modifiers(), childrenJson(state, MODIFIERS),
                state.thresholdsLuck(), childrenJson(state, THRESHOLDS),
                state.topFlop(), childrenJson(state, TOP_FLOP),
                jsonEscape(state.modifierName("MODM1PERS")),
                jsonEscape(state.modifierName("MODM2PERS")),
                jsonEscape(state.modifierName("MODM3PERS")),
                state.culometro(), state.publishToSite()
        );
        Files.writeString(file, json, StandardCharsets.UTF_8);
    }

    private static Map<String, String> readModifierNames(String json) {
        Map<String, String> result = new LinkedHashMap<>(defaultModifierNames());
        int start = json.indexOf("\"modifierNames\"");
        if (start < 0) return result;
        int blockStart = json.indexOf('{', start);
        int blockEnd = blockStart < 0 ? -1 : findMatchingBrace(json, blockStart);
        if (blockStart < 0 || blockEnd < 0) return result;
        String section = json.substring(blockStart, blockEnd + 1);
        for (String field : new String[] {"MODM1PERS", "MODM2PERS", "MODM3PERS"}) {
            Pattern pattern = Pattern.compile("\\\"" + Pattern.quote(field)
                    + "\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"])*)\\\"");
            Matcher matcher = pattern.matcher(section);
            if (matcher.find()) result.put(field, jsonUnescape(matcher.group(1)));
        }
        return result;
    }

    private static String jsonEscape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    private static String jsonUnescape(String value) {
        return value.replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t")
                .replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private static String childrenJson(State state, String[] ids) {
        StringBuilder out = new StringBuilder("{\n");
        for (int i = 0; i < ids.length; i++) {
            String id = ids[i];
            String key = id.substring(id.indexOf('.') + 1);
            out.append("                          \"").append(key).append("\": ")
                    .append(state.childEnabled(id));
            if (i + 1 < ids.length) out.append(',');
            out.append('\n');
        }
        out.append("                        }");
        return out.toString();
    }

    private static void readChildren(String json, String family, String[] ids, Map<String, Boolean> out) {
        int familyStart = json.indexOf('"' + family + '"');
        if (familyStart < 0) return;
        int childrenStart = json.indexOf("\"children\"", familyStart);
        if (childrenStart < 0) return;
        int colon = json.indexOf(':', childrenStart);
        if (colon < 0) return;
        String tail = json.substring(colon + 1).stripLeading();
        if (tail.startsWith("\"ALL\"")) {
            for (String id : ids) out.put(id, true);
            return;
        }
        int blockStart = json.indexOf('{', colon);
        int blockEnd = blockStart < 0 ? -1 : findMatchingBrace(json, blockStart);
        if (blockStart < 0 || blockEnd < 0) return;
        String section = json.substring(blockStart, blockEnd + 1);
        for (String id : ids) {
            String key = id.substring(id.indexOf('.') + 1);
            out.put(id, section.matches("(?s).*\\\"" + Pattern.quote(key)
                    + "\\\"\\s*:\\s*true.*"));
        }
    }

    private static int findMatchingBrace(String value, int start) {
        int depth = 0;
        for (int i = start; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch == '{') depth++;
            if (ch == '}' && --depth == 0) return i;
        }
        return -1;
    }

    private static boolean enabled(String json, String block) {
        return blockBoolean(json, block, "enabled");
    }

    private static boolean blockBoolean(String json, String block, String key) {
        int start = json.indexOf('"' + block + '"');
        if (start < 0) return false;
        int end = json.indexOf('}', start);
        if (end < 0) end = json.length();
        String section = json.substring(start, end);
        return section.matches("(?s).*\\\"" + key + "\\\"\\s*:\\s*true.*");
    }

    public static final String[] CLASSICS = {
            "classics.highest-match-score", "classics.lowest-match-score",
            "classics.most-regulation-goals", "classics.largest-regulation-margin",
            "classics.average-points", "classics.total-points", "classics.standings-points",
            "classics.wins", "classics.draws", "classics.losses", "classics.goals-for",
            "classics.goals-against", "classics.yellow-cards-team", "classics.red-cards-team",
            "classics.red-cards-player", "classics.assists-team", "classics.own-goals-team",
            "classics.penalties-scored", "classics.penalties-missed",
            "classics.penalties-saved", "classics.clean-sheets"
    };

    public static final String[] SERIES = {
            "series.unbeaten", "series.winless", "series.wins", "series.draws",
            "series.losses", "series.clean-sheets"
    };

    public static final String[] RU = {
            "ru.max-in-match", "ru.matches-with", "ru.matches-against", "ru.deciding",
            "ru.deciding-against", "ru.balance", "ru.balance-against",
            "ru.average-points", "ru.average-points-against", "ru.role-distribution"
    };

    public static final String[] MODIFIERS = {
            "modifiers.modm1pers.max", "modifiers.modm1pers.total", "modifiers.modm1pers.average", "modifiers.modm1pers.uses", "modifiers.modm1pers.series",
            "modifiers.modm2pers.max", "modifiers.modm2pers.total", "modifiers.modm2pers.average", "modifiers.modm2pers.uses", "modifiers.modm2pers.series",
            "modifiers.modm3pers.max", "modifiers.modm3pers.total", "modifiers.modm3pers.average", "modifiers.modm3pers.uses", "modifiers.modm3pers.series",
            "modifiers.modportiere.max", "modifiers.modportiere.total", "modifiers.modportiere.average", "modifiers.modportiere.uses", "modifiers.modportiere.series",
            "modifiers.moddifesa.max", "modifiers.moddifesa.total", "modifiers.moddifesa.average", "modifiers.moddifesa.uses", "modifiers.moddifesa.series",
            "modifiers.modcentrocampo.max", "modifiers.modcentrocampo.total", "modifiers.modcentrocampo.average", "modifiers.modcentrocampo.uses", "modifiers.modcentrocampo.series",
            "modifiers.modattacco.max", "modifiers.modattacco.total", "modifiers.modattacco.average", "modifiers.modattacco.uses", "modifiers.modattacco.series",
            "modifiers.modmodulo.max", "modifiers.modmodulo.total", "modifiers.modmodulo.average", "modifiers.modmodulo.uses", "modifiers.modmodulo.series",
            "modifiers.home-field-deciding", "modifiers.home-field-points-gained", "modifiers.home-field-points-lost", "modifiers.home-field-balance"
    };

    /** I venti record approvati sono ID stabili. Le voci non implementate restano visibili e disabilitate. */
    public static final String[] TOP_FLOP = {
            "topFlop.tf01", "topFlop.tf02", "topFlop.tf03", "topFlop.tf04",
            "topFlop.tf05", "topFlop.tf06", "topFlop.tf07", "topFlop.tf08",
            "topFlop.tf09", "topFlop.tf10", "topFlop.tf11", "topFlop.tf12",
            "topFlop.tf13", "topFlop.tf14", "topFlop.tf15", "topFlop.tf16",
            "topFlop.tf17", "topFlop.tf18", "topFlop.tf19", "topFlop.tf20"
    };

    public static boolean topFlopImplemented(String id) {
        return switch (id) {
            case "topFlop.tf01", "topFlop.tf02", "topFlop.tf03", "topFlop.tf04",
                    "topFlop.tf07", "topFlop.tf08", "topFlop.tf09", "topFlop.tf10",
                    "topFlop.tf16", "topFlop.tf17", "topFlop.tf18", "topFlop.tf19",
                    "topFlop.tf20" -> true;
            default -> false;
        };
    }

    public static final String[] THRESHOLDS = {
            "thresholds.surgical-win", "thresholds.mocking-loss",
            "thresholds.miraculous-draw", "thresholds.narrow-draw",
            "thresholds.missed-win-half-point", "thresholds.loss-by-a-whisker",
            "thresholds.exact-threshold", "thresholds.just-enough",
            "thresholds.wasted-points", "luck.favourable-events",
            "luck.unfavourable-events", "luck.balance"
    };
}
~~~~

## `src/main/java/it/alterlega/recordsnext/app/config/ProcessingConfigLoader.java`

SHA-256: `052295f322386aa7a75f2d244414b181873e3ca85712b6a4b49e17ff7457ce66`

~~~~java
package it.alterlega.recordsnext.app.config;

import it.alterlega.recordsnext.app.ProcessingOptions;
import it.alterlega.recordsnext.app.model.CoreRecordCatalog;
import it.alterlega.recordsnext.app.model.ProcessingSelection;
import it.alterlega.recordsnext.app.model.RecordFamily;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class ProcessingConfigLoader {
    private ProcessingConfigLoader() {}

    public static ProcessingOptions load(Path file) throws IOException {
        Object parsed = MiniJson.parse(Files.readString(file, StandardCharsets.UTF_8));
        Map<String, Object> root = object(parsed, "root");
        String schema = string(root.get("schemaVersion"), "schemaVersion");
        if (!"2.0".equals(schema)) {
            throw new IllegalArgumentException("Versione schema processing non supportata: " + schema);
        }

        Map<String, Object> processing = object(root.get("processing"), "processing");
        Map<String, Object> familiesNode = object(processing.get("families"), "processing.families");
        EnumSet<RecordFamily> families = EnumSet.noneOf(RecordFamily.class);
        Set<String> children = new LinkedHashSet<>();

        for (RecordFamily family : RecordFamily.values()) {
            Object raw = familiesNode.get(family.id());
            if (raw == null) continue;
            Map<String, Object> familyNode = object(raw, "family " + family.id());
            if (!bool(familyNode.get("enabled"), false)) continue;
            families.add(family);
            collectChildren(family, familyNode.get("children"), children);
        }

        Map<String, Object> culometroNode = object(processing.get("culometro"), "processing.culometro");
        boolean culometro = bool(culometroNode.get("enabled"), false);
        if (culometro) {
            families.add(RecordFamily.THRESHOLDS_LUCK);
            children.add(CoreRecordCatalog.CULOMETRO_ID);
        }

        Map<String, Object> output = object(processing.get("output"), "processing.output");
        boolean generateJs = bool(output.get("writeManifest"), true) || bool(output.get("writeCore"), true);
        boolean publish = bool(output.get("publishToSite"), false);

        ProcessingSelection selection = new ProcessingSelection(
                Set.copyOf(families), Set.copyOf(children), culometro, generateJs, publish
        );
        return ProcessingOptions.modular(selection);
    }

    private static void collectChildren(RecordFamily family, Object node, Set<String> out) {
        if (node == null || "ALL".equalsIgnoreCase(String.valueOf(node))) return;
        Map<String, Object> children = object(node, "children of " + family.id());
        for (Map.Entry<String, Object> entry : children.entrySet()) {
            if (!bool(entry.getValue(), false)) continue;
            if (family == RecordFamily.TOP_FLOP &&
                    !ProcessingConfigWriter.topFlopImplemented("topFlop." + entry.getKey())) {
                throw new IllegalArgumentException(
                        "Record Top & Flop non ancora verificato: " + entry.getKey());
            }
            String id = switch (family) {
                case MODIFIERS -> switch (entry.getKey()) {
                    case "defence" -> "modifiers.defence";
                    case "captain" -> "modifiers.captain";
                    case "homeField" -> "modifiers.home-field-deciding";
                    default -> "modifiers." + entry.getKey();
                };
                case THRESHOLDS_LUCK -> switch (entry.getKey()) {
                    case "favourable-events", "unfavourable-events", "balance" ->
                            "luck." + entry.getKey();
                    default -> "thresholds." + entry.getKey();
                };
                default -> family.id() + "." + entry.getKey();
            };
            out.add(id);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> object(Object value, String name) {
        if (value instanceof Map<?, ?> map) return (Map<String, Object>) map;
        throw new IllegalArgumentException("Oggetto JSON mancante o non valido: " + name);
    }

    private static boolean bool(Object value, boolean defaultValue) {
        return value == null ? defaultValue : Boolean.TRUE.equals(value);
    }

    private static String string(Object value, String name) {
        if (value instanceof String s && !s.isBlank()) return s;
        throw new IllegalArgumentException("Stringa JSON mancante o non valida: " + name);
    }
}
~~~~

## `src/main/java/it/alterlega/recordsnext/app/config/MiniJson.java`

SHA-256: `a3936d024812bcb0e661b1def202dcc4f6f247b383833e7a7782b19d93d52b8e`

~~~~java
package it.alterlega.recordsnext.app.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MiniJson {
    private final String text;
    private int pos;

    private MiniJson(String text) {
        this.text = text != null && text.startsWith("\uFEFF") ? text.substring(1) : text;
    }

    public static Object parse(String text) {
        MiniJson parser = new MiniJson(text);
        Object value = parser.readValue();
        parser.skipWhitespace();
        if (parser.pos != parser.text.length()) {
            throw parser.error("Contenuto inatteso dopo il valore JSON");
        }
        return value;
    }

    private Object readValue() {
        skipWhitespace();
        if (pos >= text.length()) throw error("Valore JSON mancante");
        return switch (text.charAt(pos)) {
            case '{' -> readObject();
            case '[' -> readArray();
            case '"' -> readString();
            case 't' -> readLiteral("true", Boolean.TRUE);
            case 'f' -> readLiteral("false", Boolean.FALSE);
            case 'n' -> readLiteral("null", null);
            default -> readNumber();
        };
    }

    private Map<String, Object> readObject() {
        expect('{');
        Map<String, Object> result = new LinkedHashMap<>();
        skipWhitespace();
        if (peek('}')) { pos++; return result; }
        while (true) {
            skipWhitespace();
            String key = readString();
            skipWhitespace();
            expect(':');
            result.put(key, readValue());
            skipWhitespace();
            if (peek('}')) { pos++; return result; }
            expect(',');
        }
    }

    private List<Object> readArray() {
        expect('[');
        List<Object> result = new ArrayList<>();
        skipWhitespace();
        if (peek(']')) { pos++; return result; }
        while (true) {
            result.add(readValue());
            skipWhitespace();
            if (peek(']')) { pos++; return result; }
            expect(',');
        }
    }

    private String readString() {
        expect('"');
        StringBuilder out = new StringBuilder();
        while (pos < text.length()) {
            char c = text.charAt(pos++);
            if (c == '"') return out.toString();
            if (c != '\\') { out.append(c); continue; }
            if (pos >= text.length()) throw error("Escape JSON incompleto");
            char e = text.charAt(pos++);
            switch (e) {
                case '"', '\\', '/' -> out.append(e);
                case 'b' -> out.append('\b');
                case 'f' -> out.append('\f');
                case 'n' -> out.append('\n');
                case 'r' -> out.append('\r');
                case 't' -> out.append('\t');
                case 'u' -> {
                    if (pos + 4 > text.length()) throw error("Escape Unicode incompleto");
                    out.append((char) Integer.parseInt(text.substring(pos, pos + 4), 16));
                    pos += 4;
                }
                default -> throw error("Escape JSON non valido: \\" + e);
            }
        }
        throw error("Stringa JSON non chiusa");
    }

    private Object readNumber() {
        int start = pos;
        while (pos < text.length() && "-+0123456789.eE".indexOf(text.charAt(pos)) >= 0) pos++;
        if (start == pos) throw error("Valore JSON non valido");
        String raw = text.substring(start, pos);
        try {
            return raw.contains(".") || raw.contains("e") || raw.contains("E")
                    ? Double.valueOf(raw) : Long.valueOf(raw);
        } catch (NumberFormatException ex) {
            throw error("Numero JSON non valido: " + raw);
        }
    }

    private Object readLiteral(String literal, Object value) {
        if (!text.startsWith(literal, pos)) throw error("Valore JSON non valido");
        pos += literal.length();
        return value;
    }

    private void skipWhitespace() {
        while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) pos++;
    }

    private void expect(char expected) {
        skipWhitespace();
        if (pos >= text.length() || text.charAt(pos) != expected) {
            throw error("Atteso '" + expected + "'");
        }
        pos++;
    }

    private boolean peek(char c) {
        return pos < text.length() && text.charAt(pos) == c;
    }

    private IllegalArgumentException error(String message) {
        return new IllegalArgumentException(message + " alla posizione " + pos);
    }
}
~~~~

## `src/main/java/it/alterlega/recordsnext/gui/RecordsNext2Dashboard.java`

SHA-256: `7acd627a0e1331f5ac42029797fdfe606dc858e88581c10395aa109730cd0ee4`

~~~~java
package it.alterlega.recordsnext.gui;

import it.alterlega.recordsnext.ConfigurationSchema;
import it.alterlega.recordsnext.app.PipelineConfig;
import it.alterlega.recordsnext.app.ProcessingMode;
import it.alterlega.recordsnext.app.ProcessingOptions;
import it.alterlega.recordsnext.app.RecordsNextPipeline;
import it.alterlega.recordsnext.app.config.ProcessingConfigLoader;
import it.alterlega.recordsnext.app.config.ProcessingConfigWriter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public final class RecordsNext2Dashboard {
    private static final Color NAVY = new Color(22, 42, 79);
    private static final Color BLUE = new Color(38, 86, 171);
    private static final Color RED = new Color(198, 38, 48);
    private static final Color BG = new Color(241, 245, 251);
    private static final Color BORDER = new Color(211, 219, 232);
    private static final Color MUTED = new Color(82, 92, 110);

    private final Path root = Path.of("").toAbsolutePath().normalize();
    private final Path processingFile = root.resolve("config/processing.json");
    private final Path culometroFile = root.resolve("config/culometro.json");
    private final Path propertiesFile = root.resolve("config/recordsnext-gui.properties");
    private final Path consolidationStateFile = root.resolve("data/consolidation/recordsnext-consolidation.properties");

    private final JFrame frame = new JFrame("RecordsNext by mauz79 · 4.0 (sviluppo)");
    private final CardLayout pages = new CardLayout();
    private final JPanel pageHost = new JPanel(pages);
    private final Map<String, JToggleButton> navButtons = new LinkedHashMap<>();
    private final Map<String, JCheckBox> familyChecks = new LinkedHashMap<>();
    private final Map<String, JCheckBox> childChecks = new LinkedHashMap<>();
    private final Map<String, JLabel> familySummaries = new LinkedHashMap<>();
    private final Map<String, JTextField> modifierNameFields = new LinkedHashMap<>();

    private final JCheckBox culometro = new JCheckBox("Genera Culometro");
    private final JCheckBox publish = new JCheckBox("Pubblica nel sito della stagione corrente al termine");
    private final JRadioButton full = new JRadioButton("Completa");
    private final JRadioButton consolidated = new JRadioButton("Consolidata");
    private final JTextArea log = new JTextArea();
    private final JProgressBar progress = new JProgressBar(0, 100);
    private final JLabel status = new JLabel("Pronto");
    private final JLabel phase = new JLabel("Nessuna elaborazione in corso");
    private final JButton run = new JButton("Elabora");
    private final JButton publishAllSites = new JButton("Pubblica i siti delle stagioni selezionate");
    private final JTextField exampleSiteDirectory = new JTextField();
    private final JLabel exampleRootTarget = new JLabel("Non selezionata");
    private final JLabel exampleViewsTarget = new JLabel("Non selezionata");
    private final JLabel exampleJsTarget = new JLabel("Non selezionata");
    private final JButton installExamples = new JButton("Installa esempi");
    private final JComboBox<String> htmlProfile = new JComboBox<>(new String[] {"mauzstrom", "maelstrom", "fantablue2", "neutral"});
    private ProcessingConfigWriter.State state;
    private JPanel dashboardPage;

    private static final Map<String, String> TOP_FLOP_LABELS = Map.ofEntries(
        Map.entry("topFlop.tf01", "TF01 - Miglior voto reale"),
        Map.entry("topFlop.tf02", "TF02 - Peggior voto reale"),
        Map.entry("topFlop.tf03", "TF03 - Miglior fantavoto apportato"),
        Map.entry("topFlop.tf04", "TF04 - Peggior fantavoto apportato"),
        Map.entry("topFlop.tf05", "TF05 - Media voto (soglia da definire)"),
        Map.entry("topFlop.tf06", "TF06 - Fantamedia (soglia da definire)"),
        Map.entry("topFlop.tf07", "TF07 - Gol reali e apportati"),
        Map.entry("topFlop.tf08", "TF08 - Assist reali e apportati"),
        Map.entry("topFlop.tf09", "TF09 - Malus"),
        Map.entry("topFlop.tf10", "TF10 - Presenze reali e fantasy"),
        Map.entry("topFlop.tf11", "TF11 - Voti sprecati (da verificare)"),
        Map.entry("topFlop.tf12", "TF12 - Errori pesati (da verificare)"),
        Map.entry("topFlop.tf13", "TF13 - Fantavoti sprecati (da verificare)"),
        Map.entry("topFlop.tf14", "TF14 - Stagione schillaciana (da definire)"),
        Map.entry("topFlop.tf15", "TF15 - Stagione sbagliata (da definire)"),
        Map.entry("topFlop.tf16", "TF16 - Rendimento per fantasquadra"),
        Map.entry("topFlop.tf17", "TF17 - Rigori parati"),
        Map.entry("topFlop.tf18", "TF18 - Gol subiti"),
        Map.entry("topFlop.tf19", "TF19 - Giornate con zero gol subiti"),
        Map.entry("topFlop.tf20", "TF20 - Portieri goleador e assist")
    );

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new RecordsNext2Dashboard().show();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, ex.toString(), "RecordsNext 4.0 (sviluppo)", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private RecordsNext2Dashboard() throws Exception {
        bootstrap();
        state = ProcessingConfigWriter.load(processingFile);
        applyLookAndFeel();
        build();
        loadState();
        refreshProcessingModeAvailability();
    }

    private void bootstrap() throws Exception {
        Files.createDirectories(root.resolve("config"));
        Files.createDirectories(root.resolve("data/database"));
        ConfigurationSchema.initializeEmpty(root.resolve("data/database/recordsnext.db"));
        if (!Files.isRegularFile(processingFile)) {
            ProcessingConfigWriter.save(processingFile,
                    new ProcessingConfigWriter.State(true, true, true, true, true, false, false));
        }
    }

    private static void applyLookAndFeel() {
        UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 12));
        UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 12));
        UIManager.put("CheckBox.font", new Font("Segoe UI", Font.PLAIN, 12));
        UIManager.put("RadioButton.font", new Font("Segoe UI", Font.PLAIN, 12));
        UIManager.put("TabbedPane.font", new Font("Segoe UI", Font.BOLD, 12));
    }

    private void build() {
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { saveQuietly(); frame.dispose(); }
        });
        frame.setMinimumSize(new Dimension(900, 620));
        frame.setSize(1040, 700);
        frame.setLocationRelativeTo(null);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(BG);
        rootPanel.add(buildSidebar(), BorderLayout.WEST);
        rootPanel.add(buildMain(), BorderLayout.CENTER);
        frame.setContentPane(rootPanel);
    }

    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setPreferredSize(new Dimension(180, 700));
        side.setBackground(NAVY);
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(new EmptyBorder(18, 12, 16, 12));

        JLabel brand = new JLabel("RecordsNext");
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.setFont(new Font("Segoe UI Black", Font.BOLD, 22));
        brand.setForeground(Color.WHITE);
        side.add(brand);
        JLabel version = new JLabel("by mauz79 · 4.0 dev");
        version.setAlignmentX(Component.LEFT_ALIGNMENT);
        version.setForeground(new Color(174, 192, 224));
        version.setFont(new Font("Segoe UI", Font.BOLD, 11));
        side.add(version);
        side.add(Box.createVerticalStrut(28));

        ButtonGroup group = new ButtonGroup();
        addNav(side, group, "dashboard", "Dashboard");
        addNav(side, group, "seasons", "Configurazione stagioni");
        addNav(side, group, "topflop", "Top & Flop");
        addNav(side, group, "project", "Progetto e motore");
        addNav(side, group, "tools", "Debug e strumenti");
        addNav(side, group, "log", "Log e diagnostica");
        side.add(Box.createVerticalGlue());
        JLabel footer = new JLabel("Backend modulare attivo");
        footer.setForeground(new Color(130, 219, 164));
        footer.setFont(new Font("Segoe UI", Font.BOLD, 11));
        side.add(footer);
        return side;
    }

    private void addNav(JPanel side, ButtonGroup group, String id, String text) {
        JToggleButton button = new JToggleButton(text);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setForeground(Color.WHITE);
        button.setBackground(NAVY);
        button.setBorder(new EmptyBorder(0, 12, 0, 8));
        button.setFocusPainted(false);
        button.addActionListener(e -> showPage(id));
        group.add(button);
        side.add(button);
        side.add(Box.createVerticalStrut(6));
        navButtons.put(id, button);
    }

    private JPanel buildMain() {
        JPanel main = new JPanel(new BorderLayout(14, 12));
        main.setOpaque(false);
        main.setBorder(new EmptyBorder(12, 14, 10, 14));
        main.add(buildHeader(), BorderLayout.NORTH);

        pageHost.setOpaque(false);
        dashboardPage = buildDashboardPage();
        pageHost.add(dashboardPage, "dashboard");
        pageHost.add(buildSeasonsPage(), "seasons");
        pageHost.add(buildFamiliesPage(), "families");
        pageHost.add(buildCulometroPage(), "culometro");
        pageHost.add(buildTopFlopPage(), "topflop");
        pageHost.add(buildProjectPage(), "project");
        pageHost.add(buildPublishPage(), "tools");
        pageHost.add(buildLogPage(), "log");
        main.add(pageHost, BorderLayout.CENTER);
        main.add(buildCompactRunBar(), BorderLayout.SOUTH);
        navButtons.get("dashboard").setSelected(true);
        return main;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new GridBagLayout());
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(0, 42));

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;

        JPanel leftSpacer = new JPanel();
        leftSpacer.setOpaque(false);
        leftSpacer.setPreferredSize(new Dimension(180, 1));
        c.gridx = 0;
        c.weightx = 0.0;
        header.add(leftSpacer, c);

        JLabel title = new JLabel("RecordsNext 4.0 (sviluppo)", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI Black", Font.BOLD, 25));
        title.setForeground(RED);
        c.gridx = 1;
        c.weightx = 1.0;
        header.add(title, c);

        status.setFont(new Font("Segoe UI", Font.BOLD, 12));
        status.setForeground(new Color(35, 105, 62));
        status.setHorizontalAlignment(SwingConstants.RIGHT);
        status.setPreferredSize(new Dimension(180, 24));
        c.gridx = 2;
        c.weightx = 0.0;
        header.add(status, c);

        return header;
    }

    private JPanel buildDashboardPage() {
        JPanel page = new JPanel(new BorderLayout(0, 10));
        page.setOpaque(false);
        page.add(centeredSectionTitle(
                "Dashboard",
                "Configura RecordsNext e avvia l’elaborazione dalla barra inferiore"
        ), BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 3, 12, 0));
        cards.setOpaque(false);
        cards.setPreferredSize(new Dimension(0, 185));
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 185));
        cards.add(actionCard(
                "Configurazione stagioni",
                seasonSummary() + "\nGestite e manuali\nSorgenti, siti e tabellini",
                "Apri configurazione",
                this::openSeasonConfiguration
        ));
        cards.add(actionCard(
                "Famiglie record",
                activeFamiliesSummary() + "\n\nSelezione granulare dei sottorecord",
                activeFamilyCountFromState() + " famiglie selezionate",
                () -> showPage("families")
        ));
        cards.add(actionCard(
                "Soglie, Fortuna e Culometro",
                thresholdsCulometroSummary(),
                state.culometro() ? "Culometro attivo" : "Culometro disattivo",
                () -> showPage("culometro")
        ));

        JPanel cardsTop = new JPanel(new BorderLayout());
        cardsTop.setOpaque(false);
        cardsTop.add(cards, BorderLayout.NORTH);
        page.add(cardsTop, BorderLayout.CENTER);
        return page;
    }

    private String seasonSummary() {
        try {
            Properties properties = new Properties();
            if (Files.isRegularFile(propertiesFile)) {
                try (InputStream input = Files.newInputStream(propertiesFile)) {
                    properties.load(input);
                }
            }
            Path database = root.resolve(
                    properties.getProperty("database", "data/database/recordsnext.db")
            ).normalize();
            if (Files.isRegularFile(database)) {
                int count = new SeasonConfigurationRepository(database).load().size();
                if (count > 0) return count == 1 ? "1 stagione" : count + " stagioni";
            }
        } catch (Exception ignored) {
            // Ripiego sul JSON pubblico, se il database non è ancora disponibile.
        }

        Path seasons = root.resolve("config/seasons.json");
        if (!Files.isRegularFile(seasons)) return "Nessuna stagione configurata";
        try {
            String json = Files.readString(seasons);
            int count = json.split("\"seasonNumber\"\s*:", -1).length - 1;
            if (count == 0) count = json.split("\"seasonId\"\s*:", -1).length - 1;
            return count == 1 ? "1 stagione" : count + " stagioni";
        } catch (Exception ex) {
            return "Stagioni configurate";
        }
    }

    private String activeFamiliesSummary() {
        java.util.List<String> names = new java.util.ArrayList<>();
        if (state.classics()) names.add("Classici");
        if (state.series()) names.add("Serie");
        if (state.ru()) names.add("Riserve d'Ufficio");
        if (state.modifiers()) names.add("Modificatori");
        if (state.topFlop()) names.add("Top & Flop");
        if (names.isEmpty()) return "Nessuna famiglia selezionata";
        return String.join("\n", names);
    }

    private String thresholdsCulometroSummary() {
        String thresholds = state.thresholdsLuck()
                ? "Soglie e indicatori di fortuna attivi"
                : "Soglie e indicatori di fortuna disattivi";
        String ru = state.ru()
                ? "Componenti RU disponibili"
                : "Componenti RU non elaborate";
        String generation = state.culometro()
                ? "Generazione Culometro selezionata"
                : "Generazione Culometro non selezionata";
        return thresholds + "\n" + ru + "\n" + generation;
    }

    private int activeFamilyCountFromState() {
        int count = 0;
        if (state.classics()) count++;
        if (state.series()) count++;
        if (state.ru()) count++;
        if (state.modifiers()) count++;
        if (state.topFlop()) count++;
        return count;
    }

    private JPanel buildSeasonsPage() {
        JPanel page = verticalPage();
        page.add(pageHeader("Configurazione stagioni",
                "Stagioni, sorgenti, siti e collegamenti storici", true));
        JPanel card = cardPanel(new BorderLayout(10, 10));
        JLabel text = new JLabel("<html><b>Gestita:</b> FCM e FCA obbligatori; sito locale e online opzionali.<br>"
                + "<b>Calendario:</b> gestito automaticamente dall'archivio interno RecordsNext.<br>"
                + "<b>Manuale:</b> solo anni nel formato YYYY/YYYY e numero stagione.<br>"
                + "Tabellini compatibili: <b>ris*.htm</b>, <b>ris*.html</b> e <b>ris*.php</b>.</html>");
        card.add(text, BorderLayout.CENTER);
        JButton open = new JButton("Apri configurazione stagioni");
        open.addActionListener(e -> openSeasonConfiguration());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actions.setOpaque(false);
        actions.add(open);
        card.add(actions, BorderLayout.SOUTH);
        page.add(card);
        return page;
    }

    private JPanel buildFamiliesPage() {
        JPanel page = new JPanel(new BorderLayout(0, 8));
        page.setOpaque(false);
        page.add(pageHeader("Famiglie record",
                "Attiva la famiglia e scegli i singoli sottorecord da elaborare", true), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Classici", familySelectionPanel("classics", "Classici",
                ProcessingConfigWriter.CLASSICS, CLASSIC_LABELS));
        tabs.addTab("Serie", seriesSelectionPanel());
        tabs.addTab("RU", familySelectionPanel("ru", "Riserve d'Ufficio",
                ProcessingConfigWriter.RU, RU_LABELS));
        tabs.addTab("Modificatori", modifiersSelectionPanel());
        page.add(tabs, BorderLayout.CENTER);

        JButton save = new JButton("Salva selezione granulare");
        save.addActionListener(e -> saveState());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottom.setOpaque(false);
        bottom.add(save);
        page.add(bottom, BorderLayout.SOUTH);
        return page;
    }

    private JPanel buildTopFlopPage() {
        JPanel page = familySelectionPanel("topFlop", "Top & Flop",
                ProcessingConfigWriter.TOP_FLOP, TOP_FLOP_LABELS);
        JLabel info = new JLabel("<html>Le viste di carriera e squadra restano disponibili. "
                + "I record grigi non sono ancora attivabili: mancano soglie o prove di possesso. "
                + "Nessuna esclusione o media verra' inventata.</html>");
        info.setForeground(MUTED);
        info.setBorder(new EmptyBorder(6, 12, 6, 12));
        page.add(info, BorderLayout.NORTH);
        for (String id : ProcessingConfigWriter.TOP_FLOP) {
            if (!ProcessingConfigWriter.topFlopImplemented(id)) {
                JCheckBox check = childChecks.get(id);
                check.setSelected(false);
                check.setEnabled(false);
                check.setToolTipText("Non attivo: richiede formule, soglie o dati di esclusione verificati");
            }
        }
        return page;
    }

    private JPanel seriesSelectionPanel() {
        JPanel panel = familySelectionPanel("series", "Serie",
                ProcessingConfigWriter.SERIES, SERIES_LABELS);
        JLabel note = new JLabel("<html><b>Serie dei modificatori:</b> si selezionano nella card Modificatori, accanto a Massimo, Totale, Media e Utilizzi.</html>");
        note.setForeground(MUTED);
        note.setBorder(new EmptyBorder(4, 12, 4, 12));
        panel.add(note, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel modifiersSelectionPanel() {
        JPanel rootPanel = new JPanel(new BorderLayout(0, 8));
        rootPanel.setOpaque(false);

        JPanel names = cardPanel(new GridBagLayout());
        names.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER), new EmptyBorder(8, 10, 8, 10)));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 3, 3, 8);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        JLabel title = new JLabel("Nomi modificatori personalizzati");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2; c.weightx = 1.0;
        names.add(title, c);
        addModifierNameRow(names, c, 1, "MODM1PERS", "Personalizzato 1");
        addModifierNameRow(names, c, 2, "MODM2PERS", "Personalizzato 2");
        addModifierNameRow(names, c, 3, "MODM3PERS", "Personalizzato 3");
        rootPanel.add(names, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setBackground(Color.WHITE);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(8, 10, 8, 10));
        JCheckBox master = new JCheckBox("Elabora famiglia Modificatori");
        master.setFont(new Font("Segoe UI", Font.BOLD, 15));
        familyChecks.put("modifiers", master);
        JLabel summary = new JLabel();
        summary.setForeground(MUTED);
        familySummaries.put("modifiers", summary);
        JPanel head = new JPanel(new BorderLayout()); head.setOpaque(false);
        head.add(master, BorderLayout.WEST); head.add(summary, BorderLayout.EAST);
        body.add(head);

        body.add(modifierGroupPanel("Modificatori personalizzati", new String[] {"MODM1PERS", "MODM2PERS", "MODM3PERS"}));
        body.add(modifierGroupPanel("Modificatori standard FCM", new String[] {"MODPORTIERE", "MODDIFESA", "MODCENTROCAMPO", "MODATTACCO", "MODMODULO"}));
        body.add(modifierSimpleCheck("modifiers.home-field-deciding", "Fattore Campo decisivo"));
        body.add(modifierSimpleCheck("modifiers.home-field-points-gained", "Punti guadagnati col Fattore Campo"));
        body.add(modifierSimpleCheck("modifiers.home-field-points-lost", "Punti persi fuori casa"));
        body.add(modifierSimpleCheck("modifiers.home-field-balance", "Saldo Fattore Campo"));

        master.addActionListener(e -> {
            boolean enabled = master.isSelected();
            for (String id : ProcessingConfigWriter.MODIFIERS) childChecks.get(id).setEnabled(enabled);
            updateFamilySummary("modifiers", ProcessingConfigWriter.MODIFIERS);
        });
        rootPanel.add(new JScrollPane(body), BorderLayout.CENTER);
        return rootPanel;
    }

    private JPanel modifierGroupPanel(String title, String[] sourceFields) {
        JPanel group = new JPanel();
        group.setOpaque(false);
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setBorder(BorderFactory.createTitledBorder(title));
        for (String sourceField : sourceFields) {
            String fixedName = switch (sourceField) {
                case "MODPORTIERE" -> "Modificatore Portiere FCM";
                case "MODDIFESA" -> "Modificatore Difesa FCM";
                case "MODCENTROCAMPO" -> "Modificatore Centrocampo FCM";
                case "MODATTACCO" -> "Modificatore Attacco FCM";
                case "MODMODULO" -> "Modificatore Modulo FCM";
                default -> sourceField;
            };
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
            row.setOpaque(false);
            JLabel label = new JLabel(fixedName + " (" + sourceField + ")");
            label.setPreferredSize(new Dimension(245, 24));
            row.add(label);
            for (String stat : new String[] {"max", "total", "average", "uses", "series"}) {
                String id = "modifiers." + sourceField.toLowerCase() + "." + stat;
                String text = switch (stat) {
                    case "max" -> "Massimo";
                    case "total" -> "Totale";
                    case "average" -> "Media";
                    case "uses" -> "Utilizzi";
                    default -> "Serie";
                };
                JCheckBox check = new JCheckBox(text);
                check.setToolTipText(id);
                check.addActionListener(e -> updateFamilySummary("modifiers", ProcessingConfigWriter.MODIFIERS));
                childChecks.put(id, check);
                row.add(check);
            }
            group.add(row);
        }
        return group;
    }

    private JCheckBox modifierSimpleCheck(String id, String text) {
        JCheckBox check = new JCheckBox(text);
        check.setToolTipText(id);
        check.addActionListener(e -> updateFamilySummary("modifiers", ProcessingConfigWriter.MODIFIERS));
        childChecks.put(id, check);
        return check;
    }

    private void addModifierNameRow(JPanel panel, GridBagConstraints c, int row,
                                    String sourceField, String label) {
        c.gridwidth = 1; c.gridy = row; c.gridx = 0; c.weightx = 0.0;
        JLabel fieldLabel = new JLabel(label + " (" + sourceField + ")");
        panel.add(fieldLabel, c);

        JTextField field = new JTextField(28);
        field.setToolTipText("Nome mostrato nei dati e nei visualizzatori");
        modifierNameFields.put(sourceField, field);
        c.gridx = 1; c.weightx = 1.0;
        panel.add(field, c);
    }

    private JPanel familySelectionPanel(String familyId, String title, String[] ids, Map<String, String> labels) {
        JPanel body = new JPanel(new BorderLayout(8, 8));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(10, 12, 10, 12));

        JCheckBox master = new JCheckBox("Elabora famiglia " + title);
        master.setFont(new Font("Segoe UI", Font.BOLD, 15));
        JLabel summary = new JLabel();
        summary.setForeground(MUTED);
        familySummaries.put(familyId, summary);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(master, BorderLayout.WEST);
        top.add(summary, BorderLayout.EAST);
        body.add(top, BorderLayout.NORTH);
        familyChecks.put(familyId, master);

        JPanel grid = new JPanel(new GridLayout(0, 2, 8, 8));
        grid.setOpaque(false);
        for (String id : ids) {
            JCheckBox child = new JCheckBox(labels.getOrDefault(id, id));
            child.setToolTipText(id);
            child.addActionListener(e -> updateFamilySummary(familyId, ids));
            childChecks.put(id, child);
            grid.add(child);
        }
        JPanel gridTop = new JPanel(new BorderLayout());
        gridTop.setOpaque(false);
        gridTop.add(grid, BorderLayout.NORTH);
        master.addActionListener(e -> {
            boolean enabled = master.isSelected();
            for (String id : ids) childChecks.get(id).setEnabled(enabled &&
                    (!familyId.equals("topFlop") || ProcessingConfigWriter.topFlopImplemented(id)));
            updateFamilySummary(familyId, ids);
        });
        JScrollPane scroll = new JScrollPane(gridTop);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        body.add(scroll, BorderLayout.CENTER);

        JPanel commands = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        commands.setOpaque(false);
        JButton all = new JButton("Seleziona tutti");
        JButton none = new JButton("Deseleziona tutti");
        all.addActionListener(e -> setChildren(ids, true, familyId));
        none.addActionListener(e -> setChildren(ids, false, familyId));
        commands.add(all);
        commands.add(none);
        body.add(commands, BorderLayout.SOUTH);
        return body;
    }

    private JPanel buildCulometroPage() {
        JPanel page = new JPanel(new BorderLayout(0, 8));
        page.setOpaque(false);
        page.add(pageHeader("Soglie, Fortuna e Culometro",
                "Configura gli indicatori di base e scegli separatamente se generare il Culometro", true),
                BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Soglie e Fortuna",
                familySelectionPanel("thresholdsLuck", "Soglie e Fortuna",
                        ProcessingConfigWriter.THRESHOLDS, THRESHOLD_LABELS));

        JPanel culometroPanel = new JPanel(new BorderLayout(10, 10));
        culometroPanel.setBackground(Color.WHITE);
        culometroPanel.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel explanation = new JLabel("<html>"
                + "<b>Generazione opzionale:</b> il Culometro viene prodotto solo quando la casella è selezionata.<br><br>"
                + "Usa gli indicatori di Soglie/Fortuna e, quando disponibili, soltanto le RU "
                + "di cui il dataset dimostra l'effetto decisivo.<br><br>"
                + "<b>Modalità semplice:</b> preset e slider principali. "
                + "<b>Modalità avanzata:</b> pesi, rarità, affidabilità ed etichette."
                + "</html>");
        culometroPanel.add(explanation, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        culometro.setText("Genera Culometro");
        actions.add(culometro);
        JButton configure = new JButton("Configura preset, slider e pesi");
        configure.addActionListener(e -> openCulometroSettings());
        actions.add(configure);
        culometroPanel.add(actions, BorderLayout.SOUTH);
        tabs.addTab("Generazione Culometro", culometroPanel);

        JPanel publication = new JPanel(new BorderLayout(8, 8));
        publication.setBackground(Color.WHITE);
        publication.setBorder(new EmptyBorder(12, 14, 12, 14));
        publication.add(new JLabel("<html><b>HTML Culometro:</b><br>"
                + "• modello senza dati: copiabile nella cartella della skin FCM;<br>"
                + "• pagina con dati incorporati: pubblicabile direttamente nel sito generato;<br>"
                + "• dati JavaScript: destinazione <code>js</code> del sito.<br><br>"
                + "Questi comandi saranno abilitati quando verrà definito il pacchetto HTML definitivo."
                + "</html>"), BorderLayout.CENTER);
        tabs.addTab("HTML e pubblicazione", publication);

        page.add(tabs, BorderLayout.CENTER);

        JButton save = new JButton("Salva Soglie e Culometro");
        save.addActionListener(e -> saveState());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottom.setOpaque(false);
        bottom.add(save);
        page.add(bottom, BorderLayout.SOUTH);
        return page;
    }

    private JPanel buildProjectPage() {
        JPanel page = verticalPage();
        page.add(pageHeader("Progetto e motore",
                "Percorsi operativi, configurazioni e stato della pipeline", true));
        JPanel card = cardPanel(new GridLayout(0, 1, 6, 6));
        card.add(infoLine("Cartella progetto", root.toString()));
        card.add(infoLine("Configurazione elaborazione", processingFile.toString()));
        card.add(infoLine("Configurazione Culometro", culometroFile.toString()));
        card.add(infoLine("Database predefinito", root.resolve("data/database/recordsnext.db").toString()));
        card.add(infoLine("Configurazione GUI", Files.isRegularFile(propertiesFile)
                ? propertiesFile.toString() : "Default PipelineConfig"));
        card.add(infoLine("Backend", "Modulare 2.0 attivo"));
        card.add(infoLine("Modalità selezionata", consolidated.isSelected() ? "Consolidata" : "Completa"));
        card.add(infoLine("Famiglie attive", String.valueOf(activeFamilyCount())));
        card.add(infoLine("Sottorecord attivi", String.valueOf(activeChildCount())));
        card.add(infoLine("Culometro", culometro.isSelected() ? "Attivo" : "Disattivo"));
        card.add(infoLine("Fase corrente", phase.getText()));
        page.add(card);
        return page;
    }

    private JPanel buildPublishPage() {
        JPanel page = verticalPage();
        page.add(pageHeader("Debug e strumenti",
                "Diagnostica della pubblicazione e utilità per la skin FCM", true));

        JPanel publishCard = cardPanel(new BorderLayout(8, 8));
        JLabel text = new JLabel("<html>L'elaborazione normale pubblica, se richiesto, solo nel sito "
                + "della stagione corrente. La pubblicazione di tutti i siti storici è un'azione separata: "
                + "ogni sito riceve esclusivamente lo storico disponibile fino alla propria stagione.</html>");
        publishCard.add(text, BorderLayout.CENTER);
        JPanel options = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        options.setOpaque(false);
        options.add(publish);
        publishAllSites.setToolTipText(
                "Rigenera e pubblica RecordsNext nei siti configurati delle stagioni selezionate"
        );
        publishAllSites.addActionListener(e -> runPipeline(
                RecordsNextPipeline.PublicationMode.ALL_CONFIGURED_SITES,
                true
        ));
        options.add(publishAllSites);
        publishCard.add(options, BorderLayout.SOUTH);
        page.add(publishCard);
        page.add(Box.createVerticalStrut(10));

        JPanel examples = cardPanel(new BorderLayout(10, 10));
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Installa esempi nella skin");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(NAVY);
        JLabel subtitle = new JLabel("Seleziona la cartella della skin FCM e il profilo grafico da installare.");
        subtitle.setForeground(MUTED);
        heading.add(title);
        heading.add(Box.createVerticalStrut(2));
        heading.add(subtitle);
        examples.add(heading, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 0, 3, 8);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0;
        c.gridx = 0;
        c.gridy = 0;
        body.add(new JLabel("Cartella skin:"), c);

        exampleSiteDirectory.setEditable(false);
        c.gridx = 1;
        c.weightx = 1;
        body.add(exampleSiteDirectory, c);

        JButton browse = new JButton("Sfoglia...");
        browse.addActionListener(e -> chooseExampleSiteDirectory());
        c.gridx = 2;
        c.weightx = 0;
        body.add(browse, c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 1;
        c.weightx = 0;
        body.add(new JLabel("Profilo HTML:"), c);

        htmlProfile.setSelectedItem("mauzstrom");
        htmlProfile.setPreferredSize(new Dimension(180, 26));
        c.gridx = 1;
        c.gridwidth = 2;
        c.weightx = 1;
        body.add(htmlProfile, c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 1;
        c.weightx = 0;
        body.add(new JLabel("HTML indice:"), c);
        c.gridx = 1;
        c.gridwidth = 2;
        body.add(exampleRootTarget, c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 1;
        body.add(new JLabel("Viste e asset:"), c);
        c.gridx = 1;
        c.gridwidth = 2;
        body.add(exampleViewsTarget, c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 1;
        body.add(new JLabel("JavaScript:"), c);
        c.gridx = 1;
        c.gridwidth = 2;
        body.add(exampleJsTarget, c);

        examples.add(body, BorderLayout.CENTER);

        JButton preview = new JButton("Anteprima destinazioni");
        preview.setEnabled(false);
        installExamples.setEnabled(false);
        installExamples.setToolTipText("Installa gli HTML RecordsNext e il profilo grafico selezionato");
        installExamples.addActionListener(e -> installExampleViewers());
        JPanel exampleActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        exampleActions.setOpaque(false);
        exampleActions.add(preview);
        exampleActions.add(installExamples);
        examples.add(exampleActions, BorderLayout.SOUTH);

        page.add(examples);
        return page;
    }

    private void chooseExampleSiteDirectory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Seleziona la cartella della skin FCM");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        String current = exampleSiteDirectory.getText().trim();
        if (!current.isEmpty()) {
            Path currentPath = Path.of(current);
            if (Files.isDirectory(currentPath)) chooser.setCurrentDirectory(currentPath.toFile());
        }

        if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) return;

        Path siteRoot = chooser.getSelectedFile().toPath().toAbsolutePath().normalize();
        exampleSiteDirectory.setText(siteRoot.toString());
        exampleRootTarget.setText(siteRoot.toString());
        exampleViewsTarget.setText(siteRoot.resolve("RecordsNext").toString());
        exampleJsTarget.setText(siteRoot.resolve("js").toString());
        installExamples.setEnabled(true);
        status.setText("Destinazioni skin rilevate");
        status.setForeground(new Color(35, 105, 62));
    }

    private Path resolveViewerPackageDirectory() {
        Path releasePackage = root.resolve("visualizzatori");
        if (Files.isDirectory(releasePackage)) return releasePackage;

        Path developmentPackage = root.resolve("release/visualizzatori");
        if (Files.isDirectory(developmentPackage)) return developmentPackage;

        throw new IllegalStateException("Pacchetto visualizzatori non trovato.");
    }

    private void installExampleViewers() {
        String selectedSite = exampleSiteDirectory.getText().trim();
        if (selectedSite.isEmpty()) {
            JOptionPane.showMessageDialog(frame,
                    "Selezionare prima la cartella della skin FCM.",
                    "Installazione visualizzatori",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Path siteRoot = Path.of(selectedSite).toAbsolutePath().normalize();
            if (!Files.isDirectory(siteRoot)) {
                throw new IllegalStateException("Cartella skin non trovata: " + siteRoot);
            }

            Path packageDir = resolveViewerPackageDirectory();
            String profile = String.valueOf(htmlProfile.getSelectedItem());
            Path profileCss = packageDir.resolve("profiles").resolve(profile).resolve("recordsnext.css");

            if (!Files.isRegularFile(profileCss)) {
                throw new IllegalStateException("Profilo CSS non trovato: " + profileCss);
            }

            Path siteJs = siteRoot.resolve("js");
            Path siteViews = siteRoot.resolve("RecordsNext");
            Files.createDirectories(siteJs);
            Files.createDirectories(siteViews);

            Files.copy(packageDir.resolve("recordsnext.html"),
                    siteRoot.resolve("recordsnext.html"),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            try (java.util.stream.Stream<Path> stream = Files.list(packageDir.resolve("RecordsNext"))) {
                for (Path source : stream.filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().toLowerCase(java.util.Locale.ROOT).endsWith(".html"))
                        .toList()) {
                    Files.copy(source,
                            siteViews.resolve(source.getFileName()),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
            }

            Files.copy(packageDir.resolve("js/fcmRecordsNextFunzioni_common.js"),
                    siteJs.resolve("fcmRecordsNextFunzioni_common.js"),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            Files.copy(packageDir.resolve("js/fcmRecordsNextFunzioni_viewer.js"),
                    siteJs.resolve("fcmRecordsNextFunzioni_viewer.js"),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            Files.copy(profileCss,
                    siteViews.resolve("recordsnext.css"),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            status.setText("Visualizzatori installati · profilo " + profile);
            status.setForeground(new Color(35, 105, 62));

            JOptionPane.showMessageDialog(frame,
                    "Visualizzatori RecordsNext installati.\n\n"
                            + "Profilo: " + profile + "\n"
                            + "Indice: " + siteRoot.resolve("recordsnext.html") + "\n"
                            + "Viste: " + siteViews + "\n"
                            + "JavaScript: " + siteJs,
                    "Installazione visualizzatori",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            status.setText("Errore installazione visualizzatori");
            status.setForeground(RED);
            JOptionPane.showMessageDialog(frame,
                    ex.getMessage(),
                    "Installazione visualizzatori",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel buildLogPage() {
        JPanel page = new JPanel(new BorderLayout(10, 10));
        page.setOpaque(false);
        page.add(pageHeader("Log e diagnostica", "Messaggi della pipeline e tempi di elaborazione", true), BorderLayout.NORTH);
        log.setEditable(false);
        log.setFont(new Font("Consolas", Font.PLAIN, 12));
        log.setLineWrap(false);
        ((DefaultCaret) log.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        page.add(new JScrollPane(log), BorderLayout.CENTER);
        JButton clear = new JButton("Pulisci log");
        clear.addActionListener(e -> log.setText(""));
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.add(clear);
        page.add(bottom, BorderLayout.SOUTH);
        return page;
    }

    private JPanel buildCompactRunBar() {
        JPanel bar = cardPanel(new BorderLayout(10, 0));
        bar.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER), new EmptyBorder(7, 9, 7, 9)));
        JPanel options = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        options.setOpaque(false);
        ButtonGroup group = new ButtonGroup();
        group.add(full);
        group.add(consolidated);
        options.add(new JLabel("Modalità:"));
        options.add(consolidated);
        options.add(full);
        bar.add(options, BorderLayout.CENTER);

        JButton save = new JButton("Salva");
        save.addActionListener(e -> saveState());
        JButton terminate = new JButton("Termina");
        terminate.addActionListener(e -> {
            saveQuietly();
            frame.dispose();
            System.exit(0);
        });
        run.setBackground(BLUE);
        run.setForeground(Color.WHITE);
        run.addActionListener(e -> runPipeline(RecordsNextPipeline.PublicationMode.CURRENT_SITE, false));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(save);
        actions.add(run);
        actions.add(terminate);
        bar.add(actions, BorderLayout.EAST);
        return bar;
    }

    private JPanel pageHeader(String title, String subtitle, boolean back) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.setPreferredSize(new Dimension(0, 72));

        JPanel leftSpacer = new JPanel();
        leftSpacer.setOpaque(false);
        leftSpacer.setPreferredSize(new Dimension(150, 36));
        wrapper.add(leftSpacer, BorderLayout.WEST);

        JPanel labels = sectionTitle(title, subtitle, SwingConstants.CENTER);
        wrapper.add(labels, BorderLayout.CENTER);

        JPanel holder = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        holder.setOpaque(false);
        holder.setPreferredSize(new Dimension(150, 36));

        if (back) {
            JButton button = new JButton("← Dashboard");
            button.addActionListener(e -> showPage("dashboard"));
            holder.add(button);
        }

        wrapper.add(holder, BorderLayout.EAST);
        return wrapper;
    }

    private JPanel centeredSectionTitle(String title, String subtitle) {
        return sectionTitle(title, subtitle, SwingConstants.CENTER);
    }

    private static JPanel sectionTitle(String titleText, String subtitleText, int alignment) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel title = new JLabel(titleText, alignment);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(NAVY);
        title.setAlignmentX(alignment == SwingConstants.CENTER ? Component.CENTER_ALIGNMENT : Component.LEFT_ALIGNMENT);
        JLabel subtitle = new JLabel(subtitleText, alignment);
        subtitle.setForeground(MUTED);
        subtitle.setAlignmentX(alignment == SwingConstants.CENTER ? Component.CENTER_ALIGNMENT : Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(2));
        panel.add(subtitle);
        panel.add(Box.createVerticalStrut(10));
        return panel;
    }

    private JPanel actionCard(String title, String text, String value, Runnable action) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER),
                new EmptyBorder(12, 10, 10, 10)
        ));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.setPreferredSize(new Dimension(230, 185));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 185));

        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setFont(new Font("Segoe UI", Font.BOLD, 14));
        t.setForeground(NAVY);
        t.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel body = new JLabel(
                "<html><div style='text-align:center;'>" + text.replace("\n", "<br>") + "</div></html>",
                SwingConstants.CENTER
        );
        body.setForeground(MUTED);
        body.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel v = new JLabel(value + "  ›", SwingConstants.CENTER);
        v.setFont(new Font("Segoe UI", Font.BOLD, 12));
        v.setForeground(BLUE);
        v.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(t);
        card.add(Box.createVerticalStrut(14));
        card.add(body);
        card.add(Box.createVerticalGlue());
        card.add(v);

        MouseAdapter click = new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { action.run(); }
        };
        card.addMouseListener(click);
        t.addMouseListener(click);
        body.addMouseListener(click);
        v.addMouseListener(click);
        return card;
    }

    private static JPanel verticalPage() {
        JPanel page = new JPanel();
        page.setOpaque(false);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        return page;
    }

    private static JPanel cardPanel(LayoutManager layout) {
        JPanel card = new JPanel(layout);
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER), new EmptyBorder(10, 11, 10, 11)));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }

    private static JPanel infoLine(String label, String value) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        JLabel left = new JLabel(label);
        left.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JLabel right = new JLabel(value);
        right.setForeground(MUTED);
        row.add(left, BorderLayout.WEST);
        row.add(right, BorderLayout.CENTER);
        return row;
    }

    private void setChildren(String[] ids, boolean selected, String familyId) {
        for (String id : ids) childChecks.get(id).setSelected(selected &&
                (!familyId.equals("topFlop") || ProcessingConfigWriter.topFlopImplemented(id)));
        updateFamilySummary(familyId, ids);
    }

    private void updateFamilySummary(String familyId, String[] ids) {
        long selected = java.util.Arrays.stream(ids).filter(id -> childChecks.get(id).isSelected()).count();
        familySummaries.get(familyId).setText(selected + " / " + ids.length + " selezionati");
    }

    private void showPage(String id) {
        pages.show(pageHost, id);
        JToggleButton button = navButtons.get(id);
        if (button != null) button.setSelected(true);
    }


    private void refreshDashboard() {
        if (dashboardPage != null) pageHost.remove(dashboardPage);
        dashboardPage = buildDashboardPage();
        pageHost.add(dashboardPage, "dashboard");
        pageHost.revalidate();
        pageHost.repaint();
    }

    private void openSeasonConfiguration() {
        try {
            RecordsNextConfigurationDialog dialog = new RecordsNextConfigurationDialog(frame, root, propertiesFile);
            if (dialog.open()) {
                status.setText("Configurazione stagioni salvata");
                refreshDashboard();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Configurazione stagioni", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openCulometroSettings() {
        try {
            CulometroSettingsDialog dialog = new CulometroSettingsDialog(frame, culometroFile);
            if (dialog.open()) {
                culometro.setSelected(true);
                saveState();
                status.setText("Configurazione Culometro salvata");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Culometro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadState() {
        familyChecks.get("classics").setSelected(state.classics());
        familyChecks.get("series").setSelected(state.series());
        familyChecks.get("ru").setSelected(state.ru());
        familyChecks.get("modifiers").setSelected(state.modifiers());
        familyChecks.get("thresholdsLuck").setSelected(state.thresholdsLuck());
        familyChecks.get("topFlop").setSelected(state.topFlop());
        loadChildren(ProcessingConfigWriter.CLASSICS, "classics");
        loadChildren(ProcessingConfigWriter.SERIES, "series");
        loadChildren(ProcessingConfigWriter.RU, "ru");
        loadChildren(ProcessingConfigWriter.MODIFIERS, "modifiers");
        loadChildren(ProcessingConfigWriter.THRESHOLDS, "thresholdsLuck");
        loadChildren(ProcessingConfigWriter.TOP_FLOP, "topFlop");
        culometro.setSelected(state.culometro());
        publish.setSelected(state.publishToSite());
        // La disponibilita della modalita Consolidata viene aggiornata
        // da refreshProcessingModeAvailability().
        modifierNameFields.forEach((sourceField, field) ->
                field.setText(state.modifierName(sourceField)));
    }

    private void loadChildren(String[] ids, String familyId) {
        boolean familyEnabled = familyChecks.get(familyId).isSelected();
        for (String id : ids) {
            JCheckBox child = childChecks.get(id);
            child.setSelected(state.childEnabled(id));
            child.setEnabled(familyEnabled &&
                    (!familyId.equals("topFlop") || ProcessingConfigWriter.topFlopImplemented(id)));
        }
        updateFamilySummary(familyId, ids);
    }


    private void refreshProcessingModeAvailability() {
        boolean available = Files.isRegularFile(consolidationStateFile);
        consolidated.setEnabled(available);
        consolidated.setToolTipText(available
                ? "Usa il consolidamento esistente e aggiorna la stagione corrente."
                : "Disponibile dopo una prima elaborazione Completa riuscita.");
        if (!available) {
            consolidated.setSelected(false);
            full.setSelected(true);
        } else if (!consolidated.isSelected() && !full.isSelected()) {
            consolidated.setSelected(true);
        }
    }

    private void saveState() {
        try {
            Map<String, Boolean> children = new LinkedHashMap<>();
            childChecks.forEach((id, check) -> children.put(id, check.isSelected()));
            state = new ProcessingConfigWriter.State(
                    familyChecks.get("classics").isSelected(),
                    familyChecks.get("series").isSelected(),
                    familyChecks.get("ru").isSelected(),
                    familyChecks.get("modifiers").isSelected(),
                    familyChecks.get("thresholdsLuck").isSelected(),
                    familyChecks.get("topFlop").isSelected(),
                    culometro.isSelected(),
                    publish.isSelected(),
                    children,
                    readModifierNamesFromFields()
            );
            ProcessingConfigWriter.save(processingFile, state);
            status.setText("Configurazione salvata");
            status.setForeground(new Color(35, 105, 62));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Salvataggio", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Map<String, String> readModifierNamesFromFields() {
        Map<String, String> names = new LinkedHashMap<>();
        for (String sourceField : new String[] {"MODM1PERS", "MODM2PERS", "MODM3PERS"}) {
            JTextField field = modifierNameFields.get(sourceField);
            names.put(sourceField, field == null ? "" : field.getText().trim());
        }
        return names;
    }

    private void saveQuietly() {
        try { saveState(); } catch (Exception ignored) { }
    }

    private int activeFamilyCount() {
        return (int) familyChecks.values().stream().filter(AbstractButton::isSelected).count();
    }

    private int activeChildCount() {
        return (int) childChecks.values().stream().filter(AbstractButton::isSelected).count();
    }

    private void runPipeline(
            RecordsNextPipeline.PublicationMode publicationMode,
            boolean forcePublish) {
        if (forcePublish && !publish.isSelected()) {
            publish.setSelected(true);
        }
        saveState();
        run.setEnabled(false);
        publishAllSites.setEnabled(false);
        log.setText("");
        progress.setValue(0);
        phase.setText("Preparazione");
        status.setText("Elaborazione in corso");
        status.setForeground(BLUE);
        showPage("log");

        new SwingWorker<RecordsNextPipeline.Result, String>() {
            @Override protected RecordsNextPipeline.Result doInBackground() throws Exception {
                PipelineConfig cfg = Files.isRegularFile(propertiesFile)
                        ? PipelineConfig.load(root, propertiesFile)
                        : PipelineConfig.defaults(root);
                ProcessingOptions options = ProcessingConfigLoader.load(processingFile);
                ProcessingMode mode = consolidated.isSelected() ? ProcessingMode.CONSOLIDATED : ProcessingMode.FULL;
                return new RecordsNextPipeline().run(
                        cfg,
                        options,
                        mode,
                        new RecordsNextPipeline.Listener() {
                    @Override public void phase(String text, int percent) {
                        publish(text);
                        SwingUtilities.invokeLater(() -> {
                            phase.setText(text);
                            if (percent >= 0) progress.setValue(percent);
                        });
                    }
                    @Override public void timing(String text) { publish("TEMPO  " + text); }
                },
                        publicationMode
                );
            }

            @Override protected void process(java.util.List<String> chunks) {
                chunks.forEach(v -> log.append(v + System.lineSeparator()));
            }

            @Override protected void done() {
                try {
                    RecordsNextPipeline.Result result = get();
                    log.append("\nCompletato · file validi=" + result.files()
                            + " · pubblicati=" + result.published() + System.lineSeparator());
                    status.setText("Elaborazione completata");
                    status.setForeground(new Color(35, 105, 62));
                    progress.setValue(100);
                    phase.setText("Elaborazione completata");
                    refreshProcessingModeAvailability();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                    log.append("ERRORE: " + cause + System.lineSeparator());
                    status.setText("Errore");
                    status.setForeground(RED);
                    JOptionPane.showMessageDialog(frame, String.valueOf(cause), "RecordsNext 4.0 (sviluppo)", JOptionPane.ERROR_MESSAGE);
                } finally {
                    run.setEnabled(true);
                    publishAllSites.setEnabled(true);
                }
            }
        }.execute();
    }

    private void show() { frame.setVisible(true); }

    private static Map<String, String> labels(Object... values) {
        Map<String, String> result = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) result.put(String.valueOf(values[i]), String.valueOf(values[i + 1]));
        return Map.copyOf(result);
    }

    private static final Map<String, String> CLASSIC_LABELS = labels(
            "classics.highest-match-score", "Maggior punteggio in una partita",
            "classics.lowest-match-score", "Minor punteggio in una partita",
            "classics.most-regulation-goals", "Partita con più gol regolamentari",
            "classics.largest-regulation-margin", "Maggior scarto regolamentare",
            "classics.average-points", "Media punteggio",
            "classics.total-points", "Somma totale punti",
            "classics.standings-points", "Punti classifica",
            "classics.wins", "Vittorie",
            "classics.draws", "Pareggi",
            "classics.losses", "Sconfitte",
            "classics.goals-for", "Gol fatti",
            "classics.goals-against", "Gol subiti",
            "classics.yellow-cards-team", "Ammonizioni per squadra",
            "classics.red-cards-team", "Espulsioni per squadra",
            "classics.red-cards-player", "Espulsioni per giocatore",
            "classics.assists-team", "Assist per squadra",
            "classics.own-goals-team", "Autogol per squadra",
            "classics.penalties-scored", "Rigori segnati",
            "classics.penalties-missed", "Rigori sbagliati",
            "classics.penalties-saved", "Rigori parati",
            "classics.clean-sheets", "Clean sheet"
    );

    private static final Map<String, String> SERIES_LABELS = labels(
            "series.unbeaten", "Serie senza sconfitte",
            "series.winless", "Serie senza vittorie",
            "series.wins", "Vittorie consecutive",
            "series.draws", "Pareggi consecutivi",
            "series.losses", "Sconfitte consecutive",
            "series.clean-sheets", "Clean sheet consecutivi"
    );

    private static final Map<String, String> RU_LABELS = labels(
            "ru.max-in-match", "Maggior numero di RU in una partita",
            "ru.matches-with", "Partite con RU",
            "ru.matches-against", "Partite contro squadre con RU",
            "ru.deciding", "RU decisive",
            "ru.deciding-against", "RU decisive subite",
            "ru.balance", "Bilancio con RU",
            "ru.balance-against", "Bilancio contro RU",
            "ru.average-points", "Media punti con RU",
            "ru.average-points-against", "Media punti contro RU",
            "ru.role-distribution", "Distribuzione PU, DU, CU e AU"
    );

    private static final Map<String, String> THRESHOLD_LABELS = labels(
            "thresholds.surgical-win", "Vittoria chirurgica",
            "thresholds.mocking-loss", "Sconfitta beffa",
            "thresholds.miraculous-draw", "Pareggio miracolato",
            "thresholds.narrow-draw", "Pareggio stretto",
            "thresholds.missed-win-half-point", "Vittoria mancata per mezzo punto",
            "thresholds.loss-by-a-whisker", "Sconfitta per un pelo",
            "thresholds.exact-threshold", "Soglia precisa",
            "thresholds.just-enough", "Giusto giusto",
            "thresholds.wasted-points", "Spreco punti",
            "luck.favourable-events", "Eventi favorevoli",
            "luck.unfavourable-events", "Eventi sfavorevoli",
            "luck.balance", "Saldo fortuna-sfortuna"
    );
}
~~~~

## `src/main/java/it/alterlega/recordsnext/app/RecordsNextPipeline.java`

SHA-256: `6c90e10174831140a3fbd6a217293bb99d14cabd253144fbde5b703008324119`

~~~~java
package it.alterlega.recordsnext.app;

import it.alterlega.recordsnext.Records2026SitePublisher;
import it.alterlega.recordsnext.RiserveUfficioArchiveBuilder;
import it.alterlega.recordsnext.SeasonRecordsArchiveBuilder;
import it.alterlega.recordsnext.app.manifest.ManifestMetadata;
import it.alterlega.recordsnext.app.core.LeagueMetadata;
import it.alterlega.recordsnext.app.core.LeagueMetadataLoader;
import it.alterlega.recordsnext.app.model.RecordFamily;
import it.alterlega.recordsnext.app.topflop.TopFlopViewsPublication;
import it.alterlega.recordsnext.app.manifest.ManifestPublishingSupport;
import it.alterlega.recordsnext.app.core.CoreJsExporter;
import it.alterlega.recordsnext.app.output.SeasonPublicationTargetRepository;
import it.alterlega.recordsnext.app.output.SeasonFamilyShardPublisher;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Comparator;
import java.util.Set;

public final class RecordsNextPipeline {
    private static final Set<RecordFamily> IMPLEMENTED_FAMILIES = Set.copyOf(
            EnumSet.of(
                    RecordFamily.CLASSICS,
                    RecordFamily.SERIES,
                    RecordFamily.RU,
                    RecordFamily.MODIFIERS,
                    RecordFamily.THRESHOLDS_LUCK,
                    RecordFamily.TOP_FLOP
            )
    );

    public interface Listener {
        void phase(String text, int percent);

        default void timing(String text) {
            phase("TEMPO " + text, -1);
        }
    }

    public record Result(
            int classicEntries,
            int ruSeasons,
            int files,
            int published
    ) {
    }

    public enum PublicationMode {
        CURRENT_SITE,
        ALL_CONFIGURED_SITES
    }

    public Result run(
            PipelineConfig c,
            ProcessingOptions o,
            ProcessingMode mode,
            Listener l
    ) throws Exception {
        return run(c, o, mode, l, PublicationMode.CURRENT_SITE);
    }

    public Result run(
            PipelineConfig c,
            ProcessingOptions o,
            ProcessingMode mode,
            Listener l,
            PublicationMode publicationMode
    ) throws Exception {
        PipelinePreflight.Result preflight = preflight(o);

        l.phase(preflight.summary(), 2);

        for (String message : preflight.messages()) {
            l.phase("PREFLIGHT " + message, -1);
        }

        validateImplementedFamilies(o);

        long totalStarted = System.nanoTime();

        Path database = c.projectRoot()
                .resolve("data/database/recordsnext.db")
                .normalize();

        RecordsNextPreparationService preparation =
                new RecordsNextPreparationService(
                        c.projectRoot(),
                        database
                );

        long preparationStarted = System.nanoTime();

        List<String> changedSeasons =
                preparation.prepare(
                        mode,
                        c.seasons(),
                        l
                );

        l.timing(
                "preparazione complessiva: "
                        + elapsed(preparationStarted)
        );

        /*
         * Una stagione puo' risultare modificata/elaborata senza produrre
         * alcun season_normalized_*.json.
         *
         * Caso reale:
         * 2026_2027 e' correttamente la stagione corrente, ma prima della
         * prima partita non esiste ancora alcuna competizione testa-a-testa
         * elaborabile.
         *
         * In modalita' CONSOLIDATED questo NON deve essere considerato
         * un errore e gli archivi storici gia' consolidati non devono
         * essere ricostruiti.
         */
        List<String> reportSeasons = new ArrayList<>();

        for (String season : changedSeasons) {
            Path seasonReports = c.reports()
                    .resolve(season)
                    .normalize();

            if (!Files.isDirectory(seasonReports)) {
                continue;
            }

            try (var files = Files.list(seasonReports)) {
                boolean hasNormalized = files.anyMatch(path -> {
                    if (!Files.isRegularFile(path)) {
                        return false;
                    }

                    String name = path.getFileName().toString();

                    return name.startsWith("season_normalized_")
                            && name.endsWith(".json");
                });

                if (hasNormalized) {
                    reportSeasons.add(season);
                }
            }
        }

        if (mode == ProcessingMode.FULL) {
            l.phase(
                    "Pulizia archivi derivati delle stagioni gestite",
                    52
            );

            for (String season : changedSeasons) {
                deleteTree(
                        c.classicArchive().resolve(season)
                );

                deleteTree(
                        c.ruArchive().resolve(season)
                );
            }
        }

        if (o.familyEnabled(RecordFamily.CLASSICS)) {
            if (reportSeasons.isEmpty()) {
                l.phase(
                        "Record classici invariati: nessun nuovo report normalizzato",
                        55
                );
            } else {
                l.phase(
                        "Generazione record classici",
                        55
                );

                long started = System.nanoTime();

                SeasonRecordsArchiveBuilder.build(
                        c.reports(),
                        c.classicArchive(),
                        reportSeasons
                );

                l.timing(
                        "record classici: "
                                + elapsed(started)
                );
            }
        }

        if (o.familyEnabled(RecordFamily.RU)) {
            if (reportSeasons.isEmpty()) {
                l.phase(
                        "Riserve d'ufficio invariate: nessun nuovo report normalizzato",
                        68
                );
            } else {
                l.phase(
                        "Generazione riserve d'ufficio",
                        68
                );

                long started = System.nanoTime();

                RiserveUfficioArchiveBuilder.build(
                        c.reports(),
                        c.ruArchive(),
                        reportSeasons
                );

                l.timing(
                        "riserve d'ufficio: "
                                + elapsed(started)
                );
            }
        }

        Result result;
        Path topFlopScratch = null;
        TopFlopViewsPublication.Prepared topFlopPrepared = null;
        if (o.generateJs() && o.familyEnabled(RecordFamily.TOP_FLOP)) {
            Files.createDirectories(c.staging());
            topFlopScratch = Files.createTempDirectory(c.staging(), "recordsnext4-topflop-");
            try {
                l.phase("Elaborazione delle viste Top & Flop selezionate (SQLite locale)", 79);
                topFlopPrepared = TopFlopViewsPublication.prepare(
                        database, c.reports(), c.seasons(),
                        TopFlopViewsPublication.selectedIds(o.selection()), topFlopScratch
                );
                l.phase("Top & Flop: " + topFlopPrepared.files()
                        + " JS verificati, una sola elaborazione per tutte le destinazioni", -1);
            } catch (Exception ex) {
                deleteTree(topFlopScratch);
                throw ex;
            }
        }

        try {
        if (!o.generateJs()) {
            l.phase(
                    "Archivi elaborati; generazione JavaScript non richiesta",
                    96
            );

            result = new Result(
                    0,
                    0,
                    0,
                    0
            );
        } else {
            l.phase(
                    o.publish()
                            ? "Generazione e pubblicazione JavaScript"
                            : "Generazione JavaScript",
                    82
            );

            long started = System.nanoTime();

            LeagueMetadata leagueMetadata =
                    LeagueMetadataLoader.load(
                            c.projectRoot()
                                    .resolve("config/league.json")
                    );

            if (!o.publish()) {
                ManifestMetadata manifestMetadata =
                        new ManifestMetadata(
                                "RecordsNext by mauz79",
                                "3.2.0",
                                "2.0",
                                OffsetDateTime.now(),
                                leagueMetadata.leagueId(),
                                leagueMetadata.currentSeasonId(),
                                c.seasons(),
                                List.of()
                        );

                var r = Records2026SitePublisher.run(
                        c.classicArchive(),
                        c.ruArchive(),
                        c.staging(),
                        c.siteJs(),
                        true,
                        o.familyEnabled(RecordFamily.CLASSICS),
                        o.familyEnabled(RecordFamily.RU),
                        o,
                        preflight,
                        manifestMetadata,
                        database,
                        leagueMetadata,
                        c.reports()
                );

                int addedTopFlop = 0;
                if (topFlopPrepared != null) {
                    addedTopFlop = TopFlopViewsPublication.attachScoped(
                            topFlopPrepared, c.seasons(), r.stagingDirectory().resolve("js"));
                    ManifestPublishingSupport.write(r.stagingDirectory().resolve("js"),
                            o, preflight, manifestMetadata);
                    TopFlopViewsPublication.validateStaging(r.stagingDirectory().resolve("js"));
                    l.phase("Top & Flop integrato nello staging 3.2: " + addedTopFlop + " JS", -1);
                }
                result = new Result(
                        r.classicEntries(),
                        r.ruSeasons(),
                        r.validatedFiles() + addedTopFlop,
                        0
                );
            } else {
                SeasonPublicationTargetRepository targetRepository =
                        new SeasonPublicationTargetRepository(database);

                List<SeasonPublicationTargetRepository.Target> targets =
                        targetRepository.load(c.seasons());

                List<SeasonPublicationTargetRepository.Target> selectedTargets =
                        selectPublicationTargets(
                                targets,
                                leagueMetadata.currentSeasonId(),
                                publicationMode
                        );

                List<SeasonPublicationTargetRepository.Target> availableTargets =
                        selectedTargets.stream()
                                .filter(SeasonPublicationTargetRepository.Target::available)
                                .toList();

                for (SeasonPublicationTargetRepository.Target target : selectedTargets) {
                    if (!target.available()) {
                        l.phase(
                                "PUBBLICAZIONE saltata per "
                                        + target.seasonId()
                                        + ": sito locale non disponibile: "
                                        + target.siteRoot(),
                                -1
                        );
                    }
                }

                if (availableTargets.isEmpty()) {
                    if (publicationMode == PublicationMode.CURRENT_SITE) {
                        throw new IllegalStateException(
                                "Nessun sito locale configurato e disponibile per la stagione corrente "
                                        + leagueMetadata.currentSeasonId()
                                        + ". Configurare il sito della stagione oppure disattivare la pubblicazione."
                        );
                    }
                    throw new IllegalStateException(
                            "Nessun sito locale configurato e disponibile per la pubblicazione multisito."
                    );
                }

                int totalClassicEntries = 0;
                int totalRuSeasons = 0;
                int totalFiles = 0;
                int totalPublished = 0;

                Path scopesRoot = c.staging()
                        .resolve("multisite-scopes")
                        .normalize();

                deleteTree(scopesRoot);
                Files.createDirectories(scopesRoot);

                Path culometroConfig = c.projectRoot()
                        .resolve("config/culometro.json")
                        .normalize();

                if (o.culometroEnabled()) {
                    if (!Files.isRegularFile(culometroConfig)) {
                        throw new IllegalStateException(
                                "Configurazione Culometro non trovata: "
                                        + culometroConfig
                        );
                    }

                    Path scopedConfigDir = scopesRoot.resolve("config");
                    Files.createDirectories(scopedConfigDir);
                    Files.copy(
                            culometroConfig,
                            scopedConfigDir.resolve("culometro.json"),
                            StandardCopyOption.REPLACE_EXISTING,
                            StandardCopyOption.COPY_ATTRIBUTES
                    );
                }

                try {
                    for (SeasonPublicationTargetRepository.Target target : availableTargets) {
                        List<String> targetSeasons =
                                targetRepository.scope(
                                        c.seasons(),
                                        target
                                );

                        if (targetSeasons.isEmpty()) {
                            l.phase(
                                    "PUBBLICAZIONE saltata per "
                                            + target.seasonId()
                                            + ": nessuna stagione selezionata nello scope.",
                                    -1
                            );
                            continue;
                        }

                        l.phase(
                                "Pubblicazione sito "
                                        + target.seasonId()
                                        + " -> "
                                        + target.siteJs(),
                                84
                        );

                        Path targetScope = scopesRoot.resolve(target.seasonId());
                        Path scopedClassic = targetScope.resolve("classic");
                        Path scopedRu = targetScope.resolve("ru");
                        Path scopedReports = targetScope.resolve("reports");

                        createSeasonScopedView(
                                c.classicArchive(),
                                scopedClassic,
                                targetSeasons
                        );
                        createSeasonScopedView(
                                c.ruArchive(),
                                scopedRu,
                                targetSeasons
                        );
                        createSeasonScopedView(
                                c.reports(),
                                scopedReports,
                                targetSeasons
                        );

                        ManifestMetadata targetManifest =
                                new ManifestMetadata(
                                        "RecordsNext by mauz79",
                                        "3.2.0",
                                        "2.0",
                                        OffsetDateTime.now(),
                                        leagueMetadata.leagueId(),
                                        target.seasonId(),
                                        targetSeasons,
                                        List.of()
                                );

                        var generated = Records2026SitePublisher.run(
                                scopedClassic,
                                scopedRu,
                                c.staging(),
                                target.siteJs(),
                                true,
                                o.familyEnabled(RecordFamily.CLASSICS),
                                o.familyEnabled(RecordFamily.RU),
                                o,
                                preflight,
                                targetManifest,
                                null,
                                null,
                                scopedReports
                        );

                        Path generatedDir =
                                generated.stagingDirectory().resolve("js");

                        CoreJsExporter.export(
                                database,
                                generatedDir.resolve("fcmRecordsNext_Core.js"),
                                leagueMetadata.leagueId(),
                                leagueMetadata.leagueName(),
                                target.seasonId()
                        );

                        if (topFlopPrepared != null) {
                            int appended = TopFlopViewsPublication.attachScoped(
                                    topFlopPrepared, targetSeasons, generatedDir);
                            l.phase("Top & Flop " + target.seasonId()
                                    + ": " + appended + " JS nel cutoff storico", -1);
                        }

                        var flatPlan = SeasonFamilyShardPublisher.prepareFlat(generatedDir);
                        if (topFlopPrepared != null) {
                            // Il manifest deve elencare anche le viste e gli shard flat reali.
                            ManifestPublishingSupport.write(generatedDir, o, preflight, targetManifest);
                            TopFlopViewsPublication.validateStaging(generatedDir);
                        }
                        l.phase(
                                "Output flat " + target.seasonId()
                                        + ": shard=" + flatPlan.shards().size()
                                        + ", max=" + String.format(Locale.ROOT, "%.2f MiB",
                                                flatPlan.maxShardBytes() / (1024.0 * 1024.0)),
                                -1
                        );

                        int published = publishGeneratedDirectory(
                                generatedDir,
                                target.siteJs()
                        );

                        totalClassicEntries += generated.classicEntries();
                        totalRuSeasons += generated.ruSeasons();
                        totalFiles += published;
                        totalPublished += published;
                    }
                } finally {
                    deleteTree(scopesRoot);
                }

                result = new Result(
                        totalClassicEntries,
                        totalRuSeasons,
                        totalFiles,
                        totalPublished
                );
            }

            l.timing(
                    (
                            o.publish()
                                    ? "generazione e pubblicazione JavaScript multisito: "
                                    : "generazione JavaScript: "
                    )
                            + elapsed(started)
            );
        }

        } finally {
            if (topFlopScratch != null) deleteTree(topFlopScratch);
        }

        preparation.saveConsolidation(
                c.seasons()
        );

        l.timing(
                "totale elaborazione: "
                        + elapsed(totalStarted)
        );

        l.phase(
                "Elaborazione completata e consolidamento aggiornato",
                100
        );

        return result;
    }

    public PipelinePreflight.Result preflight(
            ProcessingOptions options
    ) {
        return PipelinePreflight.evaluate(options);
    }

    public boolean hasConsolidation(
            PipelineConfig c
    ) {
        Path database = c.projectRoot()
                .resolve("data/database/recordsnext.db")
                .normalize();

        return new RecordsNextPreparationService(
                c.projectRoot(),
                database
        ).hasConsolidation();
    }

    static void validateImplementedFamilies(
            ProcessingOptions options
    ) {
        Set<RecordFamily> unsupported =
                EnumSet.copyOf(
                        options.selection().enabledFamilies()
                );

        unsupported.removeAll(
                IMPLEMENTED_FAMILIES
        );

        if (!unsupported.isEmpty()) {
            throw new IllegalArgumentException(
                    "Famiglie selezionate ma non ancora collegate alla pipeline 2.0: "
                            + unsupported
            );
        }
    }

    static List<SeasonPublicationTargetRepository.Target> selectPublicationTargets(
            List<SeasonPublicationTargetRepository.Target> targets,
            String currentSeasonId,
            PublicationMode mode) {

        if (mode == PublicationMode.ALL_CONFIGURED_SITES) {
            return List.copyOf(targets);
        }

        return targets.stream()
                .filter(target -> target.seasonId().equals(currentSeasonId))
                .toList();
    }

    private static void createSeasonScopedView(
            Path sourceRoot,
            Path targetRoot,
            List<String> seasons) throws Exception {

        Files.createDirectories(targetRoot);

        for (String season : seasons) {
            Path source = sourceRoot.resolve(season).normalize();
            if (!Files.isDirectory(source)) {
                continue;
            }

            copyTree(source, targetRoot.resolve(season));
        }
    }

    private static void copyTree(
            Path source,
            Path target) throws Exception {

        try (var paths = Files.walk(source)) {
            for (Path path : paths.toList()) {
                Path relative = source.relativize(path);
                Path destination = target.resolve(relative);

                if (Files.isDirectory(path)) {
                    Files.createDirectories(destination);
                } else {
                    Files.createDirectories(destination.getParent());
                    Files.copy(
                            path,
                            destination,
                            StandardCopyOption.REPLACE_EXISTING,
                            StandardCopyOption.COPY_ATTRIBUTES
                    );
                }
            }
        }
    }

    static int publishGeneratedDirectory(
            Path generatedDir,
            Path siteJsDir) throws Exception {

        Files.createDirectories(siteJsDir);

        List<Path> files;
        try (var stream = Files.list(generatedDir)) {
            files = stream
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(
                            path -> path.getFileName().toString()
                    ))
                    .toList();
        }

        Set<String> generatedNames = files.stream()
                .map(path -> path.getFileName().toString())
                .collect(java.util.stream.Collectors.toSet());

        List<Path> staleFlatShards;
        try (var stream = Files.list(siteJsDir)) {
            staleFlatShards = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> SeasonFamilyShardPublisher.isFlatShardFileName(
                            path.getFileName().toString())
                            || path.getFileName().toString().equals(
                                    "fcmRecordsNext_TopFlopViews.js")
                            || path.getFileName().toString().matches(
                                    "fcmRecordsNext_TopFlopViews\\.(?:Careers|Teams|Records|Highlights)\\."
                                    + "[0-9]{4}_[0-9]{4}\\.[0-9]{3}\\.js"))
                    .filter(path -> !generatedNames.contains(path.getFileName().toString()))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .toList();
        }

        Path backupRoot = generatedDir.getParent()
                .resolve("multisite-publish-backup");
        deleteTree(backupRoot);
        Files.createDirectories(backupRoot);

        List<Path> replaced = new ArrayList<>();
        List<Path> created = new ArrayList<>();
        List<Path> removedStale = new ArrayList<>();

        try {
            for (Path stale : staleFlatShards) {
                Path backup = backupRoot.resolve(stale.getFileName());
                Files.copy(
                        stale,
                        backup,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.COPY_ATTRIBUTES
                );
                Files.delete(stale);
                removedStale.add(stale);
            }

            for (Path source : files) {
                Path target = siteJsDir.resolve(source.getFileName());

                if (Files.exists(target)) {
                    Files.copy(
                            target,
                            backupRoot.resolve(source.getFileName()),
                            StandardCopyOption.REPLACE_EXISTING,
                            StandardCopyOption.COPY_ATTRIBUTES
                    );
                    replaced.add(target);
                } else {
                    created.add(target);
                }

                Path temp = siteJsDir.resolve(
                        "." + source.getFileName()
                                + ".recordsnext-multisite.tmp"
                );

                Files.copy(
                        source,
                        temp,
                        StandardCopyOption.REPLACE_EXISTING
                );

                try {
                    Files.move(
                            temp,
                            target,
                            StandardCopyOption.ATOMIC_MOVE,
                            StandardCopyOption.REPLACE_EXISTING
                    );
                } catch (java.nio.file.AtomicMoveNotSupportedException ex) {
                    Files.move(
                            temp,
                            target,
                            StandardCopyOption.REPLACE_EXISTING
                    );
                }
            }
        } catch (Exception ex) {
            for (Path target : created) {
                Files.deleteIfExists(target);
            }

            for (Path target : replaced) {
                Path backup = backupRoot.resolve(target.getFileName());
                if (Files.isRegularFile(backup)) {
                    Files.copy(
                            backup,
                            target,
                            StandardCopyOption.REPLACE_EXISTING
                    );
                }
            }

            for (Path target : removedStale) {
                Path backup = backupRoot.resolve(target.getFileName());
                if (Files.isRegularFile(backup)) {
                    Files.copy(
                            backup,
                            target,
                            StandardCopyOption.REPLACE_EXISTING
                    );
                }
            }

            throw ex;
        }

        return files.size();
    }

    private static void deleteTree(
            Path root
    ) throws Exception {
        if (!Files.exists(root)) {
            return;
        }

        try (var paths = Files.walk(root)) {
            for (Path path :
                    paths.sorted(
                            java.util.Comparator.reverseOrder()
                    ).toList()) {

                Files.deleteIfExists(path);
            }
        }
    }

    private static String elapsed(
            long started
    ) {
        double seconds =
                (System.nanoTime() - started)
                        / 1_000_000_000.0;

        return String.format(
                Locale.ROOT,
                "%.3f s",
                seconds
        );
    }
}
~~~~

## `src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopPublicViewsStageExporter.java`

SHA-256: `1bb3e1ba8d8a91853f8c8e86f0db7bf7a91136237585c5b92cd9fe070af865ab`

~~~~java
package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.topflop.TopFlopFacts.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * Approved public VIEWS, not the full FCA/FCM facts dump. Stage-only.
 * All historical facts remain in local SQLite. No scorecard URL is synthesized.
 * No publisher, manifest, CSS or site is modified by this class.
 *
 * Averages (TF05/TF06), wasted votes (TF11-13), and baseline-derived
 * rankings (TF14-15) are NOT promoted to definitive rankings while their
 * policy/evidence is incomplete. Their available raw aggregates are exposed.
 */
public final class TopFlopPublicViewsStageExporter {
    private TopFlopPublicViewsStageExporter() {}
    public static final long LIMIT_BYTES = 1_500_000;
    public static final long TARGET_BYTES = 950_000;
    public static final String FACADE = "fcmRecordsNext_TopFlopViews.js";
    private static final MathContext PRECISION = MathContext.DECIMAL64;
    private static final List<String> EVENTS = List.of(
        "GOLFATTI1", "GOLFATTISURIGORE1", "ASSIST", "GOLSUBITI",
        "GOLSUBITISURIGORE", "RIGPAR", "RIGSBA", "AUTOGOL1", "AMM", "ESP");

    public record FileInfo(String name, String season, String kind, int rows, long bytes, String sha256) {}
    public record Result(List<FileInfo> files, int seasons, int players,
                         long sourceRealDays, long sourceFantasyContexts, Path output) {}

    static final class Totals {
        int validVotes, counted, countedRealValid, observed, benchUnused, outsideList, unknown;
        int keeperValidConcededDays, keeperZeroConcededDays;
        BigDecimal votes = BigDecimal.ZERO, fantasy = BigDecimal.ZERO;
        BigDecimal countedRealVotes = BigDecimal.ZERO;
        BigDecimal keeperConcededOnValidDays = BigDecimal.ZERO;
        final Map<String,BigDecimal> events = new LinkedHashMap<>();
        void event(Map<String,BigDecimal> raw) {
            for (String key : EVENTS) {
                BigDecimal amount = raw.get(key);
                if (amount != null && amount.signum() != 0) {
                    events.merge(key, amount, BigDecimal::add);
                }
            }
        }
        Map<String,Object> realView() {
            return obj("votiValidi",validVotes,"sommaVoti",votes,
                "mediaVoto",mean(votes,validVotes),"eventi",events,
                "giornateGolSubitiConVoto",keeperValidConcededDays,
                "giornateZeroGolSubitiRegistrati",keeperZeroConcededDays,
                "mediaGolSubitiSuGiornateVerificate",
                    mean(keeperConcededOnValidDays,keeperValidConcededDays));
        }
        Map<String,Object> fantasyView() {
            return obj("contestiOsservati",observed,"conteggiati",counted,
                "conVotoRealeValido",countedRealValid,"sommaVotiRealiApportati",countedRealVotes,
                "mediaVotoRealeApportato",mean(countedRealVotes,countedRealValid),
                "sommaFantavotiApportati",fantasy,
                "fantamediaApportata",mean(fantasy,counted),
                "panchinaNonConteggiata",benchUnused,"fuoriLista",outsideList,
                "utilizzoDaVerificare",unknown,"eventiApportati",events);
        }
    }
    static final class Player {
        final TopFlopFcaJdbcReader.PlayerProfile profile;
        final Totals real = new Totals(), fantasy = new Totals();
        Player(TopFlopFcaJdbcReader.PlayerProfile profile) {this.profile=profile;}
        String id() {return profile.player().seasonId()+":"+profile.player().fcaPlayerId();}
        int role() {
            try {return Integer.parseInt(profile.originalFields().get("GiocatoreA.RUOLO"));}
            catch (RuntimeException ex) {return -1;}
        }
        Map<String,Object> label() {
            return obj("playerKey",id(),"nome",profile.name(),
                "codiceStorico",profile.player().gazzaCode(),"ruoloFca",role());
        }
    }
    record TeamKey(int playerId,int teamId,int competitionId) {}
    static final class Team {
        final Player player;
        final int teamId, competitionId;
        final String teamName, competitionName, family;
        final Totals totals = new Totals();
        Team(Player player,FantasyFact fact) {
            this.player=player;
            teamId=fact.key().fantasyTeamId();competitionId=fact.key().sourceCompetitionId();
            teamName=fact.sourceFields().getOrDefault("FCM.Fantasquadra.Nome", "");
            competitionName=fact.competitionName();family=fact.family().name();
        }
        Map<String,Object> view() {
            Map<String,Object> x=new LinkedHashMap<>(player.label());
            x.putAll(obj("fantasquadra",teamName,"idSquadraInterno",teamId,
                "competizione",competitionName,"idCompetizioneInterno",competitionId,
                "famiglia",family,"apporto",totals.fantasyView()));
            return x;
        }
    }
    static final class Winner {
        final String id, group, metric, direction;
        BigDecimal value;
        final List<Map<String,Object>> entries = new ArrayList<>();
        Winner(String id,String group,String metric,String direction) {
            this.id=id;this.group=group;this.metric=metric;this.direction=direction;
        }
        void offer(BigDecimal candidate,Map<String,Object> entry) {
            if (candidate == null) return;
            int compare=value==null ? 1 : candidate.compareTo(value);
            boolean better=value==null || (direction.equals("MAX") && compare>0) ||
                (direction.equals("MIN") && compare<0);
            if (better) {value=candidate;entries.clear();}
            if (better || candidate.compareTo(value)==0) entries.add(entry);
        }
        Map<String,Object> view() {
            return obj("recordId",id,"ambito",group,"metrica",metric,
                "ordine",direction,"valore",value,"pariMerito",entries.size(),"risultati",entries);
        }
    }
    static final class SeasonView {
        final List<Map<String,Object>> careers = new ArrayList<>(), teams = new ArrayList<>(),
            records = new ArrayList<>(), highlights = new ArrayList<>();
        final Map<String,Object> counters = new LinkedHashMap<>();
    }

    /** Exposed for synthetic, source-independent regression tests. */
    static SeasonView build(TopFlopJdbcArchiveAssembler.LoadedSeason loaded,
                     Map<TopFlopCanonicalScorecards.MatchTeam,String> links) {
        Objects.requireNonNull(loaded);Objects.requireNonNull(links);
        TopFlopArchive archive=loaded.archive();
        Map<Integer,Player> players=new TreeMap<>();
        for (var profile:loaded.profiles().values()) {
            Player old=players.putIfAbsent(profile.player().fcaPlayerId(),new Player(profile));
            if(old!=null)throw new IllegalStateException("Duplicate player profile");
        }
        Map<String,Winner> winners=new LinkedHashMap<>();
        Map<String,Map<String,Object>> highlights=new LinkedHashMap<>();
        int valid=0,counted=0;
        for (RealDay day:archive.allRealDays()) {
            Player p=need(players,day.key().player().fcaPlayerId());
            boolean validVote=day.voteState()==VoteState.VALID;
            if(validVote) {
                valid++;
                p.real.validVotes++;
                p.real.votes=p.real.votes.add(day.vote());
                String hk=realHighlight(day,p);
                offer(winners,"TF01","realeSingolaGiornata","votoReale","MAX",day.vote(),
                    obj("calciatore",p.label(),"giornataSerieA",day.key().serieARound(),"highlightKey",hk));
                offer(winners,"TF02","realeSingolaGiornata","votoReale","MIN",day.vote(),
                    obj("calciatore",p.label(),"giornataSerieA",day.key().serieARound(),"highlightKey",hk));
            }
            p.real.event(day.events());
            if(p.role()==1 && validVote && day.events().containsKey("GOLSUBITI")) {
                p.real.keeperValidConcededDays++;
                BigDecimal conceded=day.events().get("GOLSUBITI");
                p.real.keeperConcededOnValidDays=p.real.keeperConcededOnValidDays.add(conceded);
                if(conceded.signum()==0)p.real.keeperZeroConcededDays++;
                if(conceded.signum()>0)offer(winners,"TF18","portieriSingolaGiornata","golSubiti","MAX",conceded,
                    obj("calciatore",p.label(),"giornataSerieA",day.key().serieARound(),
                        "highlightKey",hkReal(day,p,highlights)));
            }
            if(p.role()==1) {
                BigDecimal saved=event(day.events(),"RIGPAR");
                if(saved.signum()>0) offer(winners,"TF17","portieriSingolaGiornata","rigoriParati","MAX",saved,
                    obj("calciatore",p.label(),"giornataSerieA",day.key().serieARound(),
                        "highlightKey",hkReal(day,p,highlights)));
                BigDecimal goals=event(day.events(),"GOLFATTI1");
                if(goals.signum()>0) {
                    // Only an actual source goal, NOT goals + penalty goals.
                    hkReal(day,p,highlights);
                }
            }
        }
        Map<TeamKey,Team> teams=new LinkedHashMap<>();
        for (FantasyFact fact:archive.allFantasyFacts()) {
            Player p=need(players,fact.key().player().fcaPlayerId());
            TeamKey key=new TeamKey(fact.key().player().fcaPlayerId(),
                fact.key().fantasyTeamId(),fact.key().sourceCompetitionId());
            Team t=teams.computeIfAbsent(key, ignored->new Team(p,fact));
            addUsage(p.fantasy,fact);addUsage(t.totals,fact);
            if(fact.usage()!=Usage.COUNTED)continue;
            RealDay realForFact=archive.realDay(fact.realDayKey());
            if(realForFact.voteState()==VoteState.VALID) {
                p.fantasy.countedRealValid++;
                p.fantasy.countedRealVotes=p.fantasy.countedRealVotes.add(realForFact.vote());
                t.totals.countedRealValid++;
                t.totals.countedRealVotes=t.totals.countedRealVotes.add(realForFact.vote());
            }
            counted++;
            String hk=highlightKey(fact);
            Map<String,Object> entry=obj("calciatore",p.label(),
                "fantasquadra",t.teamName,"competizione",t.competitionName,
                "famiglia",t.family,"giornataCompetizione",fact.fantasyRound(),
                "giornataSerieA",fact.realDayKey().serieARound(),"highlightKey",hk);
            offer(winners,"TF03","fantasySingoloIncontro","fantavotoApportato","MAX",fact.fantasyVote(),entry);
            offer(winners,"TF04","fantasySingoloIncontro","fantavotoApportato","MIN",fact.fantasyVote(),entry);
        }
        // A highlight is published ONLY if an approved record points to it;
        // notable goalkeeper goals are an explicitly approved TF20 exception.
        Set<String> selected=new LinkedHashSet<>();
        for(Winner w:winners.values())for(var e:w.entries){
            Object h=e.get("highlightKey");if(h!=null)selected.add(h.toString());
        }
        for(RealDay day:archive.allRealDays()) {
            Player p=need(players,day.key().player().fcaPlayerId());
            if(p.role()==1 && (event(day.events(),"GOLFATTI1").signum()>0 ||
                event(day.events(),"ASSIST").signum()>0))
                selected.add(realHighlight(day,p));
        }
        // If a REAL record day also appears in a fantasy fixture, expose its
        // actual FCM context and canonical tabellino (never a fabricated link).
        Map<String,List<Map<String,Object>>> realLinks=new LinkedHashMap<>();
        for(FantasyFact fact:archive.allFantasyFacts()) {
            RealDay realDay=archive.realDay(fact.realDayKey());
            Player p=need(players,fact.key().player().fcaPlayerId());
            String realKey=realHighlight(realDay,p);
            if(!selected.contains(realKey))continue;
            String url=links.get(new TopFlopCanonicalScorecards.MatchTeam(
                fact.key().matchId(),fact.key().fantasyTeamId()));
            realLinks.computeIfAbsent(realKey,ignored->new ArrayList<>()).add(obj(
                "fantasquadra",fact.sourceFields().get("FCM.Fantasquadra.Nome"),
                "competizione",fact.competitionName(),"famiglia",fact.family().name(),
                "giornataCompetizione",fact.fantasyRound(),
                "descrizioneGiornata",clean(fact.sourceFields().get("FCM.Giornata.Desc")),
                "giornataSerieA",fact.realDayKey().serieARound(),
                "schieramento",lineup(fact),"conteggiato",fact.usage()==Usage.COUNTED,
                "fantavoto",fact.usage()==Usage.COUNTED?fact.fantasyVote():null,
                "urlTabellino",url));
        }
        // Resolve only the selected fantasy encounters, never copy all canonical links.
        for(FantasyFact fact:archive.allFantasyFacts()) {
            String key=highlightKey(fact);
            if(!selected.contains(key))continue;
            Player p=need(players,fact.key().player().fcaPlayerId());
            String url=links.get(new TopFlopCanonicalScorecards.MatchTeam(fact.key().matchId(),
                fact.key().fantasyTeamId()));
            RealDay real=archive.realDay(fact.realDayKey());
            highlights.putIfAbsent(key,obj("key",key,"tipo","fantasyConteggiato",
                "calciatore",p.label(),"votoReale",real.vote(),"fantavoto",fact.fantasyVote(),
                "votoTabellino",numericToken(fact.sourceFields().get("FCM.Tabellino.Voto")),
                "eventi",publicEvents(fact.fantasyEvents()),
                "fantasquadra",fact.sourceFields().get("FCM.Fantasquadra.Nome"),
                "competizione",fact.competitionName(),"famiglia",fact.family().name(),
                "giornataCompetizione",fact.fantasyRound(),
                "descrizioneGiornata",clean(fact.sourceFields().get("FCM.Giornata.Desc")),
                "giornataSerieA",fact.realDayKey().serieARound(),
                "schieramento",lineup(fact),"conteggiato",true,
                "urlTabellino",url));
        }
        for(RealDay day:archive.allRealDays()) {
            Player p=need(players,day.key().player().fcaPlayerId());
            String key=realHighlight(day,p);
            if(selected.contains(key)) highlights.putIfAbsent(key,obj(
                "key",key,"tipo","prestazioneReale","calciatore",p.label(),
                "giornataSerieA",day.key().serieARound(),"votoReale",day.vote(),
                "eventi",publicEvents(day.events()),"urlTabellino",null,
                "contestiFantasy",realLinks.getOrDefault(key,List.of())));
        }
        SeasonView out=new SeasonView();
        for(Player p:players.values()) {
            Map<String,Object> career=new LinkedHashMap<>(p.label());
            career.put("reale",p.real.realView());career.put("fantasy",p.fantasy.fantasyView());
            career.put("identitaStoricaVerificata",false); // no auto-merge of CodiceGazza
            out.careers.add(career);
            Map<String,Object> base=obj("calciatore",p.label());
            offerPositive(winners,"TF07","golRealiStagione","gol",event(p.real.events,"GOLFATTI1"),base);
            offerPositive(winners,"TF07","golApportatiContestiFantasy","gol",event(p.fantasy.events,"GOLFATTI1"),base);
            offerPositive(winners,"TF08","assistRealiStagione","assist",event(p.real.events,"ASSIST"),base);
            offerPositive(winners,"TF08","assistApportatiContestiFantasy","assist",event(p.fantasy.events,"ASSIST"),base);
            for(String negative:List.of("AMM","ESP","AUTOGOL1","RIGSBA")) {
                offerPositive(winners,"TF09","eventiNegativiRealiStagione",negative,event(p.real.events,negative),base);
                offerPositive(winners,"TF09","eventiNegativiApportati",negative,event(p.fantasy.events,negative),base);
            }
            offerPositive(winners,"TF10","giornateRealiConVoto","n",BigDecimal.valueOf(p.real.validVotes),base);
            offerPositive(winners,"TF10","incontriFantasyConteggiati","n",BigDecimal.valueOf(p.fantasy.counted),base);
            if(p.role()==1) {
                offerPositive(winners,"TF17","portieriRigoriParatiStagione","RIGPAR",event(p.real.events,"RIGPAR"),base);
                offerPositive(winners,"TF17","portieriRigoriParatiApportati","RIGPAR",event(p.fantasy.events,"RIGPAR"),base);
                offerPositive(winners,"TF18","portieriGolSubitiStagione","GOLSUBITI",event(p.real.events,"GOLSUBITI"),base);
                offerPositive(winners,"TF18","portieriGolSubitiApportati","GOLSUBITI",event(p.fantasy.events,"GOLSUBITI"),base);
                offerPositive(winners,"TF19","portieriGiornateZeroGolSubitiRegistrati","n",
                    BigDecimal.valueOf(p.real.keeperZeroConcededDays),base);
                offerPositive(winners,"TF20","portieriGolStagione","GOLFATTI1",event(p.real.events,"GOLFATTI1"),base);
                offerPositive(winners,"TF20","portieriGolSuRigoreStagione","GOLFATTISURIGORE1",event(p.real.events,"GOLFATTISURIGORE1"),base);
                offerPositive(winners,"TF20","portieriAssistStagione","ASSIST",event(p.real.events,"ASSIST"),base);
                offerPositive(winners,"TF20","portieriGolApportati","GOLFATTI1",event(p.fantasy.events,"GOLFATTI1"),base);
                offerPositive(winners,"TF20","portieriGolSuRigoreApportati","GOLFATTISURIGORE1",event(p.fantasy.events,"GOLFATTISURIGORE1"),base);
                offerPositive(winners,"TF20","portieriAssistApportati","ASSIST",event(p.fantasy.events,"ASSIST"),base);
            }
        }
        teams.values().stream().sorted(Comparator.comparing((Team t)->t.player.id())
            .thenComparingInt(t->t.teamId).thenComparingInt(t->t.competitionId))
            .map(Team::view).forEach(out.teams::add);
        for(Winner w:winners.values()) if(w.value!=null)out.records.add(w.view());
        out.highlights.addAll(highlights.values());
        out.counters.putAll(obj("players",players.size(),"realDays",archive.realDayCount(),
            "validRealVotes",valid,"fantasyContexts",archive.fantasyFactCount(),
            "fantasyCounted",counted,"teamCompetitionAggregates",teams.size(),
            "selectedHighlights",out.highlights.size(),"youthExcluded",loaded.fcm().youthExcluded()));
        return out;
    }

    private static void addUsage(Totals a,FantasyFact f) {
        a.observed++;
        if(f.usage()!=Usage.COUNTED) {
            switch(f.usage()){
                case BENCH_UNUSED -> a.benchUnused++;
                case OUTSIDE_LIST -> a.outsideList++;
                default -> a.unknown++;
            }
            return;
        }
        if(f.fantasyVote()==null) throw new IllegalStateException("COUNTED without FCM total");
        a.counted++;a.fantasy=a.fantasy.add(f.fantasyVote());
        // The assembler credits source FCA events ONLY on COUNTED contexts.
        a.event(f.fantasyEvents());
    }
    private static String lineup(FantasyFact fact) {
        if(fact.usage()==Usage.BENCH_UNUSED)return "RISERVA_NON_ENTRATA";
        if(fact.usage()==Usage.OUTSIDE_LIST)return "FUORI_LISTA";
        if(fact.usage()!=Usage.COUNTED)return "SCHIERAMENTO_DA_VERIFICARE";
        if(fact.initialStarter())return "TITOLARE";
        if("1".equals(fact.sourceFields().get("FCM.Formazione.EntratoRiserva")))return "RISERVA_ENTRATA";
        return "CONTEGGIATO_SCHIERAMENTO_DA_VERIFICARE";
    }
    private static BigDecimal numericToken(String source) {
        if(source==null || !source.matches("[+-]?[0-9]+(?:[,.][0-9]+)?"))return null;
        return new BigDecimal(source.replace(',','.'));
    }
    private static String clean(String raw) {
        if(raw==null || raw.equals("__SQL_NULL__"))return null;
        return raw;
    }
    private static void offerPositive(Map<String,Winner> w,String id,String group,String metric,
                                     BigDecimal amount,Map<String,Object> entry) {
        if(amount.signum()>0) offer(w,id,group,metric,"MAX",amount,entry);
    }
    private static void offer(Map<String,Winner> w,String id,String group,String metric,
                              String order,BigDecimal value,Map<String,Object> entry) {
        String key=id+":"+group+":"+metric+":"+order;
        w.computeIfAbsent(key,ignored->new Winner(id,group,metric,order)).offer(value,entry);
    }
    static BigDecimal event(Map<String,BigDecimal> events,String name) {
        return events.getOrDefault(name,BigDecimal.ZERO);
    }
    static Map<String,BigDecimal> publicEvents(Map<String,BigDecimal> input) {
        Map<String,BigDecimal> m=new LinkedHashMap<>();
        for(String key:EVENTS) {
            BigDecimal v=input.get(key);
            if(v!=null && v.signum()!=0) m.put(key,v);
        }
        return m;
    }
    private static String realHighlight(RealDay day,Player p) {
        return "R:"+p.id()+":"+day.key().serieARound();
    }
    private static String hkReal(RealDay day,Player p,Map<String,Map<String,Object>> highlights) {
        return realHighlight(day,p);
    }
    private static String highlightKey(FantasyFact fact) {
        return "F:"+fact.key().player().seasonId()+":"+fact.key().player().fcaPlayerId()+
            ":"+fact.key().matchId()+":"+fact.key().fantasyTeamId();
    }
    private static Player need(Map<Integer,Player> players,int id) {
        Player p=players.get(id);if(p==null)throw new IllegalStateException("Missing profile: "+id);return p;
    }
    private static BigDecimal mean(BigDecimal value,int count) {
        return count==0?null:value.divide(BigDecimal.valueOf(count),PRECISION);
    }
    private static Map<String,Object> obj(Object... args) {
        if(args.length%2!=0)throw new IllegalArgumentException("odd map args");
        Map<String,Object> m=new LinkedHashMap<>();
        for(int i=0;i<args.length;i+=2)m.put((String)args[i],args[i+1]);
        return m;
    }
    private static String json(Object value) {return TopFlopCompactStageJsExporter.Json.write(value);}

    /** Reversible positional encoding: only public VIEWS, never source match histories. */
    private static List<Object> packEvents(Object raw) {
        @SuppressWarnings("unchecked") Map<String,BigDecimal> events=(Map<String,BigDecimal>)raw;
        List<Object> compact=new ArrayList<>();
        if(events==null)return compact;
        for(int i=0;i<EVENTS.size();i++) {
            BigDecimal v=events.get(EVENTS.get(i));
            if(v!=null && v.signum()!=0) {compact.add(i);compact.add(v);}
        }
        return compact;
    }
    @SuppressWarnings("unchecked")
    private static List<Object> packCareer(Map<String,Object> row) {
        Map<String,Object> r=(Map<String,Object>)row.get("reale");
        Map<String,Object> f=(Map<String,Object>)row.get("fantasy");
        return List.of(row.get("playerKey"), row.get("nome"),row.get("ruoloFca"),
            // null-safe numeric fields: List.of() forbids nulls.
            row.get("codiceStorico") == null ? 0 : row.get("codiceStorico"),
            List.of(r.get("votiValidi"),r.get("sommaVoti"),packEvents(r.get("eventi")),
                r.get("giornateGolSubitiConVoto"),r.get("giornateZeroGolSubitiRegistrati"),
                r.get("mediaGolSubitiSuGiornateVerificate") == null ? "" : r.get("mediaGolSubitiSuGiornateVerificate")),
            List.of(f.get("contestiOsservati"),f.get("conteggiati"),f.get("conVotoRealeValido"),
                f.get("sommaVotiRealiApportati"),f.get("sommaFantavotiApportati"),
                f.get("panchinaNonConteggiata"),f.get("fuoriLista"),f.get("utilizzoDaVerificare"),
                packEvents(f.get("eventiApportati"))));
    }
    @SuppressWarnings("unchecked")
    private static List<Object> packTeam(Map<String,Object> row) {
        Map<String,Object> f=(Map<String,Object>)row.get("apporto");
        return List.of(row.get("playerKey"), row.get("idSquadraInterno"),
            row.get("idCompetizioneInterno"),
            List.of(f.get("contestiOsservati"),f.get("conteggiati"),f.get("conVotoRealeValido"),
                f.get("sommaVotiRealiApportati"),f.get("sommaFantavotiApportati"),
                f.get("panchinaNonConteggiata"),f.get("fuoriLista"),f.get("utilizzoDaVerificare"),
                packEvents(f.get("eventiApportati"))));
    }
    private static List<Object> packs(String kind,List<Map<String,Object>> rows) {
        if(kind.equals("Careers"))return rows.stream().map(x->(Object)packCareer(x)).toList();
        if(kind.equals("Teams"))return rows.stream().map(x->(Object)packTeam(x)).toList();
        return new ArrayList<>(rows);
    }
    private static Map<String,Object> dictionaries(SeasonView view) throws IOException {
        Map<Integer,String> teams=new TreeMap<>();
        Map<Integer,List<String>> competitions=new TreeMap<>();
        for(var row:view.teams) {
            int t=((Number)row.get("idSquadraInterno")).intValue();
            int comp=((Number)row.get("idCompetizioneInterno")).intValue();
            String tn=(String)row.get("fantasquadra");
            List<String> cn=List.of((String)row.get("competizione"),(String)row.get("famiglia"));
            String old=teams.putIfAbsent(t,tn);
            if(old!=null && !old.equals(tn))throw new IOException("Conflicting team ID "+t);
            List<String> oc=competitions.putIfAbsent(comp,cn);
            if(oc!=null && !oc.equals(cn))throw new IOException("Conflicting competition ID "+comp);
        }
        return obj("squadre",teams.entrySet().stream().map(e->List.of(e.getKey(),e.getValue())).toList(),
            "competizioni",competitions.entrySet().stream().map(e->List.of(e.getKey(),e.getValue().get(0),
                e.getValue().get(1))).toList());
    }

    public static Result export(Connection c,List<String> seasonIds,Path output,Path reports)
            throws SQLException,IOException {
        return export(c, seasonIds, output, reports, Set.of());
    }

    /**
     * selectedIds: catalogo TF01..TF20. In questo overload un insieme vuoto
     * conserva il comportamento v1 per la diagnostica; la pipeline risolve e
     * valida sempre una selezione esplicita non vuota.
     */
    public static Result export(Connection c,List<String> seasonIds,Path output,Path reports,
                                Set<String> selectedIds) throws SQLException,IOException {
        Objects.requireNonNull(c);Objects.requireNonNull(seasonIds);Objects.requireNonNull(output);
        Objects.requireNonNull(selectedIds);
        for (String id : selectedIds) if (!id.matches("TF(?:0[1-9]|1[0-9]|20)"))
            throw new IllegalArgumentException("Identificativo record sconosciuto: " + id);
        if(seasonIds.isEmpty() || seasonIds.stream().distinct().count()!=seasonIds.size())
            throw new IllegalArgumentException("Empty or duplicate season list");
        for(String s:seasonIds)if(s==null || !s.matches("[0-9]{4}_[0-9]{4}"))
            throw new IllegalArgumentException("Invalid season: "+s);
        if(Files.exists(output))throw new IOException("Staging must be a NEW directory: "+output);
        Files.createDirectories(output);
        List<FileInfo> files=new ArrayList<>();
        List<Map<String,Object>> metadata=new ArrayList<>();
        int players=0;long real=0,fantasy=0;
        for(String season:seasonIds) {
            var loaded=TopFlopJdbcArchiveAssembler.loadSeason(c,season);
            var canonical=reports==null ? Map.<TopFlopCanonicalScorecards.MatchTeam,String>of()
                :TopFlopCanonicalScorecards.load(reports,season);
            SeasonView view=build(loaded,canonical);
            if (!selectedIds.isEmpty()) filterSelected(view, selectedIds);
            players+=view.careers.size();real+=loaded.archive().realDayCount();
            fantasy+=loaded.archive().fantasyFactCount();
            List<FileInfo> seasonFiles=new ArrayList<>();
            seasonFiles.addAll(shard(output,season,"Careers",packs("Careers",view.careers)));
            seasonFiles.addAll(shard(output,season,"Teams",packs("Teams",view.teams)));
            seasonFiles.addAll(shard(output,season,"Records",packs("Records",view.records)));
            seasonFiles.addAll(shard(output,season,"Highlights",packs("Highlights",view.highlights)));
            files.addAll(seasonFiles);
            metadata.add(obj("stagione",season,"contatori",view.counters,"dizionari",dictionaries(view),
                "files",seasonFiles.stream().map(f->obj("nome",f.name(),"tipo",f.kind(),
                    "righe",f.rows(),"byte",f.bytes(),"sha256",f.sha256())).toList()));
            System.out.println("VIEWS_SEASON_OK "+season+" players="+view.careers.size()+
                " fantasy="+loaded.archive().fantasyFactCount()+" files="+seasonFiles.size()+
                " bytes="+seasonFiles.stream().mapToLong(FileInfo::bytes).sum());
        }
        Map<String,Object> status=new LinkedHashMap<>();
        for(int i=1;i<=20;i++) status.put(String.format("TF%02d",i),"ATTIVA");
        for(String id:List.of("TF05","TF06"))status.put(id,"AGGREGATI_DISPONIBILI_SOGLIE_NON_APPROVATE");
        for(String id:List.of("TF11","TF12","TF13"))status.put(id,"NON_ATTIVA_ESCLUSIONI_NON_VERIFICATE");
        for(String id:List.of("TF14","TF15"))status.put(id,"NON_ATTIVA_BASELINE_E_SOGLIE_DA_VALIDARE");
        status.put("TF16","CONFRONTO_PER_SQUADRA_SENZA_VINCITORE_ARBITRARIO");
        if (!selectedIds.isEmpty())
            for (String id : new ArrayList<>(status.keySet()))
                if (!selectedIds.contains(id)) status.put(id, "NON_SELEZIONATO");
        Map<String,Object> facade=obj("schemaVersion","4.0-public-views-v1",
            "catalogo","TF01-TF20","status","STAGING_ONLY_NOT_PUBLISHED",
            "limitBytesPerJs",LIMIT_BYTES,"identity","SEASON_LOCAL_NO_AUTO_MERGE",
            "viste",List.of("Careers","Teams","Records","Highlights"),"eventi",EVENTS,
            "attivazioneRecord",status,"stagioni",metadata);
        byte[] b=("window.fcmRecordsNextTopFlopViews = "+json(facade)+";\n")
            .getBytes(StandardCharsets.UTF_8);
        files.add(write(output,FACADE,"","Facade",1,b));
        return new Result(List.copyOf(files),seasonIds.size(),players,real,fantasy,output);
    }
    @SuppressWarnings("unchecked")
    static void filterSelected(SeasonView view, Set<String> ids) {
        view.records.removeIf(record -> !ids.contains((String)record.get("recordId")));
        Set<String> highlightKeys = new LinkedHashSet<>();
        for (Map<String,Object> record : view.records) {
            for (Map<String,Object> result : (List<Map<String,Object>>)record.get("risultati")) {
                Object key = result.get("highlightKey");
                if (key != null) highlightKeys.add(key.toString());
            }
        }
        // Il portiere goleador merita un highlight anche se non ha un voto estremo.
        if (ids.contains("TF20")) {
            for (Map<String,Object> highlight : view.highlights) {
                Map<String,Object> player=(Map<String,Object>)highlight.get("calciatore");
                if(player == null || !"prestazioneReale".equals(highlight.get("tipo")))continue;
                if(!Integer.valueOf(1).equals(player.get("ruoloFca")))continue;
                Map<String,Object> events=(Map<String,Object>)highlight.get("eventi");
                if(events!=null && (events.containsKey("GOLFATTI1") || events.containsKey("ASSIST")))
                    highlightKeys.add(String.valueOf(highlight.get("key")));
            }
        }
        view.highlights.removeIf(h -> !highlightKeys.contains(h.get("key")));
        view.counters.put("selectedHighlights", view.highlights.size());
        view.counters.put("recordGroups", view.records.size());
    }

    static List<FileInfo> shard(Path dir,String season,String kind,
                                        List<Object> entries) throws IOException {
        String prefix="(function(){var w=window.fcmRecordsNextTopFlopViewsData||(window.fcmRecordsNextTopFlopViewsData={});"+
            "var g=w."+kind+"||(w."+kind+"={});var a=g[\""+season+"\"]||(g[\""+season+"\"]=[]);a.push(...[";
        String suffix="]);})();\n";
        byte[] overhead=(prefix+suffix).getBytes(StandardCharsets.UTF_8);
        List<FileInfo> result=new ArrayList<>();
        StringBuilder current=new StringBuilder(prefix);int used=overhead.length,count=0,part=1;
        for(var entry:entries) {
            String json=json(entry);
            int add=json.getBytes(StandardCharsets.UTF_8).length+(count==0?0:1);
            if(count>0 && used+add>TARGET_BYTES) {
                result.add(write(dir,name(season,kind,part++),season,kind,count,
                    (current.toString()+suffix).getBytes(StandardCharsets.UTF_8)));
                current=new StringBuilder(prefix);used=overhead.length;count=0;add=json.getBytes(StandardCharsets.UTF_8).length;
            }
            if(used+add>LIMIT_BYTES)throw new IOException("Oversize single view row "+season+" "+kind);
            if(count>0)current.append(',');current.append(json);used+=add;count++;
        }
        if(count>0)result.add(write(dir,name(season,kind,part),season,kind,count,
            (current.toString()+suffix).getBytes(StandardCharsets.UTF_8)));
        return result;
    }
    private static String name(String season,String kind,int n) {
        return "fcmRecordsNext_TopFlopViews."+kind+"."+season+"."+String.format("%03d",n)+".js";
    }
    private static FileInfo write(Path dir,String name,String season,String kind,int rows,byte[] bytes)
        throws IOException {
        if(bytes.length>LIMIT_BYTES)throw new IOException("JS size limit exceeded "+name+" "+bytes.length);
        Path target=dir.resolve(name);Files.write(target,bytes,
            java.nio.file.StandardOpenOption.CREATE_NEW);
        if(Files.size(target)!=bytes.length)throw new IOException("JS size changed: "+name);
        String hash;
        try {
            byte[] digest=MessageDigest.getInstance("SHA-256").digest(bytes);
            StringBuilder s=new StringBuilder();for(byte x:digest)s.append(String.format("%02x",x&255));hash=s.toString();
        }catch(Exception ex){throw new IllegalStateException(ex);}
        return new FileInfo(name,season,kind,rows,bytes.length,hash);
    }

    /** CLI, read-only database: db stage reports-or-dash season1 season2 ... */
    public static void main(String[] args) throws Exception {
        if(args.length<4)throw new IllegalArgumentException("<database> <new-stage> <reports-or-dash> <season...>");
        Path db=Path.of(args[0]).toAbsolutePath().normalize();
        if(!Files.isRegularFile(db))throw new IOException("Missing SQLite: "+db);
        Path out=Path.of(args[1]).toAbsolutePath().normalize();
        Path reports=args[2].equals("-")?null:Path.of(args[2]).toAbsolutePath().normalize();
        if(reports!=null && !Files.isDirectory(reports))throw new IOException("Missing reports: "+reports);
        Class.forName("org.sqlite.JDBC");
        try(Connection c=DriverManager.getConnection("jdbc:sqlite:"+db.toUri()+"?mode=ro")) {
            Result r=export(c,List.of(args).subList(3,args.length),out,reports);
            long total=r.files().stream().mapToLong(FileInfo::bytes).sum();
            long maximum=r.files().stream().mapToLong(FileInfo::bytes).max().orElse(0);
            System.out.println("TOP_FLOP_VIEWS_STAGE_OK seasons="+r.seasons()+" players="+r.players()+
                " realDays="+r.sourceRealDays()+" fantasyContexts="+r.sourceFantasyContexts()+
                " files="+r.files().size()+" bytes="+total+" maxBytes="+maximum);
        }
    }
}
~~~~

## `src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopViewsPublication.java`

SHA-256: `6e0a3e9063ed084c0ae763a09ff24fc9686e20010a822a4f5bf0927331542c56`

~~~~java
package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.config.MiniJson;
import it.alterlega.recordsnext.app.config.ProcessingConfigWriter;
import it.alterlega.recordsnext.app.model.ProcessingSelection;
import it.alterlega.recordsnext.app.model.RecordFamily;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Adattatore dell'exporter di VISTE approvate alla pipeline 3.2.
 * Nessuna pubblicazione diretta: aggiunge allo staging JS gia' validato.
 * La pipeline 3.2 mantiene la responsabilita' esclusiva di rollback e publish.
 */
public final class TopFlopViewsPublication {
    private TopFlopViewsPublication() {}

    private static final String PREFIX = "window.fcmRecordsNextTopFlopViews = ";
    private static final String BASENAME = "fcmRecordsNext_TopFlopViews.";

    public static Set<String> selectedIds(ProcessingSelection selection) {
        Objects.requireNonNull(selection);
        if (!selection.isFamilyEnabled(RecordFamily.TOP_FLOP)) return Set.of();
        Set<String> chosen = new LinkedHashSet<>();
        for (String child : ProcessingConfigWriter.TOP_FLOP) {
            boolean supported = ProcessingConfigWriter.topFlopImplemented(child);
            if (selection.enabledChildren().contains(child) && !supported) {
                throw new IllegalArgumentException("Top & Flop non ancora verificato: " + child);
            }
            if (supported && (selection.enabledChildren().isEmpty()
                    || selection.enabledChildren().contains(child))) {
                chosen.add(child.substring("topFlop.".length()).toUpperCase(java.util.Locale.ROOT));
            }
        }
        if (chosen.isEmpty()) {
            throw new IllegalArgumentException("Top & Flop attivo senza record disponibili selezionati");
        }
        return Set.copyOf(chosen);
    }

    /** Esegue il calcolo una volta sola, indipendentemente dal numero di siti. */
    public static Prepared prepare(Path sqlite, Path reports, List<String> seasonIds,
                                   Set<String> selectedIds, Path scratch) throws Exception {
        if (selectedIds.isEmpty()) throw new IllegalArgumentException("Selezionare i record TF");
        if (seasonIds.isEmpty()) throw new IllegalArgumentException("Nessuna stagione per Top & Flop");
        if (!Files.isRegularFile(sqlite)) throw new IOException("SQLite mancante: " + sqlite);
        if (reports != null && !Files.isDirectory(reports)) {
            throw new IOException("Report canonici mancanti: " + reports);
        }
        Files.createDirectories(scratch);
        Path output = scratch.resolve("js");
        Class.forName("org.sqlite.JDBC");
        TopFlopPublicViewsStageExporter.Result result;
        try (Connection db = DriverManager.getConnection("jdbc:sqlite:" + sqlite.toUri() + "?mode=ro")) {
            result = TopFlopPublicViewsStageExporter.export(db, seasonIds, output, reports, selectedIds);
        }
        for (var file : result.files()) {
            Path path = output.resolve(file.name());
            validate(path, file.bytes(), file.sha256());
        }
        return new Prepared(output, Set.copyOf(seasonIds), result.files().size());
    }

    /** Preserva esattamente le stagioni ammesse dalla 3.2 per il singolo sito. */
    public static int attachScoped(Prepared prepared, List<String> scope, Path generatedJs)
            throws Exception {
        Objects.requireNonNull(prepared);
        if (!Files.isDirectory(generatedJs)) throw new IOException("Staging 3.2 mancante: " + generatedJs);
        if (scope.isEmpty()) throw new IllegalArgumentException("Scope Top & Flop vuoto");
        if (!prepared.seasons().containsAll(scope) || scope.stream().distinct().count()!=scope.size()) {
            throw new IOException("Cutoff storico Top & Flop incoerente: " + scope);
        }
        Map<String,Object> facade = loadFacade(prepared.jsDirectory().resolve(
                TopFlopPublicViewsStageExporter.FACADE));
        @SuppressWarnings("unchecked") List<Map<String,Object>> metadata =
                (List<Map<String,Object>>) facade.get("stagioni");
        if(metadata==null)throw new IOException("Metadati Top & Flop mancanti");
        Map<String,Map<String,Object>> bySeason = new LinkedHashMap<>();
        for (Map<String,Object> m : metadata) {
            String season=String.valueOf(m.get("stagione"));
            if(bySeason.putIfAbsent(season,m)!=null)throw new IOException("Doppia stagione: "+season);
        }
        List<Map<String,Object>> scopedMetadata = new ArrayList<>();
        List<Path> sources = new ArrayList<>();
        for(String season:scope) {
            Map<String,Object> row=bySeason.get(season);
            if(row==null)throw new IOException("Stagione non presente nell'indice TF: "+season);
            scopedMetadata.add(row);
            @SuppressWarnings("unchecked") List<Map<String,Object>> files=
                    (List<Map<String,Object>>)row.get("files");
            if(files==null)throw new IOException("Elenco file mancante per "+season);
            for(var file:files) {
                String name=String.valueOf(file.get("nome"));
                if(!name.matches("fcmRecordsNext_TopFlopViews\\.(?:Careers|Teams|Records|Highlights)\\."
                    +season+"\\.[0-9]{3}\\.js")) {
                    throw new IOException("Nome shard TF non valido: "+name);
                }
                Path src=prepared.jsDirectory().resolve(name);
                validate(src,((Number)file.get("byte")).longValue(),String.valueOf(file.get("sha256")));
                sources.add(src);
            }
        }
        if(sources.stream().map(p->p.getFileName().toString()).distinct().count()!=sources.size())
            throw new IOException("Shard Top & Flop duplicati");
        Map<String,Object> scoped = new LinkedHashMap<>(facade);
        scoped.put("stagioni",scopedMetadata);
        scoped.put("status","GENERATED_FOR_SITE_SCOPE");
        byte[] facadeJs=(PREFIX+TopFlopCompactStageJsExporter.Json.write(scoped)+";\n")
                .getBytes(StandardCharsets.UTF_8);
        if(facadeJs.length>TopFlopPublicViewsStageExporter.LIMIT_BYTES)
            throw new IOException("Facade Top & Flop troppo grande: "+facadeJs.length);
        Path targetFacade = generatedJs.resolve(TopFlopPublicViewsStageExporter.FACADE);
        if(Files.exists(targetFacade))throw new IOException("Facade TF gia' presente: "+targetFacade);
        for (Path source:sources) {
            Path target=generatedJs.resolve(source.getFileName());
            if(Files.exists(target))throw new IOException("Collisione JS TF: "+target);
            Files.copy(source,target);
        }
        Files.write(targetFacade,facadeJs);
        return sources.size()+1;
    }

    /** Controlla tutti i JS dello staging prima che parta il publish 3.2. */
    public static int validateStaging(Path staging) throws IOException {
        int count=0;
        try (var files=Files.list(staging)) {
            for(Path file:files.filter(Files::isRegularFile).toList()) {
                if(!file.getFileName().toString().endsWith(".js"))continue;
                long bytes=Files.size(file);
                if(bytes>TopFlopPublicViewsStageExporter.LIMIT_BYTES)
                    throw new IOException("JS oltre 1.500.000 byte: "+file.getFileName()+"="+bytes);
                if(bytes==0)throw new IOException("JS vuoto: "+file.getFileName());
                count++;
            }
        }
        return count;
    }

    @SuppressWarnings("unchecked")
    private static Map<String,Object> loadFacade(Path file) throws IOException {
        String content=Files.readString(file, StandardCharsets.UTF_8).strip();
        if(!content.startsWith(PREFIX) || !content.endsWith(";"))
            throw new IOException("Facade Top & Flop non canonica");
        Object parsed=MiniJson.parse(content.substring(PREFIX.length(),content.length()-1));
        if(!(parsed instanceof Map<?,?>))throw new IOException("Facade TF invalida");
        return (Map<String,Object>)parsed;
    }

    private static void validate(Path file,long size,String expectedHash) throws Exception {
        if(!Files.isRegularFile(file) || Files.size(file)!=size || size<=0
                || size>TopFlopPublicViewsStageExporter.LIMIT_BYTES)
            throw new IOException("Dimensione JS incoerente: "+file);
        byte[] hash=MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file));
        StringBuilder hex=new StringBuilder();
        for(byte b:hash)hex.append(String.format("%02x",b&255));
        if(!hex.toString().equalsIgnoreCase(expectedHash))
            throw new IOException("Hash JS differente: "+file);
    }

    public record Prepared(Path jsDirectory,Set<String> seasons,int files) {}
}
~~~~

## `src/test/java/it/alterlega/recordsnext/app/model/CoreRecordCatalogCompletenessTest.java`

SHA-256: `6514c9b2d01e8b13b6bebca8d998c86be30da6072a1a4e3d206db1d6781ba966`

~~~~java
package it.alterlega.recordsnext.app.model;

import it.alterlega.recordsnext.app.config.ProcessingConfigWriter;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoreRecordCatalogCompletenessTest {

    @Test
    void catalogContainsEveryGranularProcessingId() {
        Set<String> configuredIds = new LinkedHashSet<>();

        Stream.of(
                ProcessingConfigWriter.CLASSICS,
                ProcessingConfigWriter.SERIES,
                ProcessingConfigWriter.RU,
                ProcessingConfigWriter.MODIFIERS,
                ProcessingConfigWriter.THRESHOLDS,
                ProcessingConfigWriter.TOP_FLOP
        ).flatMap(Arrays::stream).forEach(configuredIds::add);

        Set<String> catalogIds = CoreRecordCatalog.children().stream()
                .map(RecordChild::id)
                .collect(java.util.stream.Collectors.toSet());

        Set<String> missing = new LinkedHashSet<>(configuredIds);
        missing.removeAll(catalogIds);

        assertTrue(
                missing.isEmpty(),
                "ID presenti in ProcessingConfigWriter ma assenti da CoreRecordCatalog: " + missing
        );
    }

    @Test
    void catalogContainsAllGranularRecordsPlusCulometroExactlyOnce() {
        Set<String> configuredIds = new LinkedHashSet<>();

        Stream.of(
                ProcessingConfigWriter.CLASSICS,
                ProcessingConfigWriter.SERIES,
                ProcessingConfigWriter.RU,
                ProcessingConfigWriter.MODIFIERS,
                ProcessingConfigWriter.THRESHOLDS,
                ProcessingConfigWriter.TOP_FLOP
        ).flatMap(Arrays::stream).forEach(configuredIds::add);

        assertEquals(
                113,
                configuredIds.size(),
                "Il numero degli ID granulari configurabili è cambiato: aggiornare il contratto del catalogo"
        );

        Set<String> catalogIds = CoreRecordCatalog.children().stream()
                .map(RecordChild::id)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        assertEquals(
                CoreRecordCatalog.children().size(),
                catalogIds.size(),
                "CoreRecordCatalog contiene ID duplicati"
        );

        assertEquals(
                114,
                catalogIds.size(),
                "Il catalogo deve contenere i 113 record granulari più il Culometro"
        );

        assertTrue(
                catalogIds.contains(CoreRecordCatalog.CULOMETRO_ID),
                "Il Culometro deve essere presente nel catalogo"
        );
    }
}
~~~~

## `src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopNativeIntegrationSmokeTest.java`

SHA-256: `32710d2ce58ebd65060688ac0363c40ef446cfcff1836cd62699006cf3cd8159`

~~~~java
package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.config.ProcessingConfigLoader;
import it.alterlega.recordsnext.app.config.ProcessingConfigWriter;
import it.alterlega.recordsnext.app.model.CoreRecordCatalog;
import it.alterlega.recordsnext.app.model.RecordFamily;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Independent Java 21 smoke test, works without Maven/JUnit. */
public final class TopFlopNativeIntegrationSmokeTest {
    private TopFlopNativeIntegrationSmokeTest() {}
    private static void check(boolean p, String message){if(!p)throw new AssertionError(message);}
    public static void main(String[] args)throws Exception {
        if(args.length!=3) throw new IllegalArgumentException("<db> <reports> <temp-parent>");
        Path parent=Path.of(args[2]);Files.createDirectories(parent);
        check(CoreRecordCatalog.children().size()==114,"catalog 114");
        Path processing=parent.resolve("processing-native-test.json");
        Map<String,Boolean> children=new HashMap<>();
        for(String id:ProcessingConfigWriter.TOP_FLOP)children.put(id,false);
        children.put("topFlop.tf01",true);
        children.put("topFlop.tf04",true);
        children.put("topFlop.tf17",true);
        children.put("topFlop.tf20",true);
        children.put("topFlop.tf02",false);
        children.put("topFlop.tf11",false);
        ProcessingConfigWriter.State state=new ProcessingConfigWriter.State(
            false,false,false,false,false,true,false,false,children,
            ProcessingConfigWriter.defaultModifierNames());
        ProcessingConfigWriter.save(processing,state);
        check(ProcessingConfigWriter.load(processing).topFlop(),"GUI persisted family");
        var options=ProcessingConfigLoader.load(processing);
        check(options.familyEnabled(RecordFamily.TOP_FLOP),"loader enabled family");
        Set<String> selected=TopFlopViewsPublication.selectedIds(options.selection());
        check(selected.equals(Set.of("TF01","TF04","TF17","TF20")),"granular selections: "+selected);
        Path scratch=parent.resolve("native-source");
        var prepared=TopFlopViewsPublication.prepare(Path.of(args[0]),Path.of(args[1]),
            List.of("2006_2007","2026_2027"),selected,scratch);
        Path old=parent.resolve("old-js"), current=parent.resolve("new-js");
        Files.createDirectories(old);Files.createDirectories(current);
        int oldCount=TopFlopViewsPublication.attachScoped(prepared,List.of("2006_2007"),old);
        int newCount=TopFlopViewsPublication.attachScoped(prepared,List.of("2006_2007","2026_2027"),current);
        check(newCount>oldCount,"cutoff different file count");
        String oldFacade=Files.readString(old.resolve(TopFlopPublicViewsStageExporter.FACADE));
        check(!oldFacade.contains("2026_2027"),"cutoff prevents future seasons");
        String currentFacade=Files.readString(current.resolve(TopFlopPublicViewsStageExporter.FACADE));
        check(currentFacade.contains("2026_2027"),"anchor contains current season");
        check(currentFacade.contains("TF20"),"keepers enabled");
        check(currentFacade.contains("NON_SELEZIONATO"),"disabled IDs marked");
        check(TopFlopViewsPublication.validateStaging(old)==oldCount,"staging verify old");
        check(TopFlopViewsPublication.validateStaging(current)==newCount,"staging verify current");
        String r="";
        try(var files=Files.list(current)){
            for(Path file:files.filter(p->p.getFileName().toString().contains(".Records.")).toList())
                r+=Files.readString(file);
        }
        check(r.contains("\\\"recordId\\\":\\\"TF01\\\"") || r.contains("\"recordId\":\"TF01\""),"TF01 exported");
        check(!r.contains("\"recordId\":\"TF02\""),"TF02 not exported");
        boolean badScope=false;
        try{TopFlopViewsPublication.attachScoped(prepared,List.of("2019_2020"),parent.resolve("nowhere"));}
        catch(Exception expected){badScope=true;}
        check(badScope,"out-of-scope rejected");
        Path oversized=parent.resolve("oversized");Files.createDirectories(oversized);
        Files.write(oversized.resolve("oversize.js"),new byte[1_500_001]);
        boolean rejected=false;
        try{TopFlopViewsPublication.validateStaging(oversized);}catch(Exception expected){rejected=true;}
        check(rejected,"oversize rejected");
        Map<String,Boolean> unsupported=new HashMap<>(children);unsupported.put("topFlop.tf11",true);
        ProcessingConfigWriter.State wrong=new ProcessingConfigWriter.State(false,false,false,false,false,true,
           false,false,unsupported,ProcessingConfigWriter.defaultModifierNames());
        ProcessingConfigWriter.save(processing,wrong);
        // Unsupported selections are forcibly disabled on save, never emitted as invented results.
        check(!ProcessingConfigWriter.load(processing).childEnabled("topFlop.tf11"),"pending TF disabled");
        System.out.println("TOP_FLOP_NATIVE_INTEGRATION_OK selected="+selected+
            " oldSiteJs="+oldCount+" currentSiteJs="+newCount);
    }
}
~~~~

## `src/test/java/it/alterlega/recordsnext/app/TopFlopNativePublisherSmokeTest.java`

SHA-256: `09c84276a424c375fd0cc771fc4577d81cb5775bd2048557672432bf06d13a4e`

~~~~java
package it.alterlega.recordsnext.app;

import it.alterlega.recordsnext.Records2026SitePublisher;
import it.alterlega.recordsnext.app.manifest.ManifestMetadata;
import it.alterlega.recordsnext.app.manifest.ManifestPublishingSupport;
import it.alterlega.recordsnext.app.config.ProcessingConfigLoader;
import it.alterlega.recordsnext.app.config.ProcessingConfigWriter;
import it.alterlega.recordsnext.app.output.SeasonFamilyShardPublisher;
import it.alterlega.recordsnext.app.topflop.TopFlopViewsPublication;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** End-to-end smoke of 3.2 publisher/manifest/rollback staging with TF, never real sites. */
public final class TopFlopNativePublisherSmokeTest {
    private TopFlopNativePublisherSmokeTest() {}
    private static void ok(boolean pass, String what){if(!pass)throw new AssertionError(what);}
    public static void main(String[] args)throws Exception {
        if(args.length!=3)throw new IllegalArgumentException("<db> <reports> <scratch>");
        Path root=Path.of(args[2]);Files.createDirectories(root);
        Path cfg=root.resolve("processing.json");
        Map<String,Boolean> children=new HashMap<>();
        for(String id:ProcessingConfigWriter.TOP_FLOP)children.put(id,false);
        children.put("topFlop.tf03",true);children.put("topFlop.tf20",true);
        ProcessingConfigWriter.save(cfg,new ProcessingConfigWriter.State(false,false,false,false,false,
            true,false,true,children,ProcessingConfigWriter.defaultModifierNames()));
        var options=ProcessingConfigLoader.load(cfg);
        var preflight=PipelinePreflight.evaluate(options);
        var meta=new ManifestMetadata("RecordsNext by mauz79","4.0-test","2.0",
            OffsetDateTime.now(),"test","2006_2007",List.of("2006_2007"),List.of());
        var prepared=TopFlopViewsPublication.prepare(Path.of(args[0]),Path.of(args[1]),
            List.of("2006_2007","2026_2027"),TopFlopViewsPublication.selectedIds(options.selection()),
            root.resolve("prepared"));
        var publisher=Records2026SitePublisher.run(root,root,root.resolve("site-staging"),
            root.resolve("site-js"),true,false,false,options,preflight,meta,null,null,null);
        Path stage=publisher.stagingDirectory().resolve("js");
        int copied=TopFlopViewsPublication.attachScoped(prepared,List.of("2006_2007"),stage);
        SeasonFamilyShardPublisher.prepareFlat(stage);
        ManifestPublishingSupport.write(stage,options,preflight,meta);
        int validated=TopFlopViewsPublication.validateStaging(stage);
        ok(validated==copied+1,"new TF JS + 3.2 manifest");
        Path manifest=stage.resolve("fcmRecordsNext_Manifest.js");
        ok(Files.isRegularFile(manifest),"manifest available");
        String manifestText=Files.readString(manifest);
        ok(manifestText.contains("fcmRecordsNext_TopFlopViews.js"),"manifest includes TF facade");
        ok(manifestText.contains(".2006_2007."),"manifest includes TF season shard");
        ok(!manifestText.contains("2026_2027"),"manifest excludes future year");
        Path site=root.resolve("published-site-js");
        int published=RecordsNextPipeline.publishGeneratedDirectory(stage,site);
        ok(published==validated,"3.2 publisher count");
        ok(Files.isRegularFile(site.resolve("fcmRecordsNext_TopFlopViews.js")),"TF published in temp");
        Path second=root.resolve("second-generated");Files.createDirectories(second);
        Files.writeString(second.resolve("dummy.js"),"window.keep = true;\n");
        RecordsNextPipeline.publishGeneratedDirectory(second,site);
        ok(!Files.exists(site.resolve("fcmRecordsNext_TopFlopViews.js")),"disabled TF removes stale facade");
        try(var files=Files.list(site)) {
            ok(files.noneMatch(x->x.getFileName().toString().startsWith("fcmRecordsNext_TopFlopViews.")),
                "disabled TF removes stale season shards");
        }
        ok(Files.isRegularFile(site.resolve("dummy.js")),"other site JS preserved");
        System.out.println("TOP_FLOP_NATIVE_PUBLISHER_OK copied="+copied+
            " validated="+validated+" published="+published);
    }
}
~~~~

## `src/test/java/it/alterlega/recordsnext/app/TopFlopGuiPanelSmokeTest.java`

SHA-256: `33adca0edd7b8752240f9cdacd6d52996e089187f7e3f62595447f2ae88cf456`

~~~~java
package it.alterlega.recordsnext.app;

import it.alterlega.recordsnext.gui.RecordsNext2Dashboard;
import javax.swing.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;

/** Verifica GUI senza sovrascrivere la configurazione della lega reale. */
public final class TopFlopGuiPanelSmokeTest {
    private TopFlopGuiPanelSmokeTest() {}
    private static void ok(boolean value,String reason){if(!value)throw new AssertionError(reason);}
    @SuppressWarnings("unchecked")
    public static void main(String[] args)throws Exception {
        SwingUtilities.invokeAndWait(()->{
            try{
                Constructor<RecordsNext2Dashboard> ctor=RecordsNext2Dashboard.class.getDeclaredConstructor();
                ctor.setAccessible(true);
                Object gui=ctor.newInstance();
                Field nav=RecordsNext2Dashboard.class.getDeclaredField("navButtons");nav.setAccessible(true);
                Map<String,JToggleButton> buttons=(Map<String,JToggleButton>)nav.get(gui);
                ok(buttons.containsKey("topflop"),"Top & Flop navigable dedicated page");
                Field family=RecordsNext2Dashboard.class.getDeclaredField("familyChecks");family.setAccessible(true);
                Map<String,JCheckBox> families=(Map<String,JCheckBox>)family.get(gui);
                Field child=RecordsNext2Dashboard.class.getDeclaredField("childChecks");child.setAccessible(true);
                Map<String,JCheckBox> children=(Map<String,JCheckBox>)child.get(gui);
                ok(families.containsKey("topFlop"),"Top & Flop master exists");
                ok(children.keySet().stream().filter(x->x.startsWith("topFlop.")).count()==20,
                   "20 separate Top & Flop choices");
                families.get("topFlop").doClick();
                ok(children.get("topFlop.tf20").isEnabled(),"keeper goal records available");
                ok(children.get("topFlop.tf17").isEnabled(),"keeper penalty-save records available");
                ok(!children.get("topFlop.tf11").isEnabled(),"unverified incompresi disabled");
                ok(!children.get("topFlop.tf05").isEnabled(),"unapproved threshold disabled");
                Field frame=RecordsNext2Dashboard.class.getDeclaredField("frame");frame.setAccessible(true);
                JFrame window=(JFrame)frame.get(gui);
                window.dispose();
                System.out.println("TOP_FLOP_NATIVE_GUI_PANEL_OK checks=6");
            }catch(Exception ex){throw new RuntimeException(ex);}
        });
    }
}
~~~~

## `tools/Invoke-RecordsNext4NativeGuiPipeline-v2.py`

SHA-256: `2b734bedeef5cda6504fadb9b286a50e748ca3e60c05d98c66fa1093d3d7898d`

~~~~python
#!/usr/bin/env python3
"""RecordsNext4 opt-in NativeGUI/Pipeline installer; never publishes live sites.

Baseline SHA check -> sandbox compilation/tests -> backup -> install -> bible -> report.
Defaults are explicit Windows working copy and downloads; no editing 3.2.
"""
from __future__ import annotations
import argparse,datetime,hashlib,json,os,re,shutil,subprocess,sys,traceback,zipfile
from pathlib import Path
KIT=Path(__file__).resolve().parent.parent
SOURCE_FILES=[
 'src/main/java/it/alterlega/recordsnext/app/model/RecordFamily.java',
 'src/main/java/it/alterlega/recordsnext/app/model/CoreRecordCatalog.java',
 'src/main/java/it/alterlega/recordsnext/app/config/ProcessingConfigWriter.java',
 'src/main/java/it/alterlega/recordsnext/app/config/ProcessingConfigLoader.java',
 'src/main/java/it/alterlega/recordsnext/app/config/MiniJson.java',
 'src/main/java/it/alterlega/recordsnext/gui/RecordsNext2Dashboard.java',
 'src/main/java/it/alterlega/recordsnext/app/RecordsNextPipeline.java',
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopPublicViewsStageExporter.java',
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopViewsPublication.java',
 'src/test/java/it/alterlega/recordsnext/app/model/CoreRecordCatalogCompletenessTest.java',
 'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopNativeIntegrationSmokeTest.java',
 'src/test/java/it/alterlega/recordsnext/app/TopFlopNativePublisherSmokeTest.java',
 'src/test/java/it/alterlega/recordsnext/app/TopFlopGuiPanelSmokeTest.java',
]
SUPPORT_FILES=[
 'tools/Invoke-RecordsNext4NativeGuiPipeline-v2.py',
 'tools/Create-RecordsNext4WorkingCodeMd-v26.py',
 'docs/SPECIFICA_INTEGRAZIONE_NATIVA_TOP_FLOP_v1.md',
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v28.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v27.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v27.md',
 'docs/SPECIFICA_METRICHE_TOPFLOP_v1.md',
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v29.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v28.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v28.md',
]
INSTALL=SOURCE_FILES+SUPPORT_FILES
SMOKES=[
 'it.alterlega.recordsnext.app.topflop.TopFlopArchiveSmokeTest',
 'it.alterlega.recordsnext.app.topflop.TopFlopPublicViewsStageSmokeTest',
 'it.alterlega.recordsnext.app.topflop.TopFlopNativeIntegrationSmokeTest',
 'it.alterlega.recordsnext.app.TopFlopNativePublisherSmokeTest',
 'it.alterlega.recordsnext.app.TopFlopGuiPanelSmokeTest',
]

def sha(p:Path):
 h=hashlib.sha256()
 with p.open('rb') as f:
  for b in iter(lambda:f.read(1024*1024),b''):h.update(b)
 return h.hexdigest()

def command(argv, log, *, cwd=None, timeout=240):
 argv=list(map(str,argv))
 p=subprocess.run(argv,cwd=cwd,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,
                  text=True,encoding='utf-8',errors='replace',timeout=timeout)
 line='COMMAND '+Path(argv[0]).name+' => '+str(p.returncode)
 log.append(line+'\n'+(p.stdout or '')[-14000:])
 if p.returncode:raise RuntimeError(line+'\n'+(p.stdout or '')[-8500:])
 return p.stdout or ''


def javac_arg_path(p):
  """Use forward slashes in javac @argfile: raw Windows backslashes are escapes."""
  value=str(p).replace('\\','/')
  if any(c in value for c in ('"','\n','\r')):
   raise RuntimeError('Percorso non sicuro per javac @argfile: '+repr(value))
  return '"'+value+'"'

def write_javac_argfile(dest,paths):
  dest.write_text(''.join(javac_arg_path(p)+'\n' for p in paths),encoding='utf-8')

def putzip(z:zipfile.ZipFile,name,obj):
 z.writestr(name,json.dumps(obj,ensure_ascii=False,indent=2) if not isinstance(obj,str) else obj)

def main():
 parser=argparse.ArgumentParser()
 parser.add_argument('--project-dir',default=r'D:\DEV_APPS\RecordsNext4.0')
 parser.add_argument('--downloads-dir',default=r'D:\DEV_APPS\downloads')
 parser.add_argument('--database')
 parser.add_argument('--reports-dir')
 parser.add_argument('--jar')
 args=parser.parse_args()
 root=Path(args.project_dir).resolve();downloads=Path(args.downloads_dir).resolve()
 downloads.mkdir(parents=True,exist_ok=True)
 stamp=datetime.datetime.now().strftime('%Y%m%d_%H%M%S_%f')
 output=downloads/('RecordsNext4_NativeGuiPipeline_OUTPUT_v2_'+stamp+'.zip')
 work=downloads/('RecordsNext4_NativeGuiPipeline_TEST_v2_'+stamp)
 backup=downloads/('RecordsNext4_NativeGuiPipeline_BACKUP_v2_'+stamp+'.zip')
 status={'status':'ERROR','version':'native-gui-pipeline-v2','installed':[],
         'alreadyIdentical':[],'project':str(root),'sitesTouched':False,
         'sqliteTouched':False,'published':False,'checks':[]}
 log=[];changed=[];created=[];backupBytes={}
 try:
  if not (root/'pom.xml').is_file():raise RuntimeError('Worktree 4.0 inesistente: '+str(root))
  if not (root/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v25.md').is_file():
   raise RuntimeError('Manca bibbia v25 della precedente installazione verificata')
  if (root/'.git').exists() and shutil.which('git'):
   branch=command(['git','-C',root,'branch','--show-current'],log).strip()
   if branch!='feature/recordsnext4-players':raise RuntimeError('Branch non autorizzato: '+branch)
  baseline=json.loads((KIT/'baseline-sha256.json').read_text('utf-8'))
  manifest=json.loads((KIT/'manifest-sha256.json').read_text('utf-8'))
  if set(manifest['files'])!=set(INSTALL):raise RuntimeError('Manifest interno kit non coincide')
  for rel in INSTALL:
   src=KIT/rel;dst=root/rel
   if not src.is_file() or sha(src)!=manifest['files'][rel]:
    raise RuntimeError('Kit alterato: '+rel)
   if dst.exists():
    if not dst.is_file():raise RuntimeError('Destinazione non e un file: '+rel)
    cur=sha(dst)
    if cur==sha(src):status['alreadyIdentical'].append(rel)
    elif cur!=baseline.get(rel):raise RuntimeError('File di lavoro diverso dalla baseline; stop senza sovrascrivere: '+rel)
  java=shutil.which('java');javac=shutil.which('javac')
  if not java or not javac:raise RuntimeError('JDK 21 non trovato')
  v=command([javac,'-version'],log)
  if not (m:=re.search(r'javac\s+(\d+)',v)) or int(m.group(1))<21:
   raise RuntimeError('Serve javac 21+: '+v)
  db=(Path(args.database).resolve() if args.database else
      next(iter(sorted((root/'data/database').glob('recordsnext-2026_2027-verifica-v1_*.db'))),None))
  if db is None or not db.is_file():raise RuntimeError('SQLite aggiornato mancante; usare --database')
  reports=(Path(args.reports_dir).resolve() if args.reports_dir else
       root.parent/'RecordsNext2.0'/'data'/'reports')
  if not reports.is_dir():raise RuntimeError('Report canonici 3.2 mancanti: '+str(reports))
  jar=(Path(args.jar).resolve() if args.jar else
       next((p for p in (root/'target/RecordsNext.jar',
         root.parent/'RecordsNext2.0'/'target/RecordsNext.jar',
         root.parent/'RecordsNext2.0'/'release/RecordsNext_3.2.0_FULL/payload/RecordsNext.jar')
         if p.is_file()),None))
  if jar is None or not jar.is_file():raise RuntimeError('JAR runtime SQLite 3.2 non trovato')
  status.update(database=str(db),databaseSha256Before=sha(db),reports=str(reports),
                sourceBaselineVerified=True)
  # All test work is performed outside the user's project.
  src=work/'src/main/java';shutil.copytree(root/'src/main/java',src)
  for rel in SOURCE_FILES:
   if not rel.startswith('src/main/java/'):continue
   target=work/rel;target.parent.mkdir(parents=True,exist_ok=True)
   shutil.copy2(KIT/rel,target)
  cls=work/'classes';cls.mkdir(parents=True)
  sources=sorted(src.rglob('*.java'))
  argfile=work/'sources.txt';write_javac_argfile(argfile, sources)
  command([javac,'-encoding','UTF-8','-cp',jar,'-d',cls,'@'+str(argfile).replace('\\','/')],log,timeout=240)
  status['checks'].append('Compilazione sorgenti Java 21 FULL: OK')
  tests=[root/'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopArchiveSmokeTest.java',
         root/'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopPublicViewsStageSmokeTest.java']
  tests += [KIT/f for f in SOURCE_FILES if f.endswith('SmokeTest.java')]
  testfile=work/'tests.txt'
  write_javac_argfile(testfile, tests)
  cp=os.pathsep.join([str(cls),str(jar)])
  command([javac,'-encoding','UTF-8','-cp',cp,'-d',cls,'@'+str(testfile).replace('\\','/')],log,timeout=120)
  for name in SMOKES:
   testScratch=work/name.split('.')[-1]
   testScratch.mkdir(parents=True,exist_ok=True)
   argv=[java,'-cp',cp,name]
   if name.endswith('TopFlopNativeIntegrationSmokeTest') or name.endswith('TopFlopNativePublisherSmokeTest'):
    argv += [db,reports,testScratch]
   if name.endswith('TopFlopGuiPanelSmokeTest'):
    # Headless Linux CI: xvfb; ordinary Windows desktop runs Swing directly.
    if sys.platform.startswith('linux'):
     xvfb=shutil.which('xvfb-run')
     if xvfb:argv=[xvfb,'-a',*argv]
     else:raise RuntimeError('Test GUI impossibile: Xvfb assente')
   result=command(argv,log,cwd=testScratch,timeout=180)
   if '_OK' not in result:raise RuntimeError('Test non ha confermato OK: '+name)
   status['checks'].append(name.split('.')[-1]+': OK')
  if sha(db)!=status['databaseSha256Before']:
   raise RuntimeError('Hash database modificato dai test: installazione bloccata')
  status['checks'].append('Database invariato SHA256: OK')
  # Backup of precisely the files which will be replaced.
  for rel in INSTALL:
   src=KIT/rel;dst=root/rel
   if dst.exists() and sha(dst)!=sha(src):backupBytes[rel]=dst.read_bytes()
  if backupBytes:
   with zipfile.ZipFile(backup,'x',zipfile.ZIP_DEFLATED,compresslevel=8) as z:
    for rel,content in backupBytes.items():z.writestr(rel,content)
  for rel in INSTALL:
   src=KIT/rel;dst=root/rel
   if dst.exists() and sha(dst)==sha(src):continue
   dst.parent.mkdir(parents=True,exist_ok=True)
   temp=dst.with_name('.'+dst.name+'.rn4-native-v2-tmp')
   try:
    temp.write_bytes(src.read_bytes())
    if sha(temp)!=sha(src):raise RuntimeError('Verifica post-copia fallita: '+rel)
    os.replace(temp,dst)
   finally:
    if temp.exists():temp.unlink()
   (changed if rel in backupBytes else created).append(rel)
   status['installed'].append(rel)
  gen=root/'tools/Create-RecordsNext4WorkingCodeMd-v26.py'
  command([sys.executable,gen,'--project-dir',root],log,timeout=120)
  bible=root/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v26.md'
  if bible.is_file():
   status['checks'].append('Bibbia v26: '+sha(bible))
  if sha(db)!=status['databaseSha256Before']:
   raise RuntimeError('Hash database post-installazione modificato')
  status.update(status='OK',backup=str(backup) if backupBytes else None,
                databaseSha256After=sha(db),bible=str(bible))
 except Exception as ex:
  log.append(traceback.format_exc())
  status['error']=str(ex)
  # Restore precisely altered original sources and remove only newly-created files.
  for rel in reversed(changed):
   try:(root/rel).write_bytes(backupBytes[rel])
   except Exception as restoreErr:log.append('ROLLBACK ERROR '+rel+': '+str(restoreErr))
  for rel in reversed(created):
   try:(root/rel).unlink(missing_ok=True)
   except Exception as restoreErr:log.append('ROLLBACK ERROR '+rel+': '+str(restoreErr))
  status['status']='ERROR'
 finally:
  try:
   with zipfile.ZipFile(output,'w',zipfile.ZIP_DEFLATED,compresslevel=8) as z:
    putzip(z,'result.json',status)
    putzip(z,'execution.txt','\n\n'.join(log))
  finally:
   if work.exists():shutil.rmtree(work,ignore_errors=True)
  print('ESITO:',status['status'])
  if status['status']=='OK':
   print('GUI TOP & FLOP: 20 record; 13 attivi, 7 non verificati bloccati')
   print('PUBLISHER: 3.2 originale, cutoff/manifest/rollback testati in cartella temporanea')
   print('BIBBIA:',status.get('bible'))
   print('SORGENTI INSTALLATI:',len(status['installed']),'GIA IDENTICI:',len(status['alreadyIdentical']))
  else:print('ERRORE:',status.get('error'))
  print('ZIP DA CARICARE:',output)
 return 0 if status['status']=='OK' else 1

if __name__=='__main__':sys.exit(main())
~~~~

## `docs/SPECIFICA_INTEGRAZIONE_NATIVA_TOP_FLOP_v1.md`

SHA-256: `d57ceb59d9555f833fd0aa66d30221d8e2424136d84b07b1ca902fedae24e931`

~~~~markdown
# RecordsNext 4.0 — integrazione nativa Top & Flop v1

Data: 2026-11-06. **Stato: codice compilato e smoke test locale; release e test FULL/CONSOLIDATED Windows da confermare.**

## Principio vincolante

- Nessun database parallelo, nessuna cartella condivisa ReNewo, nessun asset CSS sovrascritto.
- Fonte integrale: `data/database/recordsnext.db` e normalizzati 3.2 già configurati.
- Siti: soltanto viste Careers, Teams, Records, Highlights (non fatti completi FCM/FCA), suddivise per stagione; **ogni JS ≤ 1.500.000 byte fisici**.
- L'exporter gira **una sola volta per elaborazione**, poi `attachScoped` assembla per ciascun sito gli shard delle sole stagioni ammesse da `SeasonPublicationTargetRepository.scope`, secondo il suo `sort_order`.
- JS nella cartella `js` (flat), manifest scritto tramite `ManifestPublishingSupport`, conversione shard 3.2 con `SeasonFamilyShardPublisher.prepareFlat`, pubblicazione e rollback con `RecordsNextPipeline.publishGeneratedDirectory`.
- Non è introdotto alcun nuovo publisher: l'adattatore Top & Flop aggiunge file **solo allo staging**. La pubblicazione resta prerogativa del 3.2.
- Lo SHA-256 di ogni shard sorgente è verificato con i metadati della facade. Collisioni e superamento del limite dimensionale bloccano la procedura.
- Nuova pulizia dei TF obsoleti integrata nella gestione stale+backup+rollback del publisher multisito originale, inclusa rimozione della facade quando la famiglia viene deselezionata.

## GUI

- Nuova voce laterale **Top & Flop**, con **20 checkbox TF01–TF20** e interruttore generale di famiglia.
- Le scelte sono conservate nel consolidato `config/processing.json` usando `processing.families.topFlop.enabled` e `children.tf01...tf20`. Nessuna selezione si attiva automaticamente aggiornando un vecchio file della 3.2.
- Registrazione in `RecordFamily`, `CoreRecordCatalog` e `ProcessingConfigLoader`, senza rinominare i 94 record originali (catalogo ora 114).
- **Attivabili adesso:** TF01–TF04, TF07–TF10, TF16–TF20 (13 record). Per TF16 si espone il confronto per squadra senza vincitore arbitrario; le viste riepilogative si mantengono come supporto.
- **Visibili ma disabilitati:** TF05–TF06 (soglie non fissate), TF11–TF13 (esclusioni/tesseramenti non verificati), TF14–TF15 (baseline non validata). L'attivazione a mano tramite JSON viene rifiutata invece di simulare classifiche.
- I portieri partecipano a TF01–TF10 e a TF17–TF20 (rigori parati, gol subiti, giornate a zero gol subiti registrati, gol/rigori/assist di portieri). Gol su rigore e gol subiti su rigore restano sottocategorie, non somme aggiuntive.

## Viste e selezione

- `TopFlopPublicViewsStageExporter.export(..., selectedIds)` filtra i record TF selezionati e pubblica gli highlight strettamente collegati, mantenendo per TF20 episodi eccezionali di gol/assist dei portieri.
- Anche quando un record non è attivato, lo SQLite mantiene tutti i dati originali. Le viste Careers/Teams fungono da base per la futura scheda di carriera e i confronti di utilizzo.
- `window.fcmRecordsNextTopFlopViews.stagioni` contiene solo le stagioni dello scope di pubblicazione. Il sito di una stagione storica non deve mai includere file o metadati successivi.

## Prove eseguite localmente

- Compilazione Java 21 completa dei sorgenti principali.
- Test 52 verifiche del modello e test sintetico del portiere con doppio contesto fantasy.
- Test lettura/scrittura `processing.json` e scelta granulare TF01/TF04/TF17/TF20.
- Dati reali 2006/07+2026/27: test di scope e dimensioni, facade 2006/07 priva di riferimenti 2026/27.
- Pipeline di pubblicazione 3.2 su cartella **temporanea di prova**: manifest aggiornato, file TF presenti, disabilitazione rimuove facade+shard senza rimuovere file non TF.
- UI Swing con schermo virtuale: nuovo pannello presente, 20 checkbox, indisponibili i record in verifica, selezionabili i portieri.
- **Non ancora dimostrati:** esecuzione completa FULL e CONSOLIDATED sul computer Windows dell'utente, deploy ReNewo e reali link site-wide, visualizzatori HTML definitivi. Questa patch non realizza le classifiche Incompresi incomplete e non è un rilascio 4.0.0.

## Installazione

Installer `tools/Invoke-RecordsNext4NativeGuiPipeline-v1.py`, con backup degli originali, validazione SHA-256, 21+ test e bibbia `docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v26.md`. **L'installer non esegue publish sui siti e non altera il database.** Dopo il test Windows caricare solamente il suo ZIP diagnostico.
~~~~

## `docs/PIANO_RECORDSNEXT4_GIOCATORI_v28.md`

SHA-256: `221469251892c172dfb71b723e58a7e886cc08bf6518a77d95f583e767d62797`

~~~~markdown
# Piano RecordsNext 4.0 v28 — catalogo e GUI native, pubblicazione 3.2

Data 2026-11-06. Addendum conservativo a `PIANO_RECORDSNEXT4_GIOCATORI_v27.md`.

**Implementato nel codice da installare e da verificare su Windows:** famiglia TOP_FLOP opt-in nel catalogo e nella GUI; pannello di scelta TF01–TF20, filtro nell'exporter delle sole viste approvate, adapter per scope storico, manifest e publisher esistenti, recupero dei file TF non più selezionati. Rimane il limite bloccante 1.500.000 byte/JS. Il test locale ha confermato le scelte granulari e la rimozione di file obsoleti nella copia temporanea del sito.

**Non è una release:** servono verifica con runner Windows, FULL/CONSOLIDATED reali, GUI effettiva, visualizzatore HTML RecordsNext/ReNewo, validazione del markup e skin, record incompleti TF05/06 e TF11–15 soltanto dopo evidenze e criteri approvati. Prima dei test sul sito reale il publish della GUI deve rimanere disabilitato.

Nessuna modifica alla directory `D:\DEV_APPS\RecordsNext2.0`; lavori solo sul branch `feature/recordsnext4-players` in `D:\DEV_APPS\RecordsNext4.0`.
~~~~

## `docs/REGISTRO_DECISIONI_RECORDSNEXT4_v27.md`

SHA-256: `01195e9f739fd56de63daf6fdd2032c64e624b15c5be118f7277b5968cf36581`

~~~~markdown
# Registro decisioni RecordsNext 4.0 v27 — integrazione nativa

Addendum 2026-11-06, preserva `REGISTRO_DECISIONI_RECORDSNEXT4_v26.md`.

| ID | Stato | Decisione |
|---|---|---|
| D-188 | IMPLEMENTATA/TEST LOCALE | `TOP_FLOP` entra nel catalogo: 20 ID `topFlop.tf01...tf20`, senza rinumerare o modificare i precedenti 94 record. |
| D-189 | IMPLEMENTATA/TEST LOCALE | GUI con pannello dedicato e scelta granulare persistente nel `processing.json`; default storico disattivo per compatibilità con i siti 3.2. |
| D-190 | VINCOLANTE | TF05–TF06, TF11–TF15 non selezionabili finché soglie, baseline ed esclusioni non sono verificati; una configurazione che li attivi va rifiutata. |
| D-191 | IMPLEMENTATA/TEST LOCALE | Export TF una volta per elaborazione, poi scope per sito da `SeasonPublicationTargetRepository.scope`, senza copiare futuro su sito storico. |
| D-192 | IMPLEMENTATA/TEST LOCALE | `ManifestPublishingSupport` e `SeasonFamilyShardPublisher.prepareFlat` della 3.2; ultimo passaggio attraverso `publishGeneratedDirectory` con backup e rollback esistenti. |
| D-193 | IMPLEMENTATA/TEST LOCALE | JS TopFlop obsoleti (compresa facade) eliminati con backup/rollback della 3.2 quando la famiglia viene disattivata o si restringe lo scope. |
| D-194 | VINCOLANTE | **Ogni singolo JS non supera 1.500.000 byte.** Validazione SHA-256 per le viste e validazione dimensione dell'intero staging prima di pubblicare. |
| D-195 | DA VERIFICARE SU WINDOWS | Eseguire runner su copia del worktree, controllare visualizzatore HTML, FULL/CONSOLIDATED e pubblicazione multisito prima di chiamare il rilascio 4.0.0 pronto. |
~~~~

## `docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v27.md`

SHA-256: `2b8bad3d3a53024b5b6801755c2a10faffbaa3fac65ef53bc025515bea950210`

~~~~markdown
# Diario implementazione RecordsNext 4.0 v27

2026-11-06. Sorgente di partenza: ZIP del worktree `RecordsNext4_SORGENTI_ATTUALI_20261106.zip` più aggiornamenti CanonicalRounds, Contributions, TopFlop JS, Compact e PublicViews v1, ultimo output Windows `RecordsNext4_TopFlopVistePubbliche_OUTPUT_v1_20261106_145346_272773.zip`, esito OK, 85 JS per 5.355.536 byte, massimo 208.689 byte, bibbia v25 generata sul PC.

Implementazione locale: nuovo enum `TOP_FLOP`; catalogo allargato a 114 record; persistenza `topFlop` nel JSON compatibile con la 3.2; GUI con pannello dedicato e venti voci; blocco delle sette voci non ancora affidabili; selezione record effettiva nei JS; nuovo `TopFlopViewsPublication` per una sola estrazione locale e cutoff per sito; aggiunta in `RecordsNextPipeline` nel flusso generate-only e multisito; manifest aggiornato e rimozione transactional dei TF obsoleti.

**Verifiche locali:** `javac` Java 21 completo; 52 test modello; test portiere; test selezione e scope su SQLite reale 2006/07 e 2026/27; test di pubblicazione senza sito reale con manifest e pulizia stale; smoke Swing con display virtuale. Nessuna copia nei siti reali. Mancano le verifiche Windows sulle parti nuove.

**Prossimo passo:** installer con SHA-256 conservativo e generatore bibbia v26; dopo ZIP Windows positivo completare visualizzatore HTML e test di integrazione FULL/CONSOLIDATED/multisito.
~~~~

## `docs/SPECIFICA_METRICHE_TOPFLOP_v1.md`

SHA-256: `bd66fbde838b853dfbf6fbf387691aa8e56b61251a5dbb914294489744b5dbe1`

~~~~markdown
# RecordsNext 4.0 — Definizioni delle sette metriche TF sospese — proposta v1

Data: 2026-11-06. **Stato: proposta tecnica, da confermare. NON attivare automaticamente TF05/TF06/TF11–TF15.**

## Regole comuni

- Archivio integrale e aggregati nel SQLite locale. Solo classifiche, schede e highlights significativi nei JS; cutoff storico e limite fisico 1.500.000 byte/JS.
- `VOTO1` FCA valido = voto reale; `TOT` FCM = fantavoto **registrato nel tabellino** per giocatore effettivamente COUNTED. Mai ricostruire TOT di un escluso con coefficienti generici.
- Raggruppamenti e ranking per stagione, carriera, ruolo (portieri inclusi), fantasquadra, competizione e famiglia CAMPIONATO/COPPE; evitare confronti fra popolazioni diverse. Ordinare numericamente e mantenere i pari merito. Denominatori sempre visibili.
- Soglie candidate CONFIGURABILI nella GUI; non trattare le stagioni incomplete come stagioni da 38 giornate e non valutare medie con `N=0`.

## TF05 — medie reali

`mediaReale = SUM(VOTO1 valido) / N_R`, una sola riga per calciatore/giornata reale, senza moltiplicare per serie o coppe fantasy. Graduatorie TOP e FLOP. **Proposta:** `N_R >= 5` per stagione, `N_R >= 20` per carriera. Visualizzare N e soglia; massimi/minimi della singola gara restano TF01/TF02 senza soglia.

## TF06 — fantamedie apportate

`fantamediaApportata = SUM(FCM.Tabellino.TOT) / N_A` solo per contesti COUNTED con TOT numerico. **Proposta:** `N_A >= 5` per stagione CAMPIONATO, `N_A >= 3` per stagione COPPE, `N_A >= 20` per graduatoria carriera; se si raggruppa campionato+coppe esporre sempre N per famiglia. Una prestazione reale puo apportare in piu leghe/coppe ed e contato una volta per ciascun incontro fantasy reale. TOP e FLOP separati.

## TF11, TF12, TF13 — quattro misure Incompresi

Unità: occasione di esclusione in un incontro fantasy giocato per una rosa che **possedeva il calciatore in quella giornata**, sulla base della cronologia di tesseramento, transazioni e formazione; non creare E da assenza in `Formazione` senza prova del possesso. Tenere separati gli incontri di campionato/coppa effettivamente distinti.

- `E` = esclusioni certe; `P_V` = esclusioni con voto reale valido; `P_F` = esclusioni con fantavoto attendibile; `S_V` = somma voti su `P_V`; `S_F` = somma fantavoti su `P_F`.
- **TF11:** `S_V / P_V` (media voto sprecato), con `P_V >= 2` e `E >= 3` proposti.
- **TF12:** `S_V / E` (errore pesato voto), con `E >= 3` proposto. Si somma il voto INTERO, non `voto - 6`; esclusioni senza voto restano nel denominatore.
- **TF13:** DUE classifiche, `S_F / P_F` (fantamedia sprecata) e `S_F / E` (errore pesato fantavoto); `P_F >= 2` e `E >= 3` proposti. **Non attivare il ranking finché la disponibilità dei fantavoti degli esclusi non è comprovata e la copertura per il confronto non è omogenea.**
- `E=0`: valori pesati NULL; `P=0`: medie NULL. Assenza di dato ≠ voto zero. In caso di periodi di proprietà incerti escludere dalla classifica e indicare copertura, non fingere E completo. Ricongiungere compravendita solo se non c'e una giornata reale senza possesso (esempio Varela).

## TF14 e TF15 — stagioni eccezionali / sbagliate

Per ogni calciatore e stagione: `mediaStagione = SV_stagione/N_stagione`; `baseline = SUM(VOTO1 valido in TUTTE le altre stagioni) / N_altre`; `delta = mediaStagione - baseline`. La baseline e ponderata per ciascun voto, non e la media aritmetica delle medie stagionali; non usare la stagione esaminata nel riferimento.

**Proposta:** `N_stagione >= 8`, `N_altre >= 20` distribuiti su almeno **due altre stagioni**. TF14 ordina `delta` decrescente e identifica delta **>= +0,75**; TF15 ordina delta crescente e identifica delta **<= -0,75**. Entrambi espongono media, baseline, N, delta e anni di confronto. Una stagione in cui una fantasquadra ha acquistato un giocatore al momento sbagliato va mostrata come CONTESTO fantasy verificato, non confusa con la definizione del record reale.

## Stato e attivazione

TF05/TF06 e TF14/TF15: implementabili dopo conferma delle soglie e confronto con distribuzione reale su campionato/coppe. TF11–TF13: richiedono prima chiusura tecnica della cronologia di possesso e, per TF13, prova delle fonti fantavoto non impiegate. **La GUI attuale deve mantenere sette voci non attivabili** fino a completamento; niente valori dimostrativi pubblicati.
~~~~

## `docs/PIANO_RECORDSNEXT4_GIOCATORI_v29.md`

SHA-256: `b6518440a4fa59b1d723a10e781e1406a820898d7a1ff8077f7294af272178c2`

~~~~markdown
# Piano RecordsNext 4.0 v29 — correzione runner Windows e metriche sospese

Addendum 2026-11-06 a `PIANO_RECORDSNEXT4_GIOCATORI_v28.md`.

1. Eseguire il runner **v2** con percorsi normalizzati `/` in tutti gli `@argfile` di javac, dopo errore bloccante del runner v1; nessuna modifica alle classi Java già testate.
2. Se l'installazione Windows passa, comporre e avviare il JAR aggiornato della GUI sul worktree 4.0, testare pannello e selezione, generare pubblicazione SOLO in directory temporanea.
3. Completare il visualizzatore ReNewo e i test FULL / CONSOLIDATED multisito, prima di pubblicare.
4. Metriche TF05–TF06 e TF11–TF15: formule e soglie CANDIDATE in `SPECIFICA_METRICHE_TOPFLOP_v1.md`, nessuna abilitazione automatica. Convalidare campioni reali e tracciabilità tesseramenti.
5. Limitare i JS del sito alle viste TF interessanti, senza replicare lo storico FCA/FCM; <= 1.500.000 byte per JS.
~~~~

## `docs/REGISTRO_DECISIONI_RECORDSNEXT4_v28.md`

SHA-256: `f87dbd6603eb4a89652d88bab51e3d037d9f666e3ebf9fa715a0d87fec6f76da`

~~~~markdown
# Registro decisioni RecordsNext 4.0 v28 — ripristino installer e metriche proposte

Addendum 2026-11-06 a `REGISTRO_DECISIONI_RECORDSNEXT4_v27.md`.

| ID | Stato | Decisione |
|---|---|---|
| D-196 | VERIFICATO DAL LOG WINDOWS | Runner NativeGuiPipeline v1 fallisce alla prima compilazione: i backslash dei percorsi sorgente in `@sources.txt` vengono interpretati come escape da javac; output `installed=[]`, nessun file di progetto alterato. |
| D-197 | CORREZIONE V2 DA VALIDARE SU WINDOWS | Scrivere percorsi con `/` e virgolette in ENTRAMBI gli argument file javac (`sources.txt`, `tests.txt`), mantenendo backup, controlli hash e installazione post-test. |
| D-198 | PROPOSTA, NON ATTIVA | Soglie e formule TF05/06/11/12/13/14/15 definite in `SPECIFICA_METRICHE_TOPFLOP_v1.md`; sottoporre a verifica su dati reali prima di abilitare le classifiche. |
| D-199 | VINCOLANTE | Incompresi: E dimostrato da possesso effettivo in un incontro fantasy giocato; TF13 non attivabile con fantavoti imputati o assenti; quattro metriche separate. |
| D-200 | VINCOLANTE | Niente modifiche alla 3.2 operativa, alle skin personalizzate o ai siti durante il fix del runner. |
~~~~

## `docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v28.md`

SHA-256: `946891139ea8cd4b0a90d02f8a53912fa92094dd827203786c5889b0b058ac3f`

~~~~markdown
# Diario implementazione RecordsNext 4.0 v28

2026-11-06. Ricevuto `RecordsNext4_NativeGuiPipeline_OUTPUT_v1_20261106_162900_806104.zip`.

Diagnosi: `javac.EXE => 2`, `Invalid filename: D:DEV_APPSdownloads...`; i percorsi con backslash non escapati nel file di argomenti `@sources.txt` sono analizzati dal JDK come sequenze di escape (`\r`, ecc.). Il `result.json` indica baseline verificata, `installed: []`, siti e database intatti.

Intervento v2: `Invoke-RecordsNext4NativeGuiPipeline-v2.py` conserva il kit Java del v1 byte per byte, normalizza gli argomenti sorgente/test a slash forward con virgolette, produce output/backup propri v2 e verifica la regola dei percorsi; manifest SHA-256 ricalcolato.

Fissata la bozza di sette metriche pendenti con numeratori, denominatori, soglie e regole sulle assenze, in `SPECIFICA_METRICHE_TOPFLOP_v1.md`. **Non sono attivate** e non sono presentate come già approvate.

Prossima verifica: runner v2 Windows, quindi JAR GUI con test FULL/CONSOLIDATED e visualizzatore.
~~~~
