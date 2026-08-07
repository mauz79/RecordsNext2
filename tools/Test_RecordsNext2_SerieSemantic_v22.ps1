param(
    [string]$ProjectDir = "D:\DEV_APPS\RecordsNext2.0"
)

$ErrorActionPreference = "Stop"
Set-Location $ProjectDir

$reportsRoot = Join-Path $ProjectDir "data\reports"
$archiveRoot = Join-Path $ProjectDir "data\records-archive\stagioni"
$configPath = Join-Path $ProjectDir "config\processing.json"
$outDir = Join-Path $ProjectDir "reports\semantic-audit"
$outCsv = Join-Path $outDir "RecordsNext2_SERIE_SEMANTIC_AUDIT.csv"

New-Item -ItemType Directory -Path $outDir -Force | Out-Null

function Num([object]$Value) {
    if ($null -eq $Value -or "$Value" -eq "") { return 0.0 }
    return [double]$Value
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

function Order-Value([object]$Row) {
    if ($null -ne $Row.ordineGiornata -and "$($Row.ordineGiornata)" -ne "") {
        return Num $Row.ordineGiornata
    }
    return Num $Row.giornataDiA
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

function New-SeriesRow {
    param(
        [object[]]$Rows
    )
    if ($Rows.Count -eq 0) { return $null }
    $first = $Rows[0]
    $last = $Rows[$Rows.Count - 1]
    return [pscustomobject]@{
        idSquadra = [string]$first.idSquadra
        squadra = [string]$first.squadra
        valore = $Rows.Count
        daGiornataDiA = $first.giornataDiA
        aGiornataDiA = $last.giornataDiA
        daOrdine = Order-Value $first
        aOrdine = Order-Value $last
    }
}

function Get-ResultRuns {
    param(
        [object[]]$Matches,
        [string]$ResultCode,
        [bool]$MustMatch
    )
    $runs = New-Object 'System.Collections.Generic.List[object]'
    $groups = $Matches | Group-Object -Property idSquadra
    foreach ($group in $groups) {
        $teamMatches = @($group.Group | Sort-Object @{Expression={ Order-Value $_ }}, @{Expression={ [string]$_.idIncontro }})
        $current = New-Object 'System.Collections.Generic.List[object]'
        foreach ($match in $teamMatches) {
            $matchesResult = ([string]$match.esito -eq $ResultCode)
            $belongs = if ($MustMatch) { $matchesResult } else { -not $matchesResult }
            if ($belongs) {
                $current.Add($match)
            } else {
                if ($current.Count -gt 0) {
                    $runs.Add((New-SeriesRow -Rows $current.ToArray()))
                    $current = New-Object 'System.Collections.Generic.List[object]'
                }
            }
        }
        if ($current.Count -gt 0) {
            $runs.Add((New-SeriesRow -Rows $current.ToArray()))
        }
    }
    return @($runs | Sort-Object @{Expression={ [int]$_.valore }; Descending=$true}, @{Expression={ [string]$_.squadra }})
}

function Get-UnbeatenRuns {
    param([object[]]$Matches)
    return Get-ResultRuns -Matches $Matches -ResultCode "S" -MustMatch $false
}

function Get-BestEventSeriesByTeam {
    param(
        [object[]]$Matches,
        [object[]]$Events
    )
    $eventKeys = @{}
    foreach ($event in $Events) {
        $eventKeys[([string]$event.idSquadra + "|" + [string]$event.idIncontro)] = $true
    }

    $result = New-Object 'System.Collections.Generic.List[object]'
    $groups = $Matches | Group-Object -Property idSquadra

    foreach ($group in $groups) {
        $teamMatches = @($group.Group | Sort-Object @{Expression={ Order-Value $_ }}, @{Expression={ [string]$_.idIncontro }})
        $best = @()
        $current = New-Object 'System.Collections.Generic.List[object]'

        foreach ($match in $teamMatches) {
            $key = [string]$match.idSquadra + "|" + [string]$match.idIncontro
            if ($eventKeys.ContainsKey($key)) {
                $current.Add($match)
            } else {
                if ($current.Count -gt $best.Count) { $best = $current.ToArray() }
                $current = New-Object 'System.Collections.Generic.List[object]'
            }
        }

        if ($current.Count -gt $best.Count) { $best = $current.ToArray() }
        if ($best.Count -gt 0) { $result.Add((New-SeriesRow -Rows $best)) }
    }

    return @($result | Sort-Object @{Expression={ [int]$_.valore }; Descending=$true}, @{Expression={ [string]$_.squadra }})
}

function Compare-TopRuns {
    param(
        [System.Collections.Generic.List[object]]$Problems,
        [string]$Season,
        [string]$Competition,
        [string]$RecordName,
        [object[]]$Expected,
        [object[]]$Actual,
        [int]$Limit = 20
    )

    $expectedRows = @($Expected | Select-Object -First $Limit)
    $actualRows = @($Actual)

    if ($actualRows.Count -ne $expectedRows.Count) {
        Add-Problem $Problems $Season $Competition $RecordName "" "Numero righe diverso" $expectedRows.Count $actualRows.Count
    }

    $count = [math]::Min($expectedRows.Count, $actualRows.Count)
    for ($i = 0; $i -lt $count; $i++) {
        $e = $expectedRows[$i]
        $a = $actualRows[$i]

        if ([string]$a.squadra -ne [string]$e.squadra -or [int](Num $a.valore) -ne [int]$e.valore) {
            Add-Problem $Problems $Season $Competition $RecordName ([string]$a.squadra) "Classifica serie diversa" ($e.squadra + " = " + $e.valore) ($a.squadra + " = " + $a.valore)
            continue
        }

        if ($null -ne $a.daGiornataDiA -and [string]$a.daGiornataDiA -ne [string]$e.daGiornataDiA) {
            Add-Problem $Problems $Season $Competition $RecordName ([string]$a.squadra) "Inizio serie errato" $e.daGiornataDiA $a.daGiornataDiA
        }

        if ($null -ne $a.aGiornataDiA -and [string]$a.aGiornataDiA -ne [string]$e.aGiornataDiA) {
            Add-Problem $Problems $Season $Competition $RecordName ([string]$a.squadra) "Fine serie errata" $e.aGiornataDiA $a.aGiornataDiA
        }
    }
}

$config = Get-Content -LiteralPath $configPath -Raw -Encoding UTF8 | ConvertFrom-Json

$seriesDefinitions = @(
    [pscustomobject]@{ Key="wins"; Section="serieVittorie"; Name="Vittorie consecutive"; Result="V"; MustMatch=$true; Type="result" },
    [pscustomobject]@{ Key="draws"; Section="seriePareggi"; Name="Pareggi consecutivi"; Result="P"; MustMatch=$true; Type="result" },
    [pscustomobject]@{ Key="losses"; Section="serieSconfitte"; Name="Sconfitte consecutive"; Result="S"; MustMatch=$true; Type="result" },
    [pscustomobject]@{ Key="unbeaten"; Section="serieSenzaSconfitte"; Name="Senza sconfitte"; Result="S"; MustMatch=$false; Type="result" },
    [pscustomobject]@{ Key="winless"; Section="serieSenzaVittorie"; Name="Senza vittorie"; Result="V"; MustMatch=$false; Type="result" },
    [pscustomobject]@{ Key="clean-sheets"; Section="cleanSheetPortiereSerieSquadre"; Name="Clean sheet consecutivi"; Type="clean" }
)

$modifierDefinitions = @(
    [pscustomobject]@{ Config="modm1pers.series"; TypeName="modDifesa"; Section="modDifesaSerieSquadre"; Name="Serie Modificatore Difesa" },
    [pscustomobject]@{ Config="modm2pers.series"; TypeName="capitano"; Section="capitanoSerieSquadre"; Name="Serie Capitano" },
    [pscustomobject]@{ Config="modm3pers.series"; TypeName="personalizzato3"; Section="modPersonalizzato3SerieSquadre"; Name="Serie Modificatore personale 3" },
    [pscustomobject]@{ Config="modportiere.series"; TypeName="fcmPortiere"; Section="modPortiereFcmSerieSquadre"; Name="Serie Modificatore Portiere FCM" },
    [pscustomobject]@{ Config="moddifesa.series"; TypeName="fcmDifesa"; Section="modDifesaFcmSerieSquadre"; Name="Serie Modificatore Difesa FCM" },
    [pscustomobject]@{ Config="modcentrocampo.series"; TypeName="fcmCentrocampo"; Section="modCentrocampoFcmSerieSquadre"; Name="Serie Modificatore Centrocampo FCM" },
    [pscustomobject]@{ Config="modattacco.series"; TypeName="fcmAttacco"; Section="modAttaccoFcmSerieSquadre"; Name="Serie Modificatore Attacco FCM" },
    [pscustomobject]@{ Config="modmodulo.series"; TypeName="fcmModulo"; Section="modModuloFcmSerieSquadre"; Name="Serie Modificatore Modulo FCM" }
)

$problems = New-Object 'System.Collections.Generic.List[object]'
$checks = @{}
foreach ($def in $seriesDefinitions) { $checks[$def.Name] = 0 }
foreach ($def in $modifierDefinitions) {
    if ([bool]$config.processing.families.modifiers.children.($def.Config)) { $checks[$def.Name] = 0 }
}

$filesChecked = 0
$matchesChecked = 0
$teamsChecked = 0

$normalizedFiles = @(Get-ChildItem -LiteralPath $reportsRoot -Recurse -File -Filter "season_normalized_*.json" -ErrorAction SilentlyContinue | Sort-Object FullName)
if ($normalizedFiles.Count -eq 0) { throw "Nessun season_normalized_*.json trovato in $reportsRoot" }

foreach ($normalizedFile in $normalizedFiles) {
    $source = Get-Content -LiteralPath $normalizedFile.FullName -Raw -Encoding UTF8 | ConvertFrom-Json
    $season = $normalizedFile.Directory.Name
    $competitionId = [string]$source.meta.competizioneStoricaId
    if ([string]::IsNullOrWhiteSpace($competitionId)) { $competitionId = $normalizedFile.BaseName.Substring("season_normalized_".Length) }
    $competitionName = [string]$source.meta.competizioneNome
    if ([string]::IsNullOrWhiteSpace($competitionName)) { $competitionName = $competitionId }

    $recordFile = Join-Path (Join-Path $archiveRoot $season) ("season_records_" + $competitionId + ".json")
    if (-not (Test-Path -LiteralPath $recordFile)) {
        Add-Problem $problems $season $competitionName "(file)" "" "season_records mancante" $recordFile ""
        continue
    }

    $actual = Get-Content -LiteralPath $recordFile -Raw -Encoding UTF8 | ConvertFrom-Json
    $matches = @($source.partiteSquadra | Where-Object { Valid-Match $_ })
    if ($matches.Count -eq 0) { continue }

    $filesChecked++
    $matchesChecked += $matches.Count
    $teamsChecked += @($matches | Group-Object idSquadra).Count

    foreach ($def in $seriesDefinitions) {
        $selected = [bool]$config.processing.families.series.children.($def.Key)
        if (-not $selected) { continue }

        $checks[$def.Name]++

        if ($def.Type -eq "result") {
            $expected = Get-ResultRuns -Matches $matches -ResultCode $def.Result -MustMatch $def.MustMatch
            $actualRows = @($actual.records.($def.Section))
            Compare-TopRuns -Problems $problems -Season $season -Competition $competitionName -RecordName $def.Name -Expected $expected -Actual $actualRows -Limit 20
        } elseif ($def.Type -eq "clean") {
            $clean = @($source.cleanSheetB3Dettaglio)
            $expected = Get-BestEventSeriesByTeam -Matches $matches -Events $clean
            $actualRows = @($actual.records.($def.Section))
            Compare-TopRuns -Problems $problems -Season $season -Competition $competitionName -RecordName $def.Name -Expected $expected -Actual $actualRows -Limit 99999
        }
    }

    $modifiers = @($source.modificatoriB2Dettaglio)
    foreach ($def in $modifierDefinitions) {
        $selected = [bool]$config.processing.families.modifiers.children.($def.Config)
        if (-not $selected) { continue }

        $checks[$def.Name]++
        $events = @($modifiers | Where-Object { [string]$_.tipo -eq $def.TypeName })
        $expected = Get-BestEventSeriesByTeam -Matches $matches -Events $events
        $actualRows = @($actual.records.($def.Section))
        Compare-TopRuns -Problems $problems -Season $season -Competition $competitionName -RecordName $def.Name -Expected $expected -Actual $actualRows -Limit 99999
    }
}

$problems | Export-Csv -LiteralPath $outCsv -NoTypeInformation -Encoding UTF8

$summary = foreach ($name in $checks.Keys | Sort-Object) {
    $recordProblems = @($problems | Where-Object Record -eq $name)
    [pscustomobject]@{
        Record = $name
        CompetizioniControllate = $checks[$name]
        Problemi = $recordProblems.Count
        Esito = if ($recordProblems.Count -eq 0) { "OK" } else { "ERRORE" }
    }
}

Write-Host ""
Write-Host "=== AUDIT SEMANTICO SERIE ==="
$summary | Format-Table -AutoSize
Write-Host ""
Write-Host "File competizione controllati : $filesChecked"
Write-Host "Righe partita valide          : $matchesChecked"
Write-Host "Squadre controllate           : $teamsChecked"
Write-Host "Problemi totali               : $($problems.Count)"
Write-Host "Dettaglio CSV                  : $outCsv"

if ($problems.Count -gt 0) {
    Write-Host ""
    Write-Host "=== PRIMI 25 PROBLEMI ==="
    $problems | Select-Object -First 25 | Format-Table -AutoSize
    exit 1
}

Write-Host ""
Write-Host "AUDIT SEMANTICO SERIE: OK"
