[CmdletBinding()]
param(
    [string]$ProjectDir = "D:\DEV_APPS\RecordsNext2.0"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$DocsDir = Join-Path $ProjectDir "docs"
$OutputFile = Join-Path $DocsDir "CODICE_FUNZIONANTE_RECORDSNEXT2.md"

if (-not (Test-Path -LiteralPath $ProjectDir)) {
    throw "Directory progetto non trovata: $ProjectDir"
}

if (-not (Test-Path -LiteralPath $DocsDir)) {
    New-Item -ItemType Directory -Path $DocsDir -Force | Out-Null
}

function Add-IndentedFileSection {
    param(
        [Parameter(Mandatory = $true)]
        [System.Text.StringBuilder]$Builder,

        [Parameter(Mandatory = $true)]
        [string]$Title,

        [Parameter(Mandatory = $true)]
        [string]$RelativePath
    )

    $FullPath = Join-Path $ProjectDir $RelativePath

    [void]$Builder.AppendLine("## " + $Title)
    [void]$Builder.AppendLine("")

    if (-not (Test-Path -LiteralPath $FullPath)) {
        [void]$Builder.AppendLine("> File non presente: " + $RelativePath)
        [void]$Builder.AppendLine("")
        return
    }

    [void]$Builder.AppendLine("File: " + $RelativePath)
    [void]$Builder.AppendLine("")

    $Lines = Get-Content -LiteralPath $FullPath -Encoding UTF8

    if ($Lines.Count -eq 0) {
        [void]$Builder.AppendLine("    [file vuoto]")
    }
    else {
        foreach ($Line in $Lines) {
            [void]$Builder.AppendLine("    " + $Line)
        }
    }

    [void]$Builder.AppendLine("")
}

$Builder = New-Object System.Text.StringBuilder
$GeneratedAt = Get-Date -Format "yyyy-MM-dd HH:mm:ss zzz"

[void]$Builder.AppendLine("# Codice funzionante RecordsNext 3.1")
[void]$Builder.AppendLine("")
[void]$Builder.AppendLine("> Documento generato automaticamente.")
[void]$Builder.AppendLine("> Data generazione: " + $GeneratedAt)
[void]$Builder.AppendLine("> Directory progetto: " + $ProjectDir)
[void]$Builder.AppendLine("")

[void]$Builder.AppendLine("## Stato release RecordsNext 3.1.0 - 2026-09-02")
[void]$Builder.AppendLine("")
[void]$Builder.AppendLine("RecordsNext 3.1.0 e' completato, collaudato e pubblicato. La distribuzione pubblica avviene tramite installer clean-install.")
[void]$Builder.AppendLine("")
[void]$Builder.AppendLine("- Suite automatica: 50 test, 0 failure, 0 errori.")
[void]$Builder.AppendLine("- Collaudo multisito reale: 21 target, 189 file validati e 189 pubblicati.")
[void]$Builder.AppendLine("- Setup pubblico SHA256: C39B0EA205F7D81660CD1FF09EC3F6014090932FF3E0D4DA721870D2740CA6EB.")
[void]$Builder.AppendLine("")
[void]$Builder.AppendLine("### Multisito 3.1")
[void]$Builder.AppendLine("")
[void]$Builder.AppendLine("- FCM/FCA/DataA definiscono lo storico; i siti sono destinazioni opzionali di pubblicazione;")
[void]$Builder.AppendLine("- il sito locale per stagione e' opzionale; una stagione senza sito resta nello storico;")
[void]$Builder.AppendLine("- DataA canonico: ``data/calendars/DataA-YYYY.js``;")
[void]$Builder.AppendLine("- la cartella JS deriva sempre da ``<local_site_path>/js``;")
[void]$Builder.AppendLine("- cutoff multisito basato su ``rn_season.sort_order``;")
[void]$Builder.AppendLine("- ogni sito riceve solo lo storico fino alla propria stagione;")
[void]$Builder.AppendLine("- Core e Manifest vengono filtrati/coerenti con la stagione target;")
[void]$Builder.AppendLine("- la GUI distingue pubblicazione nel sito corrente e pubblicazione dei siti delle stagioni selezionate;")
[void]$Builder.AppendLine("- rimosso il fallback hardcoded verso ``E:/fantacalcio/Lega2025/js``;")
[void]$Builder.AppendLine("- fix multisito Culometro: ``config/culometro.json`` disponibile nello scope di staging;")
[void]$Builder.AppendLine("- collaudo sandbox: 18 file pubblicati su due target selezionati; nessuna stagione futura rilevata nei JS;")
[void]$Builder.AppendLine("- suite verificata: 50 test, 0 failure, 0 errori;")
[void]$Builder.AppendLine("- la 3.1 non e' ancora installata nell'ambiente operativo e i siti reali non sono ancora stati modificati.")
[void]$Builder.AppendLine("")
[void]$Builder.AppendLine("## Stato consolidato RecordsNext 2.1.0 - 2026-08-27")
[void]$Builder.AppendLine("")
[void]$Builder.AppendLine("RecordsNext 2.1.0 e' la release stabile corrente.")
[void]$Builder.AppendLine("")
[void]$Builder.AppendLine("Riferimento Git:")
[void]$Builder.AppendLine("")
[void]$Builder.AppendLine("- commit release: ``682f0f6`` - ``Release RecordsNext 2.1.0``;")
[void]$Builder.AppendLine("- tag: ``v2.1.0``;")
[void]$Builder.AppendLine("- branch di sviluppo: ``main``.")
[void]$Builder.AppendLine("")
[void]$Builder.AppendLine("### Stato operativo corrente")
[void]$Builder.AppendLine("")
[void]$Builder.AppendLine("- stagione corrente: ``2026_2027`` (2026/2027);")
[void]$Builder.AppendLine("- sito FCM corrente configurato: ``E:\fantacalcio\Lega2026``;")
[void]$Builder.AppendLine("- cartella JS corrente: ``E:\fantacalcio\Lega2026\js``;")
[void]$Builder.AppendLine("- archivio Classici/RU consolidato disponibile per le stagioni 2006_2007-2025_2026;")
[void]$Builder.AppendLine("- la stagione 2026_2027 e' registrata come anchor corrente nel database operativo e dispone dei report normalizzati correnti.")
[void]$Builder.AppendLine("")
[void]$Builder.AppendLine("### Funzionalita' consolidate nella 2.1")
[void]$Builder.AppendLine("")
[void]$Builder.AppendLine("- output modulari pubblici:")
[void]$Builder.AppendLine("  - ``fcmRecordsNext_Core.js``;")
[void]$Builder.AppendLine("  - ``fcmRecordsNext_Classics.js``;")
[void]$Builder.AppendLine("  - ``fcmRecordsNext_Series.js``;")
[void]$Builder.AppendLine("  - ``fcmRecordsNext_RU.js``;")
[void]$Builder.AppendLine("  - ``fcmRecordsNext_Modifiers.js``;")
[void]$Builder.AppendLine("  - ``fcmRecordsNext_ThresholdsLuck.js``;")
[void]$Builder.AppendLine("  - ``fcmRecordsNext_Culometro.js``;")
[void]$Builder.AppendLine("  - ``fcmRecordsNext_Matches.js``;")
[void]$Builder.AppendLine("  - ``fcmRecordsNext_Manifest.js``;")
[void]$Builder.AppendLine("- ``fcmRecordsNext_Matches.js`` e' l'output canonico delle partite: una riga per squadra per ogni incontro reale, due righe speculari per partita;")
[void]$Builder.AppendLine("- esclusione dei turni di riposo da Matches;")
[void]$Builder.AppendLine("- esiti Matches standardizzati ``V/N/P``;")
[void]$Builder.AppendLine("- link ai tabellini storici corretti senza annidamento della stagione corrente;")
[void]$Builder.AppendLine("- Serie arricchite con stagione, competizione, giornata, match e tabellino;")
[void]$Builder.AppendLine("- Culometro arricchito per evento con frequenza storica evento, frequenza storica configurazione, chiave configurazione e impatto;")
[void]$Builder.AppendLine("- formula e classifiche del Culometro invariate rispetto alla 2.0;")
[void]$Builder.AppendLine("- GUI pubblica identificata come ``RecordsNext 2.1``;")
[void]$Builder.AppendLine("- manifest applicativo ``2.1.0``;")
[void]$Builder.AppendLine("- schema generale del manifest ancora ``2.0`` dove previsto dal contratto dati.")
[void]$Builder.AppendLine("")
[void]$Builder.AppendLine("### Bonifica output legacy - 2026-08-27")
[void]$Builder.AppendLine("")
[void]$Builder.AppendLine("- i file pubblici ``records2026.recordstagionali.classic.js``, ``records2026.recordstagionali.ru.js``, ``records2026.storico.ru.<stagione>.js`` e ``records2026.storico.ru.manifest.js`` non vengono piu' generati ne' pubblicati;")
[void]$Builder.AppendLine("- ``Records2026ClassicJsExporter`` e ``Records2026RuJsExporter`` restano nel codice come sorgenti interne di compatibilita' per i wrapper RecordsNext;")
[void]$Builder.AppendLine("- il frontend non cambia: nomi, globali e struttura degli output ``fcmRecordsNext_*`` restano invariati;")
[void]$Builder.AppendLine("- test su dati operativi reali: 9 output moderni validati, 0 output legacy;")
[void]$Builder.AppendLine("- ``fcmRecordsNext_Classics.js`` e ``fcmRecordsNext_RU.js`` verificati byte-per-byte invariati rispetto alla staging 2.1 precedente;")
[void]$Builder.AppendLine("- riduzione misurata: circa 28 MiB di output legacy eliminati per sito.")
[void]$Builder.AppendLine("")
[void]$Builder.AppendLine("### Verifiche finali 2.1")
[void]$Builder.AppendLine("")
[void]$Builder.AppendLine("- Maven: 41 test eseguiti, 0 failure, 0 errori dopo la pulizia finale del codice morto;")
[void]$Builder.AppendLine("- build/package 2.1 completato con successo;")
[void]$Builder.AppendLine("- regressione completa dello storico preesistente superata;")
[void]$Builder.AppendLine("- generazione reale del sito da FCM verificata con RecordsNext 2.1;")
[void]$Builder.AppendLine("- publisher bonificato compilato separatamente e verificato sui dati operativi 2026/2027.")
[void]$Builder.AppendLine("")
[void]$Builder.AppendLine("### Aggiornamento 2.0 -> 2.1")
[void]$Builder.AppendLine("")
[void]$Builder.AppendLine("E' disponibile un aggiornamento in-place che sostituisce programma e launcher e conserva configurazione, database, stagioni e associazioni storiche.")
[void]$Builder.AppendLine("")
[void]$Builder.AppendLine("### Nota sulla parte sottostante del documento")
[void]$Builder.AppendLine("")
[void]$Builder.AppendLine("Le sezioni successive includono documentazione e codice reale del progetto. Le indicazioni di stato storiche, se in conflitto con questa sezione, sono superate dallo stato consolidato sopra riportato.")
[void]$Builder.AppendLine("")

[void]$Builder.AppendLine("## Regole della bibbia")
[void]$Builder.AppendLine("")
[void]$Builder.AppendLine("- Le decisioni progettuali consolidate sono separate dal codice implementato.")
[void]$Builder.AppendLine("- Un file incluso non e automaticamente dichiarato funzionante.")
[void]$Builder.AppendLine("- Lo stato implementato deve essere aggiornato soltanto dopo test.")
[void]$Builder.AppendLine("- Le questioni ancora aperte non devono essere presentate come funzionalita.")
[void]$Builder.AppendLine("")

[void]$Builder.AppendLine("## Stato sintetico")
[void]$Builder.AppendLine("")
[void]$Builder.AppendLine("### Decisioni consolidate")
[void]$Builder.AppendLine("")
[void]$Builder.AppendLine("- Progetto separato in D:\DEV_APPS\RecordsNext2.0.")
[void]$Builder.AppendLine("- Stagione operativa corrente verificata al 27/08/2026: 2026_2027; sito corrente E:\fantacalcio\Lega2026.")
[void]$Builder.AppendLine("- Cinque famiglie: Classici, Serie, Riserve d'Ufficio, Modificatori, Soglie e Fortuna.")
[void]$Builder.AppendLine("- Fattore Campo incluso nei Modificatori.")
[void]$Builder.AppendLine("- Culometro opzionale e prodotto soltanto su richiesta.")
[void]$Builder.AppendLine("- Dipendenze gestite a livello di singolo figlio.")
[void]$Builder.AppendLine("- Associazioni canoniche per squadre e competizioni.")
[void]$Builder.AppendLine("- Link ai tabellini per i record riferiti a partite specifiche.")
[void]$Builder.AppendLine("- JS pubblici nella cartella js del sito.")
[void]$Builder.AppendLine("- Un solo HTML indice nella root del sito.")
[void]$Builder.AppendLine("- HTML statici senza dati incorporati: visualizzatori dei JS pubblici.")
[void]$Builder.AppendLine("- Viste HTML nella cartella RecordsNext e un solo indice nella root.")
[void]$Builder.AppendLine("- JS statici di rendering nella cartella js della skin: fcmRecordsNextFunzioni_common.js e fcmRecordsNextFunzioni_viewer.js.")
[void]$Builder.AppendLine("- Profili grafici iniziali: mauzstrom, fantablue2 e neutral.")
[void]$Builder.AppendLine("- Il profilo mauzstrom usa Trebuchet MS.")
[void]$Builder.AppendLine("- Nei nuovi nomi file RecordsNext si usa underscore, non trattino.")
[void]$Builder.AppendLine("")

[void]$Builder.AppendLine("### Implementato e verificato")
[void]$Builder.AppendLine("")
[void]$Builder.AppendLine("- Base funzionante RecordsNext 1.0.2 importata nella linea RecordsNext 2.x.")
[void]$Builder.AppendLine("- Accesso ai database FCM e FCA tramite UCanAccess.")
[void]$Builder.AppendLine("- Configurazione delle stagioni gestite e manuali.
- Eliminazione completa e transazionale delle stagioni verificata anche sul database operativo copiato: pulizia dei dati stagionali, riancoraggio delle identita canoniche e promozione automatica della nuova anchor.")
[void]$Builder.AppendLine("- Importazione, normalizzazione e consolidamento storico delle stagioni gestite.")
[void]$Builder.AppendLine("- Modello modulare con famiglie, figli, dipendenze, planner e preflight.")
[void]$Builder.AppendLine("- GUI RecordsNext 2.1 con configurazione granulare delle famiglie.")
[void]$Builder.AppendLine("- Configurazione gerarchica dei Modificatori per tipo e statistica.")
[void]$Builder.AppendLine("- Nomi configurabili per MODM1PERS, MODM2PERS e MODM3PERS.")
[void]$Builder.AppendLine("- Modificatori standard FCM distinti dai modificatori personalizzati.")
[void]$Builder.AppendLine("- Generazione diretta di fcmRecordsNext_Modifiers.js dagli archivi season_records.")
[void]$Builder.AppendLine("- Statistiche Massimo, Totale, Media e Utilizzi per i modificatori selezionati.")
[void]$Builder.AppendLine("- Esportazione verificata del MODDIFESA FCM della stagione 2006_2007.")
[void]$Builder.AppendLine("- Metadati availableSections e generatedSections distinti.")
[void]$Builder.AppendLine("- Test automatici: 44 eseguiti, 0 failure, 0 errori.")
[void]$Builder.AppendLine("- Gli output legacy records2026.* non vengono piu pubblicati; gli exporter legacy Classici/RU restano solo come sorgente interna di compatibilita.")
[void]$Builder.AppendLine("- Bonifica publisher verificata sui dati operativi: 9 output moderni, 0 output legacy; Classics e RU invariati byte-per-byte.")
[void]$Builder.AppendLine("- Verifica reale del JS Modificatori completata con tutte le sezioni selezionate presenti.")
[void]$Builder.AppendLine("")

[void]$Builder.AppendLine("### Questioni aperte")
[void]$Builder.AppendLine("")
[void]$Builder.AppendLine("- Le questioni ancora aperte sono mantenute in docs\DECISIONI_APERTE.md e non vengono duplicate nella sintesi della bibbia.")
[void]$Builder.AppendLine("")

Add-IndentedFileSection -Builder $Builder -Title "README" -RelativePath "README.md"
Add-IndentedFileSection -Builder $Builder -Title "Architettura" -RelativePath "docs\ARCHITETTURA_RECORDSNEXT2.md"
Add-IndentedFileSection -Builder $Builder -Title "Architettura visualizzatori HTML" -RelativePath "docs\ARCHITETTURA_VISUALIZZATORI_HTML.md"
Add-IndentedFileSection -Builder $Builder -Title "Installazione visualizzatori HTML" -RelativePath "docs\INSTALLAZIONE_VISUALIZZATORI_HTML.md"
Add-IndentedFileSection -Builder $Builder -Title "Personalizzazione visualizzatori HTML" -RelativePath "docs\PERSONALIZZAZIONE_VISUALIZZATORI_HTML.md"
Add-IndentedFileSection -Builder $Builder -Title "Catalogo record" -RelativePath "docs\CATALOGO_RECORD.md"
Add-IndentedFileSection -Builder $Builder -Title "Dipendenze output" -RelativePath "docs\DIPENDENZE_OUTPUT.md"
Add-IndentedFileSection -Builder $Builder -Title "Decisioni aperte" -RelativePath "docs\DECISIONI_APERTE.md"
Add-IndentedFileSection -Builder $Builder -Title "Modello dati" -RelativePath "docs\MODELLO_DATI_RECORDSNEXT2.md"
Add-IndentedFileSection -Builder $Builder -Title "Configurazione" -RelativePath "docs\CONFIGURAZIONE_RECORDSNEXT2.md"
Add-IndentedFileSection -Builder $Builder -Title "Stato implementazione" -RelativePath "docs\STATO_IMPLEMENTAZIONE_RECORDSNEXT2.md"
Add-IndentedFileSection -Builder $Builder -Title "Changelog" -RelativePath "CHANGELOG.md"

$SourceExtensions = @(
    ".java",
    ".ps1",
    ".json",
    ".js",
    ".html",
    ".css",
    ".xml",
    ".properties"
)

$SourceRoots = @(
    "src",
    "config",
    "tools"
)

[void]$Builder.AppendLine("## File reali del progetto")
[void]$Builder.AppendLine("")

$IncludedFiles = New-Object System.Collections.Generic.List[string]

foreach ($RelativeRoot in $SourceRoots) {
    $FullRoot = Join-Path $ProjectDir $RelativeRoot

    if (-not (Test-Path -LiteralPath $FullRoot)) {
        continue
    }

    $Files = Get-ChildItem -LiteralPath $FullRoot -File -Recurse |
        Where-Object { $SourceExtensions -contains $_.Extension.ToLowerInvariant() } |
        Sort-Object FullName

    foreach ($File in $Files) {
        $RelativePath = $File.FullName.Substring($ProjectDir.Length).TrimStart("\")
        [void]$IncludedFiles.Add($RelativePath)

        Add-IndentedFileSection `
            -Builder $Builder `
            -Title $RelativePath `
            -RelativePath $RelativePath
    }
}

[void]$Builder.AppendLine("## Indice dei file inclusi")
[void]$Builder.AppendLine("")

if ($IncludedFiles.Count -eq 0) {
    [void]$Builder.AppendLine("- Nessun file sorgente presente.")
}
else {
    foreach ($RelativePath in $IncludedFiles) {
        [void]$Builder.AppendLine("- " + $RelativePath)
    }
}

[void]$Builder.AppendLine("")
[void]$Builder.AppendLine("## Fine documento")

$Utf8NoBom = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllText(
    $OutputFile,
    $Builder.ToString(),
    $Utf8NoBom
)

Write-Host ""
Write-Host "Bibbia generata:" -ForegroundColor Green
Write-Host $OutputFile
Write-Host ""
Write-Host ("Dimensione: {0} byte" -f (Get-Item -LiteralPath $OutputFile).Length)
# Pulizia finale del Markdown generato:
# rimuove spazi e TAB a fine riga senza modificare il contenuto utile.
if (Test-Path -LiteralPath $OutputFile) {

    $Utf8NoBomCleanup = New-Object System.Text.UTF8Encoding($false)

    $MarkdownCleanup = [System.IO.File]::ReadAllText(
        $OutputFile
    )

    $MarkdownCleanup = [System.Text.RegularExpressions.Regex]::Replace(
        $MarkdownCleanup,
        "[ `t]+(?=`r?$)",
        "",
        [System.Text.RegularExpressions.RegexOptions]::Multiline
    )

    [System.IO.File]::WriteAllText(
        $OutputFile,
        $MarkdownCleanup,
        $Utf8NoBomCleanup
    )
}
