'use strict';
const fs=require('fs'),path=require('path'),vm=require('vm'),crypto=require('crypto'),assert=require('assert');
const stage=process.argv[2],decoder=process.argv[3];
if(!stage || !decoder)throw new Error('Usage: node test.js <stage-js-dir> <decoder>');
const ctx={window:{},console};vm.createContext(ctx);
function load(name){const data=fs.readFileSync(path.join(stage,name),'utf8');
  vm.runInContext(data,ctx,{filename:name,timeout:30000});}
load('fcmRecordsNext_TopFlopViews.js');
vm.runInContext(fs.readFileSync(decoder,'utf8'),ctx,{filename:'decoder',timeout:30000});
const f=ctx.window.fcmRecordsNextTopFlopViews,api=ctx.window.RecordsNextTopFlopViewsV1;
assert.equal(f.schemaVersion,'4.0-public-views-v1');
assert.equal(f.status,'STAGING_ONLY_NOT_PUBLISHED');
const counters={seasons:0,careers:0,teams:0,records:0,highlights:0,realDays:0,fantasy:0,
  fantasyCounted:0,totalBytes:fs.statSync(path.join(stage,'fcmRecordsNext_TopFlopViews.js')).size,
  maxBytes:0,linkHits:0,goaliesWithGoals:0,unverifiedRankings:0};
let previous='';
for(const season of f.stagioni){
  counters.seasons++;
  assert.ok(season.stagione > previous);previous=season.stagione;
  for(const file of season.files){
    const p=path.join(stage,file.nome),bytes=fs.readFileSync(p);
    assert.equal(bytes.length,file.byte);
    assert.ok(bytes.length<=1500000,'JS too large '+file.nome);
    assert.equal(crypto.createHash('sha256').update(bytes).digest('hex'),file.sha256);
    load(file.nome);counters.totalBytes+=bytes.length;
    counters.maxBytes=Math.max(counters.maxBytes,bytes.length);
  }
  const raw=ctx.window.fcmRecordsNextTopFlopViewsData;
  const careers=(raw.Careers||{})[season.stagione]||[],teams=(raw.Teams||{})[season.stagione]||[],
    records=(raw.Records||{})[season.stagione]||[], highlights=(raw.Highlights||{})[season.stagione]||[];
  assert.equal(careers.length,season.contatori.players);
  assert.equal(teams.length,season.contatori.teamCompetitionAggregates);
  assert.equal(records.length,season.files.filter(x=>x.tipo==='Records').reduce((a,x)=>a+x.righe,0));
  assert.equal(highlights.length,season.contatori.selectedHighlights);
  const decoded=careers.map(api.decodeCareer),groups=teams.map(x=>api.decodeTeam(x,season.stagione));
  const keys=new Set(decoded.map(c=>c.playerKey));
  assert.equal(keys.size,decoded.length);
  assert.ok(decoded.every(c=>!c.identitaStoricaVerificata));
  assert.ok(groups.every(x=>keys.has(x.playerKey)));
  assert.ok(groups.every(x=>x.competizione.toLowerCase() !== 'youth league'));
  assert.ok(groups.every(x=>x.apporto.conteggiati<=x.apporto.contestiOsservati));
  assert.ok(decoded.every(x=>x.reale.votiValidi>=0 && x.fantasy.conteggiati<=x.fantasy.contestiOsservati));
  const count=decoded.reduce((s,c)=>s+c.fantasy.conteggiati,0);
  assert.equal(count,season.contatori.fantasyCounted);
  assert.equal(groups.reduce((s,x)=>s+x.apporto.conteggiati,0),count);
  const hmap=new Map(highlights.map(x=>[x.key,x]));
  assert.equal(hmap.size,highlights.length);
  for(const x of highlights){
    assert.ok(x.competizione !== 'Youth League');
    if(x.urlTabellino){assert.ok(/^https?:\/\//.test(x.urlTabellino));counters.linkHits++;}
  }
  for(const rec of records){
    assert.ok(/^TF(0[1-9]|1[0-9]|20)$/.test(rec.recordId));
    assert.ok(!['TF05','TF06','TF11','TF12','TF13','TF14','TF15'].includes(rec.recordId));
    assert.equal(rec.pariMerito,rec.risultati.length);
    for(const entry of rec.risultati){
      if(entry.highlightKey)assert.ok(hmap.has(entry.highlightKey),`Missing ${entry.highlightKey}`);
      if(rec.recordId.startsWith('TF1') && ['TF17','TF18','TF19'].includes(rec.recordId))
        assert.equal(entry.calciatore.ruoloFca,1);
    }
  }
  for(const c of decoded){
    if(c.ruoloFca===1 && (c.reale.eventi.GOLFATTI1||0)>0)counters.goaliesWithGoals++;
    if(c.reale.eventi.GOLFATTI1!==undefined && c.reale.eventi.GOLFATTISURIGORE1!==undefined)
      assert.ok(c.reale.eventi.GOLFATTI1 >= c.reale.eventi.GOLFATTISURIGORE1);
    if(c.reale.eventi.GOLSUBITI!==undefined && c.reale.eventi.GOLSUBITISURIGORE!==undefined)
      assert.ok(c.reale.eventi.GOLSUBITI >= c.reale.eventi.GOLSUBITISURIGORE);
  }
  counters.careers+=decoded.length;counters.teams+=groups.length;
  counters.records+=records.length;counters.highlights+=highlights.length;
  counters.realDays+=season.contatori.realDays;
  counters.fantasy+=season.contatori.fantasyContexts;
  counters.fantasyCounted+=count;
}
assert.ok(counters.careers>0 && counters.records>0);
assert.ok(counters.totalBytes<60000000,'Public views have not reduced 60MB');
console.log('TOP_FLOP_PUBLIC_VIEWS_BROWSER_OK',JSON.stringify(counters));
