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
            boolean culometro,
            boolean publishToSite,
            Map<String, Boolean> children,
            Map<String, String> modifierNames
    ) {
        public State {
            children = Map.copyOf(children == null ? Map.of() : children);
            modifierNames = Map.copyOf(modifierNames == null ? defaultModifierNames() : modifierNames);
        }

        public State(
                boolean classics,
                boolean series,
                boolean ru,
                boolean modifiers,
                boolean thresholdsLuck,
                boolean culometro,
                boolean publishToSite
        ) {
            this(classics, series, ru, modifiers, thresholdsLuck, culometro,
                    publishToSite, Map.of(), defaultModifierNames());
        }

        public boolean childEnabled(String id) {
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

        return new State(
                enabled(json, "classics"),
                enabled(json, "series"),
                enabled(json, "ru"),
                enabled(json, "modifiers"),
                enabled(json, "thresholdsLuck"),
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

    public static final String[] THRESHOLDS = {
            "thresholds.surgical-win", "thresholds.mocking-loss",
            "thresholds.miraculous-draw", "thresholds.narrow-draw",
            "thresholds.missed-win-half-point", "thresholds.loss-by-a-whisker",
            "thresholds.exact-threshold", "thresholds.just-enough",
            "thresholds.wasted-points", "luck.favourable-events",
            "luck.unfavourable-events", "luck.balance"
    };
}
