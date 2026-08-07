param(
    [string]$ProjectDir = "D:\DEV_APPS\RecordsNext2.0"
)
$ErrorActionPreference = "Stop"
Set-Location $ProjectDir
$reportsRoot = Join-Path $ProjectDir "data\reports"
$archiveRoot = Join-Path $ProjectDir "data\records-archive\stagioni"
$outDir = Join-Path $ProjectDir "reports\semantic-audit"
$outCsv = Join-Path $outDir "RecordsNext2_CLASSICI_SEMANTIC_AUDIT.csv"
New-Item -ItemType Directory -Path $outDir -Force | Out-Null
function Num([object]$Value) {
    if ($null -eq $Value -or "$Value" -eq "") { return 0.0 }
    return [double]$Value
}
function EqNum([double]$A, [double]$B) {
    return [math]::Abs($A - $B) -lt 0.000001
}
function Valid-Match([object]$Row) {
    if ($null -eq $Row) { return $false }
    if ([string]::IsNullOrWhiteSpace([string]$Row.idSquadra)) { return $false }
    if ([string]$Row.idSquadra -eq "0") { return $false }
    if ([string]::IsNullOrWhiteSpace([string]$Row.squadra)) { return $false }
    if ([string]::IsNullOrWhiteSpace([string]$Row.idIncontro)) { return $false }
    if ([string]::IsNullOrWhiteSpace([string]$Row.avversaria)) { return $false }
    return $true
}
function Add-Problem {
    param(
        [System.Collections.Generic.List[object]]$List,
        [string]$Season,
        [string]$Competition,
        [string]$Record,
        [string]$Team,
        [string]$Problem,
        [object]$Expected,
        [object]$Actual
    )
    $List.Add([pscustomobject]@{
        Stagione = $Season
        Competizione = $Competition
        Record = $Record
        Squadra = $Team
        Problema = $Problem
        Atteso = $Expected
        Reale = $Actual
    })
}
$recordNames = [ordered]@{
    puntiSquadraMin = "Minor punteggio"
    partitePiuGolRegolamentari = "Più gol regolamentari"
    partitePiuScartoRegolamentari = "Maggior scarto regolamentare"
    mediaPuntiSquadre = "Media punti"
    totalePuntiSquadre = "Somma punti"
    puntiClassificaSquadre = "Punti classifica"
    vittorieSquadre = "Vittorie"
    pareggiSquadre = "Pareggi"
    sconfitteSquadre = "Sconfitte"
    golFattiSquadre = "Gol fatti"
    golSubitiSquadre = "Gol subiti"
}
$checked = @{}
foreach ($key in $recordNames.Keys) { $checked[$key] = 0 }
$problems = New-Object 'System.Collections.Generic.List[object]'
$filesChecked = 0
$matchesChecked = 0
$teamsChecked = 0
$normalizedFiles = @(
    Get-ChildItem `
        -LiteralPath $reportsRoot `
        -Recurse `
        -File `
        -Filter "season_normalized_*.json" `
        -ErrorAction SilentlyContinue |
    Sort-Object FullName
)
if ($normalizedFiles.Count -eq 0) {
    throw "Nessun season_normalized_*.json trovato in $reportsRoot"
}
foreach ($normalizedFile in $normalizedFiles) {
    $source = Get-Content -LiteralPath $normalizedFile.FullName -Raw | ConvertFrom-Json
    $season = $normalizedFile.Directory.Name
    $competitionId = [string]$source.meta.competizioneStoricaId
    if ([string]::IsNullOrWhiteSpace($competitionId)) {
        $competitionId = $normalizedFile.BaseName.Substring("season_normalized_".Length)
    }
    $competitionName = [string]$source.meta.competizioneNome
    if ([string]::IsNullOrWhiteSpace($competitionName)) {
        $competitionName = $competitionId
    }
    $recordFile = Join-Path `
        (Join-Path $archiveRoot $season) `
        ("season_records_" + $competitionId + ".json")
    if (-not (Test-Path -LiteralPath $recordFile)) {
        Add-Problem $problems $season $competitionName "(file)" "" `
            "season_records mancante" $recordFile ""
        continue
    }
    $actual = Get-Content -LiteralPath $recordFile -Raw | ConvertFrom-Json
    $matches = @($source.partiteSquadra | Where-Object { Valid-Match $_ })
    if ($matches.Count -eq 0) {
        continue
    }
    $filesChecked++
    $matchesChecked += $matches.Count
    # Controlli generali sulle 11 sezioni.
    foreach ($section in $recordNames.Keys) {
        $rows = @($actual.records.$section)
        $checked[$section]++
        if ($rows.Count -eq 0) {
            Add-Problem $problems $season $competitionName $recordNames[$section] "" `
                "Sezione vuota" "almeno 1 riga" "0 righe"
            continue
        }
        foreach ($row in $rows) {
            if ([string]::IsNullOrWhiteSpace([string]$row.squadra)) {
                Add-Problem $problems $season $competitionName $recordNames[$section] "" `
                    "Squadra vuota" "nome squadra" ""
            }
            if ($null -ne $row.idSquadra -and [string]$row.idSquadra -eq "0") {
                Add-Problem $problems $season $competitionName $recordNames[$section] "" `
                    "idSquadra tecnico" "idSquadra != 0" "0"
            }
            if ($row.stagione -and [string]$row.stagione -ne $season) {
                Add-Problem $problems $season $competitionName $recordNames[$section] ([string]$row.squadra) `
                    "Stagione errata" $season $row.stagione
            }
            if ($row.competizioneNome -and [string]$row.competizioneNome -ne $competitionName) {
                Add-Problem $problems $season $competitionName $recordNames[$section] ([string]$row.squadra) `
                    "Competizione errata" $competitionName $row.competizioneNome
            }
        }
    }
    # Record di singola partita.
    $expectedMin = ($matches | Measure-Object -Property puntiFatti -Minimum).Minimum
    $actualMinRows = @($actual.records.puntiSquadraMin)
    if ($actualMinRows.Count -gt 0 -and -not (EqNum (Num $expectedMin) (Num $actualMinRows[0].valore))) {
        Add-Problem $problems $season $competitionName "Minor punteggio" ([string]$actualMinRows[0].squadra) `
            "Valore errato" $expectedMin $actualMinRows[0].valore
    }
    $byMatch = @{}
    foreach ($row in $matches) {
        $id = [string]$row.idIncontro
        if (-not $byMatch.ContainsKey($id)) { $byMatch[$id] = $row }
    }
    $expectedMostGoals = 0.0
    $expectedLargestMargin = 0.0
    foreach ($row in $byMatch.Values) {
        $regFor = Num $row.golRegolamentariFatti
        $regAgainst = Num $row.golRegolamentariSubiti
        $totalReg = $regFor + $regAgainst
        $margin = [math]::Abs($regFor - $regAgainst)
        if ($totalReg -gt $expectedMostGoals) { $expectedMostGoals = $totalReg }
        if ($margin -gt $expectedLargestMargin) { $expectedLargestMargin = $margin }
    }
    $actualGoalsRows = @($actual.records.partitePiuGolRegolamentari)
    if ($actualGoalsRows.Count -gt 0 -and -not (EqNum $expectedMostGoals (Num $actualGoalsRows[0].valore))) {
        Add-Problem $problems $season $competitionName "Più gol regolamentari" ([string]$actualGoalsRows[0].squadra) `
            "Valore errato" $expectedMostGoals $actualGoalsRows[0].valore
    }
    $actualMarginRows = @($actual.records.partitePiuScartoRegolamentari)
    if ($actualMarginRows.Count -gt 0 -and -not (EqNum $expectedLargestMargin (Num $actualMarginRows[0].valore))) {
        Add-Problem $problems $season $competitionName "Maggior scarto regolamentare" ([string]$actualMarginRows[0].squadra) `
            "Valore errato" $expectedLargestMargin $actualMarginRows[0].valore
    }
    # Aggregati per squadra.
    $groups = $matches | Group-Object -Property idSquadra
    foreach ($group in $groups) {
        $teamMatches = @($group.Group)
        if ($teamMatches.Count -eq 0) { continue }
        $teamsChecked++
        $teamId = [string]$teamMatches[0].idSquadra
        $teamName = [string]$teamMatches[0].squadra
        $sumPoints = ($teamMatches | Measure-Object -Property puntiFatti -Sum).Sum
        $avgPoints = $sumPoints / $teamMatches.Count
        $wins = @($teamMatches | Where-Object { [string]$_.esito -eq "V" }).Count
        $draws = @($teamMatches | Where-Object { [string]$_.esito -eq "P" }).Count
        $losses = @($teamMatches | Where-Object { [string]$_.esito -eq "S" }).Count
        $standings = ($wins * 3) + $draws
        $goalsFor = ($teamMatches | Measure-Object -Property golFatti -Sum).Sum
        $goalsAgainst = ($teamMatches | Measure-Object -Property golSubiti -Sum).Sum
        $expectedBySection = [ordered]@{
            mediaPuntiSquadre = $avgPoints
            totalePuntiSquadre = $sumPoints
            puntiClassificaSquadre = $standings
            vittorieSquadre = $wins
            pareggiSquadre = $draws
            sconfitteSquadre = $losses
            golFattiSquadre = $goalsFor
            golSubitiSquadre = $goalsAgainst
        }
        foreach ($section in $expectedBySection.Keys) {
            $actualRow = @($actual.records.$section | Where-Object {
                [string]$_.idSquadra -eq $teamId
            } | Select-Object -First 1)
            if ($actualRow.Count -eq 0) {
                Add-Problem $problems $season $competitionName $recordNames[$section] $teamName `
                    "Squadra assente dal record" $expectedBySection[$section] "(assente)"
                continue
            }
            $actualValue = Num $actualRow[0].valore
            $expectedValue = Num $expectedBySection[$section]
            if (-not (EqNum $expectedValue $actualValue)) {
                Add-Problem $problems $season $competitionName $recordNames[$section] $teamName `
                    "Valore errato" $expectedValue $actualValue
            }
            if ([int]$actualRow[0].partite -ne $teamMatches.Count) {
                Add-Problem $problems $season $competitionName $recordNames[$section] $teamName `
                    "Numero partite errato" $teamMatches.Count $actualRow[0].partite
            }
        }
    }
}
$problems |
    Export-Csv `
        -LiteralPath $outCsv `
        -NoTypeInformation `
        -Encoding UTF8
$summary = foreach ($key in $recordNames.Keys) {
    $recordProblems = @($problems | Where-Object Record -eq $recordNames[$key])
    [pscustomobject]@{
        Record = $recordNames[$key]
        CompetizioniControllate = $checked[$key]
        Problemi = $recordProblems.Count
        Esito = if ($recordProblems.Count -eq 0) { "OK" } else { "ERRORE" }
    }
}
Write-Host ""
Write-Host "=== AUDIT SEMANTICO CLASSICI ==="
$summary | Format-Table -AutoSize
Write-Host ""
Write-Host "File competizione controllati : $filesChecked"
Write-Host "Righe partita valide          : $matchesChecked"
Write-Host "Aggregati squadra controllati : $teamsChecked"
Write-Host "Problemi totali               : $($problems.Count)"
Write-Host "Dettaglio CSV                  : $outCsv"
if ($problems.Count -gt 0) {
    Write-Host ""
    Write-Host "=== PRIMI 25 PROBLEMI ==="
    $problems | Select-Object -First 25 | Format-Table -AutoSize
    exit 1
}
Write-Host ""
Write-Host "AUDIT SEMANTICO CLASSICI: OK"
