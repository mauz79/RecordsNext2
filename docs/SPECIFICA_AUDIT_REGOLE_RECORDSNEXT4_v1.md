# RecordsNext 4.0 - Specifica Fase 3B2 v1: regole osservate

## Obiettivo

Collegare ogni incontro fantasy alla configurazione originale della competizione **di quella stagione** prima di definire come conteggiare prestazioni fantasy e punti inutilizzati. Per il momento nessun calcolo di punti persi o di sostituzioni teoriche.

## Relazioni SQL

- `raw_<stagione>_fcm_incontro.IDGIRONE` -> `raw_<stagione>_fcm_girone.ID`
- `raw_<stagione>_fcm_girone.IDCOMPETIZIONE` -> `raw_<stagione>_fcm_competizione.ID`
- `raw_<stagione>_fcm_moduloammesso.IDCOMPETIZIONE` -> `Competizione.ID`, `IDMODULO` -> `Modulo.ID`
- `raw_<stagione>_fcm_tabellino.IDINCONTRO` -> `Incontro.ID`, se presente.

Un ID incontro inesistente si conserva come fatto separato. Non si cerca di inferire la competizione dal nome della squadra, dal campionato piu probabile o da un altro anno.

## Campi regolamentari preservati

La versione JSON integrale conserva tutte le colonne `Competizione` e ogni riga `ModuloAmmesso` corredata da `Modulo`.

Il CSV di confronto evidenzia: numero sostituzioni; RU; voti ufficio per giocatore/portiere; limiti panchina per ruolo; sostituzioni tra ruoli; regole portiere/difesa, voto e bonus speciali; fonti voto e impiego tabellino; numero incontri e tabellini effettivamente collegati.

## Limiti importanti

- I dati `Competizione` rappresentano la configurazione conservata nell'archivio FCM; non dimostrano che quella configurazione fosse uguale a tutte le giornate se le impostazioni sono state cambiate in corso d'anno.
- `NUMEROSOSTITUZIONI=11`, `NUMERORISUFFICIO=99` e altri valori elevati non vengono convertiti in convenzioni interpretate senza prova.
- Un incontro presente in `Incontro` non e necessariamente giocato; il censimento include anche incontri con `GIOCATO=0` e righe con squadra 0. Un tabellino senza incontro non equivale automaticamente a riposo o errore.
- Non utilizzare `NUMEROSOSTITUZIONI` da solo per simulare recuperabilita: servono anche RU, ruolo, formazione iniziale, modulistica, voto effettivo, eventi speciali e ulteriori regole.

## Output

- `RecordsNext4_Regole_v1_competizioni.csv`: una riga per stagione/competizione, con campi selezionati e conteggi.
- `RecordsNext4_Regole_v1_stagioni.csv`: totali per stagione.
- `RecordsNext4_Regole_v1_eccezioni_campione.csv`: campioni limitati, senza dati delle fantasquadre.
- `RecordsNext4_Regole_v1_configurazioni_complete.json`: configurazioni sorgenti e moduli per competizione.
- `RecordsNext4_Regole_v1_sintesi.json`: totali e distribuzione di valori.
- `RecordsNext4_Regole_v1_MANIFEST.json`: hash SHA-256 di file e database.

Sorgente aperta esclusivamente con SQLite `mode=ro` e `PRAGMA query_only=ON`. Il piccolo database sintetico dei test viene creato esclusivamente nella sottocartella diagnostica in `D:\DEV_APPS\downloads`, non in `%TEMP%`. Nessuna modifica ai dati.
