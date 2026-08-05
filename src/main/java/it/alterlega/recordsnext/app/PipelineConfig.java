package it.alterlega.recordsnext.app;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public record PipelineConfig(Path projectRoot, Path reports, Path classicArchive, Path ruArchive,
                             Path staging, Path siteJs, List<String> seasons) {
    public static PipelineConfig load(Path projectRoot, Path file) throws IOException {
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(file)) {
            p.load(in);
        }
        List<String> seasons = Arrays.stream(p.getProperty("seasons", "").split("\\s*,\\s*"))
            .filter(s -> !s.isBlank()).toList();
        Path normalizedRoot = projectRoot.toAbsolutePath().normalize();
        return new PipelineConfig(normalizedRoot,
            resolve(normalizedRoot, p.getProperty("reports", "data/reports")),
            resolve(normalizedRoot, p.getProperty("classicArchive", "data/records-archive/stagioni")),
            resolve(normalizedRoot, p.getProperty("ruArchive", "data/records-archive/riserveufficio")),
            resolve(normalizedRoot, p.getProperty("staging", "data/site-export-staging")),
            resolvePublishDirectory(normalizedRoot, p), seasons);
    }

    public static Path resolvePublishDirectory(Path projectRoot, Properties properties) {
        String mode = properties.getProperty("publish.destinationMode", "currentSeason").trim();
        if ("custom".equalsIgnoreCase(mode)) {
            String custom = properties.getProperty("publish.customDirectory", "").trim();
            if (!custom.isEmpty()) {
                return resolve(projectRoot, custom);
            }
        }

        Path database = resolve(projectRoot,
            properties.getProperty("database", "data/database/recordsnext.db"));
        if (Files.isRegularFile(database)) {
            String sql = """
                SELECT c.local_site_path
                FROM rn_season s
                JOIN rn_season_configuration c ON c.season_id=s.season_id
                WHERE s.is_anchor=1
                  AND c.local_site_path IS NOT NULL
                  AND TRIM(c.local_site_path)<>''
                ORDER BY s.sort_order DESC
                LIMIT 1
                """;
            try {
                Class.forName("org.sqlite.JDBC");
                try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + database);
                     Statement statement = connection.createStatement();
                     ResultSet result = statement.executeQuery(sql)) {
                    if (result.next()) {
                        return Path.of(result.getString(1)).resolve("js").toAbsolutePath().normalize();
                    }
                }
            } catch (Exception ignored) {
                // Fallback to the legacy property below.
            }
        }
        return resolve(projectRoot,
            properties.getProperty("siteJs", "E:/fantacalcio/Lega2025/js"));
    }

    private static Path resolve(Path root, String value) {
        Path path = Path.of(value);
        return (path.isAbsolute() ? path : root.resolve(path)).normalize();
    }
}
