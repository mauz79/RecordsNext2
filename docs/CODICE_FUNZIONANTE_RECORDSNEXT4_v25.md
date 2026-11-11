# RecordsNext 4.0 — Bibbia v25 — Viste pubbliche Top & Flop

Storico integrale FCA/FCM solo SQLite; HTML e JS pubblici contengono viste interessanti TF01–TF20, non lo storico di tutte le giornate.
Questa versione e STAGING; non incorpora ancora RecordFamily, pipeline, publisher, GUI, CSS ne pubblica sui siti.
Bibbia precedente: `docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v24.md` SHA-256: `85bff478c4233c907b7eb62e6516824cd63ec1dd0aa6895181ce86141b8b38e6`

## `src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopPublicViewsStageExporter.java`

SHA-256: `d57560ab6e32ec29db8d31bed2fd96e325310f7dfdca166ad94fb8178fdaa8ef`

~~~~java
package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.topflop.TopFlopFacts.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * Approved public VIEWS, not the full FCA/FCM facts dump. Stage-only.
 * All historical facts remain in local SQLite. No scorecard URL is synthesized.
 * No publisher, manifest, CSS or site is modified by this class.
 *
 * Averages (TF05/TF06), wasted votes (TF11-13), and baseline-derived
 * rankings (TF14-15) are NOT promoted to definitive rankings while their
 * policy/evidence is incomplete. Their available raw aggregates are exposed.
 */
public final class TopFlopPublicViewsStageExporter {
    private TopFlopPublicViewsStageExporter() {}
    public static final long LIMIT_BYTES = 1_500_000;
    public static final long TARGET_BYTES = 950_000;
    public static final String FACADE = "fcmRecordsNext_TopFlopViews.js";
    private static final MathContext PRECISION = MathContext.DECIMAL64;
    private static final List<String> EVENTS = List.of(
        "GOLFATTI1", "GOLFATTISURIGORE1", "ASSIST", "GOLSUBITI",
        "GOLSUBITISURIGORE", "RIGPAR", "RIGSBA", "AUTOGOL1", "AMM", "ESP");

    public record FileInfo(String name, String season, String kind, int rows, long bytes, String sha256) {}
    public record Result(List<FileInfo> files, int seasons, int players,
                         long sourceRealDays, long sourceFantasyContexts, Path output) {}

    static final class Totals {
        int validVotes, counted, countedRealValid, observed, benchUnused, outsideList, unknown;
        int keeperValidConcededDays, keeperZeroConcededDays;
        BigDecimal votes = BigDecimal.ZERO, fantasy = BigDecimal.ZERO;
        BigDecimal countedRealVotes = BigDecimal.ZERO;
        BigDecimal keeperConcededOnValidDays = BigDecimal.ZERO;
        final Map<String,BigDecimal> events = new LinkedHashMap<>();
        void event(Map<String,BigDecimal> raw) {
            for (String key : EVENTS) {
                BigDecimal amount = raw.get(key);
                if (amount != null && amount.signum() != 0) {
                    events.merge(key, amount, BigDecimal::add);
                }
            }
        }
        Map<String,Object> realView() {
            return obj("votiValidi",validVotes,"sommaVoti",votes,
                "mediaVoto",mean(votes,validVotes),"eventi",events,
                "giornateGolSubitiConVoto",keeperValidConcededDays,
                "giornateZeroGolSubitiRegistrati",keeperZeroConcededDays,
                "mediaGolSubitiSuGiornateVerificate",
                    mean(keeperConcededOnValidDays,keeperValidConcededDays));
        }
        Map<String,Object> fantasyView() {
            return obj("contestiOsservati",observed,"conteggiati",counted,
                "conVotoRealeValido",countedRealValid,"sommaVotiRealiApportati",countedRealVotes,
                "mediaVotoRealeApportato",mean(countedRealVotes,countedRealValid),
                "sommaFantavotiApportati",fantasy,
                "fantamediaApportata",mean(fantasy,counted),
                "panchinaNonConteggiata",benchUnused,"fuoriLista",outsideList,
                "utilizzoDaVerificare",unknown,"eventiApportati",events);
        }
    }
    static final class Player {
        final TopFlopFcaJdbcReader.PlayerProfile profile;
        final Totals real = new Totals(), fantasy = new Totals();
        Player(TopFlopFcaJdbcReader.PlayerProfile profile) {this.profile=profile;}
        String id() {return profile.player().seasonId()+":"+profile.player().fcaPlayerId();}
        int role() {
            try {return Integer.parseInt(profile.originalFields().get("GiocatoreA.RUOLO"));}
            catch (RuntimeException ex) {return -1;}
        }
        Map<String,Object> label() {
            return obj("playerKey",id(),"nome",profile.name(),
                "codiceStorico",profile.player().gazzaCode(),"ruoloFca",role());
        }
    }
    record TeamKey(int playerId,int teamId,int competitionId) {}
    static final class Team {
        final Player player;
        final int teamId, competitionId;
        final String teamName, competitionName, family;
        final Totals totals = new Totals();
        Team(Player player,FantasyFact fact) {
            this.player=player;
            teamId=fact.key().fantasyTeamId();competitionId=fact.key().sourceCompetitionId();
            teamName=fact.sourceFields().getOrDefault("FCM.Fantasquadra.Nome", "");
            competitionName=fact.competitionName();family=fact.family().name();
        }
        Map<String,Object> view() {
            Map<String,Object> x=new LinkedHashMap<>(player.label());
            x.putAll(obj("fantasquadra",teamName,"idSquadraInterno",teamId,
                "competizione",competitionName,"idCompetizioneInterno",competitionId,
                "famiglia",family,"apporto",totals.fantasyView()));
            return x;
        }
    }
    static final class Winner {
        final String id, group, metric, direction;
        BigDecimal value;
        final List<Map<String,Object>> entries = new ArrayList<>();
        Winner(String id,String group,String metric,String direction) {
            this.id=id;this.group=group;this.metric=metric;this.direction=direction;
        }
        void offer(BigDecimal candidate,Map<String,Object> entry) {
            if (candidate == null) return;
            int compare=value==null ? 1 : candidate.compareTo(value);
            boolean better=value==null || (direction.equals("MAX") && compare>0) ||
                (direction.equals("MIN") && compare<0);
            if (better) {value=candidate;entries.clear();}
            if (better || candidate.compareTo(value)==0) entries.add(entry);
        }
        Map<String,Object> view() {
            return obj("recordId",id,"ambito",group,"metrica",metric,
                "ordine",direction,"valore",value,"pariMerito",entries.size(),"risultati",entries);
        }
    }
    static final class SeasonView {
        final List<Map<String,Object>> careers = new ArrayList<>(), teams = new ArrayList<>(),
            records = new ArrayList<>(), highlights = new ArrayList<>();
        final Map<String,Object> counters = new LinkedHashMap<>();
    }

    /** Exposed for synthetic, source-independent regression tests. */
    static SeasonView build(TopFlopJdbcArchiveAssembler.LoadedSeason loaded,
                     Map<TopFlopCanonicalScorecards.MatchTeam,String> links) {
        Objects.requireNonNull(loaded);Objects.requireNonNull(links);
        TopFlopArchive archive=loaded.archive();
        Map<Integer,Player> players=new TreeMap<>();
        for (var profile:loaded.profiles().values()) {
            Player old=players.putIfAbsent(profile.player().fcaPlayerId(),new Player(profile));
            if(old!=null)throw new IllegalStateException("Duplicate player profile");
        }
        Map<String,Winner> winners=new LinkedHashMap<>();
        Map<String,Map<String,Object>> highlights=new LinkedHashMap<>();
        int valid=0,counted=0;
        for (RealDay day:archive.allRealDays()) {
            Player p=need(players,day.key().player().fcaPlayerId());
            boolean validVote=day.voteState()==VoteState.VALID;
            if(validVote) {
                valid++;
                p.real.validVotes++;
                p.real.votes=p.real.votes.add(day.vote());
                String hk=realHighlight(day,p);
                offer(winners,"TF01","realeSingolaGiornata","votoReale","MAX",day.vote(),
                    obj("calciatore",p.label(),"giornataSerieA",day.key().serieARound(),"highlightKey",hk));
                offer(winners,"TF02","realeSingolaGiornata","votoReale","MIN",day.vote(),
                    obj("calciatore",p.label(),"giornataSerieA",day.key().serieARound(),"highlightKey",hk));
            }
            p.real.event(day.events());
            if(p.role()==1 && validVote && day.events().containsKey("GOLSUBITI")) {
                p.real.keeperValidConcededDays++;
                BigDecimal conceded=day.events().get("GOLSUBITI");
                p.real.keeperConcededOnValidDays=p.real.keeperConcededOnValidDays.add(conceded);
                if(conceded.signum()==0)p.real.keeperZeroConcededDays++;
                if(conceded.signum()>0)offer(winners,"TF18","portieriSingolaGiornata","golSubiti","MAX",conceded,
                    obj("calciatore",p.label(),"giornataSerieA",day.key().serieARound(),
                        "highlightKey",hkReal(day,p,highlights)));
            }
            if(p.role()==1) {
                BigDecimal saved=event(day.events(),"RIGPAR");
                if(saved.signum()>0) offer(winners,"TF17","portieriSingolaGiornata","rigoriParati","MAX",saved,
                    obj("calciatore",p.label(),"giornataSerieA",day.key().serieARound(),
                        "highlightKey",hkReal(day,p,highlights)));
                BigDecimal goals=event(day.events(),"GOLFATTI1");
                if(goals.signum()>0) {
                    // Only an actual source goal, NOT goals + penalty goals.
                    hkReal(day,p,highlights);
                }
            }
        }
        Map<TeamKey,Team> teams=new LinkedHashMap<>();
        for (FantasyFact fact:archive.allFantasyFacts()) {
            Player p=need(players,fact.key().player().fcaPlayerId());
            TeamKey key=new TeamKey(fact.key().player().fcaPlayerId(),
                fact.key().fantasyTeamId(),fact.key().sourceCompetitionId());
            Team t=teams.computeIfAbsent(key, ignored->new Team(p,fact));
            addUsage(p.fantasy,fact);addUsage(t.totals,fact);
            if(fact.usage()!=Usage.COUNTED)continue;
            RealDay realForFact=archive.realDay(fact.realDayKey());
            if(realForFact.voteState()==VoteState.VALID) {
                p.fantasy.countedRealValid++;
                p.fantasy.countedRealVotes=p.fantasy.countedRealVotes.add(realForFact.vote());
                t.totals.countedRealValid++;
                t.totals.countedRealVotes=t.totals.countedRealVotes.add(realForFact.vote());
            }
            counted++;
            String hk=highlightKey(fact);
            Map<String,Object> entry=obj("calciatore",p.label(),
                "fantasquadra",t.teamName,"competizione",t.competitionName,
                "famiglia",t.family,"giornataCompetizione",fact.fantasyRound(),
                "giornataSerieA",fact.realDayKey().serieARound(),"highlightKey",hk);
            offer(winners,"TF03","fantasySingoloIncontro","fantavotoApportato","MAX",fact.fantasyVote(),entry);
            offer(winners,"TF04","fantasySingoloIncontro","fantavotoApportato","MIN",fact.fantasyVote(),entry);
        }
        // A highlight is published ONLY if an approved record points to it;
        // notable goalkeeper goals are an explicitly approved TF20 exception.
        Set<String> selected=new LinkedHashSet<>();
        for(Winner w:winners.values())for(var e:w.entries){
            Object h=e.get("highlightKey");if(h!=null)selected.add(h.toString());
        }
        for(RealDay day:archive.allRealDays()) {
            Player p=need(players,day.key().player().fcaPlayerId());
            if(p.role()==1 && (event(day.events(),"GOLFATTI1").signum()>0 ||
                event(day.events(),"ASSIST").signum()>0))
                selected.add(realHighlight(day,p));
        }
        // If a REAL record day also appears in a fantasy fixture, expose its
        // actual FCM context and canonical tabellino (never a fabricated link).
        Map<String,List<Map<String,Object>>> realLinks=new LinkedHashMap<>();
        for(FantasyFact fact:archive.allFantasyFacts()) {
            RealDay realDay=archive.realDay(fact.realDayKey());
            Player p=need(players,fact.key().player().fcaPlayerId());
            String realKey=realHighlight(realDay,p);
            if(!selected.contains(realKey))continue;
            String url=links.get(new TopFlopCanonicalScorecards.MatchTeam(
                fact.key().matchId(),fact.key().fantasyTeamId()));
            realLinks.computeIfAbsent(realKey,ignored->new ArrayList<>()).add(obj(
                "fantasquadra",fact.sourceFields().get("FCM.Fantasquadra.Nome"),
                "competizione",fact.competitionName(),"famiglia",fact.family().name(),
                "giornataCompetizione",fact.fantasyRound(),
                "descrizioneGiornata",clean(fact.sourceFields().get("FCM.Giornata.Desc")),
                "giornataSerieA",fact.realDayKey().serieARound(),
                "schieramento",lineup(fact),"conteggiato",fact.usage()==Usage.COUNTED,
                "fantavoto",fact.usage()==Usage.COUNTED?fact.fantasyVote():null,
                "urlTabellino",url));
        }
        // Resolve only the selected fantasy encounters, never copy all canonical links.
        for(FantasyFact fact:archive.allFantasyFacts()) {
            String key=highlightKey(fact);
            if(!selected.contains(key))continue;
            Player p=need(players,fact.key().player().fcaPlayerId());
            String url=links.get(new TopFlopCanonicalScorecards.MatchTeam(fact.key().matchId(),
                fact.key().fantasyTeamId()));
            RealDay real=archive.realDay(fact.realDayKey());
            highlights.putIfAbsent(key,obj("key",key,"tipo","fantasyConteggiato",
                "calciatore",p.label(),"votoReale",real.vote(),"fantavoto",fact.fantasyVote(),
                "votoTabellino",numericToken(fact.sourceFields().get("FCM.Tabellino.Voto")),
                "eventi",publicEvents(fact.fantasyEvents()),
                "fantasquadra",fact.sourceFields().get("FCM.Fantasquadra.Nome"),
                "competizione",fact.competitionName(),"famiglia",fact.family().name(),
                "giornataCompetizione",fact.fantasyRound(),
                "descrizioneGiornata",clean(fact.sourceFields().get("FCM.Giornata.Desc")),
                "giornataSerieA",fact.realDayKey().serieARound(),
                "schieramento",lineup(fact),"conteggiato",true,
                "urlTabellino",url));
        }
        for(RealDay day:archive.allRealDays()) {
            Player p=need(players,day.key().player().fcaPlayerId());
            String key=realHighlight(day,p);
            if(selected.contains(key)) highlights.putIfAbsent(key,obj(
                "key",key,"tipo","prestazioneReale","calciatore",p.label(),
                "giornataSerieA",day.key().serieARound(),"votoReale",day.vote(),
                "eventi",publicEvents(day.events()),"urlTabellino",null,
                "contestiFantasy",realLinks.getOrDefault(key,List.of())));
        }
        SeasonView out=new SeasonView();
        for(Player p:players.values()) {
            Map<String,Object> career=new LinkedHashMap<>(p.label());
            career.put("reale",p.real.realView());career.put("fantasy",p.fantasy.fantasyView());
            career.put("identitaStoricaVerificata",false); // no auto-merge of CodiceGazza
            out.careers.add(career);
            Map<String,Object> base=obj("calciatore",p.label());
            offerPositive(winners,"TF07","golRealiStagione","gol",event(p.real.events,"GOLFATTI1"),base);
            offerPositive(winners,"TF07","golApportatiContestiFantasy","gol",event(p.fantasy.events,"GOLFATTI1"),base);
            offerPositive(winners,"TF08","assistRealiStagione","assist",event(p.real.events,"ASSIST"),base);
            offerPositive(winners,"TF08","assistApportatiContestiFantasy","assist",event(p.fantasy.events,"ASSIST"),base);
            for(String negative:List.of("AMM","ESP","AUTOGOL1","RIGSBA")) {
                offerPositive(winners,"TF09","eventiNegativiRealiStagione",negative,event(p.real.events,negative),base);
                offerPositive(winners,"TF09","eventiNegativiApportati",negative,event(p.fantasy.events,negative),base);
            }
            offerPositive(winners,"TF10","giornateRealiConVoto","n",BigDecimal.valueOf(p.real.validVotes),base);
            offerPositive(winners,"TF10","incontriFantasyConteggiati","n",BigDecimal.valueOf(p.fantasy.counted),base);
            if(p.role()==1) {
                offerPositive(winners,"TF17","portieriRigoriParatiStagione","RIGPAR",event(p.real.events,"RIGPAR"),base);
                offerPositive(winners,"TF17","portieriRigoriParatiApportati","RIGPAR",event(p.fantasy.events,"RIGPAR"),base);
                offerPositive(winners,"TF18","portieriGolSubitiStagione","GOLSUBITI",event(p.real.events,"GOLSUBITI"),base);
                offerPositive(winners,"TF18","portieriGolSubitiApportati","GOLSUBITI",event(p.fantasy.events,"GOLSUBITI"),base);
                offerPositive(winners,"TF19","portieriGiornateZeroGolSubitiRegistrati","n",
                    BigDecimal.valueOf(p.real.keeperZeroConcededDays),base);
                offerPositive(winners,"TF20","portieriGolStagione","GOLFATTI1",event(p.real.events,"GOLFATTI1"),base);
                offerPositive(winners,"TF20","portieriGolSuRigoreStagione","GOLFATTISURIGORE1",event(p.real.events,"GOLFATTISURIGORE1"),base);
                offerPositive(winners,"TF20","portieriAssistStagione","ASSIST",event(p.real.events,"ASSIST"),base);
                offerPositive(winners,"TF20","portieriGolApportati","GOLFATTI1",event(p.fantasy.events,"GOLFATTI1"),base);
                offerPositive(winners,"TF20","portieriGolSuRigoreApportati","GOLFATTISURIGORE1",event(p.fantasy.events,"GOLFATTISURIGORE1"),base);
                offerPositive(winners,"TF20","portieriAssistApportati","ASSIST",event(p.fantasy.events,"ASSIST"),base);
            }
        }
        teams.values().stream().sorted(Comparator.comparing((Team t)->t.player.id())
            .thenComparingInt(t->t.teamId).thenComparingInt(t->t.competitionId))
            .map(Team::view).forEach(out.teams::add);
        for(Winner w:winners.values()) if(w.value!=null)out.records.add(w.view());
        out.highlights.addAll(highlights.values());
        out.counters.putAll(obj("players",players.size(),"realDays",archive.realDayCount(),
            "validRealVotes",valid,"fantasyContexts",archive.fantasyFactCount(),
            "fantasyCounted",counted,"teamCompetitionAggregates",teams.size(),
            "selectedHighlights",out.highlights.size(),"youthExcluded",loaded.fcm().youthExcluded()));
        return out;
    }

    private static void addUsage(Totals a,FantasyFact f) {
        a.observed++;
        if(f.usage()!=Usage.COUNTED) {
            switch(f.usage()){
                case BENCH_UNUSED -> a.benchUnused++;
                case OUTSIDE_LIST -> a.outsideList++;
                default -> a.unknown++;
            }
            return;
        }
        if(f.fantasyVote()==null) throw new IllegalStateException("COUNTED without FCM total");
        a.counted++;a.fantasy=a.fantasy.add(f.fantasyVote());
        // The assembler credits source FCA events ONLY on COUNTED contexts.
        a.event(f.fantasyEvents());
    }
    private static String lineup(FantasyFact fact) {
        if(fact.usage()==Usage.BENCH_UNUSED)return "RISERVA_NON_ENTRATA";
        if(fact.usage()==Usage.OUTSIDE_LIST)return "FUORI_LISTA";
        if(fact.usage()!=Usage.COUNTED)return "SCHIERAMENTO_DA_VERIFICARE";
        if(fact.initialStarter())return "TITOLARE";
        if("1".equals(fact.sourceFields().get("FCM.Formazione.EntratoRiserva")))return "RISERVA_ENTRATA";
        return "CONTEGGIATO_SCHIERAMENTO_DA_VERIFICARE";
    }
    private static BigDecimal numericToken(String source) {
        if(source==null || !source.matches("[+-]?[0-9]+(?:[,.][0-9]+)?"))return null;
        return new BigDecimal(source.replace(',','.'));
    }
    private static String clean(String raw) {
        if(raw==null || raw.equals("__SQL_NULL__"))return null;
        return raw;
    }
    private static void offerPositive(Map<String,Winner> w,String id,String group,String metric,
                                     BigDecimal amount,Map<String,Object> entry) {
        if(amount.signum()>0) offer(w,id,group,metric,"MAX",amount,entry);
    }
    private static void offer(Map<String,Winner> w,String id,String group,String metric,
                              String order,BigDecimal value,Map<String,Object> entry) {
        String key=id+":"+group+":"+metric+":"+order;
        w.computeIfAbsent(key,ignored->new Winner(id,group,metric,order)).offer(value,entry);
    }
    static BigDecimal event(Map<String,BigDecimal> events,String name) {
        return events.getOrDefault(name,BigDecimal.ZERO);
    }
    static Map<String,BigDecimal> publicEvents(Map<String,BigDecimal> input) {
        Map<String,BigDecimal> m=new LinkedHashMap<>();
        for(String key:EVENTS) {
            BigDecimal v=input.get(key);
            if(v!=null && v.signum()!=0) m.put(key,v);
        }
        return m;
    }
    private static String realHighlight(RealDay day,Player p) {
        return "R:"+p.id()+":"+day.key().serieARound();
    }
    private static String hkReal(RealDay day,Player p,Map<String,Map<String,Object>> highlights) {
        return realHighlight(day,p);
    }
    private static String highlightKey(FantasyFact fact) {
        return "F:"+fact.key().player().seasonId()+":"+fact.key().player().fcaPlayerId()+
            ":"+fact.key().matchId()+":"+fact.key().fantasyTeamId();
    }
    private static Player need(Map<Integer,Player> players,int id) {
        Player p=players.get(id);if(p==null)throw new IllegalStateException("Missing profile: "+id);return p;
    }
    private static BigDecimal mean(BigDecimal value,int count) {
        return count==0?null:value.divide(BigDecimal.valueOf(count),PRECISION);
    }
    private static Map<String,Object> obj(Object... args) {
        if(args.length%2!=0)throw new IllegalArgumentException("odd map args");
        Map<String,Object> m=new LinkedHashMap<>();
        for(int i=0;i<args.length;i+=2)m.put((String)args[i],args[i+1]);
        return m;
    }
    private static String json(Object value) {return TopFlopCompactStageJsExporter.Json.write(value);}

    /** Reversible positional encoding: only public VIEWS, never source match histories. */
    private static List<Object> packEvents(Object raw) {
        @SuppressWarnings("unchecked") Map<String,BigDecimal> events=(Map<String,BigDecimal>)raw;
        List<Object> compact=new ArrayList<>();
        if(events==null)return compact;
        for(int i=0;i<EVENTS.size();i++) {
            BigDecimal v=events.get(EVENTS.get(i));
            if(v!=null && v.signum()!=0) {compact.add(i);compact.add(v);}
        }
        return compact;
    }
    @SuppressWarnings("unchecked")
    private static List<Object> packCareer(Map<String,Object> row) {
        Map<String,Object> r=(Map<String,Object>)row.get("reale");
        Map<String,Object> f=(Map<String,Object>)row.get("fantasy");
        return List.of(row.get("playerKey"), row.get("nome"),row.get("ruoloFca"),
            // null-safe numeric fields: List.of() forbids nulls.
            row.get("codiceStorico") == null ? 0 : row.get("codiceStorico"),
            List.of(r.get("votiValidi"),r.get("sommaVoti"),packEvents(r.get("eventi")),
                r.get("giornateGolSubitiConVoto"),r.get("giornateZeroGolSubitiRegistrati"),
                r.get("mediaGolSubitiSuGiornateVerificate") == null ? "" : r.get("mediaGolSubitiSuGiornateVerificate")),
            List.of(f.get("contestiOsservati"),f.get("conteggiati"),f.get("conVotoRealeValido"),
                f.get("sommaVotiRealiApportati"),f.get("sommaFantavotiApportati"),
                f.get("panchinaNonConteggiata"),f.get("fuoriLista"),f.get("utilizzoDaVerificare"),
                packEvents(f.get("eventiApportati"))));
    }
    @SuppressWarnings("unchecked")
    private static List<Object> packTeam(Map<String,Object> row) {
        Map<String,Object> f=(Map<String,Object>)row.get("apporto");
        return List.of(row.get("playerKey"), row.get("idSquadraInterno"),
            row.get("idCompetizioneInterno"),
            List.of(f.get("contestiOsservati"),f.get("conteggiati"),f.get("conVotoRealeValido"),
                f.get("sommaVotiRealiApportati"),f.get("sommaFantavotiApportati"),
                f.get("panchinaNonConteggiata"),f.get("fuoriLista"),f.get("utilizzoDaVerificare"),
                packEvents(f.get("eventiApportati"))));
    }
    private static List<Object> packs(String kind,List<Map<String,Object>> rows) {
        if(kind.equals("Careers"))return rows.stream().map(x->(Object)packCareer(x)).toList();
        if(kind.equals("Teams"))return rows.stream().map(x->(Object)packTeam(x)).toList();
        return new ArrayList<>(rows);
    }
    private static Map<String,Object> dictionaries(SeasonView view) throws IOException {
        Map<Integer,String> teams=new TreeMap<>();
        Map<Integer,List<String>> competitions=new TreeMap<>();
        for(var row:view.teams) {
            int t=((Number)row.get("idSquadraInterno")).intValue();
            int comp=((Number)row.get("idCompetizioneInterno")).intValue();
            String tn=(String)row.get("fantasquadra");
            List<String> cn=List.of((String)row.get("competizione"),(String)row.get("famiglia"));
            String old=teams.putIfAbsent(t,tn);
            if(old!=null && !old.equals(tn))throw new IOException("Conflicting team ID "+t);
            List<String> oc=competitions.putIfAbsent(comp,cn);
            if(oc!=null && !oc.equals(cn))throw new IOException("Conflicting competition ID "+comp);
        }
        return obj("squadre",teams.entrySet().stream().map(e->List.of(e.getKey(),e.getValue())).toList(),
            "competizioni",competitions.entrySet().stream().map(e->List.of(e.getKey(),e.getValue().get(0),
                e.getValue().get(1))).toList());
    }

    public static Result export(Connection c,List<String> seasonIds,Path output,Path reports)
            throws SQLException,IOException {
        Objects.requireNonNull(c);Objects.requireNonNull(seasonIds);Objects.requireNonNull(output);
        if(seasonIds.isEmpty() || seasonIds.stream().distinct().count()!=seasonIds.size())
            throw new IllegalArgumentException("Empty or duplicate season list");
        for(String s:seasonIds)if(s==null || !s.matches("[0-9]{4}_[0-9]{4}"))
            throw new IllegalArgumentException("Invalid season: "+s);
        if(Files.exists(output))throw new IOException("Staging must be a NEW directory: "+output);
        Files.createDirectories(output);
        List<FileInfo> files=new ArrayList<>();
        List<Map<String,Object>> metadata=new ArrayList<>();
        int players=0;long real=0,fantasy=0;
        for(String season:seasonIds) {
            var loaded=TopFlopJdbcArchiveAssembler.loadSeason(c,season);
            var canonical=reports==null ? Map.<TopFlopCanonicalScorecards.MatchTeam,String>of()
                :TopFlopCanonicalScorecards.load(reports,season);
            SeasonView view=build(loaded,canonical);
            players+=view.careers.size();real+=loaded.archive().realDayCount();
            fantasy+=loaded.archive().fantasyFactCount();
            List<FileInfo> seasonFiles=new ArrayList<>();
            seasonFiles.addAll(shard(output,season,"Careers",packs("Careers",view.careers)));
            seasonFiles.addAll(shard(output,season,"Teams",packs("Teams",view.teams)));
            seasonFiles.addAll(shard(output,season,"Records",packs("Records",view.records)));
            seasonFiles.addAll(shard(output,season,"Highlights",packs("Highlights",view.highlights)));
            files.addAll(seasonFiles);
            metadata.add(obj("stagione",season,"contatori",view.counters,"dizionari",dictionaries(view),
                "files",seasonFiles.stream().map(f->obj("nome",f.name(),"tipo",f.kind(),
                    "righe",f.rows(),"byte",f.bytes(),"sha256",f.sha256())).toList()));
            System.out.println("VIEWS_SEASON_OK "+season+" players="+view.careers.size()+
                " fantasy="+loaded.archive().fantasyFactCount()+" files="+seasonFiles.size()+
                " bytes="+seasonFiles.stream().mapToLong(FileInfo::bytes).sum());
        }
        Map<String,Object> status=new LinkedHashMap<>();
        for(int i=1;i<=20;i++) status.put(String.format("TF%02d",i),"ATTIVA");
        for(String id:List.of("TF05","TF06"))status.put(id,"AGGREGATI_DISPONIBILI_SOGLIE_NON_APPROVATE");
        for(String id:List.of("TF11","TF12","TF13"))status.put(id,"NON_ATTIVA_ESCLUSIONI_NON_VERIFICATE");
        for(String id:List.of("TF14","TF15"))status.put(id,"NON_ATTIVA_BASELINE_E_SOGLIE_DA_VALIDARE");
        status.put("TF16","CONFRONTO_PER_SQUADRA_SENZA_VINCITORE_ARBITRARIO");
        Map<String,Object> facade=obj("schemaVersion","4.0-public-views-v1",
            "catalogo","TF01-TF20","status","STAGING_ONLY_NOT_PUBLISHED",
            "limitBytesPerJs",LIMIT_BYTES,"identity","SEASON_LOCAL_NO_AUTO_MERGE",
            "viste",List.of("Careers","Teams","Records","Highlights"),"eventi",EVENTS,
            "attivazioneRecord",status,"stagioni",metadata);
        byte[] b=("window.fcmRecordsNextTopFlopViews = "+json(facade)+";\n")
            .getBytes(StandardCharsets.UTF_8);
        files.add(write(output,FACADE,"","Facade",1,b));
        return new Result(List.copyOf(files),seasonIds.size(),players,real,fantasy,output);
    }
    static List<FileInfo> shard(Path dir,String season,String kind,
                                        List<Object> entries) throws IOException {
        String prefix="(function(){var w=window.fcmRecordsNextTopFlopViewsData||(window.fcmRecordsNextTopFlopViewsData={});"+
            "var g=w."+kind+"||(w."+kind+"={});var a=g[\""+season+"\"]||(g[\""+season+"\"]=[]);a.push(...[";
        String suffix="]);})();\n";
        byte[] overhead=(prefix+suffix).getBytes(StandardCharsets.UTF_8);
        List<FileInfo> result=new ArrayList<>();
        StringBuilder current=new StringBuilder(prefix);int used=overhead.length,count=0,part=1;
        for(var entry:entries) {
            String json=json(entry);
            int add=json.getBytes(StandardCharsets.UTF_8).length+(count==0?0:1);
            if(count>0 && used+add>TARGET_BYTES) {
                result.add(write(dir,name(season,kind,part++),season,kind,count,
                    (current.toString()+suffix).getBytes(StandardCharsets.UTF_8)));
                current=new StringBuilder(prefix);used=overhead.length;count=0;add=json.getBytes(StandardCharsets.UTF_8).length;
            }
            if(used+add>LIMIT_BYTES)throw new IOException("Oversize single view row "+season+" "+kind);
            if(count>0)current.append(',');current.append(json);used+=add;count++;
        }
        if(count>0)result.add(write(dir,name(season,kind,part),season,kind,count,
            (current.toString()+suffix).getBytes(StandardCharsets.UTF_8)));
        return result;
    }
    private static String name(String season,String kind,int n) {
        return "fcmRecordsNext_TopFlopViews."+kind+"."+season+"."+String.format("%03d",n)+".js";
    }
    private static FileInfo write(Path dir,String name,String season,String kind,int rows,byte[] bytes)
        throws IOException {
        if(bytes.length>LIMIT_BYTES)throw new IOException("JS size limit exceeded "+name+" "+bytes.length);
        Path target=dir.resolve(name);Files.write(target,bytes,
            java.nio.file.StandardOpenOption.CREATE_NEW);
        if(Files.size(target)!=bytes.length)throw new IOException("JS size changed: "+name);
        String hash;
        try {
            byte[] digest=MessageDigest.getInstance("SHA-256").digest(bytes);
            StringBuilder s=new StringBuilder();for(byte x:digest)s.append(String.format("%02x",x&255));hash=s.toString();
        }catch(Exception ex){throw new IllegalStateException(ex);}
        return new FileInfo(name,season,kind,rows,bytes.length,hash);
    }

    /** CLI, read-only database: db stage reports-or-dash season1 season2 ... */
    public static void main(String[] args) throws Exception {
        if(args.length<4)throw new IllegalArgumentException("<database> <new-stage> <reports-or-dash> <season...>");
        Path db=Path.of(args[0]).toAbsolutePath().normalize();
        if(!Files.isRegularFile(db))throw new IOException("Missing SQLite: "+db);
        Path out=Path.of(args[1]).toAbsolutePath().normalize();
        Path reports=args[2].equals("-")?null:Path.of(args[2]).toAbsolutePath().normalize();
        if(reports!=null && !Files.isDirectory(reports))throw new IOException("Missing reports: "+reports);
        Class.forName("org.sqlite.JDBC");
        try(Connection c=DriverManager.getConnection("jdbc:sqlite:"+db.toUri()+"?mode=ro")) {
            Result r=export(c,List.of(args).subList(3,args.length),out,reports);
            long total=r.files().stream().mapToLong(FileInfo::bytes).sum();
            long maximum=r.files().stream().mapToLong(FileInfo::bytes).max().orElse(0);
            System.out.println("TOP_FLOP_VIEWS_STAGE_OK seasons="+r.seasons()+" players="+r.players()+
                " realDays="+r.sourceRealDays()+" fantasyContexts="+r.sourceFantasyContexts()+
                " files="+r.files().size()+" bytes="+total+" maxBytes="+maximum);
        }
    }
}
~~~~

## `src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopPublicViewsStageSmokeTest.java`

SHA-256: `9bdf79f0c58e1777e68b89e491133f6d57d7210e64f5498e0152c8306cb5ac7c`

~~~~java
package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.topflop.TopFlopFacts.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Deterministic regression: goalkeeper can score, assist, save and concede. */
public final class TopFlopPublicViewsStageSmokeTest {
    private static BigDecimal n(int x) {return BigDecimal.valueOf(x);}
    private static void check(boolean yes,String what) {
        if(!yes)throw new AssertionError(what);
    }
    public static void main(String[] args) throws Exception {
        String season="2026_2027";
        PlayerSeason keeper=new PlayerSeason(season,7001,90001);
        TopFlopArchive archive=new TopFlopArchive();
        var profile=new TopFlopFcaJdbcReader.PlayerProfile(keeper,"PORTIERE TEST",
            Map.of("GiocatoreA.RUOLO","1"));
        Map<String,BigDecimal> events=Map.of("GOLFATTI1",n(1),
            "GOLFATTISURIGORE1",n(1),"ASSIST",n(1),
            "GOLSUBITI",n(2),"GOLSUBITISURIGORE",n(1),"RIGPAR",n(1));
        RealDayKey day1=new RealDayKey(keeper,7);
        RealDayKey day2=new RealDayKey(keeper,8);
        archive.addRealDay(new RealDay(day1,VoteState.VALID,n(8),events,Map.of()));
        archive.addRealDay(new RealDay(day2,VoteState.VALID,n(6),
            Map.of("GOLSUBITI",n(0)),Map.of()));
        var src=Map.of("FCM.Fantasquadra.Nome","Test Team",
            "FCM.Formazione.EntratoRiserva","0","FCM.Giornata.Desc","5a Camp.");
        archive.addFantasyFact(new FantasyFact(new FantasyKey(keeper,1,110,201),day1,
            "Serie A",CompetitionFamily.CAMPIONATO,5,true,Usage.COUNTED,n(14),events,null,src));
        archive.addFantasyFact(new FantasyFact(new FantasyKey(keeper,1,120,202),day1,
            "Serie A",CompetitionFamily.CAMPIONATO,5,true,Usage.COUNTED,n(14),events,null,
            Map.of("FCM.Fantasquadra.Nome","Second Team")));
        archive.addFantasyFact(new FantasyFact(new FantasyKey(keeper,1,130,201),day2,
            "Serie A",CompetitionFamily.CAMPIONATO,6,false,Usage.BENCH_UNUSED,
            null,Map.of(),null,src));
        var loaded=new TopFlopJdbcArchiveAssembler.LoadedSeason(archive,
            Map.of(keeper.fcaPlayerId(),profile),List.of(),
            new TopFlopFcaJdbcReader.SeasonLoad(season,Map.of(keeper.fcaPlayerId(),profile),2,2,2,0,0),
            new TopFlopFcmJdbcReader.SeasonLoad(season,3,3,0,3,0,0),0,2,0);
        var view=TopFlopPublicViewsStageExporter.build(loaded,Map.of(
            new TopFlopCanonicalScorecards.MatchTeam(110,201),
            "https://example.invalid/lega2026/ris.php?Gio=5"));
        check(view.careers.size()==1 && view.teams.size()==2,"player/team aggregates");
        var career=view.careers.get(0);
        @SuppressWarnings("unchecked") var real=(Map<String,Object>)career.get("reale");
        @SuppressWarnings("unchecked") var fantasy=(Map<String,Object>)career.get("fantasy");
        @SuppressWarnings("unchecked") var realEvents=(Map<String,BigDecimal>)real.get("eventi");
        @SuppressWarnings("unchecked") var apported=(Map<String,BigDecimal>)fantasy.get("eventiApportati");
        check(realEvents.get("GOLFATTI1").compareTo(n(1))==0,"one real goal (penalty subset)");
        check(realEvents.get("GOLSUBITI").compareTo(n(2))==0,"two conceded incl penalty");
        check(realEvents.get("RIGPAR").compareTo(n(1))==0,"one penalty save");
        check(realEvents.get("ASSIST").compareTo(n(1))==0,"goalkeeper assist");
        check(apported.get("GOLFATTI1").compareTo(n(2))==0,"two fantasy credited, real once");
        check(fantasy.get("conteggiati").equals(2),"two counted contexts");
        check(fantasy.get("panchinaNonConteggiata").equals(1),"bench unused");
        check(real.get("giornateZeroGolSubitiRegistrati").equals(1),"one explicitly zero goals day");
        check(view.records.stream().anyMatch(x->x.get("recordId").equals("TF20") &&
            x.get("ambito").equals("portieriGolStagione")),"keeper goal TF20");
        check(view.records.stream().anyMatch(x->x.get("recordId").equals("TF07")),"keeper goal TF07");
        check(view.records.stream().anyMatch(x->x.get("recordId").equals("TF08")),"keeper assist TF08");
        check(view.highlights.stream().anyMatch(x->x.get("tipo").equals("prestazioneReale") &&
            x.get("giornataSerieA").equals(7)),"keeper goal highlight");
        check(view.records.stream().anyMatch(x->x.get("recordId").equals("TF18") &&
            x.get("ambito").equals("portieriGolSubitiApportati")),"keeper conceded counted fantasy");
        check(view.records.stream().anyMatch(x->x.get("recordId").equals("TF20") &&
            x.get("ambito").equals("portieriAssistApportati")),"keeper credited assist");
        check(view.records.stream().anyMatch(x->x.get("recordId").equals("TF20") &&
            x.get("ambito").equals("portieriGolSuRigoreApportati")),"keeper credited penalty goal subset");
        @SuppressWarnings("unchecked") Map<String,Object> day2Highlight = view.highlights.stream()
            .filter(x -> x.get("tipo").equals("prestazioneReale") && x.get("giornataSerieA").equals(8))
            .findFirst().orElseThrow();
        @SuppressWarnings("unchecked") var contexts =
            (List<Map<String,Object>>)day2Highlight.get("contestiFantasy");
        check(contexts.size()==1 && contexts.get(0).get("schieramento").equals("RISERVA_NON_ENTRATA") &&
            contexts.get(0).get("conteggiato").equals(false),"unused bench must not say COUNTED");
        @SuppressWarnings("unchecked") Map<String,Object> day1Highlight=view.highlights.stream()
            .filter(x -> x.get("tipo").equals("prestazioneReale") && x.get("giornataSerieA").equals(7))
            .findFirst().orElseThrow();
        @SuppressWarnings("unchecked") List<Map<String,Object>> sourceContexts=
            (List<Map<String,Object>>)day1Highlight.get("contestiFantasy");
        check(sourceContexts.stream().anyMatch(x->
            "https://example.invalid/lega2026/ris.php?Gio=5".equals(x.get("urlTabellino"))),
            "only canonical scorecard URL is reused");
        java.nio.file.Path empty=java.nio.file.Files.createTempDirectory("rn4_tf_views_oversize_");
        try {
            boolean rejected=false;
            try {
                TopFlopPublicViewsStageExporter.shard(empty,season,"Careers",
                    List.of(Map.of("oversize","X".repeat(1_500_000))));
            } catch(java.io.IOException ex) {rejected=ex.getMessage().contains("Oversize");}
            check(rejected,"one oversized view entry must fail rather than truncate or publish");
        } finally {java.nio.file.Files.deleteIfExists(empty);}
        check(view.records.stream().noneMatch(x->List.of("TF11","TF12","TF13","TF14","TF15").contains(x.get("recordId"))),
            "unverified rankings must remain off");
        System.out.println("TOP_FLOP_PUBLIC_VIEWS_KEEPER_AND_DOUBLE_CONTEXT_TEST_OK");
    }
}
~~~~

## `visualizzatori/js/fcmRecordsNextTopFlopViewsDecoder-v1.js`

SHA-256: `fc03bec0452261c19fba68339c07c75309639f1a6d80e91a39a8cd0690cd9296`

~~~~javascript
/* RecordsNext 4.0 - public Top & Flop VIEWS v1.
 * Only selected records and aggregated careers/teams; no complete FCM/FCA dump.
 * Lazy loading: do NOT preload all years or sections. Caller applies historical cutoff.
 */
(function (root) {
  'use strict';
  var pending = Object.create(null);
  function facade() {
    var f = root.fcmRecordsNextTopFlopViews;
    if (!f || f.schemaVersion !== '4.0-public-views-v1') {
      throw new Error('RecordsNext TopFlop: incompatible views facade');
    }
    return f;
  }
  function meta(season) {
    var found = facade().stagioni.find(function (s) { return s.stagione === season; });
    if (!found) throw new Error('Unknown season (or past cutoff): ' + season);
    return found;
  }
  function pairs(vector) {
    var keys = facade().eventi, out = {};
    if (!Array.isArray(vector) || vector.length % 2 !== 0) throw new Error('Invalid event vector');
    for (var i = 0; i < vector.length; i += 2) {
      if (!Number.isInteger(vector[i]) || !keys[vector[i]] || Object.hasOwn(out,keys[vector[i]]))
        throw new Error('Invalid event index');
      out[keys[vector[i]]] = vector[i + 1];
    }
    return out;
  }
  function average(sum,n) { return n > 0 ? sum / n : null; }
  function decodeCareer(row) {
    if (!Array.isArray(row) || row.length !== 6) throw new Error('Invalid career row');
    var r = row[4], f = row[5];
    if (r.length !== 6 || f.length !== 9) throw new Error('Invalid aggregates');
    return {
      playerKey:row[0],nome:row[1],ruoloFca:row[2],
      codiceStorico:row[3] === 0 ? null : row[3],identitaStoricaVerificata:false,
      reale:{votiValidi:r[0],sommaVoti:r[1],mediaVoto:average(r[1],r[0]),
        eventi:pairs(r[2]),giornateGolSubitiConVoto:r[3],
        giornateZeroGolSubitiRegistrati:r[4],
        mediaGolSubitiSuGiornateVerificate:r[5] === '' ? null : r[5]},
      fantasy:{contestiOsservati:f[0],conteggiati:f[1],conVotoRealeValido:f[2],
        sommaVotiRealiApportati:f[3],mediaVotoRealeApportato:average(f[3],f[2]),
        sommaFantavotiApportati:f[4],fantamediaApportata:average(f[4],f[1]),
        panchinaNonConteggiata:f[5],fuoriLista:f[6],utilizzoDaVerificare:f[7],
        eventiApportati:pairs(f[8])}
    };
  }
  function dict(rows) {
    var obj=Object.create(null);
    (rows || []).forEach(function (r) {
      if (obj[r[0]] !== undefined) throw new Error('Duplicate dictionary key');
      obj[r[0]] = r;
    });
    return obj;
  }
  function decodeTeam(row, season) {
    if (!Array.isArray(row) || row.length !== 4 || row[3].length !== 9)
      throw new Error('Invalid team row');
    var md=meta(season), ts=dict(md.dizionari.squadre), cs=dict(md.dizionari.competizioni),f=row[3];
    if (!ts[row[1]] || !cs[row[2]]) throw new Error('Missing team/competition dictionary');
    return {playerKey:row[0],idSquadraInterno:row[1],fantasquadra:ts[row[1]][1],
      idCompetizioneInterno:row[2],competizione:cs[row[2]][1],famiglia:cs[row[2]][2],
      apporto:{contestiOsservati:f[0],conteggiati:f[1],conVotoRealeValido:f[2],
        sommaVotiRealiApportati:f[3],mediaVotoRealeApportato:average(f[3],f[2]),
        sommaFantavotiApportati:f[4],fantamediaApportata:average(f[4],f[1]),
        panchinaNonConteggiata:f[5],fuoriLista:f[6],utilizzoDaVerificare:f[7],
        eventiApportati:pairs(f[8])}};
  }
  function store() {
    return root.fcmRecordsNextTopFlopViewsData || (root.fcmRecordsNextTopFlopViewsData={});
  }
  function loadScript(url) {
    if (pending[url]) return pending[url];
    pending[url] = new Promise(function (resolve,reject) {
      var script=document.createElement('script');script.async=true;script.src=url;
      script.onload=resolve;
      script.onerror=function () {delete pending[url];reject(new Error('Failed to load '+url));};
      document.head.appendChild(script);
    });
    return pending[url];
  }
  function baseUrl(base,name) {return String(base || '.').replace(/\/+$/,'')+'/'+name;}
  function loadFacade(base) {
    if (root.fcmRecordsNextTopFlopViews) return Promise.resolve(facade());
    return loadScript(baseUrl(base,'fcmRecordsNext_TopFlopViews.js')).then(facade);
  }
  function loadView(base,season,kind) {
    if (!['Careers','Teams','Records','Highlights'].includes(kind))
      return Promise.reject(new Error('Unknown view section'));
    return loadFacade(base).then(function () {
      var files=meta(season).files.filter(function (f) {return f.tipo === kind;});
      return Promise.all(files.map(function (f) {return loadScript(baseUrl(base,f.nome));})).then(function () {
        var data=(store()[kind] || {})[season] || [];
        if (data.length !== files.reduce(function (n,f) {return n+f.righe;},0))
          throw new Error('Incomplete '+kind+' shard coverage for '+season);
        if (kind === 'Careers') return data.map(decodeCareer);
        if (kind === 'Teams') return data.map(function (r) {return decodeTeam(r,season);});
        return data;
      });
    });
  }
  root.RecordsNextTopFlopViewsV1 = Object.freeze({
    facade:facade,meta:meta,pairs:pairs,
    decodeCareer:decodeCareer,decodeTeam:decodeTeam,
    loadFacade:loadFacade,loadView:loadView
  });
})(typeof window !== 'undefined' ? window : globalThis);
~~~~

## `tools/Validate-RecordsNext4TopFlopPublicViews-v1.py`

SHA-256: `ed8049b492edaaa024606b1a1db0d85a3f3bc7ac25ae07c17cda0f68d254ac8e`

~~~~python
#!/usr/bin/env python3
"""Independent mandatory JSON/JS validation; requires Python 3 only, not Node.
Never changes staging or source DB. Fails closed on coverage, corruption and >1.5MB.
"""
from __future__ import annotations
import argparse,collections,hashlib,json,re,sqlite3
from pathlib import Path

MAX=1_500_000
FACADE='fcmRecordsNext_TopFlopViews.js'
KIND={'Careers','Teams','Records','Highlights'}
DISABLED={'TF05','TF06','TF11','TF12','TF13','TF14','TF15'}

def parse_js(file:Path,kind:str|None=None):
    body=file.read_text(encoding='utf-8')
    if kind is None:
        pre='window.fcmRecordsNextTopFlopViews = '
        if not body.startswith(pre) or not body.endswith(';\n'):raise ValueError('Bad facade JS: '+str(file))
        return json.loads(body[len(pre):-2])
    marker='a.push(...['
    suffix=']);})();\n'
    if body.count(marker)!=1 or not body.endswith(suffix):raise ValueError('Bad shard wrapper: '+str(file))
    return json.loads('['+body.split(marker,1)[1][:-len(suffix)]+']')

def check_pair(vector,keys):
    if not isinstance(vector,list) or len(vector)%2:raise ValueError('Invalid sparse events')
    seen=set()
    for i in range(0,len(vector),2):
        key=vector[i];value=vector[i+1]
        if not isinstance(key,int) or key<0 or key>=len(keys) or key in seen:raise ValueError('Event dictionary invalid')
        seen.add(key)
        if not isinstance(value,(int,float)) or isinstance(value,bool):raise ValueError('Non numeric event')
    return dict((keys[vector[i]],vector[i+1]) for i in range(0,len(vector),2))

def validate(stage:Path,db:Path|None=None):
    if not stage.is_dir():raise ValueError('Missing staged directory')
    fpath=stage/FACADE
    if not fpath.is_file():raise ValueError('Missing views facade')
    f=parse_js(fpath)
    if f.get('schemaVersion')!='4.0-public-views-v1' or f.get('status')!='STAGING_ONLY_NOT_PUBLISHED':
        raise ValueError('Unexpected schema/status')
    if f.get('identity')!='SEASON_LOCAL_NO_AUTO_MERGE':raise ValueError('Historical identity hazard')
    keys=f.get('eventi')
    if len(keys)!=len(set(keys)):raise ValueError('Duplicate event names')
    statuses=f['attivazioneRecord']
    if set(statuses)!={f'TF{i:02}' for i in range(1,21)}:raise ValueError('Record coverage')
    for x in DISABLED:
        if statuses[x]=='ATTIVA':raise ValueError('Unverified ranking activated '+x)
    visited={FACADE}
    numbers=collections.Counter();sizes=collections.Counter();total=fpath.stat().st_size
    maximum=total
    season_list=[]
    for season in f['stagioni']:
        s=season['stagione'];season_list.append(s)
        if not re.fullmatch(r'\d{4}_\d{4}',s):raise ValueError('Invalid season id '+s)
        rows={k:[] for k in KIND}
        for info in season['files']:
            name=info['nome'];kind=info['tipo']
            if kind not in KIND or name in visited or not re.fullmatch(
              rf'fcmRecordsNext_TopFlopViews\.{kind}\.{s}\.\d{{3}}\.js',name):
                raise ValueError('Invalid shard identity '+name)
            visited.add(name)
            file=stage/name
            if not file.is_file():raise ValueError('Missing '+name)
            b=file.read_bytes();size=len(b)
            if size>MAX or size>950000 or size!=info['byte']:raise ValueError('Invalid shard size '+name)
            if hashlib.sha256(b).hexdigest()!=info['sha256']:raise ValueError('SHA mismatch '+name)
            entries=parse_js(file,kind)
            if len(entries)!=info['righe']:raise ValueError('Rows mismatch '+name)
            rows[kind].extend(entries)
            sizes[s]+=size;total+=size;maximum=max(maximum,size)
        careers,teams,records,highlights=(rows[x] for x in ('Careers','Teams','Records','Highlights'))
        counter=season['contatori']
        if len(careers)!=counter['players'] or len(teams)!=counter['teamCompetitionAggregates'] or len(highlights)!=counter['selectedHighlights']:
            raise ValueError('Per-season coverage mismatch '+s)
        ids=set();counted=0
        for c in careers:
            if len(c)!=6 or not c[0].startswith(s+':') or c[0] in ids:raise ValueError('Invalid career '+s)
            ids.add(c[0]);r,fant=c[4],c[5]
            if len(r)!=6 or len(fant)!=9:raise ValueError('Invalid career statistics')
            realEvents=check_pair(r[2],keys);fantasyEvents=check_pair(fant[8],keys)
            if realEvents.get('GOLFATTI1',0)<realEvents.get('GOLFATTISURIGORE1',0):raise ValueError('Doubled penalty goals')
            if realEvents.get('GOLSUBITI',0)<realEvents.get('GOLSUBITISURIGORE',0):raise ValueError('Doubled conceded goals')
            if r[0]<0 or fant[1]>fant[0]:raise ValueError('Invalid denominators')
            counted+=fant[1]
        if counted!=counter['fantasyCounted']:raise ValueError('Counted player sum mismatch '+s)
        d=season['dizionari']
        teamIds={r[0] for r in d['squadre']}
        compIds={r[0] for r in d['competizioni']}
        if len(teamIds)!=len(d['squadre']) or len(compIds)!=len(d['competizioni']):raise ValueError('Dictionary conflicts')
        if any(c[1].strip().lower()=='youth league' for c in d['competizioni']):raise ValueError('Youth League in output')
        teamCounted=0
        for t in teams:
            if len(t)!=4 or t[0] not in ids or t[1] not in teamIds or t[2] not in compIds:
                raise ValueError('Invalid team aggregate')
            g=t[3]
            if len(g)!=9 or g[1]>g[0]:raise ValueError('Invalid team denominator')
            check_pair(g[8],keys);teamCounted+=g[1]
        if teamCounted!=counted:raise ValueError('Team sums and player sums differ')
        highlightKeys={h['key'] for h in highlights}
        if len(highlightKeys)!=len(highlights):raise ValueError('Duplicate selected highlights')
        for h in highlights:
            if h.get('competizione','').lower()=='youth league':raise ValueError('Youth highlight')
            for x in [h,*(h.get('contestiFantasy') or [])]:
                link=x.get('urlTabellino')
                if link is not None and not re.match(r'^https?://',link):raise ValueError('Fake or invalid scorecard')
        for rank in records:
            if rank['recordId'] in DISABLED or rank['recordId'] not in statuses:
                raise ValueError('Unverified record published '+rank['recordId'])
            if rank['pariMerito']!=len(rank['risultati']) or not rank['risultati']:
                raise ValueError('Tie integrity')
            for entry in rank['risultati']:
                if entry.get('highlightKey') and entry['highlightKey'] not in highlightKeys:
                    raise ValueError('Orphan highlight '+entry['highlightKey'])
                if rank['recordId'] in ('TF17','TF18','TF19') and entry['calciatore']['ruoloFca']!=1:
                    raise ValueError('Non-keeper in goalkeeper only record')
        numbers.update(players=len(careers),teams=len(teams),recordGroups=len(records),
            highlights=len(highlights),realDays=counter['realDays'],
            fantasyContexts=counter['fantasyContexts'],fantasyCounted=counter['fantasyCounted'])
    if season_list!=sorted(set(season_list)):raise ValueError('Unexpected chronological season order')
    actual={p.name for p in stage.glob('*.js')}
    if actual!=visited:raise ValueError('Stale or extra JS files in staged directory: '+str(actual-visited))
    if db is not None:
        with sqlite3.connect(db.as_uri()+'?mode=ro',uri=True) as c:
            for sm in f['stagioni']:
                s=sm['stagione'];k=sm['contatori']
                real=c.execute(f'SELECT COUNT(*) FROM "raw_{s}_fca_giocain"').fetchone()[0]
                fantasy=c.execute(f'''SELECT COUNT(*) FROM "raw_{s}_fcm_formazione" ff
                    JOIN "raw_{s}_fcm_incontro" ii ON ii.ID=ff.IDINCONTRO
                    JOIN "raw_{s}_fcm_girone" gg ON gg.ID=ii.IDGIRONE
                    JOIN "raw_{s}_fcm_competizione" cc ON cc.ID=gg.IDCOMPETIZIONE
                    WHERE lower(trim(cc.NOME))<>'youth league' ''').fetchone()[0]
                if real!=k['realDays'] or fantasy!=k['fantasyContexts']:
                    raise ValueError('SQLite -> view input reconciliation failed '+s)
    stat={'seasons':len(season_list),'jsFiles':len(visited),
        'totalJsBytes':total,'maxJsBytes':maximum,'sourceCounters':dict(numbers),
        'perSeasonBytes':dict(sorted(sizes.items())),
        'cumulativeLastSiteBytes':total}
    print('TOP_FLOP_PUBLIC_VIEWS_VALIDATED '+json.dumps(stat,sort_keys=True))
    return stat

def main():
    p=argparse.ArgumentParser();p.add_argument('stage',type=Path)
    p.add_argument('--database',type=Path)
    p.add_argument('--json-output',type=Path)
    a=p.parse_args()
    stat=validate(a.stage,a.database)
    if a.json_output:a.json_output.write_text(json.dumps(stat,indent=2),encoding='utf-8')
if __name__=='__main__':main()
~~~~

## `tools/Test-RecordsNext4TopFlopPublicViewsBrowser-v1.js`

SHA-256: `5d09e10bd7aa222a7b1ffce3d527b30fed0d6e0cb9c5430b09f42b726210b5ac`

~~~~javascript
'use strict';
const fs=require('fs'),path=require('path'),vm=require('vm'),crypto=require('crypto'),assert=require('assert');
const stage=process.argv[2],decoder=process.argv[3];
if(!stage || !decoder)throw new Error('Usage: node test.js <stage-js-dir> <decoder>');
const ctx={window:{},console};vm.createContext(ctx);
function load(name){const data=fs.readFileSync(path.join(stage,name),'utf8');
  vm.runInContext(data,ctx,{filename:name,timeout:30000});}
load('fcmRecordsNext_TopFlopViews.js');
vm.runInContext(fs.readFileSync(decoder,'utf8'),ctx,{filename:'decoder',timeout:30000});
const f=ctx.window.fcmRecordsNextTopFlopViews,api=ctx.window.RecordsNextTopFlopViewsV1;
assert.equal(f.schemaVersion,'4.0-public-views-v1');
assert.equal(f.status,'STAGING_ONLY_NOT_PUBLISHED');
const counters={seasons:0,careers:0,teams:0,records:0,highlights:0,realDays:0,fantasy:0,
  fantasyCounted:0,totalBytes:fs.statSync(path.join(stage,'fcmRecordsNext_TopFlopViews.js')).size,
  maxBytes:0,linkHits:0,goaliesWithGoals:0,unverifiedRankings:0};
let previous='';
for(const season of f.stagioni){
  counters.seasons++;
  assert.ok(season.stagione > previous);previous=season.stagione;
  for(const file of season.files){
    const p=path.join(stage,file.nome),bytes=fs.readFileSync(p);
    assert.equal(bytes.length,file.byte);
    assert.ok(bytes.length<=1500000,'JS too large '+file.nome);
    assert.equal(crypto.createHash('sha256').update(bytes).digest('hex'),file.sha256);
    load(file.nome);counters.totalBytes+=bytes.length;
    counters.maxBytes=Math.max(counters.maxBytes,bytes.length);
  }
  const raw=ctx.window.fcmRecordsNextTopFlopViewsData;
  const careers=(raw.Careers||{})[season.stagione]||[],teams=(raw.Teams||{})[season.stagione]||[],
    records=(raw.Records||{})[season.stagione]||[], highlights=(raw.Highlights||{})[season.stagione]||[];
  assert.equal(careers.length,season.contatori.players);
  assert.equal(teams.length,season.contatori.teamCompetitionAggregates);
  assert.equal(records.length,season.files.filter(x=>x.tipo==='Records').reduce((a,x)=>a+x.righe,0));
  assert.equal(highlights.length,season.contatori.selectedHighlights);
  const decoded=careers.map(api.decodeCareer),groups=teams.map(x=>api.decodeTeam(x,season.stagione));
  const keys=new Set(decoded.map(c=>c.playerKey));
  assert.equal(keys.size,decoded.length);
  assert.ok(decoded.every(c=>!c.identitaStoricaVerificata));
  assert.ok(groups.every(x=>keys.has(x.playerKey)));
  assert.ok(groups.every(x=>x.competizione.toLowerCase() !== 'youth league'));
  assert.ok(groups.every(x=>x.apporto.conteggiati<=x.apporto.contestiOsservati));
  assert.ok(decoded.every(x=>x.reale.votiValidi>=0 && x.fantasy.conteggiati<=x.fantasy.contestiOsservati));
  const count=decoded.reduce((s,c)=>s+c.fantasy.conteggiati,0);
  assert.equal(count,season.contatori.fantasyCounted);
  assert.equal(groups.reduce((s,x)=>s+x.apporto.conteggiati,0),count);
  const hmap=new Map(highlights.map(x=>[x.key,x]));
  assert.equal(hmap.size,highlights.length);
  for(const x of highlights){
    assert.ok(x.competizione !== 'Youth League');
    if(x.urlTabellino){assert.ok(/^https?:\/\//.test(x.urlTabellino));counters.linkHits++;}
  }
  for(const rec of records){
    assert.ok(/^TF(0[1-9]|1[0-9]|20)$/.test(rec.recordId));
    assert.ok(!['TF05','TF06','TF11','TF12','TF13','TF14','TF15'].includes(rec.recordId));
    assert.equal(rec.pariMerito,rec.risultati.length);
    for(const entry of rec.risultati){
      if(entry.highlightKey)assert.ok(hmap.has(entry.highlightKey),`Missing ${entry.highlightKey}`);
      if(rec.recordId.startsWith('TF1') && ['TF17','TF18','TF19'].includes(rec.recordId))
        assert.equal(entry.calciatore.ruoloFca,1);
    }
  }
  for(const c of decoded){
    if(c.ruoloFca===1 && (c.reale.eventi.GOLFATTI1||0)>0)counters.goaliesWithGoals++;
    if(c.reale.eventi.GOLFATTI1!==undefined && c.reale.eventi.GOLFATTISURIGORE1!==undefined)
      assert.ok(c.reale.eventi.GOLFATTI1 >= c.reale.eventi.GOLFATTISURIGORE1);
    if(c.reale.eventi.GOLSUBITI!==undefined && c.reale.eventi.GOLSUBITISURIGORE!==undefined)
      assert.ok(c.reale.eventi.GOLSUBITI >= c.reale.eventi.GOLSUBITISURIGORE);
  }
  counters.careers+=decoded.length;counters.teams+=groups.length;
  counters.records+=records.length;counters.highlights+=highlights.length;
  counters.realDays+=season.contatori.realDays;
  counters.fantasy+=season.contatori.fantasyContexts;
  counters.fantasyCounted+=count;
}
assert.ok(counters.careers>0 && counters.records>0);
assert.ok(counters.totalBytes<60000000,'Public views have not reduced 60MB');
console.log('TOP_FLOP_PUBLIC_VIEWS_BROWSER_OK',JSON.stringify(counters));
~~~~

## `tools/Invoke-RecordsNext4TopFlopPublicViews-v1.py`

SHA-256: `5a1eeb41b8059ea2d244f4a7d718ec176bcb696aa98262aa6d0aae518586bd87`

~~~~python
#!/usr/bin/env python3
"""Conservative Windows installer for native TopFlop PUBLIC VIEWS stage v1.
No site publishing; no modification of sqlite, CSS, GUI, publisher or 3.2.
"""
from __future__ import annotations
import argparse,datetime,hashlib,json,os,re,shutil,sqlite3,subprocess,sys,traceback,zipfile
from pathlib import Path

KIT=Path(__file__).resolve().parent.parent
JAVA='it.alterlega.recordsnext.app.topflop.TopFlopPublicViewsStageExporter'
TEST='it.alterlega.recordsnext.app.topflop.TopFlopPublicViewsStageSmokeTest'
INSTALL=[
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopPublicViewsStageExporter.java',
 'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopPublicViewsStageSmokeTest.java',
 'visualizzatori/js/fcmRecordsNextTopFlopViewsDecoder-v1.js',
 'tools/Validate-RecordsNext4TopFlopPublicViews-v1.py',
 'tools/Test-RecordsNext4TopFlopPublicViewsBrowser-v1.js',
 'tools/Invoke-RecordsNext4TopFlopPublicViews-v1.py',
 'tools/Create-RecordsNext4WorkingCodeMd-v25.py',
 'docs/SPECIFICA_VISTE_PUBBLICHE_TOP_FLOP_v1.md',
 'docs/RecordsNext4_CATALOGO_RECORD_E_VISTE_TOPFLOP_v1.md',
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v26.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v25.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v25.md',
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v27.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v26.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v26.md',
]
def sha(p:Path)->str:
 h=hashlib.sha256()
 with p.open('rb') as st:
  for block in iter(lambda:st.read(1048576),b''):h.update(block)
 return h.hexdigest()

def invoke(command,log):
 cmd=list(map(str,command))
 p=subprocess.run(cmd,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,
     text=True,encoding='utf-8',errors='replace')
 log.append('COMMAND: '+Path(cmd[0]).name+' (arguments abbreviated)')
 log.append((p.stdout or '')[-20000:])
 if p.returncode:raise RuntimeError('Command failed: '+str(p.returncode)+'\n'+(p.stdout or '')[-9000:])
 return p.stdout or ''

def reports_hash(folder:Path):
 reports=sorted(folder.rglob('season_normalized_*.json'))
 if not reports:raise RuntimeError('No canonical 3.2 reports in '+str(folder))
 h=hashlib.sha256()
 for report in reports:
  h.update(report.relative_to(folder).as_posix().encode('utf-8'))
  h.update(bytes.fromhex(sha(report)))
 return h.hexdigest()

def main()->int:
 cli=argparse.ArgumentParser()
 cli.add_argument('--project-dir',default=r'D:\DEV_APPS\RecordsNext4.0')
 cli.add_argument('--downloads-dir',default=r'D:\DEV_APPS\downloads')
 cli.add_argument('--database');cli.add_argument('--reports-dir');cli.add_argument('--jar')
 cli.add_argument('--seasons',help='Comma-separated pilot season list, otherwise all 21')
 args=cli.parse_args()
 root=Path(args.project_dir).resolve();downloads=Path(args.downloads_dir).resolve()
 downloads.mkdir(parents=True,exist_ok=True)
 stamp=datetime.datetime.now().strftime('%Y%m%d_%H%M%S_%f')
 report=downloads/('RecordsNext4_TopFlopVistePubbliche_OUTPUT_v1_'+stamp+'.zip')
 build=downloads/('RecordsNext4_TopFlopVistePubbliche_BUILD_v1_'+stamp)
 state={'status':'ERROR','version':'v1-public-views','checks':[],
     'installed':[],'alreadyIdentical':[],'project':str(root)}
 logs=[];installed=[]
 try:
  if not (root/'pom.xml').is_file():raise RuntimeError('Missing 4.0 project: '+str(root))
  if not (root/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v24.md').is_file():
   raise RuntimeError('Bibbia v24 missing: run verified Compact kit v2 first')
  if (root/'.git').exists() and shutil.which('git'):
   branch=invoke(['git','-C',root,'branch','--show-current'],logs).strip()
   if branch!='feature/recordsnext4-players':raise RuntimeError('Wrong Git branch: '+branch)
  manifest=json.loads((KIT/'manifest-sha256.json').read_text(encoding='utf-8'))
  if set(manifest['files'])!=set(INSTALL):raise RuntimeError('Kit manifest mismatch')
  baseline=json.loads((KIT/'baseline-hashes.json').read_text(encoding='utf-8'))
  for rel,expected in baseline.items():
   if not (root/rel).is_file() or sha(root/rel)!=expected:
    raise RuntimeError('Existing 3.2/4.0 source changed; refusing unreviewed patch: '+rel)
  for rel in INSTALL:
   src=KIT/rel;dst=root/rel
   if not src.is_file() or sha(src)!=manifest['files'][rel]:
    raise RuntimeError('Invalid kit file SHA-256: '+rel)
   if dst.exists():
    if not dst.is_file() or sha(dst)!=sha(src):
     raise RuntimeError('Existing file differs: no overwrite: '+rel)
    state['alreadyIdentical'].append(rel)
  javac=shutil.which('javac');java=shutil.which('java')
  if not javac or not java:raise RuntimeError('Java JDK 21 missing')
  version=invoke([javac,'-version'],logs)
  m=re.search(r'javac\s+(\d+)',version)
  if not m or int(m.group(1))<21:raise RuntimeError('JDK >=21 required: '+version)
  db=(Path(args.database).resolve() if args.database else
      next(iter(sorted((root/'data/database').glob('recordsnext-2026_2027-verifica-v1_*.db'))),None))
  if db is None or not db.is_file():raise RuntimeError('Updated 2026/27 SQLite missing; use --database')
  jar=(Path(args.jar).resolve() if args.jar else next((x for x in [
       root/'target/RecordsNext.jar',
       root.parent/'RecordsNext2.0/target/RecordsNext.jar',
       root.parent/'RecordsNext2.0/release/RecordsNext_3.2.0_FULL/payload/RecordsNext.jar']
       if x.is_file()),None))
  if jar is None:raise RuntimeError('3.2 runtime JAR missing; use --jar')
  normalized=(Path(args.reports_dir).resolve() if args.reports_dir else
              (root.parent/'RecordsNext2.0/data/reports').resolve())
  if not normalized.is_dir():raise RuntimeError('Canonical 3.2 reports directory missing: '+str(normalized))
  dbBefore=sha(db);norm=reports_hash(normalized)
  sourceFingerprint=hashlib.sha256((json.dumps(baseline,sort_keys=True)+
      sha(KIT/INSTALL[0])).encode('utf-8')).hexdigest()
  state.update(database=str(db),databaseSha256Before=dbBefore,
    canonicalReportsSha256=norm,sourceFingerprint=sourceFingerprint)
  with sqlite3.connect(db.as_uri()+'?mode=ro',uri=True) as c:
   tables=[x[0] for x in c.execute("SELECT name FROM sqlite_master WHERE type='table'")]
  available=sorted({m.group(1) for x in tables
     if (m:=re.fullmatch(r'raw_(\d{4}_\d{4})_fcm_formazione',x))})
  if not available:raise RuntimeError('No FCM formation seasons in SQLite')
  seasons=available
  pilot=''
  if args.seasons:
   seasons=[x.strip() for x in args.seasons.split(',')]
   if not seasons or len(set(seasons))!=len(seasons) or any(s not in available for s in seasons):
    raise RuntimeError('Invalid pilot seasons')
   pilot='_PILOT_'+'_'.join(seasons)
  state['seasons']=seasons
  rootStage=root/'data/site-export-staging'/('topflop-public-views-v1'+pilot+'_'+
      dbBefore[:10]+'_'+norm[:10]+'_'+sourceFingerprint[:10])
  stage=rootStage/'js';state['stage']=str(stage)
  classes=build/'classes';classes.mkdir(parents=True,exist_ok=True)
  src=INSTALL[0]
  existing=[f for f in (root/'src/main/java').rglob('*.java')
            if f.relative_to(root).as_posix()!=src]
  invoke([javac,'--release','21','-encoding','UTF-8','-d',classes,*existing,KIT/src],logs)
  state['checks'].append('Java 21: 3.2 + TopFlop source compilation')
  baselineTest=root/'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopArchiveSmokeTest.java'
  if not baselineTest.is_file():raise RuntimeError('Missing baseline 52-check test')
  invoke([javac,'--release','21','-encoding','UTF-8','-cp',classes,'-d',classes,
       baselineTest,KIT/INSTALL[1]],logs)
  output=invoke([java,'-ea','-cp',classes,
    'it.alterlega.recordsnext.app.topflop.TopFlopArchiveSmokeTest'],logs)
  if 'checks=52' not in output:raise RuntimeError('Base model 52-check suite incomplete')
  output=invoke([java,'-ea','-cp',classes,TEST],logs)
  if 'TOP_FLOP_PUBLIC_VIEWS_KEEPER_AND_DOUBLE_CONTEXT_TEST_OK' not in output:
   raise RuntimeError('Keeper and double-team smoke test missing')
  state['checks'].append('52 model checks; goalkeeper goal/assist/saves/penalty/duplicate fantasy test')
  reused=stage.is_dir()
  if stage.exists() and not reused:raise RuntimeError('Staging path not directory')
  state['reusedStage']=reused
  partial=rootStage/'js.partial'
  if not reused:
   if partial.exists():
    if not partial.is_dir():raise RuntimeError('Invalid incomplete stage directory')
    invoke([sys.executable,KIT/INSTALL[3],partial,'--database',db],logs)
    state['checks'].append('Recovered complete previous interrupted stage')
   else:
    rootStage.mkdir(parents=True,exist_ok=True)
    result=invoke([java,'-Xmx1800m','-cp',str(classes)+os.pathsep+str(jar),
        JAVA,db,partial,normalized,*seasons],logs)
    if 'TOP_FLOP_VIEWS_STAGE_OK' not in result:
     raise RuntimeError('Java public views export signature not found')
  validationFile=build/'validation.json'
  out=invoke([sys.executable,KIT/INSTALL[3],stage if reused else partial,
      '--database',db,'--json-output',validationFile],logs)
  if 'TOP_FLOP_PUBLIC_VIEWS_VALIDATED' not in out:raise RuntimeError('Mandatory validator failed')
  data=json.loads(validationFile.read_text(encoding='utf-8'))
  if data['seasons']!=len(seasons) or data['maxJsBytes']>950000:
   raise RuntimeError('Wrong number of seasons or oversize JS')
  state['validation']=data
  state['checks'].append('JS SHA, syntax, source totals, ties, source events, Youth exclusion, 1.5MB limit')
  node=shutil.which('node')
  if node:
   output=invoke([node,KIT/INSTALL[4],stage if reused else partial,KIT/INSTALL[2]],logs)
   if 'TOP_FLOP_PUBLIC_VIEWS_BROWSER_OK' not in output:raise RuntimeError('Node decoder browser test missing')
   state['checks'].append('Node JS decoder on full selected seasons')
  else:
   state['checks'].append('Node unavailable: Python mandatory JS validation completed')
  if sha(db)!=dbBefore:raise RuntimeError('Database changed despite read-only mode')
  state['databaseSha256After']=sha(db)
  state['checks'].append('SQLite SHA-256 unchanged; site publisher never invoked')
  if not reused:partial.rename(stage)
  for rel in INSTALL:
   dst=root/rel
   if dst.exists():continue
   dst.parent.mkdir(parents=True,exist_ok=True)
   with dst.open('xb') as stream:stream.write((KIT/rel).read_bytes())
   installed.append(rel)
  bible=root/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v25.md'
  existingBible=bible.is_file()
  try:invoke([sys.executable,root/'tools/Create-RecordsNext4WorkingCodeMd-v25.py',
       '--project-dir',root],logs)
  except Exception:
   for rel in reversed(installed):(root/rel).unlink(missing_ok=True)
   if not existingBible:bible.unlink(missing_ok=True)
   raise
  state['installed']=installed
  state['status']='OK'
  print('ESITO: OK')
  print('JS STAGING (NON PUBBLICATO):',stage)
  print('JS FILES:',data['jsFiles'],'TOTAL BYTES:',data['totalJsBytes'],
    'MAX SINGLE BYTES:',data['maxJsBytes'])
  print('STAGING REUSED:',reused,'BIBBIA: docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v25.md')
 except Exception as ex:
  state['error']=repr(ex);logs.append(traceback.format_exc())
  print('ESITO: ERRORE',file=sys.stderr);print(str(ex),file=sys.stderr)
 finally:
  with zipfile.ZipFile(report,'w',zipfile.ZIP_DEFLATED) as z:
   z.writestr('result.json',json.dumps(state,indent=2,ensure_ascii=False))
   z.writestr('execution.txt','\n'.join(logs))
   if (build/'validation.json').is_file():z.write(build/'validation.json','validation.json')
  shutil.rmtree(build,ignore_errors=True)
  print('ZIP DA CARICARE:',report)
 return 0 if state['status']=='OK' else 1
if __name__=='__main__':sys.exit(main())
~~~~

## `tools/Create-RecordsNext4WorkingCodeMd-v25.py`

SHA-256: `a779b8dee79de9b0e59164cd37f5cc81c59e12ebe36de9bab1838f20996795e2`

~~~~python
#!/usr/bin/env python3
"""Append-only complete source bible v25 with SHA-256, no overwrite on divergence."""
from __future__ import annotations
import argparse,hashlib
from pathlib import Path

PREVIOUS='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v24.md'
DEST='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v25.md'
FILES=[
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopPublicViewsStageExporter.java',
 'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopPublicViewsStageSmokeTest.java',
 'visualizzatori/js/fcmRecordsNextTopFlopViewsDecoder-v1.js',
 'tools/Validate-RecordsNext4TopFlopPublicViews-v1.py',
 'tools/Test-RecordsNext4TopFlopPublicViewsBrowser-v1.js',
 'tools/Invoke-RecordsNext4TopFlopPublicViews-v1.py',
 'tools/Create-RecordsNext4WorkingCodeMd-v25.py',
 'docs/SPECIFICA_VISTE_PUBBLICHE_TOP_FLOP_v1.md',
 'docs/RecordsNext4_CATALOGO_RECORD_E_VISTE_TOPFLOP_v1.md',
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v26.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v25.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v25.md',
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v27.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v26.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v26.md',
]
def sha(p:Path)->str:return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
 p=argparse.ArgumentParser();p.add_argument('--project-dir',required=True)
 root=Path(p.parse_args().project_dir).resolve()
 for f in [PREVIOUS,*FILES]:
  if not (root/f).is_file():raise RuntimeError('Missing required source: '+f)
 lines=['# RecordsNext 4.0 — Bibbia v25 — Viste pubbliche Top & Flop','',
   'Storico integrale FCA/FCM solo SQLite; HTML e JS pubblici contengono viste interessanti TF01–TF20, non lo storico di tutte le giornate.',
   'Questa versione e STAGING; non incorpora ancora RecordFamily, pipeline, publisher, GUI, CSS ne pubblica sui siti.',
   'Bibbia precedente: `'+PREVIOUS+'` SHA-256: `'+sha(root/PREVIOUS)+'`','']
 for f in FILES:
  dest=root/f;language={'.java':'java','.py':'python','.md':'markdown','.js':'javascript'}.get(dest.suffix,'text')
  lines.extend(['## `'+f+'`','', 'SHA-256: `'+sha(dest)+'`','',
                '~~~~'+language,dest.read_text(encoding='utf-8-sig').rstrip(),'~~~~',''])
 content='\n'.join(lines).rstrip()+'\n'
 target=root/DEST
 if target.exists():
  if target.read_text(encoding='utf-8')!=content:
   raise RuntimeError('Existing bible diverges, never overwrite: '+str(target))
  print('BIBBIA v25: GIA IDENTICA')
 else:
  with target.open('x',encoding='utf-8') as stream:stream.write(content)
  print('BIBBIA v25: OK',target)
if __name__=='__main__':main()
~~~~

## `docs/SPECIFICA_VISTE_PUBBLICHE_TOP_FLOP_v1.md`

SHA-256: `6c21479e326f936d303c923477c74020a39eaebb742a354ae221109238beadae`

~~~~markdown
# RecordsNext 4.0 — Specifica VISTE PUBBLICHE Top & Flop — v1

2026-11-06 — Stato: exporter Java e decoder **verificati in STAGING**; nessuna integrazione della 3.2 e nessuna pubblicazione autorizzata.

## Architettura

- L'archivio FCM/FCA completo rimane nel database SQLite locale. La nuova famiglia contiene ESCLUSIVAMENTE viste calcolate, non copie dei dati-giornata o dei tabellini per tutti i calciatori.
- Nel sito una facade leggera, un archivio di schede aggregate stagionali (**Careers**), aggregati per calciatore-fantasquadra-competizione (**Teams**), record con tutti i pari merito (**Records**) ed episodi selezionati (**Highlights**).
- Righe Careers e Teams in codifica posizionale con dizionari di nomi squadre, competizioni ed eventi, decoder JS reversibile `fcmRecordsNextTopFlopViewsDecoder-v1.js`. Nessuna nuova cartella sotto `js` del sito.
- Il dettaglio selezionato contiene stagioni, giornata Serie A, giornata della competizione se canonica, fantasquadra, schieramento, conteggiato, voto, fantavoto, bonus/malus ed eventuale URL dei report canonici 3.2. Le prestazioni REALI non fingono di avere un unico tabellino fantasy; possono mostrare zero o piu contesti FCM documentati.
- Nessuna fusione automatica di CodiceGazza tra anni: `playerKey` = stagione + ID locale FCA, codice storico disponibile come candidato. Ruolo 1 per portieri verificato nelle 21 stagioni del database campione.
- Gol su rigore sottoinsieme dei gol; gol subiti su rigore sottoinsieme dei gol subiti. Portieri ammessi anche fra marcatori e assistman.
- Classifica media voto TF05 e fantamedia TF06: aggregati completi e denominatori, **nessuna classifica definitiva** fino all'approvazione delle soglie minime. TF11–TF13 disabilitati finche non si documenta il possesso per giorno e il fantavoto di esclusi. TF14–TF15 disabilitati finche non si autorizzano baseline, N minimo e riconciliazione identitaria cross-season. TF16 confronti disponibili senza metrica unica arbitraria.
- TF01–TF04, TF07–TF10 e TF17–TF20 pubblicano record sostenuti dalle fonti; TF09 per evento negativo distinto. Pareggi di merito tutti conservati, senza top N arbitrario.
- Youth League esclusa anche dagli aggregati. Play Off–Play Out e Spareggio Serie B considerate CAMPIONATO. 100 contesti eccezionali 2022/23 restano con giornata competizione NULL se non canonica, conservati nelle somme.

## Esito misurato su 21 stagioni

Database test aggiornato al 2026/27, ultima prova con i contesti/link selezionati:

| Metadato | Risultato |
|---|---:|
| Stagioni | 21 |
| Prestazioni reali presenti nello SQLite di origine | 617.956 |
| Contesti fantasy ammessi nello SQLite | 554.425 |
| Contesti con apporto COUNTED | 238.921 |
| Schede statistiche stagionali | 16.262 |
| Riepiloghi fantasquadra/competizione | 45.787 |
| Gruppi di record | 540 |
| Episodi selezionati | 1.081 |
| File JS | 85 |
| **Byte JS complessivi** | **5.355.536** |
| **Singolo JS massimo** | **208.689 byte** |

Dimensione accumulata in base alle stagioni del sito (senza ancora pubblicare dal publisher 3.2): 2006/07 0,12 MB; 2010/11 0,66 MB; 2018/19 2,51 MB; 2025/26 5,08 MB; 2026/27 5,32 MB (solo shard stagionali, da aggiungere la facade con cutoff).

Il numero 5,36 MB è **per lo storico di 21 anni**, NON per stagione. I dati sono raggruppati per anno; al caricamento della pagina si possono leggere solo index/Records; Careers e Teams si caricano su richiesta.

## Test e limiti

- Compilazione Java 21 contro i sorgenti 3.2+TopFlop; smoke test sintetico per portiere goleador che para, segna, serve assist, subisce gol e compare in due fantasquadre. Prove negative: una vista oltre 1.500.000 byte viene bloccata e una riserva non entrata non viene mai etichettata come conteggiata.
- Parser Python indipendente di tutti i 85 JS, ricostruzione vettori/eventi e verifica dei conteggi origine SQLite, SHA-256, dimensioni e inclusione degli highlight citati.
- Node browser decoder su tutte le 21 stagioni, nessuna Youth League, nessun record TF11–TF15 attivato erroneamente.
- URL tabellino solo da `season_normalized_*.json` della 3.2, NULL se mancante; nessun URI ricostruito con ID tecnici.
- **FASE SUCCESSIVA NON REALIZZATA:** estensione controllata `RecordFamily`/catalogo/pipeline/manifest/publisher/GUI e HTML ReNewo; verifica multisito con cutoff e rollback. La facade dello staging contiene tutte le 21 stagioni e NON deve essere copiata nei siti storici senza trasformazione e validazione specifica del publisher.
- Il runner lascia intatti SQLite, JS della v1/v2, HTML, CSS, GUI, 3.2 e siti esistenti.
~~~~

## `docs/RecordsNext4_CATALOGO_RECORD_E_VISTE_TOPFLOP_v1.md`

SHA-256: `23c2736d29c3f763b5b57454006c301d01dc35b96942d5de25f39a0a7f842b3c`

~~~~markdown
# RecordsNext 4.0 — Catalogo Records Top & Flop e viste pubbliche — v1

Data: 2026-11-06  |  Stato: **CATALOGO FUNZIONALE APPROVATO; CONTRATTO DI CALCOLO, NON CODICE IMPLEMENTATO**

## 0. Decisione architetturale

- Conservare nel **SQLite locale** tutto lo storico originale FCM/FCA, fatti, evidenze, associazioni e aggregati; l'archivio e la sorgente dei nuovi record.
- Pubblicare sul sito esclusivamente **viste interessanti predefinite**, classifiche, aggregati di carriera e **dettagli degli episodi selezionati**, non un secondo archivio completo di ogni giornata/contesto.
- Riutilizzare catalogo, `RecordsNextPipeline`, manifest, shard stagionali nella cartella `js`, cutoff storico multisito, staging, controllo di integrita, rollback e skin della **3.2**. Niente directory condivisa ReNewo obbligatoria, niente pipeline parallela.
- Singolo JS UTF-8: obiettivo <= 1.000.000 byte; **limite invalicabile <= 1.500.000 byte** misurato sul file generato. Pubblicazione vietata se un file eccede o un controllo fallisce. Misurare anche il peso complessivo effettivo per ciascun sito/stagione; non pubblicare preventivamente lo staging v1 da 372 MB o v2 da 59,9 MB.
- Distinguere sempre **REALI** (una prestazione FCA per giocatore/stagione/giornata Serie A), **APPORTATI** (una prestazione per contesto fantasquadra/competizione/incontro comprovato come conteggiato), **SPRECATI** (esclusione certa di giocatore effettivamente tesserato in quella giornata; fantavoto disponibile da fonte verificata). Lo stesso atleta puo apportare a piu fantasquadre e campionato/coppe nella stessa giornata senza moltiplicare statistiche reali.
- Identita: `(stagione, GiocatoreA.ID)` per l'archivio stagionale; `CodiceGazza` e chiave candidata storica con collisioni da verificare, **mai fondere automaticamente** identita conflittuali. Ruolo storico stagionale, non solo quello attuale.
- **Portieri inclusi in tutte le viste applicabili**, anche marcatori e assistman; mai escludere gol segnati o assist dei portieri per la posizione.

## 1. Campi fonte FCA verificati (schema RAW 2026/27 e lettori Java)

`raw_2026_2027_fca_giocatorea.RUOLO = 1` identifica i portieri nello snapshot verificato (87 soggetti; 2, 3, 4 altri ruoli). Verificare la corrispondenza nelle stagioni precedenti, non fissare il valore 1 per ogni schema senza controllo.

`Punteggio.VOTO1` = voto reale fonte 1 (Gazzetta); `VOTO2`, `VOTO3` = altre fonti, **non sommarle**. `GiocaIn.GIORNATA` = giornata reale Serie A. Gli eventi FCA presenti nello schema 2026/27 comprendono:

| Codice sorgente | Significato operativo | Regola non negoziabile |
|---|---|---|
| `GOLFATTI1` | Gol segnati, fonte Gazzetta | Fonte 1 per i record standard; anche portieri. Non sommare `GOLFATTI1/2/3`. |
| `GOLFATTISURIGORE1` | Gol su rigore | **Sottoinsieme** di `GOLFATTI1`, NON gol aggiuntivi. Gol non su rigore = totale meno rigori, solo se campi coerenti. |
| `ASSIST` | Assist | Per tutti i ruoli, portieri inclusi. |
| `GOLSUBITI` | Gol subiti | Particolarmente per portieri; non dedurre da risultato FCM. |
| `GOLSUBITISURIGORE` | Gol subiti su rigore | **Sottoinsieme** di `GOLSUBITI`, non sommare a questi. |
| `RIGPAR` | Rigori parati | Record specifici portieri ma non sopprimere altri ruoli se la fonte segnala l'evento. |
| `RIGSBA` | Rigori sbagliati | Malus/evento per ogni ruolo. |
| `AUTOGOL1` | Autogol Gazzetta | Non sommare `AUTOGOL1/2/3`. |
| `AMM`, `ESP` | Ammonizioni, espulsioni | Conteggi distinti. |
| `GOLVITTORIA`, `GOLPAREGGIO`, `DEF1/2/3` | Altri dettagli originali | Conservare localmente; pubblicare come evento solo dopo verifica semantica del regolamento storico. |

Per i fantasy apportati usare il valore effettivo `FCM.Tabellino.TOT` **soltanto per slot conteggiati** e conservare distinto `FCM.Tabellino.VOTO`. Per i non schierati il fantavoto storico potrebbe non essere disponibile: **NULL / da verificare**, mai ricostruito con coefficienti universali. Eventi FCA dell'incontro fantasy sono attribuiti come *apportati* solo con evidenza `COUNTED`.

Un portiere con gol segnato accumula gol in TF07 e TF20; un portiere che subisce gol compare in TF18. **Il fantavoto non si ricalcola sommando o sottraendo questi eventi** senza le regole storiche; usare il TOT reale registrato.

## 2. Filtri e denominatori comuni

Asse temporale: stagione calcistica YYYY/YYYY, numero stagione AlterLega, carriera fino al cutoff del sito; nessun riferimento a stagioni future per siti precedenti. Asse fantasy: fantasquadra, singola competizione, famiglia `CAMPIONATO` o `COPPE`, tutte; risultati reali e fantasy separati. `Play Off - Play Out` e `Spareggio Serie B` = CAMPIONATO; Youth League esclusa dai dati pubblici (contatore di esclusione solo audit). Il caso 2022/23 di 100 righe con mapping canonico ESCLUSA va conservato come anomalia tracciata, non normalizzato inventando la giornata.

Sia `N_R` il numero di *giornate reali con voto FCA valido*; `N_A` il numero di *contesti fantasy con apporto COUNTED e valore valido*; `N_VA` il numero di apporto COUNTED con voto reale valido (distinto da `N_A` quando necessario). Presenze reali/non giocate e presenze con voto sono concetti differenti. `SV_R` = somma VOTO1 su N_R; `SV_A` = somma VOTO1 sui contesti N_VA; `SF_A` = somma TOT su N_A.

- `mediaReale = SV_R/N_R`, `mediaVotoApportato = SV_A/N_VA`, `fantamediaApportata = SF_A/N_A`, se il rispettivo N > 0; altrimenti `null`, non 0.
- Per Incompresi, `E` = esclusioni confermate con proprieta della rosa verificata su effettiva giornata e incontro giocato; `P_V` = esclusioni con VOTO1 valido, `P_F` = esclusioni con fantavoto attendibile; `S_V`, `S_F` le somme sui rispettivi casi. `mediaVotoSprecato = S_V/P_V`, `errorePesatoVoto = S_V/E`, `fantamediaSprecata = S_F/P_F`, `errorePesatoFantavoto = S_F/E`. Se tutti i voti e fantavoti sono disponibili, `P_V=P_F=P` come nel modello originario. Esporre sempre `E`, `P_V`, `P_F`; se `E=0` il pesato e null, se `E>0` e nessun voto valido il pesato matematico e 0 ma le medie restano null. **Se fantavoti mancanti, non assegnare zero a prestazioni note ma prive di fonti**: segnalare la copertura e non ammettere confronti fuorvianti.
- In lista con ordinamento su **medie** o **tassi** applicare soglie minime configurabili e mostrare comunque il numero osservazioni. Proposta tecnica NON ancora approvata: minimo 5 per media stagionale di campionato, 20 per carriera, adeguamento alla durata effettiva delle coppe e alle stagioni appena iniziate. Per **massimi/minimi di singola prestazione**, conteggi assoluti, gol o rigori parati non esistono soglie artificiali.
- Parimerito: conservare tutti i pari merito matematici; mai ordinare per nome come criterio di merito. Filtro portieri = ruolo della stagione FCA verificato; classifica generale valida per tutti i ruoli e, quando opportuno, tab ruolo separato.

## 3. Catalogo dei record individuali approvati

| ID | Titolo pubblico | Aggregazione e ordinamento | Evidenza e guardrail |
|---|---|---|---|
| TF01 | Miglior voto reale | MAX `VOTO1` per singola giornata FCA | `VoteState.VALID`, una sola giornata reale, link fantasy eventuale, non necessario. |
| TF02 | Peggior voto reale | MIN `VOTO1` valido per giornata FCA | Mai includere `SV` o `NULL` come zero. |
| TF03 | Miglior fantavoto apportato | MAX `TOT` tra contesti fantasy `COUNTED` | Match, team, competizione, doppia giornata e tabellino. |
| TF04 | Peggior fantavoto apportato | MIN `TOT` tra contesti `COUNTED` e TOT valido | RU/voto d'ufficio non attribuiti a giocatore reale. |
| TF05 | Migliori/peggiori medie voto reale | AVG `VOTO1` per stagione/carriera su N_R | Graduatorie TOP e FLOP distinte; N e soglia visibili. |
| TF06 | Migliori/peggiori fantamedie apportate | AVG `TOT` su N_A per fantasquadra, competizione e stagione/carriera | Non mischiare prestazioni non schierate o contesti paralleli; contesti distinti espliciti. |
| TF07 | Marcatori reali / gol apportati | SUM `GOLFATTI1` reale, oppure SUM stesso evento sui contesti COUNTED | DUE classifiche separate; rigori segnati sottoinsieme; portieri inclusi. |
| TF08 | Assist reali / apportati | SUM `ASSIST` su reali, oppure sui contesti COUNTED | DUE classifiche separate, portieri inclusi. |
| TF09 | Re degli eventi negativi | SUM per tipo (`AMM`, `ESP`, `AUTOGOL1`, `RIGSBA`, altri verificati) | Evitare un punteggio unico di malus senza coefficienti storici espliciti. |
| TF10 | Record di continuita/presenze | COUNT di giornate reali con voto o COUNT di contesti COUNTED | Etichette **distinte**; non chiamare presenze i semplici record GiocaIn. |
| TF11 | L'incompreso: media voto sprecato | DESC `S_V/P_V` su esclusioni certe | N e copertura. Non inferire titolarita da un solo snapshot tesserato. |
| TF12 | L'errore pesato sul voto | DESC `S_V/E` | La somma voto include voto intero, non `voto - 6`; esclusioni senza voto nel denominatore. |
| TF13 | Fantavoto sprecato | DESC `S_F/P_F` e `S_F/E` come due graduatorie | Fonti fantavoto per esclusi da verificare; se non complete, **vista non attivabile** come classifica definitiva. |
| TF14 | Stagione schillaciana | Differenza positiva fra media voto **della stagione** e baseline delle altre stagioni con voto dello stesso calciatore | `delta=meanSeason - meanOtherSeasons` con baseline pesata per giornate; almeno un'altra stagione comparabile; N e soglia; versionare il criterio. |
| TF15 | Stagione sbagliata | Differenza negativa rispetto alla baseline come TF14 | Non confondere una brutta stagione reale con una stagione poco sfruttata dalla singola fantasquadra; eventuale confronto apporto in sezione separata. |
| TF16 | Chi lo ha valorizzato meglio | Per stesso calciatore confronto fantasquadre: COUNT COUNTED, AVG voto e TOT, SUM gol/assist apportati | Evidenziare volume e media, non attribuire una qualita unica arbitraria; squadre e competizioni comparabili. |

`TF05/TF06` includono le versioni FLOP delle medie senza aggiungere ID duplicati. `TF09` pubblica graduatorie per tipo di evento, con opportuni filtri di ruolo e stagione. Gol subiti/rigori parati sono parte delle viste evento e dei record portieri dedicati.

## 4. Record dedicati ai portieri (estensione espressamente richiesta e accolta)

| ID | Titolo pubblico | Metrica | Condizioni |
|---|---|---|---|
| TF17 | Para-rigori | SUM `RIGPAR` per portiere e record MAX in singola giornata, sia reale sia apportato | Versioni distinte. Fantasquadra premiata solo se il portiere e stato effettivamente COUNTED. |
| TF18 | Gol subiti dai portieri | SUM `GOLSUBITI`, MAX in singola giornata e media per giornata reale valida / match fantasy conteggiato | In classifica comparare i **denominatori**; mai definire una metrica per 90 minuti senza minuti verificati. `GOLSUBITISURIGORE` e un sottoinsieme. |
| TF19 | Giornate senza gol subiti | COUNT giornate portiere con voto valido, presenza plausibile verificata e `GOLSUBITI=0` esplicito | Etichetta prudente "giornate con zero gol subiti registrati". **Non dichiarare clean sheet ufficiale** senza evidenza sulla partecipazione e sull'intera gara. |
| TF20 | Portieri goleador | SUM `GOLFATTI1`, rigori segnati subset e SUM `ASSIST`, con eventi singoli | Anche fantasquadre beneficiarie se COUNTED. Record validi pure in TF07/TF08 generali. |

Tutti i record generali TF01–TF16 rimangono aperti ai portieri. I record TF17–TF20 sono viste specializzate, non una famiglia/publisher aggiuntiva. Gol subiti, rigori sbagliati, ammonizioni, espulsioni e autogol possono apparire nei record di eventi di qualsiasi ruolo **se effettivamente presenti nella fonte**.

## 5. Le cinque viste HTML e il contratto dati minimo

**A. Top/Flop:** classifiche TF01–TF10, TF14–TF15, TF17–TF20; per ogni risultato pubblicare `recordId`, scope, posizione/pari merito, `playerKey`, `name`, `role`, `value`, `sampleN`/denominatore, unità, anno, giornate (se record di gara), competizione, fantasquadra se pertinente, `scorecardUrl` canonica quando presente, identificativo interno per join nascosto.

**B. Carriere:** ricerca nome/codice storico, ruolo per stagione, quadro reale annuale e cumulato (N, media voto, gol, assist, rigori parati, gol subiti ecc. per ruolo), quadro fantasy di contesti COUNTED (N, media, fantamedia, eventi apportati) per squadra/famiglia/competizione, progressione per anno, record personali. Niente elenco automatico di ogni giornata reale.

**C. Incompresi:** TF11–TF13 con quattro metriche separate e `E`, `P_V`, `P_F`, copertura, fantasquadra, stagione/competizione; esempi con incontro e tabellino solo per esclusioni certe; se storico di possesso/fantavoto non dimostrabile, evidenziare **non disponibile/da verificare** e non inventare graduatoria.

**D. Fantasquadre:** confronti TF16, ranking di contributi reali e fantasy per calciatore/team/competizione, differenza di utilizzo e prestazioni non sfruttate soltanto quando confermate. I contesti di coppa/campionato rimangono distinti.

**E. Eventi e scheda personale:** filtri per gol, gol su rigore, assist, rigori parati, gol subiti, gol subiti su rigore, autogol, ammonizioni, espulsioni, rigori sbagliati e dati storicamente disponibili; dettaglio limitato agli episodi classificati e alle prestazioni significative. La scheda linka il tabellino corretto per stagione; per la prestazione FCA priva di collegamento FCM non inventare URL.

Un risultato con record partita deve contenere **stagione, numero storico AlterLega, competizione e giornata DELLA COMPETIZIONE (se mappata), giornata REALE Serie A, fantasquadra, schieramento e `conteggiato`**, voto, fantavoto (se disponibili), bonus/malus separati e link al tabellino originale (se disponibile). Gli ID FCM tecnici non sono titoli pubblici.

## 6. Proiezione JS proposta, non copia del database

- `Index/Manifest` familiare: versioni contratto, stagioni incluse fino a cutoff, riferimenti agli shard delle viste, contatori/capacita, dizionari strettamente necessari.
- `Records` (shard piccoli): righe ranking dei soli TF attivati con metrica, contesto, N e riferimenti agli episodi significativi.
- `Careers` (shard piccoli): un profilo aggregato per giocatore/anno piu indici per carriera, fantasquadra, competizione/famiglia; no ripetizione delle 617.956 giornate e 554.425 contesti sorgente.
- `Highlights` (on demand): **solo** episodi che compaiono nelle classifiche o nei record/schede, deduplicati per chiave reale o fantasy, con bonus/malus e URL. Non esportare dettagli non interessanti.
- Ogni sito pubblica solo le **viste con cutoff** tramite meccanismi 3.2. Testare peso JS per ciascuna stagione/sito oltre al limite per file. Evitare nuove sottocartelle sotto `js` se la 3.2 non le prevede. Il numero massimo di record per vista e il livello di dettaglio sono configurazioni del catalogo versionate, non `TOP N` arbitrario nel browser.
- Nuove viste future = nuovo calcolo da SQLite; nessuna reimportazione obbligatoria perche non si e pubblicata ogni giornata in JS.

## 7. Criteri di accettazione / test prima dell'integrazione

1. Campo reale `VOTO1` valido, `SV` e `NULL` distinti; nessuna presenza fittizia, nessuna media con zero inventato.
2. Tre fantasquadre con stesso giocatore nella stessa giornata = un solo fatto reale e piu apports distinti; campionato e coppa differenziati anche per stesso team.
3. Portiere con `RIGPAR=1`, `GOLSUBITI=2` e `GOLSUBITISURIGORE=1`: rigori parati +1, gol subiti **2 e non 3**; distinguere sottocategoria.
4. Portiere con `GOLFATTI1=1`, `GOLFATTISURIGORE1=1`: gol **1 e non 2**; entra nelle classifiche TF07 e TF20 e, se COUNTED, nei gol apportati.
5. Portiere con assist: in TF08 e TF20, senza filtri preconcetti. Prova sintetica se il campione disponibile non contiene il caso.
6. Un giocatore senza prova di possesso non puo alimentare `E`; un fantavoto mancante non diventa `0`. Valutare casi di cessione/riacquisto e continuita fra giornate (Varela).
7. Youth League sempre assente da ogni JS, mentre Play Off e Spareggio sono considerati CAMPIONATO; conflitto 2022/23 gestito esplicitamente.
8. Record di partita con giornata e scorecard canonica del sito corretto; nessun `IDGIORNATA` mostrato al posto dell'ordinale.
9. Stesso identico record filtrato per stagione, ruolo, team, competizione e cutoff ricostruibile dalle tabelle SQLite; pari merito e N coerenti.
10. Misurare file e totale JS per sito, validare schema/facade/shard, manifest, rollback e non-regressione delle famiglie 3.2 prima di installare sui siti reali.

## 8. Questioni tecniche aperte (non fingere approvazione di coefficienti)

- Soglie numeriche minime per medie/indici: parametri configurabili, proposta preliminare al §2; da confermare con la distribuzione reale per campionati/coppe/stagioni in corso.
- Verifica delle esclusioni fuori dalla lista Formazione su cronologia tesseramento, sostituzioni e fantavoto verificabile degli esclusi; TF11–TF13 non vanno dichiarati definitivi finche incompleti.
- Verifica delle etichette originali e del significato dei campi in FCA delle stagioni precedenti; mantenere il mapping per anno/fonte e segnalare i campi assenti.
- Confronto di impiego e "stagione schillaciana/sbagliata" e solo comparabile su dati con campionamento sufficiente e baseline disponibile; il delta voto NON e un giudizio sulla squadra o sull'atleta.
- Numero preciso di JS, volume per sito e quota `Highlights` = da **misurare mediante l'implementazione**, non promessa prima dell'export.

## 9. Provenienza e stato

Contratto precedente: `RecordsNext4_TopFlop_ContrattoVistaERecord_v1.md`; decisioni v24 e specifica exporter compatto v2 (quest'ultimo e **staging di fatti integrali**, non il formato pubblico finale). Verifica diretta sul DB di test `recordsnext-2026_2027-verifica-v1_20261102_151419_388732.db`: tabella `raw_2026_2027_fca_punteggio` con i campi elencati, `raw_2026_2027_fca_giocatorea` con `RUOLO`, `raw_2026_2027_fcm_tabellino` con `TOT`/`VOTO`. Gli schemi storici e le regole regolamentari restano da convalidare prima di attivare i rispettivi record.

**Nessun sorgente Java, database, visualizzatore, CSS, GUI, publisher o sito e modificato dalla presente specifica.**
~~~~

## `docs/PIANO_RECORDSNEXT4_GIOCATORI_v26.md`

SHA-256: `99e8c4247ee52a0b2058154b8509ae4567895d68da10944c965d8e0f42b66490`

~~~~markdown
# Addendum v26 — RecordsNext 4.0


## 2026-11-06 — Catalogo Records Top & Flop approvato (v26)

Decisione: pubblicare viste/record interessanti e aggregati di carriera, NON un secondo archivio completo di prestazioni FCM/FCA. Vedere `CATALOGO_RECORD_E_VISTE_TOPFLOP_v1.md` (TF01–TF20 compresi portieri, denominatori e criteri di verifica). La nuova proiezione JS leggera va realizzata con publisher 3.2, dopo validazione dei casi aperti. Nessuna modifica Java in questa fase.

---

# Piano RecordsNext 4.0 v24 — JS Top & Flop, staging isolato

**Obiettivo operativo di questa revisione:** generazione JS nativa con budget rigoroso per ogni file, indice e dettagli caricabili su richiesta. Non è ancora attivata nella GUI, nella pipeline di pubblicazione né nel sito.

**Stato test:** Java compilato, 52 test preesistenti e prove dati reali in Linux. Esecuzione su Windows da verificare tramite ZIP diagnostico.

**Passaggi per il rilascio 4.0, in ordine:**
1. [x] Modello FCA/FCM, contribuzioni e giornate canoniche dalla 3.2.
2. [x] Exporter Java nativo in STAGING, 21 stagioni testate, file < 1.500.000 byte, indice per stagione e dettagli lazy; link solo dai report canonici.
3. [ ] Interfaccia Top/Flop/Incompresi, scheda giocatore e demo HTML ReNewo, utilizzando le quattro skin funzionanti.
4. [ ] Completamento periodo tesseramento, esclusioni certe, quattro indicatori sprecato, record Top/Flop definitivi (nessuna classificazione senza evidenza).
5. [ ] Registrazione Catalogo/ProcessingOptions/GUI, generazione JS nell'attuale flusso FULL/CONSOLIDATED, validazioni del publisher, manifest, cutoff multisito, rollback; NON bypassare le guardie della 3.2.
6. [ ] Test locali Windows dell'intera pipeline e dei visualizzatori; packaging, bibbia e release 4.0.0.

**Regola vincolante:** non pubblicare gli output di staging finché l'integrazione nel publisher 3.2 non sia completata e verificata. Dimensione ideale per singolo JS <= 1.000.000 byte; limite bloccante 1.500.000 byte fisici.

---

# RecordsNext 4.0 - Piano operativo v23

Data: 2026-11-06. Implementazione nativa dell'apporto registrato nei tabellini.

**Verificato nel mio ambiente**: compilazione Java 21 completa, regressione 52 test, analisi 2006/07 e 2026/27, CSV reale Lautaro per fantasquadra/competizione, evento `GOLFATTI1` senza triplicazione 1/2/3. **Da confermare** con runner su Windows.

**Già implementato in Top & Flop**: lettori FCA/FCM, archivio Java stagionale, giornata competizione canonica 3.2, nuovo riconoscimento dei contributi dai primi 11 slot con `TOTALE` numerico, riepilogo nativo per squadra/competizione/famiglia, dettaglio originale degli eventi.

**Passaggi per concludere senza deviazioni**: (1) completare disponibilità storica per giornata con le operazioni confermate e le esclusioni effettive; (2) calcolare le quattro metriche Incompresi; (3) creare l'exporter familiare nativo usando `RecordsNextPipeline`, `Records2026SitePublisher`, `SeasonFamilyShardPublisher` e risoluzione URL 3.2, con shard e manifest; (4) HTML completo, ricerca carriera, skin ReNewo, GUI; (5) test FULL/CONSOLIDATED e multisito, documentazione e release.

Non chiamare "pronto per il sito" un CSV di verifica: il publisher originale della 3.2 non è ancora stato modificato.

---

# Piano v20 — Demo Top & Flop e compatibilità storica


**Stato 2026-11-02.** Implementata una demo reale ma circoscritta a 2006/07 e 2026/27. Conservare pipeline e publisher 3.2; l'integrazione nativa non è ancora eseguita. Vedere `SPECIFICA_DEMO_TOP_FLOP_RECORDSNEXT4_v1.md`.

**Via alla conclusione:** disponibili HTML, CSS 3.2 con scelta skin, facade e shard campione; prossime tappe: stato tesseramento giornaliero, metriche, catalogo, exporter nativo, manifest/GUI, demo finale e deploy di test. Nessun rilascio 4.0 finché gli shard completi all-time non sono pubblicabili con la 3.2.

---

# RecordsNext 4.0 - Piano di continuita v19

# RecordsNext 4.0 - Piano v17: collegamento FCA JDBC al modello Top & Flop

Data 2026-11-01. Prima connessione JAVA di sola lettura ai raw FCA importati dalla 3.2; 21 stagioni verificate sul DB principale e sul sandbox, che differiscono nel 2026/27. Non attivare la famiglia ne modificare la pipeline 3.2.

**Fatto in questa fase**: modello `TopFlopFcaJdbcReader` con profilo giocatore, anagrafica, voti e eventi originali, un fatto per giornata FCA; test Java su due DB e contatori auto-calibrati. **Da fare**: collegamento FCM (partita, fantasquadra, competizione, scorecard canonica, uso titolare/panchinaro/escluso), validazione piena degli eventi, tesseramento temporale, storico e statistiche aggregate, catalogo/exporter/GUI/JS.

**Regola di sicurezza**: gli stessi test possono trovare 616018 giornate nel sandbox e 617310 nel principale; e' una differenza di snapshot, non motivo per riscrivere i database.


---

# RecordsNext 4.0 - Piano v16: implementazione Java Records Top & Flop

**Decisioni precedenti preservate.** Nuova tappa: modello canonico Java `app/topflop` con real day, roster snapshot, fantasy match, politiche di competizione e indicatori base. Test Java 21 e bibbia v15. Non attivare la famiglia `TOP_FLOP` finche pipeline/JS/GUI/manifest/shard non funzionano.

**Prossima fase:** adattatore Java di sola lettura ai raw FCA/FCM per l'archivio individuale completo (compresi voti, fantavoti, eventi e prove di utilizzo), con confronto sui 21 archivi; solo dopo: motore/archiviazione/catalogo/exporter/publication 3.2. Rimangono aperte la semantica delle esclusioni non in `Formazione`, il fantavoto dell'escluso, i codici storici riutilizzati, soglie e classifica definitiva.

---

# RecordsNext 4.0 - Piano operativo v15

# RecordsNext 4.0 - Piano operativo v14

# RecordsNext 4.0 - Continuita documentale PIANO_RECORDSNEXT4_GIOCATORI_v13

Versione additiva del 2026-11-01. La cronologia integrale precedente e preservata sotto.

# RecordsNext 4.0 - Piano operativo v11

Data: 2026-11-01. Base produttiva 3.2.0 immutata. Branch `feature/recordsnext4-players`; commit di riferimento `f9bf30323d9204d70e91e242bddfc72c2060ecf9`.

## Fasi completate e evidenze

- Fase 0: identita e fonti; eccezioni sui codici storici ancora aperte.
- Fase 1: estrattore FCA sperimentale; output Windows verificato, 7 test. Non integrato nel Java.
- Fase 2: estrattore FCM fantasy sperimentale; output Windows verificato, 8 test. Non integrato nel Java.
- Fase 3A: audit stati `Formazione`, output Windows verificato, 9 test.
- Fase 3B1: audit `Tabellino.LISTA` + `Formazione`, output Windows `RecordsNext4_Fase3B1_OUTPUT_v1_20261101_001744_607896.zip` verificato: 7 test, 21 stagioni, 22.265 tabellini, 552.025 righe `Formazione`, tre SHA-256 dei report corretti, sandbox invariato, bibbia v8 generata. Non equivale a prova definitiva di voto conteggiato.
- Fase 3B2: censimento osservazionale delle regole `Competizione` per stagione e competizione, legame `Incontro.IDGIRONE -> Girone.IDCOMPETIZIONE`, moduli ammessi e tabellini osservati. Strumento prototipo con 8 test locali; lo ZIP Windows deve ancora certificarne l'esecuzione sul PC.

## Passaggi successivi

- Fase 3B3: confrontare configurazioni di competizione, tabellini reali, RU, sostituzioni, eventuali voti speciali e regole per ruolo. Stabilire cosa significa realmente `conteggiato`, `disponibile`, `non sfruttato`, `recuperabile`; distinguere prove osservate, inferenze e casi non simulabili.
- Fase 3C: solo dopo approvazione della semantica: prototipo per metriche individuali fantasy (panchina non impiegata, fuori formazione, sostituzioni, eventuali controfattuali), test su casi reali verificati.
- Fase 4: integrare fonti canoniche in SQLite/Java, esportazione JS stagionale/sito, HTML e GUI. Tutte le componenti richieste fanno parte del traguardo 4.0.0.
- Fase 5: regressioni, documentazione, commit, release 4.0.0.

## Vincoli costanti

1. Prestazione FCA unica per giocatore storico, stagione, giornata: non duplicare se usata in competizioni fantasy diverse.
2. `GiocatoreA.ID` e annuale; `CodiceGazza` contiene il codice storico nella FCA. Le collisioni storiche non sono risolte automaticamente.
3. I codici speciali 0/-1 in `Tabellino.LISTA` non sono calciatori.
4. `ENTRATO`, `ENTRATORISERVA` e posizione nei primi undici slot non bastano, isolatamente, a certificare un voto conteggiato.
5. Il regolamento va ricavato per stagione e competizione, senza assumere tre sostituzioni ovunque. Un archivio FCM e uno snapshot delle impostazioni, non prova di eventuali variazioni avvenute durante la stagione.
6. Conservare integralmente le analisi RU personali della 3.2; non inserirle nei record pubblici senza decisione espressa.
7. Tabellini non collegati, incontri con squadra 0 e riposi: casistiche distinte, nessuna cancellazione.
8. Nessuna scrittura nel DB operativo/sandbox, nessuna modifica a Java, pipeline, siti, GUI, manifest o branch principale durante gli audit.
9. I file nuovi vanno in `D:\DEV_APPS\RecordsNext4.0`, i risultati in `D:\DEV_APPS\downloads`, con nomi versionati.
10. Ogni fase mantiene piano, registro decisioni, diario, specifiche, test e bibbia incrementale con codice integrale e SHA-256. Le versioni pregresse restano intatte.


## Aggiornamento dopo verifica Windows 3B2 e decisione sul record individuale

- 3B2 completata anche su Windows (`ESITO: OK`, 8/8 test): audit documentale delle regole. Non sono state apportate modifiche al database o al motore Java.
- Priorita della fase successiva: **voti non utilizzati dal singolo calciatore**. Niente simulazione del punteggio di fantasquadra e niente ottimizzazione delle sostituzioni.
- Dati minimi da raccogliere per un calciatore: codice storico, ID locale FCA, stagione, fantasquadra, partita e competizione fantasy, giornata reale, indicatore di effettivo utilizzo nei primi 11 del tabellino, presenza di voto reale valido, valore sorgente del voto, riferimento al tabellino.
- Metrica stabilita: somma dei soli voti reali validi nelle esclusioni / **tutte le esclusioni**; conservare separati i due conteggi, non dividerli fra loro.
- Implementazione successiva: un campione reale verificato a mano, poi test automatici sul campione, quindi calcolo storico. Registrare i casi ambigui anziche correggerli arbitrariamente.
- Il campo "valore massimo" menzionato dall'utente rimane **aperto** (nessun cap numerico assunto).
- Continuano a valere l'identita per `CodiceGazza` con verifica collisioni, l'ID FCA locale, l'assenza di duplicazioni delle prestazioni reali e la conservazione dei record precedenti.


## Nuovo assetto approvato per il record individuale (2026-11-01)

- Conservare i quattro indicatori con due numeratori distinti (voto reale valido, fantavoto regolamentare completo) e due denominatori distinti (esclusioni totali, esclusioni con voto valido).
- La Fase 3C si articola in: (a) campioni di esclusioni FCM/FCA con provenienza voto/fantavoto e bonus-malus; (b) audit di tesseramento e partecipazione effettiva per stabilire `E` e `P`; (c) calcolo su casi reali e confronto con tabellino, senza simulare cambi di formazione.
- La stessa prestazione FCA resta un solo fatto reale; un'esclusione e invece un fatto fantasy legato a stagione, fantasquadra, competizione e incontro. Non aggregare ciecamente incontri di competizioni parallele.
- Implementare prima unit test sintetici per i quattro indicatori (v2); i test sintetici NON dimostrano ancora la classificazione delle esclusioni negli archivi.
- Se `E > 0, P = 0`, gli indici pesati valgono zero; le medie sulle gare valide sono non definite. Se `E = 0`, tutte le metriche sono non definite.
- Le RU non sono calciatori e non devono generare un voto individuale sprecato; le regole di sostituzione per competizione restano variabili e indipendenti da questo calcolo.
- Nessun limite artificiale al voto o fantavoto; la voce 'massimo' resta proposta separata e non adottata come soglia.


## Nuova fase 3C0 - Modello archivio completo e audit contesti paralleli

- Definita specifica `SPECIFICA_ARCHIVIO_COMPLETO_GIOCATORE_v1.md` con livello reale, tesseramento e livello evento fantasy, valido anche per titolari.
- Documentate due famiglie, competizioni singole e sovrapposizioni nella stessa giornata; conservazione dei dati originali prima dei filtri.
- Audit `Audit-RecordsNext4Contesti-v1.py` READ-ONLY: report per competizione, copertura, giocatore-squadra-competizione, sovrapposizioni e caso 100843.
- **Prossimo sviluppo**: storicizzare il tesseramento e definire le assenze da Formazione in modo dimostrabile; quindi costruire il dataset sorgente completo e l'estrazione simmetrica voti/fantavoti per utilizzati ed esclusi.
- La verifica Windows del nuovo runner rimane pendente; non toccare Java/GUI/sito.

## Aggiornamento v14 - fase 3C1

- Validazione Windows della 3C0: ZIP `RecordsNext4_Contesti_OUTPUT_v1_20261101_004840_254541.zip`, stato OK, 10 test, 21 stagioni, 552025 righe, SHA256 sandbox uguale prima/dopo, bibbia v12 presente. Non ancora dataset canonico.
- Classificare Play Off - Play Out e Spareggio Serie B CAMPIONATO; escludere Youth League da ogni output record/scheda.
- Audit v2: verificare nuovamente le 21 stagioni, separare originali/ammessi/esclusi, non eliminare dati sorgente.
- Prossima implementazione: associare a ogni evento fantasy la partita FCM e la `scorecardUrl` canonica della stagione corretta, verificata sui file normalizzati 3.2. URL non verificati restano null.
- Solo successivamente generare il dataset completo dei giocatori in Java/SQLite e i record con prova del singolo episodio.


## Aggiornamento v15 - Records Top & Flop come famiglia nativa

**Decisione vincolante dell'utente (2026-11-01):** tutti gli audit dei giocatori delle fasi 0-3C1 sono lavori preparatori di una nuova famiglia nativa **Records Top & Flop**, integrata in RecordsNext 4.0. La pipeline 3.2.0 resta la base esclusiva da estendere. Non costruire sistemi di export, pubblicazione, configurazione, manifest o visualizzazione paralleli.

**Stato attuale:** 3C1 eseguita su Windows, ZIP `RecordsNext4_Contesti_OUTPUT_v2_20261101_005413_578565.zip` verificato: stato OK; 13 test; 21 stagioni; 552025 righe origine, 546775 ammesse, 5250 Youth League escluse; SHA256 database sandbox prima=dopo; bibbia v13 generata. Non e ancora implementata una famiglia Java Top & Flop.

**Ordine vincolato di implementazione**, rispettando la 3.2:
1. Definire fatti canonici: giocatore storico + prestazione FCA unica e contesti per fantasquadra, incontro, competizione; provenienza del voto e dei bonus/malus; link canonico tabellino. Produrre test sui casi 100843, competizioni parallele, Youth League e anomalie.
2. Implementare una sola volta in Java/SQLite i fatti riutilizzabili da scheda giocatore e records; mantenere gli audit Python come verificatori, non produttori operativi permanenti.
3. Inserire `TOP_FLOP` nel catalogo (`RecordFamily`, `CoreRecordCatalog`, dipendenze, selezione e configurazione), senza modificare valori preesistenti e mantenendo la lettura delle configurazioni 3.2.
4. Collegare il calcolo alla `RecordsNextPipeline` con logiche FULL/CONSOLIDATED e archivio storico separato per stagione; eseguire regressione su output invariati delle altre famiglie.
5. Collegare `TopFlopFamilyJsExporter` a `Records2026SitePublisher`, relativa validazione, manifest, `SeasonFamilyShardPublisher`, publisher multisito e link tabellini canonici. Nessun nuovo flusso di deploy.
6. GUI: aggiungere selezione famiglia e singoli records in `RecordsNext2Dashboard` / configurazione vigente. HTML sito: nuova sezione e schede individuali derivate dagli stessi dati senza duplicazione dei calcoli.
7. Eseguire test integrati: nessun impatto su famiglie preesistenti, non oltre 1.5 MiB per shard, siti storici corretti, FULL e CONSOLIDATED, JS valido, nessuna Youth League. Aggiornare bibbia e rilasciare come 4.0.0 soltanto dopo approvazione.

La classificazione CAMPIONATO/COPPE e distinta dalla classificazione **famiglie di record**: `TOP_FLOP` e una famiglia records; CAMPIONATO/COPPE sono filtri del dominio sportivo. Non confonderle nel codice.

**Prossimo passo di implementazione:** realizzazione nel Java 4.0 del modello dati canonico individuale e test di regressione, senza ancora selezionare/pubblicare la famiglia finche il suo calcolo non esiste.


## Aggiornamento v18 - lettore Java FCM JDBC

- Output Windows `RecordsNext4_FcaJdbc_OUTPUT_v1_20261101_205632_731016.zip` verificato, `stato=OK`, compilazione e 52 verifiche; letti tre campioni per database, hash DB invariati, bibbia v16.
- Additivo `TopFlopFcmJdbcReader`: contesti Formazione/Incontro/Girone/Competizione/Tabellino/FCA/Team e Tesserato snapshot, tutti in sola lettura; controlli integrita sui 21 anni.
- Non usare IDGIORNATA come giornata progressiva; non attribuire esclusioni da assenze; Youth League solo nel conteggio tecnico.
- Successivo: collegamento tra lettori FCA/FCM e archivio dei fatti `TopFlopArchive`, risoluzione URL tramite 3.2, voto/fantavoto e bonus/malus verificati, poi classificazione degli stati. Solo dopo famiglia record selezionabile e pubblicabile.
- Java/GUI/publisher/DB originari della 3.2 rimangono immutati.


## Fase 4A - Assemblatore Java FCA + FCM (2026-11-02)

- Da `TopFlopFcaJdbcReader.loadSeason` creare l'unico archivio `RealDay` per calciatore/giornata FCA; poi collegare `TopFlopFcmJdbcReader.loadSeason` tramite lo stesso `PlayerSeason` e `RealDayKey`, mantenendo i contesti fantasy per team/competizione/incontro senza duplicare i dati reali.
- Non sostituire gli ID FCA annuali con CodiceGazza durante le JOIN; CodiceGazza e' un riferimento storico da verificare in caso di collisioni.
- `Tesserato` puo' avere piu righe per lo stesso giocatore/team/stagione (es. venduto e successivamente riacquistato). Conservare **tutte** le righe con l'ID sorgente nella vista `LoadedSeason.rosterEvents`, oltre alla vista riepilogativa univoca di `TopFlopArchive`. Non datare il tesseramento per inferenza.
- Il `Tabellino.Tot` inizia a essere disponibile come valore numerico solo quando il token originale e' numerico (virgola o punto); `sv/ng`, null e testi speciali rimangono senza fantavoto numerico, con stringa originale intatta.
- `Formazione.POS=0` registra il titolare iniziale, ma gli undici slot del tabellino non costituiscono una certificazione indipendente di contribuzione regolamentare; nessun `EXCLUDED_CONFIRMED` prodotto in automatico.
- I link scorecard rimangono null finche non sono ricavati dal normalizzatore/publisher della 3.2, preservando la corretta stagione storica.
- Le Riserve d'Ufficio, i coefficienti bonus/malus e la validita temporale dei tesseramenti non vengono simulati. Youth League continua a essere esclusa.
- **Sviluppo non abilitato** nel catalogo record, pipeline FULL/CONSOLIDATED, GUI o publisher. Prima completare e verificare la costruzione canonica e l'utilizzo dati su un'intera stagione, poi integrare nella famiglia esistente.
- Test pilota: 2006/07 (riacquisti), 2023/24 (Youth), 2025/26 (Lautaro in squadre multiple), 2026/27 (stagione senza formazioni) e ulteriori stagioni tramite test selettivi. Controlli per entrambi i DB con conteggi relativi alla sorgente, non valori assoluti fissati per 2026/27.


## Precisazioni snapshot aggiornato 2026/27 (2026-11-02)

- Serie A: 9 giornate concluse; AlterLega: giornate REALI 3-9, confermato dall'utente. Nessuna partita fantasy attesa nelle prime due giornate.
- Integrato test dello snapshot RAW 3.2 con 28.956 righe FCA, 7.650 formazioni FCM, 813 tesseramenti.
- La cronologia dei movimenti `EntryInBilancio` e' fonte da conservare, ma non e' ancora una tabella di finestre di possesso dimostrate.
- Pacchetto fase 4A = solo assemblatore read-only e test; disabilitata famiglia nella GUI/publisher finche' mancano record e output nativi.


---

## Addendum correttivo demo v2 — 2 novembre 2026

Installazione della demo Top & Flop con skin originali riutilizzate. Prima prova Windows v1 arrestata preventivamente sul CSS neutral diverso. Runner v2: non altera i CSS delle quattro skin esistenti, installa quelli assenti, verifica gli hash; documenta v20 della bibbia. Demo ancora limitata a due stagioni e non pubblicabile.

## Aggiornamento v22 — Giornata competizione canonica (2026-11-06)

- Database aggiornato 2026/27: **559.675** righe Formazione complessive nelle 21 stagioni; le **552.025** precedenti si riferivano allo snapshot antecedente l’importazione di 7.650 formazioni 2026/27.

- **Implementato e testato su copia:** `TopFlopCanonicalRoundResolver` legge `rn_event.competition_round` e non `FCM.Incontro.IDGIORNATA` come numero di giornata; l'assemblatore conserva sia la descrizione originaria FCM sia l'ID sorgente tecnico in campi separati.
- `FantasyFact.fantasyRound` può essere `null` quando non esiste un incontro nella mappatura canonica della 3.2. Nessun numero inventato e nessun contesto FCM scartato per questo motivo.
- **Eccezione verificata:** stagione 2022/23, 50 formazioni in Spareggio Serie B e 50 in Vera finale di Coppa tra le Coppe: 3.2 le marca `ESCLUSA`; Top & Flop le conserva come fonte, con giornata canonica non disponibile. I 5.250 contesti Youth League rimangono esclusi.
- Test: compilazione Java 21 + 52 smoke checks + lettura di 21 stagioni `rn_event` + assemblaggio 2022/23 e 2026/27 aggiornati.
- **Ancora da completare:** motore record nativo, calcolo apporto effettivo e tesseramenti giornalieri, JS integrati nel publisher 3.2, HTML finale con scheda giocatore e skin Renewo, GUI, verifica multistagione di tutti gli output e rilascio 4.0.0. Nessuna pubblicazione automatica.


## Fase 5B — Proiezione JS compatta v2 (6 novembre 2026)

- Archivio SQLite della 3.2 resta l’unica fonte completa, con export JS leggero per il sito.
- Codifica array+dizionari per stagione e URL unici per incontro/squadra; target 950kB, massimo bloccante 1.500.000 byte.
- Verifica integrale: 21 stagioni, 106 JS, 59.898.532 byte, stesso numero di giocatori/giornate/contesti del v1.
- Decoder browser disponibile; test semantico v1/v2 esatto su 2 stagioni.
- In questa fase staging soltanto: catalogo/pipeline/publisher/gui 3.2 ancora da integrare e testare.
- Evitare copie persistenti inutili: stessa fingerprint di DB+report => riutilizzo staging v2; vecchio v1 da eliminare solo esplicitamente.
~~~~

## `docs/REGISTRO_DECISIONI_RECORDSNEXT4_v25.md`

SHA-256: `a3b386bb0de8100c922d46c5cbfbd69070f93d91e414f3821757b5b962060dfc`

~~~~markdown
# Addendum v25 — RecordsNext 4.0


## Decisioni 2026-11-06 — Catalogo selettivo e portieri (v25)

| ID | Stato | Decisione |
|---|---|---|
| D-182 | APPROVATA DALL'UTENTE | Siti = viste interessanti, record e carriere aggregate; archivio completo solo in SQLite locale. Nessuna copia integrale dei fatti nei JS pubblici. |
| D-183 | APPROVATA DALL'UTENTE | Catalogo TF01–TF16; definire fonte, formule, filtri e denominatori prima del publisher. |
| D-184 | APPROVATA DALL'UTENTE | Portieri partecipano ai record generali, anche gol segnati e assist. Eventi specifici `GOLSUBITI`, `GOLSUBITISURIGORE`, `RIGPAR` inclusi in TF17–TF20. |
| D-185 | VINCOLANTE | Non sommare sottoinsiemi `GOLFATTISURIGORE1` a `GOLFATTI1` o `GOLSUBITISURIGORE` a `GOLSUBITI`; distinguere le tre fonti gol. |
| D-186 | VINCOLANTE | Pubblicazione via 3.2, limite 1.500.000 byte per file e misura peso per sito; nessuna directory condivisa ReNewo obbligatoria. |
| D-187 | APERTA | Soglie minime di campione e valutazione `EXCLUDED_CONFIRMED`/fantavoti degli esclusi prima di attivare graduatorie definitive. |

---

# Registro decisioni RecordsNext 4.0 v23 — Export JS Top & Flop

| ID | Stato | Decisione / conseguenza |
|---|---|---|
| D-172 | APPROVATA | Ogni file JS pubblico di Top & Flop deve essere idealmente <=1 MB; **mai >1.500.000 byte reali**. Validazione preventiva e blocco dello staging se eccede. Nessun troncamento di righe. |
| D-173 | IMPLEMENTATA IN STAGING | Esportazione tramite `TopFlopStageJsExporter` nella stessa base Java 3.2: facade leggera, riepiloghi per stagione e shard di dettaglio per calciatore. Ogni riga di riepilogo indica direttamente il file di dettaglio da caricare. |
| D-174 | IMPLEMENTATA IN STAGING | Prestazioni reali una sola volta per `(season, FCA.ID, Serie A day)`; contesti fantasy distinti per competizione, incontro, fantasquadra. Nessuna fusione automatica per `CodiceGazza` in presenza di collisioni storiche. |
| D-175 | IMPLEMENTATA IN STAGING | Riepiloghi di apporto solo per slot conteggiati FCM già verificati. Somme gol su rigore incluse nei gol Gazzetta fonte 1: nessuna duplicazione negli aggregati. Esclusioni senza prova restano escluse dai record. |
| D-176 | IMPLEMENTATA IN STAGING | Link ai tabellini copiati SOLO dai `season_normalized_*.json` della 3.2, indicizzati per incontro e fantasquadra; nessun URL costruito dalla giornata/ID interno. Se non esistono report (es. RAW 2026/27) il link resta `null`. |
| D-177 | IMPLEMENTATA IN STAGING | Gli originali integrali rimangono nel SQLite e nei fatti Java; nella proiezione JS si eliminano solo zeri ripetitivi e i campi di origine non necessari alla visualizzazione, senza perdere eventi non nulli o voti. |
| D-178 | REGOLA DI PROTEZIONE | La 3.2 operativa, gli HTML/CSS e la GUI restano immutati. Non copiare gli shard nel target 3.2 senza aggiornare conteggio/prefissi/manifest/publisher/rollback/scope e test. |
| D-179 | APERTA | Le esclusioni dei calciatori non presenti in `Formazione` richiedono cronologia del tesseramento verificata per giornata; non attribuire prestazioni non sfruttate senza prova. |
| D-180 | APERTA | Integrazione familiare `TOP_FLOP` nel catalogo 3.2, generazione FULL/CONSOLIDATED, cutoff storico, visualizzatori HTML ReNewo e scheda calciatore. |
| D-181 | TEST SU COPIA COMPLETATO; WINDOWS DA CONFERMARE | Compilazione Java completa e verifica dei JS su database updated 2026/27 e 21 stagioni, bilancio conteggi ed hash SHA-256 database. |

---

# RecordsNext 4.0 - Registro decisioni v22 - Apporto registrato dal tabellino

Data: 2026-11-06. Revisione ADDITIVA alla v21, preservata integralmente sotto.

| ID | Stato | Decisione / verifica |
|---|---|---|
| D-165 | IMPLEMENTATA NEL NUOVO CODICE, PROVA WINDOWS PENDENTE | `Usage.COUNTED` per formazione presente nei primi 11 slot di `Tabellino.LISTA`, incontro `GIOCATO=1`, `Tabellino.TOT` numerico. È l'apporto *registrato dal tabellino FCM*, non una simulazione. I flag `ENTRATO` e `ENTRATORISERVA` restano separati. |
| D-166 | IMPLEMENTATA | Le prestazioni in panchina con totale numerico ma oltre i primi undici slot non entrano nei totali **apportati**. Le posizioni ambigue restano `UNKNOWN`, nessun azzeramento o attribuzione inventata. |
| D-167 | IMPLEMENTATA | Aggregazioni per `PlayerSeason`, fantasquadra, famiglia CAMPIONATO/COPPE, competizione, una volta per incontro fantasy. Prestazione reale unica nell'archivio FCA. Il codice storico non unisce automaticamente omonimie/collisioni. |
| D-168 | IMPLEMENTATA | Per la media voto reale usare `FCA.Punteggio.Voto1` e conservarne distinto il valore `FCM.Tabellino.Voto`. Per la fantamedia apportata usare `FCM.Tabellino.Tot` effettivamente registrato; nessun coefficiente inventato. |
| D-169 | IMPLEMENTATA | Eventi apportati sono quantità originali FCA associate SOLO a contesti `COUNTED`. Le tre fonti di `GOLFATTI1/2/3` non si sommano. Normalizzare per visualizzazione solo la fonte 1 (Gazzetta); `GOLFATTISURIGORE1` è una sotto-categoria dei gol, non una somma aggiuntiva. Conservare anche tutte le colonne grezze. |
| D-170 | APERTA / DA VERIFICARE IN LOCALE | Completamento esclusioni certe e periodi di tesseramento; nessun dato fuori `Formazione` trasformato automaticamente in esclusione. Le 4 formule dei voti sprecati restano separate dal contributo verificato. |
| D-171 | APERTA | Collegare questo modello al catalogo `TOP_FLOP`, all'exporter e agli shard flat della pipeline 3.2, con manifest, GUI e HTML ReNewo. Il CSV ora è solo verifica dei dati Java, NON il publisher finale. |

---

# Registro decisioni v19 — Demo integrativa


| ID | Stato | Decisione |
|---|---|---|
| D-158 | APPROVATA | Records Top & Flop è famiglia **nativa** dei records RecordsNext; stesso motore e stesso publisher 3.2, non secondo programma. |
| D-159 | APPROVATA | HTML demo con selezione skin e layout RecordsNext; profilo neutral ispirato a ReNewo, oltre alle altre skin 3.2. |
| D-160 | APPROVATA | Dati JS nella `js` del sito, con facade, shard flat per stagione, cutoff storico, rollback e link canonici; nessun URL inventato. |
| D-161 | APPROVATA | Varela: se fra cessione e reinserimento non passa una giornata di reale assenza dalla rosa, appartenenza continua. |
| D-162 | IMPLEMENTATA SOLO DEMO | Preview 2006/07 + 2026/27, nomi JS e limiti coerenti 3.2; nessuna abilitazione catalogo, GUI o publisher. |
| D-163 | APERTA | Strategie per shard di stagioni oltre 1536 KiB: non accettare troncamenti; usare estensione controllata publisher. |
| D-164 | APERTA | Calcolo esclusioni certe e loro quattro metriche: nessuna inferenza da sola mancanza in formazione o tesseramento snapshot. |

---

# RecordsNext 4.0 - Registro decisioni v18

# RecordsNext 4.0 - Registro decisioni v16

| ID | Stato | Decisione / evidenza |
|---|---|---|
| D-124 | IMPLEMENTATA E VERIFICATA | L'adattatore FCA Top & Flop usa JDBC SQLite e le **stesse tabelle raw importate dalla 3.2**, senza copie o nuove pipeline. |
| D-125 | VERIFICATA SU FONTI | `recordsnext.db`: 617310 GiocaIn e 16245 GiocatoreA stagionali; `recordsnext-sandbox.db`: 616018 e 16211. Differenza tutta nel 2026/27: usare test di consistenza dinamica. |
| D-126 | REGOLA TECNICA | Conservare nei fatti la distinzione `GiocatoreA.ID` locale vs `CodiceGazza` storico, senza attribuire identita canoniche nei casi dubbi. |
| D-127 | REGOLA TECNICA | Un record `GiocaIn` non prova da solo una presenza reale: interpretare `VOTO1>0` come voto valido, valori negativi come senza voto e zero/NULL come sconosciuti; preservare i valori originali. |
| D-128 | IMPLEMENTATA | Conservare integralmente campi GiocatoreA/GiocaIn/Punteggio nel campo diagnostico `sourceFields`, con marcatore esplicito per SQL NULL; nessuna interpretazione dei coefficienti bonus/malus prima della fonte FCM. |
| D-129 | APERTA | Completare la lettura di Statistiche/Storico/IncontroInA e gli eventi FCM; definire il modello temporale dei tesseramenti e la validita degli esclusi. |
| D-130 | REGOLA DI PROTEZIONE | La 3.2 operativa, il DB, la GUI, la selezione catalogo, i publisher e i siti non si modificano in questa fase. |


---

# RecordsNext 4.0 - Registro decisioni v15

Le decisioni D-001--D-112 sono preservate integralmente dalla v14. Nuove D-113--D-123:

| ID | Stato | Decisione / evidenza |
|---|---|---|
| D-113 | IMPLEMENTATA: MODELLO | Aggiungere la famiglia `app/topflop` al codice Java esistente, senza modificare le classi 3.2 ne attivare prematuramente `RecordFamily.TOP_FLOP`. |
| D-114 | APPROVATA/IMPLEMENTATA | Prestazione reale unica per stagione+ID FCA+giornata Serie A. Eventi fantasy separati per stagione+competizione FCM+incontro+fantasquadra+ID FCA. |
| D-115 | APPROVATA/IMPLEMENTATA | Conservare `CodiceGazza` storico e ID FCA stagionale distinti; non fondere automaticamente collisioni storiche. |
| D-116 | APPROVATA/IMPLEMENTATA | `Tesserato` e una fotografia della rosa, non dimostra tesseramento in una giornata non documentata. Nessuna esclusione inferita dal silenzio FCM. |
| D-117 | APPROVATA/IMPLEMENTATA | Youth League esclusa categoricamente; Play Off - Play Out e Spareggio Serie B = CAMPIONATO; 24 nomi ammessi codificati esplicitamente, altri nomi in revisione obbligatoria. |
| D-118 | APPROVATA/IMPLEMENTATA | Dati per titolari, conteggiati, panchinari e fuori lista; stato `EXCLUDED_CONFIRMED` utilizzabile solo dopo prova sorgente. |
| D-119 | APPROVATA/IMPLEMENTATA | Quattro metriche personali calcolate da voti/fantavoti integrali, `SV/P`, `SV/E`, `SF/P`, `SF/E`. Sulle fonti incomplete mantenere separati contatori voto/fantavoto, non inventare valori. |
| D-120 | APPROVATA/IMPLEMENTATA | Non sintetizzare mai URL tabellini: copiare dal normalizzato/link canonico 3.2; conservare ID incontro e giornata distinti. |
| D-121 | IMPLEMENTATA: TEST | Test Java standard con 52 assert e wrapper JUnit 5; compilazione completa codice Java 3.2+nuovo modulo verificata localmente con JDK 21, non suite Maven completa. |
| D-122 | REGOLA OPERATIVA | Il runner deve verificare JDK 21, git branch, file preesistenti e collisioni prima dell'installazione; output diagnostico sotto `D:\DEV_APPS\downloads`, mai `%TEMP%`; niente sovrascritture. |
| D-123 | APERTA | Realizzazione estrattore JDBC, identificazione esclusioni provate, ambito tesseramento, classifiche e soglie, integrazione piena RecordFamily/pipeline/exporter/GUI. Non segnare come implementati in v15. |

---

# RecordsNext 4.0 - Registro decisioni v14

# RecordsNext 4.0 - Registro decisioni v13

# RecordsNext 4.0 - Continuita documentale REGISTRO_DECISIONI_RECORDSNEXT4_v12

Versione additiva del 2026-11-01. La cronologia integrale precedente e preservata sotto.

# RecordsNext 4.0 - Registro decisioni v10

Data: 2026-11-01. Registro additivo: D-001--D-053 restano documentate nelle versioni v2--v8; i relativi stati non cambiano tacitamente.

| ID | Stato | Decisione / evidenza |
|---|---|---|
| D-054 | CONFERMATA: verifica Windows | ZIP 3B1 `RecordsNext4_Fase3B1_OUTPUT_v1_20261101_001744_607896.zip`: stato OK, sette test superati, 552.025 righe `Formazione`, 22.265 tabellini, tre SHA-256 dati verificati, SHA sandbox invariato. Bibbia v8 generata; il runner dichiara nessun commit/deploy. |
| D-055 | CONFERMATA: osservazione FCM | La catena per individuare il regolamento e `Incontro.IDGIRONE -> Girone.ID -> Girone.IDCOMPETIZIONE -> Competizione.ID` **della stessa stagione**. `Competizione.ID` non costituisce da solo identita storica. |
| D-056 | CONFERMATA: fonte | `Competizione` registra anche `NUMEROSOSTITUZIONI`, `NUMERORISUFFICIO`, voti d'ufficio, limiti panchina, ruoli sostituibili, regole speciali; `ModuloAmmesso` collega i moduli alla competizione. Nessun valore e convertito in punteggio recuperato. |
| D-057 | CONFERMATA: osservazione locale 3B2 | Sono presenti 180 righe `Competizione` su 21 stagioni. Nel 2019/20 tutte le 12 riportano `NUMEROSOSTITUZIONI=5` e `NUMERORISUFFICIO=3`; nel 2007/08 le tre riportano 3, 8, 11 sostituzioni e 0, 99, 99 RU. Valori non normalizzati arbitrariamente. |
| D-058 | CONFERMATA: separazione | Nel campione SQLite ci sono 168 tabellini senza incontro, 23 collegati a incontri con una squadra zero: non dichiararli automaticamente anomalie o riposi. I controlli si svolgono separatamente. |
| D-059 | APERTA | Interpretazione delle configurazioni elevate (8, 11, RU 99), possibili convenzioni per assenza di limite, rapporto con tabellini e modifiche al regolamento durante la stagione: da verificare sul modello FCM e su esempi reali. |
| D-060 | APERTA | Definizione rigorosa di prestazione conteggiata, prestazione disponibile in panchina e punti effettivamente recuperabili, per competizione, stagione e regole del ruolo. Niente calcoli prima dell'approvazione. |
| D-061 | IMPLEMENTATA: prototipo di sola lettura | Audit 3B2 con dati raw completi delle configurazioni, aggregati per competizione, moduli ammessi e verifiche integrita; 8 test locali superati. Certificazione Windows ancora da eseguire. |

Nota: non sono state prese nuove decisioni sui pesi dei record o sulle formule per i punti non sfruttati.


## Integrazione successiva a 3B2: metrica individuale (2026-11-01)

| ID | Stato | Decisione / motivazione |
|---|---|---|
| D-062 | CONFERMATA: verifica Windows 3B2 | ZIP `RecordsNext4_Fase3B2_OUTPUT_v1_20261101_002713_328984.zip`, stato OK; 8 test superati, 21 stagioni, 180 configurazioni, 13.072 incontri e 22.265 tabellini. SHA-256 del sandbox prima e dopo identico: `57923f4e75986c2b81533e4beef15b9133635258d7de1d36827a1bdbe576671b`. Bibbia v9 generata; nessun commit/deploy dichiarato. |
| D-063 | APPROVATA DALL'UTENTE: obiettivo | Il voto sprecato e un indicatore riferito a **uno specifico calciatore** lasciato fuori da una fantasquadra, non il vantaggio teorico che la fantasquadra avrebbe potuto ottenere sostituendo altri giocatori. Nessuna simulazione delle sostituzioni necessaria per questa metrica. |
| D-064 | APPROVATA DALL'UTENTE: formula | `mediaVotoSprecato = somma(voti validi del calciatore nelle occasioni in cui e rimasto fuori) / numero totale di partite in cui e stato lasciato fuori`. Le esclusioni senza prestazione utile contano nel denominatore e zero nel numeratore. Esempio: 23 esclusioni, 3 voti pari a 6: `18/23 = 0,782608...`. Non usare come media primaria `18/3 = 6`. |
| D-065 | CONFERMATA: RU e fonti 3.2 | La Riserva d'Ufficio e un voto regolamentare assegnato per un posto scoperto quando le sostituzioni ordinarie non bastano. Non e un calciatore ne un voto del calciatore lasciato fuori; non entra nella somma dei suoi voti sprecati. Preservare il sistema RU e gli strumenti personali esistenti. Fonti: `docs/ANALISI_PERSONALE_RU.md`, `docs/MODELLO_DATI_RECORDSNEXT2.md` e `tools/Report_RU_Storico_Completo_v8_4.ps1` (glossario). |
| D-066 | APPROVATA DALL'UTENTE: sostituzioni | Non esiste una regola generale di tre sostituzioni: si osservano anche 5 e, per recuperi a distanza di mesi e configurazioni eccezionali, anche 11. Mantenere il valore FCM della competizione senza normalizzarlo. Per la media individuale non simulare sostituzioni alternative. |
| D-067 | DA VERIFICARE PRIMA DEL CALCOLO | Stabilire con esempi reali il significato operativo di "lasciato fuori" (non nei primi 11 del tabellino finale, riserva non utilizzata, non schierato), il tipo di voto da sommare (voto Gazzetta base vs fantavoto totale), il trattamento di SV e voti speciali e il perimetro delle sole partite valide. Non inventare questi criteri. |
| D-068 | DA VERIFICARE PRIMA DEI RANKING | Evitare conteggi erronei di una stessa prestazione reale se ci sono piu competizioni fantasy nella stessa giornata. Conservare il dettaglio delle partite fantasy ed esplicitare la chiave di aggregazione del record personale. |
| D-069 | APERTA | L'utente ha menzionato un "valore massimo" senza definirne numericamente l'eventuale soglia o se si riferisca al massimo voto sprecato. Non fissare cap o limiti arbitrari. |
| D-070 | PROCEDURA | Comandi pronti per copia-incolla PowerShell 5.1, senza blocchi `else` separabili; file sempre versionati; ogni fase aggiorna piano, registro, diario, codice verificato e bibbia con SHA-256. |

### Nota di compatibilita

Le D-060 e D-061 riguardano anche un **possibile** studio dei punti recuperabili della fantasquadra. Per il nuovo record individuale quell'analisi non costituisce un prerequisito: questa decisione ne restringe il perimetro, senza cancellare la cronologia.


## Revisione metriche individuali (2026-11-01) - decisioni D-071--D-079

| ID | Stato | Decisione / motivazione |
|---|---|---|
| D-071 | APPROVATA DALL'UTENTE | Il voto di un calciatore non utilizzato e il **valore integrale del voto** (es. 7 resta 7), non il voto meno 6 e non il vantaggio potenziale della fantasquadra. |
| D-072 | APPROVATA DALL'UTENTE | Quattro indicatori distinti: media voto sprecato, errore pesato sul voto, fantamedia sprecata, errore pesato sul fantavoto. La formula v1 `S/E` rimane valida come **errore pesato**, ma non come unica misura principale. |
| D-073 | APPROVATA DALL'UTENTE | Con `E` esclusioni, `P` prestazioni valide, `SV` somma voti e `SF` somma fantavoti: `mediaVoto = SV/P`, `pesatoVoto = SV/E`, `mediaFantavoto = SF/P`, `pesatoFantavoto = SF/E`. Dove il denominatore e zero il risultato e non definito. |
| D-074 | APPROVATA DALL'UTENTE | Una gara in cui il calciatore non ha giocato/non ha voto non produce errore: somma zero al numeratore; se e una esclusione effettiva valida, rimane nel denominatore `E` del peso stagionale. |
| D-075 | APPROVATA DALL'UTENTE | Fantavoto comprensivo di eventi e relativi bonus/malus: gol normali, gol su rigore, assist, ammonizioni e altre categorie disponibili. Conservare separatamente voto, fantavoto ed eventi originali. Valori regolamentari da sorgenti reali, mai coefficienti unificati arbitrariamente. |
| D-076 | DERIVAZIONE MATEMATICA | `quotaConVoto=P/E`, `errorePesatoVoto=mediaVoto*quotaConVoto` e `errorePesatoFantavoto=fantamediaSprecata*quotaConVoto`, quando denominatori disponibili. Se `E>0, P=0`: indici pesati pari a zero, medie non definite. |
| D-077 | PROPOSTA, DA APPROVARE | Aggiungere massimo voto/fantavoto in singola esclusione, numero di esclusioni e quota con voto come indicatori di dettaglio, senza applicare alcun cap artificiale. |
| D-078 | APERTA - FCM/FCA | Definire con casi reali l'esclusione (panchinaro non usato, fuori lista, non schierato ma tesserato), la provenienza del fantavoto per giocatore lasciato fuori e la gestione di `SV`, voti d'ufficio e competizioni parallele. Non calcolare record reali prima della verifica. |
| D-079 | PROCEDURA / IMPLEMENTAZIONE | Aggiunto solo un test sintetico e la specifica v2. Nessuna modifica a database, Java, GUI, regolamenti, competizioni o siti. Bibbia incrementale v11 e SHA-256 dei nuovi file; vecchie versioni conservate. |

**Cronologia invariata:** D-064 `S/E` non e annullata: assume il nome corretto di **errore pesato stagionale** e si affianca alla nuova media `S/P` richiesta dall'utente.


## Decisioni D-080--D-090: archivio completo per giocatore e contesti paralleli

| ID | Stato | Decisione / evidenza |
|---|---|---|
| D-080 | APPROVATA DALL'UTENTE | Il prodotto deve conservare un set completo di dati per OGNI calciatore, con scheda individuale e record derivati dallo stesso archivio. Non estrarre soltanto le statistiche utili a record gia previsti. |
| D-081 | APPROVATA DALL'UTENTE | Un calciatore puo essere tesserato nello stesso anno da 2-3 fantasquadre diverse (serie distinte). Separare rigorosamente le osservazioni fantasy per squadra. |
| D-082 | APPROVATA DALL'UTENTE | Lo stesso calciatore nella stessa giornata puo essere usato in campionato ed escluso in coppa, e viceversa. Ogni incontro fantasy e un contesto indipendente; la prestazione reale e unica. |
| D-083 | APPROVATA DALL'UTENTE | Esattamente due famiglie per il filtro richiesto: CAMPIONATO e COPPE; conservare sempre anche la singola competizione e la sua identita stagionale. |
| D-084 | APPROVATA DALL'UTENTE | Dati individuali completi anche per titolari, giocatori conteggiati e subentrati, non soltanto per gli esclusi: voto, fantavoto, dettaglio bonus/malus, presenze, medie e contesti. |
| D-085 | CONFERMATA: FCM 2025/26 | `Tesserato` mostra il calciatore codice 100843 in tre squadre: 8 (River Pino), 49 (F.C. Pitbull), 77 (F.C. Boliverz). In giornata Serie A 4 River Pino lo ha in Serie B e Coppa Serie B (due incontri diversi). |
| D-086 | INFERENZA DA VERIFICARE | `Tesserato` e uno snapshot senza campi data di validita; non considerare automaticamente escluso il calciatore mancante da `Formazione` in ogni incontro di una squadra. |
| D-087 | PROPOSTA OPERATIVA | Mappa esplicita delle denominazioni FCM alle due famiglie, per stagione e competizione, invece di classificazione runtime con parole chiave. Per ora tre nomi senza assegnazione (Play Off - Play Out, Spareggio Serie B, Youth League), da approvare. |
| D-088 | REGOLA DI INTEGRITA | Chiave prestazione reale diversa dalla chiave evento fantasy; non moltiplicare gol/assist reali nelle aggregazioni all-time quando la stessa prestazione alimenta piu eventi fantasy. |
| D-089 | IMPLEMENTATA: AUDIT PROTOTIPO | Nuovo audit Fase 3C0 di sola lettura sui contesti. Verifica 552.025 righe Formazione, 21 stagioni, mappa delle competizioni, sovrapposizioni e caso reale codice 100843. Non crea ancora il dataset completo canonico. |
| D-090 | APERTA | Regole affidabili per esclusioni senza Formazione, coesistenza di piu partite della stessa famiglia nella giornata, disponibilita storica tesseramento, fantavoto del non schierato e trattamento Play Off/Spareggio/Youth League. |

## Decisioni D-091--D-098: classificazione completa e link ai tabellini

| ID | Stato | Decisione e verifica |
|---|---|---|
| D-091 | APPROVATA DALL'UTENTE | `Play Off - Play Out` appartiene alla famiglia CAMPIONATO. |
| D-092 | APPROVATA DALL'UTENTE | `Spareggio Serie B` appartiene alla famiglia CAMPIONATO. |
| D-093 | APPROVATA DALL'UTENTE | `Youth League` non deve essere considerata MAI nei record/statistiche/schede individuali. Nei soli audit fonti conservare l'evidenza della esclusione. |
| D-094 | APPROVATA DALL'UTENTE | I record individuali di singola partita devono contenere stagione, competizione, fantasquadra, ID incontro e link del tabellino di quella partita sul sito della stagione corrispondente. |
| D-095 | VERIFICATA NEL CODICE 3.2 | Esistono `urlTabellino`, `urlTabellinoLocale`, `urlTabellinoOnline`, `scorecardUrl`; la risoluzione della URL e gia implementata nel normalized exporter 3.2. Non fare concatenazioni inventate con la root del sito corrente. |
| D-096 | APERTA - LINK REALI | Prima della pubblicazione, verificare per ciascun sito/anno la rotta reale e la eventuale possibilita di puntare direttamente al singolo incontro. I link delle pagine di giornata non garantiscono un'ancora per il match. |
| D-097 | REGOLA DATI | L'ID incontro FCM e distinto dalla giornata Serie A e dalla competizione. Conservare tutti i campi per il record e per la scheda giocatore, anche in competizioni parallele. |
| D-098 | IMPLEMENTAZIONE AUDIT | L'audit v2 classifica Play Off/Spareggio, esclude Youth League dalle osservazioni utilizzabili e conserva contatori di esclusione. I riferimenti URL negli esempi rimangono null finche non risolti tramite 3.2. Nessun DB/Java/sito modificato. |


## Decisioni D-099--D-112: integrazione nativa Records Top & Flop

| ID | Stato | Decisione / evidenza |
|---|---|---|
| D-099 | APPROVATA DALL'UTENTE | La nuova famiglia records si chiama **Records Top & Flop**. E parte di RecordsNext 4.0, non un progetto o publisher distinto. |
| D-100 | APPROVATA DALL'UTENTE | Riutilizzare le logiche 3.2.0 FUNZIONANTI per elaborazione, archivio, configurazione, GUI, esportazione, manifest, link e pubblicazione multisito; la 3.2 produttiva rimane intatta. |
| D-101 | APPROVATA DALL'UTENTE | Tutte le statistiche individuali complete costituiscono la base riutilizzabile per schede e Records Top & Flop: calcolo dei record come aggregazione derivata, non raccolta dati autonoma. |
| D-102 | VERIFICATA SORGENTE 3.2 | Il catalogo record e in `RecordFamily`, `CoreRecordCatalog`; configurazione in `ProcessingConfigWriter` e `ProcessingConfigLoader`; `PipelinePreflight` e `DependencyInventory` gestiscono i prerequisiti. |
| D-103 | VERIFICATA SORGENTE 3.2 | La pipeline centrale e `RecordsNextPipeline` e il publisher JS e `Records2026SitePublisher`, con controllo esplicito del numero e dei prefissi dei JS prodotti. |
| D-104 | VERIFICATA SORGENTE 3.2 | `SeasonFamilyShardPublisher` divide per stagione, crea facade e shard JS flat nella cartella js con massimo predefinito 1536 KiB. Estendere la lista FAMILIES e i relativi test. |
| D-105 | VERIFICATA SORGENTE 3.2 | `SeasonNormalizedExporter` produce i riferimenti canonici al tabellino; `MatchesJsExporter` li espone tramite `scorecardUrl`. Nessun URL storico sintetizzato a mano. |
| D-106 | REGOLA IMPLEMENTATIVA | Il filtro sportivo CAMPIONATO/COPPE e separato dalla famiglia catalogo `TOP_FLOP`. Play Off - Play Out e Spareggio Serie B=CAMPIONATO; Youth League sempre esclusa dai dati pubblici. |
| D-107 | REGOLA IMPLEMENTATIVA | Stessa prestazione reale FCA non conteggiata piu volte nei record reali, anche se utilizzata in molte partite/competizioni fantasy; ogni match fantasy resta distinto nei record fantasy. |
| D-108 | REGOLA IMPLEMENTATIVA | Prevedere FULL e CONSOLIDATED, multisito, rollback e validazioni JS 3.2: nessuna scorciatoia per i nuovi dati e nessun deploy autonomo. |
| D-109 | REGOLA IMPLEMENTATIVA | Non attivare la nuova famiglia nel catalogo selezionabile o in pubblicazione finche archivio e exporter non sono funzionanti e testati; impedire successo apparente con output vuoti. |
| D-110 | VERIFICATA FASE 3C1 | ZIP Windows 3C1: `stato=OK`, 13 test, 552025 originali, 546775 ammessi, 5250 Youth esclusi, DB sandbox invariato, bibbia v13 generata. |
| D-111 | APERTA | Elenco definitivo e soglie dei singoli records Top & Flop, definizione formale delle esclusioni senza Formazione e validita storica dei tesseramenti. Non inventare classifiche o soglie. |
| D-112 | IMPLEMENTATA: DOCUMENTAZIONE | Questa fase aggiunge mappa integrazione e regole alla continuita documentale, senza modificare la pipeline Java, la GUI, gli output o il database. La famiglia non e ancora implementata. |


## Decisioni D-131--D-139 - Collegamento JDBC FCM v1

| ID | Stato | Decisione / verifica |
|---|---|---|
| D-131 | APPROVATA DALL'UTENTE | Il modulo e' la famiglia nativa Records Top & Flop, estesa dalla 3.2, non una pipeline separata. |
| D-132 | IMPLEMENTATA E VERIFICATA | Il lettore FCM usa le relazioni originali Formazione/Incontro/Girone/Competizione/Tabellino e ID FCA della medesima stagione. |
| D-133 | IMPLEMENTATA E VERIFICATA | Competizioni parallele e piu fantasquadre non sono aggregate: una riga per giocatore/incontro/team; 552025 contesti originari. |
| D-134 | APPROVATA DALL'UTENTE | Youth League esclusa dai dati utilizzabili per record/schede; 5250 righe sono comunque contate nell'audit. Play Off e Spareggio Serie B restano CAMPIONATO. |
| D-135 | REGOLA TECNICA | `IDGIORNATA` FCM resta chiave dell'archivio, distinta da `GIORNATADIA`; non dedurre un ordinale e non costruire URL arbitrari. |
| D-136 | REGOLA TECNICA | Il posto nei primi undici slot del Tabellino non certifica da solo la prestazione legalmente conteggiata; ENTRATO/ENTRATORISERVA restano campi fonte distinti. |
| D-137 | REGOLA TECNICA | Tesserato FCM e' snapshot, non archivio cronologico; non inventare esclusioni dove manca una riga Formazione. |
| D-138 | IMPLEMENTATA | Fonte JSON/JS pubblica non ancora generata: lettore Java FCM in streaming e test JDBC senza modifica a DB, pipeline o GUI. |
| D-139 | APERTA | Integrare nella stessa istanza `TopFlopArchive` FCA+FCM, provenienza del fantavoto per esclusi, regole voti speciali e URL tabellini con normalizzatore 3.2. |


## Decisioni D-140--D-147 - Assemblaggio nativo per stagione

| ID | Stato | Decisione / verifica |
|---|---|---|
| D-140 | IMPLEMENTATA IN FASE 4A | Assemblare fatti FCA e FCM nella stessa istanza `TopFlopArchive`, partendo da FCA e collegando ogni formazione mediante ID locale e giornata reali. |
| D-141 | VERIFICATA SU FONTE | `Tesserato` presenta righe ripetute per stessa fantasquadra/giocatore: vendite e successivi riacquisti possibili. Chiave distinta dell'evento: `Tesserato.ID` per stagione; non usare solo giocatore+squadra per conservare la cronologia sorgente. |
| D-142 | IMPLEMENTATA IN FASE 4A | `LoadedSeason.rosterEvents` conserva tutte le righe originali e i rispettivi ID; `TopFlopArchive.rosters` rimane vista sintetica di membership. Non inferire intervalli di appartenenza. |
| D-143 | IMPLEMENTATA IN FASE 4A | I token di voto/fantavoto dei tabellini FCM restano nei campi sorgente; convertire in BigDecimal solo i numerici con virgola/punto, senza convertire `sv/ng` in zero. |
| D-144 | REGOLA CONSERVATIVA | Gli slot del tabellino registrano evidenza ma non certificano utilizzo legalmente conteggiato: non produrre automaticamente `EXCLUDED_CONFIRMED` e non gonfiare i record. |
| D-145 | REGOLA CONSERVATIVA | `scorecardUrl` rimane null durante l'assemblaggio JDBC; risolverlo esclusivamente con le logiche normalizzate 3.2, al momento dell'integrazione nella pipeline. |
| D-146 | DA VERIFICARE | La provenienza della fantamedia per giocatori fuori da Formazione, l'esclusione certa durante un tesseramento storico e le regole RU/sostituzione sono ancora aperte. |
| D-147 | REGOLA DI REGRESSIONE | La nuova classe e i test non modificano pipeline, catalogo, GUI, sito o DB; testare gli hash e mantenere separati dati reali e contesti fantasy. |


## D-148--D-157 - Conferme 2026/27 e cronologia mercato

| ID | Stato | Regola o evidenza |
|---|---|---|
| D-148 | CONFERMATA DALL'UTENTE | 9 giornate reali di Serie A disponibili nello snapshot 2026/27. |
| D-149 | CONFERMATA DALL'UTENTE | La AlterLega 2026/27 ha cominciato dalla giornata reale 3; assenza di incontri nelle giornate 1 e 2 NON e' errore. |
| D-150 | IPOTESI DELL'UTENTE - DA VERIFICARE | La cronologia dei tesseramenti potrebbe essere legata a giornate della Serie A vera. Non assumere che `ULTIMAGIORNATA` sia la prima giornata eleggibile. |
| D-151 | FONTE VERIFICATA | `Tesserato` non contiene date di validita'. `EntryInBilancio` contiene `DATATRANSAZIONE` e `ULTIMAGIORNATA` e deve restare distinto dalla membership. |
| D-152 | CONFERMATA DALL'UTENTE | Calciatore ceduto e riacquistato in un'asta successiva e' operazione normale; riacquisti nella stessa sessione possono essere correzioni. Le sole date non provano la sessione. |
| D-153 | CONFERMATA DALL'UTENTE | Varela/FortisSIMY 2026/27 e' una correzione da svincolo accidentale; preservare tesseramenti 699 e 752 e relativi movimenti contabili. |
| D-154 | VERIFICATA: SNAPSHOT IMPORTATO 3.2 | 2026/27 aggiornato: 28956 righe reali FCA, 7650 `Formazione`, 813 `Tesserato`, 876 `EntryInBilancio`; 3..9 giornate reali con formazioni fantasy. |
| D-155 | REGOLA IMPLEMENTATIVA | Il nuovo assemblatore conserva tutte le righe `Tesserato` con ID sorgente e usa vista membership sintetica; nessuna esclusione senza prova di tesseramento in quel giorno. |
| D-156 | TEST AGGIUNTIVO | Verifica specifica sul DB 2026/27 aggiornato, compreso Varela e l'inizio della fantasy alla giornata 3. I dati di output test non sono regole eterne per altre stagioni. |
| D-157 | RIMANE APERTO | Non integrare automaticamente la datazione del possesso e il dettaglio `EntryInBilancio` in record pubblico fino a chiarimento dei campi originali FCM. |


---

## Addendum correttivo demo v2 — 2 novembre 2026

**D-158 (approvazione tecnica conservativa):** i quattro `release/visualizzatori/profiles/*/recordsnext.css` della 3.2, eventualmente modificati nel worktree utente, sono la fonte autoritativa di stile. La demo non li sovrascrive e deve utilizzarli dal loro percorso originale. Un CSS mancante puo essere installato dal kit come fallback. Ogni differenza viene registrata nel report; non richiede convergenza dell'hash con la copia del kit. Nessuna modifica al sito operativo. **Stato:** implementato nel runner v2, verifica Windows da completare.

## Decisioni e anomalie — 2026-11-06

| ID | Stato | Decisione / evidenza |
|---|---|---|
| D-158 | CONFERMATA DAL CODICE 3.2 | La giornata di competizione è `rn_event.competition_round`; `IDGIORNATA` è ID tecnico FCM e non deve essere mostrato come giornata. |
| D-159 | CONFERMATA | Conservare `FCM.Giornata.DESC` e `FCM.Incontro.IDGIORNATA` separatamente, senza normalizzare arbitrariamente etichette speciali. |
| D-160 | VINCOLANTE | Se `rn_event` non contiene una partita, il numero di giornata canonico resta nullo. Conservare la partita FCM, la descrizione e il motivo `UNMAPPED_SOURCE_ONLY`. |
| D-161 | ANOMALIA VERIFICATA, NON CORRETTA | Nel 2022/23 il mapping 3.2 marca `ESCLUSA` Spareggio Serie B (50 formazioni) e Vera finale di Coppa tra le Coppe (50): esiste un conflitto con il perimetro Top & Flop approvato. Non modificare il mapping 3.2 automaticamente. |
| D-162 | CONFERMATA | Le 5250 formazioni Youth League del 2023/24 rimangono escluse da Top & Flop, come richiesto dall'utente. |
| D-163 | STATO | Patch limitata al modello/assemblatore Java; non abilita la famiglia nel catalogo, pipeline, GUI, publisher o skin. |


## Decisioni D-164–D-171 — Ottimizzazione JS Top & Flop

| ID | Stato | Decisione |
|---|---|---|
| D-164 | APPROVATA UTENTE | Non replicare il database storico in output JS verbosi: SQLite FCM/FCA resta fonte completa, la pubblicazione contiene solo dati di consultazione. |
| D-165 | APPROVATA UTENTE | Prima ottimizzare senza perdita di informazioni (array compatti, dizionari, URL deduplicati); eventuali viste più specifiche soltanto dopo misura. |
| D-166 | TESTATA | v2 riduce l’export 21 stagioni a 59.898.532 byte / 106 file; massimo 949.971 byte per JS. |
| D-167 | VINCOLANTE | Ogni file JS UTF-8 non può superare 1.500.000 byte; target 950.000, errore prima di scrivere un giocatore troppo grande. |
| D-168 | VINCOLANTE | Nessun dato reale, contesto FCM, stato voto, evento bonus/malus o URL di tabellino può essere perso per compattare; decodifica semantica controllata contro v1. |
| D-169 | REGOLA | URL unici per matchId e teamId dentro la stagione, ottenuti solo dalla 3.2; dettagli e riepiloghi richiesti in modo differito dal browser. |
| D-170 | REGOLA | Staging v2 riutilizzato se gli hash delle fonti coincidono; non cancellare v1 o dati preesistenti senza autorizzazione. |
| D-171 | NON ANCORA FATTO | Catalogo nativo, GUI, publisher 3.2, cutoff e visualizzatori definitivi ancora da integrare: i JS v2 NON sono pubblicati. |
~~~~

## `docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v25.md`

SHA-256: `6e9fb216665cea7a6e8e579d7edbc3343f3208c9e394bbefffa2b8c4230702e1`

~~~~markdown
# Addendum v25 — RecordsNext 4.0


## 2026-11-06 — Specifica catalogo TF01–TF20

Verificati sui sorgenti Java TopFlop e sulla RAW FCA 2026/27 i campi dei portieri e gli eventi. Redatta specifica `CATALOGO_RECORD_E_VISTE_TOPFLOP_v1.md`, approvato il catalogo funzionale delle cinque viste e l'estensione ai portieri. Non e stato implementato ne testato un nuovo exporter di viste; v1/v2 di esportazione fatti restano in staging e non vanno pubblicati. Prossima fase: implementare proiezioni selettive, verificare dati, cutoff, JS e non-regressione 3.2.

---

# Diario RecordsNext 4.0 v23 — Esportazione JS staging

Data 2026-11-06. Da sorgenti Java v22 realmente installati e database 2026/27 aggiornato.

- Aggiunta lettura stagionale efficiente di tutti i fatti `TopFlopArchive` senza scansioni per giocatore O(N²).
- Creati `TopFlopStageJsExporter`, `TopFlopCanonicalScorecards` e wrapper parser `TopFlopExistingJson` che riusa il JSON della 3.2.
- Generati shard ordinati per giocatore. Facade per stagione, riepiloghi con collegamento `detailFile`, dettagli lazy e link canonici originali.
- Scrittura binaria UTF-8 e verifica dimensione fisica ogni file; budget target 950.000, hard 1.500.000 byte.
- Su due stagioni pilota 2006/07 e 2026/27: 1419 anagrafiche, 53922 giornate FCA, 13700 contesti fantasy, 17 file e scorecard 2006 dal normalizzato 3.2.
- Su 21 stagioni e database aggiornato: 16262 anagrafiche stagionali, 617956 righe reali, 554425 contesti FCM ammessi, 389 file (senza report canonici nel test integrale), massimo 949991 byte.
- L'esportazione integrale con i 156 report normalizzati 3.2 ha prodotto 430 JS, 546101 link canonici e un totale staging di circa 372 MB (limite sempre per singolo file, non sull'insieme). Il caricamento online non è stato attivato.
- Parser di verifica standard Python per integrità di tutti i JS: 1 riga riepilogo/dettaglio per giocatore, esattezza locator, assenza Youth League, identificativi univoci, conteggi cumulati, limite byte, URL http(s).
- Controllo JavaScript nativo in Node del caricamento selettivo per stagione+calciatore con corrispondenza dettagli riuscito.
- NON modificati catalogo, pipeline, GUI, publisher, manifest o sito. NON completate esclusioni certe, dettaglio HTML definitivo o link per 2026/27 senza normalizzato aggiornato.

---

# RecordsNext 4.0 - Diario v22

2026-11-06. Fase apporto FCM: in `TopFlopJdbcArchiveAssembler`, first-eleven+incontro giocato+Tot numerico => `COUNTED`; evidenza originale in `TopFlop.UsageEvidence`. Nuovi `TopFlopContributionSummary` e `TopFlopContributionReport`: medie e somme voto reale, voto tabellino, totale fantasy, conteggi per contesto, eventi originali e normalizzati solo fonte 1. Le esclusioni non sono dedotte.

Prova sulla copia SQLite aggiornata 2026/27: 3327 slot COUNTED, 2006/07: 2585. Report di Lautaro (100843) 2026/27: 9 combinazioni squadra/competizione, divise fra tre fantasquadre; nessun doppio conteggio nella carriera reale. Prima di applicare il kit in Windows verificheremo file e SHA-256 del v21.

Non modificate pipeline 3.2, publisher, schema DB, CSS, GUI e configurazioni dei siti.

---

# Diario sviluppo v19 — Demo HTML e JS


2026-11-02: consultata direttamente la struttura `release/visualizzatori` della 3.2 con i profili CSS; esaminati `SeasonFamilyShardPublisher`, `MatchesJsExporter`, normalizzazione URL e struttura SQLite aggiornata. Implementato **solo kit demo**: HTML Top & Flop, viewer JS, facade e due shard reali. Restano inalterati catalogue Java, manifest, pipeline, GUI, database e siti. Il generatore Python è uno strumento preview read-only e **NON** il publisher nativo. JS e hash verificati localmente, prova browser Windows ancora richiesta.

---

# RecordsNext 4.0 - Diario di sviluppo v18

# RecordsNext 4.0 - Diario v16: lettore FCA JDBC

2026-11-01: confrontati realmente i due file SQLite della 3.2 allegati. Rilevata differenza confinata al 2026/27, esplicitata in piano, registro, specifica e test. Implementato `TopFlopFcaJdbcReader` come nuova classe nel package Java esistente `app/topflop`. Prove Java 21 su 21 stagioni di ciascuno dei due DB, con controllo contro conteggi SQL indipendenti, e test filtrato codici 100843/12302. Source e database produttivi invariati. Validazione finale della installazione sul PC ancora da ricevere. Non vengono fabbricati `FantasyFact` o esclusioni.


---

# RecordsNext 4.0 - Diario implementazione v15

## Intervento: primo codice Java Top & Flop

- Riletti `RecordFamily`, `CoreRecordCatalog`, `SeasonNormalizedExporter`, `MatchesJsExporter`, specifica e mappa integrazione v14, sorgenti 3.2 contenuti nell'archivio dell'utente.
- Aggiunte classi nuove `TopFlopFacts`, `TopFlopArchive`, `TopFlopMetrics`, `TopFlopCompetitionPolicy` in `app/topflop`, senza riscrivere le classi precedenti.
- Aggiunti test autonomo `TopFlopArchiveSmokeTest` (52 verifiche) e wrapper `TopFlopArchiveJUnitTest` per la suite JUnit 5 esistente.
- Eseguita compilazione dei 106 sorgenti Java originali + quattro nuove classi col compilatore Java 21.0.12.1: OK. Eseguito smoke test Java: 52 verifiche OK. Maven/JUnit integrale non eseguito nell'ambiente di analisi; la verifica sul PC utente resta da completare.
- Predisposti runner conservativo, nuova bibbia incrementale v15 e ZIP diagnostico in `downloads`.
- La prova della seconda esecuzione ha inizialmente evidenziato compilazione duplicata dei quattro file gia installati; corretto il runner per escludere le classi del kit dall'elenco dei sorgenti preesistenti. Nuova prova integrale eseguita da zero con due passaggi consecutivi (vedi log di verifica).
- Nessuna modifica a database, GUI, publisher, archivio siti, configurazione o versione Maven. Nessun commit o deploy automatico.

---

# RecordsNext 4.0 - Diario implementazione v14

# RecordsNext 4.0 - Diario implementazione v13

# RecordsNext 4.0 - Continuita documentale DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v12

Versione additiva del 2026-11-01. La cronologia integrale precedente e preservata sotto.

# RecordsNext 4.0 - Diario implementazione v10

Data: 2026-11-01.

## Revisione dello ZIP Windows 3B1

- ZIP `RecordsNext4_Fase3B1_OUTPUT_v1_20261101_001744_607896.zip`: 14 voci; `MANIFEST_FASE3B1_v1.json` con `stato=OK`.
- Log `LOG_TEST_CONTRIBUZIONI_v1.txt`: sette test su Windows, tutti superati.
- SHA-256 dei file `combinazioni.csv`, `campioni.csv`, `sintesi.json` controllati confrontando gli hash dichiarati nel manifest con i byte nello ZIP.
- Invariato lo SHA del database sandbox: `57923f4e75986c2b81533e4beef15b9133635258d7de1d36827a1bdbe576671b`.
- 22.265 tabellini, 552.025 righe Formazione, 237.686 giocatori nei primi undici slot, 22.028 panchinari nei primi undici, 27.319 titolari iniziali fuori dai primi undici; nessuna attribuzione di punti persi.
- `CODICE_FUNZIONANTE_RECORDSNEXT4_v8.md` generato; nessun commit/deploy eseguito dal runner.

## Sviluppo Fase 3B2

- Riletto lo schema raw FCM originale della 3.2: `Competizione`, `Girone`, `Incontro`, `Tabellino`, `Modulo`, `ModuloAmmesso` e analisi RU personali.
- Implementato `Audit-RecordsNext4Regole-v1.py` in modalita SQLite `mode=ro` + `query_only=ON`.
- Il censimento e per coppia stagione/competizione, con mantenimento di tutte le colonne originali `Competizione` e dei moduli ammessi. La tabella di dettaglio non contiene dati personali delle fantasquadre.
- Osservati 21 archivi, 180 configurazioni, 13.072 incontri (compresi quelli programmati/riposi), 22.265 tabellini; 168 senza ID incontro valido e 23 collegati a incontri con una squadra 0.
- Test automatici `Test-RecordsNext4Regole-v1.py`: otto test sintetici e su DB reale, superati in ambiente di prova. Il database sintetico temporaneo viene generato sotto la cartella diagnostica `downloads`, mai in `%TEMP%`.
- Progettati runner versionato v1, specifica e verifica v1, piano v10, decisioni v9 e bibbia v9 con SHA-256 dei nuovi file.
- La verifica esecutiva del nuovo runner su PowerShell 5.1/Windows e ancora da effettuare; non presentarla come completata.

## Esclusioni

Nessuna modifica ai dati, alle impostazioni delle competizioni, ai 4 script RU personali, al Java, al sito o alla GUI. Nessun calcolo di voti recuperabili, nessun commit.


## Verifica dell'output Fase 3B2 sul PC e decisione operativa

- Esaminato ZIP Windows `RecordsNext4_Fase3B2_OUTPUT_v1_20261101_002713_328984.zip`.
- `MANIFEST_FASE3B2_v1.json`: `stato=OK`, branch `feature/recordsnext4-players`, base `f9bf30323d9204d70e91e242bddfc72c2060ecf9`; stessa impronta SHA-256 del sandbox prima/dopo.
- `LOG_TEST_REGOLE_v1.txt`: otto test superati. `LOG_AUDIT_REGOLE_v1.txt`: 21 stagioni, 180 competizioni, 13.072 incontri, 22.265 tabellini; 168 riferimenti senza incontro rimangono non classificati.
- Riletto `docs/ANALISI_PERSONALE_RU.md` e il glossario di `tools/Report_RU_Storico_Completo_v8_4.ps1`: RU = voto assegnato d'ufficio a copertura di una mancanza. Non associare una RU a un giocatore storico reale.
- L'utente ha approvato la metrica **individuale**, non il controfattuale di squadra: 23 occasioni fuori, 3 gare con voto 6 -> 18/23. Nessun calcolo effettivo sui giocatori e nessun cap implementati in questa revisione.
- Creato test unitario della **sola formula** su dati sintetici. Prima della successiva implementazione su SQLite occorre verificare su esempi reali quali righe costituiscano esclusioni e quale voto sia valido.
- Questa revisione installa soltanto documentazione, un test di formula, il generatore v10 e un runner conservativo. Non modifica Java, GUI, pipeline, archivi 3.2 o siti.


## Revisione della metrica su richiesta utente (2026-11-01)

- Esaminato lo ZIP di esecuzione Windows `RecordsNext4_DecisioniGiocatori_OUTPUT_v1_20261101_003202_168980.zip`: contiene bibbia v10, registro v10, test formula v1 e manifest `stato=OK`.
- Distinti il rendimento medio nelle sole esclusioni con voto (`S/P`) e la misura stagionale su tutte le esclusioni (`S/E`), per voto reale e fantavoto completo di bonus/malus.
- Creati `SPECIFICA_METRICA_VOTO_SPRECATO_GIOCATORE_v2.md` e `Test-RecordsNext4MetricheIndividuali-v2.py` per il contratto puramente aritmetico; test su dati sintetici, nessuna lettura FCM/FCA in questo intervento.
- Creati piano v12, registro decisioni v11, diario v11 e generatore bibbia v11. Conservate le versioni precedenti.
- Restano da verificare sui dati reali la definizione operativa di esclusione, voto valido, fantavoto disponibile per esclusi, bonus/malus storici e deduplicazione delle partite fantasy parallele.
- Nessuna implementazione di record nei sorgenti Java e nessun deploy; database invariato perche non viene aperto.


## Fase 3C0 - 2026-11-01

- Verificato lo ZIP Windows `RecordsNext4_Metriche_OUTPUT_v1_20261101_004055_490269.zip`: manifest OK, 8 test metriche superati, bibbia v11 prodotta; nessun deploy/commit dichiarato.
- Esaminato il database sandbox 3.2 in sola lettura: 21 stagioni, 552.025 righe Formazione, 180 righe Competizione con 25 nomi distinti; la stessa giornata puo contenere piu incontri fantasy per giocatore-squadra.
- Caso campione 2025/26, codice 100843: tre tesseramenti snapshot, presenza nello stesso giorno in campionato e coppa per River Pino.
- Il prototipo di audit Fase 3C0 verifica l'integrita delle join ed elenca le competizioni non assegnate, senza interpretare le esclusioni o sommare i voti.
- Stato: audit testato localmente, codice integrale nella bibbia incrementale v12; installazione e test Windows ancora da confermare.

## Fase 3C1 - revisione classificazione e riferimenti tabellino

- Ricevuto e verificato output 3C0 Windows: `ESITO OK`, 10/10 test, 552025 righe Formazione; SHA256 del sandbox invariato, bibbia v12. Le 159678 sovrapposizioni sono contesti fantasy distinti, non duplicazioni reali.
- L'utente ha classificato `Play Off - Play Out` e `Spareggio Serie B` come campionati e richiesto l'esclusione completa di `Youth League` da record/schede.
- Fonte Java 3.2 esaminata: `SeasonNormalizedExporter.java` espone `urlTabellino`, `urlTabellinoLocale`, `urlTabellinoOnline` e conserva `source_event_id`; `MatchesJsExporter.java` espone `matchId`, `competitionId`, `seasonId`, `scorecardUrl`.
- L'audit v2 mantiene ID incontro e giornata; aggiunge `idGiornataFcm` e `scorecardLinkStatus` negli esempi. URL effettivo assente nell'audit raw: resta null.
- Nessuna simulazione RU/sostituzioni, nessun calcolo degli esclusi mancanti da Formazione, nessuna modifica alla pipeline o al DB.


## Revisione 2026-11-01 - nuova famiglia Records Top & Flop

- L'utente ha chiarito che l'intero modulo giocatori e **una famiglia record del RecordsNext esistente**, con il nome pubblico **Records Top & Flop**.
- Rianalizzati gli effettivi sorgenti 3.2.0 forniti (`RecordsNext2.0.zip`), in particolare `RecordFamily`, `CoreRecordCatalog`, `ProcessingConfigWriter`, `ProcessingConfigLoader`, `RecordsNextPipeline`, `Records2026SitePublisher`, `SeasonFamilyShardPublisher`, `MatchesJsExporter`, `SeasonNormalizedExporter`, `RecordsNext2Dashboard`.
- `Records2026SitePublisher.validateGenerated` valida anche **il conteggio totale** dei file JS e i loro prefissi: richiede modifica coordinata prima di aggiungere un nuovo exporter. `SeasonFamilyShardPublisher.FAMILIES` e la lista esplicita delle famiglie sharded: richiede aggiunta con test flat.
- La 3.2 usa facade/shard per stagione nei js dei siti, con scope multisito e protezione rollback. Va **estesa**, non replicata con tool dedicati.
- Verificato output Windows Fase 3C1: 13/13 test, 21 stagioni, 552025 Formazione, 5250 Youth League escluse, 546775 ammesse, DB immutato.
- Stato codice: **nessun Java/SQLite/GUI/sito modificato in questa revisione**. Restano prototipi audit; prossimo sviluppo sul codice Java nel worktree 4.0. Documenti e script presenti nella bibbia incrementale v14.


## Sviluppo 2026-11-01 - Lettore FCM JDBC v1

- Verificato il report Windows FCA v16: esito OK, nessuna variazione hash database o codice 3.2.
- Ispezionato schema completo raw FCM 2006/07-2026/27 e joins originali; nessuna variazione delle colonne principali tra le 21 stagioni.
- Creato `TopFlopFcmJdbcReader.java` con scansione streaming delle Formazione, Tabellino, Incontro, Girone, Competizione, Fantasquadra, anagrafica FCA e GiocaIn. Separato il censimento Tesserato.
- Preservati testi originari del Tabellino e ID di giornata FCM; esclusi Youth League, inclusi Play Off e Spareggio nella corretta famiglia.
- Completati test integrali su entrambe le copie SQLite disponibili: 552025 Formazione, 546775 ammesse, 5250 Youth League, 14320 Tesserato, tutte le join e gli slot verificati.
- Preparati installer non distruttivo, verifica doppia esecuzione, regressione 52 test, controllo hash DB e bibbia incrementale v17.
- Nessuna modifica a 3.2, export, GUI o pubblicazione; verifica Windows sul PC reale in attesa di ZIP diagnostico.


## 2026-11-02 - Collegamento Java FCA/FCM fase 4A

- Verificato lo ZIP Windows `RecordsNext4_FcmJdbc_OUTPUT_v1_20261102_143437_776784.zip`: manifest `OK`, compilazione e regressioni 52/FCA/FCM superate, hash di entrambi i DB invariati, bibbia v17 generata.
- Rilevati e verificati tesseramenti con stessa coppia giocatore+fantasquadra ma ID `Tesserato` differenti; il modello sintetico precedente non era sufficiente a conservare gli eventi sorgente.
- Aggiunta la classe additiva `TopFlopJdbcArchiveAssembler.java`: costruisce `TopFlopArchive` stagionale dagli adapter esistenti e restituisce separatamente tutti gli eventi Tesserato.
- Test con JDBC SQLite in sola lettura, con riconciliazione di giorni reali, formazioni ammesse, Youth esclusa, tesseramenti sorgente e membership univoche; nessuna inferenza di esclusioni, URL, bonus o malus.
- Stato: test pilota locali superati. Il runner Windows produce ZIP diagnostico e bibbia v18; verifica finale Windows attesa.


## 2026-11-02 - Nuovi FCA/FCM e validazione stagione 2026/27

- Ricevuti FCM/FCA aggiornati e DB di prova importato con importatore RAW originale RecordsNext 3.2; validati con `integrity_check`.
- L'utente conferma: 9 giornate reali, AlterLega iniziata alla terza.
- Varela/FortisSIMY: due ID tesseramento 699/752 per svincolo accidentale e reinserimento; mantiene singola identita' reale.
- Audit su `EntryInBilancio` con date e `ULTIMAGIORNATA`, significato preciso ancora non certificato.
- Aggiunto test aggiornato `TopFlopJdbcArchive2026SmokeTest` al kit dell'assemblatore.
- Test Windows e installazione in worktree 4.0 da effettuare; nessuna modifica al database operativo 3.2.


---

## Addendum correttivo demo v2 — 2 novembre 2026

**2 novembre 2026:** il runner della demo v1 si e interrotto nel preflight, rilevando un CSS neutral esistente con hash diverso. Nessun file era stato copiato. Runner v2 sviluppato per riutilizzare CSS preesistenti senza sovrascriverli, mantenendo la verifica stringente per tutti gli altri file e la generazione di bibbia v19 e v20. Prova Linux ripetuta in worktree con CSS diversi; prova PowerShell/Windows ancora da confermare.

## 2026-11-06 — Patch canonica giornate Top & Flop v1

Esaminato lo ZIP `RecordsNext4_SORGENTI_ATTUALI_20261106.zip` (codice e documentazione correnti). Confrontate `CanonicalViews`, `TopFlopFcmJdbcReader`, `TopFlopFacts`, `TopFlopJdbcArchiveAssembler` e le viste SQLite 3.2 del database aggiornato. Rilevato uso errato di IDGIORNATA come fantasyRound; introdotto resolver canonico con ordinamento della 3.2 e fallback nullo per mapping esclusi. Nessun aggiornamento di RAW, mapping, database operativo, GUI, CSS o pubblicazione. Test su copia e installazione conservativa versionata. Prossimo step motore Top & Flop + JS nativi, usando gli altri publisher esistenti.

- Conteggi aggiornati: 559675 Formazione nell’SQLite con 2026/27 importato (contro 552025 nel precedente snapshot).


## 6 novembre 2026 — Exporter compatto JS Top & Flop v2

- Esaminato sorgente exporter v1 installato e copie aggiornate FCM/FCA/SQLite.
- Implementato exporter Java **additivo** `TopFlopCompactStageJsExporter` e decoder browser v2, senza sovrascrivere la v1 o modificare CSS, GUI e pipeline.
- Test Java di shard e blocco oversized; Python verifica limiti, cardinalità e ricostruzione semantica; Node.js testa decoder browser.
- Due stagioni campione 2006/07 e 2026/27: 1.419 calciatori, 53.922 giornate, 13.700 contesti: 12+ MB v1 -> 2.214.884 byte v2 e confronto semantico completo.
- 21 stagioni: 16.262 calciatori, 617.956 giornate, 554.425 contesti, 546.101 scorecard-link nei contesti, 106 JS per 59.898.532 byte; max 949.971.
- Anche le giornate senza voto restano esplicite e i dettagli non vengono caricati tutti in apertura.
- Nessun sito aggiornato; esecuzione Windows e pubblicazione multisito ancora da completare.
~~~~

## `docs/PIANO_RECORDSNEXT4_GIOCATORI_v27.md`

SHA-256: `110a1490dd430a08fcb02f91d6561f46f876a1d6647bc2be653c1aae186728bc`

~~~~markdown
# Addendum v26 — RecordsNext 4.0


## 2026-11-06 — Catalogo Records Top & Flop approvato (v26)

Decisione: pubblicare viste/record interessanti e aggregati di carriera, NON un secondo archivio completo di prestazioni FCM/FCA. Vedere `CATALOGO_RECORD_E_VISTE_TOPFLOP_v1.md` (TF01–TF20 compresi portieri, denominatori e criteri di verifica). La nuova proiezione JS leggera va realizzata con publisher 3.2, dopo validazione dei casi aperti. Nessuna modifica Java in questa fase.

---

# Piano RecordsNext 4.0 v24 — JS Top & Flop, staging isolato

**Obiettivo operativo di questa revisione:** generazione JS nativa con budget rigoroso per ogni file, indice e dettagli caricabili su richiesta. Non è ancora attivata nella GUI, nella pipeline di pubblicazione né nel sito.

**Stato test:** Java compilato, 52 test preesistenti e prove dati reali in Linux. Esecuzione su Windows da verificare tramite ZIP diagnostico.

**Passaggi per il rilascio 4.0, in ordine:**
1. [x] Modello FCA/FCM, contribuzioni e giornate canoniche dalla 3.2.
2. [x] Exporter Java nativo in STAGING, 21 stagioni testate, file < 1.500.000 byte, indice per stagione e dettagli lazy; link solo dai report canonici.
3. [ ] Interfaccia Top/Flop/Incompresi, scheda giocatore e demo HTML ReNewo, utilizzando le quattro skin funzionanti.
4. [ ] Completamento periodo tesseramento, esclusioni certe, quattro indicatori sprecato, record Top/Flop definitivi (nessuna classificazione senza evidenza).
5. [ ] Registrazione Catalogo/ProcessingOptions/GUI, generazione JS nell'attuale flusso FULL/CONSOLIDATED, validazioni del publisher, manifest, cutoff multisito, rollback; NON bypassare le guardie della 3.2.
6. [ ] Test locali Windows dell'intera pipeline e dei visualizzatori; packaging, bibbia e release 4.0.0.

**Regola vincolante:** non pubblicare gli output di staging finché l'integrazione nel publisher 3.2 non sia completata e verificata. Dimensione ideale per singolo JS <= 1.000.000 byte; limite bloccante 1.500.000 byte fisici.

---

# RecordsNext 4.0 - Piano operativo v23

Data: 2026-11-06. Implementazione nativa dell'apporto registrato nei tabellini.

**Verificato nel mio ambiente**: compilazione Java 21 completa, regressione 52 test, analisi 2006/07 e 2026/27, CSV reale Lautaro per fantasquadra/competizione, evento `GOLFATTI1` senza triplicazione 1/2/3. **Da confermare** con runner su Windows.

**Già implementato in Top & Flop**: lettori FCA/FCM, archivio Java stagionale, giornata competizione canonica 3.2, nuovo riconoscimento dei contributi dai primi 11 slot con `TOTALE` numerico, riepilogo nativo per squadra/competizione/famiglia, dettaglio originale degli eventi.

**Passaggi per concludere senza deviazioni**: (1) completare disponibilità storica per giornata con le operazioni confermate e le esclusioni effettive; (2) calcolare le quattro metriche Incompresi; (3) creare l'exporter familiare nativo usando `RecordsNextPipeline`, `Records2026SitePublisher`, `SeasonFamilyShardPublisher` e risoluzione URL 3.2, con shard e manifest; (4) HTML completo, ricerca carriera, skin ReNewo, GUI; (5) test FULL/CONSOLIDATED e multisito, documentazione e release.

Non chiamare "pronto per il sito" un CSV di verifica: il publisher originale della 3.2 non è ancora stato modificato.

---

# Piano v20 — Demo Top & Flop e compatibilità storica


**Stato 2026-11-02.** Implementata una demo reale ma circoscritta a 2006/07 e 2026/27. Conservare pipeline e publisher 3.2; l'integrazione nativa non è ancora eseguita. Vedere `SPECIFICA_DEMO_TOP_FLOP_RECORDSNEXT4_v1.md`.

**Via alla conclusione:** disponibili HTML, CSS 3.2 con scelta skin, facade e shard campione; prossime tappe: stato tesseramento giornaliero, metriche, catalogo, exporter nativo, manifest/GUI, demo finale e deploy di test. Nessun rilascio 4.0 finché gli shard completi all-time non sono pubblicabili con la 3.2.

---

# RecordsNext 4.0 - Piano di continuita v19

# RecordsNext 4.0 - Piano v17: collegamento FCA JDBC al modello Top & Flop

Data 2026-11-01. Prima connessione JAVA di sola lettura ai raw FCA importati dalla 3.2; 21 stagioni verificate sul DB principale e sul sandbox, che differiscono nel 2026/27. Non attivare la famiglia ne modificare la pipeline 3.2.

**Fatto in questa fase**: modello `TopFlopFcaJdbcReader` con profilo giocatore, anagrafica, voti e eventi originali, un fatto per giornata FCA; test Java su due DB e contatori auto-calibrati. **Da fare**: collegamento FCM (partita, fantasquadra, competizione, scorecard canonica, uso titolare/panchinaro/escluso), validazione piena degli eventi, tesseramento temporale, storico e statistiche aggregate, catalogo/exporter/GUI/JS.

**Regola di sicurezza**: gli stessi test possono trovare 616018 giornate nel sandbox e 617310 nel principale; e' una differenza di snapshot, non motivo per riscrivere i database.


---

# RecordsNext 4.0 - Piano v16: implementazione Java Records Top & Flop

**Decisioni precedenti preservate.** Nuova tappa: modello canonico Java `app/topflop` con real day, roster snapshot, fantasy match, politiche di competizione e indicatori base. Test Java 21 e bibbia v15. Non attivare la famiglia `TOP_FLOP` finche pipeline/JS/GUI/manifest/shard non funzionano.

**Prossima fase:** adattatore Java di sola lettura ai raw FCA/FCM per l'archivio individuale completo (compresi voti, fantavoti, eventi e prove di utilizzo), con confronto sui 21 archivi; solo dopo: motore/archiviazione/catalogo/exporter/publication 3.2. Rimangono aperte la semantica delle esclusioni non in `Formazione`, il fantavoto dell'escluso, i codici storici riutilizzati, soglie e classifica definitiva.

---

# RecordsNext 4.0 - Piano operativo v15

# RecordsNext 4.0 - Piano operativo v14

# RecordsNext 4.0 - Continuita documentale PIANO_RECORDSNEXT4_GIOCATORI_v13

Versione additiva del 2026-11-01. La cronologia integrale precedente e preservata sotto.

# RecordsNext 4.0 - Piano operativo v11

Data: 2026-11-01. Base produttiva 3.2.0 immutata. Branch `feature/recordsnext4-players`; commit di riferimento `f9bf30323d9204d70e91e242bddfc72c2060ecf9`.

## Fasi completate e evidenze

- Fase 0: identita e fonti; eccezioni sui codici storici ancora aperte.
- Fase 1: estrattore FCA sperimentale; output Windows verificato, 7 test. Non integrato nel Java.
- Fase 2: estrattore FCM fantasy sperimentale; output Windows verificato, 8 test. Non integrato nel Java.
- Fase 3A: audit stati `Formazione`, output Windows verificato, 9 test.
- Fase 3B1: audit `Tabellino.LISTA` + `Formazione`, output Windows `RecordsNext4_Fase3B1_OUTPUT_v1_20261101_001744_607896.zip` verificato: 7 test, 21 stagioni, 22.265 tabellini, 552.025 righe `Formazione`, tre SHA-256 dei report corretti, sandbox invariato, bibbia v8 generata. Non equivale a prova definitiva di voto conteggiato.
- Fase 3B2: censimento osservazionale delle regole `Competizione` per stagione e competizione, legame `Incontro.IDGIRONE -> Girone.IDCOMPETIZIONE`, moduli ammessi e tabellini osservati. Strumento prototipo con 8 test locali; lo ZIP Windows deve ancora certificarne l'esecuzione sul PC.

## Passaggi successivi

- Fase 3B3: confrontare configurazioni di competizione, tabellini reali, RU, sostituzioni, eventuali voti speciali e regole per ruolo. Stabilire cosa significa realmente `conteggiato`, `disponibile`, `non sfruttato`, `recuperabile`; distinguere prove osservate, inferenze e casi non simulabili.
- Fase 3C: solo dopo approvazione della semantica: prototipo per metriche individuali fantasy (panchina non impiegata, fuori formazione, sostituzioni, eventuali controfattuali), test su casi reali verificati.
- Fase 4: integrare fonti canoniche in SQLite/Java, esportazione JS stagionale/sito, HTML e GUI. Tutte le componenti richieste fanno parte del traguardo 4.0.0.
- Fase 5: regressioni, documentazione, commit, release 4.0.0.

## Vincoli costanti

1. Prestazione FCA unica per giocatore storico, stagione, giornata: non duplicare se usata in competizioni fantasy diverse.
2. `GiocatoreA.ID` e annuale; `CodiceGazza` contiene il codice storico nella FCA. Le collisioni storiche non sono risolte automaticamente.
3. I codici speciali 0/-1 in `Tabellino.LISTA` non sono calciatori.
4. `ENTRATO`, `ENTRATORISERVA` e posizione nei primi undici slot non bastano, isolatamente, a certificare un voto conteggiato.
5. Il regolamento va ricavato per stagione e competizione, senza assumere tre sostituzioni ovunque. Un archivio FCM e uno snapshot delle impostazioni, non prova di eventuali variazioni avvenute durante la stagione.
6. Conservare integralmente le analisi RU personali della 3.2; non inserirle nei record pubblici senza decisione espressa.
7. Tabellini non collegati, incontri con squadra 0 e riposi: casistiche distinte, nessuna cancellazione.
8. Nessuna scrittura nel DB operativo/sandbox, nessuna modifica a Java, pipeline, siti, GUI, manifest o branch principale durante gli audit.
9. I file nuovi vanno in `D:\DEV_APPS\RecordsNext4.0`, i risultati in `D:\DEV_APPS\downloads`, con nomi versionati.
10. Ogni fase mantiene piano, registro decisioni, diario, specifiche, test e bibbia incrementale con codice integrale e SHA-256. Le versioni pregresse restano intatte.


## Aggiornamento dopo verifica Windows 3B2 e decisione sul record individuale

- 3B2 completata anche su Windows (`ESITO: OK`, 8/8 test): audit documentale delle regole. Non sono state apportate modifiche al database o al motore Java.
- Priorita della fase successiva: **voti non utilizzati dal singolo calciatore**. Niente simulazione del punteggio di fantasquadra e niente ottimizzazione delle sostituzioni.
- Dati minimi da raccogliere per un calciatore: codice storico, ID locale FCA, stagione, fantasquadra, partita e competizione fantasy, giornata reale, indicatore di effettivo utilizzo nei primi 11 del tabellino, presenza di voto reale valido, valore sorgente del voto, riferimento al tabellino.
- Metrica stabilita: somma dei soli voti reali validi nelle esclusioni / **tutte le esclusioni**; conservare separati i due conteggi, non dividerli fra loro.
- Implementazione successiva: un campione reale verificato a mano, poi test automatici sul campione, quindi calcolo storico. Registrare i casi ambigui anziche correggerli arbitrariamente.
- Il campo "valore massimo" menzionato dall'utente rimane **aperto** (nessun cap numerico assunto).
- Continuano a valere l'identita per `CodiceGazza` con verifica collisioni, l'ID FCA locale, l'assenza di duplicazioni delle prestazioni reali e la conservazione dei record precedenti.


## Nuovo assetto approvato per il record individuale (2026-11-01)

- Conservare i quattro indicatori con due numeratori distinti (voto reale valido, fantavoto regolamentare completo) e due denominatori distinti (esclusioni totali, esclusioni con voto valido).
- La Fase 3C si articola in: (a) campioni di esclusioni FCM/FCA con provenienza voto/fantavoto e bonus-malus; (b) audit di tesseramento e partecipazione effettiva per stabilire `E` e `P`; (c) calcolo su casi reali e confronto con tabellino, senza simulare cambi di formazione.
- La stessa prestazione FCA resta un solo fatto reale; un'esclusione e invece un fatto fantasy legato a stagione, fantasquadra, competizione e incontro. Non aggregare ciecamente incontri di competizioni parallele.
- Implementare prima unit test sintetici per i quattro indicatori (v2); i test sintetici NON dimostrano ancora la classificazione delle esclusioni negli archivi.
- Se `E > 0, P = 0`, gli indici pesati valgono zero; le medie sulle gare valide sono non definite. Se `E = 0`, tutte le metriche sono non definite.
- Le RU non sono calciatori e non devono generare un voto individuale sprecato; le regole di sostituzione per competizione restano variabili e indipendenti da questo calcolo.
- Nessun limite artificiale al voto o fantavoto; la voce 'massimo' resta proposta separata e non adottata come soglia.


## Nuova fase 3C0 - Modello archivio completo e audit contesti paralleli

- Definita specifica `SPECIFICA_ARCHIVIO_COMPLETO_GIOCATORE_v1.md` con livello reale, tesseramento e livello evento fantasy, valido anche per titolari.
- Documentate due famiglie, competizioni singole e sovrapposizioni nella stessa giornata; conservazione dei dati originali prima dei filtri.
- Audit `Audit-RecordsNext4Contesti-v1.py` READ-ONLY: report per competizione, copertura, giocatore-squadra-competizione, sovrapposizioni e caso 100843.
- **Prossimo sviluppo**: storicizzare il tesseramento e definire le assenze da Formazione in modo dimostrabile; quindi costruire il dataset sorgente completo e l'estrazione simmetrica voti/fantavoti per utilizzati ed esclusi.
- La verifica Windows del nuovo runner rimane pendente; non toccare Java/GUI/sito.

## Aggiornamento v14 - fase 3C1

- Validazione Windows della 3C0: ZIP `RecordsNext4_Contesti_OUTPUT_v1_20261101_004840_254541.zip`, stato OK, 10 test, 21 stagioni, 552025 righe, SHA256 sandbox uguale prima/dopo, bibbia v12 presente. Non ancora dataset canonico.
- Classificare Play Off - Play Out e Spareggio Serie B CAMPIONATO; escludere Youth League da ogni output record/scheda.
- Audit v2: verificare nuovamente le 21 stagioni, separare originali/ammessi/esclusi, non eliminare dati sorgente.
- Prossima implementazione: associare a ogni evento fantasy la partita FCM e la `scorecardUrl` canonica della stagione corretta, verificata sui file normalizzati 3.2. URL non verificati restano null.
- Solo successivamente generare il dataset completo dei giocatori in Java/SQLite e i record con prova del singolo episodio.


## Aggiornamento v15 - Records Top & Flop come famiglia nativa

**Decisione vincolante dell'utente (2026-11-01):** tutti gli audit dei giocatori delle fasi 0-3C1 sono lavori preparatori di una nuova famiglia nativa **Records Top & Flop**, integrata in RecordsNext 4.0. La pipeline 3.2.0 resta la base esclusiva da estendere. Non costruire sistemi di export, pubblicazione, configurazione, manifest o visualizzazione paralleli.

**Stato attuale:** 3C1 eseguita su Windows, ZIP `RecordsNext4_Contesti_OUTPUT_v2_20261101_005413_578565.zip` verificato: stato OK; 13 test; 21 stagioni; 552025 righe origine, 546775 ammesse, 5250 Youth League escluse; SHA256 database sandbox prima=dopo; bibbia v13 generata. Non e ancora implementata una famiglia Java Top & Flop.

**Ordine vincolato di implementazione**, rispettando la 3.2:
1. Definire fatti canonici: giocatore storico + prestazione FCA unica e contesti per fantasquadra, incontro, competizione; provenienza del voto e dei bonus/malus; link canonico tabellino. Produrre test sui casi 100843, competizioni parallele, Youth League e anomalie.
2. Implementare una sola volta in Java/SQLite i fatti riutilizzabili da scheda giocatore e records; mantenere gli audit Python come verificatori, non produttori operativi permanenti.
3. Inserire `TOP_FLOP` nel catalogo (`RecordFamily`, `CoreRecordCatalog`, dipendenze, selezione e configurazione), senza modificare valori preesistenti e mantenendo la lettura delle configurazioni 3.2.
4. Collegare il calcolo alla `RecordsNextPipeline` con logiche FULL/CONSOLIDATED e archivio storico separato per stagione; eseguire regressione su output invariati delle altre famiglie.
5. Collegare `TopFlopFamilyJsExporter` a `Records2026SitePublisher`, relativa validazione, manifest, `SeasonFamilyShardPublisher`, publisher multisito e link tabellini canonici. Nessun nuovo flusso di deploy.
6. GUI: aggiungere selezione famiglia e singoli records in `RecordsNext2Dashboard` / configurazione vigente. HTML sito: nuova sezione e schede individuali derivate dagli stessi dati senza duplicazione dei calcoli.
7. Eseguire test integrati: nessun impatto su famiglie preesistenti, non oltre 1.5 MiB per shard, siti storici corretti, FULL e CONSOLIDATED, JS valido, nessuna Youth League. Aggiornare bibbia e rilasciare come 4.0.0 soltanto dopo approvazione.

La classificazione CAMPIONATO/COPPE e distinta dalla classificazione **famiglie di record**: `TOP_FLOP` e una famiglia records; CAMPIONATO/COPPE sono filtri del dominio sportivo. Non confonderle nel codice.

**Prossimo passo di implementazione:** realizzazione nel Java 4.0 del modello dati canonico individuale e test di regressione, senza ancora selezionare/pubblicare la famiglia finche il suo calcolo non esiste.


## Aggiornamento v18 - lettore Java FCM JDBC

- Output Windows `RecordsNext4_FcaJdbc_OUTPUT_v1_20261101_205632_731016.zip` verificato, `stato=OK`, compilazione e 52 verifiche; letti tre campioni per database, hash DB invariati, bibbia v16.
- Additivo `TopFlopFcmJdbcReader`: contesti Formazione/Incontro/Girone/Competizione/Tabellino/FCA/Team e Tesserato snapshot, tutti in sola lettura; controlli integrita sui 21 anni.
- Non usare IDGIORNATA come giornata progressiva; non attribuire esclusioni da assenze; Youth League solo nel conteggio tecnico.
- Successivo: collegamento tra lettori FCA/FCM e archivio dei fatti `TopFlopArchive`, risoluzione URL tramite 3.2, voto/fantavoto e bonus/malus verificati, poi classificazione degli stati. Solo dopo famiglia record selezionabile e pubblicabile.
- Java/GUI/publisher/DB originari della 3.2 rimangono immutati.


## Fase 4A - Assemblatore Java FCA + FCM (2026-11-02)

- Da `TopFlopFcaJdbcReader.loadSeason` creare l'unico archivio `RealDay` per calciatore/giornata FCA; poi collegare `TopFlopFcmJdbcReader.loadSeason` tramite lo stesso `PlayerSeason` e `RealDayKey`, mantenendo i contesti fantasy per team/competizione/incontro senza duplicare i dati reali.
- Non sostituire gli ID FCA annuali con CodiceGazza durante le JOIN; CodiceGazza e' un riferimento storico da verificare in caso di collisioni.
- `Tesserato` puo' avere piu righe per lo stesso giocatore/team/stagione (es. venduto e successivamente riacquistato). Conservare **tutte** le righe con l'ID sorgente nella vista `LoadedSeason.rosterEvents`, oltre alla vista riepilogativa univoca di `TopFlopArchive`. Non datare il tesseramento per inferenza.
- Il `Tabellino.Tot` inizia a essere disponibile come valore numerico solo quando il token originale e' numerico (virgola o punto); `sv/ng`, null e testi speciali rimangono senza fantavoto numerico, con stringa originale intatta.
- `Formazione.POS=0` registra il titolare iniziale, ma gli undici slot del tabellino non costituiscono una certificazione indipendente di contribuzione regolamentare; nessun `EXCLUDED_CONFIRMED` prodotto in automatico.
- I link scorecard rimangono null finche non sono ricavati dal normalizzatore/publisher della 3.2, preservando la corretta stagione storica.
- Le Riserve d'Ufficio, i coefficienti bonus/malus e la validita temporale dei tesseramenti non vengono simulati. Youth League continua a essere esclusa.
- **Sviluppo non abilitato** nel catalogo record, pipeline FULL/CONSOLIDATED, GUI o publisher. Prima completare e verificare la costruzione canonica e l'utilizzo dati su un'intera stagione, poi integrare nella famiglia esistente.
- Test pilota: 2006/07 (riacquisti), 2023/24 (Youth), 2025/26 (Lautaro in squadre multiple), 2026/27 (stagione senza formazioni) e ulteriori stagioni tramite test selettivi. Controlli per entrambi i DB con conteggi relativi alla sorgente, non valori assoluti fissati per 2026/27.


## Precisazioni snapshot aggiornato 2026/27 (2026-11-02)

- Serie A: 9 giornate concluse; AlterLega: giornate REALI 3-9, confermato dall'utente. Nessuna partita fantasy attesa nelle prime due giornate.
- Integrato test dello snapshot RAW 3.2 con 28.956 righe FCA, 7.650 formazioni FCM, 813 tesseramenti.
- La cronologia dei movimenti `EntryInBilancio` e' fonte da conservare, ma non e' ancora una tabella di finestre di possesso dimostrate.
- Pacchetto fase 4A = solo assemblatore read-only e test; disabilitata famiglia nella GUI/publisher finche' mancano record e output nativi.


---

## Addendum correttivo demo v2 — 2 novembre 2026

Installazione della demo Top & Flop con skin originali riutilizzate. Prima prova Windows v1 arrestata preventivamente sul CSS neutral diverso. Runner v2: non altera i CSS delle quattro skin esistenti, installa quelli assenti, verifica gli hash; documenta v20 della bibbia. Demo ancora limitata a due stagioni e non pubblicabile.

## Aggiornamento v22 — Giornata competizione canonica (2026-11-06)

- Database aggiornato 2026/27: **559.675** righe Formazione complessive nelle 21 stagioni; le **552.025** precedenti si riferivano allo snapshot antecedente l’importazione di 7.650 formazioni 2026/27.

- **Implementato e testato su copia:** `TopFlopCanonicalRoundResolver` legge `rn_event.competition_round` e non `FCM.Incontro.IDGIORNATA` come numero di giornata; l'assemblatore conserva sia la descrizione originaria FCM sia l'ID sorgente tecnico in campi separati.
- `FantasyFact.fantasyRound` può essere `null` quando non esiste un incontro nella mappatura canonica della 3.2. Nessun numero inventato e nessun contesto FCM scartato per questo motivo.
- **Eccezione verificata:** stagione 2022/23, 50 formazioni in Spareggio Serie B e 50 in Vera finale di Coppa tra le Coppe: 3.2 le marca `ESCLUSA`; Top & Flop le conserva come fonte, con giornata canonica non disponibile. I 5.250 contesti Youth League rimangono esclusi.
- Test: compilazione Java 21 + 52 smoke checks + lettura di 21 stagioni `rn_event` + assemblaggio 2022/23 e 2026/27 aggiornati.
- **Ancora da completare:** motore record nativo, calcolo apporto effettivo e tesseramenti giornalieri, JS integrati nel publisher 3.2, HTML finale con scheda giocatore e skin Renewo, GUI, verifica multistagione di tutti gli output e rilascio 4.0.0. Nessuna pubblicazione automatica.


## Fase 5B — Proiezione JS compatta v2 (6 novembre 2026)

- Archivio SQLite della 3.2 resta l’unica fonte completa, con export JS leggero per il sito.
- Codifica array+dizionari per stagione e URL unici per incontro/squadra; target 950kB, massimo bloccante 1.500.000 byte.
- Verifica integrale: 21 stagioni, 106 JS, 59.898.532 byte, stesso numero di giocatori/giornate/contesti del v1.
- Decoder browser disponibile; test semantico v1/v2 esatto su 2 stagioni.
- In questa fase staging soltanto: catalogo/pipeline/publisher/gui 3.2 ancora da integrare e testare.
- Evitare copie persistenti inutili: stessa fingerprint di DB+report => riutilizzo staging v2; vecchio v1 da eliminare solo esplicitamente.

---

## Integrazione viste interessanti TF01–TF20 — 2026-11-06 (fase implementata)

Exporter Java `TopFlopPublicViewsStageExporter`, decoder JS e test: proiezione completa STAGING verificata 21 stagioni, 85 JS, 5.355.536 byte, file piu grande 208.689 byte. Fonte completa SOLO SQLite. TF05/TF06 medie con soglie da approvare, TF11–TF13 e TF14–TF15 non attivati. Prossima attivita: integrazione nativa, manifest e cutoff 3.2, demo HTML skin e GUI, con regression e blocco 1.500.000 byte/file. Non distribuire staging sui siti.
~~~~

## `docs/REGISTRO_DECISIONI_RECORDSNEXT4_v26.md`

SHA-256: `36eebbc544805680bb44651d7b4da68240a18b1de1f2f8f90f421865b3143663`

~~~~markdown
# Addendum v25 — RecordsNext 4.0


## Decisioni 2026-11-06 — Catalogo selettivo e portieri (v25)

| ID | Stato | Decisione |
|---|---|---|
| D-182 | APPROVATA DALL'UTENTE | Siti = viste interessanti, record e carriere aggregate; archivio completo solo in SQLite locale. Nessuna copia integrale dei fatti nei JS pubblici. |
| D-183 | APPROVATA DALL'UTENTE | Catalogo TF01–TF16; definire fonte, formule, filtri e denominatori prima del publisher. |
| D-184 | APPROVATA DALL'UTENTE | Portieri partecipano ai record generali, anche gol segnati e assist. Eventi specifici `GOLSUBITI`, `GOLSUBITISURIGORE`, `RIGPAR` inclusi in TF17–TF20. |
| D-185 | VINCOLANTE | Non sommare sottoinsiemi `GOLFATTISURIGORE1` a `GOLFATTI1` o `GOLSUBITISURIGORE` a `GOLSUBITI`; distinguere le tre fonti gol. |
| D-186 | VINCOLANTE | Pubblicazione via 3.2, limite 1.500.000 byte per file e misura peso per sito; nessuna directory condivisa ReNewo obbligatoria. |
| D-187 | APERTA | Soglie minime di campione e valutazione `EXCLUDED_CONFIRMED`/fantavoti degli esclusi prima di attivare graduatorie definitive. |

---

# Registro decisioni RecordsNext 4.0 v23 — Export JS Top & Flop

| ID | Stato | Decisione / conseguenza |
|---|---|---|
| D-172 | APPROVATA | Ogni file JS pubblico di Top & Flop deve essere idealmente <=1 MB; **mai >1.500.000 byte reali**. Validazione preventiva e blocco dello staging se eccede. Nessun troncamento di righe. |
| D-173 | IMPLEMENTATA IN STAGING | Esportazione tramite `TopFlopStageJsExporter` nella stessa base Java 3.2: facade leggera, riepiloghi per stagione e shard di dettaglio per calciatore. Ogni riga di riepilogo indica direttamente il file di dettaglio da caricare. |
| D-174 | IMPLEMENTATA IN STAGING | Prestazioni reali una sola volta per `(season, FCA.ID, Serie A day)`; contesti fantasy distinti per competizione, incontro, fantasquadra. Nessuna fusione automatica per `CodiceGazza` in presenza di collisioni storiche. |
| D-175 | IMPLEMENTATA IN STAGING | Riepiloghi di apporto solo per slot conteggiati FCM già verificati. Somme gol su rigore incluse nei gol Gazzetta fonte 1: nessuna duplicazione negli aggregati. Esclusioni senza prova restano escluse dai record. |
| D-176 | IMPLEMENTATA IN STAGING | Link ai tabellini copiati SOLO dai `season_normalized_*.json` della 3.2, indicizzati per incontro e fantasquadra; nessun URL costruito dalla giornata/ID interno. Se non esistono report (es. RAW 2026/27) il link resta `null`. |
| D-177 | IMPLEMENTATA IN STAGING | Gli originali integrali rimangono nel SQLite e nei fatti Java; nella proiezione JS si eliminano solo zeri ripetitivi e i campi di origine non necessari alla visualizzazione, senza perdere eventi non nulli o voti. |
| D-178 | REGOLA DI PROTEZIONE | La 3.2 operativa, gli HTML/CSS e la GUI restano immutati. Non copiare gli shard nel target 3.2 senza aggiornare conteggio/prefissi/manifest/publisher/rollback/scope e test. |
| D-179 | APERTA | Le esclusioni dei calciatori non presenti in `Formazione` richiedono cronologia del tesseramento verificata per giornata; non attribuire prestazioni non sfruttate senza prova. |
| D-180 | APERTA | Integrazione familiare `TOP_FLOP` nel catalogo 3.2, generazione FULL/CONSOLIDATED, cutoff storico, visualizzatori HTML ReNewo e scheda calciatore. |
| D-181 | TEST SU COPIA COMPLETATO; WINDOWS DA CONFERMARE | Compilazione Java completa e verifica dei JS su database updated 2026/27 e 21 stagioni, bilancio conteggi ed hash SHA-256 database. |

---

# RecordsNext 4.0 - Registro decisioni v22 - Apporto registrato dal tabellino

Data: 2026-11-06. Revisione ADDITIVA alla v21, preservata integralmente sotto.

| ID | Stato | Decisione / verifica |
|---|---|---|
| D-165 | IMPLEMENTATA NEL NUOVO CODICE, PROVA WINDOWS PENDENTE | `Usage.COUNTED` per formazione presente nei primi 11 slot di `Tabellino.LISTA`, incontro `GIOCATO=1`, `Tabellino.TOT` numerico. È l'apporto *registrato dal tabellino FCM*, non una simulazione. I flag `ENTRATO` e `ENTRATORISERVA` restano separati. |
| D-166 | IMPLEMENTATA | Le prestazioni in panchina con totale numerico ma oltre i primi undici slot non entrano nei totali **apportati**. Le posizioni ambigue restano `UNKNOWN`, nessun azzeramento o attribuzione inventata. |
| D-167 | IMPLEMENTATA | Aggregazioni per `PlayerSeason`, fantasquadra, famiglia CAMPIONATO/COPPE, competizione, una volta per incontro fantasy. Prestazione reale unica nell'archivio FCA. Il codice storico non unisce automaticamente omonimie/collisioni. |
| D-168 | IMPLEMENTATA | Per la media voto reale usare `FCA.Punteggio.Voto1` e conservarne distinto il valore `FCM.Tabellino.Voto`. Per la fantamedia apportata usare `FCM.Tabellino.Tot` effettivamente registrato; nessun coefficiente inventato. |
| D-169 | IMPLEMENTATA | Eventi apportati sono quantità originali FCA associate SOLO a contesti `COUNTED`. Le tre fonti di `GOLFATTI1/2/3` non si sommano. Normalizzare per visualizzazione solo la fonte 1 (Gazzetta); `GOLFATTISURIGORE1` è una sotto-categoria dei gol, non una somma aggiuntiva. Conservare anche tutte le colonne grezze. |
| D-170 | APERTA / DA VERIFICARE IN LOCALE | Completamento esclusioni certe e periodi di tesseramento; nessun dato fuori `Formazione` trasformato automaticamente in esclusione. Le 4 formule dei voti sprecati restano separate dal contributo verificato. |
| D-171 | APERTA | Collegare questo modello al catalogo `TOP_FLOP`, all'exporter e agli shard flat della pipeline 3.2, con manifest, GUI e HTML ReNewo. Il CSV ora è solo verifica dei dati Java, NON il publisher finale. |

---

# Registro decisioni v19 — Demo integrativa


| ID | Stato | Decisione |
|---|---|---|
| D-158 | APPROVATA | Records Top & Flop è famiglia **nativa** dei records RecordsNext; stesso motore e stesso publisher 3.2, non secondo programma. |
| D-159 | APPROVATA | HTML demo con selezione skin e layout RecordsNext; profilo neutral ispirato a ReNewo, oltre alle altre skin 3.2. |
| D-160 | APPROVATA | Dati JS nella `js` del sito, con facade, shard flat per stagione, cutoff storico, rollback e link canonici; nessun URL inventato. |
| D-161 | APPROVATA | Varela: se fra cessione e reinserimento non passa una giornata di reale assenza dalla rosa, appartenenza continua. |
| D-162 | IMPLEMENTATA SOLO DEMO | Preview 2006/07 + 2026/27, nomi JS e limiti coerenti 3.2; nessuna abilitazione catalogo, GUI o publisher. |
| D-163 | APERTA | Strategie per shard di stagioni oltre 1536 KiB: non accettare troncamenti; usare estensione controllata publisher. |
| D-164 | APERTA | Calcolo esclusioni certe e loro quattro metriche: nessuna inferenza da sola mancanza in formazione o tesseramento snapshot. |

---

# RecordsNext 4.0 - Registro decisioni v18

# RecordsNext 4.0 - Registro decisioni v16

| ID | Stato | Decisione / evidenza |
|---|---|---|
| D-124 | IMPLEMENTATA E VERIFICATA | L'adattatore FCA Top & Flop usa JDBC SQLite e le **stesse tabelle raw importate dalla 3.2**, senza copie o nuove pipeline. |
| D-125 | VERIFICATA SU FONTI | `recordsnext.db`: 617310 GiocaIn e 16245 GiocatoreA stagionali; `recordsnext-sandbox.db`: 616018 e 16211. Differenza tutta nel 2026/27: usare test di consistenza dinamica. |
| D-126 | REGOLA TECNICA | Conservare nei fatti la distinzione `GiocatoreA.ID` locale vs `CodiceGazza` storico, senza attribuire identita canoniche nei casi dubbi. |
| D-127 | REGOLA TECNICA | Un record `GiocaIn` non prova da solo una presenza reale: interpretare `VOTO1>0` come voto valido, valori negativi come senza voto e zero/NULL come sconosciuti; preservare i valori originali. |
| D-128 | IMPLEMENTATA | Conservare integralmente campi GiocatoreA/GiocaIn/Punteggio nel campo diagnostico `sourceFields`, con marcatore esplicito per SQL NULL; nessuna interpretazione dei coefficienti bonus/malus prima della fonte FCM. |
| D-129 | APERTA | Completare la lettura di Statistiche/Storico/IncontroInA e gli eventi FCM; definire il modello temporale dei tesseramenti e la validita degli esclusi. |
| D-130 | REGOLA DI PROTEZIONE | La 3.2 operativa, il DB, la GUI, la selezione catalogo, i publisher e i siti non si modificano in questa fase. |


---

# RecordsNext 4.0 - Registro decisioni v15

Le decisioni D-001--D-112 sono preservate integralmente dalla v14. Nuove D-113--D-123:

| ID | Stato | Decisione / evidenza |
|---|---|---|
| D-113 | IMPLEMENTATA: MODELLO | Aggiungere la famiglia `app/topflop` al codice Java esistente, senza modificare le classi 3.2 ne attivare prematuramente `RecordFamily.TOP_FLOP`. |
| D-114 | APPROVATA/IMPLEMENTATA | Prestazione reale unica per stagione+ID FCA+giornata Serie A. Eventi fantasy separati per stagione+competizione FCM+incontro+fantasquadra+ID FCA. |
| D-115 | APPROVATA/IMPLEMENTATA | Conservare `CodiceGazza` storico e ID FCA stagionale distinti; non fondere automaticamente collisioni storiche. |
| D-116 | APPROVATA/IMPLEMENTATA | `Tesserato` e una fotografia della rosa, non dimostra tesseramento in una giornata non documentata. Nessuna esclusione inferita dal silenzio FCM. |
| D-117 | APPROVATA/IMPLEMENTATA | Youth League esclusa categoricamente; Play Off - Play Out e Spareggio Serie B = CAMPIONATO; 24 nomi ammessi codificati esplicitamente, altri nomi in revisione obbligatoria. |
| D-118 | APPROVATA/IMPLEMENTATA | Dati per titolari, conteggiati, panchinari e fuori lista; stato `EXCLUDED_CONFIRMED` utilizzabile solo dopo prova sorgente. |
| D-119 | APPROVATA/IMPLEMENTATA | Quattro metriche personali calcolate da voti/fantavoti integrali, `SV/P`, `SV/E`, `SF/P`, `SF/E`. Sulle fonti incomplete mantenere separati contatori voto/fantavoto, non inventare valori. |
| D-120 | APPROVATA/IMPLEMENTATA | Non sintetizzare mai URL tabellini: copiare dal normalizzato/link canonico 3.2; conservare ID incontro e giornata distinti. |
| D-121 | IMPLEMENTATA: TEST | Test Java standard con 52 assert e wrapper JUnit 5; compilazione completa codice Java 3.2+nuovo modulo verificata localmente con JDK 21, non suite Maven completa. |
| D-122 | REGOLA OPERATIVA | Il runner deve verificare JDK 21, git branch, file preesistenti e collisioni prima dell'installazione; output diagnostico sotto `D:\DEV_APPS\downloads`, mai `%TEMP%`; niente sovrascritture. |
| D-123 | APERTA | Realizzazione estrattore JDBC, identificazione esclusioni provate, ambito tesseramento, classifiche e soglie, integrazione piena RecordFamily/pipeline/exporter/GUI. Non segnare come implementati in v15. |

---

# RecordsNext 4.0 - Registro decisioni v14

# RecordsNext 4.0 - Registro decisioni v13

# RecordsNext 4.0 - Continuita documentale REGISTRO_DECISIONI_RECORDSNEXT4_v12

Versione additiva del 2026-11-01. La cronologia integrale precedente e preservata sotto.

# RecordsNext 4.0 - Registro decisioni v10

Data: 2026-11-01. Registro additivo: D-001--D-053 restano documentate nelle versioni v2--v8; i relativi stati non cambiano tacitamente.

| ID | Stato | Decisione / evidenza |
|---|---|---|
| D-054 | CONFERMATA: verifica Windows | ZIP 3B1 `RecordsNext4_Fase3B1_OUTPUT_v1_20261101_001744_607896.zip`: stato OK, sette test superati, 552.025 righe `Formazione`, 22.265 tabellini, tre SHA-256 dati verificati, SHA sandbox invariato. Bibbia v8 generata; il runner dichiara nessun commit/deploy. |
| D-055 | CONFERMATA: osservazione FCM | La catena per individuare il regolamento e `Incontro.IDGIRONE -> Girone.ID -> Girone.IDCOMPETIZIONE -> Competizione.ID` **della stessa stagione**. `Competizione.ID` non costituisce da solo identita storica. |
| D-056 | CONFERMATA: fonte | `Competizione` registra anche `NUMEROSOSTITUZIONI`, `NUMERORISUFFICIO`, voti d'ufficio, limiti panchina, ruoli sostituibili, regole speciali; `ModuloAmmesso` collega i moduli alla competizione. Nessun valore e convertito in punteggio recuperato. |
| D-057 | CONFERMATA: osservazione locale 3B2 | Sono presenti 180 righe `Competizione` su 21 stagioni. Nel 2019/20 tutte le 12 riportano `NUMEROSOSTITUZIONI=5` e `NUMERORISUFFICIO=3`; nel 2007/08 le tre riportano 3, 8, 11 sostituzioni e 0, 99, 99 RU. Valori non normalizzati arbitrariamente. |
| D-058 | CONFERMATA: separazione | Nel campione SQLite ci sono 168 tabellini senza incontro, 23 collegati a incontri con una squadra zero: non dichiararli automaticamente anomalie o riposi. I controlli si svolgono separatamente. |
| D-059 | APERTA | Interpretazione delle configurazioni elevate (8, 11, RU 99), possibili convenzioni per assenza di limite, rapporto con tabellini e modifiche al regolamento durante la stagione: da verificare sul modello FCM e su esempi reali. |
| D-060 | APERTA | Definizione rigorosa di prestazione conteggiata, prestazione disponibile in panchina e punti effettivamente recuperabili, per competizione, stagione e regole del ruolo. Niente calcoli prima dell'approvazione. |
| D-061 | IMPLEMENTATA: prototipo di sola lettura | Audit 3B2 con dati raw completi delle configurazioni, aggregati per competizione, moduli ammessi e verifiche integrita; 8 test locali superati. Certificazione Windows ancora da eseguire. |

Nota: non sono state prese nuove decisioni sui pesi dei record o sulle formule per i punti non sfruttati.


## Integrazione successiva a 3B2: metrica individuale (2026-11-01)

| ID | Stato | Decisione / motivazione |
|---|---|---|
| D-062 | CONFERMATA: verifica Windows 3B2 | ZIP `RecordsNext4_Fase3B2_OUTPUT_v1_20261101_002713_328984.zip`, stato OK; 8 test superati, 21 stagioni, 180 configurazioni, 13.072 incontri e 22.265 tabellini. SHA-256 del sandbox prima e dopo identico: `57923f4e75986c2b81533e4beef15b9133635258d7de1d36827a1bdbe576671b`. Bibbia v9 generata; nessun commit/deploy dichiarato. |
| D-063 | APPROVATA DALL'UTENTE: obiettivo | Il voto sprecato e un indicatore riferito a **uno specifico calciatore** lasciato fuori da una fantasquadra, non il vantaggio teorico che la fantasquadra avrebbe potuto ottenere sostituendo altri giocatori. Nessuna simulazione delle sostituzioni necessaria per questa metrica. |
| D-064 | APPROVATA DALL'UTENTE: formula | `mediaVotoSprecato = somma(voti validi del calciatore nelle occasioni in cui e rimasto fuori) / numero totale di partite in cui e stato lasciato fuori`. Le esclusioni senza prestazione utile contano nel denominatore e zero nel numeratore. Esempio: 23 esclusioni, 3 voti pari a 6: `18/23 = 0,782608...`. Non usare come media primaria `18/3 = 6`. |
| D-065 | CONFERMATA: RU e fonti 3.2 | La Riserva d'Ufficio e un voto regolamentare assegnato per un posto scoperto quando le sostituzioni ordinarie non bastano. Non e un calciatore ne un voto del calciatore lasciato fuori; non entra nella somma dei suoi voti sprecati. Preservare il sistema RU e gli strumenti personali esistenti. Fonti: `docs/ANALISI_PERSONALE_RU.md`, `docs/MODELLO_DATI_RECORDSNEXT2.md` e `tools/Report_RU_Storico_Completo_v8_4.ps1` (glossario). |
| D-066 | APPROVATA DALL'UTENTE: sostituzioni | Non esiste una regola generale di tre sostituzioni: si osservano anche 5 e, per recuperi a distanza di mesi e configurazioni eccezionali, anche 11. Mantenere il valore FCM della competizione senza normalizzarlo. Per la media individuale non simulare sostituzioni alternative. |
| D-067 | DA VERIFICARE PRIMA DEL CALCOLO | Stabilire con esempi reali il significato operativo di "lasciato fuori" (non nei primi 11 del tabellino finale, riserva non utilizzata, non schierato), il tipo di voto da sommare (voto Gazzetta base vs fantavoto totale), il trattamento di SV e voti speciali e il perimetro delle sole partite valide. Non inventare questi criteri. |
| D-068 | DA VERIFICARE PRIMA DEI RANKING | Evitare conteggi erronei di una stessa prestazione reale se ci sono piu competizioni fantasy nella stessa giornata. Conservare il dettaglio delle partite fantasy ed esplicitare la chiave di aggregazione del record personale. |
| D-069 | APERTA | L'utente ha menzionato un "valore massimo" senza definirne numericamente l'eventuale soglia o se si riferisca al massimo voto sprecato. Non fissare cap o limiti arbitrari. |
| D-070 | PROCEDURA | Comandi pronti per copia-incolla PowerShell 5.1, senza blocchi `else` separabili; file sempre versionati; ogni fase aggiorna piano, registro, diario, codice verificato e bibbia con SHA-256. |

### Nota di compatibilita

Le D-060 e D-061 riguardano anche un **possibile** studio dei punti recuperabili della fantasquadra. Per il nuovo record individuale quell'analisi non costituisce un prerequisito: questa decisione ne restringe il perimetro, senza cancellare la cronologia.


## Revisione metriche individuali (2026-11-01) - decisioni D-071--D-079

| ID | Stato | Decisione / motivazione |
|---|---|---|
| D-071 | APPROVATA DALL'UTENTE | Il voto di un calciatore non utilizzato e il **valore integrale del voto** (es. 7 resta 7), non il voto meno 6 e non il vantaggio potenziale della fantasquadra. |
| D-072 | APPROVATA DALL'UTENTE | Quattro indicatori distinti: media voto sprecato, errore pesato sul voto, fantamedia sprecata, errore pesato sul fantavoto. La formula v1 `S/E` rimane valida come **errore pesato**, ma non come unica misura principale. |
| D-073 | APPROVATA DALL'UTENTE | Con `E` esclusioni, `P` prestazioni valide, `SV` somma voti e `SF` somma fantavoti: `mediaVoto = SV/P`, `pesatoVoto = SV/E`, `mediaFantavoto = SF/P`, `pesatoFantavoto = SF/E`. Dove il denominatore e zero il risultato e non definito. |
| D-074 | APPROVATA DALL'UTENTE | Una gara in cui il calciatore non ha giocato/non ha voto non produce errore: somma zero al numeratore; se e una esclusione effettiva valida, rimane nel denominatore `E` del peso stagionale. |
| D-075 | APPROVATA DALL'UTENTE | Fantavoto comprensivo di eventi e relativi bonus/malus: gol normali, gol su rigore, assist, ammonizioni e altre categorie disponibili. Conservare separatamente voto, fantavoto ed eventi originali. Valori regolamentari da sorgenti reali, mai coefficienti unificati arbitrariamente. |
| D-076 | DERIVAZIONE MATEMATICA | `quotaConVoto=P/E`, `errorePesatoVoto=mediaVoto*quotaConVoto` e `errorePesatoFantavoto=fantamediaSprecata*quotaConVoto`, quando denominatori disponibili. Se `E>0, P=0`: indici pesati pari a zero, medie non definite. |
| D-077 | PROPOSTA, DA APPROVARE | Aggiungere massimo voto/fantavoto in singola esclusione, numero di esclusioni e quota con voto come indicatori di dettaglio, senza applicare alcun cap artificiale. |
| D-078 | APERTA - FCM/FCA | Definire con casi reali l'esclusione (panchinaro non usato, fuori lista, non schierato ma tesserato), la provenienza del fantavoto per giocatore lasciato fuori e la gestione di `SV`, voti d'ufficio e competizioni parallele. Non calcolare record reali prima della verifica. |
| D-079 | PROCEDURA / IMPLEMENTAZIONE | Aggiunto solo un test sintetico e la specifica v2. Nessuna modifica a database, Java, GUI, regolamenti, competizioni o siti. Bibbia incrementale v11 e SHA-256 dei nuovi file; vecchie versioni conservate. |

**Cronologia invariata:** D-064 `S/E` non e annullata: assume il nome corretto di **errore pesato stagionale** e si affianca alla nuova media `S/P` richiesta dall'utente.


## Decisioni D-080--D-090: archivio completo per giocatore e contesti paralleli

| ID | Stato | Decisione / evidenza |
|---|---|---|
| D-080 | APPROVATA DALL'UTENTE | Il prodotto deve conservare un set completo di dati per OGNI calciatore, con scheda individuale e record derivati dallo stesso archivio. Non estrarre soltanto le statistiche utili a record gia previsti. |
| D-081 | APPROVATA DALL'UTENTE | Un calciatore puo essere tesserato nello stesso anno da 2-3 fantasquadre diverse (serie distinte). Separare rigorosamente le osservazioni fantasy per squadra. |
| D-082 | APPROVATA DALL'UTENTE | Lo stesso calciatore nella stessa giornata puo essere usato in campionato ed escluso in coppa, e viceversa. Ogni incontro fantasy e un contesto indipendente; la prestazione reale e unica. |
| D-083 | APPROVATA DALL'UTENTE | Esattamente due famiglie per il filtro richiesto: CAMPIONATO e COPPE; conservare sempre anche la singola competizione e la sua identita stagionale. |
| D-084 | APPROVATA DALL'UTENTE | Dati individuali completi anche per titolari, giocatori conteggiati e subentrati, non soltanto per gli esclusi: voto, fantavoto, dettaglio bonus/malus, presenze, medie e contesti. |
| D-085 | CONFERMATA: FCM 2025/26 | `Tesserato` mostra il calciatore codice 100843 in tre squadre: 8 (River Pino), 49 (F.C. Pitbull), 77 (F.C. Boliverz). In giornata Serie A 4 River Pino lo ha in Serie B e Coppa Serie B (due incontri diversi). |
| D-086 | INFERENZA DA VERIFICARE | `Tesserato` e uno snapshot senza campi data di validita; non considerare automaticamente escluso il calciatore mancante da `Formazione` in ogni incontro di una squadra. |
| D-087 | PROPOSTA OPERATIVA | Mappa esplicita delle denominazioni FCM alle due famiglie, per stagione e competizione, invece di classificazione runtime con parole chiave. Per ora tre nomi senza assegnazione (Play Off - Play Out, Spareggio Serie B, Youth League), da approvare. |
| D-088 | REGOLA DI INTEGRITA | Chiave prestazione reale diversa dalla chiave evento fantasy; non moltiplicare gol/assist reali nelle aggregazioni all-time quando la stessa prestazione alimenta piu eventi fantasy. |
| D-089 | IMPLEMENTATA: AUDIT PROTOTIPO | Nuovo audit Fase 3C0 di sola lettura sui contesti. Verifica 552.025 righe Formazione, 21 stagioni, mappa delle competizioni, sovrapposizioni e caso reale codice 100843. Non crea ancora il dataset completo canonico. |
| D-090 | APERTA | Regole affidabili per esclusioni senza Formazione, coesistenza di piu partite della stessa famiglia nella giornata, disponibilita storica tesseramento, fantavoto del non schierato e trattamento Play Off/Spareggio/Youth League. |

## Decisioni D-091--D-098: classificazione completa e link ai tabellini

| ID | Stato | Decisione e verifica |
|---|---|---|
| D-091 | APPROVATA DALL'UTENTE | `Play Off - Play Out` appartiene alla famiglia CAMPIONATO. |
| D-092 | APPROVATA DALL'UTENTE | `Spareggio Serie B` appartiene alla famiglia CAMPIONATO. |
| D-093 | APPROVATA DALL'UTENTE | `Youth League` non deve essere considerata MAI nei record/statistiche/schede individuali. Nei soli audit fonti conservare l'evidenza della esclusione. |
| D-094 | APPROVATA DALL'UTENTE | I record individuali di singola partita devono contenere stagione, competizione, fantasquadra, ID incontro e link del tabellino di quella partita sul sito della stagione corrispondente. |
| D-095 | VERIFICATA NEL CODICE 3.2 | Esistono `urlTabellino`, `urlTabellinoLocale`, `urlTabellinoOnline`, `scorecardUrl`; la risoluzione della URL e gia implementata nel normalized exporter 3.2. Non fare concatenazioni inventate con la root del sito corrente. |
| D-096 | APERTA - LINK REALI | Prima della pubblicazione, verificare per ciascun sito/anno la rotta reale e la eventuale possibilita di puntare direttamente al singolo incontro. I link delle pagine di giornata non garantiscono un'ancora per il match. |
| D-097 | REGOLA DATI | L'ID incontro FCM e distinto dalla giornata Serie A e dalla competizione. Conservare tutti i campi per il record e per la scheda giocatore, anche in competizioni parallele. |
| D-098 | IMPLEMENTAZIONE AUDIT | L'audit v2 classifica Play Off/Spareggio, esclude Youth League dalle osservazioni utilizzabili e conserva contatori di esclusione. I riferimenti URL negli esempi rimangono null finche non risolti tramite 3.2. Nessun DB/Java/sito modificato. |


## Decisioni D-099--D-112: integrazione nativa Records Top & Flop

| ID | Stato | Decisione / evidenza |
|---|---|---|
| D-099 | APPROVATA DALL'UTENTE | La nuova famiglia records si chiama **Records Top & Flop**. E parte di RecordsNext 4.0, non un progetto o publisher distinto. |
| D-100 | APPROVATA DALL'UTENTE | Riutilizzare le logiche 3.2.0 FUNZIONANTI per elaborazione, archivio, configurazione, GUI, esportazione, manifest, link e pubblicazione multisito; la 3.2 produttiva rimane intatta. |
| D-101 | APPROVATA DALL'UTENTE | Tutte le statistiche individuali complete costituiscono la base riutilizzabile per schede e Records Top & Flop: calcolo dei record come aggregazione derivata, non raccolta dati autonoma. |
| D-102 | VERIFICATA SORGENTE 3.2 | Il catalogo record e in `RecordFamily`, `CoreRecordCatalog`; configurazione in `ProcessingConfigWriter` e `ProcessingConfigLoader`; `PipelinePreflight` e `DependencyInventory` gestiscono i prerequisiti. |
| D-103 | VERIFICATA SORGENTE 3.2 | La pipeline centrale e `RecordsNextPipeline` e il publisher JS e `Records2026SitePublisher`, con controllo esplicito del numero e dei prefissi dei JS prodotti. |
| D-104 | VERIFICATA SORGENTE 3.2 | `SeasonFamilyShardPublisher` divide per stagione, crea facade e shard JS flat nella cartella js con massimo predefinito 1536 KiB. Estendere la lista FAMILIES e i relativi test. |
| D-105 | VERIFICATA SORGENTE 3.2 | `SeasonNormalizedExporter` produce i riferimenti canonici al tabellino; `MatchesJsExporter` li espone tramite `scorecardUrl`. Nessun URL storico sintetizzato a mano. |
| D-106 | REGOLA IMPLEMENTATIVA | Il filtro sportivo CAMPIONATO/COPPE e separato dalla famiglia catalogo `TOP_FLOP`. Play Off - Play Out e Spareggio Serie B=CAMPIONATO; Youth League sempre esclusa dai dati pubblici. |
| D-107 | REGOLA IMPLEMENTATIVA | Stessa prestazione reale FCA non conteggiata piu volte nei record reali, anche se utilizzata in molte partite/competizioni fantasy; ogni match fantasy resta distinto nei record fantasy. |
| D-108 | REGOLA IMPLEMENTATIVA | Prevedere FULL e CONSOLIDATED, multisito, rollback e validazioni JS 3.2: nessuna scorciatoia per i nuovi dati e nessun deploy autonomo. |
| D-109 | REGOLA IMPLEMENTATIVA | Non attivare la nuova famiglia nel catalogo selezionabile o in pubblicazione finche archivio e exporter non sono funzionanti e testati; impedire successo apparente con output vuoti. |
| D-110 | VERIFICATA FASE 3C1 | ZIP Windows 3C1: `stato=OK`, 13 test, 552025 originali, 546775 ammessi, 5250 Youth esclusi, DB sandbox invariato, bibbia v13 generata. |
| D-111 | APERTA | Elenco definitivo e soglie dei singoli records Top & Flop, definizione formale delle esclusioni senza Formazione e validita storica dei tesseramenti. Non inventare classifiche o soglie. |
| D-112 | IMPLEMENTATA: DOCUMENTAZIONE | Questa fase aggiunge mappa integrazione e regole alla continuita documentale, senza modificare la pipeline Java, la GUI, gli output o il database. La famiglia non e ancora implementata. |


## Decisioni D-131--D-139 - Collegamento JDBC FCM v1

| ID | Stato | Decisione / verifica |
|---|---|---|
| D-131 | APPROVATA DALL'UTENTE | Il modulo e' la famiglia nativa Records Top & Flop, estesa dalla 3.2, non una pipeline separata. |
| D-132 | IMPLEMENTATA E VERIFICATA | Il lettore FCM usa le relazioni originali Formazione/Incontro/Girone/Competizione/Tabellino e ID FCA della medesima stagione. |
| D-133 | IMPLEMENTATA E VERIFICATA | Competizioni parallele e piu fantasquadre non sono aggregate: una riga per giocatore/incontro/team; 552025 contesti originari. |
| D-134 | APPROVATA DALL'UTENTE | Youth League esclusa dai dati utilizzabili per record/schede; 5250 righe sono comunque contate nell'audit. Play Off e Spareggio Serie B restano CAMPIONATO. |
| D-135 | REGOLA TECNICA | `IDGIORNATA` FCM resta chiave dell'archivio, distinta da `GIORNATADIA`; non dedurre un ordinale e non costruire URL arbitrari. |
| D-136 | REGOLA TECNICA | Il posto nei primi undici slot del Tabellino non certifica da solo la prestazione legalmente conteggiata; ENTRATO/ENTRATORISERVA restano campi fonte distinti. |
| D-137 | REGOLA TECNICA | Tesserato FCM e' snapshot, non archivio cronologico; non inventare esclusioni dove manca una riga Formazione. |
| D-138 | IMPLEMENTATA | Fonte JSON/JS pubblica non ancora generata: lettore Java FCM in streaming e test JDBC senza modifica a DB, pipeline o GUI. |
| D-139 | APERTA | Integrare nella stessa istanza `TopFlopArchive` FCA+FCM, provenienza del fantavoto per esclusi, regole voti speciali e URL tabellini con normalizzatore 3.2. |


## Decisioni D-140--D-147 - Assemblaggio nativo per stagione

| ID | Stato | Decisione / verifica |
|---|---|---|
| D-140 | IMPLEMENTATA IN FASE 4A | Assemblare fatti FCA e FCM nella stessa istanza `TopFlopArchive`, partendo da FCA e collegando ogni formazione mediante ID locale e giornata reali. |
| D-141 | VERIFICATA SU FONTE | `Tesserato` presenta righe ripetute per stessa fantasquadra/giocatore: vendite e successivi riacquisti possibili. Chiave distinta dell'evento: `Tesserato.ID` per stagione; non usare solo giocatore+squadra per conservare la cronologia sorgente. |
| D-142 | IMPLEMENTATA IN FASE 4A | `LoadedSeason.rosterEvents` conserva tutte le righe originali e i rispettivi ID; `TopFlopArchive.rosters` rimane vista sintetica di membership. Non inferire intervalli di appartenenza. |
| D-143 | IMPLEMENTATA IN FASE 4A | I token di voto/fantavoto dei tabellini FCM restano nei campi sorgente; convertire in BigDecimal solo i numerici con virgola/punto, senza convertire `sv/ng` in zero. |
| D-144 | REGOLA CONSERVATIVA | Gli slot del tabellino registrano evidenza ma non certificano utilizzo legalmente conteggiato: non produrre automaticamente `EXCLUDED_CONFIRMED` e non gonfiare i record. |
| D-145 | REGOLA CONSERVATIVA | `scorecardUrl` rimane null durante l'assemblaggio JDBC; risolverlo esclusivamente con le logiche normalizzate 3.2, al momento dell'integrazione nella pipeline. |
| D-146 | DA VERIFICARE | La provenienza della fantamedia per giocatori fuori da Formazione, l'esclusione certa durante un tesseramento storico e le regole RU/sostituzione sono ancora aperte. |
| D-147 | REGOLA DI REGRESSIONE | La nuova classe e i test non modificano pipeline, catalogo, GUI, sito o DB; testare gli hash e mantenere separati dati reali e contesti fantasy. |


## D-148--D-157 - Conferme 2026/27 e cronologia mercato

| ID | Stato | Regola o evidenza |
|---|---|---|
| D-148 | CONFERMATA DALL'UTENTE | 9 giornate reali di Serie A disponibili nello snapshot 2026/27. |
| D-149 | CONFERMATA DALL'UTENTE | La AlterLega 2026/27 ha cominciato dalla giornata reale 3; assenza di incontri nelle giornate 1 e 2 NON e' errore. |
| D-150 | IPOTESI DELL'UTENTE - DA VERIFICARE | La cronologia dei tesseramenti potrebbe essere legata a giornate della Serie A vera. Non assumere che `ULTIMAGIORNATA` sia la prima giornata eleggibile. |
| D-151 | FONTE VERIFICATA | `Tesserato` non contiene date di validita'. `EntryInBilancio` contiene `DATATRANSAZIONE` e `ULTIMAGIORNATA` e deve restare distinto dalla membership. |
| D-152 | CONFERMATA DALL'UTENTE | Calciatore ceduto e riacquistato in un'asta successiva e' operazione normale; riacquisti nella stessa sessione possono essere correzioni. Le sole date non provano la sessione. |
| D-153 | CONFERMATA DALL'UTENTE | Varela/FortisSIMY 2026/27 e' una correzione da svincolo accidentale; preservare tesseramenti 699 e 752 e relativi movimenti contabili. |
| D-154 | VERIFICATA: SNAPSHOT IMPORTATO 3.2 | 2026/27 aggiornato: 28956 righe reali FCA, 7650 `Formazione`, 813 `Tesserato`, 876 `EntryInBilancio`; 3..9 giornate reali con formazioni fantasy. |
| D-155 | REGOLA IMPLEMENTATIVA | Il nuovo assemblatore conserva tutte le righe `Tesserato` con ID sorgente e usa vista membership sintetica; nessuna esclusione senza prova di tesseramento in quel giorno. |
| D-156 | TEST AGGIUNTIVO | Verifica specifica sul DB 2026/27 aggiornato, compreso Varela e l'inizio della fantasy alla giornata 3. I dati di output test non sono regole eterne per altre stagioni. |
| D-157 | RIMANE APERTO | Non integrare automaticamente la datazione del possesso e il dettaglio `EntryInBilancio` in record pubblico fino a chiarimento dei campi originali FCM. |


---

## Addendum correttivo demo v2 — 2 novembre 2026

**D-158 (approvazione tecnica conservativa):** i quattro `release/visualizzatori/profiles/*/recordsnext.css` della 3.2, eventualmente modificati nel worktree utente, sono la fonte autoritativa di stile. La demo non li sovrascrive e deve utilizzarli dal loro percorso originale. Un CSS mancante puo essere installato dal kit come fallback. Ogni differenza viene registrata nel report; non richiede convergenza dell'hash con la copia del kit. Nessuna modifica al sito operativo. **Stato:** implementato nel runner v2, verifica Windows da completare.

## Decisioni e anomalie — 2026-11-06

| ID | Stato | Decisione / evidenza |
|---|---|---|
| D-158 | CONFERMATA DAL CODICE 3.2 | La giornata di competizione è `rn_event.competition_round`; `IDGIORNATA` è ID tecnico FCM e non deve essere mostrato come giornata. |
| D-159 | CONFERMATA | Conservare `FCM.Giornata.DESC` e `FCM.Incontro.IDGIORNATA` separatamente, senza normalizzare arbitrariamente etichette speciali. |
| D-160 | VINCOLANTE | Se `rn_event` non contiene una partita, il numero di giornata canonico resta nullo. Conservare la partita FCM, la descrizione e il motivo `UNMAPPED_SOURCE_ONLY`. |
| D-161 | ANOMALIA VERIFICATA, NON CORRETTA | Nel 2022/23 il mapping 3.2 marca `ESCLUSA` Spareggio Serie B (50 formazioni) e Vera finale di Coppa tra le Coppe (50): esiste un conflitto con il perimetro Top & Flop approvato. Non modificare il mapping 3.2 automaticamente. |
| D-162 | CONFERMATA | Le 5250 formazioni Youth League del 2023/24 rimangono escluse da Top & Flop, come richiesto dall'utente. |
| D-163 | STATO | Patch limitata al modello/assemblatore Java; non abilita la famiglia nel catalogo, pipeline, GUI, publisher o skin. |


## Decisioni D-164–D-171 — Ottimizzazione JS Top & Flop

| ID | Stato | Decisione |
|---|---|---|
| D-164 | APPROVATA UTENTE | Non replicare il database storico in output JS verbosi: SQLite FCM/FCA resta fonte completa, la pubblicazione contiene solo dati di consultazione. |
| D-165 | APPROVATA UTENTE | Prima ottimizzare senza perdita di informazioni (array compatti, dizionari, URL deduplicati); eventuali viste più specifiche soltanto dopo misura. |
| D-166 | TESTATA | v2 riduce l’export 21 stagioni a 59.898.532 byte / 106 file; massimo 949.971 byte per JS. |
| D-167 | VINCOLANTE | Ogni file JS UTF-8 non può superare 1.500.000 byte; target 950.000, errore prima di scrivere un giocatore troppo grande. |
| D-168 | VINCOLANTE | Nessun dato reale, contesto FCM, stato voto, evento bonus/malus o URL di tabellino può essere perso per compattare; decodifica semantica controllata contro v1. |
| D-169 | REGOLA | URL unici per matchId e teamId dentro la stagione, ottenuti solo dalla 3.2; dettagli e riepiloghi richiesti in modo differito dal browser. |
| D-170 | REGOLA | Staging v2 riutilizzato se gli hash delle fonti coincidono; non cancellare v1 o dati preesistenti senza autorizzazione. |
| D-171 | NON ANCORA FATTO | Catalogo nativo, GUI, publisher 3.2, cutoff e visualizzatori definitivi ancora da integrare: i JS v2 NON sono pubblicati. |

---

## D26 — Vista pubblica, non archivio duplicato (approvato; implementato in staging)

Catalogo TF01–TF20 confermato inclusi portieri goleador. Spedire soltanto Careers, Teams, Records e Highlights; archivio completo dei fatti FCA/FCM resta SQLite. Limite 1.500.000 byte per JS, obiettivo sotto 1 MB; caricamento lazy. TF05/TF06 non ordinate come record definitivi senza soglie; TF11–TF13 disattivate senza proprieta verificata, TF14–TF15 senza baseline/soglia. Identita storiche non fuse automaticamente, pareggi conservati, URL dalla 3.2. Nessun nuovo publisher parallelo o shared ReNewo obbligatorio.
~~~~

## `docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v26.md`

SHA-256: `0877a91425c9b6a1507001b15dfb2d3fbfb5149a3abb4f27db82769fabe0baf5`

~~~~markdown
# Addendum v25 — RecordsNext 4.0


## 2026-11-06 — Specifica catalogo TF01–TF20

Verificati sui sorgenti Java TopFlop e sulla RAW FCA 2026/27 i campi dei portieri e gli eventi. Redatta specifica `CATALOGO_RECORD_E_VISTE_TOPFLOP_v1.md`, approvato il catalogo funzionale delle cinque viste e l'estensione ai portieri. Non e stato implementato ne testato un nuovo exporter di viste; v1/v2 di esportazione fatti restano in staging e non vanno pubblicati. Prossima fase: implementare proiezioni selettive, verificare dati, cutoff, JS e non-regressione 3.2.

---

# Diario RecordsNext 4.0 v23 — Esportazione JS staging

Data 2026-11-06. Da sorgenti Java v22 realmente installati e database 2026/27 aggiornato.

- Aggiunta lettura stagionale efficiente di tutti i fatti `TopFlopArchive` senza scansioni per giocatore O(N²).
- Creati `TopFlopStageJsExporter`, `TopFlopCanonicalScorecards` e wrapper parser `TopFlopExistingJson` che riusa il JSON della 3.2.
- Generati shard ordinati per giocatore. Facade per stagione, riepiloghi con collegamento `detailFile`, dettagli lazy e link canonici originali.
- Scrittura binaria UTF-8 e verifica dimensione fisica ogni file; budget target 950.000, hard 1.500.000 byte.
- Su due stagioni pilota 2006/07 e 2026/27: 1419 anagrafiche, 53922 giornate FCA, 13700 contesti fantasy, 17 file e scorecard 2006 dal normalizzato 3.2.
- Su 21 stagioni e database aggiornato: 16262 anagrafiche stagionali, 617956 righe reali, 554425 contesti FCM ammessi, 389 file (senza report canonici nel test integrale), massimo 949991 byte.
- L'esportazione integrale con i 156 report normalizzati 3.2 ha prodotto 430 JS, 546101 link canonici e un totale staging di circa 372 MB (limite sempre per singolo file, non sull'insieme). Il caricamento online non è stato attivato.
- Parser di verifica standard Python per integrità di tutti i JS: 1 riga riepilogo/dettaglio per giocatore, esattezza locator, assenza Youth League, identificativi univoci, conteggi cumulati, limite byte, URL http(s).
- Controllo JavaScript nativo in Node del caricamento selettivo per stagione+calciatore con corrispondenza dettagli riuscito.
- NON modificati catalogo, pipeline, GUI, publisher, manifest o sito. NON completate esclusioni certe, dettaglio HTML definitivo o link per 2026/27 senza normalizzato aggiornato.

---

# RecordsNext 4.0 - Diario v22

2026-11-06. Fase apporto FCM: in `TopFlopJdbcArchiveAssembler`, first-eleven+incontro giocato+Tot numerico => `COUNTED`; evidenza originale in `TopFlop.UsageEvidence`. Nuovi `TopFlopContributionSummary` e `TopFlopContributionReport`: medie e somme voto reale, voto tabellino, totale fantasy, conteggi per contesto, eventi originali e normalizzati solo fonte 1. Le esclusioni non sono dedotte.

Prova sulla copia SQLite aggiornata 2026/27: 3327 slot COUNTED, 2006/07: 2585. Report di Lautaro (100843) 2026/27: 9 combinazioni squadra/competizione, divise fra tre fantasquadre; nessun doppio conteggio nella carriera reale. Prima di applicare il kit in Windows verificheremo file e SHA-256 del v21.

Non modificate pipeline 3.2, publisher, schema DB, CSS, GUI e configurazioni dei siti.

---

# Diario sviluppo v19 — Demo HTML e JS


2026-11-02: consultata direttamente la struttura `release/visualizzatori` della 3.2 con i profili CSS; esaminati `SeasonFamilyShardPublisher`, `MatchesJsExporter`, normalizzazione URL e struttura SQLite aggiornata. Implementato **solo kit demo**: HTML Top & Flop, viewer JS, facade e due shard reali. Restano inalterati catalogue Java, manifest, pipeline, GUI, database e siti. Il generatore Python è uno strumento preview read-only e **NON** il publisher nativo. JS e hash verificati localmente, prova browser Windows ancora richiesta.

---

# RecordsNext 4.0 - Diario di sviluppo v18

# RecordsNext 4.0 - Diario v16: lettore FCA JDBC

2026-11-01: confrontati realmente i due file SQLite della 3.2 allegati. Rilevata differenza confinata al 2026/27, esplicitata in piano, registro, specifica e test. Implementato `TopFlopFcaJdbcReader` come nuova classe nel package Java esistente `app/topflop`. Prove Java 21 su 21 stagioni di ciascuno dei due DB, con controllo contro conteggi SQL indipendenti, e test filtrato codici 100843/12302. Source e database produttivi invariati. Validazione finale della installazione sul PC ancora da ricevere. Non vengono fabbricati `FantasyFact` o esclusioni.


---

# RecordsNext 4.0 - Diario implementazione v15

## Intervento: primo codice Java Top & Flop

- Riletti `RecordFamily`, `CoreRecordCatalog`, `SeasonNormalizedExporter`, `MatchesJsExporter`, specifica e mappa integrazione v14, sorgenti 3.2 contenuti nell'archivio dell'utente.
- Aggiunte classi nuove `TopFlopFacts`, `TopFlopArchive`, `TopFlopMetrics`, `TopFlopCompetitionPolicy` in `app/topflop`, senza riscrivere le classi precedenti.
- Aggiunti test autonomo `TopFlopArchiveSmokeTest` (52 verifiche) e wrapper `TopFlopArchiveJUnitTest` per la suite JUnit 5 esistente.
- Eseguita compilazione dei 106 sorgenti Java originali + quattro nuove classi col compilatore Java 21.0.12.1: OK. Eseguito smoke test Java: 52 verifiche OK. Maven/JUnit integrale non eseguito nell'ambiente di analisi; la verifica sul PC utente resta da completare.
- Predisposti runner conservativo, nuova bibbia incrementale v15 e ZIP diagnostico in `downloads`.
- La prova della seconda esecuzione ha inizialmente evidenziato compilazione duplicata dei quattro file gia installati; corretto il runner per escludere le classi del kit dall'elenco dei sorgenti preesistenti. Nuova prova integrale eseguita da zero con due passaggi consecutivi (vedi log di verifica).
- Nessuna modifica a database, GUI, publisher, archivio siti, configurazione o versione Maven. Nessun commit o deploy automatico.

---

# RecordsNext 4.0 - Diario implementazione v14

# RecordsNext 4.0 - Diario implementazione v13

# RecordsNext 4.0 - Continuita documentale DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v12

Versione additiva del 2026-11-01. La cronologia integrale precedente e preservata sotto.

# RecordsNext 4.0 - Diario implementazione v10

Data: 2026-11-01.

## Revisione dello ZIP Windows 3B1

- ZIP `RecordsNext4_Fase3B1_OUTPUT_v1_20261101_001744_607896.zip`: 14 voci; `MANIFEST_FASE3B1_v1.json` con `stato=OK`.
- Log `LOG_TEST_CONTRIBUZIONI_v1.txt`: sette test su Windows, tutti superati.
- SHA-256 dei file `combinazioni.csv`, `campioni.csv`, `sintesi.json` controllati confrontando gli hash dichiarati nel manifest con i byte nello ZIP.
- Invariato lo SHA del database sandbox: `57923f4e75986c2b81533e4beef15b9133635258d7de1d36827a1bdbe576671b`.
- 22.265 tabellini, 552.025 righe Formazione, 237.686 giocatori nei primi undici slot, 22.028 panchinari nei primi undici, 27.319 titolari iniziali fuori dai primi undici; nessuna attribuzione di punti persi.
- `CODICE_FUNZIONANTE_RECORDSNEXT4_v8.md` generato; nessun commit/deploy eseguito dal runner.

## Sviluppo Fase 3B2

- Riletto lo schema raw FCM originale della 3.2: `Competizione`, `Girone`, `Incontro`, `Tabellino`, `Modulo`, `ModuloAmmesso` e analisi RU personali.
- Implementato `Audit-RecordsNext4Regole-v1.py` in modalita SQLite `mode=ro` + `query_only=ON`.
- Il censimento e per coppia stagione/competizione, con mantenimento di tutte le colonne originali `Competizione` e dei moduli ammessi. La tabella di dettaglio non contiene dati personali delle fantasquadre.
- Osservati 21 archivi, 180 configurazioni, 13.072 incontri (compresi quelli programmati/riposi), 22.265 tabellini; 168 senza ID incontro valido e 23 collegati a incontri con una squadra 0.
- Test automatici `Test-RecordsNext4Regole-v1.py`: otto test sintetici e su DB reale, superati in ambiente di prova. Il database sintetico temporaneo viene generato sotto la cartella diagnostica `downloads`, mai in `%TEMP%`.
- Progettati runner versionato v1, specifica e verifica v1, piano v10, decisioni v9 e bibbia v9 con SHA-256 dei nuovi file.
- La verifica esecutiva del nuovo runner su PowerShell 5.1/Windows e ancora da effettuare; non presentarla come completata.

## Esclusioni

Nessuna modifica ai dati, alle impostazioni delle competizioni, ai 4 script RU personali, al Java, al sito o alla GUI. Nessun calcolo di voti recuperabili, nessun commit.


## Verifica dell'output Fase 3B2 sul PC e decisione operativa

- Esaminato ZIP Windows `RecordsNext4_Fase3B2_OUTPUT_v1_20261101_002713_328984.zip`.
- `MANIFEST_FASE3B2_v1.json`: `stato=OK`, branch `feature/recordsnext4-players`, base `f9bf30323d9204d70e91e242bddfc72c2060ecf9`; stessa impronta SHA-256 del sandbox prima/dopo.
- `LOG_TEST_REGOLE_v1.txt`: otto test superati. `LOG_AUDIT_REGOLE_v1.txt`: 21 stagioni, 180 competizioni, 13.072 incontri, 22.265 tabellini; 168 riferimenti senza incontro rimangono non classificati.
- Riletto `docs/ANALISI_PERSONALE_RU.md` e il glossario di `tools/Report_RU_Storico_Completo_v8_4.ps1`: RU = voto assegnato d'ufficio a copertura di una mancanza. Non associare una RU a un giocatore storico reale.
- L'utente ha approvato la metrica **individuale**, non il controfattuale di squadra: 23 occasioni fuori, 3 gare con voto 6 -> 18/23. Nessun calcolo effettivo sui giocatori e nessun cap implementati in questa revisione.
- Creato test unitario della **sola formula** su dati sintetici. Prima della successiva implementazione su SQLite occorre verificare su esempi reali quali righe costituiscano esclusioni e quale voto sia valido.
- Questa revisione installa soltanto documentazione, un test di formula, il generatore v10 e un runner conservativo. Non modifica Java, GUI, pipeline, archivi 3.2 o siti.


## Revisione della metrica su richiesta utente (2026-11-01)

- Esaminato lo ZIP di esecuzione Windows `RecordsNext4_DecisioniGiocatori_OUTPUT_v1_20261101_003202_168980.zip`: contiene bibbia v10, registro v10, test formula v1 e manifest `stato=OK`.
- Distinti il rendimento medio nelle sole esclusioni con voto (`S/P`) e la misura stagionale su tutte le esclusioni (`S/E`), per voto reale e fantavoto completo di bonus/malus.
- Creati `SPECIFICA_METRICA_VOTO_SPRECATO_GIOCATORE_v2.md` e `Test-RecordsNext4MetricheIndividuali-v2.py` per il contratto puramente aritmetico; test su dati sintetici, nessuna lettura FCM/FCA in questo intervento.
- Creati piano v12, registro decisioni v11, diario v11 e generatore bibbia v11. Conservate le versioni precedenti.
- Restano da verificare sui dati reali la definizione operativa di esclusione, voto valido, fantavoto disponibile per esclusi, bonus/malus storici e deduplicazione delle partite fantasy parallele.
- Nessuna implementazione di record nei sorgenti Java e nessun deploy; database invariato perche non viene aperto.


## Fase 3C0 - 2026-11-01

- Verificato lo ZIP Windows `RecordsNext4_Metriche_OUTPUT_v1_20261101_004055_490269.zip`: manifest OK, 8 test metriche superati, bibbia v11 prodotta; nessun deploy/commit dichiarato.
- Esaminato il database sandbox 3.2 in sola lettura: 21 stagioni, 552.025 righe Formazione, 180 righe Competizione con 25 nomi distinti; la stessa giornata puo contenere piu incontri fantasy per giocatore-squadra.
- Caso campione 2025/26, codice 100843: tre tesseramenti snapshot, presenza nello stesso giorno in campionato e coppa per River Pino.
- Il prototipo di audit Fase 3C0 verifica l'integrita delle join ed elenca le competizioni non assegnate, senza interpretare le esclusioni o sommare i voti.
- Stato: audit testato localmente, codice integrale nella bibbia incrementale v12; installazione e test Windows ancora da confermare.

## Fase 3C1 - revisione classificazione e riferimenti tabellino

- Ricevuto e verificato output 3C0 Windows: `ESITO OK`, 10/10 test, 552025 righe Formazione; SHA256 del sandbox invariato, bibbia v12. Le 159678 sovrapposizioni sono contesti fantasy distinti, non duplicazioni reali.
- L'utente ha classificato `Play Off - Play Out` e `Spareggio Serie B` come campionati e richiesto l'esclusione completa di `Youth League` da record/schede.
- Fonte Java 3.2 esaminata: `SeasonNormalizedExporter.java` espone `urlTabellino`, `urlTabellinoLocale`, `urlTabellinoOnline` e conserva `source_event_id`; `MatchesJsExporter.java` espone `matchId`, `competitionId`, `seasonId`, `scorecardUrl`.
- L'audit v2 mantiene ID incontro e giornata; aggiunge `idGiornataFcm` e `scorecardLinkStatus` negli esempi. URL effettivo assente nell'audit raw: resta null.
- Nessuna simulazione RU/sostituzioni, nessun calcolo degli esclusi mancanti da Formazione, nessuna modifica alla pipeline o al DB.


## Revisione 2026-11-01 - nuova famiglia Records Top & Flop

- L'utente ha chiarito che l'intero modulo giocatori e **una famiglia record del RecordsNext esistente**, con il nome pubblico **Records Top & Flop**.
- Rianalizzati gli effettivi sorgenti 3.2.0 forniti (`RecordsNext2.0.zip`), in particolare `RecordFamily`, `CoreRecordCatalog`, `ProcessingConfigWriter`, `ProcessingConfigLoader`, `RecordsNextPipeline`, `Records2026SitePublisher`, `SeasonFamilyShardPublisher`, `MatchesJsExporter`, `SeasonNormalizedExporter`, `RecordsNext2Dashboard`.
- `Records2026SitePublisher.validateGenerated` valida anche **il conteggio totale** dei file JS e i loro prefissi: richiede modifica coordinata prima di aggiungere un nuovo exporter. `SeasonFamilyShardPublisher.FAMILIES` e la lista esplicita delle famiglie sharded: richiede aggiunta con test flat.
- La 3.2 usa facade/shard per stagione nei js dei siti, con scope multisito e protezione rollback. Va **estesa**, non replicata con tool dedicati.
- Verificato output Windows Fase 3C1: 13/13 test, 21 stagioni, 552025 Formazione, 5250 Youth League escluse, 546775 ammesse, DB immutato.
- Stato codice: **nessun Java/SQLite/GUI/sito modificato in questa revisione**. Restano prototipi audit; prossimo sviluppo sul codice Java nel worktree 4.0. Documenti e script presenti nella bibbia incrementale v14.


## Sviluppo 2026-11-01 - Lettore FCM JDBC v1

- Verificato il report Windows FCA v16: esito OK, nessuna variazione hash database o codice 3.2.
- Ispezionato schema completo raw FCM 2006/07-2026/27 e joins originali; nessuna variazione delle colonne principali tra le 21 stagioni.
- Creato `TopFlopFcmJdbcReader.java` con scansione streaming delle Formazione, Tabellino, Incontro, Girone, Competizione, Fantasquadra, anagrafica FCA e GiocaIn. Separato il censimento Tesserato.
- Preservati testi originari del Tabellino e ID di giornata FCM; esclusi Youth League, inclusi Play Off e Spareggio nella corretta famiglia.
- Completati test integrali su entrambe le copie SQLite disponibili: 552025 Formazione, 546775 ammesse, 5250 Youth League, 14320 Tesserato, tutte le join e gli slot verificati.
- Preparati installer non distruttivo, verifica doppia esecuzione, regressione 52 test, controllo hash DB e bibbia incrementale v17.
- Nessuna modifica a 3.2, export, GUI o pubblicazione; verifica Windows sul PC reale in attesa di ZIP diagnostico.


## 2026-11-02 - Collegamento Java FCA/FCM fase 4A

- Verificato lo ZIP Windows `RecordsNext4_FcmJdbc_OUTPUT_v1_20261102_143437_776784.zip`: manifest `OK`, compilazione e regressioni 52/FCA/FCM superate, hash di entrambi i DB invariati, bibbia v17 generata.
- Rilevati e verificati tesseramenti con stessa coppia giocatore+fantasquadra ma ID `Tesserato` differenti; il modello sintetico precedente non era sufficiente a conservare gli eventi sorgente.
- Aggiunta la classe additiva `TopFlopJdbcArchiveAssembler.java`: costruisce `TopFlopArchive` stagionale dagli adapter esistenti e restituisce separatamente tutti gli eventi Tesserato.
- Test con JDBC SQLite in sola lettura, con riconciliazione di giorni reali, formazioni ammesse, Youth esclusa, tesseramenti sorgente e membership univoche; nessuna inferenza di esclusioni, URL, bonus o malus.
- Stato: test pilota locali superati. Il runner Windows produce ZIP diagnostico e bibbia v18; verifica finale Windows attesa.


## 2026-11-02 - Nuovi FCA/FCM e validazione stagione 2026/27

- Ricevuti FCM/FCA aggiornati e DB di prova importato con importatore RAW originale RecordsNext 3.2; validati con `integrity_check`.
- L'utente conferma: 9 giornate reali, AlterLega iniziata alla terza.
- Varela/FortisSIMY: due ID tesseramento 699/752 per svincolo accidentale e reinserimento; mantiene singola identita' reale.
- Audit su `EntryInBilancio` con date e `ULTIMAGIORNATA`, significato preciso ancora non certificato.
- Aggiunto test aggiornato `TopFlopJdbcArchive2026SmokeTest` al kit dell'assemblatore.
- Test Windows e installazione in worktree 4.0 da effettuare; nessuna modifica al database operativo 3.2.


---

## Addendum correttivo demo v2 — 2 novembre 2026

**2 novembre 2026:** il runner della demo v1 si e interrotto nel preflight, rilevando un CSS neutral esistente con hash diverso. Nessun file era stato copiato. Runner v2 sviluppato per riutilizzare CSS preesistenti senza sovrascriverli, mantenendo la verifica stringente per tutti gli altri file e la generazione di bibbia v19 e v20. Prova Linux ripetuta in worktree con CSS diversi; prova PowerShell/Windows ancora da confermare.

## 2026-11-06 — Patch canonica giornate Top & Flop v1

Esaminato lo ZIP `RecordsNext4_SORGENTI_ATTUALI_20261106.zip` (codice e documentazione correnti). Confrontate `CanonicalViews`, `TopFlopFcmJdbcReader`, `TopFlopFacts`, `TopFlopJdbcArchiveAssembler` e le viste SQLite 3.2 del database aggiornato. Rilevato uso errato di IDGIORNATA come fantasyRound; introdotto resolver canonico con ordinamento della 3.2 e fallback nullo per mapping esclusi. Nessun aggiornamento di RAW, mapping, database operativo, GUI, CSS o pubblicazione. Test su copia e installazione conservativa versionata. Prossimo step motore Top & Flop + JS nativi, usando gli altri publisher esistenti.

- Conteggi aggiornati: 559675 Formazione nell’SQLite con 2026/27 importato (contro 552025 nel precedente snapshot).


## 6 novembre 2026 — Exporter compatto JS Top & Flop v2

- Esaminato sorgente exporter v1 installato e copie aggiornate FCM/FCA/SQLite.
- Implementato exporter Java **additivo** `TopFlopCompactStageJsExporter` e decoder browser v2, senza sovrascrivere la v1 o modificare CSS, GUI e pipeline.
- Test Java di shard e blocco oversized; Python verifica limiti, cardinalità e ricostruzione semantica; Node.js testa decoder browser.
- Due stagioni campione 2006/07 e 2026/27: 1.419 calciatori, 53.922 giornate, 13.700 contesti: 12+ MB v1 -> 2.214.884 byte v2 e confronto semantico completo.
- 21 stagioni: 16.262 calciatori, 617.956 giornate, 554.425 contesti, 546.101 scorecard-link nei contesti, 106 JS per 59.898.532 byte; max 949.971.
- Anche le giornate senza voto restano esplicite e i dettagli non vengono caricati tutti in apertura.
- Nessun sito aggiornato; esecuzione Windows e pubblicazione multisito ancora da completare.

---

## 2026-11-06 — Implementazione viste TF01–TF20

Nuova classe Java stage-only, decoder JS, test portiere sintetico, validatore Python, test Node browser; export su 21 stagioni completo (16.262 schede, 45.787 aggregati, 540 gruppi record, 1.081 highlights), 85 JS = 5.355.536 byte; max 208.689 byte. Verificata riconciliazione con SQLite, ambiguita esclusioni, ruolo portieri, link canonici e pareggi. Restano da installare con runner versionato e integrare nella pipeline/pubblicazione 3.2.
~~~~
