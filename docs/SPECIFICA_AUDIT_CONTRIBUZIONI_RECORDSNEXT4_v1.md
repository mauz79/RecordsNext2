# RecordsNext 4.0 - Specifica Fase 3B1 v1

## Perche serve

`Formazione.ENTRATO` e `ENTRATORISERVA` descrivono stati FCM differenti. Non sono una prova autonoma del voto effettivamente utilizzato. Incrociamo quindi ogni riga `Formazione` con la posizione della relativa occorrenza in `Tabellino.LISTA`, insieme allo stato di `Incontro`.

## Fonti

Tabelle SQLite `raw_<stagione>_fcm_formazione`, `raw_<stagione>_fcm_tabellino`, `raw_<stagione>_fcm_incontro`, `raw_<stagione>_fca_giocatorea`. Si usa `GiocatoreA.ID` solo per la relazione annuale; `CODICEGAZZA` e esposto solo per l'identita storica. Non si leggono credenziali o tabelle `Fantasquadra`.

## Categorie descrittive

- `PRIMI11`: il giocatore compare con indice zero-based inferiore a 11 in `Tabellino.LISTA`.
- `DOPO_PRIMI11`: indice pari o superiore a 11.
- `SLOT_SPECIALE_PRIMI11`: ID del tabellino <= 0 (per esempio RU), non e un giocatore.
- `PRIMI11_ENTRATO_ZERO`: incongruenza informativa dei flag da studiare, non un dato da correggere.
- `FLAG_RISERVA_FUORI_PRIMI11`: `ENTRATORISERVA=1` ma slot dopo i primi undici; non considerarlo automaticamente utilizzato.
- `TABELLO_ID_INCONTRO_ASSENTE`, `TABELLO_SENZA_FORMAZIONE`, `TABELLO_INCONTRO_SQUADRA_ZERO`: verifiche DISTINTE e potenzialmente sovrapposte; non etichettare automaticamente come errore o turno di riposo.

Vengono prodotti CSV aggregati per stagione/flag/stato, campioni limitati e sintesi JSON con SHA-256. Tutte le righe `Formazione` devono comparire in uno e un solo slot individuale; in caso contrario il test fallisce.

## Limiti espliciti

La posizione nei primi undici slot e un'evidenza del tabellino FCM, NON ancora una definizione approvata di voto/bonus conteggiato, ne di punto sprecato. Prima di attribuire punteggi bisogna testare RU, modulistica, ruoli, numero sostituzioni, configurazione per competizione, campionati in inferiorita e modificatori. Gli archivi RU personali della 3.2 restano separati.

Nessuna modifica al DB o alle funzioni Java. Nessuna pubblicazione.
