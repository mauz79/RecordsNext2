# Stato implementazione RecordsNext 2.0

Aggiornamento: 5 agosto 2026.

## Base tecnica verificata

RecordsNext 2.0 nasce dal codice funzionante di RecordsNext 1.0.2.

Sono stati importati e mantenuti:

- progetto Maven e Maven Wrapper;
- Java 21;
- accesso FCM/FCA tramite UCanAccess;
- importazione raw in SQLite;
- configurazione delle stagioni;
- stagioni gestite e manuali;
- associazioni canoniche di squadre e competizioni;
- viste canoniche;
- normalizzazione stagionale;
- consolidamento;
- cache di normalizzazione;
- esportatori Classici e Riserve d'Ufficio esistenti;
- staging, validazione, pubblicazione e rollback;
- GUI esistente come base da evolvere.

UCanAccess 2.0.9.5 resta candidato obbligatorio nei benchmark comparativi.

## Modello modulare implementato

Sono state introdotte le strutture Java per:

- cinque famiglie elaborabili;
- figli di famiglia;
- dipendenze obbligatorie e opzionali;
- stati degli output;
- selezione modulare;
- catalogo iniziale;
- valutazione delle dipendenze.

Famiglie:

1. Classici;
2. Serie;
3. Riserve d'Ufficio;
4. Modificatori;
5. Soglie e Fortuna.

Il Culometro resta un easter egg opzionale, non selezionato automaticamente.

## Compatibilità con la pipeline 1.0.2

ProcessingOptions e RecordsNextPipeline sono stati estesi mantenendo la compatibilità con il costruttore legacy basato su:

- Classici;
- Riserve d'Ufficio;
- generazione JavaScript;
- pubblicazione.

La pipeline usa internamente la selezione modulare.

Le famiglie non ancora dotate di elaboratore non vengono ignorate silenziosamente.

## Planner e preflight

Sono implementati:

- inventario delle capacità disponibili;
- piano di esecuzione;
- valutazione preventiva delle dipendenze;
- conteggio di figli completi, parziali e saltati;
- preflight eseguibile prima della produzione degli archivi e degli output.

La mancanza di una dipendenza blocca soltanto il figlio interessato.

Esempio consolidato:

- Capitano disattivato;
- output del Capitano saltati;
- serie dipendenti dal Capitano saltate;
- tutte le altre serie e famiglie continuano.

## Configurazione modulare

La configurazione processing.json viene letta dal codice Java.

Sono supportati:

- selezione delle cinque famiglie;
- selezione dei figli dei Modificatori;
- Capitano disattivabile;
- Culometro abilitabile solo esplicitamente;
- generazione JavaScript;
- pubblicazione.

Il percorso legacy della GUI resta disponibile durante la migrazione.

## Manifest 2.0

E' implementato il writer di:

fcmRecordsNext_Manifest.js

Il manifest contiene almeno:

- versione programma e schema;
- lega;
- stagione corrente;
- stagioni elaborate;
- famiglie richieste;
- famiglie eseguibili;
- figli generati;
- figli saltati;
- dipendenze mancanti;
- file prodotti;
- stato Culometro;
- riepilogo preflight.

Il manifest viene:

- generato nella staging;
- validato;
- incluso nel conteggio dei file;
- pubblicato insieme agli altri JavaScript;
- protetto dal rollback transazionale esistente.

## Test

Ultimo stato verificato dall'utente:

- 22 test eseguiti;
- 0 failure;
- 0 errori;
- BUILD SUCCESS.

## Non ancora implementato

Non sono ancora implementati come nuovi elaboratori nativi 2.0:

- Serie;
- Modificatori;
- Soglie e Fortuna;
- Culometro;
- nuovo output Classici completo secondo lo schema 2.0;
- nuovo output RU completo secondo lo schema 2.0;
- fcmRecordsNext_Core.js;
- GUI definitiva a cinque famiglie;
- viste HTML 2.0 definitive;
- installer 2.0.

Gli esportatori Classici e RU della 1.0.2 restano operativi come ponte.

## Regole da non perdere

- Non riscrivere accesso FCM/FCA, mapping, normalizzazione e consolidamento senza una necessità verificata.
- Basarsi sul codice reale di RecordsNext 1.0.2.
- Tutti i JavaScript pubblici vanno nella cartella js del sito FCM.
- Nella root del sito deve esserci un solo HTML indice.
- Le viste e gli asset vanno nella cartella RecordsNext.
- Ogni record riferito a una partita specifica conserva il link al tabellino.
- Squadre e competizioni conservano identità stagionale e canonica.
- Gli output devono essere viste dati complete, non top list tagliate.
- Gli ZIP temporanei applicati, testati e committati vanno eliminati periodicamente da D:\DEV_APPS\downloads.
