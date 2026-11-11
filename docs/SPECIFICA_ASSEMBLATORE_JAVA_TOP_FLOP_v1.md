# RecordsNext 4.0 - Specifica assemblatore Java Top & Flop v1

## Scopo

Unire **per stagione** i due lettori JDBC FCM/FCA già validati, senza produrre nuovi esportatori, record definitivi o tabelle SQL. L'oggetto risultante ha una sola prestazione FCA per calciatore/giornata e tante partecipazioni FCM quante sono le partite/competizioni/squadre, anche nello stesso turno reale.

## Strutture

- `LoadedSeason.archive`: `TopFlopArchive` con `RealDay`, `FantasyFact`, `RosterSnapshot` riassuntivi.
- `LoadedSeason.profiles`: profili FCA completi per ID locale stagionale.
- `LoadedSeason.rosterEvents`: **tutti** gli eventi `Tesserato` letti, con `tesseratoId`, senza deduplicazione e senza assegnare intervalli temporali immaginari.
- `LoadedSeason.fca/fcm`: bilancio degli adapter sorgente.
- `numericTabellinoTotals/nonNumericTabellinoTotals`: conversioni numeriche e token conservati senza conversione.

## Regole

- `TopFlopFcaJdbcReader` deve popolare i giorni **prima** del caricamento FCM, per consentire il controllo referenziale dei fatti fantasy.
- Identità: usare `PlayerSeason(seasonId, fcaPlayerId, gazzaCode)`; per i record storici i codici eventuali collisioni restano da disambiguare, senza unificazione di calciatori diversi.
- Per `Tabellino.Tot` convertire i token con regex numerica e virgola in `BigDecimal`. Per `sv/ng`, vuoti e altre stringhe usare `fantasyVote=null`, conservando comunque `FCM.Tabellino.Tot` nel `sourceFields`.
- Per la posizione iniziale: `POS=0` titolare iniziale. Per la posizione di tabellino `slot<=11`, conservare evidenza sorgente ma non inventare una partecipazione legalmente accertata (`Usage.UNKNOWN`). `slot>11/POS>0` = panchina non utilizzata; `slot>11/POS<0` = fuori lista, non automaticamente escluso nel senso della formula stagionale.
- Mai produrre da questa classe `EXCLUDED_CONFIRMED`, punti persi alternativi, gol teorici, Riserve d'Ufficio, coefficienti bonus/malus o scorecard URL ipotetici.
- `FantasyKey` distingue competizione, match, team e giocatore della stagione; i dati reali non vengono mai replicati.
- Solo i nomi delle competizioni ammessi dal `TopFlopCompetitionPolicy`; Youth League viene contata nel rapporto di esclusione tecnica, non caricata nei fatti pubblici.

## Limiti ancora aperti

La classe costruisce un archivio **di prova in memoria**, non ancora collegato a `RecordsNextPipeline` o pubblicato. Mancano in particolare: tesseramenti data-per-data, regole e coefficienti FCM storici, attestazione dei voti conteggiati, calcolo reale delle esclusioni, attribuzione del fantavoto per i non schierati, URL normalizzati dai siti corretti e gestione canonica delle collisioni anagrafiche.


## Snapshot 2026/27 aggiornato - chiarimenti

Gli incontri fantasy partono da `GIORNATADIA=3` fino a `9`; non trasformare giornate reali `1` e `2` in formazioni inventate. `LoadedSeason.rosterEvents` contiene ogni `Tesserato.ID`, mentre `archive.rosters` contiene membership univoche; conservare anche il futuro ledger `EntryInBilancio` separatamente prima di calcolare finestre di disponibilita'. Riferimento: `NOTA_FCM_GIORNATE_MERCATO_2026_2027_v1.md`.
