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
