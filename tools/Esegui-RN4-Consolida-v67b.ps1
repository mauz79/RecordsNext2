param(
    [ValidateSet('github')][string]$Mode = 'github',
    [switch]$Apply
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
if (-not $Apply) { throw 'Usare -Apply per completare il checkpoint GitHub v67b.' }
$Root = 'D:\DEV_APPS\RecordsNext4.0_RC_v64'
$Down = 'D:\DEV_APPS\downloads'
$Work = 'D:\DEV_APPS\downloads\RN4_GITHUB_WORK_v63_20261116_174243'
$OldBranch = 'feature/recordsnext4-v63-sandbox-validated'
$NewBranch = 'feature/recordsnext4-v67-rc-gui-migrated'
$ExpectedCommit = '3b008a8'
$Stamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$env:GIT_PAGER = 'cat'
$env:PAGER = 'cat'
$env:GIT_CONFIG_NOSYSTEM = '0'
function Git-Checked([string]$operation,[string[]]$arguments) {
    & git -C $Work @arguments
    if ($LASTEXITCODE -ne 0) { throw "Git $operation fallito (exit=$LASTEXITCODE)" }
}
function Copy-Safe([string]$src,[string]$dst) {
    if (-not (Test-Path -LiteralPath $src -PathType Leaf)) { throw "Sorgente mancante: $src" }
    $folder=Split-Path -Parent $dst
    if (-not (Test-Path -LiteralPath $folder)) { New-Item -ItemType Directory -Path $folder -Force | Out-Null }
    Copy-Item -LiteralPath $src -Destination $dst -Force
}
function Copy-AllowedFiles([string]$src,[string]$dst) {
    if (-not (Test-Path -LiteralPath $src -PathType Container)) { throw "Pacchetto mancante: $src" }
    $list = @(Get-ChildItem -LiteralPath $src -File | Where-Object { $_.Extension -in @('.py','.ps1','.md') })
    if ($list.Count -lt 2) { throw "Pacchetto incompleto: $src" }
    foreach ($p in $list) { Copy-Safe $p.FullName (Join-Path $dst $p.Name) }
    Write-Host "PACCHETTO REPRODUCIBILE: $(Split-Path -Leaf $src) = $($list.Count) file"
}
function Confirm-Docs {
    foreach($p in @(
        'docs\CODICE_FUNZIONANTE_RECORDSNEXT4_v47.md',
        'docs\DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v47.md',
        'docs\REGISTRO_DECISIONI_RECORDSNEXT4_v40.md',
        'docs\STATO_MIGRAZIONE_GUI_RELEASE_v67.md',
        'docs\VERIFICA_GUI_CONFIG_v65.md',
        'docs\VERIFICA_GUI_DASHBOARD_v66.md',
        'tools\Create-RecordsNext4WorkingCodeMd-v47.py',
        'tools\Esegui-RN4-Consolida-v67.ps1'
    )) {
        if (-not (Test-Path -LiteralPath (Join-Path $Root $p) -PathType Leaf)) { throw "Documento RC mancante: $p" }
    }
}
if (-not (Test-Path -LiteralPath $Root -PathType Container)) { throw 'RC v64 non trovata' }
if ($Mode -in @('docs','all')) {
    $bib = Join-Path $Root 'docs\CODICE_FUNZIONANTE_RECORDSNEXT4_v47.md'
    if (-not (Test-Path -LiteralPath $bib)) {
        Write-Host 'RN4 v67: verifiche RC + generazione Bibbia v47 e documenti...'
        & py -3 (Join-Path $PSScriptRoot 'Create-RecordsNext4WorkingCodeMd-v47.py') --root $Root --downloads $Down
        if ($LASTEXITCODE -ne 0) { throw 'Generatore documentazione v47 fallito. Non tocco Git.' }
    } else {
        Write-Host 'DOCUMENTAZIONE v47 GIA PRESENTE - non rigenero e non sovrascrivo'
    }
    Confirm-Docs
    Write-Host 'DOCUMENTAZIONE v47 RC OK'
}
if ($Mode -eq 'docs') {
    Write-Host 'STOP VOLONTARIO: nessun commit/push eseguito (Mode docs).'
    exit 0
}
Confirm-Docs
Write-Host 'GITHUB v67: aggiornamento incrementale dal commit v63 (mai branch v46)'
if (-not (Test-Path -LiteralPath (Join-Path $Work '.git') -PathType Container)) { throw 'Clone Git v63 assente; nessun nuovo clone automatico.' }
$branchData = @(& git -C $Work branch --show-current 2>&1)
$branchCode = $LASTEXITCODE
if ($branchCode -ne 0 -or $branchData.Count -ne 1 -or [string]::IsNullOrWhiteSpace([string]$branchData[0])) {
    throw "Impossibile leggere branch Git (exit=$branchCode): $($branchData -join '; ')"
}
$branch = ([string]$branchData[0]).Trim()
$headData = @(& git -C $Work rev-parse --short HEAD 2>&1)
$headCode = $LASTEXITCODE
if ($headCode -ne 0 -or $headData.Count -ne 1) {
    throw "Impossibile leggere commit Git (exit=$headCode): $($headData -join '; ')"
}
$head = ([string]$headData[0]).Trim()
$originData = @(& git -C $Work remote get-url origin 2>&1)
$originCode = $LASTEXITCODE
if ($originCode -ne 0 -or $originData.Count -ne 1) {
    throw "Impossibile leggere origin Git (exit=$originCode): $($originData -join '; ')"
}
$origin = ([string]$originData[0]).Trim()
Write-Host "GIT PREFLIGHT: branch=$branch commit=$head remote=$origin"
if ($origin -notmatch '(^https://github\.com/mauz79/RecordsNext2(\.git)?$|^git@github\.com:mauz79/RecordsNext2(\.git)?$)') { throw "Remote Git inatteso: $origin" }
if ($head -ne $ExpectedCommit) { throw "Commit base inatteso: $head (richiesto $ExpectedCommit)" }
if ($branch -notin @($OldBranch,$NewBranch)) { throw "Branch inatteso: $branch" }
$dirty = @(& git -C $Work status --porcelain --untracked-files=normal)
if ($LASTEXITCODE -ne 0) { throw 'Git status fallito' }
if ($dirty.Count -gt 0) { throw "Clone Git con modifiche non previste: $($dirty.Count). Fermo prima di sovrascrivere." }
if ($branch -eq $OldBranch) {
    Git-Checked 'creazione branch v67' @('switch','-c',$NewBranch)
}
$remoteHead=@(& git -C $Work ls-remote --heads origin $NewBranch)
if ($LASTEXITCODE -ne 0) { throw 'Impossibile verificare branch remoto v67' }
if ($remoteHead.Count -gt 0) { throw 'Branch v67 gia presente sul remoto; non sovrascrivo.' }
# Versioned corrected runner recorded in RC and in the new Git branch.
Copy-Safe (Join-Path $PSScriptRoot 'Esegui-RN4-Consolida-v67b.ps1') (Join-Path $Root 'tools\Esegui-RN4-Consolida-v67b.ps1')
Copy-Safe (Join-Path $PSScriptRoot 'NOTA_GITHUB_v67b.md') (Join-Path $Root 'docs\NOTA_GITHUB_v67b.md')
# Copiare SOLO i due sorgenti GUI corretti, non l'intero albero della RC.
$java = @(
  'src/main/java/it/alterlega/recordsnext/gui/RecordsNext2Dashboard.java',
  'src/main/java/it/alterlega/recordsnext/gui/RecordsNextConfigurationDialog.java'
)
foreach($f in $java) { Copy-Safe (Join-Path $Root $f) (Join-Path $Work $f) }
$docRel=@(
  'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v47.md',
  'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v47.md',
  'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v40.md',
  'docs/STATO_MIGRAZIONE_GUI_RELEASE_v67.md',
  'docs/VERIFICA_GUI_CONFIG_v65.md',
  'docs/VERIFICA_GUI_DASHBOARD_v66.md',
  'docs/NOTA_GITHUB_v67b.md',
  'tools/Create-RecordsNext4WorkingCodeMd-v47.py',
  'tools/Esegui-RN4-Consolida-v67b.ps1',
  'README.md','INSTALL.md','CHANGELOG.md'
)
foreach($f in $docRel) { Copy-Safe (Join-Path $Root $f) (Join-Path $Work $f) }
# Sorgenti dei pacchetti di migrazione e correzione GUI, NON dati utente.
$packages=@{
 'v64-migrazione'='RN4_MIGRAZIONE_RC_v64'
 'v64b-ripresa'='RN4_MIGRAZIONE_RC_v64b'
 'v65-gui'='RN4_GUI_CONFIG_v65'
 'v66-dashboard'='RN4_GUI_DASHBOARD_v66'
}
foreach($k in ($packages.Keys | Sort-Object)) {
  Copy-AllowedFiles (Join-Path $Down $packages[$k]) (Join-Path $Work "integrations\release-tools\$k")
}
# Staging espresso con whitelist: nessuna configurazione o DB privato.
$stage = @($java) + @($docRel) + @('integrations/release-tools')
& git -C $Work -c core.autocrlf=false add -A -- @stage
if ($LASTEXITCODE -ne 0) { throw 'Git add fallito' }
$staged=@(& git -C $Work diff --cached --name-only)
if ($LASTEXITCODE -ne 0 -or $staged.Count -lt 12) { throw 'Staging inatteso o vuoto' }
foreach($f in $staged) {
 if ($f -notmatch '^(src/main/java/it/alterlega/recordsnext/gui/(RecordsNext2Dashboard|RecordsNextConfigurationDialog)\.java$|docs/|tools/(Create-RecordsNext4WorkingCodeMd-v47\.py|Esegui-RN4-Consolida-v67b\.ps1)$|integrations/release-tools/|README\.md$|INSTALL\.md$|CHANGELOG\.md$)') { throw "File inatteso in staging: $f" }
 if ($f -match '(?i)(\.fca$|\.fcm$|\.db$|\.sqlite$|\.env$|secret|token|credential|site-export-staging|publish-sandbox)') { throw "File riservato in staging: $f" }
 if ($f -match '^docs/' -and $docRel -notcontains $f) { throw "Documento fuori whitelist: $f" }
}
& git -C $Work --no-pager diff --cached --check *> (Join-Path $Down "RN4_GITHUB_V67_WHITESPACE_$Stamp.txt")
if ($LASTEXITCODE -ne 0) {
    Write-Host 'GIT CHECK: whitespace rilevato; leggere il rapporto, nessun commit eseguito.'
    throw 'git diff --cached --check fallito'
}
Write-Host "STAGING v67: $($staged.Count) file"
& git -C $Work --no-pager diff --cached --shortstat
if ($LASTEXITCODE -ne 0) { throw 'Riepilogo Git fallito' }
$Log=Join-Path $Down "RN4_GITHUB_V67_MAVEN_$Stamp.txt"
$Wrapper=Join-Path $Work 'mvnw.cmd'
if (-not (Test-Path -LiteralPath $Wrapper -PathType Leaf)) { throw 'Wrapper Maven clone mancante' }
Write-Host 'MAVEN: test della versione che andra su GitHub...'
& $Wrapper -B -ntp -f (Join-Path $Work 'pom.xml') test *> $Log
$mavenCode=$LASTEXITCODE
Get-Content -LiteralPath $Log -Tail 15
if ($mavenCode -ne 0) { throw "Maven non superato. Nessun commit/push. Log: $Log" }
Git-Checked 'commit' @('-c','core.pager=cat','commit','-m','RecordsNext4 v67 RC: migrazione 3.2, GUI v65-v66, Bibbia v47')
Git-Checked 'push' @('-c','core.pager=cat','push','-u','origin',$NewBranch)
$commit = (& git -C $Work rev-parse --short HEAD | Select-Object -First 1)
if ($LASTEXITCODE -ne 0) { throw 'Errore lettura commit finale' }
Write-Host "GITHUB PUSH v67 OK: $NewBranch commit=$commit"
Write-Host "MAVEN LOG: $Log"
Write-Host 'STATO: RC GUI/documentazione consolidate. Browser e rilascio siti reali ANCORA PENDENTI.'
