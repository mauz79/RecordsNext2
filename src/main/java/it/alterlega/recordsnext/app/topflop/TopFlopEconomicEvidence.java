package it.alterlega.recordsnext.app.topflop;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Faithful FCM tesserato observations: contracts, NOT market prices or dated possession. */
public final class TopFlopEconomicEvidence {
    private TopFlopEconomicEvidence() {}
    public record Budget(BigDecimal initial,BigDecimal total) {}
    public record Result(List<Map<String,Object>> contracts,Map<String,Object> summary) {}

    /** Actual archived league settings, exactly one row per season. */
    public static Budget readBudget(Connection db,String season) throws SQLException {
        if(season==null || !season.matches("[0-9]{4}_[0-9]{4}"))
            throw new IllegalArgumentException("Invalid season: "+season);
        String sql="SELECT CREDITIINIZIALI, CREDITITOTALI FROM \"raw_"+season+"_fcm_lega\"";
        try(PreparedStatement ps=db.prepareStatement(sql);ResultSet rs=ps.executeQuery()) {
            if(!rs.next())throw new SQLException("FCM lega mancante: "+season);
            BigDecimal initial=nonnegative(rs.getString(1),"CREDITIINIZIALI");
            BigDecimal total=nonnegative(rs.getString(2),"CREDITITOTALI");
            if(rs.next())throw new SQLException("Più di una lega FCM: "+season);
            return new Budget(initial,total);
        }
    }
    /** Null is missing, zero is a recorded zero, strictly never coerce missing to zero. */
    static BigDecimal nonnegative(String raw,String field) {
        if(raw==null || raw.startsWith("__SQL_NULL__"))return null;
        if(!raw.matches("[0-9]+(?:[,.][0-9]+)?"))
            throw new IllegalArgumentException("Prezzo/credito non numerico " + field + ": " + raw);
        return new BigDecimal(raw.replace(',','.'));
    }
    private static String state(BigDecimal n) {return n==null?"MANCANTE":n.signum()==0?"ZERO_REGISTRATO":"POSITIVO_REGISTRATO";}
    /** No synthetic purchase date, no implicit annual budget normalization. */
    public static Result collect(List<TopFlopFcmJdbcReader.RosterLine> roster,Budget budget) {
        Set<Long> seen=new HashSet<>();
        List<Map<String,Object>> result=new ArrayList<>();
        int purchases=0,zero=0,missing=0,outliers=0,sales=0,salesZero=0,salesMissing=0;
        BigDecimal sum=BigDecimal.ZERO,min=null,max=null,saleSum=BigDecimal.ZERO;
        for(var contract:roster) {
            if(!seen.add(contract.tesseratoId()))throw new IllegalArgumentException("Tesserato duplicato: "+contract.tesseratoId());
            Map<String,String> raw=contract.sourceFields();
            BigDecimal cost=nonnegative(raw.get("boughtFor"),"COSTOACQ");
            BigDecimal sale=nonnegative(raw.get("soldFor"),"COSTOCESS");
            if(cost==null)missing++;
            else if(cost.signum()==0)zero++;
            else {
                purchases++;sum=sum.add(cost);
                if(min==null||cost.compareTo(min)<0)min=cost;
                if(max==null||cost.compareTo(max)>0)max=cost;
            }
            if(sale==null)salesMissing++;
            else if(sale.signum()==0)salesZero++;
            else {sales++;saleSum=saleSum.add(sale);}
            boolean outlier=cost!=null && budget.total()!=null
                && budget.total().signum()>0 && cost.compareTo(budget.total())>0;
            if(outlier)outliers++;
            Map<String,Object> row=new LinkedHashMap<>();
            row.put("stagione",contract.player().seasonId());
            row.put("playerKey",contract.player().seasonId()+":"+contract.player().fcaPlayerId());
            row.put("idTesserato",contract.tesseratoId());
            row.put("idSquadraInterno",contract.fantasyTeamId());
            row.put("costoAcquisto",cost);row.put("statoAcquisto",state(cost));
            row.put("costoCessione",sale);row.put("statoCessione",state(sale));
            row.put("vendutoRegistrato",raw.get("sold"));
            row.put("annoContrattoRegistrato",raw.get("contractYear"));
            row.put("acquistoSuperaCreditiTotali",outlier);
            row.put("indiceEconomico",null);
            row.put("confrontabilita","BUDGET_E_REGOLAMENTO_NON_CERTIFICATI");
            row.put("origine","FCM.TESSERATO");
            result.add(row);
        }
        result.sort(Comparator.comparing((Map<String,Object> x)->String.valueOf(x.get("playerKey")))
            .thenComparingLong(x->((Number)x.get("idTesserato")).longValue()));
        Map<String,Object> summary=new LinkedHashMap<>();
        summary.put("creditiIniziali",budget.initial());
        summary.put("creditiTotali",budget.total());
        summary.put("contratti",result.size());
        summary.put("acquistiPositivi",purchases);
        summary.put("acquistiZero",zero);
        summary.put("acquistiMancanti",missing);
        summary.put("acquistiMinimo",min);
        summary.put("acquistiMassimo",max);
        summary.put("acquistiMedia",purchases==0?null:sum.divide(BigDecimal.valueOf(purchases),MathContext.DECIMAL64));
        summary.put("acquistiSuperanoCreditiTotali",outliers);
        summary.put("cessioniPositive",sales);
        summary.put("cessioniZero",salesZero);
        summary.put("cessioniMancanti",salesMissing);
        summary.put("cessioniSommaPositive",saleSum);
        summary.put("statoConfrontabilita","SOLO_PREZZI_REGISTRATI_NON_NORMALIZZATI");
        summary.put("indiceEconomico",null);
        summary.put("costoNonDimostraPossessoAllaGiornata",true);
        return new Result(List.copyOf(result),summary);
    }
}
