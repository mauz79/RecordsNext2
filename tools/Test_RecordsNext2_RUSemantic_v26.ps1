param(
    [string]$ProjectDir = "D:\DEV_APPS\RecordsNext2.0"
)

$ErrorActionPreference = "Stop"
Set-Location $ProjectDir

$outDir = Join-Path $ProjectDir "reports\semantic-audit"
$outCsv = Join-Path $outDir "RecordsNext2_RU_SEMANTIC_AUDIT.csv"
New-Item -ItemType Directory -Path $outDir -Force | Out-Null

function Num([object]$Value) {
    if ($null -eq $Value -or "$Value" -eq "") { return 0.0 }
    return [double]$Value
}

function EqNum([double]$A, [double]$B, [double]$Tolerance = 0.0050001) {
    return [math]::Abs($A - $B) -le $Tolerance
}

function Key-MatchTeam([object]$Row) {
    return ([string]$Row.idIncontro + "|" + [string]$Row.idSquadra)
}

function Key-Match([object]$Row) {
    return [string]$Row.idIncontro
}

function Add-Problem {
    param(
        [System.Collections.Generic.List[object]]$List,
        [string]$Season,
        [string]$Section,
        [string]$Key,
        [string]$Problem,
        [object]$Expected,
        [object]$Actual
    )

    $List.Add([pscustomobject]@{
        Stagione = $Season
        Sezione = $Section
        Chiave = $Key
        Problema = $Problem
        Atteso = $Expected
        Reale = $Actual
    })
}

function To-Map {
    param(
        [object[]]$Rows,
        [scriptblock]$KeySelector
    )

    $map = @{}
    foreach ($row in @($Rows)) {
        $key = & $KeySelector $row
        if (-not [string]::IsNullOrWhiteSpace([string]$key)) {
            $map[[string]$key] = $row
        }
    }
    return $map
}

function Check-EqualNumber {
    param(
        [System.Collections.Generic.List[object]]$Problems,
        [string]$Season,
        [string]$Section,
        [string]$Key,
        [string]$Field,
        [object]$Expected,
        [object]$Actual
    )

    if (-not (EqNum (Num $Expected) (Num $Actual))) {
        Add-Problem $Problems $Season $Section $Key ("Campo " + $Field + " errato") $Expected $Actual
    }
}

function Check-EqualText {
    param(
        [System.Collections.Generic.List[object]]$Problems,
        [string]$Season,
        [string]$Section,
        [string]$Key,
        [string]$Field,
        [object]$Expected,
        [object]$Actual
    )

    if ([string]$Expected -ne [string]$Actual) {
        Add-Problem $Problems $Season $Section $Key ("Campo " + $Field + " errato") $Expected $Actual
    }
}

$manifest = Get-ChildItem -Path (Join-Path $ProjectDir "data\site-export-staging") -Recurse -File -Filter "fcmRecordsNext_Manifest.js" -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending | Select-Object -First 1

if (-not $manifest) {
    throw "Manifest JS non trovato."
}

$ruPath = Join-Path $manifest.Directory.FullName "fcmRecordsNext_RU.js"

if (-not (Test-Path -LiteralPath $ruPath)) {
    throw "fcmRecordsNext_RU.js non trovato: $ruPath"
}

$js = [System.IO.File]::ReadAllText($ruPath)
$eq = $js.IndexOf("=")

if ($eq -lt 0) {
    throw "Formato JS RU inatteso."
}

$json = $js.Substring($eq + 1).Trim()
if ($json.EndsWith(";")) {
    $json = $json.Substring(0, $json.Length - 1)
}

$root = $json | ConvertFrom-Json
$problems = New-Object 'System.Collections.Generic.List[object]'

$checks = [ordered]@{
    "partiteConPiuRU" = 0
    "partiteConRU" = 0
    "partiteControRU" = 0
    "ruDecisiva" = 0
    "bilancioRUDecisiva" = 0
    "ruDecisivaContro" = 0
    "bilancioRUDecisivaContro" = 0
    "bilancioConRU" = 0
    "bilancioControRU" = 0
    "mediaPuntiConRU" = 0
    "mediaPuntiControRU" = 0
    "tipoRUUsata" = 0
}

$seasonCount = 0
$detailRows = 0
$teamMatchRows = 0

foreach ($seasonAggregate in @($root.seasonAggregates)) {
    $season = [string]$seasonAggregate.stagione
    $data = $seasonAggregate.data
    $views = $data.views
    $detail = $data.dettaglio

    $seasonCount++

    $ruDetail = @($detail.ruDettaglio)
    $ruTeamMatch = @($detail.ruTeamMatch)
    $partiteConRU = @($views.partiteConRU)
    $partiteControRU = @($views.partiteControRU)
    $partiteConPiuRU = @($views.partiteConPiuRU)
    $ruDecisiva = @($views.ruDecisiva)
    $ruDecisivaContro = @($views.ruDecisivaContro)
    $bilancioRUDecisiva = @($views.bilancioRUDecisiva)
    $bilancioRUDecisivaContro = @($views.bilancioRUDecisivaContro)
    $bilancioConRU = @($views.bilancioConRU)
    $bilancioControRU = @($views.bilancioControRU)
    $mediaPuntiConRU = @($views.mediaPuntiConRU)
    $mediaPuntiControRU = @($views.mediaPuntiControRU)
    $tipoRUUsata = @($views.tipoRUUsata)

    $detailRows += $ruDetail.Count
    $teamMatchRows += $ruTeamMatch.Count

    $checkNames = @($checks.Keys | ForEach-Object { $_ })
    foreach ($name in $checkNames) {
        $checks[$name]++
    }

    # 1) ruTeamMatch deve essere la stessa vista di partiteConRU.
    $teamMap = To-Map $ruTeamMatch { param($r) Key-MatchTeam $r }
    $withMap = To-Map $partiteConRU { param($r) Key-MatchTeam $r }

    if ($teamMap.Count -ne $withMap.Count) {
        Add-Problem $problems $season "partiteConRU" "(conteggio)" "Numero righe diverso da ruTeamMatch" $teamMap.Count $withMap.Count
    }

    foreach ($key in $teamMap.Keys) {
        if (-not $withMap.ContainsKey($key)) {
            Add-Problem $problems $season "partiteConRU" $key "Riga ruTeamMatch assente dalla vista" "presente" "assente"
            continue
        }

        $expected = $teamMap[$key]
        $actual = $withMap[$key]

        foreach ($field in @("competizione","squadra","avversaria","tipiRU","dettaglioRU","esito")) {
            Check-EqualText $problems $season "partiteConRU" $key $field $expected.$field $actual.$field
        }

        foreach ($field in @("numeroRU","valoreRUTotale","puntiSquadra","puntiAvversaria","golSquadra","golAvversaria")) {
            Check-EqualNumber $problems $season "partiteConRU" $key $field $expected.$field $actual.$field
        }
    }

    # 2) Ricostruisce ruTeamMatch direttamente da ruDettaglio.
    $detailGroups = $ruDetail | Group-Object { Key-MatchTeam $_ }

    foreach ($group in $detailGroups) {
        $rows = @($group.Group)
        $first = $rows[0]
        $key = [string]$group.Name

        if (-not $teamMap.ContainsKey($key)) {
            Add-Problem $problems $season "ruTeamMatch" $key "Aggregato mancante" "presente" "assente"
            continue
        }

        $actual = $teamMap[$key]
        $expectedCount = $rows.Count
        $expectedValue = ($rows | Measure-Object -Property valoreRU -Sum).Sum
        $expectedTypes = @($rows | ForEach-Object { ([string]$_.tipoRU).Trim() } | Where-Object { $_ } | Sort-Object -Unique) -join ","

        Check-EqualNumber $problems $season "ruTeamMatch" $key "numeroRU" $expectedCount $actual.numeroRU
        Check-EqualNumber $problems $season "ruTeamMatch" $key "valoreRUTotale" $expectedValue $actual.valoreRUTotale

        $actualTypes = @(([string]$actual.tipiRU) -split "," | ForEach-Object { $_.Trim() } | Where-Object { $_ } | Sort-Object -Unique) -join ","
        Check-EqualText $problems $season "ruTeamMatch" $key "tipiRU" $expectedTypes $actualTypes
        Check-EqualText $problems $season "ruTeamMatch" $key "competizione" $first.competizione $actual.competizione
        Check-EqualText $problems $season "ruTeamMatch" $key "squadra" $first.squadra $actual.squadra
        Check-EqualText $problems $season "ruTeamMatch" $key "avversaria" $first.avversaria $actual.avversaria
    }

    # 3) partiteConPiuRU: una riga per incontro, somma di tutte le RU della gara.
    $matchGroups = $ruDetail | Group-Object idIncontro
    $mostMap = To-Map $partiteConPiuRU { param($r) Key-Match $r }

    if ($matchGroups.Count -ne $mostMap.Count) {
        Add-Problem $problems $season "partiteConPiuRU" "(conteggio)" "Numero incontri diverso" $matchGroups.Count $mostMap.Count
    }

    foreach ($group in $matchGroups) {
        $rows = @($group.Group)
        $key = [string]$group.Name

        if (-not $mostMap.ContainsKey($key)) {
            Add-Problem $problems $season "partiteConPiuRU" $key "Incontro RU mancante" "presente" "assente"
            continue
        }

        $actual = $mostMap[$key]
        $expectedCount = $rows.Count
        $expectedValue = ($rows | Measure-Object -Property valoreRU -Sum).Sum

        Check-EqualNumber $problems $season "partiteConPiuRU" $key "numeroRU" $expectedCount $actual.numeroRU
        Check-EqualNumber $problems $season "partiteConPiuRU" $key "valoreRUTotale" $expectedValue $actual.valoreRUTotale
    }

    # 4) partiteControRU deve essere l'inversione di ogni partita con RU.
    $againstMap = To-Map $partiteControRU { param($r) Key-MatchTeam $r }

    if ($againstMap.Count -ne $partiteConRU.Count) {
        Add-Problem $problems $season "partiteControRU" "(conteggio)" "Numero righe diverso da partiteConRU" $partiteConRU.Count $againstMap.Count
    }

    foreach ($with in $partiteConRU) {
        $key = [string]$with.idIncontro + "|" + [string]$with.idAvversaria

        if (-not $againstMap.ContainsKey($key)) {
            Add-Problem $problems $season "partiteControRU" $key "Vista contro RU mancante" "presente" "assente"
            continue
        }

        $against = $againstMap[$key]
        Check-EqualText $problems $season "partiteControRU" $key "avversariaConRU" $with.squadra $against.avversariaConRU
        Check-EqualNumber $problems $season "partiteControRU" $key "numeroRUAvversaria" $with.numeroRU $against.numeroRUAvversaria
        Check-EqualNumber $problems $season "partiteControRU" $key "valoreRUAvversaria" $with.valoreRUTotale $against.valoreRUAvversaria
        Check-EqualText $problems $season "partiteControRU" $key "tipiRUAvversaria" $with.tipiRU $against.tipiRUAvversaria
        Check-EqualText $problems $season "partiteControRU" $key "competizione" $with.competizione $against.competizione
    }

    # 5) Bilanci con RU e contro RU, ricostruiti dalle rispettive viste gara.
    foreach ($pair in @(
        [pscustomobject]@{ Rows=$partiteConRU; Balance=$bilancioConRU; CountField="partiteConRU"; Section="bilancioConRU" },
        [pscustomobject]@{ Rows=$partiteControRU; Balance=$bilancioControRU; CountField="partiteControRU"; Section="bilancioControRU" }
    )) {
        $balanceMap = To-Map $pair.Balance { param($r) [string]$r.idSquadra }

        foreach ($group in ($pair.Rows | Group-Object idSquadra)) {
            $rows = @($group.Group)
            $key = [string]$group.Name

            if (-not $balanceMap.ContainsKey($key)) {
                Add-Problem $problems $season $pair.Section $key "Squadra mancante" "presente" "assente"
                continue
            }

            $actual = $balanceMap[$key]
            $wins = @($rows | Where-Object { [string]$_.esito -eq "V" }).Count
            $draws = @($rows | Where-Object { [string]$_.esito -eq "N" }).Count
            $losses = @($rows | Where-Object { [string]$_.esito -eq "P" }).Count
            $count = $rows.Count
            $avgPts = (($rows | Measure-Object -Property puntiSquadra -Sum).Sum) / $count
            $avgOppPts = (($rows | Measure-Object -Property puntiAvversaria -Sum).Sum) / $count
            $avgGoals = (($rows | Measure-Object -Property golSquadra -Sum).Sum) / $count
            $avgOppGoals = (($rows | Measure-Object -Property golAvversaria -Sum).Sum) / $count

            Check-EqualNumber $problems $season $pair.Section $key $pair.CountField $count $actual.($pair.CountField)
            Check-EqualNumber $problems $season $pair.Section $key "V" $wins $actual.V
            Check-EqualNumber $problems $season $pair.Section $key "N" $draws $actual.N
            Check-EqualNumber $problems $season $pair.Section $key "P" $losses $actual.P
            Check-EqualNumber $problems $season $pair.Section $key "mediaPuntiSquadra" $avgPts $actual.mediaPuntiSquadra
            Check-EqualNumber $problems $season $pair.Section $key "mediaPuntiAvversaria" $avgOppPts $actual.mediaPuntiAvversaria
            Check-EqualNumber $problems $season $pair.Section $key "mediaGolSquadra" $avgGoals $actual.mediaGolSquadra
            Check-EqualNumber $problems $season $pair.Section $key "mediaGolAvversaria" $avgOppGoals $actual.mediaGolAvversaria
        }
    }

    # 6) Media punti deve essere una proiezione coerente dei bilanci.
    foreach ($pair in @(
        [pscustomobject]@{ Balance=$bilancioConRU; Media=$mediaPuntiConRU; CountField="partiteConRU"; Section="mediaPuntiConRU" },
        [pscustomobject]@{ Balance=$bilancioControRU; Media=$mediaPuntiControRU; CountField="partiteControRU"; Section="mediaPuntiControRU" }
    )) {
        $mediaMap = To-Map $pair.Media { param($r) [string]$r.idSquadra }

        foreach ($balance in $pair.Balance) {
            $key = [string]$balance.idSquadra

            if (-not $mediaMap.ContainsKey($key)) {
                Add-Problem $problems $season $pair.Section $key "Squadra mancante" "presente" "assente"
                continue
            }

            $actual = $mediaMap[$key]
            Check-EqualNumber $problems $season $pair.Section $key $pair.CountField $balance.($pair.CountField) $actual.($pair.CountField)
            Check-EqualNumber $problems $season $pair.Section $key "mediaPuntiSquadra" $balance.mediaPuntiSquadra $actual.mediaPuntiSquadra
            Check-EqualNumber $problems $season $pair.Section $key "mediaPuntiAvversaria" $balance.mediaPuntiAvversaria $actual.mediaPuntiAvversaria
            Check-EqualNumber $problems $season $pair.Section $key "differenzaMedia" ((Num $balance.mediaPuntiSquadra) - (Num $balance.mediaPuntiAvversaria)) $actual.differenzaMedia
        }
    }

    # 7) Tipo RU usata: conteggi e valori direttamente da ruDettaglio.
    $typeMap = To-Map $tipoRUUsata { param($r) [string]$r.idSquadra }

    foreach ($group in ($ruDetail | Group-Object idSquadra)) {
        $rows = @($group.Group)
        $key = [string]$group.Name

        if (-not $typeMap.ContainsKey($key)) {
            Add-Problem $problems $season "tipoRUUsata" $key "Squadra mancante" "presente" "assente"
            continue
        }

        $actual = $typeMap[$key]
        $totalValue = 0.0

        foreach ($type in @("PU","DU","CU","AU")) {
            $typed = @($rows | Where-Object { [string]$_.tipoRU -eq $type })
            $count = $typed.Count
            $value = if ($count -gt 0) { ($typed | Measure-Object -Property valoreRU -Sum).Sum } else { 0 }
            $totalValue += Num $value

            Check-EqualNumber $problems $season "tipoRUUsata" $key $type $count $actual.$type
            Check-EqualNumber $problems $season "tipoRUUsata" $key ("valore" + $type) $value $actual.("valore" + $type)
        }

        Check-EqualNumber $problems $season "tipoRUUsata" $key "totaleRU" $rows.Count $actual.totaleRU
        Check-EqualNumber $problems $season "tipoRUUsata" $key "valoreTotale" $totalValue $actual.valoreTotale
    }

    # 8) Decisività: coerenza interna dell'effetto e dei punti di classifica.
    foreach ($row in $ruDecisiva) {
        $key = Key-MatchTeam $row
        $expectedPoints = switch ([string]$row.effetto) {
            "Da sconfitta a pareggio" { 1 }
            "Da pareggio a vittoria" { 2 }
            "Da sconfitta a vittoria" { 3 }
            default { -1 }
        }

        if ($expectedPoints -lt 0) {
            Add-Problem $problems $season "ruDecisiva" $key "Effetto sconosciuto" "effetto noto" $row.effetto
        }
        else {
            Check-EqualNumber $problems $season "ruDecisiva" $key "puntiClassificaGuadagnati" $expectedPoints $row.puntiClassificaGuadagnati
        }
    }

    foreach ($row in $ruDecisivaContro) {
        $key = Key-MatchTeam $row
        $expectedPoints = switch ([string]$row.danno) {
            "Da vittoria a pareggio" { 2 }
            "Da pareggio a sconfitta" { 1 }
            "Da vittoria a sconfitta" { 3 }
            default { -1 }
        }

        if ($expectedPoints -lt 0) {
            Add-Problem $problems $season "ruDecisivaContro" $key "Danno sconosciuto" "danno noto" $row.danno
        }
        else {
            Check-EqualNumber $problems $season "ruDecisivaContro" $key "puntiClassificaPersi" $expectedPoints $row.puntiClassificaPersi
        }
    }

    # 9) Bilanci decisività derivati dalle righe decisive.
    $decBalanceMap = To-Map $bilancioRUDecisiva { param($r) [string]$r.idSquadra }

    foreach ($group in ($ruDecisiva | Group-Object idSquadra)) {
        $rows = @($group.Group)
        $key = [string]$group.Name

        if (-not $decBalanceMap.ContainsKey($key)) {
            Add-Problem $problems $season "bilancioRUDecisiva" $key "Squadra mancante" "presente" "assente"
            continue
        }

        $actual = $decBalanceMap[$key]
        $wins = @($rows | Where-Object { [string]$_.effetto -in @("Da pareggio a vittoria","Da sconfitta a vittoria") }).Count
        $draws = @($rows | Where-Object { [string]$_.effetto -eq "Da sconfitta a pareggio" }).Count
        $points = ($rows | Measure-Object -Property puntiClassificaGuadagnati -Sum).Sum

        Check-EqualNumber $problems $season "bilancioRUDecisiva" $key "partiteRUDecisiva" $rows.Count $actual.partiteRUDecisiva
        Check-EqualNumber $problems $season "bilancioRUDecisiva" $key "vittorieGrazieRU" $wins $actual.vittorieGrazieRU
        Check-EqualNumber $problems $season "bilancioRUDecisiva" $key "pareggiGrazieRU" $draws $actual.pareggiGrazieRU
        Check-EqualNumber $problems $season "bilancioRUDecisiva" $key "puntiClassificaGuadagnati" $points $actual.puntiClassificaGuadagnati
    }

    $decAgainstBalanceMap = To-Map $bilancioRUDecisivaContro { param($r) [string]$r.idSquadra }

    foreach ($group in ($ruDecisivaContro | Group-Object idSquadra)) {
        $rows = @($group.Group)
        $key = [string]$group.Name

        if (-not $decAgainstBalanceMap.ContainsKey($key)) {
            Add-Problem $problems $season "bilancioRUDecisivaContro" $key "Squadra mancante" "presente" "assente"
            continue
        }

        $actual = $decAgainstBalanceMap[$key]
        $winsLost = @($rows | Where-Object { [string]$_.danno -in @("Da vittoria a pareggio","Da vittoria a sconfitta") }).Count
        $drawsLost = @($rows | Where-Object { [string]$_.danno -eq "Da pareggio a sconfitta" }).Count
        $points = ($rows | Measure-Object -Property puntiClassificaPersi -Sum).Sum

        Check-EqualNumber $problems $season "bilancioRUDecisivaContro" $key "partiteControRUDecisiva" $rows.Count $actual.partiteControRUDecisiva
        Check-EqualNumber $problems $season "bilancioRUDecisivaContro" $key "vittoriePerse" $winsLost $actual.vittoriePerse
        Check-EqualNumber $problems $season "bilancioRUDecisivaContro" $key "pareggiDiventatiSconfitte" $drawsLost $actual.pareggiDiventatiSconfitte
        Check-EqualNumber $problems $season "bilancioRUDecisivaContro" $key "puntiClassificaPersi" $points $actual.puntiClassificaPersi
    }

    # 10) Competizione deve essere presente su tutte le viste gara/dettaglio.
    foreach ($pair in @(
        [pscustomobject]@{ Name="partiteConPiuRU"; Rows=$partiteConPiuRU },
        [pscustomobject]@{ Name="partiteConRU"; Rows=$partiteConRU },
        [pscustomobject]@{ Name="partiteControRU"; Rows=$partiteControRU },
        [pscustomobject]@{ Name="ruDecisiva"; Rows=$ruDecisiva },
        [pscustomobject]@{ Name="ruDecisivaContro"; Rows=$ruDecisivaContro },
        [pscustomobject]@{ Name="ruDettaglio"; Rows=$ruDetail },
        [pscustomobject]@{ Name="ruTeamMatch"; Rows=$ruTeamMatch }
    )) {
        foreach ($row in $pair.Rows) {
            if ([string]::IsNullOrWhiteSpace([string]$row.competizione)) {
                Add-Problem $problems $season $pair.Name (Key-MatchTeam $row) "Competizione mancante" "nome competizione" ""
            }
        }
    }
}

$problems | Export-Csv -LiteralPath $outCsv -NoTypeInformation -Encoding UTF8

$checkNames = @($checks.Keys)
$summary = @(
    foreach ($name in $checkNames) {
        $count = @($problems | Where-Object Sezione -eq $name).Count
        [pscustomobject]@{
            Record = $name
            StagioniControllate = $checks[$name]
            Problemi = $count
            Esito = if ($count -eq 0) { "OK" } else { "ERRORE" }
        }
    }
)

Write-Host ""
Write-Host "=== AUDIT SEMANTICO RU v26 ==="
$summary | Format-Table -AutoSize

Write-Host ""
Write-Host "Stagioni controllate          : $seasonCount"
Write-Host "Righe RU dettaglio            : $detailRows"
Write-Host "Partite-squadra con RU        : $teamMatchRows"
Write-Host "Problemi totali               : $($problems.Count)"
Write-Host "Dettaglio CSV                  : $outCsv"

if ($problems.Count -gt 0) {
    Write-Host ""
    Write-Host "=== PRIMI 25 PROBLEMI ==="
    $problems | Select-Object -First 25 | Format-Table -AutoSize
    exit 1
}

Write-Host ""
Write-Host "AUDIT SEMANTICO RU: OK"
