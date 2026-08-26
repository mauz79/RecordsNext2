# Changelog

## RecordsNext 2.1.0 — 2026-08-26

Aggiornamento della linea RecordsNext 2.x orientato alla completezza degli output,
alla parità dei dati disponibili sul sito e alla semplificazione dell'aggiornamento
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
- aggiornamento 2.0 -> 2.1 verificato su un'installazione reale già configurata.

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

## RecordsNext 2.0.0 — 2026-08-10

Prima release stabile della nuova linea RecordsNext 2.0.

### Novità principali

- nuova GUI Swing dedicata `RecordsNext by mauz79`;
- configurazione guidata di lega e stagioni;
- supporto stagioni GESTITE e MANUALI;
- associazioni storiche/canoniche di squadre e competizioni;
- lettura FCM/FCA tramite UCanAccess 2.0.9.5;
- pipeline modulare per famiglie di record;
- modalità di elaborazione Consolidata e Completa;
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
- audit contratto checkbox → vista superato;
- audit semantici Classici, Serie, RU, Modificatori, Fattore Campo, Soglie/Fortuna e Culometro superati;
- smoke test GUI e visualizzatori superato;
- lettura FCM dalla distribuzione di prova verificata;
- generazione automatica `config/league.json` verificata.

### Note

Le stringhe interne che citano RecordsNext 1.0.2 negli exporter indicano la provenienza/compatibilità degli archivi normalizzati e non la versione corrente del prodotto.
