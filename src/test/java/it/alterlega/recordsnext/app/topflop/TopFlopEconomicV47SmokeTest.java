package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.topflop.TopFlopFacts.PlayerSeason;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public final class TopFlopEconomicV47SmokeTest {
    private static void ok(boolean x,String why){if(!x)throw new AssertionError(why);}
    private static TopFlopFcmJdbcReader.RosterLine r(long id,String cost,String sale) {
        var fields=new java.util.LinkedHashMap<String,String>();
        if(cost!=null)fields.put("boughtFor",cost);
        if(sale!=null)fields.put("soldFor",sale);
        fields.put("contractYear","1");
        return new TopFlopFcmJdbcReader.RosterLine(new PlayerSeason("2023_2024",100,null),8,id,fields);
    }
    public static void main(String[] args) {
        var records=List.of(r(1,null,null),r(2,"0","0"),r(3,"100","30"),
            r(4,"406","0"),r(5,"30","0"));
        var budget=new TopFlopEconomicEvidence.Budget(new BigDecimal("260"),new BigDecimal("320"));
        var audit=TopFlopEconomicEvidence.collect(records,budget);
        var m=audit.summary();
        ok(m.get("contratti").equals(5),"contracts");
        ok(m.get("acquistiMancanti").equals(1) && m.get("acquistiZero").equals(1),"zero and null distinct");
        ok(m.get("acquistiPositivi").equals(3),"only positive purchases in mean");
        ok(m.get("acquistiSuperanoCreditiTotali").equals(1),"406 outlier preserved");
        ok(((BigDecimal)m.get("acquistiMassimo")).compareTo(new BigDecimal("406"))==0,"max cost");
        ok(m.get("cessioniPositive").equals(1),"sale recorded");
        ok(audit.contracts().get(0).get("indiceEconomico")==null,"unapproved index must stay null");
        ok(TopFlopEconomicEvidence.nonnegative(null,"x")==null,"missing no zero coercion");
        boolean duplicated=false;
        try{TopFlopEconomicEvidence.collect(List.of(records.get(0),records.get(0)),budget);}
        catch(IllegalArgumentException e){duplicated=true;}
        ok(duplicated,"contract IDs unique");
        System.out.println("RN4_V47_ECONOMY_OK contracts=5 zero=1 missing=1 outlier406 retained no-fake-index");
    }
}
