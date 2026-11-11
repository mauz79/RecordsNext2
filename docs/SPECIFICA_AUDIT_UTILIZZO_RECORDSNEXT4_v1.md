# RecordsNext 4.0 - Specifica Fase 3A v1: utilizzo osservato

## Fonte

Tabelle SQLite raw per stagione `FCM.Formazione`, `FCM.Incontro`, `FCM.Tabellino`, `FCA.GiocatoreA` del database 3.2. Le prestazioni reali restano archiviate nella Fase 1 e NON sono duplicate per competizione.

## Campi classificati

- `POS=0`: titolare iniziale (come nelle analisi RU 3.2).
- `POS>0`: panchina.
- `POS<0`: fuori dalla formazione schierata (distinta dalle riserve).
- `ENTRATO`: flag sorgente FCM, conservato come 0/1 (non dedurre autonomamente numero di cambi).
- `ENTRATORISERVA`: secondo flag FCM; analisi RU 3.2 lo usa per i cambi realmente effettuati in specifici casi.
- `GIOCATO`: stato incontro FCM.
- `Tabellino.TOT`: slot del giocatore annuale individuato mediante liste percentuali FCM; categorizzato solo NUMERICO/NON_NUMERICO/VUOTO/MANCANTE. Non si somma.

Si produce un CSV per tutte le combinazioni osservate per stagione, un CSV con pochi campioni per categoria da studiare e un JSON riassuntivo. Il file manifest contiene SHA-256. Nessun contatto/password `Fantasquadra` viene letto o esportato.

## Marcatori diagnostici, non record pubblici

- PANCHINA_NON_ENTRATA_TOT_NUMERICO: panchinaro con totale numerico non entrato, INCONTRO.GIOCATO=1. Non significa punti recuperabili.
- FUORI_FORMAZIONE_TOT_NUMERICO: giocatore con POS negativo, non conteggiato come panchinaro.
- PANCHINA_ENTRATA_SENZA_FLAG_RISERVA: i due flag non coincidono; interpretazione da verificare.
- RISERVA_FLAG_SENZA_ENTRATO: caso inverso da approfondire.
- TITOLARE_NON_ENTRATO_TOT_NUMERICO: caso anomalo o particolare, non correttivo automatico.
- ENTRATO_INCONTRO_NON_GIOCATO: incoerenza da segnalare, se presente.
- SLOT_NON_CANONICO: mancanza/multiplicita slot, se presente.

## Limiti

I 168 tabellini senza incontro e i riposi si analizzano separatamente: il presente universo sono le righe `Formazione` con match. Codici di fantasia `0`, `-1` in LISTA non sono giocatori. La presenza di un totale numerico non certifica idoneita regolamentare, impossibilita di una sostituzione o risultato controfattuale. Non calcolare punti sprecati fino a definizione approvata. Nessun deploy, nessuna modifica al DB o al codice Java.
