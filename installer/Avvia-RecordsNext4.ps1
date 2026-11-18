$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot
$jar = Join-Path $root 'RecordsNext.jar'
if (-not (Test-Path -LiteralPath $jar -PathType Leaf)) { throw 'JAR mancante' }
$jars = @(Get-ChildItem (Join-Path $root 'runtime\ucanaccess') -Filter '*.jar' -File -Recurse)
if ($jars.Count -lt 5) { throw 'Runtime UCanAccess incompleto' }
$cp = (@($jar) + @($jars | ForEach-Object FullName)) -join ';'
Push-Location $root
try {
    & java -cp $cp it.alterlega.recordsnext.gui.RecordsNext2Dashboard
    if ($LASTEXITCODE -ne 0) { throw "Java exit code: $LASTEXITCODE" }
} finally { Pop-Location }
