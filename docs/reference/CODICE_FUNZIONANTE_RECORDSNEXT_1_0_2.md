# Codice funzionante RecordsNext

Documento generato automaticamente.

Non modificare manualmente questo file.
Rigenerarlo dopo ogni modifica verificata.

## Stato

- Generato: 2026-08-04 22:01:57 +02:00
- Project root: D:\DEV_APPS\RecordsNext
- Branch: main
- Commit: b89335260e10cbd7ed979562d3f8a1ddace5a937
- Java: 21.0.1
- Maven: 3.9.16

### Stato Git

```text
M pom.xml
 M release/site-examples/RecordsNext/recordsnext.js
 M src/main/java/it/alterlega/recordsnext/SeasonNormalizedExporter.java
 M tools/Build-RecordsNextRelease.ps1
```

## Comandi verificati

### Compilazione e test

```powershell
Set-Location "D:\DEV_APPS\RecordsNext"
.\mvnw.cmd clean test
```

### Generazione della Bibbia

```powershell
.\tools\Create-RecordsNextWorkingCodeMd.ps1
```

## Struttura documentata

```text
.gitignore
.mvn\wrapper\maven-wrapper.properties
config\site-publish.local.example.json
docs\AVVIO_SENZA_CONSOLE.md
docs\BENCHMARK_UCANACCESS.md
docs\DECISIONI.md
docs\ESEMPI_SITO.md
docs\INSTALLAZIONE.md
docs\PIANO_INIZIALE.md
docs\REQUISITI_DATI.md
mvnw.cmd
pom.xml
README.md
release\Avvia-RecordsNext.vbs
release\Installa-RecordsNext.bat
release\site-examples\LEGGIMI.txt
release\site-examples\recordsnext.html
release\site-examples\RecordsNext\recordsnext.css
release\site-examples\RecordsNext\recordsnext.js
src\main\java\it\alterlega\recordsnext\app\PipelineConfig.java
src\main\java\it\alterlega\recordsnext\app\ProcessingMode.java
src\main\java\it\alterlega\recordsnext\app\ProcessingOptions.java
src\main\java\it\alterlega\recordsnext\app\RecordsNextPipeline.java
src\main\java\it\alterlega\recordsnext\app\RecordsNextPreparationService.java
src\main\java\it\alterlega\recordsnext\CalendarSourceManager.java
src\main\java\it\alterlega\recordsnext\CanonicalSchemaProbe.java
src\main\java\it\alterlega\recordsnext\CanonicalViews.java
src\main\java\it\alterlega\recordsnext\ConfigurationSchema.java
src\main\java\it\alterlega\recordsnext\ConfrontiStoriciCalendarImporter.java
src\main\java\it\alterlega\recordsnext\DatabaseInspector.java
src\main\java\it\alterlega\recordsnext\gui\FcmSeasonDetector.java
src\main\java\it\alterlega\recordsnext\gui\HistoricalMappingDialog.java
src\main\java\it\alterlega\recordsnext\gui\HistoricalMappingRepository.java
src\main\java\it\alterlega\recordsnext\gui\RecordsNextApp.java
src\main\java\it\alterlega\recordsnext\gui\RecordsNextConfigurationDialog.java
src\main\java\it\alterlega\recordsnext\gui\SeasonConfigurationRepository.java
src\main\java\it\alterlega\recordsnext\PlayoffRecordsExporter.java
src\main\java\it\alterlega\recordsnext\RawSqliteImporter.java
src\main\java\it\alterlega\recordsnext\Records2026ClassicJsExporter.java
src\main\java\it\alterlega\recordsnext\Records2026RuJsExporter.java
src\main\java\it\alterlega\recordsnext\Records2026SitePublisher.java
src\main\java\it\alterlega\recordsnext\RiserveUfficioArchiveBuilder.java
src\main\java\it\alterlega\recordsnext\SeasonMappingConfigurator.java
src\main\java\it\alterlega\recordsnext\SeasonNormalizedBatchExporter.java
src\main\java\it\alterlega\recordsnext\SeasonNormalizedExporter.java
src\main\java\it\alterlega\recordsnext\SeasonRecordsArchiveBuilder.java
src\main\java\it\alterlega\recordsnext\SeasonRegistry.java
src\main\java\it\alterlega\recordsnext\SerieAQueryProbe.java
src\main\java\it\alterlega\recordsnext\SerieARoundProbe.java
src\main\java\it\alterlega\recordsnext\SqliteAudit.java
src\test\java\it\alterlega\recordsnext\RecordsNextApplicationTest.java
tools\Analyze-Records2026RemainingContracts-v3.ps1
tools\Build-RecordsNextRelease.ps1
tools\Cleanup-RecordsNextGuiPatches.ps1
tools\Compare-NormalizedReserveOffice.ps1
tools\Compare-Records2026Classic-v1.ps1
tools\Compare-RiserveUfficioArchive.ps1
tools\Compare-SeasonRecordsArchive.ps1
tools\Create-RecordsNextWorkingCodeMd.ps1
tools\Initialize-RecordsNextClassicArchive.ps1
tools\Initialize-RecordsNextRuArchive.ps1
tools\Publish-RecordsNextSite.ps1
tools\Validate-NormalizedSeason-v3.ps1
tools\Validate-NormalizedSeason-v8.ps1
tools\Validate-NormalizedSeason-v9.ps1
```

## File

### .gitignore

```text
# Build Java
target/
*.class

# IDE
.idea/
.vscode/
*.iml

# Database e dati locali
data/database/*
!data/database/.gitkeep
data/raw/*
!data/raw/.gitkeep
data/reports/*
!data/reports/.gitkeep

# Output di build e benchmark generati
dist/
output/*
!output/.gitkeep
benchmark/results/*
!benchmark/results/.gitkeep

# Configurazioni locali
config/paths.local.json

# Log
*.log

# Maven locale usato solo per inizializzare il wrapper
tools/apache-maven-*/

# RecordsNext local publishing
/config/site-publish.local.json
/data/site-export-staging/
/reference/
/data/records-archive/

/data/consolidation/
```

### .mvn\wrapper\maven-wrapper.properties

```properties
wrapperVersion=3.3.4
distributionType=only-script
distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.16/apache-maven-3.9.16-bin.zip
```

### config\site-publish.local.example.json

```json
{
  "normalizedReports": ".\\data\\reports",
  "classicArchive": ".\\data\\records-archive\\stagioni",
  "classicSeasons": [
    "2024_2025",
    "2025_2026"
  ],
  "ruArchive": ".\\data\\records-archive\\riserveufficio",
  "ruSeasons": [
    "2024_2025",
    "2025_2026"
  ],
  "stagingRoot": ".\\data\\site-export-staging",
  "siteJsDir": "E:\\fantacalcio\\Lega2025\\js"
}
```

### docs\AVVIO_SENZA_CONSOLE.md

```markdown
# Avvio senza console

Il launcher pubblico della release è `Avvia-RecordsNext.vbs`.

- avvia `javaw.exe`, non `java.exe`;
- usa la stessa classpath del precedente BAT;
- non mostra la finestra CMD durante l'uso normale;
- mostra una finestra grafica soltanto se mancano JAR, UCanAccess o Java;
- l'installer elimina automaticamente un eventuale vecchio `Avvia-RecordsNext.bat`.
```

### docs\BENCHMARK_UCANACCESS.md

```markdown
# Benchmark UCanAccess

## Informazione di partenza

Il creatore di ConfrontiStorici ha comunicato che UCanAccess 2.0.9.5
risulta molto più veloce delle versioni successive nel suo utilizzo.

Non disponiamo dei sorgenti di ConfrontiStorici e non conosciamo le sue query
o la sua architettura interna.

Questa informazione viene quindi trattata come indicazione tecnica autorevole
da verificare sperimentalmente, non come prova conclusiva.

## Versioni obbligatorie nel confronto

Il benchmark iniziale deve includere almeno:

- UCanAccess 2.0.9.5;
- una versione recente di UCanAccess;
- stesso Java;
- stessi file FCM e FCA;
- stesse query;
- stessi risultati attesi.

## Misure

Per ogni versione devono essere misurati separatamente:

- caricamento delle librerie;
- apertura del database;
- introspezione dello schema;
- prima query;
- query successive;
- lettura completa delle righe richieste;
- chiusura della connessione;
- tempo totale;
- memoria utilizzata;
- dimensione delle dipendenze;
- compatibilità con Java 21.

## Verifiche funzionali

I risultati devono essere confrontati per verificare:

- stesso numero di tabelle;
- stesso numero di colonne;
- stessi tipi rilevati;
- stessi conteggi di righe;
- stessi valori restituiti;
- nessuna colonna omessa;
- nessuna conversione silenziosa;
- nessuna differenza nei valori nulli.

## Regola di scelta

La versione definitiva non sarà scelta solo in base alla velocità.

Saranno considerati anche:

- correttezza;
- stabilità;
- compatibilità con i database FCM/FCA;
- compatibilità con Java;
- possibilità di distribuzione portabile;
- presenza di errori o differenze nei dati.

UCanAccess 2.0.9.5 resta comunque un candidato obbligatorio e prioritario.
```

### docs\DECISIONI.md

```markdown
# Registro delle decisioni

## DEC-001 — Progetto separato

RecordsNext è un repository autonomo.

Records2026 resta operativo e non viene modificato durante la fase di
progettazione e benchmark.

## DEC-002 — Records2026 come oracolo

Gli output di RecordsNext devono essere confrontati con gli output già
validati di Records2026.

Le differenze devono essere spiegate e documentate.

## DEC-003 — Eliminazione dei CSV dal percorso ordinario

I CSV non saranno il formato operativo interno del nuovo motore.

Potranno essere prodotti solo come output diagnostico o di esportazione.

## DEC-004 — UCanAccess come lettore delle fonti

UCanAccess sarà utilizzato per aprire e interrogare i file FCM e FCA.

Non si presume ancora che UCanAccess diretto sia l'architettura definitiva.

## DEC-005 — Database locale consolidato

Saranno confrontati almeno:

- SQLite;
- DuckDB;
- accesso UCanAccess diretto.

SQLite è il candidato principale per il database canonico locale.

## DEC-006 — Conservazione integrale

Ogni tabella e ogni colonna rilevata nei database sorgente deve essere
conservata nella zona raw.

Lo schema canonico è aggiuntivo e non sostituisce la copia raw.

## DEC-007 — Database online

Un database MySQL o PostgreSQL potrà essere una destinazione futura di
sincronizzazione o pubblicazione.

Il motore locale non dovrà dipendere obbligatoriamente da Internet.

## DEC-008 — Nessuna supposizione su ConfrontiStorici

Non disponiamo dei sorgenti di ConfrontiStorici.

Le sole informazioni utilizzabili sono quelle osservate dall'utente o
comunicate direttamente dal suo autore, compreso l'uso di UCanAccess.

## DEC-009 — Benchmark obbligatorio UCanAccess 2.0.9.5

Il creatore di ConfrontiStorici ha riferito che UCanAccess 2.0.9.5
è molto più veloce delle versioni successive nel suo utilizzo.

RecordsNext deve quindi confrontare obbligatoriamente UCanAccess 2.0.9.5
con almeno una versione recente, usando gli stessi database, le stesse query
e gli stessi controlli sui risultati.

La versione definitiva sarà scelta solo dopo benchmark riproducibili e
verifica dell'integrità dei dati.
```

### docs\ESEMPI_SITO.md

```markdown
# Esempi sito RecordsNext

Gli esempi sono neutri e adatti a leghe con qualsiasi struttura di competizioni.

- `recordsnext.html` è la pagina applicativa e va nella root del sito.
- La cartella `RecordsNext` contiene soltanto CSS e JavaScript e va nella root del sito.
- Non esiste un secondo HTML di rinvio dentro `RecordsNext`.
- I dati vengono letti dalla cartella `js` del sito.
- Stagioni e competizioni vengono scoperte dinamicamente dai file pubblicati.
- Le competizioni classiche sono selezionabili tramite checkbox multiple.
- L'utente può combinare qualsiasi insieme di competizioni senza raggruppamenti predefiniti.
- Non sono presenti nomi di squadre o esclusioni specifiche di una lega.
```

### docs\INSTALLAZIONE.md

```markdown
# Installazione di FCM RecordsNext 1.0

1. Estrai tutto lo ZIP direttamente nella cartella definitiva del plugin `RecordsNext`.
2. Esegui `Installa-RecordsNext.bat`; nelle cartelle protette di Windows può essere necessario avviarlo come amministratore.
3. Dopo l'installazione il payload temporaneo e l'installer vengono rimossi.
4. Per gli avvii normali usa `Avvia-RecordsNext.vbs`.

Il launcher VBS usa `javaw.exe` e non apre una finestra del prompt dei comandi. Java 21 deve essere disponibile tramite `JAVA_HOME` oppure nel `PATH` di Windows.

La cartella `Esempi-sito` contiene la pagina HTML e gli asset da copiare nel sito FCM. Può essere rimossa dall'installazione del plugin dopo la copia.
```

### docs\PIANO_INIZIALE.md

```markdown
# Piano iniziale RecordsNext

## Milestone 0 — Ambiente

- verificare Java;
- verificare Maven;
- creare il progetto;
- aggiungere UCanAccess;
- aggiungere il candidato SQLite;
- predisporre test automatici.

## Milestone 1 — Introspezione

Creare un comando che, dato un file FCM o FCA:

1. apre il database in sola lettura;
2. elenca tutte le tabelle;
3. elenca tutte le colonne;
4. rileva i tipi;
5. rileva chiavi primarie e indici;
6. conta le righe;
7. produce un rapporto JSON;
8. non modifica il database sorgente.

Output previsto:

data/reports/<stagione>/
- fcm-schema.json;
- fca-schema.json;
- fcm-row-counts.json;
- fca-row-counts.json;
- inspection-summary.json.

## Milestone 2 — Importazione raw

- creare SQLite locale;
- riprodurre tutte le tabelle FCM/FCA;
- importare tutte le colonne;
- conservare i tipi e la provenienza;
- eseguire audit completo.

## Milestone 3 — Schema canonico

- progettare le entità canoniche;
- mappare FCM e FCA;
- verificare le relazioni;
- non eliminare la zona raw.

## Milestone 4 — Prototipo Serie A

- stagione 2025/2026;
- Serie A;
- 180 incontri;
- 360 righe squadra;
- output normalizzato compatibile con Records2026;
- confronto semantico automatico.

## Milestone 5 — Benchmark

Confrontare:

- pipeline Records2026;
- UCanAccess diretto;
- UCanAccess + SQLite;
- UCanAccess + DuckDB.

Misurare:

- prima importazione;
- seconda esecuzione;
- aggiornamento della stagione corrente;
- una competizione;
- tutte le competizioni;
- storico completo;
- memoria;
- dimensione del database;
- compatibilità degli output.
```

### docs\REQUISITI_DATI.md

```markdown
# Requisiti di conservazione dei dati

## 1. Principio generale

RecordsNext non deve perdere dati presenti nei file FCM e FCA.

L'importazione deve essere conservativa, verificabile e ripetibile.

## 2. Livelli dei dati

### 2.1 Sorgenti

File originali:

- FCM: database della lega;
- FCA: archivio dei giocatori reali, delle squadre reali, dei voti e
  dei bonus/malus.

I file sorgente non devono essere modificati.

### 2.2 Zona raw

La zona raw deve conservare:

- tutte le tabelle leggibili;
- tutte le colonne;
- i nomi originali;
- i tipi JDBC e Access rilevati;
- i valori nulli;
- le chiavi originali;
- la provenienza del dato;
- il file e la stagione di origine.

Le tabelle raw dovranno usare un prefisso esplicito, per esempio:

- raw_fcm_competizione;
- raw_fcm_incontro;
- raw_fcm_formazione;
- raw_fca_giocatore_a;
- raw_fca_gioca_in;
- raw_fca_punteggio.

Il nome definitivo sarà prodotto automaticamente dalla mappatura dei nomi
originali.

### 2.3 Schema canonico

Lo schema canonico contiene entità normalizzate per il motore RecordsNext.

Esempi:

- stagione;
- competizione;
- girone;
- giornata;
- squadra;
- giocatore;
- incontro;
- formazione;
- punteggio;
- evento;
- associazione_squadra;
- importazione.

Lo schema canonico non elimina né sostituisce le tabelle raw.

## 3. Metadati obbligatori

Per ogni database sorgente:

- percorso;
- nome file;
- tipo FCM o FCA;
- dimensione;
- ultima modifica;
- hash SHA-256;
- stagione;
- data di importazione;
- versione dell'importatore;
- versione dello schema.

Per ogni tabella:

- nome originale;
- nome raw;
- numero di colonne;
- numero di righe;
- chiave primaria rilevata;
- indici rilevati;
- eventuali errori.

Per ogni colonna:

- nome originale;
- posizione ordinale;
- tipo JDBC;
- nome tipo database;
- dimensione;
- precisione;
- scala;
- nullable;
- valore predefinito;
- stato della mappatura canonica.

## 4. Audit obbligatorio

Ogni importazione deve verificare:

- stesso numero di tabelle rilevate e registrate;
- stesso numero di colonne per tabella;
- stesso numero di righe sorgente e raw;
- nessuna colonna omessa;
- nessuna conversione fallita silenziosamente;
- nessun valore nullo introdotto senza spiegazione;
- nessun duplicato introdotto;
- hash o checksum riproducibile quando applicabile.

L'importazione non può essere dichiarata valida se l'audit non passa.

## 5. Catena relazionale già verificata

La documentazione di Records2026 riporta questa catena:

Formazione.IDIncontro -> Incontro.ID
Formazione.IDGioc -> GiocatoreA.ID
Incontro.GiornataDiA + Formazione.IDGioc
  -> GiocaIn.Giornata + GiocaIn.IDGiocatore
GiocaIn.IDPunteggio -> Punteggio.ID

La copertura documentata era 39850 / 39850.

RecordsNext dovrà ripetere e documentare autonomamente questa verifica.
```

### mvnw.cmd

```batch
<# : batch portion
@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script, version 3.3.4
@REM
@REM Optional ENV vars
@REM   MVNW_REPOURL - repo url base for downloading maven distribution
@REM   MVNW_USERNAME/MVNW_PASSWORD - user and password for downloading maven
@REM   MVNW_VERBOSE - true: enable verbose log; others: silence the output
@REM ----------------------------------------------------------------------------

@IF "%__MVNW_ARG0_NAME__%"=="" (SET __MVNW_ARG0_NAME__=%~nx0)
@SET __MVNW_CMD__=
@SET __MVNW_ERROR__=
@SET __MVNW_PSMODULEP_SAVE=%PSModulePath%
@SET PSModulePath=
@FOR /F "usebackq tokens=1* delims==" %%A IN (`powershell -noprofile "& {$scriptDir='%~dp0'; $script='%__MVNW_ARG0_NAME__%'; icm -ScriptBlock ([Scriptblock]::Create((Get-Content -Raw '%~f0'))) -NoNewScope}"`) DO @(
  IF "%%A"=="MVN_CMD" (set __MVNW_CMD__=%%B) ELSE IF "%%B"=="" (echo %%A) ELSE (echo %%A=%%B)
)
@SET PSModulePath=%__MVNW_PSMODULEP_SAVE%
@SET __MVNW_PSMODULEP_SAVE=
@SET __MVNW_ARG0_NAME__=
@SET MVNW_USERNAME=
@SET MVNW_PASSWORD=
@IF NOT "%__MVNW_CMD__%"=="" ("%__MVNW_CMD__%" %*)
@echo Cannot start maven from wrapper >&2 && exit /b 1
@GOTO :EOF
: end batch / begin powershell #>

$ErrorActionPreference = "Stop"
if ($env:MVNW_VERBOSE -eq "true") {
  $VerbosePreference = "Continue"
}

# calculate distributionUrl, requires .mvn/wrapper/maven-wrapper.properties
$distributionUrl = (Get-Content -Raw "$scriptDir/.mvn/wrapper/maven-wrapper.properties" | ConvertFrom-StringData).distributionUrl
if (!$distributionUrl) {
  Write-Error "cannot read distributionUrl property in $scriptDir/.mvn/wrapper/maven-wrapper.properties"
}

switch -wildcard -casesensitive ( $($distributionUrl -replace '^.*/','') ) {
  "maven-mvnd-*" {
    $USE_MVND = $true
    $distributionUrl = $distributionUrl -replace '-bin\.[^.]*$',"-windows-amd64.zip"
    $MVN_CMD = "mvnd.cmd"
    break
  }
  default {
    $USE_MVND = $false
    $MVN_CMD = $script -replace '^mvnw','mvn'
    break
  }
}

# apply MVNW_REPOURL and calculate MAVEN_HOME
# maven home pattern: ~/.m2/wrapper/dists/{apache-maven-<version>,maven-mvnd-<version>-<platform>}/<hash>
if ($env:MVNW_REPOURL) {
  $MVNW_REPO_PATTERN = if ($USE_MVND -eq $False) { "/org/apache/maven/" } else { "/maven/mvnd/" }
  $distributionUrl = "$env:MVNW_REPOURL$MVNW_REPO_PATTERN$($distributionUrl -replace "^.*$MVNW_REPO_PATTERN",'')"
}
$distributionUrlName = $distributionUrl -replace '^.*/',''
$distributionUrlNameMain = $distributionUrlName -replace '\.[^.]*$','' -replace '-bin$',''

$MAVEN_M2_PATH = "$HOME/.m2"
if ($env:MAVEN_USER_HOME) {
  $MAVEN_M2_PATH = "$env:MAVEN_USER_HOME"
}

if (-not (Test-Path -Path $MAVEN_M2_PATH)) {
    New-Item -Path $MAVEN_M2_PATH -ItemType Directory | Out-Null
}

$MAVEN_WRAPPER_DISTS = $null
if ((Get-Item $MAVEN_M2_PATH).Target[0] -eq $null) {
  $MAVEN_WRAPPER_DISTS = "$MAVEN_M2_PATH/wrapper/dists"
} else {
  $MAVEN_WRAPPER_DISTS = (Get-Item $MAVEN_M2_PATH).Target[0] + "/wrapper/dists"
}

$MAVEN_HOME_PARENT = "$MAVEN_WRAPPER_DISTS/$distributionUrlNameMain"
$MAVEN_HOME_NAME = ([System.Security.Cryptography.SHA256]::Create().ComputeHash([byte[]][char[]]$distributionUrl) | ForEach-Object {$_.ToString("x2")}) -join ''
$MAVEN_HOME = "$MAVEN_HOME_PARENT/$MAVEN_HOME_NAME"

if (Test-Path -Path "$MAVEN_HOME" -PathType Container) {
  Write-Verbose "found existing MAVEN_HOME at $MAVEN_HOME"
  Write-Output "MVN_CMD=$MAVEN_HOME/bin/$MVN_CMD"
  exit $?
}

if (! $distributionUrlNameMain -or ($distributionUrlName -eq $distributionUrlNameMain)) {
  Write-Error "distributionUrl is not valid, must end with *-bin.zip, but found $distributionUrl"
}

# prepare tmp dir
$TMP_DOWNLOAD_DIR_HOLDER = New-TemporaryFile
$TMP_DOWNLOAD_DIR = New-Item -Itemtype Directory -Path "$TMP_DOWNLOAD_DIR_HOLDER.dir"
$TMP_DOWNLOAD_DIR_HOLDER.Delete() | Out-Null
trap {
  if ($TMP_DOWNLOAD_DIR.Exists) {
    try { Remove-Item $TMP_DOWNLOAD_DIR -Recurse -Force | Out-Null }
    catch { Write-Warning "Cannot remove $TMP_DOWNLOAD_DIR" }
  }
}

New-Item -Itemtype Directory -Path "$MAVEN_HOME_PARENT" -Force | Out-Null

# Download and Install Apache Maven
Write-Verbose "Couldn't find MAVEN_HOME, downloading and installing it ..."
Write-Verbose "Downloading from: $distributionUrl"
Write-Verbose "Downloading to: $TMP_DOWNLOAD_DIR/$distributionUrlName"

$webclient = New-Object System.Net.WebClient
if ($env:MVNW_USERNAME -and $env:MVNW_PASSWORD) {
  $webclient.Credentials = New-Object System.Net.NetworkCredential($env:MVNW_USERNAME, $env:MVNW_PASSWORD)
}
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
$webclient.DownloadFile($distributionUrl, "$TMP_DOWNLOAD_DIR/$distributionUrlName") | Out-Null

# If specified, validate the SHA-256 sum of the Maven distribution zip file
$distributionSha256Sum = (Get-Content -Raw "$scriptDir/.mvn/wrapper/maven-wrapper.properties" | ConvertFrom-StringData).distributionSha256Sum
if ($distributionSha256Sum) {
  if ($USE_MVND) {
    Write-Error "Checksum validation is not supported for maven-mvnd. `nPlease disable validation by removing 'distributionSha256Sum' from your maven-wrapper.properties."
  }
  Import-Module $PSHOME\Modules\Microsoft.PowerShell.Utility -Function Get-FileHash
  if ((Get-FileHash "$TMP_DOWNLOAD_DIR/$distributionUrlName" -Algorithm SHA256).Hash.ToLower() -ne $distributionSha256Sum) {
    Write-Error "Error: Failed to validate Maven distribution SHA-256, your Maven distribution might be compromised. If you updated your Maven version, you need to update the specified distributionSha256Sum property."
  }
}

# unzip and move
Expand-Archive "$TMP_DOWNLOAD_DIR/$distributionUrlName" -DestinationPath "$TMP_DOWNLOAD_DIR" | Out-Null

# Find the actual extracted directory name (handles snapshots where filename != directory name)
$actualDistributionDir = ""

# First try the expected directory name (for regular distributions)
$expectedPath = Join-Path "$TMP_DOWNLOAD_DIR" "$distributionUrlNameMain"
$expectedMvnPath = Join-Path "$expectedPath" "bin/$MVN_CMD"
if ((Test-Path -Path $expectedPath -PathType Container) -and (Test-Path -Path $expectedMvnPath -PathType Leaf)) {
  $actualDistributionDir = $distributionUrlNameMain
}

# If not found, search for any directory with the Maven executable (for snapshots)
if (!$actualDistributionDir) {
  Get-ChildItem -Path "$TMP_DOWNLOAD_DIR" -Directory | ForEach-Object {
    $testPath = Join-Path $_.FullName "bin/$MVN_CMD"
    if (Test-Path -Path $testPath -PathType Leaf) {
      $actualDistributionDir = $_.Name
    }
  }
}

if (!$actualDistributionDir) {
  Write-Error "Could not find Maven distribution directory in extracted archive"
}

Write-Verbose "Found extracted Maven distribution directory: $actualDistributionDir"
Rename-Item -Path "$TMP_DOWNLOAD_DIR/$actualDistributionDir" -NewName $MAVEN_HOME_NAME | Out-Null
try {
  Move-Item -Path "$TMP_DOWNLOAD_DIR/$MAVEN_HOME_NAME" -Destination $MAVEN_HOME_PARENT | Out-Null
} catch {
  if (! (Test-Path -Path "$MAVEN_HOME" -PathType Container)) {
    Write-Error "fail to move MAVEN_HOME"
  }
} finally {
  try { Remove-Item $TMP_DOWNLOAD_DIR -Recurse -Force | Out-Null }
  catch { Write-Warning "Cannot remove $TMP_DOWNLOAD_DIR" }
}

Write-Output "MVN_CMD=$MAVEN_HOME/bin/$MVN_CMD"
```

### pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="
             http://maven.apache.org/POM/4.0.0
             https://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>it.alterlega</groupId>
    <artifactId>recordsnext</artifactId>
    <version>1.0.2</version>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.release>21</maven.compiler.release>
        <junit.version>5.12.2</junit.version>
    </properties>

    <dependencies>

<dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
    <version>3.53.2.1</version>
</dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.14.0</version>
            </plugin>

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.5.3</version>
            </plugin>


            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>3.6.1</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals><goal>shade</goal></goals>
                        <configuration>
                            <finalName>RecordsNext</finalName>
                            <createDependencyReducedPom>false</createDependencyReducedPom>
                            <transformers>
                                <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                    <mainClass>it.alterlega.recordsnext.gui.RecordsNextApp</mainClass>
                                </transformer>
                            </transformers>
                        </configuration>
                    </execution>
                </executions>
            </plugin>

            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>exec-maven-plugin</artifactId>
                <version>3.5.0</version>
            </plugin>
        </plugins>
    </build>
</project>
```

### README.md

```markdown
# FCM RecordsNext 1.0

**FCM RecordsNext** è un'applicazione desktop multistagione per elaborare gli archivi di **Fantacalcio Manager (FCM/FCA)**, ricostruire record storici e stagionali e pubblicare sul sito della lega file JavaScript già pronti per la consultazione.

RecordsNext sostituisce il precedente flusso Records2026 con un'unica procedura guidata per:

- configurare le stagioni e le relative fonti;
- associare squadre e competizioni storiche alle identità correnti;
- normalizzare e consolidare i dati;
- generare record classici e statistiche sulle riserve d'ufficio;
- pubblicare gli output nella cartella `js` del sito FCM;
- aggiornare soltanto le stagioni cambiate nelle elaborazioni successive.

![Schermata principale di FCM RecordsNext](docs/screenshots/01.png)

## Cosa genera

RecordsNext produce due famiglie di output pubblici: **record classici** e **riserve d'ufficio**. I risultati conservano stagione, competizione, squadra, avversaria, giornata e, quando disponibile, il collegamento al tabellino della singola partita.

### Record classici

Il generatore classico produce 18 sezioni:

1. **Maggior numero di punti fatti** in una singola partita;
2. **Partite consecutive senza sconfitte**;
3. **Espulsioni per squadra**;
4. **Espulsioni per giocatore**;
5. **Ammonizioni per squadra**;
6. **Assist per squadra**;
7. **Autogol per squadra**;
8. **Rigori sbagliati per squadra**;
9. **Rigori parati per squadra**;
10. **Gol segnati su rigore per squadra**;
11. **Miglior modificatore della difesa** in una singola gara;
12. **Totale modificatore della difesa per squadra**;
13. **Numero di utilizzi del capitano per squadra**;
14. **Totale del modificatore capitano per squadra**;
15. **Numero di clean sheet del portiere per squadra**;
16. **Totale clean sheet del portiere per squadra**;
17. **Serie consecutive di clean sheet del portiere**;
18. **Serie consecutive con bonus capitano**.

Per i record basati sulle singole gare vengono mantenuti anche i dettagli utili alla verifica: punteggio, risultato, avversaria, giornata, identificativo dell'incontro e URL del tabellino.

### Riserve d'ufficio

L'elaborazione delle riserve d'ufficio genera viste stagionali e storiche dedicate a:

- partite con il maggior numero di riserve d'ufficio;
- partite giocate con riserve d'ufficio;
- partite disputate contro squadre con riserve d'ufficio;
- riserve d'ufficio decisive sul risultato;
- bilancio delle riserve decisive;
- riserve decisive subite dall'avversaria;
- bilancio delle riserve decisive contro;
- bilancio complessivo con riserve d'ufficio;
- bilancio complessivo contro riserve d'ufficio;
- media punti con riserve d'ufficio;
- media punti contro riserve d'ufficio;
- distribuzione per tipo di riserva: **PU, DU, CU e AU**.

## Flusso operativo

### 1. Scelta dell'elaborazione

Dalla schermata principale si selezionano:

- **Elaborazione completa**, per ricostruire i dati dalle fonti configurate;
- **Aggiornamento da consolidamento**, per riutilizzare lo stato già prodotto e aggiornare soltanto ciò che è cambiato;
- record classici e/o riserve d'ufficio;
- generazione dei file JavaScript;
- pubblicazione diretta nel sito attuale o in una destinazione personalizzata.

![Opzioni di elaborazione e pubblicazione](docs/screenshots/06.png)

### 2. Configurazione delle stagioni

Ogni stagione può essere:

- **gestita**, con file FCM e FCA e con le cartelle del sito associate;
- **manuale**, quando la stagione deve essere censita ma non può essere importata automaticamente.

La finestra di configurazione mostra lo stato delle sorgenti e delle associazioni storiche per ogni stagione.

![Configurazione delle stagioni](docs/screenshots/02.png)

Per aggiungere una nuova stagione si indicano i file FCM/FCA oppure i dati minimi della stagione manuale.

![Aggiunta di una stagione](docs/screenshots/03.png)

### 3. Associazioni storiche

RecordsNext usa la stagione corrente come riferimento e permette di ricondurre gli elementi delle stagioni precedenti alle rispettive identità canoniche.

Le competizioni vengono associate alle competizioni storiche corrispondenti.

![Associazione delle competizioni storiche](docs/screenshots/05.png)

Le squadre storiche vengono collegate alle identità attuali o canoniche.

![Associazione delle squadre storiche](docs/screenshots/04.png)

### 4. Elaborazione e consolidamento

Durante l'esecuzione vengono mostrati:

- fase corrente;
- avanzamento dell'operazione;
- log delle stagioni importate o riutilizzate;
- tempi delle singole fasi;
- numero di file generati, validati e pubblicati.

![Elaborazione in corso](docs/screenshots/07.png)

Al termine il consolidamento viene aggiornato per velocizzare le elaborazioni successive.

![Elaborazione completata](docs/screenshots/08.png)

## Output prodotti

Gli output pubblici principali sono:

```text
records2026.recordstagionali.classic.js
records2026.recordstagionali.ru.js
records2026.storico.ru.<stagione>.js
records2026.storico.ru.manifest.js
```

I nomi mantengono il contratto pubblico di Records2026 per garantire compatibilità con le pagine del sito già predisposte, mentre la generazione è eseguita interamente da RecordsNext.

La struttura consigliata del sito è:

```text
<root sito>\
├── recordsnext.html
├── RecordsNext\
│   ├── recordsnext.css
│   └── recordsnext.js
└── js\
    ├── records2026.recordstagionali.classic.js
    ├── records2026.recordstagionali.ru.js
    ├── records2026.storico.ru.<stagione>.js
    └── records2026.storico.ru.manifest.js
```

La pagina di esempio:

- rileva dinamicamente stagioni e competizioni disponibili;
- consente la selezione multipla delle competizioni;
- non contiene nomi o regole specifiche di una singola lega;
- legge direttamente i file pubblicati nella cartella `js`.

Le istruzioni dettagliate sono in [`docs/ESEMPI_SITO.md`](docs/ESEMPI_SITO.md).

## Installazione

La release contiene:

```text
Installa-RecordsNext.bat
INSTALLAZIONE.md
Esempi-sito\
payload\
```

Procedura:

1. estrarre tutto lo ZIP nella cartella definitiva del plugin `RecordsNext`;
2. eseguire `Installa-RecordsNext.bat`;
3. avviare normalmente l'applicazione tramite `Avvia-RecordsNext.vbs`.

Il launcher utilizza `javaw.exe` e non apre una finestra del prompt dei comandi.

### Requisiti

- Windows;
- Java 21 disponibile tramite `JAVA_HOME` oppure nel `PATH`;
- archivi e siti prodotti con Fantacalcio Manager;
- UCanAccess 2.0.9.5, incluso nel pacchetto della release.

Le istruzioni complete sono in [`docs/INSTALLAZIONE.md`](docs/INSTALLAZIONE.md).

## Struttura del repository

```text
src\                  sorgenti Java dell'applicazione e della pipeline
release\              installer, launcher ed esempi distribuiti
docs\                 documentazione e screenshot
config\               configurazioni di esempio
tools\                script di build, pubblicazione, confronto e validazione
benchmark\            infrastruttura per benchmark e misure
pom.xml                configurazione Maven
```

I database, gli archivi stagionali, i report e le configurazioni locali non fanno parte del prodotto distribuito: vengono creati o configurati nell'ambiente dell'utente.

## Build e test

Il progetto usa Java 21 e il Maven Wrapper incluso nel repository.

```powershell
Set-Location "D:\DEV_APPS\RecordsNext"

.\mvnw.cmd clean test
```

Per costruire la release:

```powershell
powershell.exe `
  -NoProfile `
  -ExecutionPolicy Bypass `
  -File ".\tools\Build-RecordsNextRelease.ps1" `
  -Version "1.0.0"
```

Lo ZIP viene prodotto in:

```text
dist\RecordsNext-1.0.0.zip
```

## Continuità tecnica

La documentazione completa del codice funzionante e delle decisioni consolidate è disponibile in:

```text
docs\CODICE_FUNZIONANTE_RECORDSNEXT.md
```

Questo documento è generato tramite:

```text
tools\Create-RecordsNextWorkingCodeMd.ps1
```

## Licenza e autore

powered by **mauz79** © 2026
```

### release\Avvia-RecordsNext.vbs

```text
Option Explicit

Dim shell, fso, root, javaHome, javaw, jarPath, ucaJar, classPath, command, rc
Set shell = CreateObject("WScript.Shell")
Set fso = CreateObject("Scripting.FileSystemObject")

root = fso.GetParentFolderName(WScript.ScriptFullName)
jarPath = fso.BuildPath(root, "RecordsNext.jar")
ucaJar = fso.BuildPath(root, "runtime\ucanaccess\ucanaccess-2.0.9.5.jar")
javaHome = shell.ExpandEnvironmentStrings("%JAVA_HOME%")

If javaHome <> "%JAVA_HOME%" And Len(Trim(javaHome)) > 0 Then
    javaw = fso.BuildPath(javaHome, "bin\javaw.exe")
Else
    javaw = "javaw.exe"
End If

If Not fso.FileExists(jarPath) Then
    MsgBox "RecordsNext.jar non trovato:" & vbCrLf & jarPath, vbCritical, "FCM RecordsNext"
    WScript.Quit 1
End If

If Not fso.FileExists(ucaJar) Then
    MsgBox "Runtime UCanAccess 2.0.9.5 non trovato:" & vbCrLf & ucaJar, vbCritical, "FCM RecordsNext"
    WScript.Quit 1
End If

If javaw <> "javaw.exe" And Not fso.FileExists(javaw) Then
    MsgBox "Java non trovato in JAVA_HOME:" & vbCrLf & javaw, vbCritical, "FCM RecordsNext"
    WScript.Quit 1
End If

classPath = jarPath & ";" & _
    fso.BuildPath(root, "runtime\ucanaccess\*") & ";" & _
    fso.BuildPath(root, "runtime\ucanaccess\lib\*")

command = Quote(javaw) & " -cp " & Quote(classPath) & _
    " it.alterlega.recordsnext.gui.RecordsNextApp"

shell.CurrentDirectory = root
On Error Resume Next
rc = shell.Run(command, 0, False)
If Err.Number <> 0 Then
    MsgBox "Impossibile avviare RecordsNext." & vbCrLf & _
        "Verifica che Java 21 sia installato e disponibile nel PATH o in JAVA_HOME." & vbCrLf & vbCrLf & _
        Err.Description, vbCritical, "FCM RecordsNext"
    WScript.Quit 1
End If
On Error GoTo 0

Function Quote(value)
    Quote = Chr(34) & value & Chr(34)
End Function
```

### release\Installa-RecordsNext.bat

```text
@echo off
setlocal EnableExtensions
cd /d "%~dp0"

set "ROOT=%CD%"
set "SELF=%~f0"

title FCM RecordsNext 1.0 - Installazione

echo.
echo FCM RecordsNext 1.0 - Installazione nella cartella corrente
echo %ROOT%
echo.

if not exist "payload\RecordsNext.jar" goto :missing_payload
if not exist "payload\Avvia-RecordsNext.vbs" goto :missing_payload
if not exist "payload\runtime" goto :missing_payload

>"%ROOT%\.recordsnext-write-test.tmp" echo test 2>nul
if errorlevel 1 goto :write_error
del /q "%ROOT%\.recordsnext-write-test.tmp" >nul 2>&1

copy /Y "payload\RecordsNext.jar" "%ROOT%\RecordsNext.jar" >nul
if errorlevel 1 goto :copy_error
copy /Y "payload\Avvia-RecordsNext.vbs" "%ROOT%\Avvia-RecordsNext.vbs" >nul
if errorlevel 1 goto :copy_error

rem Rimuove il vecchio launcher a console dalle installazioni aggiornate.
if exist "%ROOT%\Avvia-RecordsNext.bat" del /F /Q "%ROOT%\Avvia-RecordsNext.bat" >nul 2>&1

if exist "%ROOT%\runtime" rmdir /S /Q "%ROOT%\runtime"
xcopy "payload\runtime" "%ROOT%\runtime\" /E /I /Y >nul
if errorlevel 1 goto :copy_error

rmdir /S /Q "%ROOT%\payload"
if exist "%ROOT%\payload" goto :copy_error

echo.
echo Installazione completata.
echo.
echo Per i prossimi avvii usa:
echo %ROOT%\Avvia-RecordsNext.vbs
echo.

set /p "START_NOW=Avviare RecordsNext adesso? [S/n]: "
if /I not "%START_NOW%"=="N" start "" wscript.exe "%ROOT%\Avvia-RecordsNext.vbs"

rem Elimina l'installer dopo la chiusura di questo processo.
start "" /MIN cmd.exe /D /C "ping 127.0.0.1 -n 2 >nul & del /F /Q ""%SELF%"""
endlocal
exit /b 0

:missing_payload
echo.
echo ERRORE: il pacchetto di installazione e incompleto.
echo Estrai nuovamente tutto lo ZIP prima di eseguire l'installer.
echo.
pause
exit /b 1

:write_error
echo.
echo ERRORE: non e possibile scrivere nella cartella corrente.
echo Estrai il pacchetto nella cartella plugin desiderata e avvia
echo Installa-RecordsNext.bat come amministratore.
echo.
pause
exit /b 1

:copy_error
echo.
echo ERRORE: installazione non completata.
echo Nessun dato utente e stato cancellato. Ripeti l'operazione come amministratore.
echo.
pause
exit /b 1
```

### release\site-examples\LEGGIMI.txt

```text
ESEMPI SITO RECORDSNEXT

Questi file sono modelli neutri: non contengono stagioni, squadre o risultati incorporati.

Copia recordsnext.html nella root del sito FCM.
Copia la cartella RecordsNext nella root del sito FCM.

La pagina recordsnext.html legge automaticamente:
  js/records2026.recordstagionali.classic.js
  js/records2026.recordstagionali.ru.js

Non esiste una seconda pagina HTML dentro RecordsNext: la pagina applicativa è direttamente recordsnext.html.
Le competizioni classiche sono scoperte dai dati e presentate come checkbox multiselezione.
Si possono quindi combinare liberamente, per esempio Serie A + Serie B, senza gruppi predefiniti.
I pulsanti Tutte e Nessuna permettono di cambiare rapidamente la selezione.
```

### release\site-examples\recordsnext.html

```text
<!doctype html>
<html lang="it">
<head>
<meta charset="utf-8">
<title>FCM RecordsNext - Record stagionali e storici</title>
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="stylesheet" href="RecordsNext/recordsnext.css">
</head>
<body>
<header>
    <h1>FCM RecordsNext - Record stagionali e storici</h1>
    <div>Record raggruppati per stagione, competizione e categoria, con filtri, Top N e risultati cliccabili.</div>
</header>

<div class="toolbar">
    <div class="field"><label>Stagione</label><select id="seasonFilter"><option value="all">Tutte</option></select></div>
    <div class="field"><label>Area</label><select id="areaFilter"><option value="classic">Record classici</option><option value="ru">Riserve d'Ufficio</option><option value="all">Tutto</option></select></div>
    <div class="field competition-field">
        <label>Competizioni classiche</label>
        <div class="competition-actions">
            <button id="selectAllCompetitionsBtn" type="button" class="compact secondary">Tutte</button>
            <button id="clearCompetitionsBtn" type="button" class="compact secondary">Nessuna</button>
            <span id="competitionCount" class="small"></span>
        </div>
        <div id="competitionFilter" class="competition-list" role="group" aria-label="Competizioni"></div>
    </div>
    <div class="field"><label>Record</label><select id="recordFilter"><option value="all">Tutti i record</option></select></div>
    <div class="field"><label>Tipo squadre</label><select id="teamModeFilter"><option value="all">Tutte</option><option value="current">Attuali</option></select></div>
    <div class="field"><label>Squadra</label><select id="teamFilter"><option value="">Tutte</option></select></div>
    <div class="field"><label>Cerca libera</label><input id="textFilter" type="text" placeholder="squadra, risultato, punteggio..."></div>
    <div class="field"><label>Top per record</label><select id="topFilter"><option value="1">Top 1</option><option value="5" selected>Top 5</option><option value="10">Top 10</option><option value="20">Top 20</option><option value="30">Top 30</option><option value="999999">Tutti</option></select></div>
    <button id="renderBtn" type="button">Aggiorna</button>
    <button id="resetBtn" type="button" class="secondary">Reset filtri</button>
</div>

<main>
    <div id="summary" class="summary"></div>
    <div id="app"></div>
</main>

<!-- Questi file sono generati/pubblicati da RecordsNext nella cartella js del sito. -->
<script src="js/records2026.recordstagionali.classic.js"></script>
<script src="js/records2026.recordstagionali.ru.js"></script>
<script src="RecordsNext/recordsnext.js"></script>
</body>
</html>
```

### release\site-examples\RecordsNext\recordsnext.css

```text
body{margin:0;font-family:Arial,Helvetica,sans-serif;background:#f4f4f4;color:#222}
header{background:#1f1f1f;color:#fff;padding:16px 20px}
header h1{margin:0 0 4px 0;font-size:22px}
header div{font-size:13px;color:#ccc}
.toolbar{position:sticky;top:0;background:#fff;border-bottom:1px solid #ccc;padding:12px 16px;display:flex;flex-wrap:wrap;gap:10px;align-items:end;z-index:20}
.field{display:flex;flex-direction:column;gap:3px}.field label{font-size:12px;color:#666}
select,input{font-size:14px;padding:6px 8px;border:1px solid #bbb;border-radius:4px;min-width:160px}
button{font-size:14px;padding:7px 12px;border:1px solid #2f5d8c;background:#2f5d8c;color:#fff;border-radius:4px;cursor:pointer}
button.secondary{border-color:#777;background:#777}
main{padding:16px}.summary{background:#fff8df;border:1px solid #e0cf91;padding:10px 12px;margin-bottom:14px;font-size:13px}
.block{background:#fff;border:1px solid #ccc;border-radius:6px;margin:0 0 18px 0;overflow:hidden}.block h2{margin:0;background:#e9e9e9;padding:10px 12px;font-size:17px}
.block .meta{font-size:12px;color:#666;border-bottom:1px solid #ddd;padding:7px 12px}.table-wrap{overflow-x:auto}
table{width:100%;border-collapse:collapse;font-size:13px}th,td{border-bottom:1px solid #eee;padding:7px 8px;text-align:left;vertical-align:top;white-space:nowrap}th{background:#fafafa}tr:hover td{background:#f8fbff}
a{color:#1b5c98;text-decoration:none}a:hover{text-decoration:underline}.badge{display:inline-block;background:#e9eef5;border-radius:10px;padding:2px 7px;font-size:11px;color:#333;margin-right:4px}.empty{padding:20px;color:#666;background:#fff;border:1px solid #ccc;border-radius:6px}.small{font-size:12px;color:#666}.warn{color:#8a4b00;font-weight:bold}.muted{color:#777}

.competition-field{min-width:320px;max-width:620px;flex:1 1 420px}
.competition-actions{display:flex;align-items:center;gap:6px;margin-bottom:5px}
button.compact{font-size:12px;padding:4px 8px}
.competition-list{display:flex;flex-wrap:wrap;gap:5px 10px;max-height:120px;overflow:auto;border:1px solid #bbb;border-radius:4px;padding:7px;background:#fafafa}
.competition-option{display:inline-flex;align-items:center;gap:5px;font-size:13px;color:#222;white-space:nowrap;cursor:pointer}
.competition-option input{min-width:auto;margin:0;padding:0}
```

### release\site-examples\RecordsNext\recordsnext.js

```text
(function(){
"use strict";

const DATA = {
    meta: window.RECORDS2026_PREVIEW_META || { generatedAt: "dati pubblicati da RecordsNext" },
    classicFiles: window.RECORDS2026_PREVIEW_CLASSIC || [],
    ruFiles: window.RECORDS2026_PREVIEW_RU || []
};

const COMP_LABELS = {
    serie_a: "Serie A", serie_b: "Serie B", serie_c: "Serie C",
    campionato: "Campionato", coppa_di_lega: "Coppa di Lega", supercoppa_di_lega: "Supercoppa di Lega",
    coppa_lega_serie_a: "Coppa di Lega Serie A", coppa_lega_serie_b: "Coppa di Lega Serie B", coppa_lega_serie_c: "Coppa di Lega Serie C",
    coppa_tra_le_coppe: "Coppa tra le Coppe", europa_pipps: "Europa Pipps",
    supercoppa_serie_a: "Supercoppa Serie A", supercoppa_serie_b: "Supercoppa Serie B", supercoppa_serie_c: "Supercoppa Serie C",
    playoff_playout: "Play Off / Play Out"
};

const RECORD_ORDER = [
    "puntiSquadraMax",
    "puntiSquadraMin",

    "partitePiuGolRegolamentari",
    "partitePiuScartoRegolamentari",

    "serieSenzaSconfitte",
    "serieSenzaVittorie",
    "vittorieConsecutive",
    "pareggiConsecutivi",
    "sconfitteConsecutive",

    "modDifesaMax",
    "modDifesaTotaleSquadre",

    "capitanoVolteSquadre",
    "capitanoTotaleSquadre",
    "capitanoSerieSquadre",

    "cleanSheetPortiereVolteSquadre",
    "cleanSheetPortiereTotaleSquadre",
    "cleanSheetPortiereSerieSquadre",

    "ammonizioniSquadre",
    "espulsioniSquadre",
    "espulsioniGiocatori",
    "assistSquadre",
    "autogolSquadre",
    "rigoriSbagliatiSquadre",
    "rigoriParatiSquadre",
    "golRigoreSquadre"
];

const CLASSIC_COLUMNS = {
    puntiSquadraMax: ["pos","stagione","squadra","avversaria","competizioneNome","giornata","risultato","punteggio","valore"],
    puntiSquadraMin: ["pos","stagione","squadra","avversaria","competizioneNome","giornata","risultato","punteggio","valore"],

    partitePiuGolRegolamentari: ["pos","stagione","squadra","avversaria","competizioneNome","giornata","risultato","punteggio","valore"],
    partitePiuScartoRegolamentari: ["pos","stagione","squadra","avversaria","competizioneNome","giornata","risultato","punteggio","valore"],

    serieSenzaSconfitte: ["pos","stagione","squadra","competizioneNome","daGiornata","aGiornata","valore","vittorie","pareggi","dettagliCount"],
    serieSenzaVittorie: ["pos","stagione","squadra","competizioneNome","daGiornata","aGiornata","valore","dettagliCount"],
    vittorieConsecutive: ["pos","stagione","squadra","competizioneNome","daGiornata","aGiornata","valore","dettagliCount"],
    pareggiConsecutivi: ["pos","stagione","squadra","competizioneNome","daGiornata","aGiornata","valore","dettagliCount"],
    sconfitteConsecutive: ["pos","stagione","squadra","competizioneNome","daGiornata","aGiornata","valore","dettagliCount"],

    modDifesaMax: ["pos","stagione","squadra","avversaria","competizioneNome","giornata","risultato","punteggio","valore"],
    modDifesaTotaleSquadre: ["pos","stagione","squadra","competizioneNome","valore","dettagliCount"],

    defaultTeam: ["pos","stagione","squadra","competizioneNome","valore","dettagliCount"],
    defaultPlayer: ["pos","stagione","giocatore","competizioneNome","valore","dettagliCount"]
};

const RU_COLUMNS_FALLBACK = {
    partiteConPiuRU: ["pos","stagione","competizione","giornataFCM","partita","numeroRU","valoreRUTotale","risultato","punteggio"],
    partiteConRU: ["pos","stagione","competizione","giornataFCM","squadra","avversaria","numeroRU","tipiRU","risultato","punteggio"],
    ruDettaglio: ["pos","stagione","competizione","giornataFCM","squadra","avversaria","tipoRU","ruoloRU","valoreRU","risultato","punteggio"]
};

const classicItems = DATA.classicFiles || [];
const ruItems = DATA.ruFiles || [];
let competitionSelectionInitialized = false;
let competitionSelectAllMode = true;

function h(s){ if(s === null || s === undefined) return ""; return String(s).replace(/&/g,"&amp;").replace(/</g,"&lt;").replace(/>/g,"&gt;").replace(/\"/g,"&quot;"); }
function attr(s){ return h(s).replace(/'/g,"&#39;"); }
function normalizeUrl(url){
    if(!url) return "";
    return String(url)
        .trim()
        .replace(/^(?:\.\.\/|\.\/)?(?=lega[^/]*\/)/i, "/")
        .replace(/\/ris\.php(?=\?)/i, "/ris.htm");
}
function prettifyCompetitionId(id){
    return String(id || "")
        .replace(/[_-]+/g, " ")
        .replace(/\b\w/g, function(c){ return c.toUpperCase(); });
}

function getCompLabel(id){
    if(COMP_LABELS[id]) return COMP_LABELS[id];
    for(const item of classicItems){
        if(item.id !== id) continue;
        const recs = ((item.data || {}).records || {});
        for(const key of Object.keys(recs)){
            const rows = recs[key];
            if(!Array.isArray(rows)) continue;
            const row = rows.find(function(r){ return r && r.competizioneNome; });
            if(row) return row.competizioneNome;
        }
    }
    return prettifyCompetitionId(id);
}
function val(id){ return document.getElementById(id).value; }
function selectedSeason(){ return document.getElementById("seasonFilter") ? val("seasonFilter") : "all"; }

function getClassicItemsForCurrentSeason(){
    const season = selectedSeason();
    return classicItems.filter(function(f){ return season === "all" || f.stagione === season; });
}

function getRuItemsForCurrentSeason(){
    const season = selectedSeason();
    return ruItems.filter(function(f){ return season === "all" || f.stagione === season; });
}

function allCompetitionIds(){
    const ids = {};
    getClassicItemsForCurrentSeason().forEach(function(f){ ids[f.id] = true; });
    return Object.keys(ids).sort(function(a,b){ return getCompLabel(a).localeCompare(getCompLabel(b)); });
}

function selectedCompetitionIds(){
    return Array.from(document.querySelectorAll("#competitionFilter input[type='checkbox']:checked"))
        .map(function(input){ return input.value; });
}

function updateCompetitionCount(){
    const all = document.querySelectorAll("#competitionFilter input[type='checkbox']").length;
    const selected = selectedCompetitionIds().length;
    document.getElementById("competitionCount").textContent = selected + " / " + all;
}

function setAllCompetitions(checked){
    document.querySelectorAll("#competitionFilter input[type='checkbox']").forEach(function(input){ input.checked = checked; });
    competitionSelectAllMode = checked;
    updateCompetitionCount();
    fillRecordFilter();
    fillTeamFilter();
    render();
}

function fillSeasonFilter(){
    const sel = document.getElementById("seasonFilter");
    const old = sel.value;
    const seasons = {};
    classicItems.forEach(function(f){ seasons[f.stagione] = true; });
    sel.innerHTML = "<option value='all'>Tutte</option>";
    Object.keys(seasons).sort().forEach(function(s){ const opt = document.createElement("option"); opt.value = s; opt.textContent = s; sel.appendChild(opt); });
    if(Array.from(sel.options).some(function(o){ return o.value === old; })) sel.value = old;
}

function fillCompetitionFilter(){
    const box = document.getElementById("competitionFilter");
    const previous = new Set(selectedCompetitionIds());
    const ids = allCompetitionIds();
    box.innerHTML = "";

    ids.forEach(function(id){
        const label = document.createElement("label");
        label.className = "competition-option";
        const input = document.createElement("input");
        input.type = "checkbox";
        input.value = id;
        input.checked = !competitionSelectionInitialized || competitionSelectAllMode || previous.has(id);
        const text = document.createElement("span");
        text.textContent = getCompLabel(id);
        label.appendChild(input);
        label.appendChild(text);
        box.appendChild(label);
    });

    competitionSelectionInitialized = true;
    updateCompetitionCount();
}

function getClassicRecordIds(){
    const found = {};
    const selectedIds = new Set(selectedCompetitionIds());
    getClassicItemsForCurrentSeason().forEach(function(item){
        if(selectedIds.size && !selectedIds.has(item.id)) return;
        const recs = (item.data || {}).records || {};
        Object.keys(recs).forEach(function(k){ if(Array.isArray(recs[k])) found[k] = true; });
    });
    const ordered = [];
    RECORD_ORDER.forEach(function(k){ if(found[k]) ordered.push(k); });
    Object.keys(found).sort().forEach(function(k){ if(ordered.indexOf(k) < 0) ordered.push(k); });
    return ordered;
}

function getClassicRecordTitle(recordId, rows){ if(rows && rows.length && rows[0].nome) return rows[0].nome; return recordId; }

function getRuRecordIds(){
    const found = {};
    getRuItemsForCurrentSeason().forEach(function(item){
        const ruData = item.data || {};
        Object.keys(ruData.views || {}).forEach(function(k){ if(Array.isArray(ruData.views[k])) found[k] = true; });
        Object.keys(ruData.dettaglio || {}).forEach(function(k){ if(Array.isArray(ruData.dettaglio[k])) found["dettaglio." + k] = true; });
    });
    return Object.keys(found).sort();
}

function getRuTitle(id){
    const cleanId = id.replace(/^dettaglio\./,"");
    for(const item of getRuItemsForCurrentSeason()){
        const meta = ((item.data || {}).curiosita || []).find(function(x){ return x.id === cleanId; });
        if(meta && meta.nome) return meta.nome;
    }
    if(id === "dettaglio.ruDettaglio") return "Dettaglio Riserve d'Ufficio";
    if(id === "dettaglio.ruTeamMatch") return "Partite squadra con Riserve d'Ufficio";
    return id;
}

function fillRecordFilter(){
    const area = val("areaFilter");
    const sel = document.getElementById("recordFilter");
    const current = sel.value;
    sel.innerHTML = "<option value='all'>Tutti i record</option>";
    if(area === "classic" || area === "all"){
        getClassicRecordIds().forEach(function(id){
            let sample = [];
            const selectedIds = new Set(selectedCompetitionIds());
            for(const item of getClassicItemsForCurrentSeason()){
                if(selectedIds.size && !selectedIds.has(item.id)) continue;
                const arr = (((item.data || {}).records || {})[id]);
                if(Array.isArray(arr) && arr.length){ sample = arr; break; }
            }
            const opt = document.createElement("option");
            opt.value = "classic|" + id;
            opt.textContent = "Classici - " + getClassicRecordTitle(id, sample);
            sel.appendChild(opt);
        });
    }
    if(area === "ru" || area === "all"){
        getRuRecordIds().forEach(function(id){ const opt = document.createElement("option"); opt.value = "ru|" + id; opt.textContent = "RU - " + getRuTitle(id); sel.appendChild(opt); });
    }
    if(Array.from(sel.options).some(function(o){ return o.value === current; })) sel.value = current;
}

function currentTeamsMap(){
    const map = {};
    const seasons = {};
    (DATA.classicFiles || []).forEach(function(f){ seasons[f.stagione] = true; });
    const latest = Object.keys(seasons).sort().slice(-1)[0] || "";
    (DATA.classicFiles || []).forEach(function(item){
        if(item.stagione !== latest) return;
        const recs = (item.data || {}).records || {};
        Object.keys(recs).forEach(function(recordId){
            const arr = recs[recordId];
            if(!Array.isArray(arr)) return;
            arr.forEach(function(r){
                if(r.squadra) map[String(r.squadra)] = true;
                if(r.avversaria) map[String(r.avversaria)] = true;
            });
        });
    });
    return map;
}

function collectTeams(){
    const map = {};
    const mode = val("teamModeFilter");
    const currentMap = mode === "current" ? currentTeamsMap() : null;
    getClassicItemsForCurrentSeason().forEach(function(item){
        const recs = (item.data || {}).records || {};
        Object.keys(recs).forEach(function(recordId){
            const arr = recs[recordId];
            if(!Array.isArray(arr)) return;
            arr.forEach(function(r){
                if(r.squadra && String(r.squadra).trim() !== "" && String(r.idSquadra || "") !== "0" && (!currentMap || currentMap[String(r.squadra)])) map[String(r.squadra)] = true;
                if(r.avversaria && String(r.avversaria).trim() !== "" && (!currentMap || currentMap[String(r.avversaria)])) map[String(r.avversaria)] = true;
            });
        });
    });
    getRuItemsForCurrentSeason().forEach(function(item){
        ["views","dettaglio"].forEach(function(section){
            const obj = (item.data || {})[section] || {};
            Object.keys(obj).forEach(function(k){
                const arr = obj[k];
                if(!Array.isArray(arr)) return;
                arr.forEach(function(r){
                    if(r.squadra && String(r.squadra).trim() !== "" && (!currentMap || currentMap[String(r.squadra)])) map[String(r.squadra)] = true;
                    if(r.avversaria && String(r.avversaria).trim() !== "" && (!currentMap || currentMap[String(r.avversaria)])) map[String(r.avversaria)] = true;
                });
            });
        });
    });
    return Object.keys(map).sort(function(a,b){ return a.localeCompare(b); });
}

function fillTeamFilter(){
    const sel = document.getElementById("teamFilter");
    const old = sel.value;
    sel.innerHTML = "<option value=''>Tutte</option>";
    collectTeams().forEach(function(t){ const opt = document.createElement("option"); opt.value = t; opt.textContent = t; sel.appendChild(opt); });
    if(Array.from(sel.options).some(function(o){ return o.value === old; })) sel.value = old;
}

function resetTeamFilter(){ document.getElementById("teamFilter").value = ""; }
function resetTextFilter(){ document.getElementById("textFilter").value = ""; }

function resetFilters(){
    document.getElementById("seasonFilter").value = "all";
    document.getElementById("areaFilter").value = "classic";
    document.getElementById("topFilter").value = "5";
    document.getElementById("teamModeFilter").value = "all";
    resetTextFilter();
    competitionSelectAllMode = true;
    competitionSelectionInitialized = false;
    fillCompetitionFilter();
    fillRecordFilter();
    document.getElementById("recordFilter").value = "all";
    fillTeamFilter();
    resetTeamFilter();
    render();
}

function teamMatches(row, selectedTeam){
    if(!selectedTeam) return true;
    return String(row.squadra || "") === selectedTeam || String(row.avversaria || "") === selectedTeam || String(row.partita || "").indexOf(selectedTeam) >= 0;
}

function textMatches(row, needle){
    if(!needle) return true;
    const s = [row.stagione,row.squadra,row.avversaria,row.partita,row.giocatore,row.risultato,row.punteggio,row.giornata,row.giornataFCM,row.competizioneNome,row.competizione,row.valore]
        .filter(function(x){ return x !== null && x !== undefined; })
        .join(" ")
        .toLowerCase();
    return s.indexOf(needle.toLowerCase()) >= 0;
}

function isCleanClassicRow(recordId, row){
    if(!row) return false;

    const requiresTeam = [
        "serieSenzaSconfitte",
        "serieSenzaVittorie",
        "vittorieConsecutive",
        "pareggiConsecutivi",
        "sconfitteConsecutive",

        "puntiSquadraMax",
        "puntiSquadraMin",

        "partitePiuGolRegolamentari",
        "partitePiuScartoRegolamentari",

        "modDifesaMax",
        "modDifesaTotaleSquadre",

        "capitanoVolteSquadre",
        "capitanoTotaleSquadre",
        "capitanoSerieSquadre",

        "cleanSheetPortiereVolteSquadre",
        "cleanSheetPortiereTotaleSquadre",
        "cleanSheetPortiereSerieSquadre",

        "ammonizioniSquadre",
        "espulsioniSquadre",
        "assistSquadre",
        "autogolSquadre",
        "rigoriSbagliatiSquadre",
        "rigoriParatiSquadre",
        "golRigoreSquadre"
    ];

    if(requiresTeam.indexOf(recordId) >= 0){
        if(!row.squadra || String(row.squadra).trim() === "") return false;
        if(String(row.idSquadra || "") === "0") return false;
    }

    return true;
}

function sortRows(rows){
    return rows.slice().sort(function(a,b){
        const av = Number(a.valore ?? a.numeroRU ?? a.valoreRUTotale ?? 0);
        const bv = Number(b.valore ?? b.numeroRU ?? b.valoreRUTotale ?? 0);
        return bv - av;
    });
}

function addCompetitionRows(recordId, compIds, selectedTeam, textNeedle){
    let rows = [];
    getClassicItemsForCurrentSeason().forEach(function(item){
        if(compIds.indexOf(item.id) < 0) return;
        const arr = (((item.data || {}).records || {})[recordId]);
        if(!Array.isArray(arr)) return;
        arr.forEach(function(r){
            if(!isCleanClassicRow(recordId, r)) return;
            const copy = Object.assign({}, r);
            copy._competizioneId = item.id;
            copy.stagione = copy.stagione || item.stagione;
            copy.competizioneNome = copy.competizioneNome || getCompLabel(item.id);
            if(!teamMatches(copy, selectedTeam)) return;
            if(!textMatches(copy, textNeedle)) return;
            rows.push(copy);
        });
    });
    return sortRows(rows);
}

function getRuRows(recordId, selectedTeam, textNeedle){
    let rows = [];
    getRuItemsForCurrentSeason().forEach(function(item){
        const ruData = item.data || {};
        let arr = [];
        if(recordId.indexOf("dettaglio.") === 0){
            const key = recordId.replace(/^dettaglio\./,"");
            arr = Array.isArray(ruData.dettaglio && ruData.dettaglio[key]) ? ruData.dettaglio[key] : [];
        } else {
            arr = Array.isArray(ruData.views && ruData.views[recordId]) ? ruData.views[recordId] : [];
        }
        arr.forEach(function(r){
            const copy = Object.assign({}, r);
            copy.stagione = copy.stagione || item.stagione;

            if(!teamMatches(copy, selectedTeam)) return;
            if(!textMatches(copy, textNeedle)) return;

            rows.push(copy);
        });
    });
    return sortRows(rows);
}

function valueFor(row, col, index){
    if(col === "pos") return index + 1;
    if(col === "dettagliCount") return row.dettagliCount ?? (Array.isArray(row.dettagli) ? row.dettagli.length : "");
    if(col === "competizioneNome") return row.competizioneNome || row.competizione || getCompLabel(row._competizioneId);
    return row[col];
}

function renderValue(row, col, index){
    const valx = valueFor(row, col, index);
    const url = normalizeUrl(row.urlTabellino);
    if((col === "risultato" || col === "giornata" || col === "giornataFCM") && url) return "<a target='_blank' href='" + attr(url) + "'>" + h(valx) + "</a>";
    if(col === "punteggio" && url && !row.risultato) return "<a target='_blank' href='" + attr(url) + "'>" + h(valx) + "</a>";
    return h(valx);
}

function colsForClassic(recordId, rows){
    if(CLASSIC_COLUMNS[recordId]) return CLASSIC_COLUMNS[recordId];
    if(rows.some(function(r){ return r.giocatore; })) return CLASSIC_COLUMNS.defaultPlayer;
    return CLASSIC_COLUMNS.defaultTeam;
}

function colsForRu(recordId, rows){
    const cleanId = recordId.replace(/^dettaglio\./,"");
    for(const item of getRuItemsForCurrentSeason()){
        const meta = (((item.data || {}).curiosita || [])).find(function(x){ return x.id === cleanId; });
        if(meta && Array.isArray(meta.colonne)) return ["pos","stagione"].concat(meta.colonne.map(function(c){ return c.key; }).filter(function(k){ return k !== "stagione"; }));
    }
    if(RU_COLUMNS_FALLBACK[cleanId]) return RU_COLUMNS_FALLBACK[cleanId];
    return ["pos"].concat(Object.keys(rows[0] || {}).filter(function(k){ return k !== "urlTabellino"; }).slice(0,9));
}

function labelFor(c){
    const labels = { pos:"#", stagione:"Stagione", squadra:"Squadra", avversaria:"Avversaria", competizioneNome:"Competizione", competizione:"Competizione", giornata:"Giornata", giornataFCM:"Giornata", giornataDiA:"Giorn. A", risultato:"Ris.", punteggio:"Punteggio", valore:"Valore", dettagliCount:"Dett.", partita:"Partita", numeroRU:"RU", valoreRUTotale:"Valore RU", dettaglioRU:"Dettaglio RU", tipiRU:"Tipo RU", tipoRU:"Tipo", ruoloRU:"Ruolo", giocatore:"Giocatore", daGiornata:"Da", aGiornata:"A", vittorie:"V", pareggi:"P" };
    return labels[c] || c;
}

function renderTable(rows, cols){
    if(!rows.length) return "<div class='empty'>Nessuna riga.</div>";
    let html = "<div class='table-wrap'><table><thead><tr>";
    cols.forEach(function(c){ html += "<th>" + h(labelFor(c)) + "</th>"; });
    html += "</tr></thead><tbody>";
    rows.forEach(function(r,i){
        html += "<tr>";
        cols.forEach(function(c){ html += "<td>" + renderValue(r,c,i) + "</td>"; });
        html += "</tr>";
    });
    html += "</tbody></table></div>";
    return html;
}

function renderBlock(title, subtitle, rows, cols, topN){
    const shown = rows.slice(0, topN);
    let html = "<section class='block'>";
    html += "<h2>" + h(title) + "</h2>";
    html += "<div class='meta'>" + subtitle + " <span class='small'>Totale dopo filtri: " + rows.length + " | Mostrate: " + shown.length + "</span></div>";
    html += renderTable(shown, cols);
    html += "</section>";
    return html;
}

function selectedRecordText(){
    const sel = document.getElementById("recordFilter");
    return sel.options[sel.selectedIndex] ? sel.options[sel.selectedIndex].textContent : "";
}

function render(){
    const area = val("areaFilter");
    const recordSel = val("recordFilter");
    const topN = parseInt(val("topFilter"), 10);
    const selectedTeam = val("teamFilter");
    const textNeedle = val("textFilter").trim();
    const compIds = selectedCompetitionIds();
    let html = "";
    let blockCount = 0;
    const classicRequested = area === "classic" || area === "all" || recordSel.indexOf("classic|") === 0;

    if(classicRequested && compIds.length === 0){
        document.getElementById("summary").innerHTML = "<b>Nessuna competizione classica selezionata.</b> Selezionane almeno una oppure usa il pulsante Tutte.";
        document.getElementById("app").innerHTML = "<div class='empty'>Nessun record classico da mostrare: la selezione delle competizioni è vuota.</div>";
        return;
    }

    const renderClassic = function(recordId){
        const rows = addCompetitionRows(recordId, compIds, selectedTeam, textNeedle);
        if(!rows.length) return;
        const title = getClassicRecordTitle(recordId, rows);
        const cols = colsForClassic(recordId, rows);
        const subtitle = "<span class='badge'>Classici</span><span class='badge'>" + h(compIds.length + " competizioni") + "</span>";
        html += renderBlock(title, subtitle, rows, cols, topN);
        blockCount++;
    };

    const renderRu = function(recordId){
        const rows = getRuRows(recordId, selectedTeam, textNeedle);
        if(!rows.length) return;
        const title = getRuTitle(recordId);
        const cols = colsForRu(recordId, rows);
        const subtitle = "<span class='badge'>Riserve d'Ufficio</span>";
        html += renderBlock(title, subtitle, rows, cols, topN);
        blockCount++;
    };

    if(recordSel !== "all"){
        const parts = recordSel.split("|");
        if(parts[0] === "classic") renderClassic(parts[1]);
        if(parts[0] === "ru") renderRu(parts[1]);
    } else {
        if(area === "classic" || area === "all") getClassicRecordIds().forEach(renderClassic);
        if(area === "ru" || area === "all") getRuRecordIds().forEach(renderRu);
    }

    const activeFilterWarn = (selectedTeam || textNeedle) ? " <span class='warn'>Filtro squadra/testo attivo</span>" : "";
    const topLabel = topN >= 999999 ? "Tutti" : ("Top " + topN);
    document.getElementById("summary").innerHTML =
        "<b>Stagione:</b> " + h(selectedSeason()) +
        " &nbsp; <b>Competizioni:</b> " + h(compIds.length ? compIds.map(getCompLabel).join(", ") : "Nessuna") +
        " &nbsp; <b>Record:</b> " + h(selectedRecordText()) +
        " &nbsp; <b>Squadra:</b> " + h(selectedTeam || "Tutte") +
        " &nbsp; <b>Cerca:</b> " + h(textNeedle || "-") +
        " &nbsp; <b>Top:</b> " + h(topLabel) + activeFilterWarn +
        "<br><span class='small'>Blocchi: " + blockCount + " | Generato: " + h(DATA.meta.generatedAt) + " | Tutti i record = pagina lunga raggruppata. Top N applicato a ogni blocco.</span>";

    document.getElementById("app").innerHTML = html || "<div class='empty'>Nessun record con questi filtri.</div>";
}

fillSeasonFilter();
fillCompetitionFilter();
fillRecordFilter();
fillTeamFilter();

["seasonFilter","areaFilter","recordFilter"].forEach(function(id){
    document.getElementById(id).addEventListener("change", function(){
        resetTeamFilter();
        if(id === "seasonFilter") { fillCompetitionFilter(); fillRecordFilter(); fillTeamFilter(); }
        if(id === "areaFilter") { fillRecordFilter(); fillTeamFilter(); }
        if(id === "recordFilter") { fillTeamFilter(); }
        render();
    });
});

document.getElementById("competitionFilter").addEventListener("change", function(event){
    if(!event.target.matches("input[type='checkbox']")) return;
    const inputs = Array.from(document.querySelectorAll("#competitionFilter input[type='checkbox']"));
    competitionSelectAllMode = inputs.length > 0 && inputs.every(function(input){ return input.checked; });
    updateCompetitionCount();
    resetTeamFilter();
    fillRecordFilter();
    fillTeamFilter();
    render();
});

document.getElementById("topFilter").addEventListener("change", render);
document.getElementById("teamFilter").addEventListener("change", render);
document.getElementById("teamModeFilter").addEventListener("change", function(){ fillTeamFilter(); resetTeamFilter(); render(); });
document.getElementById("textFilter").addEventListener("input", render);
document.getElementById("selectAllCompetitionsBtn").addEventListener("click", function(){ setAllCompetitions(true); });
document.getElementById("clearCompetitionsBtn").addEventListener("click", function(){ setAllCompetitions(false); });
document.getElementById("renderBtn").addEventListener("click", render);
document.getElementById("resetBtn").addEventListener("click", resetFilters);

render();
})();
```

### src\main\java\it\alterlega\recordsnext\app\PipelineConfig.java

```java
package it.alterlega.recordsnext.app;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public record PipelineConfig(Path projectRoot, Path reports, Path classicArchive, Path ruArchive,
                             Path staging, Path siteJs, List<String> seasons) {
    public static PipelineConfig load(Path projectRoot, Path file) throws IOException {
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(file)) {
            p.load(in);
        }
        List<String> seasons = Arrays.stream(p.getProperty("seasons", "").split("\\s*,\\s*"))
            .filter(s -> !s.isBlank()).toList();
        Path normalizedRoot = projectRoot.toAbsolutePath().normalize();
        return new PipelineConfig(normalizedRoot,
            resolve(normalizedRoot, p.getProperty("reports", "data/reports")),
            resolve(normalizedRoot, p.getProperty("classicArchive", "data/records-archive/stagioni")),
            resolve(normalizedRoot, p.getProperty("ruArchive", "data/records-archive/riserveufficio")),
            resolve(normalizedRoot, p.getProperty("staging", "data/site-export-staging")),
            resolvePublishDirectory(normalizedRoot, p), seasons);
    }

    public static Path resolvePublishDirectory(Path projectRoot, Properties properties) {
        String mode = properties.getProperty("publish.destinationMode", "currentSeason").trim();
        if ("custom".equalsIgnoreCase(mode)) {
            String custom = properties.getProperty("publish.customDirectory", "").trim();
            if (!custom.isEmpty()) {
                return resolve(projectRoot, custom);
            }
        }

        Path database = resolve(projectRoot,
            properties.getProperty("database", "data/database/recordsnext.db"));
        if (Files.isRegularFile(database)) {
            String sql = """
                SELECT c.local_site_path
                FROM rn_season s
                JOIN rn_season_configuration c ON c.season_id=s.season_id
                WHERE s.is_anchor=1
                  AND c.local_site_path IS NOT NULL
                  AND TRIM(c.local_site_path)<>''
                ORDER BY s.sort_order DESC
                LIMIT 1
                """;
            try {
                Class.forName("org.sqlite.JDBC");
                try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + database);
                     Statement statement = connection.createStatement();
                     ResultSet result = statement.executeQuery(sql)) {
                    if (result.next()) {
                        return Path.of(result.getString(1)).resolve("js").toAbsolutePath().normalize();
                    }
                }
            } catch (Exception ignored) {
                // Fallback to the legacy property below.
            }
        }
        return resolve(projectRoot,
            properties.getProperty("siteJs", "E:/fantacalcio/Lega2025/js"));
    }

    private static Path resolve(Path root, String value) {
        Path path = Path.of(value);
        return (path.isAbsolute() ? path : root.resolve(path)).normalize();
    }
}
```

### src\main\java\it\alterlega\recordsnext\app\ProcessingMode.java

```java
package it.alterlega.recordsnext.app;

public enum ProcessingMode {
    FULL,
    CONSOLIDATED
}
```

### src\main\java\it\alterlega\recordsnext\app\ProcessingOptions.java

```java
package it.alterlega.recordsnext.app;

public record ProcessingOptions(boolean classic, boolean ru, boolean generateJs, boolean publish) {
    public ProcessingOptions {
        if (!classic && !ru) {
            throw new IllegalArgumentException("Selezionare almeno un'elaborazione");
        }
        if (publish && !generateJs) {
            throw new IllegalArgumentException("Per pubblicare nel sito occorre generare i file JavaScript");
        }
    }
}
```

### src\main\java\it\alterlega\recordsnext\app\RecordsNextPipeline.java

```java
package it.alterlega.recordsnext.app;

import it.alterlega.recordsnext.Records2026SitePublisher;
import it.alterlega.recordsnext.RiserveUfficioArchiveBuilder;
import it.alterlega.recordsnext.SeasonRecordsArchiveBuilder;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public final class RecordsNextPipeline {
    public interface Listener {
        void phase(String text, int percent);
        default void timing(String text) { phase("TEMPO " + text, -1); }
    }
    public record Result(int classicEntries, int ruSeasons, int files, int published) {}

    public Result run(PipelineConfig c, ProcessingOptions o, ProcessingMode mode, Listener l) throws Exception {
        long totalStarted = System.nanoTime();
        Path database = c.projectRoot().resolve("data/database/recordsnext.db").normalize();
        RecordsNextPreparationService preparation = new RecordsNextPreparationService(c.projectRoot(), database);

        long preparationStarted = System.nanoTime();
        List<String> changedSeasons = preparation.prepare(mode, c.seasons(), l);
        l.timing("preparazione complessiva: " + elapsed(preparationStarted));

        if (o.classic()) {
            l.phase("Generazione record classici", 55);
            long started = System.nanoTime();
            SeasonRecordsArchiveBuilder.build(c.reports(), c.classicArchive(), changedSeasons);
            l.timing("record classici: " + elapsed(started));
        }
        if (o.ru()) {
            l.phase("Generazione riserve d'ufficio", 68);
            long started = System.nanoTime();
            RiserveUfficioArchiveBuilder.build(c.reports(), c.ruArchive(), changedSeasons);
            l.timing("riserve d'ufficio: " + elapsed(started));
        }

        Result result;
        if (!o.generateJs()) {
            l.phase("Archivi elaborati; generazione JavaScript non richiesta", 96);
            result = new Result(0, 0, 0, 0);
        } else {
            l.phase(o.publish() ? "Generazione e pubblicazione JavaScript" : "Generazione JavaScript", 82);
            long started = System.nanoTime();
            var r = Records2026SitePublisher.run(
                c.classicArchive(), c.ruArchive(), c.staging(), c.siteJs(),
                !o.publish(), o.classic(), o.ru());
            l.timing((o.publish() ? "generazione e pubblicazione JavaScript: " : "generazione JavaScript: ")
                + elapsed(started));
            result = new Result(r.classicEntries(), r.ruSeasons(), r.validatedFiles(), r.publishedFiles());
        }
        preparation.saveConsolidation(c.seasons());
        l.timing("totale elaborazione: " + elapsed(totalStarted));
        l.phase("Elaborazione completata e consolidamento aggiornato", 100);
        return result;
    }

    public boolean hasConsolidation(PipelineConfig c) {
        Path database = c.projectRoot().resolve("data/database/recordsnext.db").normalize();
        return new RecordsNextPreparationService(c.projectRoot(), database).hasConsolidation();
    }

    private static String elapsed(long started) {
        double seconds = (System.nanoTime() - started) / 1_000_000_000.0;
        return String.format(Locale.ROOT, "%.3f s", seconds);
    }
}
```

### src\main\java\it\alterlega\recordsnext\app\RecordsNextPreparationService.java

```java
package it.alterlega.recordsnext.app;

import it.alterlega.recordsnext.CanonicalViews;
import it.alterlega.recordsnext.ConfigurationSchema;
import it.alterlega.recordsnext.RawSqliteImporter;
import it.alterlega.recordsnext.SeasonNormalizedBatchExporter;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

final class RecordsNextPreparationService {
    record SeasonSource(String id, String type, String fcm, String fca,
                        String localSite, String onlineSite) {}

    private final Path root;
    private final Path database;
    private static final String NORMALIZER_CACHE_VERSION = "season-normalized-v21";

    private final Path stateFile;
    private final Path normalizationCacheFile;

    RecordsNextPreparationService(Path root, Path database) {
        this.root = root.toAbsolutePath().normalize();
        this.database = database.toAbsolutePath().normalize();
        this.stateFile = this.root.resolve("data/consolidation/recordsnext-consolidation.properties");
        this.normalizationCacheFile = this.root.resolve("data/consolidation/normalization-cache.properties");
    }

    List<String> prepare(ProcessingMode mode, List<String> selected,
                         RecordsNextPipeline.Listener listener) throws Exception {
        List<SeasonSource> seasons = loadConfigured(selected);
        if (seasons.isEmpty()) {
            throw new IllegalStateException("Nessuna stagione configurata da elaborare.");
        }
        List<SeasonSource> managed = seasons.stream()
            .filter(s -> "GESTITA".equals(s.type()))
            .sorted(Comparator.comparing(SeasonSource::id))
            .toList();
        if (managed.isEmpty()) {
            throw new IllegalStateException("Non esistono stagioni gestite da importare.");
        }
        SeasonSource current = managed.get(managed.size() - 1);

        List<SeasonSource> toImport;
        if (mode == ProcessingMode.CONSOLIDATED) {
            validateConsolidation(seasons, current.id());
            toImport = List.of(current);
            listener.phase("Aggiornamento della stagione attuale " + current.id(), 5);
        } else {
            toImport = managed;
            listener.phase("Importazione completa delle stagioni gestite", 5);
        }

        int index = 0;
        boolean imported = false;
        for (SeasonSource season : toImport) {
            validateManagedSource(season);
            int percent = 6 + (int) Math.round((index++ * 22.0) / Math.max(1, toImport.size()));
            if (sourceNeedsImport(season.id(), "FCM", season.fcm())) {
                listener.phase(season.id() + " — importazione FCM", percent);
                long started = System.nanoTime();
                RawSqliteImporter.main(new String[]{season.fcm(), "FCM", season.id(), database.toString()});
                listener.timing(season.id() + " — importazione FCM: " + elapsed(started));
                imported = true;
            } else {
                listener.phase(season.id() + " — FCM invariato", percent);
            }
            if (sourceNeedsImport(season.id(), "FCA", season.fca())) {
                listener.phase(season.id() + " — importazione FCA", Math.min(29, percent + 2));
                long started = System.nanoTime();
                RawSqliteImporter.main(new String[]{season.fca(), "FCA", season.id(), database.toString()});
                listener.timing(season.id() + " — importazione FCA: " + elapsed(started));
                imported = true;
            } else {
                listener.phase(season.id() + " — FCA invariato", Math.min(29, percent + 2));
            }
        }

        if (imported) {
            listener.phase("Aggiornamento configurazione e identità storiche", 30);
            long started = System.nanoTime();
            ConfigurationSchema.main(new String[]{database.toString(), current.id()});
            listener.timing("configurazione e identità: " + elapsed(started));
        } else {
            listener.phase("Sorgenti già importate; configurazione conservata", 30);
        }

        validateMappings(managed, current.id());

        listener.phase("Rigenerazione viste canoniche", 34);
        long canonicalStarted = System.nanoTime();
        CanonicalViews.main(new String[]{database.toString()});
        listener.timing("viste canoniche: " + elapsed(canonicalStarted));

        List<String> normalize = mode == ProcessingMode.CONSOLIDATED
            ? List.of(current.id())
            : managed.stream().map(SeasonSource::id).toList();
        Properties normalizationCache = loadNormalizationCache();
        int done = 0;
        for (String season : normalize) {
            int percent = 36 + (int) Math.round((done++ * 14.0) / Math.max(1, normalize.size()));
            SeasonSource source = managed.stream()
                .filter(item -> item.id().equals(season))
                .findFirst()
                .orElseThrow();
            String signature = normalizationSignature(source);
            if (normalizationCacheValid(season, signature, normalizationCache)) {
                listener.phase(season + " — normalizzazione invariata, riutilizzata", percent);
                continue;
            }
            if (!normalizationCache.containsKey("season." + season + ".signature")
                    && canBootstrapNormalizationCache(source)) {
                normalizationCache.setProperty("season." + season + ".signature", signature);
                normalizationCache.setProperty("season." + season + ".completedAt", java.time.Instant.now().toString());
                saveNormalizationCache(normalizationCache);
                listener.phase(season + " — cache normalizzazione inizializzata, dati riutilizzati", percent);
                continue;
            }
            listener.phase(season + " — normalizzazione", percent);
            long normalizeStarted = System.nanoTime();
            SeasonNormalizedBatchExporter.export(database, season, root);
            listener.timing(season + " — normalizzazione: " + elapsed(normalizeStarted));
            normalizationCache.setProperty("season." + season + ".signature", signature);
            normalizationCache.setProperty("season." + season + ".completedAt", java.time.Instant.now().toString());
            saveNormalizationCache(normalizationCache);
        }
        return normalize;
    }


    private Properties loadNormalizationCache() throws Exception {
        Properties cache = new Properties();
        if (Files.isRegularFile(normalizationCacheFile)) {
            try (InputStream in = Files.newInputStream(normalizationCacheFile)) {
                cache.load(in);
            }
        }
        return cache;
    }

    private void saveNormalizationCache(Properties cache) throws Exception {
        Files.createDirectories(normalizationCacheFile.getParent());
        try (OutputStream out = Files.newOutputStream(normalizationCacheFile)) {
            cache.store(out, "RecordsNext normalized season cache");
        }
    }

    private String normalizationSignature(SeasonSource season) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        updateDigest(digest, NORMALIZER_CACHE_VERSION);
        updateDigest(digest, season.id());
        updateDigest(digest, season.type());
        updateFileDigest(digest, season.fcm());
        updateFileDigest(digest, season.fca());
        updateDigest(digest, season.localSite());
        updateDigest(digest, season.onlineSite());
        updateDigest(digest, mappingStamp(season.id()));
        return toHex(digest.digest());
    }

    private static void updateFileDigest(MessageDigest digest, String value) throws Exception {
        updateDigest(digest, value == null ? "" : value);
        if (value == null || value.isBlank()) return;
        Path file = Path.of(value).toAbsolutePath().normalize();
        if (!Files.isRegularFile(file)) return;
        updateDigest(digest, Long.toString(Files.size(file)));
        updateDigest(digest, Files.getLastModifiedTime(file).toInstant().toString());
    }

    private static void updateDigest(MessageDigest digest, String value) {
        digest.update((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
        digest.update((byte) 0);
    }

    private boolean canBootstrapNormalizationCache(SeasonSource season) throws Exception {
        if (!Files.isRegularFile(stateFile) || !normalizationOutputsComplete(season.id())) {
            return false;
        }
        Properties old = new Properties();
        try (InputStream in = Files.newInputStream(stateFile)) {
            old.load(in);
        }
        String prefix = "season." + season.id() + ".";
        Properties now = snapshot(List.of(season));
        for (String suffix : List.of("type", "fcm", "fcm.size", "fcm.mtime",
                "fca", "fca.size", "fca.mtime", "site", "online", "mapping")) {
            String key = prefix + suffix;
            if (!old.getProperty(key, "").equals(now.getProperty(key, ""))) {
                return false;
            }
        }
        return true;
    }

    private boolean normalizationCacheValid(String season, String signature, Properties cache) throws Exception {
        if (!signature.equals(cache.getProperty("season." + season + ".signature", ""))) {
            return false;
        }
        return normalizationOutputsComplete(season);
    }

    private boolean normalizationOutputsComplete(String season) throws Exception {
        Path outputDir = root.resolve("data/reports").resolve(season);
        if (!Files.isDirectory(outputDir)) return false;
        long actual;
        try (var files = Files.list(outputDir)) {
            actual = files.filter(path -> {
                String name = path.getFileName().toString();
                return name.startsWith("season_normalized_") && name.endsWith(".json")
                    && !name.contains(".stage") && !name.contains(".final");
            }).count();
        }
        return actual >= expectedCompetitionCount(season) && actual > 0;
    }

    private long expectedCompetitionCount(String season) throws Exception {
        String sql = """
            SELECT COUNT(DISTINCT competition_name)
            FROM rn_team_match
            WHERE season_id=? AND competition_name IS NOT NULL AND TRIM(competition_name)<>''
            """;
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + database);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, season);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        }
    }

    void saveConsolidation(List<String> selected) throws Exception {
        List<SeasonSource> seasons = loadConfigured(selected);
        Properties p = snapshot(seasons);
        Files.createDirectories(stateFile.getParent());
        try (OutputStream out = Files.newOutputStream(stateFile)) {
            p.store(out, "RecordsNext consolidation state");
        }
    }

    boolean hasConsolidation() {
        return Files.isRegularFile(stateFile);
    }

    private void validateConsolidation(List<SeasonSource> seasons, String currentId) throws Exception {
        if (!Files.isRegularFile(stateFile)) {
            throw new IllegalStateException("Nessun consolidamento disponibile. Eseguire prima un'elaborazione completa.");
        }
        Properties old = new Properties();
        try (InputStream in = Files.newInputStream(stateFile)) { old.load(in); }
        Properties now = snapshot(seasons);
        String oldIds = old.getProperty("seasons", "");
        String nowIds = now.getProperty("seasons", "");
        if (!oldIds.equals(nowIds)) {
            throw invalid("è cambiato l'elenco delle stagioni");
        }
        for (SeasonSource season : seasons) {
            if (season.id().equals(currentId)) continue;
            String prefix = "season." + season.id() + ".";
            for (String suffix : List.of("type", "fcm", "fcm.size", "fcm.mtime", "fca", "fca.size", "fca.mtime", "site", "online", "mapping")) {
                String key = prefix + suffix;
                if (!old.getProperty(key, "").equals(now.getProperty(key, ""))) {
                    throw invalid("è cambiata la stagione storica " + season.id() + " (" + suffix + ")");
                }
            }
        }
    }

    private static IllegalStateException invalid(String reason) {
        return new IllegalStateException("Il consolidamento non è più valido: " + reason
            + ". Eseguire una nuova elaborazione completa.");
    }

    private Properties snapshot(List<SeasonSource> seasons) throws Exception {
        Properties p = new Properties();
        p.setProperty("seasons", String.join(",", seasons.stream().map(SeasonSource::id).sorted().toList()));
        for (SeasonSource s : seasons) {
            String k = "season." + s.id() + ".";
            p.setProperty(k + "type", s.type());
            fileSnapshot(p, k + "fcm", s.fcm());
            fileSnapshot(p, k + "fca", s.fca());
            p.setProperty(k + "site", s.localSite());
            p.setProperty(k + "online", s.onlineSite());
            p.setProperty(k + "mapping", mappingStamp(s.id()));
        }
        return p;
    }

    private static void fileSnapshot(Properties p, String key, String value) throws Exception {
        p.setProperty(key, value == null ? "" : value);
        if (value != null && !value.isBlank() && Files.isRegularFile(Path.of(value))) {
            Path file = Path.of(value);
            p.setProperty(key + ".size", Long.toString(Files.size(file)));
            p.setProperty(key + ".mtime", Long.toString(Files.getLastModifiedTime(file).toMillis()));
        } else {
            p.setProperty(key + ".size", "");
            p.setProperty(key + ".mtime", "");
        }
    }

    private String mappingStamp(String seasonId) throws Exception {
        // The consolidation signature must describe mapping decisions, not timestamps.
        // Only entities belonging to the latest FCM import of the season are relevant.
        String competitionSql = """
            SELECT s.source_competition_id,
                   s.normalized_name,
                   m.mapping_status,
                   COALESCE(m.competition_identity_id,0)
            FROM rn_competition_season s
            JOIN rn_competition_mapping m
              ON m.competition_season_id=s.competition_season_id
            JOIN rn_source_file sf ON sf.source_file_id=s.source_file_id
            WHERE s.season_id=?
              AND sf.source_type='FCM'
              AND sf.import_id=(
                  SELECT MAX(sf2.import_id)
                  FROM rn_source_file sf2
                  WHERE sf2.season_id=s.season_id AND sf2.source_type='FCM'
              )
            ORDER BY s.source_competition_id, s.normalized_name,
                     m.mapping_status, COALESCE(m.competition_identity_id,0)
            """;
        String teamSql = """
            SELECT s.source_team_id,
                   s.normalized_name,
                   COALESCE(s.source_division_id,-1),
                   COALESCE(s.source_team_number,-1),
                   m.mapping_status,
                   COALESCE(m.team_identity_id,0)
            FROM rn_team_season s
            JOIN rn_team_mapping m ON m.team_season_id=s.team_season_id
            JOIN rn_source_file sf ON sf.source_file_id=s.source_file_id
            WHERE s.season_id=?
              AND sf.source_type='FCM'
              AND sf.import_id=(
                  SELECT MAX(sf2.import_id)
                  FROM rn_source_file sf2
                  WHERE sf2.season_id=s.season_id AND sf2.source_type='FCM'
              )
            ORDER BY s.source_team_id, s.normalized_name,
                     COALESCE(s.source_division_id,-1), COALESCE(s.source_team_number,-1),
                     m.mapping_status, COALESCE(m.team_identity_id,0)
            """;

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + database)) {
            updateMappingDigest(c, competitionSql, seasonId, "C", digest);
            updateMappingDigest(c, teamSql, seasonId, "T", digest);
        }
        return toHex(digest.digest());
    }

    private static void updateMappingDigest(Connection connection, String sql,
                                            String seasonId, String prefix,
                                            MessageDigest digest) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, seasonId);
            try (ResultSet rs = ps.executeQuery()) {
                int columns = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    digest.update(prefix.getBytes(StandardCharsets.UTF_8));
                    digest.update((byte) 0);
                    for (int column = 1; column <= columns; column++) {
                        String value = rs.getString(column);
                        digest.update((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
                        digest.update((byte) 0);
                    }
                    digest.update((byte) '\n');
                }
            }
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder result = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            result.append(Character.forDigit((value >>> 4) & 0x0f, 16));
            result.append(Character.forDigit(value & 0x0f, 16));
        }
        return result.toString();
    }


    private boolean sourceNeedsImport(String seasonId, String sourceType, String configuredPath) throws Exception {
        Path file = Path.of(configuredPath).toAbsolutePath().normalize();
        String sql = """
            SELECT source_path,source_size_bytes,source_last_modified
            FROM rn_import
            WHERE season_id=? AND source_type=? AND status='COMPLETED'
            ORDER BY import_id DESC
            LIMIT 1
            """;
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + database);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, seasonId);
            ps.setString(2, sourceType);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return true;
                String previousPath = Path.of(rs.getString(1)).toAbsolutePath().normalize().toString();
                long previousSize = rs.getLong(2);
                String previousModified = rs.getString(3);
                return !previousPath.equalsIgnoreCase(file.toString())
                    || previousSize != Files.size(file)
                    || !previousModified.equals(Files.getLastModifiedTime(file).toInstant().toString());
            }
        }
    }
    private List<SeasonSource> loadConfigured(List<String> selected) throws Exception {
        if (!Files.isRegularFile(database)) {
            throw new IllegalStateException("Database RecordsNext non trovato: " + database);
        }
        String sql = """
            SELECT s.season_id,
                   COALESCE(c.management_type,'GESTITA'),
                   COALESCE(c.configured_fcm_path,''),
                   COALESCE(c.configured_fca_path,''),
                   COALESCE(c.local_site_path,''),
                   COALESCE(c.online_site_url,'')
            FROM rn_season s
            LEFT JOIN rn_season_configuration c ON c.season_id=s.season_id
            ORDER BY s.season_id
            """;
        List<SeasonSource> result = new ArrayList<>();
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + database);
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String id = rs.getString(1);
                if (selected.contains(id)) {
                    result.add(new SeasonSource(id, rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getString(5), rs.getString(6)));
                }
            }
        }
        return result;
    }

    private void validateMappings(List<SeasonSource> managed, String currentId) throws Exception {
        String sql = """
            SELECT
              (SELECT COUNT(*) FROM rn_competition_mapping m
               JOIN rn_competition_season s ON s.competition_season_id=m.competition_season_id
               JOIN rn_source_file sf ON sf.source_file_id=s.source_file_id
               WHERE s.season_id=? AND m.mapping_status='DA_CONFIGURARE'
                 AND sf.import_id=(SELECT MAX(sf2.import_id) FROM rn_source_file sf2 WHERE sf2.season_id=s.season_id AND sf2.source_type='FCM')) +
              (SELECT COUNT(*) FROM rn_team_mapping m
               JOIN rn_team_season s ON s.team_season_id=m.team_season_id
               JOIN rn_source_file sf ON sf.source_file_id=s.source_file_id
               WHERE s.season_id=? AND m.mapping_status='DA_CONFIGURARE'
                 AND sf.import_id=(SELECT MAX(sf2.import_id) FROM rn_source_file sf2 WHERE sf2.season_id=s.season_id AND sf2.source_type='FCM'))
            """;
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + database);
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (SeasonSource season : managed) {
                if (season.id().equals(currentId)) continue;
                ps.setString(1, season.id()); ps.setString(2, season.id());
                try (ResultSet rs = ps.executeQuery()) {
                    int pending = rs.next() ? rs.getInt(1) : 0;
                    if (pending > 0) {
                        throw new IllegalStateException(season.id() + ": restano " + pending
                            + " associazioni da configurare. Aprire Configurazione prima di elaborare.");
                    }
                }
            }
        }
    }

    private static void validateManagedSource(SeasonSource s) {
        if (s.fcm().isBlank() || !Files.isRegularFile(Path.of(s.fcm()))) {
            throw new IllegalStateException(s.id() + ": file FCM non trovato: " + s.fcm());
        }
        if (s.fca().isBlank() || !Files.isRegularFile(Path.of(s.fca()))) {
            throw new IllegalStateException(s.id() + ": file FCA non trovato: " + s.fca());
        }
    }
    private static String elapsed(long started) {
        double seconds = (System.nanoTime() - started) / 1_000_000_000.0;
        return String.format(java.util.Locale.ROOT, "%.3f s", seconds);
    }

}
```

### src\main\java\it\alterlega\recordsnext\CalendarSourceManager.java

```java
package it.alterlega.recordsnext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Risolve i file DataA-AAAA.js senza dipendere da ConfrontiStorici.
 *
 * <p>Priorita: cartella esterna configurata, poi data/calendars del progetto.
 * L'importazione effettiva resta affidata a ConfrontiStoriciCalendarImporter,
 * gia validato. Questa classe registra la provenienza per stagione.</p>
 */
public final class CalendarSourceManager {

    private static final String EXTERNAL_DIRECTORY_KEY = "dataa_external_directory";
    private static final Pattern SEASON_PATTERN =
        Pattern.compile("^(\\d{4})_(\\d{4})$");

    private CalendarSourceManager() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            usage();
            System.exit(2);
        }

        Path database = Path.of(args[0]).toAbsolutePath().normalize();
        if (!Files.isRegularFile(database)) {
            throw new IllegalArgumentException("Database SQLite non trovato: " + database);
        }

        String command = args[1].trim().toLowerCase(Locale.ROOT);
        Class.forName("org.sqlite.JDBC");

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + database)) {
            configure(connection);
            installSchema(connection);

            switch (command) {
                case "set-directory" -> setDirectory(connection, args);
                case "clear-directory" -> clearDirectory(connection, args);
                case "resolve" -> resolveCommand(connection, args);
                case "import" -> importCommand(connection, database, args);
                case "validate" -> validateCommand(connection, database, args);
                case "show" -> showCommand(connection, args);
                default -> {
                    usage();
                    System.exit(2);
                }
            }
        }
    }

    static void installSchema(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE IF NOT EXISTS rn_global_configuration (
                    config_key TEXT PRIMARY KEY,
                    config_value TEXT NOT NULL,
                    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS rn_calendar_source (
                    season_id TEXT PRIMARY KEY,
                    source_type TEXT NOT NULL
                        CHECK (source_type IN ('USER_DIRECTORY', 'BUNDLED')),
                    source_directory TEXT NOT NULL,
                    source_file TEXT NOT NULL,
                    source_sha256 TEXT NOT NULL,
                    imported_at TEXT NOT NULL,
                    FOREIGN KEY (season_id) REFERENCES rn_season(season_id)
                )
                """);
        }
    }

    private static void setDirectory(Connection connection, String[] args) throws Exception {
        requireArgCount(args, 3, "<db> set-directory <cartella-DataA>");
        Path directory = Path.of(args[2]).toAbsolutePath().normalize();
        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException("Cartella DataA non trovata: " + directory);
        }

        try (PreparedStatement statement = connection.prepareStatement("""
            INSERT INTO rn_global_configuration(config_key, config_value, updated_at)
            VALUES (?, ?, CURRENT_TIMESTAMP)
            ON CONFLICT(config_key) DO UPDATE SET
                config_value = excluded.config_value,
                updated_at = CURRENT_TIMESTAMP
            """)) {
            statement.setString(1, EXTERNAL_DIRECTORY_KEY);
            statement.setString(2, directory.toString());
            statement.executeUpdate();
        }

        System.out.println("Cartella DataA esterna configurata: " + directory);
    }

    private static void clearDirectory(Connection connection, String[] args) throws Exception {
        requireArgCount(args, 2, "<db> clear-directory");
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM rn_global_configuration WHERE config_key = ?")) {
            statement.setString(1, EXTERNAL_DIRECTORY_KEY);
            statement.executeUpdate();
        }
        System.out.println("Cartella DataA esterna rimossa. Verra usato il fallback distribuito.");
    }

    private static void resolveCommand(Connection connection, String[] args) throws Exception {
        requireArgCount(args, 4, "<db> resolve <stagione> <project-root>");
        ResolvedSource source = resolve(connection, args[2], Path.of(args[3]));
        printSource(source);
    }

    private static void importCommand(
            Connection connection,
            Path database,
            String[] args) throws Exception {

        requireArgCount(args, 4, "<db> import <stagione> <project-root>");
        String season = requireSeason(connection, args[2]);
        ResolvedSource source = resolve(connection, season, Path.of(args[3]));

        ConfrontiStoriciCalendarImporter.main(new String[] {
            database.toString(), "set-directory", source.directory().toString()
        });
        ConfrontiStoriciCalendarImporter.main(new String[] {
            database.toString(), "import", season
        });

        recordSource(connection, season, source);
        printSource(source);
    }

    private static void validateCommand(
            Connection connection,
            Path database,
            String[] args) throws Exception {

        requireArgCount(args, 4, "<db> validate <stagione> <project-root>");
        String season = requireSeason(connection, args[2]);
        ResolvedSource source = resolve(connection, season, Path.of(args[3]));

        ConfrontiStoriciCalendarImporter.main(new String[] {
            database.toString(), "set-directory", source.directory().toString()
        });
        ConfrontiStoriciCalendarImporter.main(new String[] {
            database.toString(), "validate", season
        });

        verifyRecordedSource(connection, season, source);
        printSource(source);
    }

    private static void showCommand(Connection connection, String[] args) throws Exception {
        requireArgCount(args, 3, "<db> show <stagione>");
        String season = requireSeason(connection, args[2]);

        try (PreparedStatement statement = connection.prepareStatement("""
            SELECT source_type, source_directory, source_file,
                   source_sha256, imported_at
            FROM rn_calendar_source
            WHERE season_id = ?
            """)) {
            statement.setString(1, season);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    System.out.println("Nessuna sorgente calendario registrata per " + season);
                    return;
                }
                System.out.println("Stagione : " + season);
                System.out.println("Origine  : " + result.getString("source_type"));
                System.out.println("Cartella : " + result.getString("source_directory"));
                System.out.println("File     : " + result.getString("source_file"));
                System.out.println("SHA-256  : " + result.getString("source_sha256"));
                System.out.println("Importato: " + result.getString("imported_at"));
            }
        }
    }

    private static ResolvedSource resolve(
            Connection connection,
            String seasonValue,
            Path projectRootValue) throws Exception {

        String season = requireSeason(connection, seasonValue);
        int startYear = startYear(season);
        String fileName = "DataA-" + startYear + ".js";

        Path external = readExternalDirectory(connection);
        if (external != null) {
            Path candidate = external.resolve(fileName).toAbsolutePath().normalize();
            if (Files.isRegularFile(candidate)) {
                return new ResolvedSource(
                    "USER_DIRECTORY", external, candidate, sha256(candidate)
                );
            }
        }

        Path projectRoot = projectRootValue.toAbsolutePath().normalize();
        Path bundledDirectory = projectRoot.resolve("data").resolve("calendars");
        Path bundled = bundledDirectory.resolve(fileName).normalize();
        if (Files.isRegularFile(bundled)) {
            return new ResolvedSource(
                "BUNDLED", bundledDirectory, bundled, sha256(bundled)
            );
        }

        StringBuilder message = new StringBuilder("DataA non trovato per ")
            .append(season).append(". Atteso: ").append(fileName);
        if (external != null) {
            message.append(" in ").append(external);
        }
        message.append(" oppure in ").append(bundledDirectory);
        throw new IllegalArgumentException(message.toString());
    }

    private static Path readExternalDirectory(Connection connection) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement("""
            SELECT config_value
            FROM rn_global_configuration
            WHERE config_key = ?
            """)) {
            statement.setString(1, EXTERNAL_DIRECTORY_KEY);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return null;
                }
                Path directory = Path.of(result.getString(1))
                    .toAbsolutePath().normalize();
                return Files.isDirectory(directory) ? directory : null;
            }
        }
    }

    private static void recordSource(
            Connection connection,
            String season,
            ResolvedSource source) throws Exception {

        try (PreparedStatement statement = connection.prepareStatement("""
            INSERT INTO rn_calendar_source (
                season_id, source_type, source_directory,
                source_file, source_sha256, imported_at
            ) VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT(season_id) DO UPDATE SET
                source_type = excluded.source_type,
                source_directory = excluded.source_directory,
                source_file = excluded.source_file,
                source_sha256 = excluded.source_sha256,
                imported_at = excluded.imported_at
            """)) {
            statement.setString(1, season);
            statement.setString(2, source.type());
            statement.setString(3, source.directory().toString());
            statement.setString(4, source.file().toString());
            statement.setString(5, source.sha256());
            statement.setString(6, Instant.now().toString());
            statement.executeUpdate();
        }
    }

    private static void verifyRecordedSource(
            Connection connection,
            String season,
            ResolvedSource source) throws Exception {

        try (PreparedStatement statement = connection.prepareStatement("""
            SELECT source_type, source_file, source_sha256
            FROM rn_calendar_source
            WHERE season_id = ?
            """)) {
            statement.setString(1, season);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalStateException(
                        "Sorgente calendario non registrata per " + season
                    );
                }
                if (!source.type().equals(result.getString("source_type"))
                        || !source.file().toString().equals(result.getString("source_file"))
                        || !source.sha256().equals(result.getString("source_sha256"))) {
                    throw new IllegalStateException(
                        "La sorgente calendario corrente differisce da quella importata per "
                            + season
                    );
                }
            }
        }
    }

    private static String requireSeason(Connection connection, String value) throws Exception {
        String season = value.trim();
        Matcher matcher = SEASON_PATTERN.matcher(season);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                "Formato stagione non valido, atteso AAAA_AAAA: " + season
            );
        }
        int start = Integer.parseInt(matcher.group(1));
        int end = Integer.parseInt(matcher.group(2));
        if (end != start + 1) {
            throw new IllegalArgumentException("Stagione non consecutiva: " + season);
        }

        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT COUNT(*) FROM rn_season WHERE season_id = ?")) {
            statement.setString(1, season);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                if (result.getInt(1) != 1) {
                    throw new IllegalArgumentException("Stagione non trovata: " + season);
                }
            }
        }
        return season;
    }

    private static int startYear(String season) {
        return Integer.parseInt(season.substring(0, 4));
    }

    private static String sha256(Path file) throws Exception {
        byte[] bytes = Files.readAllBytes(file);
        return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(bytes)
        );
    }

    private static void printSource(ResolvedSource source) {
        System.out.println("Origine  : " + source.type());
        System.out.println("Cartella : " + source.directory());
        System.out.println("File     : " + source.file());
        System.out.println("SHA-256  : " + source.sha256());
    }

    private static void configure(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("PRAGMA busy_timeout = 10000");
        }
    }

    private static void requireArgCount(String[] args, int expected, String usage) {
        if (args.length != expected) {
            throw new IllegalArgumentException("Uso: " + usage);
        }
    }

    private static void usage() {
        System.err.println("Comandi:");
        System.err.println("  <db> set-directory <cartella-DataA>");
        System.err.println("  <db> clear-directory");
        System.err.println("  <db> resolve <stagione> <project-root>");
        System.err.println("  <db> import <stagione> <project-root>");
        System.err.println("  <db> validate <stagione> <project-root>");
        System.err.println("  <db> show <stagione>");
    }

    private record ResolvedSource(
        String type,
        Path directory,
        Path file,
        String sha256
    ) {
    }
}
```

### src\main\java\it\alterlega\recordsnext\CanonicalSchemaProbe.java

```java
package it.alterlega.recordsnext;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.List;

public final class CanonicalSchemaProbe {

    private static final List<String> TABLES = List.of(
        "raw_2025_2026_fcm_competizione",
        "raw_2025_2026_fcm_girone",
        "raw_2025_2026_fcm_giornata",
        "raw_2025_2026_fcm_fantasquadra",
        "raw_2025_2026_fcm_incontro",
        "raw_2025_2026_fcm_formazione",
        "raw_2025_2026_fcm_tabellino",
        "raw_2025_2026_fca_giocatorea",
        "raw_2025_2026_fca_giocain",
        "raw_2025_2026_fca_punteggio"
    );

    private CanonicalSchemaProbe() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Uso: CanonicalSchemaProbe <recordsnext.db>");
            System.exit(2);
        }

        Path database = Path.of(args[0]).toAbsolutePath().normalize();

        Class.forName("org.sqlite.JDBC");

        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + database)) {

            for (String table : TABLES) {
                printTable(connection, table);
            }
        }
    }

    private static void printTable(
            Connection connection,
            String table) throws Exception {

        System.out.println();
        System.out.println("==================================================");
        System.out.println(table);
        System.out.println("==================================================");

        try (Statement statement = connection.createStatement();
             ResultSet columns = statement.executeQuery(
                 "PRAGMA table_info(\"" + table.replace("\"", "\"\"") + "\")"
             )) {

            System.out.println("COLONNE:");

            while (columns.next()) {
                System.out.printf(
                    "%3d  %-35s %s%n",
                    columns.getInt("cid"),
                    columns.getString("name"),
                    columns.getString("type")
                );
            }
        }

        String sql = "SELECT * FROM \""
            + table.replace("\"", "\"\"")
            + "\" LIMIT 1";

        try (Statement statement = connection.createStatement();
             ResultSet row = statement.executeQuery(sql)) {

            if (!row.next()) {
                System.out.println("TABELLA VUOTA");
                return;
            }

            ResultSetMetaData metadata = row.getMetaData();

            System.out.println();
            System.out.println("PRIMA RIGA:");

            for (int index = 1; index <= metadata.getColumnCount(); index++) {
                Object value = row.getObject(index);

                System.out.printf(
                    "%-35s = %s%n",
                    metadata.getColumnName(index),
                    value == null ? "<NULL>" : String.valueOf(value)
                );
            }
        }
    }
}
```

### src\main\java\it\alterlega\recordsnext\CanonicalViews.java

```java
package it.alterlega.recordsnext;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class CanonicalViews {

    private CanonicalViews() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println(
                "Uso: CanonicalViews <recordsnext.db>"
            );
            System.exit(2);
        }

        Path database = Path.of(args[0])
            .toAbsolutePath()
            .normalize();

        Class.forName("org.sqlite.JDBC");

        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + database)) {

            connection.setAutoCommit(false);

            try {
                createViews(connection);
                connection.commit();
                printAudit(connection);
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    private static void createViews(Connection connection)
            throws Exception {

        dropCanonicalViews(connection);
        createConfiguredEntityViews(connection);

        List<String> seasonEventViews =
            createSeasonEventViews(connection);

        createUnionViews(
            connection,
            seasonEventViews
        );
    }

    private static void dropCanonicalViews(
            Connection connection) throws Exception {

        List<String> generatedViews = new ArrayList<>();

        try (
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("""
                SELECT name
                FROM sqlite_master
                WHERE type = 'view'
                  AND (
                      name LIKE 'rn_event_%'
                      OR name LIKE 'rn_match_%'
                  )
                ORDER BY name
                """)
        ) {
            while (result.next()) {
                generatedViews.add(
                    result.getString("name")
                );
            }
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute(
                "DROP VIEW IF EXISTS rn_playoff_result"
            );

            statement.execute(
                "DROP VIEW IF EXISTS rn_team_match"
            );

            statement.execute(
                "DROP VIEW IF EXISTS rn_team_event"
            );

            statement.execute(
                "DROP VIEW IF EXISTS rn_match"
            );

            statement.execute(
                "DROP VIEW IF EXISTS rn_event"
            );

            for (String viewName : generatedViews) {
                statement.execute(
                    "DROP VIEW IF EXISTS "
                        + quoteIdentifier(viewName)
                );
            }

            statement.execute(
                "DROP VIEW IF EXISTS rn_configured_team"
            );

            statement.execute(
                "DROP VIEW IF EXISTS rn_configured_competition"
            );
        }
    }

    private static void createConfiguredEntityViews(
            Connection connection) throws Exception {

        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE VIEW rn_configured_competition AS
                SELECT
                    cs.competition_season_id,
                    cs.season_id,
                    cs.source_file_id,
                    cs.source_competition_id,
                    cs.source_name,
                    cs.normalized_name,
                    cm.competition_identity_id,
                    ci.canonical_name,
                    cm.mapping_status,
                    cm.mapping_method,
                    cm.notes
                FROM rn_competition_season cs
                JOIN rn_competition_mapping cm
                  ON cm.competition_season_id =
                     cs.competition_season_id
                LEFT JOIN rn_competition_identity ci
                  ON ci.competition_identity_id =
                     cm.competition_identity_id
                """);

            statement.execute("""
                CREATE VIEW rn_configured_team AS
                SELECT
                    ts.team_season_id,
                    ts.season_id,
                    ts.source_file_id,
                    ts.source_team_id,
                    ts.source_name,
                    ts.normalized_name,
                    ts.source_division_id,
                    ts.source_team_number,
                    tm.team_identity_id,
                    ti.canonical_name,
                    tm.mapping_status,
                    tm.mapping_method,
                    tm.notes
                FROM rn_team_season ts
                JOIN rn_team_mapping tm
                  ON tm.team_season_id =
                     ts.team_season_id
                LEFT JOIN rn_team_identity ti
                  ON ti.team_identity_id =
                     tm.team_identity_id
                """);
        }
    }

    private static List<String> createSeasonEventViews(
            Connection connection) throws Exception {

        List<FcmSource> sources = readFcmSources(connection);
        List<String> generatedViews = new ArrayList<>();

        for (FcmSource source : sources) {
            String incontroTable = rawTable(
                connection,
                source.importId(),
                "INCONTRO"
            );

            String gironeTable = rawTable(
                connection,
                source.importId(),
                "GIRONE"
            );

            String giornataTable = rawTable(
                connection,
                source.importId(),
                "GIORNATA"
            );

            String viewName =
                "rn_event_"
                    + normalizeIdentifier(
                        source.seasonId()
                    )
                    + "_"
                    + source.importId();

            createSeasonEventView(
                connection,
                source,
                viewName,
                incontroTable,
                gironeTable,
                giornataTable
            );

            generatedViews.add(viewName);
        }

        if (generatedViews.isEmpty()) {
            throw new IllegalStateException(
                "Nessuna sorgente FCM configurata."
            );
        }

        return generatedViews;
    }

    private static List<FcmSource> readFcmSources(
            Connection connection) throws Exception {

        List<FcmSource> sources = new ArrayList<>();

        String sql = """
            SELECT
                source_file_id,
                import_id,
                season_id
            FROM rn_source_file sf
            WHERE source_type = 'FCM'
              AND sf.import_id = (
                  SELECT MAX(sf2.import_id)
                  FROM rn_source_file sf2
                  WHERE sf2.season_id=sf.season_id
                    AND sf2.source_type='FCM'
              )
            ORDER BY season_id, import_id
            """;

        try (
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(sql)
        ) {
            while (result.next()) {
                sources.add(
                    new FcmSource(
                        result.getLong("source_file_id"),
                        result.getLong("import_id"),
                        result.getString("season_id")
                    )
                );
            }
        }

        return sources;
    }

    private static void createSeasonEventView(
            Connection connection,
            FcmSource source,
            String viewName,
            String incontroTable,
            String gironeTable,
            String giornataTable) throws Exception {

        String sql = """
            CREATE VIEW %s AS
            WITH rounds AS (
                SELECT
                    g.ID AS source_group_id,
                    i.IDGIORNATA AS source_round_id,
                    MIN(i.ID) AS first_event_id,
                    ROW_NUMBER() OVER (
                        PARTITION BY g.ID
                        ORDER BY MIN(i.ID)
                    ) AS competition_round
                FROM %s i
                JOIN %s g
                  ON g.ID = i.IDGIRONE
                JOIN rn_configured_competition cc
                  ON cc.source_file_id = %d
                 AND cc.source_competition_id =
                     g.IDCOMPETIZIONE
                 AND cc.mapping_status = 'ASSOCIATA'
                WHERE i.GIOCATO <> 0
                  AND i.IDCASA <> 0
                GROUP BY
                    g.ID,
                    i.IDGIORNATA
            )
            SELECT
                '%s' AS season_id,
                %d AS source_file_id,

                cc.competition_identity_id,
                cc.canonical_name AS competition_name,
                cc.source_competition_id,

                g.ID AS source_group_id,
                g.NOME AS source_group_name,

                i.ID AS source_event_id,

                r.competition_round,
                i.GIORNATADIA AS serie_a_round,
                i.IDGIORNATA AS source_round_id,
                gio."DESC" AS round_description,

                i.IDTIPO AS source_match_type_id,

                i.IDCASA AS home_source_team_id,
                home.team_identity_id
                    AS home_team_identity_id,
                COALESCE(
                    home.canonical_name,
                    home.source_name
                ) AS home_team_name,

                i.IDFUORI AS away_source_team_id,
                away.team_identity_id
                    AS away_team_identity_id,
                CASE
                    WHEN i.IDFUORI = 0 THEN NULL
                    ELSE COALESCE(
                        away.canonical_name,
                        away.source_name
                    )
                END AS away_team_name,

                i.PARZCASA AS home_partial_score,
                i.PARZFUORI AS away_partial_score,

                i.TOTCASA AS home_total_score,
                i.TOTFUORI AS away_total_score,

                i.GOLCASA AS home_goals,
                i.GOLFUORI AS away_goals,

                i.GIOCATO AS played,

                CASE
                    WHEN i.IDFUORI <> 0
                        THEN 'HEAD_TO_HEAD'

                    WHEN i.TOTCASA <> 0
                      OR i.PARZCASA <> 0
                        THEN 'SCORE_ONLY'

                    ELSE 'REST'
                END AS event_type

            FROM %s i

            JOIN %s g
              ON g.ID = i.IDGIRONE

            JOIN rn_configured_competition cc
              ON cc.source_file_id = %d
             AND cc.source_competition_id =
                 g.IDCOMPETIZIONE
             AND cc.mapping_status = 'ASSOCIATA'

            JOIN rn_configured_team home
              ON home.source_file_id = %d
             AND home.source_team_id = i.IDCASA
             AND home.mapping_status = 'ASSOCIATA'

            LEFT JOIN rn_configured_team away
              ON away.source_file_id = %d
             AND away.source_team_id = i.IDFUORI
             AND away.mapping_status = 'ASSOCIATA'

            LEFT JOIN %s gio
              ON gio.ID = i.IDGIORNATA

            JOIN rounds r
              ON r.source_group_id = g.ID
             AND r.source_round_id = i.IDGIORNATA

            WHERE i.GIOCATO <> 0
              AND i.IDCASA <> 0
              AND (
                  i.IDFUORI = 0
                  OR away.team_identity_id IS NOT NULL
              )
            """.formatted(
                quoteIdentifier(viewName),
                quoteIdentifier(incontroTable),
                quoteIdentifier(gironeTable),
                source.sourceFileId(),
                escapeSqlLiteral(source.seasonId()),
                source.sourceFileId(),
                quoteIdentifier(incontroTable),
                quoteIdentifier(gironeTable),
                source.sourceFileId(),
                source.sourceFileId(),
                source.sourceFileId(),
                quoteIdentifier(giornataTable)
            );

        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private static void createUnionViews(
            Connection connection,
            List<String> seasonEventViews) throws Exception {

        StringBuilder eventUnion = new StringBuilder();

        for (String viewName : seasonEventViews) {
            if (!eventUnion.isEmpty()) {
                eventUnion.append("\nUNION ALL\n");
            }

            eventUnion.append(
                "SELECT * FROM "
                    + quoteIdentifier(viewName)
            );
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE VIEW rn_event AS
                %s
                """.formatted(eventUnion));

            statement.execute("""
                CREATE VIEW rn_match AS
                SELECT
                    season_id,
                    source_file_id,
                    competition_identity_id,
                    competition_name,
                    source_competition_id,
                    source_group_id,
                    source_group_name,

                    source_event_id,
                    source_event_id AS source_match_id,

                    competition_round,
                    serie_a_round,
                    source_round_id,
                    round_description,
                    source_match_type_id,

                    home_source_team_id,
                    home_team_identity_id,
                    home_team_name,

                    away_source_team_id,
                    away_team_identity_id,
                    away_team_name,

                    home_partial_score,
                    away_partial_score,
                    home_total_score,
                    away_total_score,
                    home_goals,
                    away_goals,

                    played
                FROM rn_event
                WHERE event_type = 'HEAD_TO_HEAD'
                """);

            statement.execute("""
                CREATE VIEW rn_team_event AS

                SELECT
                    season_id,
                    source_file_id,

                    competition_identity_id,
                    competition_name,
                    source_competition_id,

                    source_group_id,
                    source_group_name,

                    source_event_id,

                    competition_round,
                    serie_a_round,
                    source_round_id,
                    round_description,
                    source_match_type_id,

                    event_type,

                    home_source_team_id
                        AS source_team_id,

                    home_team_identity_id
                        AS team_identity_id,

                    home_team_name
                        AS team_name,

                    CASE
                        WHEN event_type = 'HEAD_TO_HEAD'
                        THEN away_source_team_id
                        ELSE NULL
                    END AS opponent_source_team_id,

                    CASE
                        WHEN event_type = 'HEAD_TO_HEAD'
                        THEN away_team_identity_id
                        ELSE NULL
                    END AS opponent_team_identity_id,

                    CASE
                        WHEN event_type = 'HEAD_TO_HEAD'
                        THEN away_team_name
                        ELSE NULL
                    END AS opponent_name,

                    CASE
                        WHEN event_type = 'HEAD_TO_HEAD'
                        THEN 'HOME'
                        ELSE 'NEUTRAL'
                    END AS venue,

                    home_goals AS goals_for,

                    CASE
                        WHEN event_type = 'HEAD_TO_HEAD'
                        THEN away_goals
                        ELSE NULL
                    END AS goals_against,

                    home_partial_score AS partial_score_for,

                    CASE
                        WHEN event_type = 'HEAD_TO_HEAD'
                        THEN away_partial_score
                        ELSE NULL
                    END AS partial_score_against,

                    home_total_score AS score_for,

                    CASE
                        WHEN event_type = 'HEAD_TO_HEAD'
                        THEN away_total_score
                        ELSE NULL
                    END AS score_against,

                    CASE
                        WHEN event_type <> 'HEAD_TO_HEAD'
                            THEN NULL

                        WHEN home_goals > away_goals
                            THEN 'W'

                        WHEN home_goals = away_goals
                            THEN 'D'

                        ELSE 'L'
                    END AS result

                FROM rn_event

                UNION ALL

                SELECT
                    season_id,
                    source_file_id,

                    competition_identity_id,
                    competition_name,
                    source_competition_id,

                    source_group_id,
                    source_group_name,

                    source_event_id,

                    competition_round,
                    serie_a_round,
                    source_round_id,
                    round_description,
                    source_match_type_id,

                    event_type,

                    away_source_team_id
                        AS source_team_id,

                    away_team_identity_id
                        AS team_identity_id,

                    away_team_name
                        AS team_name,

                    home_source_team_id
                        AS opponent_source_team_id,

                    home_team_identity_id
                        AS opponent_team_identity_id,

                    home_team_name
                        AS opponent_name,

                    'AWAY' AS venue,

                    away_goals AS goals_for,
                    home_goals AS goals_against,

                    away_partial_score AS partial_score_for,
                    home_partial_score AS partial_score_against,

                    away_total_score AS score_for,
                    home_total_score AS score_against,

                    CASE
                        WHEN away_goals > home_goals
                            THEN 'W'

                        WHEN away_goals = home_goals
                            THEN 'D'

                        ELSE 'L'
                    END AS result

                FROM rn_event
                WHERE event_type = 'HEAD_TO_HEAD'
                """);

            statement.execute("""
                CREATE VIEW rn_team_match AS
                SELECT
                    season_id,
                    source_file_id,

                    competition_identity_id,
                    competition_name,
                    source_competition_id,

                    source_group_id,
                    source_group_name,

                    source_event_id,
                    source_event_id AS source_match_id,

                    competition_round,
                    serie_a_round,
                    source_round_id,
                    round_description,
                    source_match_type_id,

                    source_team_id,
                    team_identity_id,
                    team_name,

                    opponent_source_team_id,
                    opponent_team_identity_id,
                    opponent_name,

                    venue,

                    goals_for,
                    goals_against,

                    partial_score_for,
                    partial_score_against,

                    score_for,
                    score_against,

                    result
                FROM rn_team_event
                WHERE event_type = 'HEAD_TO_HEAD'
                """);

            statement.execute("""
                CREATE VIEW rn_playoff_result AS
                SELECT
                    current.season_id,
                    current.source_file_id,

                    current.competition_identity_id,
                    current.competition_name,
                    current.source_competition_id,

                    current.source_group_id,
                    current.source_group_name,

                    current.source_round_id,
                    current.round_description,
                    current.serie_a_round,
                    current.competition_round,

                    current.source_event_id,
                    current.home_source_team_id
                        AS source_team_id,
                    current.home_team_identity_id
                        AS team_identity_id,
                    current.home_team_name
                        AS team_name,

                    opponent.source_event_id
                        AS opponent_source_event_id,
                    opponent.home_source_team_id
                        AS opponent_source_team_id,
                    opponent.home_team_identity_id
                        AS opponent_team_identity_id,
                    opponent.home_team_name
                        AS opponent_name,

                    current.home_total_score
                        AS score_for,
                    opponent.home_total_score
                        AS score_against,

                    CASE
                        WHEN current.home_total_score >
                             opponent.home_total_score
                            THEN 'W'

                        WHEN current.home_total_score <
                             opponent.home_total_score
                            THEN 'L'

                        ELSE 'D'
                    END AS result

                FROM rn_event current

                JOIN rn_event opponent
                  ON opponent.season_id =
                     current.season_id
                 AND opponent.source_file_id =
                     current.source_file_id
                 AND opponent.competition_identity_id =
                     current.competition_identity_id
                 AND opponent.source_group_id =
                     current.source_group_id
                 AND opponent.source_round_id =
                     current.source_round_id
                 AND opponent.source_event_id <>
                     current.source_event_id
                 AND opponent.event_type = 'SCORE_ONLY'

                WHERE current.event_type = 'SCORE_ONLY'
                  AND UPPER(current.competition_name) =
                      'PLAY OFF - PLAY OUT'
                """);
        }
    }

    private static String rawTable(
            Connection connection,
            long importId,
            String sourceTableName) throws Exception {

        String sql = """
            SELECT raw_table_name
            FROM rn_table_catalog
            WHERE import_id = ?
              AND UPPER(source_table_name) = ?
            """;

        try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setLong(1, importId);
            statement.setString(
                2,
                sourceTableName.toUpperCase(Locale.ROOT)
            );

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalStateException(
                        "Tabella raw mancante: "
                            + sourceTableName
                            + ", import_id="
                            + importId
                    );
                }

                return result.getString("raw_table_name");
            }
        }
    }

    private static void printAudit(
            Connection connection) throws Exception {

        System.out.println(
            "Viste canoniche create"
        );

        System.out.println();

        printCount(
            connection,
            "Stagioni",
            """
            SELECT COUNT(DISTINCT season_id)
            FROM rn_event
            """
        );

        printCount(
            connection,
            "Competizioni con eventi",
            """
            SELECT COUNT(
                DISTINCT competition_identity_id
            )
            FROM rn_event
            """
        );

        printCount(
            connection,
            "Eventi totali",
            """
            SELECT COUNT(*)
            FROM rn_event
            """
        );

        printCount(
            connection,
            "Scontri diretti",
            """
            SELECT COUNT(*)
            FROM rn_event
            WHERE event_type = 'HEAD_TO_HEAD'
            """
        );

        printCount(
            connection,
            "Riposi",
            """
            SELECT COUNT(*)
            FROM rn_event
            WHERE event_type = 'REST'
            """
        );

        printCount(
            connection,
            "Punteggi puri",
            """
            SELECT COUNT(*)
            FROM rn_event
            WHERE event_type = 'SCORE_ONLY'
            """
        );

        printCount(
            connection,
            "Partecipazioni",
            """
            SELECT COUNT(*)
            FROM rn_team_event
            """
        );

        printCount(
            connection,
            "Righe squadra match",
            """
            SELECT COUNT(*)
            FROM rn_team_match
            """
        );

        printCount(
            connection,
            "Righe play off/out",
            """
            SELECT COUNT(*)
            FROM rn_playoff_result
            """
        );

        System.out.println();
        System.out.println("=== ESITI SCONTRI DIRETTI ===");

        printCount(
            connection,
            "Vittorie",
            """
            SELECT COUNT(*)
            FROM rn_team_match
            WHERE result = 'W'
            """
        );

        printCount(
            connection,
            "Pareggi",
            """
            SELECT COUNT(*)
            FROM rn_team_match
            WHERE result = 'D'
            """
        );

        printCount(
            connection,
            "Sconfitte",
            """
            SELECT COUNT(*)
            FROM rn_team_match
            WHERE result = 'L'
            """
        );

        System.out.println();
        System.out.println("=== ESITI PLAY OFF / PLAY OUT ===");

        printCount(
            connection,
            "Vinti",
            """
            SELECT COUNT(*)
            FROM rn_playoff_result
            WHERE result = 'W'
            """
        );

        printCount(
            connection,
            "Persi",
            """
            SELECT COUNT(*)
            FROM rn_playoff_result
            WHERE result = 'L'
            """
        );

        printCount(
            connection,
            "Pari",
            """
            SELECT COUNT(*)
            FROM rn_playoff_result
            WHERE result = 'D'
            """
        );

        System.out.println();
        System.out.println("=== EVENTI PER TIPO ===");

        try (
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("""
                SELECT
                    event_type,
                    COUNT(*) AS event_count
                FROM rn_event
                GROUP BY event_type
                ORDER BY event_type
                """)
        ) {
            while (result.next()) {
                System.out.printf(
                    Locale.ROOT,
                    "%-16s: %d%n",
                    result.getString("event_type"),
                    result.getLong("event_count")
                );
            }
        }
    }

    private static void printCount(
            Connection connection,
            String label,
            String sql) throws Exception {

        try (
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(sql)
        ) {
            result.next();

            System.out.printf(
                Locale.ROOT,
                "%-24s: %d%n",
                label,
                result.getLong(1)
            );
        }
    }

    private static String normalizeIdentifier(String value) {
        String normalized = value
            .trim()
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "_")
            .replaceAll("^_+|_+$", "");

        if (normalized.isBlank()) {
            throw new IllegalArgumentException(
                "Identificatore non valido: " + value
            );
        }

        return normalized;
    }

    private static String quoteIdentifier(String value) {
        return "\""
            + value.replace("\"", "\"\"")
            + "\"";
    }

    private static String escapeSqlLiteral(String value) {
        return value.replace("'", "''");
    }

    private record FcmSource(
        long sourceFileId,
        long importId,
        String seasonId
    ) {
    }
}
```

### src\main\java\it\alterlega\recordsnext\ConfigurationSchema.java

```java
package it.alterlega.recordsnext;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ConfigurationSchema {

    private ConfigurationSchema() {
    }

    /**
     * Crea lo schema RecordsNext vuoto per una nuova installazione.
     * Non richiede ancora una stagione-ancora e non importa dati.
     */
    public static void initializeEmpty(Path database) throws Exception {
        Path normalized = database.toAbsolutePath().normalize();
        Path parent = normalized.getParent();
        if (parent != null) {
            java.nio.file.Files.createDirectories(parent);
        }

        Class.forName("org.sqlite.JDBC");
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + normalized)) {
            connection.setAutoCommit(false);
            try {
                configureConnection(connection);
                createSchema(connection);
                connection.commit();
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println(
                "Uso: ConfigurationSchema <recordsnext.db> <stagione-ancora>"
            );
            System.exit(2);
        }

        Path database = Path.of(args[0]).toAbsolutePath().normalize();
        String anchorSeason = args[1].trim();

        if (anchorSeason.isBlank()) {
            throw new IllegalArgumentException(
                "La stagione-ancora non può essere vuota."
            );
        }

        Class.forName("org.sqlite.JDBC");

        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + database)) {

            connection.setAutoCommit(false);

            try {
                configureConnection(connection);
                createSchema(connection);
                importSeasonsAndSources(connection);
                setAnchorSeason(connection, anchorSeason);
                importSeasonEntities(connection);
                createAnchorIdentities(connection, anchorSeason);
                initializeHistoricalMappings(connection, anchorSeason);

                connection.commit();

                printSummary(connection, anchorSeason);
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    private static void configureConnection(Connection connection)
            throws Exception {

        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("PRAGMA busy_timeout = 10000");
        }
    }

    private static void createSchema(Connection connection)
            throws Exception {

        try (Statement statement = connection.createStatement()) {

            statement.execute("""
                CREATE TABLE IF NOT EXISTS rn_season (
                    season_id TEXT PRIMARY KEY,
                    display_name TEXT NOT NULL,
                    sort_order INTEGER,
                    is_anchor INTEGER NOT NULL DEFAULT 0
                        CHECK (is_anchor IN (0, 1)),
                    created_at TEXT NOT NULL,
                    updated_at TEXT NOT NULL
                )
                """);

            statement.execute("""
                CREATE UNIQUE INDEX IF NOT EXISTS
                    ux_rn_season_anchor
                ON rn_season(is_anchor)
                WHERE is_anchor = 1
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS rn_source_file (
                    source_file_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    import_id INTEGER NOT NULL UNIQUE,
                    season_id TEXT NOT NULL,
                    source_type TEXT NOT NULL
                        CHECK (source_type IN ('FCM', 'FCA')),
                    source_path TEXT NOT NULL,
                    source_file_name TEXT NOT NULL,
                    source_size_bytes INTEGER NOT NULL,
                    source_last_modified TEXT NOT NULL,
                    source_sha256 TEXT NOT NULL,
                    imported_at TEXT NOT NULL,
                    FOREIGN KEY (season_id)
                        REFERENCES rn_season(season_id)
                )
                """);

            statement.execute("""
                CREATE INDEX IF NOT EXISTS
                    ix_rn_source_file_season_type
                ON rn_source_file(season_id, source_type)
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS rn_competition_season (
                    competition_season_id INTEGER
                        PRIMARY KEY AUTOINCREMENT,
                    season_id TEXT NOT NULL,
                    source_file_id INTEGER NOT NULL,
                    source_competition_id INTEGER NOT NULL,
                    source_name TEXT NOT NULL,
                    normalized_name TEXT NOT NULL,
                    discovered_at TEXT NOT NULL,
                    UNIQUE (
                        source_file_id,
                        source_competition_id
                    ),
                    FOREIGN KEY (season_id)
                        REFERENCES rn_season(season_id),
                    FOREIGN KEY (source_file_id)
                        REFERENCES rn_source_file(source_file_id)
                )
                """);

            statement.execute("""
                CREATE INDEX IF NOT EXISTS
                    ix_rn_competition_season
                ON rn_competition_season(season_id)
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS rn_team_season (
                    team_season_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    season_id TEXT NOT NULL,
                    source_file_id INTEGER NOT NULL,
                    source_team_id INTEGER NOT NULL,
                    source_name TEXT NOT NULL,
                    normalized_name TEXT NOT NULL,
                    source_division_id INTEGER,
                    source_team_number INTEGER,
                    discovered_at TEXT NOT NULL,
                    UNIQUE (
                        source_file_id,
                        source_team_id
                    ),
                    FOREIGN KEY (season_id)
                        REFERENCES rn_season(season_id),
                    FOREIGN KEY (source_file_id)
                        REFERENCES rn_source_file(source_file_id)
                )
                """);

            statement.execute("""
                CREATE INDEX IF NOT EXISTS
                    ix_rn_team_season
                ON rn_team_season(season_id)
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS rn_competition_identity (
                    competition_identity_id INTEGER
                        PRIMARY KEY AUTOINCREMENT,
                    anchor_season_id TEXT NOT NULL,
                    anchor_competition_season_id INTEGER NOT NULL UNIQUE,
                    canonical_name TEXT NOT NULL,
                    created_at TEXT NOT NULL,
                    FOREIGN KEY (anchor_season_id)
                        REFERENCES rn_season(season_id),
                    FOREIGN KEY (anchor_competition_season_id)
                        REFERENCES rn_competition_season(
                            competition_season_id
                        )
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS rn_team_identity (
                    team_identity_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    anchor_season_id TEXT NOT NULL,
                    anchor_team_season_id INTEGER NOT NULL UNIQUE,
                    canonical_name TEXT NOT NULL,
                    created_at TEXT NOT NULL,
                    FOREIGN KEY (anchor_season_id)
                        REFERENCES rn_season(season_id),
                    FOREIGN KEY (anchor_team_season_id)
                        REFERENCES rn_team_season(team_season_id)
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS rn_competition_mapping (
                    competition_season_id INTEGER PRIMARY KEY,
                    competition_identity_id INTEGER,
                    mapping_status TEXT NOT NULL
                        CHECK (
                            mapping_status IN (
                                'DA_CONFIGURARE',
                                'ASSOCIATA',
                                'NON_ASSOCIATA',
                                'ESCLUSA'
                            )
                        ),
                    mapping_method TEXT,
                    notes TEXT,
                    updated_at TEXT NOT NULL,
                    CHECK (
                        (
                            mapping_status = 'ASSOCIATA'
                            AND competition_identity_id IS NOT NULL
                        )
                        OR
                        (
                            mapping_status <> 'ASSOCIATA'
                            AND competition_identity_id IS NULL
                        )
                    ),
                    FOREIGN KEY (competition_season_id)
                        REFERENCES rn_competition_season(
                            competition_season_id
                        ),
                    FOREIGN KEY (competition_identity_id)
                        REFERENCES rn_competition_identity(
                            competition_identity_id
                        )
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS rn_team_mapping (
                    team_season_id INTEGER PRIMARY KEY,
                    team_identity_id INTEGER,
                    mapping_status TEXT NOT NULL
                        CHECK (
                            mapping_status IN (
                                'DA_CONFIGURARE',
                                'ASSOCIATA',
                                'NON_ASSOCIATA',
                                'ESCLUSA'
                            )
                        ),
                    mapping_method TEXT,
                    notes TEXT,
                    updated_at TEXT NOT NULL,
                    CHECK (
                        (
                            mapping_status = 'ASSOCIATA'
                            AND team_identity_id IS NOT NULL
                        )
                        OR
                        (
                            mapping_status <> 'ASSOCIATA'
                            AND team_identity_id IS NULL
                        )
                    ),
                    FOREIGN KEY (team_season_id)
                        REFERENCES rn_team_season(team_season_id),
                    FOREIGN KEY (team_identity_id)
                        REFERENCES rn_team_identity(team_identity_id)
                )
                """);
        }
    }

    private static void importSeasonsAndSources(
            Connection connection) throws Exception {

        String now = Instant.now().toString();

        String seasonSql = """
            INSERT INTO rn_season (
                season_id,
                display_name,
                sort_order,
                is_anchor,
                created_at,
                updated_at
            )
            SELECT DISTINCT
                season_id,
                season_id,
                NULL,
                0,
                ?,
                ?
            FROM rn_import
            WHERE status = 'COMPLETED'
            ON CONFLICT(season_id) DO UPDATE SET
                updated_at = excluded.updated_at
            """;

        try (PreparedStatement statement =
                 connection.prepareStatement(seasonSql)) {

            statement.setString(1, now);
            statement.setString(2, now);
            statement.executeUpdate();
        }

        String sourceSql = """
            INSERT INTO rn_source_file (
                import_id,
                season_id,
                source_type,
                source_path,
                source_file_name,
                source_size_bytes,
                source_last_modified,
                source_sha256,
                imported_at
            )
            SELECT
                import_id,
                season_id,
                source_type,
                source_path,
                source_file_name,
                source_size_bytes,
                source_last_modified,
                source_sha256,
                COALESCE(completed_at, started_at)
            FROM rn_import i
            WHERE i.status = 'COMPLETED'
              AND i.import_id = (
                  SELECT MAX(i2.import_id)
                  FROM rn_import i2
                  WHERE i2.season_id=i.season_id
                    AND i2.source_type=i.source_type
                    AND i2.status='COMPLETED'
              )
            ON CONFLICT(import_id) DO UPDATE SET
                season_id = excluded.season_id,
                source_type = excluded.source_type,
                source_path = excluded.source_path,
                source_file_name = excluded.source_file_name,
                source_size_bytes = excluded.source_size_bytes,
                source_last_modified = excluded.source_last_modified,
                source_sha256 = excluded.source_sha256,
                imported_at = excluded.imported_at
            """;

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sourceSql);
        }
    }

    private static void setAnchorSeason(
            Connection connection,
            String anchorSeason) throws Exception {

        try (PreparedStatement check = connection.prepareStatement(
                "SELECT COUNT(*) FROM rn_season WHERE season_id = ?")) {

            check.setString(1, anchorSeason);

            try (ResultSet result = check.executeQuery()) {
                result.next();

                if (result.getInt(1) != 1) {
                    throw new IllegalArgumentException(
                        "Stagione-ancora non trovata: " + anchorSeason
                    );
                }
            }
        }

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                "UPDATE rn_season SET is_anchor = 0"
            );
        }

        try (PreparedStatement statement = connection.prepareStatement(
                """
                UPDATE rn_season
                SET is_anchor = 1,
                    updated_at = ?
                WHERE season_id = ?
                """)) {

            statement.setString(1, Instant.now().toString());
            statement.setString(2, anchorSeason);
            statement.executeUpdate();
        }
    }

    private static void importSeasonEntities(
            Connection connection) throws Exception {

        List<FcmSource> sources = readFcmSources(connection);

        for (FcmSource source : sources) {
            String competitionTable = findRawTable(
                connection,
                source.importId(),
                "COMPETIZIONE"
            );

            String teamTable = findRawTable(
                connection,
                source.importId(),
                "FANTASQUADRA"
            );

            importCompetitions(
                connection,
                source,
                competitionTable
            );

            importTeams(
                connection,
                source,
                teamTable
            );
        }
    }

    private static List<FcmSource> readFcmSources(
            Connection connection) throws Exception {

        List<FcmSource> sources = new ArrayList<>();

        String sql = """
            SELECT
                source_file_id,
                import_id,
                season_id
            FROM rn_source_file sf
            WHERE source_type = 'FCM'
              AND sf.import_id = (
                  SELECT MAX(sf2.import_id)
                  FROM rn_source_file sf2
                  WHERE sf2.season_id=sf.season_id
                    AND sf2.source_type='FCM'
              )
            ORDER BY season_id, import_id
            """;

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {

            while (result.next()) {
                sources.add(
                    new FcmSource(
                        result.getLong("source_file_id"),
                        result.getLong("import_id"),
                        result.getString("season_id")
                    )
                );
            }
        }

        return sources;
    }

    private static String findRawTable(
            Connection connection,
            long importId,
            String sourceTableName) throws Exception {

        String sql = """
            SELECT raw_table_name
            FROM rn_table_catalog
            WHERE import_id = ?
              AND UPPER(source_table_name) = ?
            """;

        try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setLong(1, importId);
            statement.setString(
                2,
                sourceTableName.toUpperCase(Locale.ROOT)
            );

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalStateException(
                        "Tabella raw non trovata per import "
                            + importId
                            + ": "
                            + sourceTableName
                    );
                }

                return result.getString("raw_table_name");
            }
        }
    }

    private static void importCompetitions(
            Connection connection,
            FcmSource source,
            String rawTable) throws Exception {

        String sql = """
            INSERT INTO rn_competition_season (
                season_id,
                source_file_id,
                source_competition_id,
                source_name,
                normalized_name,
                discovered_at
            )
            SELECT
                ?,
                ?,
                ID,
                NOME,
                LOWER(TRIM(NOME)),
                ?
            FROM %s
            WHERE ID IS NOT NULL
              AND NOME IS NOT NULL
              AND TRIM(NOME) <> ''
            ON CONFLICT(
                source_file_id,
                source_competition_id
            ) DO UPDATE SET
                source_name = excluded.source_name,
                normalized_name = excluded.normalized_name
            """.formatted(quoteIdentifier(rawTable));

        try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setString(1, source.seasonId());
            statement.setLong(2, source.sourceFileId());
            statement.setString(3, Instant.now().toString());
            statement.executeUpdate();
        }
    }

    private static void importTeams(
            Connection connection,
            FcmSource source,
            String rawTable) throws Exception {

        String sql = """
            INSERT INTO rn_team_season (
                season_id,
                source_file_id,
                source_team_id,
                source_name,
                normalized_name,
                source_division_id,
                source_team_number,
                discovered_at
            )
            SELECT
                ?,
                ?,
                ID,
                NOME,
                LOWER(TRIM(NOME)),
                IDDIVISIONE,
                NUMEROSQUADRA,
                ?
            FROM %s
            WHERE ID IS NOT NULL
              AND NOME IS NOT NULL
              AND TRIM(NOME) <> ''
            ON CONFLICT(
                source_file_id,
                source_team_id
            ) DO UPDATE SET
                source_name = excluded.source_name,
                normalized_name = excluded.normalized_name,
                source_division_id = excluded.source_division_id,
                source_team_number = excluded.source_team_number
            """.formatted(quoteIdentifier(rawTable));

        try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setString(1, source.seasonId());
            statement.setLong(2, source.sourceFileId());
            statement.setString(3, Instant.now().toString());
            statement.executeUpdate();
        }
    }

    private static void createAnchorIdentities(
            Connection connection,
            String anchorSeason) throws Exception {

        String now = Instant.now().toString();

        String competitionIdentitySql = """
            INSERT INTO rn_competition_identity (
                anchor_season_id,
                anchor_competition_season_id,
                canonical_name,
                created_at
            )
            SELECT
                season_id,
                competition_season_id,
                source_name,
                ?
            FROM rn_competition_season
            WHERE season_id = ?
            ON CONFLICT(anchor_competition_season_id)
            DO UPDATE SET
                canonical_name = excluded.canonical_name
            """;

        try (PreparedStatement statement =
                 connection.prepareStatement(competitionIdentitySql)) {

            statement.setString(1, now);
            statement.setString(2, anchorSeason);
            statement.executeUpdate();
        }

        String teamIdentitySql = """
            INSERT INTO rn_team_identity (
                anchor_season_id,
                anchor_team_season_id,
                canonical_name,
                created_at
            )
            SELECT
                season_id,
                team_season_id,
                source_name,
                ?
            FROM rn_team_season
            WHERE season_id = ?
            ON CONFLICT(anchor_team_season_id)
            DO UPDATE SET
                canonical_name = excluded.canonical_name
            """;

        try (PreparedStatement statement =
                 connection.prepareStatement(teamIdentitySql)) {

            statement.setString(1, now);
            statement.setString(2, anchorSeason);
            statement.executeUpdate();
        }

        String anchorCompetitionMappingSql = """
            INSERT INTO rn_competition_mapping (
                competition_season_id,
                competition_identity_id,
                mapping_status,
                mapping_method,
                notes,
                updated_at
            )
            SELECT
                cs.competition_season_id,
                ci.competition_identity_id,
                'ASSOCIATA',
                'ANCHOR_SELF',
                NULL,
                ?
            FROM rn_competition_season cs
            JOIN rn_competition_identity ci
              ON ci.anchor_competition_season_id =
                 cs.competition_season_id
            WHERE cs.season_id = ?
            ON CONFLICT(competition_season_id)
            DO UPDATE SET
                competition_identity_id =
                    excluded.competition_identity_id,
                mapping_status = 'ASSOCIATA',
                mapping_method = 'ANCHOR_SELF',
                notes = NULL,
                updated_at = excluded.updated_at
            """;

        try (PreparedStatement statement =
                 connection.prepareStatement(
                     anchorCompetitionMappingSql
                 )) {

            statement.setString(1, now);
            statement.setString(2, anchorSeason);
            statement.executeUpdate();
        }

        String anchorTeamMappingSql = """
            INSERT INTO rn_team_mapping (
                team_season_id,
                team_identity_id,
                mapping_status,
                mapping_method,
                notes,
                updated_at
            )
            SELECT
                ts.team_season_id,
                ti.team_identity_id,
                'ASSOCIATA',
                'ANCHOR_SELF',
                NULL,
                ?
            FROM rn_team_season ts
            JOIN rn_team_identity ti
              ON ti.anchor_team_season_id =
                 ts.team_season_id
            WHERE ts.season_id = ?
            ON CONFLICT(team_season_id)
            DO UPDATE SET
                team_identity_id = excluded.team_identity_id,
                mapping_status = 'ASSOCIATA',
                mapping_method = 'ANCHOR_SELF',
                notes = NULL,
                updated_at = excluded.updated_at
            """;

        try (PreparedStatement statement =
                 connection.prepareStatement(anchorTeamMappingSql)) {

            statement.setString(1, now);
            statement.setString(2, anchorSeason);
            statement.executeUpdate();
        }
    }

    private static void initializeHistoricalMappings(
            Connection connection,
            String anchorSeason) throws Exception {

        String now = Instant.now().toString();

        String competitionSql = """
            INSERT INTO rn_competition_mapping (
                competition_season_id,
                competition_identity_id,
                mapping_status,
                mapping_method,
                notes,
                updated_at
            )
            SELECT
                competition_season_id,
                NULL,
                'DA_CONFIGURARE',
                NULL,
                NULL,
                ?
            FROM rn_competition_season
            WHERE season_id <> ?
            ON CONFLICT(competition_season_id) DO NOTHING
            """;

        try (PreparedStatement statement =
                 connection.prepareStatement(competitionSql)) {

            statement.setString(1, now);
            statement.setString(2, anchorSeason);
            statement.executeUpdate();
        }

        String teamSql = """
            INSERT INTO rn_team_mapping (
                team_season_id,
                team_identity_id,
                mapping_status,
                mapping_method,
                notes,
                updated_at
            )
            SELECT
                team_season_id,
                NULL,
                'DA_CONFIGURARE',
                NULL,
                NULL,
                ?
            FROM rn_team_season
            WHERE season_id <> ?
            ON CONFLICT(team_season_id) DO NOTHING
            """;

        try (PreparedStatement statement =
                 connection.prepareStatement(teamSql)) {

            statement.setString(1, now);
            statement.setString(2, anchorSeason);
            statement.executeUpdate();
        }
    }

    private static void printSummary(
            Connection connection,
            String anchorSeason) throws Exception {

        System.out.println();
        System.out.println("Configurazione multistagione installata");
        System.out.println("Database       : "
            + connection.getMetaData().getURL());
        System.out.println("Stagione ancora: " + anchorSeason);
        System.out.println();

        printCount(
            connection,
            "Stagioni",
            "SELECT COUNT(*) FROM rn_season"
        );

        printCount(
            connection,
            "Sorgenti",
            "SELECT COUNT(*) FROM rn_source_file"
        );

        printCount(
            connection,
            "Competizioni locali",
            "SELECT COUNT(*) FROM rn_competition_season"
        );

        printCount(
            connection,
            "Squadre locali",
            "SELECT COUNT(*) FROM rn_team_season"
        );

        printCount(
            connection,
            "Identità competizioni",
            "SELECT COUNT(*) FROM rn_competition_identity"
        );

        printCount(
            connection,
            "Identità squadre",
            "SELECT COUNT(*) FROM rn_team_identity"
        );

        printCount(
            connection,
            "Competizioni da configurare",
            """
            SELECT COUNT(*)
            FROM rn_competition_mapping
            WHERE mapping_status = 'DA_CONFIGURARE'
            """
        );

        printCount(
            connection,
            "Squadre da configurare",
            """
            SELECT COUNT(*)
            FROM rn_team_mapping
            WHERE mapping_status = 'DA_CONFIGURARE'
            """
        );
    }

    private static void printCount(
            Connection connection,
            String label,
            String sql) throws Exception {

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {

            result.next();

            System.out.printf(
                Locale.ROOT,
                "%-28s: %d%n",
                label,
                result.getLong(1)
            );
        }
    }

    private static String quoteIdentifier(String value) {
        return "\""
            + value.replace("\"", "\"\"")
            + "\"";
    }

    private record FcmSource(
        long sourceFileId,
        long importId,
        String seasonId
    ) {
    }
}
```

### src\main\java\it\alterlega\recordsnext\ConfrontiStoriciCalendarImporter.java

```java
package it.alterlega.recordsnext;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Importa le date delle giornate dai DataA-AAAA.js della configurazione
 * di ConfrontiStorici. Non apre file FCM/FCA e non modifica gli export.
 */
public final class ConfrontiStoriciCalendarImporter {

    private static final String CONFIG_KEY = "confrontistorici_data_directory";
    private static final Pattern SEASON_PATTERN = Pattern.compile("^(\\d{4})_(\\d{4})$");
    private static final Pattern DATE_LINE_PATTERN = Pattern.compile(
        "(?m)^\\s*dataGiornata\\s*\\[\\s*(\\d+)\\s*]\\s*=\\s*[\"']([^\"']+)[\"']\\s*;?\\s*$"
    );
    private static final DateTimeFormatter DATA_A_DATE_FORMAT = new DateTimeFormatterBuilderSafe()
        .dateFormatter();
    private static final DateTimeFormatter DATA_A_DATE_TIME_FORMAT = new DateTimeFormatterBuilderSafe()
        .dateTimeFormatter();

    private ConfrontiStoriciCalendarImporter() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            usage();
            System.exit(2);
        }

        Path database = Path.of(args[0]).toAbsolutePath().normalize();
        String command = args[1].trim().toLowerCase(Locale.ROOT);

        Class.forName("org.sqlite.JDBC");
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + database)) {
            configure(connection);
            installSchema(connection);

            switch (command) {
                case "set-directory" -> setDirectory(connection, args);
                case "resolve" -> resolveCommand(connection, args);
                case "inspect" -> inspectCommand(connection, args);
                case "import" -> importCommand(connection, args);
                case "show" -> showCommand(connection, args);
                case "validate" -> validateCommand(connection, args);
                default -> {
                    usage();
                    System.exit(2);
                }
            }
        }
    }

    static void installSchema(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE IF NOT EXISTS rn_global_configuration (
                    config_key TEXT PRIMARY KEY,
                    config_value TEXT NOT NULL,
                    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS rn_matchday_date (
                    season_id TEXT NOT NULL,
                    serie_a_round INTEGER NOT NULL CHECK (serie_a_round > 0),
                    match_date TEXT NOT NULL,
                    source_path TEXT NOT NULL,
                    source_sha256 TEXT NOT NULL,
                    imported_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (season_id, serie_a_round),
                    FOREIGN KEY (season_id) REFERENCES rn_season(season_id)
                )
                """);

            statement.execute("""
                CREATE INDEX IF NOT EXISTS ix_rn_matchday_date_date
                ON rn_matchday_date(match_date)
                """);
        }

        addColumnIfMissing(connection, "rn_matchday_date", "match_time", "TEXT");
        addColumnIfMissing(connection, "rn_matchday_date", "match_datetime", "TEXT");

        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE INDEX IF NOT EXISTS ix_rn_matchday_date_datetime
                ON rn_matchday_date(match_datetime)
                """);
        }
    }

    private static void addColumnIfMissing(
            Connection connection,
            String table,
            String column,
            String definition) throws Exception {

        boolean present = false;
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (result.next()) {
                if (column.equalsIgnoreCase(result.getString("name"))) {
                    present = true;
                    break;
                }
            }
        }

        if (!present) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("ALTER TABLE " + table + " ADD COLUMN "
                    + column + " " + definition);
            }
        }
    }

    private static void setDirectory(Connection connection, String[] args) throws Exception {
        requireArgCount(args, 3, "<db> set-directory <directory-config-ConfrontiStorici>");
        Path directory = Path.of(args[2]).toAbsolutePath().normalize();
        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException("Directory non trovata: " + directory);
        }

        try (PreparedStatement statement = connection.prepareStatement("""
            INSERT INTO rn_global_configuration(config_key, config_value, updated_at)
            VALUES (?, ?, CURRENT_TIMESTAMP)
            ON CONFLICT(config_key) DO UPDATE SET
                config_value = excluded.config_value,
                updated_at = CURRENT_TIMESTAMP
            """)) {
            statement.setString(1, CONFIG_KEY);
            statement.setString(2, directory.toString());
            statement.executeUpdate();
        }

        System.out.println("Directory ConfrontiStorici configurata: " + directory);
    }

    private static void resolveCommand(Connection connection, String[] args) throws Exception {
        requireArgCount(args, 3, "<db> resolve <stagione>");
        String season = requireSeason(connection, args[2]);
        Path file = resolveFile(connection, season);
        System.out.println(file);
        System.out.println(Files.isRegularFile(file) ? "TROVATO" : "MANCANTE");
    }

    private static void inspectCommand(Connection connection, String[] args) throws Exception {
        requireArgCount(args, 3, "<db> inspect <stagione>");
        String season = requireSeason(connection, args[2]);
        Inspection inspection = inspect(resolveExistingFile(connection, season), season);
        printInspection(inspection);
    }

    private static void importCommand(Connection connection, String[] args) throws Exception {
        requireArgCount(args, 3, "<db> import <stagione>");
        String season = requireSeason(connection, args[2]);
        Inspection inspection = inspect(resolveExistingFile(connection, season), season);

        connection.setAutoCommit(false);
        try {
            try (PreparedStatement delete = connection.prepareStatement(
                    "DELETE FROM rn_matchday_date WHERE season_id = ?")) {
                delete.setString(1, season);
                delete.executeUpdate();
            }

            try (PreparedStatement insert = connection.prepareStatement("""
                INSERT INTO rn_matchday_date (
                    season_id, serie_a_round, match_date,
                    match_time, match_datetime,
                    source_path, source_sha256, imported_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """)) {
                for (MatchdayDate item : inspection.dates()) {
                    insert.setString(1, season);
                    insert.setInt(2, item.round());
                    insert.setString(3, item.date().toString());
                    insert.setString(4, item.time() == null ? null : item.time().toString());
                    insert.setString(5, item.dateTime() == null
                        ? null : item.dateTime().toString());
                    insert.setString(6, inspection.file().toString());
                    insert.setString(7, inspection.sha256());
                    insert.addBatch();
                }
                insert.executeBatch();
            }

            connection.commit();
        } catch (Exception exception) {
            connection.rollback();
            throw exception;
        } finally {
            connection.setAutoCommit(true);
        }

        System.out.printf(
            Locale.ROOT,
            "Importate %d giornate per %s da %s%n",
            inspection.dates().size(), season, inspection.file()
        );
    }

    private static void showCommand(Connection connection, String[] args) throws Exception {
        requireArgCount(args, 3, "<db> show <stagione>");
        String season = requireSeason(connection, args[2]);

        try (PreparedStatement statement = connection.prepareStatement("""
            SELECT serie_a_round, match_date, match_time,
                   match_datetime, source_path, source_sha256
            FROM rn_matchday_date
            WHERE season_id = ?
            ORDER BY serie_a_round
            """)) {
            statement.setString(1, season);
            try (ResultSet result = statement.executeQuery()) {
                int count = 0;
                while (result.next()) {
                    count++;
                    System.out.printf(
                        Locale.ROOT,
                        "%2d  %s%n",
                        result.getInt("serie_a_round"),
                        result.getString("match_datetime") != null
                            ? result.getString("match_datetime")
                            : result.getString("match_date")
                    );
                }
                if (count == 0) {
                    System.out.println("Nessuna data importata per " + season);
                }
            }
        }
    }

    private static void validateCommand(Connection connection, String[] args) throws Exception {
        requireArgCount(args, 3, "<db> validate <stagione>");
        String season = requireSeason(connection, args[2]);
        Path file = resolveExistingFile(connection, season);
        Inspection current = inspect(file, season);

        String sql = """
            SELECT COUNT(*) AS total,
                   COUNT(DISTINCT serie_a_round) AS distinct_rounds,
                   MIN(serie_a_round) AS first_round,
                   MAX(serie_a_round) AS last_round,
                   MIN(source_sha256) AS min_hash,
                   MAX(source_sha256) AS max_hash
            FROM rn_matchday_date
            WHERE season_id = ?
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, season);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                int total = result.getInt("total");
                int distinct = result.getInt("distinct_rounds");
                int first = result.getInt("first_round");
                int last = result.getInt("last_round");
                String minHash = result.getString("min_hash");
                String maxHash = result.getString("max_hash");

                List<String> errors = new ArrayList<>();
                if (total == 0) {
                    errors.add("nessuna data importata");
                }
                if (total != distinct) {
                    errors.add("giornate duplicate nel database");
                }
                if (total > 0 && (first != 1 || last != total)) {
                    errors.add("sequenza database non continua: " + first + ".." + last);
                }
                if (total != current.dates().size()) {
                    errors.add("numero date diverso dal file: db=" + total
                        + ", file=" + current.dates().size());
                }
                if (minHash != null && (!minHash.equals(maxHash)
                        || !minHash.equals(current.sha256()))) {
                    errors.add("file DataA.js cambiato dopo l'importazione");
                }

                if (!errors.isEmpty()) {
                    System.out.println(season + " NON VALIDA");
                    errors.forEach(error -> System.out.println("- " + error));
                    System.exit(1);
                }
                System.out.println(season + " VALIDA");
                System.out.println("Giornate: " + total);
                System.out.println("SHA-256 : " + current.sha256());
            }
        }
    }

    private static Inspection inspect(Path file, String season) throws Exception {
        byte[] bytes = Files.readAllBytes(file);
        String text = decode(bytes);
        Map<Integer, MatchdayDate> parsed = new TreeMap<>();
        Matcher matcher = DATE_LINE_PATTERN.matcher(text);

        while (matcher.find()) {
            int round = Integer.parseInt(matcher.group(1));
            String rawValue = matcher.group(2).trim();
            MatchdayDate parsedValue;
            try {
                LocalDateTime dateTime = LocalDateTime.parse(
                    rawValue,
                    DATA_A_DATE_TIME_FORMAT
                );
                parsedValue = new MatchdayDate(
                    round,
                    dateTime.toLocalDate(),
                    dateTime.toLocalTime(),
                    dateTime
                );
            } catch (DateTimeParseException dateTimeException) {
                try {
                    LocalDate date = LocalDate.parse(rawValue, DATA_A_DATE_FORMAT);
                    parsedValue = new MatchdayDate(round, date, null, null);
                } catch (DateTimeParseException dateException) {
                    throw new IllegalArgumentException(
                        "Data/ora non valida alla giornata " + round + ": " + rawValue,
                        dateTimeException
                    );
                }
            }
            MatchdayDate previous = parsed.putIfAbsent(round, parsedValue);
            if (previous != null) {
                throw new IllegalArgumentException("Giornata duplicata nel file: " + round);
            }
        }

        if (parsed.isEmpty()) {
            throw new IllegalArgumentException(
                "Nessuna assegnazione dataGiornata[n] trovata in " + file
            );
        }

        int expected = 1;
        for (int round : parsed.keySet()) {
            if (round != expected) {
                throw new IllegalArgumentException(
                    "Sequenza giornate non continua: attesa " + expected + ", trovata " + round
                );
            }
            expected++;
        }

        SeasonYears years = parseSeason(season);
        List<MatchdayDate> dates = parsed.values().stream()
            .sorted(Comparator.comparingInt(MatchdayDate::round))
            .toList();

        for (MatchdayDate item : dates) {
            int year = item.date().getYear();
            if (year != years.startYear() && year != years.endYear()) {
                throw new IllegalArgumentException(
                    "Data fuori stagione alla giornata " + item.round() + ": " + item.date()
                );
            }
        }

        return new Inspection(file, sha256(bytes), dates);
    }

    private static void printInspection(Inspection inspection) {
        MatchdayDate first = inspection.dates().getFirst();
        MatchdayDate last = inspection.dates().getLast();
        System.out.println("File     : " + inspection.file());
        System.out.println("Giornate : " + inspection.dates().size());
        System.out.println("Prima    : " + first.round() + " -> " + first.displayValue());
        System.out.println("Ultima   : " + last.round() + " -> " + last.displayValue());
        System.out.println("SHA-256  : " + inspection.sha256());
    }

    private static Path resolveExistingFile(Connection connection, String season)
            throws Exception {
        Path file = resolveFile(connection, season);
        if (!Files.isRegularFile(file)) {
            throw new IllegalArgumentException("DataA non trovato: " + file);
        }
        return file;
    }

    private static Path resolveFile(Connection connection, String season) throws Exception {
        SeasonYears years = parseSeason(season);
        Path directory = configuredDirectory(connection);
        return directory.resolve("DataA-" + years.startYear() + ".js")
            .toAbsolutePath().normalize();
    }

    private static Path configuredDirectory(Connection connection) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement("""
            SELECT config_value
            FROM rn_global_configuration
            WHERE config_key = ?
            """)) {
            statement.setString(1, CONFIG_KEY);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalStateException(
                        "Directory ConfrontiStorici non configurata. "
                            + "Usare set-directory."
                    );
                }
                Path directory = Path.of(result.getString(1))
                    .toAbsolutePath().normalize();
                if (!Files.isDirectory(directory)) {
                    throw new IllegalStateException(
                        "Directory ConfrontiStorici non disponibile: " + directory
                    );
                }
                return directory;
            }
        }
    }

    private static String requireSeason(Connection connection, String value) throws Exception {
        String season = value.trim();
        parseSeason(season);
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT COUNT(*) FROM rn_season WHERE season_id = ?")) {
            statement.setString(1, season);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                if (result.getInt(1) != 1) {
                    throw new IllegalArgumentException("Stagione non trovata: " + season);
                }
            }
        }
        return season;
    }

    private static SeasonYears parseSeason(String season) {
        Matcher matcher = SEASON_PATTERN.matcher(season);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                "Formato stagione non valido, atteso AAAA_AAAA: " + season
            );
        }
        int start = Integer.parseInt(matcher.group(1));
        int end = Integer.parseInt(matcher.group(2));
        if (end != start + 1) {
            throw new IllegalArgumentException("Stagione non consecutiva: " + season);
        }
        return new SeasonYears(start, end);
    }

    private static String decode(byte[] bytes) throws IOException {
        try {
            return StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(java.nio.ByteBuffer.wrap(bytes))
                .toString();
        } catch (java.nio.charset.CharacterCodingException exception) {
            return Charset.forName("windows-1252").decode(
                java.nio.ByteBuffer.wrap(bytes)
            ).toString();
        }
    }

    private static String sha256(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }

    private static void configure(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("PRAGMA busy_timeout = 10000");
        }
    }

    private static void requireArgCount(String[] args, int expected, String usage) {
        if (args.length != expected) {
            throw new IllegalArgumentException("Uso: " + usage);
        }
    }

    private static void usage() {
        System.err.println("Comandi:");
        System.err.println("  <db> set-directory <directory-config-ConfrontiStorici>");
        System.err.println("  <db> resolve <stagione>");
        System.err.println("  <db> inspect <stagione>");
        System.err.println("  <db> import <stagione>");
        System.err.println("  <db> show <stagione>");
        System.err.println("  <db> validate <stagione>");
    }

    private record MatchdayDate(
        int round,
        LocalDate date,
        LocalTime time,
        LocalDateTime dateTime
    ) {
        String displayValue() {
            return dateTime == null ? date.toString() : dateTime.toString();
        }
    }

    private record Inspection(Path file, String sha256, List<MatchdayDate> dates) {
    }

    private record SeasonYears(int startYear, int endYear) {
    }

    /** Isola la costruzione dei formatter per i DataA.js storici. */
    private static final class DateTimeFormatterBuilderSafe {
        DateTimeFormatter dateFormatter() {
            return new java.time.format.DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("MMMM d uuuu")
                .toFormatter(Locale.ENGLISH);
        }

        DateTimeFormatter dateTimeFormatter() {
            return new java.time.format.DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("MMMM d uuuu H:mm")
                .toFormatter(Locale.ENGLISH);
        }
    }
}
```

### src\main\java\it\alterlega\recordsnext\DatabaseInspector.java

```java
package it.alterlega.recordsnext;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class DatabaseInspector {

    private DatabaseInspector() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println(
                "Uso: DatabaseInspector <file.fcm|file.fca> <output.json>"
            );
            System.exit(2);
        }

        Path database = Path.of(args[0]).toAbsolutePath().normalize();
        Path output = Path.of(args[1]).toAbsolutePath().normalize();

        if (!Files.isRegularFile(database)) {
            throw new IllegalArgumentException(
                "Database non trovato: " + database
            );
        }

        Path outputParent = output.getParent();

        if (outputParent != null) {
            Files.createDirectories(outputParent);
        }

        Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");

        long totalStarted = System.nanoTime();
        String jdbcUrl = "jdbc:ucanaccess://" + database;

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("schemaVersion", 1);
        report.put("generatedAt", Instant.now().toString());
        report.put("source", inspectSource(database));

        try (Connection connection = DriverManager.getConnection(jdbcUrl)) {
            long openedAt = System.nanoTime();

            connection.setReadOnly(true);

            DatabaseMetaData metadata = connection.getMetaData();

            Map<String, Object> driver = new LinkedHashMap<>();
            driver.put("name", metadata.getDriverName());
            driver.put("version", metadata.getDriverVersion());
            driver.put("jdbcMajorVersion", metadata.getJDBCMajorVersion());
            driver.put("jdbcMinorVersion", metadata.getJDBCMinorVersion());
            driver.put("databaseProductName", metadata.getDatabaseProductName());
            driver.put(
                "databaseProductVersion",
                metadata.getDatabaseProductVersion()
            );
            report.put("driver", driver);

            List<String> tableNames = readTableNames(metadata);
            List<Map<String, Object>> tables = new ArrayList<>();

            long totalRows = 0;
            long totalColumns = 0;

            for (String tableName : tableNames) {
                Map<String, Object> table = inspectTable(
                    connection,
                    metadata,
                    tableName
                );

                totalRows += ((Number) table.get("rowCount")).longValue();
                totalColumns += ((Number) table.get("columnCount")).longValue();

                tables.add(table);
            }

            long finishedAt = System.nanoTime();

            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("tableCount", tables.size());
            summary.put("columnCount", totalColumns);
            summary.put("rowCount", totalRows);
            report.put("summary", summary);
            report.put("tables", tables);

            Map<String, Object> timings = new LinkedHashMap<>();
            timings.put(
                "openMilliseconds",
                nanosToMilliseconds(openedAt - totalStarted)
            );
            timings.put(
                "inspectionMilliseconds",
                nanosToMilliseconds(finishedAt - openedAt)
            );
            timings.put(
                "totalMilliseconds",
                nanosToMilliseconds(finishedAt - totalStarted)
            );
            report.put("timings", timings);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(
                output,
                StandardCharsets.UTF_8)) {

            writeJson(report, writer, 0);
            writer.write(System.lineSeparator());
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> summary =
            (Map<String, Object>) report.get("summary");

        @SuppressWarnings("unchecked")
        Map<String, Object> timings =
            (Map<String, Object>) report.get("timings");

        System.out.println();
        System.out.println("Inventario completato");
        System.out.println("Database : " + database);
        System.out.println("Output   : " + output);
        System.out.println("Tabelle  : " + summary.get("tableCount"));
        System.out.println("Colonne  : " + summary.get("columnCount"));
        System.out.println("Righe    : " + summary.get("rowCount"));
        System.out.printf(
            Locale.ROOT,
            "Apertura : %.3f s%n",
            ((Number) timings.get("openMilliseconds")).doubleValue() / 1000.0
        );
        System.out.printf(
            Locale.ROOT,
            "Ispezione: %.3f s%n",
            ((Number) timings.get("inspectionMilliseconds")).doubleValue()
                / 1000.0
        );
        System.out.printf(
            Locale.ROOT,
            "Totale   : %.3f s%n",
            ((Number) timings.get("totalMilliseconds")).doubleValue() / 1000.0
        );
    }

    private static Map<String, Object> inspectSource(Path database)
            throws Exception {

        Map<String, Object> source = new LinkedHashMap<>();

        source.put("path", database.toString());
        source.put("fileName", database.getFileName().toString());
        source.put("sourceType", detectSourceType(database));
        source.put("sizeBytes", Files.size(database));

        FileTime modified = Files.getLastModifiedTime(database);
        source.put("lastModified", modified.toInstant().toString());
        source.put("sha256", sha256(database));

        return source;
    }

    private static String detectSourceType(Path database) {
        String name = database.getFileName()
            .toString()
            .toLowerCase(Locale.ROOT);

        if (name.endsWith(".fcm")) {
            return "FCM";
        }

        if (name.endsWith(".fca")) {
            return "FCA";
        }

        return "UNKNOWN";
    }

    private static List<String> readTableNames(DatabaseMetaData metadata)
            throws Exception {

        List<String> tables = new ArrayList<>();

        try (ResultSet rs = metadata.getTables(
                null,
                null,
                "%",
                new String[]{"TABLE"})) {

            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");

                if (tableName != null && !tableName.isBlank()) {
                    tables.add(tableName);
                }
            }
        }

        tables.sort(String.CASE_INSENSITIVE_ORDER);
        return tables;
    }

    private static Map<String, Object> inspectTable(
            Connection connection,
            DatabaseMetaData metadata,
            String tableName) throws Exception {

        Map<String, Object> table = new LinkedHashMap<>();

        long rowCount = countRows(connection, tableName);
        List<Map<String, Object>> columns = readColumns(metadata, tableName);
        List<Map<String, Object>> primaryKeys =
            readPrimaryKeys(metadata, tableName);
        List<Map<String, Object>> indexes = readIndexes(metadata, tableName);

        table.put("name", tableName);
        table.put("rowCount", rowCount);
        table.put("columnCount", columns.size());
        table.put("columns", columns);
        table.put("primaryKeys", primaryKeys);
        table.put("indexes", indexes);

        return table;
    }

    private static long countRows(
            Connection connection,
            String tableName) throws Exception {

        String escapedName = tableName.replace("]", "]]");
        String sql = "SELECT COUNT(*) FROM [" + escapedName + "]";

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            rs.next();
            return rs.getLong(1);
        }
    }

    private static List<Map<String, Object>> readColumns(
            DatabaseMetaData metadata,
            String tableName) throws Exception {

        List<Map<String, Object>> columns = new ArrayList<>();

        try (ResultSet rs = metadata.getColumns(
                null,
                null,
                tableName,
                "%")) {

            while (rs.next()) {
                Map<String, Object> column = new LinkedHashMap<>();

                int nullableCode = rs.getInt("NULLABLE");

                column.put("name", rs.getString("COLUMN_NAME"));
                column.put("ordinalPosition", rs.getInt("ORDINAL_POSITION"));
                column.put("jdbcType", rs.getInt("DATA_TYPE"));
                column.put("typeName", rs.getString("TYPE_NAME"));
                column.put("columnSize", rs.getInt("COLUMN_SIZE"));
                column.put("decimalDigits", nullableInteger(
                    rs,
                    "DECIMAL_DIGITS"
                ));
                column.put("numericPrecisionRadix", nullableInteger(
                    rs,
                    "NUM_PREC_RADIX"
                ));
                column.put("nullableCode", nullableCode);
                column.put(
                    "nullable",
                    nullableCode == DatabaseMetaData.columnNullable
                );
                column.put("defaultValue", rs.getString("COLUMN_DEF"));
                column.put("remarks", rs.getString("REMARKS"));
                column.put(
                    "autoIncrement",
                    safeGetString(rs, "IS_AUTOINCREMENT")
                );
                column.put(
                    "generatedColumn",
                    safeGetString(rs, "IS_GENERATEDCOLUMN")
                );

                columns.add(column);
            }
        }

        columns.sort(Comparator.comparingInt(
            item -> ((Number) item.get("ordinalPosition")).intValue()
        ));

        return columns;
    }

    private static List<Map<String, Object>> readPrimaryKeys(
            DatabaseMetaData metadata,
            String tableName) throws Exception {

        List<Map<String, Object>> primaryKeys = new ArrayList<>();

        try (ResultSet rs = metadata.getPrimaryKeys(
                null,
                null,
                tableName)) {

            while (rs.next()) {
                Map<String, Object> key = new LinkedHashMap<>();

                key.put("name", rs.getString("PK_NAME"));
                key.put("columnName", rs.getString("COLUMN_NAME"));
                key.put("keySequence", rs.getInt("KEY_SEQ"));

                primaryKeys.add(key);
            }
        }

        primaryKeys.sort(Comparator.comparingInt(
            item -> ((Number) item.get("keySequence")).intValue()
        ));

        return primaryKeys;
    }

    private static List<Map<String, Object>> readIndexes(
            DatabaseMetaData metadata,
            String tableName) throws Exception {

        List<Map<String, Object>> indexes = new ArrayList<>();

        try (ResultSet rs = metadata.getIndexInfo(
                null,
                null,
                tableName,
                false,
                false)) {

            while (rs.next()) {
                String indexName = rs.getString("INDEX_NAME");

                if (indexName == null || indexName.isBlank()) {
                    continue;
                }

                Map<String, Object> index = new LinkedHashMap<>();

                index.put("name", indexName);
                index.put("unique", !rs.getBoolean("NON_UNIQUE"));
                index.put("type", rs.getShort("TYPE"));
                index.put(
                    "ordinalPosition",
                    rs.getShort("ORDINAL_POSITION")
                );
                index.put("columnName", rs.getString("COLUMN_NAME"));
                index.put("sortDirection", rs.getString("ASC_OR_DESC"));
                index.put("filterCondition", rs.getString("FILTER_CONDITION"));

                indexes.add(index);
            }
        }

indexes.sort(
    Comparator
        .comparing(
            (Map<String, Object> item) ->
                String.valueOf(item.get("name")),
            String.CASE_INSENSITIVE_ORDER
        )
        .thenComparingInt(
            item ->
                ((Number) item.get("ordinalPosition")).intValue()
        )
);

        return indexes;
    }

    private static Integer nullableInteger(
            ResultSet rs,
            String columnName) throws Exception {

        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }

    private static String safeGetString(
            ResultSet rs,
            String columnName) {

        try {
            return rs.getString(columnName);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static double nanosToMilliseconds(long nanos) {
        return Math.round((nanos / 1_000_000.0) * 1000.0) / 1000.0;
    }

    private static String sha256(Path path) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        try (var input = Files.newInputStream(path)) {
            byte[] buffer = new byte[1024 * 1024];
            int read;

            while ((read = input.read(buffer)) >= 0) {
                digest.update(buffer, 0, read);
            }
        }

        return HexFormat.of().formatHex(digest.digest());
    }

    private static void writeJson(
            Object value,
            Writer writer,
            int indent) throws IOException {

        if (value == null) {
            writer.write("null");
            return;
        }

        if (value instanceof String text) {
            writeJsonString(text, writer);
            return;
        }

        if (value instanceof Number || value instanceof Boolean) {
            writer.write(String.valueOf(value));
            return;
        }

        if (value instanceof Map<?, ?> map) {
            writeJsonMap(map, writer, indent);
            return;
        }

        if (value instanceof Iterable<?> iterable) {
            writeJsonArray(iterable, writer, indent);
            return;
        }

        writeJsonString(String.valueOf(value), writer);
    }

    private static void writeJsonMap(
            Map<?, ?> map,
            Writer writer,
            int indent) throws IOException {

        writer.write("{");

        if (!map.isEmpty()) {
            writer.write(System.lineSeparator());

            int index = 0;

            for (Map.Entry<?, ?> entry : map.entrySet()) {
                writeIndent(writer, indent + 1);
                writeJsonString(String.valueOf(entry.getKey()), writer);
                writer.write(": ");
                writeJson(entry.getValue(), writer, indent + 1);

                if (++index < map.size()) {
                    writer.write(",");
                }

                writer.write(System.lineSeparator());
            }

            writeIndent(writer, indent);
        }

        writer.write("}");
    }

    private static void writeJsonArray(
            Iterable<?> iterable,
            Writer writer,
            int indent) throws IOException {

        List<Object> values = new ArrayList<>();

        for (Object value : iterable) {
            values.add(value);
        }

        writer.write("[");

        if (!values.isEmpty()) {
            writer.write(System.lineSeparator());

            for (int index = 0; index < values.size(); index++) {
                writeIndent(writer, indent + 1);
                writeJson(values.get(index), writer, indent + 1);

                if (index + 1 < values.size()) {
                    writer.write(",");
                }

                writer.write(System.lineSeparator());
            }

            writeIndent(writer, indent);
        }

        writer.write("]");
    }

    private static void writeJsonString(
            String text,
            Writer writer) throws IOException {

        writer.write("\"");

        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);

            switch (character) {
                case '"' -> writer.write("\\\"");
                case '\\' -> writer.write("\\\\");
                case '\b' -> writer.write("\\b");
                case '\f' -> writer.write("\\f");
                case '\n' -> writer.write("\\n");
                case '\r' -> writer.write("\\r");
                case '\t' -> writer.write("\\t");
                default -> {
                    if (character < 0x20) {
                        writer.write(
                            String.format(
                                Locale.ROOT,
                                "\\u%04x",
                                (int) character
                            )
                        );
                    } else {
                        writer.write(character);
                    }
                }
            }
        }

        writer.write("\"");
    }

    private static void writeIndent(
            Writer writer,
            int indent) throws IOException {

        writer.write("  ".repeat(indent));
    }
}
```

### src\main\java\it\alterlega\recordsnext\gui\FcmSeasonDetector.java

```java
package it.alterlega.recordsnext.gui;

import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import java.util.regex.*;

final class FcmSeasonDetector {
    record Detection(String seasonId, int seasonNumber, String evidence) {}

    private static final Pattern RANGE = Pattern.compile("(?<!\\d)(20\\d{2})[^0-9]{0,5}(20\\d{2})(?!\\d)");
    private static final Pattern SINGLE = Pattern.compile("(?<!\\d)(20\\d{2})(?!\\d)");

    Detection detect(Path fcm) throws Exception {
        Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        try (Connection c = DriverManager.getConnection("jdbc:ucanaccess://" + fcm.toAbsolutePath())) {
            Detection fromLeague = detectFromLeague(c);
            if (fromLeague != null) {
                return fromLeague;
            }
        }
        throw new IllegalArgumentException(
            "Impossibile ricavare stagione e numero stagione dalla tabella LEGA del file FCM selezionato."
        );
    }

    private Detection detectFromLeague(Connection c) throws SQLException {
        String table = findTable(c, "LEGA");
        if (table == null) {
            return null;
        }

        Set<String> columns = columns(c, table);
        if (!containsIgnoreCase(columns, "STAGIONE")) {
            return null;
        }

        String seasonColumn = actualName(columns, "STAGIONE");
        String yearColumn = actualName(columns, "ANNOARCHIVIO");
        String nameColumn = actualName(columns, "NOME");

        StringBuilder sql = new StringBuilder("SELECT TOP 1 [")
            .append(escape(seasonColumn)).append("]");
        if (yearColumn != null) sql.append(", [").append(escape(yearColumn)).append("]");
        if (nameColumn != null) sql.append(", [").append(escape(nameColumn)).append("]");
        sql.append(" FROM [").append(escape(table)).append("]");

        try (Statement st = c.createStatement(); ResultSet r = st.executeQuery(sql.toString())) {
            if (!r.next()) {
                return null;
            }

            int seasonNumber = toPositiveInt(r.getObject(1));
            if (seasonNumber < 1) {
                throw new IllegalArgumentException("Il campo LEGA.STAGIONE non contiene un numero stagione valido.");
            }

            int index = 2;
            Integer archiveYear = null;
            if (yearColumn != null) {
                int value = toPositiveInt(r.getObject(index++));
                if (value >= 1900 && value <= 2200) archiveYear = value;
            }

            String leagueName = null;
            if (nameColumn != null) {
                Object value = r.getObject(index);
                if (value != null) leagueName = value.toString();
            }

            String seasonId = archiveYear == null ? parse(leagueName) : archiveYear + "_" + (archiveYear + 1);
            if (seasonId == null) {
                throw new IllegalArgumentException(
                    "Il file FCM contiene LEGA.STAGIONE=" + seasonNumber
                        + " ma non consente di ricavare gli anni della stagione."
                );
            }

            return new Detection(
                seasonId,
                seasonNumber,
                "LEGA.STAGIONE=" + seasonNumber
                    + (archiveYear == null ? "" : ", LEGA.ANNOARCHIVIO=" + archiveYear)
            );
        }
    }

    private static String findTable(Connection c, String expected) throws SQLException {
        DatabaseMetaData md = c.getMetaData();
        try (ResultSet tables = md.getTables(null, null, "%", new String[]{"TABLE"})) {
            while (tables.next()) {
                String name = tables.getString("TABLE_NAME");
                if (expected.equalsIgnoreCase(name)) return name;
            }
        }
        return null;
    }

    private static Set<String> columns(Connection c, String table) throws SQLException {
        Set<String> out = new LinkedHashSet<>();
        try (ResultSet cols = c.getMetaData().getColumns(null, null, table, "%")) {
            while (cols.next()) out.add(cols.getString("COLUMN_NAME"));
        }
        return out;
    }

    private static boolean containsIgnoreCase(Collection<String> values, String expected) {
        return actualName(values, expected) != null;
    }

    private static String actualName(Collection<String> values, String expected) {
        for (String value : values) {
            if (expected.equalsIgnoreCase(value)) return value;
        }
        return null;
    }

    private static int toPositiveInt(Object value) {
        if (value instanceof Number n) return n.intValue();
        if (value == null) return -1;
        try { return Integer.parseInt(value.toString().trim()); }
        catch (NumberFormatException ex) { return -1; }
    }

    private static String escape(String identifier) {
        return identifier.replace("]", "]]");
    }

    private static String parse(String value) {
        if (value == null) return null;
        Matcher range = RANGE.matcher(value);
        while (range.find()) {
            int a = Integer.parseInt(range.group(1));
            int b = Integer.parseInt(range.group(2));
            if (b == a + 1) return a + "_" + b;
        }
        Matcher single = SINGLE.matcher(value);
        if (single.find()) {
            int year = Integer.parseInt(single.group(1));
            return year + "_" + (year + 1);
        }
        return null;
    }
}
```

### src\main\java\it\alterlega\recordsnext\gui\HistoricalMappingDialog.java

```java
package it.alterlega.recordsnext.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

final class HistoricalMappingDialog extends JDialog {
    private static final Object NEW_IDENTITY = "<Nuova identità storica>";
    private static final Object EXCLUDE = "<Non elaborare>";

    private final HistoricalMappingRepository repository;
    private final List<String> seasons;
    private int seasonIndex;
    private String seasonId;
    private final JLabel heading = new JLabel();
    private final JLabel missingCount = new JLabel();
    private final JButton nextMissing = new JButton("Vai alla prossima mancante");
    private final JTabbedPane tabs = new JTabbedPane();
    private final List<RowEditor> competitionEditors = new ArrayList<>();
    private final List<RowEditor> teamEditors = new ArrayList<>();
    private final JButton previous = new JButton("<< Indietro");
    private final JButton next = new JButton("Salva e avanti >>");
    private boolean saved;

    HistoricalMappingDialog(Window owner, HistoricalMappingRepository repository) throws Exception {
        this(owner, repository, null);
    }

    HistoricalMappingDialog(Window owner, HistoricalMappingRepository repository, String initialSeason) throws Exception {
        super(owner, "RecordsNext - Associazioni storiche", ModalityType.APPLICATION_MODAL);
        this.repository = repository;
        repository.prepare();
        this.seasons = repository.seasonsNewestFirst();
        build();
        if (!seasons.isEmpty()) {
            int index = initialSeason == null ? 0 : seasons.indexOf(initialSeason);
            loadSeason(index < 0 ? 0 : index);
        }
    }

    boolean open() {
        if (seasons.isEmpty()) {
            JOptionPane.showMessageDialog(getOwner(), "Non ci sono stagioni gestite da configurare.", "RecordsNext", JOptionPane.INFORMATION_MESSAGE);
            return true;
        }
        setVisible(true);
        return saved;
    }

    private void build() {
        setLayout(new BorderLayout(8, 8));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(10, 12, 10, 12));
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 14f));
        missingCount.setFont(missingCount.getFont().deriveFont(Font.BOLD));
        missingCount.setForeground(new Color(185, 45, 35));
        nextMissing.addActionListener(e -> focusNextMissing());
        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.add(heading, BorderLayout.WEST);
        JPanel missingPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        missingPanel.add(missingCount);
        missingPanel.add(nextMissing);
        header.add(missingPanel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancel = new JButton("Annulla");
        cancel.addActionListener(e -> dispose());
        previous.addActionListener(e -> goPrevious());
        next.addActionListener(e -> saveAndNext());
        buttons.add(cancel); buttons.add(previous); buttons.add(next);
        add(buttons, BorderLayout.SOUTH);
        setSize(900, 700); setMinimumSize(new Dimension(780, 540)); setLocationRelativeTo(getOwner());
    }

    private void loadSeason(int index) throws Exception {
        seasonIndex = index;
        seasonId = seasons.get(index);
        competitionEditors.clear();
        teamEditors.clear();
        tabs.removeAll();
        tabs.addTab("1. Competizioni", createPage(HistoricalMappingRepository.Kind.COMPETITION, competitionEditors));
        tabs.addTab("2. Squadre", createPage(HistoricalMappingRepository.Kind.TEAM, teamEditors));
        boolean anchor = repository.isAnchor(seasonId);
        heading.setText("Stagione " + seasonId + "  (" + (index + 1) + "/" + seasons.size() + ") — " +
            (anchor ? "definizione delle identità attuali" : "associazione alle identità già definite"));
        previous.setEnabled(index > 0);
        next.setText(index == seasons.size() - 1 ? "Salva e termina" : "Salva e avanti >>");
        tabs.setSelectedIndex(0);
        updateMissingState();
        SwingUtilities.invokeLater(this::focusNextMissing);
    }

    private JScrollPane createPage(HistoricalMappingRepository.Kind kind, List<RowEditor> editors) throws Exception {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));
        GridBagConstraints h = new GridBagConstraints();
        h.gridy = 0; h.insets = new Insets(3, 4, 8, 4); h.anchor = GridBagConstraints.WEST;
        h.gridx = 0; h.weightx = .42; h.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JLabel(kind == HistoricalMappingRepository.Kind.COMPETITION ? "Competizione stagione" : "Squadra stagione"), h);
        h.gridx = 1; h.weightx = .58;
        panel.add(new JLabel(kind == HistoricalMappingRepository.Kind.COMPETITION ? "Identità storica/canonica" : "Identità storica/canonica"), h);

        int row = 1;
        for (var mapping : repository.load(seasonId, kind)) {
            RowEditor editor = new RowEditor(mapping, repository.isAnchor(seasonId));
            editors.add(editor);
            GridBagConstraints g = new GridBagConstraints();
            g.gridy = row++; g.insets = new Insets(3, 4, 3, 4); g.anchor = GridBagConstraints.WEST;
            g.gridx = 0; g.weightx = .42; g.fill = GridBagConstraints.HORIZONTAL;
            panel.add(editor.sourceLabel, g);
            g.gridx = 1; g.weightx = .58;
            panel.add(editor.combo, g);
        }
        GridBagConstraints filler = new GridBagConstraints();
        filler.gridy = row; filler.weighty = 1; filler.fill = GridBagConstraints.VERTICAL;
        panel.add(Box.createVerticalGlue(), filler);
        JScrollPane scroll = new JScrollPane(panel);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        return scroll;
    }

    private void updateMissingState() {
        long count = competitionEditors.stream().filter(RowEditor::isMissing).count()
            + teamEditors.stream().filter(RowEditor::isMissing).count();
        missingCount.setText(count == 0 ? "Associazioni complete" : count + " associazioni mancanti");
        missingCount.setForeground(count == 0 ? new Color(20, 120, 55) : new Color(185, 45, 35));
        nextMissing.setEnabled(count > 0);
    }

    private void focusNextMissing() {
        List<RowEditor> current = tabs.getSelectedIndex() == 0 ? competitionEditors : teamEditors;
        RowEditor missing = current.stream().filter(RowEditor::isMissing).findFirst().orElse(null);
        if (missing == null && tabs.getSelectedIndex() == 0) {
            tabs.setSelectedIndex(1);
            missing = teamEditors.stream().filter(RowEditor::isMissing).findFirst().orElse(null);
        }
        if (missing != null) {
            missing.combo.requestFocusInWindow();
            missing.combo.scrollRectToVisible(missing.combo.getBounds());
        }
    }

    private void saveAndNext() {
        try {
            saveCurrent();
            if (seasonIndex == seasons.size() - 1) {
                saved = true;
                dispose();
            } else {
                loadSeason(seasonIndex + 1);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "RecordsNext", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void goPrevious() {
        try {
            saveCurrent();
            loadSeason(seasonIndex - 1);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "RecordsNext", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveCurrent() throws Exception {
        repository.save(seasonId, HistoricalMappingRepository.Kind.COMPETITION,
            competitionEditors.stream().map(RowEditor::decision).toList());
        repository.save(seasonId, HistoricalMappingRepository.Kind.TEAM,
            teamEditors.stream().map(RowEditor::decision).toList());
    }

    private final class RowEditor {
        final HistoricalMappingRepository.MappingRow row;
        final JLabel sourceLabel;
        final JComboBox<Object> combo = new JComboBox<>();

        RowEditor(HistoricalMappingRepository.MappingRow row, boolean anchorSeason) {
            this.row = row;
            this.sourceLabel = new JLabel(row.sourceName());
            combo.addItem("<Selezionare>");
            combo.addItem(EXCLUDE);
            if (!anchorSeason) combo.addItem(NEW_IDENTITY);
            for (var identity : row.candidates()) combo.addItem(identity);

            Long preferredIdentityId = row.identityId() != null
                ? row.identityId()
                : row.inheritedIdentityId();
            if (preferredIdentityId != null) {
                for (int i = 0; i < combo.getItemCount(); i++) {
                    Object item = combo.getItemAt(i);
                    if (item instanceof HistoricalMappingRepository.Identity id && id.id() == preferredIdentityId) {
                        combo.setSelectedIndex(i);
                        break;
                    }
                }
            } else if ("ESCLUSA".equals(row.status())) {
                combo.setSelectedItem(EXCLUDE);
            } else {
                for (int i = 0; i < combo.getItemCount(); i++) {
                    Object item = combo.getItemAt(i);
                    if (item instanceof HistoricalMappingRepository.Identity id
                        && normalize(id.name()).equals(normalize(row.sourceName()))) {
                        combo.setSelectedIndex(i);
                        break;
                    }
                }
            }

            combo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                               boolean selected, boolean focus) {
                    JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, selected, focus);
                    if ("<Selezionare>".equals(value)) {
                        label.setForeground(selected ? Color.WHITE : new Color(185, 45, 35));
                        label.setFont(label.getFont().deriveFont(Font.BOLD));
                    } else if (EXCLUDE.equals(value) || NEW_IDENTITY.equals(value)) {
                        label.setFont(label.getFont().deriveFont(Font.BOLD));
                    }
                    return label;
                }
            });
            combo.addActionListener(e -> { updateVisualState(); updateMissingState(); });
            updateVisualState();
        }

        boolean isMissing() {
            return "<Selezionare>".equals(combo.getSelectedItem());
        }

        void updateVisualState() {
            boolean missing = isMissing();
            sourceLabel.setForeground(missing ? new Color(185, 45, 35) : UIManager.getColor("Label.foreground"));
            sourceLabel.setFont(sourceLabel.getFont().deriveFont(missing ? Font.BOLD : Font.PLAIN));
            combo.setBackground(missing ? new Color(255, 225, 220) : Color.WHITE);
            combo.setBorder(missing ? BorderFactory.createLineBorder(new Color(210, 60, 45), 2)
                                    : UIManager.getBorder("ComboBox.border"));
        }

        HistoricalMappingRepository.Decision decision() {
            Object selected = combo.getSelectedItem();
            if (selected instanceof HistoricalMappingRepository.Identity id) {
                return new HistoricalMappingRepository.Decision(row.seasonEntityIds(), row.sourceName(), id.id(), false, false);
            }
            return new HistoricalMappingRepository.Decision(row.seasonEntityIds(), row.sourceName(), null,
                NEW_IDENTITY.equals(selected), EXCLUDE.equals(selected));
        }

        private String normalize(String value) {
            return value == null ? "" : value.toLowerCase().replaceAll("[^a-z0-9]", "");
        }
    }
}
```

### src\main\java\it\alterlega\recordsnext\gui\HistoricalMappingRepository.java

```java
package it.alterlega.recordsnext.gui;

import java.nio.file.Path;
import java.sql.*;
import java.time.Instant;
import java.util.*;

final class HistoricalMappingRepository {
    enum Kind { COMPETITION, TEAM }

    record Identity(long id, String name) {
        @Override public String toString() { return name; }
    }

    record MappingRow(List<Long> seasonEntityIds, String sourceName, String normalizedName,
                      String status, Long identityId, Long inheritedIdentityId,
                      List<Identity> candidates) {}

    record Decision(List<Long> seasonEntityIds, String sourceName, Long identityId,
                    boolean createNew, boolean excluded) {}

    private final Path database;

    HistoricalMappingRepository(Path database) {
        this.database = database.toAbsolutePath().normalize();
    }

    /**
     * Prepara il database per la configurazione globale. Gli import FCM/FCA possono
     * produrre due righe tecniche della stessa entita nella stagione ancora; tali
     * righe devono condividere una sola identita canonica.
     */
    void prepare() throws Exception {
        try (Connection c = open()) {
            c.setAutoCommit(false);
            try {
                consolidateDuplicateIdentities(c, Kind.COMPETITION);
                consolidateDuplicateIdentities(c, Kind.TEAM);
                synchronizeGroupedMappings(c, Kind.COMPETITION);
                synchronizeGroupedMappings(c, Kind.TEAM);
                compactObsoleteSources(c);
                c.commit();
            } catch (Exception ex) {
                c.rollback();
                throw ex;
            }
        }
    }

    /** Tutte le stagioni gestite, inclusa l'attuale, dalla piu recente alla piu vecchia. */
    List<String> seasonsNewestFirst() throws Exception {
        String sql = "SELECT c.season_id " +
            "FROM rn_season_configuration c " +
            "JOIN rn_season s ON s.season_id=c.season_id " +
            "WHERE c.management_type='GESTITA' " +
            "ORDER BY COALESCE(s.sort_order,0) DESC, c.season_id DESC";
        try (Connection c = open(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            List<String> result = new ArrayList<>();
            while (rs.next()) result.add(rs.getString(1));
            return result;
        }
    }

    boolean isAnchor(String seasonId) throws Exception {
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(
                "SELECT is_anchor FROM rn_season WHERE season_id=?")) {
            ps.setString(1, seasonId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) == 1;
            }
        }
    }

    List<MappingRow> load(String seasonId, Kind kind) throws Exception {
        try (Connection c = open()) {
            String entityTable = kind == Kind.COMPETITION ? "rn_competition_season" : "rn_team_season";
            String entityId = kind == Kind.COMPETITION ? "competition_season_id" : "team_season_id";
            String mappingTable = kind == Kind.COMPETITION ? "rn_competition_mapping" : "rn_team_mapping";
            String identityTable = kind == Kind.COMPETITION ? "rn_competition_identity" : "rn_team_identity";
            String identityId = kind == Kind.COMPETITION ? "competition_identity_id" : "team_identity_id";

            // Sono disponibili solo le identita effettivamente mantenute in elaborazione.
            List<Identity> identities = new ArrayList<>();
            String identitySql = "SELECT i." + identityId + ",i.canonical_name " +
                "FROM " + identityTable + " i " +
                "WHERE EXISTS (SELECT 1 FROM " + mappingTable + " m " +
                "WHERE m." + identityId + "=i." + identityId + " AND m.mapping_status='ASSOCIATA') " +
                "ORDER BY i.canonical_name COLLATE NOCASE,i." + identityId;
            try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(identitySql)) {
                while (rs.next()) identities.add(new Identity(rs.getLong(1), rs.getString(2)));
            }

            String sql = "SELECT e." + entityId + ",e.source_name,e.normalized_name," +
                "COALESCE(m.mapping_status,'DA_CONFIGURARE'),m." + identityId + " " +
                "FROM " + entityTable + " e LEFT JOIN " + mappingTable + " m ON m." + entityId + "=e." + entityId + " " +
                "JOIN rn_source_file sf ON sf.source_file_id=e.source_file_id " +
                "WHERE e.season_id=? AND sf.import_id=(" +
                "SELECT MAX(sf2.import_id) FROM rn_source_file sf2 " +
                "WHERE sf2.season_id=e.season_id AND sf2.source_type='FCM') " +
                "ORDER BY e.source_name COLLATE NOCASE,e." + entityId;

            LinkedHashMap<String, Group> groups = new LinkedHashMap<>();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, seasonId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        long id = rs.getLong(1);
                        String source = rs.getString(2);
                        String normalized = rs.getString(3);
                        String key = normalize(normalized == null || normalized.isBlank() ? source : normalized);
                        Group group = groups.computeIfAbsent(key, k -> new Group(source, normalized));
                        group.ids.add(id);
                        String status = rs.getString(4);
                        Long mapped = rs.getObject(5) == null ? null : rs.getLong(5);
                        group.accept(status, mapped);
                    }
                }
            }

            List<MappingRow> rows = new ArrayList<>();
            for (Group group : groups.values()) {
                List<Identity> ordered = new ArrayList<>(identities);
                ordered.sort(Comparator
                    .comparingInt((Identity i) -> similarityRank(group.normalizedName, i.name()))
                    .thenComparing(Identity::name, String.CASE_INSENSITIVE_ORDER));
                Long inheritedIdentityId = group.identityId == null && "DA_CONFIGURARE".equals(group.status)
                    ? findInheritedIdentity(c, seasonId, kind, group.normalizedName, group.sourceName)
                    : null;
                rows.add(new MappingRow(List.copyOf(group.ids), group.sourceName, group.normalizedName,
                    group.status, group.identityId, inheritedIdentityId, ordered));
            }
            return rows;
        }
    }


    private static Long findInheritedIdentity(
        Connection c,
        String seasonId,
        Kind kind,
        String normalizedName,
        String sourceName
    ) throws SQLException {
        String entityTable = kind == Kind.COMPETITION ? "rn_competition_season" : "rn_team_season";
        String entityId = kind == Kind.COMPETITION ? "competition_season_id" : "team_season_id";
        String mappingTable = kind == Kind.COMPETITION ? "rn_competition_mapping" : "rn_team_mapping";
        String identityId = kind == Kind.COMPETITION ? "competition_identity_id" : "team_identity_id";
        String lookupName = (normalizedName == null || normalizedName.isBlank() ? sourceName : normalizedName)
            .trim().toLowerCase(Locale.ROOT);
        String sql = "SELECT m." + identityId + " " +
            "FROM " + entityTable + " e " +
            "JOIN " + mappingTable + " m ON m." + entityId + "=e." + entityId + " " +
            "JOIN rn_season newer ON newer.season_id=e.season_id " +
            "JOIN rn_season current ON current.season_id=? " +
            "WHERE LOWER(TRIM(e.normalized_name))=? " +
            "AND newer.sort_order>current.sort_order " +
            "AND m.mapping_status='ASSOCIATA' AND m." + identityId + " IS NOT NULL " +
            "ORDER BY newer.sort_order ASC LIMIT 1";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, seasonId);
            ps.setString(2, lookupName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : null;
            }
        }
    }

    void save(String seasonId, Kind kind, List<Decision> decisions) throws Exception {
        try (Connection c = open()) {
            c.setAutoCommit(false);
            try {
                Set<Long> used = new HashSet<>();
                for (Decision d : decisions) {
                    Long identityId = d.identityId();
                    String status;
                    String method;
                    if (d.createNew()) {
                        identityId = createIdentity(c, kind, seasonId, d.seasonEntityIds().get(0), d.sourceName());
                        status = "ASSOCIATA";
                        method = "NEW_HISTORICAL_IDENTITY";
                    } else if (identityId != null) {
                        if (!used.add(identityId)) {
                            throw new IllegalStateException("La stessa identita e stata scelta due volte nella stagione: " + d.sourceName());
                        }
                        status = "ASSOCIATA";
                        method = isAnchor(c, seasonId) ? "ANCHOR_GUI" : "GUI_MANUAL";
                    } else if (d.excluded()) {
                        status = "ESCLUSA";
                        method = "GUI_EXCLUDED";
                    } else {
                        throw new IllegalStateException("Decisione mancante per: " + d.sourceName());
                    }
                    for (long entityId : d.seasonEntityIds()) {
                        updateMapping(c, kind, entityId, identityId, status, method);
                    }
                }
                c.commit();
            } catch (Exception ex) {
                c.rollback();
                throw ex;
            }
        }
    }

    int pending(String seasonId) throws Exception {
        int pending = 0;
        for (MappingRow row : load(seasonId, Kind.COMPETITION)) {
            if ("DA_CONFIGURARE".equals(row.status())) pending++;
        }
        for (MappingRow row : load(seasonId, Kind.TEAM)) {
            if ("DA_CONFIGURARE".equals(row.status())) pending++;
        }
        return pending;
    }


    private static void synchronizeGroupedMappings(Connection c, Kind kind) throws Exception {
        String entityTable = kind == Kind.COMPETITION ? "rn_competition_season" : "rn_team_season";
        String entityId = kind == Kind.COMPETITION ? "competition_season_id" : "team_season_id";
        String mappingTable = kind == Kind.COMPETITION ? "rn_competition_mapping" : "rn_team_mapping";
        String identityId = kind == Kind.COMPETITION ? "competition_identity_id" : "team_identity_id";

        String groupsSql = "SELECT e.season_id,LOWER(TRIM(e.normalized_name))," +
            "COUNT(DISTINCT CASE WHEN m.mapping_status='ASSOCIATA' THEN m." + identityId + " END)," +
            "MIN(CASE WHEN m.mapping_status='ASSOCIATA' THEN m." + identityId + " END)," +
            "MAX(CASE WHEN m.mapping_status='ESCLUSA' THEN 1 ELSE 0 END) " +
            "FROM " + entityTable + " e LEFT JOIN " + mappingTable + " m ON m." + entityId + "=e." + entityId + " " +
            "GROUP BY e.season_id,LOWER(TRIM(e.normalized_name))";

        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(groupsSql)) {
            while (rs.next()) {
                String season = rs.getString(1);
                String normalized = rs.getString(2);
                int identities = rs.getInt(3);
                Long mapped = rs.getObject(4) == null ? null : rs.getLong(4);
                boolean excluded = rs.getInt(5) == 1;
                if (identities > 1) {
                    throw new IllegalStateException("Associazioni incoerenti per " + season + ": " + normalized);
                }
                if (mapped == null && !excluded) continue;
                String status = mapped != null ? "ASSOCIATA" : "ESCLUSA";
                String method = mapped != null ? "GUI_GROUP_SYNC" : "GUI_EXCLUDED_GROUP_SYNC";
                String update = "UPDATE " + mappingTable + " SET " + identityId + "=?,mapping_status=?,mapping_method=?,updated_at=? " +
                    "WHERE " + entityId + " IN (SELECT " + entityId + " FROM " + entityTable + " WHERE season_id=? AND LOWER(TRIM(normalized_name))=?)";
                try (PreparedStatement ps = c.prepareStatement(update)) {
                    if (mapped == null) ps.setNull(1, Types.BIGINT); else ps.setLong(1, mapped);
                    ps.setString(2, status);
                    ps.setString(3, method);
                    ps.setString(4, Instant.now().toString());
                    ps.setString(5, season);
                    ps.setString(6, normalized);
                    ps.executeUpdate();
                }
            }
        }
    }

    private static void compactObsoleteSources(Connection c) throws Exception {
        reanchorIdentities(c, Kind.COMPETITION);
        reanchorIdentities(c, Kind.TEAM);

        for (Kind kind : Kind.values()) {
            String entityTable = kind == Kind.COMPETITION ? "rn_competition_season" : "rn_team_season";
            String entityId = kind == Kind.COMPETITION ? "competition_season_id" : "team_season_id";
            String mappingTable = kind == Kind.COMPETITION ? "rn_competition_mapping" : "rn_team_mapping";
            String identityTable = kind == Kind.COMPETITION ? "rn_competition_identity" : "rn_team_identity";
            String anchorId = kind == Kind.COMPETITION ? "anchor_competition_season_id" : "anchor_team_season_id";

            String stale = "SELECT e." + entityId + " FROM " + entityTable + " e JOIN rn_source_file sf ON sf.source_file_id=e.source_file_id " +
                "WHERE sf.source_type='FCM' AND sf.import_id<>(SELECT MAX(sf2.import_id) FROM rn_source_file sf2 WHERE sf2.season_id=sf.season_id AND sf2.source_type='FCM') " +
                "AND NOT EXISTS(SELECT 1 FROM " + identityTable + " i WHERE i." + anchorId + "=e." + entityId + ")";
            try (Statement st = c.createStatement()) {
                st.executeUpdate("DELETE FROM " + mappingTable + " WHERE " + entityId + " IN (" + stale + ")");
                st.executeUpdate("DELETE FROM " + entityTable + " WHERE " + entityId + " IN (" + stale + ")");
            }
        }

        List<Long> obsoleteImports = new ArrayList<>();
        String obsoleteSql = "SELECT sf.import_id FROM rn_source_file sf WHERE sf.import_id<>(" +
            "SELECT MAX(sf2.import_id) FROM rn_source_file sf2 WHERE sf2.season_id=sf.season_id AND sf2.source_type=sf.source_type) " +
            "AND NOT EXISTS(SELECT 1 FROM rn_competition_season cs WHERE cs.source_file_id=sf.source_file_id) " +
            "AND NOT EXISTS(SELECT 1 FROM rn_team_season ts WHERE ts.source_file_id=sf.source_file_id)";
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(obsoleteSql)) {
            while (rs.next()) obsoleteImports.add(rs.getLong(1));
        }
        for (long importId : obsoleteImports) {
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM rn_source_file WHERE import_id=?")) {
                ps.setLong(1, importId); ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM rn_column_catalog WHERE import_id=?")) {
                ps.setLong(1, importId); ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM rn_table_catalog WHERE import_id=?")) {
                ps.setLong(1, importId); ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM rn_import WHERE import_id=?")) {
                ps.setLong(1, importId); ps.executeUpdate();
            }
        }
    }

    private static void reanchorIdentities(Connection c, Kind kind) throws Exception {
        String identityTable = kind == Kind.COMPETITION ? "rn_competition_identity" : "rn_team_identity";
        String identityId = kind == Kind.COMPETITION ? "competition_identity_id" : "team_identity_id";
        String anchorId = kind == Kind.COMPETITION ? "anchor_competition_season_id" : "anchor_team_season_id";
        String entityTable = kind == Kind.COMPETITION ? "rn_competition_season" : "rn_team_season";
        String entityId = kind == Kind.COMPETITION ? "competition_season_id" : "team_season_id";

        String sql = "SELECT i." + identityId + ",e.season_id,e.normalized_name FROM " + identityTable + " i " +
            "JOIN " + entityTable + " e ON e." + entityId + "=i." + anchorId + " " +
            "JOIN rn_source_file sf ON sf.source_file_id=e.source_file_id " +
            "WHERE sf.import_id<>(SELECT MAX(sf2.import_id) FROM rn_source_file sf2 WHERE sf2.season_id=sf.season_id AND sf2.source_type='FCM')";
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            List<Object[]> rows = new ArrayList<>();
            while (rs.next()) rows.add(new Object[]{rs.getLong(1), rs.getString(2), rs.getString(3)});
            for (Object[] row : rows) {
                long id = (Long) row[0];
                String season = (String) row[1];
                String normalized = (String) row[2];
                String latestSql = "SELECT e." + entityId + " FROM " + entityTable + " e JOIN rn_source_file sf ON sf.source_file_id=e.source_file_id " +
                    "WHERE e.season_id=? AND LOWER(TRIM(e.normalized_name))=LOWER(TRIM(?)) " +
                    "AND sf.import_id=(SELECT MAX(sf2.import_id) FROM rn_source_file sf2 WHERE sf2.season_id=e.season_id AND sf2.source_type='FCM') LIMIT 1";
                try (PreparedStatement find = c.prepareStatement(latestSql)) {
                    find.setString(1, season); find.setString(2, normalized);
                    try (ResultSet latest = find.executeQuery()) {
                        if (latest.next()) {
                            try (PreparedStatement update = c.prepareStatement("UPDATE " + identityTable + " SET " + anchorId + "=? WHERE " + identityId + "=?")) {
                                update.setLong(1, latest.getLong(1)); update.setLong(2, id); update.executeUpdate();
                            }
                        }
                    }
                }
            }
        }
    }
    private Connection open() throws Exception {
        Class.forName("org.sqlite.JDBC");
        Connection c = DriverManager.getConnection("jdbc:sqlite:" + database);
        try (Statement st = c.createStatement()) {
            st.execute("PRAGMA foreign_keys=ON");
            st.execute("PRAGMA busy_timeout=10000");
        }
        return c;
    }

    private static boolean isAnchor(Connection c, String seasonId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT is_anchor FROM rn_season WHERE season_id=?")) {
            ps.setString(1, seasonId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) == 1;
            }
        }
    }

    private static void consolidateDuplicateIdentities(Connection c, Kind kind) throws Exception {
        String identityTable = kind == Kind.COMPETITION ? "rn_competition_identity" : "rn_team_identity";
        String identityId = kind == Kind.COMPETITION ? "competition_identity_id" : "team_identity_id";
        String mappingTable = kind == Kind.COMPETITION ? "rn_competition_mapping" : "rn_team_mapping";

        String groupsSql = "SELECT anchor_season_id,LOWER(TRIM(canonical_name)),MIN(" + identityId + ") " +
            "FROM " + identityTable + " GROUP BY anchor_season_id,LOWER(TRIM(canonical_name)) HAVING COUNT(*)>1";
        List<long[]> duplicateGroups = new ArrayList<>();
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(groupsSql)) {
            while (rs.next()) {
                String anchorSeason = rs.getString(1);
                String normalizedName = rs.getString(2);
                long keeper = rs.getLong(3);
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT " + identityId + " FROM " + identityTable +
                        " WHERE anchor_season_id=? AND LOWER(TRIM(canonical_name))=? AND " + identityId + "<>?")) {
                    ps.setString(1, anchorSeason);
                    ps.setString(2, normalizedName);
                    ps.setLong(3, keeper);
                    try (ResultSet duplicates = ps.executeQuery()) {
                        while (duplicates.next()) duplicateGroups.add(new long[]{keeper, duplicates.getLong(1)});
                    }
                }
            }
        }

        for (long[] pair : duplicateGroups) {
            long keeper = pair[0];
            long duplicate = pair[1];
            try (PreparedStatement update = c.prepareStatement(
                    "UPDATE " + mappingTable + " SET " + identityId + "=? WHERE " + identityId + "=?")) {
                update.setLong(1, keeper);
                update.setLong(2, duplicate);
                update.executeUpdate();
            }
            try (PreparedStatement delete = c.prepareStatement(
                    "DELETE FROM " + identityTable + " WHERE " + identityId + "=?")) {
                delete.setLong(1, duplicate);
                delete.executeUpdate();
            }
        }
    }

    private static long createIdentity(Connection c, Kind kind, String seasonId,
                                       long entityId, String name) throws Exception {
        String table = kind == Kind.COMPETITION ? "rn_competition_identity" : "rn_team_identity";
        String anchorCol = kind == Kind.COMPETITION ? "anchor_competition_season_id" : "anchor_team_season_id";
        String sql = "INSERT INTO " + table + "(anchor_season_id," + anchorCol + ",canonical_name,created_at) VALUES(?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, seasonId); ps.setLong(2, entityId); ps.setString(3, name); ps.setString(4, Instant.now().toString());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) throw new IllegalStateException("Identita non creata: " + name);
                return rs.getLong(1);
            }
        }
    }

    private static void updateMapping(Connection c, Kind kind, long entityId, Long identityId,
                                      String status, String method) throws Exception {
        String table = kind == Kind.COMPETITION ? "rn_competition_mapping" : "rn_team_mapping";
        String entityCol = kind == Kind.COMPETITION ? "competition_season_id" : "team_season_id";
        String identityCol = kind == Kind.COMPETITION ? "competition_identity_id" : "team_identity_id";
        String sql = "INSERT INTO " + table + "(" + entityCol + "," + identityCol + ",mapping_status,mapping_method,notes,updated_at) " +
            "VALUES(?,?,?,?,NULL,?) ON CONFLICT(" + entityCol + ") DO UPDATE SET " + identityCol + "=excluded." + identityCol + "," +
            "mapping_status=excluded.mapping_status,mapping_method=excluded.mapping_method,notes=NULL,updated_at=excluded.updated_at";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, entityId);
            if (identityId == null) ps.setNull(2, Types.BIGINT); else ps.setLong(2, identityId);
            ps.setString(3, status); ps.setString(4, method); ps.setString(5, Instant.now().toString());
            ps.executeUpdate();
        }
    }

    private static int similarityRank(String normalized, String candidate) {
        String a = normalize(normalized), b = normalize(candidate);
        if (a.equals(b)) return 0;
        if (a.contains(b) || b.contains(a)) return 1;
        return 2;
    }

    private static String normalize(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private static final class Group {
        final String sourceName;
        final String normalizedName;
        final List<Long> ids = new ArrayList<>();
        String status = "DA_CONFIGURARE";
        Long identityId;
        Group(String sourceName, String normalizedName) {
            this.sourceName = sourceName;
            this.normalizedName = normalizedName;
        }
        void accept(String candidateStatus, Long candidateIdentity) {
            if (candidateIdentity != null && identityId == null) identityId = candidateIdentity;
            if ("ASSOCIATA".equals(candidateStatus)) status = "ASSOCIATA";
            else if (!"ASSOCIATA".equals(status) && "ESCLUSA".equals(candidateStatus)) status = "ESCLUSA";
        }
    }
}
```

### src\main\java\it\alterlega\recordsnext\gui\RecordsNextApp.java

```java
package it.alterlega.recordsnext.gui;

import it.alterlega.recordsnext.ConfigurationSchema;
import it.alterlega.recordsnext.app.PipelineConfig;
import it.alterlega.recordsnext.app.ProcessingOptions;
import it.alterlega.recordsnext.app.ProcessingMode;
import it.alterlega.recordsnext.app.RecordsNextPipeline;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.image.BufferedImage;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class RecordsNextApp {
    private static final String KEY_CLASSIC = "processing.classic";
    private static final String KEY_RU = "processing.ru";
    private static final String KEY_GENERATE_JS = "processing.generateJs";
    private static final String KEY_PUBLISH = "processing.publish";
    private static final String KEY_MODE = "processing.mode";
    private static final String KEY_PUBLISH_MODE = "publish.destinationMode";
    private static final String KEY_PUBLISH_CUSTOM = "publish.customDirectory";

    private final JFrame frame = new JFrame("FCM RecordsNext 1.0");
    private final JCheckBox classic = new JCheckBox("Record classici");
    private final JCheckBox ru = new JCheckBox("Riserve d'ufficio");
    private final JCheckBox generateJs = new JCheckBox("Genera file JavaScript");
    private final JCheckBox publish = new JCheckBox("Pubblica i file nel sito");
    private final JRadioButton publishCurrent = new JRadioButton("Cartella js della stagione attuale");
    private final JRadioButton publishCustom = new JRadioButton("Cartella personalizzata");
    private final JTextField publishDirectory = new JTextField();
    private final JButton publishBrowse = new JButton("...");
    private final JLabel publishResolved = new JLabel(" ");
    private final JRadioButton fullMode = new JRadioButton("Elaborazione completa");
    private final JRadioButton consolidatedMode = new JRadioButton("Aggiornamento da consolidamento");
    private final JTextArea log = new JTextArea(10, 48);
    private final JProgressBar phaseProgress = new JProgressBar();
    private final JProgressBar progress = new JProgressBar(0, 100);
    private final JLabel phaseLabel = new JLabel("Nessuna operazione in corso");
    private final JButton start = new JButton("Avvia");
    private final JLabel status = new JLabel("Pronto", SwingConstants.CENTER);
    private final Path root = Path.of("").toAbsolutePath().normalize();
    private final Path configPath = root.resolve("config/recordsnext-gui.properties");
    private final Properties properties = new Properties();
    private boolean loadingSelections;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RecordsNextApp().show());
    }

    private RecordsNextApp() {
        bootstrapRuntimeDirectories();
        loadProperties();
        build();
        loadSelections();
    }

    private void bootstrapRuntimeDirectories() {
        String[] directories = {
                "config",
                "data/database"
        };
        try {
            for (String directory : directories) {
                Files.createDirectories(root.resolve(directory));
            }
            ConfigurationSchema.initializeEmpty(root.resolve("data/database/recordsnext.db"));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Impossibile creare le cartelle di lavoro di RecordsNext:\n" + ex.getMessage(),
                    "FCM RecordsNext 1.0", JOptionPane.ERROR_MESSAGE);
            throw new IllegalStateException("Bootstrap delle cartelle fallito", ex);
        }
    }

    private void build() {
        UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 13));
        UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("CheckBox.font", new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("RadioButton.font", new Font("Segoe UI", Font.PLAIN, 13));

        Color background = new Color(244, 247, 252);
        Color panelBorder = new Color(196, 205, 222);
        Color blue = new Color(34, 72, 150);
        Color red = new Color(201, 34, 45);

        JPanel rootPanel = new JPanel(new BorderLayout(12, 12));
        rootPanel.setBorder(new EmptyBorder(16, 20, 12, 20));
        rootPanel.setBackground(background);

        JPanel header = new JPanel(new GridBagLayout());
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(590, 112));
        header.setMinimumSize(new Dimension(590, 112));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 112));
        GridBagConstraints hg = new GridBagConstraints();
        hg.gridx = 0;
        hg.weightx = 1;
        hg.fill = GridBagConstraints.HORIZONTAL;
        hg.anchor = GridBagConstraints.CENTER;

        JLabel title = new JLabel("FCM RecordsNext 1.0", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI Black", Font.BOLD, 35));
        title.setForeground(red);
        hg.gridy = 0;
        header.add(title, hg);

        JLabel sub = new JLabel("Records storici e tanto altro", SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
        sub.setForeground(new Color(62, 72, 92));
        hg.gridy = 1;
        hg.insets = new Insets(4, 0, 0, 0);
        header.add(sub, hg);

        status.setFont(new Font("Segoe UI", Font.BOLD, 13));
        status.setForeground(new Color(35, 105, 62));
        status.setPreferredSize(new Dimension(540, 26));
        status.setMinimumSize(new Dimension(540, 26));
        hg.gridy = 2;
        hg.insets = new Insets(11, 0, 0, 0);
        header.add(status, hg);
        rootPanel.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        JPanel options = new JPanel(new GridBagLayout());
        options.setBackground(Color.WHITE);
        options.setBorder(new CompoundBorder(
                new LineBorder(panelBorder),
                new EmptyBorder(12, 15, 12, 15)));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 0;
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        g.insets = new Insets(3, 4, 7, 4);

        JLabel modeTitle = new JLabel("Modalità");
        modeTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        modeTitle.setForeground(blue);
        options.add(modeTitle, g);
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(fullMode);
        modeGroup.add(consolidatedMode);
        g.gridy++;
        options.add(fullMode, g);
        g.gridy++;
        options.add(consolidatedMode, g);
        g.gridy++;
        g.insets = new Insets(10, 4, 7, 4);

        JLabel sectionTitle = new JLabel("Elaborazioni");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sectionTitle.setForeground(blue);
        options.add(sectionTitle, g);

        g.insets = new Insets(2, 4, 2, 4);
        g.gridy++;
        options.add(classic, g);
        g.gridy++;
        options.add(ru, g);
        g.gridy++;
        options.add(generateJs, g);
        g.gridy++;
        options.add(publish, g);

        ButtonGroup publishGroup = new ButtonGroup();
        publishGroup.add(publishCurrent);
        publishGroup.add(publishCustom);
        JPanel publishDestination = new JPanel(new GridBagLayout());
        publishDestination.setOpaque(false);
        publishDestination.setBorder(new EmptyBorder(3, 24, 2, 0));
        GridBagConstraints dg = new GridBagConstraints();
        dg.gridx = 0;
        dg.gridy = 0;
        dg.gridwidth = 3;
        dg.anchor = GridBagConstraints.WEST;
        dg.fill = GridBagConstraints.HORIZONTAL;
        dg.weightx = 1;
        publishDestination.add(publishCurrent, dg);
        dg.gridy = 1;
        publishDestination.add(publishCustom, dg);
        dg.gridy = 2;
        dg.gridwidth = 1;
        dg.weightx = 1;
        publishDestination.add(publishDirectory, dg);
        dg.gridx = 1;
        dg.weightx = 0;
        dg.fill = GridBagConstraints.NONE;
        publishDestination.add(publishBrowse, dg);
        dg.gridx = 0;
        dg.gridy = 3;
        dg.gridwidth = 3;
        dg.fill = GridBagConstraints.HORIZONTAL;
        publishResolved.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        publishResolved.setForeground(new Color(90, 98, 112));
        publishDestination.add(publishResolved, dg);
        g.gridy++;
        g.insets = new Insets(0, 4, 2, 4);
        options.add(publishDestination, g);

        JLabel savedHint = new JLabel("Le scelte vengono memorizzate automaticamente.");
        savedHint.setForeground(new Color(90, 98, 112));
        savedHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        g.gridy++;
        g.insets = new Insets(8, 7, 1, 4);
        options.add(savedHint, g);
        options.setAlignmentX(Component.LEFT_ALIGNMENT);
        options.setMaximumSize(new Dimension(Integer.MAX_VALUE, options.getPreferredSize().height));
        center.add(options);
        center.add(Box.createVerticalStrut(10));

        JPanel progressPanel = new JPanel(new GridBagLayout());
        progressPanel.setBackground(Color.WHITE);
        progressPanel.setBorder(new CompoundBorder(
                new LineBorder(panelBorder),
                new EmptyBorder(10, 12, 10, 12)));
        GridBagConstraints pg = new GridBagConstraints();
        pg.gridx = 0;
        pg.weightx = 1;
        pg.fill = GridBagConstraints.HORIZONTAL;
        pg.anchor = GridBagConstraints.WEST;
        pg.insets = new Insets(2, 2, 3, 2);

        JLabel phaseTitle = new JLabel("Operazione corrente");
        phaseTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        phaseTitle.setForeground(blue);
        pg.gridy = 0;
        progressPanel.add(phaseTitle, pg);
        phaseLabel.setPreferredSize(new Dimension(540, 22));
        phaseLabel.setMinimumSize(new Dimension(540, 22));
        pg.gridy = 1;
        progressPanel.add(phaseLabel, pg);
        phaseProgress.setIndeterminate(false);
        phaseProgress.setStringPainted(false);
        phaseProgress.setPreferredSize(new Dimension(540, 16));
        pg.gridy = 2;
        progressPanel.add(phaseProgress, pg);

        JLabel overallTitle = new JLabel("Avanzamento generale");
        overallTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        overallTitle.setForeground(blue);
        pg.gridy = 3;
        pg.insets = new Insets(9, 2, 3, 2);
        progressPanel.add(overallTitle, pg);
        progress.setStringPainted(true);
        progress.setValue(0);
        progress.setString("0%");
        progress.setPreferredSize(new Dimension(540, 20));
        pg.gridy = 4;
        pg.insets = new Insets(2, 2, 2, 2);
        progressPanel.add(progress, pg);
        progressPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        progressPanel.setPreferredSize(new Dimension(560, 126));
        progressPanel.setMinimumSize(new Dimension(560, 126));
        progressPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 126));
        center.add(progressPanel);
        center.add(Box.createVerticalStrut(10));

        log.setEditable(false);
        log.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        log.setLineWrap(true);
        log.setWrapStyleWord(true);
        DefaultCaret logCaret = (DefaultCaret) log.getCaret();
        logCaret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JScrollPane logScroll = new JScrollPane(
                log,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        logScroll.setBorder(new LineBorder(panelBorder));
        logScroll.setPreferredSize(new Dimension(560, 190));
        logScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        logScroll.setMinimumSize(new Dimension(560, 120));
        logScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        logScroll.getVerticalScrollBar().setUnitIncrement(18);
        logScroll.getHorizontalScrollBar().setUnitIncrement(18);
        center.add(logScroll);
        rootPanel.add(center, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout(0, 8));
        south.setOpaque(false);
        JPanel credits = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        credits.setOpaque(false);
        JLabel credit = new JLabel("powered by mauz79 © 2026");
        credit.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        credit.setForeground(new Color(82, 89, 105));
        credits.add(credit);
        south.add(credits, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttons.setOpaque(false);
        JButton config = new JButton("Configurazione");
        JButton exit = new JButton("Esci");
        buttons.add(start);
        buttons.add(config);
        buttons.add(exit);
        south.add(buttons, BorderLayout.SOUTH);
        rootPanel.add(south, BorderLayout.SOUTH);

        fullMode.addActionListener(e -> saveSelections());
        consolidatedMode.addActionListener(e -> saveSelections());
        classic.addActionListener(e -> saveSelections());
        ru.addActionListener(e -> saveSelections());
        generateJs.addActionListener(e -> {
            if (!generateJs.isSelected()) {
                publish.setSelected(false);
            }
            publish.setEnabled(generateJs.isSelected());
            updatePublishControls();
            saveSelections();
        });
        publish.addActionListener(e -> {
            updatePublishControls();
            saveSelections();
        });
        publishCurrent.addActionListener(e -> {
            updatePublishControls();
            saveSelections();
        });
        publishCustom.addActionListener(e -> {
            updatePublishControls();
            saveSelections();
        });
        publishBrowse.addActionListener(e -> choosePublishDirectory());
        start.addActionListener(e -> runPipeline());
        exit.addActionListener(e -> closeApplication());
        config.addActionListener(e -> openConfiguration());

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveSelections();
            }
        });

        frame.setIconImage(createAppIcon());
        frame.setContentPane(rootPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setResizable(true);
        frame.setMinimumSize(new Dimension(660, 760));
        frame.setPreferredSize(new Dimension(680, 920));
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    private static Image createAppIcon() {
        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(new Color(201, 34, 45));
            graphics.fillRoundRect(3, 3, 58, 58, 14, 14);
            graphics.setColor(Color.WHITE);
            graphics.setFont(new Font("Segoe UI", Font.BOLD, 25));
            FontMetrics metrics = graphics.getFontMetrics();
            String text = "RN";
            graphics.drawString(text, (64 - metrics.stringWidth(text)) / 2, 42);
        } finally {
            graphics.dispose();
        }
        return image;
    }

    private void show() {
        frame.setVisible(true);
    }

    private void loadProperties() {
        if (!Files.isRegularFile(configPath)) {
            return;
        }
        try (InputStream input = Files.newInputStream(configPath)) {
            properties.load(input);
        } catch (IOException ex) {
            throw new IllegalStateException("Impossibile leggere " + configPath, ex);
        }
    }

    private void loadSelections() {
        loadingSelections = true;
        try {
            classic.setSelected(readBoolean(KEY_CLASSIC, true));
            ru.setSelected(readBoolean(KEY_RU, false));
            generateJs.setSelected(readBoolean(KEY_GENERATE_JS, true));
            publish.setSelected(readBoolean(KEY_PUBLISH, true) && generateJs.isSelected());
            publish.setEnabled(generateJs.isSelected());
            boolean customDestination = "custom".equalsIgnoreCase(
                properties.getProperty(KEY_PUBLISH_MODE, "currentSeason"));
            publishCustom.setSelected(customDestination);
            publishCurrent.setSelected(!customDestination);
            publishDirectory.setText(properties.getProperty(KEY_PUBLISH_CUSTOM, ""));
            updatePublishControls();
            boolean consolidated = "CONSOLIDATED".equalsIgnoreCase(properties.getProperty(KEY_MODE, "FULL"));
            consolidatedMode.setSelected(consolidated);
            fullMode.setSelected(!consolidated);
            try {
                var cfg = PipelineConfig.load(root, configPath);
                boolean available = new RecordsNextPipeline().hasConsolidation(cfg);
                consolidatedMode.setEnabled(available);
                if (!available) fullMode.setSelected(true);
            } catch (Exception ignored) {
                consolidatedMode.setEnabled(false);
                fullMode.setSelected(true);
            }
        } finally {
            loadingSelections = false;
        }
    }

    private boolean readBoolean(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        return value == null ? defaultValue : Boolean.parseBoolean(value.trim());
    }

    private void saveSelections() {
        if (loadingSelections) {
            return;
        }
        properties.setProperty(KEY_CLASSIC, Boolean.toString(classic.isSelected()));
        properties.setProperty(KEY_RU, Boolean.toString(ru.isSelected()));
        properties.setProperty(KEY_GENERATE_JS, Boolean.toString(generateJs.isSelected()));
        properties.setProperty(KEY_PUBLISH, Boolean.toString(publish.isSelected()));
        properties.setProperty(KEY_MODE, consolidatedMode.isSelected() ? "CONSOLIDATED" : "FULL");
        properties.setProperty(KEY_PUBLISH_MODE,
            publishCustom.isSelected() ? "custom" : "currentSeason");
        properties.setProperty(KEY_PUBLISH_CUSTOM, publishDirectory.getText().trim());
        try {
            Files.createDirectories(configPath.getParent());
            try (OutputStream output = Files.newOutputStream(configPath)) {
                properties.store(output, "RecordsNext configuration");
            }
        } catch (IOException ex) {
            status.setText("Impossibile salvare la configurazione");
            log.append("AVVISO: impossibile salvare " + configPath + ": " + ex.getMessage()
                    + System.lineSeparator());
        }
    }

    private void openConfiguration() {
        saveSelections();
        RecordsNextConfigurationDialog dialog =
                new RecordsNextConfigurationDialog(frame, root, configPath);
        if (dialog.open()) {
            properties.clear();
            loadProperties();
            loadSelections();
            status.setText("Configurazione salvata");
            log.append("Configurazione aggiornata." + System.lineSeparator());
        }
    }

    private void updatePublishControls() {
        boolean enabled = generateJs.isSelected() && publish.isSelected();
        publishCurrent.setEnabled(enabled);
        publishCustom.setEnabled(enabled);
        boolean custom = enabled && publishCustom.isSelected();
        publishDirectory.setEnabled(custom);
        publishBrowse.setEnabled(custom);
        try {
            Path resolved = PipelineConfig.resolvePublishDirectory(root, propertiesForCurrentUi());
            publishResolved.setText("Destinazione: " + resolved);
        } catch (Exception ex) {
            publishResolved.setText("Destinazione non disponibile");
        }
    }

    private Properties propertiesForCurrentUi() {
        Properties copy = new Properties();
        copy.putAll(properties);
        copy.setProperty(KEY_PUBLISH_MODE,
            publishCustom.isSelected() ? "custom" : "currentSeason");
        copy.setProperty(KEY_PUBLISH_CUSTOM, publishDirectory.getText().trim());
        return copy;
    }

    private void choosePublishDirectory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Seleziona la cartella di pubblicazione");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        String current = publishDirectory.getText().trim();
        if (!current.isEmpty() && Files.isDirectory(Path.of(current))) {
            chooser.setCurrentDirectory(Path.of(current).toFile());
        } else {
            String remembered = properties.getProperty("chooser.lastPublishDirectory", "").trim();
            if (!remembered.isEmpty() && Files.isDirectory(Path.of(remembered))) {
                chooser.setCurrentDirectory(Path.of(remembered).toFile());
            }
        }
        if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            Path selected = chooser.getSelectedFile().toPath().toAbsolutePath().normalize();
            publishDirectory.setText(selected.toString());
            properties.setProperty("chooser.lastPublishDirectory", selected.toString());
            updatePublishControls();
            saveSelections();
        }
    }

    private void closeApplication() {
        saveSelections();
        frame.dispose();
    }

    private void runPipeline() {
        saveSelections();
        if (publish.isSelected() && publishCustom.isSelected()
                && publishDirectory.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame,
                "Selezionare la cartella personalizzata di pubblicazione.",
                "RecordsNext", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            var cfg = PipelineConfig.load(root, configPath);
            if (publish.isSelected() && !Files.isDirectory(cfg.siteJs())) {
                JOptionPane.showMessageDialog(frame,
                    "La cartella di pubblicazione non esiste:\n" + cfg.siteJs(),
                    "RecordsNext", JOptionPane.WARNING_MESSAGE);
                return;
            }
            HistoricalMappingRepository repository = new HistoricalMappingRepository(
                root.resolve("data/database/recordsnext.db"));
            repository.prepare();
            String incompleteSeason = null;
            for (String season : repository.seasonsNewestFirst()) {
                if (cfg.seasons().contains(season) && repository.pending(season) > 0) {
                    incompleteSeason = season;
                    break;
                }
            }
            if (incompleteSeason != null) {
                int pending = repository.pending(incompleteSeason);
                status.setText("Configurazione incompleta");
                JOptionPane.showMessageDialog(frame,
                    incompleteSeason + ": restano " + pending + " associazioni da configurare.",
                    "RecordsNext", JOptionPane.WARNING_MESSAGE);
                HistoricalMappingDialog dialog = new HistoricalMappingDialog(frame, repository, incompleteSeason);
                dialog.open();
                return;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "RecordsNext", JOptionPane.ERROR_MESSAGE);
            return;
        }
        final ProcessingOptions options;
        try {
            options = new ProcessingOptions(
                    classic.isSelected(), ru.isSelected(),
                    generateJs.isSelected(), publish.isSelected());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    frame, ex.getMessage(), "RecordsNext", JOptionPane.WARNING_MESSAGE);
            return;
        }

        start.setEnabled(false);
        log.setText("");
        progress.setValue(0);
        progress.setString("0%");
        phaseLabel.setText("Preparazione elaborazione");
        phaseProgress.setIndeterminate(true);
        status.setText("Elaborazione in corso");
        status.setForeground(new Color(35, 82, 150));

        new SwingWorker<RecordsNextPipeline.Result, String>() {
            @Override
            protected RecordsNextPipeline.Result doInBackground() throws Exception {
                var cfg = PipelineConfig.load(root, configPath);
                ProcessingMode mode = consolidatedMode.isSelected()
                    ? ProcessingMode.CONSOLIDATED : ProcessingMode.FULL;
                return new RecordsNextPipeline().run(cfg, options, mode,
                    new RecordsNextPipeline.Listener() {
                        @Override
                        public void phase(String text, int percent) {
                            publish(text);
                            SwingUtilities.invokeLater(() -> {
                                phaseLabel.setText(text);
                                if (percent >= 0) {
                                    progress.setValue(percent);
                                    progress.setString(percent + "%");
                                }
                            });
                        }

                        @Override
                        public void timing(String text) {
                            publish("TEMPO  " + text);
                        }
                    });
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                chunks.forEach(value -> log.append(value + System.lineSeparator()));
            }

            @Override
            protected void done() {
                try {
                    var result = get();
                    log.append("File validi: " + result.files()
                            + "; pubblicati: " + result.published()
                            + System.lineSeparator());
                    status.setText("Elaborazione completata");
                    status.setForeground(new Color(35, 105, 62));
                    phaseLabel.setText("Elaborazione completata");
                    phaseProgress.setIndeterminate(false);
                    phaseProgress.setValue(100);
                    consolidatedMode.setEnabled(true);
                } catch (Exception ex) {
                    status.setText("Errore");
                    status.setForeground(new Color(178, 38, 45));
                    phaseLabel.setText("Elaborazione interrotta");
                    phaseProgress.setIndeterminate(false);
                    phaseProgress.setValue(0);
                    Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                    log.append("ERRORE: " + cause + System.lineSeparator());
                    JOptionPane.showMessageDialog(
                            frame, String.valueOf(cause),
                            "Errore RecordsNext", JOptionPane.ERROR_MESSAGE);
                } finally {
                    start.setEnabled(true);
                }
            }
        }.execute();
    }
}
```

### src\main\java\it\alterlega\recordsnext\gui\RecordsNextConfigurationDialog.java

```java
package it.alterlega.recordsnext.gui;

import it.alterlega.recordsnext.ConfigurationSchema;
import it.alterlega.recordsnext.RawSqliteImporter;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

final class RecordsNextConfigurationDialog extends JDialog {
    private static final Pattern SEASON = Pattern.compile("\\d{4}_\\d{4}");
    private final Path projectRoot, configPath, databasePath;
    private final Properties properties = new Properties();
    private final JPanel seasonsPanel = new JPanel();
    private final List<SeasonEditor> editors = new ArrayList<>();
    private final SeasonConfigurationRepository repository;
    private boolean saved;

    RecordsNextConfigurationDialog(Window owner, Path projectRoot, Path configPath) {
        super(owner,"RecordsNext - Configurazione stagioni",ModalityType.APPLICATION_MODAL);
        this.projectRoot=projectRoot; this.configPath=configPath;
        loadProperties();
        this.databasePath=projectRoot.resolve(properties.getProperty("database","data/database/recordsnext.db")).normalize();
        this.repository=new SeasonConfigurationRepository(databasePath);
        build(); loadSeasons();
    }
    boolean open(){ setVisible(true); return saved; }

    private void build(){
        JPanel root=new JPanel(new BorderLayout(10,10)); root.setBorder(new EmptyBorder(12,14,12,14));
        JPanel top=new JPanel(new BorderLayout());
        JLabel info=new JLabel("Configurare le stagioni gestite o manuali e, successivamente, i relativi siti.");
        JPanel topButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton mappings = new JButton("Configura associazioni storiche...");
        mappings.addActionListener(e -> openMappings());
        JButton add=new JButton("Aggiungi stagione"); add.addActionListener(e->addSeason());
        topButtons.add(mappings); topButtons.add(add);
        top.add(info,BorderLayout.WEST); top.add(topButtons,BorderLayout.EAST); root.add(top,BorderLayout.NORTH);
        seasonsPanel.setLayout(new BoxLayout(seasonsPanel,BoxLayout.Y_AXIS)); seasonsPanel.setBorder(new EmptyBorder(4,4,4,4));
        JScrollPane scroll=new JScrollPane(seasonsPanel); scroll.getVerticalScrollBar().setUnitIncrement(20); root.add(scroll,BorderLayout.CENTER);
        JPanel buttons=new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancel=new JButton("Annulla"), save=new JButton("Salva");
        cancel.addActionListener(e->dispose()); save.addActionListener(e->saveConfiguration());
        buttons.add(cancel); buttons.add(save); root.add(buttons,BorderLayout.SOUTH);
        setContentPane(root); setDefaultCloseOperation(DISPOSE_ON_CLOSE); setSize(980,720); setMinimumSize(new Dimension(860,600)); setLocationRelativeTo(getOwner());
    }

    private void loadProperties(){
        if(Files.isRegularFile(configPath)) try(InputStream in=Files.newInputStream(configPath)){properties.load(in);} catch(IOException ex){error("Lettura configurazione",ex);}
    }
    private void loadSeasons(){
        try {
            if (Files.isRegularFile(databasePath)) {
                new HistoricalMappingRepository(databasePath).prepare();
            }
        } catch (Exception ex) {
            error("Pulizia configurazione storica", ex);
        }
        editors.clear(); seasonsPanel.removeAll();
        Set<String> selected=Arrays.stream(properties.getProperty("seasons","").split(",")).map(String::trim).filter(s->!s.isEmpty()).collect(Collectors.toSet());
        try{
            for(var loaded:repository.load()) {
                var row = refreshManagedMetadata(loaded);
                addEditor(new SeasonEditor(row,selected.contains(row.seasonId())));
            }
        }catch(Exception ex){error("Lettura stagioni",ex);}
        refresh();
    }


    private SeasonConfigurationRepository.SeasonRow refreshManagedMetadata(
        SeasonConfigurationRepository.SeasonRow row
    ) {
        if (!"GESTITA".equals(row.managementType()) || row.fcmPath().isBlank()) {
            return row;
        }
        try {
            Path fcm = Path.of(row.fcmPath());
            if (!Files.isRegularFile(fcm)) return row;
            var detection = new FcmSeasonDetector().detect(fcm);
            if (!row.seasonId().equals(detection.seasonId())) {
                return row;
            }
            return new SeasonConfigurationRepository.SeasonRow(
                row.seasonId(),
                detection.seasonNumber(),
                row.anchor(),
                row.managementType(),
                row.status(),
                row.fcmPath(),
                row.fcaPath(),
                row.localSitePath(),
                row.onlineSiteUrl()
            );
        } catch (Exception ignored) {
            return row;
        }
    }

    private void addSeason(){
        try {
            List<SeasonConfigurationRepository.SeasonRow> current=editors.stream().map(SeasonEditor::value).toList();
            AddSeasonWizard wizard=new AddSeasonWizard(this,repository,current,properties,configPath);
            SeasonConfigurationRepository.SeasonRow row=wizard.open();
            if(row==null)return;
            if(editors.stream().anyMatch(e->e.row.seasonId().equals(row.seasonId()))){warn("La stagione è già presente.");return;}
            List<SeasonConfigurationRepository.SeasonRow> rows = new ArrayList<>();
            for (SeasonEditor editor : editors) rows.add(editor.value());
            rows.add(row);
            repository.save(rows);
            selectSeasonByDefault(row.seasonId());

            if ("GESTITA".equals(row.managementType())) {
                importForConfiguration(row, rows);
            }

            loadSeasons();
            SeasonConfigurationRepository.SeasonRow loaded = repository.load().stream()
                .filter(r -> r.seasonId().equals(row.seasonId()))
                .findFirst().orElse(row);
            if ("GESTITA".equals(loaded.managementType()) && !loaded.anchor()) {
                openMappings();
                loadSeasons();
            }
        } catch(Exception ex) { error("Aggiunta stagione",ex); }
    }

    private void selectSeasonByDefault(String seasonId) throws IOException {
        LinkedHashSet<String> selected = Arrays.stream(properties.getProperty("seasons", "").split(","))
            .map(String::trim)
            .filter(value -> !value.isEmpty())
            .collect(Collectors.toCollection(LinkedHashSet::new));
        selected.add(seasonId);
        properties.setProperty("seasons", String.join(",", selected));
        Files.createDirectories(configPath.getParent());
        try (OutputStream out = Files.newOutputStream(configPath)) {
            properties.store(out, "RecordsNext configuration");
        }
    }

    private void importForConfiguration(
        SeasonConfigurationRepository.SeasonRow row,
        List<SeasonConfigurationRepository.SeasonRow> allRows
    ) throws Exception {
        RawSqliteImporter.main(new String[]{row.fcmPath(), "FCM", row.seasonId(), databasePath.toString()});
        RawSqliteImporter.main(new String[]{row.fcaPath(), "FCA", row.seasonId(), databasePath.toString()});
        String anchor = allRows.stream()
            .filter(r -> "GESTITA".equals(r.managementType()))
            .max(Comparator.comparingInt(r -> Integer.parseInt(r.seasonId().substring(0, 4))))
            .orElseThrow(() -> new IllegalStateException("Nessuna stagione gestita"))
            .seasonId();
        ConfigurationSchema.main(new String[]{databasePath.toString(), anchor});
    }

    private void openMappings() {
        try {
            HistoricalMappingRepository mappingRepository = new HistoricalMappingRepository(databasePath);
            HistoricalMappingDialog dialog = new HistoricalMappingDialog(this, mappingRepository);
            dialog.open();
            loadSeasons();
        } catch (Exception ex) {
            error("Associazioni storiche", ex);
        }
    }
    private void addEditor(SeasonEditor e){editors.add(e); seasonsPanel.add(e.panel); seasonsPanel.add(Box.createVerticalStrut(8));}
    private void refresh(){seasonsPanel.revalidate();seasonsPanel.repaint();}

    private void saveConfiguration(){
        if(editors.isEmpty()){warn("Aggiungere almeno una stagione.");return;}
        List<SeasonConfigurationRepository.SeasonRow> rows=new ArrayList<>();
        for(SeasonEditor e:editors){String problem=e.validateFields(); if(problem!=null){warn(problem);return;} rows.add(e.value());}
        List<String> selected=editors.stream().filter(e->e.include.isSelected()).map(e->e.row.seasonId()).toList();
        if(selected.isEmpty()){warn("Selezionare almeno una stagione da elaborare.");return;}
        try {
            HistoricalMappingRepository mappingRepository = new HistoricalMappingRepository(databasePath);
            for (SeasonConfigurationRepository.SeasonRow row : rows) {
                if (selected.contains(row.seasonId()) && "GESTITA".equals(row.managementType()) && !row.anchor()) {
                    int pending = mappingRepository.pending(row.seasonId());
                    if (pending > 0) {
                        warn(row.seasonId() + ": restano " + pending + " associazioni da configurare.");
                        return;
                    }
                }
            }
        } catch (Exception ex) {
            error("Verifica associazioni", ex);
            return;
        }
        properties.setProperty("seasons",String.join(",",selected));
        rows.stream().filter(r->"GESTITA".equals(r.managementType())).max(Comparator.comparing(r->r.seasonId())).ifPresent(current->
            properties.setProperty("siteJs",Path.of(current.localSitePath()).resolve("js").toString()));
        try{
            repository.save(rows);
            Files.createDirectories(configPath.getParent());
            try(OutputStream out=Files.newOutputStream(configPath)){properties.store(out,"RecordsNext configuration");}
            saved=true;dispose();
        }catch(Exception ex){error("Salvataggio configurazione",ex);}
    }

    private void remove(SeasonEditor e){
        int x=JOptionPane.showConfirmDialog(this,"Rimuovere "+e.row.seasonId()+" dalla configurazione?\nI dati già importati non saranno cancellati.","RecordsNext",JOptionPane.YES_NO_OPTION);
        if(x!=JOptionPane.YES_OPTION)return;
        try{repository.removeConfiguration(e.row.seasonId());}catch(Exception ex){error("Rimozione stagione",ex);return;}
        int i=editors.indexOf(e); editors.remove(e); seasonsPanel.remove(e.panel); if(i<seasonsPanel.getComponentCount()) seasonsPanel.remove(i); refresh();
    }

    private void choose(JTextField field, int mode, String extension) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(mode);
        configureExtensionFilter(chooser, extension);

        String text = field.getText().trim();
        Path directory = null;
        if (!text.isEmpty()) {
            Path path = Path.of(text);
            directory = Files.isDirectory(path) ? path : path.getParent();
        }
        String chooserKey = chooserKey(extension, mode);
        if ((directory == null || !Files.exists(directory)) && chooserKey != null) {
            String remembered = properties.getProperty(chooserKey, "").trim();
            if (!remembered.isEmpty() && Files.isDirectory(Path.of(remembered))) directory = Path.of(remembered);
        }
        if (directory != null && Files.exists(directory)) chooser.setCurrentDirectory(directory.toFile());

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            Path selected = chooser.getSelectedFile().toPath().toAbsolutePath().normalize();
            if (!hasExtension(selected, extension)) {
                warn("Selezionare un file " + extension);
                return;
            }
            field.setText(selected.toString());
            Path rememberedDirectory = mode == JFileChooser.DIRECTORIES_ONLY ? selected : selected.getParent();
            rememberChooserDirectory(extension, mode, rememberedDirectory);
            if (".fcm".equalsIgnoreCase(extension)) {
                rememberChooserDirectory(".fca", JFileChooser.FILES_ONLY, selected.getParent());
            }
        }
    }

    private static String chooserKey(String extension, int mode) {
        if (mode == JFileChooser.DIRECTORIES_ONLY) return "chooser.lastSiteDirectory";
        if (".fcm".equalsIgnoreCase(extension)) return "chooser.lastFcmDirectory";
        if (".fca".equalsIgnoreCase(extension)) return "chooser.lastFcaDirectory";
        return null;
    }

    private void rememberChooserDirectory(String extension, int mode, Path directory) {
        String key = chooserKey(extension, mode);
        if (key == null || directory == null) return;
        properties.setProperty(key, directory.toString());
        try {
            Files.createDirectories(configPath.getParent());
            try (OutputStream out = Files.newOutputStream(configPath)) { properties.store(out, "RecordsNext configuration"); }
        } catch (IOException ignored) { }
    }

    private static void configureExtensionFilter(JFileChooser chooser, String extension) {
        if (extension == null || extension.isBlank()) {
            return;
        }
        String normalized = extension.startsWith(".") ? extension.substring(1) : extension;
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter(
            "File " + normalized.toUpperCase(Locale.ROOT) + " (*." + normalized + ")",
            normalized
        ));
    }

    private static boolean hasExtension(Path path, String extension) {
        if (extension == null || extension.isBlank()) {
            return true;
        }
        return path.getFileName().toString().toLowerCase(Locale.ROOT)
            .endsWith(extension.toLowerCase(Locale.ROOT));
    }
    private void warn(String m){JOptionPane.showMessageDialog(this,m,"RecordsNext",JOptionPane.WARNING_MESSAGE);} private void error(String m,Exception e){JOptionPane.showMessageDialog(this,m+":\n"+e.getMessage(),"Errore RecordsNext",JOptionPane.ERROR_MESSAGE);}

    private final class SeasonEditor{
        final SeasonConfigurationRepository.SeasonRow row; final JPanel panel=new JPanel(new GridBagLayout());
        final JCheckBox include=new JCheckBox("Elabora");
        final JTextField fcm=new JTextField(),fca=new JTextField(),site=new JTextField(),online=new JTextField(); final JLabel js=new JLabel(),dataa=new JLabel();
        SeasonEditor(SeasonConfigurationRepository.SeasonRow row,boolean selected){this.row=row;include.setSelected(selected);fcm.setText(row.fcmPath());fca.setText(row.fcaPath());site.setText(row.localSitePath());online.setText(row.onlineSiteUrl());build();updateDerived();}
        void build(){
            panel.setAlignmentX(Component.LEFT_ALIGNMENT); panel.setMaximumSize(new Dimension(Integer.MAX_VALUE,315)); panel.setBackground(Color.WHITE); panel.setBorder(new CompoundBorder(new LineBorder(new Color(190,199,214)),new EmptyBorder(9,10,9,10)));
            GridBagConstraints g=new GridBagConstraints();g.gridy=0;g.gridx=0;g.gridwidth=2;g.anchor=GridBagConstraints.WEST;
            String current=row.anchor()?"  -  ATTUALE":"";
            JLabel title=new JLabel("Stagione "+row.seasonId()+"  (#"+row.seasonNumber()+")  -  "+row.managementType()+current);title.setFont(title.getFont().deriveFont(Font.BOLD,14f));title.setForeground(new Color(25,67,160));panel.add(title,g);
            JPanel flags=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));flags.setOpaque(false);flags.add(include);JButton remove=new JButton("Rimuovi");remove.addActionListener(e->remove(this));flags.add(remove);g.gridx=2;g.gridwidth=2;g.weightx=1;g.anchor=GridBagConstraints.EAST;panel.add(flags,g);
            if("GESTITA".equals(row.managementType())) {addPath("File FCM",fcm,1,JFileChooser.FILES_ONLY,".fcm"); addPath("File FCA",fca,2,JFileChooser.FILES_ONLY,".fca");}
            else {addReadOnly("File FCM","Stagione manuale: non previsto",1); addPath("File FCA (facoltativo)",fca,2,JFileChooser.FILES_ONLY,".fca");}
            addPath("Cartella sito locale",site,3,JFileChooser.DIRECTORIES_ONLY,null); addText("Sito online",online,4);
            site.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){public void insertUpdate(javax.swing.event.DocumentEvent e){updateDerived();}public void removeUpdate(javax.swing.event.DocumentEvent e){updateDerived();}public void changedUpdate(javax.swing.event.DocumentEvent e){updateDerived();}});
            addLabel("Cartella JS",js,5);addLabel("DataA.js",dataa,6);
            if ("GESTITA".equals(row.managementType()) && !row.anchor()) {
                JLabel mappingStatus = new JLabel();
                mappingStatus.setName("mappingStatus");
                addLabel("Associazioni", mappingStatus, 7);
                updateMappingStatus();
            } else if (row.anchor()) {
                JLabel currentStatus = new JLabel("Identità della stagione attuale");
                currentStatus.setForeground(new Color(20,120,55));
                addLabel("Associazioni", currentStatus, 7);
            }
        }

        void updateMappingStatus() {
            for (Component component : panel.getComponents()) {
                if (component instanceof JLabel label && "mappingStatus".equals(label.getName())) {
                    try {
                        int pending = new HistoricalMappingRepository(databasePath).pending(row.seasonId());
                        label.setText(pending == 0 ? "Complete" : pending + " da configurare");
                        label.setForeground(pending == 0 ? new Color(20,120,55) : new Color(170,55,35));
                    } catch (Exception ex) {
                        label.setText("Stato non disponibile");
                        label.setForeground(new Color(170,55,35));
                    }
                }
            }
        }
        void addPath(String label,JTextField field,int y,int mode,String ext){GridBagConstraints g=base(label,y);g.gridx=1;g.gridwidth=2;g.weightx=1;g.fill=GridBagConstraints.HORIZONTAL;panel.add(field,g);JButton b=new JButton("...");b.addActionListener(e->choose(field,mode,ext));g.gridx=3;g.gridwidth=1;g.weightx=0;g.fill=GridBagConstraints.NONE;panel.add(b,g);}
        void addText(String label,JTextField field,int y){GridBagConstraints g=base(label,y);g.gridx=1;g.gridwidth=3;g.weightx=1;g.fill=GridBagConstraints.HORIZONTAL;panel.add(field,g);}
        void addReadOnly(String label,String text,int y){JLabel value=new JLabel(text);value.setForeground(Color.GRAY);addLabel(label,value,y);}
        void addLabel(String label,JLabel value,int y){GridBagConstraints g=base(label,y);g.gridx=1;g.gridwidth=3;g.weightx=1;g.fill=GridBagConstraints.HORIZONTAL;panel.add(value,g);}
        GridBagConstraints base(String label,int y){GridBagConstraints g=new GridBagConstraints();g.gridy=y;g.gridx=0;g.anchor=GridBagConstraints.WEST;g.insets=new Insets(3,2,3,8);panel.add(new JLabel(label+":"),g);return g;}
        void updateDerived(){String s=site.getText().trim();if(s.isEmpty()){js.setText("-");dataa.setText("-");return;}Path j=Path.of(s).resolve("js");Path d=j.resolve("DataA.js");js.setText(j.toString());dataa.setText((Files.isRegularFile(d)?"Trovato: ":"Non trovato: ")+d);dataa.setForeground(Files.isRegularFile(d)?new Color(20,120,55):new Color(170,55,35));}
        String validateFields(){if("GESTITA".equals(row.managementType())){if(fcm.getText().trim().isEmpty()||!Files.isRegularFile(Path.of(fcm.getText().trim())))return row.seasonId()+": selezionare un file FCM esistente.";if(fca.getText().trim().isEmpty()||!Files.isRegularFile(Path.of(fca.getText().trim())))return row.seasonId()+": selezionare un file FCA esistente.";}else if(!fca.getText().trim().isEmpty()&&!Files.isRegularFile(Path.of(fca.getText().trim())))return row.seasonId()+": il file FCA indicato non esiste.";if(site.getText().trim().isEmpty()||!Files.isDirectory(Path.of(site.getText().trim())))return row.seasonId()+": selezionare una cartella sito esistente.";return null;}
        SeasonConfigurationRepository.SeasonRow value(){return new SeasonConfigurationRepository.SeasonRow(row.seasonId(),row.seasonNumber(),row.anchor(),row.managementType(),row.status(),"GESTITA".equals(row.managementType())?fcm.getText().trim():"",fca.getText().trim(),site.getText().trim(),online.getText().trim());}
    }

    private static final class AddSeasonWizard extends JDialog {
        private final JRadioButton managed = new JRadioButton("Gestita", true);
        private final JRadioButton manual = new JRadioButton("Manuale");
        private final JTextField fcm = new JTextField();
        private final JTextField fca = new JTextField();
        private final JTextField manualSeason = new JTextField();
        private final JTextField manualNumber = new JTextField();
        private final JLabel detected = new JLabel(" ");
        private final SeasonConfigurationRepository repo;
        private final List<SeasonConfigurationRepository.SeasonRow> current;
        private final Properties properties;
        private final Path configPath;
        private SeasonConfigurationRepository.SeasonRow result;

        AddSeasonWizard(
            Window owner,
            SeasonConfigurationRepository repo,
            List<SeasonConfigurationRepository.SeasonRow> current,
            Properties properties,
            Path configPath
        ) {
            super(owner, "RecordsNext - Aggiungi stagione", ModalityType.APPLICATION_MODAL);
            this.repo = repo;
            this.current = current;
            this.properties = properties;
            this.configPath = configPath;
            build();
        }

        SeasonConfigurationRepository.SeasonRow open() {
            setVisible(true);
            return result;
        }

        private void build() {
            setLayout(new BorderLayout(10, 10));
            ((JComponent) getContentPane()).setBorder(new EmptyBorder(14, 16, 12, 16));

            ButtonGroup group = new ButtonGroup();
            group.add(managed);
            group.add(manual);

            JPanel form = new JPanel(new GridBagLayout());
            form.setBorder(new TitledBorder("Tipo e sorgenti della stagione"));

            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(5, 5, 5, 5);
            g.anchor = GridBagConstraints.WEST;
            g.gridx = 0;
            g.gridy = 0;
            form.add(managed, g);
            g.gridx = 1;
            form.add(manual, g);

            addChooser(form, "File FCM", fcm, 1, ".fcm");
            addChooser(form, "File FCA", fca, 2, ".fca");
            addField(form, "Stagione manuale (AAAA_AAAA)", manualSeason, 3);
            addField(form, "Numero stagione", manualNumber, 4);
            addValue(form, "Dati rilevati", detected, 5);

            managed.addActionListener(e -> updateMode());
            manual.addActionListener(e -> updateMode());
            updateMode();

            add(form, BorderLayout.CENTER);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton add = new JButton("Aggiungi stagione");
            JButton cancel = new JButton("Annulla");
            add.addActionListener(e -> finish());
            cancel.addActionListener(e -> dispose());
            buttons.add(add);
            buttons.add(cancel);
            add(buttons, BorderLayout.SOUTH);

            setSize(720, 390);
            setLocationRelativeTo(getOwner());
        }

        private void updateMode() {
            boolean isManaged = managed.isSelected();
            fcm.setEnabled(isManaged);
            manualSeason.setEnabled(!isManaged);
            manualNumber.setEnabled(!isManaged);
            detected.setText(isManaged
                ? "Stagione e numero saranno letti dal file FCM."
                : "Inserire stagione e numero manualmente.");
        }

        private void finish() {
            try {
                String seasonId;
                int seasonNumber;
                String type;
                String fcmPath = "";

                if (managed.isSelected()) {
                    if (!file(fcm, ".fcm") || !file(fca, ".fca")) return;
                    var detection = new FcmSeasonDetector().detect(Path.of(fcm.getText().trim()));
                    seasonId = detection.seasonId();
                    seasonNumber = detection.seasonNumber();
                    type = "GESTITA";
                    fcmPath = fcm.getText().trim();
                    detected.setText(
                        seasonId + " (#" + seasonNumber + ") - " + detection.evidence()
                    );
                } else {
                    seasonId = manualSeason.getText().trim();
                    if (!SEASON.matcher(seasonId).matches()) {
                        warn("Formato stagione non valido.");
                        return;
                    }
                    try {
                        seasonNumber = Integer.parseInt(manualNumber.getText().trim());
                    } catch (NumberFormatException ex) {
                        warn("Indicare un numero stagione valido.");
                        return;
                    }
                    if (seasonNumber < 1) {
                        warn("Il numero stagione deve essere positivo.");
                        return;
                    }
                    if (!fca.getText().trim().isEmpty()
                        && !Files.isRegularFile(Path.of(fca.getText().trim()))) {
                        warn("Il file FCA indicato non esiste.");
                        return;
                    }
                    type = "MANUALE";
                }

                if (current.stream().anyMatch(r -> r.seasonId().equals(seasonId))) {
                    warn("La stagione " + seasonId + " è già presente.");
                    return;
                }

                result = new SeasonConfigurationRepository.SeasonRow(
                    seasonId,
                    seasonNumber,
                    false,
                    type,
                    "DA_CONFIGURARE",
                    fcmPath,
                    fca.getText().trim(),
                    "",
                    ""
                );
                dispose();
            } catch (Exception ex) {
                warn(ex.getMessage());
            }
        }

        private boolean file(JTextField field, String extension) {
            String value = field.getText().trim();
            if (value.isEmpty() || !Files.isRegularFile(Path.of(value))) {
                warn("Selezionare un file " + extension + " esistente.");
                return false;
            }
            return true;
        }

        private void addChooser(
            JPanel panel,
            String label,
            JTextField field,
            int row,
            String extension
        ) {
            addField(panel, label, field, row);
            JButton button = new JButton("...");
            button.addActionListener(e -> {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                configureExtensionFilter(chooser, extension);
                String current = field.getText().trim();
                Path directory = null;
                if (!current.isEmpty()) {
                    Path path = Path.of(current);
                    directory = Files.isDirectory(path) ? path : path.getParent();
                }
                String key = chooserKey(extension, JFileChooser.FILES_ONLY);
                if ((directory == null || !Files.exists(directory)) && key != null) {
                    String remembered = properties.getProperty(key, "").trim();
                    if (!remembered.isEmpty() && Files.isDirectory(Path.of(remembered))) directory = Path.of(remembered);
                }
                if (directory != null && Files.exists(directory)) chooser.setCurrentDirectory(directory.toFile());
                if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    Path selected = chooser.getSelectedFile().toPath().toAbsolutePath().normalize();
                    if (!hasExtension(selected, extension)) {
                        warn("Selezionare un file " + extension);
                        return;
                    }
                    field.setText(selected.toString());
                    if (key != null && selected.getParent() != null) {
                        properties.setProperty(key, selected.getParent().toString());
                        if (".fcm".equalsIgnoreCase(extension)) {
                            properties.setProperty("chooser.lastFcaDirectory", selected.getParent().toString());
                        }
                        try {
                            Files.createDirectories(configPath.getParent());
                            try (OutputStream out = Files.newOutputStream(configPath)) {
                                properties.store(out, "RecordsNext configuration");
                            }
                        } catch (IOException ignored) { }
                    }
                }
            });
            GridBagConstraints g = new GridBagConstraints();
            g.gridx = 2;
            g.gridy = row;
            g.insets = new Insets(5, 5, 5, 5);
            panel.add(button, g);
        }

        private void addField(JPanel panel, String label, JTextField field, int row) {
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(5, 5, 5, 5);
            g.anchor = GridBagConstraints.WEST;
            g.gridx = 0;
            g.gridy = row;
            panel.add(new JLabel(label + ":"), g);
            g.gridx = 1;
            g.weightx = 1;
            g.fill = GridBagConstraints.HORIZONTAL;
            panel.add(field, g);
        }

        private void addValue(JPanel panel, String label, JLabel value, int row) {
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(5, 5, 5, 5);
            g.anchor = GridBagConstraints.WEST;
            g.gridx = 0;
            g.gridy = row;
            panel.add(new JLabel(label + ":"), g);
            g.gridx = 1;
            g.gridwidth = 2;
            g.weightx = 1;
            g.fill = GridBagConstraints.HORIZONTAL;
            panel.add(value, g);
        }

        private void warn(String message) {
            JOptionPane.showMessageDialog(
                this,
                message,
                "RecordsNext",
                JOptionPane.WARNING_MESSAGE
            );
        }
    }

}
```

### src\main\java\it\alterlega\recordsnext\gui\SeasonConfigurationRepository.java

```java
package it.alterlega.recordsnext.gui;

import java.nio.file.Path;
import java.sql.*;
import java.time.Instant;
import java.util.*;

final class SeasonConfigurationRepository {
    record SeasonRow(String seasonId, int seasonNumber, boolean anchor,
                     String managementType, String status,
                     String fcmPath, String fcaPath,
                     String localSitePath, String onlineSiteUrl) {}

    private final Path database;

    SeasonConfigurationRepository(Path database) {
        this.database = database.toAbsolutePath().normalize();
    }

    List<SeasonRow> load() throws Exception {
        Class.forName("org.sqlite.JDBC");
        try (Connection c = open()) {
            ensureSchema(c);
            String sql = """
                SELECT s.season_id, COALESCE(s.sort_order,0), s.is_anchor,
                       COALESCE(c.management_type,'GESTITA') management_type,
                       COALESCE(c.configuration_status,'DA_CONFIGURARE') configuration_status,
                       COALESCE(c.configured_fcm_path,
                         MAX(CASE WHEN f.source_type='FCM' THEN f.source_path END),'') fcm_path,
                       COALESCE(c.configured_fca_path,
                         MAX(CASE WHEN f.source_type='FCA' THEN f.source_path END),'') fca_path,
                       COALESCE(c.local_site_path,'') local_site_path,
                       COALESCE(c.online_site_url,'') online_site_url
                FROM rn_season s
                LEFT JOIN rn_season_configuration c ON c.season_id=s.season_id
                LEFT JOIN rn_source_file f ON f.season_id=s.season_id
                GROUP BY s.season_id,s.sort_order,s.is_anchor,c.management_type,
                         c.configuration_status,c.configured_fcm_path,
                         c.configured_fca_path,c.local_site_path,c.online_site_url
                ORDER BY CAST(SUBSTR(s.season_id,1,4) AS INTEGER) DESC, s.season_id DESC
                """;
            List<SeasonRow> out = new ArrayList<>();
            try (Statement st=c.createStatement(); ResultSet r=st.executeQuery(sql)) {
                while (r.next()) out.add(new SeasonRow(
                    r.getString(1), r.getInt(2), r.getInt(3)==1,
                    r.getString(4), r.getString(5), r.getString(6),
                    r.getString(7), r.getString(8), r.getString(9)));
            }
            assignMissingNumbers(out);
            return out;
        }
    }

    int suggestedSeasonNumber(String seasonId, Collection<SeasonRow> current) {
        List<String> ids = new ArrayList<>();
        for (SeasonRow row : current) ids.add(row.seasonId());
        if (!ids.contains(seasonId)) ids.add(seasonId);
        ids.sort(Comparator.comparingInt(SeasonConfigurationRepository::startYear));
        return ids.indexOf(seasonId) + 1;
    }

    void save(List<SeasonRow> rows) throws Exception {
        Class.forName("org.sqlite.JDBC");
        try (Connection c=open()) {
            ensureSchema(c); c.setAutoCommit(false);
            try {
                String anchorSeason = rows.stream()
                    .filter(r -> "GESTITA".equals(r.managementType()))
                    .max(Comparator.comparingInt(r -> startYear(r.seasonId())))
                    .map(SeasonRow::seasonId).orElse(null);
                String now= Instant.now().toString();
                try (Statement st=c.createStatement()) { st.executeUpdate("UPDATE rn_season SET is_anchor=0"); }
                for (SeasonRow row: rows) {
                    boolean anchor = Objects.equals(row.seasonId(), anchorSeason);
                    try (PreparedStatement p=c.prepareStatement("""
                        INSERT INTO rn_season(season_id,display_name,sort_order,is_anchor,created_at,updated_at)
                        VALUES(?,?,?,?,?,?)
                        ON CONFLICT(season_id) DO UPDATE SET display_name=excluded.display_name,
                          sort_order=excluded.sort_order,is_anchor=excluded.is_anchor,
                          updated_at=excluded.updated_at
                        """)) {
                        p.setString(1,row.seasonId()); p.setString(2,row.seasonId());
                        p.setInt(3,row.seasonNumber()); p.setInt(4,anchor?1:0);
                        p.setString(5,now); p.setString(6,now); p.executeUpdate();
                    }
                    try (PreparedStatement p=c.prepareStatement("""
                        INSERT INTO rn_season_configuration(
                          season_id,management_type,local_site_path,online_site_url,dataa_path,
                          configuration_status,created_at,updated_at,configured_fcm_path,configured_fca_path)
                        VALUES(?,?,?,?,NULL,?,?,?,?,?)
                        ON CONFLICT(season_id) DO UPDATE SET management_type=excluded.management_type,
                          local_site_path=excluded.local_site_path,online_site_url=excluded.online_site_url,
                          configuration_status=excluded.configuration_status,updated_at=excluded.updated_at,
                          configured_fcm_path=excluded.configured_fcm_path,
                          configured_fca_path=excluded.configured_fca_path
                        """)) {
                        p.setString(1,row.seasonId()); p.setString(2,row.managementType());
                        nullable(p,3,row.localSitePath()); nullable(p,4,row.onlineSiteUrl());
                        p.setString(5,status(row)); p.setString(6,now); p.setString(7,now);
                        nullable(p,8,row.fcmPath()); nullable(p,9,row.fcaPath()); p.executeUpdate();
                    }
                }
                c.commit();
            } catch(Exception ex) { c.rollback(); throw ex; }
        }
    }

    void removeConfiguration(String seasonId) throws Exception {
        try (Connection c=open()) {
            ensureSchema(c);
            try (PreparedStatement p=c.prepareStatement("DELETE FROM rn_season_configuration WHERE season_id=?")) {
                p.setString(1,seasonId); p.executeUpdate();
            }
            try (PreparedStatement p=c.prepareStatement("""
                DELETE FROM rn_season WHERE season_id=?
                  AND NOT EXISTS(SELECT 1 FROM rn_source_file WHERE season_id=?)
                """)) {
                p.setString(1,seasonId); p.setString(2,seasonId); p.executeUpdate();
            }
        }
    }

    private Connection open() throws Exception {
        Connection c=DriverManager.getConnection("jdbc:sqlite:"+database);
        try(Statement s=c.createStatement()) { s.execute("PRAGMA foreign_keys=ON"); s.execute("PRAGMA busy_timeout=10000"); }
        return c;
    }

    private static void ensureSchema(Connection c) throws Exception {
        try(Statement s=c.createStatement()) {
            s.execute("""
                CREATE TABLE IF NOT EXISTS rn_season_configuration(
                  season_id TEXT PRIMARY KEY, management_type TEXT NOT NULL,
                  local_site_path TEXT, online_site_url TEXT, dataa_path TEXT,
                  configuration_status TEXT NOT NULL DEFAULT 'DA_CONFIGURARE',
                  created_at TEXT NOT NULL, updated_at TEXT NOT NULL,
                  configured_fcm_path TEXT, configured_fca_path TEXT,
                  FOREIGN KEY(season_id) REFERENCES rn_season(season_id))
                """);
        }
        addColumnIfMissing(c,"configured_fcm_path","TEXT");
        addColumnIfMissing(c,"configured_fca_path","TEXT");
    }

    private static void addColumnIfMissing(Connection c,String name,String type) throws Exception {
        boolean found=false;
        try(Statement s=c.createStatement(); ResultSet r=s.executeQuery("PRAGMA table_info(rn_season_configuration)")) {
            while(r.next()) if(name.equalsIgnoreCase(r.getString("name"))) found=true;
        }
        if(!found) try(Statement s=c.createStatement()) { s.execute("ALTER TABLE rn_season_configuration ADD COLUMN "+name+" "+type); }
    }

    private static String status(SeasonRow r) {
        if ("MANUALE".equals(r.managementType())) {
            return r.localSitePath().isBlank() ? "DA_CONFIGURARE" : "COMPLETA";
        }
        return !r.fcmPath().isBlank() && !r.fcaPath().isBlank() && !r.localSitePath().isBlank()
                ? "COMPLETA" : "DA_CONFIGURARE";
    }

    private static void nullable(PreparedStatement p,int i,String value) throws Exception {
        String v=value==null?"":value.trim(); if(v.isEmpty()) p.setNull(i,Types.VARCHAR); else p.setString(i,v);
    }

    private static int startYear(String seasonId) {
        try { return Integer.parseInt(seasonId.substring(0,4)); }
        catch (Exception ex) { return Integer.MIN_VALUE; }
    }

    private static void assignMissingNumbers(List<SeasonRow> rows) {
        List<SeasonRow> chronological = new ArrayList<>(rows);
        chronological.sort(Comparator.comparingInt(r -> startYear(r.seasonId())));
        Map<String,Integer> numbers = new HashMap<>();
        int next=1;
        for (SeasonRow row : chronological) {
            int n=row.seasonNumber()>0?row.seasonNumber():next;
            numbers.put(row.seasonId(),n); next=Math.max(next,n+1);
        }
        for (int i=0;i<rows.size();i++) {
            SeasonRow r=rows.get(i);
            if (r.seasonNumber()<=0) rows.set(i,new SeasonRow(r.seasonId(),numbers.get(r.seasonId()),r.anchor(),r.managementType(),r.status(),r.fcmPath(),r.fcaPath(),r.localSitePath(),r.onlineSiteUrl()));
        }
    }
}
```

### src\main\java\it\alterlega\recordsnext\PlayoffRecordsExporter.java

```java
package it.alterlega.recordsnext;

import java.io.BufferedWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class PlayoffRecordsExporter {

    private PlayoffRecordsExporter() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.err.println(
                "Uso: PlayoffRecordsExporter "
                    + "<recordsnext.db> <stagione> <output.json>"
            );
            System.exit(2);
        }

        Path database = Path.of(args[0])
            .toAbsolutePath()
            .normalize();

        String seasonId = args[1].trim();

        Path output = Path.of(args[2])
            .toAbsolutePath()
            .normalize();

        if (seasonId.isBlank()) {
            throw new IllegalArgumentException(
                "La stagione non può essere vuota."
            );
        }

        if (output.getParent() != null) {
            Files.createDirectories(output.getParent());
        }

        Class.forName("org.sqlite.JDBC");

        long started = System.nanoTime();

        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + database)) {

            ensureViewExists(connection);

            List<TeamSummary> summaries = readSummaries(
                connection,
                seasonId
            );

            List<PlayoffDetail> wins = readDetails(
                connection,
                seasonId,
                "W"
            );

            List<PlayoffDetail> losses = readDetails(
                connection,
                seasonId,
                "L"
            );

            writeJson(
                output,
                new ExportData(
                    new Meta(
                        Instant.now().toString(),
                        seasonId,
                        summaries.size(),
                        wins.size(),
                        losses.size()
                    ),
                    summaries,
                    wins,
                    losses
                )
            );

            long finished = System.nanoTime();

            System.out.println("Record play off / play out esportati");
            System.out.println("Stagione       : " + seasonId);
            System.out.println("Squadre        : " + summaries.size());
            System.out.println("Play off vinti : " + wins.size());
            System.out.println("Play off persi : " + losses.size());
            System.out.println("Output         : " + output);

            System.out.printf(
                Locale.ROOT,
                "Tempo          : %.3f ms%n",
                (finished - started) / 1_000_000.0
            );
        }
    }

    private static void ensureViewExists(
            Connection connection) throws Exception {

        String sql = """
            SELECT COUNT(*)
            FROM sqlite_master
            WHERE type = 'view'
              AND name = 'rn_playoff_result'
            """;

        try (
            PreparedStatement statement =
                connection.prepareStatement(sql);
            ResultSet result = statement.executeQuery()
        ) {
            result.next();

            if (result.getInt(1) != 1) {
                throw new IllegalStateException(
                    "Vista rn_playoff_result non trovata. "
                        + "Eseguire prima CanonicalViews."
                );
            }
        }
    }

    private static List<TeamSummary> readSummaries(
            Connection connection,
            String seasonId) throws Exception {

        String sql = """
            SELECT
                source_team_id,
                team_identity_id,
                team_name,
                SUM(CASE WHEN result = 'W' THEN 1 ELSE 0 END)
                    AS playoff_wins,
                SUM(CASE WHEN result = 'L' THEN 1 ELSE 0 END)
                    AS playoff_losses
            FROM rn_playoff_result
            WHERE season_id = ?
            GROUP BY
                source_team_id,
                team_identity_id,
                team_name
            ORDER BY
                playoff_wins DESC,
                playoff_losses ASC,
                team_name COLLATE NOCASE
            """;

        List<TeamSummary> rows = new ArrayList<>();

        try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setString(1, seasonId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    rows.add(
                        new TeamSummary(
                            result.getInt("source_team_id"),
                            result.getLong("team_identity_id"),
                            result.getString("team_name"),
                            result.getInt("playoff_wins"),
                            result.getInt("playoff_losses")
                        )
                    );
                }
            }
        }

        return rows;
    }

    private static List<PlayoffDetail> readDetails(
            Connection connection,
            String seasonId,
            String resultCode) throws Exception {

        String sql = """
            SELECT
                season_id,
                competition_name,
                source_competition_id,
                source_group_id,
                source_group_name,
                source_round_id,
                round_description,
                serie_a_round,
                source_event_id,
                source_team_id,
                team_identity_id,
                team_name,
                opponent_source_event_id,
                opponent_source_team_id,
                opponent_team_identity_id,
                opponent_name,
                score_for,
                score_against,
                result
            FROM rn_playoff_result
            WHERE season_id = ?
              AND result = ?
            ORDER BY
                serie_a_round,
                source_group_id,
                source_round_id,
                source_event_id
            """;

        List<PlayoffDetail> rows = new ArrayList<>();

        try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setString(1, seasonId);
            statement.setString(2, resultCode);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    rows.add(
                        new PlayoffDetail(
                            result.getString("season_id"),
                            result.getString("competition_name"),
                            result.getInt("source_competition_id"),
                            result.getInt("source_group_id"),
                            result.getString("source_group_name"),
                            result.getInt("source_round_id"),
                            result.getString("round_description"),
                            result.getInt("serie_a_round"),
                            result.getLong("source_event_id"),
                            result.getInt("source_team_id"),
                            result.getLong("team_identity_id"),
                            result.getString("team_name"),
                            result.getLong("opponent_source_event_id"),
                            result.getInt("opponent_source_team_id"),
                            result.getLong("opponent_team_identity_id"),
                            result.getString("opponent_name"),
                            result.getBigDecimal("score_for"),
                            result.getBigDecimal("score_against"),
                            result.getString("result")
                        )
                    );
                }
            }
        }

        return rows;
    }

    private static void writeJson(
            Path output,
            ExportData data) throws Exception {

        try (BufferedWriter writer = Files.newBufferedWriter(
                output,
                StandardCharsets.UTF_8)) {

            writer.write("{\n");

            writeMeta(writer, data.meta());
            writer.write(",\n");

            writeSummaries(writer, data.summaries());
            writer.write(",\n");

            writeDetails(
                writer,
                "playOffVinti",
                data.wins()
            );
            writer.write(",\n");

            writeDetails(
                writer,
                "playOffPersi",
                data.losses()
            );

            writer.write("\n}\n");
        }
    }

    private static void writeMeta(
            BufferedWriter writer,
            Meta meta) throws Exception {

        writer.write("  \"meta\": {\n");
        writeStringProperty(
            writer,
            "generatedAt",
            meta.generatedAt(),
            true,
            4
        );
        writeStringProperty(
            writer,
            "stagione",
            meta.seasonId(),
            true,
            4
        );
        writeNumberProperty(
            writer,
            "squadreCoinvolte",
            meta.teams(),
            true,
            4
        );
        writeNumberProperty(
            writer,
            "playOffVinti",
            meta.wins(),
            true,
            4
        );
        writeNumberProperty(
            writer,
            "playOffPersi",
            meta.losses(),
            false,
            4
        );
        writer.write("  }");
    }

    private static void writeSummaries(
            BufferedWriter writer,
            List<TeamSummary> rows) throws Exception {

        writer.write("  \"riepilogoSquadre\": [\n");

        for (int index = 0; index < rows.size(); index++) {
            TeamSummary row = rows.get(index);

            writer.write("    {\n");
            writeStringProperty(
                writer,
                "idSquadra",
                Integer.toString(row.sourceTeamId()),
                true,
                6
            );
            writeStringProperty(
                writer,
                "idIdentitaSquadra",
                Long.toString(row.teamIdentityId()),
                true,
                6
            );
            writeStringProperty(
                writer,
                "squadra",
                row.teamName(),
                true,
                6
            );
            writeNumberProperty(
                writer,
                "playOffVinti",
                row.wins(),
                true,
                6
            );
            writeNumberProperty(
                writer,
                "playOffPersi",
                row.losses(),
                false,
                6
            );
            writer.write("    }");

            if (index + 1 < rows.size()) {
                writer.write(",");
            }

            writer.write("\n");
        }

        writer.write("  ]");
    }

    private static void writeDetails(
            BufferedWriter writer,
            String propertyName,
            List<PlayoffDetail> rows) throws Exception {

        writer.write("  \"");
        writer.write(jsonEscape(propertyName));
        writer.write("\": [\n");

        for (int index = 0; index < rows.size(); index++) {
            PlayoffDetail row = rows.get(index);

            writer.write("    {\n");

            writeStringProperty(
                writer,
                "stagione",
                row.seasonId(),
                true,
                6
            );
            writeStringProperty(
                writer,
                "competizione",
                row.competitionName(),
                true,
                6
            );
            writeStringProperty(
                writer,
                "idCompetizioneFcm",
                Integer.toString(row.sourceCompetitionId()),
                true,
                6
            );
            writeStringProperty(
                writer,
                "idGirone",
                Integer.toString(row.sourceGroupId()),
                true,
                6
            );
            writeStringProperty(
                writer,
                "girone",
                row.sourceGroupName(),
                true,
                6
            );
            writeStringProperty(
                writer,
                "idGiornata",
                Integer.toString(row.sourceRoundId()),
                true,
                6
            );
            writeStringProperty(
                writer,
                "giornata",
                row.roundDescription(),
                true,
                6
            );
            writeNumberProperty(
                writer,
                "giornataDiA",
                row.serieARound(),
                true,
                6
            );
            writeStringProperty(
                writer,
                "idEvento",
                Long.toString(row.sourceEventId()),
                true,
                6
            );
            writeStringProperty(
                writer,
                "idSquadra",
                Integer.toString(row.sourceTeamId()),
                true,
                6
            );
            writeStringProperty(
                writer,
                "idIdentitaSquadra",
                Long.toString(row.teamIdentityId()),
                true,
                6
            );
            writeStringProperty(
                writer,
                "squadra",
                row.teamName(),
                true,
                6
            );
            writeStringProperty(
                writer,
                "idEventoAvversaria",
                Long.toString(row.opponentSourceEventId()),
                true,
                6
            );
            writeStringProperty(
                writer,
                "idAvversaria",
                Integer.toString(row.opponentSourceTeamId()),
                true,
                6
            );
            writeStringProperty(
                writer,
                "idIdentitaAvversaria",
                Long.toString(row.opponentTeamIdentityId()),
                true,
                6
            );
            writeStringProperty(
                writer,
                "avversaria",
                row.opponentName(),
                true,
                6
            );
            writeDecimalProperty(
                writer,
                "puntiFatti",
                row.scoreFor(),
                true,
                6
            );
            writeDecimalProperty(
                writer,
                "puntiSubiti",
                row.scoreAgainst(),
                true,
                6
            );
            writeStringProperty(
                writer,
                "esito",
                row.result(),
                false,
                6
            );

            writer.write("    }");

            if (index + 1 < rows.size()) {
                writer.write(",");
            }

            writer.write("\n");
        }

        writer.write("  ]");
    }

    private static void writeStringProperty(
            BufferedWriter writer,
            String name,
            String value,
            boolean comma,
            int indent) throws Exception {

        writer.write(" ".repeat(indent));
        writer.write("\"");
        writer.write(jsonEscape(name));
        writer.write("\": ");

        if (value == null) {
            writer.write("null");
        } else {
            writer.write("\"");
            writer.write(jsonEscape(value));
            writer.write("\"");
        }

        if (comma) {
            writer.write(",");
        }

        writer.write("\n");
    }

    private static void writeNumberProperty(
            BufferedWriter writer,
            String name,
            long value,
            boolean comma,
            int indent) throws Exception {

        writer.write(" ".repeat(indent));
        writer.write("\"");
        writer.write(jsonEscape(name));
        writer.write("\": ");
        writer.write(Long.toString(value));

        if (comma) {
            writer.write(",");
        }

        writer.write("\n");
    }

    private static void writeDecimalProperty(
            BufferedWriter writer,
            String name,
            BigDecimal value,
            boolean comma,
            int indent) throws Exception {

        writer.write(" ".repeat(indent));
        writer.write("\"");
        writer.write(jsonEscape(name));
        writer.write("\": ");

        if (value == null) {
            writer.write("null");
        } else {
            writer.write(
                value.stripTrailingZeros().toPlainString()
            );
        }

        if (comma) {
            writer.write(",");
        }

        writer.write("\n");
    }

    private static String jsonEscape(String value) {
        StringBuilder escaped = new StringBuilder();

        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);

            switch (current) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");

                default -> {
                    if (current < 0x20) {
                        escaped.append(
                            String.format(
                                Locale.ROOT,
                                "\\u%04x",
                                (int) current
                            )
                        );
                    } else {
                        escaped.append(current);
                    }
                }
            }
        }

        return escaped.toString();
    }

    private record Meta(
        String generatedAt,
        String seasonId,
        int teams,
        int wins,
        int losses
    ) {
    }

    private record TeamSummary(
        int sourceTeamId,
        long teamIdentityId,
        String teamName,
        int wins,
        int losses
    ) {
    }

    private record PlayoffDetail(
        String seasonId,
        String competitionName,
        int sourceCompetitionId,
        int sourceGroupId,
        String sourceGroupName,
        int sourceRoundId,
        String roundDescription,
        int serieARound,
        long sourceEventId,
        int sourceTeamId,
        long teamIdentityId,
        String teamName,
        long opponentSourceEventId,
        int opponentSourceTeamId,
        long opponentTeamIdentityId,
        String opponentName,
        BigDecimal scoreFor,
        BigDecimal scoreAgainst,
        String result
    ) {
    }

    private record ExportData(
        Meta meta,
        List<TeamSummary> summaries,
        List<PlayoffDetail> wins,
        List<PlayoffDetail> losses
    ) {
    }
}
```

### src\main\java\it\alterlega\recordsnext\RawSqliteImporter.java

```java
package it.alterlega.recordsnext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

public final class RawSqliteImporter {

    private static final int BATCH_SIZE = 1000;

    private RawSqliteImporter() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            System.err.println(
                "Uso: RawSqliteImporter "
                    + "<file.fcm|file.fca> <FCM|FCA> <stagione> <output.db>"
            );
            System.exit(2);
        }

        Path source = Path.of(args[0]).toAbsolutePath().normalize();
        String sourceType = args[1].trim().toUpperCase(Locale.ROOT);
        String seasonId = args[2].trim();
        Path sqliteFile = Path.of(args[3]).toAbsolutePath().normalize();

        if (!Files.isRegularFile(source)) {
            throw new IllegalArgumentException(
                "File sorgente non trovato: " + source
            );
        }

        if (!sourceType.equals("FCM") && !sourceType.equals("FCA")) {
            throw new IllegalArgumentException(
                "Tipo sorgente non valido: " + sourceType
            );
        }

        if (seasonId.isBlank()) {
            throw new IllegalArgumentException("Stagione non specificata.");
        }

        if (sqliteFile.getParent() != null) {
            Files.createDirectories(sqliteFile.getParent());
        }

        Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        Class.forName("org.sqlite.JDBC");

        String accessUrl = "jdbc:ucanaccess://" + source;
        String sqliteUrl = "jdbc:sqlite:" + sqliteFile;

        long totalStarted = System.nanoTime();

        try (
            Connection access = DriverManager.getConnection(accessUrl);
            Connection sqlite = DriverManager.getConnection(sqliteUrl)
        ) {
access.setReadOnly(true);

configureSqlite(sqlite);
sqlite.setAutoCommit(false);

createMetadataTables(sqlite);

            long importId = registerImport(
                sqlite,
                source,
                sourceType,
                seasonId
            );

            DatabaseMetaData metadata = access.getMetaData();
            List<String> tableNames = readTableNames(metadata);

            long importedRows = 0;
            long importedColumns = 0;

            for (String tableName : tableNames) {
                TableImportResult result = importTable(
                    access,
                    sqlite,
                    metadata,
                    importId,
                    sourceType,
                    seasonId,
                    tableName
                );

                importedRows += result.rows();
                importedColumns += result.columns();

                System.out.printf(
                    Locale.ROOT,
                    "%-40s colonne=%4d righe=%8d%n",
                    tableName,
                    result.columns(),
                    result.rows()
                );
            }

            finishImport(
                sqlite,
                importId,
                tableNames.size(),
                importedColumns,
                importedRows
            );

            sqlite.commit();

            long totalFinished = System.nanoTime();

            System.out.println();
            System.out.println("Importazione raw completata");
            System.out.println("Sorgente : " + source);
            System.out.println("Tipo     : " + sourceType);
            System.out.println("Stagione : " + seasonId);
            System.out.println("SQLite   : " + sqliteFile);
            System.out.println("Tabelle  : " + tableNames.size());
            System.out.println("Colonne  : " + importedColumns);
            System.out.println("Righe    : " + importedRows);
            System.out.printf(
                Locale.ROOT,
                "Totale   : %.3f s%n",
                (totalFinished - totalStarted) / 1_000_000_000.0
            );
        }
    }

    private static void configureSqlite(Connection sqlite) throws Exception {
        try (Statement statement = sqlite.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("PRAGMA journal_mode = WAL");
            statement.execute("PRAGMA synchronous = NORMAL");
            statement.execute("PRAGMA temp_store = MEMORY");
        }
    }

    private static void createMetadataTables(Connection sqlite)
            throws Exception {

        try (Statement statement = sqlite.createStatement()) {
            statement.execute("""
                CREATE TABLE IF NOT EXISTS rn_import (
                    import_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    season_id TEXT NOT NULL,
                    source_type TEXT NOT NULL,
                    source_path TEXT NOT NULL,
                    source_file_name TEXT NOT NULL,
                    source_size_bytes INTEGER NOT NULL,
                    source_last_modified TEXT NOT NULL,
                    source_sha256 TEXT NOT NULL,
                    started_at TEXT NOT NULL,
                    completed_at TEXT,
                    table_count INTEGER,
                    column_count INTEGER,
                    row_count INTEGER,
                    status TEXT NOT NULL
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS rn_table_catalog (
                    import_id INTEGER NOT NULL,
                    season_id TEXT NOT NULL,
                    source_type TEXT NOT NULL,
                    source_table_name TEXT NOT NULL,
                    raw_table_name TEXT NOT NULL,
                    source_row_count INTEGER NOT NULL,
                    imported_row_count INTEGER NOT NULL,
                    column_count INTEGER NOT NULL,
                    audit_ok INTEGER NOT NULL,
                    PRIMARY KEY (
                        import_id,
                        source_table_name
                    )
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS rn_column_catalog (
                    import_id INTEGER NOT NULL,
                    source_table_name TEXT NOT NULL,
                    column_name TEXT NOT NULL,
                    ordinal_position INTEGER NOT NULL,
                    jdbc_type INTEGER NOT NULL,
                    type_name TEXT,
                    column_size INTEGER,
                    decimal_digits INTEGER,
                    nullable_code INTEGER,
                    default_value TEXT,
                    PRIMARY KEY (
                        import_id,
                        source_table_name,
                        column_name
                    )
                )
                """);
        }
    }

    private static long registerImport(
            Connection sqlite,
            Path source,
            String sourceType,
            String seasonId) throws Exception {

        String sql = """
            INSERT INTO rn_import (
                season_id,
                source_type,
                source_path,
                source_file_name,
                source_size_bytes,
                source_last_modified,
                source_sha256,
                started_at,
                status
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (
            PreparedStatement statement = sqlite.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS
            )
        ) {
            statement.setString(1, seasonId);
            statement.setString(2, sourceType);
            statement.setString(3, source.toString());
            statement.setString(4, source.getFileName().toString());
            statement.setLong(5, Files.size(source));
            statement.setString(
                6,
                Files.getLastModifiedTime(source).toInstant().toString()
            );
            statement.setString(7, sha256(source));
            statement.setString(8, Instant.now().toString());
            statement.setString(9, "RUNNING");
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new IllegalStateException(
                        "Impossibile ottenere import_id."
                    );
                }

                return keys.getLong(1);
            }
        }
    }

    private static List<String> readTableNames(DatabaseMetaData metadata)
            throws Exception {

        List<String> tables = new ArrayList<>();

        try (
            ResultSet result = metadata.getTables(
                null,
                null,
                "%",
                new String[]{"TABLE"}
            )
        ) {
            while (result.next()) {
                String name = result.getString("TABLE_NAME");

                if (name != null && !name.isBlank()) {
                    tables.add(name);
                }
            }
        }

        tables.sort(String.CASE_INSENSITIVE_ORDER);
        return tables;
    }

    private static TableImportResult importTable(
            Connection access,
            Connection sqlite,
            DatabaseMetaData metadata,
            long importId,
            String sourceType,
            String seasonId,
            String sourceTableName) throws Exception {

        String rawTableName = rawTableName(
            sourceType,
            seasonId,
            sourceTableName
        );

        List<ColumnDefinition> columns = readColumns(
            metadata,
            sourceTableName
        );

        dropRawTable(sqlite, rawTableName);
        createRawTable(sqlite, rawTableName, columns);
        registerColumns(
            sqlite,
            importId,
            sourceTableName,
            columns
        );

        long sourceRowCount = countSourceRows(
            access,
            sourceTableName
        );

        long importedRowCount = copyRows(
            access,
            sqlite,
            sourceTableName,
            rawTableName,
            columns
        );

        registerTable(
            sqlite,
            importId,
            seasonId,
            sourceType,
            sourceTableName,
            rawTableName,
            sourceRowCount,
            importedRowCount,
            columns.size()
        );

        if (sourceRowCount != importedRowCount) {
            throw new IllegalStateException(
                "Audit fallito per " + sourceTableName
                    + ": sorgente=" + sourceRowCount
                    + ", importate=" + importedRowCount
            );
        }

        return new TableImportResult(
            columns.size(),
            importedRowCount
        );
    }

    private static List<ColumnDefinition> readColumns(
            DatabaseMetaData metadata,
            String tableName) throws Exception {

        List<ColumnDefinition> columns = new ArrayList<>();

        try (
            ResultSet result = metadata.getColumns(
                null,
                null,
                tableName,
                "%"
            )
        ) {
            while (result.next()) {
                columns.add(
                    new ColumnDefinition(
                        result.getString("COLUMN_NAME"),
                        result.getInt("ORDINAL_POSITION"),
                        result.getInt("DATA_TYPE"),
                        result.getString("TYPE_NAME"),
                        result.getInt("COLUMN_SIZE"),
                        nullableInteger(result, "DECIMAL_DIGITS"),
                        result.getInt("NULLABLE"),
                        result.getString("COLUMN_DEF")
                    )
                );
            }
        }

        columns.sort(
            (left, right) ->
                Integer.compare(
                    left.ordinalPosition(),
                    right.ordinalPosition()
                )
        );

        return columns;
    }

    private static void dropRawTable(
            Connection sqlite,
            String rawTableName) throws Exception {

        try (Statement statement = sqlite.createStatement()) {
            statement.execute(
                "DROP TABLE IF EXISTS " + quoteSqlite(rawTableName)
            );
        }
    }

    private static void createRawTable(
            Connection sqlite,
            String rawTableName,
            List<ColumnDefinition> columns) throws Exception {

        StringBuilder sql = new StringBuilder();

        sql.append("CREATE TABLE ");
        sql.append(quoteSqlite(rawTableName));
        sql.append(" (");

        for (int index = 0; index < columns.size(); index++) {
            if (index > 0) {
                sql.append(", ");
            }

            ColumnDefinition column = columns.get(index);

            sql.append(quoteSqlite(column.name()));
            sql.append(" ");
            sql.append(sqliteType(column.jdbcType()));
        }

        sql.append(")");

        try (Statement statement = sqlite.createStatement()) {
            statement.execute(sql.toString());
        }
    }

    private static long copyRows(
            Connection access,
            Connection sqlite,
            String sourceTableName,
            String rawTableName,
            List<ColumnDefinition> columns) throws Exception {

        String sourceSql =
            "SELECT * FROM " + quoteAccess(sourceTableName);

        StringBuilder insertSql = new StringBuilder();

        insertSql.append("INSERT INTO ");
        insertSql.append(quoteSqlite(rawTableName));
        insertSql.append(" (");

        for (int index = 0; index < columns.size(); index++) {
            if (index > 0) {
                insertSql.append(", ");
            }

            insertSql.append(
                quoteSqlite(columns.get(index).name())
            );
        }

        insertSql.append(") VALUES (");

        for (int index = 0; index < columns.size(); index++) {
            if (index > 0) {
                insertSql.append(", ");
            }

            insertSql.append("?");
        }

        insertSql.append(")");

        long importedRows = 0;
        int batchRows = 0;

        try (
            Statement sourceStatement = access.createStatement();
            ResultSet sourceRows =
                sourceStatement.executeQuery(sourceSql);
            PreparedStatement destination =
                sqlite.prepareStatement(insertSql.toString())
        ) {
            ResultSetMetaData rowMetadata =
                sourceRows.getMetaData();

            while (sourceRows.next()) {
                for (
                    int columnIndex = 1;
                    columnIndex <= columns.size();
                    columnIndex++
                ) {
                    setValue(
                        destination,
                        columnIndex,
                        sourceRows,
                        rowMetadata,
                        columnIndex
                    );
                }

                destination.addBatch();
                importedRows++;
                batchRows++;

                if (batchRows >= BATCH_SIZE) {
                    destination.executeBatch();
                    batchRows = 0;
                }
            }

            if (batchRows > 0) {
                destination.executeBatch();
            }
        }

        return importedRows;
    }

    private static void setValue(
            PreparedStatement destination,
            int destinationIndex,
            ResultSet source,
            ResultSetMetaData metadata,
            int sourceIndex) throws Exception {

        int jdbcType = metadata.getColumnType(sourceIndex);
        Object value = source.getObject(sourceIndex);

        if (value == null) {
            destination.setNull(
                destinationIndex,
                sqliteNullType(jdbcType)
            );
            return;
        }

        switch (jdbcType) {
            case Types.BINARY,
                 Types.VARBINARY,
                 Types.LONGVARBINARY,
                 Types.BLOB ->
                destination.setBytes(
                    destinationIndex,
                    source.getBytes(sourceIndex)
                );

            case Types.TINYINT,
                 Types.SMALLINT,
                 Types.INTEGER,
                 Types.BIGINT ->
                destination.setLong(
                    destinationIndex,
                    source.getLong(sourceIndex)
                );

            case Types.FLOAT,
                 Types.REAL,
                 Types.DOUBLE ->
                destination.setDouble(
                    destinationIndex,
                    source.getDouble(sourceIndex)
                );

            case Types.NUMERIC,
                 Types.DECIMAL ->
                destination.setBigDecimal(
                    destinationIndex,
                    source.getBigDecimal(sourceIndex)
                );

            case Types.BIT,
                 Types.BOOLEAN ->
                destination.setInt(
                    destinationIndex,
                    source.getBoolean(sourceIndex) ? 1 : 0
                );

            case Types.DATE,
                 Types.TIME,
                 Types.TIMESTAMP,
                 Types.TIMESTAMP_WITH_TIMEZONE ->
                destination.setString(
                    destinationIndex,
                    String.valueOf(value)
                );

            default ->
                destination.setString(
                    destinationIndex,
                    source.getString(sourceIndex)
                );
        }
    }

    private static long countSourceRows(
            Connection access,
            String tableName) throws Exception {

        String sql =
            "SELECT COUNT(*) FROM " + quoteAccess(tableName);

        try (
            Statement statement = access.createStatement();
            ResultSet result = statement.executeQuery(sql)
        ) {
            result.next();
            return result.getLong(1);
        }
    }

    private static void registerColumns(
            Connection sqlite,
            long importId,
            String tableName,
            List<ColumnDefinition> columns) throws Exception {

        String sql = """
            INSERT INTO rn_column_catalog (
                import_id,
                source_table_name,
                column_name,
                ordinal_position,
                jdbc_type,
                type_name,
                column_size,
                decimal_digits,
                nullable_code,
                default_value
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement =
                 sqlite.prepareStatement(sql)) {

            for (ColumnDefinition column : columns) {
                statement.setLong(1, importId);
                statement.setString(2, tableName);
                statement.setString(3, column.name());
                statement.setInt(4, column.ordinalPosition());
                statement.setInt(5, column.jdbcType());
                statement.setString(6, column.typeName());
                statement.setInt(7, column.columnSize());

                if (column.decimalDigits() == null) {
                    statement.setNull(8, Types.INTEGER);
                } else {
                    statement.setInt(
                        8,
                        column.decimalDigits()
                    );
                }

                statement.setInt(9, column.nullableCode());
                statement.setString(10, column.defaultValue());
                statement.addBatch();
            }

            statement.executeBatch();
        }
    }

    private static void registerTable(
            Connection sqlite,
            long importId,
            String seasonId,
            String sourceType,
            String sourceTableName,
            String rawTableName,
            long sourceRowCount,
            long importedRowCount,
            int columnCount) throws Exception {

        String sql = """
            INSERT INTO rn_table_catalog (
                import_id,
                season_id,
                source_type,
                source_table_name,
                raw_table_name,
                source_row_count,
                imported_row_count,
                column_count,
                audit_ok
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement =
                 sqlite.prepareStatement(sql)) {

            statement.setLong(1, importId);
            statement.setString(2, seasonId);
            statement.setString(3, sourceType);
            statement.setString(4, sourceTableName);
            statement.setString(5, rawTableName);
            statement.setLong(6, sourceRowCount);
            statement.setLong(7, importedRowCount);
            statement.setInt(8, columnCount);
            statement.setInt(
                9,
                sourceRowCount == importedRowCount ? 1 : 0
            );
            statement.executeUpdate();
        }
    }

    private static void finishImport(
            Connection sqlite,
            long importId,
            int tableCount,
            long columnCount,
            long rowCount) throws Exception {

        String sql = """
            UPDATE rn_import
            SET completed_at = ?,
                table_count = ?,
                column_count = ?,
                row_count = ?,
                status = ?
            WHERE import_id = ?
            """;

        try (PreparedStatement statement =
                 sqlite.prepareStatement(sql)) {

            statement.setString(1, Instant.now().toString());
            statement.setInt(2, tableCount);
            statement.setLong(3, columnCount);
            statement.setLong(4, rowCount);
            statement.setString(5, "COMPLETED");
            statement.setLong(6, importId);
            statement.executeUpdate();
        }
    }

    private static String rawTableName(
            String sourceType,
            String seasonId,
            String sourceTableName) {

        return "raw_"
            + normalizeIdentifier(seasonId)
            + "_"
            + sourceType.toLowerCase(Locale.ROOT)
            + "_"
            + normalizeIdentifier(sourceTableName);
    }

    private static String normalizeIdentifier(String value) {
        String normalized = value
            .trim()
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "_")
            .replaceAll("^_+|_+$", "");

        if (normalized.isBlank()) {
            throw new IllegalArgumentException(
                "Identificatore non normalizzabile: " + value
            );
        }

        return normalized;
    }

    private static String quoteAccess(String name) {
        return "[" + name.replace("]", "]]") + "]";
    }

    private static String quoteSqlite(String name) {
        return "\"" + name.replace("\"", "\"\"") + "\"";
    }

    private static String sqliteType(int jdbcType) {
        return switch (jdbcType) {
            case Types.BIT,
                 Types.BOOLEAN,
                 Types.TINYINT,
                 Types.SMALLINT,
                 Types.INTEGER,
                 Types.BIGINT -> "INTEGER";

            case Types.FLOAT,
                 Types.REAL,
                 Types.DOUBLE -> "REAL";

            case Types.NUMERIC,
                 Types.DECIMAL -> "NUMERIC";

            case Types.BINARY,
                 Types.VARBINARY,
                 Types.LONGVARBINARY,
                 Types.BLOB -> "BLOB";

            default -> "TEXT";
        };
    }

    private static int sqliteNullType(int jdbcType) {
        return switch (sqliteType(jdbcType)) {
            case "INTEGER" -> Types.INTEGER;
            case "REAL" -> Types.REAL;
            case "NUMERIC" -> Types.NUMERIC;
            case "BLOB" -> Types.BLOB;
            default -> Types.VARCHAR;
        };
    }

    private static Integer nullableInteger(
            ResultSet result,
            String columnName) throws Exception {

        int value = result.getInt(columnName);
        return result.wasNull() ? null : value;
    }

    private static String sha256(Path path) throws Exception {
        MessageDigest digest =
            MessageDigest.getInstance("SHA-256");

        try (var input = Files.newInputStream(path)) {
            byte[] buffer = new byte[1024 * 1024];
            int read;

            while ((read = input.read(buffer)) >= 0) {
                digest.update(buffer, 0, read);
            }
        }

        return HexFormat.of().formatHex(digest.digest());
    }

    private record ColumnDefinition(
        String name,
        int ordinalPosition,
        int jdbcType,
        String typeName,
        int columnSize,
        Integer decimalDigits,
        int nullableCode,
        String defaultValue
    ) {
    }

    private record TableImportResult(
        int columns,
        long rows
    ) {
    }
}
```

### src\main\java\it\alterlega\recordsnext\Records2026ClassicJsExporter.java

```java
package it.alterlega.recordsnext;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Genera records2026.recordstagionali.classic.js mantenendo il contratto
 * pubblico di Records2026 e pubblicando soltanto le sezioni/campi previsti.
 */
public final class Records2026ClassicJsExporter {

    private static final String PREFIX = "season_records_";
    private static final String SUFFIX = ".json";

    private static final Map<String, Set<String>> PUBLIC_FIELDS = buildPublicFields();

    private Records2026ClassicJsExporter() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            printUsage();
            System.exit(2);
        }

        Path archiveRoot = Path.of(args[0]).toAbsolutePath().normalize();
        Path outputFile = Path.of(args[1]).toAbsolutePath().normalize();
        List<String> requestedSeasons = new ArrayList<>();
        for (int i = 2; i < args.length; i++) {
            String value = args[i].trim();
            if (!value.isEmpty()) {
                requestedSeasons.add(value);
            }
        }

        ExportResult result = export(archiveRoot, outputFile, requestedSeasons);
        System.out.println("Archivio : " + archiveRoot);
        System.out.println("Output   : " + outputFile);
        System.out.println("Stagioni : " + result.seasonCount());
        System.out.println("Recordset: " + result.entryCount());
    }

    public static ExportResult export(Path archiveRoot, Path outputFile, List<String> requestedSeasons)
            throws IOException {
        if (!Files.isDirectory(archiveRoot)) {
            throw new IOException("Archivio stagioni non trovato: " + archiveRoot);
        }

        List<Path> seasonDirectories = resolveSeasonDirectories(archiveRoot, requestedSeasons);
        if (seasonDirectories.isEmpty()) {
            throw new IOException("Nessuna stagione trovata in: " + archiveRoot);
        }

        List<Entry> entries = new ArrayList<>();
        int seasonsWithRecords = 0;

        for (Path seasonDirectory : seasonDirectories) {
            List<Path> recordFiles = listRecordFiles(seasonDirectory);
            if (recordFiles.isEmpty()) {
                continue;
            }
            seasonsWithRecords++;
            String season = seasonDirectory.getFileName().toString();

            for (Path recordFile : recordFiles) {
                String fileName = recordFile.getFileName().toString();
                String competitionId = fileName.substring(PREFIX.length(), fileName.length() - SUFFIX.length());
                String sourceText = normalizeJsonText(Files.readString(recordFile, StandardCharsets.UTF_8));
                Object parsed = new JsonParser(sourceText, recordFile).parse();
                Map<String, Object> root = requireObject(parsed, recordFile, "radice");
                Map<String, Object> sourceRecords = requireObject(root.get("records"), recordFile, "records");
                Map<String, Object> publicRecords = projectRecords(sourceRecords, recordFile);

                Map<String, Object> publicData = new LinkedHashMap<>();
                publicData.put("records", publicRecords);
                String json = escapeScriptTerminator(JsonWriter.write(publicData));
                entries.add(new Entry(season, competitionId, fileName, json));
            }
        }

        if (entries.isEmpty()) {
            throw new IOException("Nessun file season_records_*.json trovato in: " + archiveRoot);
        }

        Path parent = outputFile.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(outputFile, buildJavascript(entries), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        return new ExportResult(seasonsWithRecords, entries.size());
    }

    private static Map<String, Object> projectRecords(Map<String, Object> sourceRecords, Path source)
            throws IOException {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> sectionEntry : sourceRecords.entrySet()) {
            String section = sectionEntry.getKey();
            Set<String> allowedFields = PUBLIC_FIELDS.get(section);
            if (allowedFields == null) {
                continue;
            }

            Object value = sectionEntry.getValue();
            if (!(value instanceof List<?> sourceRows)) {
                continue;
            }
            if (sourceRows.isEmpty()) {
                continue;
            }

            List<Object> publicRows = new ArrayList<>(sourceRows.size());
            for (Object row : sourceRows) {
                if (!(row instanceof Map<?, ?> rawMap)) {
                    throw new IOException("Riga non oggetto nella sezione '" + section + "': " + source);
                }
                Map<String, Object> projected = new LinkedHashMap<>();
                for (Map.Entry<?, ?> fieldEntry : rawMap.entrySet()) {
                    String fieldName = String.valueOf(fieldEntry.getKey());
                    if (allowedFields.contains(fieldName)) {
                        projected.put(fieldName, fieldEntry.getValue());
                    }
                }

                // Nei JSON sorgente il dettaglio completo e' spesso conservato
                // nell'array "dettagli". Il file pubblico espone soltanto il
                // relativo conteggio, calcolato dal generatore legacy.
                if (allowedFields.contains("dettagliCount") && !projected.containsKey("dettagliCount")) {
                    Object details = rawMap.get("dettagli");
                    if (details instanceof List<?> detailRows) {
                        projected.put("dettagliCount", detailRows.size());
                    }
                }
                publicRows.add(projected);
            }
            result.put(section, publicRows);
        }
        return result;
    }

    private static Map<String, Set<String>> buildPublicFields() {
        Map<String, Set<String>> fields = new LinkedHashMap<>();
        fields.put("puntiSquadraMax", orderedSet(
                "recordId", "nome", "stagione", "competizioneStoricaId", "competizioneNome", "valore",
                "squadra", "avversaria", "idIncontro", "giornata", "giornataDiA", "urlTabellino",
                "risultato", "punteggio"));
        fields.put("serieSenzaSconfitte", orderedSet(
                "recordId", "nome", "stagione", "competizioneStoricaId", "competizioneNome", "valore",
                "squadra", "idSquadra", "daGiornata", "aGiornata", "daGiornataDiA", "aGiornataDiA",
                "vittorie", "pareggi", "dettagliCount"));
        fields.put("espulsioniSquadre", compactTeamFields());
        fields.put("espulsioniGiocatori", orderedSet(
                "recordId", "nome", "valore", "idGiocatore", "giocatore", "dettagliCount"));
        fields.put("ammonizioniSquadre", compactTeamFields());
        fields.put("assistSquadre", compactTeamFields());
        fields.put("autogolSquadre", compactTeamFields());
        fields.put("rigoriSbagliatiSquadre", compactTeamFields());
        fields.put("rigoriParatiSquadre", compactTeamFields());
        fields.put("golRigoreSquadre", compactTeamFields());
        fields.put("modDifesaMax", orderedSet(
                "recordId", "nome", "valore", "idSquadra", "squadra", "avversaria", "idIncontro", "giornataDiA"));
        fields.put("modDifesaTotaleSquadre", compactTeamFields());
        fields.put("capitanoVolteSquadre", compactTeamFields());
        fields.put("capitanoTotaleSquadre", compactTeamFields());
        fields.put("capitanoSerieSquadre", orderedSet(
                "recordId", "nome", "valore", "idSquadra", "squadra", "daGiornataDiA", "aGiornataDiA", "dettagliCount"));
        fields.put("cleanSheetPortiereVolteSquadre", compactTeamFields());
        fields.put("cleanSheetPortiereTotaleSquadre", compactTeamFields());
        fields.put("cleanSheetPortiereSerieSquadre", orderedSet(
                "recordId", "nome", "valore", "idSquadra", "squadra", "daGiornataDiA", "aGiornataDiA", "dettagliCount"));
        return fields;
    }

    private static Set<String> compactTeamFields() {
        return orderedSet("recordId", "nome", "valore", "idSquadra", "squadra", "dettagliCount");
    }

    private static Set<String> orderedSet(String... values) {
        Set<String> result = new LinkedHashSet<>();
        for (String value : values) {
            result.add(value);
        }
        return Set.copyOf(result);
    }

    private static Map<String, Object> requireObject(Object value, Path source, String label) throws IOException {
        if (!(value instanceof Map<?, ?> raw)) {
            throw new IOException("Oggetto JSON '" + label + "' mancante o non valido: " + source);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : raw.entrySet()) {
            result.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return result;
    }

    private static List<Path> resolveSeasonDirectories(Path archiveRoot, List<String> requestedSeasons)
            throws IOException {
        List<Path> result = new ArrayList<>();
        if (requestedSeasons == null || requestedSeasons.isEmpty()) {
            try (Stream<Path> stream = Files.list(archiveRoot)) {
                stream.filter(Files::isDirectory)
                        .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                        .forEach(result::add);
            }
            return result;
        }
        requestedSeasons.stream().distinct().sorted().map(archiveRoot::resolve)
                .filter(Files::isDirectory).forEach(result::add);
        return result;
    }

    private static List<Path> listRecordFiles(Path seasonDirectory) throws IOException {
        try (Stream<Path> stream = Files.list(seasonDirectory)) {
            return stream.filter(Files::isRegularFile)
                    .filter(path -> {
                        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
                        return name.startsWith(PREFIX) && name.endsWith(SUFFIX);
                    })
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .toList();
        }
    }

    private static String normalizeJsonText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        String normalized = text;
        if (normalized.charAt(0) == '\uFEFF') {
            normalized = normalized.substring(1);
        }
        return normalized.trim();
    }

    private static String buildJavascript(List<Entry> entries) {
        StringBuilder output = new StringBuilder();
        output.append("window.RECORDS2026_PREVIEW_CLASSIC = [");
        for (int i = 0; i < entries.size(); i++) {
            if (i > 0) {
                output.append(',');
            }
            Entry entry = entries.get(i);
            output.append("{\"stagione\":\"").append(JsonWriter.escape(entry.season()))
                    .append("\",\"id\":\"").append(JsonWriter.escape(entry.competitionId()))
                    .append("\",\"file\":\"").append(JsonWriter.escape(entry.fileName()))
                    .append("\",\"data\":").append(entry.json()).append('}');
        }
        output.append("];\n");
        return output.toString();
    }

    private static String escapeScriptTerminator(String json) {
        return json.replace("</script>", "<\\/script>");
    }

    private static void printUsage() {
        System.err.println("Uso:");
        System.err.println("  Records2026ClassicJsExporter <archiveRoot> <outputFile> [stagione ...]");
    }

    private record Entry(String season, String competitionId, String fileName, String json) {
    }

    public record ExportResult(int seasonCount, int entryCount) {
    }

    private static final class JsonParser {
        private final String text;
        private final Path source;
        private int index;

        JsonParser(String text, Path source) {
            this.text = text;
            this.source = source;
        }

        Object parse() throws IOException {
            skipWhitespace();
            Object value = parseValue();
            skipWhitespace();
            if (index != text.length()) {
                fail("Contenuto dopo la fine del JSON");
            }
            return value;
        }

        private Object parseValue() throws IOException {
            skipWhitespace();
            if (index >= text.length()) fail("Valore mancante");
            return switch (text.charAt(index)) {
                case '{' -> parseObject();
                case '[' -> parseArray();
                case '"' -> parseString();
                case 't' -> parseLiteral("true", Boolean.TRUE);
                case 'f' -> parseLiteral("false", Boolean.FALSE);
                case 'n' -> parseLiteral("null", null);
                default -> parseNumber();
            };
        }

        private Map<String, Object> parseObject() throws IOException {
            expect('{');
            Map<String, Object> result = new LinkedHashMap<>();
            skipWhitespace();
            if (peek('}')) { index++; return result; }
            while (true) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                expect(':');
                result.put(key, parseValue());
                skipWhitespace();
                if (peek('}')) { index++; return result; }
                expect(',');
            }
        }

        private List<Object> parseArray() throws IOException {
            expect('[');
            List<Object> result = new ArrayList<>();
            skipWhitespace();
            if (peek(']')) { index++; return result; }
            while (true) {
                result.add(parseValue());
                skipWhitespace();
                if (peek(']')) { index++; return result; }
                expect(',');
            }
        }

        private String parseString() throws IOException {
            expect('"');
            StringBuilder result = new StringBuilder();
            while (index < text.length()) {
                char ch = text.charAt(index++);
                if (ch == '"') return result.toString();
                if (ch != '\\') { result.append(ch); continue; }
                if (index >= text.length()) fail("Escape incompleto");
                char esc = text.charAt(index++);
                switch (esc) {
                    case '"', '\\', '/' -> result.append(esc);
                    case 'b' -> result.append('\b');
                    case 'f' -> result.append('\f');
                    case 'n' -> result.append('\n');
                    case 'r' -> result.append('\r');
                    case 't' -> result.append('\t');
                    case 'u' -> result.append(parseUnicode());
                    default -> fail("Escape non valido: \\" + esc);
                }
            }
            fail("Stringa non terminata");
            return null;
        }

        private char parseUnicode() throws IOException {
            if (index + 4 > text.length()) fail("Escape unicode incompleto");
            String hex = text.substring(index, index + 4);
            index += 4;
            try { return (char) Integer.parseInt(hex, 16); }
            catch (NumberFormatException ex) { fail("Escape unicode non valido: " + hex); return 0; }
        }

        private Object parseLiteral(String literal, Object value) throws IOException {
            if (!text.startsWith(literal, index)) fail("Token non valido");
            index += literal.length();
            return value;
        }

        private BigDecimal parseNumber() throws IOException {
            int start = index;
            if (peek('-')) index++;
            while (index < text.length() && Character.isDigit(text.charAt(index))) index++;
            if (peek('.')) {
                index++;
                while (index < text.length() && Character.isDigit(text.charAt(index))) index++;
            }
            if (peek('e') || peek('E')) {
                index++;
                if (peek('+') || peek('-')) index++;
                while (index < text.length() && Character.isDigit(text.charAt(index))) index++;
            }
            if (start == index) fail("Numero non valido");
            try { return new BigDecimal(text.substring(start, index)); }
            catch (NumberFormatException ex) { fail("Numero non valido"); return null; }
        }

        private void expect(char expected) throws IOException {
            skipWhitespace();
            if (index >= text.length() || text.charAt(index) != expected) {
                fail("Atteso '" + expected + "'");
            }
            index++;
        }

        private boolean peek(char value) {
            return index < text.length() && text.charAt(index) == value;
        }

        private void skipWhitespace() {
            while (index < text.length() && Character.isWhitespace(text.charAt(index))) index++;
        }

        private void fail(String message) throws IOException {
            throw new IOException(message + " in " + source + " alla posizione " + index);
        }
    }

    private static final class JsonWriter {
        static String write(Object value) {
            StringBuilder out = new StringBuilder();
            append(out, value);
            return out.toString();
        }

        private static void append(StringBuilder out, Object value) {
            if (value == null) { out.append("null"); return; }
            if (value instanceof String string) { out.append('"').append(escape(string)).append('"'); return; }
            if (value instanceof Boolean || value instanceof BigDecimal) { out.append(value); return; }
            if (value instanceof Number number) { out.append(number); return; }
            if (value instanceof Map<?, ?> map) {
                out.append('{');
                boolean first = true;
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    if (!first) out.append(',');
                    first = false;
                    out.append('"').append(escape(String.valueOf(entry.getKey()))).append("\":");
                    append(out, entry.getValue());
                }
                out.append('}');
                return;
            }
            if (value instanceof List<?> list) {
                out.append('[');
                for (int i = 0; i < list.size(); i++) {
                    if (i > 0) out.append(',');
                    append(out, list.get(i));
                }
                out.append(']');
                return;
            }
            throw new IllegalArgumentException("Tipo JSON non supportato: " + value.getClass());
        }

        static String escape(String value) {
            StringBuilder escaped = new StringBuilder(value.length() + 16);
            for (int i = 0; i < value.length(); i++) {
                char ch = value.charAt(i);
                switch (ch) {
                    case '\\' -> escaped.append("\\\\");
                    case '"' -> escaped.append("\\\"");
                    case '\b' -> escaped.append("\\b");
                    case '\f' -> escaped.append("\\f");
                    case '\n' -> escaped.append("\\n");
                    case '\r' -> escaped.append("\\r");
                    case '\t' -> escaped.append("\\t");
                    default -> {
                        if (ch < 0x20) escaped.append(String.format("\\u%04x", (int) ch));
                        else escaped.append(ch);
                    }
                }
            }
            return escaped.toString();
        }
    }
}
```

### src\main\java\it\alterlega\recordsnext\Records2026RuJsExporter.java

```java
package it.alterlega.recordsnext;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/** Genera gli output RU pubblici compatibili con Records2026. */
public final class Records2026RuJsExporter {
    private Records2026RuJsExporter() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Uso: Records2026RuJsExporter <archive-riserveufficio> <output-js-dir>");
            System.exit(2);
        }
        ExportResult result = export(Path.of(args[0]), Path.of(args[1]));
        System.out.println("Archivio : " + Path.of(args[0]).toAbsolutePath().normalize());
        System.out.println("Output   : " + Path.of(args[1]).toAbsolutePath().normalize());
        System.out.println("Stagioni : " + result.seasons());
        System.out.println("Annuali  : " + result.annualFiles());
    }

    public static ExportResult export(Path archiveRoot, Path outputDir) throws IOException {
        archiveRoot = archiveRoot.toAbsolutePath().normalize();
        outputDir = outputDir.toAbsolutePath().normalize();
        if (!Files.isDirectory(archiveRoot)) throw new IOException("Archivio RU non trovato: " + archiveRoot);
        Files.createDirectories(outputDir);

        List<Path> seasonDirs;
        try (Stream<Path> s = Files.list(archiveRoot)) {
            seasonDirs = s.filter(Files::isDirectory)
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .toList();
        }

        List<Object> compactItems = new ArrayList<>();
        List<Object> manifestItems = new ArrayList<>();
        int annualFiles = 0;

        for (Path seasonDir : seasonDirs) {
            String season = seasonDir.getFileName().toString();
            Path jsonPath = seasonDir.resolve("riserveufficio.json");
            if (!Files.isRegularFile(jsonPath)) continue;

            String rawSource = readUtf8WithoutBom(jsonPath);
            String source = normalizeJsonText(rawSource);
            Object parsed = new JsonParser(source, jsonPath).parse();
            Map<String,Object> root = asObject(parsed, jsonPath, "radice");

            String annualJson = escapeScriptTerminator(stripTrailingLineBreaks(rawSource));
            String annual = "window.RECORDS2026_STORICO_RU = window.RECORDS2026_STORICO_RU || {};\r\n"
                    + "window.RECORDS2026_STORICO_RU['" + JsonWriter.escape(season) + "'] = " + annualJson + ";\r\n";
            Path annualPath = outputDir.resolve("records2026.storico.ru." + season.replaceAll("[^\\w]+", "_") + ".js");
            writeUtf8Bom(annualPath, annual);
            annualFiles++;

            Map<String,Object> data = new LinkedHashMap<>();
            data.put("views", compactArrayMap(root.get("views")));
            data.put("dettaglio", compactArrayMap(root.get("dettaglio")));
            data.put("curiosita", root.get("curiosita"));
            Map<String,Object> compact = new LinkedHashMap<>();
            compact.put("stagione", season);
            compact.put("data", data);
            compactItems.add(compact);

            Map<String,Object> detail = objectOrEmpty(root.get("dettaglio"));
            int ruRows = listSize(detail.get("ruDettaglio"));
            Map<String,Object> meta = objectOrEmpty(root.get("meta"));
            Map<String,Object> manifestItem = new LinkedHashMap<>();
            manifestItem.put("stagione", season);
            manifestItem.put("jsFile", annualPath.getFileName().toString());
            manifestItem.put("ruDettaglio", ruRows);
            manifestItem.put("generated", stringValue(meta.get("generato")));
            manifestItems.add(manifestItem);
        }

        String compactJs = "window.RECORDS2026_PREVIEW_RU = "
                + escapeScriptTerminator(JsonWriter.write(compactItems)) + ";";
        writeUtf8(outputDir.resolve("records2026.recordstagionali.ru.js"), compactJs);

        Map<String,Object> manifestMeta = new LinkedHashMap<>();
        manifestMeta.put("titolo", "Records2026 Storico Riserve d'Ufficio");
        manifestMeta.put("generato", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        manifestMeta.put("modello", "manifest + js annuale");
        Map<String,Object> manifest = new LinkedHashMap<>();
        manifest.put("meta", manifestMeta);
        manifest.put("stagioni", manifestItems);
        String manifestJs = "window.RECORDS2026_STORICO_RU_MANIFEST = "
                + JsonWriter.writePretty(manifest) + ";\r\n";
        writeUtf8Bom(outputDir.resolve("records2026.storico.ru.manifest.js"), manifestJs);

        return new ExportResult(compactItems.size(), annualFiles);
    }

    private static Map<String,Object> compactArrayMap(Object value) {
        Map<String,Object> out = new LinkedHashMap<>();
        for (Map.Entry<String,Object> e : objectOrEmpty(value).entrySet()) {
            if (e.getValue() instanceof List<?> rows) {
                List<Object> compactRows = new ArrayList<>(rows.size());
                for (Object row : rows) compactRows.add(compactRow(row));
                out.put(e.getKey(), compactRows);
            } else if (e.getValue() == null) {
                out.put(e.getKey(), List.of());
            }
        }
        return out;
    }

    private static Map<String,Object> compactRow(Object value) {
        Map<String,Object> out = new LinkedHashMap<>();
        if (!(value instanceof Map<?,?> raw)) return out;
        for (Map.Entry<?,?> e : raw.entrySet()) {
            String name = String.valueOf(e.getKey());
            Object v = e.getValue();
            if (name.equals("dettagli")) {
                if (v instanceof List<?> rows) out.put("dettagliCount", rows.size());
                continue;
            }
            if (name.matches("^(dettaglio|dettagliPartite|partiteDettaglio|rows|raw|sourceRows)$")) continue;
            if (v == null || v instanceof String || v instanceof Number || v instanceof Boolean) out.put(name, v);
        }
        return out;
    }

    private static Map<String,Object> asObject(Object value, Path source, String label) throws IOException {
        if (!(value instanceof Map<?,?> raw)) throw new IOException("Oggetto JSON '" + label + "' non valido: " + source);
        Map<String,Object> out = new LinkedHashMap<>();
        for (Map.Entry<?,?> e : raw.entrySet()) out.put(String.valueOf(e.getKey()), e.getValue());
        return out;
    }
    private static Map<String,Object> objectOrEmpty(Object value) {
        Map<String,Object> out = new LinkedHashMap<>();
        if (value instanceof Map<?,?> raw) for (Map.Entry<?,?> e : raw.entrySet()) out.put(String.valueOf(e.getKey()), e.getValue());
        return out;
    }
    private static int listSize(Object value) { return value instanceof List<?> l ? l.size() : 0; }
    private static String stringValue(Object value) { return value == null ? "" : String.valueOf(value); }
    private static String readUtf8WithoutBom(Path path) throws IOException {
        String text = Files.readString(path, StandardCharsets.UTF_8);
        return (!text.isEmpty() && text.charAt(0) == '\uFEFF') ? text.substring(1) : text;
    }
    private static String stripTrailingLineBreaks(String text) {
        int end = text.length();
        while (end > 0 && (text.charAt(end - 1) == '\r' || text.charAt(end - 1) == '\n')) end--;
        return text.substring(0, end);
    }
    private static String normalizeJsonText(String text) {
        if (text == null || text.isEmpty()) return "";
        if (text.charAt(0) == '\uFEFF') text = text.substring(1);
        return text.trim();
    }
    private static String escapeScriptTerminator(String json) { return json.replace("</script>", "<\\/script>"); }
    private static void writeUtf8(Path path, String text) throws IOException {
        Files.writeString(path, text, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
    private static void writeUtf8Bom(Path path, String text) throws IOException {
        byte[] body = text.getBytes(StandardCharsets.UTF_8);
        byte[] out = new byte[body.length + 3];
        out[0] = (byte)0xEF; out[1] = (byte)0xBB; out[2] = (byte)0xBF;
        System.arraycopy(body, 0, out, 3, body.length);
        Files.write(path, out, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
    public record ExportResult(int seasons, int annualFiles) {}

    private static final class JsonParser {
        private final String text;
        private final Path source;
        private int index;

        JsonParser(String text, Path source) {
            this.text = text;
            this.source = source;
        }

        Object parse() throws IOException {
            skipWhitespace();
            Object value = parseValue();
            skipWhitespace();
            if (index != text.length()) {
                fail("Contenuto dopo la fine del JSON");
            }
            return value;
        }

        private Object parseValue() throws IOException {
            skipWhitespace();
            if (index >= text.length()) fail("Valore mancante");
            return switch (text.charAt(index)) {
                case '{' -> parseObject();
                case '[' -> parseArray();
                case '"' -> parseString();
                case 't' -> parseLiteral("true", Boolean.TRUE);
                case 'f' -> parseLiteral("false", Boolean.FALSE);
                case 'n' -> parseLiteral("null", null);
                default -> parseNumber();
            };
        }

        private Map<String, Object> parseObject() throws IOException {
            expect('{');
            Map<String, Object> result = new LinkedHashMap<>();
            skipWhitespace();
            if (peek('}')) { index++; return result; }
            while (true) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                expect(':');
                result.put(key, parseValue());
                skipWhitespace();
                if (peek('}')) { index++; return result; }
                expect(',');
            }
        }

        private List<Object> parseArray() throws IOException {
            expect('[');
            List<Object> result = new ArrayList<>();
            skipWhitespace();
            if (peek(']')) { index++; return result; }
            while (true) {
                result.add(parseValue());
                skipWhitespace();
                if (peek(']')) { index++; return result; }
                expect(',');
            }
        }

        private String parseString() throws IOException {
            expect('"');
            StringBuilder result = new StringBuilder();
            while (index < text.length()) {
                char ch = text.charAt(index++);
                if (ch == '"') return result.toString();
                if (ch != '\\') { result.append(ch); continue; }
                if (index >= text.length()) fail("Escape incompleto");
                char esc = text.charAt(index++);
                switch (esc) {
                    case '"', '\\', '/' -> result.append(esc);
                    case 'b' -> result.append('\b');
                    case 'f' -> result.append('\f');
                    case 'n' -> result.append('\n');
                    case 'r' -> result.append('\r');
                    case 't' -> result.append('\t');
                    case 'u' -> result.append(parseUnicode());
                    default -> fail("Escape non valido: \\" + esc);
                }
            }
            fail("Stringa non terminata");
            return null;
        }

        private char parseUnicode() throws IOException {
            if (index + 4 > text.length()) fail("Escape unicode incompleto");
            String hex = text.substring(index, index + 4);
            index += 4;
            try { return (char) Integer.parseInt(hex, 16); }
            catch (NumberFormatException ex) { fail("Escape unicode non valido: " + hex); return 0; }
        }

        private Object parseLiteral(String literal, Object value) throws IOException {
            if (!text.startsWith(literal, index)) fail("Token non valido");
            index += literal.length();
            return value;
        }

        private BigDecimal parseNumber() throws IOException {
            int start = index;
            if (peek('-')) index++;
            while (index < text.length() && Character.isDigit(text.charAt(index))) index++;
            if (peek('.')) {
                index++;
                while (index < text.length() && Character.isDigit(text.charAt(index))) index++;
            }
            if (peek('e') || peek('E')) {
                index++;
                if (peek('+') || peek('-')) index++;
                while (index < text.length() && Character.isDigit(text.charAt(index))) index++;
            }
            if (start == index) fail("Numero non valido");
            try { return new BigDecimal(text.substring(start, index)); }
            catch (NumberFormatException ex) { fail("Numero non valido"); return null; }
        }

        private void expect(char expected) throws IOException {
            skipWhitespace();
            if (index >= text.length() || text.charAt(index) != expected) {
                fail("Atteso '" + expected + "'");
            }
            index++;
        }

        private boolean peek(char value) {
            return index < text.length() && text.charAt(index) == value;
        }

        private void skipWhitespace() {
            while (index < text.length() && Character.isWhitespace(text.charAt(index))) index++;
        }

        private void fail(String message) throws IOException {
            throw new IOException(message + " in " + source + " alla posizione " + index);
        }
    }

    private static final class JsonWriter {
        static String write(Object value) {
            StringBuilder out = new StringBuilder();
            append(out, value);
            return out.toString();
        }

        static String writePretty(Object value) {
            StringBuilder out = new StringBuilder();
            appendPretty(out, value, 0);
            return out.toString();
        }

        private static void appendPretty(StringBuilder out, Object value, int depth) {
            if (value == null || value instanceof String || value instanceof Boolean || value instanceof Number) { append(out, value); return; }
            String indent = "    ".repeat(depth);
            String childIndent = "    ".repeat(depth + 1);
            if (value instanceof Map<?, ?> map) {
                if (map.isEmpty()) { out.append("{}"); return; }
                out.append("{\r\n");
                boolean first = true;
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    if (!first) out.append(",\r\n");
                    first = false;
                    out.append(childIndent).append('"').append(escape(String.valueOf(entry.getKey()))).append("\": ");
                    appendPretty(out, entry.getValue(), depth + 1);
                }
                out.append("\r\n").append(indent).append("}");
                return;
            }
            if (value instanceof List<?> list) {
                if (list.isEmpty()) { out.append("[]"); return; }
                out.append("[\r\n");
                for (int i = 0; i < list.size(); i++) {
                    if (i > 0) out.append(",\r\n");
                    out.append(childIndent);
                    appendPretty(out, list.get(i), depth + 1);
                }
                out.append("\r\n").append(indent).append("]");
                return;
            }
            append(out, value);
        }

        private static void append(StringBuilder out, Object value) {
            if (value == null) { out.append("null"); return; }
            if (value instanceof String string) { out.append('"').append(escape(string)).append('"'); return; }
            if (value instanceof Boolean || value instanceof BigDecimal) { out.append(value); return; }
            if (value instanceof Number number) { out.append(number); return; }
            if (value instanceof Map<?, ?> map) {
                out.append('{');
                boolean first = true;
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    if (!first) out.append(',');
                    first = false;
                    out.append('"').append(escape(String.valueOf(entry.getKey()))).append("\":");
                    append(out, entry.getValue());
                }
                out.append('}');
                return;
            }
            if (value instanceof List<?> list) {
                out.append('[');
                for (int i = 0; i < list.size(); i++) {
                    if (i > 0) out.append(',');
                    append(out, list.get(i));
                }
                out.append(']');
                return;
            }
            throw new IllegalArgumentException("Tipo JSON non supportato: " + value.getClass());
        }

        static String escape(String value) {
            StringBuilder escaped = new StringBuilder(value.length() + 16);
            for (int i = 0; i < value.length(); i++) {
                char ch = value.charAt(i);
                switch (ch) {
                    case '\\' -> escaped.append("\\\\");
                    case '"' -> escaped.append("\\\"");
                    case '\b' -> escaped.append("\\b");
                    case '\f' -> escaped.append("\\f");
                    case '\n' -> escaped.append("\\n");
                    case '\r' -> escaped.append("\\r");
                    case '\t' -> escaped.append("\\t");
                    default -> {
                        if (ch < 0x20) escaped.append(String.format("\\u%04x", (int) ch));
                        else escaped.append(ch);
                    }
                }
            }
            return escaped.toString();
        }
    }
}
```

### src\main\java\it\alterlega\recordsnext\Records2026SitePublisher.java

```java
package it.alterlega.recordsnext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Coordina la generazione e la pubblicazione degli output JS compatibili
 * con Records2026.
 *
 * Flusso:
 *  1. genera tutto in una staging isolata;
 *  2. valida nomi, quantità, prefissi e dimensioni minime;
 *  3. pubblica ogni file mediante file temporaneo + move atomica;
 *  4. ripristina i file precedenti se una pubblicazione fallisce.
 */
public final class Records2026SitePublisher {

    private static final String CLASSIC_FILE = "records2026.recordstagionali.classic.js";
    private static final String RU_FILE = "records2026.recordstagionali.ru.js";
    private static final String MANIFEST_FILE = "records2026.storico.ru.manifest.js";
    private static final String ANNUAL_PREFIX = "records2026.storico.ru.";
    private static final String ANNUAL_SUFFIX = ".js";

    private Records2026SitePublisher() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 4 || args.length > 5) {
            System.err.println("Uso:");
            System.err.println("  Records2026SitePublisher <classicArchive> <ruArchive> <stagingRoot> <siteJsDir> [--generate-only]");
            System.exit(2);
        }

        Path classicArchive = Path.of(args[0]).toAbsolutePath().normalize();
        Path ruArchive = Path.of(args[1]).toAbsolutePath().normalize();
        Path stagingRoot = Path.of(args[2]).toAbsolutePath().normalize();
        Path siteJsDir = Path.of(args[3]).toAbsolutePath().normalize();
        boolean generateOnly = args.length == 5 && "--generate-only".equalsIgnoreCase(args[4]);

        PublishResult result = run(classicArchive, ruArchive, stagingRoot, siteJsDir, generateOnly);

        System.out.println("Classic     : " + result.classicEntries() + " recordset");
        System.out.println("RU stagioni : " + result.ruSeasons());
        System.out.println("RU annuali  : " + result.annualFiles());
        System.out.println("File validi : " + result.validatedFiles());
        System.out.println("Staging     : " + result.stagingDirectory());
        System.out.println(generateOnly
                ? "Pubblicazione: NON ESEGUITA (--generate-only)"
                : "Pubblicati   : " + result.publishedFiles() + " file in " + siteJsDir);
    }

    public static PublishResult run(
            Path classicArchive,
            Path ruArchive,
            Path stagingRoot,
            Path siteJsDir,
            boolean generateOnly) throws IOException {
        return run(classicArchive, ruArchive, stagingRoot, siteJsDir, generateOnly, true, true);
    }

    public static PublishResult run(
            Path classicArchive,
            Path ruArchive,
            Path stagingRoot,
            Path siteJsDir,
            boolean generateOnly,
            boolean includeClassic,
            boolean includeRu) throws IOException {

        if (!includeClassic && !includeRu) {
            throw new IOException("Nessun modulo selezionato per la generazione JS");
        }
        if (includeClassic) requireDirectory(classicArchive, "Archivio classic");
        if (includeRu) requireDirectory(ruArchive, "Archivio RU");
        Files.createDirectories(stagingRoot);

        String runId = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                + "_" + UUID.randomUUID().toString().substring(0, 8);
        Path runDir = stagingRoot.resolve("records2026_" + runId);
        Path generatedDir = runDir.resolve("js");
        Files.createDirectories(generatedDir);

        int classicEntries = 0;
        int ruSeasons = 0;
        int annualFiles = 0;
        if (includeClassic) {
            var classic = Records2026ClassicJsExporter.export(
                    classicArchive, generatedDir.resolve(CLASSIC_FILE), List.of());
            classicEntries = classic.entryCount();
        }
        if (includeRu) {
            var ru = Records2026RuJsExporter.export(ruArchive, generatedDir);
            ruSeasons = ru.seasons();
            annualFiles = ru.annualFiles();
        }

        ValidationResult validation = validateGenerated(generatedDir, annualFiles, includeClassic, includeRu);
        int published = 0;
        if (!generateOnly) {
            Files.createDirectories(siteJsDir);
            published = publishWithRollback(generatedDir, siteJsDir, validation.files());
        }
        return new PublishResult(classicEntries, ruSeasons, annualFiles,
                validation.files().size(), published, runDir);
    }

    private static ValidationResult validateGenerated(Path generatedDir, int expectedAnnualFiles,
            boolean includeClassic, boolean includeRu) throws IOException {

        List<Path> files;
        try (var stream = Files.list(generatedDir)) {
            files = stream
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .toList();
        }

        Map<String, Path> byName = new LinkedHashMap<>();
        for (Path file : files) {
            byName.put(file.getFileName().toString(), file);
        }

        if (includeClassic) {
            requireFile(byName, CLASSIC_FILE);
            validatePrefix(byName.get(CLASSIC_FILE), "window.RECORDS2026_PREVIEW_CLASSIC");
        }
        List<Path> annuals = files.stream().filter(Records2026SitePublisher::isAnnualFile).toList();
        if (includeRu) {
            requireFile(byName, RU_FILE);
            requireFile(byName, MANIFEST_FILE);
            if (annuals.size() != expectedAnnualFiles) {
                throw new IOException("Numero file annuali inatteso: " + annuals.size()
                        + ", attesi " + expectedAnnualFiles);
            }
            validatePrefix(byName.get(RU_FILE), "window.RECORDS2026_PREVIEW_RU");
            validatePrefix(byName.get(MANIFEST_FILE), "window.RECORDS2026_STORICO_RU_MANIFEST");
            for (Path annual : annuals) validateContains(annual, "window.RECORDS2026_STORICO_RU");
        } else if (!annuals.isEmpty()) {
            throw new IOException("File RU annuali generati nonostante il modulo RU sia disattivato");
        }
        int expectedTotal = (includeClassic ? 1 : 0) + (includeRu ? expectedAnnualFiles + 2 : 0);
        if (files.size() != expectedTotal) {
            throw new IOException("Numero file JS inatteso: " + files.size()
                    + ", attesi " + expectedTotal);
        }

        return new ValidationResult(files);
    }

    private static int publishWithRollback(Path generatedDir, Path siteJsDir, List<Path> generatedFiles)
            throws IOException {

        Path transactionDir = generatedDir.getParent().resolve("publish-transaction");
        Path backupDir = transactionDir.resolve("backup");
        Files.createDirectories(backupDir);

        List<String> replacedNames = new ArrayList<>();
        List<String> newlyCreatedNames = new ArrayList<>();

        try {
            for (Path source : generatedFiles) {
                String name = source.getFileName().toString();
                Path target = siteJsDir.resolve(name);
                Path backup = backupDir.resolve(name);

                if (Files.exists(target)) {
                    Files.copy(target, backup,
                            StandardCopyOption.REPLACE_EXISTING,
                            StandardCopyOption.COPY_ATTRIBUTES);
                    replacedNames.add(name);
                } else {
                    newlyCreatedNames.add(name);
                }

                Path temp = siteJsDir.resolve("." + name + ".recordsnext-" + UUID.randomUUID() + ".tmp");
                Files.copy(source, temp, StandardCopyOption.REPLACE_EXISTING);
                moveReplace(temp, target);
            }
        } catch (Exception publicationFailure) {
            IOException rollbackFailure = rollback(siteJsDir, backupDir, replacedNames, newlyCreatedNames);
            if (rollbackFailure != null) {
                publicationFailure.addSuppressed(rollbackFailure);
            }
            if (publicationFailure instanceof IOException io) {
                throw io;
            }
            throw new IOException("Pubblicazione fallita", publicationFailure);
        }

        return generatedFiles.size();
    }

    private static IOException rollback(
            Path siteJsDir,
            Path backupDir,
            List<String> replacedNames,
            List<String> newlyCreatedNames) {

        IOException firstFailure = null;

        for (String name : newlyCreatedNames) {
            try {
                Files.deleteIfExists(siteJsDir.resolve(name));
            } catch (IOException ex) {
                if (firstFailure == null) firstFailure = ex;
                else firstFailure.addSuppressed(ex);
            }
        }

        for (String name : replacedNames) {
            try {
                Path backup = backupDir.resolve(name);
                Path temp = siteJsDir.resolve("." + name + ".rollback-" + UUID.randomUUID() + ".tmp");
                Files.copy(backup, temp, StandardCopyOption.REPLACE_EXISTING);
                moveReplace(temp, siteJsDir.resolve(name));
            } catch (IOException ex) {
                if (firstFailure == null) firstFailure = ex;
                else firstFailure.addSuppressed(ex);
            }
        }

        return firstFailure;
    }

    private static void moveReplace(Path source, Path target) throws IOException {
        try {
            Files.move(source, target,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void requireDirectory(Path path, String label) throws IOException {
        if (!Files.isDirectory(path)) {
            throw new IOException(label + " inesistente o non valida: " + path);
        }
    }

    private static void requireFile(Map<String, Path> files, String name) throws IOException {
        if (!files.containsKey(name)) {
            throw new IOException("File generato mancante: " + name);
        }
    }

    private static boolean isAnnualFile(Path path) {
        String name = path.getFileName().toString();
        return name.startsWith(ANNUAL_PREFIX)
                && name.endsWith(ANNUAL_SUFFIX)
                && !name.equals(MANIFEST_FILE);
    }

    private static void validatePrefix(Path path, String expectedPrefix) throws IOException {
        String sample = readStart(path, 4096);
        if (!stripBom(sample).stripLeading().startsWith(expectedPrefix)) {
            throw new IOException("Prefisso JS non valido in " + path.getFileName()
                    + ": atteso " + expectedPrefix);
        }
    }

    private static void validateContains(Path path, String expectedToken) throws IOException {
        String sample = readStart(path, 8192);
        if (!stripBom(sample).contains(expectedToken)) {
            throw new IOException("Token JS non trovato in " + path.getFileName()
                    + ": " + expectedToken);
        }
    }

    private static String readStart(Path path, int maxBytes) throws IOException {
        long size = Files.size(path);
        if (size <= 16) {
            throw new IOException("File generato vuoto o troppo corto: " + path);
        }
        byte[] bytes = new byte[(int) Math.min(size, maxBytes)];
        try (var input = Files.newInputStream(path)) {
            int offset = 0;
            while (offset < bytes.length) {
                int read = input.read(bytes, offset, bytes.length - offset);
                if (read < 0) break;
                offset += read;
            }
            return new String(bytes, 0, offset, StandardCharsets.UTF_8);
        }
    }

    private static String stripBom(String value) {
        return !value.isEmpty() && value.charAt(0) == '\uFEFF' ? value.substring(1) : value;
    }

    public record PublishResult(
            int classicEntries,
            int ruSeasons,
            int annualFiles,
            int validatedFiles,
            int publishedFiles,
            Path stagingDirectory) {
    }

    private record ValidationResult(List<Path> files) {
    }
}
```

### src\main\java\it\alterlega\recordsnext\RiserveUfficioArchiveBuilder.java

```java
package it.alterlega.recordsnext;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

/** Costruisce riserveufficio.json dai season_normalized_*.json RecordsNext. */
public final class RiserveUfficioArchiveBuilder {
    private RiserveUfficioArchiveBuilder() {}

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Uso: RiserveUfficioArchiveBuilder <reportsRoot> <archiveRoot> [stagione ...]");
            System.exit(2);
        }
        Path reports = Path.of(args[0]).toAbsolutePath().normalize();
        Path archive = Path.of(args[1]).toAbsolutePath().normalize();
        List<String> seasons = new ArrayList<>();
        for (int i = 2; i < args.length; i++) if (!args[i].isBlank()) seasons.add(args[i].trim());
        Result r = build(reports, archive, seasons);
        System.out.println("Report       : " + reports);
        System.out.println("Archivio RU  : " + archive);
        System.out.println("Stagioni     : " + r.seasons());
        System.out.println("File letti   : " + r.files());
        System.out.println("Righe RU     : " + r.reserveRows());
        System.out.println("Viste        : 12/12");
    }

    public static Result build(Path reportsRoot, Path archiveRoot, List<String> requested) throws IOException {
        if (!Files.isDirectory(reportsRoot)) throw new IOException("Cartella report non trovata: " + reportsRoot);
        Files.createDirectories(archiveRoot);
        int seasonCount = 0, fileCount = 0, rowCount = 0;
        for (Path seasonDir : resolveSeasonDirs(reportsRoot, requested)) {
            List<Path> files = listNormalizedFiles(seasonDir);
            if (files.isEmpty()) continue;
            String season = seasonDir.getFileName().toString();
            Map<String,Object> payload = buildSeason(season, files);
            Path outDir = archiveRoot.resolve(season);
            Files.createDirectories(outDir);
            Files.writeString(outDir.resolve("riserveufficio.json"), JsonWriter.writePretty(payload) + System.lineSeparator(),
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            seasonCount++; fileCount += files.size();
            rowCount += rows(object(payload.get("dettaglio")).get("ruDettaglio")).size();
        }
        if (seasonCount == 0) throw new IOException("Nessuna stagione normalizzata trovata");
        return new Result(seasonCount, fileCount, rowCount);
    }

    private static Map<String,Object> buildSeason(String season, List<Path> files) throws IOException {
        List<Map<String,Object>> ruDetail = new ArrayList<>();
        Map<String,Map<String,Object>> matchByKey = new LinkedHashMap<>();
        Map<String,List<Map<String,Object>>> bandsByCompetition = new LinkedHashMap<>();
        Set<String> competitions = new TreeSet<>();

        for (Path file : files) {
            Map<String,Object> doc = object(parse(file));
            Map<String,Object> meta = object(doc.get("meta"));
            String compName = publicCompetitionName(s(meta.get("competizioneNome")));
            String compId = s(meta.get("idCompetizioneFcm"));
            competitions.add(compName);
            List<Map<String,Object>> matches = rows(doc.get("partiteSquadra"));
            for (Map<String,Object> m : matches) {
                if (n(m.get("idSquadra")) == 0) continue;
                matchByKey.put(s(m.get("idIncontro")) + "|" + s(m.get("idSquadra")), m);
            }
            bandsByCompetition.put(compId, rows(doc.get("fasceGolDettaglio")));
            for (Map<String,Object> raw : rows(doc.get("riserveUfficioDettaglio"))) {
                Map<String,Object> match = matchByKey.get(s(raw.get("idIncontro")) + "|" + s(raw.get("idSquadra")));
                if (match == null) continue;
                ruDetail.add(detailRow(raw, match, compName));
            }
        }

        ruDetail.sort(compare("competizione", "giornataDiA", "idIncontro", "idSquadra", "ordine"));
        List<Map<String,Object>> teamMatch = teamMatch(ruDetail);
        List<Map<String,Object>> against = against(teamMatch, matchByKey);
        List<Map<String,Object>> decisive = new ArrayList<>(), decisiveAgainst = new ArrayList<>();
        calculateDecisive(teamMatch, bandsByCompetition, decisive, decisiveAgainst);

        Map<String,Object> views = new LinkedHashMap<>();
        views.put("partiteConPiuRU", matchesWithMostRu(ruDetail));
        views.put("partiteConRU", sorted(teamMatch, compare("competizione", "giornataDiA", "squadra")));
        views.put("partiteControRU", sorted(against, compare("competizione", "giornataDiA", "squadra")));
        views.put("ruDecisiva", decisive);
        views.put("bilancioRUDecisiva", decisiveBalance(decisive));
        views.put("ruDecisivaContro", decisiveAgainst);
        views.put("bilancioRUDecisivaContro", decisiveAgainstBalance(decisiveAgainst));
        List<Map<String,Object>> balanceWith = balance(teamMatch, true);
        List<Map<String,Object>> balanceAgainst = balance(against, false);
        views.put("bilancioConRU", balanceWith);
        views.put("bilancioControRU", balanceAgainst);
        views.put("mediaPuntiConRU", averagePoints(balanceWith, true));
        views.put("mediaPuntiControRU", averagePoints(balanceAgainst, false));
        views.put("tipoRUUsata", typeUsed(ruDetail));

        Map<String,Object> detail = new LinkedHashMap<>();
        detail.put("ruDettaglio", ruDetail);
        detail.put("ruTeamMatch", teamMatch);

        stripInternalFields(views);
        stripInternalFields(detail);
        Map<String,Object> meta = linked(
                "titolo", "Riserve d'Ufficio",
                "stagione", season,
                "generato", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                "builder", "RecordsNext RiserveUfficioArchiveBuilder");
        Map<String,Object> out = new LinkedHashMap<>();
        out.put("meta", meta);
        out.put("competizioni", canonicalCompetitions());
        out.put("curiosita", curiosityDefinitions());
        out.put("views", views);
        out.put("dettaglio", detail);
        return out;
    }

    private static Map<String,Object> detailRow(Map<String,Object> r, Map<String,Object> m, String compName) {
        String side = s(m.get("lato"));
        String result = n(m.get("golFatti")) + "-" + n(m.get("golSubiti"));
        String score = plainText(m.get("puntiFatti")) + "-" + plainText(m.get("puntiSubiti"));
        String team = s(r.get("squadra"));
        String opponent = s(r.get("avversaria"));
        String homeTeam = "CASA".equalsIgnoreCase(side) ? team : opponent;
        String awayTeam = "CASA".equalsIgnoreCase(side) ? opponent : team;
        Object homePoints = "CASA".equalsIgnoreCase(side) ? m.get("puntiFatti") : m.get("puntiSubiti");
        Object awayPoints = "CASA".equalsIgnoreCase(side) ? m.get("puntiSubiti") : m.get("puntiFatti");
        int homeGoals = "CASA".equalsIgnoreCase(side) ? n(m.get("golFatti")) : n(m.get("golSubiti"));
        int awayGoals = "CASA".equalsIgnoreCase(side) ? n(m.get("golSubiti")) : n(m.get("golFatti"));
        return linked(
                "idIncontro", s(r.get("idIncontro")), "competizione", compName,
                "girone", publicGroupName(compName, m.get("gironeNome"), m.get("idGirone")), "giornataFCM", m.get("giornata"),
                "giornataDiA", m.get("giornataDiA"), "urlTabellino", m.get("urlTabellino"),
                "idSquadra", s(r.get("idSquadra")), "squadra", r.get("squadra"),
                "idAvversaria", s(r.get("idAvversaria")), "avversaria", r.get("avversaria"),
                "tipoRU", r.get("tipoRU"), "ruoloRU", r.get("ruoloRU"), "valoreRU", r.get("valoreRU"),
                "ordine", r.get("ordine"), "votoTabellino", r.get("votoTabellino"),
                "modifTabellino", r.get("modifTabellino"), "totTabellino", r.get("totTabellino"),
                "puntiSquadra", m.get("puntiFatti"), "puntiAvversaria", m.get("puntiSubiti"),
                "golSquadra", m.get("golFatti"), "golAvversaria", m.get("golSubiti"),
                "risultato", result, "punteggio", score, "esito", legacyOutcome(s(m.get("esito"))),
                "_idCompetizioneFcm", m.get("idCompetizioneFcm"), "_lato", side,
                "_squadraCasa", homeTeam, "_squadraFuori", awayTeam,
                "_puntiCasa", homePoints, "_puntiFuori", awayPoints,
                "_golCasa", homeGoals, "_golFuori", awayGoals);
    }

    private static List<Map<String,Object>> teamMatch(List<Map<String,Object>> detail) {
        Map<String,List<Map<String,Object>>> groups = group(detail, r -> s(r.get("idIncontro")) + "|" + s(r.get("idSquadra")));
        List<Map<String,Object>> out = new ArrayList<>();
        for (List<Map<String,Object>> g : groups.values()) {
            Map<String,Object> f = g.get(0); LinkedHashSet<String> types = new LinkedHashSet<>();
            BigDecimal total = BigDecimal.ZERO; List<String> pieces = new ArrayList<>();
            for (Map<String,Object> r : g) { String type=s(r.get("tipoRU")); types.add(type); total=total.add(bd(r.get("valoreRU"))); pieces.add(type+"="+plain(r.get("valoreRU"))); }
            Map<String,Object> row = copyFields(f, "idIncontro","competizione","girone","giornataFCM","giornataDiA","urlTabellino","idSquadra","squadra","idAvversaria","avversaria");
            row.put("numeroRU", g.size()); row.put("valoreRUTotale", clean(total)); row.put("tipiRU", String.join(", ", types)); row.put("dettaglioRU", String.join("; ", pieces));
            copyInto(row, f, "puntiSquadra","puntiAvversaria","golSquadra","golAvversaria","risultato","punteggio","esito",
                    "_idCompetizioneFcm","_lato","_squadraCasa","_squadraFuori","_puntiCasa","_puntiFuori","_golCasa","_golFuori"); out.add(row);
        }
        return out;
    }

    private static List<Map<String,Object>> against(List<Map<String,Object>> teamMatch, Map<String,Map<String,Object>> matchByKey) {
        List<Map<String,Object>> out = new ArrayList<>();
        for (Map<String,Object> ru : teamMatch) {
            Map<String,Object> opp = matchByKey.get(s(ru.get("idIncontro")) + "|" + s(ru.get("idAvversaria")));
            if (opp == null) continue;
            out.add(linked("idIncontro",ru.get("idIncontro"),"competizione",ru.get("competizione"),"girone",ru.get("girone"),"giornataFCM",opp.get("giornata"),"giornataDiA",opp.get("giornataDiA"),"urlTabellino",opp.get("urlTabellino"),
                    "idSquadra",s(opp.get("idSquadra")),"squadra",opp.get("squadra"),"idAvversaria",ru.get("idSquadra"),"avversaria",ru.get("squadra"),"avversariaConRU",ru.get("squadra"),
                    "numeroRUAvversaria",ru.get("numeroRU"),"valoreRUAvversaria",ru.get("valoreRUTotale"),"tipiRUAvversaria",ru.get("tipiRU"),"dettaglioRUAvversaria",ru.get("dettaglioRU"),
                    "puntiSquadra",opp.get("puntiFatti"),"puntiAvversaria",opp.get("puntiSubiti"),"golSquadra",opp.get("golFatti"),"golAvversaria",opp.get("golSubiti"),
                    "risultato",n(opp.get("golFatti"))+"-"+n(opp.get("golSubiti")),
                    "punteggio",plainText(opp.get("puntiFatti"))+"-"+plainText(opp.get("puntiSubiti")),
                    "esito",legacyOutcome(s(opp.get("esito")))));
        }
        return out;
    }

    private static void calculateDecisive(List<Map<String,Object>> teamMatch, Map<String,List<Map<String,Object>>> bands, List<Map<String,Object>> yes, List<Map<String,Object>> against) {
        for (Map<String,Object> ru : teamMatch) {
            BigDecimal with = bd(ru.get("puntiSquadra")), value = bd(ru.get("valoreRUTotale")), without = with.subtract(value);
            int goalsWith=n(ru.get("golSquadra")), goalsOpp=n(ru.get("golAvversaria"));
            int goalsWithout=goals(without, bands.getOrDefault(s(ru.get("_idCompetizioneFcm")), List.of()));
            String resultWith=result(goalsWith, goalsOpp), resultWithout=result(goalsWithout, goalsOpp);
            int gained=points(resultWith)-points(resultWithout); if (gained<=0) continue;
            Map<String,Object> y=copyFields(ru,"idIncontro","competizione","girone","giornataFCM","giornataDiA","urlTabellino","idSquadra","squadra","idAvversaria","avversaria","tipiRU","dettaglioRU");
            y.put("valoreRU",clean(value)); y.put("puntiConRU",clean(with)); y.put("puntiSenzaRU",clean(without)); y.put("puntiAvversaria",ru.get("puntiAvversaria")); y.put("golConRU",goalsWith); y.put("golSenzaRU",goalsWithout); y.put("golAvversaria",goalsOpp); y.put("esitoSenzaRU",resultWithout); y.put("esitoConRU",resultWith); y.put("effetto",effect(resultWithout,resultWith)); y.put("puntiClassificaGuadagnati",gained); y.put("risultatoReale",ru.get("risultato")); y.put("punteggioReale",ru.get("punteggio")); yes.add(y);
            String oppWithout=result(goalsOpp, goalsWithout), oppWith=result(goalsOpp, goalsWith); int lost=points(oppWithout)-points(oppWith);
            against.add(linked("idIncontro",ru.get("idIncontro"),"competizione",ru.get("competizione"),"girone",ru.get("girone"),"giornataFCM",ru.get("giornataFCM"),"giornataDiA",ru.get("giornataDiA"),"urlTabellino",ru.get("urlTabellino"),
                    "idSquadra",ru.get("idAvversaria"),"squadra",ru.get("avversaria"),"idAvversaria",ru.get("idSquadra"),"avversaria",ru.get("squadra"),"avversariaConRU",ru.get("squadra"),"tipiRUAvversaria",ru.get("tipiRU"),"dettaglioRUAvversaria",ru.get("dettaglioRU"),"valoreRUAvversaria",clean(value),
                    "puntiSquadra",ru.get("puntiAvversaria"),"puntiAvversariaConRU",clean(with),"puntiAvversariaSenzaRU",clean(without),"golSquadra",goalsOpp,"golAvversariaConRU",goalsWith,"golAvversariaSenzaRU",goalsWithout,
                    "esitoSenzaRUAvversaria",oppWithout,"esitoConRUAvversaria",oppWith,"danno",damage(oppWithout,oppWith),"puntiClassificaPersi",lost,"risultatoReale",goalsOpp+"-"+goalsWith,"punteggioReale",plain(ru.get("puntiAvversaria"))+"-"+plain(with)));
        }
    }

    private static List<Map<String,Object>> matchesWithMostRu(List<Map<String,Object>> detail) {
        List<Map<String,Object>> out = new ArrayList<>();
        for (List<Map<String,Object>> g : group(detail, r -> s(r.get("idIncontro"))).values()) {
            g.sort(Comparator.<Map<String,Object>,BigDecimal>comparing(r -> bd(r.get("idSquadra"))).thenComparing(r -> bd(r.get("ordine"))));
            Map<String,Object> f = g.get(0);
            BigDecimal total = BigDecimal.ZERO;
            List<String> pieces = new ArrayList<>();
            for (Map<String,Object> r : g) {
                total = total.add(bd(r.get("valoreRU")));
                pieces.add(r.get("squadra") + ":" + r.get("tipoRU") + "=" + plain(r.get("valoreRU")));
            }
            out.add(linked("idIncontro",f.get("idIncontro"),"competizione",f.get("competizione"),"girone",f.get("girone"),
                    "giornataFCM",f.get("giornataFCM"),"giornataDiA",f.get("giornataDiA"),
                    "partita",f.get("squadra")+" - "+f.get("avversaria"),
                    "numeroRU",g.size(),"valoreRUTotale",clean(total),"dettaglioRU",String.join("; ",pieces),
                    "risultato",f.get("risultato"),
                    "punteggio",plain(f.get("puntiSquadra"))+"-"+plain(f.get("puntiAvversaria"))));
        }
        out.sort(compareDesc("numeroRU","valoreRUTotale","competizione","giornataDiA"));
        return out;
    }

    private static List<Map<String,Object>> balance(List<Map<String,Object>> source, boolean with) {
        List<Map<String,Object>> out=new ArrayList<>();
        for(List<Map<String,Object>> g:group(source,r->s(r.get("idSquadra"))).values()){Map<String,Object> f=g.get(0);int v=0,d=0,l=0;BigDecimal pf=BigDecimal.ZERO,pa=BigDecimal.ZERO,gf=BigDecimal.ZERO,ga=BigDecimal.ZERO;for(Map<String,Object> r:g){switch(s(r.get("esito"))){case"V"->v++;case"N"->d++;case"P"->l++;}pf=pf.add(bd(r.get("puntiSquadra")));pa=pa.add(bd(r.get("puntiAvversaria")));gf=gf.add(bd(r.get("golSquadra")));ga=ga.add(bd(r.get("golAvversaria")));}int count=g.size();
            out.add(linked("idSquadra",f.get("idSquadra"),"squadra",f.get("squadra"),with?"partiteConRU":"partiteControRU",count,"V",v,"N",d,"P",l,"percV",percent(v,count),"percN",percent(d,count),"percP",percent(l,count),"mediaPuntiSquadra",average(pf,count),"mediaPuntiAvversaria",average(pa,count),"mediaGolSquadra",average(gf,count),"mediaGolAvversaria",average(ga,count)));}
        out.sort(Comparator.<Map<String,Object>>comparingInt(r->n(r.get(with?"partiteConRU":"partiteControRU"))).reversed().thenComparing(r->s(r.get("squadra"))));return out;
    }

    private static List<Map<String,Object>> averagePoints(List<Map<String,Object>> balance, boolean with){List<Map<String,Object>>out=new ArrayList<>();for(Map<String,Object>r:balance)out.add(linked("idSquadra",r.get("idSquadra"),"squadra",r.get("squadra"),with?"partiteConRU":"partiteControRU",r.get(with?"partiteConRU":"partiteControRU"),"mediaPuntiSquadra",r.get("mediaPuntiSquadra"),"mediaPuntiAvversaria",r.get("mediaPuntiAvversaria"),"differenzaMedia",clean(bd(r.get("mediaPuntiSquadra")).subtract(bd(r.get("mediaPuntiAvversaria"))))));out.sort(Comparator.<Map<String,Object>,BigDecimal>comparing(r->bd(r.get("mediaPuntiSquadra"))).reversed().thenComparing(r->s(r.get("squadra"))));return out;}

    private static List<Map<String,Object>> typeUsed(List<Map<String,Object>> detail){List<Map<String,Object>>out=new ArrayList<>();for(List<Map<String,Object>>g:group(detail,r->s(r.get("idSquadra"))).values()){Map<String,Object>f=g.get(0);Map<String,Integer>c=new HashMap<>();Map<String,BigDecimal>v=new HashMap<>();for(String t:List.of("PU","DU","CU","AU")){c.put(t,0);v.put(t,BigDecimal.ZERO);}for(Map<String,Object>r:g){String t=s(r.get("tipoRU"));c.put(t,c.getOrDefault(t,0)+1);v.put(t,v.getOrDefault(t,BigDecimal.ZERO).add(bd(r.get("valoreRU"))));}out.add(linked("idSquadra",f.get("idSquadra"),"squadra",f.get("squadra"),"PU",c.get("PU"),"DU",c.get("DU"),"CU",c.get("CU"),"AU",c.get("AU"),"totaleRU",g.size(),"valorePU",clean(v.get("PU")),"valoreDU",clean(v.get("DU")),"valoreCU",clean(v.get("CU")),"valoreAU",clean(v.get("AU")),"valoreTotale",clean(v.values().stream().reduce(BigDecimal.ZERO,BigDecimal::add))));}out.sort(Comparator.<Map<String,Object>>comparingInt(r->n(r.get("totaleRU"))).reversed().thenComparing(r->s(r.get("squadra"))));return out;}

    private static List<Map<String,Object>> decisiveBalance(List<Map<String,Object>> rows){List<Map<String,Object>>out=new ArrayList<>();for(List<Map<String,Object>>g:group(rows,r->s(r.get("idSquadra"))).values()){Map<String,Object>f=g.get(0);int w=(int)g.stream().filter(r->"V".equals(s(r.get("esitoConRU")))).count(),d=(int)g.stream().filter(r->"N".equals(s(r.get("esitoConRU")))).count();out.add(linked("idSquadra",f.get("idSquadra"),"squadra",f.get("squadra"),"partiteRUDecisiva",g.size(),"vittorieGrazieRU",w,"pareggiGrazieRU",d,"puntiClassificaGuadagnati",g.stream().mapToInt(r->n(r.get("puntiClassificaGuadagnati"))).sum()));}out.sort(compareDesc("partiteRUDecisiva","puntiClassificaGuadagnati","squadra"));return out;}
    private static List<Map<String,Object>> decisiveAgainstBalance(List<Map<String,Object>> rows){List<Map<String,Object>>out=new ArrayList<>();for(List<Map<String,Object>>g:group(rows,r->s(r.get("idSquadra"))).values()){Map<String,Object>f=g.get(0);int w=(int)g.stream().filter(r->"V".equals(s(r.get("esitoSenzaRUAvversaria")))&&!"V".equals(s(r.get("esitoConRUAvversaria")))).count(),d=(int)g.stream().filter(r->"N".equals(s(r.get("esitoSenzaRUAvversaria")))&&"P".equals(s(r.get("esitoConRUAvversaria")))).count();out.add(linked("idSquadra",f.get("idSquadra"),"squadra",f.get("squadra"),"partiteControRUDecisiva",g.size(),"vittoriePerse",w,"pareggiDiventatiSconfitte",d,"puntiClassificaPersi",g.stream().mapToInt(r->n(r.get("puntiClassificaPersi"))).sum()));}out.sort(compareDesc("partiteControRUDecisiva","puntiClassificaPersi","squadra"));return out;}

    private static int goals(BigDecimal score,List<Map<String,Object>>bands){int g=0;List<Map<String,Object>>sorted=new ArrayList<>(bands);sorted.sort(Comparator.comparing(r->bd(r.get("min"))));for(Map<String,Object>b:sorted)if(score.compareTo(bd(b.get("min")))>=0)g=n(b.get("gol"));return g;}
    private static String result(int a,int b){return a>b?"V":a<b?"P":"N";} private static int points(String r){return "V".equals(r)?3:"N".equals(r)?1:0;}
    private static String effect(String a,String b){if("P".equals(a)&&"N".equals(b))return"Da sconfitta a pareggio";if("N".equals(a)&&"V".equals(b))return"Da pareggio a vittoria";if("P".equals(a)&&"V".equals(b))return"Da sconfitta a vittoria";return"";}
    private static String damage(String a,String b){if("V".equals(a)&&"N".equals(b))return"Da vittoria a pareggio";if("N".equals(a)&&"P".equals(b))return"Da pareggio a sconfitta";if("V".equals(a)&&"P".equals(b))return"Da vittoria a sconfitta";return"";}

    @SuppressWarnings("unchecked")
    private static void stripInternalFields(Object value){
        if(value instanceof Map<?,?> map){
            ((Map<String,Object>)map).keySet().removeIf(k->k.startsWith("_"));
            for(Object child:((Map<String,Object>)map).values())stripInternalFields(child);
        }else if(value instanceof List<?> list){for(Object child:list)stripInternalFields(child);}
    }
    private static String legacyOutcome(String value){return switch(value){case "V"->"V";case "P"->"N";case "S"->"P";default->value;};}
    private static String plainText(Object value){return plain(value).replace('.',',');}
    private static String publicCompetitionName(String name){return switch(name){case "Coppa di Lega Serie A"->"Coppa Serie A";case "Coppa di Lega Serie B"->"Coppa Serie B";case "Coppa di Lega Serie C"->"Coppa Serie C";default->name;};}
    private static String publicGroupName(String competition,Object groupName,Object idGirone){String name=s(groupName).trim();if(!name.isEmpty())return name;return competition;}
    private static List<String> canonicalCompetitions(){return List.of("Coppa Serie A","Coppa Serie B","Coppa Serie C","Coppa tra le Coppe","Europa Pipps","Play Off - Play Out","Serie A","Serie B","Serie C","Supercoppa Serie A","Supercoppa Serie B","Supercoppa Serie C");}
    private static List<Object> curiosityDefinitions(){return List.of();}
    private static List<Path> resolveSeasonDirs(Path root,List<String> requested)throws IOException{List<Path>out=new ArrayList<>();if(requested.isEmpty()){try(Stream<Path>s=Files.list(root)){s.filter(Files::isDirectory).sorted().forEach(out::add);}}else for(String x:requested){Path p=root.resolve(x);if(!Files.isDirectory(p))throw new IOException("Stagione non trovata: "+p);out.add(p);}return out;}
    private static List<Path> listNormalizedFiles(Path dir)throws IOException{try(Stream<Path>s=Files.list(dir)){return s.filter(Files::isRegularFile).filter(p->{String n=p.getFileName().toString();return n.startsWith("season_normalized_")&&n.endsWith(".json")&&!n.contains(".stage")&&!n.contains(".final");}).sorted().toList();}}
    private static <T>List<T>sorted(List<T>src,Comparator<? super T>c){List<T>o=new ArrayList<>(src);o.sort(c);return o;}
    private static Comparator<Map<String,Object>> compare(String...f){return (a,b)->{for(String x:f){int c=cmp(a.get(x),b.get(x));if(c!=0)return c;}return 0;};}
    private static Comparator<Map<String,Object>> compareDesc(String...f){return (a,b)->{for(String x:f){int c=cmp(b.get(x),a.get(x));if(c!=0)return c;}return 0;};}
    private static int cmp(Object a,Object b){if(a instanceof Number||b instanceof Number)return bd(a).compareTo(bd(b));return s(a).compareTo(s(b));}
    private static Map<String,List<Map<String,Object>>>group(List<Map<String,Object>>rows,java.util.function.Function<Map<String,Object>,String>key){Map<String,List<Map<String,Object>>>m=new LinkedHashMap<>();for(Map<String,Object>r:rows)m.computeIfAbsent(key.apply(r),k->new ArrayList<>()).add(r);return m;}
    private static Map<String,Object>copyFields(Map<String,Object>s,String...f){Map<String,Object>o=new LinkedHashMap<>();copyInto(o,s,f);return o;}private static void copyInto(Map<String,Object>o,Map<String,Object>s,String...f){for(String x:f)o.put(x,s.get(x));}
    private static Map<String,Object>linked(Object...v){Map<String,Object>m=new LinkedHashMap<>();for(int i=0;i<v.length;i+=2)m.put(String.valueOf(v[i]),v[i+1]);return m;}
    private static Object average(BigDecimal total,int count){return count==0?0:clean(total.divide(BigDecimal.valueOf(count),2,RoundingMode.HALF_EVEN));}
    private static Object percent(int x,int total){return total==0?0:clean(BigDecimal.valueOf(x*100L).divide(BigDecimal.valueOf(total),1,RoundingMode.HALF_UP));}
    private static Object clean(BigDecimal x){BigDecimal z=x.setScale(Math.min(2,Math.max(0,x.scale())),RoundingMode.HALF_UP).stripTrailingZeros();return z.scale()<=0?z.longValue():z;}
    private static String plain(Object x){return bd(x).stripTrailingZeros().toPlainString();}private static BigDecimal bd(Object x){if(x==null||s(x).isBlank())return BigDecimal.ZERO;return x instanceof BigDecimal b?b:new BigDecimal(s(x).replace(',','.'));}private static int n(Object x){return bd(x).intValue();}private static String s(Object x){return x==null?"":String.valueOf(x);}
    @SuppressWarnings("unchecked")private static Map<String,Object>object(Object x){return x instanceof Map<?,?>?(Map<String,Object>)x:new LinkedHashMap<>();}@SuppressWarnings("unchecked")private static List<Map<String,Object>>rows(Object x){if(!(x instanceof List<?>l))return new ArrayList<>();List<Map<String,Object>>o=new ArrayList<>();for(Object r:l)if(r instanceof Map<?,?>)o.add((Map<String,Object>)r);return o;}

    private static Object parse(Path source) throws IOException {
        String text = Files.readString(source, StandardCharsets.UTF_8);
        if (!text.isEmpty() && text.charAt(0) == '\uFEFF') text = text.substring(1);
        return new JsonParser(text, source).parse();
    }

    private static final class JsonParser {
        private final String text; private final Path source; private int index;
        JsonParser(String text, Path source) { this.text = text; this.source = source; }
        Object parse() throws IOException { skip(); Object v = value(); skip(); if (index != text.length()) fail("contenuto dopo JSON"); return v; }
        private Object value() throws IOException {
            skip(); if (index >= text.length()) fail("fine inattesa"); char c = text.charAt(index);
            return switch (c) { case '{' -> object(); case '[' -> array(); case '"' -> string(); case 't' -> literal("true", true); case 'f' -> literal("false", false); case 'n' -> literal("null", null); default -> number(); };
        }
        private Map<String,Object> object() throws IOException { expect('{'); Map<String,Object> m=new LinkedHashMap<>(); skip(); if (take('}')) return m; while(true){ skip(); String k=string(); skip(); expect(':'); m.put(k,value()); skip(); if(take('}')) return m; expect(','); } }
        private List<Object> array() throws IOException { expect('['); List<Object> a=new ArrayList<>(); skip(); if(take(']')) return a; while(true){ a.add(value()); skip(); if(take(']')) return a; expect(','); } }
        private String string() throws IOException { expect('"'); StringBuilder b=new StringBuilder(); while(index<text.length()){ char c=text.charAt(index++); if(c=='"') return b.toString(); if(c!='\\'){ b.append(c); continue;} if(index>=text.length()) fail("escape incompleto"); char e=text.charAt(index++); switch(e){case '"','\\','/'->b.append(e); case 'b'->b.append('\b'); case 'f'->b.append('\f'); case 'n'->b.append('\n'); case 'r'->b.append('\r'); case 't'->b.append('\t'); case 'u'->{ if(index+4>text.length()) fail("unicode incompleto"); b.append((char)Integer.parseInt(text.substring(index,index+4),16)); index+=4;} default->fail("escape non valido");}} fail("stringa non chiusa"); return ""; }
        private Object number() throws IOException { int s=index; if(peek('-')) index++; digits(); boolean dec=false; if(peek('.')){dec=true;index++;digits();} if(peek('e')||peek('E')){dec=true;index++;if(peek('+')||peek('-'))index++;digits();} String raw=text.substring(s,index); try{ BigDecimal d=new BigDecimal(raw); if(!dec && d.scale()<=0) try{return d.longValueExact();}catch(ArithmeticException ignored){} return d.stripTrailingZeros(); }catch(Exception e){fail("numero non valido"); return null;} }
        private void digits() throws IOException { int s=index; while(index<text.length()&&Character.isDigit(text.charAt(index))) index++; if(index==s) fail("cifre attese"); }
        private Object literal(String l,Object v)throws IOException{ if(!text.startsWith(l,index)) fail("letterale non valido"); index+=l.length(); return v; }
        private void skip(){ while(index<text.length()&&Character.isWhitespace(text.charAt(index))) index++; }
        private boolean take(char c){ if(index<text.length()&&text.charAt(index)==c){index++;return true;} return false; }
        private void expect(char c)throws IOException{ if(!take(c)) fail("atteso '"+c+"'"); }
        private boolean peek(char c){ return index<text.length()&&text.charAt(index)==c; }
        private void fail(String m)throws IOException{ throw new IOException("JSON non valido in "+source+" posizione "+index+": "+m); }
    }

    private static final class JsonWriter {
        static String writePretty(Object v){ StringBuilder b=new StringBuilder(); write(v,b,0); return b.toString(); }
        private static void write(Object v,StringBuilder b,int depth){
            if(v==null){b.append("null");return;} if(v instanceof String s){quote(s,b);return;} if(v instanceof Boolean){b.append(v);return;} if(v instanceof Number n){b.append(n instanceof BigDecimal d?d.stripTrailingZeros().toPlainString():n);return;}
            if(v instanceof Map<?,?> m){ b.append('{'); if(!m.isEmpty()){ b.append('\n'); int i=0; for(var e:m.entrySet()){ indent(b,depth+1); quote(String.valueOf(e.getKey()),b); b.append(": "); write(e.getValue(),b,depth+1); if(++i<m.size()) b.append(','); b.append('\n'); } indent(b,depth);} b.append('}'); return; }
            if(v instanceof List<?> l){ b.append('['); if(!l.isEmpty()){ b.append('\n'); for(int i=0;i<l.size();i++){ indent(b,depth+1); write(l.get(i),b,depth+1); if(i+1<l.size()) b.append(','); b.append('\n'); } indent(b,depth);} b.append(']'); return; }
            quote(String.valueOf(v),b);
        }
        private static void indent(StringBuilder b,int d){ b.append("  ".repeat(d)); }
        private static void quote(String s,StringBuilder b){ b.append('"'); for(int i=0;i<s.length();i++){ char c=s.charAt(i); switch(c){case '"'->b.append("\\\"");case '\\'->b.append("\\\\");case '\b'->b.append("\\b");case '\f'->b.append("\\f");case '\n'->b.append("\\n");case '\r'->b.append("\\r");case '\t'->b.append("\\t");default->{if(c<0x20)b.append(String.format("\\u%04x",(int)c));else b.append(c);}}} b.append('"'); }
    }

    public record Result(int seasons,int files,int reserveRows){}
}
```

### src\main\java\it\alterlega\recordsnext\SeasonMappingConfigurator.java

```java
package it.alterlega.recordsnext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Configuratore transazionale delle associazioni storiche di squadre e
 * competizioni. Opera esclusivamente sul database SQLite gia popolato da
 * RawSqliteImporter e ConfigurationSchema.
 */
public final class SeasonMappingConfigurator {

    private SeasonMappingConfigurator() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            printUsage();
            System.exit(2);
        }

        Path database = Path.of(args[0]).toAbsolutePath().normalize();
        if (!Files.isRegularFile(database)) {
            throw new IllegalArgumentException("Database non trovato: " + database);
        }

        Class.forName("org.sqlite.JDBC");

        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + database)) {

            configureConnection(connection);
            requireSchema(connection);

            String command = args[1].trim().toLowerCase(Locale.ROOT);
            switch (command) {
                case "show-seasons" -> showSeasons(connection, args);
                case "pending" -> showPending(connection, args);
                case "proposals" -> showProposals(connection, args);
                case "validate" -> validateSeason(connection, args, true);
                case "auto-exact" -> autoExact(connection, args);
                case "associate-team" -> associateTeam(connection, args);
                case "new-team" -> createTeamIdentity(connection, args);
                case "associate-competition" -> associateCompetition(connection, args);
                case "new-competition" -> createCompetitionIdentity(connection, args);
                default -> {
                    printUsage();
                    System.exit(2);
                }
            }
        }
    }

    private static void configureConnection(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("PRAGMA busy_timeout = 10000");
        }
    }

    private static void requireSchema(Connection connection) throws Exception {
        String[] required = {
            "rn_season",
            "rn_source_file",
            "rn_competition_season",
            "rn_team_season",
            "rn_competition_identity",
            "rn_team_identity",
            "rn_competition_mapping",
            "rn_team_mapping"
        };

        for (String table : required) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT COUNT(*) FROM sqlite_master "
                        + "WHERE type = 'table' AND name = ?")) {
                statement.setString(1, table);
                try (ResultSet result = statement.executeQuery()) {
                    result.next();
                    if (result.getInt(1) != 1) {
                        throw new IllegalStateException(
                            "Schema RecordsNext incompleto: tabella mancante " + table
                        );
                    }
                }
            }
        }
    }

    private static void showSeasons(Connection connection, String[] args)
            throws Exception {
        requireArgCount(args, 2, "<db> show-seasons");

        String sql = """
            SELECT
                s.season_id,
                s.is_anchor,
                (SELECT COUNT(*) FROM rn_source_file f
                 WHERE f.season_id = s.season_id AND f.source_type = 'FCM') AS fcm,
                (SELECT COUNT(*) FROM rn_source_file f
                 WHERE f.season_id = s.season_id AND f.source_type = 'FCA') AS fca,
                (SELECT COUNT(*)
                 FROM rn_competition_mapping cm
                 JOIN rn_competition_season cs
                   ON cs.competition_season_id = cm.competition_season_id
                 WHERE cs.season_id = s.season_id
                   AND cm.mapping_status = 'DA_CONFIGURARE') AS pending_comp,
                (SELECT COUNT(*)
                 FROM rn_team_mapping tm
                 JOIN rn_team_season ts
                   ON ts.team_season_id = tm.team_season_id
                 WHERE ts.season_id = s.season_id
                   AND tm.mapping_status = 'DA_CONFIGURARE') AS pending_team
            FROM rn_season s
            ORDER BY COALESCE(s.sort_order, 0) DESC, s.season_id DESC
            """;

        System.out.printf(
            Locale.ROOT,
            "%-11s %-6s %3s %3s %6s %6s %-10s%n",
            "STAGIONE", "ANCORA", "FCM", "FCA", "COMP", "TEAM", "ESITO"
        );

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            while (result.next()) {
                int fcm = result.getInt("fcm");
                int fca = result.getInt("fca");
                int pendingComp = result.getInt("pending_comp");
                int pendingTeam = result.getInt("pending_team");
                String outcome = fcm == 1 && fca == 1
                    && pendingComp == 0 && pendingTeam == 0
                    ? "COMPLETA"
                    : "IN_CORSO";

                System.out.printf(
                    Locale.ROOT,
                    "%-11s %-6s %3d %3d %6d %6d %-10s%n",
                    result.getString("season_id"),
                    result.getInt("is_anchor") == 1 ? "SI" : "NO",
                    fcm,
                    fca,
                    pendingComp,
                    pendingTeam,
                    outcome
                );
            }
        }
    }

    private static void showPending(Connection connection, String[] args)
            throws Exception {
        requireArgCount(args, 3, "<db> pending <stagione>");
        String seasonId = requireSeason(connection, args[2]);

        System.out.println("COMPETIZIONI DA CONFIGURARE");
        printPendingCompetitions(connection, seasonId);
        System.out.println();
        System.out.println("SQUADRE DA CONFIGURARE");
        printPendingTeams(connection, seasonId);
    }

    private static void showProposals(Connection connection, String[] args)
            throws Exception {
        requireArgCount(args, 3, "<db> proposals <stagione>");
        String seasonId = requireSeason(connection, args[2]);

        System.out.println("COMPETIZIONI");
        printCompetitionProposals(connection, seasonId);
        System.out.println();
        System.out.println("SQUADRE");
        printTeamProposals(connection, seasonId);
    }

    private static void autoExact(Connection connection, String[] args)
            throws Exception {
        requireArgCount(args, 3, "<db> auto-exact <stagione>");
        String seasonId = requireSeason(connection, args[2]);

        runTransaction(connection, () -> {
            int competitions = applyUnambiguousExactCompetitionMappings(
                connection, seasonId
            );
            int teams = applyUnambiguousExactTeamMappings(connection, seasonId);
            System.out.println("Associazioni esatte non ambigue applicate");
            System.out.println("Competizioni: " + competitions);
            System.out.println("Squadre     : " + teams);
        });
    }

    private static void associateTeam(Connection connection, String[] args)
            throws Exception {
        requireArgCount(
            args,
            4,
            "<db> associate-team <team-season-id> <team-identity-id>"
        );
        long teamSeasonId = parsePositiveLong(args[2], "team-season-id");
        long teamIdentityId = parsePositiveLong(args[3], "team-identity-id");

        runTransaction(connection, () -> {
            SeasonEntity team = requireTeamSeason(connection, teamSeasonId);
            Identity identity = requireTeamIdentity(connection, teamIdentityId);
            requireIdentityAvailableForTeam(
                connection, team.seasonId(), teamSeasonId, teamIdentityId
            );
            updateTeamMapping(
                connection,
                teamSeasonId,
                teamIdentityId,
                "MANUAL",
                null
            );
            System.out.println(
                "Squadra associata: " + team.name() + " -> " + identity.name()
            );
        });
    }

    private static void createTeamIdentity(Connection connection, String[] args)
            throws Exception {
        requireArgCount(args, 3, "<db> new-team <team-season-id>");
        long teamSeasonId = parsePositiveLong(args[2], "team-season-id");

        runTransaction(connection, () -> {
            SeasonEntity team = requireTeamSeason(connection, teamSeasonId);
            requirePendingTeam(connection, teamSeasonId);
            long identityId = insertTeamIdentity(connection, team);
            updateTeamMapping(
                connection,
                teamSeasonId,
                identityId,
                "NEW_HISTORICAL_IDENTITY",
                null
            );
            System.out.println(
                "Nuova identita squadra: " + identityId + " | " + team.name()
            );
        });
    }

    private static void associateCompetition(Connection connection, String[] args)
            throws Exception {
        requireArgCount(
            args,
            4,
            "<db> associate-competition "
                + "<competition-season-id> <competition-identity-id>"
        );
        long competitionSeasonId = parsePositiveLong(
            args[2], "competition-season-id"
        );
        long competitionIdentityId = parsePositiveLong(
            args[3], "competition-identity-id"
        );

        runTransaction(connection, () -> {
            SeasonEntity competition = requireCompetitionSeason(
                connection, competitionSeasonId
            );
            Identity identity = requireCompetitionIdentity(
                connection, competitionIdentityId
            );
            requireIdentityAvailableForCompetition(
                connection,
                competition.seasonId(),
                competitionSeasonId,
                competitionIdentityId
            );
            updateCompetitionMapping(
                connection,
                competitionSeasonId,
                competitionIdentityId,
                "MANUAL",
                null
            );
            System.out.println(
                "Competizione associata: " + competition.name()
                    + " -> " + identity.name()
            );
        });
    }

    private static void createCompetitionIdentity(
            Connection connection,
            String[] args) throws Exception {
        requireArgCount(
            args,
            3,
            "<db> new-competition <competition-season-id>"
        );
        long competitionSeasonId = parsePositiveLong(
            args[2], "competition-season-id"
        );

        runTransaction(connection, () -> {
            SeasonEntity competition = requireCompetitionSeason(
                connection, competitionSeasonId
            );
            requirePendingCompetition(connection, competitionSeasonId);
            long identityId = insertCompetitionIdentity(connection, competition);
            updateCompetitionMapping(
                connection,
                competitionSeasonId,
                identityId,
                "NEW_HISTORICAL_IDENTITY",
                null
            );
            System.out.println(
                "Nuova identita competizione: " + identityId
                    + " | " + competition.name()
            );
        });
    }

    private static boolean validateSeason(
            Connection connection,
            String[] args,
            boolean print) throws Exception {
        requireArgCount(args, 3, "<db> validate <stagione>");
        String seasonId = requireSeason(connection, args[2]);
        Validation validation = validate(connection, seasonId);

        if (print) {
            System.out.println("Stagione       : " + seasonId);
            System.out.println("Sorgenti FCM   : " + validation.fcmSources());
            System.out.println("Sorgenti FCA   : " + validation.fcaSources());
            System.out.println("Comp. pendenti : " + validation.pendingCompetitions());
            System.out.println("Team pendenti  : " + validation.pendingTeams());
            System.out.println("Dup. competiz. : " + validation.duplicateCompetitions());
            System.out.println("Dup. squadre   : " + validation.duplicateTeams());
            System.out.println("Mapping orfani : " + validation.orphanMappings());
            System.out.println("ESITO          : "
                + (validation.valid() ? "VALIDA" : "NON VALIDA"));
        }

        if (!validation.valid()) {
            throw new IllegalStateException(
                "Configurazione stagione non valida: " + seasonId
            );
        }
        return true;
    }

    private static Validation validate(Connection connection, String seasonId)
            throws Exception {
        int fcm = count(connection, """
            SELECT COUNT(*) FROM rn_source_file
            WHERE season_id = ? AND source_type = 'FCM'
            """, seasonId);
        int fca = count(connection, """
            SELECT COUNT(*) FROM rn_source_file
            WHERE season_id = ? AND source_type = 'FCA'
            """, seasonId);
        int pendingCompetitions = count(connection, """
            SELECT COUNT(*)
            FROM rn_competition_mapping cm
            JOIN rn_competition_season cs
              ON cs.competition_season_id = cm.competition_season_id
            WHERE cs.season_id = ?
              AND cm.mapping_status = 'DA_CONFIGURARE'
            """, seasonId);
        int pendingTeams = count(connection, """
            SELECT COUNT(*)
            FROM rn_team_mapping tm
            JOIN rn_team_season ts
              ON ts.team_season_id = tm.team_season_id
            WHERE ts.season_id = ?
              AND tm.mapping_status = 'DA_CONFIGURARE'
            """, seasonId);
        int duplicateCompetitions = count(connection, """
            SELECT COUNT(*) FROM (
                SELECT cm.competition_identity_id
                FROM rn_competition_mapping cm
                JOIN rn_competition_season cs
                  ON cs.competition_season_id = cm.competition_season_id
                WHERE cs.season_id = ?
                  AND cm.mapping_status = 'ASSOCIATA'
                GROUP BY cm.competition_identity_id
                HAVING COUNT(*) > 1
            )
            """, seasonId);
        int duplicateTeams = count(connection, """
            SELECT COUNT(*) FROM (
                SELECT tm.team_identity_id
                FROM rn_team_mapping tm
                JOIN rn_team_season ts
                  ON ts.team_season_id = tm.team_season_id
                WHERE ts.season_id = ?
                  AND tm.mapping_status = 'ASSOCIATA'
                GROUP BY tm.team_identity_id
                HAVING COUNT(*) > 1
            )
            """, seasonId);
        int orphanMappings = count(connection, """
            SELECT
                (SELECT COUNT(*)
                 FROM rn_competition_mapping cm
                 JOIN rn_competition_season cs
                   ON cs.competition_season_id = cm.competition_season_id
                 LEFT JOIN rn_competition_identity ci
                   ON ci.competition_identity_id = cm.competition_identity_id
                 WHERE cs.season_id = ?
                   AND cm.mapping_status = 'ASSOCIATA'
                   AND ci.competition_identity_id IS NULL)
                +
                (SELECT COUNT(*)
                 FROM rn_team_mapping tm
                 JOIN rn_team_season ts
                   ON ts.team_season_id = tm.team_season_id
                 LEFT JOIN rn_team_identity ti
                   ON ti.team_identity_id = tm.team_identity_id
                 WHERE ts.season_id = ?
                   AND tm.mapping_status = 'ASSOCIATA'
                   AND ti.team_identity_id IS NULL)
            """, seasonId, seasonId);

        return new Validation(
            fcm,
            fca,
            pendingCompetitions,
            pendingTeams,
            duplicateCompetitions,
            duplicateTeams,
            orphanMappings
        );
    }

    private static void printPendingCompetitions(
            Connection connection,
            String seasonId) throws Exception {
        String sql = """
            SELECT cs.competition_season_id, cs.source_competition_id, cs.source_name
            FROM rn_competition_season cs
            JOIN rn_competition_mapping cm
              ON cm.competition_season_id = cs.competition_season_id
            WHERE cs.season_id = ?
              AND cm.mapping_status = 'DA_CONFIGURARE'
            ORDER BY cs.source_name COLLATE NOCASE, cs.competition_season_id
            """;
        printPending(connection, sql, seasonId);
    }

    private static void printPendingTeams(
            Connection connection,
            String seasonId) throws Exception {
        String sql = """
            SELECT ts.team_season_id, ts.source_team_id, ts.source_name
            FROM rn_team_season ts
            JOIN rn_team_mapping tm
              ON tm.team_season_id = ts.team_season_id
            WHERE ts.season_id = ?
              AND tm.mapping_status = 'DA_CONFIGURARE'
            ORDER BY ts.source_name COLLATE NOCASE, ts.team_season_id
            """;
        printPending(connection, sql, seasonId);
    }

    private static void printPending(
            Connection connection,
            String sql,
            String seasonId) throws Exception {
        int rows = 0;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, seasonId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    rows++;
                    System.out.printf(
                        Locale.ROOT,
                        "%d | sorgente=%d | %s%n",
                        result.getLong(1),
                        result.getLong(2),
                        result.getString(3)
                    );
                }
            }
        }
        if (rows == 0) {
            System.out.println("- nessuna -");
        }
    }

    private static void printCompetitionProposals(
            Connection connection,
            String seasonId) throws Exception {
        List<Identity> identities = readCompetitionIdentities(connection);
        String sql = """
            SELECT cs.competition_season_id, cs.source_name
            FROM rn_competition_season cs
            JOIN rn_competition_mapping cm
              ON cm.competition_season_id = cs.competition_season_id
            WHERE cs.season_id = ?
              AND cm.mapping_status = 'DA_CONFIGURARE'
            ORDER BY cs.source_name COLLATE NOCASE
            """;
        printProposals(connection, sql, seasonId, identities);
    }

    private static void printTeamProposals(
            Connection connection,
            String seasonId) throws Exception {
        List<Identity> identities = readTeamIdentities(connection);
        String sql = """
            SELECT ts.team_season_id, ts.source_name
            FROM rn_team_season ts
            JOIN rn_team_mapping tm
              ON tm.team_season_id = ts.team_season_id
            WHERE ts.season_id = ?
              AND tm.mapping_status = 'DA_CONFIGURARE'
            ORDER BY ts.source_name COLLATE NOCASE
            """;
        printProposals(connection, sql, seasonId, identities);
    }

    private static void printProposals(
            Connection connection,
            String sql,
            String seasonId,
            List<Identity> identities) throws Exception {
        int rows = 0;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, seasonId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    rows++;
                    long localId = result.getLong(1);
                    String localName = result.getString(2);
                    System.out.println(localId + " | " + localName);

                    identities.stream()
                        .map(identity -> new ScoredIdentity(
                            identity,
                            similarity(localName, identity.name())
                        ))
                        .sorted(
                            Comparator.comparingDouble(ScoredIdentity::score)
                                .reversed()
                                .thenComparingLong(value -> value.identity().id())
                        )
                        .limit(5)
                        .forEach(candidate -> System.out.printf(
                            Locale.ROOT,
                            "    %.3f | %d | %s%n",
                            candidate.score(),
                            candidate.identity().id(),
                            candidate.identity().name()
                        ));
                    System.out.println("    [NON GESTITA -> nuova identita storica]");
                }
            }
        }
        if (rows == 0) {
            System.out.println("- nessuna -");
        }
    }

    private static int applyUnambiguousExactTeamMappings(
            Connection connection,
            String seasonId) throws Exception {
        List<ExactCandidate> candidates = new ArrayList<>();
        String sql = """
            SELECT
                ts.team_season_id,
                MIN(ti.team_identity_id) AS identity_id,
                COUNT(*) AS candidate_count
            FROM rn_team_season ts
            JOIN rn_team_mapping tm
              ON tm.team_season_id = ts.team_season_id
            JOIN rn_team_identity ti
              ON LOWER(TRIM(ti.canonical_name)) = LOWER(TRIM(ts.source_name))
            WHERE ts.season_id = ?
              AND tm.mapping_status = 'DA_CONFIGURARE'
              AND NOT EXISTS (
                    SELECT 1
                    FROM rn_team_mapping used
                    JOIN rn_team_season used_ts
                      ON used_ts.team_season_id = used.team_season_id
                    WHERE used_ts.season_id = ts.season_id
                      AND used.mapping_status = 'ASSOCIATA'
                      AND used.team_identity_id = ti.team_identity_id
                )
            GROUP BY ts.team_season_id
            HAVING COUNT(*) = 1
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, seasonId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    candidates.add(new ExactCandidate(
                        result.getLong("team_season_id"),
                        result.getLong("identity_id")
                    ));
                }
            }
        }

        for (ExactCandidate candidate : candidates) {
            updateTeamMapping(
                connection,
                candidate.seasonEntityId(),
                candidate.identityId(),
                "EXACT_NAME",
                null
            );
        }
        return candidates.size();
    }

    private static int applyUnambiguousExactCompetitionMappings(
            Connection connection,
            String seasonId) throws Exception {
        List<ExactCandidate> candidates = new ArrayList<>();
        String sql = """
            SELECT
                cs.competition_season_id,
                MIN(ci.competition_identity_id) AS identity_id,
                COUNT(*) AS candidate_count
            FROM rn_competition_season cs
            JOIN rn_competition_mapping cm
              ON cm.competition_season_id = cs.competition_season_id
            JOIN rn_competition_identity ci
              ON LOWER(TRIM(ci.canonical_name)) = LOWER(TRIM(cs.source_name))
            WHERE cs.season_id = ?
              AND cm.mapping_status = 'DA_CONFIGURARE'
              AND NOT EXISTS (
                    SELECT 1
                    FROM rn_competition_mapping used
                    JOIN rn_competition_season used_cs
                      ON used_cs.competition_season_id = used.competition_season_id
                    WHERE used_cs.season_id = cs.season_id
                      AND used.mapping_status = 'ASSOCIATA'
                      AND used.competition_identity_id = ci.competition_identity_id
                )
            GROUP BY cs.competition_season_id
            HAVING COUNT(*) = 1
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, seasonId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    candidates.add(new ExactCandidate(
                        result.getLong("competition_season_id"),
                        result.getLong("identity_id")
                    ));
                }
            }
        }

        for (ExactCandidate candidate : candidates) {
            updateCompetitionMapping(
                connection,
                candidate.seasonEntityId(),
                candidate.identityId(),
                "EXACT_NAME",
                null
            );
        }
        return candidates.size();
    }

    private static void requireIdentityAvailableForTeam(
            Connection connection,
            String seasonId,
            long teamSeasonId,
            long teamIdentityId) throws Exception {
        int used = count(connection, """
            SELECT COUNT(*)
            FROM rn_team_mapping tm
            JOIN rn_team_season ts
              ON ts.team_season_id = tm.team_season_id
            WHERE ts.season_id = ?
              AND tm.mapping_status = 'ASSOCIATA'
              AND tm.team_identity_id = ?
              AND tm.team_season_id <> ?
            """, seasonId, teamIdentityId, teamSeasonId);
        if (used != 0) {
            throw new IllegalStateException(
                "Identita squadra gia usata nella stagione " + seasonId
                    + ": " + teamIdentityId
            );
        }
    }

    private static void requireIdentityAvailableForCompetition(
            Connection connection,
            String seasonId,
            long competitionSeasonId,
            long competitionIdentityId) throws Exception {
        int used = count(connection, """
            SELECT COUNT(*)
            FROM rn_competition_mapping cm
            JOIN rn_competition_season cs
              ON cs.competition_season_id = cm.competition_season_id
            WHERE cs.season_id = ?
              AND cm.mapping_status = 'ASSOCIATA'
              AND cm.competition_identity_id = ?
              AND cm.competition_season_id <> ?
            """, seasonId, competitionIdentityId, competitionSeasonId);
        if (used != 0) {
            throw new IllegalStateException(
                "Identita competizione gia usata nella stagione " + seasonId
                    + ": " + competitionIdentityId
            );
        }
    }

    private static void requirePendingTeam(Connection connection, long id)
            throws Exception {
        int count = count(connection, """
            SELECT COUNT(*) FROM rn_team_mapping
            WHERE team_season_id = ? AND mapping_status = 'DA_CONFIGURARE'
            """, id);
        if (count != 1) {
            throw new IllegalStateException(
                "La squadra stagionale non e DA_CONFIGURARE: " + id
            );
        }
    }

    private static void requirePendingCompetition(Connection connection, long id)
            throws Exception {
        int count = count(connection, """
            SELECT COUNT(*) FROM rn_competition_mapping
            WHERE competition_season_id = ? AND mapping_status = 'DA_CONFIGURARE'
            """, id);
        if (count != 1) {
            throw new IllegalStateException(
                "La competizione stagionale non e DA_CONFIGURARE: " + id
            );
        }
    }

    private static long insertTeamIdentity(
            Connection connection,
            SeasonEntity team) throws Exception {
        String sql = """
            INSERT INTO rn_team_identity (
                anchor_season_id,
                anchor_team_season_id,
                canonical_name,
                created_at
            ) VALUES (?, ?, ?, ?)
            """;
        try (PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, team.seasonId());
            statement.setLong(2, team.id());
            statement.setString(3, team.name());
            statement.setString(4, Instant.now().toString());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new IllegalStateException(
                        "Identita squadra non creata per " + team.id()
                    );
                }
                return keys.getLong(1);
            }
        }
    }

    private static long insertCompetitionIdentity(
            Connection connection,
            SeasonEntity competition) throws Exception {
        String sql = """
            INSERT INTO rn_competition_identity (
                anchor_season_id,
                anchor_competition_season_id,
                canonical_name,
                created_at
            ) VALUES (?, ?, ?, ?)
            """;
        try (PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, competition.seasonId());
            statement.setLong(2, competition.id());
            statement.setString(3, competition.name());
            statement.setString(4, Instant.now().toString());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new IllegalStateException(
                        "Identita competizione non creata per " + competition.id()
                    );
                }
                return keys.getLong(1);
            }
        }
    }

    private static void updateTeamMapping(
            Connection connection,
            long teamSeasonId,
            long teamIdentityId,
            String method,
            String notes) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement("""
            UPDATE rn_team_mapping
            SET team_identity_id = ?,
                mapping_status = 'ASSOCIATA',
                mapping_method = ?,
                notes = ?,
                updated_at = ?
            WHERE team_season_id = ?
            """)) {
            statement.setLong(1, teamIdentityId);
            statement.setString(2, method);
            statement.setString(3, notes);
            statement.setString(4, Instant.now().toString());
            statement.setLong(5, teamSeasonId);
            if (statement.executeUpdate() != 1) {
                throw new IllegalStateException(
                    "Mapping squadra non trovato: " + teamSeasonId
                );
            }
        }
    }

    private static void updateCompetitionMapping(
            Connection connection,
            long competitionSeasonId,
            long competitionIdentityId,
            String method,
            String notes) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement("""
            UPDATE rn_competition_mapping
            SET competition_identity_id = ?,
                mapping_status = 'ASSOCIATA',
                mapping_method = ?,
                notes = ?,
                updated_at = ?
            WHERE competition_season_id = ?
            """)) {
            statement.setLong(1, competitionIdentityId);
            statement.setString(2, method);
            statement.setString(3, notes);
            statement.setString(4, Instant.now().toString());
            statement.setLong(5, competitionSeasonId);
            if (statement.executeUpdate() != 1) {
                throw new IllegalStateException(
                    "Mapping competizione non trovato: " + competitionSeasonId
                );
            }
        }
    }

    private static SeasonEntity requireTeamSeason(Connection connection, long id)
            throws Exception {
        return requireSeasonEntity(
            connection,
            "SELECT team_season_id, season_id, source_name "
                + "FROM rn_team_season WHERE team_season_id = ?",
            id,
            "Squadra stagionale"
        );
    }

    private static SeasonEntity requireCompetitionSeason(
            Connection connection,
            long id) throws Exception {
        return requireSeasonEntity(
            connection,
            "SELECT competition_season_id, season_id, source_name "
                + "FROM rn_competition_season WHERE competition_season_id = ?",
            id,
            "Competizione stagionale"
        );
    }

    private static SeasonEntity requireSeasonEntity(
            Connection connection,
            String sql,
            long id,
            String label) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalArgumentException(label + " non trovata: " + id);
                }
                return new SeasonEntity(
                    result.getLong(1),
                    result.getString(2),
                    result.getString(3)
                );
            }
        }
    }

    private static Identity requireTeamIdentity(Connection connection, long id)
            throws Exception {
        return requireIdentity(
            connection,
            "SELECT team_identity_id, canonical_name "
                + "FROM rn_team_identity WHERE team_identity_id = ?",
            id,
            "Identita squadra"
        );
    }

    private static Identity requireCompetitionIdentity(
            Connection connection,
            long id) throws Exception {
        return requireIdentity(
            connection,
            "SELECT competition_identity_id, canonical_name "
                + "FROM rn_competition_identity WHERE competition_identity_id = ?",
            id,
            "Identita competizione"
        );
    }

    private static Identity requireIdentity(
            Connection connection,
            String sql,
            long id,
            String label) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalArgumentException(label + " non trovata: " + id);
                }
                return new Identity(result.getLong(1), result.getString(2));
            }
        }
    }

    private static List<Identity> readTeamIdentities(Connection connection)
            throws Exception {
        return readIdentities(
            connection,
            "SELECT team_identity_id, canonical_name "
                + "FROM rn_team_identity ORDER BY canonical_name COLLATE NOCASE"
        );
    }

    private static List<Identity> readCompetitionIdentities(Connection connection)
            throws Exception {
        return readIdentities(
            connection,
            "SELECT competition_identity_id, canonical_name "
                + "FROM rn_competition_identity ORDER BY canonical_name COLLATE NOCASE"
        );
    }

    private static List<Identity> readIdentities(
            Connection connection,
            String sql) throws Exception {
        List<Identity> identities = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            while (result.next()) {
                identities.add(new Identity(result.getLong(1), result.getString(2)));
            }
        }
        return identities;
    }

    private static String requireSeason(Connection connection, String raw)
            throws Exception {
        String seasonId = raw.trim();
        int count = count(
            connection,
            "SELECT COUNT(*) FROM rn_season WHERE season_id = ?",
            seasonId
        );
        if (count != 1) {
            throw new IllegalArgumentException("Stagione non trovata: " + seasonId);
        }
        return seasonId;
    }

    private static int count(
            Connection connection,
            String sql,
            Object... parameters) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1);
            }
        }
    }

    private static long parsePositiveLong(String raw, String label) {
        try {
            long value = Long.parseLong(raw);
            if (value <= 0) {
                throw new NumberFormatException("non positivo");
            }
            return value;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                label + " non valido: " + raw,
                exception
            );
        }
    }

    private static void requireArgCount(
            String[] args,
            int expected,
            String usage) {
        if (args.length != expected) {
            throw new IllegalArgumentException("Uso: " + usage);
        }
    }

    private static double similarity(String left, String right) {
        String a = normalize(left);
        String b = normalize(right);
        if (a.equals(b)) {
            return 1.0d;
        }
        int max = Math.max(a.length(), b.length());
        return max == 0 ? 1.0d : 1.0d - ((double) levenshtein(a, b) / max);
    }

    private static String normalize(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "")
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", " ")
            .trim()
            .replaceAll("\\s+", " ");
    }

    private static int levenshtein(String left, String right) {
        int[] previous = new int[right.length() + 1];
        int[] current = new int[right.length() + 1];
        for (int j = 0; j <= right.length(); j++) {
            previous[j] = j;
        }
        for (int i = 1; i <= left.length(); i++) {
            current[0] = i;
            for (int j = 1; j <= right.length(); j++) {
                int cost = left.charAt(i - 1) == right.charAt(j - 1) ? 0 : 1;
                current[j] = Math.min(
                    Math.min(current[j - 1] + 1, previous[j] + 1),
                    previous[j - 1] + cost
                );
            }
            int[] swap = previous;
            previous = current;
            current = swap;
        }
        return previous[right.length()];
    }

    private static void runTransaction(Connection connection, SqlAction action)
            throws Exception {
        boolean oldAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            action.run();
            connection.commit();
        } catch (Exception exception) {
            connection.rollback();
            throw exception;
        } finally {
            connection.setAutoCommit(oldAutoCommit);
        }
    }

    private static void printUsage() {
        System.err.println("Comandi:");
        System.err.println("  <db> show-seasons");
        System.err.println("  <db> pending <stagione>");
        System.err.println("  <db> proposals <stagione>");
        System.err.println("  <db> validate <stagione>");
        System.err.println("  <db> auto-exact <stagione>");
        System.err.println(
            "  <db> associate-team <team-season-id> <team-identity-id>"
        );
        System.err.println("  <db> new-team <team-season-id>");
        System.err.println(
            "  <db> associate-competition "
                + "<competition-season-id> <competition-identity-id>"
        );
        System.err.println("  <db> new-competition <competition-season-id>");
    }

    @FunctionalInterface
    private interface SqlAction {
        void run() throws Exception;
    }

    private record SeasonEntity(long id, String seasonId, String name) {
    }

    private record Identity(long id, String name) {
    }

    private record ScoredIdentity(Identity identity, double score) {
    }

    private record ExactCandidate(long seasonEntityId, long identityId) {
    }

    private record Validation(
        int fcmSources,
        int fcaSources,
        int pendingCompetitions,
        int pendingTeams,
        int duplicateCompetitions,
        int duplicateTeams,
        int orphanMappings
    ) {
        boolean valid() {
            return fcmSources == 1
                && fcaSources == 1
                && pendingCompetitions == 0
                && pendingTeams == 0
                && duplicateCompetitions == 0
                && duplicateTeams == 0
                && orphanMappings == 0;
        }
    }
}
```

### src\main\java\it\alterlega\recordsnext\SeasonNormalizedBatchExporter.java

```java
package it.alterlega.recordsnext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class SeasonNormalizedBatchExporter {

    private SeasonNormalizedBatchExporter() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.err.println(
                "Uso: SeasonNormalizedBatchExporter "
                    + "<recordsnext.db> <stagione> <project-dir>"
            );
            System.exit(2);
        }

        Path database = Path.of(args[0]).toAbsolutePath().normalize();
        String seasonId = args[1].trim();
        Path projectDir = Path.of(args[2]).toAbsolutePath().normalize();
        export(database, seasonId, projectDir);
    }

    public static void export(Path database, String seasonId, Path projectDir) throws Exception {
        Path outputDir = projectDir
            .resolve("data")
            .resolve("reports")
            .resolve(seasonId);

        Files.createDirectories(outputDir);

        Class.forName("org.sqlite.JDBC");

        List<String> competitions = readCompetitions(
            database,
            seasonId
        );

        if (competitions.isEmpty()) {
            throw new IllegalStateException(
                "Nessuna competizione trovata per la stagione "
                    + seasonId
            );
        }

        int completed = 0;
        List<String> failures = new ArrayList<>();

        System.out.println(
            "Competizioni da esportare: "
                + competitions.size()
        );

        for (String competition : competitions) {
            Path output = outputDir.resolve(
                "season_normalized_"
                    + slug(competition)
                    + ".json"
            );

            System.out.println();
            System.out.println(
                "=== " + competition + " ==="
            );

            try {
                SeasonNormalizedExporter.main(
                    new String[] {
                        database.toString(),
                        seasonId,
                        competition,
                        projectDir.toString(),
                        output.toString()
                    }
                );

                completed++;
            } catch (Exception error) {
                failures.add(
                    competition
                        + ": "
                        + error.getClass().getSimpleName()
                        + " - "
                        + error.getMessage()
                );

                error.printStackTrace(System.err);
            }
        }

        System.out.println();
        System.out.println("=== RIEPILOGO BATCH ===");
        System.out.println(
            "Stagione     : " + seasonId
        );
        System.out.println(
            "Competizioni : " + competitions.size()
        );
        System.out.println(
            "Completate   : " + completed
        );
        System.out.println(
            "Fallite      : " + failures.size()
        );
        System.out.println(
            "Output       : " + outputDir
        );

        if (!failures.isEmpty()) {
            System.out.println();
            System.out.println("Errori:");
            for (String failure : failures) System.out.println(" - " + failure);
            throw new IllegalStateException("Normalizzazione fallita per " + failures.size() + " competizioni: " + String.join("; ", failures));
        }
    }

    private static List<String> readCompetitions(
            Path database,
            String seasonId) throws Exception {

        String sql = """
            SELECT DISTINCT competition_name
            FROM rn_team_match
            WHERE season_id = ?
              AND competition_name IS NOT NULL
              AND TRIM(competition_name) <> ''
            ORDER BY competition_name COLLATE NOCASE
            """;

        List<String> competitions = new ArrayList<>();

        try (
            Connection connection =
                DriverManager.getConnection(
                    "jdbc:sqlite:" + database
                );

            PreparedStatement statement =
                connection.prepareStatement(sql)
        ) {
            statement.setString(1, seasonId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    competitions.add(
                        result.getString("competition_name")
                    );
                }
            }
        }

        return competitions;
    }

    private static String slug(String value) {
        String normalized = Normalizer.normalize(
            value,
            Normalizer.Form.NFD
        );

        return normalized
            .replaceAll("\\p{M}+", "")
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "_")
            .replaceAll("^_+|_+$", "");
    }
}
```

### src\main\java\it\alterlega\recordsnext\SeasonNormalizedExporter.java

```java
package it.alterlega.recordsnext;

import java.io.BufferedWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class SeasonNormalizedExporter {

    private SeasonNormalizedExporter() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 5) {
            System.err.println(
                "Uso: SeasonNormalizedExporter "
                    + "<recordsnext.db> "
                    + "<stagione> "
                    + "<competizione-canonica> "
                    + "<project-dir> "
                    + "<output.json>"
            );
            System.exit(2);
        }

        Path database = Path.of(args[0])
            .toAbsolutePath()
            .normalize();

        String seasonId = args[1].trim();
        String competitionName = args[2].trim();

        Path projectDir = Path.of(args[3])
            .toAbsolutePath()
            .normalize();

        Path output = Path.of(args[4])
            .toAbsolutePath()
            .normalize();

        if (output.getParent() != null) {
            Files.createDirectories(output.getParent());
        }

        Class.forName("org.sqlite.JDBC");

        long started = System.nanoTime();

        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + database)) {

            ExportData data = readExportData(
                connection,
                seasonId,
                competitionName,
                projectDir
            );

            writeJson(output, data);

            long finished = System.nanoTime();

            System.out.println("Normalized stage 1 completato");
            System.out.println("Stagione    : " + seasonId);
            System.out.println("Competizione: " + competitionName);
            System.out.println("Incontri    : " + data.meta().matchesAnalyzed());
            System.out.println("Righe squadra: " + data.teamMatches().size());
            System.out.println("Output      : " + output);

            System.out.printf(
                Locale.ROOT,
                "Tempo       : %.3f ms%n",
                (finished - started) / 1_000_000.0
            );
        }
    }

    private static ExportData readExportData(
            Connection connection,
            String seasonId,
            String competitionName,
            Path projectDir) throws Exception {

        CompetitionInfo competition = readCompetition(
            connection,
            seasonId,
            competitionName
        );

        List<Integer> groupIds = readGroupIds(
            connection,
            seasonId,
            competition.identityId()
        );

        List<TeamMatch> teamMatches = readTeamMatches(
            connection,
            seasonId,
            competition.identityId(),
            competitionName
        );

        List<ExpulsionDetail> expulsionDetails =
            readExpulsionDetails(
                connection,
                seasonId,
                competition.identityId()
            );

        List<EventDetail> eventDetails =
            readEventDetails(
                connection,
                seasonId,
                competition.identityId()
            );

        List<ModifierDetail> modifierDetails =
            readModifierDetails(
                connection,
                seasonId,
                competition.identityId()
            );

        List<CleanSheetDetail> cleanSheetDetails =
            readCleanSheetDetails(
                connection,
                seasonId,
                competition.identityId()
            );

        List<ReserveOfficeDetail> reserveOfficeDetails =
            readReserveOfficeDetails(
                connection,
                seasonId,
                competition.identityId()
            );

        List<GoalBandDetail> goalBandDetails =
            readGoalBandDetails(
                connection,
                seasonId,
                competition.sourceCompetitionId()
            );

        int matchesAnalyzed = teamMatches.size() / 2;

        Meta meta = new Meta(
            Instant.now().toString(),
            projectDir.toString(),
            seasonId,
            outputHistoricalCompetitionId(competitionName),
            outputCompetitionName(competitionName),
            competition.sourceCompetitionId(),
            null,
            groupIds,
            "SQLite: " + connection.getMetaData().getURL(),
            "SQLite: " + connection.getMetaData().getURL(),
            matchesAnalyzed,
            teamMatches.size()
        );

        return new ExportData(
            meta,
            teamMatches,
            expulsionDetails,
            eventDetails,
            modifierDetails,
            cleanSheetDetails,
            reserveOfficeDetails,
            goalBandDetails
        );
    }

    private static CompetitionInfo readCompetition(
            Connection connection,
            String seasonId,
            String competitionName) throws Exception {

        String sql = """
            SELECT DISTINCT
                competition_identity_id,
                source_competition_id
            FROM rn_match
            WHERE season_id = ?
              AND competition_name = ?
            """;

        try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setString(1, seasonId);
            statement.setString(2, competitionName);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalArgumentException(
                        "Competizione non trovata: "
                            + seasonId
                            + " / "
                            + competitionName
                    );
                }

                CompetitionInfo info = new CompetitionInfo(
                    result.getLong("competition_identity_id"),
                    result.getInt("source_competition_id")
                );

                if (result.next()) {
                    throw new IllegalStateException(
                        "PiÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¹ identitÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â  trovate per "
                            + seasonId
                            + " / "
                            + competitionName
                    );
                }

                return info;
            }
        }
    }

    private static List<Integer> readGroupIds(
            Connection connection,
            String seasonId,
            long competitionIdentityId) throws Exception {

        String sql = """
            SELECT DISTINCT source_group_id
            FROM rn_match
            WHERE season_id = ?
              AND competition_identity_id = ?
            ORDER BY source_group_id
            """;

        List<Integer> values = new ArrayList<>();

        try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setString(1, seasonId);
            statement.setLong(2, competitionIdentityId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    values.add(
                        result.getInt("source_group_id")
                    );
                }
            }
        }

        return values;
    }

    private static List<TeamMatch> readTeamMatches(
            Connection connection,
            String seasonId,
            long competitionIdentityId,
            String competitionName) throws Exception {

        String outputHistoricalId =
            outputHistoricalCompetitionId(competitionName);

        String outputCompetitionName =
            outputCompetitionName(competitionName);

        SourceInfo source = readFcmSource(
            connection,
            seasonId
        );

        String tabellinoTable = rawTable(
            connection,
            source.importId(),
            "TABELLINO"
        );

        String gironeTable = rawTable(
            connection,
            source.importId(),
            "GIRONE"
        );

        boolean calendarAvailable = tableExists(connection, "rn_matchday_date");

        String calendarColumns = calendarAvailable
            ? "md.match_date, md.match_time, md.match_datetime,"
            : "NULL AS match_date, NULL AS match_time, NULL AS match_datetime,";

        String calendarJoin = calendarAvailable
            ? "LEFT JOIN rn_matchday_date md "
                + "ON md.season_id = e.season_id "
                + "AND md.serie_a_round = e.serie_a_round"
            : "";

        String sql = """
            SELECT
                e.season_id,
                %s
                e.competition_name,
                e.source_competition_id,
                e.source_group_id,
                g.NOME AS source_group_name,
                e.source_round_id,
                e.round_description,
                e.serie_a_round,
                e.source_event_id,
                e.event_type,
                e.venue,
                e.source_team_id,
                e.team_name,
                e.opponent_source_team_id,
                e.opponent_name,
                e.score_for,
                e.score_against,
                e.partial_score_for,
                e.partial_score_against,
                e.goals_for,
                e.goals_against,
                e.result,

                CASE
                    WHEN e.event_type = 'REST' THEN 0
                    WHEN tf.IDINCONTRO IS NULL THEN e.goals_for
                    ELSE
                        CAST(COALESCE(tf.GOL, 0) AS INTEGER)
                        - CAST(COALESCE(tf.GOLSUPPLEMENTARI, 0) AS INTEGER)
                        - CAST(COALESCE(tf.GOLRIGORI, 0) AS INTEGER)
                END AS regulation_goals_for,

                CASE
                    WHEN e.event_type = 'REST' THEN 0
                    WHEN ta.IDINCONTRO IS NULL THEN e.goals_against
                    ELSE
                        CAST(COALESCE(ta.GOL, 0) AS INTEGER)
                        - CAST(COALESCE(ta.GOLSUPPLEMENTARI, 0) AS INTEGER)
                        - CAST(COALESCE(ta.GOLRIGORI, 0) AS INTEGER)
                END AS regulation_goals_against,

                CASE
                    WHEN e.event_type = 'REST' THEN 0
                    WHEN tf.IDINCONTRO IS NULL THEN 0
                    ELSE 1
                END AS regulation_goals_found

            FROM rn_team_event e

            JOIN %s g
              ON g.ID = e.source_group_id

            LEFT JOIN %s tf
              ON e.event_type = 'HEAD_TO_HEAD'
             AND tf.IDINCONTRO = e.source_event_id
             AND tf.IDSQUADRA = e.source_team_id

            LEFT JOIN %s ta
              ON e.event_type = 'HEAD_TO_HEAD'
             AND ta.IDINCONTRO = e.source_event_id
             AND ta.IDSQUADRA = e.opponent_source_team_id

            %s

            WHERE e.season_id = ?
              AND e.competition_identity_id = ?
              AND e.event_type IN ('HEAD_TO_HEAD', 'REST')

            ORDER BY
                e.source_event_id,
                CASE e.venue
                    WHEN 'HOME' THEN 0
                    WHEN 'AWAY' THEN 1
                    ELSE 0
                END
            """.formatted(
                calendarColumns,
                quoteIdentifier(gironeTable),
                quoteIdentifier(tabellinoTable),
                quoteIdentifier(tabellinoTable),
                calendarJoin
            );

        ScorecardBases scorecardBases = readScorecardBases(
            connection,
            seasonId
        );

        List<TeamMatch> rows = new ArrayList<>();

        try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setString(1, seasonId);
            statement.setLong(2, competitionIdentityId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    String eventType =
                        result.getString("event_type");

                    boolean rest =
                        "REST".equals(eventType);

                    String venue =
                        result.getString("venue");

                    String side;

                    if (rest) {
                        side = "casa";
                    } else {
                        side = switch (venue) {
                            case "HOME" -> "casa";
                            case "AWAY" -> "fuori";
                            default -> throw new IllegalStateException(
                                "Lato non previsto: "
                                    + venue
                                    + " / evento "
                                    + result.getLong(
                                        "source_event_id"
                                    )
                            );
                        };
                    }

                    int goalsFor = rest
                        ? 0
                        : result.getInt("goals_for");

                    int goalsAgainst = rest
                        ? 0
                        : result.getInt("goals_against");

                    int regulationGoalsFor = rest
                        ? 0
                        : result.getInt(
                            "regulation_goals_for"
                        );

                    int regulationGoalsAgainst = rest
                        ? 0
                        : result.getInt(
                            "regulation_goals_against"
                        );

                    BigDecimal scoreFor = zeroIfNull(
                        result.getBigDecimal("score_for")
                    );

                    BigDecimal scoreAgainst = rest
                        ? BigDecimal.ZERO
                        : zeroIfNull(
                            result.getBigDecimal(
                                "score_against"
                            )
                        );

                    BigDecimal partialFor = zeroIfNull(
                        result.getBigDecimal(
                            "partial_score_for"
                        )
                    );

                    BigDecimal partialAgainst = rest
                        ? BigDecimal.ZERO
                        : zeroIfNull(
                            result.getBigDecimal(
                                "partial_score_against"
                            )
                        );

                    int opponentId = rest
                        ? 0
                        : result.getInt(
                            "opponent_source_team_id"
                        );

                    String opponentName = rest
                        ? ""
                        : emptyIfNull(
                            result.getString(
                                "opponent_name"
                            )
                        );

                    int homeGoals;
                    int awayGoals;
                    int regulationHomeGoals;
                    int regulationAwayGoals;
                    BigDecimal homeScore;
                    BigDecimal awayScore;

                    if (rest || "HOME".equals(venue)) {
                        homeGoals = goalsFor;
                        awayGoals = goalsAgainst;

                        regulationHomeGoals =
                            regulationGoalsFor;

                        regulationAwayGoals =
                            regulationGoalsAgainst;

                        homeScore = scoreFor;
                        awayScore = scoreAgainst;
                    } else {
                        homeGoals = goalsAgainst;
                        awayGoals = goalsFor;

                        regulationHomeGoals =
                            regulationGoalsAgainst;

                        regulationAwayGoals =
                            regulationGoalsFor;

                        homeScore = scoreAgainst;
                        awayScore = scoreFor;
                    }

                    String resultCode;

                    if (rest) {
                        resultCode = "P";
                    } else {
                        resultCode = switch (
                            result.getString("result")
                        ) {
                            case "W" -> "V";
                            case "D" -> "P";
                            case "L" -> "S";
                            default -> throw new IllegalStateException(
                                "Esito non previsto: "
                                    + result.getString(
                                        "result"
                                    )
                            );
                        };
                    }

                    int serieARound =
                        result.getInt("serie_a_round");

                    String regulationSource;

                    if (rest) {
                        regulationSource =
                            "GolCasa/GolFuori fallback";
                    } else if (
                        result.getInt(
                            "regulation_goals_found"
                        ) != 0
                    ) {
                        regulationSource =
                            "GolRegoCasa/GolRegoFuori";
                    } else {
                        regulationSource =
                            "GolCasa/GolFuori fallback";
                    }

                    rows.add(
                        new TeamMatch(
                            result.getString("season_id"),
                            outputHistoricalId,
                            outputCompetitionName,
                            result.getInt(
                                "source_competition_id"
                            ),
                            null,
                            result.getInt(
                                "source_group_id"
                            ),
                            result.getString(
                                "source_group_name"
                            ),
                            result.getInt(
                                "source_round_id"
                            ),
                            result.getString(
                                "round_description"
                            ),
                            serieARound,
                            result.getString("match_date"),
                            result.getString("match_time"),
                            result.getString("match_datetime"),
                            result.getInt(
                                "source_round_id"
                            ),
                            result.getLong(
                                "source_event_id"
                            ),
                            scorecardUrl(result.getString("season_id"), serieARound),
                            scorecardBases.localUrl(serieARound),
                            scorecardBases.onlineUrl(serieARound),
                            side,
                            result.getInt(
                                "source_team_id"
                            ),
                            result.getString(
                                "team_name"
                            ),
                            opponentId,
                            opponentName,
                            scoreFor,
                            scoreAgainst,
                            partialFor,
                            partialAgainst,
                            goalsFor,
                            goalsAgainst,
                            regulationGoalsFor,
                            regulationGoalsAgainst,
                            regulationHomeGoals
                                + "-"
                                + regulationAwayGoals,
                            regulationSource,
                            resultCode,
                            homeGoals + "-" + awayGoals,
                            decimalText(homeScore)
                                + "-"
                                + decimalText(awayScore)
                        )
                    );

                    if (rest) {
                        rows.add(
                            new TeamMatch(
                                result.getString("season_id"),
                                outputHistoricalId,
                                outputCompetitionName,
                                result.getInt(
                                    "source_competition_id"
                                ),
                                null,
                                result.getInt(
                                    "source_group_id"
                                ),
                                result.getString(
                                    "source_group_name"
                                ),
                                result.getInt(
                                    "source_round_id"
                                ),
                                result.getString(
                                    "round_description"
                                ),
                                serieARound,
                            result.getString("match_date"),
                            result.getString("match_time"),
                            result.getString("match_datetime"),
                                result.getInt(
                                    "source_round_id"
                                ),
                                result.getLong(
                                    "source_event_id"
                                ),
                                scorecardUrl(result.getString("season_id"), serieARound),
                                scorecardBases.localUrl(serieARound),
                                scorecardBases.onlineUrl(serieARound),
                                "fuori",
                                0,
                                "",
                                result.getInt(
                                    "source_team_id"
                                ),
                                result.getString(
                                    "team_name"
                                ),
                                BigDecimal.ZERO,
                                BigDecimal.ZERO,
                                BigDecimal.ZERO,
                                BigDecimal.ZERO,
                                0,
                                0,
                                0,
                                0,
                                "0-0",
                                "GolCasa/GolFuori fallback",
                                "P",
                                "0-0",
                                "0-0"
                            )
                        );
                    }
                }
            }
        }

        return rows;
    }
    private static List<ExpulsionDetail> readExpulsionDetails(
            Connection connection,
            String seasonId,
            long competitionIdentityId) throws Exception {

        SourceInfo fcmSource = readSource(
            connection,
            seasonId,
            "FCM"
        );

        SourceInfo fcaSource = readSource(
            connection,
            seasonId,
            "FCA"
        );

        String formazioneTable = rawTable(
            connection,
            fcmSource.importId(),
            "FORMAZIONE"
        );

        String giocaInTable = rawTable(
            connection,
            fcaSource.importId(),
            "GIOCAIN"
        );

        String punteggioTable = rawTable(
            connection,
            fcaSource.importId(),
            "PUNTEGGIO"
        );

        String giocatoreTable = rawTable(
            connection,
            fcaSource.importId(),
            "GIOCATOREA"
        );

        String sql = """
            SELECT
                tm.source_match_id,
                tm.serie_a_round,
                tm.source_team_id,
                tm.team_name,
                f.IDGIOC AS player_id,
                ga.NOME AS player_name,
                gi.IDPUNTEGGIO AS score_id

            FROM rn_team_match tm

            JOIN %s f
              ON f.IDINCONTRO = tm.source_match_id
             AND f.IDSQUADRA = tm.source_team_id

            JOIN %s gi
              ON gi.IDGIOCATORE = f.IDGIOC
             AND gi.GIORNATA = tm.serie_a_round

            JOIN %s p
              ON p.ID = gi.IDPUNTEGGIO

            JOIN %s ga
              ON ga.ID = f.IDGIOC

            WHERE tm.season_id = ?
              AND tm.competition_identity_id = ?
              AND f.ENTRATO <> 0
              AND p.ESP <> 0

            ORDER BY
                tm.source_match_id,
                f.IDGIOC
            """.formatted(
                quoteIdentifier(formazioneTable),
                quoteIdentifier(giocaInTable),
                quoteIdentifier(punteggioTable),
                quoteIdentifier(giocatoreTable)
            );

        List<ExpulsionDetail> rows = new ArrayList<>();

        try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setString(1, seasonId);
            statement.setLong(2, competitionIdentityId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    rows.add(
                        new ExpulsionDetail(
                            result.getLong("source_match_id"),
                            result.getInt("serie_a_round"),
                            result.getInt("source_team_id"),
                            result.getString("team_name"),
                            result.getInt("player_id"),
                            result.getString("player_name"),
                            result.getInt("score_id")
                        )
                    );
                }
            }
        }

        return rows;
    }

    private static List<EventDetail> readEventDetails(
            Connection connection,
            String seasonId,
            long competitionIdentityId) throws Exception {

        SourceInfo fcmSource = readSource(
            connection,
            seasonId,
            "FCM"
        );

        SourceInfo fcaSource = readSource(
            connection,
            seasonId,
            "FCA"
        );

        String formazioneTable = rawTable(
            connection,
            fcmSource.importId(),
            "FORMAZIONE"
        );

        String giocaInTable = rawTable(
            connection,
            fcaSource.importId(),
            "GIOCAIN"
        );

        String punteggioTable = rawTable(
            connection,
            fcaSource.importId(),
            "PUNTEGGIO"
        );

        String giocatoreTable = rawTable(
            connection,
            fcaSource.importId(),
            "GIOCATOREA"
        );

        String sql = """
            SELECT
                e.record_key,
                e.event_type,
                e.event_name,
                e.source_field,
                tm.source_match_id,
                tm.serie_a_round,
                tm.source_team_id,
                tm.team_name,
                f.IDGIOC AS player_id,
                ga.NOME AS player_name,
                gi.IDPUNTEGGIO AS score_id,
                CASE e.event_number
                    WHEN 1 THEN p.AMM
                    WHEN 2 THEN p.ASSIST
                    WHEN 3 THEN p.GOLFATTISURIGORE1
                    WHEN 4 THEN p.RIGPAR
                    WHEN 5 THEN p.RIGSBA
                    WHEN 6 THEN p.AUTOGOL1
                END AS event_value,
                e.event_number

            FROM rn_team_match tm

            JOIN %s f
              ON f.IDINCONTRO = tm.source_match_id
             AND f.IDSQUADRA = tm.source_team_id

            JOIN %s gi
              ON gi.IDGIOCATORE = f.IDGIOC
             AND gi.GIORNATA = tm.serie_a_round

            JOIN %s p
              ON p.ID = gi.IDPUNTEGGIO

            JOIN %s ga
              ON ga.ID = f.IDGIOC

            CROSS JOIN (
                SELECT
                    1 AS event_number,
                    'ammonizioniSquadre' AS record_key,
                    'ammonizione' AS event_type,
                    'Maggiori ammonizioni' AS event_name,
                    'Amm' AS source_field

                UNION ALL

                SELECT
                    2,
                    'assistSquadre',
                    'assist',
                    'Maggiori assist',
                    'Assist'

                UNION ALL

                SELECT
                    3,
                    'golRigoreSquadre',
                    'gol_su_rigore',
                    'Maggiori gol fatti su rigore',
                    'GolFattiSuRigore1'

                UNION ALL

                SELECT
                    4,
                    'rigoriParatiSquadre',
                    'rigore_parato',
                    'Maggiori rigori parati',
                    'RigPar'

                UNION ALL

                SELECT
                    5,
                    'rigoriSbagliatiSquadre',
                    'rigore_sbagliato',
                    'Maggiori rigori sbagliati',
                    'RigSba'

                UNION ALL

                SELECT
                    6,
                    'autogolSquadre',
                    'autogol',
                    'Maggiori autogol',
                    'Autogol1'
            ) e

            WHERE tm.season_id = ?
              AND tm.competition_identity_id = ?
              AND f.ENTRATO <> 0
              AND CASE e.event_number
                    WHEN 1 THEN p.AMM
                    WHEN 2 THEN p.ASSIST
                    WHEN 3 THEN p.GOLFATTISURIGORE1
                    WHEN 4 THEN p.RIGPAR
                    WHEN 5 THEN p.RIGSBA
                    WHEN 6 THEN p.AUTOGOL1
                  END <> 0

            ORDER BY
                tm.source_match_id,
                f.rowid,
                e.event_number
            """.formatted(
                quoteIdentifier(formazioneTable),
                quoteIdentifier(giocaInTable),
                quoteIdentifier(punteggioTable),
                quoteIdentifier(giocatoreTable)
            );

        List<EventDetail> rows = new ArrayList<>();

        try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setString(1, seasonId);
            statement.setLong(2, competitionIdentityId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    rows.add(
                        new EventDetail(
                            result.getString("record_key"),
                            result.getString("event_type"),
                            result.getString("event_name"),
                            result.getString("source_field"),
                            result.getLong("source_match_id"),
                            result.getInt("serie_a_round"),
                            result.getInt("source_team_id"),
                            result.getString("team_name"),
                            result.getInt("player_id"),
                            result.getString("player_name"),
                            result.getInt("score_id"),
                            result.getInt("event_value")
                        )
                    );
                }
            }
        }

        return rows;
    }

    private static List<CleanSheetDetail> readCleanSheetDetails(
            Connection connection,
            String seasonId,
            long competitionIdentityId) throws Exception {

        SourceInfo fcmSource = readSource(
            connection,
            seasonId,
            "FCM"
        );

        SourceInfo fcaSource = readSource(
            connection,
            seasonId,
            "FCA"
        );

        String formazioneTable = rawTable(
            connection,
            fcmSource.importId(),
            "FORMAZIONE"
        );

        String giocaInTable = rawTable(
            connection,
            fcaSource.importId(),
            "GIOCAIN"
        );

        String punteggioTable = rawTable(
            connection,
            fcaSource.importId(),
            "PUNTEGGIO"
        );

        String giocatoreTable = rawTable(
            connection,
            fcaSource.importId(),
            "GIOCATOREA"
        );

        String sql = """
            SELECT
                tm.source_match_id,
                tm.serie_a_round,
                tm.source_team_id,
                tm.team_name,
                tm.opponent_source_team_id,
                tm.opponent_name,
                f.IDGIOC AS player_id,
                ga.NOME AS player_name,
                gi.IDPUNTEGGIO AS score_id,
                p.GOLSUBITI AS goals_conceded

            FROM rn_team_match tm

            JOIN %s f
              ON f.IDINCONTRO = tm.source_match_id
             AND f.IDSQUADRA = tm.source_team_id

            JOIN %s gi
              ON gi.IDGIOCATORE = f.IDGIOC
             AND gi.GIORNATA = tm.serie_a_round

            JOIN %s p
              ON p.ID = gi.IDPUNTEGGIO

            JOIN %s ga
              ON ga.ID = f.IDGIOC

            WHERE tm.season_id = ?
              AND tm.competition_identity_id = ?
              AND f.ENTRATO <> 0
              AND ga.RUOLO = 1
              AND p.GOLSUBITI = 0

            ORDER BY
                f.IDGIOC,
                tm.source_match_id
            """.formatted(
                quoteIdentifier(formazioneTable),
                quoteIdentifier(giocaInTable),
                quoteIdentifier(punteggioTable),
                quoteIdentifier(giocatoreTable)
            );

        List<CleanSheetDetail> rows = new ArrayList<>();

        try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setString(1, seasonId);
            statement.setLong(2, competitionIdentityId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    rows.add(
                        new CleanSheetDetail(
                            result.getLong("source_match_id"),
                            result.getInt("serie_a_round"),
                            result.getInt("source_team_id"),
                            result.getString("team_name"),
                            result.getInt("opponent_source_team_id"),
                            result.getString("opponent_name"),
                            result.getInt("player_id"),
                            result.getString("player_name"),
                            result.getInt("score_id"),
                            result.getInt("goals_conceded"),
                            new BigDecimal("0.5")
                        )
                    );
                }
            }
        }

        return rows;
    }

    private static List<ModifierDetail> readModifierDetails(
            Connection connection,
            String seasonId,
            long competitionIdentityId) throws Exception {

        SourceInfo source = readFcmSource(
            connection,
            seasonId
        );

        String tabellinoTable = rawTable(
            connection,
            source.importId(),
            "TABELLINO"
        );

        String gironeTable = rawTable(
            connection,
            source.importId(),
            "GIRONE"
        );

        String sql = """
            SELECT
                x.modifier_type,
                x.source_field,
                m.source_match_id,
                m.serie_a_round,
                tm.source_team_id,
                tm.team_name,
                tm.opponent_source_team_id,
                tm.opponent_name,

                CASE x.modifier_number
                    WHEN 1 THEN t.MODM1PERS
                    WHEN 2 THEN t.MODM2PERS
                END AS modifier_value

            FROM %s t

            JOIN rn_match m
              ON m.source_file_id = ?
             AND m.source_match_id = t.IDINCONTRO

            JOIN rn_team_match tm
              ON tm.source_file_id = m.source_file_id
             AND tm.source_match_id = m.source_match_id
             AND tm.source_team_id = t.IDSQUADRA

            CROSS JOIN (
                SELECT
                    1 AS modifier_number,
                    'modDifesa' AS modifier_type,
                    'ModM1Pers' AS source_field

                UNION ALL

                SELECT
                    2 AS modifier_number,
                    'capitano' AS modifier_type,
                    'ModM2Pers' AS source_field
            ) x

            WHERE m.season_id = ?
              AND m.competition_identity_id = ?

              AND CASE x.modifier_number
                    WHEN 1 THEN t.MODM1PERSESISTE
                    WHEN 2 THEN t.MODM2PERSESISTE
                  END <> 0

              AND CASE x.modifier_number
                    WHEN 1 THEN t.MODM1PERS
                    WHEN 2 THEN t.MODM2PERS
                  END <> 0

            ORDER BY
                t.rowid,
                x.modifier_number
            """.formatted(
                quoteIdentifier(tabellinoTable)
            );

        List<ModifierDetail> rows = new ArrayList<>();

        try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setLong(1, source.sourceFileId());
            statement.setString(2, seasonId);
            statement.setLong(3, competitionIdentityId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    rows.add(
                        new ModifierDetail(
                            result.getString("modifier_type"),
                            result.getLong("source_match_id"),
                            result.getInt("serie_a_round"),
                            result.getInt("source_team_id"),
                            result.getString("team_name"),
                            result.getInt(
                                "opponent_source_team_id"
                            ),
                            result.getString("opponent_name"),
                            result.getBigDecimal(
                                "modifier_value"
                            ),
                            result.getString("source_field")
                        )
                    );
                }
            }
        }

        return rows;
    }

    private static List<ReserveOfficeDetail> readReserveOfficeDetails(
            Connection connection,
            String seasonId,
            long competitionIdentityId) throws Exception {

        SourceInfo source = readFcmSource(connection, seasonId);
        String tabellinoTable = rawTable(connection, source.importId(), "TABELLINO");

        String sql = """
            SELECT
                m.source_match_id,
                m.serie_a_round,
                tm.source_team_id,
                tm.team_name,
                tm.opponent_source_team_id,
                tm.opponent_name,
                t.LISTA,
                t.RUOLO,
                t.VOTO,
                t.MODIF,
                t.TOT
            FROM %s t
            JOIN rn_match m
              ON m.source_file_id = ?
             AND m.source_match_id = t.IDINCONTRO
            JOIN rn_team_match tm
              ON tm.source_file_id = m.source_file_id
             AND tm.source_match_id = m.source_match_id
             AND tm.source_team_id = t.IDSQUADRA
            WHERE m.season_id = ?
              AND m.competition_identity_id = ?
            ORDER BY t.rowid
            """.formatted(quoteIdentifier(tabellinoTable));

        List<ReserveOfficeDetail> rows = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, source.sourceFileId());
            statement.setString(2, seasonId);
            statement.setLong(3, competitionIdentityId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    String[] players = splitPercent(result.getString("LISTA"));
                    String[] roles = splitPercent(result.getString("RUOLO"));
                    String[] votes = splitPercent(result.getString("VOTO"));
                    String[] modifiers = splitPercent(result.getString("MODIF"));
                    String[] totals = splitPercent(result.getString("TOT"));
                    int max = Math.max(players.length,
                        Math.max(roles.length,
                        Math.max(votes.length,
                        Math.max(modifiers.length, totals.length))));

                    for (int index = 0; index < max; index++) {
                        if (!"-1".equals(item(players, index))) {
                            continue;
                        }

                        int role = parseInteger(item(roles, index));
                        rows.add(new ReserveOfficeDetail(
                            result.getLong("source_match_id"),
                            result.getInt("serie_a_round"),
                            result.getInt("source_team_id"),
                            result.getString("team_name"),
                            result.getInt("opponent_source_team_id"),
                            result.getString("opponent_name"),
                            roleCode(role),
                            roleName(role),
                            index + 1,
                            item(votes, index),
                            item(modifiers, index),
                            item(totals, index),
                            parseDecimal(item(totals, index))
                        ));
                    }
                }
            }
        }

        return rows;
    }

    private static List<GoalBandDetail> readGoalBandDetails(
            Connection connection,
            String seasonId,
            int sourceCompetitionId) throws Exception {

        SourceInfo source = readFcmSource(connection, seasonId);
        String goalTable = rawTable(connection, source.importId(), "TABELLAGOL");
        String bandTable = rawTable(connection, source.importId(), "FASCIA");

        String sql = """
            SELECT
                tg.IDCOMPETIZIONE AS source_competition_id,
                tg.IDFASCIA AS source_band_id,
                f.MIN AS min_score,
                f.MAX AS max_score,
                f.VALORE AS goals
            FROM %s tg
            JOIN %s f
              ON f.ID = tg.IDFASCIA
            WHERE tg.IDCOMPETIZIONE = ?
            ORDER BY CAST(f.MIN AS REAL), CAST(f.VALORE AS INTEGER), tg.IDFASCIA
            """.formatted(
                quoteIdentifier(goalTable),
                quoteIdentifier(bandTable)
            );

        List<GoalBandDetail> rows = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, sourceCompetitionId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    rows.add(new GoalBandDetail(
                        result.getInt("source_competition_id"),
                        Integer.toString(result.getInt("source_band_id")),
                        zeroIfNull(result.getBigDecimal("min_score")),
                        zeroIfNull(result.getBigDecimal("max_score")),
                        result.getInt("goals")
                    ));
                }
            }
        }

        if (rows.isEmpty()) {
            throw new IllegalStateException(
                "Nessuna fascia gol trovata per "
                    + seasonId
                    + " / competizione FCM "
                    + sourceCompetitionId
            );
        }

        return rows;
    }

    private static String[] splitPercent(String value) {
        if (value == null || value.isBlank()) {
            return new String[0];
        }
        return value.split("%", -1);
    }

    private static String item(String[] values, int index) {
        return index >= 0 && index < values.length ? values[index].trim() : "";
    }

    private static int parseInteger(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static BigDecimal parseDecimal(String value) {
        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value.trim().replace(',', '.'));
        } catch (NumberFormatException ignored) {
            return BigDecimal.ZERO;
        }
    }

    private static String roleCode(int role) {
        return switch (role) {
            case 1 -> "PU";
            case 2 -> "DU";
            case 3 -> "CU";
            case 4 -> "AU";
            default -> "";
        };
    }

    private static String roleName(int role) {
        return switch (role) {
            case 1 -> "Portiere";
            case 2 -> "Difensore";
            case 3 -> "Centrocampista";
            case 4 -> "Attaccante";
            default -> "";
        };
    }

    private static SourceInfo readFcmSource(
            Connection connection,
            String seasonId) throws Exception {

        return readSource(
            connection,
            seasonId,
            "FCM"
        );
    }

    private static SourceInfo readSource(
            Connection connection,
            String seasonId,
            String sourceType) throws Exception {

        String sql = """
            SELECT
                source_file_id,
                import_id
            FROM rn_source_file
            WHERE season_id = ?
              AND source_type = ?
            ORDER BY import_id DESC
            LIMIT 1
            """;

        try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setString(1, seasonId);
            statement.setString(2, sourceType);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalArgumentException(
                        "Sorgente "
                            + sourceType
                            + " non trovata: "
                            + seasonId
                    );
                }

                return new SourceInfo(
                    result.getLong("source_file_id"),
                    result.getLong("import_id")
                );
            }
        }
    }

    private static String rawTable(
            Connection connection,
            long importId,
            String sourceTableName) throws Exception {

        String sql = """
            SELECT raw_table_name
            FROM rn_table_catalog
            WHERE import_id = ?
              AND UPPER(source_table_name) = ?
            """;

        try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setLong(1, importId);
            statement.setString(
                2,
                sourceTableName.toUpperCase(Locale.ROOT)
            );

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalStateException(
                        "Tabella raw non trovata: "
                            + sourceTableName
                    );
                }

                return result.getString("raw_table_name");
            }
        }
    }

    private static String quoteIdentifier(String value) {
        return "\""
            + value.replace("\"", "\"\"")
            + "\"";
    }

    private static void writeJson(
            Path output,
            ExportData data) throws Exception {

        try (BufferedWriter writer = Files.newBufferedWriter(
                output,
                StandardCharsets.UTF_8)) {

            writer.write("{\n");

            writeMeta(
                writer,
                data.meta()
            );

            writer.write(",\n");

            writeTeamMatches(
                writer,
                data.teamMatches()
            );

            writer.write(",\n");

            writeExpulsionDetails(
                writer,
                data.expulsionDetails()
            );

            writer.write(",\n");

            writeEventDetails(
                writer,
                data.eventDetails()
            );

            writer.write(",\n");

            writeModifierDetails(
                writer,
                data.modifierDetails()
            );

            writer.write(",\n");

            writeCleanSheetDetails(
                writer,
                data.cleanSheetDetails()
            );

            writer.write(",\n");

            writeReserveOfficeDetails(
                writer,
                data.reserveOfficeDetails()
            );

            writer.write(",\n");

            writeGoalBandDetails(
                writer,
                data.goalBandDetails()
            );

            writer.write("}\n");
        }
    }

    private static void writeMeta(
            BufferedWriter writer,
            Meta meta) throws Exception {

        writer.write("  \"meta\": {\n");

        writeStringProperty(
            writer,
            "generatedAt",
            meta.generatedAt(),
            true,
            4
        );

        writeStringProperty(
            writer,
            "projectDir",
            meta.projectDir(),
            true,
            4
        );

        writeStringProperty(
            writer,
            "stagione",
            meta.seasonId(),
            true,
            4
        );

        writeStringProperty(
            writer,
            "competizioneStoricaId",
            meta.historicalCompetitionId(),
            true,
            4
        );

        writeStringProperty(
            writer,
            "competizioneNome",
            meta.competitionName(),
            true,
            4
        );

        writeNumberProperty(
            writer,
            "idCompetizioneFcm",
            meta.sourceCompetitionId(),
            true,
            4
        );

        writeStringProperty(
            writer,
            "nomeCompetizioneDb",
            meta.databaseCompetitionName(),
            true,
            4
        );

        writer.write("    \"idGironiInclusi\": [");

        for (
            int index = 0;
            index < meta.groupIds().size();
            index++
        ) {
            if (index > 0) {
                writer.write(", ");
            }

            writer.write(
                Integer.toString(
                    meta.groupIds().get(index)
                )
            );
        }

        writer.write("],\n");

        writeStringProperty(
            writer,
            "fcmTablesDir",
            meta.fcmSource(),
            true,
            4
        );

        writeStringProperty(
            writer,
            "fcaTablesDir",
            meta.fcaSource(),
            true,
            4
        );

        writeNumberProperty(
            writer,
            "incontriAnalizzati",
            meta.matchesAnalyzed(),
            true,
            4
        );

        writeNumberProperty(
            writer,
            "partiteSquadra",
            meta.teamMatches(),
            false,
            4
        );

        writer.write("  }");
    }

    private static void writeTeamMatches(
            BufferedWriter writer,
            List<TeamMatch> rows) throws Exception {

        writer.write("  \"partiteSquadra\": [\n");

        for (int index = 0; index < rows.size(); index++) {
            TeamMatch row = rows.get(index);

            writer.write("    {\n");

            writeStringProperty(
                writer,
                "stagione",
                row.seasonId(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "competizioneStoricaId",
                row.historicalCompetitionId(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "competizioneNome",
                row.competitionName(),
                true,
                6
            );

            writeNumberProperty(
                writer,
                "idCompetizioneFcm",
                row.sourceCompetitionId(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "nomeCompetizioneDb",
                row.databaseCompetitionName(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "idGirone",
                Integer.toString(row.groupId()),
                true,
                6
            );

            writeStringProperty(
                writer,
                "gironeNome",
                row.groupName(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "idGiornata",
                Integer.toString(row.roundId()),
                true,
                6
            );

            writeStringProperty(
                writer,
                "giornata",
                row.roundDescription(),
                true,
                6
            );

            writeNumberProperty(
                writer,
                "giornataDiA",
                row.serieARound(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "dataGiornata",
                row.matchDate(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "oraGiornata",
                row.matchTime(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "dataOraGiornata",
                row.matchDateTime(),
                true,
                6
            );

            writeNumberProperty(
                writer,
                "ordineGiornata",
                row.roundOrder(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "idIncontro",
                Long.toString(row.matchId()),
                true,
                6
            );

            writeStringProperty(
                writer,
                "urlTabellino",
                row.scorecardUrl(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "urlTabellinoLocale",
                row.localScorecardUrl(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "urlTabellinoOnline",
                row.onlineScorecardUrl(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "lato",
                row.side(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "idSquadra",
                Integer.toString(row.teamId()),
                true,
                6
            );

            writeStringProperty(
                writer,
                "squadra",
                row.teamName(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "idAvversaria",
                Integer.toString(row.opponentId()),
                true,
                6
            );

            writeStringProperty(
                writer,
                "avversaria",
                row.opponentName(),
                true,
                6
            );

            writeDecimalProperty(
                writer,
                "puntiFatti",
                row.scoreFor(),
                true,
                6
            );

            writeDecimalProperty(
                writer,
                "puntiSubiti",
                row.scoreAgainst(),
                true,
                6
            );

            writeDecimalProperty(
                writer,
                "parzialeFatto",
                row.partialFor(),
                true,
                6
            );

            writeDecimalProperty(
                writer,
                "parzialeSubito",
                row.partialAgainst(),
                true,
                6
            );

            writeNumberProperty(
                writer,
                "golFatti",
                row.goalsFor(),
                true,
                6
            );

            writeNumberProperty(
                writer,
                "golSubiti",
                row.goalsAgainst(),
                true,
                6
            );

            writeNumberProperty(
                writer,
                "golRegolamentariFatti",
                row.regulationGoalsFor(),
                true,
                6
            );

            writeNumberProperty(
                writer,
                "golRegolamentariSubiti",
                row.regulationGoalsAgainst(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "risultatoRegolamentari",
                row.regulationResult(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "fonteGolRegolamentari",
                row.regulationGoalsSource(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "esito",
                row.resultCode(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "risultato",
                row.resultText(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "punteggio",
                row.scoreText(),
                false,
                6
            );

            writer.write("    }");

            if (index + 1 < rows.size()) {
                writer.write(",");
            }

            writer.write("\n");
        }

        writer.write("  ]");
    }

    private static void writeExpulsionDetails(
            BufferedWriter writer,
            List<ExpulsionDetail> rows) throws Exception {

        writer.write("  \"espulsioniDettaglio\": [\n");

        for (int index = 0; index < rows.size(); index++) {
            ExpulsionDetail row = rows.get(index);

            writer.write("    {\n");

            writeStringProperty(
                writer,
                "idIncontro",
                Long.toString(row.matchId()),
                true,
                6
            );

            writeNumberProperty(
                writer,
                "giornataDiA",
                row.serieARound(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "idSquadra",
                Integer.toString(row.teamId()),
                true,
                6
            );

            writeStringProperty(
                writer,
                "squadra",
                row.teamName(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "idGiocatore",
                Integer.toString(row.playerId()),
                true,
                6
            );

            writeStringProperty(
                writer,
                "giocatore",
                row.playerName(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "idPunteggio",
                Integer.toString(row.scoreId()),
                false,
                6
            );

            writer.write("    }");

            if (index + 1 < rows.size()) {
                writer.write(",");
            }

            writer.write("\n");
        }

        writer.write("  ]");
    }

    private static void writeEventDetails(
            BufferedWriter writer,
            List<EventDetail> rows) throws Exception {

        writer.write("  \"eventiSquadraDettaglio\": [\n");

        for (int index = 0; index < rows.size(); index++) {
            EventDetail row = rows.get(index);

            writer.write("    {\n");

            writeStringProperty(writer, "recordKey", row.recordKey(), true, 6);
            writeStringProperty(writer, "tipoEvento", row.eventType(), true, 6);
            writeStringProperty(writer, "nomeEvento", row.eventName(), true, 6);
            writeStringProperty(writer, "campoOrigine", row.sourceField(), true, 6);
            writeStringProperty(writer, "idIncontro", Long.toString(row.matchId()), true, 6);
            writeNumberProperty(writer, "giornataDiA", row.serieARound(), true, 6);
            writeStringProperty(writer, "idSquadra", Integer.toString(row.teamId()), true, 6);
            writeStringProperty(writer, "squadra", row.teamName(), true, 6);
            writeStringProperty(writer, "idGiocatore", Integer.toString(row.playerId()), true, 6);
            writeStringProperty(writer, "giocatore", row.playerName(), true, 6);
            writeStringProperty(writer, "idPunteggio", Integer.toString(row.scoreId()), true, 6);
            writeNumberProperty(writer, "valore", row.value(), false, 6);

            writer.write("    }");

            if (index + 1 < rows.size()) {
                writer.write(",");
            }

            writer.write("\n");
        }

        writer.write("  ]");
    }

    private static void writeModifierDetails(
            BufferedWriter writer,
            List<ModifierDetail> rows) throws Exception {

        writer.write("  \"modificatoriB2Dettaglio\": [\n");

        for (int index = 0; index < rows.size(); index++) {
            ModifierDetail row = rows.get(index);

            writer.write("    {\n");

            writeStringProperty(
                writer,
                "tipo",
                row.type(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "idIncontro",
                Long.toString(row.matchId()),
                true,
                6
            );

            writeNumberProperty(
                writer,
                "giornataDiA",
                row.serieARound(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "idSquadra",
                Integer.toString(row.teamId()),
                true,
                6
            );

            writeStringProperty(
                writer,
                "squadra",
                row.teamName(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "idAvversaria",
                Integer.toString(row.opponentId()),
                true,
                6
            );

            writeStringProperty(
                writer,
                "avversaria",
                row.opponentName(),
                true,
                6
            );

            writeDecimalProperty(
                writer,
                "valore",
                row.value(),
                true,
                6
            );

            writeStringProperty(
                writer,
                "campoOrigine",
                row.sourceField(),
                false,
                6
            );

            writer.write("    }");

            if (index + 1 < rows.size()) {
                writer.write(",");
            }

            writer.write("\n");
        }

        writer.write("  ]");
    }

    private static void writeCleanSheetDetails(
            BufferedWriter writer,
            List<CleanSheetDetail> rows) throws Exception {

        writer.write("  \"cleanSheetB3Dettaglio\": [\n");

        for (int index = 0; index < rows.size(); index++) {
            CleanSheetDetail row = rows.get(index);

            writer.write("    {\n");

            writeStringProperty(writer, "tipo", "cleanSheetPortiere", true, 6);
            writeStringProperty(writer, "idIncontro", Long.toString(row.matchId()), true, 6);
            writeNumberProperty(writer, "giornataDiA", row.serieARound(), true, 6);
            writeStringProperty(writer, "idSquadra", Integer.toString(row.teamId()), true, 6);
            writeStringProperty(writer, "squadra", row.teamName(), true, 6);
            writeStringProperty(writer, "idAvversaria", Integer.toString(row.opponentId()), true, 6);
            writeStringProperty(writer, "avversaria", row.opponentName(), true, 6);
            writeStringProperty(writer, "idGiocatore", Integer.toString(row.playerId()), true, 6);
            writeStringProperty(writer, "giocatore", row.playerName(), true, 6);
            writeStringProperty(writer, "idPunteggio", Integer.toString(row.scoreId()), true, 6);
            writeNumberProperty(writer, "golSubiti", row.goalsConceded(), true, 6);
            writeDecimalProperty(writer, "valore", row.value(), true, 6);
            writeStringProperty(
                writer,
                "campoOrigine",
                "GiocatoreA.Ruolo=1 + Punteggio.GolSubiti=0",
                false,
                6
            );

            writer.write("    }");

            if (index + 1 < rows.size()) {
                writer.write(",");
            }

            writer.write("\n");
        }

        writer.write("  ]");
    }

    private static void writeStringProperty(
            BufferedWriter writer,
            String name,
            String value,
            boolean comma,
            int indent) throws Exception {

        writer.write(" ".repeat(indent));
        writer.write("\"");
        writer.write(jsonEscape(name));
        writer.write("\": ");

        if (value == null) {
            writer.write("null");
        } else {
            writer.write("\"");
            writer.write(jsonEscape(value));
            writer.write("\"");
        }

        if (comma) {
            writer.write(",");
        }

        writer.write("\n");
    }

    private static void writeNumberProperty(
            BufferedWriter writer,
            String name,
            long value,
            boolean comma,
            int indent) throws Exception {

        writer.write(" ".repeat(indent));
        writer.write("\"");
        writer.write(jsonEscape(name));
        writer.write("\": ");
        writer.write(Long.toString(value));

        if (comma) {
            writer.write(",");
        }

        writer.write("\n");
    }

    private static void writeDecimalProperty(
            BufferedWriter writer,
            String name,
            BigDecimal value,
            boolean comma,
            int indent) throws Exception {

        writer.write(" ".repeat(indent));
        writer.write("\"");
        writer.write(jsonEscape(name));
        writer.write("\": ");

        if (value == null) {
            writer.write("null");
        } else {
            writer.write(decimalText(value));
        }

        if (comma) {
            writer.write(",");
        }

        writer.write("\n");
    }

    private static String decimalText(BigDecimal value) {
        if (value == null) {
            return "";
        }

        return value
            .stripTrailingZeros()
            .toPlainString();
    }

    private static BigDecimal zeroIfNull(
            BigDecimal value) {

        return value == null
            ? BigDecimal.ZERO
            : value;
    }

    private static String emptyIfNull(
            String value) {

        return value == null
            ? ""
            : value;
    }

    private static boolean tableExists(
            Connection connection,
            String tableName) throws Exception {

        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM sqlite_master "
                    + "WHERE type = 'table' AND name = ?")) {
            statement.setString(1, tableName);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private static ScorecardBases readScorecardBases(
            Connection connection,
            String seasonId) throws Exception {

        if (!tableExists(connection, "rn_season_configuration")) {
            return new ScorecardBases(null, null);
        }

        try (PreparedStatement statement = connection.prepareStatement("""
            SELECT local_site_path, online_site_url
            FROM rn_season_configuration
            WHERE season_id = ?
            """)) {
            statement.setString(1, seasonId);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return new ScorecardBases(null, null);
                }

                String localPath = result.getString("local_site_path");
                String onlineRoot = result.getString("online_site_url");

                String localBase = null;
                if (localPath != null && !localPath.isBlank()) {
                    Path fileName = Path.of(localPath).normalize().getFileName();
                    if (fileName != null && !fileName.toString().isBlank()) {
                        localBase = "../" + fileName + "/ris.htm?Gio=";
                    }
                }

                String onlineBase = null;
                if (onlineRoot != null && !onlineRoot.isBlank()) {
                    onlineBase = onlineRoot.replaceAll("/+$", "")
                        + "/ris.htm?Gio=";
                }

                return new ScorecardBases(localBase, onlineBase);
            }
        }
    }

    private static String scorecardUrl(
            String seasonId,
            int serieARound) {

        String[] parts = seasonId.split("_", -1);

        if (parts.length != 2 || !parts[0].matches("\\d{4}")) {
            throw new IllegalArgumentException(
                "Stagione non valida per URL tabellino: "
                    + seasonId
            );
        }

        return "../lega"
            + parts[0]
            + "/ris.htm?Gio="
            + serieARound;
    }
    private static String outputHistoricalCompetitionId(
            String competitionName) {

        return switch (competitionName) {
            case "Coppa Serie A" ->
                "coppa_lega_serie_a";

            case "Coppa Serie B" ->
                "coppa_lega_serie_b";

            case "Coppa Serie C" ->
                "coppa_lega_serie_c";

            default ->
                historicalCompetitionId(
                    competitionName
                );
        };
    }

    private static String outputCompetitionName(
            String competitionName) {

        return switch (competitionName) {
            case "Coppa Serie A" ->
                "Coppa di Lega Serie A";

            case "Coppa Serie B" ->
                "Coppa di Lega Serie B";

            case "Coppa Serie C" ->
                "Coppa di Lega Serie C";

            default -> competitionName;
        };
    }

    private static String historicalCompetitionId(
            String competitionName) {

        return competitionName
            .trim()
            .toLowerCase(Locale.ROOT)
            .replace('\u00e0', 'a')
            .replace('\u00e8', 'e')
            .replace('\u00e9', 'e')
            .replace('\u00ec', 'i')
            .replace('\u00f2', 'o')
            .replace('\u00f9', 'u')
            .replaceAll("[^a-z0-9]+", "_")
            .replaceAll("^_+|_+$", "");
    }

    private static String jsonEscape(String value) {
        StringBuilder escaped = new StringBuilder();

        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);

            switch (current) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");

                default -> {
                    if (current < 0x20) {
                        escaped.append(
                            String.format(
                                Locale.ROOT,
                                "\\u%04x",
                                (int) current
                            )
                        );
                    } else {
                        escaped.append(current);
                    }
                }
            }
        }

        return escaped.toString();
    }

    private record CompetitionInfo(
        long identityId,
        int sourceCompetitionId
    ) {
    }

    private record Meta(
        String generatedAt,
        String projectDir,
        String seasonId,
        String historicalCompetitionId,
        String competitionName,
        int sourceCompetitionId,
        String databaseCompetitionName,
        List<Integer> groupIds,
        String fcmSource,
        String fcaSource,
        int matchesAnalyzed,
        int teamMatches
    ) {
    }

    private record ScorecardBases(
        String localBase,
        String onlineBase
    ) {
        String localUrl(int serieARound) {
            return localBase == null ? null : localBase + serieARound;
        }

        String onlineUrl(int serieARound) {
            return onlineBase == null ? null : onlineBase + serieARound;
        }
    }

    private record TeamMatch(
        String seasonId,
        String historicalCompetitionId,
        String competitionName,
        int sourceCompetitionId,
        String databaseCompetitionName,
        int groupId,
        String groupName,
        int roundId,
        String roundDescription,
        int serieARound,
        String matchDate,
        String matchTime,
        String matchDateTime,
        int roundOrder,
        long matchId,
        String scorecardUrl,
        String localScorecardUrl,
        String onlineScorecardUrl,
        String side,
        int teamId,
        String teamName,
        int opponentId,
        String opponentName,
        BigDecimal scoreFor,
        BigDecimal scoreAgainst,
        BigDecimal partialFor,
        BigDecimal partialAgainst,
        int goalsFor,
        int goalsAgainst,
        int regulationGoalsFor,
        int regulationGoalsAgainst,
        String regulationResult,
        String regulationGoalsSource,
        String resultCode,
        String resultText,
        String scoreText
    ) {
    }

    private record SourceInfo(
        long sourceFileId,
        long importId
    ) {
    }

    private record ExpulsionDetail(
        long matchId,
        int serieARound,
        int teamId,
        String teamName,
        int playerId,
        String playerName,
        int scoreId
    ) {
    }

    private static void writeGoalBandDetails(
            BufferedWriter writer,
            List<GoalBandDetail> rows) throws Exception {

        writer.write("  \"fasceGolDettaglio\": [\n");

        for (int index = 0; index < rows.size(); index++) {
            GoalBandDetail row = rows.get(index);
            writer.write("    {\n");
            writeNumberProperty(writer, "idCompetizioneFcm", row.sourceCompetitionId(), true, 6);
            writeStringProperty(writer, "idFascia", row.sourceBandId(), true, 6);
            writeDecimalProperty(writer, "min", row.minScore(), true, 6);
            writeDecimalProperty(writer, "max", row.maxScore(), true, 6);
            writeNumberProperty(writer, "gol", row.goals(), false, 6);
            writer.write("    }");
            if (index + 1 < rows.size()) {
                writer.write(",");
            }
            writer.write("\n");
        }

        writer.write("  ]");
    }

    private record GoalBandDetail(
        int sourceCompetitionId,
        String sourceBandId,
        BigDecimal minScore,
        BigDecimal maxScore,
        int goals
    ) {
    }

    private static void writeReserveOfficeDetails(
            BufferedWriter writer,
            List<ReserveOfficeDetail> rows) throws Exception {

        writer.write("  \"riserveUfficioDettaglio\": [\n");

        for (int index = 0; index < rows.size(); index++) {
            ReserveOfficeDetail row = rows.get(index);
            writer.write("    {\n");
            writeStringProperty(writer, "idIncontro", Long.toString(row.matchId()), true, 6);
            writeNumberProperty(writer, "giornataDiA", row.serieARound(), true, 6);
            writeStringProperty(writer, "idSquadra", Integer.toString(row.teamId()), true, 6);
            writeStringProperty(writer, "squadra", row.teamName(), true, 6);
            writeStringProperty(writer, "idAvversaria", Integer.toString(row.opponentId()), true, 6);
            writeStringProperty(writer, "avversaria", row.opponentName(), true, 6);
            writeStringProperty(writer, "tipoRU", row.roleCode(), true, 6);
            writeStringProperty(writer, "ruoloRU", row.roleName(), true, 6);
            writeNumberProperty(writer, "ordine", row.order(), true, 6);
            writeStringProperty(writer, "votoTabellino", row.vote(), true, 6);
            writeStringProperty(writer, "modifTabellino", row.modifier(), true, 6);
            writeStringProperty(writer, "totTabellino", row.total(), true, 6);
            writeDecimalProperty(writer, "valoreRU", row.value(), false, 6);
            writer.write("    }");
            if (index + 1 < rows.size()) {
                writer.write(",");
            }
            writer.write("\n");
        }

        writer.write("  ]");
    }

    private record ReserveOfficeDetail(
        long matchId,
        int serieARound,
        int teamId,
        String teamName,
        int opponentId,
        String opponentName,
        String roleCode,
        String roleName,
        int order,
        String vote,
        String modifier,
        String total,
        BigDecimal value
    ) {
    }

    private record EventDetail(
        String recordKey,
        String eventType,
        String eventName,
        String sourceField,
        long matchId,
        int serieARound,
        int teamId,
        String teamName,
        int playerId,
        String playerName,
        int scoreId,
        int value
    ) {
    }

    private record CleanSheetDetail(
        long matchId,
        int serieARound,
        int teamId,
        String teamName,
        int opponentId,
        String opponentName,
        int playerId,
        String playerName,
        int scoreId,
        int goalsConceded,
        BigDecimal value
    ) {
    }

    private record ModifierDetail(
        String type,
        long matchId,
        int serieARound,
        int teamId,
        String teamName,
        int opponentId,
        String opponentName,
        BigDecimal value,
        String sourceField
    ) {
    }

    private record ExportData(
        Meta meta,
        List<TeamMatch> teamMatches,
        List<ExpulsionDetail> expulsionDetails,
        List<EventDetail> eventDetails,
        List<ModifierDetail> modifierDetails,
        List<CleanSheetDetail> cleanSheetDetails,
        List<ReserveOfficeDetail> reserveOfficeDetails,
        List<GoalBandDetail> goalBandDetails
    ) {
    }
}
```

### src\main\java\it\alterlega\recordsnext\SeasonRecordsArchiveBuilder.java

```java
package it.alterlega.recordsnext;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Costruisce l'archivio season_records_*.json a partire dai JSON normalizzati
 * prodotti da RecordsNext.
 *
 * Genera tutte le 18 sezioni del contratto pubblico Records2026 usando i
 * dati normalizzati correnti, compresi i bonus capitano presenti in
 * modificatoriB2Dettaglio come tipo=capitano / campoOrigine=ModM2Pers.
 */
public final class SeasonRecordsArchiveBuilder {

    private SeasonRecordsArchiveBuilder() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Uso: SeasonRecordsArchiveBuilder <reportsRoot> <archiveRoot> [stagione ...]");
            System.exit(2);
        }
        Path reportsRoot = Path.of(args[0]).toAbsolutePath().normalize();
        Path archiveRoot = Path.of(args[1]).toAbsolutePath().normalize();
        List<String> seasons = new ArrayList<>();
        for (int i = 2; i < args.length; i++) {
            if (!args[i].isBlank()) seasons.add(args[i].trim());
        }
        Result result = build(reportsRoot, archiveRoot, seasons);
        System.out.println("Report       : " + reportsRoot);
        System.out.println("Archivio     : " + archiveRoot);
        System.out.println("Stagioni     : " + result.seasons());
        System.out.println("Competizioni : " + result.competitions());
        System.out.println("Sezioni      : 18/18");
    }

    public static Result build(Path reportsRoot, Path archiveRoot, List<String> requestedSeasons) throws IOException {
        if (!Files.isDirectory(reportsRoot)) {
            throw new IOException("Cartella report non trovata: " + reportsRoot);
        }
        Files.createDirectories(archiveRoot);

        List<Path> seasonDirs = resolveSeasonDirs(reportsRoot, requestedSeasons);
        int competitions = 0;
        int seasons = 0;
        for (Path seasonDir : seasonDirs) {
            List<Path> normalizedFiles = listNormalizedFiles(seasonDir);
            if (normalizedFiles.isEmpty()) continue;
            seasons++;
            Path outputSeason = archiveRoot.resolve(seasonDir.getFileName().toString());
            Files.createDirectories(outputSeason);
            for (Path normalizedFile : normalizedFiles) {
                Map<String, Object> source = object(parse(normalizedFile), normalizedFile, "radice");
                Map<String, Object> meta = object(source.get("meta"), normalizedFile, "meta");
                String competitionId = string(meta.get("competizioneStoricaId"));
                if (competitionId.isBlank()) {
                    String name = normalizedFile.getFileName().toString();
                    competitionId = name.substring("season_normalized_".length(), name.length() - ".json".length());
                }
                Map<String, Object> output = buildCompetition(source, meta);
                Path target = outputSeason.resolve("season_records_" + competitionId + ".json");
                String json = JsonWriter.writePretty(output) + System.lineSeparator();
                Files.writeString(target, json, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
                competitions++;
            }
        }
        if (competitions == 0) throw new IOException("Nessun season_normalized_*.json trovato in " + reportsRoot);
        return new Result(seasons, competitions);
    }

    private static Map<String, Object> buildCompetition(Map<String, Object> source, Map<String, Object> sourceMeta) {
        List<Map<String, Object>> matches = rows(source.get("partiteSquadra"));
        List<Map<String, Object>> expulsions = rows(source.get("espulsioniDettaglio"));
        List<Map<String, Object>> events = rows(source.get("eventiSquadraDettaglio"));
        List<Map<String, Object>> modifiers = rows(source.get("modificatoriB2Dettaglio"));
        List<Map<String, Object>> cleanSheets = rows(source.get("cleanSheetB3Dettaglio"));

        Map<String, Object> records = new LinkedHashMap<>();
        records.put("puntiSquadraMax", pointsMax(matches));
        records.put("serieSenzaSconfitte", unbeatenSeries(matches));
        records.put("espulsioniSquadre", expulsionsByTeam(expulsions));
        records.put("espulsioniGiocatori", expulsionsByPlayer(expulsions));
        records.put("ammonizioniSquadre", eventByTeam(events, "ammonizioniSquadre", "Maggiori ammonizioni"));
        records.put("assistSquadre", eventByTeam(events, "assistSquadre", "Maggiori assist"));
        records.put("autogolSquadre", eventByTeam(events, "autogolSquadre", "Maggiori autogol"));
        records.put("rigoriSbagliatiSquadre", eventByTeam(events, "rigoriSbagliatiSquadre", "Maggiori rigori sbagliati"));
        records.put("rigoriParatiSquadre", eventByTeam(events, "rigoriParatiSquadre", "Maggiori rigori parati"));
        records.put("golRigoreSquadre", eventByTeam(events, "golRigoreSquadre", "Maggiori gol fatti su rigore"));
        records.put("modDifesaMax", modifierMax(modifiers));
        records.put("modDifesaTotaleSquadre", modifierTotal(modifiers));
        records.put("capitanoVolteSquadre", captainCount(modifiers));
        records.put("capitanoTotaleSquadre", captainTotal(modifiers));
        records.put("cleanSheetPortiereVolteSquadre", cleanSheetCount(cleanSheets));
        records.put("cleanSheetPortiereTotaleSquadre", cleanSheetTotal(cleanSheets));
        records.put("cleanSheetPortiereSerieSquadre", cleanSheetSeries(matches, cleanSheets));
        records.put("capitanoSerieSquadre", captainSeries(matches, modifiers));

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.putAll(sourceMeta);
        meta.put("builder", "RecordsNext SeasonRecordsArchiveBuilder");
        meta.put("sezioniGenerate", 18);
        meta.put("sezioniAttese", 18);
        meta.put("sezioniNonDisponibili", List.of());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("meta", meta);
        result.put("records", records);
        return result;
    }

    private static List<Object> pointsMax(List<Map<String, Object>> matches) {
        List<Map<String, Object>> sorted = new ArrayList<>(matches);
        sorted.sort(Comparator.comparingDouble((Map<String, Object> r) -> number(r.get("puntiFatti"))).reversed()
                .thenComparing(r -> string(r.get("idIncontro"))));
        List<Object> out = new ArrayList<>();
        for (int i = 0; i < Math.min(20, sorted.size()); i++) {
            Map<String, Object> r = sorted.get(i);
            Map<String, Object> row = ordered(
                    "recordId", "punti_squadra_max",
                    "nome", "Maggior numero di punti fatti",
                    "stagione", r.get("stagione"),
                    "competizioneStoricaId", r.get("competizioneStoricaId"),
                    "competizioneNome", r.get("competizioneNome"),
                    "valore", r.get("puntiFatti"),
                    "squadra", r.get("squadra"),
                    "avversaria", r.get("avversaria"),
                    "idIncontro", r.get("idIncontro"),
                    "giornata", r.get("giornata"),
                    "giornataDiA", r.get("giornataDiA"),
                    "urlTabellino", r.get("urlTabellino"),
                    "risultato", r.get("risultato"),
                    "punteggio", r.get("punteggio"));
            row.put("dettagli", ordered(
                    "parzialeFatto", r.get("parzialeFatto"),
                    "parzialeSubito", r.get("parzialeSubito"),
                    "puntiSubiti", r.get("puntiSubiti"),
                    "golFatti", r.get("golFatti"),
                    "golSubiti", r.get("golSubiti"),
                    "golRegolamentariFatti", r.get("golRegolamentariFatti"),
                    "golRegolamentariSubiti", r.get("golRegolamentariSubiti"),
                    "risultatoRegolamentari", r.get("risultatoRegolamentari"),
                    "fonteGolRegolamentari", r.get("fonteGolRegolamentari")));
            out.add(row);
        }
        return out;
    }

    private static List<Object> unbeatenSeries(List<Map<String, Object>> matches) {
        Map<String, List<Map<String, Object>>> byTeam = group(matches, "idSquadra");
        List<Map<String, Object>> records = new ArrayList<>();
        for (List<Map<String, Object>> teamMatches : byTeam.values()) {
            teamMatches.sort(Comparator.comparingDouble((Map<String, Object> r) -> number(r.get("ordineGiornata")))
                    .thenComparing(r -> string(r.get("idIncontro"))));
            List<Map<String, Object>> current = new ArrayList<>();
            for (Map<String, Object> match : teamMatches) {
                if (!"S".equals(string(match.get("esito")))) {
                    current.add(match);
                } else {
                    addUnbeaten(records, current);
                    current = new ArrayList<>();
                }
            }
            addUnbeaten(records, current);
        }
        records.sort(Comparator.comparingDouble((Map<String, Object> r) -> number(r.get("valore"))).reversed()
                .thenComparing(Comparator.comparingDouble((Map<String, Object> r) -> number(r.get("vittorie"))).reversed())
                .thenComparing(r -> string(r.get("squadra"))));
        return new ArrayList<>(records.subList(0, Math.min(20, records.size())));
    }

    private static void addUnbeaten(List<Map<String, Object>> records, List<Map<String, Object>> series) {
        if (series.isEmpty()) return;
        Map<String, Object> first = series.get(0), last = series.get(series.size() - 1);
        long wins = series.stream().filter(r -> "V".equals(string(r.get("esito")))).count();
        long draws = series.stream().filter(r -> "P".equals(string(r.get("esito")))).count();
        List<Object> details = new ArrayList<>();
        for (Map<String, Object> r : series) {
            details.add(ordered("idIncontro", r.get("idIncontro"), "giornata", r.get("giornata"),
                    "giornataDiA", r.get("giornataDiA"), "urlTabellino", r.get("urlTabellino"),
                    "avversaria", r.get("avversaria"), "risultato", r.get("risultato"),
                    "punteggio", r.get("punteggio"), "esito", r.get("esito")));
        }
        Map<String, Object> row = ordered(
                "recordId", "serie_senza_sconfitte", "nome", "Partite consecutive senza sconfitte",
                "stagione", first.get("stagione"), "competizioneStoricaId", first.get("competizioneStoricaId"),
                "competizioneNome", first.get("competizioneNome"), "valore", series.size(),
                "squadra", first.get("squadra"), "idSquadra", first.get("idSquadra"),
                "daGiornata", first.get("giornata"), "aGiornata", last.get("giornata"),
                "daGiornataDiA", first.get("giornataDiA"), "aGiornataDiA", last.get("giornataDiA"),
                "vittorie", wins, "pareggi", draws);
        row.put("dettagli", details);
        records.add(row);
    }

    private static List<Object> expulsionsByTeam(List<Map<String, Object>> rows) {
        return aggregateCount(rows, "idSquadra", "squadra", "espulsioni_squadra",
                "Maggiori espulsioni squadra", List.of("idIncontro", "giornataDiA", "idGiocatore", "giocatore"));
    }

    private static List<Object> expulsionsByPlayer(List<Map<String, Object>> rows) {
        return aggregateCount(rows, "idGiocatore", "giocatore", "espulsioni_giocatore",
                "Maggiori espulsioni giocatore", List.of("idIncontro", "giornataDiA", "idSquadra", "squadra"));
    }

    private static List<Object> aggregateCount(List<Map<String, Object>> rows, String idField, String nameField,
                                                String recordId, String name, List<String> detailFields) {
        Map<String, List<Map<String, Object>>> groups = group(rows, idField);
        List<Map<String, Object>> out = new ArrayList<>();
        for (List<Map<String, Object>> group : groups.values()) {
            Map<String, Object> first = group.get(0);
            Map<String, Object> item = ordered("recordId", recordId, "nome", name, "valore", group.size(),
                    idField, first.get(idField), nameField, first.get(nameField));
            List<Object> details = new ArrayList<>();
            for (Map<String, Object> r : group) details.add(project(r, detailFields));
            item.put("dettagli", details);
            out.add(item);
        }
        out.sort(Comparator.comparingDouble((Map<String, Object> r) -> number(r.get("valore"))).reversed()
                .thenComparing(r -> string(r.get(nameField))));
        return new ArrayList<>(out);
    }

    private static List<Object> eventByTeam(List<Map<String, Object>> events, String key, String name) {
        List<Map<String, Object>> selected = events.stream().filter(r -> key.equals(string(r.get("recordKey")))).toList();
        Map<String, List<Map<String, Object>>> groups = group(selected, "idSquadra");
        List<Map<String, Object>> out = new ArrayList<>();
        for (List<Map<String, Object>> group : groups.values()) {
            group.sort(Comparator.comparingDouble((Map<String, Object> r) -> number(r.get("giornataDiA")))
                    .thenComparing(r -> string(r.get("idIncontro"))).thenComparing(r -> string(r.get("giocatore"))));
            Map<String, Object> first = group.get(0);
            double total = group.stream().mapToDouble(r -> number(r.get("valore"))).sum();
            Map<String, Object> item = ordered("recordId", key, "nome", name, "valore", cleanNumber(total),
                    "idSquadra", first.get("idSquadra"), "squadra", first.get("squadra"));
            List<Object> details = new ArrayList<>();
            for (Map<String, Object> r : group) {
                details.add(ordered("giornataDiA", r.get("giornataDiA"), "idIncontro", r.get("idIncontro"),
                        "idGiocatore", r.get("idGiocatore"), "giocatore", r.get("giocatore"),
                        "valore", r.get("valore"), "campoOrigine", r.get("campoOrigine")));
            }
            item.put("dettagli", details);
            out.add(item);
        }
        out.sort(Comparator.comparingDouble((Map<String, Object> r) -> number(r.get("valore"))).reversed()
                .thenComparing(r -> string(r.get("squadra"))));
        return new ArrayList<>(out);
    }

    private static List<Object> modifierMax(List<Map<String, Object>> modifiers) {
        List<Map<String, Object>> rows = modifiers.stream().filter(r -> "modDifesa".equals(string(r.get("tipo"))))
                .sorted(Comparator.comparingDouble((Map<String, Object> r) -> number(r.get("valore"))).reversed()
                        .thenComparing(r -> string(r.get("squadra"))))
                .limit(20).toList();
        List<Object> out = new ArrayList<>();
        for (Map<String, Object> r : rows) out.add(ordered("recordId", "modDifesaMax", "nome", "Miglior modificatore difesa",
                "valore", r.get("valore"), "idSquadra", r.get("idSquadra"), "squadra", r.get("squadra"),
                "avversaria", r.get("avversaria"), "idIncontro", r.get("idIncontro"), "giornataDiA", r.get("giornataDiA")));
        return out;
    }

    private static List<Object> modifierTotal(List<Map<String, Object>> modifiers) {
        List<Map<String, Object>> selected = modifiers.stream().filter(r -> "modDifesa".equals(string(r.get("tipo")))).toList();
        return aggregateSum(selected, "modDifesaTotaleSquadre", "Maggior totale modificatore difesa",
                List.of("idIncontro", "giornataDiA", "avversaria", "valore"));
    }

    private static List<Object> captainCount(List<Map<String, Object>> modifiers) {
        List<Map<String, Object>> selected = modifiers.stream()
                .filter(r -> "capitano".equals(string(r.get("tipo"))))
                .toList();
        Map<String, List<Map<String, Object>>> groups = group(selected, "idSquadra");
        List<Map<String, Object>> out = new ArrayList<>();
        for (List<Map<String, Object>> group : groups.values()) {
            group.sort(Comparator.comparingDouble((Map<String, Object> r) -> number(r.get("giornataDiA")))
                    .thenComparing(r -> string(r.get("idIncontro"))));
            Map<String, Object> first = group.get(0);
            Map<String, Object> item = ordered(
                    "recordId", "capitanoVolteSquadre",
                    "nome", "Maggior numero bonus capitano",
                    "valore", group.size(),
                    "idSquadra", first.get("idSquadra"),
                    "squadra", first.get("squadra"));
            item.put("dettagli", modifierDetails(group));
            out.add(item);
        }
        sortValueTeam(out);
        return new ArrayList<>(out);
    }

    private static List<Object> captainTotal(List<Map<String, Object>> modifiers) {
        List<Map<String, Object>> selected = modifiers.stream()
                .filter(r -> "capitano".equals(string(r.get("tipo"))))
                .toList();
        return aggregateSum(selected, "capitanoTotaleSquadre",
                "Maggior totale bonus capitano",
                List.of("idIncontro", "giornataDiA", "avversaria", "valore"));
    }

    private static List<Object> modifierDetails(List<Map<String, Object>> group) {
        List<Object> details = new ArrayList<>();
        for (Map<String, Object> r : group) {
            details.add(ordered(
                    "idIncontro", r.get("idIncontro"),
                    "giornataDiA", r.get("giornataDiA"),
                    "avversaria", r.get("avversaria"),
                    "valore", r.get("valore")));
        }
        return details;
    }

    private static List<Object> captainSeries(List<Map<String, Object>> matches,
                                               List<Map<String, Object>> modifiers) {
        List<Map<String, Object>> captain = modifiers.stream()
                .filter(r -> "capitano".equals(string(r.get("tipo"))))
                .toList();
        return eventSeries(matches, captain, "capitanoSerieSquadre",
                "Maggior serie bonus capitano");
    }

    private static List<Object> cleanSheetCount(List<Map<String, Object>> clean) {
        Map<String, List<Map<String, Object>>> groups = group(clean, "idSquadra");
        List<Map<String, Object>> out = new ArrayList<>();
        for (List<Map<String, Object>> group : groups.values()) {
            Map<String, Object> first = group.get(0);
            Map<String, Object> item = ordered("recordId", "cleanSheetPortiereVolteSquadre",
                    "nome", "Maggior numero clean sheet portiere", "valore", group.size(),
                    "idSquadra", first.get("idSquadra"), "squadra", first.get("squadra"));
            item.put("dettagli", cleanDetails(group));
            out.add(item);
        }
        sortValueTeam(out);
        return new ArrayList<>(out);
    }

    private static List<Object> cleanSheetTotal(List<Map<String, Object>> clean) {
        Map<String, List<Map<String, Object>>> groups = group(clean, "idSquadra");
        List<Map<String, Object>> out = new ArrayList<>();
        for (List<Map<String, Object>> group : groups.values()) {
            Map<String, Object> first = group.get(0);
            double total = group.stream().mapToDouble(r -> number(r.get("valore"))).sum();
            Map<String, Object> item = ordered("recordId", "cleanSheetPortiereTotaleSquadre",
                    "nome", "Maggior totale bonus clean sheet portiere", "valore", cleanNumber(total),
                    "idSquadra", first.get("idSquadra"), "squadra", first.get("squadra"));
            item.put("dettagli", cleanDetails(group));
            out.add(item);
        }
        sortValueTeam(out);
        return new ArrayList<>(out);
    }

    private static List<Object> cleanDetails(List<Map<String, Object>> group) {
        List<Map<String, Object>> sorted = new ArrayList<>(group);
        sorted.sort(Comparator.comparingDouble((Map<String, Object> r) -> number(r.get("giornataDiA")))
                .thenComparing(r -> string(r.get("idIncontro"))));
        List<Object> details = new ArrayList<>();
        for (Map<String, Object> r : sorted) details.add(ordered("idIncontro", r.get("idIncontro"),
                "giornataDiA", r.get("giornataDiA"), "avversaria", r.get("avversaria"),
                "giocatore", r.get("giocatore"), "valore", r.get("valore")));
        return details;
    }

    private static List<Object> cleanSheetSeries(List<Map<String, Object>> matches, List<Map<String, Object>> clean) {
        return eventSeries(matches, clean, "cleanSheetPortiereSerieSquadre",
                "Maggior serie clean sheet portiere");
    }

    private static List<Object> eventSeries(List<Map<String, Object>> matches,
                                            List<Map<String, Object>> events,
                                            String recordId, String name) {
        Set<String> eventKeys = new LinkedHashSet<>();
        for (Map<String, Object> r : events) {
            eventKeys.add(string(r.get("idSquadra")) + "|" + string(r.get("idIncontro")));
        }

        Map<String, List<Map<String, Object>>> byTeam = group(matches, "idSquadra");
        List<Map<String, Object>> out = new ArrayList<>();

        for (List<Map<String, Object>> teamMatches : byTeam.values()) {
            teamMatches.sort(Comparator
                    .comparingDouble((Map<String, Object> r) -> number(r.get("giornataDiA")))
                    .thenComparing(r -> string(r.get("idIncontro"))));

            List<Map<String, Object>> best = new ArrayList<>();
            List<Map<String, Object>> current = new ArrayList<>();

            for (Map<String, Object> match : teamMatches) {
                String key = string(match.get("idSquadra")) + "|" + string(match.get("idIncontro"));
                if (eventKeys.contains(key)) {
                    current.add(match);
                } else {
                    if (current.size() > best.size()) best = new ArrayList<>(current);
                    current.clear();
                }
            }
            if (current.size() > best.size()) best = new ArrayList<>(current);

            if (!best.isEmpty()) out.add(eventSeriesRecord(best, recordId, name));
        }

        sortValueTeam(out);
        return new ArrayList<>(out);
    }

    private static Map<String, Object> eventSeriesRecord(List<Map<String, Object>> series,
                                                          String recordId, String name) {
        Map<String, Object> first = series.get(0);
        Map<String, Object> last = series.get(series.size() - 1);
        return ordered(
                "recordId", recordId,
                "nome", name,
                "valore", series.size(),
                "idSquadra", first.get("idSquadra"),
                "squadra", first.get("squadra"),
                "daGiornataDiA", first.get("giornataDiA"),
                "aGiornataDiA", last.get("giornataDiA"),
                "dettagli", series.stream().map(r -> ordered(
                        "idIncontro", r.get("idIncontro"),
                        "giornataDiA", r.get("giornataDiA"),
                        "avversaria", r.get("avversaria"))).toList());
    }

    private static List<Object> aggregateSum(List<Map<String, Object>> rows, String recordId, String name,
                                              List<String> detailFields) {
        Map<String, List<Map<String, Object>>> groups = group(rows, "idSquadra");
        List<Map<String, Object>> out = new ArrayList<>();
        for (List<Map<String, Object>> group : groups.values()) {
            group.sort(Comparator.comparingDouble((Map<String, Object> r) -> number(r.get("giornataDiA")))
                    .thenComparing(r -> string(r.get("idIncontro"))));
            Map<String, Object> first = group.get(0);
            double total = group.stream().mapToDouble(r -> number(r.get("valore"))).sum();
            Map<String, Object> item = ordered("recordId", recordId, "nome", name, "valore", cleanNumber(total),
                    "idSquadra", first.get("idSquadra"), "squadra", first.get("squadra"));
            List<Object> details = new ArrayList<>();
            for (Map<String, Object> r : group) details.add(project(r, detailFields));
            item.put("dettagli", details);
            out.add(item);
        }
        sortValueTeam(out);
        return new ArrayList<>(out);
    }

    private static void sortValueTeam(List<Map<String, Object>> out) {
        out.sort(Comparator.comparingDouble((Map<String, Object> r) -> number(r.get("valore"))).reversed()
                .thenComparing(r -> string(r.get("squadra"))));
    }

    private static Map<String, List<Map<String, Object>>> group(List<Map<String, Object>> rows, String field) {
        Map<String, List<Map<String, Object>>> groups = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) groups.computeIfAbsent(string(row.get(field)), k -> new ArrayList<>()).add(row);
        return groups;
    }

    private static Map<String, Object> project(Map<String, Object> row, List<String> fields) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (String field : fields) result.put(field, row.get(field));
        return result;
    }

    private static Map<String, Object> ordered(Object... values) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) map.put(String.valueOf(values[i]), values[i + 1]);
        return map;
    }

    private static long longNumber(Object value) {
        String text = string(value);
        if (text.isBlank()) return 0L;
        try {
            return new BigDecimal(text.replace(',', '.')).longValue();
        }
        catch (NumberFormatException ex) {
            return 0L;
        }
    }

    private static Object cleanNumber(double value) {
        if (Math.rint(value) == value) return (long) value;
        return BigDecimal.valueOf(value).stripTrailingZeros();
    }

    private static List<Path> resolveSeasonDirs(Path root, List<String> requested) throws IOException {
        if (!requested.isEmpty()) {
            List<Path> result = new ArrayList<>();
            for (String season : requested) {
                Path dir = root.resolve(season);
                if (!Files.isDirectory(dir)) throw new IOException("Stagione non trovata: " + dir);
                result.add(dir);
            }
            result.sort(Comparator.comparing(p -> p.getFileName().toString()));
            return result;
        }
        try (Stream<Path> stream = Files.list(root)) {
            return stream.filter(Files::isDirectory).sorted(Comparator.comparing(p -> p.getFileName().toString())).toList();
        }
    }

    private static List<Path> listNormalizedFiles(Path seasonDir) throws IOException {
        try (Stream<Path> stream = Files.list(seasonDir)) {
            return stream.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().startsWith("season_normalized_"))
                    .filter(p -> p.getFileName().toString().endsWith(".json"))
                    .filter(p -> !p.getFileName().toString().contains(".stage"))
                    .filter(p -> !p.getFileName().toString().contains(".final"))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString())).toList();
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> rows(Object value) {
        if (!(value instanceof List<?> list)) return new ArrayList<>();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : list) if (item instanceof Map<?, ?> map) result.add((Map<String, Object>) map);
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> object(Object value, Path source, String name) throws IOException {
        if (!(value instanceof Map<?, ?> map)) throw new IOException("Oggetto " + name + " mancante in " + source);
        return (Map<String, Object>) map;
    }

    private static String string(Object value) { return value == null ? "" : String.valueOf(value); }
    private static double number(Object value) {
        if (value instanceof Number n) return n.doubleValue();
        if (value == null || string(value).isBlank()) return 0;
        try { return Double.parseDouble(string(value).replace(',', '.')); } catch (NumberFormatException e) { return 0; }
    }

    private static Object parse(Path source) throws IOException {
        String text = Files.readString(source, StandardCharsets.UTF_8);
        if (!text.isEmpty() && text.charAt(0) == '\uFEFF') text = text.substring(1);
        return new JsonParser(text, source).parse();
    }

    public record Result(int seasons, int competitions) {}

    private static final class JsonParser {
        private final String text; private final Path source; private int index;
        JsonParser(String text, Path source) { this.text = text; this.source = source; }
        Object parse() throws IOException { skip(); Object v = value(); skip(); if (index != text.length()) fail("contenuto dopo JSON"); return v; }
        private Object value() throws IOException {
            skip(); if (index >= text.length()) fail("fine inattesa"); char c = text.charAt(index);
            return switch (c) { case '{' -> object(); case '[' -> array(); case '"' -> string(); case 't' -> literal("true", true); case 'f' -> literal("false", false); case 'n' -> literal("null", null); default -> number(); };
        }
        private Map<String,Object> object() throws IOException { expect('{'); Map<String,Object> m=new LinkedHashMap<>(); skip(); if (take('}')) return m; while(true){ skip(); String k=string(); skip(); expect(':'); m.put(k,value()); skip(); if(take('}')) return m; expect(','); } }
        private List<Object> array() throws IOException { expect('['); List<Object> a=new ArrayList<>(); skip(); if(take(']')) return a; while(true){ a.add(value()); skip(); if(take(']')) return a; expect(','); } }
        private String string() throws IOException { expect('"'); StringBuilder b=new StringBuilder(); while(index<text.length()){ char c=text.charAt(index++); if(c=='"') return b.toString(); if(c!='\\'){ b.append(c); continue;} if(index>=text.length()) fail("escape incompleto"); char e=text.charAt(index++); switch(e){case '"','\\','/'->b.append(e); case 'b'->b.append('\b'); case 'f'->b.append('\f'); case 'n'->b.append('\n'); case 'r'->b.append('\r'); case 't'->b.append('\t'); case 'u'->{ if(index+4>text.length()) fail("unicode incompleto"); b.append((char)Integer.parseInt(text.substring(index,index+4),16)); index+=4;} default->fail("escape non valido");}} fail("stringa non chiusa"); return ""; }
        private Object number() throws IOException { int s=index; if(peek('-')) index++; digits(); boolean dec=false; if(peek('.')){dec=true;index++;digits();} if(peek('e')||peek('E')){dec=true;index++;if(peek('+')||peek('-'))index++;digits();} String raw=text.substring(s,index); try{ BigDecimal d=new BigDecimal(raw); if(!dec && d.scale()<=0) try{return d.longValueExact();}catch(ArithmeticException ignored){} return d.stripTrailingZeros(); }catch(Exception e){fail("numero non valido"); return null;} }
        private void digits() throws IOException { int s=index; while(index<text.length()&&Character.isDigit(text.charAt(index))) index++; if(index==s) fail("cifre attese"); }
        private Object literal(String l,Object v)throws IOException{ if(!text.startsWith(l,index)) fail("letterale non valido"); index+=l.length(); return v; }
        private void skip(){ while(index<text.length()&&Character.isWhitespace(text.charAt(index))) index++; }
        private boolean take(char c){ if(index<text.length()&&text.charAt(index)==c){index++;return true;} return false; }
        private void expect(char c)throws IOException{ if(!take(c)) fail("atteso '"+c+"'"); }
        private boolean peek(char c){ return index<text.length()&&text.charAt(index)==c; }
        private void fail(String m)throws IOException{ throw new IOException("JSON non valido in "+source+" posizione "+index+": "+m); }
    }

    private static final class JsonWriter {
        static String writePretty(Object v){ StringBuilder b=new StringBuilder(); write(v,b,0); return b.toString(); }
        private static void write(Object v,StringBuilder b,int depth){
            if(v==null){b.append("null");return;} if(v instanceof String s){quote(s,b);return;} if(v instanceof Boolean){b.append(v);return;} if(v instanceof Number n){b.append(n instanceof BigDecimal d?d.stripTrailingZeros().toPlainString():n);return;}
            if(v instanceof Map<?,?> m){ b.append('{'); if(!m.isEmpty()){ b.append('\n'); int i=0; for(var e:m.entrySet()){ indent(b,depth+1); quote(String.valueOf(e.getKey()),b); b.append(": "); write(e.getValue(),b,depth+1); if(++i<m.size()) b.append(','); b.append('\n'); } indent(b,depth);} b.append('}'); return; }
            if(v instanceof List<?> l){ b.append('['); if(!l.isEmpty()){ b.append('\n'); for(int i=0;i<l.size();i++){ indent(b,depth+1); write(l.get(i),b,depth+1); if(i+1<l.size()) b.append(','); b.append('\n'); } indent(b,depth);} b.append(']'); return; }
            quote(String.valueOf(v),b);
        }
        private static void indent(StringBuilder b,int d){ b.append("  ".repeat(d)); }
        private static void quote(String s,StringBuilder b){ b.append('"'); for(int i=0;i<s.length();i++){ char c=s.charAt(i); switch(c){case '"'->b.append("\\\"");case '\\'->b.append("\\\\");case '\b'->b.append("\\b");case '\f'->b.append("\\f");case '\n'->b.append("\\n");case '\r'->b.append("\\r");case '\t'->b.append("\\t");default->{if(c<0x20)b.append(String.format("\\u%04x",(int)c));else b.append(c);}}} b.append('"'); }
    }
}
```

### src\main\java\it\alterlega\recordsnext\SeasonRegistry.java

```java
package it.alterlega.recordsnext;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Registro autonomo delle stagioni e delle relative risorse esterne.
 *
 * <p>Opera esclusivamente sul database SQLite gia importato. Non apre e non
 * modifica file FCM/FCA. Lo schema viene installato in modo idempotente solo
 * quando questa classe viene eseguita.</p>
 */
public final class SeasonRegistry {

    private static final Pattern SEASON_PATTERN =
        Pattern.compile("^(\\d{4})_(\\d{4})$");

    private SeasonRegistry() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            printUsage();
            System.exit(2);
        }

        Path database = Path.of(args[0]).toAbsolutePath().normalize();
        if (!Files.isRegularFile(database)) {
            throw new IllegalArgumentException(
                "Database SQLite non trovato: " + database
            );
        }

        String command = args[1].trim().toLowerCase(Locale.ROOT);
        Class.forName("org.sqlite.JDBC");

        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + database)) {

            configureConnection(connection);
            installSchema(connection);

            switch (command) {
                case "show" -> show(connection);
                case "set-managed" -> setManaged(connection, args);
                case "set-manual" -> setManual(connection, args);
                case "set-sites" -> setSites(connection, args);
                case "validate" -> validateCommand(connection, args);
                default -> {
                    printUsage();
                    System.exit(2);
                }
            }
        }
    }

    static void installSchema(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE IF NOT EXISTS rn_season_configuration (
                    season_id TEXT PRIMARY KEY,
                    management_type TEXT NOT NULL
                        CHECK (management_type IN ('GESTITA', 'MANUALE')),
                    local_site_path TEXT,
                    online_site_url TEXT,
                    dataa_path TEXT,
                    configuration_status TEXT NOT NULL DEFAULT 'DA_CONFIGURARE'
                        CHECK (
                            configuration_status IN (
                                'DA_CONFIGURARE',
                                'IN_CORSO',
                                'COMPLETA'
                            )
                        ),
                    created_at TEXT NOT NULL,
                    updated_at TEXT NOT NULL,
                    FOREIGN KEY (season_id)
                        REFERENCES rn_season(season_id)
                )
                """);

            statement.execute("""
                CREATE INDEX IF NOT EXISTS ix_rn_season_configuration_status
                ON rn_season_configuration(configuration_status)
                """);
        }
    }

    private static void show(Connection connection) throws Exception {
        String sql = """
            SELECT
                s.season_id,
                s.is_anchor,
                COALESCE(c.management_type, 'NON_CONFIGURATA') AS tipo,
                COALESCE(c.configuration_status, 'DA_CONFIGURARE') AS stato,
                c.local_site_path,
                c.online_site_url,
                c.dataa_path,
                (SELECT COUNT(*)
                 FROM rn_source_file f
                 WHERE f.season_id = s.season_id
                   AND f.source_type = 'FCM') AS fcm,
                (SELECT COUNT(*)
                 FROM rn_source_file f
                 WHERE f.season_id = s.season_id
                   AND f.source_type = 'FCA') AS fca
            FROM rn_season s
            LEFT JOIN rn_season_configuration c
              ON c.season_id = s.season_id
            ORDER BY CAST(SUBSTR(s.season_id, 1, 4) AS INTEGER) DESC,
                     s.season_id DESC
            """;

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {

            System.out.printf(
                Locale.ROOT,
                "%-11s %-6s %-17s %-15s %3s %3s  %s%n",
                "STAGIONE", "ANCORA", "TIPO", "STATO", "FCM", "FCA",
                "RISORSE"
            );

            while (result.next()) {
                String resources = resourcesSummary(result);
                System.out.printf(
                    Locale.ROOT,
                    "%-11s %-6s %-17s %-15s %3d %3d  %s%n",
                    result.getString("season_id"),
                    result.getInt("is_anchor") == 1 ? "SI" : "NO",
                    result.getString("tipo"),
                    result.getString("stato"),
                    result.getInt("fcm"),
                    result.getInt("fca"),
                    resources
                );
            }
        }
    }

    private static void setManaged(Connection connection, String[] args)
            throws Exception {

        requireArgumentCount(
            args,
            6,
            "<db> set-managed <stagione> <sito-locale|-> "
                + "<sito-online|-> <DataA.js|AUTO|->"
        );

        String seasonId = requireExistingSeason(connection, args[2]);
        SiteValues sites = parseSites(args[3], args[4], args[5]);

        Validation validation = validateManagedSources(connection, seasonId);
        if (!validation.valid()) {
            throw new IllegalStateException(validation.message());
        }

        inTransaction(connection, () -> {
            upsertConfiguration(
                connection,
                seasonId,
                "GESTITA",
                sites,
                calculateStatus(connection, seasonId, "GESTITA")
            );
        });

        System.out.println("Stagione gestita registrata: " + seasonId);
        printSites(sites);
    }

    private static void setManual(Connection connection, String[] args)
            throws Exception {

        requireArgumentCount(
            args,
            6,
            "<db> set-manual <stagione> <sito-locale|-> "
                + "<sito-online|-> <DataA.js|AUTO|->"
        );

        String seasonId = requireValidSeasonId(args[2]);
        SiteValues sites = parseSites(args[3], args[4], args[5]);

        inTransaction(connection, () -> {
            ensureManualSeasonCanBeUsed(connection, seasonId);
            insertSeasonIfMissing(connection, seasonId);
            upsertConfiguration(
                connection,
                seasonId,
                "MANUALE",
                sites,
                "COMPLETA"
            );
        });

        System.out.println("Stagione manuale registrata: " + seasonId);
        printSites(sites);
    }

    private static void setSites(Connection connection, String[] args)
            throws Exception {

        requireArgumentCount(
            args,
            6,
            "<db> set-sites <stagione> <sito-locale|-> "
                + "<sito-online|-> <DataA.js|AUTO|->"
        );

        String seasonId = requireExistingSeason(connection, args[2]);
        SiteValues sites = parseSites(args[3], args[4], args[5]);
        String managementType = requireConfiguredType(connection, seasonId);

        inTransaction(connection, () -> {
            upsertConfiguration(
                connection,
                seasonId,
                managementType,
                sites,
                calculateStatus(connection, seasonId, managementType)
            );
        });

        System.out.println("Risorse stagione aggiornate: " + seasonId);
        printSites(sites);
    }

    private static void validateCommand(Connection connection, String[] args)
            throws Exception {

        requireArgumentCount(args, 3, "<db> validate <stagione>");
        String seasonId = requireExistingSeason(connection, args[2]);
        String managementType = requireConfiguredType(connection, seasonId);

        Validation validation = validateSeason(
            connection,
            seasonId,
            managementType
        );

        if (!validation.valid()) {
            System.out.println(seasonId + "  NON VALIDA");
            System.out.println(validation.message());
            System.exit(1);
        }

        String status = calculateStatus(connection, seasonId, managementType);
        updateStoredStatus(connection, seasonId, status);

        System.out.println(seasonId + "  VALIDA");
        System.out.println("Tipo  : " + managementType);
        System.out.println("Stato : " + status);
    }

    private static Validation validateSeason(
            Connection connection,
            String seasonId,
            String managementType) throws Exception {

        if (managementType.equals("GESTITA")) {
            Validation sources = validateManagedSources(connection, seasonId);
            if (!sources.valid()) {
                return sources;
            }
        } else if (countSources(connection, seasonId) != 0) {
            return Validation.error(
                "La stagione manuale " + seasonId
                    + " possiede sorgenti FCM/FCA importate."
            );
        }

        SiteValues sites = readSites(connection, seasonId);
        try {
            validateStoredSites(sites);
        } catch (IllegalArgumentException exception) {
            return Validation.error(exception.getMessage());
        }

        return Validation.ok();
    }

    private static Validation validateManagedSources(
            Connection connection,
            String seasonId) throws Exception {

        SourceCount fcm = readSourceCount(connection, seasonId, "FCM");
        SourceCount fca = readSourceCount(connection, seasonId, "FCA");

        if (fcm.configured() != 1 || fca.configured() != 1) {
            return Validation.error(
                "La stagione " + seasonId
                    + " deve avere esattamente un FCM e un FCA in "
                    + "rn_source_file; trovati FCM=" + fcm.configured()
                    + ", FCA=" + fca.configured() + "."
            );
        }

        if (fcm.completedImports() != 1 || fca.completedImports() != 1) {
            return Validation.error(
                "Le sorgenti della stagione " + seasonId
                    + " non corrispondono a importazioni COMPLETED univoche; "
                    + "FCM=" + fcm.completedImports()
                    + ", FCA=" + fca.completedImports() + "."
            );
        }

        return Validation.ok();
    }

    private static SourceCount readSourceCount(
            Connection connection,
            String seasonId,
            String sourceType) throws Exception {

        String sql = """
            SELECT
                COUNT(*) AS configured_count,
                SUM(CASE WHEN i.status = 'COMPLETED' THEN 1 ELSE 0 END)
                    AS completed_count
            FROM rn_source_file f
            LEFT JOIN rn_import i
              ON i.import_id = f.import_id
            WHERE f.season_id = ?
              AND f.source_type = ?
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, seasonId);
            statement.setString(2, sourceType);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return new SourceCount(
                    result.getInt("configured_count"),
                    result.getInt("completed_count")
                );
            }
        }
    }

    private static int countSources(
            Connection connection,
            String seasonId) throws Exception {

        try (PreparedStatement statement = connection.prepareStatement("""
            SELECT COUNT(*)
            FROM rn_source_file
            WHERE season_id = ?
            """)) {
            statement.setString(1, seasonId);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1);
            }
        }
    }

    private static String calculateStatus(
            Connection connection,
            String seasonId,
            String managementType) throws Exception {

        if (managementType.equals("MANUALE")) {
            return "COMPLETA";
        }

        Validation sources = validateManagedSources(connection, seasonId);
        if (!sources.valid()) {
            return "DA_CONFIGURARE";
        }

        long pendingMappings = countPendingMappings(connection, seasonId);
        return pendingMappings == 0 ? "COMPLETA" : "IN_CORSO";
    }

    private static long countPendingMappings(
            Connection connection,
            String seasonId) throws Exception {

        String sql = """
            SELECT
                (SELECT COUNT(*)
                 FROM rn_competition_mapping cm
                 JOIN rn_competition_season cs
                   ON cs.competition_season_id = cm.competition_season_id
                 WHERE cs.season_id = ?
                   AND cm.mapping_status = 'DA_CONFIGURARE')
                +
                (SELECT COUNT(*)
                 FROM rn_team_mapping tm
                 JOIN rn_team_season ts
                   ON ts.team_season_id = tm.team_season_id
                 WHERE ts.season_id = ?
                   AND tm.mapping_status = 'DA_CONFIGURARE')
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, seasonId);
            statement.setString(2, seasonId);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getLong(1);
            }
        }
    }

    private static void ensureManualSeasonCanBeUsed(
            Connection connection,
            String seasonId) throws Exception {

        if (countSources(connection, seasonId) != 0) {
            throw new IllegalStateException(
                "La stagione " + seasonId
                    + " possiede gia sorgenti importate e non puo essere "
                    + "registrata come MANUALE."
            );
        }

        try (PreparedStatement statement = connection.prepareStatement("""
            SELECT is_anchor
            FROM rn_season
            WHERE season_id = ?
            """)) {
            statement.setString(1, seasonId);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next() && result.getInt("is_anchor") == 1) {
                    throw new IllegalStateException(
                        "La stagione ancora non puo essere MANUALE: "
                            + seasonId
                    );
                }
            }
        }
    }

    private static void insertSeasonIfMissing(
            Connection connection,
            String seasonId) throws Exception {

        int startYear = startYear(seasonId);
        String now = Instant.now().toString();

        try (PreparedStatement statement = connection.prepareStatement("""
            INSERT INTO rn_season (
                season_id,
                display_name,
                sort_order,
                is_anchor,
                created_at,
                updated_at
            ) VALUES (?, ?, ?, 0, ?, ?)
            ON CONFLICT(season_id) DO NOTHING
            """)) {
            statement.setString(1, seasonId);
            statement.setString(2, seasonId.replace('_', '/'));
            statement.setInt(3, startYear);
            statement.setString(4, now);
            statement.setString(5, now);
            statement.executeUpdate();
        }
    }

    private static void upsertConfiguration(
            Connection connection,
            String seasonId,
            String managementType,
            SiteValues sites,
            String status) throws Exception {

        String now = Instant.now().toString();

        try (PreparedStatement statement = connection.prepareStatement("""
            INSERT INTO rn_season_configuration (
                season_id,
                management_type,
                local_site_path,
                online_site_url,
                dataa_path,
                configuration_status,
                created_at,
                updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(season_id) DO UPDATE SET
                management_type = excluded.management_type,
                local_site_path = excluded.local_site_path,
                online_site_url = excluded.online_site_url,
                dataa_path = excluded.dataa_path,
                configuration_status = excluded.configuration_status,
                updated_at = excluded.updated_at
            """)) {
            statement.setString(1, seasonId);
            statement.setString(2, managementType);
            statement.setString(3, sites.localSite());
            statement.setString(4, sites.onlineSite());
            statement.setString(5, sites.dataA());
            statement.setString(6, status);
            statement.setString(7, now);
            statement.setString(8, now);
            statement.executeUpdate();
        }
    }

    private static void updateStoredStatus(
            Connection connection,
            String seasonId,
            String status) throws Exception {

        try (PreparedStatement statement = connection.prepareStatement("""
            UPDATE rn_season_configuration
            SET configuration_status = ?,
                updated_at = ?
            WHERE season_id = ?
            """)) {
            statement.setString(1, status);
            statement.setString(2, Instant.now().toString());
            statement.setString(3, seasonId);
            statement.executeUpdate();
        }
    }

    private static String requireConfiguredType(
            Connection connection,
            String seasonId) throws Exception {

        try (PreparedStatement statement = connection.prepareStatement("""
            SELECT management_type
            FROM rn_season_configuration
            WHERE season_id = ?
            """)) {
            statement.setString(1, seasonId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalStateException(
                        "Stagione non ancora registrata: " + seasonId
                    );
                }
                return result.getString(1);
            }
        }
    }

    private static SiteValues readSites(
            Connection connection,
            String seasonId) throws Exception {

        try (PreparedStatement statement = connection.prepareStatement("""
            SELECT local_site_path, online_site_url, dataa_path
            FROM rn_season_configuration
            WHERE season_id = ?
            """)) {
            statement.setString(1, seasonId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalStateException(
                        "Stagione non ancora registrata: " + seasonId
                    );
                }
                return new SiteValues(
                    result.getString(1),
                    result.getString(2),
                    result.getString(3)
                );
            }
        }
    }

    private static SiteValues parseSites(
            String localArgument,
            String onlineArgument,
            String dataAArgument) {

        String local = nullable(localArgument);
        String online = nullable(onlineArgument);

        if (local != null) {
            Path localPath = Path.of(local).toAbsolutePath().normalize();
            if (!Files.isDirectory(localPath)) {
                throw new IllegalArgumentException(
                    "Cartella sito locale non trovata: " + localPath
                );
            }
            local = localPath.toString();
        }

        validateOnlineUrl(online);

        String dataA;
        if (dataAArgument.trim().equalsIgnoreCase("AUTO")) {
            if (local == null) {
                throw new IllegalArgumentException(
                    "AUTO richiede il percorso del sito locale."
                );
            }
            Path detected = Path.of(local, "js", "DataA.js")
                .toAbsolutePath().normalize();
            if (!Files.isRegularFile(detected)) {
                throw new IllegalArgumentException(
                    "DataA.js non trovato automaticamente: " + detected
                );
            }
            dataA = detected.toString();
        } else {
            dataA = nullable(dataAArgument);
            if (dataA != null) {
                Path dataAPath = Path.of(dataA).toAbsolutePath().normalize();
                if (!Files.isRegularFile(dataAPath)) {
                    throw new IllegalArgumentException(
                        "File DataA.js non trovato: " + dataAPath
                    );
                }
                dataA = dataAPath.toString();
            }
        }

        return new SiteValues(local, online, dataA);
    }

    private static void validateStoredSites(SiteValues sites) {
        if (sites.localSite() != null
                && !Files.isDirectory(Path.of(sites.localSite()))) {
            throw new IllegalArgumentException(
                "Cartella sito locale non piu disponibile: "
                    + sites.localSite()
            );
        }

        validateOnlineUrl(sites.onlineSite());

        if (sites.dataA() != null
                && !Files.isRegularFile(Path.of(sites.dataA()))) {
            throw new IllegalArgumentException(
                "File DataA.js non piu disponibile: " + sites.dataA()
            );
        }
    }

    private static void validateOnlineUrl(String value) {
        if (value == null) {
            return;
        }

        URI uri;
        try {
            uri = URI.create(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                "URL sito online non valido: " + value,
                exception
            );
        }

        String scheme = uri.getScheme();
        if (scheme == null
                || !(scheme.equalsIgnoreCase("http")
                    || scheme.equalsIgnoreCase("https"))
                || uri.getHost() == null) {
            throw new IllegalArgumentException(
                "URL sito online non valido: " + value
            );
        }
    }

    private static String requireExistingSeason(
            Connection connection,
            String value) throws Exception {

        String seasonId = requireValidSeasonId(value);

        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT COUNT(*) FROM rn_season WHERE season_id = ?")) {
            statement.setString(1, seasonId);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                if (result.getInt(1) != 1) {
                    throw new IllegalArgumentException(
                        "Stagione non trovata: " + seasonId
                    );
                }
            }
        }

        return seasonId;
    }

    private static String requireValidSeasonId(String value) {
        String seasonId = value.trim();
        Matcher matcher = SEASON_PATTERN.matcher(seasonId);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                "Formato stagione non valido: " + seasonId
                    + ". Atteso AAAA_AAAA."
            );
        }

        int start = Integer.parseInt(matcher.group(1));
        int end = Integer.parseInt(matcher.group(2));
        if (end != start + 1) {
            throw new IllegalArgumentException(
                "Stagione non consecutiva: " + seasonId
            );
        }
        return seasonId;
    }

    private static int startYear(String seasonId) {
        return Integer.parseInt(seasonId.substring(0, 4));
    }

    private static String resourcesSummary(ResultSet result) throws Exception {
        StringBuilder value = new StringBuilder();
        appendResource(value, "locale", result.getString("local_site_path"));
        appendResource(value, "online", result.getString("online_site_url"));
        appendResource(value, "DataA", result.getString("dataa_path"));
        return value.length() == 0 ? "-" : value.toString();
    }

    private static void appendResource(
            StringBuilder builder,
            String label,
            String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (builder.length() > 0) {
            builder.append(" | ");
        }
        builder.append(label).append('=').append(value);
    }

    private static void printSites(SiteValues sites) {
        System.out.println(
            "Locale: " + displayNullable(sites.localSite())
        );
        System.out.println(
            "Online: " + displayNullable(sites.onlineSite())
        );
        System.out.println(
            "DataA : " + displayNullable(sites.dataA())
        );
    }

    private static String displayNullable(String value) {
        return value == null ? "-" : value;
    }

    private static String nullable(String value) {
        String trimmed = value.trim();
        return trimmed.isBlank() || trimmed.equals("-") ? null : trimmed;
    }

    private static void requireArgumentCount(
            String[] args,
            int expected,
            String usage) {
        if (args.length != expected) {
            throw new IllegalArgumentException("Uso: " + usage);
        }
    }

    private static void configureConnection(Connection connection)
            throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("PRAGMA busy_timeout = 10000");
        }
    }

    private static void inTransaction(
            Connection connection,
            SqlOperation operation) throws Exception {

        boolean originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            operation.run();
            connection.commit();
        } catch (Exception exception) {
            connection.rollback();
            throw exception;
        } finally {
            connection.setAutoCommit(originalAutoCommit);
        }
    }

    private static void printUsage() {
        System.err.println("Comandi:");
        System.err.println("  <db> show");
        System.err.println(
            "  <db> set-managed <stagione> <sito-locale|-> "
                + "<sito-online|-> <DataA.js|AUTO|->"
        );
        System.err.println(
            "  <db> set-manual <stagione> <sito-locale|-> "
                + "<sito-online|-> <DataA.js|AUTO|->"
        );
        System.err.println(
            "  <db> set-sites <stagione> <sito-locale|-> "
                + "<sito-online|-> <DataA.js|AUTO|->"
        );
        System.err.println("  <db> validate <stagione>");
    }

    @FunctionalInterface
    private interface SqlOperation {
        void run() throws Exception;
    }

    private record SiteValues(
        String localSite,
        String onlineSite,
        String dataA
    ) {
    }

    private record SourceCount(int configured, int completedImports) {
    }

    private record Validation(boolean valid, String message) {
        static Validation ok() {
            return new Validation(true, "OK");
        }

        static Validation error(String message) {
            return new Validation(false, message);
        }
    }
}
```

### src\main\java\it\alterlega\recordsnext\SerieAQueryProbe.java

```java
package it.alterlega.recordsnext;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Locale;

public final class SerieAQueryProbe {

    private SerieAQueryProbe() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Uso: SerieAQueryProbe <recordsnext.db>");
            System.exit(2);
        }

        Path database = Path.of(args[0]).toAbsolutePath().normalize();

        Class.forName("org.sqlite.JDBC");

        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + database)) {

            printCompetition(connection);
            printGironi(connection);
            printCounts(connection);
            printMatches(connection);
        }
    }

    private static void printCompetition(Connection connection)
            throws Exception {

        String sql = """
            SELECT ID, NOME
            FROM raw_2025_2026_fcm_competizione
            WHERE ID = 4
            """;

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {

            if (!result.next()) {
                throw new IllegalStateException(
                    "Competizione Serie A con ID 4 non trovata."
                );
            }

            System.out.println("=== COMPETIZIONE ===");
            System.out.println("ID   : " + result.getInt("ID"));
            System.out.println("Nome : " + result.getString("NOME"));
        }
    }

    private static void printGironi(Connection connection)
            throws Exception {

        String sql = """
            SELECT
                g.ID,
                g.NOME,
                COUNT(i.ID) AS incontri,
                SUM(CASE WHEN i.GIOCATO <> 0 THEN 1 ELSE 0 END) AS giocati,
                SUM(
                    CASE
                        WHEN i.GIOCATO <> 0
                         AND i.IDCASA <> 0
                         AND i.IDFUORI <> 0
                        THEN 1
                        ELSE 0
                    END
                ) AS validi
            FROM raw_2025_2026_fcm_girone g
            LEFT JOIN raw_2025_2026_fcm_incontro i
                ON i.IDGIRONE = g.ID
            WHERE g.IDCOMPETIZIONE = 4
            GROUP BY g.ID, g.NOME
            ORDER BY g.ID
            """;

        System.out.println();
        System.out.println("=== GIRONI SERIE A ===");

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {

            while (result.next()) {
                System.out.printf(
                    Locale.ROOT,
                    "ID=%d nome=%s incontri=%d giocati=%d validi=%d%n",
                    result.getInt("ID"),
                    result.getString("NOME"),
                    result.getLong("incontri"),
                    result.getLong("giocati"),
                    result.getLong("validi")
                );
            }
        }
    }

    private static void printCounts(Connection connection)
            throws Exception {

        String sql = """
            SELECT
                COUNT(*) AS tutti,
                SUM(CASE WHEN i.GIOCATO <> 0 THEN 1 ELSE 0 END) AS giocati,
                SUM(
                    CASE
                        WHEN i.GIOCATO <> 0
                         AND i.IDCASA <> 0
                         AND i.IDFUORI <> 0
                        THEN 1
                        ELSE 0
                    END
                ) AS validi
            FROM raw_2025_2026_fcm_incontro i
            JOIN raw_2025_2026_fcm_girone g
                ON g.ID = i.IDGIRONE
            WHERE g.IDCOMPETIZIONE = 4
            """;

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {

            result.next();

            System.out.println();
            System.out.println("=== CONTEGGI ===");
            System.out.println("Tutti   : " + result.getLong("tutti"));
            System.out.println("Giocati : " + result.getLong("giocati"));
            System.out.println("Validi  : " + result.getLong("validi"));
        }
    }

    private static void printMatches(Connection connection)
            throws Exception {

        String sql = """
            SELECT
                i.ID AS id_incontro,
                g.ID AS id_girone,
                g.NOME AS girone,
                i.GIORNATADIA AS giornata_di_a,
                i.IDGIORNATA AS id_giornata,
                gio.DESC AS descrizione_giornata,
                i.IDCASA AS id_casa,
                casa.NOME AS squadra_casa,
                i.IDFUORI AS id_fuori,
                fuori.NOME AS squadra_fuori,
                i.GOLCASA AS gol_casa,
                i.GOLFUORI AS gol_fuori,
                i.TOTCASA AS punti_casa,
                i.TOTFUORI AS punti_fuori
            FROM raw_2025_2026_fcm_incontro i
            JOIN raw_2025_2026_fcm_girone g
                ON g.ID = i.IDGIRONE
            LEFT JOIN raw_2025_2026_fcm_giornata gio
                ON gio.ID = i.IDGIORNATA
            JOIN raw_2025_2026_fcm_fantasquadra casa
                ON casa.ID = i.IDCASA
            JOIN raw_2025_2026_fcm_fantasquadra fuori
                ON fuori.ID = i.IDFUORI
            WHERE g.IDCOMPETIZIONE = 4
              AND i.GIOCATO <> 0
              AND i.IDCASA <> 0
              AND i.IDFUORI <> 0
            ORDER BY
                i.GIORNATADIA,
                i.ID
            LIMIT 15
            """;

        long started = System.nanoTime();

        System.out.println();
        System.out.println("=== PRIME 15 PARTITE ===");

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {

            while (result.next()) {
                System.out.printf(
                    Locale.ROOT,
                    "%d | %2d | %-25s - %-25s | %d-%d | %.1f-%.1f | %s%n",
                    result.getLong("id_incontro"),
                    result.getInt("giornata_di_a"),
                    result.getString("squadra_casa"),
                    result.getString("squadra_fuori"),
                    result.getInt("gol_casa"),
                    result.getInt("gol_fuori"),
                    result.getDouble("punti_casa"),
                    result.getDouble("punti_fuori"),
                    result.getString("descrizione_giornata")
                );
            }
        }

        long finished = System.nanoTime();

        System.out.printf(
            Locale.ROOT,
            "%nTempo query e lettura campione: %.3f ms%n",
            (finished - started) / 1_000_000.0
        );
    }
}
```

### src\main\java\it\alterlega\recordsnext\SerieARoundProbe.java

```java
package it.alterlega.recordsnext;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public final class SerieARoundProbe {

    private SerieARoundProbe() {
    }

    public static void main(String[] args) throws Exception {
        Path database = Path.of(args[0]).toAbsolutePath().normalize();

        Class.forName("org.sqlite.JDBC");

        String sql = """
            WITH giornate AS (
                SELECT
                    i.IDGIORNATA,
                    i.GIORNATADIA,
                    gio."DESC" AS descrizione,
                    MIN(i.ID) AS primo_incontro,
                    COUNT(*) AS incontri
                FROM raw_2025_2026_fcm_incontro i
                JOIN raw_2025_2026_fcm_girone g
                    ON g.ID = i.IDGIRONE
                LEFT JOIN raw_2025_2026_fcm_giornata gio
                    ON gio.ID = i.IDGIORNATA
                WHERE g.IDCOMPETIZIONE = 4
                  AND i.GIOCATO <> 0
                  AND i.IDCASA <> 0
                  AND i.IDFUORI <> 0
                GROUP BY
                    i.IDGIORNATA,
                    i.GIORNATADIA,
                    gio."DESC"
            )
            SELECT
                ROW_NUMBER() OVER (
                    ORDER BY primo_incontro
                ) AS giornata_competizione,
                IDGIORNATA,
                GIORNATADIA AS giornata_serie_a,
                descrizione,
                incontri,
                primo_incontro
            FROM giornate
            ORDER BY primo_incontro
            """;

        try (Connection connection = DriverManager.getConnection(
                    "jdbc:sqlite:" + database);
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {

            int giornate = 0;
            int incontri = 0;

            while (result.next()) {
                giornate++;
                incontri += result.getInt("incontri");

                System.out.printf(
                    "%2d | IDGIORNATA=%4d | Serie A=%2d | incontri=%d | %s%n",
                    result.getInt("giornata_competizione"),
                    result.getInt("IDGIORNATA"),
                    result.getInt("giornata_serie_a"),
                    result.getInt("incontri"),
                    result.getString("descrizione")
                );
            }

            System.out.println();
            System.out.println("Giornate : " + giornate);
            System.out.println("Incontri : " + incontri);
        }
    }
}
```

### src\main\java\it\alterlega\recordsnext\SqliteAudit.java

```java
package it.alterlega.recordsnext;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public final class SqliteAudit {

    private SqliteAudit() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Uso: SqliteAudit <recordsnext.db>");
            System.exit(2);
        }

        Path database = Path.of(args[0]).toAbsolutePath().normalize();

        Class.forName("org.sqlite.JDBC");

        try (Connection connection =
                 DriverManager.getConnection("jdbc:sqlite:" + database);
             Statement statement = connection.createStatement()) {

            printValue(
                statement,
                "Importazioni completate",
                "SELECT COUNT(*) FROM rn_import WHERE status='COMPLETED'"
            );

            printValue(
                statement,
                "Tabelle catalogate",
                "SELECT COUNT(*) FROM rn_table_catalog"
            );

            printValue(
                statement,
                "Colonne catalogate",
                "SELECT COUNT(*) FROM rn_column_catalog"
            );

            printValue(
                statement,
                "Righe sorgente",
                "SELECT SUM(source_row_count) FROM rn_table_catalog"
            );

            printValue(
                statement,
                "Righe importate",
                "SELECT SUM(imported_row_count) FROM rn_table_catalog"
            );

            printValue(
                statement,
                "Audit falliti",
                "SELECT COUNT(*) FROM rn_table_catalog WHERE audit_ok<>1"
            );

            printValue(
                statement,
                "Tabelle raw reali",
                """
                SELECT COUNT(*)
                FROM sqlite_master
                WHERE type='table'
                  AND name LIKE 'raw_%'
                """
            );

            System.out.println();
            System.out.println("=== IMPORTAZIONI ===");

            try (ResultSet result = statement.executeQuery(
                    """
                    SELECT source_type,
                           table_count,
                           column_count,
                           row_count,
                           status
                    FROM rn_import
                    ORDER BY import_id
                    """)) {

                while (result.next()) {
                    System.out.printf(
                        "%s tabelle=%d colonne=%d righe=%d stato=%s%n",
                        result.getString("source_type"),
                        result.getInt("table_count"),
                        result.getInt("column_count"),
                        result.getLong("row_count"),
                        result.getString("status")
                    );
                }
            }

            System.out.println();
            System.out.println("Audit SQLite completato.");
        }
    }

    private static void printValue(
            Statement statement,
            String label,
            String sql) throws Exception {

        try (ResultSet result = statement.executeQuery(sql)) {
            result.next();
            System.out.printf("%-24s: %d%n", label, result.getLong(1));
        }
    }
}
```

### src\test\java\it\alterlega\recordsnext\RecordsNextApplicationTest.java

```java
package it.alterlega.recordsnext;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RecordsNextApplicationTest {

    @Test
    void usesJava21() {
        assertEquals(21, Runtime.version().feature());
    }
}
```

### tools\Analyze-Records2026RemainingContracts-v3.ps1

```powershell
param(
    [string]$ReferenceJsDir = "D:\DEV_APPS\RecordsNext\reference\records2026-site-2025\js",
    [string]$OutputDir = "D:\DEV_APPS\RecordsNext\reference\records2026-site-2025\reports"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null

function Read-AssignedJson {
    param(
        [string]$Path,
        [string]$PrefixPattern
    )

    $text = Get-Content -LiteralPath $Path -Raw
    $json = [regex]::Replace(
        $text,
        $PrefixPattern,
        "",
        [Text.RegularExpressions.RegexOptions]::Singleline
    )
    $json = [regex]::Replace($json, ";\s*$", "")
    return $json | ConvertFrom-Json
}

function Get-Names {
    param($Object)

    if ($null -eq $Object) {
        return @()
    }

    return @($Object.PSObject.Properties.Name | Sort-Object -Unique)
}


function Has-Property {
    param(
        $Object,
        [string]$Name
    )

    if ($null -eq $Object) {
        return $false
    }

    return $Object.PSObject.Properties.Name -contains $Name
}

function Get-PropertyValue {
    param(
        $Object,
        [string]$Name
    )

    if (Has-Property $Object $Name) {
        return $Object.$Name
    }

    return $null
}

function Write-Report {
    param(
        [string]$Path,
        [System.Collections.Generic.List[string]]$Lines
    )

    $Lines | Set-Content -LiteralPath $Path -Encoding UTF8
}

function Add-Block {
    param(
        [System.Collections.Generic.List[string]]$Lines,
        [string]$Title,
        $Value
    )

    $Lines.Add("=== $Title ===")
    $Lines.Add(($Value | Out-String -Width 260).TrimEnd())
    $Lines.Add("")
}

# RECORD STAGIONALI RU
$ruPath = Join-Path $ReferenceJsDir "records2026.recordstagionali.ru.js"
$ru = Read-AssignedJson `
    -Path $ruPath `
    -PrefixPattern "^\s*window\.RECORDS2026_PREVIEW_RU\s*=\s*"

$ruLines = [System.Collections.Generic.List[string]]::new()

Add-Block $ruLines "RIEPILOGO" ([pscustomobject]@{
    File = $ruPath
    Byte = (Get-Item $ruPath).Length
    ElementiTopLevel = @($ru).Count
})

Add-Block $ruLines "CAMPI TOP LEVEL" ((Get-Names @($ru)[0]) -join [Environment]::NewLine)

$ruRows = foreach ($entry in @($ru)) {
    $data = Get-PropertyValue $entry "data"
    $views = Get-PropertyValue $data "views"
    $catalogo = Get-PropertyValue $data "catalogo"

    [pscustomobject]@{
        Stagione = Get-PropertyValue $entry "stagione"
        CampiData = (Get-Names $data) -join ", "
        Views = if ($null -ne $views) { (Get-Names $views).Count } else { 0 }
        Catalogo = if ($null -ne $catalogo) { (Get-Names $catalogo).Count } else { 0 }
    }
}

Add-Block $ruLines "STAGIONI E STRUTTURA" $ruRows

$firstRu = @($ru)[0]
$firstData = Get-PropertyValue $firstRu "data"
$firstViews = Get-PropertyValue $firstData "views"
$firstCatalogo = Get-PropertyValue $firstData "catalogo"

if ($null -ne $firstViews) {
    $viewRows = foreach ($property in $firstViews.PSObject.Properties) {
        $value = $property.Value

        [pscustomobject]@{
            View = $property.Name
            Tipo = if ($value -is [System.Array]) { "array" } else { $value.GetType().Name }
            Elementi = if ($value -is [System.Array]) { @($value).Count } else { 1 }
            CampiPrimoElemento = if ($value -is [System.Array] -and @($value).Count -gt 0) {
                (Get-Names @($value)[0]) -join ", "
            }
            else {
                (Get-Names $value) -join ", "
            }
        }
    }

    Add-Block $ruLines "VIEWS PRIMA STAGIONE" $viewRows
}

if ($null -ne $firstCatalogo) {
    Add-Block $ruLines "CATALOGO PRIMA STAGIONE" ((Get-Names $firstCatalogo) -join [Environment]::NewLine)
}

$ruReport = Join-Path $OutputDir "records2026-ru-contract.txt"
Write-Report $ruReport $ruLines

# STORICO RU ANNUALE
$annualPath = Join-Path $ReferenceJsDir "records2026.storico.ru.2025_2026.js"
$annualText = Get-Content -LiteralPath $annualPath -Raw

$annualMatch = [regex]::Match(
    $annualText,
    'window\.RECORDS2026_STORICO_RU\s*\[\s*["'']2025_2026["'']\s*\]\s*=\s*(?<json>\{.*\})\s*;?\s*$',
    [Text.RegularExpressions.RegexOptions]::Singleline
)

if (-not $annualMatch.Success) {
    throw "Impossibile estrarre il JSON da $annualPath"
}

$annual = $annualMatch.Groups["json"].Value | ConvertFrom-Json
$annualLines = [System.Collections.Generic.List[string]]::new()

Add-Block $annualLines "RIEPILOGO" ([pscustomobject]@{
    File = $annualPath
    Byte = (Get-Item $annualPath).Length
    CampiTopLevel = (Get-Names $annual) -join ", "
})

if (Has-Property $annual "meta") {
    Add-Block $annualLines "META - CAMPI" ((Get-Names $annual.meta) -join [Environment]::NewLine)
}

foreach ($property in $annual.PSObject.Properties) {
    $value = $property.Value

    $summary = [pscustomobject]@{
        Sezione = $property.Name
        Tipo = if ($value -is [System.Array]) { "array" } elseif ($null -eq $value) { "null" } else { $value.GetType().Name }
        Elementi = if ($value -is [System.Array]) { @($value).Count } else { 1 }
        CampiPrimoElemento = if ($value -is [System.Array] -and @($value).Count -gt 0) {
            (Get-Names @($value)[0]) -join ", "
        }
        else {
            (Get-Names $value) -join ", "
        }
    }

    Add-Block $annualLines ("SEZIONE " + $property.Name) $summary
}

$annualReport = Join-Path $OutputDir "records2026-storico-annual-contract.txt"
Write-Report $annualReport $annualLines

# MANIFEST
$manifestPath = Join-Path $ReferenceJsDir "records2026.storico.ru.manifest.js"
$manifest = Read-AssignedJson `
    -Path $manifestPath `
    -PrefixPattern "^\s*window\.RECORDS2026_STORICO_RU_MANIFEST\s*=\s*"

$manifestLines = [System.Collections.Generic.List[string]]::new()

Add-Block $manifestLines "RIEPILOGO" ([pscustomobject]@{
    File = $manifestPath
    Byte = (Get-Item $manifestPath).Length
    CampiTopLevel = (Get-Names $manifest) -join ", "
    NumeroStagioni = @($manifest.stagioni).Count
})

if (Has-Property $manifest "meta") {
    Add-Block $manifestLines "META" $manifest.meta
}

if (Has-Property $manifest "stagioni") {
    Add-Block $manifestLines "CAMPI STAGIONI" ((Get-Names @($manifest.stagioni)[0]) -join [Environment]::NewLine)
    Add-Block $manifestLines "STAGIONI" $manifest.stagioni
}

$manifestReport = Join-Path $OutputDir "records2026-manifest-contract.txt"
Write-Report $manifestReport $manifestLines

Write-Host "Creati report:"
Get-Item $ruReport, $annualReport, $manifestReport |
    Select-Object FullName, Length |
    Format-Table -AutoSize
```

### tools\Build-RecordsNextRelease.ps1

```powershell
param(
    [string]$Version = "1.0.2"
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

$java = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME "bin\java.exe" } else { "java.exe" }
& $java -version
if ($LASTEXITCODE -ne 0) { throw "Java 21 non disponibile." }

& ".\mvnw.cmd" clean package
if ($LASTEXITCODE -ne 0) { throw "Build Maven fallita." }

$jar = Join-Path $root "target\RecordsNext.jar"
if (-not (Test-Path $jar)) { throw "JAR release non trovato: $jar" }

$ucaSource = "$root\tools\ucanaccess\2.0.9.5\UCanAccess-2.0.9.5-bin"
if (-not (Test-Path $ucaSource)) { throw "Runtime UCanAccess non trovato: $ucaSource" }

$examplesSource = "$root\release\site-examples"
if (-not (Test-Path $examplesSource)) { throw "Esempi sito non trovati: $examplesSource" }

$distRoot = Join-Path $root "dist"
$releaseDir = Join-Path $distRoot "RecordsNext-$Version"
$zipPath = Join-Path $distRoot "RecordsNext-$Version.zip"
$payloadDir = Join-Path $releaseDir "payload"

Remove-Item $releaseDir -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item $zipPath -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Path $payloadDir -Force | Out-Null

# Contenuto visibile nello ZIP prima dell'installazione.
Copy-Item "$root\release\Installa-RecordsNext.bat" $releaseDir
Copy-Item "$root\docs\INSTALLAZIONE.md" (Join-Path $releaseDir "INSTALLAZIONE.md")
Copy-Item $examplesSource (Join-Path $releaseDir "Esempi-sito") -Recurse

# Payload temporaneo: l'installer lo porta nella root e poi lo elimina.
Copy-Item $jar (Join-Path $payloadDir "RecordsNext.jar")
Copy-Item "$root\release\Avvia-RecordsNext.vbs" $payloadDir
$ucaTarget = Join-Path $payloadDir "runtime\ucanaccess"
New-Item -ItemType Directory -Path (Split-Path $ucaTarget -Parent) -Force | Out-Null
Copy-Item $ucaSource $ucaTarget -Recurse

Compress-Archive -Path "$releaseDir\*" -DestinationPath $zipPath -CompressionLevel Optimal
Write-Host "Release creata: $zipPath"
```

### tools\Cleanup-RecordsNextGuiPatches.ps1

```powershell
Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
Set-Location $projectRoot

$removed = 0
Get-ChildItem -LiteralPath $projectRoot -File -Filter "GUI*_INSTALL.txt" -ErrorAction SilentlyContinue | ForEach-Object {
    Remove-Item -LiteralPath $_.FullName -Force
    $removed++
}

Get-ChildItem -LiteralPath (Join-Path $projectRoot "data\reports") -Recurse -File -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -match '\.(stage\d+|final)\.json$' } |
    ForEach-Object {
        Remove-Item -LiteralPath $_.FullName -Force
        $removed++
    }

Write-Host "File temporanei rimossi: $removed"
```

### tools\Compare-NormalizedReserveOffice.ps1

```powershell
param(
    [Parameter(Mandatory = $true)]
    [string]$Season,

    [string]$ReportsRoot = ".\data\reports",

    [string]$ReferenceRoot = "E:\FCM\plugin\Mauz_strom2014Full\Records2026\data_archive\riserveufficio",

    [string]$OutputPath = ""
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Get-PropertyValue {
    param(
        $Object,
        [string]$Name
    )

    if ($null -eq $Object) {
        return $null
    }

    $property = $Object.PSObject.Properties[$Name]
    if ($null -eq $property) {
        return $null
    }

    return $property.Value
}

function Normalize-Text {
    param($Value)

    if ($null -eq $Value) {
        return ""
    }

    return ([string]$Value).Trim()
}

function Normalize-Number {
    param($Value)

    if ($null -eq $Value -or ([string]$Value).Trim() -eq "") {
        return "0"
    }

    $text = ([string]$Value).Trim().Replace(",", ".")
    $number = 0.0
    $ok = [double]::TryParse(
        $text,
        [System.Globalization.NumberStyles]::Any,
        [System.Globalization.CultureInfo]::InvariantCulture,
        [ref]$number
    )

    if (-not $ok) {
        return $text
    }

    return $number.ToString(
        "0.################",
        [System.Globalization.CultureInfo]::InvariantCulture
    )
}

function Get-RowKey {
    param($Row)

    return @(
        Normalize-Text (Get-PropertyValue $Row "idIncontro")
        Normalize-Text (Get-PropertyValue $Row "idSquadra")
        Normalize-Text (Get-PropertyValue $Row "tipoRU")
        Normalize-Text (Get-PropertyValue $Row "ordine")
    ) -join "|"
}

function Convert-ToComparableRow {
    param($Row)

    return [pscustomobject][ordered]@{
        idIncontro       = Normalize-Text (Get-PropertyValue $Row "idIncontro")
        giornataDiA      = Normalize-Text (Get-PropertyValue $Row "giornataDiA")
        idSquadra        = Normalize-Text (Get-PropertyValue $Row "idSquadra")
        squadra          = Normalize-Text (Get-PropertyValue $Row "squadra")
        idAvversaria     = Normalize-Text (Get-PropertyValue $Row "idAvversaria")
        avversaria       = Normalize-Text (Get-PropertyValue $Row "avversaria")
        tipoRU           = Normalize-Text (Get-PropertyValue $Row "tipoRU")
        ruoloRU          = Normalize-Text (Get-PropertyValue $Row "ruoloRU")
        ordine           = Normalize-Text (Get-PropertyValue $Row "ordine")
        votoTabellino    = Normalize-Text (Get-PropertyValue $Row "votoTabellino")
        modifTabellino   = Normalize-Text (Get-PropertyValue $Row "modifTabellino")
        totTabellino     = Normalize-Text (Get-PropertyValue $Row "totTabellino")
        valoreRU         = Normalize-Number (Get-PropertyValue $Row "valoreRU")
    }
}

function Add-ToMap {
    param(
        [hashtable]$Map,
        $Row,
        [string]$Origin
    )

    $key = Get-RowKey $Row

    if ($Map.ContainsKey($key)) {
        throw "Chiave RU duplicata in ${Origin}: $key"
    }

    $Map[$key] = Convert-ToComparableRow $Row
}

$seasonReports = Join-Path $ReportsRoot $Season
if (-not (Test-Path -LiteralPath $seasonReports -PathType Container)) {
    throw "Cartella report stagione non trovata: $seasonReports"
}

$referencePath = Join-Path (Join-Path $ReferenceRoot $Season) "riserveufficio.json"
if (-not (Test-Path -LiteralPath $referencePath -PathType Leaf)) {
    throw "Archivio RU legacy non trovato: $referencePath"
}

if ([string]::IsNullOrWhiteSpace($OutputPath)) {
    $OutputPath = Join-Path $ReportsRoot ("compare-ru-detail-{0}.txt" -f $Season)
}

$generatedMap = @{}
$normalizedFiles = @(
    Get-ChildItem -LiteralPath $seasonReports -File -Filter "season_normalized_*.json" |
        Sort-Object Name
)

foreach ($file in $normalizedFiles) {
    $document = Get-Content -LiteralPath $file.FullName -Raw -Encoding UTF8 |
        ConvertFrom-Json

    $rows = @(Get-PropertyValue $document "riserveUfficioDettaglio")

    foreach ($row in $rows) {
        if ($null -eq $row) {
            continue
        }

        Add-ToMap -Map $generatedMap -Row $row -Origin $file.Name
    }
}

$referenceDocument = Get-Content -LiteralPath $referencePath -Raw -Encoding UTF8 |
    ConvertFrom-Json

$referenceDetail = Get-PropertyValue $referenceDocument "dettaglio"
$referenceRows = @(Get-PropertyValue $referenceDetail "ruDettaglio")
$referenceMap = @{}

foreach ($row in $referenceRows) {
    if ($null -eq $row) {
        continue
    }

    Add-ToMap -Map $referenceMap -Row $row -Origin $referencePath
}

$differences = [System.Collections.Generic.List[object]]::new()
$allKeys = @(
    @($generatedMap.Keys)
    @($referenceMap.Keys)
) | Sort-Object -Unique

$fields = @(
    "idIncontro",
    "giornataDiA",
    "idSquadra",
    "squadra",
    "idAvversaria",
    "avversaria",
    "tipoRU",
    "ruoloRU",
    "ordine",
    "votoTabellino",
    "modifTabellino",
    "totTabellino",
    "valoreRU"
)

foreach ($key in $allKeys) {
    $hasGenerated = $generatedMap.ContainsKey($key)
    $hasReference = $referenceMap.ContainsKey($key)

    if (-not $hasGenerated) {
        $differences.Add([pscustomobject]@{
            Chiave = $key
            Campo = "<record>"
            Generato = "<mancante>"
            Riferimento = ($referenceMap[$key] | ConvertTo-Json -Compress)
        })
        continue
    }

    if (-not $hasReference) {
        $differences.Add([pscustomobject]@{
            Chiave = $key
            Campo = "<record>"
            Generato = ($generatedMap[$key] | ConvertTo-Json -Compress)
            Riferimento = "<mancante>"
        })
        continue
    }

    foreach ($field in $fields) {
        $generatedValue = Normalize-Text (Get-PropertyValue $generatedMap[$key] $field)
        $referenceValue = Normalize-Text (Get-PropertyValue $referenceMap[$key] $field)

        if ($generatedValue -cne $referenceValue) {
            $differences.Add([pscustomobject]@{
                Chiave = $key
                Campo = $field
                Generato = $generatedValue
                Riferimento = $referenceValue
            })
        }
    }
}

$lines = [System.Collections.Generic.List[string]]::new()
$lines.Add("=== RIEPILOGO RU DETTAGLIO ===")
$lines.Add("")
$lines.Add("Stagione             : $Season")
$lines.Add("File normalizzati    : $($normalizedFiles.Count)")
$lines.Add("Righe generate       : $($generatedMap.Count)")
$lines.Add("Righe legacy         : $($referenceMap.Count)")
$lines.Add("Differenze           : $($differences.Count)")
$lines.Add("")
$lines.Add("=== DIFFERENZE ===")
$lines.Add("")

if ($differences.Count -eq 0) {
    $lines.Add("Nessuna differenza nel dettaglio elementare delle riserve d'ufficio.")
}
else {
    $table = $differences |
        Sort-Object Chiave, Campo |
        Format-Table Chiave, Campo, Generato, Riferimento -AutoSize |
        Out-String -Width 280

    $lines.Add($table.TrimEnd())
}

$outputParent = Split-Path -Parent $OutputPath
if (-not [string]::IsNullOrWhiteSpace($outputParent)) {
    New-Item -ItemType Directory -Path $outputParent -Force | Out-Null
}

$lines | Set-Content -LiteralPath $OutputPath -Encoding UTF8

Write-Host "Stagione          : $Season"
Write-Host "File normalizzati : $($normalizedFiles.Count)"
Write-Host "Righe generate    : $($generatedMap.Count)"
Write-Host "Righe legacy      : $($referenceMap.Count)"
Write-Host "Differenze        : $($differences.Count)"
Write-Host "Report            : $OutputPath"

if ($differences.Count -gt 0) {
    exit 1
}
```

### tools\Compare-Records2026Classic-v1.ps1

```powershell
param(
    [Parameter(Mandatory = $true)]
    [string]$GeneratedPath,

    [Parameter(Mandatory = $true)]
    [string]$ReferencePath,

    [Parameter(Mandatory = $true)]
    [string]$OutputPath
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Read-ClassicArray {
    param([Parameter(Mandatory = $true)][string]$Path)

    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) {
        throw "File non trovato: $Path"
    }

    $text = Get-Content -LiteralPath $Path -Raw -Encoding UTF8
    $json = $text -replace '^\s*window\.RECORDS2026_PREVIEW_CLASSIC\s*=\s*', ''
    $json = $json -replace ';\s*$', ''
    return @($json | ConvertFrom-Json)
}

function Get-FieldStats {
    param([Parameter(Mandatory = $true)][object[]]$Entries)

    $stats = @{}

    foreach ($entry in $Entries) {
        if ($null -eq $entry.data -or $null -eq $entry.data.records) { continue }

        foreach ($sectionProperty in @($entry.data.records.PSObject.Properties)) {
            $section = $sectionProperty.Name
            $rows = @($sectionProperty.Value)

            if (-not $stats.ContainsKey($section)) {
                $stats[$section] = @{}
            }

            foreach ($row in $rows) {
                if ($null -eq $row) { continue }
                foreach ($fieldProperty in @($row.PSObject.Properties)) {
                    $field = $fieldProperty.Name
                    if (-not $stats[$section].ContainsKey($field)) {
                        $stats[$section][$field] = 0
                    }
                    $stats[$section][$field]++
                }
            }
        }
    }

    return $stats
}

$generated = Read-ClassicArray -Path $GeneratedPath
$reference = Read-ClassicArray -Path $ReferencePath

$generatedStats = Get-FieldStats -Entries $generated
$referenceStats = Get-FieldStats -Entries $reference

$sections = @($generatedStats.Keys + $referenceStats.Keys | Sort-Object -Unique)
$rows = foreach ($section in $sections) {
    $generatedFields = if ($generatedStats.ContainsKey($section)) { @($generatedStats[$section].Keys) } else { @() }
    $referenceFields = if ($referenceStats.ContainsKey($section)) { @($referenceStats[$section].Keys) } else { @() }
    $fields = @($generatedFields + $referenceFields | Sort-Object -Unique)

    foreach ($field in $fields) {
        $generatedCount = if ($generatedStats.ContainsKey($section) -and $generatedStats[$section].ContainsKey($field)) {
            [int]$generatedStats[$section][$field]
        } else { 0 }

        $referenceCount = if ($referenceStats.ContainsKey($section) -and $referenceStats[$section].ContainsKey($field)) {
            [int]$referenceStats[$section][$field]
        } else { 0 }

        if ($generatedCount -ne $referenceCount) {
            [pscustomobject]@{
                Sezione = $section
                Campo = $field
                Generato = $generatedCount
                Riferimento = $referenceCount
                Differenza = $referenceCount - $generatedCount
            }
        }
    }
}

$entrySummary = [pscustomobject]@{
    RecordsetGenerati = @($generated).Count
    RecordsetRiferimento = @($reference).Count
    ByteGenerati = (Get-Item -LiteralPath $GeneratedPath).Length
    ByteRiferimento = (Get-Item -LiteralPath $ReferencePath).Length
}

$report = @()
$report += '=== RIEPILOGO ==='
$report += ($entrySummary | Format-List | Out-String)
$report += '=== CAMPI CON CONTEGGI DIVERSI ==='
if (@($rows).Count -eq 0) {
    $report += 'Nessuna differenza nei conteggi dei campi.'
} else {
    $report += ($rows | Sort-Object Sezione, Campo | Format-Table -AutoSize | Out-String -Width 220)
}

$parent = Split-Path -Parent $OutputPath
if ($parent) { New-Item -ItemType Directory -Path $parent -Force | Out-Null }
$report | Set-Content -LiteralPath $OutputPath -Encoding UTF8

Write-Host "Report: $OutputPath"
Write-Host "Differenze campo: $(@($rows).Count)"
```

### tools\Compare-RiserveUfficioArchive.ps1

```powershell
param(
    [Parameter(Mandatory = $true)]
    [string]$Season,

    [string]$GeneratedRoot = ".\data\records-archive\riserveufficio",

    [string]$ReferenceRoot = "E:\FCM\plugin\Mauz_strom2014Full\Records2026\data_archive\riserveufficio",

    [string]$OutputPath = ""
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($OutputPath)) {
    $OutputPath = ".\data\records-archive\compare-ru-$Season.txt"
}

$generatedPath = Join-Path (Join-Path $GeneratedRoot $Season) "riserveufficio.json"
$referencePath = Join-Path (Join-Path $ReferenceRoot $Season) "riserveufficio.json"

if (-not (Test-Path -LiteralPath $generatedPath -PathType Leaf)) {
    throw "File generato non trovato: $generatedPath"
}

if (-not (Test-Path -LiteralPath $referencePath -PathType Leaf)) {
    throw "File legacy non trovato: $referencePath"
}

function Has-Property {
    param($Object, [string]$Name)

    return $null -ne $Object -and
        ($Object.PSObject.Properties.Name -contains $Name)
}

function Get-Value {
    param($Object, [string]$Name)

    if (Has-Property $Object $Name) {
        return $Object.$Name
    }

    return $null
}

function Normalize-Object {
    param($Value)

    if ($null -eq $Value) {
        return $null
    }

    if ($Value -is [pscustomobject]) {
        $properties = @($Value.PSObject.Properties)

        if ($properties.Count -eq 0) {
            return @()
        }

        $ordered = [ordered]@{}

        foreach ($property in $properties | Sort-Object Name) {
            if ($property.Name -in @("generato", "builder", "urlTabellino")) {
                continue
            }

            $ordered[$property.Name] = Normalize-Object $property.Value
        }

        return [pscustomobject]$ordered
    }

    if ($Value -is [System.Collections.IDictionary]) {
        $ordered = [ordered]@{}

        foreach ($key in @($Value.Keys | Sort-Object)) {
            if ([string]$key -in @("generato", "builder", "urlTabellino")) {
                continue
            }

            $ordered[$key] = Normalize-Object $Value[$key]
        }

        return [pscustomobject]$ordered
    }

    if ($Value -is [System.Collections.IEnumerable] -and
        $Value -isnot [string]) {

        $items = @(
            foreach ($item in $Value) {
                Normalize-Object $item
            }
        )

        return @(
            $items |
                Sort-Object {
                    $_ | ConvertTo-Json -Depth 100 -Compress
                }
        )
    }

    return $Value
}

function Canonical-Json {
    param($Value)

    if ($null -eq $Value) {
        return "null"
    }

    $normalized = Normalize-Object $Value
    $json = $normalized | ConvertTo-Json -Depth 100 -Compress

    if ($null -eq $json) {
        return "null"
    }

    return [string]$json
}

function Element-Count {
    param($Value)

    if ($null -eq $Value) {
        return 0
    }

    if ($Value -is [System.Array]) {
        return @($Value).Count
    }

    if ($Value -is [pscustomobject] -and
        @($Value.PSObject.Properties).Count -eq 0) {
        return 0
    }

    return 1
}

$generated = Get-Content -LiteralPath $generatedPath -Raw |
    ConvertFrom-Json

$reference = Get-Content -LiteralPath $referencePath -Raw |
    ConvertFrom-Json

$sections = @(
    @{ Area = "views"; Name = "partiteConPiuRU" },
    @{ Area = "views"; Name = "partiteConRU" },
    @{ Area = "views"; Name = "partiteControRU" },
    @{ Area = "views"; Name = "ruDecisiva" },
    @{ Area = "views"; Name = "bilancioRUDecisiva" },
    @{ Area = "views"; Name = "ruDecisivaContro" },
    @{ Area = "views"; Name = "bilancioRUDecisivaContro" },
    @{ Area = "views"; Name = "bilancioConRU" },
    @{ Area = "views"; Name = "bilancioControRU" },
    @{ Area = "views"; Name = "mediaPuntiConRU" },
    @{ Area = "views"; Name = "mediaPuntiControRU" },
    @{ Area = "views"; Name = "tipoRUUsata" },
    @{ Area = "dettaglio"; Name = "ruDettaglio" },
    @{ Area = "dettaglio"; Name = "ruTeamMatch" }
)

$rows = [System.Collections.Generic.List[object]]::new()

foreach ($section in $sections) {
    $generatedArea = Get-Value $generated $section.Area
    $referenceArea = Get-Value $reference $section.Area

    $generatedValue = Get-Value $generatedArea $section.Name
    $referenceValue = Get-Value $referenceArea $section.Name

    $generatedJson = Canonical-Json $generatedValue
    $referenceJson = Canonical-Json $referenceValue

    $rows.Add([pscustomobject]@{
        Area = $section.Area
        Sezione = $section.Name
        Uguale = $generatedJson -ceq $referenceJson
        ElementiGenerati = Element-Count $generatedValue
        ElementiRiferimento = Element-Count $referenceValue
        CaratteriGenerati = $generatedJson.Length
        CaratteriRiferimento = $referenceJson.Length
    })
}

$generatedCompetitions = Canonical-Json (Get-Value $generated "competizioni")
$referenceCompetitions = Canonical-Json (Get-Value $reference "competizioni")

$rows.Add([pscustomobject]@{
    Area = "root"
    Sezione = "competizioni"
    Uguale = $generatedCompetitions -ceq $referenceCompetitions
    ElementiGenerati = Element-Count (Get-Value $generated "competizioni")
    ElementiRiferimento = Element-Count (Get-Value $reference "competizioni")
    CaratteriGenerati = $generatedCompetitions.Length
    CaratteriRiferimento = $referenceCompetitions.Length
})

$equal = @($rows | Where-Object Uguale).Count
$differences = @($rows | Where-Object { -not $_.Uguale })

$lines = [System.Collections.Generic.List[string]]::new()
$lines.Add("=== RIEPILOGO ARCHIVIO RU ===")
$lines.Add("")
$lines.Add("Stagione            : $Season")
$lines.Add("Confronti totali    : $($rows.Count)")
$lines.Add("Sezioni uguali      : $equal")
$lines.Add("Sezioni differenti  : $($differences.Count)")
$lines.Add("")
$lines.Add("=== DIFFERENZE ===")
$lines.Add("")

if ($differences.Count -eq 0) {
    $lines.Add("Nessuna differenza semantica nell'archivio RU.")
}
else {
    $table = $differences |
        Sort-Object Area, Sezione |
        Format-Table `
            Area,
            Sezione,
            ElementiGenerati,
            ElementiRiferimento,
            CaratteriGenerati,
            CaratteriRiferimento `
            -AutoSize |
        Out-String -Width 240

    $lines.Add($table.TrimEnd())
}

$parent = Split-Path -Parent $OutputPath

if ($parent) {
    New-Item -ItemType Directory -Path $parent -Force | Out-Null
}

$lines | Set-Content -LiteralPath $OutputPath -Encoding UTF8

Write-Host "Stagione           : $Season"
Write-Host "Confronti totali   : $($rows.Count)"
Write-Host "Sezioni uguali     : $equal"
Write-Host "Sezioni differenti : $($differences.Count)"
Write-Host "Report             : $OutputPath"
```

### tools\Compare-SeasonRecordsArchive.ps1

```powershell
param(
    [string]$GeneratedDir = ".\data\records-archive\stagioni\2025_2026",
    [string]$ReferenceDir = "E:\FCM\plugin\Mauz_strom2014Full\Records2026\data_archive\stagioni\2025_2026",
    [string]$OutputPath = ".\data\records-archive\compare-semantic.txt"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$sections = @(
    "puntiSquadraMax",
    "serieSenzaSconfitte",
    "espulsioniSquadre",
    "espulsioniGiocatori",
    "ammonizioniSquadre",
    "assistSquadre",
    "autogolSquadre",
    "rigoriSbagliatiSquadre",
    "rigoriParatiSquadre",
    "golRigoreSquadre",
    "modDifesaMax",
    "modDifesaTotaleSquadre",
    "capitanoVolteSquadre",
    "capitanoTotaleSquadre",
    "cleanSheetPortiereVolteSquadre",
    "cleanSheetPortiereTotaleSquadre",
    "cleanSheetPortiereSerieSquadre",
    "capitanoSerieSquadre"
)

function Has-Property {
    param($Object, [string]$Name)
    return $null -ne $Object -and ($Object.PSObject.Properties.Name -contains $Name)
}

function Get-Value {
    param($Object, [string]$Name)
    if (Has-Property $Object $Name) { return $Object.$Name }
    return $null
}

function Normalize-Generic {
    param($Value)

    if ($null -eq $Value) { return $null }

    if ($Value -is [pscustomobject]) {
        $properties = @($Value.PSObject.Properties)
        if ($properties.Count -eq 0) { return @() }

        $ordered = [ordered]@{}
        foreach ($property in $properties | Sort-Object Name) {
            if ($property.Name -eq "fonteGolRegolamentari") { continue }
            $ordered[$property.Name] = Normalize-Generic $property.Value
        }
        return [pscustomobject]$ordered
    }

    if ($Value -is [System.Collections.IEnumerable] -and $Value -isnot [string]) {
        $items = @(
            foreach ($item in $Value) { Normalize-Generic $item }
        )
        return @($items | Sort-Object { $_ | ConvertTo-Json -Depth 100 -Compress })
    }

    return $Value
}

function Project-Section {
    param(
        [string]$Section,
        $Value
    )

    $items = @(
        @($Value) | Where-Object {
            if ($null -eq $_) {
                return $false
            }

            if ($_ -is [pscustomobject] -and
                @($_.PSObject.Properties).Count -eq 0) {
                return $false
            }

            return $true
        }
    )

    switch ($Section) {
        "modDifesaMax" {
            return @(
                $items |
                    ForEach-Object {
                        [pscustomobject][ordered]@{
                            valore = (Get-Value $_ "valore")
                            idSquadra = [string](Get-Value $_ "idSquadra")
                            squadra = (Get-Value $_ "squadra")
                        }
                    } |
                    Sort-Object `
                        @{Expression="valore";Descending=$true},
                        @{Expression="squadra";Descending=$false},
                        @{Expression="idSquadra";Descending=$false}
            )
        }

        "serieSenzaSconfitte" {
            return @(
                $items |
                    ForEach-Object {
                        [pscustomobject][ordered]@{
                            valore = (Get-Value $_ "valore")
                            vittorie = (Get-Value $_ "vittorie")
                            pareggi = (Get-Value $_ "pareggi")
                            idSquadra = [string](Get-Value $_ "idSquadra")
                            squadra = (Get-Value $_ "squadra")
                        }
                    } |
                    Sort-Object `
                        @{Expression="valore";Descending=$true},
                        @{Expression="vittorie";Descending=$true},
                        @{Expression="squadra";Descending=$false},
                        @{Expression="idSquadra";Descending=$false}
            )
        }

        "puntiSquadraMax" {
            return @(
                $items |
                    ForEach-Object {
                        [pscustomobject][ordered]@{
                            recordId = (Get-Value $_ "recordId")
                            valore = (Get-Value $_ "valore")
                            squadra = (Get-Value $_ "squadra")
                            idSquadra = [string](Get-Value $_ "idSquadra")
                            idIncontro = [string](Get-Value $_ "idIncontro")
                            giornata = (Get-Value $_ "giornata")
                            giornataDiA = (Get-Value $_ "giornataDiA")
                            risultato = (Get-Value $_ "risultato")
                            punteggio = (Get-Value $_ "punteggio")
                            parzialeFatto = (Get-Value (Get-Value $_ "dettagli") "parzialeFatto")
                            parzialeSubito = (Get-Value (Get-Value $_ "dettagli") "parzialeSubito")
                            puntiSubiti = (Get-Value (Get-Value $_ "dettagli") "puntiSubiti")
                        }
                    } |
                    Sort-Object `
                        @{Expression="valore";Descending=$true},
                        @{Expression="squadra";Descending=$false},
                        @{Expression="idIncontro";Descending=$false}
            )
        }

        default {
            return Normalize-Generic $Value
        }
    }
}

function Canonical-Json {
    param($Value)
    if ($null -eq $Value) { return "null" }
    $json = $Value | ConvertTo-Json -Depth 100 -Compress
    if ($null -eq $json) { return "null" }
    return [string]$json
}

$generatedRoot = (Resolve-Path -LiteralPath $GeneratedDir).Path
$referenceRoot = (Resolve-Path -LiteralPath $ReferenceDir).Path
$rows = [System.Collections.Generic.List[object]]::new()

$files = Get-ChildItem -LiteralPath $generatedRoot -File -Filter "season_records_*.json" |
    Sort-Object Name

foreach ($file in $files) {
    $referencePath = Join-Path $referenceRoot $file.Name
    if (-not (Test-Path -LiteralPath $referencePath -PathType Leaf)) {
        throw "File legacy mancante: $referencePath"
    }

    $generated = Get-Content -LiteralPath $file.FullName -Raw | ConvertFrom-Json
    $reference = Get-Content -LiteralPath $referencePath -Raw | ConvertFrom-Json

    foreach ($section in $sections) {
        $g = Project-Section $section (Get-Value $generated.records $section)
        $r = Project-Section $section (Get-Value $reference.records $section)

        $gj = Canonical-Json $g
        $rj = Canonical-Json $r

        $rows.Add([pscustomobject]@{
            File = $file.Name
            Sezione = $section
            Uguale = $gj -ceq $rj
            CaratteriGenerati = $gj.Length
            CaratteriRiferimento = $rj.Length
        })
    }
}

$differences = @($rows | Where-Object { -not $_.Uguale })
$equal = @($rows | Where-Object Uguale).Count

$lines = [System.Collections.Generic.List[string]]::new()
$lines.Add("=== RIEPILOGO SEMANTICO ===")
$lines.Add("")
$lines.Add("Confronti totali   : $($rows.Count)")
$lines.Add("Sezioni uguali     : $equal")
$lines.Add("Sezioni differenti : $($differences.Count)")
$lines.Add("")
$lines.Add("=== DIFFERENZE ===")
$lines.Add("")

if ($differences.Count -eq 0) {
    $lines.Add("Nessuna differenza semantica nelle 15 sezioni disponibili.")
}
else {
    $table = $differences |
        Sort-Object File, Sezione |
        Format-Table File, Sezione, CaratteriGenerati, CaratteriRiferimento -AutoSize |
        Out-String -Width 240
    $lines.Add($table.TrimEnd())
}

$parent = Split-Path -Parent $OutputPath
if ($parent) { New-Item -ItemType Directory -Path $parent -Force | Out-Null }
$lines | Set-Content -LiteralPath $OutputPath -Encoding UTF8

Write-Host "Confronti totali   : $($rows.Count)"
Write-Host "Sezioni uguali     : $equal"
Write-Host "Sezioni differenti : $($differences.Count)"
Write-Host "Report              : $OutputPath"
```

### tools\Create-RecordsNextWorkingCodeMd.ps1

```powershell
[CmdletBinding()]
param(
    [string]$ProjectRoot = (Split-Path -Parent $PSScriptRoot),
    [string]$OutputPath
)

$ErrorActionPreference = "Stop"

$ProjectRoot = [System.IO.Path]::GetFullPath($ProjectRoot)

if ([string]::IsNullOrWhiteSpace($OutputPath)) {
    $OutputPath = Join-Path $ProjectRoot "docs\CODICE_FUNZIONANTE_RECORDSNEXT.md"
}
else {
    $OutputPath = [System.IO.Path]::GetFullPath($OutputPath)
}

function Get-RelativeProjectPath {
    param(
        [Parameter(Mandatory)]
        [string]$FullPath
    )

    $root = $ProjectRoot.TrimEnd("\") + "\"

    if (!$FullPath.StartsWith(
        $root,
        [System.StringComparison]::OrdinalIgnoreCase
    )) {
        throw "Il file non appartiene al progetto: $FullPath"
    }

    return $FullPath.Substring($root.Length)
}

function Test-IncludedFile {
    param(
        [Parameter(Mandatory)]
        [System.IO.FileInfo]$File
    )

    $relative = (Get-RelativeProjectPath $File.FullName).Replace("\", "/")

    $excludedPrefixes = @(
        ".git/",
        "target/",
        "dist/",
        "tools/apache-maven-",
        "data/raw/",
        "data/database/",
        "data/reports/",
        "data/consolidation/",
        "benchmark/results/",
        "output/"
    )

    foreach ($prefix in $excludedPrefixes) {
        if ($relative.StartsWith(
            $prefix,
            [System.StringComparison]::OrdinalIgnoreCase
        )) {
            return $false
        }
    }

    if ($relative -ieq "docs/CODICE_FUNZIONANTE_RECORDSNEXT.md") {
        return $false
    }

    $includedNames = @(
        ".gitignore",
        "README.md",
        "pom.xml",
        "mvnw.cmd"
    )

    if ($includedNames -contains $relative) {
        return $true
    }

    $releaseExtensions = @(
        ".vbs",
        ".bat",
        ".cmd",
        ".html",
        ".css",
        ".js",
        ".txt"
    )

    if (
        $relative.StartsWith(
            "release/",
            [System.StringComparison]::OrdinalIgnoreCase
        ) -and
        $releaseExtensions -contains $File.Extension.ToLowerInvariant()
    ) {
        return $true
    }

    $includedExtensions = @(
        ".java",
        ".ps1",
        ".psm1",
        ".xml",
        ".json",
        ".md",
        ".sql",
        ".yml",
        ".yaml",
        ".properties"
    )

    return $includedExtensions -contains $File.Extension.ToLowerInvariant()
}

function Get-ProjectFiles {
    return @(
        Get-ChildItem `
            -LiteralPath $ProjectRoot `
            -Recurse `
            -File `
            -Force |
        Where-Object {
            Test-IncludedFile -File $_
        } |
        Sort-Object FullName
    )
}

function Get-CodeLanguage {
    param(
        [Parameter(Mandatory)]
        [string]$Path
    )

    switch ([System.IO.Path]::GetExtension($Path).ToLowerInvariant()) {
        ".java"       { return "java" }
        ".ps1"        { return "powershell" }
        ".psm1"       { return "powershell" }
        ".xml"        { return "xml" }
        ".json"       { return "json" }
        ".md"         { return "markdown" }
        ".sql"        { return "sql" }
        ".yml"        { return "yaml" }
        ".yaml"       { return "yaml" }
        ".cmd"        { return "batch" }
        ".properties" { return "properties" }
        default       { return "text" }
    }
}

function Invoke-GitText {
    param(
        [Parameter(Mandatory)]
        [string[]]$Arguments
    )

    try {
        $result = & git -C $ProjectRoot @Arguments 2>$null

        if ($LASTEXITCODE -eq 0) {
            return (($result | ForEach-Object { [string]$_ }) -join "`n").Trim()
        }
    }
    catch {
    }

    return ""
}

$files = Get-ProjectFiles

$branch = Invoke-GitText @("branch", "--show-current")
$commit = Invoke-GitText @("rev-parse", "HEAD")
$status = Invoke-GitText @("status", "--short")

if ([string]::IsNullOrWhiteSpace($branch)) {
    $branch = "(non disponibile)"
}

if ([string]::IsNullOrWhiteSpace($commit)) {
    $commit = "(nessun commit)"
}

if ([string]::IsNullOrWhiteSpace($status)) {
    $status = "(working tree pulita)"
}

$javaVersion = "(non rilevata)"
$javaReleasePath = Join-Path $env:JAVA_HOME "release"

if (Test-Path -LiteralPath $javaReleasePath) {
    $javaReleaseLine = Get-Content -LiteralPath $javaReleasePath |
        Where-Object { $_ -match '^JAVA_VERSION=' } |
        Select-Object -First 1

    if ($javaReleaseLine -match '^JAVA_VERSION="([^"]+)"') {
        $javaVersion = $matches[1]
    }
}

$mavenVersion = "(non rilevata)"
$wrapperPropertiesPath = Join-Path `
    $ProjectRoot `
    ".mvn\wrapper\maven-wrapper.properties"

if (Test-Path -LiteralPath $wrapperPropertiesPath) {
    $distributionLine = Get-Content -LiteralPath $wrapperPropertiesPath |
        Where-Object { $_ -match '^distributionUrl=' } |
        Select-Object -First 1

    if ($distributionLine -match 'apache-maven-([0-9.]+)-bin') {
        $mavenVersion = $matches[1]
    }
}

$generatedAt = Get-Date -Format "yyyy-MM-dd HH:mm:ss zzz"
$fence = [string]([char]96) * 3

$lines = [System.Collections.Generic.List[string]]::new()

$lines.Add("# Codice funzionante RecordsNext")
$lines.Add("")
$lines.Add("Documento generato automaticamente.")
$lines.Add("")
$lines.Add("Non modificare manualmente questo file.")
$lines.Add("Rigenerarlo dopo ogni modifica verificata.")
$lines.Add("")

$lines.Add("## Stato")
$lines.Add("")
$lines.Add("- Generato: $generatedAt")
$lines.Add("- Project root: $ProjectRoot")
$lines.Add("- Branch: $branch")
$lines.Add("- Commit: $commit")
$lines.Add("- Java: $javaVersion")
$lines.Add("- Maven: $mavenVersion")
$lines.Add("")

$lines.Add("### Stato Git")
$lines.Add("")
$lines.Add($fence + "text")
$lines.Add($status)
$lines.Add($fence)
$lines.Add("")

$lines.Add("## Comandi verificati")
$lines.Add("")
$lines.Add("### Compilazione e test")
$lines.Add("")
$lines.Add($fence + "powershell")
$lines.Add('Set-Location "D:\DEV_APPS\RecordsNext"')
$lines.Add('.\mvnw.cmd clean test')
$lines.Add($fence)
$lines.Add("")

$lines.Add("### Generazione della Bibbia")
$lines.Add("")
$lines.Add($fence + "powershell")
$lines.Add('.\tools\Create-RecordsNextWorkingCodeMd.ps1')
$lines.Add($fence)
$lines.Add("")

$lines.Add("## Struttura documentata")
$lines.Add("")
$lines.Add($fence + "text")

foreach ($file in $files) {
    $lines.Add((Get-RelativeProjectPath $file.FullName))
}

$lines.Add($fence)
$lines.Add("")

$lines.Add("## File")
$lines.Add("")

foreach ($file in $files) {
    $relative = Get-RelativeProjectPath $file.FullName
    $language = Get-CodeLanguage $file.FullName
    $content = [System.IO.File]::ReadAllText($file.FullName)

    $lines.Add("### $relative")
    $lines.Add("")
    $lines.Add($fence + $language)
    $lines.Add($content.TrimEnd())
    $lines.Add($fence)
    $lines.Add("")
}

$outputDirectory = Split-Path -Parent $OutputPath

if (!(Test-Path -LiteralPath $outputDirectory)) {
    New-Item `
        -ItemType Directory `
        -Force `
        -Path $outputDirectory |
        Out-Null
}

$document = $lines -join [Environment]::NewLine

[System.IO.File]::WriteAllText(
    $OutputPath,
    $document,
    [System.Text.UTF8Encoding]::new($false)
)

Write-Host ""
Write-Host "Bibbia RecordsNext generata:"
Write-Host $OutputPath
Write-Host "File documentati: $($files.Count)"
```

### tools\Initialize-RecordsNextClassicArchive.ps1

```powershell
param(
    [string]$SourceArchive = "E:\FCM\plugin\Mauz_strom2014Full\Records2026\data_archive\stagioni",
    [string]$TargetArchive = ".\data\records-archive\stagioni",
    [string[]]$ManagedSeasons = @("2024_2025", "2025_2026"),
    [switch]$ReplaceHistorical
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
Set-Location $projectRoot

function Resolve-ProjectPath {
    param([string]$Value)

    if ([IO.Path]::IsPathRooted($Value)) {
        return [IO.Path]::GetFullPath($Value)
    }

    return [IO.Path]::GetFullPath((Join-Path $projectRoot $Value))
}

$source = Resolve-ProjectPath $SourceArchive
$target = Resolve-ProjectPath $TargetArchive

if (-not (Test-Path -LiteralPath $source -PathType Container)) {
    throw "Archivio storico sorgente non trovato: $source"
}

New-Item -ItemType Directory -Path $target -Force | Out-Null

$managed = @{}
foreach ($season in $ManagedSeasons) {
    if (-not [string]::IsNullOrWhiteSpace($season)) {
        $managed[$season.Trim()] = $true
    }
}

$copied = 0
$preserved = 0
$skippedManaged = 0

$sourceSeasons = Get-ChildItem -LiteralPath $source -Directory |
    Where-Object { $_.Name -match '^\d{4}_\d{4}$' } |
    Sort-Object Name

foreach ($seasonDir in $sourceSeasons) {
    if ($managed.ContainsKey($seasonDir.Name)) {
        $skippedManaged++
        continue
    }

    $destination = Join-Path $target $seasonDir.Name

    if ((Test-Path -LiteralPath $destination -PathType Container) -and -not $ReplaceHistorical) {
        $preserved++
        continue
    }

    if (Test-Path -LiteralPath $destination) {
        Remove-Item -LiteralPath $destination -Recurse -Force
    }

    Copy-Item -LiteralPath $seasonDir.FullName -Destination $destination -Recurse -Force
    $copied++
}

Write-Host "Archivio sorgente : $source"
Write-Host "Archivio interno  : $target"
Write-Host "Storiche copiate  : $copied"
Write-Host "Storiche presenti : $preserved"
Write-Host "Gestite escluse   : $skippedManaged"
Write-Host ""
Write-Host "Le stagioni gestite da RecordsNext non sono state copiate né sovrascritte."
```

### tools\Initialize-RecordsNextRuArchive.ps1

```powershell
param(
    [string]$SourceArchive = "E:\FCM\plugin\Mauz_strom2014Full\Records2026\data_archive\riserveufficio",
    [string]$DestinationArchive = ".\data\records-archive\riserveufficio",
    [string[]]$ManagedSeasons = @("2024_2025", "2025_2026"),
    [switch]$ReplaceHistorical
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
Set-Location $projectRoot

function Resolve-ProjectPath {
    param([string]$Value)

    if ([IO.Path]::IsPathRooted($Value)) {
        return [IO.Path]::GetFullPath($Value)
    }

    return [IO.Path]::GetFullPath((Join-Path $projectRoot $Value))
}

$source = Resolve-ProjectPath $SourceArchive
$destination = Resolve-ProjectPath $DestinationArchive

if (-not (Test-Path -LiteralPath $source -PathType Container)) {
    throw "Archivio RU legacy non trovato: $source"
}

New-Item -ItemType Directory -Path $destination -Force | Out-Null

$copied = 0
$skippedManaged = 0
$alreadyPresent = 0

$seasonDirectories = @(
    Get-ChildItem -LiteralPath $source -Directory |
        Where-Object { $_.Name -match '^\d{4}_\d{4}$' } |
        Sort-Object Name
)

foreach ($seasonDirectory in $seasonDirectories) {
    $season = $seasonDirectory.Name

    if ($ManagedSeasons -contains $season) {
        $skippedManaged++
        continue
    }

    $target = Join-Path $destination $season

    if (Test-Path -LiteralPath $target) {
        if (-not $ReplaceHistorical) {
            $alreadyPresent++
            continue
        }

        Remove-Item -LiteralPath $target -Recurse -Force
    }

    Copy-Item -LiteralPath $seasonDirectory.FullName -Destination $target -Recurse -Force
    $copied++
}

$totalInternal = @(
    Get-ChildItem -LiteralPath $destination -Directory |
        Where-Object { $_.Name -match '^\d{4}_\d{4}$' }
).Count

Write-Host "Archivio origine   : $source"
Write-Host "Archivio interno   : $destination"
Write-Host "Storiche copiate   : $copied"
Write-Host "Gestite escluse    : $skippedManaged"
Write-Host "Gia presenti       : $alreadyPresent"
Write-Host "Stagioni interne   : $totalInternal"
```

### tools\Publish-RecordsNextSite.ps1

```powershell
param(
    [string]$ConfigPath = ".\config\site-publish.local.json",
    [switch]$GenerateOnly,
    [switch]$SkipBuild
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
Set-Location $projectRoot

if (-not (Test-Path -LiteralPath $ConfigPath -PathType Leaf)) {
    throw "Configurazione non trovata: $ConfigPath"
}

$config = Get-Content -LiteralPath $ConfigPath -Raw | ConvertFrom-Json

$required = @(
    "normalizedReports",
    "classicArchive",
    "classicSeasons",
    "ruArchive",
    "ruSeasons",
    "stagingRoot",
    "siteJsDir"
)

foreach ($name in $required) {
    if (-not ($config.PSObject.Properties.Name -contains $name)) {
        throw "Proprieta mancante nella configurazione: $name"
    }
}

function Resolve-ConfiguredPath {
    param([string]$Value)

    if ([string]::IsNullOrWhiteSpace($Value)) {
        throw "Percorso vuoto nella configurazione"
    }

    if ([IO.Path]::IsPathRooted($Value)) {
        return [IO.Path]::GetFullPath($Value)
    }

    return [IO.Path]::GetFullPath((Join-Path $projectRoot $Value))
}

$normalizedReports = Resolve-ConfiguredPath ([string]$config.normalizedReports)
$classicArchive = Resolve-ConfiguredPath ([string]$config.classicArchive)
$ruArchive = Resolve-ConfiguredPath ([string]$config.ruArchive)
$stagingRoot = Resolve-ConfiguredPath ([string]$config.stagingRoot)
$siteJsDir = Resolve-ConfiguredPath ([string]$config.siteJsDir)
$classicSeasons = @($config.classicSeasons | ForEach-Object { [string]$_ })
$ruSeasons = @($config.ruSeasons | ForEach-Object { [string]$_ })

if ($classicSeasons.Count -eq 0) {
    throw "classicSeasons non contiene stagioni"
}

if ($ruSeasons.Count -eq 0) {
    throw "ruSeasons non contiene stagioni"
}

if (-not (Test-Path -LiteralPath $normalizedReports -PathType Container)) {
    throw "Report normalizzati non trovati: $normalizedReports"
}

if (-not (Test-Path -LiteralPath $classicArchive -PathType Container)) {
    throw "Archivio classic interno non trovato: $classicArchive. Eseguire prima Initialize-RecordsNextClassicArchive.ps1"
}

if (-not (Test-Path -LiteralPath $ruArchive -PathType Container)) {
    throw "Archivio RU interno non trovato: $ruArchive. Eseguire prima Initialize-RecordsNextRuArchive.ps1"
}

if (-not $SkipBuild) {
    & ".\mvnw.cmd" clean package dependency:copy-dependencies
    if ($LASTEXITCODE -ne 0) {
        throw "Build Maven fallita con codice $LASTEXITCODE"
    }
}

$classes = (Resolve-Path ".\target\classes").Path
$dependencies = (Resolve-Path ".\target\dependency").Path + "\*"

$ucanAccessJars = Get-ChildItem ".\tools\ucanaccess\2.0.9.5" `
    -Recurse `
    -File `
    -Filter "*.jar"

$classPath = $classes +
    [IO.Path]::PathSeparator +
    $dependencies +
    [IO.Path]::PathSeparator +
    (($ucanAccessJars.FullName) -join [IO.Path]::PathSeparator)

Write-Host ""
Write-Host "Rigenerazione archivio classic RecordsNext"
Write-Host "Report   : $normalizedReports"
Write-Host "Archivio : $classicArchive"
Write-Host "Stagioni : $($classicSeasons -join ', ')"
Write-Host ""

$classicBuilderArguments = @(
    "-cp",
    $classPath,
    "it.alterlega.recordsnext.SeasonRecordsArchiveBuilder",
    $normalizedReports,
    $classicArchive
) + $classicSeasons

& "$env:JAVA_HOME\bin\java.exe" @classicBuilderArguments
if ($LASTEXITCODE -ne 0) {
    throw "Generazione archivio classic terminata con codice $LASTEXITCODE"
}

Write-Host ""
Write-Host "Rigenerazione archivio RU RecordsNext"
Write-Host "Report   : $normalizedReports"
Write-Host "Archivio : $ruArchive"
Write-Host "Stagioni : $($ruSeasons -join ', ')"
Write-Host ""

$ruBuilderArguments = @(
    "-cp",
    $classPath,
    "it.alterlega.recordsnext.RiserveUfficioArchiveBuilder",
    $normalizedReports,
    $ruArchive
) + $ruSeasons

& "$env:JAVA_HOME\bin\java.exe" @ruBuilderArguments
if ($LASTEXITCODE -ne 0) {
    throw "Generazione archivio RU terminata con codice $LASTEXITCODE"
}

$classicSeasonCount = @(
    Get-ChildItem -LiteralPath $classicArchive -Directory |
        Where-Object { $_.Name -match '^\d{4}_\d{4}$' }
).Count

if ($classicSeasonCount -lt 20) {
    throw "Archivio classic incompleto: trovate $classicSeasonCount stagioni, attese almeno 20"
}

$ruSeasonCount = @(
    Get-ChildItem -LiteralPath $ruArchive -Directory |
        Where-Object { $_.Name -match '^\d{4}_\d{4}$' }
).Count

if ($ruSeasonCount -lt 20) {
    throw "Archivio RU incompleto: trovate $ruSeasonCount stagioni, attese almeno 20"
}

$publisherArguments = @(
    "-cp",
    $classPath,
    "it.alterlega.recordsnext.Records2026SitePublisher",
    $classicArchive,
    $ruArchive,
    $stagingRoot,
    $siteJsDir
)

if ($GenerateOnly) {
    $publisherArguments += "--generate-only"
}

Write-Host ""
Write-Host "RecordsNext site publish"
Write-Host "Classic : $classicArchive ($classicSeasonCount stagioni)"
Write-Host "RU      : $ruArchive ($ruSeasonCount stagioni)"
Write-Host "Staging : $stagingRoot"
Write-Host "Sito JS : $siteJsDir"
Write-Host "Modalita: $(if ($GenerateOnly) { 'GENERAZIONE' } else { 'PUBBLICAZIONE' })"
Write-Host ""

& "$env:JAVA_HOME\bin\java.exe" @publisherArguments

if ($LASTEXITCODE -ne 0) {
    throw "Publisher terminato con codice $LASTEXITCODE"
}
```

### tools\Validate-NormalizedSeason-v3.ps1

```powershell
param(
    [string]$RecordsNextDir = "D:\DEV_APPS\RecordsNext\data\reports\2025_2026",
    [string]$Records2026Dir = "D:\DEV_APPS\Records2026\data_archive\stagioni\2025_2026"
)

$nameMap = @{
    "season_normalized_coppa_serie_a.json" = "season_normalized_coppa_lega_serie_a.json"
    "season_normalized_coppa_serie_b.json" = "season_normalized_coppa_lega_serie_b.json"
    "season_normalized_coppa_serie_c.json" = "season_normalized_coppa_lega_serie_c.json"
}

$sections = @(
    "partiteSquadra",
    "espulsioniDettaglio",
    "eventiSquadraDettaglio",
    "modificatoriB2Dettaglio",
    "cleanSheetB3Dettaglio"
)

function Get-JsonText($value) {
    return ($value | ConvertTo-Json -Depth 30 -Compress)
}

function Get-NormalizedSection($value) {
    return @(
        @($value) | Where-Object {
            if ($null -eq $_) {
                return $false
            }

            if ($_ -is [System.Management.Automation.PSCustomObject]) {
                return @($_.PSObject.Properties).Count -gt 0
            }

            return $true
        }
    )
}

$files = Get-ChildItem $RecordsNextDir -File -Filter "season_normalized_*.json" |
    Where-Object {
        $_.BaseName -notmatch '\.stage\d+$' -and
        $_.BaseName -notmatch '\.final$'
    } |
    Sort-Object Name

$results = foreach ($nextFile in $files) {
    $refName = if ($nameMap.ContainsKey($nextFile.Name)) {
        $nameMap[$nextFile.Name]
    } else {
        $nextFile.Name
    }

    $refFile = Join-Path $Records2026Dir $refName

    if (-not (Test-Path $refFile)) {
        [PSCustomObject]@{
            File = $nextFile.Name
            ReferenceFile = $refName
            Reference = "MISSING"
            Meta = $null
            Partite = $null
            Espulsioni = $null
            Eventi = $null
            Modificatori = $null
            CleanSheet = $null
            Dati = $false
        }
        continue
    }

    $ref = Get-Content $refFile -Raw | ConvertFrom-Json
    $next = Get-Content $nextFile.FullName -Raw | ConvertFrom-Json

    $equal = @{}
    foreach ($section in $sections) {
        $refSection = @(Get-NormalizedSection $ref.$section)
        $nextSection = @(Get-NormalizedSection $next.$section)

        $equal[$section] = (
            (Get-JsonText $refSection) -ceq
            (Get-JsonText $nextSection)
        )
    }

    $metaEqual = ((Get-JsonText $ref.meta) -ceq (Get-JsonText $next.meta))

    [PSCustomObject]@{
        File = $nextFile.Name
        ReferenceFile = $refName
        Reference = "OK"
        Meta = $metaEqual
        Partite = $equal["partiteSquadra"]
        Espulsioni = $equal["espulsioniDettaglio"]
        Eventi = $equal["eventiSquadraDettaglio"]
        Modificatori = $equal["modificatoriB2Dettaglio"]
        CleanSheet = $equal["cleanSheetB3Dettaglio"]
        Dati = (
            $equal["partiteSquadra"] -and
            $equal["espulsioniDettaglio"] -and
            $equal["eventiSquadraDettaglio"] -and
            $equal["modificatoriB2Dettaglio"] -and
            $equal["cleanSheetB3Dettaglio"]
        )
    }
}

$results | Format-Table File,Reference,Partite,Espulsioni,Eventi,Modificatori,CleanSheet,Dati -AutoSize

"`n=== RIEPILOGO DATI ==="
"File canonici RecordsNext : $($results.Count)"
"Reference trovati         : $(($results | Where-Object Reference -eq 'OK').Count)"
"Dati completamente True   : $(($results | Where-Object Dati -eq $true).Count)"
"Con differenze dati       : $(($results | Where-Object { $_.Reference -eq 'OK' -and -not $_.Dati }).Count)"
"Reference mancanti        : $(($results | Where-Object Reference -eq 'MISSING').Count)"

"`n=== DIAGNOSTICA DIFFERENZE ==="

foreach ($row in $results | Where-Object { $_.Reference -eq "OK" -and -not $_.Dati }) {
    $nextFile = Join-Path $RecordsNextDir $row.File
    $refFile = Join-Path $Records2026Dir $row.ReferenceFile

    $ref = Get-Content $refFile -Raw | ConvertFrom-Json
    $next = Get-Content $nextFile -Raw | ConvertFrom-Json

    "`n--- $($row.File) ---"

    foreach ($section in $sections) {
        $isEqual = $row.($section -replace "partiteSquadra","Partite" -replace "espulsioniDettaglio","Espulsioni" -replace "eventiSquadraDettaglio","Eventi" -replace "modificatoriB2Dettaglio","Modificatori" -replace "cleanSheetB3Dettaglio","CleanSheet")

        if ($isEqual -eq $false) {
            $refSection = @(Get-NormalizedSection $ref.$section)
            $nextSection = @(Get-NormalizedSection $next.$section)

            $refCount = $refSection.Count
            $nextCount = $nextSection.Count

            "Sezione: $section | ref=$refCount | next=$nextCount"

            $max = [Math]::Min($refCount, $nextCount)
            $firstDiff = $null

            for ($i = 0; $i -lt $max; $i++) {
                if (
                    (Get-JsonText $refSection[$i]) -cne
                    (Get-JsonText $nextSection[$i])
                ) {
                    $firstDiff = $i
                    break
                }
            }

            if ($null -eq $firstDiff -and $refCount -ne $nextCount) {
                $firstDiff = $max
            }

            if ($null -ne $firstDiff) {
                "Prima differenza: indice $firstDiff"
                if ($firstDiff -lt $refCount) {
                    "REF : $(Get-JsonText $refSection[$firstDiff])"
                }
                if ($firstDiff -lt $nextCount) {
                    "NEXT: $(Get-JsonText $nextSection[$firstDiff])"
                }
            }
        }
    }
}

$csv = Join-Path $RecordsNextDir "validation_summary_v3.csv"
$results | Export-Csv $csv -NoTypeInformation -Encoding UTF8
"`nReport CSV: $csv"
```

### tools\Validate-NormalizedSeason-v8.ps1

```powershell
param(
    [string]$RecordsNextDir = "D:\DEV_APPS\RecordsNext\data\reports\2025_2026",
    [string]$Records2026Dir = "D:\DEV_APPS\Records2026\data_archive\stagioni\2025_2026"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$nameMap = @{
    "season_normalized_coppa_serie_a.json" = "season_normalized_coppa_lega_serie_a.json"
    "season_normalized_coppa_serie_b.json" = "season_normalized_coppa_lega_serie_b.json"
    "season_normalized_coppa_serie_c.json" = "season_normalized_coppa_lega_serie_c.json"
}

$sections = @(
    "partiteSquadra",
    "espulsioniDettaglio",
    "eventiSquadraDettaglio",
    "modificatoriB2Dettaglio",
    "cleanSheetB3Dettaglio"
)

$calendarFields = @(
    "dataGiornata",
    "oraGiornata",
    "dataOraGiornata"
)

function Get-JsonText($value) {
    return ($value | ConvertTo-Json -Depth 30 -Compress)
}


function Convert-ToRowArray($value) {
    $rows = @()

    foreach ($item in @($value)) {
        if ($null -eq $item) {
            continue
        }

        # Alcuni vecchi export Records2026 serializzano sezioni vuote come {}.
        # Un oggetto senza proprieta equivale a una collezione vuota, non a una riga.
        if (@($item.PSObject.Properties).Count -eq 0) {
            continue
        }

        $rows += $item
    }

    return @($rows)
}

function Copy-WithoutCalendarFields($rows) {
    $cleanRows = @()

    foreach ($row in @($rows)) {
        $copy = [ordered]@{}
        foreach ($property in $row.PSObject.Properties) {
            if ($calendarFields -notcontains $property.Name) {
                $copy[$property.Name] = $property.Value
            }
        }
        $cleanRows += [PSCustomObject]$copy
    }

    return $cleanRows
}

function Test-CalendarRows($rows) {
    $errors = [System.Collections.Generic.List[string]]::new()
    $datedRows = 0
    $undatedRows = 0

    foreach ($row in @($rows)) {
        $date = $row.dataGiornata
        $time = $row.oraGiornata
        $dateTime = $row.dataOraGiornata

        $allEmpty = [string]::IsNullOrWhiteSpace([string]$date) -and
                    [string]::IsNullOrWhiteSpace([string]$time) -and
                    [string]::IsNullOrWhiteSpace([string]$dateTime)

        if ($allEmpty) {
            $undatedRows++
            continue
        }

        $datedRows++

        if ([string]::IsNullOrWhiteSpace([string]$date)) {
            $errors.Add("idIncontro=$($row.idIncontro): dataGiornata mancante")
            continue
        }

        $parsedDate = [datetime]::MinValue
        if (-not [datetime]::TryParseExact(
                [string]$date,
                "yyyy-MM-dd",
                [Globalization.CultureInfo]::InvariantCulture,
                [Globalization.DateTimeStyles]::None,
                [ref]$parsedDate)) {
            $errors.Add("idIncontro=$($row.idIncontro): dataGiornata non valida '$date'")
        }

        if (-not [string]::IsNullOrWhiteSpace([string]$time)) {
            $parsedTime = [datetime]::MinValue
            if (-not [datetime]::TryParseExact(
                    [string]$time,
                    "HH:mm",
                    [Globalization.CultureInfo]::InvariantCulture,
                    [Globalization.DateTimeStyles]::None,
                    [ref]$parsedTime)) {
                $errors.Add("idIncontro=$($row.idIncontro): oraGiornata non valida '$time'")
            }
        }

        $expectedDateTime = if ([string]::IsNullOrWhiteSpace([string]$time)) {
            $null
        } else {
            "$date`T$time"
        }

        if ($expectedDateTime -ne $dateTime) {
            $errors.Add(
                "idIncontro=$($row.idIncontro): dataOraGiornata='$dateTime', atteso='$expectedDateTime'"
            )
        }
    }

    [PSCustomObject]@{
        Valid = ($errors.Count -eq 0)
        DatedRows = $datedRows
        UndatedRows = $undatedRows
        Errors = $errors
    }
}

if (-not (Test-Path $RecordsNextDir -PathType Container)) {
    throw "Directory RecordsNext non trovata: $RecordsNextDir"
}
if (-not (Test-Path $Records2026Dir -PathType Container)) {
    throw "Directory Records2026 non trovata: $Records2026Dir"
}

$files = Get-ChildItem $RecordsNextDir -File -Filter "season_normalized_*.json" |
    Where-Object {
        $_.BaseName -notmatch '\.stage\d+$' -and
        $_.BaseName -notmatch '\.final$'
    } |
    Sort-Object Name

$results = foreach ($nextFile in $files) {
    $refName = if ($nameMap.ContainsKey($nextFile.Name)) {
        $nameMap[$nextFile.Name]
    } else {
        $nextFile.Name
    }

    $refFile = Join-Path $Records2026Dir $refName

    if (-not (Test-Path $refFile -PathType Leaf)) {
        [PSCustomObject]@{
            File = $nextFile.Name
            ReferenceFile = $refName
            Reference = "MISSING"
            Meta = $null
            Partite = $null
            Calendario = $null
            RigheConData = 0
            RigheSenzaData = 0
            Espulsioni = $null
            Eventi = $null
            Modificatori = $null
            CleanSheet = $null
            Dati = $false
            CalendarErrors = @()
        }
        continue
    }

    $ref = Get-Content $refFile -Raw | ConvertFrom-Json
    $next = Get-Content $nextFile.FullName -Raw | ConvertFrom-Json

    $equal = @{}
    foreach ($section in $sections) {
        $refRows = Convert-ToRowArray $ref.$section
        $nextRows = Convert-ToRowArray $next.$section

        if ($section -eq "partiteSquadra") {
            $refLegacy = Copy-WithoutCalendarFields $refRows
            $nextLegacy = Copy-WithoutCalendarFields $nextRows
            $equal[$section] = ((Get-JsonText $refLegacy) -ceq (Get-JsonText $nextLegacy))
        } else {
            $equal[$section] = ((Get-JsonText $refRows) -ceq (Get-JsonText $nextRows))
        }
    }

    $calendar = Test-CalendarRows $next.partiteSquadra
    $metaEqual = ((Get-JsonText $ref.meta) -ceq (Get-JsonText $next.meta))

    [PSCustomObject]@{
        File = $nextFile.Name
        ReferenceFile = $refName
        Reference = "OK"
        Meta = $metaEqual
        Partite = $equal["partiteSquadra"]
        Calendario = $calendar.Valid
        RigheConData = $calendar.DatedRows
        RigheSenzaData = $calendar.UndatedRows
        Espulsioni = $equal["espulsioniDettaglio"]
        Eventi = $equal["eventiSquadraDettaglio"]
        Modificatori = $equal["modificatoriB2Dettaglio"]
        CleanSheet = $equal["cleanSheetB3Dettaglio"]
        Dati = (
            $equal["partiteSquadra"] -and
            $calendar.Valid -and
            $equal["espulsioniDettaglio"] -and
            $equal["eventiSquadraDettaglio"] -and
            $equal["modificatoriB2Dettaglio"] -and
            $equal["cleanSheetB3Dettaglio"]
        )
        CalendarErrors = @($calendar.Errors)
    }
}

$results |
    Format-Table File,Reference,Partite,Calendario,Espulsioni,Eventi,Modificatori,CleanSheet,Dati -AutoSize

"`n=== RIEPILOGO DATI ==="
"File canonici RecordsNext : $(@($results).Count)"
"Reference trovati         : $(@($results | Where-Object Reference -eq 'OK').Count)"
"Calendari validi          : $(@($results | Where-Object Calendario -eq $true).Count)"
"Dati completamente True   : $(@($results | Where-Object Dati -eq $true).Count)"
"Con differenze dati       : $(@($results | Where-Object { $_.Reference -eq 'OK' -and -not $_.Dati }).Count)"
"Reference mancanti        : $(@($results | Where-Object Reference -eq 'MISSING').Count)"

"`n=== DIAGNOSTICA DIFFERENZE ==="

foreach ($row in $results | Where-Object { $_.Reference -eq "OK" -and -not $_.Dati }) {
    $nextFile = Join-Path $RecordsNextDir $row.File
    $refFile = Join-Path $Records2026Dir $row.ReferenceFile

    $ref = Get-Content $refFile -Raw | ConvertFrom-Json
    $next = Get-Content $nextFile -Raw | ConvertFrom-Json

    "`n--- $($row.File) ---"

    if (-not $row.Calendario) {
        "Sezione: calendario | con data=$($row.RigheConData) | senza data=$($row.RigheSenzaData)"
        foreach ($errorText in @($row.CalendarErrors) | Select-Object -First 5) {
            "ERRORE: $errorText"
        }
    }

    foreach ($section in $sections) {
        $propertyName = switch ($section) {
            "partiteSquadra" { "Partite" }
            "espulsioniDettaglio" { "Espulsioni" }
            "eventiSquadraDettaglio" { "Eventi" }
            "modificatoriB2Dettaglio" { "Modificatori" }
            "cleanSheetB3Dettaglio" { "CleanSheet" }
        }

        if ($row.$propertyName -eq $false) {
            $refSourceRows = Convert-ToRowArray $ref.$section
            $nextSourceRows = Convert-ToRowArray $next.$section

            $refRows = if ($section -eq "partiteSquadra") {
                @(Copy-WithoutCalendarFields $refSourceRows)
            } else {
                @($refSourceRows)
            }
            $nextRows = if ($section -eq "partiteSquadra") {
                @(Copy-WithoutCalendarFields $nextSourceRows)
            } else {
                @($nextSourceRows)
            }

            $refCount = @($refRows).Count
            $nextCount = @($nextRows).Count
            "Sezione: $section | ref=$refCount | next=$nextCount"

            $max = [Math]::Min($refCount, $nextCount)
            $firstDiff = $null

            for ($i = 0; $i -lt $max; $i++) {
                if ((Get-JsonText $refRows[$i]) -cne (Get-JsonText $nextRows[$i])) {
                    $firstDiff = $i
                    break
                }
            }

            if ($null -eq $firstDiff -and $refCount -ne $nextCount) {
                $firstDiff = $max
            }

            if ($null -ne $firstDiff) {
                "Prima differenza: indice $firstDiff"
                if ($firstDiff -lt $refCount) {
                    "REF : $(Get-JsonText $refRows[$firstDiff])"
                }
                if ($firstDiff -lt $nextCount) {
                    "NEXT: $(Get-JsonText $nextRows[$firstDiff])"
                }
            }
        }
    }
}

$csv = Join-Path $RecordsNextDir "validation_summary_v7.csv"
$results |
    Select-Object File,ReferenceFile,Reference,Meta,Partite,Calendario,RigheConData,RigheSenzaData,Espulsioni,Eventi,Modificatori,CleanSheet,Dati |
    Export-Csv $csv -NoTypeInformation -Encoding UTF8

"`nReport CSV: $csv"
```

### tools\Validate-NormalizedSeason-v9.ps1

```powershell
param(
    [string]$RecordsNextDir = "D:\DEV_APPS\RecordsNext\data\reports\2025_2026",
    [string]$Records2026Dir = "D:\DEV_APPS\Records2026\data_archive\stagioni\2025_2026"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$nameMap = @{
    "season_normalized_coppa_serie_a.json" = "season_normalized_coppa_lega_serie_a.json"
    "season_normalized_coppa_serie_b.json" = "season_normalized_coppa_lega_serie_b.json"
    "season_normalized_coppa_serie_c.json" = "season_normalized_coppa_lega_serie_c.json"
}

$sections = @(
    "partiteSquadra",
    "espulsioniDettaglio",
    "eventiSquadraDettaglio",
    "modificatoriB2Dettaglio",
    "cleanSheetB3Dettaglio"
)

$extendedFields = @(
    "dataGiornata",
    "oraGiornata",
    "dataOraGiornata",
    "urlTabellinoLocale",
    "urlTabellinoOnline"
)

function Get-JsonText($value) {
    return ($value | ConvertTo-Json -Depth 30 -Compress)
}


function Convert-ToRowArray($value) {
    $rows = @()

    foreach ($item in @($value)) {
        if ($null -eq $item) {
            continue
        }

        # Alcuni vecchi export Records2026 serializzano sezioni vuote come {}.
        # Un oggetto senza proprieta equivale a una collezione vuota, non a una riga.
        if (@($item.PSObject.Properties).Count -eq 0) {
            continue
        }

        $rows += $item
    }

    return @($rows)
}

function Copy-WithoutExtendedFields($rows) {
    $cleanRows = @()

    foreach ($row in @($rows)) {
        $copy = [ordered]@{}
        foreach ($property in $row.PSObject.Properties) {
            if ($extendedFields -notcontains $property.Name) {
                $copy[$property.Name] = $property.Value
            }
        }
        $cleanRows += [PSCustomObject]$copy
    }

    return $cleanRows
}


function Test-LinkRows($rows) {
    $errors = [System.Collections.Generic.List[string]]::new()
    $localLinks = 0
    $onlineLinks = 0

    foreach ($row in @($rows)) {
        $round = [string]$row.giornataDiA
        $local = [string]$row.urlTabellinoLocale
        $online = [string]$row.urlTabellinoOnline

        if (-not [string]::IsNullOrWhiteSpace($local)) {
            $localLinks++
            if ($local -notmatch '^\.\./[^/\\]+/ris\.php\?Gio=\d+$') {
                $errors.Add("idIncontro=$($row.idIncontro): urlTabellinoLocale non valido '$local'")
            } elseif ($local -notmatch ('\?Gio=' + [regex]::Escape($round) + '$')) {
                $errors.Add("idIncontro=$($row.idIncontro): urlTabellinoLocale non coerente con giornataDiA=$round")
            }
        }

        if (-not [string]::IsNullOrWhiteSpace($online)) {
            $onlineLinks++
            if ($online -notmatch '^https?://.+/ris\.php\?Gio=\d+$') {
                $errors.Add("idIncontro=$($row.idIncontro): urlTabellinoOnline non valido '$online'")
            } elseif ($online -notmatch ('\?Gio=' + [regex]::Escape($round) + '$')) {
                $errors.Add("idIncontro=$($row.idIncontro): urlTabellinoOnline non coerente con giornataDiA=$round")
            }
        }
    }

    [PSCustomObject]@{
        Valid = ($errors.Count -eq 0)
        LocalLinks = $localLinks
        OnlineLinks = $onlineLinks
        Errors = $errors
    }
}

function Test-CalendarRows($rows) {
    $errors = [System.Collections.Generic.List[string]]::new()
    $datedRows = 0
    $undatedRows = 0

    foreach ($row in @($rows)) {
        $date = $row.dataGiornata
        $time = $row.oraGiornata
        $dateTime = $row.dataOraGiornata

        $allEmpty = [string]::IsNullOrWhiteSpace([string]$date) -and
                    [string]::IsNullOrWhiteSpace([string]$time) -and
                    [string]::IsNullOrWhiteSpace([string]$dateTime)

        if ($allEmpty) {
            $undatedRows++
            continue
        }

        $datedRows++

        if ([string]::IsNullOrWhiteSpace([string]$date)) {
            $errors.Add("idIncontro=$($row.idIncontro): dataGiornata mancante")
            continue
        }

        $parsedDate = [datetime]::MinValue
        if (-not [datetime]::TryParseExact(
                [string]$date,
                "yyyy-MM-dd",
                [Globalization.CultureInfo]::InvariantCulture,
                [Globalization.DateTimeStyles]::None,
                [ref]$parsedDate)) {
            $errors.Add("idIncontro=$($row.idIncontro): dataGiornata non valida '$date'")
        }

        if (-not [string]::IsNullOrWhiteSpace([string]$time)) {
            $parsedTime = [datetime]::MinValue
            if (-not [datetime]::TryParseExact(
                    [string]$time,
                    "HH:mm",
                    [Globalization.CultureInfo]::InvariantCulture,
                    [Globalization.DateTimeStyles]::None,
                    [ref]$parsedTime)) {
                $errors.Add("idIncontro=$($row.idIncontro): oraGiornata non valida '$time'")
            }
        }

        $expectedDateTime = if ([string]::IsNullOrWhiteSpace([string]$time)) {
            $null
        } else {
            "$date`T$time"
        }

        if ($expectedDateTime -ne $dateTime) {
            $errors.Add(
                "idIncontro=$($row.idIncontro): dataOraGiornata='$dateTime', atteso='$expectedDateTime'"
            )
        }
    }

    [PSCustomObject]@{
        Valid = ($errors.Count -eq 0)
        DatedRows = $datedRows
        UndatedRows = $undatedRows
        Errors = $errors
    }
}

if (-not (Test-Path $RecordsNextDir -PathType Container)) {
    throw "Directory RecordsNext non trovata: $RecordsNextDir"
}
if (-not (Test-Path $Records2026Dir -PathType Container)) {
    throw "Directory Records2026 non trovata: $Records2026Dir"
}

$files = Get-ChildItem $RecordsNextDir -File -Filter "season_normalized_*.json" |
    Where-Object {
        $_.BaseName -notmatch '\.stage\d+$' -and
        $_.BaseName -notmatch '\.final$'
    } |
    Sort-Object Name

$results = foreach ($nextFile in $files) {
    $refName = if ($nameMap.ContainsKey($nextFile.Name)) {
        $nameMap[$nextFile.Name]
    } else {
        $nextFile.Name
    }

    $refFile = Join-Path $Records2026Dir $refName

    if (-not (Test-Path $refFile -PathType Leaf)) {
        [PSCustomObject]@{
            File = $nextFile.Name
            ReferenceFile = $refName
            Reference = "MISSING"
            Meta = $null
            Partite = $null
            Calendario = $null
            Collegamenti = $null
            LinkLocali = 0
            LinkOnline = 0
            RigheConData = 0
            RigheSenzaData = 0
            Espulsioni = $null
            Eventi = $null
            Modificatori = $null
            CleanSheet = $null
            Dati = $false
            CalendarErrors = @()
        }
        continue
    }

    $ref = Get-Content $refFile -Raw | ConvertFrom-Json
    $next = Get-Content $nextFile.FullName -Raw | ConvertFrom-Json

    $equal = @{}
    foreach ($section in $sections) {
        $refRows = Convert-ToRowArray $ref.$section
        $nextRows = Convert-ToRowArray $next.$section

        if ($section -eq "partiteSquadra") {
            $refLegacy = Copy-WithoutExtendedFields $refRows
            $nextLegacy = Copy-WithoutExtendedFields $nextRows
            $equal[$section] = ((Get-JsonText $refLegacy) -ceq (Get-JsonText $nextLegacy))
        } else {
            $equal[$section] = ((Get-JsonText $refRows) -ceq (Get-JsonText $nextRows))
        }
    }

    $calendar = Test-CalendarRows $next.partiteSquadra
    $links = Test-LinkRows $next.partiteSquadra
    $metaEqual = ((Get-JsonText $ref.meta) -ceq (Get-JsonText $next.meta))

    [PSCustomObject]@{
        File = $nextFile.Name
        ReferenceFile = $refName
        Reference = "OK"
        Meta = $metaEqual
        Partite = $equal["partiteSquadra"]
        Calendario = $calendar.Valid
        Collegamenti = $links.Valid
        LinkLocali = $links.LocalLinks
        LinkOnline = $links.OnlineLinks
        RigheConData = $calendar.DatedRows
        RigheSenzaData = $calendar.UndatedRows
        Espulsioni = $equal["espulsioniDettaglio"]
        Eventi = $equal["eventiSquadraDettaglio"]
        Modificatori = $equal["modificatoriB2Dettaglio"]
        CleanSheet = $equal["cleanSheetB3Dettaglio"]
        Dati = (
            $equal["partiteSquadra"] -and
            $calendar.Valid -and
            $links.Valid -and
            $equal["espulsioniDettaglio"] -and
            $equal["eventiSquadraDettaglio"] -and
            $equal["modificatoriB2Dettaglio"] -and
            $equal["cleanSheetB3Dettaglio"]
        )
        CalendarErrors = @($calendar.Errors)
        LinkErrors = @($links.Errors)
    }
}

$results |
    Format-Table File,Reference,Partite,Calendario,Collegamenti,Espulsioni,Eventi,Modificatori,CleanSheet,Dati -AutoSize

"`n=== RIEPILOGO DATI ==="
"File canonici RecordsNext : $(@($results).Count)"
"Reference trovati         : $(@($results | Where-Object Reference -eq 'OK').Count)"
"Calendari validi          : $(@($results | Where-Object Calendario -eq $true).Count)"
"Collegamenti validi       : $(@($results | Where-Object Collegamenti -eq $true).Count)"
"Dati completamente True   : $(@($results | Where-Object Dati -eq $true).Count)"
"Con differenze dati       : $(@($results | Where-Object { $_.Reference -eq 'OK' -and -not $_.Dati }).Count)"
"Reference mancanti        : $(@($results | Where-Object Reference -eq 'MISSING').Count)"

"`n=== DIAGNOSTICA DIFFERENZE ==="

foreach ($row in $results | Where-Object { $_.Reference -eq "OK" -and -not $_.Dati }) {
    $nextFile = Join-Path $RecordsNextDir $row.File
    $refFile = Join-Path $Records2026Dir $row.ReferenceFile

    $ref = Get-Content $refFile -Raw | ConvertFrom-Json
    $next = Get-Content $nextFile -Raw | ConvertFrom-Json

    "`n--- $($row.File) ---"

    if (-not $row.Calendario) {
        "Sezione: calendario | con data=$($row.RigheConData) | senza data=$($row.RigheSenzaData)"
        foreach ($errorText in @($row.CalendarErrors) | Select-Object -First 5) {
            "ERRORE: $errorText"
        }
    }

    if (-not $row.Collegamenti) {
        "Sezione: collegamenti | locali=$($row.LinkLocali) | online=$($row.LinkOnline)"
        foreach ($errorText in @($row.LinkErrors) | Select-Object -First 5) {
            "ERRORE: $errorText"
        }
    }

    foreach ($section in $sections) {
        $propertyName = switch ($section) {
            "partiteSquadra" { "Partite" }
            "espulsioniDettaglio" { "Espulsioni" }
            "eventiSquadraDettaglio" { "Eventi" }
            "modificatoriB2Dettaglio" { "Modificatori" }
            "cleanSheetB3Dettaglio" { "CleanSheet" }
        }

        if ($row.$propertyName -eq $false) {
            $refSourceRows = Convert-ToRowArray $ref.$section
            $nextSourceRows = Convert-ToRowArray $next.$section

            $refRows = if ($section -eq "partiteSquadra") {
                @(Copy-WithoutExtendedFields $refSourceRows)
            } else {
                @($refSourceRows)
            }
            $nextRows = if ($section -eq "partiteSquadra") {
                @(Copy-WithoutExtendedFields $nextSourceRows)
            } else {
                @($nextSourceRows)
            }

            $refCount = @($refRows).Count
            $nextCount = @($nextRows).Count
            "Sezione: $section | ref=$refCount | next=$nextCount"

            $max = [Math]::Min($refCount, $nextCount)
            $firstDiff = $null

            for ($i = 0; $i -lt $max; $i++) {
                if ((Get-JsonText $refRows[$i]) -cne (Get-JsonText $nextRows[$i])) {
                    $firstDiff = $i
                    break
                }
            }

            if ($null -eq $firstDiff -and $refCount -ne $nextCount) {
                $firstDiff = $max
            }

            if ($null -ne $firstDiff) {
                "Prima differenza: indice $firstDiff"
                if ($firstDiff -lt $refCount) {
                    "REF : $(Get-JsonText $refRows[$firstDiff])"
                }
                if ($firstDiff -lt $nextCount) {
                    "NEXT: $(Get-JsonText $nextRows[$firstDiff])"
                }
            }
        }
    }
}

$csv = Join-Path $RecordsNextDir "validation_summary_v9.csv"
$results |
    Select-Object File,ReferenceFile,Reference,Meta,Partite,Calendario,Collegamenti,LinkLocali,LinkOnline,RigheConData,RigheSenzaData,Espulsioni,Eventi,Modificatori,CleanSheet,Dati |
    Export-Csv $csv -NoTypeInformation -Encoding UTF8

"`nReport CSV: $csv"
```
