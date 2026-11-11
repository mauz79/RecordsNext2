package it.alterlega.recordsnext.app.topflop;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Staging-only no-database regression: shard size, link store, no silent truncation. */
public final class TopFlopCompactStageSmokeTest {
    private TopFlopCompactStageSmokeTest() {}
    public static void main(String[] args) throws Exception {
        Path root=Files.createTempDirectory("topflop-compact-smoke-");
        try {
            var summary=TopFlopCompactStageJsExporter.shard(root,"2026_2027","Summary",
                List.of("[1,100843,\"Lautaro\",0]","[2,123,\"Altro\",0]"));
            if(summary.size()!=1 || summary.getFirst().rows()!=2)throw new AssertionError("summary rows lost");
            if(Files.size(root.resolve(summary.getFirst().name()))!=summary.getFirst().bytes())
                throw new AssertionError("invalid real byte count");
            var links=TopFlopCompactStageJsExporter.shard(root,"2026_2027","Links",
                List.of("[100,12,\"https://example.invalid/ris.php?Gio=4\"]"));
            if(links.size()!=1 || !Files.readString(root.resolve(links.getFirst().name()),
                StandardCharsets.UTF_8).contains("d.links"))throw new AssertionError("link dictionary missing");
            boolean refused=false;
            try {
                TopFlopCompactStageJsExporter.shard(root,"2025_2026","Detail",
                    List.of("\""+"x".repeat(1_500_001)+"\""));
            }catch(IOException expected){refused=true;}
            if(!refused)throw new AssertionError("oversized individual row was not blocked");
            System.out.println("TOP_FLOP_COMPACT_SMOKE_OK checks=4");
        }finally{
            try(var paths=Files.walk(root)) {
                paths.sorted(java.util.Comparator.reverseOrder()).forEach(p->{try{Files.deleteIfExists(p);}catch(IOException ignored){}});
            }
        }
    }
}
