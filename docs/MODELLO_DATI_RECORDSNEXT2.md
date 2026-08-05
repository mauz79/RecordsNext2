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

### Stato implementato Classici

`fcmRecordsNext_Classics.js` è generato dalla pipeline e pubblica:

- `schemaVersion: "2.0"`;
- `familyId: "classics"`;
- metadata con numero di stagioni e recordset;
- `seasonAggregates` derivati dagli archivi Classici consolidati 1.0.2;
- stato `GENERATED_COMPLETE`.

Durante la migrazione resta generato anche il file legacy `records2026.recordstagionali.classic.js`.

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


## Output famiglia Serie implementato

`fcmRecordsNext_Series.js` espone `window.fcmRecordsNextSeries`. La prima versione riusa gli archivi normalizzati 1.0.2 e pubblica le sezioni disponibili: imbattibilita, serie Capitano e serie clean sheet. Lo stato e `GENERATED_PARTIAL` finche non saranno aggiunte vittorie, pareggi, sconfitte e serie senza vittorie.


## Output Modificatori implementato

`fcmRecordsNext_Modifiers.js` espone `window.fcmRecordsNextModifiers` e contiene le sezioni `modDifesaMax`, `modDifesaTotaleSquadre`, `capitanoVolteSquadre` e `capitanoTotaleSquadre`. Il Fattore Campo resta escluso finche non viene implementato il relativo calcolo dedicato.


## Aggiornamento Serie complete v2

La famiglia Serie include vittorie consecutive, pareggi consecutivi, sconfitte consecutive, imbattibilita, serie senza vittorie, serie Capitano e serie clean sheet. Stato: `GENERATED_COMPLETE`.
