package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.config.ProcessingConfigLoader;
import it.alterlega.recordsnext.app.config.ProcessingConfigWriter;
import it.alterlega.recordsnext.app.model.CoreRecordCatalog;
import it.alterlega.recordsnext.app.model.RecordFamily;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Independent Java 21 smoke test, works without Maven/JUnit. */
public final class TopFlopNativeIntegrationSmokeTest {
    private TopFlopNativeIntegrationSmokeTest() {}
    private static void check(boolean p, String message){if(!p)throw new AssertionError(message);}
    public static void main(String[] args)throws Exception {
        if(args.length!=3) throw new IllegalArgumentException("<db> <reports> <temp-parent>");
        Path parent=Path.of(args[2]);Files.createDirectories(parent);
        check(CoreRecordCatalog.children().size()==114,"catalog 114");
        Path processing=parent.resolve("processing-native-test.json");
        Map<String,Boolean> children=new HashMap<>();
        for(String id:ProcessingConfigWriter.TOP_FLOP)children.put(id,false);
        children.put("topFlop.tf01",true);
        children.put("topFlop.tf04",true);
        children.put("topFlop.tf17",true);
        children.put("topFlop.tf20",true);
        children.put("topFlop.tf02",false);
        children.put("topFlop.tf11",false);
        ProcessingConfigWriter.State state=new ProcessingConfigWriter.State(
            false,false,false,false,false,true,false,false,children,
            ProcessingConfigWriter.defaultModifierNames());
        ProcessingConfigWriter.save(processing,state);
        check(ProcessingConfigWriter.load(processing).topFlop(),"GUI persisted family");
        var options=ProcessingConfigLoader.load(processing);
        check(options.familyEnabled(RecordFamily.TOP_FLOP),"loader enabled family");
        Set<String> selected=TopFlopViewsPublication.selectedIds(options.selection());
        check(selected.equals(Set.of("TF01","TF04","TF17","TF20")),"granular selections: "+selected);
        Path scratch=parent.resolve("native-source");
        var prepared=TopFlopViewsPublication.prepare(Path.of(args[0]),Path.of(args[1]),
            List.of("2006_2007","2026_2027"),selected,scratch);
        Path old=parent.resolve("old-js"), current=parent.resolve("new-js");
        Files.createDirectories(old);Files.createDirectories(current);
        int oldCount=TopFlopViewsPublication.attachScoped(prepared,List.of("2006_2007"),old);
        int newCount=TopFlopViewsPublication.attachScoped(prepared,List.of("2006_2007","2026_2027"),current);
        check(newCount>oldCount,"cutoff different file count");
        String oldFacade=Files.readString(old.resolve(TopFlopPublicViewsStageExporter.FACADE));
        check(!oldFacade.contains("2026_2027"),"cutoff prevents future seasons");
        String currentFacade=Files.readString(current.resolve(TopFlopPublicViewsStageExporter.FACADE));
        check(currentFacade.contains("2026_2027"),"anchor contains current season");
        check(currentFacade.contains("TF20"),"keepers enabled");
        check(currentFacade.contains("NON_SELEZIONATO"),"disabled IDs marked");
        check(TopFlopViewsPublication.validateStaging(old)==oldCount,"staging verify old");
        check(TopFlopViewsPublication.validateStaging(current)==newCount,"staging verify current");
        String r="";
        try(var files=Files.list(current)){
            for(Path file:files.filter(p->p.getFileName().toString().contains(".Records.")).toList())
                r+=Files.readString(file);
        }
        check(r.contains("\\\"recordId\\\":\\\"TF01\\\"") || r.contains("\"recordId\":\"TF01\""),"TF01 exported");
        check(!r.contains("\"recordId\":\"TF02\""),"TF02 not exported");
        boolean badScope=false;
        try{TopFlopViewsPublication.attachScoped(prepared,List.of("2019_2020"),parent.resolve("nowhere"));}
        catch(Exception expected){badScope=true;}
        check(badScope,"out-of-scope rejected");
        Path oversized=parent.resolve("oversized");Files.createDirectories(oversized);
        Files.write(oversized.resolve("oversize.js"),new byte[1_500_001]);
        boolean rejected=false;
        try{TopFlopViewsPublication.validateStaging(oversized);}catch(Exception expected){rejected=true;}
        check(rejected,"oversize rejected");
        Map<String,Boolean> unsupported=new HashMap<>(children);unsupported.put("topFlop.tf11",true);
        ProcessingConfigWriter.State wrong=new ProcessingConfigWriter.State(false,false,false,false,false,true,
           false,false,unsupported,ProcessingConfigWriter.defaultModifierNames());
        ProcessingConfigWriter.save(processing,wrong);
        // Unsupported selections are forcibly disabled on save, never emitted as invented results.
        check(!ProcessingConfigWriter.load(processing).childEnabled("topFlop.tf11"),"pending TF disabled");
        System.out.println("TOP_FLOP_NATIVE_INTEGRATION_OK selected="+selected+
            " oldSiteJs="+oldCount+" currentSiteJs="+newCount);
    }
}
