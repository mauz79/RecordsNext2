param(
    [string]$RecordsNextRoot = "E:\FCM\plugin\Mauz_strom2014Full\RecordsNext2",
    [string]$TabellinoCsv = "",
    [string]$DiagnosticaCsv = "",
    [string]$FormazioneCsv = ""
)

$ErrorActionPreference = "Stop"
$CompetizioniEscluse = @("Youth League")

$outputDir = Join-Path $RecordsNextRoot "data\personal-reports"
$reportsRoot = Join-Path $RecordsNextRoot "data\reports"
$ruArchiveRoot = Join-Path $RecordsNextRoot "data\records-archive\riserveufficio"

if ([string]::IsNullOrWhiteSpace($TabellinoCsv)) {
    $TabellinoCsv = Join-Path $outputDir "RU_tabellino_storico_v2.csv"
}
if ([string]::IsNullOrWhiteSpace($DiagnosticaCsv)) {
    $DiagnosticaCsv = Join-Path $outputDir "diagnostica_sotto11_v3\Panchina_sotto11_diagnostica_v3.csv"
}
if ([string]::IsNullOrWhiteSpace($FormazioneCsv)) {
    $FormazioneCsv = Join-Path $outputDir "diagnostica_sotto11_v3\Panchina_sotto11_formazione_v3.csv"
}

foreach ($p in @($TabellinoCsv,$DiagnosticaCsv,$FormazioneCsv,$reportsRoot,$ruArchiveRoot)) {
    if (-not (Test-Path $p)) { throw "Percorso non trovato: $p" }
}

New-Item -ItemType Directory -Path $outputDir -Force | Out-Null

function Get-Prop {
    param($Object,[string]$Name)
    if ($null -eq $Object) { return $null }
    $p = $Object.PSObject.Properties[$Name]
    if ($null -eq $p) { return $null }
    return $p.Value
}

function Num {
    param($Value)
    if ($null -eq $Value) { return 0.0 }
    $t = ([string]$Value).Trim().Replace(",",".")
    $n = 0.0
    if ([double]::TryParse($t,[Globalization.NumberStyles]::Any,[Globalization.CultureInfo]::InvariantCulture,[ref]$n)) {
        return $n
    }
    return 0.0
}

function Int {
    param($Value)
    return [int](Num $Value)
}

function Pct {
    param([double]$N,[double]$D,[int]$Decimals=2)
    if ($D -le 0) { return 0 }
    return [math]::Round(($N/$D)*100,$Decimals)
}

function Html {
    param($Text)
    return [System.Net.WebUtility]::HtmlEncode([string]$Text)
}

function Is-ExcludedCompetition {
    param([string]$Competition)
    foreach ($x in $CompetizioniEscluse) {
        if (([string]$Competition).Trim().Equals($x,[System.StringComparison]::OrdinalIgnoreCase)) { return $true }
    }
    return $false
}

function Is-KeeperRU {
    param($Row)
    $tipo = ([string]$Row.TipoRU).Trim().ToUpperInvariant()
    $ruolo = ([string]$Row.RuoloRU).Trim().ToUpperInvariant()
    return ($tipo -eq "PU" -or $ruolo -eq "P" -or $ruolo -eq "PU" -or $ruolo -eq "PORTIERE")
}

function Normalize-Effect {
    param($Effect)
    $text = ([string]$Effect).Trim().ToUpperInvariant()
    if ($text -match "SCONFITTA.*PAREGGIO") { return "SCONFITTA_PAREGGIO" }
    if ($text -match "PAREGGIO.*VITTORIA") { return "PAREGGIO_VITTORIA" }
    if ($text -match "SCONFITTA.*VITTORIA") { return "SCONFITTA_VITTORIA" }
    return "NESSUN_EFFETTO"
}

function New-Key {
    param($Season,$Match,$Team)
    return "$Season|$Match|$Team"
}

function New-MatchKey {
    param($Season,$Match)
    return "$Season|$Match"
}

# ============================================================
# 1. Universo TABELLINO
# ============================================================

$tabRows = @(
    Import-Csv $TabellinoCsv -Delimiter ";" |
    Where-Object { -not (Is-ExcludedCompetition $_.competizione) } |
    ForEach-Object {
        [pscustomobject]@{
            Stagione = [string]$_.stagione
            Competizione = [string]$_.competizione
            IdIncontro = [string]$_.idIncontro
            IdSquadra = [string]$_.idSquadra
            Squadra = [string]$_.squadra
            PostiVuoti = Int $_.postiVuoti
            NumeroRU = Int $_.numeroRU
            GiocatoriEffettivi = Int $_.giocatoriEffettivi
        }
    }
)

$allMatchKeys = @{}
foreach ($r in $tabRows) { $allMatchKeys[(New-MatchKey $r.Stagione $r.IdIncontro)] = $true }
$totalMatches = $allMatchKeys.Count

# ============================================================
# 2. Sotto 11 senza RU: peso sul totale
# ============================================================

$diag = @(Import-Csv $DiagnosticaCsv -Delimiter ";")

$under11NoRuMatchKeys = @{}
foreach ($r in $diag) {
    $under11NoRuMatchKeys[(New-MatchKey $r.stagione $r.idIncontro)] = $true
}

$under11NoRuMatches = $under11NoRuMatchKeys.Count
$pctUnder11NoRuMatches = Pct $under11NoRuMatches $totalMatches 2

$missingDist = @()
foreach ($missing in @(1,2,3,4,11)) {
    $rows = @($diag | Where-Object { (Int $_.postiVuoti) -eq $missing })
    $mk = @{}
    foreach ($r in $rows) { $mk[(New-MatchKey $r.stagione $r.idIncontro)] = $true }
    $label = switch ($missing) {
        1 { "IN 10 (manca 1 giocatore)" }
        2 { "IN 9 (mancano 2 giocatori)" }
        3 { "IN 8 (mancano 3 giocatori)" }
        4 { "IN 7 (mancano 4 giocatori)" }
        11 { "TABELLINO VUOTO / ANOMALO" }
    }
    $missingDist += [pscustomobject]@{
        Situazione = $label
        SquadrePartita = $rows.Count
        PartiteDistinte = $mk.Count
        PctTotale = Pct $mk.Count $totalMatches 3
    }
}

$twoPlusRows = @($diag | Where-Object { (Int $_.postiVuoti) -ge 2 -and (Int $_.postiVuoti) -lt 11 })
$twoPlusKeys = @{}
foreach ($r in $twoPlusRows) { $twoPlusKeys[(New-MatchKey $r.stagione $r.idIncontro)] = $true }

$threePlusRows = @($diag | Where-Object { (Int $_.postiVuoti) -ge 3 -and (Int $_.postiVuoti) -lt 11 })
$threePlusKeys = @{}
foreach ($r in $threePlusRows) { $threePlusKeys[(New-MatchKey $r.stagione $r.idIncontro)] = $true }

# ============================================================
# 3. Simulazione 5 sostituzioni
#    POS=0 viene usato SOLO nei casi dove produce esattamente 11
#    titolari. Gli altri restano esplicitamente non simulabili.
# ============================================================

$form = @(Import-Csv $FormazioneCsv -Delimiter ";")
$formGroups = @{}
foreach ($r in $form) {
    $key = New-Key $r.stagione $r.idIncontro $r.idSquadra
    if (-not $formGroups.ContainsKey($key)) {
        $formGroups[$key] = New-Object System.Collections.Generic.List[object]
    }
    $formGroups[$key].Add($r)
}

$simulationRows = New-Object System.Collections.Generic.List[object]
$simulabili = 0
$anomali = 0
$benefitTeam = 0
$fullTeam = 0
$benefitMatchKeys = @{}
$fullMatchKeys = @{}

foreach ($d in $diag) {
    $key = New-Key $d.stagione $d.idIncontro $d.idSquadra
    $g = if ($formGroups.ContainsKey($key)) { $formGroups[$key].ToArray() } else { @() }

    $initial = @($g | Where-Object { (Int $_.pos) -eq 0 })
    if ($initial.Count -ne 11) {
        $anomali++
        $simulationRows.Add([pscustomobject]@{
            Stagione=$d.stagione; IdIncontro=$d.idIncontro; IdSquadra=$d.idSquadra; Squadra=$d.squadra
            PostiVuoti=Int $d.postiVuoti; Simulabile=$false; SostituzioniUsate=""; ExtraFinoA5=""
            Compatibili=0; RisoltoCon5=$false; Nota="POS=0 non produce 11 titolari (o FORMAZIONE assente)"
        })
        continue
    }

    $simulabili++

    $initialRole = @{}
    foreach ($r in $initial) {
        $role = ([string]$r.ruoloFca).Trim()
        if ([string]::IsNullOrWhiteSpace($role)) { continue }
        if (-not $initialRole.ContainsKey($role)) { $initialRole[$role] = 0 }
        $initialRole[$role]++
    }

    $finalRole = @{}
    foreach ($r in @($g | Where-Object { (Int $_.nelPrimi11Finale) -eq 1 })) {
        $role = ([string]$r.ruoloFca).Trim()
        if ([string]::IsNullOrWhiteSpace($role)) { continue }
        if (-not $finalRole.ContainsKey($role)) { $finalRole[$role] = 0 }
        $finalRole[$role]++
    }

    $deficit = @{}
    foreach ($role in $initialRole.Keys) {
        $f = if ($finalRole.ContainsKey($role)) { $finalRole[$role] } else { 0 }
        $n = $initialRole[$role] - $f
        if ($n -gt 0) { $deficit[$role] = $n }
    }

    $candidate = @{}
    foreach ($r in @($g | Where-Object {
        (Int $_.inPanchinaTabellino) -eq 1 -and (Int $_.nelPrimi11Finale) -eq 0
    })) {
        $role = ([string]$r.ruoloFca).Trim()
        if ([string]::IsNullOrWhiteSpace($role)) { continue }
        if (-not $candidate.ContainsKey($role)) { $candidate[$role] = 0 }
        $candidate[$role]++
    }

    $compatible = 0
    $allCovered = $true
    foreach ($role in $deficit.Keys) {
        $c = if ($candidate.ContainsKey($role)) { $candidate[$role] } else { 0 }
        $compatible += [math]::Min($deficit[$role],$c)
        if ($c -lt $deficit[$role]) { $allCovered = $false }
    }

    $subsUsed = @($g | Where-Object { (Int $_.entratoRiserva) -ne 0 }).Count
    $extra = [math]::Max(0,5-$subsUsed)
    $missing = Int $d.postiVuoti
    $resolved = ($allCovered -and $missing -le $extra)

    if ($compatible -gt 0) {
        $benefitTeam++
        $benefitMatchKeys[(New-MatchKey $d.stagione $d.idIncontro)] = $true
    }
    if ($resolved) {
        $fullTeam++
        $fullMatchKeys[(New-MatchKey $d.stagione $d.idIncontro)] = $true
    }

    $defText = @($deficit.Keys | Sort-Object | ForEach-Object { "$_=$($deficit[$_])" }) -join ","
    $candText = @($candidate.Keys | Sort-Object | ForEach-Object { "$_=$($candidate[$_])" }) -join ","

    $simulationRows.Add([pscustomobject]@{
        Stagione=$d.stagione
        IdIncontro=$d.idIncontro
        IdSquadra=$d.idSquadra
        Squadra=$d.squadra
        PostiVuoti=$missing
        Simulabile=$true
        SostituzioniUsate=$subsUsed
        ExtraFinoA5=$extra
        RuoliMancanti=$defText
        PanchinaValidaPerRuolo=$candText
        Compatibili=$compatible
        RisoltoCon5=$resolved
        Nota=""
    })
}

$pctBenefitAllMatches = Pct $benefitMatchKeys.Count $totalMatches 2
$pctFullAllMatches = Pct $fullMatchKeys.Count $totalMatches 2
$pctBenefitSim = Pct $benefitTeam $simulabili 2
$pctFullSim = Pct $fullTeam $simulabili 2

# ============================================================
# 4. RU complete + effetto
# ============================================================

$allRu = New-Object System.Collections.Generic.List[object]

$normalizedFiles = @(
    Get-ChildItem $reportsRoot -Recurse -Filter "season_normalized_*.json" |
    Where-Object { -not $_.PSIsContainer }
)

foreach ($file in $normalizedFiles) {
    $season = Split-Path $file.DirectoryName -Leaf
    $doc = Get-Content $file.FullName -Raw -Encoding UTF8 | ConvertFrom-Json

    foreach ($ru in @($doc.riserveUfficioDettaglio)) {
        if ($null -eq $ru) { continue }

        $idMatch = [string](Get-Prop $ru "idIncontro")
        $idTeam = [string](Get-Prop $ru "idSquadra")
        if ([string]::IsNullOrWhiteSpace($idMatch) -or [string]::IsNullOrWhiteSpace($idTeam)) { continue }
        if (-not $allMatchKeys.ContainsKey((New-MatchKey $season $idMatch))) { continue }

        $allRu.Add([pscustomobject]@{
            Stagione=$season
            IdIncontro=$idMatch
            IdSquadra=$idTeam
            Squadra=[string](Get-Prop $ru "squadra")
            TipoRU=[string](Get-Prop $ru "tipoRU")
            RuoloRU=[string](Get-Prop $ru "ruoloRU")
            ValoreRU=Num (Get-Prop $ru "valoreRU")
        })
    }
}

$effects = @{}
Get-ChildItem $ruArchiveRoot -Recurse -Filter "riserveufficio.json" |
Where-Object { -not $_.PSIsContainer } |
ForEach-Object {
    $season = Split-Path $_.DirectoryName -Leaf
    $doc = Get-Content $_.FullName -Raw -Encoding UTF8 | ConvertFrom-Json
    $views = Get-Prop $doc "views"

    foreach ($row in @(Get-Prop $views "ruDecisiva")) {
        if ($null -eq $row) { continue }
        $idMatch = [string](Get-Prop $row "idIncontro")
        $idTeam = [string](Get-Prop $row "idSquadra")
        if ([string]::IsNullOrWhiteSpace($idMatch) -or [string]::IsNullOrWhiteSpace($idTeam)) { continue }
        $effects[(New-Key $season $idMatch $idTeam)] = Normalize-Effect (Get-Prop $row "effetto")
    }
}

function Get-RuStats {
    param($Rows,[hashtable]$UniverseMatches)

    $teamKeys=@{}
    $matchKeys=@{}
    foreach ($r in $Rows) {
        $teamKeys[(New-Key $r.Stagione $r.IdIncontro $r.IdSquadra)]=$true
        $matchKeys[(New-MatchKey $r.Stagione $r.IdIncontro)]=$true
    }

    $sp=0; $pv=0; $sv=0
    $decMatch=@{}
    foreach ($key in $teamKeys.Keys) {
        $fx = if ($effects.ContainsKey($key)) { $effects[$key] } else { "NESSUN_EFFETTO" }
        switch ($fx) {
            "SCONFITTA_PAREGGIO" { $sp++ }
            "PAREGGIO_VITTORIA" { $pv++ }
            "SCONFITTA_VITTORIA" { $sv++ }
        }
        if ($fx -ne "NESSUN_EFFETTO") {
            $parts=$key -split "\|",3
            $decMatch["$($parts[0])|$($parts[1])"]=$true
        }
    }

    $dec=$sp+$pv+$sv
    return [pscustomobject]@{
        TeamCases=$teamKeys.Count
        RuOccurrences=$Rows.Count
        Matches=$matchKeys.Count
        Universe=$UniverseMatches.Count
        PctMatches=Pct $matchKeys.Count $UniverseMatches.Count 2
        DefeatDraw=$sp
        DrawWin=$pv
        DefeatWin=$sv
        DecisiveTeam=$dec
        PctDecisiveTeam=Pct $dec $teamKeys.Count 2
        DecisiveMatches=$decMatch.Count
        PctDecisiveMatchesUniverse=Pct $decMatch.Count $UniverseMatches.Count 2
    }
}

$allHistoryStats = Get-RuStats $allRu $allMatchKeys

# ============================================================
# 5. Identificazione automatica dell'era RU 3 movimento / 2 portiere
#    Regola: prendo il suffisso cronologico piu recente in cui tutte
#    le RU osservate sono compatibili con movimento=3 e portiere=2.
# ============================================================

$seasonPatterns = @()
$seasonsWithRu = @($allRu | Select-Object -ExpandProperty Stagione -Unique | Sort-Object)

foreach ($season in $seasonsWithRu) {
    $rows=@($allRu | Where-Object { $_.Stagione -eq $season })
    $mov=@($rows | Where-Object { -not (Is-KeeperRU $_) } | Select-Object -ExpandProperty ValoreRU -Unique | Sort-Object)
    $keep=@($rows | Where-Object { (Is-KeeperRU $_) } | Select-Object -ExpandProperty ValoreRU -Unique | Sort-Object)

    $movOk = (@($mov | Where-Object { $_ -ne 3 }).Count -eq 0)
    $keepOk = (@($keep | Where-Object { $_ -ne 2 }).Count -eq 0)
    $compatible32 = ($rows.Count -gt 0 -and $movOk -and $keepOk)

    $seasonPatterns += [pscustomobject]@{
        Stagione=$season
        Movimento=if($mov.Count){($mov -join ",")}else{"-"}
        Portiere=if($keep.Count){($keep -join ",")}else{"-"}
        Compatibile32=$compatible32
        RU=$rows.Count
    }
}

$rule32Seasons = New-Object System.Collections.Generic.List[string]
$sortedDesc = @($seasonPatterns | Sort-Object Stagione -Descending)
$started=$false
foreach ($s in $sortedDesc) {
    if (-not $started) {
        if ($s.Compatibile32) {
            $rule32Seasons.Add($s.Stagione)
            $started=$true
        }
        continue
    }
    if ($s.Compatibile32) {
        $rule32Seasons.Add($s.Stagione)
    } else {
        break
    }
}
$rule32Seasons = @($rule32Seasons | Sort-Object)

$rule32SeasonSet=@{}
foreach($s in $rule32Seasons){$rule32SeasonSet[$s]=$true}

$rule32Universe=@{}
foreach($r in $tabRows){
    if($rule32SeasonSet.ContainsKey($r.Stagione)){
        $rule32Universe[(New-MatchKey $r.Stagione $r.IdIncontro)]=$true
    }
}

$rule32Ru=@($allRu | Where-Object { $rule32SeasonSet.ContainsKey($_.Stagione) })
$rule32Stats=Get-RuStats $rule32Ru $rule32Universe

$ruleStart = if($rule32Seasons.Count){$rule32Seasons[0]}else{"NON RILEVATA"}
$ruleEnd = if($rule32Seasons.Count){$rule32Seasons[-1]}else{"NON RILEVATA"}

# ============================================================
# 6. Output CSV
# ============================================================

$simCsv = Join-Path $outputDir "RU_5_sostituzioni_simulazione_v6_1.csv"
$seasonCsv = Join-Path $outputDir "RU_regole_per_stagione_v6_1.csv"
$htmlPath = Join-Path $outputDir "RU_5_sostituzioni_analisi_v6_1.html"

$simulationRows | Export-Csv -Path $simCsv -Delimiter ";" -NoTypeInformation -Encoding UTF8
$seasonPatterns | Export-Csv -Path $seasonCsv -Delimiter ";" -NoTypeInformation -Encoding UTF8

# ============================================================
# 7. HTML
# ============================================================

$missingHtml = foreach($r in $missingDist){
@"
<tr>
<td class="left strong">$(Html $r.Situazione)</td>
<td>$($r.SquadrePartita)</td>
<td>$($r.PartiteDistinte)</td>
<td>$($r.PctTotale)%</td>
</tr>
"@
}

$seasonHtml = foreach($r in $seasonPatterns){
    $class = if($rule32SeasonSet.ContainsKey($r.Stagione)){"rule"}else{""}
@"
<tr class="$class">
<td>$(Html $r.Stagione)</td>
<td>$($r.RU)</td>
<td>$(Html $r.Movimento)</td>
<td>$(Html $r.Portiere)</td>
<td>$(if($r.Compatibile32){"SI"}else{"NO"})</td>
</tr>
"@
}

function RuStatsHtml {
    param([string]$Label,$S)
@"
<tr>
<td class="left strong">$(Html $Label)</td>
<td>$($S.Universe)</td>
<td>$($S.TeamCases)</td>
<td>$($S.RuOccurrences)</td>
<td>$($S.Matches)</td>
<td>$($S.PctMatches)%</td>
<td>$($S.DefeatDraw)</td>
<td>$($S.DrawWin)</td>
<td>$($S.DefeatWin)</td>
<td class="red strong">$($S.DecisiveTeam)</td>
<td class="red strong">$($S.PctDecisiveTeam)%</td>
<td class="red strong">$($S.DecisiveMatches)</td>
<td class="red strong">$($S.PctDecisiveMatchesUniverse)%</td>
</tr>
"@
}

$ruleSeasonText = if($rule32Seasons.Count){$rule32Seasons -join ", "}else{"nessuna"}

$html = @"
<!doctype html>
<html lang="it">
<head>
<meta charset="utf-8">
<title>RU e 5 sostituzioni - analisi storica</title>
<style>
body{font-family:"Trebuchet MS",Verdana,Arial,sans-serif;margin:22px;color:#111;background:#fff}
h1,h2,h3{color:#003366} h1{margin-bottom:4px}
.subtitle{color:#666;margin-bottom:20px}
.box{padding:15px;margin:16px 0 22px;border:1px solid #003366;background:#f7f7f7}
.important{padding:16px;margin:18px 0 24px;border:2px solid #990000;background:#fff8f8}
.good{padding:16px;margin:18px 0 24px;border:2px solid #2b6b2b;background:#f6fff6}
.big{font-size:21px;font-weight:bold;color:#003366}.red{color:#990000}.strong{font-weight:bold}.left{text-align:left}
.small{font-size:12px;color:#555}.note{line-height:1.55}
table{width:100%;border-collapse:collapse;margin-bottom:24px;font-size:13px}
th{padding:7px 5px;border:1px solid #003366;background:#003366;color:#fff;text-align:center}
td{padding:6px 5px;border:1px solid #003366;text-align:right;vertical-align:top}
tbody tr:nth-child(even){background:#ccffff} tr.rule{background:#e7ffe7!important}
</style>
</head>
<body>

<h1>RU e 5 sostituzioni: cosa dice davvero lo storico</h1>
<div class="subtitle">RecordsNext 2.0 - analisi personale sulle $totalMatches partite con TABELLINO reale</div>

<div class="box">
<h2>1. Prima domanda: quanto pesa davvero il problema del giocare sotto 11?</h2>
<p>Le partite con almeno una squadra <strong>sotto 11 e senza RU</strong> sono <span class="big">$under11NoRuMatches</span> su <strong>$totalMatches</strong>: <span class="big">$pctUnder11NoRuMatches%</span>.</p>
<p>Quindi il fenomeno esiste, ma riguarda circa <strong>1 partita ogni $([math]::Round($totalMatches/[math]::Max(1,$under11NoRuMatches),1))</strong>.</p>
<p>Le situazioni più estreme sono molto più rare: almeno due giocatori mancanti compaiono in <strong>$($twoPlusKeys.Count) partite ($((Pct $twoPlusKeys.Count $totalMatches 2))%)</strong>; almeno tre mancanti in <strong>$($threePlusKeys.Count) partite ($((Pct $threePlusKeys.Count $totalMatches 2))%)</strong>.</p>
</div>

<table>
<thead><tr><th>Situazione</th><th>Casi-squadra</th><th>Partite distinte</th><th>% di tutte le partite</th></tr></thead>
<tbody>
$($missingHtml -join "`r`n")
</tbody>
</table>

<div class="note">
<p><strong>Interpretazione:</strong> aumentare i cambi può essere utile, ma va pesato sul totale. Una regola generale non va valutata soltanto sulla percentuale di casi che riesce a correggere <em>dopo che il problema si è verificato</em>: conta anche quanto spesso quel problema nasce davvero.</p>
<p>I 10 casi con 11 slot vuoti sono tabellini anomali/vuoti e non vengono usati nella simulazione delle 5 sostituzioni.</p>
</div>

<h2>2. Cinque sostituzioni: cosa avrebbero cambiato?</h2>
<div class="box">
<p>Dei <strong>$($diag.Count)</strong> casi-squadra sotto 11 senza RU, <strong>$simulabili</strong> sono ricostruibili in modo coerente con 11 titolari iniziali identificati da <code>POS=0</code>; <strong>$anomali</strong> restano fuori dalla simulazione.</p>
<p>Nei casi simulabili, <strong>$benefitTeam ($pctBenefitSim%)</strong> avevano almeno un panchinaro con voto valido del ruolo effettivamente mancante.</p>
<p>Con un massimo di 5 sostituzioni, <strong>$fullTeam ($pctFullSim%)</strong> sarebbero tornati completamente a 11.</p>
</div>

<div class="important">
<h3>Ma sul totale storico?</h3>
<p>Le partite nelle quali almeno una squadra avrebbe potuto beneficiare di un cambio aggiuntivo compatibile sono <span class="big">$($benefitMatchKeys.Count)</span>: <strong>$pctBenefitAllMatches% di tutte le $totalMatches partite</strong>.</p>
<p>Le partite nelle quali la simulazione ricostruisce una squadra completamente riportata a 11 con massimo 5 cambi sono <span class="big">$($fullMatchKeys.Count)</span>: <strong>$pctFullAllMatches% di tutte le partite</strong>.</p>
<p>Questa è la misura più utile per la decisione regolamentare: il vantaggio condizionato è consistente, ma l'effetto complessivo riguarda circa <strong>3 partite ogni 100</strong> dello storico.</p>
</div>

<p class="small">La simulazione considera compatibile un panchinaro con TOT valido appartenente a un ruolo rimasto scoperto rispetto agli 11 iniziali. Il numero di sostituzioni già utilizzate viene letto da FORMATION.ENTRATORISERVA; si concedono cambi aggiuntivi fino a un massimo di 5.</p>

<h2>3. Il "fattore sfiga"</h2>
<div class="box">
<p>Rimanere senza <strong>due o più</strong> giocatori senza RU è successo in <strong>$($twoPlusKeys.Count) partite su $totalMatches</strong>: <strong>$(Pct $twoPlusKeys.Count $totalMatches 2)%</strong>.</p>
<p>Rimanere senza <strong>tre o più</strong> giocatori è successo in appena <strong>$($threePlusKeys.Count) partite</strong>: <strong>$(Pct $threePlusKeys.Count $totalMatches 3)%</strong>.</p>
<p>Rimanere addirittura con <strong>quattro giocatori mancanti</strong> compare in <strong>$((@($missingDist|Where-Object{$_.Situazione -like "IN 7*"}))[0].PartiteDistinte) partite</strong>, cioè <strong>$((@($missingDist|Where-Object{$_.Situazione -like "IN 7*"}))[0].PctTotale)%</strong> dello storico.</p>
<p>Questi eventi sono quindi realmente eccezionali. Se la filosofia della lega vuole conservare una componente di rischio/sfortuna quando una formazione viene travolta da assenze imprevedibili, i numeri mostrano che questa componente non domina affatto il campionato: è rara.</p>
</div>

<h2>4. RU: effetto su tutto lo storico e nella sola era 3/2</h2>
<p>Per la RU il problema è diverso: la RU entra quando non esiste più un sostituto utilizzabile per quel ruolo. Qui interessa soprattutto sapere <strong>quanto spesso modifica il risultato</strong>.</p>

<table>
<thead>
<tr>
<th>Campione</th><th>Partite del campione</th><th>Casi-squadra con RU</th><th>RU usate</th><th>Partite con RU</th><th>% partite con RU</th>
<th>Sconfitta→Pareggio</th><th>Pareggio→Vittoria</th><th>Sconfitta→Vittoria</th>
<th>Casi-squadra decisivi</th><th>% decisivi quando c'è RU</th><th>Partite distinte con RU decisiva</th><th>% di tutte le partite del campione</th>
</tr>
</thead>
<tbody>
$(RuStatsHtml "TUTTO LO STORICO" $allHistoryStats)
$(RuStatsHtml "SOLO REGOLA RU 3 MOVIMENTO / 2 PORTIERE" $rule32Stats)
</tbody>
</table>

<div class="good">
<h3>Campione RU 3/2 rilevato automaticamente</h3>
<p>Il report ha individuato come era compatibile con <strong>RU di movimento = 3</strong> e <strong>RU portiere = 2</strong> il periodo da <strong>$ruleStart</strong> a <strong>$ruleEnd</strong>.</p>
<p>Stagioni incluse: <strong>$(Html $ruleSeasonText)</strong>.</p>
<p>Nel solo campione 3/2, la RU è decisiva in <strong>$($rule32Stats.DecisiveTeam) casi-squadra su $($rule32Stats.TeamCases)</strong>: <strong>$($rule32Stats.PctDecisiveTeam)%</strong> dei casi in cui viene usata.</p>
<p>Guardando invece tutte le partite del periodo, una RU decisiva compare in <strong>$($rule32Stats.DecisiveMatches) partite su $($rule32Stats.Universe)</strong>: <strong>$($rule32Stats.PctDecisiveMatchesUniverse)%</strong>.</p>
<p>Questo secondo numero è quello più corretto per confrontare il peso regolamentare della RU con il peso complessivo delle 5 sostituzioni.</p>
</div>

<h3>Controllo della regola RU per stagione</h3>
<table>
<thead><tr><th>Stagione</th><th>RU osservate</th><th>Valori movimento</th><th>Valori portiere</th><th>Compatibile 3/2</th></tr></thead>
<tbody>
$($seasonHtml -join "`r`n")
</tbody>
</table>

<div class="important">
<h2>5. Lettura regolamentare</h2>
<p><strong>5 sostituzioni:</strong> risolvono una quota importante dei casi in cui il problema è già sorto, ma la simulazione completa riguarda soltanto <strong>$pctFullAllMatches% di tutte le partite storiche</strong>.</p>
<p><strong>RU:</strong> interviene su un fenomeno diverso, legato all'esaurimento dei sostituti utilizzabili per ruolo. Il suo peso va giudicato soprattutto dalla quota di partite nelle quali è <strong>decisiva</strong>, in particolare nel periodo moderno con valore 3/2.</p>
<p>Quindi non è corretto dedurre automaticamente «5 cambi = RU inutile». Le due regole correggono due problemi differenti. I numeri permettono invece di decidere quanta protezione si vuole dare contro l'imprevisto e quanta responsabilità si vuole lasciare alla costruzione della rosa e alla scelta della formazione.</p>
</div>

<div class="small">
File generato da Report_RU_5Sostituzioni_v6_1.ps1.<br>
Simulazione dettagliata: $(Html $simCsv)<br>
Regole RU per stagione: $(Html $seasonCsv)
</div>

</body>
</html>
"@

Set-Content -Path $htmlPath -Value $html -Encoding UTF8

Write-Host ""
Write-Host "=== RU E 5 SOSTITUZIONI - ANALISI v6.1 ==="
Write-Host ""
Write-Host "Partite storiche                           : $totalMatches"
Write-Host "Partite sotto 11 senza RU                  : $under11NoRuMatches ($pctUnder11NoRuMatches%)"
Write-Host "Casi simulabili                            : $simulabili"
Write-Host "Casi anomali/non simulabili                : $anomali"
Write-Host "Partite che beneficerebbero di un cambio   : $($benefitMatchKeys.Count) ($pctBenefitAllMatches%)"
Write-Host "Partite completamente risolte con 5 cambi  : $($fullMatchKeys.Count) ($pctFullAllMatches%)"
Write-Host ""
Write-Host "Era RU 3/2                                 : $ruleStart -> $ruleEnd"
Write-Host "Partite campione 3/2                       : $($rule32Stats.Universe)"
Write-Host "Partite con RU nel campione 3/2            : $($rule32Stats.Matches) ($($rule32Stats.PctMatches)%)"
Write-Host "Casi-squadra RU decisiva nel campione 3/2  : $($rule32Stats.DecisiveTeam) ($($rule32Stats.PctDecisiveTeam)%)"
Write-Host "Partite RU decisiva nel campione 3/2       : $($rule32Stats.DecisiveMatches) ($($rule32Stats.PctDecisiveMatchesUniverse)%)"
Write-Host ""
Write-Host "HTML                                       : $htmlPath"
Write-Host "CSV simulazione                            : $simCsv"
Write-Host "CSV regole RU                              : $seasonCsv"
Write-Host ""
