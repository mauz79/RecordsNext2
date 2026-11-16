param([switch]$Apply)
$ErrorActionPreference = 'Stop'
if (-not $Apply) { throw 'Per applicare alla sola RC v64 specificare -Apply.' }
$Root = 'D:\DEV_APPS\RecordsNext4.0_RC_v64'
$Tool = Join-Path $PSScriptRoot 'Applica-RN4-Dashboard-v66.py'
$Stamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$Backup = Join-Path $Root "backups\rn4_dashboard_v66_$Stamp"
$Log = "D:\DEV_APPS\downloads\RN4_DASHBOARD_V66_MAVEN_$Stamp.txt"
$Relative = 'src\main\java\it\alterlega\recordsnext\gui\RecordsNext2Dashboard.java'
$Source = Join-Path $Root $Relative
$Jar = Join-Path $Root 'target\RecordsNext.jar'
$Wrapper = Join-Path $Root 'mvnw.cmd'
$Patched = $false
function Restore-RN4Dashboard {
    if (-not (Test-Path -LiteralPath $Backup)) { return }
    $old = Join-Path $Backup $Relative
    if (Test-Path -LiteralPath $old) { Copy-Item -LiteralPath $old -Destination $Source -Force }
    $oldJar = Join-Path $Backup 'target\RecordsNext.jar'
    if (Test-Path -LiteralPath $oldJar) { Copy-Item -LiteralPath $oldJar -Destination $Jar -Force }
}
try {
    if (-not (Test-Path -LiteralPath $Wrapper -PathType Leaf)) { throw 'Wrapper Maven RC assente' }
    if (-not (Test-Path -LiteralPath $Source -PathType Leaf)) { throw 'Sorgente Java dashboard assente' }
    $running = @(Get-CimInstance Win32_Process -ErrorAction Stop | Where-Object {
        ($_.Name -in @('java.exe', 'javaw.exe')) -and $_.CommandLine -and $_.CommandLine.Contains('RecordsNext4.0_RC_v64')
    })
    if ($running.Count -gt 0) { throw 'Chiudere la GUI RC v64 prima di applicare la patch.' }
    Write-Host 'RN4 v66 - SOLO DASHBOARD nella RC v64; database e siti invariati'
    & py -3 $Tool --root $Root --backup $Backup --mode apply --check-fca
    if ($LASTEXITCODE -ne 0) { throw 'Preflight o patch sorgente fallita' }
    $Patched = $true
    Write-Host 'MAVEN: build completo e test...'
    Push-Location $Root
    try {
        & $Wrapper -B -ntp clean package *> $Log
        if ($LASTEXITCODE -ne 0) {
            Get-Content -LiteralPath $Log -Tail 40 | ForEach-Object { Write-Host $_ }
            throw "Build/test Maven falliti: $Log"
        }
    } finally { Pop-Location }
    & py -3 $Tool --root $Root --backup $Backup --mode verify
    if ($LASTEXITCODE -ne 0) { throw 'Verifica sorgente post-build fallita' }
    if (-not (Test-Path -LiteralPath $Jar -PathType Leaf)) { throw 'JAR non generato' }
    $Doc = Join-Path $Root 'docs\VERIFICA_GUI_DASHBOARD_v66.md'
    $content = @(
        '# RecordsNext4 - verifica dashboard v66',
        '',
        "Data: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')",
        '',
        '- Ambiente: RecordsNext4.0_RC_v64, non TEST e non produzione.',
        '- Titoli: RecordsNext 4.0 senza marcatori sviluppo/dev in frame, intestazione, finestra errori e sidebar.',
        '- Dashboard: card 2x2 ad altezza flessibile; rimosso limite rigido 352/185 px che tagliava la descrizione e il link.',
        '- Menu laterale: Dashboard, Configurazione stagioni, Famiglie record, Top & Flop, Soglie e Culometro, altre sezioni tecniche.',
        '- Conservate la correzione v65 del clic sulle card e la lettura metadati stagioni da SQLite.',
        '- Eseguite compilazione Maven e suite test; controllo visuale Windows ancora da confermare.',
        '- Controllato SHA256 FCA corretto 2012/2013. Nessun FCM/FCA/SQLite/JS/sito reale modificato.',
        '- Bibbia v46 e checkpoint GitHub v63 immutati finche non viene approvato il collaudo della nuova GUI.'
    )
    if (-not (Test-Path -LiteralPath $Doc)) {
        $content | Set-Content -LiteralPath $Doc -Encoding UTF8
    } else { throw "Documento v66 gia presente, non sovrascrivo: $Doc" }
    Write-Host 'PATCH DASHBOARD v66 OK - build/test e verifica sorgente completati'
    Write-Host 'JAR:' $Jar
    Write-Host 'BACKUP:' $Backup
    Write-Host 'MAVEN LOG:' $Log
    Write-Host 'DOCUMENTAZIONE:' $Doc
    Write-Host "AVVIO: powershell.exe -NoProfile -ExecutionPolicy Bypass -File '$Root\Avvia-RecordsNext4-RC-v64.ps1'"
} catch {
    if ($Patched -or (Test-Path -LiteralPath $Backup)) {
        Restore-RN4Dashboard
        Write-Host 'ROLLBACK v66: sorgente e JAR ripristinati dal backup, se disponibili'
    }
    throw
}
