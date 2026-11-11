package it.alterlega.recordsnext.app;

import it.alterlega.recordsnext.Records2026SitePublisher;
import it.alterlega.recordsnext.app.manifest.ManifestMetadata;
import it.alterlega.recordsnext.app.manifest.ManifestPublishingSupport;
import it.alterlega.recordsnext.app.config.ProcessingConfigLoader;
import it.alterlega.recordsnext.app.config.ProcessingConfigWriter;
import it.alterlega.recordsnext.app.output.SeasonFamilyShardPublisher;
import it.alterlega.recordsnext.app.topflop.TopFlopViewsPublication;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** End-to-end smoke of 3.2 publisher/manifest/rollback staging with TF, never real sites. */
public final class TopFlopNativePublisherSmokeTest {
    private TopFlopNativePublisherSmokeTest() {}
    private static void ok(boolean pass, String what){if(!pass)throw new AssertionError(what);}
    public static void main(String[] args)throws Exception {
        if(args.length!=3)throw new IllegalArgumentException("<db> <reports> <scratch>");
        Path root=Path.of(args[2]);Files.createDirectories(root);
        Path cfg=root.resolve("processing.json");
        Map<String,Boolean> children=new HashMap<>();
        for(String id:ProcessingConfigWriter.TOP_FLOP)children.put(id,false);
        children.put("topFlop.tf03",true);children.put("topFlop.tf20",true);
        ProcessingConfigWriter.save(cfg,new ProcessingConfigWriter.State(false,false,false,false,false,
            true,false,true,children,ProcessingConfigWriter.defaultModifierNames()));
        var options=ProcessingConfigLoader.load(cfg);
        var preflight=PipelinePreflight.evaluate(options);
        var meta=new ManifestMetadata("RecordsNext by mauz79","4.0-test","2.0",
            OffsetDateTime.now(),"test","2006_2007",List.of("2006_2007"),List.of());
        var prepared=TopFlopViewsPublication.prepare(Path.of(args[0]),Path.of(args[1]),
            List.of("2006_2007","2026_2027"),TopFlopViewsPublication.selectedIds(options.selection()),
            root.resolve("prepared"));
        var publisher=Records2026SitePublisher.run(root,root,root.resolve("site-staging"),
            root.resolve("site-js"),true,false,false,options,preflight,meta,null,null,null);
        Path stage=publisher.stagingDirectory().resolve("js");
        int copied=TopFlopViewsPublication.attachScoped(prepared,List.of("2006_2007"),stage);
        SeasonFamilyShardPublisher.prepareFlat(stage);
        ManifestPublishingSupport.write(stage,options,preflight,meta);
        int validated=TopFlopViewsPublication.validateStaging(stage);
        ok(validated==copied+1,"new TF JS + 3.2 manifest");
        Path manifest=stage.resolve("fcmRecordsNext_Manifest.js");
        ok(Files.isRegularFile(manifest),"manifest available");
        String manifestText=Files.readString(manifest);
        ok(manifestText.contains("fcmRecordsNext_TopFlopViews.js"),"manifest includes TF facade");
        ok(manifestText.contains(".2006_2007."),"manifest includes TF season shard");
        ok(!manifestText.contains("2026_2027"),"manifest excludes future year");
        Path site=root.resolve("published-site-js");
        int published=RecordsNextPipeline.publishGeneratedDirectory(stage,site);
        ok(published==validated,"3.2 publisher count");
        ok(Files.isRegularFile(site.resolve("fcmRecordsNext_TopFlopViews.js")),"TF published in temp");
        Path second=root.resolve("second-generated");Files.createDirectories(second);
        Files.writeString(second.resolve("dummy.js"),"window.keep = true;\n");
        RecordsNextPipeline.publishGeneratedDirectory(second,site);
        ok(!Files.exists(site.resolve("fcmRecordsNext_TopFlopViews.js")),"disabled TF removes stale facade");
        try(var files=Files.list(site)) {
            ok(files.noneMatch(x->x.getFileName().toString().startsWith("fcmRecordsNext_TopFlopViews.")),
                "disabled TF removes stale season shards");
        }
        ok(Files.isRegularFile(site.resolve("dummy.js")),"other site JS preserved");
        System.out.println("TOP_FLOP_NATIVE_PUBLISHER_OK copied="+copied+
            " validated="+validated+" published="+published);
    }
}
