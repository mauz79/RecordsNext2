#!/usr/bin/env python3
"""Independent mandatory JSON/JS validation; requires Python 3 only, not Node.
Never changes staging or source DB. Fails closed on coverage, corruption and >1.5MB.
"""
from __future__ import annotations
import argparse,collections,hashlib,json,re,sqlite3
from pathlib import Path

MAX=1_500_000
FACADE='fcmRecordsNext_TopFlopViews.js'
KIND={'Careers','Teams','Records','Highlights'}
DISABLED={'TF05','TF06','TF11','TF12','TF13','TF14','TF15'}

def parse_js(file:Path,kind:str|None=None):
    body=file.read_text(encoding='utf-8')
    if kind is None:
        pre='window.fcmRecordsNextTopFlopViews = '
        if not body.startswith(pre) or not body.endswith(';\n'):raise ValueError('Bad facade JS: '+str(file))
        return json.loads(body[len(pre):-2])
    marker='a.push(...['
    suffix=']);})();\n'
    if body.count(marker)!=1 or not body.endswith(suffix):raise ValueError('Bad shard wrapper: '+str(file))
    return json.loads('['+body.split(marker,1)[1][:-len(suffix)]+']')

def check_pair(vector,keys):
    if not isinstance(vector,list) or len(vector)%2:raise ValueError('Invalid sparse events')
    seen=set()
    for i in range(0,len(vector),2):
        key=vector[i];value=vector[i+1]
        if not isinstance(key,int) or key<0 or key>=len(keys) or key in seen:raise ValueError('Event dictionary invalid')
        seen.add(key)
        if not isinstance(value,(int,float)) or isinstance(value,bool):raise ValueError('Non numeric event')
    return dict((keys[vector[i]],vector[i+1]) for i in range(0,len(vector),2))

def validate(stage:Path,db:Path|None=None):
    if not stage.is_dir():raise ValueError('Missing staged directory')
    fpath=stage/FACADE
    if not fpath.is_file():raise ValueError('Missing views facade')
    f=parse_js(fpath)
    if f.get('schemaVersion')!='4.0-public-views-v1' or f.get('status')!='STAGING_ONLY_NOT_PUBLISHED':
        raise ValueError('Unexpected schema/status')
    if f.get('identity')!='SEASON_LOCAL_NO_AUTO_MERGE':raise ValueError('Historical identity hazard')
    keys=f.get('eventi')
    if len(keys)!=len(set(keys)):raise ValueError('Duplicate event names')
    statuses=f['attivazioneRecord']
    if set(statuses)!={f'TF{i:02}' for i in range(1,21)}:raise ValueError('Record coverage')
    for x in DISABLED:
        if statuses[x]=='ATTIVA':raise ValueError('Unverified ranking activated '+x)
    visited={FACADE}
    numbers=collections.Counter();sizes=collections.Counter();total=fpath.stat().st_size
    maximum=total
    season_list=[]
    for season in f['stagioni']:
        s=season['stagione'];season_list.append(s)
        if not re.fullmatch(r'\d{4}_\d{4}',s):raise ValueError('Invalid season id '+s)
        rows={k:[] for k in KIND}
        for info in season['files']:
            name=info['nome'];kind=info['tipo']
            if kind not in KIND or name in visited or not re.fullmatch(
              rf'fcmRecordsNext_TopFlopViews\.{kind}\.{s}\.\d{{3}}\.js',name):
                raise ValueError('Invalid shard identity '+name)
            visited.add(name)
            file=stage/name
            if not file.is_file():raise ValueError('Missing '+name)
            b=file.read_bytes();size=len(b)
            if size>MAX or size>950000 or size!=info['byte']:raise ValueError('Invalid shard size '+name)
            if hashlib.sha256(b).hexdigest()!=info['sha256']:raise ValueError('SHA mismatch '+name)
            entries=parse_js(file,kind)
            if len(entries)!=info['righe']:raise ValueError('Rows mismatch '+name)
            rows[kind].extend(entries)
            sizes[s]+=size;total+=size;maximum=max(maximum,size)
        careers,teams,records,highlights=(rows[x] for x in ('Careers','Teams','Records','Highlights'))
        counter=season['contatori']
        if len(careers)!=counter['players'] or len(teams)!=counter['teamCompetitionAggregates'] or len(highlights)!=counter['selectedHighlights']:
            raise ValueError('Per-season coverage mismatch '+s)
        ids=set();counted=0
        for c in careers:
            if len(c)!=6 or not c[0].startswith(s+':') or c[0] in ids:raise ValueError('Invalid career '+s)
            ids.add(c[0]);r,fant=c[4],c[5]
            if len(r)!=6 or len(fant)!=9:raise ValueError('Invalid career statistics')
            realEvents=check_pair(r[2],keys);fantasyEvents=check_pair(fant[8],keys)
            if realEvents.get('GOLFATTI1',0)<realEvents.get('GOLFATTISURIGORE1',0):raise ValueError('Doubled penalty goals')
            if realEvents.get('GOLSUBITI',0)<realEvents.get('GOLSUBITISURIGORE',0):raise ValueError('Doubled conceded goals')
            if r[0]<0 or fant[1]>fant[0]:raise ValueError('Invalid denominators')
            counted+=fant[1]
        if counted!=counter['fantasyCounted']:raise ValueError('Counted player sum mismatch '+s)
        d=season['dizionari']
        teamIds={r[0] for r in d['squadre']}
        compIds={r[0] for r in d['competizioni']}
        if len(teamIds)!=len(d['squadre']) or len(compIds)!=len(d['competizioni']):raise ValueError('Dictionary conflicts')
        if any(c[1].strip().lower()=='youth league' for c in d['competizioni']):raise ValueError('Youth League in output')
        teamCounted=0
        for t in teams:
            if len(t)!=4 or t[0] not in ids or t[1] not in teamIds or t[2] not in compIds:
                raise ValueError('Invalid team aggregate')
            g=t[3]
            if len(g)!=9 or g[1]>g[0]:raise ValueError('Invalid team denominator')
            check_pair(g[8],keys);teamCounted+=g[1]
        if teamCounted!=counted:raise ValueError('Team sums and player sums differ')
        highlightKeys={h['key'] for h in highlights}
        if len(highlightKeys)!=len(highlights):raise ValueError('Duplicate selected highlights')
        for h in highlights:
            if h.get('competizione','').lower()=='youth league':raise ValueError('Youth highlight')
            for x in [h,*(h.get('contestiFantasy') or [])]:
                link=x.get('urlTabellino')
                if link is not None and not re.match(r'^https?://',link):raise ValueError('Fake or invalid scorecard')
        for rank in records:
            if rank['recordId'] in DISABLED or rank['recordId'] not in statuses:
                raise ValueError('Unverified record published '+rank['recordId'])
            if rank['pariMerito']!=len(rank['risultati']) or not rank['risultati']:
                raise ValueError('Tie integrity')
            for entry in rank['risultati']:
                if entry.get('highlightKey') and entry['highlightKey'] not in highlightKeys:
                    raise ValueError('Orphan highlight '+entry['highlightKey'])
                if rank['recordId'] in ('TF17','TF18','TF19') and entry['calciatore']['ruoloFca']!=1:
                    raise ValueError('Non-keeper in goalkeeper only record')
        numbers.update(players=len(careers),teams=len(teams),recordGroups=len(records),
            highlights=len(highlights),realDays=counter['realDays'],
            fantasyContexts=counter['fantasyContexts'],fantasyCounted=counter['fantasyCounted'])
    if season_list!=sorted(set(season_list)):raise ValueError('Unexpected chronological season order')
    actual={p.name for p in stage.glob('*.js')}
    if actual!=visited:raise ValueError('Stale or extra JS files in staged directory: '+str(actual-visited))
    if db is not None:
        with sqlite3.connect(db.as_uri()+'?mode=ro',uri=True) as c:
            for sm in f['stagioni']:
                s=sm['stagione'];k=sm['contatori']
                real=c.execute(f'SELECT COUNT(*) FROM "raw_{s}_fca_giocain"').fetchone()[0]
                fantasy=c.execute(f'''SELECT COUNT(*) FROM "raw_{s}_fcm_formazione" ff
                    JOIN "raw_{s}_fcm_incontro" ii ON ii.ID=ff.IDINCONTRO
                    JOIN "raw_{s}_fcm_girone" gg ON gg.ID=ii.IDGIRONE
                    JOIN "raw_{s}_fcm_competizione" cc ON cc.ID=gg.IDCOMPETIZIONE
                    WHERE lower(trim(cc.NOME))<>'youth league' ''').fetchone()[0]
                if real!=k['realDays'] or fantasy!=k['fantasyContexts']:
                    raise ValueError('SQLite -> view input reconciliation failed '+s)
    stat={'seasons':len(season_list),'jsFiles':len(visited),
        'totalJsBytes':total,'maxJsBytes':maximum,'sourceCounters':dict(numbers),
        'perSeasonBytes':dict(sorted(sizes.items())),
        'cumulativeLastSiteBytes':total}
    print('TOP_FLOP_PUBLIC_VIEWS_VALIDATED '+json.dumps(stat,sort_keys=True))
    return stat

def main():
    p=argparse.ArgumentParser();p.add_argument('stage',type=Path)
    p.add_argument('--database',type=Path)
    p.add_argument('--json-output',type=Path)
    a=p.parse_args()
    stat=validate(a.stage,a.database)
    if a.json_output:a.json_output.write_text(json.dumps(stat,indent=2),encoding='utf-8')
if __name__=='__main__':main()
