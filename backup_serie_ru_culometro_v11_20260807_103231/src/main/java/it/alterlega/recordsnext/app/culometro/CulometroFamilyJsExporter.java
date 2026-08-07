package it.alterlega.recordsnext.app.culometro;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CulometroFamilyJsExporter {
    public static final String FILE_NAME = "fcmRecordsNext_Culometro.js";
    public static final String GLOBAL_NAME = "window.fcmRecordsNextCulometro";
    private static final String THRESHOLDS_PREFIX = "window.fcmRecordsNextThresholdsLuck = ";
    private static final String RU_PREFIX = "window.fcmRecordsNextRU = ";

    private CulometroFamilyJsExporter() {}

    public static ExportResult export(Path thresholdsJs, Path ruJs, Path configFile, Path outputFile) throws IOException {
        CulometroConfig config = CulometroConfigLoader.load(configFile);
        if (!config.enabled()) throw new IOException("Culometro richiesto ma config.enabled=false: " + configFile);

        Map<String,Object> thresholds = parseAssignment(thresholdsJs, THRESHOLDS_PREFIX);
        List<Map<String,Object>> thresholdEvents = rows(thresholds.get("events"));
        List<Map<String,Object>> ruEvents = Files.isRegularFile(ruJs)
                ? findRuEvents(parseAssignment(ruJs, RU_PREFIX))
                : List.of();

        List<Event> candidates = new ArrayList<>();
        for (Map<String,Object> event : thresholdEvents) {
            String type = text(event.get("eventType"));
            CulometroConfig.Component component = config.components().get(type);
            if (component == null || !component.enabled()) continue;
            int direction = switch (text(event.get("direction"))) {
                case "FAVOURABLE" -> 1;
                case "UNFAVOURABLE" -> -1;
                default -> 0;
            };
            if (direction == 0) continue;
            candidates.add(Event.fromThreshold(event, type, direction, component.weight()));
        }
        for (Map<String,Object> event : ruEvents) {
            CulometroConfig.Component component = config.components().get("RU_DECISIVE");
            if (component == null || !component.enabled()) continue;
            Event ru = Event.fromRu(event, component.weight());
            if (ru != null) candidates.add(ru);
        }

        Set<String> performances = new LinkedHashSet<>();
        Map<String,Integer> occurrences = new HashMap<>();
        for (Event event : candidates) {
            performances.add(event.performanceKey());
            occurrences.merge(event.type(), 1, Integer::sum);
        }
        int denominator = Math.max(1, performances.size());

        Map<String,List<Event>> grouped = new LinkedHashMap<>();
        for (Event event : candidates) grouped.computeIfAbsent(event.performanceKey(), ignored -> new ArrayList<>()).add(event);

        List<Map<String,Object>> scoredEvents = new ArrayList<>();
        Map<String,TeamAggregate> teamAggregates = new LinkedHashMap<>();
        for (List<Event> group : grouped.values()) {
            group.sort(Comparator.comparing((Event e) -> e.weight().abs()).reversed().thenComparing(Event::type));
            for (int index = 0; index < group.size(); index++) {
                Event event = group.get(index);
                BigDecimal overlap = index == 0 ? BigDecimal.ONE : index == 1 ? config.secondaryWeight() : BigDecimal.ZERO;
                String level = index == 0 ? "PRIMARY" : index == 1 ? "SECONDARY" : "TAG";
                BigDecimal rarity = rarityMultiplier(occurrences.getOrDefault(event.type(), 1), denominator, config);
                BigDecimal contribution = event.weight()
                        .multiply(BigDecimal.valueOf(event.direction()))
                        .multiply(rarity)
                        .multiply(overlap)
                        .setScale(6, RoundingMode.HALF_UP);
                Map<String,Object> out = event.toMap();
                out.put("level", level);
                out.put("rarityMultiplier", rarity);
                out.put("overlapMultiplier", overlap);
                out.put("contribution", contribution);
                scoredEvents.add(out);
                teamAggregates.computeIfAbsent(event.teamKey(), ignored -> new TeamAggregate(event)).add(contribution, level);
            }
        }

        double rawMean = teamAggregates.values().stream().mapToDouble(TeamAggregate::perMatch).average().orElse(0.0);
        List<Map<String,Object>> ranking = new ArrayList<>();
        for (TeamAggregate aggregate : teamAggregates.values()) ranking.add(aggregate.finish(rawMean, config));
        ranking.sort(Comparator.comparingDouble(value -> -number(value.get("index")).doubleValue()));

        Map<String,Object> root = new LinkedHashMap<>();
        root.put("schemaVersion", "2.0");
        root.put("familyId", "culometro");
        root.put("metadata", Map.of(
                "engineVersion", "2.0",
                "thresholdEventCount", thresholdEvents.size(),
                "ruCandidateCount", ruEvents.size(),
                "scoredEventCount", scoredEvents.size(),
                "performanceCount", performances.size(),
                "historicalMeanPerMatch", BigDecimal.valueOf(rawMean).setScale(6, RoundingMode.HALF_UP),
                "ruPolicy", "RU pesa solo con evidenza di decisivita o cambio esito; la sola presenza non incide"
        ));
        root.put("configuration", Map.of(
                "minimumMatches", config.minimumMatches(),
                "kScale", config.kScale(),
                "secondaryWeight", config.secondaryWeight(),
                "maximumRarityMultiplier", config.maximumRarityMultiplier(),
                "labelPreset", config.labelConfiguration().preset(),
                "labelsCustomized", config.labelConfiguration().customized(),
                "labelResetSource", config.labelConfiguration().resetSource(),
                "labels", config.labels(),
                "labelPresetDefaults", config.labelConfiguration().presetDefaults()
        ));
        root.put("events", scoredEvents);
        root.put("ranking", ranking);
        root.put("outputStatus", List.of(Map.of("status", "GENERATED_COMPLETE", "detail", "Culometro configurabile con pesi vincolati, rarita, affidabilita, etichette editabili e RU prudenziale")));

        Path parent = outputFile.toAbsolutePath().normalize().getParent();
        if (parent == null) throw new IOException("Directory output Culometro non determinabile");
        Files.createDirectories(parent);
        Files.writeString(outputFile, GLOBAL_NAME + " = " + Json.write(root) + ";\n", StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        return new ExportResult(scoredEvents.size(), ranking.size(), outputFile);
    }

    private static BigDecimal rarityMultiplier(int occurrences, int denominator, CulometroConfig config) {
        int safeOccurrences = Math.max(occurrences, config.minimumHistoricalOccurrences());
        double frequency = (double) safeOccurrences / denominator;
        double value = Math.sqrt(0.10 / Math.max(frequency, 0.000001));
        value = Math.max(1.0, Math.min(config.maximumRarityMultiplier().doubleValue(), value));
        return BigDecimal.valueOf(value).setScale(6, RoundingMode.HALF_UP);
    }

    private static Map<String,Object> parseAssignment(Path file, String prefix) throws IOException {
        String js = Files.readString(file, StandardCharsets.UTF_8).trim();
        if (!js.startsWith(prefix) || !js.endsWith(";")) throw new IOException("Formato JS inatteso: " + file);
        Object parsed = new Json(js.substring(prefix.length(), js.length()-1).trim()).parse();
        return object(parsed);
    }

    private static List<Map<String,Object>> findRuEvents(Object root) {
        List<Map<String,Object>> out = new ArrayList<>();
        walk(root, out);
        return out;
    }
    private static void walk(Object value, List<Map<String,Object>> out) {
        if (value instanceof Map<?,?> raw) {
            Map<String,Object> map = object(raw);
            if (map.containsKey("idIncontro") && map.containsKey("idSquadra") && map.containsKey("numeroRU")) out.add(map);
            for (Object child : map.values()) walk(child, out);
        } else if (value instanceof List<?> list) for (Object child : list) walk(child, out);
    }

    @SuppressWarnings("unchecked") private static Map<String,Object> object(Object v){ if(v instanceof Map<?,?> m)return (Map<String,Object>)m; throw new IllegalArgumentException("Oggetto JSON atteso"); }
    private static List<Map<String,Object>> rows(Object v){ List<Map<String,Object>> out=new ArrayList<>(); if(v instanceof List<?> l)for(Object x:l)if(x instanceof Map<?,?>)out.add(object(x)); return out; }
    private static String text(Object v){ return v==null?"":String.valueOf(v); }
    private static BigDecimal number(Object v){ if(v==null||text(v).isBlank())return BigDecimal.ZERO; return v instanceof BigDecimal b?b:new BigDecimal(text(v).replace(',','.')); }

    private record Event(String type,int direction,BigDecimal weight,String seasonId,String competitionId,String matchId,String teamId,String team,String opponent,String url,String detail) {
        static Event fromThreshold(Map<String,Object> m,String type,int direction,BigDecimal weight){return new Event(type,direction,weight,text(m.get("seasonId")),text(m.get("competitionId")),text(m.get("matchId")),text(m.get("teamId")),text(m.get("team")),text(m.get("opponent")),text(m.get("scorecardUrl")),text(m.get("detail")));}
        static Event fromRu(Map<String,Object> m,BigDecimal weight){
            boolean decisive = Boolean.TRUE.equals(m.get("decisivo")) || Boolean.TRUE.equals(m.get("ruDecisiva")) || !text(m.get("esitoSenzaRU")).isBlank() && !text(m.get("esitoSenzaRU")).equalsIgnoreCase(text(m.get("esito")));
            if(!decisive)return null;
            String result=text(m.get("esito")).toUpperCase(); int direction="V".equals(result)?1:("S".equals(result)||"P".equals(result)?-1:0); if(direction==0)return null;
            return new Event("RU_DECISIVE",direction,weight,text(m.get("stagione")),text(m.get("competizione")),text(m.get("idIncontro")),text(m.get("idSquadra")),text(m.get("squadra")),text(m.get("avversaria")),text(m.get("urlTabellino")),"RU decisiva dimostrata dal dataset");
        }
        String performanceKey(){return seasonId+"|"+matchId+"|"+teamId;}
        String teamKey(){return seasonId+"|"+teamId;}
        Map<String,Object> toMap(){Map<String,Object>m=new LinkedHashMap<>();m.put("eventType",type);m.put("direction",direction>0?"FAVOURABLE":"UNFAVOURABLE");m.put("seasonId",seasonId);m.put("competitionId",competitionId);m.put("matchId",matchId);m.put("teamId",teamId);m.put("team",team);m.put("opponent",opponent);m.put("scorecardUrl",url);m.put("detail",detail);m.put("componentWeight",weight);return m;}
    }

    private static final class TeamAggregate {
        final String seasonId,teamId,team; BigDecimal total=BigDecimal.ZERO; int matches,primary,secondary;
        TeamAggregate(Event e){seasonId=e.seasonId();teamId=e.teamId();team=e.team();}
        void add(BigDecimal contribution,String level){total=total.add(contribution);matches++;if("PRIMARY".equals(level))primary++;else if("SECONDARY".equals(level))secondary++;}
        double perMatch(){return matches==0?0:total.doubleValue()/matches;}
        Map<String,Object> finish(double mean,CulometroConfig c){double centered=perMatch()-mean;double raw=50.0+50.0*Math.tanh(centered/c.kScale().doubleValue());double reliability=Math.min(1.0,(double)matches/c.minimumMatches());double index=50.0+(raw-50.0)*reliability;index=Math.max(0,Math.min(100,index));Map<String,Object>m=new LinkedHashMap<>();m.put("seasonId",seasonId);m.put("teamId",teamId);m.put("team",team);m.put("matches",matches);m.put("primaryEvents",primary);m.put("secondaryEvents",secondary);m.put("totalContribution",total.setScale(6,RoundingMode.HALF_UP));m.put("perMatch",BigDecimal.valueOf(perMatch()).setScale(6,RoundingMode.HALF_UP));m.put("reliability",BigDecimal.valueOf(reliability).setScale(6,RoundingMode.HALF_UP));m.put("index",BigDecimal.valueOf(index).setScale(2,RoundingMode.HALF_UP));m.put("label",label(index,c.labels()));return m;}
        private static String label(double index,List<CulometroConfig.LabelBand> bands){for(CulometroConfig.LabelBand b:bands)if(index>=b.min().doubleValue())return b.label();return bands.get(bands.size()-1).label();}
    }

    public record ExportResult(int eventCount,int teamCount,Path outputFile){}

    private static final class Json {
        private final String t;private int i;Json(String s){t=s.charAt(0)=='\uFEFF'?s.substring(1):s;}Object parse(){skip();Object v=val();skip();if(i!=t.length())fail();return v;}private Object val(){skip();if(i>=t.length())fail();return switch(t.charAt(i)){case '{'->obj();case '['->arr();case '"'->str();case 't'->lit("true",true);case 'f'->lit("false",false);case 'n'->lit("null",null);default->num();};}private Map<String,Object>obj(){expect('{');Map<String,Object>m=new LinkedHashMap<>();skip();if(peek('}')){i++;return m;}while(true){String k=str();expect(':');m.put(k,val());skip();if(peek('}')){i++;return m;}expect(',');}}private List<Object>arr(){expect('[');List<Object>l=new ArrayList<>();skip();if(peek(']')){i++;return l;}while(true){l.add(val());skip();if(peek(']')){i++;return l;}expect(',');}}private String str(){expect('"');StringBuilder b=new StringBuilder();while(i<t.length()){char c=t.charAt(i++);if(c=='"')return b.toString();if(c=='\\'){char e=t.charAt(i++);if(e=='u'){b.append((char)Integer.parseInt(t.substring(i,i+4),16));i+=4;}else b.append(switch(e){case '"'->'"';case '\\'->'\\';case '/'->'/';case 'b'->'\b';case 'f'->'\f';case 'n'->'\n';case 'r'->'\r';case 't'->'\t';default->throw new IllegalArgumentException();});}else b.append(c);}fail();return "";}private Object num(){int s=i;while(i<t.length()&&"-+0123456789.eE".indexOf(t.charAt(i))>=0)i++;return new BigDecimal(t.substring(s,i));}private Object lit(String s,Object v){if(!t.startsWith(s,i))fail();i+=s.length();return v;}private void skip(){while(i<t.length()&&Character.isWhitespace(t.charAt(i)))i++;}private boolean peek(char c){skip();return i<t.length()&&t.charAt(i)==c;}private void expect(char c){skip();if(i>=t.length()||t.charAt(i)!=c)fail();i++;}private void fail(){throw new IllegalArgumentException("JSON non valido a "+i);}static String write(Object v){StringBuilder b=new StringBuilder();w(v,b);return b.toString();}private static void w(Object v,StringBuilder b){if(v==null){b.append("null");return;}if(v instanceof String s){b.append('"');for(char c:s.toCharArray()){switch(c){case '"'->b.append("\\\"");case '\\'->b.append("\\\\");case '\n'->b.append("\\n");case '\r'->b.append("\\r");case '\t'->b.append("\\t");default->b.append(c);}}b.append('"');return;}if(v instanceof Number||v instanceof Boolean){b.append(v);return;}if(v instanceof CulometroConfig.LabelBand band){w(Map.of("min",band.min(),"label",band.label()),b);return;}if(v instanceof CulometroConfig.LabelConfiguration labels){Map<String,Object>m=new LinkedHashMap<>();m.put("preset",labels.preset());m.put("customized",labels.customized());m.put("resetSource",labels.resetSource());m.put("bands",labels.bands());m.put("presetDefaults",labels.presetDefaults());w(m,b);return;}if(v instanceof Map<?,?>m){b.append('{');boolean first=true;for(var e:m.entrySet()){if(!first)b.append(',');first=false;w(String.valueOf(e.getKey()),b);b.append(':');w(e.getValue(),b);}b.append('}');return;}if(v instanceof Iterable<?>it){b.append('[');boolean first=true;for(Object x:it){if(!first)b.append(',');first=false;w(x,b);}b.append(']');return;}w(String.valueOf(v),b);}}
}
