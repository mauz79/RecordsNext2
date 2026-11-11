# RecordsNext 4 — Piano operativo v6

Base: `v3.2.0`, commit `f9bf30323d9204d70e91e242bddfc72c2060ecf9`; sviluppo separato `D:\DEV_APPS\RecordsNext4.0`. Versione target 4.0.0 **non rilasciata**.

## Obiettivo

Archiviare tutte le prestazioni reali di ogni calciatore per stagione/giornata FCA, e tutte le partecipazioni fantasy per fantasquadra, competizione e incontro; produrre record individuali reali, fantasy e non sfruttati stagionali e all-time con conteggi non duplicati, provenienza e cut-off multisito.

## Stato effettivo al 31/10/2026

| Fase | Stato | Evidenza |
|---|---|---|
| 0. Inventario, audit storico, identita | Audit eseguito; eccezioni di dominio aperte | 252/252 tabelle raw e prova Fase 0 Windows; correttivo probe v2 |
| 1. Archivio reale | **Estrattore Python v1 implementato e collaudato su DB copia; NON integrato Java** | `Estrai-RecordsNext4Reali-v1.py`; 21 export, 617.310 righe, 7 test |
| 2. Archivio fantasy | DA INIZIARE | `Formazione`, `Tesserato`, `Tabellino` da normalizzare |
| 3. Record individuali | DA INIZIARE | Definizioni statistiche e regolamentari ancora aperte |
| 4. JS, viste HTML, GUI | DA INIZIARE | 9 output 3.2 da preservare; flat shard in `js` |
| 5. Regressioni, bibbia, release | DA INIZIARE | Nessuna release, nessun commit automatico |

## Regole immutabili

- FCM/FCA originali, SQLite operativo 3.2, pipeline Java, HTML e JS pubblici non si modificano in questa fase.
- `GiocatoreA.ID` e stagionale; `CodiceGazza` e il codice ricercabile ma richiede audit di anomalie/riutilizzi. Campo FCM corrispondente ancora da accertare; non imporre associazioni fittizie.
- `GiocaIn` non equivale a presenza reale; `VOTO1=0` o `-1` conservati senza reinterpretazioni automatiche; `Storico` ripetuto fra FCA non si somma.
- Incontri reali non abbinabili e tabellini fantasy senza `Incontro` vengono mantenuti e segnalati separatamente, senza classificazione automatica come errore o riposo.
- Report e pacchetti in `D:\DEV_APPS\downloads`, sorgenti/doc in `D:\DEV_APPS\RecordsNext4.0`, nomi sempre versionati; nessun overwrite.
- Nessun commit/merge/tag finche i risultati non sono stati verificati insieme.

## Sviluppo successivo

1. Ricevere ZIP verifica locale della Fase 1 v1 e confrontare output con i risultati gia ottenuti sul DB di copia.
2. Audit delle giornate e dei punteggi speciali, ricerca delle squadre reali non collegate con `IncontroInA`.
3. Stabilire priorita fra `Statistiche`, `Storico`, `GiocaIn` per record e periodi storici mancanti, senza imputazioni.
4. Progettare e implementare schema canonico reali stabile e incrementale nella pipeline 4.0, con migrazioni e benchmark.
5. Avviare Fase 2 fantasy mantenendo i vincoli di riposi, RU e sottocompetizioni.

## Documentazione versionata

- `docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md`: base integrale, invariata.
- `docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v4.md`: bibbia Fase 0 corretta.
- `docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v5.md`: bibbia incrementale Fase 1, generata dal codice installato e con SHA.
- `docs/SPECIFICA_ESTRAZIONE_REALE_RECORDSNEXT4_v1.md`: modello campi e limiti Fase 1.
- `docs/REGISTRO_DECISIONI_RECORDSNEXT4_v5.md`: decisioni/esiti aperti.
- `docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v5.md`: attivita e verifiche.
