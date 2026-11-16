package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.topflop.TopFlopFacts.*;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Coverage audit, NOT a TF11-13 record or asserted ownership history.
 * A seasonal Tesserato snapshot does not establish ownership for every fixture;
 * without market timestamps we must preserve the difference between a player
 * also submitted for the same team in the same Serie A round and one merely
 * present in a seasonal roster snapshot.
 */
public final class TopFlopTribunaAuditV49 {
    private TopFlopTribunaAuditV49() {}
    private record Fixture(int teamId,long matchId,int round) {}
    private record TeamRound(int teamId,int round) {}

    public static Map<String,Object> analyze(TopFlopArchive archive) {
        Objects.requireNonNull(archive);
        Map<Fixture,Set<PlayerSeason>> formations=new LinkedHashMap<>();
        Map<TeamRound,Set<PlayerSeason>> seenInSameRound=new LinkedHashMap<>();
        for(FantasyFact fact:archive.allFantasyFacts()) {
            if(!"1".equals(fact.sourceFields().get("FCM.Incontro.Giocato"))) continue;
            int team=fact.key().fantasyTeamId();
            int round=fact.realDayKey().serieARound();
            PlayerSeason player=fact.key().player();
            formations.computeIfAbsent(new Fixture(team,fact.key().matchId(),round),x->new LinkedHashSet<>()).add(player);
            seenInSameRound.computeIfAbsent(new TeamRound(team,round),x->new LinkedHashSet<>()).add(player);
        }
        Map<Integer,Set<PlayerSeason>> seasonRoster=new LinkedHashMap<>();
        for(RosterSnapshot r:archive.allRosters())
            seasonRoster.computeIfAbsent(r.fantasyTeamId(),x->new LinkedHashSet<>()).add(r.player());
        long sameRound=0,snapshotOnly=0,sameRoundWithVote=0,snapshotOnlyWithVote=0;
        for(var fixture:formations.entrySet()) {
            Fixture game=fixture.getKey();
            Set<PlayerSeason> lineup=fixture.getValue();
            Set<PlayerSeason> same=seenInSameRound.getOrDefault(new TeamRound(game.teamId(),game.round()),Set.of());
            for(PlayerSeason player:seasonRoster.getOrDefault(game.teamId(),Set.of())) {
                if(lineup.contains(player))continue;
                RealDay day=archive.realDay(new RealDayKey(player,game.round()));
                // FCA day must exist: without it this is not a measurable opportunity.
                if(day==null)continue;
                if(same.contains(player)) {
                    sameRound++;
                    if(day.voteState()==VoteState.VALID)sameRoundWithVote++;
                } else {
                    snapshotOnly++;
                    if(day.voteState()==VoteState.VALID)snapshotOnlyWithVote++;
                }
            }
        }
        Map<String,Object> result=new LinkedHashMap<>();
        result.put("stato","DIAGNOSTICA_NON_CLASSIFICA_TF");
        result.put("tribunaPresenteAltraFormazioneStessoTurno",sameRound);
        result.put("tribunaPresenteAltraFormazioneStessoTurnoConVoto",sameRoundWithVote);
        result.put("tribunaSoloSnapshotTesserati",snapshotOnly);
        result.put("tribunaSoloSnapshotTesseratiConVoto",snapshotOnlyWithVote);
        result.put("incontriFantasquadraGiocatiOsservati",formations.size());
        result.put("criterio","Non sommabile alle esclusioni certe senza verificare il possesso alla data");
        return Map.copyOf(result);
    }
}
