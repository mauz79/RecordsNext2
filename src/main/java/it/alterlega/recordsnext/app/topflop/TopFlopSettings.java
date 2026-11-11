package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.config.MiniJson;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;

/** Soglie Top & Flop locali, indipendenti dai parametri delle altre famiglie. */
public record TopFlopSettings(
        int realSeasonVotes, int realCareerVotes,
        int fantasyLeagueMatches, int fantasyCupMatches, int fantasyCareerMatches,
        int verifiedExclusions, int exclusionsWithVote,
        int anomalySeasonVotes, int anomalyBaselineVotes, int anomalyBaselineSeasons,
        BigDecimal anomalyDelta) {
    public TopFlopSettings {
        check("realSeasonVotes", realSeasonVotes, 1, 1000);
        check("realCareerVotes", realCareerVotes, 1, 10000);
        check("fantasyLeagueMatches", fantasyLeagueMatches, 1, 1000);
        check("fantasyCupMatches", fantasyCupMatches, 1, 1000);
        check("fantasyCareerMatches", fantasyCareerMatches, 1, 10000);
        check("verifiedExclusions", verifiedExclusions, 1, 1000);
        check("exclusionsWithVote", exclusionsWithVote, 1, 1000);
        check("anomalySeasonVotes", anomalySeasonVotes, 1, 1000);
        check("anomalyBaselineVotes", anomalyBaselineVotes, 1, 10000);
        check("anomalyBaselineSeasons", anomalyBaselineSeasons, 1, 100);
        if (anomalyDelta == null || anomalyDelta.compareTo(BigDecimal.ZERO) <= 0
                || anomalyDelta.compareTo(BigDecimal.TEN) > 0) {
            throw new IllegalArgumentException("anomalyDelta deve essere > 0 e <= 10");
        }
    }
    private static void check(String key, int value, int min, int max) {
        if (value < min || value > max) throw new IllegalArgumentException(key + " fuori intervallo " + min + ".." + max);
    }
    public static TopFlopSettings defaults() {
        return new TopFlopSettings(5,20,5,3,20,3,2,8,20,2,new BigDecimal("0.75"));
    }
    private static int integer(Map<?,?> m, String key, int fallback) {
        Object v=m.get(key);
        if (v==null) return fallback;
        if (!(v instanceof Number n) || n.doubleValue() != n.intValue())
            throw new IllegalArgumentException("Valore intero non valido per " + key + ": " + v);
        return n.intValue();
    }
    public static TopFlopSettings load(Path file) throws IOException {
        if (!Files.isRegularFile(file)) return defaults();
        Object parsed=MiniJson.parse(Files.readString(file,StandardCharsets.UTF_8));
        if (!(parsed instanceof Map<?,?> m)) throw new IOException("Formato topflop.json non valido");
        TopFlopSettings d=defaults();
        Object delta=m.get("anomalyDelta");
        return new TopFlopSettings(integer(m,"realSeasonVotes",d.realSeasonVotes),
            integer(m,"realCareerVotes",d.realCareerVotes),
            integer(m,"fantasyLeagueMatches",d.fantasyLeagueMatches),
            integer(m,"fantasyCupMatches",d.fantasyCupMatches),
            integer(m,"fantasyCareerMatches",d.fantasyCareerMatches),
            integer(m,"verifiedExclusions",d.verifiedExclusions),
            integer(m,"exclusionsWithVote",d.exclusionsWithVote),
            integer(m,"anomalySeasonVotes",d.anomalySeasonVotes),
            integer(m,"anomalyBaselineVotes",d.anomalyBaselineVotes),
            integer(m,"anomalyBaselineSeasons",d.anomalyBaselineSeasons),
            delta==null?d.anomalyDelta:new BigDecimal(delta.toString()));
    }
    public void save(Path file) throws IOException {
        Files.createDirectories(file.toAbsolutePath().normalize().getParent());
        Map<String,Object> settings=new LinkedHashMap<>();
        settings.put("schemaVersion","4.0-topflop-settings-v1");
        settings.put("realSeasonVotes",realSeasonVotes);
        settings.put("realCareerVotes",realCareerVotes);
        settings.put("fantasyLeagueMatches",fantasyLeagueMatches);
        settings.put("fantasyCupMatches",fantasyCupMatches);
        settings.put("fantasyCareerMatches",fantasyCareerMatches);
        settings.put("verifiedExclusions",verifiedExclusions);
        settings.put("exclusionsWithVote",exclusionsWithVote);
        settings.put("anomalySeasonVotes",anomalySeasonVotes);
        settings.put("anomalyBaselineVotes",anomalyBaselineVotes);
        settings.put("anomalyBaselineSeasons",anomalyBaselineSeasons);
        settings.put("anomalyDelta",anomalyDelta);
        StringBuilder out=new StringBuilder("{\n");
        int n=0;
        for (var e:settings.entrySet()) {
            if(n++>0)out.append(",\n");
            out.append("  \"").append(e.getKey()).append("\": ");
            if(e.getValue() instanceof String) out.append('"').append(e.getValue()).append('"');
            else out.append(e.getValue());
        }
        out.append("\n}\n");
        Path temp=Files.createTempFile(file.getParent(),".topflop-", ".tmp");
        try {
            Files.writeString(temp,out,StandardCharsets.UTF_8);
            try {Files.move(temp,file,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}
            catch(java.nio.file.AtomicMoveNotSupportedException ex) {
                Files.move(temp,file,StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {Files.deleteIfExists(temp);}
    }
}
