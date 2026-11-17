package it.alterlega.recordsnext.app.topflop;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class TopFlopIntegratedV83Test {
    @TempDir Path tmp;
    private Object split(Map<String,BigDecimal> events,String delta,Set<String> absent) throws Exception {
        Method method=TopFlopIntegratedV83.class.getDeclaredMethod("split",Map.class,BigDecimal.class,Set.class);
        method.setAccessible(true);return method.invoke(null,events,new BigDecimal(delta),absent);
    }
    private String result(Object value,String accessor) throws Exception {
        Method method=value.getClass().getDeclaredMethod(accessor);method.setAccessible(true);
        Object out=method.invoke(value);return out==null?null:out.toString();
    }
    private Map<String,BigDecimal> event(Object... args) {
        Map<String,BigDecimal> m=new HashMap<>();for(int i=0;i<args.length;i+=2)m.put((String)args[i],new BigDecimal(args[i+1].toString()));return m;
    }
    @Test void fixedGoalsAssistsYellowResolveToThreeAndHalf() throws Exception {
        Object r=split(event("GOLFATTI1",1,"ASSIST",1,"AMM",1),"3.5",Set.of());
        assertEquals("RECONCILED",result(r,"status"));assertEquals("4",result(r,"bonus"));
        assertEquals("0.5",result(r,"malus"));
    }
    @Test void penaltyGoalsAreSubsetAndVariable() throws Exception {
        Object r=split(event("GOLFATTI1",1,"GOLFATTISURIGORE1",1,"AMM",1),"1.5",Set.of());
        assertEquals("RECONCILED",result(r,"status"));assertEquals("2",result(r,"bonus"));
        assertEquals("0.5",result(r,"malus"));
    }
    @Test void ambiguityMustNotInventSeparatePoints() throws Exception {
        Object r=split(event("GOLFATTI1",1,"GOLFATTISURIGORE1",1,"RIGSBA",1),"0",Set.of());
        assertEquals("AMBIGUOUS_SPLIT",result(r,"status"));assertNull(result(r,"bonus"));assertNull(result(r,"malus"));
        Object no=split(event("RIGPAR",1),"3.5",Set.of());
        assertEquals("UNEXPLAINED_MODIF",result(no,"status"));
    }
    @Test void missingEventFieldNotEqualZero() throws Exception {
        Object r=split(event(),"0",Set.of("RIGPAR"));
        assertEquals("MISSING_FIELDS",result(r,"status"));assertNull(result(r,"bonus"));
    }
    @Test void shardLimitAndNoFutureSeasonInScopedManifest() throws Exception {
        Method method=TopFlopIntegratedV83.class.getDeclaredMethod("shards",Path.class,String.class,List.class,String.class);
        method.setAccessible(true);
        List<Object> rows=new ArrayList<>();for(int i=0;i<15000;i++)
            rows.add(List.of("2026_2027:"+i,"identity-"+i,"NAME " + "x".repeat(100),"","", "1"));
        @SuppressWarnings("unchecked") List<Map<String,Object>> shards=(List<Map<String,Object>>)method.invoke(null,tmp,"2026_2027",rows,"Index");
        assertTrue(shards.size()>1);
        assertEquals(15000,shards.stream().mapToInt(x->((Number)x.get("righe")).intValue()).sum());
        for(var item:shards) assertTrue(Files.size(tmp.resolve((String)item.get("nome")))<=1_500_000L);
    }
}
