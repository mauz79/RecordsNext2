param(
    [string]$ProjectDir = "D:\DEV_APPS\RecordsNext2.0",
    [string]$InstalledRoot = "E:\FCM\plugin\Mauz_strom2014Full\RecordsNext-1.0.0"
)

$ErrorActionPreference = "Stop"

Set-Location $ProjectDir

& "$ProjectDir\mvnw.cmd" dependency:build-classpath `
    "-Dmdep.outputFile=target\maven-classpath.txt"

if ($LASTEXITCODE -ne 0) {
    throw "Impossibile costruire il classpath Maven."
}

$mavenCp = Get-Content `
    "$ProjectDir\target\maven-classpath.txt" `
    -Raw

$ucanRoot = Join-Path $InstalledRoot "runtime\ucanaccess"

$required = @(
    "$ucanRoot\ucanaccess-2.0.9.5.jar"
    "$ucanRoot\lib\jackcess-2.1.0.jar"
    "$ucanRoot\lib\hsqldb.jar"
    "$ucanRoot\lib\commons-lang-2.6.jar"
    "$ucanRoot\lib\commons-logging-1.1.1.jar"
)

$missing = $required | Where-Object { -not (Test-Path -LiteralPath $_) }

if ($missing.Count -gt 0) {
    $message = "Mancano i seguenti file UCanAccess:`r`n" + ($missing -join "`r`n")
    throw $message
}

$ucanCp = $required -join ";"

$fullCp = @(
    "$ProjectDir\target\classes"
    $mavenCp.Trim()
    $ucanCp
) -join ";"

java `
    -cp $fullCp `
    it.alterlega.recordsnext.gui.RecordsNext2Dashboard
