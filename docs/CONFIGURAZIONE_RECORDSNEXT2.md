# Configurazione RecordsNext 3.1

## Aggiornamento configurazione 3.1

### Sito locale opzionale

Per ogni stagione gestita:

- FCM: obbligatorio;
- FCA: obbligatorio;
- sito locale: opzionale;
- sito online: opzionale.

Se `local_site_path` e' valorizzato deve indicare una directory esistente. Se e' vuoto la stagione resta valida e continua a partecipare allo storico, ma non costituisce un target di pubblicazione.

La cartella JS e' sempre derivata da:

```text
<local_site_path>/js
```

### Calendari DataA

Il DataA storico non dipende piu' dalla presenza del sito.

Percorso canonico:

```text
data/calendars/DataA-YYYY.js
```

Esempi:

```text
2024_2025 -> data/calendars/DataA-2024.js
2025_2026 -> data/calendars/DataA-2025.js
2026_2027 -> data/calendars/DataA-2026.js
```

La GUI segnala separatamente:

- `DataA archivio trovato`;
- `DataA archivio non trovato`.

### Pubblicazione

La GUI 3.1 espone:

- `Pubblica nel sito della stagione corrente al termine`;
- `Pubblica i siti delle stagioni selezionate`.

La seconda azione usa le checkbox `Elabora` come ambito esplicito dei target storici da pubblicare. Una stagione selezionata ma senza sito continua a contribuire ai dati; semplicemente non produce una pubblicazione propria.

E' stato rimosso il fallback hardcoded verso `E:/fantacalcio/Lega2025/js`. In assenza di un target configurato, RecordsNext non deve scegliere autonomamente un sito reale.


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
