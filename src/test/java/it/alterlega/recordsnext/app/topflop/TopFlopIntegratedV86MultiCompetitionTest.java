package it.alterlega.recordsnext.app.topflop;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.lang.reflect.Method;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class TopFlopIntegratedV86MultiCompetitionTest {
    @Test void sameMatchIdButDifferentCompetitionsBothCount() throws Exception {
        var player=new PlayerSeason("2026_2027",136,12345);
        var day=new RealDayKey(player,1);
        var archive=new TopFlopArchive();
        archive.addRealDay(new RealDay(day,VoteState.VALID,new BigDecimal("6"),
            Map.of("ASSIST",BigDecimal.ONE),Map.of()));
        for(int comp: new int[]{1,2}) archive.addFantasyFact(new FantasyFact(
            new FantasyKey(player,comp,50,10),day,
            comp==1?"Serie A":"Coppa di Lega",
            comp==1?CompetitionFamily.CAMPIONATO:CompetitionFamily.COPPE,
            1,true,Usage.COUNTED,new BigDecimal("7"),Map.of("ASSIST",BigDecimal.ONE),null,
            Map.of("FCM.Tabellino.Voto","6","FCM.Tabellino.Modif","1")));
        Method method=TopFlopIntegratedV83.class.getDeclaredMethod("points",String.class,TopFlopArchive.class,Set.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Map<String,Object>> rows=(List<Map<String,Object>>)method.invoke(null,"2026_2027",archive,Set.of());
        assertEquals(2,rows.size());
        assertEquals(2,rows.stream().map(x->x.get("idCompetizioneInterno")).distinct().count());
        for(var row:rows){assertEquals(1,row.get("gare"));assertEquals(BigDecimal.ONE,
            ((BigDecimal)row.get("saldoPunti")).stripTrailingZeros());}
    }
}
