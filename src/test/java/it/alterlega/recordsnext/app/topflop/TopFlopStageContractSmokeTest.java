package it.alterlega.recordsnext.app.topflop;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/** Execute without JUnit; full package tests remain in the existing suite. */
public final class TopFlopStageContractSmokeTest {
    private TopFlopStageContractSmokeTest() {}
    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new IllegalArgumentException("Expected <reportsRoot> <testOutputDir>");
        Map<String,Object> row=new LinkedHashMap<>();
        row.put("name","Nome \"accentato\" à \\ \n test");
        row.put("vote",new java.math.BigDecimal("7.50"));
        row.put("nullable",null);
        String js=TopFlopStageJsExporter.Json.write(row);
        if (!js.contains("\\\"accentato\\\"") || !js.contains("7.5") || !js.contains("null"))
            throw new AssertionError("JSON escaping and decimals failed: " + js);
        if (TopFlopStageJsExporter.MAX_BYTES!=1500000L || TopFlopStageJsExporter.TARGET_BYTES>1000000L)
            throw new AssertionError("Incorrect byte budget");
        var links=TopFlopCanonicalScorecards.load(Path.of(args[0]),"2006_2007");
        String url=links.get(new TopFlopCanonicalScorecards.MatchTeam(271L,7));
        if(url==null || !url.endsWith("/lega2006/ris.htm?Gio=4"))
            throw new AssertionError("Missing original 3.2 historical scorecard: " + url);
        Path testDir=Path.of(args[1]);
        java.nio.file.Files.createDirectories(testDir);
        boolean rejected=false;
        try {
            TopFlopStageJsExporter.shard(testDir,"2099_2100","Detail",
                java.util.List.of("{\"huge\":\""+"x".repeat(1_500_200)+"\"}"));
        } catch (java.io.IOException expected) {
            rejected=expected.getMessage().contains("too large");
        }
        boolean wroteFile;
        try (var files=java.nio.file.Files.list(testDir)) { wroteFile=files.findAny().isPresent(); }
        if(!rejected || wroteFile)
            throw new AssertionError("Oversized single-player shard was not rejected before write");
        System.out.println("TOP_FLOP_STAGE_CONTRACT_OK links2006="+links.size()+
                " maxBytes="+TopFlopStageJsExporter.MAX_BYTES+" oversizedPlayerRejected=true");
    }
}
