[CmdletBinding()]
param([string]$ProjectDir = "D:\DEV_APPS\RecordsNext2.0")
Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"
$directories = @(
    "config","docs","examples","examples\site","release","release\site-examples",
    "reports","src","src\main","src\main\java","src\main\resources",
    "src\test","src\test\java","src\test\resources","tests","tools"
)
New-Item -ItemType Directory -Path $ProjectDir -Force | Out-Null
foreach ($relativePath in $directories) {
    New-Item -ItemType Directory -Path (Join-Path $ProjectDir $relativePath) -Force | Out-Null
}
Write-Host "Struttura RecordsNext 2.0 pronta: $ProjectDir" -ForegroundColor Green
