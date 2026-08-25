package it.alterlega.recordsnext.app.matches;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Esporta il dataset canonico delle partite RecordsNext 2.1.
 * Una partita reale produce due righe, una per ciascuna squadra.
 */
public final class MatchesJsExporter {
    public static final String FILE_NAME = "fcmRecordsNext_Matches.js";
    public static final String GLOBAL_NAME = "window.fcmRecordsNextMatches";

    private MatchesJsExporter() {}

    public static ExportResult export(Path reportsRoot, Path outputFile) throws IOException {
        if (!Files.isDirectory(reportsRoot)) {
            throw new IOException("Cartella report normalizzati non trovata: " + reportsRoot);
        }
        Path parent = outputFile.toAbsolutePath().normalize().getParent();
        if (parent == null) throw new IOException("Directory output Matches non determinabile: " + outputFile);
        Files.createDirectories(parent);

        List<Path> files;
        try (Stream<Path> stream = Files.walk(reportsRoot)) {
            files = stream.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().startsWith("season_normalized_"))
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted()
                    .toList();
        }
        if (files.isEmpty()) throw new IOException("Nessun report season_normalized_*.json in " + reportsRoot);

        List<Object> matches = new ArrayList<>();
        int sourceRows = 0;
        int discardedRestRows = 0;

        for (Path file : files) {
            Object parsed = new JsonParser(Files.readString(file, StandardCharsets.UTF_8), file).parse();
            Map<String,Object> root = object(parsed, file, "radice");
            for (Map<String,Object> row : rows(root.get("partiteSquadra"))) {
                sourceRows++;
                if (!isRealTeamMatch(row)) {
                    discardedRestRows++;
                    continue;
                }
                matches.add(publicRow(row));
            }
        }

        matches.sort(Comparator
                .comparing((Object value) -> string(((Map<?,?>) value).get("seasonId")))
                .thenComparing(value -> string(((Map<?,?>) value).get("competitionId")))
                .thenComparing(value -> string(((Map<?,?>) value).get("matchId")))
                .thenComparing(value -> string(((Map<?,?>) value).get("teamId"))));

        Map<String,Object> metadata = new LinkedHashMap<>();
        metadata.put("source", "RecordsNext normalized reports");
        metadata.put("normalizedFileCount", files.size());
        metadata.put("sourceTeamMatchRowCount", sourceRows);
        metadata.put("discardedRestRowCount", discardedRestRows);
        metadata.put("teamMatchRowCount", matches.size());
        metadata.put("realMatchCount", matches.size() / 2);
        metadata.put("rowModel", "one team per real match; two rows per match");
        metadata.put("resultConvention", "V=win, N=draw, P=loss");

        Map<String,Object> root = new LinkedHashMap<>();
        root.put("schemaVersion", "2.1");
        root.put("familyId", "matches");
        root.put("metadata", metadata);
        root.put("matches", matches);
        root.put("outputStatus", List.of(Map.of(
                "status", "GENERATED_COMPLETE",
                "detail", "Dataset canonico completo delle partite reali, una riga per squadra per incontro"
        )));

        Files.writeString(outputFile, GLOBAL_NAME + " = " + JsonWriter.write(root) + ";\n",
                StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);

        return new ExportResult(files.size(), sourceRows, discardedRestRows, matches.size(), matches.size() / 2, outputFile);
    }

    private static boolean isRealTeamMatch(Map<String,Object> row) {
        return integer(row.get("idSquadra")) > 0
                && integer(row.get("idAvversaria")) > 0
                && !string(row.get("squadra")).isBlank()
                && !string(row.get("avversaria")).isBlank()
                && !string(row.get("idIncontro")).isBlank();
    }

    private static Map<String,Object> publicRow(Map<String,Object> row) {
        BigDecimal pointsFor = number(row.get("puntiFatti"));
        BigDecimal pointsAgainst = number(row.get("puntiSubiti"));

        Map<String,Object> out = new LinkedHashMap<>();
        out.put("seasonId", row.get("stagione"));
        out.put("competitionId", row.get("competizioneStoricaId"));
        out.put("competitionName", row.get("competizioneNome"));
        out.put("matchId", row.get("idIncontro"));
        out.put("round", row.get("giornata"));
        out.put("roundNumber", row.get("giornataDiA"));
        out.put("scorecardUrl", row.get("urlTabellino"));
        out.put("teamId", row.get("idSquadra"));
        out.put("team", row.get("squadra"));
        out.put("opponentId", row.get("idAvversaria"));
        out.put("opponent", row.get("avversaria"));
        out.put("goalsFor", integer(row.get("golFatti")));
        out.put("goalsAgainst", integer(row.get("golSubiti")));
        out.put("result", publicResult(row.get("esito")));
        out.put("pointsFor", pointsFor);
        out.put("pointsAgainst", pointsAgainst);
        out.put("pointsTotal", pointsFor.add(pointsAgainst));
        out.put("regulationGoalsFor", integer(row.get("golRegolamentariFatti")));
        out.put("regulationGoalsAgainst", integer(row.get("golRegolamentariSubiti")));
        return out;
    }

    private static String publicResult(Object value) throws IllegalArgumentException {
        return switch (string(value).trim().toUpperCase()) {
            case "V", "W" -> "V";
            case "P", "N", "D" -> "N";
            case "S", "L" -> "P";
            default -> throw new IllegalArgumentException("Esito normalizzato non previsto: " + value);
        };
    }

    private static Map<String,Object> object(Object value, Path source, String label) throws IOException {
        if (!(value instanceof Map<?,?> raw)) throw new IOException("Oggetto JSON '" + label + "' non valido: " + source);
        Map<String,Object> out = new LinkedHashMap<>();
        for (Map.Entry<?,?> e : raw.entrySet()) out.put(String.valueOf(e.getKey()), e.getValue());
        return out;
    }

    private static List<Map<String,Object>> rows(Object value) {
        List<Map<String,Object>> out = new ArrayList<>();
        if (!(value instanceof List<?> list)) return out;
        for (Object item : list) if (item instanceof Map<?,?> raw) {
            Map<String,Object> map = new LinkedHashMap<>();
            for (Map.Entry<?,?> e : raw.entrySet()) map.put(String.valueOf(e.getKey()), e.getValue());
            out.add(map);
        }
        return out;
    }

    private static String string(Object value) { return value == null ? "" : String.valueOf(value); }
    private static BigDecimal number(Object value) {
        if (value == null || string(value).isBlank()) return BigDecimal.ZERO;
        if (value instanceof BigDecimal b) return b;
        if (value instanceof Number n) return new BigDecimal(n.toString());
        return new BigDecimal(string(value).replace(',', '.'));
    }
    private static int integer(Object value) { return number(value).intValue(); }

    public record ExportResult(int normalizedFileCount, int sourceTeamMatchRowCount,
                               int discardedRestRowCount, int teamMatchRowCount,
                               int realMatchCount, Path outputFile) {}

    private static final class JsonParser {
        private final String text; private final Path source; private int index;
        JsonParser(String text, Path source){this.text=text.charAt(0)=='\uFEFF'?text.substring(1):text;this.source=source;}
        Object parse() throws IOException { skip(); Object v=value(); skip(); if(index!=text.length()) fail("Contenuto dopo JSON"); return v; }
        private Object value() throws IOException { skip(); if(index>=text.length()) fail("Valore mancante"); return switch(text.charAt(index)){
            case '{'->object(); case '['->array(); case '"'->string(); case 't'->literal("true",Boolean.TRUE);
            case 'f'->literal("false",Boolean.FALSE); case 'n'->literal("null",null); default->number();}; }
        private Map<String,Object> object() throws IOException { expect('{'); Map<String,Object> m=new LinkedHashMap<>(); skip(); if(peek('}')){index++;return m;} while(true){String k=string();expect(':');m.put(k,value());skip();if(peek('}')){index++;return m;}expect(',');} }
        private List<Object> array() throws IOException { expect('['); List<Object> l=new ArrayList<>(); skip(); if(peek(']')){index++;return l;} while(true){l.add(value());skip();if(peek(']')){index++;return l;}expect(',');} }
        private String string() throws IOException { expect('"'); StringBuilder b=new StringBuilder(); while(index<text.length()){char c=text.charAt(index++);if(c=='"')return b.toString();if(c=='\\'){if(index>=text.length())fail("Escape troncato");char e=text.charAt(index++);switch(e){case '"'->b.append('"');case '\\'->b.append('\\');case '/'->b.append('/');case 'b'->b.append('\b');case 'f'->b.append('\f');case 'n'->b.append('\n');case 'r'->b.append('\r');case 't'->b.append('\t');case 'u'->{if(index+4>text.length())fail("Unicode troncato");b.append((char)Integer.parseInt(text.substring(index,index+4),16));index+=4;}default->fail("Escape non valido");}}else b.append(c);}fail("Stringa non terminata");return ""; }
        private Object number() throws IOException { int start=index; while(index<text.length()&&"-+0123456789.eE".indexOf(text.charAt(index))>=0)index++; try{return new BigDecimal(text.substring(start,index));}catch(Exception ex){fail("Numero non valido");return null;} }
        private Object literal(String token,Object value) throws IOException { if(!text.startsWith(token,index))fail("Letterale non valido");index+=token.length();return value; }
        private void skip(){while(index<text.length()&&Character.isWhitespace(text.charAt(index)))index++;}
        private boolean peek(char c){skip();return index<text.length()&&text.charAt(index)==c;}
        private void expect(char c)throws IOException{skip();if(index>=text.length()||text.charAt(index)!=c)fail("Atteso '"+c+"'");index++;}
        private void fail(String message)throws IOException{throw new IOException(message+" in "+source+" alla posizione "+index);}
    }

    private static final class JsonWriter {
        static String write(Object value){StringBuilder b=new StringBuilder();append(value,b);return b.toString();}
        private static void append(Object value,StringBuilder b){
            if(value==null){b.append("null");return;}
            if(value instanceof String s){quote(s,b);return;}
            if(value instanceof BigDecimal d){b.append(d.stripTrailingZeros().toPlainString());return;}
            if(value instanceof Number||value instanceof Boolean){b.append(value);return;}
            if(value instanceof Map<?,?> map){b.append('{');boolean first=true;for(Map.Entry<?,?> e:map.entrySet()){if(!first)b.append(',');first=false;quote(String.valueOf(e.getKey()),b);b.append(':');append(e.getValue(),b);}b.append('}');return;}
            if(value instanceof Iterable<?> iterable){b.append('[');boolean first=true;for(Object item:iterable){if(!first)b.append(',');first=false;append(item,b);}b.append(']');return;}
            quote(String.valueOf(value),b);
        }
        private static void quote(String value,StringBuilder b){b.append('"');for(int i=0;i<value.length();i++){char c=value.charAt(i);switch(c){case '"'->b.append("\\\"");case '\\'->b.append("\\\\");case '\b'->b.append("\\b");case '\f'->b.append("\\f");case '\n'->b.append("\\n");case '\r'->b.append("\\r");case '\t'->b.append("\\t");default->{if(c<0x20)b.append(String.format("\\u%04x",(int)c));else b.append(c);}}}b.append('"');}
    }
}
