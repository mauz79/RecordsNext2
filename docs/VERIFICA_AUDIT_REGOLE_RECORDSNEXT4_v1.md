# RecordsNext 4.0 - Verifica locale Fase 3B2 v1

## Base di test

Database sandbox della 3.2 fornita, SHA-256 `57923f4e75986c2b81533e4beef15b9133635258d7de1d36827a1bdbe576671b`.

## Risultati locali osservati

| Indicatore | Risultato |
|---|---:|
| Stagioni FCM | 21 |
| Configurazioni `Competizione` | 180 |
| Incontri (non necessariamente giocati) | 13.072 |
| Tabellini | 22.265 |
| Tabellini con incontro collegato | 22.097 |
| Tabellini senza incontro presente | 168 |
| Tabellini associati a incontro con squadra 0 | 23 |
| Incontri con squadra 0 (anche senza tabellino) | 1.334 |
| Test Python | 8/8 superati |

I valori 168 e 23 sono distinti; i 1.334 incontri con squadra 0 includono incontri non giocati o senza tabellino e non vanno interpretati come 1.334 errori. Lo studio dei turni di riposo resta separato.

### Esempi che impediscono una regola unica

- 2007/08: tre competizioni, sostituzioni configurate rispettivamente 3, 8, 11; RU 0, 99, 99.
- 2019/20: dodici competizioni con 5 sostituzioni e 3 RU.
- 2023/24: alcune competizioni hanno 11 sostituzioni e 11 RU; altre 3.
- 2025/26: le dodici competizioni hanno 3 sostituzioni, ma Play Off - Play Out registra 0 RU e le altre 1.
- 2026/27: 780 incontri presenti, zero tabellini: la presenza di un incontro non prova una gara giocata.

Il parser mantiene il valore originale e non lo interpreta come un diritto a recuperare un voto.

## Ancora da certificare

Il runner operativo e la generazione della bibbia v9 devono essere verificati sul worktree reale Windows tramite lo ZIP prodotto dall'utente. Fino ad allora questa verifica e **locale**, non una certificazione Windows.

Nessuna simulazione o pubblicazione.
