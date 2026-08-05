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
            String id = switch (family) {
                case MODIFIERS -> switch (entry.getKey()) {
                    case "defence" -> "modifiers.defence";
                    case "captain" -> "modifiers.captain";
                    case "homeField" -> "modifiers.home-field-deciding";
                    default -> "modifiers." + entry.getKey();
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
