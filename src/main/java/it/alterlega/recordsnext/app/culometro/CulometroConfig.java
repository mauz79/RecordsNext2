package it.alterlega.recordsnext.app.culometro;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record CulometroConfig(
        boolean enabled,
        int minimumMatches,
        BigDecimal kScale,
        BigDecimal secondaryWeight,
        BigDecimal maximumRarityMultiplier,
        int minimumHistoricalOccurrences,
        Map<String, Component> components,
        LabelConfiguration labelConfiguration
) {
    public record Component(boolean enabled, BigDecimal weight, BigDecimal min, BigDecimal max) {}
    public record LabelBand(BigDecimal min, String label) {}
    public record LabelConfiguration(
            String preset,
            boolean customized,
            String resetSource,
            List<LabelBand> bands,
            Map<String, List<LabelBand>> presetDefaults
    ) {
        public List<LabelBand> resetBands() {
            List<LabelBand> reset = presetDefaults.get(resetSource);
            if (reset == null) {
                throw new IllegalStateException("Preset di reset non disponibile: " + resetSource);
            }
            return reset;
        }
    }

    public List<LabelBand> labels() {
        return labelConfiguration.bands();
    }
}
