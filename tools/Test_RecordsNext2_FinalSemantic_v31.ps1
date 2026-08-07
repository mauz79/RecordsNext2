param(
    [string]$ProjectDir = "D:\DEV_APPS\RecordsNext2.0"
)

$ErrorActionPreference = "Stop"
Set-Location $ProjectDir

$StagingRoot = Join-Path $ProjectDir "data\site-export-staging"
$OutDir = Join-Path $ProjectDir "reports\semantic-audit"
$OutCsv = Join-Path $OutDir "RecordsNext2_CULOMETRO_LEAGUE_v31.csv"

New-Item -ItemType Directory -Path $OutDir -Force | Out-Null
$Problems = New-Object System.Collections.ArrayList

function Add-Problem(
    [string]$Section,
    [string]$Key,
    [string]$Problem,
    [object]$Expected,
    [object]$Actual
) {
    [void]$Problems.Add([pscustomobject]@{
        Sezione = $Section
        Chiave = $Key
        Problema = $Problem
        Atteso = $Expected
        Reale = $Actual
    })
}

function Num([object]$Value) {
    if ($null -eq $Value -or [string]::IsNullOrWhiteSpace([string]$Value)) {
        return 0.0
    }
    return [double]([string]$Value).Replace(",", ".")
}

function Same([object]$A, [object]$B, [double]$Tolerance = 0.000001) {
    return [math]::Abs((Num $A) - (Num $B)) -le $Tolerance
}

function Prop([object]$Object, [string]$Name) {
    if ($null -eq $Object) {
        return $null
    }
    $property = $Object.PSObject.Properties[$Name]
    if ($null -eq $property) {
        return $null
    }
    return $property.Value
}

function Parse-Js([string]$Path, [string]$Prefix) {
    if (-not (Test-Path -LiteralPath $Path)) {
        throw "File non trovato: $Path"
    }

    $text = [System.IO.File]::ReadAllText($Path).Trim()

    if (-not $text.StartsWith($Prefix)) {
        throw "Prefisso inatteso in $Path"
    }

    $json = $text.Substring($Prefix.Length).Trim()
    if ($json.EndsWith(";")) {
        $json = $json.Substring(0, $json.Length - 1)
    }

    return $json | ConvertFrom-Json
}

function Event-Key([object]$Event) {
    return ([string]$Event.seasonId + "|" + [string]$Event.competitionId + "|" + [string]$Event.matchId + "|" + [string]$Event.teamId + "|" + [string]$Event.eventType)
}

function Team-Key([object]$Event) {
    return ([string]$Event.seasonId + "|" + [string]$Event.teamId)
}

function Competition-Team-Key([object]$Event) {
    return ([string]$Event.seasonId + "|" + [string]$Event.competitionId + "|" + [string]$Event.teamId)
}

$Manifest = Get-ChildItem -Path $StagingRoot -Recurse -File -Filter "fcmRecordsNext_Manifest.js" -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending | Select-Object -First 1
if ($null -eq $Manifest) {
    throw "Manifest non trovato."
}

$Dir = $Manifest.Directory.FullName
$Culometro = Parse-Js (Join-Path $Dir "fcmRecordsNext_Culometro.js") "window.fcmRecordsNextCulometro = "
$Thresholds = Parse-Js (Join-Path $Dir "fcmRecordsNext_ThresholdsLuck.js") "window.fcmRecordsNextThresholdsLuck = "
$Classics = Parse-Js (Join-Path $Dir "fcmRecordsNext_Classics.js") "window.fcmRecordsNextClassics = "

$events = @($Culometro.events | Where-Object { $null -ne $_ })
$ranking = @($Culometro.ranking | Where-Object { $null -ne $_ })
$competitionRanking = @($Culometro.competitionRanking | Where-Object { $null -ne $_ })

$secondaryWeight = Num $Culometro.configuration.secondaryWeight
$minimumMatches = [int](Num $Culometro.configuration.minimumMatches)
$kScale = Num $Culometro.configuration.kScale

# 1. Ogni contributo deve essere matematicamente corretto.
foreach ($event in $events) {
    $direction = if ([string]$event.direction -eq "FAVOURABLE") { 1.0 } elseif ([string]$event.direction -eq "UNFAVOURABLE") { -1.0 } else { 0.0 }
    $expected = (Num $event.componentWeight) * $direction * (Num $event.rarityMultiplier) * (Num $event.overlapMultiplier)

    if (-not (Same $expected $event.contribution 0.0000015)) {
        Add-Problem "Culometro.events" (Event-Key $event) "Contributo errato" $expected $event.contribution
    }

    $expectedOverlap = switch ([string]$event.level) {
        "PRIMARY" { 1.0 }
        "SECONDARY" { $secondaryWeight }
        "TAG" { 0.0 }
        default { -999.0 }
    }

    if ($expectedOverlap -eq -999.0) {
        Add-Problem "Culometro.events" (Event-Key $event) "Livello sconosciuto" "PRIMARY/SECONDARY/TAG" $event.level
    }
    elseif (-not (Same $expectedOverlap $event.overlapMultiplier)) {
        Add-Problem "Culometro.events" (Event-Key $event) "Overlap errato" $expectedOverlap $event.overlapMultiplier
    }
}

# 2. Ogni performance deve avere un PRIMARY, al massimo un SECONDARY, gli altri TAG.
foreach ($group in @($events | Group-Object { [string]$_.seasonId + "|" + [string]$_.matchId + "|" + [string]$_.teamId })) {
    $rows = @($group.Group)
    $primary = @($rows | Where-Object { [string]$_.level -eq "PRIMARY" }).Count
    $secondary = @($rows | Where-Object { [string]$_.level -eq "SECONDARY" }).Count

    if ($primary -ne 1) {
        Add-Problem "Culometro.overlap" $group.Name "Numero PRIMARY errato" 1 $primary
    }

    $expectedSecondary = if ($rows.Count -gt 1) { 1 } else { 0 }
    if ($secondary -ne $expectedSecondary) {
        Add-Problem "Culometro.overlap" $group.Name "Numero SECONDARY errato" $expectedSecondary $secondary
    }
}

# 3. Ranking generale ricostruito dagli eventi.
$teamExpected = @{}
foreach ($group in @($events | Group-Object { Team-Key $_ })) {
    $rows = @($group.Group)
    $sum = 0.0
    foreach ($row in $rows) {
        $sum += Num $row.contribution
    }

    $teamExpected[$group.Name] = [pscustomobject]@{
        total = $sum
        matches = $rows.Count
        primary = @($rows | Where-Object { [string]$_.level -eq "PRIMARY" }).Count
        secondary = @($rows | Where-Object { [string]$_.level -eq "SECONDARY" }).Count
        perMatch = if ($rows.Count -eq 0) { 0.0 } else { $sum / $rows.Count }
    }
}

$mean = 0.0
if ($teamExpected.Count -gt 0) {
    $mean = (@($teamExpected.Values | ForEach-Object { $_.perMatch }) | Measure-Object -Average).Average
}

if (-not (Same $mean $Culometro.metadata.historicalMeanPerMatch 0.0000015)) {
    Add-Problem "Culometro.metadata" "historicalMeanPerMatch" "Media storica errata" $mean $Culometro.metadata.historicalMeanPerMatch
}

$rankingMap = @{}
foreach ($row in $ranking) {
    $rankingMap[[string]$row.seasonId + "|" + [string]$row.teamId] = $row
}

foreach ($key in @($teamExpected.Keys)) {
    if (-not $rankingMap.ContainsKey($key)) {
        Add-Problem "Culometro.ranking" $key "Squadra attesa assente" "presente" "assente"
        continue
    }

    $expected = $teamExpected[$key]
    $actual = $rankingMap[$key]

    if (-not (Same $expected.total $actual.totalContribution 0.0000015)) {
        Add-Problem "Culometro.ranking" $key "totalContribution errato" $expected.total $actual.totalContribution
    }

    if ([int]$expected.matches -ne [int](Num $actual.matches)) {
        Add-Problem "Culometro.ranking" $key "matches errato" $expected.matches $actual.matches
    }

    if ([int]$expected.primary -ne [int](Num $actual.primaryEvents)) {
        Add-Problem "Culometro.ranking" $key "primaryEvents errato" $expected.primary $actual.primaryEvents
    }

    if ([int]$expected.secondary -ne [int](Num $actual.secondaryEvents)) {
        Add-Problem "Culometro.ranking" $key "secondaryEvents errato" $expected.secondary $actual.secondaryEvents
    }

    if (-not (Same $expected.perMatch $actual.perMatch 0.0000015)) {
        Add-Problem "Culometro.ranking" $key "perMatch errato" $expected.perMatch $actual.perMatch
    }

    $reliability = [math]::Min(1.0, $expected.matches / [double]$minimumMatches)
    if (-not (Same $reliability $actual.reliability 0.0000015)) {
        Add-Problem "Culometro.ranking" $key "reliability errata" $reliability $actual.reliability
    }

    $centered = $expected.perMatch - $mean
    $raw = 50.0 + 50.0 * [math]::Tanh($centered / $kScale)
    $index = 50.0 + ($raw - 50.0) * $reliability
    $index = [math]::Max(0.0, [math]::Min(100.0, $index))
    $roundedIndex = [math]::Round($index, 2, [System.MidpointRounding]::AwayFromZero)

    if (-not (Same $roundedIndex $actual.index 0.0000015)) {
        Add-Problem "Culometro.ranking" $key "Indice errato" $roundedIndex $actual.index
    }

    $label = $null
    foreach ($band in @($Culometro.configuration.labels)) {
        if ($index -ge (Num $band.min)) {
            $label = [string]$band.label
            break
        }
    }

    if ([string]$label -ne [string]$actual.label) {
        Add-Problem "Culometro.ranking" $key "Etichetta errata" $label $actual.label
    }
}

# 4. Ranking per competizione: aggregazione separata.
$competitionExpected = @{}
foreach ($group in @($events | Group-Object { Competition-Team-Key $_ })) {
    $rows = @($group.Group)
    $sum = 0.0
    foreach ($row in $rows) {
        $sum += Num $row.contribution
    }

    $competitionExpected[$group.Name] = [pscustomobject]@{
        total = $sum
        matches = $rows.Count
        perMatch = if ($rows.Count -eq 0) { 0.0 } else { $sum / $rows.Count }
    }
}

$competitionMean = 0.0
if ($competitionExpected.Count -gt 0) {
    $competitionMean = (@($competitionExpected.Values | ForEach-Object { $_.perMatch }) | Measure-Object -Average).Average
}

$competitionMap = @{}
foreach ($row in $competitionRanking) {
    $competitionMap[[string]$row.seasonId + "|" + [string]$row.competitionId + "|" + [string]$row.teamId] = $row
}

foreach ($key in @($competitionExpected.Keys)) {
    if (-not $competitionMap.ContainsKey($key)) {
        Add-Problem "Culometro.competitionRanking" $key "Squadra/competizione attesa assente" "presente" "assente"
        continue
    }

    $expected = $competitionExpected[$key]
    $actual = $competitionMap[$key]

    if (-not (Same $expected.total $actual.totalContribution 0.0000015)) {
        Add-Problem "Culometro.competitionRanking" $key "totalContribution errato" $expected.total $actual.totalContribution
    }

    $reliability = [math]::Min(1.0, $expected.matches / [double]$minimumMatches)
    $centered = $expected.perMatch - $competitionMean
    $raw = 50.0 + 50.0 * [math]::Tanh($centered / $kScale)
    $index = 50.0 + ($raw - 50.0) * $reliability
    $index = [math]::Max(0.0, [math]::Min(100.0, $index))
    $roundedIndex = [math]::Round($index, 2, [System.MidpointRounding]::AwayFromZero)

    if (-not (Same $roundedIndex $actual.index 0.0000015)) {
        Add-Problem "Culometro.competitionRanking" $key "Indice errato" $roundedIndex $actual.index
    }
}

# 5. Il numero di eventi soglia di input deve coincidere con il metadata.
$thresholdCount = @($Thresholds.events | Where-Object { $null -ne $_ }).Count
if ($thresholdCount -ne [int](Num $Culometro.metadata.thresholdEventCount)) {
    Add-Problem "Culometro.metadata" "thresholdEventCount" "Conteggio eventi soglia errato" $thresholdCount $Culometro.metadata.thresholdEventCount
}

# 6. Record di lega: il solo record Classici con direzione minima deve essere puntiSquadraMin.
$minRows = @()
foreach ($aggregate in @($Classics.seasonAggregates)) {
    $rows = @(Prop $aggregate.data.records "puntiSquadraMin" | Where-Object { $null -ne $_ })
    foreach ($row in $rows) {
        $minRows += $row
    }
}

if ($minRows.Count -eq 0) {
    Add-Problem "RecordDiLega" "puntiSquadraMin" "Nessun dato disponibile" "> 0" 0
}
else {
    $expectedAbsoluteMin = (@($minRows | ForEach-Object { Num $_.valore }) | Measure-Object -Minimum).Minimum
    $wrongAbsoluteMax = (@($minRows | ForEach-Object { Num $_.valore }) | Measure-Object -Maximum).Maximum

    if ($expectedAbsoluteMin -eq $wrongAbsoluteMax) {
        Add-Problem "RecordDiLega" "puntiSquadraMin" "Dataset non discriminante per test min/max" "min diverso da max" $expectedAbsoluteMin
    }
}

$Viewer = Join-Path $ProjectDir "release\visualizzatori\js\fcmRecordsNextFunzioni_viewer.js"
$viewerText = [System.IO.File]::ReadAllText($Viewer)

$viewerChecks = @(
    "function leagueRecordDirection(view)",
    "id === 'puntiSquadraMin'",
    "var direction = leagueRecordDirection(entry.view);",
    "direction === 'min' ? a.score - b.score : b.score - a.score"
)

foreach ($check in $viewerChecks) {
    if (-not $viewerText.Contains($check)) {
        Add-Problem "RecordDiLega" "viewer" ("Patch direzione mancante: " + $check) "presente" "assente"
    }
}

$Problems | Export-Csv -LiteralPath $OutCsv -NoTypeInformation -Encoding UTF8

$summary = @(
    [pscustomobject]@{ Blocco="Culometro - contributi"; Problemi=@($Problems | Where-Object { $_.Sezione -eq "Culometro.events" }).Count },
    [pscustomobject]@{ Blocco="Culometro - overlap"; Problemi=@($Problems | Where-Object { $_.Sezione -eq "Culometro.overlap" }).Count },
    [pscustomobject]@{ Blocco="Culometro - ranking"; Problemi=@($Problems | Where-Object { $_.Sezione -eq "Culometro.ranking" }).Count },
    [pscustomobject]@{ Blocco="Culometro - ranking competizione"; Problemi=@($Problems | Where-Object { $_.Sezione -eq "Culometro.competitionRanking" }).Count },
    [pscustomobject]@{ Blocco="Culometro - metadata"; Problemi=@($Problems | Where-Object { $_.Sezione -eq "Culometro.metadata" }).Count },
    [pscustomobject]@{ Blocco="Record di lega"; Problemi=@($Problems | Where-Object { $_.Sezione -eq "RecordDiLega" }).Count }
)

foreach ($row in $summary) {
    $row | Add-Member -NotePropertyName Esito -NotePropertyValue $(if ($row.Problemi -eq 0) { "OK" } else { "ERRORE" })
}

Write-Host ""
Write-Host "=== AUDIT FINALE CULOMETRO + RECORD DI LEGA v31 ==="
$summary | Format-Table -AutoSize
Write-Host ""
Write-Host ("Eventi Culometro             : " + $events.Count)
Write-Host ("Ranking Culometro            : " + $ranking.Count)
Write-Host ("Ranking per competizione     : " + $competitionRanking.Count)
Write-Host ("Righe puntiSquadraMin        : " + $minRows.Count)
Write-Host ("Problemi totali              : " + $Problems.Count)
Write-Host ("Dettaglio CSV                 : " + $OutCsv)

if ($Problems.Count -gt 0) {
    Write-Host ""
    Write-Host "=== PRIMI 25 PROBLEMI ==="
    $Problems | Select-Object -First 25 | Format-Table -AutoSize
    exit 1
}

Write-Host ""
Write-Host "AUDIT FINALE CULOMETRO + RECORD DI LEGA: OK"
