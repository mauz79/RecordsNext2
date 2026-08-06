'use strict';
const fs = require('fs');
const path = require('path');
const vm = require('vm');

function loadJs(file) {
  const sandbox = { window: {} };
  sandbox.globalThis = sandbox;
  vm.createContext(sandbox);
  vm.runInContext(fs.readFileSync(file, 'utf8'), sandbox, { filename: file });
  const values = Object.entries(sandbox.window);
  if (values.length === 0) throw new Error('Nessuna variabile window.* trovata');
  return { variable: values[0][0], data: values[0][1] };
}
function uniq(a){ return [...new Set(a.filter(v => v !== undefined && v !== null && String(v).trim() !== ''))]; }
function seasonOf(x){ return x?.stagione ?? x?.seasonId ?? x?.season ?? null; }
function compOf(x){ return x?.competizioneNome ?? x?.competitionName ?? x?.competizioneStoricaId ?? x?.competitionId ?? null; }
function walk(node, stats, inheritedSeason=null, inheritedComp=null, key='root') {
  if (node == null) return;
  if (Array.isArray(node)) {
    stats.arrays++;
    if (node.length === 0) stats.emptyArrays.push(key);
    for (const item of node) walk(item, stats, inheritedSeason, inheritedComp, key);
    return;
  }
  if (typeof node !== 'object') return;
  stats.objects++;
  const s = seasonOf(node) ?? inheritedSeason;
  const c = compOf(node) ?? inheritedComp;
  if (s) stats.seasons.push(String(s));
  if (c) stats.competitions.push(String(c));
  if (node.urlTabellino || node.scorecardUrl) stats.scorecardLinks++;
  if (node.recordId) stats.recordIds.push(String(node.recordId));
  if (node.eventType) stats.eventTypes.push(String(node.eventType));
  for (const [k,v] of Object.entries(node)) walk(v, stats, s, c, k);
}
function countRows(data) {
  const top = ['events','seasonAggregates','globalAggregates','absoluteOccurrences','ranking'];
  const out = {};
  for (const k of top) out[k] = Array.isArray(data[k]) ? data[k].length : (data[k] == null ? 0 : 1);
  return out;
}
function statusText(data){
  const s = Array.isArray(data.outputStatus) ? data.outputStatus : [];
  return s.map(x => x.status || x.code || '').filter(Boolean).join(', ') || 'NON_DICHIARATO';
}
function esc(v){ return String(v ?? '').replace(/\|/g,'\\|').replace(/\r?\n/g,' '); }

const dir = process.argv[2];
const outDir = process.argv[3] || process.cwd();
if (!dir || !fs.existsSync(dir)) { console.error('Uso: node Audit-RecordsNext2Js.js <cartella-js> [cartella-output]'); process.exit(2); }
fs.mkdirSync(outDir,{recursive:true});
const files = fs.readdirSync(dir).filter(n => /^fcmRecordsNext_.*\.js$/i.test(n)).sort();
const loaded = {};
const errors = [];
for (const name of files) {
  try { loaded[name] = loadJs(path.join(dir,name)); }
  catch(e) { errors.push({file:name,error:e.message}); }
}
const core = loaded['fcmRecordsNext_Core.js']?.data || {};
const expectedSeasons = uniq((core.seasons || []).map(seasonOf));
const rows = [];
for (const [file, obj] of Object.entries(loaded)) {
  const d = obj.data;
  const stats={arrays:0,objects:0,seasons:[],competitions:[],scorecardLinks:0,recordIds:[],eventTypes:[],emptyArrays:[]};
  walk(d,stats);
  const seasons=uniq(stats.seasons).sort();
  const comps=uniq(stats.competitions).sort();
  const missing=expectedSeasons.filter(s=>!seasons.includes(String(s)));
  const declared=d.metadata?.seasonCount ?? null;
  const counts=countRows(d);
  rows.push({
    file, variable:obj.variable, familyId:d.familyId || (file.includes('Core')?'core':file.includes('Manifest')?'manifest':''),
    schemaVersion:d.schemaVersion || '', status:statusText(d), declaredSeasonCount:declared,
    actualSeasonCount:seasons.length, expectedSeasonCount:expectedSeasons.length,
    seasons, missingSeasons:missing, competitionCount:comps.length, competitions:comps,
    events:counts.events, seasonAggregates:counts.seasonAggregates, globalAggregates:counts.globalAggregates,
    absoluteOccurrences:counts.absoluteOccurrences, ranking:counts.ranking,
    recordIdCount:uniq(stats.recordIds).length, recordIds:uniq(stats.recordIds).sort(),
    eventTypeCount:uniq(stats.eventTypes).length, eventTypes:uniq(stats.eventTypes).sort(),
    scorecardLinks:stats.scorecardLinks, emptyTopLevel: ['events','seasonAggregates','globalAggregates','absoluteOccurrences','ranking'].filter(k=>Array.isArray(d[k])&&d[k].length===0),
    metadata:d.metadata || {}
  });
}
const audit={generatedAt:new Date().toISOString(),sourceDirectory:path.resolve(dir),expectedSeasons,filesFound:files.length,filesLoaded:rows.length,errors,rows};
fs.writeFileSync(path.join(outDir,'RecordsNext2_JS_AUDIT.json'),JSON.stringify(audit,null,2),'utf8');
const headers=['File','Famiglia','Stato','Stagioni dichiarate','Stagioni reali','Stagioni attese','Stagioni mancanti','Competizioni','Eventi','Aggregati stagione','Ranking','Record ID','Tipi evento','Link tabellino'];
const csv=[headers.join(';')];
for(const r of rows) csv.push([r.file,r.familyId,r.status,r.declaredSeasonCount??'',r.actualSeasonCount,r.expectedSeasonCount,r.missingSeasons.join(','),r.competitionCount,r.events,r.seasonAggregates,r.ranking,r.recordIdCount,r.eventTypeCount,r.scorecardLinks].map(v=>'"'+String(v).replace(/"/g,'""')+'"').join(';'));
fs.writeFileSync(path.join(outDir,'RecordsNext2_JS_AUDIT.csv'),'\ufeff'+csv.join('\r\n'),'utf8');
let md='# Audit JS RecordsNext 2.0\n\n';
md+=`Generato: ${audit.generatedAt}\n\nCartella analizzata: \`${audit.sourceDirectory}\`\n\n`;
md+=`Stagioni attese dal Core: **${expectedSeasons.length}** (${expectedSeasons.join(', ') || 'non rilevate'})\n\n`;
md+='## Riepilogo\n\n| File | Famiglia | Stato | Stagioni reali/attese | Mancanti | Competizioni | Eventi | Aggregati | Ranking | Link |\n|---|---|---:|---:|---|---:|---:|---:|---:|---:|\n';
for(const r of rows) md+=`| ${esc(r.file)} | ${esc(r.familyId)} | ${esc(r.status)} | ${r.actualSeasonCount}/${r.expectedSeasonCount} | ${esc(r.missingSeasons.join(', ')||'—')} | ${r.competitionCount} | ${r.events} | ${r.seasonAggregates} | ${r.ranking} | ${r.scorecardLinks} |\n`;
md+='\n## Anomalie rilevate\n\n';
let anomaly=0;
for(const r of rows){
  if(r.expectedSeasonCount>0 && r.actualSeasonCount>0 && r.actualSeasonCount<r.expectedSeasonCount){ md+=`- **${r.file}** copre ${r.actualSeasonCount}/${r.expectedSeasonCount} stagioni. Mancano: ${r.missingSeasons.join(', ')}.\n`; anomaly++; }
  if(/PARTIAL|SKIPPED|ERROR/i.test(r.status)){ md+=`- **${r.file}** dichiara stato: ${r.status}.\n`; anomaly++; }
  if(r.declaredSeasonCount!=null && r.declaredSeasonCount!==r.actualSeasonCount){ md+=`- **${r.file}** dichiara seasonCount=${r.declaredSeasonCount}, ma nel contenuto risultano ${r.actualSeasonCount} stagioni.\n`; anomaly++; }
}
if(!anomaly) md+='Nessuna anomalia automatica rilevata.\n';
md+='\n## Dettaglio per file\n';
for(const r of rows){
 md+=`\n### ${r.file}\n\n`;
 md+=`- Variabile globale: \`window.${r.variable}\`\n- Schema: ${r.schemaVersion||'—'}\n- Stato: ${r.status}\n- Stagioni: ${r.seasons.join(', ')||'—'}\n- Competizioni (${r.competitionCount}): ${r.competitions.join(', ')||'—'}\n- Eventi: ${r.events}; aggregati stagionali: ${r.seasonAggregates}; ranking: ${r.ranking}\n- Record ID (${r.recordIdCount}): ${r.recordIds.join(', ')||'—'}\n- Tipi evento (${r.eventTypeCount}): ${r.eventTypes.join(', ')||'—'}\n- Link tabellino rilevati: ${r.scorecardLinks}\n`;
}
if(errors.length){ md+='\n## Errori di lettura\n\n'; for(const e of errors) md+=`- ${e.file}: ${e.error}\n`; }
fs.writeFileSync(path.join(outDir,'RecordsNext2_JS_AUDIT.md'),md,'utf8');
console.log(JSON.stringify({files:rows.length,expectedSeasons:expectedSeasons.length,output:outDir},null,2));
