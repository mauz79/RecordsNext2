# RecordsNext 4.0 - Bibbia v21 - round canonico Top & Flop

Base invariata: RecordsNext 3.2.0. Non si abilita la famiglia Top & Flop alla pubblicazione.

Previous bible: `docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v20.md`
Previous SHA-256: `3bc0e1c5b0fcfb8c5cb13bc59b76f53b86fa9e97c14553b375b03ba321e489c9`

## `src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopFacts.java`

SHA-256: `98af67fe1c1b0985435e9e28272fb9d33728235158df8f0941613d889e05ec07`

~~~~java
package it.alterlega.recordsnext.app.topflop;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

/**
 * Canonical, immutable facts for the native Records Top & Flop family.
 * These are facts, not the public record catalogue or a replacement publisher.
 * Season-local FCA IDs never stand in for cross-season player identities.
 */
public final class TopFlopFacts {
    private TopFlopFacts() {}

    public enum CompetitionFamily { CAMPIONATO, COPPE }

    /** No vote / special / not played are NOT silently converted to a zero vote. */
    public enum VoteState { VALID, NO_VOTE, SPECIAL, NOT_PLAYED, UNKNOWN }

    /**
     * Only EXCLUDED_CONFIRMED is eligible for a wasted-vote denominator.
     * BENCH_UNUSED and OUTSIDE_LIST are descriptive until eligibility is proven.
     */
    public enum Usage { COUNTED, BENCH_UNUSED, OUTSIDE_LIST, EXCLUDED_CONFIRMED, UNKNOWN }

    public record PlayerSeason(String seasonId, int fcaPlayerId, Integer gazzaCode) {
        public PlayerSeason {
            requireText(seasonId, "seasonId");
            if (fcaPlayerId <= 0) throw new IllegalArgumentException("fcaPlayerId must be positive");
            if (gazzaCode != null && gazzaCode <= 0)
                throw new IllegalArgumentException("gazzaCode must be positive when known");
        }
    }

    /** One actual Serie A day, regardless of how many fantasy matches reference it. */
    public record RealDayKey(PlayerSeason player, int serieARound) {
        public RealDayKey {
            Objects.requireNonNull(player, "player");
            if (serieARound <= 0) throw new IllegalArgumentException("serieARound must be positive");
        }
    }

    public record RealDay(RealDayKey key, VoteState voteState,
                          BigDecimal vote, Map<String, BigDecimal> events,
                          Map<String, String> sourceFields) {
        public RealDay {
            Objects.requireNonNull(key, "key");
            Objects.requireNonNull(voteState, "voteState");
            events = Map.copyOf(Objects.requireNonNull(events, "events"));
            sourceFields = Map.copyOf(Objects.requireNonNull(sourceFields, "sourceFields"));
            if ((voteState == VoteState.VALID) != (vote != null)) {
                throw new IllegalArgumentException("VALID requires a vote; other states require null vote");
            }
            for (var entry : events.entrySet()) {
                requireText(entry.getKey(), "event name");
                Objects.requireNonNull(entry.getValue(), "event value");
            }
        }
    }

    /** Specific FCM team match, not a real matchday or a generic round number. */
    public record FantasyKey(PlayerSeason player, int sourceCompetitionId,
                             long matchId, int fantasyTeamId) {
        public FantasyKey {
            Objects.requireNonNull(player, "player");
            if (sourceCompetitionId <= 0 || matchId <= 0 || fantasyTeamId <= 0)
                throw new IllegalArgumentException("competition/match/team IDs must be positive");
        }
    }

    /**
     * One actual fantasy context. Same player + real round may legitimately
     * appear in several teams and competitions. A scorecard URL is copied only
     * from the existing 3.2 normalizer; it must never be guessed from matchId.
     */
    public record FantasyFact(FantasyKey key, RealDayKey realDayKey,
                              String competitionName, CompetitionFamily family,
                              Integer fantasyRound, boolean initialStarter,
                              Usage usage, BigDecimal fantasyVote,
                              Map<String, BigDecimal> fantasyEvents,
                              String scorecardUrl, Map<String, String> sourceFields) {
        public FantasyFact {
            Objects.requireNonNull(key, "key");
            Objects.requireNonNull(realDayKey, "realDayKey");
            if (!key.player().equals(realDayKey.player()))
                throw new IllegalArgumentException("Fantasy and real day refer to different season players");
            requireText(competitionName, "competitionName");
            Objects.requireNonNull(family, "family");
            if (TopFlopCompetitionPolicy.requireFamily(competitionName) != family)
                throw new IllegalArgumentException("Wrong family for source competition: " + competitionName);
            if (fantasyRound != null && fantasyRound <= 0) throw new IllegalArgumentException("fantasyRound must be positive when mapped");
            Objects.requireNonNull(usage, "usage");
            fantasyEvents = Map.copyOf(Objects.requireNonNull(fantasyEvents, "fantasyEvents"));
            sourceFields = Map.copyOf(Objects.requireNonNull(sourceFields, "sourceFields"));
            for (var entry : fantasyEvents.entrySet()) {
                requireText(entry.getKey(), "fantasy event name");
                Objects.requireNonNull(entry.getValue(), "fantasy event value");
            }
            if (usage == Usage.EXCLUDED_CONFIRMED && initialStarter)
                throw new IllegalArgumentException("Confirmed excluded player cannot be an initial starter");
            // Null URL is allowed. Never manufacture a fallback URL here.
            if (scorecardUrl != null && scorecardUrl.isBlank()) scorecardUrl = null;
        }
    }

    /** Snapshot only. Never assert the player was owned on every round. */
    public record RosterSnapshot(PlayerSeason player, int fantasyTeamId,
                                 Map<String, String> sourceFields) {
        public RosterSnapshot {
            Objects.requireNonNull(player, "player");
            if (fantasyTeamId <= 0) throw new IllegalArgumentException("fantasyTeamId must be positive");
            sourceFields = Map.copyOf(Objects.requireNonNull(sourceFields, "sourceFields"));
        }
    }

    private static void requireText(String value, String label) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException(label + " cannot be empty");
    }
}
~~~~

## `src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopJdbcArchiveAssembler.java`

SHA-256: `ad81349bfd1f0b9586f37cdfe153c66123f5ef46c3412c39dac7f99356c82b1a`

~~~~java
package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.topflop.TopFlopFacts.FantasyFact;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.FantasyKey;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.RealDayKey;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.RosterSnapshot;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.Usage;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Read-only, season-scoped bridge from existing FCA/FCM readers to TopFlopArchive.
 * It preserves all Tesserato rows separately, even repeated registrations of the
 * same player in the same fantasy team. It never infers ownership dates, exclusions,
 * played minutes, scorecard URLs or legally counted substitutions.
 */
public final class TopFlopJdbcArchiveAssembler {
    private TopFlopJdbcArchiveAssembler() {}

    public record LoadedSeason(TopFlopArchive archive,
                               Map<Integer, TopFlopFcaJdbcReader.PlayerProfile> profiles,
                               List<TopFlopFcmJdbcReader.RosterLine> rosterEvents,
                               TopFlopFcaJdbcReader.SeasonLoad fca,
                               TopFlopFcmJdbcReader.SeasonLoad fcm,
                               int distinctRosterMemberships,
                               long numericTabellinoTotals,
                               long nonNumericTabellinoTotals) {
        public LoadedSeason {
            Objects.requireNonNull(archive);
            profiles = Map.copyOf(profiles);
            rosterEvents = List.copyOf(rosterEvents);
        }
    }

    private record RosterMembership(TopFlopFacts.PlayerSeason player, int teamId) {}

    public static LoadedSeason loadSeason(Connection connection, String seasonId)
            throws SQLException {
        Objects.requireNonNull(connection, "connection");
        Objects.requireNonNull(seasonId, "seasonId");
        TopFlopArchive archive = new TopFlopArchive();
        var fca = TopFlopFcaJdbcReader.loadSeason(connection, archive, seasonId, null);
        // Use the canonical 3.2 competition round, not source IDGIORNATA.
        var canonicalRounds = TopFlopCanonicalRoundResolver.loadSeason(connection, seasonId);
        List<TopFlopFcmJdbcReader.RosterLine> rosterEvents = new ArrayList<>();
        Set<RosterMembership> membershipKeys = new LinkedHashSet<>();
        long[] numericTotals = {0};
        long[] nonNumericTotals = {0};
        var fcm = TopFlopFcmJdbcReader.loadSeason(connection, seasonId,
            new TopFlopFcmJdbcReader.Sink() {
                @Override
                public void formation(TopFlopFcmJdbcReader.FormationLine line)
                        throws SQLException {
                    try {
                        RealDayKey realKey = new RealDayKey(line.player(), line.serieARound());
                        if (archive.realDay(realKey) == null) {
                            throw new SQLException("Missing FCA day for FCM formation: " +
                                seasonId + "/" + line.matchId() + "/" + line.player());
                        }
                        var canonicalRound = canonicalRounds.get(line.matchId());
                        if (canonicalRound != null &&
                            (canonicalRound.serieARound() != line.serieARound() ||
                             canonicalRound.sourceRoundId() != line.fcmRoundId() ||
                             canonicalRound.sourceCompetitionId() != line.competitionId())) {
                            throw new SQLException("Canonical 3.2 match metadata mismatch: " +
                                seasonId + "/" + line.matchId());
                        }
                        BigDecimal total = numericTabellino(line.tabellinoTotal());
                        if (total == null) nonNumericTotals[0]++;
                        else numericTotals[0]++;
                        Map<String, String> raw = new LinkedHashMap<>(line.sourceFields());
                        raw.put("FCM.Fantasquadra.Nome", line.fantasyTeamName());
                        raw.put("FCM.Formazione.Pos", Integer.toString(line.initialPosition()));
                        raw.put("FCM.Formazione.Entrato", Integer.toString(line.entered()));
                        raw.put("FCM.Formazione.EntratoRiserva", Integer.toString(line.enteredAsReserve()));
                        raw.put("FCM.Formazione.Manuale", Integer.toString(line.manual()));
                        raw.put("FCM.Tabellino.Slot", Integer.toString(line.tabellinoSlot()));
                        raw.put("FCM.Tabellino.FirstEleven", Boolean.toString(line.firstElevenSlot()));
                        raw.put("FCM.Tabellino.Voto", orNullMarker(line.tabellinoVote()));
                        raw.put("FCM.Tabellino.Modif", orNullMarker(line.tabellinoModifier()));
                        raw.put("FCM.Tabellino.Tot", orNullMarker(line.tabellinoTotal()));
                        raw.put("FCM.Incontro.Giocato", Integer.toString(line.matchPlayed()));
                        raw.put("FCM.Incontro.GiornataDiA", Integer.toString(line.serieARound()));
                        raw.put("FCM.Incontro.IDGiornata", Integer.toString(line.fcmRoundId()));
                        raw.put("FCM.Giornata.Desc", orNullMarker(line.fcmRoundDescription()));
                        raw.put("FCM.Incontro.IDGiornata", Integer.toString(line.fcmRoundId()));
                        raw.put("TopFlop.CompetitionRoundStatus",
                            canonicalRound == null ? "UNMAPPED_SOURCE_ONLY" : "CANONICAL_3_2");
                        // First-eleven tabellino slots are evidence, not proof of
                        // legally counted fantasy points in every historical rule.
                        Usage usage = line.firstElevenSlot() ? Usage.UNKNOWN :
                            (line.initialPosition() > 0 ? Usage.BENCH_UNUSED :
                            (line.initialPosition() < 0 ? Usage.OUTSIDE_LIST : Usage.UNKNOWN));
                        FantasyFact fact = new FantasyFact(
                            new FantasyKey(line.player(), line.competitionId(),
                                           line.matchId(), line.fantasyTeamId()),
                            realKey, line.competitionName(), line.family(),
                            canonicalRound == null ? null : canonicalRound.competitionRound(),
                            line.initialPosition() == 0,
                            usage, total, Map.of(), null, raw);
                        archive.addFantasyFact(fact);
                    } catch (IllegalArgumentException ex) {
                        throw new SQLException("Invalid TopFlop FCM fact: " + seasonId +
                            "/" + line.matchId() + "/" + line.player(), ex);
                    }
                }

                @Override
                public void roster(TopFlopFcmJdbcReader.RosterLine line)
                        throws SQLException {
                    rosterEvents.add(line); // All distinct source tesserato IDs retained.
                    RosterMembership key = new RosterMembership(line.player(), line.fantasyTeamId());
                    if (membershipKeys.add(key)) {
                        Map<String, String> raw = new LinkedHashMap<>(line.sourceFields());
                        raw.put("FCM.Tesserato.IDRappresentativo", Long.toString(line.tesseratoId()));
                        raw.put("FCM.Tesserato.AltriEventi", "Vedere LoadedSeason.rosterEvents");
                        archive.addRosterSnapshot(new RosterSnapshot(line.player(),
                                                   line.fantasyTeamId(), raw));
                    }
                }
            });
        if (archive.realDayCount() != fca.realDaysAdded() ||
            archive.fantasyFactCount() != fcm.allowedFormations() ||
            rosterEvents.size() != fcm.readTesserati() ||
            archive.rosterCount() != membershipKeys.size() ||
            numericTotals[0] + nonNumericTotals[0] != fcm.allowedFormations()) {
            throw new SQLException("TopFlop archive reconciliation failed: " + seasonId);
        }
        return new LoadedSeason(archive, fca.playersByLocalId(), rosterEvents,
            fca, fcm, membershipKeys.size(), numericTotals[0], nonNumericTotals[0]);
    }

    /** Null means SV/NG, missing or an unrecognized original token, never zero. */
    static BigDecimal numericTabellino(String original) {
        if (original == null) return null;
        String value = original.trim();
        if (!value.matches("[+-]?[0-9]+(?:[,.][0-9]+)?")) return null;
        return new BigDecimal(value.replace(',', '.'));
    }

    private static String orNullMarker(String original) {
        return original == null ? "__SQL_NULL__" : original;
    }
}
~~~~

## `src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopCanonicalRoundResolver.java`

SHA-256: `6aa60eb698519eb85f92b4166e1bc7010afd4214ac0a3b2888e0321eb9f64e3e`

~~~~java
package it.alterlega.recordsnext.app.topflop;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Resolve round/description from the EXISTING RecordsNext 3.2 canonical views.
 * A source FCM IDGIORNATA is never treated as a competition-round ordinal.
 * Unmapped competitions remain absent here; their raw FCM facts are still kept.
 */
public final class TopFlopCanonicalRoundResolver {
    private TopFlopCanonicalRoundResolver() {}

    public record Round(int competitionRound, String roundDescription,
                        int serieARound, int sourceRoundId, int sourceCompetitionId) {
        public Round {
            if (competitionRound <= 0 || serieARound <= 0 || sourceRoundId <= 0 || sourceCompetitionId <= 0)
                throw new IllegalArgumentException("Invalid canonical round values");
        }
    }

    public static Map<Long, Round> loadSeason(Connection connection, String seasonId)
            throws SQLException {
        Objects.requireNonNull(connection, "connection");
        if (seasonId == null || !seasonId.matches("[0-9]{4}_[0-9]{4}"))
            throw new IllegalArgumentException("Invalid seasonId: " + seasonId);
        Map<Long, Round> rounds = new LinkedHashMap<>();
        String sql = "SELECT source_event_id, competition_round, round_description, " +
            "serie_a_round, source_round_id, source_competition_id " +
            "FROM rn_event WHERE season_id = ?";
        try (PreparedStatement query = connection.prepareStatement(sql)) {
            query.setString(1, seasonId);
            try (ResultSet rows = query.executeQuery()) {
                while (rows.next()) {
                    long id = rows.getLong("source_event_id");
                    Round value = new Round(rows.getInt("competition_round"),
                        rows.getString("round_description"),
                        rows.getInt("serie_a_round"),
                        rows.getInt("source_round_id"),
                        rows.getInt("source_competition_id"));
                    if (rounds.putIfAbsent(id, value) != null)
                        throw new SQLException("Duplicate canonical event: " + seasonId + "/" + id);
                }
            }
        }
        return Map.copyOf(rounds);
    }
}
~~~~

## `src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopCanonicalRoundSmokeTest.java`

SHA-256: `a92f47c91192fcfcd1f9b8643ca1fa2db6228448af2d9d174a3590bbdf94cc6c`

~~~~java
package it.alterlega.recordsnext.app.topflop;

import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.Map;

/** Read-only integration smoke test against a 3.2 SQLite database. */
public final class TopFlopCanonicalRoundSmokeTest {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Database path required");
        Class.forName("org.sqlite.JDBC");
        try (var db = DriverManager.getConnection("jdbc:sqlite:" +
                 Path.of(args[0]).toAbsolutePath().toUri() + "?mode=ro")) {
            var m2026 = TopFlopCanonicalRoundResolver.loadSeason(db, "2026_2027");
            var real = m2026.get(33814L);
            if (real == null || real.competitionRound() != 5 ||
                real.serieARound() != 7 || real.sourceRoundId() != 2056)
                throw new AssertionError("Canonical 2026 round/real round/source ID mismatch: " + real);
            var m2022 = TopFlopCanonicalRoundResolver.loadSeason(db, "2022_2023");
            if (m2022.containsKey(26002L) || m2022.containsKey(26004L))
                throw new AssertionError("An explicitly excluded 2022 competition was treated as mapped");
            int seasons = 0;
            for (int y=2006;y<=2026;y++) {
                var season = y + "_" + (y+1);
                var rounds = TopFlopCanonicalRoundResolver.loadSeason(db, season);
                if (rounds.isEmpty()) throw new AssertionError("No canonical data for " + season);
                seasons++;
            }
            // Validate that TopFlop uses the mapped ordinal, while preserving the
            // unmapped 2022 FCM facts rather than faking a competition round.
            var bundle2026 = TopFlopJdbcArchiveAssembler.loadSeason(db, "2026_2027");
            boolean found2026 = false;
            for (var profile : bundle2026.profiles().values()) {
                for (var fact : bundle2026.archive().fantasyFactsFor(profile.player())) {
                    if (fact.key().matchId() == 33814L) {
                        if (!Integer.valueOf(5).equals(fact.fantasyRound()) ||
                            !"CANONICAL_3_2".equals(fact.sourceFields().get("TopFlop.CompetitionRoundStatus")))
                            throw new AssertionError("The assembler still uses the FCM source ID instead of competition round");
                        found2026 = true;
                    }
                }
                if (found2026) break;
            }
            if (!found2026) throw new AssertionError("No player for 2026 match 33814");
            var bundle2022 = TopFlopJdbcArchiveAssembler.loadSeason(db, "2022_2023");
            long unmapped = 0;
            for (var profile : bundle2022.profiles().values()) {
                for (var fact : bundle2022.archive().fantasyFactsFor(profile.player())) {
                    if (fact.key().matchId() == 26002L || fact.key().matchId() == 26004L) {
                        if (fact.fantasyRound() != null ||
                            !"UNMAPPED_SOURCE_ONLY".equals(fact.sourceFields().get("TopFlop.CompetitionRoundStatus")))
                            throw new AssertionError("Unmapped 2022 round was fabricated");
                        unmapped++;
                    }
                }
            }
            if (unmapped != 100) throw new AssertionError("Expected 100 preserved unmapped fantasy contexts, got " + unmapped);
            System.out.println("TOP_FLOP_CANONICAL_ROUNDS_OK seasons=" + seasons +
                " 2026Round=" + real.competitionRound() + " serieARound=" + real.serieARound() +
                " sourceId=" + real.sourceRoundId() + " unmapped2022Facts=" + unmapped);
        }
    }
}
~~~~

## `docs/TOP_FLOP_CANONICAL_ROUNDS_PATCH_v1.md`

SHA-256: `40cb971125555044149f75449ca95302e40d93546ef685932dfb2b9ec85bd4c6`

~~~~markdown
# RecordsNext 4.0 - Canonical rounds patch v1 (2026-11-06)

## Scope
- Base: original 3.2 Java pipeline and `rn_event`; no changes to pipeline, publisher, CSS, GUI, site or DB.
- Fixed: `TopFlopJdbcArchiveAssembler` previously used FCM `IDGIORNATA` as a competition round; now uses `rn_event.competition_round`, and the original `Giornata.DESC` is retained for display.
- `FantasyFact.fantasyRound` is nullable for a competition explicitly EXCLUDED from the canonical 3.2 mapping; source FCM ID is retained in `sourceFields` but must never be displayed as a round.
- `TopFlop.CompetitionRoundStatus`: `CANONICAL_3_2` or `UNMAPPED_SOURCE_ONLY`.

## Concrete 3.2 data anomaly
- 21 seasons, 559675 total Formazione rows in the updated SQLite (552025 in the older pre-update snapshot).
- 5350 `Formazione` rows lack an `rn_event` match: 5250 Youth League, correctly excluded, and **100 in 2022/23**, 50 Spareggio Serie B and 50 Vera finale di Coppa tra le Coppe. The last two have `mapping_status=ESCLUSA` in 3.2 but Top & Flop policy includes these competitions. We preserve all 100 as source facts with unknown competition ordinal; no fabricated link or round.
- Example 2026/27, encounter 33814: competition round 5, Serie A day 7, source FCM round ID 2056; original description `5a Camp. (1a Andata)`.

## Installation / next work
- Java replacements are only permitted after checking exact source SHA256; installer protects previous versions under `backups/topflop-canonical-rounds-v1`.
- Do not enable Top & Flop in the catalog, GUI or publisher yet. Next: native aggregator/rankings with usage explicitly proven; integration into existing 3.2 exporter, shards, manifest and HTML with skin, not a parallel pipeline.
- This patch makes round provenance explicit, it does not complete Top & Flop 4.0.0.
~~~~

## `docs/PIANO_RECORDSNEXT4_GIOCATORI_v22.md`

SHA-256: `260189cd1fbc42fdda0405dbb1475c0235369bb041e942369d4803bf539fe758`

~~~~markdown
# Piano v20 — Demo Top & Flop e compatibilità storica


**Stato 2026-11-02.** Implementata una demo reale ma circoscritta a 2006/07 e 2026/27. Conservare pipeline e publisher 3.2; l'integrazione nativa non è ancora eseguita. Vedere `SPECIFICA_DEMO_TOP_FLOP_RECORDSNEXT4_v1.md`.

**Via alla conclusione:** disponibili HTML, CSS 3.2 con scelta skin, facade e shard campione; prossime tappe: stato tesseramento giornaliero, metriche, catalogo, exporter nativo, manifest/GUI, demo finale e deploy di test. Nessun rilascio 4.0 finché gli shard completi all-time non sono pubblicabili con la 3.2.

---

# RecordsNext 4.0 - Piano di continuita v19

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


## Fase 4A - Assemblatore Java FCA + FCM (2026-11-02)

- Da `TopFlopFcaJdbcReader.loadSeason` creare l'unico archivio `RealDay` per calciatore/giornata FCA; poi collegare `TopFlopFcmJdbcReader.loadSeason` tramite lo stesso `PlayerSeason` e `RealDayKey`, mantenendo i contesti fantasy per team/competizione/incontro senza duplicare i dati reali.
- Non sostituire gli ID FCA annuali con CodiceGazza durante le JOIN; CodiceGazza e' un riferimento storico da verificare in caso di collisioni.
- `Tesserato` puo' avere piu righe per lo stesso giocatore/team/stagione (es. venduto e successivamente riacquistato). Conservare **tutte** le righe con l'ID sorgente nella vista `LoadedSeason.rosterEvents`, oltre alla vista riepilogativa univoca di `TopFlopArchive`. Non datare il tesseramento per inferenza.
- Il `Tabellino.Tot` inizia a essere disponibile come valore numerico solo quando il token originale e' numerico (virgola o punto); `sv/ng`, null e testi speciali rimangono senza fantavoto numerico, con stringa originale intatta.
- `Formazione.POS=0` registra il titolare iniziale, ma gli undici slot del tabellino non costituiscono una certificazione indipendente di contribuzione regolamentare; nessun `EXCLUDED_CONFIRMED` prodotto in automatico.
- I link scorecard rimangono null finche non sono ricavati dal normalizzatore/publisher della 3.2, preservando la corretta stagione storica.
- Le Riserve d'Ufficio, i coefficienti bonus/malus e la validita temporale dei tesseramenti non vengono simulati. Youth League continua a essere esclusa.
- **Sviluppo non abilitato** nel catalogo record, pipeline FULL/CONSOLIDATED, GUI o publisher. Prima completare e verificare la costruzione canonica e l'utilizzo dati su un'intera stagione, poi integrare nella famiglia esistente.
- Test pilota: 2006/07 (riacquisti), 2023/24 (Youth), 2025/26 (Lautaro in squadre multiple), 2026/27 (stagione senza formazioni) e ulteriori stagioni tramite test selettivi. Controlli per entrambi i DB con conteggi relativi alla sorgente, non valori assoluti fissati per 2026/27.


## Precisazioni snapshot aggiornato 2026/27 (2026-11-02)

- Serie A: 9 giornate concluse; AlterLega: giornate REALI 3-9, confermato dall'utente. Nessuna partita fantasy attesa nelle prime due giornate.
- Integrato test dello snapshot RAW 3.2 con 28.956 righe FCA, 7.650 formazioni FCM, 813 tesseramenti.
- La cronologia dei movimenti `EntryInBilancio` e' fonte da conservare, ma non e' ancora una tabella di finestre di possesso dimostrate.
- Pacchetto fase 4A = solo assemblatore read-only e test; disabilitata famiglia nella GUI/publisher finche' mancano record e output nativi.


---

## Addendum correttivo demo v2 — 2 novembre 2026

Installazione della demo Top & Flop con skin originali riutilizzate. Prima prova Windows v1 arrestata preventivamente sul CSS neutral diverso. Runner v2: non altera i CSS delle quattro skin esistenti, installa quelli assenti, verifica gli hash; documenta v20 della bibbia. Demo ancora limitata a due stagioni e non pubblicabile.

## Aggiornamento v22 — Giornata competizione canonica (2026-11-06)

- Database aggiornato 2026/27: **559.675** righe Formazione complessive nelle 21 stagioni; le **552.025** precedenti si riferivano allo snapshot antecedente l’importazione di 7.650 formazioni 2026/27.

- **Implementato e testato su copia:** `TopFlopCanonicalRoundResolver` legge `rn_event.competition_round` e non `FCM.Incontro.IDGIORNATA` come numero di giornata; l'assemblatore conserva sia la descrizione originaria FCM sia l'ID sorgente tecnico in campi separati.
- `FantasyFact.fantasyRound` può essere `null` quando non esiste un incontro nella mappatura canonica della 3.2. Nessun numero inventato e nessun contesto FCM scartato per questo motivo.
- **Eccezione verificata:** stagione 2022/23, 50 formazioni in Spareggio Serie B e 50 in Vera finale di Coppa tra le Coppe: 3.2 le marca `ESCLUSA`; Top & Flop le conserva come fonte, con giornata canonica non disponibile. I 5.250 contesti Youth League rimangono esclusi.
- Test: compilazione Java 21 + 52 smoke checks + lettura di 21 stagioni `rn_event` + assemblaggio 2022/23 e 2026/27 aggiornati.
- **Ancora da completare:** motore record nativo, calcolo apporto effettivo e tesseramenti giornalieri, JS integrati nel publisher 3.2, HTML finale con scheda giocatore e skin Renewo, GUI, verifica multistagione di tutti gli output e rilascio 4.0.0. Nessuna pubblicazione automatica.
~~~~

## `docs/REGISTRO_DECISIONI_RECORDSNEXT4_v21.md`

SHA-256: `1346fac2b36d6cf1d8ab575b1992784c1ab63e6d0ecb479efec1c0f83de00550`

~~~~markdown
# Registro decisioni v19 — Demo integrativa


| ID | Stato | Decisione |
|---|---|---|
| D-158 | APPROVATA | Records Top & Flop è famiglia **nativa** dei records RecordsNext; stesso motore e stesso publisher 3.2, non secondo programma. |
| D-159 | APPROVATA | HTML demo con selezione skin e layout RecordsNext; profilo neutral ispirato a ReNewo, oltre alle altre skin 3.2. |
| D-160 | APPROVATA | Dati JS nella `js` del sito, con facade, shard flat per stagione, cutoff storico, rollback e link canonici; nessun URL inventato. |
| D-161 | APPROVATA | Varela: se fra cessione e reinserimento non passa una giornata di reale assenza dalla rosa, appartenenza continua. |
| D-162 | IMPLEMENTATA SOLO DEMO | Preview 2006/07 + 2026/27, nomi JS e limiti coerenti 3.2; nessuna abilitazione catalogo, GUI o publisher. |
| D-163 | APERTA | Strategie per shard di stagioni oltre 1536 KiB: non accettare troncamenti; usare estensione controllata publisher. |
| D-164 | APERTA | Calcolo esclusioni certe e loro quattro metriche: nessuna inferenza da sola mancanza in formazione o tesseramento snapshot. |

---

# RecordsNext 4.0 - Registro decisioni v18

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


## Decisioni D-140--D-147 - Assemblaggio nativo per stagione

| ID | Stato | Decisione / verifica |
|---|---|---|
| D-140 | IMPLEMENTATA IN FASE 4A | Assemblare fatti FCA e FCM nella stessa istanza `TopFlopArchive`, partendo da FCA e collegando ogni formazione mediante ID locale e giornata reali. |
| D-141 | VERIFICATA SU FONTE | `Tesserato` presenta righe ripetute per stessa fantasquadra/giocatore: vendite e successivi riacquisti possibili. Chiave distinta dell'evento: `Tesserato.ID` per stagione; non usare solo giocatore+squadra per conservare la cronologia sorgente. |
| D-142 | IMPLEMENTATA IN FASE 4A | `LoadedSeason.rosterEvents` conserva tutte le righe originali e i rispettivi ID; `TopFlopArchive.rosters` rimane vista sintetica di membership. Non inferire intervalli di appartenenza. |
| D-143 | IMPLEMENTATA IN FASE 4A | I token di voto/fantavoto dei tabellini FCM restano nei campi sorgente; convertire in BigDecimal solo i numerici con virgola/punto, senza convertire `sv/ng` in zero. |
| D-144 | REGOLA CONSERVATIVA | Gli slot del tabellino registrano evidenza ma non certificano utilizzo legalmente conteggiato: non produrre automaticamente `EXCLUDED_CONFIRMED` e non gonfiare i record. |
| D-145 | REGOLA CONSERVATIVA | `scorecardUrl` rimane null durante l'assemblaggio JDBC; risolverlo esclusivamente con le logiche normalizzate 3.2, al momento dell'integrazione nella pipeline. |
| D-146 | DA VERIFICARE | La provenienza della fantamedia per giocatori fuori da Formazione, l'esclusione certa durante un tesseramento storico e le regole RU/sostituzione sono ancora aperte. |
| D-147 | REGOLA DI REGRESSIONE | La nuova classe e i test non modificano pipeline, catalogo, GUI, sito o DB; testare gli hash e mantenere separati dati reali e contesti fantasy. |


## D-148--D-157 - Conferme 2026/27 e cronologia mercato

| ID | Stato | Regola o evidenza |
|---|---|---|
| D-148 | CONFERMATA DALL'UTENTE | 9 giornate reali di Serie A disponibili nello snapshot 2026/27. |
| D-149 | CONFERMATA DALL'UTENTE | La AlterLega 2026/27 ha cominciato dalla giornata reale 3; assenza di incontri nelle giornate 1 e 2 NON e' errore. |
| D-150 | IPOTESI DELL'UTENTE - DA VERIFICARE | La cronologia dei tesseramenti potrebbe essere legata a giornate della Serie A vera. Non assumere che `ULTIMAGIORNATA` sia la prima giornata eleggibile. |
| D-151 | FONTE VERIFICATA | `Tesserato` non contiene date di validita'. `EntryInBilancio` contiene `DATATRANSAZIONE` e `ULTIMAGIORNATA` e deve restare distinto dalla membership. |
| D-152 | CONFERMATA DALL'UTENTE | Calciatore ceduto e riacquistato in un'asta successiva e' operazione normale; riacquisti nella stessa sessione possono essere correzioni. Le sole date non provano la sessione. |
| D-153 | CONFERMATA DALL'UTENTE | Varela/FortisSIMY 2026/27 e' una correzione da svincolo accidentale; preservare tesseramenti 699 e 752 e relativi movimenti contabili. |
| D-154 | VERIFICATA: SNAPSHOT IMPORTATO 3.2 | 2026/27 aggiornato: 28956 righe reali FCA, 7650 `Formazione`, 813 `Tesserato`, 876 `EntryInBilancio`; 3..9 giornate reali con formazioni fantasy. |
| D-155 | REGOLA IMPLEMENTATIVA | Il nuovo assemblatore conserva tutte le righe `Tesserato` con ID sorgente e usa vista membership sintetica; nessuna esclusione senza prova di tesseramento in quel giorno. |
| D-156 | TEST AGGIUNTIVO | Verifica specifica sul DB 2026/27 aggiornato, compreso Varela e l'inizio della fantasy alla giornata 3. I dati di output test non sono regole eterne per altre stagioni. |
| D-157 | RIMANE APERTO | Non integrare automaticamente la datazione del possesso e il dettaglio `EntryInBilancio` in record pubblico fino a chiarimento dei campi originali FCM. |


---

## Addendum correttivo demo v2 — 2 novembre 2026

**D-158 (approvazione tecnica conservativa):** i quattro `release/visualizzatori/profiles/*/recordsnext.css` della 3.2, eventualmente modificati nel worktree utente, sono la fonte autoritativa di stile. La demo non li sovrascrive e deve utilizzarli dal loro percorso originale. Un CSS mancante puo essere installato dal kit come fallback. Ogni differenza viene registrata nel report; non richiede convergenza dell'hash con la copia del kit. Nessuna modifica al sito operativo. **Stato:** implementato nel runner v2, verifica Windows da completare.

## Decisioni e anomalie — 2026-11-06

| ID | Stato | Decisione / evidenza |
|---|---|---|
| D-158 | CONFERMATA DAL CODICE 3.2 | La giornata di competizione è `rn_event.competition_round`; `IDGIORNATA` è ID tecnico FCM e non deve essere mostrato come giornata. |
| D-159 | CONFERMATA | Conservare `FCM.Giornata.DESC` e `FCM.Incontro.IDGIORNATA` separatamente, senza normalizzare arbitrariamente etichette speciali. |
| D-160 | VINCOLANTE | Se `rn_event` non contiene una partita, il numero di giornata canonico resta nullo. Conservare la partita FCM, la descrizione e il motivo `UNMAPPED_SOURCE_ONLY`. |
| D-161 | ANOMALIA VERIFICATA, NON CORRETTA | Nel 2022/23 il mapping 3.2 marca `ESCLUSA` Spareggio Serie B (50 formazioni) e Vera finale di Coppa tra le Coppe (50): esiste un conflitto con il perimetro Top & Flop approvato. Non modificare il mapping 3.2 automaticamente. |
| D-162 | CONFERMATA | Le 5250 formazioni Youth League del 2023/24 rimangono escluse da Top & Flop, come richiesto dall'utente. |
| D-163 | STATO | Patch limitata al modello/assemblatore Java; non abilita la famiglia nel catalogo, pipeline, GUI, publisher o skin. |
~~~~

## `docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v21.md`

SHA-256: `1f2b1ad6d721371168ac734bb24542a834268f5894f66e5aac8ff642f3637c2f`

~~~~markdown
# Diario sviluppo v19 — Demo HTML e JS


2026-11-02: consultata direttamente la struttura `release/visualizzatori` della 3.2 con i profili CSS; esaminati `SeasonFamilyShardPublisher`, `MatchesJsExporter`, normalizzazione URL e struttura SQLite aggiornata. Implementato **solo kit demo**: HTML Top & Flop, viewer JS, facade e due shard reali. Restano inalterati catalogue Java, manifest, pipeline, GUI, database e siti. Il generatore Python è uno strumento preview read-only e **NON** il publisher nativo. JS e hash verificati localmente, prova browser Windows ancora richiesta.

---

# RecordsNext 4.0 - Diario di sviluppo v18

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


## 2026-11-02 - Collegamento Java FCA/FCM fase 4A

- Verificato lo ZIP Windows `RecordsNext4_FcmJdbc_OUTPUT_v1_20261102_143437_776784.zip`: manifest `OK`, compilazione e regressioni 52/FCA/FCM superate, hash di entrambi i DB invariati, bibbia v17 generata.
- Rilevati e verificati tesseramenti con stessa coppia giocatore+fantasquadra ma ID `Tesserato` differenti; il modello sintetico precedente non era sufficiente a conservare gli eventi sorgente.
- Aggiunta la classe additiva `TopFlopJdbcArchiveAssembler.java`: costruisce `TopFlopArchive` stagionale dagli adapter esistenti e restituisce separatamente tutti gli eventi Tesserato.
- Test con JDBC SQLite in sola lettura, con riconciliazione di giorni reali, formazioni ammesse, Youth esclusa, tesseramenti sorgente e membership univoche; nessuna inferenza di esclusioni, URL, bonus o malus.
- Stato: test pilota locali superati. Il runner Windows produce ZIP diagnostico e bibbia v18; verifica finale Windows attesa.


## 2026-11-02 - Nuovi FCA/FCM e validazione stagione 2026/27

- Ricevuti FCM/FCA aggiornati e DB di prova importato con importatore RAW originale RecordsNext 3.2; validati con `integrity_check`.
- L'utente conferma: 9 giornate reali, AlterLega iniziata alla terza.
- Varela/FortisSIMY: due ID tesseramento 699/752 per svincolo accidentale e reinserimento; mantiene singola identita' reale.
- Audit su `EntryInBilancio` con date e `ULTIMAGIORNATA`, significato preciso ancora non certificato.
- Aggiunto test aggiornato `TopFlopJdbcArchive2026SmokeTest` al kit dell'assemblatore.
- Test Windows e installazione in worktree 4.0 da effettuare; nessuna modifica al database operativo 3.2.


---

## Addendum correttivo demo v2 — 2 novembre 2026

**2 novembre 2026:** il runner della demo v1 si e interrotto nel preflight, rilevando un CSS neutral esistente con hash diverso. Nessun file era stato copiato. Runner v2 sviluppato per riutilizzare CSS preesistenti senza sovrascriverli, mantenendo la verifica stringente per tutti gli altri file e la generazione di bibbia v19 e v20. Prova Linux ripetuta in worktree con CSS diversi; prova PowerShell/Windows ancora da confermare.

## 2026-11-06 — Patch canonica giornate Top & Flop v1

Esaminato lo ZIP `RecordsNext4_SORGENTI_ATTUALI_20261106.zip` (codice e documentazione correnti). Confrontate `CanonicalViews`, `TopFlopFcmJdbcReader`, `TopFlopFacts`, `TopFlopJdbcArchiveAssembler` e le viste SQLite 3.2 del database aggiornato. Rilevato uso errato di IDGIORNATA come fantasyRound; introdotto resolver canonico con ordinamento della 3.2 e fallback nullo per mapping esclusi. Nessun aggiornamento di RAW, mapping, database operativo, GUI, CSS o pubblicazione. Test su copia e installazione conservativa versionata. Prossimo step motore Top & Flop + JS nativi, usando gli altri publisher esistenti.

- Conteggi aggiornati: 559675 Formazione nell’SQLite con 2026/27 importato (contro 552025 nel precedente snapshot).
~~~~

## `tools/Invoke-RecordsNext4CanonicalRounds-v1.py`

SHA-256: `1181bf49b95399f5aecdebe95aea865e8a226f84cc6689bc5a932e74ba240fa5`

~~~~python
#!/usr/bin/env python3
"""Safe, idempotent RecordsNext 4.0 Top & Flop canonical-round patch."""
from __future__ import annotations
import argparse
import datetime
import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys
import traceback
import zipfile

KIT = Path(__file__).resolve().parent.parent
PATCH = [
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopFacts.java',
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopJdbcArchiveAssembler.java',
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopCanonicalRoundResolver.java',
 'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopCanonicalRoundSmokeTest.java',
 'docs/TOP_FLOP_CANONICAL_ROUNDS_PATCH_v1.md',
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v22.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v21.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v21.md',
 'tools/Create-RecordsNext4WorkingCodeMd-v21.py',
 'tools/Invoke-RecordsNext4CanonicalRounds-v1.py',
]
BASE = 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v20.md'
BACKUP = 'backups/topflop-canonical-rounds-v1'

def sha(p: Path) -> str: return hashlib.sha256(p.read_bytes()).hexdigest()

def run(command, log):
    r = subprocess.run([str(x) for x in command], text=True, encoding='utf-8', errors='replace',
                       stdout=subprocess.PIPE, stderr=subprocess.STDOUT, check=False)
    log.append('COMMAND: ' + ' '.join(Path(str(x)).name if i>3 else str(x) for i,x in enumerate(command[:5])))
    log.append(r.stdout[-20000:])
    if r.returncode != 0: raise RuntimeError('Command failed with code ' + str(r.returncode) + ':\n' + r.stdout[-4500:])
    return r.stdout

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--project-dir', default=r'D:\DEV_APPS\RecordsNext4.0')
    ap.add_argument('--download-dir', default=r'D:\DEV_APPS\downloads')
    ap.add_argument('--database', default=None)
    ap.add_argument('--jar', default=None)
    a = ap.parse_args()
    root=Path(a.project_dir).resolve()
    downloads=Path(a.download_dir).resolve()
    downloads.mkdir(parents=True,exist_ok=True)
    stamp=datetime.datetime.now().strftime('%Y%m%d_%H%M%S_%f')
    out=downloads/('RecordsNext4_CanonicalRounds_OUTPUT_v1_'+stamp+'.zip')
    logs=[]; state={'status':'ERROR','database':None,'project':str(root),'checks':[],'installed':[],'planned':[],'identical':[]}
    build=downloads/('RecordsNext4_CanonicalRounds_BUILD_v1_'+stamp)
    changed=[]
    originals={}
    try:
        if not (root/'pom.xml').is_file() or not (root/BASE).is_file():
            raise RuntimeError('Not the expected RecordsNext4.0 worktree or missing bible v20')
        manifest=json.loads((KIT/'manifest-sha256.json').read_text(encoding='utf-8'))
        for rel in PATCH:
            source=KIT/rel; target=root/rel
            if not source.is_file():raise RuntimeError('Missing patch file: '+rel)
            if target.exists():
                if not target.is_file():raise RuntimeError('Destination is not a file: '+rel)
                if sha(target)!=sha(source):
                    if rel in manifest['expected_existing'] and sha(target)==manifest['expected_existing'][rel]:
                        originals[rel]=target.read_bytes()
                    else: raise RuntimeError('Existing file differs, safe install stopped: '+rel)
                else:state['identical'].append(rel)
            else:state['planned'].append(rel)
        for rel in originals:state['planned'].append(rel)
        # Compile patched Java in isolation, BEFORE touching the project.
        classes=build/'classes'; classes.mkdir(parents=True,exist_ok=True)
        replacement={x for x in manifest['expected_existing']} | {'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopCanonicalRoundResolver.java'}
        source_files=[x for x in (root/'src/main/java').rglob('*.java')
                      if x.relative_to(root).as_posix() not in replacement]
        for rel in manifest['expected_existing']:
            source_files.append(KIT/rel)
        source_files.append(KIT/'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopCanonicalRoundResolver.java')
        java=shutil.which('java');javac=shutil.which('javac')
        if not java or not javac:raise RuntimeError('JDK 21+ not found in PATH')
        run([javac,'--release','21','-encoding','UTF-8','-d',classes,*source_files],logs)
        state['checks'].append('Java 21 full source compilation OK')
        tests=[root/'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopArchiveSmokeTest.java',
               KIT/'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopCanonicalRoundSmokeTest.java']
        run([javac,'--release','21','-encoding','UTF-8','-cp',classes,'-d',classes,*tests],logs)
        main_test='it.alterlega.recordsnext.app.topflop.TopFlopArchiveSmokeTest'
        testout=run([java,'-ea','-cp',classes,main_test],logs)
        if 'checks=52' not in testout:raise RuntimeError('52-check regression signature absent')
        state['checks'].append('52 Java regression checks OK')
        jar=Path(a.jar) if a.jar else next((x for x in [root/'target/RecordsNext.jar',
                root.parent/'RecordsNext2.0/target/RecordsNext.jar',
                root.parent/'RecordsNext2.0/release/RecordsNext_3.2.0_FULL/payload/RecordsNext.jar'] if x.is_file()),None)
        db=Path(a.database) if a.database else next(iter(sorted((root/'data/database').glob('recordsnext-2026_2027-verifica-v1_*.db'))),None)
        if jar and db and jar.is_file() and db.is_file():
            test='it.alterlega.recordsnext.app.topflop.TopFlopCanonicalRoundSmokeTest'
            o=run([java,'-ea','-cp',str(classes)+os.pathsep+str(jar),test,db],logs)
            if 'unmapped2022Facts=100' not in o:
                raise RuntimeError('2022 unmapped-competition regression signature absent')
            state['checks'].append('21 seasons and 2022/2026 canonical matches OK')
            state['database']=str(db)
        else:
            state['checks'].append('JDBC sample skipped (updated SQLite or shaded 3.2 JAR not found)')
        # All preflight checks passed. Keep full original bytes before any replacement.
        for rel,data in originals.items():
            back=root/BACKUP/rel
            back.parent.mkdir(parents=True,exist_ok=True)
            if back.exists() and back.read_bytes()!=data:
                raise RuntimeError('Conflicting backup, nothing replaced: '+str(back))
            if not back.exists(): back.write_bytes(data)
        bible_preexisting=(root/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v21.md').exists()
        try:
            for rel in PATCH:
                target=root/rel
                if target.is_file() and sha(target)==sha(KIT/rel):continue
                target.parent.mkdir(parents=True,exist_ok=True)
                target.write_bytes((KIT/rel).read_bytes())
                changed.append(rel)
            state['installed']=changed.copy()
            generator=root/'tools/Create-RecordsNext4WorkingCodeMd-v21.py'
            run([sys.executable,generator,'--project-dir',root],logs)
            state['checks'].append('Versioned bible v21 generated')
        except Exception:
            for rel in reversed(changed):
                target=root/rel
                if rel in originals:target.write_bytes(originals[rel])
                else:target.unlink(missing_ok=True)
            if not bible_preexisting:
                (root/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v21.md').unlink(missing_ok=True)
            raise
        state['status']='OK'
        print('ESITO: OK')
        for label in state['checks']:print('VERIFICA:',label)
        print('FILE INSTALLATI/AGGIORNATI:',len(state['installed']))
        print('FILE GIA IDENTICI:',len(state['identical']))
        print('BIBBIA: docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v21.md')
    except Exception as e:
        state['error']=repr(e)
        logs.append(traceback.format_exc())
        print('ESITO: ERRORE\n'+str(e),file=sys.stderr)
    finally:
        with zipfile.ZipFile(out,'w',zipfile.ZIP_DEFLATED) as z:
            z.writestr('result.json',json.dumps(state,ensure_ascii=False,indent=2))
            z.writestr('execution.txt','\n'.join(logs))
            z.write(KIT/'docs/TOP_FLOP_CANONICAL_ROUNDS_PATCH_v1.md','specification.md')
        shutil.rmtree(build,ignore_errors=True)
        print('ZIP DA CARICARE:',out)
    return 0 if state['status']=='OK' else 1

if __name__=='__main__':sys.exit(main())
~~~~

## `tools/Create-RecordsNext4WorkingCodeMd-v21.py`

SHA-256: `76d6417d81299340729fb163f667f2e0b3e4bd405143d178c40b11089861e40b`

~~~~python
#!/usr/bin/env python3
"""RecordsNext4 native TopFlop patch, append-only code bible v21."""
from pathlib import Path
import argparse, hashlib, subprocess
PREVIOUS='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v20.md'
DEST='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v21.md'
FILES=[
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopFacts.java',
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopJdbcArchiveAssembler.java',
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopCanonicalRoundResolver.java',
 'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopCanonicalRoundSmokeTest.java',
 'docs/TOP_FLOP_CANONICAL_ROUNDS_PATCH_v1.md',
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v22.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v21.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v21.md',
 'tools/Invoke-RecordsNext4CanonicalRounds-v1.py',
 'tools/Create-RecordsNext4WorkingCodeMd-v21.py'
]
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
 ap=argparse.ArgumentParser(); ap.add_argument('--project-dir',required=True); args=ap.parse_args()
 root=Path(args.project_dir).resolve()
 for n in [PREVIOUS,*FILES]:
  if not (root/n).is_file():raise RuntimeError('Missing bible input: '+n)
 lines=['# RecordsNext 4.0 - Bibbia v21 - round canonico Top & Flop','','Base invariata: RecordsNext 3.2.0. Non si abilita la famiglia Top & Flop alla pubblicazione.','',
  'Previous bible: `'+PREVIOUS+'`', 'Previous SHA-256: `'+sha(root/PREVIOUS)+'`','']
 for n in FILES:
  f=root/n;lang={'java':'java','py':'python','md':'markdown'}.get(f.suffix.lstrip('.'),'text')
  lines+=['## `'+n+'`','', 'SHA-256: `'+sha(f)+'`','','~~~~'+lang,f.read_text(encoding='utf-8-sig').rstrip(),'~~~~','']
 content='\n'.join(lines).rstrip()+'\n'
 dest=root/DEST
 if dest.exists():
  if dest.read_text(encoding='utf-8')!=content:raise RuntimeError('Existing bible v21 differs: '+str(dest))
  print('BIBBIA v21 GIA PRESENTE IDENTICA')
 else:
  with dest.open('x',encoding='utf-8') as h:h.write(content)
  print('BIBBIA v21 GENERATA',dest)
if __name__=='__main__':main()
~~~~
