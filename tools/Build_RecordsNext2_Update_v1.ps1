param(
    [string]$ProjectRoot = "D:\DEV_APPS\RecordsNext2.0",
    [string]$ReleaseVersion = "2.1.0",
    [string]$DownloadsDir = "D:\DEV_APPS\downloads"
)

$ErrorActionPreference = "Stop"

$updateName = "RecordsNext_${ReleaseVersion}_UPDATE"
$distRoot = Join-Path $ProjectRoot "dist"
$updateDir = Join-Path $distRoot $updateName
$payloadDir = Join-Path $updateDir "payload"

$zipPath = Join-Path $DownloadsDir ($updateName + ".zip")
$shaPath = Join-Path $DownloadsDir ($updateName + "_SHA256.txt")

Write-Host ""
Write-Host "=== RecordsNext $ReleaseVersion - Build UPDATE ==="
Write-Host ""

$required = @(
    (Join-Path $ProjectRoot "target\RecordsNext.jar"),
    (Join-Path $ProjectRoot "RecordsNext.bat"),
    (Join-Path $ProjectRoot "tools\Aggiorna-RecordsNext-2.1.ps1"),
    (Join-Path $ProjectRoot "tools\Aggiorna-RecordsNext-2.1.bat")
)

foreach ($file in $required) {
    if (-not (Test-Path -LiteralPath $file -PathType Leaf)) {
        throw "File richiesto mancante: $file"
    }
}

Remove-Item -LiteralPath $updateDir -Recurse -Force -ErrorAction SilentlyContinue

New-Item -ItemType Directory -Path $payloadDir -Force | Out-Null

Copy-Item `
    (Join-Path $ProjectRoot "target\RecordsNext.jar") `
    $payloadDir `
    -Force

Copy-Item `
    (Join-Path $ProjectRoot "RecordsNext.bat") `
    $payloadDir `
    -Force

Copy-Item `
    (Join-Path $ProjectRoot "tools\Aggiorna-RecordsNext-2.1.ps1") `
    (Join-Path $updateDir "Aggiorna-RecordsNext-2.1.ps1") `
    -Force

Copy-Item `
    (Join-Path $ProjectRoot "tools\Aggiorna-RecordsNext-2.1.bat") `
    (Join-Path $updateDir "Aggiorna-RecordsNext-2.1.bat") `
    -Force

$readme = @"
RecordsNext $ReleaseVersion - AGGIORNAMENTO DA 2.0

1. Chiudere RecordsNext.
2. Estrarre completamente questa cartella.
3. Eseguire Aggiorna-RecordsNext-2.1.bat.
4. Se l'installazione non viene trovata automaticamente,
   indicare la cartella della propria installazione RecordsNext 2.0.

L'aggiornamento sostituisce soltanto il programma.

NON vengono cancellati o sostituiti:
- configurazione della lega;
- stagioni configurate;
- associazioni storiche;
- database RecordsNext;
- dati e archivi esistenti.

Prima della sostituzione viene creato automaticamente un backup
del programma precedente, della configurazione e del database.
"@

[IO.File]::WriteAllText(
    (Join-Path $updateDir "LEGGIMI-AGGIORNAMENTO.txt"),
    $readme,
    (New-Object System.Text.UTF8Encoding($false))
)

# Sicurezza assoluta: nell'UPDATE non devono esistere dati/config utente.
$forbidden = Get-ChildItem $updateDir -Recurse -File |
    Where-Object {
        $_.Name -eq "recordsnext.db" -or
        $_.Name -eq "league.json" -or
        $_.Name -eq "seasons.json" -or
        $_.Name -eq "recordsnext-gui.properties" -or
        $_.Name -eq "processing.json" -or
        $_.FullName -match '\\data\\' -or
        $_.FullName -match '\\config\\'
    }

if ($forbidden) {
    Write-Host ""
    Write-Host "ERRORE: file vietati trovati:"
    $forbidden | ForEach-Object { Write-Host $_.FullName }
    throw "UPDATE non sicuro."
}

Write-Host "=== CONTENUTO UPDATE ==="

Get-ChildItem $updateDir -Recurse -File |
    Sort-Object FullName |
    ForEach-Object {
        $_.FullName.Substring($updateDir.Length + 1)
    }

New-Item -ItemType Directory -Path $DownloadsDir -Force | Out-Null

Remove-Item $zipPath -Force -ErrorAction SilentlyContinue
Remove-Item $shaPath -Force -ErrorAction SilentlyContinue

Compress-Archive `
    -Path $updateDir `
    -DestinationPath $zipPath `
    -CompressionLevel Optimal `
    -Force

$hash = (Get-FileHash $zipPath -Algorithm SHA256).Hash

Set-Content `
    -LiteralPath $shaPath `
    -Value ("SHA256  " + $hash + "  " + [IO.Path]::GetFileName($zipPath)) `
    -Encoding ASCII

Write-Host ""
Write-Host "=== UPDATE CREATO ==="
Write-Host "ZIP    : $zipPath"
Write-Host "SHA256 : $hash"