/* RecordsNext 4.0 - Archivio calciatori v83. The profile arithmetic is shared with v79. */
(function(w,d){'use strict';
 const api=w.RecordsNextTopFlopViewsV2,profile=w.RecordsNextPlayerProfileV79,search=w.RecordsNextGlobalSearchV79;
 const $=id=>d.getElementById(id),element=(tag,value)=>{const n=d.createElement(tag);if(value!==undefined)n.textContent=String(value);return n;};
 const state={facade:null,history:null,manifest:null,cache:new Map(),busy:new Map(),points:new Map(),fmt:new Map(),index:null,token:0};
 const seasonLabel=id=>String(id).replace('_','/');
 const fmt=v=>v===null||v===undefined||v===''?'—':typeof v==='number'?v.toLocaleString('it-IT',{maximumFractionDigits:3}):String(v);
 const load=name=>new Promise((ok,fail)=>{const e=element('script');e.async=true;e.src='../js/'+name;e.onload=ok;e.onerror=()=>fail(Error('Impossibile caricare '+name));d.head.append(e);});
 const put=(parent,tag,value)=>{const el=element(tag,value);parent.append(el);return el;};
 const seasonAllowed=s=>!!state.facade?.stagioni.some(x=>x.stagione===s);
 function badge(parent,label,value){const n=put(parent,'div');n.className='rn-tf-stat';put(n,'small',label);put(n,'strong',fmt(value));}
 async function section(season,kind){
   if(!seasonAllowed(season))throw Error('Stagione oltre il cutoff del sito: '+season);
   const key=season+'|'+kind;if(state.cache.has(key))return state.cache.get(key);
   if(!state.busy.has(key))state.busy.set(key,api.loadView('../js',season,kind).then(x=>{state.cache.set(key,x);return x;}).finally(()=>state.busy.delete(key)));
   return state.busy.get(key);
 }
 async function ensureProfileManifest(){
   if(state.manifest)return;
   await load('fcmRecordsNext_ProfileManifest_v83.js');state.manifest=w.RecordsNextV79Manifest;
   if(!state.manifest||state.manifest.schemaVersion!=='4.0-profile-v79')throw Error('Manifest profili non valido');
   const ids=new Set(state.facade.stagioni.map(s=>s.stagione));
   if(state.manifest.seasons.length!==ids.size||state.manifest.seasons.some(s=>!ids.has(s)))throw Error('Cutoff manifest profili incoerente');
 }
 async function points(years){
   await ensureProfileManifest();
   const answer=[];
   for(const s of new Set(years.map(y=>y.stagione))){
     if(!seasonAllowed(s))throw Error('Cutoff oltrepassato');
     const files=state.manifest.points.filter(x=>x.stagione===s).sort((a,b)=>a.part-b.part);
     if(!files.length)throw Error('Dati punteggi assenti: '+s);
     if(!state.points.has(s)){
       for(const file of files)await load(file.nome);
       const rows=w.RecordsNextV79Points?.[s]?.righe;
       if(!rows||rows.length!==files.reduce((n,x)=>n+x.righe,0))throw Error('Punteggi incompleti '+s);
       state.points.set(s,rows);
     }
     answer.push(...state.points.get(s));
   }
   return answer;
 }
 async function official(years){
   if(!state.fmtManifest){await load('fcmRecordsNext_TopFlopFcaFmt_v83.js');state.fmtManifest=w.fcmRecordsNextFcaFmtManifestV70;
      if(!state.fmtManifest||state.fmtManifest.schemaVersion!=='4.0-real-fmt-v70')throw Error('Fantamedia FCA manifest non valido');}
   for(const s of new Set(years.map(y=>y.stagione))){
     if(!state.fmt.has(s)){const row=state.fmtManifest.stagioni.find(x=>x.stagione===s);
       if(!row)throw Error('Fonte FMT mancante '+s);await load(row.nome);
       if(!w.fcmRecordsNextFcaFmtV70?.[s])throw Error('FMT incompleta '+s);
       state.fmt.set(s,w.fcmRecordsNextFcaFmtV70[s]);}
   }
   return Object.fromEntries([...state.fmt]);
 }
 async function loadIndex(){
   if(state.index)return state.index;
   await ensureProfileManifest();
   state.index=await search.loadAll(name=>load(name),()=>state.manifest,()=>w.RecordsNextV79Index);
   return state.index;
 }
 function showError(e){$('rn-calc-status').textContent='Errore: '+e.message;}
 const cols=[['stagione','Stagione'],['fantasquadra','Fantasquadra'],['gare','Gare utilizzate'],
   ['fantamedia','Fantamedia'],['mediaVoto','Media voto'],['gol','Gol'],['assist','Assist'],
   ['golSuRigore','Gol su rigore'],['rigoriSbagliati','Rigori sbagliati'],['ammonizioni','Ammonizioni'],
   ['espulsioni','Espulsioni'],['autogol','Autogol'],['golSubiti','Gol subiti'],
   ['golSubitiSuRigore','Gol subiti su rigore'],['rigParati','Rigori parati'],
   ['bonusPunti','Bonus (punti)'],['malusPunti','Malus (punti)'],['saldoPunti','Saldo (punti)'],
   ['riconciliazione','Verifica']];
 function cell(tr,value){put(tr,'td',fmt(value));}
 function statTable(parent,rows,columns){
   const wrap=put(parent,'div');wrap.className='rn-table-wrap';
   const tab=put(wrap,'table');tab.className='rn-table rn-tf-squad-summary';
   const head=put(tab,'thead'),hr=put(head,'tr');for(const [,name] of columns)put(hr,'th',name);
   const body=put(tab,'tbody');for(const row of rows){const tr=put(body,'tr');for(const [key] of columns)cell(tr,key==='stagione'?seasonLabel(row[key]):row[key]);}
   return {tab,body,head};
 }
 function teamRows(target,rows){
   const root=put(target,'div'),sort={key:'stagione',dir:-1};
   const render=()=>{
     root.replaceChildren();
     const ordered=rows.slice().sort((a,b)=>{
       const v=a[sort.key],u=b[sort.key];if(v==null&&u==null)return 0;if(v==null)return 1;if(u==null)return -1;
       const diff=typeof v==='number'&&typeof u==='number'?v-u:String(v).localeCompare(String(u),'it');
       return sort.dir*diff||b.stagione.localeCompare(a.stagione);
     });
     const {tab,body,head}=statTable(root,ordered,cols);
     const hr=head.firstElementChild;for(let i=0;i<cols.length;i++){
       const th=hr.children[i];th.replaceChildren();const [key,label]=cols[i];
       const btn=put(th,'button',label+(sort.key===key?(sort.dir>0?' ▲':' ▼'):''));btn.type='button';
       btn.addEventListener('click',()=>{if(sort.key===key)sort.dir*=-1;else{sort.key=key;sort.dir=['stagione','fantasquadra'].includes(key)?1:-1;}render();});
     }
     put(hr,'th','Competizioni');
     ordered.forEach((row,i)=>{
       const tr=body.children[i],td=put(tr,'td'),detail=put(td,'details'),summary=put(detail,'summary',row.numeroCompetizioni+' competizioni');
       summary.title='Indicatori suddivisi per competizione';
       statTable(detail,row.competizioni,[['competizione','Competizione'],...cols.slice(2)]);
     });
   };render();
 }
 function renderPlayer(data,playerKey,season){
   const target=$('rn-calc-detail');target.replaceChildren();target.hidden=false;
   put(target,'h2',data.name);put(target,'p',data.approved?'Identità storica approvata · '+data.years.length+' stagioni':'Identità stagionale (nessuna fusione implicita)');
   const real=put(target,'div');real.className='rn-tf-profile-badges';
   badge(real,'Voti reali',data.voti);badge(real,'Media voto reale',data.mediaVoto);
   badge(real,'Fantamedia FCA',data.fmt.media);
   badge(real,'Gol reali',data.events?.GOLFATTI1||0);badge(real,'Assist reali',data.events?.ASSIST||0);
   const years=data.years.map(y=>({stagione:y.stagione,voti:y.votiValidi,media:y.mediaVoto,fantamedia:y.fantamediaReale,
     gol:y.eventi?.GOLFATTI1||0,assist:y.eventi?.ASSIST||0}));
   put(target,'h3','Carriera reale FCA');
   statTable(target,years,[['stagione','Stagione'],['voti','Voti validi'],['media','Media voto'],['fantamedia','Fantamedia reale'],['gol','Gol'],['assist','Assist']]);
   put(target,'h3','Carriera nelle fantasquadre FCM');
   const b=data.best,summary=put(target,'div');summary.className='rn-tf-profile-badges';
   badge(summary,'Più gare utilizzate',b.mostGames?b.mostGames.fantasquadra+' · '+b.mostGames.gare:'—');
   badge(summary,'Fantamedia massima',b.max?b.max.fantasquadra+' · '+fmt(b.max.fantamedia)+' ('+b.max.gare+' gare)':'—');
   badge(summary,'Fantamedia minima',b.min?b.min.fantasquadra+' · '+fmt(b.min.fantamedia)+' ('+b.min.gare+' gare)':'—');
   badge(summary,'Saldo annuale massimo',b.saldoMax?seasonLabel(b.saldoMax.stagione)+' · '+b.saldoMax.fantasquadra+' · '+fmt(b.saldoMax.saldoPunti)+' punti ('+b.saldoMax.gare+' gare)':'—');
   badge(summary,'Saldo annuale minimo',b.saldoMin?seasonLabel(b.saldoMin.stagione)+' · '+b.saldoMin.fantasquadra+' · '+fmt(b.saldoMin.saldoPunti)+' punti ('+b.saldoMin.gare+' gare)':'—');
   put(target,'p','Bonus e malus sono punti fantasy solo quando la scomposizione riconcilia ogni tabellino; saldo = somma MODIF FCM COUNTED. Il trattino indica dato non verificabile, non zero.');
   teamRows(target,data.seasonSummary);
   target.scrollIntoView({block:'start',behavior:'smooth'});
   history.replaceState(null,'','?'+new URLSearchParams({playerKey,season}));
 }
 async function openProfile(key,season){
   const ticket=++state.token;$('rn-calc-status').textContent='Caricamento carriera '+key+'…';
   try{
     if(!seasonAllowed(season))throw Error('Il periodo richiesto non appartiene al sito');
     const careers=await section(season,'Careers'),individual=careers.find(x=>x.playerKey===key);
     if(!individual)throw Error('Chiave giocatore non presente: '+key);
     const history=await api.loadHistory('../js');
     const approved=history.find(h=>h.stagioni?.some(y=>y.playerKey===key&&y.stagione===season))||null;
     const years=approved?approved.stagioni:[{stagione:season,playerKey:key}];
     const teams=await Promise.all(years.map(async y=>profile.seasonTeamContexts(await section(y.stagione,'Teams'),y.stagione,y.playerKey)));
     const contexts=teams.flat();
     const [officialData,scoreRows,seasonTeams]=await Promise.all([official(years),points(years),section(season,'Teams')]);
     if(ticket!==state.token)return;
     const data=profile.composeProfile(approved,individual,season,seasonTeams,officialData,contexts,scoreRows);
     renderPlayer(data,key,season);$('rn-calc-status').textContent='Carriera: '+data.years.length+' stagioni e '+data.seasonSummary.length+' righe stagione × fantasquadra.';
   }catch(e){if(ticket===state.token)showError(e);}
 }
 function validBack(value){
   try{const url=new URL(value,location.href);return url.origin===location.origin&&url.pathname.endsWith('/RecordsNext/top_flop.html')?url.href:null;}
   catch(e){return null;}
 }
 async function init(){
   if(!api||!profile||!search){showError(Error('Moduli RecordsNext mancanti'));return;}
   const themes=['mauzstrom','fantablue2','maelstrom','neutral'];
   let skin='mauzstrom';try{skin=sessionStorage.getItem('rn4-recordsnext-skin')||skin;}catch(e){}
   if(!themes.includes(skin))skin='mauzstrom';$('rn-calc-skin').value=skin;
   $('rn-calc-css').href='../profiles/'+skin+'/recordsnext.css';
   $('rn-calc-skin').addEventListener('change',()=>{const v=themes.includes($('rn-calc-skin').value)?$('rn-calc-skin').value:'mauzstrom';
      $('rn-calc-css').href='../profiles/'+v+'/recordsnext.css';try{sessionStorage.setItem('rn4-recordsnext-skin',v);}catch(e){}});
   const params=new URLSearchParams(location.search),back=validBack(params.get('back'));
   if(back)$('rn-calc-back').href=back;
   try{state.facade=await api.loadFacade('../js');if(!state.facade.stagioni?.length)throw Error('Stagioni non pubblicate');
     $('rn-calc-status').textContent='Archivio storico disponibile: '+state.facade.stagioni.length+' stagioni.';
     const q=$('rn-calc-search'),results=$('rn-calc-results');let debounce=0;
     q.addEventListener('input',async()=>{const ticket=++debounce,term=q.value.trim();results.replaceChildren();
       if(term.length<2)return;put(results,'p','Ricerca in tutte le stagioni…');
       try{const index=await loadIndex();if(ticket!==debounce)return;
         const found=search.search(index,term,50);results.replaceChildren();
         put(results,'p',found.length+' risultati (identità stagionali o approvate).');
         for(const item of found){const button=put(results,'button',item.name+' · '+item.start+'–'+item.end+(item.approved?' · identità approvata':''));
           button.type='button';button.className='rn-tf-global-option';
           button.addEventListener('click',()=>openProfile(item.playerKey,item.season));}
       }catch(e){if(ticket===debounce)showError(e);}
     });
     const key=params.get('playerKey'),season=params.get('season');
     if(key&&season)openProfile(key,season);
   }catch(e){showError(e);}
 }
 if(d.readyState==='loading')d.addEventListener('DOMContentLoaded',init);else init();
})(window,document);
