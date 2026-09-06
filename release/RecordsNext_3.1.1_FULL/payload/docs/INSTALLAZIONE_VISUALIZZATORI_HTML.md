# Installazione visualizzatori HTML — RecordsNext 2.1.0

I visualizzatori RecordsNext sono file statici e non contengono i dati della lega.

## Struttura sul sito FCM

Copiare nella root del sito:

```text
recordsnext.html
```

Copiare la cartella:

```text
RecordsNext\
```

nella root del sito.

Copiare nella cartella `js` del sito:

```text
fcmRecordsNextFunzioni_common.js
fcmRecordsNextFunzioni_viewer.js
```

I file dati generati da RecordsNext devono trovarsi nella stessa cartella `js`:

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

Sono presenti solo i file relativi alle famiglie effettivamente generate.

## Struttura finale

```text
<root sito>\
│
├─ recordsnext.html
├─ js\
│  ├─ fcmRecordsNext_Core.js
│  ├─ fcmRecordsNext_Manifest.js
│  ├─ fcmRecordsNext_Classics.js
│  ├─ fcmRecordsNext_Series.js
│  ├─ fcmRecordsNext_RU.js
│  ├─ fcmRecordsNext_Modifiers.js
│  ├─ fcmRecordsNext_ThresholdsLuck.js
│  ├─ fcmRecordsNext_Culometro.js
│  ├─ fcmRecordsNextFunzioni_common.js
│  └─ fcmRecordsNextFunzioni_viewer.js
│
└─ RecordsNext\
   ├─ classici.html
   ├─ serie.html
   ├─ riserve_ufficio.html
   ├─ modificatori.html
   ├─ soglie_fortuna.html
   ├─ culometro.html
   ├─ record_di_lega.html
   └─ recordsnext.css
```

## Profili grafici

La release include:

```text
profiles\mauzstrom\recordsnext.css
profiles\fantablue2\recordsnext.css
profiles\neutral\recordsnext.css
```

`mauzstrom` usa Trebuchet MS.

## Dati e aggiornamenti

Dopo ogni elaborazione della lega è sufficiente aggiornare i file `fcmRecordsNext_*.js` generati.

I file HTML e le funzioni viewer cambiano solo quando viene aggiornata la release.

## Tabellini

I link ai tabellini dipendono dalla configurazione della stagione e dal sito associato.

Sono supportate pagine risultato:

```text
ris.htm
ris.html
ris.php
```

## Controllo rapido

Aprire:

```text
recordsnext.html
```

e verificare:

- disponibilità delle famiglie;
- filtri stagione/competizione;
- Classici;
- Serie;
- RU;
- Modificatori;
- Soglie/Fortuna;
- Culometro;
- Record di lega;
- link tabellini.

Per schermate di riferimento vedere:

```text
docs\screenshots\
```
