$ErrorActionPreference = "Stop"

$ProjectDir = "D:\DEV_APPS\RecordsNext2.0"
$Viewer = Join-Path $ProjectDir "release\visualizzatori\js\fcmRecordsNextFunzioni_viewer.js"
$Stamp = Get-Date -Format "yyyyMMdd_HHmmss"
$Backup = Join-Path $ProjectDir ("backup_league_direction_v31_" + $Stamp + "\fcmRecordsNextFunzioni_viewer.js")

Set-Location $ProjectDir

if (-not (Test-Path -LiteralPath $Viewer)) {
    throw "Viewer non trovato: $Viewer"
}

New-Item -ItemType Directory -Path (Split-Path -Parent $Backup) -Force | Out-Null
Copy-Item -LiteralPath $Viewer -Destination $Backup -Force

$text = [System.IO.File]::ReadAllText($Viewer)

$marker = "  function leagueRecordViews() {"
if (-not $text.Contains($marker)) {
    throw "Funzione leagueRecordViews non trovata."
}

if (-not $text.Contains("function leagueRecordDirection(view)")) {
    $directionFunction = @'
  function leagueRecordDirection(view) {
    var id = String(view && view.id || '');
    var label = String(view && view.label || '').toLowerCase();

    if (id === 'puntiSquadraMin') return 'min';
    if (label.indexOf('minor ') === 0) return 'min';
    if (label.indexOf('minimo ') === 0) return 'min';
    if (label.indexOf('piu basso') === 0) return 'min';
    if (label.indexOf('più basso') === 0) return 'min';

    return 'max';
  }

'@
    $text = $text.Replace($marker, $directionFunction + $marker)
}

$oldSort = @'
      candidates.sort(function (a, b) {
        return b.score - a.score;
      });
'@

$newSort = @'
      var direction = leagueRecordDirection(entry.view);

      candidates.sort(function (a, b) {
        return direction === 'min' ? a.score - b.score : b.score - a.score;
      });
'@

if ($text.Contains($oldSort)) {
    $text = $text.Replace($oldSort, $newSort)
}
elseif (-not $text.Contains("var direction = leagueRecordDirection(entry.view);")) {
    throw "Blocco ordinamento Record di lega non riconosciuto."
}

$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllText($Viewer, $text, $utf8NoBom)

Write-Host ""
Write-Host "=== VERIFICA PATCH RECORD DI LEGA ==="

$checks = @(
    "function leagueRecordDirection(view)",
    "id === 'puntiSquadraMin'",
    "var direction = leagueRecordDirection(entry.view);",
    "direction === 'min' ? a.score - b.score : b.score - a.score"
)

foreach ($check in $checks) {
    if (-not ([System.IO.File]::ReadAllText($Viewer).Contains($check))) {
        throw "Verifica fallita: $check"
    }
    Write-Host ("OK: " + $check)
}

if (Get-Command node -ErrorAction SilentlyContinue) {
    Write-Host ""
    Write-Host "=== NODE --CHECK ==="
    & node --check $Viewer
    if ($LASTEXITCODE -ne 0) {
        throw "node --check fallito. Backup: $Backup"
    }
}

Write-Host ""
Write-Host "=== MAVEN TEST ==="
& .\mvnw.cmd test
if ($LASTEXITCODE -ne 0) {
    throw "Maven test falliti. Backup: $Backup"
}

Write-Host ""
Write-Host "=== GIT DIFF --CHECK ==="
& git diff --check
if ($LASTEXITCODE -ne 0) {
    throw "git diff --check ha rilevato problemi."
}

Write-Host ""
Write-Host "PATCH RECORD DI LEGA v31: OK"
Write-Host ("Backup: " + $Backup)
