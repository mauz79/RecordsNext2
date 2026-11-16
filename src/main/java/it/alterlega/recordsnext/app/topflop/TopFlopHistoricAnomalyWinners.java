package it.alterlega.recordsnext.app.topflop;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Historical rankings, scoped to the site's cutoff and explicitly approved identities. */
public final class TopFlopHistoricAnomalyWinners {
    private TopFlopHistoricAnomalyWinners() {}
    @SuppressWarnings("unchecked")
    public static List<Map<String,Object>> winners(List<Map<String,Object>> careers,Set<String> selected) {
        List<Map<String,Object>> output=new ArrayList<>();
        for(String id:List.of("TF14","TF15")) {
            if(!selected.contains(id))continue;
            BigDecimal best=null;
            List<Map<String,Object>> ties=new ArrayList<>();
            for(Map<String,Object> career:careers) {
                for(Map<String,Object> raw:(List<Map<String,Object>>)career.get("anomalieStagionali")) {
                    if(!id.equals(raw.get("recordId")))continue;
                    BigDecimal delta=(BigDecimal)raw.get("delta");
                    int cmp=best==null?0:delta.compareTo(best);
                    boolean better=best==null || (id.equals("TF14")?cmp>0:cmp<0);
                    if(better){best=delta;ties.clear();}
                    if(better || delta.compareTo(best)==0) {
                        Map<String,Object> entry=new LinkedHashMap<>(raw);
                        entry.put("playerIdentityId",career.get("playerIdentityId"));
                        ties.add(entry);
                    }
                }
            }
            if(best!=null)output.add(Map.of("recordId",id,"metrica","deltaMediaVotoReale",
                "ordine",id.equals("TF14")?"MAX":"MIN","valore",best,
                "pariMerito",ties.size(),"risultati",List.copyOf(ties)));
        }
        return List.copyOf(output);
    }
}
