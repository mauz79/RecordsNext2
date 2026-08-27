package it.alterlega.recordsnext.app.config;

import it.alterlega.recordsnext.app.PipelineConfig;
import it.alterlega.recordsnext.app.ProcessingMode;
import it.alterlega.recordsnext.app.RecordsNextPipeline;

import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfiguredPipelineRunner {
    private ConfiguredPipelineRunner() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1 || args.length > 3) {
            System.err.println(
                "Uso: ConfiguredPipelineRunner "
                    + "<processing.json> [FULL|CONSOLIDATED] [projectRoot]"
            );
            System.exit(2);
        }

        Path processingConfig = Path.of(args[0]).toAbsolutePath().normalize();

        if (!Files.isRegularFile(processingConfig)) {
            throw new IllegalArgumentException(
                "File di configurazione elaborazione non trovato: " + processingConfig
            );
        }

        Path inferredRoot = processingConfig.getParent() == null
            ? null
            : processingConfig.getParent().getParent();

        Path projectRoot = args.length == 3
            ? Path.of(args[2]).toAbsolutePath().normalize()
            : inferredRoot;

        if (projectRoot == null) {
            throw new IllegalArgumentException(
                "Impossibile ricavare la root progetto da: " + processingConfig
            );
        }

        Path propertiesFile = projectRoot
            .resolve("config")
            .resolve("recordsnext-gui.properties");

        PipelineConfig pipelineConfig = Files.isRegularFile(propertiesFile)
            ? PipelineConfig.load(projectRoot, propertiesFile)
            : PipelineConfig.defaults(projectRoot);

        RecordsNextPipeline pipeline = new RecordsNextPipeline();

        ProcessingMode mode;
        if (args.length >= 2 && !args[1].isBlank()) {
            mode = ProcessingMode.valueOf(args[1].trim().toUpperCase());
        } else {
            mode = pipeline.hasConsolidation(pipelineConfig)
                ? ProcessingMode.CONSOLIDATED
                : ProcessingMode.FULL;
        }

        System.out.println("RecordsNext 3.0 - esecuzione configurata");
        System.out.println("Project root : " + projectRoot);
        System.out.println("Processing   : " + processingConfig);
        System.out.println(
            "Pipeline cfg : "
                + (Files.isRegularFile(propertiesFile)
                    ? propertiesFile
                    : "[default PipelineConfig]")
        );
        System.out.println("Mode         : " + mode);
        System.out.println();

        RecordsNextPipeline.Result result = run(
            pipelineConfig,
            processingConfig,
            mode,
            new ConsoleListener()
        );

        System.out.println();
        System.out.println("Elaborazione completata.");
        System.out.println("Classic entries : " + result.classicEntries());
        System.out.println("RU seasons      : " + result.ruSeasons());
        System.out.println("File validi     : " + result.files());
        System.out.println("File pubblicati : " + result.published());
    }

    public static RecordsNextPipeline.Result run(
        PipelineConfig pipelineConfig,
        Path processingConfig,
        ProcessingMode mode,
        RecordsNextPipeline.Listener listener
    ) throws Exception {
        return new RecordsNextPipeline().run(
            pipelineConfig,
            ProcessingConfigLoader.load(processingConfig),
            mode,
            listener
        );
    }

    private static final class ConsoleListener
        implements RecordsNextPipeline.Listener {

        @Override
        public void phase(String text, int percent) {
            System.out.println("[" + percent + "%] " + text);
        }

        @Override
        public void timing(String text) {
            System.out.println("TEMPO " + text);
        }
    }
}
