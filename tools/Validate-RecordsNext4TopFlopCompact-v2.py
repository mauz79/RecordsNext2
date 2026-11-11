#!/usr/bin/env python3
"""Read-only verifier for RecordsNext4 compact TopFlop staging JS.

Checks cardinalities, player identity, event integrity, canonical link lookup,
physical byte sizes, and (when provided) exact semantic parity with v1 JS.
"""
from __future__ import annotations
import argparse
from collections import Counter
from pathlib import Path
import hashlib
import json

MAX_BYTES = 1_500_000
STATES = ['VALID','NO_VOTE','SPECIAL','NOT_PLAYED','UNKNOWN']
USES = ['COUNTED','BENCH_UNUSED','OUTSIDE_LIST','EXCLUDED_CONFIRMED','UNKNOWN']

def parse(path):
    data=path.read_bytes()
    assert len(data)<=MAX_BYTES, (path.name,len(data))
    js=data.decode('utf-8')
    if path.name=='fcmRecordsNext_TopFlop.js':
        p='window.fcmRecordsNextTopFlop = '
        assert js.startswith(p) and js.rstrip().endswith(';'),path
        return json.loads(js[len(p):].strip()[:-1])
    p='.push(...['
    i=js.index(p)+len(p)
    end=js.rfind(']);})();')
    assert end>=i and js.startswith('(function(){'),path
    return json.loads('['+js[i:end]+']')

def files_data(folder,names):
    for name in names:
        for rec in parse(folder/name):yield name,rec

def event_map(data,events):
    if data is None:return {}
    assert len(data)%2==0
    result={}
    for i in range(0,len(data),2):
        k=events[data[i]]
        assert k not in result
        result[k]=data[i+1]
    return result

def unpack_real_day(row,events):
    assert 2<=len(row)<=4
    return {'serieARound':row[0],'voteState':STATES[row[1]],
            'vote':row[2] if len(row)>2 else None,
            'sourceEvents':event_map(row[3] if len(row)>3 else None,events)}

def unpack_summary(row,season,metadata):
    assert len(row)==11
    teams=dict(metadata['teams'])
    competitions={x[0]:x[1:] for x in metadata['competitions']}
    events=metadata['eventNames']
    groups=[]
    for a in row[10]:
        assert len(a)==10
        competition=competitions[a[1]]
        groups.append({'teamId':a[0],'team':teams[a[0]],'competitionId':a[1],
            'competition':competition[0],'family':competition[1],
            'contexts':a[2],'counted':a[3],'countedWithRealVote':a[4],
            'voteSum':a[5],'meanVote':a[6],'fantasySum':a[7],
            'meanFantasy':a[8],'sourceEventsApportioned':event_map(a[9],events)})
    assert 0<=row[3]<len(metadata['detailFiles'])
    return {'seasonId':season,'localFcaId':row[0],'gazzaCode':row[1],'name':row[2],
        'detailFile':metadata['detailFiles'][row[3]],
        'real':{'days':row[4],'withVote':row[5],'voteSum':row[6],
                'meanVote':row[7],'sourceEvents':event_map(row[8],events)},
        'fantasyContexts':row[9],'contributions':groups}

def unpack_detail(row,season,metadata,links):
    assert len(row)==5
    teams=dict(metadata['teams']); comps={x[0]:x[1:] for x in metadata['competitions']}
    events=metadata['eventNames'];desc=metadata['roundDescriptions']; evidence=metadata['usageEvidence']
    contexts=[]
    for f in row[4]:
        assert len(f)==17,(season,row[0],f)
        comp=comps[f[1]]
        contexts.append({'matchId':f[0],'competitionId':f[1],'competition':comp[0],
           'family':comp[1],'competitionRound':f[2],
           'roundDescription':None if f[3] is None else desc[f[3]],
           'serieARound':f[4],'teamId':f[5],'team':teams[f[5]],
           'usage':USES[f[6]],'initialStarter':bool(f[7]),
           'tabellinoVote':f[8],'fantasyVote':f[9],
           'sourceEvents':event_map(f[10],events),
           'scorecardUrl':links.get((f[0],f[5])),
           'position':f[11],'entered':f[12],'enteredAsReserve':f[13],
           'firstEleven':f[14],'modifier':f[15],
           'usageEvidence':None if f[16] is None else evidence[f[16]]})
    return {'seasonId':season,'localFcaId':row[0],'gazzaCode':row[1],
            'name':row[2],'realDays':[unpack_real_day(x,events) for x in row[3]],
            'fantasy':contexts}

def audit(path,old=None):
    index=parse(path/'fcmRecordsNext_TopFlop.js')
    assert index['schemaVersion']=='4.0-stage-v2-compact'
    assert index['status']=='STAGING_ONLY_NOT_PUBLISHED'
    assert index['maxJsBytes']==MAX_BYTES
    old_facade=parse(old/'fcmRecordsNext_TopFlop.js') if old else None
    old_seasons={x['seasonId']:x for x in old_facade['seasons']} if old_facade else {}
    result=Counter()
    declared={'fcmRecordsNext_TopFlop.js'}
    details_by_season={}
    for md in index['seasons']:
        season=md['seasonId'];result['seasons']+=1
        links={}
        for _,l in files_data(path,md['linksFiles']):
            assert len(l)==3
            k=tuple(l[:2]); assert k not in links
            assert isinstance(l[2],str) and l[2].startswith(('https://','http://'))
            links[k]=l[2]
        result['uniqueScorecardLinks']+=len(links)
        declared.update(md['linksFiles']+md['detailFiles']+md['summaryFiles'])
        detail={}
        detail_file={}
        for filename,packed in files_data(path,md['detailFiles']):
            d=unpack_detail(packed,season,md,links)
            pid=d['localFcaId'];assert pid not in detail
            detail[pid]=d;detail_file[pid]=filename
            result['realDays']+=len(d['realDays'])
            result['fantasyContexts']+=len(d['fantasy'])
            result['resolvedContextLinks']+=sum(f['scorecardUrl'] is not None for f in d['fantasy'])
            assert len({(f['matchId'],f['competitionId'],f['teamId']) for f in d['fantasy']})==len(d['fantasy'])
            assert all(f['competition']!='Youth League' for f in d['fantasy'])
            assert all(f['competitionRound'] is None or f['competitionRound']>0 for f in d['fantasy'])
        summary={}
        for _,packed in files_data(path,md['summaryFiles']):
            r=unpack_summary(packed,season,md)
            pid=r['localFcaId'];assert pid not in summary
            summary[pid]=r
        assert len(summary)==len(detail)==md['players']
        assert sum(len(d['realDays']) for d in detail.values())==md['realDays']
        assert sum(len(d['fantasy']) for d in detail.values())==md['fantasyContexts']
        for pid,d in detail.items():
            r=summary[pid]
            assert r['detailFile']==detail_file[pid]
            assert (r['name'],r['gazzaCode'])==(d['name'],d['gazzaCode'])
        if old is not None:
            older=old_seasons[season]
            original_details={d['localFcaId']:d for _,d in files_data(old,older['detailFiles'])}
            original_summary={d['localFcaId']:d for _,d in files_data(old,older['summaryFiles'])}
            for pid,d in detail.items():
                assert d==original_details[pid],f'V1/V2 DETAIL MISMATCH {season}/{pid}'
                assert {k:v for k,v in summary[pid].items() if k!='detailFile'}=={k:v for k,v in original_summary[pid].items() if k!='detailFile'},f'V1/V2 SUMMARY MISMATCH {season}/{pid}'
            result['semanticallyComparedPlayers']+=len(detail)
        result['players']+=len(detail)
    actual={x.name for x in path.glob('*.js')}
    assert actual==declared, f'file mismatch {actual^declared}'
    result['jsFiles']=len(actual)
    result['totalJsBytes']=sum(p.stat().st_size for p in path.glob('*.js'))
    result['maxJsBytes']=max(p.stat().st_size for p in path.glob('*.js'))
    result['sha256Facade']=hashlib.sha256((path/'fcmRecordsNext_TopFlop.js').read_bytes()).hexdigest()
    return dict(result)

def main():
    p=argparse.ArgumentParser()
    p.add_argument('stage',type=Path)
    p.add_argument('--compare-v1',type=Path)
    p.add_argument('--json-output',type=Path)
    args=p.parse_args()
    result=audit(args.stage,args.compare_v1)
    if args.json_output:
        if args.json_output.exists():raise RuntimeError('Refuse overwrite '+str(args.json_output))
        args.json_output.write_text(json.dumps(result,indent=2,ensure_ascii=False)+'\n',encoding='utf-8')
    print('TOP_FLOP_COMPACT_V2_VALIDATED',json.dumps(result,ensure_ascii=False))

if __name__=='__main__':main()
