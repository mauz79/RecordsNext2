#!/usr/bin/env python3
"""PREVIEW ONLY. Read-only SQLite -> 3.2-style flat season shards.
This is NOT the production RecordsNext publisher and never writes to website JS.
"""
import argparse
import hashlib
import json
import re
import sqlite3
from urllib.parse import urljoin,urlsplit
from pathlib import Path

MAX_SHARD=1536*1024
FAMILIES={
 'campionato':'CAMPIONATO','serie a':'CAMPIONATO','serie b':'CAMPIONATO','serie c':'CAMPIONATO',
 'play off - play out':'CAMPIONATO','spareggio serie b':'CAMPIONATO',
 'coppa alterego':'COPPE','coppa reame perduto':'COPPE','coppa serie a':'COPPE',
 'coppa serie b':'COPPE','coppa serie c':'COPPE','coppa uefa':'COPPE',
 'coppa dei campioni':'COPPE','coppa di lega':'COPPE','coppa tra le coppe':'COPPE',
 'europa pipps':'COPPE','mitropa cup':'COPPE','super coppa reame perduto':'COPPE',
 'supercoppa alterego':'COPPE','supercoppa serie a':'COPPE','supercoppa serie b':'COPPE',
 'supercoppa serie c':'COPPE','supercoppa di lega':'COPPE','vera finale di coppa tra le coppe':'COPPE'
}
FIELDS=['p','t','c','m','r','fr','pos','en','es','slot','v','fv','gv','go','gr','as','am','ex']

def mini(v):return json.dumps(v,ensure_ascii=False,separators=(',',':'))
def numeric(x):
 if x is None:return None
 s=str(x).strip().replace(',','.')
 if not re.fullmatch(r'[+-]?\d+(?:\.\d+)?',s):return None
 return float(s)
def normint(v):return int(v or 0)
def match_urls(filename,site_url):
 if not filename or not site_url:return {}
 source=Path(filename).read_text(encoding='utf-8-sig')
 mark='var d='
 if mark not in source:raise RuntimeError('Formato shard Matches 3.2 non riconosciuto')
 data,_=json.JSONDecoder().raw_decode(source.split(mark,1)[1])
 urls={}
 for index,row in data['matches']:
  key=(int(row['matchId']),int(row['teamId']))
  raw=row.get('scorecardUrl')
  if not raw: continue
  parsed=urlsplit(raw);base=urlsplit(site_url)
  if parsed.scheme in ('http','https'):
   resolved=raw
  elif parsed.scheme or parsed.netloc:
   continue
  else:
   # 3.2 uses ../legaYYYY/ris.htm?Gio=N: anchor at that season's own website, not the current site's directory.
   prefix='../'+base.path.strip('/').split('/')[-1]+'/'
   if not raw.startswith(prefix):continue
   resolved=urljoin(site_url.rstrip('/')+'/',raw[len(prefix):])
  urls[key]=resolved
 return urls

def load(c,season,urls):
 pre='raw_'+season+'_fcm_';fca='raw_'+season+'_fca_'
 sql=f'''SELECT f.IDGIOC p, a.CODICEGAZZA code,a.NOME playerName,
 f.IDSQUADRA team,s.NOME teamName,g.IDCOMPETIZIONE comp,c.NOME compName,
 f.IDINCONTRO matchId,i.GIORNATADIA realRound,i.IDGIORNATA fantasyRound,
 f.POS initialPosition,f.ENTRATO entered,f.ENTRATORISERVA enteredReserve,
 t.LISTA roster,t.VOTO votes,t.TOT totals,
 p.VOTO1 realVote,p.GOLFATTI1 goals,p.GOLFATTISURIGORE1 penaltyGoals,
 p.ASSIST assists,p.AMM yellow,p.ESP red
 FROM "{pre}formazione" f
 JOIN "{pre}incontro" i ON i.ID=f.IDINCONTRO
 JOIN "{pre}girone" g ON g.ID=i.IDGIRONE
 JOIN "{pre}competizione" c ON c.ID=g.IDCOMPETIZIONE
 JOIN "{pre}fantasquadra" s ON s.ID=f.IDSQUADRA
 JOIN "{pre}tabellino" t ON t.IDINCONTRO=f.IDINCONTRO AND t.IDSQUADRA=f.IDSQUADRA
 JOIN "{fca}giocatorea" a ON a.ID=f.IDGIOC
 JOIN "{fca}giocain" gi ON gi.IDGIOCATORE=f.IDGIOC AND gi.GIORNATA=i.GIORNATADIA
 LEFT JOIN "{fca}punteggio" p ON p.ID=gi.IDPUNTEGGIO
 ORDER BY f.IDINCONTRO,f.IDSQUADRA,f.IDGIOC'''
 players={};teams={};comps={};matches={};rows=[];skipped=0
 for row in c.execute(sql):
  comp=row['compName'].strip().lower()
  if comp=='youth league':skipped+=1;continue
  family=FAMILIES.get(comp)
  if not family:raise RuntimeError('Competizione non mappata: '+str(row['compName']))
  player=normint(row['p']);team=normint(row['team']);ci=normint(row['comp']);mi=normint(row['matchId'])
  players[str(player)]=[normint(row['code']),row['playerName']]
  teams[str(team)]=row['teamName'];comps[str(ci)]=[row['compName'],family]
  idx=(row['roster'] or '').split('%').index(str(player)) if str(player) in (row['roster'] or '').split('%') else -1
  if idx<0:raise RuntimeError('Formazione senza slot tabellino per '+str((season,mi,team,player)))
  vs=(row['votes'] or '').split('%');tot=(row['totals'] or '').split('%')
  if idx>=len(vs) or idx>=len(tot):raise RuntimeError('Tabellino incompleto')
  if (mi,team) in urls:matches[str(mi)]=urls[(mi,team)]
  rv=numeric(row['realVote']);rv=rv if rv and rv>0 else None
  # v and fv are FCM tabellino values, gv is FCA Gazzetta grade.
  rows.append([player,team,ci,mi,normint(row['realRound']),normint(row['fantasyRound']),
    normint(row['initialPosition']),normint(row['entered']),normint(row['enteredReserve']),idx,
    numeric(vs[idx]),numeric(tot[idx]),rv,normint(row['goals']),normint(row['penaltyGoals']),
    normint(row['assists']),normint(row['yellow']),normint(row['red'])])
 return {'players':players,'teams':teams,'competitions':comps,'matches':matches,'rows':rows},skipped

def write_data(db,output,seasons,links):
 output.mkdir(parents=True,exist_ok=True)
 c=sqlite3.connect('file:'+str(db.resolve())+'?mode=ro',uri=True)
 c.row_factory=sqlite3.Row
 checks=[]
 for season in seasons:
  site=c.execute('SELECT online_site_url FROM rn_season_configuration WHERE season_id=?',(season,)).fetchone()
  online_root=(site[0] or '') if site else ''
  data,skipped=load(c,season,match_urls(links.get(season),online_root))
  data['seasonId']=season
  # 3.2-like queue uses per-season shard and index/row pairs; append-only.
  indexed=[[idx,rec] for idx,rec in enumerate(data['rows'])]
  slim={'rows':indexed,'players':data['players'],'teams':data['teams'],
        'competitions':data['competitions'],'matches':data['matches']}
  script='(function(){var q=window.__recordsNextShardQueue=window.__recordsNextShardQueue||{};var f=q.TopFlop=q.TopFlop||{};var d='+mini(slim)+';f.seasons=f.seasons||{};f.seasons['+mini(season)+']=d;})();\n'
  name='fcmRecordsNext_TopFlop.'+season+'.js';p=output/name
  if len(script.encode('utf-8'))>MAX_SHARD:
   raise RuntimeError('Shard '+name+' supera il limite 3.2 ('+str(len(script.encode('utf-8')))+' > '+str(MAX_SHARD)+'). Nessun dato troncato.')
  if p.exists() and p.read_text(encoding='utf-8') != script: raise RuntimeError('File shard gia presente diverso: '+str(p))
  if not p.exists(): p.write_text(script,encoding='utf-8')
  checks.append({'seasonId':season,'fantasyFacts':len(data['rows']),'players':len(data['players']),
                 'youthExcluded':skipped,'jsBytes':p.stat().st_size,'sha256':hashlib.sha256(p.read_bytes()).hexdigest(),
                 'canonicalScorecardMatches':len(data['matches'])})
 filenames=['fcmRecordsNext_TopFlop.'+s+'.js' for s in seasons]
 final="(function(){var q=window.__recordsNextShardQueue||{},f=q.TopFlop||{};var r=window.fcmRecordsNextTopFlop;Object.keys(f.seasons||{}).forEach(function(s){var d=f.seasons[s];r.seasons[s]={players:d.players,teams:d.teams,competitions:d.competitions,matches:d.matches,rows:d.rows.sort(function(a,b){return a[0]-b[0]}).map(function(x){return x[1]})};});delete q.TopFlop;})();"
 js='window.fcmRecordsNextTopFlop = '+mini({'schemaVersion':'4.0-preview','familyId':'topFlop','columns':FIELDS,'seasons':{}})+';\n'
 js+='(function(){var files='+mini(filenames)+',c=document.currentScript,b=(c&&c.src)?c.src.substring(0,c.src.lastIndexOf("/")+1):"";if(document.readyState!=="loading")throw new Error("TopFlop facade deve essere caricata durante parsing HTML");for(var i=0;i<files.length;i++){document.write("<script src=\\\""+b+files[i]+"\\\"><\\/script>");}document.write("<script>"+'+mini(final)+'+"<\\/script>");})();\n'
 facade=output/'fcmRecordsNext_TopFlop.js'
 if facade.exists() and facade.read_text(encoding='utf-8') != js: raise RuntimeError('Facade gia presente diversa: '+str(facade))
 if not facade.exists():facade.write_text(js,encoding='utf-8')
 manifest=output/'TOPFLOP_PREVIEW_MANIFEST_v1.json'
 content=json.dumps({'previewOnly':True,'database':db.name,'seasons':checks,'maxShardBytes':MAX_SHARD},ensure_ascii=False,indent=2)
 if manifest.exists() and manifest.read_text(encoding='utf-8')!=content:raise RuntimeError('Manifest gia presente diverso: '+str(manifest))
 if not manifest.exists():manifest.write_text(content,encoding='utf-8')
 c.close()
 return checks

if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--database',type=Path,required=True);p.add_argument('--output-js',type=Path,required=True);p.add_argument('--seasons',nargs='+',default=['2006_2007','2026_2027']);p.add_argument('--matches-2006',type=Path);p.add_argument('--matches-2025',type=Path)
 a=p.parse_args();links={'2006_2007':a.matches_2006,'2025_2026':a.matches_2025}
 print(json.dumps(write_data(a.database,a.output_js,a.seasons,links),ensure_ascii=False,indent=2))
