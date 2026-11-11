#!/usr/bin/env python3
"""RecordsNext4 Phase 3B1: evidence audit of individual scores in FCM tabellini.

READ ONLY. Records membership in the first eleven LISTA slots, not "points lost".
Never modifies SQLite, exported site assets, or fantasy results.
"""
from __future__ import annotations
import argparse
from collections import Counter, defaultdict
import csv
import hashlib
import json
from pathlib import Path
import re
import sqlite3
import sys

SEASON = re.compile(r'^\d{4}_\d{4}$')
FAMILIES = ('fcm_formazione', 'fcm_tabellino', 'fcm_incontro', 'fca_giocatorea')


def sha(path: Path) -> str:
    h = hashlib.sha256()
    with path.open('rb') as f:
        for block in iter(lambda: f.read(1024 * 1024), b''):
            h.update(block)
    return h.hexdigest()


def tbl(year: str, fam: str) -> str:
    if not SEASON.fullmatch(year) or fam not in FAMILIES:
        raise ValueError('Bad table request')
    return '"raw_' + year + '_' + fam + '"'


def seasons(c: sqlite3.Connection) -> list[str]:
    return sorted(set(m.group(1) for (n,) in c.execute("SELECT name FROM sqlite_master WHERE type='table'")
                      if (m := re.fullmatch(r'raw_(\d{4}_\d{4})_fcm_formazione', n.lower()))))


def split(raw) -> list[str]:
    return str(raw).split('%') if raw is not None and str(raw) != '' else []


def position(value) -> str:
    if value is None:
        return 'NULL'
    return 'TITOLARE_INIZIALE' if value == 0 else ('PANCHINA' if value > 0 else 'FUORI_FORMAZIONE')


def role_group(value: str) -> str:
    try:
        n = int(value)
    except (ValueError, TypeError):
        return 'ALTRO'
    return 'POSITIVO' if n > 0 else ('NEGATIVO' if n < 0 else 'ZERO')


def analyze(c: sqlite3.Connection, year: str, max_examples: int = 4) -> dict:
    required = {'raw_' + year + '_' + f for f in FAMILIES}
    actual = {r[0].lower() for r in c.execute("SELECT name FROM sqlite_master WHERE type='table'")}
    if missing := required - actual:
        raise ValueError('Missing tables ' + str(sorted(missing)))
    matches = {r['ID']: dict(r) for r in c.execute('SELECT ID,GIOCATO,IDTIPO,IDCASA,IDFUORI FROM '+tbl(year,'fcm_incontro'))}
    names = {r['ID']: (r['NOME'], r['CODICEGAZZA']) for r in c.execute('SELECT ID,NOME,CODICEGAZZA FROM '+tbl(year,'fca_giocatorea'))}
    formations = defaultdict(dict)
    count_formations = 0
    for r in c.execute('SELECT IDINCONTRO,IDSQUADRA,IDGIOC,POS,ENTRATO,ENTRATORISERVA,RIG FROM '+tbl(year,'fcm_formazione')):
        key = (r['IDINCONTRO'], r['IDSQUADRA'])
        pid = r['IDGIOC']
        if pid in formations[key]:
            raise ValueError('Duplicate formazione player '+repr((year,key,pid)))
        formations[key][pid] = dict(r)
        count_formations += 1
    counts = Counter()
    combinations = Counter()
    examples = defaultdict(list)
    seen_groups = set()

    def note(kind: str, match, team, pid=None, index=None, role=None, tot=None, form=None, info=None):
        counts[kind] += 1
        if len(examples[kind]) < max_examples:
            name, code = names.get(pid, ('', ''))
            examples[kind].append({
                'stagione':year,'categoria':kind,'idIncontro':match,'idSquadra':team,
                'idGiocatoreFca':pid,'codiceStorico':code,'nomeGiocatore':name,
                'indiceLista':index,'ruoloTabellino':role,'totRaw':tot,
                'pos':form.get('POS') if form else None,
                'entrato':form.get('ENTRATO') if form else None,
                'entratoRiserva':form.get('ENTRATORISERVA') if form else None,
                'informazione':info})

    for r in c.execute('SELECT IDINCONTRO,IDSQUADRA,LISTA,RUOLO,TOT,TOTALESQUADRAESISTE FROM '+tbl(year,'fcm_tabellino')):
        counts['TABELLO_RIGHE'] += 1
        mid, tid = r['IDINCONTRO'], r['IDSQUADRA']
        key = (mid, tid)
        if key in seen_groups:
            note('TABELLO_CHIAVE_DUPLICATA', mid, tid)
            continue
        seen_groups.add(key)
        match = matches.get(mid)
        if match is None:
            note('TABELLO_ID_INCONTRO_ASSENTE',mid,tid)
        else:
            if match['IDCASA'] == 0 or match['IDFUORI'] == 0:
                note('TABELLO_INCONTRO_SQUADRA_ZERO',mid,tid,info='tipo='+str(match['IDTIPO']))
            if tid not in (match['IDCASA'],match['IDFUORI']):
                note('TABELLO_SQUADRA_ESTRANEA_INCONTRO',mid,tid,info='tipo='+str(match['IDTIPO']))
        team_form = formations.get(key)
        if not team_form:
            note('TABELLO_SENZA_FORMAZIONE',mid,tid,info='incontroPresente='+str(match is not None))
        ids,roles,tots = split(r['LISTA']),split(r['RUOLO']),split(r['TOT'])
        if not (len(ids) == len(roles) == len(tots)):
            note('TABELLO_LISTE_NON_ALLINEATE',mid,tid,info=f'{len(ids)}:{len(roles)}:{len(tots)}')
            continue
        if len(ids) < 11:
            note('TABELLO_MENO_DI_11_SLOT',mid,tid,info='slot='+str(len(ids)))
        tab_seen=set()
        for ix, text in enumerate(ids):
            counts['SLOT_TUTTI'] += 1
            try:
                pid = int(text)
            except (ValueError, TypeError):
                note('SLOT_ID_NON_INTERO',mid,tid,index=ix,role=roles[ix],tot=tots[ix],info=text)
                continue
            if pid <= 0:
                counts['SLOT_SPECIALI'] += 1
                if ix < 11:
                    note('SLOT_SPECIALE_PRIMI11',mid,tid,pid,ix,roles[ix],tots[ix])
                continue
            if pid in tab_seen:
                note('SLOT_GIOCATORE_DUPLICATO',mid,tid,pid,ix,roles[ix],tots[ix])
                continue
            tab_seen.add(pid)
            if not team_form or pid not in team_form:
                if team_form:
                    note('SLOT_SENZA_RIGA_FORMAZIONE',mid,tid,pid,ix,roles[ix],tots[ix])
                continue
            f=team_form[pid]
            where = 'PRIMI11' if ix < 11 else 'DOPO_PRIMI11'
            pos=position(f['POS'])
            entr='1' if f['ENTRATO'] else '0'
            ersv='1' if f['ENTRATORISERVA'] else '0'
            played = str(match['GIOCATO']) if match is not None else 'ASSENTE'
            keyc=(year,where,pos,entr,ersv,role_group(roles[ix]),played)
            combinations[keyc] += 1
            counts['FORMAZIONI_CON_SLOT'] += 1
            if where=='PRIMI11':
                counts['GIOCATORI_PRIMI11'] += 1
                if entr == '0':
                    note('PRIMI11_ENTRATO_ZERO',mid,tid,pid,ix,roles[ix],tots[ix],f)
            elif entr == '1':
                note('DOPO_PRIMI11_ENTRATO_UNO',mid,tid,pid,ix,roles[ix],tots[ix],f)
            if ersv == '1' and where == 'DOPO_PRIMI11':
                note('FLAG_RISERVA_FUORI_PRIMI11',mid,tid,pid,ix,roles[ix],tots[ix],f)
            if pos == 'TITOLARE_INIZIALE' and where == 'DOPO_PRIMI11':
                counts['TITOLARI_INIZIALI_NON_NEI_PRIMI11'] += 1
            if pos == 'PANCHINA' and where == 'PRIMI11':
                counts['PANCHINARI_NEI_PRIMI11'] += 1
        if team_form:
            for pid,f in team_form.items():
                if pid not in tab_seen:
                    note('FORMAZIONE_SENZA_SLOT',mid,tid,pid,form=f)
    for mid,tid in formations:
        if (mid,tid) not in seen_groups:
            note('FORMAZIONE_SENZA_TABELLINO',mid,tid)
    if counts['FORMAZIONI_CON_SLOT'] + counts['FORMAZIONE_SENZA_SLOT'] != count_formations:
        raise ValueError('Formation coverage mismatch: '+year)
    counts['RIGHE_FORMAZIONE'] = count_formations
    counts['GRUPPI_FORMAZIONE'] = len(formations)
    return {'stagione':year,'contatori':dict(counts), 'combinazioni':combinations,'campioni':examples}


def export(c: sqlite3.Connection, years: list[str], output: Path) -> dict:
    all_counts = Counter()
    csv_combos=[]
    csv_examples=[]
    stats=[]
    for year in years:
        item=analyze(c,year)
        for k,v in sorted(item['combinazioni'].items()):
            csv_combos.append(dict(zip(('stagione','selezioneLista','pos','entrato','entratoRiserva','ruoloTabellino','giocato'),k),righe=v))
        for arr in item['campioni'].values():
            csv_examples.extend(arr)
        all_counts.update(item['contatori'])
        stats.append({'stagione':year,'contatori':item['contatori']})
        print('CONTROLLATO',year,'Formazione',item['contatori']['RIGHE_FORMAZIONE'],'Tabellino',item['contatori'].get('TABELLO_RIGHE',0),flush=True)
    files=[]
    def savecsv(name,columns,rows):
        dest=output/name
        with dest.open('x',encoding='utf-8-sig',newline='') as f:
            writer=csv.DictWriter(f,fieldnames=columns,delimiter=';',extrasaction='ignore')
            writer.writeheader();writer.writerows(rows)
        files.append(dest)
    savecsv('RecordsNext4_Contribuzioni_v1_combinazioni.csv',
            ('stagione','selezioneLista','pos','entrato','entratoRiserva','ruoloTabellino','giocato','righe'),csv_combos)
    savecsv('RecordsNext4_Contribuzioni_v1_campioni.csv',
            ('stagione','categoria','idIncontro','idSquadra','idGiocatoreFca','codiceStorico','nomeGiocatore',
             'indiceLista','ruoloTabellino','totRaw','pos','entrato','entratoRiserva','informazione'),csv_examples)
    s={'formato':'RecordsNext4.ContribuzioniOsservate.v1',
       'avvertenza':'PRIMI11 significa soltanto appartenenza ai primi undici slot del Tabellino; non prova autonomamente la regolarita del punteggio. Nessun punto perso calcolato.',
       'stagioni':stats,'totali':dict(all_counts)}
    dest=output/'RecordsNext4_Contribuzioni_v1_sintesi.json'
    with dest.open('x',encoding='utf-8') as f:
        json.dump(s,f,indent=2,ensure_ascii=False);f.write('\n')
    files.append(dest)
    return {'formato':s['formato'],'stagioni':len(years),'formazioni':all_counts['RIGHE_FORMAZIONE'],
            'tabellini':all_counts['TABELLO_RIGHE'],
            'file':[{'nome':p.name,'byte':p.stat().st_size,'sha256':sha(p)} for p in files]}


def main() -> int:
    ap=argparse.ArgumentParser(description=__doc__)
    ap.add_argument('--database',required=True,type=Path)
    ap.add_argument('--output-dir',required=True,type=Path)
    ap.add_argument('--season',default=None)
    args=ap.parse_args()
    db=args.database.resolve(strict=True)
    out=args.output_dir.resolve()
    out.mkdir(parents=True,exist_ok=True)
    with sqlite3.connect(db.as_uri()+'?mode=ro',uri=True) as c:
        c.row_factory=sqlite3.Row
        c.execute('PRAGMA query_only=ON')
        years=seasons(c)
        if args.season:
            if args.season not in years:ap.error('Stagione non valida')
            years=[args.season]
        if not years:raise RuntimeError('Nessuna stagione')
        result=export(c,years,out)
    result['databaseSha256']=sha(db)
    dest=out/'RecordsNext4_Contribuzioni_v1_MANIFEST.json'
    with dest.open('x',encoding='utf-8') as f:json.dump(result,f,indent=2,ensure_ascii=False);f.write('\n')
    print('FORMATIONS:',result['formazioni'],'SEASONS:',len(years),flush=True)
    return 0

if __name__ == '__main__':
    try:sys.exit(main())
    except Exception as exc:
        print('ERRORE AUDIT CONTRIBUZIONI:',repr(exc),file=sys.stderr,flush=True)
        sys.exit(1)
