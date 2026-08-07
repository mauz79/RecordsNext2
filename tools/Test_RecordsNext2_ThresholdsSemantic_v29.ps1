param(
    [string]$ProjectDir = "D:\DEV_APPS\RecordsNext2.0"
)

$ErrorActionPreference = "Stop"
Set-Location $ProjectDir

$reportsRoot = Join-Path $ProjectDir "data\reports"
$stagingRoot = Join-Path $ProjectDir "data\site-export-staging"
$outDir = Join-Path $ProjectDir "reports\semantic-audit"
$outCsv = Join-Path $outDir "RecordsNext2_SOGLIE_SEMANTIC_AUDIT.csv"

New-Item -ItemType Directory -Path $outDir -Force | Out-Null

$Problems = New-Object System.Collections.ArrayList
$ExpectedEvents = New-Object System.Collections.ArrayList
$ExpectedAggregates = @{}

function Dec([object]$Value) {
    if ($null -eq $Value) {
        return [decimal]0
    }
    $text = [string]$Value
    if ([string]::IsNullOrWhiteSpace($text)) {
        return [decimal]0
    }
    $text = $text.Replace(",", ".")
    return [decimal]::Parse($text, [System.Globalization.CultureInfo]::InvariantCulture)
}

function Same-Dec([object]$A, [object]$B) {
    return [math]::Abs([double]((Dec $A) - (Dec $B))) -le 0.000001
}

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

function Property-Value([object]$Object, [string]$Name) {
    if ($null -eq $Object) {
        return $null
    }
    $property = $Object.PSObject.Properties[$Name]
    if ($null -eq $property) {
        return $null
    }
    return $property.Value
}

function Current-Band([object[]]$Bands, [decimal]$Score) {
    foreach ($band in @($Bands)) {
        $min = Dec $band.min
        $max = Dec $band.max
        if ($Score -ge $min -and $Score -le $max) {
            return $band
        }
    }
    return $null
}

function Next-Band-Min([object[]]$Bands, [decimal]$Score) {
    $found = $false
    $next = [decimal]0
    foreach ($band in @($Bands)) {
        $min = Dec $band.min
        if ($min -gt $Score) {
            if (-not $found -or $min -lt $next) {
                $next = $min
                $found = $true
            }
        }
    }
    if ($found) {
        return $next
    }
    return $null
}

function Is-Win([string]$Result) {
    return $Result -eq "V"
}

function Is-Draw([string]$Result) {
    return $Result -eq "P"
}

function Is-Loss([string]$Result) {
    return $Result -eq "S"
}

function Aggregate-Key([string]$Season, [string]$TeamId) {
    return $Season + "|" + $TeamId
}

function Event-Key([object]$Event) {
    return ([string]$Event.seasonId + "|" + [string]$Event.competitionId + "|" + [string]$Event.matchId + "|" + [string]$Event.teamId + "|" + [string]$Event.eventType)
}

function Add-Expected-Event(
    [object]$Match,
    [string]$Type,
    [string]$Direction,
    [object]$Distance,
    [decimal]$BandSurplus
) {
    $event = [pscustomobject]@{
        eventType = $Type
        direction = $Direction
        seasonId = [string]$Match.stagione
        competitionId = [string]$Match.competizioneStoricaId
        competitionName = [string]$Match.competizioneNome
        matchId = [string]$Match.idIncontro
        teamId = [string]$Match.idSquadra
        team = [string]$Match.squadra
        opponentId = [string]$Match.idAvversaria
        opponent = [string]$Match.avversaria
        scoreFor = $Match.puntiFatti
        scoreAgainst = $Match.puntiSubiti
        goalsFor = $Match.golFatti
        goalsAgainst = $Match.golSubiti
        result = [string]$Match.esito
        distanceToNextThreshold = $Distance
        unusedBandPoints = $BandSurplus
    }

    [void]$ExpectedEvents.Add($event)

    $aggregateKey = Aggregate-Key ([string]$Match.stagione) ([string]$Match.idSquadra)
    if (-not $ExpectedAggregates.ContainsKey($aggregateKey)) {
        $ExpectedAggregates[$aggregateKey] = [ordered]@{
            seasonId = [string]$Match.stagione
            teamId = [string]$Match.idSquadra
            team = [string]$Match.squadra
            favourableEvents = 0
            unfavourableEvents = 0
            neutralEvents = 0
            unusedBandPoints = [decimal]0
            eventsByType = @{}
        }
    }

    $aggregate = $ExpectedAggregates[$aggregateKey]

    if ($Direction -eq "FAVOURABLE") {
        $aggregate.favourableEvents = [int]$aggregate.favourableEvents + 1
    }
    elseif ($Direction -eq "UNFAVOURABLE") {
        $aggregate.unfavourableEvents = [int]$aggregate.unfavourableEvents + 1
    }
    else {
        $aggregate.neutralEvents = [int]$aggregate.neutralEvents + 1
    }

    if (-not $aggregate.eventsByType.ContainsKey($Type)) {
        $aggregate.eventsByType[$Type] = 0
    }
    $aggregate.eventsByType[$Type] = [int]$aggregate.eventsByType[$Type] + 1

    if ($Type -eq "UNUSED_BAND_POINTS") {
        $aggregate.unusedBandPoints = (Dec $aggregate.unusedBandPoints) + $BandSurplus
    }
}

$manifest = Get-ChildItem -Path $stagingRoot -Recurse -File -Filter "fcmRecordsNext_Manifest.js" -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending | Select-Object -First 1
if ($null -eq $manifest) {
    throw "fcmRecordsNext_Manifest.js non trovato in $stagingRoot"
}

$thresholdsJs = Join-Path $manifest.Directory.FullName "fcmRecordsNext_ThresholdsLuck.js"
if (-not (Test-Path -LiteralPath $thresholdsJs)) {
    throw "fcmRecordsNext_ThresholdsLuck.js non trovato: $thresholdsJs"
}

$js = [System.IO.File]::ReadAllText($thresholdsJs)
$prefix = "window.fcmRecordsNextThresholdsLuck = "
if (-not $js.TrimStart().StartsWith($prefix)) {
    throw "Formato fcmRecordsNext_ThresholdsLuck.js inatteso."
}

$trimmed = $js.Trim()
$json = $trimmed.Substring($prefix.Length)
if ($json.EndsWith(";")) {
    $json = $json.Substring(0, $json.Length - 1)
}
$actualRoot = $json | ConvertFrom-Json
$actualEvents = @($actualRoot.events | Where-Object { $null -ne $_ })
$actualAggregates = @($actualRoot.seasonAggregates | Where-Object { $null -ne $_ })

$normalizedFiles = @(Get-ChildItem -Path $reportsRoot -Recurse -File -Filter "season_normalized_*.json" -ErrorAction SilentlyContinue | Sort-Object FullName)
if ($normalizedFiles.Count -eq 0) {
    throw "Nessun season_normalized_*.json trovato in $reportsRoot"
}

$matchRows = 0

foreach ($file in $normalizedFiles) {
    $root = [System.IO.File]::ReadAllText($file.FullName) | ConvertFrom-Json
    $matches = @($root.partiteSquadra | Where-Object { $null -ne $_ })
    $bands = @($root.fasceGolDettaglio | Where-Object { $null -ne $_ })
    $matchRows += $matches.Count

    foreach ($match in $matches) {
        $score = Dec $match.puntiFatti
        $scoreAgainst = Dec $match.puntiSubiti
        $goalsFor = [int](Dec $match.golFatti)
        $goalsAgainst = [int](Dec $match.golSubiti)
        $result = ([string]$match.esito).Trim().ToUpperInvariant()

        $current = Current-Band $bands $score
        $nextMin = Next-Band-Min $bands $score

        $distance = $null
        if ($null -ne $nextMin) {
            $distance = (Dec $nextMin) - $score
        }

        $bandSurplus = [decimal]0
        if ($null -ne $current) {
            $bandSurplus = $score - (Dec $current.min)
        }

        if ($null -ne $current -and $score -eq (Dec $current.min)) {
            Add-Expected-Event $match "EXACT_THRESHOLD" "NEUTRAL" ([decimal]0) ([decimal]0)
        }

        if ((Is-Win $result) -and $null -ne $current -and $score -eq (Dec $current.min) -and $goalsFor -eq ($goalsAgainst + 1)) {
            Add-Expected-Event $match "JUST_ENOUGH" "FAVOURABLE" ([decimal]0) ([decimal]0)
        }

        if ($null -ne $distance -and (Dec $distance) -eq [decimal]0.5) {
            if ((Is-Draw $result) -and $goalsFor -eq $goalsAgainst) {
                Add-Expected-Event $match "MISSED_WIN_HALF_POINT" "UNFAVOURABLE" $distance $bandSurplus
            }
            elseif ((Is-Loss $result) -and ($goalsFor + 1) -eq $goalsAgainst) {
                Add-Expected-Event $match "LOSS_BY_A_WHISKER" "UNFAVOURABLE" $distance $bandSurplus
            }
        }

        if ((Is-Draw $result) -and $goalsFor -eq $goalsAgainst) {
            if ($score -lt $scoreAgainst) {
                Add-Expected-Event $match "MIRACLE_DRAW" "FAVOURABLE" $distance $bandSurplus
            }
            elseif ($score -gt $scoreAgainst) {
                Add-Expected-Event $match "TIGHT_DRAW" "UNFAVOURABLE" $distance $bandSurplus
            }
        }

        if ((Is-Win $result) -and $goalsFor -eq ($goalsAgainst + 1)) {
            Add-Expected-Event $match "ONE_GOAL_WIN" "FAVOURABLE" $distance $bandSurplus
        }
        elseif ((Is-Loss $result) -and ($goalsFor + 1) -eq $goalsAgainst) {
            Add-Expected-Event $match "ONE_GOAL_LOSS" "UNFAVOURABLE" $distance $bandSurplus
        }

        if ($null -ne $current -and $bandSurplus -gt [decimal]0) {
            Add-Expected-Event $match "UNUSED_BAND_POINTS" "NEUTRAL" $distance $bandSurplus
        }
    }
}

$expectedEventMap = @{}
foreach ($event in @($ExpectedEvents)) {
    $expectedEventMap[(Event-Key $event)] = $event
}

$actualEventMap = @{}
foreach ($event in $actualEvents) {
    $actualEventMap[(Event-Key $event)] = $event
}

if ($expectedEventMap.Count -ne $actualEventMap.Count) {
    Add-Problem "events" "(conteggio)" "Numero eventi errato" $expectedEventMap.Count $actualEventMap.Count
}

foreach ($key in @($expectedEventMap.Keys)) {
    if (-not $actualEventMap.ContainsKey($key)) {
        Add-Problem "events" $key "Evento atteso assente" "presente" "assente"
        continue
    }

    $expected = $expectedEventMap[$key]
    $actual = $actualEventMap[$key]

    foreach ($field in @("direction","competitionName","team","opponent","result")) {
        if ([string](Property-Value $expected $field) -ne [string](Property-Value $actual $field)) {
            Add-Problem "events" $key ("Campo " + $field + " errato") (Property-Value $expected $field) (Property-Value $actual $field)
        }
    }

    foreach ($field in @("scoreFor","scoreAgainst","goalsFor","goalsAgainst","unusedBandPoints")) {
        if (-not (Same-Dec (Property-Value $expected $field) (Property-Value $actual $field))) {
            Add-Problem "events" $key ("Campo " + $field + " errato") (Property-Value $expected $field) (Property-Value $actual $field)
        }
    }

    $expectedDistance = Property-Value $expected "distanceToNextThreshold"
    $actualDistance = Property-Value $actual "distanceToNextThreshold"

    if ($null -eq $expectedDistance -and $null -ne $actualDistance) {
        Add-Problem "events" $key "distanceToNextThreshold errata" "(null)" $actualDistance
    }
    elseif ($null -ne $expectedDistance -and $null -eq $actualDistance) {
        Add-Problem "events" $key "distanceToNextThreshold errata" $expectedDistance "(null)"
    }
    elseif ($null -ne $expectedDistance -and $null -ne $actualDistance -and -not (Same-Dec $expectedDistance $actualDistance)) {
        Add-Problem "events" $key "distanceToNextThreshold errata" $expectedDistance $actualDistance
    }
}

foreach ($key in @($actualEventMap.Keys)) {
    if (-not $expectedEventMap.ContainsKey($key)) {
        Add-Problem "events" $key "Evento esportato non atteso" "assente" "presente"
    }
}

$actualAggregateMap = @{}
foreach ($aggregate in $actualAggregates) {
    $key = Aggregate-Key ([string]$aggregate.seasonId) ([string]$aggregate.teamId)
    $actualAggregateMap[$key] = $aggregate
}

if ($ExpectedAggregates.Count -ne $actualAggregateMap.Count) {
    Add-Problem "seasonAggregates" "(conteggio)" "Numero aggregati squadra errato" $ExpectedAggregates.Count $actualAggregateMap.Count
}

foreach ($key in @($ExpectedAggregates.Keys)) {
    if (-not $actualAggregateMap.ContainsKey($key)) {
        Add-Problem "seasonAggregates" $key "Aggregato atteso assente" "presente" "assente"
        continue
    }

    $expected = $ExpectedAggregates[$key]
    $actual = $actualAggregateMap[$key]

    foreach ($field in @("team","favourableEvents","unfavourableEvents","neutralEvents")) {
        if ([string]$expected[$field] -ne [string](Property-Value $actual $field)) {
            Add-Problem "seasonAggregates" $key ("Campo " + $field + " errato") $expected[$field] (Property-Value $actual $field)
        }
    }

    $expectedBalance = [int]$expected.favourableEvents - [int]$expected.unfavourableEvents
    if ([int](Dec $actual.luckBalance) -ne $expectedBalance) {
        Add-Problem "seasonAggregates" $key "Campo luckBalance errato" $expectedBalance $actual.luckBalance
    }

    if (-not (Same-Dec $expected.unusedBandPoints $actual.unusedBandPoints)) {
        Add-Problem "seasonAggregates" $key "Campo unusedBandPoints errato" $expected.unusedBandPoints $actual.unusedBandPoints
    }

    $actualByType = Property-Value $actual "eventsByType"
    foreach ($type in @($expected.eventsByType.Keys)) {
        $actualValue = Property-Value $actualByType $type
        if ([int](Dec $actualValue) -ne [int]$expected.eventsByType[$type]) {
            Add-Problem "seasonAggregates" ($key + "|" + $type) "Conteggio tipo evento errato" $expected.eventsByType[$type] $actualValue
        }
    }

    foreach ($property in @($actualByType.PSObject.Properties)) {
        if (-not $expected.eventsByType.ContainsKey([string]$property.Name)) {
            Add-Problem "seasonAggregates" ($key + "|" + [string]$property.Name) "Tipo evento aggregato non atteso" 0 $property.Value
        }
    }
}

foreach ($key in @($actualAggregateMap.Keys)) {
    if (-not $ExpectedAggregates.ContainsKey($key)) {
        Add-Problem "seasonAggregates" $key "Aggregato esportato non atteso" "assente" "presente"
    }
}

$typeNames = @(
    "EXACT_THRESHOLD",
    "JUST_ENOUGH",
    "MISSED_WIN_HALF_POINT",
    "LOSS_BY_A_WHISKER",
    "MIRACLE_DRAW",
    "TIGHT_DRAW",
    "ONE_GOAL_WIN",
    "ONE_GOAL_LOSS",
    "UNUSED_BAND_POINTS"
)

$summary = @()
foreach ($type in $typeNames) {
    $expectedCount = @($ExpectedEvents | Where-Object { [string]$_.eventType -eq $type }).Count
    $actualCount = @($actualEvents | Where-Object { [string]$_.eventType -eq $type }).Count
    $problemCount = @($Problems | Where-Object { [string]$_.Chiave -like ("*|" + $type) }).Count

    $summary += [pscustomobject]@{
        Evento = $type
        Attesi = $expectedCount
        Reali = $actualCount
        Problemi = $problemCount
        Esito = if ($expectedCount -eq $actualCount -and $problemCount -eq 0) { "OK" } else { "ERRORE" }
    }
}

$Problems | Export-Csv -LiteralPath $outCsv -NoTypeInformation -Encoding UTF8

Write-Host ""
Write-Host "=== AUDIT SEMANTICO SOGLIE / FORTUNA v29 ==="
$summary | Format-Table -AutoSize
Write-Host ""
Write-Host ("File normalizzati            : " + $normalizedFiles.Count)
Write-Host ("Righe partita analizzate     : " + $matchRows)
Write-Host ("Eventi attesi                : " + $ExpectedEvents.Count)
Write-Host ("Eventi esportati             : " + $actualEvents.Count)
Write-Host ("Aggregati squadra attesi     : " + $ExpectedAggregates.Count)
Write-Host ("Aggregati squadra esportati  : " + $actualAggregates.Count)
Write-Host ("Problemi totali              : " + $Problems.Count)
Write-Host ("Dettaglio CSV                 : " + $outCsv)

if ($Problems.Count -gt 0) {
    Write-Host ""
    Write-Host "=== PRIMI 25 PROBLEMI ==="
    $Problems | Select-Object -First 25 | Format-Table -AutoSize
    exit 1
}

Write-Host ""
Write-Host "AUDIT SEMANTICO SOGLIE / FORTUNA: OK"
