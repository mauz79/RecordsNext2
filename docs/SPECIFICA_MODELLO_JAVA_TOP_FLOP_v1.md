# RecordsNext 4.0 - Records Top & Flop - Modello Java v1

Data: 2026-11-01. Prima implementazione del **modello di dominio nativo Java**; ancora NON connessa a configurazione, catalogo, pipeline, exporter, GUI, publisher o siti.

## Provenienza e compatibilita 3.2

Progetto di partenza: `RecordsNext2.0.zip`, commit `f9bf30323d9204d70e91e242bddfc72c2060ecf9`, Java 21. Nessuna classe esistente alterata: sono aggiunti file in `app/topflop`. Il resto di `src/main/java` viene compilato **insieme** ai nuovi sorgenti per controllarne la compatibilita, senza cambiare il ciclo della 3.2.

## Chiavi e fatti

- `PlayerSeason(seasonId, fcaPlayerId, gazzaCode)`: identificativo **locale all'FCA** e codice storico Gazzetta separati. `gazzaCode` puo essere null se non certo. Non si fondono automaticamente annate diverse basandosi sul solo codice: le collisioni storiche restano da verificare.
- `RealDayKey(PlayerSeason, serieARound)`: una prestazione reale per calciatore stagionale/giornata di Serie A. `RealDay` contiene `VoteState`, voto, eventi fonte e `sourceFields`. `NOT_PLAYED`, `NO_VOTE`, `SPECIAL`, `UNKNOWN` NON sono trasformati in voto 0.
- `RosterSnapshot`: fotografia FCM di un tesseramento con fantasquadra e campi originali; **non** dimostra disponibilita per tutte le giornate e non crea automaticamente esclusioni.
- `FantasyKey(PlayerSeason, sourceCompetitionId, matchId, fantasyTeamId)`: identificativo univoco del contesto FCM, non solo giornata o giocatore.
- `FantasyFact`: partita e giornata sportiva, stato (`COUNTED`, `BENCH_UNUSED`, `OUTSIDE_LIST`, `EXCLUDED_CONFIRMED`, `UNKNOWN`), titolarita iniziale indipendente, fantavoto di quel **contesto**, bonus/malus, campi originari, link al tabellino **solo** se gia fornito dalle logiche 3.2. URL sconosciuta = null. Diversi incontri, coppe e squadre possono utilizzare la medesima `RealDay` senza duplicarla.
- `TopFlopArchive`: raccolta in memoria con chiavi e validazione di duplicati incompatibili. Inserimento ripetuto identico e idempotente. Non persiste ne pubblica dati.

I campi `sourceFields` hanno funzione di conservazione progressiva e non costituiscono ancora una importazione integrale dei campi FCM/FCA: l'adattatore/estrattore Java e il collegamento alle tabelle originali sono il **prossimo intervento**.

## Famiglie di competizioni e esclusioni

`TopFlopCompetitionPolicy` codifica **esplicitamente** le 24 denominazioni ammesse emerse nell'audit 3C1 (180 configurazioni in 21 stagioni); la venticinquesima, **Youth League**, e categoricamente esclusa. `Play Off - Play Out` e `Spareggio Serie B` sono `CAMPIONATO`. Altri campionati e coppe mantengono la propria denominazione e il proprio ID FCM stagionale. Una competizione nuova/sconosciuta produce errore (fail-closed), non una classificazione indovinata. Questa famiglia sportiva e distinta dalla futura famiglia record `TOP_FLOP`.

## Metriche individuali gia implementate sul modello

`TopFlopMetrics.summarize` richiede **giocatore stagionale, fantasquadra, famiglia sportiva (oppure tutte), stato d'utilizzo**. Per ogni aggregazione conserva `E` contesti, `P_voto` e `P_fantavoto` validi separati, `SV` e `SF` e calcola:

- `mediaVoto = SV/P_voto` e `indicePesatoVoto = SV/E`;
- `fantamedia = SF/P_fantavoto` e `indicePesatoFantavoto = SF/E`.

Le medie con denominatore zero sono **null** (non 0). Una prestazione senza voto nel contesto accertato contribuisce al denominatore `E`, ma non a `SV`/`SF`. `fantasyVote` null indica valore non conosciuto, mai una conversione implicita del voto reale in fantavoto. I valori originali restano `BigDecimal`, divisione con precisione `DECIMAL64`. Il campo `Usage.EXCLUDED_CONFIRMED` non va assegnato a dati FCM **senza evidenza**: `BENCH_UNUSED` e `OUTSIDE_LIST` rimangono stati distinti.

La funzione non impone soglie massime o minime, non calcola controfattuali della fantasquadra e non applica sostituzioni 3/5/11 o RU: sono regole della fonte da preservare, non da simulare nel modello canonico.

## Verifica

- Test sintetico Java 21 eseguibile in console con assert: **52 verifiche**, inclusi voto 5.5/7/8, 5 esclusioni e 3 voti, fantavoti con bonus/malus, piu fantasquadre, campionato/coppa nello stesso turno, tutte le 24 denominazioni ammesse, Youth League, mancato collegamento reale, collisioni, rosters.
- Compilazione di **tutti** i sorgenti `src/main/java` della 3.2 assieme ai nuovi file riuscita in ambiente di verifica con `javac 21.0.12.1`. Non sono stati eseguiti i test JUnit della 3.2 nell'ambiente di verifica, perche Maven non e installato. Il wrapper `TopFlopArchiveJUnitTest` rende il test disponibile anche a JUnit 5 quando Maven e disponibile sul PC.
- Il runner sul PC compila i sorgenti in una cartella diagnostica sotto `D:\DEV_APPS\downloads`, esegue i 52 assert, conserva i log, genera bibbia v15 e ZIP. Cancella la sola cartella temporanea da esso creata.

## Non implementato in questa fase

Lettura JDBC integrale dei dati individuali e caricamento `RealDay`/`FantasyFact` dai raw FCM/FCA; validita temporale tesseramento; classificazione certa delle esclusioni; formula finale dei record; catalogo `TOP_FLOP` nel `RecordFamily`; GUI; export JS; manifest; shard; URL storici risolti dai normalizzati; pubblicazione. **Nessun record Top & Flop e ancora selezionabile o pubblicabile.**

## Vincoli operativi

La 3.2 operativa resta intatta. Non alterare Java preesistente, SQLite, configurazione o file dei siti in questo intervento. Ogni successivo collegamento al motore deve essere trattato con test completi della pipeline, non con nuove logiche parallele.
