param(
    [string]$ProjectDir = "D:\DEV_APPS\RecordsNext2.0",
    [string]$JsDir = "",
    [string]$OutputDir = ""
)

$ErrorActionPreference = "Stop"

function Get-PropertyValue {
    param($Object, [string[]]$Names)
    if ($null -eq $Object) { return $null }
    foreach ($name in $Names) {
        $property = $Object.PSObject.Properties[$name]
        if ($null -ne $property -and $null -ne $property.Value -and -not [string]::IsNullOrWhiteSpace([string]$property.Value)) {
            return $property.Value
        }
    }
    return $null
}

function Add-UniqueValue {
    param([System.Collections.Generic.HashSet[string]]$Set, $Value)
    if ($null -ne $Value -and -not [string]::IsNullOrWhiteSpace([string]$Value)) {
        [void]$Set.Add([string]$Value)
    }
}

function Read-RecordsNextJs {
    param([string]$Path)

    $text = [System.IO.File]::ReadAllText($Path, [System.Text.Encoding]::UTF8).Trim([char]0xFEFF)
    $match = [regex]::Match($text, '(?s)^\s*window\.([A-Za-z0-9_]+)\s*=\s*(\{.*\})\s*;?\s*$')
    if (-not $match.Success) {
        throw "Formato window.<variabile> = {...}; non riconosciuto"
    }

    return [pscustomobject]@{
        Variable = $match.Groups[1].Value
        Data = ($match.Groups[2].Value | ConvertFrom-Json)
    }
}

function Visit-Node {
    param(
        $Node,
        [string]$InheritedSeason,
        [string]$InheritedCompetition,
        [hashtable]$Stats
    )

    if ($null -eq $Node) { return }

    if ($Node -is [string] -or $Node -is [ValueType]) { return }

    if ($Node -is [System.Collections.IEnumerable] -and -not ($Node -is [pscustomobject]) -and -not ($Node -is [hashtable])) {
        $count = 0
        foreach ($item in $Node) {
            $count++
            Visit-Node -Node $item -InheritedSeason $InheritedSeason -InheritedCompetition $InheritedCompetition -Stats $Stats
        }
        $Stats.Arrays++
        return
    }

    $Stats.Objects++

    $season = Get-PropertyValue -Object $Node -Names @('stagione','seasonId','season')
    if ($null -eq $season) { $season = $InheritedSeason }

    $competition = Get-PropertyValue -Object $Node -Names @('competizioneNome','competitionName','competizioneStoricaId','competitionId','competizione')
    if ($null -eq $competition) { $competition = $InheritedCompetition }

    Add-UniqueValue -Set $Stats.Seasons -Value $season
    Add-UniqueValue -Set $Stats.Competitions -Value $competition

    if ($null -ne (Get-PropertyValue -Object $Node -Names @('urlTabellino','scorecardUrl'))) {
        $Stats.ScorecardLinks++
    }

    Add-UniqueValue -Set $Stats.RecordIds -Value (Get-PropertyValue -Object $Node -Names @('recordId'))
    Add-UniqueValue -Set $Stats.EventTypes -Value (Get-PropertyValue -Object $Node -Names @('eventType'))

    foreach ($property in $Node.PSObject.Properties) {
        Visit-Node -Node $property.Value -InheritedSeason ([string]$season) -InheritedCompetition ([string]$competition) -Stats $Stats
    }
}

function Get-TopLevelCount {
    param($Data, [string]$Name)
    $property = $Data.PSObject.Properties[$Name]
    if ($null -eq $property -or $null -eq $property.Value) { return 0 }
    if ($property.Value -is [System.Array]) { return $property.Value.Count }
    return 1
}

if ([string]::IsNullOrWhiteSpace($JsDir)) {
    $stagingRoot = Join-Path $ProjectDir "data\site-export-staging"
    if (-not (Test-Path -LiteralPath $stagingRoot)) {
        throw "Cartella staging non trovata: $stagingRoot"
    }

    $latest = Get-ChildItem -LiteralPath $stagingRoot -Directory |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1

    if ($null -eq $latest) {
        throw "Nessuna elaborazione trovata in $stagingRoot"
    }

    $JsDir = Join-Path $latest.FullName "js"
}

if (-not (Test-Path -LiteralPath $JsDir)) {
    throw "Cartella JS non trovata: $JsDir"
}

if ([string]::IsNullOrWhiteSpace($OutputDir)) {
    $OutputDir = Join-Path $ProjectDir "reports\js-audit"
}

New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null

$files = Get-ChildItem -LiteralPath $JsDir -File |
    Where-Object { $_.Name -match '^fcmRecordsNext_.*\.js$' } |
    Sort-Object Name

if ($files.Count -eq 0) {
    throw "Nessun file fcmRecordsNext_*.js trovato in $JsDir"
}

$loaded = @{}
$errors = @()
foreach ($file in $files) {
    try {
        $loaded[$file.Name] = Read-RecordsNextJs -Path $file.FullName
    }
    catch {
        $errors += [pscustomobject]@{ file = $file.Name; error = $_.Exception.Message }
    }
}

$expectedSeasons = New-Object 'System.Collections.Generic.HashSet[string]'
if ($loaded.ContainsKey('fcmRecordsNext_Core.js')) {
    $core = $loaded['fcmRecordsNext_Core.js'].Data
    $seasonProperty = $core.PSObject.Properties['seasons']
    if ($null -ne $seasonProperty) {
        foreach ($seasonItem in @($seasonProperty.Value)) {
            Add-UniqueValue -Set $expectedSeasons -Value (Get-PropertyValue -Object $seasonItem -Names @('stagione','seasonId','season'))
        }
    }
}

$expectedSeasonArray = @($expectedSeasons | Sort-Object)
$rows = @()

foreach ($fileName in ($loaded.Keys | Sort-Object)) {
    $entry = $loaded[$fileName]
    $data = $entry.Data
    $stats = @{
        Arrays = 0
        Objects = 0
        Seasons = New-Object 'System.Collections.Generic.HashSet[string]'
        Competitions = New-Object 'System.Collections.Generic.HashSet[string]'
        RecordIds = New-Object 'System.Collections.Generic.HashSet[string]'
        EventTypes = New-Object 'System.Collections.Generic.HashSet[string]'
        ScorecardLinks = 0
    }

    Visit-Node -Node $data -InheritedSeason $null -InheritedCompetition $null -Stats $stats

    $seasons = @($stats.Seasons | Sort-Object)
    $competitions = @($stats.Competitions | Sort-Object)
    $recordIds = @($stats.RecordIds | Sort-Object)
    $eventTypes = @($stats.EventTypes | Sort-Object)
    $missing = @($expectedSeasonArray | Where-Object { $_ -notin $seasons })

    $statusParts = @()
    $outputStatusProperty = $data.PSObject.Properties['outputStatus']
    if ($null -ne $outputStatusProperty) {
        foreach ($statusItem in @($outputStatusProperty.Value)) {
            $statusValue = Get-PropertyValue -Object $statusItem -Names @('status','code')
            if ($null -ne $statusValue) { $statusParts += [string]$statusValue }
        }
    }
    $statusText = if ($statusParts.Count -gt 0) { $statusParts -join ', ' } else { 'NON_DICHIARATO' }

    $familyId = Get-PropertyValue -Object $data -Names @('familyId')
    if ($null -eq $familyId) {
        if ($fileName -like '*Core*') { $familyId = 'core' }
        elseif ($fileName -like '*Manifest*') { $familyId = 'manifest' }
        else { $familyId = '' }
    }

    $declaredSeasonCount = $null
    $metadataProperty = $data.PSObject.Properties['metadata']
    if ($null -ne $metadataProperty -and $null -ne $metadataProperty.Value) {
        $declaredSeasonCount = Get-PropertyValue -Object $metadataProperty.Value -Names @('seasonCount')
    }

    $rows += [pscustomobject]@{
        File = $fileName
        Variable = $entry.Variable
        FamilyId = [string]$familyId
        SchemaVersion = [string](Get-PropertyValue -Object $data -Names @('schemaVersion'))
        Status = $statusText
        DeclaredSeasonCount = $declaredSeasonCount
        ActualSeasonCount = $seasons.Count
        ExpectedSeasonCount = $expectedSeasonArray.Count
        Seasons = $seasons
        MissingSeasons = $missing
        CompetitionCount = $competitions.Count
        Competitions = $competitions
        Events = Get-TopLevelCount -Data $data -Name 'events'
        SeasonAggregates = Get-TopLevelCount -Data $data -Name 'seasonAggregates'
        GlobalAggregates = Get-TopLevelCount -Data $data -Name 'globalAggregates'
        AbsoluteOccurrences = Get-TopLevelCount -Data $data -Name 'absoluteOccurrences'
        Ranking = Get-TopLevelCount -Data $data -Name 'ranking'
        RecordIdCount = $recordIds.Count
        RecordIds = $recordIds
        EventTypeCount = $eventTypes.Count
        EventTypes = $eventTypes
        ScorecardLinks = $stats.ScorecardLinks
    }
}

$generatedAt = (Get-Date).ToString('o')
$jsonPath = Join-Path $OutputDir 'RecordsNext2_JS_AUDIT.json'
$csvPath = Join-Path $OutputDir 'RecordsNext2_JS_AUDIT.csv'
$mdPath = Join-Path $OutputDir 'RecordsNext2_JS_AUDIT.md'

$audit = [pscustomobject]@{
    generatedAt = $generatedAt
    sourceDirectory = (Resolve-Path -LiteralPath $JsDir).Path
    expectedSeasons = $expectedSeasonArray
    filesFound = $files.Count
    filesLoaded = $rows.Count
    errors = $errors
    rows = $rows
}

$json = $audit | ConvertTo-Json -Depth 100
[System.IO.File]::WriteAllText($jsonPath, $json, (New-Object System.Text.UTF8Encoding($false)))

$csvRows = $rows | Select-Object `
    File, FamilyId, Status, DeclaredSeasonCount, ActualSeasonCount, ExpectedSeasonCount,
    @{Name='MissingSeasons';Expression={$_.MissingSeasons -join ','}},
    CompetitionCount, Events, SeasonAggregates, Ranking, RecordIdCount, EventTypeCount, ScorecardLinks
$csvText = $csvRows | ConvertTo-Csv -Delimiter ';' -NoTypeInformation
[System.IO.File]::WriteAllLines($csvPath, $csvText, (New-Object System.Text.UTF8Encoding($true)))

$lines = New-Object System.Collections.Generic.List[string]
$lines.Add('# Audit JS RecordsNext 2.0')
$lines.Add('')
$lines.Add("Generato: $generatedAt")
$lines.Add('')
$lines.Add("Cartella analizzata: `$((Resolve-Path -LiteralPath $JsDir).Path)`")
$lines.Add('')
$lines.Add("Stagioni attese dal Core: **$($expectedSeasonArray.Count)** ($($expectedSeasonArray -join ', '))")
$lines.Add('')
$lines.Add('## Riepilogo')
$lines.Add('')
$lines.Add('| File | Famiglia | Stato | Stagioni reali/attese | Mancanti | Competizioni | Eventi | Aggregati | Ranking | Link |')
$lines.Add('|---|---|---|---:|---|---:|---:|---:|---:|---:|')
foreach ($row in $rows) {
    $missingText = if ($row.MissingSeasons.Count -gt 0) { $row.MissingSeasons -join ', ' } else { '—' }
    $lines.Add("| $($row.File) | $($row.FamilyId) | $($row.Status) | $($row.ActualSeasonCount)/$($row.ExpectedSeasonCount) | $missingText | $($row.CompetitionCount) | $($row.Events) | $($row.SeasonAggregates) | $($row.Ranking) | $($row.ScorecardLinks) |")
}

$lines.Add('')
$lines.Add('## Anomalie rilevate')
$lines.Add('')
$anomalyCount = 0
foreach ($row in $rows) {
    if ($row.ExpectedSeasonCount -gt 0 -and $row.ActualSeasonCount -gt 0 -and $row.ActualSeasonCount -lt $row.ExpectedSeasonCount) {
        $lines.Add("- **$($row.File)** copre $($row.ActualSeasonCount)/$($row.ExpectedSeasonCount) stagioni. Mancano: $($row.MissingSeasons -join ', ').")
        $anomalyCount++
    }
    if ($row.Status -match 'PARTIAL|SKIPPED|ERROR') {
        $lines.Add("- **$($row.File)** dichiara stato: $($row.Status).")
        $anomalyCount++
    }
    if ($null -ne $row.DeclaredSeasonCount -and [int]$row.DeclaredSeasonCount -ne $row.ActualSeasonCount) {
        $lines.Add("- **$($row.File)** dichiara seasonCount=$($row.DeclaredSeasonCount), ma nel contenuto risultano $($row.ActualSeasonCount) stagioni.")
        $anomalyCount++
    }
}
if ($anomalyCount -eq 0) {
    $lines.Add('Nessuna anomalia automatica rilevata.')
}

$lines.Add('')
$lines.Add('## Dettaglio per file')
foreach ($row in $rows) {
    $lines.Add('')
    $lines.Add("### $($row.File)")
    $lines.Add('')
    $lines.Add("- Variabile globale: ``window.$($row.Variable)``")
    $lines.Add("- Schema: $($row.SchemaVersion)")
    $lines.Add("- Stato: $($row.Status)")
    $lines.Add("- Stagioni: $($row.Seasons -join ', ')")
    $lines.Add("- Competizioni ($($row.CompetitionCount)): $($row.Competitions -join ', ')")
    $lines.Add("- Eventi: $($row.Events); aggregati stagionali: $($row.SeasonAggregates); ranking: $($row.Ranking)")
    $lines.Add("- Record ID ($($row.RecordIdCount)): $($row.RecordIds -join ', ')")
    $lines.Add("- Tipi evento ($($row.EventTypeCount)): $($row.EventTypes -join ', ')")
    $lines.Add("- Link tabellino rilevati: $($row.ScorecardLinks)")
}

if ($errors.Count -gt 0) {
    $lines.Add('')
    $lines.Add('## Errori di lettura')
    $lines.Add('')
    foreach ($errorItem in $errors) {
        $lines.Add("- $($errorItem.file): $($errorItem.error)")
    }
}

[System.IO.File]::WriteAllLines($mdPath, $lines, (New-Object System.Text.UTF8Encoding($false)))

Write-Host ''
Write-Host 'Audit completato senza Node.js:' -ForegroundColor Green
Write-Host $mdPath
Write-Host $csvPath
Write-Host $jsonPath
