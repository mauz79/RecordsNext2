'use strict';
const fs = require('fs');
const path = require('path');
const vm = require('vm');
const assert = require('assert');
const stage = process.argv[2];
const decoder = process.argv[3];
if (!stage || !decoder) throw new Error('Usage: node test.js <stage> <decoder>');
const context = {window:{}, console};
vm.createContext(context);
function load(name) { vm.runInContext(fs.readFileSync(path.join(stage,name),'utf8'),context,{filename:name}); }
load('fcmRecordsNext_TopFlop.js');
vm.runInContext(fs.readFileSync(decoder,'utf8'),context,{filename:'decoder'});
const api = context.window.RecordsNextTopFlopCompactV2;
let players = 0, realDays = 0, fantasy = 0;
for (const season of context.window.fcmRecordsNextTopFlop.seasons) {
  for (const name of season.summaryFiles) load(name);
  for (const name of season.detailFiles) load(name);
  for (const name of season.linksFiles) load(name);
  const scorecards = api.indexLinks(context.window.fcmRecordsNextTopFlopData.links[season.seasonId] || []);
  const summaries = context.window.fcmRecordsNextTopFlopData.summaries[season.seasonId].map(r => api.decodeSummary(r,season));
  const details = context.window.fcmRecordsNextTopFlopData.details[season.seasonId].map(r => api.decodeDetail(r,season,scorecards));
  assert.strictEqual(summaries.length,season.players);
  assert.strictEqual(details.length,season.players);
  players += details.length;
  for (const d of details) {
    realDays += d.realDays.length;
    fantasy += d.fantasy.length;
    assert.strictEqual(summaries.find(r=>r.localFcaId === d.localFcaId).name,d.name);
    assert.ok(d.fantasy.every(c=>c.competition !== 'Youth League'));
  }
}
assert.ok(players>0);
console.log('TOP_FLOP_COMPACT_BROWSER_OK',JSON.stringify({players,realDays,fantasy}));
