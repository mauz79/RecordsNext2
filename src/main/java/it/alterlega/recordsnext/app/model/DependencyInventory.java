package it.alterlega.recordsnext.app.model;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Inventario iniziale delle dipendenze disponibili nel flusso RecordsNext 1.0.2.
 * Non esegue controlli sul database: rappresenta solo capacita dichiarate.
 */
public final class DependencyInventory {
    private DependencyInventory() {
    }

    public static Set<String> legacyCapabilities(
            boolean captainEnabled,
            boolean homeFieldEnabled,
            boolean ruSimulationEnabled,
            boolean culometroConfigured
    ) {
        Set<String> dependencies = new LinkedHashSet<>();
        dependencies.add("data.matches");
        dependencies.add("data.scores");
        dependencies.add("data.ordered-matches");
        dependencies.add("configuration.goal-bands");
        dependencies.add("ru.events");

        if (captainEnabled) {
            dependencies.add("modifier.captain");
        }
        if (homeFieldEnabled) {
            dependencies.add("modifier.home-field");
        }
        if (ruSimulationEnabled) {
            dependencies.add("simulation.without-ru");
        }
        if (culometroConfigured) {
            dependencies.add("configuration.culometro");
        }

        return Set.copyOf(dependencies);
    }
}
