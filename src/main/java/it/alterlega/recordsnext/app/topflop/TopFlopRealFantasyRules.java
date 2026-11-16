package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.config.MiniJson;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.RealDay;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.VoteState;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Explicit, documented and manually confirmed per-season rules. NO guessed coefficients. */
public final class TopFlopRealFantasyRules {
    private static final Set<String> INDEPENDENT=Set.of("GOLFATTI1","ASSIST","GOLSUBITI",
        "RIGPAR","RIGSBA","AUTOGOL1","AMM","ESP");
    public record Rule(String source,Map<String,BigDecimal> coefficients) {
        public Rule {
            if(source==null || source.isBlank())throw new IllegalArgumentException("Fonte regolamento mancante");
            if(!coefficients.keySet().equals(INDEPENDENT))
                throw new IllegalArgumentException("Coefficienti completi richiesti: "+INDEPENDENT);
            coefficients=Map.copyOf(coefficients);
        }
    }
    private final Map<String,Rule> verified;
    private TopFlopRealFantasyRules(Map<String,Rule> rules){verified=Map.copyOf(rules);}
    public static TopFlopRealFantasyRules empty(){return new TopFlopRealFantasyRules(Map.of());}
    public Rule forSeason(String season){return verified.get(season);}
    public static TopFlopRealFantasyRules load(Path file) throws IOException {
        if(file==null || !Files.isRegularFile(file))return empty();
        Object parsed=MiniJson.parse(Files.readString(file,StandardCharsets.UTF_8));
        if(!(parsed instanceof Map<?,?> doc)
            || !"4.0-real-fantasy-rules-v1".equals(doc.get("schemaVersion"))
            || !(doc.get("seasons") instanceof Map<?,?> seasons))
            throw new IOException("Schema del regolamento reale non valido");
        Map<String,Rule> result=new LinkedHashMap<>();
        for(var entry:seasons.entrySet()) {
            String season=String.valueOf(entry.getKey());
            if(!season.matches("[0-9]{4}_[0-9]{4}"))throw new IOException("Stagione errata: "+season);
            if(!(entry.getValue() instanceof Map<?,?> rule))throw new IOException("Regolamento non oggetto: "+season);
            if(!Boolean.TRUE.equals(rule.get("verified")))
                throw new IOException("Regolamento senza conferma esplicita: "+season);
            if(!Boolean.TRUE.equals(rule.get("penaltyGoalsIncludedInGoals"))
                || !Boolean.TRUE.equals(rule.get("penaltyConcededIncludedInGoalsAgainst")))
                throw new IOException("Inclusione rigori non certificata: "+season);
            if(!(rule.get("coefficients") instanceof Map<?,?> raw))
                throw new IOException("Coefficienti mancanti: "+season);
            Map<String,BigDecimal> coefficients=new LinkedHashMap<>();
            for(var c:raw.entrySet()) {
                String name=String.valueOf(c.getKey());
                if(!INDEPENDENT.contains(name) || !(c.getValue() instanceof Number))
                    throw new IOException("Coefficiente non supportato: "+name);
                BigDecimal value=new BigDecimal(c.getValue().toString());
                if(value.abs().compareTo(new BigDecimal("50"))>0)
                    throw new IOException("Coefficiente fuori limite: "+name);
                coefficients.put(name,value);
            }
            String source=rule.get("source")==null?"":rule.get("source").toString();
            result.put(season,new Rule(source,coefficients));
        }
        return new TopFlopRealFantasyRules(result);
    }
    public static BigDecimal calculate(RealDay day,Rule rule) {
        if(day.voteState()!=VoteState.VALID)return null;
        BigDecimal total=day.vote();
        // GOLFATTISURIGORE1 and GOLSUBITISURIGORE are subsets, never double count.
        for(var entry:rule.coefficients().entrySet()) {
            BigDecimal count=day.events().get(entry.getKey());
            if(count!=null)total=total.add(count.multiply(entry.getValue()));
        }
        return total;
    }
    public record Result(List<Map<String,Object>> rows,Map<String,Object> quality) {}
    public static Result extract(TopFlopJdbcArchiveAssembler.LoadedSeason season,Rule rule) {
        if(rule==null)return new Result(List.of(),Map.of("status","NON_VERIFICABILE_SENZA_REGOLAMENTO_STAGIONALE"));
        Map<String,BigDecimal> sums=new LinkedHashMap<>();
        Map<String,Integer> counts=new LinkedHashMap<>();
        for(RealDay day:season.archive().allRealDays()) {
            BigDecimal score=calculate(day,rule);
            if(score==null)continue;
            String player=day.key().player().seasonId()+":"+day.key().player().fcaPlayerId();
            sums.merge(player,score,BigDecimal::add);
            counts.merge(player,1,Integer::sum);
        }
        List<Map<String,Object>> rows=new ArrayList<>();
        for(var entry:sums.entrySet()) {
            int n=counts.get(entry.getKey());
            Map<String,Object> row=new LinkedHashMap<>();
            row.put("playerKey",entry.getKey());
            row.put("votiRealiValidi",n);
            row.put("sommaFantavotiReali",entry.getValue());
            row.put("fantamediaReale",entry.getValue().divide(BigDecimal.valueOf(n),MathContext.DECIMAL64));
            row.put("regolamentoFonte",rule.source());
            row.put("stato","REGOLAMENTO_CONFERMATO_ESPLICITAMENTE");
            rows.add(row);
        }
        rows.sort(Comparator.comparing(x->String.valueOf(x.get("playerKey"))));
        return new Result(List.copyOf(rows),Map.of("status","REGOLAMENTO_CONFERMATO_ESPLICITAMENTE",
            "origine",rule.source(),"calciatoriConVoto",rows.size()));
    }
}
