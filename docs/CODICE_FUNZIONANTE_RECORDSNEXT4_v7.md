# RecordsNext 4 - Codice funzionante / bibbia incrementale v7

Stato: Fase 3A audit osservazionale Python, non release e non pipeline Java.
Le bibbie 3.2 e 4.0 precedenti rimangono immutate.

## Provenienza

- Branch: `feature/recordsnext4-players`
- HEAD: `f9bf30323d9204d70e91e242bddfc72c2060ecf9`
- SHA bibbia 3.2: `91ecb0006d6d1c6fda501c7243ba65ec2f0625c40e93394079238eac558a7da3`
- SHA bibbia 4.0 v6: `97cef86dbc3ef009d877e721565251e33baa18eb6e820370c78f9f525d2e9646`
- Fase 2: audit Windows 8/8, quattro output con SHA verificati.
- Fase 3A: 9/9 test locali, 21 stagioni, 552.025 righe.
- Non sono stati stimati punti persi o simulazioni controfattuali.

## File aggiunti nella Fase 3A

### `docs/PIANO_RECORDSNEXT4_GIOCATORI_v8.md`

SHA256: `c0c3c6d1aed95c0060a2baa73b111b8da9e18ce481db1460b3a529eac9337358`

~~~~markdown
# RecordsNext 4.0 - Piano operativo v8

Base 3.2.0: commit `f9bf30323d9204d70e91e242bddfc72c2060ecf9`.
Worktree: `D:\DEV_APPS\RecordsNext4.0`.

Tutte le decisioni precedenti restano nei documenti versionati precedenti, che non vengono sovrascritti.

## Stato

- Fase 0: audit fonti/identita completati; casi ambigui documentati e aperti.
- Fase 1: estrattore dati reali Python testato in Windows, 7/7; nessuna integrazione Java.
- Fase 2: estrattore dati fantasy Python testato in Windows, 8/8; 552.025 righe Formazione, collegamenti univoci ai tabellini; DB immutato.
- Fase 3A: audit semantico di `Formazione.POS`, `ENTRATO`, `ENTRATORISERVA`, `Incontro.GIOCATO`, slot `Tabellino.TOT` (nessun calcolo punti persi).
- Fase 3B: solo dopo verifica, regole su utilizzo, riserve, RU, contribuzione e punti non sfruttati.
- Fase 4: integrazione nel modello canonico SQLite/Java, export JS, pagine HTML, GUI.
- Fase 5: regressioni, aggiornamento bibbia, commit, release 4.0.

## Criteri per la Fase 3B

1. Separare titolari POS=0, panchina POS>0, fuori formazione POS<0.
2. `TOT` numerico NON significa voto conteggiato; `ENTRATO` e `ENTRATORISERVA` devono essere interpretati alla luce di FCM e RU.
3. Distinguere almeno: contributo realmente utilizzato, voto rimasto inutilizzato in panchina, prestazione di un calciatore non schierato, eventuale contributo recuperabile sotto le regole reali, simulazioni controfattuali.
4. Documentare il denominatore per squadra-partita, stagione, competizione; non sommare la stessa prestazione FCA attraverso gare fantasy differenti.
5. Per RU e sostituzioni, confrontare il codice esistente della 3.2: `docs/ANALISI_PERSONALE_RU.md`, `tools/Report_RU_Storico_Completo_v8_4.ps1` e gli altri report RU; non cancellarli o rietichettarli come record pubblici.
6. Preservare riposi e tabellini orfani in diagnostica separata.

## Vincoli

Nessuna modifica 3.2, DB, pipeline pubblica, HTML, JS, GUI o branch Git. Niente deployment o commit. Un nuovo file richiede suffisso versione; includere sempre codice, test, SHA-256, piano, registro, diario e bibbia. Output per l'utente in `D:\DEV_APPS\downloads`.
~~~~

### `docs/REGISTRO_DECISIONI_RECORDSNEXT4_v7.md`

SHA256: `1774b805f2e843cd81abbb6512feae08be33dc5b0137cc02ce787a1d05c981c6`

~~~~markdown
# RecordsNext 4.0 - Registro decisioni v7

Sono conservate senza modifica le decisioni D-001 - D-036 nei registri precedenti. Nessuna decisione precedente viene considerata implicitamente revocata.

## Fase 2 verificata

| ID | Stato | Decisione / evidenza |
|---|---|---|
| D-037 | CONFERMATA (esito test) | Lo ZIP Windows `RecordsNext4_Fase2_OUTPUT_v1_20261031_214006_892950.zip` riporta 8/8 test riusciti, 6.050 righe 2006/07, 39.850 righe 2025/26, 1.327 partecipazioni fantasy del codice 100843, SHA dei quattro estratti validi, DB SHA prima/dopo uguale. Nessuna integrazione Java. |

## Fase 3A

| ID | Stato | Decisione / evidenza |
|---|---|---|
| D-038 | CONFERMATA (documentazione 3.2) | `Formazione.POS=0` indica gli iniziali; le posizioni positive sono la panchina; `ENTRATORISERVA` e usato nelle diagnostiche RU per contare gli ingressi effettivi. La 3.2 usa anche `ENTRATO <> 0` per alcune statistiche. Non sono sinonimi universali. |
| D-039 | IMPLEMENTATA PROTOTIPO | Audit 3A classifica senza alterare FCM: POS, ENTRATO, ENTRATORISERVA, GIOCATO e presenza di un totale individuale numerico nel Tabellino. Nessuna somma di punti. |
| D-040 | CONFERMATA (osservazione) | La panchina con `TOT` numerico e `ENTRATO=0` e distinta da `POS<0` con `TOT` numerico: non sono la stessa opportunita di scelta. Non sommarle in un singolo record. |
| D-041 | APERTA | Significato operativo dei casi `POS>0, ENTRATO=1, ENTRATORISERVA=0` e `ENTRATORISERVA=1, ENTRATO=0` prima di determinare i giocatori effettivamente conteggiati. |
| D-042 | APERTA | Punteggi sprecati: voto valido, voto non conteggiato, punti recuperabili a regolamento ed esito controfattuale sono metriche diverse; non calcolarle senza regole verificate. |
| D-043 | CONFERMATA (vincolo storico) | Le analisi RU della 3.2 sono diagnostica privata e non una famiglia di record pubblici; i quattro script RU esistenti vanno preservati. |
| D-044 | CONFERMATA (vincolo) | I tabellini non collegati e le giornate di riposo restano in un audit separato; non sono automaticamente errori. |

## Evidenze locali (da certificare con ZIP Windows 3A)

Su SQLite 3.2 ricevuto: 21 stagioni e 552.025 Formazione. Audit Fase 3A locale: 94.992 righe panchina/non entrata/TOT numerico; 38.646 POS negativo/TOT numerico; 20.295 panchina/ENTRATO=1/ENTRATORISERVA=0; 150 titolare/ENTRATO=0/TOT numerico; 43 riserva flag senza entrato. I gruppi possono sovrapporsi quando hanno criteri diversi e NON equivalgono a punti persi.
~~~~

### `docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v7.md`

SHA256: `0783ed69512e34ff40d6cd884c0441ccc346cd79ee656a7afb3a6671ee9a1a23`

~~~~markdown
# RecordsNext 4.0 - Diario implementazione v7

Data: 2026-10-31. Base 3.2.0, commit `f9bf30323d9204d70e91e242bddfc72c2060ecf9`.

## Verifica ZIP Windows Fase 2

- Acquisito `RecordsNext4_Fase2_OUTPUT_v1_20261031_214006_892950.zip`.
- Log: 8 test superati; anno 2006/07 = 6.050 righe, anno 2025/26 = 39.850; codici 100843 = 1.327, 12302 = 31.
- Quattro hash SHA-256 degli output coincidono con i manifest.
- Manifest database SHA prima/dopo = `57923f4e75986c2b81533e4beef15b9133635258d7de1d36827a1bdbe576671b`.
- Bibbia v6 generata, nessun commit/deploy dichiarato dal runner.

## Fase 3A realizzata

- Aggiunto `tools/Audit-RecordsNext4Utilizzo-v1.py`, sola lettura.
- Aggiunto `tools/Test-RecordsNext4Utilizzo-v1.py` (9 test automatici).
- Aggiunto runner `tools/Invoke-RecordsNext4-Fase3A-v1.py` che installa conservativamente, testa, estrae un report all-time in CSV/JSON e genera ZIP diagnostico.
- Bibbia v7 generata da `tools/Create-RecordsNext4WorkingCodeMd-v7.py` con codice completo e hash.
- L'audit locale ha letto 552.025 Formazione in 21 stagioni; tutte le righe sono rappresentate nelle combinazioni. Niente modifica database, niente punteggi persi stimati.

## Limiti e prossimi controlli

- Certificare la Fase 3A su Windows mediante ZIP del runner.
- Esaminare le combinazioni anomale e i campioni con il codice RU preesistente prima di definire una metrica pubblica.
- Stabilire regole per ruoli, entrate, riserve d'ufficio, moduli e numero massimo sostituzioni per periodo/competizione.
- Nessuna modifica Java, GUI, siti e release 4.0 finora.
~~~~

### `docs/SPECIFICA_AUDIT_UTILIZZO_RECORDSNEXT4_v1.md`

SHA256: `aec5b41ea691abf36c7c86e9a5bc0d8550a93eb3aecb869f2b6aa6a905029c2b`

~~~~markdown
# RecordsNext 4.0 - Specifica Fase 3A v1: utilizzo osservato

## Fonte

Tabelle SQLite raw per stagione `FCM.Formazione`, `FCM.Incontro`, `FCM.Tabellino`, `FCA.GiocatoreA` del database 3.2. Le prestazioni reali restano archiviate nella Fase 1 e NON sono duplicate per competizione.

## Campi classificati

- `POS=0`: titolare iniziale (come nelle analisi RU 3.2).
- `POS>0`: panchina.
- `POS<0`: fuori dalla formazione schierata (distinta dalle riserve).
- `ENTRATO`: flag sorgente FCM, conservato come 0/1 (non dedurre autonomamente numero di cambi).
- `ENTRATORISERVA`: secondo flag FCM; analisi RU 3.2 lo usa per i cambi realmente effettuati in specifici casi.
- `GIOCATO`: stato incontro FCM.
- `Tabellino.TOT`: slot del giocatore annuale individuato mediante liste percentuali FCM; categorizzato solo NUMERICO/NON_NUMERICO/VUOTO/MANCANTE. Non si somma.

Si produce un CSV per tutte le combinazioni osservate per stagione, un CSV con pochi campioni per categoria da studiare e un JSON riassuntivo. Il file manifest contiene SHA-256. Nessun contatto/password `Fantasquadra` viene letto o esportato.

## Marcatori diagnostici, non record pubblici

- PANCHINA_NON_ENTRATA_TOT_NUMERICO: panchinaro con totale numerico non entrato, INCONTRO.GIOCATO=1. Non significa punti recuperabili.
- FUORI_FORMAZIONE_TOT_NUMERICO: giocatore con POS negativo, non conteggiato come panchinaro.
- PANCHINA_ENTRATA_SENZA_FLAG_RISERVA: i due flag non coincidono; interpretazione da verificare.
- RISERVA_FLAG_SENZA_ENTRATO: caso inverso da approfondire.
- TITOLARE_NON_ENTRATO_TOT_NUMERICO: caso anomalo o particolare, non correttivo automatico.
- ENTRATO_INCONTRO_NON_GIOCATO: incoerenza da segnalare, se presente.
- SLOT_NON_CANONICO: mancanza/multiplicita slot, se presente.

## Limiti

I 168 tabellini senza incontro e i riposi si analizzano separatamente: il presente universo sono le righe `Formazione` con match. Codici di fantasia `0`, `-1` in LISTA non sono giocatori. La presenza di un totale numerico non certifica idoneita regolamentare, impossibilita di una sostituzione o risultato controfattuale. Non calcolare punti sprecati fino a definizione approvata. Nessun deploy, nessuna modifica al DB o al codice Java.
~~~~

### `docs/VERIFICA_AUDIT_UTILIZZO_RECORDSNEXT4_v1.md`

SHA256: `662514e143677797bd78f0fe16f76792cfe6d6cf6011f2f4a3dea5bb19706f62`

~~~~markdown
# RecordsNext 4.0 - Verifica locale Fase 3A v1

Database: copia di `recordsnext-sandbox.db` 3.2 fornita. Esecuzione locale Linux in sola lettura, **non ancora certificata Windows**.

| Controllo | Esito |
|---|---|
| Stagioni FCM | 21 |
| Righe `Formazione` analizzate | 552.025 |
| Test Python | 9/9 |
| Panchina non entrata / TOT numerico (GIOCATO=1) | 94.992 |
| POS negativo / TOT numerico (GIOCATO=1) | 38.646 |
| Panchina entrata / ENTRATORISERVA=0 (GIOCATO=1) | 20.295 |
| Titolare non entrato / TOT numerico (GIOCATO=1) | 150 |
| ENTRATORISERVA=1, ENTRATO=0 | 43 |
| Stagione 2026/27 | 0 Formazione nella snapshot |

Questi numeri sono *righe di partecipazioni di formazione*, non giocatori unici ne punti non sfruttati. La stessa prestazione reale puo apparire in molte competizioni. Non trarre record all-time prima delle regole.

## Riferimenti codice 3.2 gia riletti

- `docs/ANALISI_PERSONALE_RU.md`: diagnostica personale, non pubblica.
- `tools/Report_RU_Storico_Completo_v8_4.ps1`: casi di 11 titolari `POS=0`, utilizzo di `ENTRATORISERVA` per sostituzioni.
- `src/main/java/it/alterlega/recordsnext/SeasonNormalizedExporter.java`: alcune metriche correnti selezionano `f.ENTRATO <> 0`.

Prima di costruire record su punti persi bisogna interpretare i 20.295/43 casi e le regole di campionato. Il presente audit conserva questi casi senza modificarli.
~~~~

### `tools/Audit-RecordsNext4Utilizzo-v1.py`

SHA256: `eb3775caf43ae8e428ba27989f78327500ccc183dc4a0bf56c48850798fb745f`

~~~~python
#!/usr/bin/env python3
"""Phase 3A observational audit: FCM lineup flags and individual score slots.

Read-only. No reconstructed substitutions, fantasy points or wasted points.
Output contains only public player names, IDs, team/match IDs, and observed flags.
"""
from __future__ import annotations
import argparse
from collections import Counter, defaultdict
from datetime import datetime
from decimal import Decimal, InvalidOperation
import hashlib
import json
from pathlib import Path
import re
import sqlite3
import csv
import sys

SEASON = re.compile(r'^\d{4}_\d{4}$')
RAW_TABLES = ('fcm_formazione', 'fcm_incontro', 'fcm_tabellino', 'fca_giocatorea')


def sha256(path: Path) -> str:
    h = hashlib.sha256()
    with path.open('rb') as file:
        for chunk in iter(lambda: file.read(1024 * 1024), b''):
            h.update(chunk)
    return h.hexdigest()


def table(season: str, family: str) -> str:
    if not SEASON.fullmatch(season) or family not in RAW_TABLES:
        raise ValueError('Table not authorized: ' + repr((season, family)))
    return '"raw_' + season + '_' + family + '"'


def seasons(c: sqlite3.Connection) -> list[str]:
    found = []
    for (name,) in c.execute("SELECT name FROM sqlite_master WHERE type='table'"):
        m = re.fullmatch(r'raw_(\d{4}_\d{4})_fcm_formazione', name.lower())
        if m:
            found.append(m.group(1))
    return sorted(set(found))


def score_type(text: str | None) -> str:
    """Classify a source tabellino TOT as numeric / non-numeric / absent.

    Being numeric does NOT imply a legal or recoverable fantasy score.
    """
    if text is None:
        return 'MANCANTE'
    value = str(text).strip()
    if not value:
        return 'VUOTO'
    try:
        n = Decimal(value.replace(',', '.'))
        return 'NUMERICO' if n.is_finite() else 'NON_NUMERICO'
    except InvalidOperation:
        return 'NON_NUMERICO'


def index_tabellino(raw: dict) -> tuple[str, dict[int, list[str]]]:
    fields = [str(raw.get(k) or '').split('%') if raw.get(k) else []
              for k in ('LISTA', 'RUOLO', 'VOTO', 'MODIF', 'TOT')]
    if len({len(x) for x in fields}) != 1:
        return 'LUNGHEZZE_DIVERSE', {}
    out: dict[int, list[str]] = defaultdict(list)
    for pid, tot in zip(fields[0], fields[-1]):
        try:
            out[int(pid)].append(tot)
        except ValueError:
            continue
    return 'ALLINEATO', out


def pos_group(value) -> str:
    if value is None:
        return 'POS_NULL'
    if value == 0:
        return 'POS_0_TITOLARE'
    return 'POS_POS_PANCHINA' if value > 0 else 'POS_NEG_FUORI'


def flag(value) -> str:
    if value is None:
        return 'NULL'
    return '1' if value != 0 else '0'


def audit_season(c: sqlite3.Connection, year: str, max_samples: int = 3) -> dict:
    needed = {'raw_' + year + '_' + family for family in RAW_TABLES}
    existing = {r[0].lower() for r in c.execute("SELECT name FROM sqlite_master WHERE type='table'")}
    if missing := needed - existing:
        raise RuntimeError('Missing tables: ' + ', '.join(sorted(missing)))
    matches = {}
    for row in c.execute('SELECT ID,GIOCATO FROM ' + table(year, 'fcm_incontro')):
        if row['ID'] in matches:
            raise RuntimeError('Duplicated match ID ' + str((year, row['ID'])))
        matches[row['ID']] = row['GIOCATO']
    players = {r['ID']: (r['NOME'], r['CODICEGAZZA'])
               for r in c.execute('SELECT ID,NOME,CODICEGAZZA FROM ' + table(year, 'fca_giocatorea'))}
    tabs = defaultdict(list)
    for row in c.execute('SELECT IDINCONTRO,IDSQUADRA,LISTA,RUOLO,VOTO,MODIF,TOT FROM ' + table(year, 'fcm_tabellino')):
        state, idx = index_tabellino(dict(row))
        tabs[(row['IDINCONTRO'], row['IDSQUADRA'])].append((state,idx))
    combos = Counter()
    positions = Counter()
    score_counts = Counter()
    markers = Counter()
    examples = defaultdict(list)
    count = 0
    for row in c.execute('SELECT IDINCONTRO,IDSQUADRA,IDGIOC,POS,RIG,ENTRATO,ENTRATORISERVA FROM '+table(year,'fcm_formazione')):
        count += 1
        mid,tid,pid = row['IDINCONTRO'],row['IDSQUADRA'],row['IDGIOC']
        played = matches.get(mid)
        if mid not in matches:
            played = None
        pos = pos_group(row['POS'])
        entered, reserve = flag(row['ENTRATO']), flag(row['ENTRATORISERVA'])
        t = tabs.get((mid,tid), [])
        if len(t) != 1:
            score = 'TABELLINO_ASSENTE' if not t else 'TABELLINO_MULTIPLO'
            raw = None
        else:
            state, slotidx = t[0]
            if state != 'ALLINEATO':
                score = state
                raw = None
            else:
                slots = slotidx.get(pid,[])
                if len(slots) != 1:
                    score = 'SLOT_ASSENTE' if not slots else 'SLOT_MULTIPLO'
                    raw = None
                else:
                    raw=slots[0]
                    score=score_type(raw)
        status = 'GIOCATO_1' if played == 1 else ('GIOCATO_0' if played == 0 else 'GIOCATO_NON_RISOLTO')
        key=(pos,entered,reserve,status,score)
        combos[key] += 1
        positions[pos] += 1
        score_counts[score] += 1
        tags=[]
        if reserve=='1' and entered!='1': tags.append('RISERVA_FLAG_SENZA_ENTRATO')
        if status=='GIOCATO_1' and pos=='POS_POS_PANCHINA' and entered=='0' and score=='NUMERICO':
            tags.append('PANCHINA_NON_ENTRATA_TOT_NUMERICO')
        if status=='GIOCATO_1' and pos=='POS_NEG_FUORI' and score=='NUMERICO':
            tags.append('FUORI_FORMAZIONE_TOT_NUMERICO')
        if status=='GIOCATO_1' and pos=='POS_0_TITOLARE' and entered=='0' and score=='NUMERICO':
            tags.append('TITOLARE_NON_ENTRATO_TOT_NUMERICO')
        if status=='GIOCATO_1' and pos=='POS_POS_PANCHINA' and entered=='1' and reserve=='0':
            tags.append('PANCHINA_ENTRATA_SENZA_FLAG_RISERVA')
        if status=='GIOCATO_0' and entered=='1': tags.append('ENTRATO_INCONTRO_NON_GIOCATO')
        if score not in ('NUMERICO','NON_NUMERICO','VUOTO'):
            tags.append('SLOT_NON_CANONICO')
        for tag in tags:
            markers[tag] += 1
            if len(examples[tag])<max_samples:
                player,code=players.get(pid,(None,None))
                examples[tag].append({'stagione':year,'categoria':tag,'idIncontroFcm':mid,
                                      'idSquadraFcm':tid,'idGiocatoreFca':pid,'codiceStorico':code,
                                      'nomeGiocatore':player,'POS':row['POS'],'RIG':row['RIG'],
                                      'ENTRATO':row['ENTRATO'],'ENTRATORISERVA':row['ENTRATORISERVA'],
                                      'GIOCATO':played,'totaleRaw':raw})
    return {'stagione':year,'formazioni':count,'posizioni':dict(positions),
            'totaliTabellino':dict(score_counts),'marcatori':dict(markers),
            'combinazioni':combos,'campioni':examples}


def export(c: sqlite3.Connection, output_dir: Path, wanted: list[str]) -> dict:
    combinations=[]; samples=[]; yearstats=[]
    for year in wanted:
        s=audit_season(c,year)
        if sum(s['combinazioni'].values())!=s['formazioni']:
            raise RuntimeError('Cross-tab incomplete: '+year)
        expected=c.execute('SELECT COUNT(*) FROM '+table(year,'fcm_formazione')).fetchone()[0]
        if expected!=s['formazioni']:
            raise RuntimeError('Source row count differs: '+year)
        for (pos,entered,reserve,played,score),n in sorted(s['combinazioni'].items()):
            combinations.append({'stagione':year,'pos':pos,'entrato':entered,
                                 'entratoRiserva':reserve,'incontro':played,
                                 'totale':score,'righe':n})
        for arr in s['campioni'].values(): samples.extend(arr)
        del s['combinazioni'],s['campioni']
        yearstats.append(s)
        print('CONTROLLATO:',year,'righe:',expected,flush=True)
    files=[]
    def writecsv(name,fields,rows):
        dest=output_dir/name
        with dest.open('x',encoding='utf-8-sig',newline='') as f:
            w=csv.DictWriter(f,fieldnames=fields,delimiter=';',extrasaction='ignore')
            w.writeheader();w.writerows(rows)
        files.append(dest)
    writecsv('RecordsNext4_Utilizzo_v1_combinazioni.csv',
             ('stagione','pos','entrato','entratoRiserva','incontro','totale','righe'),combinations)
    writecsv('RecordsNext4_Utilizzo_v1_campioni.csv',
             ('stagione','categoria','idIncontroFcm','idSquadraFcm','idGiocatoreFca',
              'codiceStorico','nomeGiocatore','POS','RIG','ENTRATO','ENTRATORISERVA','GIOCATO','totaleRaw'),samples)
    summary={'formato':'RecordsNext4.UtilizzoOsservato.v1',
             'nota':'NUMERICO non significa utilizzato/recuperabile. Nessun punto perso calcolato.',
             'stagioni':yearstats,
             'totaleFormazioni':sum(s['formazioni'] for s in yearstats),
             'totaleMarcatori':dict(sum((Counter(s['marcatori']) for s in yearstats),Counter()))}
    dest=output_dir/'RecordsNext4_Utilizzo_v1_sintesi.json'
    with dest.open('x',encoding='utf-8') as f: json.dump(summary,f,ensure_ascii=False,indent=2);f.write('\n')
    files.append(dest)
    return {'formato':summary['formato'],'stagioniEsaminate':len(yearstats),
            'totaleFormazioni':summary['totaleFormazioni'],'totaleMarcatori':summary['totaleMarcatori'],
            'file':[{'nome':f.name,'sha256':sha256(f),'byte':f.stat().st_size} for f in files]}


def main() -> int:
    ap=argparse.ArgumentParser(description=__doc__)
    ap.add_argument('--database',type=Path,required=True)
    ap.add_argument('--output-dir',type=Path,required=True)
    ap.add_argument('--season',default=None,help='Optional AAAA_AAAA')
    args=ap.parse_args()
    db=args.database.resolve(strict=True)
    out=args.output_dir.resolve()
    out.mkdir(parents=True,exist_ok=True)
    with sqlite3.connect(db.as_uri()+'?mode=ro',uri=True) as c:
        c.row_factory=sqlite3.Row
        c.execute('PRAGMA query_only=ON')
        ys=seasons(c)
        if args.season:
            if args.season not in ys: ap.error('Unknown season '+args.season)
            ys=[args.season]
        if not ys: raise RuntimeError('No seasons found in DB')
        result=export(c,out,ys)
    result['sha256Database']=sha256(db)
    manifest=out/'RecordsNext4_Utilizzo_v1_MANIFEST.json'
    with manifest.open('x',encoding='utf8') as f: json.dump(result,f,ensure_ascii=False,indent=2);f.write('\n')
    print('FORMATIONS:',result['totaleFormazioni'],'SEASONS:',len(ys),flush=True)
    print('MANIFEST:',manifest,flush=True)
    return 0

if __name__=='__main__':
    try: sys.exit(main())
    except Exception as exc:
        print('ERRORE AUDIT UTILIZZO v1:',repr(exc),file=sys.stderr,flush=True)
        sys.exit(1)
~~~~

### `tools/Test-RecordsNext4Utilizzo-v1.py`

SHA256: `92d43e344dcc4d1c8f467e95beb0ba9c4bd02a22f6ec46adeb35b6c4036d1506`

~~~~python
#!/usr/bin/env python3
"""Tests for read-only observational flags audit."""
from __future__ import annotations
import argparse
from pathlib import Path
import sqlite3
import sys
import unittest
import importlib.util

SCRIPT=Path(__file__).with_name('Audit-RecordsNext4Utilizzo-v1.py')
SPEC=importlib.util.spec_from_file_location('rn4_audit_utilizzo_v1',SCRIPT)
MOD=importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(MOD)
DB=None

class TestFormat(unittest.TestCase):
    def test_numeric_decimals(self):
        self.assertEqual(MOD.score_type('7,5'), 'NUMERICO')
        self.assertEqual(MOD.score_type('-1.5'), 'NUMERICO')
    def test_non_numeric(self):
        self.assertEqual(MOD.score_type('sv/ng'), 'NON_NUMERICO')
        self.assertEqual(MOD.score_type('NaN'), 'NON_NUMERICO')
        self.assertEqual(MOD.score_type(''), 'VUOTO')
    def test_tab_alignment_and_special(self):
        s, data=MOD.index_tabellino({'LISTA':'0%42%-1','RUOLO':'0%1%1',
                                    'VOTO':'0%6%0','MODIF':'0%0%0','TOT':'0%6%0'})
        self.assertEqual(s,'ALLINEATO')
        self.assertEqual(data[42],['6'])
        self.assertEqual(data[0],['0'])
        self.assertEqual(data[-1],['0'])
    def test_non_aligned(self):
        s,_=MOD.index_tabellino({'LISTA':'1%2','RUOLO':'0','VOTO':'6%7','MODIF':'0%0','TOT':'6%7'})
        self.assertEqual(s,'LUNGHEZZE_DIVERSE')
    def test_position(self):
        self.assertEqual(MOD.pos_group(0),'POS_0_TITOLARE')
        self.assertEqual(MOD.pos_group(-1),'POS_NEG_FUORI')
        self.assertEqual(MOD.pos_group(9),'POS_POS_PANCHINA')

class TestDatabase(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.conn=sqlite3.connect(DB.as_uri()+'?mode=ro',uri=True)
        cls.conn.row_factory=sqlite3.Row
        cls.conn.execute('PRAGMA query_only=ON')
    @classmethod
    def tearDownClass(cls): cls.conn.close()
    def test_all_season_count(self):
        self.assertEqual(len(MOD.seasons(self.conn)),21)
    def test_2006(self):
        r=MOD.audit_season(self.conn,'2006_2007')
        self.assertEqual(r['formazioni'],6050)
        self.assertEqual(sum(r['combinazioni'].values()),6050)
        self.assertEqual(r['marcatori']['PANCHINA_NON_ENTRATA_TOT_NUMERICO'],837)
    def test_2025(self):
        r=MOD.audit_season(self.conn,'2025_2026')
        self.assertEqual(r['formazioni'],39850)
        self.assertEqual(sum(r['combinazioni'].values()),39850)
        self.assertEqual(r['marcatori']['PANCHINA_NON_ENTRATA_TOT_NUMERICO'],7970)
        self.assertEqual(r['marcatori']['PANCHINA_ENTRATA_SENZA_FLAG_RISERVA'],1547)
    def test_2026_empty(self):
        r=MOD.audit_season(self.conn,'2026_2027')
        self.assertEqual(r['formazioni'],0)

def main():
    global DB
    parser=argparse.ArgumentParser()
    parser.add_argument('--database',required=True,type=Path)
    args=parser.parse_args()
    DB=args.database.resolve(strict=True)
    suite=unittest.defaultTestLoader.loadTestsFromModule(sys.modules[__name__])
    outcome=unittest.TextTestRunner(verbosity=2).run(suite)
    return 0 if outcome.wasSuccessful() else 1

if __name__=='__main__': sys.exit(main())
~~~~

### `tools/Create-RecordsNext4WorkingCodeMd-v7.py`

SHA256: `510c86d3de70133d191d9b8be3ae1c0fedf5566023344558dbfeefd28eeba7b6`

~~~~python
#!/usr/bin/env python3
"""Generate additive Bible v7 with exact Phase 3A files and sha256 hashes."""
from __future__ import annotations
import argparse
import hashlib
from pathlib import Path
import subprocess
import sys

BASE='docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md'
PREVIOUS='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v6.md'
DEST='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v7.md'
NEW_FILES=[
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v8.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v7.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v7.md',
 'docs/SPECIFICA_AUDIT_UTILIZZO_RECORDSNEXT4_v1.md',
 'docs/VERIFICA_AUDIT_UTILIZZO_RECORDSNEXT4_v1.md',
 'tools/Audit-RecordsNext4Utilizzo-v1.py',
 'tools/Test-RecordsNext4Utilizzo-v1.py',
 'tools/Create-RecordsNext4WorkingCodeMd-v7.py',
 'tools/Invoke-RecordsNext4-Fase3A-v1.py',
]

def sha(p:Path)->str:
    h=hashlib.sha256()
    with p.open('rb') as f:
        for chunk in iter(lambda:f.read(1024*1024),b''):h.update(chunk)
    return h.hexdigest()

def git(project:Path,*args:str)->str:
    p=subprocess.run(['git','-C',str(project),*args],capture_output=True,text=True,
                     encoding='utf8',errors='replace')
    if p.returncode:raise RuntimeError('Git command failed: '+' '.join(args)+' '+p.stderr)
    return p.stdout.strip()

def main():
    ap=argparse.ArgumentParser();ap.add_argument('--project-dir',required=True,type=Path)
    pr=ap.parse_args().project_dir.resolve()
    for f in (BASE,PREVIOUS,*NEW_FILES):
        if not (pr/f).is_file():raise RuntimeError('Missing file: '+f)
    lines=['# RecordsNext 4 - Codice funzionante / bibbia incrementale v7','',
           'Stato: Fase 3A audit osservazionale Python, non release e non pipeline Java.',
           'Le bibbie 3.2 e 4.0 precedenti rimangono immutate.','',
           '## Provenienza','',
           '- Branch: `'+git(pr,'branch','--show-current')+'`',
           '- HEAD: `'+git(pr,'rev-parse','HEAD')+'`',
           '- SHA bibbia 3.2: `'+sha(pr/BASE)+'`',
           '- SHA bibbia 4.0 v6: `'+sha(pr/PREVIOUS)+'`',
           '- Fase 2: audit Windows 8/8, quattro output con SHA verificati.',
           '- Fase 3A: 9/9 test locali, 21 stagioni, 552.025 righe.',
           '- Non sono stati stimati punti persi o simulazioni controfattuali.','',
           '## File aggiunti nella Fase 3A','']
    for f in NEW_FILES:
        p=pr/f
        lang='python' if f.endswith('.py') else 'markdown'
        lines.extend(['### `'+f+'`','','SHA256: `'+sha(p)+'`','','~~~~'+lang,
                      p.read_text(encoding='utf-8-sig').rstrip('\n'),'~~~~',''])
    data=('\n'.join(lines).rstrip()+'\n').encode('utf8')
    target=pr/DEST
    if target.exists():
        if target.read_bytes()!=data:raise RuntimeError('Bible v7 differs; refusing to overwrite: '+str(target))
        print('BIBBIA v7 gia presente identica: '+str(target))
    else:
        with target.open('xb') as f:f.write(data)
        print('BIBBIA v7 generata: '+str(target))
    return 0

if __name__=='__main__':
    try:sys.exit(main())
    except Exception as exc:
        print('ERRORE BIBBIA v7: '+str(exc),file=sys.stderr);sys.exit(1)
~~~~

### `tools/Invoke-RecordsNext4-Fase3A-v1.py`

SHA256: `a20109422b212fbe01ceae757b15ccd05fb04ec5d42ec3d284e34ebae9173320`

~~~~python
#!/usr/bin/env python3
"""RecordsNext 4.0 Phase 3A conservative installer and diagnostics archive.

Only creates new versioned files in the existing 4.0 worktree and reports
under downloads. Uses the operational 3.2 SQLite database read-only.
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
from zipfile import ZipFile, ZIP_DEFLATED

BASE='f9bf30323d9204d70e91e242bddfc72c2060ecf9'
FILES=[
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v8.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v7.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v7.md',
 'docs/SPECIFICA_AUDIT_UTILIZZO_RECORDSNEXT4_v1.md',
 'docs/VERIFICA_AUDIT_UTILIZZO_RECORDSNEXT4_v1.md',
 'tools/Audit-RecordsNext4Utilizzo-v1.py',
 'tools/Test-RecordsNext4Utilizzo-v1.py',
 'tools/Create-RecordsNext4WorkingCodeMd-v7.py',
 'tools/Invoke-RecordsNext4-Fase3A-v1.py',
]
REQUIRED=[
 'pom.xml','docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md',
 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v6.md',
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v7.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v6.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v6.md',
 'tools/Estrai-RecordsNext4Fantasy-v1.py',
 'tools/Test-RecordsNext4Fantasy-v1.py',
]

def sha(path:Path)->str:
    h=hashlib.sha256()
    with path.open('rb') as f:
        for chunk in iter(lambda:f.read(1024*1024),b''):h.update(chunk)
    return h.hexdigest()

def git(project:Path,*argv:str)->str:
    p=subprocess.run(['git','-C',str(project),*argv],capture_output=True,
                     text=True,encoding='utf8',errors='replace')
    if p.returncode:raise RuntimeError('git '+ ' '.join(argv)+': '+p.stderr.strip())
    return p.stdout.strip()

def validate_project(project:Path)->dict:
    for f in REQUIRED:
        if not (project/f).is_file():raise RuntimeError('Worktree incompleto: '+f)
    root=Path(git(project,'rev-parse','--show-toplevel')).resolve()
    if os.path.normcase(str(root))!=os.path.normcase(str(project)):
        raise RuntimeError('La directory non e radice del worktree: '+str(root))
    branch=git(project,'branch','--show-current')
    if branch!='feature/recordsnext4-players':raise RuntimeError('Branch inatteso: '+branch)
    git(project,'merge-base','--is-ancestor',BASE,'HEAD')
    return {'branch':branch,'head':git(project,'rev-parse','HEAD'),
            'gitStatusPrima':git(project,'status','-sb')}

def install(source:Path,project:Path)->list[str]:
    # Check everything first: refuse to replace a changed file.
    for f in FILES:
        src,dst=source/f,project/f
        if not src.is_file():raise RuntimeError('File kit mancante: '+f)
        if dst.exists() and (not dst.is_file() or sha(src)!=sha(dst)):
            raise RuntimeError('File esistente differente; nessuna sovrascrittura: '+f)
    result=[]
    for f in FILES:
        src,dst=source/f,project/f
        if dst.is_file():
            print('GIA PRESENTE IDENTICO:',f,flush=True)
            continue
        dst.parent.mkdir(parents=True,exist_ok=True)
        with dst.open('xb') as out:out.write(src.read_bytes())
        result.append(f)
        print('INSTALLATO:',f,flush=True)
    return result

def execute(script:Path,args:list[str],log:Path)->None:
    env=dict(os.environ,PYTHONDONTWRITEBYTECODE='1')
    p=subprocess.run([sys.executable,str(script),*args],capture_output=True,
                     text=True,encoding='utf8',errors='replace',env=env)
    output=p.stdout+p.stderr
    log.write_text(output,encoding='utf8')
    print(script.name, 'codice:',p.returncode,flush=True)
    if p.returncode:raise RuntimeError('Errore in '+script.name+'; log: '+str(log))

def main()->int:
    ap=argparse.ArgumentParser()
    ap.add_argument('--work-dir',type=Path,default=Path('D:/DEV_APPS/RecordsNext4.0'))
    ap.add_argument('--database',type=Path,default=Path('D:/DEV_APPS/RecordsNext2.0/data/database/recordsnext-sandbox.db'))
    ap.add_argument('--output-dir',type=Path,default=Path('D:/DEV_APPS/downloads'))
    args=ap.parse_args()
    work=args.work_dir.resolve()
    db=args.database.resolve()
    out=args.output_dir.resolve()
    if not out.is_dir():raise RuntimeError('Output dir assente: '+str(out))
    stamp=datetime.now().strftime('%Y%m%d_%H%M%S_%f')
    result_dir=out/('RecordsNext4_Fase3A_OUTPUT_v1_'+stamp)
    result_dir.mkdir(exist_ok=False)
    zip_path=out/(result_dir.name+'.zip')
    kit=Path(__file__).resolve().parent.parent
    manifest={'fase':'3A','kitVersion':'v1','worktree':str(work),'database':str(db),
              'nessunCommit':True,'nessunDeploy':True,'stato':'ERRORE'}
    ok=False
    try:
        if not db.is_file():raise RuntimeError('Database sandbox non trovato: '+str(db))
        manifest.update(validate_project(work))
        manifest['dbShaPrima']=sha(db)
        manifest['fileInstallati']=install(kit,work)
        execute(work/'tools/Test-RecordsNext4Utilizzo-v1.py',
                ['--database',str(db)],result_dir/'LOG_TEST_UTILIZZO_v1.txt')
        data=result_dir/'dati_audit'
        data.mkdir()
        execute(work/'tools/Audit-RecordsNext4Utilizzo-v1.py',
                ['--database',str(db),'--output-dir',str(data)],
                result_dir/'LOG_AUDIT_UTILIZZO_v1.txt')
        summary=json.loads((data/'RecordsNext4_Utilizzo_v1_MANIFEST.json').read_text(encoding='utf8'))
        manifest['righeAudit']=summary['totaleFormazioni']
        manifest['stagioniAudit']=summary['stagioniEsaminate']
        execute(work/'tools/Create-RecordsNext4WorkingCodeMd-v7.py',
                ['--project-dir',str(work)],result_dir/'LOG_BIBBIA_v7.txt')
        docs=result_dir/'documentazione'
        docs.mkdir()
        for f in FILES:
            if f.startswith('docs/'):
                shutil.copy2(work/f,docs/Path(f).name)
        shutil.copy2(work/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v7.md',
                     docs/'CODICE_FUNZIONANTE_RECORDSNEXT4_v7.md')
        manifest['dbShaDopo']=sha(db)
        if manifest['dbShaPrima']!=manifest['dbShaDopo']:
            raise RuntimeError('Il database e cambiato durante la fase 3A!')
        manifest['gitStatusDopo']=git(work,'status','-sb')
        manifest['stato']='OK'
        ok=True
    except Exception:
        error=traceback.format_exc()
        (result_dir/'ERRORE_FASE3A_v1.txt').write_text(error,encoding='utf8')
        print('ERRORE FASE 3A:',error[-1400:],file=sys.stderr,flush=True)
    finally:
        (result_dir/'MANIFEST_FASE3A_v1.json').write_text(json.dumps(manifest,indent=2,ensure_ascii=False)+'\n',encoding='utf8')
        with ZipFile(zip_path,'x',compression=ZIP_DEFLATED,compresslevel=6) as z:
            for file in sorted(result_dir.rglob('*')):
                if file.is_file():z.write(file,file.relative_to(result_dir).as_posix())
        print('ESITO:', 'OK' if ok else 'ERRORE',flush=True)
        print('ZIP DA CARICARE:',zip_path,flush=True)
    return 0 if ok else 1

if __name__=='__main__':sys.exit(main())
~~~~
