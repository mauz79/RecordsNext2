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

[void]$Builder.AppendLine("# Codice funzionante RecordsNext 2.0")
[void]$Builder.AppendLine("")
[void]$Builder.AppendLine("> Documento generato automaticamente.")
[void]$Builder.AppendLine("> Data generazione: " + $GeneratedAt)
[void]$Builder.AppendLine("> Directory progetto: " + $ProjectDir)
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
[void]$Builder.AppendLine("- Base funzionante RecordsNext 1.0.2 importata nel progetto 2.0.")
[void]$Builder.AppendLine("- Accesso ai database FCM e FCA tramite UCanAccess.")
[void]$Builder.AppendLine("- Configurazione delle stagioni gestite e manuali.")
[void]$Builder.AppendLine("- Importazione, normalizzazione e consolidamento storico delle stagioni gestite.")
[void]$Builder.AppendLine("- Modello modulare con famiglie, figli, dipendenze, planner e preflight.")
[void]$Builder.AppendLine("- GUI RecordsNext 2.0 con configurazione granulare delle famiglie.")
[void]$Builder.AppendLine("- Configurazione gerarchica dei Modificatori per tipo e statistica.")
[void]$Builder.AppendLine("- Nomi configurabili per MODM1PERS, MODM2PERS e MODM3PERS.")
[void]$Builder.AppendLine("- Modificatori standard FCM distinti dai modificatori personalizzati.")
[void]$Builder.AppendLine("- Generazione diretta di fcmRecordsNext_Modifiers.js dagli archivi season_records.")
[void]$Builder.AppendLine("- Statistiche Massimo, Totale, Media e Utilizzi per i modificatori selezionati.")
[void]$Builder.AppendLine("- Esportazione verificata del MODDIFESA FCM della stagione 2006_2007.")
[void]$Builder.AppendLine("- Metadati availableSections e generatedSections distinti.")
[void]$Builder.AppendLine("- Test automatici: 38 eseguiti, 0 failure, 0 errori.")
[void]$Builder.AppendLine("- Verifica reale del JS Modificatori completata con tutte le sezioni selezionate presenti.")
[void]$Builder.AppendLine("")

[void]$Builder.AppendLine("### Non ancora implementato o da completare")
[void]$Builder.AppendLine("")
[void]$Builder.AppendLine("- Elaboratore nativo completo della famiglia Serie.")
[void]$Builder.AppendLine("- Elaboratore nativo completo della famiglia Soglie e Fortuna.")
[void]$Builder.AppendLine("- Culometro definitivo e relativo contratto dati pubblico.")
[void]$Builder.AppendLine("- Contratto JavaScript pubblico definitivo di tutte le famiglie.")
[void]$Builder.AppendLine("- Visualizzatori HTML 2.0 definitivi.")
[void]$Builder.AppendLine("- JS statici definitivi dei visualizzatori.")
[void]$Builder.AppendLine("- Installer definitivo dei visualizzatori e dei profili CSS.")
[void]$Builder.AppendLine("")

[void]$Builder.AppendLine("### Non ancora implementato")
[void]$Builder.AppendLine("")
[void]$Builder.AppendLine("- Lettura FCM e FCA.")
[void]$Builder.AppendLine("- Modello dati.")
[void]$Builder.AppendLine("- Elaboratori delle famiglie.")
[void]$Builder.AppendLine("- Esportatori JS.")
[void]$Builder.AppendLine("- Consolidamento GUI 2.0.")
[void]$Builder.AppendLine("- Installer.")
[void]$Builder.AppendLine("- Viste HTML 2.0.")
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