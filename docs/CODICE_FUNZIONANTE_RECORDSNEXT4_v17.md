# RecordsNext 4.0 - Bibbia incrementale v17

Lettore FCM JDBC read-only per la famiglia nativa Top & Flop.
**Nessuna integrazione ancora nel publisher, nella pipeline o nella GUI.**

## Origine e continuita

- Branch: `feature/recordsnext4-players`
- HEAD: `f9bf30323d9204d70e91e242bddfc72c2060ecf9`
- Bibbia precedente: `docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v16.md`
- SHA-256 bibbia precedente: `99152347e7ae8337d31e6e88b222c49f6ea1b86770f43ac95a1d6fd0e0b391e8`

## Codice e documenti integrali

### `src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopFcmJdbcReader.java`

SHA-256: `6adf43b5bfcdeabb2730dbb53779bdd658184a57aab18a094360a63d6fe70ee5`

~~~~java
package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.topflop.TopFlopFacts.CompetitionFamily;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.PlayerSeason;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Read-only FCM adapter. Joins match, team, competition and FCA season identity.
 * Tabellino slots are observed values, NOT authoritative proof of legal usage.
 * It never fabricates scorecard URLs, inferred absences or bonus coefficients.
 */
public final class TopFlopFcmJdbcReader {
    private TopFlopFcmJdbcReader() {}

    public record FormationLine(PlayerSeason player, int fantasyTeamId, String fantasyTeamName,
                                long matchId, int competitionId, String competitionName,
                                CompetitionFamily family, int serieARound, int fcmRoundId,
                                String fcmRoundDescription, int initialPosition,
                                int entered, int enteredAsReserve, int manual,
                                int tabellinoSlot, String tabellinoVote,
                                String tabellinoModifier, String tabellinoTotal,
                                boolean firstElevenSlot, int matchPlayed,
                                Map<String, String> sourceFields) {
        public FormationLine {
            Objects.requireNonNull(player);
            Objects.requireNonNull(fantasyTeamName);
            Objects.requireNonNull(competitionName);
            Objects.requireNonNull(family);
            sourceFields = Map.copyOf(sourceFields);
        }
    }

    /** One source tesseramento record, not guaranteed current for any matchday. */
    public record RosterLine(PlayerSeason player, int fantasyTeamId, long tesseratoId,
                             Map<String, String> sourceFields) {
        public RosterLine {
            Objects.requireNonNull(player);
            sourceFields = Map.copyOf(sourceFields);
        }
    }

    public interface Sink {
        void formation(FormationLine line) throws SQLException;
        void roster(RosterLine line) throws SQLException;
    }

    public record SeasonLoad(String seasonId, long sourceFormations, long allowedFormations,
                             long youthExcluded, long matchedSlots, long sourceTesserati,
                             long readTesserati) {}

    public static SeasonLoad loadSeason(Connection c, String seasonId, Sink sink) throws SQLException {
        Objects.requireNonNull(c, "connection");
        Objects.requireNonNull(sink, "sink");
        if (seasonId == null || !seasonId.matches("[0-9]{4}_[0-9]{4}"))
            throw new IllegalArgumentException("Invalid seasonId: " + seasonId);
        String fcm = "raw_" + seasonId + "_fcm_";
        String fca = "raw_" + seasonId + "_fca_";
        long sourceFormationCount = count(c, fcm + "formazione");
        long sourceRosterCount = count(c, fcm + "tesserato");
        long allowed = 0, youth = 0, matched = 0, totalRead = 0;
        // Join only non-secret fantasy-team name; never read passwords, contacts or email.
        String sql = "SELECT f.IDGIOC AS pid, a.CODICEGAZZA AS code, f.IDSQUADRA AS teamId," +
            " s.NOME AS teamName, f.IDINCONTRO AS matchId, c.ID AS competitionId," +
            " c.NOME AS competitionName, i.GIORNATADIA AS realRound," +
            " i.IDGIORNATA AS fcmRoundId, gr.DESC AS fcmRoundDescription," +
            " f.POS AS position, f.ENTRATO AS entered, f.ENTRATORISERVA AS enteredReserve," +
            " f.MANUALE AS manual, i.GIOCATO AS matchPlayed," +
            " t.LISTA AS slots, t.VOTO AS votes, t.MODIF AS modifiers, t.TOT AS totals," +
            " f.RIG AS formationRole, f.IDLEGA AS formationLeague, t.IDLEGA AS tabLeague," +
            " i.IDTIPO AS matchType, i.IDCASA AS homeTeam, i.IDFUORI AS awayTeam," +
            " i.IDGIRONE AS groupId, g.IDCOMPETIZIONE AS groupCompetitionId" +
            " FROM \"" + fcm + "formazione\" f" +
            " JOIN \"" + fcm + "incontro\" i ON i.ID=f.IDINCONTRO" +
            " JOIN \"" + fcm + "girone\" g ON g.ID=i.IDGIRONE" +
            " JOIN \"" + fcm + "competizione\" c ON c.ID=g.IDCOMPETIZIONE" +
            " JOIN \"" + fcm + "tabellino\" t" +
            " ON t.IDINCONTRO=f.IDINCONTRO AND t.IDSQUADRA=f.IDSQUADRA" +
            " JOIN \"" + fcm + "fantasquadra\" s ON s.ID=f.IDSQUADRA" +
            " JOIN \"" + fca + "giocatorea\" a ON a.ID=f.IDGIOC" +
            " JOIN \"" + fca + "giocain\" d" +
            " ON d.IDGIOCATORE=f.IDGIOC AND d.GIORNATA=i.GIORNATADIA" +
            " LEFT JOIN \"" + fcm + "giornata\" gr ON gr.ID=i.IDGIORNATA" +
            " ORDER BY f.IDINCONTRO, f.IDSQUADRA, f.IDGIOC";
        try (PreparedStatement statement = c.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) {
                totalRead++;
                String competitionName = rows.getString("competitionName");
                if (competitionName == null) throw new SQLException("Missing FCM competition");
                if (competitionName.trim().equalsIgnoreCase("Youth League")) {
                    youth++;
                    continue;
                }
                CompetitionFamily family = TopFlopCompetitionPolicy.requireFamily(competitionName);
                int fcaId = rows.getInt("pid");
                int code = rows.getInt("code");
                Integer codeOrNull = rows.wasNull() || code <= 0 ? null : code;
                int teamId = rows.getInt("teamId");
                long matchId = rows.getLong("matchId");
                String[] slots = split(rows.getString("slots"));
                String[] votes = split(rows.getString("votes"));
                String[] modifiers = split(rows.getString("modifiers"));
                String[] totals = split(rows.getString("totals"));
                if (slots.length != votes.length || slots.length != modifiers.length ||
                        slots.length != totals.length)
                    throw new SQLException("Tabellino array length mismatch: " + seasonId + "/" + matchId + "/" + teamId);
                int slot = 0;
                for (int index = 0; index < slots.length; index++) {
                    if (slots[index].equals(Integer.toString(fcaId))) {
                        if (slot != 0) throw new SQLException("Duplicate player in tabellino: " + seasonId + "/" + matchId + "/" + fcaId);
                        slot = index + 1;
                    }
                }
                if (slot == 0) throw new SQLException("Formazione player missing from tabellino: " + seasonId + "/" + matchId + "/" + fcaId);
                Map<String, String> origin = new LinkedHashMap<>();
                // Preserve source, not interpretation: the existing 3.2 normalizer owns links.
                for (String name : List.of("formationRole", "formationLeague", "tabLeague", "matchType",
                        "homeTeam", "awayTeam", "groupId", "groupCompetitionId")) {
                    String value = rows.getString(name);
                    if (value == null) origin.put("__SQL_NULL__." + name, "true");
                    else origin.put(name, value);
                }
                FormationLine line = new FormationLine(
                    new PlayerSeason(seasonId, fcaId, codeOrNull), teamId,
                    rows.getString("teamName"), matchId, rows.getInt("competitionId"),
                    competitionName, family, rows.getInt("realRound"),
                    rows.getInt("fcmRoundId"), rows.getString("fcmRoundDescription"),
                    rows.getInt("position"), rows.getInt("entered"),
                    rows.getInt("enteredReserve"), rows.getInt("manual"),
                    slot, votes[slot - 1], modifiers[slot - 1], totals[slot - 1],
                    slot <= 11, rows.getInt("matchPlayed"), origin);
                sink.formation(line);
                allowed++;
                matched++;
            }
        }
        if (totalRead != sourceFormationCount)
            throw new SQLException("FCM join lost/duplicated formation records: " + seasonId +
                " expected=" + sourceFormationCount + " got=" + totalRead);
        if (allowed + youth != sourceFormationCount || matched != allowed)
            throw new SQLException("FCM formation reconciliation failed: " + seasonId);
        long readRoster = 0;
        String rosterSql = "SELECT t.ID AS rosterId, t.IDGIOCATORE AS pid, " +
            " t.IDSQUADRA AS teamId, a.CODICEGAZZA AS code," +
            " t.RUOLO AS role, t.ANNODICONTRATTO AS contractYear, t.VENDUTO AS sold," +
            " t.INIZIOINFORTUNIO AS injuryStart, t.COSTOACQ AS boughtFor," +
            " t.COSTOCESS AS soldFor, t.IDSOSTITUITO AS replacedId" +
            " FROM \"" + fcm + "tesserato\" t" +
            " JOIN \"" + fca + "giocatorea\" a ON a.ID=t.IDGIOCATORE" +
            " ORDER BY t.ID";
        try (PreparedStatement statement = c.prepareStatement(rosterSql);
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) {
                int code = rows.getInt("code");
                Integer codeOrNull = rows.wasNull() || code <= 0 ? null : code;
                Map<String, String> fields = new LinkedHashMap<>();
                for (String field : List.of("role", "contractYear", "sold", "injuryStart", "boughtFor", "soldFor", "replacedId")) {
                    String value = rows.getString(field);
                    if (value == null) fields.put("__SQL_NULL__." + field, "true");
                    else fields.put(field, value);
                }
                sink.roster(new RosterLine(new PlayerSeason(seasonId, rows.getInt("pid"), codeOrNull),
                    rows.getInt("teamId"), rows.getLong("rosterId"), fields));
                readRoster++;
            }
        }
        if (readRoster != sourceRosterCount)
            throw new SQLException("FCM tesserato join lost rows: " + seasonId + ": " + readRoster + "/" + sourceRosterCount);
        return new SeasonLoad(seasonId, sourceFormationCount, allowed, youth, matched,
                              sourceRosterCount, readRoster);
    }

    private static String[] split(String value) {
        return value == null ? new String[0] : value.split("%", -1);
    }

    private static long count(Connection c, String table) throws SQLException {
        try (Statement statement = c.createStatement();
             ResultSet rows = statement.executeQuery("SELECT count(*) FROM \"" + table + "\"")) {
            return rows.getLong(1);
        }
    }
}
~~~~

### `src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopFcmJdbcSmokeTest.java`

SHA-256: `5ca3deaba1648f47ce990f3c3cbdeea5089f86838c8679356c41ba8becc06a3b`

~~~~java
package it.alterlega.recordsnext.app.topflop;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

/** Executable read-only source test; no JUnit dependency. */
public final class TopFlopFcmJdbcSmokeTest {
    private TopFlopFcmJdbcSmokeTest() {}
    public static void main(String[] args) throws Exception {
        if (args.length < 1 || args.length > 2) throw new IllegalArgumentException("database path [--all]");
        boolean all = args.length == 2 && args[1].equals("--all");
        Path db = Path.of(args[0]).toAbsolutePath();
        if (!db.toFile().isFile()) throw new IllegalArgumentException("Database not found: " + db);
        String url = "jdbc:sqlite:" + db.toUri() + "?mode=ro";
        List<String> seasons = all ? List.of("2006_2007", "2007_2008", "2008_2009", "2009_2010",
            "2010_2011", "2011_2012", "2012_2013", "2013_2014", "2014_2015", "2015_2016",
            "2016_2017", "2017_2018", "2018_2019", "2019_2020", "2020_2021", "2021_2022",
            "2022_2023", "2023_2024", "2024_2025", "2025_2026", "2026_2027") :
            List.of("2006_2007", "2023_2024", "2025_2026", "2026_2027");
        long rows = 0, allowed = 0, youth = 0, rosters = 0;
        long[] lautaroContexts = {0};
        Class.forName("org.sqlite.JDBC");
        try (Connection c = DriverManager.getConnection(url)) {
            for (String season : seasons) {
                var data = TopFlopFcmJdbcReader.loadSeason(c, season, new TopFlopFcmJdbcReader.Sink() {
                    public void formation(TopFlopFcmJdbcReader.FormationLine line) {
                        if (line.tabellinoSlot() <= 0 || line.serieARound() <= 0 || line.matchId() <= 0)
                            throw new AssertionError("Malformed formation line: " + line);
                        if (line.player().gazzaCode() != null && line.player().gazzaCode() == 100843)
                            lautaroContexts[0]++;
                    }
                    public void roster(TopFlopFcmJdbcReader.RosterLine line) {
                        if (line.fantasyTeamId() <= 0) throw new AssertionError("Invalid team ID");
                    }
                });
                rows += data.sourceFormations();
                allowed += data.allowedFormations();
                youth += data.youthExcluded();
                rosters += data.readTesserati();
                System.out.println("STAGIONE " + season + " FORMAZIONI " + data.sourceFormations() +
                    " AMMESSE " + data.allowedFormations() + " YOUTH " + data.youthExcluded() +
                    " TESSERATI " + data.readTesserati());
            }
        }
        if (rows != allowed + youth) throw new AssertionError("Row accounting failed");
        if (all && rows <= 0) throw new AssertionError("No FCM formations loaded");
        System.out.println("TOP_FLOP_FCM_JDBC_OK seasons=" + seasons.size() +
            " formations=" + rows + " allowed=" + allowed + " youth=" + youth +
            " rosters=" + rosters + " lautaroContexts=" + lautaroContexts[0]);
    }
}
~~~~

### `docs/SPECIFICA_LETTORE_JAVA_FCM_TOP_FLOP_v1.md`

SHA-256: `9bc383d96c503d12033b5a50d18d27eaa8d36078336d5ad3da5ce5de0b37a434`

~~~~markdown
# RecordsNext 4.0 - Records Top & Flop - Lettore FCM JDBC v1

Data: 2026-11-01. Modulo nativo Java, **read-only**, additivo. Non sostituisce il motore di importazione, gli archivi, gli exporter, la GUI o la pubblicazione di RecordsNext 3.2.

## Fonti e relazioni effettivamente usate

Per ogni stagione `AAAA_AAAA` importata nel medesimo SQLite della 3.2:

- `raw_<stagione>_fcm_formazione.IDINCONTRO -> fcm_incontro.ID`.
- `fcm_incontro.IDGIRONE -> fcm_girone.ID -> fcm_competizione.ID`.
- `(Formazione.IDINCONTRO, Formazione.IDSQUADRA) -> Tabellino.(IDINCONTRO, IDSQUADRA)`.
- `Formazione.IDGIOC -> fca_giocatorea.ID` **nella stessa stagione**, da cui `CODICEGAZZA`.
- `fca_giocain.(IDGIOCATORE, GIORNATA) = (Formazione.IDGIOC, Incontro.GIORNATADIA)`.
- `Formazione.IDSQUADRA -> fcm_fantasquadra.ID`; si legge esclusivamente `NOME`, **non** indirizzi, contatti o password.
- `fcm_tesserato.IDGIOCATORE -> fca_giocatorea.ID`; i tesseramenti sono letti separatamente per non moltiplicare le righe `Formazione`.

`TopFlopFcmJdbcReader.loadSeason(Connection, seasonId, Sink)` fornisce al chiamante le righe in streaming. Non conserva l'intera stagione in RAM. Il chiamante fornisce una connessione JDBC come nella 3.2; il test usa esclusivamente `mode=ro`.

## Dati esposti per ogni episodio FCM

- Identita annuale FCA, codice storico Gazzetta, fantasia squadra (ID e nome), ID incontro, ID competizione, nome e famiglia CAMPIONATO/COPPE.
- `GIORNATADIA` reale e `IDGIORNATA` FCM **distinti**; `IDGIORNATA` e una chiave, NON un ordinale di calendario. Conservata anche la descrizione `Giornata.DESC`.
- Stato fonte `Formazione.POS`, `ENTRATO`, `ENTRATORISERVA`, `MANUALE` e il ruolo.
- Posizione verificata nella lista `Tabellino.LISTA` e i corrispondenti valori TESTUALI `VOTO`, `MODIF`, `TOT` (inclusi decimali con virgola e `sv/ng`).
- `firstElevenSlot` indica unicamente la posizione nei primi undici slot; **non** attesta la legalita delle sostituzioni, la presenza effettiva o il contributo regolamentare.
- L'incontro conserva `GIOCATO`, `IDTIPO`, ID squadra casa/fuori e ID girone; la disponibilita dello storico non e inferita.
- `Tesserato` e una fotografia sorgente con ID riga e dettagli originali; non prova la titolarita del tesseramento in ogni singola giornata.

## Limiti intenzionali

- **Non** costruisce URL tabellini: questi saranno ottenuti con le logiche **gia funzionanti della 3.2**, che includono le configurazioni del sito/stagione. Non costruire link da ID incontro o root corrente.
- Non assegna `Usage.EXCLUDED_CONFIRMED` da `Formazione` assente o da `Tesserato`.
- Non determina ancora fantavoti dei giocatori completamente non schierati; i valori del tabellino di una riga presente sono i valori memorizzati nella sorgente, non un voto inventato.
- Non sostituisce `TopFlopArchive`, non calcola record/medie, non attiva la famiglia `TOP_FLOP` nella GUI/pipeline/publisher.
- Ignora per record e schede pubbliche la Youth League, ma ne conserva il **conteggio tecnico**.
- I codici `0`/`-1` nei tabellini sono slot speciali e non generano righe giocatore.
- Il modello e' un estrattore di FCM disponibile **per righe `Formazione` esistenti**. Le assenze da formazione e la disponibilita storica saranno esaminate separatamente.

## Verifiche gia eseguite sui database 3.2 disponibili

Sia `recordsnext.db` sia `recordsnext-sandbox.db`: 21 stagioni, 552.025 righe `Formazione` originali, 546.775 ammesse, 5.250 Youth League escluse; tutti gli slot dei giocatori ammessi trovati, 14.320 righe `Tesserato` lette senza perdite. FCM 2026/27 non contiene ancora righe `Formazione` ma ha tesserati. I due database differiscono per una parte dell'FCA 2026/27, non per questi conteggi FCM.

I link, le metriche, gli aggregati storici e la connessione definitiva al modello Top & Flop saranno sviluppi **successivi**, sempre nel perimetro della 3.2.
~~~~

### `docs/VERIFICA_LETTORE_JAVA_FCM_TOP_FLOP_v1.md`

SHA-256: `509ce34c6d257224d74bb3c166cd1585a1312cc735980e70d49939027039f46a`

~~~~markdown
# RecordsNext 4.0 - Evidenze FCM JDBC v1

- Base: `RecordsNext2.0.zip` fornito dall'utente, worktree 4.0 del ramo `feature/recordsnext4-players`.
- Output precedente ricevuto: `RecordsNext4_FcaJdbc_OUTPUT_v1_20261101_205632_731016.zip`, `MANIFEST.json` dichiara `stato=OK`, JDK 21, test precedenti 52, entrambi i database invariati, bibbia v16 generata. Il test Windows precedente ha letto tre stagioni per DB; il test completo FCA era separato.
- Questo intervento: compilazione Java 21 e scansione effettiva **integrale** FCM su entrambi i database della copia di sviluppo, senza scritture.
- 21 stagioni; 552.025 Formazione; 546.775 ammesse; 5.250 Youth League escluse; 14.320 Tesserato; nessun record Formazione perso nelle join; slot corrispondenti verificati.
- Test Windows 5.1 da confermare con ZIP `RecordsNext4_FcmJdbc_OUTPUT_v1_*.zip`.
- Non sono state modificate le classi della 3.2, il database, la pipeline, la GUI, il sito o le regole di competizione.
- Anche in caso di 52/52 test superati non attribuire il voto sprecato prima di classificare le vere esclusioni.
~~~~

### `docs/PIANO_RECORDSNEXT4_GIOCATORI_v18.md`

SHA-256: `d9e073d00ace1ec7a802d1b4c5ed4c4790d65da43fd1370d74d6e79dd3066c35`

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


## Aggiornamento v18 - lettore Java FCM JDBC

- Output Windows `RecordsNext4_FcaJdbc_OUTPUT_v1_20261101_205632_731016.zip` verificato, `stato=OK`, compilazione e 52 verifiche; letti tre campioni per database, hash DB invariati, bibbia v16.
- Additivo `TopFlopFcmJdbcReader`: contesti Formazione/Incontro/Girone/Competizione/Tabellino/FCA/Team e Tesserato snapshot, tutti in sola lettura; controlli integrita sui 21 anni.
- Non usare IDGIORNATA come giornata progressiva; non attribuire esclusioni da assenze; Youth League solo nel conteggio tecnico.
- Successivo: collegamento tra lettori FCA/FCM e archivio dei fatti `TopFlopArchive`, risoluzione URL tramite 3.2, voto/fantavoto e bonus/malus verificati, poi classificazione degli stati. Solo dopo famiglia record selezionabile e pubblicabile.
- Java/GUI/publisher/DB originari della 3.2 rimangono immutati.
~~~~

### `docs/REGISTRO_DECISIONI_RECORDSNEXT4_v17.md`

SHA-256: `0ddc97b64af33291021be9449ebb830ee1ce9ae01ae22493d6391bff7baa4fea`

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


## Decisioni D-131--D-139 - Collegamento JDBC FCM v1

| ID | Stato | Decisione / verifica |
|---|---|---|
| D-131 | APPROVATA DALL'UTENTE | Il modulo e' la famiglia nativa Records Top & Flop, estesa dalla 3.2, non una pipeline separata. |
| D-132 | IMPLEMENTATA E VERIFICATA | Il lettore FCM usa le relazioni originali Formazione/Incontro/Girone/Competizione/Tabellino e ID FCA della medesima stagione. |
| D-133 | IMPLEMENTATA E VERIFICATA | Competizioni parallele e piu fantasquadre non sono aggregate: una riga per giocatore/incontro/team; 552025 contesti originari. |
| D-134 | APPROVATA DALL'UTENTE | Youth League esclusa dai dati utilizzabili per record/schede; 5250 righe sono comunque contate nell'audit. Play Off e Spareggio Serie B restano CAMPIONATO. |
| D-135 | REGOLA TECNICA | `IDGIORNATA` FCM resta chiave dell'archivio, distinta da `GIORNATADIA`; non dedurre un ordinale e non costruire URL arbitrari. |
| D-136 | REGOLA TECNICA | Il posto nei primi undici slot del Tabellino non certifica da solo la prestazione legalmente conteggiata; ENTRATO/ENTRATORISERVA restano campi fonte distinti. |
| D-137 | REGOLA TECNICA | Tesserato FCM e' snapshot, non archivio cronologico; non inventare esclusioni dove manca una riga Formazione. |
| D-138 | IMPLEMENTATA | Fonte JSON/JS pubblica non ancora generata: lettore Java FCM in streaming e test JDBC senza modifica a DB, pipeline o GUI. |
| D-139 | APERTA | Integrare nella stessa istanza `TopFlopArchive` FCA+FCM, provenienza del fantavoto per esclusi, regole voti speciali e URL tabellini con normalizzatore 3.2. |
~~~~

### `docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v17.md`

SHA-256: `e145ce913d13a7f2be557785c896fb8e15847014f81e0bdb75bf0cef97f2d5a3`

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


## Sviluppo 2026-11-01 - Lettore FCM JDBC v1

- Verificato il report Windows FCA v16: esito OK, nessuna variazione hash database o codice 3.2.
- Ispezionato schema completo raw FCM 2006/07-2026/27 e joins originali; nessuna variazione delle colonne principali tra le 21 stagioni.
- Creato `TopFlopFcmJdbcReader.java` con scansione streaming delle Formazione, Tabellino, Incontro, Girone, Competizione, Fantasquadra, anagrafica FCA e GiocaIn. Separato il censimento Tesserato.
- Preservati testi originari del Tabellino e ID di giornata FCM; esclusi Youth League, inclusi Play Off e Spareggio nella corretta famiglia.
- Completati test integrali su entrambe le copie SQLite disponibili: 552025 Formazione, 546775 ammesse, 5250 Youth League, 14320 Tesserato, tutte le join e gli slot verificati.
- Preparati installer non distruttivo, verifica doppia esecuzione, regressione 52 test, controllo hash DB e bibbia incrementale v17.
- Nessuna modifica a 3.2, export, GUI o pubblicazione; verifica Windows sul PC reale in attesa di ZIP diagnostico.
~~~~

### `tools/Create-RecordsNext4WorkingCodeMd-v17.py`

SHA-256: `30b576e185d5563569b2dacab37dd9648e531dd688d183d8bdd281afdc5f14e8`

~~~~python
#!/usr/bin/env python3
"""Generate one additive, versioned bible with entire new files and their SHA-256."""
from __future__ import annotations
import argparse
import hashlib
import subprocess
from pathlib import Path

PREVIOUS = 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v16.md'
DEST = 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v17.md'
NEW_FILES = [
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopFcmJdbcReader.java',
    'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopFcmJdbcSmokeTest.java',
    'docs/SPECIFICA_LETTORE_JAVA_FCM_TOP_FLOP_v1.md',
    'docs/VERIFICA_LETTORE_JAVA_FCM_TOP_FLOP_v1.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v18.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v17.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v17.md',
    'tools/Create-RecordsNext4WorkingCodeMd-v17.py',
    'tools/Invoke-RecordsNext4FcmJdbc-v1.py',
]

def sha(p: Path) -> str:
    h = hashlib.sha256()
    with p.open('rb') as stream:
        for block in iter(lambda: stream.read(1048576), b''):
            h.update(block)
    return h.hexdigest()

def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument('--project-dir', required=True, type=Path)
    root = parser.parse_args().project_dir.resolve(strict=True)
    for name in [PREVIOUS, *NEW_FILES]:
        if not (root/name).is_file():
            raise RuntimeError('Missing bible input: ' + name)
    branch = subprocess.check_output(['git', '-C', str(root), 'branch', '--show-current'], text=True).strip()
    head = subprocess.check_output(['git', '-C', str(root), 'rev-parse', 'HEAD'], text=True).strip()
    lines = [
        '# RecordsNext 4.0 - Bibbia incrementale v17', '',
        'Lettore FCM JDBC read-only per la famiglia nativa Top & Flop.',
        '**Nessuna integrazione ancora nel publisher, nella pipeline o nella GUI.**', '',
        '## Origine e continuita', '',
        '- Branch: `' + branch + '`', '- HEAD: `' + head + '`',
        '- Bibbia precedente: `' + PREVIOUS + '`',
        '- SHA-256 bibbia precedente: `' + sha(root/PREVIOUS) + '`', '',
        '## Codice e documenti integrali', '',
    ]
    for name in NEW_FILES:
        path = root/name
        language = 'java' if name.endswith('.java') else 'python' if name.endswith('.py') else 'markdown'
        lines.extend(['### `' + name + '`', '', 'SHA-256: `' + sha(path) + '`', '',
                      '~~~~' + language, path.read_text(encoding='utf-8-sig').rstrip('\n'),
                      '~~~~', ''])
    data = ('\n'.join(lines).rstrip() + '\n').encode('utf-8')
    dest = root/DEST
    if dest.exists():
        if not dest.is_file() or dest.read_bytes() != data:
            raise RuntimeError('Versioned bible exists but differs: ' + str(dest))
        print('BIBBIA v17 GIA PRESENTE IDENTICA:', dest, flush=True)
    else:
        dest.write_bytes(data) if not dest.exists() else None
        print('BIBBIA v17 GENERATA:', dest, flush=True)

if __name__ == '__main__':
    main()
~~~~

### `tools/Invoke-RecordsNext4FcmJdbc-v1.py`

SHA-256: `20baebf1d7db9a11ee9a9ac5e2a2523cd75e6e9be9d71b1a4fecf06fe47fe5f2`

~~~~python
#!/usr/bin/env python3
"""Safe Windows-compatible installation and read-only FCM JDBC audit."""
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
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopFcmJdbcReader.java',
    'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopFcmJdbcSmokeTest.java',
    'docs/SPECIFICA_LETTORE_JAVA_FCM_TOP_FLOP_v1.md',
    'docs/VERIFICA_LETTORE_JAVA_FCM_TOP_FLOP_v1.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v18.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v17.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v17.md',
    'tools/Create-RecordsNext4WorkingCodeMd-v17.py',
    'tools/Invoke-RecordsNext4FcmJdbc-v1.py',
]
REQUIRED = [
    'pom.xml',
    'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v16.md',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopFcaJdbcReader.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopArchive.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopFacts.java',
    'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopArchiveSmokeTest.java',
    'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopFcaJdbcSmokeTest.java',
]
PRESERVED = [
    'pom.xml',
    'src/main/java/it/alterlega/recordsnext/app/model/RecordFamily.java',
    'src/main/java/it/alterlega/recordsnext/app/model/CoreRecordCatalog.java',
    'src/main/java/it/alterlega/recordsnext/app/RecordsNextPipeline.java',
    'src/main/java/it/alterlega/recordsnext/Records2026SitePublisher.java',
    'src/main/java/it/alterlega/recordsnext/app/output/SeasonFamilyShardPublisher.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopFcaJdbcReader.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopArchive.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopMetrics.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopFacts.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopCompetitionPolicy.java',
]

def sha(path: Path) -> str:
    h = hashlib.sha256()
    with path.open('rb') as stream:
        for block in iter(lambda: stream.read(1048576), b''):
            h.update(block)
    return h.hexdigest()

def command(cmd: list[str]) -> tuple[int, str]:
    proc = subprocess.run(cmd, capture_output=True, text=True,
                          encoding='utf-8', errors='replace',
                          env=dict(os.environ, PYTHONDONTWRITEBYTECODE='1'))
    return proc.returncode, proc.stdout + proc.stderr

def git(root: Path, *args: str) -> str:
    code, output = command(['git', '-C', str(root), *args])
    if code: raise RuntimeError('Git: ' + output[-1200:])
    return output.strip()

def shaded_jar(paths: list[Path]) -> Path:
    for p in paths:
        if not p.is_file(): continue
        with ZipFile(p) as zipf:
            if 'org/sqlite/JDBC.class' in zipf.namelist(): return p
    raise RuntimeError('RecordsNext 3.2 shaded JAR con org.sqlite.JDBC non trovato')

def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument('--work-dir', type=Path, default=Path('D:/DEV_APPS/RecordsNext4.0'))
    parser.add_argument('--base-dir', type=Path, default=Path('D:/DEV_APPS/RecordsNext2.0'))
    parser.add_argument('--output-dir', type=Path, default=Path('D:/DEV_APPS/downloads'))
    parser.add_argument('--jar', type=Path, default=None, help='Solo per test: shaded JAR 3.2')
    args = parser.parse_args()
    kit = Path(__file__).resolve().parent.parent
    output = args.output_dir.resolve(strict=True)
    stamp = datetime.now().strftime('%Y%m%d_%H%M%S_%f')
    result = output/('RecordsNext4_FcmJdbc_OUTPUT_v1_' + stamp + '.zip')
    build = output/('RecordsNext4_FcmJdbc_BUILD_v1_' + stamp)
    report = {'phase': 'TopFlop-Java-FCM-JDBC-v1', 'status': 'ERROR',
              'branch': None, 'databaseModified': None, 'published': False,
              'bible': 'v17', 'installedFiles': []}
    logs: list[tuple[str, int, str]] = []
    work: Path | None = None
    success = False
    try:
        work = args.work_dir.resolve(strict=True)
        base = args.base_dir.resolve(strict=True)
        if work == base or work == kit:
            raise RuntimeError('Cartella 4.0 deve essere diversa da 3.2 e dal kit')
        for name in REQUIRED:
            if not (work/name).is_file(): raise RuntimeError('Prerequisito mancante: ' + name)
        if Path(git(work, 'rev-parse', '--show-toplevel')).resolve(strict=True) != work:
            raise RuntimeError('La cartella 4.0 non coincide con la root Git')
        branch = git(work, 'branch', '--show-current')
        if branch != 'feature/recordsnext4-players': raise RuntimeError('Branch errato: ' + branch)
        report['branch'] = branch
        report['head'] = git(work, 'rev-parse', 'HEAD')
        preserved = {name: sha(work/name) for name in PRESERVED}
        report['preservedSha256Before'] = preserved
        for name in NEW_FILES:
            source = kit/name
            dest = work/name
            if not source.is_file(): raise RuntimeError('File non presente nel kit: ' + name)
            if dest.exists() and (not dest.is_file() or sha(dest) != sha(source)):
                raise RuntimeError('File gia presente DIVERSO, nessuna sovrascrittura: ' + name)
        dbpaths = [base/'data/database/recordsnext.db',
                   base/'data/database/recordsnext-sandbox.db']
        if not all(path.is_file() for path in dbpaths):
            raise RuntimeError('Database 3.2 assente: non avvio la verifica')
        db_before = {p.name: sha(p) for p in dbpaths}
        report['databaseSha256Before'] = db_before
        jar = (args.jar.resolve(strict=True) if args.jar else shaded_jar([
            base/'release/RecordsNext_3.2.0_FULL/payload/RecordsNext.jar',
            base/'target/RecordsNext.jar', work/'target/RecordsNext.jar']))
        if not jar.is_file(): raise RuntimeError('JAR non trovato: ' + str(jar))
        report['jar'] = str(jar)
        java, javac = shutil.which('java'), shutil.which('javac')
        if not java or not javac: raise RuntimeError('JDK 21: java o javac mancanti')
        code, text = command([javac, '-version'])
        logs.append(('JDK', code, text))
        version = re.search(r'\bjavac\s+(\d+)(?:\.|\s|$)', text)
        if code or version is None or int(version.group(1)) < 21:
            raise RuntimeError('Richiesto JDK 21+: ' + text)
        build.mkdir(exist_ok=False)
        new_java = list((kit/'src/main/java').rglob('*.java'))
        new_paths = {p.relative_to(kit).as_posix() for p in new_java}
        existing = sorted(p for p in (work/'src/main/java').rglob('*.java')
                          if p.relative_to(work).as_posix() not in new_paths)
        if len(new_java) != 1 or len(existing) < 70:
            raise RuntimeError('Conteggio sorgenti inatteso: ' + str((len(new_java),len(existing))))
        sources = [str(p) for p in existing + new_java] + [
            str(work/'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopArchiveSmokeTest.java'),
            str(work/'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopFcaJdbcSmokeTest.java'),
            str(kit/'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopFcmJdbcSmokeTest.java')]
        code, text = command([javac, '-encoding', 'UTF-8', '--release', '21',
                              '-cp', str(jar), '-d', str(build), *sources])
        logs.append(('COMPILA_COMPLETA', code, text))
        print('Compilazione Java 3.2 + TopFlop:', 'OK' if code == 0 else 'ERRORE', flush=True)
        if code: raise RuntimeError('Compilazione: ' + text[-2500:])
        cp = os.pathsep.join((str(build), str(jar)))
        main_old = 'it.alterlega.recordsnext.app.topflop.TopFlopArchiveSmokeTest'
        code, text = command([java, '-ea', '-cp', cp, main_old])
        logs.append(('REGRESSIONE_52', code, text))
        if code or 'TOP_FLOP_JAVA_TEST_OK checks=52' not in text:
            raise RuntimeError('52 test precedenti non superati: ' + text[-2500:])
        print('Regressione 52 test: OK', flush=True)
        main_fca = 'it.alterlega.recordsnext.app.topflop.TopFlopFcaJdbcSmokeTest'
        code, text = command([java, '-Xmx2048m', '-ea', '-cp', cp, main_fca,
                              str(dbpaths[1]), '--quick'])
        logs.append(('REGRESSIONE_FCA', code, text))
        if code or 'TOP_FLOP_FCA_JDBC_OK seasons=21' not in text:
            raise RuntimeError('Regressione FCA non superata: ' + text[-2500:])
        print('Regressione FCA: OK', flush=True)
        main_fcm = 'it.alterlega.recordsnext.app.topflop.TopFlopFcmJdbcSmokeTest'
        for database in dbpaths:
            code, text = command([java, '-Xmx2048m', '-ea', '-cp', cp, main_fcm,
                                  str(database)])
            logs.append(('FCM_' + database.stem.replace('-', '_'), code, text))
            last = text.strip().splitlines()[-1] if text.strip() else ''
            print('Verifica FCM', database.name, ':', last if code == 0 else 'ERRORE', flush=True)
            if code or 'TOP_FLOP_FCM_JDBC_OK seasons=4' not in text:
                raise RuntimeError('FCM non valido in ' + database.name + ': ' + text[-2500:])
        db_after = {p.name: sha(p) for p in dbpaths}
        report['databaseSha256After'] = db_after
        report['databaseModified'] = db_after != db_before
        if db_after != db_before: raise RuntimeError('Database modificato: STOP')
        for name in NEW_FILES:
            source, target = kit/name, work/name
            if target.exists():
                print('GIA PRESENTE IDENTICO:', name, flush=True)
            else:
                target.parent.mkdir(parents=True, exist_ok=True)
                with target.open('xb') as stream: stream.write(source.read_bytes())
                report['installedFiles'].append(name)
                print('INSTALLATO:', name, flush=True)
        code, text = command([sys.executable, str(work/'tools/Create-RecordsNext4WorkingCodeMd-v17.py'),
                              '--project-dir', str(work)])
        logs.append(('BIBBIA_V17', code, text))
        if code: raise RuntimeError('Bibbia v17: ' + text[-1500:])
        print('Bibbia v17: OK', flush=True)
        if {name: sha(work/name) for name in PRESERVED} != preserved:
            raise RuntimeError('Un file Java/3.2 preesistente e stato modificato!')
        report['preservedSha256After'] = preserved
        report['bibleSha256'] = sha(work/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v17.md')
        success = True
        report['status'] = 'OK'
    except Exception as exc:
        report['error'] = str(exc)
        report['trace'] = traceback.format_exc()[-3500:]
        print('ERRORE:', str(exc), file=sys.stderr, flush=True)
    finally:
        shutil.rmtree(build, ignore_errors=True)
        try:
            with ZipFile(result, 'w', compression=ZIP_DEFLATED, compresslevel=8) as z:
                z.writestr('MANIFEST.json', json.dumps(report, ensure_ascii=False, indent=2))
                for label, code, log in logs:
                    z.writestr('log/' + label + '.txt', 'exit_code=' + str(code) + '\n' + log)
                for name in NEW_FILES:
                    file = (work/name if work and (work/name).is_file() else kit/name)
                    if file.is_file(): z.write(file, name)
                if work:
                    bible = work/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v17.md'
                    if bible.is_file(): z.write(bible, 'docs/' + bible.name)
        except Exception as exc:
            print('ERRORE creazione ZIP:', exc, file=sys.stderr, flush=True)
            return 1
        print('ESITO:', 'OK' if success else 'ERRORE', flush=True)
        print('ZIP DA CARICARE:', result, flush=True)
    return 0 if success else 1

if __name__ == '__main__':
    sys.exit(main())
~~~~
