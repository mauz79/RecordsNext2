# Installazione RecordsNext 3.1.0

## Requisiti

- Windows
- Java 21 o superiore
- Fantacalcio Manager
- file FCM/FCA delle stagioni che si desidera importare

UCanAccess 2.0.9.5 e le dipendenze necessarie sono incluse nel setup.

## Pacchetto pubblico

La 3.1 viene distribuita come **installazione pulita**.

File:

`RecordsNext_3.1.0_SETUP.exe`

Non esiste un pacchetto UPDATE pubblico per la 3.1.

SHA256:

`C39B0EA205F7D81660CD1FF09EC3F6014090932FF3E0D4DA721870D2740CA6EB`

## Installazione

1. Avviare `RecordsNext_3.1.0_SETUP.exe`.
2. Confermare il controllo Java.
3. Scegliere la cartella di destinazione.
4. Completare il wizard.
5. Avviare RecordsNext dal collegamento creato oppure da `RecordsNext.bat`.

La schermata della cartella viene sempre mostrata. La convenzione consigliata è:

`<cartella FCM>\plugin\RecordsNext`

Esempio, se FCM è installato in `C:\FCM`:

`C:\FCM\plugin\RecordsNext`

La cartella non è hardcoded: ogni utente può avere FCM in un percorso diverso.

## Cosa installa il setup

Il setup installa:

- applicazione RecordsNext;
- launcher;
- runtime necessario;
- UCanAccess 2.0.9.5 e dipendenze;
- visualizzatori/asset distribuiti con RecordsNext;
- archivio calendari `DataA-1991.js` … `DataA-2026.js`;
- struttura delle directory dati.

Il setup non installa:

- database personali;
- file FCM/FCA;
- configurazioni della lega;
- dati provenienti da installazioni precedenti;
- file nei siti FCM dell'utente.

## Installazione pulita e vecchie versioni

La 3.1 è progettata per una installazione pulita. Una vecchia installazione può essere conservata separatamente finché la nuova non è stata verificata.

Non sovrascrivere alla cieca una vecchia installazione contenente configurazioni o dati personali.

## Primo avvio

Avviare:

`RecordsNext.bat`

Aprire **Configurazione stagioni**.

### Nome lega

Il nome lega può essere lasciato vuoto: RecordsNext prova a ricavarlo dal primo FCM gestito. L'ID tecnico della lega non viene richiesto nella GUI.

### Aggiungere una stagione gestita

Una stagione Gestita richiede:

- file FCM;
- file FCA.

Sono opzionali:

- sito locale;
- sito online.

Il sito locale serve esclusivamente come destinazione di pubblicazione. La cartella JS non si configura separatamente: è sempre:

`<sito locale>\js`

### Aggiungere una stagione manuale

Una stagione Manuale richiede soltanto:

- anni nel formato `AAAA/AAAA`;
- numero stagione.

Non richiede FCM/FCA.

## URL sito online

È possibile inserire, per esempio:

`www.example.org\lega2026`

Al salvataggio RecordsNext normalizza il valore in:

`http://www.example.org/lega2026`

Regole:

- `\` → `/`;
- protocollo mancante → `http://`;
- `https://` esplicito viene conservato;
- slash finali superflui vengono eliminati.

## Calendari

Il calendario storico canonico è:

`data\calendars\DataA-YYYY.js`

Esempi:

- `2024_2025` → `data\calendars\DataA-2024.js`
- `2025_2026` → `data\calendars\DataA-2025.js`
- `2026_2027` → `data\calendars\DataA-2026.js`

La 3.1 distribuisce i calendari dal 1991 al 2026.

Se il DataA canonico manca e il sito locale della stagione contiene `js\DataA.js`, RecordsNext prova a recuperarlo nell'archivio interno.

## Prima elaborazione

Dopo aver configurato le stagioni:

1. scegliere le famiglie record;
2. configurare Modificatori, Soglie/Fortuna e Culometro se necessari;
3. usare modalità **Completa**;
4. eseguire l'elaborazione;
5. controllare il preflight;
6. verificare i JS generati.

## Uso ordinario

Dopo ogni aggiornamento FCM:

1. avviare RecordsNext;
2. usare **Consolidata**;
3. selezionare **Pubblica nel sito della stagione corrente al termine**;
4. premere **Elabora**.

Questa modalità aggiorna soltanto il sito della stagione corrente.

## Riallineamento dei siti storici

Usare **Pubblica i siti delle stagioni selezionate** quando occorre aggiornare più vecchi siti.

Le checkbox di stagione identificano i target da pubblicare. Per ciascun target RecordsNext applica il cutoff storico: il sito riceve soltanto le stagioni disponibili fino a quella stagione.

## Output pubblicati

Nella cartella `js` del sito target vengono pubblicati:

- `fcmRecordsNext_Core.js`
- `fcmRecordsNext_Classics.js`
- `fcmRecordsNext_Series.js`
- `fcmRecordsNext_RU.js`
- `fcmRecordsNext_Modifiers.js`
- `fcmRecordsNext_ThresholdsLuck.js`
- `fcmRecordsNext_Culometro.js`
- `fcmRecordsNext_Matches.js`
- `fcmRecordsNext_Manifest.js`

## Problemi comuni

### Java non trovato o versione troppo vecchia

Installare Java 21 o superiore e verificare che `java` sia disponibile nel `PATH`.

### Una stagione non viene pubblicata

Controllare:

- che abbia un sito locale configurato;
- che la directory del sito esista;
- che la stagione sia selezionata quando si usa la pubblicazione multisito.

Una stagione senza sito può comunque contribuire allo storico.

### Il Culometro risulta parziale

Il preflight può indicare dipendenze opzionali mancanti. In questo caso il Culometro può essere generato parzialmente senza rendere fallita l'intera elaborazione.

### Il link al tabellino apre la stagione sbagliata

Controllare il sito online configurato sulla singola stagione e salvarlo nuovamente per applicare la normalizzazione URL.
