package it.alterlega.recordsnext.app.config;

import it.alterlega.recordsnext.app.PipelineConfig;
import it.alterlega.recordsnext.app.ProcessingMode;
import it.alterlega.recordsnext.app.RecordsNextPipeline;

import java.nio.file.Path;

public final class ConfiguredPipelineRunner {
    private ConfiguredPipelineRunner() {}

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
}
