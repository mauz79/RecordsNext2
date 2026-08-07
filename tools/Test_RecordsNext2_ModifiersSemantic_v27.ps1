param(
    [string]$ProjectDir = "D:\DEV_APPS\RecordsNext2.0"
)

$ErrorActionPreference = "Stop"
Set-Location $ProjectDir

$reportsRoot = Join-Path $ProjectDir "data\reports"
$stagingRoot = Join-Path $ProjectDir "data\site-export-staging"
$outDir = Join-Path $ProjectDir "reports\semantic-audit"
$outCsv = Join-Path $outDir "RecordsNext2_MODIFICATORI_SEMANTIC_AUDIT.csv"

New-Item -ItemType Directory -Path $outDir -Force | Out-Null

$Problems = New-Object System.Collections.ArrayList

function Num([object]$Value) {
    if ($null -eq $Value -or [string]::IsNullOrWhiteSpace([string]$Value)) {
        return 0.0
    }
    return [double]$Value
}

function Same-Number([object]$A, [object]$B) {
    return [math]::Abs((Num $A) - (Num $B)) -le 0.000001
}

function Add-Problem(
    [string]$Season,
    [string]$Competition,
    [string]$Section,
    [string]$Key,
    [string]$Problem,
    [object]$Expected,
    [object]$Actual
) {
    [void]$Problems.Add([pscustomobject]@{
        Stagione = $Season
        Competizione = $Competition
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

function Modifier-Key([object]$Row) {
    return ([string]$Row.idIncontro + "|" + [string]$Row.idSquadra + "|" + [string]$Row.valore)
}

function Team-Key([object]$Row) {
    return [string]$Row.idSquadra
}

function Impact-Key([object]$Row) {
    return ([string]$Row.idIncontro + "|" + [string]$Row.idSquadra)
}

function Compare-Text(
    [string]$Season,
    [string]$Competition,
    [string]$Section,
    [string]$Key,
    [string]$Field,
    [object]$Expected,
    [object]$Actual
) {
    if ([string]$Expected -ne [string]$Actual) {
        Add-Problem $Season $Competition $Section $Key ("Campo " + $Field + " errato") $Expected $Actual
    }
}

function Compare-Number(
    [string]$Season,
    [string]$Competition,
    [string]$Section,
    [string]$Key,
    [string]$Field,
    [object]$Expected,
    [object]$Actual
) {
    if (-not (Same-Number $Expected $Actual)) {
        Add-Problem $Season $Competition $Section $Key ("Campo " + $Field + " errato") $Expected $Actual
    }
}

function Make-Map([object[]]$Rows, [scriptblock]$KeySelector) {
    $map = @{}
    foreach ($row in @($Rows)) {
        if ($null -eq $row) {
            continue
        }
        $key = [string](& $KeySelector $row)
        if ([string]::IsNullOrWhiteSpace($key)) {
            continue
        }
        $map[$key] = $row
    }
    return $map
}

function Standings-Points([int]$GoalsFor, [int]$GoalsAgainst) {
    if ($GoalsFor -gt $GoalsAgainst) {
        return 3
    }
    if ($GoalsFor -eq $GoalsAgainst) {
        return 1
    }
    return 0
}

function Goals-For-Score([double]$Score, [object[]]$Bands) {
    $goals = 0
    foreach ($band in @($Bands | Sort-Object { Num $_.min })) {
        if (($Score + 0.000001) -ge (Num $band.min)) {
            $candidate = [int](Num $band.gol)
            if ($candidate -gt $goals) {
                $goals = $candidate
            }
        }
    }
    return $goals
}

function Build-ModifierTotalsByMatchTeam([object[]]$Modifiers) {
    $totals = @{}
    foreach ($modifier in @($Modifiers)) {
        $key = [string]$modifier.idIncontro + "|" + [string]$modifier.idSquadra
        if (-not $totals.ContainsKey($key)) {
            $totals[$key] = 0.0
        }
        $totals[$key] = (Num $totals[$key]) + (Num $modifier.valore)
    }
    return $totals
}

function Build-HomeImpacts([object[]]$Matches, [object[]]$Modifiers, [object[]]$Bands) {
    $modifierTotals = Build-ModifierTotalsByMatchTeam $Modifiers
    $impacts = @()

    foreach ($group in @($Matches | Group-Object idIncontro)) {
        $homeRowsTmp = @($group.Group | Where-Object { [string]$_.lato -eq "casa" } | Select-Object -First 1)
        $awayRowsTmp = @($group.Group | Where-Object { [string]$_.lato -eq "fuori" } | Select-Object -First 1)

        if ($homeRowsTmp.Count -eq 0 -or $awayRowsTmp.Count -eq 0) {
            continue
        }

        $homeRow = $homeRowsTmp[0]
        $awayRow = $awayRowsTmp[0]
        $key = [string]$homeRow.idIncontro + "|" + [string]$homeRow.idSquadra
        $modifierTotal = 0.0
        if ($modifierTotals.ContainsKey($key)) {
            $modifierTotal = Num $modifierTotals[$key]
        }

        $bonus = (Num $homeRow.puntiFatti) - (Num $homeRow.parzialeFatto) - $modifierTotal
        if ([math]::Abs($bonus) -lt 0.000001) {
            $bonus = 0.0
        }
        if ($bonus -le 0) {
            continue
        }

        $scoreWithout = (Num $homeRow.puntiFatti) - $bonus
        $goalsWithout = Goals-For-Score $scoreWithout $Bands
        $homeGoals = [int](Num $homeRow.golFatti)
        $awayGoals = [int](Num $homeRow.golSubiti)
        $actualPoints = Standings-Points $homeGoals $awayGoals
        $pointsWithout = Standings-Points $goalsWithout $awayGoals
        $delta = $actualPoints - $pointsWithout

        if ($delta -lt 0) {
            $delta = 0
        }

        $impacts += [pscustomobject]@{
            idIncontro = [string]$homeRow.idIncontro
            idSquadra = [string]$homeRow.idSquadra
            squadra = [string]$homeRow.squadra
            idAvversaria = [string]$awayRow.idSquadra
            avversaria = [string]$awayRow.squadra
            bonus = $bonus
            puntiDelta = $delta
            home = $homeRow
            away = $awayRow
        }
    }

    return $impacts
}

function Audit-ModifierSection(
    [string]$Season,
    [string]$Competition,
    [string]$Section,
    [string]$Mode,
    [string]$Type,
    [object[]]$Modifiers,
    [object[]]$ActualRows
) {
    $selected = @($Modifiers | Where-Object { [string]$_.tipo -eq $Type })
    $actual = @($ActualRows)

    if ($Mode -eq "max") {
        $expected = @($selected | Sort-Object @{Expression={ Num $_.valore }; Descending=$true}, @{Expression={ [string]$_.squadra }; Descending=$false} | Select-Object -First 20)
        $expectedMap = Make-Map $expected { param($r) Modifier-Key $r }
        $actualMap = Make-Map $actual { param($r) Modifier-Key $r }

        if ($expectedMap.Count -ne $actualMap.Count) {
            Add-Problem $Season $Competition $Section "(conteggio)" "Numero righe errato" $expectedMap.Count $actualMap.Count
        }

        foreach ($key in @($expectedMap.Keys)) {
            if (-not $actualMap.ContainsKey($key)) {
                Add-Problem $Season $Competition $Section $key "Record massimo atteso assente" "presente" "assente"
            }
        }

        foreach ($key in @($actualMap.Keys)) {
            if (-not $expectedMap.ContainsKey($key)) {
                Add-Problem $Season $Competition $Section $key "Record massimo esportato non atteso" "assente" "presente"
            }
        }
        return
    }

    $expectedGroups = @($selected | Group-Object idSquadra)
    $actualMap = Make-Map $actual { param($r) Team-Key $r }

    if ($expectedGroups.Count -ne $actualMap.Count) {
        Add-Problem $Season $Competition $Section "(conteggio)" "Numero squadre errato" $expectedGroups.Count $actualMap.Count
    }

    foreach ($group in $expectedGroups) {
        $rows = @($group.Group)
        if ($rows.Count -eq 0) {
            continue
        }

        $teamId = [string]$group.Name
        if (-not $actualMap.ContainsKey($teamId)) {
            Add-Problem $Season $Competition $Section $teamId "Squadra attesa assente" "presente" "assente"
            continue
        }

        $actualRow = $actualMap[$teamId]
        $sum = 0.0
        foreach ($row in $rows) {
            $sum += Num $row.valore
        }

        if ($Mode -eq "total") {
            Compare-Number $Season $Competition $Section $teamId "valore" $sum $actualRow.valore
        }
        elseif ($Mode -eq "average") {
            $average = $sum / $rows.Count
            Compare-Number $Season $Competition $Section $teamId "valore" $average $actualRow.valore
            Compare-Number $Season $Competition $Section $teamId "utilizzi" $rows.Count $actualRow.utilizzi
        }
        elseif ($Mode -eq "uses") {
            Compare-Number $Season $Competition $Section $teamId "valore" $rows.Count $actualRow.valore
        }

        $details = @(Property-Value $actualRow "dettagli")
        if ($details.Count -ne $rows.Count) {
            Add-Problem $Season $Competition $Section $teamId "Numero dettagli errato" $rows.Count $details.Count
        }
    }
}

$manifest = Get-ChildItem -Path $stagingRoot -Recurse -File -Filter "fcmRecordsNext_Manifest.js" -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending | Select-Object -First 1
if ($null -eq $manifest) {
    throw "fcmRecordsNext_Manifest.js non trovato in $stagingRoot"
}

$modifiersJs = Join-Path $manifest.Directory.FullName "fcmRecordsNext_Modifiers.js"
if (-not (Test-Path -LiteralPath $modifiersJs)) {
    throw "fcmRecordsNext_Modifiers.js non trovato: $modifiersJs"
}

$js = [System.IO.File]::ReadAllText($modifiersJs)
$eq = $js.IndexOf("=")
if ($eq -lt 0) {
    throw "Formato fcmRecordsNext_Modifiers.js inatteso."
}
$json = $js.Substring($eq + 1).Trim()
if ($json.EndsWith(";")) {
    $json = $json.Substring(0, $json.Length - 1)
}
$root = $json | ConvertFrom-Json

$normalizedIndex = @{}
$normalizedFiles = @(Get-ChildItem -Path $reportsRoot -Recurse -File -Filter "season_normalized_*.json" -ErrorAction SilentlyContinue)
foreach ($file in $normalizedFiles) {
    $source = [System.IO.File]::ReadAllText($file.FullName) | ConvertFrom-Json
    $season = [string](Property-Value $source.meta "stagione")
    if ([string]::IsNullOrWhiteSpace($season)) {
        $season = [string]$file.Directory.Name
    }

    $competitionId = [string](Property-Value $source.meta "competizioneStoricaId")
    if ([string]::IsNullOrWhiteSpace($competitionId)) {
        $competitionId = $file.BaseName.Substring("season_normalized_".Length)
    }

    $normalizedIndex[$season + "|" + $competitionId] = $source
}

$definitions = @(
    [pscustomobject]@{ Section="modDifesaMax"; Type="modDifesa"; Mode="max" },
    [pscustomobject]@{ Section="modDifesaTotaleSquadre"; Type="modDifesa"; Mode="total" },
    [pscustomobject]@{ Section="modDifesaMediaSquadre"; Type="modDifesa"; Mode="average" },
    [pscustomobject]@{ Section="modDifesaUtilizziSquadre"; Type="modDifesa"; Mode="uses" },
    [pscustomobject]@{ Section="capitanoTotaleSquadre"; Type="capitano"; Mode="total" },
    [pscustomobject]@{ Section="capitanoUtilizziSquadre"; Type="capitano"; Mode="uses" },
    [pscustomobject]@{ Section="modDifesaFcmMax"; Type="fcmDifesa"; Mode="max" },
    [pscustomobject]@{ Section="modDifesaFcmTotaleSquadre"; Type="fcmDifesa"; Mode="total" },
    [pscustomobject]@{ Section="modDifesaFcmMediaSquadre"; Type="fcmDifesa"; Mode="average" },
    [pscustomobject]@{ Section="modDifesaFcmUtilizziSquadre"; Type="fcmDifesa"; Mode="uses" }
)

$sectionChecks = [ordered]@{}
foreach ($definition in $definitions) {
    $sectionChecks[$definition.Section] = 0
}
$sectionChecks["fattoreCampoDecisivo"] = 0
$sectionChecks["fattoreCampoTotaleSquadre"] = 0
$sectionChecks["fattoreCampoPuntiGuadagnatiSquadre"] = 0
$sectionChecks["fattoreCampoPuntiPersiSquadre"] = 0

$competitionCount = 0
$modifierRows = 0
$homeMatchRows = 0

foreach ($entry in @($root.seasonAggregates)) {
    $season = [string]$entry.stagione
    $competitionId = [string]$entry.competizioneId
    $competitionName = [string]$entry.competizioneNome
    $sourceKey = $season + "|" + $competitionId

    if (-not $normalizedIndex.ContainsKey($sourceKey)) {
        Add-Problem $season $competitionName "(source)" $sourceKey "Normalizzato non trovato" "presente" "assente"
        continue
    }

    $source = $normalizedIndex[$sourceKey]
    $records = $entry.data.records
    $modifiers = @($source.modificatoriB2Dettaglio)
    $matches = @($source.partiteSquadra | Where-Object {
        -not [string]::IsNullOrWhiteSpace([string]$_.idSquadra) -and
        [string]$_.idSquadra -ne "0" -and
        -not [string]::IsNullOrWhiteSpace([string]$_.squadra) -and
        -not [string]::IsNullOrWhiteSpace([string]$_.idIncontro) -and
        -not [string]::IsNullOrWhiteSpace([string]$_.avversaria)
    })
    $bands = @($source.fasceGolDettaglio)

    $competitionCount++
    $modifierRows += $modifiers.Count
    $homeMatchRows += @($matches | Where-Object { [string]$_.lato -eq "casa" }).Count

    foreach ($definition in $definitions) {
        $section = $definition.Section
        $sectionChecks[$section] = [int]$sectionChecks[$section] + 1
        $actualRows = @(Property-Value $records $section)
        Audit-ModifierSection $season $competitionName $section $definition.Mode $definition.Type $modifiers $actualRows
    }

    $impacts = @(Build-HomeImpacts $matches $modifiers $bands)
    $decisiveExpected = @($impacts | Where-Object { [int]$_.puntiDelta -gt 0 })

    $section = "fattoreCampoDecisivo"
    $sectionChecks[$section] = [int]$sectionChecks[$section] + 1
    $actualDecisive = @(Property-Value $records $section)
    $expectedDecisiveMap = Make-Map $decisiveExpected { param($r) Impact-Key $r }
    $actualDecisiveMap = Make-Map $actualDecisive { param($r) Impact-Key $r }

    if ($expectedDecisiveMap.Count -ne $actualDecisiveMap.Count) {
        Add-Problem $season $competitionName $section "(conteggio)" "Numero partite decisive errato" $expectedDecisiveMap.Count $actualDecisiveMap.Count
    }

    foreach ($key in @($expectedDecisiveMap.Keys)) {
        if (-not $actualDecisiveMap.ContainsKey($key)) {
            Add-Problem $season $competitionName $section $key "Partita decisiva attesa assente" "presente" "assente"
            continue
        }
        $expectedRow = $expectedDecisiveMap[$key]
        $actualRow = $actualDecisiveMap[$key]
        Compare-Number $season $competitionName $section $key "valore" $expectedRow.bonus $actualRow.valore
        Compare-Number $season $competitionName $section $key "puntiClassificaGuadagnati" $expectedRow.puntiDelta $actualRow.puntiClassificaGuadagnati
        Compare-Text $season $competitionName $section $key "squadra" $expectedRow.squadra $actualRow.squadra
        Compare-Text $season $competitionName $section $key "avversaria" $expectedRow.avversaria $actualRow.avversaria
    }

    $section = "fattoreCampoTotaleSquadre"
    $sectionChecks[$section] = [int]$sectionChecks[$section] + 1
    $homeRows = @($matches | Where-Object { [string]$_.lato -eq "casa" })
    $actualTotals = @(Property-Value $records $section)
    $actualTotalsMap = Make-Map $actualTotals { param($r) Team-Key $r }

    foreach ($group in @($homeRows | Group-Object idSquadra)) {
        $teamRows = @($group.Group)
        $teamId = [string]$group.Name
        if (-not $actualTotalsMap.ContainsKey($teamId)) {
            Add-Problem $season $competitionName $section $teamId "Squadra casa attesa assente" "presente" "assente"
            continue
        }

        $modifierTotals = Build-ModifierTotalsByMatchTeam $modifiers
        $expectedTotal = 0.0
        foreach ($match in $teamRows) {
            $key = [string]$match.idIncontro + "|" + [string]$match.idSquadra
            $modifierTotal = 0.0
            if ($modifierTotals.ContainsKey($key)) {
                $modifierTotal = Num $modifierTotals[$key]
            }
            $bonus = (Num $match.puntiFatti) - (Num $match.parzialeFatto) - $modifierTotal
            if ([math]::Abs($bonus) -lt 0.000001) {
                $bonus = 0.0
            }
            $expectedTotal += $bonus
        }

        $actualRow = $actualTotalsMap[$teamId]
        Compare-Number $season $competitionName $section $teamId "valore" $expectedTotal $actualRow.valore
        Compare-Number $season $competitionName $section $teamId "presenzeCasa" $teamRows.Count $actualRow.presenzeCasa
        $details = @(Property-Value $actualRow "dettagli")
        if ($details.Count -ne $teamRows.Count) {
            Add-Problem $season $competitionName $section $teamId "Numero dettagli casa errato" $teamRows.Count $details.Count
        }
    }

    foreach ($impactDefinition in @(
        [pscustomobject]@{ Section="fattoreCampoPuntiGuadagnatiSquadre"; TeamField="idSquadra"; TeamName="squadra" },
        [pscustomobject]@{ Section="fattoreCampoPuntiPersiSquadre"; TeamField="idAvversaria"; TeamName="avversaria" }
    )) {
        $section = $impactDefinition.Section
        $sectionChecks[$section] = [int]$sectionChecks[$section] + 1
        $actualRows = @(Property-Value $records $section)
        $actualMap = Make-Map $actualRows { param($r) Team-Key $r }

        $expectedGroups = @($decisiveExpected | Group-Object { [string](Property-Value $_ $impactDefinition.TeamField) })

        if ($expectedGroups.Count -ne $actualMap.Count) {
            Add-Problem $season $competitionName $section "(conteggio)" "Numero squadre errato" $expectedGroups.Count $actualMap.Count
        }

        foreach ($group in $expectedGroups) {
            $teamId = [string]$group.Name
            if (-not $actualMap.ContainsKey($teamId)) {
                Add-Problem $season $competitionName $section $teamId "Squadra attesa assente" "presente" "assente"
                continue
            }

            $sum = 0
            foreach ($impact in @($group.Group)) {
                $sum += [int]$impact.puntiDelta
            }

            $actualRow = $actualMap[$teamId]
            Compare-Number $season $competitionName $section $teamId "valore" $sum $actualRow.valore
            $details = @(Property-Value $actualRow "dettagli")
            if ($details.Count -ne @($group.Group).Count) {
                Add-Problem $season $competitionName $section $teamId "Numero dettagli decisivi errato" @($group.Group).Count $details.Count
            }
        }
    }
}

$Problems | Export-Csv -LiteralPath $outCsv -NoTypeInformation -Encoding UTF8

$summary = @()
foreach ($sectionName in @($sectionChecks.Keys | ForEach-Object { [string]$_ })) {
    $count = @($Problems | Where-Object { [string]$_.Sezione -eq $sectionName }).Count
    $summary += [pscustomobject]@{
        Record = $sectionName
        CompetizioniControllate = $sectionChecks[$sectionName]
        Problemi = $count
        Esito = if ($count -eq 0) { "OK" } else { "ERRORE" }
    }
}

Write-Host ""
Write-Host "=== AUDIT SEMANTICO MODIFICATORI + FATTORE CAMPO v27 ==="
$summary | Format-Table -AutoSize
Write-Host ""
Write-Host ("Competizioni controllate     : " + $competitionCount)
Write-Host ("Righe modificatori sorgente  : " + $modifierRows)
Write-Host ("Righe squadra in casa        : " + $homeMatchRows)
Write-Host ("Problemi totali              : " + $Problems.Count)
Write-Host ("Dettaglio CSV                 : " + $outCsv)

if ($Problems.Count -gt 0) {
    Write-Host ""
    Write-Host "=== PRIMI 25 PROBLEMI ==="
    $Problems | Select-Object -First 25 | Format-Table -AutoSize
    exit 1
}

Write-Host ""
Write-Host "AUDIT SEMANTICO MODIFICATORI + FATTORE CAMPO: OK"
