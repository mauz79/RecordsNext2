#!/usr/bin/env python3
"""RN4 v79: FCM COUNTED scorecards + canonically keyed global player index.

Source: RC SQLite in read-only mode. Output: versioned JS shards in an isolated staging
folder. MODIF is the observed fantasy balance, verified against TOT - VOTO.
Bonus/malus components are exposed ONLY when user-confirmed fixed coefficients,
allowed historical penalty variants and the observed MODIF identify a unique split.
"""
from __future__ import annotations
import argparse
import collections
from decimal import Decimal, InvalidOperation
import hashlib
import json
import re
import sqlite3
from pathlib import Path

LIM = 1_500_000
SEASON = re.compile(r'^\d{4}_\d{4}$')
N = re.compile(r'^[+-]?\d+(?:[.,]\d+)?$')
EVENTS = ('GOLFATTI1','ASSIST','GOLFATTISURIGORE1','RIGSBA','AMM','ESP',
          'AUTOGOL1','GOLSUBITI','GOLSUBITISURIGORE','RIGPAR')


def die(msg): raise RuntimeError(msg)
def check(v, msg):
    if not v: die(msg)
def numeric(raw):
    if raw is None or not N.fullmatch(str(raw).strip()): return None
    return Decimal(str(raw).strip().replace(',','.'))
def num(value): return float(value) if value is not None else None
def quote(n):
    check(bool(re.fullmatch('[a-zA-Z0-9_]+',n)), 'SQL identifier invalid: '+n)
    return '"'+n+'"'
def cols(c,table):
    return {r[1].upper():r[1] for r in c.execute('PRAGMA table_info('+quote(table)+')')}
def jsonp(var, payload):
    return ('window.'+var+'=window.'+var+'||{};\n' if var.endswith('Data') else '') + 'window.'+var+' = '+json.dumps(payload,ensure_ascii=False,separators=(',',':'))+';\n'
def write_js(path, contents):
    raw=contents.encode('utf-8');check(0<len(raw)<=LIM,f'JS oversize {path}: {len(raw)} bytes')
    check(not path.exists(), 'Output exists, choose empty staging: '+str(path))
    path.write_bytes(raw)
    return {'nome':path.name,'byte':len(raw),'sha256':hashlib.sha256(raw).hexdigest()}


def shards(out,season,rows,kind):
    """Bounded append-only JS shards; one file never exceeds 1,500,000 bytes."""
    check(kind in ('Points','Index'),'Invalid shard kind')
    var='RecordsNextV79Points' if kind=='Points' else 'RecordsNextV79Index'
    prefix=('window.'+var+'=window.'+var+'||{};\n')
    initialization=(('window.'+var+'['+json.dumps(season)+']=window.'+var+'['+json.dumps(season)+']||'+
       ('{"schemaVersion":"4.0-scorecard-points-v79","stagione":'+json.dumps(season)+',"righe":[]}'
         if kind=='Points' else '[]')+';\n'))
    expression='window.'+var+'['+json.dumps(season)+']'+('.righe' if kind=='Points' else '')
    def content(items):
        return prefix+initialization+expression+'.push(...'+json.dumps(items,ensure_ascii=False,separators=(',',':'))+');\n'
    bundles=[];current=[];current_bytes=0
    for row in rows:
        encoded=len(json.dumps(row,ensure_ascii=False,separators=(',',':')).encode('utf-8'))
        if current and current_bytes+encoded+200>850_000:
            bundles.append(current);current=[];current_bytes=0
        check(encoded+200<=LIM, 'Oversize unshardable single row')
        current.append(row);current_bytes+=encoded+1
    if current or not bundles:bundles.append(current)
    meta=[]
    for n,chunk in enumerate(bundles,1):
        name='fcmRecordsNext_'+('ProfilePoints' if kind=='Points' else 'PlayerIndex')+'_v79_'+season+'_'+str(n).zfill(3)+'.js'
        info=write_js(out/name,content(chunk))
        meta.append({'stagione':season,'part':n,'righe':len(chunk),**info})
    check(sum(x['righe'] for x in meta)==len(rows),'Shard row coverage mismatch')
    return meta

# Rules explicitly confirmed by the league administrator. Variants for penalty
# goals and missed penalties must be resolved by the original FCM MODIF value.
FIXED = {'ASSIST':Decimal('1'), 'RIGPAR':Decimal('3'), 'AMM':Decimal('0.5'),
         'ESP':Decimal('1'), 'AUTOGOL1':Decimal('2'), 'GOLSUBITI':Decimal('1')}


def reconstruct(event, observed, absent=()):
    """Return (bonus, malus, status, candidates) in real fantasy POINTS.

    No mixing of FCA event *counts* with FCM score without a per-appearance
    reconciliation. Under-determined splits remain null rather than invented.
    """
    if absent:
        return None,None,'MISSING_FIELDS',[]
    if event['GOLFATTISURIGORE1'] > event['GOLFATTI1']:
        return None,None,'INVALID_PENALTY_SUBSET',[]
    if event['GOLSUBITISURIGORE'] > event['GOLSUBITI']:
        return None,None,'INVALID_CONCEDED_SUBSET',[]
    normal_goals=event['GOLFATTI1']-event['GOLFATTISURIGORE1']
    basic_bonus=normal_goals*3+event['ASSIST']*FIXED['ASSIST']+event['RIGPAR']*FIXED['RIGPAR']
    basic_malus=(event['AMM']*FIXED['AMM']+event['ESP']*FIXED['ESP']+
        event['AUTOGOL1']*FIXED['AUTOGOL1']+event['GOLSUBITI']*FIXED['GOLSUBITI'])
    goals_coeff=(Decimal(2),Decimal(3)) if event['GOLFATTISURIGORE1'] else (Decimal(3),)
    missed_coeff=(Decimal(2),Decimal(3)) if event['RIGSBA'] else (Decimal(3),)
    candidates=set()
    for pg in goals_coeff:
        for ps in missed_coeff:
            bonus=basic_bonus+event['GOLFATTISURIGORE1']*pg
            malus=basic_malus+event['RIGSBA']*ps
            if abs(bonus-malus-observed)<=Decimal('0.011'):
                candidates.add((bonus,malus))
    if len(candidates)==1:
        bonus,malus=next(iter(candidates))
        return bonus,malus,'RECONCILED',[(num(bonus),num(malus))]
    if len(candidates)>1:
        return None,None,'AMBIGUOUS_SPLIT',[(num(a),num(b)) for a,b in sorted(candidates)]
    return None,None,'UNEXPLAINED_MODIF',[]

def parse_record(tokens,pid):
    items=str(tokens or '').split('%')
    indexes=[i for i,x in enumerate(items) if x.strip()==str(pid)]
    check(len(indexes)<=1,'FCM duplicate player in scorecard: '+str(pid))
    return indexes[0] if indexes else None

def load_one_season(c, season):
    fcm='raw_'+season+'_fcm_';fca='raw_'+season+'_fca_'
    t=[fcm+n for n in ('formazione','incontro','girone','competizione','tabellino')]
    p=[fca+n for n in ('giocain','punteggio')]
    for table in t+p:check(bool(cols(c,table)), 'Raw table missing: '+table)
    source_cols=cols(c,p[1])
    absent=[k for k in EVENTS if k not in source_cols]
    # Some older FCA imports lack newer statistic columns. Missing means unknown, NOT zero.
    fields=','.join(('p.'+quote(source_cols[k]) if k in source_cols else 'NULL')+' AS '+quote('ev_'+k) for k in EVENTS)
    sql=f'''SELECT f.IDGIOC AS pid, f.IDSQUADRA AS team, f.IDINCONTRO AS mid,
       i.GIORNATADIA AS day, i.GIOCATO AS played,
       cp.ID AS cid, cp.NOME AS cname,
       t.LISTA AS slots, t.VOTO AS votes, t.MODIF AS modifiers, t.TOT AS totals,
       {fields}
    FROM {quote(fcm+'formazione')} f
    JOIN {quote(fcm+'incontro')} i ON i.ID=f.IDINCONTRO
    JOIN {quote(fcm+'girone')} g ON g.ID=i.IDGIRONE
    JOIN {quote(fcm+'competizione')} cp ON cp.ID=g.IDCOMPETIZIONE
    JOIN {quote(fcm+'tabellino')} t ON t.IDINCONTRO=f.IDINCONTRO AND t.IDSQUADRA=f.IDSQUADRA
    JOIN {quote(fca+'giocain')} d ON d.IDGIOCATORE=f.IDGIOC AND d.GIORNATA=i.GIORNATADIA
    JOIN {quote(fca+'punteggio')} p ON p.ID=d.IDPUNTEGGIO
    ORDER BY f.IDINCONTRO,f.IDSQUADRA,f.IDGIOC'''
    c.row_factory=sqlite3.Row
    by={};seen=set();audit=collections.Counter();examples=[]
    for row in c.execute(sql):
        audit['formation_rows']+=1
        if str(row['cname']).strip().casefold()=='youth league':continue
        if int(row['played'] or 0)!=1:continue
        pid=int(row['pid']);slot=parse_record(row['slots'],pid)
        check(slot is not None, f'FCM formation missing in tabellino: {season}/{row["mid"]}/{pid}')
        arrays=[str(row[k] or '').split('%') for k in ('slots','votes','modifiers','totals')]
        check(len({len(x) for x in arrays})==1,'FCM scorecard array size mismatch '+season)
        if slot>=11:continue
        tv=numeric(arrays[1][slot]);total=numeric(arrays[3][slot]);modifier=numeric(arrays[2][slot])
        if total is None:continue
        match=(pid,int(row['team']),int(row['mid']))
        check(match not in seen,'Duplicate FCM scorecard usage '+str(match));seen.add(match)
        audit['counted']+=1
        key=(str(season)+':'+str(pid),int(row['team']),int(row['cid']))
        item=by.setdefault(key,{'playerKey':key[0],'idSquadraInterno':key[1],
              'idCompetizioneInterno':key[2], 'competizione':row['cname'],
              'gare':0,'totSum':Decimal(0),'votoSum':Decimal(0),'pairs':0,
              'deltaSum':Decimal(0),'modifValid':0,'verified':0,'bonusSum':Decimal(0),'malusSum':Decimal(0),
              'ruleSource':set(),'mismatches':0,'events':collections.Counter(), 'modifCount':0, 'missing':set(absent),'unresolved':collections.Counter()})
        check(item['competizione']==row['cname'],'Competition name conflict: '+str(key))
        item['gare']+=1;item['totSum']+=total
        e={}
        for k in EVENTS:
            raw=row['ev_'+k]
            v=numeric(raw)
            if v is None:
                check(raw is None or str(raw).strip() in ('','0'),'Event not numeric: '+k+' '+season)
                v=Decimal(0)
            check(v>=0 and v==v.to_integral_value(), 'Invalid FCA event: '+k+' '+season)
            e[k]=v;item['events'][k]+=int(v)
        if tv is not None:
            audit['pairs']+=1;item['pairs']+=1;item['votoSum']+=tv
            delta=total-tv;item['deltaSum']+=delta
            if modifier is not None and modifier!=0:item['modifCount']+=1
            # v78 real archive: 240,096/240,096 FCM rows had MODIF=TOT-VOTO.
            # This is an independent invariant; MODIF must not be applied twice.
            if modifier is None or abs(modifier-delta)>Decimal('0.011'):
                audit['modif_inconsistent']+=1;item['mismatches']+=1
                item['unresolved']['MODIF_INCONSISTENT']+=1
                if len(examples)<100:examples.append({'stagione':season,'idIncontro':row['mid'],
                    'idCalciatore':pid,'idFantasquadra':key[1],'competizione':row['cname'],
                    'reason':'MODIF_INCONSISTENT','modif':str(modifier),'delta':str(delta)})
            else:
                audit['modif_verified']+=1;item['modifValid']+=1
                bonus,malus,status,possible=reconstruct(e,modifier,absent)
                if status=='RECONCILED':
                    item['verified']+=1;audit['reconciled_scorecards']+=1
                    item['bonusSum']+=bonus;item['malusSum']+=malus
                    item['ruleSource'].add('FCM MODIF; coefficienti confermati e varianti rigore ricostruite')
                else:
                    item['mismatches']+=1;item['unresolved'][status]+=1;audit[status]+=1
                    if len(examples)<100:examples.append({'stagione':season,'idIncontro':row['mid'],
                        'idCalciatore':pid,'idFantasquadra':key[1],'giornata':row['day'],
                        'competizione':row['cname'],'reason':status,'voto':str(tv),'tot':str(total),
                        'modif':str(modifier),'events':{k:int(v) for k,v in e.items()},
                        'candidateSplits':possible})
        else:
            audit['tot_without_numeric_vote']+=1
    result=[]
    for key,x in sorted(by.items()):
        complete=x['gare']>0 and x['verified']==x['gare'] and x['pairs']==x['gare']
        result.append({'playerKey':x['playerKey'],'idSquadraInterno':x['idSquadraInterno'],
          'idCompetizioneInterno':x['idCompetizioneInterno'],'competizione':x['competizione'],
          'gare':x['gare'],'totSum':num(x['totSum']),
          'votoFcmSum':num(x['votoSum']) if x['pairs']==x['gare'] else None,
          'pairs':x['pairs'],'deltaNetTabellino':num(x['deltaSum']) if x['pairs']==x['gare'] else None,
          'bonusPunti':num(x['bonusSum']) if complete else None,
          'malusPunti':num(x['malusSum']) if complete else None,
          'saldoPunti':num(x['deltaSum']) if x['modifValid']==x['gare'] else None,
          'certified':complete, 'ruleSource':sorted(x['ruleSource']) if complete else [],
          'ruleMismatches':x['mismatches'],'modifNonzero':x['modifCount'],
          'unresolvedReasons':dict(x['unresolved']),'pointsMethod':'FCM_MODIF_RECONSTRUCTED_v79',
          'events':{k:(int(x['events'][k]) if k not in x['missing'] else None) for k in EVENTS},
          'missingSourceFields':sorted(x['missing'])})
    audit['contexts']=len(result)
    audit['certified_contexts']=sum(x['certified'] for x in result)
    audit['observed_saldo_contexts']=sum(x['saldoPunti'] is not None for x in result)
    return result, {'season':season,'counts':dict(audit),'missingFcaFields':absent,'exceptions':examples,
      'certification':'saldo = MODIF FCM osservato; bonus/malus solo se lo split e univoco e TUTTI gli incontri del contesto riconciliati'}

def build_index(c,seasons,approved):
    data={};counts={}
    for season in seasons:
        table='raw_'+season+'_fca_giocatorea';fields=cols(c,table)
        check({'ID','NOME'}.issubset(fields), 'Missing player ID or NAME '+season)
        code='CODICEGAZZA' if 'CODICEGAZZA' in fields else None
        birth='DATADINASCITA' if 'DATADINASCITA' in fields else None
        role='RUOLO' if 'RUOLO' in fields else None
        stmt='SELECT '+','.join(quote(fields[x]) if x else 'NULL' for x in ['ID','NOME',code,birth,role])+' FROM '+quote(table)
        rows=[];keys=set()
        for pid,name,gazza,date,ruolo in c.execute(stmt):
            key=f'{season}:{int(pid)}';check(key not in keys,'Duplicate FCA identity '+key);keys.add(key)
            canon=approved.get(key) or key
            rows.append([key,canon,str(name or '').strip(),str(date or '')[:10],
                         str(gazza) if gazza is not None else '',str(ruolo) if ruolo is not None else ''])
        counts[season]=len(rows);data[season]=rows
    return data,counts

def export(db, registry, site, output):
    check(output.is_dir() and not any(output.iterdir()), 'Use empty staging folder: '+str(output))
    c=sqlite3.connect('file:'+db.resolve().as_posix()+'?mode=ro',uri=True)
    c.execute('PRAGMA query_only=ON')
    facade=site/'js'/'fcmRecordsNext_TopFlopViews.js'
    raw=facade.read_text(encoding='utf-8-sig').strip()
    prefix='window.fcmRecordsNextTopFlopViews = '
    check(raw.startswith(prefix) and raw.endswith(';'),'Public facade format mismatch')
    manifest=json.loads(raw[len(prefix):-1]);check(manifest.get('schemaVersion')=='4.0-public-views-v1','Unknown public facade')
    seasons=[x['stagione'] for x in manifest['stagioni']]
    check(0<len(seasons)<=30 and len(seasons)==len(set(seasons)),'Invalid published seasons list')
    for season in seasons:check(SEASON.fullmatch(season),'Invalid published season '+season)
    cfg=json.loads(registry.read_text(encoding='utf-8-sig'))
    check(cfg.get('schemaVersion')=='rn4-player-identities-v1' and isinstance(cfg.get('approved'),dict),
          'Player registry schema mismatch')
    report={'schemaVersion':'rn4-v79-audit','database':str(db),'site':str(site),
            'seasons':seasons,'seasonAudit':[],'scoringMethod':'v79-modif-observed-unique-split'}
    points_meta=[];search_meta=[]
    data_index,counts=build_index(c,seasons,cfg['approved'])
    for season in seasons:
        rows,audit=load_one_season(c,season)
        report['seasonAudit'].append(audit)
        points_meta.extend(shards(output,season,rows,'Points'))
        search_meta.extend(shards(output,season,data_index[season],'Index'))
    write_js(output/'fcmRecordsNext_ProfileManifest_v79.js','window.RecordsNextV79Manifest='+json.dumps({
      'schemaVersion':'4.0-profile-v79','seasons':seasons,'points':points_meta,'index':search_meta,
      'rulesStatus':'MODIF is observed from FCM; bonus/malus reconciled only when unique'
    },ensure_ascii=False,separators=(',',':'))+';\n')
    c.close()
    report['totals']={'seasons':len(seasons),'counted':sum(a['counts'].get('counted',0) for a in report['seasonAudit']),
      'certifiedContexts':sum(a['counts'].get('certified_contexts',0) for a in report['seasonAudit']),
      'reconciledContexts':sum(a['counts'].get('certified_contexts',0) for a in report['seasonAudit']),
      'observedSaldoContexts':sum(a['counts'].get('observed_saldo_contexts',0) for a in report['seasonAudit']),
      'unexplainedScorecards':sum(a['counts'].get('UNEXPLAINED_MODIF',0) for a in report['seasonAudit']),
      'ambiguousScorecards':sum(a['counts'].get('AMBIGUOUS_SPLIT',0) for a in report['seasonAudit']),
      'contexts':sum(a['counts'].get('contexts',0) for a in report['seasonAudit']),
      'searchRows':sum(counts.values())}
    (output/'AUDIT_SCHEDA_v79.json').write_text(json.dumps(report,indent=2,ensure_ascii=False)+'\n',encoding='utf-8')
    return report

def main():
    ap=argparse.ArgumentParser()
    ap.add_argument('--db',type=Path,required=True);ap.add_argument('--registry',type=Path,required=True)
    ap.add_argument('--site',type=Path,required=True);ap.add_argument('--out',type=Path,required=True)
    a=ap.parse_args();a.out.mkdir(parents=True,exist_ok=True)
    rep=export(a.db,a.registry,a.site,a.out)
    print('RN4_V79_EXPORT_OK',json.dumps(rep['totals'],ensure_ascii=False))

if __name__=='__main__':
    try:main()
    except Exception as exc:
        import sys,traceback
        print('STOP V79 EXPORT: '+str(exc),file=sys.stderr)
        sys.exit(2)
