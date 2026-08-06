$ErrorActionPreference = "Stop"

$projectRoot = "D:\DEV_APPS\RecordsNext2.0"
$siteJsRoot  = "E:\fantacalcio\Lega2025\js"
$zipPath     = "D:\DEV_APPS\downloads\RecordsNext2_JS_REALI.zip"

$files = @()

if (Test-Path -LiteralPath $projectRoot) {
    $files += Get-ChildItem `
        -LiteralPath $projectRoot `
        -Recurse `
        -File `
        -ErrorAction SilentlyContinue |
        Where-Object {
            $_.Name -like "fcmRecordsNext*.js"
        } |
        ForEach-Object {
            $_.FullName
        }
}

if (Test-Path -LiteralPath $siteJsRoot) {
    $files += Get-ChildItem `
        -LiteralPath $siteJsRoot `
        -File `
        -ErrorAction SilentlyContinue |
        Where-Object {
            $_.Name -like "fcmRecordsNext*.js"
        } |
        ForEach-Object {
            $_.FullName
        }
}

$files = @(
    $files |
        Where-Object {
            -not [string]::IsNullOrWhiteSpace($_)
        } |
        Sort-Object -Unique
)

Write-Host ""
Write-Host "File JS trovati: $($files.Count)" -ForegroundColor Cyan

if ($files.Count -eq 0) {
    Write-Host ""
    Write-Host "Nessun file fcmRecordsNext*.js trovato." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Percorsi controllati:"
    Write-Host "  $projectRoot"
    Write-Host "  $siteJsRoot"
    Write-Host ""
    Write-Host "Lo ZIP non e stato creato."
    exit 1
}

Write-Host ""

$files |
    ForEach-Object {
        Write-Host $_
    }

if (Test-Path -LiteralPath $zipPath) {
    Remove-Item -LiteralPath $zipPath -Force
}

Compress-Archive `
    -LiteralPath $files `
    -DestinationPath $zipPath `
    -Force

Write-Host ""
Write-Host "ZIP creato correttamente:" -ForegroundColor Green
Write-Host $zipPath

$hash = Get-FileHash `
    -LiteralPath $zipPath `
    -Algorithm SHA256

Write-Host ""
Write-Host "SHA256:"
Write-Host $hash.Hash