# Installazione visualizzatori HTML RecordsNext 2.0

## Destinatari

Questa guida e destinata all'utente che vuole installare le pagine RecordsNext in una skin FCM.

## Procedura prevista nella GUI

1. Aprire la sezione di installazione dei visualizzatori.
2. Selezionare la cartella principale della skin FCM.
3. Scegliere il profilo `mauzstrom`, `fantablue2` oppure `neutral`.
4. Verificare le destinazioni proposte.
5. Avviare `Installa visualizzatori RecordsNext nella skin`.

## File copiati

```text
recordsnext.html
RecordsNext\*.html
RecordsNext\recordsnext.css
js\fcmRecordsNextFunzioni_common.js
js\fcmRecordsNextFunzioni_viewer.js
```

I file dati `fcmRecordsNext_*.js` non vengono copiati dall'installer: sono generati dall'elaborazione.

## Aggiornamento del sito

Dopo l'installazione nella skin, FCM riporta gli asset statici nel sito generato. RecordsNext aggiorna invece i JS dati nella cartella `js` del sito.

## Famiglie non generate

Le pagine possono essere presenti anche quando una famiglia non e stata elaborata. Il visualizzatore mostra uno stato vuoto e il collegamento puo essere disabilitato dall'indice tramite il manifest.

## Aggiornamento dei visualizzatori

Una nuova versione puo sostituire HTML, JS statici e CSS senza toccare i JS dati. Prima della sovrascrittura l'installer dovra creare un backup mirato dei file RecordsNext esistenti.
