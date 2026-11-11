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
        int unusedRealVotesValid, unusedFantasyTotalsValid;
        BigDecimal unusedVotes=BigDecimal.ZERO, unusedFantasy=BigDecimal.ZERO;
        final Map<String,BigDecimal> unusedEvents=new LinkedHashMap<>();
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
                "utilizzoDaVerificare",unknown,"eventiApportati",events,
                "nonUtilizzatoOsservato",obj(
                    "gareConVotoReale",unusedRealVotesValid,
                    "sommaVotiReali",unusedVotes,
                    "mediaVotoReale",mean(unusedVotes,unusedRealVotesValid),
                    "gareConFantavotoNumerico",unusedFantasyTotalsValid,
                    "sommaFantavotiTabellino",unusedFantasy,
                    "fantamediaTabellino",mean(unusedFantasy,unusedFantasyTotalsValid),
                    "eventiRealiNonApportati",unusedEvents,
                    "origine","FCM_FORMAZIONE_NON_CONTEGGIATA_E_VOTO_FCA"));
        }
    }
    static final class Costs {
        int n;
        BigDecimal sum=BigDecimal.ZERO,min,max;
        void add(String raw) {
            if(raw==null || !raw.matches("[0-9]+(?:[,.][0-9]+)?"))return;
            BigDecimal value=new BigDecimal(raw.replace(',','.'));
            n++;sum=sum.add(value);
            if(min==null || value.compareTo(min)<0)min=value;
            if(max==null || value.compareTo(max)>0)max=value;
        }
        Map<String,Object> view() {
            return obj("registrazioniConCosto",n,"minimo",min,"massimo",max,
                "medio",n==0?null:mean(sum,n),"origine","FCM.Tesserato.COSTOACQ");
        }
    }
    static final class Player {
        final TopFlopFcaJdbcReader.PlayerProfile profile;
        final Totals real = new Totals(), fantasy = new Totals();
        final Costs costs = new Costs();
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
        final Costs costs = new Costs();
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
                "famiglia",family,"apporto",totals.fantasyView(),
                "costoAcquisto",costs.view()));
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
        return build(loaded,links,TopFlopSettings.defaults());
    }
    static SeasonView build(TopFlopJdbcArchiveAssembler.LoadedSeason loaded,
                     Map<TopFlopCanonicalScorecards.MatchTeam,String> links,TopFlopSettings settings) {
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
            RealDay realForFact=archive.realDay(fact.realDayKey());
            addUsage(p.fantasy,fact,realForFact);addUsage(t.totals,fact,realForFact);
            if(fact.usage()!=Usage.COUNTED)continue;
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
        // Acquisition prices are actual FCM roster-event values, never inferred
        // from votes or price trends; no assumption about ownership dates.
        Map<String,Costs> costsByTeam=new LinkedHashMap<>();
        for(var roster:loaded.rosterEvents()) {
            Player p=players.get(roster.player().fcaPlayerId());
            if(p==null)continue;
            String cost=roster.sourceFields().get("boughtFor");
            p.costs.add(cost);
            costsByTeam.computeIfAbsent(roster.player().fcaPlayerId()+":"+roster.fantasyTeamId(),
                ignored->new Costs()).add(cost);
        }
        for(Team team:teams.values()) {
            Costs registered=costsByTeam.get(team.player.profile.player().fcaPlayerId()+":"+team.teamId);
            if(registered!=null) {
                team.costs.n=registered.n;team.costs.sum=registered.sum;
                team.costs.min=registered.min;team.costs.max=registered.max;
            }
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
            career.put("stagione",p.profile.player().seasonId());
            career.put("reale",p.real.realView());career.put("fantasy",p.fantasy.fantasyView());
            career.put("costoAcquisto",p.costs.view());
            career.put("identitaStoricaVerificata",false); // no auto-merge of CodiceGazza
            out.careers.add(career);
            Map<String,Object> base=obj("calciatore",p.label());
            if(p.real.validVotes>=settings.realSeasonVotes()) {
                offer(winners,"TF05","mediaVotoRealeStagione","mediaVoto","MAX",
                    mean(p.real.votes,p.real.validVotes),
                    obj("calciatore",p.label(),"votiValidi",p.real.validVotes));
                offer(winners,"TF05","mediaVotoRealeStagione","mediaVoto","MIN",
                    mean(p.real.votes,p.real.validVotes),
                    obj("calciatore",p.label(),"votiValidi",p.real.validVotes));
            }
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
        for(Team t:teams.values()) {
            int minimum="CAMPIONATO".equals(t.family)
                ? settings.fantasyLeagueMatches():settings.fantasyCupMatches();
            if(t.totals.counted>=minimum) {
                BigDecimal avg=mean(t.totals.fantasy,t.totals.counted);
                Map<String,Object> entry=obj("calciatore",t.player.label(),
                    "fantasquadra",t.teamName,"competizione",t.competitionName,
                    "famiglia",t.family,"incontriConteggiati",t.totals.counted);
                offer(winners,"TF06","fantamediaApportataPerSquadraECompetizione",
                    "fantamedia","MAX",avg,entry);
                offer(winners,"TF06","fantamediaApportataPerSquadraECompetizione",
                    "fantamedia","MIN",avg,entry);
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

    private static void addUsage(Totals a,FantasyFact f,RealDay real) {
        a.observed++;
        if(f.usage()!=Usage.COUNTED) {
            switch(f.usage()){
                case BENCH_UNUSED -> a.benchUnused++;
                case OUTSIDE_LIST -> a.outsideList++;
                default -> a.unknown++;
            }
            // A substitute not counted, or a documented outside-list slot, can
            // have an actual FCA vote and an actual numeric FCM tabellino total.
            // This is NOT a simulated lineup, nor proof of continuous ownership.
            if(f.usage()==Usage.BENCH_UNUSED || f.usage()==Usage.OUTSIDE_LIST) {
                if(real.voteState()==VoteState.VALID) {
                    a.unusedRealVotesValid++;
                    a.unusedVotes=a.unusedVotes.add(real.vote());
                    for(String event:EVENTS) {
                        BigDecimal v=real.events().get(event);
                        if(v!=null && v.signum()!=0)
                            a.unusedEvents.merge(event,v,BigDecimal::add);
                    }
                }
                if(f.fantasyVote()!=null && real.voteState()==VoteState.VALID) {
                    a.unusedFantasyTotalsValid++;
                    a.unusedFantasy=a.unusedFantasy.add(f.fantasyVote());
                }
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
    private static List<Object> packCost(Object raw) {
        @SuppressWarnings("unchecked") Map<String,Object> c=(Map<String,Object>)raw;
        return List.of(c.get("registrazioniConCosto"),
            c.get("minimo")==null?"":c.get("minimo"),
            c.get("massimo")==null?"":c.get("massimo"),
            c.get("medio")==null?"":c.get("medio"));
    }
    private static List<Object> packUnused(Object raw) {
        @SuppressWarnings("unchecked") Map<String,Object> u=(Map<String,Object>)raw;
        return List.of(u.get("gareConVotoReale"),u.get("sommaVotiReali"),
            u.get("gareConFantavotoNumerico"),u.get("sommaFantavotiTabellino"),
            packEvents(u.get("eventiRealiNonApportati")));
    }
    private static List<Object> packFantasy(Map<String,Object> f) {
        return List.of(f.get("contestiOsservati"),f.get("conteggiati"),f.get("conVotoRealeValido"),
            f.get("sommaVotiRealiApportati"),f.get("sommaFantavotiApportati"),
            f.get("panchinaNonConteggiata"),f.get("fuoriLista"),f.get("utilizzoDaVerificare"),
            packEvents(f.get("eventiApportati")),packUnused(f.get("nonUtilizzatoOsservato")));
    }
    @SuppressWarnings("unchecked")
    private static List<Object> packCareer(Map<String,Object> row) {
        Map<String,Object> r=(Map<String,Object>)row.get("reale");
        Map<String,Object> f=(Map<String,Object>)row.get("fantasy");
        return List.of(row.get("playerKey"), row.get("nome"),row.get("ruoloFca"),
            row.get("codiceStorico")==null?0:row.get("codiceStorico"),
            List.of(r.get("votiValidi"),r.get("sommaVoti"),packEvents(r.get("eventi")),
                r.get("giornateGolSubitiConVoto"),r.get("giornateZeroGolSubitiRegistrati"),
                r.get("mediaGolSubitiSuGiornateVerificate")==null?"":r.get("mediaGolSubitiSuGiornateVerificate")),
            packFantasy(f),packCost(row.get("costoAcquisto")));
    }
    @SuppressWarnings("unchecked")
    private static List<Object> packTeam(Map<String,Object> row) {
        Map<String,Object> f=(Map<String,Object>)row.get("apporto");
        return List.of(row.get("playerKey"),row.get("idSquadraInterno"),
            row.get("idCompetizioneInterno"),packFantasy(f),packCost(row.get("costoAcquisto")));
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
        return export(c, seasonIds, output, reports, Set.of());
    }

    /**
     * selectedIds: catalogo TF01..TF20. In questo overload un insieme vuoto
     * conserva il comportamento v1 per la diagnostica; la pipeline risolve e
     * valida sempre una selezione esplicita non vuota.
     */
    public static Result export(Connection c,List<String> seasonIds,Path output,Path reports,
                                Set<String> selectedIds) throws SQLException,IOException {
        return export(c,seasonIds,output,reports,selectedIds,TopFlopSettings.defaults());
    }

    public static Result export(Connection c,List<String> seasonIds,Path output,Path reports,
                                Set<String> selectedIds,TopFlopSettings settings) throws SQLException,IOException {
        Objects.requireNonNull(settings);
        Objects.requireNonNull(c);Objects.requireNonNull(seasonIds);Objects.requireNonNull(output);
        Objects.requireNonNull(selectedIds);
        for (String id : selectedIds) if (!id.matches("TF(?:0[1-9]|1[0-9]|20)"))
            throw new IllegalArgumentException("Identificativo record sconosciuto: " + id);
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
            SeasonView view=build(loaded,canonical,settings);
            if (!selectedIds.isEmpty()) filterSelected(view, selectedIds);
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
        status.put("TF05","ATTIVA_SOGLIA_MEDIA_VOTO_STAGIONALE");
        status.put("TF06","ATTIVA_SOGLIA_FANTAMEDIA_PER_SQUADRA_E_COMPETIZIONE");
        for(String id:List.of("TF11","TF12","TF13"))status.put(id,"NON_ATTIVA_ESCLUSIONI_NON_VERIFICATE");
        for(String id:List.of("TF14","TF15"))status.put(id,"NON_ATTIVA_BASELINE_E_SOGLIE_DA_VALIDARE");
        status.put("TF16","CONFRONTO_PER_SQUADRA_SENZA_VINCITORE_ARBITRARIO");
        if (!selectedIds.isEmpty())
            for (String id : new ArrayList<>(status.keySet()))
                if (!selectedIds.contains(id)) status.put(id, "NON_SELEZIONATO");
        Map<String,Object> facade=obj("schemaVersion","4.0-public-views-v1",
            "catalogo","TF01-TF20","status","STAGING_ONLY_NOT_PUBLISHED",
            "limitBytesPerJs",LIMIT_BYTES,"identity","SEASON_LOCAL_NO_AUTO_MERGE",
            "viste",List.of("Careers","Teams","Records","Highlights"),"eventi",EVENTS,
            "soglie",obj("votiMinimiStagione",settings.realSeasonVotes(),
                "gareMinimeCampionato",settings.fantasyLeagueMatches(),
                "gareMinimeCoppe",settings.fantasyCupMatches()),
            "attivazioneRecord",status,"stagioni",metadata);
        byte[] b=("window.fcmRecordsNextTopFlopViews = "+json(facade)+";\n")
            .getBytes(StandardCharsets.UTF_8);
        files.add(write(output,FACADE,"","Facade",1,b));
        return new Result(List.copyOf(files),seasonIds.size(),players,real,fantasy,output);
    }
    @SuppressWarnings("unchecked")
    static void filterSelected(SeasonView view, Set<String> ids) {
        view.records.removeIf(record -> !ids.contains((String)record.get("recordId")));
        Set<String> highlightKeys = new LinkedHashSet<>();
        for (Map<String,Object> record : view.records) {
            for (Map<String,Object> result : (List<Map<String,Object>>)record.get("risultati")) {
                Object key = result.get("highlightKey");
                if (key != null) highlightKeys.add(key.toString());
            }
        }
        // Il portiere goleador merita un highlight anche se non ha un voto estremo.
        if (ids.contains("TF20")) {
            for (Map<String,Object> highlight : view.highlights) {
                Map<String,Object> player=(Map<String,Object>)highlight.get("calciatore");
                if(player == null || !"prestazioneReale".equals(highlight.get("tipo")))continue;
                if(!Integer.valueOf(1).equals(player.get("ruoloFca")))continue;
                Map<String,Object> events=(Map<String,Object>)highlight.get("eventi");
                if(events!=null && (events.containsKey("GOLFATTI1") || events.containsKey("ASSIST")))
                    highlightKeys.add(String.valueOf(highlight.get("key")));
            }
        }
        view.highlights.removeIf(h -> !highlightKeys.contains(h.get("key")));
        view.counters.put("selectedHighlights", view.highlights.size());
        view.counters.put("recordGroups", view.records.size());
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
