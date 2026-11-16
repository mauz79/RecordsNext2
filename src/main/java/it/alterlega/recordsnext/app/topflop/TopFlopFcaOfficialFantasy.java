package it.alterlega.recordsnext.app.topflop;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Official FCA Fantamedia: Statistiche.FMT weighted by Statistiche.PRESENZE.
 * The source is accepted player-by-player ONLY when the official presence count
 * matches the real GiocaIn/Punteggio valid votes. No FCM totals, no guessed
 * bonus coefficients and no reuse of partially outdated Storico summaries.
 *
 * FMT is a rounded season-average from FCA. The derived sum (FMT * presences)
 * is a weighted approximation, NOT a reconstructed sum of match fantasy votes.
 */
public final class TopFlopFcaOfficialFantasy {
    private TopFlopFcaOfficialFantasy() {}

    static Map<String,Object> verifiedRow(String season, int playerId, int validVotes,
                                           Integer officialPresences, BigDecimal fmt) {
        if (validVotes <= 0 || officialPresences == null || officialPresences != validVotes ||
            fmt == null || fmt.signum() <= 0) return null;
        Map<String,Object> row = new LinkedHashMap<>();
        row.put("playerKey", season + ":" + playerId);
        row.put("votiRealiValidi", validVotes);
        row.put("sommaFantavotiReali", fmt.multiply(BigDecimal.valueOf(validVotes)));
        row.put("fantamediaReale", fmt);
        row.put("regolamentoFonte", "FCA.Statistiche.FMT");
        row.put("stato", "FCA_UFFICIALE_PRESENZE_VERIFICATE");
        row.put("precisione", "FMT_STAGIONALE_ARROTONDATA_ORIGINALE");
        return row;
    }

    public static TopFlopRealFantasyRules.Result extract(Connection connection, String season,
            TopFlopJdbcArchiveAssembler.LoadedSeason loaded, TopFlopRealFantasyRules.Rule verifiedRule)
            throws SQLException {
        if (season == null || !season.matches("[0-9]{4}_[0-9]{4}"))
            throw new IllegalArgumentException("Invalid season " + season);
        String prefix = "raw_" + season + "_fca_";
        Map<Integer,Integer> rawVotes = new HashMap<>();
        String rawSql = "SELECT g.\"IDGIOCATORE\", " +
            "SUM(CASE WHEN p.\"VOTO1\">0 THEN 1 ELSE 0 END) " +
            "FROM \"" + prefix + "giocain\" g " +
            "JOIN \"" + prefix + "punteggio\" p ON p.\"ID\"=g.\"IDPUNTEGGIO\" " +
            "GROUP BY g.\"IDGIOCATORE\"";
        int allValid = 0;
        try (PreparedStatement statement = connection.prepareStatement(rawSql);
             ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                int id = results.getInt(1), count = results.getInt(2);
                rawVotes.put(id, count);
                allValid += count;
            }
        }
        if (allValid != loaded.fca().validVotes())
            throw new SQLException("FCA vote reconciliation failed for " + season +
                ": raw=" + allValid + " reader=" + loaded.fca().validVotes());

        String statsSql = "SELECT a.\"ID\",s.\"PRESENZE\",s.\"FMT\" FROM \"" +
            prefix + "giocatorea\" a LEFT JOIN \"" + prefix +
            "statistiche\" s ON s.\"ID\"=a.\"ID\" ORDER BY a.\"ID\"";
        List<Map<String,Object>> rows = new ArrayList<>();
        Map<String,Map<String,Object>> accepted = new LinkedHashMap<>();
        int withVotes=0, excluded=0, mismatched=0;
        try (PreparedStatement statement = connection.prepareStatement(statsSql);
             ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                int id = results.getInt(1);
                int valid = rawVotes.getOrDefault(id, 0);
                Integer officialN = null;
                int readN = results.getInt(2);
                if (!results.wasNull()) officialN = readN;
                BigDecimal fmt = results.getBigDecimal(3);
                if (valid > 0) {
                    withVotes++;
                    if (officialN != null && !officialN.equals(valid)) mismatched++;
                }
                Map<String,Object> row = verifiedRow(season,id,valid,officialN,fmt);
                if (row == null) {
                    if (valid > 0) excluded++;
                    continue;
                }
                accepted.put((String) row.get("playerKey"), row);
            }
        }
        int manualCount = 0;
        if (verifiedRule != null && excluded > 0) {
            var fallback = TopFlopRealFantasyRules.extract(loaded,verifiedRule);
            for (Map<String,Object> row : fallback.rows()) {
                String key = String.valueOf(row.get("playerKey"));
                if (accepted.containsKey(key)) continue;
                int id;
                try { id = Integer.parseInt(key.substring(key.lastIndexOf(':')+1)); }
                catch (RuntimeException ex) { throw new SQLException("Bad FCA player key: " + key,ex); }
                int count = ((Number) row.get("votiRealiValidi")).intValue();
                if (count != rawVotes.getOrDefault(id,0))
                    throw new SQLException("Unmatched fallback votes: " + key);
                accepted.put(key,row);
                manualCount++;
            }
        }
        rows.addAll(accepted.values());
        rows.sort(Comparator.comparing(r -> String.valueOf(r.get("playerKey"))));
        int coveredVotes=rows.stream().mapToInt(r -> ((Number) r.get("votiRealiValidi")).intValue()).sum();
        Map<String,Object> quality = new LinkedHashMap<>();
        quality.put("status", coveredVotes == allValid ? "DISPONIBILE" : "DATI_PARZIALI");
        quality.put("fonte", "FCA_STATISTICHE_FMT_E_PRESENZE");
        quality.put("tipoSomma", "FMT_ARROTONDATA_MOLTIPLICATA_PER_PRESENZE");
        quality.put("votiValidiFCA", allValid);
        quality.put("votiCopertiFantamedia", coveredVotes);
        quality.put("calciatoriConVoti", withVotes);
        quality.put("calciatoriConFMTUfficialeVerificata", rows.size()-manualCount);
        quality.put("calciatoriConRegolamentoVerificato", manualCount);
        quality.put("discordanzePresenzeFCA", mismatched);
        quality.put("calciatoriSenzaFMTVerificata", withVotes-rows.size());
        quality.put("vietatoUsareFcmTabellinoTot", true);
        return new TopFlopRealFantasyRules.Result(List.copyOf(rows), Map.copyOf(quality));
    }
}
