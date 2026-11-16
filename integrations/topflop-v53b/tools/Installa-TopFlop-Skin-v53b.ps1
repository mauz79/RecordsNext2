param([ValidateSet('plan','apply','verify')][string]$Mode='plan')
$ErrorActionPreference='Stop'
$root='D:\DEV_APPS\RecordsNext4.0_TEST_v37'
$sites=Join-Path $root 'data\publish-sandbox-v52\sites'
$source=Split-Path -Parent $PSScriptRoot
$manifestFile=Join-Path $source 'MANIFEST_SHA256.json'
if(-not(Test-Path -LiteralPath $manifestFile)) {throw 'MANIFEST SHA-256 non trovato'}
$manifest=Get-Content -LiteralPath $manifestFile -Raw | ConvertFrom-Json
foreach($f in $manifest.files) {
  $p=Join-Path $source $f.path
  if(-not(Test-Path -LiteralPath $p)) {throw "File pacchetto mancante: $($f.path)"}
  if((Get-FileHash -LiteralPath $p -Algorithm SHA256).Hash -ine $f.sha256) {throw "Hash pacchetto non valido: $($f.path)"}
}
$seasons=@('2025_2026','2026_2027')
$skins=@('mauzstrom','fantablue2','maelstrom','neutral')
$jsNames=@('fcmRecordsNextFunzioni_topflop_v53.js','fcmRecordsNextFunzioni_topflop_index_v53.js','fcmRecordsNextTopFlopViewsDecoder-v2.js')
$card='<article class="rn-family-card" data-family-card="topFlop"><span class="rn-availability">Verifica...</span><h2>Records Top &amp; Flop</h2><p>Classifiche, calciatori e carriere storiche dalle viste JS generate dal plugin.</p><a class="rn-button" href="RecordsNext/top_flop.html">Apri</a></article>'
$includes='<script src="js/fcmRecordsNext_TopFlopViews.js"></script><script src="js/fcmRecordsNextFunzioni_topflop_index_v53.js"></script>'
function Assert-Inside([string]$site,[string]$path) {
  $base=[IO.Path]::GetFullPath($sites).TrimEnd([IO.Path]::DirectorySeparatorChar)+[IO.Path]::DirectorySeparatorChar
  $siteFull=[IO.Path]::GetFullPath($site)
  if(-not($siteFull+[IO.Path]::DirectorySeparatorChar).StartsWith($base,[StringComparison]::OrdinalIgnoreCase)) {throw "Sito fuori sandbox: $siteFull"}
  $target=[IO.Path]::GetFullPath($path)
  if($target -ine $siteFull -and -not $target.StartsWith($siteFull+[IO.Path]::DirectorySeparatorChar,[StringComparison]::OrdinalIgnoreCase)) {throw "DESTINAZIONE FUORI SANDBOX: $target"}
  $p=$target
  while($p.StartsWith([IO.Path]::GetFullPath($root),[StringComparison]::OrdinalIgnoreCase)) {
    if(Test-Path -LiteralPath $p) {
      $item=Get-Item -LiteralPath $p -Force
      if(($item.Attributes -band [IO.FileAttributes]::ReparsePoint) -ne 0) {throw "Junction/symlink vietato: $p"}
    }
    if($p -ieq [IO.Path]::GetFullPath($root)) {break}
    $p=Split-Path -Parent $p
  }
}
function Read-Index([string]$site) {
  $index=Join-Path $site 'recordsnext.html'
  Assert-Inside $site $index
  if(-not(Test-Path -LiteralPath $index)) {throw "Indice mancante: $index"}
  $html=[IO.File]::ReadAllText($index)
  if($html -notmatch '(?is)<body\b' -or $html -notmatch '(?is)</body\s*>' -or
     $html -notmatch '(?is)RecordsNext/recordsnext\.css|fcmRecordsNext_Core\.js|rn-shell') {
     throw "HTML non riconosciuto come RecordsNext: $index (controllo strutturale, nessun file modificato)"
  }
  return $html
}
function Update-Index([string]$html) {
  if($html -notmatch 'data-family-card\s*=\s*["'']topFlop["'']') {
    $grid=[regex]::Match($html,'(?is)<(?:section|div)\b[^>]*\bclass\s*=\s*["''][^"'']*\brn-card-grid\b[^"'']*["''][^>]*>')
    if($grid.Success) {
      $html=$html.Insert($grid.Index+$grid.Length,$card)
    }else{
      # Existing older/custom RecordsNext index: add a self-contained card grid, preserving its layout.
      $closing=[regex]::Match($html,'(?is)</main\s*>')
      if(-not $closing.Success) {$closing=[regex]::Match($html,'(?is)</body\s*>')}
      if(-not $closing.Success) {throw 'Chiusura HTML non disponibile'}
      $html=$html.Insert($closing.Index,'<section class="rn-card-grid">'+$card+'</section>')
    }
  }
  if($html -notmatch 'fcmRecordsNextFunzioni_topflop_index_v53\.js') {
    $closing=[regex]::Match($html,'(?is)</body\s*>')
    if(-not $closing.Success) {throw 'Chiusura body non disponibile'}
    $html=$html.Insert($closing.Index,$includes)
  }
  return $html
}
function Verify-Site([string]$site) {
  $html=Read-Index $site
  if($html -notmatch 'data-family-card\s*=\s*["'']topFlop["'']') {throw "Card Top & Flop mancante: $site"}
  if($html -notmatch 'fcmRecordsNextFunzioni_topflop_index_v53\.js') {throw "Bridge indice mancante: $site"}
  foreach($name in $jsNames) {
    $p=Join-Path $site "js\$name";Assert-Inside $site $p
    if(-not(Test-Path -LiteralPath $p)) {throw "JS non installato: $p"}
  }
  foreach($name in @('top_flop.html','top_flop_v53.css')) {
    $p=Join-Path $site "RecordsNext\$name";Assert-Inside $site $p
    if(-not(Test-Path -LiteralPath $p)) {throw "Pagina o CSS mancante: $p"}
  }
  foreach($skin in $skins) {
    $p=Join-Path $site "profiles\$skin\recordsnext.css";Assert-Inside $site $p
    if(-not(Test-Path -LiteralPath $p)) {throw "Profilo CSS mancante: $p"}
  }
  Write-Host "VERIFICATO: $site\RecordsNext\top_flop.html"
}
if(-not(Test-Path -LiteralPath $sites)) {throw "Sandbox v52 mancante: $sites"}
$planned=@()
foreach($season in $seasons) {
  $site=Join-Path $sites $season
  Assert-Inside $site $site
  $facade=Join-Path $site 'js\fcmRecordsNext_TopFlopViews.js'
  Assert-Inside $site $facade
  if(-not(Test-Path -LiteralPath $facade)) {throw "Facade del plugin assente: $season"}
  $orig=Read-Index $site
  $new=Update-Index $orig
  $gridMode=if($orig -match '\brn-card-grid\b') {'griglia esistente'} else {'griglia integrativa'}
  $planned+=@([pscustomobject]@{Season=$season;Site=$site;Original=$orig;New=$new})
  if($Mode -eq 'plan') {Write-Host "PIANO $season : $gridMode; indice integrabile; solo SANDBOX"}
}
if($Mode -eq 'plan') {Write-Host 'PIANO v53b OK: entrambi gli indici validati; nessun file modificato.';exit 0}
if($Mode -eq 'verify') {foreach($entry in $planned){Verify-Site $entry.Site};Write-Host 'VERIFICA v53b OK';exit 0}
# Apply with all-site preflight, copies of originals and rollback of changed paths on failure.
$backup=Join-Path $root ('backups\rn4_frontend_v53b_'+(Get-Date -Format 'yyyyMMdd_HHmmss'))
$undo=New-Object System.Collections.ArrayList
function Backup-Target([string]$site,[string]$dest) {
  Assert-Inside $site $dest
  $relative=[IO.Path]::GetFullPath($dest).Substring([IO.Path]::GetFullPath($sites).TrimEnd('\').Length).TrimStart('\')
  $original=$null
  if(Test-Path -LiteralPath $dest) {
    $original=Join-Path $backup $relative
    New-Item -ItemType Directory -Path (Split-Path -Parent $original) -Force | Out-Null
    Copy-Item -LiteralPath $dest -Destination $original -Force
  }
  [void]$undo.Add([pscustomobject]@{Dest=$dest;Original=$original})
}
try {
  foreach($entry in $planned) {
    $site=$entry.Site
    foreach($name in $jsNames) {
      $dest=Join-Path $site "js\$name";Backup-Target $site $dest
      New-Item -ItemType Directory -Path (Split-Path -Parent $dest) -Force | Out-Null
      Copy-Item -LiteralPath (Join-Path $source "js\$name") -Destination $dest -Force
    }
    foreach($name in @('top_flop.html','top_flop_v53.css')) {
      $dest=Join-Path $site "RecordsNext\$name";Backup-Target $site $dest
      New-Item -ItemType Directory -Path (Split-Path -Parent $dest) -Force | Out-Null
      Copy-Item -LiteralPath (Join-Path $source "RecordsNext\$name") -Destination $dest -Force
    }
    foreach($skin in $skins) {
      $dest=Join-Path $site "profiles\$skin\recordsnext.css"
      if(-not(Test-Path -LiteralPath $dest)) {
        Backup-Target $site $dest
        New-Item -ItemType Directory -Path (Split-Path -Parent $dest) -Force | Out-Null
        Copy-Item -LiteralPath (Join-Path $source "profiles\$skin\recordsnext.css") -Destination $dest -Force
      }
    }
    $index=Join-Path $site 'recordsnext.html'
    if($entry.New -cne $entry.Original) {
      Backup-Target $site $index
      [IO.File]::WriteAllText($index,$entry.New,(New-Object System.Text.UTF8Encoding($false)))
    }
    Verify-Site $site
  }
}catch{
  Write-Warning ('Installazione interrotta: '+$_.Exception.Message)
  for($i=$undo.Count-1;$i -ge 0;$i--) {
    $x=$undo[$i]
    if($x.Original) {Copy-Item -LiteralPath $x.Original -Destination $x.Dest -Force}
    elseif(Test-Path -LiteralPath $x.Dest) {Remove-Item -LiteralPath $x.Dest -Force}
  }
  throw 'INSTALLAZIONE FALLITA: file ripristinati dal backup'
}
Write-Host "INTEGRAZIONE v53b OK - due siti sandbox; backup $backup; siti E: non modificati"
