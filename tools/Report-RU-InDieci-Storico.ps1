param(
    [string]$RecordsNextRoot = "E:\FCM\plugin\Mauz_strom2014Full\RecordsNext2",
    [string]$FcmRoot = "E:\FCM\data"
)

$ErrorActionPreference = "Stop"

# ============================================================
# CONFIGURAZIONE
# ============================================================

$reportsRoot = Join-Path $RecordsNextRoot "data\reports"
$ruArchiveRoot = Join-Path $RecordsNextRoot "data\records-archive\riserveufficio"
$outputDir = Join-Path $RecordsNextRoot "data\personal-reports"

$CompetizioniEscluse = @(
    "Youth League"
)

New-Item -ItemType Directory -Path $outputDir -Force | Out-Null


# ============================================================
# FUNZIONI
# ============================================================

function Get-Prop {
    param(
        $Object,
        [string]$Name
    )

    if ($null -eq $Object) {
        return $null
    }

    $p = $Object.PSObject.Properties[$Name]

    if ($null -eq $p) {
        return $null
    }

    return $p.Value
}


function Get-Percent {
    param(
        [double]$Numerator,
        [double]$Denominator,
        [int]$Decimals = 2
    )

    if ($Denominator -le 0) {
        return 0
    }

    return [math]::Round(
        ($Numerator / $Denominator) * 100,
        $Decimals
    )
}


function Is-ExcludedCompetition {
    param([string]$Competition)

    if ([string]::IsNullOrWhiteSpace($Competition)) {
        return $false
    }

    foreach ($excluded in $CompetizioniEscluse) {

        if (
            $Competition.Trim().Equals(
                $excluded,
                [System.StringComparison]::OrdinalIgnoreCase
            )
        ) {
            return $true
        }
    }

    return $false
}


function Normalize-Effect {
    param($Effect)

    $text = ([string]$Effect).Trim().ToUpperInvariant()

    if ($text -match "SCONFITTA.*PAREGGIO") {
        return "SCONFITTA_PAREGGIO"
    }

    if ($text -match "PAREGGIO.*VITTORIA") {
        return "PAREGGIO_VITTORIA"
    }

    if ($text -match "SCONFITTA.*VITTORIA") {
        return "SCONFITTA_VITTORIA"
    }

    return "NESSUN_EFFETTO"
}


function Get-FcmFileForSeason {
    param([string]$Season)

    $parts = $Season -split "_"

    if ($parts.Count -ne 2) {
        throw "Stagione non valida: $Season"
    }

    $startYear = $parts[0]
    $endYear = $parts[1]

    $pattern = "*${startYear}_${endYear}*.fcm"

    $files = @(
        Get-ChildItem $FcmRoot -Filter $pattern |
        Where-Object { -not $_.PSIsContainer }
    )

    if ($files.Count -eq 1) {
        return $files[0].FullName
    }

    # Caso storico speciale 2006/2007
    if ($Season -eq "2006_2007") {

        $special = Join-Path $FcmRoot "Lega Reame Perduto 2006_2007-2-2006.fcm"

        if (Test-Path $special) {
            return $special
        }
    }

    if ($files.Count -eq 0) {
        throw "Nessun FCM trovato per $Season in $FcmRoot"
    }

    throw "Più FCM trovati per ${Season}: $($files.Name -join ', ')"
}


function Read-Recordset {
    param(
        $Connection,
        [string]$Sql
    )

    $rs = New-Object -ComObject ADODB.Recordset
    $rs.Open($Sql, $Connection)

    $rows = New-Object System.Collections.Generic.List[object]

    while (-not $rs.EOF) {

        $obj = [ordered]@{}

        for ($i = 0; $i -lt $rs.Fields.Count; $i++) {

            $name = $rs.Fields.Item($i).Name
            $value = $rs.Fields.Item($i).Value

            $obj[$name] = $value
        }

        $rows.Add([pscustomobject]$obj)

        $rs.MoveNext()
    }

    $rs.Close()

    return @($rows)
}


# ============================================================
# UNIVERSO PARTITE RECORDSNEXT
#
# Usiamo i normalized per sapere quali incontri fanno parte
# del nostro archivio storico. IDIncontro è univoco nel FCM
# della singola stagione.
# ============================================================

$eligibleMatches = @{}
$seasons = @{}

$normalizedFiles = @(
    Get-ChildItem $reportsRoot -Recurse -Filter "season_normalized_*.json" |
    Where-Object { -not $_.PSIsContainer }
)

foreach ($file in $normalizedFiles) {

    $season = Split-Path $file.DirectoryName -Leaf
    $seasons[$season] = $true

    $doc = Get-Content $file.FullName -Raw -Encoding UTF8 |
        ConvertFrom-Json

    foreach ($row in @($doc.partiteSquadra)) {

        if ($null -eq $row) {
            continue
        }

        $idMatch = [string]$row.idIncontro

        if ([string]::IsNullOrWhiteSpace($idMatch)) {
            continue
        }

        $eligibleMatches["$season|$idMatch"] = $true
    }
}


# ============================================================
# RU PER SQUADRA-PARTITA
# ============================================================

$ruByTeamMatch = @{}

foreach ($file in $normalizedFiles) {

    $season = Split-Path $file.DirectoryName -Leaf

    $doc = Get-Content $file.FullName -Raw -Encoding UTF8 |
        ConvertFrom-Json

    foreach ($row in @($doc.riserveUfficioDettaglio)) {

        if ($null -eq $row) {
            continue
        }

        $idMatch = [string]$row.idIncontro
        $idTeam = [string]$row.idSquadra

        if (
            [string]::IsNullOrWhiteSpace($idMatch) -or
            [string]::IsNullOrWhiteSpace($idTeam)
        ) {
            continue
        }

        $key = "$season|$idMatch|$idTeam"

        if (-not $ruByTeamMatch.ContainsKey($key)) {
            $ruByTeamMatch[$key] = 0
        }

        $ruByTeamMatch[$key]++
    }
}


# ============================================================
# EFFETTI RU
# ============================================================

$effects = @{}

$ruArchiveFiles = @(
    Get-ChildItem $ruArchiveRoot -Recurse -Filter "riserveufficio.json" |
    Where-Object { -not $_.PSIsContainer }
)

foreach ($file in $ruArchiveFiles) {

    $season = Split-Path $file.DirectoryName -Leaf

    $doc = Get-Content $file.FullName -Raw -Encoding UTF8 |
        ConvertFrom-Json

    $views = Get-Prop $doc "views"

    foreach ($row in @(Get-Prop $views "ruDecisiva")) {

        if ($null -eq $row) {
            continue
        }

        if (Is-ExcludedCompetition ([string]$row.competizione)) {
            continue
        }

        $idMatch = [string]$row.idIncontro
        $idTeam = [string]$row.idSquadra

        if (
            [string]::IsNullOrWhiteSpace($idMatch) -or
            [string]::IsNullOrWhiteSpace($idTeam)
        ) {
            continue
        }

        $key = "$season|$idMatch|$idTeam"

        $effects[$key] = Normalize-Effect $row.effetto
    }
}


# ============================================================
# LETTURA INCONTRO DA TUTTI GLI FCM
# ============================================================

$allTeamMatches = New-Object System.Collections.Generic.List[object]

foreach ($season in @($seasons.Keys | Sort-Object)) {

    $fcmFile = Get-FcmFileForSeason $season

    Write-Host ""
    Write-Host "Lettura $season"
    Write-Host "FCM: $fcmFile"

    $cn = New-Object -ComObject ADODB.Connection

    $connectionString =
        "Provider=Microsoft.Jet.OLEDB.4.0;Data Source=$fcmFile;"

    $cn.Open($connectionString)

    try {

        # Squadre
        $teamRows = Read-Recordset `
            $cn `
            "SELECT ID, Nome FROM Fantasquadra"

        $teamNames = @{}

        foreach ($team in $teamRows) {
            $teamNames[[string]$team.ID] = [string]$team.Nome
        }


        # Competizioni per girone
        $gironeRows = Read-Recordset `
            $cn `
            "SELECT g.ID, c.Nome AS Competizione FROM Girone AS g LEFT JOIN Competizione AS c ON g.IDCompetizione = c.ID"

        $competitionByGirone = @{}

        foreach ($g in $gironeRows) {
            $competitionByGirone[[string]$g.ID] = [string]$g.Competizione
        }


        # Incontri
        $matches = Read-Recordset `
            $cn `
            "SELECT ID, IDCasa, IDFuori, IDGirone, NGiocCasa, NGiocFuori, GolCasa, GolFuori, Giocato, GiornataDiA FROM Incontro"

        foreach ($match in $matches) {

            $idMatch = [string]$match.ID

            if (-not $eligibleMatches.ContainsKey("$season|$idMatch")) {
                continue
            }

            $idCasa = [string]$match.IDCasa
            $idFuori = [string]$match.IDFuori

            if (
                [string]::IsNullOrWhiteSpace($idCasa) -or
                [string]::IsNullOrWhiteSpace($idFuori) -or
                $idCasa -eq "0" -or
                $idFuori -eq "0"
            ) {
                continue
            }

            $competition = ""

            $gironeKey = [string]$match.IDGirone

            if ($competitionByGirone.ContainsKey($gironeKey)) {
                $competition = $competitionByGirone[$gironeKey]
            }

            if (Is-ExcludedCompetition $competition) {
                continue
            }


            $golCasa = [int]$match.GolCasa
            $golFuori = [int]$match.GolFuori

            $esitoCasa = if ($golCasa -gt $golFuori) {
                "VITTORIA"
            }
            elseif ($golCasa -eq $golFuori) {
                "PAREGGIO"
            }
            else {
                "SCONFITTA"
            }

            $esitoFuori = if ($golFuori -gt $golCasa) {
                "VITTORIA"
            }
            elseif ($golFuori -eq $golCasa) {
                "PAREGGIO"
            }
            else {
                "SCONFITTA"
            }


            # CASA
            $keyCasa = "$season|$idMatch|$idCasa"

            $ruCasa = 0

            if ($ruByTeamMatch.ContainsKey($keyCasa)) {
                $ruCasa = [int]$ruByTeamMatch[$keyCasa]
            }

            $effectCasa = "NESSUN_EFFETTO"

            if ($effects.ContainsKey($keyCasa)) {
                $effectCasa = $effects[$keyCasa]
            }

            $allTeamMatches.Add(
                [pscustomobject]@{
                    Stagione = $season
                    Competizione = $competition
                    IdIncontro = $idMatch
                    IdSquadra = $idCasa
                    Squadra = $teamNames[$idCasa]
                    Lato = "Casa"
                    NGioc = [int]$match.NGiocCasa
                    NumeroRU = $ruCasa
                    Esito = $esitoCasa
                    EffettoRU = $effectCasa
                }
            )


            # FUORI
            $keyFuori = "$season|$idMatch|$idFuori"

            $ruFuori = 0

            if ($ruByTeamMatch.ContainsKey($keyFuori)) {
                $ruFuori = [int]$ruByTeamMatch[$keyFuori]
            }

            $effectFuori = "NESSUN_EFFETTO"

            if ($effects.ContainsKey($keyFuori)) {
                $effectFuori = $effects[$keyFuori]
            }

            $allTeamMatches.Add(
                [pscustomobject]@{
                    Stagione = $season
                    Competizione = $competition
                    IdIncontro = $idMatch
                    IdSquadra = $idFuori
                    Squadra = $teamNames[$idFuori]
                    Lato = "Fuori"
                    NGioc = [int]$match.NGiocFuori
                    NumeroRU = $ruFuori
                    Esito = $esitoFuori
                    EffettoRU = $effectFuori
                }
            )
        }
    }
    finally {

        if ($cn.State -ne 0) {
            $cn.Close()
        }
    }
}


# ============================================================
# POPOLAZIONI
# ============================================================

$totalMatches = @(
    $allTeamMatches |
    Group-Object Stagione,IdIncontro
).Count


# Esattamente 10 giocatori validi
$in10 = @(
    $allTeamMatches |
    Where-Object {
        $_.NGioc -eq 10
    }
)


# 9 o meno, tenuti separati
$sotto10 = @(
    $allTeamMatches |
    Where-Object {
        $_.NGioc -lt 10
    }
)


$in10SenzaRU = @(
    $in10 |
    Where-Object {
        $_.NumeroRU -eq 0
    }
)


$in10ConRU = @(
    $in10 |
    Where-Object {
        $_.NumeroRU -gt 0
    }
)


$in10Con1RU = @(
    $in10 |
    Where-Object {
        $_.NumeroRU -eq 1
    }
)


$in10Con2RU = @(
    $in10 |
    Where-Object {
        $_.NumeroRU -eq 2
    }
)


$in10ConPiu2RU = @(
    $in10 |
    Where-Object {
        $_.NumeroRU -gt 2
    }
)


# ============================================================
# STATISTICHE
# ============================================================

function Get-Stats {
    param(
        [object[]]$Rows,
        [string]$Categoria
    )

    $matches = @(
        $Rows |
        Group-Object Stagione,IdIncontro
    ).Count

    $wins = @(
        $Rows |
        Where-Object { $_.Esito -eq "VITTORIA" }
    ).Count

    $draws = @(
        $Rows |
        Where-Object { $_.Esito -eq "PAREGGIO" }
    ).Count

    $losses = @(
        $Rows |
        Where-Object { $_.Esito -eq "SCONFITTA" }
    ).Count

    $sp = @(
        $Rows |
        Where-Object {
            $_.EffettoRU -eq "SCONFITTA_PAREGGIO"
        }
    ).Count

    $pv = @(
        $Rows |
        Where-Object {
            $_.EffettoRU -eq "PAREGGIO_VITTORIA"
        }
    ).Count

    $sv = @(
        $Rows |
        Where-Object {
            $_.EffettoRU -eq "SCONFITTA_VITTORIA"
        }
    ).Count

    $decisive = $sp + $pv + $sv

    [pscustomobject]@{
        Categoria = $Categoria

        SquadrePartita = $Rows.Count

        PartiteDistinte = $matches

        PercentualeSulTotale = Get-Percent `
            $matches `
            $totalMatches

        Vittorie = $wins
        Pareggi = $draws
        Sconfitte = $losses

        SconfittaPareggio = $sp
        PareggioVittoria = $pv
        SconfittaVittoria = $sv

        TotaleEffettiDecisivi = $decisive

        PercentualeEffettiDecisivi = Get-Percent `
            $decisive `
            $Rows.Count
    }
}


$summary = @()

$summary += Get-Stats `
    $in10 `
    "SQUADRA CON ESATTAMENTE 10 GIOCATORI VALIDI"

$summary += Get-Stats `
    $in10SenzaRU `
    "IN 10 SENZA AVER UTILIZZATO RU"

$summary += Get-Stats `
    $in10ConRU `
    "IN 10 NONOSTANTE L'UTILIZZO DI ALMENO UNA RU"

$summary += Get-Stats `
    $in10Con1RU `
    "IN 10 CON ESATTAMENTE 1 RU"

$summary += Get-Stats `
    $in10Con2RU `
    "IN 10 CON ESATTAMENTE 2 RU"

$summary += Get-Stats `
    $in10ConPiu2RU `
    "IN 10 CON PIÙ DI 2 RU"

$summary += Get-Stats `
    $sotto10 `
    "SQUADRA CON 9 O MENO GIOCATORI VALIDI"


# ============================================================
# OUTPUT CSV
# ============================================================

$csv = Join-Path $outputDir "RU_squadre_in_10_storico.csv"
$html = Join-Path $outputDir "RU_squadre_in_10_storico.html"

$summary |
    Export-Csv `
        -Path $csv `
        -Delimiter ";" `
        -NoTypeInformation `
        -Encoding UTF8


# ============================================================
# HTML
# ============================================================

$in10Matches = @(
    $in10 |
    Group-Object Stagione,IdIncontro
).Count

$in10Pct = Get-Percent $in10Matches $totalMatches

$in10ConRuMatches = @(
    $in10ConRU |
    Group-Object Stagione,IdIncontro
).Count

$in10ConRuPct = Get-Percent `
    $in10ConRU.Count `
    $in10.Count


$rowsHtml = foreach ($r in $summary) {

@"
<tr>
<td class="cat">$([System.Net.WebUtility]::HtmlEncode($r.Categoria))</td>
<td>$($r.SquadrePartita)</td>
<td>$($r.PartiteDistinte)</td>
<td>$($r.PercentualeSulTotale)%</td>
<td>$($r.Vittorie)</td>
<td>$($r.Pareggi)</td>
<td>$($r.Sconfitte)</td>
<td>$($r.SconfittaPareggio)</td>
<td>$($r.PareggioVittoria)</td>
<td>$($r.SconfittaVittoria)</td>
<td class="important">$($r.TotaleEffettiDecisivi)</td>
<td class="important">$($r.PercentualeEffettiDecisivi)%</td>
</tr>
"@
}


$htmlContent = @"
<!doctype html>

<html lang="it">

<head>

<meta charset="utf-8">

<title>Squadre rimaste in 10 - Riserve d'Ufficio</title>

<style>

body {
    font-family: "Trebuchet MS", Verdana, Arial, sans-serif;
    margin: 22px;
    color: #111;
    background: #fff;
}

h1, h2 {
    color: #003366;
}

h1 {
    margin-bottom: 4px;
}

.subtitle {
    color: #666;
    margin-bottom: 20px;
}

.box {
    padding: 15px;
    margin-bottom: 20px;
    border: 1px solid #003366;
    background: #f7f7f7;
}

.big {
    font-size: 20px;
    font-weight: bold;
    color: #003366;
}

.warning {
    font-weight: bold;
    color: #990000;
}

table {
    width: 100%;
    border-collapse: collapse;
    font-size: 13px;
}

th {
    border: 1px solid #003366;
    background: #003366;
    color: white;
    padding: 7px 5px;
    text-align: center;
}

td {
    border: 1px solid #003366;
    padding: 6px 5px;
    text-align: right;
}

tbody tr:nth-child(even) {
    background: #ccffff;
}

td.cat {
    text-align: left;
    color: #003366;
    font-weight: bold;
}

td.important {
    color: #990000;
    font-weight: bold;
}

.notes {
    margin-top: 22px;
    line-height: 1.5;
}

</style>

</head>

<body>

<h1>Quando una squadra rimane in 10</h1>

<div class="subtitle">
RecordsNext 2.0 — analisi storica personale
</div>


<div class="box">

<h2>Quadro generale</h2>

<p>
L'archivio analizzato contiene
<span class="big">$totalMatches</span>
partite.
</p>

<p>
In
<span class="big">$in10Matches</span>
partite almeno una squadra ha terminato il calcolo FCM
con <strong>esattamente 10 giocatori validi</strong>.
</p>

<p>
L'incidenza è pari al
<span class="big">$in10Pct%</span>
delle partite storiche.
</p>

<p>
Le singole prestazioni di squadra in 10 sono
<strong>$($in10.Count)</strong>.
</p>

<p>
Di queste,
<strong>$($in10ConRU.Count)</strong>
si sono verificate
<span class="warning">nonostante la squadra avesse già utilizzato almeno una Riserva d'Ufficio</span>.
Sono il
<strong>$in10ConRuPct%</strong>
di tutte le prestazioni in 10.
</p>

</div>


<table>

<thead>

<tr>

<th>Situazione</th>
<th>Volte accaduto<br>a una squadra</th>
<th>Partite<br>coinvolte</th>
<th>% di tutte<br>le partite</th>

<th>Vittorie</th>
<th>Pareggi</th>
<th>Sconfitte</th>

<th>RU: sconfitta<br>→ pareggio</th>
<th>RU: pareggio<br>→ vittoria</th>
<th>RU: sconfitta<br>→ vittoria</th>

<th>Effetti decisivi<br>totali</th>
<th>% effetti decisivi<br>sui casi</th>

</tr>

</thead>

<tbody>

$($rowsHtml -join "`r`n")

</tbody>

</table>


<div class="notes">

<h2>Come leggere questa pagina</h2>

<p>
<strong>Giocare in 10</strong> significa che Fantacalcio Manager
ha registrato <strong>10 giocatori validi</strong> per quella squadra
nel campo <code>NGiocCasa</code> o <code>NGiocFuori</code>
della partita.
</p>

<p>
Questo è un dato registrato direttamente da FCM:
non viene ricostruito contando manualmente titolari,
riserve o voti.
</p>

<p>
<strong>In 10 senza RU</strong> significa che la squadra ha avuto
10 giocatori validi e non risulta alcuna Riserva d'Ufficio utilizzata.
</p>

<p>
<strong>In 10 nonostante almeno una RU</strong> è il caso più interessante:
la squadra ha già ricevuto uno o più voti d'ufficio,
ma FCM registra comunque soltanto 10 giocatori validi.
</p>

<p>
Le righe successive distinguono questi casi in base al numero
di RU utilizzate: una, due oppure più di due.
</p>

<p>
<strong>Vittorie, pareggi e sconfitte</strong> indicano il risultato
effettivamente ottenuto dalla squadra che ha giocato in 10.
</p>

<p>
Le tre colonne sull'effetto della RU indicano invece i casi nei quali
i punti forniti dalla Riserva d'Ufficio hanno modificato concretamente
l'esito della partita.
</p>

<p>
La riga <strong>9 o meno giocatori validi</strong> è tenuta separata:
non viene mescolata con i casi in cui la squadra ha giocato
esattamente in 10.
</p>

<p>
La competizione <strong>Youth League</strong> viene esclususa
dall'analisi.
</p>

</div>

</body>

</html>
"@

Set-Content `
    -Path $html `
    -Value $htmlContent `
    -Encoding UTF8


# ============================================================
# CONSOLE
# ============================================================

Write-Host ""
Write-Host "=== SQUADRE IN 10 - REPORT STORICO ==="
Write-Host ""

Write-Host "Partite storiche                  : $totalMatches"
Write-Host "Partite con almeno una squadra in 10: $in10Matches"
Write-Host "Percentuale                       : $in10Pct %"
Write-Host "Prestazioni di squadra in 10      : $($in10.Count)"
Write-Host "In 10 senza RU                    : $($in10SenzaRU.Count)"
Write-Host "In 10 con almeno una RU           : $($in10ConRU.Count)"
Write-Host "  - con 1 RU                      : $($in10Con1RU.Count)"
Write-Host "  - con 2 RU                      : $($in10Con2RU.Count)"
Write-Host "  - con più di 2 RU               : $($in10ConPiu2RU.Count)"
Write-Host "Prestazioni con 9 o meno          : $($sotto10.Count)"
Write-Host ""

$summary | Format-Table -AutoSize

Write-Host ""
Write-Host "CSV : $csv"
Write-Host "HTML: $html"
Write-Host ""
