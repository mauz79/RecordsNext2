package it.alterlega.recordsnext.app;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class RecordsNextPipelineV83PublishTest {
    @TempDir Path root;
    @Test void publishPagesAndJsTogether() throws Exception {
        Path stage=Files.createDirectories(root.resolve("stage"));
        Path site=Files.createDirectories(root.resolve("site/js"));
        Path pages=Files.createDirectories(root.resolve("site/RecordsNext"));
        Files.writeString(site.resolve("data.js"),"OLD");
        Files.writeString(pages.resolve("top_flop.html"),"OLD PAGE");
        Path page=Files.writeString(root.resolve("top_flop.html"),"NEW PAGE");
        Files.writeString(stage.resolve("data.js"),"NEW");
        assertEquals(2,RecordsNextPipeline.publishGeneratedDirectory(stage,site,List.of(page)));
        assertEquals("NEW",Files.readString(site.resolve("data.js")));
        assertEquals("NEW PAGE",Files.readString(pages.resolve("top_flop.html")));
    }
    @Test void invalidPageRollsBackJsAndExistingPages() throws Exception {
        Path stage=Files.createDirectories(root.resolve("stage"));
        Path site=Files.createDirectories(root.resolve("site/js"));
        Path pages=Files.createDirectories(root.resolve("site/RecordsNext"));
        Files.writeString(site.resolve("data.js"),"BEFORE");
        Files.writeString(pages.resolve("top_flop.html"),"BEFORE PAGE");
        Files.writeString(stage.resolve("data.js"),"AFTER");
        Path valid=Files.writeString(root.resolve("top_flop.html"),"AFTER PAGE");
        Path invalid=Files.writeString(root.resolve("unexpected-file.txt"),"x");
        assertThrows(Exception.class,()->RecordsNextPipeline.publishGeneratedDirectory(stage,site,List.of(valid,invalid)));
        assertEquals("BEFORE",Files.readString(site.resolve("data.js")));
        assertEquals("BEFORE PAGE",Files.readString(pages.resolve("top_flop.html")));
    }
}
