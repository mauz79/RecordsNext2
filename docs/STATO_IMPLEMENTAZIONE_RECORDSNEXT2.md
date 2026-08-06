# Stato implementazione RecordsNext 2.0

Aggiornamento: 6 agosto 2026.

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

Ultimo stato verificato il 6 agosto 2026:

- 38 test eseguiti;
- 0 failure;
- 0 errori;
- 0 test saltati;
- BUILD SUCCESS.

La verifica è stata eseguita anche sull’elaborazione reale delle stagioni configurate.

## Famiglia Modificatori

La famiglia Modificatori è implementata e verificata.

La configurazione GUI permette di selezionare separatamente, per ciascun modificatore:

- Massimo;
- Totale;
- Media;
- Utilizzi.

Modificatori personalizzati gestiti:

- `MODM1PERS`, con nome configurabile;
- `MODM2PERS`, con nome configurabile;
- `MODM3PERS`, con nome configurabile.

Modificatori standard FCM gestiti separatamente:

- `MODPORTIERE`;
- `MODDIFESA`;
- `MODCENTROCAMPO`;
- `MODATTACCO`;
- `MODMODULO`.

Il file `fcmRecordsNext_Modifiers.js` viene costruito direttamente dagli archivi `season_records_*.json`, senza dipendere dall’esportatore Classici legacy.

I metadati distinguono:

- `availableSections`: sezioni supportate dall’esportatore;
- `generatedSections`: sezioni effettivamente prodotte in base alla configurazione e ai dati disponibili.

È stata verificata nell’output reale la presenza di:

- Massimo, Totale, Media e Utilizzi per `MODM1PERS`;
- Massimo, Totale, Media e Utilizzi per `MODM2PERS`;
- Massimo, Totale, Media e Utilizzi per `MODDIFESA`;
- stagione `2006_2007` nei dati del Modificatore Difesa FCM;
- nomi configurati corretti;
- esclusione delle statistiche disattivate.

## Non ancora implementato

Non sono ancora implementati come nuovi elaboratori nativi 2.0:

- Serie;
- Soglie e Fortuna;
- Culometro;
- nuovo output Classici completo secondo lo schema 2.0;
- nuovo output RU completo secondo lo schema 2.0;
- fcmRecordsNext_Core.js;
- completamento e consolidamento della GUI 2.0;
- viste HTML 2.0 definitive;
- JS statici di rendering dei visualizzatori;
- installer effettivo dei visualizzatori e dei profili CSS.

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


## GUI e visualizzatori: stato al 6 agosto 2026

La dashboard GUI e stata riorganizzata con tre aree principali:

- Configurazione stagioni;
- Famiglie record;
- Soglie, Fortuna e Culometro.

La GUI consente gia la configurazione granulare delle famiglie e del Culometro. L'avvio operativo deve includere UCanAccess nel classpath.

Sono consolidate, ma non ancora implementate integralmente, le seguenti decisioni sui visualizzatori:

- gli HTML non contengono dati;
- gli HTML leggono esclusivamente i JS pubblici;
- i JS statici di rendering saranno `fcmRecordsNextFunzioni_common.js` e `fcmRecordsNextFunzioni_viewer.js`;
- tali file saranno installati nella cartella `js` della skin;
- i nuovi nomi file usano underscore e non trattini;
- i profili grafici iniziali saranno `mauzstrom`, `fantablue2` e `neutral`;
- `mauzstrom` usera Trebuchet MS;
- il profilo `neutral` sara moderno, autonomo e ispirato ai principi grafici di ReNewo;
- la presenza degli HTML nella skin non implica la generazione dei relativi dati;
- una pagina priva del proprio JS deve mostrare uno stato vuoto senza errori.

Prossima fase:

1. definire il contratto JavaScript pubblico definitivo;
2. creare i due JS statici di rendering;
3. creare l'indice e le pagine famiglia senza dati incorporati;
4. creare e verificare i tre profili CSS;
5. implementare l'installer nella GUI;
6. aggiungere test automatici per struttura, riferimenti e assenza di dati negli HTML.
