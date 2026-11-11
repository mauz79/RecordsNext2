package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.config.MiniJson;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Historical player facts for explicitly approved identities and a 3.2 site cutoff.
 * Real-life votes/events are counted once per FCA player and Serie A day (as
 * already consolidated in Careers). Fantasy contributions are NEVER summed
 * across teams/competitions into a fictitious player total; every row retains
 * its original fantasy team, competition, and season.
 */
public final class TopFlopHistoricalViews {
    private TopFlopHistoricalViews() {}
    private static final MathContext MC=MathContext.DECIMAL64;
    private static final String SHARD_PREFIX="a.push(...[";
    private static final List<String> EVENTS=List.of("GOLFATTI1","GOLFATTISURIGORE1","ASSIST",
        "GOLSUBITI","GOLSUBITISURIGORE","RIGPAR","RIGSBA","AUTOGOL1","AMM","ESP");

    static final class Career {
        final String identityId;
        final List<Map<String,Object>> seasons=new ArrayList<>();
        final List<Map<String,Object>> fantasyContexts=new ArrayList<>();
        final Set<String> seenContexts=new HashSet<>();
        BigDecimal votes=BigDecimal.ZERO;
        int valid=0;
        int costObservations=0;
        BigDecimal costSum=BigDecimal.ZERO,costMin=null,costMax=null;
        final Map<String,BigDecimal> events=new LinkedHashMap<>();
        Career(String id){identityId=id;}
        void add(Map<String,Object> row) {
            Map<String,Object> real=asObject(row.get("reale"));
            int validSeason=asInt(real.get("votiValidi"));
            BigDecimal sum=asDecimal(real.get("sommaVoti"));
            if(validSeason<0 || sum.signum()<0)throw new IllegalArgumentException("Voti reali invalidi");
            valid+=validSeason;votes=votes.add(sum);
            Map<String,Object> srcEvents=asObject(real.get("eventi"));
            for(var e:srcEvents.entrySet())events.merge(e.getKey(),asDecimal(e.getValue()),BigDecimal::add);
            String key=String.valueOf(row.get("playerKey"));
            String season=seasonOf(key);
            if(row.get("stagione")!=null && !season.equals(String.valueOf(row.get("stagione"))))
                throw new IllegalArgumentException("Stagione incoerente: " + key);
            Map<String,Object> seasonRow=new LinkedHashMap<>();
            seasonRow.put("stagione",season);seasonRow.put("playerKey",key);
            seasonRow.put("nome",String.valueOf(row.get("nome")));
            seasonRow.put("votiValidi",validSeason);seasonRow.put("sommaVoti",sum);
            seasonRow.put("mediaVoto",validSeason==0?null:sum.divide(BigDecimal.valueOf(validSeason),MC));
            seasonRow.put("eventi",srcEvents);
            if(row.get("costoAcquisto") instanceof Map<?,?> cost) {
                Map<String,Object> values=asObject(cost);
                seasonRow.put("costoAcquisto",values);
                int n=asInt(values.get("registrazioniConCosto"));
                if(n>0) {
                    BigDecimal costAverage=asDecimal(values.get("medio"));
                    BigDecimal minimum=asDecimal(values.get("minimo")),maximum=asDecimal(values.get("massimo"));
                    costObservations+=n;
                    costSum=costSum.add(costAverage.multiply(BigDecimal.valueOf(n)));
                    if(costMin==null||costMin.compareTo(minimum)>0)costMin=minimum;
                    if(costMax==null||costMax.compareTo(maximum)<0)costMax=maximum;
                }
            }
            seasons.add(seasonRow);
        }
        void addTeam(Map<String,Object> row) {
            String playerKey=String.valueOf(row.get("playerKey"));
            String season=seasonOf(playerKey);
            int team=asInt(row.get("idSquadraInterno"));
            int competition=asInt(row.get("idCompetizioneInterno"));
            String unique=playerKey+":"+team+":"+competition;
            if(!seenContexts.add(unique))throw new IllegalArgumentException("Contesto fantasy duplicato: "+unique);
            int counted=asInt(row.get("gareConteggiate"));
            int realValid=asInt(row.get("gareConVotoReale"));
            BigDecimal realVotes=asDecimal(row.get("sommaVotiRealiApportati"));
            BigDecimal fantasyVotes=asDecimal(row.get("sommaFantavotiApportati"));
            if(counted<0||realValid<0||realValid>counted)
                throw new IllegalArgumentException("Conteggi fantasy incoerenti: "+unique);
            Map<String,Object> context=new LinkedHashMap<>();
            context.put("stagione",season);
            context.put("playerKey",playerKey);
            context.put("idSquadraInterno",team);
            context.put("fantasquadra",row.get("fantasquadra"));
            context.put("idCompetizioneInterno",competition);
            context.put("competizione",row.get("competizione"));
            context.put("famiglia",row.get("famiglia"));
            context.put("gareConteggiate",counted);
            context.put("gareConVotoReale",realValid);
            context.put("mediaVotoApportata",realValid==0?null:realVotes.divide(BigDecimal.valueOf(realValid),MC));
            context.put("fantamediaApportata",counted==0?null:fantasyVotes.divide(BigDecimal.valueOf(counted),MC));
            context.put("sommaVotiRealiApportati",realVotes);
            context.put("sommaFantavotiApportati",fantasyVotes);
            context.put("panchinaNonConteggiata",asInt(row.get("panchinaNonConteggiata")));
            context.put("fuoriLista",asInt(row.get("fuoriLista")));
            if(row.containsKey("nonUtilizzatoOsservato"))
                context.put("nonUtilizzatoOsservato",row.get("nonUtilizzatoOsservato"));
            if(row.containsKey("costoAcquisto"))
                context.put("costoAcquisto",row.get("costoAcquisto"));
            fantasyContexts.add(context);
        }
        Map<String,Object> view() {
            Map<String,Object> result=new LinkedHashMap<>();
            result.put("playerIdentityId",identityId);
            result.put("identitaStoricaVerificata",true);
            result.put("stagioni",seasons);
            result.put("numeroStagioni",seasons.size());
            result.put("votiRealiValidi",valid);
            result.put("mediaVotoReale",valid==0?null:votes.divide(BigDecimal.valueOf(valid),MC));
            result.put("eventiReali",events);
            result.put("gol",events.getOrDefault("GOLFATTI1",BigDecimal.ZERO));
            result.put("assist",events.getOrDefault("ASSIST",BigDecimal.ZERO));
            result.put("ammonizioni",events.getOrDefault("AMM",BigDecimal.ZERO));
            result.put("espulsioni",events.getOrDefault("ESP",BigDecimal.ZERO));
            result.put("autogol",events.getOrDefault("AUTOGOL1",BigDecimal.ZERO));
            result.put("rigoriSbagliati",events.getOrDefault("RIGSBA",BigDecimal.ZERO));
            result.put("golSubiti",events.getOrDefault("GOLSUBITI",BigDecimal.ZERO));
            result.put("rigoriParati",events.getOrDefault("RIGPAR",BigDecimal.ZERO));
            result.put("fantamediaReale",null); // Never invent a score from FCM team contributions.
            Map<String,Object> costs=new LinkedHashMap<>();
            costs.put("registrazioniConCosto",costObservations);
            costs.put("minimo",costMin);costs.put("massimo",costMax);
            costs.put("medio",costObservations==0?null:costSum.divide(BigDecimal.valueOf(costObservations),MC));
            costs.put("confrontabilita","PREZZI_REGISTRATI_NON_NORMALIZZATI_PER_BUDGET_STAGIONALE");
            result.put("costoAcquistoStorico",costs);
            result.put("fantasquadrePerStagione",fantasyContexts);
            result.put("contestiFantasyDistinti",fantasyContexts.size());
            result.put("aggregazioneFantasy","PER_FANTASQUADRA_COMPETIZIONE_STAGIONE_NO_TOTALE_GLOBALE");
            return result;
        }
    }
    /** Old entrypoint retained for 3.2/4.0 tests and consumers. */
    public static List<Map<String,Object>> aggregate(List<Map<String,Object>> seasonCareers,
                                                       Map<String,String> approved) {
        return aggregate(seasonCareers,List.of(),approved);
    }
    public static List<Map<String,Object>> aggregate(List<Map<String,Object>> seasonCareers,
                                                       List<Map<String,Object>> teams,
                                                       Map<String,String> approved) {
        Objects.requireNonNull(seasonCareers);Objects.requireNonNull(teams);Objects.requireNonNull(approved);
        Map<String,Career> byIdentity=new LinkedHashMap<>();
        Map<String,String> seenKeys=new LinkedHashMap<>();
        for(Map<String,Object> row:seasonCareers) {
            String key=String.valueOf(row.get("playerKey"));
            String identity=approved.get(key);
            if(identity==null)continue; // Never guess identity by raw code or name.
            if(seenKeys.putIfAbsent(key,identity)!=null)
                throw new IllegalArgumentException("Doppia anagrafica nello stesso cutoff: " + key);
            byIdentity.computeIfAbsent(identity,Career::new).add(row);
        }
        for(Map<String,Object> row:teams) {
            String key=String.valueOf(row.get("playerKey"));
            String identity=approved.get(key);
            if(identity==null)continue;
            Career career=byIdentity.get(identity);
            if(career==null || !seenKeys.containsKey(key))
                throw new IllegalArgumentException("Contesto fantasy senza carriera FCA nello scope: "+key);
            career.addTeam(row);
        }
        List<Map<String,Object>> out=new ArrayList<>();
        for(Career c:byIdentity.values()) {
            c.seasons.sort(Comparator.comparing(x->String.valueOf(x.get("stagione"))));
            if(c.seasons.stream().map(s->s.get("stagione")).distinct().count()!=c.seasons.size())
                throw new IllegalArgumentException("Identita' approvata compare due volte nella stessa stagione: " + c.identityId);
            c.fantasyContexts.sort(Comparator.comparing((Map<String,Object> x)->String.valueOf(x.get("stagione")))
                .thenComparing(x->String.valueOf(x.get("fantasquadra")))
                .thenComparing(x->String.valueOf(x.get("competizione"))));
            if(c.seasons.size()>1)out.add(c.view());
        }
        out.sort(Comparator.comparing(x->String.valueOf(x.get("playerIdentityId"))));
        return List.copyOf(out);
    }

    /** Parse the same audited, compact JS staged by the Java 4.0 exporter. */
    public static List<Map<String,Object>> readCareers(Path directory,List<Path> files) throws IOException {
        List<Map<String,Object>> out=new ArrayList<>();
        for(Path source:files) {
            if(!source.getFileName().toString().contains(".Careers."))continue;
            for(Object raw:readRows(directory,source))out.add(decodeCareer(raw));
        }
        return out;
    }

    /** Per-season dictionaries are from the scoped 3.2 facade, NEVER guessed by name. */
    public static List<Map<String,Object>> readTeams(Path directory,List<Path> files,
                                                      List<Map<String,Object>> seasonMetadata) throws IOException {
        Map<String,Map<Integer,String>> teamNames=new LinkedHashMap<>(),competitionNames=new LinkedHashMap<>(),
            competitionFamilies=new LinkedHashMap<>();
        for(Map<String,Object> season:seasonMetadata) {
            String id=String.valueOf(season.get("stagione"));
            Map<String,Object> dict=asObject(season.get("dizionari"));
            Map<Integer,String> teams=new LinkedHashMap<>(),competitions=new LinkedHashMap<>(),families=new LinkedHashMap<>();
            for(Object raw:asList(dict.get("squadre"))) {
                List<?> row=asList(raw);
                if(row.size()!=2)throw new IOException("Dizionario squadre non valido: "+id);
                teams.put(asInt(row.get(0)),String.valueOf(row.get(1)));
            }
            for(Object raw:asList(dict.get("competizioni"))) {
                List<?> row=asList(raw);
                if(row.size()!=3)throw new IOException("Dizionario competizioni non valido: "+id);
                competitions.put(asInt(row.get(0)),String.valueOf(row.get(1)));
                families.put(asInt(row.get(0)),String.valueOf(row.get(2)));
            }
            if(teamNames.putIfAbsent(id,teams)!=null)throw new IOException("Stagione duplicata nei dizionari: "+id);
            competitionNames.put(id,competitions);competitionFamilies.put(id,families);
        }
        List<Map<String,Object>> out=new ArrayList<>();
        for(Path source:files) {
            if(!source.getFileName().toString().contains(".Teams."))continue;
            for(Object raw:readRows(directory,source)) {
                if(!(raw instanceof List<?> row)|| (row.size()!=4 && row.size()!=5))
                    throw new IOException("Riga Teams non valida in "+source);
                String key=String.valueOf(row.get(0)),season=seasonOf(key);
                int team=asInt(row.get(1)),competition=asInt(row.get(2));
                String teamName=teamNames.getOrDefault(season,Map.of()).get(team);
                String compName=competitionNames.getOrDefault(season,Map.of()).get(competition);
                String family=competitionFamilies.getOrDefault(season,Map.of()).get(competition);
                if(teamName==null||compName==null||family==null)
                    throw new IOException("ID squadra/competizione non risolti: "+key+"/"+team+"/"+competition);
                List<?> usage=asList(row.get(3));
                if(usage.size()<9 || usage.size()>10)
                    throw new IOException("Riga apporto Teams non valida: "+key);
                Map<String,Object> decoded=new LinkedHashMap<>();
                decoded.put("playerKey",key);
                decoded.put("idSquadraInterno",team);decoded.put("idCompetizioneInterno",competition);
                decoded.put("fantasquadra",teamName);decoded.put("competizione",compName);decoded.put("famiglia",family);
                decoded.put("gareConteggiate",usage.get(1));decoded.put("gareConVotoReale",usage.get(2));
                decoded.put("sommaVotiRealiApportati",usage.get(3));
                decoded.put("sommaFantavotiApportati",usage.get(4));
                decoded.put("panchinaNonConteggiata",usage.get(5));decoded.put("fuoriLista",usage.get(6));
                if(usage.size()==10)decoded.put("nonUtilizzatoOsservato",decodeUnused(usage.get(9)));
                if(row.size()==5)decoded.put("costoAcquisto",decodeCost(row.get(4)));
                out.add(decoded);
            }
        }
        return out;
    }
    private static Map<String,Object> decodeCost(Object raw) throws IOException {
        List<?> r=asList(raw);
        if(r.size()!=4)throw new IOException("Costo FCM non valido");
        int n=asInt(r.get(0));
        if(n<0)throw new IOException("Conteggio costi negativo");
        Map<String,Object> result=new LinkedHashMap<>();
        result.put("registrazioniConCosto",n);
        result.put("minimo",n==0?null:asDecimal(r.get(1)));
        result.put("massimo",n==0?null:asDecimal(r.get(2)));
        result.put("medio",n==0?null:asDecimal(r.get(3)));
        return result;
    }
    private static Map<String,Object> decodeUnused(Object raw) throws IOException {
        List<?> u=asList(raw);
        if(u.size()!=5)throw new IOException("Dettaglio fuori campo non valido");
        int realValid=asInt(u.get(0)),fantasyValid=asInt(u.get(2));
        BigDecimal votes=asDecimal(u.get(1)),fantasy=asDecimal(u.get(3));
        return Map.of("gareConVotoReale",realValid,"sommaVotiReali",votes,
            "mediaVotoReale",realValid==0?"":votes.divide(BigDecimal.valueOf(realValid),MC),
            "gareConFantavotoNumerico",fantasyValid,"sommaFantavotiTabellino",fantasy,
            "fantamediaTabellino",fantasyValid==0?"":fantasy.divide(BigDecimal.valueOf(fantasyValid),MC),
            "eventiRealiNonApportati",decodeEvents(asList(u.get(4))));
    }
    private static List<?> readRows(Path directory,Path source) throws IOException {
        if(!source.normalize().getParent().equals(directory.normalize()))throw new IOException("Shard fuori dalla directory");
        String js=Files.readString(source,StandardCharsets.UTF_8);
        int begin=js.indexOf(SHARD_PREFIX),end=js.lastIndexOf("]);})();");
        if(begin<0 || end<begin)throw new IOException("Formato shard non riconosciuto: " + source);
        String body=js.substring(begin+SHARD_PREFIX.length(),end);
        Object parsed=MiniJson.parse("["+body+"]");
        if(!(parsed instanceof List<?> rows))throw new IOException("Shard non-array: " + source);
        return rows;
    }
    private static Map<String,Object> decodeCareer(Object raw) {
        if(raw instanceof Map<?,?>)return asObject(raw); // controlled tests.
        if(!(raw instanceof List<?> r) || (r.size()!=6 && r.size()!=7))
            throw new IllegalArgumentException("Career compatta non valida: "+raw);
        if(!(r.get(4) instanceof List<?> real) || real.size()!=6)
            throw new IllegalArgumentException("Aggregato reale non valido");
        Map<String,Object> row=new LinkedHashMap<>();
        row.put("playerKey",r.get(0));row.put("nome",r.get(1));
        row.put("reale",Map.of("votiValidi",real.get(0),"sommaVoti",real.get(1),
            "eventi",decodeEvents(asList(real.get(2)))));
        if(r.size()==7) {
            try {row.put("costoAcquisto",decodeCost(r.get(6)));}
            catch(IOException ex){throw new IllegalArgumentException("Costo Career non valido",ex);}
        }
        return row;
    }
    private static Map<String,Object> decodeEvents(List<?> pairs) {
        if(pairs.size()%2!=0)throw new IllegalArgumentException("Eventi reali non validi");
        Map<String,Object> events=new LinkedHashMap<>();
        for(int i=0;i<pairs.size();i+=2) {
            int index=asInt(pairs.get(i));
            if(index<0||index>=EVENTS.size()||events.putIfAbsent(EVENTS.get(index),pairs.get(i+1))!=null)
                throw new IllegalArgumentException("Codice evento duplicato o fuori range");
        }
        return events;
    }
    private static String seasonOf(String key) {
        String season=key.length()>=9?key.substring(0,9):"";
        if(!season.matches("[0-9]{4}_[0-9]{4}") || key.length()<=10 || key.charAt(9)!=':')
            throw new IllegalArgumentException("PlayerKey non valido: "+key);
        return season;
    }
    @SuppressWarnings("unchecked")
    private static Map<String,Object> asObject(Object raw) {
        if(!(raw instanceof Map<?,?> m))throw new IllegalArgumentException("Formato non-oggetto: " + raw);
        return (Map<String,Object>)m;
    }
    private static List<?> asList(Object raw) {
        if(!(raw instanceof List<?> a))throw new IllegalArgumentException("Formato non-array: "+raw);
        return a;
    }
    private static int asInt(Object raw){if(!(raw instanceof Number n))throw new IllegalArgumentException("Contatore mancante");return n.intValue();}
    private static BigDecimal asDecimal(Object raw){if(raw==null)return BigDecimal.ZERO;return new BigDecimal(raw.toString());}
}
