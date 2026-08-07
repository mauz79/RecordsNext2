param(
    [string]$ProjectDir = "D:\DEV_APPS\RecordsNext2.0"
)

$ErrorActionPreference = "Stop"
Set-Location $ProjectDir

$configPath = Join-Path $ProjectDir "config\processing.json"
if (-not (Test-Path -LiteralPath $configPath)) {
    throw "Configurazione non trovata: $configPath"
}
$config = Get-Content -LiteralPath $configPath -Raw | ConvertFrom-Json
$families = $config.processing.families

$manifest = Get-ChildItem `
    -Path (Join-Path $ProjectDir "data\site-export-staging") `
    -Recurse -File -Filter "fcmRecordsNext_Manifest.js" `
    -ErrorAction SilentlyContinue |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1

if (-not $manifest) {
    throw "Manifest JS non trovato sotto data\site-export-staging."
}
$jsDir = $manifest.Directory.FullName

function Read-Js([string]$Name) {
    $path = Join-Path $jsDir $Name
    if (-not (Test-Path -LiteralPath $path)) { return "" }
    return Get-Content -LiteralPath $path -Raw
}

$classicsJs = Read-Js "fcmRecordsNext_Classics.js"
$seriesJs = Read-Js "fcmRecordsNext_Series.js"
$ruJs = Read-Js "fcmRecordsNext_RU.js"
$modifiersJs = Read-Js "fcmRecordsNext_Modifiers.js"
$thresholdsJs = Read-Js "fcmRecordsNext_ThresholdsLuck.js"
$culometroJs = Read-Js "fcmRecordsNext_Culometro.js"

$rows = New-Object System.Collections.Generic.List[object]

function Add-Check {
    param(
        [string]$Family,
        [string]$Checkbox,
        [bool]$Selected,
        [string]$Expected,
        [bool]$Present,
        [string]$Kind = "Sezione"
    )
    $ok = ($Selected -eq $Present)
    $rows.Add([pscustomobject]@{
        Famiglia = $Family
        Checkbox = $Checkbox
        Selezionata = $Selected
        Tipo = $Kind
        Atteso = $Expected
        Presente = $Present
        Esito = if ($ok) { "OK" } else { "ERRORE" }
    })
}

function Child-Selected($familyChildren, [string]$key) {
    if ($null -eq $familyChildren) { return $false }
    $property = $familyChildren.PSObject.Properties[$key]
    if ($null -eq $property) { return $false }
    return [bool]$property.Value
}

# CLASSICI 21
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
foreach ($key in $classicsMap.Keys) {
    $section = $classicsMap[$key]
    Add-Check "Classici" $key (Child-Selected $families.classics.children $key) $section ($classicsJs.Contains('"' + $section + '":['))
}

# SERIE 6
$seriesMap = [ordered]@{
    "wins" = "serieVittorie"
    "draws" = "seriePareggi"
    "losses" = "serieSconfitte"
    "unbeaten" = "serieSenzaSconfitte"
    "winless" = "serieSenzaVittorie"
    "clean-sheets" = "cleanSheetPortiereSerieSquadre"
}
foreach ($key in $seriesMap.Keys) {
    $section = $seriesMap[$key]
    Add-Check "Serie" $key (Child-Selected $families.series.children $key) $section ($seriesJs.Contains('"' + $section + '":['))
}

# SERIE MODIFICATORI 8
$modifierSeriesMap = [ordered]@{
    "modm1pers.series" = "modDifesaSerieSquadre"
    "modm2pers.series" = "capitanoSerieSquadre"
    "modm3pers.series" = "modPersonalizzato3SerieSquadre"
    "modportiere.series" = "modPortiereFcmSerieSquadre"
    "moddifesa.series" = "modDifesaFcmSerieSquadre"
    "modcentrocampo.series" = "modCentrocampoFcmSerieSquadre"
    "modattacco.series" = "modAttaccoFcmSerieSquadre"
    "modmodulo.series" = "modModuloFcmSerieSquadre"
}
foreach ($key in $modifierSeriesMap.Keys) {
    $section = $modifierSeriesMap[$key]
    Add-Check "Serie modificatori" $key (Child-Selected $families.modifiers.children $key) $section ($seriesJs.Contains('"' + $section + '":['))
}

# MODIFICATORI: max/totale/media/utilizzi + Fattore Campo
$modifierPrefixes = [ordered]@{
    "modm1pers" = "modDifesa"
    "modm2pers" = "capitano"
    "modm3pers" = "modPersonalizzato3"
    "modportiere" = "modPortiereFcm"
    "moddifesa" = "modDifesaFcm"
    "modcentrocampo" = "modCentrocampoFcm"
    "modattacco" = "modAttaccoFcm"
    "modmodulo" = "modModuloFcm"
}
$modifierSuffix = [ordered]@{
    "max" = "Max"
    "total" = "TotaleSquadre"
    "average" = "MediaSquadre"
    "uses" = "UtilizziSquadre"
}
foreach ($mod in $modifierPrefixes.Keys) {
    foreach ($metric in $modifierSuffix.Keys) {
        $key = $mod + "." + $metric
        $section = $modifierPrefixes[$mod] + $modifierSuffix[$metric]
        Add-Check "Modificatori" $key (Child-Selected $families.modifiers.children $key) $section ($modifiersJs.Contains('"' + $section + '":['))
    }
}
$homeFieldMap = [ordered]@{
    "home-field-deciding" = "fattoreCampoDecisivo"
    "home-field-points-gained" = "fattoreCampoPuntiGuadagnatiSquadre"
    "home-field-points-lost" = "fattoreCampoPuntiPersiSquadre"
    "home-field-balance" = "fattoreCampoTotaleSquadre"
}
foreach ($key in $homeFieldMap.Keys) {
    $section = $homeFieldMap[$key]
    Add-Check "Fattore Campo" $key (Child-Selected $families.modifiers.children $key) $section ($modifiersJs.Contains('"' + $section + '":['))
}

# RU: una checkbox -> una vista canonica.
$ruMap = [ordered]@{
    "max-in-match" = "partiteConPiuRU"
    "matches-with" = "partiteConRU"
    "matches-against" = "partiteControRU"
    "deciding" = "ruDecisiva"
    "deciding-against" = "ruDecisivaContro"
    "balance" = "bilancioConRU"
    "balance-against" = "bilancioControRU"
    "average-points" = "mediaPuntiConRU"
    "average-points-against" = "mediaPuntiControRU"
    "role-distribution" = "tipoRUUsata"
}
foreach ($key in $ruMap.Keys) {
    $section = $ruMap[$key]
    Add-Check "RU" $key (Child-Selected $families.ru.children $key) $section ($ruJs.Contains('"' + $section + '":')) "Vista"
}

# Evidenzia viste RU prodotte ma senza checkbox dedicata.
$ruExtra = @("bilancioRUDecisiva", "bilancioRUDecisivaContro")
foreach ($section in $ruExtra) {
    if ($ruJs.Contains('"' + $section + '":')) {
        $rows.Add([pscustomobject]@{
            Famiglia = "RU"
            Checkbox = "(nessuna)"
            Selezionata = $false
            Tipo = "Vista extra"
            Atteso = $section
            Presente = $true
            Esito = "EXTRA"
        })
    }
}

# SOGLIE/FORTUNA: tipo evento o aggregato corrispondente.
$thresholdEventMap = [ordered]@{
    "surgical-win" = "ONE_GOAL_WIN"
    "mocking-loss" = "ONE_GOAL_LOSS"
    "miraculous-draw" = "MIRACLE_DRAW"
    "narrow-draw" = "TIGHT_DRAW"
    "missed-win-half-point" = "MISSED_WIN_HALF_POINT"
    "loss-by-a-whisker" = "LOSS_BY_A_WHISKER"
    "exact-threshold" = "EXACT_THRESHOLD"
    "just-enough" = "JUST_ENOUGH"
    "wasted-points" = "UNUSED_BAND_POINTS"
}
foreach ($key in $thresholdEventMap.Keys) {
    $eventType = $thresholdEventMap[$key]
    $selected = Child-Selected $families.thresholdsLuck.children $key
    # implementedEventTypes e' il contratto: la vista deve poter esistere anche con 0 occorrenze.
    $present = $thresholdsJs.Contains('"' + $eventType + '"')
    Add-Check "Soglie/Fortuna" $key $selected $eventType $present "Tipo evento"
}
$luckMap = [ordered]@{
    "favourable-events" = "favourableEvents"
    "unfavourable-events" = "unfavourableEvents"
    "balance" = "luckBalance"
}
foreach ($key in $luckMap.Keys) {
    $field = $luckMap[$key]
    Add-Check "Soglie/Fortuna" $key (Child-Selected $families.thresholdsLuck.children $key) $field ($thresholdsJs.Contains('"' + $field + '"')) "Aggregato"
}

# CULOMETRO: unica checkbox dedicata in config/culometro.json.
$culometroConfigPath = Join-Path $ProjectDir "config\culometro.json"
if (Test-Path -LiteralPath $culometroConfigPath) {
    $culoConfig = Get-Content -LiteralPath $culometroConfigPath -Raw | ConvertFrom-Json
    $culoEnabled = [bool]$culoConfig.enabled
    $culoPresent = -not [string]::IsNullOrWhiteSpace($culometroJs)
    Add-Check "Culometro" "enabled" $culoEnabled "fcmRecordsNext_Culometro.js" $culoPresent "Famiglia"
}

# Report di sintesi.
$reportDir = Join-Path $ProjectDir "reports\checkbox-view-audit"
New-Item -ItemType Directory -Path $reportDir -Force | Out-Null
$csvPath = Join-Path $reportDir "RecordsNext2_CHECKBOX_VIEW_AUDIT.csv"
$rows | Export-Csv -LiteralPath $csvPath -NoTypeInformation -Encoding UTF8

$summary = $rows |
    Group-Object Famiglia |
    ForEach-Object {
        $group = @($_.Group)
        [pscustomobject]@{
            Famiglia = $_.Name
            Checkbox = @($group | Where-Object Checkbox -ne "(nessuna)").Count
            Selezionate = @($group | Where-Object { $_.Checkbox -ne "(nessuna)" -and $_.Selezionata }).Count
            Presenti = @($group | Where-Object { $_.Checkbox -ne "(nessuna)" -and $_.Presente }).Count
            Errori = @($group | Where-Object Esito -eq "ERRORE").Count
            Extra = @($group | Where-Object Esito -eq "EXTRA").Count
        }
    }

Write-Host ""
Write-Host "=== CONTRATTO CHECKBOX -> VISTA ==="
$summary | Format-Table -AutoSize

$errors = @($rows | Where-Object { $_.Esito -eq "ERRORE" -or $_.Esito -eq "EXTRA" })
if ($errors.Count -gt 0) {
    Write-Host ""
    Write-Host "=== PROBLEMI ==="
    $errors | Format-Table Famiglia, Checkbox, Selezionata, Tipo, Atteso, Presente, Esito -AutoSize
}

Write-Host ""
Write-Host "Audit dettagliato: $csvPath"
Write-Host "Controlli totali : $($rows.Count)"
Write-Host "Problemi         : $($errors.Count)"

if ($errors.Count -gt 0) { exit 1 }
Write-Host "CONTRATTO CHECKBOX -> VISTA: OK"
