package it.alterlega.recordsnext.app.topflop;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.*;
import java.math.BigDecimal;
import java.util.Map;

public final class TopFlopTribunaAuditV49SmokeTest {
    public static void main(String[] args) {
        TopFlopArchive a=new TopFlopArchive();
        PlayerSeason x=new PlayerSeason("2023_2024",1,111);
        PlayerSeason y=new PlayerSeason("2023_2024",2,222);
        PlayerSeason z=new PlayerSeason("2023_2024",3,333);
        for(PlayerSeason p:new PlayerSeason[]{x,y,z}) {
            a.addRosterSnapshot(new RosterSnapshot(p,17,Map.of()));
            a.addRealDay(new RealDay(new RealDayKey(p,7),VoteState.VALID,
                BigDecimal.valueOf(7),Map.of(),Map.of()));
        }
        a.addFantasyFact(new FantasyFact(new FantasyKey(x,1,10,17),new RealDayKey(x,7),
            "Serie A",CompetitionFamily.CAMPIONATO,7,true,Usage.COUNTED,
            BigDecimal.valueOf(7),Map.of(),null,Map.of("FCM.Incontro.Giocato","1")));
        a.addFantasyFact(new FantasyFact(new FantasyKey(y,2,11,17),new RealDayKey(y,7),
            "Coppa di Lega",CompetitionFamily.COPPE,7,true,Usage.COUNTED,
            BigDecimal.valueOf(7),Map.of(),null,Map.of("FCM.Incontro.Giocato","1")));
        var r=TopFlopTribunaAuditV49.analyze(a);
        check(r,"tribunaPresenteAltraFormazioneStessoTurno",2L);
        check(r,"tribunaPresenteAltraFormazioneStessoTurnoConVoto",2L);
        check(r,"tribunaSoloSnapshotTesserati",2L);
        check(r,"tribunaSoloSnapshotTesseratiConVoto",2L);
        check(r,"incontriFantasquadraGiocatiOsservati",2);
        if(!"DIAGNOSTICA_NON_CLASSIFICA_TF".equals(r.get("stato")))throw new AssertionError(r);
        System.out.println("RN4_V49_TRIBUNA_AUDIT_OK sameRound=2 snapshotOnly=2 noFakeTF");
    }
    private static void check(Map<String,Object> v,String name,Object expected) {
        if(!expected.equals(v.get(name)))throw new AssertionError(name+": "+v.get(name));
    }
}
