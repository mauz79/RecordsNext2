package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.config.MiniJson;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Staging from real 21-season export; strict site cutoff and approved history only. */
public final class TopFlopScopedPublicationSmokeTest {
    @SuppressWarnings("unchecked")
    public static void main(String[] args)throws Exception {
        Path from=Path.of(args[0]);
        var prepared=new TopFlopViewsPublication.Prepared(from,
            Set.of("2013_2014","2014_2015","2015_2016","2016_2017","2017_2018","2018_2019","2019_2020","2020_2021"), 85,
            Map.of("2015_2016:315","rn4p-2013_2014-304", "2016_2017:302","rn4p-2013_2014-304",
                "2017_2018:265","rn4p-2013_2014-304"));
        Path dest=Files.createTempDirectory("rn4-publish-scoped-");
        try {
            int count=TopFlopViewsPublication.attachScoped(prepared,
                List.of("2015_2016","2016_2017"),dest);
            if(count<2)throw new AssertionError("No JS attached");
            String facade=Files.readString(dest.resolve(TopFlopPublicViewsStageExporter.FACADE));
            int begin=facade.indexOf('{');int end=facade.lastIndexOf("};");
            if(begin<0||end<begin)throw new AssertionError("No facade");
            Map<String,Object> meta=(Map<String,Object>)MiniJson.parse(facade.substring(begin,end+1));
            if(((List<?>)meta.get("stagioni")).size()!=2)throw new AssertionError("Historical cutoff leaked");
            Map<String,Object> historical=(Map<String,Object>)meta.get("storico");
            if(((Number)historical.get("identita")).intValue()!=1)throw new AssertionError("Approved identity not present: "+historical);
            List<Map<String,Object>> historyFiles=(List<Map<String,Object>>)historical.get("file");
            if(historyFiles.isEmpty())throw new AssertionError("History file missing");
            Path history=dest.resolve((String)historyFiles.get(0).get("nome"));
            String js=Files.readString(history,StandardCharsets.UTF_8);
            if(!js.contains("2015_2016:315")||!js.contains("2016_2017:302")||js.contains("2017_2018:265"))
                throw new AssertionError("Approved historical cutoff incorrect");
            if(TopFlopViewsPublication.validateStaging(dest)!=count)
                throw new AssertionError("JS count mismatch");
            System.out.println("RN4_SCOPED_PUBLISH_OK files="+count+" careers=1 seasonScope=2");
        } finally {
            try(var stream=Files.list(dest)){for(Path file:stream.toList())Files.deleteIfExists(file);}
            Files.deleteIfExists(dest);
        }
    }
}
