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
[void]$Builder.AppendLine("- Viste HTML dimostrative nella cartella RecordsNext.")
[void]$Builder.AppendLine("")

[void]$Builder.AppendLine("### Implementato e verificato")
[void]$Builder.AppendLine("")
[void]$Builder.AppendLine("- Struttura iniziale del progetto.")
[void]$Builder.AppendLine("- Documentazione architetturale iniziale.")
[void]$Builder.AppendLine("- Generatore della bibbia aggiornato con documentazione e sorgenti reali.")
[void]$Builder.AppendLine("")

[void]$Builder.AppendLine("### Non ancora implementato")
[void]$Builder.AppendLine("")
[void]$Builder.AppendLine("- Lettura FCM e FCA.")
[void]$Builder.AppendLine("- Modello dati.")
[void]$Builder.AppendLine("- Elaboratori delle famiglie.")
[void]$Builder.AppendLine("- Esportatori JS.")
[void]$Builder.AppendLine("- GUI.")
[void]$Builder.AppendLine("- Installer.")
[void]$Builder.AppendLine("- Viste HTML 2.0.")
[void]$Builder.AppendLine("")

Add-IndentedFileSection -Builder $Builder -Title "README" -RelativePath "README.md"
Add-IndentedFileSection -Builder $Builder -Title "Architettura" -RelativePath "docs\ARCHITETTURA_RECORDSNEXT2.md"
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
