package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.config.MiniJson;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Real 21-season staging regression, never approves or publishes identities. */
public final class TopFlopHistoricalFantasySmokeTest {
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        Path js=Path.of(args[0]);
        String facade=Files.readString(js.resolve(TopFlopPublicViewsStageExporter.FACADE),StandardCharsets.UTF_8);
        int begin=facade.indexOf('{'),end=facade.lastIndexOf("};");
        Map<String,Object> meta=(Map<String,Object>)MiniJson.parse(facade.substring(begin,end+1));
        List<Map<String,Object>> seasons=(List<Map<String,Object>>)meta.get("stagioni");
        List<Path> files=new ArrayList<>();
        try(var f=Files.list(js)) {
            files.addAll(f.filter(p->p.getFileName().toString().matches(
                "fcmRecordsNext_TopFlopViews\\.(?:Careers|Teams)\\.[0-9]{4}_[0-9]{4}\\.[0-9]{3}\\.js"))
                .sorted().toList());
        }
        var careers=TopFlopHistoricalViews.readCareers(js,files);
        var teams=TopFlopHistoricalViews.readTeams(js,files,seasons);
        if(careers.size()!=16262)throw new AssertionError("Carriere="+careers.size());
        if(teams.size()!=45787)throw new AssertionError("Contesti="+teams.size());
        var higuain=careers.stream().filter(x->String.valueOf(x.get("nome")).startsWith("HIGUAIN Gonzalo")).toList();
        if(higuain.size()!=8)throw new AssertionError("Stagioni Higuaín="+higuain.size());
        Map<String,String> approved=new HashMap<>();
        for(var c:higuain)approved.put((String)c.get("playerKey"),"rn4p-higuain-test");
        var all=TopFlopHistoricalViews.aggregate(careers,teams,approved);
        if(all.size()!=1)throw new AssertionError("Identita storiche="+all.size());
        var player=all.get(0);
        if((int)player.get("numeroStagioni")!=8)throw new AssertionError("Stagioni incoerenti");
        if((int)player.get("votiRealiValidi")!=217)throw new AssertionError("Voti reali duplicati");
        List<Map<String,Object>> contexts=(List<Map<String,Object>>)player.get("fantasquadrePerStagione");
        long original=teams.stream().filter(t->approved.containsKey(t.get("playerKey"))).count();
        if(contexts.size()!=original || original<8)throw new AssertionError("Contesti non completi "+contexts.size()+" vs "+original);
        for(var t:contexts) {
            if(t.get("fantasquadra")==null||t.get("competizione")==null)throw new AssertionError("Nome assente");
            int n=((Number)t.get("gareConteggiate")).intValue();
            if(n>0) {
                BigDecimal avg=(BigDecimal)t.get("fantamediaApportata");
                BigDecimal sum=(BigDecimal)t.get("sommaFantavotiApportati");
                if(avg.multiply(BigDecimal.valueOf(n)).subtract(sum).abs().compareTo(new BigDecimal("0.00001"))>0)
                    throw new AssertionError("Fantamedia non ponderata correttamente");
            }
        }
        if(player.containsKey("sommaFantapuntiTotali") || player.containsKey("utilizziFantasyTotali"))
            throw new AssertionError("Totale fantasy improprio nella scheda storica");
        var cutCareers=careers.stream().filter(c->((String)c.get("playerKey")).startsWith("2015_2016:") ||
            ((String)c.get("playerKey")).startsWith("2016_2017:")).toList();
        var cutTeams=teams.stream().filter(c->((String)c.get("playerKey")).startsWith("2015_2016:") ||
            ((String)c.get("playerKey")).startsWith("2016_2017:")).toList();
        var cut=TopFlopHistoricalViews.aggregate(cutCareers,cutTeams,approved);
        if(cut.size()!=1|| (int)cut.get(0).get("numeroStagioni")!=2)
            throw new AssertionError("Cutoff storico non rispettato");
        for(var t:(List<Map<String,Object>>)cut.get(0).get("fantasquadrePerStagione"))
            if(!List.of("2015_2016","2016_2017").contains(t.get("stagione")))
                throw new AssertionError("Fantasia futura presente nel sito storico");
        // Full Java-publisher path: real staging -> site cutoff -> real JS History file.
        Path integrated=Files.createTempDirectory("rn4-history-publisher-smoke");
        try {
            var prepared=new TopFlopViewsPublication.Prepared(js,
                seasons.stream().map(m->String.valueOf(m.get("stagione"))).collect(java.util.stream.Collectors.toSet()),
                files.size(),approved);
            int attached=TopFlopViewsPublication.attachScoped(prepared,
                List.of("2015_2016","2016_2017"),integrated);
            if(attached<3 || TopFlopViewsPublication.validateStaging(integrated)!=attached)
                throw new AssertionError("Integrato: file staging mancanti");
            String siteFacade=Files.readString(integrated.resolve(TopFlopPublicViewsStageExporter.FACADE));
            int start=siteFacade.indexOf('{'),stop=siteFacade.lastIndexOf("};");
            Map<String,Object> siteMeta=(Map<String,Object>)MiniJson.parse(siteFacade.substring(start,stop+1));
            Map<String,Object> h=(Map<String,Object>)siteMeta.get("storico");
            if(((Number)h.get("identita")).intValue()!=1)throw new AssertionError("Integrato: identita mancante");
            List<Map<String,Object>> hfiles=(List<Map<String,Object>>)h.get("file");
            if(hfiles.size()!=1)throw new AssertionError("Integrato: numero shard storico errato");
            String historyJs=Files.readString(integrated.resolve((String)hfiles.get(0).get("nome")));
            if(!historyJs.contains("fantasquadrePerStagione") || !historyJs.contains("HIGUAIN"))
                throw new AssertionError("Integrato: storico senza rendimento fantasy");
            if(historyJs.contains("2017_2018:265"))throw new AssertionError("Integrato: leak cutoff futuro");
        }finally {
            try(var stream=Files.list(integrated)){for(Path f:stream.toList())Files.delete(f);}
            Files.delete(integrated);
        }
        Path dir=Files.createTempDirectory("rn4-history-fantasy-smoke");
        try {
            var staged=TopFlopPublicViewsStageExporter.shard(dir,"Storico","History",new ArrayList<Object>(all));
            if(staged.isEmpty())throw new AssertionError("Shard storico mancante");
            for(var shard:staged)if(shard.bytes()>1_500_000)throw new AssertionError("Shard troppo grande");
            String content=Files.readString(dir.resolve(staged.get(0).name()));
            if(!content.contains("fantasquadrePerStagione")||!content.contains("HIGUAIN"))
                throw new AssertionError("Rendimento fantasy non esposto nei JS");
        }finally {
            try(var stream=Files.list(dir)){for(Path f:stream.toList())Files.delete(f);}
            Files.delete(dir);
        }
        System.out.println("RN4_REAL_HISTORY_FANTASY_OK careers="+careers.size()+" teams="+teams.size()+
            " higuainSeasons="+higuain.size()+" contexts="+original+" cutoff=2 voteCount=217");
    }
}
