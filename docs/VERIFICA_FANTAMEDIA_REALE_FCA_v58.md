# RecordsNext 4.0 - v58: fonte ufficiale FMT FCA per le viste Top & Flop

## Scopo e limitazioni

Questo intervento modifica unicamente l'estrazione `RealFantasy` per le viste
Top & Flop e la stringa di disponibilita' della scheda storica. Non modifica
la fonte FCA/FCM, l'SQLite, il motore dei record classici 3.2, il sistema di
identita' o i punteggi apportati dalle fantasquadre (FCM Tabellino.TOT).

## Algoritmo

- Leggere i dati ufficiali `raw_..._fca_statistiche`: `FMT` e `PRESENZE`.
- Per ciascun calciatore confrontare `Statistiche.PRESENZE` con
  `COUNT(GiocaIn JOIN Punteggio WHERE VOTO1 > 0)` della stessa stagione.
- Accettare `FMT` ufficiale solo con voto positivo, campione esattamente
  corrispondente e FMT valida; mai sostituire dati mancanti con zero.
- Esportare il campione, la fonte e lo stato. La `sommaFantavotiReali` e'
  `FMT*PRESENZE` ed e' una ricostruzione ponderata a partire dalla media
  stagionale ARROTONDATA della fonte FCA, non la somma dei singoli fantavoti.
- Se un regolamento stagionale e' stato verificato e registrato esplicitamente,
  la ricostruzione da eventi viene usata SOLO per gli eventuali calciatori
  privi di una FMT ufficiale allineata, senza inventare coefficienti.
- Non usare `Storico.FANTAMEDIA1` come sostituto automatico: nel 2012/13
  la sua copertura e' ferma alle giornate 2-38 (v57).
- Non fondere identita' tra stagioni per nome o codice storico.

## Prove prima del rilascio

1. `TopFlopOfficialFmtV58SmokeTest`: test aritmetici + confronto SQLite
   2012/13, con voti 9936, Abbiati 28 e `FMT=5.25`.
2. Compilazione e test del progetto con Maven nella copia TEST.
3. Rigenerazione Completa da GUI, pubblicazione DISABILITATA.
4. Verifica dei nove JS base compatibili 3.2 + facades Top & Flop,
   shard, hash, limite fisico di 1.500.000 byte per singolo JS.
5. Audit confrontando ogni RealFantasy ufficiale pubblicata nel nuovo staging
   con Statistiche.FMT/PRESENZE e count VOTO1 effettivo.
6. Solo successivamente: prova di pubblicazione multisito in sandbox,
   integrazione visualizzatori, cutoff storico, e collaudo navigazione.

## Stato

Patch TEST non equivale a release finale. Non pubblicare nei siti reali
prima dei test di sandbox e dell'approvazione esplicita dell'utente.

## Correzione v58c del test planner

`ExecutionPlannerTest.planGroupsItemsByFamily` verificava erroneamente cinque
famiglie. Il catalogo comprende sei famiglie, inclusa `TOP_FLOP`, e
`ExecutionPlan.byFamily()` restituisce un raggruppamento per ciascuna.
La correzione **non sostituisce il file con il vecchio v46**: modifica
soltanto l'asserzione nel file di TEST installato, verificando prima il suo
SHA-256 e conservando tutto il resto. L'asserzione risultante confronta
le famiglie pianificate con tutte le famiglie di `RecordFamily`.
Nessuna logica del planner viene alterata. Maven v58 falliva su un test
obsoleto (1 fallimento su 58).
