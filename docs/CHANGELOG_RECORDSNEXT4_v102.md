# RecordsNext 4.0 — Changelog v102 (18 novembre 2026)

## Difetto individuato nel tentativo v101
Il preflight v101 richiedeva per `RecordsNextPipeline.java` il SHA256 della v86 (`47bafda8...`) anziche' quello della v92 realmente installata (`12a1b660...`). Il preflight ha bloccato correttamente l'installazione. Nessun file v101 e' stato applicato a seguito di questo errore.

Inoltre il payload Java v101 era stato ricavato dalla v86, quindi avrebbe ripristinato nomi e asset v83, perdendo parte della pubblicazione v92.

## Correzione v102
- **Baseline esatta:** `RecordsNextPipeline.java` v92, SHA256 `12a1b6606d52d224ac0713d8f573fae052dde336d9aae0191cd1dda737cd189d`.
- Preservati `attachTopFlopV92Assets`, `topFlopV92Pages`, `top_flop_v92.css` e tutti i file v92.
- Aggiunti al publisher nativo esclusivamente `fcmRecordsNextPlayerProfile_v101.js` e `fcmRecordsNextCalciatori_v101.js`.
- Aggiornato `calciatori.html` per caricare i due script v101.
- Etichette FCM: `OK`, `Saldo verificato`, `Nessun apporto`, `Dati insufficienti`; `null` non diventa zero.
- Il registro `player-identities.json`, SQLite, gli output FCM precedenti, gli altri plugin e Git non vengono modificati.
- Backup e rollback conservativi prima di build Maven e aggiornamento della sola pagina e dei due script nel sito locale.

**Stato al confezionamento:** preflight v101 fallito per SHA errato; v102 testato in isolamento, non ancora eseguito sul Windows dell'utente. GitHub rimane al commit `5c10ec5b729783747c5a8d33f17b5be84f70de74`.
