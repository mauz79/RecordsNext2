# RecordsNext 4.0 - Bibbia incrementale v16

Lettore JDBC FCA di sola lettura collegato al modello Java Top & Flop.
**Non integrato ancora nel motore di elaborazione/pubblicazione.**

## Provenienza

- Branch: `feature/recordsnext4-players`
- HEAD: `f9bf30323d9204d70e91e242bddfc72c2060ecf9`
- Riferimento preservato: `docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v15.md`
- SHA-256 bibbia v15: `fc8a98a3bd35a312117a3180b39b028bf72859abf8f607dfcf6b03ecc73881b8`
- Codice Java 3.2 esistente: invariato in questa installazione.

## File integrali e impronte SHA-256

### `src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopFcaJdbcReader.java`

SHA-256: `de001ce9d9f3f73b10873a8d4ba6783656fde4e15d0e684c3c82873ed9f1352d`

~~~~java
package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.topflop.TopFlopFacts.PlayerSeason;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.RealDay;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.RealDayKey;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.VoteState;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Read-only FCA source adapter for the native Top & Flop model.
 * Does not create tables, generate JS, infer fantasy exclusion, or alter the 3.2 pipeline.
 * The caller supplies the SAME SQLite JDBC connection strategy as RecordsNext 3.2.
 */
public final class TopFlopFcaJdbcReader {
    private TopFlopFcaJdbcReader() {}

    public record PlayerProfile(PlayerSeason player, String name, Map<String, String> originalFields) {
        public PlayerProfile {
            Objects.requireNonNull(player, "player");
            Objects.requireNonNull(name, "name");
            originalFields = Map.copyOf(Objects.requireNonNull(originalFields, "originalFields"));
        }
    }

    public record SeasonLoad(String seasonId, Map<Integer, PlayerProfile> playersByLocalId,
                             int sourceGiocaInRows, int realDaysAdded, int validVotes,
                             int nonValidVotes, int zeroOrUnclassifiedVotes) {
        public SeasonLoad {
            playersByLocalId = Map.copyOf(playersByLocalId);
        }
    }

    public static SeasonLoad loadSeason(Connection connection, TopFlopArchive archive,
                                        String seasonId, Integer gazzaCode) throws SQLException {
        Objects.requireNonNull(connection, "connection");
        Objects.requireNonNull(archive, "archive");
        if (seasonId == null || !seasonId.matches("[0-9]{4}_[0-9]{4}")) {
            throw new IllegalArgumentException("Invalid seasonId: " + seasonId);
        }
        if (gazzaCode != null && gazzaCode <= 0) {
            throw new IllegalArgumentException("gazzaCode filter must be positive");
        }
        String prefix = "raw_" + seasonId + "_fca_";
        String playerTable = prefix + "giocatorea";
        String dayTable = prefix + "giocain";
        String scoreTable = prefix + "punteggio";
        List<String> playerColumns = columns(connection, playerTable);
        List<String> dayColumns = columns(connection, dayTable);
        List<String> scoreColumns = columns(connection, scoreTable);
        requireColumns(playerColumns, playerTable, "ID", "CODICEGAZZA", "NOME");
        requireColumns(dayColumns, dayTable, "IDGIOCATORE", "GIORNATA", "IDPUNTEGGIO");
        requireColumns(scoreColumns, scoreTable, "ID", "VOTO1", "VALORESPECIALE");
        Map<Integer, PlayerProfile> profiles = new LinkedHashMap<>();
        String filter = gazzaCode == null ? "" : " WHERE a.\"CODICEGAZZA\" = ?";
        String playerSql = "SELECT " + projection("a", "a__", playerColumns) +
            " FROM \"" + playerTable + "\" a" + filter + " ORDER BY a.\"ID\"";
        try (PreparedStatement statement = connection.prepareStatement(playerSql)) {
            if (gazzaCode != null) statement.setInt(1, gazzaCode);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    int localId = rows.getInt("a__ID");
                    int rawCode = rows.getInt("a__CODICEGAZZA");
                    Integer code = rows.wasNull() || rawCode <= 0 ? null : rawCode;
                    PlayerSeason key = new PlayerSeason(seasonId, localId, code);
                    PlayerProfile profile = new PlayerProfile(key, rows.getString("a__NOME"),
                            sourceFields(rows, "GiocatoreA", "a__", playerColumns));
                    if (profiles.putIfAbsent(localId, profile) != null)
                        throw new SQLException("Duplicate FCA player ID " + localId + ": " + seasonId);
                }
            }
        }
        String joined = " FROM \"" + dayTable + "\" g " +
                "JOIN \"" + playerTable + "\" a ON a.\"ID\"=g.\"IDGIOCATORE\" " +
                "JOIN \"" + scoreTable + "\" p ON p.\"ID\"=g.\"IDPUNTEGGIO\"";
        String where = gazzaCode == null ? "" : " WHERE a.\"CODICEGAZZA\"=?";
        String sourceSql = "SELECT " + projection("g", "g__", dayColumns) + "," +
                projection("p", "p__", scoreColumns) + "," + projection("a", "a__", playerColumns) +
                joined + where + " ORDER BY g.\"IDGIOCATORE\",g.\"GIORNATA\"";
        int sourceCount = countSourceRows(connection, dayTable, playerTable, gazzaCode);
        if (gazzaCode == null) {
            int rawCount = countRawRows(connection, dayTable);
            if (sourceCount != rawCount) throw new SQLException(
                "Orphan FCA GiocaIn players in " + seasonId + ": raw=" + rawCount + " joined=" + sourceCount);
        }
        int count = 0, valid = 0, nonvalid = 0, zeros = 0;
        try (PreparedStatement statement = connection.prepareStatement(sourceSql)) {
            if (gazzaCode != null) statement.setInt(1, gazzaCode);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    int localId = rows.getInt("g__IDGIOCATORE");
                    PlayerProfile profile = profiles.get(localId);
                    if (profile == null) throw new SQLException("Missing FCA player " + localId);
                    int round = rows.getInt("g__GIORNATA");
                    BigDecimal originalVote = rows.getBigDecimal("p__VOTO1");
                    BigDecimal special = rows.getBigDecimal("p__VALORESPECIALE");
                    VoteState state;
                    BigDecimal vote = null;
                    if (originalVote != null && originalVote.compareTo(BigDecimal.ZERO) > 0) {
                        state = VoteState.VALID;
                        vote = originalVote;
                        valid++;
                    } else if (special != null && special.compareTo(BigDecimal.ZERO) != 0) {
                        state = VoteState.SPECIAL;
                        nonvalid++;
                    } else if (originalVote != null && originalVote.compareTo(BigDecimal.ZERO) < 0) {
                        state = VoteState.NO_VOTE;
                        nonvalid++;
                    } else {
                        state = VoteState.UNKNOWN; // 0 or SQL NULL: never infer played/not played.
                        nonvalid++;
                        zeros++;
                    }
                    Map<String, BigDecimal> events = new LinkedHashMap<>();
                    for (String column : scoreColumns) {
                        String upper = column.toUpperCase(Locale.ROOT);
                        if (upper.equals("ID") || upper.startsWith("VOTO") ||
                            upper.equals("VALORESPECIALE") || upper.startsWith("FINDER")) continue;
                        BigDecimal number = rows.getBigDecimal("p__" + column);
                        if (number != null) events.put(column, number); // Never apply bonus coefficients here.
                    }
                    Map<String, String> original = new LinkedHashMap<>(profile.originalFields());
                    original.putAll(sourceFields(rows, "GiocaIn", "g__", dayColumns));
                    original.putAll(sourceFields(rows, "Punteggio", "p__", scoreColumns));
                    RealDay fact = new RealDay(new RealDayKey(profile.player(), round),
                            state, vote, events, original);
                    archive.addRealDay(fact); // Fail closed on contradictory duplicate player/day.
                    count++;
                }
            }
        }
        if (count != sourceCount) throw new SQLException("FCA join lost records in " + seasonId +
                ": source=" + sourceCount + " joined=" + count);
        return new SeasonLoad(seasonId, profiles, sourceCount, count, valid, nonvalid, zeros);
    }

    private static int countRawRows(Connection c, String dayTable) throws SQLException {
        try (Statement s = c.createStatement(); ResultSet r = s.executeQuery(
                "SELECT count(*) FROM \"" + dayTable + "\"")) { return r.getInt(1); }
    }

    private static int countSourceRows(Connection c, String dayTable, String playerTable,
                                      Integer code) throws SQLException {
        String sql = "SELECT count(*) FROM \"" + dayTable + "\" g JOIN \"" + playerTable +
                "\" a ON a.\"ID\"=g.\"IDGIOCATORE\"" +
                (code == null ? "" : " WHERE a.\"CODICEGAZZA\"=?");
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            if (code != null) ps.setInt(1, code);
            try (ResultSet r = ps.executeQuery()) { return r.getInt(1); }
        }
    }

    private static List<String> columns(Connection c, String table) throws SQLException {
        List<String> columns = new ArrayList<>();
        try (Statement statement = c.createStatement();
             ResultSet result = statement.executeQuery("PRAGMA table_info(\"" + table + "\")")) {
            while (result.next()) {
                String column = result.getString("name");
                if (column == null || !column.matches("[A-Za-z][A-Za-z0-9_]*"))
                    throw new SQLException("Unsupported FCA source column: " + column);
                columns.add(column);
            }
        }
        if (columns.isEmpty()) throw new SQLException("Missing FCA table or columns: " + table);
        return List.copyOf(columns);
    }

    private static void requireColumns(List<String> actual, String table, String... required)
            throws SQLException {
        for (String column : required) if (!actual.contains(column))
            throw new SQLException("Missing column " + column + " in " + table);
    }

    private static String projection(String tableAlias, String prefix, List<String> columns) {
        List<String> parts = new ArrayList<>();
        for (String column : columns) parts.add(tableAlias + ".\"" + column + "\" AS \"" + prefix + column + "\"");
        return String.join(",", parts);
    }

    private static Map<String, String> sourceFields(ResultSet rows, String source,
                                      String alias, List<String> columns) throws SQLException {
        Map<String, String> result = new LinkedHashMap<>();
        for (String column : columns) {
            String value = rows.getString(alias + column);
            String key = source + "." + column;
            if (value == null) result.put("__SQL_NULL__." + key, "true");
            else result.put(key, value);
        }
        return result;
    }
}
~~~~

### `src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopFcaJdbcSmokeTest.java`

SHA-256: `2c8d46ac455c75ac8dae90bb637081c7039933317f9ffca863f57fac34849d14`

~~~~java
package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.topflop.TopFlopFcaJdbcReader.SeasonLoad;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.PlayerSeason;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.RealDay;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.VoteState;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Read-only integration checks on the actual RecordsNext 3.2 SQLite schema. */
public final class TopFlopFcaJdbcSmokeTest {
    private TopFlopFcaJdbcSmokeTest() {}

    public static void main(String[] args) throws Exception {
        if (args.length < 1 || args.length > 2)
            throw new IllegalArgumentException("Usage: <recordsnext.db> [--quick]");
        boolean quick = args.length == 2 && "--quick".equals(args[1]);
        if (args.length == 2 && !quick) throw new IllegalArgumentException("Unknown option: " + args[1]);
        Path file = Path.of(args[0]).toAbsolutePath().normalize();
        String url = "jdbc:sqlite:" + file.toUri() + "?mode=ro";
        Class.forName("org.sqlite.JDBC");
        try (Connection c = DriverManager.getConnection(url)) {
            List<String> seasons = new ArrayList<>();
            try (Statement s = c.createStatement(); ResultSet r = s.executeQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' " +
                    "AND name LIKE 'raw_%_fca_giocain' ORDER BY name")) {
                while (r.next()) {
                    String name = r.getString(1);
                    String season = name.substring(4, 13);
                    if (name.equals("raw_" + season + "_fca_giocain") && season.matches("[0-9]{4}_[0-9]{4}"))
                        seasons.add(season);
                }
            }
            check(seasons.size() == 21, "Expected 21 imported FCA seasons, got " + seasons.size());
            int total = 0, valid = 0, zero = 0, players = 0;
            int independentlyCounted = 0, independentlyCountedPlayers = 0;
            int seasonsFullyRead = 0;
            for (String season : seasons) {
                if (quick && !(season.equals("2006_2007") || season.equals("2025_2026") ||
                               season.equals("2026_2027"))) continue;
                seasonsFullyRead++;
                TopFlopArchive archive = new TopFlopArchive();
                SeasonLoad result = TopFlopFcaJdbcReader.loadSeason(c, archive, season, null);
                check(result.realDaysAdded() == archive.realDayCount(), "Wrong real day count " + season);
                check(result.sourceGiocaInRows() == result.realDaysAdded(), "Loss of rows " + season);
                check(result.validVotes() + result.nonValidVotes() == result.realDaysAdded(), "Vote status mismatch " + season);
                check(result.zeroOrUnclassifiedVotes() <= result.nonValidVotes(), "Invalid unknown count " + season);
                if (season.equals("2006_2007")) {
                    check(result.realDaysAdded() == 24966, "Wrong 2006 day count");
                    check(result.playersByLocalId().size() == 657, "Wrong 2006 player count");
                }
                if (season.equals("2025_2026")) {
                    check(result.realDaysAdded() == 29754, "Wrong 2025 day count");
                    check(result.playersByLocalId().size() == 783, "Wrong 2025 player count");
                    PlayerSeason lautaro = result.playersByLocalId().values().stream()
                        .map(TopFlopFcaJdbcReader.PlayerProfile::player)
                        .filter(p -> Integer.valueOf(100843).equals(p.gazzaCode()))
                        .findFirst().orElseThrow();
                    List<RealDay> days = archive.realDaysFor(lautaro);
                    check(days.size() == 38, "Wrong Lautaro day count");
                    check(days.stream().filter(d -> d.voteState() == VoteState.VALID).count() == 30,
                        "Wrong Lautaro valid votes");
                    check(days.get(0).sourceFields().containsKey("GiocaIn.IDPUNTEGGIO"), "Missing GiocaIn provenance");
                    check(days.get(0).sourceFields().containsKey("Punteggio.FINDER1"), "Missing source flags");
                    check(days.get(0).sourceFields().containsKey("GiocatoreA.CODICEGAZZA"), "Missing player code");
                }
                try (Statement independent = c.createStatement(); ResultSet source = independent.executeQuery(
                        "SELECT count(*) FROM raw_" + season + "_fca_giocain")) {
                    independentlyCounted += source.getInt(1);
                }
                try (Statement independent = c.createStatement(); ResultSet source = independent.executeQuery(
                        "SELECT count(*) FROM raw_" + season + "_fca_giocatorea")) {
                    independentlyCountedPlayers += source.getInt(1);
                }
                total += result.realDaysAdded(); valid += result.validVotes();
                zero += result.zeroOrUnclassifiedVotes(); players += result.playersByLocalId().size();
                System.out.println("STAGIONE " + season + " GIOCATORI " + result.playersByLocalId().size() +
                        " GIORNATE " + result.realDaysAdded() + " VOTI " + result.validVotes());
            }
            check(total == independentlyCounted, "Wrong overall GiocaIn count: " + total);
            check(players == independentlyCountedPlayers, "Wrong overall players count: " + players);
            int lautaroSeasons = 0, collisionSeasons = 0;
            for (String season : seasons) {
                SeasonLoad one = TopFlopFcaJdbcReader.loadSeason(c, new TopFlopArchive(), season, 100843);
                if (!one.playersByLocalId().isEmpty()) {
                    lautaroSeasons++;
                    check(one.playersByLocalId().size() == 1, "Lautaro duplicate in " + season);
                }
                SeasonLoad other = TopFlopFcaJdbcReader.loadSeason(c, new TopFlopArchive(), season, 12302);
                if (!other.playersByLocalId().isEmpty()) collisionSeasons++;
            }
            check(lautaroSeasons == 9, "Wrong Lautaro season count " + lautaroSeasons);
            check(collisionSeasons == 3, "Wrong collision code count " + collisionSeasons);
            boolean rejected = false;
            try { TopFlopFcaJdbcReader.loadSeason(c, new TopFlopArchive(), "2025_2026;DROP TABLE X", null); }
            catch (IllegalArgumentException ok) { rejected = true; }
            check(rejected, "Season SQL injection was accepted");
            System.out.println("TOP_FLOP_FCA_JDBC_OK seasons=" + seasons.size() +
                " readSeasons=" + seasonsFullyRead + " players=" + players + " days=" + total + " validVotes=" + valid +
                " zeroOrUnknown=" + zero + " lautaroSeasons=" + lautaroSeasons);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
~~~~

### `docs/SPECIFICA_LETTORE_JAVA_FCA_TOP_FLOP_v1.md`

SHA-256: `1a2897801db9ceeffa03ce1dee144e71cf66c36afda7140d0a30d7a6bd2e8614`

~~~~markdown
# RecordsNext 4.0 - Top & Flop - Lettore FCA JDBC v1

Data: 2026-11-01. **Modulo nativo Java, additivo, sola lettura**. Connesso ai dati raw SQLite gia importati dal motore 3.2; non sostituisce l'importatore, l'exporter, la pipeline o la GUI della 3.2.

## Cosa legge

`TopFlopFcaJdbcReader.loadSeason(Connection, TopFlopArchive, seasonId, gazzaCode)` utilizza `raw_<stagione>_fca_giocatorea`, `raw_<stagione>_fca_giocain` e `raw_<stagione>_fca_punteggio`. La connessione JDBC e ricevuta dal chiamante; il test integrativo apre gli archivi SQLite con `mode=ro`.

- **Anagrafiche**: `SeasonLoad.playersByLocalId` comprende anche i calciatori senza una giornata `GiocaIn`. `PlayerProfile.originalFields` conserva tutti i campi originali di `GiocatoreA`.
- **Prestazioni giornaliere**: ogni `(stagione, GiocatoreA.ID, GiocaIn.GIORNATA)` genera **un solo** `RealDay` nell'archivio Top & Flop. Il collegamento `GiocaIn.IDPUNTEGGIO -> Punteggio.ID` replica la logica del modello 3.2.
- `GiocatoreA.ID` resta **locale alla stagione**. `CodiceGazza` rimane il codice esterno; valori 0/non positivi non sono trattati come identita storica valida. Collisioni storiche non risolte automaticamente.
- `RealDay.sourceFields` salva i valori non nulli di tutti i campi delle tre tabelle con prefissi `GiocatoreA.`, `GiocaIn.`, `Punteggio.`; i SQL NULL sono rappresentati da un campo tecnico `__SQL_NULL__.<origine>.<colonna>=true`. Questo evita di equiparare valore assente e stringa vuota. `RealDay.events` conserva le colonne evento numeriche di Punteggio senza applicare coefficienti.
- `Punteggio.VOTO1 > 0` -> `VALID`, con voto originale. `VOTO1 < 0` e nessun valore speciale -> `NO_VOTE`; voto 0/NULL -> `UNKNOWN` (non e prova di mancata presenza); `VALORESPECIALE != 0` senza voto positivo -> `SPECIAL`. Non e lecito dedurre automaticamente `NOT_PLAYED` dalla sola riga `GiocaIn`.
- `VOTO2`, `VOTO3`, `VALORESPECIALE` e i flag `FINDER` sono conservati nei campi fonte, **non** scambiati per eventi o per voto Gazzetta. Restano disponibili per le analisi successive.
- Nessun bonus/malus viene calcolato: i coefficienti vanno dalla competizione/anno quando leggeremo FCM.

## Integrita e limiti

- Il nome della stagione e validato come `AAAA_AAAA`, le colonne sono lette dal catalogo SQLite ed i nomi non validi vengono rifiutati.
- In modalita non filtrata, il numero di righe `GiocaIn` deve coincidere con le righe collegate al giocatore e con quelle collegate a `Punteggio`. Le chiavi duplicate incoerenti generano un errore, non una sovrascrittura.
- `loadSeason` e pensato per **una stagione alla volta**: non mantenere tutte le 21 stagioni simultaneamente in RAM.
- Lettore ancora limitato a `GiocatoreA`, `GiocaIn`, `Punteggio`. Non importa ancora `Statistiche`, `Storico`, `IncontroInA`, FCM, rose temporali o tesseramenti. Questi rimangono passaggi successivi. `RealDay` non e da solo una presenza reale certificata.
- Nessun `TOP_FLOP` attivato in `RecordFamily`, GUI, exporter, JS, manifest o siti. Nessun record pubblicato.

## Risultati verificati su due DB diversi, senza modificarli

Archivio ZIP 3.2 fornito dall'utente, dati `recordsnext.db` e `recordsnext-sandbox.db`:

| Fonte | Stagioni FCA | Anagrafiche stagionali | GiocaIn | VOTO1 validi |
|---|---:|---:|---:|---:|
| recordsnext.db | 21 | 16.245 | 617.310 | 202.145 |
| recordsnext-sandbox.db | 21 | 16.211 | 616.018 | 201.851 |

La differenza e **interamente nel 2026/27**, con 34 anagrafiche e 1.292 giornate in piu nel database principale. Non indichiamo un DB come errato: si tratta di due snapshot distinti. I test non codificano mai un totale globale fisso.

Caso 2025/26, Lautaro (`CodiceGazza=100843`): 38 giornate sorgente, 30 con `VOTO1` valido. Presente con questo codice in nove stagioni. Codice 12302 presente in tre stagioni, con identita storiche da verificare.

## Collegamento futuro, non attivo

I `RealDay` alimenteranno le schede e Top & Flop, ma il collegamento ai contesti FCM verra creato **solo tramite logiche gia verificabili della 3.2** e conservando scorecardUrl canonica, competizione, fantasquadra e matchId. Youth League esclusa da output pubblici; Play Off - Play Out e Spareggio Serie B = CAMPIONATO. Le classifiche sui voti inutilizzati richiederanno uno stato di esclusione comprovato, non la semplice assenza da `Formazione`.
~~~~

### `docs/VERIFICA_LETTORE_JAVA_FCA_TOP_FLOP_v1.md`

SHA-256: `135c64b49fd05db4c6b734a74f43ae18f9a1f99ee877ebcae4aa8d82e6a53d70`

~~~~markdown
# RecordsNext 4.0 - Verifica lettore FCA JDBC v1

Verifica locale 2026-11-01 contro **due distinti SQLite originali** provenienti da `RecordsNext2.0.zip`, Java 21, JDBC SQLite incluso nel JAR originale di RecordsNext 3.2.

- Compilazione del modulo `TopFlopFcaJdbcReader` insieme alle quattro classi TopFlop gia presenti: superata.
- Test Java di lettura completa, stagione per stagione, sui 21 FCA del DB principale e del sandbox: superati.
- Inventario indipendente `SELECT count(*)` sulle tabelle raw per ciascuna stagione: coincidente con il numero di fatti letti; no duplicati incompatibili osservati.
- Anagrafiche anche senza righe giornata: conservate in `SeasonLoad.playersByLocalId`.
- Lautaro 2025/26: 38 giornate, 30 voti validi, 9 stagioni per codice storico. Codice 12302: tre stagioni, senza risoluzione identita automatica.
- Valori zero, -1 e speciali preservati senza interpretarli come voto valido o gol/bonus impliciti.
- Rifiuto di stagione non valida verificato.
- Nessuna modifica al database nelle prove; nella verifica Windows il runner ricalcolera SHA-256 prima/dopo per entrambi gli archivi.

**Limite**: la validazione di questo modulo riguarda FCA; i test Java/JUnit completi e le prove d'integrazione pipeline 3.2 non sono ancora effettuati. Le classifiche e l'export Top & Flop non esistono ancora.
~~~~

### `docs/PIANO_RECORDSNEXT4_GIOCATORI_v17.md`

SHA-256: `045902e3ac7876c893521a266e6a70f71d8e5d6cd5a9e942cc9916ba9fedb81a`

~~~~markdown
# RecordsNext 4.0 - Piano v17: collegamento FCA JDBC al modello Top & Flop

Data 2026-11-01. Prima connessione JAVA di sola lettura ai raw FCA importati dalla 3.2; 21 stagioni verificate sul DB principale e sul sandbox, che differiscono nel 2026/27. Non attivare la famiglia ne modificare la pipeline 3.2.

**Fatto in questa fase**: modello `TopFlopFcaJdbcReader` con profilo giocatore, anagrafica, voti e eventi originali, un fatto per giornata FCA; test Java su due DB e contatori auto-calibrati. **Da fare**: collegamento FCM (partita, fantasquadra, competizione, scorecard canonica, uso titolare/panchinaro/escluso), validazione piena degli eventi, tesseramento temporale, storico e statistiche aggregate, catalogo/exporter/GUI/JS.

**Regola di sicurezza**: gli stessi test possono trovare 616018 giornate nel sandbox e 617310 nel principale; e' una differenza di snapshot, non motivo per riscrivere i database.


---

# RecordsNext 4.0 - Piano v16: implementazione Java Records Top & Flop

**Decisioni precedenti preservate.** Nuova tappa: modello canonico Java `app/topflop` con real day, roster snapshot, fantasy match, politiche di competizione e indicatori base. Test Java 21 e bibbia v15. Non attivare la famiglia `TOP_FLOP` finche pipeline/JS/GUI/manifest/shard non funzionano.

**Prossima fase:** adattatore Java di sola lettura ai raw FCA/FCM per l'archivio individuale completo (compresi voti, fantavoti, eventi e prove di utilizzo), con confronto sui 21 archivi; solo dopo: motore/archiviazione/catalogo/exporter/publication 3.2. Rimangono aperte la semantica delle esclusioni non in `Formazione`, il fantavoto dell'escluso, i codici storici riutilizzati, soglie e classifica definitiva.

---

# RecordsNext 4.0 - Piano operativo v15

# RecordsNext 4.0 - Piano operativo v14

# RecordsNext 4.0 - Continuita documentale PIANO_RECORDSNEXT4_GIOCATORI_v13

Versione additiva del 2026-11-01. La cronologia integrale precedente e preservata sotto.

# RecordsNext 4.0 - Piano operativo v11

Data: 2026-11-01. Base produttiva 3.2.0 immutata. Branch `feature/recordsnext4-players`; commit di riferimento `f9bf30323d9204d70e91e242bddfc72c2060ecf9`.

## Fasi completate e evidenze

- Fase 0: identita e fonti; eccezioni sui codici storici ancora aperte.
- Fase 1: estrattore FCA sperimentale; output Windows verificato, 7 test. Non integrato nel Java.
- Fase 2: estrattore FCM fantasy sperimentale; output Windows verificato, 8 test. Non integrato nel Java.
- Fase 3A: audit stati `Formazione`, output Windows verificato, 9 test.
- Fase 3B1: audit `Tabellino.LISTA` + `Formazione`, output Windows `RecordsNext4_Fase3B1_OUTPUT_v1_20261101_001744_607896.zip` verificato: 7 test, 21 stagioni, 22.265 tabellini, 552.025 righe `Formazione`, tre SHA-256 dei report corretti, sandbox invariato, bibbia v8 generata. Non equivale a prova definitiva di voto conteggiato.
- Fase 3B2: censimento osservazionale delle regole `Competizione` per stagione e competizione, legame `Incontro.IDGIRONE -> Girone.IDCOMPETIZIONE`, moduli ammessi e tabellini osservati. Strumento prototipo con 8 test locali; lo ZIP Windows deve ancora certificarne l'esecuzione sul PC.

## Passaggi successivi

- Fase 3B3: confrontare configurazioni di competizione, tabellini reali, RU, sostituzioni, eventuali voti speciali e regole per ruolo. Stabilire cosa significa realmente `conteggiato`, `disponibile`, `non sfruttato`, `recuperabile`; distinguere prove osservate, inferenze e casi non simulabili.
- Fase 3C: solo dopo approvazione della semantica: prototipo per metriche individuali fantasy (panchina non impiegata, fuori formazione, sostituzioni, eventuali controfattuali), test su casi reali verificati.
- Fase 4: integrare fonti canoniche in SQLite/Java, esportazione JS stagionale/sito, HTML e GUI. Tutte le componenti richieste fanno parte del traguardo 4.0.0.
- Fase 5: regressioni, documentazione, commit, release 4.0.0.

## Vincoli costanti

1. Prestazione FCA unica per giocatore storico, stagione, giornata: non duplicare se usata in competizioni fantasy diverse.
2. `GiocatoreA.ID` e annuale; `CodiceGazza` contiene il codice storico nella FCA. Le collisioni storiche non sono risolte automaticamente.
3. I codici speciali 0/-1 in `Tabellino.LISTA` non sono calciatori.
4. `ENTRATO`, `ENTRATORISERVA` e posizione nei primi undici slot non bastano, isolatamente, a certificare un voto conteggiato.
5. Il regolamento va ricavato per stagione e competizione, senza assumere tre sostituzioni ovunque. Un archivio FCM e uno snapshot delle impostazioni, non prova di eventuali variazioni avvenute durante la stagione.
6. Conservare integralmente le analisi RU personali della 3.2; non inserirle nei record pubblici senza decisione espressa.
7. Tabellini non collegati, incontri con squadra 0 e riposi: casistiche distinte, nessuna cancellazione.
8. Nessuna scrittura nel DB operativo/sandbox, nessuna modifica a Java, pipeline, siti, GUI, manifest o branch principale durante gli audit.
9. I file nuovi vanno in `D:\DEV_APPS\RecordsNext4.0`, i risultati in `D:\DEV_APPS\downloads`, con nomi versionati.
10. Ogni fase mantiene piano, registro decisioni, diario, specifiche, test e bibbia incrementale con codice integrale e SHA-256. Le versioni pregresse restano intatte.


## Aggiornamento dopo verifica Windows 3B2 e decisione sul record individuale

- 3B2 completata anche su Windows (`ESITO: OK`, 8/8 test): audit documentale delle regole. Non sono state apportate modifiche al database o al motore Java.
- Priorita della fase successiva: **voti non utilizzati dal singolo calciatore**. Niente simulazione del punteggio di fantasquadra e niente ottimizzazione delle sostituzioni.
- Dati minimi da raccogliere per un calciatore: codice storico, ID locale FCA, stagione, fantasquadra, partita e competizione fantasy, giornata reale, indicatore di effettivo utilizzo nei primi 11 del tabellino, presenza di voto reale valido, valore sorgente del voto, riferimento al tabellino.
- Metrica stabilita: somma dei soli voti reali validi nelle esclusioni / **tutte le esclusioni**; conservare separati i due conteggi, non dividerli fra loro.
- Implementazione successiva: un campione reale verificato a mano, poi test automatici sul campione, quindi calcolo storico. Registrare i casi ambigui anziche correggerli arbitrariamente.
- Il campo "valore massimo" menzionato dall'utente rimane **aperto** (nessun cap numerico assunto).
- Continuano a valere l'identita per `CodiceGazza` con verifica collisioni, l'ID FCA locale, l'assenza di duplicazioni delle prestazioni reali e la conservazione dei record precedenti.


## Nuovo assetto approvato per il record individuale (2026-11-01)

- Conservare i quattro indicatori con due numeratori distinti (voto reale valido, fantavoto regolamentare completo) e due denominatori distinti (esclusioni totali, esclusioni con voto valido).
- La Fase 3C si articola in: (a) campioni di esclusioni FCM/FCA con provenienza voto/fantavoto e bonus-malus; (b) audit di tesseramento e partecipazione effettiva per stabilire `E` e `P`; (c) calcolo su casi reali e confronto con tabellino, senza simulare cambi di formazione.
- La stessa prestazione FCA resta un solo fatto reale; un'esclusione e invece un fatto fantasy legato a stagione, fantasquadra, competizione e incontro. Non aggregare ciecamente incontri di competizioni parallele.
- Implementare prima unit test sintetici per i quattro indicatori (v2); i test sintetici NON dimostrano ancora la classificazione delle esclusioni negli archivi.
- Se `E > 0, P = 0`, gli indici pesati valgono zero; le medie sulle gare valide sono non definite. Se `E = 0`, tutte le metriche sono non definite.
- Le RU non sono calciatori e non devono generare un voto individuale sprecato; le regole di sostituzione per competizione restano variabili e indipendenti da questo calcolo.
- Nessun limite artificiale al voto o fantavoto; la voce 'massimo' resta proposta separata e non adottata come soglia.


## Nuova fase 3C0 - Modello archivio completo e audit contesti paralleli

- Definita specifica `SPECIFICA_ARCHIVIO_COMPLETO_GIOCATORE_v1.md` con livello reale, tesseramento e livello evento fantasy, valido anche per titolari.
- Documentate due famiglie, competizioni singole e sovrapposizioni nella stessa giornata; conservazione dei dati originali prima dei filtri.
- Audit `Audit-RecordsNext4Contesti-v1.py` READ-ONLY: report per competizione, copertura, giocatore-squadra-competizione, sovrapposizioni e caso 100843.
- **Prossimo sviluppo**: storicizzare il tesseramento e definire le assenze da Formazione in modo dimostrabile; quindi costruire il dataset sorgente completo e l'estrazione simmetrica voti/fantavoti per utilizzati ed esclusi.
- La verifica Windows del nuovo runner rimane pendente; non toccare Java/GUI/sito.

## Aggiornamento v14 - fase 3C1

- Validazione Windows della 3C0: ZIP `RecordsNext4_Contesti_OUTPUT_v1_20261101_004840_254541.zip`, stato OK, 10 test, 21 stagioni, 552025 righe, SHA256 sandbox uguale prima/dopo, bibbia v12 presente. Non ancora dataset canonico.
- Classificare Play Off - Play Out e Spareggio Serie B CAMPIONATO; escludere Youth League da ogni output record/scheda.
- Audit v2: verificare nuovamente le 21 stagioni, separare originali/ammessi/esclusi, non eliminare dati sorgente.
- Prossima implementazione: associare a ogni evento fantasy la partita FCM e la `scorecardUrl` canonica della stagione corretta, verificata sui file normalizzati 3.2. URL non verificati restano null.
- Solo successivamente generare il dataset completo dei giocatori in Java/SQLite e i record con prova del singolo episodio.


## Aggiornamento v15 - Records Top & Flop come famiglia nativa

**Decisione vincolante dell'utente (2026-11-01):** tutti gli audit dei giocatori delle fasi 0-3C1 sono lavori preparatori di una nuova famiglia nativa **Records Top & Flop**, integrata in RecordsNext 4.0. La pipeline 3.2.0 resta la base esclusiva da estendere. Non costruire sistemi di export, pubblicazione, configurazione, manifest o visualizzazione paralleli.

**Stato attuale:** 3C1 eseguita su Windows, ZIP `RecordsNext4_Contesti_OUTPUT_v2_20261101_005413_578565.zip` verificato: stato OK; 13 test; 21 stagioni; 552025 righe origine, 546775 ammesse, 5250 Youth League escluse; SHA256 database sandbox prima=dopo; bibbia v13 generata. Non e ancora implementata una famiglia Java Top & Flop.

**Ordine vincolato di implementazione**, rispettando la 3.2:
1. Definire fatti canonici: giocatore storico + prestazione FCA unica e contesti per fantasquadra, incontro, competizione; provenienza del voto e dei bonus/malus; link canonico tabellino. Produrre test sui casi 100843, competizioni parallele, Youth League e anomalie.
2. Implementare una sola volta in Java/SQLite i fatti riutilizzabili da scheda giocatore e records; mantenere gli audit Python come verificatori, non produttori operativi permanenti.
3. Inserire `TOP_FLOP` nel catalogo (`RecordFamily`, `CoreRecordCatalog`, dipendenze, selezione e configurazione), senza modificare valori preesistenti e mantenendo la lettura delle configurazioni 3.2.
4. Collegare il calcolo alla `RecordsNextPipeline` con logiche FULL/CONSOLIDATED e archivio storico separato per stagione; eseguire regressione su output invariati delle altre famiglie.
5. Collegare `TopFlopFamilyJsExporter` a `Records2026SitePublisher`, relativa validazione, manifest, `SeasonFamilyShardPublisher`, publisher multisito e link tabellini canonici. Nessun nuovo flusso di deploy.
6. GUI: aggiungere selezione famiglia e singoli records in `RecordsNext2Dashboard` / configurazione vigente. HTML sito: nuova sezione e schede individuali derivate dagli stessi dati senza duplicazione dei calcoli.
7. Eseguire test integrati: nessun impatto su famiglie preesistenti, non oltre 1.5 MiB per shard, siti storici corretti, FULL e CONSOLIDATED, JS valido, nessuna Youth League. Aggiornare bibbia e rilasciare come 4.0.0 soltanto dopo approvazione.

La classificazione CAMPIONATO/COPPE e distinta dalla classificazione **famiglie di record**: `TOP_FLOP` e una famiglia records; CAMPIONATO/COPPE sono filtri del dominio sportivo. Non confonderle nel codice.

**Prossimo passo di implementazione:** realizzazione nel Java 4.0 del modello dati canonico individuale e test di regressione, senza ancora selezionare/pubblicare la famiglia finche il suo calcolo non esiste.
~~~~

### `docs/REGISTRO_DECISIONI_RECORDSNEXT4_v16.md`

SHA-256: `f0675433ac7d6740ca0dce0ee0bacc087318cfb02e270d2e5c1c9b80468a90e0`

~~~~markdown
# RecordsNext 4.0 - Registro decisioni v16

| ID | Stato | Decisione / evidenza |
|---|---|---|
| D-124 | IMPLEMENTATA E VERIFICATA | L'adattatore FCA Top & Flop usa JDBC SQLite e le **stesse tabelle raw importate dalla 3.2**, senza copie o nuove pipeline. |
| D-125 | VERIFICATA SU FONTI | `recordsnext.db`: 617310 GiocaIn e 16245 GiocatoreA stagionali; `recordsnext-sandbox.db`: 616018 e 16211. Differenza tutta nel 2026/27: usare test di consistenza dinamica. |
| D-126 | REGOLA TECNICA | Conservare nei fatti la distinzione `GiocatoreA.ID` locale vs `CodiceGazza` storico, senza attribuire identita canoniche nei casi dubbi. |
| D-127 | REGOLA TECNICA | Un record `GiocaIn` non prova da solo una presenza reale: interpretare `VOTO1>0` come voto valido, valori negativi come senza voto e zero/NULL come sconosciuti; preservare i valori originali. |
| D-128 | IMPLEMENTATA | Conservare integralmente campi GiocatoreA/GiocaIn/Punteggio nel campo diagnostico `sourceFields`, con marcatore esplicito per SQL NULL; nessuna interpretazione dei coefficienti bonus/malus prima della fonte FCM. |
| D-129 | APERTA | Completare la lettura di Statistiche/Storico/IncontroInA e gli eventi FCM; definire il modello temporale dei tesseramenti e la validita degli esclusi. |
| D-130 | REGOLA DI PROTEZIONE | La 3.2 operativa, il DB, la GUI, la selezione catalogo, i publisher e i siti non si modificano in questa fase. |


---

# RecordsNext 4.0 - Registro decisioni v15

Le decisioni D-001--D-112 sono preservate integralmente dalla v14. Nuove D-113--D-123:

| ID | Stato | Decisione / evidenza |
|---|---|---|
| D-113 | IMPLEMENTATA: MODELLO | Aggiungere la famiglia `app/topflop` al codice Java esistente, senza modificare le classi 3.2 ne attivare prematuramente `RecordFamily.TOP_FLOP`. |
| D-114 | APPROVATA/IMPLEMENTATA | Prestazione reale unica per stagione+ID FCA+giornata Serie A. Eventi fantasy separati per stagione+competizione FCM+incontro+fantasquadra+ID FCA. |
| D-115 | APPROVATA/IMPLEMENTATA | Conservare `CodiceGazza` storico e ID FCA stagionale distinti; non fondere automaticamente collisioni storiche. |
| D-116 | APPROVATA/IMPLEMENTATA | `Tesserato` e una fotografia della rosa, non dimostra tesseramento in una giornata non documentata. Nessuna esclusione inferita dal silenzio FCM. |
| D-117 | APPROVATA/IMPLEMENTATA | Youth League esclusa categoricamente; Play Off - Play Out e Spareggio Serie B = CAMPIONATO; 24 nomi ammessi codificati esplicitamente, altri nomi in revisione obbligatoria. |
| D-118 | APPROVATA/IMPLEMENTATA | Dati per titolari, conteggiati, panchinari e fuori lista; stato `EXCLUDED_CONFIRMED` utilizzabile solo dopo prova sorgente. |
| D-119 | APPROVATA/IMPLEMENTATA | Quattro metriche personali calcolate da voti/fantavoti integrali, `SV/P`, `SV/E`, `SF/P`, `SF/E`. Sulle fonti incomplete mantenere separati contatori voto/fantavoto, non inventare valori. |
| D-120 | APPROVATA/IMPLEMENTATA | Non sintetizzare mai URL tabellini: copiare dal normalizzato/link canonico 3.2; conservare ID incontro e giornata distinti. |
| D-121 | IMPLEMENTATA: TEST | Test Java standard con 52 assert e wrapper JUnit 5; compilazione completa codice Java 3.2+nuovo modulo verificata localmente con JDK 21, non suite Maven completa. |
| D-122 | REGOLA OPERATIVA | Il runner deve verificare JDK 21, git branch, file preesistenti e collisioni prima dell'installazione; output diagnostico sotto `D:\DEV_APPS\downloads`, mai `%TEMP%`; niente sovrascritture. |
| D-123 | APERTA | Realizzazione estrattore JDBC, identificazione esclusioni provate, ambito tesseramento, classifiche e soglie, integrazione piena RecordFamily/pipeline/exporter/GUI. Non segnare come implementati in v15. |

---

# RecordsNext 4.0 - Registro decisioni v14

# RecordsNext 4.0 - Registro decisioni v13

# RecordsNext 4.0 - Continuita documentale REGISTRO_DECISIONI_RECORDSNEXT4_v12

Versione additiva del 2026-11-01. La cronologia integrale precedente e preservata sotto.

# RecordsNext 4.0 - Registro decisioni v10

Data: 2026-11-01. Registro additivo: D-001--D-053 restano documentate nelle versioni v2--v8; i relativi stati non cambiano tacitamente.

| ID | Stato | Decisione / evidenza |
|---|---|---|
| D-054 | CONFERMATA: verifica Windows | ZIP 3B1 `RecordsNext4_Fase3B1_OUTPUT_v1_20261101_001744_607896.zip`: stato OK, sette test superati, 552.025 righe `Formazione`, 22.265 tabellini, tre SHA-256 dati verificati, SHA sandbox invariato. Bibbia v8 generata; il runner dichiara nessun commit/deploy. |
| D-055 | CONFERMATA: osservazione FCM | La catena per individuare il regolamento e `Incontro.IDGIRONE -> Girone.ID -> Girone.IDCOMPETIZIONE -> Competizione.ID` **della stessa stagione**. `Competizione.ID` non costituisce da solo identita storica. |
| D-056 | CONFERMATA: fonte | `Competizione` registra anche `NUMEROSOSTITUZIONI`, `NUMERORISUFFICIO`, voti d'ufficio, limiti panchina, ruoli sostituibili, regole speciali; `ModuloAmmesso` collega i moduli alla competizione. Nessun valore e convertito in punteggio recuperato. |
| D-057 | CONFERMATA: osservazione locale 3B2 | Sono presenti 180 righe `Competizione` su 21 stagioni. Nel 2019/20 tutte le 12 riportano `NUMEROSOSTITUZIONI=5` e `NUMERORISUFFICIO=3`; nel 2007/08 le tre riportano 3, 8, 11 sostituzioni e 0, 99, 99 RU. Valori non normalizzati arbitrariamente. |
| D-058 | CONFERMATA: separazione | Nel campione SQLite ci sono 168 tabellini senza incontro, 23 collegati a incontri con una squadra zero: non dichiararli automaticamente anomalie o riposi. I controlli si svolgono separatamente. |
| D-059 | APERTA | Interpretazione delle configurazioni elevate (8, 11, RU 99), possibili convenzioni per assenza di limite, rapporto con tabellini e modifiche al regolamento durante la stagione: da verificare sul modello FCM e su esempi reali. |
| D-060 | APERTA | Definizione rigorosa di prestazione conteggiata, prestazione disponibile in panchina e punti effettivamente recuperabili, per competizione, stagione e regole del ruolo. Niente calcoli prima dell'approvazione. |
| D-061 | IMPLEMENTATA: prototipo di sola lettura | Audit 3B2 con dati raw completi delle configurazioni, aggregati per competizione, moduli ammessi e verifiche integrita; 8 test locali superati. Certificazione Windows ancora da eseguire. |

Nota: non sono state prese nuove decisioni sui pesi dei record o sulle formule per i punti non sfruttati.


## Integrazione successiva a 3B2: metrica individuale (2026-11-01)

| ID | Stato | Decisione / motivazione |
|---|---|---|
| D-062 | CONFERMATA: verifica Windows 3B2 | ZIP `RecordsNext4_Fase3B2_OUTPUT_v1_20261101_002713_328984.zip`, stato OK; 8 test superati, 21 stagioni, 180 configurazioni, 13.072 incontri e 22.265 tabellini. SHA-256 del sandbox prima e dopo identico: `57923f4e75986c2b81533e4beef15b9133635258d7de1d36827a1bdbe576671b`. Bibbia v9 generata; nessun commit/deploy dichiarato. |
| D-063 | APPROVATA DALL'UTENTE: obiettivo | Il voto sprecato e un indicatore riferito a **uno specifico calciatore** lasciato fuori da una fantasquadra, non il vantaggio teorico che la fantasquadra avrebbe potuto ottenere sostituendo altri giocatori. Nessuna simulazione delle sostituzioni necessaria per questa metrica. |
| D-064 | APPROVATA DALL'UTENTE: formula | `mediaVotoSprecato = somma(voti validi del calciatore nelle occasioni in cui e rimasto fuori) / numero totale di partite in cui e stato lasciato fuori`. Le esclusioni senza prestazione utile contano nel denominatore e zero nel numeratore. Esempio: 23 esclusioni, 3 voti pari a 6: `18/23 = 0,782608...`. Non usare come media primaria `18/3 = 6`. |
| D-065 | CONFERMATA: RU e fonti 3.2 | La Riserva d'Ufficio e un voto regolamentare assegnato per un posto scoperto quando le sostituzioni ordinarie non bastano. Non e un calciatore ne un voto del calciatore lasciato fuori; non entra nella somma dei suoi voti sprecati. Preservare il sistema RU e gli strumenti personali esistenti. Fonti: `docs/ANALISI_PERSONALE_RU.md`, `docs/MODELLO_DATI_RECORDSNEXT2.md` e `tools/Report_RU_Storico_Completo_v8_4.ps1` (glossario). |
| D-066 | APPROVATA DALL'UTENTE: sostituzioni | Non esiste una regola generale di tre sostituzioni: si osservano anche 5 e, per recuperi a distanza di mesi e configurazioni eccezionali, anche 11. Mantenere il valore FCM della competizione senza normalizzarlo. Per la media individuale non simulare sostituzioni alternative. |
| D-067 | DA VERIFICARE PRIMA DEL CALCOLO | Stabilire con esempi reali il significato operativo di "lasciato fuori" (non nei primi 11 del tabellino finale, riserva non utilizzata, non schierato), il tipo di voto da sommare (voto Gazzetta base vs fantavoto totale), il trattamento di SV e voti speciali e il perimetro delle sole partite valide. Non inventare questi criteri. |
| D-068 | DA VERIFICARE PRIMA DEI RANKING | Evitare conteggi erronei di una stessa prestazione reale se ci sono piu competizioni fantasy nella stessa giornata. Conservare il dettaglio delle partite fantasy ed esplicitare la chiave di aggregazione del record personale. |
| D-069 | APERTA | L'utente ha menzionato un "valore massimo" senza definirne numericamente l'eventuale soglia o se si riferisca al massimo voto sprecato. Non fissare cap o limiti arbitrari. |
| D-070 | PROCEDURA | Comandi pronti per copia-incolla PowerShell 5.1, senza blocchi `else` separabili; file sempre versionati; ogni fase aggiorna piano, registro, diario, codice verificato e bibbia con SHA-256. |

### Nota di compatibilita

Le D-060 e D-061 riguardano anche un **possibile** studio dei punti recuperabili della fantasquadra. Per il nuovo record individuale quell'analisi non costituisce un prerequisito: questa decisione ne restringe il perimetro, senza cancellare la cronologia.


## Revisione metriche individuali (2026-11-01) - decisioni D-071--D-079

| ID | Stato | Decisione / motivazione |
|---|---|---|
| D-071 | APPROVATA DALL'UTENTE | Il voto di un calciatore non utilizzato e il **valore integrale del voto** (es. 7 resta 7), non il voto meno 6 e non il vantaggio potenziale della fantasquadra. |
| D-072 | APPROVATA DALL'UTENTE | Quattro indicatori distinti: media voto sprecato, errore pesato sul voto, fantamedia sprecata, errore pesato sul fantavoto. La formula v1 `S/E` rimane valida come **errore pesato**, ma non come unica misura principale. |
| D-073 | APPROVATA DALL'UTENTE | Con `E` esclusioni, `P` prestazioni valide, `SV` somma voti e `SF` somma fantavoti: `mediaVoto = SV/P`, `pesatoVoto = SV/E`, `mediaFantavoto = SF/P`, `pesatoFantavoto = SF/E`. Dove il denominatore e zero il risultato e non definito. |
| D-074 | APPROVATA DALL'UTENTE | Una gara in cui il calciatore non ha giocato/non ha voto non produce errore: somma zero al numeratore; se e una esclusione effettiva valida, rimane nel denominatore `E` del peso stagionale. |
| D-075 | APPROVATA DALL'UTENTE | Fantavoto comprensivo di eventi e relativi bonus/malus: gol normali, gol su rigore, assist, ammonizioni e altre categorie disponibili. Conservare separatamente voto, fantavoto ed eventi originali. Valori regolamentari da sorgenti reali, mai coefficienti unificati arbitrariamente. |
| D-076 | DERIVAZIONE MATEMATICA | `quotaConVoto=P/E`, `errorePesatoVoto=mediaVoto*quotaConVoto` e `errorePesatoFantavoto=fantamediaSprecata*quotaConVoto`, quando denominatori disponibili. Se `E>0, P=0`: indici pesati pari a zero, medie non definite. |
| D-077 | PROPOSTA, DA APPROVARE | Aggiungere massimo voto/fantavoto in singola esclusione, numero di esclusioni e quota con voto come indicatori di dettaglio, senza applicare alcun cap artificiale. |
| D-078 | APERTA - FCM/FCA | Definire con casi reali l'esclusione (panchinaro non usato, fuori lista, non schierato ma tesserato), la provenienza del fantavoto per giocatore lasciato fuori e la gestione di `SV`, voti d'ufficio e competizioni parallele. Non calcolare record reali prima della verifica. |
| D-079 | PROCEDURA / IMPLEMENTAZIONE | Aggiunto solo un test sintetico e la specifica v2. Nessuna modifica a database, Java, GUI, regolamenti, competizioni o siti. Bibbia incrementale v11 e SHA-256 dei nuovi file; vecchie versioni conservate. |

**Cronologia invariata:** D-064 `S/E` non e annullata: assume il nome corretto di **errore pesato stagionale** e si affianca alla nuova media `S/P` richiesta dall'utente.


## Decisioni D-080--D-090: archivio completo per giocatore e contesti paralleli

| ID | Stato | Decisione / evidenza |
|---|---|---|
| D-080 | APPROVATA DALL'UTENTE | Il prodotto deve conservare un set completo di dati per OGNI calciatore, con scheda individuale e record derivati dallo stesso archivio. Non estrarre soltanto le statistiche utili a record gia previsti. |
| D-081 | APPROVATA DALL'UTENTE | Un calciatore puo essere tesserato nello stesso anno da 2-3 fantasquadre diverse (serie distinte). Separare rigorosamente le osservazioni fantasy per squadra. |
| D-082 | APPROVATA DALL'UTENTE | Lo stesso calciatore nella stessa giornata puo essere usato in campionato ed escluso in coppa, e viceversa. Ogni incontro fantasy e un contesto indipendente; la prestazione reale e unica. |
| D-083 | APPROVATA DALL'UTENTE | Esattamente due famiglie per il filtro richiesto: CAMPIONATO e COPPE; conservare sempre anche la singola competizione e la sua identita stagionale. |
| D-084 | APPROVATA DALL'UTENTE | Dati individuali completi anche per titolari, giocatori conteggiati e subentrati, non soltanto per gli esclusi: voto, fantavoto, dettaglio bonus/malus, presenze, medie e contesti. |
| D-085 | CONFERMATA: FCM 2025/26 | `Tesserato` mostra il calciatore codice 100843 in tre squadre: 8 (River Pino), 49 (F.C. Pitbull), 77 (F.C. Boliverz). In giornata Serie A 4 River Pino lo ha in Serie B e Coppa Serie B (due incontri diversi). |
| D-086 | INFERENZA DA VERIFICARE | `Tesserato` e uno snapshot senza campi data di validita; non considerare automaticamente escluso il calciatore mancante da `Formazione` in ogni incontro di una squadra. |
| D-087 | PROPOSTA OPERATIVA | Mappa esplicita delle denominazioni FCM alle due famiglie, per stagione e competizione, invece di classificazione runtime con parole chiave. Per ora tre nomi senza assegnazione (Play Off - Play Out, Spareggio Serie B, Youth League), da approvare. |
| D-088 | REGOLA DI INTEGRITA | Chiave prestazione reale diversa dalla chiave evento fantasy; non moltiplicare gol/assist reali nelle aggregazioni all-time quando la stessa prestazione alimenta piu eventi fantasy. |
| D-089 | IMPLEMENTATA: AUDIT PROTOTIPO | Nuovo audit Fase 3C0 di sola lettura sui contesti. Verifica 552.025 righe Formazione, 21 stagioni, mappa delle competizioni, sovrapposizioni e caso reale codice 100843. Non crea ancora il dataset completo canonico. |
| D-090 | APERTA | Regole affidabili per esclusioni senza Formazione, coesistenza di piu partite della stessa famiglia nella giornata, disponibilita storica tesseramento, fantavoto del non schierato e trattamento Play Off/Spareggio/Youth League. |

## Decisioni D-091--D-098: classificazione completa e link ai tabellini

| ID | Stato | Decisione e verifica |
|---|---|---|
| D-091 | APPROVATA DALL'UTENTE | `Play Off - Play Out` appartiene alla famiglia CAMPIONATO. |
| D-092 | APPROVATA DALL'UTENTE | `Spareggio Serie B` appartiene alla famiglia CAMPIONATO. |
| D-093 | APPROVATA DALL'UTENTE | `Youth League` non deve essere considerata MAI nei record/statistiche/schede individuali. Nei soli audit fonti conservare l'evidenza della esclusione. |
| D-094 | APPROVATA DALL'UTENTE | I record individuali di singola partita devono contenere stagione, competizione, fantasquadra, ID incontro e link del tabellino di quella partita sul sito della stagione corrispondente. |
| D-095 | VERIFICATA NEL CODICE 3.2 | Esistono `urlTabellino`, `urlTabellinoLocale`, `urlTabellinoOnline`, `scorecardUrl`; la risoluzione della URL e gia implementata nel normalized exporter 3.2. Non fare concatenazioni inventate con la root del sito corrente. |
| D-096 | APERTA - LINK REALI | Prima della pubblicazione, verificare per ciascun sito/anno la rotta reale e la eventuale possibilita di puntare direttamente al singolo incontro. I link delle pagine di giornata non garantiscono un'ancora per il match. |
| D-097 | REGOLA DATI | L'ID incontro FCM e distinto dalla giornata Serie A e dalla competizione. Conservare tutti i campi per il record e per la scheda giocatore, anche in competizioni parallele. |
| D-098 | IMPLEMENTAZIONE AUDIT | L'audit v2 classifica Play Off/Spareggio, esclude Youth League dalle osservazioni utilizzabili e conserva contatori di esclusione. I riferimenti URL negli esempi rimangono null finche non risolti tramite 3.2. Nessun DB/Java/sito modificato. |


## Decisioni D-099--D-112: integrazione nativa Records Top & Flop

| ID | Stato | Decisione / evidenza |
|---|---|---|
| D-099 | APPROVATA DALL'UTENTE | La nuova famiglia records si chiama **Records Top & Flop**. E parte di RecordsNext 4.0, non un progetto o publisher distinto. |
| D-100 | APPROVATA DALL'UTENTE | Riutilizzare le logiche 3.2.0 FUNZIONANTI per elaborazione, archivio, configurazione, GUI, esportazione, manifest, link e pubblicazione multisito; la 3.2 produttiva rimane intatta. |
| D-101 | APPROVATA DALL'UTENTE | Tutte le statistiche individuali complete costituiscono la base riutilizzabile per schede e Records Top & Flop: calcolo dei record come aggregazione derivata, non raccolta dati autonoma. |
| D-102 | VERIFICATA SORGENTE 3.2 | Il catalogo record e in `RecordFamily`, `CoreRecordCatalog`; configurazione in `ProcessingConfigWriter` e `ProcessingConfigLoader`; `PipelinePreflight` e `DependencyInventory` gestiscono i prerequisiti. |
| D-103 | VERIFICATA SORGENTE 3.2 | La pipeline centrale e `RecordsNextPipeline` e il publisher JS e `Records2026SitePublisher`, con controllo esplicito del numero e dei prefissi dei JS prodotti. |
| D-104 | VERIFICATA SORGENTE 3.2 | `SeasonFamilyShardPublisher` divide per stagione, crea facade e shard JS flat nella cartella js con massimo predefinito 1536 KiB. Estendere la lista FAMILIES e i relativi test. |
| D-105 | VERIFICATA SORGENTE 3.2 | `SeasonNormalizedExporter` produce i riferimenti canonici al tabellino; `MatchesJsExporter` li espone tramite `scorecardUrl`. Nessun URL storico sintetizzato a mano. |
| D-106 | REGOLA IMPLEMENTATIVA | Il filtro sportivo CAMPIONATO/COPPE e separato dalla famiglia catalogo `TOP_FLOP`. Play Off - Play Out e Spareggio Serie B=CAMPIONATO; Youth League sempre esclusa dai dati pubblici. |
| D-107 | REGOLA IMPLEMENTATIVA | Stessa prestazione reale FCA non conteggiata piu volte nei record reali, anche se utilizzata in molte partite/competizioni fantasy; ogni match fantasy resta distinto nei record fantasy. |
| D-108 | REGOLA IMPLEMENTATIVA | Prevedere FULL e CONSOLIDATED, multisito, rollback e validazioni JS 3.2: nessuna scorciatoia per i nuovi dati e nessun deploy autonomo. |
| D-109 | REGOLA IMPLEMENTATIVA | Non attivare la nuova famiglia nel catalogo selezionabile o in pubblicazione finche archivio e exporter non sono funzionanti e testati; impedire successo apparente con output vuoti. |
| D-110 | VERIFICATA FASE 3C1 | ZIP Windows 3C1: `stato=OK`, 13 test, 552025 originali, 546775 ammessi, 5250 Youth esclusi, DB sandbox invariato, bibbia v13 generata. |
| D-111 | APERTA | Elenco definitivo e soglie dei singoli records Top & Flop, definizione formale delle esclusioni senza Formazione e validita storica dei tesseramenti. Non inventare classifiche o soglie. |
| D-112 | IMPLEMENTATA: DOCUMENTAZIONE | Questa fase aggiunge mappa integrazione e regole alla continuita documentale, senza modificare la pipeline Java, la GUI, gli output o il database. La famiglia non e ancora implementata. |
~~~~

### `docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v16.md`

SHA-256: `c83d5b964dc48d976f2b2b16399009e82950136a008fde17ebf2ce0b142163dd`

~~~~markdown
# RecordsNext 4.0 - Diario v16: lettore FCA JDBC

2026-11-01: confrontati realmente i due file SQLite della 3.2 allegati. Rilevata differenza confinata al 2026/27, esplicitata in piano, registro, specifica e test. Implementato `TopFlopFcaJdbcReader` come nuova classe nel package Java esistente `app/topflop`. Prove Java 21 su 21 stagioni di ciascuno dei due DB, con controllo contro conteggi SQL indipendenti, e test filtrato codici 100843/12302. Source e database produttivi invariati. Validazione finale della installazione sul PC ancora da ricevere. Non vengono fabbricati `FantasyFact` o esclusioni.


---

# RecordsNext 4.0 - Diario implementazione v15

## Intervento: primo codice Java Top & Flop

- Riletti `RecordFamily`, `CoreRecordCatalog`, `SeasonNormalizedExporter`, `MatchesJsExporter`, specifica e mappa integrazione v14, sorgenti 3.2 contenuti nell'archivio dell'utente.
- Aggiunte classi nuove `TopFlopFacts`, `TopFlopArchive`, `TopFlopMetrics`, `TopFlopCompetitionPolicy` in `app/topflop`, senza riscrivere le classi precedenti.
- Aggiunti test autonomo `TopFlopArchiveSmokeTest` (52 verifiche) e wrapper `TopFlopArchiveJUnitTest` per la suite JUnit 5 esistente.
- Eseguita compilazione dei 106 sorgenti Java originali + quattro nuove classi col compilatore Java 21.0.12.1: OK. Eseguito smoke test Java: 52 verifiche OK. Maven/JUnit integrale non eseguito nell'ambiente di analisi; la verifica sul PC utente resta da completare.
- Predisposti runner conservativo, nuova bibbia incrementale v15 e ZIP diagnostico in `downloads`.
- La prova della seconda esecuzione ha inizialmente evidenziato compilazione duplicata dei quattro file gia installati; corretto il runner per escludere le classi del kit dall'elenco dei sorgenti preesistenti. Nuova prova integrale eseguita da zero con due passaggi consecutivi (vedi log di verifica).
- Nessuna modifica a database, GUI, publisher, archivio siti, configurazione o versione Maven. Nessun commit o deploy automatico.

---

# RecordsNext 4.0 - Diario implementazione v14

# RecordsNext 4.0 - Diario implementazione v13

# RecordsNext 4.0 - Continuita documentale DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v12

Versione additiva del 2026-11-01. La cronologia integrale precedente e preservata sotto.

# RecordsNext 4.0 - Diario implementazione v10

Data: 2026-11-01.

## Revisione dello ZIP Windows 3B1

- ZIP `RecordsNext4_Fase3B1_OUTPUT_v1_20261101_001744_607896.zip`: 14 voci; `MANIFEST_FASE3B1_v1.json` con `stato=OK`.
- Log `LOG_TEST_CONTRIBUZIONI_v1.txt`: sette test su Windows, tutti superati.
- SHA-256 dei file `combinazioni.csv`, `campioni.csv`, `sintesi.json` controllati confrontando gli hash dichiarati nel manifest con i byte nello ZIP.
- Invariato lo SHA del database sandbox: `57923f4e75986c2b81533e4beef15b9133635258d7de1d36827a1bdbe576671b`.
- 22.265 tabellini, 552.025 righe Formazione, 237.686 giocatori nei primi undici slot, 22.028 panchinari nei primi undici, 27.319 titolari iniziali fuori dai primi undici; nessuna attribuzione di punti persi.
- `CODICE_FUNZIONANTE_RECORDSNEXT4_v8.md` generato; nessun commit/deploy eseguito dal runner.

## Sviluppo Fase 3B2

- Riletto lo schema raw FCM originale della 3.2: `Competizione`, `Girone`, `Incontro`, `Tabellino`, `Modulo`, `ModuloAmmesso` e analisi RU personali.
- Implementato `Audit-RecordsNext4Regole-v1.py` in modalita SQLite `mode=ro` + `query_only=ON`.
- Il censimento e per coppia stagione/competizione, con mantenimento di tutte le colonne originali `Competizione` e dei moduli ammessi. La tabella di dettaglio non contiene dati personali delle fantasquadre.
- Osservati 21 archivi, 180 configurazioni, 13.072 incontri (compresi quelli programmati/riposi), 22.265 tabellini; 168 senza ID incontro valido e 23 collegati a incontri con una squadra 0.
- Test automatici `Test-RecordsNext4Regole-v1.py`: otto test sintetici e su DB reale, superati in ambiente di prova. Il database sintetico temporaneo viene generato sotto la cartella diagnostica `downloads`, mai in `%TEMP%`.
- Progettati runner versionato v1, specifica e verifica v1, piano v10, decisioni v9 e bibbia v9 con SHA-256 dei nuovi file.
- La verifica esecutiva del nuovo runner su PowerShell 5.1/Windows e ancora da effettuare; non presentarla come completata.

## Esclusioni

Nessuna modifica ai dati, alle impostazioni delle competizioni, ai 4 script RU personali, al Java, al sito o alla GUI. Nessun calcolo di voti recuperabili, nessun commit.


## Verifica dell'output Fase 3B2 sul PC e decisione operativa

- Esaminato ZIP Windows `RecordsNext4_Fase3B2_OUTPUT_v1_20261101_002713_328984.zip`.
- `MANIFEST_FASE3B2_v1.json`: `stato=OK`, branch `feature/recordsnext4-players`, base `f9bf30323d9204d70e91e242bddfc72c2060ecf9`; stessa impronta SHA-256 del sandbox prima/dopo.
- `LOG_TEST_REGOLE_v1.txt`: otto test superati. `LOG_AUDIT_REGOLE_v1.txt`: 21 stagioni, 180 competizioni, 13.072 incontri, 22.265 tabellini; 168 riferimenti senza incontro rimangono non classificati.
- Riletto `docs/ANALISI_PERSONALE_RU.md` e il glossario di `tools/Report_RU_Storico_Completo_v8_4.ps1`: RU = voto assegnato d'ufficio a copertura di una mancanza. Non associare una RU a un giocatore storico reale.
- L'utente ha approvato la metrica **individuale**, non il controfattuale di squadra: 23 occasioni fuori, 3 gare con voto 6 -> 18/23. Nessun calcolo effettivo sui giocatori e nessun cap implementati in questa revisione.
- Creato test unitario della **sola formula** su dati sintetici. Prima della successiva implementazione su SQLite occorre verificare su esempi reali quali righe costituiscano esclusioni e quale voto sia valido.
- Questa revisione installa soltanto documentazione, un test di formula, il generatore v10 e un runner conservativo. Non modifica Java, GUI, pipeline, archivi 3.2 o siti.


## Revisione della metrica su richiesta utente (2026-11-01)

- Esaminato lo ZIP di esecuzione Windows `RecordsNext4_DecisioniGiocatori_OUTPUT_v1_20261101_003202_168980.zip`: contiene bibbia v10, registro v10, test formula v1 e manifest `stato=OK`.
- Distinti il rendimento medio nelle sole esclusioni con voto (`S/P`) e la misura stagionale su tutte le esclusioni (`S/E`), per voto reale e fantavoto completo di bonus/malus.
- Creati `SPECIFICA_METRICA_VOTO_SPRECATO_GIOCATORE_v2.md` e `Test-RecordsNext4MetricheIndividuali-v2.py` per il contratto puramente aritmetico; test su dati sintetici, nessuna lettura FCM/FCA in questo intervento.
- Creati piano v12, registro decisioni v11, diario v11 e generatore bibbia v11. Conservate le versioni precedenti.
- Restano da verificare sui dati reali la definizione operativa di esclusione, voto valido, fantavoto disponibile per esclusi, bonus/malus storici e deduplicazione delle partite fantasy parallele.
- Nessuna implementazione di record nei sorgenti Java e nessun deploy; database invariato perche non viene aperto.


## Fase 3C0 - 2026-11-01

- Verificato lo ZIP Windows `RecordsNext4_Metriche_OUTPUT_v1_20261101_004055_490269.zip`: manifest OK, 8 test metriche superati, bibbia v11 prodotta; nessun deploy/commit dichiarato.
- Esaminato il database sandbox 3.2 in sola lettura: 21 stagioni, 552.025 righe Formazione, 180 righe Competizione con 25 nomi distinti; la stessa giornata puo contenere piu incontri fantasy per giocatore-squadra.
- Caso campione 2025/26, codice 100843: tre tesseramenti snapshot, presenza nello stesso giorno in campionato e coppa per River Pino.
- Il prototipo di audit Fase 3C0 verifica l'integrita delle join ed elenca le competizioni non assegnate, senza interpretare le esclusioni o sommare i voti.
- Stato: audit testato localmente, codice integrale nella bibbia incrementale v12; installazione e test Windows ancora da confermare.

## Fase 3C1 - revisione classificazione e riferimenti tabellino

- Ricevuto e verificato output 3C0 Windows: `ESITO OK`, 10/10 test, 552025 righe Formazione; SHA256 del sandbox invariato, bibbia v12. Le 159678 sovrapposizioni sono contesti fantasy distinti, non duplicazioni reali.
- L'utente ha classificato `Play Off - Play Out` e `Spareggio Serie B` come campionati e richiesto l'esclusione completa di `Youth League` da record/schede.
- Fonte Java 3.2 esaminata: `SeasonNormalizedExporter.java` espone `urlTabellino`, `urlTabellinoLocale`, `urlTabellinoOnline` e conserva `source_event_id`; `MatchesJsExporter.java` espone `matchId`, `competitionId`, `seasonId`, `scorecardUrl`.
- L'audit v2 mantiene ID incontro e giornata; aggiunge `idGiornataFcm` e `scorecardLinkStatus` negli esempi. URL effettivo assente nell'audit raw: resta null.
- Nessuna simulazione RU/sostituzioni, nessun calcolo degli esclusi mancanti da Formazione, nessuna modifica alla pipeline o al DB.


## Revisione 2026-11-01 - nuova famiglia Records Top & Flop

- L'utente ha chiarito che l'intero modulo giocatori e **una famiglia record del RecordsNext esistente**, con il nome pubblico **Records Top & Flop**.
- Rianalizzati gli effettivi sorgenti 3.2.0 forniti (`RecordsNext2.0.zip`), in particolare `RecordFamily`, `CoreRecordCatalog`, `ProcessingConfigWriter`, `ProcessingConfigLoader`, `RecordsNextPipeline`, `Records2026SitePublisher`, `SeasonFamilyShardPublisher`, `MatchesJsExporter`, `SeasonNormalizedExporter`, `RecordsNext2Dashboard`.
- `Records2026SitePublisher.validateGenerated` valida anche **il conteggio totale** dei file JS e i loro prefissi: richiede modifica coordinata prima di aggiungere un nuovo exporter. `SeasonFamilyShardPublisher.FAMILIES` e la lista esplicita delle famiglie sharded: richiede aggiunta con test flat.
- La 3.2 usa facade/shard per stagione nei js dei siti, con scope multisito e protezione rollback. Va **estesa**, non replicata con tool dedicati.
- Verificato output Windows Fase 3C1: 13/13 test, 21 stagioni, 552025 Formazione, 5250 Youth League escluse, 546775 ammesse, DB immutato.
- Stato codice: **nessun Java/SQLite/GUI/sito modificato in questa revisione**. Restano prototipi audit; prossimo sviluppo sul codice Java nel worktree 4.0. Documenti e script presenti nella bibbia incrementale v14.
~~~~

### `tools/Create-RecordsNext4WorkingCodeMd-v16.py`

SHA-256: `c7d31fbea58c5187a1e787d39247216a1e035c51b48cfa4d2b5a875c24ba343c`

~~~~python
#!/usr/bin/env python3
"""Append-only v16 bible; includes exact Java/Python/Markdown code and SHA-256."""
from __future__ import annotations
import argparse
import hashlib
from pathlib import Path
import subprocess
import sys

PREVIOUS = 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v15.md'
DEST = 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v16.md'
NEW_FILES = [
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopFcaJdbcReader.java',
    'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopFcaJdbcSmokeTest.java',
    'docs/SPECIFICA_LETTORE_JAVA_FCA_TOP_FLOP_v1.md',
    'docs/VERIFICA_LETTORE_JAVA_FCA_TOP_FLOP_v1.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v17.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v16.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v16.md',
    'tools/Create-RecordsNext4WorkingCodeMd-v16.py',
    'tools/Invoke-RecordsNext4FcaJdbc-v1.py',
]


def sha(path: Path) -> str:
    h = hashlib.sha256()
    with path.open('rb') as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b''):
            h.update(block)
    return h.hexdigest()


def git(root: Path, *args: str) -> str:
    p = subprocess.run(['git', '-C', str(root), *args], capture_output=True,
                       text=True, encoding='utf-8', errors='replace')
    if p.returncode:
        raise RuntimeError('Git: ' + p.stderr.strip())
    return p.stdout.strip()


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument('--project-dir', required=True, type=Path)
    root = ap.parse_args().project_dir.resolve(strict=True)
    for name in [PREVIOUS, *NEW_FILES]:
        if not (root / name).is_file():
            raise RuntimeError('Prerequisito bibbia non trovato: ' + name)
    lines = [
        '# RecordsNext 4.0 - Bibbia incrementale v16', '',
        'Lettore JDBC FCA di sola lettura collegato al modello Java Top & Flop.',
        '**Non integrato ancora nel motore di elaborazione/pubblicazione.**', '',
        '## Provenienza', '',
        '- Branch: `' + git(root, 'branch', '--show-current') + '`',
        '- HEAD: `' + git(root, 'rev-parse', 'HEAD') + '`',
        '- Riferimento preservato: `'+ PREVIOUS +'`',
        '- SHA-256 bibbia v15: `' + sha(root / PREVIOUS) + '`',
        '- Codice Java 3.2 esistente: invariato in questa installazione.', '',
        '## File integrali e impronte SHA-256', '',
    ]
    for name in NEW_FILES:
        path = root / name
        language = 'java' if name.endswith('.java') else ('python' if name.endswith('.py') else 'markdown')
        lines += ['### `' + name + '`', '', 'SHA-256: `' + sha(path) + '`', '',
                  '~~~~' + language, path.read_text(encoding='utf-8-sig').rstrip('\n'),
                  '~~~~', '']
    content = ('\n'.join(lines).rstrip() + '\n').encode('utf-8')
    target = root / DEST
    if target.exists():
        if not target.is_file() or target.read_bytes() != content:
            raise RuntimeError('Bibbia v16 esistente ma diversa, non sovrascrivo')
        print('BIBBIA v16 GIA PRESENTE IDENTICA:', target, flush=True)
    else:
        with target.open('xb') as stream:
            stream.write(content)
        print('BIBBIA v16 GENERATA:', target, flush=True)
    return 0


if __name__ == '__main__':
    try:
        sys.exit(main())
    except Exception as exc:
        print('ERRORE BIBBIA v16:', exc, file=sys.stderr)
        sys.exit(1)
~~~~

### `tools/Invoke-RecordsNext4FcaJdbc-v1.py`

SHA-256: `6469d29022c5e97c4538ec8bdda6e60937628ac7f38023dabb7a647ccb382e43`

~~~~python
#!/usr/bin/env python3
"""Conservative native FCA reader installer. Java 21; 3.2 source/JDBC reused read-only."""
from __future__ import annotations

import argparse
from datetime import datetime
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
import traceback
from zipfile import ZipFile, ZIP_DEFLATED

NEW_FILES = [
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopFcaJdbcReader.java',
    'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopFcaJdbcSmokeTest.java',
    'docs/SPECIFICA_LETTORE_JAVA_FCA_TOP_FLOP_v1.md',
    'docs/VERIFICA_LETTORE_JAVA_FCA_TOP_FLOP_v1.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v17.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v16.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v16.md',
    'tools/Create-RecordsNext4WorkingCodeMd-v16.py',
    'tools/Invoke-RecordsNext4FcaJdbc-v1.py',
]
REQUIRED = [
    'pom.xml',
    'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v15.md',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopArchive.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopFacts.java',
    'src/main/java/it/alterlega/recordsnext/app/model/RecordFamily.java',
]
PRESERVE = [
    'pom.xml',
    'src/main/java/it/alterlega/recordsnext/app/model/RecordFamily.java',
    'src/main/java/it/alterlega/recordsnext/app/model/CoreRecordCatalog.java',
    'src/main/java/it/alterlega/recordsnext/app/RecordsNextPipeline.java',
    'src/main/java/it/alterlega/recordsnext/Records2026SitePublisher.java',
    'src/main/java/it/alterlega/recordsnext/app/output/SeasonFamilyShardPublisher.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopArchive.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopFacts.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopMetrics.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopCompetitionPolicy.java',
]
SMOKE_OLD = 'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopArchiveSmokeTest.java'
SMOKE_NEW = 'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopFcaJdbcSmokeTest.java'
MAIN_OLD = 'it.alterlega.recordsnext.app.topflop.TopFlopArchiveSmokeTest'
MAIN_NEW = 'it.alterlega.recordsnext.app.topflop.TopFlopFcaJdbcSmokeTest'


def sha(path: Path) -> str:
    h = hashlib.sha256()
    with path.open('rb') as f:
        for part in iter(lambda: f.read(1048576), b''):
            h.update(part)
    return h.hexdigest()


def invoke(cmd: list[str]) -> tuple[int, str]:
    proc = subprocess.run(cmd, capture_output=True, encoding='utf-8', errors='replace',
                          env=dict(os.environ, PYTHONDONTWRITEBYTECODE='1'))
    return proc.returncode, proc.stdout + proc.stderr


def git(root: Path, *args: str) -> str:
    status, output = invoke(['git', '-C', str(root), *args])
    if status:
        raise RuntimeError('Git: ' + output[-2000:])
    return output.strip()


def java_jar(candidates: list[Path]) -> Path:
    for candidate in candidates:
        if not candidate.is_file():
            continue
        try:
            with ZipFile(candidate) as z:
                if 'org/sqlite/JDBC.class' in z.namelist():
                    return candidate
        except Exception:
            continue
    raise RuntimeError('JAR 3.2 con org.sqlite.JDBC non trovato. Nessun file installato.')


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument('--work-dir', type=Path, default=Path('D:/DEV_APPS/RecordsNext4.0'))
    parser.add_argument('--base-dir', type=Path, default=Path('D:/DEV_APPS/RecordsNext2.0'))
    parser.add_argument('--output-dir', type=Path, default=Path('D:/DEV_APPS/downloads'))
    parser.add_argument('--test-db', choices=('both','main','sandbox'), default='both',
                        help='Default both; use single DB only for local QA')
    args = parser.parse_args()
    kit = Path(__file__).resolve().parent.parent
    output = args.output_dir.resolve(strict=True)
    stamp = datetime.now().strftime('%Y%m%d_%H%M%S_%f')
    result_zip = output / ('RecordsNext4_FcaJdbc_OUTPUT_v1_' + stamp + '.zip')
    build = output / ('RecordsNext4_FcaJdbc_BUILD_v1_' + stamp)
    report = {'fase': 'TopFlop-Java-FCA-JDBC-v1', 'stato': 'ERRORE',
              'pipeline32Alterata': False, 'databaseModificato': False,
              'pubblicazioneEseguita': False, 'bibbia': 'v16', 'filesInstallati': []}
    logs: list[tuple[str, int, str]] = []
    work = None
    success = False
    try:
        work = args.work_dir.resolve(strict=True)
        base = args.base_dir.resolve(strict=True)
        if work == kit or work == base:
            raise RuntimeError('Il worktree 4.0 deve essere distinto dal kit e dalla 3.2')
        for name in REQUIRED:
            if not (work / name).is_file():
                raise RuntimeError('Prerequisito worktree 4.0 mancante: ' + name)
        if Path(git(work, 'rev-parse', '--show-toplevel')).resolve(strict=True) != work:
            raise RuntimeError('Worktree 4.0 non coincide con la root Git')
        branch = git(work, 'branch', '--show-current')
        if branch != 'feature/recordsnext4-players':
            raise RuntimeError('Branch 4.0 errato: ' + branch)
        report['branch'] = branch
        report['head'] = git(work, 'rev-parse', 'HEAD')
        before = {name: sha(work / name) for name in PRESERVE}
        report['preservedSha256Before'] = before
        for name in NEW_FILES:
            source, target = kit / name, work / name
            if not source.is_file():
                raise RuntimeError('File mancante nel kit: ' + name)
            if target.exists() and (not target.is_file() or sha(target) != sha(source)):
                raise RuntimeError('File gia presente ma DIVERSO: ' + name)
        database_paths = [base / 'data/database/recordsnext.db',
                          base / 'data/database/recordsnext-sandbox.db']
        if not all(p.is_file() for p in database_paths):
            raise RuntimeError('Manca almeno uno dei database 3.2, non avvio i test')
        database_before = {p.name: sha(p) for p in database_paths}
        report['databaseSha256Before'] = database_before
        jar = java_jar([base / 'release/RecordsNext_3.2.0_FULL/payload/RecordsNext.jar',
                        base / 'target/RecordsNext.jar',
                        work / 'target/RecordsNext.jar'])
        report['jar3_2'] = str(jar)
        javac, java = shutil.which('javac'), shutil.which('java')
        if not javac or not java:
            raise RuntimeError('JDK 21 richiesto, java/javac non trovati')
        status, version = invoke([javac, '-version'])
        logs.append(('JDK', status, version))
        match = re.search(r'\bjavac\s+(\d+)(?:\.|\s|$)', version)
        if status or match is None or int(match.group(1)) < 21:
            raise RuntimeError('JDK 21 o successivo richiesto: ' + version)
        build.mkdir(exist_ok=False)
        added = sorted((kit / 'src/main/java').rglob('*.java'))
        overlays = {file.relative_to(kit).as_posix() for file in added}
        existing = sorted(p for p in (work / 'src/main/java').rglob('*.java')
                          if p.relative_to(work).as_posix() not in overlays)
        if len(added) != 1 or len(existing) < 70:
            raise RuntimeError('Numero sorgenti inatteso: ' + str((len(added), len(existing))))
        command = [javac, '-encoding', 'UTF-8', '--release', '21',
                   '-cp', str(jar), '-d', str(build)] + [str(p) for p in existing + added] + [
                       str(work / SMOKE_OLD), str(kit / SMOKE_NEW)]
        status, result = invoke(command)
        logs.append(('COMPILA_JAVA', status, result))
        print('Compilazione Java completa:', 'OK' if not status else 'ERRORE', flush=True)
        if status:
            raise RuntimeError('Compilazione fallita: ' + result[-2500:])
        classpath = os.pathsep.join((str(build), str(jar)))
        status, result = invoke([java, '-ea', '-cp', classpath, MAIN_OLD])
        logs.append(('REGRESSIONE_52', status, result))
        print('Regressione 52 verifiche:', 'OK' if not status else 'ERRORE', flush=True)
        if status or 'TOP_FLOP_JAVA_TEST_OK checks=52' not in result:
            raise RuntimeError('Regressione 52 non superata: ' + result[-2500:])
        selected = database_paths if args.test_db == 'both' else [
            p for p in database_paths if (p.name == 'recordsnext.db') == (args.test_db == 'main')]
        report['testedDatabaseNames'] = [p.name for p in selected]
        for db in selected:
            status, result = invoke([java, '-Xmx2048m', '-cp', classpath, MAIN_NEW, str(db), '--quick'])
            logs.append(('FCA_' + db.stem.replace('-', '_'), status, result))
            last = result.strip().splitlines()[-1] if result.strip() else ''
            print('Verifica FCA', db.name, ':', last if not status else 'ERRORE', flush=True)
            if status or 'TOP_FLOP_FCA_JDBC_OK seasons=21' not in result:
                raise RuntimeError('Verifica FCA fallita (' + db.name + '): ' + result[-2500:])
        database_after = {p.name: sha(p) for p in database_paths}
        report['databaseSha256After'] = database_after
        if database_after != database_before:
            raise RuntimeError('Database 3.2 modificato: STOP')
        for name in NEW_FILES:
            source, target = kit / name, work / name
            if target.is_file():
                print('GIA PRESENTE IDENTICO:', name, flush=True)
            else:
                target.parent.mkdir(parents=True, exist_ok=True)
                with target.open('xb') as stream:
                    stream.write(source.read_bytes())
                report['filesInstallati'].append(name)
                print('INSTALLATO:', name, flush=True)
        status, result = invoke([sys.executable,
                                 str(work / 'tools/Create-RecordsNext4WorkingCodeMd-v16.py'),
                                 '--project-dir', str(work)])
        logs.append(('BIBBIA_V16', status, result))
        if status:
            raise RuntimeError('Bibbia v16 non generata: ' + result[-2500:])
        print('Bibbia v16: OK', flush=True)
        if {name: sha(work / name) for name in PRESERVE} != before:
            raise RuntimeError('File protetti della 3.2/TopFlop alterati: STOP')
        report['preservedSha256After'] = before
        report['bibbiaSha256'] = sha(work / 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v16.md')
        report['stato'] = 'OK'
        success = True
    except Exception:
        error = traceback.format_exc()
        logs.append(('ERRORE', 1, error))
        print('ERRORE FCA JDBC:\n' + error[-3500:], file=sys.stderr, flush=True)
    finally:
        if build.exists():
            shutil.rmtree(build)
        with ZipFile(result_zip, 'x', ZIP_DEFLATED) as z:
            z.writestr('MANIFEST.json', json.dumps(report, indent=2, ensure_ascii=False) + '\n')
            for label, code, result in logs:
                z.writestr('log/' + label + '.txt', 'exit_code=' + str(code) + '\n' + result)
            if work is not None:
                for name in NEW_FILES:
                    path = work / name
                    if path.is_file():
                        z.write(path, name)
                path = work / 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v16.md'
                if path.is_file():
                    z.write(path, 'docs/' + path.name)
        print('ESITO:', 'OK' if success else 'ERRORE', flush=True)
        print('ZIP DA CARICARE:', result_zip, flush=True)
    return 0 if success else 1


if __name__ == '__main__':
    sys.exit(main())
~~~~
