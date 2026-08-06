# Architettura visualizzatori HTML RecordsNext 2.0

## Scopo

I visualizzatori RecordsNext offrono pagine gia utili per un sito FCM semplice e una base tecnica per personalizzazioni autonome.

## Vincolo principale

Nessun HTML distribuito con RecordsNext contiene dati della lega. Tutti i dati provengono dai file `fcmRecordsNext_*.js` generati dalla pipeline.

## Struttura prevista

```text
recordsnext.html
RecordsNext\classici.html
RecordsNext\serie.html
RecordsNext\riserve_ufficio.html
RecordsNext\modificatori.html
RecordsNext\soglie_fortuna.html
RecordsNext\culometro.html
RecordsNext\recordsnext.css
js\fcmRecordsNextFunzioni_common.js
js\fcmRecordsNextFunzioni_viewer.js
```

## Responsabilita

### HTML

Definisce titolo, navigazione, filtri, contenitori e stati vuoti. Non contiene dataset o risultati.

### CSS

Definisce esclusivamente la resa grafica. Gli HTML usano classi stabili con prefisso `rn_`.

### `fcmRecordsNextFunzioni_common.js`

Gestisce funzioni condivise: rilevamento output disponibili, formattazione, accesso sicuro alle variabili globali, escape, link e stati errore.

### `fcmRecordsNextFunzioni_viewer.js`

Gestisce filtri, ordinamenti, selezione delle viste e rendering di tabelle o schede.

### JS dati

Sono prodotti dall'elaborazione, pubblicati nella cartella `js` del sito e non distribuiti come file statici della skin.

## Profili grafici

- `mauzstrom`: Trebuchet MS, adattamento alla relativa skin;
- `fantablue2`: adattamento alla skin Fantablue2;
- `neutral`: stile moderno autonomo.

Gli HTML non cambiano fra un profilo e l'altro.

## Assenza di dati

Ogni pagina deve poter essere presente anche quando il relativo output non e stato generato. In quel caso mostra un messaggio leggibile e non genera eccezioni.

## Culometro

`culometro.html` e sempre un visualizzatore statico. Il relativo JS dati esiste solo quando il Culometro e stato richiesto ed elaborato.
