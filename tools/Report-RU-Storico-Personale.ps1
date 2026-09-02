param(
    [string]$RecordsNextRoot = "E:\FCM\plugin\Mauz_strom2014Full\RecordsNext2"
)

$ErrorActionPreference = "Stop"

# ============================================================
# CONFIGURAZIONE
# ============================================================

$CompetizioniEscluse = @(
    "Youth League"
)

$reportsRoot   = Join-Path $RecordsNextRoot "data\reports"
$ruArchiveRoot = Join-Path $RecordsNextRoot "data\records-archive\riserveufficio"
$outputDir     = Join-Path $RecordsNextRoot "data\personal-reports"

if (-not (Test-Path $reportsRoot)) {
    throw "Cartella reports non trovata: $reportsRoot"
}

if (-not (Test-Path $ruArchiveRoot)) {
    throw "Archivio RU non trovato: $ruArchiveRoot"
}

New-Item -ItemType Directory -Path $outputDir -Force | Out-Null


# ============================================================
# FUNZIONI BASE
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


function Get-Number {
    param($Value)

    if ($null -eq $Value) {
        return 0.0
    }

    $text = ([string]$Value).Trim()

    if ([string]::IsNullOrWhiteSpace($text)) {
        return 0.0
    }

    $text = $text.Replace(",", ".")

    $number = 0.0

    if (
        [double]::TryParse(
            $text,
            [Globalization.NumberStyles]::Any,
            [Globalization.CultureInfo]::InvariantCulture,
            [ref]$number
        )
    ) {
        return $number
    }

    return 0.0
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


function Canonical-Competition {
    param(
        [string]$Value
    )

    if ([string]::IsNullOrWhiteSpace($Value)) {
        return ""
    }

    return (
        $Value.ToLowerInvariant() -replace '[^a-z0-9]', ''
    )
}


function Get-CompetitionFromFile {
    param(
        [System.IO.FileInfo]$File
    )

    $name = $File.BaseName

    if ($name -match '^season_normalized_(.+)$') {
        return $Matches[1].Replace("_", " ")
    }

    return $name
}


function Is-ExcludedCompetition {
    param(
        [string]$Competition
    )

    $canonical = Canonical-Competition $Competition

    foreach ($excluded in $CompetizioniEscluse) {

        if (
            $canonical -eq
            (Canonical-Competition $excluded)
        ) {
            return $true
        }
    }

    return $false
}


function Normalize-Effect {
    param($Effect)

    $text = ([string]$Effect).Trim().ToUpperInvariant()

    if ([string]::IsNullOrWhiteSpace($text)) {
        return "NESSUN_EFFETTO"
    }

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


function Is-KeeperRU {
    param($Row)

    $tipo  = ([string]$Row.TipoRU).Trim().ToUpperInvariant()
    $ruolo = ([string]$Row.RuoloRU).Trim().ToUpperInvariant()

    if ($tipo -eq "PU") {
        return $true
    }

    if ($ruolo -eq "P") {
        return $true
    }

    if ($ruolo -eq "PU") {
        return $true
    }

    if ($ruolo -eq "PORTIERE") {
        return $true
    }

    return $false
}


# ============================================================
# LETTURA DEI FILE NORMALIZZATI
# ============================================================

$allMatches = @{}
$allRu = New-Object System.Collections.Generic.List[object]

$normalizedFiles = @(
    Get-ChildItem `
        $reportsRoot `
        -Recurse `
        -File `
        -Filter "season_normalized_*.json"
)

foreach ($file in $normalizedFiles) {

    $season = Split-Path $file.DirectoryName -Leaf

    $fileCompetition = Get-CompetitionFromFile $file

    if (Is-ExcludedCompetition $fileCompetition) {
        continue
    }

    $doc = Get-Content `
        $file.FullName `
        -Raw `
        -Encoding UTF8 |
        ConvertFrom-Json


    # --------------------------------------------------------
    # PARTITE TOTALI
    # --------------------------------------------------------

    foreach ($row in @(Get-Prop $doc "partiteSquadra")) {

        if ($null -eq $row) {
            continue
        }

        $competition = [string](Get-Prop $row "competizione")

        if ([string]::IsNullOrWhiteSpace($competition)) {
            $competition = $fileCompetition
        }

        if (Is-ExcludedCompetition $competition) {
            continue
        }

        $idMatch = [string](Get-Prop $row "idIncontro")

        if ([string]::IsNullOrWhiteSpace($idMatch)) {
            $idMatch = [string](Get-Prop $row "sourceMatchId")
        }

        if ([string]::IsNullOrWhiteSpace($idMatch)) {
            continue
        }

        $competitionKey = Canonical-Competition $competition

        $matchKey = "$season|$competitionKey|$idMatch"

        $allMatches[$matchKey] = $true
    }


    # --------------------------------------------------------
    # RISERVE D'UFFICIO
    # --------------------------------------------------------

    foreach ($row in @(Get-Prop $doc "riserveUfficioDettaglio")) {

        if ($null -eq $row) {
            continue
        }

        $competition = [string](Get-Prop $row "competizione")

        if ([string]::IsNullOrWhiteSpace($competition)) {
            $competition = $fileCompetition
        }

        if (Is-ExcludedCompetition $competition) {
            continue
        }

        $idMatch = [string](Get-Prop $row "idIncontro")
        $idTeam  = [string](Get-Prop $row "idSquadra")

        if (
            [string]::IsNullOrWhiteSpace($idMatch) -or
            [string]::IsNullOrWhiteSpace($idTeam)
        ) {
            continue
        }

        $allRu.Add(
            [pscustomobject]@{
                Stagione       = $season
                Competizione   = $competition
                CompetizioneKey = Canonical-Competition $competition
                IdIncontro     = $idMatch
                IdSquadra      = $idTeam
                Squadra        = [string](Get-Prop $row "squadra")
                TipoRU         = [string](Get-Prop $row "tipoRU")
                RuoloRU        = [string](Get-Prop $row "ruoloRU")
                ValoreRU       = Get-Number (Get-Prop $row "valoreRU")
            }
        )
    }
}


# ============================================================
# EFFETTI DELLE RU
# ============================================================

$effectsExact = @{}
$effectsFallback = @{}

$ruArchiveFiles = @(
    Get-ChildItem `
        $ruArchiveRoot `
        -Recurse `
        -File `
        -Filter "riserveufficio.json"
)

foreach ($file in $ruArchiveFiles) {

    $season = Split-Path $file.DirectoryName -Leaf

    $doc = Get-Content `
        $file.FullName `
        -Raw `
        -Encoding UTF8 |
        ConvertFrom-Json

    $views = Get-Prop $doc "views"

    if ($null -eq $views) {
        continue
    }

    foreach ($row in @(Get-Prop $views "ruDecisiva")) {

        if ($null -eq $row) {
            continue
        }

        $competition = [string](Get-Prop $row "competizione")

        if (Is-ExcludedCompetition $competition) {
            continue
        }

        $idMatch = [string](Get-Prop $row "idIncontro")
        $idTeam  = [string](Get-Prop $row "idSquadra")

        if (
            [string]::IsNullOrWhiteSpace($idMatch) -or
            [string]::IsNullOrWhiteSpace($idTeam)
        ) {
            continue
        }

        $effect = Normalize-Effect (Get-Prop $row "effetto")

        if ($effect -eq "NESSUN_EFFETTO") {
            continue
        }

        $competitionKey = Canonical-Competition $competition

        if (-not [string]::IsNullOrWhiteSpace($competitionKey)) {

            $exactKey =
                "$season|$competitionKey|$idMatch|$idTeam"

            $effectsExact[$exactKey] = $effect
        }

        $fallbackKey =
            "$season|$idMatch|$idTeam"

        if (-not $effectsFallback.ContainsKey($fallbackKey)) {
            $effectsFallback[$fallbackKey] = $effect
        }
    }
}


function Get-EffectForTeamMatch {
    param(
        $Row
    )

    $exactKey =
        "$($Row.Stagione)|$($Row.CompetizioneKey)|$($Row.IdIncontro)|$($Row.IdSquadra)"

    if ($effectsExact.ContainsKey($exactKey)) {
        return $effectsExact[$exactKey]
    }

    $fallbackKey =
        "$($Row.Stagione)|$($Row.IdIncontro)|$($Row.IdSquadra)"

    if ($effectsFallback.ContainsKey($fallbackKey)) {
        return $effectsFallback[$fallbackKey]
    }

    return "NESSUN_EFFETTO"
}


# ============================================================
# TOTALI GENERALI
# ============================================================

$totalMatches = $allMatches.Count

$teamMatchGroups = @(
    $allRu |
        Group-Object `
            Stagione,
            CompetizioneKey,
            IdIncontro,
            IdSquadra
)

$ruMatchGroups = @(
    $allRu |
        Group-Object `
            Stagione,
            CompetizioneKey,
            IdIncontro
)

$totalRuOccurrences = $allRu.Count
$totalRuTeamMatches  = $teamMatchGroups.Count
$totalRuMatches      = $ruMatchGroups.Count


# ============================================================
# FUNZIONE STATISTICHE EFFETTI
# ============================================================

function Get-EffectCounts {
    param(
        [object[]]$Groups
    )

    $counts = @{
        SCONFITTA_PAREGGIO = 0
        PAREGGIO_VITTORIA  = 0
        SCONFITTA_VITTORIA = 0
        NESSUN_EFFETTO     = 0
    }

    foreach ($group in $Groups) {

        $first = $group.Group[0]

        $effect = Get-EffectForTeamMatch $first

        $counts[$effect]++
    }

    return $counts
}


# ============================================================
# RIGA: ALMENO UNA RU
# ============================================================

$allRuEffects = Get-EffectCounts $teamMatchGroups

$allRuDecisive =
    $allRuEffects["SCONFITTA_PAREGGIO"] +
    $allRuEffects["PAREGGIO_VITTORIA"] +
    $allRuEffects["SCONFITTA_VITTORIA"]


# ============================================================
# FUNZIONE MULTI-RU
# ============================================================

function Get-GroupStats {
    param(
        [object[]]$Groups,
        [string]$Title
    )

    $matches = @{}

    foreach ($group in $Groups) {

        $first = $group.Group[0]

        $key =
            "$($first.Stagione)|$($first.CompetizioneKey)|$($first.IdIncontro)"

        $matches[$key] = $true
    }

    $counts = Get-EffectCounts $Groups

    $teamMatchCount = $Groups.Count
    $matchCount = $matches.Count

    $decisive =
        $counts["SCONFITTA_PAREGGIO"] +
        $counts["PAREGGIO_VITTORIA"] +
        $counts["SCONFITTA_VITTORIA"]

    return [pscustomobject]@{

        Categoria = $Title

        SquadrePartita = $teamMatchCount

        OccorrenzeRU = (
            $Groups |
            ForEach-Object {
                $_.Count
            } |
            Measure-Object -Sum
        ).Sum

        PartiteDistinte = $matchCount

        PercentualePartite =
            Get-Percent $matchCount $totalMatches 2

        SconfittaPareggio =
            $counts["SCONFITTA_PAREGGIO"]

        PctSconfittaPareggio =
            Get-Percent `
                $counts["SCONFITTA_PAREGGIO"] `
                $teamMatchCount

        PareggioVittoria =
            $counts["PAREGGIO_VITTORIA"]

        PctPareggioVittoria =
            Get-Percent `
                $counts["PAREGGIO_VITTORIA"] `
                $teamMatchCount

        SconfittaVittoria =
            $counts["SCONFITTA_VITTORIA"]

        PctSconfittaVittoria =
            Get-Percent `
                $counts["SCONFITTA_VITTORIA"] `
                $teamMatchCount

        TotaleDecisive = $decisive

        PctDecisive =
            Get-Percent `
                $decisive `
                $teamMatchCount
    }
}


# ============================================================
# FUNZIONE TIPO/VALORE RU
# ============================================================

function Get-RuValueStats {
    param(
        [object[]]$Rows,
        [string]$Title
    )

    $teamGroups = @(
        $Rows |
            Group-Object `
                Stagione,
                CompetizioneKey,
                IdIncontro,
                IdSquadra
    )

    $matchGroups = @(
        $Rows |
            Group-Object `
                Stagione,
                CompetizioneKey,
                IdIncontro
    )

    $counts = Get-EffectCounts $teamGroups

    $teamMatchCount = $teamGroups.Count
    $matchCount = $matchGroups.Count

    $decisive =
        $counts["SCONFITTA_PAREGGIO"] +
        $counts["PAREGGIO_VITTORIA"] +
        $counts["SCONFITTA_VITTORIA"]

    return [pscustomobject]@{

        Categoria = $Title

        SquadrePartita = $teamMatchCount

        OccorrenzeRU = $Rows.Count

        PartiteDistinte = $matchCount

        PercentualePartite =
            Get-Percent $matchCount $totalMatches 2

        SconfittaPareggio =
            $counts["SCONFITTA_PAREGGIO"]

        PctSconfittaPareggio =
            Get-Percent `
                $counts["SCONFITTA_PAREGGIO"] `
                $teamMatchCount

        PareggioVittoria =
            $counts["PAREGGIO_VITTORIA"]

        PctPareggioVittoria =
            Get-Percent `
                $counts["PAREGGIO_VITTORIA"] `
                $teamMatchCount

        SconfittaVittoria =
            $counts["SCONFITTA_VITTORIA"]

        PctSconfittaVittoria =
            Get-Percent `
                $counts["SCONFITTA_VITTORIA"] `
                $teamMatchCount

        TotaleDecisive = $decisive

        PctDecisive =
            Get-Percent `
                $decisive `
                $teamMatchCount
    }
}


# ============================================================
# CATEGORIE
# ============================================================

$groups2 = @(
    $teamMatchGroups |
        Where-Object {
            $_.Count -eq 2
        }
)

$groupsMore2 = @(
    $teamMatchGroups |
        Where-Object {
            $_.Count -gt 2
        }
)

$movement4 = @(
    $allRu |
        Where-Object {
            -not (Is-KeeperRU $_) -and
            $_.ValoreRU -eq 4
        }
)

$movement3 = @(
    $allRu |
        Where-Object {
            -not (Is-KeeperRU $_) -and
            $_.ValoreRU -eq 3
        }
)

$keeper3 = @(
    $allRu |
        Where-Object {
            (Is-KeeperRU $_) -and
            $_.ValoreRU -eq 3
        }
)

$keeper2 = @(
    $allRu |
        Where-Object {
            (Is-KeeperRU $_) -and
            $_.ValoreRU -eq 2
        }
)


# ============================================================
# RIEPILOGO
# ============================================================

$summary = @()

$summary += [pscustomobject]@{
    Categoria = "TOTALE PARTITE STORICHE"
    SquadrePartita = ""
    OccorrenzeRU = ""
    PartiteDistinte = $totalMatches
    PercentualePartite = 100
    SconfittaPareggio = ""
    PctSconfittaPareggio = ""
    PareggioVittoria = ""
    PctPareggioVittoria = ""
    SconfittaVittoria = ""
    PctSconfittaVittoria = ""
    TotaleDecisive = ""
    PctDecisive = ""
}


$summary += [pscustomobject]@{

    Categoria = "PARTITE CON ALMENO UNA RU"

    SquadrePartita = $totalRuTeamMatches

    OccorrenzeRU = $totalRuOccurrences

    PartiteDistinte = $totalRuMatches

    PercentualePartite =
        Get-Percent $totalRuMatches $totalMatches 2

    SconfittaPareggio =
        $allRuEffects["SCONFITTA_PAREGGIO"]

    PctSconfittaPareggio =
        Get-Percent `
            $allRuEffects["SCONFITTA_PAREGGIO"] `
            $totalRuTeamMatches

    PareggioVittoria =
        $allRuEffects["PAREGGIO_VITTORIA"]

    PctPareggioVittoria =
        Get-Percent `
            $allRuEffects["PAREGGIO_VITTORIA"] `
            $totalRuTeamMatches

    SconfittaVittoria =
        $allRuEffects["SCONFITTA_VITTORIA"]

    PctSconfittaVittoria =
        Get-Percent `
            $allRuEffects["SCONFITTA_VITTORIA"] `
            $totalRuTeamMatches

    TotaleDecisive = $allRuDecisive

    PctDecisive =
        Get-Percent `
            $allRuDecisive `
            $totalRuTeamMatches
}


$summary += Get-GroupStats `
    $groups2 `
    "ESATTAMENTE 2 RU NELLA STESSA SQUADRA"


$summary += Get-GroupStats `
    $groupsMore2 `
    "PIÙ DI 2 RU NELLA STESSA SQUADRA"


$summary += Get-RuValueStats `
    $movement4 `
    "ALMENO UNA RU DI MOVIMENTO DA 4 PUNTI"


$summary += Get-RuValueStats `
    $movement3 `
    "ALMENO UNA RU DI MOVIMENTO DA 3 PUNTI"


$summary += Get-RuValueStats `
    $keeper3 `
    "ALMENO UNA RU PORTIERE DA 3 PUNTI"


$summary += Get-RuValueStats `
    $keeper2 `
    "ALMENO UNA RU PORTIERE DA 2 PUNTI"


# ============================================================
# OUTPUT
# ============================================================

$csv  = Join-Path $outputDir "RU_statistiche_storiche.csv"
$txt  = Join-Path $outputDir "RU_statistiche_storiche.txt"
$html = Join-Path $outputDir "RU_statistiche_storiche.html"

$summary |
    Export-Csv `
        -Path $csv `
        -Delimiter ";" `
        -NoTypeInformation `
        -Encoding UTF8

$summary |
    Format-Table -AutoSize |
    Out-String -Width 300 |
    Set-Content `
        -Path $txt `
        -Encoding UTF8


# ============================================================
# HTML
# ============================================================

$ruMatchPct =
    Get-Percent $totalRuMatches $totalMatches 2

$oneEvery = if ($totalRuMatches -gt 0) {
    [math]::Round($totalMatches / $totalRuMatches, 1)
}
else {
    0
}

$decisivePct =
    Get-Percent $allRuDecisive $totalRuTeamMatches 2


$rowsHtml = foreach ($row in $summary) {

    $categoria = [System.Net.WebUtility]::HtmlEncode(
        [string]$row.Categoria
    )

@"
<tr>
    <td class="categoria">$categoria</td>

    <td>$($row.SquadrePartita)</td>
    <td>$($row.OccorrenzeRU)</td>
    <td>$($row.PartiteDistinte)</td>
    <td>$($row.PercentualePartite)%</td>

    <td>$($row.SconfittaPareggio)</td>
    <td>$($row.PctSconfittaPareggio)%</td>

    <td>$($row.PareggioVittoria)</td>
    <td>$($row.PctPareggioVittoria)%</td>

    <td>$($row.SconfittaVittoria)</td>
    <td>$($row.PctSconfittaVittoria)%</td>

    <td class="decisive">$($row.TotaleDecisive)</td>
    <td class="decisive">$($row.PctDecisive)%</td>
</tr>
"@
}


$htmlContent = @"
<!doctype html>

<html lang="it">

<head>

<meta charset="utf-8">

<title>Statistiche storiche Riserve d'Ufficio</title>

<style>

body {
    font-family: "Trebuchet MS", Verdana, Arial, sans-serif;
    margin: 20px;
    background: #ffffff;
    color: #111111;
}

h1 {
    margin: 0 0 4px 0;
    color: #003366;
}

h2 {
    margin-top: 0;
    color: #003366;
}

.subtitle {
    margin-bottom: 20px;
    color: #666666;
}

.box {
    margin-bottom: 20px;
    padding: 15px;
    border: 1px solid #003366;
    background: #f7f7f7;
}

.big {
    font-size: 20px;
    font-weight: bold;
    color: #003366;
}

.highlight {
    font-weight: bold;
    color: #990000;
}

table {
    width: 100%;
    border-collapse: collapse;
    font-size: 13px;
}

th {
    padding: 7px 5px;
    border: 1px solid #003366;
    background: #003366;
    color: white;
    text-align: center;
}

td {
    padding: 6px 5px;
    border: 1px solid #003366;
    text-align: right;
}

tbody tr:nth-child(even) {
    background: #ccffff;
}

td.categoria {
    text-align: left;
    font-weight: bold;
    color: #003366;
}

td.decisive {
    font-weight: bold;
    color: #990000;
}

.spiegazioni {
    margin-top: 22px;
    line-height: 1.5;
}

.spiegazioni h2 {
    margin-bottom: 8px;
}

.spiegazioni p {
    margin: 7px 0;
}

</style>

</head>


<body>

<h1>Statistiche storiche Riserve d'Ufficio</h1>

<div class="subtitle">
RecordsNext 2.0 — report storico personale
</div>


<div class="box">

<h2>Quadro generale</h2>

<p>
Sono state analizzate
<span class="big">$totalMatches</span>
partite storiche.
</p>

<p>
In
<span class="big">$totalRuMatches</span>
partite è comparsa almeno una Riserva d'Ufficio.
</p>

<p>
Questo corrisponde al
<span class="big">$ruMatchPct%</span>
delle partite analizzate:
in media circa
<span class="big">1 partita ogni $oneEvery</span>
ha visto l'utilizzo di almeno una RU.
</p>

<p>
Le singole RU utilizzate complessivamente sono state
<strong>$totalRuOccurrences</strong>.
</p>

<p>
Le squadre che, considerate partita per partita,
hanno utilizzato almeno una RU sono state
<strong>$totalRuTeamMatches</strong>.
</p>

<p>
In
<span class="highlight">$allRuDecisive</span>
di queste situazioni la presenza delle RU
ha cambiato concretamente il risultato della squadra.
L'incidenza degli effetti decisivi è stata del
<span class="highlight">$decisivePct%</span>.
</p>

</div>


<table>

<thead>

<tr>

<th>Situazione</th>

<th>Volte in cui una squadra<br>si è trovata nel caso</th>

<th>Numero totale<br>di RU utilizzate</th>

<th>Partite<br>coinvolte</th>

<th>% di tutte<br>le partite</th>

<th>RU che ha evitato<br>una sconfitta</th>

<th>%</th>

<th>RU che ha trasformato<br>il pari in vittoria</th>

<th>%</th>

<th>RU che ha trasformato<br>la sconfitta in vittoria</th>

<th>%</th>

<th>Casi in cui la RU<br>ha cambiato il risultato</th>

<th>% dei casi<br>con RU</th>

</tr>

</thead>


<tbody>

$($rowsHtml -join "`r`n")

</tbody>

</table>


<div class="spiegazioni">

<h2>Come leggere i dati</h2>

<p>
<strong>Partite coinvolte</strong> conta ogni incontro una sola volta,
anche quando entrambe le squadre hanno utilizzato una Riserva d'Ufficio.
</p>

<p>
<strong>Volte in cui una squadra si è trovata nel caso</strong>
considera invece separatamente le due squadre.
Se nella stessa partita entrambe utilizzano una RU,
la partita conta una volta nelle "Partite coinvolte"
ma due volte in questa colonna.
</p>

<p>
<strong>Numero totale di RU utilizzate</strong>
conta le singole Riserve d'Ufficio.
Una squadra può utilizzare due o più RU nella stessa partita.
</p>

<p>
<strong>% di tutte le partite</strong>
indica quanto frequentemente quella situazione si è verificata
rispetto all'intero archivio storico.
</p>

<p>
<strong>RU che ha evitato una sconfitta</strong>
indica i casi nei quali, senza i punti forniti dalla RU,
la squadra avrebbe perso e invece ha pareggiato.
</p>

<p>
<strong>RU che ha trasformato il pari in vittoria</strong>
indica i casi nei quali, senza RU, la squadra avrebbe pareggiato,
mentre grazie alla RU ha vinto.
</p>

<p>
<strong>RU che ha trasformato la sconfitta in vittoria</strong>
indica i casi più estremi:
senza RU la squadra avrebbe perso,
mentre con la RU ha vinto.
</p>

<p>
<strong>Casi in cui la RU ha cambiato il risultato</strong>
è la somma dei tre effetti precedenti.
</p>

<p>
Per le righe relative a RU da 4, 3 o 2 punti,
una partita viene considerata quando la squadra ha utilizzato
<strong>almeno una RU di quel tipo</strong>.
</p>

<p>
Le competizioni indicate come escluse nella configurazione,
come <strong>Youth League</strong>,
non vengono considerate quando sono identificabili nei dati
o nel nome del file normalizzato.
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
Write-Host "=== REPORT RU STORICO ==="
Write-Host ""

Write-Host "Partite storiche             : $totalMatches"
Write-Host "Partite con almeno una RU    : $totalRuMatches"
Write-Host "Percentuale partite con RU   : $ruMatchPct %"
Write-Host "Una partita con RU ogni      : $oneEvery"
Write-Host "Squadre-partita con RU       : $totalRuTeamMatches"
Write-Host "Occorrenze RU totali         : $totalRuOccurrences"
Write-Host "Casi decisivi complessivi    : $allRuDecisive"
Write-Host "Incidenza effetti decisivi   : $decisivePct %"
Write-Host ""

$summary | Format-Table -AutoSize

Write-Host ""
Write-Host "CSV : $csv"
Write-Host "TXT : $txt"
Write-Host "HTML: $html"
Write-Host ""
