package it.alterlega.recordsnext.app.culometro;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class CulometroConfigWriter {
    private CulometroConfigWriter() {}

    public static void save(Path file, CulometroConfig c) throws IOException {
        CulometroConfigLoader.validate(c);
        StringBuilder out = new StringBuilder();
        out.append("{\n  \"schemaVersion\": \"2.0\",\n");
        out.append("  \"enabled\": ").append(c.enabled()).append(",\n");
        out.append("  \"minimumMatches\": ").append(c.minimumMatches()).append(",\n");
        out.append("  \"normalization\": { \"mode\": \"PER_MATCH\", \"centerOnHistoricalMean\": true, \"kScale\": ")
                .append(number(c.kScale())).append(" },\n");
        out.append("  \"overlap\": { \"strategy\": \"PRIMARY_PLUS_SECONDARY\", \"secondaryWeight\": ")
                .append(number(c.secondaryWeight())).append(", \"tagWeight\": 0.0, \"maxSecondary\": 1, \"maxTags\": 2 },\n");
        out.append("  \"rarity\": { \"enabled\": true, \"profile\": \"NORMAL\", \"maximumMultiplier\": ")
                .append(number(c.maximumRarityMultiplier())).append(", \"minimumHistoricalOccurrences\": ")
                .append(c.minimumHistoricalOccurrences()).append(" },\n");
        out.append("  \"components\": [\n");
        int i = 0;
        for (Map.Entry<String, CulometroConfig.Component> e : c.components().entrySet()) {
            CulometroConfig.Component v = e.getValue();
            if (i++ > 0) out.append(",\n");
            out.append("    { \"componentId\": \"").append(escape(e.getKey())).append("\", \"enabled\": ")
                    .append(v.enabled()).append(", \"weight\": ").append(number(v.weight()))
                    .append(", \"allowedRange\": { \"min\": ").append(number(v.min()))
                    .append(", \"max\": ").append(number(v.max())).append(" } }");
        }
        out.append("\n  ],\n  \"labels\": {\n");
        var lc = c.labelConfiguration();
        out.append("    \"preset\": \"").append(escape(lc.preset())).append("\",\n");
        out.append("    \"customized\": ").append(lc.customized()).append(",\n");
        out.append("    \"resetSource\": \"").append(escape(lc.resetSource())).append("\",\n");
        out.append("    \"bands\": "); appendBands(out, lc.bands(), 4); out.append(",\n");
        out.append("    \"presetDefaults\": {\n");
        int p = 0;
        for (Map.Entry<String, List<CulometroConfig.LabelBand>> e : lc.presetDefaults().entrySet()) {
            if (p++ > 0) out.append(",\n");
            out.append("      \"").append(escape(e.getKey())).append("\": "); appendBands(out, e.getValue(), 6);
        }
        out.append("\n    }\n  }\n}\n");
        Files.createDirectories(file.toAbsolutePath().normalize().getParent());
        Files.writeString(file, out.toString(), StandardCharsets.UTF_8);
    }

    private static void appendBands(StringBuilder out, List<CulometroConfig.LabelBand> bands, int indent) {
        out.append("[\n");
        String pad = " ".repeat(indent + 2);
        for (int i = 0; i < bands.size(); i++) {
            var b = bands.get(i);
            if (i > 0) out.append(",\n");
            out.append(pad).append("{ \"min\": ").append(number(b.min())).append(", \"label\": \"")
                    .append(escape(b.label())).append("\" }");
        }
        out.append("\n").append(" ".repeat(indent)).append("]");
    }

    private static String number(BigDecimal value) { return value.stripTrailingZeros().toPlainString(); }
    private static String escape(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n"); }
}
