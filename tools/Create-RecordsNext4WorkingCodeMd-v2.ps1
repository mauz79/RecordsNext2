[CmdletBinding()]
param(
    [string]$ProjectDir = 'D:\DEV_APPS\RecordsNext4.0'
)
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

if (-not (Test-Path -LiteralPath (Join-Path $ProjectDir 'pom.xml') -PathType Leaf)) {
    throw ('Progetto RecordsNext non trovato: ' + $ProjectDir)
}
$docDir = Join-Path $ProjectDir 'docs'
$destination = Join-Path $docDir 'CODICE_FUNZIONANTE_RECORDSNEXT4_v2.md'
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
foreach ($f in $files) {
    if (-not (Test-Path -LiteralPath (Join-Path $ProjectDir $f) -PathType Leaf)) {
        throw ('File necessario alla bibbia non trovato: ' + $f)
    }
}
$commitLines = & git -C $ProjectDir rev-parse HEAD
$exitCode = $LASTEXITCODE
if ($exitCode -ne 0) { throw 'Impossibile leggere HEAD Git.' }
$branchLines = & git -C $ProjectDir branch --show-current
$exitCode = $LASTEXITCODE
if ($exitCode -ne 0) { throw 'Impossibile leggere il branch Git.' }
$commit = ($commitLines -join '').Trim()
$branch = ($branchLines -join '').Trim()
$b = New-Object System.Text.StringBuilder
[void]$b.AppendLine('# RecordsNext 4.0 - Bibbia incrementale v2 (Fase 0)')
[void]$b.AppendLine('')
[void]$b.AppendLine('> Documentazione e codice diagnostico, NON una release 4.0 funzionante.')
[void]$b.AppendLine('> Base 3.2 immutata: docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md.')
[void]$b.AppendLine('> I dati della fase 0 sono nei report di D:\DEV_APPS\downloads, non certificati da questo file.')
[void]$b.AppendLine('')
[void]$b.AppendLine(('Generato: ' + (Get-Date -Format 'yyyy-MM-dd HH:mm:ss')))
[void]$b.AppendLine(('Branch: ' + $branch))
[void]$b.AppendLine(('Commit: ' + $commit))
[void]$b.AppendLine('')
[void]$b.AppendLine('## Stato verificabile')
[void]$b.AppendLine('')
[void]$b.AppendLine('- Base sorgente v3.2.0; nessuna variazione a codice Java, schema SQL, GUI o JS pubblici.')
[void]$b.AppendLine('- Verifiche diagnostiche previste: identificativi, famiglie raw, riferimenti tabellini e candidati riposo.')
[void]$b.AppendLine('- I tabellini senza Incontro non vengono dichiarati corrotti o a riposo in assenza di prova.')
[void]$b.AppendLine('- Stato locale dei test: consultare log e report ZIP generati dal runner v3.')
[void]$b.AppendLine('')
foreach ($f in $files) {
    $p = Join-Path $ProjectDir $f
    $hash = (Get-FileHash -LiteralPath $p -Algorithm SHA256).Hash
    [void]$b.AppendLine(('## ' + ($f -replace '\', '/')))
    [void]$b.AppendLine('')
    [void]$b.AppendLine(('SHA256: ' + $hash))
    [void]$b.AppendLine('')
    [void]$b.AppendLine('~~~~text')
    foreach ($line in @(Get-Content -LiteralPath $p -Encoding UTF8)) {
        [void]$b.AppendLine($line)
    }
    [void]$b.AppendLine('~~~~')
    [void]$b.AppendLine('')
}
$enc = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllText($destination, $b.ToString(), $enc)
Write-Host ('Bibbia incrementale 4.0 v2 generata: ' + $destination)
