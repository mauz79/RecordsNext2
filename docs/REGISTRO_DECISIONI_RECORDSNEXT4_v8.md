# RecordsNext 4.0 - Registro decisioni v8

Data: 2026-11-01. Questo registro e additivo: D-001 - D-044 rimangono nelle versioni precedenti (`v2`-`v7`). Le decisioni aperte non sono state risolte implicitamente.

| ID | Stato | Decisione / evidenza |
|---|---|---|
| D-045 | CONFERMATA: test Windows 3A | Lo ZIP 3A consegnato riporta 9/9 test, 21 stagioni, 552.025 righe `Formazione`; tre SHA-256 dei report coincidenti; SHA del DB sandbox prima/dopo identico. Bibbia v7 generata. |
| D-046 | CONFERMATA: osservazione diretta | L'ordine `Tabellino.LISTA` distingue il blocco dei primi undici slot dagli elementi successivi; e necessario rilevarlo accanto a `ENTRATO` e `ENTRATORISERVA`, senza trattare i flag come equivalenti. |
| D-047 | CONFERMATA: caso sorgente | In stagione 2012/13, incontro FCM 3256, squadra 3, undici giocatori nei primi undici slot hanno `ENTRATO=0` ma `Tabellino.TOTALESQUADRAESISTE=1`; quindi `ENTRATO` non e una fonte autonoma affidabile per stabilire il contributo. |
| D-048 | IMPLEMENTATA: prototipo 3B1 | L'audit legge `Formazione`, `Incontro`, `Tabellino`, `GiocatoreA` in sola lettura, incrocia gli slot, genera combinazioni e campioni, senza calcolare punti. |
| D-049 | CONFERMATA: controllo locale | Le 552.025 righe `Formazione` analizzate trovano ciascuna uno slot personale nel tabellino; gli slot speciali 0/-1 sono contabilizzati separatamente. |
| D-050 | APERTA | Prima di denominare `conteggiato` o `voto utilizzato` lo slot iniziale bisogna verificare le eccezioni RU, numero ridotto di giocatori, modificatori e stati speciali. Per ora il nome tecnico e `PRIMI11`. |
| D-051 | CONFERMATA: vincolo | 168 tabellini con ID incontro assente, 178 senza `Formazione`, 23 relativi a incontri con squadra zero: categorie non esclusive, non trasformate automaticamente in errori. Possibili turni di riposo da esaminare a parte. |
| D-052 | APERTA | Definire separatamente voto presente, voto utilizzato, voto inutilizzato dalla panchina, voto fuori formazione, cambi disponibili e punti recuperabili con regole vere della competizione/stagione. |
| D-053 | APERTA | Verificare gli anni con configurazioni di sostituzioni e RU differenti prima dei record sui punti persi. Le analisi RU personali 3.2 sono diagnostiche, non record pubblici. |

*Nota*: i contatori del prototipo 3B1 sono ancora da certificare con lo ZIP Windows. Non alterare registri preesistenti.
