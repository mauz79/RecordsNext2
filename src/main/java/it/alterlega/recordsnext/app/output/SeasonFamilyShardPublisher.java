package it.alterlega.recordsnext.app.output;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;

/**
 * Trasforma gli output cumulativi RecordsNext in facade compatibili e shard stagionali.
 *
 * <p>Ogni shard appartiene fisicamente al sito della propria stagione. Le facade conservano
 * gli stessi nomi file e gli stessi globali pubblici usati dal frontend corrente. Gli shard
 * vengono caricati come normali script, quindi non richiedono PHP, rewrite, CORS o API server.
 *
 * <p>La disponibilita' e' progressiva: la stagione anchor corrente e' sempre caricabile;
 * le stagioni storiche entrano nelle facade online solo quando risultano esplicitamente
 * abilitate nello stato persistente. In questo modo un sito nuovo puo' partire con la sola
 * stagione corrente senza produrre richieste 404 verso siti storici non ancora pubblicati.
 */
public final class SeasonFamilyShardPublisher {
    public static final long DEFAULT_MAX_SHARD_BYTES = 1024L * 1024L;
    public static final String DATA_DIR = "recordsnext-data";
    public static final String STATE_FILE_NAME = "recordsnext-shards.properties";

    private static final List<FamilySpec> FAMILIES = List.of(
            new FamilySpec("Classics", "fcmRecordsNext_Classics.js", "window.fcmRecordsNextClassics",
                    "stagione", List.of("seasonAggregates")),
            new FamilySpec("Series", "fcmRecordsNext_Series.js", "window.fcmRecordsNextSeries",
                    "stagione", List.of("seasonAggregates")),
            new FamilySpec("RU", "fcmRecordsNext_RU.js", "window.fcmRecordsNextRU",
                    "stagione", List.of("seasonAggregates")),
            new FamilySpec("Modifiers", "fcmRecordsNext_Modifiers.js", "window.fcmRecordsNextModifiers",
                    "stagione", List.of("seasonAggregates")),
            new FamilySpec("ThresholdsLuck", "fcmRecordsNext_ThresholdsLuck.js", "window.fcmRecordsNextThresholdsLuck",
                    "seasonId", List.of("events", "seasonAggregates")),
            new FamilySpec("Culometro", "fcmRecordsNext_Culometro.js", "window.fcmRecordsNextCulometro",
                    "seasonId", List.of("events", "ranking", "competitionRanking")),
            new FamilySpec("Matches", "fcmRecordsNext_Matches.js", "window.fcmRecordsNextMatches",
                    "seasonId", List.of("matches"))
    );

    private SeasonFamilyShardPublisher() {}

    public static Plan prepare(Path generatedDir, Path shardStagingRoot) throws IOException {
        Path transientState = shardStagingRoot.resolve(STATE_FILE_NAME);
        return prepare(generatedDir, shardStagingRoot, transientState, DEFAULT_MAX_SHARD_BYTES);
    }

    public static Plan prepare(Path generatedDir, Path shardStagingRoot, Path stateFile) throws IOException {
        return prepare(generatedDir, shardStagingRoot, stateFile, DEFAULT_MAX_SHARD_BYTES);
    }

    public static Plan prepare(Path generatedDir, Path shardStagingRoot, Path stateFile, long maxShardBytes)
            throws IOException {
        Objects.requireNonNull(generatedDir, "generatedDir");
        Objects.requireNonNull(shardStagingRoot, "shardStagingRoot");
        Objects.requireNonNull(stateFile, "stateFile");
        if (maxShardBytes < 1) throw new IllegalArgumentException("maxShardBytes deve essere positivo");

        Path coreFile = generatedDir.resolve("fcmRecordsNext_Core.js");
        if (!Files.isRegularFile(coreFile)) {
            throw new IOException("Core richiesto per la distribuzione stagionale: " + coreFile);
        }
        Assignment core = readAssignment(coreFile, "window.fcmRecordsNextCore");
        Map<String, SeasonRoute> routes = readRoutes(core.root());
        String anchorSeasonId = routes.values().stream()
                .filter(SeasonRoute::anchor)
                .map(SeasonRoute::seasonId)
                .findFirst()
                .orElseThrow(() -> new IOException("Core senza stagione anchor"));
        ShardState state = ShardState.load(stateFile);
        Files.createDirectories(shardStagingRoot);

        List<Shard> shards = new ArrayList<>();
        List<Path> facades = new ArrayList<>();
        for (FamilySpec spec : FAMILIES) {
            Path familyFile = generatedDir.resolve(spec.fileName());
            if (!Files.isRegularFile(familyFile)) continue;
            Assignment assignment = readAssignment(familyFile, spec.globalName());
            Map<String, Object> root = assignment.root();
            Map<String, Object> facadeRoot = deepCopyMap(root);
            for (String field : spec.shardedFields()) facadeRoot.put(field, new ArrayList<>());

            Map<String, Map<String, List<Object>>> bySeason = splitBySeason(root, spec);
            for (Map.Entry<String, Map<String, List<Object>>> entry : bySeason.entrySet()) {
                String seasonId = entry.getKey();
                SeasonRoute route = routes.get(seasonId);
                if (route == null) {
                    throw new IOException("Nessun percorso sito nel Core per la stagione " + seasonId
                            + " richiesta da " + spec.fileName());
                }
                String shardName = "fcmRecordsNext_" + spec.id() + "." + seasonId + ".js";
                Path staged = shardStagingRoot.resolve(seasonId).resolve(DATA_DIR).resolve(shardName);
                Files.createDirectories(staged.getParent());
                String shardJs = renderShard(spec, entry.getValue());
                Files.writeString(staged, shardJs, StandardCharsets.UTF_8);
                long bytes = Files.size(staged);
                if (bytes >= maxShardBytes) {
                    throw new IOException("Shard oltre il limite di " + maxShardBytes + " byte: "
                            + staged.getFileName() + " = " + bytes);
                }
                Path target = route.localSitePath() == null ? null
                        : route.localSitePath().resolve("js").resolve(DATA_DIR).resolve(shardName);
                String onlineUrl = route.onlineSiteUrl() == null ? null
                        : joinUrl(route.onlineSiteUrl(), "js/" + DATA_DIR + "/" + shardName);
                String localUrl = target == null ? null : target.toUri().toString();
                boolean current = seasonId.equals(anchorSeasonId);
                boolean localActive = current || state.localEnabled(seasonId)
                        || (target != null && Files.isRegularFile(target));
                boolean onlineActive = current || state.onlineEnabled(seasonId);
                shards.add(new Shard(spec.id(), seasonId, staged, target, onlineUrl, localUrl, bytes,
                        sha256(staged), current, localActive, onlineActive));
            }

            String facadeJs = renderFacade(spec, facadeRoot, shardsForFamily(shards, spec.id()));
            Files.writeString(familyFile, facadeJs, StandardCharsets.UTF_8);
            facades.add(familyFile);
        }
        return new Plan(List.copyOf(shards), List.copyOf(facades), anchorSeasonId, stateFile);
    }

    /**
     * Pubblica solo shard attivi localmente. La stagione corrente e' sempre attiva; una storica
     * viene attivata quando era gia' presente o quando lo stato la abilita esplicitamente.
     */
    public static PublishStats publishShards(Plan plan) throws IOException {
        ShardState state = ShardState.load(plan.stateFile());
        int written = 0;
        int unchanged = 0;
        int skippedUnavailable = 0;
        for (Shard shard : plan.shards()) {
            if (!shard.localActive() || shard.target() == null) {
                skippedUnavailable++;
                continue;
            }
            Path siteRoot = shard.target().getParent().getParent().getParent();
            if (!shard.currentSeason() && !Files.isDirectory(siteRoot)) {
                skippedUnavailable++;
                continue;
            }
            Files.createDirectories(shard.target().getParent());
            String stateHash = state.fingerprint(shard.familyId(), shard.seasonId());
            if (Files.isRegularFile(shard.target())
                    && shard.sha256().equals(stateHash)
                    && Files.mismatch(shard.stagedFile(), shard.target()) == -1L) {
                unchanged++;
            } else if (Files.isRegularFile(shard.target())
                    && Files.mismatch(shard.stagedFile(), shard.target()) == -1L) {
                unchanged++;
            } else {
                Path temp = shard.target().resolveSibling("." + shard.target().getFileName()
                        + ".recordsnext-" + UUID.randomUUID() + ".tmp");
                Files.copy(shard.stagedFile(), temp, StandardCopyOption.REPLACE_EXISTING);
                moveReplace(temp, shard.target());
                written++;
            }
            state.setLocalEnabled(shard.seasonId(), true);
            state.setFingerprint(shard.familyId(), shard.seasonId(), shard.sha256());
            state.setBytes(shard.familyId(), shard.seasonId(), shard.bytes());
        }
        state.setLastAnchor(plan.anchorSeasonId());
        state.save(plan.stateFile());
        return new PublishStats(written, unchanged, skippedUnavailable);
    }

    /** Abilita una stagione storica nelle facade online dopo che i relativi shard sono stati caricati sul web. */
    public static void setOnlineAvailability(Path stateFile, String seasonId, boolean available) throws IOException {
        ShardState state = ShardState.load(stateFile);
        state.setOnlineEnabled(seasonId, available);
        state.save(stateFile);
    }

    /** Abilita/disabilita la materializzazione locale di una stagione storica. */
    public static void setLocalAvailability(Path stateFile, String seasonId, boolean available) throws IOException {
        ShardState state = ShardState.load(stateFile);
        state.setLocalEnabled(seasonId, available);
        state.save(stateFile);
    }

    private static List<Shard> shardsForFamily(List<Shard> shards, String familyId) {
        return shards.stream().filter(s -> s.familyId().equals(familyId))
                .sorted(Comparator.comparing(Shard::seasonId)).toList();
    }

    private static Map<String, Map<String, List<Object>>> splitBySeason(Map<String, Object> root, FamilySpec spec)
            throws IOException {
        Map<String, Map<String, List<Object>>> out = new LinkedHashMap<>();
        for (String field : spec.shardedFields()) {
            Object raw = root.get(field);
            if (!(raw instanceof List<?> list)) {
                throw new IOException("Campo array mancante in " + spec.fileName() + ": " + field);
            }
            for (int index = 0; index < list.size(); index++) {
                Object item = list.get(index);
                if (!(item instanceof Map<?, ?> map)) {
                    throw new IOException("Riga non-oggetto in " + spec.fileName() + ": " + field);
                }
                String seasonId = text(map.get(spec.seasonField()));
                if (seasonId.isBlank()) {
                    throw new IOException("Stagione mancante in " + spec.fileName() + ": " + field + "[" + index + "]");
                }
                Map<String, List<Object>> fields = out.computeIfAbsent(seasonId, ignored -> new LinkedHashMap<>());
                List<Object> rows = fields.computeIfAbsent(field, ignored -> new ArrayList<>());
                rows.add(List.of(new RawNumber(Integer.toString(index)), item));
            }
        }
        for (Map<String, List<Object>> fields : out.values()) {
            for (String field : spec.shardedFields()) fields.computeIfAbsent(field, ignored -> new ArrayList<>());
        }
        return out;
    }

    private static String renderShard(FamilySpec spec, Map<String, List<Object>> indexedRows) {
        return "(function(){var q=window.__recordsNextShardQueue=window.__recordsNextShardQueue||{};"
                + "var f=q[\"" + spec.id() + "\"]=q[\"" + spec.id() + "\"]||{};var d="
                + Json.write(indexedRows) + ";"
                + spec.shardedFields().stream()
                    .map(field -> "(f." + field + "=f." + field + "||[]).push.apply(f." + field + ",d." + field + ");")
                    .reduce("", String::concat)
                + "})();\n";
    }

    private static String renderFacade(FamilySpec spec, Map<String, Object> facadeRoot, List<Shard> shards)
            throws IOException {
        List<String> online = new ArrayList<>();
        List<String> local = new ArrayList<>();
        for (Shard shard : shards) {
            if (shard.onlineActive()) {
                if (shard.onlineUrl() == null || shard.onlineUrl().isBlank()) {
                    throw new IOException("URL online mancante per stagione online " + shard.seasonId() + " / " + spec.id());
                }
                online.add(shard.onlineUrl());
            }
            if (shard.localActive()) {
                if (shard.localUrl() != null && !shard.localUrl().isBlank()) local.add(shard.localUrl());
                else if (shard.onlineUrl() != null && !shard.onlineUrl().isBlank()) local.add(shard.onlineUrl());
            }
        }
        String fields = Json.write(spec.shardedFields());
        String finalizer = "(function(){var q=window.__recordsNextShardQueue||{},f=q[\"" + spec.id()
                + "\"]||{},r=" + spec.globalName() + ",fields=" + fields
                + ";for(var x=0;x<fields.length;x++){var k=fields[x],a=f[k]||[];"
                + "a.sort(function(A,B){return A[0]-B[0]});r[k]=a.map(function(v){return v[1]});}"
                + "if(q[\"" + spec.id() + "\"])delete q[\"" + spec.id() + "\"];})();";
        String finalizerHtml = finalizer.replace("\\", "\\\\").replace("'", "\\'");
        return spec.globalName() + "=" + Json.write(facadeRoot) + ";\n"
                + "(function(){var o=" + Json.write(online) + ",l=" + Json.write(local)
                + ",u=(location.protocol==='file:'?l:o);"
                + "if(document.readyState!=='loading')throw new Error('RecordsNext shard loader deve essere caricato durante il parsing HTML');"
                + "for(var i=0;i<u.length;i++){var s=String(u[i]).replace(/&/g,'&amp;').replace(/\"/g,'&quot;');"
                + "document.write('<script src=\"'+s+'\"><\\/script>');}"
                + "document.write('<script>" + finalizerHtml + "<\\/script>');})();\n";
    }

    private static Map<String, SeasonRoute> readRoutes(Map<String, Object> core) throws IOException {
        Object raw = core.get("seasons");
        if (!(raw instanceof List<?> list)) throw new IOException("Core senza seasons");
        Map<String, SeasonRoute> routes = new LinkedHashMap<>();
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> map)) continue;
            String seasonId = text(map.get("seasonId"));
            if (seasonId.isBlank()) continue;
            String local = text(map.get("localSitePath"));
            String online = text(map.get("onlineSiteUrl"));
            boolean anchor = bool(map.get("isAnchor"));
            Path localPath = local.isBlank() ? null : Path.of(local).toAbsolutePath().normalize();
            routes.put(seasonId, new SeasonRoute(seasonId, localPath, blankToNull(online), anchor));
        }
        return routes;
    }

    private static Assignment readAssignment(Path file, String expectedGlobal) throws IOException {
        String source = Files.readString(file, StandardCharsets.UTF_8);
        if (!source.isEmpty() && source.charAt(0) == '\uFEFF') source = source.substring(1);
        String trimmed = source.trim();
        String prefix = expectedGlobal + " = ";
        if (!trimmed.startsWith(prefix)) {
            prefix = expectedGlobal + "=";
            if (!trimmed.startsWith(prefix)) throw new IOException("Prefisso JS inatteso: " + file);
        }
        if (!trimmed.endsWith(";")) throw new IOException("Terminatore JS mancante: " + file);
        String json = trimmed.substring(prefix.length(), trimmed.length() - 1).trim();
        Object parsed = new Json.Parser(json).parse();
        if (!(parsed instanceof Map<?, ?> map)) throw new IOException("Radice JSON non-oggetto: " + file);
        Map<String, Object> root = new LinkedHashMap<>();
        for (Map.Entry<?, ?> e : map.entrySet()) root.put(String.valueOf(e.getKey()), e.getValue());
        return new Assignment(expectedGlobal, root);
    }

    private static Map<String, Object> deepCopyMap(Map<String, Object> source) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : source.entrySet()) out.put(e.getKey(), deepCopy(e.getValue()));
        return out;
    }
    private static Object deepCopy(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> out = new LinkedHashMap<>();
            for (Map.Entry<?, ?> e : map.entrySet()) out.put(String.valueOf(e.getKey()), deepCopy(e.getValue()));
            return out;
        }
        if (value instanceof List<?> list) {
            List<Object> out = new ArrayList<>(list.size());
            for (Object item : list) out.add(deepCopy(item));
            return out;
        }
        return value;
    }

    private static String sha256(Path file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream in = Files.newInputStream(file)) {
                byte[] buffer = new byte[8192];
                for (int n; (n = in.read(buffer)) >= 0;) digest.update(buffer, 0, n);
            }
            StringBuilder out = new StringBuilder(64);
            for (byte b : digest.digest()) out.append(String.format("%02x", b & 0xff));
            return out.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 non disponibile", ex);
        }
    }

    private static String joinUrl(String base, String relative) {
        String b = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        return b + "/" + relative;
    }
    private static String blankToNull(String value) { return value == null || value.isBlank() ? null : value; }
    private static String text(Object value) { return value == null ? "" : String.valueOf(value); }
    private static boolean bool(Object value) {
        if (value instanceof Boolean b) return b;
        if (value instanceof Number n) return n.intValue() != 0;
        String s = text(value);
        return "1".equals(s) || "true".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s);
    }

    private static void moveReplace(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private record FamilySpec(String id, String fileName, String globalName, String seasonField, List<String> shardedFields) {}
    private record Assignment(String globalName, Map<String, Object> root) {}
    private record SeasonRoute(String seasonId, Path localSitePath, String onlineSiteUrl, boolean anchor) {}
    private record RawNumber(String value) {}

    public record Shard(String familyId, String seasonId, Path stagedFile, Path target,
                        String onlineUrl, String localUrl, long bytes, String sha256,
                        boolean currentSeason, boolean localActive, boolean onlineActive) {}
    public record Plan(List<Shard> shards, List<Path> facades, String anchorSeasonId, Path stateFile) {
        public long totalShardBytes() { return shards.stream().mapToLong(Shard::bytes).sum(); }
        public long maxShardBytes() { return shards.stream().mapToLong(Shard::bytes).max().orElse(0L); }
        public int onlineSeasonCount() { return (int) shards.stream().filter(Shard::onlineActive).map(Shard::seasonId).distinct().count(); }
        public int localSeasonCount() { return (int) shards.stream().filter(Shard::localActive).map(Shard::seasonId).distinct().count(); }
    }
    public record PublishStats(int written, int unchanged, int skippedUnavailable) {}

    private static final class ShardState {
        private final Properties properties = new Properties();

        static ShardState load(Path file) throws IOException {
            ShardState state = new ShardState();
            if (Files.isRegularFile(file)) {
                try (InputStream in = Files.newInputStream(file)) { state.properties.load(in); }
            }
            return state;
        }
        boolean localEnabled(String season) { return Boolean.parseBoolean(properties.getProperty("season." + season + ".local", "false")); }
        boolean onlineEnabled(String season) { return Boolean.parseBoolean(properties.getProperty("season." + season + ".online", "false")); }
        String fingerprint(String family, String season) { return properties.getProperty("season." + season + ".sha256." + family, ""); }
        void setLocalEnabled(String season, boolean value) { properties.setProperty("season." + season + ".local", Boolean.toString(value)); }
        void setOnlineEnabled(String season, boolean value) { properties.setProperty("season." + season + ".online", Boolean.toString(value)); }
        void setFingerprint(String family, String season, String hash) { properties.setProperty("season." + season + ".sha256." + family, hash); }
        void setBytes(String family, String season, long bytes) { properties.setProperty("season." + season + ".bytes." + family, Long.toString(bytes)); }
        void setLastAnchor(String season) { properties.setProperty("lastAnchorSeason", season); }
        void save(Path file) throws IOException {
            Path parent = file.toAbsolutePath().normalize().getParent();
            if (parent != null) Files.createDirectories(parent);
            properties.setProperty("updatedAt", Instant.now().toString());
            Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
            try (OutputStream out = Files.newOutputStream(tmp)) {
                properties.store(out, "RecordsNext season shard state");
            }
            moveReplace(tmp, file);
        }
    }

    /** Piccolo parser/writer JSON locale, per non aggiungere dipendenze al runtime. */
    private static final class Json {
        static String write(Object value) { StringBuilder out = new StringBuilder(); write(value, out); return out.toString(); }
        private static void write(Object value, StringBuilder out) {
            if (value == null) { out.append("null"); return; }
            if (value instanceof RawNumber n) { out.append(n.value()); return; }
            if (value instanceof String s) { string(s, out); return; }
            if (value instanceof Number || value instanceof Boolean) { out.append(value); return; }
            if (value instanceof Map<?, ?> map) {
                out.append('{'); boolean first = true;
                for (Map.Entry<?, ?> e : map.entrySet()) {
                    if (!first) out.append(','); first = false; string(String.valueOf(e.getKey()), out); out.append(':'); write(e.getValue(), out);
                }
                out.append('}'); return;
            }
            if (value instanceof Iterable<?> iterable) {
                out.append('['); boolean first = true;
                for (Object item : iterable) { if (!first) out.append(','); first = false; write(item, out); }
                out.append(']'); return;
            }
            string(String.valueOf(value), out);
        }
        private static void string(String s, StringBuilder out) {
            out.append('"');
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                switch (c) {
                    case '"' -> out.append("\\\"");
                    case '\\' -> out.append("\\\\");
                    case '\b' -> out.append("\\b");
                    case '\f' -> out.append("\\f");
                    case '\n' -> out.append("\\n");
                    case '\r' -> out.append("\\r");
                    case '\t' -> out.append("\\t");
                    default -> { if (c < 0x20) out.append(String.format("\\u%04x", (int)c)); else out.append(c); }
                }
            }
            out.append('"');
        }

        private static final class Parser {
            private final String text; private int index;
            Parser(String text) { this.text = text; }
            Object parse() { skip(); Object v = value(); skip(); if (index != text.length()) fail(); return v; }
            private Object value() {
                skip(); if (index >= text.length()) fail();
                return switch (text.charAt(index)) {
                    case '{' -> object(); case '[' -> array(); case '"' -> string();
                    case 't' -> literal("true", Boolean.TRUE); case 'f' -> literal("false", Boolean.FALSE);
                    case 'n' -> literal("null", null); default -> number();
                };
            }
            private Map<String,Object> object() {
                expect('{'); Map<String,Object> out = new LinkedHashMap<>(); skip();
                if (peek('}')) { index++; return out; }
                while (true) { String k = string(); expect(':'); out.put(k, value()); skip(); if (peek('}')) { index++; return out; } expect(','); }
            }
            private List<Object> array() {
                expect('['); List<Object> out = new ArrayList<>(); skip();
                if (peek(']')) { index++; return out; }
                while (true) { out.add(value()); skip(); if (peek(']')) { index++; return out; } expect(','); }
            }
            private String string() {
                expect('"'); StringBuilder out = new StringBuilder();
                while (index < text.length()) {
                    char c = text.charAt(index++); if (c == '"') return out.toString();
                    if (c != '\\') { out.append(c); continue; }
                    if (index >= text.length()) fail(); char e = text.charAt(index++);
                    switch (e) {
                        case '"' -> out.append('"'); case '\\' -> out.append('\\'); case '/' -> out.append('/');
                        case 'b' -> out.append('\b'); case 'f' -> out.append('\f'); case 'n' -> out.append('\n');
                        case 'r' -> out.append('\r'); case 't' -> out.append('\t');
                        case 'u' -> { if (index + 4 > text.length()) fail(); out.append((char)Integer.parseInt(text.substring(index,index+4),16)); index += 4; }
                        default -> fail();
                    }
                }
                fail(); return "";
            }
            private Object number() {
                int start = index; if (peek('-')) index++;
                while (index < text.length() && Character.isDigit(text.charAt(index))) index++;
                if (index < text.length() && text.charAt(index) == '.') { index++; while (index < text.length() && Character.isDigit(text.charAt(index))) index++; }
                if (index < text.length() && (text.charAt(index) == 'e' || text.charAt(index) == 'E')) {
                    index++; if (index < text.length() && (text.charAt(index) == '+' || text.charAt(index) == '-')) index++;
                    while (index < text.length() && Character.isDigit(text.charAt(index))) index++;
                }
                String n = text.substring(start,index); try { return new BigDecimal(n); } catch (Exception ex) { fail(); return null; }
            }
            private Object literal(String token,Object value) { if (!text.startsWith(token,index)) fail(); index += token.length(); return value; }
            private void skip() { while (index < text.length() && Character.isWhitespace(text.charAt(index))) index++; }
            private boolean peek(char c) { skip(); return index < text.length() && text.charAt(index) == c; }
            private void expect(char c) { skip(); if (index >= text.length() || text.charAt(index) != c) fail(); index++; }
            private void fail() { throw new IllegalArgumentException("JSON non valido a " + index); }
        }
    }
}
