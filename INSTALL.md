# Patch RecordsNext4 v49 — solo TEST

Richiede la v48 installata in `D:\DEV_APPS\RecordsNext4.0_TEST_v37`. Il programma va chiuso prima dell'installazione. Eseguire preflight e installatore v49. Il preflight verifica SHA-256 dei sorgenti v48 e bibbia v40, oltre alla pubblicazione disabilitata. Backup/rollback automatici; nessun accesso in scrittura a database, identita' o siti reali.

Dopo l'installazione avviare `tools/Start-RecordsNext4-Test-v46.ps1` (riusa il launcher UCanAccess). Controllare **solo l'apertura di Top & Flop e la modifica visiva delle soglie**; non occorre una nuova elaborazione lunga per questa prova. L'audit tribuna viene scritto nei JS soltanto alla successiva elaborazione completa, da raggruppare con l'audit finale.

---

# RecordsNext 4.0 v48 — installazione TEST

**Solo** nella cartella `D:\DEV_APPS\RecordsNext4.0_TEST_v37` con la v47 gia' installata. Non toccare la 3.2 o la cartella `RecordsNext4.0` principale. Il preflight SHA-256 rifiuta basi diverse; l'installatore crea backup, compila con Java21 e ripristina automaticamente i file e il JAR in caso di errore. Pubblicazione OFF obbligatoria.

Eseguire una riga PowerShell per volta:

```powershell
Expand-Archive -LiteralPath 'D:\DEV_APPS\downloads\RecordsNext4_v48.zip' -DestinationPath 'D:\DEV_APPS\downloads\RecordsNext4_v48' -Force
powershell.exe -NoProfile -ExecutionPolicy Bypass -File 'D:\DEV_APPS\downloads\RecordsNext4_v48\Install-RecordsNext4-v48.ps1' -VerifyOnly
powershell.exe -NoProfile -ExecutionPolicy Bypass -File 'D:\DEV_APPS\downloads\RecordsNext4_v48\Install-RecordsNext4-v48.ps1'
powershell.exe -NoProfile -ExecutionPolicy Bypass -File 'D:\DEV_APPS\RecordsNext4.0_TEST_v37\tools\Start-RecordsNext4-Test-v46.ps1'
```

Nella GUI Top & Flop selezionare **TF11–TF13** (ora cliccabili), eventualmente **TF15**, salvare e avviare **Completa** senza pubblicare. La patch non cambia la selezione personale automaticamente. La popolazione comprende **soltanto riserve documentate non conteggiate**; verificare `Bench` e i record TF11–TF13 sul sito di prova prima di qualunque distribuzione reale.

---

# RecordsNext 4.0 v47 - aggiornamento TEST

**Non installare sulla v3.2 o sulla cartella 4.0 principale.** Solo `D:\DEV_APPS\RecordsNext4.0_TEST_v37`, basata su v46 e con `publishToSite=false`. L'installatore esegue preflight hash di tutte le basi, crea backup completo dei file toccati e del JAR, compila con Maven/Java 21, esegue smoke test; in caso di errore ripristina automaticamente.

Comandi PowerShell, **una riga per comando**:

```powershell
Expand-Archive -LiteralPath 'D:\DEV_APPS\downloads\RecordsNext4_v47.zip' -DestinationPath 'D:\DEV_APPS\downloads\RecordsNext4_v47' -Force
powershell.exe -NoProfile -ExecutionPolicy Bypass -File 'D:\DEV_APPS\downloads\RecordsNext4_v47\Install-RecordsNext4-v47.ps1' -VerifyOnly
powershell.exe -NoProfile -ExecutionPolicy Bypass -File 'D:\DEV_APPS\downloads\RecordsNext4_v47\Install-RecordsNext4-v47.ps1'
powershell.exe -NoProfile -ExecutionPolicy Bypass -File 'D:\DEV_APPS\RecordsNext4.0_TEST_v37\tools\Start-RecordsNext4-Test-v46.ps1'
```

Dalla GUI lasciare **pubblicazione disattivata**. Selezionare TF14/TF15 dalla famiglia Top & Flop se la selezione locale li conserva su `false` dalla versione precedente. Non modificare `config/player-identities.json` per questa installazione. Il file `config/topflop-real-rules.json` e' opzionale e non viene installato o compilato automaticamente: prima di introdurlo verificare le fonti per ogni stagione (vedi SPECIFICA_CONFIG_REGOLE_REALI_v47.md). Nessun costo viene normalizzato automaticamente sul budget.

**Atteso**: log completo, shard `Economy` stagionali, `History` con TF14/TF15 solo su identita' approvate, stato `RealFantasy=NON_VERIFICABILE` dove manca il rulebook, ogni JS <=1.500.000 byte. Registrare l'esito prima di autorizzare il punto 1 e poi il punto 2.

---

# Addendum: collaudo sviluppo RecordsNext 4.0 / v46

La release 3.2.0 sottostante resta operativa; questa sezione riguarda **soltanto** la cartella test `D:\DEV_APPS\RecordsNext4.0_TEST_v37`.

1. Usare il pacchetto v46 con preflight SHA-256 e backup; non sovrascrivere sorgenti inattesi.
2. Non usare `RecordsNext.bat` della 3.2 per lanciare la build di sviluppo.
3. Per testare l'importazione con UCanAccess, usare `tools/Start-RecordsNext4-Test-v46.ps1` incluso nella patch: carica il JAR 4.0 e i JAR del runtime ufficiale 3.2 in **sola lettura**.
4. Tenere **Pubblica sui siti = No**, scegliere Completa e avviare la GUI; la suddivisione dei 7 output pesanti avviene anche sullo staging non pubblicato.
5. Verificare file fisici, facade, manifest, identita' e cutoff. Solo successivamente testare su siti clonati.
6. GitHub: le modifiche v46 restano locali fino a `git commit`/`git push` sul ramo di sviluppo 4.0, dopo le verifiche. Non committare database, siti o `player-identities.json`.

Riferimenti: `docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v38.md` e `docs/VERIFICA_STAGING_JS_v46.md`.

---

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

## Aggiornamento RC v67
La RC migrata si trova in `D:\DEV_APPS\RecordsNext4.0_RC_v64`. Non reinstallare o reimportare le 21 stagioni; FCA 2012/13 recuperato obbligatorio. GUI `Avvia-RecordsNext4-RC-v64.ps1`. Prima della pubblicazione reale: controllare destinazioni, backup, browser e rollback. La documentazione v47 non pubblica nulla.
