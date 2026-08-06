param(
    [string]$ProjectDir = "D:\DEV_APPS\RecordsNext2.0",
    [string]$JsDir = "",
    [string]$OutputDir = ""
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($JsDir)) {
    $stagingRoot = Join-Path $ProjectDir "data\site-export-staging"
    $latest = Get-ChildItem -LiteralPath $stagingRoot -Directory |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1

    if ($null -eq $latest) {
        throw "Nessuna elaborazione trovata in $stagingRoot"
    }

    $JsDir = Join-Path $latest.FullName "js"
}

if (-not (Test-Path -LiteralPath $JsDir)) {
    throw "Cartella JS non trovata: $JsDir"
}

if ([string]::IsNullOrWhiteSpace($OutputDir)) {
    $OutputDir = Join-Path $ProjectDir "reports\js-audit"
}

New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null

$nodeScript = Join-Path $ProjectDir "tools\Audit-RecordsNext2Js.js"
if (-not (Test-Path -LiteralPath $nodeScript)) {
    throw "Script Node non trovato: $nodeScript"
}

node $nodeScript $JsDir $OutputDir
if ($LASTEXITCODE -ne 0) {
    throw "Audit JS terminato con errore."
}

Write-Host ""
Write-Host "Audit completato:" -ForegroundColor Green
Write-Host (Join-Path $OutputDir "RecordsNext2_JS_AUDIT.md")
Write-Host (Join-Path $OutputDir "RecordsNext2_JS_AUDIT.csv")
Write-Host (Join-Path $OutputDir "RecordsNext2_JS_AUDIT.json")
