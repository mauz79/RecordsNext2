# RecordsNext 4.0 — Verifica TF11–TF13 su panchina documentata (v48)

Data: 2026-11-12. **La v48 e' una patch TEST, non una release pubblica.**

## Definizione implementata, senza inferenze

Un evento eleggibile e' **un calciatore inserito nella formazione FCM come riserva** per un incontro giocato, presente nel tabellino finale fuori dagli undici conteggiati, con `POS>0`, `Slot>11`, `GIOCATO=1`, `ENTRATO=0`, `ENTRATORISERVA=0`. La squadra, la competizione e il calciatore derivano dall'FCM/FCA di quell'evento; il tabellino puo' essere collegato solo tramite il resolver canonico 3.2.

La presenza in formazione e' una prova specifica **per quella gara**. Non prova il tesseramento continuo nell'intera stagione e non identifica gli assenti dalla formazione. Restano esclusi: `POS<0` (fuori lista), `POS=0`, slot <=11, incontro non giocato, contraddizioni su entrata, formazione senza match provato, giocatori assenti dalla formazione. Per questi casi non si crea un'esclusione fittizia.

La popolazione pubblicata e' quindi **SOLO_RISERVE_PRESENTI_IN_FORMAZIONE_FCM_NON_CONTEGGIATE**. Non rappresenta tutti gli errori dell'allenatore, ne' la completa storia del possesso.

## Metriche e soglie

Per ogni combinazione distinta stagione + giocatore locale + fantasquadra + competizione:

- `E`: occasioni di panchina documentata non utilizzata; include eventi senza voto.
- `P_V`: occasioni eleggibili con voto reale FCA valido. `S_V`: somma di tali voti.
- `P_F`: occasioni eleggibili con voto reale valido e `FCM.Tabellino.TOT` numerico nel proprio slot; `S_F`: somma di questi TOT gia' presenti nella fonte, mai ricostruiti.
- **TF11**: `S_V/P_V` (media voto reale non utilizzato), attivo solo con `E >= verifiedExclusions` e `P_V >= exclusionsWithVote`.
- **TF12**: `S_V/E` (errore pesato voto), attivo con `E >= verifiedExclusions` e `P_V>0`.
- **TF13**: due record `S_F/P_F` (fantamedia tabellino non utilizzata) e `S_F/E` (errore pesato fantavoto), attivi con `E >= verifiedExclusions` e `P_F >= exclusionsWithVote`.
- Tutti i record rispettano l'identita' stagionale, il raggruppamento per fantasquadra/competizione e la **famiglia CAMPIONATO/COPPE**; non sommano contesti di leghe diverse o stagioni diverse per creare un vincitore artificiale. Non viene applicato alcun punteggio alternativo alla squadra.

Le soglie sono configurabili nella scheda Top & Flop; valori predefiniti `E>=3`, `P_V/P_F>=2`. Le voci GUI TF11, TF12, TF13 sono selezionabili. TF14/TF15 ora hanno etichette aggiornate, senza la dicitura 'da definire'. La patch non modifica `config/topflop.json` esistente e non attiva implicitamente record non selezionati.

## Formato JS e sito

- Nuova sezione lazy `Bench`, shard stagionali `fcmRecordsNext_TopFlopViews.Bench.YYYY_YYYY.NNN.js` con aggregati documentati e soglie soddisfatte. Nessun dump completo di formazione/rosa.
- Nuovi gruppi nelle sezioni `Records` (TF11, TF12 e due metriche TF13) e highlights `panchinaDocumentataNonUtilizzata` soltanto per i vincitori, con incontro, giornata, voto, TOT e link canonico se noto.
- Facade `fcmRecordsNext_TopFlopViews.js` continua con `schemaVersion=4.0-public-views-v1`, descrive perimetro, soglie, file, SHA e dimensioni; il decoder V2 carica `Bench` a richiesta.
- Il ramo `attachScoped` mantiene cutoff storico e ammette esplicitamente `Bench`; ogni JS mantiene limite fisico **1.500.000 byte** e target shard <=950.000 byte.

## Prove offline

- Java 21 compilato (95 sorgenti).
- Smoke sintetico v48 con `E=4`, `P_V=3`, `P_F=3`, `S_V=21`, `S_F=29`, TF11=7, TF12=5.25, TF13 pesato=7.25; gare non giocate, riserve entrate, `POS<0`, classi con slot conteggiato, e soglie superiori sono escluse.
- Regessioni v47/v46: TF14/TF15, collisone Okaka, identita' separate Ascoli/Crivello, costi e fantamedia da fonti verificate, Classics 9 MB in shard <=1.500.000 byte.
- Audit FCM su snapshot SQLite del 2026-11-06 (non sul registro corrente dell'utente): 2006/07 1452 riserve non conteggiate, 837 TOT numerici; 2015/16 9307 e 5793; 2023/24 14180 e 8819; 2025/26 13227 e 7969. Sono riscontri della **popolazione eleggibile grezza**, non graduatorie definitive né verifica dei voti FCA.

## Gate Windows prima del punto 1

1. Preflight base v47, installazione TEST con backup e rollback, senza toccare SQLite/identita'/config/siti.
2. Nella GUI selezionare TF11–TF13 e TF15 dove desiderato, salvare, lasciare pubblicazione OFF, eseguire una Completa.
3. Log: record selezionati non devono essere etichettati indisponibili; contare `Bench`/`Records`/`Highlights`, riconciliare E/P/S con tabellini reali, poi audit 282+ file JS e test di cutoff su siti di prova.

**Limite dichiarato:** le esclusioni di giocatori NON presenti nella formazione rimangono non dimostrabili con i dati usati. Una futura cronologia transazionale completa potra' estendere il record, senza falsificare questi dati.
