# RecordsNext 2.0.0

**RecordsNext by mauz79** è un'applicazione per Fantacalcio Manager che legge gli archivi FCM/FCA, consolida i dati storici della lega e genera record e viste statistiche pubblicabili sul sito FCM.

La versione 2.0 introduce una GUI dedicata, configurazione guidata delle stagioni e delle associazioni storiche, famiglie di record elaborabili separatamente, output JavaScript modulari e visualizzatori HTML.

## Requisiti

- Windows
- Java 21 o superiore
- Fantacalcio Manager e relativi file `.fcm` / `.fca`
- per la pubblicazione: cartella del sito FCM locale

La distribuzione include **UCanAccess 2.0.9.5** e le dipendenze necessarie alla lettura dei database FCM/FCA.

## Avvio rapido

1. Estrarre l'intero archivio `RecordsNext_2.0.0.zip` in una cartella.
2. Avviare `RecordsNext.bat`.
3. Aprire **Configurazione stagioni**.
4. Configurare la lega e almeno una stagione gestita.
5. Salvare la configurazione.
6. Completare le associazioni storiche quando richiesto.
7. Configurare le famiglie di record.
8. Avviare l'elaborazione.

`RecordsNext.bat`, `RecordsNext.jar` e la directory `runtime` devono restare nella struttura fornita dalla release.

## Dashboard

![Dashboard](docs/screenshots/01_dashboard.png)

La Dashboard è il punto di ingresso principale. Mostra lo stato della configurazione e consente di accedere a configurazione stagioni, famiglie record, strumenti e diagnostica.

## Configurazione stagioni

![Configurazione stagioni](docs/screenshots/02_configurazione_stagioni.png)

Per la lega vengono definiti **Nome lega** e **ID lega**.

Per ogni stagione gestita possono essere configurati file FCM/FCA, sito locale, sito online, DataA.js, tabellini e associazioni storiche.

![Stagioni configurate](docs/screenshots/12_stagioni_configurate.png)

### Aggiungere una stagione

![Aggiunta stagione](docs/screenshots/03_aggiungi_stagione.png)

Per una stagione gestita selezionare FCM e FCA. Quando disponibili, stagione e numero vengono letti dal file FCM.

![Aggiunta stagione compilata](docs/screenshots/04_aggiungi_stagione_compilata.png)

Le stagioni prive di FCM possono essere mantenute come **MANUALI**: servono alla successione storica ma non vengono elaborate come stagioni FCM.

## Associazioni storiche

### Squadre

![Associazioni squadre](docs/screenshots/05_associazioni_squadre.png)

### Competizioni

![Associazioni competizioni](docs/screenshots/06_associazioni_competizioni.png)

Le associazioni permettono di collegare nomi stagionali differenti alla stessa identità storica/canonica.

## Famiglie di record

### Classici

![Famiglia Classici](docs/screenshots/07_famiglia_classici.png)

### Modificatori

![Famiglia Modificatori](docs/screenshots/08_famiglia_modificatori.png)

Le famiglie disponibili comprendono:

- Classici
- Serie
- Riserve d'Ufficio
- Modificatori
- Soglie e Fortuna
- Culometro

## Culometro

Il **Culometro** è opzionale e viene generato soltanto quando viene esplicitamente abilitato.

![Generazione Culometro](docs/screenshots/14_culometro_generazione.png)

La configurazione permette di scegliere un profilo semplice oppure intervenire sui parametri avanzati, sui pesi dei singoli fattori e sulle etichette.

### Profilo semplice e slider principali

![Culometro - profilo semplice](docs/screenshots/15_culometro_profilo_semplice.png)

Gli slider mostrano il fondoscala e il valore selezionato, così è immediato capire quanto il parametro è vicino al minimo o al massimo ammesso.

### Parametri avanzati

![Culometro - parametri avanzati](docs/screenshots/16_culometro_avanzato.png)

Anche i parametri avanzati mostrano minimo, massimo e valore corrente.

### Pesi dei fattori

![Pesi Culometro](docs/screenshots/17_culometro_pesi_fattori.png)

Il viewer dispone anche della vista **Eventi**, con filtro dedicato per il tipo di evento.

![Viewer Culometro](docs/screenshots/20_culometro_viewer_eventi.png)

La spiegazione completa di normalizzazione, rarità, sovrapposizioni, pesi, soglie ed etichette è in:

**[docs/CULOMETRO.md](docs/CULOMETRO.md)**

## Modalità di elaborazione

### Consolidata

È la modalità normale per l'aggiornamento durante la stagione.

![Modalità consolidata](docs/screenshots/10_modalita_consolidata.png)

### Completa

Rigenera i dati derivati di tutte le stagioni gestite usando la logica corrente. Le stagioni manuali non vengono elaborate come FCM.

## Log e diagnostica

![Log e diagnostica](docs/screenshots/11_log_diagnostica.png)

La pagina mostra fasi della pipeline, tempi e messaggi utili alla diagnosi.

## Pubblicazione e visualizzatori HTML

![Installazione HTML](docs/screenshots/09_debug_installazione_html.png)

I visualizzatori sono statici e caricano i dati JavaScript generati da RecordsNext.

Gli output principali sono:

```text
fcmRecordsNext_Core.js
fcmRecordsNext_Manifest.js
fcmRecordsNext_Classics.js
fcmRecordsNext_Series.js
fcmRecordsNext_RU.js
fcmRecordsNext_Modifiers.js
fcmRecordsNext_ThresholdsLuck.js
fcmRecordsNext_Culometro.js
```

I JS pubblici sono destinati alla cartella `js` del sito FCM.

Sono inclusi i profili:

- `mauzstrom`
- `fantablue2`
- `neutral`

Il profilo `mauzstrom` usa **Trebuchet MS**.

## Tabellini

Ogni stagione può essere associata a sito locale e sito online. Sono supportate pagine risultato `ris.htm`, `ris.html` e `ris.php` quando coerenti con il sito configurato.

## Aggiornamento ordinario

1. aggiornare Fantacalcio Manager;
2. avviare RecordsNext;
3. scegliere **Consolidata**;
4. elaborare;
5. generare/pubblicare i JS;
6. controllare i visualizzatori.

## Documentazione

- `README.md` — panoramica illustrata
- `INSTALL.txt` — installazione e primo avvio
- `CHANGELOG.md` — contenuti della release
- `docs\INSTALLAZIONE_VISUALIZZATORI_HTML.md` — pubblicazione dei visualizzatori
- `docs\CULOMETRO.md` — funzionamento e configurazione del Culometro
- `docs\screenshots\` — schermate di riferimento

## Autore

**RecordsNext by mauz79**
