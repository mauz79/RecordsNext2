package it.alterlega.recordsnext.app.output;

import java.nio.file.Path;
import java.util.Locale;

/** Utility minima per la migrazione progressiva degli shard storici. */
public final class SeasonShardAvailabilityCli {
    private SeasonShardAvailabilityCli() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            System.err.println("Uso: SeasonShardAvailabilityCli <projectRoot> <AAAA_AAAA> <local|online|both> <true|false>");
            System.exit(2);
        }
        Path root = Path.of(args[0]).toAbsolutePath().normalize();
        String season = args[1].trim();
        if (!season.matches("\\d{4}_\\d{4}")) {
            throw new IllegalArgumentException("Stagione non valida: " + season);
        }
        String scope = args[2].trim().toLowerCase(Locale.ROOT);
        boolean value = Boolean.parseBoolean(args[3]);
        Path state = root.resolve("data/consolidation").resolve(SeasonFamilyShardPublisher.STATE_FILE_NAME);
        switch (scope) {
            case "local" -> SeasonFamilyShardPublisher.setLocalAvailability(state, season, value);
            case "online" -> SeasonFamilyShardPublisher.setOnlineAvailability(state, season, value);
            case "both" -> {
                SeasonFamilyShardPublisher.setLocalAvailability(state, season, value);
                SeasonFamilyShardPublisher.setOnlineAvailability(state, season, value);
            }
            default -> throw new IllegalArgumentException("Ambito non valido: " + scope);
        }
        System.out.println("Shard " + season + " " + scope + "=" + value);
        System.out.println("Stato: " + state);
    }
}
