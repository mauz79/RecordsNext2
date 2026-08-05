package it.alterlega.recordsnext;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

/** Costruisce riserveufficio.json dai season_normalized_*.json RecordsNext. */
public final class RiserveUfficioArchiveBuilder {
    private RiserveUfficioArchiveBuilder() {}

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Uso: RiserveUfficioArchiveBuilder <reportsRoot> <archiveRoot> [stagione ...]");
            System.exit(2);
        }
        Path reports = Path.of(args[0]).toAbsolutePath().normalize();
        Path archive = Path.of(args[1]).toAbsolutePath().normalize();
        List<String> seasons = new ArrayList<>();
        for (int i = 2; i < args.length; i++) if (!args[i].isBlank()) seasons.add(args[i].trim());
        Result r = build(reports, archive, seasons);
        System.out.println("Report       : " + reports);
        System.out.println("Archivio RU  : " + archive);
        System.out.println("Stagioni     : " + r.seasons());
        System.out.println("File letti   : " + r.files());
        System.out.println("Righe RU     : " + r.reserveRows());
        System.out.println("Viste        : 12/12");
    }

    public static Result build(Path reportsRoot, Path archiveRoot, List<String> requested) throws IOException {
        if (!Files.isDirectory(reportsRoot)) throw new IOException("Cartella report non trovata: " + reportsRoot);
        Files.createDirectories(archiveRoot);
        int seasonCount = 0, fileCount = 0, rowCount = 0;
        for (Path seasonDir : resolveSeasonDirs(reportsRoot, requested)) {
            List<Path> files = listNormalizedFiles(seasonDir);
            if (files.isEmpty()) continue;
            String season = seasonDir.getFileName().toString();
            Map<String,Object> payload = buildSeason(season, files);
            Path outDir = archiveRoot.resolve(season);
            Files.createDirectories(outDir);
            Files.writeString(outDir.resolve("riserveufficio.json"), JsonWriter.writePretty(payload) + System.lineSeparator(),
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            seasonCount++; fileCount += files.size();
            rowCount += rows(object(payload.get("dettaglio")).get("ruDettaglio")).size();
        }
        if (seasonCount == 0) throw new IOException("Nessuna stagione normalizzata trovata");
        return new Result(seasonCount, fileCount, rowCount);
    }

    private static Map<String,Object> buildSeason(String season, List<Path> files) throws IOException {
        List<Map<String,Object>> ruDetail = new ArrayList<>();
        Map<String,Map<String,Object>> matchByKey = new LinkedHashMap<>();
        Map<String,List<Map<String,Object>>> bandsByCompetition = new LinkedHashMap<>();
        Set<String> competitions = new TreeSet<>();

        for (Path file : files) {
            Map<String,Object> doc = object(parse(file));
            Map<String,Object> meta = object(doc.get("meta"));
            String compName = publicCompetitionName(s(meta.get("competizioneNome")));
            String compId = s(meta.get("idCompetizioneFcm"));
            competitions.add(compName);
            List<Map<String,Object>> matches = rows(doc.get("partiteSquadra"));
            for (Map<String,Object> m : matches) {
                if (n(m.get("idSquadra")) == 0) continue;
                matchByKey.put(s(m.get("idIncontro")) + "|" + s(m.get("idSquadra")), m);
            }
            bandsByCompetition.put(compId, rows(doc.get("fasceGolDettaglio")));
            for (Map<String,Object> raw : rows(doc.get("riserveUfficioDettaglio"))) {
                Map<String,Object> match = matchByKey.get(s(raw.get("idIncontro")) + "|" + s(raw.get("idSquadra")));
                if (match == null) continue;
                ruDetail.add(detailRow(raw, match, compName));
            }
        }

        ruDetail.sort(compare("competizione", "giornataDiA", "idIncontro", "idSquadra", "ordine"));
        List<Map<String,Object>> teamMatch = teamMatch(ruDetail);
        List<Map<String,Object>> against = against(teamMatch, matchByKey);
        List<Map<String,Object>> decisive = new ArrayList<>(), decisiveAgainst = new ArrayList<>();
        calculateDecisive(teamMatch, bandsByCompetition, decisive, decisiveAgainst);

        Map<String,Object> views = new LinkedHashMap<>();
        views.put("partiteConPiuRU", matchesWithMostRu(ruDetail));
        views.put("partiteConRU", sorted(teamMatch, compare("competizione", "giornataDiA", "squadra")));
        views.put("partiteControRU", sorted(against, compare("competizione", "giornataDiA", "squadra")));
        views.put("ruDecisiva", decisive);
        views.put("bilancioRUDecisiva", decisiveBalance(decisive));
        views.put("ruDecisivaContro", decisiveAgainst);
        views.put("bilancioRUDecisivaContro", decisiveAgainstBalance(decisiveAgainst));
        List<Map<String,Object>> balanceWith = balance(teamMatch, true);
        List<Map<String,Object>> balanceAgainst = balance(against, false);
        views.put("bilancioConRU", balanceWith);
        views.put("bilancioControRU", balanceAgainst);
        views.put("mediaPuntiConRU", averagePoints(balanceWith, true));
        views.put("mediaPuntiControRU", averagePoints(balanceAgainst, false));
        views.put("tipoRUUsata", typeUsed(ruDetail));

        Map<String,Object> detail = new LinkedHashMap<>();
        detail.put("ruDettaglio", ruDetail);
        detail.put("ruTeamMatch", teamMatch);

        stripInternalFields(views);
        stripInternalFields(detail);
        Map<String,Object> meta = linked(
                "titolo", "Riserve d'Ufficio",
                "stagione", season,
                "generato", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                "builder", "RecordsNext RiserveUfficioArchiveBuilder");
        Map<String,Object> out = new LinkedHashMap<>();
        out.put("meta", meta);
        out.put("competizioni", canonicalCompetitions());
        out.put("curiosita", curiosityDefinitions());
        out.put("views", views);
        out.put("dettaglio", detail);
        return out;
    }

    private static Map<String,Object> detailRow(Map<String,Object> r, Map<String,Object> m, String compName) {
        String side = s(m.get("lato"));
        String result = n(m.get("golFatti")) + "-" + n(m.get("golSubiti"));
        String score = plainText(m.get("puntiFatti")) + "-" + plainText(m.get("puntiSubiti"));
        String team = s(r.get("squadra"));
        String opponent = s(r.get("avversaria"));
        String homeTeam = "CASA".equalsIgnoreCase(side) ? team : opponent;
        String awayTeam = "CASA".equalsIgnoreCase(side) ? opponent : team;
        Object homePoints = "CASA".equalsIgnoreCase(side) ? m.get("puntiFatti") : m.get("puntiSubiti");
        Object awayPoints = "CASA".equalsIgnoreCase(side) ? m.get("puntiSubiti") : m.get("puntiFatti");
        int homeGoals = "CASA".equalsIgnoreCase(side) ? n(m.get("golFatti")) : n(m.get("golSubiti"));
        int awayGoals = "CASA".equalsIgnoreCase(side) ? n(m.get("golSubiti")) : n(m.get("golFatti"));
        return linked(
                "idIncontro", s(r.get("idIncontro")), "competizione", compName,
                "girone", publicGroupName(compName, m.get("gironeNome"), m.get("idGirone")), "giornataFCM", m.get("giornata"),
                "giornataDiA", m.get("giornataDiA"), "urlTabellino", m.get("urlTabellino"),
                "idSquadra", s(r.get("idSquadra")), "squadra", r.get("squadra"),
                "idAvversaria", s(r.get("idAvversaria")), "avversaria", r.get("avversaria"),
                "tipoRU", r.get("tipoRU"), "ruoloRU", r.get("ruoloRU"), "valoreRU", r.get("valoreRU"),
                "ordine", r.get("ordine"), "votoTabellino", r.get("votoTabellino"),
                "modifTabellino", r.get("modifTabellino"), "totTabellino", r.get("totTabellino"),
                "puntiSquadra", m.get("puntiFatti"), "puntiAvversaria", m.get("puntiSubiti"),
                "golSquadra", m.get("golFatti"), "golAvversaria", m.get("golSubiti"),
                "risultato", result, "punteggio", score, "esito", legacyOutcome(s(m.get("esito"))),
                "_idCompetizioneFcm", m.get("idCompetizioneFcm"), "_lato", side,
                "_squadraCasa", homeTeam, "_squadraFuori", awayTeam,
                "_puntiCasa", homePoints, "_puntiFuori", awayPoints,
                "_golCasa", homeGoals, "_golFuori", awayGoals);
    }

    private static List<Map<String,Object>> teamMatch(List<Map<String,Object>> detail) {
        Map<String,List<Map<String,Object>>> groups = group(detail, r -> s(r.get("idIncontro")) + "|" + s(r.get("idSquadra")));
        List<Map<String,Object>> out = new ArrayList<>();
        for (List<Map<String,Object>> g : groups.values()) {
            Map<String,Object> f = g.get(0); LinkedHashSet<String> types = new LinkedHashSet<>();
            BigDecimal total = BigDecimal.ZERO; List<String> pieces = new ArrayList<>();
            for (Map<String,Object> r : g) { String type=s(r.get("tipoRU")); types.add(type); total=total.add(bd(r.get("valoreRU"))); pieces.add(type+"="+plain(r.get("valoreRU"))); }
            Map<String,Object> row = copyFields(f, "idIncontro","competizione","girone","giornataFCM","giornataDiA","urlTabellino","idSquadra","squadra","idAvversaria","avversaria");
            row.put("numeroRU", g.size()); row.put("valoreRUTotale", clean(total)); row.put("tipiRU", String.join(", ", types)); row.put("dettaglioRU", String.join("; ", pieces));
            copyInto(row, f, "puntiSquadra","puntiAvversaria","golSquadra","golAvversaria","risultato","punteggio","esito",
                    "_idCompetizioneFcm","_lato","_squadraCasa","_squadraFuori","_puntiCasa","_puntiFuori","_golCasa","_golFuori"); out.add(row);
        }
        return out;
    }

    private static List<Map<String,Object>> against(List<Map<String,Object>> teamMatch, Map<String,Map<String,Object>> matchByKey) {
        List<Map<String,Object>> out = new ArrayList<>();
        for (Map<String,Object> ru : teamMatch) {
            Map<String,Object> opp = matchByKey.get(s(ru.get("idIncontro")) + "|" + s(ru.get("idAvversaria")));
            if (opp == null) continue;
            out.add(linked("idIncontro",ru.get("idIncontro"),"competizione",ru.get("competizione"),"girone",ru.get("girone"),"giornataFCM",opp.get("giornata"),"giornataDiA",opp.get("giornataDiA"),"urlTabellino",opp.get("urlTabellino"),
                    "idSquadra",s(opp.get("idSquadra")),"squadra",opp.get("squadra"),"idAvversaria",ru.get("idSquadra"),"avversaria",ru.get("squadra"),"avversariaConRU",ru.get("squadra"),
                    "numeroRUAvversaria",ru.get("numeroRU"),"valoreRUAvversaria",ru.get("valoreRUTotale"),"tipiRUAvversaria",ru.get("tipiRU"),"dettaglioRUAvversaria",ru.get("dettaglioRU"),
                    "puntiSquadra",opp.get("puntiFatti"),"puntiAvversaria",opp.get("puntiSubiti"),"golSquadra",opp.get("golFatti"),"golAvversaria",opp.get("golSubiti"),
                    "risultato",n(opp.get("golFatti"))+"-"+n(opp.get("golSubiti")),
                    "punteggio",plainText(opp.get("puntiFatti"))+"-"+plainText(opp.get("puntiSubiti")),
                    "esito",legacyOutcome(s(opp.get("esito")))));
        }
        return out;
    }

    private static void calculateDecisive(List<Map<String,Object>> teamMatch, Map<String,List<Map<String,Object>>> bands, List<Map<String,Object>> yes, List<Map<String,Object>> against) {
        for (Map<String,Object> ru : teamMatch) {
            BigDecimal with = bd(ru.get("puntiSquadra")), value = bd(ru.get("valoreRUTotale")), without = with.subtract(value);
            int goalsWith=n(ru.get("golSquadra")), goalsOpp=n(ru.get("golAvversaria"));
            int goalsWithout=goals(without, bands.getOrDefault(s(ru.get("_idCompetizioneFcm")), List.of()));
            String resultWith=result(goalsWith, goalsOpp), resultWithout=result(goalsWithout, goalsOpp);
            int gained=points(resultWith)-points(resultWithout); if (gained<=0) continue;
            Map<String,Object> y=copyFields(ru,"idIncontro","competizione","girone","giornataFCM","giornataDiA","urlTabellino","idSquadra","squadra","idAvversaria","avversaria","tipiRU","dettaglioRU");
            y.put("valoreRU",clean(value)); y.put("puntiConRU",clean(with)); y.put("puntiSenzaRU",clean(without)); y.put("puntiAvversaria",ru.get("puntiAvversaria")); y.put("golConRU",goalsWith); y.put("golSenzaRU",goalsWithout); y.put("golAvversaria",goalsOpp); y.put("esitoSenzaRU",resultWithout); y.put("esitoConRU",resultWith); y.put("effetto",effect(resultWithout,resultWith)); y.put("puntiClassificaGuadagnati",gained); y.put("risultatoReale",ru.get("risultato")); y.put("punteggioReale",ru.get("punteggio")); yes.add(y);
            String oppWithout=result(goalsOpp, goalsWithout), oppWith=result(goalsOpp, goalsWith); int lost=points(oppWithout)-points(oppWith);
            against.add(linked("idIncontro",ru.get("idIncontro"),"competizione",ru.get("competizione"),"girone",ru.get("girone"),"giornataFCM",ru.get("giornataFCM"),"giornataDiA",ru.get("giornataDiA"),"urlTabellino",ru.get("urlTabellino"),
                    "idSquadra",ru.get("idAvversaria"),"squadra",ru.get("avversaria"),"idAvversaria",ru.get("idSquadra"),"avversaria",ru.get("squadra"),"avversariaConRU",ru.get("squadra"),"tipiRUAvversaria",ru.get("tipiRU"),"dettaglioRUAvversaria",ru.get("dettaglioRU"),"valoreRUAvversaria",clean(value),
                    "puntiSquadra",ru.get("puntiAvversaria"),"puntiAvversariaConRU",clean(with),"puntiAvversariaSenzaRU",clean(without),"golSquadra",goalsOpp,"golAvversariaConRU",goalsWith,"golAvversariaSenzaRU",goalsWithout,
                    "esitoSenzaRUAvversaria",oppWithout,"esitoConRUAvversaria",oppWith,"danno",damage(oppWithout,oppWith),"puntiClassificaPersi",lost,"risultatoReale",goalsOpp+"-"+goalsWith,"punteggioReale",plain(ru.get("puntiAvversaria"))+"-"+plain(with)));
        }
    }

    private static List<Map<String,Object>> matchesWithMostRu(List<Map<String,Object>> detail) {
        List<Map<String,Object>> out = new ArrayList<>();
        for (List<Map<String,Object>> g : group(detail, r -> s(r.get("idIncontro"))).values()) {
            g.sort(Comparator.<Map<String,Object>,BigDecimal>comparing(r -> bd(r.get("idSquadra"))).thenComparing(r -> bd(r.get("ordine"))));
            Map<String,Object> f = g.get(0);
            BigDecimal total = BigDecimal.ZERO;
            List<String> pieces = new ArrayList<>();
            for (Map<String,Object> r : g) {
                total = total.add(bd(r.get("valoreRU")));
                pieces.add(r.get("squadra") + ":" + r.get("tipoRU") + "=" + plain(r.get("valoreRU")));
            }
            out.add(linked("idIncontro",f.get("idIncontro"),"competizione",f.get("competizione"),"girone",f.get("girone"),
                    "giornataFCM",f.get("giornataFCM"),"giornataDiA",f.get("giornataDiA"),
                    "partita",f.get("squadra")+" - "+f.get("avversaria"),
                    "numeroRU",g.size(),"valoreRUTotale",clean(total),"dettaglioRU",String.join("; ",pieces),
                    "risultato",f.get("risultato"),
                    "punteggio",plain(f.get("puntiSquadra"))+"-"+plain(f.get("puntiAvversaria"))));
        }
        out.sort(compareDesc("numeroRU","valoreRUTotale","competizione","giornataDiA"));
        return out;
    }

    private static List<Map<String,Object>> balance(List<Map<String,Object>> source, boolean with) {
        List<Map<String,Object>> out=new ArrayList<>();
        for(List<Map<String,Object>> g:group(source,r->s(r.get("idSquadra"))).values()){Map<String,Object> f=g.get(0);int v=0,d=0,l=0;BigDecimal pf=BigDecimal.ZERO,pa=BigDecimal.ZERO,gf=BigDecimal.ZERO,ga=BigDecimal.ZERO;for(Map<String,Object> r:g){switch(s(r.get("esito"))){case"V"->v++;case"N"->d++;case"P"->l++;}pf=pf.add(bd(r.get("puntiSquadra")));pa=pa.add(bd(r.get("puntiAvversaria")));gf=gf.add(bd(r.get("golSquadra")));ga=ga.add(bd(r.get("golAvversaria")));}int count=g.size();
            out.add(linked("idSquadra",f.get("idSquadra"),"squadra",f.get("squadra"),with?"partiteConRU":"partiteControRU",count,"V",v,"N",d,"P",l,"percV",percent(v,count),"percN",percent(d,count),"percP",percent(l,count),"mediaPuntiSquadra",average(pf,count),"mediaPuntiAvversaria",average(pa,count),"mediaGolSquadra",average(gf,count),"mediaGolAvversaria",average(ga,count)));}
        out.sort(Comparator.<Map<String,Object>>comparingInt(r->n(r.get(with?"partiteConRU":"partiteControRU"))).reversed().thenComparing(r->s(r.get("squadra"))));return out;
    }

    private static List<Map<String,Object>> averagePoints(List<Map<String,Object>> balance, boolean with){List<Map<String,Object>>out=new ArrayList<>();for(Map<String,Object>r:balance)out.add(linked("idSquadra",r.get("idSquadra"),"squadra",r.get("squadra"),with?"partiteConRU":"partiteControRU",r.get(with?"partiteConRU":"partiteControRU"),"mediaPuntiSquadra",r.get("mediaPuntiSquadra"),"mediaPuntiAvversaria",r.get("mediaPuntiAvversaria"),"differenzaMedia",clean(bd(r.get("mediaPuntiSquadra")).subtract(bd(r.get("mediaPuntiAvversaria"))))));out.sort(Comparator.<Map<String,Object>,BigDecimal>comparing(r->bd(r.get("mediaPuntiSquadra"))).reversed().thenComparing(r->s(r.get("squadra"))));return out;}

    private static List<Map<String,Object>> typeUsed(List<Map<String,Object>> detail){List<Map<String,Object>>out=new ArrayList<>();for(List<Map<String,Object>>g:group(detail,r->s(r.get("idSquadra"))).values()){Map<String,Object>f=g.get(0);Map<String,Integer>c=new HashMap<>();Map<String,BigDecimal>v=new HashMap<>();for(String t:List.of("PU","DU","CU","AU")){c.put(t,0);v.put(t,BigDecimal.ZERO);}for(Map<String,Object>r:g){String t=s(r.get("tipoRU"));c.put(t,c.getOrDefault(t,0)+1);v.put(t,v.getOrDefault(t,BigDecimal.ZERO).add(bd(r.get("valoreRU"))));}out.add(linked("idSquadra",f.get("idSquadra"),"squadra",f.get("squadra"),"PU",c.get("PU"),"DU",c.get("DU"),"CU",c.get("CU"),"AU",c.get("AU"),"totaleRU",g.size(),"valorePU",clean(v.get("PU")),"valoreDU",clean(v.get("DU")),"valoreCU",clean(v.get("CU")),"valoreAU",clean(v.get("AU")),"valoreTotale",clean(v.values().stream().reduce(BigDecimal.ZERO,BigDecimal::add))));}out.sort(Comparator.<Map<String,Object>>comparingInt(r->n(r.get("totaleRU"))).reversed().thenComparing(r->s(r.get("squadra"))));return out;}

    private static List<Map<String,Object>> decisiveBalance(List<Map<String,Object>> rows){List<Map<String,Object>>out=new ArrayList<>();for(List<Map<String,Object>>g:group(rows,r->s(r.get("idSquadra"))).values()){Map<String,Object>f=g.get(0);int w=(int)g.stream().filter(r->"V".equals(s(r.get("esitoConRU")))).count(),d=(int)g.stream().filter(r->"N".equals(s(r.get("esitoConRU")))).count();out.add(linked("idSquadra",f.get("idSquadra"),"squadra",f.get("squadra"),"partiteRUDecisiva",g.size(),"vittorieGrazieRU",w,"pareggiGrazieRU",d,"puntiClassificaGuadagnati",g.stream().mapToInt(r->n(r.get("puntiClassificaGuadagnati"))).sum()));}out.sort(compareDesc("partiteRUDecisiva","puntiClassificaGuadagnati","squadra"));return out;}
    private static List<Map<String,Object>> decisiveAgainstBalance(List<Map<String,Object>> rows){List<Map<String,Object>>out=new ArrayList<>();for(List<Map<String,Object>>g:group(rows,r->s(r.get("idSquadra"))).values()){Map<String,Object>f=g.get(0);int w=(int)g.stream().filter(r->"V".equals(s(r.get("esitoSenzaRUAvversaria")))&&!"V".equals(s(r.get("esitoConRUAvversaria")))).count(),d=(int)g.stream().filter(r->"N".equals(s(r.get("esitoSenzaRUAvversaria")))&&"P".equals(s(r.get("esitoConRUAvversaria")))).count();out.add(linked("idSquadra",f.get("idSquadra"),"squadra",f.get("squadra"),"partiteControRUDecisiva",g.size(),"vittoriePerse",w,"pareggiDiventatiSconfitte",d,"puntiClassificaPersi",g.stream().mapToInt(r->n(r.get("puntiClassificaPersi"))).sum()));}out.sort(compareDesc("partiteControRUDecisiva","puntiClassificaPersi","squadra"));return out;}

    private static int goals(BigDecimal score,List<Map<String,Object>>bands){int g=0;List<Map<String,Object>>sorted=new ArrayList<>(bands);sorted.sort(Comparator.comparing(r->bd(r.get("min"))));for(Map<String,Object>b:sorted)if(score.compareTo(bd(b.get("min")))>=0)g=n(b.get("gol"));return g;}
    private static String result(int a,int b){return a>b?"V":a<b?"P":"N";} private static int points(String r){return "V".equals(r)?3:"N".equals(r)?1:0;}
    private static String effect(String a,String b){if("P".equals(a)&&"N".equals(b))return"Da sconfitta a pareggio";if("N".equals(a)&&"V".equals(b))return"Da pareggio a vittoria";if("P".equals(a)&&"V".equals(b))return"Da sconfitta a vittoria";return"";}
    private static String damage(String a,String b){if("V".equals(a)&&"N".equals(b))return"Da vittoria a pareggio";if("N".equals(a)&&"P".equals(b))return"Da pareggio a sconfitta";if("V".equals(a)&&"P".equals(b))return"Da vittoria a sconfitta";return"";}

    @SuppressWarnings("unchecked")
    private static void stripInternalFields(Object value){
        if(value instanceof Map<?,?> map){
            ((Map<String,Object>)map).keySet().removeIf(k->k.startsWith("_"));
            for(Object child:((Map<String,Object>)map).values())stripInternalFields(child);
        }else if(value instanceof List<?> list){for(Object child:list)stripInternalFields(child);}
    }
    private static String legacyOutcome(String value){return switch(value){case "V"->"V";case "P"->"N";case "S"->"P";default->value;};}
    private static String plainText(Object value){return plain(value).replace('.',',');}
    private static String publicCompetitionName(String name){return switch(name){case "Coppa di Lega Serie A"->"Coppa Serie A";case "Coppa di Lega Serie B"->"Coppa Serie B";case "Coppa di Lega Serie C"->"Coppa Serie C";default->name;};}
    private static String publicGroupName(String competition,Object groupName,Object idGirone){String name=s(groupName).trim();if(!name.isEmpty())return name;return competition;}
    private static List<String> canonicalCompetitions(){return List.of("Coppa Serie A","Coppa Serie B","Coppa Serie C","Coppa tra le Coppe","Europa Pipps","Play Off - Play Out","Serie A","Serie B","Serie C","Supercoppa Serie A","Supercoppa Serie B","Supercoppa Serie C");}
    private static List<Object> curiosityDefinitions(){return List.of();}
    private static List<Path> resolveSeasonDirs(Path root,List<String> requested)throws IOException{List<Path>out=new ArrayList<>();if(requested.isEmpty()){try(Stream<Path>s=Files.list(root)){s.filter(Files::isDirectory).sorted().forEach(out::add);}}else for(String x:requested){Path p=root.resolve(x);if(!Files.isDirectory(p))throw new IOException("Stagione non trovata: "+p);out.add(p);}return out;}
    private static List<Path> listNormalizedFiles(Path dir)throws IOException{try(Stream<Path>s=Files.list(dir)){return s.filter(Files::isRegularFile).filter(p->{String n=p.getFileName().toString();return n.startsWith("season_normalized_")&&n.endsWith(".json")&&!n.contains(".stage")&&!n.contains(".final");}).sorted().toList();}}
    private static <T>List<T>sorted(List<T>src,Comparator<? super T>c){List<T>o=new ArrayList<>(src);o.sort(c);return o;}
    private static Comparator<Map<String,Object>> compare(String...f){return (a,b)->{for(String x:f){int c=cmp(a.get(x),b.get(x));if(c!=0)return c;}return 0;};}
    private static Comparator<Map<String,Object>> compareDesc(String...f){return (a,b)->{for(String x:f){int c=cmp(b.get(x),a.get(x));if(c!=0)return c;}return 0;};}
    private static int cmp(Object a,Object b){if(a instanceof Number||b instanceof Number)return bd(a).compareTo(bd(b));return s(a).compareTo(s(b));}
    private static Map<String,List<Map<String,Object>>>group(List<Map<String,Object>>rows,java.util.function.Function<Map<String,Object>,String>key){Map<String,List<Map<String,Object>>>m=new LinkedHashMap<>();for(Map<String,Object>r:rows)m.computeIfAbsent(key.apply(r),k->new ArrayList<>()).add(r);return m;}
    private static Map<String,Object>copyFields(Map<String,Object>s,String...f){Map<String,Object>o=new LinkedHashMap<>();copyInto(o,s,f);return o;}private static void copyInto(Map<String,Object>o,Map<String,Object>s,String...f){for(String x:f)o.put(x,s.get(x));}
    private static Map<String,Object>linked(Object...v){Map<String,Object>m=new LinkedHashMap<>();for(int i=0;i<v.length;i+=2)m.put(String.valueOf(v[i]),v[i+1]);return m;}
    private static Object average(BigDecimal total,int count){return count==0?0:clean(total.divide(BigDecimal.valueOf(count),2,RoundingMode.HALF_EVEN));}
    private static Object percent(int x,int total){return total==0?0:clean(BigDecimal.valueOf(x*100L).divide(BigDecimal.valueOf(total),1,RoundingMode.HALF_UP));}
    private static Object clean(BigDecimal x){BigDecimal z=x.setScale(Math.min(2,Math.max(0,x.scale())),RoundingMode.HALF_UP).stripTrailingZeros();return z.scale()<=0?z.longValue():z;}
    private static String plain(Object x){return bd(x).stripTrailingZeros().toPlainString();}private static BigDecimal bd(Object x){if(x==null||s(x).isBlank())return BigDecimal.ZERO;return x instanceof BigDecimal b?b:new BigDecimal(s(x).replace(',','.'));}private static int n(Object x){return bd(x).intValue();}private static String s(Object x){return x==null?"":String.valueOf(x);}
    @SuppressWarnings("unchecked")private static Map<String,Object>object(Object x){return x instanceof Map<?,?>?(Map<String,Object>)x:new LinkedHashMap<>();}@SuppressWarnings("unchecked")private static List<Map<String,Object>>rows(Object x){if(!(x instanceof List<?>l))return new ArrayList<>();List<Map<String,Object>>o=new ArrayList<>();for(Object r:l)if(r instanceof Map<?,?>)o.add((Map<String,Object>)r);return o;}

    private static Object parse(Path source) throws IOException {
        String text = Files.readString(source, StandardCharsets.UTF_8);
        if (!text.isEmpty() && text.charAt(0) == '\uFEFF') text = text.substring(1);
        return new JsonParser(text, source).parse();
    }

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

    public record Result(int seasons,int files,int reserveRows){}
}
