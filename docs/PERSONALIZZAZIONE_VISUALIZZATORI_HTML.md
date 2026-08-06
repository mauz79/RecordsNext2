# Personalizzazione visualizzatori HTML RecordsNext 2.0

## Principio

La personalizzazione deve mantenere separati dati e presentazione. Non copiare dataset dentro HTML o CSS.

## Classi CSS

Le nuove pagine useranno classi con prefisso `rn_`, per esempio:

```text
rn_page
rn_shell
rn_header
rn_navigation
rn_toolbar
rn_filter
rn_panel
rn_table
rn_row_even
rn_row_odd
rn_empty_state
rn_error_state
```

## Profili

Ogni profilo fornisce un proprio `recordsnext.css` ma usa gli stessi selettori `rn_`.

Struttura sorgente:

```text
templates\skins\mauzstrom\recordsnext.css
templates\skins\fantablue2\recordsnext.css
templates\skins\neutral\recordsnext.css
```

Il profilo `mauzstrom` usa Trebuchet MS come font principale.

## JavaScript

Non modificare i file dati generati per cambiare la grafica. Le personalizzazioni funzionali vanno nei file:

```text
fcmRecordsNextFunzioni_common.js
fcmRecordsNextFunzioni_viewer.js
```

Una pagina personalizzata deve:

- verificare la disponibilita del manifest;
- verificare la disponibilita del dataset richiesto;
- gestire schema non compatibile e dati mancanti;
- applicare filtri nel browser;
- mantenere i link ai tabellini;
- evitare dipendenze dalla posizione assoluta del sito.

## Divieto di dati incorporati

Non sono ammessi:

- array di record scritti nell'HTML;
- JSON inline con dati della lega;
- classifiche statiche generate nel markup;
- nomi di squadre o stagioni inseriti come contenuto permanente della pagina.

Sono ammessi soltanto testi generici dell'interfaccia e configurazioni di visualizzazione non legate a una lega specifica.
