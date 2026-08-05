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
