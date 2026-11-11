# RecordsNext 4.0 - Specifica apporto registrato Top & Flop v1

## Fonte autorevole

Il punteggio effettivamente registrato di un giocatore in una partita fantasy e' quello del suo slot nel `Tabellino` FCM. `COUNTED` richiede insieme: partita `GIOCATO=1`, giocatore nei primi undici slot di `Tabellino.LISTA` e `Tabellino.TOT` numerico. Lo slot e' riferito alla coppia `IDINCONTRO` + `IDSQUADRA`, non alla singola giornata reale. Questo e' un criterio basato sul tabellino finale, NON una simulazione delle sostituzioni.

Altri stati: `BENCH_UNUSED` per calciatori di panchina non nei primi undici, `OUTSIDE_LIST` per posizioni negative fuori lista, `UNKNOWN` nei casi non dimostrati. Per `EXCLUDED_CONFIRMED` servono in seguito cronologia di tesseramento valida nella giornata e prova dell'esclusione; la sola mancanza di riga `Formazione` non basta. Riserve d'ufficio 0/-1 non sono calciatori.

## Grandezze

Ogni riga aggregata e' una combinazione giocatore stagionale / fantasquadra / competizione specifica. Campionato e Coppe si possono aggregare mantenendo distinti gli incontri FCM paralleli.

- `contestiFormazione`: righe `Formazione` presenti (non e' il numero di giornate di possesso).
- `partiteConteggiate`: i soli contesti `COUNTED` nel tabellino finale.
- `sommaVotiReali`: Voto1 FCA valido, per ciascun incontro fantasy conteggiato; non va usata per il totale delle presenze REALI all-time (si usa `RealDay` una sola volta).
- `sommaVotiTabellino`: campo FCM `Tabellino.Voto` numerico nei contesti COUNTED, distinto da FCA.
- `sommaFantavoti`: `FCM.Tabellino.Tot` nei contesti COUNTED; fantamedia = somma / partite conteggiate.
- `eventiApportati`: conteggi originali FCA associati esclusivamente ai contesti COUNTED. La voce `gol` deriva da `GOLFATTI1`, `golSuRigore` da `GOLFATTISURIGORE1` (sottoinsieme); `ASSIST`, `AMM`, `ESP`, `RIGPAR`, `RIGSBA` ecc. da campi FCA omonimi. Conservare tutti i campi originali, SENZA sommare le tre fonti voto 1/2/3. Gli eventi sono resi visibili solo come conteggi, non si applicano nuovi coefficienti fantasy.
- Media nulla quando manca il denominatore, non zero artificiale.

## Limiti ed evoluzione

Il report CSV e' read-only, non e' il file finale del sito. La completa pubblicazione dovra' riutilizzare facade, shard 1536 KiB, manifest, cutoff storico, rollback e URL canoniche di RecordsNext 3.2. L'archivio non unisce automaticamente codici storici eventualmente riutilizzati fra giocatori diversi.

Youth League esclusa, Spareggio Serie B e Play Off - Play Out nella famiglia CAMPIONATO. Non attribuire un voto sprecato in una giornata in cui il giocatore non era in rosa.
