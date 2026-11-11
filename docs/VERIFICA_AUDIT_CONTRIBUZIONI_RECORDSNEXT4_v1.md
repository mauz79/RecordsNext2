# RecordsNext 4.0 - Verifica tecnica Fase 3B1 v1

## Verifica sulla copia SQLite 3.2 ricevuta (ambiente Linux)

- 21 stagioni, 552.025 `Formazione`, 22.265 righe `Tabellino`.
- 552.025/552.025 righe `Formazione` coperte da slot personali univoci del tabellino.
- 237.686 giocatori negli undici slot iniziali (occorrenze giocatore-partita; non calcolare ancora presenze ufficiali).
- 22.028 panchinari iniziali fra i primi undici slot; 27.319 titolari iniziali oltre i primi undici slot.
- 5.421 slot speciali (ID <= 0) nei primi undici slot; non sono calciatori.
- 40 casi `PRIMI11_ENTRATO_ZERO`, inclusi i 11 dell'incontro 3256 del 2012_2013 con totale squadra presente.
- 43 casi `FLAG_RISERVA_FUORI_PRIMI11`, coerenti con l'anomalia distinta della Fase 3A.
- 168 tabellini con incontro assente; 178 tabellini senza formazione; 23 tabellini associati a incontri con una squadra zero. Queste categorie si sovrappongono potenzialmente.
- 7 test automatici superati (4 test sintetici e 3 regressioni su database).
- Il database e stato aperto solo con SQLite `mode=ro` e `PRAGMA query_only=ON`.

## Da certificare con esecuzione Windows

Il runner installa solo file versionati nuovi, controlla l'integrita della 3.2 e del worktree 4.0, ripete i test e il probe sul sandbox, genera la bibbia v8 e produce uno ZIP di verifica. I risultati locali non sostituiscono lo ZIP Windows.

## Ancora aperto

Semantica definitiva di conteggio dei voti, eccezioni RU e recuperabilita dei punti secondo regolamento per competizione/stagione. I tabellini senza incontro e i riposi non vengono trasformati in record giocatore.
