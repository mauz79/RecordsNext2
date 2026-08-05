package it.alterlega.recordsnext.gui;

import java.nio.file.Path;
import java.sql.*;
import java.time.Instant;
import java.util.*;

final class HistoricalMappingRepository {
    enum Kind { COMPETITION, TEAM }

    record Identity(long id, String name) {
        @Override public String toString() { return name; }
    }

    record MappingRow(List<Long> seasonEntityIds, String sourceName, String normalizedName,
                      String status, Long identityId, Long inheritedIdentityId,
                      List<Identity> candidates) {}

    record Decision(List<Long> seasonEntityIds, String sourceName, Long identityId,
                    boolean createNew, boolean excluded) {}

    private final Path database;

    HistoricalMappingRepository(Path database) {
        this.database = database.toAbsolutePath().normalize();
    }

    /**
     * Prepara il database per la configurazione globale. Gli import FCM/FCA possono
     * produrre due righe tecniche della stessa entita nella stagione ancora; tali
     * righe devono condividere una sola identita canonica.
     */
    void prepare() throws Exception {
        try (Connection c = open()) {
            c.setAutoCommit(false);
            try {
                consolidateDuplicateIdentities(c, Kind.COMPETITION);
                consolidateDuplicateIdentities(c, Kind.TEAM);
                synchronizeGroupedMappings(c, Kind.COMPETITION);
                synchronizeGroupedMappings(c, Kind.TEAM);
                compactObsoleteSources(c);
                c.commit();
            } catch (Exception ex) {
                c.rollback();
                throw ex;
            }
        }
    }

    /** Tutte le stagioni gestite, inclusa l'attuale, dalla piu recente alla piu vecchia. */
    List<String> seasonsNewestFirst() throws Exception {
        String sql = "SELECT c.season_id " +
            "FROM rn_season_configuration c " +
            "JOIN rn_season s ON s.season_id=c.season_id " +
            "WHERE c.management_type='GESTITA' " +
            "ORDER BY COALESCE(s.sort_order,0) DESC, c.season_id DESC";
        try (Connection c = open(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            List<String> result = new ArrayList<>();
            while (rs.next()) result.add(rs.getString(1));
            return result;
        }
    }

    boolean isAnchor(String seasonId) throws Exception {
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(
                "SELECT is_anchor FROM rn_season WHERE season_id=?")) {
            ps.setString(1, seasonId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) == 1;
            }
        }
    }

    List<MappingRow> load(String seasonId, Kind kind) throws Exception {
        try (Connection c = open()) {
            String entityTable = kind == Kind.COMPETITION ? "rn_competition_season" : "rn_team_season";
            String entityId = kind == Kind.COMPETITION ? "competition_season_id" : "team_season_id";
            String mappingTable = kind == Kind.COMPETITION ? "rn_competition_mapping" : "rn_team_mapping";
            String identityTable = kind == Kind.COMPETITION ? "rn_competition_identity" : "rn_team_identity";
            String identityId = kind == Kind.COMPETITION ? "competition_identity_id" : "team_identity_id";

            // Sono disponibili solo le identita effettivamente mantenute in elaborazione.
            List<Identity> identities = new ArrayList<>();
            String identitySql = "SELECT i." + identityId + ",i.canonical_name " +
                "FROM " + identityTable + " i " +
                "WHERE EXISTS (SELECT 1 FROM " + mappingTable + " m " +
                "WHERE m." + identityId + "=i." + identityId + " AND m.mapping_status='ASSOCIATA') " +
                "ORDER BY i.canonical_name COLLATE NOCASE,i." + identityId;
            try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(identitySql)) {
                while (rs.next()) identities.add(new Identity(rs.getLong(1), rs.getString(2)));
            }

            String sql = "SELECT e." + entityId + ",e.source_name,e.normalized_name," +
                "COALESCE(m.mapping_status,'DA_CONFIGURARE'),m." + identityId + " " +
                "FROM " + entityTable + " e LEFT JOIN " + mappingTable + " m ON m." + entityId + "=e." + entityId + " " +
                "JOIN rn_source_file sf ON sf.source_file_id=e.source_file_id " +
                "WHERE e.season_id=? AND sf.import_id=(" +
                "SELECT MAX(sf2.import_id) FROM rn_source_file sf2 " +
                "WHERE sf2.season_id=e.season_id AND sf2.source_type='FCM') " +
                "ORDER BY e.source_name COLLATE NOCASE,e." + entityId;

            LinkedHashMap<String, Group> groups = new LinkedHashMap<>();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, seasonId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        long id = rs.getLong(1);
                        String source = rs.getString(2);
                        String normalized = rs.getString(3);
                        String key = normalize(normalized == null || normalized.isBlank() ? source : normalized);
                        Group group = groups.computeIfAbsent(key, k -> new Group(source, normalized));
                        group.ids.add(id);
                        String status = rs.getString(4);
                        Long mapped = rs.getObject(5) == null ? null : rs.getLong(5);
                        group.accept(status, mapped);
                    }
                }
            }

            List<MappingRow> rows = new ArrayList<>();
            for (Group group : groups.values()) {
                List<Identity> ordered = new ArrayList<>(identities);
                ordered.sort(Comparator
                    .comparingInt((Identity i) -> similarityRank(group.normalizedName, i.name()))
                    .thenComparing(Identity::name, String.CASE_INSENSITIVE_ORDER));
                Long inheritedIdentityId = group.identityId == null && "DA_CONFIGURARE".equals(group.status)
                    ? findInheritedIdentity(c, seasonId, kind, group.normalizedName, group.sourceName)
                    : null;
                rows.add(new MappingRow(List.copyOf(group.ids), group.sourceName, group.normalizedName,
                    group.status, group.identityId, inheritedIdentityId, ordered));
            }
            return rows;
        }
    }


    private static Long findInheritedIdentity(
        Connection c,
        String seasonId,
        Kind kind,
        String normalizedName,
        String sourceName
    ) throws SQLException {
        String entityTable = kind == Kind.COMPETITION ? "rn_competition_season" : "rn_team_season";
        String entityId = kind == Kind.COMPETITION ? "competition_season_id" : "team_season_id";
        String mappingTable = kind == Kind.COMPETITION ? "rn_competition_mapping" : "rn_team_mapping";
        String identityId = kind == Kind.COMPETITION ? "competition_identity_id" : "team_identity_id";
        String lookupName = (normalizedName == null || normalizedName.isBlank() ? sourceName : normalizedName)
            .trim().toLowerCase(Locale.ROOT);
        String sql = "SELECT m." + identityId + " " +
            "FROM " + entityTable + " e " +
            "JOIN " + mappingTable + " m ON m." + entityId + "=e." + entityId + " " +
            "JOIN rn_season newer ON newer.season_id=e.season_id " +
            "JOIN rn_season current ON current.season_id=? " +
            "WHERE LOWER(TRIM(e.normalized_name))=? " +
            "AND newer.sort_order>current.sort_order " +
            "AND m.mapping_status='ASSOCIATA' AND m." + identityId + " IS NOT NULL " +
            "ORDER BY newer.sort_order ASC LIMIT 1";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, seasonId);
            ps.setString(2, lookupName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : null;
            }
        }
    }

    void save(String seasonId, Kind kind, List<Decision> decisions) throws Exception {
        try (Connection c = open()) {
            c.setAutoCommit(false);
            try {
                Set<Long> used = new HashSet<>();
                for (Decision d : decisions) {
                    Long identityId = d.identityId();
                    String status;
                    String method;
                    if (d.createNew()) {
                        identityId = createIdentity(c, kind, seasonId, d.seasonEntityIds().get(0), d.sourceName());
                        status = "ASSOCIATA";
                        method = "NEW_HISTORICAL_IDENTITY";
                    } else if (identityId != null) {
                        if (!used.add(identityId)) {
                            throw new IllegalStateException("La stessa identita e stata scelta due volte nella stagione: " + d.sourceName());
                        }
                        status = "ASSOCIATA";
                        method = isAnchor(c, seasonId) ? "ANCHOR_GUI" : "GUI_MANUAL";
                    } else if (d.excluded()) {
                        status = "ESCLUSA";
                        method = "GUI_EXCLUDED";
                    } else {
                        throw new IllegalStateException("Decisione mancante per: " + d.sourceName());
                    }
                    for (long entityId : d.seasonEntityIds()) {
                        updateMapping(c, kind, entityId, identityId, status, method);
                    }
                }
                c.commit();
            } catch (Exception ex) {
                c.rollback();
                throw ex;
            }
        }
    }

    int pending(String seasonId) throws Exception {
        int pending = 0;
        for (MappingRow row : load(seasonId, Kind.COMPETITION)) {
            if ("DA_CONFIGURARE".equals(row.status())) pending++;
        }
        for (MappingRow row : load(seasonId, Kind.TEAM)) {
            if ("DA_CONFIGURARE".equals(row.status())) pending++;
        }
        return pending;
    }


    private static void synchronizeGroupedMappings(Connection c, Kind kind) throws Exception {
        String entityTable = kind == Kind.COMPETITION ? "rn_competition_season" : "rn_team_season";
        String entityId = kind == Kind.COMPETITION ? "competition_season_id" : "team_season_id";
        String mappingTable = kind == Kind.COMPETITION ? "rn_competition_mapping" : "rn_team_mapping";
        String identityId = kind == Kind.COMPETITION ? "competition_identity_id" : "team_identity_id";

        String groupsSql = "SELECT e.season_id,LOWER(TRIM(e.normalized_name))," +
            "COUNT(DISTINCT CASE WHEN m.mapping_status='ASSOCIATA' THEN m." + identityId + " END)," +
            "MIN(CASE WHEN m.mapping_status='ASSOCIATA' THEN m." + identityId + " END)," +
            "MAX(CASE WHEN m.mapping_status='ESCLUSA' THEN 1 ELSE 0 END) " +
            "FROM " + entityTable + " e LEFT JOIN " + mappingTable + " m ON m." + entityId + "=e." + entityId + " " +
            "GROUP BY e.season_id,LOWER(TRIM(e.normalized_name))";

        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(groupsSql)) {
            while (rs.next()) {
                String season = rs.getString(1);
                String normalized = rs.getString(2);
                int identities = rs.getInt(3);
                Long mapped = rs.getObject(4) == null ? null : rs.getLong(4);
                boolean excluded = rs.getInt(5) == 1;
                if (identities > 1) {
                    throw new IllegalStateException("Associazioni incoerenti per " + season + ": " + normalized);
                }
                if (mapped == null && !excluded) continue;
                String status = mapped != null ? "ASSOCIATA" : "ESCLUSA";
                String method = mapped != null ? "GUI_GROUP_SYNC" : "GUI_EXCLUDED_GROUP_SYNC";
                String update = "UPDATE " + mappingTable + " SET " + identityId + "=?,mapping_status=?,mapping_method=?,updated_at=? " +
                    "WHERE " + entityId + " IN (SELECT " + entityId + " FROM " + entityTable + " WHERE season_id=? AND LOWER(TRIM(normalized_name))=?)";
                try (PreparedStatement ps = c.prepareStatement(update)) {
                    if (mapped == null) ps.setNull(1, Types.BIGINT); else ps.setLong(1, mapped);
                    ps.setString(2, status);
                    ps.setString(3, method);
                    ps.setString(4, Instant.now().toString());
                    ps.setString(5, season);
                    ps.setString(6, normalized);
                    ps.executeUpdate();
                }
            }
        }
    }

    private static void compactObsoleteSources(Connection c) throws Exception {
        reanchorIdentities(c, Kind.COMPETITION);
        reanchorIdentities(c, Kind.TEAM);

        for (Kind kind : Kind.values()) {
            String entityTable = kind == Kind.COMPETITION ? "rn_competition_season" : "rn_team_season";
            String entityId = kind == Kind.COMPETITION ? "competition_season_id" : "team_season_id";
            String mappingTable = kind == Kind.COMPETITION ? "rn_competition_mapping" : "rn_team_mapping";
            String identityTable = kind == Kind.COMPETITION ? "rn_competition_identity" : "rn_team_identity";
            String anchorId = kind == Kind.COMPETITION ? "anchor_competition_season_id" : "anchor_team_season_id";

            String stale = "SELECT e." + entityId + " FROM " + entityTable + " e JOIN rn_source_file sf ON sf.source_file_id=e.source_file_id " +
                "WHERE sf.source_type='FCM' AND sf.import_id<>(SELECT MAX(sf2.import_id) FROM rn_source_file sf2 WHERE sf2.season_id=sf.season_id AND sf2.source_type='FCM') " +
                "AND NOT EXISTS(SELECT 1 FROM " + identityTable + " i WHERE i." + anchorId + "=e." + entityId + ")";
            try (Statement st = c.createStatement()) {
                st.executeUpdate("DELETE FROM " + mappingTable + " WHERE " + entityId + " IN (" + stale + ")");
                st.executeUpdate("DELETE FROM " + entityTable + " WHERE " + entityId + " IN (" + stale + ")");
            }
        }

        List<Long> obsoleteImports = new ArrayList<>();
        String obsoleteSql = "SELECT sf.import_id FROM rn_source_file sf WHERE sf.import_id<>(" +
            "SELECT MAX(sf2.import_id) FROM rn_source_file sf2 WHERE sf2.season_id=sf.season_id AND sf2.source_type=sf.source_type) " +
            "AND NOT EXISTS(SELECT 1 FROM rn_competition_season cs WHERE cs.source_file_id=sf.source_file_id) " +
            "AND NOT EXISTS(SELECT 1 FROM rn_team_season ts WHERE ts.source_file_id=sf.source_file_id)";
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(obsoleteSql)) {
            while (rs.next()) obsoleteImports.add(rs.getLong(1));
        }
        for (long importId : obsoleteImports) {
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM rn_source_file WHERE import_id=?")) {
                ps.setLong(1, importId); ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM rn_column_catalog WHERE import_id=?")) {
                ps.setLong(1, importId); ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM rn_table_catalog WHERE import_id=?")) {
                ps.setLong(1, importId); ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM rn_import WHERE import_id=?")) {
                ps.setLong(1, importId); ps.executeUpdate();
            }
        }
    }

    private static void reanchorIdentities(Connection c, Kind kind) throws Exception {
        String identityTable = kind == Kind.COMPETITION ? "rn_competition_identity" : "rn_team_identity";
        String identityId = kind == Kind.COMPETITION ? "competition_identity_id" : "team_identity_id";
        String anchorId = kind == Kind.COMPETITION ? "anchor_competition_season_id" : "anchor_team_season_id";
        String entityTable = kind == Kind.COMPETITION ? "rn_competition_season" : "rn_team_season";
        String entityId = kind == Kind.COMPETITION ? "competition_season_id" : "team_season_id";

        String sql = "SELECT i." + identityId + ",e.season_id,e.normalized_name FROM " + identityTable + " i " +
            "JOIN " + entityTable + " e ON e." + entityId + "=i." + anchorId + " " +
            "JOIN rn_source_file sf ON sf.source_file_id=e.source_file_id " +
            "WHERE sf.import_id<>(SELECT MAX(sf2.import_id) FROM rn_source_file sf2 WHERE sf2.season_id=sf.season_id AND sf2.source_type='FCM')";
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            List<Object[]> rows = new ArrayList<>();
            while (rs.next()) rows.add(new Object[]{rs.getLong(1), rs.getString(2), rs.getString(3)});
            for (Object[] row : rows) {
                long id = (Long) row[0];
                String season = (String) row[1];
                String normalized = (String) row[2];
                String latestSql = "SELECT e." + entityId + " FROM " + entityTable + " e JOIN rn_source_file sf ON sf.source_file_id=e.source_file_id " +
                    "WHERE e.season_id=? AND LOWER(TRIM(e.normalized_name))=LOWER(TRIM(?)) " +
                    "AND sf.import_id=(SELECT MAX(sf2.import_id) FROM rn_source_file sf2 WHERE sf2.season_id=e.season_id AND sf2.source_type='FCM') LIMIT 1";
                try (PreparedStatement find = c.prepareStatement(latestSql)) {
                    find.setString(1, season); find.setString(2, normalized);
                    try (ResultSet latest = find.executeQuery()) {
                        if (latest.next()) {
                            try (PreparedStatement update = c.prepareStatement("UPDATE " + identityTable + " SET " + anchorId + "=? WHERE " + identityId + "=?")) {
                                update.setLong(1, latest.getLong(1)); update.setLong(2, id); update.executeUpdate();
                            }
                        }
                    }
                }
            }
        }
    }
    private Connection open() throws Exception {
        Class.forName("org.sqlite.JDBC");
        Connection c = DriverManager.getConnection("jdbc:sqlite:" + database);
        try (Statement st = c.createStatement()) {
            st.execute("PRAGMA foreign_keys=ON");
            st.execute("PRAGMA busy_timeout=10000");
        }
        return c;
    }

    private static boolean isAnchor(Connection c, String seasonId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT is_anchor FROM rn_season WHERE season_id=?")) {
            ps.setString(1, seasonId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) == 1;
            }
        }
    }

    private static void consolidateDuplicateIdentities(Connection c, Kind kind) throws Exception {
        String identityTable = kind == Kind.COMPETITION ? "rn_competition_identity" : "rn_team_identity";
        String identityId = kind == Kind.COMPETITION ? "competition_identity_id" : "team_identity_id";
        String mappingTable = kind == Kind.COMPETITION ? "rn_competition_mapping" : "rn_team_mapping";

        String groupsSql = "SELECT anchor_season_id,LOWER(TRIM(canonical_name)),MIN(" + identityId + ") " +
            "FROM " + identityTable + " GROUP BY anchor_season_id,LOWER(TRIM(canonical_name)) HAVING COUNT(*)>1";
        List<long[]> duplicateGroups = new ArrayList<>();
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(groupsSql)) {
            while (rs.next()) {
                String anchorSeason = rs.getString(1);
                String normalizedName = rs.getString(2);
                long keeper = rs.getLong(3);
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT " + identityId + " FROM " + identityTable +
                        " WHERE anchor_season_id=? AND LOWER(TRIM(canonical_name))=? AND " + identityId + "<>?")) {
                    ps.setString(1, anchorSeason);
                    ps.setString(2, normalizedName);
                    ps.setLong(3, keeper);
                    try (ResultSet duplicates = ps.executeQuery()) {
                        while (duplicates.next()) duplicateGroups.add(new long[]{keeper, duplicates.getLong(1)});
                    }
                }
            }
        }

        for (long[] pair : duplicateGroups) {
            long keeper = pair[0];
            long duplicate = pair[1];
            try (PreparedStatement update = c.prepareStatement(
                    "UPDATE " + mappingTable + " SET " + identityId + "=? WHERE " + identityId + "=?")) {
                update.setLong(1, keeper);
                update.setLong(2, duplicate);
                update.executeUpdate();
            }
            try (PreparedStatement delete = c.prepareStatement(
                    "DELETE FROM " + identityTable + " WHERE " + identityId + "=?")) {
                delete.setLong(1, duplicate);
                delete.executeUpdate();
            }
        }
    }

    private static long createIdentity(Connection c, Kind kind, String seasonId,
                                       long entityId, String name) throws Exception {
        String table = kind == Kind.COMPETITION ? "rn_competition_identity" : "rn_team_identity";
        String anchorCol = kind == Kind.COMPETITION ? "anchor_competition_season_id" : "anchor_team_season_id";
        String sql = "INSERT INTO " + table + "(anchor_season_id," + anchorCol + ",canonical_name,created_at) VALUES(?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, seasonId); ps.setLong(2, entityId); ps.setString(3, name); ps.setString(4, Instant.now().toString());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) throw new IllegalStateException("Identita non creata: " + name);
                return rs.getLong(1);
            }
        }
    }

    private static void updateMapping(Connection c, Kind kind, long entityId, Long identityId,
                                      String status, String method) throws Exception {
        String table = kind == Kind.COMPETITION ? "rn_competition_mapping" : "rn_team_mapping";
        String entityCol = kind == Kind.COMPETITION ? "competition_season_id" : "team_season_id";
        String identityCol = kind == Kind.COMPETITION ? "competition_identity_id" : "team_identity_id";
        String sql = "INSERT INTO " + table + "(" + entityCol + "," + identityCol + ",mapping_status,mapping_method,notes,updated_at) " +
            "VALUES(?,?,?,?,NULL,?) ON CONFLICT(" + entityCol + ") DO UPDATE SET " + identityCol + "=excluded." + identityCol + "," +
            "mapping_status=excluded.mapping_status,mapping_method=excluded.mapping_method,notes=NULL,updated_at=excluded.updated_at";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, entityId);
            if (identityId == null) ps.setNull(2, Types.BIGINT); else ps.setLong(2, identityId);
            ps.setString(3, status); ps.setString(4, method); ps.setString(5, Instant.now().toString());
            ps.executeUpdate();
        }
    }

    private static int similarityRank(String normalized, String candidate) {
        String a = normalize(normalized), b = normalize(candidate);
        if (a.equals(b)) return 0;
        if (a.contains(b) || b.contains(a)) return 1;
        return 2;
    }

    private static String normalize(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private static final class Group {
        final String sourceName;
        final String normalizedName;
        final List<Long> ids = new ArrayList<>();
        String status = "DA_CONFIGURARE";
        Long identityId;
        Group(String sourceName, String normalizedName) {
            this.sourceName = sourceName;
            this.normalizedName = normalizedName;
        }
        void accept(String candidateStatus, Long candidateIdentity) {
            if (candidateIdentity != null && identityId == null) identityId = candidateIdentity;
            if ("ASSOCIATA".equals(candidateStatus)) status = "ASSOCIATA";
            else if (!"ASSOCIATA".equals(status) && "ESCLUSA".equals(candidateStatus)) status = "ESCLUSA";
        }
    }
}
