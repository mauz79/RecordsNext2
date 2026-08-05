package it.alterlega.recordsnext.app.thresholds;

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

/** Genera l'output 2.0 Soglie e Fortuna dai JSON normalizzati. */
public final class ThresholdsLuckFamilyJsExporter {
    public static final String FILE_NAME = "fcmRecordsNext_ThresholdsLuck.js";
    public static final String GLOBAL_NAME = "window.fcmRecordsNextThresholdsLuck";

    private ThresholdsLuckFamilyJsExporter() {
    }

    public static ExportResult export(Path reportsRoot, Path outputFile) throws IOException {
        if (!Files.isDirectory(reportsRoot)) {
            throw new IOException("Cartella report normalizzati non trovata: " + reportsRoot);
        }
        Path parent = outputFile.toAbsolutePath().normalize().getParent();
        if (parent == null) throw new IOException("Directory output Soglie non determinabile: " + outputFile);
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

        List<Object> events = new ArrayList<>();
        Map<String, Aggregate> aggregates = new LinkedHashMap<>();
        int matchRows = 0;
        for (Path file : files) {
            Object parsed = new JsonParser(Files.readString(file, StandardCharsets.UTF_8), file).parse();
            Map<String,Object> root = object(parsed, file, "radice");
            List<Map<String,Object>> matches = rows(root.get("partiteSquadra"));
            List<Map<String,Object>> bands = rows(root.get("fasceGolDettaglio"));
            matchRows += matches.size();
            for (Map<String,Object> match : matches) {
                BigDecimal score = number(match.get("puntiFatti"));
                BigDecimal scoreAgainst = number(match.get("puntiSubiti"));
                int goalsFor = integer(match.get("golFatti"));
                int goalsAgainst = integer(match.get("golSubiti"));
                String result = string(match.get("esito")).trim().toUpperCase();
                Band current = currentBand(bands, score);
                BigDecimal nextMin = nextBandMin(bands, score);
                BigDecimal distance = nextMin == null ? null : nextMin.subtract(score);
                BigDecimal bandSurplus = current == null ? BigDecimal.ZERO : score.subtract(current.min());

                if (current != null && score.compareTo(current.min()) == 0) {
                    addEvent(events, aggregates, match, "EXACT_THRESHOLD", "NEUTRAL", BigDecimal.ZERO,
                            BigDecimal.ZERO, "Punteggio esattamente sul minimo della fascia gol");
                }
                if (isWin(result) && current != null && score.compareTo(current.min()) == 0
                        && goalsFor == goalsAgainst + 1) {
                    addEvent(events, aggregates, match, "JUST_ENOUGH", "FAVOURABLE", BigDecimal.ZERO,
                            BigDecimal.ZERO, "Vittoria con punteggio sul minimo della fascia decisiva");
                }
                if (distance != null && distance.compareTo(new BigDecimal("0.5")) == 0) {
                    if (isDraw(result) && goalsFor == goalsAgainst) {
                        addEvent(events, aggregates, match, "MISSED_WIN_HALF_POINT", "UNFAVOURABLE", distance,
                                bandSurplus, "Mezzo punto dalla fascia successiva che avrebbe prodotto la vittoria");
                    } else if (isLoss(result) && goalsFor + 1 == goalsAgainst) {
                        addEvent(events, aggregates, match, "LOSS_BY_A_WHISKER", "UNFAVOURABLE", distance,
                                bandSurplus, "Mezzo punto dalla fascia successiva che avrebbe prodotto il pareggio");
                    }
                }
                if (isDraw(result) && goalsFor == goalsAgainst) {
                    if (score.compareTo(scoreAgainst) < 0) {
                        addEvent(events, aggregates, match, "MIRACLE_DRAW", "FAVOURABLE",
                                distance, bandSurplus,
                                "Pareggio ottenuto con punteggio inferiore all'avversaria nella stessa fascia gol");
                    } else if (score.compareTo(scoreAgainst) > 0) {
                        addEvent(events, aggregates, match, "TIGHT_DRAW", "UNFAVOURABLE",
                                distance, bandSurplus,
                                "Pareggio nonostante un punteggio superiore all'avversaria nella stessa fascia gol");
                    }
                }
                if (isWin(result) && goalsFor == goalsAgainst + 1) {
                    addEvent(events, aggregates, match, "ONE_GOAL_WIN", "FAVOURABLE",
                            distance, bandSurplus, "Vittoria con un solo gol di margine");
                } else if (isLoss(result) && goalsFor + 1 == goalsAgainst) {
                    addEvent(events, aggregates, match, "ONE_GOAL_LOSS", "UNFAVOURABLE",
                            distance, bandSurplus, "Sconfitta con un solo gol di margine");
                }
                if (current != null && bandSurplus.compareTo(BigDecimal.ZERO) > 0) {
                    addEvent(events, aggregates, match, "UNUSED_BAND_POINTS", "NEUTRAL",
                            distance, bandSurplus,
                            "Punti oltre il minimo della fascia che non hanno prodotto un gol aggiuntivo");
                }
            }
        }

        events.sort(Comparator.comparing(value -> string(((Map<?,?>) value).get("seasonId")))
                .thenComparing(value -> string(((Map<?,?>) value).get("competitionId")))
                .thenComparing(value -> string(((Map<?,?>) value).get("matchId")))
                .thenComparing(value -> string(((Map<?,?>) value).get("teamId")))
                .thenComparing(value -> string(((Map<?,?>) value).get("eventType"))));

        List<Object> teamAggregates = new ArrayList<>();
        for (Aggregate a : aggregates.values()) teamAggregates.add(a.toMap());
        teamAggregates.sort(Comparator.comparing(value -> string(((Map<?,?>) value).get("seasonId")))
                .thenComparing(value -> string(((Map<?,?>) value).get("team"))));

        Map<String,Object> metadata = new LinkedHashMap<>();
        metadata.put("source", "RecordsNext 1.0.2 normalized reports");
        metadata.put("normalizedFileCount", files.size());
        metadata.put("teamMatchRowCount", matchRows);
        metadata.put("eventCount", events.size());
        metadata.put("implementedEventTypes", List.of(
                "EXACT_THRESHOLD", "JUST_ENOUGH", "MISSED_WIN_HALF_POINT", "LOSS_BY_A_WHISKER",
                "MIRACLE_DRAW", "TIGHT_DRAW", "ONE_GOAL_WIN", "ONE_GOAL_LOSS",
                "UNUSED_BAND_POINTS"));
        metadata.put("culometroGenerated", false);

        Map<String,Object> root = new LinkedHashMap<>();
        root.put("schemaVersion", "2.0");
        root.put("familyId", "thresholds-luck");
        root.put("metadata", metadata);
        root.put("events", events);
        root.put("seasonAggregates", teamAggregates);
        root.put("globalAggregates", List.of());
        root.put("absoluteOccurrences", List.of());
        root.put("outputStatus", List.of(Map.of(
                "status", "GENERATED_COMPLETE",
                "detail", "Indicatori oggettivi completi basati su esiti, fasce gol, margini e punti inutilizzati. Culometro escluso."
        )));

        Files.writeString(outputFile, GLOBAL_NAME + " = " + JsonWriter.write(root) + ";\n",
                StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        return new ExportResult(files.size(), matchRows, events.size(), teamAggregates.size(), outputFile);
    }

    private static void addEvent(List<Object> events, Map<String,Aggregate> aggregates,
                                 Map<String,Object> match, String type, String direction,
                                 BigDecimal distance, BigDecimal bandSurplus, String detail) {
        Map<String,Object> event = new LinkedHashMap<>();
        event.put("eventId", "threshold:" + string(match.get("stagione")) + ":" + string(match.get("idIncontro"))
                + ":" + string(match.get("idSquadra")) + ":" + type.toLowerCase());
        event.put("eventType", type);
        event.put("direction", direction);
        event.put("seasonId", match.get("stagione"));
        event.put("competitionId", match.get("competizioneStoricaId"));
        event.put("competitionName", match.get("competizioneNome"));
        event.put("matchId", match.get("idIncontro"));
        event.put("round", match.get("giornata"));
        event.put("serieARound", match.get("giornataDiA"));
        event.put("scorecardUrl", match.get("urlTabellino"));
        event.put("teamId", match.get("idSquadra"));
        event.put("team", match.get("squadra"));
        event.put("opponentId", match.get("idAvversaria"));
        event.put("opponent", match.get("avversaria"));
        event.put("scoreFor", match.get("puntiFatti"));
        event.put("scoreAgainst", match.get("puntiSubiti"));
        event.put("goalsFor", match.get("golFatti"));
        event.put("goalsAgainst", match.get("golSubiti"));
        event.put("result", match.get("esito"));
        event.put("distanceToNextThreshold", distance);
        event.put("unusedBandPoints", bandSurplus);
        event.put("detail", detail);
        events.add(event);

        String key = string(match.get("stagione")) + "|" + string(match.get("idSquadra"));
        aggregates.computeIfAbsent(key, ignored -> new Aggregate(match))
                .add(direction, type, bandSurplus);
    }

    private static Band currentBand(List<Map<String,Object>> bands, BigDecimal score) {
        for (Map<String,Object> row : bands) {
            BigDecimal min = number(row.get("min"));
            BigDecimal max = number(row.get("max"));
            if (score.compareTo(min) >= 0 && score.compareTo(max) <= 0) return new Band(min, max, integer(row.get("gol")));
        }
        return null;
    }

    private static BigDecimal nextBandMin(List<Map<String,Object>> bands, BigDecimal score) {
        BigDecimal next = null;
        for (Map<String,Object> row : bands) {
            BigDecimal min = number(row.get("min"));
            if (min.compareTo(score) > 0 && (next == null || min.compareTo(next) < 0)) next = min;
        }
        return next;
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
        return new BigDecimal(string(value).replace(',', '.'));
    }
    private static int integer(Object value) { return number(value).intValue(); }
    private static boolean isWin(String result) { return "V".equals(result); }
    private static boolean isDraw(String result) { return "P".equals(result) || "N".equals(result); }
    private static boolean isLoss(String result) { return "S".equals(result); }

    public record ExportResult(int normalizedFileCount, int teamMatchRowCount, int eventCount,
                               int aggregateCount, Path outputFile) {}
    private record Band(BigDecimal min, BigDecimal max, int goals) {}

    private static final class Aggregate {
        private final Object seasonId, teamId, team;
        private int favourable, unfavourable, neutral;
        private BigDecimal unusedBandPoints = BigDecimal.ZERO;
        private final Map<String,Integer> byType = new LinkedHashMap<>();
        Aggregate(Map<String,Object> match) { seasonId=match.get("stagione"); teamId=match.get("idSquadra"); team=match.get("squadra"); }
        void add(String direction, String type, BigDecimal surplus) {
            switch (direction) { case "FAVOURABLE" -> favourable++; case "UNFAVOURABLE" -> unfavourable++; default -> neutral++; }
            byType.merge(type, 1, Integer::sum);
            if ("UNUSED_BAND_POINTS".equals(type) && surplus != null) {
                unusedBandPoints = unusedBandPoints.add(surplus);
            }
        }
        Map<String,Object> toMap() {
            Map<String,Object> out = new LinkedHashMap<>();
            out.put("seasonId", seasonId); out.put("teamId", teamId); out.put("team", team);
            out.put("favourableEvents", favourable); out.put("unfavourableEvents", unfavourable);
            out.put("neutralEvents", neutral); out.put("luckBalance", favourable - unfavourable);
            out.put("unusedBandPoints", unusedBandPoints);
            out.put("eventsByType", byType); return out;
        }
    }

    private static final class JsonParser {
        private final String text; private final Path source; private int index;
        JsonParser(String text, Path source){this.text=text;this.source=source;}
        Object parse() throws IOException { skip(); Object v=value(); skip(); if(index!=text.length()) fail("Contenuto dopo JSON"); return v; }
        private Object value() throws IOException { skip(); if(index>=text.length()) fail("Valore mancante"); return switch(text.charAt(index)){
            case '{'->object(); case '['->array(); case '"'->string(); case 't'->literal("true",Boolean.TRUE);
            case 'f'->literal("false",Boolean.FALSE); case 'n'->literal("null",null); default->number();}; }
        private Map<String,Object> object() throws IOException { expect('{'); Map<String,Object> m=new LinkedHashMap<>(); skip(); if(peek('}')){index++;return m;}
            while(true){String k=string();expect(':');m.put(k,value());skip();if(peek('}')){index++;return m;}expect(',');} }
        private List<Object> array() throws IOException { expect('['); List<Object> a=new ArrayList<>(); skip(); if(peek(']')){index++;return a;}
            while(true){a.add(value());skip();if(peek(']')){index++;return a;}expect(',');} }
        private String string() throws IOException { expect('"'); StringBuilder b=new StringBuilder(); while(index<text.length()){char c=text.charAt(index++);if(c=='"')return b.toString();
            if(c!='\\'){b.append(c);continue;} if(index>=text.length())fail("Escape incompleto");char e=text.charAt(index++);switch(e){case '"','\\','/'->b.append(e);case 'b'->b.append('\b');case 'f'->b.append('\f');case 'n'->b.append('\n');case 'r'->b.append('\r');case 't'->b.append('\t');case 'u'->b.append(unicode());default->fail("Escape non valido");}}fail("Stringa non terminata");return null; }
        private char unicode() throws IOException { if(index+4>text.length())fail("Unicode incompleto");String h=text.substring(index,index+4);index+=4;try{return(char)Integer.parseInt(h,16);}catch(NumberFormatException ex){fail("Unicode non valido");return 0;} }
        private Object literal(String s,Object v)throws IOException{if(!text.startsWith(s,index))fail("Token non valido");index+=s.length();return v;}
        private BigDecimal number() throws IOException {int s=index;if(peek('-'))index++;while(index<text.length()&&Character.isDigit(text.charAt(index)))index++;if(peek('.')){index++;while(index<text.length()&&Character.isDigit(text.charAt(index)))index++;}if(peek('e')||peek('E')){index++;if(peek('+')||peek('-'))index++;while(index<text.length()&&Character.isDigit(text.charAt(index)))index++;}if(s==index)fail("Numero non valido");try{return new BigDecimal(text.substring(s,index));}catch(NumberFormatException ex){fail("Numero non valido");return null;}}
        private void expect(char c)throws IOException{skip();if(index>=text.length()||text.charAt(index)!=c)fail("Atteso '"+c+"'");index++;}
        private boolean peek(char c){return index<text.length()&&text.charAt(index)==c;} private void skip(){while(index<text.length()&&Character.isWhitespace(text.charAt(index)))index++;}
        private void fail(String m)throws IOException{throw new IOException(m+" in "+source+" alla posizione "+index);}
    }
    private static final class JsonWriter {
        static String write(Object v){StringBuilder b=new StringBuilder();append(b,v);return b.toString();}
        private static void append(StringBuilder b,Object v){if(v==null){b.append("null");return;}if(v instanceof String s){b.append('"').append(escape(s)).append('"');return;}
            if(v instanceof Boolean||v instanceof Number){b.append(v);return;}if(v instanceof Map<?,?> m){b.append('{');boolean f=true;for(Map.Entry<?,?>e:m.entrySet()){if(!f)b.append(',');f=false;b.append('"').append(escape(String.valueOf(e.getKey()))).append("\":");append(b,e.getValue());}b.append('}');return;}
            if(v instanceof List<?> l){b.append('[');for(int i=0;i<l.size();i++){if(i>0)b.append(',');append(b,l.get(i));}b.append(']');return;}throw new IllegalArgumentException("Tipo JSON non supportato: "+v.getClass());}
        private static String escape(String s){StringBuilder b=new StringBuilder();for(int i=0;i<s.length();i++){char c=s.charAt(i);switch(c){case '\\'->b.append("\\\\");case '"'->b.append("\\\"");case '\n'->b.append("\\n");case '\r'->b.append("\\r");case '\t'->b.append("\\t");default->{if(c<0x20)b.append(String.format("\\u%04x",(int)c));else b.append(c);}}}return b.toString();}
    }
}
