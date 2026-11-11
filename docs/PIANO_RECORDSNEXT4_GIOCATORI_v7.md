# RecordsNext 4.0 — Piano operativo v7

Base verificata `v3.2.0`, commit `f9bf30323d9204d70e91e242bddfc72c2060ecf9`. Worktree `D:\DEV_APPS\RecordsNext4.0`, ramo `feature/recordsnext4-players`. Release 4.0 NON esistente.

## Obiettivo approvato

Dati reali di ogni calciatore per ogni anno; dati fantasy per fantasquadra, competizione e partita; classifiche reali e fantasy per stagione, competizione, squadra e all-time. Il codice stabile (`CodiceGazza` nella tabella FCA) va distinto dall'ID FCA stagionale. Nessuna somma di prestazioni reali quando più competizioni fantasy usano la stessa giornata.

## Stato verificato al 31 ottobre 2026

| Ambito | Stato |
|---|---|
| Fase 0 — sorgenti/identità | Audit eseguito e documentato; identità storiche anomale e casi `Tabellino` non collegati restano aperti. |
| Fase 1 — dati reali | Estrattore Python v1 verificato **anche su Windows**, 7/7 test e piloti reali, SHA sandbox invariato. NON integrato nella pipeline Java. |
| Fase 2 — dati fantasy | Prototipo Python v1 in sola lettura, 8 test, 21 stagioni: 552.025 righe `Formazione` trovano slot Tabellino univoco e `GiocaIn` corrispondente. **Test locale Python effettuato; esecuzione Windows da certificare**. |
| Fase 3 — record | Non iniziata; metriche e denominatori ancora da formalizzare. |
| Fase 4 — Java/JS/GUI | Non iniziata; non sono stati toccati pipeline, siti, facade o GUI 3.2. |
| Fase 5 — regressione, bibbia e release | Non iniziata; nessun commit, merge o tag. |

## Passo successivo

1. Verifica del pacchetto Windows della Fase 2 v1 e della bibbia v6.
2. Studio semanticamente rigoroso di `ENTRATO`, `ENTRATORISERVA`, RU, voti non numerici e moduli/sostituzioni prima di calcolare punti non sfruttati.
3. Audit della permanenza in rosa e dei trasferimenti (snapshot `Tesserato` non equivale a cronologia).
4. Progettare tabelle canoniche SQL dei dati reali/fantasy, importazione incrementale, deduplicazione idempotente e benchmark (fase successiva, NON ancora realizzata).
5. Solo dopo definire aggregazioni e record reali/fantasy/all-time con filtro, link tabellino corretto e cutoff multisito; preservare `fcmRecordsNext_*.js` esistenti e shard flat `js` della 3.2.

## Discipline operative

- Solo file nuovi con suffissi versionati, nessuna sovrascrittura inconsapevole; non modificare 3.2, non pubblicare siti.
- Codice, test, SHA-256, piani, decisioni, diario e bibbia sempre verificabili; la bibbia 3.2 resta invariata.
- ZIP/report in `D:\DEV_APPS\downloads`, sorgenti e documenti in `D:\DEV_APPS\RecordsNext4.0`.
- Riposi fantasy e 168 righe di tabellino senza riferimento `Incontro` non sono automaticamente errori e non devono essere confusi con i normali slot collegati a `Formazione`.
