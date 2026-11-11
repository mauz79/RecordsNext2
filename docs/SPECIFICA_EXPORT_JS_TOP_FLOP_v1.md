# Top & Flop — Specifica exporter JS nativo v1

## Schema

- `fcmRecordsNext_TopFlop.js`: facade `window.fcmRecordsNextTopFlop` con metadati e nomi shard per stagione. **Non carica automaticamente** i dettagli.
- `fcmRecordsNext_TopFlop.Summary.<seasonId>.<nnn>.js`: aggiunge righe a `window.fcmRecordsNextTopFlopData.summaries[seasonId]`. Ogni riga: identificatori stagionali, codice storico, nome, recap reale, medie di apporti alle fantasquadre/competizioni, nome esatto dello shard dettaglio.
- `fcmRecordsNext_TopFlop.Detail.<seasonId>.<nnn>.js`: aggiunge righe a `window.fcmRecordsNextTopFlopData.details[seasonId]`, ciascuna con giornate FCA (voto, stato, eventi sorgente non-zero) e contesti FCM (squadra, competizione, famiglia, giornata competizione, giornata Serie A, schieramento, uso effettivo, voto tabellino, fantavoto, eventi FCA conteggiati, link tabellino canonico se presente).

**Navigazione browser:** caricare facade, i soli shard Summary della stagione interessata, selezionare un calciatore, quindi caricare solo il suo `detailFile`. Per carriera multipla iterare stagioni senza unificare automaticamente codici storici ambigui.

## Dimensioni e pubblicazione

- Target massimo consigliato 950.000 byte; **limite di sicurezza 1.500.000 byte reali** su ogni JS (UTF-8). Nessuna riga giocatore può essere spezzata: se troppo grande, l'export fallisce invece di troncarla.
- Nomi di file **flat**, preparati per la cartella `js` dei siti. Non esistono sottocartelle JS.
- Questa fase scrive solo in `data/site-export-staging/topflop-stage-v1_<timestamp>/js` nel worktree 4.0. I file non sono ancora compatibili con la validazione di conteggio/prefissi del publisher esistente fino all'integrazione prevista.
- Per pubblicare serviranno estensioni coordinate delle classi esistenti `RecordFamily`, `CoreRecordCatalog`, `ProcessingOptions`, `RecordsNextPipeline`, `Records2026SitePublisher`, `SeasonFamilyShardPublisher`, manifest e GUI, senza bypass e con cutoff multisito e rollback.

## Dati, provenienza e limiti

- La fonte dei fatti è `TopFlopJdbcArchiveAssembler` che riusa il RAW FCM/FCA importato dalla 3.2 e i round canonici di `rn_event`.
- Il `scorecardUrl` proviene SOLO dai `season_normalized_*.json` 3.2 per `idIncontro` + `idSquadra` quando esiste `urlTabellinoOnline` valido. Non costruire URL da `matchId` o `serieARound`.
- I report 2026/27 aggiornati non sono ancora disponibili: link `null`, senza fallback inventati. Si risolvono dopo la normalizzazione 3.2 della stagione aggiornata.
- Youth League non viene esportata. Spareggio Serie B, Play Off - Play Out sono CAMPIONATO.
- Lo stesso gol reale può apportare un gol in due incontri fantasy distinti (coppa/campionato): carriera reale deduplicata per giornata; carriera fantasy conteggia gli incontri distinti.
- Le medie individuali sono derivate dai soli casi `Usage.COUNTED` verificati nei tabellini; per `OUTSIDE_LIST`/`BENCH_UNUSED` non si attribuisce automaticamente un'esclusione certa. Periodi di tesseramento e quattro indici voto sprecato ancora da completare.
- `CodiceGazza` non è una chiave globale infallibile per tutti gli anni. Le righe mantengono sempre `seasonId` + `localFcaId` e codice storico.
- La proiezione JS compatta conserva gli eventi non-zero (senza duplicare i 3 quotidiani) mentre **tutti gli originali** restano accessibili nel database SQLite e nei fatti Java.

## Test obbligatori

1. Compilazione Java 21, regressione modello 52/52 e test parser/link canonici.
2. Contabilità righe FCA/FCM e anagrafiche per ogni stagione.
3. Unicità scheda calciatore (stagione + ID locale) e chiave FCM (calciatore, competizione, incontro, squadra), risoluzione `detailFile`.
4. Limite byte su TUTTI i `.js`, non solo sui dettagli; parser JSON del payload e caricamento come JS.
5. Controllo SHA-256 database originale invariato.
6. Nessuna pubblicazione reale; pipeline 3.2 invariata.
