package it.alterlega.recordsnext.app.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LeagueMetadataLoader {
    private LeagueMetadataLoader() {
    }

    public static LeagueMetadata load(Path configFile) throws IOException {
        Path file = configFile.toAbsolutePath().normalize();
        if (!Files.isRegularFile(file)) {
            throw new IOException("Configurazione lega non trovata: " + file);
        }
        String json = Files.readString(file, StandardCharsets.UTF_8);
        return new LeagueMetadata(
                readString(json, "leagueId"),
                readString(json, "leagueName"),
                readString(json, "currentSeasonId")
        );
    }

    private static String readString(String json, String key) throws IOException {
        Pattern pattern = Pattern.compile(
                "\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\""
        );
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            throw new IOException("Campo obbligatorio mancante in league.json: " + key);
        }
        return matcher.group(1).trim();
    }
}
