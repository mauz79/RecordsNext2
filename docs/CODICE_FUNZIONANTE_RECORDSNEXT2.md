# Codice funzionante RecordsNext 2.0

> Documento generato automaticamente.
> Data generazione: 2026-08-05 14:48:54 +02:00
> Directory progetto: D:\DEV_APPS\RecordsNext2.0

## Regole della bibbia

- Le decisioni progettuali consolidate sono separate dal codice implementato.
- Un file incluso non e automaticamente dichiarato funzionante.
- Lo stato implementato deve essere aggiornato soltanto dopo test.
- Le questioni ancora aperte non devono essere presentate come funzionalita.

## Stato sintetico

### Decisioni consolidate

- Progetto separato in D:\DEV_APPS\RecordsNext2.0.
- Cinque famiglie: Classici, Serie, Riserve d'Ufficio, Modificatori, Soglie e Fortuna.
- Fattore Campo incluso nei Modificatori.
- Culometro opzionale e prodotto soltanto su richiesta.
- Dipendenze gestite a livello di singolo figlio.
- Associazioni canoniche per squadre e competizioni.
- Link ai tabellini per i record riferiti a partite specifiche.
- JS pubblici nella cartella js del sito.
- Un solo HTML indice nella root del sito.
- Viste HTML dimostrative nella cartella RecordsNext.

### Implementato e verificato

- Struttura iniziale del progetto.
- Documentazione architetturale iniziale.
- Generatore iniziale della bibbia.

### Non ancora implementato

- Lettura FCM e FCA.
- Modello dati.
- Elaboratori delle famiglie.
- Esportatori JS.
- GUI.
- Installer.
- Viste HTML 2.0.

## README

File: README.md

    # RecordsNext 2.0
    
    RecordsNext 2.0 e un nuovo progetto indipendente che genera viste dati modulari, complete e filtrabili, dalle quali ricavare record stagionali, assoluti, globali e personali.
    
    ## Directory
    
    `D:\DEV_APPS\RecordsNext2.0`
    
    ## Repository di riferimento
    
    - https://github.com/mauz79/RecordsNext
    - https://github.com/mauz79/ConfrontiStorici-3.x-Plus
    - https://github.com/mauz79/ConfrontiStorici34
    
    I download manuali vanno salvati in `D:\DEV_APPS\downloads`.
    
    ## Famiglie iniziali
    
    1. Classici
    2. Serie
    3. Riserve d'Ufficio
    4. Modificatori
    5. Soglie e Fortuna
    
    Il Culometro e un easter egg opzionale, generato soltanto su richiesta esplicita e con configurazione dedicata.
    
    ## Output previsti
    
    - `fcmRecordsNext_Core.js`
    - `fcmRecordsNext_Manifest.js`
    - `fcmRecordsNext_Classics.js`
    - `fcmRecordsNext_Series.js`
    - `fcmRecordsNext_RU.js`
    - `fcmRecordsNext_Modifiers.js`
    - `fcmRecordsNext_ThresholdsLuck.js`
    - `fcmRecordsNext_Culometro.js`
    
    Tutti i JS pubblici andranno nella cartella `js` del sito FCM. Nella root ci sara un solo `recordsnext.html`; viste e asset dimostrativi andranno nella cartella `RecordsNext`.
    
    ## Bibbia
    
    La bibbia del progetto e `docs\CODICE_FUNZIONANTE_RECORDSNEXT2.md`. Deve distinguere decisioni consolidate, codice implementato e verificato, lavori in corso e questioni aperte.

## Architettura

File: docs\ARCHITETTURA_RECORDSNEXT2.md

    # Architettura RecordsNext 2.0
    
    ## Scopo
    
    RecordsNext 2.0 genera viste dati tematiche complete e filtrabili, non semplici classifiche finali.
    
    ## Flusso
    
    ```text
    FCM / FCA / configurazioni
                |
                v
    normalizzazione stagioni, squadre e competizioni
                |
                v
    dataset comune delle partite
                |
                v
    elaboratori delle famiglie
                |
                v
    viste dati JS modulari
                |
                v
    viste HTML filtrabili
    ```
    
    ## Famiglie
    
    ### Classici
    
    Punteggi, medie, somme, risultati, gol, disciplinari, assist, autogol, rigori, clean sheet e aggregati per squadra, giocatore e portiere. Le sequenze appartengono a Serie.
    
    Output: `fcmRecordsNext_Classics.js`
    
    ### Serie
    
    Serie positive e negative, vittorie, pareggi e sconfitte consecutive, clean sheet consecutivi e serie dipendenti da eventi o modificatori.
    
    Output: `fcmRecordsNext_Series.js`
    
    ### Riserve d'Ufficio
    
    PU, DU, CU, AU, partite con e contro uffici, uffici decisivi, bilanci, medie e distribuzioni.
    
    Output: `fcmRecordsNext_RU.js`
    
    ### Modificatori
    
    Difesa, Capitano, altri modificatori selezionati e Fattore Campo.
    
    Output: `fcmRecordsNext_Modifiers.js`
    
    ### Soglie e Fortuna
    
    Vittorie chirurgiche, sconfitte beffa, pareggi miracolati e stretti, mezzo punto, soglie precise, spreco punti e indicatori di fortuna/sfortuna.
    
    Output: `fcmRecordsNext_ThresholdsLuck.js`
    
    ## Culometro
    
    Easter egg opzionale: non viene generato automaticamente; richiede selezione e configurazione dedicate.
    
    Output: `fcmRecordsNext_Culometro.js`
    
    ## Dipendenze
    
    Ogni figlio dichiara dipendenze obbligatorie e opzionali. Una dipendenza mancante blocca solo il figlio interessato.
    
    Esempio: senza Capitano non vengono generati i figli Capitano e le relative serie, ma le altre serie continuano.
    
    ## Ambiti temporali
    
    - stagionale: valore per squadra e stagione;
    - globale: aggregato su tutte le stagioni dell'identita canonica;
    - assoluto: migliore o peggiore occorrenza fra tutte le stagioni;
    - storico continuo: sequenza che puo attraversare piu stagioni.
    
    Non tutti i record supportano tutti gli ambiti.
    
    ## Identita canoniche
    
    Squadre e competizioni conservano identita e nome stagionale, identita e nome canonico, stagione e stato dell'associazione.
    
    ## Filtri minimi
    
    Famiglia, figlio, squadra stagionale e canonica, stagione, competizione stagionale e canonica, casa/trasferta/neutro, squadre attuali/tutte e ambito temporale.
    
    ## Tabellini
    
    Ogni record riferito a una partita specifica conserva identificativo, link locale e online, stagione, competizione, giornata, squadre, risultato e data.
    
    ## Fuori perimetro iniziale
    
    Palmares, albo d'oro, promozioni, retrocessioni, bilancio prossimo turno, confronti diretti e altre funzioni non-record.

## Catalogo record

File: docs\CATALOGO_RECORD.md

    # Catalogo record RecordsNext 2.0
    
    ## Stati
    
    `DA_CATALOGARE`, `DEFINITO`, `IN_SVILUPPO`, `IMPLEMENTATO`, `VERIFICATO`, `SOSPESO`.
    
    ## Classici
    
    | ID provvisorio | Nome | Origine | Ambiti | Tabellino | Stato |
    |---|---|---|---|---|---|
    | classics.highest-match-score | Maggior punteggio in una partita | RecordsNext + ConfrontiStorici | Stagionale, assoluto | Singolo | DA_CATALOGARE |
    | classics.lowest-match-score | Minor punteggio in una partita | ConfrontiStorici | Stagionale, assoluto | Singolo | DA_CATALOGARE |
    | classics.most-regulation-goals | Partita con piu gol regolamentari | ConfrontiStorici | Stagionale, assoluto | Singolo | DA_CATALOGARE |
    | classics.largest-regulation-margin | Maggior scarto regolamentare | ConfrontiStorici | Stagionale, assoluto | Singolo | DA_CATALOGARE |
    | classics.average-points | Media punteggio | Plus | Stagionale, assoluto, globale | No | DA_CATALOGARE |
    | classics.total-points | Somma totale punti | Plus | Stagionale, assoluto, globale | No | DA_CATALOGARE |
    | classics.standings-points | Punti classifica | Plus | Stagionale, assoluto, globale | No | DA_CATALOGARE |
    | classics.wins | Vittorie | Plus | Stagionale, assoluto, globale | No | DA_CATALOGARE |
    | classics.draws | Pareggi | Plus | Stagionale, assoluto, globale | No | DA_CATALOGARE |
    | classics.losses | Sconfitte | Plus | Stagionale, assoluto, globale | No | DA_CATALOGARE |
    | classics.goals-for | Gol fatti | Plus | Stagionale, assoluto, globale | No | DA_CATALOGARE |
    | classics.goals-against | Gol subiti | Plus | Stagionale, assoluto, globale | No | DA_CATALOGARE |
    | classics.yellow-cards-team | Ammonizioni per squadra | RecordsNext | Stagionale, assoluto, globale | No | DA_CATALOGARE |
    | classics.red-cards-team | Espulsioni per squadra | RecordsNext | Stagionale, assoluto, globale | No | DA_CATALOGARE |
    | classics.red-cards-player | Espulsioni per giocatore | RecordsNext | Stagionale, assoluto, globale | No | DA_CATALOGARE |
    | classics.assists-team | Assist per squadra | RecordsNext | Stagionale, assoluto, globale | No | DA_CATALOGARE |
    | classics.own-goals-team | Autogol per squadra | RecordsNext | Stagionale, assoluto, globale | No | DA_CATALOGARE |
    | classics.penalties-scored | Rigori segnati | RecordsNext | Stagionale, assoluto, globale | No | DA_CATALOGARE |
    | classics.penalties-missed | Rigori sbagliati | RecordsNext | Stagionale, assoluto, globale | No | DA_CATALOGARE |
    | classics.penalties-saved | Rigori parati | RecordsNext | Stagionale, assoluto, globale | No | DA_CATALOGARE |
    | classics.clean-sheets | Clean sheet | RecordsNext | Stagionale, assoluto, globale | No | DA_CATALOGARE |
    
    ## Serie
    
    | ID provvisorio | Nome | Dipendenze | Stato |
    |---|---|---|---|
    | series.unbeaten | Serie positiva | Risultati ordinati | DA_CATALOGARE |
    | series.winless | Serie negativa | Risultati ordinati | DA_CATALOGARE |
    | series.wins | Vittorie consecutive | Risultati ordinati | DA_CATALOGARE |
    | series.draws | Pareggi consecutivi | Risultati ordinati | DA_CATALOGARE |
    | series.losses | Sconfitte consecutive | Risultati ordinati | DA_CATALOGARE |
    | series.clean-sheets | Clean sheet consecutivi | Clean sheet elaborati | DA_CATALOGARE |
    | series.captain-bonus | Bonus Capitano consecutivo | Modificatore Capitano | DA_CATALOGARE |
    
    ## Riserve d'Ufficio
    
    | ID provvisorio | Nome | Stato |
    |---|---|---|
    | ru.max-in-match | Maggior numero di RU in una partita | DA_CATALOGARE |
    | ru.matches-with | Partite con RU | DA_CATALOGARE |
    | ru.matches-against | Partite contro squadre con RU | DA_CATALOGARE |
    | ru.deciding | RU decisive | DA_CATALOGARE |
    | ru.deciding-against | RU decisive subite | DA_CATALOGARE |
    | ru.balance | Bilancio con RU | DA_CATALOGARE |
    | ru.balance-against | Bilancio contro RU | DA_CATALOGARE |
    | ru.average-points | Media punti con RU | DA_CATALOGARE |
    | ru.average-points-against | Media punti contro RU | DA_CATALOGARE |
    | ru.role-distribution | Distribuzione PU, DU, CU e AU | DA_CATALOGARE |
    
    ## Modificatori
    
    | ID provvisorio | Nome | Dipendenza | Stato |
    |---|---|---|---|
    | modifiers.defence-best-match | Miglior modificatore difesa in una gara | Difesa | DA_CATALOGARE |
    | modifiers.defence-total | Totale modificatore difesa | Difesa | DA_CATALOGARE |
    | modifiers.captain-uses | Utilizzi Capitano | Capitano | DA_CATALOGARE |
    | modifiers.captain-total | Totale modificatore Capitano | Capitano | DA_CATALOGARE |
    | modifiers.home-field-deciding | Fattore Campo decisivo | Fattore Campo | DA_CATALOGARE |
    | modifiers.home-field-points-gained | Punti guadagnati col Fattore Campo | Fattore Campo | DA_CATALOGARE |
    | modifiers.home-field-points-lost | Punti persi fuori casa | Fattore Campo | DA_CATALOGARE |
    | modifiers.home-field-balance | Saldo Fattore Campo | Fattore Campo | DA_CATALOGARE |
    
    ## Soglie e Fortuna
    
    | ID provvisorio | Nome | Stato |
    |---|---|---|
    | thresholds.surgical-win | Vittoria chirurgica | DA_CATALOGARE |
    | thresholds.mocking-loss | Sconfitta beffa | DA_CATALOGARE |
    | thresholds.miraculous-draw | Pareggio miracolato | DA_CATALOGARE |
    | thresholds.narrow-draw | Pareggio stretto | DA_CATALOGARE |
    | thresholds.missed-win-half-point | Vittoria mancata per mezzo punto | DA_CATALOGARE |
    | thresholds.loss-by-a-whisker | Sconfitta per un pelo | DA_CATALOGARE |
    | thresholds.exact-threshold | Soglia precisa | DA_CATALOGARE |
    | thresholds.just-enough | Giusto giusto | DA_CATALOGARE |
    | thresholds.wasted-points | Spreco punti | DA_CATALOGARE |
    | luck.favourable-events | Eventi favorevoli | DA_CATALOGARE |
    | luck.unfavourable-events | Eventi sfavorevoli | DA_CATALOGARE |
    | luck.balance | Saldo fortuna-sfortuna | DA_CATALOGARE |
    
    ## Culometro
    
    | ID | Regola | Stato |
    |---|---|---|
    | easter-egg.culometro | Solo su richiesta e con configurazione dedicata | DA_CATALOGARE |

## Dipendenze output

File: docs\DIPENDENZE_OUTPUT.md

    # Dipendenze output RecordsNext 2.0
    
    ## Principio
    
    Una dipendenza mancante blocca soltanto il figlio interessato ed e dichiarata nel manifest.
    
    ## Stati
    
    - `GENERATED_COMPLETE`
    - `GENERATED_PARTIAL`
    - `SKIPPED_REQUIRED_DEPENDENCY`
    - `SKIPPED_NOT_SELECTED`
    - `SKIPPED_NO_DATA`
    
    ## Capitano
    
    Senza Capitano:
    
    ```text
    modifiers.captain-total -> SKIPPED_NOT_SELECTED
    series.captain-bonus    -> SKIPPED_REQUIRED_DEPENDENCY
    ```
    
    Le altre serie continuano.
    
    ## Fattore Campo
    
    Richiede punteggi, risultato, bonus casa e configurazione delle soglie gol.
    
    ## RU decisive
    
    Richiedono RU identificate, formazione, punteggio e simulazione senza RU. I semplici conteggi possono essere prodotti senza simulazione.
    
    ## Soglie e Fortuna
    
    Gli eventi soglia richiedono punteggi, soglie gol e risultato. Gli indicatori derivati richiedono gli eventi selezionati.
    
    ## Culometro
    
    Richiede selezione esplicita, configurazione dedicata, componenti, pesi e normalizzazione. Se non selezionato: `SKIPPED_NOT_SELECTED`. Questo non rende incompleta la famiglia.

## Decisioni aperte

File: docs\DECISIONI_APERTE.md

    # Decisioni aperte RecordsNext 2.0
    
    - catalogo definitivo dei figli;
    - ID pubblici definitivi;
    - struttura esatta dei JS;
    - granularita degli eventi;
    - regole di ex aequo;
    - soglie minime di partite;
    - attraversamento stagioni nelle serie;
    - configurazione Culometro;
    - modificatori personalizzati;
    - selezione GUI;
    - formato configurazione;
    - compatibilita con output 1.0.2;
    - tecnologia del motore;
    - strategia di test comparativo;
    - forma definitiva del manifest.

## File reali del progetto

## tools\Create-RecordsNext2WorkingCodeMd.ps1

File: tools\Create-RecordsNext2WorkingCodeMd.ps1

    [CmdletBinding()]
    param(
        [string]$ProjectDir = "D:\DEV_APPS\RecordsNext2.0"
    )
    
    Set-StrictMode -Version Latest
    $ErrorActionPreference = "Stop"
    
    $DocsDir = Join-Path $ProjectDir "docs"
    $OutputFile = Join-Path $DocsDir "CODICE_FUNZIONANTE_RECORDSNEXT2.md"
    
    if (-not (Test-Path -LiteralPath $ProjectDir)) {
        throw "Directory progetto non trovata: $ProjectDir"
    }
    
    if (-not (Test-Path -LiteralPath $DocsDir)) {
        New-Item -ItemType Directory -Path $DocsDir -Force | Out-Null
    }
    
    function Add-IndentedFileSection {
        param(
            [Parameter(Mandatory = $true)]
            [System.Text.StringBuilder]$Builder,
    
            [Parameter(Mandatory = $true)]
            [string]$Title,
    
            [Parameter(Mandatory = $true)]
            [string]$RelativePath
        )
    
        $FullPath = Join-Path $ProjectDir $RelativePath
    
        [void]$Builder.AppendLine("## " + $Title)
        [void]$Builder.AppendLine("")
    
        if (-not (Test-Path -LiteralPath $FullPath)) {
            [void]$Builder.AppendLine("> File non presente: " + $RelativePath)
            [void]$Builder.AppendLine("")
            return
        }
    
        [void]$Builder.AppendLine("File: " + $RelativePath)
        [void]$Builder.AppendLine("")
    
        $Lines = Get-Content -LiteralPath $FullPath -Encoding UTF8
    
        if ($Lines.Count -eq 0) {
            [void]$Builder.AppendLine("    [file vuoto]")
        }
        else {
            foreach ($Line in $Lines) {
                [void]$Builder.AppendLine("    " + $Line)
            }
        }
    
        [void]$Builder.AppendLine("")
    }
    
    $Builder = New-Object System.Text.StringBuilder
    $GeneratedAt = Get-Date -Format "yyyy-MM-dd HH:mm:ss zzz"
    
    [void]$Builder.AppendLine("# Codice funzionante RecordsNext 2.0")
    [void]$Builder.AppendLine("")
    [void]$Builder.AppendLine("> Documento generato automaticamente.")
    [void]$Builder.AppendLine("> Data generazione: " + $GeneratedAt)
    [void]$Builder.AppendLine("> Directory progetto: " + $ProjectDir)
    [void]$Builder.AppendLine("")
    
    [void]$Builder.AppendLine("## Regole della bibbia")
    [void]$Builder.AppendLine("")
    [void]$Builder.AppendLine("- Le decisioni progettuali consolidate sono separate dal codice implementato.")
    [void]$Builder.AppendLine("- Un file incluso non e automaticamente dichiarato funzionante.")
    [void]$Builder.AppendLine("- Lo stato implementato deve essere aggiornato soltanto dopo test.")
    [void]$Builder.AppendLine("- Le questioni ancora aperte non devono essere presentate come funzionalita.")
    [void]$Builder.AppendLine("")
    
    [void]$Builder.AppendLine("## Stato sintetico")
    [void]$Builder.AppendLine("")
    [void]$Builder.AppendLine("### Decisioni consolidate")
    [void]$Builder.AppendLine("")
    [void]$Builder.AppendLine("- Progetto separato in D:\DEV_APPS\RecordsNext2.0.")
    [void]$Builder.AppendLine("- Cinque famiglie: Classici, Serie, Riserve d'Ufficio, Modificatori, Soglie e Fortuna.")
    [void]$Builder.AppendLine("- Fattore Campo incluso nei Modificatori.")
    [void]$Builder.AppendLine("- Culometro opzionale e prodotto soltanto su richiesta.")
    [void]$Builder.AppendLine("- Dipendenze gestite a livello di singolo figlio.")
    [void]$Builder.AppendLine("- Associazioni canoniche per squadre e competizioni.")
    [void]$Builder.AppendLine("- Link ai tabellini per i record riferiti a partite specifiche.")
    [void]$Builder.AppendLine("- JS pubblici nella cartella js del sito.")
    [void]$Builder.AppendLine("- Un solo HTML indice nella root del sito.")
    [void]$Builder.AppendLine("- Viste HTML dimostrative nella cartella RecordsNext.")
    [void]$Builder.AppendLine("")
    
    [void]$Builder.AppendLine("### Implementato e verificato")
    [void]$Builder.AppendLine("")
    [void]$Builder.AppendLine("- Struttura iniziale del progetto.")
    [void]$Builder.AppendLine("- Documentazione architetturale iniziale.")
    [void]$Builder.AppendLine("- Generatore iniziale della bibbia.")
    [void]$Builder.AppendLine("")
    
    [void]$Builder.AppendLine("### Non ancora implementato")
    [void]$Builder.AppendLine("")
    [void]$Builder.AppendLine("- Lettura FCM e FCA.")
    [void]$Builder.AppendLine("- Modello dati.")
    [void]$Builder.AppendLine("- Elaboratori delle famiglie.")
    [void]$Builder.AppendLine("- Esportatori JS.")
    [void]$Builder.AppendLine("- GUI.")
    [void]$Builder.AppendLine("- Installer.")
    [void]$Builder.AppendLine("- Viste HTML 2.0.")
    [void]$Builder.AppendLine("")
    
    Add-IndentedFileSection -Builder $Builder -Title "README" -RelativePath "README.md"
    Add-IndentedFileSection -Builder $Builder -Title "Architettura" -RelativePath "docs\ARCHITETTURA_RECORDSNEXT2.md"
    Add-IndentedFileSection -Builder $Builder -Title "Catalogo record" -RelativePath "docs\CATALOGO_RECORD.md"
    Add-IndentedFileSection -Builder $Builder -Title "Dipendenze output" -RelativePath "docs\DIPENDENZE_OUTPUT.md"
    Add-IndentedFileSection -Builder $Builder -Title "Decisioni aperte" -RelativePath "docs\DECISIONI_APERTE.md"
    
    $SourceExtensions = @(
        ".java",
        ".ps1",
        ".json",
        ".js",
        ".html",
        ".css",
        ".xml",
        ".properties"
    )
    
    $SourceRoots = @(
        "src",
        "config",
        "tools"
    )
    
    [void]$Builder.AppendLine("## File reali del progetto")
    [void]$Builder.AppendLine("")
    
    $IncludedFiles = New-Object System.Collections.Generic.List[string]
    
    foreach ($RelativeRoot in $SourceRoots) {
        $FullRoot = Join-Path $ProjectDir $RelativeRoot
    
        if (-not (Test-Path -LiteralPath $FullRoot)) {
            continue
        }
    
        $Files = Get-ChildItem -LiteralPath $FullRoot -File -Recurse |
            Where-Object { $SourceExtensions -contains $_.Extension.ToLowerInvariant() } |
            Sort-Object FullName
    
        foreach ($File in $Files) {
            $RelativePath = $File.FullName.Substring($ProjectDir.Length).TrimStart("\")
            [void]$IncludedFiles.Add($RelativePath)
    
            Add-IndentedFileSection `
                -Builder $Builder `
                -Title $RelativePath `
                -RelativePath $RelativePath
        }
    }
    
    [void]$Builder.AppendLine("## Indice dei file inclusi")
    [void]$Builder.AppendLine("")
    
    if ($IncludedFiles.Count -eq 0) {
        [void]$Builder.AppendLine("- Nessun file sorgente presente.")
    }
    else {
        foreach ($RelativePath in $IncludedFiles) {
            [void]$Builder.AppendLine("- " + $RelativePath)
        }
    }
    
    [void]$Builder.AppendLine("")
    [void]$Builder.AppendLine("## Fine documento")
    
    $Utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText(
        $OutputFile,
        $Builder.ToString(),
        $Utf8NoBom
    )
    
    Write-Host ""
    Write-Host "Bibbia generata:" -ForegroundColor Green
    Write-Host $OutputFile
    Write-Host ""
    Write-Host ("Dimensione: {0} byte" -f (Get-Item -LiteralPath $OutputFile).Length)

## tools\Initialize-RecordsNext2Project.ps1

File: tools\Initialize-RecordsNext2Project.ps1

    [CmdletBinding()]
    param([string]$ProjectDir = "D:\DEV_APPS\RecordsNext2.0")
    Set-StrictMode -Version Latest
    $ErrorActionPreference = "Stop"
    $directories = @(
        "config","docs","examples","examples\site","release","release\site-examples",
        "reports","src","src\main","src\main\java","src\main\resources",
        "src\test","src\test\java","src\test\resources","tests","tools"
    )
    New-Item -ItemType Directory -Path $ProjectDir -Force | Out-Null
    foreach ($relativePath in $directories) {
        New-Item -ItemType Directory -Path (Join-Path $ProjectDir $relativePath) -Force | Out-Null
    }
    Write-Host "Struttura RecordsNext 2.0 pronta: $ProjectDir" -ForegroundColor Green

## Indice dei file inclusi

- tools\Create-RecordsNext2WorkingCodeMd.ps1
- tools\Initialize-RecordsNext2Project.ps1

## Fine documento
