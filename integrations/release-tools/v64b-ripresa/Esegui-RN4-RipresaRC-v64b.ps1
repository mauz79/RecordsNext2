param([ValidateSet('check','resume')][string]$Mode='resume')
$ErrorActionPreference='Stop'
Set-StrictMode -Version Latest
$script = Join-Path $PSScriptRoot 'Riprendi-RecordsNext4-RC-v64b.py'
if (!(Test-Path -LiteralPath $script -PathType Leaf)) { throw "Script mancante: $script" }
Write-Host "RN4 v64b - ripresa preparazione esistente (mode=$Mode)"
& py -3 $script --mode $Mode
if ($LASTEXITCODE -ne 0) { throw "STOP ripresa RC v64b: exit code $LASTEXITCODE" }
