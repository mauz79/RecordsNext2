'use strict';
const fs=require('fs'),vm=require('vm'),path=require('path');
const dir=path.resolve(__dirname,'../release/visualizzatori/js');
const writes=[];
const sandbox={window:{},document:{readyState:'loading',currentScript:{src:'file://'+path.join(dir,'fcmRecordsNext_TopFlop.js')},write:s=>writes.push(s)}};
vm.createContext(sandbox);
function run(p){vm.runInContext(fs.readFileSync(p,'utf8'),sandbox,{filename:p});}
run(path.join(dir,'fcmRecordsNext_TopFlop.js'));
for(const s of writes){const src=s.match(/<script src="([^"]+)"/);if(src){run(src[1].replace(/^file:\/\//,''));continue;}const js=s.match(/^<script>(.*)<\/script>$/s);if(js){vm.runInContext(js[1],sandbox);continue;}throw Error('Script tag non previsto: '+s.slice(0,100))}
const root=sandbox.window.fcmRecordsNextTopFlop;
if(!root||Object.keys(root.seasons).length!==2)throw Error('facade seasons incomplete');
const total=Object.values(root.seasons).reduce((a,x)=>a+x.rows.length,0);
if(total!==13700)throw Error('Expected 13,700 rows, got '+total);
if(root.seasons['2006_2007'].rows.length!==6050)throw Error('2006 wrong');
if(root.seasons['2026_2027'].rows.length!==7650)throw Error('2026 wrong');
const urls=root.seasons['2006_2007'].matches;
for(const u of Object.values(urls)){if(!u.startsWith('http://www.alterlega.altervista.org/lega2006/ris.'))throw Error('URL historical wrong: '+u)}
if(Object.keys(root.seasons['2026_2027'].matches).length)throw Error('2026 lacks canonical URLs');
console.log('TOPFLOP_JS_OK seasons=2 rows=13700 canonicalMatchIds='+Object.keys(urls).length);
