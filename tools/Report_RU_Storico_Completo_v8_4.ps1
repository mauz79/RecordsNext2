param(
    [string]$RecordsNextRoot = "E:\FCM\plugin\Mauz_strom2014Full\RecordsNext2",
    [string]$TabellinoCsv = "",
    [string]$DiagnosticaCsv = "",
    [string]$FormazioneCsv = ""
)

$ErrorActionPreference = "Stop"

$CompetizioniEscluse = @("Youth League")

$reportsRoot = Join-Path $RecordsNextRoot "data\reports"
$ruArchiveRoot = Join-Path $RecordsNextRoot "data\records-archive\riserveufficio"
$outputDir = Join-Path $RecordsNextRoot "data\personal-reports"

if ([string]::IsNullOrWhiteSpace($TabellinoCsv)) {
    $TabellinoCsv = Join-Path $outputDir "RU_tabellino_storico_v2.csv"
}
if ([string]::IsNullOrWhiteSpace($DiagnosticaCsv)) {
    $DiagnosticaCsv = Join-Path $outputDir "diagnostica_sotto11_v3\Panchina_sotto11_diagnostica_v3.csv"
}
if ([string]::IsNullOrWhiteSpace($FormazioneCsv)) {
    $FormazioneCsv = Join-Path $outputDir "diagnostica_sotto11_v3\Panchina_sotto11_formazione_v3.csv"
}

if (-not (Test-Path $TabellinoCsv)) { throw "CSV TABELLINO non trovato: $TabellinoCsv" }
if (-not (Test-Path $DiagnosticaCsv)) { throw "CSV diagnostica sotto 11 non trovato: $DiagnosticaCsv" }
if (-not (Test-Path $FormazioneCsv)) { throw "CSV formazione sotto 11 non trovato: $FormazioneCsv" }
if (-not (Test-Path $reportsRoot)) { throw "Cartella reports non trovata: $reportsRoot" }
if (-not (Test-Path $ruArchiveRoot)) { throw "Archivio RU non trovato: $ruArchiveRoot" }

New-Item -ItemType Directory -Path $outputDir -Force | Out-Null

function Get-Prop {
    param($Object,[string]$Name)
    if ($null -eq $Object) { return $null }
    $p = $Object.PSObject.Properties[$Name]
    if ($null -eq $p) { return $null }
    return $p.Value
}

function Get-Number {
    param($Value)
    if ($null -eq $Value) { return 0.0 }
    $text = ([string]$Value).Trim().Replace(",", ".")
    if ([string]::IsNullOrWhiteSpace($text)) { return 0.0 }
    $n = 0.0
    if ([double]::TryParse($text,[Globalization.NumberStyles]::Any,[Globalization.CultureInfo]::InvariantCulture,[ref]$n)) {
        return $n
    }
    return 0.0
}

function Get-Percent {
    param([double]$Numerator,[double]$Denominator,[int]$Decimals = 2)
    if ($Denominator -le 0) { return 0 }
    return [math]::Round(($Numerator / $Denominator) * 100,$Decimals)
}

function Html {
    param($Text)
    return [System.Net.WebUtility]::HtmlEncode([string]$Text)
}

function Is-ExcludedCompetition {
    param([string]$Competition)
    if ([string]::IsNullOrWhiteSpace($Competition)) { return $false }
    foreach ($excluded in $CompetizioniEscluse) {
        if ($Competition.Trim().Equals($excluded,[System.StringComparison]::OrdinalIgnoreCase)) {
            return $true
        }
    }
    return $false
}

function Is-ExcludedFakeTeam {
    param([string]$TeamName)
    if ([string]::IsNullOrWhiteSpace($TeamName)) { return $false }
    return ($TeamName.Trim() -match "(?i)\bFANTOCCIO\b")
}

function Normalize-Effect {
    param($Effect)
    $text = ([string]$Effect).Trim().ToUpperInvariant()
    if ($text -match "SCONFITTA.*PAREGGIO") { return "SCONFITTA_PAREGGIO" }
    if ($text -match "PAREGGIO.*VITTORIA") { return "PAREGGIO_VITTORIA" }
    if ($text -match "SCONFITTA.*VITTORIA") { return "SCONFITTA_VITTORIA" }
    return "NESSUN_EFFETTO"
}

function Is-KeeperRU {
    param($Row)
    $tipo = ([string]$Row.TipoRU).Trim().ToUpperInvariant()
    $ruolo = ([string]$Row.RuoloRU).Trim().ToUpperInvariant()
    return ($tipo -eq "PU" -or $ruolo -eq "P" -or $ruolo -eq "PU" -or $ruolo -eq "PORTIERE")
}

function Get-EffectForKey {
    param([string]$Key)
    if ($effects.ContainsKey($Key)) { return $effects[$Key] }
    return "NESSUN_EFFETTO"
}

function Get-OutcomeCounts {
    param([object[]]$Rows)
    return @{
        VITTORIA = @($Rows | Where-Object { $_.Esito -eq "VITTORIA" }).Count
        PAREGGIO = @($Rows | Where-Object { $_.Esito -eq "PAREGGIO" }).Count
        SCONFITTA = @($Rows | Where-Object { $_.Esito -eq "SCONFITTA" }).Count
    }
}


function Get-TeamGoalsFromResult {
    param([string]$Result,[string]$Side)
    if ([string]::IsNullOrWhiteSpace($Result)) { return $null }
    $m = [regex]::Match($Result.Trim(),"^\s*(\d+)\s*-\s*(\d+)\s*$")
    if (-not $m.Success) { return $null }
    $homeGoalsParsed = [int]$m.Groups[1].Value
    $awayGoalsParsed = [int]$m.Groups[2].Value
    $s = ([string]$Side).Trim().ToUpperInvariant()
    if ($s -eq "CASA" -or $s -eq "HOME") { return $homeGoalsParsed }
    if ($s -eq "FUORI" -or $s -eq "AWAY") { return $awayGoalsParsed }
    return $null
}

function Get-EffectCountsFromKeys {
    param([string[]]$Keys)
    $counts = @{
        SCONFITTA_PAREGGIO = 0
        PAREGGIO_VITTORIA = 0
        SCONFITTA_VITTORIA = 0
        NESSUN_EFFETTO = 0
    }
    foreach ($key in $Keys) {
        $effect = Get-EffectForKey $key
        $counts[$effect]++
    }
    return $counts
}

# ------------------------------------------------------------
# 1. TABELLINO: universo del report
# ------------------------------------------------------------

# Esclude l'intera partita se coinvolge "Il Fantoccio", squadra fittizia a 0.
$fakeTeamMatchKeys = @{}
$fakeTeamMatchesBySeason = @{}

Get-ChildItem $reportsRoot -Recurse -Filter "season_normalized_*.json" |
Where-Object { -not $_.PSIsContainer } |
ForEach-Object {
    $season = Split-Path $_.DirectoryName -Leaf
    $doc = Get-Content $_.FullName -Raw -Encoding UTF8 | ConvertFrom-Json

    foreach ($tm in @($doc.partiteSquadra)) {
        if ($null -eq $tm) { continue }
        $competition = [string](Get-Prop $tm "competizioneNome")
        if (Is-ExcludedCompetition $competition) { continue }

        $idMatch = [string](Get-Prop $tm "idIncontro")
        if ([string]::IsNullOrWhiteSpace($idMatch)) { continue }

        $team = [string](Get-Prop $tm "squadra")
        $opponent = [string](Get-Prop $tm "avversaria")

        if ((Is-ExcludedFakeTeam $team) -or (Is-ExcludedFakeTeam $opponent)) {
            $fakeTeamMatchKeys["$season|$idMatch"] = $true
        }
    }
}

foreach ($mk in $fakeTeamMatchKeys.Keys) {
    $season = ($mk -split "\|",2)[0]
    if (-not $fakeTeamMatchesBySeason.ContainsKey($season)) {
        $fakeTeamMatchesBySeason[$season] = 0
    }
    $fakeTeamMatchesBySeason[$season]++
}

$tabRows = @(
    Import-Csv $TabellinoCsv -Delimiter ";" |
    Where-Object { -not (Is-ExcludedCompetition $_.competizione) } |
    ForEach-Object {
        [pscustomobject]@{
            Stagione = [string]$_.stagione
            CompetizioneId = [string]$_.competizioneId
            Competizione = [string]$_.competizione
            IdIncontro = [string]$_.idIncontro
            IdSquadra = [string]$_.idSquadra
            Squadra = [string]$_.squadra
            Lato = [string]$_.lato
            Esito = ([string]$_.esito).ToUpperInvariant()
            ListaPrimi11 = [string]$_.listaPrimi11
            GiocatoriReali = [int]$_.giocatoriReali
            NumeroRU_Tabellino = [int]$_.numeroRU
            PostiVuoti = [int]$_.postiVuoti
            GiocatoriEffettivi = [int]$_.giocatoriEffettivi
        }
    } |
    Where-Object { -not $fakeTeamMatchKeys.ContainsKey("$($_.Stagione)|$($_.IdIncontro)") }
)

$allMatchKeys = @{}
foreach ($r in $tabRows) { $allMatchKeys["$($r.Stagione)|$($r.IdIncontro)"] = $true }
$totalMatches = $allMatchKeys.Count

# ------------------------------------------------------------
# 2. Metadata normalized + dettaglio RU
# ------------------------------------------------------------

$metaByTeamMatch = @{}
$allRu = New-Object System.Collections.Generic.List[object]
$ruCountByTeamMatch = @{}
$normalizedAllMatchKeys = @{}

$normalizedFiles = @(
    Get-ChildItem $reportsRoot -Recurse -Filter "season_normalized_*.json" |
    Where-Object { -not $_.PSIsContainer }
)

foreach ($file in $normalizedFiles) {
    $season = Split-Path $file.DirectoryName -Leaf
    $doc = Get-Content $file.FullName -Raw -Encoding UTF8 | ConvertFrom-Json

    foreach ($tm in @($doc.partiteSquadra)) {
        if ($null -eq $tm) { continue }

        $competition = [string](Get-Prop $tm "competizioneNome")
        if (Is-ExcludedCompetition $competition) { continue }

        $idMatch = [string](Get-Prop $tm "idIncontro")
        $idTeam = [string](Get-Prop $tm "idSquadra")
        if ([string]::IsNullOrWhiteSpace($idMatch) -or [string]::IsNullOrWhiteSpace($idTeam)) { continue }

        if (-not $fakeTeamMatchKeys.ContainsKey("$season|$idMatch")) {
            $normalizedAllMatchKeys["$season|$idMatch"] = $true
        }

        if (-not $allMatchKeys.ContainsKey("$season|$idMatch")) {
            continue
        }

        $key = "$season|$idMatch|$idTeam"
        $metaByTeamMatch[$key] = [pscustomobject]@{
            Giornata = [string](Get-Prop $tm "giornata")
            GiornataDiA = [string](Get-Prop $tm "giornataDiA")
            Avversaria = [string](Get-Prop $tm "avversaria")
            Risultato = [string](Get-Prop $tm "risultato")
            Punteggio = [string](Get-Prop $tm "punteggio")
            UrlTabellinoLocale = [string](Get-Prop $tm "urlTabellinoLocale")
            UrlTabellinoOnline = [string](Get-Prop $tm "urlTabellinoOnline")
        }
    }

    foreach ($ru in @($doc.riserveUfficioDettaglio)) {
        if ($null -eq $ru) { continue }

        $idMatch = [string](Get-Prop $ru "idIncontro")
        $idTeam = [string](Get-Prop $ru "idSquadra")
        if ([string]::IsNullOrWhiteSpace($idMatch) -or [string]::IsNullOrWhiteSpace($idTeam)) { continue }

        if (-not $allMatchKeys.ContainsKey("$season|$idMatch")) {
            continue
        }

        $key = "$season|$idMatch|$idTeam"
        if (-not $ruCountByTeamMatch.ContainsKey($key)) { $ruCountByTeamMatch[$key] = 0 }
        $ruCountByTeamMatch[$key]++

        $allRu.Add([pscustomobject]@{
            Stagione = $season
            IdIncontro = $idMatch
            IdSquadra = $idTeam
            Squadra = [string](Get-Prop $ru "squadra")
            TipoRU = [string](Get-Prop $ru "tipoRU")
            RuoloRU = [string](Get-Prop $ru "ruoloRU")
            ValoreRU = Get-Number (Get-Prop $ru "valoreRU")
        })
    }
}

$normalizedMatches = $normalizedAllMatchKeys.Count
$normalizedNonCanonical = $normalizedMatches - $totalMatches

# ------------------------------------------------------------
# 3. Effetti RU
# ------------------------------------------------------------

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
        $effects["$season|$idMatch|$idTeam"] = Normalize-Effect (Get-Prop $row "effetto")
    }
}

function New-RuCategoryRow {
    param([string]$Categoria,$Rows)

    $teamKeys = @{}
    $matchKeys = @{}

    foreach ($r in $Rows) {
        $teamKeys["$($r.Stagione)|$($r.IdIncontro)|$($r.IdSquadra)"] = $true
        $matchKeys["$($r.Stagione)|$($r.IdIncontro)"] = $true
    }

    $keys = @($teamKeys.Keys)
    $fx = Get-EffectCountsFromKeys $keys
    $decisive = $fx["SCONFITTA_PAREGGIO"] + $fx["PAREGGIO_VITTORIA"] + $fx["SCONFITTA_VITTORIA"]

    $decisiveMatchKeys = @{}
    foreach ($key in $keys) {
        if ((Get-EffectForKey $key) -ne "NESSUN_EFFETTO") {
            $parts = $key -split "\|", 3
            if ($parts.Count -ge 2) {
                $decisiveMatchKeys["$($parts[0])|$($parts[1])"] = $true
            }
        }
    }

    return [pscustomobject]@{
        Categoria = $Categoria
        SquadrePartita = $teamKeys.Count
        OccorrenzeRU = $Rows.Count
        PartiteDistinte = $matchKeys.Count
        PercentualePartite = Get-Percent $matchKeys.Count $totalMatches 2
        SconfittaPareggio = $fx["SCONFITTA_PAREGGIO"]
        PctSconfittaPareggio = Get-Percent $fx["SCONFITTA_PAREGGIO"] $teamKeys.Count 2
        PareggioVittoria = $fx["PAREGGIO_VITTORIA"]
        PctPareggioVittoria = Get-Percent $fx["PAREGGIO_VITTORIA"] $teamKeys.Count 2
        SconfittaVittoria = $fx["SCONFITTA_VITTORIA"]
        PctSconfittaVittoria = Get-Percent $fx["SCONFITTA_VITTORIA"] $teamKeys.Count 2
        TotaleDecisive = $decisive
        PctDecisive = Get-Percent $decisive $teamKeys.Count 2
        PartiteDecisiveDistinte = $decisiveMatchKeys.Count
        PctPartiteDecisiveTotale = Get-Percent $decisiveMatchKeys.Count $totalMatches 2
    }
}

$ruTeamGroups = @($allRu | Group-Object Stagione,IdIncontro,IdSquadra)

$ruExactly2 = @(
    $ruTeamGroups |
    Where-Object { $_.Count -eq 2 } |
    ForEach-Object { $_.Group }
)

$ruMore2 = @(
    $ruTeamGroups |
    Where-Object { $_.Count -gt 2 } |
    ForEach-Object { $_.Group }
)

$movement4 = @($allRu | Where-Object { -not (Is-KeeperRU $_) -and $_.ValoreRU -eq 4 })
$movement3 = @($allRu | Where-Object { -not (Is-KeeperRU $_) -and $_.ValoreRU -eq 3 })
$keeper3 = @($allRu | Where-Object { (Is-KeeperRU $_) -and $_.ValoreRU -eq 3 })
$keeper2 = @($allRu | Where-Object { (Is-KeeperRU $_) -and $_.ValoreRU -eq 2 })


$ruSummary = @(
    New-RuCategoryRow "ALMENO UNA RU" $allRu
    New-RuCategoryRow "ESATTAMENTE 2 RU NELLA STESSA SQUADRA" $ruExactly2
    New-RuCategoryRow "PIÙ DI 2 RU NELLA STESSA SQUADRA" $ruMore2
    New-RuCategoryRow "ALMENO UNA RU DI MOVIMENTO DA 4 PUNTI" $movement4
    New-RuCategoryRow "ALMENO UNA RU DI MOVIMENTO DA 3 PUNTI" $movement3
    New-RuCategoryRow "ALMENO UNA RU PORTIERE DA 3 PUNTI" $keeper3
    New-RuCategoryRow "ALMENO UNA RU PORTIERE DA 2 PUNTI" $keeper2
)

# ------------------------------------------------------------
# 3 bis. Periodo moderno RU: movimento=3, portiere=2
# ------------------------------------------------------------

$ruRulesBySeason = @()
$seasonsWithRu = @($allRu | Select-Object -ExpandProperty Stagione -Unique | Sort-Object)

foreach ($season in $seasonsWithRu) {
    $rows = @($allRu | Where-Object { $_.Stagione -eq $season })
    $movementValues = @(
        $rows |
        Where-Object { -not (Is-KeeperRU $_) } |
        Select-Object -ExpandProperty ValoreRU -Unique |
        Sort-Object
    )
    $keeperValues = @(
        $rows |
        Where-Object { (Is-KeeperRU $_) } |
        Select-Object -ExpandProperty ValoreRU -Unique |
        Sort-Object
    )

    $movementOk = (@($movementValues | Where-Object { $_ -ne 3 }).Count -eq 0)
    $keeperOk = (@($keeperValues | Where-Object { $_ -ne 2 }).Count -eq 0)
    $compatible32 = ($rows.Count -gt 0 -and $movementOk -and $keeperOk)

    $ruRulesBySeason += [pscustomobject]@{
        Stagione = $season
        RU = $rows.Count
        Movimento = if ($movementValues.Count -gt 0) { $movementValues -join "," } else { "-" }
        Portiere = if ($keeperValues.Count -gt 0) { $keeperValues -join "," } else { "-" }
        Compatibile32 = $compatible32
    }
}

# Prende il blocco cronologico più recente e continuo compatibile con 3/2.
$modern32SeasonsList = New-Object System.Collections.Generic.List[string]
$startedModern32 = $false

foreach ($seasonRow in @($ruRulesBySeason | Sort-Object Stagione -Descending)) {
    if (-not $startedModern32) {
        if ($seasonRow.Compatibile32) {
            $modern32SeasonsList.Add($seasonRow.Stagione)
            $startedModern32 = $true
        }
        continue
    }

    if ($seasonRow.Compatibile32) {
        $modern32SeasonsList.Add($seasonRow.Stagione)
    } else {
        break
    }
}

$modern32Seasons = @($modern32SeasonsList.ToArray() | Sort-Object)
$modern32Set = @{}
foreach ($season in $modern32Seasons) { $modern32Set[$season] = $true }

$modern32MatchKeys = @{}
foreach ($r in $tabRows) {
    if ($modern32Set.ContainsKey($r.Stagione)) {
        $modern32MatchKeys["$($r.Stagione)|$($r.IdIncontro)"] = $true
    }
}

$modern32Ru = @($allRu | Where-Object { $modern32Set.ContainsKey($_.Stagione) })

function New-RuPeriodStats {
    param($Rows,[hashtable]$UniverseMatchKeys)

    $teamKeys = @{}
    $matchKeys = @{}
    foreach ($r in $Rows) {
        $teamKeys["$($r.Stagione)|$($r.IdIncontro)|$($r.IdSquadra)"] = $true
        $matchKeys["$($r.Stagione)|$($r.IdIncontro)"] = $true
    }

    $keys = @($teamKeys.Keys)
    $fx = Get-EffectCountsFromKeys $keys
    $decisive = $fx["SCONFITTA_PAREGGIO"] + $fx["PAREGGIO_VITTORIA"] + $fx["SCONFITTA_VITTORIA"]

    $decisiveMatches = @{}
    foreach ($key in $keys) {
        if ((Get-EffectForKey $key) -ne "NESSUN_EFFETTO") {
            $parts = $key -split "\|",3
            if ($parts.Count -ge 2) {
                $decisiveMatches["$($parts[0])|$($parts[1])"] = $true
            }
        }
    }

    return [pscustomobject]@{
        PartiteCampione = $UniverseMatchKeys.Count
        SquadrePartitaRU = $teamKeys.Count
        OccorrenzeRU = $Rows.Count
        PartiteConRU = $matchKeys.Count
        PctPartiteConRU = Get-Percent $matchKeys.Count $UniverseMatchKeys.Count 2
        SconfittaPareggio = $fx["SCONFITTA_PAREGGIO"]
        PareggioVittoria = $fx["PAREGGIO_VITTORIA"]
        SconfittaVittoria = $fx["SCONFITTA_VITTORIA"]
        CasiDecisivi = $decisive
        PctDecisiviSuRU = Get-Percent $decisive $teamKeys.Count 2
        PartiteDecisive = $decisiveMatches.Count
        PctPartiteDecisiveCampione = Get-Percent $decisiveMatches.Count $UniverseMatchKeys.Count 2
    }
}

$modern32Stats = New-RuPeriodStats $modern32Ru $modern32MatchKeys
$modern32Start = if ($modern32Seasons.Count -gt 0) { $modern32Seasons[0] } else { "NON RILEVATO" }
$modern32End = if ($modern32Seasons.Count -gt 0) { $modern32Seasons[-1] } else { "NON RILEVATO" }
$modern32SingleSeason = ($modern32Seasons.Count -eq 1)

# ------------------------------------------------------------
# 4. Arricchisce le righe TABELLINO
# ------------------------------------------------------------

$teamRows = @(
    $tabRows |
    ForEach-Object {
        $r = $_
        $key = "$($r.Stagione)|$($r.IdIncontro)|$($r.IdSquadra)"
        $meta = if ($metaByTeamMatch.ContainsKey($key)) { $metaByTeamMatch[$key] } else { $null }

        [pscustomobject]@{
            Stagione = $r.Stagione
            Competizione = $r.Competizione
            IdIncontro = $r.IdIncontro
            IdSquadra = $r.IdSquadra
            Squadra = $r.Squadra
            Lato = $r.Lato
            Esito = $r.Esito
            GiocatoriReali = $r.GiocatoriReali
            NumeroRU = [int]$r.NumeroRU_Tabellino
            NumeroRU_Normalizzato = if ($ruCountByTeamMatch.ContainsKey($key)) { [int]$ruCountByTeamMatch[$key] } else { 0 }
            PostiVuoti = $r.PostiVuoti
            GiocatoriEffettivi = $r.GiocatoriEffettivi
            Giornata = if ($null -ne $meta) { $meta.Giornata } else { "" }
            GiornataDiA = if ($null -ne $meta) { $meta.GiornataDiA } else { "" }
            Avversaria = if ($null -ne $meta) { $meta.Avversaria } else { "" }
            Risultato = if ($null -ne $meta) { $meta.Risultato } else { "" }
            Punteggio = if ($null -ne $meta) { $meta.Punteggio } else { "" }
            UrlTabellinoLocale = if ($null -ne $meta) { $meta.UrlTabellinoLocale } else { "" }
            UrlTabellinoOnline = if ($null -ne $meta) { $meta.UrlTabellinoOnline } else { "" }
            EffettoRU = Get-EffectForKey $key
        }
    }
)

# ------------------------------------------------------------
# 5. Statistiche sotto 11
# ------------------------------------------------------------

$under11 = @($teamRows | Where-Object { $_.PostiVuoti -gt 0 })
$under11NoRu = @($under11 | Where-Object { $_.NumeroRU -eq 0 })
$under11WithRu = @($under11 | Where-Object { $_.NumeroRU -gt 0 })
$allWithRu = @($teamRows | Where-Object { $_.NumeroRU -gt 0 })
$full11WithRu = @($allWithRu | Where-Object { $_.PostiVuoti -eq 0 })

# Esiti per numero effettivo di giocatori:
# - esattamente 10 = un solo slot mancante
# - meno di 10 = 9 o meno giocatori effettivi
$playedIn10 = @($teamRows | Where-Object { $_.GiocatoriEffettivi -eq 10 })
$playedBelow10 = @($teamRows | Where-Object { $_.GiocatoriEffettivi -lt 10 })

$under11MatchKeys = @{}
foreach ($r in $under11) { $under11MatchKeys["$($r.Stagione)|$($r.IdIncontro)"] = $true }

$under11NoRuMatchKeys = @{}
foreach ($r in $under11NoRu) { $under11NoRuMatchKeys["$($r.Stagione)|$($r.IdIncontro)"] = $true }

$under11WithRuMatchKeys = @{}
foreach ($r in $under11WithRu) { $under11WithRuMatchKeys["$($r.Stagione)|$($r.IdIncontro)"] = $true }

$outcomeAll = Get-OutcomeCounts $under11
$outcomeNoRu = Get-OutcomeCounts $under11NoRu
$outcomeWithRu = Get-OutcomeCounts $under11WithRu
$outcomeFull11WithRu = Get-OutcomeCounts $full11WithRu

$outcomePlayedIn10 = Get-OutcomeCounts $playedIn10
$outcomePlayedBelow10 = Get-OutcomeCounts $playedBelow10

$playedIn10MatchKeys = @{}
foreach ($r in $playedIn10) { $playedIn10MatchKeys["$($r.Stagione)|$($r.IdIncontro)"] = $true }

$playedBelow10MatchKeys = @{}
foreach ($r in $playedBelow10) { $playedBelow10MatchKeys["$($r.Stagione)|$($r.IdIncontro)"] = $true }

$full11WithRuKeys = @(
    $full11WithRu |
    ForEach-Object { "$($_.Stagione)|$($_.IdIncontro)|$($_.IdSquadra)" }
)
$full11Effects = Get-EffectCountsFromKeys $full11WithRuKeys
$full11Decisive =
    $full11Effects["SCONFITTA_PAREGGIO"] +
    $full11Effects["PAREGGIO_VITTORIA"] +
    $full11Effects["SCONFITTA_VITTORIA"]

$under11WithRuKeys = @(
    $under11WithRu |
    ForEach-Object { "$($_.Stagione)|$($_.IdIncontro)|$($_.IdSquadra)" }
)

$under11Effects = Get-EffectCountsFromKeys $under11WithRuKeys
$under11Decisive =
    $under11Effects["SCONFITTA_PAREGGIO"] +
    $under11Effects["PAREGGIO_VITTORIA"] +
    $under11Effects["SCONFITTA_VITTORIA"]

$pctUnder11Total = Get-Percent $under11MatchKeys.Count $totalMatches 2
$pctNoRuAmongUnder11 = Get-Percent $under11NoRu.Count $under11.Count 2
$pctWithRuAmongUnder11 = Get-Percent $under11WithRu.Count $under11.Count 2
$pctRuFailedToReach11 = Get-Percent $under11WithRu.Count $allWithRu.Count 2
$pctRuCompleted11 = Get-Percent $full11WithRu.Count $allWithRu.Count 2
$oneEveryRuFailure = if ($under11WithRu.Count -gt 0) { [math]::Round($allWithRu.Count / $under11WithRu.Count,1) } else { 0 }


# ------------------------------------------------------------
# 5 bis. Peso reale delle 5 sostituzioni
# ------------------------------------------------------------

$diagRows = @(
    Import-Csv $DiagnosticaCsv -Delimiter ";" |
    Where-Object { -not $fakeTeamMatchKeys.ContainsKey("$($_.stagione)|$($_.idIncontro)") }
)
$formRows = @(
    Import-Csv $FormazioneCsv -Delimiter ";" |
    Where-Object { -not $fakeTeamMatchKeys.ContainsKey("$($_.stagione)|$($_.idIncontro)") }
)

$formGroups = @{}
foreach ($r in $formRows) {
    $key = "$($r.stagione)|$($r.idIncontro)|$($r.idSquadra)"
    if (-not $formGroups.ContainsKey($key)) {
        $formGroups[$key] = New-Object System.Collections.Generic.List[object]
    }
    $formGroups[$key].Add($r)
}

$fiveSubRows = New-Object System.Collections.Generic.List[object]
$fiveSubSimulable = 0
$fiveSubNotSimulable = 0
$fiveSubTeamBenefit = 0
$fiveSubTeamResolved = 0
$fiveSubBenefitMatchKeys = @{}
$fiveSubResolvedMatchKeys = @{}

foreach ($d in $diagRows) {
    $key = "$($d.stagione)|$($d.idIncontro)|$($d.idSquadra)"
    $g = if ($formGroups.ContainsKey($key)) { $formGroups[$key].ToArray() } else { @() }

    $initial = @($g | Where-Object { [int]$_.pos -eq 0 })

    if ($initial.Count -ne 11) {
        $fiveSubNotSimulable++
        $fiveSubRows.Add([pscustomobject]@{
            Stagione=$d.stagione; IdIncontro=$d.idIncontro; IdSquadra=$d.idSquadra; Squadra=$d.squadra
            PostiVuoti=[int]$d.postiVuoti; Simulabile=$false; SostituzioniUsate=""; ExtraFinoA5=""
            Compatibili=0; RisoltoCon5=$false; Nota="FORMAZIONE non ricostruibile con 11 titolari POS=0"
        })
        continue
    }

    $fiveSubSimulable++

    $initialRoles = @{}
    foreach ($r in $initial) {
        $role = ([string]$r.ruoloFca).Trim()
        if ([string]::IsNullOrWhiteSpace($role)) { continue }
        if (-not $initialRoles.ContainsKey($role)) { $initialRoles[$role] = 0 }
        $initialRoles[$role]++
    }

    $finalRoles = @{}
    foreach ($r in @($g | Where-Object { [int]$_.nelPrimi11Finale -eq 1 })) {
        $role = ([string]$r.ruoloFca).Trim()
        if ([string]::IsNullOrWhiteSpace($role)) { continue }
        if (-not $finalRoles.ContainsKey($role)) { $finalRoles[$role] = 0 }
        $finalRoles[$role]++
    }

    $deficit = @{}
    foreach ($role in $initialRoles.Keys) {
        $finalCount = if ($finalRoles.ContainsKey($role)) { $finalRoles[$role] } else { 0 }
        $missing = $initialRoles[$role] - $finalCount
        if ($missing -gt 0) { $deficit[$role] = $missing }
    }

    $candidates = @{}
    foreach ($r in @($g | Where-Object {
        [int]$_.inPanchinaTabellino -eq 1 -and [int]$_.nelPrimi11Finale -eq 0
    })) {
        $role = ([string]$r.ruoloFca).Trim()
        if ([string]::IsNullOrWhiteSpace($role)) { continue }
        if (-not $candidates.ContainsKey($role)) { $candidates[$role] = 0 }
        $candidates[$role]++
    }

    $compatible = 0
    $allCovered = $true
    foreach ($role in $deficit.Keys) {
        $available = if ($candidates.ContainsKey($role)) { $candidates[$role] } else { 0 }
        $compatible += [math]::Min($deficit[$role],$available)
        if ($available -lt $deficit[$role]) { $allCovered = $false }
    }

    # ENTRATORISERVA è il flag che, nei 513 casi diagnosticati,
    # restituisce il numero effettivo di sostituzioni: 499 casi a 3,
    # un caso a 2 e 13 casi non ricostruibili.
    $subsUsed = @($g | Where-Object { [int]$_.entratoRiserva -ne 0 }).Count
    $extraAllowed = [math]::Max(0,5-$subsUsed)
    $emptySlots = [int]$d.postiVuoti
    $resolved = ($allCovered -and $emptySlots -le $extraAllowed)

    if ($compatible -gt 0) {
        $fiveSubTeamBenefit++
        $fiveSubBenefitMatchKeys["$($d.stagione)|$($d.idIncontro)"] = $true
    }
    if ($resolved) {
        $fiveSubTeamResolved++
        $fiveSubResolvedMatchKeys["$($d.stagione)|$($d.idIncontro)"] = $true
    }

    $fiveSubRows.Add([pscustomobject]@{
        Stagione=$d.stagione
        IdIncontro=$d.idIncontro
        IdSquadra=$d.idSquadra
        Squadra=$d.squadra
        PostiVuoti=$emptySlots
        Simulabile=$true
        SostituzioniUsate=$subsUsed
        ExtraFinoA5=$extraAllowed
        Compatibili=$compatible
        RisoltoCon5=$resolved
        Nota=""
    })
}

$fiveSubBenefitPctAll = Get-Percent $fiveSubBenefitMatchKeys.Count $totalMatches 2
$fiveSubResolvedPctAll = Get-Percent $fiveSubResolvedMatchKeys.Count $totalMatches 2
$fiveSubBenefitPctCases = Get-Percent $fiveSubTeamBenefit $fiveSubSimulable 2
$fiveSubResolvedPctCases = Get-Percent $fiveSubTeamResolved $fiveSubSimulable 2

# Quanto è raro il "crollo" più grave.
$under11NoRuTwoPlusKeys = @{}
$under11NoRuThreePlusKeys = @{}
$under11NoRuFourPlusKeys = @{}

foreach ($r in $under11NoRu) {
    $mk = "$($r.Stagione)|$($r.IdIncontro)"
    if ($r.PostiVuoti -ge 2) { $under11NoRuTwoPlusKeys[$mk] = $true }
    if ($r.PostiVuoti -ge 3) { $under11NoRuThreePlusKeys[$mk] = $true }
    if ($r.PostiVuoti -ge 4) { $under11NoRuFourPlusKeys[$mk] = $true }
}

$pctTwoPlusAll = Get-Percent $under11NoRuTwoPlusKeys.Count $totalMatches 2
$pctThreePlusAll = Get-Percent $under11NoRuThreePlusKeys.Count $totalMatches 3
$pctFourPlusAll = Get-Percent $under11NoRuFourPlusKeys.Count $totalMatches 3

# Quante prestazioni sotto 11 senza RU producono zero gol.
$under11NoRuZeroGoals = 0
$under11NoRuKnownGoals = 0
foreach ($r in $under11NoRu) {
    $goals = Get-TeamGoalsFromResult $r.Risultato $r.Lato
    if ($null -ne $goals) {
        $under11NoRuKnownGoals++
        if ($goals -eq 0) { $under11NoRuZeroGoals++ }
    }
}
$pctUnder11NoRuZeroGoals = Get-Percent $under11NoRuZeroGoals $under11NoRuKnownGoals 2


# ------------------------------------------------------------
# 5 ter. Andamento RU per stagione
# ------------------------------------------------------------

# Breakpoint richiesto per il confronto:
# 2021/2022 = stagione dalla quale la Serie A reale ha continuato a usare
# le cinque sostituzioni in deroga; la modifica permanente della Regola 3
# FIGC/IFAB è stata poi formalizzata dall'estate 2022.
$fiveSubsBreakpoint = "2021_2022"

$seasonRuStats = @()

$allSeasons = @(
    $tabRows |
    Select-Object -ExpandProperty Stagione -Unique |
    Sort-Object
)

foreach ($season in $allSeasons) {
    $seasonTab = @($tabRows | Where-Object { $_.Stagione -eq $season })

    $seasonMatchKeys = @{}
    foreach ($r in $seasonTab) {
        $seasonMatchKeys["$($r.Stagione)|$($r.IdIncontro)"] = $true
    }

    $seasonRu = @($allRu | Where-Object { $_.Stagione -eq $season })

    $teamGroupsSeason = @{}
    foreach ($r in $seasonRu) {
        $key = "$($r.Stagione)|$($r.IdIncontro)|$($r.IdSquadra)"
        if (-not $teamGroupsSeason.ContainsKey($key)) {
            $teamGroupsSeason[$key] = New-Object System.Collections.Generic.List[object]
        }
        $teamGroupsSeason[$key].Add($r)
    }

    $ruTeamKeys = @{}
    $ruMatchKeysSeason = @{}
    $oneRuTeam = 0
    $twoRuTeam = 0
    $moreThanTwoRuTeam = 0
    $maxRuObserved = 0

    foreach ($key in $teamGroupsSeason.Keys) {
        $rows = $teamGroupsSeason[$key].ToArray()
        $count = $rows.Count

        $ruTeamKeys[$key] = $true

        $parts = $key -split "\|",3
        if ($parts.Count -ge 2) {
            $ruMatchKeysSeason["$($parts[0])|$($parts[1])"] = $true
        }

        if ($count -eq 1) { $oneRuTeam++ }
        elseif ($count -eq 2) { $twoRuTeam++ }
        elseif ($count -gt 2) { $moreThanTwoRuTeam++ }

        if ($count -gt $maxRuObserved) { $maxRuObserved = $count }
    }

    $fx = Get-EffectCountsFromKeys @($ruTeamKeys.Keys)
    $decisiveTeam = $fx["SCONFITTA_PAREGGIO"] + $fx["PAREGGIO_VITTORIA"] + $fx["SCONFITTA_VITTORIA"]

    $decisiveMatchKeysSeason = @{}
    foreach ($key in $ruTeamKeys.Keys) {
        if ((Get-EffectForKey $key) -ne "NESSUN_EFFETTO") {
            $parts = $key -split "\|",3
            if ($parts.Count -ge 2) {
                $decisiveMatchKeysSeason["$($parts[0])|$($parts[1])"] = $true
            }
        }
    }

    $seasonRuStats += [pscustomobject]@{
        Stagione = $season
        Periodo5Sost = if ($season -ge $fiveSubsBreakpoint) { "DAL 2021/2022" } else { "PRIMA DEL 2021/2022" }
        Partite = $seasonMatchKeys.Count
        PartiteConRU = $ruMatchKeysSeason.Count
        PctPartiteConRU = Get-Percent $ruMatchKeysSeason.Count $seasonMatchKeys.Count 2
        CasiSquadraConRU = $ruTeamKeys.Count
        OccorrenzeRU = $seasonRu.Count
        MediaRUPerCaso = if ($ruTeamKeys.Count -gt 0) { [math]::Round($seasonRu.Count / $ruTeamKeys.Count,3) } else { 0 }
        Casi1RU = $oneRuTeam
        Casi2RU = $twoRuTeam
        CasiPiu2RU = $moreThanTwoRuTeam
        MaxRUOsservate = $maxRuObserved
        CasiDecisivi = $decisiveTeam
        PctDecisiviSuCasiRU = Get-Percent $decisiveTeam $ruTeamKeys.Count 2
        PartiteDecisive = $decisiveMatchKeysSeason.Count
        PctPartiteDecisive = Get-Percent $decisiveMatchKeysSeason.Count $seasonMatchKeys.Count 2
    }
}

function New-RuEraSummary {
    param([string]$Label,$Rows)

    $matches = 0
    $matchesRu = 0
    $teamCases = 0
    $occ = 0
    $decMatches = 0
    $decTeam = 0

    foreach ($r in $Rows) {
        $matches += [int]$r.Partite
        $matchesRu += [int]$r.PartiteConRU
        $teamCases += [int]$r.CasiSquadraConRU
        $occ += [int]$r.OccorrenzeRU
        $decMatches += [int]$r.PartiteDecisive
        $decTeam += [int]$r.CasiDecisivi
    }

    return [pscustomobject]@{
        Periodo = $Label
        Stagioni = $Rows.Count
        Partite = $matches
        PartiteConRU = $matchesRu
        PctPartiteConRU = Get-Percent $matchesRu $matches 2
        CasiSquadraConRU = $teamCases
        OccorrenzeRU = $occ
        PctDecisiviSuCasiRU = Get-Percent $decTeam $teamCases 2
        PartiteDecisive = $decMatches
        PctPartiteDecisive = Get-Percent $decMatches $matches 2
    }
}

$preFiveSubs = @($seasonRuStats | Where-Object { $_.Stagione -lt $fiveSubsBreakpoint })
$postFiveSubs = @($seasonRuStats | Where-Object { $_.Stagione -ge $fiveSubsBreakpoint })

$ruEraSummary = @(
    New-RuEraSummary "PRIMA DEL 2021/2022" $preFiveSubs
    New-RuEraSummary "DAL 2021/2022" $postFiveSubs
)

# ------------------------------------------------------------
# 6. CSV output
# ------------------------------------------------------------

$ruSummaryCsv = Join-Path $outputDir "RU_statistiche_storiche_complete_v8_4.csv"
$under11Csv = Join-Path $outputDir "RU_casi_sotto_11_v8_4.csv"
$htmlPath = Join-Path $outputDir "RU_statistiche_storiche_complete_v8_4.html"

$fiveSubCsv = Join-Path $outputDir "RU_5_sostituzioni_simulazione_v8_4.csv"
$ruRulesCsv = Join-Path $outputDir "RU_regole_per_stagione_v8_4.csv"
$ruSeasonCsv = Join-Path $outputDir "RU_andamento_per_stagione_v8_4.csv"
$ruEraCsv = Join-Path $outputDir "RU_confronto_pre_post_2021_2022_v8_4.csv"

$ruSummary | Export-Csv -Path $ruSummaryCsv -Delimiter ";" -NoTypeInformation -Encoding UTF8
$under11 | Export-Csv -Path $under11Csv -Delimiter ";" -NoTypeInformation -Encoding UTF8
$fiveSubRows | Export-Csv -Path $fiveSubCsv -Delimiter ";" -NoTypeInformation -Encoding UTF8
$ruRulesBySeason | Export-Csv -Path $ruRulesCsv -Delimiter ";" -NoTypeInformation -Encoding UTF8
$seasonRuStats | Export-Csv -Path $ruSeasonCsv -Delimiter ";" -NoTypeInformation -Encoding UTF8
$ruEraSummary | Export-Csv -Path $ruEraCsv -Delimiter ";" -NoTypeInformation -Encoding UTF8

# ------------------------------------------------------------
# 7. HTML
# ------------------------------------------------------------

$ruRowsHtml = foreach ($r in $ruSummary) {
@"
<tr>
<td class="left strong">$(Html $r.Categoria)</td>
<td>$($r.SquadrePartita)</td>
<td>$($r.OccorrenzeRU)</td>
<td>$($r.PartiteDistinte)</td>
<td>$($r.PercentualePartite)%</td>
<td>$($r.SconfittaPareggio)</td>
<td>$($r.PctSconfittaPareggio)%</td>
<td>$($r.PareggioVittoria)</td>
<td>$($r.PctPareggioVittoria)%</td>
<td>$($r.SconfittaVittoria)</td>
<td>$($r.PctSconfittaVittoria)%</td>
<td class="red strong">$($r.TotaleDecisive)</td>
<td class="red strong">$($r.PctDecisive)%</td>
<td class="red strong">$($r.PartiteDecisiveDistinte)</td>
<td class="red strong">$($r.PctPartiteDecisiveTotale)%</td>
</tr>
"@
}

function Under11RowHtml {
    param(
        [string]$Label,
        [object[]]$Rows,
        [hashtable]$MatchKeys,
        [hashtable]$Outcomes,
        [string]$ExtraPct
    )

    $pctTotal = Get-Percent $MatchKeys.Count $totalMatches 2

    return @"
<tr>
<td class="left strong">$(Html $Label)</td>
<td>$($Rows.Count)</td>
<td>$($MatchKeys.Count)</td>
<td>$pctTotal%</td>
<td>$($Outcomes["VITTORIA"])</td>
<td>$(Get-Percent $Outcomes["VITTORIA"] $Rows.Count 2)%</td>
<td>$($Outcomes["PAREGGIO"])</td>
<td>$(Get-Percent $Outcomes["PAREGGIO"] $Rows.Count 2)%</td>
<td>$($Outcomes["SCONFITTA"])</td>
<td>$(Get-Percent $Outcomes["SCONFITTA"] $Rows.Count 2)%</td>
<td>$ExtraPct</td>
</tr>
"@
}

$under11RowsHtml = @(
    Under11RowHtml "IN 10 O MENO - TOTALE" $under11 $under11MatchKeys $outcomeAll "100%"
    Under11RowHtml "IN 10 O MENO SENZA ALCUNA RU" $under11NoRu $under11NoRuMatchKeys $outcomeNoRu "$pctNoRuAmongUnder11% dei casi sotto 11"
    Under11RowHtml "IN 10 O MENO CON ALMENO UNA RU" $under11WithRu $under11WithRuMatchKeys $outcomeWithRu "$pctWithRuAmongUnder11% dei casi sotto 11"
)

$casesHtml = foreach ($r in @($under11 | Sort-Object Stagione,Competizione,IdIncontro,Squadra)) {

    $effectText = switch ($r.EffettoRU) {
        "SCONFITTA_PAREGGIO" { "La RU ha evitato la sconfitta" }
        "PAREGGIO_VITTORIA" { "La RU ha trasformato il pareggio in vittoria" }
        "SCONFITTA_VITTORIA" { "La RU ha trasformato la sconfitta in vittoria" }
        default { "Nessun effetto decisivo registrato" }
    }

    $url = ""
    if (-not [string]::IsNullOrWhiteSpace($r.UrlTabellinoLocale)) { $url = $r.UrlTabellinoLocale }
    elseif (-not [string]::IsNullOrWhiteSpace($r.UrlTabellinoOnline)) { $url = $r.UrlTabellinoOnline }

    $link = if (-not [string]::IsNullOrWhiteSpace($url)) {
        '<a href="' + (Html $url) + '">Apri tabellino</a>'
    } else { "" }

@"
<tr>
<td>$(Html $r.Stagione)</td>
<td>$(Html $r.Competizione)</td>
<td>$(Html $r.Giornata)</td>
<td class="left strong">$(Html $r.Squadra)</td>
<td class="left">$(Html $r.Avversaria)</td>
<td>$($r.GiocatoriEffettivi)</td>
<td>$($r.NumeroRU)</td>
<td>$(Html $r.Esito)</td>
<td>$(Html $r.Risultato)</td>
<td>$(Html $r.Punteggio)</td>
<td class="left">$(Html $effectText)</td>
<td>$link</td>
</tr>
"@
}

$allRuRow = $ruSummary | Select-Object -First 1
$ruMatchPct = $allRuRow.PercentualePartite
$oneEveryRuMatch = if ($allRuRow.PartiteDistinte -gt 0) { [math]::Round($totalMatches / $allRuRow.PartiteDistinte,1) } else { 0 }

$modern32Warning = if ($modern32SingleSeason) {
    "ATTENZIONE: il regime RU 3/2 risulta presente in una sola stagione ($modern32Start). È un campione corto e va interpretato con cautela."
} elseif ($modern32Seasons.Count -gt 1) {
    "Il regime RU 3/2 è stato rilevato nel blocco $modern32Start - $modern32End."
} else {
    "Il regime RU 3/2 non è stato identificato automaticamente."
}


$fakeTeamSeasonRowsHtml = foreach ($season in @($fakeTeamMatchesBySeason.Keys | Sort-Object)) {
@"
<tr><td>$(Html $season)</td><td>$($fakeTeamMatchesBySeason[$season])</td></tr>
"@
}
$fakeTeamMatchesExcluded = $fakeTeamMatchKeys.Count

$ruSeasonRowsHtml = foreach ($r in $seasonRuStats) {
    $class = if ($r.Stagione -ge $fiveSubsBreakpoint) { ' class="fiveSubsEra"' } else { "" }
@"
<tr$class>
<td>$(Html $r.Stagione)</td>
<td>$($r.Partite)</td>
<td>$($r.PartiteConRU)</td>
<td><strong>$($r.PctPartiteConRU)%</strong></td>
<td>$($r.CasiSquadraConRU)</td>
<td>$($r.OccorrenzeRU)</td>
<td>$($r.MediaRUPerCaso)</td>
<td>$($r.Casi1RU)</td>
<td>$($r.Casi2RU)</td>
<td>$($r.CasiPiu2RU)</td>
<td>$($r.MaxRUOsservate)</td>
<td>$($r.CasiDecisivi)</td>
<td>$($r.PctDecisiviSuCasiRU)%</td>
<td>$($r.PartiteDecisive)</td>
<td>$($r.PctPartiteDecisive)%</td>
</tr>
"@
}

$ruEraRowsHtml = foreach ($r in $ruEraSummary) {
@"
<tr>
<td class="left strong">$(Html $r.Periodo)</td>
<td>$($r.Stagioni)</td>
<td>$($r.Partite)</td>
<td>$($r.PartiteConRU)</td>
<td><strong>$($r.PctPartiteConRU)%</strong></td>
<td>$($r.CasiSquadraConRU)</td>
<td>$($r.OccorrenzeRU)</td>
<td>$($r.PctDecisiviSuCasiRU)%</td>
<td>$($r.PartiteDecisive)</td>
<td><strong>$($r.PctPartiteDecisive)%</strong></td>
</tr>
"@
}

$ruRulesRowsHtml = foreach ($r in $ruRulesBySeason) {
    $class = if ($modern32Set.ContainsKey($r.Stagione)) { ' class="modern"' } else { "" }
@"
<tr$class><td>$(Html $r.Stagione)</td><td>$($r.RU)</td><td>$(Html $r.Movimento)</td><td>$(Html $r.Portiere)</td><td>$(if($r.Compatibile32){"SI"}else{"NO"})</td></tr>
"@
}

$htmlContent = @"
<!doctype html>
<html lang="it">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover">
<title>Statistiche storiche Riserve d'Ufficio</title>
<style>
body { font-family:"Trebuchet MS",Verdana,Arial,sans-serif; margin:22px; color:#111; background:#fff; }
h1,h2,h3 { color:#003366; }
h1 { margin-bottom:4px; }
.subtitle { color:#666; margin-bottom:20px; }
.box { padding:15px; margin:16px 0 22px 0; border:1px solid #003366; background:#f7f7f7; }
.box-important { padding:16px; margin:18px 0 24px 0; border:2px solid #990000; background:#fff8f8; }
.big { color:#003366; font-size:20px; font-weight:bold; }
.red { color:#990000; }
.strong { font-weight:bold; }
.left { text-align:left; }
.small { font-size:12px; }
table { width:100%; border-collapse:collapse; margin-bottom:24px; font-size:13px; }
th { padding:7px 5px; border:1px solid #003366; background:#003366; color:#fff; text-align:center; }
td { padding:6px 5px; border:1px solid #003366; text-align:right; vertical-align:top; }
tbody tr:nth-child(even) { background:#ccffff; }
tbody tr.modern { background:#e9ffe9; }
tbody tr.fiveSubsEra { background:#fff4cc; }
.conclusion { padding:18px; margin:22px 0; border:3px solid #003366; background:#f4f8ff; line-height:1.6; }
.policy { padding:16px; margin:18px 0; border:2px solid #666; background:#fafafa; line-height:1.55; }
.explain { line-height:1.55; }
.explain p { margin:8px 0; }
.code { font-family:Consolas,monospace; background:#eee; padding:1px 4px; }

.mobileHint { display:none; }
@media (max-width: 760px) {
  body { margin:10px; font-size:16px; line-height:1.5; }
  h1 { font-size:25px; line-height:1.15; }
  h2 { font-size:21px; line-height:1.2; margin-top:22px; }
  h3 { font-size:18px; }
  .subtitle { font-size:14px; }
  .box,.policy,.conclusion { padding:12px; margin:14px 0; }
  .big { font-size:20px; }
  .mobileHint { display:block; font-size:12px; color:#555; margin:4px 0 8px; }
  table { display:block; width:100%; overflow-x:auto; -webkit-overflow-scrolling:touch; white-space:nowrap; font-size:13px; }
  th,td { padding:8px 7px; }
  p { margin:10px 0; }
  code { white-space:normal; word-break:break-word; }
}

</style>
</head>
<body>

<h1>Statistiche storiche Riserve d'Ufficio</h1>
<div class="subtitle">RecordsNext 2.0 - report storico personale completo</div>
<div class="mobileHint">Versione mobile: le tabelle scorrono lateralmente. Anche i numeri, a differenza di certe opinioni, si adattano allo schermo.</div>
<div class="policy">
<h2>0. Pulizia del campione: esclusione de "Il Fantoccio"</h2>
<p>Tutte le statistiche escludono integralmente le partite che coinvolgono <strong>Il Fantoccio</strong>, squadra fittizia che totalizzava 0 di default.</p>
<p>Viene esclusa <strong>l'intera partita</strong>, quindi anche la prestazione dell'avversario: altrimenti lo 0 artificiale altererebbe esiti, gol, frequenze RU e denominatori.</p>
<p>Partite escluse: <strong>$fakeTeamMatchesExcluded</strong>.</p>
<table>
<thead><tr><th>Stagione</th><th>Partite escluse con Il Fantoccio</th></tr></thead>
<tbody>
$($fakeTeamSeasonRowsHtml -join "`r`n")
</tbody>
</table>
</div>

<div class="box">
<h2>1. Quadro generale</h2>
<p>Sono state analizzate <span class="big">$totalMatches</span> partite storiche, dopo l'esclusione di <strong>$($CompetizioniEscluse -join ", ")</strong>.</p>
<p>In <span class="big">$($allRuRow.PartiteDistinte)</span> partite è comparsa almeno una Riserva d'Ufficio: <strong>$ruMatchPct%</strong> del totale, cioè circa <strong>1 partita ogni $oneEveryRuMatch</strong>.</p>
<p>Le prestazioni di squadra con almeno una RU sono state <strong>$($allRuRow.SquadrePartita)</strong>; le singole RU rilevate nei dati normalizzati sono state <strong>$($allRuRow.OccorrenzeRU)</strong>.</p>

<p><strong>Perché il totale storico è $totalMatches e non $normalizedMatches?</strong>
I file normalizzati contengono <strong>$normalizedNonCanonical</strong> riferimenti-partita in più che non hanno un corrispondente TABELLINO FCM.
Queste eccedenze sono concentrate nelle coppe e sono compatibili con turni di riposo/byes o altre righe di calendario senza una partita effettivamente disputata.
Per evitare di contare come partita qualcosa che non ha un tabellino reale, questo report usa come universo statistico solo le <strong>$totalMatches partite con TABELLINO</strong>.</p>
</div>

<h2>2. Frequenza ed effetto delle Riserve d'Ufficio</h2>
<div class="explain">
<p>La tabella distingue <strong>quanto spesso compare una RU</strong> da <strong>quanto spesso cambia il risultato</strong>.</p>
<p>Una <strong>squadra-partita</strong> è una singola prestazione di una squadra. Se nella stessa partita entrambe le squadre usano RU, la partita conta una volta ma le squadre-partita sono due.</p>
<p>Le percentuali degli effetti sono calcolate sulle squadre-partita della relativa categoria.</p>
</div>

<table>
<thead>
<tr>
<th>Situazione</th><th>Volte accaduto a una squadra</th><th>RU utilizzate</th><th>Partite coinvolte</th><th>% di tutte le partite</th>
<th>Ha evitato una sconfitta</th><th>% sui casi</th><th>Ha trasformato il pari in vittoria</th><th>% sui casi</th>
<th>Ha trasformato la sconfitta in vittoria</th><th>% sui casi</th><th>Casi-squadra decisivi</th><th>% decisivi sui casi con RU</th><th>Partite distinte con RU decisiva</th><th>% di tutte le partite</th>
</tr>
</thead>
<tbody>
$($ruRowsHtml -join "`r`n")
</tbody>
</table>

<h2>3. Quando una squadra non riesce ad arrivare a 11</h2>
<div class="explain">
<p>Per questa parte non viene usato <span class="code">NGioc</span>. Il dato viene letto direttamente dal <strong>tabellino finale FCM</strong>.</p>
<p>Nei primi 11 posti di <span class="code">TABELLINO.LISTA</span>: un numero positivo è un giocatore reale, <span class="code">-1</span> è una RU e <span class="code">0</span> è un posto rimasto scoperto.</p>
<p>Una squadra è quindi <strong>in 10 o meno</strong> quando esiste almeno uno slot vuoto nei primi 11 posti. Un solo zero = 10 giocatori effettivi; due zeri = 9; e così via.</p>
<p>Il criterio è stato verificato sul caso reale <strong>F.C. Boliverz - SegaTori FC, Serie C 2025/2026</strong>, nel quale il tabellino di SegaTori mostra 10 giocatori validi e un undicesimo slot a zero.</p>
</div>

<table>
<thead>
<tr><th>Situazione</th><th>Volte accaduto a una squadra</th><th>Partite coinvolte</th><th>% di tutte le partite</th><th>Vittorie</th><th>% vittorie</th><th>Pareggi</th><th>% pareggi</th><th>Sconfitte</th><th>% sconfitte</th><th>Peso tra i casi sotto 11</th></tr>
</thead>
<tbody>
$($under11RowsHtml -join "`r`n")
</tbody>
</table>

<div class="box-important">
<h2>4. Quando nemmeno la RU basta</h2>
<p>In tutto lo storico risultano <span class="big">$($allWithRu.Count)</span> prestazioni di squadra nelle quali almeno una RU è entrata effettivamente negli 11 posti del tabellino.</p>
<p>In <span class="big red">$($under11WithRu.Count)</span> di queste occasioni, nonostante una o più RU, la squadra è rimasta comunque con meno di 11 giocatori.</p>
<p>Questo rappresenta il <span class="big red">$pctRuFailedToReach11%</span> dei casi con almeno una RU effettivamente in campo.</p>
<p>In altre parole: <strong>circa 1 volta ogni $oneEveryRuFailure utilizzi di RU</strong> la squadra è rimasta comunque sotto gli 11.</p>
</div>

<div class="box">
<h2>4 bis. Quanto pesa davvero il problema che le 5 sostituzioni cercano di correggere?</h2>
<p>Le partite in cui almeno una squadra è rimasta sotto 11 <strong>senza RU</strong> sono <strong>$($under11NoRuMatchKeys.Count) su $totalMatches</strong>: <strong>$(Get-Percent $under11NoRuMatchKeys.Count $totalMatches 2)%</strong>.</p>
<p>La diagnostica sulle formazioni rende simulabili <strong>$fiveSubSimulable casi-squadra</strong>; <strong>$fiveSubNotSimulable</strong> restano fuori perché la formazione iniziale non è ricostruibile in modo coerente.</p>
<p>Portando il tetto a 5 sostituzioni, almeno una squadra avrebbe avuto un cambio aggiuntivo realmente compatibile in <strong>$($fiveSubBenefitMatchKeys.Count) partite</strong>, cioè nel <strong>$fiveSubBenefitPctAll%</strong> di tutto lo storico.</p>
<p>Le partite nelle quali la simulazione ricostruisce una squadra <strong>completamente riportata a 11</strong> sono <strong>$($fiveSubResolvedMatchKeys.Count)</strong>: appena <strong>$fiveSubResolvedPctAll%</strong> delle $totalMatches partite.</p>
<p>Quindi le 5 sostituzioni sono efficaci quando il problema si presenta, ma il loro impatto complessivo sul campionato è basso: correggerebbero completamente circa <strong>3 partite ogni 100</strong> dello storico.</p>
</div>

<div class="policy">
<h2>4 ter. Il fattore sfortuna è raro</h2>
<p>Rimanere senza almeno <strong>due giocatori</strong> e senza RU è successo in <strong>$($under11NoRuTwoPlusKeys.Count) partite</strong>: <strong>$pctTwoPlusAll%</strong> del totale.</p>
<p>Rimanere senza almeno <strong>tre giocatori</strong> è successo in <strong>$($under11NoRuThreePlusKeys.Count) partite</strong>: <strong>$pctThreePlusAll%</strong>.</p>
<p>Rimanere senza almeno <strong>quattro giocatori</strong> è successo in appena <strong>$($under11NoRuFourPlusKeys.Count) partite</strong>: <strong>$pctFourPlusAll%</strong>.</p>
<p>Se la filosofia regolamentare vuole lasciare una piccola quota di partite esposta alla pura sfortuna - la "tegola in testa" dopo una formazione ragionevole - i numeri mostrano che questa aleatorietà è circoscritta e non domina il campionato.</p>
<p>Tra le prestazioni sotto 11 senza RU per cui il risultato è leggibile, <strong>$under11NoRuZeroGoals su $under11NoRuKnownGoals</strong> hanno prodotto zero gol: <strong>$pctUnder11NoRuZeroGoals%</strong>.</p>
</div>

<div class="box">
<h2>4 quater. Uso delle RU anno per anno</h2>
<p>Per capire se la maggiore disponibilità di cambi nel calcio reale abbia ridotto la necessità delle RU nel fantacalcio, il report mostra l'incidenza delle RU <strong>stagione per stagione</strong>. Il confronto evidenzia dal <strong>2021/2022</strong> il periodo richiesto per l'analisi delle cinque sostituzioni.</p>

<table>
<thead>
<tr>
<th>Stagione</th><th>Partite</th><th>Partite con RU</th><th>% partite con RU</th>
<th>Casi-squadra RU</th><th>RU totali</th><th>RU/caso</th>
<th>1 RU</th><th>2 RU</th><th>&gt;2 RU</th><th>Max RU</th>
<th>Casi decisivi</th><th>% decisivi sui casi RU</th><th>Partite decisive</th><th>% decisive su tutte</th>
</tr>
</thead>
<tbody>
$($ruSeasonRowsHtml -join "`r`n")
</tbody>
</table>

<p><strong>Come leggere 1, 2 e &gt;2 RU:</strong> la lega ha nel tempo ridotto il numero ordinario di RU da 2 a 1 per partita. I casi con più di 2 RU non rappresentano il normale limite regolamentare: sono casi eccezionali legati ai recuperi di partite rinviate, possibilità prevista storicamente dal regolamento. Per questo vengono mostrati separatamente e non vanno interpretati come normale dipendenza della lega da 3 o più RU.</p>

<h3>Confronto aggregato prima/dopo 2021/2022</h3>
<table>
<thead>
<tr><th>Periodo</th><th>Stagioni</th><th>Partite</th><th>Partite con RU</th><th>% partite con RU</th><th>Casi-squadra RU</th><th>RU totali</th><th>% decisivi sui casi RU</th><th>Partite decisive</th><th>% decisive su tutte</th></tr>
</thead>
<tbody>
$($ruEraRowsHtml -join "`r`n")
</tbody>
</table>

<p class="small"><strong>Nota storica:</strong> il 2021/2022 è usato qui come soglia di confronto richiesta. In Serie A le cinque sostituzioni erano già utilizzate in deroga nelle stagioni dell'emergenza Covid; la disciplina permanente FIGC/IFAB è stata formalizzata dall'estate 2022. La tabella serve quindi soprattutto a verificare empiricamente se, nella nostra lega, l'incidenza delle RU mostri un calo nel periodo recente.</p>
</div>

<div class="box">
<h2>4 quinquies. La RU è molto usata, ma quanto incide davvero sul risultato?</h2>
<p>Una RU compare in <strong>$($allRuRow.PartiteDistinte) partite</strong>, cioè nel <strong>$($allRuRow.PercentualePartite)%</strong> dello storico: è quindi una rete di sicurezza usata frequentemente.</p>
<p>Nonostante questo largo utilizzo, le partite distinte in cui la RU cambia realmente il risultato sono <strong>$($allRuRow.PartiteDecisiveDistinte)</strong>, pari al <strong>$($allRuRow.PctPartiteDecisiveTotale)%</strong> di tutte le partite.</p>
<p>Questo distingue nettamente <strong>frequenza</strong> e <strong>potere decisivo</strong>: la RU evita molto spesso di lasciare una squadra incompleta, ma solo in una quota molto più piccola delle partite altera l'esito finale.</p>

<h3>Effetto nel solo regime moderno RU 3/2</h3>
<p><strong>$modern32Warning</strong></p>
<p>Nel campione moderno sono state analizzate <strong>$($modern32Stats.PartiteCampione)</strong> partite. Una RU compare in <strong>$($modern32Stats.PartiteConRU)</strong> partite (<strong>$($modern32Stats.PctPartiteConRU)%</strong>).</p>
<p>Le RU decisive sono <strong>$($modern32Stats.CasiDecisivi) casi-squadra</strong>, pari al <strong>$($modern32Stats.PctDecisiviSuRU)%</strong> dei casi con RU; le <strong>partite distinte</strong> in cui la RU cambia l'esito sono <strong>$($modern32Stats.PartiteDecisive)</strong>, cioè il <strong>$($modern32Stats.PctPartiteDecisiveCampione)%</strong> di tutte le partite del periodo.</p>

<table>
<thead><tr><th>Stagione</th><th>RU osservate</th><th>Valori RU movimento</th><th>Valori RU portiere</th><th>Compatibile con 3/2</th></tr></thead>
<tbody>
$($ruRulesRowsHtml -join "`r`n")
</tbody>
</table>
</div>

<div class="conclusion">
<h2>Conclusione regolamentare</h2>
<p><strong>Le 5 sostituzioni risolvono bene un problema raro.</strong> La simulazione completa riporta a 11 almeno una squadra in circa <strong>$fiveSubResolvedPctAll%</strong> delle partite storiche. Due cambi in più possono quindi essere comodi; semplicemente, non stanno salvando il campionato dall'apocalisse. Se si accetta che una quota intorno al 3% delle partite resti esposta alla sfortuna pura, non emerge una necessità statistica forte di cambiare la regola soltanto per eliminare questi episodi.</p>
<p><strong>La RU, invece, risponde a un problema frequente.</strong> È comparsa nel <strong>$($allRuRow.PercentualePartite)%</strong> delle partite: senza quella rete di sicurezza, una quantità molto maggiore di gare avrebbe almeno una squadra costretta a rinunciare a un giocatore perché non dispone più di un sostituto utilizzabile per quel ruolo.</p>
<p><strong>Il suo impatto sul risultato è però molto più basso del suo utilizzo.</strong> Solo il <strong>$($allRuRow.PctPartiteDecisiveTotale)%</strong> di tutte le partite storiche viene effettivamente deciso dalla RU. Quindi sì, la RU compare spesso; no, non è il ministero dei risultati regalati. Nella maggior parte dei casi evita soprattutto che una partita venga impoverita dalla formazione incompleta senza diventare essa stessa il fattore che decide l'esito.</p>
<p>Dal punto di vista della filosofia della lega, la distinzione è quindi utile: la sfortuna estrema può essere lasciata vivere perché è rara; la RU conserva invece una responsabilità gestionale, perché interviene quando una rosa o una formazione non dispone più di alternative valide per quel ruolo. Toglierla aumenterebbe molto più della regola dei 5 cambi il numero di squadre incomplete e renderebbe più prevedibili alcune gare per l'avversario, riducendo la possibilità che la squadra penalizzata resti comunque competitiva.</p>
<p><strong>Andamento nel tempo:</strong> la tabella per stagione consente inoltre di verificare se l'uso della RU sia effettivamente diminuito nel periodo recente, in particolare dal 2021/2022. Se la percentuale di partite con RU scende, questo suggerisce che rose, disponibilità di calciatori e maggiore flessibilità delle sostituzioni abbiano ridotto il bisogno della rete di sicurezza; se resta stabile, significa invece che il problema coperto dalla RU è strutturale e non viene eliminato semplicemente aumentando i cambi. Le impressioni del lunedì mattina, purtroppo, non hanno una colonna nel database.</p>
<p><strong>Quando si gioca davvero corti:</strong> la nuova sezione 5C mostra separatamente gli esiti con esattamente 10 giocatori e con meno di 10. È il controllo finale più concreto: se le sconfitte dominano soprattutto scendendo sotto 10, allora lasciare qualche raro caso alla sfortuna può essere una scelta; moltiplicare artificialmente quei casi abolendo la RU è tutt'altra faccenda.</p>
<p><strong>In sintesi:</strong> i dati sostengono meglio il mantenimento della RU che l'introduzione delle 5 sostituzioni. Le 5 sostituzioni hanno un beneficio reale ma limitato sul totale; la RU è molto più necessaria come meccanismo di continuità della partita, pur risultando decisiva soltanto in una minoranza delle gare.</p>
</div>

<h2>5A. Risultato delle squadre arrivate a 11 con almeno una RU</h2>
<p>Questi sono i casi in cui almeno una RU è entrata e la squadra ha comunque chiuso con tutti gli 11 posti coperti: <strong>$($full11WithRu.Count)</strong> casi-squadra, cioè il <strong>$pctRuCompleted11%</strong> di tutti i casi con RU.</p>
<p>È il gruppo che chiarisce meglio se la RU sia davvero una scorciatoia per vincere o se, molto più prosaicamente, serva soprattutto a non giocare con un buco. Spoiler statistico: le leggende da spogliatoio soffrono parecchio quando incontrano un denominatore.</p>
<table>
<thead><tr><th>Situazione</th><th>Casi</th><th>% sui casi a 11 con RU</th></tr></thead>
<tbody>
<tr><td class="left strong">Vittorie</td><td>$($outcomeFull11WithRu["VITTORIA"])</td><td>$(Get-Percent $outcomeFull11WithRu["VITTORIA"] $full11WithRu.Count 2)%</td></tr>
<tr><td class="left strong">Pareggi</td><td>$($outcomeFull11WithRu["PAREGGIO"])</td><td>$(Get-Percent $outcomeFull11WithRu["PAREGGIO"] $full11WithRu.Count 2)%</td></tr>
<tr><td class="left strong">Sconfitte</td><td>$($outcomeFull11WithRu["SCONFITTA"])</td><td>$(Get-Percent $outcomeFull11WithRu["SCONFITTA"] $full11WithRu.Count 2)%</td></tr>
<tr><td class="left strong">La RU ha evitato una sconfitta</td><td>$($full11Effects["SCONFITTA_PAREGGIO"])</td><td>$(Get-Percent $full11Effects["SCONFITTA_PAREGGIO"] $full11WithRu.Count 2)%</td></tr>
<tr><td class="left strong">La RU ha trasformato il pareggio in vittoria</td><td>$($full11Effects["PAREGGIO_VITTORIA"])</td><td>$(Get-Percent $full11Effects["PAREGGIO_VITTORIA"] $full11WithRu.Count 2)%</td></tr>
<tr><td class="left strong">La RU ha trasformato la sconfitta in vittoria</td><td>$($full11Effects["SCONFITTA_VITTORIA"])</td><td>$(Get-Percent $full11Effects["SCONFITTA_VITTORIA"] $full11WithRu.Count 2)%</td></tr>
<tr><td class="left strong red">Totale casi in cui la RU ha cambiato il risultato</td><td class="red strong">$full11Decisive</td><td class="red strong">$(Get-Percent $full11Decisive $full11WithRu.Count 2)%</td></tr>
</tbody>
</table>

<h2>5B. Risultato delle squadre rimaste sotto 11 pur avendo RU</h2>
<p>Qui invece la sfortuna ha deciso di fare straordinario: almeno una RU è entrata, ma la squadra è rimasta comunque incompleta. Sono <strong>$($under11WithRu.Count)</strong> casi-squadra, il <strong>$pctRuFailedToReach11%</strong> dei casi con RU.</p>
<table>
<thead><tr><th>Situazione</th><th>Casi</th><th>% sui casi sotto 11 con RU</th></tr></thead>
<tbody>
<tr><td class="left strong">Vittorie</td><td>$($outcomeWithRu["VITTORIA"])</td><td>$(Get-Percent $outcomeWithRu["VITTORIA"] $under11WithRu.Count 2)%</td></tr>
<tr><td class="left strong">Pareggi</td><td>$($outcomeWithRu["PAREGGIO"])</td><td>$(Get-Percent $outcomeWithRu["PAREGGIO"] $under11WithRu.Count 2)%</td></tr>
<tr><td class="left strong">Sconfitte</td><td>$($outcomeWithRu["SCONFITTA"])</td><td>$(Get-Percent $outcomeWithRu["SCONFITTA"] $under11WithRu.Count 2)%</td></tr>
<tr><td class="left strong">La RU ha evitato una sconfitta</td><td>$($under11Effects["SCONFITTA_PAREGGIO"])</td><td>$(Get-Percent $under11Effects["SCONFITTA_PAREGGIO"] $under11WithRu.Count 2)%</td></tr>
<tr><td class="left strong">La RU ha trasformato il pareggio in vittoria</td><td>$($under11Effects["PAREGGIO_VITTORIA"])</td><td>$(Get-Percent $under11Effects["PAREGGIO_VITTORIA"] $under11WithRu.Count 2)%</td></tr>
<tr><td class="left strong">La RU ha trasformato la sconfitta in vittoria</td><td>$($under11Effects["SCONFITTA_VITTORIA"])</td><td>$(Get-Percent $under11Effects["SCONFITTA_VITTORIA"] $under11WithRu.Count 2)%</td></tr>
<tr><td class="left strong red">Totale casi in cui la RU ha cambiato il risultato</td><td class="red strong">$under11Decisive</td><td class="red strong">$(Get-Percent $under11Decisive $under11WithRu.Count 2)%</td></tr>
</tbody>
</table>


<h2>5C. Quando si gioca davvero in 10, e quando va persino peggio</h2>
<p>Questo è il dato che mancava: non quante volte succede, ma <strong>come finiscono le partite quando succede</strong>. Perché "giocare in 10 non è poi così grave" è una frase che merita almeno di incontrare una tabella prima di diventare regolamento.</p>

<h3>Esattamente 10 giocatori effettivi</h3>
<p>Casi-squadra: <strong>$($playedIn10.Count)</strong>. Partite distinte coinvolte: <strong>$($playedIn10MatchKeys.Count)</strong> su $totalMatches (<strong>$(Get-Percent $playedIn10MatchKeys.Count $totalMatches 2)%</strong>).</p>
<table>
<thead><tr><th>Esito</th><th>Casi</th><th>% sui casi giocati in 10</th></tr></thead>
<tbody>
<tr><td class="left strong">Vittorie</td><td>$($outcomePlayedIn10["VITTORIA"])</td><td>$(Get-Percent $outcomePlayedIn10["VITTORIA"] $playedIn10.Count 2)%</td></tr>
<tr><td class="left strong">Pareggi</td><td>$($outcomePlayedIn10["PAREGGIO"])</td><td>$(Get-Percent $outcomePlayedIn10["PAREGGIO"] $playedIn10.Count 2)%</td></tr>
<tr><td class="left strong red">Sconfitte</td><td class="red strong">$($outcomePlayedIn10["SCONFITTA"])</td><td class="red strong">$(Get-Percent $outcomePlayedIn10["SCONFITTA"] $playedIn10.Count 2)%</td></tr>
</tbody>
</table>

<h3>Meno di 10 giocatori effettivi</h3>
<p>Casi-squadra: <strong>$($playedBelow10.Count)</strong>. Partite distinte coinvolte: <strong>$($playedBelow10MatchKeys.Count)</strong> su $totalMatches (<strong>$(Get-Percent $playedBelow10MatchKeys.Count $totalMatches 3)%</strong>).</p>
<table>
<thead><tr><th>Esito</th><th>Casi</th><th>% sui casi giocati in meno di 10</th></tr></thead>
<tbody>
<tr><td class="left strong">Vittorie</td><td>$($outcomePlayedBelow10["VITTORIA"])</td><td>$(Get-Percent $outcomePlayedBelow10["VITTORIA"] $playedBelow10.Count 2)%</td></tr>
<tr><td class="left strong">Pareggi</td><td>$($outcomePlayedBelow10["PAREGGIO"])</td><td>$(Get-Percent $outcomePlayedBelow10["PAREGGIO"] $playedBelow10.Count 2)%</td></tr>
<tr><td class="left strong red">Sconfitte</td><td class="red strong">$($outcomePlayedBelow10["SCONFITTA"])</td><td class="red strong">$(Get-Percent $outcomePlayedBelow10["SCONFITTA"] $playedBelow10.Count 2)%</td></tr>
</tbody>
</table>

<p class="small">Qui "giocatori effettivi" significa giocatori reali + eventuali RU nei primi 11 slot del TABELLINO. Quindi la distinzione 10 / meno di 10 fotografa davvero quanti uomini hanno prodotto il tabellino finale.</p>

<h2>6. Tutti i casi in cui una squadra è rimasta in 10 o meno</h2>
<div class="explain">
<p>Questa tabella elenca ogni singolo caso trovato e serve anche come controllo manuale.</p>
<p><strong>Giocatori effettivi</strong> = giocatori reali + RU presenti nei primi 11 slot. Gli slot vuoti non vengono contati.</p>
</div>

<table>
<thead>
<tr><th>Stagione</th><th>Competizione</th><th>Giornata</th><th>Squadra</th><th>Avversaria</th><th>Giocatori effettivi</th><th>RU in campo</th><th>Esito</th><th>Risultato</th><th>Punteggio</th><th>Effetto RU</th><th>Tabellino</th></tr>
</thead>
<tbody>
$($casesHtml -join "`r`n")
</tbody>
</table>

<h2>7. Glossario</h2>
<div class="box explain">
<p><strong>Riserva d'Ufficio (RU):</strong> un voto assegnato d'ufficio quando le normali sostituzioni non riescono a coprire un giocatore mancante.</p>
<p><strong>Partita coinvolta:</strong> l'incontro viene contato una sola volta anche se entrambe le squadre presentano la stessa situazione.</p>
<p><strong>Squadra-partita:</strong> la prestazione di una singola squadra in una singola partita.</p>
<p><strong>RU decisiva:</strong> una RU è decisiva quando, togliendo i suoi punti, il risultato della squadra peggiorerebbe.</p>
<p><strong>In 10 o meno:</strong> il tabellino finale FCM presenta almeno uno dei primi 11 posti scoperto.</p>
<p><strong>In 10 o meno senza RU:</strong> almeno uno slot è vuoto e nessuna RU è presente nei primi 11 posti.</p>
<p><strong>In 10 o meno con RU:</strong> almeno una RU è effettivamente in campo ma resta ancora almeno uno slot vuoto. È il caso in cui la RU non basta a completare gli 11.</p>
<p><strong>% sul totale storico:</strong> usa come denominatore tutte le partite valide analizzate.</p>
<p><strong>% RU che non completano gli 11:</strong> usa come denominatore esclusivamente le prestazioni di squadra nelle quali almeno una RU è presente negli 11 posti finali.</p>
<p><strong>Simulazione 5 sostituzioni:</strong> usa i casi sotto 11 senza RU, ricostruisce gli 11 iniziali quando FORMATION.POS=0 produce esattamente 11 titolari, individua i ruoli rimasti scoperti e verifica se esistono panchinari con voto valido e ruolo compatibile. Le sostituzioni già effettuate sono lette da ENTRATORISERVA.</p>
<p><strong>Regime RU 3/2:</strong> blocco cronologico più recente in cui tutte le RU di movimento osservate valgono 3 e tutte le RU portiere osservate valgono 2. Se il blocco comprende una sola stagione, il report lo segnala esplicitamente come campione corto.</p>
<p><strong>Il Fantoccio:</strong> tutte le partite che coinvolgono la squadra fittizia sono escluse integralmente dal campione e da ogni denominatore.</p>
</div>

<div class="small">File generato automaticamente da Report_RU_Storico_Completo_v8_4.ps1.</div>

</body>
</html>
"@

Set-Content -Path $htmlPath -Value $htmlContent -Encoding UTF8

Write-Host ""
Write-Host "=== REPORT RU STORICO COMPLETO ==="
Write-Host ""
Write-Host "Partite storiche valide (TABELLINO)      : $totalMatches"
Write-Host "Partite uniche nei normalized            : $normalizedMatches"
Write-Host "Normalized senza TABELLINO               : $normalizedNonCanonical"
Write-Host "Partite con almeno una RU                : $($allRuRow.PartiteDistinte)"
Write-Host "Prestazioni squadra con RU (normalized)  : $($allRuRow.SquadrePartita)"
Write-Host "RU totali (normalized)                   : $($allRuRow.OccorrenzeRU)"
Write-Host ""
Write-Host "Prestazioni squadra sotto 11             : $($under11.Count)"
Write-Host "Partite con almeno una squadra sotto 11  : $($under11MatchKeys.Count)"
Write-Host "% partite con almeno una squadra sotto11 : $pctUnder11Total %"
Write-Host ""
Write-Host "Sotto 11 senza RU                        : $($under11NoRu.Count)"
Write-Host "Sotto 11 con almeno una RU               : $($under11WithRu.Count)"
Write-Host "Giocato esattamente in 10                : $($playedIn10.Count) | V=$($outcomePlayedIn10["VITTORIA"]) N=$($outcomePlayedIn10["PAREGGIO"]) P=$($outcomePlayedIn10["SCONFITTA"])"
Write-Host "Giocato in meno di 10                    : $($playedBelow10.Count) | V=$($outcomePlayedBelow10["VITTORIA"]) N=$($outcomePlayedBelow10["PAREGGIO"]) P=$($outcomePlayedBelow10["SCONFITTA"])"
Write-Host "% senza RU tra i casi sotto 11           : $pctNoRuAmongUnder11 %"
Write-Host "% con RU tra i casi sotto 11             : $pctWithRuAmongUnder11 %"
Write-Host ""
Write-Host "Prestazioni con RU in campo (TABELLINO)  : $($allWithRu.Count)"
Write-Host "Con RU e formazione completa a 11        : $($full11WithRu.Count) ($pctRuCompleted11 %)"
Write-Host "Con RU ma ancora sotto 11                : $($under11WithRu.Count) ($pctRuFailedToReach11 %)"
Write-Host "% RU che non completano gli 11           : $pctRuFailedToReach11 %"
Write-Host ""
Write-Host ""
Write-Host "=== PULIZIA CAMPIONE ==="
Write-Host "Partite Il Fantoccio escluse              : $fakeTeamMatchesExcluded"
Write-Host "Partite valide dopo esclusione             : $totalMatches"
Write-Host ""
Write-Host "=== 5 SOSTITUZIONI ==="
Write-Host "Casi simulabili                           : $fiveSubSimulable"
Write-Host "Partite con cambio compatibile            : $($fiveSubBenefitMatchKeys.Count) ($fiveSubBenefitPctAll %)"
Write-Host "Partite completamente riportate a 11      : $($fiveSubResolvedMatchKeys.Count) ($fiveSubResolvedPctAll %)"
Write-Host ""
Write-Host "=== RU MODERNA 3/2 ==="
Write-Host "Periodo rilevato                          : $modern32Start -> $modern32End"
Write-Host "Numero stagioni                           : $($modern32Seasons.Count)"
Write-Host "Partite campione                          : $($modern32Stats.PartiteCampione)"
Write-Host "Partite con RU                            : $($modern32Stats.PartiteConRU) ($($modern32Stats.PctPartiteConRU) %)"
Write-Host "Partite con RU decisiva                   : $($modern32Stats.PartiteDecisive) ($($modern32Stats.PctPartiteDecisiveCampione) %)"
Write-Host ""
Write-Host "CSV riepilogo RU  : $ruSummaryCsv"
Write-Host "CSV casi sotto 11 : $under11Csv"
Write-Host "CSV simulazione 5 : $fiveSubCsv"
Write-Host "CSV regole RU     : $ruRulesCsv"
Write-Host "CSV RU per annata : $ruSeasonCsv"
Write-Host "CSV pre/post 21/22: $ruEraCsv"
Write-Host "HTML completo     : $htmlPath"
Write-Host ""
