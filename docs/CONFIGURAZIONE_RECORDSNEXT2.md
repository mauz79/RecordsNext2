# Configurazione RecordsNext 2.0

## Scopo

Questi file costituiscono la prima configurazione concreta del progetto.

## File

- `config/league.json`: identità generale della lega.
- `config/seasons.json`: stagioni gestite, manuali e correnti.
- `config/teams.json`: squadre canoniche e squadre stagionali.
- `config/competitions.json`: competizioni canoniche e stagionali.
- `config/processing.json`: famiglie e figli richiesti.
- `config/culometro.json`: configurazione separata dell'easter egg.
- `config/manifest.example.json`: forma preliminare del manifest prodotto.

## Configurazione granulare dei Modificatori

La sezione Modificatori di `config/processing.json` conserva separatamente le selezioni per ogni campo disponibile.

Per ogni modificatore sono configurabili:

- `max`;
- `total`;
- `average`;
- `uses`.

I modificatori personalizzati sono:

- `modm1pers`;
- `modm2pers`;
- `modm3pers`.

I relativi nomi visualizzati sono configurabili nella GUI e vengono salvati nella configurazione.

I modificatori standard FCM sono:

- `modportiere`;
- `moddifesa`;
- `modcentrocampo`;
- `modattacco`;
- `modmodulo`.

I nomi dei modificatori standard FCM restano espliciti e non devono essere confusi con quelli personalizzati.

## Regole consolidate

- Il file FCM e il file FCA sono configurati per stagione.
- La cartella `js` non viene configurata separatamente: è interna alla root del sito.
- Ogni stagione può avere sito locale e online.
- Il nome della pagina tabellino è configurato per stagione.
- Squadre e competizioni conservano identità stagionale e canonica.
- Le famiglie possono essere elaborate separatamente.
- Ogni figlio può dipendere da altri dati o moduli.
- Il Capitano può essere disattivato senza bloccare le altre Serie.
- Il Culometro è disattivato per impostazione predefinita.
- Il Culometro richiede configurazione esplicita.
- Le competizioni canoniche rispettano l'ordine stabilito.
- Play Off e Play Out non appartengono all'ordine principale.

## Nota importante

I valori presenti sono iniziali o di esempio. Prima dell'elaborazione reale sarà necessario importare e verificare:

- tutte le stagioni;
- i percorsi FCM/FCA;
- le root locali e online;
- il formato del tabellino di ogni stagione;
- i mapping delle squadre;
- i mapping delle competizioni.


## Installazione dei visualizzatori nella skin

La configurazione dell'installer deve prevedere:

- cartella della skin FCM;
- profilo grafico da installare;
- conferma della struttura di destinazione;
- sovrascrittura controllata dei soli file RecordsNext.

Profili iniziali ammessi:

- `mauzstrom`;
- `fantablue2`;
- `neutral`.

Struttura dei profili nel progetto:

```text
templates\skins\mauzstrom\recordsnext.css
templates\skins\fantablue2\recordsnext.css
templates\skins\neutral\recordsnext.css
```

Il profilo `mauzstrom` usa Trebuchet MS come font principale.

La procedura `Installa visualizzatori RecordsNext nella skin` deve copiare:

```text
recordsnext.html                         -> root della skin
RecordsNext\*.html                     -> RecordsNext della skin
RecordsNext\recordsnext.css            -> RecordsNext della skin
js\fcmRecordsNextFunzioni_common.js    -> js della skin
js\fcmRecordsNextFunzioni_viewer.js    -> js della skin
```

Non deve copiare i file dati `fcmRecordsNext_*.js`, che vengono generati dalla pipeline.
