param([ValidateSet('plan','prepare')][string]$Mode='plan')
$ErrorActionPreference='Stop'
Set-StrictMode -Version Latest
$script=Join-Path $PSScriptRoot 'Prepara-RecordsNext4-RC-v64.py'
if (!(Test-Path -LiteralPath $script -PathType Leaf)) {throw "Script mancante: $script"}
Write-Host "RN4 v64 - Migrazione 3.2 -> 4.0 RC (modalita: $Mode)"
& py -3 $script --mode $Mode
if ($LASTEXITCODE -ne 0) {throw "STOP migrazione RC v64: exit code $LASTEXITCODE"}
