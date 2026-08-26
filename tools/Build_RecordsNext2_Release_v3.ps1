param(
    [string]$ProjectRoot = "D:\DEV_APPS\RecordsNext2.0",
    [string]$ReleaseVersion = "2.1.0",
    [string]$DownloadsDir = "D:\DEV_APPS\downloads",
    [string]$UCanAccessRoot = "D:\DEV_APPS\RecordsNext\tools\ucanaccess\2.0.9.5\UCanAccess-2.0.9.5-bin"
)

$ErrorActionPreference = "Stop"

$releaseName = "RecordsNext_$ReleaseVersion"
$distRoot = Join-Path $ProjectRoot "dist"
$releaseDir = Join-Path $distRoot $releaseName
$zipPath = Join-Path $DownloadsDir ($releaseName + ".zip")
$shaPath = Join-Path $DownloadsDir ($releaseName + "_SHA256.txt")

Write-Host ""
Write-Host "=== RecordsNext $ReleaseVersion - Build Release ==="
Write-Host "ProjectRoot    : $ProjectRoot"
Write-Host "ReleaseDir     : $releaseDir"
Write-Host "Zip            : $zipPath"
Write-Host ""

# 1. Verifiche preliminari
$requiredFiles = @(
    (Join-Path $ProjectRoot "target\RecordsNext.jar"),
    (Join-Path $ProjectRoot "RecordsNext.bat"),
    (Join-Path $ProjectRoot "README.md"),
    (Join-Path $ProjectRoot "INSTALL.txt"),
    (Join-Path $ProjectRoot "CHANGELOG.md"),
    (Join-Path $ProjectRoot "config\competitions.json"),
    (Join-Path $ProjectRoot "config\teams.json"),
    (Join-Path $ProjectRoot "config\culometro.json"),
    (Join-Path $ProjectRoot "config\manifest.example.json"),
    (Join-Path $ProjectRoot "release\visualizzatori\recordsnext.html"),
    (Join-Path $ProjectRoot "release\visualizzatori\js\fcmRecordsNextFunzioni_common.js"),
    (Join-Path $ProjectRoot "release\visualizzatori\js\fcmRecordsNextFunzioni_viewer.js"),
    (Join-Path $ProjectRoot "docs\INSTALLAZIONE_VISUALIZZATORI_HTML.md"),
    (Join-Path $ProjectRoot "docs\CULOMETRO.md"),
    (Join-Path $ProjectRoot "tools\Install-RecordsNextVisualizzatori_v2.ps1"),
    (Join-Path $UCanAccessRoot "ucanaccess-2.0.9.5.jar")
)

foreach ($file in $requiredFiles) {
    if (-not (Test-Path -LiteralPath $file -PathType Leaf)) {
        throw "File richiesto mancante: $file"
    }
}

if (-not (Test-Path -LiteralPath (Join-Path $ProjectRoot "docs\screenshots") -PathType Container)) {
    throw "Cartella screenshots mancante: $(Join-Path $ProjectRoot 'docs\screenshots')"
}

if (-not (Test-Path -LiteralPath (Join-Path $ProjectRoot "release\visualizzatori\RecordsNext") -PathType Container)) {
    throw "Cartella visualizzatori RecordsNext mancante."
}

if (-not (Test-Path -LiteralPath (Join-Path $ProjectRoot "release\visualizzatori\profiles") -PathType Container)) {
    throw "Cartella profili visualizzatori mancante."
}

# 2. Pulizia staging
Remove-Item -LiteralPath $releaseDir -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Path $releaseDir -Force | Out-Null

# 3. File principali
Copy-Item (Join-Path $ProjectRoot "target\RecordsNext.jar") $releaseDir -Force
Copy-Item (Join-Path $ProjectRoot "RecordsNext.bat") $releaseDir -Force
Copy-Item (Join-Path $ProjectRoot "README.md") $releaseDir -Force
Copy-Item (Join-Path $ProjectRoot "INSTALL.txt") $releaseDir -Force
Copy-Item (Join-Path $ProjectRoot "CHANGELOG.md") $releaseDir -Force

# 4. Runtime UCanAccess completo
$runtimeUcan = Join-Path $releaseDir "runtime\ucanaccess"
New-Item -ItemType Directory -Path $runtimeUcan -Force | Out-Null
Copy-Item (Join-Path $UCanAccessRoot "*") $runtimeUcan -Recurse -Force

Remove-Item (Join-Path $runtimeUcan "console.bat") -Force -ErrorAction SilentlyContinue
Remove-Item (Join-Path $runtimeUcan "console.sh") -Force -ErrorAction SilentlyContinue

# 5. Config neutra
$configDir = Join-Path $releaseDir "config"
New-Item -ItemType Directory -Path $configDir -Force | Out-Null

$neutralConfig = @(
    "competitions.json",
    "teams.json",
    "culometro.json",
    "manifest.example.json"
)

foreach ($name in $neutralConfig) {
    Copy-Item (Join-Path $ProjectRoot ("config\" + $name)) $configDir -Force
}

# NON distribuire configurazioni/dati personali
$forbiddenConfig = @(
    "league.json",
    "seasons.json",
    "recordsnext-gui.properties",
    "processing.json"
)

foreach ($name in $forbiddenConfig) {
    Remove-Item (Join-Path $configDir $name) -Force -ErrorAction SilentlyContinue
}

# 6. Visualizzatori statici
$visSrc = Join-Path $ProjectRoot "release\visualizzatori"
$visDst = Join-Path $releaseDir "visualizzatori"

New-Item -ItemType Directory -Path $visDst -Force | Out-Null
Copy-Item (Join-Path $visSrc "recordsnext.html") $visDst -Force
Copy-Item (Join-Path $visSrc "RecordsNext") $visDst -Recurse -Force
Copy-Item (Join-Path $visSrc "profiles") $visDst -Recurse -Force

$visJsDst = Join-Path $visDst "js"
New-Item -ItemType Directory -Path $visJsDst -Force | Out-Null
Copy-Item (Join-Path $visSrc "js\fcmRecordsNextFunzioni_common.js") $visJsDst -Force
Copy-Item (Join-Path $visSrc "js\fcmRecordsNextFunzioni_viewer.js") $visJsDst -Force

# Sicurezza: nessun JS dati della lega / nessun backup
Get-ChildItem $visJsDst -File -ErrorAction SilentlyContinue |
    Where-Object {
        $_.Name -like "fcmRecordsNext_Core.js" -or
        $_.Name -like "fcmRecordsNext_Manifest.js" -or
        $_.Name -like "fcmRecordsNext_Classics.js" -or
        $_.Name -like "fcmRecordsNext_Series.js" -or
        $_.Name -like "fcmRecordsNext_RU.js" -or
        $_.Name -like "fcmRecordsNext_Modifiers.js" -or
        $_.Name -like "fcmRecordsNext_ThresholdsLuck.js" -or
        $_.Name -like "fcmRecordsNext_Culometro.js" -or
        $_.Name -like "fcmRecordsNext_Matches.js" -or
        $_.Name -like "*BACKUP*"
    } |
    Remove-Item -Force

# 7. Documentazione
$docsDst = Join-Path $releaseDir "docs"
New-Item -ItemType Directory -Path $docsDst -Force | Out-Null

Copy-Item `
    (Join-Path $ProjectRoot "docs\INSTALLAZIONE_VISUALIZZATORI_HTML.md") `
    $docsDst `
    -Force

Copy-Item `
    (Join-Path $ProjectRoot "docs\CULOMETRO.md") `
    $docsDst `
    -Force

Copy-Item `
    (Join-Path $ProjectRoot "docs\screenshots") `
    $docsDst `
    -Recurse `
    -Force

# 8. Tool utente finale
$toolsDst = Join-Path $releaseDir "tools"
New-Item -ItemType Directory -Path $toolsDst -Force | Out-Null

Copy-Item `
    (Join-Path $ProjectRoot "tools\Install-RecordsNextVisualizzatori_v2.ps1") `
    (Join-Path $toolsDst "Install_RecordsNextVisualizzatori.ps1") `
    -Force

# 9. Controllo file vietati
$forbiddenPatterns = @(
    "*BACKUP*",
    "*.fcm",
    "*.fca",
    "recordsnext.db",
    "recordsnext-gui.properties",
    "league.json",
    "seasons.json",
    "processing.json",
    "fcmRecordsNext_Core.js",
    "fcmRecordsNext_Manifest.js",
    "fcmRecordsNext_Classics.js",
    "fcmRecordsNext_Series.js",
    "fcmRecordsNext_RU.js",
    "fcmRecordsNext_Modifiers.js",
    "fcmRecordsNext_ThresholdsLuck.js",
    "fcmRecordsNext_Culometro.js",
    "fcmRecordsNext_Matches.js"
)

$bad = @()
foreach ($pattern in $forbiddenPatterns) {
    $bad += Get-ChildItem $releaseDir -Recurse -File -Filter $pattern -ErrorAction SilentlyContinue
}

$bad = $bad | Sort-Object FullName -Unique

if ($bad.Count -gt 0) {
    Write-Host ""
    Write-Host "ERRORE: file vietati trovati nella release:"
    $bad | ForEach-Object { Write-Host $_.FullName }
    throw "Release non pulita."
}

# 10. Elenco contenuto
Write-Host ""
Write-Host "=== CONTENUTO RELEASE ==="
Get-ChildItem $releaseDir -Recurse -File |
    Sort-Object FullName |
    ForEach-Object {
        $_.FullName.Substring($releaseDir.Length + 1)
    }

# 11. ZIP finale
New-Item -ItemType Directory -Path $DownloadsDir -Force | Out-Null
Remove-Item -LiteralPath $zipPath -Force -ErrorAction SilentlyContinue
Remove-Item -LiteralPath $shaPath -Force -ErrorAction SilentlyContinue

Compress-Archive `
    -Path $releaseDir `
    -DestinationPath $zipPath `
    -CompressionLevel Optimal `
    -Force

$hash = (Get-FileHash -LiteralPath $zipPath -Algorithm SHA256).Hash
Set-Content `
    -LiteralPath $shaPath `
    -Value ("SHA256  " + $hash + "  " + [IO.Path]::GetFileName($zipPath)) `
    -Encoding ASCII

Write-Host ""
Write-Host "=== RELEASE CREATA ==="
Write-Host "ZIP    : $zipPath"
Write-Host "SHA256 : $hash"
Write-Host "SHA file: $shaPath"
Write-Host ""
Write-Host "NON pubblicare ancora: eseguire prima il test pulito dalla cartella dist."
