package it.alterlega.recordsnext.app.output;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Risolve le destinazioni sito configurate per le stagioni RecordsNext.
 * Una stagione puo' partecipare allo storico anche senza un sito locale.
 */
public final class SeasonPublicationTargetRepository {
    private final Path database;

    public SeasonPublicationTargetRepository(Path database) {
        this.database = database.toAbsolutePath().normalize();
    }

    public List<Target> load(List<String> selectedSeasons) throws Exception {
        if (!Files.isRegularFile(database)) {
            return List.of();
        }

        Set<String> selected = selectedSeasons == null
                ? Set.of()
                : new HashSet<>(selectedSeasons);

        String sql = """
            SELECT s.season_id, COALESCE(s.sort_order, 0), c.local_site_path
            FROM rn_season s
            JOIN rn_season_configuration c ON c.season_id = s.season_id
            WHERE COALESCE(c.management_type, 'GESTITA') = 'GESTITA'
              AND c.local_site_path IS NOT NULL
              AND TRIM(c.local_site_path) <> ''
            ORDER BY COALESCE(s.sort_order, 0), s.season_id
            """;

        Class.forName("org.sqlite.JDBC");
        List<Target> result = new ArrayList<>();

        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + database);
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet rs = p.executeQuery()) {

            while (rs.next()) {
                String seasonId = rs.getString(1);
                if (!selected.isEmpty() && !selected.contains(seasonId)) {
                    continue;
                }

                Path siteRoot = Path.of(rs.getString(3)).toAbsolutePath().normalize();
                result.add(new Target(
                        seasonId,
                        rs.getInt(2),
                        siteRoot,
                        siteRoot.resolve("js").normalize(),
                        Files.isDirectory(siteRoot)
                ));
            }
        }

        return List.copyOf(result);
    }


    /**
     * Restituisce le stagioni selezionate che appartengono allo storico
     * disponibile fino al target incluso. Il cutoff usa sort_order del DB,
     * non il confronto testuale del season_id.
     */
    public List<String> scope(
            List<String> selectedSeasons,
            Target target) throws Exception {

        if (target == null) {
            return List.of();
        }
        if (!Files.isRegularFile(database)) {
            return List.of();
        }

        Set<String> selected = selectedSeasons == null
                ? Set.of()
                : new HashSet<>(selectedSeasons);

        String sql = """
            SELECT season_id
            FROM rn_season
            WHERE COALESCE(sort_order, 0) <= ?
            ORDER BY COALESCE(sort_order, 0) DESC, season_id DESC
            """;

        Class.forName("org.sqlite.JDBC");
        List<String> result = new ArrayList<>();

        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + database);
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setInt(1, target.sortOrder());

            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    String seasonId = rs.getString(1);
                    if (!selected.isEmpty() && !selected.contains(seasonId)) {
                        continue;
                    }
                    result.add(seasonId);
                }
            }
        }

        return List.copyOf(result);
    }

    public record Target(
            String seasonId,
            int sortOrder,
            Path siteRoot,
            Path siteJs,
            boolean available) {
    }
}
