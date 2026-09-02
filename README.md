# RecordsNext 3.1

**RecordsNext by mauz79** è il generatore di record e statistiche storiche per leghe gestite con Fantacalcio Manager.

RecordsNext legge i dati delle stagioni dai file FCM/FCA, mantiene uno storico normalizzato nel proprio database e genera file JavaScript pronti per essere usati dal sito della lega. La 3.1 introduce la pubblicazione multistagione/multisito: ogni vecchio sito può essere riallineato con i record disponibili **fino a quella stagione**, senza ricevere dati futuri.

![Dashboard RecordsNext 3.1](docs/screenshots/01-dashboard.png)

## Indice

- [Cosa fa RecordsNext](#cosa-fa-recordsnext)
- [Installazione](#installazione)
- [Concetti fondamentali](#concetti-fondamentali)
- [Prima configurazione](#prima-configurazione)
- [Schermate e funzioni della GUI](#schermate-e-funzioni-della-gui)
- [Famiglie di record](#famiglie-di-record)
- [Culometro](#culometro)
- [Elaborazione completa e consolidata](#elaborazione-completa-e-consolidata)
- [Pubblicazione](#pubblicazione)
- [Output JavaScript](#output-javascript)
- [Calendari DataA](#calendari-dataa)
- [Tabellini storici](#tabellini-storici)
- [Flusso d'uso consigliato](#flusso-duso-consigliato)
- [Stato della release 3.1](#stato-della-release-31)

## Cosa fa RecordsNext

RecordsNext centralizza e rende riutilizzabile lo storico della lega. In particolare:

- importa le stagioni gestite da FCM/FCA;
- mantiene identità canoniche di squadre e competizioni anche quando i nomi cambiano negli anni;
- usa calendari DataA canonici interni;
- genera record classici e serie statistiche;
- genera statistiche sulle Riserve d'Ufficio;
- genera record e serie sui Modificatori;
- genera Soglie e indicatori di Fortuna;
- genera il Culometro, quando attivato;
- esporta un dataset partita-per-partita in `fcmRecordsNext_Matches.js`;
- collega i record ai tabellini storici quando disponibili;
- pubblica i JavaScript nel sito FCM corrente;
- può riallineare contemporaneamente più siti storici.

Il principio architetturale della 3.1 è:

> **FCM, FCA e DataA definiscono lo storico. I siti sono destinazioni opzionali di pubblicazione.**

Un vecchio sito quindi non deve esistere perché una stagione resti valida nello storico di RecordsNext.

## Installazione

La release pubblica 3.1 viene distribuita come **installazione pulita** tramite:

`RecordsNext_3.1.0_SETUP.exe`

Requisiti:

- Windows;
- Java 21 o superiore;
- Fantacalcio Manager;
- file FCM/FCA delle stagioni da importare.

Durante il setup viene sempre mostrata la cartella di destinazione. La convenzione consigliata è:

`<cartella FCM>\plugin\RecordsNext`

L'installazione include programma, runtime necessario, visualizzatori e archivio calendari DataA 1991-2026.

Non include:

- database personali;
- file FCM/FCA dell'utente;
- configurazioni della lega;
- pubblicazioni automatiche nei siti.

Per i dettagli vedere [INSTALL.md](INSTALL.md).

## Concetti fondamentali

### Stagione gestita

Una stagione **Gestita** è alimentata dai file FCM/FCA.

Richiede:

- FCM;
- FCA.

Può avere, ma non deve avere:

- sito locale;
- sito online.

Il sito locale serve solo come destinazione di pubblicazione. La sua cartella `js` deriva sempre da:

`<cartella sito>\js`

### Stagione manuale

Una stagione **Manuale** serve a rappresentare una stagione che deve esistere nello storico/configurazione ma non è importabile tramite FCM/FCA.

Richiede soltanto:

- anni stagione nel formato `AAAA/AAAA`;
- numero stagione.

### Stagione corrente

La stagione corrente è l'ancora operativa del sito. Nell'uso normale RecordsNext pubblica solo nel sito associato a questa stagione.

### Sito locale

È la root del sito FCM sul disco. Esempio:

`E:\fantacalcio\Lega2026`

RecordsNext pubblicherà i JS in:

`E:\fantacalcio\Lega2026\js`

### Sito online

Serve a costruire collegamenti web, in particolare verso i tabellini.

RecordsNext normalizza l'URL:

- converte `\` in `/`;
- se il protocollo manca aggiunge `http://`;
- conserva un eventuale `https://` inserito esplicitamente;
- rimuove slash finali inutili.

Esempio:

`www.example.org\lega2026`

diventa:

`http://www.example.org/lega2026`

## Prima configurazione

Aprire **Configurazione stagioni**.

![Configurazione stagione](docs/screenshots/02-configurazione-stagione.png)

Per ogni stagione gestita indicare FCM e FCA. Il sito locale può essere lasciato vuoto se quella stagione deve contribuire allo storico ma non deve ricevere file pubblicati.

Il nome lega può essere lasciato vuoto: RecordsNext prova a ricavarlo dal primo FCM gestito. L'identificativo tecnico della lega resta interno e non viene richiesto all'utente.

La GUI mostra anche lo stato del calendario e dei tabellini disponibili.

## Schermate e funzioni della GUI

### Panoramica

La dashboard riassume:

- lega corrente;
- numero di stagioni configurate;
- famiglie abilitate;
- stato di Soglie/Fortuna;
- stato del Culometro;
- modalità di elaborazione;
- opzioni di pubblicazione.

### Configurazione stagioni

Serve per:

- aggiungere stagioni gestite;
- aggiungere stagioni manuali;
- modificare sorgenti e siti;
- scegliere la stagione corrente;
- verificare calendario e tabellini;
- eliminare una stagione con pulizia coerente dei dati collegati.

La cancellazione di una stagione non è una semplice rimozione dalla GUI: RecordsNext elimina i dati stagionali collegati e riancora, quando necessario, le identità canoniche condivise.

### Famiglie record

La pagina consente di attivare o disattivare intere famiglie e, dove previsto, i singoli sottorecord.

![Famiglia Classici](docs/screenshots/03-famiglia-classici.png)

### Modificatori

La sezione Modificatori permette di configurare i nomi dei modificatori personalizzati e scegliere quali elaborazioni produrre, tra cui massimo, totale, media, utilizzi e relative serie quando disponibili.

### Soglie e Fortuna

Questa pagina abilita le elaborazioni basate sulle soglie dei punteggi e gli indicatori di fortuna.

![Soglie e Fortuna](docs/screenshots/04-soglie-fortuna.png)

### Debug / Pubblicazione

Questa pagina raccoglie le funzioni operative e di verifica, comprese le azioni di pubblicazione corrente e multisito.

![Pubblicazione multisito](docs/screenshots/06-pubblicazione-multisito.png)

## Famiglie di record

RecordsNext 3.1 produce le seguenti famiglie canoniche.

### Classici

Record tradizionali ricavati dalle partite e dai punteggi storici. La selezione è granulare: è possibile abilitare solo i record che interessano.

### Serie

Serie positive e negative ricavate dalla successione delle partite e dai risultati. Le serie relative ai modificatori seguono la configurazione della famiglia Modificatori.

### Riserve d'Ufficio

Statistiche e record relativi all'impiego delle RU. La famiglia mantiene una propria configurazione granulare.

### Modificatori

Record aggregati e serie sui modificatori disponibili nei dati FCM/FCA. I modificatori personalizzati possono essere rinominati in GUI.

### Soglie e Fortuna

Raggruppa elaborazioni che confrontano punteggi, esiti e soglie per evidenziare situazioni statisticamente favorevoli o sfavorevoli.

## Culometro

Il Culometro è un'elaborazione separata e opzionale.

![Configurazione Culometro](docs/screenshots/05-culometro-configurazione.png)

La configurazione permette di scegliere quali componenti partecipano al calcolo e con quale peso. I dettagli esportati comprendono, per ogni evento disponibile, i valori necessari alla spiegazione del risultato, comprese frequenze storiche e impatto.

Il Culometro può risultare **parziale** se mancano dipendenze opzionali. Questo non equivale a un errore dell'intera elaborazione.

## Elaborazione completa e consolidata

### Completa

Ricostruisce i dati gestiti e gli archivi derivati. È la modalità da usare:

- alla prima importazione;
- dopo modifiche strutturali;
- dopo cambi importanti nella configurazione;
- quando si vuole ricostruire integralmente lo storico.

### Consolidata

È la modalità ordinaria dopo un aggiornamento del sito/FCM. Riutilizza ciò che non è cambiato e aggiorna ciò che serve.

È la modalità consigliata per l'uso giornata per giornata.

## Pubblicazione

RecordsNext 3.1 distingue due operazioni.

### Pubblica nel sito della stagione corrente al termine

È l'opzione normale.

Usarla quando si aggiorna la stagione corrente dopo una nuova giornata. Al termine dell'elaborazione RecordsNext pubblica i JS nel solo sito configurato come corrente.

### Pubblica i siti delle stagioni selezionate

È un'operazione di manutenzione/riallineamento storico.

Usarla quando:

- cambia una logica globale;
- vengono corrette identità o competizioni;
- viene introdotto un nuovo dataset;
- si vuole riallineare lo storico dopo un aggiornamento di RecordsNext.

Le checkbox delle stagioni determinano i target da pubblicare.

La regola fondamentale è il **cutoff storico**: ogni sito riceve soltanto le stagioni con `sort_order` minore o uguale al proprio.

Esempio:

- sito 2024/25 → dati fino al 2024/25;
- sito 2025/26 → dati fino al 2025/26;
- sito 2026/27 → dati fino al 2026/27.

Un sito storico non può quindi ricevere squadre, competizioni o risultati futuri.

## Output JavaScript

RecordsNext pubblica nove dataset canonici:

| File | Contenuto |
| --- | --- |
| `fcmRecordsNext_Core.js` | metadati, stagioni, squadre, competizioni, identità canoniche e mapping |
| `fcmRecordsNext_Classics.js` | record classici |
| `fcmRecordsNext_Series.js` | serie statistiche |
| `fcmRecordsNext_RU.js` | record Riserve d'Ufficio |
| `fcmRecordsNext_Modifiers.js` | record e statistiche Modificatori |
| `fcmRecordsNext_ThresholdsLuck.js` | Soglie e indicatori di Fortuna |
| `fcmRecordsNext_Culometro.js` | dati del Culometro |
| `fcmRecordsNext_Matches.js` | dataset partita-per-partita |
| `fcmRecordsNext_Manifest.js` | manifest della pubblicazione e stagione target |

### Matches

`fcmRecordsNext_Matches.js` contiene una riga per squadra per incontro, quindi normalmente **due righe per partita**.

Tra i campi canonici sono previsti:

- stagione;
- competizione;
- match;
- giornata/round;
- squadra;
- avversario;
- gol fatti/subiti;
- risultato standardizzato `V/N/P`;
- informazioni necessarie ai record;
- collegamento al tabellino quando disponibile.

I turni di riposo non vengono rappresentati come partite.

## Calendari DataA

L'archivio canonico è interno a RecordsNext:

`data\calendars\DataA-YYYY.js`

La release 3.1 include i calendari:

`DataA-1991.js` … `DataA-2026.js`

I formati storici non vengono distruttivamente uniformati: RecordsNext conserva i file originali compatibili con le varie stagioni.

Se il DataA canonico manca e per quella stagione è configurato un sito locale con `js\DataA.js`, RecordsNext può recuperarlo e copiarlo nell'archivio interno.

## Tabellini storici

I tabellini possono essere individuati nei formati compatibili con i siti FCM, tra cui:

- `ris*.htm`;
- `ris*.html`;
- `ris*.php`.

Il collegamento online usa il sito online configurato per la stagione corretta, evitando che un risultato storico venga aperto sotto la cartella della stagione corrente.

## Flusso d'uso consigliato

### Prima installazione

1. Installare RecordsNext.
2. Avviare `RecordsNext.bat`.
3. Configurare la lega.
4. Aggiungere le stagioni gestite con FCM/FCA.
5. Associare i siti locali solo alle stagioni che devono essere pubblicate.
6. Configurare le famiglie record.
7. Eseguire una elaborazione **Completa**.
8. Verificare l'output.
9. Se necessario, pubblicare i siti storici selezionati.

### Aggiornamento dopo una giornata

1. Aggiornare normalmente FCM e il sito.
2. Avviare RecordsNext.
3. Usare **Consolidata**.
4. Selezionare **Pubblica nel sito della stagione corrente al termine**.
5. Premere **Elabora**.

### Dopo una modifica strutturale

1. Eseguire una elaborazione completa.
2. Verificare preflight e output.
3. Selezionare le stagioni che hanno un sito da riallineare.
4. Premere **Pubblica i siti delle stagioni selezionate**.

## Stato della release 3.1

La 3.1.0 è stata verificata con:

- suite automatica: **50 test, 0 failure, 0 errori**;
- build Maven completata con successo;
- pubblicazione multisito reale sulle stagioni gestite dal 2006/07 al 2026/27;
- **21 siti target**;
- **189 file validati e 189 pubblicati**;
- verifica del cutoff storico senza contaminazione da stagioni future;
- setup Windows compilato con Inno Setup 7.

SHA256 del setup 3.1.0 pubblicato:

`C39B0EA205F7D81660CD1FF09EC3F6014090932FF3E0D4DA721870D2740CA6EB`

## Versione

RecordsNext 3.1.0
RecordsNext by mauz79
