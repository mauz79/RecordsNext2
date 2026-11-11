#!/usr/bin/env python3
"""Read-only, season+competition-specific FCM regulation census (no score simulation)."""
from __future__ import annotations
import argparse
import csv
from datetime import datetime, timezone
import hashlib
import json
from pathlib import Path
import re
import sqlite3
import sys
from collections import Counter, defaultdict

FAMILY = 'RecordsNext4.RegoleOsservate.v1'
REQUIRED = ('competizione', 'girone', 'incontro', 'tabellino', 'modulo', 'moduloammesso')
SELECTED = (
    'NUMEROSOSTITUZIONI', 'NUMERORISUFFICIO', 'VOTODUFFICIO', 'VOTODUFFICIOPORT',
    'USATABELLINO', 'USAQUOT1', 'USAQUOT2', 'USAQUOT3',
    'GIOCINPANCHINAP', 'GIOCINPANCHINAD', 'GIOCINPANCHINAC',
    'GIOCINPANCHINAA', 'GIOCINPANCHINATOTALE',
    'DSOSTITUISCEC', 'DSOSTITUISCEA', 'CSOSTITUISCED',
    'CSOSTITUISCEA', 'ASOSTITUISCED', 'ASOSTITUISCEC',
    'REGOLAPORTIERE', 'REGOLADIFESA', 'REGOLAMIN59', 'REGOLAMIN60',
    'USASPECIALE1', 'USASPECIALE2', 'USASPECIALE3',
    'NOMESPECIALE1', 'NOMESPECIALE2', 'NOMESPECIALE3',
)


def sha_bytes(b:bytes)->str:
    return hashlib.sha256(b).hexdigest()


def sha_path(p:Path)->str:
    h=hashlib.sha256()
    with p.open('rb') as f:
        for chunk in iter(lambda:f.read(1024*1024),b''):
            h.update(chunk)
    return h.hexdigest()


def table(conn:sqlite3.Connection, names:set[str], season:str, family:str)->str:
    name = f'raw_{season}_fcm_{family}'
    if name not in names:
        raise ValueError(f'Tabella FCM mancante: {name}')
    return '"' + name + '"'


def discover(conn:sqlite3.Connection)->tuple[list[str],set[str]]:
    names={r[0].lower() for r in conn.execute("SELECT name FROM sqlite_master WHERE type='table'")}
    pattern=re.compile(r'^raw_(\d{4}_\d{4})_fcm_competizione$')
    seasons=sorted(m.group(1) for n in names if (m:=pattern.fullmatch(n)))
    if not seasons: raise ValueError('Nessuna stagione con tabella FCM Competizione')
    for season in seasons:
        for family in REQUIRED: table(conn,names,season,family)
    return seasons,names


def rows(conn:sqlite3.Connection, sql:str):
    for row in conn.execute(sql):
        yield dict(row)


def csv_write(dest:Path,fieldnames:list[str],records:list[dict]):
    with dest.open('w',encoding='utf-8-sig',newline='') as f:
        writer=csv.DictWriter(f,fieldnames=fieldnames,extrasaction='ignore',lineterminator='\n')
        writer.writeheader()
        writer.writerows(records)


def analyze(conn:sqlite3.Connection,max_samples:int=40)->dict:
    seasons,names=discover(conn)
    comp_export=[]
    full_config=[]
    season_export=[]
    incidents=[]
    global_stats=Counter()
    settings_count=Counter()
    samples=Counter()
    for season in seasons:
        ctab=table(conn,names,season,'competizione')
        gtab=table(conn,names,season,'girone')
        itab=table(conn,names,season,'incontro')
        ttab=table(conn,names,season,'tabellino')
        mtab=table(conn,names,season,'modulo')
        matab=table(conn,names,season,'moduloammesso')
        comps={int(d['ID']):d for d in rows(conn,f'SELECT * FROM {ctab} ORDER BY ID')}
        gironi={int(d['ID']):d for d in rows(conn,f'SELECT * FROM {gtab} ORDER BY ID')}
        moduli={int(d['ID']):d for d in rows(conn,f'SELECT * FROM {mtab} ORDER BY ID')}
        per_comp=defaultdict(Counter)
        all_comp_moduli=defaultdict(list)
        for d in rows(conn,f'SELECT * FROM {matab} ORDER BY IDCOMPETIZIONE,IDMODULO'):
            cid=d.get('IDCOMPETIZIONE');mid=d.get('IDMODULO')
            rec={**d,'modulo':moduli.get(mid)}
            all_comp_moduli[cid].append(rec)
            if cid not in comps: raise ValueError(f'{season}: modulo ammesso punta a competizione assente: {cid}')
        for id_girone,data in gironi.items():
            if data['IDCOMPETIZIONE'] not in comps:
                raise ValueError(f'{season}: girone {id_girone} ha competizione assente')
        incontri={}
        for match in rows(conn,f'SELECT ID, IDGIRONE, IDTIPO, IDCASA, IDFUORI, GIOCATO FROM {itab}'):
            mid=int(match['ID'])
            if mid in incontri: raise ValueError(f'{season}: ID incontro duplicato: {mid}')
            gid=match['IDGIRONE']
            if gid not in gironi:raise ValueError(f'{season}: incontro {mid} con girone inesistente {gid}')
            cid=gironi[gid]['IDCOMPETIZIONE']
            incontri[mid]=(cid,match)
            per_comp[cid]['incontri']+=1
            global_stats['incontri']+=1
            if not match['IDCASA'] or not match['IDFUORI']:
                per_comp[cid]['incontri_squadra_zero']+=1
                global_stats['incontri_squadra_zero']+=1
        for tab in rows(conn,f'SELECT IDINCONTRO,IDSQUADRA,TOTALESQUADRAESISTE,PARZIALESQUADRAESISTE,LISTA,RUOLO,TOT FROM {ttab}'):
            mid=tab['IDINCONTRO'];sid=tab['IDSQUADRA']
            global_stats['tabellini']+=1
            if mid not in incontri:
                global_stats['tabellini_incontro_assente']+=1
                if samples['incontro_assente']<max_samples:
                    incidents.append(dict(stagione=season,categoria='TABELLO_ID_INCONTRO_ASSENTE',idIncontro=mid,idSquadra=sid,idCompetizione='',nomeCompetizione='',dettaglio='IDIncontro non trovato; riposo non dimostrato'))
                    samples['incontro_assente']+=1
                continue
            cid,match=incontri[mid]
            per_comp[cid]['tabellini']+=1
            per_comp[cid]['tabellini_totale_esiste']+=int(bool(tab['TOTALESQUADRAESISTE']))
            per_comp[cid]['tabellini_parziale_esiste']+=int(bool(tab['PARZIALESQUADRAESISTE']))
            if not match['IDCASA'] or not match['IDFUORI']:
                per_comp[cid]['tabellini_incontro_squadra_zero']+=1
                global_stats['tabellini_incontro_squadra_zero']+=1
                if samples['riposo_potenziale']<max_samples:
                    incidents.append(dict(stagione=season,categoria='TABELLO_INCONTRO_SQUADRA_ZERO',idIncontro=mid,idSquadra=sid,idCompetizione=cid,nomeCompetizione=comps[cid]['NOME'],dettaglio='Incontro con almeno una squadra 0: potenziale riposo, verificare'))
                    samples['riposo_potenziale']+=1
        sc=Counter()
        for cid,d in sorted(comps.items()):
            num=d.get('NUMEROSOSTITUZIONI')
            ru=d.get('NUMERORISUFFICIO')
            settings_count[(num,ru)]+=1
            row={'stagione':season,'idCompetizione':cid,'competizione':d['NOME'],
                 'incontri':per_comp[cid]['incontri'],
                 'incontriSquadraZero':per_comp[cid]['incontri_squadra_zero'],
                 'tabellini':per_comp[cid]['tabellini'],
                 'tabelliniTotaleEsiste':per_comp[cid]['tabellini_totale_esiste'],
                 'tabelliniParzialeEsiste':per_comp[cid]['tabellini_parziale_esiste'],
                 'tabelliniIncontroSquadraZero':per_comp[cid]['tabellini_incontro_squadra_zero'],
                 'moduliAmmessi':len(all_comp_moduli[cid])}
            for col in SELECTED:
                if col not in d:raise ValueError(f'{season}: colonna Competizione mancante: {col}')
                row[col]=d[col]
            comp_export.append(row)
            full_config.append({'stagione':season,'idCompetizione':cid,'nome':d['NOME'],
                                'configurazioneFcmOriginale':d,'moduliAmmessi':all_comp_moduli[cid],
                                'conteggiCollegati':dict(per_comp[cid])})
            sc.update(per_comp[cid])
        season_export.append({'stagione':season,'competizioni':len(comps),'gironi':len(gironi),
                              'incontri':len(incontri),'tabellini':sum(per_comp[cid]['tabellini'] for cid in comps),
                              'tabelliniSenzaIncontro':global_stats['tabellini_incontro_assente']-sum(x['tabelliniSenzaIncontro'] for x in season_export),
                              'incontriSquadraZero':sc['incontri_squadra_zero'],
                              'tabelliniSuIncontroSquadraZero':sc['tabellini_incontro_squadra_zero']})
    global_stats['competizioni']=len(comp_export)
    global_stats['stagioni']=len(seasons)
    global_stats['tabellini_con_incontro']=sum(r['tabellini'] for r in comp_export)
    if global_stats['tabellini_con_incontro']+global_stats['tabellini_incontro_assente']!=global_stats['tabellini']:
        raise AssertionError('Totali Tabellino incoerenti')
    if sum(r['incontri'] for r in comp_export)!=global_stats['incontri']:
        raise AssertionError('Incontri non assegnati a competizioni')
    return {'formato':FAMILY,'avvertenza':'Configurazioni FCM finali osservate, non prova di regole applicate retroattivamente a ogni singola giornata. Nessun punto recuperabile calcolato.',
            'stagioni':season_export,'competizioni':comp_export,'configurazioniComplete':full_config,
            'campioniEccezioni':incidents,'totali':dict(global_stats),
            'distribuzioneSostituzioniRu':[{'numeroSostituzioni':k[0],'numeroRiserveUfficio':k[1],'competizioni':v}
                                        for k,v in sorted(settings_count.items(), key=lambda kv: (str(kv[0]),kv[1]))]}


def run(database:Path,output_dir:Path,max_samples:int):
    if not database.is_file():raise FileNotFoundError(database)
    if not output_dir.is_dir():raise FileNotFoundError(output_dir)
    uri=database.resolve().as_uri()+'?mode=ro'
    conn=sqlite3.connect(uri,uri=True)
    conn.row_factory=sqlite3.Row
    try:
        conn.execute('PRAGMA query_only=ON')
        result=analyze(conn,max_samples)
    finally:conn.close()
    prefix='RecordsNext4_Regole_v1'
    columns=['stagione','idCompetizione','competizione','incontri','incontriSquadraZero',
             'tabellini','tabelliniTotaleEsiste','tabelliniParzialeEsiste',
             'tabelliniIncontroSquadraZero','moduliAmmessi',*SELECTED]
    paths={
        'competizioni':output_dir/(prefix+'_competizioni.csv'),
        'stagioni':output_dir/(prefix+'_stagioni.csv'),
        'eccezioni':output_dir/(prefix+'_eccezioni_campione.csv'),
        'configurazioni':output_dir/(prefix+'_configurazioni_complete.json'),
        'sintesi':output_dir/(prefix+'_sintesi.json'),
    }
    csv_write(paths['competizioni'],columns,result['competizioni'])
    csv_write(paths['stagioni'],['stagione','competizioni','gironi','incontri','tabellini',
                               'tabelliniSenzaIncontro','incontriSquadraZero','tabelliniSuIncontroSquadraZero'],result['stagioni'])
    csv_write(paths['eccezioni'],['stagione','categoria','idIncontro','idSquadra','idCompetizione','nomeCompetizione','dettaglio'],result['campioniEccezioni'])
    paths['configurazioni'].write_text(json.dumps(result['configurazioniComplete'],ensure_ascii=False,indent=2)+'\n',encoding='utf8')
    short={k:v for k,v in result.items() if k not in ('competizioni','configurazioniComplete','campioniEccezioni')}
    paths['sintesi'].write_text(json.dumps(short,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
    manifest={'formato':FAMILY,'sha256Database':sha_path(database),'file':[
        {'nome':p.name,'byte':p.stat().st_size,'sha256':sha_path(p)} for p in paths.values()],
        'totali':result['totali']}
    (output_dir/(prefix+'_MANIFEST.json')).write_text(json.dumps(manifest,indent=2,ensure_ascii=False)+'\n',encoding='utf8')
    print('ESITO AUDIT: OK')
    print('STAGIONI:',result['totali']['stagioni'],'COMPETIZIONI:',result['totali']['competizioni'])
    print('INCONTRI:',result['totali']['incontri'],'TABELLINI:',result['totali']['tabellini'])
    print('TABELLINI ID INCONTRO ASSENTE:',result['totali']['tabellini_incontro_assente'])
    print('NESSUNA SIMULAZIONE O MODIFICA DB')


def main():
    parser=argparse.ArgumentParser()
    parser.add_argument('--database',type=Path,required=True)
    parser.add_argument('--output-dir',type=Path,required=True)
    parser.add_argument('--max-samples',type=int,default=40)
    args=parser.parse_args()
    if args.max_samples < 0 or args.max_samples > 10000: parser.error('max-samples fuori intervallo 0..10000')
    run(args.database,args.output_dir,args.max_samples)


if __name__=='__main__':
    try:main()
    except Exception as exc:
        print('ERRORE AUDIT REGOLE:',str(exc),file=sys.stderr)
        sys.exit(1)
