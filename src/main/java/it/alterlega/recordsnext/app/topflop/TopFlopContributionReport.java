package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.topflop.TopFlopFacts.PlayerSeason;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Read-only, reproducible contribution CSV for a selected player/season.
 * Does not publish JS, modify SQLite, infer excluded days or alter the 3.2 pipeline.
 */
public final class TopFlopContributionReport {
    private TopFlopContributionReport() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 4)
            throw new IllegalArgumentException("Usage: <sqliteDb> <seasonId> <gazzaCode> <outputCsv>");
        Path db = Path.of(args[0]).toAbsolutePath().normalize();
        String season = args[1];
        int gazzaCode = Integer.parseInt(args[2]);
        Path dest = Path.of(args[3]).toAbsolutePath().normalize();
        if (!Files.isRegularFile(db) || !season.matches("[0-9]{4}_[0-9]{4}") || gazzaCode<=0)
            throw new IllegalArgumentException("Missing DB or invalid player/season");
        if (Files.exists(dest)) throw new IOException("Refusing to overwrite report: " + dest);
        Class.forName("org.sqlite.JDBC");
        List<String> csv = new ArrayList<>();
        csv.add("stagione;codiceStorico;giocatore;fantasquadra;fantasquadraId;famiglia;competizione;"+
            "contestiFormazione;partiteConteggiate;conVotoRealeValido;conVotoTabellinoNumerico;"+
            "sommaVotiReali;mediaVotiRealiConteggiati;sommaVotiTabellino;mediaVotiTabellino;"+
            "sommaFantavoti;fantamediaApportata;eventiApportati;eventiFcaConteggiatiOriginali");
        int groups = 0;
        try (var connection = DriverManager.getConnection("jdbc:sqlite:"+db.toUri()+"?mode=ro")) {
            var seasonData = TopFlopJdbcArchiveAssembler.loadSeason(connection,season);
            var profiles = seasonData.profiles().values().stream()
                .filter(p -> Objects.equals(p.player().gazzaCode(),gazzaCode))
                .sorted(Comparator.comparingInt(p -> p.player().fcaPlayerId())).toList();
            if (profiles.isEmpty()) throw new IllegalArgumentException("Historic code absent in season: " + gazzaCode);
            for(var profile:profiles) {
                PlayerSeason player = profile.player();
                // Explicitly separate simultaneous fantasy teams/competition contexts.
                record Group(int teamId,String teamName,String family,String competition) {}
                Map<Group,Boolean> groupsFound = new java.util.LinkedHashMap<>();
                for(var fact:seasonData.archive().fantasyFactsFor(player)) {
                    String teamName = fact.sourceFields().getOrDefault("FCM.Fantasquadra.Nome", "");
                    groupsFound.put(new Group(fact.key().fantasyTeamId(),teamName,
                        fact.family().name(),fact.competitionName()),Boolean.TRUE);
                }
                var sorted = new ArrayList<>(groupsFound.keySet());
                sorted.sort(Comparator.comparing(Group::teamName).thenComparing(Group::family).thenComparing(Group::competition));
                for(var g:sorted) {
                    var summary=TopFlopContributionSummary.summarize(seasonData.archive(),player,
                        g.teamId(),TopFlopFacts.CompetitionFamily.valueOf(g.family()),g.competition());
                    List<String> fields=List.of(season,Integer.toString(gazzaCode),profile.name(),g.teamName(),
                        Integer.toString(g.teamId()),g.family(),g.competition(),
                        Integer.toString(summary.fantasyContexts()),Integer.toString(summary.countedMatches()),
                        Integer.toString(summary.countedWithValidRealVote()),Integer.toString(summary.countedWithNumericTabellinoVote()),
                        decimal(summary.realVoteSum()),decimal(summary.meanRealVote()),
                        decimal(summary.tabellinoVoteSum()),decimal(summary.meanTabellinoVote()),
                        decimal(summary.fantasyTotalSum()),decimal(summary.meanFantasyTotal()),
                        summary.normalizedEventsApportioned().toString(),
                        summary.sourceEventsApportioned().toString());
                    csv.add(String.join(";",fields.stream().map(TopFlopContributionReport::quoted).toList()));
                    groups++;
                }
            }
        }
        Files.createDirectories(dest.getParent());
        Files.write(dest,csv,StandardCharsets.UTF_8,java.nio.file.StandardOpenOption.CREATE_NEW);
        System.out.println("TOP_FLOP_REPORT_OK season="+season+" code="+gazzaCode+
            " groups="+groups+" file="+dest);
    }

    private static String decimal(java.math.BigDecimal v) {
        return v==null ? "" : v.stripTrailingZeros().toPlainString();
    }
    private static String quoted(String v) {
        if (v == null) v = "";
        return '"'+v.replace("\"","\"\"")+'"';
    }
}
