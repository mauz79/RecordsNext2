# RecordsNext 4.0 — Bibbia incrementale v5 (Fase 1 reali)

> Questa NON e la release 4.0.0; la pipeline Java 3.2 e invariata.
> Conservare intatta la bibbia 3.2 e le bibbie 4.0 precedenti.

## Riferimenti verificabili

- Commit base 3.2: `f9bf30323d9204d70e91e242bddfc72c2060ecf9`
- Branch: `feature/recordsnext4-players`
- HEAD al momento della generazione: `f9bf30323d9204d70e91e242bddfc72c2060ecf9`
- Bibbia 3.2 SHA-256: `91ecb0006d6d1c6fda501c7243ba65ec2f0625c40e93394079238eac558a7da3`
- Bibbia 4.0 v4 SHA-256: `ef2edf9a3fe4d2f3fbaad3e42215df595c13d18a24098c4725c17ec18c41015b`

## Stato tecnico

- Estrattore FCA read-only Python v1 funzionante sulla copia DB.
- Tutte le 21 stagioni: 16.245 giocatori stagionali, 617.310 righe GiocaIn.
- 7 test automatici superati sulla copia DB; esito Windows da verificare.
- Nessuna scrittura SQLite, modifica FCM/FCA, commit, JS, HTML, GUI o release.
- Mancato abbinamento con IncontroInA non significa errore; vedere specifica.

## Codice e documentazione aggiunti (contenuto integrale)

### `docs/SPECIFICA_ESTRAZIONE_REALE_RECORDSNEXT4_v1.md`

SHA256: `4b12814e5f96e55bf6c46da41f3ff3243462a668d1b74fd3a7977b8e49b6c537`

~~~~markdown
# RecordsNext 4.0 — Specifica Fase 1: archivio prestazioni reali v1

Data: 2026-10-31. Stato: **implementazione sperimentale verificata sulla copia SQLite fornita**; **NON ancora integrata nella pipeline Java, GUI o pubblicazione**. Non e una release 4.0.

## Scopo e limiti

Esportare tutte le informazioni FCA disponibili per stagione e giocatore, comprese le giornate senza voto, senza usare FCM/Formazione come filtro. I giocatori non tesserati da fantasquadre restano inclusi nella modalita completa `--all`. Nessuna attribuzione inventata di presenza in campo o contributo fantasy.

### Identita

- `GiocatoreA.ID`: ID FCA **locale** alla stagione. Identifica le join di `GiocaIn.IDGiocatore`, `Statistiche.ID`, `Storico.IDGiocatore`.
- `GiocatoreA.CODICEGAZZA`: codice storico indicato negli FCA e coerente col codice foto in molti casi. Il campo esatto nell'FCM resta **da verificare** secondo D-003/Q-001.
- La query per codice `100843` non dichiara automaticamente un'identita canonica verificata. Se nome/data nascita differiscono tra stagioni il file contiene `avvertenzeIdentita`, non fonde l'anagrafica.

### Famiglie FCA (otto)

| Tabella | Uso nell'output |
|---|---|
| `GiocatoreA` | Anagrafica stagionale completa, **tutte** le colonne |
| `GiocaIn` | Una riga per `IDGiocatore` e `Giornata`, mantenuta integralmente |
| `Punteggio` | Oggetto originale completo referenziato da `GiocaIn.IDPunteggio` |
| `Statistiche` | Riepilogo **dello snapshot FCA di quella stagione**, non origine di presenza inventata |
| `Storico` | Istantanea di valori annui (inclusi anni precedenti) ripetuta tra FCA: **MAI sommare across snapshot** |
| `SquadraDiA` | Nome della squadra reale da `GiocaIn.IDSquadra` |
| `GiornataDiA` | Stato raw `GIOCATA` e altri dati della giornata, senza equipararlo a presenza |
| `IncontroInA` | Candidati incontro reale per `(giornata, squadra reale)`; 0/1/>1 risolti separatamente |

La riga `GiocaIn` esiste anche per giornate non disputate. `Punteggio.VOTO1=0` e `-1` si mantengono distinti; sono etichette di osservazione, non una regola completa di SV. Conserviamo `VOTO1/2/3` e gli altri valori di punteggio **cosi come presenti** senza selezionare una fonte voti ufficiale e senza ricalcolare il fantavoto.

### Modello dati JSON

- `formato = RecordsNext4.PlayerReal.v1` (per `--code`): `codiceRichiesto`, `avvertenzeIdentita`, `coperturaStagioni`, `giocatoriStagionali`.
- Per ciascun giocatore stagionale: `stagione`, `giocatoreIdFca`, `codiceStoricoSorgente`, `giocatoreAFca`, `statisticheFca[]`, `storicoFcaSnapshot[]`, `giornateReali[]`.
- Per ciascuna giornata: `giornata`, `giornataFca`, `squadraRealeId/nome`, `giocaInFca`, `punteggioFca`, `statoVoto1`, `incontroReale`.
- `incontroReale.stato = COLLEGATO | NON_COLLEGATO | AMBIGUO`: non collegato NON significa errore del file e non si forza il match.
- `--all` crea un file `.jsonl.gz` per stagione con una riga JSON per giocatore e la stessa struttura stagionale; manifest separato contiene conteggi e SHA-256 di output.

Il file `_MANIFEST` ha hash del DB e degli output per ripetibilita. Il database viene aperto come URI `mode=ro` e PRAGMA `query_only=ON`.

## Test sulla copia SQLite dell'utente

- 21 stagioni raw FCA: 2006/07–2026/27.
- Estratti in modalita completa: **16.245 anagrafiche stagionali**, **617.310 righe `GiocaIn`** tutte conservate, **0 IDPunteggio irrisolti** (con riferimento valido nel `Punteggio` stagionale).
- `IncontroInA`: 143.258 righe `GiocaIn` non risolte univocamente con il semplice match su giornata e squadra; **non classificare come dato errato**. Da analizzare struttura e provenienza, non confondere con riposi fantasy `Incontro`/`Tabellino`.
- `100843`: Lautaro, 9 anagrafiche stagionali (2018/19–2026/27), 342 righe `GiocaIn`. Nell'FCA 2025/26 `Statistiche.GolFatti=17`, `Assist=6`, in accordo con le somme degli eventi giornalieri `Punteggio.GolFatti1` e `Punteggio.Assist`. Questo confronto **non** valida ogni schema storico né definisce il campo voto ufficiale.
- `12302`: 3 anagrafiche stagionali, identita incompatibili → `IDENTITA_DA_VERIFICARE`. Nessun calcolo all-time eseguito.
- 7 test automatici standard library superati sulla copia SQLite. Test Windows: da confermare con ZIP utente.

## Limiti noti e prossime fasi

1. Stagione manuale 2005/06 non presente fra le raw SQLite: serviranno un'importazione distinta o utilizzo delle sorgenti FCA originali per includerla; NON e inclusa nei conteggi di questa fase.
2. L'istantanea SQLite non rappresenta necessariamente lo stato piu recente dei file FCA sul disco, specialmente 2026/27.
3. `Storico` non e un archivio assoluto deduplicato: serviranno criteri di priorita per record storici in anni senza FCA originale.
4. La forma JSON v1 e un **archivio di ricerca**: schema canonico SQL, output JS, GUI, ricerca veloce e record sono fasi successive.
5. Non generiamo media voto, presenze ufficiali o punti sprecati senza fonte/regole concordate.
6. Il DB `recordsnext-sandbox.db` e utilizzato in sola lettura. I due programmi possono presentare differenze rispetto al file originale, quindi i confronti sono fondati sulla **copia** fornita.

## Comandi v1

Per un codice su tutte le stagioni:

```powershell
py -3 "D:\DEV_APPS\RecordsNext4.0\tools\Estrai-RecordsNext4Reali-v1.py" --database "D:\DEV_APPS\RecordsNext2.0\data\database\recordsnext-sandbox.db" --output-dir "D:\DEV_APPS\downloads" --code 100843
```

Per tutti i calciatori di una sola stagione:

```powershell
py -3 "D:\DEV_APPS\RecordsNext4.0\tools\Estrai-RecordsNext4Reali-v1.py" --database "D:\DEV_APPS\RecordsNext2.0\data\database\recordsnext-sandbox.db" --output-dir "D:\DEV_APPS\downloads" --all --season 2025_2026
```

Per tutte le stagioni, omettere `--season` con `--all`; **non e necessario** durante il primo collaudo sul PC: sono gia stati testati 21 esporti sul database caricato e richiede piu spazio/tempo per l'esecuzione.
~~~~

### `docs/PIANO_RECORDSNEXT4_GIOCATORI_v6.md`

SHA256: `89b906e23144df6892661bad39c20f5da2f0400c9f1717003a61ccf86d68f2dd`

~~~~markdown
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
~~~~

### `docs/REGISTRO_DECISIONI_RECORDSNEXT4_v5.md`

SHA256: `b9eca4897782f12e4efd6f246b28b368fd1574a5f662704371490c84be91807b`

~~~~markdown
# RecordsNext 4 — Registro decisioni v5

Fonte: versioni precedenti `REGISTRO_DECISIONI_RECORDSNEXT4_v2/v3/v4.md` **mantenute**, questa versione le integra senza riscrivere la storia. Stato CONFERMATA = requisito utente o test tecnico; PROPOSTA = scelta reversibile in attesa di approvazione; APERTA = da accertare.

## Decisioni consolidate (D-001–D-018)

Restano applicabili le decisioni registrate nella v4: sviluppo separato dalla 3.2, ID FCA stagionale, codice storico distinto, non alterare DB/fonti e HTML/JS, non duplicare dati reali per competizioni fantasy, output in `D:\DEV_APPS\downloads`, file versionati, preservare riposi e tabellini non collegati, bibbia incrementale, correzione probe `incontraina`.

## Nuove decisioni/verifiche

| ID | Stato | Contenuto | Riscontro |
|---|---|---|---|
| D-019 | CONFERMATA | Estrarre tutte le righe `GiocaIn` per stagione/giocatore, non solo i tesserati nelle nostre fantasquadre | Requisito utente; export reale v1 |
| D-020 | CONFERMATA | Mai identificare automaticamente `GiocaIn` con una presenza effettiva: conservarne sorgente e stato `GiornataDiA` | 2026/27: 38 righe per Lautaro, `Statistiche.Presenze=2` nell'istantanea |
| D-021 | CONFERMATA | `Punteggio.VOTO1` 0 e -1 restano valori distinti, e i tre voti sono salvati senza decidere la fonte ufficiale | Schema FCA e casi reali |
| D-022 | CONFERMATA | `Storico` e uno snapshot annuale ripetibile in archivi FCA diversi: vietata la somma tra snapshot per all-time | Confronto storico Lautaro |
| D-023 | CONFERMATA | Per un codice con anagrafiche incompatibili, segnalare e non unire automaticamente le stagioni nella carriera canonica | Codice 12302, test regressione |
| D-024 | PROPOSTA TECNICA, IMPLEMENTATA NEL PROTOTIPO | Archivio esplorativo JSON/JSONL.gz con tutti i valori raw + riferimenti contestuali; non e ancora lo schema canonico del motore | Estrattore reale v1, test su copia DB |
| D-025 | PROPOSTA TECNICA, IMPLEMENTATA NEL PROTOTIPO | `IncontroInA` collegato solo con candidato unico per giornata/squadra; candidati 0 o multipli restano non risolti | 143.258 righe su 617.310 non risolte; non equivalgono ai riposi fantasy |
| D-026 | CONFERMATA | Test e bibbia devono documentare esattamente codice eseguito, SHA e risultati; nessuna modifica alla pipeline 3.2 | Richiesta utente; generatore bibbia v5 |

## Questioni aperte (v4 + Fase 1)

- Q-001: individuare e verificare il campo preciso che conserva il codice permanente nel file FCM, differenziandolo dall'ID FCA locale.
- Q-002: definire trattamento delle collisioni `CodiceGazza` e delle anagrafiche mutate (ad es. 12302).
- Q-003/Q-009: distinguere tipologie dei riposi fantasy e tabellini con ID incontro assente (168 righe audit).
- Q-004: semantica esatta schieramento/riserva/entrato, SV, RU, bonus e modificatori, per la futura Fase 2.
- Q-005: definizione punti non sfruttati, regole e simulazione formazioni valide.
- Q-006: intervalli reali di tesseramento e trasferimenti.
- Q-007: filtri, denominatori, parita e ranking record.
- Q-010: interpretare il mancato abbinamento fra molte righe `GiocaIn` e `IncontroInA` prima di definire una partita reale ufficiale.
- Q-011: scelta ufficiale `Voto1/2/3` e regole particolari per presenza/SV/bonus, da fondare su FCM e regolamento.
- Q-012: integrare la stagione manuale 2005/06 dall'FCA originale non presente nelle tabelle raw SQLite attuali.

Nessuna di queste questioni viene risolta per deduzione dal prototipo.
~~~~

### `docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v5.md`

SHA256: `bf287348ee38c5a90af5432018705a38f3ad34721d5244de7b7767662b3ea555`

~~~~markdown
# RecordsNext 4 — Diario implementazione v5

Le versioni v2-v4 e `VERIFICA_FASE0_RECORDSNEXT4_v1.md` restano la cronologia della Fase 0. Non alterate.

## 2026-10-31 — Fase 1, prototipo archivio reali v1

- Letti codice 3.2, schema raw FCA, modelli e rapporti di Fase 0; identificato esattamente il nome della tabella `IncontroInA` normalizzata `incontraina`.
- Implementato `tools/Estrai-RecordsNext4Reali-v1.py`: lettura SQLite `mode=ro`, selezione per codice oppure intera stagione, conservazione di tutte le colonne raw FCA, giornate non giocate e valori voto speciali, snapshot `Storico` separato.
- Collegamento opzionale a `IncontroInA` solo quando giornata+squadra restituiscono un candidato unico; nessuna assunzione su casi non risolti.
- Implementati `tools/Test-RecordsNext4Reali-v1.py` (7 controlli), inclusi confronto record Lautaro 2025/26 e collisione codice 12302.
- Eseguiti 21 export completi sulla copia DB 3.2: 16.245 righe anagrafiche stagionali, 617.310 righe `GiocaIn`, 0 `IDPunteggio` senza score. 143.258 righe non risolte per match reale, senza classificarle come errori.
- Eseguiti export per `100843`: 9 stagioni, 342 righe; per `12302`: 3 anagrafiche, warning storico. 7 test superati. Database immutato.
- Preparati piano v6, decisioni v5, specifica v1, generatore bibbia v5, runner di installazione v1 e kit zip. Test su Windows e audit restituito dall'utente ancora da eseguire.

## Stato veritiero

- Python esplorativo funzionante sulla copia di SQLite; **non** codice Java della pipeline, non generazione di record, non pubblicazione JS, non GUI.
- Nessun commit e nessuna release 4.0.0.
- Per successivi cambiamenti versionare tutti i file ed includerne il testo integrale e gli hash nella bibbia successiva.
~~~~

### `docs/VERIFICA_ESTRAZIONE_REALE_RECORDSNEXT4_v1.md`

SHA256: `89ed185c0696bf30d6112e5b290cf48ccef45930e4cd64d11fbe5359f7473543`

~~~~markdown
# Verifica locale tecnica — Estrattore reale v1

Database: copia `recordsnext.db` ricevuta dall'utente, SHA256 `d95e4597ec9f1747ca1576876c6443f8c4cbdec92cd66f0a264c5d11da1caf76`.

- Implementazione: `tools/Estrai-RecordsNext4Reali-v1.py` (sola lettura).
- Test: `tools/Test-RecordsNext4Reali-v1.py`, **7/7 superati** sull'ambiente di prova disponibile.
- Esportazione `--all` su tutte le **21** stagioni: **16.245** giocatori stagionali, **617.310** righe `GiocaIn`, 0 score mancanti, 143.258 potenziali incontri reali non abbinati. File `.jsonl.gz` generati singolarmente per anno, senza perdita di righe.
- Esempio codice `100843`: **9 stagioni / 342 giornate FCA**. Esempio `12302`: **3 stagioni / 114 giornate FCA**, segnala il conflitto storico.
- `Statistiche` 2025/26 di Lautaro: 17 gol e 6 assist, coerenti con `GolFatti1`/`Assist` ricostruiti da `GiocaIn`+`Punteggio` per quella stagione.

La mancata attribuzione univoca a `IncontroInA` non significa dato danneggiato. Questa prova non certifica tutte le statistiche storiche, ne una fonte voti ufficiale, ne la fase di integrazione nella pipeline Java. Aspetti mancanti e problemi da approfondire sono riportati nella specifica v1 e nel registro decisioni v5.

**Le verifiche PowerShell/Windows devono essere confermate attraverso lo ZIP diagnostico Fase 1.**
~~~~

### `tools/Estrai-RecordsNext4Reali-v1.py`

SHA256: `830c03c5881f68aa713d9b1026c6cc255c6f0b9ff1a62b5117ac21da9dc20f3f`

~~~~python
#!/usr/bin/env python3
"""RecordsNext 4, Fase 1: esportatore FCA reali senza scritture sul database.

Per codice storico: JSON completo con anagrafica per stagione, tutte le righe
GiocaIn/Punteggio, Statistiche, Storico (snapshot) e contesto FCA disponibile.
Per intera stagione: JSONL.gz, una riga per calciatore, con identico modello.
Nessun aggregato all-time automatico, nessuna deduzione di presenza da GiocaIn.
"""
from __future__ import annotations
import argparse
from collections import defaultdict
from datetime import datetime
import gzip
import hashlib
import json
from pathlib import Path
import re
import sqlite3
import sys
from typing import Any

VERSION = "1.0"
REQUIRED = ("giocatorea", "giocain", "punteggio", "statistiche", "storico",
            "squadradia", "giornatadia", "incontraina")
SEASON_RE = re.compile(r"^raw_(\d{4}_\d{4})_fca_giocatorea$", re.IGNORECASE)


def table_name(year: str, table: str) -> str:
    if not re.fullmatch(r"\d{4}_\d{4}", year) or table not in REQUIRED:
        raise ValueError("Identificativo di tabella non valido")
    return '"raw_' + year + '_fca_' + table + '"'


def rows(con: sqlite3.Connection, sql: str, params: tuple = ()) -> list[dict]:
    return [dict(r) for r in con.execute(sql, params)]


def sha256(path: Path) -> str:
    h = hashlib.sha256()
    with path.open('rb') as f:
        for b in iter(lambda: f.read(4 * 1024 * 1024), b''):
            h.update(b)
    return h.hexdigest()


def seasons(con: sqlite3.Connection) -> list[str]:
    found = []
    for (name,) in con.execute("SELECT name FROM sqlite_master WHERE type='table'"):
        m = SEASON_RE.fullmatch(name)
        if m:
            found.append(m.group(1))
    return sorted(set(found))


def validate_schema(con: sqlite3.Connection, year: str) -> None:
    existing = {row[0].lower() for row in con.execute("SELECT name FROM sqlite_master WHERE type='table'")}
    missing = [t for t in REQUIRED if f'raw_{year}_fca_{t}' not in existing]
    if missing:
        raise RuntimeError('FCA non completo, stagione %s: %s' % (year, ', '.join(missing)))


def load_references(con: sqlite3.Connection, year: str) -> dict:
    names = {int(r['ID']): r['NOME'] for r in rows(
        con, 'SELECT ID, NOME FROM ' + table_name(year, 'squadradia'))}
    days = {int(r['GIORNATA']): r for r in rows(
        con, 'SELECT * FROM ' + table_name(year, 'giornatadia'))}
    encounters: dict[tuple[int, int], list[dict]] = defaultdict(list)
    for item in rows(con, 'SELECT * FROM ' + table_name(year, 'incontraina')):
        day = int(item['GIORNATA'])
        for team in (item['IDCASA'], item['IDFUORI']):
            if team is not None and int(team) > 0:
                encounters[(day, int(team))].append(item)
    return {'teams': names, 'days': days, 'encounters': encounters}


def real_match(day: int, team: int | None, ref: dict) -> dict:
    matches = ref['encounters'].get((day, team), []) if team is not None else []
    if len(matches) != 1:
        return {'stato': 'NON_COLLEGATO' if not matches else 'AMBIGUO',
                'numeroCandidati': len(matches), 'dettaglio': None}
    src = matches[0]
    home = src['IDCASA']
    away = src['IDFUORI']
    opp = away if home == team else home
    return {'stato': 'COLLEGATO', 'numeroCandidati': 1,
            'dettaglio': {'sorgenteIncontroInA': src, 'avversarioId': opp,
                         'avversario': ref['teams'].get(opp),
                         'squadraCasa': ref['teams'].get(home),
                         'squadraFuori': ref['teams'].get(away),
                         'casa': bool(home == team)}}


def vote_state(p: dict | None) -> str:
    # Classifica solo il valore raw VOTO1: non e una regola di presenza o SV generale.
    if p is None or p.get('VOTO1') is None:
        return 'VOTO1_ASSENTE'
    v = p['VOTO1']
    if v == -1:
        return 'VOTO1_MENO_UNO'
    if v == 0:
        return 'VOTO1_ZERO'
    if v > 0:
        return 'VOTO1_POSITIVO'
    return 'VOTO1_ALTRO_NEGATIVO'


def build_season(con: sqlite3.Connection, year: str,
                 candidates: list[dict], ref: dict,
                 all_gioca: dict[int, list[dict]] | None = None,
                 scores: dict[int, dict] | None = None,
                 statistics: dict[int, list[dict]] | None = None,
                 history: dict[int, list[dict]] | None = None) -> tuple[list[dict], dict]:
    results = []
    counters = defaultdict(int)
    for player in candidates:
        pid = int(player['ID'])
        if all_gioca is None:
            giocain = rows(con, 'SELECT * FROM ' + table_name(year, 'giocain') +
                          ' WHERE IDGIOCATORE=? ORDER BY GIORNATA', (pid,))
        else:
            giocain = all_gioca.get(pid, [])
        if statistics is None:
            stat = rows(con, 'SELECT * FROM ' + table_name(year, 'statistiche') +
                        ' WHERE ID=?', (pid,))
        else:
            stat = statistics.get(pid, [])
        if history is None:
            hist = rows(con, 'SELECT * FROM ' + table_name(year, 'storico') +
                        ' WHERE IDGIOCATORE=? ORDER BY ANNO', (pid,))
        else:
            hist = history.get(pid, [])
        days = []
        seen = set()
        for g in giocain:
            round_num = int(g['GIORNATA'])
            key = (pid, round_num)
            if key in seen:
                raise RuntimeError('Duplicato GiocaIn nella stagione %s, giocatore %d giornata %d' %
                                   (year, pid, round_num))
            seen.add(key)
            point_id = g['IDPUNTEGGIO']
            if scores is None:
                pp = rows(con, 'SELECT * FROM ' + table_name(year, 'punteggio') +
                          ' WHERE ID=?', (point_id,)) if point_id is not None else []
                p = pp[0] if len(pp) == 1 else None
            else:
                p = scores.get(point_id)
            d = ref['days'].get(round_num)
            if p is None:
                counters['punteggiNonRisolti'] += 1
            if d is None:
                counters['giornateNonRisolte'] += 1
            team = int(g['IDSQUADRA']) if g['IDSQUADRA'] is not None else None
            match = real_match(round_num, team, ref)
            if match['stato'] != 'COLLEGATO':
                counters['partiteRealiNonCollegate'] += 1
            counters['righeGiocaIn'] += 1
            days.append({
                'stagione': year, 'giocatoreIdFca': pid,
                'codiceStoricoSorgente': player.get('CODICEGAZZA'),
                'giornata': round_num,
                'giornataFca': d,  # flag GIOCATA originale, senza reinterpretazione
                'squadraRealeId': team,
                'squadraReale': ref['teams'].get(team),
                'giocaInFca': g,
                'punteggioFca': p,
                'statoVoto1': vote_state(p),
                'incontroReale': match,
            })
        results.append({
            'stagione': year,
            'giocatoreIdFca': pid,
            'codiceStoricoSorgente': player.get('CODICEGAZZA'),
            'giocatoreAFca': player,
            'statisticheFca': stat,
            # Storico in ogni FCA e una *istantanea* (anche di anni precedenti).
            # Non sommare gli snapshot tra stagioni.
            'storicoFcaSnapshot': hist,
            'giornateReali': days,
        })
        counters['giocatori'] += 1
        counters['statisticheRighe'] += len(stat)
        counters['storicoSnapshotRighe'] += len(hist)
    return results, dict(counters)


def grouped(con: sqlite3.Connection, year: str, family: str, key: str) -> dict[int, list[dict]]:
    groups = defaultdict(list)
    for item in rows(con, 'SELECT * FROM ' + table_name(year, family)):
        if item[key] is not None:
            groups[int(item[key])].append(item)
    return groups


def identity_warning(records: list[dict]) -> list[dict]:
    # Per un codice, non presume un'identita all-time se cambiano i dati.
    name_sets = defaultdict(list)
    for r in records:
        a = r['giocatoreAFca']
        name = re.sub(r'\s+', ' ', (a.get('NOME') or '').strip()).casefold()
        birth = str(a.get('DATADINASCITA') or '')[:10]
        name_sets[(name, birth)].append(r['stagione'])
    if len(name_sets) < 2:
        return []
    return [{'tipo': 'IDENTITA_DA_VERIFICARE',
             'motivo': 'Anagrafiche differenti per lo stesso codice; NON aggregare all-time automaticamente',
             'gruppi': [{'nomeNormalizzato': name, 'dataNascita': birth, 'stagioni': years}
                        for (name, birth), years in sorted(name_sets.items())]}]


def select_players(con: sqlite3.Connection, year: str, code: int | None) -> list[dict]:
    name = table_name(year, 'giocatorea')
    if code is None:
        return rows(con, 'SELECT * FROM ' + name + ' ORDER BY ID')
    return rows(con, 'SELECT * FROM ' + name + ' WHERE CODICEGAZZA=? ORDER BY ID', (code,))


def export_code(con, code: int, chosen: list[str], destination: Path, db: Path) -> dict:
    records, cover = [], []
    for year in chosen:
        validate_schema(con, year)
        p = select_players(con, year, code)
        ref = load_references(con, year) if p else None
        part, counter = build_season(con, year, p, ref) if p else ([], {})
        records.extend(part)
        cover.append({'stagione': year, 'giocatoriTrovati': len(p), **counter})
    result = {
        'formato': 'RecordsNext4.PlayerReal.v1', 'tipo': 'SELEZIONE_CODICE',
        'codiceRichiesto': code,
        'nota': 'CODICEGAZZA e identita proposta, verificare le anagrafiche prima degli aggregati all-time.',
        'avvertenzeIdentita': identity_warning(records),
        'coperturaStagioni': cover,
        'giocatoriStagionali': records,
    }
    with destination.open('x', encoding='utf-8', newline='\n') as f:
        json.dump(result, f, ensure_ascii=False, indent=2)
        f.write('\n')
    return {'codice': code, 'file': destination.name,
            'giocatoriStagionali': len(records),
            'giornateReali': sum(len(r['giornateReali']) for r in records),
            'avvertenzeIdentita': len(result['avvertenzeIdentita']),
            'sha256Output': sha256(destination)}


def export_all_season(con, year: str, destination: Path) -> dict:
    validate_schema(con, year)
    ref = load_references(con, year)
    players = select_players(con, year, None)
    all_gioca = grouped(con, year, 'giocain', 'IDGIOCATORE')
    scores_list = rows(con, 'SELECT * FROM ' + table_name(year, 'punteggio'))
    scores = {p['ID']: p for p in scores_list}
    if len(scores) != len(scores_list):
        raise RuntimeError('Punteggio.ID duplicato nella stagione ' + year)
    statistics = grouped(con, year, 'statistiche', 'ID')
    history = grouped(con, year, 'storico', 'IDGIOCATORE')
    written, count = 0, defaultdict(int)
    with gzip.open(destination, 'xt', encoding='utf-8', newline='\n', compresslevel=6) as out:
        for player in players:
            subset, counters = build_season(con, year, [player], ref, all_gioca,
                                            scores, statistics, history)
            out.write(json.dumps(subset[0], ensure_ascii=False, separators=(',', ':')) + '\n')
            written += 1
            for k, v in counters.items():
                count[k] += v
    row_count = con.execute('SELECT COUNT(*) FROM ' + table_name(year, 'giocain')).fetchone()[0]
    if row_count != count['righeGiocaIn']:
        raise RuntimeError('Numero GiocaIn non conservato per %s: %s != %s' %
                           (year, count['righeGiocaIn'], row_count))
    return {'stagione': year, 'file': destination.name, 'giocatori': written,
            'righeGiocaIn': count['righeGiocaIn'],
            'punteggiNonRisolti': count['punteggiNonRisolti'],
            'partiteRealiNonCollegate': count['partiteRealiNonCollegate'],
            'sha256Output': sha256(destination)}


def main() -> int:
    parser = argparse.ArgumentParser(description='Estrazione FCA reale, v1 sola lettura')
    parser.add_argument('--database', type=Path, required=True)
    parser.add_argument('--output-dir', type=Path, required=True)
    x = parser.add_mutually_exclusive_group(required=True)
    x.add_argument('--code', type=int, help='Codice storico es. 100843')
    x.add_argument('--all', action='store_true', help='Tutti i giocatori, per stagione, gzip JSONL')
    parser.add_argument('--season', action='append', help='Stagione YYYY_YYYY; ripetibile; senza = tutte')
    args = parser.parse_args()
    if args.code is not None and args.code <= 0:
        parser.error('Il codice deve essere maggiore di zero')
    db = args.database.resolve(strict=True)
    out = args.output_dir.resolve()
    out.mkdir(parents=True, exist_ok=True)
    stamp = datetime.now().strftime('%Y%m%d_%H%M%S_%f')
    uri = db.as_uri() + '?mode=ro'
    connection = sqlite3.connect(uri, uri=True)
    connection.row_factory = sqlite3.Row
    connection.execute('PRAGMA query_only=ON')
    try:
        years = seasons(connection)
        selected = sorted(set(args.season)) if args.season else years
        unknown = set(selected) - set(years)
        if unknown:
            raise ValueError('Stagioni prive di GiocatoreA raw: ' + ', '.join(sorted(unknown)))
        manifest = {'formato': 'RecordsNext4.PlayerReal.Manifest.v1',
                    'versioneEstrattore': VERSION,
                    'database': str(db), 'databaseSha256': sha256(db),
                    'stagioni': selected, 'tipo': 'ALL' if args.all else 'CODICE',
                    'output': []}
        if args.all:
            for year in selected:
                path = out / ('RecordsNext4_Reali_v1_' + year + '_' + stamp + '.jsonl.gz')
                info = export_all_season(connection, year, path)
                manifest['output'].append(info)
                print('ESPORTATA', year, info['giocatori'], 'giocatori,', info['righeGiocaIn'], 'giornate FCA', flush=True)
        else:
            path = out / ('RecordsNext4_Reali_v1_' + str(args.code) + '_' + stamp + '.json')
            info = export_code(connection, args.code, selected, path, db)
            manifest['output'].append(info)
            print('ESPORTATO', args.code, info['giocatoriStagionali'], 'stagioni giocatore,', info['giornateReali'], 'giornate FCA', flush=True)
        manifest_path = out / ('RecordsNext4_Reali_MANIFEST_v1_' + stamp + '.json')
        with manifest_path.open('x', encoding='utf-8', newline='\n') as f:
            json.dump(manifest, f, ensure_ascii=False, indent=2)
            f.write('\n')
        print('MANIFEST:', manifest_path, flush=True)
        return 0
    finally:
        connection.close()


if __name__ == '__main__':
    try:
        sys.exit(main())
    except Exception as exc:
        print('ERRORE ESTRAZIONE REALI v1:', str(exc), file=sys.stderr, flush=True)
        sys.exit(1)
~~~~

### `tools/Test-RecordsNext4Reali-v1.py`

SHA256: `6425531c73d232ae8b317b7cbcc46bea5cc0ac0c94e80bea52e522bed9d74b58`

~~~~python
#!/usr/bin/env python3
"""Test standard library dell'estrattore FCA reale v1. Solo lettura.

Uso: py -3 tools/Test-RecordsNext4Reali-v1.py --database <DB>
"""
from __future__ import annotations
import argparse
import importlib.util
from pathlib import Path
import sqlite3
import sys
import unittest

MODULE = Path(__file__).with_name('Estrai-RecordsNext4Reali-v1.py')
spec = importlib.util.spec_from_file_location('recordsnext4_real_v1', MODULE)
extractor = importlib.util.module_from_spec(spec)
spec.loader.exec_module(extractor)


class TestRegolePure(unittest.TestCase):
    def test_stati_voto1_non_interpretati_come_presenze(self):
        self.assertEqual('VOTO1_ASSENTE', extractor.vote_state(None))
        self.assertEqual('VOTO1_ASSENTE', extractor.vote_state({'VOTO1': None}))
        self.assertEqual('VOTO1_ZERO', extractor.vote_state({'VOTO1': 0}))
        self.assertEqual('VOTO1_MENO_UNO', extractor.vote_state({'VOTO1': -1}))
        self.assertEqual('VOTO1_POSITIVO', extractor.vote_state({'VOTO1': 6.5}))

    def test_partita_reale_non_si_inventa(self):
        ref = {'teams': {1: 'Casa', 2: 'Fuori'},
               'encounters': {(1, 1): [{'GIORNATA': 1, 'IDCASA': 1,
                                         'IDFUORI': 2, 'GOLCASA': 2, 'GOLFUORI': 1}],
                              (2, 1): [{'GIORNATA': 2, 'IDCASA': 1, 'IDFUORI': 2},
                                       {'GIORNATA': 2, 'IDCASA': 1, 'IDFUORI': 3}]}}
        self.assertEqual('COLLEGATO', extractor.real_match(1, 1, ref)['stato'])
        self.assertEqual('Fuori', extractor.real_match(1, 1, ref)['dettaglio']['avversario'])
        self.assertEqual('NON_COLLEGATO', extractor.real_match(3, 1, ref)['stato'])
        self.assertEqual('AMBIGUO', extractor.real_match(2, 1, ref)['stato'])

    def test_anagrafiche_omonime_codice_non_vengono_fuse(self):
        recs = [
            {'stagione': '2006_2007', 'giocatoreAFca': {'NOME': 'A', 'DATADINASCITA': '1979-01-01'}},
            {'stagione': '2015_2016', 'giocatoreAFca': {'NOME': 'B', 'DATADINASCITA': '1991-01-01'}},
        ]
        self.assertEqual(1, len(extractor.identity_warning(recs)))
        recs[1]['giocatoreAFca'] = recs[0]['giocatoreAFca']
        self.assertEqual([], extractor.identity_warning(recs))


class TestDbOriginale(unittest.TestCase):
    DB = None

    @classmethod
    def setUpClass(cls):
        if cls.DB is None:
            raise unittest.SkipTest('DB non specificato')
        cls.con = sqlite3.connect(cls.DB.resolve().as_uri() + '?mode=ro', uri=True)
        cls.con.row_factory = sqlite3.Row
        cls.con.execute('PRAGMA query_only=ON')

    @classmethod
    def tearDownClass(cls):
        if hasattr(cls, 'con'):
            cls.con.close()

    def test_copertura_21_anni_e_8_famiglie(self):
        years = extractor.seasons(self.con)
        self.assertEqual(21, len(years))
        for year in years:
            extractor.validate_schema(self.con, year)

    def test_lautaro_code_100843_nove_stagioni(self):
        available = [year for year in extractor.seasons(self.con)
                     if extractor.select_players(self.con, year, 100843)]
        self.assertEqual(['2018_2019','2019_2020','2020_2021','2021_2022',
                          '2022_2023','2023_2024','2024_2025','2025_2026',
                          '2026_2027'], available)
        all_rows = []
        for year in available:
            pp = extractor.select_players(self.con, year, 100843)
            extracted, _ = extractor.build_season(self.con, year, pp,
                                                  extractor.load_references(self.con, year))
            self.assertEqual(1, len(extracted))
            self.assertEqual(38, len(extracted[0]['giornateReali']))
            all_rows.extend(extracted)
        self.assertEqual([], extractor.identity_warning(all_rows))
        yr25 = next(r for r in all_rows if r['stagione'] == '2025_2026')
        stat = yr25['statisticheFca']
        self.assertEqual(1, len(stat))
        self.assertEqual(17, stat[0]['GOLFATTI'])
        self.assertEqual(6, stat[0]['ASSIST'])
        day_sum_goals = sum((r['punteggioFca'] or {}).get('GOLFATTI1') or 0
                            for r in yr25['giornateReali'])
        day_sum_assists = sum((r['punteggioFca'] or {}).get('ASSIST') or 0
                             for r in yr25['giornateReali'])
        self.assertEqual(17, day_sum_goals)
        self.assertEqual(6, day_sum_assists)

    def test_12302_non_aggregabile_automaticamente(self):
        candidates = []
        for y in extractor.seasons(self.con):
            for p in extractor.select_players(self.con, y, 12302):
                candidates.append({'stagione': y, 'giocatoreAFca': p})
        self.assertEqual(3, len(candidates))
        self.assertTrue(extractor.identity_warning(candidates))

    def test_statistiche_e_storico_sono_fonti_diverse(self):
        yr = '2025_2026'
        p = extractor.select_players(self.con, yr, 100843)
        recs, _ = extractor.build_season(self.con, yr, p, extractor.load_references(self.con, yr))
        r = recs[0]
        self.assertEqual(1, len(r['statisticheFca']))
        self.assertGreater(len(r['storicoFcaSnapshot']), 1)
        self.assertEqual(38, len(r['giornateReali']))


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--database', type=Path, required=True)
    args = parser.parse_args()
    if not args.database.is_file():
        parser.error('Database non trovato: ' + str(args.database))
    TestDbOriginale.DB = args.database
    suite = unittest.TestSuite()
    suite.addTests(unittest.defaultTestLoader.loadTestsFromTestCase(TestRegolePure))
    suite.addTests(unittest.defaultTestLoader.loadTestsFromTestCase(TestDbOriginale))
    result = unittest.TextTestRunner(verbosity=2, stream=sys.stdout).run(suite)
    return 0 if result.wasSuccessful() else 1


if __name__ == '__main__':
    sys.exit(main())
~~~~

### `tools/Create-RecordsNext4WorkingCodeMd-v5.py`

SHA256: `95421a77bb8704a11bdf67dbeef42c9100744ead8c932549d69a13291bd46835`

~~~~python
#!/usr/bin/env python3
"""Bibbia incrementale Fase 1 v5: include sorgenti esatti, SHA, decisioni e test.

Non tocca docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md e non sovrascrive
un precedente output diverso. Deve essere eseguito dopo installazione kit.
"""
from __future__ import annotations
import argparse
import hashlib
from pathlib import Path
import subprocess
import sys

BASE_COMMIT = 'f9bf30323d9204d70e91e242bddfc72c2060ecf9'
PREVIOUS = 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v4.md'
BASE = 'docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md'
OUTPUT = 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v5.md'
NEW_FILES = [
    'docs/SPECIFICA_ESTRAZIONE_REALE_RECORDSNEXT4_v1.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v6.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v5.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v5.md',
    'docs/VERIFICA_ESTRAZIONE_REALE_RECORDSNEXT4_v1.md',
    'tools/Estrai-RecordsNext4Reali-v1.py',
    'tools/Test-RecordsNext4Reali-v1.py',
    'tools/Create-RecordsNext4WorkingCodeMd-v5.py',
    'tools/Invoke-RecordsNext4-Fase1-v1.py',
]


def digest(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def git(project: Path, *args: str) -> str:
    p = subprocess.run(['git', '-C', str(project), *args],
                       stdout=subprocess.PIPE, stderr=subprocess.PIPE,
                       text=True, encoding='utf-8', errors='replace')
    if p.returncode:
        raise RuntimeError('git ' + ' '.join(args) + ': ' + p.stderr.strip())
    return p.stdout.strip()


def main() -> int:
    p = argparse.ArgumentParser()
    p.add_argument('--project-dir', type=Path, required=True)
    args = p.parse_args()
    project = args.project_dir.resolve()
    for name in (BASE, PREVIOUS, *NEW_FILES):
        if not (project / name).is_file():
            raise RuntimeError('File mancante per la bibbia: ' + name)
    lines = [
        '# RecordsNext 4.0 — Bibbia incrementale v5 (Fase 1 reali)', '',
        '> Questa NON e la release 4.0.0; la pipeline Java 3.2 e invariata.',
        '> Conservare intatta la bibbia 3.2 e le bibbie 4.0 precedenti.', '',
        '## Riferimenti verificabili', '',
        '- Commit base 3.2: `' + BASE_COMMIT + '`',
        '- Branch: `' + git(project, 'branch', '--show-current') + '`',
        '- HEAD al momento della generazione: `' + git(project, 'rev-parse', 'HEAD') + '`',
        '- Bibbia 3.2 SHA-256: `' + digest(project / BASE) + '`',
        '- Bibbia 4.0 v4 SHA-256: `' + digest(project / PREVIOUS) + '`',
        '', '## Stato tecnico', '',
        '- Estrattore FCA read-only Python v1 funzionante sulla copia DB.',
        '- Tutte le 21 stagioni: 16.245 giocatori stagionali, 617.310 righe GiocaIn.',
        '- 7 test automatici superati sulla copia DB; esito Windows da verificare.',
        '- Nessuna scrittura SQLite, modifica FCM/FCA, commit, JS, HTML, GUI o release.',
        '- Mancato abbinamento con IncontroInA non significa errore; vedere specifica.',
        '', '## Codice e documentazione aggiunti (contenuto integrale)', '',
    ]
    for name in NEW_FILES:
        source = project / name
        body = source.read_text(encoding='utf-8-sig')
        lines.extend(['### `' + name + '`', '', 'SHA256: `' + digest(source) + '`',
                      '', '~~~~' + ('python' if name.endswith('.py') else 'markdown'),
                      body.rstrip('\n'), '~~~~', ''])
    payload = ('\n'.join(lines).rstrip() + '\n').encode('utf-8')
    target = project / OUTPUT
    if target.exists():
        if target.read_bytes() != payload:
            raise RuntimeError('Bibbia v5 diversa gia esistente, non sovrascritta: ' + str(target))
        print('Bibbia v5 gia presente e identica:', target, flush=True)
        return 0
    with target.open('xb') as f:
        f.write(payload)
    print('Bibbia v5 generata:', target, flush=True)
    return 0


if __name__ == '__main__':
    try:
        sys.exit(main())
    except Exception as e:
        print('ERRORE BIBBIA v5:', e, file=sys.stderr, flush=True)
        sys.exit(1)
~~~~

### `tools/Invoke-RecordsNext4-Fase1-v1.py`

SHA256: `368ef44556d9c25c3c720c5e796191fa295dea0ad393a3088ffd1b6394a11ad8`

~~~~python
#!/usr/bin/env python3
"""Installa e verifica in maniera conservativa Fase 1 reale v1, con ZIP diagnostico.

Non crea worktree, non modifica DB né codice Java, non esegue deploy o git commit.
Solo nuovi file versionati e rapporti in directory di output.
"""
from __future__ import annotations
import argparse
from datetime import datetime
import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys
import traceback
import zipfile

BASE_COMMIT = 'f9bf30323d9204d70e91e242bddfc72c2060ecf9'
F0_PATCH = [
    'docs/VERIFICA_FASE0_RECORDSNEXT4_v1.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v5.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v4.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v4.md',
    'tools/Probe-RecordsNext4Fonti-v2.py',
    'tools/Create-RecordsNext4WorkingCodeMd-v4.py',
]
F1_PATCH = [
    'docs/SPECIFICA_ESTRAZIONE_REALE_RECORDSNEXT4_v1.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v6.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v5.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v5.md',
    'docs/VERIFICA_ESTRAZIONE_REALE_RECORDSNEXT4_v1.md',
    'tools/Estrai-RecordsNext4Reali-v1.py',
    'tools/Test-RecordsNext4Reali-v1.py',
    'tools/Create-RecordsNext4WorkingCodeMd-v5.py',
    'tools/Invoke-RecordsNext4-Fase1-v1.py',
]
ALL_PATCH = F0_PATCH + F1_PATCH
REQUIRED = [
    'pom.xml', 'docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md',
    'docs/REVISIONE_BASE_RECORDSNEXT_3_2_v1.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v4.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v3.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v3.md',
    'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v3.md',
    'tools/Audit-RecordsNext4Giocatori-v1.py',
    'tools/Create-RecordsNext4WorkingCodeMd-v3.py',
    'tools/Invoke-RecordsNext4-Fase0-v4.py',
]


def sha256(path: Path) -> str:
    h = hashlib.sha256()
    with path.open('rb') as f:
        for block in iter(lambda: f.read(4 * 1024 * 1024), b''):
            h.update(block)
    return h.hexdigest()


def git(project: Path, *args: str) -> str:
    p = subprocess.run(['git', '-C', str(project), *args],
                       stdout=subprocess.PIPE, stderr=subprocess.PIPE,
                       text=True, encoding='utf-8', errors='replace')
    if p.returncode:
        raise RuntimeError('Git: ' + ' '.join(args) + ': ' + p.stderr.strip())
    return p.stdout.strip()


def same_path(a: Path, b: Path) -> bool:
    return os.path.normcase(str(a.resolve())) == os.path.normcase(str(b.resolve()))


def check_project(project: Path) -> dict:
    missing = [p for p in REQUIRED if not (project / p).is_file()]
    if missing:
        raise RuntimeError('Il worktree Fase 0 manca di: ' + ', '.join(missing))
    if not same_path(Path(git(project, 'rev-parse', '--show-toplevel')), project):
        raise RuntimeError('Il progetto indicato non coincide con la radice del worktree')
    branch = git(project, 'branch', '--show-current')
    head = git(project, 'rev-parse', 'HEAD')
    if branch != 'feature/recordsnext4-players':
        raise RuntimeError('Branch inatteso: ' + branch)
    git(project, 'merge-base', '--is-ancestor', BASE_COMMIT, head)
    return {'branch': branch, 'head': head, 'statusBefore': git(project, 'status', '--short', '--branch')}


def install(root: Path, project: Path) -> list[str]:
    # Validazione TOTALE prima di copiare un solo file.
    for path in ALL_PATCH:
        src, dst = root / path, project / path
        if not src.is_file():
            raise RuntimeError('File non incluso nel kit: ' + path)
        if dst.exists():
            if not dst.is_file() or sha256(dst) != sha256(src):
                raise RuntimeError('File gia presente ma differente; nessuna sovrascrittura: ' + path)
    added = []
    for path in ALL_PATCH:
        src, dst = root / path, project / path
        if dst.exists():
            print('GIA PRESENTE IDENTICO:', path, flush=True)
            continue
        dst.parent.mkdir(parents=True, exist_ok=True)
        with dst.open('xb') as f:
            f.write(src.read_bytes())
        added.append(path)
        print('INSTALLATO:', path, flush=True)
    return added


def run_python(source: Path, options: list[str], log: Path) -> str:
    env = dict(os.environ, PYTHONDONTWRITEBYTECODE='1')
    proc = subprocess.run([sys.executable, str(source), *options],
                          stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                          text=True, encoding='utf-8', errors='replace', env=env)
    log.write_text(proc.stdout, encoding='utf-8')
    print(source.name, '(uscita %d)' % proc.returncode, flush=True)
    print(proc.stdout[-900:].strip(), flush=True)
    if proc.returncode:
        raise RuntimeError('Comando non riuscito: ' + source.name + ', controllare ' + log.name)
    return proc.stdout


def main() -> int:
    p = argparse.ArgumentParser(description='Fase 1 RecordsNext 4.0: installazione e audit reali v1')
    p.add_argument('--work-dir', type=Path, default=Path('D:/DEV_APPS/RecordsNext4.0'))
    p.add_argument('--database', type=Path, default=Path('D:/DEV_APPS/RecordsNext2.0/data/database/recordsnext-sandbox.db'))
    p.add_argument('--output-dir', type=Path, default=Path('D:/DEV_APPS/downloads'))
    args = p.parse_args()
    root = Path(__file__).resolve().parent.parent
    project, db, out = args.work_dir.resolve(), args.database.resolve(), args.output_dir.resolve()
    out.mkdir(parents=True, exist_ok=True)
    stamp = datetime.now().strftime('%Y%m%d_%H%M%S_%f')
    bundle = out / ('RecordsNext4_Fase1_OUTPUT_v1_' + stamp)
    bundle.mkdir()
    zip_path = out / (bundle.name + '.zip')
    state = 'ERRORE'
    manifest = {'fase': 1, 'versioneKit': 'v1', 'stato': state,
                'worktree': str(project), 'database': str(db),
                'nessunDeploy': True, 'nessunCommit': True}
    try:
        if not db.is_file():
            raise RuntimeError('Database sandbox non trovato: ' + str(db))
        before = sha256(db)
        check = check_project(project)
        manifest.update(check)
        manifest['databaseSha256Prima'] = before
        print('BASE:', check['head'], 'RAMO:', check['branch'], flush=True)
        added = install(root, project)
        manifest['fileInstallati'] = added
        run_python(project / 'tools/Create-RecordsNext4WorkingCodeMd-v4.py',
                   ['--project-dir', str(project)], bundle / 'LOG_BIBBIA_FASE0_v4.txt')
        run_python(project / 'tools/Test-RecordsNext4Reali-v1.py',
                   ['--database', str(db)], bundle / 'LOG_TEST_REAL_v1.txt')
        pilot = bundle / 'dati_pilota'
        pilot.mkdir()
        for code in (100843, 12302):
            run_python(project / 'tools/Estrai-RecordsNext4Reali-v1.py',
                       ['--database', str(db), '--output-dir', str(pilot), '--code', str(code)],
                       bundle / ('LOG_GIOCATORE_%s_v1.txt' % code))
        for year in ('2006_2007', '2025_2026'):
            run_python(project / 'tools/Estrai-RecordsNext4Reali-v1.py',
                       ['--database', str(db), '--output-dir', str(pilot), '--all', '--season', year],
                       bundle / ('LOG_ALL_%s_v1.txt' % year))
        run_python(project / 'tools/Create-RecordsNext4WorkingCodeMd-v5.py',
                   ['--project-dir', str(project)], bundle / 'LOG_BIBBIA_FASE1_v5.txt')
        docs = bundle / 'documentazione'
        docs.mkdir()
        for name in ('CODICE_FUNZIONANTE_RECORDSNEXT4_v5.md',
                     'PIANO_RECORDSNEXT4_GIOCATORI_v6.md',
                     'REGISTRO_DECISIONI_RECORDSNEXT4_v5.md',
                     'DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v5.md',
                     'SPECIFICA_ESTRAZIONE_REALE_RECORDSNEXT4_v1.md',
                     'VERIFICA_ESTRAZIONE_REALE_RECORDSNEXT4_v1.md'):
            shutil.copy2(project / 'docs' / name, docs / name)
        after = sha256(db)
        if before != after:
            raise RuntimeError('Database modificato DURANTE le verifiche: interrompere altre elaborazioni prima di riprovare')
        manifest['databaseSha256Dopo'] = after
        manifest['statusAfter'] = git(project, 'status', '--short', '--branch')
        manifest['stato'] = 'FASE1_AUDIT_ESEGUITI'
        state = 'OK'
    except Exception:
        error = traceback.format_exc()
        (bundle / 'ERRORE_FASE1_v1.txt').write_text(error, encoding='utf-8')
        print('FASE1 ERRORE:', error[-3200:], file=sys.stderr, flush=True)
    finally:
        (bundle / 'MANIFEST_FASE1_v1.json').write_text(
            json.dumps(manifest, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
        with zipfile.ZipFile(zip_path, 'x', zipfile.ZIP_DEFLATED, compresslevel=6) as z:
            for f in sorted(bundle.rglob('*')):
                if f.is_file():
                    z.write(f, f.relative_to(bundle).as_posix())
        print('ESITO:', state, flush=True)
        print('ZIP DA CARICARE:', zip_path, flush=True)
    return 0 if state == 'OK' else 1


if __name__ == '__main__':
    sys.exit(main())
~~~~
