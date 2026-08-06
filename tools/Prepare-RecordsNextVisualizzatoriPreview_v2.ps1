param(
    [string]$ProjectDir = "D:\DEV_APPS\RecordsNext2.0"
)

$ErrorActionPreference = "Stop"

$stagingRoot = Join-Path $ProjectDir "data\site-export-staging"
$viewerRoot = Join-Path $ProjectDir "release\visualizzatori"
$viewerJs = Join-Path $viewerRoot "js"

if (-not (Test-Path -LiteralPath $stagingRoot)) {
    throw "Cartella staging non trovata: $stagingRoot"
}

$latest = Get-ChildItem -LiteralPath $stagingRoot -Directory |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1

if ($null -eq $latest) {
    throw "Nessuna elaborazione presente nello staging."
}

$sourceJs = Join-Path $latest.FullName "js"
if (-not (Test-Path -LiteralPath $sourceJs)) {
    throw "Cartella JS non trovata nell'ultima elaborazione: $sourceJs"
}

New-Item -ItemType Directory -Path $viewerJs -Force | Out-Null

Get-ChildItem -LiteralPath $sourceJs -File -Filter "fcmRecordsNext_*.js" |
    Copy-Item -Destination $viewerJs -Force

Write-Host "Preview preparata dall'elaborazione:" -ForegroundColor Green
Write-Host $latest.FullName
Write-Host ""
Write-Host "Apri nel browser:" -ForegroundColor Cyan
Write-Host (Join-Path $viewerRoot "recordsnext.html")
