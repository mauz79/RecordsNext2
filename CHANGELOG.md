# Changelog

## RecordsNext 3.0.0 — 2026-08-28

### Architettura dati stagionale

- introdotti shard JavaScript separati per stagione per Classics, Series, RU, Modifiers, ThresholdsLuck, Culometro e Matches;
- ogni stagione conserva i propri sette shard nella cartella `js/recordsnext-data` del relativo sito FCM;
- il sito corrente mantiene gli stessi nomi pubblici `fcmRecordsNext_*.js`, ora trasformati in facade leggere;
- API JavaScript pubblica e strutture dati esposte ai visualizzatori restano compatibili con RecordsNext 2.1;
- caricamento locale e online degli shard basato sui percorsi configurati per ciascuna stagione, senza dipendenze specifiche da Altervista;
- introdotta la disponibilita' progressiva locale/online delle stagioni storiche;
- introdotto lo stato persistente `data/consolidation/recordsnext-shards.properties`;
- aggiunta `SeasonShardAvailabilityCli` per la gestione della migrazione progressiva;
- shard invariati non vengono riscritti;
- una stagione corrente ancora senza partite testa-a-testa non genera shard vuoti ne' riferimenti a file inesistenti.

### Elaborazione consolidata

- la modalita' `CONSOLIDATED` aggiorna soltanto la stagione corrente quando storico e configurazione risultano invariati;
- corretta la gestione di una nuova stagione senza `season_normalized_*.json`: Classic e RU restano invariati invece di terminare con errore;
- validato il passaggio da circa 103 secondi in FULL a circa 5-6 secondi in CONSOLIDATED sul dataset operativo corrente.

### Pubblicazione

- restano pubblicati soltanto i 9 output moderni RecordsNext;
- gli output pubblici legacy `records2026.*` non vengono piu' generati;
- validata la distribuzione di 20 stagioni storiche, dal 2006_2007 al 2025_2026;
- validati 140 shard complessivi, 7 per stagione;
- tutti i singoli shard restano sotto 1 MiB nel dataset operativo validato;
- le sette facade del sito corrente referenziano correttamente tutte le stagioni storiche abilitate;
- corretta la rotta online della stagione 2022_2023 verso `/lega2022/`.

### Versione

- versione Maven 3.0.0;
- manifest applicativo 3.0.0;
- runner console identificato come RecordsNext 3.0;
- Java 21 o superiore;
- UCanAccess 2.0.9.5 invariato.

## RecordsNext 2.1.0 â€” 2026-08-26

Aggiornamento della linea RecordsNext 2.x orientato alla completezza degli output,
alla paritÃ  dei dati disponibili sul sito e alla semplificazione dell'aggiornamento
delle installazioni esistenti.

### Output e dati

- aggiunto l'output canonico `fcmRecordsNext_Matches.js`;
- una riga per squadra per ogni incontro reale, quindi due righe per partita;
- esclusione dei turni di riposo dall'output Matches;
- risultati standardizzati `V/N/P`;
- link ai tabellini storici corretti anche per stagioni precedenti;
- arricchiti i dettagli delle Serie con stagione, competizione, giornata, incontro e tabellino;
- arricchiti gli eventi del Culometro con frequenza storica dell'evento,
  frequenza storica della configurazione e impatto;
- invariata la formula di calcolo e la classifica del Culometro.

### Bonifica output legacy

- rimossa dalla pubblicazione la famiglia di file `records2026.*` non piu consumata dal sito moderno;
- `Records2026ClassicJsExporter` e `Records2026RuJsExporter` restano usati solo internamente dai wrapper RecordsNext per preservare la compatibilita dei dati;
- `fcmRecordsNext_Classics.js` e `fcmRecordsNext_RU.js` verificati byte-per-byte invariati rispetto agli output 2.1 precedenti;
- eliminati circa 28 MiB di output legacy per sito senza modifiche al frontend.

### Configurazione storica

- dopo l'aggiunta di una stagione gestita, la finestra delle associazioni storiche
  si apre direttamente sulla stagione appena aggiunta;
- il comando generale di configurazione delle associazioni conserva invece
  il comportamento newest-first.

### Aggiornamento da RecordsNext 2.0

- introdotto un pacchetto UPDATE dedicato;
- l'aggiornamento sostituisce soltanto programma e launcher;
- configurazione, database, stagioni e associazioni storiche vengono conservati;
- backup automatico di sicurezza prima della sostituzione;
- aggiornamento 2.0 -> 2.1 verificato su un'installazione reale giÃ  configurata.

### Release e runtime

- versione Maven 2.1.0;
- manifest applicativo 2.1.0;
- GUI identificata come RecordsNext 2.1;
- runtime UCanAccess 2.0.9.5 invariato;
- Java 21 o superiore.

### Verifiche

- suite Maven completata con 41 test superati;
- regressione completa sui dati storici verificata;
- Matches verificato su 20 stagioni;
- 10.930 partite reali e 21.860 righe squadra verificate;
- nessun gruppo Matches diverso da due righe;
- nessuna coppia non speculare;
- confronto semantico delle famiglie preesistenti superato;
- aggiornamento reale 2.0 -> 2.1 verificato senza modifica di configurazione e database;
- generazione del sito tramite il flusso reale FCM verificata con RecordsNext 2.1.

## RecordsNext 2.0.0 â€” 2026-08-10

Prima release stabile della nuova linea RecordsNext 2.0.

### NovitÃ  principali

- nuova GUI Swing dedicata `RecordsNext by mauz79`;
- configurazione guidata di lega e stagioni;
- supporto stagioni GESTITE e MANUALI;
- associazioni storiche/canoniche di squadre e competizioni;
- lettura FCM/FCA tramite UCanAccess 2.0.9.5;
- pipeline modulare per famiglie di record;
- modalitÃ  di elaborazione Consolidata e Completa;
- output JavaScript separati per famiglia;
- pubblicazione verso il sito FCM;
- visualizzatori HTML statici;
- supporto ai link tabellini `.htm`, `.html` e `.php`;
- record di lega;
- Culometro opzionale.

### Famiglie disponibili

- Classici
- Serie
- Riserve d'Ufficio
- Modificatori
- Soglie e Fortuna
- Culometro

### Modificatori

- supporto modificatori standard FCM;
- supporto MODM1PERS, MODM2PERS e MODM3PERS;
- nomi personalizzabili dei modificatori personali;
- selezione granulare delle statistiche;
- supporto serie dei modificatori;
- supporto Fattore Campo.

### Storico

- associazioni canoniche delle squadre;
- associazioni canoniche delle competizioni;
- gestione differenze di denominazione tra stagioni;
- esclusione delle stagioni MANUALI dall'elaborazione FCM;
- ricostruzione completa delle stagioni GESTITE.

### Visualizzatori

- home RecordsNext;
- Classici;
- Serie;
- Riserve d'Ufficio;
- Modificatori;
- Soglie e Fortuna;
- Culometro;
- Record di lega;
- filtri per stagione e competizione;
- nomi competizioni leggibili;
- selezione eventi Culometro nella sola vista Eventi;
- profili `mauzstrom`, `maelstrom`, `fantablue2`, `neutral`.

### Release e runtime

- versione Maven 2.0.0;
- manifest applicativo 2.0.0;
- launcher `RecordsNext.bat`;
- runtime UCanAccess 2.0.9.5 distribuito con dipendenze e licenze;
- configurazione `league.json` generata automaticamente dalla GUI.

### Verifiche effettuate

- build Maven completato con successo;
- test automatici superati;
- audit contratto checkbox â†’ vista superato;
- audit semantici Classici, Serie, RU, Modificatori, Fattore Campo, Soglie/Fortuna e Culometro superati;
- smoke test GUI e visualizzatori superato;
- lettura FCM dalla distribuzione di prova verificata;
- generazione automatica `config/league.json` verificata.

### Note

Le stringhe interne che citano RecordsNext 1.0.2 negli exporter indicano la provenienza/compatibilitÃ  degli archivi normalizzati e non la versione corrente del prodotto.
