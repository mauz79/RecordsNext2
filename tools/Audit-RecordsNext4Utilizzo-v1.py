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
