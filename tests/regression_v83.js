'use strict';
const fs=require('node:fs'),vm=require('node:vm'),path=require('node:path'),assert=require('node:assert/strict');
const assets=fs.existsSync(path.join(__dirname,'..','assets','js'))?path.join(__dirname,'..','assets','js'):path.join(__dirname,'..','SITE','js');
const target={};for(const file of ['fcmRecordsNextTopFlopRankingEngine_v83.js','fcmRecordsNextEventRanking_v83.js','fcmRecordsNextPlayerProfile_v79.js','fcmRecordsNextGlobalSearch_v79.js'])
 vm.runInNewContext(fs.readFileSync(path.join(assets,file),'utf8'),target);
const rank=target.RecordsNextRankingEngineV69;
const make=(key,name,role,votes,goals,assists,penalty=0)=>({playerKey:'2026_2027:'+key,nome:name,ruoloFca:role,
  reale:{votiValidi:votes,eventi:{GOLFATTI1:goals,GOLFATTISURIGORE1:penalty,ASSIST:assists}},fantasy:{conteggiati:votes,eventiApportati:{GOLFATTI1:goals,ASSIST:assists}}});
const careers=[make(136,'CARNESECCHI Marco',1,11,0,1),make(5,'PORTIERE B',1,11,0,0),make(6,'PORTIERE SENZA VOTO',1,0,0,0),make(7,'ATTACCANTE X',4,12,8,2)];
const season=rank.indexedSeason({season:'2026_2027',careers,teams:[],real:[],counted:[],bench:[],dictionaries:{}});
const view=(id,event)=>rank.build({seasons:[season]},{id,context:'real',keeperEvent:event,direction:'MAX'});
assert.equal(view('TF20','GOLFATTI1').length,0);
const assists=view('TF20','ASSIST');assert.equal(assists.length,1);assert.equal(assists[0].playerKey,'2026_2027:136');
assert.equal(assists[0].gol,0);assert.equal(assists[0].assist,1);assert.equal(assists[0].totaleOffensivo,1);
assert.equal(view('TF20','TOTAL_OFFENSE').length,1);assert.equal(view('TF07','GOLFATTI1').length,1);
assert.equal(view('TF08','ASSIST').length,2);
const ctx=target.RecordsNextPlayerProfileV79;
const key='2026_2027:136',mk=(cid,competition,count,tot,events)=>({playerKey:key,idSquadraInterno:20,
 fantasquadra:'Esempio',idCompetizioneInterno:cid,competizione:competition,apporto:{conteggiati:count,conVotoRealeValido:count,
 sommaVotiRealiApportati:6*count,sommaFantavotiApportati:tot,eventiApportati:events}});
const e={GOLFATTI1:0,ASSIST:1,RIGPAR:1,GOLSUBITI:2};
const sources=[mk(10,'Serie A',2,16,e),mk(20,'Coppa',1,6.5,{AMM:1})];
const score=[{playerKey:key,idSquadraInterno:20,idCompetizioneInterno:10,gare:2,pairs:2,totSum:16,
 bonusPunti:4,malusPunti:2,saldoPunti:2,certified:true},
 {playerKey:key,idSquadraInterno:20,idCompetizioneInterno:20,gare:1,pairs:1,totSum:6.5,
 bonusPunti:null,malusPunti:null,saldoPunti:0,certified:false}];
const summary=ctx.aggregateSeasonTeams(ctx.attachScorecards(ctx.seasonTeamContexts(sources,'2026_2027',key),score));
assert.equal(summary.length,1);assert.equal(summary[0].gare,3);assert.equal(summary[0].assist,1);
assert.equal(summary[0].bonusPunti,null);assert.equal(summary[0].malusPunti,null);assert.equal(summary[0].saldoPunti,2);
const names=target.RecordsNextGlobalSearchV79;assert.equal(names.fold('MiRoSlAv KLOSE'),'miroslav klose');
for(const f of fs.readdirSync(assets)) if(f.endsWith('.js')) assert.ok(fs.statSync(path.join(assets,f)).size<=1_500_000,f);
const html=fs.readFileSync(path.join(__dirname,'..',fs.existsSync(path.join(__dirname,'..','assets','RecordsNext'))?'assets':'SITE','RecordsNext','top_flop.html'),'utf8');
assert.ok(!html.includes('tf-global-search'));assert.ok(html.includes('Filtra la classifica'));
assert.ok(fs.existsSync(path.join(__dirname,'..',fs.existsSync(path.join(__dirname,'..','assets','RecordsNext'))?'assets':'SITE','RecordsNext','calciatori.html')));
console.log('RN4_V83_NODE_OK TF20-positive-only, visible goal+assist, profile 15 metrics, FCM incomplete split, separate search, size');
