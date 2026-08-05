# Codice funzionante RecordsNext 2.0

> Documento generato automaticamente.
> Data generazione: 2026-08-05 15:43:27 +02:00
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
- Generatore della bibbia aggiornato con documentazione e sorgenti reali.

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

## Modello dati

File: docs\MODELLO_DATI_RECORDSNEXT2.md

    # Modello dati RecordsNext 2.0
    
    ## 1. Scopo
    
    Questo documento definisce il modello dati comune di RecordsNext 2.0.
    
    Il modello deve sostenere:
    
    - cinque famiglie elaborabili;
    - record stagionali, assoluti, globali e personali;
    - serie cronologiche;
    - associazioni fra squadre stagionali e squadre canoniche;
    - associazioni fra competizioni stagionali e competizioni canoniche;
    - record riferiti a una partita specifica;
    - collegamenti ai tabellini locali e online;
    - dipendenze fra output;
    - generazione modulare dei file JavaScript;
    - GUI futura;
    - stagioni gestite e stagioni manuali.
    
    Il modello comune deve essere indipendente dalla singola famiglia di record.
    
    ## 2. Principi generali
    
    ### 2.1 Dati completi, non classifiche finali
    
    Gli output non devono contenere soltanto top 10 o classifiche già tagliate.
    
    Devono contenere dati sufficientemente completi da permettere alle viste HTML di ricavare:
    
    - record generali;
    - record personali;
    - classifiche complete;
    - migliori e peggiori occorrenze;
    - filtri per squadra;
    - filtri per stagione;
    - filtri per competizione;
    - filtri per ambito;
    - confronti fra squadre;
    - dettagli partita.
    
    I limiti di visualizzazione appartengono alla vista, non al dataset.
    
    ### 2.2 Identità stagionale e identità canonica
    
    Ogni squadra e competizione deve conservare due livelli di identità:
    
    - identità originale della stagione;
    - identità canonica storica.
    
    Questo permette di mantenere i nomi realmente usati nella stagione e, nello stesso tempo, aggregare correttamente tutta la storia della stessa entità.
    
    ### 2.3 Dipendenze locali
    
    Ogni figlio dichiara le proprie dipendenze.
    
    Se una dipendenza manca:
    
    - non viene bloccata automaticamente la famiglia;
    - viene saltato soltanto il figlio interessato;
    - il manifest registra il motivo;
    - non vengono generati dati apparentemente completi ma incompleti.
    
    ### 2.4 Provenienza dei dati
    
    Ogni dato derivato deve poter dichiarare:
    
    - fonte;
    - stagione;
    - file origine;
    - record origine;
    - metodo di calcolo;
    - eventuale fallback;
    - eventuale simulazione.
    
    ## 3. Entità principali
    
    Il modello comune iniziale comprende:
    
    1. configurazione lega;
    2. stagione;
    3. sito della stagione;
    4. squadra stagionale;
    5. squadra canonica;
    6. associazione squadra;
    7. competizione stagionale;
    8. competizione canonica;
    9. associazione competizione;
    10. giornata;
    11. partita;
    12. partecipazione squadra alla partita;
    13. giocatore;
    14. presenza giocatore;
    15. evento giocatore;
    16. modificatore;
    17. riserva d'ufficio;
    18. evento soglia;
    19. evento fortuna-sfortuna;
    20. serie;
    21. aggregato stagionale;
    22. aggregato globale;
    23. occorrenza assoluta;
    24. dipendenza;
    25. stato output;
    26. manifest.
    
    ## 4. Configurazione della lega
    
    Struttura concettuale:
    
        {
          "leagueId": "alterlega",
          "leagueName": "AlterLega",
          "currentSeasonId": "2025_2026",
          "defaultLocale": "it-IT",
          "defaultTimeZone": "Europe/Rome",
          "dataVersion": "2.0"
        }
    
    Campi minimi:
    
    | Campo | Significato |
    |---|---|
    | leagueId | Identificativo stabile della lega |
    | leagueName | Nome visualizzato |
    | currentSeasonId | Ultima stagione gestita |
    | defaultLocale | Formato locale |
    | defaultTimeZone | Fuso orario |
    | dataVersion | Versione dello schema |
    
    ## 5. Stagione
    
    Struttura concettuale:
    
        {
          "seasonId": "2025_2026",
          "startYear": 2025,
          "endYear": 2026,
          "seasonNumber": 21,
          "status": "MANAGED",
          "fcmFile": "AlterLega 2025_2026-21-2025.fcm",
          "fcaFile": "ArchivioA2025SerieA.fca",
          "siteLocalConfigured": true,
          "siteOnlineConfigured": true,
          "hasMatchSheets": true
        }
    
    Stati ammessi:
    
    - MANAGED;
    - MANUAL;
    - ARCHIVED;
    - CURRENT;
    - DISABLED.
    
    Regole:
    
    - una stagione MANAGED può essere elaborata da FCM/FCA;
    - una stagione MANUAL resta nello storico ma non viene elaborata da FCM;
    - una stagione MANUAL può avere classifiche o albo d'oro manuali;
    - una stagione MANUAL può non avere tabellini;
    - l'ultimo FCM disponibile rappresenta la stagione attuale del sito, anche quando cronologicamente non è l'anno corrente reale.
    
    ## 6. Sito della stagione
    
    Ogni stagione può avere:
    
    - sito locale;
    - sito online.
    
    Struttura concettuale:
    
        {
          "seasonId": "2025_2026",
          "localSiteRoot": "E:\\fantacalcio\\Lega2025",
          "onlineSiteRoot": "http://www.alterlega.altervista.org/lega2025",
          "matchPageType": "HTM",
          "matchPageName": "ris.htm",
          "dataAPath": "js\\DataA.js"
        }
    
    Campi minimi:
    
    | Campo | Significato |
    |---|---|
    | localSiteRoot | Root del sito locale |
    | onlineSiteRoot | Root del sito online |
    | matchPageType | HTM, PHP o altro formato configurato |
    | matchPageName | Nome effettivo della pagina tabellino |
    | dataAPath | Percorso relativo di DataA.js |
    
    Regole:
    
    - la cartella `js` è interna alla root del sito;
    - il formato `.htm` o `.php` non deve essere dedotto genericamente;
    - il formato deve derivare dalla configurazione della stagione;
    - il link online e il link locale devono essere costruiti separatamente;
    - non bisogna concatenare la root della stagione corrente con quella storica;
    - gli eventuali DataA.js storici possono essere copiati e normalizzati nel progetto.
    
    ## 7. Squadra stagionale
    
    Struttura concettuale:
    
        {
          "seasonTeamId": "2025_2026:team:7",
          "seasonId": "2025_2026",
          "sourceTeamId": "7",
          "seasonName": "River Pino F.C.",
          "shortName": "River Pino",
          "canonicalTeamId": "river-pino",
          "associationStatus": "MAPPED",
          "isCurrent": true
        }
    
    Campi minimi:
    
    | Campo | Significato |
    |---|---|
    | seasonTeamId | ID univoco nella stagione |
    | seasonId | Stagione |
    | sourceTeamId | ID originale FCM/FCA |
    | seasonName | Nome usato nella stagione |
    | shortName | Nome abbreviato |
    | canonicalTeamId | Identità storica |
    | associationStatus | Stato associazione |
    | isCurrent | Squadra presente nell'ultimo FCM |
    
    Stati associazione:
    
    - MAPPED;
    - UNMAPPED;
    - AMBIGUOUS;
    - MANUAL;
    - EXCLUDED.
    
    ## 8. Squadra canonica
    
    Struttura concettuale:
    
        {
          "canonicalTeamId": "river-pino",
          "canonicalName": "River Pino",
          "currentSeasonTeamId": "2025_2026:team:7",
          "active": true,
          "aliases": [
            "River Pino F.C.",
            "River Pino"
          ]
        }
    
    Regole:
    
    - il canonicalTeamId deve essere stabile;
    - il nome canonico è distinto dal nome storico;
    - l'associazione parte dalla squadra attuale;
    - le squadre delle stagioni precedenti vengono collegate alla squadra attuale;
    - gli aggregati globali usano il canonicalTeamId;
    - le viste stagionali possono mostrare il seasonName.
    
    ## 9. Competizione stagionale
    
    Struttura concettuale:
    
        {
          "seasonCompetitionId": "2025_2026:competition:1",
          "seasonId": "2025_2026",
          "sourceCompetitionId": "1",
          "seasonName": "Serie A",
          "canonicalCompetitionId": "serie-a",
          "associationStatus": "MAPPED",
          "competitionType": "LEAGUE",
          "includedByDefault": true
        }
    
    Tipi preliminari:
    
    - LEAGUE;
    - CUP;
    - SUPERCUP;
    - PLAYOFF;
    - PLAYOUT;
    - OTHER.
    
    Regole:
    
    - non tutte le stagioni hanno le stesse competizioni;
    - una competizione assente non deve essere inventata;
    - Play Off e Play Out restano fuori dai gruppi principali;
    - competizioni presenti in una sola stagione sono ammesse;
    - i gruppi predefiniti devono rispettare l'ordine canonico già stabilito nel progetto storico.
    
    ## 10. Competizione canonica
    
    Struttura concettuale:
    
        {
          "canonicalCompetitionId": "europa-pipps",
          "canonicalName": "Europa Pipps",
          "aliases": [
            "Coppa tra le Pippe",
            "Europa Pipps"
          ],
          "active": true
        }
    
    Regole:
    
    - competizioni con nomi diversi possono condividere la stessa identità canonica;
    - il nome originale della stagione deve essere conservato;
    - gli aggregati storici per competizione usano il canonicalCompetitionId.
    
    ## 11. Giornata
    
    Struttura concettuale:
    
        {
          "roundId": "2025_2026:serie-a:18",
          "seasonId": "2025_2026",
          "seasonCompetitionId": "2025_2026:competition:1",
          "roundNumber": 18,
          "competitionRoundNumber": 16,
          "matchDate": "2026-01-11",
          "dateSource": "DataA.js"
        }
    
    Regole:
    
    - la data va letta da DataA.js quando disponibile;
    - devono essere distinti numero giornata reale e numero giornata della competizione, se differenti;
    - la data deve essere normalizzata in formato ISO.
    
    ## 12. Partita normalizzata
    
    La partita è l'entità centrale del modello.
    
    Struttura concettuale:
    
        {
          "matchId": "2025_2026:competition:1:round:18:match:4",
          "seasonId": "2025_2026",
          "seasonCompetitionId": "2025_2026:competition:1",
          "canonicalCompetitionId": "serie-a",
          "roundId": "2025_2026:serie-a:18",
          "roundNumber": 18,
          "matchDate": "2026-01-11",
          "homeSeasonTeamId": "2025_2026:team:7",
          "homeCanonicalTeamId": "river-pino",
          "awaySeasonTeamId": "2025_2026:team:3",
          "awayCanonicalTeamId": "fc-squirt-game",
          "homeScore": 63.0,
          "awayScore": 68.0,
          "homeGoals": 0,
          "awayGoals": 1,
          "homeRegulationGoals": 0,
          "awayRegulationGoals": 1,
          "result": "AWAY_WIN",
          "isNeutral": false,
          "localMatchUrl": "file:///E:/fantacalcio/Lega2025/ris.htm?Gio=18",
          "onlineMatchUrl": "http://www.alterlega.altervista.org/lega2025/ris.htm?Gio=18"
        }
    
    Campi principali:
    
    | Gruppo | Campi |
    |---|---|
    | Identità | matchId, seasonId, competitionId, roundId |
    | Squadre | homeSeasonTeamId, awaySeasonTeamId |
    | Identità canoniche | homeCanonicalTeamId, awayCanonicalTeamId |
    | Punteggi | homeScore, awayScore |
    | Gol | homeGoals, awayGoals |
    | Gol regolamentari | homeRegulationGoals, awayRegulationGoals |
    | Esito | result |
    | Campo | isNeutral |
    | Data | matchDate |
    | Tabellini | localMatchUrl, onlineMatchUrl |
    
    Esiti ammessi:
    
    - HOME_WIN;
    - DRAW;
    - AWAY_WIN;
    - NOT_PLAYED;
    - CANCELLED;
    - UNKNOWN.
    
    ## 13. Partecipazione squadra alla partita
    
    Per semplificare aggregati e record personali, ogni partita produce due righe squadra-partita.
    
    Struttura concettuale:
    
        {
          "matchTeamId": "2025_2026:competition:1:round:18:match:4:team:7",
          "matchId": "2025_2026:competition:1:round:18:match:4",
          "seasonTeamId": "2025_2026:team:7",
          "canonicalTeamId": "river-pino",
          "side": "HOME",
          "scoreFor": 63.0,
          "scoreAgainst": 68.0,
          "goalsFor": 0,
          "goalsAgainst": 1,
          "regulationGoalsFor": 0,
          "regulationGoalsAgainst": 1,
          "standingsPoints": 0,
          "result": "LOSS"
        }
    
    Vantaggi:
    
    - aggregazioni più semplici;
    - filtri per squadra;
    - record personali;
    - medie;
    - serie;
    - casa e trasferta;
    - statistiche fatte e subite.
    
    ## 14. Link ai tabellini
    
    Ogni record riferito a una partita specifica deve conservare almeno:
    
    - matchId;
    - localMatchUrl;
    - onlineMatchUrl.
    
    Tipi di collegamento:
    
    - SINGLE_MATCH;
    - MATCH_RANGE;
    - MULTIPLE_MATCHES;
    - NOT_APPLICABLE.
    
    Esempi:
    
    - maggior punteggio: SINGLE_MATCH;
    - partita con più gol regolamentari: SINGLE_MATCH;
    - serie positiva: MATCH_RANGE o MULTIPLE_MATCHES;
    - totale punti stagionale: NOT_APPLICABLE;
    - sequenza storica: MULTIPLE_MATCHES.
    
    Per le serie devono essere conservati:
    
    - startMatchId;
    - endMatchId;
    - matchIds;
    - startMatchUrl;
    - endMatchUrl.
    
    ## 15. Giocatore
    
    Struttura concettuale:
    
        {
          "playerId": "player:12345",
          "sourcePlayerId": "12345",
          "displayName": "Mario Rossi",
          "role": "D",
          "active": true
        }
    
    Ruoli preliminari:
    
    - P;
    - D;
    - C;
    - A;
    - UNKNOWN.
    
    L'identità storica del giocatore sarà approfondita solo se necessaria per aggregati pluristagionali affidabili.
    
    ## 16. Presenza giocatore
    
    Struttura concettuale:
    
        {
          "appearanceId": "match:xyz:team:7:player:12345",
          "matchId": "match:xyz",
          "seasonTeamId": "2025_2026:team:7",
          "playerId": "player:12345",
          "lineupType": "STARTER",
          "role": "D",
          "rawVote": 6.5,
          "fantasyVote": 7.0,
          "played": true
        }
    
    Tipi presenza:
    
    - STARTER;
    - SUBSTITUTE;
    - BENCH;
    - OFFICE_PLAYER;
    - NOT_USED.
    
    ## 17. Evento giocatore
    
    Struttura concettuale:
    
        {
          "playerEventId": "event:match:xyz:player:12345:yellow-card:1",
          "matchId": "match:xyz",
          "seasonTeamId": "2025_2026:team:7",
          "playerId": "player:12345",
          "eventType": "YELLOW_CARD",
          "value": 1
        }
    
    Tipi preliminari:
    
    - GOAL;
    - ASSIST;
    - OWN_GOAL;
    - YELLOW_CARD;
    - SECOND_YELLOW;
    - RED_CARD;
    - PENALTY_SCORED;
    - PENALTY_MISSED;
    - PENALTY_SAVED;
    - CLEAN_SHEET;
    - CAPTAIN_BONUS;
    - DEFENCE_MODIFIER_COMPONENT;
    - OTHER.
    
    ## 18. Modificatore
    
    Struttura concettuale:
    
        {
          "modifierEventId": "modifier:match:xyz:team:7:defence",
          "matchId": "match:xyz",
          "seasonTeamId": "2025_2026:team:7",
          "modifierType": "DEFENCE",
          "rawValue": 2.0,
          "appliedValue": 2.0,
          "enabled": true,
          "source": "FCM"
        }
    
    Tipi iniziali:
    
    - DEFENCE;
    - CAPTAIN;
    - HOME_FIELD;
    - CUSTOM.
    
    Ogni modificatore può essere elaborato o escluso indipendentemente.
    
    ## 19. Fattore Campo
    
    Il Fattore Campo appartiene alla famiglia Modificatori.
    
    Struttura concettuale:
    
        {
          "homeFieldEventId": "homefield:match:xyz",
          "matchId": "match:xyz",
          "homeBonus": 2.0,
          "realHomeScore": 72.0,
          "realAwayScore": 71.0,
          "simulatedHomeScoreWithoutBonus": 70.0,
          "realResult": "HOME_WIN",
          "simulatedResultWithoutBonus": "DRAW",
          "decisive": true,
          "standingsPointsDeltaHome": 2,
          "standingsPointsDeltaAway": -1
        }
    
    ## 20. Riserva d'Ufficio
    
    Struttura concettuale:
    
        {
          "officePlayerEventId": "office:match:xyz:team:7:role:A:1",
          "matchId": "match:xyz",
          "seasonTeamId": "2025_2026:team:7",
          "officeType": "AU",
          "role": "A",
          "count": 1,
          "scoreImpact": 1.5,
          "decisive": false
        }
    
    Tipi:
    
    - PU;
    - DU;
    - CU;
    - AU.
    
    Le elaborazioni decisive richiedono una simulazione dedicata.
    
    I semplici conteggi non dipendono dalla simulazione.
    
    ## 21. Evento soglia
    
    Struttura concettuale:
    
        {
          "thresholdEventId": "threshold:match:xyz:team:7:surgical-win",
          "matchId": "match:xyz",
          "seasonTeamId": "2025_2026:team:7",
          "thresholdEventType": "SURGICAL_WIN",
          "distanceToThreshold": 0.5,
          "resultImpact": "FAVOURABLE"
        }
    
    Tipi iniziali:
    
    - SURGICAL_WIN;
    - MOCKING_LOSS;
    - MIRACULOUS_DRAW;
    - NARROW_DRAW;
    - MISSED_WIN_HALF_POINT;
    - LOSS_BY_A_WHISKER;
    - EXACT_THRESHOLD;
    - JUST_ENOUGH;
    - WASTED_POINTS.
    
    ## 22. Evento fortuna-sfortuna
    
    Struttura concettuale:
    
        {
          "luckEventId": "luck:match:xyz:team:7:1",
          "matchId": "match:xyz",
          "seasonTeamId": "2025_2026:team:7",
          "canonicalTeamId": "river-pino",
          "direction": "FAVOURABLE",
          "component": "SURGICAL_WIN",
          "weight": 1.0,
          "normalizedValue": 0.125
        }
    
    Direzioni:
    
    - FAVOURABLE;
    - UNFAVOURABLE;
    - NEUTRAL.
    
    ## 23. Culometro
    
    Il Culometro non fa parte dell'elaborazione ordinaria automatica.
    
    Struttura concettuale della configurazione:
    
        {
          "enabled": true,
          "minimumMatches": 10,
          "normalization": "PER_MATCH",
          "components": [
            {
              "componentId": "SURGICAL_WIN",
              "enabled": true,
              "weight": 1.0
            },
            {
              "componentId": "HOME_FIELD_DECISIVE",
              "enabled": false,
              "weight": 1.5
            }
          ]
        }
    
    Regole:
    
    - viene generato solo su richiesta esplicita;
    - richiede configurazione dedicata;
    - i pesi devono essere dichiarati;
    - le componenti mancanti devono essere segnalate;
    - la mancata generazione non rende incompleta Soglie e Fortuna;
    - l'output dedicato è separato.
    
    ## 24. Serie
    
    Struttura concettuale:
    
        {
          "seriesId": "series:unbeaten:river-pino:2024_2025:1",
          "seriesType": "UNBEATEN",
          "canonicalTeamId": "river-pino",
          "seasonTeamId": "2024_2025:team:4",
          "scope": "SEASON",
          "length": 12,
          "startMatchId": "match:a",
          "endMatchId": "match:l",
          "matchIds": [
            "match:a",
            "match:b",
            "match:l"
          ],
          "crossesSeasonBoundary": false
        }
    
    Ambiti:
    
    - SEASON;
    - ABSOLUTE;
    - HISTORICAL_CONTINUOUS.
    
    Ogni tipo di serie deve dichiarare se può attraversare il confine stagionale.
    
    ## 25. Aggregato stagionale
    
    Struttura concettuale:
    
        {
          "aggregateId": "aggregate:2025_2026:river-pino:serie-a:total-points",
          "metricId": "classics.total-points",
          "seasonId": "2025_2026",
          "canonicalTeamId": "river-pino",
          "seasonTeamId": "2025_2026:team:7",
          "canonicalCompetitionId": "serie-a",
          "seasonCompetitionId": "2025_2026:competition:1",
          "matches": 36,
          "total": 2615.5,
          "average": 72.6528,
          "percentage": null
        }
    
    Ogni aggregato deve conservare, quando applicabili:
    
    - numero partite;
    - totale;
    - media;
    - percentuale;
    - minimo;
    - massimo.
    
    ## 26. Aggregato globale
    
    Struttura concettuale:
    
        {
          "aggregateId": "global:river-pino:all:total-points",
          "metricId": "classics.total-points",
          "canonicalTeamId": "river-pino",
          "canonicalCompetitionId": null,
          "seasons": 20,
          "matches": 712,
          "total": 51428.5,
          "average": 72.2310
        }
    
    Il globale somma o media tutte le stagioni della stessa identità canonica.
    
    Non va confuso con l'assoluto.
    
    ## 27. Occorrenza assoluta
    
    Struttura concettuale:
    
        {
          "absoluteId": "absolute:classics.total-points:river-pino",
          "metricId": "classics.total-points",
          "canonicalTeamId": "river-pino",
          "bestSeasonId": "2021_2022",
          "bestSeasonTeamId": "2021_2022:team:5",
          "value": 2680.0,
          "matches": 38,
          "average": 70.5263
        }
    
    L'assoluto individua la migliore o peggiore occorrenza fra stagioni.
    
    Non è la somma della carriera.
    
    ## 28. Ex aequo
    
    Regola preliminare:
    
    - i dati conservano tutte le occorrenze;
    - la vista decide quante posizioni mostrare;
    - gli ex aequo non devono essere eliminati durante l'esportazione;
    - l'ordinamento secondario deve essere dichiarato per ogni figlio;
    - in assenza di criterio specifico, usare:
      1. valore principale;
      2. numero partite;
      3. media;
      4. data;
      5. nome visualizzato.
    
    La regola definitiva resta da catalogare figlio per figlio.
    
    ## 29. Dipendenza
    
    Struttura concettuale:
    
        {
          "childId": "series.captain-bonus",
          "required": [
            "ordered_matches",
            "modifier.captain"
          ],
          "optional": [],
          "missingBehaviour": "SKIP_CHILD"
        }
    
    Tipi di dipendenza:
    
    - DATA;
    - MODULE;
    - CONFIGURATION;
    - FAMILY_CHILD;
    - SIMULATION;
    - SITE;
    - MAPPING.
    
    ## 30. Stato output
    
    Stati ammessi:
    
    - GENERATED_COMPLETE;
    - GENERATED_PARTIAL;
    - SKIPPED_REQUIRED_DEPENDENCY;
    - SKIPPED_NOT_SELECTED;
    - SKIPPED_NO_DATA;
    - FAILED.
    
    Struttura concettuale:
    
        {
          "childId": "series.captain-bonus",
          "status": "SKIPPED_REQUIRED_DEPENDENCY",
          "missingDependencies": [
            "modifier.captain"
          ],
          "message": "Modificatore Capitano non elaborato."
        }
    
    ## 31. Manifest generale
    
    Struttura concettuale:
    
        {
          "program": "RecordsNext by mauz79",
          "programVersion": "2.0.0-dev",
          "schemaVersion": "2.0",
          "generatedAt": "2026-08-05T14:50:00+02:00",
          "leagueId": "alterlega",
          "currentSeasonId": "2025_2026",
          "processedSeasons": [],
          "requestedFamilies": [],
          "generatedFamilies": [],
          "generatedChildren": [],
          "skippedChildren": [],
          "generatedFiles": [],
          "culometroGenerated": false
        }
    
    Il manifest deve descrivere ciò che è stato realmente elaborato.
    
    ## 32. Output JavaScript
    
    Output preliminari:
    
    - `fcmRecordsNext_Core.js`
    - `fcmRecordsNext_Manifest.js`
    - `fcmRecordsNext_Classics.js`
    - `fcmRecordsNext_Series.js`
    - `fcmRecordsNext_RU.js`
    - `fcmRecordsNext_Modifiers.js`
    - `fcmRecordsNext_ThresholdsLuck.js`
    - `fcmRecordsNext_Culometro.js`
    
    Il Culometro viene scritto solo se richiesto.
    
    ## 33. Struttura comune di una famiglia
    
    Schema concettuale:
    
        window.fcmRecordsNextClassics = {
            schemaVersion: "2.0",
            familyId: "classics",
            metadata: {},
            events: [],
            seasonAggregates: [],
            globalAggregates: [],
            absoluteOccurrences: [],
            outputStatus: []
        };
    
    Ogni famiglia può omettere sezioni non pertinenti, ma deve rispettare lo schema comune di metadata e stato.
    
    ## 34. Cartelle pubbliche
    
    Sul sito FCM:
    
    - tutti i JS pubblici vanno nella cartella `js`;
    - nella root va un solo HTML indice;
    - viste e asset vanno nella cartella `RecordsNext`;
    - i file intermedi non devono essere pubblicati.
    
    ## 35. Dati intermedi
    
    I dati intermedi possono risiedere nel progetto:
    
        D:\DEV_APPS\RecordsNext2.0\work
    
    Possibili sottocartelle future:
    
    - `work\normalized`
    - `work\seasons`
    - `work\cache`
    - `work\exports`
    - `work\diagnostics`
    
    Questi file non sono necessariamente pubblici.
    
    ## 36. Validazione
    
    Ogni entità deve poter essere validata.
    
    Controlli minimi:
    
    - ID obbligatori non nulli;
    - stagione esistente;
    - squadra stagionale esistente;
    - squadra canonica associata o esplicitamente non associata;
    - competizione esistente;
    - partita con due squadre differenti;
    - punteggi numerici;
    - link coerenti con la configurazione della stagione;
    - data valida;
    - dipendenze note;
    - stato output ammesso.
    
    ## 37. Test minimi del modello
    
    Il modello deve essere testato almeno su:
    
    1. stagione corrente gestita;
    2. stagione storica gestita;
    3. stagione manuale;
    4. squadra che cambia nome;
    5. competizione che cambia nome;
    6. competizione assente in alcune stagioni;
    7. partita con link `.htm`;
    8. partita con link `.php`;
    9. serie che attraversa una stagione;
    10. modificatore Capitano non selezionato;
    11. RU presente senza simulazione decisiva;
    12. Culometro non richiesto;
    13. Culometro richiesto con configurazione;
    14. record personale di una squadra non prima in classifica;
    15. ex aequo.
    
    ## 38. Decisioni consolidate
    
    Sono consolidate:
    
    - progetto separato RecordsNext2.0;
    - cinque famiglie;
    - output modulari;
    - dati completi e filtrabili;
    - identità canoniche di squadre e competizioni;
    - link ai tabellini per record di partita;
    - dipendenze a livello di figlio;
    - Fattore Campo nei Modificatori;
    - Culometro opzionale;
    - stagioni gestite e manuali;
    - JS pubblici nella cartella `js`;
    - un solo HTML indice nella root;
    - viste in cartella `RecordsNext`.
    
    ## 39. Decisioni ancora aperte
    
    Restano da definire:
    
    - struttura fisica definitiva dei file di configurazione;
    - tecnologia del motore 2.0;
    - formato finale degli ID;
    - schema JSON formalizzato;
    - catalogo definitivo dei figli;
    - regole precise degli ex aequo;
    - soglie minime per medie e percentuali;
    - aggregazione fra gruppi di competizioni;
    - identità storica dei giocatori;
    - formato finale della configurazione Culometro;
    - struttura definitiva della GUI;
    - strategia di migrazione o confronto con RecordsNext 1.0.2.

## Configurazione

File: docs\CONFIGURAZIONE_RECORDSNEXT2.md

    # Configurazione RecordsNext 2.0
    
    ## Scopo
    
    Questi file costituiscono la prima configurazione concreta del progetto.
    
    ## File
    
    - `config/league.json`: identità generale della lega.
    - `config/seasons.json`: stagioni gestite, manuali e correnti.
    - `config/teams.json`: squadre canoniche e squadre stagionali.
    - `config/competitions.json`: competizioni canoniche e stagionali.
    - `config/processing.json`: famiglie e figli richiesti.
    - `config/culometro.json`: configurazione separata dell'easter egg.
    - `config/manifest.example.json`: forma preliminare del manifest prodotto.
    
    ## Regole consolidate
    
    - Il file FCM e il file FCA sono configurati per stagione.
    - La cartella `js` non viene configurata separatamente: è interna alla root del sito.
    - Ogni stagione può avere sito locale e online.
    - Il nome della pagina tabellino è configurato per stagione.
    - Squadre e competizioni conservano identità stagionale e canonica.
    - Le famiglie possono essere elaborate separatamente.
    - Ogni figlio può dipendere da altri dati o moduli.
    - Il Capitano può essere disattivato senza bloccare le altre Serie.
    - Il Culometro è disattivato per impostazione predefinita.
    - Il Culometro richiede configurazione esplicita.
    - Le competizioni canoniche rispettano l'ordine stabilito.
    - Play Off e Play Out non appartengono all'ordine principale.
    
    ## Nota importante
    
    I valori presenti sono iniziali o di esempio. Prima dell'elaborazione reale sarà necessario importare e verificare:
    
    - tutte le stagioni;
    - i percorsi FCM/FCA;
    - le root locali e online;
    - il formato del tabellino di ogni stagione;
    - i mapping delle squadre;
    - i mapping delle competizioni.

## Stato implementazione

File: docs\STATO_IMPLEMENTAZIONE_RECORDSNEXT2.md

    # Stato implementazione RecordsNext 2.0
    
    Aggiornamento: 5 agosto 2026.
    
    ## Base tecnica verificata
    
    RecordsNext 2.0 nasce dal codice funzionante di RecordsNext 1.0.2.
    
    Sono stati importati e mantenuti:
    
    - progetto Maven e Maven Wrapper;
    - Java 21;
    - accesso FCM/FCA tramite UCanAccess;
    - importazione raw in SQLite;
    - configurazione delle stagioni;
    - stagioni gestite e manuali;
    - associazioni canoniche di squadre e competizioni;
    - viste canoniche;
    - normalizzazione stagionale;
    - consolidamento;
    - cache di normalizzazione;
    - esportatori Classici e Riserve d'Ufficio esistenti;
    - staging, validazione, pubblicazione e rollback;
    - GUI esistente come base da evolvere.
    
    UCanAccess 2.0.9.5 resta candidato obbligatorio nei benchmark comparativi.
    
    ## Modello modulare implementato
    
    Sono state introdotte le strutture Java per:
    
    - cinque famiglie elaborabili;
    - figli di famiglia;
    - dipendenze obbligatorie e opzionali;
    - stati degli output;
    - selezione modulare;
    - catalogo iniziale;
    - valutazione delle dipendenze.
    
    Famiglie:
    
    1. Classici;
    2. Serie;
    3. Riserve d'Ufficio;
    4. Modificatori;
    5. Soglie e Fortuna.
    
    Il Culometro resta un easter egg opzionale, non selezionato automaticamente.
    
    ## Compatibilità con la pipeline 1.0.2
    
    ProcessingOptions e RecordsNextPipeline sono stati estesi mantenendo la compatibilità con il costruttore legacy basato su:
    
    - Classici;
    - Riserve d'Ufficio;
    - generazione JavaScript;
    - pubblicazione.
    
    La pipeline usa internamente la selezione modulare.
    
    Le famiglie non ancora dotate di elaboratore non vengono ignorate silenziosamente.
    
    ## Planner e preflight
    
    Sono implementati:
    
    - inventario delle capacità disponibili;
    - piano di esecuzione;
    - valutazione preventiva delle dipendenze;
    - conteggio di figli completi, parziali e saltati;
    - preflight eseguibile prima della produzione degli archivi e degli output.
    
    La mancanza di una dipendenza blocca soltanto il figlio interessato.
    
    Esempio consolidato:
    
    - Capitano disattivato;
    - output del Capitano saltati;
    - serie dipendenti dal Capitano saltate;
    - tutte le altre serie e famiglie continuano.
    
    ## Configurazione modulare
    
    La configurazione processing.json viene letta dal codice Java.
    
    Sono supportati:
    
    - selezione delle cinque famiglie;
    - selezione dei figli dei Modificatori;
    - Capitano disattivabile;
    - Culometro abilitabile solo esplicitamente;
    - generazione JavaScript;
    - pubblicazione.
    
    Il percorso legacy della GUI resta disponibile durante la migrazione.
    
    ## Manifest 2.0
    
    E' implementato il writer di:
    
    fcmRecordsNext_Manifest.js
    
    Il manifest contiene almeno:
    
    - versione programma e schema;
    - lega;
    - stagione corrente;
    - stagioni elaborate;
    - famiglie richieste;
    - famiglie eseguibili;
    - figli generati;
    - figli saltati;
    - dipendenze mancanti;
    - file prodotti;
    - stato Culometro;
    - riepilogo preflight.
    
    Il manifest viene:
    
    - generato nella staging;
    - validato;
    - incluso nel conteggio dei file;
    - pubblicato insieme agli altri JavaScript;
    - protetto dal rollback transazionale esistente.
    
    ## Test
    
    Ultimo stato verificato dall'utente:
    
    - 22 test eseguiti;
    - 0 failure;
    - 0 errori;
    - BUILD SUCCESS.
    
    ## Non ancora implementato
    
    Non sono ancora implementati come nuovi elaboratori nativi 2.0:
    
    - Serie;
    - Modificatori;
    - Soglie e Fortuna;
    - Culometro;
    - nuovo output Classici completo secondo lo schema 2.0;
    - nuovo output RU completo secondo lo schema 2.0;
    - fcmRecordsNext_Core.js;
    - GUI definitiva a cinque famiglie;
    - viste HTML 2.0 definitive;
    - installer 2.0.
    
    Gli esportatori Classici e RU della 1.0.2 restano operativi come ponte.
    
    ## Regole da non perdere
    
    - Non riscrivere accesso FCM/FCA, mapping, normalizzazione e consolidamento senza una necessità verificata.
    - Basarsi sul codice reale di RecordsNext 1.0.2.
    - Tutti i JavaScript pubblici vanno nella cartella js del sito FCM.
    - Nella root del sito deve esserci un solo HTML indice.
    - Le viste e gli asset vanno nella cartella RecordsNext.
    - Ogni record riferito a una partita specifica conserva il link al tabellino.
    - Squadre e competizioni conservano identità stagionale e canonica.
    - Gli output devono essere viste dati complete, non top list tagliate.
    - Gli ZIP temporanei applicati, testati e committati vanno eliminati periodicamente da D:\DEV_APPS\downloads.

## Changelog

File: CHANGELOG.md

    # Changelog
    
    ## 2.0.0-dev
    
    ### Fondazione
    
    - creato progetto separato RecordsNext2.0;
    - importata la base funzionante RecordsNext 1.0.2;
    - confermato accesso FCM/FCA tramite UCanAccess;
    - mantenuti SQLite, mapping canonici, normalizzazione, consolidamento e pubblicazione.
    
    ### Architettura modulare
    
    - definite cinque famiglie elaborabili;
    - introdotti figli, dipendenze e stati degli output;
    - introdotta selezione modulare;
    - mantenuta compatibilita temporanea con Classici e RU legacy;
    - introdotti planner e preflight;
    - collegato config\processing.json alla pipeline.
    
    ### Manifest
    
    - introdotto fcmRecordsNext_Manifest.js;
    - integrata generazione nella staging;
    - integrata validazione;
    - integrata pubblicazione con rollback.
    
    ### Verifica
    
    - ultimo stato: 22 test superati, BUILD SUCCESS.

## File reali del progetto

## src\main\java\it\alterlega\recordsnext\app\config\ConfiguredPipelineRunner.java

File: src\main\java\it\alterlega\recordsnext\app\config\ConfiguredPipelineRunner.java

    package it.alterlega.recordsnext.app.config;
    
    import it.alterlega.recordsnext.app.PipelineConfig;
    import it.alterlega.recordsnext.app.ProcessingMode;
    import it.alterlega.recordsnext.app.RecordsNextPipeline;
    
    import java.nio.file.Path;
    
    public final class ConfiguredPipelineRunner {
        private ConfiguredPipelineRunner() {}
    
        public static RecordsNextPipeline.Result run(
                PipelineConfig pipelineConfig,
                Path processingConfig,
                ProcessingMode mode,
                RecordsNextPipeline.Listener listener
        ) throws Exception {
            return new RecordsNextPipeline().run(
                    pipelineConfig,
                    ProcessingConfigLoader.load(processingConfig),
                    mode,
                    listener
            );
        }
    }

## src\main\java\it\alterlega\recordsnext\app\config\MiniJson.java

File: src\main\java\it\alterlega\recordsnext\app\config\MiniJson.java

    package it.alterlega.recordsnext.app.config;
    
    import java.util.ArrayList;
    import java.util.LinkedHashMap;
    import java.util.List;
    import java.util.Map;
    
    final class MiniJson {
        private final String text;
        private int pos;
    
        private MiniJson(String text) {
            this.text = text != null && text.startsWith("\uFEFF") ? text.substring(1) : text;
        }
    
        static Object parse(String text) {
            MiniJson parser = new MiniJson(text);
            Object value = parser.readValue();
            parser.skipWhitespace();
            if (parser.pos != parser.text.length()) {
                throw parser.error("Contenuto inatteso dopo il valore JSON");
            }
            return value;
        }
    
        private Object readValue() {
            skipWhitespace();
            if (pos >= text.length()) throw error("Valore JSON mancante");
            return switch (text.charAt(pos)) {
                case '{' -> readObject();
                case '[' -> readArray();
                case '"' -> readString();
                case 't' -> readLiteral("true", Boolean.TRUE);
                case 'f' -> readLiteral("false", Boolean.FALSE);
                case 'n' -> readLiteral("null", null);
                default -> readNumber();
            };
        }
    
        private Map<String, Object> readObject() {
            expect('{');
            Map<String, Object> result = new LinkedHashMap<>();
            skipWhitespace();
            if (peek('}')) { pos++; return result; }
            while (true) {
                skipWhitespace();
                String key = readString();
                skipWhitespace();
                expect(':');
                result.put(key, readValue());
                skipWhitespace();
                if (peek('}')) { pos++; return result; }
                expect(',');
            }
        }
    
        private List<Object> readArray() {
            expect('[');
            List<Object> result = new ArrayList<>();
            skipWhitespace();
            if (peek(']')) { pos++; return result; }
            while (true) {
                result.add(readValue());
                skipWhitespace();
                if (peek(']')) { pos++; return result; }
                expect(',');
            }
        }
    
        private String readString() {
            expect('"');
            StringBuilder out = new StringBuilder();
            while (pos < text.length()) {
                char c = text.charAt(pos++);
                if (c == '"') return out.toString();
                if (c != '\\') { out.append(c); continue; }
                if (pos >= text.length()) throw error("Escape JSON incompleto");
                char e = text.charAt(pos++);
                switch (e) {
                    case '"', '\\', '/' -> out.append(e);
                    case 'b' -> out.append('\b');
                    case 'f' -> out.append('\f');
                    case 'n' -> out.append('\n');
                    case 'r' -> out.append('\r');
                    case 't' -> out.append('\t');
                    case 'u' -> {
                        if (pos + 4 > text.length()) throw error("Escape Unicode incompleto");
                        out.append((char) Integer.parseInt(text.substring(pos, pos + 4), 16));
                        pos += 4;
                    }
                    default -> throw error("Escape JSON non valido: \\" + e);
                }
            }
            throw error("Stringa JSON non chiusa");
        }
    
        private Object readNumber() {
            int start = pos;
            while (pos < text.length() && "-+0123456789.eE".indexOf(text.charAt(pos)) >= 0) pos++;
            if (start == pos) throw error("Valore JSON non valido");
            String raw = text.substring(start, pos);
            try {
                return raw.contains(".") || raw.contains("e") || raw.contains("E")
                        ? Double.valueOf(raw) : Long.valueOf(raw);
            } catch (NumberFormatException ex) {
                throw error("Numero JSON non valido: " + raw);
            }
        }
    
        private Object readLiteral(String literal, Object value) {
            if (!text.startsWith(literal, pos)) throw error("Valore JSON non valido");
            pos += literal.length();
            return value;
        }
    
        private void skipWhitespace() {
            while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) pos++;
        }
    
        private void expect(char expected) {
            skipWhitespace();
            if (pos >= text.length() || text.charAt(pos) != expected) {
                throw error("Atteso '" + expected + "'");
            }
            pos++;
        }
    
        private boolean peek(char c) {
            return pos < text.length() && text.charAt(pos) == c;
        }
    
        private IllegalArgumentException error(String message) {
            return new IllegalArgumentException(message + " alla posizione " + pos);
        }
    }

## src\main\java\it\alterlega\recordsnext\app\config\ProcessingConfigLoader.java

File: src\main\java\it\alterlega\recordsnext\app\config\ProcessingConfigLoader.java

    package it.alterlega.recordsnext.app.config;
    
    import it.alterlega.recordsnext.app.ProcessingOptions;
    import it.alterlega.recordsnext.app.model.CoreRecordCatalog;
    import it.alterlega.recordsnext.app.model.ProcessingSelection;
    import it.alterlega.recordsnext.app.model.RecordFamily;
    
    import java.io.IOException;
    import java.nio.charset.StandardCharsets;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.util.EnumSet;
    import java.util.LinkedHashSet;
    import java.util.Map;
    import java.util.Set;
    
    public final class ProcessingConfigLoader {
        private ProcessingConfigLoader() {}
    
        public static ProcessingOptions load(Path file) throws IOException {
            Object parsed = MiniJson.parse(Files.readString(file, StandardCharsets.UTF_8));
            Map<String, Object> root = object(parsed, "root");
            String schema = string(root.get("schemaVersion"), "schemaVersion");
            if (!"2.0".equals(schema)) {
                throw new IllegalArgumentException("Versione schema processing non supportata: " + schema);
            }
    
            Map<String, Object> processing = object(root.get("processing"), "processing");
            Map<String, Object> familiesNode = object(processing.get("families"), "processing.families");
            EnumSet<RecordFamily> families = EnumSet.noneOf(RecordFamily.class);
            Set<String> children = new LinkedHashSet<>();
    
            for (RecordFamily family : RecordFamily.values()) {
                Object raw = familiesNode.get(family.id());
                if (raw == null) continue;
                Map<String, Object> familyNode = object(raw, "family " + family.id());
                if (!bool(familyNode.get("enabled"), false)) continue;
                families.add(family);
                collectChildren(family, familyNode.get("children"), children);
            }
    
            Map<String, Object> culometroNode = object(processing.get("culometro"), "processing.culometro");
            boolean culometro = bool(culometroNode.get("enabled"), false);
            if (culometro) {
                families.add(RecordFamily.THRESHOLDS_LUCK);
                children.add(CoreRecordCatalog.CULOMETRO_ID);
            }
    
            Map<String, Object> output = object(processing.get("output"), "processing.output");
            boolean generateJs = bool(output.get("writeManifest"), true) || bool(output.get("writeCore"), true);
            boolean publish = bool(output.get("publishToSite"), false);
    
            ProcessingSelection selection = new ProcessingSelection(
                    Set.copyOf(families), Set.copyOf(children), culometro, generateJs, publish
            );
            return ProcessingOptions.modular(selection);
        }
    
        private static void collectChildren(RecordFamily family, Object node, Set<String> out) {
            if (node == null || "ALL".equalsIgnoreCase(String.valueOf(node))) return;
            Map<String, Object> children = object(node, "children of " + family.id());
            for (Map.Entry<String, Object> entry : children.entrySet()) {
                if (!bool(entry.getValue(), false)) continue;
                String id = switch (family) {
                    case MODIFIERS -> switch (entry.getKey()) {
                        case "defence" -> "modifiers.defence";
                        case "captain" -> "modifiers.captain";
                        case "homeField" -> "modifiers.home-field-deciding";
                        default -> "modifiers." + entry.getKey();
                    };
                    default -> family.id() + "." + entry.getKey();
                };
                out.add(id);
            }
        }
    
        @SuppressWarnings("unchecked")
        private static Map<String, Object> object(Object value, String name) {
            if (value instanceof Map<?, ?> map) return (Map<String, Object>) map;
            throw new IllegalArgumentException("Oggetto JSON mancante o non valido: " + name);
        }
    
        private static boolean bool(Object value, boolean defaultValue) {
            return value == null ? defaultValue : Boolean.TRUE.equals(value);
        }
    
        private static String string(Object value, String name) {
            if (value instanceof String s && !s.isBlank()) return s;
            throw new IllegalArgumentException("Stringa JSON mancante o non valida: " + name);
        }
    }

## src\main\java\it\alterlega\recordsnext\app\manifest\ManifestJsWriter.java

File: src\main\java\it\alterlega\recordsnext\app\manifest\ManifestJsWriter.java

    package it.alterlega.recordsnext.app.manifest;
    
    import it.alterlega.recordsnext.app.PipelinePreflight;
    import it.alterlega.recordsnext.app.ProcessingOptions;
    import it.alterlega.recordsnext.app.model.ExecutionPlanItem;
    import it.alterlega.recordsnext.app.model.RecordFamily;
    
    import java.io.IOException;
    import java.nio.charset.StandardCharsets;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.util.ArrayList;
    import java.util.Comparator;
    import java.util.List;
    import java.util.Objects;
    
    public final class ManifestJsWriter {
        public static final String FILE_NAME = "fcmRecordsNext_Manifest.js";
    
        private ManifestJsWriter() {
        }
    
        public static Path write(
                Path outputDirectory,
                ProcessingOptions options,
                PipelinePreflight.Result preflight,
                ManifestMetadata metadata
        ) throws IOException {
            Objects.requireNonNull(outputDirectory, "outputDirectory");
            Objects.requireNonNull(options, "options");
            Objects.requireNonNull(preflight, "preflight");
            Objects.requireNonNull(metadata, "metadata");
    
            Files.createDirectories(outputDirectory);
            Path outputFile = outputDirectory.resolve(FILE_NAME);
            Files.writeString(outputFile, render(options, preflight, metadata), StandardCharsets.UTF_8);
            return outputFile;
        }
    
        public static String render(
                ProcessingOptions options,
                PipelinePreflight.Result preflight,
                ManifestMetadata metadata
        ) {
            Objects.requireNonNull(options, "options");
            Objects.requireNonNull(preflight, "preflight");
            Objects.requireNonNull(metadata, "metadata");
    
            List<String> requestedFamilies = options.selection().enabledFamilies().stream()
                    .sorted(Comparator.comparing(RecordFamily::id))
                    .map(RecordFamily::id)
                    .toList();
    
            List<String> generatedFamilies = preflight.plan().executableItems().stream()
                    .map(item -> item.child().family())
                    .distinct()
                    .sorted(Comparator.comparing(RecordFamily::id))
                    .map(RecordFamily::id)
                    .toList();
    
            List<ExecutionPlanItem> selected = preflight.plan().selectedItems().stream()
                    .sorted(Comparator.comparing(item -> item.child().id()))
                    .toList();
    
            List<String> generatedChildren = selected.stream()
                    .filter(ExecutionPlanItem::executable)
                    .map(item -> item.child().id())
                    .toList();
    
            List<ExecutionPlanItem> skippedChildren = selected.stream()
                    .filter(item -> !item.executable())
                    .toList();
    
            StringBuilder out = new StringBuilder();
            out.append("window.fcmRecordsNextManifest = {\n");
            property(out, 1, "program", metadata.program(), true);
            property(out, 1, "programVersion", metadata.programVersion(), true);
            property(out, 1, "schemaVersion", metadata.schemaVersion(), true);
            property(out, 1, "generatedAt", metadata.generatedAt().toString(), true);
            property(out, 1, "leagueId", metadata.leagueId(), true);
            property(out, 1, "currentSeasonId", metadata.currentSeasonId(), true);
            stringArray(out, 1, "processedSeasons", metadata.processedSeasons(), true);
            stringArray(out, 1, "requestedFamilies", requestedFamilies, true);
            stringArray(out, 1, "generatedFamilies", generatedFamilies, true);
            stringArray(out, 1, "generatedChildren", generatedChildren, true);
            skippedArray(out, 1, skippedChildren, true);
            stringArray(out, 1, "generatedFiles", metadata.generatedFiles(), true);
            out.append("  culometroGenerated: ")
                    .append(options.culometroEnabled() && generatedChildren.contains("easter-egg.culometro"))
                    .append(",\n");
            out.append("  preflight: {\n");
            numberProperty(out, 2, "selected", preflight.selectedCount(), true);
            numberProperty(out, 2, "executable", preflight.executableCount(), true);
            numberProperty(out, 2, "complete", preflight.completeCount(), true);
            numberProperty(out, 2, "partial", preflight.partialCount(), true);
            numberProperty(out, 2, "skippedRequiredDependency", preflight.skippedDependencyCount(), false);
            out.append("  }\n");
            out.append("};\n");
            return out.toString();
        }
    
        private static void skippedArray(
                StringBuilder out,
                int indent,
                List<ExecutionPlanItem> items,
                boolean comma
        ) {
            pad(out, indent).append("skippedChildren: [");
            if (!items.isEmpty()) {
                out.append("\n");
                for (int i = 0; i < items.size(); i++) {
                    ExecutionPlanItem item = items.get(i);
                    pad(out, indent + 1).append("{\n");
                    property(out, indent + 2, "id", item.child().id(), true);
                    property(out, indent + 2, "status", item.status().name(), true);
                    stringArray(out, indent + 2, "missingRequired", sorted(item.missingRequired()), true);
                    stringArray(out, indent + 2, "missingOptional", sorted(item.missingOptional()), false);
                    pad(out, indent + 1).append("}");
                    if (i < items.size() - 1) out.append(",");
                    out.append("\n");
                }
                pad(out, indent);
            }
            out.append("]");
            if (comma) out.append(",");
            out.append("\n");
        }
    
        private static List<String> sorted(Iterable<String> values) {
            List<String> result = new ArrayList<>();
            for (String value : values) result.add(value);
            result.sort(String::compareTo);
            return result;
        }
    
        private static void property(StringBuilder out, int indent, String name, String value, boolean comma) {
            pad(out, indent).append(name).append(": \"").append(escape(value)).append("\"");
            if (comma) out.append(",");
            out.append("\n");
        }
    
        private static void numberProperty(StringBuilder out, int indent, String name, int value, boolean comma) {
            pad(out, indent).append(name).append(": ").append(value);
            if (comma) out.append(",");
            out.append("\n");
        }
    
        private static void stringArray(
                StringBuilder out,
                int indent,
                String name,
                List<String> values,
                boolean comma
        ) {
            pad(out, indent).append(name).append(": [");
            for (int i = 0; i < values.size(); i++) {
                if (i > 0) out.append(", ");
                out.append("\"").append(escape(values.get(i))).append("\"");
            }
            out.append("]");
            if (comma) out.append(",");
            out.append("\n");
        }
    
        private static StringBuilder pad(StringBuilder out, int indent) {
            return out.append("  ".repeat(Math.max(0, indent)));
        }
    
        private static String escape(String value) {
            String safe = value == null ? "" : value;
            return safe
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\r", "\\r")
                    .replace("\n", "\\n");
        }
    }

## src\main\java\it\alterlega\recordsnext\app\manifest\ManifestMetadata.java

File: src\main\java\it\alterlega\recordsnext\app\manifest\ManifestMetadata.java

    package it.alterlega.recordsnext.app.manifest;
    
    import java.time.OffsetDateTime;
    import java.util.List;
    import java.util.Objects;
    
    public record ManifestMetadata(
            String program,
            String programVersion,
            String schemaVersion,
            OffsetDateTime generatedAt,
            String leagueId,
            String currentSeasonId,
            List<String> processedSeasons,
            List<String> generatedFiles
    ) {
        public ManifestMetadata {
            program = required(program, "program");
            programVersion = required(programVersion, "programVersion");
            schemaVersion = required(schemaVersion, "schemaVersion");
            generatedAt = Objects.requireNonNull(generatedAt, "generatedAt");
            leagueId = optional(leagueId);
            currentSeasonId = optional(currentSeasonId);
            processedSeasons = List.copyOf(Objects.requireNonNullElse(processedSeasons, List.of()));
            generatedFiles = List.copyOf(Objects.requireNonNullElse(generatedFiles, List.of()));
        }
    
        public static ManifestMetadata minimal(String programVersion) {
            return new ManifestMetadata(
                    "RecordsNext by mauz79",
                    programVersion,
                    "2.0",
                    OffsetDateTime.now(),
                    "",
                    "",
                    List.of(),
                    List.of()
            );
        }
    
        private static String required(String value, String field) {
            Objects.requireNonNull(value, field);
            String normalized = value.trim();
            if (normalized.isEmpty()) {
                throw new IllegalArgumentException(field + " non puo essere vuoto");
            }
            return normalized;
        }
    
        private static String optional(String value) {
            return value == null ? "" : value.trim();
        }
    }

## src\main\java\it\alterlega\recordsnext\app\manifest\ManifestPublishingSupport.java

File: src\main\java\it\alterlega\recordsnext\app\manifest\ManifestPublishingSupport.java

    package it.alterlega.recordsnext.app.manifest;
    
    import it.alterlega.recordsnext.app.PipelinePreflight;
    import it.alterlega.recordsnext.app.ProcessingOptions;
    
    import java.io.IOException;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.util.ArrayList;
    import java.util.Comparator;
    import java.util.List;
    import java.util.Objects;
    
    public final class ManifestPublishingSupport {
        private ManifestPublishingSupport() {
        }
    
        public static Path write(
                Path generatedDirectory,
                ProcessingOptions options,
                PipelinePreflight.Result preflight,
                ManifestMetadata metadata
        ) throws IOException {
            Objects.requireNonNull(generatedDirectory, "generatedDirectory");
            Objects.requireNonNull(options, "options");
            Objects.requireNonNull(preflight, "preflight");
            Objects.requireNonNull(metadata, "metadata");
    
            List<String> generatedFiles = new ArrayList<>();
            if (Files.isDirectory(generatedDirectory)) {
                try (var stream = Files.list(generatedDirectory)) {
                    stream.filter(Files::isRegularFile)
                            .map(path -> path.getFileName().toString())
                            .sorted()
                            .forEach(generatedFiles::add);
                }
            }
            generatedFiles.add(ManifestJsWriter.FILE_NAME);
            generatedFiles = generatedFiles.stream().distinct().sorted().toList();
    
            ManifestMetadata effectiveMetadata = new ManifestMetadata(
                    metadata.program(),
                    metadata.programVersion(),
                    metadata.schemaVersion(),
                    metadata.generatedAt(),
                    metadata.leagueId(),
                    metadata.currentSeasonId(),
                    metadata.processedSeasons(),
                    generatedFiles
            );
    
            return ManifestJsWriter.write(
                    generatedDirectory,
                    options,
                    preflight,
                    effectiveMetadata
            );
        }
    }

## src\main\java\it\alterlega\recordsnext\app\model\CoreRecordCatalog.java

File: src\main\java\it\alterlega\recordsnext\app\model\CoreRecordCatalog.java

    package it.alterlega.recordsnext.app.model;
    
    import java.util.List;
    import java.util.Set;
    
    /**
     * Primo catalogo minimo usato per validare il modello modulare.
     */
    public final class CoreRecordCatalog {
        public static final String CULOMETRO_ID = "easter-egg.culometro";
    
        private CoreRecordCatalog() {
        }
    
        public static List<RecordChild> children() {
            return List.of(
                    new RecordChild(
                            "classics.highest-match-score",
                            "Maggior punteggio in una partita",
                            RecordFamily.CLASSICS,
                            Set.of(
                                    RecordDependency.required("data.matches", DependencyType.DATA),
                                    RecordDependency.required("data.scores", DependencyType.DATA)
                            ),
                            false
                    ),
                    new RecordChild(
                            "series.captain-bonus",
                            "Bonus Capitano consecutivo",
                            RecordFamily.SERIES,
                            Set.of(
                                    RecordDependency.required("data.ordered-matches", DependencyType.DATA),
                                    RecordDependency.required("modifier.captain", DependencyType.FAMILY_CHILD)
                            ),
                            false
                    ),
                    new RecordChild(
                            "ru.deciding",
                            "Riserve d'ufficio decisive",
                            RecordFamily.RU,
                            Set.of(
                                    RecordDependency.required("ru.events", DependencyType.DATA),
                                    RecordDependency.required("simulation.without-ru", DependencyType.SIMULATION)
                            ),
                            false
                    ),
                    new RecordChild(
                            "modifiers.home-field-deciding",
                            "Fattore Campo decisivo",
                            RecordFamily.MODIFIERS,
                            Set.of(
                                    RecordDependency.required("modifier.home-field", DependencyType.MODULE),
                                    RecordDependency.required("configuration.goal-bands", DependencyType.CONFIGURATION)
                            ),
                            false
                    ),
                    new RecordChild(
                            "thresholds.surgical-win",
                            "Vittoria chirurgica",
                            RecordFamily.THRESHOLDS_LUCK,
                            Set.of(
                                    RecordDependency.required("data.scores", DependencyType.DATA),
                                    RecordDependency.required("configuration.goal-bands", DependencyType.CONFIGURATION)
                            ),
                            false
                    ),
                    new RecordChild(
                            CULOMETRO_ID,
                            "Culometro",
                            RecordFamily.THRESHOLDS_LUCK,
                            Set.of(
                                    RecordDependency.required("configuration.culometro", DependencyType.CONFIGURATION),
                                    RecordDependency.optional("modifier.home-field", DependencyType.FAMILY_CHILD)
                            ),
                            true
                    )
            );
        }
    }

## src\main\java\it\alterlega\recordsnext\app\model\DependencyEvaluation.java

File: src\main\java\it\alterlega\recordsnext\app\model\DependencyEvaluation.java

    package it.alterlega.recordsnext.app.model;
    
    import java.util.LinkedHashSet;
    import java.util.Objects;
    import java.util.Set;
    
    /**
     * Esito della valutazione delle dipendenze di un figlio.
     */
    public record DependencyEvaluation(
            OutputStatus status,
            Set<String> missingRequired,
            Set<String> missingOptional
    ) {
        public DependencyEvaluation {
            status = Objects.requireNonNull(status, "status");
            missingRequired = Set.copyOf(new LinkedHashSet<>(
                    Objects.requireNonNullElse(missingRequired, Set.of())
            ));
            missingOptional = Set.copyOf(new LinkedHashSet<>(
                    Objects.requireNonNullElse(missingOptional, Set.of())
            ));
        }
    
        public boolean canGenerate() {
            return status == OutputStatus.GENERATED_COMPLETE
                    || status == OutputStatus.GENERATED_PARTIAL;
        }
    }

## src\main\java\it\alterlega\recordsnext\app\model\DependencyEvaluator.java

File: src\main\java\it\alterlega\recordsnext\app\model\DependencyEvaluator.java

    package it.alterlega.recordsnext.app.model;
    
    import java.util.LinkedHashSet;
    import java.util.Objects;
    import java.util.Set;
    
    /**
     * Valuta le dipendenze senza bloccare l'intera famiglia.
     */
    public final class DependencyEvaluator {
        private DependencyEvaluator() {
        }
    
        public static DependencyEvaluation evaluate(
                RecordChild child,
                ProcessingSelection selection,
                Set<String> availableDependencies
        ) {
            Objects.requireNonNull(child, "child");
            Objects.requireNonNull(selection, "selection");
            Set<String> available = Set.copyOf(
                    Objects.requireNonNullElse(availableDependencies, Set.of())
            );
    
            if (!selection.isChildSelected(child)) {
                return new DependencyEvaluation(
                        OutputStatus.SKIPPED_NOT_SELECTED,
                        Set.of(),
                        Set.of()
                );
            }
    
            Set<String> missingRequired = new LinkedHashSet<>();
            Set<String> missingOptional = new LinkedHashSet<>();
    
            for (RecordDependency dependency : child.dependencies()) {
                if (available.contains(dependency.id())) {
                    continue;
                }
                if (dependency.required()) {
                    missingRequired.add(dependency.id());
                } else {
                    missingOptional.add(dependency.id());
                }
            }
    
            if (!missingRequired.isEmpty()) {
                return new DependencyEvaluation(
                        OutputStatus.SKIPPED_REQUIRED_DEPENDENCY,
                        missingRequired,
                        missingOptional
                );
            }
    
            if (!missingOptional.isEmpty()) {
                return new DependencyEvaluation(
                        OutputStatus.GENERATED_PARTIAL,
                        Set.of(),
                        missingOptional
                );
            }
    
            return new DependencyEvaluation(
                    OutputStatus.GENERATED_COMPLETE,
                    Set.of(),
                    Set.of()
            );
        }
    }

## src\main\java\it\alterlega\recordsnext\app\model\DependencyInventory.java

File: src\main\java\it\alterlega\recordsnext\app\model\DependencyInventory.java

    package it.alterlega.recordsnext.app.model;
    
    import java.util.LinkedHashSet;
    import java.util.Set;
    
    /**
     * Inventario iniziale delle dipendenze disponibili nel flusso RecordsNext 1.0.2.
     * Non esegue controlli sul database: rappresenta solo capacita dichiarate.
     */
    public final class DependencyInventory {
        private DependencyInventory() {
        }
    
        public static Set<String> legacyCapabilities(
                boolean captainEnabled,
                boolean homeFieldEnabled,
                boolean ruSimulationEnabled,
                boolean culometroConfigured
        ) {
            Set<String> dependencies = new LinkedHashSet<>();
            dependencies.add("data.matches");
            dependencies.add("data.scores");
            dependencies.add("data.ordered-matches");
            dependencies.add("configuration.goal-bands");
            dependencies.add("ru.events");
    
            if (captainEnabled) {
                dependencies.add("modifier.captain");
            }
            if (homeFieldEnabled) {
                dependencies.add("modifier.home-field");
            }
            if (ruSimulationEnabled) {
                dependencies.add("simulation.without-ru");
            }
            if (culometroConfigured) {
                dependencies.add("configuration.culometro");
            }
    
            return Set.copyOf(dependencies);
        }
    }

## src\main\java\it\alterlega\recordsnext\app\model\DependencyType.java

File: src\main\java\it\alterlega\recordsnext\app\model\DependencyType.java

    package it.alterlega.recordsnext.app.model;
    
    /**
     * Tipi di dipendenza dichiarabili da un figlio elaborabile.
     */
    public enum DependencyType {
        DATA,
        MODULE,
        CONFIGURATION,
        FAMILY_CHILD,
        SIMULATION,
        SITE,
        MAPPING
    }

## src\main\java\it\alterlega\recordsnext\app\model\ExecutionPlan.java

File: src\main\java\it\alterlega\recordsnext\app\model\ExecutionPlan.java

    package it.alterlega.recordsnext.app.model;
    
    import java.util.List;
    import java.util.Map;
    import java.util.Objects;
    import java.util.stream.Collectors;
    
    /**
     * Piano completo delle elaborazioni richieste.
     */
    public record ExecutionPlan(List<ExecutionPlanItem> items) {
        public ExecutionPlan {
            items = List.copyOf(Objects.requireNonNullElse(items, List.of()));
        }
    
        public List<ExecutionPlanItem> selectedItems() {
            return items.stream().filter(ExecutionPlanItem::selected).toList();
        }
    
        public List<ExecutionPlanItem> executableItems() {
            return items.stream().filter(ExecutionPlanItem::executable).toList();
        }
    
        public List<ExecutionPlanItem> skippedItems() {
            return items.stream().filter(item -> !item.executable()).toList();
        }
    
        public Map<RecordFamily, List<ExecutionPlanItem>> byFamily() {
            return items.stream().collect(Collectors.groupingBy(
                    item -> item.child().family(),
                    java.util.LinkedHashMap::new,
                    Collectors.toList()
            ));
        }
    
        public boolean hasFailures() {
            return items.stream().anyMatch(item -> item.status() == OutputStatus.FAILED);
        }
    }

## src\main\java\it\alterlega\recordsnext\app\model\ExecutionPlanItem.java

File: src\main\java\it\alterlega\recordsnext\app\model\ExecutionPlanItem.java

    package it.alterlega.recordsnext.app.model;
    
    import java.util.Objects;
    import java.util.Set;
    
    /**
     * Riga del piano di elaborazione di un singolo figlio.
     */
    public record ExecutionPlanItem(
            RecordChild child,
            OutputStatus status,
            Set<String> missingRequired,
            Set<String> missingOptional
    ) {
        public ExecutionPlanItem {
            child = Objects.requireNonNull(child, "child");
            status = Objects.requireNonNull(status, "status");
            missingRequired = Set.copyOf(Objects.requireNonNullElse(missingRequired, Set.of()));
            missingOptional = Set.copyOf(Objects.requireNonNullElse(missingOptional, Set.of()));
        }
    
        public boolean selected() {
            return status != OutputStatus.SKIPPED_NOT_SELECTED;
        }
    
        public boolean executable() {
            return status == OutputStatus.GENERATED_COMPLETE
                    || status == OutputStatus.GENERATED_PARTIAL;
        }
    }

## src\main\java\it\alterlega\recordsnext\app\model\ExecutionPlanner.java

File: src\main\java\it\alterlega\recordsnext\app\model\ExecutionPlanner.java

    package it.alterlega.recordsnext.app.model;
    
    import java.util.List;
    import java.util.Objects;
    import java.util.Set;
    
    /**
     * Costruisce il piano senza eseguire alcun elaboratore.
     */
    public final class ExecutionPlanner {
        private ExecutionPlanner() {
        }
    
        public static ExecutionPlan plan(
                ProcessingSelection selection,
                Set<String> availableDependencies
        ) {
            return plan(CoreRecordCatalog.children(), selection, availableDependencies);
        }
    
        public static ExecutionPlan plan(
                List<RecordChild> catalog,
                ProcessingSelection selection,
                Set<String> availableDependencies
        ) {
            Objects.requireNonNull(catalog, "catalog");
            Objects.requireNonNull(selection, "selection");
    
            List<ExecutionPlanItem> items = catalog.stream()
                    .map(child -> toItem(
                            child,
                            DependencyEvaluator.evaluate(
                                    child,
                                    selection,
                                    availableDependencies
                            )
                    ))
                    .toList();
    
            return new ExecutionPlan(items);
        }
    
        private static ExecutionPlanItem toItem(
                RecordChild child,
                DependencyEvaluation evaluation
        ) {
            return new ExecutionPlanItem(
                    child,
                    evaluation.status(),
                    evaluation.missingRequired(),
                    evaluation.missingOptional()
            );
        }
    }

## src\main\java\it\alterlega\recordsnext\app\model\OutputStatus.java

File: src\main\java\it\alterlega\recordsnext\app\model\OutputStatus.java

    package it.alterlega.recordsnext.app.model;
    
    /**
     * Stato finale di un figlio o di un output.
     */
    public enum OutputStatus {
        GENERATED_COMPLETE,
        GENERATED_PARTIAL,
        SKIPPED_REQUIRED_DEPENDENCY,
        SKIPPED_NOT_SELECTED,
        SKIPPED_NO_DATA,
        FAILED
    }

## src\main\java\it\alterlega\recordsnext\app\model\ProcessingSelection.java

File: src\main\java\it\alterlega\recordsnext\app\model\ProcessingSelection.java

    package it.alterlega.recordsnext.app.model;
    
    import java.util.LinkedHashSet;
    import java.util.Objects;
    import java.util.Set;
    import java.util.stream.Collectors;
    
    /**
     * Selezione modulare richiesta dall'utente.
     */
    public record ProcessingSelection(
            Set<RecordFamily> enabledFamilies,
            Set<String> enabledChildren,
            boolean culometroEnabled,
            boolean generateJs,
            boolean publish
    ) {
        public ProcessingSelection {
            enabledFamilies = Set.copyOf(new LinkedHashSet<>(
                    Objects.requireNonNullElse(enabledFamilies, Set.of())
            ));
            Set<String> selectedChildren = enabledChildren == null ? Set.of() : enabledChildren;
            enabledChildren = selectedChildren.stream()
                    .map(ProcessingSelection::normalizeChildId)
                    .collect(Collectors.toUnmodifiableSet());
    
            if (publish && !generateJs) {
                throw new IllegalArgumentException(
                        "Publishing requires JavaScript generation"
                );
            }
        }
    
        public boolean isFamilyEnabled(RecordFamily family) {
            return enabledFamilies.contains(Objects.requireNonNull(family, "family"));
        }
    
        public boolean isChildSelected(RecordChild child) {
            Objects.requireNonNull(child, "child");
            if (!isFamilyEnabled(child.family())) {
                return false;
            }
            if (child.optInOnly()) {
                return enabledChildren.contains(child.id());
            }
            return enabledChildren.isEmpty() || enabledChildren.contains(child.id());
        }
    
        public OutputStatus selectionStatus(RecordChild child) {
            return isChildSelected(child)
                    ? OutputStatus.GENERATED_COMPLETE
                    : OutputStatus.SKIPPED_NOT_SELECTED;
        }
    
        private static String normalizeChildId(String value) {
            Objects.requireNonNull(value, "enabled child id");
            String normalized = value.trim();
            if (normalized.isEmpty()) {
                throw new IllegalArgumentException("Enabled child id cannot be blank");
            }
            return normalized;
        }
    }

## src\main\java\it\alterlega\recordsnext\app\model\RecordChild.java

File: src\main\java\it\alterlega\recordsnext\app\model\RecordChild.java

    package it.alterlega.recordsnext.app.model;
    
    import java.util.LinkedHashSet;
    import java.util.Objects;
    import java.util.Set;
    
    /**
     * Definizione di un singolo figlio elaborabile.
     */
    public record RecordChild(
            String id,
            String displayName,
            RecordFamily family,
            Set<RecordDependency> dependencies,
            boolean optInOnly
    ) {
        public RecordChild {
            id = normalize(id, "id");
            displayName = normalize(displayName, "displayName");
            family = Objects.requireNonNull(family, "family");
            dependencies = Set.copyOf(new LinkedHashSet<>(
                    Objects.requireNonNullElse(dependencies, Set.of())
            ));
        }
    
        public Set<RecordDependency> requiredDependencies() {
            return dependencies.stream()
                    .filter(RecordDependency::required)
                    .collect(java.util.stream.Collectors.toUnmodifiableSet());
        }
    
        public Set<RecordDependency> optionalDependencies() {
            return dependencies.stream()
                    .filter(dependency -> !dependency.required())
                    .collect(java.util.stream.Collectors.toUnmodifiableSet());
        }
    
        private static String normalize(String value, String field) {
            Objects.requireNonNull(value, field);
            String normalized = value.trim();
            if (normalized.isEmpty()) {
                throw new IllegalArgumentException(field + " cannot be blank");
            }
            return normalized;
        }
    }

## src\main\java\it\alterlega\recordsnext\app\model\RecordDependency.java

File: src\main\java\it\alterlega\recordsnext\app\model\RecordDependency.java

    package it.alterlega.recordsnext.app.model;
    
    import java.util.Objects;
    
    /**
     * Dipendenza dichiarata da un figlio.
     */
    public record RecordDependency(
            String id,
            DependencyType type,
            boolean required
    ) {
        public RecordDependency {
            id = normalizeId(id);
            type = Objects.requireNonNull(type, "type");
        }
    
        public static RecordDependency required(String id, DependencyType type) {
            return new RecordDependency(id, type, true);
        }
    
        public static RecordDependency optional(String id, DependencyType type) {
            return new RecordDependency(id, type, false);
        }
    
        private static String normalizeId(String value) {
            Objects.requireNonNull(value, "id");
            String normalized = value.trim();
            if (normalized.isEmpty()) {
                throw new IllegalArgumentException("Dependency id cannot be blank");
            }
            return normalized;
        }
    }

## src\main\java\it\alterlega\recordsnext\app\model\RecordFamily.java

File: src\main\java\it\alterlega\recordsnext\app\model\RecordFamily.java

    package it.alterlega.recordsnext.app.model;
    
    /**
     * Famiglie elaborabili di RecordsNext 2.0.
     */
    public enum RecordFamily {
        CLASSICS("classics"),
        SERIES("series"),
        RU("ru"),
        MODIFIERS("modifiers"),
        THRESHOLDS_LUCK("thresholdsLuck");
    
        private final String id;
    
        RecordFamily(String id) {
            this.id = id;
        }
    
        public String id() {
            return id;
        }
    }

## src\main\java\it\alterlega\recordsnext\app\PipelineConfig.java

File: src\main\java\it\alterlega\recordsnext\app\PipelineConfig.java

    package it.alterlega.recordsnext.app;
    
    import java.io.IOException;
    import java.io.InputStream;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.sql.Connection;
    import java.sql.DriverManager;
    import java.sql.ResultSet;
    import java.sql.Statement;
    import java.util.Arrays;
    import java.util.List;
    import java.util.Properties;
    
    public record PipelineConfig(Path projectRoot, Path reports, Path classicArchive, Path ruArchive,
                                 Path staging, Path siteJs, List<String> seasons) {
        public static PipelineConfig load(Path projectRoot, Path file) throws IOException {
            Properties p = new Properties();
            try (InputStream in = Files.newInputStream(file)) {
                p.load(in);
            }
            List<String> seasons = Arrays.stream(p.getProperty("seasons", "").split("\\s*,\\s*"))
                .filter(s -> !s.isBlank()).toList();
            Path normalizedRoot = projectRoot.toAbsolutePath().normalize();
            return new PipelineConfig(normalizedRoot,
                resolve(normalizedRoot, p.getProperty("reports", "data/reports")),
                resolve(normalizedRoot, p.getProperty("classicArchive", "data/records-archive/stagioni")),
                resolve(normalizedRoot, p.getProperty("ruArchive", "data/records-archive/riserveufficio")),
                resolve(normalizedRoot, p.getProperty("staging", "data/site-export-staging")),
                resolvePublishDirectory(normalizedRoot, p), seasons);
        }
    
        public static Path resolvePublishDirectory(Path projectRoot, Properties properties) {
            String mode = properties.getProperty("publish.destinationMode", "currentSeason").trim();
            if ("custom".equalsIgnoreCase(mode)) {
                String custom = properties.getProperty("publish.customDirectory", "").trim();
                if (!custom.isEmpty()) {
                    return resolve(projectRoot, custom);
                }
            }
    
            Path database = resolve(projectRoot,
                properties.getProperty("database", "data/database/recordsnext.db"));
            if (Files.isRegularFile(database)) {
                String sql = """
                    SELECT c.local_site_path
                    FROM rn_season s
                    JOIN rn_season_configuration c ON c.season_id=s.season_id
                    WHERE s.is_anchor=1
                      AND c.local_site_path IS NOT NULL
                      AND TRIM(c.local_site_path)<>''
                    ORDER BY s.sort_order DESC
                    LIMIT 1
                    """;
                try {
                    Class.forName("org.sqlite.JDBC");
                    try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + database);
                         Statement statement = connection.createStatement();
                         ResultSet result = statement.executeQuery(sql)) {
                        if (result.next()) {
                            return Path.of(result.getString(1)).resolve("js").toAbsolutePath().normalize();
                        }
                    }
                } catch (Exception ignored) {
                    // Fallback to the legacy property below.
                }
            }
            return resolve(projectRoot,
                properties.getProperty("siteJs", "E:/fantacalcio/Lega2025/js"));
        }
    
        private static Path resolve(Path root, String value) {
            Path path = Path.of(value);
            return (path.isAbsolute() ? path : root.resolve(path)).normalize();
        }
    }

## src\main\java\it\alterlega\recordsnext\app\PipelinePreflight.java

File: src\main\java\it\alterlega\recordsnext\app\PipelinePreflight.java

    package it.alterlega.recordsnext.app;
    
    import it.alterlega.recordsnext.app.model.DependencyInventory;
    import it.alterlega.recordsnext.app.model.ExecutionPlan;
    import it.alterlega.recordsnext.app.model.ExecutionPlanItem;
    import it.alterlega.recordsnext.app.model.ExecutionPlanner;
    import it.alterlega.recordsnext.app.model.OutputStatus;
    
    import java.util.List;
    import java.util.Objects;
    import java.util.Set;
    
    /**
     * Costruisce e riassume il piano prima dell'esecuzione della pipeline.
     */
    public final class PipelinePreflight {
        private PipelinePreflight() {
        }
    
        public static Result evaluate(ProcessingOptions options) {
            Objects.requireNonNull(options, "options");
    
            Set<String> availableDependencies = DependencyInventory.legacyCapabilities(
                    false,
                    false,
                    options.familyEnabled(it.alterlega.recordsnext.app.model.RecordFamily.RU),
                    options.culometroEnabled()
            );
    
            ExecutionPlan plan = ExecutionPlanner.plan(
                    options.selection(),
                    availableDependencies
            );
    
            return new Result(plan, availableDependencies);
        }
    
        public record Result(
                ExecutionPlan plan,
                Set<String> availableDependencies
        ) {
            public Result {
                plan = Objects.requireNonNull(plan, "plan");
                availableDependencies = Set.copyOf(
                        Objects.requireNonNullElse(availableDependencies, Set.of())
                );
            }
    
            public int selectedCount() {
                return plan.selectedItems().size();
            }
    
            public int executableCount() {
                return plan.executableItems().size();
            }
    
            public int completeCount() {
                return count(OutputStatus.GENERATED_COMPLETE);
            }
    
            public int partialCount() {
                return count(OutputStatus.GENERATED_PARTIAL);
            }
    
            public int skippedDependencyCount() {
                return count(OutputStatus.SKIPPED_REQUIRED_DEPENDENCY);
            }
    
            public List<ExecutionPlanItem> relevantItems() {
                return plan.selectedItems();
            }
    
            public List<String> messages() {
                return relevantItems().stream()
                        .map(PipelinePreflight.Result::message)
                        .toList();
            }
    
            public String summary() {
                return "Preflight: selezionati=" + selectedCount()
                        + ", eseguibili=" + executableCount()
                        + ", completi=" + completeCount()
                        + ", parziali=" + partialCount()
                        + ", saltati per dipendenze=" + skippedDependencyCount();
            }
    
            private int count(OutputStatus status) {
                return (int) plan.items().stream()
                        .filter(item -> item.status() == status)
                        .count();
            }
    
            private static String message(ExecutionPlanItem item) {
                StringBuilder value = new StringBuilder()
                        .append(item.child().id())
                        .append(" -> ")
                        .append(item.status());
    
                if (!item.missingRequired().isEmpty()) {
                    value.append("; richieste mancanti=")
                            .append(item.missingRequired());
                }
                if (!item.missingOptional().isEmpty()) {
                    value.append("; opzionali mancanti=")
                            .append(item.missingOptional());
                }
                return value.toString();
            }
        }
    }

## src\main\java\it\alterlega\recordsnext\app\ProcessingMode.java

File: src\main\java\it\alterlega\recordsnext\app\ProcessingMode.java

    package it.alterlega.recordsnext.app;
    
    public enum ProcessingMode {
        FULL,
        CONSOLIDATED
    }

## src\main\java\it\alterlega\recordsnext\app\ProcessingOptions.java

File: src\main\java\it\alterlega\recordsnext\app\ProcessingOptions.java

    package it.alterlega.recordsnext.app;
    
    import it.alterlega.recordsnext.app.model.ProcessingSelection;
    import it.alterlega.recordsnext.app.model.RecordChild;
    import it.alterlega.recordsnext.app.model.RecordFamily;
    
    import java.util.EnumSet;
    import java.util.Objects;
    import java.util.Set;
    
    /**
     * Opzioni di elaborazione compatibili con RecordsNext 1.0.2 e con il modello
     * modulare di RecordsNext 2.0.
     */
    public record ProcessingOptions(
            boolean classic,
            boolean ru,
            boolean generateJs,
            boolean publish,
            ProcessingSelection selection
    ) {
        /**
         * Costruttore compatibile con la GUI e la pipeline RecordsNext 1.0.2.
         */
        public ProcessingOptions(
                boolean classic,
                boolean ru,
                boolean generateJs,
                boolean publish
        ) {
            this(
                    classic,
                    ru,
                    generateJs,
                    publish,
                    legacySelection(classic, ru, generateJs, publish)
            );
        }
    
        public ProcessingOptions {
            selection = Objects.requireNonNull(selection, "selection");
    
            if (!classic && !ru && selection.enabledFamilies().isEmpty()) {
                throw new IllegalArgumentException("Selezionare almeno un'elaborazione");
            }
            if (publish && !generateJs) {
                throw new IllegalArgumentException(
                        "Per pubblicare nel sito occorre generare i file JavaScript"
                );
            }
            if (selection.generateJs() != generateJs) {
                throw new IllegalArgumentException(
                        "La selezione modulare e le opzioni legacy discordano su generateJs"
                );
            }
            if (selection.publish() != publish) {
                throw new IllegalArgumentException(
                        "La selezione modulare e le opzioni legacy discordano su publish"
                );
            }
            if (classic != selection.isFamilyEnabled(RecordFamily.CLASSICS)) {
                throw new IllegalArgumentException(
                        "La selezione modulare e le opzioni legacy discordano sui Classici"
                );
            }
            if (ru != selection.isFamilyEnabled(RecordFamily.RU)) {
                throw new IllegalArgumentException(
                        "La selezione modulare e le opzioni legacy discordano sulle RU"
                );
            }
        }
    
        /**
         * Crea opzioni 2.0 partendo dalla selezione modulare.
         */
        public static ProcessingOptions modular(ProcessingSelection selection) {
            Objects.requireNonNull(selection, "selection");
            return new ProcessingOptions(
                    selection.isFamilyEnabled(RecordFamily.CLASSICS),
                    selection.isFamilyEnabled(RecordFamily.RU),
                    selection.generateJs(),
                    selection.publish(),
                    selection
            );
        }
    
        public boolean familyEnabled(RecordFamily family) {
            return selection.isFamilyEnabled(family);
        }
    
        public boolean childSelected(RecordChild child) {
            return selection.isChildSelected(child);
        }
    
        public boolean culometroEnabled() {
            return selection.culometroEnabled();
        }
    
        private static ProcessingSelection legacySelection(
                boolean classic,
                boolean ru,
                boolean generateJs,
                boolean publish
        ) {
            EnumSet<RecordFamily> families = EnumSet.noneOf(RecordFamily.class);
            if (classic) {
                families.add(RecordFamily.CLASSICS);
            }
            if (ru) {
                families.add(RecordFamily.RU);
            }
            return new ProcessingSelection(
                    Set.copyOf(families),
                    Set.of(),
                    false,
                    generateJs,
                    publish
            );
        }
    }

## src\main\java\it\alterlega\recordsnext\app\RecordsNextPipeline.java

File: src\main\java\it\alterlega\recordsnext\app\RecordsNextPipeline.java

    package it.alterlega.recordsnext.app;
    
    import it.alterlega.recordsnext.Records2026SitePublisher;
    import it.alterlega.recordsnext.RiserveUfficioArchiveBuilder;
    import it.alterlega.recordsnext.SeasonRecordsArchiveBuilder;
    import it.alterlega.recordsnext.app.manifest.ManifestMetadata;
    import it.alterlega.recordsnext.app.model.RecordFamily;
    
    import java.nio.file.Path;
    import java.time.OffsetDateTime;
    import java.util.EnumSet;
    import java.util.List;
    import java.util.Locale;
    import java.util.Set;
    
    public final class RecordsNextPipeline {
        private static final Set<RecordFamily> IMPLEMENTED_FAMILIES = Set.copyOf(
                EnumSet.of(RecordFamily.CLASSICS, RecordFamily.RU)
        );
    
        public interface Listener {
            void phase(String text, int percent);
            default void timing(String text) { phase("TEMPO " + text, -1); }
        }
    
        public record Result(int classicEntries, int ruSeasons, int files, int published) {}
    
        public Result run(
                PipelineConfig c,
                ProcessingOptions o,
                ProcessingMode mode,
                Listener l
        ) throws Exception {
            PipelinePreflight.Result preflight = preflight(o);
            l.phase(preflight.summary(), 2);
            for (String message : preflight.messages()) {
                l.phase("PREFLIGHT " + message, -1);
            }
    
            validateImplementedFamilies(o);
    
            long totalStarted = System.nanoTime();
            Path database = c.projectRoot().resolve("data/database/recordsnext.db").normalize();
            RecordsNextPreparationService preparation = new RecordsNextPreparationService(
                    c.projectRoot(),
                    database
            );
    
            long preparationStarted = System.nanoTime();
            List<String> changedSeasons = preparation.prepare(mode, c.seasons(), l);
            l.timing("preparazione complessiva: " + elapsed(preparationStarted));
    
            if (o.familyEnabled(RecordFamily.CLASSICS)) {
                l.phase("Generazione record classici", 55);
                long started = System.nanoTime();
                SeasonRecordsArchiveBuilder.build(
                        c.reports(),
                        c.classicArchive(),
                        changedSeasons
                );
                l.timing("record classici: " + elapsed(started));
            }
    
            if (o.familyEnabled(RecordFamily.RU)) {
                l.phase("Generazione riserve d'ufficio", 68);
                long started = System.nanoTime();
                RiserveUfficioArchiveBuilder.build(
                        c.reports(),
                        c.ruArchive(),
                        changedSeasons
                );
                l.timing("riserve d'ufficio: " + elapsed(started));
            }
    
            Result result;
            if (!o.generateJs()) {
                l.phase("Archivi elaborati; generazione JavaScript non richiesta", 96);
                result = new Result(0, 0, 0, 0);
            } else {
                l.phase(
                        o.publish()
                                ? "Generazione e pubblicazione JavaScript"
                                : "Generazione JavaScript",
                        82
                );
                long started = System.nanoTime();
                ManifestMetadata manifestMetadata = new ManifestMetadata(
                        "RecordsNext by mauz79",
                        "2.0.0-dev",
                        "2.0",
                        OffsetDateTime.now(),
                        "",
                        "",
                        c.seasons(),
                        List.of()
                );
                var r = Records2026SitePublisher.run(
                        c.classicArchive(),
                        c.ruArchive(),
                        c.staging(),
                        c.siteJs(),
                        !o.publish(),
                        o.familyEnabled(RecordFamily.CLASSICS),
                        o.familyEnabled(RecordFamily.RU),
                        o,
                        preflight,
                        manifestMetadata
                );
                l.timing(
                        (o.publish()
                                ? "generazione e pubblicazione JavaScript: "
                                : "generazione JavaScript: ")
                                + elapsed(started)
                );
                result = new Result(
                        r.classicEntries(),
                        r.ruSeasons(),
                        r.validatedFiles(),
                        r.publishedFiles()
                );
            }
    
            preparation.saveConsolidation(c.seasons());
            l.timing("totale elaborazione: " + elapsed(totalStarted));
            l.phase("Elaborazione completata e consolidamento aggiornato", 100);
            return result;
        }
    
        public PipelinePreflight.Result preflight(ProcessingOptions options) {
            return PipelinePreflight.evaluate(options);
        }
    
        public boolean hasConsolidation(PipelineConfig c) {
            Path database = c.projectRoot().resolve("data/database/recordsnext.db").normalize();
            return new RecordsNextPreparationService(
                    c.projectRoot(),
                    database
            ).hasConsolidation();
        }
    
        static void validateImplementedFamilies(ProcessingOptions options) {
            Set<RecordFamily> unsupported = EnumSet.copyOf(options.selection().enabledFamilies());
            unsupported.removeAll(IMPLEMENTED_FAMILIES);
            if (!unsupported.isEmpty()) {
                throw new IllegalArgumentException(
                        "Famiglie selezionate ma non ancora collegate alla pipeline 2.0: "
                                + unsupported
                );
            }
            if (options.culometroEnabled()) {
                throw new IllegalArgumentException(
                        "Il Culometro e configurabile ma non e ancora collegato alla pipeline 2.0"
                );
            }
        }
    
        private static String elapsed(long started) {
            double seconds = (System.nanoTime() - started) / 1_000_000_000.0;
            return String.format(Locale.ROOT, "%.3f s", seconds);
        }
    }

## src\main\java\it\alterlega\recordsnext\app\RecordsNextPreparationService.java

File: src\main\java\it\alterlega\recordsnext\app\RecordsNextPreparationService.java

    package it.alterlega.recordsnext.app;
    
    import it.alterlega.recordsnext.CanonicalViews;
    import it.alterlega.recordsnext.ConfigurationSchema;
    import it.alterlega.recordsnext.RawSqliteImporter;
    import it.alterlega.recordsnext.SeasonNormalizedBatchExporter;
    
    import java.io.InputStream;
    import java.io.OutputStream;
    import java.nio.charset.StandardCharsets;
    import java.security.MessageDigest;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.sql.Connection;
    import java.sql.DriverManager;
    import java.sql.PreparedStatement;
    import java.sql.ResultSet;
    import java.util.ArrayList;
    import java.util.Comparator;
    import java.util.List;
    import java.util.Properties;
    
    final class RecordsNextPreparationService {
        record SeasonSource(String id, String type, String fcm, String fca,
                            String localSite, String onlineSite) {}
    
        private final Path root;
        private final Path database;
        private static final String NORMALIZER_CACHE_VERSION = "season-normalized-v21";
    
        private final Path stateFile;
        private final Path normalizationCacheFile;
    
        RecordsNextPreparationService(Path root, Path database) {
            this.root = root.toAbsolutePath().normalize();
            this.database = database.toAbsolutePath().normalize();
            this.stateFile = this.root.resolve("data/consolidation/recordsnext-consolidation.properties");
            this.normalizationCacheFile = this.root.resolve("data/consolidation/normalization-cache.properties");
        }
    
        List<String> prepare(ProcessingMode mode, List<String> selected,
                             RecordsNextPipeline.Listener listener) throws Exception {
            List<SeasonSource> seasons = loadConfigured(selected);
            if (seasons.isEmpty()) {
                throw new IllegalStateException("Nessuna stagione configurata da elaborare.");
            }
            List<SeasonSource> managed = seasons.stream()
                .filter(s -> "GESTITA".equals(s.type()))
                .sorted(Comparator.comparing(SeasonSource::id))
                .toList();
            if (managed.isEmpty()) {
                throw new IllegalStateException("Non esistono stagioni gestite da importare.");
            }
            SeasonSource current = managed.get(managed.size() - 1);
    
            List<SeasonSource> toImport;
            if (mode == ProcessingMode.CONSOLIDATED) {
                validateConsolidation(seasons, current.id());
                toImport = List.of(current);
                listener.phase("Aggiornamento della stagione attuale " + current.id(), 5);
            } else {
                toImport = managed;
                listener.phase("Importazione completa delle stagioni gestite", 5);
            }
    
            int index = 0;
            boolean imported = false;
            for (SeasonSource season : toImport) {
                validateManagedSource(season);
                int percent = 6 + (int) Math.round((index++ * 22.0) / Math.max(1, toImport.size()));
                if (sourceNeedsImport(season.id(), "FCM", season.fcm())) {
                    listener.phase(season.id() + " — importazione FCM", percent);
                    long started = System.nanoTime();
                    RawSqliteImporter.main(new String[]{season.fcm(), "FCM", season.id(), database.toString()});
                    listener.timing(season.id() + " — importazione FCM: " + elapsed(started));
                    imported = true;
                } else {
                    listener.phase(season.id() + " — FCM invariato", percent);
                }
                if (sourceNeedsImport(season.id(), "FCA", season.fca())) {
                    listener.phase(season.id() + " — importazione FCA", Math.min(29, percent + 2));
                    long started = System.nanoTime();
                    RawSqliteImporter.main(new String[]{season.fca(), "FCA", season.id(), database.toString()});
                    listener.timing(season.id() + " — importazione FCA: " + elapsed(started));
                    imported = true;
                } else {
                    listener.phase(season.id() + " — FCA invariato", Math.min(29, percent + 2));
                }
            }
    
            if (imported) {
                listener.phase("Aggiornamento configurazione e identità storiche", 30);
                long started = System.nanoTime();
                ConfigurationSchema.main(new String[]{database.toString(), current.id()});
                listener.timing("configurazione e identità: " + elapsed(started));
            } else {
                listener.phase("Sorgenti già importate; configurazione conservata", 30);
            }
    
            validateMappings(managed, current.id());
    
            listener.phase("Rigenerazione viste canoniche", 34);
            long canonicalStarted = System.nanoTime();
            CanonicalViews.main(new String[]{database.toString()});
            listener.timing("viste canoniche: " + elapsed(canonicalStarted));
    
            List<String> normalize = mode == ProcessingMode.CONSOLIDATED
                ? List.of(current.id())
                : managed.stream().map(SeasonSource::id).toList();
            Properties normalizationCache = loadNormalizationCache();
            int done = 0;
            for (String season : normalize) {
                int percent = 36 + (int) Math.round((done++ * 14.0) / Math.max(1, normalize.size()));
                SeasonSource source = managed.stream()
                    .filter(item -> item.id().equals(season))
                    .findFirst()
                    .orElseThrow();
                String signature = normalizationSignature(source);
                if (normalizationCacheValid(season, signature, normalizationCache)) {
                    listener.phase(season + " — normalizzazione invariata, riutilizzata", percent);
                    continue;
                }
                if (!normalizationCache.containsKey("season." + season + ".signature")
                        && canBootstrapNormalizationCache(source)) {
                    normalizationCache.setProperty("season." + season + ".signature", signature);
                    normalizationCache.setProperty("season." + season + ".completedAt", java.time.Instant.now().toString());
                    saveNormalizationCache(normalizationCache);
                    listener.phase(season + " — cache normalizzazione inizializzata, dati riutilizzati", percent);
                    continue;
                }
                listener.phase(season + " — normalizzazione", percent);
                long normalizeStarted = System.nanoTime();
                SeasonNormalizedBatchExporter.export(database, season, root);
                listener.timing(season + " — normalizzazione: " + elapsed(normalizeStarted));
                normalizationCache.setProperty("season." + season + ".signature", signature);
                normalizationCache.setProperty("season." + season + ".completedAt", java.time.Instant.now().toString());
                saveNormalizationCache(normalizationCache);
            }
            return normalize;
        }
    
    
        private Properties loadNormalizationCache() throws Exception {
            Properties cache = new Properties();
            if (Files.isRegularFile(normalizationCacheFile)) {
                try (InputStream in = Files.newInputStream(normalizationCacheFile)) {
                    cache.load(in);
                }
            }
            return cache;
        }
    
        private void saveNormalizationCache(Properties cache) throws Exception {
            Files.createDirectories(normalizationCacheFile.getParent());
            try (OutputStream out = Files.newOutputStream(normalizationCacheFile)) {
                cache.store(out, "RecordsNext normalized season cache");
            }
        }
    
        private String normalizationSignature(SeasonSource season) throws Exception {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            updateDigest(digest, NORMALIZER_CACHE_VERSION);
            updateDigest(digest, season.id());
            updateDigest(digest, season.type());
            updateFileDigest(digest, season.fcm());
            updateFileDigest(digest, season.fca());
            updateDigest(digest, season.localSite());
            updateDigest(digest, season.onlineSite());
            updateDigest(digest, mappingStamp(season.id()));
            return toHex(digest.digest());
        }
    
        private static void updateFileDigest(MessageDigest digest, String value) throws Exception {
            updateDigest(digest, value == null ? "" : value);
            if (value == null || value.isBlank()) return;
            Path file = Path.of(value).toAbsolutePath().normalize();
            if (!Files.isRegularFile(file)) return;
            updateDigest(digest, Long.toString(Files.size(file)));
            updateDigest(digest, Files.getLastModifiedTime(file).toInstant().toString());
        }
    
        private static void updateDigest(MessageDigest digest, String value) {
            digest.update((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
            digest.update((byte) 0);
        }
    
        private boolean canBootstrapNormalizationCache(SeasonSource season) throws Exception {
            if (!Files.isRegularFile(stateFile) || !normalizationOutputsComplete(season.id())) {
                return false;
            }
            Properties old = new Properties();
            try (InputStream in = Files.newInputStream(stateFile)) {
                old.load(in);
            }
            String prefix = "season." + season.id() + ".";
            Properties now = snapshot(List.of(season));
            for (String suffix : List.of("type", "fcm", "fcm.size", "fcm.mtime",
                    "fca", "fca.size", "fca.mtime", "site", "online", "mapping")) {
                String key = prefix + suffix;
                if (!old.getProperty(key, "").equals(now.getProperty(key, ""))) {
                    return false;
                }
            }
            return true;
        }
    
        private boolean normalizationCacheValid(String season, String signature, Properties cache) throws Exception {
            if (!signature.equals(cache.getProperty("season." + season + ".signature", ""))) {
                return false;
            }
            return normalizationOutputsComplete(season);
        }
    
        private boolean normalizationOutputsComplete(String season) throws Exception {
            Path outputDir = root.resolve("data/reports").resolve(season);
            if (!Files.isDirectory(outputDir)) return false;
            long actual;
            try (var files = Files.list(outputDir)) {
                actual = files.filter(path -> {
                    String name = path.getFileName().toString();
                    return name.startsWith("season_normalized_") && name.endsWith(".json")
                        && !name.contains(".stage") && !name.contains(".final");
                }).count();
            }
            return actual >= expectedCompetitionCount(season) && actual > 0;
        }
    
        private long expectedCompetitionCount(String season) throws Exception {
            String sql = """
                SELECT COUNT(DISTINCT competition_name)
                FROM rn_team_match
                WHERE season_id=? AND competition_name IS NOT NULL AND TRIM(competition_name)<>''
                """;
            try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + database);
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, season);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getLong(1) : 0;
                }
            }
        }
    
        void saveConsolidation(List<String> selected) throws Exception {
            List<SeasonSource> seasons = loadConfigured(selected);
            Properties p = snapshot(seasons);
            Files.createDirectories(stateFile.getParent());
            try (OutputStream out = Files.newOutputStream(stateFile)) {
                p.store(out, "RecordsNext consolidation state");
            }
        }
    
        boolean hasConsolidation() {
            return Files.isRegularFile(stateFile);
        }
    
        private void validateConsolidation(List<SeasonSource> seasons, String currentId) throws Exception {
            if (!Files.isRegularFile(stateFile)) {
                throw new IllegalStateException("Nessun consolidamento disponibile. Eseguire prima un'elaborazione completa.");
            }
            Properties old = new Properties();
            try (InputStream in = Files.newInputStream(stateFile)) { old.load(in); }
            Properties now = snapshot(seasons);
            String oldIds = old.getProperty("seasons", "");
            String nowIds = now.getProperty("seasons", "");
            if (!oldIds.equals(nowIds)) {
                throw invalid("è cambiato l'elenco delle stagioni");
            }
            for (SeasonSource season : seasons) {
                if (season.id().equals(currentId)) continue;
                String prefix = "season." + season.id() + ".";
                for (String suffix : List.of("type", "fcm", "fcm.size", "fcm.mtime", "fca", "fca.size", "fca.mtime", "site", "online", "mapping")) {
                    String key = prefix + suffix;
                    if (!old.getProperty(key, "").equals(now.getProperty(key, ""))) {
                        throw invalid("è cambiata la stagione storica " + season.id() + " (" + suffix + ")");
                    }
                }
            }
        }
    
        private static IllegalStateException invalid(String reason) {
            return new IllegalStateException("Il consolidamento non è più valido: " + reason
                + ". Eseguire una nuova elaborazione completa.");
        }
    
        private Properties snapshot(List<SeasonSource> seasons) throws Exception {
            Properties p = new Properties();
            p.setProperty("seasons", String.join(",", seasons.stream().map(SeasonSource::id).sorted().toList()));
            for (SeasonSource s : seasons) {
                String k = "season." + s.id() + ".";
                p.setProperty(k + "type", s.type());
                fileSnapshot(p, k + "fcm", s.fcm());
                fileSnapshot(p, k + "fca", s.fca());
                p.setProperty(k + "site", s.localSite());
                p.setProperty(k + "online", s.onlineSite());
                p.setProperty(k + "mapping", mappingStamp(s.id()));
            }
            return p;
        }
    
        private static void fileSnapshot(Properties p, String key, String value) throws Exception {
            p.setProperty(key, value == null ? "" : value);
            if (value != null && !value.isBlank() && Files.isRegularFile(Path.of(value))) {
                Path file = Path.of(value);
                p.setProperty(key + ".size", Long.toString(Files.size(file)));
                p.setProperty(key + ".mtime", Long.toString(Files.getLastModifiedTime(file).toMillis()));
            } else {
                p.setProperty(key + ".size", "");
                p.setProperty(key + ".mtime", "");
            }
        }
    
        private String mappingStamp(String seasonId) throws Exception {
            // The consolidation signature must describe mapping decisions, not timestamps.
            // Only entities belonging to the latest FCM import of the season are relevant.
            String competitionSql = """
                SELECT s.source_competition_id,
                       s.normalized_name,
                       m.mapping_status,
                       COALESCE(m.competition_identity_id,0)
                FROM rn_competition_season s
                JOIN rn_competition_mapping m
                  ON m.competition_season_id=s.competition_season_id
                JOIN rn_source_file sf ON sf.source_file_id=s.source_file_id
                WHERE s.season_id=?
                  AND sf.source_type='FCM'
                  AND sf.import_id=(
                      SELECT MAX(sf2.import_id)
                      FROM rn_source_file sf2
                      WHERE sf2.season_id=s.season_id AND sf2.source_type='FCM'
                  )
                ORDER BY s.source_competition_id, s.normalized_name,
                         m.mapping_status, COALESCE(m.competition_identity_id,0)
                """;
            String teamSql = """
                SELECT s.source_team_id,
                       s.normalized_name,
                       COALESCE(s.source_division_id,-1),
                       COALESCE(s.source_team_number,-1),
                       m.mapping_status,
                       COALESCE(m.team_identity_id,0)
                FROM rn_team_season s
                JOIN rn_team_mapping m ON m.team_season_id=s.team_season_id
                JOIN rn_source_file sf ON sf.source_file_id=s.source_file_id
                WHERE s.season_id=?
                  AND sf.source_type='FCM'
                  AND sf.import_id=(
                      SELECT MAX(sf2.import_id)
                      FROM rn_source_file sf2
                      WHERE sf2.season_id=s.season_id AND sf2.source_type='FCM'
                  )
                ORDER BY s.source_team_id, s.normalized_name,
                         COALESCE(s.source_division_id,-1), COALESCE(s.source_team_number,-1),
                         m.mapping_status, COALESCE(m.team_identity_id,0)
                """;
    
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + database)) {
                updateMappingDigest(c, competitionSql, seasonId, "C", digest);
                updateMappingDigest(c, teamSql, seasonId, "T", digest);
            }
            return toHex(digest.digest());
        }
    
        private static void updateMappingDigest(Connection connection, String sql,
                                                String seasonId, String prefix,
                                                MessageDigest digest) throws Exception {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, seasonId);
                try (ResultSet rs = ps.executeQuery()) {
                    int columns = rs.getMetaData().getColumnCount();
                    while (rs.next()) {
                        digest.update(prefix.getBytes(StandardCharsets.UTF_8));
                        digest.update((byte) 0);
                        for (int column = 1; column <= columns; column++) {
                            String value = rs.getString(column);
                            digest.update((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
                            digest.update((byte) 0);
                        }
                        digest.update((byte) '\n');
                    }
                }
            }
        }
    
        private static String toHex(byte[] bytes) {
            StringBuilder result = new StringBuilder(bytes.length * 2);
            for (byte value : bytes) {
                result.append(Character.forDigit((value >>> 4) & 0x0f, 16));
                result.append(Character.forDigit(value & 0x0f, 16));
            }
            return result.toString();
        }
    
    
        private boolean sourceNeedsImport(String seasonId, String sourceType, String configuredPath) throws Exception {
            Path file = Path.of(configuredPath).toAbsolutePath().normalize();
            String sql = """
                SELECT source_path,source_size_bytes,source_last_modified
                FROM rn_import
                WHERE season_id=? AND source_type=? AND status='COMPLETED'
                ORDER BY import_id DESC
                LIMIT 1
                """;
            try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + database);
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, seasonId);
                ps.setString(2, sourceType);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return true;
                    String previousPath = Path.of(rs.getString(1)).toAbsolutePath().normalize().toString();
                    long previousSize = rs.getLong(2);
                    String previousModified = rs.getString(3);
                    return !previousPath.equalsIgnoreCase(file.toString())
                        || previousSize != Files.size(file)
                        || !previousModified.equals(Files.getLastModifiedTime(file).toInstant().toString());
                }
            }
        }
        private List<SeasonSource> loadConfigured(List<String> selected) throws Exception {
            if (!Files.isRegularFile(database)) {
                throw new IllegalStateException("Database RecordsNext non trovato: " + database);
            }
            String sql = """
                SELECT s.season_id,
                       COALESCE(c.management_type,'GESTITA'),
                       COALESCE(c.configured_fcm_path,''),
                       COALESCE(c.configured_fca_path,''),
                       COALESCE(c.local_site_path,''),
                       COALESCE(c.online_site_url,'')
                FROM rn_season s
                LEFT JOIN rn_season_configuration c ON c.season_id=s.season_id
                ORDER BY s.season_id
                """;
            List<SeasonSource> result = new ArrayList<>();
            try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + database);
                 PreparedStatement ps = c.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString(1);
                    if (selected.contains(id)) {
                        result.add(new SeasonSource(id, rs.getString(2), rs.getString(3),
                            rs.getString(4), rs.getString(5), rs.getString(6)));
                    }
                }
            }
            return result;
        }
    
        private void validateMappings(List<SeasonSource> managed, String currentId) throws Exception {
            String sql = """
                SELECT
                  (SELECT COUNT(*) FROM rn_competition_mapping m
                   JOIN rn_competition_season s ON s.competition_season_id=m.competition_season_id
                   JOIN rn_source_file sf ON sf.source_file_id=s.source_file_id
                   WHERE s.season_id=? AND m.mapping_status='DA_CONFIGURARE'
                     AND sf.import_id=(SELECT MAX(sf2.import_id) FROM rn_source_file sf2 WHERE sf2.season_id=s.season_id AND sf2.source_type='FCM')) +
                  (SELECT COUNT(*) FROM rn_team_mapping m
                   JOIN rn_team_season s ON s.team_season_id=m.team_season_id
                   JOIN rn_source_file sf ON sf.source_file_id=s.source_file_id
                   WHERE s.season_id=? AND m.mapping_status='DA_CONFIGURARE'
                     AND sf.import_id=(SELECT MAX(sf2.import_id) FROM rn_source_file sf2 WHERE sf2.season_id=s.season_id AND sf2.source_type='FCM'))
                """;
            try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + database);
                 PreparedStatement ps = c.prepareStatement(sql)) {
                for (SeasonSource season : managed) {
                    if (season.id().equals(currentId)) continue;
                    ps.setString(1, season.id()); ps.setString(2, season.id());
                    try (ResultSet rs = ps.executeQuery()) {
                        int pending = rs.next() ? rs.getInt(1) : 0;
                        if (pending > 0) {
                            throw new IllegalStateException(season.id() + ": restano " + pending
                                + " associazioni da configurare. Aprire Configurazione prima di elaborare.");
                        }
                    }
                }
            }
        }
    
        private static void validateManagedSource(SeasonSource s) {
            if (s.fcm().isBlank() || !Files.isRegularFile(Path.of(s.fcm()))) {
                throw new IllegalStateException(s.id() + ": file FCM non trovato: " + s.fcm());
            }
            if (s.fca().isBlank() || !Files.isRegularFile(Path.of(s.fca()))) {
                throw new IllegalStateException(s.id() + ": file FCA non trovato: " + s.fca());
            }
        }
        private static String elapsed(long started) {
            double seconds = (System.nanoTime() - started) / 1_000_000_000.0;
            return String.format(java.util.Locale.ROOT, "%.3f s", seconds);
        }
    
    }

## src\main\java\it\alterlega\recordsnext\CalendarSourceManager.java

File: src\main\java\it\alterlega\recordsnext\CalendarSourceManager.java

    package it.alterlega.recordsnext;
    
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.security.MessageDigest;
    import java.sql.Connection;
    import java.sql.DriverManager;
    import java.sql.PreparedStatement;
    import java.sql.ResultSet;
    import java.sql.Statement;
    import java.time.Instant;
    import java.util.HexFormat;
    import java.util.Locale;
    import java.util.regex.Matcher;
    import java.util.regex.Pattern;
    
    /**
     * Risolve i file DataA-AAAA.js senza dipendere da ConfrontiStorici.
     *
     * <p>Priorita: cartella esterna configurata, poi data/calendars del progetto.
     * L'importazione effettiva resta affidata a ConfrontiStoriciCalendarImporter,
     * gia validato. Questa classe registra la provenienza per stagione.</p>
     */
    public final class CalendarSourceManager {
    
        private static final String EXTERNAL_DIRECTORY_KEY = "dataa_external_directory";
        private static final Pattern SEASON_PATTERN =
            Pattern.compile("^(\\d{4})_(\\d{4})$");
    
        private CalendarSourceManager() {
        }
    
        public static void main(String[] args) throws Exception {
            if (args.length < 2) {
                usage();
                System.exit(2);
            }
    
            Path database = Path.of(args[0]).toAbsolutePath().normalize();
            if (!Files.isRegularFile(database)) {
                throw new IllegalArgumentException("Database SQLite non trovato: " + database);
            }
    
            String command = args[1].trim().toLowerCase(Locale.ROOT);
            Class.forName("org.sqlite.JDBC");
    
            try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + database)) {
                configure(connection);
                installSchema(connection);
    
                switch (command) {
                    case "set-directory" -> setDirectory(connection, args);
                    case "clear-directory" -> clearDirectory(connection, args);
                    case "resolve" -> resolveCommand(connection, args);
                    case "import" -> importCommand(connection, database, args);
                    case "validate" -> validateCommand(connection, database, args);
                    case "show" -> showCommand(connection, args);
                    default -> {
                        usage();
                        System.exit(2);
                    }
                }
            }
        }
    
        static void installSchema(Connection connection) throws Exception {
            try (Statement statement = connection.createStatement()) {
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS rn_global_configuration (
                        config_key TEXT PRIMARY KEY,
                        config_value TEXT NOT NULL,
                        updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
    
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS rn_calendar_source (
                        season_id TEXT PRIMARY KEY,
                        source_type TEXT NOT NULL
                            CHECK (source_type IN ('USER_DIRECTORY', 'BUNDLED')),
                        source_directory TEXT NOT NULL,
                        source_file TEXT NOT NULL,
                        source_sha256 TEXT NOT NULL,
                        imported_at TEXT NOT NULL,
                        FOREIGN KEY (season_id) REFERENCES rn_season(season_id)
                    )
                    """);
            }
        }
    
        private static void setDirectory(Connection connection, String[] args) throws Exception {
            requireArgCount(args, 3, "<db> set-directory <cartella-DataA>");
            Path directory = Path.of(args[2]).toAbsolutePath().normalize();
            if (!Files.isDirectory(directory)) {
                throw new IllegalArgumentException("Cartella DataA non trovata: " + directory);
            }
    
            try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO rn_global_configuration(config_key, config_value, updated_at)
                VALUES (?, ?, CURRENT_TIMESTAMP)
                ON CONFLICT(config_key) DO UPDATE SET
                    config_value = excluded.config_value,
                    updated_at = CURRENT_TIMESTAMP
                """)) {
                statement.setString(1, EXTERNAL_DIRECTORY_KEY);
                statement.setString(2, directory.toString());
                statement.executeUpdate();
            }
    
            System.out.println("Cartella DataA esterna configurata: " + directory);
        }
    
        private static void clearDirectory(Connection connection, String[] args) throws Exception {
            requireArgCount(args, 2, "<db> clear-directory");
            try (PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM rn_global_configuration WHERE config_key = ?")) {
                statement.setString(1, EXTERNAL_DIRECTORY_KEY);
                statement.executeUpdate();
            }
            System.out.println("Cartella DataA esterna rimossa. Verra usato il fallback distribuito.");
        }
    
        private static void resolveCommand(Connection connection, String[] args) throws Exception {
            requireArgCount(args, 4, "<db> resolve <stagione> <project-root>");
            ResolvedSource source = resolve(connection, args[2], Path.of(args[3]));
            printSource(source);
        }
    
        private static void importCommand(
                Connection connection,
                Path database,
                String[] args) throws Exception {
    
            requireArgCount(args, 4, "<db> import <stagione> <project-root>");
            String season = requireSeason(connection, args[2]);
            ResolvedSource source = resolve(connection, season, Path.of(args[3]));
    
            ConfrontiStoriciCalendarImporter.main(new String[] {
                database.toString(), "set-directory", source.directory().toString()
            });
            ConfrontiStoriciCalendarImporter.main(new String[] {
                database.toString(), "import", season
            });
    
            recordSource(connection, season, source);
            printSource(source);
        }
    
        private static void validateCommand(
                Connection connection,
                Path database,
                String[] args) throws Exception {
    
            requireArgCount(args, 4, "<db> validate <stagione> <project-root>");
            String season = requireSeason(connection, args[2]);
            ResolvedSource source = resolve(connection, season, Path.of(args[3]));
    
            ConfrontiStoriciCalendarImporter.main(new String[] {
                database.toString(), "set-directory", source.directory().toString()
            });
            ConfrontiStoriciCalendarImporter.main(new String[] {
                database.toString(), "validate", season
            });
    
            verifyRecordedSource(connection, season, source);
            printSource(source);
        }
    
        private static void showCommand(Connection connection, String[] args) throws Exception {
            requireArgCount(args, 3, "<db> show <stagione>");
            String season = requireSeason(connection, args[2]);
    
            try (PreparedStatement statement = connection.prepareStatement("""
                SELECT source_type, source_directory, source_file,
                       source_sha256, imported_at
                FROM rn_calendar_source
                WHERE season_id = ?
                """)) {
                statement.setString(1, season);
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        System.out.println("Nessuna sorgente calendario registrata per " + season);
                        return;
                    }
                    System.out.println("Stagione : " + season);
                    System.out.println("Origine  : " + result.getString("source_type"));
                    System.out.println("Cartella : " + result.getString("source_directory"));
                    System.out.println("File     : " + result.getString("source_file"));
                    System.out.println("SHA-256  : " + result.getString("source_sha256"));
                    System.out.println("Importato: " + result.getString("imported_at"));
                }
            }
        }
    
        private static ResolvedSource resolve(
                Connection connection,
                String seasonValue,
                Path projectRootValue) throws Exception {
    
            String season = requireSeason(connection, seasonValue);
            int startYear = startYear(season);
            String fileName = "DataA-" + startYear + ".js";
    
            Path external = readExternalDirectory(connection);
            if (external != null) {
                Path candidate = external.resolve(fileName).toAbsolutePath().normalize();
                if (Files.isRegularFile(candidate)) {
                    return new ResolvedSource(
                        "USER_DIRECTORY", external, candidate, sha256(candidate)
                    );
                }
            }
    
            Path projectRoot = projectRootValue.toAbsolutePath().normalize();
            Path bundledDirectory = projectRoot.resolve("data").resolve("calendars");
            Path bundled = bundledDirectory.resolve(fileName).normalize();
            if (Files.isRegularFile(bundled)) {
                return new ResolvedSource(
                    "BUNDLED", bundledDirectory, bundled, sha256(bundled)
                );
            }
    
            StringBuilder message = new StringBuilder("DataA non trovato per ")
                .append(season).append(". Atteso: ").append(fileName);
            if (external != null) {
                message.append(" in ").append(external);
            }
            message.append(" oppure in ").append(bundledDirectory);
            throw new IllegalArgumentException(message.toString());
        }
    
        private static Path readExternalDirectory(Connection connection) throws Exception {
            try (PreparedStatement statement = connection.prepareStatement("""
                SELECT config_value
                FROM rn_global_configuration
                WHERE config_key = ?
                """)) {
                statement.setString(1, EXTERNAL_DIRECTORY_KEY);
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        return null;
                    }
                    Path directory = Path.of(result.getString(1))
                        .toAbsolutePath().normalize();
                    return Files.isDirectory(directory) ? directory : null;
                }
            }
        }
    
        private static void recordSource(
                Connection connection,
                String season,
                ResolvedSource source) throws Exception {
    
            try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO rn_calendar_source (
                    season_id, source_type, source_directory,
                    source_file, source_sha256, imported_at
                ) VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT(season_id) DO UPDATE SET
                    source_type = excluded.source_type,
                    source_directory = excluded.source_directory,
                    source_file = excluded.source_file,
                    source_sha256 = excluded.source_sha256,
                    imported_at = excluded.imported_at
                """)) {
                statement.setString(1, season);
                statement.setString(2, source.type());
                statement.setString(3, source.directory().toString());
                statement.setString(4, source.file().toString());
                statement.setString(5, source.sha256());
                statement.setString(6, Instant.now().toString());
                statement.executeUpdate();
            }
        }
    
        private static void verifyRecordedSource(
                Connection connection,
                String season,
                ResolvedSource source) throws Exception {
    
            try (PreparedStatement statement = connection.prepareStatement("""
                SELECT source_type, source_file, source_sha256
                FROM rn_calendar_source
                WHERE season_id = ?
                """)) {
                statement.setString(1, season);
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        throw new IllegalStateException(
                            "Sorgente calendario non registrata per " + season
                        );
                    }
                    if (!source.type().equals(result.getString("source_type"))
                            || !source.file().toString().equals(result.getString("source_file"))
                            || !source.sha256().equals(result.getString("source_sha256"))) {
                        throw new IllegalStateException(
                            "La sorgente calendario corrente differisce da quella importata per "
                                + season
                        );
                    }
                }
            }
        }
    
        private static String requireSeason(Connection connection, String value) throws Exception {
            String season = value.trim();
            Matcher matcher = SEASON_PATTERN.matcher(season);
            if (!matcher.matches()) {
                throw new IllegalArgumentException(
                    "Formato stagione non valido, atteso AAAA_AAAA: " + season
                );
            }
            int start = Integer.parseInt(matcher.group(1));
            int end = Integer.parseInt(matcher.group(2));
            if (end != start + 1) {
                throw new IllegalArgumentException("Stagione non consecutiva: " + season);
            }
    
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT COUNT(*) FROM rn_season WHERE season_id = ?")) {
                statement.setString(1, season);
                try (ResultSet result = statement.executeQuery()) {
                    result.next();
                    if (result.getInt(1) != 1) {
                        throw new IllegalArgumentException("Stagione non trovata: " + season);
                    }
                }
            }
            return season;
        }
    
        private static int startYear(String season) {
            return Integer.parseInt(season.substring(0, 4));
        }
    
        private static String sha256(Path file) throws Exception {
            byte[] bytes = Files.readAllBytes(file);
            return HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(bytes)
            );
        }
    
        private static void printSource(ResolvedSource source) {
            System.out.println("Origine  : " + source.type());
            System.out.println("Cartella : " + source.directory());
            System.out.println("File     : " + source.file());
            System.out.println("SHA-256  : " + source.sha256());
        }
    
        private static void configure(Connection connection) throws Exception {
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");
                statement.execute("PRAGMA busy_timeout = 10000");
            }
        }
    
        private static void requireArgCount(String[] args, int expected, String usage) {
            if (args.length != expected) {
                throw new IllegalArgumentException("Uso: " + usage);
            }
        }
    
        private static void usage() {
            System.err.println("Comandi:");
            System.err.println("  <db> set-directory <cartella-DataA>");
            System.err.println("  <db> clear-directory");
            System.err.println("  <db> resolve <stagione> <project-root>");
            System.err.println("  <db> import <stagione> <project-root>");
            System.err.println("  <db> validate <stagione> <project-root>");
            System.err.println("  <db> show <stagione>");
        }
    
        private record ResolvedSource(
            String type,
            Path directory,
            Path file,
            String sha256
        ) {
        }
    }

## src\main\java\it\alterlega\recordsnext\CanonicalSchemaProbe.java

File: src\main\java\it\alterlega\recordsnext\CanonicalSchemaProbe.java

    package it.alterlega.recordsnext;
    
    import java.nio.file.Path;
    import java.sql.Connection;
    import java.sql.DriverManager;
    import java.sql.ResultSet;
    import java.sql.ResultSetMetaData;
    import java.sql.Statement;
    import java.util.List;
    
    public final class CanonicalSchemaProbe {
    
        private static final List<String> TABLES = List.of(
            "raw_2025_2026_fcm_competizione",
            "raw_2025_2026_fcm_girone",
            "raw_2025_2026_fcm_giornata",
            "raw_2025_2026_fcm_fantasquadra",
            "raw_2025_2026_fcm_incontro",
            "raw_2025_2026_fcm_formazione",
            "raw_2025_2026_fcm_tabellino",
            "raw_2025_2026_fca_giocatorea",
            "raw_2025_2026_fca_giocain",
            "raw_2025_2026_fca_punteggio"
        );
    
        private CanonicalSchemaProbe() {
        }
    
        public static void main(String[] args) throws Exception {
            if (args.length != 1) {
                System.err.println("Uso: CanonicalSchemaProbe <recordsnext.db>");
                System.exit(2);
            }
    
            Path database = Path.of(args[0]).toAbsolutePath().normalize();
    
            Class.forName("org.sqlite.JDBC");
    
            try (Connection connection = DriverManager.getConnection(
                    "jdbc:sqlite:" + database)) {
    
                for (String table : TABLES) {
                    printTable(connection, table);
                }
            }
        }
    
        private static void printTable(
                Connection connection,
                String table) throws Exception {
    
            System.out.println();
            System.out.println("==================================================");
            System.out.println(table);
            System.out.println("==================================================");
    
            try (Statement statement = connection.createStatement();
                 ResultSet columns = statement.executeQuery(
                     "PRAGMA table_info(\"" + table.replace("\"", "\"\"") + "\")"
                 )) {
    
                System.out.println("COLONNE:");
    
                while (columns.next()) {
                    System.out.printf(
                        "%3d  %-35s %s%n",
                        columns.getInt("cid"),
                        columns.getString("name"),
                        columns.getString("type")
                    );
                }
            }
    
            String sql = "SELECT * FROM \""
                + table.replace("\"", "\"\"")
                + "\" LIMIT 1";
    
            try (Statement statement = connection.createStatement();
                 ResultSet row = statement.executeQuery(sql)) {
    
                if (!row.next()) {
                    System.out.println("TABELLA VUOTA");
                    return;
                }
    
                ResultSetMetaData metadata = row.getMetaData();
    
                System.out.println();
                System.out.println("PRIMA RIGA:");
    
                for (int index = 1; index <= metadata.getColumnCount(); index++) {
                    Object value = row.getObject(index);
    
                    System.out.printf(
                        "%-35s = %s%n",
                        metadata.getColumnName(index),
                        value == null ? "<NULL>" : String.valueOf(value)
                    );
                }
            }
        }
    }

## src\main\java\it\alterlega\recordsnext\CanonicalViews.java

File: src\main\java\it\alterlega\recordsnext\CanonicalViews.java

    package it.alterlega.recordsnext;
    
    import java.nio.file.Path;
    import java.sql.Connection;
    import java.sql.DriverManager;
    import java.sql.PreparedStatement;
    import java.sql.ResultSet;
    import java.sql.Statement;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.Locale;
    
    public final class CanonicalViews {
    
        private CanonicalViews() {
        }
    
        public static void main(String[] args) throws Exception {
            if (args.length != 1) {
                System.err.println(
                    "Uso: CanonicalViews <recordsnext.db>"
                );
                System.exit(2);
            }
    
            Path database = Path.of(args[0])
                .toAbsolutePath()
                .normalize();
    
            Class.forName("org.sqlite.JDBC");
    
            try (Connection connection = DriverManager.getConnection(
                    "jdbc:sqlite:" + database)) {
    
                connection.setAutoCommit(false);
    
                try {
                    createViews(connection);
                    connection.commit();
                    printAudit(connection);
                } catch (Exception exception) {
                    connection.rollback();
                    throw exception;
                }
            }
        }
    
        private static void createViews(Connection connection)
                throws Exception {
    
            dropCanonicalViews(connection);
            createConfiguredEntityViews(connection);
    
            List<String> seasonEventViews =
                createSeasonEventViews(connection);
    
            createUnionViews(
                connection,
                seasonEventViews
            );
        }
    
        private static void dropCanonicalViews(
                Connection connection) throws Exception {
    
            List<String> generatedViews = new ArrayList<>();
    
            try (
                Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery("""
                    SELECT name
                    FROM sqlite_master
                    WHERE type = 'view'
                      AND (
                          name LIKE 'rn_event_%'
                          OR name LIKE 'rn_match_%'
                      )
                    ORDER BY name
                    """)
            ) {
                while (result.next()) {
                    generatedViews.add(
                        result.getString("name")
                    );
                }
            }
    
            try (Statement statement = connection.createStatement()) {
                statement.execute(
                    "DROP VIEW IF EXISTS rn_playoff_result"
                );
    
                statement.execute(
                    "DROP VIEW IF EXISTS rn_team_match"
                );
    
                statement.execute(
                    "DROP VIEW IF EXISTS rn_team_event"
                );
    
                statement.execute(
                    "DROP VIEW IF EXISTS rn_match"
                );
    
                statement.execute(
                    "DROP VIEW IF EXISTS rn_event"
                );
    
                for (String viewName : generatedViews) {
                    statement.execute(
                        "DROP VIEW IF EXISTS "
                            + quoteIdentifier(viewName)
                    );
                }
    
                statement.execute(
                    "DROP VIEW IF EXISTS rn_configured_team"
                );
    
                statement.execute(
                    "DROP VIEW IF EXISTS rn_configured_competition"
                );
            }
        }
    
        private static void createConfiguredEntityViews(
                Connection connection) throws Exception {
    
            try (Statement statement = connection.createStatement()) {
                statement.execute("""
                    CREATE VIEW rn_configured_competition AS
                    SELECT
                        cs.competition_season_id,
                        cs.season_id,
                        cs.source_file_id,
                        cs.source_competition_id,
                        cs.source_name,
                        cs.normalized_name,
                        cm.competition_identity_id,
                        ci.canonical_name,
                        cm.mapping_status,
                        cm.mapping_method,
                        cm.notes
                    FROM rn_competition_season cs
                    JOIN rn_competition_mapping cm
                      ON cm.competition_season_id =
                         cs.competition_season_id
                    LEFT JOIN rn_competition_identity ci
                      ON ci.competition_identity_id =
                         cm.competition_identity_id
                    """);
    
                statement.execute("""
                    CREATE VIEW rn_configured_team AS
                    SELECT
                        ts.team_season_id,
                        ts.season_id,
                        ts.source_file_id,
                        ts.source_team_id,
                        ts.source_name,
                        ts.normalized_name,
                        ts.source_division_id,
                        ts.source_team_number,
                        tm.team_identity_id,
                        ti.canonical_name,
                        tm.mapping_status,
                        tm.mapping_method,
                        tm.notes
                    FROM rn_team_season ts
                    JOIN rn_team_mapping tm
                      ON tm.team_season_id =
                         ts.team_season_id
                    LEFT JOIN rn_team_identity ti
                      ON ti.team_identity_id =
                         tm.team_identity_id
                    """);
            }
        }
    
        private static List<String> createSeasonEventViews(
                Connection connection) throws Exception {
    
            List<FcmSource> sources = readFcmSources(connection);
            List<String> generatedViews = new ArrayList<>();
    
            for (FcmSource source : sources) {
                String incontroTable = rawTable(
                    connection,
                    source.importId(),
                    "INCONTRO"
                );
    
                String gironeTable = rawTable(
                    connection,
                    source.importId(),
                    "GIRONE"
                );
    
                String giornataTable = rawTable(
                    connection,
                    source.importId(),
                    "GIORNATA"
                );
    
                String viewName =
                    "rn_event_"
                        + normalizeIdentifier(
                            source.seasonId()
                        )
                        + "_"
                        + source.importId();
    
                createSeasonEventView(
                    connection,
                    source,
                    viewName,
                    incontroTable,
                    gironeTable,
                    giornataTable
                );
    
                generatedViews.add(viewName);
            }
    
            if (generatedViews.isEmpty()) {
                throw new IllegalStateException(
                    "Nessuna sorgente FCM configurata."
                );
            }
    
            return generatedViews;
        }
    
        private static List<FcmSource> readFcmSources(
                Connection connection) throws Exception {
    
            List<FcmSource> sources = new ArrayList<>();
    
            String sql = """
                SELECT
                    source_file_id,
                    import_id,
                    season_id
                FROM rn_source_file sf
                WHERE source_type = 'FCM'
                  AND sf.import_id = (
                      SELECT MAX(sf2.import_id)
                      FROM rn_source_file sf2
                      WHERE sf2.season_id=sf.season_id
                        AND sf2.source_type='FCM'
                  )
                ORDER BY season_id, import_id
                """;
    
            try (
                Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery(sql)
            ) {
                while (result.next()) {
                    sources.add(
                        new FcmSource(
                            result.getLong("source_file_id"),
                            result.getLong("import_id"),
                            result.getString("season_id")
                        )
                    );
                }
            }
    
            return sources;
        }
    
        private static void createSeasonEventView(
                Connection connection,
                FcmSource source,
                String viewName,
                String incontroTable,
                String gironeTable,
                String giornataTable) throws Exception {
    
            String sql = """
                CREATE VIEW %s AS
                WITH rounds AS (
                    SELECT
                        g.ID AS source_group_id,
                        i.IDGIORNATA AS source_round_id,
                        MIN(i.ID) AS first_event_id,
                        ROW_NUMBER() OVER (
                            PARTITION BY g.ID
                            ORDER BY MIN(i.ID)
                        ) AS competition_round
                    FROM %s i
                    JOIN %s g
                      ON g.ID = i.IDGIRONE
                    JOIN rn_configured_competition cc
                      ON cc.source_file_id = %d
                     AND cc.source_competition_id =
                         g.IDCOMPETIZIONE
                     AND cc.mapping_status = 'ASSOCIATA'
                    WHERE i.GIOCATO <> 0
                      AND i.IDCASA <> 0
                    GROUP BY
                        g.ID,
                        i.IDGIORNATA
                )
                SELECT
                    '%s' AS season_id,
                    %d AS source_file_id,
    
                    cc.competition_identity_id,
                    cc.canonical_name AS competition_name,
                    cc.source_competition_id,
    
                    g.ID AS source_group_id,
                    g.NOME AS source_group_name,
    
                    i.ID AS source_event_id,
    
                    r.competition_round,
                    i.GIORNATADIA AS serie_a_round,
                    i.IDGIORNATA AS source_round_id,
                    gio."DESC" AS round_description,
    
                    i.IDTIPO AS source_match_type_id,
    
                    i.IDCASA AS home_source_team_id,
                    home.team_identity_id
                        AS home_team_identity_id,
                    COALESCE(
                        home.canonical_name,
                        home.source_name
                    ) AS home_team_name,
    
                    i.IDFUORI AS away_source_team_id,
                    away.team_identity_id
                        AS away_team_identity_id,
                    CASE
                        WHEN i.IDFUORI = 0 THEN NULL
                        ELSE COALESCE(
                            away.canonical_name,
                            away.source_name
                        )
                    END AS away_team_name,
    
                    i.PARZCASA AS home_partial_score,
                    i.PARZFUORI AS away_partial_score,
    
                    i.TOTCASA AS home_total_score,
                    i.TOTFUORI AS away_total_score,
    
                    i.GOLCASA AS home_goals,
                    i.GOLFUORI AS away_goals,
    
                    i.GIOCATO AS played,
    
                    CASE
                        WHEN i.IDFUORI <> 0
                            THEN 'HEAD_TO_HEAD'
    
                        WHEN i.TOTCASA <> 0
                          OR i.PARZCASA <> 0
                            THEN 'SCORE_ONLY'
    
                        ELSE 'REST'
                    END AS event_type
    
                FROM %s i
    
                JOIN %s g
                  ON g.ID = i.IDGIRONE
    
                JOIN rn_configured_competition cc
                  ON cc.source_file_id = %d
                 AND cc.source_competition_id =
                     g.IDCOMPETIZIONE
                 AND cc.mapping_status = 'ASSOCIATA'
    
                JOIN rn_configured_team home
                  ON home.source_file_id = %d
                 AND home.source_team_id = i.IDCASA
                 AND home.mapping_status = 'ASSOCIATA'
    
                LEFT JOIN rn_configured_team away
                  ON away.source_file_id = %d
                 AND away.source_team_id = i.IDFUORI
                 AND away.mapping_status = 'ASSOCIATA'
    
                LEFT JOIN %s gio
                  ON gio.ID = i.IDGIORNATA
    
                JOIN rounds r
                  ON r.source_group_id = g.ID
                 AND r.source_round_id = i.IDGIORNATA
    
                WHERE i.GIOCATO <> 0
                  AND i.IDCASA <> 0
                  AND (
                      i.IDFUORI = 0
                      OR away.team_identity_id IS NOT NULL
                  )
                """.formatted(
                    quoteIdentifier(viewName),
                    quoteIdentifier(incontroTable),
                    quoteIdentifier(gironeTable),
                    source.sourceFileId(),
                    escapeSqlLiteral(source.seasonId()),
                    source.sourceFileId(),
                    quoteIdentifier(incontroTable),
                    quoteIdentifier(gironeTable),
                    source.sourceFileId(),
                    source.sourceFileId(),
                    source.sourceFileId(),
                    quoteIdentifier(giornataTable)
                );
    
            try (Statement statement = connection.createStatement()) {
                statement.execute(sql);
            }
        }
    
        private static void createUnionViews(
                Connection connection,
                List<String> seasonEventViews) throws Exception {
    
            StringBuilder eventUnion = new StringBuilder();
    
            for (String viewName : seasonEventViews) {
                if (!eventUnion.isEmpty()) {
                    eventUnion.append("\nUNION ALL\n");
                }
    
                eventUnion.append(
                    "SELECT * FROM "
                        + quoteIdentifier(viewName)
                );
            }
    
            try (Statement statement = connection.createStatement()) {
                statement.execute("""
                    CREATE VIEW rn_event AS
                    %s
                    """.formatted(eventUnion));
    
                statement.execute("""
                    CREATE VIEW rn_match AS
                    SELECT
                        season_id,
                        source_file_id,
                        competition_identity_id,
                        competition_name,
                        source_competition_id,
                        source_group_id,
                        source_group_name,
    
                        source_event_id,
                        source_event_id AS source_match_id,
    
                        competition_round,
                        serie_a_round,
                        source_round_id,
                        round_description,
                        source_match_type_id,
    
                        home_source_team_id,
                        home_team_identity_id,
                        home_team_name,
    
                        away_source_team_id,
                        away_team_identity_id,
                        away_team_name,
    
                        home_partial_score,
                        away_partial_score,
                        home_total_score,
                        away_total_score,
                        home_goals,
                        away_goals,
    
                        played
                    FROM rn_event
                    WHERE event_type = 'HEAD_TO_HEAD'
                    """);
    
                statement.execute("""
                    CREATE VIEW rn_team_event AS
    
                    SELECT
                        season_id,
                        source_file_id,
    
                        competition_identity_id,
                        competition_name,
                        source_competition_id,
    
                        source_group_id,
                        source_group_name,
    
                        source_event_id,
    
                        competition_round,
                        serie_a_round,
                        source_round_id,
                        round_description,
                        source_match_type_id,
    
                        event_type,
    
                        home_source_team_id
                            AS source_team_id,
    
                        home_team_identity_id
                            AS team_identity_id,
    
                        home_team_name
                            AS team_name,
    
                        CASE
                            WHEN event_type = 'HEAD_TO_HEAD'
                            THEN away_source_team_id
                            ELSE NULL
                        END AS opponent_source_team_id,
    
                        CASE
                            WHEN event_type = 'HEAD_TO_HEAD'
                            THEN away_team_identity_id
                            ELSE NULL
                        END AS opponent_team_identity_id,
    
                        CASE
                            WHEN event_type = 'HEAD_TO_HEAD'
                            THEN away_team_name
                            ELSE NULL
                        END AS opponent_name,
    
                        CASE
                            WHEN event_type = 'HEAD_TO_HEAD'
                            THEN 'HOME'
                            ELSE 'NEUTRAL'
                        END AS venue,
    
                        home_goals AS goals_for,
    
                        CASE
                            WHEN event_type = 'HEAD_TO_HEAD'
                            THEN away_goals
                            ELSE NULL
                        END AS goals_against,
    
                        home_partial_score AS partial_score_for,
    
                        CASE
                            WHEN event_type = 'HEAD_TO_HEAD'
                            THEN away_partial_score
                            ELSE NULL
                        END AS partial_score_against,
    
                        home_total_score AS score_for,
    
                        CASE
                            WHEN event_type = 'HEAD_TO_HEAD'
                            THEN away_total_score
                            ELSE NULL
                        END AS score_against,
    
                        CASE
                            WHEN event_type <> 'HEAD_TO_HEAD'
                                THEN NULL
    
                            WHEN home_goals > away_goals
                                THEN 'W'
    
                            WHEN home_goals = away_goals
                                THEN 'D'
    
                            ELSE 'L'
                        END AS result
    
                    FROM rn_event
    
                    UNION ALL
    
                    SELECT
                        season_id,
                        source_file_id,
    
                        competition_identity_id,
                        competition_name,
                        source_competition_id,
    
                        source_group_id,
                        source_group_name,
    
                        source_event_id,
    
                        competition_round,
                        serie_a_round,
                        source_round_id,
                        round_description,
                        source_match_type_id,
    
                        event_type,
    
                        away_source_team_id
                            AS source_team_id,
    
                        away_team_identity_id
                            AS team_identity_id,
    
                        away_team_name
                            AS team_name,
    
                        home_source_team_id
                            AS opponent_source_team_id,
    
                        home_team_identity_id
                            AS opponent_team_identity_id,
    
                        home_team_name
                            AS opponent_name,
    
                        'AWAY' AS venue,
    
                        away_goals AS goals_for,
                        home_goals AS goals_against,
    
                        away_partial_score AS partial_score_for,
                        home_partial_score AS partial_score_against,
    
                        away_total_score AS score_for,
                        home_total_score AS score_against,
    
                        CASE
                            WHEN away_goals > home_goals
                                THEN 'W'
    
                            WHEN away_goals = home_goals
                                THEN 'D'
    
                            ELSE 'L'
                        END AS result
    
                    FROM rn_event
                    WHERE event_type = 'HEAD_TO_HEAD'
                    """);
    
                statement.execute("""
                    CREATE VIEW rn_team_match AS
                    SELECT
                        season_id,
                        source_file_id,
    
                        competition_identity_id,
                        competition_name,
                        source_competition_id,
    
                        source_group_id,
                        source_group_name,
    
                        source_event_id,
                        source_event_id AS source_match_id,
    
                        competition_round,
                        serie_a_round,
                        source_round_id,
                        round_description,
                        source_match_type_id,
    
                        source_team_id,
                        team_identity_id,
                        team_name,
    
                        opponent_source_team_id,
                        opponent_team_identity_id,
                        opponent_name,
    
                        venue,
    
                        goals_for,
                        goals_against,
    
                        partial_score_for,
                        partial_score_against,
    
                        score_for,
                        score_against,
    
                        result
                    FROM rn_team_event
                    WHERE event_type = 'HEAD_TO_HEAD'
                    """);
    
                statement.execute("""
                    CREATE VIEW rn_playoff_result AS
                    SELECT
                        current.season_id,
                        current.source_file_id,
    
                        current.competition_identity_id,
                        current.competition_name,
                        current.source_competition_id,
    
                        current.source_group_id,
                        current.source_group_name,
    
                        current.source_round_id,
                        current.round_description,
                        current.serie_a_round,
                        current.competition_round,
    
                        current.source_event_id,
                        current.home_source_team_id
                            AS source_team_id,
                        current.home_team_identity_id
                            AS team_identity_id,
                        current.home_team_name
                            AS team_name,
    
                        opponent.source_event_id
                            AS opponent_source_event_id,
                        opponent.home_source_team_id
                            AS opponent_source_team_id,
                        opponent.home_team_identity_id
                            AS opponent_team_identity_id,
                        opponent.home_team_name
                            AS opponent_name,
    
                        current.home_total_score
                            AS score_for,
                        opponent.home_total_score
                            AS score_against,
    
                        CASE
                            WHEN current.home_total_score >
                                 opponent.home_total_score
                                THEN 'W'
    
                            WHEN current.home_total_score <
                                 opponent.home_total_score
                                THEN 'L'
    
                            ELSE 'D'
                        END AS result
    
                    FROM rn_event current
    
                    JOIN rn_event opponent
                      ON opponent.season_id =
                         current.season_id
                     AND opponent.source_file_id =
                         current.source_file_id
                     AND opponent.competition_identity_id =
                         current.competition_identity_id
                     AND opponent.source_group_id =
                         current.source_group_id
                     AND opponent.source_round_id =
                         current.source_round_id
                     AND opponent.source_event_id <>
                         current.source_event_id
                     AND opponent.event_type = 'SCORE_ONLY'
    
                    WHERE current.event_type = 'SCORE_ONLY'
                      AND UPPER(current.competition_name) =
                          'PLAY OFF - PLAY OUT'
                    """);
            }
        }
    
        private static String rawTable(
                Connection connection,
                long importId,
                String sourceTableName) throws Exception {
    
            String sql = """
                SELECT raw_table_name
                FROM rn_table_catalog
                WHERE import_id = ?
                  AND UPPER(source_table_name) = ?
                """;
    
            try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {
    
                statement.setLong(1, importId);
                statement.setString(
                    2,
                    sourceTableName.toUpperCase(Locale.ROOT)
                );
    
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        throw new IllegalStateException(
                            "Tabella raw mancante: "
                                + sourceTableName
                                + ", import_id="
                                + importId
                        );
                    }
    
                    return result.getString("raw_table_name");
                }
            }
        }
    
        private static void printAudit(
                Connection connection) throws Exception {
    
            System.out.println(
                "Viste canoniche create"
            );
    
            System.out.println();
    
            printCount(
                connection,
                "Stagioni",
                """
                SELECT COUNT(DISTINCT season_id)
                FROM rn_event
                """
            );
    
            printCount(
                connection,
                "Competizioni con eventi",
                """
                SELECT COUNT(
                    DISTINCT competition_identity_id
                )
                FROM rn_event
                """
            );
    
            printCount(
                connection,
                "Eventi totali",
                """
                SELECT COUNT(*)
                FROM rn_event
                """
            );
    
            printCount(
                connection,
                "Scontri diretti",
                """
                SELECT COUNT(*)
                FROM rn_event
                WHERE event_type = 'HEAD_TO_HEAD'
                """
            );
    
            printCount(
                connection,
                "Riposi",
                """
                SELECT COUNT(*)
                FROM rn_event
                WHERE event_type = 'REST'
                """
            );
    
            printCount(
                connection,
                "Punteggi puri",
                """
                SELECT COUNT(*)
                FROM rn_event
                WHERE event_type = 'SCORE_ONLY'
                """
            );
    
            printCount(
                connection,
                "Partecipazioni",
                """
                SELECT COUNT(*)
                FROM rn_team_event
                """
            );
    
            printCount(
                connection,
                "Righe squadra match",
                """
                SELECT COUNT(*)
                FROM rn_team_match
                """
            );
    
            printCount(
                connection,
                "Righe play off/out",
                """
                SELECT COUNT(*)
                FROM rn_playoff_result
                """
            );
    
            System.out.println();
            System.out.println("=== ESITI SCONTRI DIRETTI ===");
    
            printCount(
                connection,
                "Vittorie",
                """
                SELECT COUNT(*)
                FROM rn_team_match
                WHERE result = 'W'
                """
            );
    
            printCount(
                connection,
                "Pareggi",
                """
                SELECT COUNT(*)
                FROM rn_team_match
                WHERE result = 'D'
                """
            );
    
            printCount(
                connection,
                "Sconfitte",
                """
                SELECT COUNT(*)
                FROM rn_team_match
                WHERE result = 'L'
                """
            );
    
            System.out.println();
            System.out.println("=== ESITI PLAY OFF / PLAY OUT ===");
    
            printCount(
                connection,
                "Vinti",
                """
                SELECT COUNT(*)
                FROM rn_playoff_result
                WHERE result = 'W'
                """
            );
    
            printCount(
                connection,
                "Persi",
                """
                SELECT COUNT(*)
                FROM rn_playoff_result
                WHERE result = 'L'
                """
            );
    
            printCount(
                connection,
                "Pari",
                """
                SELECT COUNT(*)
                FROM rn_playoff_result
                WHERE result = 'D'
                """
            );
    
            System.out.println();
            System.out.println("=== EVENTI PER TIPO ===");
    
            try (
                Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery("""
                    SELECT
                        event_type,
                        COUNT(*) AS event_count
                    FROM rn_event
                    GROUP BY event_type
                    ORDER BY event_type
                    """)
            ) {
                while (result.next()) {
                    System.out.printf(
                        Locale.ROOT,
                        "%-16s: %d%n",
                        result.getString("event_type"),
                        result.getLong("event_count")
                    );
                }
            }
        }
    
        private static void printCount(
                Connection connection,
                String label,
                String sql) throws Exception {
    
            try (
                Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery(sql)
            ) {
                result.next();
    
                System.out.printf(
                    Locale.ROOT,
                    "%-24s: %d%n",
                    label,
                    result.getLong(1)
                );
            }
        }
    
        private static String normalizeIdentifier(String value) {
            String normalized = value
                .trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    
            if (normalized.isBlank()) {
                throw new IllegalArgumentException(
                    "Identificatore non valido: " + value
                );
            }
    
            return normalized;
        }
    
        private static String quoteIdentifier(String value) {
            return "\""
                + value.replace("\"", "\"\"")
                + "\"";
        }
    
        private static String escapeSqlLiteral(String value) {
            return value.replace("'", "''");
        }
    
        private record FcmSource(
            long sourceFileId,
            long importId,
            String seasonId
        ) {
        }
    }

## src\main\java\it\alterlega\recordsnext\ConfigurationSchema.java

File: src\main\java\it\alterlega\recordsnext\ConfigurationSchema.java

    package it.alterlega.recordsnext;
    
    import java.nio.file.Path;
    import java.sql.Connection;
    import java.sql.DriverManager;
    import java.sql.PreparedStatement;
    import java.sql.ResultSet;
    import java.sql.Statement;
    import java.time.Instant;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.Locale;
    
    public final class ConfigurationSchema {
    
        private ConfigurationSchema() {
        }
    
        /**
         * Crea lo schema RecordsNext vuoto per una nuova installazione.
         * Non richiede ancora una stagione-ancora e non importa dati.
         */
        public static void initializeEmpty(Path database) throws Exception {
            Path normalized = database.toAbsolutePath().normalize();
            Path parent = normalized.getParent();
            if (parent != null) {
                java.nio.file.Files.createDirectories(parent);
            }
    
            Class.forName("org.sqlite.JDBC");
            try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + normalized)) {
                connection.setAutoCommit(false);
                try {
                    configureConnection(connection);
                    createSchema(connection);
                    connection.commit();
                } catch (Exception exception) {
                    connection.rollback();
                    throw exception;
                }
            }
        }
    
        public static void main(String[] args) throws Exception {
            if (args.length != 2) {
                System.err.println(
                    "Uso: ConfigurationSchema <recordsnext.db> <stagione-ancora>"
                );
                System.exit(2);
            }
    
            Path database = Path.of(args[0]).toAbsolutePath().normalize();
            String anchorSeason = args[1].trim();
    
            if (anchorSeason.isBlank()) {
                throw new IllegalArgumentException(
                    "La stagione-ancora non può essere vuota."
                );
            }
    
            Class.forName("org.sqlite.JDBC");
    
            try (Connection connection = DriverManager.getConnection(
                    "jdbc:sqlite:" + database)) {
    
                connection.setAutoCommit(false);
    
                try {
                    configureConnection(connection);
                    createSchema(connection);
                    importSeasonsAndSources(connection);
                    setAnchorSeason(connection, anchorSeason);
                    importSeasonEntities(connection);
                    createAnchorIdentities(connection, anchorSeason);
                    initializeHistoricalMappings(connection, anchorSeason);
    
                    connection.commit();
    
                    printSummary(connection, anchorSeason);
                } catch (Exception exception) {
                    connection.rollback();
                    throw exception;
                }
            }
        }
    
        private static void configureConnection(Connection connection)
                throws Exception {
    
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");
                statement.execute("PRAGMA busy_timeout = 10000");
            }
        }
    
        private static void createSchema(Connection connection)
                throws Exception {
    
            try (Statement statement = connection.createStatement()) {
    
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS rn_season (
                        season_id TEXT PRIMARY KEY,
                        display_name TEXT NOT NULL,
                        sort_order INTEGER,
                        is_anchor INTEGER NOT NULL DEFAULT 0
                            CHECK (is_anchor IN (0, 1)),
                        created_at TEXT NOT NULL,
                        updated_at TEXT NOT NULL
                    )
                    """);
    
                statement.execute("""
                    CREATE UNIQUE INDEX IF NOT EXISTS
                        ux_rn_season_anchor
                    ON rn_season(is_anchor)
                    WHERE is_anchor = 1
                    """);
    
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS rn_source_file (
                        source_file_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        import_id INTEGER NOT NULL UNIQUE,
                        season_id TEXT NOT NULL,
                        source_type TEXT NOT NULL
                            CHECK (source_type IN ('FCM', 'FCA')),
                        source_path TEXT NOT NULL,
                        source_file_name TEXT NOT NULL,
                        source_size_bytes INTEGER NOT NULL,
                        source_last_modified TEXT NOT NULL,
                        source_sha256 TEXT NOT NULL,
                        imported_at TEXT NOT NULL,
                        FOREIGN KEY (season_id)
                            REFERENCES rn_season(season_id)
                    )
                    """);
    
                statement.execute("""
                    CREATE INDEX IF NOT EXISTS
                        ix_rn_source_file_season_type
                    ON rn_source_file(season_id, source_type)
                    """);
    
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS rn_competition_season (
                        competition_season_id INTEGER
                            PRIMARY KEY AUTOINCREMENT,
                        season_id TEXT NOT NULL,
                        source_file_id INTEGER NOT NULL,
                        source_competition_id INTEGER NOT NULL,
                        source_name TEXT NOT NULL,
                        normalized_name TEXT NOT NULL,
                        discovered_at TEXT NOT NULL,
                        UNIQUE (
                            source_file_id,
                            source_competition_id
                        ),
                        FOREIGN KEY (season_id)
                            REFERENCES rn_season(season_id),
                        FOREIGN KEY (source_file_id)
                            REFERENCES rn_source_file(source_file_id)
                    )
                    """);
    
                statement.execute("""
                    CREATE INDEX IF NOT EXISTS
                        ix_rn_competition_season
                    ON rn_competition_season(season_id)
                    """);
    
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS rn_team_season (
                        team_season_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        season_id TEXT NOT NULL,
                        source_file_id INTEGER NOT NULL,
                        source_team_id INTEGER NOT NULL,
                        source_name TEXT NOT NULL,
                        normalized_name TEXT NOT NULL,
                        source_division_id INTEGER,
                        source_team_number INTEGER,
                        discovered_at TEXT NOT NULL,
                        UNIQUE (
                            source_file_id,
                            source_team_id
                        ),
                        FOREIGN KEY (season_id)
                            REFERENCES rn_season(season_id),
                        FOREIGN KEY (source_file_id)
                            REFERENCES rn_source_file(source_file_id)
                    )
                    """);
    
                statement.execute("""
                    CREATE INDEX IF NOT EXISTS
                        ix_rn_team_season
                    ON rn_team_season(season_id)
                    """);
    
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS rn_competition_identity (
                        competition_identity_id INTEGER
                            PRIMARY KEY AUTOINCREMENT,
                        anchor_season_id TEXT NOT NULL,
                        anchor_competition_season_id INTEGER NOT NULL UNIQUE,
                        canonical_name TEXT NOT NULL,
                        created_at TEXT NOT NULL,
                        FOREIGN KEY (anchor_season_id)
                            REFERENCES rn_season(season_id),
                        FOREIGN KEY (anchor_competition_season_id)
                            REFERENCES rn_competition_season(
                                competition_season_id
                            )
                    )
                    """);
    
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS rn_team_identity (
                        team_identity_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        anchor_season_id TEXT NOT NULL,
                        anchor_team_season_id INTEGER NOT NULL UNIQUE,
                        canonical_name TEXT NOT NULL,
                        created_at TEXT NOT NULL,
                        FOREIGN KEY (anchor_season_id)
                            REFERENCES rn_season(season_id),
                        FOREIGN KEY (anchor_team_season_id)
                            REFERENCES rn_team_season(team_season_id)
                    )
                    """);
    
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS rn_competition_mapping (
                        competition_season_id INTEGER PRIMARY KEY,
                        competition_identity_id INTEGER,
                        mapping_status TEXT NOT NULL
                            CHECK (
                                mapping_status IN (
                                    'DA_CONFIGURARE',
                                    'ASSOCIATA',
                                    'NON_ASSOCIATA',
                                    'ESCLUSA'
                                )
                            ),
                        mapping_method TEXT,
                        notes TEXT,
                        updated_at TEXT NOT NULL,
                        CHECK (
                            (
                                mapping_status = 'ASSOCIATA'
                                AND competition_identity_id IS NOT NULL
                            )
                            OR
                            (
                                mapping_status <> 'ASSOCIATA'
                                AND competition_identity_id IS NULL
                            )
                        ),
                        FOREIGN KEY (competition_season_id)
                            REFERENCES rn_competition_season(
                                competition_season_id
                            ),
                        FOREIGN KEY (competition_identity_id)
                            REFERENCES rn_competition_identity(
                                competition_identity_id
                            )
                    )
                    """);
    
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS rn_team_mapping (
                        team_season_id INTEGER PRIMARY KEY,
                        team_identity_id INTEGER,
                        mapping_status TEXT NOT NULL
                            CHECK (
                                mapping_status IN (
                                    'DA_CONFIGURARE',
                                    'ASSOCIATA',
                                    'NON_ASSOCIATA',
                                    'ESCLUSA'
                                )
                            ),
                        mapping_method TEXT,
                        notes TEXT,
                        updated_at TEXT NOT NULL,
                        CHECK (
                            (
                                mapping_status = 'ASSOCIATA'
                                AND team_identity_id IS NOT NULL
                            )
                            OR
                            (
                                mapping_status <> 'ASSOCIATA'
                                AND team_identity_id IS NULL
                            )
                        ),
                        FOREIGN KEY (team_season_id)
                            REFERENCES rn_team_season(team_season_id),
                        FOREIGN KEY (team_identity_id)
                            REFERENCES rn_team_identity(team_identity_id)
                    )
                    """);
            }
        }
    
        private static void importSeasonsAndSources(
                Connection connection) throws Exception {
    
            String now = Instant.now().toString();
    
            String seasonSql = """
                INSERT INTO rn_season (
                    season_id,
                    display_name,
                    sort_order,
                    is_anchor,
                    created_at,
                    updated_at
                )
                SELECT DISTINCT
                    season_id,
                    season_id,
                    NULL,
                    0,
                    ?,
                    ?
                FROM rn_import
                WHERE status = 'COMPLETED'
                ON CONFLICT(season_id) DO UPDATE SET
                    updated_at = excluded.updated_at
                """;
    
            try (PreparedStatement statement =
                     connection.prepareStatement(seasonSql)) {
    
                statement.setString(1, now);
                statement.setString(2, now);
                statement.executeUpdate();
            }
    
            String sourceSql = """
                INSERT INTO rn_source_file (
                    import_id,
                    season_id,
                    source_type,
                    source_path,
                    source_file_name,
                    source_size_bytes,
                    source_last_modified,
                    source_sha256,
                    imported_at
                )
                SELECT
                    import_id,
                    season_id,
                    source_type,
                    source_path,
                    source_file_name,
                    source_size_bytes,
                    source_last_modified,
                    source_sha256,
                    COALESCE(completed_at, started_at)
                FROM rn_import i
                WHERE i.status = 'COMPLETED'
                  AND i.import_id = (
                      SELECT MAX(i2.import_id)
                      FROM rn_import i2
                      WHERE i2.season_id=i.season_id
                        AND i2.source_type=i.source_type
                        AND i2.status='COMPLETED'
                  )
                ON CONFLICT(import_id) DO UPDATE SET
                    season_id = excluded.season_id,
                    source_type = excluded.source_type,
                    source_path = excluded.source_path,
                    source_file_name = excluded.source_file_name,
                    source_size_bytes = excluded.source_size_bytes,
                    source_last_modified = excluded.source_last_modified,
                    source_sha256 = excluded.source_sha256,
                    imported_at = excluded.imported_at
                """;
    
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(sourceSql);
            }
        }
    
        private static void setAnchorSeason(
                Connection connection,
                String anchorSeason) throws Exception {
    
            try (PreparedStatement check = connection.prepareStatement(
                    "SELECT COUNT(*) FROM rn_season WHERE season_id = ?")) {
    
                check.setString(1, anchorSeason);
    
                try (ResultSet result = check.executeQuery()) {
                    result.next();
    
                    if (result.getInt(1) != 1) {
                        throw new IllegalArgumentException(
                            "Stagione-ancora non trovata: " + anchorSeason
                        );
                    }
                }
            }
    
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(
                    "UPDATE rn_season SET is_anchor = 0"
                );
            }
    
            try (PreparedStatement statement = connection.prepareStatement(
                    """
                    UPDATE rn_season
                    SET is_anchor = 1,
                        updated_at = ?
                    WHERE season_id = ?
                    """)) {
    
                statement.setString(1, Instant.now().toString());
                statement.setString(2, anchorSeason);
                statement.executeUpdate();
            }
        }
    
        private static void importSeasonEntities(
                Connection connection) throws Exception {
    
            List<FcmSource> sources = readFcmSources(connection);
    
            for (FcmSource source : sources) {
                String competitionTable = findRawTable(
                    connection,
                    source.importId(),
                    "COMPETIZIONE"
                );
    
                String teamTable = findRawTable(
                    connection,
                    source.importId(),
                    "FANTASQUADRA"
                );
    
                importCompetitions(
                    connection,
                    source,
                    competitionTable
                );
    
                importTeams(
                    connection,
                    source,
                    teamTable
                );
            }
        }
    
        private static List<FcmSource> readFcmSources(
                Connection connection) throws Exception {
    
            List<FcmSource> sources = new ArrayList<>();
    
            String sql = """
                SELECT
                    source_file_id,
                    import_id,
                    season_id
                FROM rn_source_file sf
                WHERE source_type = 'FCM'
                  AND sf.import_id = (
                      SELECT MAX(sf2.import_id)
                      FROM rn_source_file sf2
                      WHERE sf2.season_id=sf.season_id
                        AND sf2.source_type='FCM'
                  )
                ORDER BY season_id, import_id
                """;
    
            try (Statement statement = connection.createStatement();
                 ResultSet result = statement.executeQuery(sql)) {
    
                while (result.next()) {
                    sources.add(
                        new FcmSource(
                            result.getLong("source_file_id"),
                            result.getLong("import_id"),
                            result.getString("season_id")
                        )
                    );
                }
            }
    
            return sources;
        }
    
        private static String findRawTable(
                Connection connection,
                long importId,
                String sourceTableName) throws Exception {
    
            String sql = """
                SELECT raw_table_name
                FROM rn_table_catalog
                WHERE import_id = ?
                  AND UPPER(source_table_name) = ?
                """;
    
            try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {
    
                statement.setLong(1, importId);
                statement.setString(
                    2,
                    sourceTableName.toUpperCase(Locale.ROOT)
                );
    
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        throw new IllegalStateException(
                            "Tabella raw non trovata per import "
                                + importId
                                + ": "
                                + sourceTableName
                        );
                    }
    
                    return result.getString("raw_table_name");
                }
            }
        }
    
        private static void importCompetitions(
                Connection connection,
                FcmSource source,
                String rawTable) throws Exception {
    
            String sql = """
                INSERT INTO rn_competition_season (
                    season_id,
                    source_file_id,
                    source_competition_id,
                    source_name,
                    normalized_name,
                    discovered_at
                )
                SELECT
                    ?,
                    ?,
                    ID,
                    NOME,
                    LOWER(TRIM(NOME)),
                    ?
                FROM %s
                WHERE ID IS NOT NULL
                  AND NOME IS NOT NULL
                  AND TRIM(NOME) <> ''
                ON CONFLICT(
                    source_file_id,
                    source_competition_id
                ) DO UPDATE SET
                    source_name = excluded.source_name,
                    normalized_name = excluded.normalized_name
                """.formatted(quoteIdentifier(rawTable));
    
            try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {
    
                statement.setString(1, source.seasonId());
                statement.setLong(2, source.sourceFileId());
                statement.setString(3, Instant.now().toString());
                statement.executeUpdate();
            }
        }
    
        private static void importTeams(
                Connection connection,
                FcmSource source,
                String rawTable) throws Exception {
    
            String sql = """
                INSERT INTO rn_team_season (
                    season_id,
                    source_file_id,
                    source_team_id,
                    source_name,
                    normalized_name,
                    source_division_id,
                    source_team_number,
                    discovered_at
                )
                SELECT
                    ?,
                    ?,
                    ID,
                    NOME,
                    LOWER(TRIM(NOME)),
                    IDDIVISIONE,
                    NUMEROSQUADRA,
                    ?
                FROM %s
                WHERE ID IS NOT NULL
                  AND NOME IS NOT NULL
                  AND TRIM(NOME) <> ''
                ON CONFLICT(
                    source_file_id,
                    source_team_id
                ) DO UPDATE SET
                    source_name = excluded.source_name,
                    normalized_name = excluded.normalized_name,
                    source_division_id = excluded.source_division_id,
                    source_team_number = excluded.source_team_number
                """.formatted(quoteIdentifier(rawTable));
    
            try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {
    
                statement.setString(1, source.seasonId());
                statement.setLong(2, source.sourceFileId());
                statement.setString(3, Instant.now().toString());
                statement.executeUpdate();
            }
        }
    
        private static void createAnchorIdentities(
                Connection connection,
                String anchorSeason) throws Exception {
    
            String now = Instant.now().toString();
    
            String competitionIdentitySql = """
                INSERT INTO rn_competition_identity (
                    anchor_season_id,
                    anchor_competition_season_id,
                    canonical_name,
                    created_at
                )
                SELECT
                    season_id,
                    competition_season_id,
                    source_name,
                    ?
                FROM rn_competition_season
                WHERE season_id = ?
                ON CONFLICT(anchor_competition_season_id)
                DO UPDATE SET
                    canonical_name = excluded.canonical_name
                """;
    
            try (PreparedStatement statement =
                     connection.prepareStatement(competitionIdentitySql)) {
    
                statement.setString(1, now);
                statement.setString(2, anchorSeason);
                statement.executeUpdate();
            }
    
            String teamIdentitySql = """
                INSERT INTO rn_team_identity (
                    anchor_season_id,
                    anchor_team_season_id,
                    canonical_name,
                    created_at
                )
                SELECT
                    season_id,
                    team_season_id,
                    source_name,
                    ?
                FROM rn_team_season
                WHERE season_id = ?
                ON CONFLICT(anchor_team_season_id)
                DO UPDATE SET
                    canonical_name = excluded.canonical_name
                """;
    
            try (PreparedStatement statement =
                     connection.prepareStatement(teamIdentitySql)) {
    
                statement.setString(1, now);
                statement.setString(2, anchorSeason);
                statement.executeUpdate();
            }
    
            String anchorCompetitionMappingSql = """
                INSERT INTO rn_competition_mapping (
                    competition_season_id,
                    competition_identity_id,
                    mapping_status,
                    mapping_method,
                    notes,
                    updated_at
                )
                SELECT
                    cs.competition_season_id,
                    ci.competition_identity_id,
                    'ASSOCIATA',
                    'ANCHOR_SELF',
                    NULL,
                    ?
                FROM rn_competition_season cs
                JOIN rn_competition_identity ci
                  ON ci.anchor_competition_season_id =
                     cs.competition_season_id
                WHERE cs.season_id = ?
                ON CONFLICT(competition_season_id)
                DO UPDATE SET
                    competition_identity_id =
                        excluded.competition_identity_id,
                    mapping_status = 'ASSOCIATA',
                    mapping_method = 'ANCHOR_SELF',
                    notes = NULL,
                    updated_at = excluded.updated_at
                """;
    
            try (PreparedStatement statement =
                     connection.prepareStatement(
                         anchorCompetitionMappingSql
                     )) {
    
                statement.setString(1, now);
                statement.setString(2, anchorSeason);
                statement.executeUpdate();
            }
    
            String anchorTeamMappingSql = """
                INSERT INTO rn_team_mapping (
                    team_season_id,
                    team_identity_id,
                    mapping_status,
                    mapping_method,
                    notes,
                    updated_at
                )
                SELECT
                    ts.team_season_id,
                    ti.team_identity_id,
                    'ASSOCIATA',
                    'ANCHOR_SELF',
                    NULL,
                    ?
                FROM rn_team_season ts
                JOIN rn_team_identity ti
                  ON ti.anchor_team_season_id =
                     ts.team_season_id
                WHERE ts.season_id = ?
                ON CONFLICT(team_season_id)
                DO UPDATE SET
                    team_identity_id = excluded.team_identity_id,
                    mapping_status = 'ASSOCIATA',
                    mapping_method = 'ANCHOR_SELF',
                    notes = NULL,
                    updated_at = excluded.updated_at
                """;
    
            try (PreparedStatement statement =
                     connection.prepareStatement(anchorTeamMappingSql)) {
    
                statement.setString(1, now);
                statement.setString(2, anchorSeason);
                statement.executeUpdate();
            }
        }
    
        private static void initializeHistoricalMappings(
                Connection connection,
                String anchorSeason) throws Exception {
    
            String now = Instant.now().toString();
    
            String competitionSql = """
                INSERT INTO rn_competition_mapping (
                    competition_season_id,
                    competition_identity_id,
                    mapping_status,
                    mapping_method,
                    notes,
                    updated_at
                )
                SELECT
                    competition_season_id,
                    NULL,
                    'DA_CONFIGURARE',
                    NULL,
                    NULL,
                    ?
                FROM rn_competition_season
                WHERE season_id <> ?
                ON CONFLICT(competition_season_id) DO NOTHING
                """;
    
            try (PreparedStatement statement =
                     connection.prepareStatement(competitionSql)) {
    
                statement.setString(1, now);
                statement.setString(2, anchorSeason);
                statement.executeUpdate();
            }
    
            String teamSql = """
                INSERT INTO rn_team_mapping (
                    team_season_id,
                    team_identity_id,
                    mapping_status,
                    mapping_method,
                    notes,
                    updated_at
                )
                SELECT
                    team_season_id,
                    NULL,
                    'DA_CONFIGURARE',
                    NULL,
                    NULL,
                    ?
                FROM rn_team_season
                WHERE season_id <> ?
                ON CONFLICT(team_season_id) DO NOTHING
                """;
    
            try (PreparedStatement statement =
                     connection.prepareStatement(teamSql)) {
    
                statement.setString(1, now);
                statement.setString(2, anchorSeason);
                statement.executeUpdate();
            }
        }
    
        private static void printSummary(
                Connection connection,
                String anchorSeason) throws Exception {
    
            System.out.println();
            System.out.println("Configurazione multistagione installata");
            System.out.println("Database       : "
                + connection.getMetaData().getURL());
            System.out.println("Stagione ancora: " + anchorSeason);
            System.out.println();
    
            printCount(
                connection,
                "Stagioni",
                "SELECT COUNT(*) FROM rn_season"
            );
    
            printCount(
                connection,
                "Sorgenti",
                "SELECT COUNT(*) FROM rn_source_file"
            );
    
            printCount(
                connection,
                "Competizioni locali",
                "SELECT COUNT(*) FROM rn_competition_season"
            );
    
            printCount(
                connection,
                "Squadre locali",
                "SELECT COUNT(*) FROM rn_team_season"
            );
    
            printCount(
                connection,
                "Identità competizioni",
                "SELECT COUNT(*) FROM rn_competition_identity"
            );
    
            printCount(
                connection,
                "Identità squadre",
                "SELECT COUNT(*) FROM rn_team_identity"
            );
    
            printCount(
                connection,
                "Competizioni da configurare",
                """
                SELECT COUNT(*)
                FROM rn_competition_mapping
                WHERE mapping_status = 'DA_CONFIGURARE'
                """
            );
    
            printCount(
                connection,
                "Squadre da configurare",
                """
                SELECT COUNT(*)
                FROM rn_team_mapping
                WHERE mapping_status = 'DA_CONFIGURARE'
                """
            );
        }
    
        private static void printCount(
                Connection connection,
                String label,
                String sql) throws Exception {
    
            try (Statement statement = connection.createStatement();
                 ResultSet result = statement.executeQuery(sql)) {
    
                result.next();
    
                System.out.printf(
                    Locale.ROOT,
                    "%-28s: %d%n",
                    label,
                    result.getLong(1)
                );
            }
        }
    
        private static String quoteIdentifier(String value) {
            return "\""
                + value.replace("\"", "\"\"")
                + "\"";
        }
    
        private record FcmSource(
            long sourceFileId,
            long importId,
            String seasonId
        ) {
        }
    }

## src\main\java\it\alterlega\recordsnext\ConfrontiStoriciCalendarImporter.java

File: src\main\java\it\alterlega\recordsnext\ConfrontiStoriciCalendarImporter.java

    package it.alterlega.recordsnext;
    
    import java.io.IOException;
    import java.nio.charset.Charset;
    import java.nio.charset.CodingErrorAction;
    import java.nio.charset.StandardCharsets;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.security.MessageDigest;
    import java.sql.Connection;
    import java.sql.DriverManager;
    import java.sql.PreparedStatement;
    import java.sql.ResultSet;
    import java.sql.Statement;
    import java.time.LocalDate;
    import java.time.LocalDateTime;
    import java.time.LocalTime;
    import java.time.format.DateTimeFormatter;
    import java.time.format.DateTimeParseException;
    import java.util.ArrayList;
    import java.util.Comparator;
    import java.util.HexFormat;
    import java.util.List;
    import java.util.Locale;
    import java.util.Map;
    import java.util.TreeMap;
    import java.util.regex.Matcher;
    import java.util.regex.Pattern;
    
    /**
     * Importa le date delle giornate dai DataA-AAAA.js della configurazione
     * di ConfrontiStorici. Non apre file FCM/FCA e non modifica gli export.
     */
    public final class ConfrontiStoriciCalendarImporter {
    
        private static final String CONFIG_KEY = "confrontistorici_data_directory";
        private static final Pattern SEASON_PATTERN = Pattern.compile("^(\\d{4})_(\\d{4})$");
        private static final Pattern DATE_LINE_PATTERN = Pattern.compile(
            "(?m)^\\s*dataGiornata\\s*\\[\\s*(\\d+)\\s*]\\s*=\\s*[\"']([^\"']+)[\"']\\s*;?\\s*$"
        );
        private static final DateTimeFormatter DATA_A_DATE_FORMAT = new DateTimeFormatterBuilderSafe()
            .dateFormatter();
        private static final DateTimeFormatter DATA_A_DATE_TIME_FORMAT = new DateTimeFormatterBuilderSafe()
            .dateTimeFormatter();
    
        private ConfrontiStoriciCalendarImporter() {
        }
    
        public static void main(String[] args) throws Exception {
            if (args.length < 2) {
                usage();
                System.exit(2);
            }
    
            Path database = Path.of(args[0]).toAbsolutePath().normalize();
            String command = args[1].trim().toLowerCase(Locale.ROOT);
    
            Class.forName("org.sqlite.JDBC");
            try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + database)) {
                configure(connection);
                installSchema(connection);
    
                switch (command) {
                    case "set-directory" -> setDirectory(connection, args);
                    case "resolve" -> resolveCommand(connection, args);
                    case "inspect" -> inspectCommand(connection, args);
                    case "import" -> importCommand(connection, args);
                    case "show" -> showCommand(connection, args);
                    case "validate" -> validateCommand(connection, args);
                    default -> {
                        usage();
                        System.exit(2);
                    }
                }
            }
        }
    
        static void installSchema(Connection connection) throws Exception {
            try (Statement statement = connection.createStatement()) {
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS rn_global_configuration (
                        config_key TEXT PRIMARY KEY,
                        config_value TEXT NOT NULL,
                        updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
    
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS rn_matchday_date (
                        season_id TEXT NOT NULL,
                        serie_a_round INTEGER NOT NULL CHECK (serie_a_round > 0),
                        match_date TEXT NOT NULL,
                        source_path TEXT NOT NULL,
                        source_sha256 TEXT NOT NULL,
                        imported_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        PRIMARY KEY (season_id, serie_a_round),
                        FOREIGN KEY (season_id) REFERENCES rn_season(season_id)
                    )
                    """);
    
                statement.execute("""
                    CREATE INDEX IF NOT EXISTS ix_rn_matchday_date_date
                    ON rn_matchday_date(match_date)
                    """);
            }
    
            addColumnIfMissing(connection, "rn_matchday_date", "match_time", "TEXT");
            addColumnIfMissing(connection, "rn_matchday_date", "match_datetime", "TEXT");
    
            try (Statement statement = connection.createStatement()) {
                statement.execute("""
                    CREATE INDEX IF NOT EXISTS ix_rn_matchday_date_datetime
                    ON rn_matchday_date(match_datetime)
                    """);
            }
        }
    
        private static void addColumnIfMissing(
                Connection connection,
                String table,
                String column,
                String definition) throws Exception {
    
            boolean present = false;
            try (Statement statement = connection.createStatement();
                 ResultSet result = statement.executeQuery("PRAGMA table_info(" + table + ")")) {
                while (result.next()) {
                    if (column.equalsIgnoreCase(result.getString("name"))) {
                        present = true;
                        break;
                    }
                }
            }
    
            if (!present) {
                try (Statement statement = connection.createStatement()) {
                    statement.execute("ALTER TABLE " + table + " ADD COLUMN "
                        + column + " " + definition);
                }
            }
        }
    
        private static void setDirectory(Connection connection, String[] args) throws Exception {
            requireArgCount(args, 3, "<db> set-directory <directory-config-ConfrontiStorici>");
            Path directory = Path.of(args[2]).toAbsolutePath().normalize();
            if (!Files.isDirectory(directory)) {
                throw new IllegalArgumentException("Directory non trovata: " + directory);
            }
    
            try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO rn_global_configuration(config_key, config_value, updated_at)
                VALUES (?, ?, CURRENT_TIMESTAMP)
                ON CONFLICT(config_key) DO UPDATE SET
                    config_value = excluded.config_value,
                    updated_at = CURRENT_TIMESTAMP
                """)) {
                statement.setString(1, CONFIG_KEY);
                statement.setString(2, directory.toString());
                statement.executeUpdate();
            }
    
            System.out.println("Directory ConfrontiStorici configurata: " + directory);
        }
    
        private static void resolveCommand(Connection connection, String[] args) throws Exception {
            requireArgCount(args, 3, "<db> resolve <stagione>");
            String season = requireSeason(connection, args[2]);
            Path file = resolveFile(connection, season);
            System.out.println(file);
            System.out.println(Files.isRegularFile(file) ? "TROVATO" : "MANCANTE");
        }
    
        private static void inspectCommand(Connection connection, String[] args) throws Exception {
            requireArgCount(args, 3, "<db> inspect <stagione>");
            String season = requireSeason(connection, args[2]);
            Inspection inspection = inspect(resolveExistingFile(connection, season), season);
            printInspection(inspection);
        }
    
        private static void importCommand(Connection connection, String[] args) throws Exception {
            requireArgCount(args, 3, "<db> import <stagione>");
            String season = requireSeason(connection, args[2]);
            Inspection inspection = inspect(resolveExistingFile(connection, season), season);
    
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement delete = connection.prepareStatement(
                        "DELETE FROM rn_matchday_date WHERE season_id = ?")) {
                    delete.setString(1, season);
                    delete.executeUpdate();
                }
    
                try (PreparedStatement insert = connection.prepareStatement("""
                    INSERT INTO rn_matchday_date (
                        season_id, serie_a_round, match_date,
                        match_time, match_datetime,
                        source_path, source_sha256, imported_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                    """)) {
                    for (MatchdayDate item : inspection.dates()) {
                        insert.setString(1, season);
                        insert.setInt(2, item.round());
                        insert.setString(3, item.date().toString());
                        insert.setString(4, item.time() == null ? null : item.time().toString());
                        insert.setString(5, item.dateTime() == null
                            ? null : item.dateTime().toString());
                        insert.setString(6, inspection.file().toString());
                        insert.setString(7, inspection.sha256());
                        insert.addBatch();
                    }
                    insert.executeBatch();
                }
    
                connection.commit();
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
    
            System.out.printf(
                Locale.ROOT,
                "Importate %d giornate per %s da %s%n",
                inspection.dates().size(), season, inspection.file()
            );
        }
    
        private static void showCommand(Connection connection, String[] args) throws Exception {
            requireArgCount(args, 3, "<db> show <stagione>");
            String season = requireSeason(connection, args[2]);
    
            try (PreparedStatement statement = connection.prepareStatement("""
                SELECT serie_a_round, match_date, match_time,
                       match_datetime, source_path, source_sha256
                FROM rn_matchday_date
                WHERE season_id = ?
                ORDER BY serie_a_round
                """)) {
                statement.setString(1, season);
                try (ResultSet result = statement.executeQuery()) {
                    int count = 0;
                    while (result.next()) {
                        count++;
                        System.out.printf(
                            Locale.ROOT,
                            "%2d  %s%n",
                            result.getInt("serie_a_round"),
                            result.getString("match_datetime") != null
                                ? result.getString("match_datetime")
                                : result.getString("match_date")
                        );
                    }
                    if (count == 0) {
                        System.out.println("Nessuna data importata per " + season);
                    }
                }
            }
        }
    
        private static void validateCommand(Connection connection, String[] args) throws Exception {
            requireArgCount(args, 3, "<db> validate <stagione>");
            String season = requireSeason(connection, args[2]);
            Path file = resolveExistingFile(connection, season);
            Inspection current = inspect(file, season);
    
            String sql = """
                SELECT COUNT(*) AS total,
                       COUNT(DISTINCT serie_a_round) AS distinct_rounds,
                       MIN(serie_a_round) AS first_round,
                       MAX(serie_a_round) AS last_round,
                       MIN(source_sha256) AS min_hash,
                       MAX(source_sha256) AS max_hash
                FROM rn_matchday_date
                WHERE season_id = ?
                """;
    
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, season);
                try (ResultSet result = statement.executeQuery()) {
                    result.next();
                    int total = result.getInt("total");
                    int distinct = result.getInt("distinct_rounds");
                    int first = result.getInt("first_round");
                    int last = result.getInt("last_round");
                    String minHash = result.getString("min_hash");
                    String maxHash = result.getString("max_hash");
    
                    List<String> errors = new ArrayList<>();
                    if (total == 0) {
                        errors.add("nessuna data importata");
                    }
                    if (total != distinct) {
                        errors.add("giornate duplicate nel database");
                    }
                    if (total > 0 && (first != 1 || last != total)) {
                        errors.add("sequenza database non continua: " + first + ".." + last);
                    }
                    if (total != current.dates().size()) {
                        errors.add("numero date diverso dal file: db=" + total
                            + ", file=" + current.dates().size());
                    }
                    if (minHash != null && (!minHash.equals(maxHash)
                            || !minHash.equals(current.sha256()))) {
                        errors.add("file DataA.js cambiato dopo l'importazione");
                    }
    
                    if (!errors.isEmpty()) {
                        System.out.println(season + " NON VALIDA");
                        errors.forEach(error -> System.out.println("- " + error));
                        System.exit(1);
                    }
                    System.out.println(season + " VALIDA");
                    System.out.println("Giornate: " + total);
                    System.out.println("SHA-256 : " + current.sha256());
                }
            }
        }
    
        private static Inspection inspect(Path file, String season) throws Exception {
            byte[] bytes = Files.readAllBytes(file);
            String text = decode(bytes);
            Map<Integer, MatchdayDate> parsed = new TreeMap<>();
            Matcher matcher = DATE_LINE_PATTERN.matcher(text);
    
            while (matcher.find()) {
                int round = Integer.parseInt(matcher.group(1));
                String rawValue = matcher.group(2).trim();
                MatchdayDate parsedValue;
                try {
                    LocalDateTime dateTime = LocalDateTime.parse(
                        rawValue,
                        DATA_A_DATE_TIME_FORMAT
                    );
                    parsedValue = new MatchdayDate(
                        round,
                        dateTime.toLocalDate(),
                        dateTime.toLocalTime(),
                        dateTime
                    );
                } catch (DateTimeParseException dateTimeException) {
                    try {
                        LocalDate date = LocalDate.parse(rawValue, DATA_A_DATE_FORMAT);
                        parsedValue = new MatchdayDate(round, date, null, null);
                    } catch (DateTimeParseException dateException) {
                        throw new IllegalArgumentException(
                            "Data/ora non valida alla giornata " + round + ": " + rawValue,
                            dateTimeException
                        );
                    }
                }
                MatchdayDate previous = parsed.putIfAbsent(round, parsedValue);
                if (previous != null) {
                    throw new IllegalArgumentException("Giornata duplicata nel file: " + round);
                }
            }
    
            if (parsed.isEmpty()) {
                throw new IllegalArgumentException(
                    "Nessuna assegnazione dataGiornata[n] trovata in " + file
                );
            }
    
            int expected = 1;
            for (int round : parsed.keySet()) {
                if (round != expected) {
                    throw new IllegalArgumentException(
                        "Sequenza giornate non continua: attesa " + expected + ", trovata " + round
                    );
                }
                expected++;
            }
    
            SeasonYears years = parseSeason(season);
            List<MatchdayDate> dates = parsed.values().stream()
                .sorted(Comparator.comparingInt(MatchdayDate::round))
                .toList();
    
            for (MatchdayDate item : dates) {
                int year = item.date().getYear();
                if (year != years.startYear() && year != years.endYear()) {
                    throw new IllegalArgumentException(
                        "Data fuori stagione alla giornata " + item.round() + ": " + item.date()
                    );
                }
            }
    
            return new Inspection(file, sha256(bytes), dates);
        }
    
        private static void printInspection(Inspection inspection) {
            MatchdayDate first = inspection.dates().getFirst();
            MatchdayDate last = inspection.dates().getLast();
            System.out.println("File     : " + inspection.file());
            System.out.println("Giornate : " + inspection.dates().size());
            System.out.println("Prima    : " + first.round() + " -> " + first.displayValue());
            System.out.println("Ultima   : " + last.round() + " -> " + last.displayValue());
            System.out.println("SHA-256  : " + inspection.sha256());
        }
    
        private static Path resolveExistingFile(Connection connection, String season)
                throws Exception {
            Path file = resolveFile(connection, season);
            if (!Files.isRegularFile(file)) {
                throw new IllegalArgumentException("DataA non trovato: " + file);
            }
            return file;
        }
    
        private static Path resolveFile(Connection connection, String season) throws Exception {
            SeasonYears years = parseSeason(season);
            Path directory = configuredDirectory(connection);
            return directory.resolve("DataA-" + years.startYear() + ".js")
                .toAbsolutePath().normalize();
        }
    
        private static Path configuredDirectory(Connection connection) throws Exception {
            try (PreparedStatement statement = connection.prepareStatement("""
                SELECT config_value
                FROM rn_global_configuration
                WHERE config_key = ?
                """)) {
                statement.setString(1, CONFIG_KEY);
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        throw new IllegalStateException(
                            "Directory ConfrontiStorici non configurata. "
                                + "Usare set-directory."
                        );
                    }
                    Path directory = Path.of(result.getString(1))
                        .toAbsolutePath().normalize();
                    if (!Files.isDirectory(directory)) {
                        throw new IllegalStateException(
                            "Directory ConfrontiStorici non disponibile: " + directory
                        );
                    }
                    return directory;
                }
            }
        }
    
        private static String requireSeason(Connection connection, String value) throws Exception {
            String season = value.trim();
            parseSeason(season);
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT COUNT(*) FROM rn_season WHERE season_id = ?")) {
                statement.setString(1, season);
                try (ResultSet result = statement.executeQuery()) {
                    result.next();
                    if (result.getInt(1) != 1) {
                        throw new IllegalArgumentException("Stagione non trovata: " + season);
                    }
                }
            }
            return season;
        }
    
        private static SeasonYears parseSeason(String season) {
            Matcher matcher = SEASON_PATTERN.matcher(season);
            if (!matcher.matches()) {
                throw new IllegalArgumentException(
                    "Formato stagione non valido, atteso AAAA_AAAA: " + season
                );
            }
            int start = Integer.parseInt(matcher.group(1));
            int end = Integer.parseInt(matcher.group(2));
            if (end != start + 1) {
                throw new IllegalArgumentException("Stagione non consecutiva: " + season);
            }
            return new SeasonYears(start, end);
        }
    
        private static String decode(byte[] bytes) throws IOException {
            try {
                return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(java.nio.ByteBuffer.wrap(bytes))
                    .toString();
            } catch (java.nio.charset.CharacterCodingException exception) {
                return Charset.forName("windows-1252").decode(
                    java.nio.ByteBuffer.wrap(bytes)
                ).toString();
            }
        }
    
        private static String sha256(byte[] bytes) throws Exception {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        }
    
        private static void configure(Connection connection) throws Exception {
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");
                statement.execute("PRAGMA busy_timeout = 10000");
            }
        }
    
        private static void requireArgCount(String[] args, int expected, String usage) {
            if (args.length != expected) {
                throw new IllegalArgumentException("Uso: " + usage);
            }
        }
    
        private static void usage() {
            System.err.println("Comandi:");
            System.err.println("  <db> set-directory <directory-config-ConfrontiStorici>");
            System.err.println("  <db> resolve <stagione>");
            System.err.println("  <db> inspect <stagione>");
            System.err.println("  <db> import <stagione>");
            System.err.println("  <db> show <stagione>");
            System.err.println("  <db> validate <stagione>");
        }
    
        private record MatchdayDate(
            int round,
            LocalDate date,
            LocalTime time,
            LocalDateTime dateTime
        ) {
            String displayValue() {
                return dateTime == null ? date.toString() : dateTime.toString();
            }
        }
    
        private record Inspection(Path file, String sha256, List<MatchdayDate> dates) {
        }
    
        private record SeasonYears(int startYear, int endYear) {
        }
    
        /** Isola la costruzione dei formatter per i DataA.js storici. */
        private static final class DateTimeFormatterBuilderSafe {
            DateTimeFormatter dateFormatter() {
                return new java.time.format.DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("MMMM d uuuu")
                    .toFormatter(Locale.ENGLISH);
            }
    
            DateTimeFormatter dateTimeFormatter() {
                return new java.time.format.DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("MMMM d uuuu H:mm")
                    .toFormatter(Locale.ENGLISH);
            }
        }
    }

## src\main\java\it\alterlega\recordsnext\DatabaseInspector.java

File: src\main\java\it\alterlega\recordsnext\DatabaseInspector.java

    package it.alterlega.recordsnext;
    
    import java.io.BufferedWriter;
    import java.io.IOException;
    import java.io.Writer;
    import java.nio.charset.StandardCharsets;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.nio.file.attribute.FileTime;
    import java.security.MessageDigest;
    import java.sql.Connection;
    import java.sql.DatabaseMetaData;
    import java.sql.DriverManager;
    import java.sql.ResultSet;
    import java.sql.Statement;
    import java.time.Instant;
    import java.util.ArrayList;
    import java.util.Comparator;
    import java.util.HexFormat;
    import java.util.LinkedHashMap;
    import java.util.List;
    import java.util.Locale;
    import java.util.Map;
    
    public final class DatabaseInspector {
    
        private DatabaseInspector() {
        }
    
        public static void main(String[] args) throws Exception {
            if (args.length != 2) {
                System.err.println(
                    "Uso: DatabaseInspector <file.fcm|file.fca> <output.json>"
                );
                System.exit(2);
            }
    
            Path database = Path.of(args[0]).toAbsolutePath().normalize();
            Path output = Path.of(args[1]).toAbsolutePath().normalize();
    
            if (!Files.isRegularFile(database)) {
                throw new IllegalArgumentException(
                    "Database non trovato: " + database
                );
            }
    
            Path outputParent = output.getParent();
    
            if (outputParent != null) {
                Files.createDirectories(outputParent);
            }
    
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
    
            long totalStarted = System.nanoTime();
            String jdbcUrl = "jdbc:ucanaccess://" + database;
    
            Map<String, Object> report = new LinkedHashMap<>();
            report.put("schemaVersion", 1);
            report.put("generatedAt", Instant.now().toString());
            report.put("source", inspectSource(database));
    
            try (Connection connection = DriverManager.getConnection(jdbcUrl)) {
                long openedAt = System.nanoTime();
    
                connection.setReadOnly(true);
    
                DatabaseMetaData metadata = connection.getMetaData();
    
                Map<String, Object> driver = new LinkedHashMap<>();
                driver.put("name", metadata.getDriverName());
                driver.put("version", metadata.getDriverVersion());
                driver.put("jdbcMajorVersion", metadata.getJDBCMajorVersion());
                driver.put("jdbcMinorVersion", metadata.getJDBCMinorVersion());
                driver.put("databaseProductName", metadata.getDatabaseProductName());
                driver.put(
                    "databaseProductVersion",
                    metadata.getDatabaseProductVersion()
                );
                report.put("driver", driver);
    
                List<String> tableNames = readTableNames(metadata);
                List<Map<String, Object>> tables = new ArrayList<>();
    
                long totalRows = 0;
                long totalColumns = 0;
    
                for (String tableName : tableNames) {
                    Map<String, Object> table = inspectTable(
                        connection,
                        metadata,
                        tableName
                    );
    
                    totalRows += ((Number) table.get("rowCount")).longValue();
                    totalColumns += ((Number) table.get("columnCount")).longValue();
    
                    tables.add(table);
                }
    
                long finishedAt = System.nanoTime();
    
                Map<String, Object> summary = new LinkedHashMap<>();
                summary.put("tableCount", tables.size());
                summary.put("columnCount", totalColumns);
                summary.put("rowCount", totalRows);
                report.put("summary", summary);
                report.put("tables", tables);
    
                Map<String, Object> timings = new LinkedHashMap<>();
                timings.put(
                    "openMilliseconds",
                    nanosToMilliseconds(openedAt - totalStarted)
                );
                timings.put(
                    "inspectionMilliseconds",
                    nanosToMilliseconds(finishedAt - openedAt)
                );
                timings.put(
                    "totalMilliseconds",
                    nanosToMilliseconds(finishedAt - totalStarted)
                );
                report.put("timings", timings);
            }
    
            try (BufferedWriter writer = Files.newBufferedWriter(
                    output,
                    StandardCharsets.UTF_8)) {
    
                writeJson(report, writer, 0);
                writer.write(System.lineSeparator());
            }
    
            @SuppressWarnings("unchecked")
            Map<String, Object> summary =
                (Map<String, Object>) report.get("summary");
    
            @SuppressWarnings("unchecked")
            Map<String, Object> timings =
                (Map<String, Object>) report.get("timings");
    
            System.out.println();
            System.out.println("Inventario completato");
            System.out.println("Database : " + database);
            System.out.println("Output   : " + output);
            System.out.println("Tabelle  : " + summary.get("tableCount"));
            System.out.println("Colonne  : " + summary.get("columnCount"));
            System.out.println("Righe    : " + summary.get("rowCount"));
            System.out.printf(
                Locale.ROOT,
                "Apertura : %.3f s%n",
                ((Number) timings.get("openMilliseconds")).doubleValue() / 1000.0
            );
            System.out.printf(
                Locale.ROOT,
                "Ispezione: %.3f s%n",
                ((Number) timings.get("inspectionMilliseconds")).doubleValue()
                    / 1000.0
            );
            System.out.printf(
                Locale.ROOT,
                "Totale   : %.3f s%n",
                ((Number) timings.get("totalMilliseconds")).doubleValue() / 1000.0
            );
        }
    
        private static Map<String, Object> inspectSource(Path database)
                throws Exception {
    
            Map<String, Object> source = new LinkedHashMap<>();
    
            source.put("path", database.toString());
            source.put("fileName", database.getFileName().toString());
            source.put("sourceType", detectSourceType(database));
            source.put("sizeBytes", Files.size(database));
    
            FileTime modified = Files.getLastModifiedTime(database);
            source.put("lastModified", modified.toInstant().toString());
            source.put("sha256", sha256(database));
    
            return source;
        }
    
        private static String detectSourceType(Path database) {
            String name = database.getFileName()
                .toString()
                .toLowerCase(Locale.ROOT);
    
            if (name.endsWith(".fcm")) {
                return "FCM";
            }
    
            if (name.endsWith(".fca")) {
                return "FCA";
            }
    
            return "UNKNOWN";
        }
    
        private static List<String> readTableNames(DatabaseMetaData metadata)
                throws Exception {
    
            List<String> tables = new ArrayList<>();
    
            try (ResultSet rs = metadata.getTables(
                    null,
                    null,
                    "%",
                    new String[]{"TABLE"})) {
    
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
    
                    if (tableName != null && !tableName.isBlank()) {
                        tables.add(tableName);
                    }
                }
            }
    
            tables.sort(String.CASE_INSENSITIVE_ORDER);
            return tables;
        }
    
        private static Map<String, Object> inspectTable(
                Connection connection,
                DatabaseMetaData metadata,
                String tableName) throws Exception {
    
            Map<String, Object> table = new LinkedHashMap<>();
    
            long rowCount = countRows(connection, tableName);
            List<Map<String, Object>> columns = readColumns(metadata, tableName);
            List<Map<String, Object>> primaryKeys =
                readPrimaryKeys(metadata, tableName);
            List<Map<String, Object>> indexes = readIndexes(metadata, tableName);
    
            table.put("name", tableName);
            table.put("rowCount", rowCount);
            table.put("columnCount", columns.size());
            table.put("columns", columns);
            table.put("primaryKeys", primaryKeys);
            table.put("indexes", indexes);
    
            return table;
        }
    
        private static long countRows(
                Connection connection,
                String tableName) throws Exception {
    
            String escapedName = tableName.replace("]", "]]");
            String sql = "SELECT COUNT(*) FROM [" + escapedName + "]";
    
            try (Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery(sql)) {
    
                rs.next();
                return rs.getLong(1);
            }
        }
    
        private static List<Map<String, Object>> readColumns(
                DatabaseMetaData metadata,
                String tableName) throws Exception {
    
            List<Map<String, Object>> columns = new ArrayList<>();
    
            try (ResultSet rs = metadata.getColumns(
                    null,
                    null,
                    tableName,
                    "%")) {
    
                while (rs.next()) {
                    Map<String, Object> column = new LinkedHashMap<>();
    
                    int nullableCode = rs.getInt("NULLABLE");
    
                    column.put("name", rs.getString("COLUMN_NAME"));
                    column.put("ordinalPosition", rs.getInt("ORDINAL_POSITION"));
                    column.put("jdbcType", rs.getInt("DATA_TYPE"));
                    column.put("typeName", rs.getString("TYPE_NAME"));
                    column.put("columnSize", rs.getInt("COLUMN_SIZE"));
                    column.put("decimalDigits", nullableInteger(
                        rs,
                        "DECIMAL_DIGITS"
                    ));
                    column.put("numericPrecisionRadix", nullableInteger(
                        rs,
                        "NUM_PREC_RADIX"
                    ));
                    column.put("nullableCode", nullableCode);
                    column.put(
                        "nullable",
                        nullableCode == DatabaseMetaData.columnNullable
                    );
                    column.put("defaultValue", rs.getString("COLUMN_DEF"));
                    column.put("remarks", rs.getString("REMARKS"));
                    column.put(
                        "autoIncrement",
                        safeGetString(rs, "IS_AUTOINCREMENT")
                    );
                    column.put(
                        "generatedColumn",
                        safeGetString(rs, "IS_GENERATEDCOLUMN")
                    );
    
                    columns.add(column);
                }
            }
    
            columns.sort(Comparator.comparingInt(
                item -> ((Number) item.get("ordinalPosition")).intValue()
            ));
    
            return columns;
        }
    
        private static List<Map<String, Object>> readPrimaryKeys(
                DatabaseMetaData metadata,
                String tableName) throws Exception {
    
            List<Map<String, Object>> primaryKeys = new ArrayList<>();
    
            try (ResultSet rs = metadata.getPrimaryKeys(
                    null,
                    null,
                    tableName)) {
    
                while (rs.next()) {
                    Map<String, Object> key = new LinkedHashMap<>();
    
                    key.put("name", rs.getString("PK_NAME"));
                    key.put("columnName", rs.getString("COLUMN_NAME"));
                    key.put("keySequence", rs.getInt("KEY_SEQ"));
    
                    primaryKeys.add(key);
                }
            }
    
            primaryKeys.sort(Comparator.comparingInt(
                item -> ((Number) item.get("keySequence")).intValue()
            ));
    
            return primaryKeys;
        }
    
        private static List<Map<String, Object>> readIndexes(
                DatabaseMetaData metadata,
                String tableName) throws Exception {
    
            List<Map<String, Object>> indexes = new ArrayList<>();
    
            try (ResultSet rs = metadata.getIndexInfo(
                    null,
                    null,
                    tableName,
                    false,
                    false)) {
    
                while (rs.next()) {
                    String indexName = rs.getString("INDEX_NAME");
    
                    if (indexName == null || indexName.isBlank()) {
                        continue;
                    }
    
                    Map<String, Object> index = new LinkedHashMap<>();
    
                    index.put("name", indexName);
                    index.put("unique", !rs.getBoolean("NON_UNIQUE"));
                    index.put("type", rs.getShort("TYPE"));
                    index.put(
                        "ordinalPosition",
                        rs.getShort("ORDINAL_POSITION")
                    );
                    index.put("columnName", rs.getString("COLUMN_NAME"));
                    index.put("sortDirection", rs.getString("ASC_OR_DESC"));
                    index.put("filterCondition", rs.getString("FILTER_CONDITION"));
    
                    indexes.add(index);
                }
            }
    
    indexes.sort(
        Comparator
            .comparing(
                (Map<String, Object> item) ->
                    String.valueOf(item.get("name")),
                String.CASE_INSENSITIVE_ORDER
            )
            .thenComparingInt(
                item ->
                    ((Number) item.get("ordinalPosition")).intValue()
            )
    );
    
            return indexes;
        }
    
        private static Integer nullableInteger(
                ResultSet rs,
                String columnName) throws Exception {
    
            int value = rs.getInt(columnName);
            return rs.wasNull() ? null : value;
        }
    
        private static String safeGetString(
                ResultSet rs,
                String columnName) {
    
            try {
                return rs.getString(columnName);
            } catch (Exception ignored) {
                return null;
            }
        }
    
        private static double nanosToMilliseconds(long nanos) {
            return Math.round((nanos / 1_000_000.0) * 1000.0) / 1000.0;
        }
    
        private static String sha256(Path path) throws Exception {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
    
            try (var input = Files.newInputStream(path)) {
                byte[] buffer = new byte[1024 * 1024];
                int read;
    
                while ((read = input.read(buffer)) >= 0) {
                    digest.update(buffer, 0, read);
                }
            }
    
            return HexFormat.of().formatHex(digest.digest());
        }
    
        private static void writeJson(
                Object value,
                Writer writer,
                int indent) throws IOException {
    
            if (value == null) {
                writer.write("null");
                return;
            }
    
            if (value instanceof String text) {
                writeJsonString(text, writer);
                return;
            }
    
            if (value instanceof Number || value instanceof Boolean) {
                writer.write(String.valueOf(value));
                return;
            }
    
            if (value instanceof Map<?, ?> map) {
                writeJsonMap(map, writer, indent);
                return;
            }
    
            if (value instanceof Iterable<?> iterable) {
                writeJsonArray(iterable, writer, indent);
                return;
            }
    
            writeJsonString(String.valueOf(value), writer);
        }
    
        private static void writeJsonMap(
                Map<?, ?> map,
                Writer writer,
                int indent) throws IOException {
    
            writer.write("{");
    
            if (!map.isEmpty()) {
                writer.write(System.lineSeparator());
    
                int index = 0;
    
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    writeIndent(writer, indent + 1);
                    writeJsonString(String.valueOf(entry.getKey()), writer);
                    writer.write(": ");
                    writeJson(entry.getValue(), writer, indent + 1);
    
                    if (++index < map.size()) {
                        writer.write(",");
                    }
    
                    writer.write(System.lineSeparator());
                }
    
                writeIndent(writer, indent);
            }
    
            writer.write("}");
        }
    
        private static void writeJsonArray(
                Iterable<?> iterable,
                Writer writer,
                int indent) throws IOException {
    
            List<Object> values = new ArrayList<>();
    
            for (Object value : iterable) {
                values.add(value);
            }
    
            writer.write("[");
    
            if (!values.isEmpty()) {
                writer.write(System.lineSeparator());
    
                for (int index = 0; index < values.size(); index++) {
                    writeIndent(writer, indent + 1);
                    writeJson(values.get(index), writer, indent + 1);
    
                    if (index + 1 < values.size()) {
                        writer.write(",");
                    }
    
                    writer.write(System.lineSeparator());
                }
    
                writeIndent(writer, indent);
            }
    
            writer.write("]");
        }
    
        private static void writeJsonString(
                String text,
                Writer writer) throws IOException {
    
            writer.write("\"");
    
            for (int index = 0; index < text.length(); index++) {
                char character = text.charAt(index);
    
                switch (character) {
                    case '"' -> writer.write("\\\"");
                    case '\\' -> writer.write("\\\\");
                    case '\b' -> writer.write("\\b");
                    case '\f' -> writer.write("\\f");
                    case '\n' -> writer.write("\\n");
                    case '\r' -> writer.write("\\r");
                    case '\t' -> writer.write("\\t");
                    default -> {
                        if (character < 0x20) {
                            writer.write(
                                String.format(
                                    Locale.ROOT,
                                    "\\u%04x",
                                    (int) character
                                )
                            );
                        } else {
                            writer.write(character);
                        }
                    }
                }
            }
    
            writer.write("\"");
        }
    
        private static void writeIndent(
                Writer writer,
                int indent) throws IOException {
    
            writer.write("  ".repeat(indent));
        }
    }

## src\main\java\it\alterlega\recordsnext\gui\FcmSeasonDetector.java

File: src\main\java\it\alterlega\recordsnext\gui\FcmSeasonDetector.java

    package it.alterlega.recordsnext.gui;
    
    import java.nio.file.Path;
    import java.sql.*;
    import java.util.*;
    import java.util.regex.*;
    
    final class FcmSeasonDetector {
        record Detection(String seasonId, int seasonNumber, String evidence) {}
    
        private static final Pattern RANGE = Pattern.compile("(?<!\\d)(20\\d{2})[^0-9]{0,5}(20\\d{2})(?!\\d)");
        private static final Pattern SINGLE = Pattern.compile("(?<!\\d)(20\\d{2})(?!\\d)");
    
        Detection detect(Path fcm) throws Exception {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            try (Connection c = DriverManager.getConnection("jdbc:ucanaccess://" + fcm.toAbsolutePath())) {
                Detection fromLeague = detectFromLeague(c);
                if (fromLeague != null) {
                    return fromLeague;
                }
            }
            throw new IllegalArgumentException(
                "Impossibile ricavare stagione e numero stagione dalla tabella LEGA del file FCM selezionato."
            );
        }
    
        private Detection detectFromLeague(Connection c) throws SQLException {
            String table = findTable(c, "LEGA");
            if (table == null) {
                return null;
            }
    
            Set<String> columns = columns(c, table);
            if (!containsIgnoreCase(columns, "STAGIONE")) {
                return null;
            }
    
            String seasonColumn = actualName(columns, "STAGIONE");
            String yearColumn = actualName(columns, "ANNOARCHIVIO");
            String nameColumn = actualName(columns, "NOME");
    
            StringBuilder sql = new StringBuilder("SELECT TOP 1 [")
                .append(escape(seasonColumn)).append("]");
            if (yearColumn != null) sql.append(", [").append(escape(yearColumn)).append("]");
            if (nameColumn != null) sql.append(", [").append(escape(nameColumn)).append("]");
            sql.append(" FROM [").append(escape(table)).append("]");
    
            try (Statement st = c.createStatement(); ResultSet r = st.executeQuery(sql.toString())) {
                if (!r.next()) {
                    return null;
                }
    
                int seasonNumber = toPositiveInt(r.getObject(1));
                if (seasonNumber < 1) {
                    throw new IllegalArgumentException("Il campo LEGA.STAGIONE non contiene un numero stagione valido.");
                }
    
                int index = 2;
                Integer archiveYear = null;
                if (yearColumn != null) {
                    int value = toPositiveInt(r.getObject(index++));
                    if (value >= 1900 && value <= 2200) archiveYear = value;
                }
    
                String leagueName = null;
                if (nameColumn != null) {
                    Object value = r.getObject(index);
                    if (value != null) leagueName = value.toString();
                }
    
                String seasonId = archiveYear == null ? parse(leagueName) : archiveYear + "_" + (archiveYear + 1);
                if (seasonId == null) {
                    throw new IllegalArgumentException(
                        "Il file FCM contiene LEGA.STAGIONE=" + seasonNumber
                            + " ma non consente di ricavare gli anni della stagione."
                    );
                }
    
                return new Detection(
                    seasonId,
                    seasonNumber,
                    "LEGA.STAGIONE=" + seasonNumber
                        + (archiveYear == null ? "" : ", LEGA.ANNOARCHIVIO=" + archiveYear)
                );
            }
        }
    
        private static String findTable(Connection c, String expected) throws SQLException {
            DatabaseMetaData md = c.getMetaData();
            try (ResultSet tables = md.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (tables.next()) {
                    String name = tables.getString("TABLE_NAME");
                    if (expected.equalsIgnoreCase(name)) return name;
                }
            }
            return null;
        }
    
        private static Set<String> columns(Connection c, String table) throws SQLException {
            Set<String> out = new LinkedHashSet<>();
            try (ResultSet cols = c.getMetaData().getColumns(null, null, table, "%")) {
                while (cols.next()) out.add(cols.getString("COLUMN_NAME"));
            }
            return out;
        }
    
        private static boolean containsIgnoreCase(Collection<String> values, String expected) {
            return actualName(values, expected) != null;
        }
    
        private static String actualName(Collection<String> values, String expected) {
            for (String value : values) {
                if (expected.equalsIgnoreCase(value)) return value;
            }
            return null;
        }
    
        private static int toPositiveInt(Object value) {
            if (value instanceof Number n) return n.intValue();
            if (value == null) return -1;
            try { return Integer.parseInt(value.toString().trim()); }
            catch (NumberFormatException ex) { return -1; }
        }
    
        private static String escape(String identifier) {
            return identifier.replace("]", "]]");
        }
    
        private static String parse(String value) {
            if (value == null) return null;
            Matcher range = RANGE.matcher(value);
            while (range.find()) {
                int a = Integer.parseInt(range.group(1));
                int b = Integer.parseInt(range.group(2));
                if (b == a + 1) return a + "_" + b;
            }
            Matcher single = SINGLE.matcher(value);
            if (single.find()) {
                int year = Integer.parseInt(single.group(1));
                return year + "_" + (year + 1);
            }
            return null;
        }
    }

## src\main\java\it\alterlega\recordsnext\gui\HistoricalMappingDialog.java

File: src\main\java\it\alterlega\recordsnext\gui\HistoricalMappingDialog.java

    package it.alterlega.recordsnext.gui;
    
    import javax.swing.*;
    import javax.swing.border.EmptyBorder;
    import java.awt.*;
    import java.util.ArrayList;
    import java.util.List;
    
    final class HistoricalMappingDialog extends JDialog {
        private static final Object NEW_IDENTITY = "<Nuova identità storica>";
        private static final Object EXCLUDE = "<Non elaborare>";
    
        private final HistoricalMappingRepository repository;
        private final List<String> seasons;
        private int seasonIndex;
        private String seasonId;
        private final JLabel heading = new JLabel();
        private final JLabel missingCount = new JLabel();
        private final JButton nextMissing = new JButton("Vai alla prossima mancante");
        private final JTabbedPane tabs = new JTabbedPane();
        private final List<RowEditor> competitionEditors = new ArrayList<>();
        private final List<RowEditor> teamEditors = new ArrayList<>();
        private final JButton previous = new JButton("<< Indietro");
        private final JButton next = new JButton("Salva e avanti >>");
        private boolean saved;
    
        HistoricalMappingDialog(Window owner, HistoricalMappingRepository repository) throws Exception {
            this(owner, repository, null);
        }
    
        HistoricalMappingDialog(Window owner, HistoricalMappingRepository repository, String initialSeason) throws Exception {
            super(owner, "RecordsNext - Associazioni storiche", ModalityType.APPLICATION_MODAL);
            this.repository = repository;
            repository.prepare();
            this.seasons = repository.seasonsNewestFirst();
            build();
            if (!seasons.isEmpty()) {
                int index = initialSeason == null ? 0 : seasons.indexOf(initialSeason);
                loadSeason(index < 0 ? 0 : index);
            }
        }
    
        boolean open() {
            if (seasons.isEmpty()) {
                JOptionPane.showMessageDialog(getOwner(), "Non ci sono stagioni gestite da configurare.", "RecordsNext", JOptionPane.INFORMATION_MESSAGE);
                return true;
            }
            setVisible(true);
            return saved;
        }
    
        private void build() {
            setLayout(new BorderLayout(8, 8));
            ((JComponent) getContentPane()).setBorder(new EmptyBorder(10, 12, 10, 12));
            heading.setFont(heading.getFont().deriveFont(Font.BOLD, 14f));
            missingCount.setFont(missingCount.getFont().deriveFont(Font.BOLD));
            missingCount.setForeground(new Color(185, 45, 35));
            nextMissing.addActionListener(e -> focusNextMissing());
            JPanel header = new JPanel(new BorderLayout(8, 0));
            header.add(heading, BorderLayout.WEST);
            JPanel missingPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            missingPanel.add(missingCount);
            missingPanel.add(nextMissing);
            header.add(missingPanel, BorderLayout.EAST);
            add(header, BorderLayout.NORTH);
            add(tabs, BorderLayout.CENTER);
            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton cancel = new JButton("Annulla");
            cancel.addActionListener(e -> dispose());
            previous.addActionListener(e -> goPrevious());
            next.addActionListener(e -> saveAndNext());
            buttons.add(cancel); buttons.add(previous); buttons.add(next);
            add(buttons, BorderLayout.SOUTH);
            setSize(900, 700); setMinimumSize(new Dimension(780, 540)); setLocationRelativeTo(getOwner());
        }
    
        private void loadSeason(int index) throws Exception {
            seasonIndex = index;
            seasonId = seasons.get(index);
            competitionEditors.clear();
            teamEditors.clear();
            tabs.removeAll();
            tabs.addTab("1. Competizioni", createPage(HistoricalMappingRepository.Kind.COMPETITION, competitionEditors));
            tabs.addTab("2. Squadre", createPage(HistoricalMappingRepository.Kind.TEAM, teamEditors));
            boolean anchor = repository.isAnchor(seasonId);
            heading.setText("Stagione " + seasonId + "  (" + (index + 1) + "/" + seasons.size() + ") — " +
                (anchor ? "definizione delle identità attuali" : "associazione alle identità già definite"));
            previous.setEnabled(index > 0);
            next.setText(index == seasons.size() - 1 ? "Salva e termina" : "Salva e avanti >>");
            tabs.setSelectedIndex(0);
            updateMissingState();
            SwingUtilities.invokeLater(this::focusNextMissing);
        }
    
        private JScrollPane createPage(HistoricalMappingRepository.Kind kind, List<RowEditor> editors) throws Exception {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(new EmptyBorder(8, 8, 8, 8));
            GridBagConstraints h = new GridBagConstraints();
            h.gridy = 0; h.insets = new Insets(3, 4, 8, 4); h.anchor = GridBagConstraints.WEST;
            h.gridx = 0; h.weightx = .42; h.fill = GridBagConstraints.HORIZONTAL;
            panel.add(new JLabel(kind == HistoricalMappingRepository.Kind.COMPETITION ? "Competizione stagione" : "Squadra stagione"), h);
            h.gridx = 1; h.weightx = .58;
            panel.add(new JLabel(kind == HistoricalMappingRepository.Kind.COMPETITION ? "Identità storica/canonica" : "Identità storica/canonica"), h);
    
            int row = 1;
            for (var mapping : repository.load(seasonId, kind)) {
                RowEditor editor = new RowEditor(mapping, repository.isAnchor(seasonId));
                editors.add(editor);
                GridBagConstraints g = new GridBagConstraints();
                g.gridy = row++; g.insets = new Insets(3, 4, 3, 4); g.anchor = GridBagConstraints.WEST;
                g.gridx = 0; g.weightx = .42; g.fill = GridBagConstraints.HORIZONTAL;
                panel.add(editor.sourceLabel, g);
                g.gridx = 1; g.weightx = .58;
                panel.add(editor.combo, g);
            }
            GridBagConstraints filler = new GridBagConstraints();
            filler.gridy = row; filler.weighty = 1; filler.fill = GridBagConstraints.VERTICAL;
            panel.add(Box.createVerticalGlue(), filler);
            JScrollPane scroll = new JScrollPane(panel);
            scroll.getVerticalScrollBar().setUnitIncrement(18);
            return scroll;
        }
    
        private void updateMissingState() {
            long count = competitionEditors.stream().filter(RowEditor::isMissing).count()
                + teamEditors.stream().filter(RowEditor::isMissing).count();
            missingCount.setText(count == 0 ? "Associazioni complete" : count + " associazioni mancanti");
            missingCount.setForeground(count == 0 ? new Color(20, 120, 55) : new Color(185, 45, 35));
            nextMissing.setEnabled(count > 0);
        }
    
        private void focusNextMissing() {
            List<RowEditor> current = tabs.getSelectedIndex() == 0 ? competitionEditors : teamEditors;
            RowEditor missing = current.stream().filter(RowEditor::isMissing).findFirst().orElse(null);
            if (missing == null && tabs.getSelectedIndex() == 0) {
                tabs.setSelectedIndex(1);
                missing = teamEditors.stream().filter(RowEditor::isMissing).findFirst().orElse(null);
            }
            if (missing != null) {
                missing.combo.requestFocusInWindow();
                missing.combo.scrollRectToVisible(missing.combo.getBounds());
            }
        }
    
        private void saveAndNext() {
            try {
                saveCurrent();
                if (seasonIndex == seasons.size() - 1) {
                    saved = true;
                    dispose();
                } else {
                    loadSeason(seasonIndex + 1);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "RecordsNext", JOptionPane.ERROR_MESSAGE);
            }
        }
    
        private void goPrevious() {
            try {
                saveCurrent();
                loadSeason(seasonIndex - 1);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "RecordsNext", JOptionPane.ERROR_MESSAGE);
            }
        }
    
        private void saveCurrent() throws Exception {
            repository.save(seasonId, HistoricalMappingRepository.Kind.COMPETITION,
                competitionEditors.stream().map(RowEditor::decision).toList());
            repository.save(seasonId, HistoricalMappingRepository.Kind.TEAM,
                teamEditors.stream().map(RowEditor::decision).toList());
        }
    
        private final class RowEditor {
            final HistoricalMappingRepository.MappingRow row;
            final JLabel sourceLabel;
            final JComboBox<Object> combo = new JComboBox<>();
    
            RowEditor(HistoricalMappingRepository.MappingRow row, boolean anchorSeason) {
                this.row = row;
                this.sourceLabel = new JLabel(row.sourceName());
                combo.addItem("<Selezionare>");
                combo.addItem(EXCLUDE);
                if (!anchorSeason) combo.addItem(NEW_IDENTITY);
                for (var identity : row.candidates()) combo.addItem(identity);
    
                Long preferredIdentityId = row.identityId() != null
                    ? row.identityId()
                    : row.inheritedIdentityId();
                if (preferredIdentityId != null) {
                    for (int i = 0; i < combo.getItemCount(); i++) {
                        Object item = combo.getItemAt(i);
                        if (item instanceof HistoricalMappingRepository.Identity id && id.id() == preferredIdentityId) {
                            combo.setSelectedIndex(i);
                            break;
                        }
                    }
                } else if ("ESCLUSA".equals(row.status())) {
                    combo.setSelectedItem(EXCLUDE);
                } else {
                    for (int i = 0; i < combo.getItemCount(); i++) {
                        Object item = combo.getItemAt(i);
                        if (item instanceof HistoricalMappingRepository.Identity id
                            && normalize(id.name()).equals(normalize(row.sourceName()))) {
                            combo.setSelectedIndex(i);
                            break;
                        }
                    }
                }
    
                combo.setRenderer(new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                                   boolean selected, boolean focus) {
                        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, selected, focus);
                        if ("<Selezionare>".equals(value)) {
                            label.setForeground(selected ? Color.WHITE : new Color(185, 45, 35));
                            label.setFont(label.getFont().deriveFont(Font.BOLD));
                        } else if (EXCLUDE.equals(value) || NEW_IDENTITY.equals(value)) {
                            label.setFont(label.getFont().deriveFont(Font.BOLD));
                        }
                        return label;
                    }
                });
                combo.addActionListener(e -> { updateVisualState(); updateMissingState(); });
                updateVisualState();
            }
    
            boolean isMissing() {
                return "<Selezionare>".equals(combo.getSelectedItem());
            }
    
            void updateVisualState() {
                boolean missing = isMissing();
                sourceLabel.setForeground(missing ? new Color(185, 45, 35) : UIManager.getColor("Label.foreground"));
                sourceLabel.setFont(sourceLabel.getFont().deriveFont(missing ? Font.BOLD : Font.PLAIN));
                combo.setBackground(missing ? new Color(255, 225, 220) : Color.WHITE);
                combo.setBorder(missing ? BorderFactory.createLineBorder(new Color(210, 60, 45), 2)
                                        : UIManager.getBorder("ComboBox.border"));
            }
    
            HistoricalMappingRepository.Decision decision() {
                Object selected = combo.getSelectedItem();
                if (selected instanceof HistoricalMappingRepository.Identity id) {
                    return new HistoricalMappingRepository.Decision(row.seasonEntityIds(), row.sourceName(), id.id(), false, false);
                }
                return new HistoricalMappingRepository.Decision(row.seasonEntityIds(), row.sourceName(), null,
                    NEW_IDENTITY.equals(selected), EXCLUDE.equals(selected));
            }
    
            private String normalize(String value) {
                return value == null ? "" : value.toLowerCase().replaceAll("[^a-z0-9]", "");
            }
        }
    }

## src\main\java\it\alterlega\recordsnext\gui\HistoricalMappingRepository.java

File: src\main\java\it\alterlega\recordsnext\gui\HistoricalMappingRepository.java

    package it.alterlega.recordsnext.gui;
    
    import java.nio.file.Path;
    import java.sql.*;
    import java.time.Instant;
    import java.util.*;
    
    final class HistoricalMappingRepository {
        enum Kind { COMPETITION, TEAM }
    
        record Identity(long id, String name) {
            @Override public String toString() { return name; }
        }
    
        record MappingRow(List<Long> seasonEntityIds, String sourceName, String normalizedName,
                          String status, Long identityId, Long inheritedIdentityId,
                          List<Identity> candidates) {}
    
        record Decision(List<Long> seasonEntityIds, String sourceName, Long identityId,
                        boolean createNew, boolean excluded) {}
    
        private final Path database;
    
        HistoricalMappingRepository(Path database) {
            this.database = database.toAbsolutePath().normalize();
        }
    
        /**
         * Prepara il database per la configurazione globale. Gli import FCM/FCA possono
         * produrre due righe tecniche della stessa entita nella stagione ancora; tali
         * righe devono condividere una sola identita canonica.
         */
        void prepare() throws Exception {
            try (Connection c = open()) {
                c.setAutoCommit(false);
                try {
                    consolidateDuplicateIdentities(c, Kind.COMPETITION);
                    consolidateDuplicateIdentities(c, Kind.TEAM);
                    synchronizeGroupedMappings(c, Kind.COMPETITION);
                    synchronizeGroupedMappings(c, Kind.TEAM);
                    compactObsoleteSources(c);
                    c.commit();
                } catch (Exception ex) {
                    c.rollback();
                    throw ex;
                }
            }
        }
    
        /** Tutte le stagioni gestite, inclusa l'attuale, dalla piu recente alla piu vecchia. */
        List<String> seasonsNewestFirst() throws Exception {
            String sql = "SELECT c.season_id " +
                "FROM rn_season_configuration c " +
                "JOIN rn_season s ON s.season_id=c.season_id " +
                "WHERE c.management_type='GESTITA' " +
                "ORDER BY COALESCE(s.sort_order,0) DESC, c.season_id DESC";
            try (Connection c = open(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                List<String> result = new ArrayList<>();
                while (rs.next()) result.add(rs.getString(1));
                return result;
            }
        }
    
        boolean isAnchor(String seasonId) throws Exception {
            try (Connection c = open(); PreparedStatement ps = c.prepareStatement(
                    "SELECT is_anchor FROM rn_season WHERE season_id=?")) {
                ps.setString(1, seasonId);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() && rs.getInt(1) == 1;
                }
            }
        }
    
        List<MappingRow> load(String seasonId, Kind kind) throws Exception {
            try (Connection c = open()) {
                String entityTable = kind == Kind.COMPETITION ? "rn_competition_season" : "rn_team_season";
                String entityId = kind == Kind.COMPETITION ? "competition_season_id" : "team_season_id";
                String mappingTable = kind == Kind.COMPETITION ? "rn_competition_mapping" : "rn_team_mapping";
                String identityTable = kind == Kind.COMPETITION ? "rn_competition_identity" : "rn_team_identity";
                String identityId = kind == Kind.COMPETITION ? "competition_identity_id" : "team_identity_id";
    
                // Sono disponibili solo le identita effettivamente mantenute in elaborazione.
                List<Identity> identities = new ArrayList<>();
                String identitySql = "SELECT i." + identityId + ",i.canonical_name " +
                    "FROM " + identityTable + " i " +
                    "WHERE EXISTS (SELECT 1 FROM " + mappingTable + " m " +
                    "WHERE m." + identityId + "=i." + identityId + " AND m.mapping_status='ASSOCIATA') " +
                    "ORDER BY i.canonical_name COLLATE NOCASE,i." + identityId;
                try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(identitySql)) {
                    while (rs.next()) identities.add(new Identity(rs.getLong(1), rs.getString(2)));
                }
    
                String sql = "SELECT e." + entityId + ",e.source_name,e.normalized_name," +
                    "COALESCE(m.mapping_status,'DA_CONFIGURARE'),m." + identityId + " " +
                    "FROM " + entityTable + " e LEFT JOIN " + mappingTable + " m ON m." + entityId + "=e." + entityId + " " +
                    "JOIN rn_source_file sf ON sf.source_file_id=e.source_file_id " +
                    "WHERE e.season_id=? AND sf.import_id=(" +
                    "SELECT MAX(sf2.import_id) FROM rn_source_file sf2 " +
                    "WHERE sf2.season_id=e.season_id AND sf2.source_type='FCM') " +
                    "ORDER BY e.source_name COLLATE NOCASE,e." + entityId;
    
                LinkedHashMap<String, Group> groups = new LinkedHashMap<>();
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setString(1, seasonId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            long id = rs.getLong(1);
                            String source = rs.getString(2);
                            String normalized = rs.getString(3);
                            String key = normalize(normalized == null || normalized.isBlank() ? source : normalized);
                            Group group = groups.computeIfAbsent(key, k -> new Group(source, normalized));
                            group.ids.add(id);
                            String status = rs.getString(4);
                            Long mapped = rs.getObject(5) == null ? null : rs.getLong(5);
                            group.accept(status, mapped);
                        }
                    }
                }
    
                List<MappingRow> rows = new ArrayList<>();
                for (Group group : groups.values()) {
                    List<Identity> ordered = new ArrayList<>(identities);
                    ordered.sort(Comparator
                        .comparingInt((Identity i) -> similarityRank(group.normalizedName, i.name()))
                        .thenComparing(Identity::name, String.CASE_INSENSITIVE_ORDER));
                    Long inheritedIdentityId = group.identityId == null && "DA_CONFIGURARE".equals(group.status)
                        ? findInheritedIdentity(c, seasonId, kind, group.normalizedName, group.sourceName)
                        : null;
                    rows.add(new MappingRow(List.copyOf(group.ids), group.sourceName, group.normalizedName,
                        group.status, group.identityId, inheritedIdentityId, ordered));
                }
                return rows;
            }
        }
    
    
        private static Long findInheritedIdentity(
            Connection c,
            String seasonId,
            Kind kind,
            String normalizedName,
            String sourceName
        ) throws SQLException {
            String entityTable = kind == Kind.COMPETITION ? "rn_competition_season" : "rn_team_season";
            String entityId = kind == Kind.COMPETITION ? "competition_season_id" : "team_season_id";
            String mappingTable = kind == Kind.COMPETITION ? "rn_competition_mapping" : "rn_team_mapping";
            String identityId = kind == Kind.COMPETITION ? "competition_identity_id" : "team_identity_id";
            String lookupName = (normalizedName == null || normalizedName.isBlank() ? sourceName : normalizedName)
                .trim().toLowerCase(Locale.ROOT);
            String sql = "SELECT m." + identityId + " " +
                "FROM " + entityTable + " e " +
                "JOIN " + mappingTable + " m ON m." + entityId + "=e." + entityId + " " +
                "JOIN rn_season newer ON newer.season_id=e.season_id " +
                "JOIN rn_season current ON current.season_id=? " +
                "WHERE LOWER(TRIM(e.normalized_name))=? " +
                "AND newer.sort_order>current.sort_order " +
                "AND m.mapping_status='ASSOCIATA' AND m." + identityId + " IS NOT NULL " +
                "ORDER BY newer.sort_order ASC LIMIT 1";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, seasonId);
                ps.setString(2, lookupName);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getLong(1) : null;
                }
            }
        }
    
        void save(String seasonId, Kind kind, List<Decision> decisions) throws Exception {
            try (Connection c = open()) {
                c.setAutoCommit(false);
                try {
                    Set<Long> used = new HashSet<>();
                    for (Decision d : decisions) {
                        Long identityId = d.identityId();
                        String status;
                        String method;
                        if (d.createNew()) {
                            identityId = createIdentity(c, kind, seasonId, d.seasonEntityIds().get(0), d.sourceName());
                            status = "ASSOCIATA";
                            method = "NEW_HISTORICAL_IDENTITY";
                        } else if (identityId != null) {
                            if (!used.add(identityId)) {
                                throw new IllegalStateException("La stessa identita e stata scelta due volte nella stagione: " + d.sourceName());
                            }
                            status = "ASSOCIATA";
                            method = isAnchor(c, seasonId) ? "ANCHOR_GUI" : "GUI_MANUAL";
                        } else if (d.excluded()) {
                            status = "ESCLUSA";
                            method = "GUI_EXCLUDED";
                        } else {
                            throw new IllegalStateException("Decisione mancante per: " + d.sourceName());
                        }
                        for (long entityId : d.seasonEntityIds()) {
                            updateMapping(c, kind, entityId, identityId, status, method);
                        }
                    }
                    c.commit();
                } catch (Exception ex) {
                    c.rollback();
                    throw ex;
                }
            }
        }
    
        int pending(String seasonId) throws Exception {
            int pending = 0;
            for (MappingRow row : load(seasonId, Kind.COMPETITION)) {
                if ("DA_CONFIGURARE".equals(row.status())) pending++;
            }
            for (MappingRow row : load(seasonId, Kind.TEAM)) {
                if ("DA_CONFIGURARE".equals(row.status())) pending++;
            }
            return pending;
        }
    
    
        private static void synchronizeGroupedMappings(Connection c, Kind kind) throws Exception {
            String entityTable = kind == Kind.COMPETITION ? "rn_competition_season" : "rn_team_season";
            String entityId = kind == Kind.COMPETITION ? "competition_season_id" : "team_season_id";
            String mappingTable = kind == Kind.COMPETITION ? "rn_competition_mapping" : "rn_team_mapping";
            String identityId = kind == Kind.COMPETITION ? "competition_identity_id" : "team_identity_id";
    
            String groupsSql = "SELECT e.season_id,LOWER(TRIM(e.normalized_name))," +
                "COUNT(DISTINCT CASE WHEN m.mapping_status='ASSOCIATA' THEN m." + identityId + " END)," +
                "MIN(CASE WHEN m.mapping_status='ASSOCIATA' THEN m." + identityId + " END)," +
                "MAX(CASE WHEN m.mapping_status='ESCLUSA' THEN 1 ELSE 0 END) " +
                "FROM " + entityTable + " e LEFT JOIN " + mappingTable + " m ON m." + entityId + "=e." + entityId + " " +
                "GROUP BY e.season_id,LOWER(TRIM(e.normalized_name))";
    
            try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(groupsSql)) {
                while (rs.next()) {
                    String season = rs.getString(1);
                    String normalized = rs.getString(2);
                    int identities = rs.getInt(3);
                    Long mapped = rs.getObject(4) == null ? null : rs.getLong(4);
                    boolean excluded = rs.getInt(5) == 1;
                    if (identities > 1) {
                        throw new IllegalStateException("Associazioni incoerenti per " + season + ": " + normalized);
                    }
                    if (mapped == null && !excluded) continue;
                    String status = mapped != null ? "ASSOCIATA" : "ESCLUSA";
                    String method = mapped != null ? "GUI_GROUP_SYNC" : "GUI_EXCLUDED_GROUP_SYNC";
                    String update = "UPDATE " + mappingTable + " SET " + identityId + "=?,mapping_status=?,mapping_method=?,updated_at=? " +
                        "WHERE " + entityId + " IN (SELECT " + entityId + " FROM " + entityTable + " WHERE season_id=? AND LOWER(TRIM(normalized_name))=?)";
                    try (PreparedStatement ps = c.prepareStatement(update)) {
                        if (mapped == null) ps.setNull(1, Types.BIGINT); else ps.setLong(1, mapped);
                        ps.setString(2, status);
                        ps.setString(3, method);
                        ps.setString(4, Instant.now().toString());
                        ps.setString(5, season);
                        ps.setString(6, normalized);
                        ps.executeUpdate();
                    }
                }
            }
        }
    
        private static void compactObsoleteSources(Connection c) throws Exception {
            reanchorIdentities(c, Kind.COMPETITION);
            reanchorIdentities(c, Kind.TEAM);
    
            for (Kind kind : Kind.values()) {
                String entityTable = kind == Kind.COMPETITION ? "rn_competition_season" : "rn_team_season";
                String entityId = kind == Kind.COMPETITION ? "competition_season_id" : "team_season_id";
                String mappingTable = kind == Kind.COMPETITION ? "rn_competition_mapping" : "rn_team_mapping";
                String identityTable = kind == Kind.COMPETITION ? "rn_competition_identity" : "rn_team_identity";
                String anchorId = kind == Kind.COMPETITION ? "anchor_competition_season_id" : "anchor_team_season_id";
    
                String stale = "SELECT e." + entityId + " FROM " + entityTable + " e JOIN rn_source_file sf ON sf.source_file_id=e.source_file_id " +
                    "WHERE sf.source_type='FCM' AND sf.import_id<>(SELECT MAX(sf2.import_id) FROM rn_source_file sf2 WHERE sf2.season_id=sf.season_id AND sf2.source_type='FCM') " +
                    "AND NOT EXISTS(SELECT 1 FROM " + identityTable + " i WHERE i." + anchorId + "=e." + entityId + ")";
                try (Statement st = c.createStatement()) {
                    st.executeUpdate("DELETE FROM " + mappingTable + " WHERE " + entityId + " IN (" + stale + ")");
                    st.executeUpdate("DELETE FROM " + entityTable + " WHERE " + entityId + " IN (" + stale + ")");
                }
            }
    
            List<Long> obsoleteImports = new ArrayList<>();
            String obsoleteSql = "SELECT sf.import_id FROM rn_source_file sf WHERE sf.import_id<>(" +
                "SELECT MAX(sf2.import_id) FROM rn_source_file sf2 WHERE sf2.season_id=sf.season_id AND sf2.source_type=sf.source_type) " +
                "AND NOT EXISTS(SELECT 1 FROM rn_competition_season cs WHERE cs.source_file_id=sf.source_file_id) " +
                "AND NOT EXISTS(SELECT 1 FROM rn_team_season ts WHERE ts.source_file_id=sf.source_file_id)";
            try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(obsoleteSql)) {
                while (rs.next()) obsoleteImports.add(rs.getLong(1));
            }
            for (long importId : obsoleteImports) {
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM rn_source_file WHERE import_id=?")) {
                    ps.setLong(1, importId); ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM rn_column_catalog WHERE import_id=?")) {
                    ps.setLong(1, importId); ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM rn_table_catalog WHERE import_id=?")) {
                    ps.setLong(1, importId); ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM rn_import WHERE import_id=?")) {
                    ps.setLong(1, importId); ps.executeUpdate();
                }
            }
        }
    
        private static void reanchorIdentities(Connection c, Kind kind) throws Exception {
            String identityTable = kind == Kind.COMPETITION ? "rn_competition_identity" : "rn_team_identity";
            String identityId = kind == Kind.COMPETITION ? "competition_identity_id" : "team_identity_id";
            String anchorId = kind == Kind.COMPETITION ? "anchor_competition_season_id" : "anchor_team_season_id";
            String entityTable = kind == Kind.COMPETITION ? "rn_competition_season" : "rn_team_season";
            String entityId = kind == Kind.COMPETITION ? "competition_season_id" : "team_season_id";
    
            String sql = "SELECT i." + identityId + ",e.season_id,e.normalized_name FROM " + identityTable + " i " +
                "JOIN " + entityTable + " e ON e." + entityId + "=i." + anchorId + " " +
                "JOIN rn_source_file sf ON sf.source_file_id=e.source_file_id " +
                "WHERE sf.import_id<>(SELECT MAX(sf2.import_id) FROM rn_source_file sf2 WHERE sf2.season_id=sf.season_id AND sf2.source_type='FCM')";
            try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                List<Object[]> rows = new ArrayList<>();
                while (rs.next()) rows.add(new Object[]{rs.getLong(1), rs.getString(2), rs.getString(3)});
                for (Object[] row : rows) {
                    long id = (Long) row[0];
                    String season = (String) row[1];
                    String normalized = (String) row[2];
                    String latestSql = "SELECT e." + entityId + " FROM " + entityTable + " e JOIN rn_source_file sf ON sf.source_file_id=e.source_file_id " +
                        "WHERE e.season_id=? AND LOWER(TRIM(e.normalized_name))=LOWER(TRIM(?)) " +
                        "AND sf.import_id=(SELECT MAX(sf2.import_id) FROM rn_source_file sf2 WHERE sf2.season_id=e.season_id AND sf2.source_type='FCM') LIMIT 1";
                    try (PreparedStatement find = c.prepareStatement(latestSql)) {
                        find.setString(1, season); find.setString(2, normalized);
                        try (ResultSet latest = find.executeQuery()) {
                            if (latest.next()) {
                                try (PreparedStatement update = c.prepareStatement("UPDATE " + identityTable + " SET " + anchorId + "=? WHERE " + identityId + "=?")) {
                                    update.setLong(1, latest.getLong(1)); update.setLong(2, id); update.executeUpdate();
                                }
                            }
                        }
                    }
                }
            }
        }
        private Connection open() throws Exception {
            Class.forName("org.sqlite.JDBC");
            Connection c = DriverManager.getConnection("jdbc:sqlite:" + database);
            try (Statement st = c.createStatement()) {
                st.execute("PRAGMA foreign_keys=ON");
                st.execute("PRAGMA busy_timeout=10000");
            }
            return c;
        }
    
        private static boolean isAnchor(Connection c, String seasonId) throws SQLException {
            try (PreparedStatement ps = c.prepareStatement("SELECT is_anchor FROM rn_season WHERE season_id=?")) {
                ps.setString(1, seasonId);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() && rs.getInt(1) == 1;
                }
            }
        }
    
        private static void consolidateDuplicateIdentities(Connection c, Kind kind) throws Exception {
            String identityTable = kind == Kind.COMPETITION ? "rn_competition_identity" : "rn_team_identity";
            String identityId = kind == Kind.COMPETITION ? "competition_identity_id" : "team_identity_id";
            String mappingTable = kind == Kind.COMPETITION ? "rn_competition_mapping" : "rn_team_mapping";
    
            String groupsSql = "SELECT anchor_season_id,LOWER(TRIM(canonical_name)),MIN(" + identityId + ") " +
                "FROM " + identityTable + " GROUP BY anchor_season_id,LOWER(TRIM(canonical_name)) HAVING COUNT(*)>1";
            List<long[]> duplicateGroups = new ArrayList<>();
            try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(groupsSql)) {
                while (rs.next()) {
                    String anchorSeason = rs.getString(1);
                    String normalizedName = rs.getString(2);
                    long keeper = rs.getLong(3);
                    try (PreparedStatement ps = c.prepareStatement(
                            "SELECT " + identityId + " FROM " + identityTable +
                            " WHERE anchor_season_id=? AND LOWER(TRIM(canonical_name))=? AND " + identityId + "<>?")) {
                        ps.setString(1, anchorSeason);
                        ps.setString(2, normalizedName);
                        ps.setLong(3, keeper);
                        try (ResultSet duplicates = ps.executeQuery()) {
                            while (duplicates.next()) duplicateGroups.add(new long[]{keeper, duplicates.getLong(1)});
                        }
                    }
                }
            }
    
            for (long[] pair : duplicateGroups) {
                long keeper = pair[0];
                long duplicate = pair[1];
                try (PreparedStatement update = c.prepareStatement(
                        "UPDATE " + mappingTable + " SET " + identityId + "=? WHERE " + identityId + "=?")) {
                    update.setLong(1, keeper);
                    update.setLong(2, duplicate);
                    update.executeUpdate();
                }
                try (PreparedStatement delete = c.prepareStatement(
                        "DELETE FROM " + identityTable + " WHERE " + identityId + "=?")) {
                    delete.setLong(1, duplicate);
                    delete.executeUpdate();
                }
            }
        }
    
        private static long createIdentity(Connection c, Kind kind, String seasonId,
                                           long entityId, String name) throws Exception {
            String table = kind == Kind.COMPETITION ? "rn_competition_identity" : "rn_team_identity";
            String anchorCol = kind == Kind.COMPETITION ? "anchor_competition_season_id" : "anchor_team_season_id";
            String sql = "INSERT INTO " + table + "(anchor_season_id," + anchorCol + ",canonical_name,created_at) VALUES(?,?,?,?)";
            try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, seasonId); ps.setLong(2, entityId); ps.setString(3, name); ps.setString(4, Instant.now().toString());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) throw new IllegalStateException("Identita non creata: " + name);
                    return rs.getLong(1);
                }
            }
        }
    
        private static void updateMapping(Connection c, Kind kind, long entityId, Long identityId,
                                          String status, String method) throws Exception {
            String table = kind == Kind.COMPETITION ? "rn_competition_mapping" : "rn_team_mapping";
            String entityCol = kind == Kind.COMPETITION ? "competition_season_id" : "team_season_id";
            String identityCol = kind == Kind.COMPETITION ? "competition_identity_id" : "team_identity_id";
            String sql = "INSERT INTO " + table + "(" + entityCol + "," + identityCol + ",mapping_status,mapping_method,notes,updated_at) " +
                "VALUES(?,?,?,?,NULL,?) ON CONFLICT(" + entityCol + ") DO UPDATE SET " + identityCol + "=excluded." + identityCol + "," +
                "mapping_status=excluded.mapping_status,mapping_method=excluded.mapping_method,notes=NULL,updated_at=excluded.updated_at";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setLong(1, entityId);
                if (identityId == null) ps.setNull(2, Types.BIGINT); else ps.setLong(2, identityId);
                ps.setString(3, status); ps.setString(4, method); ps.setString(5, Instant.now().toString());
                ps.executeUpdate();
            }
        }
    
        private static int similarityRank(String normalized, String candidate) {
            String a = normalize(normalized), b = normalize(candidate);
            if (a.equals(b)) return 0;
            if (a.contains(b) || b.contains(a)) return 1;
            return 2;
        }
    
        private static String normalize(String s) {
            return s == null ? "" : s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        }
    
        private static final class Group {
            final String sourceName;
            final String normalizedName;
            final List<Long> ids = new ArrayList<>();
            String status = "DA_CONFIGURARE";
            Long identityId;
            Group(String sourceName, String normalizedName) {
                this.sourceName = sourceName;
                this.normalizedName = normalizedName;
            }
            void accept(String candidateStatus, Long candidateIdentity) {
                if (candidateIdentity != null && identityId == null) identityId = candidateIdentity;
                if ("ASSOCIATA".equals(candidateStatus)) status = "ASSOCIATA";
                else if (!"ASSOCIATA".equals(status) && "ESCLUSA".equals(candidateStatus)) status = "ESCLUSA";
            }
        }
    }

## src\main\java\it\alterlega\recordsnext\gui\RecordsNextApp.java

File: src\main\java\it\alterlega\recordsnext\gui\RecordsNextApp.java

    package it.alterlega.recordsnext.gui;
    
    import it.alterlega.recordsnext.ConfigurationSchema;
    import it.alterlega.recordsnext.app.PipelineConfig;
    import it.alterlega.recordsnext.app.ProcessingOptions;
    import it.alterlega.recordsnext.app.ProcessingMode;
    import it.alterlega.recordsnext.app.RecordsNextPipeline;
    
    import javax.swing.*;
    import javax.swing.border.CompoundBorder;
    import javax.swing.border.EmptyBorder;
    import javax.swing.border.LineBorder;
    import javax.swing.text.DefaultCaret;
    import java.awt.*;
    import java.awt.event.WindowAdapter;
    import java.awt.image.BufferedImage;
    import java.awt.event.WindowEvent;
    import java.io.IOException;
    import java.io.InputStream;
    import java.io.OutputStream;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.util.Properties;
    
    public final class RecordsNextApp {
        private static final String KEY_CLASSIC = "processing.classic";
        private static final String KEY_RU = "processing.ru";
        private static final String KEY_GENERATE_JS = "processing.generateJs";
        private static final String KEY_PUBLISH = "processing.publish";
        private static final String KEY_MODE = "processing.mode";
        private static final String KEY_PUBLISH_MODE = "publish.destinationMode";
        private static final String KEY_PUBLISH_CUSTOM = "publish.customDirectory";
    
        private final JFrame frame = new JFrame("FCM RecordsNext 1.0");
        private final JCheckBox classic = new JCheckBox("Record classici");
        private final JCheckBox ru = new JCheckBox("Riserve d'ufficio");
        private final JCheckBox generateJs = new JCheckBox("Genera file JavaScript");
        private final JCheckBox publish = new JCheckBox("Pubblica i file nel sito");
        private final JRadioButton publishCurrent = new JRadioButton("Cartella js della stagione attuale");
        private final JRadioButton publishCustom = new JRadioButton("Cartella personalizzata");
        private final JTextField publishDirectory = new JTextField();
        private final JButton publishBrowse = new JButton("...");
        private final JLabel publishResolved = new JLabel(" ");
        private final JRadioButton fullMode = new JRadioButton("Elaborazione completa");
        private final JRadioButton consolidatedMode = new JRadioButton("Aggiornamento da consolidamento");
        private final JTextArea log = new JTextArea(10, 48);
        private final JProgressBar phaseProgress = new JProgressBar();
        private final JProgressBar progress = new JProgressBar(0, 100);
        private final JLabel phaseLabel = new JLabel("Nessuna operazione in corso");
        private final JButton start = new JButton("Avvia");
        private final JLabel status = new JLabel("Pronto", SwingConstants.CENTER);
        private final Path root = Path.of("").toAbsolutePath().normalize();
        private final Path configPath = root.resolve("config/recordsnext-gui.properties");
        private final Properties properties = new Properties();
        private boolean loadingSelections;
    
        public static void main(String[] args) {
            SwingUtilities.invokeLater(() -> new RecordsNextApp().show());
        }
    
        private RecordsNextApp() {
            bootstrapRuntimeDirectories();
            loadProperties();
            build();
            loadSelections();
        }
    
        private void bootstrapRuntimeDirectories() {
            String[] directories = {
                    "config",
                    "data/database"
            };
            try {
                for (String directory : directories) {
                    Files.createDirectories(root.resolve(directory));
                }
                ConfigurationSchema.initializeEmpty(root.resolve("data/database/recordsnext.db"));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null,
                        "Impossibile creare le cartelle di lavoro di RecordsNext:\n" + ex.getMessage(),
                        "FCM RecordsNext 1.0", JOptionPane.ERROR_MESSAGE);
                throw new IllegalStateException("Bootstrap delle cartelle fallito", ex);
            }
        }
    
        private void build() {
            UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 13));
            UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 13));
            UIManager.put("CheckBox.font", new Font("Segoe UI", Font.PLAIN, 13));
            UIManager.put("RadioButton.font", new Font("Segoe UI", Font.PLAIN, 13));
    
            Color background = new Color(244, 247, 252);
            Color panelBorder = new Color(196, 205, 222);
            Color blue = new Color(34, 72, 150);
            Color red = new Color(201, 34, 45);
    
            JPanel rootPanel = new JPanel(new BorderLayout(12, 12));
            rootPanel.setBorder(new EmptyBorder(16, 20, 12, 20));
            rootPanel.setBackground(background);
    
            JPanel header = new JPanel(new GridBagLayout());
            header.setOpaque(false);
            header.setPreferredSize(new Dimension(590, 112));
            header.setMinimumSize(new Dimension(590, 112));
            header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 112));
            GridBagConstraints hg = new GridBagConstraints();
            hg.gridx = 0;
            hg.weightx = 1;
            hg.fill = GridBagConstraints.HORIZONTAL;
            hg.anchor = GridBagConstraints.CENTER;
    
            JLabel title = new JLabel("FCM RecordsNext 1.0", SwingConstants.CENTER);
            title.setFont(new Font("Segoe UI Black", Font.BOLD, 35));
            title.setForeground(red);
            hg.gridy = 0;
            header.add(title, hg);
    
            JLabel sub = new JLabel("Records storici e tanto altro", SwingConstants.CENTER);
            sub.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
            sub.setForeground(new Color(62, 72, 92));
            hg.gridy = 1;
            hg.insets = new Insets(4, 0, 0, 0);
            header.add(sub, hg);
    
            status.setFont(new Font("Segoe UI", Font.BOLD, 13));
            status.setForeground(new Color(35, 105, 62));
            status.setPreferredSize(new Dimension(540, 26));
            status.setMinimumSize(new Dimension(540, 26));
            hg.gridy = 2;
            hg.insets = new Insets(11, 0, 0, 0);
            header.add(status, hg);
            rootPanel.add(header, BorderLayout.NORTH);
    
            JPanel center = new JPanel();
            center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
            center.setOpaque(false);
    
            JPanel options = new JPanel(new GridBagLayout());
            options.setBackground(Color.WHITE);
            options.setBorder(new CompoundBorder(
                    new LineBorder(panelBorder),
                    new EmptyBorder(12, 15, 12, 15)));
    
            GridBagConstraints g = new GridBagConstraints();
            g.gridx = 0;
            g.gridy = 0;
            g.anchor = GridBagConstraints.WEST;
            g.fill = GridBagConstraints.HORIZONTAL;
            g.weightx = 1;
            g.insets = new Insets(3, 4, 7, 4);
    
            JLabel modeTitle = new JLabel("Modalità");
            modeTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
            modeTitle.setForeground(blue);
            options.add(modeTitle, g);
            ButtonGroup modeGroup = new ButtonGroup();
            modeGroup.add(fullMode);
            modeGroup.add(consolidatedMode);
            g.gridy++;
            options.add(fullMode, g);
            g.gridy++;
            options.add(consolidatedMode, g);
            g.gridy++;
            g.insets = new Insets(10, 4, 7, 4);
    
            JLabel sectionTitle = new JLabel("Elaborazioni");
            sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
            sectionTitle.setForeground(blue);
            options.add(sectionTitle, g);
    
            g.insets = new Insets(2, 4, 2, 4);
            g.gridy++;
            options.add(classic, g);
            g.gridy++;
            options.add(ru, g);
            g.gridy++;
            options.add(generateJs, g);
            g.gridy++;
            options.add(publish, g);
    
            ButtonGroup publishGroup = new ButtonGroup();
            publishGroup.add(publishCurrent);
            publishGroup.add(publishCustom);
            JPanel publishDestination = new JPanel(new GridBagLayout());
            publishDestination.setOpaque(false);
            publishDestination.setBorder(new EmptyBorder(3, 24, 2, 0));
            GridBagConstraints dg = new GridBagConstraints();
            dg.gridx = 0;
            dg.gridy = 0;
            dg.gridwidth = 3;
            dg.anchor = GridBagConstraints.WEST;
            dg.fill = GridBagConstraints.HORIZONTAL;
            dg.weightx = 1;
            publishDestination.add(publishCurrent, dg);
            dg.gridy = 1;
            publishDestination.add(publishCustom, dg);
            dg.gridy = 2;
            dg.gridwidth = 1;
            dg.weightx = 1;
            publishDestination.add(publishDirectory, dg);
            dg.gridx = 1;
            dg.weightx = 0;
            dg.fill = GridBagConstraints.NONE;
            publishDestination.add(publishBrowse, dg);
            dg.gridx = 0;
            dg.gridy = 3;
            dg.gridwidth = 3;
            dg.fill = GridBagConstraints.HORIZONTAL;
            publishResolved.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            publishResolved.setForeground(new Color(90, 98, 112));
            publishDestination.add(publishResolved, dg);
            g.gridy++;
            g.insets = new Insets(0, 4, 2, 4);
            options.add(publishDestination, g);
    
            JLabel savedHint = new JLabel("Le scelte vengono memorizzate automaticamente.");
            savedHint.setForeground(new Color(90, 98, 112));
            savedHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            g.gridy++;
            g.insets = new Insets(8, 7, 1, 4);
            options.add(savedHint, g);
            options.setAlignmentX(Component.LEFT_ALIGNMENT);
            options.setMaximumSize(new Dimension(Integer.MAX_VALUE, options.getPreferredSize().height));
            center.add(options);
            center.add(Box.createVerticalStrut(10));
    
            JPanel progressPanel = new JPanel(new GridBagLayout());
            progressPanel.setBackground(Color.WHITE);
            progressPanel.setBorder(new CompoundBorder(
                    new LineBorder(panelBorder),
                    new EmptyBorder(10, 12, 10, 12)));
            GridBagConstraints pg = new GridBagConstraints();
            pg.gridx = 0;
            pg.weightx = 1;
            pg.fill = GridBagConstraints.HORIZONTAL;
            pg.anchor = GridBagConstraints.WEST;
            pg.insets = new Insets(2, 2, 3, 2);
    
            JLabel phaseTitle = new JLabel("Operazione corrente");
            phaseTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
            phaseTitle.setForeground(blue);
            pg.gridy = 0;
            progressPanel.add(phaseTitle, pg);
            phaseLabel.setPreferredSize(new Dimension(540, 22));
            phaseLabel.setMinimumSize(new Dimension(540, 22));
            pg.gridy = 1;
            progressPanel.add(phaseLabel, pg);
            phaseProgress.setIndeterminate(false);
            phaseProgress.setStringPainted(false);
            phaseProgress.setPreferredSize(new Dimension(540, 16));
            pg.gridy = 2;
            progressPanel.add(phaseProgress, pg);
    
            JLabel overallTitle = new JLabel("Avanzamento generale");
            overallTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
            overallTitle.setForeground(blue);
            pg.gridy = 3;
            pg.insets = new Insets(9, 2, 3, 2);
            progressPanel.add(overallTitle, pg);
            progress.setStringPainted(true);
            progress.setValue(0);
            progress.setString("0%");
            progress.setPreferredSize(new Dimension(540, 20));
            pg.gridy = 4;
            pg.insets = new Insets(2, 2, 2, 2);
            progressPanel.add(progress, pg);
            progressPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            progressPanel.setPreferredSize(new Dimension(560, 126));
            progressPanel.setMinimumSize(new Dimension(560, 126));
            progressPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 126));
            center.add(progressPanel);
            center.add(Box.createVerticalStrut(10));
    
            log.setEditable(false);
            log.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            log.setLineWrap(true);
            log.setWrapStyleWord(true);
            DefaultCaret logCaret = (DefaultCaret) log.getCaret();
            logCaret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
            JScrollPane logScroll = new JScrollPane(
                    log,
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            logScroll.setBorder(new LineBorder(panelBorder));
            logScroll.setPreferredSize(new Dimension(560, 190));
            logScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
            logScroll.setMinimumSize(new Dimension(560, 120));
            logScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
            logScroll.getVerticalScrollBar().setUnitIncrement(18);
            logScroll.getHorizontalScrollBar().setUnitIncrement(18);
            center.add(logScroll);
            rootPanel.add(center, BorderLayout.CENTER);
    
            JPanel south = new JPanel(new BorderLayout(0, 8));
            south.setOpaque(false);
            JPanel credits = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            credits.setOpaque(false);
            JLabel credit = new JLabel("powered by mauz79 © 2026");
            credit.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            credit.setForeground(new Color(82, 89, 105));
            credits.add(credit);
            south.add(credits, BorderLayout.NORTH);
    
            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
            buttons.setOpaque(false);
            JButton config = new JButton("Configurazione");
            JButton exit = new JButton("Esci");
            buttons.add(start);
            buttons.add(config);
            buttons.add(exit);
            south.add(buttons, BorderLayout.SOUTH);
            rootPanel.add(south, BorderLayout.SOUTH);
    
            fullMode.addActionListener(e -> saveSelections());
            consolidatedMode.addActionListener(e -> saveSelections());
            classic.addActionListener(e -> saveSelections());
            ru.addActionListener(e -> saveSelections());
            generateJs.addActionListener(e -> {
                if (!generateJs.isSelected()) {
                    publish.setSelected(false);
                }
                publish.setEnabled(generateJs.isSelected());
                updatePublishControls();
                saveSelections();
            });
            publish.addActionListener(e -> {
                updatePublishControls();
                saveSelections();
            });
            publishCurrent.addActionListener(e -> {
                updatePublishControls();
                saveSelections();
            });
            publishCustom.addActionListener(e -> {
                updatePublishControls();
                saveSelections();
            });
            publishBrowse.addActionListener(e -> choosePublishDirectory());
            start.addActionListener(e -> runPipeline());
            exit.addActionListener(e -> closeApplication());
            config.addActionListener(e -> openConfiguration());
    
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    saveSelections();
                }
            });
    
            frame.setIconImage(createAppIcon());
            frame.setContentPane(rootPanel);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setResizable(true);
            frame.setMinimumSize(new Dimension(660, 760));
            frame.setPreferredSize(new Dimension(680, 920));
            frame.pack();
            frame.setLocationRelativeTo(null);
        }
    
        private static Image createAppIcon() {
            BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = image.createGraphics();
            try {
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics.setColor(new Color(201, 34, 45));
                graphics.fillRoundRect(3, 3, 58, 58, 14, 14);
                graphics.setColor(Color.WHITE);
                graphics.setFont(new Font("Segoe UI", Font.BOLD, 25));
                FontMetrics metrics = graphics.getFontMetrics();
                String text = "RN";
                graphics.drawString(text, (64 - metrics.stringWidth(text)) / 2, 42);
            } finally {
                graphics.dispose();
            }
            return image;
        }
    
        private void show() {
            frame.setVisible(true);
        }
    
        private void loadProperties() {
            if (!Files.isRegularFile(configPath)) {
                return;
            }
            try (InputStream input = Files.newInputStream(configPath)) {
                properties.load(input);
            } catch (IOException ex) {
                throw new IllegalStateException("Impossibile leggere " + configPath, ex);
            }
        }
    
        private void loadSelections() {
            loadingSelections = true;
            try {
                classic.setSelected(readBoolean(KEY_CLASSIC, true));
                ru.setSelected(readBoolean(KEY_RU, false));
                generateJs.setSelected(readBoolean(KEY_GENERATE_JS, true));
                publish.setSelected(readBoolean(KEY_PUBLISH, true) && generateJs.isSelected());
                publish.setEnabled(generateJs.isSelected());
                boolean customDestination = "custom".equalsIgnoreCase(
                    properties.getProperty(KEY_PUBLISH_MODE, "currentSeason"));
                publishCustom.setSelected(customDestination);
                publishCurrent.setSelected(!customDestination);
                publishDirectory.setText(properties.getProperty(KEY_PUBLISH_CUSTOM, ""));
                updatePublishControls();
                boolean consolidated = "CONSOLIDATED".equalsIgnoreCase(properties.getProperty(KEY_MODE, "FULL"));
                consolidatedMode.setSelected(consolidated);
                fullMode.setSelected(!consolidated);
                try {
                    var cfg = PipelineConfig.load(root, configPath);
                    boolean available = new RecordsNextPipeline().hasConsolidation(cfg);
                    consolidatedMode.setEnabled(available);
                    if (!available) fullMode.setSelected(true);
                } catch (Exception ignored) {
                    consolidatedMode.setEnabled(false);
                    fullMode.setSelected(true);
                }
            } finally {
                loadingSelections = false;
            }
        }
    
        private boolean readBoolean(String key, boolean defaultValue) {
            String value = properties.getProperty(key);
            return value == null ? defaultValue : Boolean.parseBoolean(value.trim());
        }
    
        private void saveSelections() {
            if (loadingSelections) {
                return;
            }
            properties.setProperty(KEY_CLASSIC, Boolean.toString(classic.isSelected()));
            properties.setProperty(KEY_RU, Boolean.toString(ru.isSelected()));
            properties.setProperty(KEY_GENERATE_JS, Boolean.toString(generateJs.isSelected()));
            properties.setProperty(KEY_PUBLISH, Boolean.toString(publish.isSelected()));
            properties.setProperty(KEY_MODE, consolidatedMode.isSelected() ? "CONSOLIDATED" : "FULL");
            properties.setProperty(KEY_PUBLISH_MODE,
                publishCustom.isSelected() ? "custom" : "currentSeason");
            properties.setProperty(KEY_PUBLISH_CUSTOM, publishDirectory.getText().trim());
            try {
                Files.createDirectories(configPath.getParent());
                try (OutputStream output = Files.newOutputStream(configPath)) {
                    properties.store(output, "RecordsNext configuration");
                }
            } catch (IOException ex) {
                status.setText("Impossibile salvare la configurazione");
                log.append("AVVISO: impossibile salvare " + configPath + ": " + ex.getMessage()
                        + System.lineSeparator());
            }
        }
    
        private void openConfiguration() {
            saveSelections();
            RecordsNextConfigurationDialog dialog =
                    new RecordsNextConfigurationDialog(frame, root, configPath);
            if (dialog.open()) {
                properties.clear();
                loadProperties();
                loadSelections();
                status.setText("Configurazione salvata");
                log.append("Configurazione aggiornata." + System.lineSeparator());
            }
        }
    
        private void updatePublishControls() {
            boolean enabled = generateJs.isSelected() && publish.isSelected();
            publishCurrent.setEnabled(enabled);
            publishCustom.setEnabled(enabled);
            boolean custom = enabled && publishCustom.isSelected();
            publishDirectory.setEnabled(custom);
            publishBrowse.setEnabled(custom);
            try {
                Path resolved = PipelineConfig.resolvePublishDirectory(root, propertiesForCurrentUi());
                publishResolved.setText("Destinazione: " + resolved);
            } catch (Exception ex) {
                publishResolved.setText("Destinazione non disponibile");
            }
        }
    
        private Properties propertiesForCurrentUi() {
            Properties copy = new Properties();
            copy.putAll(properties);
            copy.setProperty(KEY_PUBLISH_MODE,
                publishCustom.isSelected() ? "custom" : "currentSeason");
            copy.setProperty(KEY_PUBLISH_CUSTOM, publishDirectory.getText().trim());
            return copy;
        }
    
        private void choosePublishDirectory() {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Seleziona la cartella di pubblicazione");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            String current = publishDirectory.getText().trim();
            if (!current.isEmpty() && Files.isDirectory(Path.of(current))) {
                chooser.setCurrentDirectory(Path.of(current).toFile());
            } else {
                String remembered = properties.getProperty("chooser.lastPublishDirectory", "").trim();
                if (!remembered.isEmpty() && Files.isDirectory(Path.of(remembered))) {
                    chooser.setCurrentDirectory(Path.of(remembered).toFile());
                }
            }
            if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                Path selected = chooser.getSelectedFile().toPath().toAbsolutePath().normalize();
                publishDirectory.setText(selected.toString());
                properties.setProperty("chooser.lastPublishDirectory", selected.toString());
                updatePublishControls();
                saveSelections();
            }
        }
    
        private void closeApplication() {
            saveSelections();
            frame.dispose();
        }
    
        private void runPipeline() {
            saveSelections();
            if (publish.isSelected() && publishCustom.isSelected()
                    && publishDirectory.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(frame,
                    "Selezionare la cartella personalizzata di pubblicazione.",
                    "RecordsNext", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                var cfg = PipelineConfig.load(root, configPath);
                if (publish.isSelected() && !Files.isDirectory(cfg.siteJs())) {
                    JOptionPane.showMessageDialog(frame,
                        "La cartella di pubblicazione non esiste:\n" + cfg.siteJs(),
                        "RecordsNext", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                HistoricalMappingRepository repository = new HistoricalMappingRepository(
                    root.resolve("data/database/recordsnext.db"));
                repository.prepare();
                String incompleteSeason = null;
                for (String season : repository.seasonsNewestFirst()) {
                    if (cfg.seasons().contains(season) && repository.pending(season) > 0) {
                        incompleteSeason = season;
                        break;
                    }
                }
                if (incompleteSeason != null) {
                    int pending = repository.pending(incompleteSeason);
                    status.setText("Configurazione incompleta");
                    JOptionPane.showMessageDialog(frame,
                        incompleteSeason + ": restano " + pending + " associazioni da configurare.",
                        "RecordsNext", JOptionPane.WARNING_MESSAGE);
                    HistoricalMappingDialog dialog = new HistoricalMappingDialog(frame, repository, incompleteSeason);
                    dialog.open();
                    return;
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "RecordsNext", JOptionPane.ERROR_MESSAGE);
                return;
            }
            final ProcessingOptions options;
            try {
                options = new ProcessingOptions(
                        classic.isSelected(), ru.isSelected(),
                        generateJs.isSelected(), publish.isSelected());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        frame, ex.getMessage(), "RecordsNext", JOptionPane.WARNING_MESSAGE);
                return;
            }
    
            start.setEnabled(false);
            log.setText("");
            progress.setValue(0);
            progress.setString("0%");
            phaseLabel.setText("Preparazione elaborazione");
            phaseProgress.setIndeterminate(true);
            status.setText("Elaborazione in corso");
            status.setForeground(new Color(35, 82, 150));
    
            new SwingWorker<RecordsNextPipeline.Result, String>() {
                @Override
                protected RecordsNextPipeline.Result doInBackground() throws Exception {
                    var cfg = PipelineConfig.load(root, configPath);
                    ProcessingMode mode = consolidatedMode.isSelected()
                        ? ProcessingMode.CONSOLIDATED : ProcessingMode.FULL;
                    return new RecordsNextPipeline().run(cfg, options, mode,
                        new RecordsNextPipeline.Listener() {
                            @Override
                            public void phase(String text, int percent) {
                                publish(text);
                                SwingUtilities.invokeLater(() -> {
                                    phaseLabel.setText(text);
                                    if (percent >= 0) {
                                        progress.setValue(percent);
                                        progress.setString(percent + "%");
                                    }
                                });
                            }
    
                            @Override
                            public void timing(String text) {
                                publish("TEMPO  " + text);
                            }
                        });
                }
    
                @Override
                protected void process(java.util.List<String> chunks) {
                    chunks.forEach(value -> log.append(value + System.lineSeparator()));
                }
    
                @Override
                protected void done() {
                    try {
                        var result = get();
                        log.append("File validi: " + result.files()
                                + "; pubblicati: " + result.published()
                                + System.lineSeparator());
                        status.setText("Elaborazione completata");
                        status.setForeground(new Color(35, 105, 62));
                        phaseLabel.setText("Elaborazione completata");
                        phaseProgress.setIndeterminate(false);
                        phaseProgress.setValue(100);
                        consolidatedMode.setEnabled(true);
                    } catch (Exception ex) {
                        status.setText("Errore");
                        status.setForeground(new Color(178, 38, 45));
                        phaseLabel.setText("Elaborazione interrotta");
                        phaseProgress.setIndeterminate(false);
                        phaseProgress.setValue(0);
                        Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                        log.append("ERRORE: " + cause + System.lineSeparator());
                        JOptionPane.showMessageDialog(
                                frame, String.valueOf(cause),
                                "Errore RecordsNext", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        start.setEnabled(true);
                    }
                }
            }.execute();
        }
    }

## src\main\java\it\alterlega\recordsnext\gui\RecordsNextConfigurationDialog.java

File: src\main\java\it\alterlega\recordsnext\gui\RecordsNextConfigurationDialog.java

    package it.alterlega.recordsnext.gui;
    
    import it.alterlega.recordsnext.ConfigurationSchema;
    import it.alterlega.recordsnext.RawSqliteImporter;
    
    import javax.swing.*;
    import javax.swing.border.*;
    import javax.swing.filechooser.FileNameExtensionFilter;
    import java.awt.*;
    import java.io.*;
    import java.nio.file.*;
    import java.util.*;
    import java.util.List;
    import java.util.regex.Pattern;
    import java.util.stream.Collectors;
    
    final class RecordsNextConfigurationDialog extends JDialog {
        private static final Pattern SEASON = Pattern.compile("\\d{4}_\\d{4}");
        private final Path projectRoot, configPath, databasePath;
        private final Properties properties = new Properties();
        private final JPanel seasonsPanel = new JPanel();
        private final List<SeasonEditor> editors = new ArrayList<>();
        private final SeasonConfigurationRepository repository;
        private boolean saved;
    
        RecordsNextConfigurationDialog(Window owner, Path projectRoot, Path configPath) {
            super(owner,"RecordsNext - Configurazione stagioni",ModalityType.APPLICATION_MODAL);
            this.projectRoot=projectRoot; this.configPath=configPath;
            loadProperties();
            this.databasePath=projectRoot.resolve(properties.getProperty("database","data/database/recordsnext.db")).normalize();
            this.repository=new SeasonConfigurationRepository(databasePath);
            build(); loadSeasons();
        }
        boolean open(){ setVisible(true); return saved; }
    
        private void build(){
            JPanel root=new JPanel(new BorderLayout(10,10)); root.setBorder(new EmptyBorder(12,14,12,14));
            JPanel top=new JPanel(new BorderLayout());
            JLabel info=new JLabel("Configurare le stagioni gestite o manuali e, successivamente, i relativi siti.");
            JPanel topButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            JButton mappings = new JButton("Configura associazioni storiche...");
            mappings.addActionListener(e -> openMappings());
            JButton add=new JButton("Aggiungi stagione"); add.addActionListener(e->addSeason());
            topButtons.add(mappings); topButtons.add(add);
            top.add(info,BorderLayout.WEST); top.add(topButtons,BorderLayout.EAST); root.add(top,BorderLayout.NORTH);
            seasonsPanel.setLayout(new BoxLayout(seasonsPanel,BoxLayout.Y_AXIS)); seasonsPanel.setBorder(new EmptyBorder(4,4,4,4));
            JScrollPane scroll=new JScrollPane(seasonsPanel); scroll.getVerticalScrollBar().setUnitIncrement(20); root.add(scroll,BorderLayout.CENTER);
            JPanel buttons=new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton cancel=new JButton("Annulla"), save=new JButton("Salva");
            cancel.addActionListener(e->dispose()); save.addActionListener(e->saveConfiguration());
            buttons.add(cancel); buttons.add(save); root.add(buttons,BorderLayout.SOUTH);
            setContentPane(root); setDefaultCloseOperation(DISPOSE_ON_CLOSE); setSize(980,720); setMinimumSize(new Dimension(860,600)); setLocationRelativeTo(getOwner());
        }
    
        private void loadProperties(){
            if(Files.isRegularFile(configPath)) try(InputStream in=Files.newInputStream(configPath)){properties.load(in);} catch(IOException ex){error("Lettura configurazione",ex);}
        }
        private void loadSeasons(){
            try {
                if (Files.isRegularFile(databasePath)) {
                    new HistoricalMappingRepository(databasePath).prepare();
                }
            } catch (Exception ex) {
                error("Pulizia configurazione storica", ex);
            }
            editors.clear(); seasonsPanel.removeAll();
            Set<String> selected=Arrays.stream(properties.getProperty("seasons","").split(",")).map(String::trim).filter(s->!s.isEmpty()).collect(Collectors.toSet());
            try{
                for(var loaded:repository.load()) {
                    var row = refreshManagedMetadata(loaded);
                    addEditor(new SeasonEditor(row,selected.contains(row.seasonId())));
                }
            }catch(Exception ex){error("Lettura stagioni",ex);}
            refresh();
        }
    
    
        private SeasonConfigurationRepository.SeasonRow refreshManagedMetadata(
            SeasonConfigurationRepository.SeasonRow row
        ) {
            if (!"GESTITA".equals(row.managementType()) || row.fcmPath().isBlank()) {
                return row;
            }
            try {
                Path fcm = Path.of(row.fcmPath());
                if (!Files.isRegularFile(fcm)) return row;
                var detection = new FcmSeasonDetector().detect(fcm);
                if (!row.seasonId().equals(detection.seasonId())) {
                    return row;
                }
                return new SeasonConfigurationRepository.SeasonRow(
                    row.seasonId(),
                    detection.seasonNumber(),
                    row.anchor(),
                    row.managementType(),
                    row.status(),
                    row.fcmPath(),
                    row.fcaPath(),
                    row.localSitePath(),
                    row.onlineSiteUrl()
                );
            } catch (Exception ignored) {
                return row;
            }
        }
    
        private void addSeason(){
            try {
                List<SeasonConfigurationRepository.SeasonRow> current=editors.stream().map(SeasonEditor::value).toList();
                AddSeasonWizard wizard=new AddSeasonWizard(this,repository,current,properties,configPath);
                SeasonConfigurationRepository.SeasonRow row=wizard.open();
                if(row==null)return;
                if(editors.stream().anyMatch(e->e.row.seasonId().equals(row.seasonId()))){warn("La stagione è già presente.");return;}
                List<SeasonConfigurationRepository.SeasonRow> rows = new ArrayList<>();
                for (SeasonEditor editor : editors) rows.add(editor.value());
                rows.add(row);
                repository.save(rows);
                selectSeasonByDefault(row.seasonId());
    
                if ("GESTITA".equals(row.managementType())) {
                    importForConfiguration(row, rows);
                }
    
                loadSeasons();
                SeasonConfigurationRepository.SeasonRow loaded = repository.load().stream()
                    .filter(r -> r.seasonId().equals(row.seasonId()))
                    .findFirst().orElse(row);
                if ("GESTITA".equals(loaded.managementType()) && !loaded.anchor()) {
                    openMappings();
                    loadSeasons();
                }
            } catch(Exception ex) { error("Aggiunta stagione",ex); }
        }
    
        private void selectSeasonByDefault(String seasonId) throws IOException {
            LinkedHashSet<String> selected = Arrays.stream(properties.getProperty("seasons", "").split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
            selected.add(seasonId);
            properties.setProperty("seasons", String.join(",", selected));
            Files.createDirectories(configPath.getParent());
            try (OutputStream out = Files.newOutputStream(configPath)) {
                properties.store(out, "RecordsNext configuration");
            }
        }
    
        private void importForConfiguration(
            SeasonConfigurationRepository.SeasonRow row,
            List<SeasonConfigurationRepository.SeasonRow> allRows
        ) throws Exception {
            RawSqliteImporter.main(new String[]{row.fcmPath(), "FCM", row.seasonId(), databasePath.toString()});
            RawSqliteImporter.main(new String[]{row.fcaPath(), "FCA", row.seasonId(), databasePath.toString()});
            String anchor = allRows.stream()
                .filter(r -> "GESTITA".equals(r.managementType()))
                .max(Comparator.comparingInt(r -> Integer.parseInt(r.seasonId().substring(0, 4))))
                .orElseThrow(() -> new IllegalStateException("Nessuna stagione gestita"))
                .seasonId();
            ConfigurationSchema.main(new String[]{databasePath.toString(), anchor});
        }
    
        private void openMappings() {
            try {
                HistoricalMappingRepository mappingRepository = new HistoricalMappingRepository(databasePath);
                HistoricalMappingDialog dialog = new HistoricalMappingDialog(this, mappingRepository);
                dialog.open();
                loadSeasons();
            } catch (Exception ex) {
                error("Associazioni storiche", ex);
            }
        }
        private void addEditor(SeasonEditor e){editors.add(e); seasonsPanel.add(e.panel); seasonsPanel.add(Box.createVerticalStrut(8));}
        private void refresh(){seasonsPanel.revalidate();seasonsPanel.repaint();}
    
        private void saveConfiguration(){
            if(editors.isEmpty()){warn("Aggiungere almeno una stagione.");return;}
            List<SeasonConfigurationRepository.SeasonRow> rows=new ArrayList<>();
            for(SeasonEditor e:editors){String problem=e.validateFields(); if(problem!=null){warn(problem);return;} rows.add(e.value());}
            List<String> selected=editors.stream().filter(e->e.include.isSelected()).map(e->e.row.seasonId()).toList();
            if(selected.isEmpty()){warn("Selezionare almeno una stagione da elaborare.");return;}
            try {
                HistoricalMappingRepository mappingRepository = new HistoricalMappingRepository(databasePath);
                for (SeasonConfigurationRepository.SeasonRow row : rows) {
                    if (selected.contains(row.seasonId()) && "GESTITA".equals(row.managementType()) && !row.anchor()) {
                        int pending = mappingRepository.pending(row.seasonId());
                        if (pending > 0) {
                            warn(row.seasonId() + ": restano " + pending + " associazioni da configurare.");
                            return;
                        }
                    }
                }
            } catch (Exception ex) {
                error("Verifica associazioni", ex);
                return;
            }
            properties.setProperty("seasons",String.join(",",selected));
            rows.stream().filter(r->"GESTITA".equals(r.managementType())).max(Comparator.comparing(r->r.seasonId())).ifPresent(current->
                properties.setProperty("siteJs",Path.of(current.localSitePath()).resolve("js").toString()));
            try{
                repository.save(rows);
                Files.createDirectories(configPath.getParent());
                try(OutputStream out=Files.newOutputStream(configPath)){properties.store(out,"RecordsNext configuration");}
                saved=true;dispose();
            }catch(Exception ex){error("Salvataggio configurazione",ex);}
        }
    
        private void remove(SeasonEditor e){
            int x=JOptionPane.showConfirmDialog(this,"Rimuovere "+e.row.seasonId()+" dalla configurazione?\nI dati già importati non saranno cancellati.","RecordsNext",JOptionPane.YES_NO_OPTION);
            if(x!=JOptionPane.YES_OPTION)return;
            try{repository.removeConfiguration(e.row.seasonId());}catch(Exception ex){error("Rimozione stagione",ex);return;}
            int i=editors.indexOf(e); editors.remove(e); seasonsPanel.remove(e.panel); if(i<seasonsPanel.getComponentCount()) seasonsPanel.remove(i); refresh();
        }
    
        private void choose(JTextField field, int mode, String extension) {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(mode);
            configureExtensionFilter(chooser, extension);
    
            String text = field.getText().trim();
            Path directory = null;
            if (!text.isEmpty()) {
                Path path = Path.of(text);
                directory = Files.isDirectory(path) ? path : path.getParent();
            }
            String chooserKey = chooserKey(extension, mode);
            if ((directory == null || !Files.exists(directory)) && chooserKey != null) {
                String remembered = properties.getProperty(chooserKey, "").trim();
                if (!remembered.isEmpty() && Files.isDirectory(Path.of(remembered))) directory = Path.of(remembered);
            }
            if (directory != null && Files.exists(directory)) chooser.setCurrentDirectory(directory.toFile());
    
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                Path selected = chooser.getSelectedFile().toPath().toAbsolutePath().normalize();
                if (!hasExtension(selected, extension)) {
                    warn("Selezionare un file " + extension);
                    return;
                }
                field.setText(selected.toString());
                Path rememberedDirectory = mode == JFileChooser.DIRECTORIES_ONLY ? selected : selected.getParent();
                rememberChooserDirectory(extension, mode, rememberedDirectory);
                if (".fcm".equalsIgnoreCase(extension)) {
                    rememberChooserDirectory(".fca", JFileChooser.FILES_ONLY, selected.getParent());
                }
            }
        }
    
        private static String chooserKey(String extension, int mode) {
            if (mode == JFileChooser.DIRECTORIES_ONLY) return "chooser.lastSiteDirectory";
            if (".fcm".equalsIgnoreCase(extension)) return "chooser.lastFcmDirectory";
            if (".fca".equalsIgnoreCase(extension)) return "chooser.lastFcaDirectory";
            return null;
        }
    
        private void rememberChooserDirectory(String extension, int mode, Path directory) {
            String key = chooserKey(extension, mode);
            if (key == null || directory == null) return;
            properties.setProperty(key, directory.toString());
            try {
                Files.createDirectories(configPath.getParent());
                try (OutputStream out = Files.newOutputStream(configPath)) { properties.store(out, "RecordsNext configuration"); }
            } catch (IOException ignored) { }
        }
    
        private static void configureExtensionFilter(JFileChooser chooser, String extension) {
            if (extension == null || extension.isBlank()) {
                return;
            }
            String normalized = extension.startsWith(".") ? extension.substring(1) : extension;
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setFileFilter(new FileNameExtensionFilter(
                "File " + normalized.toUpperCase(Locale.ROOT) + " (*." + normalized + ")",
                normalized
            ));
        }
    
        private static boolean hasExtension(Path path, String extension) {
            if (extension == null || extension.isBlank()) {
                return true;
            }
            return path.getFileName().toString().toLowerCase(Locale.ROOT)
                .endsWith(extension.toLowerCase(Locale.ROOT));
        }
        private void warn(String m){JOptionPane.showMessageDialog(this,m,"RecordsNext",JOptionPane.WARNING_MESSAGE);} private void error(String m,Exception e){JOptionPane.showMessageDialog(this,m+":\n"+e.getMessage(),"Errore RecordsNext",JOptionPane.ERROR_MESSAGE);}
    
        private final class SeasonEditor{
            final SeasonConfigurationRepository.SeasonRow row; final JPanel panel=new JPanel(new GridBagLayout());
            final JCheckBox include=new JCheckBox("Elabora");
            final JTextField fcm=new JTextField(),fca=new JTextField(),site=new JTextField(),online=new JTextField(); final JLabel js=new JLabel(),dataa=new JLabel();
            SeasonEditor(SeasonConfigurationRepository.SeasonRow row,boolean selected){this.row=row;include.setSelected(selected);fcm.setText(row.fcmPath());fca.setText(row.fcaPath());site.setText(row.localSitePath());online.setText(row.onlineSiteUrl());build();updateDerived();}
            void build(){
                panel.setAlignmentX(Component.LEFT_ALIGNMENT); panel.setMaximumSize(new Dimension(Integer.MAX_VALUE,315)); panel.setBackground(Color.WHITE); panel.setBorder(new CompoundBorder(new LineBorder(new Color(190,199,214)),new EmptyBorder(9,10,9,10)));
                GridBagConstraints g=new GridBagConstraints();g.gridy=0;g.gridx=0;g.gridwidth=2;g.anchor=GridBagConstraints.WEST;
                String current=row.anchor()?"  -  ATTUALE":"";
                JLabel title=new JLabel("Stagione "+row.seasonId()+"  (#"+row.seasonNumber()+")  -  "+row.managementType()+current);title.setFont(title.getFont().deriveFont(Font.BOLD,14f));title.setForeground(new Color(25,67,160));panel.add(title,g);
                JPanel flags=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));flags.setOpaque(false);flags.add(include);JButton remove=new JButton("Rimuovi");remove.addActionListener(e->remove(this));flags.add(remove);g.gridx=2;g.gridwidth=2;g.weightx=1;g.anchor=GridBagConstraints.EAST;panel.add(flags,g);
                if("GESTITA".equals(row.managementType())) {addPath("File FCM",fcm,1,JFileChooser.FILES_ONLY,".fcm"); addPath("File FCA",fca,2,JFileChooser.FILES_ONLY,".fca");}
                else {addReadOnly("File FCM","Stagione manuale: non previsto",1); addPath("File FCA (facoltativo)",fca,2,JFileChooser.FILES_ONLY,".fca");}
                addPath("Cartella sito locale",site,3,JFileChooser.DIRECTORIES_ONLY,null); addText("Sito online",online,4);
                site.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){public void insertUpdate(javax.swing.event.DocumentEvent e){updateDerived();}public void removeUpdate(javax.swing.event.DocumentEvent e){updateDerived();}public void changedUpdate(javax.swing.event.DocumentEvent e){updateDerived();}});
                addLabel("Cartella JS",js,5);addLabel("DataA.js",dataa,6);
                if ("GESTITA".equals(row.managementType()) && !row.anchor()) {
                    JLabel mappingStatus = new JLabel();
                    mappingStatus.setName("mappingStatus");
                    addLabel("Associazioni", mappingStatus, 7);
                    updateMappingStatus();
                } else if (row.anchor()) {
                    JLabel currentStatus = new JLabel("Identità della stagione attuale");
                    currentStatus.setForeground(new Color(20,120,55));
                    addLabel("Associazioni", currentStatus, 7);
                }
            }
    
            void updateMappingStatus() {
                for (Component component : panel.getComponents()) {
                    if (component instanceof JLabel label && "mappingStatus".equals(label.getName())) {
                        try {
                            int pending = new HistoricalMappingRepository(databasePath).pending(row.seasonId());
                            label.setText(pending == 0 ? "Complete" : pending + " da configurare");
                            label.setForeground(pending == 0 ? new Color(20,120,55) : new Color(170,55,35));
                        } catch (Exception ex) {
                            label.setText("Stato non disponibile");
                            label.setForeground(new Color(170,55,35));
                        }
                    }
                }
            }
            void addPath(String label,JTextField field,int y,int mode,String ext){GridBagConstraints g=base(label,y);g.gridx=1;g.gridwidth=2;g.weightx=1;g.fill=GridBagConstraints.HORIZONTAL;panel.add(field,g);JButton b=new JButton("...");b.addActionListener(e->choose(field,mode,ext));g.gridx=3;g.gridwidth=1;g.weightx=0;g.fill=GridBagConstraints.NONE;panel.add(b,g);}
            void addText(String label,JTextField field,int y){GridBagConstraints g=base(label,y);g.gridx=1;g.gridwidth=3;g.weightx=1;g.fill=GridBagConstraints.HORIZONTAL;panel.add(field,g);}
            void addReadOnly(String label,String text,int y){JLabel value=new JLabel(text);value.setForeground(Color.GRAY);addLabel(label,value,y);}
            void addLabel(String label,JLabel value,int y){GridBagConstraints g=base(label,y);g.gridx=1;g.gridwidth=3;g.weightx=1;g.fill=GridBagConstraints.HORIZONTAL;panel.add(value,g);}
            GridBagConstraints base(String label,int y){GridBagConstraints g=new GridBagConstraints();g.gridy=y;g.gridx=0;g.anchor=GridBagConstraints.WEST;g.insets=new Insets(3,2,3,8);panel.add(new JLabel(label+":"),g);return g;}
            void updateDerived(){String s=site.getText().trim();if(s.isEmpty()){js.setText("-");dataa.setText("-");return;}Path j=Path.of(s).resolve("js");Path d=j.resolve("DataA.js");js.setText(j.toString());dataa.setText((Files.isRegularFile(d)?"Trovato: ":"Non trovato: ")+d);dataa.setForeground(Files.isRegularFile(d)?new Color(20,120,55):new Color(170,55,35));}
            String validateFields(){if("GESTITA".equals(row.managementType())){if(fcm.getText().trim().isEmpty()||!Files.isRegularFile(Path.of(fcm.getText().trim())))return row.seasonId()+": selezionare un file FCM esistente.";if(fca.getText().trim().isEmpty()||!Files.isRegularFile(Path.of(fca.getText().trim())))return row.seasonId()+": selezionare un file FCA esistente.";}else if(!fca.getText().trim().isEmpty()&&!Files.isRegularFile(Path.of(fca.getText().trim())))return row.seasonId()+": il file FCA indicato non esiste.";if(site.getText().trim().isEmpty()||!Files.isDirectory(Path.of(site.getText().trim())))return row.seasonId()+": selezionare una cartella sito esistente.";return null;}
            SeasonConfigurationRepository.SeasonRow value(){return new SeasonConfigurationRepository.SeasonRow(row.seasonId(),row.seasonNumber(),row.anchor(),row.managementType(),row.status(),"GESTITA".equals(row.managementType())?fcm.getText().trim():"",fca.getText().trim(),site.getText().trim(),online.getText().trim());}
        }
    
        private static final class AddSeasonWizard extends JDialog {
            private final JRadioButton managed = new JRadioButton("Gestita", true);
            private final JRadioButton manual = new JRadioButton("Manuale");
            private final JTextField fcm = new JTextField();
            private final JTextField fca = new JTextField();
            private final JTextField manualSeason = new JTextField();
            private final JTextField manualNumber = new JTextField();
            private final JLabel detected = new JLabel(" ");
            private final SeasonConfigurationRepository repo;
            private final List<SeasonConfigurationRepository.SeasonRow> current;
            private final Properties properties;
            private final Path configPath;
            private SeasonConfigurationRepository.SeasonRow result;
    
            AddSeasonWizard(
                Window owner,
                SeasonConfigurationRepository repo,
                List<SeasonConfigurationRepository.SeasonRow> current,
                Properties properties,
                Path configPath
            ) {
                super(owner, "RecordsNext - Aggiungi stagione", ModalityType.APPLICATION_MODAL);
                this.repo = repo;
                this.current = current;
                this.properties = properties;
                this.configPath = configPath;
                build();
            }
    
            SeasonConfigurationRepository.SeasonRow open() {
                setVisible(true);
                return result;
            }
    
            private void build() {
                setLayout(new BorderLayout(10, 10));
                ((JComponent) getContentPane()).setBorder(new EmptyBorder(14, 16, 12, 16));
    
                ButtonGroup group = new ButtonGroup();
                group.add(managed);
                group.add(manual);
    
                JPanel form = new JPanel(new GridBagLayout());
                form.setBorder(new TitledBorder("Tipo e sorgenti della stagione"));
    
                GridBagConstraints g = new GridBagConstraints();
                g.insets = new Insets(5, 5, 5, 5);
                g.anchor = GridBagConstraints.WEST;
                g.gridx = 0;
                g.gridy = 0;
                form.add(managed, g);
                g.gridx = 1;
                form.add(manual, g);
    
                addChooser(form, "File FCM", fcm, 1, ".fcm");
                addChooser(form, "File FCA", fca, 2, ".fca");
                addField(form, "Stagione manuale (AAAA_AAAA)", manualSeason, 3);
                addField(form, "Numero stagione", manualNumber, 4);
                addValue(form, "Dati rilevati", detected, 5);
    
                managed.addActionListener(e -> updateMode());
                manual.addActionListener(e -> updateMode());
                updateMode();
    
                add(form, BorderLayout.CENTER);
    
                JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                JButton add = new JButton("Aggiungi stagione");
                JButton cancel = new JButton("Annulla");
                add.addActionListener(e -> finish());
                cancel.addActionListener(e -> dispose());
                buttons.add(add);
                buttons.add(cancel);
                add(buttons, BorderLayout.SOUTH);
    
                setSize(720, 390);
                setLocationRelativeTo(getOwner());
            }
    
            private void updateMode() {
                boolean isManaged = managed.isSelected();
                fcm.setEnabled(isManaged);
                manualSeason.setEnabled(!isManaged);
                manualNumber.setEnabled(!isManaged);
                detected.setText(isManaged
                    ? "Stagione e numero saranno letti dal file FCM."
                    : "Inserire stagione e numero manualmente.");
            }
    
            private void finish() {
                try {
                    String seasonId;
                    int seasonNumber;
                    String type;
                    String fcmPath = "";
    
                    if (managed.isSelected()) {
                        if (!file(fcm, ".fcm") || !file(fca, ".fca")) return;
                        var detection = new FcmSeasonDetector().detect(Path.of(fcm.getText().trim()));
                        seasonId = detection.seasonId();
                        seasonNumber = detection.seasonNumber();
                        type = "GESTITA";
                        fcmPath = fcm.getText().trim();
                        detected.setText(
                            seasonId + " (#" + seasonNumber + ") - " + detection.evidence()
                        );
                    } else {
                        seasonId = manualSeason.getText().trim();
                        if (!SEASON.matcher(seasonId).matches()) {
                            warn("Formato stagione non valido.");
                            return;
                        }
                        try {
                            seasonNumber = Integer.parseInt(manualNumber.getText().trim());
                        } catch (NumberFormatException ex) {
                            warn("Indicare un numero stagione valido.");
                            return;
                        }
                        if (seasonNumber < 1) {
                            warn("Il numero stagione deve essere positivo.");
                            return;
                        }
                        if (!fca.getText().trim().isEmpty()
                            && !Files.isRegularFile(Path.of(fca.getText().trim()))) {
                            warn("Il file FCA indicato non esiste.");
                            return;
                        }
                        type = "MANUALE";
                    }
    
                    if (current.stream().anyMatch(r -> r.seasonId().equals(seasonId))) {
                        warn("La stagione " + seasonId + " è già presente.");
                        return;
                    }
    
                    result = new SeasonConfigurationRepository.SeasonRow(
                        seasonId,
                        seasonNumber,
                        false,
                        type,
                        "DA_CONFIGURARE",
                        fcmPath,
                        fca.getText().trim(),
                        "",
                        ""
                    );
                    dispose();
                } catch (Exception ex) {
                    warn(ex.getMessage());
                }
            }
    
            private boolean file(JTextField field, String extension) {
                String value = field.getText().trim();
                if (value.isEmpty() || !Files.isRegularFile(Path.of(value))) {
                    warn("Selezionare un file " + extension + " esistente.");
                    return false;
                }
                return true;
            }
    
            private void addChooser(
                JPanel panel,
                String label,
                JTextField field,
                int row,
                String extension
            ) {
                addField(panel, label, field, row);
                JButton button = new JButton("...");
                button.addActionListener(e -> {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    configureExtensionFilter(chooser, extension);
                    String current = field.getText().trim();
                    Path directory = null;
                    if (!current.isEmpty()) {
                        Path path = Path.of(current);
                        directory = Files.isDirectory(path) ? path : path.getParent();
                    }
                    String key = chooserKey(extension, JFileChooser.FILES_ONLY);
                    if ((directory == null || !Files.exists(directory)) && key != null) {
                        String remembered = properties.getProperty(key, "").trim();
                        if (!remembered.isEmpty() && Files.isDirectory(Path.of(remembered))) directory = Path.of(remembered);
                    }
                    if (directory != null && Files.exists(directory)) chooser.setCurrentDirectory(directory.toFile());
                    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                        Path selected = chooser.getSelectedFile().toPath().toAbsolutePath().normalize();
                        if (!hasExtension(selected, extension)) {
                            warn("Selezionare un file " + extension);
                            return;
                        }
                        field.setText(selected.toString());
                        if (key != null && selected.getParent() != null) {
                            properties.setProperty(key, selected.getParent().toString());
                            if (".fcm".equalsIgnoreCase(extension)) {
                                properties.setProperty("chooser.lastFcaDirectory", selected.getParent().toString());
                            }
                            try {
                                Files.createDirectories(configPath.getParent());
                                try (OutputStream out = Files.newOutputStream(configPath)) {
                                    properties.store(out, "RecordsNext configuration");
                                }
                            } catch (IOException ignored) { }
                        }
                    }
                });
                GridBagConstraints g = new GridBagConstraints();
                g.gridx = 2;
                g.gridy = row;
                g.insets = new Insets(5, 5, 5, 5);
                panel.add(button, g);
            }
    
            private void addField(JPanel panel, String label, JTextField field, int row) {
                GridBagConstraints g = new GridBagConstraints();
                g.insets = new Insets(5, 5, 5, 5);
                g.anchor = GridBagConstraints.WEST;
                g.gridx = 0;
                g.gridy = row;
                panel.add(new JLabel(label + ":"), g);
                g.gridx = 1;
                g.weightx = 1;
                g.fill = GridBagConstraints.HORIZONTAL;
                panel.add(field, g);
            }
    
            private void addValue(JPanel panel, String label, JLabel value, int row) {
                GridBagConstraints g = new GridBagConstraints();
                g.insets = new Insets(5, 5, 5, 5);
                g.anchor = GridBagConstraints.WEST;
                g.gridx = 0;
                g.gridy = row;
                panel.add(new JLabel(label + ":"), g);
                g.gridx = 1;
                g.gridwidth = 2;
                g.weightx = 1;
                g.fill = GridBagConstraints.HORIZONTAL;
                panel.add(value, g);
            }
    
            private void warn(String message) {
                JOptionPane.showMessageDialog(
                    this,
                    message,
                    "RecordsNext",
                    JOptionPane.WARNING_MESSAGE
                );
            }
        }
    
    }

## src\main\java\it\alterlega\recordsnext\gui\SeasonConfigurationRepository.java

File: src\main\java\it\alterlega\recordsnext\gui\SeasonConfigurationRepository.java

    package it.alterlega.recordsnext.gui;
    
    import java.nio.file.Path;
    import java.sql.*;
    import java.time.Instant;
    import java.util.*;
    
    final class SeasonConfigurationRepository {
        record SeasonRow(String seasonId, int seasonNumber, boolean anchor,
                         String managementType, String status,
                         String fcmPath, String fcaPath,
                         String localSitePath, String onlineSiteUrl) {}
    
        private final Path database;
    
        SeasonConfigurationRepository(Path database) {
            this.database = database.toAbsolutePath().normalize();
        }
    
        List<SeasonRow> load() throws Exception {
            Class.forName("org.sqlite.JDBC");
            try (Connection c = open()) {
                ensureSchema(c);
                String sql = """
                    SELECT s.season_id, COALESCE(s.sort_order,0), s.is_anchor,
                           COALESCE(c.management_type,'GESTITA') management_type,
                           COALESCE(c.configuration_status,'DA_CONFIGURARE') configuration_status,
                           COALESCE(c.configured_fcm_path,
                             MAX(CASE WHEN f.source_type='FCM' THEN f.source_path END),'') fcm_path,
                           COALESCE(c.configured_fca_path,
                             MAX(CASE WHEN f.source_type='FCA' THEN f.source_path END),'') fca_path,
                           COALESCE(c.local_site_path,'') local_site_path,
                           COALESCE(c.online_site_url,'') online_site_url
                    FROM rn_season s
                    LEFT JOIN rn_season_configuration c ON c.season_id=s.season_id
                    LEFT JOIN rn_source_file f ON f.season_id=s.season_id
                    GROUP BY s.season_id,s.sort_order,s.is_anchor,c.management_type,
                             c.configuration_status,c.configured_fcm_path,
                             c.configured_fca_path,c.local_site_path,c.online_site_url
                    ORDER BY CAST(SUBSTR(s.season_id,1,4) AS INTEGER) DESC, s.season_id DESC
                    """;
                List<SeasonRow> out = new ArrayList<>();
                try (Statement st=c.createStatement(); ResultSet r=st.executeQuery(sql)) {
                    while (r.next()) out.add(new SeasonRow(
                        r.getString(1), r.getInt(2), r.getInt(3)==1,
                        r.getString(4), r.getString(5), r.getString(6),
                        r.getString(7), r.getString(8), r.getString(9)));
                }
                assignMissingNumbers(out);
                return out;
            }
        }
    
        int suggestedSeasonNumber(String seasonId, Collection<SeasonRow> current) {
            List<String> ids = new ArrayList<>();
            for (SeasonRow row : current) ids.add(row.seasonId());
            if (!ids.contains(seasonId)) ids.add(seasonId);
            ids.sort(Comparator.comparingInt(SeasonConfigurationRepository::startYear));
            return ids.indexOf(seasonId) + 1;
        }
    
        void save(List<SeasonRow> rows) throws Exception {
            Class.forName("org.sqlite.JDBC");
            try (Connection c=open()) {
                ensureSchema(c); c.setAutoCommit(false);
                try {
                    String anchorSeason = rows.stream()
                        .filter(r -> "GESTITA".equals(r.managementType()))
                        .max(Comparator.comparingInt(r -> startYear(r.seasonId())))
                        .map(SeasonRow::seasonId).orElse(null);
                    String now= Instant.now().toString();
                    try (Statement st=c.createStatement()) { st.executeUpdate("UPDATE rn_season SET is_anchor=0"); }
                    for (SeasonRow row: rows) {
                        boolean anchor = Objects.equals(row.seasonId(), anchorSeason);
                        try (PreparedStatement p=c.prepareStatement("""
                            INSERT INTO rn_season(season_id,display_name,sort_order,is_anchor,created_at,updated_at)
                            VALUES(?,?,?,?,?,?)
                            ON CONFLICT(season_id) DO UPDATE SET display_name=excluded.display_name,
                              sort_order=excluded.sort_order,is_anchor=excluded.is_anchor,
                              updated_at=excluded.updated_at
                            """)) {
                            p.setString(1,row.seasonId()); p.setString(2,row.seasonId());
                            p.setInt(3,row.seasonNumber()); p.setInt(4,anchor?1:0);
                            p.setString(5,now); p.setString(6,now); p.executeUpdate();
                        }
                        try (PreparedStatement p=c.prepareStatement("""
                            INSERT INTO rn_season_configuration(
                              season_id,management_type,local_site_path,online_site_url,dataa_path,
                              configuration_status,created_at,updated_at,configured_fcm_path,configured_fca_path)
                            VALUES(?,?,?,?,NULL,?,?,?,?,?)
                            ON CONFLICT(season_id) DO UPDATE SET management_type=excluded.management_type,
                              local_site_path=excluded.local_site_path,online_site_url=excluded.online_site_url,
                              configuration_status=excluded.configuration_status,updated_at=excluded.updated_at,
                              configured_fcm_path=excluded.configured_fcm_path,
                              configured_fca_path=excluded.configured_fca_path
                            """)) {
                            p.setString(1,row.seasonId()); p.setString(2,row.managementType());
                            nullable(p,3,row.localSitePath()); nullable(p,4,row.onlineSiteUrl());
                            p.setString(5,status(row)); p.setString(6,now); p.setString(7,now);
                            nullable(p,8,row.fcmPath()); nullable(p,9,row.fcaPath()); p.executeUpdate();
                        }
                    }
                    c.commit();
                } catch(Exception ex) { c.rollback(); throw ex; }
            }
        }
    
        void removeConfiguration(String seasonId) throws Exception {
            try (Connection c=open()) {
                ensureSchema(c);
                try (PreparedStatement p=c.prepareStatement("DELETE FROM rn_season_configuration WHERE season_id=?")) {
                    p.setString(1,seasonId); p.executeUpdate();
                }
                try (PreparedStatement p=c.prepareStatement("""
                    DELETE FROM rn_season WHERE season_id=?
                      AND NOT EXISTS(SELECT 1 FROM rn_source_file WHERE season_id=?)
                    """)) {
                    p.setString(1,seasonId); p.setString(2,seasonId); p.executeUpdate();
                }
            }
        }
    
        private Connection open() throws Exception {
            Connection c=DriverManager.getConnection("jdbc:sqlite:"+database);
            try(Statement s=c.createStatement()) { s.execute("PRAGMA foreign_keys=ON"); s.execute("PRAGMA busy_timeout=10000"); }
            return c;
        }
    
        private static void ensureSchema(Connection c) throws Exception {
            try(Statement s=c.createStatement()) {
                s.execute("""
                    CREATE TABLE IF NOT EXISTS rn_season_configuration(
                      season_id TEXT PRIMARY KEY, management_type TEXT NOT NULL,
                      local_site_path TEXT, online_site_url TEXT, dataa_path TEXT,
                      configuration_status TEXT NOT NULL DEFAULT 'DA_CONFIGURARE',
                      created_at TEXT NOT NULL, updated_at TEXT NOT NULL,
                      configured_fcm_path TEXT, configured_fca_path TEXT,
                      FOREIGN KEY(season_id) REFERENCES rn_season(season_id))
                    """);
            }
            addColumnIfMissing(c,"configured_fcm_path","TEXT");
            addColumnIfMissing(c,"configured_fca_path","TEXT");
        }
    
        private static void addColumnIfMissing(Connection c,String name,String type) throws Exception {
            boolean found=false;
            try(Statement s=c.createStatement(); ResultSet r=s.executeQuery("PRAGMA table_info(rn_season_configuration)")) {
                while(r.next()) if(name.equalsIgnoreCase(r.getString("name"))) found=true;
            }
            if(!found) try(Statement s=c.createStatement()) { s.execute("ALTER TABLE rn_season_configuration ADD COLUMN "+name+" "+type); }
        }
    
        private static String status(SeasonRow r) {
            if ("MANUALE".equals(r.managementType())) {
                return r.localSitePath().isBlank() ? "DA_CONFIGURARE" : "COMPLETA";
            }
            return !r.fcmPath().isBlank() && !r.fcaPath().isBlank() && !r.localSitePath().isBlank()
                    ? "COMPLETA" : "DA_CONFIGURARE";
        }
    
        private static void nullable(PreparedStatement p,int i,String value) throws Exception {
            String v=value==null?"":value.trim(); if(v.isEmpty()) p.setNull(i,Types.VARCHAR); else p.setString(i,v);
        }
    
        private static int startYear(String seasonId) {
            try { return Integer.parseInt(seasonId.substring(0,4)); }
            catch (Exception ex) { return Integer.MIN_VALUE; }
        }
    
        private static void assignMissingNumbers(List<SeasonRow> rows) {
            List<SeasonRow> chronological = new ArrayList<>(rows);
            chronological.sort(Comparator.comparingInt(r -> startYear(r.seasonId())));
            Map<String,Integer> numbers = new HashMap<>();
            int next=1;
            for (SeasonRow row : chronological) {
                int n=row.seasonNumber()>0?row.seasonNumber():next;
                numbers.put(row.seasonId(),n); next=Math.max(next,n+1);
            }
            for (int i=0;i<rows.size();i++) {
                SeasonRow r=rows.get(i);
                if (r.seasonNumber()<=0) rows.set(i,new SeasonRow(r.seasonId(),numbers.get(r.seasonId()),r.anchor(),r.managementType(),r.status(),r.fcmPath(),r.fcaPath(),r.localSitePath(),r.onlineSiteUrl()));
            }
        }
    }

## src\main\java\it\alterlega\recordsnext\PlayoffRecordsExporter.java

File: src\main\java\it\alterlega\recordsnext\PlayoffRecordsExporter.java

    package it.alterlega.recordsnext;
    
    import java.io.BufferedWriter;
    import java.math.BigDecimal;
    import java.nio.charset.StandardCharsets;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.sql.Connection;
    import java.sql.DriverManager;
    import java.sql.PreparedStatement;
    import java.sql.ResultSet;
    import java.time.Instant;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.Locale;
    
    public final class PlayoffRecordsExporter {
    
        private PlayoffRecordsExporter() {
        }
    
        public static void main(String[] args) throws Exception {
            if (args.length != 3) {
                System.err.println(
                    "Uso: PlayoffRecordsExporter "
                        + "<recordsnext.db> <stagione> <output.json>"
                );
                System.exit(2);
            }
    
            Path database = Path.of(args[0])
                .toAbsolutePath()
                .normalize();
    
            String seasonId = args[1].trim();
    
            Path output = Path.of(args[2])
                .toAbsolutePath()
                .normalize();
    
            if (seasonId.isBlank()) {
                throw new IllegalArgumentException(
                    "La stagione non può essere vuota."
                );
            }
    
            if (output.getParent() != null) {
                Files.createDirectories(output.getParent());
            }
    
            Class.forName("org.sqlite.JDBC");
    
            long started = System.nanoTime();
    
            try (Connection connection = DriverManager.getConnection(
                    "jdbc:sqlite:" + database)) {
    
                ensureViewExists(connection);
    
                List<TeamSummary> summaries = readSummaries(
                    connection,
                    seasonId
                );
    
                List<PlayoffDetail> wins = readDetails(
                    connection,
                    seasonId,
                    "W"
                );
    
                List<PlayoffDetail> losses = readDetails(
                    connection,
                    seasonId,
                    "L"
                );
    
                writeJson(
                    output,
                    new ExportData(
                        new Meta(
                            Instant.now().toString(),
                            seasonId,
                            summaries.size(),
                            wins.size(),
                            losses.size()
                        ),
                        summaries,
                        wins,
                        losses
                    )
                );
    
                long finished = System.nanoTime();
    
                System.out.println("Record play off / play out esportati");
                System.out.println("Stagione       : " + seasonId);
                System.out.println("Squadre        : " + summaries.size());
                System.out.println("Play off vinti : " + wins.size());
                System.out.println("Play off persi : " + losses.size());
                System.out.println("Output         : " + output);
    
                System.out.printf(
                    Locale.ROOT,
                    "Tempo          : %.3f ms%n",
                    (finished - started) / 1_000_000.0
                );
            }
        }
    
        private static void ensureViewExists(
                Connection connection) throws Exception {
    
            String sql = """
                SELECT COUNT(*)
                FROM sqlite_master
                WHERE type = 'view'
                  AND name = 'rn_playoff_result'
                """;
    
            try (
                PreparedStatement statement =
                    connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()
            ) {
                result.next();
    
                if (result.getInt(1) != 1) {
                    throw new IllegalStateException(
                        "Vista rn_playoff_result non trovata. "
                            + "Eseguire prima CanonicalViews."
                    );
                }
            }
        }
    
        private static List<TeamSummary> readSummaries(
                Connection connection,
                String seasonId) throws Exception {
    
            String sql = """
                SELECT
                    source_team_id,
                    team_identity_id,
                    team_name,
                    SUM(CASE WHEN result = 'W' THEN 1 ELSE 0 END)
                        AS playoff_wins,
                    SUM(CASE WHEN result = 'L' THEN 1 ELSE 0 END)
                        AS playoff_losses
                FROM rn_playoff_result
                WHERE season_id = ?
                GROUP BY
                    source_team_id,
                    team_identity_id,
                    team_name
                ORDER BY
                    playoff_wins DESC,
                    playoff_losses ASC,
                    team_name COLLATE NOCASE
                """;
    
            List<TeamSummary> rows = new ArrayList<>();
    
            try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {
    
                statement.setString(1, seasonId);
    
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        rows.add(
                            new TeamSummary(
                                result.getInt("source_team_id"),
                                result.getLong("team_identity_id"),
                                result.getString("team_name"),
                                result.getInt("playoff_wins"),
                                result.getInt("playoff_losses")
                            )
                        );
                    }
                }
            }
    
            return rows;
        }
    
        private static List<PlayoffDetail> readDetails(
                Connection connection,
                String seasonId,
                String resultCode) throws Exception {
    
            String sql = """
                SELECT
                    season_id,
                    competition_name,
                    source_competition_id,
                    source_group_id,
                    source_group_name,
                    source_round_id,
                    round_description,
                    serie_a_round,
                    source_event_id,
                    source_team_id,
                    team_identity_id,
                    team_name,
                    opponent_source_event_id,
                    opponent_source_team_id,
                    opponent_team_identity_id,
                    opponent_name,
                    score_for,
                    score_against,
                    result
                FROM rn_playoff_result
                WHERE season_id = ?
                  AND result = ?
                ORDER BY
                    serie_a_round,
                    source_group_id,
                    source_round_id,
                    source_event_id
                """;
    
            List<PlayoffDetail> rows = new ArrayList<>();
    
            try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {
    
                statement.setString(1, seasonId);
                statement.setString(2, resultCode);
    
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        rows.add(
                            new PlayoffDetail(
                                result.getString("season_id"),
                                result.getString("competition_name"),
                                result.getInt("source_competition_id"),
                                result.getInt("source_group_id"),
                                result.getString("source_group_name"),
                                result.getInt("source_round_id"),
                                result.getString("round_description"),
                                result.getInt("serie_a_round"),
                                result.getLong("source_event_id"),
                                result.getInt("source_team_id"),
                                result.getLong("team_identity_id"),
                                result.getString("team_name"),
                                result.getLong("opponent_source_event_id"),
                                result.getInt("opponent_source_team_id"),
                                result.getLong("opponent_team_identity_id"),
                                result.getString("opponent_name"),
                                result.getBigDecimal("score_for"),
                                result.getBigDecimal("score_against"),
                                result.getString("result")
                            )
                        );
                    }
                }
            }
    
            return rows;
        }
    
        private static void writeJson(
                Path output,
                ExportData data) throws Exception {
    
            try (BufferedWriter writer = Files.newBufferedWriter(
                    output,
                    StandardCharsets.UTF_8)) {
    
                writer.write("{\n");
    
                writeMeta(writer, data.meta());
                writer.write(",\n");
    
                writeSummaries(writer, data.summaries());
                writer.write(",\n");
    
                writeDetails(
                    writer,
                    "playOffVinti",
                    data.wins()
                );
                writer.write(",\n");
    
                writeDetails(
                    writer,
                    "playOffPersi",
                    data.losses()
                );
    
                writer.write("\n}\n");
            }
        }
    
        private static void writeMeta(
                BufferedWriter writer,
                Meta meta) throws Exception {
    
            writer.write("  \"meta\": {\n");
            writeStringProperty(
                writer,
                "generatedAt",
                meta.generatedAt(),
                true,
                4
            );
            writeStringProperty(
                writer,
                "stagione",
                meta.seasonId(),
                true,
                4
            );
            writeNumberProperty(
                writer,
                "squadreCoinvolte",
                meta.teams(),
                true,
                4
            );
            writeNumberProperty(
                writer,
                "playOffVinti",
                meta.wins(),
                true,
                4
            );
            writeNumberProperty(
                writer,
                "playOffPersi",
                meta.losses(),
                false,
                4
            );
            writer.write("  }");
        }
    
        private static void writeSummaries(
                BufferedWriter writer,
                List<TeamSummary> rows) throws Exception {
    
            writer.write("  \"riepilogoSquadre\": [\n");
    
            for (int index = 0; index < rows.size(); index++) {
                TeamSummary row = rows.get(index);
    
                writer.write("    {\n");
                writeStringProperty(
                    writer,
                    "idSquadra",
                    Integer.toString(row.sourceTeamId()),
                    true,
                    6
                );
                writeStringProperty(
                    writer,
                    "idIdentitaSquadra",
                    Long.toString(row.teamIdentityId()),
                    true,
                    6
                );
                writeStringProperty(
                    writer,
                    "squadra",
                    row.teamName(),
                    true,
                    6
                );
                writeNumberProperty(
                    writer,
                    "playOffVinti",
                    row.wins(),
                    true,
                    6
                );
                writeNumberProperty(
                    writer,
                    "playOffPersi",
                    row.losses(),
                    false,
                    6
                );
                writer.write("    }");
    
                if (index + 1 < rows.size()) {
                    writer.write(",");
                }
    
                writer.write("\n");
            }
    
            writer.write("  ]");
        }
    
        private static void writeDetails(
                BufferedWriter writer,
                String propertyName,
                List<PlayoffDetail> rows) throws Exception {
    
            writer.write("  \"");
            writer.write(jsonEscape(propertyName));
            writer.write("\": [\n");
    
            for (int index = 0; index < rows.size(); index++) {
                PlayoffDetail row = rows.get(index);
    
                writer.write("    {\n");
    
                writeStringProperty(
                    writer,
                    "stagione",
                    row.seasonId(),
                    true,
                    6
                );
                writeStringProperty(
                    writer,
                    "competizione",
                    row.competitionName(),
                    true,
                    6
                );
                writeStringProperty(
                    writer,
                    "idCompetizioneFcm",
                    Integer.toString(row.sourceCompetitionId()),
                    true,
                    6
                );
                writeStringProperty(
                    writer,
                    "idGirone",
                    Integer.toString(row.sourceGroupId()),
                    true,
                    6
                );
                writeStringProperty(
                    writer,
                    "girone",
                    row.sourceGroupName(),
                    true,
                    6
                );
                writeStringProperty(
                    writer,
                    "idGiornata",
                    Integer.toString(row.sourceRoundId()),
                    true,
                    6
                );
                writeStringProperty(
                    writer,
                    "giornata",
                    row.roundDescription(),
                    true,
                    6
                );
                writeNumberProperty(
                    writer,
                    "giornataDiA",
                    row.serieARound(),
                    true,
                    6
                );
                writeStringProperty(
                    writer,
                    "idEvento",
                    Long.toString(row.sourceEventId()),
                    true,
                    6
                );
                writeStringProperty(
                    writer,
                    "idSquadra",
                    Integer.toString(row.sourceTeamId()),
                    true,
                    6
                );
                writeStringProperty(
                    writer,
                    "idIdentitaSquadra",
                    Long.toString(row.teamIdentityId()),
                    true,
                    6
                );
                writeStringProperty(
                    writer,
                    "squadra",
                    row.teamName(),
                    true,
                    6
                );
                writeStringProperty(
                    writer,
                    "idEventoAvversaria",
                    Long.toString(row.opponentSourceEventId()),
                    true,
                    6
                );
                writeStringProperty(
                    writer,
                    "idAvversaria",
                    Integer.toString(row.opponentSourceTeamId()),
                    true,
                    6
                );
                writeStringProperty(
                    writer,
                    "idIdentitaAvversaria",
                    Long.toString(row.opponentTeamIdentityId()),
                    true,
                    6
                );
                writeStringProperty(
                    writer,
                    "avversaria",
                    row.opponentName(),
                    true,
                    6
                );
                writeDecimalProperty(
                    writer,
                    "puntiFatti",
                    row.scoreFor(),
                    true,
                    6
                );
                writeDecimalProperty(
                    writer,
                    "puntiSubiti",
                    row.scoreAgainst(),
                    true,
                    6
                );
                writeStringProperty(
                    writer,
                    "esito",
                    row.result(),
                    false,
                    6
                );
    
                writer.write("    }");
    
                if (index + 1 < rows.size()) {
                    writer.write(",");
                }
    
                writer.write("\n");
            }
    
            writer.write("  ]");
        }
    
        private static void writeStringProperty(
                BufferedWriter writer,
                String name,
                String value,
                boolean comma,
                int indent) throws Exception {
    
            writer.write(" ".repeat(indent));
            writer.write("\"");
            writer.write(jsonEscape(name));
            writer.write("\": ");
    
            if (value == null) {
                writer.write("null");
            } else {
                writer.write("\"");
                writer.write(jsonEscape(value));
                writer.write("\"");
            }
    
            if (comma) {
                writer.write(",");
            }
    
            writer.write("\n");
        }
    
        private static void writeNumberProperty(
                BufferedWriter writer,
                String name,
                long value,
                boolean comma,
                int indent) throws Exception {
    
            writer.write(" ".repeat(indent));
            writer.write("\"");
            writer.write(jsonEscape(name));
            writer.write("\": ");
            writer.write(Long.toString(value));
    
            if (comma) {
                writer.write(",");
            }
    
            writer.write("\n");
        }
    
        private static void writeDecimalProperty(
                BufferedWriter writer,
                String name,
                BigDecimal value,
                boolean comma,
                int indent) throws Exception {
    
            writer.write(" ".repeat(indent));
            writer.write("\"");
            writer.write(jsonEscape(name));
            writer.write("\": ");
    
            if (value == null) {
                writer.write("null");
            } else {
                writer.write(
                    value.stripTrailingZeros().toPlainString()
                );
            }
    
            if (comma) {
                writer.write(",");
            }
    
            writer.write("\n");
        }
    
        private static String jsonEscape(String value) {
            StringBuilder escaped = new StringBuilder();
    
            for (int index = 0; index < value.length(); index++) {
                char current = value.charAt(index);
    
                switch (current) {
                    case '"' -> escaped.append("\\\"");
                    case '\\' -> escaped.append("\\\\");
                    case '\b' -> escaped.append("\\b");
                    case '\f' -> escaped.append("\\f");
                    case '\n' -> escaped.append("\\n");
                    case '\r' -> escaped.append("\\r");
                    case '\t' -> escaped.append("\\t");
    
                    default -> {
                        if (current < 0x20) {
                            escaped.append(
                                String.format(
                                    Locale.ROOT,
                                    "\\u%04x",
                                    (int) current
                                )
                            );
                        } else {
                            escaped.append(current);
                        }
                    }
                }
            }
    
            return escaped.toString();
        }
    
        private record Meta(
            String generatedAt,
            String seasonId,
            int teams,
            int wins,
            int losses
        ) {
        }
    
        private record TeamSummary(
            int sourceTeamId,
            long teamIdentityId,
            String teamName,
            int wins,
            int losses
        ) {
        }
    
        private record PlayoffDetail(
            String seasonId,
            String competitionName,
            int sourceCompetitionId,
            int sourceGroupId,
            String sourceGroupName,
            int sourceRoundId,
            String roundDescription,
            int serieARound,
            long sourceEventId,
            int sourceTeamId,
            long teamIdentityId,
            String teamName,
            long opponentSourceEventId,
            int opponentSourceTeamId,
            long opponentTeamIdentityId,
            String opponentName,
            BigDecimal scoreFor,
            BigDecimal scoreAgainst,
            String result
        ) {
        }
    
        private record ExportData(
            Meta meta,
            List<TeamSummary> summaries,
            List<PlayoffDetail> wins,
            List<PlayoffDetail> losses
        ) {
        }
    }

## src\main\java\it\alterlega\recordsnext\RawSqliteImporter.java

File: src\main\java\it\alterlega\recordsnext\RawSqliteImporter.java

    package it.alterlega.recordsnext;
    
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.security.MessageDigest;
    import java.sql.Connection;
    import java.sql.DatabaseMetaData;
    import java.sql.DriverManager;
    import java.sql.PreparedStatement;
    import java.sql.ResultSet;
    import java.sql.ResultSetMetaData;
    import java.sql.Statement;
    import java.sql.Types;
    import java.time.Instant;
    import java.util.ArrayList;
    import java.util.HexFormat;
    import java.util.List;
    import java.util.Locale;
    
    public final class RawSqliteImporter {
    
        private static final int BATCH_SIZE = 1000;
    
        private RawSqliteImporter() {
        }
    
        public static void main(String[] args) throws Exception {
            if (args.length != 4) {
                System.err.println(
                    "Uso: RawSqliteImporter "
                        + "<file.fcm|file.fca> <FCM|FCA> <stagione> <output.db>"
                );
                System.exit(2);
            }
    
            Path source = Path.of(args[0]).toAbsolutePath().normalize();
            String sourceType = args[1].trim().toUpperCase(Locale.ROOT);
            String seasonId = args[2].trim();
            Path sqliteFile = Path.of(args[3]).toAbsolutePath().normalize();
    
            if (!Files.isRegularFile(source)) {
                throw new IllegalArgumentException(
                    "File sorgente non trovato: " + source
                );
            }
    
            if (!sourceType.equals("FCM") && !sourceType.equals("FCA")) {
                throw new IllegalArgumentException(
                    "Tipo sorgente non valido: " + sourceType
                );
            }
    
            if (seasonId.isBlank()) {
                throw new IllegalArgumentException("Stagione non specificata.");
            }
    
            if (sqliteFile.getParent() != null) {
                Files.createDirectories(sqliteFile.getParent());
            }
    
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            Class.forName("org.sqlite.JDBC");
    
            String accessUrl = "jdbc:ucanaccess://" + source;
            String sqliteUrl = "jdbc:sqlite:" + sqliteFile;
    
            long totalStarted = System.nanoTime();
    
            try (
                Connection access = DriverManager.getConnection(accessUrl);
                Connection sqlite = DriverManager.getConnection(sqliteUrl)
            ) {
    access.setReadOnly(true);
    
    configureSqlite(sqlite);
    sqlite.setAutoCommit(false);
    
    createMetadataTables(sqlite);
    
                long importId = registerImport(
                    sqlite,
                    source,
                    sourceType,
                    seasonId
                );
    
                DatabaseMetaData metadata = access.getMetaData();
                List<String> tableNames = readTableNames(metadata);
    
                long importedRows = 0;
                long importedColumns = 0;
    
                for (String tableName : tableNames) {
                    TableImportResult result = importTable(
                        access,
                        sqlite,
                        metadata,
                        importId,
                        sourceType,
                        seasonId,
                        tableName
                    );
    
                    importedRows += result.rows();
                    importedColumns += result.columns();
    
                    System.out.printf(
                        Locale.ROOT,
                        "%-40s colonne=%4d righe=%8d%n",
                        tableName,
                        result.columns(),
                        result.rows()
                    );
                }
    
                finishImport(
                    sqlite,
                    importId,
                    tableNames.size(),
                    importedColumns,
                    importedRows
                );
    
                sqlite.commit();
    
                long totalFinished = System.nanoTime();
    
                System.out.println();
                System.out.println("Importazione raw completata");
                System.out.println("Sorgente : " + source);
                System.out.println("Tipo     : " + sourceType);
                System.out.println("Stagione : " + seasonId);
                System.out.println("SQLite   : " + sqliteFile);
                System.out.println("Tabelle  : " + tableNames.size());
                System.out.println("Colonne  : " + importedColumns);
                System.out.println("Righe    : " + importedRows);
                System.out.printf(
                    Locale.ROOT,
                    "Totale   : %.3f s%n",
                    (totalFinished - totalStarted) / 1_000_000_000.0
                );
            }
        }
    
        private static void configureSqlite(Connection sqlite) throws Exception {
            try (Statement statement = sqlite.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");
                statement.execute("PRAGMA journal_mode = WAL");
                statement.execute("PRAGMA synchronous = NORMAL");
                statement.execute("PRAGMA temp_store = MEMORY");
            }
        }
    
        private static void createMetadataTables(Connection sqlite)
                throws Exception {
    
            try (Statement statement = sqlite.createStatement()) {
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS rn_import (
                        import_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        season_id TEXT NOT NULL,
                        source_type TEXT NOT NULL,
                        source_path TEXT NOT NULL,
                        source_file_name TEXT NOT NULL,
                        source_size_bytes INTEGER NOT NULL,
                        source_last_modified TEXT NOT NULL,
                        source_sha256 TEXT NOT NULL,
                        started_at TEXT NOT NULL,
                        completed_at TEXT,
                        table_count INTEGER,
                        column_count INTEGER,
                        row_count INTEGER,
                        status TEXT NOT NULL
                    )
                    """);
    
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS rn_table_catalog (
                        import_id INTEGER NOT NULL,
                        season_id TEXT NOT NULL,
                        source_type TEXT NOT NULL,
                        source_table_name TEXT NOT NULL,
                        raw_table_name TEXT NOT NULL,
                        source_row_count INTEGER NOT NULL,
                        imported_row_count INTEGER NOT NULL,
                        column_count INTEGER NOT NULL,
                        audit_ok INTEGER NOT NULL,
                        PRIMARY KEY (
                            import_id,
                            source_table_name
                        )
                    )
                    """);
    
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS rn_column_catalog (
                        import_id INTEGER NOT NULL,
                        source_table_name TEXT NOT NULL,
                        column_name TEXT NOT NULL,
                        ordinal_position INTEGER NOT NULL,
                        jdbc_type INTEGER NOT NULL,
                        type_name TEXT,
                        column_size INTEGER,
                        decimal_digits INTEGER,
                        nullable_code INTEGER,
                        default_value TEXT,
                        PRIMARY KEY (
                            import_id,
                            source_table_name,
                            column_name
                        )
                    )
                    """);
            }
        }
    
        private static long registerImport(
                Connection sqlite,
                Path source,
                String sourceType,
                String seasonId) throws Exception {
    
            String sql = """
                INSERT INTO rn_import (
                    season_id,
                    source_type,
                    source_path,
                    source_file_name,
                    source_size_bytes,
                    source_last_modified,
                    source_sha256,
                    started_at,
                    status
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
    
            try (
                PreparedStatement statement = sqlite.prepareStatement(
                    sql,
                    Statement.RETURN_GENERATED_KEYS
                )
            ) {
                statement.setString(1, seasonId);
                statement.setString(2, sourceType);
                statement.setString(3, source.toString());
                statement.setString(4, source.getFileName().toString());
                statement.setLong(5, Files.size(source));
                statement.setString(
                    6,
                    Files.getLastModifiedTime(source).toInstant().toString()
                );
                statement.setString(7, sha256(source));
                statement.setString(8, Instant.now().toString());
                statement.setString(9, "RUNNING");
                statement.executeUpdate();
    
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new IllegalStateException(
                            "Impossibile ottenere import_id."
                        );
                    }
    
                    return keys.getLong(1);
                }
            }
        }
    
        private static List<String> readTableNames(DatabaseMetaData metadata)
                throws Exception {
    
            List<String> tables = new ArrayList<>();
    
            try (
                ResultSet result = metadata.getTables(
                    null,
                    null,
                    "%",
                    new String[]{"TABLE"}
                )
            ) {
                while (result.next()) {
                    String name = result.getString("TABLE_NAME");
    
                    if (name != null && !name.isBlank()) {
                        tables.add(name);
                    }
                }
            }
    
            tables.sort(String.CASE_INSENSITIVE_ORDER);
            return tables;
        }
    
        private static TableImportResult importTable(
                Connection access,
                Connection sqlite,
                DatabaseMetaData metadata,
                long importId,
                String sourceType,
                String seasonId,
                String sourceTableName) throws Exception {
    
            String rawTableName = rawTableName(
                sourceType,
                seasonId,
                sourceTableName
            );
    
            List<ColumnDefinition> columns = readColumns(
                metadata,
                sourceTableName
            );
    
            dropRawTable(sqlite, rawTableName);
            createRawTable(sqlite, rawTableName, columns);
            registerColumns(
                sqlite,
                importId,
                sourceTableName,
                columns
            );
    
            long sourceRowCount = countSourceRows(
                access,
                sourceTableName
            );
    
            long importedRowCount = copyRows(
                access,
                sqlite,
                sourceTableName,
                rawTableName,
                columns
            );
    
            registerTable(
                sqlite,
                importId,
                seasonId,
                sourceType,
                sourceTableName,
                rawTableName,
                sourceRowCount,
                importedRowCount,
                columns.size()
            );
    
            if (sourceRowCount != importedRowCount) {
                throw new IllegalStateException(
                    "Audit fallito per " + sourceTableName
                        + ": sorgente=" + sourceRowCount
                        + ", importate=" + importedRowCount
                );
            }
    
            return new TableImportResult(
                columns.size(),
                importedRowCount
            );
        }
    
        private static List<ColumnDefinition> readColumns(
                DatabaseMetaData metadata,
                String tableName) throws Exception {
    
            List<ColumnDefinition> columns = new ArrayList<>();
    
            try (
                ResultSet result = metadata.getColumns(
                    null,
                    null,
                    tableName,
                    "%"
                )
            ) {
                while (result.next()) {
                    columns.add(
                        new ColumnDefinition(
                            result.getString("COLUMN_NAME"),
                            result.getInt("ORDINAL_POSITION"),
                            result.getInt("DATA_TYPE"),
                            result.getString("TYPE_NAME"),
                            result.getInt("COLUMN_SIZE"),
                            nullableInteger(result, "DECIMAL_DIGITS"),
                            result.getInt("NULLABLE"),
                            result.getString("COLUMN_DEF")
                        )
                    );
                }
            }
    
            columns.sort(
                (left, right) ->
                    Integer.compare(
                        left.ordinalPosition(),
                        right.ordinalPosition()
                    )
            );
    
            return columns;
        }
    
        private static void dropRawTable(
                Connection sqlite,
                String rawTableName) throws Exception {
    
            try (Statement statement = sqlite.createStatement()) {
                statement.execute(
                    "DROP TABLE IF EXISTS " + quoteSqlite(rawTableName)
                );
            }
        }
    
        private static void createRawTable(
                Connection sqlite,
                String rawTableName,
                List<ColumnDefinition> columns) throws Exception {
    
            StringBuilder sql = new StringBuilder();
    
            sql.append("CREATE TABLE ");
            sql.append(quoteSqlite(rawTableName));
            sql.append(" (");
    
            for (int index = 0; index < columns.size(); index++) {
                if (index > 0) {
                    sql.append(", ");
                }
    
                ColumnDefinition column = columns.get(index);
    
                sql.append(quoteSqlite(column.name()));
                sql.append(" ");
                sql.append(sqliteType(column.jdbcType()));
            }
    
            sql.append(")");
    
            try (Statement statement = sqlite.createStatement()) {
                statement.execute(sql.toString());
            }
        }
    
        private static long copyRows(
                Connection access,
                Connection sqlite,
                String sourceTableName,
                String rawTableName,
                List<ColumnDefinition> columns) throws Exception {
    
            String sourceSql =
                "SELECT * FROM " + quoteAccess(sourceTableName);
    
            StringBuilder insertSql = new StringBuilder();
    
            insertSql.append("INSERT INTO ");
            insertSql.append(quoteSqlite(rawTableName));
            insertSql.append(" (");
    
            for (int index = 0; index < columns.size(); index++) {
                if (index > 0) {
                    insertSql.append(", ");
                }
    
                insertSql.append(
                    quoteSqlite(columns.get(index).name())
                );
            }
    
            insertSql.append(") VALUES (");
    
            for (int index = 0; index < columns.size(); index++) {
                if (index > 0) {
                    insertSql.append(", ");
                }
    
                insertSql.append("?");
            }
    
            insertSql.append(")");
    
            long importedRows = 0;
            int batchRows = 0;
    
            try (
                Statement sourceStatement = access.createStatement();
                ResultSet sourceRows =
                    sourceStatement.executeQuery(sourceSql);
                PreparedStatement destination =
                    sqlite.prepareStatement(insertSql.toString())
            ) {
                ResultSetMetaData rowMetadata =
                    sourceRows.getMetaData();
    
                while (sourceRows.next()) {
                    for (
                        int columnIndex = 1;
                        columnIndex <= columns.size();
                        columnIndex++
                    ) {
                        setValue(
                            destination,
                            columnIndex,
                            sourceRows,
                            rowMetadata,
                            columnIndex
                        );
                    }
    
                    destination.addBatch();
                    importedRows++;
                    batchRows++;
    
                    if (batchRows >= BATCH_SIZE) {
                        destination.executeBatch();
                        batchRows = 0;
                    }
                }
    
                if (batchRows > 0) {
                    destination.executeBatch();
                }
            }
    
            return importedRows;
        }
    
        private static void setValue(
                PreparedStatement destination,
                int destinationIndex,
                ResultSet source,
                ResultSetMetaData metadata,
                int sourceIndex) throws Exception {
    
            int jdbcType = metadata.getColumnType(sourceIndex);
            Object value = source.getObject(sourceIndex);
    
            if (value == null) {
                destination.setNull(
                    destinationIndex,
                    sqliteNullType(jdbcType)
                );
                return;
            }
    
            switch (jdbcType) {
                case Types.BINARY,
                     Types.VARBINARY,
                     Types.LONGVARBINARY,
                     Types.BLOB ->
                    destination.setBytes(
                        destinationIndex,
                        source.getBytes(sourceIndex)
                    );
    
                case Types.TINYINT,
                     Types.SMALLINT,
                     Types.INTEGER,
                     Types.BIGINT ->
                    destination.setLong(
                        destinationIndex,
                        source.getLong(sourceIndex)
                    );
    
                case Types.FLOAT,
                     Types.REAL,
                     Types.DOUBLE ->
                    destination.setDouble(
                        destinationIndex,
                        source.getDouble(sourceIndex)
                    );
    
                case Types.NUMERIC,
                     Types.DECIMAL ->
                    destination.setBigDecimal(
                        destinationIndex,
                        source.getBigDecimal(sourceIndex)
                    );
    
                case Types.BIT,
                     Types.BOOLEAN ->
                    destination.setInt(
                        destinationIndex,
                        source.getBoolean(sourceIndex) ? 1 : 0
                    );
    
                case Types.DATE,
                     Types.TIME,
                     Types.TIMESTAMP,
                     Types.TIMESTAMP_WITH_TIMEZONE ->
                    destination.setString(
                        destinationIndex,
                        String.valueOf(value)
                    );
    
                default ->
                    destination.setString(
                        destinationIndex,
                        source.getString(sourceIndex)
                    );
            }
        }
    
        private static long countSourceRows(
                Connection access,
                String tableName) throws Exception {
    
            String sql =
                "SELECT COUNT(*) FROM " + quoteAccess(tableName);
    
            try (
                Statement statement = access.createStatement();
                ResultSet result = statement.executeQuery(sql)
            ) {
                result.next();
                return result.getLong(1);
            }
        }
    
        private static void registerColumns(
                Connection sqlite,
                long importId,
                String tableName,
                List<ColumnDefinition> columns) throws Exception {
    
            String sql = """
                INSERT INTO rn_column_catalog (
                    import_id,
                    source_table_name,
                    column_name,
                    ordinal_position,
                    jdbc_type,
                    type_name,
                    column_size,
                    decimal_digits,
                    nullable_code,
                    default_value
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
    
            try (PreparedStatement statement =
                     sqlite.prepareStatement(sql)) {
    
                for (ColumnDefinition column : columns) {
                    statement.setLong(1, importId);
                    statement.setString(2, tableName);
                    statement.setString(3, column.name());
                    statement.setInt(4, column.ordinalPosition());
                    statement.setInt(5, column.jdbcType());
                    statement.setString(6, column.typeName());
                    statement.setInt(7, column.columnSize());
    
                    if (column.decimalDigits() == null) {
                        statement.setNull(8, Types.INTEGER);
                    } else {
                        statement.setInt(
                            8,
                            column.decimalDigits()
                        );
                    }
    
                    statement.setInt(9, column.nullableCode());
                    statement.setString(10, column.defaultValue());
                    statement.addBatch();
                }
    
                statement.executeBatch();
            }
        }
    
        private static void registerTable(
                Connection sqlite,
                long importId,
                String seasonId,
                String sourceType,
                String sourceTableName,
                String rawTableName,
                long sourceRowCount,
                long importedRowCount,
                int columnCount) throws Exception {
    
            String sql = """
                INSERT INTO rn_table_catalog (
                    import_id,
                    season_id,
                    source_type,
                    source_table_name,
                    raw_table_name,
                    source_row_count,
                    imported_row_count,
                    column_count,
                    audit_ok
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
    
            try (PreparedStatement statement =
                     sqlite.prepareStatement(sql)) {
    
                statement.setLong(1, importId);
                statement.setString(2, seasonId);
                statement.setString(3, sourceType);
                statement.setString(4, sourceTableName);
                statement.setString(5, rawTableName);
                statement.setLong(6, sourceRowCount);
                statement.setLong(7, importedRowCount);
                statement.setInt(8, columnCount);
                statement.setInt(
                    9,
                    sourceRowCount == importedRowCount ? 1 : 0
                );
                statement.executeUpdate();
            }
        }
    
        private static void finishImport(
                Connection sqlite,
                long importId,
                int tableCount,
                long columnCount,
                long rowCount) throws Exception {
    
            String sql = """
                UPDATE rn_import
                SET completed_at = ?,
                    table_count = ?,
                    column_count = ?,
                    row_count = ?,
                    status = ?
                WHERE import_id = ?
                """;
    
            try (PreparedStatement statement =
                     sqlite.prepareStatement(sql)) {
    
                statement.setString(1, Instant.now().toString());
                statement.setInt(2, tableCount);
                statement.setLong(3, columnCount);
                statement.setLong(4, rowCount);
                statement.setString(5, "COMPLETED");
                statement.setLong(6, importId);
                statement.executeUpdate();
            }
        }
    
        private static String rawTableName(
                String sourceType,
                String seasonId,
                String sourceTableName) {
    
            return "raw_"
                + normalizeIdentifier(seasonId)
                + "_"
                + sourceType.toLowerCase(Locale.ROOT)
                + "_"
                + normalizeIdentifier(sourceTableName);
        }
    
        private static String normalizeIdentifier(String value) {
            String normalized = value
                .trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    
            if (normalized.isBlank()) {
                throw new IllegalArgumentException(
                    "Identificatore non normalizzabile: " + value
                );
            }
    
            return normalized;
        }
    
        private static String quoteAccess(String name) {
            return "[" + name.replace("]", "]]") + "]";
        }
    
        private static String quoteSqlite(String name) {
            return "\"" + name.replace("\"", "\"\"") + "\"";
        }
    
        private static String sqliteType(int jdbcType) {
            return switch (jdbcType) {
                case Types.BIT,
                     Types.BOOLEAN,
                     Types.TINYINT,
                     Types.SMALLINT,
                     Types.INTEGER,
                     Types.BIGINT -> "INTEGER";
    
                case Types.FLOAT,
                     Types.REAL,
                     Types.DOUBLE -> "REAL";
    
                case Types.NUMERIC,
                     Types.DECIMAL -> "NUMERIC";
    
                case Types.BINARY,
                     Types.VARBINARY,
                     Types.LONGVARBINARY,
                     Types.BLOB -> "BLOB";
    
                default -> "TEXT";
            };
        }
    
        private static int sqliteNullType(int jdbcType) {
            return switch (sqliteType(jdbcType)) {
                case "INTEGER" -> Types.INTEGER;
                case "REAL" -> Types.REAL;
                case "NUMERIC" -> Types.NUMERIC;
                case "BLOB" -> Types.BLOB;
                default -> Types.VARCHAR;
            };
        }
    
        private static Integer nullableInteger(
                ResultSet result,
                String columnName) throws Exception {
    
            int value = result.getInt(columnName);
            return result.wasNull() ? null : value;
        }
    
        private static String sha256(Path path) throws Exception {
            MessageDigest digest =
                MessageDigest.getInstance("SHA-256");
    
            try (var input = Files.newInputStream(path)) {
                byte[] buffer = new byte[1024 * 1024];
                int read;
    
                while ((read = input.read(buffer)) >= 0) {
                    digest.update(buffer, 0, read);
                }
            }
    
            return HexFormat.of().formatHex(digest.digest());
        }
    
        private record ColumnDefinition(
            String name,
            int ordinalPosition,
            int jdbcType,
            String typeName,
            int columnSize,
            Integer decimalDigits,
            int nullableCode,
            String defaultValue
        ) {
        }
    
        private record TableImportResult(
            int columns,
            long rows
        ) {
        }
    }

## src\main\java\it\alterlega\recordsnext\Records2026ClassicJsExporter.java

File: src\main\java\it\alterlega\recordsnext\Records2026ClassicJsExporter.java

    package it.alterlega.recordsnext;
    
    import java.io.IOException;
    import java.math.BigDecimal;
    import java.nio.charset.StandardCharsets;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.nio.file.StandardOpenOption;
    import java.util.ArrayList;
    import java.util.Comparator;
    import java.util.LinkedHashMap;
    import java.util.LinkedHashSet;
    import java.util.List;
    import java.util.Locale;
    import java.util.Map;
    import java.util.Set;
    import java.util.stream.Stream;
    
    /**
     * Genera records2026.recordstagionali.classic.js mantenendo il contratto
     * pubblico di Records2026 e pubblicando soltanto le sezioni/campi previsti.
     */
    public final class Records2026ClassicJsExporter {
    
        private static final String PREFIX = "season_records_";
        private static final String SUFFIX = ".json";
    
        private static final Map<String, Set<String>> PUBLIC_FIELDS = buildPublicFields();
    
        private Records2026ClassicJsExporter() {
        }
    
        public static void main(String[] args) throws Exception {
            if (args.length < 2) {
                printUsage();
                System.exit(2);
            }
    
            Path archiveRoot = Path.of(args[0]).toAbsolutePath().normalize();
            Path outputFile = Path.of(args[1]).toAbsolutePath().normalize();
            List<String> requestedSeasons = new ArrayList<>();
            for (int i = 2; i < args.length; i++) {
                String value = args[i].trim();
                if (!value.isEmpty()) {
                    requestedSeasons.add(value);
                }
            }
    
            ExportResult result = export(archiveRoot, outputFile, requestedSeasons);
            System.out.println("Archivio : " + archiveRoot);
            System.out.println("Output   : " + outputFile);
            System.out.println("Stagioni : " + result.seasonCount());
            System.out.println("Recordset: " + result.entryCount());
        }
    
        public static ExportResult export(Path archiveRoot, Path outputFile, List<String> requestedSeasons)
                throws IOException {
            if (!Files.isDirectory(archiveRoot)) {
                throw new IOException("Archivio stagioni non trovato: " + archiveRoot);
            }
    
            List<Path> seasonDirectories = resolveSeasonDirectories(archiveRoot, requestedSeasons);
            if (seasonDirectories.isEmpty()) {
                throw new IOException("Nessuna stagione trovata in: " + archiveRoot);
            }
    
            List<Entry> entries = new ArrayList<>();
            int seasonsWithRecords = 0;
    
            for (Path seasonDirectory : seasonDirectories) {
                List<Path> recordFiles = listRecordFiles(seasonDirectory);
                if (recordFiles.isEmpty()) {
                    continue;
                }
                seasonsWithRecords++;
                String season = seasonDirectory.getFileName().toString();
    
                for (Path recordFile : recordFiles) {
                    String fileName = recordFile.getFileName().toString();
                    String competitionId = fileName.substring(PREFIX.length(), fileName.length() - SUFFIX.length());
                    String sourceText = normalizeJsonText(Files.readString(recordFile, StandardCharsets.UTF_8));
                    Object parsed = new JsonParser(sourceText, recordFile).parse();
                    Map<String, Object> root = requireObject(parsed, recordFile, "radice");
                    Map<String, Object> sourceRecords = requireObject(root.get("records"), recordFile, "records");
                    Map<String, Object> publicRecords = projectRecords(sourceRecords, recordFile);
    
                    Map<String, Object> publicData = new LinkedHashMap<>();
                    publicData.put("records", publicRecords);
                    String json = escapeScriptTerminator(JsonWriter.write(publicData));
                    entries.add(new Entry(season, competitionId, fileName, json));
                }
            }
    
            if (entries.isEmpty()) {
                throw new IOException("Nessun file season_records_*.json trovato in: " + archiveRoot);
            }
    
            Path parent = outputFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(outputFile, buildJavascript(entries), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            return new ExportResult(seasonsWithRecords, entries.size());
        }
    
        private static Map<String, Object> projectRecords(Map<String, Object> sourceRecords, Path source)
                throws IOException {
            Map<String, Object> result = new LinkedHashMap<>();
            for (Map.Entry<String, Object> sectionEntry : sourceRecords.entrySet()) {
                String section = sectionEntry.getKey();
                Set<String> allowedFields = PUBLIC_FIELDS.get(section);
                if (allowedFields == null) {
                    continue;
                }
    
                Object value = sectionEntry.getValue();
                if (!(value instanceof List<?> sourceRows)) {
                    continue;
                }
                if (sourceRows.isEmpty()) {
                    continue;
                }
    
                List<Object> publicRows = new ArrayList<>(sourceRows.size());
                for (Object row : sourceRows) {
                    if (!(row instanceof Map<?, ?> rawMap)) {
                        throw new IOException("Riga non oggetto nella sezione '" + section + "': " + source);
                    }
                    Map<String, Object> projected = new LinkedHashMap<>();
                    for (Map.Entry<?, ?> fieldEntry : rawMap.entrySet()) {
                        String fieldName = String.valueOf(fieldEntry.getKey());
                        if (allowedFields.contains(fieldName)) {
                            projected.put(fieldName, fieldEntry.getValue());
                        }
                    }
    
                    // Nei JSON sorgente il dettaglio completo e' spesso conservato
                    // nell'array "dettagli". Il file pubblico espone soltanto il
                    // relativo conteggio, calcolato dal generatore legacy.
                    if (allowedFields.contains("dettagliCount") && !projected.containsKey("dettagliCount")) {
                        Object details = rawMap.get("dettagli");
                        if (details instanceof List<?> detailRows) {
                            projected.put("dettagliCount", detailRows.size());
                        }
                    }
                    publicRows.add(projected);
                }
                result.put(section, publicRows);
            }
            return result;
        }
    
        private static Map<String, Set<String>> buildPublicFields() {
            Map<String, Set<String>> fields = new LinkedHashMap<>();
            fields.put("puntiSquadraMax", orderedSet(
                    "recordId", "nome", "stagione", "competizioneStoricaId", "competizioneNome", "valore",
                    "squadra", "avversaria", "idIncontro", "giornata", "giornataDiA", "urlTabellino",
                    "risultato", "punteggio"));
            fields.put("serieSenzaSconfitte", orderedSet(
                    "recordId", "nome", "stagione", "competizioneStoricaId", "competizioneNome", "valore",
                    "squadra", "idSquadra", "daGiornata", "aGiornata", "daGiornataDiA", "aGiornataDiA",
                    "vittorie", "pareggi", "dettagliCount"));
            fields.put("espulsioniSquadre", compactTeamFields());
            fields.put("espulsioniGiocatori", orderedSet(
                    "recordId", "nome", "valore", "idGiocatore", "giocatore", "dettagliCount"));
            fields.put("ammonizioniSquadre", compactTeamFields());
            fields.put("assistSquadre", compactTeamFields());
            fields.put("autogolSquadre", compactTeamFields());
            fields.put("rigoriSbagliatiSquadre", compactTeamFields());
            fields.put("rigoriParatiSquadre", compactTeamFields());
            fields.put("golRigoreSquadre", compactTeamFields());
            fields.put("modDifesaMax", orderedSet(
                    "recordId", "nome", "valore", "idSquadra", "squadra", "avversaria", "idIncontro", "giornataDiA"));
            fields.put("modDifesaTotaleSquadre", compactTeamFields());
            fields.put("capitanoVolteSquadre", compactTeamFields());
            fields.put("capitanoTotaleSquadre", compactTeamFields());
            fields.put("capitanoSerieSquadre", orderedSet(
                    "recordId", "nome", "valore", "idSquadra", "squadra", "daGiornataDiA", "aGiornataDiA", "dettagliCount"));
            fields.put("cleanSheetPortiereVolteSquadre", compactTeamFields());
            fields.put("cleanSheetPortiereTotaleSquadre", compactTeamFields());
            fields.put("cleanSheetPortiereSerieSquadre", orderedSet(
                    "recordId", "nome", "valore", "idSquadra", "squadra", "daGiornataDiA", "aGiornataDiA", "dettagliCount"));
            return fields;
        }
    
        private static Set<String> compactTeamFields() {
            return orderedSet("recordId", "nome", "valore", "idSquadra", "squadra", "dettagliCount");
        }
    
        private static Set<String> orderedSet(String... values) {
            Set<String> result = new LinkedHashSet<>();
            for (String value : values) {
                result.add(value);
            }
            return Set.copyOf(result);
        }
    
        private static Map<String, Object> requireObject(Object value, Path source, String label) throws IOException {
            if (!(value instanceof Map<?, ?> raw)) {
                throw new IOException("Oggetto JSON '" + label + "' mancante o non valido: " + source);
            }
            Map<String, Object> result = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : raw.entrySet()) {
                result.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            return result;
        }
    
        private static List<Path> resolveSeasonDirectories(Path archiveRoot, List<String> requestedSeasons)
                throws IOException {
            List<Path> result = new ArrayList<>();
            if (requestedSeasons == null || requestedSeasons.isEmpty()) {
                try (Stream<Path> stream = Files.list(archiveRoot)) {
                    stream.filter(Files::isDirectory)
                            .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                            .forEach(result::add);
                }
                return result;
            }
            requestedSeasons.stream().distinct().sorted().map(archiveRoot::resolve)
                    .filter(Files::isDirectory).forEach(result::add);
            return result;
        }
    
        private static List<Path> listRecordFiles(Path seasonDirectory) throws IOException {
            try (Stream<Path> stream = Files.list(seasonDirectory)) {
                return stream.filter(Files::isRegularFile)
                        .filter(path -> {
                            String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
                            return name.startsWith(PREFIX) && name.endsWith(SUFFIX);
                        })
                        .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                        .toList();
            }
        }
    
        private static String normalizeJsonText(String text) {
            if (text == null || text.isEmpty()) {
                return "";
            }
            String normalized = text;
            if (normalized.charAt(0) == '\uFEFF') {
                normalized = normalized.substring(1);
            }
            return normalized.trim();
        }
    
        private static String buildJavascript(List<Entry> entries) {
            StringBuilder output = new StringBuilder();
            output.append("window.RECORDS2026_PREVIEW_CLASSIC = [");
            for (int i = 0; i < entries.size(); i++) {
                if (i > 0) {
                    output.append(',');
                }
                Entry entry = entries.get(i);
                output.append("{\"stagione\":\"").append(JsonWriter.escape(entry.season()))
                        .append("\",\"id\":\"").append(JsonWriter.escape(entry.competitionId()))
                        .append("\",\"file\":\"").append(JsonWriter.escape(entry.fileName()))
                        .append("\",\"data\":").append(entry.json()).append('}');
            }
            output.append("];\n");
            return output.toString();
        }
    
        private static String escapeScriptTerminator(String json) {
            return json.replace("</script>", "<\\/script>");
        }
    
        private static void printUsage() {
            System.err.println("Uso:");
            System.err.println("  Records2026ClassicJsExporter <archiveRoot> <outputFile> [stagione ...]");
        }
    
        private record Entry(String season, String competitionId, String fileName, String json) {
        }
    
        public record ExportResult(int seasonCount, int entryCount) {
        }
    
        private static final class JsonParser {
            private final String text;
            private final Path source;
            private int index;
    
            JsonParser(String text, Path source) {
                this.text = text;
                this.source = source;
            }
    
            Object parse() throws IOException {
                skipWhitespace();
                Object value = parseValue();
                skipWhitespace();
                if (index != text.length()) {
                    fail("Contenuto dopo la fine del JSON");
                }
                return value;
            }
    
            private Object parseValue() throws IOException {
                skipWhitespace();
                if (index >= text.length()) fail("Valore mancante");
                return switch (text.charAt(index)) {
                    case '{' -> parseObject();
                    case '[' -> parseArray();
                    case '"' -> parseString();
                    case 't' -> parseLiteral("true", Boolean.TRUE);
                    case 'f' -> parseLiteral("false", Boolean.FALSE);
                    case 'n' -> parseLiteral("null", null);
                    default -> parseNumber();
                };
            }
    
            private Map<String, Object> parseObject() throws IOException {
                expect('{');
                Map<String, Object> result = new LinkedHashMap<>();
                skipWhitespace();
                if (peek('}')) { index++; return result; }
                while (true) {
                    skipWhitespace();
                    String key = parseString();
                    skipWhitespace();
                    expect(':');
                    result.put(key, parseValue());
                    skipWhitespace();
                    if (peek('}')) { index++; return result; }
                    expect(',');
                }
            }
    
            private List<Object> parseArray() throws IOException {
                expect('[');
                List<Object> result = new ArrayList<>();
                skipWhitespace();
                if (peek(']')) { index++; return result; }
                while (true) {
                    result.add(parseValue());
                    skipWhitespace();
                    if (peek(']')) { index++; return result; }
                    expect(',');
                }
            }
    
            private String parseString() throws IOException {
                expect('"');
                StringBuilder result = new StringBuilder();
                while (index < text.length()) {
                    char ch = text.charAt(index++);
                    if (ch == '"') return result.toString();
                    if (ch != '\\') { result.append(ch); continue; }
                    if (index >= text.length()) fail("Escape incompleto");
                    char esc = text.charAt(index++);
                    switch (esc) {
                        case '"', '\\', '/' -> result.append(esc);
                        case 'b' -> result.append('\b');
                        case 'f' -> result.append('\f');
                        case 'n' -> result.append('\n');
                        case 'r' -> result.append('\r');
                        case 't' -> result.append('\t');
                        case 'u' -> result.append(parseUnicode());
                        default -> fail("Escape non valido: \\" + esc);
                    }
                }
                fail("Stringa non terminata");
                return null;
            }
    
            private char parseUnicode() throws IOException {
                if (index + 4 > text.length()) fail("Escape unicode incompleto");
                String hex = text.substring(index, index + 4);
                index += 4;
                try { return (char) Integer.parseInt(hex, 16); }
                catch (NumberFormatException ex) { fail("Escape unicode non valido: " + hex); return 0; }
            }
    
            private Object parseLiteral(String literal, Object value) throws IOException {
                if (!text.startsWith(literal, index)) fail("Token non valido");
                index += literal.length();
                return value;
            }
    
            private BigDecimal parseNumber() throws IOException {
                int start = index;
                if (peek('-')) index++;
                while (index < text.length() && Character.isDigit(text.charAt(index))) index++;
                if (peek('.')) {
                    index++;
                    while (index < text.length() && Character.isDigit(text.charAt(index))) index++;
                }
                if (peek('e') || peek('E')) {
                    index++;
                    if (peek('+') || peek('-')) index++;
                    while (index < text.length() && Character.isDigit(text.charAt(index))) index++;
                }
                if (start == index) fail("Numero non valido");
                try { return new BigDecimal(text.substring(start, index)); }
                catch (NumberFormatException ex) { fail("Numero non valido"); return null; }
            }
    
            private void expect(char expected) throws IOException {
                skipWhitespace();
                if (index >= text.length() || text.charAt(index) != expected) {
                    fail("Atteso '" + expected + "'");
                }
                index++;
            }
    
            private boolean peek(char value) {
                return index < text.length() && text.charAt(index) == value;
            }
    
            private void skipWhitespace() {
                while (index < text.length() && Character.isWhitespace(text.charAt(index))) index++;
            }
    
            private void fail(String message) throws IOException {
                throw new IOException(message + " in " + source + " alla posizione " + index);
            }
        }
    
        private static final class JsonWriter {
            static String write(Object value) {
                StringBuilder out = new StringBuilder();
                append(out, value);
                return out.toString();
            }
    
            private static void append(StringBuilder out, Object value) {
                if (value == null) { out.append("null"); return; }
                if (value instanceof String string) { out.append('"').append(escape(string)).append('"'); return; }
                if (value instanceof Boolean || value instanceof BigDecimal) { out.append(value); return; }
                if (value instanceof Number number) { out.append(number); return; }
                if (value instanceof Map<?, ?> map) {
                    out.append('{');
                    boolean first = true;
                    for (Map.Entry<?, ?> entry : map.entrySet()) {
                        if (!first) out.append(',');
                        first = false;
                        out.append('"').append(escape(String.valueOf(entry.getKey()))).append("\":");
                        append(out, entry.getValue());
                    }
                    out.append('}');
                    return;
                }
                if (value instanceof List<?> list) {
                    out.append('[');
                    for (int i = 0; i < list.size(); i++) {
                        if (i > 0) out.append(',');
                        append(out, list.get(i));
                    }
                    out.append(']');
                    return;
                }
                throw new IllegalArgumentException("Tipo JSON non supportato: " + value.getClass());
            }
    
            static String escape(String value) {
                StringBuilder escaped = new StringBuilder(value.length() + 16);
                for (int i = 0; i < value.length(); i++) {
                    char ch = value.charAt(i);
                    switch (ch) {
                        case '\\' -> escaped.append("\\\\");
                        case '"' -> escaped.append("\\\"");
                        case '\b' -> escaped.append("\\b");
                        case '\f' -> escaped.append("\\f");
                        case '\n' -> escaped.append("\\n");
                        case '\r' -> escaped.append("\\r");
                        case '\t' -> escaped.append("\\t");
                        default -> {
                            if (ch < 0x20) escaped.append(String.format("\\u%04x", (int) ch));
                            else escaped.append(ch);
                        }
                    }
                }
                return escaped.toString();
            }
        }
    }

## src\main\java\it\alterlega\recordsnext\Records2026RuJsExporter.java

File: src\main\java\it\alterlega\recordsnext\Records2026RuJsExporter.java

    package it.alterlega.recordsnext;
    
    import java.io.IOException;
    import java.math.BigDecimal;
    import java.nio.charset.StandardCharsets;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.nio.file.StandardOpenOption;
    import java.time.LocalDateTime;
    import java.time.format.DateTimeFormatter;
    import java.util.ArrayList;
    import java.util.Comparator;
    import java.util.LinkedHashMap;
    import java.util.List;
    import java.util.Map;
    import java.util.stream.Stream;
    
    /** Genera gli output RU pubblici compatibili con Records2026. */
    public final class Records2026RuJsExporter {
        private Records2026RuJsExporter() {}
    
        public static void main(String[] args) throws Exception {
            if (args.length != 2) {
                System.err.println("Uso: Records2026RuJsExporter <archive-riserveufficio> <output-js-dir>");
                System.exit(2);
            }
            ExportResult result = export(Path.of(args[0]), Path.of(args[1]));
            System.out.println("Archivio : " + Path.of(args[0]).toAbsolutePath().normalize());
            System.out.println("Output   : " + Path.of(args[1]).toAbsolutePath().normalize());
            System.out.println("Stagioni : " + result.seasons());
            System.out.println("Annuali  : " + result.annualFiles());
        }
    
        public static ExportResult export(Path archiveRoot, Path outputDir) throws IOException {
            archiveRoot = archiveRoot.toAbsolutePath().normalize();
            outputDir = outputDir.toAbsolutePath().normalize();
            if (!Files.isDirectory(archiveRoot)) throw new IOException("Archivio RU non trovato: " + archiveRoot);
            Files.createDirectories(outputDir);
    
            List<Path> seasonDirs;
            try (Stream<Path> s = Files.list(archiveRoot)) {
                seasonDirs = s.filter(Files::isDirectory)
                        .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                        .toList();
            }
    
            List<Object> compactItems = new ArrayList<>();
            List<Object> manifestItems = new ArrayList<>();
            int annualFiles = 0;
    
            for (Path seasonDir : seasonDirs) {
                String season = seasonDir.getFileName().toString();
                Path jsonPath = seasonDir.resolve("riserveufficio.json");
                if (!Files.isRegularFile(jsonPath)) continue;
    
                String rawSource = readUtf8WithoutBom(jsonPath);
                String source = normalizeJsonText(rawSource);
                Object parsed = new JsonParser(source, jsonPath).parse();
                Map<String,Object> root = asObject(parsed, jsonPath, "radice");
    
                String annualJson = escapeScriptTerminator(stripTrailingLineBreaks(rawSource));
                String annual = "window.RECORDS2026_STORICO_RU = window.RECORDS2026_STORICO_RU || {};\r\n"
                        + "window.RECORDS2026_STORICO_RU['" + JsonWriter.escape(season) + "'] = " + annualJson + ";\r\n";
                Path annualPath = outputDir.resolve("records2026.storico.ru." + season.replaceAll("[^\\w]+", "_") + ".js");
                writeUtf8Bom(annualPath, annual);
                annualFiles++;
    
                Map<String,Object> data = new LinkedHashMap<>();
                data.put("views", compactArrayMap(root.get("views")));
                data.put("dettaglio", compactArrayMap(root.get("dettaglio")));
                data.put("curiosita", root.get("curiosita"));
                Map<String,Object> compact = new LinkedHashMap<>();
                compact.put("stagione", season);
                compact.put("data", data);
                compactItems.add(compact);
    
                Map<String,Object> detail = objectOrEmpty(root.get("dettaglio"));
                int ruRows = listSize(detail.get("ruDettaglio"));
                Map<String,Object> meta = objectOrEmpty(root.get("meta"));
                Map<String,Object> manifestItem = new LinkedHashMap<>();
                manifestItem.put("stagione", season);
                manifestItem.put("jsFile", annualPath.getFileName().toString());
                manifestItem.put("ruDettaglio", ruRows);
                manifestItem.put("generated", stringValue(meta.get("generato")));
                manifestItems.add(manifestItem);
            }
    
            String compactJs = "window.RECORDS2026_PREVIEW_RU = "
                    + escapeScriptTerminator(JsonWriter.write(compactItems)) + ";";
            writeUtf8(outputDir.resolve("records2026.recordstagionali.ru.js"), compactJs);
    
            Map<String,Object> manifestMeta = new LinkedHashMap<>();
            manifestMeta.put("titolo", "Records2026 Storico Riserve d'Ufficio");
            manifestMeta.put("generato", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            manifestMeta.put("modello", "manifest + js annuale");
            Map<String,Object> manifest = new LinkedHashMap<>();
            manifest.put("meta", manifestMeta);
            manifest.put("stagioni", manifestItems);
            String manifestJs = "window.RECORDS2026_STORICO_RU_MANIFEST = "
                    + JsonWriter.writePretty(manifest) + ";\r\n";
            writeUtf8Bom(outputDir.resolve("records2026.storico.ru.manifest.js"), manifestJs);
    
            return new ExportResult(compactItems.size(), annualFiles);
        }
    
        private static Map<String,Object> compactArrayMap(Object value) {
            Map<String,Object> out = new LinkedHashMap<>();
            for (Map.Entry<String,Object> e : objectOrEmpty(value).entrySet()) {
                if (e.getValue() instanceof List<?> rows) {
                    List<Object> compactRows = new ArrayList<>(rows.size());
                    for (Object row : rows) compactRows.add(compactRow(row));
                    out.put(e.getKey(), compactRows);
                } else if (e.getValue() == null) {
                    out.put(e.getKey(), List.of());
                }
            }
            return out;
        }
    
        private static Map<String,Object> compactRow(Object value) {
            Map<String,Object> out = new LinkedHashMap<>();
            if (!(value instanceof Map<?,?> raw)) return out;
            for (Map.Entry<?,?> e : raw.entrySet()) {
                String name = String.valueOf(e.getKey());
                Object v = e.getValue();
                if (name.equals("dettagli")) {
                    if (v instanceof List<?> rows) out.put("dettagliCount", rows.size());
                    continue;
                }
                if (name.matches("^(dettaglio|dettagliPartite|partiteDettaglio|rows|raw|sourceRows)$")) continue;
                if (v == null || v instanceof String || v instanceof Number || v instanceof Boolean) out.put(name, v);
            }
            return out;
        }
    
        private static Map<String,Object> asObject(Object value, Path source, String label) throws IOException {
            if (!(value instanceof Map<?,?> raw)) throw new IOException("Oggetto JSON '" + label + "' non valido: " + source);
            Map<String,Object> out = new LinkedHashMap<>();
            for (Map.Entry<?,?> e : raw.entrySet()) out.put(String.valueOf(e.getKey()), e.getValue());
            return out;
        }
        private static Map<String,Object> objectOrEmpty(Object value) {
            Map<String,Object> out = new LinkedHashMap<>();
            if (value instanceof Map<?,?> raw) for (Map.Entry<?,?> e : raw.entrySet()) out.put(String.valueOf(e.getKey()), e.getValue());
            return out;
        }
        private static int listSize(Object value) { return value instanceof List<?> l ? l.size() : 0; }
        private static String stringValue(Object value) { return value == null ? "" : String.valueOf(value); }
        private static String readUtf8WithoutBom(Path path) throws IOException {
            String text = Files.readString(path, StandardCharsets.UTF_8);
            return (!text.isEmpty() && text.charAt(0) == '\uFEFF') ? text.substring(1) : text;
        }
        private static String stripTrailingLineBreaks(String text) {
            int end = text.length();
            while (end > 0 && (text.charAt(end - 1) == '\r' || text.charAt(end - 1) == '\n')) end--;
            return text.substring(0, end);
        }
        private static String normalizeJsonText(String text) {
            if (text == null || text.isEmpty()) return "";
            if (text.charAt(0) == '\uFEFF') text = text.substring(1);
            return text.trim();
        }
        private static String escapeScriptTerminator(String json) { return json.replace("</script>", "<\\/script>"); }
        private static void writeUtf8(Path path, String text) throws IOException {
            Files.writeString(path, text, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
        private static void writeUtf8Bom(Path path, String text) throws IOException {
            byte[] body = text.getBytes(StandardCharsets.UTF_8);
            byte[] out = new byte[body.length + 3];
            out[0] = (byte)0xEF; out[1] = (byte)0xBB; out[2] = (byte)0xBF;
            System.arraycopy(body, 0, out, 3, body.length);
            Files.write(path, out, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
        public record ExportResult(int seasons, int annualFiles) {}
    
        private static final class JsonParser {
            private final String text;
            private final Path source;
            private int index;
    
            JsonParser(String text, Path source) {
                this.text = text;
                this.source = source;
            }
    
            Object parse() throws IOException {
                skipWhitespace();
                Object value = parseValue();
                skipWhitespace();
                if (index != text.length()) {
                    fail("Contenuto dopo la fine del JSON");
                }
                return value;
            }
    
            private Object parseValue() throws IOException {
                skipWhitespace();
                if (index >= text.length()) fail("Valore mancante");
                return switch (text.charAt(index)) {
                    case '{' -> parseObject();
                    case '[' -> parseArray();
                    case '"' -> parseString();
                    case 't' -> parseLiteral("true", Boolean.TRUE);
                    case 'f' -> parseLiteral("false", Boolean.FALSE);
                    case 'n' -> parseLiteral("null", null);
                    default -> parseNumber();
                };
            }
    
            private Map<String, Object> parseObject() throws IOException {
                expect('{');
                Map<String, Object> result = new LinkedHashMap<>();
                skipWhitespace();
                if (peek('}')) { index++; return result; }
                while (true) {
                    skipWhitespace();
                    String key = parseString();
                    skipWhitespace();
                    expect(':');
                    result.put(key, parseValue());
                    skipWhitespace();
                    if (peek('}')) { index++; return result; }
                    expect(',');
                }
            }
    
            private List<Object> parseArray() throws IOException {
                expect('[');
                List<Object> result = new ArrayList<>();
                skipWhitespace();
                if (peek(']')) { index++; return result; }
                while (true) {
                    result.add(parseValue());
                    skipWhitespace();
                    if (peek(']')) { index++; return result; }
                    expect(',');
                }
            }
    
            private String parseString() throws IOException {
                expect('"');
                StringBuilder result = new StringBuilder();
                while (index < text.length()) {
                    char ch = text.charAt(index++);
                    if (ch == '"') return result.toString();
                    if (ch != '\\') { result.append(ch); continue; }
                    if (index >= text.length()) fail("Escape incompleto");
                    char esc = text.charAt(index++);
                    switch (esc) {
                        case '"', '\\', '/' -> result.append(esc);
                        case 'b' -> result.append('\b');
                        case 'f' -> result.append('\f');
                        case 'n' -> result.append('\n');
                        case 'r' -> result.append('\r');
                        case 't' -> result.append('\t');
                        case 'u' -> result.append(parseUnicode());
                        default -> fail("Escape non valido: \\" + esc);
                    }
                }
                fail("Stringa non terminata");
                return null;
            }
    
            private char parseUnicode() throws IOException {
                if (index + 4 > text.length()) fail("Escape unicode incompleto");
                String hex = text.substring(index, index + 4);
                index += 4;
                try { return (char) Integer.parseInt(hex, 16); }
                catch (NumberFormatException ex) { fail("Escape unicode non valido: " + hex); return 0; }
            }
    
            private Object parseLiteral(String literal, Object value) throws IOException {
                if (!text.startsWith(literal, index)) fail("Token non valido");
                index += literal.length();
                return value;
            }
    
            private BigDecimal parseNumber() throws IOException {
                int start = index;
                if (peek('-')) index++;
                while (index < text.length() && Character.isDigit(text.charAt(index))) index++;
                if (peek('.')) {
                    index++;
                    while (index < text.length() && Character.isDigit(text.charAt(index))) index++;
                }
                if (peek('e') || peek('E')) {
                    index++;
                    if (peek('+') || peek('-')) index++;
                    while (index < text.length() && Character.isDigit(text.charAt(index))) index++;
                }
                if (start == index) fail("Numero non valido");
                try { return new BigDecimal(text.substring(start, index)); }
                catch (NumberFormatException ex) { fail("Numero non valido"); return null; }
            }
    
            private void expect(char expected) throws IOException {
                skipWhitespace();
                if (index >= text.length() || text.charAt(index) != expected) {
                    fail("Atteso '" + expected + "'");
                }
                index++;
            }
    
            private boolean peek(char value) {
                return index < text.length() && text.charAt(index) == value;
            }
    
            private void skipWhitespace() {
                while (index < text.length() && Character.isWhitespace(text.charAt(index))) index++;
            }
    
            private void fail(String message) throws IOException {
                throw new IOException(message + " in " + source + " alla posizione " + index);
            }
        }
    
        private static final class JsonWriter {
            static String write(Object value) {
                StringBuilder out = new StringBuilder();
                append(out, value);
                return out.toString();
            }
    
            static String writePretty(Object value) {
                StringBuilder out = new StringBuilder();
                appendPretty(out, value, 0);
                return out.toString();
            }
    
            private static void appendPretty(StringBuilder out, Object value, int depth) {
                if (value == null || value instanceof String || value instanceof Boolean || value instanceof Number) { append(out, value); return; }
                String indent = "    ".repeat(depth);
                String childIndent = "    ".repeat(depth + 1);
                if (value instanceof Map<?, ?> map) {
                    if (map.isEmpty()) { out.append("{}"); return; }
                    out.append("{\r\n");
                    boolean first = true;
                    for (Map.Entry<?, ?> entry : map.entrySet()) {
                        if (!first) out.append(",\r\n");
                        first = false;
                        out.append(childIndent).append('"').append(escape(String.valueOf(entry.getKey()))).append("\": ");
                        appendPretty(out, entry.getValue(), depth + 1);
                    }
                    out.append("\r\n").append(indent).append("}");
                    return;
                }
                if (value instanceof List<?> list) {
                    if (list.isEmpty()) { out.append("[]"); return; }
                    out.append("[\r\n");
                    for (int i = 0; i < list.size(); i++) {
                        if (i > 0) out.append(",\r\n");
                        out.append(childIndent);
                        appendPretty(out, list.get(i), depth + 1);
                    }
                    out.append("\r\n").append(indent).append("]");
                    return;
                }
                append(out, value);
            }
    
            private static void append(StringBuilder out, Object value) {
                if (value == null) { out.append("null"); return; }
                if (value instanceof String string) { out.append('"').append(escape(string)).append('"'); return; }
                if (value instanceof Boolean || value instanceof BigDecimal) { out.append(value); return; }
                if (value instanceof Number number) { out.append(number); return; }
                if (value instanceof Map<?, ?> map) {
                    out.append('{');
                    boolean first = true;
                    for (Map.Entry<?, ?> entry : map.entrySet()) {
                        if (!first) out.append(',');
                        first = false;
                        out.append('"').append(escape(String.valueOf(entry.getKey()))).append("\":");
                        append(out, entry.getValue());
                    }
                    out.append('}');
                    return;
                }
                if (value instanceof List<?> list) {
                    out.append('[');
                    for (int i = 0; i < list.size(); i++) {
                        if (i > 0) out.append(',');
                        append(out, list.get(i));
                    }
                    out.append(']');
                    return;
                }
                throw new IllegalArgumentException("Tipo JSON non supportato: " + value.getClass());
            }
    
            static String escape(String value) {
                StringBuilder escaped = new StringBuilder(value.length() + 16);
                for (int i = 0; i < value.length(); i++) {
                    char ch = value.charAt(i);
                    switch (ch) {
                        case '\\' -> escaped.append("\\\\");
                        case '"' -> escaped.append("\\\"");
                        case '\b' -> escaped.append("\\b");
                        case '\f' -> escaped.append("\\f");
                        case '\n' -> escaped.append("\\n");
                        case '\r' -> escaped.append("\\r");
                        case '\t' -> escaped.append("\\t");
                        default -> {
                            if (ch < 0x20) escaped.append(String.format("\\u%04x", (int) ch));
                            else escaped.append(ch);
                        }
                    }
                }
                return escaped.toString();
            }
        }
    }

## src\main\java\it\alterlega\recordsnext\Records2026SitePublisher.java

File: src\main\java\it\alterlega\recordsnext\Records2026SitePublisher.java

    package it.alterlega.recordsnext;
    
    import it.alterlega.recordsnext.app.PipelinePreflight;
    import it.alterlega.recordsnext.app.ProcessingOptions;
    import it.alterlega.recordsnext.app.manifest.ManifestJsWriter;
    import it.alterlega.recordsnext.app.manifest.ManifestMetadata;
    import it.alterlega.recordsnext.app.manifest.ManifestPublishingSupport;
    
    import java.io.IOException;
    import java.nio.charset.StandardCharsets;
    import java.nio.file.AtomicMoveNotSupportedException;
    import java.nio.file.DirectoryStream;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.nio.file.StandardCopyOption;
    import java.time.LocalDateTime;
    import java.time.format.DateTimeFormatter;
    import java.util.ArrayList;
    import java.util.Comparator;
    import java.util.LinkedHashMap;
    import java.util.List;
    import java.util.Map;
    import java.util.UUID;
    
    /**
     * Coordina la generazione e la pubblicazione degli output JS compatibili
     * con Records2026.
     *
     * Flusso:
     *  1. genera tutto in una staging isolata;
     *  2. valida nomi, quantità, prefissi e dimensioni minime;
     *  3. pubblica ogni file mediante file temporaneo + move atomica;
     *  4. ripristina i file precedenti se una pubblicazione fallisce.
     */
    public final class Records2026SitePublisher {
    
        private static final String CLASSIC_FILE = "records2026.recordstagionali.classic.js";
        private static final String RU_FILE = "records2026.recordstagionali.ru.js";
        private static final String MANIFEST_FILE = "records2026.storico.ru.manifest.js";
        private static final String ANNUAL_PREFIX = "records2026.storico.ru.";
        private static final String ANNUAL_SUFFIX = ".js";
    
        private Records2026SitePublisher() {
        }
    
        public static void main(String[] args) throws Exception {
            if (args.length < 4 || args.length > 5) {
                System.err.println("Uso:");
                System.err.println("  Records2026SitePublisher <classicArchive> <ruArchive> <stagingRoot> <siteJsDir> [--generate-only]");
                System.exit(2);
            }
    
            Path classicArchive = Path.of(args[0]).toAbsolutePath().normalize();
            Path ruArchive = Path.of(args[1]).toAbsolutePath().normalize();
            Path stagingRoot = Path.of(args[2]).toAbsolutePath().normalize();
            Path siteJsDir = Path.of(args[3]).toAbsolutePath().normalize();
            boolean generateOnly = args.length == 5 && "--generate-only".equalsIgnoreCase(args[4]);
    
            PublishResult result = run(classicArchive, ruArchive, stagingRoot, siteJsDir, generateOnly);
    
            System.out.println("Classic     : " + result.classicEntries() + " recordset");
            System.out.println("RU stagioni : " + result.ruSeasons());
            System.out.println("RU annuali  : " + result.annualFiles());
            System.out.println("File validi : " + result.validatedFiles());
            System.out.println("Staging     : " + result.stagingDirectory());
            System.out.println(generateOnly
                    ? "Pubblicazione: NON ESEGUITA (--generate-only)"
                    : "Pubblicati   : " + result.publishedFiles() + " file in " + siteJsDir);
        }
    
        public static PublishResult run(
                Path classicArchive,
                Path ruArchive,
                Path stagingRoot,
                Path siteJsDir,
                boolean generateOnly) throws IOException {
            return run(classicArchive, ruArchive, stagingRoot, siteJsDir, generateOnly, true, true);
        }
    
        public static PublishResult run(
                Path classicArchive,
                Path ruArchive,
                Path stagingRoot,
                Path siteJsDir,
                boolean generateOnly,
                boolean includeClassic,
                boolean includeRu) throws IOException {
            return runInternal(
                    classicArchive,
                    ruArchive,
                    stagingRoot,
                    siteJsDir,
                    generateOnly,
                    includeClassic,
                    includeRu,
                    null,
                    null,
                    null
            );
        }
    
        public static PublishResult run(
                Path classicArchive,
                Path ruArchive,
                Path stagingRoot,
                Path siteJsDir,
                boolean generateOnly,
                boolean includeClassic,
                boolean includeRu,
                ProcessingOptions options,
                PipelinePreflight.Result preflight,
                ManifestMetadata manifestMetadata) throws IOException {
            return runInternal(
                    classicArchive,
                    ruArchive,
                    stagingRoot,
                    siteJsDir,
                    generateOnly,
                    includeClassic,
                    includeRu,
                    options,
                    preflight,
                    manifestMetadata
            );
        }
    
        private static PublishResult runInternal(
                Path classicArchive,
                Path ruArchive,
                Path stagingRoot,
                Path siteJsDir,
                boolean generateOnly,
                boolean includeClassic,
                boolean includeRu,
                ProcessingOptions options,
                PipelinePreflight.Result preflight,
                ManifestMetadata manifestMetadata) throws IOException {
    
            boolean includeRecordsNextManifest = options != null
                    && preflight != null
                    && manifestMetadata != null;
    
            if (!includeClassic && !includeRu && !includeRecordsNextManifest) {
                throw new IOException("Nessun modulo selezionato per la generazione JS");
            }
            if (includeClassic) requireDirectory(classicArchive, "Archivio classic");
            if (includeRu) requireDirectory(ruArchive, "Archivio RU");
            Files.createDirectories(stagingRoot);
    
            String runId = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                    + "_" + UUID.randomUUID().toString().substring(0, 8);
            Path runDir = stagingRoot.resolve("records2026_" + runId);
            Path generatedDir = runDir.resolve("js");
            Files.createDirectories(generatedDir);
    
            int classicEntries = 0;
            int ruSeasons = 0;
            int annualFiles = 0;
            if (includeClassic) {
                var classic = Records2026ClassicJsExporter.export(
                        classicArchive, generatedDir.resolve(CLASSIC_FILE), List.of());
                classicEntries = classic.entryCount();
            }
            if (includeRu) {
                var ru = Records2026RuJsExporter.export(ruArchive, generatedDir);
                ruSeasons = ru.seasons();
                annualFiles = ru.annualFiles();
            }
    
            if (includeRecordsNextManifest) {
                ManifestPublishingSupport.write(
                        generatedDir,
                        options,
                        preflight,
                        manifestMetadata
                );
            }
    
            ValidationResult validation = validateGenerated(
                    generatedDir,
                    annualFiles,
                    includeClassic,
                    includeRu,
                    includeRecordsNextManifest
            );
            int published = 0;
            if (!generateOnly) {
                Files.createDirectories(siteJsDir);
                published = publishWithRollback(generatedDir, siteJsDir, validation.files());
            }
            return new PublishResult(classicEntries, ruSeasons, annualFiles,
                    validation.files().size(), published, runDir);
        }
    
        private static ValidationResult validateGenerated(
                Path generatedDir,
                int expectedAnnualFiles,
                boolean includeClassic,
                boolean includeRu,
                boolean includeRecordsNextManifest) throws IOException {
    
            List<Path> files;
            try (var stream = Files.list(generatedDir)) {
                files = stream
                        .filter(Files::isRegularFile)
                        .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                        .toList();
            }
    
            Map<String, Path> byName = new LinkedHashMap<>();
            for (Path file : files) {
                byName.put(file.getFileName().toString(), file);
            }
    
            if (includeClassic) {
                requireFile(byName, CLASSIC_FILE);
                validatePrefix(byName.get(CLASSIC_FILE), "window.RECORDS2026_PREVIEW_CLASSIC");
            }
            List<Path> annuals = files.stream().filter(Records2026SitePublisher::isAnnualFile).toList();
            if (includeRu) {
                requireFile(byName, RU_FILE);
                requireFile(byName, MANIFEST_FILE);
                if (annuals.size() != expectedAnnualFiles) {
                    throw new IOException("Numero file annuali inatteso: " + annuals.size()
                            + ", attesi " + expectedAnnualFiles);
                }
                validatePrefix(byName.get(RU_FILE), "window.RECORDS2026_PREVIEW_RU");
                validatePrefix(byName.get(MANIFEST_FILE), "window.RECORDS2026_STORICO_RU_MANIFEST");
                for (Path annual : annuals) validateContains(annual, "window.RECORDS2026_STORICO_RU");
            } else if (!annuals.isEmpty()) {
                throw new IOException("File RU annuali generati nonostante il modulo RU sia disattivato");
            }
            if (includeRecordsNextManifest) {
                requireFile(byName, ManifestJsWriter.FILE_NAME);
                validatePrefix(
                        byName.get(ManifestJsWriter.FILE_NAME),
                        "window.fcmRecordsNextManifest"
                );
            }
            int expectedTotal = (includeClassic ? 1 : 0)
                    + (includeRu ? expectedAnnualFiles + 2 : 0)
                    + (includeRecordsNextManifest ? 1 : 0);
            if (files.size() != expectedTotal) {
                throw new IOException("Numero file JS inatteso: " + files.size()
                        + ", attesi " + expectedTotal);
            }
    
            return new ValidationResult(files);
        }
    
        private static int publishWithRollback(Path generatedDir, Path siteJsDir, List<Path> generatedFiles)
                throws IOException {
    
            Path transactionDir = generatedDir.getParent().resolve("publish-transaction");
            Path backupDir = transactionDir.resolve("backup");
            Files.createDirectories(backupDir);
    
            List<String> replacedNames = new ArrayList<>();
            List<String> newlyCreatedNames = new ArrayList<>();
    
            try {
                for (Path source : generatedFiles) {
                    String name = source.getFileName().toString();
                    Path target = siteJsDir.resolve(name);
                    Path backup = backupDir.resolve(name);
    
                    if (Files.exists(target)) {
                        Files.copy(target, backup,
                                StandardCopyOption.REPLACE_EXISTING,
                                StandardCopyOption.COPY_ATTRIBUTES);
                        replacedNames.add(name);
                    } else {
                        newlyCreatedNames.add(name);
                    }
    
                    Path temp = siteJsDir.resolve("." + name + ".recordsnext-" + UUID.randomUUID() + ".tmp");
                    Files.copy(source, temp, StandardCopyOption.REPLACE_EXISTING);
                    moveReplace(temp, target);
                }
            } catch (Exception publicationFailure) {
                IOException rollbackFailure = rollback(siteJsDir, backupDir, replacedNames, newlyCreatedNames);
                if (rollbackFailure != null) {
                    publicationFailure.addSuppressed(rollbackFailure);
                }
                if (publicationFailure instanceof IOException io) {
                    throw io;
                }
                throw new IOException("Pubblicazione fallita", publicationFailure);
            }
    
            return generatedFiles.size();
        }
    
        private static IOException rollback(
                Path siteJsDir,
                Path backupDir,
                List<String> replacedNames,
                List<String> newlyCreatedNames) {
    
            IOException firstFailure = null;
    
            for (String name : newlyCreatedNames) {
                try {
                    Files.deleteIfExists(siteJsDir.resolve(name));
                } catch (IOException ex) {
                    if (firstFailure == null) firstFailure = ex;
                    else firstFailure.addSuppressed(ex);
                }
            }
    
            for (String name : replacedNames) {
                try {
                    Path backup = backupDir.resolve(name);
                    Path temp = siteJsDir.resolve("." + name + ".rollback-" + UUID.randomUUID() + ".tmp");
                    Files.copy(backup, temp, StandardCopyOption.REPLACE_EXISTING);
                    moveReplace(temp, siteJsDir.resolve(name));
                } catch (IOException ex) {
                    if (firstFailure == null) firstFailure = ex;
                    else firstFailure.addSuppressed(ex);
                }
            }
    
            return firstFailure;
        }
    
        private static void moveReplace(Path source, Path target) throws IOException {
            try {
                Files.move(source, target,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    
        private static void requireDirectory(Path path, String label) throws IOException {
            if (!Files.isDirectory(path)) {
                throw new IOException(label + " inesistente o non valida: " + path);
            }
        }
    
        private static void requireFile(Map<String, Path> files, String name) throws IOException {
            if (!files.containsKey(name)) {
                throw new IOException("File generato mancante: " + name);
            }
        }
    
        private static boolean isAnnualFile(Path path) {
            String name = path.getFileName().toString();
            return name.startsWith(ANNUAL_PREFIX)
                    && name.endsWith(ANNUAL_SUFFIX)
                    && !name.equals(MANIFEST_FILE);
        }
    
        private static void validatePrefix(Path path, String expectedPrefix) throws IOException {
            String sample = readStart(path, 4096);
            if (!stripBom(sample).stripLeading().startsWith(expectedPrefix)) {
                throw new IOException("Prefisso JS non valido in " + path.getFileName()
                        + ": atteso " + expectedPrefix);
            }
        }
    
        private static void validateContains(Path path, String expectedToken) throws IOException {
            String sample = readStart(path, 8192);
            if (!stripBom(sample).contains(expectedToken)) {
                throw new IOException("Token JS non trovato in " + path.getFileName()
                        + ": " + expectedToken);
            }
        }
    
        private static String readStart(Path path, int maxBytes) throws IOException {
            long size = Files.size(path);
            if (size <= 16) {
                throw new IOException("File generato vuoto o troppo corto: " + path);
            }
            byte[] bytes = new byte[(int) Math.min(size, maxBytes)];
            try (var input = Files.newInputStream(path)) {
                int offset = 0;
                while (offset < bytes.length) {
                    int read = input.read(bytes, offset, bytes.length - offset);
                    if (read < 0) break;
                    offset += read;
                }
                return new String(bytes, 0, offset, StandardCharsets.UTF_8);
            }
        }
    
        private static String stripBom(String value) {
            return !value.isEmpty() && value.charAt(0) == '\uFEFF' ? value.substring(1) : value;
        }
    
        public record PublishResult(
                int classicEntries,
                int ruSeasons,
                int annualFiles,
                int validatedFiles,
                int publishedFiles,
                Path stagingDirectory) {
        }
    
        private record ValidationResult(List<Path> files) {
        }
    }

## src\main\java\it\alterlega\recordsnext\RiserveUfficioArchiveBuilder.java

File: src\main\java\it\alterlega\recordsnext\RiserveUfficioArchiveBuilder.java

    package it.alterlega.recordsnext;
    
    import java.io.IOException;
    import java.math.BigDecimal;
    import java.math.RoundingMode;
    import java.nio.charset.StandardCharsets;
    import java.nio.file.*;
    import java.time.LocalDateTime;
    import java.time.format.DateTimeFormatter;
    import java.util.*;
    import java.util.stream.Stream;
    
    /** Costruisce riserveufficio.json dai season_normalized_*.json RecordsNext. */
    public final class RiserveUfficioArchiveBuilder {
        private RiserveUfficioArchiveBuilder() {}
    
        public static void main(String[] args) throws Exception {
            if (args.length < 2) {
                System.err.println("Uso: RiserveUfficioArchiveBuilder <reportsRoot> <archiveRoot> [stagione ...]");
                System.exit(2);
            }
            Path reports = Path.of(args[0]).toAbsolutePath().normalize();
            Path archive = Path.of(args[1]).toAbsolutePath().normalize();
            List<String> seasons = new ArrayList<>();
            for (int i = 2; i < args.length; i++) if (!args[i].isBlank()) seasons.add(args[i].trim());
            Result r = build(reports, archive, seasons);
            System.out.println("Report       : " + reports);
            System.out.println("Archivio RU  : " + archive);
            System.out.println("Stagioni     : " + r.seasons());
            System.out.println("File letti   : " + r.files());
            System.out.println("Righe RU     : " + r.reserveRows());
            System.out.println("Viste        : 12/12");
        }
    
        public static Result build(Path reportsRoot, Path archiveRoot, List<String> requested) throws IOException {
            if (!Files.isDirectory(reportsRoot)) throw new IOException("Cartella report non trovata: " + reportsRoot);
            Files.createDirectories(archiveRoot);
            int seasonCount = 0, fileCount = 0, rowCount = 0;
            for (Path seasonDir : resolveSeasonDirs(reportsRoot, requested)) {
                List<Path> files = listNormalizedFiles(seasonDir);
                if (files.isEmpty()) continue;
                String season = seasonDir.getFileName().toString();
                Map<String,Object> payload = buildSeason(season, files);
                Path outDir = archiveRoot.resolve(season);
                Files.createDirectories(outDir);
                Files.writeString(outDir.resolve("riserveufficio.json"), JsonWriter.writePretty(payload) + System.lineSeparator(),
                        StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                seasonCount++; fileCount += files.size();
                rowCount += rows(object(payload.get("dettaglio")).get("ruDettaglio")).size();
            }
            if (seasonCount == 0) throw new IOException("Nessuna stagione normalizzata trovata");
            return new Result(seasonCount, fileCount, rowCount);
        }
    
        private static Map<String,Object> buildSeason(String season, List<Path> files) throws IOException {
            List<Map<String,Object>> ruDetail = new ArrayList<>();
            Map<String,Map<String,Object>> matchByKey = new LinkedHashMap<>();
            Map<String,List<Map<String,Object>>> bandsByCompetition = new LinkedHashMap<>();
            Set<String> competitions = new TreeSet<>();
    
            for (Path file : files) {
                Map<String,Object> doc = object(parse(file));
                Map<String,Object> meta = object(doc.get("meta"));
                String compName = publicCompetitionName(s(meta.get("competizioneNome")));
                String compId = s(meta.get("idCompetizioneFcm"));
                competitions.add(compName);
                List<Map<String,Object>> matches = rows(doc.get("partiteSquadra"));
                for (Map<String,Object> m : matches) {
                    if (n(m.get("idSquadra")) == 0) continue;
                    matchByKey.put(s(m.get("idIncontro")) + "|" + s(m.get("idSquadra")), m);
                }
                bandsByCompetition.put(compId, rows(doc.get("fasceGolDettaglio")));
                for (Map<String,Object> raw : rows(doc.get("riserveUfficioDettaglio"))) {
                    Map<String,Object> match = matchByKey.get(s(raw.get("idIncontro")) + "|" + s(raw.get("idSquadra")));
                    if (match == null) continue;
                    ruDetail.add(detailRow(raw, match, compName));
                }
            }
    
            ruDetail.sort(compare("competizione", "giornataDiA", "idIncontro", "idSquadra", "ordine"));
            List<Map<String,Object>> teamMatch = teamMatch(ruDetail);
            List<Map<String,Object>> against = against(teamMatch, matchByKey);
            List<Map<String,Object>> decisive = new ArrayList<>(), decisiveAgainst = new ArrayList<>();
            calculateDecisive(teamMatch, bandsByCompetition, decisive, decisiveAgainst);
    
            Map<String,Object> views = new LinkedHashMap<>();
            views.put("partiteConPiuRU", matchesWithMostRu(ruDetail));
            views.put("partiteConRU", sorted(teamMatch, compare("competizione", "giornataDiA", "squadra")));
            views.put("partiteControRU", sorted(against, compare("competizione", "giornataDiA", "squadra")));
            views.put("ruDecisiva", decisive);
            views.put("bilancioRUDecisiva", decisiveBalance(decisive));
            views.put("ruDecisivaContro", decisiveAgainst);
            views.put("bilancioRUDecisivaContro", decisiveAgainstBalance(decisiveAgainst));
            List<Map<String,Object>> balanceWith = balance(teamMatch, true);
            List<Map<String,Object>> balanceAgainst = balance(against, false);
            views.put("bilancioConRU", balanceWith);
            views.put("bilancioControRU", balanceAgainst);
            views.put("mediaPuntiConRU", averagePoints(balanceWith, true));
            views.put("mediaPuntiControRU", averagePoints(balanceAgainst, false));
            views.put("tipoRUUsata", typeUsed(ruDetail));
    
            Map<String,Object> detail = new LinkedHashMap<>();
            detail.put("ruDettaglio", ruDetail);
            detail.put("ruTeamMatch", teamMatch);
    
            stripInternalFields(views);
            stripInternalFields(detail);
            Map<String,Object> meta = linked(
                    "titolo", "Riserve d'Ufficio",
                    "stagione", season,
                    "generato", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    "builder", "RecordsNext RiserveUfficioArchiveBuilder");
            Map<String,Object> out = new LinkedHashMap<>();
            out.put("meta", meta);
            out.put("competizioni", canonicalCompetitions());
            out.put("curiosita", curiosityDefinitions());
            out.put("views", views);
            out.put("dettaglio", detail);
            return out;
        }
    
        private static Map<String,Object> detailRow(Map<String,Object> r, Map<String,Object> m, String compName) {
            String side = s(m.get("lato"));
            String result = n(m.get("golFatti")) + "-" + n(m.get("golSubiti"));
            String score = plainText(m.get("puntiFatti")) + "-" + plainText(m.get("puntiSubiti"));
            String team = s(r.get("squadra"));
            String opponent = s(r.get("avversaria"));
            String homeTeam = "CASA".equalsIgnoreCase(side) ? team : opponent;
            String awayTeam = "CASA".equalsIgnoreCase(side) ? opponent : team;
            Object homePoints = "CASA".equalsIgnoreCase(side) ? m.get("puntiFatti") : m.get("puntiSubiti");
            Object awayPoints = "CASA".equalsIgnoreCase(side) ? m.get("puntiSubiti") : m.get("puntiFatti");
            int homeGoals = "CASA".equalsIgnoreCase(side) ? n(m.get("golFatti")) : n(m.get("golSubiti"));
            int awayGoals = "CASA".equalsIgnoreCase(side) ? n(m.get("golSubiti")) : n(m.get("golFatti"));
            return linked(
                    "idIncontro", s(r.get("idIncontro")), "competizione", compName,
                    "girone", publicGroupName(compName, m.get("gironeNome"), m.get("idGirone")), "giornataFCM", m.get("giornata"),
                    "giornataDiA", m.get("giornataDiA"), "urlTabellino", m.get("urlTabellino"),
                    "idSquadra", s(r.get("idSquadra")), "squadra", r.get("squadra"),
                    "idAvversaria", s(r.get("idAvversaria")), "avversaria", r.get("avversaria"),
                    "tipoRU", r.get("tipoRU"), "ruoloRU", r.get("ruoloRU"), "valoreRU", r.get("valoreRU"),
                    "ordine", r.get("ordine"), "votoTabellino", r.get("votoTabellino"),
                    "modifTabellino", r.get("modifTabellino"), "totTabellino", r.get("totTabellino"),
                    "puntiSquadra", m.get("puntiFatti"), "puntiAvversaria", m.get("puntiSubiti"),
                    "golSquadra", m.get("golFatti"), "golAvversaria", m.get("golSubiti"),
                    "risultato", result, "punteggio", score, "esito", legacyOutcome(s(m.get("esito"))),
                    "_idCompetizioneFcm", m.get("idCompetizioneFcm"), "_lato", side,
                    "_squadraCasa", homeTeam, "_squadraFuori", awayTeam,
                    "_puntiCasa", homePoints, "_puntiFuori", awayPoints,
                    "_golCasa", homeGoals, "_golFuori", awayGoals);
        }
    
        private static List<Map<String,Object>> teamMatch(List<Map<String,Object>> detail) {
            Map<String,List<Map<String,Object>>> groups = group(detail, r -> s(r.get("idIncontro")) + "|" + s(r.get("idSquadra")));
            List<Map<String,Object>> out = new ArrayList<>();
            for (List<Map<String,Object>> g : groups.values()) {
                Map<String,Object> f = g.get(0); LinkedHashSet<String> types = new LinkedHashSet<>();
                BigDecimal total = BigDecimal.ZERO; List<String> pieces = new ArrayList<>();
                for (Map<String,Object> r : g) { String type=s(r.get("tipoRU")); types.add(type); total=total.add(bd(r.get("valoreRU"))); pieces.add(type+"="+plain(r.get("valoreRU"))); }
                Map<String,Object> row = copyFields(f, "idIncontro","competizione","girone","giornataFCM","giornataDiA","urlTabellino","idSquadra","squadra","idAvversaria","avversaria");
                row.put("numeroRU", g.size()); row.put("valoreRUTotale", clean(total)); row.put("tipiRU", String.join(", ", types)); row.put("dettaglioRU", String.join("; ", pieces));
                copyInto(row, f, "puntiSquadra","puntiAvversaria","golSquadra","golAvversaria","risultato","punteggio","esito",
                        "_idCompetizioneFcm","_lato","_squadraCasa","_squadraFuori","_puntiCasa","_puntiFuori","_golCasa","_golFuori"); out.add(row);
            }
            return out;
        }
    
        private static List<Map<String,Object>> against(List<Map<String,Object>> teamMatch, Map<String,Map<String,Object>> matchByKey) {
            List<Map<String,Object>> out = new ArrayList<>();
            for (Map<String,Object> ru : teamMatch) {
                Map<String,Object> opp = matchByKey.get(s(ru.get("idIncontro")) + "|" + s(ru.get("idAvversaria")));
                if (opp == null) continue;
                out.add(linked("idIncontro",ru.get("idIncontro"),"competizione",ru.get("competizione"),"girone",ru.get("girone"),"giornataFCM",opp.get("giornata"),"giornataDiA",opp.get("giornataDiA"),"urlTabellino",opp.get("urlTabellino"),
                        "idSquadra",s(opp.get("idSquadra")),"squadra",opp.get("squadra"),"idAvversaria",ru.get("idSquadra"),"avversaria",ru.get("squadra"),"avversariaConRU",ru.get("squadra"),
                        "numeroRUAvversaria",ru.get("numeroRU"),"valoreRUAvversaria",ru.get("valoreRUTotale"),"tipiRUAvversaria",ru.get("tipiRU"),"dettaglioRUAvversaria",ru.get("dettaglioRU"),
                        "puntiSquadra",opp.get("puntiFatti"),"puntiAvversaria",opp.get("puntiSubiti"),"golSquadra",opp.get("golFatti"),"golAvversaria",opp.get("golSubiti"),
                        "risultato",n(opp.get("golFatti"))+"-"+n(opp.get("golSubiti")),
                        "punteggio",plainText(opp.get("puntiFatti"))+"-"+plainText(opp.get("puntiSubiti")),
                        "esito",legacyOutcome(s(opp.get("esito")))));
            }
            return out;
        }
    
        private static void calculateDecisive(List<Map<String,Object>> teamMatch, Map<String,List<Map<String,Object>>> bands, List<Map<String,Object>> yes, List<Map<String,Object>> against) {
            for (Map<String,Object> ru : teamMatch) {
                BigDecimal with = bd(ru.get("puntiSquadra")), value = bd(ru.get("valoreRUTotale")), without = with.subtract(value);
                int goalsWith=n(ru.get("golSquadra")), goalsOpp=n(ru.get("golAvversaria"));
                int goalsWithout=goals(without, bands.getOrDefault(s(ru.get("_idCompetizioneFcm")), List.of()));
                String resultWith=result(goalsWith, goalsOpp), resultWithout=result(goalsWithout, goalsOpp);
                int gained=points(resultWith)-points(resultWithout); if (gained<=0) continue;
                Map<String,Object> y=copyFields(ru,"idIncontro","competizione","girone","giornataFCM","giornataDiA","urlTabellino","idSquadra","squadra","idAvversaria","avversaria","tipiRU","dettaglioRU");
                y.put("valoreRU",clean(value)); y.put("puntiConRU",clean(with)); y.put("puntiSenzaRU",clean(without)); y.put("puntiAvversaria",ru.get("puntiAvversaria")); y.put("golConRU",goalsWith); y.put("golSenzaRU",goalsWithout); y.put("golAvversaria",goalsOpp); y.put("esitoSenzaRU",resultWithout); y.put("esitoConRU",resultWith); y.put("effetto",effect(resultWithout,resultWith)); y.put("puntiClassificaGuadagnati",gained); y.put("risultatoReale",ru.get("risultato")); y.put("punteggioReale",ru.get("punteggio")); yes.add(y);
                String oppWithout=result(goalsOpp, goalsWithout), oppWith=result(goalsOpp, goalsWith); int lost=points(oppWithout)-points(oppWith);
                against.add(linked("idIncontro",ru.get("idIncontro"),"competizione",ru.get("competizione"),"girone",ru.get("girone"),"giornataFCM",ru.get("giornataFCM"),"giornataDiA",ru.get("giornataDiA"),"urlTabellino",ru.get("urlTabellino"),
                        "idSquadra",ru.get("idAvversaria"),"squadra",ru.get("avversaria"),"idAvversaria",ru.get("idSquadra"),"avversaria",ru.get("squadra"),"avversariaConRU",ru.get("squadra"),"tipiRUAvversaria",ru.get("tipiRU"),"dettaglioRUAvversaria",ru.get("dettaglioRU"),"valoreRUAvversaria",clean(value),
                        "puntiSquadra",ru.get("puntiAvversaria"),"puntiAvversariaConRU",clean(with),"puntiAvversariaSenzaRU",clean(without),"golSquadra",goalsOpp,"golAvversariaConRU",goalsWith,"golAvversariaSenzaRU",goalsWithout,
                        "esitoSenzaRUAvversaria",oppWithout,"esitoConRUAvversaria",oppWith,"danno",damage(oppWithout,oppWith),"puntiClassificaPersi",lost,"risultatoReale",goalsOpp+"-"+goalsWith,"punteggioReale",plain(ru.get("puntiAvversaria"))+"-"+plain(with)));
            }
        }
    
        private static List<Map<String,Object>> matchesWithMostRu(List<Map<String,Object>> detail) {
            List<Map<String,Object>> out = new ArrayList<>();
            for (List<Map<String,Object>> g : group(detail, r -> s(r.get("idIncontro"))).values()) {
                g.sort(Comparator.<Map<String,Object>,BigDecimal>comparing(r -> bd(r.get("idSquadra"))).thenComparing(r -> bd(r.get("ordine"))));
                Map<String,Object> f = g.get(0);
                BigDecimal total = BigDecimal.ZERO;
                List<String> pieces = new ArrayList<>();
                for (Map<String,Object> r : g) {
                    total = total.add(bd(r.get("valoreRU")));
                    pieces.add(r.get("squadra") + ":" + r.get("tipoRU") + "=" + plain(r.get("valoreRU")));
                }
                out.add(linked("idIncontro",f.get("idIncontro"),"competizione",f.get("competizione"),"girone",f.get("girone"),
                        "giornataFCM",f.get("giornataFCM"),"giornataDiA",f.get("giornataDiA"),
                        "partita",f.get("squadra")+" - "+f.get("avversaria"),
                        "numeroRU",g.size(),"valoreRUTotale",clean(total),"dettaglioRU",String.join("; ",pieces),
                        "risultato",f.get("risultato"),
                        "punteggio",plain(f.get("puntiSquadra"))+"-"+plain(f.get("puntiAvversaria"))));
            }
            out.sort(compareDesc("numeroRU","valoreRUTotale","competizione","giornataDiA"));
            return out;
        }
    
        private static List<Map<String,Object>> balance(List<Map<String,Object>> source, boolean with) {
            List<Map<String,Object>> out=new ArrayList<>();
            for(List<Map<String,Object>> g:group(source,r->s(r.get("idSquadra"))).values()){Map<String,Object> f=g.get(0);int v=0,d=0,l=0;BigDecimal pf=BigDecimal.ZERO,pa=BigDecimal.ZERO,gf=BigDecimal.ZERO,ga=BigDecimal.ZERO;for(Map<String,Object> r:g){switch(s(r.get("esito"))){case"V"->v++;case"N"->d++;case"P"->l++;}pf=pf.add(bd(r.get("puntiSquadra")));pa=pa.add(bd(r.get("puntiAvversaria")));gf=gf.add(bd(r.get("golSquadra")));ga=ga.add(bd(r.get("golAvversaria")));}int count=g.size();
                out.add(linked("idSquadra",f.get("idSquadra"),"squadra",f.get("squadra"),with?"partiteConRU":"partiteControRU",count,"V",v,"N",d,"P",l,"percV",percent(v,count),"percN",percent(d,count),"percP",percent(l,count),"mediaPuntiSquadra",average(pf,count),"mediaPuntiAvversaria",average(pa,count),"mediaGolSquadra",average(gf,count),"mediaGolAvversaria",average(ga,count)));}
            out.sort(Comparator.<Map<String,Object>>comparingInt(r->n(r.get(with?"partiteConRU":"partiteControRU"))).reversed().thenComparing(r->s(r.get("squadra"))));return out;
        }
    
        private static List<Map<String,Object>> averagePoints(List<Map<String,Object>> balance, boolean with){List<Map<String,Object>>out=new ArrayList<>();for(Map<String,Object>r:balance)out.add(linked("idSquadra",r.get("idSquadra"),"squadra",r.get("squadra"),with?"partiteConRU":"partiteControRU",r.get(with?"partiteConRU":"partiteControRU"),"mediaPuntiSquadra",r.get("mediaPuntiSquadra"),"mediaPuntiAvversaria",r.get("mediaPuntiAvversaria"),"differenzaMedia",clean(bd(r.get("mediaPuntiSquadra")).subtract(bd(r.get("mediaPuntiAvversaria"))))));out.sort(Comparator.<Map<String,Object>,BigDecimal>comparing(r->bd(r.get("mediaPuntiSquadra"))).reversed().thenComparing(r->s(r.get("squadra"))));return out;}
    
        private static List<Map<String,Object>> typeUsed(List<Map<String,Object>> detail){List<Map<String,Object>>out=new ArrayList<>();for(List<Map<String,Object>>g:group(detail,r->s(r.get("idSquadra"))).values()){Map<String,Object>f=g.get(0);Map<String,Integer>c=new HashMap<>();Map<String,BigDecimal>v=new HashMap<>();for(String t:List.of("PU","DU","CU","AU")){c.put(t,0);v.put(t,BigDecimal.ZERO);}for(Map<String,Object>r:g){String t=s(r.get("tipoRU"));c.put(t,c.getOrDefault(t,0)+1);v.put(t,v.getOrDefault(t,BigDecimal.ZERO).add(bd(r.get("valoreRU"))));}out.add(linked("idSquadra",f.get("idSquadra"),"squadra",f.get("squadra"),"PU",c.get("PU"),"DU",c.get("DU"),"CU",c.get("CU"),"AU",c.get("AU"),"totaleRU",g.size(),"valorePU",clean(v.get("PU")),"valoreDU",clean(v.get("DU")),"valoreCU",clean(v.get("CU")),"valoreAU",clean(v.get("AU")),"valoreTotale",clean(v.values().stream().reduce(BigDecimal.ZERO,BigDecimal::add))));}out.sort(Comparator.<Map<String,Object>>comparingInt(r->n(r.get("totaleRU"))).reversed().thenComparing(r->s(r.get("squadra"))));return out;}
    
        private static List<Map<String,Object>> decisiveBalance(List<Map<String,Object>> rows){List<Map<String,Object>>out=new ArrayList<>();for(List<Map<String,Object>>g:group(rows,r->s(r.get("idSquadra"))).values()){Map<String,Object>f=g.get(0);int w=(int)g.stream().filter(r->"V".equals(s(r.get("esitoConRU")))).count(),d=(int)g.stream().filter(r->"N".equals(s(r.get("esitoConRU")))).count();out.add(linked("idSquadra",f.get("idSquadra"),"squadra",f.get("squadra"),"partiteRUDecisiva",g.size(),"vittorieGrazieRU",w,"pareggiGrazieRU",d,"puntiClassificaGuadagnati",g.stream().mapToInt(r->n(r.get("puntiClassificaGuadagnati"))).sum()));}out.sort(compareDesc("partiteRUDecisiva","puntiClassificaGuadagnati","squadra"));return out;}
        private static List<Map<String,Object>> decisiveAgainstBalance(List<Map<String,Object>> rows){List<Map<String,Object>>out=new ArrayList<>();for(List<Map<String,Object>>g:group(rows,r->s(r.get("idSquadra"))).values()){Map<String,Object>f=g.get(0);int w=(int)g.stream().filter(r->"V".equals(s(r.get("esitoSenzaRUAvversaria")))&&!"V".equals(s(r.get("esitoConRUAvversaria")))).count(),d=(int)g.stream().filter(r->"N".equals(s(r.get("esitoSenzaRUAvversaria")))&&"P".equals(s(r.get("esitoConRUAvversaria")))).count();out.add(linked("idSquadra",f.get("idSquadra"),"squadra",f.get("squadra"),"partiteControRUDecisiva",g.size(),"vittoriePerse",w,"pareggiDiventatiSconfitte",d,"puntiClassificaPersi",g.stream().mapToInt(r->n(r.get("puntiClassificaPersi"))).sum()));}out.sort(compareDesc("partiteControRUDecisiva","puntiClassificaPersi","squadra"));return out;}
    
        private static int goals(BigDecimal score,List<Map<String,Object>>bands){int g=0;List<Map<String,Object>>sorted=new ArrayList<>(bands);sorted.sort(Comparator.comparing(r->bd(r.get("min"))));for(Map<String,Object>b:sorted)if(score.compareTo(bd(b.get("min")))>=0)g=n(b.get("gol"));return g;}
        private static String result(int a,int b){return a>b?"V":a<b?"P":"N";} private static int points(String r){return "V".equals(r)?3:"N".equals(r)?1:0;}
        private static String effect(String a,String b){if("P".equals(a)&&"N".equals(b))return"Da sconfitta a pareggio";if("N".equals(a)&&"V".equals(b))return"Da pareggio a vittoria";if("P".equals(a)&&"V".equals(b))return"Da sconfitta a vittoria";return"";}
        private static String damage(String a,String b){if("V".equals(a)&&"N".equals(b))return"Da vittoria a pareggio";if("N".equals(a)&&"P".equals(b))return"Da pareggio a sconfitta";if("V".equals(a)&&"P".equals(b))return"Da vittoria a sconfitta";return"";}
    
        @SuppressWarnings("unchecked")
        private static void stripInternalFields(Object value){
            if(value instanceof Map<?,?> map){
                ((Map<String,Object>)map).keySet().removeIf(k->k.startsWith("_"));
                for(Object child:((Map<String,Object>)map).values())stripInternalFields(child);
            }else if(value instanceof List<?> list){for(Object child:list)stripInternalFields(child);}
        }
        private static String legacyOutcome(String value){return switch(value){case "V"->"V";case "P"->"N";case "S"->"P";default->value;};}
        private static String plainText(Object value){return plain(value).replace('.',',');}
        private static String publicCompetitionName(String name){return switch(name){case "Coppa di Lega Serie A"->"Coppa Serie A";case "Coppa di Lega Serie B"->"Coppa Serie B";case "Coppa di Lega Serie C"->"Coppa Serie C";default->name;};}
        private static String publicGroupName(String competition,Object groupName,Object idGirone){String name=s(groupName).trim();if(!name.isEmpty())return name;return competition;}
        private static List<String> canonicalCompetitions(){return List.of("Coppa Serie A","Coppa Serie B","Coppa Serie C","Coppa tra le Coppe","Europa Pipps","Play Off - Play Out","Serie A","Serie B","Serie C","Supercoppa Serie A","Supercoppa Serie B","Supercoppa Serie C");}
        private static List<Object> curiosityDefinitions(){return List.of();}
        private static List<Path> resolveSeasonDirs(Path root,List<String> requested)throws IOException{List<Path>out=new ArrayList<>();if(requested.isEmpty()){try(Stream<Path>s=Files.list(root)){s.filter(Files::isDirectory).sorted().forEach(out::add);}}else for(String x:requested){Path p=root.resolve(x);if(!Files.isDirectory(p))throw new IOException("Stagione non trovata: "+p);out.add(p);}return out;}
        private static List<Path> listNormalizedFiles(Path dir)throws IOException{try(Stream<Path>s=Files.list(dir)){return s.filter(Files::isRegularFile).filter(p->{String n=p.getFileName().toString();return n.startsWith("season_normalized_")&&n.endsWith(".json")&&!n.contains(".stage")&&!n.contains(".final");}).sorted().toList();}}
        private static <T>List<T>sorted(List<T>src,Comparator<? super T>c){List<T>o=new ArrayList<>(src);o.sort(c);return o;}
        private static Comparator<Map<String,Object>> compare(String...f){return (a,b)->{for(String x:f){int c=cmp(a.get(x),b.get(x));if(c!=0)return c;}return 0;};}
        private static Comparator<Map<String,Object>> compareDesc(String...f){return (a,b)->{for(String x:f){int c=cmp(b.get(x),a.get(x));if(c!=0)return c;}return 0;};}
        private static int cmp(Object a,Object b){if(a instanceof Number||b instanceof Number)return bd(a).compareTo(bd(b));return s(a).compareTo(s(b));}
        private static Map<String,List<Map<String,Object>>>group(List<Map<String,Object>>rows,java.util.function.Function<Map<String,Object>,String>key){Map<String,List<Map<String,Object>>>m=new LinkedHashMap<>();for(Map<String,Object>r:rows)m.computeIfAbsent(key.apply(r),k->new ArrayList<>()).add(r);return m;}
        private static Map<String,Object>copyFields(Map<String,Object>s,String...f){Map<String,Object>o=new LinkedHashMap<>();copyInto(o,s,f);return o;}private static void copyInto(Map<String,Object>o,Map<String,Object>s,String...f){for(String x:f)o.put(x,s.get(x));}
        private static Map<String,Object>linked(Object...v){Map<String,Object>m=new LinkedHashMap<>();for(int i=0;i<v.length;i+=2)m.put(String.valueOf(v[i]),v[i+1]);return m;}
        private static Object average(BigDecimal total,int count){return count==0?0:clean(total.divide(BigDecimal.valueOf(count),2,RoundingMode.HALF_EVEN));}
        private static Object percent(int x,int total){return total==0?0:clean(BigDecimal.valueOf(x*100L).divide(BigDecimal.valueOf(total),1,RoundingMode.HALF_UP));}
        private static Object clean(BigDecimal x){BigDecimal z=x.setScale(Math.min(2,Math.max(0,x.scale())),RoundingMode.HALF_UP).stripTrailingZeros();return z.scale()<=0?z.longValue():z;}
        private static String plain(Object x){return bd(x).stripTrailingZeros().toPlainString();}private static BigDecimal bd(Object x){if(x==null||s(x).isBlank())return BigDecimal.ZERO;return x instanceof BigDecimal b?b:new BigDecimal(s(x).replace(',','.'));}private static int n(Object x){return bd(x).intValue();}private static String s(Object x){return x==null?"":String.valueOf(x);}
        @SuppressWarnings("unchecked")private static Map<String,Object>object(Object x){return x instanceof Map<?,?>?(Map<String,Object>)x:new LinkedHashMap<>();}@SuppressWarnings("unchecked")private static List<Map<String,Object>>rows(Object x){if(!(x instanceof List<?>l))return new ArrayList<>();List<Map<String,Object>>o=new ArrayList<>();for(Object r:l)if(r instanceof Map<?,?>)o.add((Map<String,Object>)r);return o;}
    
        private static Object parse(Path source) throws IOException {
            String text = Files.readString(source, StandardCharsets.UTF_8);
            if (!text.isEmpty() && text.charAt(0) == '\uFEFF') text = text.substring(1);
            return new JsonParser(text, source).parse();
        }
    
        private static final class JsonParser {
            private final String text; private final Path source; private int index;
            JsonParser(String text, Path source) { this.text = text; this.source = source; }
            Object parse() throws IOException { skip(); Object v = value(); skip(); if (index != text.length()) fail("contenuto dopo JSON"); return v; }
            private Object value() throws IOException {
                skip(); if (index >= text.length()) fail("fine inattesa"); char c = text.charAt(index);
                return switch (c) { case '{' -> object(); case '[' -> array(); case '"' -> string(); case 't' -> literal("true", true); case 'f' -> literal("false", false); case 'n' -> literal("null", null); default -> number(); };
            }
            private Map<String,Object> object() throws IOException { expect('{'); Map<String,Object> m=new LinkedHashMap<>(); skip(); if (take('}')) return m; while(true){ skip(); String k=string(); skip(); expect(':'); m.put(k,value()); skip(); if(take('}')) return m; expect(','); } }
            private List<Object> array() throws IOException { expect('['); List<Object> a=new ArrayList<>(); skip(); if(take(']')) return a; while(true){ a.add(value()); skip(); if(take(']')) return a; expect(','); } }
            private String string() throws IOException { expect('"'); StringBuilder b=new StringBuilder(); while(index<text.length()){ char c=text.charAt(index++); if(c=='"') return b.toString(); if(c!='\\'){ b.append(c); continue;} if(index>=text.length()) fail("escape incompleto"); char e=text.charAt(index++); switch(e){case '"','\\','/'->b.append(e); case 'b'->b.append('\b'); case 'f'->b.append('\f'); case 'n'->b.append('\n'); case 'r'->b.append('\r'); case 't'->b.append('\t'); case 'u'->{ if(index+4>text.length()) fail("unicode incompleto"); b.append((char)Integer.parseInt(text.substring(index,index+4),16)); index+=4;} default->fail("escape non valido");}} fail("stringa non chiusa"); return ""; }
            private Object number() throws IOException { int s=index; if(peek('-')) index++; digits(); boolean dec=false; if(peek('.')){dec=true;index++;digits();} if(peek('e')||peek('E')){dec=true;index++;if(peek('+')||peek('-'))index++;digits();} String raw=text.substring(s,index); try{ BigDecimal d=new BigDecimal(raw); if(!dec && d.scale()<=0) try{return d.longValueExact();}catch(ArithmeticException ignored){} return d.stripTrailingZeros(); }catch(Exception e){fail("numero non valido"); return null;} }
            private void digits() throws IOException { int s=index; while(index<text.length()&&Character.isDigit(text.charAt(index))) index++; if(index==s) fail("cifre attese"); }
            private Object literal(String l,Object v)throws IOException{ if(!text.startsWith(l,index)) fail("letterale non valido"); index+=l.length(); return v; }
            private void skip(){ while(index<text.length()&&Character.isWhitespace(text.charAt(index))) index++; }
            private boolean take(char c){ if(index<text.length()&&text.charAt(index)==c){index++;return true;} return false; }
            private void expect(char c)throws IOException{ if(!take(c)) fail("atteso '"+c+"'"); }
            private boolean peek(char c){ return index<text.length()&&text.charAt(index)==c; }
            private void fail(String m)throws IOException{ throw new IOException("JSON non valido in "+source+" posizione "+index+": "+m); }
        }
    
        private static final class JsonWriter {
            static String writePretty(Object v){ StringBuilder b=new StringBuilder(); write(v,b,0); return b.toString(); }
            private static void write(Object v,StringBuilder b,int depth){
                if(v==null){b.append("null");return;} if(v instanceof String s){quote(s,b);return;} if(v instanceof Boolean){b.append(v);return;} if(v instanceof Number n){b.append(n instanceof BigDecimal d?d.stripTrailingZeros().toPlainString():n);return;}
                if(v instanceof Map<?,?> m){ b.append('{'); if(!m.isEmpty()){ b.append('\n'); int i=0; for(var e:m.entrySet()){ indent(b,depth+1); quote(String.valueOf(e.getKey()),b); b.append(": "); write(e.getValue(),b,depth+1); if(++i<m.size()) b.append(','); b.append('\n'); } indent(b,depth);} b.append('}'); return; }
                if(v instanceof List<?> l){ b.append('['); if(!l.isEmpty()){ b.append('\n'); for(int i=0;i<l.size();i++){ indent(b,depth+1); write(l.get(i),b,depth+1); if(i+1<l.size()) b.append(','); b.append('\n'); } indent(b,depth);} b.append(']'); return; }
                quote(String.valueOf(v),b);
            }
            private static void indent(StringBuilder b,int d){ b.append("  ".repeat(d)); }
            private static void quote(String s,StringBuilder b){ b.append('"'); for(int i=0;i<s.length();i++){ char c=s.charAt(i); switch(c){case '"'->b.append("\\\"");case '\\'->b.append("\\\\");case '\b'->b.append("\\b");case '\f'->b.append("\\f");case '\n'->b.append("\\n");case '\r'->b.append("\\r");case '\t'->b.append("\\t");default->{if(c<0x20)b.append(String.format("\\u%04x",(int)c));else b.append(c);}}} b.append('"'); }
        }
    
        public record Result(int seasons,int files,int reserveRows){}
    }

## src\main\java\it\alterlega\recordsnext\SeasonMappingConfigurator.java

File: src\main\java\it\alterlega\recordsnext\SeasonMappingConfigurator.java

    package it.alterlega.recordsnext;
    
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.sql.Connection;
    import java.sql.DriverManager;
    import java.sql.PreparedStatement;
    import java.sql.ResultSet;
    import java.sql.Statement;
    import java.time.Instant;
    import java.text.Normalizer;
    import java.util.ArrayList;
    import java.util.Comparator;
    import java.util.List;
    import java.util.Locale;
    
    /**
     * Configuratore transazionale delle associazioni storiche di squadre e
     * competizioni. Opera esclusivamente sul database SQLite gia popolato da
     * RawSqliteImporter e ConfigurationSchema.
     */
    public final class SeasonMappingConfigurator {
    
        private SeasonMappingConfigurator() {
        }
    
        public static void main(String[] args) throws Exception {
            if (args.length < 2) {
                printUsage();
                System.exit(2);
            }
    
            Path database = Path.of(args[0]).toAbsolutePath().normalize();
            if (!Files.isRegularFile(database)) {
                throw new IllegalArgumentException("Database non trovato: " + database);
            }
    
            Class.forName("org.sqlite.JDBC");
    
            try (Connection connection = DriverManager.getConnection(
                    "jdbc:sqlite:" + database)) {
    
                configureConnection(connection);
                requireSchema(connection);
    
                String command = args[1].trim().toLowerCase(Locale.ROOT);
                switch (command) {
                    case "show-seasons" -> showSeasons(connection, args);
                    case "pending" -> showPending(connection, args);
                    case "proposals" -> showProposals(connection, args);
                    case "validate" -> validateSeason(connection, args, true);
                    case "auto-exact" -> autoExact(connection, args);
                    case "associate-team" -> associateTeam(connection, args);
                    case "new-team" -> createTeamIdentity(connection, args);
                    case "associate-competition" -> associateCompetition(connection, args);
                    case "new-competition" -> createCompetitionIdentity(connection, args);
                    default -> {
                        printUsage();
                        System.exit(2);
                    }
                }
            }
        }
    
        private static void configureConnection(Connection connection) throws Exception {
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");
                statement.execute("PRAGMA busy_timeout = 10000");
            }
        }
    
        private static void requireSchema(Connection connection) throws Exception {
            String[] required = {
                "rn_season",
                "rn_source_file",
                "rn_competition_season",
                "rn_team_season",
                "rn_competition_identity",
                "rn_team_identity",
                "rn_competition_mapping",
                "rn_team_mapping"
            };
    
            for (String table : required) {
                try (PreparedStatement statement = connection.prepareStatement(
                        "SELECT COUNT(*) FROM sqlite_master "
                            + "WHERE type = 'table' AND name = ?")) {
                    statement.setString(1, table);
                    try (ResultSet result = statement.executeQuery()) {
                        result.next();
                        if (result.getInt(1) != 1) {
                            throw new IllegalStateException(
                                "Schema RecordsNext incompleto: tabella mancante " + table
                            );
                        }
                    }
                }
            }
        }
    
        private static void showSeasons(Connection connection, String[] args)
                throws Exception {
            requireArgCount(args, 2, "<db> show-seasons");
    
            String sql = """
                SELECT
                    s.season_id,
                    s.is_anchor,
                    (SELECT COUNT(*) FROM rn_source_file f
                     WHERE f.season_id = s.season_id AND f.source_type = 'FCM') AS fcm,
                    (SELECT COUNT(*) FROM rn_source_file f
                     WHERE f.season_id = s.season_id AND f.source_type = 'FCA') AS fca,
                    (SELECT COUNT(*)
                     FROM rn_competition_mapping cm
                     JOIN rn_competition_season cs
                       ON cs.competition_season_id = cm.competition_season_id
                     WHERE cs.season_id = s.season_id
                       AND cm.mapping_status = 'DA_CONFIGURARE') AS pending_comp,
                    (SELECT COUNT(*)
                     FROM rn_team_mapping tm
                     JOIN rn_team_season ts
                       ON ts.team_season_id = tm.team_season_id
                     WHERE ts.season_id = s.season_id
                       AND tm.mapping_status = 'DA_CONFIGURARE') AS pending_team
                FROM rn_season s
                ORDER BY COALESCE(s.sort_order, 0) DESC, s.season_id DESC
                """;
    
            System.out.printf(
                Locale.ROOT,
                "%-11s %-6s %3s %3s %6s %6s %-10s%n",
                "STAGIONE", "ANCORA", "FCM", "FCA", "COMP", "TEAM", "ESITO"
            );
    
            try (Statement statement = connection.createStatement();
                 ResultSet result = statement.executeQuery(sql)) {
                while (result.next()) {
                    int fcm = result.getInt("fcm");
                    int fca = result.getInt("fca");
                    int pendingComp = result.getInt("pending_comp");
                    int pendingTeam = result.getInt("pending_team");
                    String outcome = fcm == 1 && fca == 1
                        && pendingComp == 0 && pendingTeam == 0
                        ? "COMPLETA"
                        : "IN_CORSO";
    
                    System.out.printf(
                        Locale.ROOT,
                        "%-11s %-6s %3d %3d %6d %6d %-10s%n",
                        result.getString("season_id"),
                        result.getInt("is_anchor") == 1 ? "SI" : "NO",
                        fcm,
                        fca,
                        pendingComp,
                        pendingTeam,
                        outcome
                    );
                }
            }
        }
    
        private static void showPending(Connection connection, String[] args)
                throws Exception {
            requireArgCount(args, 3, "<db> pending <stagione>");
            String seasonId = requireSeason(connection, args[2]);
    
            System.out.println("COMPETIZIONI DA CONFIGURARE");
            printPendingCompetitions(connection, seasonId);
            System.out.println();
            System.out.println("SQUADRE DA CONFIGURARE");
            printPendingTeams(connection, seasonId);
        }
    
        private static void showProposals(Connection connection, String[] args)
                throws Exception {
            requireArgCount(args, 3, "<db> proposals <stagione>");
            String seasonId = requireSeason(connection, args[2]);
    
            System.out.println("COMPETIZIONI");
            printCompetitionProposals(connection, seasonId);
            System.out.println();
            System.out.println("SQUADRE");
            printTeamProposals(connection, seasonId);
        }
    
        private static void autoExact(Connection connection, String[] args)
                throws Exception {
            requireArgCount(args, 3, "<db> auto-exact <stagione>");
            String seasonId = requireSeason(connection, args[2]);
    
            runTransaction(connection, () -> {
                int competitions = applyUnambiguousExactCompetitionMappings(
                    connection, seasonId
                );
                int teams = applyUnambiguousExactTeamMappings(connection, seasonId);
                System.out.println("Associazioni esatte non ambigue applicate");
                System.out.println("Competizioni: " + competitions);
                System.out.println("Squadre     : " + teams);
            });
        }
    
        private static void associateTeam(Connection connection, String[] args)
                throws Exception {
            requireArgCount(
                args,
                4,
                "<db> associate-team <team-season-id> <team-identity-id>"
            );
            long teamSeasonId = parsePositiveLong(args[2], "team-season-id");
            long teamIdentityId = parsePositiveLong(args[3], "team-identity-id");
    
            runTransaction(connection, () -> {
                SeasonEntity team = requireTeamSeason(connection, teamSeasonId);
                Identity identity = requireTeamIdentity(connection, teamIdentityId);
                requireIdentityAvailableForTeam(
                    connection, team.seasonId(), teamSeasonId, teamIdentityId
                );
                updateTeamMapping(
                    connection,
                    teamSeasonId,
                    teamIdentityId,
                    "MANUAL",
                    null
                );
                System.out.println(
                    "Squadra associata: " + team.name() + " -> " + identity.name()
                );
            });
        }
    
        private static void createTeamIdentity(Connection connection, String[] args)
                throws Exception {
            requireArgCount(args, 3, "<db> new-team <team-season-id>");
            long teamSeasonId = parsePositiveLong(args[2], "team-season-id");
    
            runTransaction(connection, () -> {
                SeasonEntity team = requireTeamSeason(connection, teamSeasonId);
                requirePendingTeam(connection, teamSeasonId);
                long identityId = insertTeamIdentity(connection, team);
                updateTeamMapping(
                    connection,
                    teamSeasonId,
                    identityId,
                    "NEW_HISTORICAL_IDENTITY",
                    null
                );
                System.out.println(
                    "Nuova identita squadra: " + identityId + " | " + team.name()
                );
            });
        }
    
        private static void associateCompetition(Connection connection, String[] args)
                throws Exception {
            requireArgCount(
                args,
                4,
                "<db> associate-competition "
                    + "<competition-season-id> <competition-identity-id>"
            );
            long competitionSeasonId = parsePositiveLong(
                args[2], "competition-season-id"
            );
            long competitionIdentityId = parsePositiveLong(
                args[3], "competition-identity-id"
            );
    
            runTransaction(connection, () -> {
                SeasonEntity competition = requireCompetitionSeason(
                    connection, competitionSeasonId
                );
                Identity identity = requireCompetitionIdentity(
                    connection, competitionIdentityId
                );
                requireIdentityAvailableForCompetition(
                    connection,
                    competition.seasonId(),
                    competitionSeasonId,
                    competitionIdentityId
                );
                updateCompetitionMapping(
                    connection,
                    competitionSeasonId,
                    competitionIdentityId,
                    "MANUAL",
                    null
                );
                System.out.println(
                    "Competizione associata: " + competition.name()
                        + " -> " + identity.name()
                );
            });
        }
    
        private static void createCompetitionIdentity(
                Connection connection,
                String[] args) throws Exception {
            requireArgCount(
                args,
                3,
                "<db> new-competition <competition-season-id>"
            );
            long competitionSeasonId = parsePositiveLong(
                args[2], "competition-season-id"
            );
    
            runTransaction(connection, () -> {
                SeasonEntity competition = requireCompetitionSeason(
                    connection, competitionSeasonId
                );
                requirePendingCompetition(connection, competitionSeasonId);
                long identityId = insertCompetitionIdentity(connection, competition);
                updateCompetitionMapping(
                    connection,
                    competitionSeasonId,
                    identityId,
                    "NEW_HISTORICAL_IDENTITY",
                    null
                );
                System.out.println(
                    "Nuova identita competizione: " + identityId
                        + " | " + competition.name()
                );
            });
        }
    
        private static boolean validateSeason(
                Connection connection,
                String[] args,
                boolean print) throws Exception {
            requireArgCount(args, 3, "<db> validate <stagione>");
            String seasonId = requireSeason(connection, args[2]);
            Validation validation = validate(connection, seasonId);
    
            if (print) {
                System.out.println("Stagione       : " + seasonId);
                System.out.println("Sorgenti FCM   : " + validation.fcmSources());
                System.out.println("Sorgenti FCA   : " + validation.fcaSources());
                System.out.println("Comp. pendenti : " + validation.pendingCompetitions());
                System.out.println("Team pendenti  : " + validation.pendingTeams());
                System.out.println("Dup. competiz. : " + validation.duplicateCompetitions());
                System.out.println("Dup. squadre   : " + validation.duplicateTeams());
                System.out.println("Mapping orfani : " + validation.orphanMappings());
                System.out.println("ESITO          : "
                    + (validation.valid() ? "VALIDA" : "NON VALIDA"));
            }
    
            if (!validation.valid()) {
                throw new IllegalStateException(
                    "Configurazione stagione non valida: " + seasonId
                );
            }
            return true;
        }
    
        private static Validation validate(Connection connection, String seasonId)
                throws Exception {
            int fcm = count(connection, """
                SELECT COUNT(*) FROM rn_source_file
                WHERE season_id = ? AND source_type = 'FCM'
                """, seasonId);
            int fca = count(connection, """
                SELECT COUNT(*) FROM rn_source_file
                WHERE season_id = ? AND source_type = 'FCA'
                """, seasonId);
            int pendingCompetitions = count(connection, """
                SELECT COUNT(*)
                FROM rn_competition_mapping cm
                JOIN rn_competition_season cs
                  ON cs.competition_season_id = cm.competition_season_id
                WHERE cs.season_id = ?
                  AND cm.mapping_status = 'DA_CONFIGURARE'
                """, seasonId);
            int pendingTeams = count(connection, """
                SELECT COUNT(*)
                FROM rn_team_mapping tm
                JOIN rn_team_season ts
                  ON ts.team_season_id = tm.team_season_id
                WHERE ts.season_id = ?
                  AND tm.mapping_status = 'DA_CONFIGURARE'
                """, seasonId);
            int duplicateCompetitions = count(connection, """
                SELECT COUNT(*) FROM (
                    SELECT cm.competition_identity_id
                    FROM rn_competition_mapping cm
                    JOIN rn_competition_season cs
                      ON cs.competition_season_id = cm.competition_season_id
                    WHERE cs.season_id = ?
                      AND cm.mapping_status = 'ASSOCIATA'
                    GROUP BY cm.competition_identity_id
                    HAVING COUNT(*) > 1
                )
                """, seasonId);
            int duplicateTeams = count(connection, """
                SELECT COUNT(*) FROM (
                    SELECT tm.team_identity_id
                    FROM rn_team_mapping tm
                    JOIN rn_team_season ts
                      ON ts.team_season_id = tm.team_season_id
                    WHERE ts.season_id = ?
                      AND tm.mapping_status = 'ASSOCIATA'
                    GROUP BY tm.team_identity_id
                    HAVING COUNT(*) > 1
                )
                """, seasonId);
            int orphanMappings = count(connection, """
                SELECT
                    (SELECT COUNT(*)
                     FROM rn_competition_mapping cm
                     JOIN rn_competition_season cs
                       ON cs.competition_season_id = cm.competition_season_id
                     LEFT JOIN rn_competition_identity ci
                       ON ci.competition_identity_id = cm.competition_identity_id
                     WHERE cs.season_id = ?
                       AND cm.mapping_status = 'ASSOCIATA'
                       AND ci.competition_identity_id IS NULL)
                    +
                    (SELECT COUNT(*)
                     FROM rn_team_mapping tm
                     JOIN rn_team_season ts
                       ON ts.team_season_id = tm.team_season_id
                     LEFT JOIN rn_team_identity ti
                       ON ti.team_identity_id = tm.team_identity_id
                     WHERE ts.season_id = ?
                       AND tm.mapping_status = 'ASSOCIATA'
                       AND ti.team_identity_id IS NULL)
                """, seasonId, seasonId);
    
            return new Validation(
                fcm,
                fca,
                pendingCompetitions,
                pendingTeams,
                duplicateCompetitions,
                duplicateTeams,
                orphanMappings
            );
        }
    
        private static void printPendingCompetitions(
                Connection connection,
                String seasonId) throws Exception {
            String sql = """
                SELECT cs.competition_season_id, cs.source_competition_id, cs.source_name
                FROM rn_competition_season cs
                JOIN rn_competition_mapping cm
                  ON cm.competition_season_id = cs.competition_season_id
                WHERE cs.season_id = ?
                  AND cm.mapping_status = 'DA_CONFIGURARE'
                ORDER BY cs.source_name COLLATE NOCASE, cs.competition_season_id
                """;
            printPending(connection, sql, seasonId);
        }
    
        private static void printPendingTeams(
                Connection connection,
                String seasonId) throws Exception {
            String sql = """
                SELECT ts.team_season_id, ts.source_team_id, ts.source_name
                FROM rn_team_season ts
                JOIN rn_team_mapping tm
                  ON tm.team_season_id = ts.team_season_id
                WHERE ts.season_id = ?
                  AND tm.mapping_status = 'DA_CONFIGURARE'
                ORDER BY ts.source_name COLLATE NOCASE, ts.team_season_id
                """;
            printPending(connection, sql, seasonId);
        }
    
        private static void printPending(
                Connection connection,
                String sql,
                String seasonId) throws Exception {
            int rows = 0;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, seasonId);
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        rows++;
                        System.out.printf(
                            Locale.ROOT,
                            "%d | sorgente=%d | %s%n",
                            result.getLong(1),
                            result.getLong(2),
                            result.getString(3)
                        );
                    }
                }
            }
            if (rows == 0) {
                System.out.println("- nessuna -");
            }
        }
    
        private static void printCompetitionProposals(
                Connection connection,
                String seasonId) throws Exception {
            List<Identity> identities = readCompetitionIdentities(connection);
            String sql = """
                SELECT cs.competition_season_id, cs.source_name
                FROM rn_competition_season cs
                JOIN rn_competition_mapping cm
                  ON cm.competition_season_id = cs.competition_season_id
                WHERE cs.season_id = ?
                  AND cm.mapping_status = 'DA_CONFIGURARE'
                ORDER BY cs.source_name COLLATE NOCASE
                """;
            printProposals(connection, sql, seasonId, identities);
        }
    
        private static void printTeamProposals(
                Connection connection,
                String seasonId) throws Exception {
            List<Identity> identities = readTeamIdentities(connection);
            String sql = """
                SELECT ts.team_season_id, ts.source_name
                FROM rn_team_season ts
                JOIN rn_team_mapping tm
                  ON tm.team_season_id = ts.team_season_id
                WHERE ts.season_id = ?
                  AND tm.mapping_status = 'DA_CONFIGURARE'
                ORDER BY ts.source_name COLLATE NOCASE
                """;
            printProposals(connection, sql, seasonId, identities);
        }
    
        private static void printProposals(
                Connection connection,
                String sql,
                String seasonId,
                List<Identity> identities) throws Exception {
            int rows = 0;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, seasonId);
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        rows++;
                        long localId = result.getLong(1);
                        String localName = result.getString(2);
                        System.out.println(localId + " | " + localName);
    
                        identities.stream()
                            .map(identity -> new ScoredIdentity(
                                identity,
                                similarity(localName, identity.name())
                            ))
                            .sorted(
                                Comparator.comparingDouble(ScoredIdentity::score)
                                    .reversed()
                                    .thenComparingLong(value -> value.identity().id())
                            )
                            .limit(5)
                            .forEach(candidate -> System.out.printf(
                                Locale.ROOT,
                                "    %.3f | %d | %s%n",
                                candidate.score(),
                                candidate.identity().id(),
                                candidate.identity().name()
                            ));
                        System.out.println("    [NON GESTITA -> nuova identita storica]");
                    }
                }
            }
            if (rows == 0) {
                System.out.println("- nessuna -");
            }
        }
    
        private static int applyUnambiguousExactTeamMappings(
                Connection connection,
                String seasonId) throws Exception {
            List<ExactCandidate> candidates = new ArrayList<>();
            String sql = """
                SELECT
                    ts.team_season_id,
                    MIN(ti.team_identity_id) AS identity_id,
                    COUNT(*) AS candidate_count
                FROM rn_team_season ts
                JOIN rn_team_mapping tm
                  ON tm.team_season_id = ts.team_season_id
                JOIN rn_team_identity ti
                  ON LOWER(TRIM(ti.canonical_name)) = LOWER(TRIM(ts.source_name))
                WHERE ts.season_id = ?
                  AND tm.mapping_status = 'DA_CONFIGURARE'
                  AND NOT EXISTS (
                        SELECT 1
                        FROM rn_team_mapping used
                        JOIN rn_team_season used_ts
                          ON used_ts.team_season_id = used.team_season_id
                        WHERE used_ts.season_id = ts.season_id
                          AND used.mapping_status = 'ASSOCIATA'
                          AND used.team_identity_id = ti.team_identity_id
                    )
                GROUP BY ts.team_season_id
                HAVING COUNT(*) = 1
                """;
    
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, seasonId);
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        candidates.add(new ExactCandidate(
                            result.getLong("team_season_id"),
                            result.getLong("identity_id")
                        ));
                    }
                }
            }
    
            for (ExactCandidate candidate : candidates) {
                updateTeamMapping(
                    connection,
                    candidate.seasonEntityId(),
                    candidate.identityId(),
                    "EXACT_NAME",
                    null
                );
            }
            return candidates.size();
        }
    
        private static int applyUnambiguousExactCompetitionMappings(
                Connection connection,
                String seasonId) throws Exception {
            List<ExactCandidate> candidates = new ArrayList<>();
            String sql = """
                SELECT
                    cs.competition_season_id,
                    MIN(ci.competition_identity_id) AS identity_id,
                    COUNT(*) AS candidate_count
                FROM rn_competition_season cs
                JOIN rn_competition_mapping cm
                  ON cm.competition_season_id = cs.competition_season_id
                JOIN rn_competition_identity ci
                  ON LOWER(TRIM(ci.canonical_name)) = LOWER(TRIM(cs.source_name))
                WHERE cs.season_id = ?
                  AND cm.mapping_status = 'DA_CONFIGURARE'
                  AND NOT EXISTS (
                        SELECT 1
                        FROM rn_competition_mapping used
                        JOIN rn_competition_season used_cs
                          ON used_cs.competition_season_id = used.competition_season_id
                        WHERE used_cs.season_id = cs.season_id
                          AND used.mapping_status = 'ASSOCIATA'
                          AND used.competition_identity_id = ci.competition_identity_id
                    )
                GROUP BY cs.competition_season_id
                HAVING COUNT(*) = 1
                """;
    
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, seasonId);
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        candidates.add(new ExactCandidate(
                            result.getLong("competition_season_id"),
                            result.getLong("identity_id")
                        ));
                    }
                }
            }
    
            for (ExactCandidate candidate : candidates) {
                updateCompetitionMapping(
                    connection,
                    candidate.seasonEntityId(),
                    candidate.identityId(),
                    "EXACT_NAME",
                    null
                );
            }
            return candidates.size();
        }
    
        private static void requireIdentityAvailableForTeam(
                Connection connection,
                String seasonId,
                long teamSeasonId,
                long teamIdentityId) throws Exception {
            int used = count(connection, """
                SELECT COUNT(*)
                FROM rn_team_mapping tm
                JOIN rn_team_season ts
                  ON ts.team_season_id = tm.team_season_id
                WHERE ts.season_id = ?
                  AND tm.mapping_status = 'ASSOCIATA'
                  AND tm.team_identity_id = ?
                  AND tm.team_season_id <> ?
                """, seasonId, teamIdentityId, teamSeasonId);
            if (used != 0) {
                throw new IllegalStateException(
                    "Identita squadra gia usata nella stagione " + seasonId
                        + ": " + teamIdentityId
                );
            }
        }
    
        private static void requireIdentityAvailableForCompetition(
                Connection connection,
                String seasonId,
                long competitionSeasonId,
                long competitionIdentityId) throws Exception {
            int used = count(connection, """
                SELECT COUNT(*)
                FROM rn_competition_mapping cm
                JOIN rn_competition_season cs
                  ON cs.competition_season_id = cm.competition_season_id
                WHERE cs.season_id = ?
                  AND cm.mapping_status = 'ASSOCIATA'
                  AND cm.competition_identity_id = ?
                  AND cm.competition_season_id <> ?
                """, seasonId, competitionIdentityId, competitionSeasonId);
            if (used != 0) {
                throw new IllegalStateException(
                    "Identita competizione gia usata nella stagione " + seasonId
                        + ": " + competitionIdentityId
                );
            }
        }
    
        private static void requirePendingTeam(Connection connection, long id)
                throws Exception {
            int count = count(connection, """
                SELECT COUNT(*) FROM rn_team_mapping
                WHERE team_season_id = ? AND mapping_status = 'DA_CONFIGURARE'
                """, id);
            if (count != 1) {
                throw new IllegalStateException(
                    "La squadra stagionale non e DA_CONFIGURARE: " + id
                );
            }
        }
    
        private static void requirePendingCompetition(Connection connection, long id)
                throws Exception {
            int count = count(connection, """
                SELECT COUNT(*) FROM rn_competition_mapping
                WHERE competition_season_id = ? AND mapping_status = 'DA_CONFIGURARE'
                """, id);
            if (count != 1) {
                throw new IllegalStateException(
                    "La competizione stagionale non e DA_CONFIGURARE: " + id
                );
            }
        }
    
        private static long insertTeamIdentity(
                Connection connection,
                SeasonEntity team) throws Exception {
            String sql = """
                INSERT INTO rn_team_identity (
                    anchor_season_id,
                    anchor_team_season_id,
                    canonical_name,
                    created_at
                ) VALUES (?, ?, ?, ?)
                """;
            try (PreparedStatement statement = connection.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, team.seasonId());
                statement.setLong(2, team.id());
                statement.setString(3, team.name());
                statement.setString(4, Instant.now().toString());
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new IllegalStateException(
                            "Identita squadra non creata per " + team.id()
                        );
                    }
                    return keys.getLong(1);
                }
            }
        }
    
        private static long insertCompetitionIdentity(
                Connection connection,
                SeasonEntity competition) throws Exception {
            String sql = """
                INSERT INTO rn_competition_identity (
                    anchor_season_id,
                    anchor_competition_season_id,
                    canonical_name,
                    created_at
                ) VALUES (?, ?, ?, ?)
                """;
            try (PreparedStatement statement = connection.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, competition.seasonId());
                statement.setLong(2, competition.id());
                statement.setString(3, competition.name());
                statement.setString(4, Instant.now().toString());
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new IllegalStateException(
                            "Identita competizione non creata per " + competition.id()
                        );
                    }
                    return keys.getLong(1);
                }
            }
        }
    
        private static void updateTeamMapping(
                Connection connection,
                long teamSeasonId,
                long teamIdentityId,
                String method,
                String notes) throws Exception {
            try (PreparedStatement statement = connection.prepareStatement("""
                UPDATE rn_team_mapping
                SET team_identity_id = ?,
                    mapping_status = 'ASSOCIATA',
                    mapping_method = ?,
                    notes = ?,
                    updated_at = ?
                WHERE team_season_id = ?
                """)) {
                statement.setLong(1, teamIdentityId);
                statement.setString(2, method);
                statement.setString(3, notes);
                statement.setString(4, Instant.now().toString());
                statement.setLong(5, teamSeasonId);
                if (statement.executeUpdate() != 1) {
                    throw new IllegalStateException(
                        "Mapping squadra non trovato: " + teamSeasonId
                    );
                }
            }
        }
    
        private static void updateCompetitionMapping(
                Connection connection,
                long competitionSeasonId,
                long competitionIdentityId,
                String method,
                String notes) throws Exception {
            try (PreparedStatement statement = connection.prepareStatement("""
                UPDATE rn_competition_mapping
                SET competition_identity_id = ?,
                    mapping_status = 'ASSOCIATA',
                    mapping_method = ?,
                    notes = ?,
                    updated_at = ?
                WHERE competition_season_id = ?
                """)) {
                statement.setLong(1, competitionIdentityId);
                statement.setString(2, method);
                statement.setString(3, notes);
                statement.setString(4, Instant.now().toString());
                statement.setLong(5, competitionSeasonId);
                if (statement.executeUpdate() != 1) {
                    throw new IllegalStateException(
                        "Mapping competizione non trovato: " + competitionSeasonId
                    );
                }
            }
        }
    
        private static SeasonEntity requireTeamSeason(Connection connection, long id)
                throws Exception {
            return requireSeasonEntity(
                connection,
                "SELECT team_season_id, season_id, source_name "
                    + "FROM rn_team_season WHERE team_season_id = ?",
                id,
                "Squadra stagionale"
            );
        }
    
        private static SeasonEntity requireCompetitionSeason(
                Connection connection,
                long id) throws Exception {
            return requireSeasonEntity(
                connection,
                "SELECT competition_season_id, season_id, source_name "
                    + "FROM rn_competition_season WHERE competition_season_id = ?",
                id,
                "Competizione stagionale"
            );
        }
    
        private static SeasonEntity requireSeasonEntity(
                Connection connection,
                String sql,
                long id,
                String label) throws Exception {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, id);
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        throw new IllegalArgumentException(label + " non trovata: " + id);
                    }
                    return new SeasonEntity(
                        result.getLong(1),
                        result.getString(2),
                        result.getString(3)
                    );
                }
            }
        }
    
        private static Identity requireTeamIdentity(Connection connection, long id)
                throws Exception {
            return requireIdentity(
                connection,
                "SELECT team_identity_id, canonical_name "
                    + "FROM rn_team_identity WHERE team_identity_id = ?",
                id,
                "Identita squadra"
            );
        }
    
        private static Identity requireCompetitionIdentity(
                Connection connection,
                long id) throws Exception {
            return requireIdentity(
                connection,
                "SELECT competition_identity_id, canonical_name "
                    + "FROM rn_competition_identity WHERE competition_identity_id = ?",
                id,
                "Identita competizione"
            );
        }
    
        private static Identity requireIdentity(
                Connection connection,
                String sql,
                long id,
                String label) throws Exception {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, id);
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        throw new IllegalArgumentException(label + " non trovata: " + id);
                    }
                    return new Identity(result.getLong(1), result.getString(2));
                }
            }
        }
    
        private static List<Identity> readTeamIdentities(Connection connection)
                throws Exception {
            return readIdentities(
                connection,
                "SELECT team_identity_id, canonical_name "
                    + "FROM rn_team_identity ORDER BY canonical_name COLLATE NOCASE"
            );
        }
    
        private static List<Identity> readCompetitionIdentities(Connection connection)
                throws Exception {
            return readIdentities(
                connection,
                "SELECT competition_identity_id, canonical_name "
                    + "FROM rn_competition_identity ORDER BY canonical_name COLLATE NOCASE"
            );
        }
    
        private static List<Identity> readIdentities(
                Connection connection,
                String sql) throws Exception {
            List<Identity> identities = new ArrayList<>();
            try (Statement statement = connection.createStatement();
                 ResultSet result = statement.executeQuery(sql)) {
                while (result.next()) {
                    identities.add(new Identity(result.getLong(1), result.getString(2)));
                }
            }
            return identities;
        }
    
        private static String requireSeason(Connection connection, String raw)
                throws Exception {
            String seasonId = raw.trim();
            int count = count(
                connection,
                "SELECT COUNT(*) FROM rn_season WHERE season_id = ?",
                seasonId
            );
            if (count != 1) {
                throw new IllegalArgumentException("Stagione non trovata: " + seasonId);
            }
            return seasonId;
        }
    
        private static int count(
                Connection connection,
                String sql,
                Object... parameters) throws Exception {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (int i = 0; i < parameters.length; i++) {
                    statement.setObject(i + 1, parameters[i]);
                }
                try (ResultSet result = statement.executeQuery()) {
                    result.next();
                    return result.getInt(1);
                }
            }
        }
    
        private static long parsePositiveLong(String raw, String label) {
            try {
                long value = Long.parseLong(raw);
                if (value <= 0) {
                    throw new NumberFormatException("non positivo");
                }
                return value;
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException(
                    label + " non valido: " + raw,
                    exception
                );
            }
        }
    
        private static void requireArgCount(
                String[] args,
                int expected,
                String usage) {
            if (args.length != expected) {
                throw new IllegalArgumentException("Uso: " + usage);
            }
        }
    
        private static double similarity(String left, String right) {
            String a = normalize(left);
            String b = normalize(right);
            if (a.equals(b)) {
                return 1.0d;
            }
            int max = Math.max(a.length(), b.length());
            return max == 0 ? 1.0d : 1.0d - ((double) levenshtein(a, b) / max);
        }
    
        private static String normalize(String value) {
            return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim()
                .replaceAll("\\s+", " ");
        }
    
        private static int levenshtein(String left, String right) {
            int[] previous = new int[right.length() + 1];
            int[] current = new int[right.length() + 1];
            for (int j = 0; j <= right.length(); j++) {
                previous[j] = j;
            }
            for (int i = 1; i <= left.length(); i++) {
                current[0] = i;
                for (int j = 1; j <= right.length(); j++) {
                    int cost = left.charAt(i - 1) == right.charAt(j - 1) ? 0 : 1;
                    current[j] = Math.min(
                        Math.min(current[j - 1] + 1, previous[j] + 1),
                        previous[j - 1] + cost
                    );
                }
                int[] swap = previous;
                previous = current;
                current = swap;
            }
            return previous[right.length()];
        }
    
        private static void runTransaction(Connection connection, SqlAction action)
                throws Exception {
            boolean oldAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                action.run();
                connection.commit();
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(oldAutoCommit);
            }
        }
    
        private static void printUsage() {
            System.err.println("Comandi:");
            System.err.println("  <db> show-seasons");
            System.err.println("  <db> pending <stagione>");
            System.err.println("  <db> proposals <stagione>");
            System.err.println("  <db> validate <stagione>");
            System.err.println("  <db> auto-exact <stagione>");
            System.err.println(
                "  <db> associate-team <team-season-id> <team-identity-id>"
            );
            System.err.println("  <db> new-team <team-season-id>");
            System.err.println(
                "  <db> associate-competition "
                    + "<competition-season-id> <competition-identity-id>"
            );
            System.err.println("  <db> new-competition <competition-season-id>");
        }
    
        @FunctionalInterface
        private interface SqlAction {
            void run() throws Exception;
        }
    
        private record SeasonEntity(long id, String seasonId, String name) {
        }
    
        private record Identity(long id, String name) {
        }
    
        private record ScoredIdentity(Identity identity, double score) {
        }
    
        private record ExactCandidate(long seasonEntityId, long identityId) {
        }
    
        private record Validation(
            int fcmSources,
            int fcaSources,
            int pendingCompetitions,
            int pendingTeams,
            int duplicateCompetitions,
            int duplicateTeams,
            int orphanMappings
        ) {
            boolean valid() {
                return fcmSources == 1
                    && fcaSources == 1
                    && pendingCompetitions == 0
                    && pendingTeams == 0
                    && duplicateCompetitions == 0
                    && duplicateTeams == 0
                    && orphanMappings == 0;
            }
        }
    }

## src\main\java\it\alterlega\recordsnext\SeasonNormalizedBatchExporter.java

File: src\main\java\it\alterlega\recordsnext\SeasonNormalizedBatchExporter.java

    package it.alterlega.recordsnext;
    
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.sql.Connection;
    import java.sql.DriverManager;
    import java.sql.PreparedStatement;
    import java.sql.ResultSet;
    import java.text.Normalizer;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.Locale;
    
    public final class SeasonNormalizedBatchExporter {
    
        private SeasonNormalizedBatchExporter() {
        }
    
        public static void main(String[] args) throws Exception {
            if (args.length != 3) {
                System.err.println(
                    "Uso: SeasonNormalizedBatchExporter "
                        + "<recordsnext.db> <stagione> <project-dir>"
                );
                System.exit(2);
            }
    
            Path database = Path.of(args[0]).toAbsolutePath().normalize();
            String seasonId = args[1].trim();
            Path projectDir = Path.of(args[2]).toAbsolutePath().normalize();
            export(database, seasonId, projectDir);
        }
    
        public static void export(Path database, String seasonId, Path projectDir) throws Exception {
            Path outputDir = projectDir
                .resolve("data")
                .resolve("reports")
                .resolve(seasonId);
    
            Files.createDirectories(outputDir);
    
            Class.forName("org.sqlite.JDBC");
    
            List<String> competitions = readCompetitions(
                database,
                seasonId
            );
    
            if (competitions.isEmpty()) {
                throw new IllegalStateException(
                    "Nessuna competizione trovata per la stagione "
                        + seasonId
                );
            }
    
            int completed = 0;
            List<String> failures = new ArrayList<>();
    
            System.out.println(
                "Competizioni da esportare: "
                    + competitions.size()
            );
    
            for (String competition : competitions) {
                Path output = outputDir.resolve(
                    "season_normalized_"
                        + slug(competition)
                        + ".json"
                );
    
                System.out.println();
                System.out.println(
                    "=== " + competition + " ==="
                );
    
                try {
                    SeasonNormalizedExporter.main(
                        new String[] {
                            database.toString(),
                            seasonId,
                            competition,
                            projectDir.toString(),
                            output.toString()
                        }
                    );
    
                    completed++;
                } catch (Exception error) {
                    failures.add(
                        competition
                            + ": "
                            + error.getClass().getSimpleName()
                            + " - "
                            + error.getMessage()
                    );
    
                    error.printStackTrace(System.err);
                }
            }
    
            System.out.println();
            System.out.println("=== RIEPILOGO BATCH ===");
            System.out.println(
                "Stagione     : " + seasonId
            );
            System.out.println(
                "Competizioni : " + competitions.size()
            );
            System.out.println(
                "Completate   : " + completed
            );
            System.out.println(
                "Fallite      : " + failures.size()
            );
            System.out.println(
                "Output       : " + outputDir
            );
    
            if (!failures.isEmpty()) {
                System.out.println();
                System.out.println("Errori:");
                for (String failure : failures) System.out.println(" - " + failure);
                throw new IllegalStateException("Normalizzazione fallita per " + failures.size() + " competizioni: " + String.join("; ", failures));
            }
        }
    
        private static List<String> readCompetitions(
                Path database,
                String seasonId) throws Exception {
    
            String sql = """
                SELECT DISTINCT competition_name
                FROM rn_team_match
                WHERE season_id = ?
                  AND competition_name IS NOT NULL
                  AND TRIM(competition_name) <> ''
                ORDER BY competition_name COLLATE NOCASE
                """;
    
            List<String> competitions = new ArrayList<>();
    
            try (
                Connection connection =
                    DriverManager.getConnection(
                        "jdbc:sqlite:" + database
                    );
    
                PreparedStatement statement =
                    connection.prepareStatement(sql)
            ) {
                statement.setString(1, seasonId);
    
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        competitions.add(
                            result.getString("competition_name")
                        );
                    }
                }
            }
    
            return competitions;
        }
    
        private static String slug(String value) {
            String normalized = Normalizer.normalize(
                value,
                Normalizer.Form.NFD
            );
    
            return normalized
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        }
    }

## src\main\java\it\alterlega\recordsnext\SeasonNormalizedExporter.java

File: src\main\java\it\alterlega\recordsnext\SeasonNormalizedExporter.java

    package it.alterlega.recordsnext;
    
    import java.io.BufferedWriter;
    import java.math.BigDecimal;
    import java.nio.charset.StandardCharsets;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.sql.Connection;
    import java.sql.DriverManager;
    import java.sql.PreparedStatement;
    import java.sql.ResultSet;
    import java.time.Instant;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.Locale;
    
    public final class SeasonNormalizedExporter {
    
        private SeasonNormalizedExporter() {
        }
    
        public static void main(String[] args) throws Exception {
            if (args.length != 5) {
                System.err.println(
                    "Uso: SeasonNormalizedExporter "
                        + "<recordsnext.db> "
                        + "<stagione> "
                        + "<competizione-canonica> "
                        + "<project-dir> "
                        + "<output.json>"
                );
                System.exit(2);
            }
    
            Path database = Path.of(args[0])
                .toAbsolutePath()
                .normalize();
    
            String seasonId = args[1].trim();
            String competitionName = args[2].trim();
    
            Path projectDir = Path.of(args[3])
                .toAbsolutePath()
                .normalize();
    
            Path output = Path.of(args[4])
                .toAbsolutePath()
                .normalize();
    
            if (output.getParent() != null) {
                Files.createDirectories(output.getParent());
            }
    
            Class.forName("org.sqlite.JDBC");
    
            long started = System.nanoTime();
    
            try (Connection connection = DriverManager.getConnection(
                    "jdbc:sqlite:" + database)) {
    
                ExportData data = readExportData(
                    connection,
                    seasonId,
                    competitionName,
                    projectDir
                );
    
                writeJson(output, data);
    
                long finished = System.nanoTime();
    
                System.out.println("Normalized stage 1 completato");
                System.out.println("Stagione    : " + seasonId);
                System.out.println("Competizione: " + competitionName);
                System.out.println("Incontri    : " + data.meta().matchesAnalyzed());
                System.out.println("Righe squadra: " + data.teamMatches().size());
                System.out.println("Output      : " + output);
    
                System.out.printf(
                    Locale.ROOT,
                    "Tempo       : %.3f ms%n",
                    (finished - started) / 1_000_000.0
                );
            }
        }
    
        private static ExportData readExportData(
                Connection connection,
                String seasonId,
                String competitionName,
                Path projectDir) throws Exception {
    
            CompetitionInfo competition = readCompetition(
                connection,
                seasonId,
                competitionName
            );
    
            List<Integer> groupIds = readGroupIds(
                connection,
                seasonId,
                competition.identityId()
            );
    
            List<TeamMatch> teamMatches = readTeamMatches(
                connection,
                seasonId,
                competition.identityId(),
                competitionName
            );
    
            List<ExpulsionDetail> expulsionDetails =
                readExpulsionDetails(
                    connection,
                    seasonId,
                    competition.identityId()
                );
    
            List<EventDetail> eventDetails =
                readEventDetails(
                    connection,
                    seasonId,
                    competition.identityId()
                );
    
            List<ModifierDetail> modifierDetails =
                readModifierDetails(
                    connection,
                    seasonId,
                    competition.identityId()
                );
    
            List<CleanSheetDetail> cleanSheetDetails =
                readCleanSheetDetails(
                    connection,
                    seasonId,
                    competition.identityId()
                );
    
            List<ReserveOfficeDetail> reserveOfficeDetails =
                readReserveOfficeDetails(
                    connection,
                    seasonId,
                    competition.identityId()
                );
    
            List<GoalBandDetail> goalBandDetails =
                readGoalBandDetails(
                    connection,
                    seasonId,
                    competition.sourceCompetitionId()
                );
    
            int matchesAnalyzed = teamMatches.size() / 2;
    
            Meta meta = new Meta(
                Instant.now().toString(),
                projectDir.toString(),
                seasonId,
                outputHistoricalCompetitionId(competitionName),
                outputCompetitionName(competitionName),
                competition.sourceCompetitionId(),
                null,
                groupIds,
                "SQLite: " + connection.getMetaData().getURL(),
                "SQLite: " + connection.getMetaData().getURL(),
                matchesAnalyzed,
                teamMatches.size()
            );
    
            return new ExportData(
                meta,
                teamMatches,
                expulsionDetails,
                eventDetails,
                modifierDetails,
                cleanSheetDetails,
                reserveOfficeDetails,
                goalBandDetails
            );
        }
    
        private static CompetitionInfo readCompetition(
                Connection connection,
                String seasonId,
                String competitionName) throws Exception {
    
            String sql = """
                SELECT DISTINCT
                    competition_identity_id,
                    source_competition_id
                FROM rn_match
                WHERE season_id = ?
                  AND competition_name = ?
                """;
    
            try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {
    
                statement.setString(1, seasonId);
                statement.setString(2, competitionName);
    
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        throw new IllegalArgumentException(
                            "Competizione non trovata: "
                                + seasonId
                                + " / "
                                + competitionName
                        );
                    }
    
                    CompetitionInfo info = new CompetitionInfo(
                        result.getLong("competition_identity_id"),
                        result.getInt("source_competition_id")
                    );
    
                    if (result.next()) {
                        throw new IllegalStateException(
                            "PiÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¹ identitÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â  trovate per "
                                + seasonId
                                + " / "
                                + competitionName
                        );
                    }
    
                    return info;
                }
            }
        }
    
        private static List<Integer> readGroupIds(
                Connection connection,
                String seasonId,
                long competitionIdentityId) throws Exception {
    
            String sql = """
                SELECT DISTINCT source_group_id
                FROM rn_match
                WHERE season_id = ?
                  AND competition_identity_id = ?
                ORDER BY source_group_id
                """;
    
            List<Integer> values = new ArrayList<>();
    
            try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {
    
                statement.setString(1, seasonId);
                statement.setLong(2, competitionIdentityId);
    
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        values.add(
                            result.getInt("source_group_id")
                        );
                    }
                }
            }
    
            return values;
        }
    
        private static List<TeamMatch> readTeamMatches(
                Connection connection,
                String seasonId,
                long competitionIdentityId,
                String competitionName) throws Exception {
    
            String outputHistoricalId =
                outputHistoricalCompetitionId(competitionName);
    
            String outputCompetitionName =
                outputCompetitionName(competitionName);
    
            SourceInfo source = readFcmSource(
                connection,
                seasonId
            );
    
            String tabellinoTable = rawTable(
                connection,
                source.importId(),
                "TABELLINO"
            );
    
            String gironeTable = rawTable(
                connection,
                source.importId(),
                "GIRONE"
            );
    
            boolean calendarAvailable = tableExists(connection, "rn_matchday_date");
    
            String calendarColumns = calendarAvailable
                ? "md.match_date, md.match_time, md.match_datetime,"
                : "NULL AS match_date, NULL AS match_time, NULL AS match_datetime,";
    
            String calendarJoin = calendarAvailable
                ? "LEFT JOIN rn_matchday_date md "
                    + "ON md.season_id = e.season_id "
                    + "AND md.serie_a_round = e.serie_a_round"
                : "";
    
            String sql = """
                SELECT
                    e.season_id,
                    %s
                    e.competition_name,
                    e.source_competition_id,
                    e.source_group_id,
                    g.NOME AS source_group_name,
                    e.source_round_id,
                    e.round_description,
                    e.serie_a_round,
                    e.source_event_id,
                    e.event_type,
                    e.venue,
                    e.source_team_id,
                    e.team_name,
                    e.opponent_source_team_id,
                    e.opponent_name,
                    e.score_for,
                    e.score_against,
                    e.partial_score_for,
                    e.partial_score_against,
                    e.goals_for,
                    e.goals_against,
                    e.result,
    
                    CASE
                        WHEN e.event_type = 'REST' THEN 0
                        WHEN tf.IDINCONTRO IS NULL THEN e.goals_for
                        ELSE
                            CAST(COALESCE(tf.GOL, 0) AS INTEGER)
                            - CAST(COALESCE(tf.GOLSUPPLEMENTARI, 0) AS INTEGER)
                            - CAST(COALESCE(tf.GOLRIGORI, 0) AS INTEGER)
                    END AS regulation_goals_for,
    
                    CASE
                        WHEN e.event_type = 'REST' THEN 0
                        WHEN ta.IDINCONTRO IS NULL THEN e.goals_against
                        ELSE
                            CAST(COALESCE(ta.GOL, 0) AS INTEGER)
                            - CAST(COALESCE(ta.GOLSUPPLEMENTARI, 0) AS INTEGER)
                            - CAST(COALESCE(ta.GOLRIGORI, 0) AS INTEGER)
                    END AS regulation_goals_against,
    
                    CASE
                        WHEN e.event_type = 'REST' THEN 0
                        WHEN tf.IDINCONTRO IS NULL THEN 0
                        ELSE 1
                    END AS regulation_goals_found
    
                FROM rn_team_event e
    
                JOIN %s g
                  ON g.ID = e.source_group_id
    
                LEFT JOIN %s tf
                  ON e.event_type = 'HEAD_TO_HEAD'
                 AND tf.IDINCONTRO = e.source_event_id
                 AND tf.IDSQUADRA = e.source_team_id
    
                LEFT JOIN %s ta
                  ON e.event_type = 'HEAD_TO_HEAD'
                 AND ta.IDINCONTRO = e.source_event_id
                 AND ta.IDSQUADRA = e.opponent_source_team_id
    
                %s
    
                WHERE e.season_id = ?
                  AND e.competition_identity_id = ?
                  AND e.event_type IN ('HEAD_TO_HEAD', 'REST')
    
                ORDER BY
                    e.source_event_id,
                    CASE e.venue
                        WHEN 'HOME' THEN 0
                        WHEN 'AWAY' THEN 1
                        ELSE 0
                    END
                """.formatted(
                    calendarColumns,
                    quoteIdentifier(gironeTable),
                    quoteIdentifier(tabellinoTable),
                    quoteIdentifier(tabellinoTable),
                    calendarJoin
                );
    
            ScorecardBases scorecardBases = readScorecardBases(
                connection,
                seasonId
            );
    
            List<TeamMatch> rows = new ArrayList<>();
    
            try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {
    
                statement.setString(1, seasonId);
                statement.setLong(2, competitionIdentityId);
    
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        String eventType =
                            result.getString("event_type");
    
                        boolean rest =
                            "REST".equals(eventType);
    
                        String venue =
                            result.getString("venue");
    
                        String side;
    
                        if (rest) {
                            side = "casa";
                        } else {
                            side = switch (venue) {
                                case "HOME" -> "casa";
                                case "AWAY" -> "fuori";
                                default -> throw new IllegalStateException(
                                    "Lato non previsto: "
                                        + venue
                                        + " / evento "
                                        + result.getLong(
                                            "source_event_id"
                                        )
                                );
                            };
                        }
    
                        int goalsFor = rest
                            ? 0
                            : result.getInt("goals_for");
    
                        int goalsAgainst = rest
                            ? 0
                            : result.getInt("goals_against");
    
                        int regulationGoalsFor = rest
                            ? 0
                            : result.getInt(
                                "regulation_goals_for"
                            );
    
                        int regulationGoalsAgainst = rest
                            ? 0
                            : result.getInt(
                                "regulation_goals_against"
                            );
    
                        BigDecimal scoreFor = zeroIfNull(
                            result.getBigDecimal("score_for")
                        );
    
                        BigDecimal scoreAgainst = rest
                            ? BigDecimal.ZERO
                            : zeroIfNull(
                                result.getBigDecimal(
                                    "score_against"
                                )
                            );
    
                        BigDecimal partialFor = zeroIfNull(
                            result.getBigDecimal(
                                "partial_score_for"
                            )
                        );
    
                        BigDecimal partialAgainst = rest
                            ? BigDecimal.ZERO
                            : zeroIfNull(
                                result.getBigDecimal(
                                    "partial_score_against"
                                )
                            );
    
                        int opponentId = rest
                            ? 0
                            : result.getInt(
                                "opponent_source_team_id"
                            );
    
                        String opponentName = rest
                            ? ""
                            : emptyIfNull(
                                result.getString(
                                    "opponent_name"
                                )
                            );
    
                        int homeGoals;
                        int awayGoals;
                        int regulationHomeGoals;
                        int regulationAwayGoals;
                        BigDecimal homeScore;
                        BigDecimal awayScore;
    
                        if (rest || "HOME".equals(venue)) {
                            homeGoals = goalsFor;
                            awayGoals = goalsAgainst;
    
                            regulationHomeGoals =
                                regulationGoalsFor;
    
                            regulationAwayGoals =
                                regulationGoalsAgainst;
    
                            homeScore = scoreFor;
                            awayScore = scoreAgainst;
                        } else {
                            homeGoals = goalsAgainst;
                            awayGoals = goalsFor;
    
                            regulationHomeGoals =
                                regulationGoalsAgainst;
    
                            regulationAwayGoals =
                                regulationGoalsFor;
    
                            homeScore = scoreAgainst;
                            awayScore = scoreFor;
                        }
    
                        String resultCode;
    
                        if (rest) {
                            resultCode = "P";
                        } else {
                            resultCode = switch (
                                result.getString("result")
                            ) {
                                case "W" -> "V";
                                case "D" -> "P";
                                case "L" -> "S";
                                default -> throw new IllegalStateException(
                                    "Esito non previsto: "
                                        + result.getString(
                                            "result"
                                        )
                                );
                            };
                        }
    
                        int serieARound =
                            result.getInt("serie_a_round");
    
                        String regulationSource;
    
                        if (rest) {
                            regulationSource =
                                "GolCasa/GolFuori fallback";
                        } else if (
                            result.getInt(
                                "regulation_goals_found"
                            ) != 0
                        ) {
                            regulationSource =
                                "GolRegoCasa/GolRegoFuori";
                        } else {
                            regulationSource =
                                "GolCasa/GolFuori fallback";
                        }
    
                        rows.add(
                            new TeamMatch(
                                result.getString("season_id"),
                                outputHistoricalId,
                                outputCompetitionName,
                                result.getInt(
                                    "source_competition_id"
                                ),
                                null,
                                result.getInt(
                                    "source_group_id"
                                ),
                                result.getString(
                                    "source_group_name"
                                ),
                                result.getInt(
                                    "source_round_id"
                                ),
                                result.getString(
                                    "round_description"
                                ),
                                serieARound,
                                result.getString("match_date"),
                                result.getString("match_time"),
                                result.getString("match_datetime"),
                                result.getInt(
                                    "source_round_id"
                                ),
                                result.getLong(
                                    "source_event_id"
                                ),
                                scorecardUrl(result.getString("season_id"), serieARound),
                                scorecardBases.localUrl(serieARound),
                                scorecardBases.onlineUrl(serieARound),
                                side,
                                result.getInt(
                                    "source_team_id"
                                ),
                                result.getString(
                                    "team_name"
                                ),
                                opponentId,
                                opponentName,
                                scoreFor,
                                scoreAgainst,
                                partialFor,
                                partialAgainst,
                                goalsFor,
                                goalsAgainst,
                                regulationGoalsFor,
                                regulationGoalsAgainst,
                                regulationHomeGoals
                                    + "-"
                                    + regulationAwayGoals,
                                regulationSource,
                                resultCode,
                                homeGoals + "-" + awayGoals,
                                decimalText(homeScore)
                                    + "-"
                                    + decimalText(awayScore)
                            )
                        );
    
                        if (rest) {
                            rows.add(
                                new TeamMatch(
                                    result.getString("season_id"),
                                    outputHistoricalId,
                                    outputCompetitionName,
                                    result.getInt(
                                        "source_competition_id"
                                    ),
                                    null,
                                    result.getInt(
                                        "source_group_id"
                                    ),
                                    result.getString(
                                        "source_group_name"
                                    ),
                                    result.getInt(
                                        "source_round_id"
                                    ),
                                    result.getString(
                                        "round_description"
                                    ),
                                    serieARound,
                                result.getString("match_date"),
                                result.getString("match_time"),
                                result.getString("match_datetime"),
                                    result.getInt(
                                        "source_round_id"
                                    ),
                                    result.getLong(
                                        "source_event_id"
                                    ),
                                    scorecardUrl(result.getString("season_id"), serieARound),
                                    scorecardBases.localUrl(serieARound),
                                    scorecardBases.onlineUrl(serieARound),
                                    "fuori",
                                    0,
                                    "",
                                    result.getInt(
                                        "source_team_id"
                                    ),
                                    result.getString(
                                        "team_name"
                                    ),
                                    BigDecimal.ZERO,
                                    BigDecimal.ZERO,
                                    BigDecimal.ZERO,
                                    BigDecimal.ZERO,
                                    0,
                                    0,
                                    0,
                                    0,
                                    "0-0",
                                    "GolCasa/GolFuori fallback",
                                    "P",
                                    "0-0",
                                    "0-0"
                                )
                            );
                        }
                    }
                }
            }
    
            return rows;
        }
        private static List<ExpulsionDetail> readExpulsionDetails(
                Connection connection,
                String seasonId,
                long competitionIdentityId) throws Exception {
    
            SourceInfo fcmSource = readSource(
                connection,
                seasonId,
                "FCM"
            );
    
            SourceInfo fcaSource = readSource(
                connection,
                seasonId,
                "FCA"
            );
    
            String formazioneTable = rawTable(
                connection,
                fcmSource.importId(),
                "FORMAZIONE"
            );
    
            String giocaInTable = rawTable(
                connection,
                fcaSource.importId(),
                "GIOCAIN"
            );
    
            String punteggioTable = rawTable(
                connection,
                fcaSource.importId(),
                "PUNTEGGIO"
            );
    
            String giocatoreTable = rawTable(
                connection,
                fcaSource.importId(),
                "GIOCATOREA"
            );
    
            String sql = """
                SELECT
                    tm.source_match_id,
                    tm.serie_a_round,
                    tm.source_team_id,
                    tm.team_name,
                    f.IDGIOC AS player_id,
                    ga.NOME AS player_name,
                    gi.IDPUNTEGGIO AS score_id
    
                FROM rn_team_match tm
    
                JOIN %s f
                  ON f.IDINCONTRO = tm.source_match_id
                 AND f.IDSQUADRA = tm.source_team_id
    
                JOIN %s gi
                  ON gi.IDGIOCATORE = f.IDGIOC
                 AND gi.GIORNATA = tm.serie_a_round
    
                JOIN %s p
                  ON p.ID = gi.IDPUNTEGGIO
    
                JOIN %s ga
                  ON ga.ID = f.IDGIOC
    
                WHERE tm.season_id = ?
                  AND tm.competition_identity_id = ?
                  AND f.ENTRATO <> 0
                  AND p.ESP <> 0
    
                ORDER BY
                    tm.source_match_id,
                    f.IDGIOC
                """.formatted(
                    quoteIdentifier(formazioneTable),
                    quoteIdentifier(giocaInTable),
                    quoteIdentifier(punteggioTable),
                    quoteIdentifier(giocatoreTable)
                );
    
            List<ExpulsionDetail> rows = new ArrayList<>();
    
            try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {
    
                statement.setString(1, seasonId);
                statement.setLong(2, competitionIdentityId);
    
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        rows.add(
                            new ExpulsionDetail(
                                result.getLong("source_match_id"),
                                result.getInt("serie_a_round"),
                                result.getInt("source_team_id"),
                                result.getString("team_name"),
                                result.getInt("player_id"),
                                result.getString("player_name"),
                                result.getInt("score_id")
                            )
                        );
                    }
                }
            }
    
            return rows;
        }
    
        private static List<EventDetail> readEventDetails(
                Connection connection,
                String seasonId,
                long competitionIdentityId) throws Exception {
    
            SourceInfo fcmSource = readSource(
                connection,
                seasonId,
                "FCM"
            );
    
            SourceInfo fcaSource = readSource(
                connection,
                seasonId,
                "FCA"
            );
    
            String formazioneTable = rawTable(
                connection,
                fcmSource.importId(),
                "FORMAZIONE"
            );
    
            String giocaInTable = rawTable(
                connection,
                fcaSource.importId(),
                "GIOCAIN"
            );
    
            String punteggioTable = rawTable(
                connection,
                fcaSource.importId(),
                "PUNTEGGIO"
            );
    
            String giocatoreTable = rawTable(
                connection,
                fcaSource.importId(),
                "GIOCATOREA"
            );
    
            String sql = """
                SELECT
                    e.record_key,
                    e.event_type,
                    e.event_name,
                    e.source_field,
                    tm.source_match_id,
                    tm.serie_a_round,
                    tm.source_team_id,
                    tm.team_name,
                    f.IDGIOC AS player_id,
                    ga.NOME AS player_name,
                    gi.IDPUNTEGGIO AS score_id,
                    CASE e.event_number
                        WHEN 1 THEN p.AMM
                        WHEN 2 THEN p.ASSIST
                        WHEN 3 THEN p.GOLFATTISURIGORE1
                        WHEN 4 THEN p.RIGPAR
                        WHEN 5 THEN p.RIGSBA
                        WHEN 6 THEN p.AUTOGOL1
                    END AS event_value,
                    e.event_number
    
                FROM rn_team_match tm
    
                JOIN %s f
                  ON f.IDINCONTRO = tm.source_match_id
                 AND f.IDSQUADRA = tm.source_team_id
    
                JOIN %s gi
                  ON gi.IDGIOCATORE = f.IDGIOC
                 AND gi.GIORNATA = tm.serie_a_round
    
                JOIN %s p
                  ON p.ID = gi.IDPUNTEGGIO
    
                JOIN %s ga
                  ON ga.ID = f.IDGIOC
    
                CROSS JOIN (
                    SELECT
                        1 AS event_number,
                        'ammonizioniSquadre' AS record_key,
                        'ammonizione' AS event_type,
                        'Maggiori ammonizioni' AS event_name,
                        'Amm' AS source_field
    
                    UNION ALL
    
                    SELECT
                        2,
                        'assistSquadre',
                        'assist',
                        'Maggiori assist',
                        'Assist'
    
                    UNION ALL
    
                    SELECT
                        3,
                        'golRigoreSquadre',
                        'gol_su_rigore',
                        'Maggiori gol fatti su rigore',
                        'GolFattiSuRigore1'
    
                    UNION ALL
    
                    SELECT
                        4,
                        'rigoriParatiSquadre',
                        'rigore_parato',
                        'Maggiori rigori parati',
                        'RigPar'
    
                    UNION ALL
    
                    SELECT
                        5,
                        'rigoriSbagliatiSquadre',
                        'rigore_sbagliato',
                        'Maggiori rigori sbagliati',
                        'RigSba'
    
                    UNION ALL
    
                    SELECT
                        6,
                        'autogolSquadre',
                        'autogol',
                        'Maggiori autogol',
                        'Autogol1'
                ) e
    
                WHERE tm.season_id = ?
                  AND tm.competition_identity_id = ?
                  AND f.ENTRATO <> 0
                  AND CASE e.event_number
                        WHEN 1 THEN p.AMM
                        WHEN 2 THEN p.ASSIST
                        WHEN 3 THEN p.GOLFATTISURIGORE1
                        WHEN 4 THEN p.RIGPAR
                        WHEN 5 THEN p.RIGSBA
                        WHEN 6 THEN p.AUTOGOL1
                      END <> 0
    
                ORDER BY
                    tm.source_match_id,
                    f.rowid,
                    e.event_number
                """.formatted(
                    quoteIdentifier(formazioneTable),
                    quoteIdentifier(giocaInTable),
                    quoteIdentifier(punteggioTable),
                    quoteIdentifier(giocatoreTable)
                );
    
            List<EventDetail> rows = new ArrayList<>();
    
            try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {
    
                statement.setString(1, seasonId);
                statement.setLong(2, competitionIdentityId);
    
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        rows.add(
                            new EventDetail(
                                result.getString("record_key"),
                                result.getString("event_type"),
                                result.getString("event_name"),
                                result.getString("source_field"),
                                result.getLong("source_match_id"),
                                result.getInt("serie_a_round"),
                                result.getInt("source_team_id"),
                                result.getString("team_name"),
                                result.getInt("player_id"),
                                result.getString("player_name"),
                                result.getInt("score_id"),
                                result.getInt("event_value")
                            )
                        );
                    }
                }
            }
    
            return rows;
        }
    
        private static List<CleanSheetDetail> readCleanSheetDetails(
                Connection connection,
                String seasonId,
                long competitionIdentityId) throws Exception {
    
            SourceInfo fcmSource = readSource(
                connection,
                seasonId,
                "FCM"
            );
    
            SourceInfo fcaSource = readSource(
                connection,
                seasonId,
                "FCA"
            );
    
            String formazioneTable = rawTable(
                connection,
                fcmSource.importId(),
                "FORMAZIONE"
            );
    
            String giocaInTable = rawTable(
                connection,
                fcaSource.importId(),
                "GIOCAIN"
            );
    
            String punteggioTable = rawTable(
                connection,
                fcaSource.importId(),
                "PUNTEGGIO"
            );
    
            String giocatoreTable = rawTable(
                connection,
                fcaSource.importId(),
                "GIOCATOREA"
            );
    
            String sql = """
                SELECT
                    tm.source_match_id,
                    tm.serie_a_round,
                    tm.source_team_id,
                    tm.team_name,
                    tm.opponent_source_team_id,
                    tm.opponent_name,
                    f.IDGIOC AS player_id,
                    ga.NOME AS player_name,
                    gi.IDPUNTEGGIO AS score_id,
                    p.GOLSUBITI AS goals_conceded
    
                FROM rn_team_match tm
    
                JOIN %s f
                  ON f.IDINCONTRO = tm.source_match_id
                 AND f.IDSQUADRA = tm.source_team_id
    
                JOIN %s gi
                  ON gi.IDGIOCATORE = f.IDGIOC
                 AND gi.GIORNATA = tm.serie_a_round
    
                JOIN %s p
                  ON p.ID = gi.IDPUNTEGGIO
    
                JOIN %s ga
                  ON ga.ID = f.IDGIOC
    
                WHERE tm.season_id = ?
                  AND tm.competition_identity_id = ?
                  AND f.ENTRATO <> 0
                  AND ga.RUOLO = 1
                  AND p.GOLSUBITI = 0
    
                ORDER BY
                    f.IDGIOC,
                    tm.source_match_id
                """.formatted(
                    quoteIdentifier(formazioneTable),
                    quoteIdentifier(giocaInTable),
                    quoteIdentifier(punteggioTable),
                    quoteIdentifier(giocatoreTable)
                );
    
            List<CleanSheetDetail> rows = new ArrayList<>();
    
            try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {
    
                statement.setString(1, seasonId);
                statement.setLong(2, competitionIdentityId);
    
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        rows.add(
                            new CleanSheetDetail(
                                result.getLong("source_match_id"),
                                result.getInt("serie_a_round"),
                                result.getInt("source_team_id"),
                                result.getString("team_name"),
                                result.getInt("opponent_source_team_id"),
                                result.getString("opponent_name"),
                                result.getInt("player_id"),
                                result.getString("player_name"),
                                result.getInt("score_id"),
                                result.getInt("goals_conceded"),
                                new BigDecimal("0.5")
                            )
                        );
                    }
                }
            }
    
            return rows;
        }
    
        private static List<ModifierDetail> readModifierDetails(
                Connection connection,
                String seasonId,
                long competitionIdentityId) throws Exception {
    
            SourceInfo source = readFcmSource(
                connection,
                seasonId
            );
    
            String tabellinoTable = rawTable(
                connection,
                source.importId(),
                "TABELLINO"
            );
    
            String gironeTable = rawTable(
                connection,
                source.importId(),
                "GIRONE"
            );
    
            String sql = """
                SELECT
                    x.modifier_type,
                    x.source_field,
                    m.source_match_id,
                    m.serie_a_round,
                    tm.source_team_id,
                    tm.team_name,
                    tm.opponent_source_team_id,
                    tm.opponent_name,
    
                    CASE x.modifier_number
                        WHEN 1 THEN t.MODM1PERS
                        WHEN 2 THEN t.MODM2PERS
                    END AS modifier_value
    
                FROM %s t
    
                JOIN rn_match m
                  ON m.source_file_id = ?
                 AND m.source_match_id = t.IDINCONTRO
    
                JOIN rn_team_match tm
                  ON tm.source_file_id = m.source_file_id
                 AND tm.source_match_id = m.source_match_id
                 AND tm.source_team_id = t.IDSQUADRA
    
                CROSS JOIN (
                    SELECT
                        1 AS modifier_number,
                        'modDifesa' AS modifier_type,
                        'ModM1Pers' AS source_field
    
                    UNION ALL
    
                    SELECT
                        2 AS modifier_number,
                        'capitano' AS modifier_type,
                        'ModM2Pers' AS source_field
                ) x
    
                WHERE m.season_id = ?
                  AND m.competition_identity_id = ?
    
                  AND CASE x.modifier_number
                        WHEN 1 THEN t.MODM1PERSESISTE
                        WHEN 2 THEN t.MODM2PERSESISTE
                      END <> 0
    
                  AND CASE x.modifier_number
                        WHEN 1 THEN t.MODM1PERS
                        WHEN 2 THEN t.MODM2PERS
                      END <> 0
    
                ORDER BY
                    t.rowid,
                    x.modifier_number
                """.formatted(
                    quoteIdentifier(tabellinoTable)
                );
    
            List<ModifierDetail> rows = new ArrayList<>();
    
            try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {
    
                statement.setLong(1, source.sourceFileId());
                statement.setString(2, seasonId);
                statement.setLong(3, competitionIdentityId);
    
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        rows.add(
                            new ModifierDetail(
                                result.getString("modifier_type"),
                                result.getLong("source_match_id"),
                                result.getInt("serie_a_round"),
                                result.getInt("source_team_id"),
                                result.getString("team_name"),
                                result.getInt(
                                    "opponent_source_team_id"
                                ),
                                result.getString("opponent_name"),
                                result.getBigDecimal(
                                    "modifier_value"
                                ),
                                result.getString("source_field")
                            )
                        );
                    }
                }
            }
    
            return rows;
        }
    
        private static List<ReserveOfficeDetail> readReserveOfficeDetails(
                Connection connection,
                String seasonId,
                long competitionIdentityId) throws Exception {
    
            SourceInfo source = readFcmSource(connection, seasonId);
            String tabellinoTable = rawTable(connection, source.importId(), "TABELLINO");
    
            String sql = """
                SELECT
                    m.source_match_id,
                    m.serie_a_round,
                    tm.source_team_id,
                    tm.team_name,
                    tm.opponent_source_team_id,
                    tm.opponent_name,
                    t.LISTA,
                    t.RUOLO,
                    t.VOTO,
                    t.MODIF,
                    t.TOT
                FROM %s t
                JOIN rn_match m
                  ON m.source_file_id = ?
                 AND m.source_match_id = t.IDINCONTRO
                JOIN rn_team_match tm
                  ON tm.source_file_id = m.source_file_id
                 AND tm.source_match_id = m.source_match_id
                 AND tm.source_team_id = t.IDSQUADRA
                WHERE m.season_id = ?
                  AND m.competition_identity_id = ?
                ORDER BY t.rowid
                """.formatted(quoteIdentifier(tabellinoTable));
    
            List<ReserveOfficeDetail> rows = new ArrayList<>();
    
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, source.sourceFileId());
                statement.setString(2, seasonId);
                statement.setLong(3, competitionIdentityId);
    
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        String[] players = splitPercent(result.getString("LISTA"));
                        String[] roles = splitPercent(result.getString("RUOLO"));
                        String[] votes = splitPercent(result.getString("VOTO"));
                        String[] modifiers = splitPercent(result.getString("MODIF"));
                        String[] totals = splitPercent(result.getString("TOT"));
                        int max = Math.max(players.length,
                            Math.max(roles.length,
                            Math.max(votes.length,
                            Math.max(modifiers.length, totals.length))));
    
                        for (int index = 0; index < max; index++) {
                            if (!"-1".equals(item(players, index))) {
                                continue;
                            }
    
                            int role = parseInteger(item(roles, index));
                            rows.add(new ReserveOfficeDetail(
                                result.getLong("source_match_id"),
                                result.getInt("serie_a_round"),
                                result.getInt("source_team_id"),
                                result.getString("team_name"),
                                result.getInt("opponent_source_team_id"),
                                result.getString("opponent_name"),
                                roleCode(role),
                                roleName(role),
                                index + 1,
                                item(votes, index),
                                item(modifiers, index),
                                item(totals, index),
                                parseDecimal(item(totals, index))
                            ));
                        }
                    }
                }
            }
    
            return rows;
        }
    
        private static List<GoalBandDetail> readGoalBandDetails(
                Connection connection,
                String seasonId,
                int sourceCompetitionId) throws Exception {
    
            SourceInfo source = readFcmSource(connection, seasonId);
            String goalTable = rawTable(connection, source.importId(), "TABELLAGOL");
            String bandTable = rawTable(connection, source.importId(), "FASCIA");
    
            String sql = """
                SELECT
                    tg.IDCOMPETIZIONE AS source_competition_id,
                    tg.IDFASCIA AS source_band_id,
                    f.MIN AS min_score,
                    f.MAX AS max_score,
                    f.VALORE AS goals
                FROM %s tg
                JOIN %s f
                  ON f.ID = tg.IDFASCIA
                WHERE tg.IDCOMPETIZIONE = ?
                ORDER BY CAST(f.MIN AS REAL), CAST(f.VALORE AS INTEGER), tg.IDFASCIA
                """.formatted(
                    quoteIdentifier(goalTable),
                    quoteIdentifier(bandTable)
                );
    
            List<GoalBandDetail> rows = new ArrayList<>();
    
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, sourceCompetitionId);
    
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        rows.add(new GoalBandDetail(
                            result.getInt("source_competition_id"),
                            Integer.toString(result.getInt("source_band_id")),
                            zeroIfNull(result.getBigDecimal("min_score")),
                            zeroIfNull(result.getBigDecimal("max_score")),
                            result.getInt("goals")
                        ));
                    }
                }
            }
    
            if (rows.isEmpty()) {
                throw new IllegalStateException(
                    "Nessuna fascia gol trovata per "
                        + seasonId
                        + " / competizione FCM "
                        + sourceCompetitionId
                );
            }
    
            return rows;
        }
    
        private static String[] splitPercent(String value) {
            if (value == null || value.isBlank()) {
                return new String[0];
            }
            return value.split("%", -1);
        }
    
        private static String item(String[] values, int index) {
            return index >= 0 && index < values.length ? values[index].trim() : "";
        }
    
        private static int parseInteger(String value) {
            try {
                return Integer.parseInt(value.trim());
            } catch (Exception ignored) {
                return 0;
            }
        }
    
        private static BigDecimal parseDecimal(String value) {
            if (value == null || value.isBlank()) {
                return BigDecimal.ZERO;
            }
            try {
                return new BigDecimal(value.trim().replace(',', '.'));
            } catch (NumberFormatException ignored) {
                return BigDecimal.ZERO;
            }
        }
    
        private static String roleCode(int role) {
            return switch (role) {
                case 1 -> "PU";
                case 2 -> "DU";
                case 3 -> "CU";
                case 4 -> "AU";
                default -> "";
            };
        }
    
        private static String roleName(int role) {
            return switch (role) {
                case 1 -> "Portiere";
                case 2 -> "Difensore";
                case 3 -> "Centrocampista";
                case 4 -> "Attaccante";
                default -> "";
            };
        }
    
        private static SourceInfo readFcmSource(
                Connection connection,
                String seasonId) throws Exception {
    
            return readSource(
                connection,
                seasonId,
                "FCM"
            );
        }
    
        private static SourceInfo readSource(
                Connection connection,
                String seasonId,
                String sourceType) throws Exception {
    
            String sql = """
                SELECT
                    source_file_id,
                    import_id
                FROM rn_source_file
                WHERE season_id = ?
                  AND source_type = ?
                ORDER BY import_id DESC
                LIMIT 1
                """;
    
            try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {
    
                statement.setString(1, seasonId);
                statement.setString(2, sourceType);
    
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        throw new IllegalArgumentException(
                            "Sorgente "
                                + sourceType
                                + " non trovata: "
                                + seasonId
                        );
                    }
    
                    return new SourceInfo(
                        result.getLong("source_file_id"),
                        result.getLong("import_id")
                    );
                }
            }
        }
    
        private static String rawTable(
                Connection connection,
                long importId,
                String sourceTableName) throws Exception {
    
            String sql = """
                SELECT raw_table_name
                FROM rn_table_catalog
                WHERE import_id = ?
                  AND UPPER(source_table_name) = ?
                """;
    
            try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {
    
                statement.setLong(1, importId);
                statement.setString(
                    2,
                    sourceTableName.toUpperCase(Locale.ROOT)
                );
    
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        throw new IllegalStateException(
                            "Tabella raw non trovata: "
                                + sourceTableName
                        );
                    }
    
                    return result.getString("raw_table_name");
                }
            }
        }
    
        private static String quoteIdentifier(String value) {
            return "\""
                + value.replace("\"", "\"\"")
                + "\"";
        }
    
        private static void writeJson(
                Path output,
                ExportData data) throws Exception {
    
            try (BufferedWriter writer = Files.newBufferedWriter(
                    output,
                    StandardCharsets.UTF_8)) {
    
                writer.write("{\n");
    
                writeMeta(
                    writer,
                    data.meta()
                );
    
                writer.write(",\n");
    
                writeTeamMatches(
                    writer,
                    data.teamMatches()
                );
    
                writer.write(",\n");
    
                writeExpulsionDetails(
                    writer,
                    data.expulsionDetails()
                );
    
                writer.write(",\n");
    
                writeEventDetails(
                    writer,
                    data.eventDetails()
                );
    
                writer.write(",\n");
    
                writeModifierDetails(
                    writer,
                    data.modifierDetails()
                );
    
                writer.write(",\n");
    
                writeCleanSheetDetails(
                    writer,
                    data.cleanSheetDetails()
                );
    
                writer.write(",\n");
    
                writeReserveOfficeDetails(
                    writer,
                    data.reserveOfficeDetails()
                );
    
                writer.write(",\n");
    
                writeGoalBandDetails(
                    writer,
                    data.goalBandDetails()
                );
    
                writer.write("}\n");
            }
        }
    
        private static void writeMeta(
                BufferedWriter writer,
                Meta meta) throws Exception {
    
            writer.write("  \"meta\": {\n");
    
            writeStringProperty(
                writer,
                "generatedAt",
                meta.generatedAt(),
                true,
                4
            );
    
            writeStringProperty(
                writer,
                "projectDir",
                meta.projectDir(),
                true,
                4
            );
    
            writeStringProperty(
                writer,
                "stagione",
                meta.seasonId(),
                true,
                4
            );
    
            writeStringProperty(
                writer,
                "competizioneStoricaId",
                meta.historicalCompetitionId(),
                true,
                4
            );
    
            writeStringProperty(
                writer,
                "competizioneNome",
                meta.competitionName(),
                true,
                4
            );
    
            writeNumberProperty(
                writer,
                "idCompetizioneFcm",
                meta.sourceCompetitionId(),
                true,
                4
            );
    
            writeStringProperty(
                writer,
                "nomeCompetizioneDb",
                meta.databaseCompetitionName(),
                true,
                4
            );
    
            writer.write("    \"idGironiInclusi\": [");
    
            for (
                int index = 0;
                index < meta.groupIds().size();
                index++
            ) {
                if (index > 0) {
                    writer.write(", ");
                }
    
                writer.write(
                    Integer.toString(
                        meta.groupIds().get(index)
                    )
                );
            }
    
            writer.write("],\n");
    
            writeStringProperty(
                writer,
                "fcmTablesDir",
                meta.fcmSource(),
                true,
                4
            );
    
            writeStringProperty(
                writer,
                "fcaTablesDir",
                meta.fcaSource(),
                true,
                4
            );
    
            writeNumberProperty(
                writer,
                "incontriAnalizzati",
                meta.matchesAnalyzed(),
                true,
                4
            );
    
            writeNumberProperty(
                writer,
                "partiteSquadra",
                meta.teamMatches(),
                false,
                4
            );
    
            writer.write("  }");
        }
    
        private static void writeTeamMatches(
                BufferedWriter writer,
                List<TeamMatch> rows) throws Exception {
    
            writer.write("  \"partiteSquadra\": [\n");
    
            for (int index = 0; index < rows.size(); index++) {
                TeamMatch row = rows.get(index);
    
                writer.write("    {\n");
    
                writeStringProperty(
                    writer,
                    "stagione",
                    row.seasonId(),
                    true,
                    6
                );
    
                writeStringProperty(
                    writer,
                    "competizioneStoricaId",
                    row.historicalCompetitionId(),
                    true,
                    6
                );
    
                writeStringProperty(
                    writer,
                    "competizioneNome",
                    row.competitionName(),
                    true,
                    6
                );
    
                writeNumberProperty(
                    writer,
                    "idCompetizioneFcm",
                    row.sourceCompetitionId(),
                    true,
                    6
                );
    
                writeStringProperty(
                    writer,
                    "nomeCompetizioneDb",
                    row.databaseCompetitionName(),
                    true,
                    6
                );
    
                writeStringProperty(
                    writer,
                    "idGirone",
                    Integer.toString(row.groupId()),
                    true,
                    6
                );
    
                writeStringProperty(
                    writer,
                    "gironeNome",
                    row.groupName(),
                    true,
                    6
                );
    
                writeStringProperty(
                    writer,
                    "idGiornata",
                    Integer.toString(row.roundId()),
                    true,
                    6
                );
    
                writeStringProperty(
                    writer,
                    "giornata",
                    row.roundDescription(),
                    true,
                    6
                );
    
                writeNumberProperty(
                    writer,
                    "giornataDiA",
                    row.serieARound(),
                    true,
                    6
                );
    
                writeStringProperty(
                    writer,
                    "dataGiornata",
                    row.matchDate(),
                    true,
                    6
                );
    
                writeStringProperty(
                    writer,
                    "oraGiornata",
                    row.matchTime(),
                    true,
                    6
                );
    
                writeStringProperty(
                    writer,
                    "dataOraGiornata",
                    row.matchDateTime(),
                    true,
                    6
                );
    
                writeNumberProperty(
                    writer,
                    "ordineGiornata",
                    row.roundOrder(),
                    true,
                    6
                );
    
                writeStringProperty(
                    writer,
                    "idIncontro",
                    Long.toString(row.matchId()),
                    true,
                    6
                );
    
                writeStringProperty(
                    writer,
                    "urlTabellino",
                    row.scorecardUrl(),
                    true,
                    6
                );
    
                writeStringProperty(
                    writer,
                    "urlTabellinoLocale",
                    row.localScorecardUrl(),
                    true,
                    6
                );
    
                writeStringProperty(
                    writer,
                    "urlTabellinoOnline",
                    row.onlineScorecardUrl(),
                    true,
                    6
                );
    
                writeStringProperty(
                    writer,
                    "lato",
                    row.side(),
                    true,
                    6
                );
    
                writeStringProperty(
                    writer,
                    "idSquadra",
                    Integer.toString(row.teamId()),
                    true,
                    6
                );
    
                writeStringProperty(
                    writer,
                    "squadra",
                    row.teamName(),
                    true,
                    6
                );
    
                writeStringProperty(
                    writer,
                    "idAvversaria",
                    Integer.toString(row.opponentId()),
                    true,
                    6
                );
    
                writeStringProperty(
                    writer,
                    "avversaria",
                    row.opponentName(),
                    true,
                    6
                );
    
                writeDecimalProperty(
                    writer,
                    "puntiFatti",
                    row.scoreFor(),
                    true,
                    6
                );
    
                writeDecimalProperty(
                    writer,
                    "puntiSubiti",
                    row.scoreAgainst(),
                    true,
                    6
                );
    
                writeDecimalProperty(
                    writer,
                    "parzialeFatto",
                    row.partialFor(),
                    true,
                    6
                );
    
                writeDecimalProperty(
                    writer,
                    "parzialeSubito",
                    row.partialAgainst(),
                    true,
                    6
                );
    
                writeNumberProperty(
                    writer,
                    "golFatti",
                    row.goalsFor(),
                    true,
                    6
                );
    
                writeNumberProperty(
                    writer,
                    "golSubiti",
                    row.goalsAgainst(),
                    true,
                    6
                );
    
                writeNumberProperty(
                    writer,
                    "golRegolamentariFatti",
                    row.regulationGoalsFor(),
                    true,
                    6
                );
    
                writeNumberProperty(
                    writer,
                    "golRegolamentariSubiti",
                    row.regulationGoalsAgainst(),
                    true,
                    6
                );
    
                writeStringProperty(
                    writer,
                    "risultatoRegolamentari",
                    row.regulationResult(),
                    true,
                    6
                );
    
                writeStringProperty(
                    writer,
                    "fonteGolRegolamentari",
                    row.regulationGoalsSource(),
                    true,
                    6
                );
    
                writeStringProperty(
                    writer,
                    "esito",
                    row.resultCode(),
                    true,
                    6
                );
    
                writeStringProperty(
                    writer,
                    "risultato",
                    row.resultText(),
                    true,
                    6
                );
    
                writeStringProperty(
                    writer,
                    "punteggio",
                    row.scoreText(),
                    false,
                    6
                );
    
                writer.write("    }");
    
                if (index + 1 < rows.size()) {
                    writer.write(",");
                }
    
                writer.write("\n");
            }
    
            writer.write("  ]");
        }
    
        private static void writeExpulsionDetails(
                BufferedWriter writer,
                List<ExpulsionDetail> rows) throws Exception {
    
            writer.write("  \"espulsioniDettaglio\": [\n");
    
            for (int index = 0; index < rows.size(); index++) {
                ExpulsionDetail row = rows.get(index);
    
                writer.write("    {\n");
    
                writeStringProperty(
                    writer,
                    "idIncontro",
                    Long.toString(row.matchId()),
                    true,
                    6
                );
    
                writeNumberProperty(
                    writer,
                    "giornataDiA",
                    row.serieARound(),
                    true,
                    6
                );
    
                writeStringProperty(
                    writer,
                    "idSquadra",
                    Integer.toString(row.teamId()),
                    true,
                    6
                );
    
                writeStringProperty(
                    writer,
                    "squadra",
                    row.teamName(),
                    true,
                    6
                );
    
                writeStringProperty(
                    writer,
                    "idGiocatore",
                    Integer.toString(row.playerId()),
                    true,
                    6
                );
    
                writeStringProperty(
                    writer,
                    "giocatore",
                    row.playerName(),
                    true,
                    6
                );
    
                writeStringProperty(
                    writer,
                    "idPunteggio",
                    Integer.toString(row.scoreId()),
                    false,
                    6
                );
    
                writer.write("    }");
    
                if (index + 1 < rows.size()) {
                    writer.write(",");
                }
    
                writer.write("\n");
            }
    
            writer.write("  ]");
        }
    
        private static void writeEventDetails(
                BufferedWriter writer,
                List<EventDetail> rows) throws Exception {
    
            writer.write("  \"eventiSquadraDettaglio\": [\n");
    
            for (int index = 0; index < rows.size(); index++) {
                EventDetail row = rows.get(index);
    
                writer.write("    {\n");
    
                writeStringProperty(writer, "recordKey", row.recordKey(), true, 6);
                writeStringProperty(writer, "tipoEvento", row.eventType(), true, 6);
                writeStringProperty(writer, "nomeEvento", row.eventName(), true, 6);
                writeStringProperty(writer, "campoOrigine", row.sourceField(), true, 6);
                writeStringProperty(writer, "idIncontro", Long.toString(row.matchId()), true, 6);
                writeNumberProperty(writer, "giornataDiA", row.serieARound(), true, 6);
                writeStringProperty(writer, "idSquadra", Integer.toString(row.teamId()), true, 6);
                writeStringProperty(writer, "squadra", row.teamName(), true, 6);
                writeStringProperty(writer, "idGiocatore", Integer.toString(row.playerId()), true, 6);
                writeStringProperty(writer, "giocatore", row.playerName(), true, 6);
                writeStringProperty(writer, "idPunteggio", Integer.toString(row.scoreId()), true, 6);
                writeNumberProperty(writer, "valore", row.value(), false, 6);
    
                writer.write("    }");
    
                if (index + 1 < rows.size()) {
                    writer.write(",");
                }
    
                writer.write("\n");
            }
    
            writer.write("  ]");
        }
    
        private static void writeModifierDetails(
                BufferedWriter writer,
                List<ModifierDetail> rows) throws Exception {
    
            writer.write("  \"modificatoriB2Dettaglio\": [\n");
    
            for (int index = 0; index < rows.size(); index++) {
                ModifierDetail row = rows.get(index);
    
                writer.write("    {\n");
    
                writeStringProperty(
                    writer,
                    "tipo",
                    row.type(),
                    true,
                    6
                );
    
                writeStringProperty(
                    writer,
                    "idIncontro",
                    Long.toString(row.matchId()),
                    true,
                    6
                );
    
                writeNumberProperty(
                    writer,
                    "giornataDiA",
                    row.serieARound(),
                    true,
                    6
                );
    
                writeStringProperty(
                    writer,
                    "idSquadra",
                    Integer.toString(row.teamId()),
                    true,
                    6
                );
    
                writeStringProperty(
                    writer,
                    "squadra",
                    row.teamName(),
                    true,
                    6
                );
    
                writeStringProperty(
                    writer,
                    "idAvversaria",
                    Integer.toString(row.opponentId()),
                    true,
                    6
                );
    
                writeStringProperty(
                    writer,
                    "avversaria",
                    row.opponentName(),
                    true,
                    6
                );
    
                writeDecimalProperty(
                    writer,
                    "valore",
                    row.value(),
                    true,
                    6
                );
    
                writeStringProperty(
                    writer,
                    "campoOrigine",
                    row.sourceField(),
                    false,
                    6
                );
    
                writer.write("    }");
    
                if (index + 1 < rows.size()) {
                    writer.write(",");
                }
    
                writer.write("\n");
            }
    
            writer.write("  ]");
        }
    
        private static void writeCleanSheetDetails(
                BufferedWriter writer,
                List<CleanSheetDetail> rows) throws Exception {
    
            writer.write("  \"cleanSheetB3Dettaglio\": [\n");
    
            for (int index = 0; index < rows.size(); index++) {
                CleanSheetDetail row = rows.get(index);
    
                writer.write("    {\n");
    
                writeStringProperty(writer, "tipo", "cleanSheetPortiere", true, 6);
                writeStringProperty(writer, "idIncontro", Long.toString(row.matchId()), true, 6);
                writeNumberProperty(writer, "giornataDiA", row.serieARound(), true, 6);
                writeStringProperty(writer, "idSquadra", Integer.toString(row.teamId()), true, 6);
                writeStringProperty(writer, "squadra", row.teamName(), true, 6);
                writeStringProperty(writer, "idAvversaria", Integer.toString(row.opponentId()), true, 6);
                writeStringProperty(writer, "avversaria", row.opponentName(), true, 6);
                writeStringProperty(writer, "idGiocatore", Integer.toString(row.playerId()), true, 6);
                writeStringProperty(writer, "giocatore", row.playerName(), true, 6);
                writeStringProperty(writer, "idPunteggio", Integer.toString(row.scoreId()), true, 6);
                writeNumberProperty(writer, "golSubiti", row.goalsConceded(), true, 6);
                writeDecimalProperty(writer, "valore", row.value(), true, 6);
                writeStringProperty(
                    writer,
                    "campoOrigine",
                    "GiocatoreA.Ruolo=1 + Punteggio.GolSubiti=0",
                    false,
                    6
                );
    
                writer.write("    }");
    
                if (index + 1 < rows.size()) {
                    writer.write(",");
                }
    
                writer.write("\n");
            }
    
            writer.write("  ]");
        }
    
        private static void writeStringProperty(
                BufferedWriter writer,
                String name,
                String value,
                boolean comma,
                int indent) throws Exception {
    
            writer.write(" ".repeat(indent));
            writer.write("\"");
            writer.write(jsonEscape(name));
            writer.write("\": ");
    
            if (value == null) {
                writer.write("null");
            } else {
                writer.write("\"");
                writer.write(jsonEscape(value));
                writer.write("\"");
            }
    
            if (comma) {
                writer.write(",");
            }
    
            writer.write("\n");
        }
    
        private static void writeNumberProperty(
                BufferedWriter writer,
                String name,
                long value,
                boolean comma,
                int indent) throws Exception {
    
            writer.write(" ".repeat(indent));
            writer.write("\"");
            writer.write(jsonEscape(name));
            writer.write("\": ");
            writer.write(Long.toString(value));
    
            if (comma) {
                writer.write(",");
            }
    
            writer.write("\n");
        }
    
        private static void writeDecimalProperty(
                BufferedWriter writer,
                String name,
                BigDecimal value,
                boolean comma,
                int indent) throws Exception {
    
            writer.write(" ".repeat(indent));
            writer.write("\"");
            writer.write(jsonEscape(name));
            writer.write("\": ");
    
            if (value == null) {
                writer.write("null");
            } else {
                writer.write(decimalText(value));
            }
    
            if (comma) {
                writer.write(",");
            }
    
            writer.write("\n");
        }
    
        private static String decimalText(BigDecimal value) {
            if (value == null) {
                return "";
            }
    
            return value
                .stripTrailingZeros()
                .toPlainString();
        }
    
        private static BigDecimal zeroIfNull(
                BigDecimal value) {
    
            return value == null
                ? BigDecimal.ZERO
                : value;
        }
    
        private static String emptyIfNull(
                String value) {
    
            return value == null
                ? ""
                : value;
        }
    
        private static boolean tableExists(
                Connection connection,
                String tableName) throws Exception {
    
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT 1 FROM sqlite_master "
                        + "WHERE type = 'table' AND name = ?")) {
                statement.setString(1, tableName);
                try (ResultSet result = statement.executeQuery()) {
                    return result.next();
                }
            }
        }
    
        private static ScorecardBases readScorecardBases(
                Connection connection,
                String seasonId) throws Exception {
    
            if (!tableExists(connection, "rn_season_configuration")) {
                return new ScorecardBases(null, null);
            }
    
            try (PreparedStatement statement = connection.prepareStatement("""
                SELECT local_site_path, online_site_url
                FROM rn_season_configuration
                WHERE season_id = ?
                """)) {
                statement.setString(1, seasonId);
    
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        return new ScorecardBases(null, null);
                    }
    
                    String localPath = result.getString("local_site_path");
                    String onlineRoot = result.getString("online_site_url");
    
                    String localBase = null;
                    if (localPath != null && !localPath.isBlank()) {
                        Path fileName = Path.of(localPath).normalize().getFileName();
                        if (fileName != null && !fileName.toString().isBlank()) {
                            localBase = "../" + fileName + "/ris.htm?Gio=";
                        }
                    }
    
                    String onlineBase = null;
                    if (onlineRoot != null && !onlineRoot.isBlank()) {
                        onlineBase = onlineRoot.replaceAll("/+$", "")
                            + "/ris.htm?Gio=";
                    }
    
                    return new ScorecardBases(localBase, onlineBase);
                }
            }
        }
    
        private static String scorecardUrl(
                String seasonId,
                int serieARound) {
    
            String[] parts = seasonId.split("_", -1);
    
            if (parts.length != 2 || !parts[0].matches("\\d{4}")) {
                throw new IllegalArgumentException(
                    "Stagione non valida per URL tabellino: "
                        + seasonId
                );
            }
    
            return "../lega"
                + parts[0]
                + "/ris.htm?Gio="
                + serieARound;
        }
        private static String outputHistoricalCompetitionId(
                String competitionName) {
    
            return switch (competitionName) {
                case "Coppa Serie A" ->
                    "coppa_lega_serie_a";
    
                case "Coppa Serie B" ->
                    "coppa_lega_serie_b";
    
                case "Coppa Serie C" ->
                    "coppa_lega_serie_c";
    
                default ->
                    historicalCompetitionId(
                        competitionName
                    );
            };
        }
    
        private static String outputCompetitionName(
                String competitionName) {
    
            return switch (competitionName) {
                case "Coppa Serie A" ->
                    "Coppa di Lega Serie A";
    
                case "Coppa Serie B" ->
                    "Coppa di Lega Serie B";
    
                case "Coppa Serie C" ->
                    "Coppa di Lega Serie C";
    
                default -> competitionName;
            };
        }
    
        private static String historicalCompetitionId(
                String competitionName) {
    
            return competitionName
                .trim()
                .toLowerCase(Locale.ROOT)
                .replace('\u00e0', 'a')
                .replace('\u00e8', 'e')
                .replace('\u00e9', 'e')
                .replace('\u00ec', 'i')
                .replace('\u00f2', 'o')
                .replace('\u00f9', 'u')
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        }
    
        private static String jsonEscape(String value) {
            StringBuilder escaped = new StringBuilder();
    
            for (int index = 0; index < value.length(); index++) {
                char current = value.charAt(index);
    
                switch (current) {
                    case '"' -> escaped.append("\\\"");
                    case '\\' -> escaped.append("\\\\");
                    case '\b' -> escaped.append("\\b");
                    case '\f' -> escaped.append("\\f");
                    case '\n' -> escaped.append("\\n");
                    case '\r' -> escaped.append("\\r");
                    case '\t' -> escaped.append("\\t");
    
                    default -> {
                        if (current < 0x20) {
                            escaped.append(
                                String.format(
                                    Locale.ROOT,
                                    "\\u%04x",
                                    (int) current
                                )
                            );
                        } else {
                            escaped.append(current);
                        }
                    }
                }
            }
    
            return escaped.toString();
        }
    
        private record CompetitionInfo(
            long identityId,
            int sourceCompetitionId
        ) {
        }
    
        private record Meta(
            String generatedAt,
            String projectDir,
            String seasonId,
            String historicalCompetitionId,
            String competitionName,
            int sourceCompetitionId,
            String databaseCompetitionName,
            List<Integer> groupIds,
            String fcmSource,
            String fcaSource,
            int matchesAnalyzed,
            int teamMatches
        ) {
        }
    
        private record ScorecardBases(
            String localBase,
            String onlineBase
        ) {
            String localUrl(int serieARound) {
                return localBase == null ? null : localBase + serieARound;
            }
    
            String onlineUrl(int serieARound) {
                return onlineBase == null ? null : onlineBase + serieARound;
            }
        }
    
        private record TeamMatch(
            String seasonId,
            String historicalCompetitionId,
            String competitionName,
            int sourceCompetitionId,
            String databaseCompetitionName,
            int groupId,
            String groupName,
            int roundId,
            String roundDescription,
            int serieARound,
            String matchDate,
            String matchTime,
            String matchDateTime,
            int roundOrder,
            long matchId,
            String scorecardUrl,
            String localScorecardUrl,
            String onlineScorecardUrl,
            String side,
            int teamId,
            String teamName,
            int opponentId,
            String opponentName,
            BigDecimal scoreFor,
            BigDecimal scoreAgainst,
            BigDecimal partialFor,
            BigDecimal partialAgainst,
            int goalsFor,
            int goalsAgainst,
            int regulationGoalsFor,
            int regulationGoalsAgainst,
            String regulationResult,
            String regulationGoalsSource,
            String resultCode,
            String resultText,
            String scoreText
        ) {
        }
    
        private record SourceInfo(
            long sourceFileId,
            long importId
        ) {
        }
    
        private record ExpulsionDetail(
            long matchId,
            int serieARound,
            int teamId,
            String teamName,
            int playerId,
            String playerName,
            int scoreId
        ) {
        }
    
        private static void writeGoalBandDetails(
                BufferedWriter writer,
                List<GoalBandDetail> rows) throws Exception {
    
            writer.write("  \"fasceGolDettaglio\": [\n");
    
            for (int index = 0; index < rows.size(); index++) {
                GoalBandDetail row = rows.get(index);
                writer.write("    {\n");
                writeNumberProperty(writer, "idCompetizioneFcm", row.sourceCompetitionId(), true, 6);
                writeStringProperty(writer, "idFascia", row.sourceBandId(), true, 6);
                writeDecimalProperty(writer, "min", row.minScore(), true, 6);
                writeDecimalProperty(writer, "max", row.maxScore(), true, 6);
                writeNumberProperty(writer, "gol", row.goals(), false, 6);
                writer.write("    }");
                if (index + 1 < rows.size()) {
                    writer.write(",");
                }
                writer.write("\n");
            }
    
            writer.write("  ]");
        }
    
        private record GoalBandDetail(
            int sourceCompetitionId,
            String sourceBandId,
            BigDecimal minScore,
            BigDecimal maxScore,
            int goals
        ) {
        }
    
        private static void writeReserveOfficeDetails(
                BufferedWriter writer,
                List<ReserveOfficeDetail> rows) throws Exception {
    
            writer.write("  \"riserveUfficioDettaglio\": [\n");
    
            for (int index = 0; index < rows.size(); index++) {
                ReserveOfficeDetail row = rows.get(index);
                writer.write("    {\n");
                writeStringProperty(writer, "idIncontro", Long.toString(row.matchId()), true, 6);
                writeNumberProperty(writer, "giornataDiA", row.serieARound(), true, 6);
                writeStringProperty(writer, "idSquadra", Integer.toString(row.teamId()), true, 6);
                writeStringProperty(writer, "squadra", row.teamName(), true, 6);
                writeStringProperty(writer, "idAvversaria", Integer.toString(row.opponentId()), true, 6);
                writeStringProperty(writer, "avversaria", row.opponentName(), true, 6);
                writeStringProperty(writer, "tipoRU", row.roleCode(), true, 6);
                writeStringProperty(writer, "ruoloRU", row.roleName(), true, 6);
                writeNumberProperty(writer, "ordine", row.order(), true, 6);
                writeStringProperty(writer, "votoTabellino", row.vote(), true, 6);
                writeStringProperty(writer, "modifTabellino", row.modifier(), true, 6);
                writeStringProperty(writer, "totTabellino", row.total(), true, 6);
                writeDecimalProperty(writer, "valoreRU", row.value(), false, 6);
                writer.write("    }");
                if (index + 1 < rows.size()) {
                    writer.write(",");
                }
                writer.write("\n");
            }
    
            writer.write("  ]");
        }
    
        private record ReserveOfficeDetail(
            long matchId,
            int serieARound,
            int teamId,
            String teamName,
            int opponentId,
            String opponentName,
            String roleCode,
            String roleName,
            int order,
            String vote,
            String modifier,
            String total,
            BigDecimal value
        ) {
        }
    
        private record EventDetail(
            String recordKey,
            String eventType,
            String eventName,
            String sourceField,
            long matchId,
            int serieARound,
            int teamId,
            String teamName,
            int playerId,
            String playerName,
            int scoreId,
            int value
        ) {
        }
    
        private record CleanSheetDetail(
            long matchId,
            int serieARound,
            int teamId,
            String teamName,
            int opponentId,
            String opponentName,
            int playerId,
            String playerName,
            int scoreId,
            int goalsConceded,
            BigDecimal value
        ) {
        }
    
        private record ModifierDetail(
            String type,
            long matchId,
            int serieARound,
            int teamId,
            String teamName,
            int opponentId,
            String opponentName,
            BigDecimal value,
            String sourceField
        ) {
        }
    
        private record ExportData(
            Meta meta,
            List<TeamMatch> teamMatches,
            List<ExpulsionDetail> expulsionDetails,
            List<EventDetail> eventDetails,
            List<ModifierDetail> modifierDetails,
            List<CleanSheetDetail> cleanSheetDetails,
            List<ReserveOfficeDetail> reserveOfficeDetails,
            List<GoalBandDetail> goalBandDetails
        ) {
        }
    }

## src\main\java\it\alterlega\recordsnext\SeasonRecordsArchiveBuilder.java

File: src\main\java\it\alterlega\recordsnext\SeasonRecordsArchiveBuilder.java

    package it.alterlega.recordsnext;
    
    import java.io.IOException;
    import java.math.BigDecimal;
    import java.nio.charset.StandardCharsets;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.nio.file.StandardOpenOption;
    import java.util.ArrayList;
    import java.util.Comparator;
    import java.util.HashMap;
    import java.util.LinkedHashMap;
    import java.util.LinkedHashSet;
    import java.util.List;
    import java.util.Map;
    import java.util.Set;
    import java.util.stream.Stream;
    
    /**
     * Costruisce l'archivio season_records_*.json a partire dai JSON normalizzati
     * prodotti da RecordsNext.
     *
     * Genera tutte le 18 sezioni del contratto pubblico Records2026 usando i
     * dati normalizzati correnti, compresi i bonus capitano presenti in
     * modificatoriB2Dettaglio come tipo=capitano / campoOrigine=ModM2Pers.
     */
    public final class SeasonRecordsArchiveBuilder {
    
        private SeasonRecordsArchiveBuilder() {
        }
    
        public static void main(String[] args) throws Exception {
            if (args.length < 2) {
                System.err.println("Uso: SeasonRecordsArchiveBuilder <reportsRoot> <archiveRoot> [stagione ...]");
                System.exit(2);
            }
            Path reportsRoot = Path.of(args[0]).toAbsolutePath().normalize();
            Path archiveRoot = Path.of(args[1]).toAbsolutePath().normalize();
            List<String> seasons = new ArrayList<>();
            for (int i = 2; i < args.length; i++) {
                if (!args[i].isBlank()) seasons.add(args[i].trim());
            }
            Result result = build(reportsRoot, archiveRoot, seasons);
            System.out.println("Report       : " + reportsRoot);
            System.out.println("Archivio     : " + archiveRoot);
            System.out.println("Stagioni     : " + result.seasons());
            System.out.println("Competizioni : " + result.competitions());
            System.out.println("Sezioni      : 18/18");
        }
    
        public static Result build(Path reportsRoot, Path archiveRoot, List<String> requestedSeasons) throws IOException {
            if (!Files.isDirectory(reportsRoot)) {
                throw new IOException("Cartella report non trovata: " + reportsRoot);
            }
            Files.createDirectories(archiveRoot);
    
            List<Path> seasonDirs = resolveSeasonDirs(reportsRoot, requestedSeasons);
            int competitions = 0;
            int seasons = 0;
            for (Path seasonDir : seasonDirs) {
                List<Path> normalizedFiles = listNormalizedFiles(seasonDir);
                if (normalizedFiles.isEmpty()) continue;
                seasons++;
                Path outputSeason = archiveRoot.resolve(seasonDir.getFileName().toString());
                Files.createDirectories(outputSeason);
                for (Path normalizedFile : normalizedFiles) {
                    Map<String, Object> source = object(parse(normalizedFile), normalizedFile, "radice");
                    Map<String, Object> meta = object(source.get("meta"), normalizedFile, "meta");
                    String competitionId = string(meta.get("competizioneStoricaId"));
                    if (competitionId.isBlank()) {
                        String name = normalizedFile.getFileName().toString();
                        competitionId = name.substring("season_normalized_".length(), name.length() - ".json".length());
                    }
                    Map<String, Object> output = buildCompetition(source, meta);
                    Path target = outputSeason.resolve("season_records_" + competitionId + ".json");
                    String json = JsonWriter.writePretty(output) + System.lineSeparator();
                    Files.writeString(target, json, StandardCharsets.UTF_8,
                            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
                    competitions++;
                }
            }
            if (competitions == 0) throw new IOException("Nessun season_normalized_*.json trovato in " + reportsRoot);
            return new Result(seasons, competitions);
        }
    
        private static Map<String, Object> buildCompetition(Map<String, Object> source, Map<String, Object> sourceMeta) {
            List<Map<String, Object>> matches = rows(source.get("partiteSquadra"));
            List<Map<String, Object>> expulsions = rows(source.get("espulsioniDettaglio"));
            List<Map<String, Object>> events = rows(source.get("eventiSquadraDettaglio"));
            List<Map<String, Object>> modifiers = rows(source.get("modificatoriB2Dettaglio"));
            List<Map<String, Object>> cleanSheets = rows(source.get("cleanSheetB3Dettaglio"));
    
            Map<String, Object> records = new LinkedHashMap<>();
            records.put("puntiSquadraMax", pointsMax(matches));
            records.put("serieSenzaSconfitte", unbeatenSeries(matches));
            records.put("espulsioniSquadre", expulsionsByTeam(expulsions));
            records.put("espulsioniGiocatori", expulsionsByPlayer(expulsions));
            records.put("ammonizioniSquadre", eventByTeam(events, "ammonizioniSquadre", "Maggiori ammonizioni"));
            records.put("assistSquadre", eventByTeam(events, "assistSquadre", "Maggiori assist"));
            records.put("autogolSquadre", eventByTeam(events, "autogolSquadre", "Maggiori autogol"));
            records.put("rigoriSbagliatiSquadre", eventByTeam(events, "rigoriSbagliatiSquadre", "Maggiori rigori sbagliati"));
            records.put("rigoriParatiSquadre", eventByTeam(events, "rigoriParatiSquadre", "Maggiori rigori parati"));
            records.put("golRigoreSquadre", eventByTeam(events, "golRigoreSquadre", "Maggiori gol fatti su rigore"));
            records.put("modDifesaMax", modifierMax(modifiers));
            records.put("modDifesaTotaleSquadre", modifierTotal(modifiers));
            records.put("capitanoVolteSquadre", captainCount(modifiers));
            records.put("capitanoTotaleSquadre", captainTotal(modifiers));
            records.put("cleanSheetPortiereVolteSquadre", cleanSheetCount(cleanSheets));
            records.put("cleanSheetPortiereTotaleSquadre", cleanSheetTotal(cleanSheets));
            records.put("cleanSheetPortiereSerieSquadre", cleanSheetSeries(matches, cleanSheets));
            records.put("capitanoSerieSquadre", captainSeries(matches, modifiers));
    
            Map<String, Object> meta = new LinkedHashMap<>();
            meta.putAll(sourceMeta);
            meta.put("builder", "RecordsNext SeasonRecordsArchiveBuilder");
            meta.put("sezioniGenerate", 18);
            meta.put("sezioniAttese", 18);
            meta.put("sezioniNonDisponibili", List.of());
    
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("meta", meta);
            result.put("records", records);
            return result;
        }
    
        private static List<Object> pointsMax(List<Map<String, Object>> matches) {
            List<Map<String, Object>> sorted = new ArrayList<>(matches);
            sorted.sort(Comparator.comparingDouble((Map<String, Object> r) -> number(r.get("puntiFatti"))).reversed()
                    .thenComparing(r -> string(r.get("idIncontro"))));
            List<Object> out = new ArrayList<>();
            for (int i = 0; i < Math.min(20, sorted.size()); i++) {
                Map<String, Object> r = sorted.get(i);
                Map<String, Object> row = ordered(
                        "recordId", "punti_squadra_max",
                        "nome", "Maggior numero di punti fatti",
                        "stagione", r.get("stagione"),
                        "competizioneStoricaId", r.get("competizioneStoricaId"),
                        "competizioneNome", r.get("competizioneNome"),
                        "valore", r.get("puntiFatti"),
                        "squadra", r.get("squadra"),
                        "avversaria", r.get("avversaria"),
                        "idIncontro", r.get("idIncontro"),
                        "giornata", r.get("giornata"),
                        "giornataDiA", r.get("giornataDiA"),
                        "urlTabellino", r.get("urlTabellino"),
                        "risultato", r.get("risultato"),
                        "punteggio", r.get("punteggio"));
                row.put("dettagli", ordered(
                        "parzialeFatto", r.get("parzialeFatto"),
                        "parzialeSubito", r.get("parzialeSubito"),
                        "puntiSubiti", r.get("puntiSubiti"),
                        "golFatti", r.get("golFatti"),
                        "golSubiti", r.get("golSubiti"),
                        "golRegolamentariFatti", r.get("golRegolamentariFatti"),
                        "golRegolamentariSubiti", r.get("golRegolamentariSubiti"),
                        "risultatoRegolamentari", r.get("risultatoRegolamentari"),
                        "fonteGolRegolamentari", r.get("fonteGolRegolamentari")));
                out.add(row);
            }
            return out;
        }
    
        private static List<Object> unbeatenSeries(List<Map<String, Object>> matches) {
            Map<String, List<Map<String, Object>>> byTeam = group(matches, "idSquadra");
            List<Map<String, Object>> records = new ArrayList<>();
            for (List<Map<String, Object>> teamMatches : byTeam.values()) {
                teamMatches.sort(Comparator.comparingDouble((Map<String, Object> r) -> number(r.get("ordineGiornata")))
                        .thenComparing(r -> string(r.get("idIncontro"))));
                List<Map<String, Object>> current = new ArrayList<>();
                for (Map<String, Object> match : teamMatches) {
                    if (!"S".equals(string(match.get("esito")))) {
                        current.add(match);
                    } else {
                        addUnbeaten(records, current);
                        current = new ArrayList<>();
                    }
                }
                addUnbeaten(records, current);
            }
            records.sort(Comparator.comparingDouble((Map<String, Object> r) -> number(r.get("valore"))).reversed()
                    .thenComparing(Comparator.comparingDouble((Map<String, Object> r) -> number(r.get("vittorie"))).reversed())
                    .thenComparing(r -> string(r.get("squadra"))));
            return new ArrayList<>(records.subList(0, Math.min(20, records.size())));
        }
    
        private static void addUnbeaten(List<Map<String, Object>> records, List<Map<String, Object>> series) {
            if (series.isEmpty()) return;
            Map<String, Object> first = series.get(0), last = series.get(series.size() - 1);
            long wins = series.stream().filter(r -> "V".equals(string(r.get("esito")))).count();
            long draws = series.stream().filter(r -> "P".equals(string(r.get("esito")))).count();
            List<Object> details = new ArrayList<>();
            for (Map<String, Object> r : series) {
                details.add(ordered("idIncontro", r.get("idIncontro"), "giornata", r.get("giornata"),
                        "giornataDiA", r.get("giornataDiA"), "urlTabellino", r.get("urlTabellino"),
                        "avversaria", r.get("avversaria"), "risultato", r.get("risultato"),
                        "punteggio", r.get("punteggio"), "esito", r.get("esito")));
            }
            Map<String, Object> row = ordered(
                    "recordId", "serie_senza_sconfitte", "nome", "Partite consecutive senza sconfitte",
                    "stagione", first.get("stagione"), "competizioneStoricaId", first.get("competizioneStoricaId"),
                    "competizioneNome", first.get("competizioneNome"), "valore", series.size(),
                    "squadra", first.get("squadra"), "idSquadra", first.get("idSquadra"),
                    "daGiornata", first.get("giornata"), "aGiornata", last.get("giornata"),
                    "daGiornataDiA", first.get("giornataDiA"), "aGiornataDiA", last.get("giornataDiA"),
                    "vittorie", wins, "pareggi", draws);
            row.put("dettagli", details);
            records.add(row);
        }
    
        private static List<Object> expulsionsByTeam(List<Map<String, Object>> rows) {
            return aggregateCount(rows, "idSquadra", "squadra", "espulsioni_squadra",
                    "Maggiori espulsioni squadra", List.of("idIncontro", "giornataDiA", "idGiocatore", "giocatore"));
        }
    
        private static List<Object> expulsionsByPlayer(List<Map<String, Object>> rows) {
            return aggregateCount(rows, "idGiocatore", "giocatore", "espulsioni_giocatore",
                    "Maggiori espulsioni giocatore", List.of("idIncontro", "giornataDiA", "idSquadra", "squadra"));
        }
    
        private static List<Object> aggregateCount(List<Map<String, Object>> rows, String idField, String nameField,
                                                    String recordId, String name, List<String> detailFields) {
            Map<String, List<Map<String, Object>>> groups = group(rows, idField);
            List<Map<String, Object>> out = new ArrayList<>();
            for (List<Map<String, Object>> group : groups.values()) {
                Map<String, Object> first = group.get(0);
                Map<String, Object> item = ordered("recordId", recordId, "nome", name, "valore", group.size(),
                        idField, first.get(idField), nameField, first.get(nameField));
                List<Object> details = new ArrayList<>();
                for (Map<String, Object> r : group) details.add(project(r, detailFields));
                item.put("dettagli", details);
                out.add(item);
            }
            out.sort(Comparator.comparingDouble((Map<String, Object> r) -> number(r.get("valore"))).reversed()
                    .thenComparing(r -> string(r.get(nameField))));
            return new ArrayList<>(out);
        }
    
        private static List<Object> eventByTeam(List<Map<String, Object>> events, String key, String name) {
            List<Map<String, Object>> selected = events.stream().filter(r -> key.equals(string(r.get("recordKey")))).toList();
            Map<String, List<Map<String, Object>>> groups = group(selected, "idSquadra");
            List<Map<String, Object>> out = new ArrayList<>();
            for (List<Map<String, Object>> group : groups.values()) {
                group.sort(Comparator.comparingDouble((Map<String, Object> r) -> number(r.get("giornataDiA")))
                        .thenComparing(r -> string(r.get("idIncontro"))).thenComparing(r -> string(r.get("giocatore"))));
                Map<String, Object> first = group.get(0);
                double total = group.stream().mapToDouble(r -> number(r.get("valore"))).sum();
                Map<String, Object> item = ordered("recordId", key, "nome", name, "valore", cleanNumber(total),
                        "idSquadra", first.get("idSquadra"), "squadra", first.get("squadra"));
                List<Object> details = new ArrayList<>();
                for (Map<String, Object> r : group) {
                    details.add(ordered("giornataDiA", r.get("giornataDiA"), "idIncontro", r.get("idIncontro"),
                            "idGiocatore", r.get("idGiocatore"), "giocatore", r.get("giocatore"),
                            "valore", r.get("valore"), "campoOrigine", r.get("campoOrigine")));
                }
                item.put("dettagli", details);
                out.add(item);
            }
            out.sort(Comparator.comparingDouble((Map<String, Object> r) -> number(r.get("valore"))).reversed()
                    .thenComparing(r -> string(r.get("squadra"))));
            return new ArrayList<>(out);
        }
    
        private static List<Object> modifierMax(List<Map<String, Object>> modifiers) {
            List<Map<String, Object>> rows = modifiers.stream().filter(r -> "modDifesa".equals(string(r.get("tipo"))))
                    .sorted(Comparator.comparingDouble((Map<String, Object> r) -> number(r.get("valore"))).reversed()
                            .thenComparing(r -> string(r.get("squadra"))))
                    .limit(20).toList();
            List<Object> out = new ArrayList<>();
            for (Map<String, Object> r : rows) out.add(ordered("recordId", "modDifesaMax", "nome", "Miglior modificatore difesa",
                    "valore", r.get("valore"), "idSquadra", r.get("idSquadra"), "squadra", r.get("squadra"),
                    "avversaria", r.get("avversaria"), "idIncontro", r.get("idIncontro"), "giornataDiA", r.get("giornataDiA")));
            return out;
        }
    
        private static List<Object> modifierTotal(List<Map<String, Object>> modifiers) {
            List<Map<String, Object>> selected = modifiers.stream().filter(r -> "modDifesa".equals(string(r.get("tipo")))).toList();
            return aggregateSum(selected, "modDifesaTotaleSquadre", "Maggior totale modificatore difesa",
                    List.of("idIncontro", "giornataDiA", "avversaria", "valore"));
        }
    
        private static List<Object> captainCount(List<Map<String, Object>> modifiers) {
            List<Map<String, Object>> selected = modifiers.stream()
                    .filter(r -> "capitano".equals(string(r.get("tipo"))))
                    .toList();
            Map<String, List<Map<String, Object>>> groups = group(selected, "idSquadra");
            List<Map<String, Object>> out = new ArrayList<>();
            for (List<Map<String, Object>> group : groups.values()) {
                group.sort(Comparator.comparingDouble((Map<String, Object> r) -> number(r.get("giornataDiA")))
                        .thenComparing(r -> string(r.get("idIncontro"))));
                Map<String, Object> first = group.get(0);
                Map<String, Object> item = ordered(
                        "recordId", "capitanoVolteSquadre",
                        "nome", "Maggior numero bonus capitano",
                        "valore", group.size(),
                        "idSquadra", first.get("idSquadra"),
                        "squadra", first.get("squadra"));
                item.put("dettagli", modifierDetails(group));
                out.add(item);
            }
            sortValueTeam(out);
            return new ArrayList<>(out);
        }
    
        private static List<Object> captainTotal(List<Map<String, Object>> modifiers) {
            List<Map<String, Object>> selected = modifiers.stream()
                    .filter(r -> "capitano".equals(string(r.get("tipo"))))
                    .toList();
            return aggregateSum(selected, "capitanoTotaleSquadre",
                    "Maggior totale bonus capitano",
                    List.of("idIncontro", "giornataDiA", "avversaria", "valore"));
        }
    
        private static List<Object> modifierDetails(List<Map<String, Object>> group) {
            List<Object> details = new ArrayList<>();
            for (Map<String, Object> r : group) {
                details.add(ordered(
                        "idIncontro", r.get("idIncontro"),
                        "giornataDiA", r.get("giornataDiA"),
                        "avversaria", r.get("avversaria"),
                        "valore", r.get("valore")));
            }
            return details;
        }
    
        private static List<Object> captainSeries(List<Map<String, Object>> matches,
                                                   List<Map<String, Object>> modifiers) {
            List<Map<String, Object>> captain = modifiers.stream()
                    .filter(r -> "capitano".equals(string(r.get("tipo"))))
                    .toList();
            return eventSeries(matches, captain, "capitanoSerieSquadre",
                    "Maggior serie bonus capitano");
        }
    
        private static List<Object> cleanSheetCount(List<Map<String, Object>> clean) {
            Map<String, List<Map<String, Object>>> groups = group(clean, "idSquadra");
            List<Map<String, Object>> out = new ArrayList<>();
            for (List<Map<String, Object>> group : groups.values()) {
                Map<String, Object> first = group.get(0);
                Map<String, Object> item = ordered("recordId", "cleanSheetPortiereVolteSquadre",
                        "nome", "Maggior numero clean sheet portiere", "valore", group.size(),
                        "idSquadra", first.get("idSquadra"), "squadra", first.get("squadra"));
                item.put("dettagli", cleanDetails(group));
                out.add(item);
            }
            sortValueTeam(out);
            return new ArrayList<>(out);
        }
    
        private static List<Object> cleanSheetTotal(List<Map<String, Object>> clean) {
            Map<String, List<Map<String, Object>>> groups = group(clean, "idSquadra");
            List<Map<String, Object>> out = new ArrayList<>();
            for (List<Map<String, Object>> group : groups.values()) {
                Map<String, Object> first = group.get(0);
                double total = group.stream().mapToDouble(r -> number(r.get("valore"))).sum();
                Map<String, Object> item = ordered("recordId", "cleanSheetPortiereTotaleSquadre",
                        "nome", "Maggior totale bonus clean sheet portiere", "valore", cleanNumber(total),
                        "idSquadra", first.get("idSquadra"), "squadra", first.get("squadra"));
                item.put("dettagli", cleanDetails(group));
                out.add(item);
            }
            sortValueTeam(out);
            return new ArrayList<>(out);
        }
    
        private static List<Object> cleanDetails(List<Map<String, Object>> group) {
            List<Map<String, Object>> sorted = new ArrayList<>(group);
            sorted.sort(Comparator.comparingDouble((Map<String, Object> r) -> number(r.get("giornataDiA")))
                    .thenComparing(r -> string(r.get("idIncontro"))));
            List<Object> details = new ArrayList<>();
            for (Map<String, Object> r : sorted) details.add(ordered("idIncontro", r.get("idIncontro"),
                    "giornataDiA", r.get("giornataDiA"), "avversaria", r.get("avversaria"),
                    "giocatore", r.get("giocatore"), "valore", r.get("valore")));
            return details;
        }
    
        private static List<Object> cleanSheetSeries(List<Map<String, Object>> matches, List<Map<String, Object>> clean) {
            return eventSeries(matches, clean, "cleanSheetPortiereSerieSquadre",
                    "Maggior serie clean sheet portiere");
        }
    
        private static List<Object> eventSeries(List<Map<String, Object>> matches,
                                                List<Map<String, Object>> events,
                                                String recordId, String name) {
            Set<String> eventKeys = new LinkedHashSet<>();
            for (Map<String, Object> r : events) {
                eventKeys.add(string(r.get("idSquadra")) + "|" + string(r.get("idIncontro")));
            }
    
            Map<String, List<Map<String, Object>>> byTeam = group(matches, "idSquadra");
            List<Map<String, Object>> out = new ArrayList<>();
    
            for (List<Map<String, Object>> teamMatches : byTeam.values()) {
                teamMatches.sort(Comparator
                        .comparingDouble((Map<String, Object> r) -> number(r.get("giornataDiA")))
                        .thenComparing(r -> string(r.get("idIncontro"))));
    
                List<Map<String, Object>> best = new ArrayList<>();
                List<Map<String, Object>> current = new ArrayList<>();
    
                for (Map<String, Object> match : teamMatches) {
                    String key = string(match.get("idSquadra")) + "|" + string(match.get("idIncontro"));
                    if (eventKeys.contains(key)) {
                        current.add(match);
                    } else {
                        if (current.size() > best.size()) best = new ArrayList<>(current);
                        current.clear();
                    }
                }
                if (current.size() > best.size()) best = new ArrayList<>(current);
    
                if (!best.isEmpty()) out.add(eventSeriesRecord(best, recordId, name));
            }
    
            sortValueTeam(out);
            return new ArrayList<>(out);
        }
    
        private static Map<String, Object> eventSeriesRecord(List<Map<String, Object>> series,
                                                              String recordId, String name) {
            Map<String, Object> first = series.get(0);
            Map<String, Object> last = series.get(series.size() - 1);
            return ordered(
                    "recordId", recordId,
                    "nome", name,
                    "valore", series.size(),
                    "idSquadra", first.get("idSquadra"),
                    "squadra", first.get("squadra"),
                    "daGiornataDiA", first.get("giornataDiA"),
                    "aGiornataDiA", last.get("giornataDiA"),
                    "dettagli", series.stream().map(r -> ordered(
                            "idIncontro", r.get("idIncontro"),
                            "giornataDiA", r.get("giornataDiA"),
                            "avversaria", r.get("avversaria"))).toList());
        }
    
        private static List<Object> aggregateSum(List<Map<String, Object>> rows, String recordId, String name,
                                                  List<String> detailFields) {
            Map<String, List<Map<String, Object>>> groups = group(rows, "idSquadra");
            List<Map<String, Object>> out = new ArrayList<>();
            for (List<Map<String, Object>> group : groups.values()) {
                group.sort(Comparator.comparingDouble((Map<String, Object> r) -> number(r.get("giornataDiA")))
                        .thenComparing(r -> string(r.get("idIncontro"))));
                Map<String, Object> first = group.get(0);
                double total = group.stream().mapToDouble(r -> number(r.get("valore"))).sum();
                Map<String, Object> item = ordered("recordId", recordId, "nome", name, "valore", cleanNumber(total),
                        "idSquadra", first.get("idSquadra"), "squadra", first.get("squadra"));
                List<Object> details = new ArrayList<>();
                for (Map<String, Object> r : group) details.add(project(r, detailFields));
                item.put("dettagli", details);
                out.add(item);
            }
            sortValueTeam(out);
            return new ArrayList<>(out);
        }
    
        private static void sortValueTeam(List<Map<String, Object>> out) {
            out.sort(Comparator.comparingDouble((Map<String, Object> r) -> number(r.get("valore"))).reversed()
                    .thenComparing(r -> string(r.get("squadra"))));
        }
    
        private static Map<String, List<Map<String, Object>>> group(List<Map<String, Object>> rows, String field) {
            Map<String, List<Map<String, Object>>> groups = new LinkedHashMap<>();
            for (Map<String, Object> row : rows) groups.computeIfAbsent(string(row.get(field)), k -> new ArrayList<>()).add(row);
            return groups;
        }
    
        private static Map<String, Object> project(Map<String, Object> row, List<String> fields) {
            Map<String, Object> result = new LinkedHashMap<>();
            for (String field : fields) result.put(field, row.get(field));
            return result;
        }
    
        private static Map<String, Object> ordered(Object... values) {
            Map<String, Object> map = new LinkedHashMap<>();
            for (int i = 0; i < values.length; i += 2) map.put(String.valueOf(values[i]), values[i + 1]);
            return map;
        }
    
        private static long longNumber(Object value) {
            String text = string(value);
            if (text.isBlank()) return 0L;
            try {
                return new BigDecimal(text.replace(',', '.')).longValue();
            }
            catch (NumberFormatException ex) {
                return 0L;
            }
        }
    
        private static Object cleanNumber(double value) {
            if (Math.rint(value) == value) return (long) value;
            return BigDecimal.valueOf(value).stripTrailingZeros();
        }
    
        private static List<Path> resolveSeasonDirs(Path root, List<String> requested) throws IOException {
            if (!requested.isEmpty()) {
                List<Path> result = new ArrayList<>();
                for (String season : requested) {
                    Path dir = root.resolve(season);
                    if (!Files.isDirectory(dir)) throw new IOException("Stagione non trovata: " + dir);
                    result.add(dir);
                }
                result.sort(Comparator.comparing(p -> p.getFileName().toString()));
                return result;
            }
            try (Stream<Path> stream = Files.list(root)) {
                return stream.filter(Files::isDirectory).sorted(Comparator.comparing(p -> p.getFileName().toString())).toList();
            }
        }
    
        private static List<Path> listNormalizedFiles(Path seasonDir) throws IOException {
            try (Stream<Path> stream = Files.list(seasonDir)) {
                return stream.filter(Files::isRegularFile)
                        .filter(p -> p.getFileName().toString().startsWith("season_normalized_"))
                        .filter(p -> p.getFileName().toString().endsWith(".json"))
                        .filter(p -> !p.getFileName().toString().contains(".stage"))
                        .filter(p -> !p.getFileName().toString().contains(".final"))
                        .sorted(Comparator.comparing(p -> p.getFileName().toString())).toList();
            }
        }
    
        @SuppressWarnings("unchecked")
        private static List<Map<String, Object>> rows(Object value) {
            if (!(value instanceof List<?> list)) return new ArrayList<>();
            List<Map<String, Object>> result = new ArrayList<>();
            for (Object item : list) if (item instanceof Map<?, ?> map) result.add((Map<String, Object>) map);
            return result;
        }
    
        @SuppressWarnings("unchecked")
        private static Map<String, Object> object(Object value, Path source, String name) throws IOException {
            if (!(value instanceof Map<?, ?> map)) throw new IOException("Oggetto " + name + " mancante in " + source);
            return (Map<String, Object>) map;
        }
    
        private static String string(Object value) { return value == null ? "" : String.valueOf(value); }
        private static double number(Object value) {
            if (value instanceof Number n) return n.doubleValue();
            if (value == null || string(value).isBlank()) return 0;
            try { return Double.parseDouble(string(value).replace(',', '.')); } catch (NumberFormatException e) { return 0; }
        }
    
        private static Object parse(Path source) throws IOException {
            String text = Files.readString(source, StandardCharsets.UTF_8);
            if (!text.isEmpty() && text.charAt(0) == '\uFEFF') text = text.substring(1);
            return new JsonParser(text, source).parse();
        }
    
        public record Result(int seasons, int competitions) {}
    
        private static final class JsonParser {
            private final String text; private final Path source; private int index;
            JsonParser(String text, Path source) { this.text = text; this.source = source; }
            Object parse() throws IOException { skip(); Object v = value(); skip(); if (index != text.length()) fail("contenuto dopo JSON"); return v; }
            private Object value() throws IOException {
                skip(); if (index >= text.length()) fail("fine inattesa"); char c = text.charAt(index);
                return switch (c) { case '{' -> object(); case '[' -> array(); case '"' -> string(); case 't' -> literal("true", true); case 'f' -> literal("false", false); case 'n' -> literal("null", null); default -> number(); };
            }
            private Map<String,Object> object() throws IOException { expect('{'); Map<String,Object> m=new LinkedHashMap<>(); skip(); if (take('}')) return m; while(true){ skip(); String k=string(); skip(); expect(':'); m.put(k,value()); skip(); if(take('}')) return m; expect(','); } }
            private List<Object> array() throws IOException { expect('['); List<Object> a=new ArrayList<>(); skip(); if(take(']')) return a; while(true){ a.add(value()); skip(); if(take(']')) return a; expect(','); } }
            private String string() throws IOException { expect('"'); StringBuilder b=new StringBuilder(); while(index<text.length()){ char c=text.charAt(index++); if(c=='"') return b.toString(); if(c!='\\'){ b.append(c); continue;} if(index>=text.length()) fail("escape incompleto"); char e=text.charAt(index++); switch(e){case '"','\\','/'->b.append(e); case 'b'->b.append('\b'); case 'f'->b.append('\f'); case 'n'->b.append('\n'); case 'r'->b.append('\r'); case 't'->b.append('\t'); case 'u'->{ if(index+4>text.length()) fail("unicode incompleto"); b.append((char)Integer.parseInt(text.substring(index,index+4),16)); index+=4;} default->fail("escape non valido");}} fail("stringa non chiusa"); return ""; }
            private Object number() throws IOException { int s=index; if(peek('-')) index++; digits(); boolean dec=false; if(peek('.')){dec=true;index++;digits();} if(peek('e')||peek('E')){dec=true;index++;if(peek('+')||peek('-'))index++;digits();} String raw=text.substring(s,index); try{ BigDecimal d=new BigDecimal(raw); if(!dec && d.scale()<=0) try{return d.longValueExact();}catch(ArithmeticException ignored){} return d.stripTrailingZeros(); }catch(Exception e){fail("numero non valido"); return null;} }
            private void digits() throws IOException { int s=index; while(index<text.length()&&Character.isDigit(text.charAt(index))) index++; if(index==s) fail("cifre attese"); }
            private Object literal(String l,Object v)throws IOException{ if(!text.startsWith(l,index)) fail("letterale non valido"); index+=l.length(); return v; }
            private void skip(){ while(index<text.length()&&Character.isWhitespace(text.charAt(index))) index++; }
            private boolean take(char c){ if(index<text.length()&&text.charAt(index)==c){index++;return true;} return false; }
            private void expect(char c)throws IOException{ if(!take(c)) fail("atteso '"+c+"'"); }
            private boolean peek(char c){ return index<text.length()&&text.charAt(index)==c; }
            private void fail(String m)throws IOException{ throw new IOException("JSON non valido in "+source+" posizione "+index+": "+m); }
        }
    
        private static final class JsonWriter {
            static String writePretty(Object v){ StringBuilder b=new StringBuilder(); write(v,b,0); return b.toString(); }
            private static void write(Object v,StringBuilder b,int depth){
                if(v==null){b.append("null");return;} if(v instanceof String s){quote(s,b);return;} if(v instanceof Boolean){b.append(v);return;} if(v instanceof Number n){b.append(n instanceof BigDecimal d?d.stripTrailingZeros().toPlainString():n);return;}
                if(v instanceof Map<?,?> m){ b.append('{'); if(!m.isEmpty()){ b.append('\n'); int i=0; for(var e:m.entrySet()){ indent(b,depth+1); quote(String.valueOf(e.getKey()),b); b.append(": "); write(e.getValue(),b,depth+1); if(++i<m.size()) b.append(','); b.append('\n'); } indent(b,depth);} b.append('}'); return; }
                if(v instanceof List<?> l){ b.append('['); if(!l.isEmpty()){ b.append('\n'); for(int i=0;i<l.size();i++){ indent(b,depth+1); write(l.get(i),b,depth+1); if(i+1<l.size()) b.append(','); b.append('\n'); } indent(b,depth);} b.append(']'); return; }
                quote(String.valueOf(v),b);
            }
            private static void indent(StringBuilder b,int d){ b.append("  ".repeat(d)); }
            private static void quote(String s,StringBuilder b){ b.append('"'); for(int i=0;i<s.length();i++){ char c=s.charAt(i); switch(c){case '"'->b.append("\\\"");case '\\'->b.append("\\\\");case '\b'->b.append("\\b");case '\f'->b.append("\\f");case '\n'->b.append("\\n");case '\r'->b.append("\\r");case '\t'->b.append("\\t");default->{if(c<0x20)b.append(String.format("\\u%04x",(int)c));else b.append(c);}}} b.append('"'); }
        }
    }

## src\main\java\it\alterlega\recordsnext\SeasonRegistry.java

File: src\main\java\it\alterlega\recordsnext\SeasonRegistry.java

    package it.alterlega.recordsnext;
    
    import java.net.URI;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.sql.Connection;
    import java.sql.DriverManager;
    import java.sql.PreparedStatement;
    import java.sql.ResultSet;
    import java.sql.Statement;
    import java.time.Instant;
    import java.util.Locale;
    import java.util.regex.Matcher;
    import java.util.regex.Pattern;
    
    /**
     * Registro autonomo delle stagioni e delle relative risorse esterne.
     *
     * <p>Opera esclusivamente sul database SQLite gia importato. Non apre e non
     * modifica file FCM/FCA. Lo schema viene installato in modo idempotente solo
     * quando questa classe viene eseguita.</p>
     */
    public final class SeasonRegistry {
    
        private static final Pattern SEASON_PATTERN =
            Pattern.compile("^(\\d{4})_(\\d{4})$");
    
        private SeasonRegistry() {
        }
    
        public static void main(String[] args) throws Exception {
            if (args.length < 2) {
                printUsage();
                System.exit(2);
            }
    
            Path database = Path.of(args[0]).toAbsolutePath().normalize();
            if (!Files.isRegularFile(database)) {
                throw new IllegalArgumentException(
                    "Database SQLite non trovato: " + database
                );
            }
    
            String command = args[1].trim().toLowerCase(Locale.ROOT);
            Class.forName("org.sqlite.JDBC");
    
            try (Connection connection = DriverManager.getConnection(
                    "jdbc:sqlite:" + database)) {
    
                configureConnection(connection);
                installSchema(connection);
    
                switch (command) {
                    case "show" -> show(connection);
                    case "set-managed" -> setManaged(connection, args);
                    case "set-manual" -> setManual(connection, args);
                    case "set-sites" -> setSites(connection, args);
                    case "validate" -> validateCommand(connection, args);
                    default -> {
                        printUsage();
                        System.exit(2);
                    }
                }
            }
        }
    
        static void installSchema(Connection connection) throws Exception {
            try (Statement statement = connection.createStatement()) {
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS rn_season_configuration (
                        season_id TEXT PRIMARY KEY,
                        management_type TEXT NOT NULL
                            CHECK (management_type IN ('GESTITA', 'MANUALE')),
                        local_site_path TEXT,
                        online_site_url TEXT,
                        dataa_path TEXT,
                        configuration_status TEXT NOT NULL DEFAULT 'DA_CONFIGURARE'
                            CHECK (
                                configuration_status IN (
                                    'DA_CONFIGURARE',
                                    'IN_CORSO',
                                    'COMPLETA'
                                )
                            ),
                        created_at TEXT NOT NULL,
                        updated_at TEXT NOT NULL,
                        FOREIGN KEY (season_id)
                            REFERENCES rn_season(season_id)
                    )
                    """);
    
                statement.execute("""
                    CREATE INDEX IF NOT EXISTS ix_rn_season_configuration_status
                    ON rn_season_configuration(configuration_status)
                    """);
            }
        }
    
        private static void show(Connection connection) throws Exception {
            String sql = """
                SELECT
                    s.season_id,
                    s.is_anchor,
                    COALESCE(c.management_type, 'NON_CONFIGURATA') AS tipo,
                    COALESCE(c.configuration_status, 'DA_CONFIGURARE') AS stato,
                    c.local_site_path,
                    c.online_site_url,
                    c.dataa_path,
                    (SELECT COUNT(*)
                     FROM rn_source_file f
                     WHERE f.season_id = s.season_id
                       AND f.source_type = 'FCM') AS fcm,
                    (SELECT COUNT(*)
                     FROM rn_source_file f
                     WHERE f.season_id = s.season_id
                       AND f.source_type = 'FCA') AS fca
                FROM rn_season s
                LEFT JOIN rn_season_configuration c
                  ON c.season_id = s.season_id
                ORDER BY CAST(SUBSTR(s.season_id, 1, 4) AS INTEGER) DESC,
                         s.season_id DESC
                """;
    
            try (Statement statement = connection.createStatement();
                 ResultSet result = statement.executeQuery(sql)) {
    
                System.out.printf(
                    Locale.ROOT,
                    "%-11s %-6s %-17s %-15s %3s %3s  %s%n",
                    "STAGIONE", "ANCORA", "TIPO", "STATO", "FCM", "FCA",
                    "RISORSE"
                );
    
                while (result.next()) {
                    String resources = resourcesSummary(result);
                    System.out.printf(
                        Locale.ROOT,
                        "%-11s %-6s %-17s %-15s %3d %3d  %s%n",
                        result.getString("season_id"),
                        result.getInt("is_anchor") == 1 ? "SI" : "NO",
                        result.getString("tipo"),
                        result.getString("stato"),
                        result.getInt("fcm"),
                        result.getInt("fca"),
                        resources
                    );
                }
            }
        }
    
        private static void setManaged(Connection connection, String[] args)
                throws Exception {
    
            requireArgumentCount(
                args,
                6,
                "<db> set-managed <stagione> <sito-locale|-> "
                    + "<sito-online|-> <DataA.js|AUTO|->"
            );
    
            String seasonId = requireExistingSeason(connection, args[2]);
            SiteValues sites = parseSites(args[3], args[4], args[5]);
    
            Validation validation = validateManagedSources(connection, seasonId);
            if (!validation.valid()) {
                throw new IllegalStateException(validation.message());
            }
    
            inTransaction(connection, () -> {
                upsertConfiguration(
                    connection,
                    seasonId,
                    "GESTITA",
                    sites,
                    calculateStatus(connection, seasonId, "GESTITA")
                );
            });
    
            System.out.println("Stagione gestita registrata: " + seasonId);
            printSites(sites);
        }
    
        private static void setManual(Connection connection, String[] args)
                throws Exception {
    
            requireArgumentCount(
                args,
                6,
                "<db> set-manual <stagione> <sito-locale|-> "
                    + "<sito-online|-> <DataA.js|AUTO|->"
            );
    
            String seasonId = requireValidSeasonId(args[2]);
            SiteValues sites = parseSites(args[3], args[4], args[5]);
    
            inTransaction(connection, () -> {
                ensureManualSeasonCanBeUsed(connection, seasonId);
                insertSeasonIfMissing(connection, seasonId);
                upsertConfiguration(
                    connection,
                    seasonId,
                    "MANUALE",
                    sites,
                    "COMPLETA"
                );
            });
    
            System.out.println("Stagione manuale registrata: " + seasonId);
            printSites(sites);
        }
    
        private static void setSites(Connection connection, String[] args)
                throws Exception {
    
            requireArgumentCount(
                args,
                6,
                "<db> set-sites <stagione> <sito-locale|-> "
                    + "<sito-online|-> <DataA.js|AUTO|->"
            );
    
            String seasonId = requireExistingSeason(connection, args[2]);
            SiteValues sites = parseSites(args[3], args[4], args[5]);
            String managementType = requireConfiguredType(connection, seasonId);
    
            inTransaction(connection, () -> {
                upsertConfiguration(
                    connection,
                    seasonId,
                    managementType,
                    sites,
                    calculateStatus(connection, seasonId, managementType)
                );
            });
    
            System.out.println("Risorse stagione aggiornate: " + seasonId);
            printSites(sites);
        }
    
        private static void validateCommand(Connection connection, String[] args)
                throws Exception {
    
            requireArgumentCount(args, 3, "<db> validate <stagione>");
            String seasonId = requireExistingSeason(connection, args[2]);
            String managementType = requireConfiguredType(connection, seasonId);
    
            Validation validation = validateSeason(
                connection,
                seasonId,
                managementType
            );
    
            if (!validation.valid()) {
                System.out.println(seasonId + "  NON VALIDA");
                System.out.println(validation.message());
                System.exit(1);
            }
    
            String status = calculateStatus(connection, seasonId, managementType);
            updateStoredStatus(connection, seasonId, status);
    
            System.out.println(seasonId + "  VALIDA");
            System.out.println("Tipo  : " + managementType);
            System.out.println("Stato : " + status);
        }
    
        private static Validation validateSeason(
                Connection connection,
                String seasonId,
                String managementType) throws Exception {
    
            if (managementType.equals("GESTITA")) {
                Validation sources = validateManagedSources(connection, seasonId);
                if (!sources.valid()) {
                    return sources;
                }
            } else if (countSources(connection, seasonId) != 0) {
                return Validation.error(
                    "La stagione manuale " + seasonId
                        + " possiede sorgenti FCM/FCA importate."
                );
            }
    
            SiteValues sites = readSites(connection, seasonId);
            try {
                validateStoredSites(sites);
            } catch (IllegalArgumentException exception) {
                return Validation.error(exception.getMessage());
            }
    
            return Validation.ok();
        }
    
        private static Validation validateManagedSources(
                Connection connection,
                String seasonId) throws Exception {
    
            SourceCount fcm = readSourceCount(connection, seasonId, "FCM");
            SourceCount fca = readSourceCount(connection, seasonId, "FCA");
    
            if (fcm.configured() != 1 || fca.configured() != 1) {
                return Validation.error(
                    "La stagione " + seasonId
                        + " deve avere esattamente un FCM e un FCA in "
                        + "rn_source_file; trovati FCM=" + fcm.configured()
                        + ", FCA=" + fca.configured() + "."
                );
            }
    
            if (fcm.completedImports() != 1 || fca.completedImports() != 1) {
                return Validation.error(
                    "Le sorgenti della stagione " + seasonId
                        + " non corrispondono a importazioni COMPLETED univoche; "
                        + "FCM=" + fcm.completedImports()
                        + ", FCA=" + fca.completedImports() + "."
                );
            }
    
            return Validation.ok();
        }
    
        private static SourceCount readSourceCount(
                Connection connection,
                String seasonId,
                String sourceType) throws Exception {
    
            String sql = """
                SELECT
                    COUNT(*) AS configured_count,
                    SUM(CASE WHEN i.status = 'COMPLETED' THEN 1 ELSE 0 END)
                        AS completed_count
                FROM rn_source_file f
                LEFT JOIN rn_import i
                  ON i.import_id = f.import_id
                WHERE f.season_id = ?
                  AND f.source_type = ?
                """;
    
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, seasonId);
                statement.setString(2, sourceType);
                try (ResultSet result = statement.executeQuery()) {
                    result.next();
                    return new SourceCount(
                        result.getInt("configured_count"),
                        result.getInt("completed_count")
                    );
                }
            }
        }
    
        private static int countSources(
                Connection connection,
                String seasonId) throws Exception {
    
            try (PreparedStatement statement = connection.prepareStatement("""
                SELECT COUNT(*)
                FROM rn_source_file
                WHERE season_id = ?
                """)) {
                statement.setString(1, seasonId);
                try (ResultSet result = statement.executeQuery()) {
                    result.next();
                    return result.getInt(1);
                }
            }
        }
    
        private static String calculateStatus(
                Connection connection,
                String seasonId,
                String managementType) throws Exception {
    
            if (managementType.equals("MANUALE")) {
                return "COMPLETA";
            }
    
            Validation sources = validateManagedSources(connection, seasonId);
            if (!sources.valid()) {
                return "DA_CONFIGURARE";
            }
    
            long pendingMappings = countPendingMappings(connection, seasonId);
            return pendingMappings == 0 ? "COMPLETA" : "IN_CORSO";
        }
    
        private static long countPendingMappings(
                Connection connection,
                String seasonId) throws Exception {
    
            String sql = """
                SELECT
                    (SELECT COUNT(*)
                     FROM rn_competition_mapping cm
                     JOIN rn_competition_season cs
                       ON cs.competition_season_id = cm.competition_season_id
                     WHERE cs.season_id = ?
                       AND cm.mapping_status = 'DA_CONFIGURARE')
                    +
                    (SELECT COUNT(*)
                     FROM rn_team_mapping tm
                     JOIN rn_team_season ts
                       ON ts.team_season_id = tm.team_season_id
                     WHERE ts.season_id = ?
                       AND tm.mapping_status = 'DA_CONFIGURARE')
                """;
    
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, seasonId);
                statement.setString(2, seasonId);
                try (ResultSet result = statement.executeQuery()) {
                    result.next();
                    return result.getLong(1);
                }
            }
        }
    
        private static void ensureManualSeasonCanBeUsed(
                Connection connection,
                String seasonId) throws Exception {
    
            if (countSources(connection, seasonId) != 0) {
                throw new IllegalStateException(
                    "La stagione " + seasonId
                        + " possiede gia sorgenti importate e non puo essere "
                        + "registrata come MANUALE."
                );
            }
    
            try (PreparedStatement statement = connection.prepareStatement("""
                SELECT is_anchor
                FROM rn_season
                WHERE season_id = ?
                """)) {
                statement.setString(1, seasonId);
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next() && result.getInt("is_anchor") == 1) {
                        throw new IllegalStateException(
                            "La stagione ancora non puo essere MANUALE: "
                                + seasonId
                        );
                    }
                }
            }
        }
    
        private static void insertSeasonIfMissing(
                Connection connection,
                String seasonId) throws Exception {
    
            int startYear = startYear(seasonId);
            String now = Instant.now().toString();
    
            try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO rn_season (
                    season_id,
                    display_name,
                    sort_order,
                    is_anchor,
                    created_at,
                    updated_at
                ) VALUES (?, ?, ?, 0, ?, ?)
                ON CONFLICT(season_id) DO NOTHING
                """)) {
                statement.setString(1, seasonId);
                statement.setString(2, seasonId.replace('_', '/'));
                statement.setInt(3, startYear);
                statement.setString(4, now);
                statement.setString(5, now);
                statement.executeUpdate();
            }
        }
    
        private static void upsertConfiguration(
                Connection connection,
                String seasonId,
                String managementType,
                SiteValues sites,
                String status) throws Exception {
    
            String now = Instant.now().toString();
    
            try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO rn_season_configuration (
                    season_id,
                    management_type,
                    local_site_path,
                    online_site_url,
                    dataa_path,
                    configuration_status,
                    created_at,
                    updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(season_id) DO UPDATE SET
                    management_type = excluded.management_type,
                    local_site_path = excluded.local_site_path,
                    online_site_url = excluded.online_site_url,
                    dataa_path = excluded.dataa_path,
                    configuration_status = excluded.configuration_status,
                    updated_at = excluded.updated_at
                """)) {
                statement.setString(1, seasonId);
                statement.setString(2, managementType);
                statement.setString(3, sites.localSite());
                statement.setString(4, sites.onlineSite());
                statement.setString(5, sites.dataA());
                statement.setString(6, status);
                statement.setString(7, now);
                statement.setString(8, now);
                statement.executeUpdate();
            }
        }
    
        private static void updateStoredStatus(
                Connection connection,
                String seasonId,
                String status) throws Exception {
    
            try (PreparedStatement statement = connection.prepareStatement("""
                UPDATE rn_season_configuration
                SET configuration_status = ?,
                    updated_at = ?
                WHERE season_id = ?
                """)) {
                statement.setString(1, status);
                statement.setString(2, Instant.now().toString());
                statement.setString(3, seasonId);
                statement.executeUpdate();
            }
        }
    
        private static String requireConfiguredType(
                Connection connection,
                String seasonId) throws Exception {
    
            try (PreparedStatement statement = connection.prepareStatement("""
                SELECT management_type
                FROM rn_season_configuration
                WHERE season_id = ?
                """)) {
                statement.setString(1, seasonId);
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        throw new IllegalStateException(
                            "Stagione non ancora registrata: " + seasonId
                        );
                    }
                    return result.getString(1);
                }
            }
        }
    
        private static SiteValues readSites(
                Connection connection,
                String seasonId) throws Exception {
    
            try (PreparedStatement statement = connection.prepareStatement("""
                SELECT local_site_path, online_site_url, dataa_path
                FROM rn_season_configuration
                WHERE season_id = ?
                """)) {
                statement.setString(1, seasonId);
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        throw new IllegalStateException(
                            "Stagione non ancora registrata: " + seasonId
                        );
                    }
                    return new SiteValues(
                        result.getString(1),
                        result.getString(2),
                        result.getString(3)
                    );
                }
            }
        }
    
        private static SiteValues parseSites(
                String localArgument,
                String onlineArgument,
                String dataAArgument) {
    
            String local = nullable(localArgument);
            String online = nullable(onlineArgument);
    
            if (local != null) {
                Path localPath = Path.of(local).toAbsolutePath().normalize();
                if (!Files.isDirectory(localPath)) {
                    throw new IllegalArgumentException(
                        "Cartella sito locale non trovata: " + localPath
                    );
                }
                local = localPath.toString();
            }
    
            validateOnlineUrl(online);
    
            String dataA;
            if (dataAArgument.trim().equalsIgnoreCase("AUTO")) {
                if (local == null) {
                    throw new IllegalArgumentException(
                        "AUTO richiede il percorso del sito locale."
                    );
                }
                Path detected = Path.of(local, "js", "DataA.js")
                    .toAbsolutePath().normalize();
                if (!Files.isRegularFile(detected)) {
                    throw new IllegalArgumentException(
                        "DataA.js non trovato automaticamente: " + detected
                    );
                }
                dataA = detected.toString();
            } else {
                dataA = nullable(dataAArgument);
                if (dataA != null) {
                    Path dataAPath = Path.of(dataA).toAbsolutePath().normalize();
                    if (!Files.isRegularFile(dataAPath)) {
                        throw new IllegalArgumentException(
                            "File DataA.js non trovato: " + dataAPath
                        );
                    }
                    dataA = dataAPath.toString();
                }
            }
    
            return new SiteValues(local, online, dataA);
        }
    
        private static void validateStoredSites(SiteValues sites) {
            if (sites.localSite() != null
                    && !Files.isDirectory(Path.of(sites.localSite()))) {
                throw new IllegalArgumentException(
                    "Cartella sito locale non piu disponibile: "
                        + sites.localSite()
                );
            }
    
            validateOnlineUrl(sites.onlineSite());
    
            if (sites.dataA() != null
                    && !Files.isRegularFile(Path.of(sites.dataA()))) {
                throw new IllegalArgumentException(
                    "File DataA.js non piu disponibile: " + sites.dataA()
                );
            }
        }
    
        private static void validateOnlineUrl(String value) {
            if (value == null) {
                return;
            }
    
            URI uri;
            try {
                uri = URI.create(value);
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException(
                    "URL sito online non valido: " + value,
                    exception
                );
            }
    
            String scheme = uri.getScheme();
            if (scheme == null
                    || !(scheme.equalsIgnoreCase("http")
                        || scheme.equalsIgnoreCase("https"))
                    || uri.getHost() == null) {
                throw new IllegalArgumentException(
                    "URL sito online non valido: " + value
                );
            }
        }
    
        private static String requireExistingSeason(
                Connection connection,
                String value) throws Exception {
    
            String seasonId = requireValidSeasonId(value);
    
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT COUNT(*) FROM rn_season WHERE season_id = ?")) {
                statement.setString(1, seasonId);
                try (ResultSet result = statement.executeQuery()) {
                    result.next();
                    if (result.getInt(1) != 1) {
                        throw new IllegalArgumentException(
                            "Stagione non trovata: " + seasonId
                        );
                    }
                }
            }
    
            return seasonId;
        }
    
        private static String requireValidSeasonId(String value) {
            String seasonId = value.trim();
            Matcher matcher = SEASON_PATTERN.matcher(seasonId);
            if (!matcher.matches()) {
                throw new IllegalArgumentException(
                    "Formato stagione non valido: " + seasonId
                        + ". Atteso AAAA_AAAA."
                );
            }
    
            int start = Integer.parseInt(matcher.group(1));
            int end = Integer.parseInt(matcher.group(2));
            if (end != start + 1) {
                throw new IllegalArgumentException(
                    "Stagione non consecutiva: " + seasonId
                );
            }
            return seasonId;
        }
    
        private static int startYear(String seasonId) {
            return Integer.parseInt(seasonId.substring(0, 4));
        }
    
        private static String resourcesSummary(ResultSet result) throws Exception {
            StringBuilder value = new StringBuilder();
            appendResource(value, "locale", result.getString("local_site_path"));
            appendResource(value, "online", result.getString("online_site_url"));
            appendResource(value, "DataA", result.getString("dataa_path"));
            return value.length() == 0 ? "-" : value.toString();
        }
    
        private static void appendResource(
                StringBuilder builder,
                String label,
                String value) {
            if (value == null || value.isBlank()) {
                return;
            }
            if (builder.length() > 0) {
                builder.append(" | ");
            }
            builder.append(label).append('=').append(value);
        }
    
        private static void printSites(SiteValues sites) {
            System.out.println(
                "Locale: " + displayNullable(sites.localSite())
            );
            System.out.println(
                "Online: " + displayNullable(sites.onlineSite())
            );
            System.out.println(
                "DataA : " + displayNullable(sites.dataA())
            );
        }
    
        private static String displayNullable(String value) {
            return value == null ? "-" : value;
        }
    
        private static String nullable(String value) {
            String trimmed = value.trim();
            return trimmed.isBlank() || trimmed.equals("-") ? null : trimmed;
        }
    
        private static void requireArgumentCount(
                String[] args,
                int expected,
                String usage) {
            if (args.length != expected) {
                throw new IllegalArgumentException("Uso: " + usage);
            }
        }
    
        private static void configureConnection(Connection connection)
                throws Exception {
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");
                statement.execute("PRAGMA busy_timeout = 10000");
            }
        }
    
        private static void inTransaction(
                Connection connection,
                SqlOperation operation) throws Exception {
    
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                operation.run();
                connection.commit();
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }
    
        private static void printUsage() {
            System.err.println("Comandi:");
            System.err.println("  <db> show");
            System.err.println(
                "  <db> set-managed <stagione> <sito-locale|-> "
                    + "<sito-online|-> <DataA.js|AUTO|->"
            );
            System.err.println(
                "  <db> set-manual <stagione> <sito-locale|-> "
                    + "<sito-online|-> <DataA.js|AUTO|->"
            );
            System.err.println(
                "  <db> set-sites <stagione> <sito-locale|-> "
                    + "<sito-online|-> <DataA.js|AUTO|->"
            );
            System.err.println("  <db> validate <stagione>");
        }
    
        @FunctionalInterface
        private interface SqlOperation {
            void run() throws Exception;
        }
    
        private record SiteValues(
            String localSite,
            String onlineSite,
            String dataA
        ) {
        }
    
        private record SourceCount(int configured, int completedImports) {
        }
    
        private record Validation(boolean valid, String message) {
            static Validation ok() {
                return new Validation(true, "OK");
            }
    
            static Validation error(String message) {
                return new Validation(false, message);
            }
        }
    }

## src\main\java\it\alterlega\recordsnext\SerieAQueryProbe.java

File: src\main\java\it\alterlega\recordsnext\SerieAQueryProbe.java

    package it.alterlega.recordsnext;
    
    import java.nio.file.Path;
    import java.sql.Connection;
    import java.sql.DriverManager;
    import java.sql.ResultSet;
    import java.sql.Statement;
    import java.util.Locale;
    
    public final class SerieAQueryProbe {
    
        private SerieAQueryProbe() {
        }
    
        public static void main(String[] args) throws Exception {
            if (args.length != 1) {
                System.err.println("Uso: SerieAQueryProbe <recordsnext.db>");
                System.exit(2);
            }
    
            Path database = Path.of(args[0]).toAbsolutePath().normalize();
    
            Class.forName("org.sqlite.JDBC");
    
            try (Connection connection = DriverManager.getConnection(
                    "jdbc:sqlite:" + database)) {
    
                printCompetition(connection);
                printGironi(connection);
                printCounts(connection);
                printMatches(connection);
            }
        }
    
        private static void printCompetition(Connection connection)
                throws Exception {
    
            String sql = """
                SELECT ID, NOME
                FROM raw_2025_2026_fcm_competizione
                WHERE ID = 4
                """;
    
            try (Statement statement = connection.createStatement();
                 ResultSet result = statement.executeQuery(sql)) {
    
                if (!result.next()) {
                    throw new IllegalStateException(
                        "Competizione Serie A con ID 4 non trovata."
                    );
                }
    
                System.out.println("=== COMPETIZIONE ===");
                System.out.println("ID   : " + result.getInt("ID"));
                System.out.println("Nome : " + result.getString("NOME"));
            }
        }
    
        private static void printGironi(Connection connection)
                throws Exception {
    
            String sql = """
                SELECT
                    g.ID,
                    g.NOME,
                    COUNT(i.ID) AS incontri,
                    SUM(CASE WHEN i.GIOCATO <> 0 THEN 1 ELSE 0 END) AS giocati,
                    SUM(
                        CASE
                            WHEN i.GIOCATO <> 0
                             AND i.IDCASA <> 0
                             AND i.IDFUORI <> 0
                            THEN 1
                            ELSE 0
                        END
                    ) AS validi
                FROM raw_2025_2026_fcm_girone g
                LEFT JOIN raw_2025_2026_fcm_incontro i
                    ON i.IDGIRONE = g.ID
                WHERE g.IDCOMPETIZIONE = 4
                GROUP BY g.ID, g.NOME
                ORDER BY g.ID
                """;
    
            System.out.println();
            System.out.println("=== GIRONI SERIE A ===");
    
            try (Statement statement = connection.createStatement();
                 ResultSet result = statement.executeQuery(sql)) {
    
                while (result.next()) {
                    System.out.printf(
                        Locale.ROOT,
                        "ID=%d nome=%s incontri=%d giocati=%d validi=%d%n",
                        result.getInt("ID"),
                        result.getString("NOME"),
                        result.getLong("incontri"),
                        result.getLong("giocati"),
                        result.getLong("validi")
                    );
                }
            }
        }
    
        private static void printCounts(Connection connection)
                throws Exception {
    
            String sql = """
                SELECT
                    COUNT(*) AS tutti,
                    SUM(CASE WHEN i.GIOCATO <> 0 THEN 1 ELSE 0 END) AS giocati,
                    SUM(
                        CASE
                            WHEN i.GIOCATO <> 0
                             AND i.IDCASA <> 0
                             AND i.IDFUORI <> 0
                            THEN 1
                            ELSE 0
                        END
                    ) AS validi
                FROM raw_2025_2026_fcm_incontro i
                JOIN raw_2025_2026_fcm_girone g
                    ON g.ID = i.IDGIRONE
                WHERE g.IDCOMPETIZIONE = 4
                """;
    
            try (Statement statement = connection.createStatement();
                 ResultSet result = statement.executeQuery(sql)) {
    
                result.next();
    
                System.out.println();
                System.out.println("=== CONTEGGI ===");
                System.out.println("Tutti   : " + result.getLong("tutti"));
                System.out.println("Giocati : " + result.getLong("giocati"));
                System.out.println("Validi  : " + result.getLong("validi"));
            }
        }
    
        private static void printMatches(Connection connection)
                throws Exception {
    
            String sql = """
                SELECT
                    i.ID AS id_incontro,
                    g.ID AS id_girone,
                    g.NOME AS girone,
                    i.GIORNATADIA AS giornata_di_a,
                    i.IDGIORNATA AS id_giornata,
                    gio.DESC AS descrizione_giornata,
                    i.IDCASA AS id_casa,
                    casa.NOME AS squadra_casa,
                    i.IDFUORI AS id_fuori,
                    fuori.NOME AS squadra_fuori,
                    i.GOLCASA AS gol_casa,
                    i.GOLFUORI AS gol_fuori,
                    i.TOTCASA AS punti_casa,
                    i.TOTFUORI AS punti_fuori
                FROM raw_2025_2026_fcm_incontro i
                JOIN raw_2025_2026_fcm_girone g
                    ON g.ID = i.IDGIRONE
                LEFT JOIN raw_2025_2026_fcm_giornata gio
                    ON gio.ID = i.IDGIORNATA
                JOIN raw_2025_2026_fcm_fantasquadra casa
                    ON casa.ID = i.IDCASA
                JOIN raw_2025_2026_fcm_fantasquadra fuori
                    ON fuori.ID = i.IDFUORI
                WHERE g.IDCOMPETIZIONE = 4
                  AND i.GIOCATO <> 0
                  AND i.IDCASA <> 0
                  AND i.IDFUORI <> 0
                ORDER BY
                    i.GIORNATADIA,
                    i.ID
                LIMIT 15
                """;
    
            long started = System.nanoTime();
    
            System.out.println();
            System.out.println("=== PRIME 15 PARTITE ===");
    
            try (Statement statement = connection.createStatement();
                 ResultSet result = statement.executeQuery(sql)) {
    
                while (result.next()) {
                    System.out.printf(
                        Locale.ROOT,
                        "%d | %2d | %-25s - %-25s | %d-%d | %.1f-%.1f | %s%n",
                        result.getLong("id_incontro"),
                        result.getInt("giornata_di_a"),
                        result.getString("squadra_casa"),
                        result.getString("squadra_fuori"),
                        result.getInt("gol_casa"),
                        result.getInt("gol_fuori"),
                        result.getDouble("punti_casa"),
                        result.getDouble("punti_fuori"),
                        result.getString("descrizione_giornata")
                    );
                }
            }
    
            long finished = System.nanoTime();
    
            System.out.printf(
                Locale.ROOT,
                "%nTempo query e lettura campione: %.3f ms%n",
                (finished - started) / 1_000_000.0
            );
        }
    }

## src\main\java\it\alterlega\recordsnext\SerieARoundProbe.java

File: src\main\java\it\alterlega\recordsnext\SerieARoundProbe.java

    package it.alterlega.recordsnext;
    
    import java.nio.file.Path;
    import java.sql.Connection;
    import java.sql.DriverManager;
    import java.sql.ResultSet;
    import java.sql.Statement;
    
    public final class SerieARoundProbe {
    
        private SerieARoundProbe() {
        }
    
        public static void main(String[] args) throws Exception {
            Path database = Path.of(args[0]).toAbsolutePath().normalize();
    
            Class.forName("org.sqlite.JDBC");
    
            String sql = """
                WITH giornate AS (
                    SELECT
                        i.IDGIORNATA,
                        i.GIORNATADIA,
                        gio."DESC" AS descrizione,
                        MIN(i.ID) AS primo_incontro,
                        COUNT(*) AS incontri
                    FROM raw_2025_2026_fcm_incontro i
                    JOIN raw_2025_2026_fcm_girone g
                        ON g.ID = i.IDGIRONE
                    LEFT JOIN raw_2025_2026_fcm_giornata gio
                        ON gio.ID = i.IDGIORNATA
                    WHERE g.IDCOMPETIZIONE = 4
                      AND i.GIOCATO <> 0
                      AND i.IDCASA <> 0
                      AND i.IDFUORI <> 0
                    GROUP BY
                        i.IDGIORNATA,
                        i.GIORNATADIA,
                        gio."DESC"
                )
                SELECT
                    ROW_NUMBER() OVER (
                        ORDER BY primo_incontro
                    ) AS giornata_competizione,
                    IDGIORNATA,
                    GIORNATADIA AS giornata_serie_a,
                    descrizione,
                    incontri,
                    primo_incontro
                FROM giornate
                ORDER BY primo_incontro
                """;
    
            try (Connection connection = DriverManager.getConnection(
                        "jdbc:sqlite:" + database);
                 Statement statement = connection.createStatement();
                 ResultSet result = statement.executeQuery(sql)) {
    
                int giornate = 0;
                int incontri = 0;
    
                while (result.next()) {
                    giornate++;
                    incontri += result.getInt("incontri");
    
                    System.out.printf(
                        "%2d | IDGIORNATA=%4d | Serie A=%2d | incontri=%d | %s%n",
                        result.getInt("giornata_competizione"),
                        result.getInt("IDGIORNATA"),
                        result.getInt("giornata_serie_a"),
                        result.getInt("incontri"),
                        result.getString("descrizione")
                    );
                }
    
                System.out.println();
                System.out.println("Giornate : " + giornate);
                System.out.println("Incontri : " + incontri);
            }
        }
    }

## src\main\java\it\alterlega\recordsnext\SqliteAudit.java

File: src\main\java\it\alterlega\recordsnext\SqliteAudit.java

    package it.alterlega.recordsnext;
    
    import java.nio.file.Path;
    import java.sql.Connection;
    import java.sql.DriverManager;
    import java.sql.ResultSet;
    import java.sql.Statement;
    
    public final class SqliteAudit {
    
        private SqliteAudit() {
        }
    
        public static void main(String[] args) throws Exception {
            if (args.length != 1) {
                System.err.println("Uso: SqliteAudit <recordsnext.db>");
                System.exit(2);
            }
    
            Path database = Path.of(args[0]).toAbsolutePath().normalize();
    
            Class.forName("org.sqlite.JDBC");
    
            try (Connection connection =
                     DriverManager.getConnection("jdbc:sqlite:" + database);
                 Statement statement = connection.createStatement()) {
    
                printValue(
                    statement,
                    "Importazioni completate",
                    "SELECT COUNT(*) FROM rn_import WHERE status='COMPLETED'"
                );
    
                printValue(
                    statement,
                    "Tabelle catalogate",
                    "SELECT COUNT(*) FROM rn_table_catalog"
                );
    
                printValue(
                    statement,
                    "Colonne catalogate",
                    "SELECT COUNT(*) FROM rn_column_catalog"
                );
    
                printValue(
                    statement,
                    "Righe sorgente",
                    "SELECT SUM(source_row_count) FROM rn_table_catalog"
                );
    
                printValue(
                    statement,
                    "Righe importate",
                    "SELECT SUM(imported_row_count) FROM rn_table_catalog"
                );
    
                printValue(
                    statement,
                    "Audit falliti",
                    "SELECT COUNT(*) FROM rn_table_catalog WHERE audit_ok<>1"
                );
    
                printValue(
                    statement,
                    "Tabelle raw reali",
                    """
                    SELECT COUNT(*)
                    FROM sqlite_master
                    WHERE type='table'
                      AND name LIKE 'raw_%'
                    """
                );
    
                System.out.println();
                System.out.println("=== IMPORTAZIONI ===");
    
                try (ResultSet result = statement.executeQuery(
                        """
                        SELECT source_type,
                               table_count,
                               column_count,
                               row_count,
                               status
                        FROM rn_import
                        ORDER BY import_id
                        """)) {
    
                    while (result.next()) {
                        System.out.printf(
                            "%s tabelle=%d colonne=%d righe=%d stato=%s%n",
                            result.getString("source_type"),
                            result.getInt("table_count"),
                            result.getInt("column_count"),
                            result.getLong("row_count"),
                            result.getString("status")
                        );
                    }
                }
    
                System.out.println();
                System.out.println("Audit SQLite completato.");
            }
        }
    
        private static void printValue(
                Statement statement,
                String label,
                String sql) throws Exception {
    
            try (ResultSet result = statement.executeQuery(sql)) {
                result.next();
                System.out.printf("%-24s: %d%n", label, result.getLong(1));
            }
        }
    }

## src\test\java\it\alterlega\recordsnext\app\config\ProcessingConfigLoaderTest.java

File: src\test\java\it\alterlega\recordsnext\app\config\ProcessingConfigLoaderTest.java

    package it.alterlega.recordsnext.app.config;
    
    import it.alterlega.recordsnext.app.model.CoreRecordCatalog;
    import it.alterlega.recordsnext.app.model.RecordFamily;
    import org.junit.jupiter.api.Test;
    import org.junit.jupiter.api.io.TempDir;
    
    import java.nio.file.Files;
    import java.nio.file.Path;
    
    import static org.junit.jupiter.api.Assertions.*;
    
    class ProcessingConfigLoaderTest {
        @TempDir Path temp;
    
        @Test
        void loadsFamiliesAndKeepsCulometroDisabled() throws Exception {
            Path file = temp.resolve("processing.json");
            Files.writeString(file, """
                    {"schemaVersion":"2.0","processing":{"families":{
                      "classics":{"enabled":true,"children":"ALL"},
                      "series":{"enabled":true,"children":"ALL"},
                      "ru":{"enabled":true,"children":"ALL"},
                      "modifiers":{"enabled":true,"children":{"defence":true,"captain":false,"homeField":true}},
                      "thresholdsLuck":{"enabled":true,"children":"ALL"}},
                      "culometro":{"enabled":false},
                      "output":{"writeManifest":true,"writeCore":true,"publishToSite":false}}}
                    """);
            var options = ProcessingConfigLoader.load(file);
            assertEquals(5, options.selection().enabledFamilies().size());
            assertTrue(options.familyEnabled(RecordFamily.MODIFIERS));
            assertFalse(options.culometroEnabled());
            assertFalse(options.selection().enabledChildren().contains(CoreRecordCatalog.CULOMETRO_ID));
            assertTrue(options.selection().enabledChildren().contains("modifiers.defence"));
            assertFalse(options.selection().enabledChildren().contains("modifiers.captain"));
        }
    
        @Test
        void enablesCulometroOnlyWhenExplicitlyRequested() throws Exception {
            Path file = temp.resolve("processing.json");
            Files.writeString(file, """
                    {"schemaVersion":"2.0","processing":{"families":{
                      "thresholdsLuck":{"enabled":true,"children":"ALL"}},
                      "culometro":{"enabled":true},
                      "output":{"writeManifest":true,"writeCore":true,"publishToSite":false}}}
                    """);
            var options = ProcessingConfigLoader.load(file);
            assertTrue(options.culometroEnabled());
            assertTrue(options.selection().enabledChildren().contains(CoreRecordCatalog.CULOMETRO_ID));
        }
    
        @Test
        void rejectsUnsupportedSchema() throws Exception {
            Path file = temp.resolve("processing.json");
            Files.writeString(file, "{\"schemaVersion\":\"3.0\",\"processing\":{}}");
            assertThrows(IllegalArgumentException.class, () -> ProcessingConfigLoader.load(file));
        }
    }

## src\test\java\it\alterlega\recordsnext\app\manifest\ManifestJsWriterTest.java

File: src\test\java\it\alterlega\recordsnext\app\manifest\ManifestJsWriterTest.java

    package it.alterlega.recordsnext.app.manifest;
    
    import it.alterlega.recordsnext.app.PipelinePreflight;
    import it.alterlega.recordsnext.app.ProcessingOptions;
    import org.junit.jupiter.api.Test;
    
    import java.time.OffsetDateTime;
    import java.util.List;
    
    import static org.junit.jupiter.api.Assertions.assertFalse;
    import static org.junit.jupiter.api.Assertions.assertTrue;
    
    class ManifestJsWriterTest {
        @Test
        void legacyClassicsAndRuProduceManifestWithoutCulometro() {
            ProcessingOptions options = new ProcessingOptions(true, true, true, false);
            PipelinePreflight.Result preflight = PipelinePreflight.evaluate(options);
            ManifestMetadata metadata = new ManifestMetadata(
                    "RecordsNext by mauz79",
                    "2.0.0-dev",
                    "2.0",
                    OffsetDateTime.parse("2026-08-05T15:30:00+02:00"),
                    "alterlega",
                    "2025_2026",
                    List.of("2025_2026"),
                    List.of("fcmRecordsNext_Classics.js", "fcmRecordsNext_RU.js")
            );
    
            String js = ManifestJsWriter.render(options, preflight, metadata);
    
            assertTrue(js.startsWith("window.fcmRecordsNextManifest = {"));
            assertTrue(js.contains("requestedFamilies: [\"classics\", \"ru\"]"));
            assertTrue(js.contains("culometroGenerated: false"));
            assertTrue(js.contains("fcmRecordsNext_Classics.js"));
            assertFalse(js.contains("null"));
        }
    }

## src\test\java\it\alterlega\recordsnext\app\manifest\ManifestPublishingSupportTest.java

File: src\test\java\it\alterlega\recordsnext\app\manifest\ManifestPublishingSupportTest.java

    package it.alterlega.recordsnext.app.manifest;
    
    import it.alterlega.recordsnext.app.PipelinePreflight;
    import it.alterlega.recordsnext.app.ProcessingOptions;
    import org.junit.jupiter.api.Test;
    import org.junit.jupiter.api.io.TempDir;
    
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.time.OffsetDateTime;
    import java.util.List;
    
    import static org.junit.jupiter.api.Assertions.assertTrue;
    
    class ManifestPublishingSupportTest {
        @TempDir
        Path tempDir;
    
        @Test
        void manifestIncludesAlreadyGeneratedFilesAndItself() throws Exception {
            Files.writeString(tempDir.resolve("records2026.recordstagionali.classic.js"), "window.TEST = {};\n");
    
            ProcessingOptions options = new ProcessingOptions(true, false, true, false);
            PipelinePreflight.Result preflight = PipelinePreflight.evaluate(options);
            ManifestMetadata metadata = new ManifestMetadata(
                    "RecordsNext by mauz79",
                    "2.0.0-dev",
                    "2.0",
                    OffsetDateTime.parse("2026-08-05T15:30:00+02:00"),
                    "alterlega",
                    "2025_2026",
                    List.of("2025_2026"),
                    List.of()
            );
    
            Path manifest = ManifestPublishingSupport.write(tempDir, options, preflight, metadata);
            String js = Files.readString(manifest);
    
            assertTrue(js.contains("records2026.recordstagionali.classic.js"));
            assertTrue(js.contains("fcmRecordsNext_Manifest.js"));
            assertTrue(js.contains("window.fcmRecordsNextManifest"));
        }
    }

## src\test\java\it\alterlega\recordsnext\app\model\ExecutionPlannerTest.java

File: src\test\java\it\alterlega\recordsnext\app\model\ExecutionPlannerTest.java

    package it.alterlega.recordsnext.app.model;
    
    import org.junit.jupiter.api.Test;
    
    import java.util.EnumSet;
    import java.util.Set;
    
    import static org.junit.jupiter.api.Assertions.assertEquals;
    import static org.junit.jupiter.api.Assertions.assertFalse;
    import static org.junit.jupiter.api.Assertions.assertTrue;
    
    class ExecutionPlannerTest {
        @Test
        void missingCaptainSkipsOnlyCaptainSeries() {
            ProcessingSelection selection = new ProcessingSelection(
                    EnumSet.of(RecordFamily.CLASSICS, RecordFamily.SERIES),
                    Set.of(),
                    false,
                    true,
                    false
            );
    
            ExecutionPlan plan = ExecutionPlanner.plan(
                    selection,
                    DependencyInventory.legacyCapabilities(false, true, true, false)
            );
    
            ExecutionPlanItem captain = plan.items().stream()
                    .filter(item -> item.child().id().equals("series.captain-bonus"))
                    .findFirst()
                    .orElseThrow();
    
            ExecutionPlanItem classic = plan.items().stream()
                    .filter(item -> item.child().id().equals("classics.highest-match-score"))
                    .findFirst()
                    .orElseThrow();
    
            assertEquals(OutputStatus.SKIPPED_REQUIRED_DEPENDENCY, captain.status());
            assertEquals(OutputStatus.GENERATED_COMPLETE, classic.status());
        }
    
        @Test
        void culometroRemainsNotSelectedByDefault() {
            ProcessingSelection selection = new ProcessingSelection(
                    EnumSet.of(RecordFamily.THRESHOLDS_LUCK),
                    Set.of(),
                    false,
                    true,
                    false
            );
    
            ExecutionPlan plan = ExecutionPlanner.plan(
                    selection,
                    DependencyInventory.legacyCapabilities(false, true, true, true)
            );
    
            ExecutionPlanItem culometro = plan.items().stream()
                    .filter(item -> item.child().id().equals(CoreRecordCatalog.CULOMETRO_ID))
                    .findFirst()
                    .orElseThrow();
    
            assertEquals(OutputStatus.SKIPPED_NOT_SELECTED, culometro.status());
        }
    
        @Test
        void explicitCulometroCanBePlanned() {
            ProcessingSelection selection = new ProcessingSelection(
                    EnumSet.of(RecordFamily.THRESHOLDS_LUCK),
                    Set.of(CoreRecordCatalog.CULOMETRO_ID),
                    true,
                    true,
                    false
            );
    
            ExecutionPlan plan = ExecutionPlanner.plan(
                    selection,
                    DependencyInventory.legacyCapabilities(false, true, true, true)
            );
    
            assertTrue(plan.executableItems().stream()
                    .anyMatch(item -> item.child().id().equals(CoreRecordCatalog.CULOMETRO_ID)));
            assertFalse(plan.hasFailures());
        }
    
        @Test
        void planGroupsItemsByFamily() {
            ProcessingSelection selection = new ProcessingSelection(
                    EnumSet.allOf(RecordFamily.class),
                    Set.of(),
                    false,
                    true,
                    false
            );
    
            ExecutionPlan plan = ExecutionPlanner.plan(
                    selection,
                    DependencyInventory.legacyCapabilities(false, true, false, false)
            );
    
            assertEquals(RecordFamily.values().length, plan.byFamily().size());
        }
    }

## src\test\java\it\alterlega\recordsnext\app\model\ModularProcessingModelTest.java

File: src\test\java\it\alterlega\recordsnext\app\model\ModularProcessingModelTest.java

    package it.alterlega.recordsnext.app.model;
    
    import static org.junit.jupiter.api.Assertions.assertEquals;
    import static org.junit.jupiter.api.Assertions.assertFalse;
    import static org.junit.jupiter.api.Assertions.assertThrows;
    import static org.junit.jupiter.api.Assertions.assertTrue;
    
    import java.util.EnumSet;
    import java.util.Set;
    import org.junit.jupiter.api.Test;
    
    class ModularProcessingModelTest {
        @Test
        void missingCaptainSkipsOnlyCaptainSeries() {
            RecordChild child = CoreRecordCatalog.children().stream()
                    .filter(item -> item.id().equals("series.captain-bonus"))
                    .findFirst()
                    .orElseThrow();
    
            ProcessingSelection selection = new ProcessingSelection(
                    EnumSet.of(RecordFamily.SERIES),
                    Set.of(),
                    false,
                    true,
                    false
            );
    
            DependencyEvaluation result = DependencyEvaluator.evaluate(
                    child,
                    selection,
                    Set.of("data.ordered-matches")
            );
    
            assertEquals(OutputStatus.SKIPPED_REQUIRED_DEPENDENCY, result.status());
            assertEquals(Set.of("modifier.captain"), result.missingRequired());
            assertFalse(result.canGenerate());
        }
    
        @Test
        void culometroIsNotSelectedAutomatically() {
            RecordChild child = CoreRecordCatalog.children().stream()
                    .filter(item -> item.id().equals(CoreRecordCatalog.CULOMETRO_ID))
                    .findFirst()
                    .orElseThrow();
    
            ProcessingSelection selection = new ProcessingSelection(
                    EnumSet.of(RecordFamily.THRESHOLDS_LUCK),
                    Set.of(),
                    false,
                    true,
                    false
            );
    
            assertFalse(selection.isChildSelected(child));
            assertEquals(OutputStatus.SKIPPED_NOT_SELECTED, selection.selectionStatus(child));
        }
    
        @Test
        void culometroCanBeExplicitlySelected() {
            RecordChild child = CoreRecordCatalog.children().stream()
                    .filter(item -> item.id().equals(CoreRecordCatalog.CULOMETRO_ID))
                    .findFirst()
                    .orElseThrow();
    
            ProcessingSelection selection = new ProcessingSelection(
                    EnumSet.of(RecordFamily.THRESHOLDS_LUCK),
                    Set.of(CoreRecordCatalog.CULOMETRO_ID),
                    true,
                    true,
                    false
            );
    
            DependencyEvaluation result = DependencyEvaluator.evaluate(
                    child,
                    selection,
                    Set.of("configuration.culometro")
            );
    
            assertTrue(selection.isChildSelected(child));
            assertEquals(OutputStatus.GENERATED_PARTIAL, result.status());
            assertEquals(Set.of("modifier.home-field"), result.missingOptional());
        }
    
        @Test
        void publishingRequiresJavascriptGeneration() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new ProcessingSelection(
                            EnumSet.of(RecordFamily.CLASSICS),
                            Set.of(),
                            false,
                            false,
                            true
                    )
            );
        }
    }

## src\test\java\it\alterlega\recordsnext\app\PipelinePreflightTest.java

File: src\test\java\it\alterlega\recordsnext\app\PipelinePreflightTest.java

    package it.alterlega.recordsnext.app;
    
    import it.alterlega.recordsnext.app.model.CoreRecordCatalog;
    import it.alterlega.recordsnext.app.model.OutputStatus;
    import it.alterlega.recordsnext.app.model.ProcessingSelection;
    import it.alterlega.recordsnext.app.model.RecordFamily;
    import org.junit.jupiter.api.Test;
    
    import java.util.Set;
    
    import static org.junit.jupiter.api.Assertions.assertEquals;
    import static org.junit.jupiter.api.Assertions.assertFalse;
    import static org.junit.jupiter.api.Assertions.assertTrue;
    
    class PipelinePreflightTest {
        @Test
        void legacyClassicsAndRuAreExecutable() {
            var result = PipelinePreflight.evaluate(
                    new ProcessingOptions(true, true, true, false)
            );
    
            assertEquals(2, result.selectedCount());
            assertEquals(2, result.executableCount());
            assertEquals(2, result.completeCount());
            assertEquals(0, result.skippedDependencyCount());
        }
    
        @Test
        void captainSeriesIsSkippedWithoutCaptainDependency() {
            var selection = new ProcessingSelection(
                    Set.of(RecordFamily.SERIES),
                    Set.of("series.captain-bonus"),
                    false,
                    true,
                    false
            );
    
            var result = PipelinePreflight.evaluate(
                    ProcessingOptions.modular(selection)
            );
    
            assertEquals(1, result.selectedCount());
            assertEquals(0, result.executableCount());
            assertEquals(OutputStatus.SKIPPED_REQUIRED_DEPENDENCY,
                    result.relevantItems().getFirst().status());
            assertTrue(result.relevantItems().getFirst()
                    .missingRequired().contains("modifier.captain"));
        }
    
        @Test
        void culometroRemainsOptIn() {
            var ordinary = new ProcessingSelection(
                    Set.of(RecordFamily.THRESHOLDS_LUCK),
                    Set.of(),
                    false,
                    true,
                    false
            );
            var ordinaryResult = PipelinePreflight.evaluate(
                    ProcessingOptions.modular(ordinary)
            );
    
            assertFalse(ordinaryResult.relevantItems().stream()
                    .anyMatch(item -> item.child().id().equals(CoreRecordCatalog.CULOMETRO_ID)));
    
            var easterEgg = new ProcessingSelection(
                    Set.of(RecordFamily.THRESHOLDS_LUCK),
                    Set.of(CoreRecordCatalog.CULOMETRO_ID),
                    true,
                    true,
                    false
            );
            var easterEggResult = PipelinePreflight.evaluate(
                    ProcessingOptions.modular(easterEgg)
            );
    
            assertTrue(easterEggResult.relevantItems().stream()
                    .anyMatch(item -> item.child().id().equals(CoreRecordCatalog.CULOMETRO_ID)));
        }
    }

## src\test\java\it\alterlega\recordsnext\app\ProcessingOptionsIntegrationTest.java

File: src\test\java\it\alterlega\recordsnext\app\ProcessingOptionsIntegrationTest.java

    package it.alterlega.recordsnext.app;
    
    import it.alterlega.recordsnext.app.model.ProcessingSelection;
    import it.alterlega.recordsnext.app.model.RecordFamily;
    import org.junit.jupiter.api.Test;
    
    import java.util.Set;
    
    import static org.junit.jupiter.api.Assertions.assertFalse;
    import static org.junit.jupiter.api.Assertions.assertThrows;
    import static org.junit.jupiter.api.Assertions.assertTrue;
    
    class ProcessingOptionsIntegrationTest {
        @Test
        void legacyConstructorBuildsEquivalentModularSelection() {
            ProcessingOptions options = new ProcessingOptions(true, false, true, false);
    
            assertTrue(options.classic());
            assertFalse(options.ru());
            assertTrue(options.familyEnabled(RecordFamily.CLASSICS));
            assertFalse(options.familyEnabled(RecordFamily.RU));
            assertFalse(options.culometroEnabled());
        }
    
        @Test
        void modularFactoryPreservesAllSelectedFamilies() {
            ProcessingSelection selection = new ProcessingSelection(
                    Set.of(RecordFamily.CLASSICS, RecordFamily.SERIES),
                    Set.of(),
                    false,
                    true,
                    false
            );
    
            ProcessingOptions options = ProcessingOptions.modular(selection);
    
            assertTrue(options.classic());
            assertFalse(options.ru());
            assertTrue(options.familyEnabled(RecordFamily.SERIES));
        }
    
        @Test
        void pipelineRejectsFamiliesNotYetImplementedInsteadOfIgnoringThem() {
            ProcessingOptions options = ProcessingOptions.modular(
                    new ProcessingSelection(
                            Set.of(RecordFamily.SERIES),
                            Set.of(),
                            false,
                            false,
                            false
                    )
            );
    
            assertThrows(
                    IllegalArgumentException.class,
                    () -> RecordsNextPipeline.validateImplementedFamilies(options)
            );
        }
    
        @Test
        void pipelineAcceptsCurrentClassicAndRuBridge() {
            ProcessingOptions options = ProcessingOptions.modular(
                    new ProcessingSelection(
                            Set.of(RecordFamily.CLASSICS, RecordFamily.RU),
                            Set.of(),
                            false,
                            true,
                            false
                    )
            );
    
            RecordsNextPipeline.validateImplementedFamilies(options);
        }
    
        @Test
        void pipelineRejectsCulometroUntilDedicatedExecutorExists() {
            ProcessingOptions options = ProcessingOptions.modular(
                    new ProcessingSelection(
                            Set.of(RecordFamily.THRESHOLDS_LUCK),
                            Set.of("easter-egg.culometro"),
                            true,
                            false,
                            false
                    )
            );
    
            assertThrows(
                    IllegalArgumentException.class,
                    () -> RecordsNextPipeline.validateImplementedFamilies(options)
            );
        }
    }

## src\test\java\it\alterlega\recordsnext\RecordsNextApplicationTest.java

File: src\test\java\it\alterlega\recordsnext\RecordsNextApplicationTest.java

    package it.alterlega.recordsnext;
    
    import static org.junit.jupiter.api.Assertions.assertEquals;
    
    import org.junit.jupiter.api.Test;
    
    class RecordsNextApplicationTest {
    
        @Test
        void usesJava21() {
            assertEquals(21, Runtime.version().feature());
        }
    }

## config\competitions.json

File: config\competitions.json

    {
      "schemaVersion": "2.0",
      "canonicalCompetitionOrder": [
        "serie-a",
        "serie-b",
        "serie-c",
        "coppa-tra-le-coppe",
        "europa-pipps",
        "coppa-di-lega-serie-a",
        "coppa-di-lega-serie-b",
        "coppa-di-lega-serie-c",
        "supercoppa-serie-a",
        "supercoppa-serie-b",
        "supercoppa-serie-c"
      ],
      "canonicalCompetitions": [],
      "seasonCompetitions": [],
      "associationStatuses": [
        "MAPPED",
        "UNMAPPED",
        "AMBIGUOUS",
        "MANUAL",
        "EXCLUDED"
      ]
    }

## config\culometro.json

File: config\culometro.json

    {
      "schemaVersion": "2.0",
      "enabled": false,
      "minimumMatches": 10,
      "normalization": "PER_MATCH",
      "components": [],
      "notes": "Easter egg opzionale. Compilare e abilitare solo su richiesta esplicita."
    }

## config\league.json

File: config\league.json

    {
      "schemaVersion": "2.0",
      "league": {
        "leagueId": "alterlega",
        "leagueName": "AlterLega",
        "currentSeasonId": "2025_2026",
        "defaultLocale": "it-IT",
        "defaultTimeZone": "Europe/Rome"
      }
    }

## config\manifest.example.json

File: config\manifest.example.json

    {
      "schemaVersion": "2.0",
      "program": "RecordsNext by mauz79",
      "programVersion": "2.0.0-dev",
      "generatedAt": null,
      "leagueId": null,
      "currentSeasonId": null,
      "processedSeasons": [],
      "requestedFamilies": [],
      "generatedFamilies": [],
      "generatedChildren": [],
      "skippedChildren": [],
      "generatedFiles": [],
      "culometroGenerated": false
    }

## config\processing.json

File: config\processing.json

    {
      "schemaVersion": "2.0",
      "processing": {
        "families": {
          "classics": {
            "enabled": true,
            "children": "ALL"
          },
          "series": {
            "enabled": true,
            "children": "ALL"
          },
          "ru": {
            "enabled": true,
            "children": "ALL"
          },
          "modifiers": {
            "enabled": true,
            "children": {
              "defence": true,
              "captain": false,
              "homeField": true
            }
          },
          "thresholdsLuck": {
            "enabled": true,
            "children": "ALL"
          }
        },
        "culometro": {
          "enabled": false,
          "configFile": "config\\culometro.json"
        },
        "output": {
          "writeManifest": true,
          "writeCore": true,
          "publishToSite": false
        }
      }
    }

## config\seasons.json

File: config\seasons.json

    {
      "schemaVersion": "2.0",
      "seasons": [
        {
          "seasonId": "2025_2026",
          "startYear": 2025,
          "endYear": 2026,
          "seasonNumber": 21,
          "status": "CURRENT",
          "fcmFile": "E:\\FCM\\data\\AlterLega 2025_2026-21-2025.fcm",
          "fcaFile": "E:\\FCM\\data\\ArchivioA2025SerieA.fca",
          "site": {
            "localRoot": "E:\\fantacalcio\\Lega2025",
            "onlineRoot": "http://www.alterlega.altervista.org/lega2025",
            "matchPageName": "ris.htm",
            "dataAPath": "js\\DataA.js"
          },
          "hasMatchSheets": true,
          "notes": ""
        }
      ]
    }

## config\teams.json

File: config\teams.json

    {
      "schemaVersion": "2.0",
      "canonicalTeams": [],
      "seasonTeams": [],
      "associationStatuses": [
        "MAPPED",
        "UNMAPPED",
        "AMBIGUOUS",
        "MANUAL",
        "EXCLUDED"
      ]
    }

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
    [void]$Builder.AppendLine("- Generatore della bibbia aggiornato con documentazione e sorgenti reali.")
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
    Add-IndentedFileSection -Builder $Builder -Title "Modello dati" -RelativePath "docs\MODELLO_DATI_RECORDSNEXT2.md"
    Add-IndentedFileSection -Builder $Builder -Title "Configurazione" -RelativePath "docs\CONFIGURAZIONE_RECORDSNEXT2.md"
    Add-IndentedFileSection -Builder $Builder -Title "Stato implementazione" -RelativePath "docs\STATO_IMPLEMENTAZIONE_RECORDSNEXT2.md"
    Add-IndentedFileSection -Builder $Builder -Title "Changelog" -RelativePath "CHANGELOG.md"
    
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

- src\main\java\it\alterlega\recordsnext\app\config\ConfiguredPipelineRunner.java
- src\main\java\it\alterlega\recordsnext\app\config\MiniJson.java
- src\main\java\it\alterlega\recordsnext\app\config\ProcessingConfigLoader.java
- src\main\java\it\alterlega\recordsnext\app\manifest\ManifestJsWriter.java
- src\main\java\it\alterlega\recordsnext\app\manifest\ManifestMetadata.java
- src\main\java\it\alterlega\recordsnext\app\manifest\ManifestPublishingSupport.java
- src\main\java\it\alterlega\recordsnext\app\model\CoreRecordCatalog.java
- src\main\java\it\alterlega\recordsnext\app\model\DependencyEvaluation.java
- src\main\java\it\alterlega\recordsnext\app\model\DependencyEvaluator.java
- src\main\java\it\alterlega\recordsnext\app\model\DependencyInventory.java
- src\main\java\it\alterlega\recordsnext\app\model\DependencyType.java
- src\main\java\it\alterlega\recordsnext\app\model\ExecutionPlan.java
- src\main\java\it\alterlega\recordsnext\app\model\ExecutionPlanItem.java
- src\main\java\it\alterlega\recordsnext\app\model\ExecutionPlanner.java
- src\main\java\it\alterlega\recordsnext\app\model\OutputStatus.java
- src\main\java\it\alterlega\recordsnext\app\model\ProcessingSelection.java
- src\main\java\it\alterlega\recordsnext\app\model\RecordChild.java
- src\main\java\it\alterlega\recordsnext\app\model\RecordDependency.java
- src\main\java\it\alterlega\recordsnext\app\model\RecordFamily.java
- src\main\java\it\alterlega\recordsnext\app\PipelineConfig.java
- src\main\java\it\alterlega\recordsnext\app\PipelinePreflight.java
- src\main\java\it\alterlega\recordsnext\app\ProcessingMode.java
- src\main\java\it\alterlega\recordsnext\app\ProcessingOptions.java
- src\main\java\it\alterlega\recordsnext\app\RecordsNextPipeline.java
- src\main\java\it\alterlega\recordsnext\app\RecordsNextPreparationService.java
- src\main\java\it\alterlega\recordsnext\CalendarSourceManager.java
- src\main\java\it\alterlega\recordsnext\CanonicalSchemaProbe.java
- src\main\java\it\alterlega\recordsnext\CanonicalViews.java
- src\main\java\it\alterlega\recordsnext\ConfigurationSchema.java
- src\main\java\it\alterlega\recordsnext\ConfrontiStoriciCalendarImporter.java
- src\main\java\it\alterlega\recordsnext\DatabaseInspector.java
- src\main\java\it\alterlega\recordsnext\gui\FcmSeasonDetector.java
- src\main\java\it\alterlega\recordsnext\gui\HistoricalMappingDialog.java
- src\main\java\it\alterlega\recordsnext\gui\HistoricalMappingRepository.java
- src\main\java\it\alterlega\recordsnext\gui\RecordsNextApp.java
- src\main\java\it\alterlega\recordsnext\gui\RecordsNextConfigurationDialog.java
- src\main\java\it\alterlega\recordsnext\gui\SeasonConfigurationRepository.java
- src\main\java\it\alterlega\recordsnext\PlayoffRecordsExporter.java
- src\main\java\it\alterlega\recordsnext\RawSqliteImporter.java
- src\main\java\it\alterlega\recordsnext\Records2026ClassicJsExporter.java
- src\main\java\it\alterlega\recordsnext\Records2026RuJsExporter.java
- src\main\java\it\alterlega\recordsnext\Records2026SitePublisher.java
- src\main\java\it\alterlega\recordsnext\RiserveUfficioArchiveBuilder.java
- src\main\java\it\alterlega\recordsnext\SeasonMappingConfigurator.java
- src\main\java\it\alterlega\recordsnext\SeasonNormalizedBatchExporter.java
- src\main\java\it\alterlega\recordsnext\SeasonNormalizedExporter.java
- src\main\java\it\alterlega\recordsnext\SeasonRecordsArchiveBuilder.java
- src\main\java\it\alterlega\recordsnext\SeasonRegistry.java
- src\main\java\it\alterlega\recordsnext\SerieAQueryProbe.java
- src\main\java\it\alterlega\recordsnext\SerieARoundProbe.java
- src\main\java\it\alterlega\recordsnext\SqliteAudit.java
- src\test\java\it\alterlega\recordsnext\app\config\ProcessingConfigLoaderTest.java
- src\test\java\it\alterlega\recordsnext\app\manifest\ManifestJsWriterTest.java
- src\test\java\it\alterlega\recordsnext\app\manifest\ManifestPublishingSupportTest.java
- src\test\java\it\alterlega\recordsnext\app\model\ExecutionPlannerTest.java
- src\test\java\it\alterlega\recordsnext\app\model\ModularProcessingModelTest.java
- src\test\java\it\alterlega\recordsnext\app\PipelinePreflightTest.java
- src\test\java\it\alterlega\recordsnext\app\ProcessingOptionsIntegrationTest.java
- src\test\java\it\alterlega\recordsnext\RecordsNextApplicationTest.java
- config\competitions.json
- config\culometro.json
- config\league.json
- config\manifest.example.json
- config\processing.json
- config\seasons.json
- config\teams.json
- tools\Create-RecordsNext2WorkingCodeMd.ps1
- tools\Initialize-RecordsNext2Project.ps1

## Fine documento
