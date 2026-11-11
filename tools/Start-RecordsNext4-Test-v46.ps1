param(
    [string]$ProjectRoot = 'D:\DEV_APPS\RecordsNext4.0_TEST_v37',
    [string]$UcanAccessRoot = 'D:\DEV_APPS\RecordsNext2.0\release\RecordsNext_3.2.0_FULL\payload\runtime\ucanaccess'
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$ProjectRoot = [System.IO.Path]::GetFullPath($ProjectRoot)
if ($ProjectRoot.TrimEnd('\') -ine 'D:\DEV_APPS\RecordsNext4.0_TEST_v37') {
    throw 'Per sicurezza questo launcher avvia soltanto RecordsNext4.0_TEST_v37.'
}
$jar = Join-Path $ProjectRoot 'target\RecordsNext.jar'
if (-not (Test-Path -LiteralPath $jar -PathType Leaf)) {throw "JAR di sviluppo non trovato: $jar"}
if (-not (Test-Path -LiteralPath $UcanAccessRoot -PathType Container)) {throw "Runtime UCanAccess non trovato: $UcanAccessRoot"}
$dependencies = @(Get-ChildItem -LiteralPath $UcanAccessRoot -File -Filter '*.jar' -Recurse)
if (-not (@($dependencies | Where-Object { $_.Name -eq 'ucanaccess-2.0.9.5.jar' }).Count -gt 0)) {
    throw 'ucanaccess-2.0.9.5.jar assente nel runtime selezionato.'
}
$cp = (@($jar) + @($dependencies | ForEach-Object { $_.FullName })) -join ';'
Write-Host "RecordsNext 4.0 TEST -> $jar"
Write-Host "UCanAccess -> $UcanAccessRoot ($($dependencies.Count) jar)"
Push-Location $ProjectRoot
try {
    & java -cp $cp it.alterlega.recordsnext.gui.RecordsNext2Dashboard
    if ($LASTEXITCODE -ne 0) {throw "GUI terminata con codice $LASTEXITCODE"}
} finally { Pop-Location }
