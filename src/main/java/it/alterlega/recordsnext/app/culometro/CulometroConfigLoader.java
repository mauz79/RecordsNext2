package it.alterlega.recordsnext.app.culometro;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CulometroConfigLoader {
    private CulometroConfigLoader() {}

    public static CulometroConfig load(Path file) throws IOException {
        if (!Files.isRegularFile(file)) throw new IOException("Configurazione Culometro non trovata: " + file);
        Object parsed = new Json(Files.readString(file, StandardCharsets.UTF_8)).parse();
        Map<String,Object> root = object(parsed, "root");
        if (!"2.0".equals(string(root.get("schemaVersion")))) throw new IllegalArgumentException("schemaVersion Culometro non supportata");
        boolean enabled = bool(root.get("enabled"), false);
        int minimumMatches = integer(root.get("minimumMatches"), 20);
        Map<String,Object> normalization = object(root.get("normalization"), "normalization");
        BigDecimal kScale = decimal(normalization.get("kScale"), "4.15");
        Map<String,Object> overlap = object(root.get("overlap"), "overlap");
        BigDecimal secondaryWeight = decimal(overlap.get("secondaryWeight"), "0.20");
        Map<String,Object> rarity = object(root.get("rarity"), "rarity");
        BigDecimal maximumRarity = decimal(rarity.get("maximumMultiplier"), "5.25");
        int minimumOccurrences = integer(rarity.get("minimumHistoricalOccurrences"), 3);

        Map<String,CulometroConfig.Component> components = new LinkedHashMap<>();
        Object rawComponents = root.get("components");
        if (rawComponents instanceof List<?> list) {
            for (Object item : list) {
                Map<String,Object> node = object(item, "component");
                String id = string(node.get("componentId"));
                Map<String,Object> range = object(node.get("allowedRange"), "allowedRange");
                components.put(id, new CulometroConfig.Component(
                        bool(node.get("enabled"), true),
                        decimal(node.get("weight"), "1.00"),
                        decimal(range.get("min"), "0.50"),
                        decimal(range.get("max"), "2.00")
                ));
            }
        }

        Map<String,Object> labelsNode = object(root.get("labels"), "labels");
        String preset = string(labelsNode.get("preset"));
        boolean customized = bool(labelsNode.get("customized"), false);
        String resetSource = string(labelsNode.get("resetSource"));
        List<CulometroConfig.LabelBand> labels = readBands(labelsNode.get("bands"), "labels.bands");

        Map<String,List<CulometroConfig.LabelBand>> presetDefaults = new LinkedHashMap<>();
        Map<String,Object> defaultsNode = object(labelsNode.get("presetDefaults"), "labels.presetDefaults");
        for (Map.Entry<String,Object> entry : defaultsNode.entrySet()) {
            presetDefaults.put(entry.getKey(), List.copyOf(readBands(entry.getValue(), "preset " + entry.getKey())));
        }

        CulometroConfig.LabelConfiguration labelConfiguration = new CulometroConfig.LabelConfiguration(
                preset, customized, resetSource, List.copyOf(labels), Map.copyOf(presetDefaults));
        CulometroConfig config = new CulometroConfig(enabled, minimumMatches, kScale, secondaryWeight,
                maximumRarity, minimumOccurrences, Map.copyOf(components), labelConfiguration);
        validate(config);
        return config;
    }

    private static List<CulometroConfig.LabelBand> readBands(Object raw, String name) {
        List<CulometroConfig.LabelBand> labels = new ArrayList<>();
        if (!(raw instanceof List<?> list)) throw new IllegalArgumentException("Array JSON mancante: " + name);
        for (Object item : list) {
            Map<String,Object> node = object(item, "label band");
            labels.add(new CulometroConfig.LabelBand(decimal(node.get("min"), "0"), string(node.get("label"))));
        }
        return labels;
    }

    public static void validate(CulometroConfig c) {
        if (c.minimumMatches() < 10 || c.minimumMatches() > 40) throw new IllegalArgumentException("minimumMatches deve essere tra 10 e 40");
        range(c.kScale(), "kScale", "3.00", "6.00");
        range(c.secondaryWeight(), "secondaryWeight", "0.10", "0.35");
        range(c.maximumRarityMultiplier(), "maximumRarityMultiplier", "3.00", "6.50");
        if (c.minimumHistoricalOccurrences() < 1 || c.minimumHistoricalOccurrences() > 20) throw new IllegalArgumentException("minimumHistoricalOccurrences deve essere tra 1 e 20");
        for (Map.Entry<String,CulometroConfig.Component> entry : c.components().entrySet()) {
            CulometroConfig.Component component = entry.getValue();
            if (component.min().compareTo(component.max()) > 0) throw new IllegalArgumentException("Range invertito per " + entry.getKey());
            if (component.weight().compareTo(component.min()) < 0 || component.weight().compareTo(component.max()) > 0) {
                throw new IllegalArgumentException("Peso fuori range per " + entry.getKey());
            }
        }
        CulometroConfig.LabelConfiguration labels = c.labelConfiguration();
        if (!List.of("GOLIARDICO", "NEUTRAL", "CUSTOM").contains(labels.preset())) {
            throw new IllegalArgumentException("Preset etichette non supportato: " + labels.preset());
        }
        if (!labels.presetDefaults().containsKey(labels.resetSource())) {
            throw new IllegalArgumentException("resetSource non presente nei presetDefaults: " + labels.resetSource());
        }
        validateBands(labels.bands(), "etichette attive");
        for (Map.Entry<String,List<CulometroConfig.LabelBand>> entry : labels.presetDefaults().entrySet()) {
            validateBands(entry.getValue(), "preset " + entry.getKey());
        }
        if (!labels.customized() && !labels.bands().equals(labels.resetBands())) {
            throw new IllegalArgumentException("Etichette diverse dal preset di reset: impostare customized=true");
        }
    }

    private static void validateBands(List<CulometroConfig.LabelBand> bands, String name) {
        if (bands.isEmpty()) throw new IllegalArgumentException("Definire almeno una fascia per " + name);
        BigDecimal previous = new BigDecimal("101");
        for (CulometroConfig.LabelBand band : bands) {
            if (band.min().compareTo(previous) >= 0) throw new IllegalArgumentException("Fasce non decrescenti per " + name);
            if (band.min().compareTo(BigDecimal.ZERO) < 0 || band.min().compareTo(new BigDecimal("100")) > 0) throw new IllegalArgumentException("Soglia fuori 0-100 per " + name);
            if (band.label() == null || band.label().isBlank() || band.label().length() > 80) throw new IllegalArgumentException("Etichetta vuota o troppo lunga per " + name);
            previous = band.min();
        }
        if (bands.get(bands.size()-1).min().compareTo(BigDecimal.ZERO) != 0) throw new IllegalArgumentException("L'ultima fascia deve partire da 0 per " + name);
    }

    private static void range(BigDecimal value, String name, String min, String max) {
        if (value.compareTo(new BigDecimal(min)) < 0 || value.compareTo(new BigDecimal(max)) > 0) throw new IllegalArgumentException(name + " fuori range");
    }
    @SuppressWarnings("unchecked") private static Map<String,Object> object(Object v, String name){ if(v instanceof Map<?,?> m) return (Map<String,Object>)m; throw new IllegalArgumentException("Oggetto JSON mancante: "+name); }
    private static String string(Object v){ if(v instanceof String s && !s.isBlank()) return s; throw new IllegalArgumentException("Stringa JSON mancante"); }
    private static boolean bool(Object v, boolean d){ return v==null?d:Boolean.TRUE.equals(v); }
    private static int integer(Object v, int d){ if(v==null)return d; return new BigDecimal(String.valueOf(v)).intValue(); }
    private static BigDecimal decimal(Object v, String d){ return new BigDecimal(v==null?d:String.valueOf(v)); }

    private static final class Json {
        private final String text; private int i;
        Json(String text){this.text=text.charAt(0)=='\uFEFF'?text.substring(1):text;}
        Object parse(){skip(); Object v=value(); skip(); if(i!=text.length()) fail(); return v;}
        private Object value(){skip(); if(i>=text.length()) fail(); return switch(text.charAt(i)){case '{'->obj();case '['->arr();case '"'->str();case 't'->lit("true",Boolean.TRUE);case 'f'->lit("false",Boolean.FALSE);case 'n'->lit("null",null);default->num();};}
        private Map<String,Object> obj(){expect('{');Map<String,Object>m=new LinkedHashMap<>();skip();if(peek('}')){i++;return m;}while(true){String k=str();expect(':');m.put(k,value());skip();if(peek('}')){i++;return m;}expect(',');}}
        private List<Object> arr(){expect('[');List<Object>l=new ArrayList<>();skip();if(peek(']')){i++;return l;}while(true){l.add(value());skip();if(peek(']')){i++;return l;}expect(',');}}
        private String str(){expect('"');StringBuilder b=new StringBuilder();while(i<text.length()){char c=text.charAt(i++);if(c=='"')return b.toString();if(c=='\\'){char e=text.charAt(i++);b.append(switch(e){case '"'->'"';case '\\'->'\\';case '/'->'/';case 'b'->'\b';case 'f'->'\f';case 'n'->'\n';case 'r'->'\r';case 't'->'\t';case 'u'->(char)Integer.parseInt(text.substring(i,i+=4),16);default->throw new IllegalArgumentException("Escape JSON");});}else b.append(c);}fail();return "";}
        private Object num(){int s=i;while(i<text.length()&&"-+0123456789.eE".indexOf(text.charAt(i))>=0)i++;return new BigDecimal(text.substring(s,i));}
        private Object lit(String s,Object v){if(!text.startsWith(s,i))fail();i+=s.length();return v;}
        private void skip(){while(i<text.length()&&Character.isWhitespace(text.charAt(i)))i++;}
        private boolean peek(char c){skip();return i<text.length()&&text.charAt(i)==c;}
        private void expect(char c){skip();if(i>=text.length()||text.charAt(i)!=c)fail();i++;}
        private void fail(){throw new IllegalArgumentException("JSON non valido in posizione "+i);}
    }
}
