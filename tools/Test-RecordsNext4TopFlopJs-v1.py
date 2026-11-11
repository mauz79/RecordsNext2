#!/usr/bin/env python3
"""Audit physical JS staging bytes, per-season coverage, player identity, shard navigation.
No database writes, no site publication; works with Python standard library only.
"""
from __future__ import annotations
import argparse
from collections import Counter
import hashlib
import json
from pathlib import Path
import sys

PREFIX='window.fcmRecordsNextTopFlop = '
SHARD_OPEN='.push(...['
SHARD_CLOSE=']);})();'
MAX_BYTES=1_500_000


def parse(jsfile:Path):
    raw=jsfile.read_bytes()
    if len(raw)>MAX_BYTES:raise AssertionError(f'OVER LIMIT {jsfile.name}: {len(raw)}')
    data=raw.decode('utf-8')
    if jsfile.name=='fcmRecordsNext_TopFlop.js':
        if not data.startswith(PREFIX) or not data.rstrip().endswith(';'):
            raise AssertionError('Bad facade assignment')
        return json.loads(data[len(PREFIX):].strip()[:-1])
    start=data.find(SHARD_OPEN)
    end=data.rfind(SHARD_CLOSE)
    if not data.startswith('(function(){') or start<0 or end<start:
        raise AssertionError('Bad shard JS wrapper: '+jsfile.name)
    rows=json.loads('['+data[start+len(SHARD_OPEN):end]+']')
    if not isinstance(rows,list):raise AssertionError('Bad shard payload')
    return rows


def audit(folder:Path)->dict:
    index=parse(folder/'fcmRecordsNext_TopFlop.js')
    if index['maxJsBytes']!=MAX_BYTES or index['status']!='STAGING_ONLY_NOT_PUBLISHED':
        raise AssertionError('Unexpected staging contract')
    declared={'fcmRecordsNext_TopFlop.js'}
    total=Counter();max_bytes=0;links=0
    for season in index['seasons']:
        season_id=season['seasonId']
        for k in ('summaryFiles','detailFiles'):
            declared.update(season[k])
        sums=[];details={};part_counts=Counter()
        for name in season['summaryFiles']:
            if not name.startswith('fcmRecordsNext_TopFlop.Summary.'+season_id+'.'):
                raise AssertionError('Wrong summary season '+name)
            sums.extend(parse(folder/name))
        for name in season['detailFiles']:
            if not name.startswith('fcmRecordsNext_TopFlop.Detail.'+season_id+'.'):
                raise AssertionError('Wrong detail season '+name)
            for item in parse(folder/name):
                key=item['localFcaId']
                if key in details:raise AssertionError('Duplicate detail player '+str(key))
                details[key]=(name,item)
                part_counts[name]+=1
                total['realDays']+=len(item['realDays'])
                total['fantasyContexts']+=len(item['fantasy'])
                for context in item['fantasy']:
                    if context['scorecardUrl'] is not None:
                        if not context['scorecardUrl'].startswith(('http://','https://')):
                            raise AssertionError('Unverified scorecard URL')
                        links+=1
                    if context['competition']=='Youth League':
                        raise AssertionError('Youth League leaked into public detail')
                    if context['competitionRound'] is not None and context['competitionRound']<=0:
                        raise AssertionError('FCM internal round leaked')
        if len(sums)!=season['players'] or len(details)!=season['players']:
            raise AssertionError('Player coverage '+season_id)
        seen=set()
        for s in sums:
            p=s['localFcaId']
            if (s['seasonId'],p) in seen:raise AssertionError('Duplicate summary')
            seen.add((s['seasonId'],p))
            if s['detailFile']!=details[p][0]:raise AssertionError('Wrong lazy detail locator')
            if any(s[k]!=details[p][1][k] for k in ['name','gazzaCode','seasonId']):
                raise AssertionError('Player identity mismatch')
        if sum(len(x['realDays']) for _,x in details.values())!=season['realDays']:
            raise AssertionError('Real day count mismatch '+season_id)
        if sum(len(x['fantasy']) for _,x in details.values())!=season['fantasyContexts']:
            raise AssertionError('Fantasy count mismatch '+season_id)
        seen_contexts=set()
        for _,rec in details.values():
            for f in rec['fantasy']:
                key=(rec['localFcaId'],f['competitionId'],f['matchId'],f['teamId'])
                if key in seen_contexts:raise AssertionError('Duplicated fantasy context '+str(key))
                seen_contexts.add(key)
        total['players']+=len(details)
        total['seasons']+=1
    actual={p.name for p in folder.glob('*.js')}
    if actual!=declared:raise AssertionError('Undeclared / missing files: '+str(actual^declared))
    for f in folder.glob('*.js'):
        max_bytes=max(max_bytes,f.stat().st_size)
    total['files']=len(actual)
    total['scorecardLinks']=links
    total['maxJsBytes']=max_bytes
    total['totalJsBytes']=sum(f.stat().st_size for f in folder.glob('*.js'))
    total['sha256Facade']=hashlib.sha256((folder/'fcmRecordsNext_TopFlop.js').read_bytes()).hexdigest()
    return dict(total)


def main():
    p=argparse.ArgumentParser();p.add_argument('staging_js_dir');p.add_argument('--json-output');a=p.parse_args()
    folder=Path(a.staging_js_dir).resolve()
    summary=audit(folder)
    if a.json_output:
        output=Path(a.json_output)
        if output.exists():raise RuntimeError('Refuse overwrite: '+str(output))
        output.write_text(json.dumps(summary,indent=2,ensure_ascii=False)+'\n',encoding='utf-8')
    print('TOP_FLOP_STAGE_JS_VALIDATE_OK '+json.dumps(summary,ensure_ascii=False))

if __name__=='__main__':
    try:main()
    except Exception as exc:
        print('TOP_FLOP_STAGE_JS_VALIDATE_FAIL '+repr(exc),file=sys.stderr)
        sys.exit(1)
