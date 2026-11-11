# RecordsNext 4 — Codice funzionante / bibbia incrementale v6

Stato: Fase 2 Python sperimentale, non release 4.0 e non pipeline Java.
La bibbia 3.2 e le bibbie 4.0 precedenti NON vengono alterate.

## Collegamenti e controlli

- Commit base: `f9bf30323d9204d70e91e242bddfc72c2060ecf9`
- Branch: `feature/recordsnext4-players`
- HEAD: `f9bf30323d9204d70e91e242bddfc72c2060ecf9`
- SHA bibbia 3.2: `91ecb0006d6d1c6fda501c7243ba65ec2f0625c40e93394079238eac558a7da3`
- SHA bibbia 4.0 v5: `b63036f366530c4d0ad35a3435ee8df551ba77e64240c584ac1c1b5914fa0c3a`
- Fase 1 testata anche su Windows: 7/7, DB SHA invariato.
- Fase 2: 8/8 test e 552.025 slot univoci sulla copia SQLite;
  richiede ancora verifica Windows, nessun deploy o commit.

## File esatti aggiunti nella fase 2

### `docs/PIANO_RECORDSNEXT4_GIOCATORI_v7.md`

SHA256: `c55cd3af2c845a2f28a1c87c0ae5e5c08359bdfbbe86a18f592a1acc33573cfa`

~~~~markdown
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
~~~~

### `docs/REGISTRO_DECISIONI_RECORDSNEXT4_v6.md`

SHA256: `20b089d6dde399d197a49edfa5c668c48690c2972acf246cf18503a6c0d7396e`

~~~~markdown
# RecordsNext 4.0 — Registro decisioni v6

Si applicano tutte le decisioni D-001–D-026 del documento v5, che NON viene sostituito. Stati: **CONFERMATA** (richiesta utente o dato verificato), **IMPLEMENTATA PROTOTIPO** (scelta tecnica reversibile), **APERTA** (da decidere / verificare).

## Esiti Fase 1 Windows

- **D-027 — CONFERMATA:** lo ZIP Windows `RecordsNext4_Fase1_OUTPUT_v1_20261031_213342_462754.zip` contiene 7 test riusciti; reali 2006/07: 657 giocatori e 24.966 righe `GiocaIn`; reali 2025/26: 783 giocatori e 29.754 righe `GiocaIn`. Il numero 6.050 si riferisce invece a `Formazione` fantasy nel 2006/07.
- **D-028 — CONFERMATA:** hash sandbox prima/dopo Fase 1 uguali (`57923f4e75986c2b81533e4beef15b9133635258d7de1d36827a1bdbe576671b`). Nessuna modifica al database per questo prototipo.

## Decisioni Fase 2

| ID | Stato | Testo | Evidenza |
|---|---|---|---|
| D-029 | CONFERMATA | L'unità di partecipazione fantasy è `(stagione, idIncontroFCM, idSquadraFCM, idGiocatoreFCA)`, non solo `codice + giornataSerieA`. | 552.025 `Formazione` su 21 stagioni, senza duplicati su questa chiave. |
| D-030 | CONFERMATA | `Formazione` e `Tabellino` vanno collegati attraverso incontro, squadra e ID giocatore **locale**; `CodiceGazza` serve per trovare il giocatore nello storico. | Tutte le 552.025 righe Formazione trovano slot univoco in LISTA, oltre a GiocaIn per giornata reale. |
| D-031 | CONFERMATA | Le liste `LISTA`, `RUOLO`, `VOTO`, `MODIF`, `TOT` sono allineate per posizione. Il codice `-1` rappresenta una voce d'ufficio; `0` è segnaposto: non attribuire questi valori a un calciatore. | Esempi FCM ed export precedente; preservare testi `sv/ng` e virgole decimali. |
| D-032 | CONFERMATA | Le righe fantasy in `Formazione` includono anche riserve non entrate e incontri non disputati; non equiparare un inserimento in Formazione a punti realmente conteggiati. | `ENTRATO` / `ENTRATORISERVA`, `Incontro.GIOCATO` disponibili in raw. |
| D-033 | IMPLEMENTATA PROTOTIPO | Esportare ogni riga FCM con score FCA e **solo lo slot personale del tabellino**, conservando raw e metadati delle competizioni. Non esportare indirizzi email/password delle fantasquadre. | `Estrai-RecordsNext4Fantasy-v1.py`, test database e verifica su un giocatore. |
| D-034 | IMPLEMENTATA PROTOTIPO | `Tesserato` è snapshot: esporre le possibili righe di tesseramento con la fantasquadra, ma NON assumere intervalli di contratto o trasferimento. | Un giocatore può risultare in più squadre nella stessa stagione. |
| D-035 | APERTA | Punti non sfruttati: definire prima differenza fra voto lasciato in panchina, contributo recuperabile, totale realmente conteggiato e simulazione di formazione valida. | Ancora nessuna metrica calcolata. |
| D-036 | APERTA | Identità storica canonica, link tabellini storico/multisito, appartenenza competitiva canonica e RU saranno integrati dopo i test semantici. | Nessuna attribuzione arbitraria nel prototipo. |

## Questioni ancora aperte

Rimangono valide Q-001–Q-012 della v5, incluso campo FCM dell'ID storico da verificare, collisioni di codice, 168 riferimenti Tabellino senza Incontro, regole ufficiali, giorni reali non abbinati, FCA manuale 2005/06 e dati 2026/27 aggiornati. Nessuno di questi casi è stato risolto per supposizione.
~~~~

### `docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v6.md`

SHA256: `43b890c7c7fe1434766ac22a391475a15c49a050bbc4cdaa35cd869b69bbac92`

~~~~markdown
# RecordsNext 4 — Diario implementazione v6

Continuazione cronologica; conservare immutati `DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v2` fino alla v5.

## 2026-10-31 — Verifica Fase 1 sul PC Windows

- Esaminato il pacchetto utente `RecordsNext4_Fase1_OUTPUT_v1_20261031_213342_462754.zip`; archivio ZIP integro con 22 elementi.
- Nessun `ERRORE`, 7/7 test PASS, esecuzioni 100843, 12302 e stagioni 2006/07 e 2025/26 riuscite.
- La bibbia v5 è stata creata in `D:\DEV_APPS\RecordsNext4.0\docs`.
- SHA DB sandbox immutato; nessun commit, deploy o modifica alla pipeline Java.

## 2026-10-31 — Fase 2, prototipo archivio fantasy v1

- Esaminate le tabelle FCM `Formazione`, `Tabellino`, `Tesserato`, `Incontro`, `Fantasquadra`, `Girone`, `Competizione`, `Giornata` e le FCA `GiocatoreA`, `GiocaIn`, `Punteggio` dal DB ricevuto.
- Implementato `tools/Estrai-RecordsNext4Fantasy-v1.py` read-only: per giocatore storico, oppure tutte le formazioni di una stagione; ogni riga conserva incontro, squadra, competizione, giornata reale, Formazione, slot singolo Tabellino con stringhe originali, tesseramenti snapshot, punteggio FCA.
- Nessuna valorizzazione fittizia di sostituzioni, punti non sfruttati, tesseramenti temporali o identità canoniche; non inclusi contatti/password di `Fantasquadra`.
- Audit delle 21 stagioni: **552.025 righe FCM Formazione**, **552.025 slot tabellino univoci**, **0 senza Incontro**, **0 senza GiocaIn**. Caso 2025/26: 39.850; 2006/07: 6.050; 2026/27: 0 formazioni nell'istantanea SQLite.
- Test Python v1: 8/8 superati su copia SQLite. Estrazione pilota codice 100843: **1.327** partecipazioni fantasy in otto stagioni (2018/19–2025/26), di cui **162** nel 2025/26.
- Non confondere con le 168 righe Tabellino che non puntano a Incontro: non contengono righe Formazione di questo audit e sono tuttora da studiare.
- La Fase 2 è ancora prototipo sperimentale non integrato in RecordsNext Java/JS/GUI; esecuzione Windows attesa tramite runner e ZIP diagnostico.
~~~~

### `docs/SPECIFICA_ESTRAZIONE_FANTASY_RECORDSNEXT4_v1.md`

SHA256: `f9a416768f19f50cb2ec6c11d253f1dfb26693a7876d590f45f2eb0e3b9a74a0`

~~~~markdown
# RecordsNext 4 — Specifica Fase 2: archivio partecipazioni fantasy v1

## Fonte, unità e confini

- Fonte SQLite raw 3.2 in **sola lettura**, 21 stagioni FCM/FCA.
- Unità: `(stagione, idIncontroFCM, idSquadraFCM, Formazione.IDGioc)`; non si deduce che ogni riga sia entrata o abbia contribuito al punteggio.
- `GiocatoreA.ID` e `Formazione.IDGioc` sono ID **annuali**. `GiocatoreA.CodiceGazza` collega la ricerca storica ma necessita validazione di collisioni prima delle aggregazioni all-time.
- Nessuna creazione di link web storici prima di recuperare i percorsi canonici della 3.2; conservare l'ID incontro e tutti i dati necessari.

## Catena dati esatta

`Formazione.IDIncontro -> Incontro.ID -> Girone.ID -> Competizione.ID`; `Incontro.IDGiornata -> Giornata.ID`; `Formazione.IDSquadra -> Fantasquadra.ID` (solo ID/nome esportati); `Formazione.IDGioc -> GiocatoreA.ID` (stessa stagione); `Incontro.GiornataDiA + Formazione.IDGioc -> GiocaIn.Giornata + IDGiocatore -> Punteggio.ID`; `Tabellino(IDIncontro,IDSquadra)` con `LISTA` esplosa e confronto sull'ID annuale.

## Struttura JSON

`RecordsNext4.PlayerFantasy.v1`:

- `stagione`, `giocatoreIdFca`, `codiceStoricoSorgente`, `giocatoreFca`.
- `idIncontroFcm`, `idSquadraFcm`, `squadraFantasy` (solo ID/nome), `avversarioFantasy`, `lato`.
- `competizioneIdFcm`, `competizioneNomeFcm`, `gironeIdFcm`, `gironeNomeFcm`, `giornataFcm`, `giornataReale`.
- `incontroFcm`, `formazioneFcm` originali; `giocaInFca`, `punteggioFca` originali.
- `statoTabellino`: `SLOT_UNICO | SLOT_ASSENTE | SLOT_MULTIPLO | LUNGHEZZE_DIVERSE | TABELLINO_ASSENTE | TABELLINO_MULTIPLO`; `slotTabellino` con `ordine`, ID, ruolo, voto, modificatore, totale **tutti stringhe raw**; `riepilogoSquadraTabellino` contiene solo campi aggregati non individuali.
- `tesseramentiFcmSnapshot`: righe `Tesserato` combacianti con coppia squadra/giocatore al momento dello snapshot, **senza inferire trasferimenti**.

`--code 100843` produce JSON con tutte le partecipazioni delle stagioni disponibili e manifest SHA-256. `--all --season 2025_2026` produce JSONL.gz per riga Formazione e manifest, senza aggregazioni.

## Casi speciali e restrizioni

- I codici `LISTA=-1` (ufficio) e `LISTA=0` (segnaposto) **non** sono ID di calciatori. Si mantengono separati dalla join dei giocatori; il loro audit complessivo e una fase distinta.
- Slot `VOTO`, `MODIF`, `TOT` con virgole decimali e valori `sv/ng` sono preservati **senza conversione numerica o somma**.
- Non equivalere `Formazione.ENTRATO` a prestazione reale, ne `ENTRATORISERVA` a una sostituzione valida senza interpretare la regola FCM.
- Se manca `Incontro`, `GiocaIn` o slot, conservare la riga FCM e marcare assenza; non cancellare né fabbricare dati.
- I tabellini privi di `Incontro` restano fuori da questa estrazione per Formazione e sono reportati separatamente in Fase 0; potrebbero riferirsi anche a particolarità/riposi.
- Il dato 2026/27 nella copia DB non ha ancora formazioni, dunque un file vuoto non certifica l'assenza nel FCM originale corrente.
- Nessuna fusione storica automatica dei codici riutilizzati (es. 12302); nessun calcolo punti inutilizzati/simulazione, nessuna produzione JS, GUI o commit.

## Sicurezza dati

La tabella `Fantasquadra` contiene anche contatti e campi password: il prototipo estrae **solo ID e nome** delle squadre per evitare di inserire dati privati nei file di output/diagnostica.
~~~~

### `docs/VERIFICA_ESTRAZIONE_FANTASY_RECORDSNEXT4_v1.md`

SHA256: `0dcf35cc38a68a27e1dee3199bad6a07f45346b124edda46b4ff5aa17b6d4fee`

~~~~markdown
# Verifica locale Fase 2 — archivio fantasy v1

Fonte: `recordsnext.db` estratto dallo ZIP già fornito; sola lettura. Questa NON e una verifica Windows fino al prossimo ZIP utente.

| Controllo | Esito |
|---|---|
| Stagioni FCM/FCA | 21 |
| `Formazione` totale | 552.025 |
| Slot individuali `Tabellino` esatti | 552.025 / 552.025 |
| `Formazione` senza `Incontro` | 0 |
| `Formazione` senza `GiocaIn` della giornata reale | 0 |
| 2025/26 | 39.850 righe e slot univoci |
| 2006/07 | 6.050 righe e slot univoci |
| 2026/27 | 0 righe nell'istantanea |
| Test Python | 8/8 superati |
| Lautaro, codice 100843 | 1.327 righe fantasy sulle stagioni 2018/19–2025/26; 162 nel 2025/26 |

**Limiti:** non classifica i 168 tabellini non riferiti a `Incontro`; questo audit riguarda solo `Formazione` esistenti. Non calcola punti non sfruttati, non prova tesseramenti temporali e non identifica tutte le anomalie storiche. I percorsi delle foto e i link ai tabellini dovranno seguire la risoluzione esistente della 3.2. Nessuna pubblicazione o integrazione Java/SQLite canonica.
~~~~

### `tools/Estrai-RecordsNext4Fantasy-v1.py`

SHA256: `af2cd23ccc3d5af944289ff3922d2e95c47a6a817dd23db5f965fbd9066692f4`

~~~~python
#!/usr/bin/env python3
"""RecordsNext 4.0 - Fase 2: archivio delle partecipazioni fantasy, READ-ONLY.

Una riga per elemento Formazione, stagione, squadra e incontro FCM. Usa soltanto
le join locali all'anno; conserva il singolo slot Tabellino senza dedurre bonus
sprecati, tesseramenti temporali, partite in campo o identita canoniche.
"""
from __future__ import annotations
import argparse
from collections import Counter, defaultdict
from datetime import datetime
import gzip
import hashlib
import json
from pathlib import Path
import re
import sqlite3
import sys

FORMAT = 'RecordsNext4.PlayerFantasy.v1'
NEEDED = {
    'fcm': ('formazione', 'tabellino', 'incontro', 'fantasquadra', 'girone', 'competizione', 'giornata', 'tesserato'),
    'fca': ('giocatorea', 'giocain', 'punteggio'),
}
SEASON = re.compile(r'^\d{4}_\d{4}$')


def sha256(p: Path) -> str:
    h = hashlib.sha256()
    with p.open('rb') as f:
        for b in iter(lambda: f.read(1024 * 1024), b''):
            h.update(b)
    return h.hexdigest()


def name(year: str, source: str, family: str) -> str:
    if not SEASON.fullmatch(year) or source not in NEEDED or family not in NEEDED[source]:
        raise ValueError('Nome tabella non valido')
    return '"raw_' + year + '_' + source + '_' + family + '"'


def get_rows(c: sqlite3.Connection, table: str, where: str='', args: tuple=()) -> list[dict]:
    return [dict(r) for r in c.execute('SELECT * FROM ' + table + where, args)]


def get_map(c: sqlite3.Connection, table: str, key: str) -> dict:
    data = get_rows(c, table)
    ret = {d[key]: d for d in data}
    if len(ret) != len(data):
        raise ValueError('Duplicato chiave ' + key + ' in ' + table)
    return ret


def get_seasons(c: sqlite3.Connection) -> list[str]:
    out = []
    for (t,) in c.execute("SELECT name FROM sqlite_master WHERE type='table'"):
        m = re.fullmatch(r'raw_(\d{4}_\d{4})_fcm_formazione', t.lower())
        if m:
            out.append(m.group(1))
    return sorted(set(out))


def validate(c: sqlite3.Connection, y: str) -> None:
    names = {r[0].lower() for r in c.execute("SELECT name FROM sqlite_master WHERE type='table'")}
    missing = [f'raw_{y}_{src}_{family}' for src, fs in NEEDED.items() for family in fs
               if f'raw_{y}_{src}_{family}' not in names]
    if missing:
        raise ValueError('Tabelle mancanti in %s: %s' % (y, ', '.join(missing)))


def split_list(v):
    if v is None or v == '':
        return []
    return str(v).split('%')


def index_tab(tab: dict) -> dict:
    """Allinea le cinque liste senza conversioni, rispettandone posizione e RU."""
    keys = ('LISTA', 'RUOLO', 'VOTO', 'MODIF', 'TOT')
    parts = {k: split_list(tab[k]) for k in keys}
    sizes = {len(v) for v in parts.values()}
    if len(sizes) != 1:
        return {'stato': 'LUNGHEZZE_DIVERSE', 'slots': {}, 'conteggi': {k: len(v) for k, v in parts.items()}}
    index = defaultdict(list)
    for i, s in enumerate(parts['LISTA']):
        try:
            pid = int(s)
        except (ValueError, TypeError):
            pid = None
        if pid is not None:
            index[pid].append({'ordine': i+1, 'idGiocatoreFca': pid,
                               'listaRaw': s, 'ruoloRaw': parts['RUOLO'][i],
                               'votoRaw': parts['VOTO'][i], 'modificatoreRaw': parts['MODIF'][i],
                               'totaleRaw': parts['TOT'][i]})
    return {'stato': 'LISTE_ALLINEATE', 'slots': dict(index), 'conteggi': {k: len(v) for k, v in parts.items()}}


def read_context(c: sqlite3.Connection, y: str) -> dict:
    mp = get_map(c, name(y, 'fcm', 'incontro'), 'ID')
    teams = {k: {'idSquadraFcm': k, 'nome': v['NOME']}
             for k, v in get_map(c, name(y, 'fcm', 'fantasquadra'), 'ID').items()}
    groups = get_map(c, name(y, 'fcm', 'girone'), 'ID')
    comps = get_map(c, name(y, 'fcm', 'competizione'), 'ID')
    days = get_map(c, name(y, 'fcm', 'giornata'), 'ID')
    tabs = defaultdict(list)
    for row in get_rows(c, name(y, 'fcm', 'tabellino')):
        tabs[row['IDINCONTRO'], row['IDSQUADRA']].append(row)
    roster = defaultdict(list)
    for row in get_rows(c, name(y, 'fcm', 'tesserato')):
        roster[row['IDSQUADRA'], row['IDGIOCATORE']].append(row)
    real = { (r['IDGIOCATORE'], r['GIORNATA']): r
             for r in get_rows(c, name(y, 'fca', 'giocain')) }
    scores = get_map(c, name(y, 'fca', 'punteggio'), 'ID')
    players = get_map(c, name(y, 'fca', 'giocatorea'), 'ID')
    return {'matches': mp, 'teams': teams, 'groups': groups, 'comps': comps, 'days': days,
            'tabs': tabs, 'roster': roster, 'real': real, 'scores': scores, 'players': players}


def build_row(y: str, form: dict, ref: dict) -> dict:
    mid, sid, pid = form['IDINCONTRO'], form['IDSQUADRA'], form['IDGIOC']
    match = ref['matches'].get(mid)
    group = ref['groups'].get(match['IDGIRONE']) if match else None
    comp = ref['comps'].get(group['IDCOMPETIZIONE']) if group else None
    day = ref['days'].get(match['IDGIORNATA']) if match else None
    ts = ref['tabs'].get((mid, sid), [])
    tabstatus, slot = '', None
    if len(ts) != 1:
        tabstatus = 'TABELLINO_ASSENTE' if not ts else 'TABELLINO_MULTIPLO'
    else:
        idx = index_tab(ts[0])
        if idx['stato'] != 'LISTE_ALLINEATE':
            tabstatus = idx['stato']
        else:
            matches = idx['slots'].get(pid, [])
            tabstatus = 'SLOT_UNICO' if len(matches)==1 else ('SLOT_ASSENTE' if not matches else 'SLOT_MULTIPLO')
            slot = matches[0] if len(matches)==1 else (matches if matches else None)
    opponent = None
    if match:
        if match['IDCASA'] == sid:
            oppid, side = match['IDFUORI'], 'casa'
        elif match['IDFUORI'] == sid:
            oppid, side = match['IDCASA'], 'fuori'
        else:
            oppid, side = None, 'non_coincidente'
        if oppid and oppid != 0:
            opponent = ref['teams'].get(oppid, {'idSquadraFcm': oppid, 'nome': None})
    else:
        side = 'incontro_assente'
    gr = ref['real'].get((pid, match['GIORNATADIA'])) if match else None
    score = ref['scores'].get(gr['IDPUNTEGGIO']) if gr is not None else None
    return {
        'stagione': y, 'giocatoreIdFca': pid,
        'codiceStoricoSorgente': ref['players'].get(pid, {}).get('CODICEGAZZA'),
        'giocatoreFca': ref['players'].get(pid),
        'idIncontroFcm': mid, 'idSquadraFcm': sid,
        'squadraFantasy': ref['teams'].get(sid), 'avversarioFantasy': opponent,
        'lato': side,
        'competizioneIdFcm': comp['ID'] if comp else None,
        'competizioneNomeFcm': comp['NOME'] if comp else None,
        'gironeIdFcm': group['ID'] if group else None,
        'gironeNomeFcm': group['NOME'] if group else None,
        'giornataFcm': day, 'giornataReale': match['GIORNATADIA'] if match else None,
        'incontroFcm': match,
        'formazioneFcm': form,
        'statoTabellino': tabstatus, 'slotTabellino': slot,
        'riepilogoSquadraTabellino': ({k: v for k, v in ts[0].items()
                                      if k not in ('LISTA','LISTA2','RUOLO','RUOLO2',
                                                   'VOTO','VOTO2','MODIF','MODIF2','TOT','TOT2')}
                                     if len(ts) == 1 else None),
        'tesseramentiFcmSnapshot': ref['roster'].get((sid, pid), []),
        'giocaInFca': gr, 'punteggioFca': score,
        'nota': 'ENTRATO, ENTRATORISERVA e slotTabellino sono valori FCM; nessuna attribuzione punti sprecati.'
    }


def run_export(c: sqlite3.Connection, y: str, code: int | None, writer) -> dict:
    validate(c, y)
    ref = read_context(c, y)
    if code is None:
        forms = get_rows(c, name(y, 'fcm', 'formazione'))
    else:
        pids = [id for id, p in ref['players'].items() if p.get('CODICEGAZZA') == code]
        if not pids:
            return {'stagione':y,'formazioni':0,'statoTabellino':{},'senzaIncontro':0,'senzaGiocaIn':0}
        params = ','.join('?' * len(pids))
        forms = get_rows(c, name(y, 'fcm', 'formazione'),
                         ' WHERE IDGIOC IN (' + params + ')', tuple(pids))
    forms.sort(key=lambda f: (f['IDINCONTRO'], f['IDSQUADRA'], f['IDGIOC']))
    seen=set()
    counters=Counter()
    for f in forms:
        key=(f['IDINCONTRO'], f['IDSQUADRA'], f['IDGIOC'])
        if key in seen:
            raise RuntimeError('Formazione duplicata: ' + str((y,key)))
        seen.add(key)
        row=build_row(y, f, ref)
        writer(row)
        counters['formazioni']+=1
        counters['tab_'+row['statoTabellino']]+=1
        if row['incontroFcm'] is None: counters['senzaIncontro']+=1
        if row['giocaInFca'] is None: counters['senzaGiocaIn']+=1
        if row['giocatoreFca'] is None: counters['senzaGiocatoreFca']+=1
        if row['competizioneNomeFcm'] is None: counters['senzaCompetizione']+=1
    return {'stagione':y, 'formazioni':counters['formazioni'],
            'statoTabellino':{k[4:]:v for k,v in counters.items() if k.startswith('tab_')},
            'senzaIncontro':counters['senzaIncontro'],
            'senzaGiocaIn':counters['senzaGiocaIn'],
            'senzaGiocatoreFca':counters['senzaGiocatoreFca'],
            'senzaCompetizione':counters['senzaCompetizione']}


def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument('--database',type=Path,required=True)
    p.add_argument('--output-dir',type=Path,required=True)
    g=p.add_mutually_exclusive_group(required=True)
    g.add_argument('--code',type=int)
    g.add_argument('--all',action='store_true')
    p.add_argument('--season',help='AAAA_AAAA, obbligatoria per --all')
    args=p.parse_args()
    if args.all and not args.season: p.error('--all richiede --season per evitare output enormi accidentali')
    if args.code is not None and args.code <= 0: p.error('--code deve essere positivo; 0 non identifica un calciatore')
    db=args.database.resolve(strict=True)
    out=args.output_dir.resolve()
    out.mkdir(parents=True, exist_ok=True)
    stamp=datetime.now().strftime('%Y%m%d_%H%M%S_%f')
    base='RecordsNext4_Fantasy_v1_' + (str(args.code) if args.code is not None else args.season) + '_' + stamp
    with sqlite3.connect(db.as_uri()+'?mode=ro',uri=True) as c:
        c.row_factory=sqlite3.Row
        c.execute('PRAGMA query_only=ON')
        years=get_seasons(c)
        if args.season:
            if args.season not in years: p.error('Stagione inesistente: '+args.season)
            years=[args.season]
        if args.all:
            filename=base+'.jsonl.gz'
            path=out/filename
            with gzip.open(path,'xt',encoding='utf-8',newline='\n') as f:
                stats=run_export(c,years[0],None, lambda x: f.write(json.dumps(x,ensure_ascii=False,separators=(',',':'))+'\n'))
            if stats['formazioni'] != c.execute('SELECT COUNT(*) FROM ' + name(years[0],'fcm','formazione')).fetchone()[0]:
                raise RuntimeError('Conteggio righe non corrispondente al sorgente FCM')
            result={'tipo':'TUTTI_STAGIONE','formato':FORMAT,'stagione':years[0], 'statistiche':[stats]}
        else:
            records=[]; stats=[]
            for y in years:
                stats.append(run_export(c,y,args.code,records.append))
            filename=base+'.json'
            path=out/filename
            with path.open('x',encoding='utf-8',newline='\n') as f:
                json.dump({'formato':FORMAT,'tipo':'SELEZIONE_CODICE',
                           'codiceRichiesto':args.code,'statistiche':stats,
                           'avvertenzaIdentita':'Il codice non prova da solo identita canonica attraverso gli anni.',
                           'partecipazioniFantasy':records}, f,ensure_ascii=False,indent=2)
                f.write('\n')
            result={'tipo':'SELEZIONE_CODICE','formato':FORMAT,'codice':args.code,
                    'statistiche':stats,'formazioniTotali':len(records)}
    result['dbSha256']=sha256(db)
    result['file']=filename
    result['sha256Output']=sha256(path)
    manifest=out/(base+'_MANIFEST.json')
    with manifest.open('x',encoding='utf-8',newline='\n') as f:
        json.dump(result,f,ensure_ascii=False,indent=2)
        f.write('\n')
    print('ESPORTATO:',filename,flush=True)
    print('MANIFEST:',manifest,flush=True)
    print('RIGHE:',result.get('formazioniTotali',result['statistiche'][0].get('formazioni')),flush=True)
    return 0

if __name__=='__main__':
    try: sys.exit(main())
    except Exception as exc:
        print('ERRORE Estrai-RecordsNext4Fantasy-v1:',repr(exc),file=sys.stderr,flush=True)
        sys.exit(1)
~~~~

### `tools/Test-RecordsNext4Fantasy-v1.py`

SHA256: `0c31bb75b7958a1fc773cf3e62ad6af76d5f7de222ed61593b62eeeeed47d383`

~~~~python
#!/usr/bin/env python3
"""Test di regressione Fase 2 sul DB 3.2: soli SELECT e test semantici."""
from __future__ import annotations
import argparse
import importlib.util
from pathlib import Path
import sqlite3
import sys
import unittest

HERE = Path(__file__).resolve().parent
spec = importlib.util.spec_from_file_location('fantasy_v1', HERE/'Estrai-RecordsNext4Fantasy-v1.py')
fantasy = importlib.util.module_from_spec(spec)
spec.loader.exec_module(fantasy)
DB = None


class TestCampi(unittest.TestCase):
    def test_senza_voto_e_decimali_restano_stringhe(self):
        tab = {'LISTA':'1%-1%0', 'RUOLO':'4%4%4', 'VOTO':'6,5%sv/ng%0',
               'MODIF':'0%-1%0', 'TOT':'9,5%sv/ng%0'}
        idx = fantasy.index_tab(tab)
        self.assertEqual(idx['stato'], 'LISTE_ALLINEATE')
        self.assertEqual(idx['slots'][1][0]['votoRaw'], '6,5')
        self.assertEqual(idx['slots'][1][0]['totaleRaw'], '9,5')
        self.assertEqual(idx['slots'][-1][0]['votoRaw'], 'sv/ng')
        self.assertEqual(idx['slots'][0][0]['listaRaw'], '0')

    def test_duplicazioni_non_arbitrariamente_risolte(self):
        t={'LISTA':'1%1','RUOLO':'2%2','VOTO':'6%6','MODIF':'0%0','TOT':'6%6'}
        idx=fantasy.index_tab(t)
        self.assertEqual(len(idx['slots'][1]),2)

    def test_liste_disallineate(self):
        t={'LISTA':'1%2','RUOLO':'1','VOTO':'6%7','MODIF':'0%0','TOT':'6%7'}
        self.assertEqual(fantasy.index_tab(t)['stato'],'LUNGHEZZE_DIVERSE')


class TestDatabase(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.conn=sqlite3.connect(DB.resolve().as_uri()+'?mode=ro',uri=True)
        cls.conn.row_factory=sqlite3.Row
        cls.conn.execute('PRAGMA query_only=ON')

    @classmethod
    def tearDownClass(cls):
        cls.conn.close()

    def test_21_stagioni(self):
        self.assertEqual(len(fantasy.get_seasons(self.conn)),21)

    def test_lautaro_2025_partite_162(self):
        arr=[]
        d=fantasy.run_export(self.conn,'2025_2026',100843,arr.append)
        self.assertEqual(d['formazioni'],162)
        self.assertEqual(len(arr),162)
        self.assertEqual(d['statoTabellino'],{'SLOT_UNICO':162})
        self.assertEqual(d['senzaGiocaIn'],0)
        self.assertTrue(all(x['codiceStoricoSorgente']==100843 for x in arr))
        self.assertTrue(all(x['incontroFcm'] is not None for x in arr))

    def test_2025_26_totali_e_integrita(self):
        d=fantasy.run_export(self.conn,'2025_2026',None,lambda _: None)
        self.assertEqual(d['formazioni'],39850)
        self.assertEqual(d['statoTabellino'],{'SLOT_UNICO':39850})
        self.assertEqual(d['senzaIncontro'],0)
        self.assertEqual(d['senzaGiocaIn'],0)
        self.assertEqual(d['senzaGiocatoreFca'],0)
        self.assertEqual(d['senzaCompetizione'],0)

    def test_2006_07_totali_e_integrita(self):
        d=fantasy.run_export(self.conn,'2006_2007',None,lambda _: None)
        self.assertEqual(d['formazioni'],6050)
        self.assertEqual(d['statoTabellino'],{'SLOT_UNICO':6050})

    def test_2026_27_vuota(self):
        d=fantasy.run_export(self.conn,'2026_2027',None,lambda _: None)
        self.assertEqual(d['formazioni'],0)


if __name__=='__main__':
    p=argparse.ArgumentParser()
    p.add_argument('--database',type=Path,required=True)
    args=p.parse_args()
    DB=args.database
    unittest.main(argv=[sys.argv[0]],verbosity=2)
~~~~

### `tools/Create-RecordsNext4WorkingCodeMd-v6.py`

SHA256: `f5f8b4303054fdfd6e685d514060a22ce254ed01de7ed00fb8b3bfb3cfb63ae0`

~~~~python
#!/usr/bin/env python3
"""Bibbia incrementale v6 Fase 2, genera codice esatto e impronte SHA-256."""
from __future__ import annotations
import argparse
import hashlib
from pathlib import Path
import subprocess
import sys

BASE_COMMIT = 'f9bf30323d9204d70e91e242bddfc72c2060ecf9'
PREVIOUS = 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v5.md'
BASE = 'docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md'
OUTPUT = 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v6.md'
NEW_FILES = [
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v7.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v6.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v6.md',
    'docs/SPECIFICA_ESTRAZIONE_FANTASY_RECORDSNEXT4_v1.md',
    'docs/VERIFICA_ESTRAZIONE_FANTASY_RECORDSNEXT4_v1.md',
    'tools/Estrai-RecordsNext4Fantasy-v1.py',
    'tools/Test-RecordsNext4Fantasy-v1.py',
    'tools/Create-RecordsNext4WorkingCodeMd-v6.py',
    'tools/Invoke-RecordsNext4-Fase2-v1.py',
]


def sha256(p: Path) -> str:
    h=hashlib.sha256()
    with p.open('rb') as f:
        for block in iter(lambda: f.read(1024*1024),b''):
            h.update(block)
    return h.hexdigest()


def git(project: Path, *args: str) -> str:
    p=subprocess.run(['git','-C',str(project),*args],capture_output=True,text=True,encoding='utf8',errors='replace')
    if p.returncode:
        raise RuntimeError('Git: '+p.stderr.strip())
    return p.stdout.strip()


def main() -> int:
    ap=argparse.ArgumentParser()
    ap.add_argument('--project-dir',required=True,type=Path)
    project=ap.parse_args().project_dir.resolve()
    for n in (BASE,PREVIOUS,*NEW_FILES):
        if not (project/n).is_file(): raise RuntimeError('File non presente: '+n)
    lines=['# RecordsNext 4 — Codice funzionante / bibbia incrementale v6', '',
           'Stato: Fase 2 Python sperimentale, non release 4.0 e non pipeline Java.',
           'La bibbia 3.2 e le bibbie 4.0 precedenti NON vengono alterate.', '',
           '## Collegamenti e controlli', '',
           '- Commit base: `'+BASE_COMMIT+'`',
           '- Branch: `'+git(project,'branch','--show-current')+'`',
           '- HEAD: `'+git(project,'rev-parse','HEAD')+'`',
           '- SHA bibbia 3.2: `'+sha256(project/BASE)+'`',
           '- SHA bibbia 4.0 v5: `'+sha256(project/PREVIOUS)+'`',
           '- Fase 1 testata anche su Windows: 7/7, DB SHA invariato.',
           '- Fase 2: 8/8 test e 552.025 slot univoci sulla copia SQLite;',
           '  richiede ancora verifica Windows, nessun deploy o commit.',
           '', '## File esatti aggiunti nella fase 2', '']
    for n in NEW_FILES:
        p=project/n
        body=p.read_text(encoding='utf-8-sig')
        lang='python' if n.endswith('.py') else 'markdown'
        lines.extend(['### `'+n+'`','', 'SHA256: `'+sha256(p)+'`','',
                      '~~~~'+lang,body.rstrip('\n'),'~~~~',''])
    payload=('\n'.join(lines).rstrip()+'\n').encode('utf8')
    dest=project/OUTPUT
    if dest.exists():
        if dest.read_bytes()!=payload:
            raise RuntimeError('Bibbia v6 presente ma differente: nessuna sovrascrittura '+str(dest))
        print('Bibbia v6 gia presente identica:',dest)
    else:
        with dest.open('xb') as f: f.write(payload)
        print('Bibbia v6 generata:',dest)
    return 0

if __name__=='__main__':
    try: sys.exit(main())
    except Exception as e:
        print('ERRORE BIBBIA v6:',str(e),file=sys.stderr)
        sys.exit(1)
~~~~

### `tools/Invoke-RecordsNext4-Fase2-v1.py`

SHA256: `0b4cfe1228bcb44758bb4d25e9dfb4592a61b792977a79e8fcf0319832af1348`

~~~~python
#!/usr/bin/env python3
"""Runner Fase 2 v1 per Windows: installazione conservativa e ZIP output.

Non tocca DB 3.2, FCM/FCA originali, codice Java, JS sito o branch Git.
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
from zipfile import ZipFile,ZIP_DEFLATED

BASE='f9bf30323d9204d70e91e242bddfc72c2060ecf9'
FILES=[
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v7.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v6.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v6.md',
 'docs/SPECIFICA_ESTRAZIONE_FANTASY_RECORDSNEXT4_v1.md',
 'docs/VERIFICA_ESTRAZIONE_FANTASY_RECORDSNEXT4_v1.md',
 'tools/Estrai-RecordsNext4Fantasy-v1.py',
 'tools/Test-RecordsNext4Fantasy-v1.py',
 'tools/Create-RecordsNext4WorkingCodeMd-v6.py',
 'tools/Invoke-RecordsNext4-Fase2-v1.py',
]
REQUIRED=[
 'pom.xml','docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md',
 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v5.md',
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v6.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v5.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v5.md',
 'tools/Estrai-RecordsNext4Reali-v1.py',
 'tools/Test-RecordsNext4Reali-v1.py',
]


def sha(p:Path)->str:
    h=hashlib.sha256()
    with p.open('rb') as f:
        for chunk in iter(lambda:f.read(1024*1024),b''):h.update(chunk)
    return h.hexdigest()


def git(project:Path,*args:str)->str:
    p=subprocess.run(['git','-C',str(project),*args],capture_output=True,text=True,encoding='utf8',errors='replace')
    if p.returncode:raise RuntimeError('git '+ ' '.join(args)+': '+p.stderr.strip())
    return p.stdout.strip()


def validate_project(project:Path)->dict:
    for n in REQUIRED:
        if not (project/n).is_file(): raise RuntimeError('Worktree non pronto, manca: '+n)
    root=Path(git(project,'rev-parse','--show-toplevel')).resolve()
    if os.path.normcase(str(root))!=os.path.normcase(str(project.resolve())):
        raise RuntimeError('La cartella non e la radice del worktree')
    branch=git(project,'branch','--show-current')
    if branch!='feature/recordsnext4-players':raise RuntimeError('Branch inatteso: '+branch)
    head=git(project,'rev-parse','HEAD')
    git(project,'merge-base','--is-ancestor',BASE,head)
    return {'branch':branch,'head':head,'statusBefore':git(project,'status','-sb')}


def install(root:Path,project:Path)->list[str]:
    # Controlla intero kit prima di scrivere un singolo file.
    for n in FILES:
        src=root/n; dst=project/n
        if not src.is_file():raise RuntimeError('Manca file nel kit: '+n)
        if dst.exists() and (not dst.is_file() or sha(src)!=sha(dst)):
            raise RuntimeError('File gia presente e diverso; nessuna sovrascrittura: '+n)
    added=[]
    for n in FILES:
        src=root/n;dst=project/n
        if dst.is_file():
            print('GIA PRESENTE IDENTICO:',n,flush=True)
            continue
        dst.parent.mkdir(parents=True,exist_ok=True)
        with dst.open('xb') as f:f.write(src.read_bytes())
        added.append(n)
        print('INSTALLATO:',n,flush=True)
    return added


def run(source:Path,args:list[str],log:Path)->None:
    env=dict(os.environ, PYTHONDONTWRITEBYTECODE='1')
    p=subprocess.run([sys.executable,str(source),*args],
        stdout=subprocess.PIPE,stderr=subprocess.STDOUT,encoding='utf8',errors='replace',text=True,env=env)
    log.write_text(p.stdout,encoding='utf8')
    print(source.name,'codice uscita',p.returncode,'\n',p.stdout[-550:],flush=True)
    if p.returncode:raise RuntimeError('Fallito '+source.name+'; vedere '+log.name)


def main() -> int:
    parser=argparse.ArgumentParser()
    parser.add_argument('--work-dir',type=Path,default=Path('D:/DEV_APPS/RecordsNext4.0'))
    parser.add_argument('--database',type=Path,default=Path('D:/DEV_APPS/RecordsNext2.0/data/database/recordsnext-sandbox.db'))
    parser.add_argument('--output-dir',type=Path,default=Path('D:/DEV_APPS/downloads'))
    a=parser.parse_args()
    project=a.work_dir.resolve();db=a.database.resolve();out=a.output_dir.resolve()
    if not out.is_dir():
        raise RuntimeError('Cartella output assente: '+str(out))
    stamp=datetime.now().strftime('%Y%m%d_%H%M%S_%f')
    folder=out/('RecordsNext4_Fase2_OUTPUT_v1_'+stamp)
    folder.mkdir()
    zipfile=out/(folder.name+'.zip')
    kit=Path(__file__).resolve().parent.parent
    status='ERRORE'
    manifest={'fase':2,'versioneKit':'v1','stato':status,'database':str(db),
              'worktree':str(project),'nessunDeploy':True,'nessunCommit':True}
    try:
        if not db.is_file():raise RuntimeError('DB sandbox assente: '+str(db))
        manifest.update(validate_project(project))
        before=sha(db);manifest['dbShaPrima']=before
        manifest['fileInstallati']=install(kit,project)
        run(project/'tools/Test-RecordsNext4Fantasy-v1.py',
            ['--database',str(db)],folder/'LOG_TEST_FANTASY_v1.txt')
        pilot=folder/'dati_pilota';pilot.mkdir()
        for code in (100843,12302):
            run(project/'tools/Estrai-RecordsNext4Fantasy-v1.py',
                ['--database',str(db),'--output-dir',str(pilot),'--code',str(code)],
                folder/('LOG_CODICE_'+str(code)+'_v1.txt'))
        for year in ('2006_2007','2025_2026'):
            run(project/'tools/Estrai-RecordsNext4Fantasy-v1.py',
                ['--database',str(db),'--output-dir',str(pilot),'--all','--season',year],
                folder/('LOG_ANNO_'+year+'_v1.txt'))
        run(project/'tools/Create-RecordsNext4WorkingCodeMd-v6.py',
            ['--project-dir',str(project)],folder/'LOG_BIBBIA_v6.txt')
        docs=folder/'documentazione';docs.mkdir()
        for n in (f.split('/',1)[1] for f in FILES if f.startswith('docs/')):
            shutil.copy2(project/'docs'/n,docs/n)
        shutil.copy2(project/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v6.md',
                     docs/'CODICE_FUNZIONANTE_RECORDSNEXT4_v6.md')
        after=sha(db);manifest['dbShaDopo']=after
        if before!=after:raise RuntimeError('SHA database cambiato durante audit!')
        manifest['statusAfter']=git(project,'status','-sb')
        manifest['stato']='FASE2_AUDIT_ESEGUITI'
        status='OK'
    except Exception:
        err=traceback.format_exc()
        (folder/'ERRORE_FASE2_v1.txt').write_text(err,encoding='utf8')
        print('ERRORE FASE2:',err[-2100:],file=sys.stderr,flush=True)
    finally:
        (folder/'MANIFEST_FASE2_v1.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
        with ZipFile(zipfile,'x',ZIP_DEFLATED,compresslevel=6) as z:
            for f in sorted(folder.rglob('*')):
                if f.is_file():z.write(f,f.relative_to(folder).as_posix())
        print('ESITO:',status,'\nZIP DA CARICARE:',zipfile,flush=True)
    return 0 if status=='OK' else 1

if __name__=='__main__':
    sys.exit(main())
~~~~
