param(
    [string]$ProjectRoot = 'D:\DEV_APPS\RecordsNext4.0',
    [switch]$Publish
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$ProjectRoot = [System.IO.Path]::GetFullPath($ProjectRoot)
if ($ProjectRoot.TrimEnd('\') -ine 'D:\DEV_APPS\RecordsNext4.0') {
    throw 'Pubblicazione Git consentita soltanto dal worktree 4.0 ufficiale.'
}
if (-not (Test-Path -LiteralPath (Join-Path $ProjectRoot '.git'))) {
    throw 'Worktree Git 4.0 non trovato.'
}
Push-Location $ProjectRoot
try {
    $branch = (& git branch --show-current).Trim()
    if ($LASTEXITCODE -ne 0 -or $branch -ne 'feature/recordsnext4-players') {
        throw "Ramo inatteso: $branch. Usare solo feature/recordsnext4-players; non toccare main o 3.2."
    }
    $remote = (& git remote get-url origin).Trim()
    if ($LASTEXITCODE -ne 0 -or $remote -notmatch '(?i)github\.com[:/]mauz79/RecordsNext2(\.git)?$') {
        throw "Remote origin inatteso: $remote"
    }
    if ((& git diff --cached --name-only).Count -ne 0) {
        throw 'Esistono cambiamenti gia staged: verificare e svuotare lo staging Git prima di procedere.'
    }
    $paths = @(
        'src/main/java/it/alterlega/recordsnext/app/RecordsNextPipeline.java',
        'src/main/java/it/alterlega/recordsnext/app/output/SeasonFamilyShardPublisher.java',
        'src/test/java/it/alterlega/recordsnext/app/RecordsNextFlatStagingV46SmokeTest.java',
        'README.md', 'INSTALL.md', 'CHANGELOG.md',
        'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v38.md',
        'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v38.md',
        'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v31.md',
        'docs/VERIFICA_STAGING_JS_v46.md',
        'tools/Create-RecordsNext4WorkingCodeMd-v38.py',
        'tools/Start-RecordsNext4-Test-v46.ps1',
        'tools/Publish-RecordsNext4-v46-GitHub.ps1'
    )
    foreach ($relative in $paths) {
        if (-not (Test-Path -LiteralPath (Join-Path $ProjectRoot $relative.Replace('/','\')) -PathType Leaf)) {
            throw "File aggiornato mancante: $relative. Promuovere prima la 4.0 completa e collaudata dal TEST."
        }
    }
    Write-Host "GitHub preflight: $branch -> $remote"
    Write-Host 'File di codice e documentazione destinati al commit:'
    $paths | ForEach-Object { Write-Host "  $_" }
    if (-not $Publish) {
        Write-Host 'SOLO VERIFICA: nessun git add, commit o push. Per pubblicare usare -Publish dopo il collaudo.'
        return
    }
    & git add -- @paths
    if ($LASTEXITCODE -ne 0) {throw 'git add fallito'}
    & git diff --cached --check
    if ($LASTEXITCODE -ne 0) {throw 'git diff --check fallito'}
    & git diff --cached --stat
    if ($LASTEXITCODE -ne 0) {throw 'git diff --stat fallito'}
    & git commit -m 'RecordsNext4: fix flat JS staging limit and update docs (v46)'
    if ($LASTEXITCODE -ne 0) {throw 'git commit fallito'}
    & git push -u origin $branch
    if ($LASTEXITCODE -ne 0) {throw 'git push fallito: verificare autenticazione GitHub'}
    Write-Host "PUSH GITHUB COMPLETATO: $remote / $branch"
} finally {Pop-Location}
