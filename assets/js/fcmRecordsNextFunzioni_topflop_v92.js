/* RecordsNext 4.0 v92: focused selectors, per-record columns; TF20-G/TF20-A virtual independent presentations of TF20. */
(function(root,doc){'use strict';
 const api=root.RecordsNextTopFlopViewsV2,engine=root.RecordsNextRankingEngineV69,profile=root.RecordsNextPlayerProfileV79,events=root.RecordsNextEventsV72;
 const $=id=>doc.getElementById(id), mk=(tag,text)=>{const e=doc.createElement(tag);if(text!==undefined)e.textContent=String(text);return e;};
 const state={manifest:null,facade:null,season:'',selected:'TF01',token:0,drawToken:0,loaded:new Map(),history:null,historyPromise:null,profile:null,ranked:[],renderObserver:null,fmtManifestPromise:null,fmtPending:new Map(),profileToken:0,pointsManifestPromise:null,pointsPending:new Map(),globalPending:null};
 const seasonLabel=s=>String(s).replace('_','/'),fmt=v=>v===null||v===undefined||v===''?'—':typeof v==='number'?v.toLocaleString('it-IT',{maximumFractionDigits:3}):String(v);
 const themes=['mauzstrom','fantablue2','maelstrom','neutral'];
 const detail=()=>$('tf-detail');
 const baseRecord=id=>id==='TF20_GOL'||id==='TF20_ASSIST'?'TF20':id;
 const keeperEvent=id=>id==='TF20_ASSIST'?'ASSIST':'GOLFATTI1';
 const selectedRecord=()=>$('tf-record').value;
 const recordTitle=()=>($('tf-record').selectedOptions[0]||{}).textContent||'Record';
 function clear(e){e.replaceChildren();}
 function option(el,val,caption){const o=mk('option',caption);o.value=val;el.append(o);}
 function info(text){$('tf-feedback').textContent=text;}
 function loadScript(path){return new Promise((resolve,reject)=>{const s=mk('script');s.src=path;s.async=true;s.onload=resolve;s.onerror=()=>reject(Error('Non riesco a caricare '+path));doc.head.append(s);});}
 function manifestSeason(season){return state.manifest.stagioni.find(x=>x.stagione===season);}
 function cacheData(season){let entry=state.loaded.get(season);if(!entry){entry={season,careers:null,teams:null,bench:null,real:null,counted:null,partial:{}};state.loaded.set(season,entry);}return entry;}
 function needed(id){id=baseRecord(id);return {teams:['TF06','TF16'].includes(id),bench:['TF11','TF12','TF13'].includes(id),real:['TF01','TF02'].includes(id),counted:['TF03','TF04'].includes(id),history:['TF14','TF15'].includes(id)};}
 async function ensureSection(season,kind){const data=cacheData(season),prop=kind==='RealPerformances'?'real':kind==='CountedPerformances'?'counted':kind.toLowerCase();
   if(data[prop]!==null)return;
   if(data.partial[prop])return data.partial[prop];
   const job=(async()=>{
     if(kind==='RealPerformances'||kind==='CountedPerformances'){
       const m=manifestSeason(season);if(!m)throw Error('Manifest v69 senza stagione '+season);
       const files=m.files.filter(f=>f.tipo===kind);for(const file of files)await loadScript('../js/'+file.nome);
       const arr=((root.fcmRecordsNextTopFlopViewsData||{})[kind]||{})[season]||[];
       if(arr.length!==files.reduce((n,f)=>n+f.righe,0))throw Error('Shard v69 incompleti: '+season+' '+kind);
       data[prop]=arr;
     } else data[prop]=await api.loadView('../js',season,kind);
   })();data.partial[prop]=job;
   try{await job;}finally{delete data.partial[prop];}
 }
 async function ensureHistory(){if(state.history!==null)return state.history;
   if(!state.historyPromise)state.historyPromise=api.loadHistory('../js').then(data=>{state.history=data;return data;}).catch(e=>{state.historyPromise=null;throw e;});
   return state.historyPromise;}
 async function ensureSeason(season,requirements){await ensureSection(season,'Careers');
   const jobs=[];
   if(requirements.teams)jobs.push(ensureSection(season,'Teams'));
   if(requirements.bench)jobs.push(ensureSection(season,'Bench'));
   if(requirements.real)jobs.push(ensureSection(season,'RealPerformances'));
   if(requirements.counted)jobs.push(ensureSection(season,'CountedPerformances'));
   await Promise.all(jobs);
 }
 async function calculate(token){
   const choice=selectedRecord(),id=baseRecord(choice),scope=$('tf-season').value,
         seasons=scope==='__ALL__'?state.facade.stagioni.map(s=>s.stagione).sort():[scope], req=needed(id);
   let loaded=0;
   for(const season of seasons){
     if(token!==state.token)return;
     info('Caricamento '+(++loaded)+'/'+seasons.length+': '+seasonLabel(season)+' — '+recordTitle());
     await ensureSeason(season,req);
   }
   if(req.history||(id==='TF09'&&$('tf-event-aggregate').value!=='SEASON'))await ensureHistory();
   if(token!==state.token)return;
   const opts={id,context:$('tf-context').value,event:$('tf-event').value,keeperEvent:keeperEvent(choice),
     benchMetric:$('tf-bench-metric').value,metric:$('tf-team-metric').value,direction:$('tf-order').value,
     search:$('tf-search').value,thresholds:state.facade.soglie||{}};
   const index=seasons.map(season=>{
     const s=cacheData(season),meta=api.meta(season);
     return engine.indexedSeason({...s,dictionaries:meta.dizionari});
   });
   state.ranked=id==='TF09'?events.build(index,state.history||[],{
     group:$('tf-event-group').value,type:$('tf-event').value,
     aggregate:$('tf-event-aggregate').value,context:opts.context,
     direction:opts.direction,search:opts.search
   }):engine.build({seasons:index,history:state.history||[]},opts);
   render();info(id==='TF09'?'Eventi '+($('tf-event-group').value==='BONUS'?'positivi':'negativi')+
     ' · '+events.LABELS[$('tf-event').value]+' · '+($('tf-event-aggregate').selectedOptions[0].textContent)+
     ' · '+(opts.context==='real'?'FCA reale':'FCM conteggiato')+' · conteggi, non punti fantasy.':
     'Classifica '+recordTitle()+' — '+(scope==='__ALL__'?'assoluta':'stagione '+seasonLabel(scope))+'.'+
     (id==='TF14'||id==='TF15'?' Δ MV = media voto stagione − media ponderata delle altre stagioni; FM solo da dati reali verificati (— se assente).':''));
 }
 function error(err,token){if(token!==state.token)return;info('ERRORE: '+err.message);$('tf-count').textContent='Dati insufficienti: '+err.message;clear($('tf-body'));}
 function refresh(){const token=++state.token;syncControls();calculate(token).catch(e=>error(e,token));}
 function syncControls(){const id=baseRecord(selectedRecord());
   const context=['TF07','TF08','TF09','TF10','TF17','TF18','TF20'].includes(id);
   $('tf-context-wrap').hidden=!context;
   $('tf-event-wrap').hidden=id!=='TF09';$('tf-event-group-wrap').hidden=id!=='TF09';
   $('tf-event-aggregate-wrap').hidden=id!=='TF09';
   $('tf-team-wrap').hidden=id!=='TF16';$('tf-bench-wrap').hidden=id!=='TF13';
   const dir=$('tf-order'),choice=selectedRecord();if(state.selected!==choice){dir.value=['TF02','TF04','TF15'].includes(id)?'MIN':'MAX';state.selected=choice;}
 }
 function td(row,text){row.append(mk('td',fmt(text)));}
 function playerButton(row){const cell=mk('td'),b=mk('button',row.name||row.playerKey);b.type='button';b.className='rn-tf-player';
   b.addEventListener('click',()=>{const q=new URLSearchParams({playerKey:row.playerKey,season:row.season,back:location.href});location.href='calciatori.html?'+q.toString();});cell.append(b);return cell;}
 function badge(target,label,value){const b=mk('div');b.className='rn-tf-stat';b.append(mk('small',label),mk('strong',fmt(value)));target.append(b);}
 function rankingColumns(id,rows,choice){
   const keeper=id==='TF20',source=$('tf-context').value==='real'?'Voti':'Gare';
   const common=[['position','Pos.'],['player','Calciatore'],['season','Stagione']];
   const result={
     TF01:[['round','Giornata'],['value','Voto']],TF02:[['round','Giornata'],['value','Voto']],
     TF03:[['round','Giornata'],['team','Fantasquadra'],['competition','Competizione'],['value','Fantavoto']],
     TF04:[['round','Giornata'],['team','Fantasquadra'],['competition','Competizione'],['value','Fantavoto']],
     TF05:[['value','Media voto'],['sample','Voti validi']],
     TF06:[['team','Fantasquadra'],['competition','Competizione'],['value','Fantamedia'],['sample','Gare']],
     TF07:[['value','Gol'],['sample',source]],TF08:[['value','Assist'],['sample',source]],
     TF10:[['value',$('tf-context').value==='real'?'Giornate con voto':'Incontri conteggiati']],
     TF11:[['team','Fantasquadra'],['competition','Competizione'],['value','Media voto'],['sample','Campione']],
     TF12:[['team','Fantasquadra'],['competition','Competizione'],['value','Errore pesato'],['sample','Campione']],
     TF13:[['team','Fantasquadra'],['competition','Competizione'],['value','Fantavoto'],['sample','Campione']],
     // RN4 TF14/TF15 v116: show the values behind the anomaly, not only its delta.
     TF14:[['mediaStagione','MV stagione'],['baseline','MV altre stagioni'],['value','Δ MV'],
       ['fantamediaStagione','FM stagione'],['fantamediaBaseline','FM altre stagioni'],
       ['fantamediaDelta','Δ FM'],['sample','Voti stagione'],['baselineVotes','Voti altre']],
     TF15:[['mediaStagione','MV stagione'],['baseline','MV altre stagioni'],['value','Δ MV'],
       ['fantamediaStagione','FM stagione'],['fantamediaBaseline','FM altre stagioni'],
       ['fantamediaDelta','Δ FM'],['sample','Voti stagione'],['baselineVotes','Voti altre']],
     TF16:[['team','Fantasquadra'],['competition','Competizione'],['value','Valore'],['sample','Gare']],
     TF17:[['value','Rigori parati'],['sample',source]],
     TF18:[['value','Gol subiti'],['sample',source]],
     TF19:[['value','Giornate senza gol subiti'],['sample','Voti']],
     TF20:choice==='TF20_ASSIST'?[['value','Assist'],['sample',source]]:[['value','Gol segnati'],['golSuRigore','di cui su rigore'],['sample',source]]
   };
   const columns=[...common,...(result[id]||[['value','Valore'],['sample','Campione']])];
   if(keeper)columns[1]=['player','Portiere'];
   const isSet=v=>v!==null&&v!==undefined&&v!=='';
   const value=(r,k)=>k==='round'?r.round:k==='golSuRigore'?r.golSuRigore:r[k];
   // Suppress genuinely unpopulated columns; zero is a real recorded value, not missing.
   const visible=columns.filter(([key])=>key==='position'||key==='player'||key==='season'||key==='value'||
      (key==='golSuRigore'?rows.some(r=>Number(r.golSuRigore)>0):rows.some(r=>isSet(value(r,key)))));
   if(rows.some(r=>r.url))visible.push(['url','Tabellino']);
   return visible;
 }
 function render(){const rows=state.ranked,size=$('tf-page-size').value,choice=selectedRecord(),id=baseRecord(choice);
   if(id==='TF09'){renderEventRanking(rows,size);return;}
   const count=size==='all'?rows.length:Math.min(rows.length,Number(size)),isKeeper=id==='TF20';
   clear($('tf-head'));clear($('tf-body'));clear($('tf-metrics'));
   const columns=rankingColumns(id,rows,choice),header=mk('tr');
   columns.forEach(c=>header.append(mk('th',c[1])));$('tf-head').append(header);
   badge($('tf-metrics'),'Risultati',rows.length);
   badge($('tf-metrics'),'Periodo',$('tf-season').value==='__ALL__'?'Assoluta':seasonLabel($('tf-season').value));
   badge($('tf-metrics'),'Ordinamento',$('tf-order').value==='MAX'?'Valore maggiore':'Valore minore');
   badge($('tf-metrics'),'Fonte',$('tf-context').value==='real'?'FCA reale':'FCM COUNTED');
   const emptyMessage=choice==='TF20_GOL'?'Nessun portiere con gol segnati nel periodo selezionato.':
      choice==='TF20_ASSIST'?'Nessun portiere con assist nel periodo selezionato.':'Nessun risultato per i filtri selezionati.';
   if(rows.length===0){const tr=mk('tr'),cell=mk('td',emptyMessage);cell.colSpan=columns.length;cell.className='rn-tf-empty';tr.append(cell);$('tf-body').append(tr);$('tf-count').textContent=emptyMessage;}
   const ticket=++state.drawToken;
   if(state.renderObserver){state.renderObserver.disconnect();state.renderObserver=null;}
   const footer=$('tf-count').parentElement,prior=doc.getElementById('tf-more-sentinel');if(prior)prior.remove();
   let shown=0;
   const addValue=(tr,r,key)=>{
      if(key==='position'){td(tr,r.position);return;}
      if(key==='player'){tr.append(playerButton(r));return;}
      if(key==='season'){td(tr,seasonLabel(r.season));return;}
      if(key==='url'){
        const cell=mk('td');if(r.url&&/^(https?:\/\/|\.\.?\/)/i.test(r.url)){
           const a=mk('a','Apri');a.href=r.url;a.target='_blank';a.rel='noopener noreferrer';cell.append(a);
        }else cell.textContent='—';tr.append(cell);return;
      }
      const cell=mk('td',fmt(r[key]));if(key==='value')cell.className='rn-tf-value';tr.append(cell);
   };
   function batch(){if(ticket!==state.drawToken||shown>=count)return;
     const frag=doc.createDocumentFragment(),end=Math.min(count,shown+200);
     for(let i=shown;i<end;i++){
       const row=mk('tr');columns.forEach(([key])=>addValue(row,rows[i],key));frag.append(row);
     }
     $('tf-body').append(frag);shown=end;
     $('tf-count').textContent=rows.length===0?emptyMessage:
       'Visualizzati '+shown.toLocaleString('it-IT')+' / '+rows.length.toLocaleString('it-IT')+' risultati';
     if(shown>=count&&state.renderObserver){state.renderObserver.disconnect();state.renderObserver=null;}
   }
   if(size==='all'&&count>200&&root.IntersectionObserver){
     const sentinel=mk('span');sentinel.id='tf-more-sentinel';sentinel.setAttribute('aria-hidden','true');
     sentinel.style.display='block';sentinel.style.height='1px';footer.append(sentinel);
     state.renderObserver=new root.IntersectionObserver(entries=>{
       if(ticket===state.drawToken&&entries.some(e=>e.isIntersecting)&&shown<count)batch();
     },{rootMargin:'500px'});batch();if(shown<count)state.renderObserver.observe(sentinel);
   }else{batch();if(shown<count){function next(){if(ticket!==state.drawToken)return;batch();
       if(shown<count)(root.requestAnimationFrame||root.setTimeout)(next,0);}
       (root.requestAnimationFrame||root.setTimeout)(next,0);
   }}
 }
 function renderEventRanking(rows,size){
   const group=$('tf-event-group').value,kind=$('tf-event').value,mode=$('tf-event-aggregate').value,
     src=$('tf-context').value,parts=events.GROUPS[group];
   const count=size==='all'?rows.length:Math.min(rows.length,Number(size));
   const ticket=++state.drawToken;
   if(state.renderObserver){state.renderObserver.disconnect();state.renderObserver=null;}
   const sentinel=doc.getElementById('tf-more-sentinel');if(sentinel)sentinel.remove();
   clear($('tf-head'));clear($('tf-body'));clear($('tf-metrics'));
   const label=events.LABELS[kind],sample=src==='real'?'Voti reali validi':'Apporti COUNTED';
   badge($('tf-metrics'),'Voci nella classifica',rows.length);
   badge($('tf-metrics'),'Tipo',label);
   badge($('tf-metrics'),'Aggregazione',$('tf-event-aggregate').selectedOptions[0].textContent);
   badge($('tf-metrics'),'Fonte',src==='real'?'FCA reale':'FCM conteggiato');
   const head=['Pos.','Calciatore','Periodo','Stagioni',sample,
     'Totale ('+label+')','Media annua','Picco annuo',
     src==='real'?'Per 100 voti':'Per 100 apporti','Composizione e annate'];
   const th=mk('tr');head.forEach(v=>th.append(mk('th',v)));$('tf-head').append(th);
   let shown=0;
   const fragmentSize=250;
   function batch(){if(ticket!==state.drawToken||shown>=count)return;
     const frag=doc.createDocumentFragment(),end=Math.min(count,shown+fragmentSize);
     for(let i=shown;i<end;i++){
       const r=rows[i],tr=mk('tr');td(tr,r.position);tr.append(playerButton(r));
       td(tr,r.seasonText);td(tr,r.years);td(tr,r.sample);
       const totalCell=mk('td',fmt(r.amount)),avgCell=mk('td',fmt(r.averagePerSeason)),
         rateCell=mk('td',fmt(r.per100));
       if(mode==='SEASON'||mode==='CAREER')totalCell.className='rn-tf-value-active';
       else if(mode==='AVERAGE')avgCell.className='rn-tf-value-active';
       else rateCell.className='rn-tf-value-active';
       tr.append(totalCell,avgCell);
       const peak=Math.max(...r.yearly.map(y=>kind==='TOTAL_'+group?
         parts.reduce((n,k)=>n+y.parts[k],0):y.parts[kind]));
       td(tr,peak);tr.append(rateCell);
       const cell=mk('td');const d=mk('details');d.className='rn-tf-drilldown';
       d.append(mk('summary',r.yearly.length>1?'Composizione e '+r.yearly.length+' anni':'Composizione'));
       const wrap=mk('div');wrap.className='rn-table-wrap';const table=mk('table');table.className='rn-table rn-tf-competition-table';
       const header=mk('tr');['Stagione',sample,events.LABELS[kind],...parts.map(k=>events.LABELS[k])].forEach(v=>header.append(mk('th',v)));
       const thead=mk('thead');thead.append(header);table.append(thead);const body=mk('tbody');
       r.yearly.forEach(y=>{const rr=mk('tr');
         [seasonLabel(y.season),y.sample,kind==='TOTAL_'+group?parts.reduce((n,k)=>n+y.parts[k],0):y.parts[kind],
           ...parts.map(k=>y.parts[k])].forEach(v=>td(rr,v));body.append(rr);});
       table.append(body);wrap.append(table);d.append(wrap);cell.append(d);
       tr.append(cell);frag.append(tr);
     }
     $('tf-body').append(frag);shown=end;
     $('tf-count').textContent='Visualizzati '+shown.toLocaleString('it-IT')+' / '+rows.length.toLocaleString('it-IT')+' risultati';
     if(shown>=count&&state.renderObserver){state.renderObserver.disconnect();state.renderObserver=null;}
   }
   if(size==='all'&&count>250&&root.IntersectionObserver){
     const foot=$('tf-count').parentElement,s=mk('span');s.id='tf-more-sentinel';s.style.display='block';s.style.height='1px';foot.append(s);
     state.renderObserver=new root.IntersectionObserver(entries=>{
       if(ticket===state.drawToken&&entries.some(e=>e.isIntersecting)&&shown<count)batch();
     },{rootMargin:'500px'});batch();if(shown<count)state.renderObserver.observe(s);
   }else{batch();if(shown<count){function next(){if(ticket!==state.drawToken)return;batch();
     if(shown<count)(root.requestAnimationFrame||root.setTimeout)(next,0);}
     (root.requestAnimationFrame||root.setTimeout)(next,0);}}
 }
 function updateEventTypes(){const family=$('tf-event-group').value,select=$('tf-event');
   const old=select.value;clear(select);
   events.TYPES[family].forEach(k=>option(select,k,events.LABELS[k]));
   select.value=events.TYPES[family].includes(old)?old:family==='MALUS'?'AMM':'TOTAL_BONUS';
 }
 function table(target,headers,rows){const wrap=mk('div'),t=mk('table');wrap.className='rn-table-wrap';t.className='rn-table';const thead=mk('thead'),th=mk('tr');headers.forEach(x=>th.append(mk('th',x)));thead.append(th);t.append(thead);
   const tbody=mk('tbody');rows.forEach(cols=>{const tr=mk('tr');cols.forEach(x=>td(tr,x));tbody.append(tr);});t.append(tbody);wrap.append(t);target.append(wrap);}
 function statBox(target,pairs){const el=mk('div');el.className='rn-tf-profile-badges';pairs.forEach(pair=>badge(el,pair[0],pair[1]));target.append(el);}
 async function ensureFMTManifest(){
   if(!state.fmtManifestPromise)state.fmtManifestPromise=loadScript('../js/fcmRecordsNext_TopFlopFcaFmt_v83.js')
     .then(()=>{const manifest=root.fcmRecordsNextFcaFmtManifestV70;
       if(!manifest||manifest.schemaVersion!=='4.0-real-fmt-v70')throw Error('Manifest fantamedia FCA v70 non valido');
       return manifest;
     }).catch(e=>{state.fmtManifestPromise=null;throw e;});
   return state.fmtManifestPromise;
 }
 async function loadRealFMT(years){
   const manifest=await ensureFMTManifest(),available=new Map(manifest.stagioni.map(x=>[x.stagione,x]));
   const unique=[...new Set(years.map(x=>x.stagione))];
   await Promise.all(unique.map(async season=>{
     if(root.fcmRecordsNextFcaFmtV70&&root.fcmRecordsNextFcaFmtV70[season])return;
     const meta=available.get(season);if(!meta)throw Error('Fantamedia FCA non esportata per '+seasonLabel(season));
     if(!state.fmtPending.has(season))state.fmtPending.set(season,loadScript('../js/'+meta.nome)
       .catch(e=>{state.fmtPending.delete(season);throw e;}));
     await state.fmtPending.get(season);
     if(!root.fcmRecordsNextFcaFmtV70||!root.fcmRecordsNextFcaFmtV70[season])
       throw Error('Dati FCA fantamedia mancanti: '+season);
   }));
   return root.fcmRecordsNextFcaFmtV70||{};
 }
 function profileTable(target,headers,rows){table(target,headers,rows);}
 function sortableTeamTable(target,original){
   const wrap=mk('div');wrap.className='rn-table-wrap';target.append(wrap);
   const sort={key:'stagione',dir:-1};
   const cols=[['stagione','Stagione'],['fantasquadra','Fantasquadra'],['gare','Gare'],
     ['mediaVoto','Media voto'],['fantamedia','Fantamedia'],['gol','Gol'],['assist','Assist'],
     ['golSuRigore','Gol rigore'],['rigoriSbagliati','Rigori sb.'],['ammonizioni','Amm.'],
     ['espulsioni','Esp.'],['autogol','Autogol'],['golSubiti','Gol subiti'],
     ['golSubitiSuRigore','Gol subiti rig.'],['rigParati','Rigori parati'],
     ['bonusPunti','Bonus punti'],['malusPunti','Malus punti'],['saldoPunti','Saldo'],['riconciliazione','Verifica'],
     ['deltaNetTabellino','Delta TOT−VOTO'],['numeroCompetizioni','Comp.']];
   function render(){clear(wrap);const tbl=mk('table');tbl.className='rn-table rn-tf-squad-summary';
     const thead=mk('thead'),thr=mk('tr');
     for(const [key,caption] of cols){const th=mk('th'),button=mk('button',caption+(sort.key===key?(sort.dir===1?' ▲':' ▼'):''));
       button.type='button';button.className='rn-tf-sort';
       button.addEventListener('click',()=>{if(sort.key===key)sort.dir*=-1;
         else {sort.key=key;sort.dir=['stagione','fantasquadra'].includes(key)?1:-1;}render();});
       th.setAttribute('aria-sort',sort.key===key?(sort.dir===1?'ascending':'descending'):'none');th.append(button);thr.append(th);}
     thr.append(mk('th','Dettaglio'));thead.append(thr);tbl.append(thead);const body=mk('tbody');
     const rows=original.slice().sort((a,b)=>{
       const x=a[sort.key],y=b[sort.key];if(x===null&&y===null)return 0;
       if(x===null)return 1;if(y===null)return -1;
       const dif=typeof x==='number'&&typeof y==='number'?x-y:String(x).localeCompare(String(y),'it');
       return sort.dir*dif||b.stagione.localeCompare(a.stagione)||a.fantasquadra.localeCompare(b.fantasquadra,'it');
     });
     for(const r of rows){const tr=mk('tr');for(const [key] of cols)
       td(tr,key==='stagione'?seasonLabel(r.stagione):r[key]);
       const cell=mk('td'),details=mk('details');details.className='rn-tf-drilldown';
       details.append(mk('summary',r.numeroCompetizioni+' competizion'+(r.numeroCompetizioni===1?'e':'i')));
       const nested=mk('div');nested.className='rn-table-wrap';const inner=mk('table');inner.className='rn-table rn-tf-competition-table';
       const row=mk('tr');const competitionCols=[['competizione','Competizione'],...cols.slice(2,-1)];
       competitionCols.forEach(([k,label])=>row.append(mk('th',label)));
       const head=mk('thead');head.append(row);inner.append(head);const tbody=mk('tbody');
       r.competizioni.forEach(c=>{const cr=mk('tr');competitionCols.forEach(([key])=>td(cr,c[key]));tbody.append(cr);});
       inner.append(tbody);nested.append(inner);details.append(nested);cell.append(details);tr.append(cell);body.append(tr);
     }tbl.append(body);wrap.append(tbl);
   }render();
 }
 function teamStat(t,metric,unit){if(!t)return '—';
   return t.fantasquadra+' · '+fmt(t[metric])+(unit||'');}
 async function loadScorecards(years){
   if(!state.pointsManifestPromise)state.pointsManifestPromise=loadScript('../js/fcmRecordsNext_ProfileManifest_v83.js')
     .then(()=>{const m=root.RecordsNextV79Manifest;if(!m||m.schemaVersion!=='4.0-profile-v79')
       throw Error('Manifest scorecard v79 errato');return m;});
   const manifest=await state.pointsManifestPromise,bySeason=new Map();
   for(const f of manifest.points){if(!bySeason.has(f.stagione))bySeason.set(f.stagione,[]);
     bySeason.get(f.stagione).push(f);}
   const wanted=[...new Set(years.map(x=>x.stagione))];
   await Promise.all(wanted.map(async season=>{
     const files=bySeason.get(season);if(!files||files.length===0)
       throw Error('Manca il tabellino v79 '+season);
     const count=files.reduce((n,f)=>n+f.righe,0);
     if(root.RecordsNextV79Points&&root.RecordsNextV79Points[season]?.righe?.length===count)return;
     if(!state.pointsPending.has(season))state.pointsPending.set(season,(async()=>{
       for(const f of files.sort((a,b)=>a.part-b.part))await loadScript('../js/'+f.nome);
       const actual=root.RecordsNextV79Points?.[season]?.righe||[];
       if(actual.length!==count)throw Error('Scorecard v79 incompleto '+season);
     })());
     try{await state.pointsPending.get(season);}catch(e){state.pointsPending.delete(season);throw e;}
   }));
   return wanted.flatMap(s=>((root.RecordsNextV79Points||{})[s]||{}).righe||[]);
 }
 async function enableGlobalSearch(){
   const query=$('tf-global-search'),target=$('tf-global-results');if(!query||!target)return;
   const global=root.RecordsNextGlobalSearchV79;if(!global)throw Error('Motore ricerca globale v79 assente');
   query.addEventListener('input',async()=>{
     const needle=query.value.trim();if(needle.length<2){clear(target);return;}
     target.textContent='Ricerca nell’archivio storico…';
     try{
       if(!state.globalPending)state.globalPending=global.loadAll(async name=>loadScript('../js/'+name),
         ()=>root.RecordsNextV79Manifest,()=>root.RecordsNextV79Index);
       const index=await state.globalPending;
       if(query.value.trim()!==needle)return;
       const found=global.search(index,needle,40);clear(target);
       target.append(mk('p',found.length+' risultati (identità approvate oppure stagionali separate).'));
       for(const item of found){const b=mk('button',item.name+' · '+item.start+'–'+item.end+
         (item.approved?' · identità approvata':' · identità stagionale'));
         b.type='button';b.className='rn-tf-global-option';b.addEventListener('click',()=>openProfile(item.playerKey,item.season));
         target.append(b);}
     }catch(e){target.textContent='Errore ricerca globale: '+e.message;state.globalPending=null;}
   });
 }
 async function openProfile(playerKey,season){const ticket=++state.profileToken,target=detail();
   target.hidden=false;clear(target);target.append(mk('p','Caricamento scheda giocatore…'));
   try{
     await ensureSection(season,'Careers');await ensureSection(season,'Teams');const ss=cacheData(season);
     const individual=ss.careers.find(p=>p.playerKey===playerKey);
     if(!individual)throw Error('Calciatore non trovato in '+seasonLabel(season));
     const history=await ensureHistory();
     const approved=history.find(h=>(h.stagioni||[]).some(s=>s.playerKey===playerKey&&s.stagione===season))||null;
     const years=approved?approved.stagioni:[{stagione:season,playerKey}];
     const [official,allContexts,scoreRows]=await Promise.all([
       loadRealFMT(years),
       Promise.all(years.map(async y=>{
         await ensureSection(y.stagione,'Teams');
         return profile.seasonTeamContexts(cacheData(y.stagione).teams,y.stagione,y.playerKey);
       })).then(items=>items.flat()),
       loadScorecards(years)
     ]);
     if(approved){
       const expected=approved.fantasquadrePerStagione||[];
       // The History facade must enumerate precisely the contexts that are now
       // being enriched from actual per-season FCM Teams shards.
       const ctxKey=x=>JSON.stringify([x.stagione,x.playerKey,Number(x.idSquadraInterno),Number(x.idCompetizioneInterno)]);
       const byKey=new Map(allContexts.map(c=>[ctxKey(c),c]));
       if(byKey.size!==allContexts.length||expected.length!==allContexts.length)
         throw Error('Storico FCM e archivio stagionale incoerenti: ripubblicare History');
       for(const c of expected){const actual=byKey.get(ctxKey(c));
         if(!actual||Number(c.gareConteggiate)!==actual.gareConteggiate||
           Number(c.gareConVotoReale)!==actual.gareConVotoReale)
           throw Error('Context storico FCM non riconciliato: '+ctxKey(c));
       }
     }
     if(ticket!==state.profileToken)return;
     const data=profile.composeProfile(approved,individual,season,ss.teams,official,allContexts,scoreRows);
     target.dataset.identity=approved?'approvata':'stagionale';
     clear(target);const close=mk('button','Chiudi scheda');close.className='rn-button rn-tf-close';close.type='button';
     close.addEventListener('click',()=>{++state.profileToken;target.hidden=true;});target.append(close);
     target.append(mk('h2',data.name));target.append(mk('p',data.approved?
       'Identità storica approvata · '+data.years.length+' stagioni collegate':
       'Identità stagionale · collegamento storico non approvato'));
     const events=data.events||{};
     target.append(mk('h3','Carriera reale — FCA'));
     statBox(target,[['Stagioni',data.years.length],['Voti validi',data.voti],['Media voto reale',data.mediaVoto],
       ['Fantamedia reale',data.fmt.media],['Gol reali',events.GOLFATTI1||0],['Assist reali',events.ASSIST||0],
       ['Ammonizioni',events.AMM||0],['Espulsioni',events.ESP||0],
       ...(data.isGoalkeeper?[['Gol subiti',events.GOLSUBITI||0],['Rigori parati',events.RIGPAR||0]]:[])]);
     const quality=mk('p');quality.className='rn-tf-source';quality.textContent=data.fmt.missing===0?
       'Fantamedia reale: media delle FMT stagionali verificate, ponderata sui voti validi. Fonte FCA.':
       'Fantamedia reale incompleta: '+fmt(data.fmt.covered)+' voti coperti su '+
         fmt(data.fmt.covered+data.fmt.missing)+'; valore complessivo non calcolato.';
     target.append(quality);
     const bestYear=data.years.filter(x=>engine.num(x.mediaVoto)!==null)
       .sort((a,b)=>Number(b.mediaVoto)-Number(a.mediaVoto))[0];
     if(bestYear)target.append(mk('p','Migliore media voto stagionale: '+seasonLabel(bestYear.stagione)+
       ' — '+fmt(bestYear.mediaVoto)+' ('+fmt(bestYear.votiValidi)+' voti).'));
     profileTable(target,['Stagione','Voti','Media voto','Fantamedia reale','Gol','Assist',
         'Ammonizioni','Espulsioni',...(data.isGoalkeeper?['Gol subiti','Rigori parati']:[])],
       data.years.map(y=>[seasonLabel(y.stagione),y.votiValidi,y.mediaVoto,y.fantamediaReale,
         (y.eventi||{}).GOLFATTI1||0,(y.eventi||{}).ASSIST||0,(y.eventi||{}).AMM||0,(y.eventi||{}).ESP||0,
         ...(data.isGoalkeeper?[(y.eventi||{}).GOLSUBITI||0,(y.eventi||{}).RIGPAR||0]:[])]));
     target.append(mk('h3','Carriera nella lega — fantasquadre'));
     const b=data.best;
     statBox(target,[['Più stagioni documentate',b.mostSeasons?
       b.mostSeasons.fantasquadra+' · '+b.mostSeasons.stagioni:'—'],
       ['Più gare utilizzate',teamStat(b.mostGames,'gare',' gare')],
       ['Fantamedia massima',teamStat(b.max,'fantamedia',' ('+(b.max?b.max.gare:0)+' gare)')],
       ['Fantamedia minima',teamStat(b.min,'fantamedia',' ('+(b.min?b.min.gare:0)+' gare)')],
       ['Saldo annuale massimo',b.saldoMax?seasonLabel(b.saldoMax.stagione)+' · '+b.saldoMax.fantasquadra+' · '+fmt(b.saldoMax.saldoPunti)+' punti ('+b.saldoMax.gare+' gare)':'—'],
       ['Saldo annuale minimo',b.saldoMin?seasonLabel(b.saldoMin.stagione)+' · '+b.saldoMin.fantasquadra+' · '+fmt(b.saldoMin.saldoPunti)+' punti ('+b.saldoMin.gare+' gare)':'—']]);
     const hint=mk('p','Confronto dei rendimenti: almeno '+b.minGare+
       ' gare utilizzate per fantasquadra. I badge pluriennali raggruppano i nomi FCM identici; ' +
       'le righe della tabella mantengono separati stagione e ID della fantasquadra.');
     hint.className='rn-tf-source';target.append(hint);
     target.append(mk('p','Una riga per stagione e fantasquadra: tutte le competizioni, ' +
       'medie ponderate ed eventi effettivamente apportati. Il SALDO è la somma del MODIF FCM ' +
       '(sempre controllato con TOT−VOTO); bonus e malus sono separati solo se tutti ' +
       'gli eventi ricostruiscono il MODIF senza ambiguità, usando i coefficienti confermati. ' +
       'La verifica OK significa riconciliazione del tabellino, non certificazione di un ' +
       'regolamento scritto. Clic sulle colonne per ordinare.'));
     sortableTeamTable(target,data.seasonSummary);
     if(approved&&(approved.anomalieStagionali||[]).length){target.append(mk('h3','Primati e stagioni eccezionali'));
       profileTable(target,['Tipo','Stagione','Scostamento'],approved.anomalieStagionali.map(a=>
         [a.recordId,seasonLabel(a.stagione),a.delta]));}
     target.scrollIntoView({block:'start',behavior:'smooth'});
   }catch(e){if(ticket===state.profileToken)target.textContent='Errore scheda giocatore: '+e.message;}
 }
 async function bootstrap(){if(!api||!engine||!profile||!events){info('ERRORE: motore Top & Flop v72 incompleto');return;}
   let skin='mauzstrom';try{skin=root.sessionStorage.getItem('rn4-recordsnext-skin')||skin;}catch(e){}
   if(!themes.includes(skin))skin='mauzstrom';$('tf-skin').value=skin;$('rn-tf-skin').href='../profiles/'+skin+'/recordsnext.css';doc.body.dataset.rnSkin=skin;
   $('tf-skin').addEventListener('change',function(){const v=themes.includes(this.value)?this.value:'mauzstrom';$('rn-tf-skin').href='../profiles/'+v+'/recordsnext.css';doc.body.dataset.rnSkin=v;try{root.sessionStorage.setItem('rn4-recordsnext-skin',v);}catch(e){}});
   engine.KEYS.filter(k=>k!=='TF20').forEach(k=>option($('tf-record'),k,engine.LABELS[k]+' ('+k+')'));
   option($('tf-record'),'TF20_GOL','Gol dei portieri (TF20-G)');
   option($('tf-record'),'TF20_ASSIST','Assist dei portieri (TF20-A)');
   try{
     state.facade=await api.loadFacade('../js');
     await loadScript('../js/fcmRecordsNext_TopFlopFull_v83.js');state.manifest=root.fcmRecordsNextTopFlopFullV69;
     if(!state.manifest||state.manifest.schemaVersion!=='4.0-full-performances-v69')throw Error('Manifest prestazioni v69 assente/incompatibile');
     const allowed=new Set(state.facade.stagioni.map(x=>x.stagione));
     const declared=new Set(state.manifest.stagioni.map(x=>x.stagione));
     for(const s of allowed)if(!declared.has(s))throw Error('Manca la stagione nel manifest v69: '+s);
     option($('tf-season'),'__ALL__','Assoluta — tutte le stagioni');
     [...allowed].sort().reverse().forEach(s=>option($('tf-season'),s,seasonLabel(s)));
     state.season=[...allowed].sort().reverse()[0];$('tf-season').value=state.season;$('tf-record').value=state.selected;
     updateEventTypes();
     ['tf-record','tf-order','tf-context','tf-event','tf-team-metric','tf-bench-metric'].forEach(id=>$(id).addEventListener('change',refresh));
     $('tf-event-group').addEventListener('change',()=>{updateEventTypes();refresh();});
     $('tf-event-aggregate').addEventListener('change',()=>{
       if($('tf-event-aggregate').value!=='SEASON')$('tf-season').value='__ALL__';
       refresh();
     });
     $('tf-season').addEventListener('change',()=>{
       if($('tf-season').value!=='__ALL__')$('tf-event-aggregate').value='SEASON';
       refresh();
     });
     $('tf-search').addEventListener('input',()=>{
       // Use cached facts; recompute ranking without an unnecessary network reload.
       refresh();
     });
     $('tf-page-size').addEventListener('change',render);
     refresh();
   }catch(e){info('ERRORE: '+e.message);}
 }
 if(doc.readyState==='loading')doc.addEventListener('DOMContentLoaded',bootstrap);else bootstrap();
})(window,document);
