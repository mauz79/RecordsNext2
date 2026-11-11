[CmdletBinding()]
param(
    [string]$RepoDir = 'D:\DEV_APPS\RecordsNext2.0',
    [string]$WorkDir = 'D:\DEV_APPS\RecordsNext4.0',
    [string]$OutputDir = 'D:\DEV_APPS\downloads',
    [string]$Database = 'D:\DEV_APPS\RecordsNext2.0\data\database\recordsnext-sandbox.db'
)
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Git-Text {
    param([string]$Directory, [string[]]$GitArguments)
    $result = & git -C $Directory @GitArguments
    $code = $LASTEXITCODE
    if ($code -ne 0) {
        throw ('Git ha restituito codice ' + $code + ': git -C ' + $Directory + ' ' + ($GitArguments -join ' '))
    }
    return (($result -join "`n").Trim())
}
function Run-Python {
    param([string]$Script, [string[]]$ScriptArguments, [string]$LogFile)
    & py -3 $Script @ScriptArguments | Tee-Object -FilePath $LogFile
    $code = $LASTEXITCODE
    if ($code -ne 0) { throw ('Python ha restituito codice ' + $code + ' in ' + $Script + ' (log: ' + $LogFile + ')') }
}

if (-not (Test-Path -LiteralPath $OutputDir -PathType Container)) {
    New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null
}
$stamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$runDir = Join-Path $OutputDir ('RecordsNext4_Fase0_OUTPUT_v3_' + $stamp)
$zipOut = Join-Path $OutputDir ('RecordsNext4_Fase0_OUTPUT_v3_' + $stamp + '.zip')
if (Test-Path -LiteralPath $runDir) { throw ('Cartella output esistente: ' + $runDir) }
New-Item -ItemType Directory -Path $runDir | Out-Null
$ok = $false
try {
    $kitRoot = Split-Path -Parent $PSScriptRoot
    if (-not (Get-Command git -ErrorAction SilentlyContinue)) { throw 'Git non presente nel PATH.' }
    if (-not (Get-Command py -ErrorAction SilentlyContinue)) { throw 'Python Launcher py non presente nel PATH.' }
    & py -3 --version
    if ($LASTEXITCODE -ne 0) { throw 'Python 3 non disponibile.' }
    if (-not (Test-Path -LiteralPath (Join-Path $RepoDir 'pom.xml') -PathType Leaf)) {
        throw ('Repository operativo 3.2 non trovato: ' + $RepoDir)
    }
    if (-not (Test-Path -LiteralPath $Database -PathType Leaf)) {
        throw ('Database sandbox non trovato: ' + $Database)
    }
    $originalRoot = Git-Text -Directory $RepoDir -GitArguments @('rev-parse', '--show-toplevel')
    if ([System.IO.Path]::GetFullPath($originalRoot).TrimEnd('\') -ine [System.IO.Path]::GetFullPath($RepoDir).TrimEnd('\')) {
        throw ('Repository base inatteso: ' + $originalRoot)
    }
    $originalHead = Git-Text -Directory $RepoDir -GitArguments @('rev-parse', 'HEAD')
    Write-Host ('Repository 3.2 HEAD: ' + $originalHead)

    # IMPORTANTE: se il worktree esiste, prima lo riconosciamo. Non dipendiamo dal tag.
    if (Test-Path -LiteralPath $WorkDir -PathType Container) {
        if (-not (Test-Path -LiteralPath (Join-Path $WorkDir 'pom.xml') -PathType Leaf)) {
            throw ('Directory 4.0 esistente ma senza pom.xml: ' + $WorkDir)
        }
        $workRoot = Git-Text -Directory $WorkDir -GitArguments @('rev-parse', '--show-toplevel')
        if ([System.IO.Path]::GetFullPath($workRoot).TrimEnd('\') -ine [System.IO.Path]::GetFullPath($WorkDir).TrimEnd('\')) {
            throw ('Directory 4.0 diversa dalla radice Git: ' + $workRoot)
        }
        $branch = Git-Text -Directory $WorkDir -GitArguments @('branch', '--show-current')
        if ($branch -ne 'feature/recordsnext4-players') {
            throw ('Il worktree 4.0 esistente usa un branch inatteso: ' + $branch)
        }
        Write-Host 'Worktree 4.0 esistente riconosciuto. Non lo ricreo.' -ForegroundColor Green
    }
    else {
        # Solo se manca il worktree: verifichiamo il tag PRIMA di creare il branch.
        & git -C $RepoDir show-ref --verify --quiet 'refs/tags/v3.2.0'
        $tagResult = $LASTEXITCODE
        if ($tagResult -ne 0) {
            throw ('Impossibile creare worktree: refs/tags/v3.2.0 non verificabile (Git exit ' + $tagResult + ').')
        }
        Write-Host 'Creo il worktree 4.0 a partire dal tag v3.2.0...' -ForegroundColor Cyan
        & git -C $RepoDir worktree add -b 'feature/recordsnext4-players' $WorkDir 'v3.2.0'
        $addResult = $LASTEXITCODE
        if ($addResult -ne 0) { throw ('Creazione worktree fallita, Git exit ' + $addResult) }
    }

    $workHead = Git-Text -Directory $WorkDir -GitArguments @('rev-parse', 'HEAD')
    & git -C $WorkDir merge-base --is-ancestor 'f9bf30323d9204d70e91e242bddfc72c2060ecf9' $workHead
    $ancestorExit = $LASTEXITCODE
    if ($ancestorExit -ne 0) {
        throw ('Il worktree 4.0 non discende dal commit base f9bf303 (Git exit ' + $ancestorExit + ').')
    }
    Write-Host ('Worktree HEAD: ' + $workHead)
    $files = @(
        'docs\REVISIONE_BASE_RECORDSNEXT_3_2_v1.md',
        'docs\PIANO_RECORDSNEXT4_GIOCATORI_v3.md',
        'docs\REGISTRO_DECISIONI_RECORDSNEXT4_v2.md',
        'docs\DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v2.md',
        'tools\Audit-RecordsNext4Giocatori-v1.py',
        'tools\Probe-RecordsNext4Fonti-v1.py',
        'tools\Probe-RecordsNext4Tabellini-v1.py',
        'tools\Create-RecordsNext4WorkingCodeMd-v2.ps1',
        'tools\Invoke-RecordsNext4-Fase0-v3.ps1'
    )
    # Controllo preliminare: nessuna copia parziale se esiste un file differente.
    foreach ($f in $files) {
        $src = Join-Path $kitRoot $f
        $dst = Join-Path $WorkDir $f
        if (-not (Test-Path -LiteralPath $src -PathType Leaf)) { throw ('File kit mancante: ' + $src) }
        if (Test-Path -LiteralPath $dst -PathType Leaf) {
            if ((Get-FileHash -LiteralPath $src -Algorithm SHA256).Hash -ne (Get-FileHash -LiteralPath $dst -Algorithm SHA256).Hash) {
                throw ('File gia presente ma diverso. Nessuna sovrascrittura: ' + $dst)
            }
        }
    }
    foreach ($f in $files) {
        $src = Join-Path $kitRoot $f
        $dst = Join-Path $WorkDir $f
        if (-not (Test-Path -LiteralPath $dst -PathType Leaf)) {
            $parent = Split-Path -Parent $dst
            if (-not (Test-Path -LiteralPath $parent -PathType Container)) {
                New-Item -ItemType Directory -Path $parent -Force | Out-Null
            }
            Copy-Item -LiteralPath $src -Destination $dst
            Write-Host ('Installato: ' + $f)
        } else { Write-Host ('Gia presente identico: ' + $f) }
    }
    & (Join-Path $WorkDir 'tools\Create-RecordsNext4WorkingCodeMd-v2.ps1') -ProjectDir $WorkDir

    Run-Python -Script (Join-Path $WorkDir 'tools\Audit-RecordsNext4Giocatori-v1.py') -ScriptArguments @('--database',$Database,'--code','100843','--output-dir',$runDir) -LogFile (Join-Path $runDir 'LOG_giocatore_100843.txt')
    Run-Python -Script (Join-Path $WorkDir 'tools\Audit-RecordsNext4Giocatori-v1.py') -ScriptArguments @('--database',$Database,'--code','12302','--output-dir',$runDir) -LogFile (Join-Path $runDir 'LOG_giocatore_12302.txt')
    Run-Python -Script (Join-Path $WorkDir 'tools\Probe-RecordsNext4Fonti-v1.py') -ScriptArguments @('--database',$Database,'--output-dir',$runDir) -LogFile (Join-Path $runDir 'LOG_fonti.txt')
    Run-Python -Script (Join-Path $WorkDir 'tools\Probe-RecordsNext4Tabellini-v1.py') -ScriptArguments @('--database',$Database,'--output-dir',$runDir) -LogFile (Join-Path $runDir 'LOG_tabellini.txt')

    $manifest = @(
        'RECORDSNEXT4 FASE 0 - v3',
        ('Data: ' + (Get-Date -Format 'yyyy-MM-dd HH:mm:ss')),
        ('Repo 3.2: ' + $RepoDir), ('Repo HEAD: ' + $originalHead),
        ('Worktree 4.0: ' + $WorkDir), ('Worktree HEAD: ' + $workHead),
        ('Database: ' + $Database),
        ('SHA256 database: ' + (Get-FileHash -LiteralPath $Database -Algorithm SHA256).Hash),
        'SQLITE letto con mode=ro e query_only=ON. Nessun sito pubblicato.'
    )
    $manifest | Out-File -LiteralPath (Join-Path $runDir 'MANIFEST_RUN_v3.txt') -Encoding UTF8
    & git -C $RepoDir status -sb | Out-File -LiteralPath (Join-Path $runDir 'GIT_32_STATO.txt') -Encoding UTF8
    & git -C $WorkDir status -sb | Out-File -LiteralPath (Join-Path $runDir 'GIT_40_STATO.txt') -Encoding UTF8
    $docs = Join-Path $runDir 'documentazione'
    New-Item -ItemType Directory -Path $docs | Out-Null
    foreach ($f in @('CODICE_FUNZIONANTE_RECORDSNEXT4_v2.md','REVISIONE_BASE_RECORDSNEXT_3_2_v1.md','PIANO_RECORDSNEXT4_GIOCATORI_v3.md','REGISTRO_DECISIONI_RECORDSNEXT4_v2.md','DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v2.md')) {
        Copy-Item -LiteralPath (Join-Path (Join-Path $WorkDir 'docs') $f) -Destination $docs
    }
    $ok = $true
}
catch {
    ($_ | Out-String) | Out-File -LiteralPath (Join-Path $runDir 'ERRORE_FASE0_v3.txt') -Encoding UTF8
    Write-Host ('Fase 0 interrotta: ' + $_.Exception.Message) -ForegroundColor Red
    throw
}
finally {
    if (Test-Path -LiteralPath $runDir -PathType Container) {
        try {
            Compress-Archive -Path (Join-Path $runDir '*') -DestinationPath $zipOut -CompressionLevel Optimal
            Write-Host ('ZIP risultati/diagnostica da caricare: ' + $zipOut) -ForegroundColor Cyan
        }
        catch { Write-Warning ('ZIP diagnostico non creato: ' + $_.Exception.Message) }
    }
}
if ($ok) {
    Write-Host 'Fase 0 v3 eseguita. Non eseguire git add/commit o pubblicazioni.' -ForegroundColor Green
}
