param(
    [string]$ProjectRoot = "D:\DEV_APPS\RecordsNext2.0",
    [string]$ReleaseVersion = "3.1.0",
    [string]$DownloadsDir = "D:\DEV_APPS\downloads",
    [string]$UCanAccessRoot = ""
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

if ([string]::IsNullOrWhiteSpace($UCanAccessRoot)) {
    throw "Specificare -UCanAccessRoot con la cartella runtime\ucanaccess di una installazione RecordsNext valida."
}

if ([string]::IsNullOrWhiteSpace($UCanAccessRoot)) {
    $maintainerRuntime = "E:\FCM\plugin\Mauz_strom2026\RecordsNext\runtime\ucanaccess"
    if (Test-Path -LiteralPath (Join-Path $maintainerRuntime "ucanaccess-2.0.9.5.jar")) {
        $UCanAccessRoot = $maintainerRuntime
    }
    else {
        throw "Specificare -UCanAccessRoot con la cartella runtime\ucanaccess di una installazione RecordsNext valida."
    }
}

$releaseName = "RecordsNext_${ReleaseVersion}_FULL"
$releaseDir = Join-Path $ProjectRoot ("release\" + $releaseName)
$payloadDir = Join-Path $releaseDir "payload"
$zipPath = Join-Path $DownloadsDir ($releaseName + ".zip")
$shaPath = Join-Path $DownloadsDir ($releaseName + "_SHA256.txt")

$requiredFiles = @(
    "target\RecordsNext.jar",
    "RecordsNext.bat",
    "README.md",
    "INSTALL.txt",
    "CHANGELOG.md",
    "tools\Installa-RecordsNext-3.1.ps1",
    "tools\INSTALLA_RECORDSNEXT.bat",
    "config\competitions.json",
    "config\teams.json",
    "config\culometro.json",
    "config\manifest.example.json",
    "data\calendars\DataA-2026.js",
    "release\visualizzatori\recordsnext.html",
    "release\visualizzatori\js\fcmRecordsNextFunzioni_common.js",
    "release\visualizzatori\js\fcmRecordsNextFunzioni_viewer.js",
    "docs\INSTALLAZIONE_VISUALIZZATORI_HTML.md",
    "docs\CULOMETRO.md",
    "tools\Install-RecordsNextVisualizzatori_v2.ps1"
)

foreach ($relative in $requiredFiles) {
    $file = Join-Path $ProjectRoot $relative
    if (-not (Test-Path -LiteralPath $file -PathType Leaf)) {
        throw "File richiesto mancante: $file"
    }
}

if (-not (Test-Path -LiteralPath (Join-Path $UCanAccessRoot "ucanaccess-2.0.9.5.jar"))) {
    throw "Runtime UCanAccess non trovato: $UCanAccessRoot"
}

Remove-Item -LiteralPath $releaseDir -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Path $payloadDir -Force | Out-Null

Copy-Item (Join-Path $ProjectRoot "tools\Installa-RecordsNext-3.1.ps1") $releaseDir -Force
Copy-Item (Join-Path $ProjectRoot "tools\INSTALLA_RECORDSNEXT.bat") $releaseDir -Force

$readmeInstaller = @"
RecordsNext $ReleaseVersion - INSTALLAZIONE

1. Estrarre completamente lo ZIP.
2. Eseguire INSTALLA_RECORDSNEXT.bat.
3. Indicare la cartella di installazione quando richiesto.
4. L'installer verifica Java 21+, runtime e integrita minima del payload.

L'installazione NON pubblica file nei siti FCM.
L'installazione NON contiene database, FCM/FCA o configurazioni personali.
"@
[IO.File]::WriteAllText(
    (Join-Path $releaseDir "LEGGIMI.txt"),
    $readmeInstaller,
    (New-Object System.Text.UTF8Encoding($false))
)

@("RecordsNext.bat","README.md","INSTALL.txt","CHANGELOG.md") | ForEach-Object {
    Copy-Item (Join-Path $ProjectRoot $_) $payloadDir -Force
}
Copy-Item (Join-Path $ProjectRoot "target\RecordsNext.jar") $payloadDir -Force

$runtimeDst = Join-Path $payloadDir "runtime\ucanaccess"
New-Item -ItemType Directory -Path $runtimeDst -Force | Out-Null
Copy-Item (Join-Path $UCanAccessRoot "*") $runtimeDst -Recurse -Force
Remove-Item (Join-Path $runtimeDst "console.bat") -Force -ErrorAction SilentlyContinue
Remove-Item (Join-Path $runtimeDst "console.sh") -Force -ErrorAction SilentlyContinue

$configDst = Join-Path $payloadDir "config"
New-Item -ItemType Directory -Path $configDst -Force | Out-Null
@("competitions.json","teams.json","culometro.json","manifest.example.json") | ForEach-Object {
    Copy-Item (Join-Path $ProjectRoot ("config\" + $_)) $configDst -Force
}

# Archivio calendari canonici
$calendarSrc = Join-Path $ProjectRoot "data\calendars"
$calendarDst = Join-Path $payloadDir "data\calendars"
New-Item -ItemType Directory -Path $calendarDst -Force | Out-Null
Get-ChildItem -LiteralPath $calendarSrc -File -Filter "DataA-*.js" | ForEach-Object {
    Copy-Item -LiteralPath $_.FullName -Destination $calendarDst -Force
}

$visSrc = Join-Path $ProjectRoot "release\visualizzatori"
$visDst = Join-Path $payloadDir "visualizzatori"
New-Item -ItemType Directory -Path $visDst -Force | Out-Null
Copy-Item (Join-Path $visSrc "recordsnext.html") $visDst -Force
Copy-Item (Join-Path $visSrc "RecordsNext") $visDst -Recurse -Force
Copy-Item (Join-Path $visSrc "profiles") $visDst -Recurse -Force
New-Item -ItemType Directory -Path (Join-Path $visDst "js") -Force | Out-Null
Copy-Item (Join-Path $visSrc "js\fcmRecordsNextFunzioni_common.js") (Join-Path $visDst "js") -Force
Copy-Item (Join-Path $visSrc "js\fcmRecordsNextFunzioni_viewer.js") (Join-Path $visDst "js") -Force

$docsDst = Join-Path $payloadDir "docs"
New-Item -ItemType Directory -Path $docsDst -Force | Out-Null
Copy-Item (Join-Path $ProjectRoot "docs\INSTALLAZIONE_VISUALIZZATORI_HTML.md") $docsDst -Force
Copy-Item (Join-Path $ProjectRoot "docs\CULOMETRO.md") $docsDst -Force
if (Test-Path -LiteralPath (Join-Path $ProjectRoot "docs\screenshots")) {
    Copy-Item (Join-Path $ProjectRoot "docs\screenshots") $docsDst -Recurse -Force
}

$toolsDst = Join-Path $payloadDir "tools"
New-Item -ItemType Directory -Path $toolsDst -Force | Out-Null
Copy-Item (Join-Path $ProjectRoot "tools\Install-RecordsNextVisualizzatori_v2.ps1") `
    (Join-Path $toolsDst "Install_RecordsNextVisualizzatori.ps1") -Force

$forbiddenNames = @(
    "recordsnext.db",
    "recordsnext-gui.properties",
    "league.json",
    "seasons.json",
    "processing.json"
)

$bad = Get-ChildItem $releaseDir -Recurse -File | Where-Object {
    $forbiddenNames -contains $_.Name -or
    $_.Extension -in @(".fcm", ".fca") -or
    $_.Name -like "fcmRecordsNext_Core.js" -or
    $_.Name -like "fcmRecordsNext_Manifest.js" -or
    $_.Name -like "fcmRecordsNext_Classics.js" -or
    $_.Name -like "fcmRecordsNext_Series.js" -or
    $_.Name -like "fcmRecordsNext_RU.js" -or
    $_.Name -like "fcmRecordsNext_Modifiers.js" -or
    $_.Name -like "fcmRecordsNext_ThresholdsLuck.js" -or
    $_.Name -like "fcmRecordsNext_Culometro.js" -or
    $_.Name -like "fcmRecordsNext_Matches.js"
}

if ($bad) {
    $bad | ForEach-Object { Write-Host $_.FullName }
    throw "Release FULL non pulita."
}

New-Item -ItemType Directory -Path $DownloadsDir -Force | Out-Null
Remove-Item -LiteralPath $zipPath -Force -ErrorAction SilentlyContinue
Remove-Item -LiteralPath $shaPath -Force -ErrorAction SilentlyContinue

Compress-Archive -Path $releaseDir -DestinationPath $zipPath -CompressionLevel Optimal -Force
$hash = (Get-FileHash -LiteralPath $zipPath -Algorithm SHA256).Hash
Set-Content -LiteralPath $shaPath `
    -Value ("SHA256  " + $hash + "  " + [IO.Path]::GetFileName($zipPath)) `
    -Encoding ASCII

Write-Host ""
Write-Host "FULL 3.1 creato:"
Write-Host "  $zipPath"
Write-Host "SHA256:"
Write-Host "  $hash"
