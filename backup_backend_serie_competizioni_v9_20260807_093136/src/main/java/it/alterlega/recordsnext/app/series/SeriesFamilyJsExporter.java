package it.alterlega.recordsnext.app.series;

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

/** Genera l'output familiare RecordsNext 2.0 dedicato esclusivamente alle serie cronologiche. */
public final class SeriesFamilyJsExporter {
    public static final String FILE_NAME = "fcmRecordsNext_Series.js";
    public static final String GLOBAL_NAME = "window.fcmRecordsNextSeries";

    private static final String RECORD_PREFIX = "season_records_";
    private static final String RECORD_SUFFIX = ".json";
    private static final List<String> AVAILABLE_SECTIONS = List.of(
            "serieVittorie",
            "seriePareggi",
            "serieSconfitte",
            "serieSenzaSconfitte",
            "serieSenzaVittorie",
            "cleanSheetPortiereSerieSquadre"
    );

    private SeriesFamilyJsExporter() {
    }

    public static ExportResult export(Path archiveRoot, Path outputFile) throws IOException {
        if (!Files.isDirectory(archiveRoot)) {
            throw new IOException("Archivio stagioni non trovato: " + archiveRoot);
        }

        Path parent = outputFile.toAbsolutePath().normalize().getParent();
        if (parent == null) {
            throw new IOException("Directory output Serie non determinabile: " + outputFile);
        }
        Files.createDirectories(parent);

        List<Object> filteredEntries = new ArrayList<>();
        Set<String> seasonsWithSeries = new LinkedHashSet<>();
        Set<String> generatedSections = new LinkedHashSet<>();
        int sectionCount = 0;

        for (Path seasonDirectory : listSeasonDirectories(archiveRoot)) {
            String season = seasonDirectory.getFileName().toString();
            for (Path recordFile : listRecordFiles(seasonDirectory)) {
                String json = Files.readString(recordFile, StandardCharsets.UTF_8);
                Object parsed = new JsonParser(json, recordFile).parse();
                if (!(parsed instanceof Map<?, ?> rawRoot)) {
                    throw new IOException("Radice JSON non valida: " + recordFile);
                }
                Map<String, Object> rootData = stringMap(rawRoot);
                Object recordsValue = rootData.get("records");
                if (!(recordsValue instanceof Map<?, ?> rawRecords)) {
                    continue;
                }
                Map<String, Object> records = stringMap(rawRecords);

                Map<String, Object> selected = new LinkedHashMap<>();
                for (String section : AVAILABLE_SECTIONS) {
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
                seasonsWithSeries.add(season);
            }
        }

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("source", "RecordsNext 1.0.2 records archive");
        metadata.put("seasonCount", seasonsWithSeries.size());
        metadata.put("entryCount", filteredEntries.size());
        metadata.put("sectionCount", sectionCount);
        metadata.put("availableSections", AVAILABLE_SECTIONS);
        metadata.put("generatedSections", new ArrayList<>(generatedSections));

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("schemaVersion", "2.0");
        root.put("familyId", "series");
        root.put("metadata", metadata);
        root.put("events", List.of());
        root.put("seasonAggregates", filteredEntries);
        root.put("globalAggregates", List.of());
        root.put("absoluteOccurrences", List.of());
        root.put("outputStatus", List.of(Map.of(
                "status", "GENERATED_COMPLETE",
                "detail", "Famiglia Serie separata dai record Classici"
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
                seasonsWithSeries.size(),
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

    private static String competitionName(Map<String, Object> selected, String fallback) {
        for (Object sectionValue : selected.values()) {
            if (!(sectionValue instanceof List<?> rows)) {
                continue;
            }
            for (Object rowValue : rows) {
                if (!(rowValue instanceof Map<?, ?> row)) {
                    continue;
                }
                Object value = row.get("competizioneNome");
                if (value != null && !String.valueOf(value).isBlank()) {
                    return String.valueOf(value);
                }
            }
        }
        return displayCompetitionName(fallback);
    }

    private static String displayCompetitionName(String competitionId) {
        if (competitionId == null || competitionId.isBlank()) {
            return "";
        }
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
        JsonParser(String text, Path source) { this.text = text; this.source = source; }
        Object parse() throws IOException {
            skipWhitespace(); Object value = parseValue(); skipWhitespace();
            if (index != text.length()) fail("Contenuto dopo la fine del JSON");
            return value;
        }
        private Object parseValue() throws IOException {
            skipWhitespace(); if (index >= text.length()) fail("Valore mancante");
            return switch (text.charAt(index)) {
                case '{' -> parseObject(); case '[' -> parseArray(); case '"' -> parseString();
                case 't' -> parseLiteral("true", Boolean.TRUE); case 'f' -> parseLiteral("false", Boolean.FALSE);
                case 'n' -> parseLiteral("null", null); default -> parseNumber();
            };
        }
        private Map<String,Object> parseObject() throws IOException {
            expect('{'); Map<String,Object> result=new LinkedHashMap<>(); skipWhitespace();
            if (peek('}')) { index++; return result; }
            while (true) { String key=parseString(); expect(':'); result.put(key,parseValue()); skipWhitespace();
                if (peek('}')) { index++; return result; } expect(','); }
        }
        private List<Object> parseArray() throws IOException {
            expect('['); List<Object> result=new ArrayList<>(); skipWhitespace();
            if (peek(']')) { index++; return result; }
            while (true) { result.add(parseValue()); skipWhitespace(); if (peek(']')) { index++; return result; } expect(','); }
        }
        private String parseString() throws IOException {
            expect('"'); StringBuilder result=new StringBuilder();
            while (index<text.length()) { char ch=text.charAt(index++); if (ch=='"') return result.toString();
                if (ch!='\\') { result.append(ch); continue; } if (index>=text.length()) fail("Escape incompleto");
                char esc=text.charAt(index++); switch(esc) { case '"','\\','/' -> result.append(esc); case 'b'->result.append('\b');
                    case 'f'->result.append('\f'); case 'n'->result.append('\n'); case 'r'->result.append('\r'); case 't'->result.append('\t');
                    case 'u'->result.append(parseUnicode()); default->fail("Escape non valido"); } }
            fail("Stringa non terminata"); return null;
        }
        private char parseUnicode() throws IOException { if(index+4>text.length()) fail("Unicode incompleto"); String h=text.substring(index,index+4); index+=4;
            try{return(char)Integer.parseInt(h,16);}catch(NumberFormatException ex){fail("Unicode non valido");return 0;} }
        private Object parseLiteral(String literal,Object value)throws IOException{if(!text.startsWith(literal,index))fail("Token non valido");index+=literal.length();return value;}
        private BigDecimal parseNumber() throws IOException { int start=index; if(peek('-'))index++; while(index<text.length()&&Character.isDigit(text.charAt(index)))index++;
            if(peek('.')){index++;while(index<text.length()&&Character.isDigit(text.charAt(index)))index++;} if(peek('e')||peek('E')){index++;if(peek('+')||peek('-'))index++;while(index<text.length()&&Character.isDigit(text.charAt(index)))index++;}
            if(start==index)fail("Numero non valido"); try{return new BigDecimal(text.substring(start,index));}catch(NumberFormatException ex){fail("Numero non valido");return null;} }
        private void expect(char expected)throws IOException{skipWhitespace();if(index>=text.length()||text.charAt(index)!=expected)fail("Atteso '"+expected+"'");index++;}
        private boolean peek(char value){return index<text.length()&&text.charAt(index)==value;}
        private void skipWhitespace(){while(index<text.length()&&Character.isWhitespace(text.charAt(index)))index++;}
        private void fail(String message)throws IOException{throw new IOException(message+" in "+source+" alla posizione "+index);}
    }

    private static final class JsonWriter {
        static String write(Object value){StringBuilder out=new StringBuilder();append(out,value);return out.toString();}
        private static void append(StringBuilder out,Object value){
            if(value==null){out.append("null");return;} if(value instanceof String s){out.append('"').append(escape(s)).append('"');return;}
            if(value instanceof Boolean||value instanceof BigDecimal||value instanceof Number){out.append(value);return;}
            if(value instanceof Map<?,?> map){out.append('{');boolean first=true;for(Map.Entry<?,?> e:map.entrySet()){if(!first)out.append(',');first=false;out.append('"').append(escape(String.valueOf(e.getKey()))).append("\":");append(out,e.getValue());}out.append('}');return;}
            if(value instanceof List<?> list){out.append('[');for(int i=0;i<list.size();i++){if(i>0)out.append(',');append(out,list.get(i));}out.append(']');return;}
            throw new IllegalArgumentException("Tipo JSON non supportato: "+value.getClass());
        }
        private static String escape(String value){StringBuilder e=new StringBuilder(value.length()+16);for(int i=0;i<value.length();i++){char ch=value.charAt(i);switch(ch){case '\\'->e.append("\\\\");case '"'->e.append("\\\"");case '\b'->e.append("\\b");case '\f'->e.append("\\f");case '\n'->e.append("\\n");case '\r'->e.append("\\r");case '\t'->e.append("\\t");default->{if(ch<0x20)e.append(String.format("\\u%04x",(int)ch));else e.append(ch);}}}return e.toString();}
    }
}
