#!/usr/bin/env python3
"""Integrated v83 verification: existing FCA v81 oracles versus native site JS,
plus INDEPENDENT raw FCM 21-season oracle (original v79 reader) versus Java points.
Reads SQLite in mode=ro; writes only the user-requested downloads report files.
No publish, no commits. Distinguishes partial coverage from verified results.
"""
import argparse, collections, csv, hashlib, importlib.util, json, re, sqlite3, sys, time
from pathlib import Path

EVENTS=('GOLFATTI1','GOLFATTISURIGORE1','ASSIST','RIGSBA','AMM','ESP','AUTOGOL1','GOLSUBITI','GOLSUBITISURIGORE','RIGPAR')
PREFIXES={'facade':'window.fcmRecordsNextTopFlopViews = ',
          'full':'window.fcmRecordsNextTopFlopFullV69 = ',
          'profile':'window.RecordsNextV79Manifest = '}

def assigned(file,prefix):
    s=file.read_text(encoding='utf-8-sig').strip()
    if not s.startswith(prefix) or not s.endswith(';'):
        raise RuntimeError('Invalid JS manifest: '+str(file))
    return json.loads(s[len(prefix):-1])

def rows(file):
    raw=file.read_text(encoding='utf-8-sig')
    # Native view: a.push(...[ ... ]); or native points: .push(...[ ... ]);
    p=raw.find('.push(...[')
    if p<0:raise RuntimeError('Not a recognized Java shard '+str(file))
    p+=len('.push(...[')
    end=raw.rfind(']);')
    if end<p:raise RuntimeError('Unterminated shard '+str(file))
    return json.loads('['+raw[p:end]+']')

def near(a,b):
    if a is None or b is None:return a is None and b is None
    try:return abs(float(a)-float(b))<0.011
    except (ValueError,TypeError):return a==b

def check_site(db,site,oracle,output):
    js=site/'js';errors=[];warnings=[];counts=collections.Counter();examples=[]
    def err(text):
        errors.append(text) if len(errors)<100 else None
        counts['errors_total']+=1
    def warn(text):
        warnings.append(text) if len(warnings)<100 else None
        counts['warnings_total']+=1
    facade=assigned(js/'fcmRecordsNext_TopFlopViews.js',PREFIXES['facade'])
    full=assigned(js/'fcmRecordsNext_TopFlopFull_v83.js',PREFIXES['full'])
    manifest=assigned(js/'fcmRecordsNext_ProfileManifest_v83.js',PREFIXES['profile'])
    seasons=[s['stagione'] for s in facade['stagioni']]
    if len(seasons)!=len(set(seasons)) or not seasons:err('Cutoff invalido o duplicato')
    if set(seasons)!=set(s['stagione'] for s in full['stagioni']) or set(seasons)!=set(manifest['seasons']):
        err('Manifest Full/Profile incoerenti con cutoff facade')
    meta={s['stagione']:s for s in facade['stagioni']}
    fullmeta={s['stagione']:s for s in full['stagioni']}
    event_names=facade['eventi'];by_season={};reals={};teams={}
    for season in seasons:
        career=[];t=[];real=[]
        for rec in meta[season]['files']:
            typ=rec['tipo'];name=rec['nome']
            if typ not in ('Careers','Teams'):continue
            block=rows(js/name)
            if len(block)!=rec['righe']:err('Shard incompleto '+name)
            (career if typ=='Careers' else t).extend(block)
        for rec in fullmeta[season]['files']:
            if rec['tipo']!='RealPerformances':continue
            block=rows(js/rec['nome']);real.extend(block)
            if len(block)!=rec['righe']:err('Full shard incompleto '+rec['nome'])
        by_season[season]={r[0]:r for r in career};teams[season]=t;reals[season]=real
        if len(by_season[season])!=len(career):err('Doppia identita FCA '+season)
        counts['player_seasons']+=len(career)
        counts['real_performances']+=len(real)
    # Independently recorded v81 FCA player-season counts & events.
    with (oracle/'ORACOLO_EVENTI_FCA_v81.csv').open(encoding='utf-8-sig',newline='') as fh:
        for c in csv.DictReader(fh,delimiter=';'):
            season=c['Stagione'];key=c['PlayerKey'];counts['fca_oracle_rows']+=1
            if season not in by_season:continue
            item=by_season[season].get(key)
            if item is None:err('FCA identita mancante '+key);continue
            valid=item[4][0]
            if int(c['VotiValidi'])!=valid:err('FCA voti diversi '+key)
            vals=dict((event_names[i],int(v)) for i,v in zip(item[4][2][::2],item[4][2][1::2]))
            for field in EVENTS:
                if int(c[field])!=vals.get(field,0):err('FCA eventi diversi '+key+' '+field)
            counts['fca_compared']+=1
    for season in seasons:
        if season not in reals:continue
        perf=reals[season]
        keys=[(int(r[0]),int(r[1])) for r in perf]
        if len(keys)!=len(set(keys)):err('FCA performance duplicate '+season)
        for row in perf:
            if row[2]!='' and float(row[2])<=0:err('Voto FCA non positivo in Full '+season)
    with (oracle/'ORACOLO_VOTI_TF01_TF02_v81.csv').open(encoding='utf-8-sig',newline='') as fh:
        for c in csv.DictReader(fh,delimiter=';'):
            season=c['Stagione']
            if season not in reals:continue
            target=(int(c['IDPlayer']),int(c['Giornata']))
            found=[r for r in reals[season] if (int(r[0]),int(r[1]))==target]
            if len(found)!=1 or not near(found[0][2],c['Voto']):
                err('TF01/02 ancora vote non corrispondente '+season+' '+str(target))
            counts['vote_anchors']+=1
    with (oracle/'PRIMATI_PORTIERI_TF20_v81.csv').open(encoding='utf-8-sig',newline='') as fh:
        for c in csv.DictReader(fh,delimiter=';'):
            season=c['Stagione'];key=c['PlayerKey']
            if season not in by_season:continue
            r=by_season[season].get(key)
            if r is None:err('TF20 portiere assente '+key);continue
            if int(r[2])!=1:err('TF20 ruolo non portiere '+key)
            e={event_names[i]:v for i,v in zip(r[4][2][::2],r[4][2][1::2])}
            for col,field in [('Gol','GOLFATTI1'),('GolSuRigore','GOLFATTISURIGORE1'),('Assist','ASSIST'),
                ('RigoriParati','RIGPAR'),('GolSubiti','GOLSUBITI')]:
                if int(c[col])!=e.get(field,0):err('TF20 '+key+' '+col)
            if int(c['Voti'])!=r[4][0]:err('TF20 voti diversi '+key)
            counts['tf20_rows']+=1
    # Independent FCM raw reader from the v79 implementation; this has never
    # been used to generate the v83 Java output.
    spec=importlib.util.spec_from_file_location('rn4_v79_oracle',Path(__file__).with_name('Oracolo-FCM-v79-readonly.py'))
    module=importlib.util.module_from_spec(spec);spec.loader.exec_module(module)
    conn=sqlite3.connect('file:'+db.resolve().as_posix()+'?mode=ro',uri=True)
    conn.execute('PRAGMA query_only=ON')
    for season in seasons:
        expected,audit=module.load_one_season(conn,season)
        native=[]
        for entry in manifest['points']:
            if entry['stagione']!=season:continue
            block=rows(js/entry['nome']);native.extend(block)
            if len(block)!=entry['righe']:err('Punti shard incompleto '+entry['nome'])
        keys=lambda x:(x['playerKey'],int(x['idSquadraInterno']),int(x['idCompetizioneInterno']))
        expectedMap={keys(x):x for x in expected};nativeMap={keys(x):x for x in native}
        if len(nativeMap)!=len(native) or set(nativeMap)!=set(expectedMap):
            err('FCM contesti/membership diversi '+season+' native='+str(len(native))+' independent='+str(len(expected)))
        for key,exp in expectedMap.items():
            actual=nativeMap.get(key)
            if not actual:continue
            for field in ('gare','totSum','pairs','votoFcmSum','saldoPunti','deltaNetTabellino',
                          'bonusPunti','malusPunti','certified'):
                if not near(actual[field],exp[field]):err('FCM differenza '+season+' '+str(key)+' '+field)
            for field in EVENTS:
                if not near(actual['events'][field],exp['events'][field]):err('FCM evento '+season+' '+str(key)+' '+field)
            counts['fcm_context_compared']+=1
        # Compare native Teams counted denominator and TOT against v79 independently.
        actualTeams=collections.defaultdict(lambda:[0,0.0])
        for row in teams[season]:
            # Packed Team: [playerKey,teamId,competitionId, fantasy, cost]
            key=(row[0],int(row[1]),int(row[2]));actualTeams[key][0]+=int(row[3][1]);
            actualTeams[key][1]+=float(row[3][4]);
        for key,exp in expectedMap.items():
            if int(exp['gare'])==0:continue
            t=actualTeams.get(key)
            if not t or t[0]!=exp['gare'] or not near(t[1],exp['totSum']):err('FCM Team score mismatch '+season+' '+str(key))
        counts['fcm_counted']+=audit['counts'].get('counted',0)
    conn.close()
    for f in js.glob('*.js'):
        if f.name.startswith('fcmRecordsNext') and f.stat().st_size>1_500_000:
            err('JS RecordsNext oltre limite: '+f.name+' '+str(f.stat().st_size))
    if '2026_2027' in by_season:
        positive=[]
        for k,item in by_season['2026_2027'].items():
            if int(item[2])!=1 or item[4][0]<=0:continue
            e={event_names[i]:int(v) for i,v in zip(item[4][2][::2],item[4][2][1::2])}
            if e.get('GOLFATTI1',0)>0:err('TF20 2026/27 non zero gol '+k)
            if e.get('ASSIST',0)>0:positive.append((k,e['ASSIST']))
        if positive!=[('2026_2027:136',1)]:err('TF20 2026/27 assist non riconciliati '+str(positive))
    result={'schemaVersion':'rn4-integration-gate-v83','status':'PARITY_VERIFIED_FOR_CHECKED_SOURCES' if not counts['errors_total'] else 'BLOCKED',
            'counts':dict(counts),'errors':errors,'warnings':warnings,
            'limits':'This does NOT verify classic/series/RU/modifiers/thresholds/Culometro against RAW independent oracles',
            'db':str(db),'site':str(site),'previous_v81_reused':True}
    output.mkdir(parents=True,exist_ok=True)
    (output/'RN4_VERIFICA_INTEGRAZIONE_v83.json').write_text(json.dumps(result,indent=2,ensure_ascii=False)+'\n',encoding='utf-8')
    summary=['# RN4 v83 - verifica indipendente','',f"Stato: **{result['status']}**",'']
    summary.extend(f'- {k}: {v}' for k,v in sorted(counts.items()))
    summary.extend(['','## Errori (primi 100)',*(('- '+x) for x in errors),'','## Limiti',result['limits'],''])
    (output/'RN4_VERIFICA_INTEGRAZIONE_v83.md').write_text('\n'.join(summary),encoding='utf-8')
    print('RN4_V83_VERIFY',json.dumps({'status':result['status'],'counts':dict(counts)}))
    return 0 if not counts['errors_total'] else 3

if __name__=='__main__':
    ap=argparse.ArgumentParser()
    ap.add_argument('--db',type=Path,required=True);ap.add_argument('--site',type=Path,required=True)
    ap.add_argument('--oracle',type=Path,required=True);ap.add_argument('--out',type=Path,required=True)
    args=ap.parse_args()
    try:sys.exit(check_site(args.db,args.site,args.oracle,args.out))
    except Exception as e:
        print('V83_VERIFICA_ERRORE',str(e),file=sys.stderr);sys.exit(2)
