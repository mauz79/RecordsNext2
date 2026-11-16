param([switch]$Apply)
$ErrorActionPreference = 'Stop'
if (-not $Apply) { throw 'Usare -Apply per installare questa patch solo nella RC v64.' }
$Root = 'D:\DEV_APPS\RecordsNext4.0_RC_v64'
$Py = Join-Path $PSScriptRoot 'Applica-RN4-GuiConfig-v65.py'
$Stamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$Backup = Join-Path $Root ("backups\rn4_gui_v65_$Stamp")
$Log = "D:\DEV_APPS\downloads\RN4_GUI_V65_MAVEN_$Stamp.txt"
$Jar = Join-Path $Root 'target\RecordsNext.jar'
$Files = @('src\main\java\it\alterlega\recordsnext\gui\RecordsNext2Dashboard.java','src\main\java\it\alterlega\recordsnext\gui\RecordsNextConfigurationDialog.java')
$Patched = $false
function Restore-RN4V65 {
    if (-not (Test-Path -LiteralPath $Backup)) { return }
    foreach ($relative in $Files) {
        $source = Join-Path $Backup $relative
        if (Test-Path -LiteralPath $source) { Copy-Item -LiteralPath $source -Destination (Join-Path $Root $relative) -Force }
    }
    $savedJar = Join-Path $Backup 'target\RecordsNext.jar'
    if (Test-Path -LiteralPath $savedJar) { Copy-Item -LiteralPath $savedJar -Destination $Jar -Force }
}
try {
    if (-not (Test-Path -LiteralPath $Root -PathType Container)) { throw 'Installazione RC v64 assente' }
    # Non interferire con la GUI in esecuzione: verificare il comando dei processi Java.
    $running = @(Get-CimInstance Win32_Process -ErrorAction Stop | Where-Object {
        ($_.Name -in @('java.exe','javaw.exe')) -and $_.CommandLine -and $_.CommandLine.Contains('RecordsNext4.0_RC_v64')
    })
    if ($running.Count -gt 0) { throw 'Chiudere prima la GUI RC v64: processo Java ancora attivo' }
    $wrapper = Join-Path $Root 'mvnw.cmd'
    if (-not (Test-Path -LiteralPath $wrapper -PathType Leaf)) { throw 'Wrapper Maven RC v64 mancante' }
    Write-Host 'PATCH v65 - GUI RC v64, database e siti invariati'
    & py -3 $Py --root $Root --backup $Backup --mode apply --check-fca
    if ($LASTEXITCODE -ne 0) { throw 'Preflight/applicazione sorgenti non riuscito' }
    $Patched = $true
    Write-Host 'COMPILAZIONE + 58 TEST MAVEN...'
    Push-Location $Root
    try {
        & $wrapper -B -ntp clean package *> $Log
        if ($LASTEXITCODE -ne 0) {
            Get-Content -LiteralPath $Log -Tail 45 | ForEach-Object { Write-Host $_ }
            throw "Build/test falliti. Log: $Log"
        }
    } finally { Pop-Location }
    & py -3 $Py --root $Root --backup $Backup --mode verify
    if ($LASTEXITCODE -ne 0) { throw 'Verifica patch post-build non riuscita' }
    if (-not (Test-Path -LiteralPath $Jar -PathType Leaf)) { throw 'Jar non generato' }
    $doc = Join-Path $Root 'docs\VERIFICA_GUI_CONFIG_v65.md'
    $docDir = Split-Path -Parent $doc
    if (-not (Test-Path -LiteralPath $docDir)) { New-Item -Path $docDir -ItemType Directory -Force | Out-Null }
    @(
      '# RecordsNext4 - verifica GUI configurazione v65'
      ''
      "Data: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
      ''
      '- RC v64: patch applicata a RecordsNext2Dashboard.actionCard e RecordsNextConfigurationDialog.loadSeasons.'
      '- Card: ogni zona riceve il primo clic, anche gli spaziatori.'
      '- Apertura stagioni: metadati gia conservati in SQLite, senza riaprire 21 FCM con UCanAccess.'
      '- Maven full build e 58 test: passati sul computer Windows.'
      '- Database/FCM/FCA/siti: non modificati. SHA256 FCA 2012/13 verificato prima della patch.'
      '- Collaudo funzionale GUI da confermare manualmente; nessuna pubblicazione reale.'
      '- Bibbia v46 resta il checkpoint precedente; integrare patch in Bibbia finale dopo collaudo.'
    ) | Set-Content -LiteralPath $doc -Encoding UTF8
    Write-Host 'DOCUMENTAZIONE GUI v65:' $doc
    Write-Host 'PATCH GUI v65 OK - Maven e test superati'
    Write-Host "JAR: $Jar"
    Write-Host "BACKUP: $Backup"
    Write-Host "MAVEN LOG: $Log"
    Write-Host "AVVIO: powershell.exe -NoProfile -ExecutionPolicy Bypass -File '$Root\Avvia-RecordsNext4-RC-v64.ps1'"
    Write-Host 'Testare un clic sulla card Configurazione stagioni. Nessun file FCM/FCA modificato.'
} catch {
    if ($Patched -or (Test-Path -LiteralPath $Backup)) {
        Restore-RN4V65
        Write-Host 'ROLLBACK sorgenti e JAR originali RC completato (se backup disponibile)'
    }
    throw
}
