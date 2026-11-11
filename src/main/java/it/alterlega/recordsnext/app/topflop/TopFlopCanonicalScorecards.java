package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.config.TopFlopExistingJson;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Reads the 3.2 normalized season reports, never computes a scorecard URL.
 * A link is keyed by season + actual encounter + fantasy team, not round alone.
 * Missing reports or missing links legitimately result in null.
 */
public final class TopFlopCanonicalScorecards {
    private TopFlopCanonicalScorecards() {}
    public record MatchTeam(long matchId, int teamId) {}

    public static Map<MatchTeam,String> load(Path reportsRoot,String seasonId) throws IOException {
        Objects.requireNonNull(reportsRoot);Objects.requireNonNull(seasonId);
        if (!seasonId.matches("[0-9]{4}_[0-9]{4}")) throw new IllegalArgumentException("Invalid season");
        Path seasonDir=reportsRoot.resolve(seasonId);
        if (!Files.isDirectory(seasonDir)) return Map.of();
        List<Path> reports;
        try(Stream<Path> files=Files.list(seasonDir)) {
            reports=files.filter(Files::isRegularFile)
                .filter(f->f.getFileName().toString().startsWith("season_normalized_"))
                .filter(f->f.getFileName().toString().endsWith(".json"))
                .sorted().toList();
        }
        Map<MatchTeam,String> links=new LinkedHashMap<>();
        for(Path report:reports) {
            final Object root;
            try {root=TopFlopExistingJson.parse(Files.readString(report,StandardCharsets.UTF_8));}
            catch(RuntimeException ex) {throw new IOException("Invalid canonical JSON: "+report,ex);}
            if (!(root instanceof Map<?,?> obj) || !(obj.get("partiteSquadra") instanceof List<?> rows))
                throw new IOException("Expected partiteSquadra in canonical report: "+report);
            for(Object item:rows) {
                if (!(item instanceof Map<?,?> row))continue;
                Long match=integer(row.get("idIncontro"));
                Long team=integer(row.get("idSquadra"));
                if(match==null||team==null||match<=0||team<=0||team>Integer.MAX_VALUE)continue;
                String link=cleanUrl(row.get("urlTabellinoOnline"));
                if(link==null)continue;
                MatchTeam key=new MatchTeam(match,team.intValue());
                String old=links.putIfAbsent(key,link);
                if(old!=null&&!old.equals(link))
                    throw new IOException("Conflicting canonical scorecard links: "+seasonId+" / "+key);
            }
        }
        return Map.copyOf(links);
    }
    private static Long integer(Object o){
        if(o==null)return null;
        try{return Long.parseLong(o.toString());}catch(NumberFormatException ex){return null;}
    }
    private static String cleanUrl(Object value){
        if(value==null)return null;
        String s=value.toString().trim();
        if(s.isEmpty()||!(s.startsWith("https://")||s.startsWith("http://")))return null;
        return s;
    }
}
