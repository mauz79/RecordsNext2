param(
    [string]$ProjectDir = "D:\DEV_APPS\RecordsNext2.0"
)

$ErrorActionPreference = "Stop"
Set-Location $ProjectDir

$configPath = Join-Path $ProjectDir "config\processing.json"
$config = Get-Content -LiteralPath $configPath -Raw | ConvertFrom-Json

$manifest = Get-ChildItem `
    -Path (Join-Path $ProjectDir "data\site-export-staging") `
    -Recurse -File -Filter "fcmRecordsNext_Manifest.js" |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1

if (-not $manifest) {
    throw "Manifest JS non trovato."
}

$jsDir = $manifest.Directory.FullName

$classicsMap = [ordered]@{
    "highest-match-score"       = "puntiSquadraMax"
    "lowest-match-score"        = "puntiSquadraMin"
    "most-regulation-goals"     = "partitePiuGolRegolamentari"
    "largest-regulation-margin" = "partitePiuScartoRegolamentari"
    "average-points"            = "mediaPuntiSquadre"
    "total-points"              = "totalePuntiSquadre"
    "standings-points"          = "puntiClassificaSquadre"
    "wins"                      = "vittorieSquadre"
    "draws"                     = "pareggiSquadre"
    "losses"                    = "sconfitteSquadre"
    "goals-for"                 = "golFattiSquadre"
    "goals-against"             = "golSubitiSquadre"
    "yellow-cards-team"         = "ammonizioniSquadre"
    "red-cards-team"            = "espulsioniSquadre"
    "red-cards-player"          = "espulsioniGiocatori"
    "assists-team"              = "assistSquadre"
    "own-goals-team"            = "autogolSquadre"
    "penalties-scored"          = "golRigoreSquadre"
    "penalties-missed"          = "rigoriSbagliatiSquadre"
    "penalties-saved"           = "rigoriParatiSquadre"
    "clean-sheets"              = "cleanSheetPortiereVolteSquadre"
}

$seriesMap = [ordered]@{
    "unbeaten"     = "serieSenzaSconfitte"
    "winless"      = "serieSenzaVittorie"
    "wins"         = "serieVittorie"
    "draws"        = "seriePareggi"
    "losses"       = "serieSconfitte"
    "clean-sheets" = "cleanSheetPortiereSerieSquadre"
}

$modifierSeriesMap = [ordered]@{
    "modm1pers.series"      = "modDifesaSerieSquadre"
    "modm2pers.series"      = "capitanoSerieSquadre"
    "modm3pers.series"      = "modPersonalizzato3SerieSquadre"
    "modportiere.series"    = "modPortiereFcmSerieSquadre"
    "moddifesa.series"      = "modDifesaFcmSerieSquadre"
    "modcentrocampo.series" = "modCentrocampoFcmSerieSquadre"
    "modattacco.series"     = "modAttaccoFcmSerieSquadre"
    "modmodulo.series"      = "modModuloFcmSerieSquadre"
}

function Test-FamilyMap {
    param(
        [string]$Family,
        [object]$Children,
        [hashtable]$Map,
        [string]$JsFile
    )

    $text = Get-Content -LiteralPath $JsFile -Raw
    $rows = foreach ($key in $Map.Keys) {
        $selected = [bool]$Children.$key
        $section = $Map[$key]
        $present = $text.Contains('"' + $section + '":[')

        [pscustomobject]@{
            Famiglia = $Family
            Checkbox = $key
            Selezionata = $selected
            Sezione = $section
            Presente = $present
            Esito = if ($selected -eq $present) { "OK" } else { "ERRORE" }
        }
    }
    return $rows
}

$families = $config.processing.families
$rows = @()

$rows += Test-FamilyMap `
    -Family "Classici" `
    -Children $families.classics.children `
    -Map $classicsMap `
    -JsFile (Join-Path $jsDir "fcmRecordsNext_Classics.js")

$rows += Test-FamilyMap `
    -Family "Serie" `
    -Children $families.series.children `
    -Map $seriesMap `
    -JsFile (Join-Path $jsDir "fcmRecordsNext_Series.js")

$rows += Test-FamilyMap `
    -Family "Serie modificatori" `
    -Children $families.modifiers.children `
    -Map $modifierSeriesMap `
    -JsFile (Join-Path $jsDir "fcmRecordsNext_Series.js")

$rows | Format-Table -AutoSize

$errors = @($rows | Where-Object Esito -eq "ERRORE")
Write-Host ""
Write-Host "Checkbox controllate : $($rows.Count)"
Write-Host "Errori               : $($errors.Count)"

if ($errors.Count -gt 0) {
    Write-Host ""
    Write-Host "=== ERRORI CONTRATTO CHECKBOX -> VISTA ==="
    $errors | Format-Table -AutoSize
    exit 1
}

Write-Host ""
Write-Host "CONTRATTO CHECKBOX -> VISTA: OK"
