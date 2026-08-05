package it.alterlega.recordsnext.app;

import it.alterlega.recordsnext.Records2026SitePublisher;
import it.alterlega.recordsnext.RiserveUfficioArchiveBuilder;
import it.alterlega.recordsnext.SeasonRecordsArchiveBuilder;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public final class RecordsNextPipeline {
    public interface Listener {
        void phase(String text, int percent);
        default void timing(String text) { phase("TEMPO " + text, -1); }
    }
    public record Result(int classicEntries, int ruSeasons, int files, int published) {}

    public Result run(PipelineConfig c, ProcessingOptions o, ProcessingMode mode, Listener l) throws Exception {
        long totalStarted = System.nanoTime();
        Path database = c.projectRoot().resolve("data/database/recordsnext.db").normalize();
        RecordsNextPreparationService preparation = new RecordsNextPreparationService(c.projectRoot(), database);

        long preparationStarted = System.nanoTime();
        List<String> changedSeasons = preparation.prepare(mode, c.seasons(), l);
        l.timing("preparazione complessiva: " + elapsed(preparationStarted));

        if (o.classic()) {
            l.phase("Generazione record classici", 55);
            long started = System.nanoTime();
            SeasonRecordsArchiveBuilder.build(c.reports(), c.classicArchive(), changedSeasons);
            l.timing("record classici: " + elapsed(started));
        }
        if (o.ru()) {
            l.phase("Generazione riserve d'ufficio", 68);
            long started = System.nanoTime();
            RiserveUfficioArchiveBuilder.build(c.reports(), c.ruArchive(), changedSeasons);
            l.timing("riserve d'ufficio: " + elapsed(started));
        }

        Result result;
        if (!o.generateJs()) {
            l.phase("Archivi elaborati; generazione JavaScript non richiesta", 96);
            result = new Result(0, 0, 0, 0);
        } else {
            l.phase(o.publish() ? "Generazione e pubblicazione JavaScript" : "Generazione JavaScript", 82);
            long started = System.nanoTime();
            var r = Records2026SitePublisher.run(
                c.classicArchive(), c.ruArchive(), c.staging(), c.siteJs(),
                !o.publish(), o.classic(), o.ru());
            l.timing((o.publish() ? "generazione e pubblicazione JavaScript: " : "generazione JavaScript: ")
                + elapsed(started));
            result = new Result(r.classicEntries(), r.ruSeasons(), r.validatedFiles(), r.publishedFiles());
        }
        preparation.saveConsolidation(c.seasons());
        l.timing("totale elaborazione: " + elapsed(totalStarted));
        l.phase("Elaborazione completata e consolidamento aggiornato", 100);
        return result;
    }

    public boolean hasConsolidation(PipelineConfig c) {
        Path database = c.projectRoot().resolve("data/database/recordsnext.db").normalize();
        return new RecordsNextPreparationService(c.projectRoot(), database).hasConsolidation();
    }

    private static String elapsed(long started) {
        double seconds = (System.nanoTime() - started) / 1_000_000_000.0;
        return String.format(Locale.ROOT, "%.3f s", seconds);
    }
}
