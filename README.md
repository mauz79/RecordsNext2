# RecordsNext 2.0

RecordsNext 2.0 e un nuovo progetto indipendente che genera viste dati modulari, complete e filtrabili, dalle quali ricavare record stagionali, assoluti, globali e personali.

## Directory

`D:\DEV_APPS\RecordsNext2.0`

## Repository di riferimento

- https://github.com/mauz79/RecordsNext
- https://github.com/mauz79/ConfrontiStorici-3.x-Plus
- https://github.com/mauz79/ConfrontiStorici34

I download manuali vanno salvati in `D:\DEV_APPS\downloads`.

## Famiglie iniziali

1. Classici
2. Serie
3. Riserve d'Ufficio
4. Modificatori
5. Soglie e Fortuna

Il Culometro e un easter egg opzionale, generato soltanto su richiesta esplicita e con configurazione dedicata.

## Output previsti

- `fcmRecordsNext_Core.js`
- `fcmRecordsNext_Manifest.js`
- `fcmRecordsNext_Classics.js`
- `fcmRecordsNext_Series.js`
- `fcmRecordsNext_RU.js`
- `fcmRecordsNext_Modifiers.js`
- `fcmRecordsNext_ThresholdsLuck.js`
- `fcmRecordsNext_Culometro.js`

Tutti i JS pubblici andranno nella cartella `js` del sito FCM. Nella root ci sara un solo `recordsnext.html`; le pagine visualizzatore e il relativo CSS andranno nella cartella `RecordsNext`.

## Visualizzatori HTML

Gli HTML distribuiti con RecordsNext 2.0 non contengono dati incorporati. Sono visualizzatori statici e riutilizzabili dei file `fcmRecordsNext_*.js` prodotti dall'elaborazione.

Separazione ufficiale:

- HTML: struttura e contenitori;
- CSS: profilo grafico;
- `fcmRecordsNextFunzioni_common.js` e `fcmRecordsNextFunzioni_viewer.js`: funzioni di lettura e rendering;
- `fcmRecordsNext_*.js`: dati e metadati generati.

I due JS di visualizzazione vengono installati nella cartella `js` della skin, in modo che FCM li riporti nella cartella `js` del sito generato.

Profili grafici iniziali:

- `mauzstrom`, con font principale Trebuchet MS;
- `fantablue2`;
- `neutral`, moderno e autonomo, ispirato alla leggibilita della skin ReNewo senza dipendere dai suoi selettori.

Gli stessi HTML e gli stessi JS di rendering devono funzionare con tutti i profili. Cambia soltanto `RecordsNext\recordsnext.css`.

## Bibbia

La bibbia del progetto e `docs\CODICE_FUNZIONANTE_RECORDSNEXT2.md`. Deve distinguere decisioni consolidate, codice implementato e verificato, lavori in corso e questioni aperte.
