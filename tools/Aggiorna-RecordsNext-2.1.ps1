param(
    [string]$InstallDir = ""
)

$ErrorActionPreference = "Stop"

Write-Host ""
Write-Host "========================================="
Write-Host " RecordsNext 2.0 -> 2.1"
Write-Host "========================================="
Write-Host ""

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$payload = Join-Path $scriptDir "payload"

if ([string]::IsNullOrWhiteSpace($InstallDir)) {
    $candidates = @(
        "E:\FCM\plugin\Mauz_strom2014Full\RecordsNext2",
        "E:\FCM\plugin\Mauz_strom2014Full\RecordsNext-2.0.0",
        "E:\FCM\plugin\Mauz_strom2014Full\RecordsNext"
    )

    $InstallDir = $candidates |
        Where-Object {
            Test-Path (Join-Path $_ "RecordsNext.jar")
        } |
        Select-Object -First 1
}

if ([string]::IsNullOrWhiteSpace($InstallDir)) {
    Write-Host "Installazione RecordsNext non trovata automaticamente."
    Write-Host ""
    $InstallDir = Read-Host "Inserisci il percorso della cartella RecordsNext 2.0"
}

$InstallDir = [IO.Path]::GetFullPath($InstallDir)

if (-not (Test-Path (Join-Path $InstallDir "RecordsNext.jar"))) {
    throw "RecordsNext.jar non trovato in: $InstallDir"
}

if (-not (Test-Path (Join-Path $payload "RecordsNext.jar"))) {
    throw "Payload non valido: RecordsNext.jar mancante."
}

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$backupDir = Join-Path $InstallDir ("backup_update_2.0_to_2.1_" + $timestamp)

Write-Host "Installazione: $InstallDir"
Write-Host "Backup       : $backupDir"
Write-Host ""

New-Item -ItemType Directory -Path $backupDir -Force | Out-Null

# Backup del vecchio programma.
Copy-Item `
    (Join-Path $InstallDir "RecordsNext.jar") `
    $backupDir `
    -Force

if (Test-Path (Join-Path $InstallDir "RecordsNext.bat")) {
    Copy-Item `
        (Join-Path $InstallDir "RecordsNext.bat") `
        $backupDir `
        -Force
}

# Backup dei dati NON rigenerabili.
if (Test-Path (Join-Path $InstallDir "config")) {
    Copy-Item `
        (Join-Path $InstallDir "config") `
        $backupDir `
        -Recurse `
        -Force
}

$db = Join-Path $InstallDir "data\database\recordsnext.db"

if (Test-Path $db) {
    $dbBackup = Join-Path $backupDir "data\database"
    New-Item -ItemType Directory -Path $dbBackup -Force | Out-Null
    Copy-Item $db $dbBackup -Force
}

Write-Host "Backup completato."
Write-Host ""

# Aggiornamento applicazione.
Copy-Item `
    (Join-Path $payload "RecordsNext.jar") `
    (Join-Path $InstallDir "RecordsNext.jar") `
    -Force

if (Test-Path (Join-Path $payload "RecordsNext.bat")) {
    Copy-Item `
        (Join-Path $payload "RecordsNext.bat") `
        (Join-Path $InstallDir "RecordsNext.bat") `
        -Force
}

Write-Host "RecordsNext aggiornato alla versione 2.1."
Write-Host ""
Write-Host "NON sono stati modificati:"
Write-Host "  config\"
Write-Host "  data\"
Write-Host "  associazioni storiche"
Write-Host "  configurazione delle stagioni"
Write-Host ""
Write-Host "Backup disponibile in:"
Write-Host "  $backupDir"
Write-Host ""
Write-Host "Aggiornamento completato."