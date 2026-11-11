# RecordsNext 4.0.0 — Piano tecnico per l'archivio e i record dei calciatori (v1)

**Base esaminata:** RecordsNext 3.2.0 (commit/tag indicato dall'utente: `f9bf303`, `v3.2.0`). Fonti: ZIP `RecordsNext_Giocatori_DOC_CODICE_20261031_205136.zip`, `RecordsNext_Giocatori_DATI_20261031_205136.zip`, documenti `MODELLO_DATI_RECORDSNEXT2.md`, `ARCHITETTURA_RECORDSNEXT2.md`, `STATO_IMPLEMENTAZIONE_RECORDSNEXT2.md` e copia SQLite dell'utente.

**Stato:** progetto/strumento diagnostico iniziale. Questo documento non significa che RecordsNext 4.0 sia stata sviluppata o rilasciata. Non aggiornare anticipatamente la versione Maven, la GUI, i manifest o il tag di release.

## Decisione di versione

La nuova capacità è strutturale: anagrafiche e carriere reali, carriere fantasy, record individuali e viste HTML. Il target è **RecordsNext 4.0.0**. Sviluppo iniziale isolato in un worktree/branch, senza interferire con la versione 3.2.0 operativa.

## Fonti e relazioni verificate

- `raw_<stagione>_fca_giocatorea`: `ID` locale della stagione, `CODICEGAZZA` (es. 100843), `NOME`, `RUOLO`, `FOTO`, anagrafica.
- `raw_<stagione>_fca_giocain`: `IDGIOCATORE` locale, `GIORNATA`, `IDSQUADRA` reale, `IDPUNTEGGIO`.
- `raw_<stagione>_fca_punteggio`: `ID`, voti, gol, assist, malus, ecc. La tabella rappresenta punteggi referenziati: **non** contarne le righe come presenze individuali.
- `raw_<stagione>_fca_statistiche` e `raw_<stagione>_fca_storico`: aggregati/prospetti FCA da confrontare con i dettagli, non da sommare automaticamente ad essi.
- `raw_<stagione>_fcm_formazione`: `IDGIOC` locale, `IDINCONTRO`, `IDSQUADRA`, `ENTRATO`, `ENTRATORISERVA`, `POS`, `RIG`.
- `raw_<stagione>_fcm_tesserato`: `IDGIOCATORE` locale, `IDSQUADRA`, attributi contratto/cessione; la data esatta dei cambi deve essere accertata prima di dedurre periodi di tesseramento.
- `raw_<stagione>_fcm_tabellino`: `LISTA`/`VOTO`/`MODIF`/`TOT` (oltre alle varianti), da interpretare secondo la semantica storica FCM.
- `rn_team_match`: contesto normalizzato per la competizione/incontro; `rn_team_identity` e `rn_competition_identity` sono le associazioni storiche già esistenti.

La copia esaminata contiene 21 stagioni con tabelle raw, dal 2006/07 al 2026/27. `rn_season` include anche un riferimento alla stagione manuale. La copia è una fotografia del momento dell'esportazione, non garantisce la situazione operativa attuale.

## Identificativi: regola vincolante

1. `GiocatoreA.ID` è **solo stagionale**; viene utilizzato dai join `Formazione.IDGIOC` e `GiocaIn.IDGIOCATORE` della stessa stagione. Mai aggregare tra anni per `ID`.
2. Il codice storico del calciatore è presente in `GiocatoreA.CODICEGAZZA`; per Lautaro Martínez il codice è `100843`, identico in nove stagioni dell'archivio. Il campo delle foto di norma segue lo stesso codice, ma eventuali anomalie vanno auditate.
3. **Non effettuare fusioni storiche cieche:** la copia FCA presenta almeno un codice con anagrafiche differenti (12302: Ascoli/Crivello). Non alterare gli originali. Una tabella di risoluzione dell'identità deve distinguere casi certi, verificati manualmente e ambigui.
4. Non confondere l'ID locale reso pubblico dalle vecchie strutture JS con l'identità all-time. Compatibilità pubblica 3.2.0 invariata durante l'implementazione.

## Granularità canoniche proposte

### A. `player_season`

Chiave: `(season_id, source_player_id)`; riferimenti ai campi FCA originali, codice storico, identità canonica solo dopo riconciliazione, nome, ruolo, squadra di A e metadati.

### B. `player_real_round`

Chiave logica: `(season_id, source_player_id, serie_a_round)`; raw di `GiocaIn` + `Punteggio`, disponibilità voto/status, bonus/malus e contesto partita reale se verificato. Una giornata senza voto non è automaticamente una presenza effettiva. Nessuna duplicazione per numero di gare fantasy.

### C. `player_fantasy_match`

Chiave logica: `(season_id, source_match_id, source_team_id, source_player_id, occurrence_index)` — l'indice si applica soltanto se si dimostrano occorrenze multiple legittime. Conservare `Formazione` e il riferimento al dettaglio `Tabellino`; classificare starter, reserve, bench, entered, unused soltanto dopo verifica semantica. Non usare la semplice somma dei fantavoti come definizione di "punti non sfruttati".

### D. `player_roster_evidence`

Archiviare l'evidenza di tesseramento per stagione/squadra, senza inventare date o proprietà non dimostrate.

### E. `player_identity_mapping`

Identità canonica con associazione `(season_id, source_player_id, historical_code)`, stato (`VERIFIED`, `AMBIGUOUS`, `UNMAPPED`) e provenienza. Codice storico 0 o discordante: nessun collegamento automatico.

## Record/filtri previsti

Due domini indipendenti: **reale** (conteggio unico per giornata della Serie A) e **fantasy** (conteggio per partita effettivamente disputata nella lega, anche in competizioni diverse). Ambiti: singola stagione, competizione, fantasquadra, totale storico, carriera personale. Esempi: gol, assist, presenze a voto, fantamedia, migliori/peggiori prestazioni, serie, rigori, cartellini, gol/assist conteggiati, punti non utilizzati. Il catalogo definitivo e i denominatori delle medie sono da validare prima di calcolare classifiche.

## Output del sito e compatibilità

- Rispettare la pubblicazione multisito di 3.2.0, con cutoff per `rn_season.sort_order`.
- Nessuno shard futuro su un sito storico; shard direttamente in `js` senza sottocartelle.
- I nove output pubblici 3.2.0 e i record esistenti devono restare compatibili; nuovi dataset individuali separati e caricati a richiesta.
- Conclusione solo dopo GUI, JS, visualizzatori, audit e aggiornamento della bibbia con generatore dedicato.

## Traguardi e criteri di accettazione

1. **Fase 0 — Base isolata e audit identità.** Worktree derivato dal tag 3.2.0. Eseguire `tools/Audit-RecordsNext4Giocatori-v1.py` su copia DB; 100843 -> nove stagioni, 12302 -> revisione manuale. Nessun DB scritto.
2. **Fase 1 — Estrazione reale completa.** Tutti i calciatori, tutte le stagioni disponibili, righe dettaglio e aggregati con totali confrontati con `Statistiche`/`Storico`. Gestione `SV`, null, valori speciali e bonus numerati.
3. **Fase 2 — Estrazione fantasy completa.** Ricostruzione incontro/competizione/squadra/giocatore; corrispondenza tabellino, titolari/riserve, nessun doppio conteggio accidentale. Audit dei casi con più squadre o più competizioni.
4. **Fase 3 — Record.** Catalogo di record reali/fantasy e definizioni rigorose per `punti non sfruttati`, classifiche, medie e serie; parità e dati mancanti.
5. **Fase 4 — GUI e sito.** Selezioni e preflight, JS per sito, cutoff storico, viste giocatore/classifiche; nessuna regressione degli output 3.2.
6. **Fase 5 — Qualità/release.** Test Maven + audit semantici storici e nuovi + confronto all-time + rigenerazione bibbia + release FULL/UPDATE/SETUP 4.0.0.

## Limiti dello strumento Fase 0

Lo script `Audit-RecordsNext4Giocatori-v1.py` è **un ispettore**: cerca un `CODICEGAZZA` fra gli FCA e mostra i riferimenti stagionali, il numero di righe GiocaIn/Formazione e possibili ambiguità. Non deduce presenze effettive, punti, trasferimenti o identità canoniche, non crea alcun record e non modifica il database. I report sono salvati esclusivamente nella cartella `--output-dir`.

## Installazione isolata consigliata (PowerShell 5.1)

Partire dal repository locale nella versione verificata, mantenendo inalterate le modifiche non committate (`config/processing.json`) e i report non tracciati:

```powershell
Set-Location 'D:\DEV_APPS\RecordsNext2.0'
git worktree add -b feature/recordsnext4-players 'D:\DEV_APPS\RecordsNext4.0' v3.2.0
```

Estrarre lo ZIP `RecordsNext4_Fase0_Giocatori_v1.zip` **nella radice** di `D:\DEV_APPS\RecordsNext4.0` (aggiunge solo questo Markdown e lo script nuovo).

Eseguire audit su una copia sandbox esistente senza scriverla:

```powershell
py -3 'D:\DEV_APPS\RecordsNext4.0\tools\Audit-RecordsNext4Giocatori-v1.py' `
  --database 'D:\DEV_APPS\RecordsNext2.0\data\database\recordsnext-sandbox.db' `
  --code 100843 --output-dir 'D:\DEV_APPS\downloads'
```

Per il controllo del codice ambiguo sostituire `100843` con `12302`. I report hanno nomi versionati e timestamp.

**Non fare ancora** `git add .`, `git commit`, bump versione Maven o rigenerazione della bibbia: prima esaminare i risultati e definire i join/semantica dei dati.
