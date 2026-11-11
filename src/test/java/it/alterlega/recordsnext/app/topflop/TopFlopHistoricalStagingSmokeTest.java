package it.alterlega.recordsnext.app.topflop;
import java.nio.file.*;
import java.util.*;
public final class TopFlopHistoricalStagingSmokeTest {
    public static void main(String[] args)throws Exception {
        Path js=Path.of(args[0]);
        List<Path> files;
        try(var s=Files.list(js)){
            files=s.filter(p->p.getFileName().toString().matches(
                "fcmRecordsNext_TopFlopViews\\.Careers\\.[0-9]{4}_[0-9]{4}\\.[0-9]{3}\\.js"))
                .sorted().toList();
        }
        var careers=TopFlopHistoricalViews.readCareers(js,files);
        if(careers.size()!=16262)throw new AssertionError("Actual career rows="+careers.size());
        var higuain=careers.stream().filter(x->String.valueOf(x.get("nome")).startsWith("HIGUAIN Gonzalo"))
                .toList();
        if(higuain.size()<4)throw new AssertionError("Higuain seasons missing "+higuain.size());
        Map<String,String> approved=new HashMap<>();
        for(Map<String,Object> x:higuain)approved.put(String.valueOf(x.get("playerKey")),"rn4p-2013_2014-304");
        var history=TopFlopHistoricalViews.aggregate(careers,approved);
        if(history.size()!=1)throw new AssertionError("Historic groups "+history.size());
        var row=history.get(0);
        if((int)row.get("numeroStagioni")!=higuain.size())throw new AssertionError("Missing higuain years");
        Path folder=Files.createTempDirectory("rn4-historic-shards-");
        try {
            var out=TopFlopPublicViewsStageExporter.shard(folder,"Storico","History",new ArrayList<Object>(history));
            if(out.size()!=1||out.get(0).bytes()>1_500_000)throw new AssertionError("Historic shard size");
        } finally {
            try(var s=Files.list(folder)){for(Path f:s.toList())Files.deleteIfExists(f);}
            Files.deleteIfExists(folder);
        }
        System.out.println("RN4_REAL_STAGING_HISTORY_OK seasons="+files.size()+
            " careers="+careers.size()+" higuainSeasons="+higuain.size()+
            " higuainRealVotes="+row.get("votiRealiValidi"));
    }
}
