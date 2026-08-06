# Changelog

## 2.0.0-dev

### Fondazione

- creato progetto separato RecordsNext2.0;
- importata la base funzionante RecordsNext 1.0.2;
- confermato accesso FCM/FCA tramite UCanAccess;
- mantenuti SQLite, mapping canonici, normalizzazione, consolidamento e pubblicazione.

### Architettura modulare

- definite cinque famiglie elaborabili;
- introdotti figli, dipendenze e stati degli output;
- introdotta selezione modulare;
- mantenuta compatibilita temporanea con Classici e RU legacy;
- introdotti planner e preflight;
- collegato config\processing.json alla pipeline.

### Manifest

- introdotto fcmRecordsNext_Manifest.js;
- integrata generazione nella staging;
- integrata validazione;
- integrata pubblicazione con rollback.

### Modificatori

- introdotta la configurazione gerarchica per modificatore e tipo di statistica;
- aggiunti Massimo, Totale, Media e Utilizzi;
- distinti i modificatori personalizzati dai modificatori standard FCM;
- aggiunti i nomi configurabili per `MODM1PERS`, `MODM2PERS` e `MODM3PERS`;
- aggiunta la lettura dei campi standard `MODPORTIERE`, `MODDIFESA`, `MODCENTROCAMPO`, `MODATTACCO` e `MODMODULO`;
- collegata la famiglia Modificatori direttamente agli archivi `season_records_*.json`;
- aggiunti i metadati distinti `availableSections` e `generatedSections`;
- verificata l’esportazione del Modificatore Difesa FCM nella stagione 2006_2007;
- verificata l’esclusione delle statistiche disattivate.

### Verifica

- 38 test eseguiti;
- 0 failure;
- 0 errori;
- BUILD SUCCESS;
- output JavaScript reale controllato con tutte le sezioni selezionate presenti.


### GUI e visualizzatori

- evoluta la dashboard GUI e la configurazione granulare delle famiglie;
- raggruppati Soglie, Fortuna e Culometro nella stessa area della GUI;
- consolidata la regola che gli HTML non contengono dati incorporati;
- definiti i JS statici `fcmRecordsNextFunzioni_common.js` e `fcmRecordsNextFunzioni_viewer.js`;
- stabilita l'installazione dei JS statici nella cartella `js` della skin;
- adottato l'underscore per i nuovi nomi file;
- definiti i profili grafici `mauzstrom`, `fantablue2` e `neutral`;
- stabilito Trebuchet MS come font principale del profilo `mauzstrom`;
- progettato il profilo `neutral` moderno ispirato ai principi grafici di ReNewo;
- aggiunta la documentazione di architettura, installazione e personalizzazione dei visualizzatori.
