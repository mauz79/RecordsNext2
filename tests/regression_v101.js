'use strict';
const fs=require('fs'),vm=require('vm'),assert=require('assert');
const source=fs.readFileSync(process.argv[2],'utf8');
const box={window:{}};vm.runInNewContext(source,box,{filename:'profile-v101.js'});
const model=box.window.RecordsNextPlayerProfileV79;
assert(model&&model.aggregateSeasonTeams);
const season='2026_2027',playerKey='2026_2027:10',idSquadraInterno=4;
function ctx(c){return {stagione:season,playerKey,idSquadraInterno,fantasquadra:'Squadra A',competizione:c.name,idCompetizioneInterno:c.id,
 gareConteggiate:c.gare,gareConVotoReale:c.gare,sommaVotiRealiApportati:6*c.gare,sommaFantavotiApportati:7*c.gare,
 eventiApportati:{},scorecard:c.score};}
const zero=model.aggregateSeasonTeams([ctx({name:'Coppa',id:1,gare:0,score:null})]);
assert.equal(zero[0].riconciliazione,'Nessun apporto');
assert.equal(zero[0].competizioni[0].riconciliazione,'Nessun apporto');
assert.equal(zero[0].saldoPunti,null);
const score=(certified,saldoPunti)=>({gare:1,pairs:1,totSum:7,saldoPunti,certified,bonusPunti:3,malusPunti:0.5});
const partial=model.aggregateSeasonTeams([ctx({name:'Serie A',id:2,gare:1,score:score(false,2.5)})]);
assert.equal(partial[0].riconciliazione,'Saldo verificato');
assert.equal(partial[0].competizioni[0].riconciliazione,'Saldo verificato');
assert.equal(partial[0].saldoPunti,2.5);
assert.equal(partial[0].bonusPunti,null);
const complete=model.aggregateSeasonTeams([ctx({name:'Serie A',id:2,gare:1,score:score(true,2.5)})]);
assert.equal(complete[0].riconciliazione,'OK');
assert.equal(complete[0].competizioni[0].riconciliazione,'OK');
assert.equal(complete[0].saldoPunti,2.5);
assert.equal(complete[0].bonusPunti,3);
const missing=model.aggregateSeasonTeams([ctx({name:'Serie A',id:2,gare:1,score:score(false,null)})]);
assert.equal(missing[0].riconciliazione,'Dati insufficienti');
assert.equal(missing[0].saldoPunti,null);
const two=model.aggregateSeasonTeams([
 ctx({name:'Coppa A',id:3,gare:1,score:score(true,2.5)}),
 ctx({name:'Coppa B',id:4,gare:1,score:score(false,2.5)})
]);
assert.equal(two.length,1);assert.equal(two[0].gare,2);
assert.equal(two[0].saldoPunti,5);assert.equal(two[0].bonusPunti,null);
assert.equal(two[0].riconciliazione,'Saldo verificato');
console.log('RN4_V101_PROFILE_OK: zero, saldo, split, assenza, multi-competizione');
