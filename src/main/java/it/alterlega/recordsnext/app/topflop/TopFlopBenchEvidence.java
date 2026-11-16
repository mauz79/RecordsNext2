package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.topflop.TopFlopFacts.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * TF11–TF13: ONLY the proven, submitted but uncounted bench of played FCM matches.
 * A tesserato season snapshot does not prove ownership on any day. Players absent
 * from the submitted lineup cannot enter the denominator; no absence is inferred.
 * FCM TOT is used only when already numeric IN THAT bench tabellino slot.
 */
public final class TopFlopBenchEvidence {
    private TopFlopBenchEvidence() {}
    public static final String COVERAGE = "SOLO_RISERVE_PRESENTI_IN_FORMAZIONE_FCM_NON_CONTEGGIATE";
    private static final MathContext MC = MathContext.DECIMAL64;

    private record Key(String playerKey, int playerId, int teamId, int competitionId) implements Comparable<Key> {
        @Override public int compareTo(Key x) {
            int c=playerKey.compareTo(x.playerKey);
            if(c!=0)return c;
            c=Integer.compare(teamId,x.teamId);
            return c!=0?c:Integer.compare(competitionId,x.competitionId);
        }
    }
    private static final class Group {
        final Key key;
        final String playerName, teamName, competition, family;
        final Integer gazzaCode;
        final int role;
        int exclusions, withVote, withFantasy;
        BigDecimal realSum=BigDecimal.ZERO, fantasySum=BigDecimal.ZERO;
        final List<Map<String,Object>> observations=new ArrayList<>();
        Group(Key key, TopFlopFcaJdbcReader.PlayerProfile profile, FantasyFact fact) {
            this.key=key;
            playerName=profile.name();gazzaCode=profile.player().gazzaCode();
            teamName=fact.sourceFields().getOrDefault("FCM.Fantasquadra.Nome","");
            competition=fact.competitionName();family=fact.family().name();
            int parsed;
            try{parsed=Integer.parseInt(profile.originalFields().getOrDefault("GiocatoreA.RUOLO","-1"));}
            catch(NumberFormatException ex){parsed=-1;}
            role=parsed;
        }
        void add(FantasyFact fact, RealDay day, String url) {
            exclusions++;
            if(day.voteState()==VoteState.VALID){withVote++;realSum=realSum.add(day.vote());}
            BigDecimal total=fact.fantasyVote();
            // Do not represent an FCM numeric total without valid real vote as comparable fantasy evidence.
            if(day.voteState()==VoteState.VALID && total!=null){withFantasy++;fantasySum=fantasySum.add(total);}
            observations.add(obj("idIncontro",fact.key().matchId(),
                "giornataSerieA",fact.realDayKey().serieARound(),
                "giornataCompetizione",fact.fantasyRound(),
                "posizioneRiserva",position(fact),
                "votoReale",day.vote(),
                "fantavotoTabellino",day.voteState()==VoteState.VALID?total:null,
                "urlTabellino",url));
        }
        String highlightKey(){return "BENCH:"+key.playerKey+":"+key.teamId+":"+key.competitionId;}
        Map<String,Object> player(){return obj("playerKey",key.playerKey,"nome",playerName,
            "codiceStorico",gazzaCode,"ruoloFca",role);}
        Map<String,Object> row(){return obj("calciatore",player(),"playerKey",key.playerKey,
            "idSquadraInterno",key.teamId,"fantasquadra",teamName,
            "idCompetizioneInterno",key.competitionId,"competizione",competition,
            "famiglia",family,"esclusioniCerte",exclusions,
            "esclusioniConVotoReale",withVote,"sommaVotiReali",realSum,
            "mediaVotoReale",avg(realSum,withVote),
            "esclusioniConFantavotoTabellino",withFantasy,
            "sommaFantavotiTabellino",fantasySum,
            "fantamediaTabellino",avg(fantasySum,withFantasy),
            "errorePesatoVoto",avg(realSum,exclusions),
            "errorePesatoFantavoto",avg(fantasySum,exclusions),
            "copertura",COVERAGE,
            "completaPossessoStorico",false);
        }
        Map<String,Object> entry() {
            return obj("calciatore",player(),"fantasquadra",teamName,
                "competizione",competition,"famiglia",family,
                "esclusioniCerte",exclusions,"esclusioniConVotoReale",withVote,
                "esclusioniConFantavotoTabellino",withFantasy,
                "sommaVotiReali",realSum,"sommaFantavotiTabellino",fantasySum,
                "copertura",COVERAGE,"highlightKey",highlightKey());
        }
        Map<String,Object> highlight() {return obj("key",highlightKey(),
            "tipo","panchinaDocumentataNonUtilizzata","calciatore",player(),
            "fantasquadra",teamName,"competizione",competition,"famiglia",family,
            "esclusioniCerte",exclusions,"copertura",COVERAGE,
            "evidenze",observations);
        }
    }
    private static final class Winner {
        final String id, ambito, metric;
        BigDecimal score;
        final List<Map<String,Object>> entries=new ArrayList<>();
        Winner(String id,String ambito,String metric){this.id=id;this.ambito=ambito;this.metric=metric;}
        void offer(BigDecimal value,Group candidate) {
            if(value==null)return;
            int comparison=score==null?1:value.compareTo(score);
            if(score==null || comparison>0){score=value;entries.clear();}
            if(comparison>=0)entries.add(candidate.entry());
        }
        Map<String,Object> row(){return obj("recordId",id,"ambito",ambito,
            "metrica",metric,"ordine","MAX","valore",score,
            "pariMerito",entries.size(),"risultati",entries,
            "copertura",COVERAGE,"classificaCompletaRosa",false);}
    }

    public record Result(List<Map<String,Object>> aggregates,
                         List<Map<String,Object>> records,
                         List<Map<String,Object>> highlights,
                         Map<String,Object> counters) {}

    public static Result analyze(TopFlopJdbcArchiveAssembler.LoadedSeason loaded,
             Map<TopFlopCanonicalScorecards.MatchTeam,String> urls,
             TopFlopSettings settings) {
        Objects.requireNonNull(loaded);Objects.requireNonNull(urls);Objects.requireNonNull(settings);
        TopFlopArchive archive=loaded.archive();
        Map<Key,Group> groups=new TreeMap<>();
        int eligible=0,rejectedBench=0,outsideList=0;
        for(FantasyFact fact:archive.allFantasyFacts()) {
            if(fact.usage()==Usage.OUTSIDE_LIST)outsideList++;
            if(fact.usage()!=Usage.BENCH_UNUSED)continue;
            if(!verifiedUnusedBench(fact)){rejectedBench++;continue;}
            RealDay day=archive.realDay(fact.realDayKey());
            if(day==null)throw new IllegalArgumentException("Real day missing from verified bench context");
            var profile=loaded.profiles().get(fact.key().player().fcaPlayerId());
            if(profile==null)throw new IllegalArgumentException("Player profile missing for bench context");
            Key key=new Key(fact.key().player().seasonId()+":"+fact.key().player().fcaPlayerId(),
                fact.key().player().fcaPlayerId(),fact.key().fantasyTeamId(),fact.key().sourceCompetitionId());
            Group group=groups.computeIfAbsent(key,k->new Group(k,profile,fact));
            if(!group.competition.equals(fact.competitionName()) ||
               !group.family.equals(fact.family().name()) ||
               !group.teamName.equals(fact.sourceFields().getOrDefault("FCM.Fantasquadra.Nome","")))
                throw new IllegalArgumentException("Conflicting team or competition in bench evidence: "+key);
            group.add(fact,day,urls.get(new TopFlopCanonicalScorecards.MatchTeam(
                fact.key().matchId(),fact.key().fantasyTeamId())));
            eligible++;
        }
        List<Map<String,Object>> aggregates=new ArrayList<>();
        Map<String,Winner> winners=new LinkedHashMap<>();
        for(Group g:groups.values()) {
            if(g.exclusions<settings.verifiedExclusions())continue;
            aggregates.add(g.row());
            String scope="panchinaDocumentata"+g.family;
            if(g.withVote>=settings.exclusionsWithVote())
                offer(winners,"TF11",scope,"mediaVotoRealeNonUtilizzato",avg(g.realSum,g.withVote),g);
            if(g.withVote>0)
                offer(winners,"TF12",scope,"errorePesatoVotoReale",avg(g.realSum,g.exclusions),g);
            if(g.withFantasy>=settings.exclusionsWithVote()) {
                offer(winners,"TF13",scope,"fantamediaTabellinoNonUtilizzata",avg(g.fantasySum,g.withFantasy),g);
                offer(winners,"TF13",scope,"errorePesatoFantavotoTabellino",avg(g.fantasySum,g.exclusions),g);
            }
        }
        List<Map<String,Object>> records=new ArrayList<>();
        Map<String,Group> byHighlight=new LinkedHashMap<>();
        for(Group g:groups.values())byHighlight.put(g.highlightKey(),g);
        Map<String,Map<String,Object>> highlights=new LinkedHashMap<>();
        for(Winner w:winners.values()) {
            records.add(w.row());
            for(Map<String,Object> entry:w.entries) {
                String key=(String)entry.get("highlightKey");
                highlights.putIfAbsent(key,byHighlight.get(key).highlight());
            }
        }
        Map<String,Object> counters=obj("coperturaTF11_TF13",COVERAGE,
            "opportunitaPanchinaDocumentata",eligible,
            "panchineSenzaProvaSufficiente",rejectedBench,
            "fuoriListaNonCandidabili",outsideList,
            "gruppiPanchina",groups.size(),"gruppiConSoglia",aggregates.size(),
            "recordGruppiTF11_TF13",records.size());
        return new Result(List.copyOf(aggregates),List.copyOf(records),
            List.copyOf(highlights.values()),Map.copyOf(counters));
    }

    /** All predicates must be proved by the same played FCM fixture. */
    static boolean verifiedUnusedBench(FantasyFact f) {
        if(f.usage()!=Usage.BENCH_UNUSED || f.initialStarter())return false;
        Map<String,String> s=f.sourceFields();
        return "1".equals(s.get("FCM.Incontro.Giocato")) &&
            positive(s.get("FCM.Formazione.Pos")) &&
            greaterThanEleven(s.get("FCM.Tabellino.Slot")) &&
            "0".equals(s.get("FCM.Formazione.Entrato")) &&
            "0".equals(s.get("FCM.Formazione.EntratoRiserva"));
    }
    private static boolean positive(String n){try{return Integer.parseInt(n)>0;}catch(RuntimeException e){return false;}}
    private static boolean greaterThanEleven(String n){try{return Integer.parseInt(n)>11;}catch(RuntimeException e){return false;}}
    private static int position(FantasyFact f){return Integer.parseInt(f.sourceFields().get("FCM.Formazione.Pos"));}
    private static BigDecimal avg(BigDecimal value,int n) {return n==0?null:value.divide(BigDecimal.valueOf(n),MC);}
    private static void offer(Map<String,Winner> map,String id,String scope,String metric,BigDecimal value,Group group) {
        String key=id+":"+scope+":"+metric;
        map.computeIfAbsent(key,k->new Winner(id,scope,metric)).offer(value,group);
    }
    private static Map<String,Object> obj(Object... pairs) {
        Map<String,Object> out=new LinkedHashMap<>();
        for(int i=0;i<pairs.length;i+=2)out.put((String)pairs[i],pairs[i+1]);
        return out;
    }
}
