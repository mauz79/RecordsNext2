package it.alterlega.recordsnext.gui;

import java.nio.file.Path;
import java.sql.*;
import java.time.Instant;
import java.util.*;

final class SeasonConfigurationRepository {
    record SeasonRow(String seasonId, int seasonNumber, boolean anchor,
                     String managementType, String status,
                     String fcmPath, String fcaPath,
                     String localSitePath, String onlineSiteUrl) {}

    private final Path database;

    SeasonConfigurationRepository(Path database) {
        this.database = database.toAbsolutePath().normalize();
    }

    List<SeasonRow> load() throws Exception {
        Class.forName("org.sqlite.JDBC");
        try (Connection c = open()) {
            ensureSchema(c);
            String sql = """
                SELECT s.season_id, COALESCE(s.sort_order,0), s.is_anchor,
                       COALESCE(c.management_type,'GESTITA') management_type,
                       COALESCE(c.configuration_status,'DA_CONFIGURARE') configuration_status,
                       COALESCE(c.configured_fcm_path,
                         MAX(CASE WHEN f.source_type='FCM' THEN f.source_path END),'') fcm_path,
                       COALESCE(c.configured_fca_path,
                         MAX(CASE WHEN f.source_type='FCA' THEN f.source_path END),'') fca_path,
                       COALESCE(c.local_site_path,'') local_site_path,
                       COALESCE(c.online_site_url,'') online_site_url
                FROM rn_season s
                LEFT JOIN rn_season_configuration c ON c.season_id=s.season_id
                LEFT JOIN rn_source_file f ON f.season_id=s.season_id
                GROUP BY s.season_id,s.sort_order,s.is_anchor,c.management_type,
                         c.configuration_status,c.configured_fcm_path,
                         c.configured_fca_path,c.local_site_path,c.online_site_url
                ORDER BY CAST(SUBSTR(s.season_id,1,4) AS INTEGER) DESC, s.season_id DESC
                """;
            List<SeasonRow> out = new ArrayList<>();
            try (Statement st=c.createStatement(); ResultSet r=st.executeQuery(sql)) {
                while (r.next()) out.add(new SeasonRow(
                    r.getString(1), r.getInt(2), r.getInt(3)==1,
                    r.getString(4), r.getString(5), r.getString(6),
                    r.getString(7), r.getString(8), r.getString(9)));
            }
            assignMissingNumbers(out);
            return out;
        }
    }

    int suggestedSeasonNumber(String seasonId, Collection<SeasonRow> current) {
        List<String> ids = new ArrayList<>();
        for (SeasonRow row : current) ids.add(row.seasonId());
        if (!ids.contains(seasonId)) ids.add(seasonId);
        ids.sort(Comparator.comparingInt(SeasonConfigurationRepository::startYear));
        return ids.indexOf(seasonId) + 1;
    }

    void save(List<SeasonRow> rows) throws Exception {
        Class.forName("org.sqlite.JDBC");
        try (Connection c=open()) {
            ensureSchema(c); c.setAutoCommit(false);
            try {
                String anchorSeason = rows.stream()
                    .filter(r -> "GESTITA".equals(r.managementType()))
                    .max(Comparator.comparingInt(r -> startYear(r.seasonId())))
                    .map(SeasonRow::seasonId).orElse(null);
                String now= Instant.now().toString();
                try (Statement st=c.createStatement()) { st.executeUpdate("UPDATE rn_season SET is_anchor=0"); }
                for (SeasonRow row: rows) {
                    boolean anchor = Objects.equals(row.seasonId(), anchorSeason);
                    try (PreparedStatement p=c.prepareStatement("""
                        INSERT INTO rn_season(season_id,display_name,sort_order,is_anchor,created_at,updated_at)
                        VALUES(?,?,?,?,?,?)
                        ON CONFLICT(season_id) DO UPDATE SET display_name=excluded.display_name,
                          sort_order=excluded.sort_order,is_anchor=excluded.is_anchor,
                          updated_at=excluded.updated_at
                        """)) {
                        p.setString(1,row.seasonId()); p.setString(2,row.seasonId());
                        p.setInt(3,row.seasonNumber()); p.setInt(4,anchor?1:0);
                        p.setString(5,now); p.setString(6,now); p.executeUpdate();
                    }
                    try (PreparedStatement p=c.prepareStatement("""
                        INSERT INTO rn_season_configuration(
                          season_id,management_type,local_site_path,online_site_url,dataa_path,
                          configuration_status,created_at,updated_at,configured_fcm_path,configured_fca_path)
                        VALUES(?,?,?,?,NULL,?,?,?,?,?)
                        ON CONFLICT(season_id) DO UPDATE SET management_type=excluded.management_type,
                          local_site_path=excluded.local_site_path,online_site_url=excluded.online_site_url,
                          configuration_status=excluded.configuration_status,updated_at=excluded.updated_at,
                          configured_fcm_path=excluded.configured_fcm_path,
                          configured_fca_path=excluded.configured_fca_path
                        """)) {
                        p.setString(1,row.seasonId()); p.setString(2,row.managementType());
                        nullable(p,3,row.localSitePath()); nullable(p,4,row.onlineSiteUrl());
                        p.setString(5,status(row)); p.setString(6,now); p.setString(7,now);
                        nullable(p,8,row.fcmPath()); nullable(p,9,row.fcaPath()); p.executeUpdate();
                    }
                }
                c.commit();
            } catch(Exception ex) { c.rollback(); throw ex; }
        }
    }

    void removeConfiguration(String seasonId) throws Exception {
        Class.forName("org.sqlite.JDBC");

        try (Connection c = open()) {
            ensureSchema(c);
            c.setAutoCommit(false);

            try {
                deleteSeasonRows(c, seasonId);
                c.commit();
            } catch (Exception ex) {
                c.rollback();
                throw ex;
            }
        }
    }

    private static void deleteSeasonRows(Connection c, String seasonId) throws Exception {
        reanchorOrDeleteTeamIdentities(c, seasonId);
        reanchorOrDeleteCompetitionIdentities(c, seasonId);

        executeIfTableExists(c, """
            DELETE FROM rn_team_mapping
            WHERE team_season_id IN (
                SELECT team_season_id
                FROM rn_team_season
                WHERE season_id = ?
            )
            """, seasonId);

        executeIfTableExists(c, """
            DELETE FROM rn_competition_mapping
            WHERE competition_season_id IN (
                SELECT competition_season_id
                FROM rn_competition_season
                WHERE season_id = ?
            )
            """, seasonId);

        executeIfTableExists(c,
            "DELETE FROM rn_team_season WHERE season_id = ?",
            seasonId);

        executeIfTableExists(c,
            "DELETE FROM rn_competition_season WHERE season_id = ?",
            seasonId);

        executeIfTableExists(c,
            "DELETE FROM rn_matchday_date WHERE season_id = ?",
            seasonId);

        executeIfTableExists(c,
            "DELETE FROM rn_calendar_source WHERE season_id = ?",
            seasonId);

        executeIfTableExists(c,
            "DELETE FROM rn_season_configuration WHERE season_id = ?",
            seasonId);

        executeIfTableExists(c,
            "DELETE FROM rn_source_file WHERE season_id = ?",
            seasonId);

        try (PreparedStatement p = c.prepareStatement(
                "DELETE FROM rn_season WHERE season_id = ?")) {
            p.setString(1, seasonId);

            int deleted = p.executeUpdate();
            if (deleted != 1) {
                throw new IllegalStateException(
                    "Stagione non trovata o non eliminata: " + seasonId
                );
            }
        }

        promoteLatestManagedSeasonAsAnchor(c);
    }

    private static void promoteLatestManagedSeasonAsAnchor(
            Connection c) throws Exception {

        try (Statement s = c.createStatement()) {
            s.executeUpdate("UPDATE rn_season SET is_anchor = 0");
        }

        try (PreparedStatement p = c.prepareStatement("""
            UPDATE rn_season
            SET is_anchor = 1
            WHERE season_id = (
                SELECT s.season_id
                FROM rn_season s
                LEFT JOIN rn_season_configuration cfg
                  ON cfg.season_id = s.season_id
                WHERE COALESCE(cfg.management_type, 'GESTITA') = 'GESTITA'
                ORDER BY CAST(SUBSTR(s.season_id,1,4) AS INTEGER) DESC,
                         s.season_id DESC
                LIMIT 1
            )
            """)) {
            p.executeUpdate();
        }
    }

    private static void reanchorOrDeleteTeamIdentities(
            Connection c,
            String seasonId) throws Exception {

        if (!tableExists(c, "rn_team_identity")
                || !tableExists(c, "rn_team_season")
                || !tableExists(c, "rn_team_mapping")) {
            return;
        }

        List<Long> identities = new ArrayList<>();

        try (PreparedStatement p = c.prepareStatement("""
            SELECT team_identity_id
            FROM rn_team_identity
            WHERE anchor_season_id = ?
            """)) {
            p.setString(1, seasonId);

            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    identities.add(rs.getLong(1));
                }
            }
        }

        for (long identityId : identities) {
            Long newTeamSeasonId = null;
            String newSeasonId = null;

            try (PreparedStatement p = c.prepareStatement("""
                SELECT ts.team_season_id, ts.season_id
                FROM rn_team_mapping tm
                JOIN rn_team_season ts
                  ON ts.team_season_id = tm.team_season_id
                WHERE tm.team_identity_id = ?
                  AND ts.season_id <> ?
                ORDER BY CAST(SUBSTR(ts.season_id,1,4) AS INTEGER) DESC,
                         ts.team_season_id DESC
                LIMIT 1
                """)) {
                p.setLong(1, identityId);
                p.setString(2, seasonId);

                try (ResultSet rs = p.executeQuery()) {
                    if (rs.next()) {
                        newTeamSeasonId = rs.getLong(1);
                        newSeasonId = rs.getString(2);
                    }
                }
            }

            if (newTeamSeasonId != null) {
                try (PreparedStatement p = c.prepareStatement("""
                    UPDATE rn_team_identity
                    SET anchor_season_id = ?,
                        anchor_team_season_id = ?
                    WHERE team_identity_id = ?
                    """)) {
                    p.setString(1, newSeasonId);
                    p.setLong(2, newTeamSeasonId);
                    p.setLong(3, identityId);
                    p.executeUpdate();
                }
            } else {
                try (PreparedStatement p = c.prepareStatement("""
                    DELETE FROM rn_team_mapping
                    WHERE team_identity_id = ?
                    """)) {
                    p.setLong(1, identityId);
                    p.executeUpdate();
                }

                try (PreparedStatement p = c.prepareStatement("""
                    DELETE FROM rn_team_identity
                    WHERE team_identity_id = ?
                    """)) {
                    p.setLong(1, identityId);
                    p.executeUpdate();
                }
            }
        }
    }

    private static void reanchorOrDeleteCompetitionIdentities(
            Connection c,
            String seasonId) throws Exception {

        if (!tableExists(c, "rn_competition_identity")
                || !tableExists(c, "rn_competition_season")
                || !tableExists(c, "rn_competition_mapping")) {
            return;
        }

        List<Long> identities = new ArrayList<>();

        try (PreparedStatement p = c.prepareStatement("""
            SELECT competition_identity_id
            FROM rn_competition_identity
            WHERE anchor_season_id = ?
            """)) {
            p.setString(1, seasonId);

            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    identities.add(rs.getLong(1));
                }
            }
        }

        for (long identityId : identities) {
            Long newCompetitionSeasonId = null;
            String newSeasonId = null;

            try (PreparedStatement p = c.prepareStatement("""
                SELECT cs.competition_season_id, cs.season_id
                FROM rn_competition_mapping cm
                JOIN rn_competition_season cs
                  ON cs.competition_season_id = cm.competition_season_id
                WHERE cm.competition_identity_id = ?
                  AND cs.season_id <> ?
                ORDER BY CAST(SUBSTR(cs.season_id,1,4) AS INTEGER) DESC,
                         cs.competition_season_id DESC
                LIMIT 1
                """)) {
                p.setLong(1, identityId);
                p.setString(2, seasonId);

                try (ResultSet rs = p.executeQuery()) {
                    if (rs.next()) {
                        newCompetitionSeasonId = rs.getLong(1);
                        newSeasonId = rs.getString(2);
                    }
                }
            }

            if (newCompetitionSeasonId != null) {
                try (PreparedStatement p = c.prepareStatement("""
                    UPDATE rn_competition_identity
                    SET anchor_season_id = ?,
                        anchor_competition_season_id = ?
                    WHERE competition_identity_id = ?
                    """)) {
                    p.setString(1, newSeasonId);
                    p.setLong(2, newCompetitionSeasonId);
                    p.setLong(3, identityId);
                    p.executeUpdate();
                }
            } else {
                try (PreparedStatement p = c.prepareStatement("""
                    DELETE FROM rn_competition_mapping
                    WHERE competition_identity_id = ?
                    """)) {
                    p.setLong(1, identityId);
                    p.executeUpdate();
                }

                try (PreparedStatement p = c.prepareStatement("""
                    DELETE FROM rn_competition_identity
                    WHERE competition_identity_id = ?
                    """)) {
                    p.setLong(1, identityId);
                    p.executeUpdate();
                }
            }
        }
    }

    private static boolean tableExists(
            Connection c,
            String table) throws Exception {

        try (PreparedStatement p = c.prepareStatement("""
            SELECT COUNT(*)
            FROM sqlite_master
            WHERE type = 'table' AND name = ?
            """)) {
            p.setString(1, table);

            try (ResultSet rs = p.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    private static void executeIfTableExists(
            Connection c,
            String sql,
            String seasonId) throws Exception {

        String table = tableNameFromSql(sql);

        try (PreparedStatement check = c.prepareStatement("""
            SELECT COUNT(*)
            FROM sqlite_master
            WHERE type = 'table' AND name = ?
            """)) {
            check.setString(1, table);

            try (ResultSet rs = check.executeQuery()) {
                rs.next();
                if (rs.getInt(1) == 0) {
                    return;
                }
            }
        }

        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, seasonId);
            p.executeUpdate();
        }
    }

    private static String tableNameFromSql(String sql) {
        String normalized = sql
            .replaceAll("\\s+", " ")
            .trim()
            .toLowerCase(Locale.ROOT);

        if (normalized.startsWith("delete from ")) {
            return normalized.substring("delete from ".length())
                .split(" ")[0];
        }

        if (normalized.startsWith("update ")) {
            return normalized.substring("update ".length())
                .split(" ")[0];
        }

        throw new IllegalArgumentException(
            "SQL non supportato per deleteSeasonRows: " + sql
        );
    }

    private Connection open() throws Exception {
        Connection c=DriverManager.getConnection("jdbc:sqlite:"+database);
        try(Statement s=c.createStatement()) { s.execute("PRAGMA foreign_keys=ON"); s.execute("PRAGMA busy_timeout=10000"); }
        return c;
    }

    private static void ensureSchema(Connection c) throws Exception {
        try(Statement s=c.createStatement()) {
            s.execute("""
                CREATE TABLE IF NOT EXISTS rn_season_configuration(
                  season_id TEXT PRIMARY KEY, management_type TEXT NOT NULL,
                  local_site_path TEXT, online_site_url TEXT, dataa_path TEXT,
                  configuration_status TEXT NOT NULL DEFAULT 'DA_CONFIGURARE',
                  created_at TEXT NOT NULL, updated_at TEXT NOT NULL,
                  configured_fcm_path TEXT, configured_fca_path TEXT,
                  FOREIGN KEY(season_id) REFERENCES rn_season(season_id))
                """);
        }
        addColumnIfMissing(c,"configured_fcm_path","TEXT");
        addColumnIfMissing(c,"configured_fca_path","TEXT");
    }

    private static void addColumnIfMissing(Connection c,String name,String type) throws Exception {
        boolean found=false;
        try(Statement s=c.createStatement(); ResultSet r=s.executeQuery("PRAGMA table_info(rn_season_configuration)")) {
            while(r.next()) if(name.equalsIgnoreCase(r.getString("name"))) found=true;
        }
        if(!found) try(Statement s=c.createStatement()) { s.execute("ALTER TABLE rn_season_configuration ADD COLUMN "+name+" "+type); }
    }

    private static String status(SeasonRow r) {
        if ("MANUALE".equals(r.managementType())) {
            return "COMPLETA";
        }
        return !r.fcmPath().isBlank() && !r.fcaPath().isBlank() && !r.localSitePath().isBlank()
                ? "COMPLETA" : "DA_CONFIGURARE";
    }

    private static void nullable(PreparedStatement p,int i,String value) throws Exception {
        String v=value==null?"":value.trim(); if(v.isEmpty()) p.setNull(i,Types.VARCHAR); else p.setString(i,v);
    }

    private static int startYear(String seasonId) {
        try { return Integer.parseInt(seasonId.substring(0,4)); }
        catch (Exception ex) { return Integer.MIN_VALUE; }
    }

    private static void assignMissingNumbers(List<SeasonRow> rows) {
        List<SeasonRow> chronological = new ArrayList<>(rows);
        chronological.sort(Comparator.comparingInt(r -> startYear(r.seasonId())));
        Map<String,Integer> numbers = new HashMap<>();
        int next=1;
        for (SeasonRow row : chronological) {
            int n=row.seasonNumber()>0?row.seasonNumber():next;
            numbers.put(row.seasonId(),n); next=Math.max(next,n+1);
        }
        for (int i=0;i<rows.size();i++) {
            SeasonRow r=rows.get(i);
            if (r.seasonNumber()<=0) rows.set(i,new SeasonRow(r.seasonId(),numbers.get(r.seasonId()),r.anchor(),r.managementType(),r.status(),r.fcmPath(),r.fcaPath(),r.localSitePath(),r.onlineSiteUrl()));
        }
    }
}
