package it.alterlega.recordsnext.app.output;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SeasonPublicationScopeRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void scopeUsesSortOrderInsteadOfSeasonIdTextOrder() throws Exception {
        Path db = tempDir.resolve("recordsnext.db");

        Class.forName("org.sqlite.JDBC");

        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + db);
             Statement s = c.createStatement()) {

            s.execute("""
                CREATE TABLE rn_season (
                    season_id TEXT PRIMARY KEY,
                    sort_order INTEGER
                )
                """);

            s.execute("""
                CREATE TABLE rn_season_configuration (
                    season_id TEXT PRIMARY KEY,
                    management_type TEXT,
                    local_site_path TEXT
                )
                """);

            s.execute("INSERT INTO rn_season VALUES ('FUTURE_TEXT_SMALL', 30)");
            s.execute("INSERT INTO rn_season VALUES ('TARGET_ZZZ', 20)");
            s.execute("INSERT INTO rn_season VALUES ('OLD_TEXT_LARGE', 10)");

            s.execute("""
                INSERT INTO rn_season_configuration
                VALUES ('TARGET_ZZZ','GESTITA','C:/site-target')
                """);
        }

        SeasonPublicationTargetRepository repository =
                new SeasonPublicationTargetRepository(db);

        SeasonPublicationTargetRepository.Target target =
                repository.load(List.of(
                        "FUTURE_TEXT_SMALL",
                        "TARGET_ZZZ",
                        "OLD_TEXT_LARGE"
                )).get(0);

        List<String> scoped = repository.scope(
                List.of(
                        "FUTURE_TEXT_SMALL",
                        "TARGET_ZZZ",
                        "OLD_TEXT_LARGE"
                ),
                target
        );

        assertEquals(
                List.of("TARGET_ZZZ", "OLD_TEXT_LARGE"),
                scoped
        );
    }
}
