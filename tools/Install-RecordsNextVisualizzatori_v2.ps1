[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$SkinDir,

    [ValidateSet('mauzstrom','fantablue2','neutral')]
    [string]$Profile = 'neutral',

    [string]$PackageDir = (Join-Path $PSScriptRoot '..\release\visualizzatori')
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$package = [System.IO.Path]::GetFullPath($PackageDir)
$skin = [System.IO.Path]::GetFullPath($SkinDir)

if (-not (Test-Path -LiteralPath $package -PathType Container)) {
    throw "Pacchetto visualizzatori non trovato: $package"
}
if (-not (Test-Path -LiteralPath $skin -PathType Container)) {
    throw "Cartella skin non trovata: $skin"
}

$skinJs = Join-Path $skin 'js'
$skinViews = Join-Path $skin 'RecordsNext'
New-Item -ItemType Directory -Path $skinJs -Force | Out-Null
New-Item -ItemType Directory -Path $skinViews -Force | Out-Null

Copy-Item -LiteralPath (Join-Path $package 'recordsnext.html') -Destination (Join-Path $skin 'recordsnext.html') -Force
Get-ChildItem -LiteralPath (Join-Path $package 'RecordsNext') -File -Filter '*.html' |
    Copy-Item -Destination $skinViews -Force

Copy-Item -LiteralPath (Join-Path $package 'js\fcmRecordsNextFunzioni_common.js') -Destination $skinJs -Force
Copy-Item -LiteralPath (Join-Path $package 'js\fcmRecordsNextFunzioni_viewer.js') -Destination $skinJs -Force

$profileCss = Join-Path $package ("profiles\{0}\recordsnext.css" -f $Profile)
if (-not (Test-Path -LiteralPath $profileCss -PathType Leaf)) {
    throw "Profilo CSS non trovato: $profileCss"
}
Copy-Item -LiteralPath $profileCss -Destination (Join-Path $skinViews 'recordsnext.css') -Force

Write-Host ''
Write-Host 'Visualizzatori RecordsNext installati.'
Write-Host "Skin    : $skin"
Write-Host "Profilo : $Profile"
Write-Host "Indice  : $(Join-Path $skin 'recordsnext.html')"
Write-Host "Viste   : $skinViews"
Write-Host "Funzioni: $skinJs"
