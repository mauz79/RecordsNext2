# RecordsNext 4.0 - Verifica Fase 0 v1

Data: 2026-10-31. Stato: **audit diagnostici completati; verifiche di dominio ancora aperte**.

## Provenienza e riproducibilita

- Ambiente utente: `D:\DEV_APPS\RecordsNext4.0`, branch `feature/recordsnext4-players`, HEAD `f9bf30323d9204d70e91e242bddfc72c2060ecf9`.
- Output originale ricevuto e controllato: `RecordsNext4_Fase0_OUTPUT_v4_20261031_212435_728403.zip`; 24 file, archivio ZIP integro, manifest `AUDIT_ESEGUITI`, nessun `ERRORE_v4.txt`.
- Database: copia esatta `recordsnext-sandbox.db` ricevuta nell'archivio `RecordsNext2.0.zip`, SHA-256 `57923f4e75986c2b81533e4beef15b9133635258d7de1d36827a1bdbe576671b` (coincide con il manifest dell'output utente).
- Nessuna modifica al database, agli originali FCM/FCA, al codice Java o ai JS pubblicati.

## Risultati verificati

| Verifica | Risultato | Nota |
| --- | ---: | --- |
| Stagioni raw FCM/FCA | 21 | archivio SQLite importato |
| Famiglie di tabelle esaminate | 12 | incluse anagrafiche, voti, formazioni, tabellini e incontri reali |
| Rilevazioni di presenza tabella | 252/252 presenti | **correzione v2 del probe** |
| Relazioni stagionali verificate | 105 | 90 OK, 15 da verificare |
| Righe `Tabellino` con ID incontro assente | 168 | in 15 stagioni, causa non ancora determinata |
| Codici Gazzetta ripetuti nella stessa stagione | 0 | non esclude riutilizzi attraverso stagioni |
| Codice giocatore 100843 (Lautaro Martinez) | 9 stagioni | audit anagrafico senza anomalie rilevate |
| Codice 12302 | 3 stagioni | audit richiede revisione manuale: Nicola Ascoli / Roberto Crivello |

## Correzione necessaria al probe v1

In `tools/Probe-RecordsNext4Fonti-v1.py` la costante `FAMILIES` conteneva la forma errata `fca_incontroina`. Il nome delle tabelle raw nel database e `fca_incontraina` (es. `raw_2025_2026_fca_incontraina`). Per questo il report v1 segnalava falsamente 21 tabelle mancanti.

La **v2** modifica soltanto l'identificativo della famiglia e versiona il prefisso dei report. E stata validata con `python -m py_compile` ed eseguita sulla copia esatta del database sandbox. Risultato: 252/252 tabelle presenti; gli altri 105 esiti referenziali restano invariati.

Non riutilizzare il report di copertura v1 per affermare che la tabella delle partite reali e assente. Conservare il v1 soltanto come documentazione storica del difetto.

## Distinzioni confermate e questioni aperte

- `GiocatoreA.ID` e locale alla stagione; il codice 100843 e quello persistente usato per la ricerca storica, ma la posizione esatta del codice nelle tabelle FCM sorgenti resta da identificare con prova diretta.
- `GiocaIn` non equivale automaticamente a presenze reali a voto; `Formazione` non equivale automaticamente a prestazioni conteggiate.
- `Incontro` con squadra a zero e `Tabellino.IDIncontro` senza corrispondenza sono fenomeni distinti. Non classificare i 168 casi come riposi o errori senza controllo sugli originali.
- Gli stati `Entrato`, `EntratoRiserva`, `Pos`, `Rig`, SV, RU e i criteri per punti non sfruttati richiedono verifica con regole e casi storici.

## Stato operativo

La parte **tecnica diagnostica** della Fase 0 e stata eseguita; non risulta alcuna modifica produttiva o release 4.0.0. Le questioni di dominio vanno affrontate nei test dell'estrattore e prima di calcolare i record.

**Nessun comando da eseguire ora sul PC:** la correzione v2 e stata verificata sul database gia fornito. Il prossimo kit di sviluppo dovra integrare il probe v2 e questa documentazione in `D:\DEV_APPS\RecordsNext4.0` con nuovi nomi versionati e test di non regressione.
