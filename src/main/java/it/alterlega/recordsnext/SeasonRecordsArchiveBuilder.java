package it.alterlega.recordsnext;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Costruisce l'archivio season_records_*.json a partire dai JSON normalizzati
 * prodotti da RecordsNext.
 *
 * Genera tutte le sezioni del contratto pubblico Records2026 usando i
 * dati normalizzati correnti, compresi i bonus capitano presenti in
 * modificatoriB2Dettaglio come tipo=capitano / campoOrigine=ModM2Pers.
 */
public final class SeasonRecordsArchiveBuilder {

    private SeasonRecordsArchiveBuilder() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Uso: SeasonRecordsArchiveBuilder <reportsRoot> <archiveRoot> [stagione ...]");
            System.exit(2);
        }
        Path reportsRoot = Path.of(args[0]).toAbsolutePath().normalize();
        Path archiveRoot = Path.of(args[1]).toAbsolutePath().normalize();
        List<String> seasons = new ArrayList<>();
        for (int i = 2; i < args.length; i++) {
            if (!args[i].isBlank()) seasons.add(args[i].trim());
        }
        Result result = build(reportsRoot, archiveRoot, seasons);
        System.out.println("Report       : " + reportsRoot);
        System.out.println("Archivio     : " + archiveRoot);
        System.out.println("Stagioni     : " + result.seasons());
        System.out.println("Competizioni : " + result.competitions());
        System.out.println("Sezioni      : 22/22");
    }

    public static Result build(Path reportsRoot, Path archiveRoot, List<String> requestedSeasons) throws IOException {
        if (!Files.isDirectory(reportsRoot)) {
            throw new IOException("Cartella report non trovata: " + reportsRoot);
        }
        Files.createDirectories(archiveRoot);

        List<Path> seasonDirs = resolveSeasonDirs(reportsRoot, requestedSeasons);
        int competitions = 0;
        int seasons = 0;
        for (Path seasonDir : seasonDirs) {
            List<Path> normalizedFiles = listNormalizedFiles(seasonDir);
            if (normalizedFiles.isEmpty()) continue;
            seasons++;
            Path outputSeason = archiveRoot.resolve(seasonDir.getFileName().toString());
            Files.createDirectories(outputSeason);
            for (Path normalizedFile : normalizedFiles) {
                Map<String, Object> source = object(parse(normalizedFile), normalizedFile, "radice");
                Map<String, Object> meta = object(source.get("meta"), normalizedFile, "meta");
                String competitionId = string(meta.get("competizioneStoricaId"));
                if (competitionId.isBlank()) {
                    String name = normalizedFile.getFileName().toString();
                    competitionId = name.substring("season_normalized_".length(), name.length() - ".json".length());
                }
                Map<String, Object> output = buildCompetition(source, meta);
                Path target = outputSeason.resolve("season_records_" + competitionId + ".json");
                String json = JsonWriter.writePretty(output) + System.lineSeparator();
                Files.writeString(target, json, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
                competitions++;
            }
        }
        if (competitions == 0) throw new IOException("Nessun season_normalized_*.json trovato in " + reportsRoot);
        return new Result(seasons, competitions);
    }

    private static Map<String, Object> buildCompetition(Map<String, Object> source, Map<String, Object> sourceMeta) {
        List<Map<String, Object>> matches = validTeamMatches(rows(source.get("partiteSquadra")));
        List<Map<String, Object>> expulsions = rows(source.get("espulsioniDettaglio"));
        List<Map<String, Object>> events = rows(source.get("eventiSquadraDettaglio"));
        List<Map<String, Object>> modifiers = rows(source.get("modificatoriB2Dettaglio"));
        List<Map<String, Object>> cleanSheets = rows(source.get("cleanSheetB3Dettaglio"));
        List<Map<String, Object>> goalBands = rows(source.get("fasceGolDettaglio"));

        Map<String, Object> records = new LinkedHashMap<>();
        records.put("puntiSquadraMax", pointsMax(matches));
        records.put("puntiSquadraMin", pointsMin(matches));
        records.put("partitePiuGolRegolamentari", matchesMostRegulationGoals(matches));
        records.put("partitePiuScartoRegolamentari", matchesLargestRegulationMargin(matches));
        records.put("mediaPuntiSquadre", aggregateMatchMetricByTeam(matches, "average-points", "Miglior media punti", "puntiFatti", AggregateMode.AVERAGE));
        records.put("totalePuntiSquadre", aggregateMatchMetricByTeam(matches, "total-points", "Maggior somma punti", "puntiFatti", AggregateMode.SUM));
        records.put("puntiClassificaSquadre", aggregateStandingsByTeam(matches));
        records.put("vittorieSquadre", aggregateResultByTeam(matches, "V", "wins", "Maggior numero di vittorie"));
        records.put("pareggiSquadre", aggregateResultByTeam(matches, "P", "draws", "Maggior numero di pareggi"));
        records.put("sconfitteSquadre", aggregateResultByTeam(matches, "S", "losses", "Maggior numero di sconfitte"));
        records.put("golFattiSquadre", aggregateMatchMetricByTeam(matches, "goals-for", "Maggior numero di gol fatti", "golFatti", AggregateMode.SUM));
        records.put("golSubitiSquadre", aggregateMatchMetricByTeam(matches, "goals-against", "Maggior numero di gol subiti", "golSubiti", AggregateMode.SUM));
        records.put("serieSenzaSconfitte", unbeatenSeries(matches));
        records.put("serieVittorie", resultSeries(matches, "V", true,
                "serie_vittorie", "Vittorie consecutive"));
        records.put("seriePareggi", resultSeries(matches, "P", true,
                "serie_pareggi", "Pareggi consecutivi"));
        records.put("serieSconfitte", resultSeries(matches, "S", true,
                "serie_sconfitte", "Sconfitte consecutive"));
        records.put("serieSenzaVittorie", resultSeries(matches, "V", false,
                "serie_senza_vittorie", "Partite consecutive senza vittorie"));
        records.put("espulsioniSquadre", expulsionsByTeam(expulsions));
        records.put("espulsioniGiocatori", expulsionsByPlayer(expulsions));
        records.put("ammonizioniSquadre", eventByTeam(events, "ammonizioniSquadre", "Maggiori ammonizioni"));
        records.put("assistSquadre", eventByTeam(events, "assistSquadre", "Maggiori assist"));
        records.put("autogolSquadre", eventByTeam(events, "autogolSquadre", "Maggiori autogol"));
        records.put("rigoriSbagliatiSquadre", eventByTeam(events, "rigoriSbagliatiSquadre", "Maggiori rigori sbagliati"));
        records.put("rigoriParatiSquadre", eventByTeam(events, "rigoriParatiSquadre", "Maggiori rigori parati"));
        records.put("golRigoreSquadre", eventByTeam(events, "golRigoreSquadre", "Maggiori gol fatti su rigore"));
        records.put("modDifesaMax", modifierMax(modifiers));
        records.put("modDifesaTotaleSquadre", modifierTotal(modifiers));
        records.put("capitanoMax", modifierMaxByType(modifiers, "capitano", "capitanoMax", "Miglior bonus Capitano in una gara"));
        records.put("capitanoVolteSquadre", captainCount(modifiers));
        records.put("capitanoTotaleSquadre", captainTotal(modifiers));
        records.put("modPersonalizzato3Max", modifierMaxByType(modifiers, "personalizzato3", "modPersonalizzato3Max", "Miglior modificatore personalizzato 3"));
        records.put("modPersonalizzato3TotaleSquadre", modifierTotalByType(modifiers, "personalizzato3", "modPersonalizzato3TotaleSquadre", "Maggior totale modificatore personalizzato 3"));
        records.put("modPortiereFcmMax", modifierMaxByType(modifiers, "fcmPortiere", "modPortiereFcmMax", "Miglior Modificatore Portiere FCM"));
        records.put("modPortiereFcmTotaleSquadre", modifierTotalByType(modifiers, "fcmPortiere", "modPortiereFcmTotaleSquadre", "Maggior totale Modificatore Portiere FCM"));
        records.put("modDifesaFcmMax", modifierMaxByType(modifiers, "fcmDifesa", "modDifesaFcmMax", "Miglior Modificatore Difesa FCM"));
        records.put("modDifesaFcmTotaleSquadre", modifierTotalByType(modifiers, "fcmDifesa", "modDifesaFcmTotaleSquadre", "Maggior totale Modificatore Difesa FCM"));
        records.put("modCentrocampoFcmMax", modifierMaxByType(modifiers, "fcmCentrocampo", "modCentrocampoFcmMax", "Miglior Modificatore Centrocampo FCM"));
        records.put("modCentrocampoFcmTotaleSquadre", modifierTotalByType(modifiers, "fcmCentrocampo", "modCentrocampoFcmTotaleSquadre", "Maggior totale Modificatore Centrocampo FCM"));
        records.put("modAttaccoFcmMax", modifierMaxByType(modifiers, "fcmAttacco", "modAttaccoFcmMax", "Miglior Modificatore Attacco FCM"));
        records.put("modAttaccoFcmTotaleSquadre", modifierTotalByType(modifiers, "fcmAttacco", "modAttaccoFcmTotaleSquadre", "Maggior totale Modificatore Attacco FCM"));
        records.put("modModuloFcmMax", modifierMaxByType(modifiers, "fcmModulo", "modModuloFcmMax", "Miglior Modificatore Modulo FCM"));
        records.put("modModuloFcmTotaleSquadre", modifierTotalByType(modifiers, "fcmModulo", "modModuloFcmTotaleSquadre", "Maggior totale Modificatore Modulo FCM"));
        records.put("modDifesaMediaSquadre", modifierAverageByType(modifiers, "modDifesa", "modDifesaMediaSquadre", "Miglior media Modificatore Difesa"));
        records.put("modDifesaUtilizziSquadre", modifierUsesByType(modifiers, "modDifesa", "modDifesaUtilizziSquadre", "Maggior numero utilizzi Modificatore Difesa"));
        records.put("capitanoMediaSquadre", modifierAverageByType(modifiers, "capitano", "capitanoMediaSquadre", "Miglior media Capitano"));
        records.put("capitanoUtilizziSquadre", modifierUsesByType(modifiers, "capitano", "capitanoUtilizziSquadre", "Maggior numero utilizzi Capitano"));
        records.put("modPersonalizzato3MediaSquadre", modifierAverageByType(modifiers, "personalizzato3", "modPersonalizzato3MediaSquadre", "Miglior media modificatore personalizzato 3"));
        records.put("modPersonalizzato3UtilizziSquadre", modifierUsesByType(modifiers, "personalizzato3", "modPersonalizzato3UtilizziSquadre", "Maggior numero utilizzi modificatore personalizzato 3"));
        records.put("modPortiereFcmMediaSquadre", modifierAverageByType(modifiers, "fcmPortiere", "modPortiereFcmMediaSquadre", "Miglior media Modificatore Portiere FCM"));
        records.put("modPortiereFcmUtilizziSquadre", modifierUsesByType(modifiers, "fcmPortiere", "modPortiereFcmUtilizziSquadre", "Maggior numero utilizzi Modificatore Portiere FCM"));
        records.put("modDifesaFcmMediaSquadre", modifierAverageByType(modifiers, "fcmDifesa", "modDifesaFcmMediaSquadre", "Miglior media Modificatore Difesa FCM"));
        records.put("modDifesaFcmUtilizziSquadre", modifierUsesByType(modifiers, "fcmDifesa", "modDifesaFcmUtilizziSquadre", "Maggior numero utilizzi Modificatore Difesa FCM"));
        records.put("modCentrocampoFcmMediaSquadre", modifierAverageByType(modifiers, "fcmCentrocampo", "modCentrocampoFcmMediaSquadre", "Miglior media Modificatore Centrocampo FCM"));
        records.put("modCentrocampoFcmUtilizziSquadre", modifierUsesByType(modifiers, "fcmCentrocampo", "modCentrocampoFcmUtilizziSquadre", "Maggior numero utilizzi Modificatore Centrocampo FCM"));
        records.put("modAttaccoFcmMediaSquadre", modifierAverageByType(modifiers, "fcmAttacco", "modAttaccoFcmMediaSquadre", "Miglior media Modificatore Attacco FCM"));
        records.put("modAttaccoFcmUtilizziSquadre", modifierUsesByType(modifiers, "fcmAttacco", "modAttaccoFcmUtilizziSquadre", "Maggior numero utilizzi Modificatore Attacco FCM"));
        records.put("modModuloFcmMediaSquadre", modifierAverageByType(modifiers, "fcmModulo", "modModuloFcmMediaSquadre", "Miglior media Modificatore Modulo FCM"));
        records.put("modModuloFcmUtilizziSquadre", modifierUsesByType(modifiers, "fcmModulo", "modModuloFcmUtilizziSquadre", "Maggior numero utilizzi Modificatore Modulo FCM"));
        records.put("cleanSheetPortiereVolteSquadre", cleanSheetCount(cleanSheets));
        records.put("cleanSheetPortiereTotaleSquadre", cleanSheetTotal(cleanSheets));
        records.put("cleanSheetPortiereSerieSquadre", cleanSheetSeries(matches, cleanSheets));
        records.put("modDifesaSerieSquadre", modifierSeries(matches, modifiers, "modDifesa",
                "modDifesaSerieSquadre", "Maggior serie modificatore personale 1"));
        records.put("capitanoSerieSquadre", modifierSeries(matches, modifiers, "capitano",
                "capitanoSerieSquadre", "Maggior serie modificatore personale 2"));
        records.put("modPersonalizzato3SerieSquadre", modifierSeries(matches, modifiers, "personalizzato3",
                "modPersonalizzato3SerieSquadre", "Maggior serie modificatore personale 3"));
        records.put("modPortiereFcmSerieSquadre", modifierSeries(matches, modifiers, "fcmPortiere",
                "modPortiereFcmSerieSquadre", "Maggior serie Modificatore Portiere FCM"));
        records.put("modDifesaFcmSerieSquadre", modifierSeries(matches, modifiers, "fcmDifesa",
                "modDifesaFcmSerieSquadre", "Maggior serie Modificatore Difesa FCM"));
        records.put("modCentrocampoFcmSerieSquadre", modifierSeries(matches, modifiers, "fcmCentrocampo",
                "modCentrocampoFcmSerieSquadre", "Maggior serie Modificatore Centrocampo FCM"));
        records.put("modAttaccoFcmSerieSquadre", modifierSeries(matches, modifiers, "fcmAttacco",
                "modAttaccoFcmSerieSquadre", "Maggior serie Modificatore Attacco FCM"));
        records.put("modModuloFcmSerieSquadre", modifierSeries(matches, modifiers, "fcmModulo",
                "modModuloFcmSerieSquadre", "Maggior serie Modificatore Modulo FCM"));
        records.put("fattoreCampoDecisivo", homeFieldDecisive(matches, modifiers, goalBands));
        records.put("fattoreCampoTotaleSquadre", homeFieldTotals(matches, modifiers));
        records.put("fattoreCampoPuntiGuadagnatiSquadre", homeFieldStandingsImpact(matches, modifiers, goalBands, true));
        records.put("fattoreCampoPuntiPersiSquadre", homeFieldStandingsImpact(matches, modifiers, goalBands, false));

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.putAll(sourceMeta);
        meta.put("builder", "RecordsNext SeasonRecordsArchiveBuilder");
        meta.put("sezioniGenerate", 26);
        meta.put("sezioniAttese", 26);
        meta.put("sezioniNonDisponibili", List.of());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("meta", meta);
        result.put("records", records);
        return result;
    }

    private enum AggregateMode { SUM, AVERAGE }

    private static List<Object> pointsMin(List<Map<String, Object>> matches) {
        List<Map<String, Object>> sorted = new ArrayList<>(matches);
        sorted.sort(Comparator.comparingDouble((Map<String, Object> r) -> number(r.get("puntiFatti")))
                .thenComparing(r -> string(r.get("idIncontro"))));
        List<Object> out = new ArrayList<>();
        for (int i = 0; i < Math.min(20, sorted.size()); i++) {
            Map<String, Object> r = sorted.get(i);
            out.add(ordered(
                    "recordId", "lowest-match-score",
                    "nome", "Minor punteggio in una partita",
                    "stagione", r.get("stagione"),
                    "competizioneStoricaId", r.get("competizioneStoricaId"),
                    "competizioneNome", r.get("competizioneNome"),
                    "valore", r.get("puntiFatti"),
                    "squadra", r.get("squadra"),
                    "avversaria", r.get("avversaria"),
                    "idIncontro", r.get("idIncontro"),
                    "giornata", r.get("giornata"),
                    "giornataDiA", r.get("giornataDiA"),
                    "urlTabellino", r.get("urlTabellino"),
                    "risultato", r.get("risultato"),
                    "punteggio", r.get("punteggio")
            ));
        }
        return out;
    }

    private static List<Object> matchesMostRegulationGoals(List<Map<String, Object>> matches) {
        Map<String, Map<String, Object>> byMatch = new LinkedHashMap<>();
        for (Map<String, Object> row : matches) {
            String matchId = string(row.get("idIncontro"));
            if (matchId.isBlank() || byMatch.containsKey(matchId)) continue;
            double total = number(row.get("golRegolamentariFatti")) + number(row.get("golRegolamentariSubiti"));
            Map<String, Object> out = ordered(
                    "recordId", "most-regulation-goals",
                    "nome", "Partita con più gol regolamentari",
                    "stagione", row.get("stagione"),
                    "competizioneStoricaId", row.get("competizioneStoricaId"),
                    "competizioneNome", row.get("competizioneNome"),
                    "valore", total,
                    "squadra", row.get("squadra"),
                    "avversaria", row.get("avversaria"),
                    "idIncontro", row.get("idIncontro"),
                    "giornata", row.get("giornata"),
                    "giornataDiA", row.get("giornataDiA"),
                    "urlTabellino", row.get("urlTabellino"),
                    "risultato", row.get("risultato"),
                    "punteggio", row.get("punteggio"),
                    "risultatoRegolamentari", row.get("risultatoRegolamentari"),
                    "fonteGolRegolamentari", row.get("fonteGolRegolamentari")
            );
            byMatch.put(matchId, out);
        }
        List<Map<String, Object>> sorted = new ArrayList<>(byMatch.values());
        sorted.sort(Comparator.comparingDouble((Map<String, Object> r) -> number(r.get("valore"))).reversed()
                .thenComparing(r -> string(r.get("idIncontro"))));
        return new ArrayList<>(sorted.subList(0, Math.min(20, sorted.size())));
    }

    private static List<Object> matchesLargestRegulationMargin(List<Map<String, Object>> matches) {
        Map<String, Map<String, Object>> byMatch = new LinkedHashMap<>();
        for (Map<String, Object> row : matches) {
            String matchId = string(row.get("idIncontro"));
            if (matchId.isBlank() || byMatch.containsKey(matchId)) continue;
            double margin = Math.abs(number(row.get("golRegolamentariFatti")) - number(row.get("golRegolamentariSubiti")));
            Map<String, Object> out = ordered(
                    "recordId", "largest-regulation-margin",
                    "nome", "Maggior scarto regolamentare",
                    "stagione", row.get("stagione"),
                    "competizioneStoricaId", row.get("competizioneStoricaId"),
                    "competizioneNome", row.get("competizioneNome"),
                    "valore", margin,
                    "squadra", row.get("squadra"),
                    "avversaria", row.get("avversaria"),
                    "idIncontro", row.get("idIncontro"),
                    "giornata", row.get("giornata"),
                    "giornataDiA", row.get("giornataDiA"),
                    "urlTabellino", row.get("urlTabellino"),
                    "risultato", row.get("risultato"),
                    "punteggio", row.get("punteggio"),
                    "risultatoRegolamentari", row.get("risultatoRegolamentari"),
                    "fonteGolRegolamentari", row.get("fonteGolRegolamentari")
            );
            byMatch.put(matchId, out);
        }
        List<Map<String, Object>> sorted = new ArrayList<>(byMatch.values());
        sorted.sort(Comparator.comparingDouble((Map<String, Object> r) -> number(r.get("valore"))).reversed()
                .thenComparing(r -> string(r.get("idIncontro"))));
        return new ArrayList<>(sorted.subList(0, Math.min(20, sorted.size())));
    }

    private static List<Object> aggregateMatchMetricByTeam(
            List<Map<String, Object>> matches,
            String recordId,
            String name,
            String field,
            AggregateMode mode) {
        Map<String, List<Map<String, Object>>> grouped = group(matches, "idSquadra");
        List<Map<String, Object>> out = new ArrayList<>();
        for (List<Map<String, Object>> teamMatches : grouped.values()) {
            if (teamMatches.isEmpty()) continue;
            double sum = teamMatches.stream().mapToDouble(r -> number(r.get(field))).sum();
            double value = mode == AggregateMode.AVERAGE ? sum / teamMatches.size() : sum;
            Map<String, Object> first = teamMatches.get(0);
            out.add(ordered(
                    "recordId", recordId,
                    "nome", name,
                    "stagione", first.get("stagione"),
                    "competizioneStoricaId", first.get("competizioneStoricaId"),
                    "competizioneNome", first.get("competizioneNome"),
                    "valore", value,
                    "idSquadra", first.get("idSquadra"),
                    "squadra", first.get("squadra"),
                    "partite", teamMatches.size()
            ));
        }
        sortValueTeam(out);
        return new ArrayList<>(out);
    }

    private static List<Object> aggregateResultByTeam(
            List<Map<String, Object>> matches,
            String resultCode,
            String recordId,
            String name) {
        Map<String, List<Map<String, Object>>> grouped = group(matches, "idSquadra");
        List<Map<String, Object>> out = new ArrayList<>();
        for (List<Map<String, Object>> teamMatches : grouped.values()) {
            if (teamMatches.isEmpty()) continue;
            long count = teamMatches.stream().filter(r -> resultCode.equals(string(r.get("esito")))).count();
            Map<String, Object> first = teamMatches.get(0);
            out.add(ordered(
                    "recordId", recordId,
                    "nome", name,
                    "stagione", first.get("stagione"),
                    "competizioneStoricaId", first.get("competizioneStoricaId"),
                    "competizioneNome", first.get("competizioneNome"),
                    "valore", count,
                    "idSquadra", first.get("idSquadra"),
                    "squadra", first.get("squadra"),
                    "partite", teamMatches.size()
            ));
        }
        sortValueTeam(out);
        return new ArrayList<>(out);
    }

    private static List<Object> aggregateStandingsByTeam(List<Map<String, Object>> matches) {
        Map<String, List<Map<String, Object>>> grouped = group(matches, "idSquadra");
        List<Map<String, Object>> out = new ArrayList<>();
        for (List<Map<String, Object>> teamMatches : grouped.values()) {
            if (teamMatches.isEmpty()) continue;
            int points = 0;
            for (Map<String, Object> row : teamMatches) {
                String result = string(row.get("esito"));
                if ("V".equals(result)) points += 3;
                else if ("P".equals(result)) points += 1;
            }
            Map<String, Object> first = teamMatches.get(0);
            out.add(ordered(
                    "recordId", "standings-points",
                    "nome", "Maggior numero di punti in classifica",
                    "stagione", first.get("stagione"),
                    "competizioneStoricaId", first.get("competizioneStoricaId"),
                    "competizioneNome", first.get("competizioneNome"),
                    "valore", points,
                    "idSquadra", first.get("idSquadra"),
                    "squadra", first.get("squadra"),
                    "partite", teamMatches.size()
            ));
        }
        sortValueTeam(out);
        return new ArrayList<>(out);
    }

    private static List<Object> pointsMax(List<Map<String, Object>> matches) {
        List<Map<String, Object>> sorted = new ArrayList<>(matches);
        sorted.sort(Comparator.comparingDouble((Map<String, Object> r) -> number(r.get("puntiFatti"))).reversed()
                .thenComparing(r -> string(r.get("idIncontro"))));
        List<Object> out = new ArrayList<>();
        for (int i = 0; i < Math.min(20, sorted.size()); i++) {
            Map<String, Object> r = sorted.get(i);
            Map<String, Object> row = ordered(
                    "recordId", "punti_squadra_max",
                    "nome", "Maggior numero di punti fatti",
                    "stagione", r.get("stagione"),
                    "competizioneStoricaId", r.get("competizioneStoricaId"),
                    "competizioneNome", r.get("competizioneNome"),
                    "valore", r.get("puntiFatti"),
                    "squadra", r.get("squadra"),
                    "avversaria", r.get("avversaria"),
                    "idIncontro", r.get("idIncontro"),
                    "giornata", r.get("giornata"),
                    "giornataDiA", r.get("giornataDiA"),
                    "urlTabellino", r.get("urlTabellino"),
                    "risultato", r.get("risultato"),
                    "punteggio", r.get("punteggio"));
            row.put("dettagli", ordered(
                    "parzialeFatto", r.get("parzialeFatto"),
                    "parzialeSubito", r.get("parzialeSubito"),
                    "puntiSubiti", r.get("puntiSubiti"),
                    "golFatti", r.get("golFatti"),
                    "golSubiti", r.get("golSubiti"),
                    "golRegolamentariFatti", r.get("golRegolamentariFatti"),
                    "golRegolamentariSubiti", r.get("golRegolamentariSubiti"),
                    "risultatoRegolamentari", r.get("risultatoRegolamentari"),
                    "fonteGolRegolamentari", r.get("fonteGolRegolamentari")));
            out.add(row);
        }
        return out;
    }

    private static List<Object> unbeatenSeries(List<Map<String, Object>> matches) {
        Map<String, List<Map<String, Object>>> byTeam = group(matches, "idSquadra");
        List<Map<String, Object>> records = new ArrayList<>();
        for (List<Map<String, Object>> teamMatches : byTeam.values()) {
            teamMatches.sort(Comparator.comparingDouble((Map<String, Object> r) -> number(r.get("ordineGiornata")))
                    .thenComparing(r -> string(r.get("idIncontro"))));
            List<Map<String, Object>> current = new ArrayList<>();
            for (Map<String, Object> match : teamMatches) {
                if (!"S".equals(string(match.get("esito")))) {
                    current.add(match);
                } else {
                    addUnbeaten(records, current);
                    current = new ArrayList<>();
                }
            }
            addUnbeaten(records, current);
        }
        records.sort(Comparator.comparingDouble((Map<String, Object> r) -> number(r.get("valore"))).reversed()
                .thenComparing(Comparator.comparingDouble((Map<String, Object> r) -> number(r.get("vittorie"))).reversed())
                .thenComparing(r -> string(r.get("squadra"))));
        return new ArrayList<>(records.subList(0, Math.min(20, records.size())));
    }

    private static void addUnbeaten(List<Map<String, Object>> records, List<Map<String, Object>> series) {
        if (series.isEmpty()) return;
        Map<String, Object> first = series.get(0), last = series.get(series.size() - 1);
        long wins = series.stream().filter(r -> "V".equals(string(r.get("esito")))).count();
        long draws = series.stream().filter(r -> "P".equals(string(r.get("esito")))).count();
        List<Object> details = new ArrayList<>();
        for (Map<String, Object> r : series) {
            details.add(ordered("idIncontro", r.get("idIncontro"), "giornata", r.get("giornata"),
                    "giornataDiA", r.get("giornataDiA"), "urlTabellino", r.get("urlTabellino"),
                    "avversaria", r.get("avversaria"), "risultato", r.get("risultato"),
                    "punteggio", r.get("punteggio"), "esito", r.get("esito")));
        }
        Map<String, Object> row = ordered(
                "recordId", "serie_senza_sconfitte", "nome", "Partite consecutive senza sconfitte",
                "stagione", first.get("stagione"), "competizioneStoricaId", first.get("competizioneStoricaId"),
                "competizioneNome", first.get("competizioneNome"), "valore", series.size(),
                "squadra", first.get("squadra"), "idSquadra", first.get("idSquadra"),
                "daGiornata", first.get("giornata"), "aGiornata", last.get("giornata"),
                "daGiornataDiA", first.get("giornataDiA"), "aGiornataDiA", last.get("giornataDiA"),
                "vittorie", wins, "pareggi", draws);
        row.put("dettagli", details);
        records.add(row);
    }


    private static List<Object> resultSeries(List<Map<String, Object>> matches,
                                             String resultCode,
                                             boolean mustMatch,
                                             String recordId,
                                             String name) {
        Map<String, List<Map<String, Object>>> byTeam = group(matches, "idSquadra");
        List<Map<String, Object>> records = new ArrayList<>();

        for (List<Map<String, Object>> teamMatches : byTeam.values()) {
            teamMatches.sort(Comparator
                    .comparingDouble((Map<String, Object> row) -> number(row.get("ordineGiornata")))
                    .thenComparing(row -> string(row.get("idIncontro"))));

            List<Map<String, Object>> current = new ArrayList<>();
            for (Map<String, Object> match : teamMatches) {
                boolean matchesResult = resultCode.equals(string(match.get("esito")));
                boolean belongs = mustMatch ? matchesResult : !matchesResult;
                if (belongs) {
                    current.add(match);
                } else {
                    addResultSeries(records, current, recordId, name);
                    current = new ArrayList<>();
                }
            }
            addResultSeries(records, current, recordId, name);
        }

        records.sort(Comparator
                .comparingDouble((Map<String, Object> row) -> number(row.get("valore"))).reversed()
                .thenComparing(row -> string(row.get("squadra"))));
        return new ArrayList<>(records.subList(0, Math.min(20, records.size())));
    }

    private static void addResultSeries(List<Map<String, Object>> records,
                                        List<Map<String, Object>> series,
                                        String recordId,
                                        String name) {
        if (series.isEmpty()) return;

        Map<String, Object> first = series.get(0);
        Map<String, Object> last = series.get(series.size() - 1);
        List<Object> details = new ArrayList<>();
        for (Map<String, Object> row : series) {
            details.add(ordered(
                    "idIncontro", row.get("idIncontro"),
                    "giornata", row.get("giornata"),
                    "giornataDiA", row.get("giornataDiA"),
                    "urlTabellino", row.get("urlTabellino"),
                    "avversaria", row.get("avversaria"),
                    "risultato", row.get("risultato"),
                    "punteggio", row.get("punteggio"),
                    "esito", row.get("esito")
            ));
        }

        Map<String, Object> record = ordered(
                "recordId", recordId,
                "nome", name,
                "stagione", first.get("stagione"),
                "competizioneStoricaId", first.get("competizioneStoricaId"),
                "competizioneNome", first.get("competizioneNome"),
                "valore", series.size(),
                "idSquadra", first.get("idSquadra"),
                "squadra", first.get("squadra"),
                "daGiornata", first.get("giornata"),
                "aGiornata", last.get("giornata"),
                "daGiornataDiA", first.get("giornataDiA"),
                "aGiornataDiA", last.get("giornataDiA")
        );
        record.put("dettagli", details);
        records.add(record);
    }

    private static List<Object> expulsionsByTeam(List<Map<String, Object>> rows) {
        return aggregateCount(rows, "idSquadra", "squadra", "espulsioni_squadra",
                "Maggiori espulsioni squadra", List.of("idIncontro", "giornataDiA", "idGiocatore", "giocatore"));
    }

    private static List<Object> expulsionsByPlayer(List<Map<String, Object>> rows) {
        return aggregateCount(rows, "idGiocatore", "giocatore", "espulsioni_giocatore",
                "Maggiori espulsioni giocatore", List.of("idIncontro", "giornataDiA", "idSquadra", "squadra"));
    }

    private static List<Object> aggregateCount(List<Map<String, Object>> rows, String idField, String nameField,
                                                String recordId, String name, List<String> detailFields) {
        Map<String, List<Map<String, Object>>> groups = group(rows, idField);
        List<Map<String, Object>> out = new ArrayList<>();
        for (List<Map<String, Object>> group : groups.values()) {
            Map<String, Object> first = group.get(0);
            Map<String, Object> item = ordered("recordId", recordId, "nome", name, "valore", group.size(),
                    idField, first.get(idField), nameField, first.get(nameField));
            List<Object> details = new ArrayList<>();
            for (Map<String, Object> r : group) details.add(project(r, detailFields));
            item.put("dettagli", details);
            out.add(item);
        }
        out.sort(Comparator.comparingDouble((Map<String, Object> r) -> number(r.get("valore"))).reversed()
                .thenComparing(r -> string(r.get(nameField))));
        return new ArrayList<>(out);
    }

    private static List<Object> eventByTeam(List<Map<String, Object>> events, String key, String name) {
        List<Map<String, Object>> selected = events.stream().filter(r -> key.equals(string(r.get("recordKey")))).toList();
        Map<String, List<Map<String, Object>>> groups = group(selected, "idSquadra");
        List<Map<String, Object>> out = new ArrayList<>();
        for (List<Map<String, Object>> group : groups.values()) {
            group.sort(Comparator.comparingDouble((Map<String, Object> r) -> number(r.get("giornataDiA")))
                    .thenComparing(r -> string(r.get("idIncontro"))).thenComparing(r -> string(r.get("giocatore"))));
            Map<String, Object> first = group.get(0);
            double total = group.stream().mapToDouble(r -> number(r.get("valore"))).sum();
            Map<String, Object> item = ordered("recordId", key, "nome", name, "valore", cleanNumber(total),
                    "idSquadra", first.get("idSquadra"), "squadra", first.get("squadra"));
            List<Object> details = new ArrayList<>();
            for (Map<String, Object> r : group) {
                details.add(ordered("giornataDiA", r.get("giornataDiA"), "idIncontro", r.get("idIncontro"),
                        "idGiocatore", r.get("idGiocatore"), "giocatore", r.get("giocatore"),
                        "valore", r.get("valore"), "campoOrigine", r.get("campoOrigine")));
            }
            item.put("dettagli", details);
            out.add(item);
        }
        out.sort(Comparator.comparingDouble((Map<String, Object> r) -> number(r.get("valore"))).reversed()
                .thenComparing(r -> string(r.get("squadra"))));
        return new ArrayList<>(out);
    }

    private static List<Object> modifierMax(List<Map<String, Object>> modifiers) {
        List<Map<String, Object>> rows = modifiers.stream().filter(r -> "modDifesa".equals(string(r.get("tipo"))))
                .sorted(Comparator.comparingDouble((Map<String, Object> r) -> number(r.get("valore"))).reversed()
                        .thenComparing(r -> string(r.get("squadra"))))
                .limit(20).toList();
        List<Object> out = new ArrayList<>();
        for (Map<String, Object> r : rows) out.add(ordered("recordId", "modDifesaMax", "nome", "Miglior modificatore difesa",
                "valore", r.get("valore"), "idSquadra", r.get("idSquadra"), "squadra", r.get("squadra"),
                "avversaria", r.get("avversaria"), "idIncontro", r.get("idIncontro"), "giornataDiA", r.get("giornataDiA")));
        return out;
    }

    private static List<Object> modifierTotal(List<Map<String, Object>> modifiers) {
        List<Map<String, Object>> selected = modifiers.stream().filter(r -> "modDifesa".equals(string(r.get("tipo")))).toList();
        return aggregateSum(selected, "modDifesaTotaleSquadre", "Maggior totale modificatore difesa",
                List.of("idIncontro", "giornataDiA", "avversaria", "valore"));
    }

    private static List<Object> modifierMaxByType(
            List<Map<String, Object>> modifiers,
            String type,
            String recordId,
            String label) {
        List<Map<String, Object>> rows = modifiers.stream()
                .filter(r -> type.equals(string(r.get("tipo"))))
                .sorted(Comparator.comparingDouble((Map<String, Object> r) -> number(r.get("valore"))).reversed()
                        .thenComparing(r -> string(r.get("squadra"))))
                .limit(20)
                .toList();
        List<Object> out = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            out.add(ordered(
                    "recordId", recordId,
                    "nome", label,
                    "valore", r.get("valore"),
                    "idSquadra", r.get("idSquadra"),
                    "squadra", r.get("squadra"),
                    "avversaria", r.get("avversaria"),
                    "idIncontro", r.get("idIncontro"),
                    "giornataDiA", r.get("giornataDiA"),
                    "campoOrigine", r.get("campoOrigine")));
        }
        return out;
    }

    private static List<Object> modifierTotalByType(
            List<Map<String, Object>> modifiers,
            String type,
            String recordId,
            String label) {
        List<Map<String, Object>> selected = modifiers.stream()
                .filter(r -> type.equals(string(r.get("tipo"))))
                .toList();
        return aggregateSum(selected, recordId, label,
                List.of("idIncontro", "giornataDiA", "avversaria", "valore", "campoOrigine"));
    }

    private static List<Object> modifierAverageByType(
            List<Map<String, Object>> modifiers,
            String type,
            String recordId,
            String label) {
        List<Map<String, Object>> selected = modifiers.stream()
                .filter(r -> type.equals(string(r.get("tipo"))))
                .toList();
        Map<String, List<Map<String, Object>>> groups = group(selected, "idSquadra");
        List<Map<String, Object>> out = new ArrayList<>();
        for (List<Map<String, Object>> rows : groups.values()) {
            if (rows.isEmpty()) continue;
            Map<String, Object> first = rows.get(0);
            double sum = rows.stream().mapToDouble(r -> number(r.get("valore"))).sum();
            Map<String, Object> item = ordered(
                    "recordId", recordId,
                    "nome", label,
                    "valore", cleanNumber(sum / rows.size()),
                    "utilizzi", rows.size(),
                    "idSquadra", first.get("idSquadra"),
                    "squadra", first.get("squadra"));
            item.put("dettagli", modifierDetails(rows));
            out.add(item);
        }
        sortValueTeam(out);
        return new ArrayList<>(out);
    }

    private static List<Object> modifierUsesByType(
            List<Map<String, Object>> modifiers,
            String type,
            String recordId,
            String label) {
        List<Map<String, Object>> selected = modifiers.stream()
                .filter(r -> type.equals(string(r.get("tipo"))))
                .toList();
        Map<String, List<Map<String, Object>>> groups = group(selected, "idSquadra");
        List<Map<String, Object>> out = new ArrayList<>();
        for (List<Map<String, Object>> rows : groups.values()) {
            if (rows.isEmpty()) continue;
            Map<String, Object> first = rows.get(0);
            Map<String, Object> item = ordered(
                    "recordId", recordId,
                    "nome", label,
                    "valore", rows.size(),
                    "idSquadra", first.get("idSquadra"),
                    "squadra", first.get("squadra"));
            item.put("dettagli", modifierDetails(rows));
            out.add(item);
        }
        sortValueTeam(out);
        return new ArrayList<>(out);
    }

    private static List<Object> captainCount(List<Map<String, Object>> modifiers) {
        List<Map<String, Object>> selected = modifiers.stream()
                .filter(r -> "capitano".equals(string(r.get("tipo"))))
                .toList();
        Map<String, List<Map<String, Object>>> groups = group(selected, "idSquadra");
        List<Map<String, Object>> out = new ArrayList<>();
        for (List<Map<String, Object>> group : groups.values()) {
            group.sort(Comparator.comparingDouble((Map<String, Object> r) -> number(r.get("giornataDiA")))
                    .thenComparing(r -> string(r.get("idIncontro"))));
            Map<String, Object> first = group.get(0);
            Map<String, Object> item = ordered(
                    "recordId", "capitanoVolteSquadre",
                    "nome", "Maggior numero bonus capitano",
                    "valore", group.size(),
                    "idSquadra", first.get("idSquadra"),
                    "squadra", first.get("squadra"));
            item.put("dettagli", modifierDetails(group));
            out.add(item);
        }
        sortValueTeam(out);
        return new ArrayList<>(out);
    }

    private static List<Object> captainTotal(List<Map<String, Object>> modifiers) {
        List<Map<String, Object>> selected = modifiers.stream()
                .filter(r -> "capitano".equals(string(r.get("tipo"))))
                .toList();
        return aggregateSum(selected, "capitanoTotaleSquadre",
                "Maggior totale bonus capitano",
                List.of("idIncontro", "giornataDiA", "avversaria", "valore"));
    }

    private static List<Object> modifierDetails(List<Map<String, Object>> group) {
        List<Object> details = new ArrayList<>();
        for (Map<String, Object> r : group) {
            details.add(ordered(
                    "idIncontro", r.get("idIncontro"),
                    "giornataDiA", r.get("giornataDiA"),
                    "avversaria", r.get("avversaria"),
                    "valore", r.get("valore")));
        }
        return details;
    }

    private static List<Object> modifierSeries(
            List<Map<String, Object>> matches,
            List<Map<String, Object>> modifiers,
            String type,
            String recordId,
            String name) {
        List<Map<String, Object>> selected = modifiers.stream()
                .filter(r -> type.equals(string(r.get("tipo"))))
                .toList();
        return eventSeries(matches, selected, recordId, name);
    }


    private static List<Object> homeFieldDecisive(List<Map<String, Object>> matches,
                                                   List<Map<String, Object>> modifiers,
                                                   List<Map<String, Object>> goalBands) {
        List<HomeFieldImpact> impacts = homeFieldImpacts(matches, modifiers, goalBands);
        List<Object> out = new ArrayList<>();
        for (HomeFieldImpact impact : impacts) {
            if (impact.homePointsDelta() <= 0) continue;
            Map<String, Object> home = impact.home();
            Map<String, Object> away = impact.away();
            out.add(ordered(
                    "recordId", "fattoreCampoDecisivo",
                    "nome", "Fattore Campo decisivo",
                    "valore", cleanNumber(impact.homeBonus()),
                    "puntiClassificaGuadagnati", impact.homePointsDelta(),
                    "stagione", home.get("stagione"),
                    "competizioneStoricaId", home.get("competizioneStoricaId"),
                    "competizioneNome", home.get("competizioneNome"),
                    "idSquadra", home.get("idSquadra"),
                    "squadra", home.get("squadra"),
                    "idAvversaria", away.get("idSquadra"),
                    "avversaria", away.get("squadra"),
                    "idIncontro", home.get("idIncontro"),
                    "giornata", home.get("giornata"),
                    "giornataDiA", home.get("giornataDiA"),
                    "urlTabellino", home.get("urlTabellino"),
                    "urlTabellinoLocale", home.get("urlTabellinoLocale"),
                    "urlTabellinoOnline", home.get("urlTabellinoOnline"),
                    "punteggioConFattoreCampo", home.get("punteggio"),
                    "risultatoConFattoreCampo", home.get("risultato"),
                    "puntiCasaSenzaFattoreCampo", cleanNumber(impact.homeScoreWithout()),
                    "golCasaSenzaFattoreCampo", impact.homeGoalsWithout(),
                    "risultatoSenzaFattoreCampo", impact.homeGoalsWithout() + "-" + impact.awayGoals()
            ));
        }
        out.sort(Comparator
                .comparingDouble((Object value) -> number(((Map<?, ?>) value).get("puntiClassificaGuadagnati"))).reversed()
                .thenComparing(value -> string(((Map<?, ?>) value).get("stagione")))
                .thenComparing(value -> string(((Map<?, ?>) value).get("idIncontro"))));
        return out;
    }

    private static List<Object> homeFieldTotals(List<Map<String, Object>> matches,
                                                 List<Map<String, Object>> modifiers) {
        Map<String, Double> modifierTotals = modifierTotalsByMatchTeam(modifiers);
        Map<String, List<Map<String, Object>>> groups = new LinkedHashMap<>();
        for (Map<String, Object> match : matches) {
            if (!"casa".equals(string(match.get("lato")))) continue;
            groups.computeIfAbsent(string(match.get("idSquadra")), ignored -> new ArrayList<>()).add(match);
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (List<Map<String, Object>> group : groups.values()) {
            Map<String, Object> first = group.get(0);
            double total = 0;
            List<Object> details = new ArrayList<>();
            for (Map<String, Object> match : group) {
                double bonus = homeBonus(match, modifierTotals);
                total += bonus;
                details.add(ordered(
                        "idIncontro", match.get("idIncontro"),
                        "giornataDiA", match.get("giornataDiA"),
                        "avversaria", match.get("avversaria"),
                        "valore", cleanNumber(bonus),
                        "urlTabellino", match.get("urlTabellino")));
            }
            Map<String, Object> item = ordered(
                    "recordId", "fattoreCampoTotaleSquadre",
                    "nome", "Maggior totale Fattore Campo",
                    "valore", cleanNumber(total),
                    "presenzeCasa", group.size(),
                    "idSquadra", first.get("idSquadra"),
                    "squadra", first.get("squadra"));
            item.put("dettagli", details);
            out.add(item);
        }
        sortValueTeam(out);
        return new ArrayList<>(out);
    }

    private static List<Object> homeFieldStandingsImpact(List<Map<String, Object>> matches,
                                                          List<Map<String, Object>> modifiers,
                                                          List<Map<String, Object>> goalBands,
                                                          boolean homeTeam) {
        Map<String, List<HomeFieldImpact>> groups = new LinkedHashMap<>();
        for (HomeFieldImpact impact : homeFieldImpacts(matches, modifiers, goalBands)) {
            if (impact.homePointsDelta() <= 0) continue;
            Map<String, Object> team = homeTeam ? impact.home() : impact.away();
            groups.computeIfAbsent(string(team.get("idSquadra")), ignored -> new ArrayList<>()).add(impact);
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (List<HomeFieldImpact> group : groups.values()) {
            HomeFieldImpact firstImpact = group.get(0);
            Map<String, Object> team = homeTeam ? firstImpact.home() : firstImpact.away();
            int total = group.stream().mapToInt(HomeFieldImpact::homePointsDelta).sum();
            List<Object> details = new ArrayList<>();
            for (HomeFieldImpact impact : group) {
                Map<String, Object> home = impact.home();
                details.add(ordered(
                        "idIncontro", home.get("idIncontro"),
                        "giornataDiA", home.get("giornataDiA"),
                        "squadraCasa", home.get("squadra"),
                        "squadraFuori", impact.away().get("squadra"),
                        "puntiClassifica", impact.homePointsDelta(),
                        "urlTabellino", home.get("urlTabellino")));
            }
            Map<String, Object> item = ordered(
                    "recordId", homeTeam ? "fattoreCampoPuntiGuadagnatiSquadre" : "fattoreCampoPuntiPersiSquadre",
                    "nome", homeTeam ? "Punti classifica guadagnati col Fattore Campo" : "Punti classifica persi per il Fattore Campo avversario",
                    "valore", total,
                    "idSquadra", team.get("idSquadra"),
                    "squadra", team.get("squadra"));
            item.put("dettagli", details);
            out.add(item);
        }
        sortValueTeam(out);
        return new ArrayList<>(out);
    }

    private static List<HomeFieldImpact> homeFieldImpacts(List<Map<String, Object>> matches,
                                                           List<Map<String, Object>> modifiers,
                                                           List<Map<String, Object>> goalBands) {
        Map<String, List<Map<String, Object>>> byMatch = group(matches, "idIncontro");
        Map<String, Double> modifierTotals = modifierTotalsByMatchTeam(modifiers);
        List<Map<String, Object>> sortedBands = new ArrayList<>(goalBands);
        sortedBands.sort(Comparator.comparingDouble(row -> number(row.get("min"))));
        List<HomeFieldImpact> impacts = new ArrayList<>();
        for (List<Map<String, Object>> pair : byMatch.values()) {
            Map<String, Object> home = pair.stream().filter(row -> "casa".equals(string(row.get("lato")))).findFirst().orElse(null);
            Map<String, Object> away = pair.stream().filter(row -> "fuori".equals(string(row.get("lato")))).findFirst().orElse(null);
            if (home == null || away == null) continue;
            double bonus = homeBonus(home, modifierTotals);
            if (bonus <= 0) continue;
            double scoreWithout = number(home.get("puntiFatti")) - bonus;
            int goalsWithout = goalsForScore(scoreWithout, sortedBands);
            int homeGoals = (int) number(home.get("golFatti"));
            int awayGoals = (int) number(home.get("golSubiti"));
            int actualPoints = standingsPoints(homeGoals, awayGoals);
            int pointsWithout = standingsPoints(goalsWithout, awayGoals);
            impacts.add(new HomeFieldImpact(home, away, bonus, scoreWithout, goalsWithout, awayGoals,
                    Math.max(0, actualPoints - pointsWithout)));
        }
        return impacts;
    }

    private static Map<String, Double> modifierTotalsByMatchTeam(List<Map<String, Object>> modifiers) {
        Map<String, Double> totals = new LinkedHashMap<>();
        for (Map<String, Object> modifier : modifiers) {
            String key = string(modifier.get("idIncontro")) + "|" + string(modifier.get("idSquadra"));
            totals.merge(key, number(modifier.get("valore")), Double::sum);
        }
        return totals;
    }

    private static double homeBonus(Map<String, Object> match, Map<String, Double> modifierTotals) {
        String key = string(match.get("idIncontro")) + "|" + string(match.get("idSquadra"));
        double residual = number(match.get("puntiFatti")) - number(match.get("parzialeFatto"))
                - modifierTotals.getOrDefault(key, 0.0);
        return Math.abs(residual) < 0.000001 ? 0.0 : residual;
    }

    private static int goalsForScore(double score, List<Map<String, Object>> goalBands) {
        int goals = 0;
        for (Map<String, Object> band : goalBands) {
            if (score + 0.000001 >= number(band.get("min"))) {
                goals = Math.max(goals, (int) number(band.get("gol")));
            }
        }
        return goals;
    }

    private static int standingsPoints(int goalsFor, int goalsAgainst) {
        if (goalsFor > goalsAgainst) return 3;
        if (goalsFor == goalsAgainst) return 1;
        return 0;
    }

    private record HomeFieldImpact(Map<String, Object> home, Map<String, Object> away,
                                   double homeBonus, double homeScoreWithout,
                                   int homeGoalsWithout, int awayGoals, int homePointsDelta) {
    }

    private static List<Object> cleanSheetCount(List<Map<String, Object>> clean) {
        Map<String, List<Map<String, Object>>> groups = group(clean, "idSquadra");
        List<Map<String, Object>> out = new ArrayList<>();
        for (List<Map<String, Object>> group : groups.values()) {
            Map<String, Object> first = group.get(0);
            Map<String, Object> item = ordered("recordId", "cleanSheetPortiereVolteSquadre",
                    "nome", "Maggior numero clean sheet portiere", "valore", group.size(),
                    "idSquadra", first.get("idSquadra"), "squadra", first.get("squadra"));
            item.put("dettagli", cleanDetails(group));
            out.add(item);
        }
        sortValueTeam(out);
        return new ArrayList<>(out);
    }

    private static List<Object> cleanSheetTotal(List<Map<String, Object>> clean) {
        Map<String, List<Map<String, Object>>> groups = group(clean, "idSquadra");
        List<Map<String, Object>> out = new ArrayList<>();
        for (List<Map<String, Object>> group : groups.values()) {
            Map<String, Object> first = group.get(0);
            double total = group.stream().mapToDouble(r -> number(r.get("valore"))).sum();
            Map<String, Object> item = ordered("recordId", "cleanSheetPortiereTotaleSquadre",
                    "nome", "Maggior totale bonus clean sheet portiere", "valore", cleanNumber(total),
                    "idSquadra", first.get("idSquadra"), "squadra", first.get("squadra"));
            item.put("dettagli", cleanDetails(group));
            out.add(item);
        }
        sortValueTeam(out);
        return new ArrayList<>(out);
    }

    private static List<Object> cleanDetails(List<Map<String, Object>> group) {
        List<Map<String, Object>> sorted = new ArrayList<>(group);
        sorted.sort(Comparator.comparingDouble((Map<String, Object> r) -> number(r.get("giornataDiA")))
                .thenComparing(r -> string(r.get("idIncontro"))));
        List<Object> details = new ArrayList<>();
        for (Map<String, Object> r : sorted) details.add(ordered("idIncontro", r.get("idIncontro"),
                "giornataDiA", r.get("giornataDiA"), "avversaria", r.get("avversaria"),
                "giocatore", r.get("giocatore"), "valore", r.get("valore")));
        return details;
    }

    private static List<Object> cleanSheetSeries(List<Map<String, Object>> matches, List<Map<String, Object>> clean) {
        return eventSeries(matches, clean, "cleanSheetPortiereSerieSquadre",
                "Maggior serie clean sheet portiere");
    }

    private static List<Object> eventSeries(List<Map<String, Object>> matches,
                                            List<Map<String, Object>> events,
                                            String recordId, String name) {
        Set<String> eventKeys = new LinkedHashSet<>();
        for (Map<String, Object> r : events) {
            eventKeys.add(string(r.get("idSquadra")) + "|" + string(r.get("idIncontro")));
        }

        Map<String, List<Map<String, Object>>> byTeam = group(matches, "idSquadra");
        List<Map<String, Object>> out = new ArrayList<>();

        for (List<Map<String, Object>> teamMatches : byTeam.values()) {
            teamMatches.sort(Comparator
                    .comparingDouble((Map<String, Object> r) -> number(r.get("ordineGiornata")))
                    .thenComparing(r -> string(r.get("idIncontro"))));

            List<Map<String, Object>> best = new ArrayList<>();
            List<Map<String, Object>> current = new ArrayList<>();

            for (Map<String, Object> match : teamMatches) {
                String key = string(match.get("idSquadra")) + "|" + string(match.get("idIncontro"));
                if (eventKeys.contains(key)) {
                    current.add(match);
                } else {
                    if (current.size() > best.size()) best = new ArrayList<>(current);
                    current.clear();
                }
            }
            if (current.size() > best.size()) best = new ArrayList<>(current);

            if (!best.isEmpty()) out.add(eventSeriesRecord(best, recordId, name));
        }

        sortValueTeam(out);
        return new ArrayList<>(out);
    }

    private static Map<String, Object> eventSeriesRecord(List<Map<String, Object>> series,
                                                          String recordId, String name) {
        Map<String, Object> first = series.get(0);
        Map<String, Object> last = series.get(series.size() - 1);
        return ordered(
                "recordId", recordId,
                "nome", name,
                "valore", series.size(),
                "stagione", first.get("stagione"),
                "competizioneStoricaId", first.get("competizioneStoricaId"),
                "competizioneNome", first.get("competizioneNome"),
                "idSquadra", first.get("idSquadra"),
                "squadra", first.get("squadra"),
                "daGiornata", first.get("giornata"),
                "aGiornata", last.get("giornata"),
                "daGiornataDiA", first.get("giornataDiA"),
                "aGiornataDiA", last.get("giornataDiA"),
                "dettagli", series.stream().map(r -> ordered(
                        "idIncontro", r.get("idIncontro"),
                        "matchId", r.get("idIncontro"),
                        "giornata", r.get("giornata"),
                        "giornataDiA", r.get("giornataDiA"),
                        "urlTabellino", r.get("urlTabellino"),
                        "avversaria", r.get("avversaria"))).toList());
    }

    private static List<Object> aggregateSum(List<Map<String, Object>> rows, String recordId, String name,
                                              List<String> detailFields) {
        Map<String, List<Map<String, Object>>> groups = group(rows, "idSquadra");
        List<Map<String, Object>> out = new ArrayList<>();
        for (List<Map<String, Object>> group : groups.values()) {
            group.sort(Comparator.comparingDouble((Map<String, Object> r) -> number(r.get("giornataDiA")))
                    .thenComparing(r -> string(r.get("idIncontro"))));
            Map<String, Object> first = group.get(0);
            double total = group.stream().mapToDouble(r -> number(r.get("valore"))).sum();
            Map<String, Object> item = ordered("recordId", recordId, "nome", name, "valore", cleanNumber(total),
                    "idSquadra", first.get("idSquadra"), "squadra", first.get("squadra"));
            List<Object> details = new ArrayList<>();
            for (Map<String, Object> r : group) details.add(project(r, detailFields));
            item.put("dettagli", details);
            out.add(item);
        }
        sortValueTeam(out);
        return new ArrayList<>(out);
    }

    private static void sortValueTeam(List<Map<String, Object>> out) {
        out.sort(Comparator.comparingDouble((Map<String, Object> r) -> number(r.get("valore"))).reversed()
                .thenComparing(r -> string(r.get("squadra"))));
    }

    /**
     * Scarta righe tecniche/non disputate presenti in partiteSquadra.
     * Una riga valida deve identificare una vera squadra, un vero incontro
     * e un avversario leggibile. Le righe con idSquadra=0 o nomi vuoti
     * non devono mai entrare nei record Classici/Serie.
     */
    private static List<Map<String, Object>> validTeamMatches(List<Map<String, Object>> matches) {
        List<Map<String, Object>> valid = new ArrayList<>();
        for (Map<String, Object> row : matches) {
            if (longNumber(row.get("idSquadra")) == 0L) continue;
            if (string(row.get("squadra")).isBlank()) continue;
            if (string(row.get("idIncontro")).isBlank()) continue;
            if (string(row.get("avversaria")).isBlank()) continue;
            valid.add(row);
        }
        return valid;
    }

    private static Map<String, List<Map<String, Object>>> group(List<Map<String, Object>> rows, String field) {
        Map<String, List<Map<String, Object>>> groups = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) groups.computeIfAbsent(string(row.get(field)), k -> new ArrayList<>()).add(row);
        return groups;
    }

    private static Map<String, Object> project(Map<String, Object> row, List<String> fields) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (String field : fields) result.put(field, row.get(field));
        return result;
    }

    private static Map<String, Object> ordered(Object... values) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) map.put(String.valueOf(values[i]), values[i + 1]);
        return map;
    }

    private static long longNumber(Object value) {
        String text = string(value);
        if (text.isBlank()) return 0L;
        try {
            return new BigDecimal(text.replace(',', '.')).longValue();
        }
        catch (NumberFormatException ex) {
            return 0L;
        }
    }

    private static Object cleanNumber(double value) {
        if (Math.rint(value) == value) return (long) value;
        return BigDecimal.valueOf(value).stripTrailingZeros();
    }

    private static List<Path> resolveSeasonDirs(Path root, List<String> requested) throws IOException {
        if (!requested.isEmpty()) {
            List<Path> result = new ArrayList<>();
            for (String season : requested) {
                Path dir = root.resolve(season);
                if (!Files.isDirectory(dir)) throw new IOException("Stagione non trovata: " + dir);
                result.add(dir);
            }
            result.sort(Comparator.comparing(p -> p.getFileName().toString()));
            return result;
        }
        try (Stream<Path> stream = Files.list(root)) {
            return stream.filter(Files::isDirectory).sorted(Comparator.comparing(p -> p.getFileName().toString())).toList();
        }
    }

    private static List<Path> listNormalizedFiles(Path seasonDir) throws IOException {
        try (Stream<Path> stream = Files.list(seasonDir)) {
            return stream.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().startsWith("season_normalized_"))
                    .filter(p -> p.getFileName().toString().endsWith(".json"))
                    .filter(p -> !p.getFileName().toString().contains(".stage"))
                    .filter(p -> !p.getFileName().toString().contains(".final"))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString())).toList();
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> rows(Object value) {
        if (!(value instanceof List<?> list)) return new ArrayList<>();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : list) if (item instanceof Map<?, ?> map) result.add((Map<String, Object>) map);
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> object(Object value, Path source, String name) throws IOException {
        if (!(value instanceof Map<?, ?> map)) throw new IOException("Oggetto " + name + " mancante in " + source);
        return (Map<String, Object>) map;
    }

    private static String string(Object value) { return value == null ? "" : String.valueOf(value); }
    private static double number(Object value) {
        if (value instanceof Number n) return n.doubleValue();
        if (value == null || string(value).isBlank()) return 0;
        try { return Double.parseDouble(string(value).replace(',', '.')); } catch (NumberFormatException e) { return 0; }
    }

    private static Object parse(Path source) throws IOException {
        String text = Files.readString(source, StandardCharsets.UTF_8);
        if (!text.isEmpty() && text.charAt(0) == '\uFEFF') text = text.substring(1);
        return new JsonParser(text, source).parse();
    }

    public record Result(int seasons, int competitions) {}

    private static final class JsonParser {
        private final String text; private final Path source; private int index;
        JsonParser(String text, Path source) { this.text = text; this.source = source; }
        Object parse() throws IOException { skip(); Object v = value(); skip(); if (index != text.length()) fail("contenuto dopo JSON"); return v; }
        private Object value() throws IOException {
            skip(); if (index >= text.length()) fail("fine inattesa"); char c = text.charAt(index);
            return switch (c) { case '{' -> object(); case '[' -> array(); case '"' -> string(); case 't' -> literal("true", true); case 'f' -> literal("false", false); case 'n' -> literal("null", null); default -> number(); };
        }
        private Map<String,Object> object() throws IOException { expect('{'); Map<String,Object> m=new LinkedHashMap<>(); skip(); if (take('}')) return m; while(true){ skip(); String k=string(); skip(); expect(':'); m.put(k,value()); skip(); if(take('}')) return m; expect(','); } }
        private List<Object> array() throws IOException { expect('['); List<Object> a=new ArrayList<>(); skip(); if(take(']')) return a; while(true){ a.add(value()); skip(); if(take(']')) return a; expect(','); } }
        private String string() throws IOException { expect('"'); StringBuilder b=new StringBuilder(); while(index<text.length()){ char c=text.charAt(index++); if(c=='"') return b.toString(); if(c!='\\'){ b.append(c); continue;} if(index>=text.length()) fail("escape incompleto"); char e=text.charAt(index++); switch(e){case '"','\\','/'->b.append(e); case 'b'->b.append('\b'); case 'f'->b.append('\f'); case 'n'->b.append('\n'); case 'r'->b.append('\r'); case 't'->b.append('\t'); case 'u'->{ if(index+4>text.length()) fail("unicode incompleto"); b.append((char)Integer.parseInt(text.substring(index,index+4),16)); index+=4;} default->fail("escape non valido");}} fail("stringa non chiusa"); return ""; }
        private Object number() throws IOException { int s=index; if(peek('-')) index++; digits(); boolean dec=false; if(peek('.')){dec=true;index++;digits();} if(peek('e')||peek('E')){dec=true;index++;if(peek('+')||peek('-'))index++;digits();} String raw=text.substring(s,index); try{ BigDecimal d=new BigDecimal(raw); if(!dec && d.scale()<=0) try{return d.longValueExact();}catch(ArithmeticException ignored){} return d.stripTrailingZeros(); }catch(Exception e){fail("numero non valido"); return null;} }
        private void digits() throws IOException { int s=index; while(index<text.length()&&Character.isDigit(text.charAt(index))) index++; if(index==s) fail("cifre attese"); }
        private Object literal(String l,Object v)throws IOException{ if(!text.startsWith(l,index)) fail("letterale non valido"); index+=l.length(); return v; }
        private void skip(){ while(index<text.length()&&Character.isWhitespace(text.charAt(index))) index++; }
        private boolean take(char c){ if(index<text.length()&&text.charAt(index)==c){index++;return true;} return false; }
        private void expect(char c)throws IOException{ if(!take(c)) fail("atteso '"+c+"'"); }
        private boolean peek(char c){ return index<text.length()&&text.charAt(index)==c; }
        private void fail(String m)throws IOException{ throw new IOException("JSON non valido in "+source+" posizione "+index+": "+m); }
    }

    private static final class JsonWriter {
        static String writePretty(Object v){ StringBuilder b=new StringBuilder(); write(v,b,0); return b.toString(); }
        private static void write(Object v,StringBuilder b,int depth){
            if(v==null){b.append("null");return;} if(v instanceof String s){quote(s,b);return;} if(v instanceof Boolean){b.append(v);return;} if(v instanceof Number n){b.append(n instanceof BigDecimal d?d.stripTrailingZeros().toPlainString():n);return;}
            if(v instanceof Map<?,?> m){ b.append('{'); if(!m.isEmpty()){ b.append('\n'); int i=0; for(var e:m.entrySet()){ indent(b,depth+1); quote(String.valueOf(e.getKey()),b); b.append(": "); write(e.getValue(),b,depth+1); if(++i<m.size()) b.append(','); b.append('\n'); } indent(b,depth);} b.append('}'); return; }
            if(v instanceof List<?> l){ b.append('['); if(!l.isEmpty()){ b.append('\n'); for(int i=0;i<l.size();i++){ indent(b,depth+1); write(l.get(i),b,depth+1); if(i+1<l.size()) b.append(','); b.append('\n'); } indent(b,depth);} b.append(']'); return; }
            quote(String.valueOf(v),b);
        }
        private static void indent(StringBuilder b,int d){ b.append("  ".repeat(d)); }
        private static void quote(String s,StringBuilder b){ b.append('"'); for(int i=0;i<s.length();i++){ char c=s.charAt(i); switch(c){case '"'->b.append("\\\"");case '\\'->b.append("\\\\");case '\b'->b.append("\\b");case '\f'->b.append("\\f");case '\n'->b.append("\\n");case '\r'->b.append("\\r");case '\t'->b.append("\\t");default->{if(c<0x20)b.append(String.format("\\u%04x",(int)c));else b.append(c);}}} b.append('"'); }
    }
}
