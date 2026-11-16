/* RecordsNext 4.0 - native-skin Top & Flop frontend v53.
 * Source of truth: plugin-generated fcmRecordsNext_TopFlopViews.js + declared shards.
 * Load one selected season; history/Teams only on explicit demand.
 */
(function (root,doc) {'use strict';
 var api=root.RecordsNextTopFlopViewsV2;
 var $=function(id){return doc.getElementById(id)};
 var state={tab:'records',season:'',records:[],highlights:[],careers:[],teams:[],history:[],loaded:{},filtered:[],limit:60,serial:0};
 var labels={TF01:'Miglior voto reale',TF02:'Peggior voto reale',TF03:'Miglior fantavoto apportato',TF04:'Peggior fantavoto apportato',TF05:'Migliori medie voto',TF06:'Fantamedie',TF07:'Gol reali e apportati',TF08:'Assist reali e apportati',TF09:'Malus',TF10:'Presenze reali e fantasy',TF11:'Voti non utilizzati (panchina documentata)',TF12:'Errori pesati (panchina documentata)',TF13:'Fantavoti non utilizzati (panchina documentata)',TF14:'Stagione schillaciana',TF15:'Stagione sbagliata',TF16:'Rendimento per fantasquadra',TF17:'Rigori parati',TF18:'Gol subiti',TF19:'Giornate con zero gol subiti',TF20:'Portieri goleador e assist'};
 var safeThemes=['mauzstrom','fantablue2','maelstrom','neutral'];
 function make(tag,text){var e=doc.createElement(tag);if(text!==undefined)e.textContent=String(text);return e}
 function clear(el){while(el.firstChild)el.removeChild(el.firstChild)}
 function feedback(s){$('tf-feedback').textContent=s}
 function n(x){return x===null||x===undefined||x===''?'—':(typeof x==='number'?x.toLocaleString('it-IT',{maximumFractionDigits:3}):String(x))}
 function seasonLabel(x){return String(x).replace('_','/')}
 function addOpt(el,val,text){var o=make('option',text);o.value=val;el.appendChild(o)}
 function td(tr,text){tr.appendChild(make('td',text));return tr.lastChild}
 function linkInto(cell,url){if(typeof url==='string'&&/^(https?:\/\/|\.?\.?\/)/i.test(url)){
   var a=make('a','Tabellino');a.href=url;a.rel='noopener noreferrer';a.target='_blank';a.className='rn-tf-link';cell.appendChild(a);
 }else cell.textContent='—';}
 function setTheme(name){if(safeThemes.indexOf(name)<0)name='mauzstrom';$('tf-skin').value=name;
  $('rn-tf-skin').href='../profiles/'+name+'/recordsnext.css';
  try{root.sessionStorage.setItem('rn4-recordsnext-skin',name)}catch(e){}
 }
 function heading(names){var h=$('tf-head');clear(h);var tr=make('tr');names.forEach(function(x){tr.appendChild(make('th',x))});h.appendChild(tr)}
 function stats(pairs){var a=$('tf-metrics');clear(a);pairs.forEach(function(pair){var d=make('div');d.className='rn-meta-item';d.appendChild(make('span',pair[0]));d.appendChild(make('strong',n(pair[1])));a.appendChild(d)})}
 function scorecard(v,highlight){if(v&&v.urlTabellino)return v.urlTabellino;
  if(v&&v.scorecardUrl)return v.scorecardUrl;
  if(highlight){if(highlight.urlTabellino)return highlight.urlTabellino;
   var ev=(highlight.evidenze||[]).find(function(x){return x&&x.urlTabellino});if(ev)return ev.urlTabellino;
   var c=(highlight.contestiFantasy||[]).find(function(x){return !!x.urlTabellino});if(c)return c.urlTabellino;}
  return null;}
 function highMap(){var map=Object.create(null);state.highlights.forEach(function(h){if(h&&h.key)map[h.key]=h});return map}
 function rawRecords(){var hm=highMap(),a=[];
  state.records.forEach(function(r){if(!r||!r.recordId)return;
   (r.risultati||[]).forEach(function(w){var hl=hm[w.highlightKey]||null;
    var p=w.calciatore||r.calciatore||(hl&&hl.calciatore)||{};
    a.push({id:r.recordId,metric:r.metrica||'',ambito:r.ambito||'',valore:r.valore,
     nome:p.nome||w.nome||'',code:p.codiceStorico||'',team:w.fantasquadra||w.squadra||(hl&&hl.fantasquadra)||'',
     competition:w.competizione||(hl&&hl.competizione)||'',round:w.giornataSerieA||(hl&&hl.giornataSerieA)||'',
     url:scorecard(w,hl),note:r.copertura||'',playerKey:p.playerKey||'',source:w});
   });
  });return a;
 }
 function filterRows(){var q=$('tf-search').value.trim().toLocaleLowerCase('it-IT');
  if(state.tab==='records'){
    var id=$('tf-record').value;
    return rawRecords().filter(function(r){return (!id||r.id===id)&&(!q||(r.nome+' '+r.code+' '+r.team+' '+r.metric).toLocaleLowerCase('it-IT').includes(q))})
      .sort(function(a,b){return a.id.localeCompare(b.id)||String(a.nome).localeCompare(String(b.nome),'it')});
  }
  if(state.tab==='careers')return state.careers.filter(function(r){return !q||(r.nome+' '+(r.codiceStorico||'')).toLocaleLowerCase('it-IT').includes(q)})
    .sort(function(a,b){return (b.reale.votiValidi||0)-(a.reale.votiValidi||0)||a.nome.localeCompare(b.nome,'it')});
  return state.history.filter(function(r){var p=r.stagioni||[];var name=p.length?p[p.length-1].nome:'';
   return !q||(name+' '+r.playerIdentityId).toLocaleLowerCase('it-IT').includes(q)}).sort(function(a,b){return (b.votiRealiValidi||0)-(a.votiRealiValidi||0)});
 }
 function draw(){state.filtered=filterRows();var body=$('tf-body');clear(body);$('tf-detail').hidden=true;
  var rows=state.filtered.slice(0,state.limit);
  if(state.tab==='records'){
   heading(['Record','Calciatore','Fantasquadra','Competizione','Metrica','Valore','Giornata','Tabellino']);
   rows.forEach(function(r){var tr=make('tr');td(tr,labels[r.id]||r.id);td(tr,r.nome||'—');td(tr,r.team||'—');td(tr,r.competition||'—');td(tr,r.metric||r.ambito);td(tr,n(r.valore));td(tr,n(r.round));linkInto(td(tr,''),r.url);body.appendChild(tr)});
   stats([['Stagione',seasonLabel(state.season)],['Record presenti',state.records.length],['Risultati con filtri',state.filtered.length],['Tabellini disponibili',state.filtered.filter(function(r){return !!r.url}).length]]);
  }else if(state.tab==='careers'){
   heading(['Calciatore','Voti reali validi','Media voto reale','Contesti fantasy','Costi registrati','Dettagli']);
   rows.forEach(function(r){var tr=make('tr');td(tr,r.nome);td(tr,n(r.reale.votiValidi));td(tr,n(r.reale.mediaVoto));td(tr,n(r.fantasy.contestiOsservati));td(tr,r.costoAcquisto?n(r.costoAcquisto.medio):'—');
    var b=make('button','Fantasquadre');b.className='rn-button';b.type='button';b.addEventListener('click',function(){showPlayer(r)});td(tr,'').appendChild(b);body.appendChild(tr)});
   stats([['Stagione',seasonLabel(state.season)],['Calciatori presenti',state.careers.length],['Con voto reale',state.careers.filter(function(r){return r.reale.votiValidi>0}).length],['Risultati filtrati',state.filtered.length]]);
  }else{
   heading(['Calciatore','Stagioni approvate','Voti reali','Media voto','Fantamedia reale verificata','Contesti distinti','Dettagli']);
   rows.forEach(function(r){var tr=make('tr');var years=r.stagioni||[],last=years[years.length-1]||{};
    td(tr,last.nome||r.playerIdentityId);td(tr,r.numeroStagioni);td(tr,n(r.votiRealiValidi));td(tr,n(r.mediaVotoReale));
    td(tr,n(r.fantamediaReale));td(tr,n(r.contestiFantasyDistinti));
    var b=make('button','Stagioni');b.className='rn-button';b.type='button';b.addEventListener('click',function(){showHistory(r)});td(tr,'').appendChild(b);body.appendChild(tr)});
   stats([['Identità approvate nel cutoff',state.history.length],['Filtrate',state.filtered.length],['Stagione più recente',seasonLabel(state.season)],['Origine','Registro approvazioni']]);
  }
  $('tf-more').hidden=state.filtered.length<=state.limit;$('tf-count').textContent='Visualizzati '+rows.length+' / '+state.filtered.length;
 }
 function showPlayer(p){var box=$('tf-detail');clear(box);box.appendChild(make('h2',p.nome+' · '+seasonLabel(state.season)));
  var list=make('ul'),found=state.teams.filter(function(t){return t.playerKey===p.playerKey});
  found.forEach(function(t){var a=t.apporto;
   list.appendChild(make('li',t.fantasquadra+' · '+t.competizione+' — gare conteggiate: '+n(a.conteggiati)+', media voto apportata: '+n(a.mediaVotoRealeApportato)+', fantamedia apportata: '+n(a.fantamediaApportata)))});
  if(!found.length)list.appendChild(make('li','Nessun contesto fantasy osservato per la stagione.'));
  box.appendChild(list);box.appendChild(make('p','Le medie fantasy sono per fantasquadra e competizione. Non viene calcolato un totale fittizio sommando competizioni diverse.'));box.hidden=false;box.scrollIntoView({block:'nearest',behavior:'smooth'});
 }
 function showHistory(r){var box=$('tf-detail');clear(box);var seasons=r.stagioni||[],last=seasons[seasons.length-1]||{};
  box.appendChild(make('h2',last.nome||r.playerIdentityId));var list=make('ul');
  seasons.forEach(function(s){list.appendChild(make('li',seasonLabel(s.stagione)+' · '+s.nome+' · voti '+n(s.votiValidi)+' · media '+n(s.votiValidi>0?s.sommaVoti/s.votiValidi:null)))});
  box.appendChild(list);if(r.anomalieStagionali&&r.anomalieStagionali.length){box.appendChild(make('h3','TF14 / TF15 – confronti storici'));
   var an=make('ul');r.anomalieStagionali.forEach(function(a){an.appendChild(make('li',a.recordId+' · '+seasonLabel(a.stagione)+' · scostamento '+n(a.delta)+' · stagioni baseline '+n(a.numeroStagioniBaseline)))});box.appendChild(an);}
  box.appendChild(make('p','Identità esplicitamente approvata. Nessuna fusione automatica; i contesti di fantasquadra rimangono distinti.'));
  box.hidden=false;box.scrollIntoView({block:'nearest',behavior:'smooth'});
 }
 function setActive(){[['records','tf-tab-records'],['careers','tf-tab-careers'],['history','tf-tab-history']].forEach(function(x){$(''+x[1]).classList.toggle('is-current',state.tab===x[0])});
  $('tf-record').disabled=state.tab!=='records';$('tf-season').disabled=state.tab==='history';}
 function loadRecords(token){feedback('Caricamento record della stagione '+seasonLabel(state.season)+'…');
  Promise.all([api.loadView('../js',state.season,'Records'),api.loadView('../js',state.season,'Highlights')]).then(function(a){if(token!==state.serial)return;
   state.records=a[0];state.highlights=a[1];state.loaded.records=state.season;
   var select=$('tf-record'),old=select.value;clear(select);addOpt(select,'','Tutti i record disponibili');
   Array.from(new Set(state.records.map(function(r){return r.recordId}).filter(Boolean))).sort().forEach(function(k){addOpt(select,k,(labels[k]||k)+' ('+k+')')});
   select.value=Array.from(select.options).some(function(o){return o.value===old})?old:'';
   feedback('Record della stagione '+seasonLabel(state.season)+' caricati dal plugin.');draw();
  }).catch(function(err){if(token===state.serial)feedback('ERRORE: '+err.message)})}
 function loadCareers(token){feedback('Caricamento delle carriere e dei contesti '+seasonLabel(state.season)+'…');
  Promise.all([api.loadView('../js',state.season,'Careers'),api.loadView('../js',state.season,'Teams')]).then(function(a){if(token!==state.serial)return;
   state.careers=a[0];state.teams=a[1];state.loaded.careers=state.season;
   feedback('Carriere stagionali caricate. Le identità non sono collegate automaticamente.');draw();
  }).catch(function(err){if(token===state.serial)feedback('ERRORE: '+err.message)})}
 function loadHistory(token){feedback('Caricamento dello storico approvato nel cutoff del sito…');
  api.loadHistory('../js').then(function(rows){if(token!==state.serial)return;
   state.history=rows;state.loaded.history=true;feedback('Carriere storiche con identità approvate: '+rows.length+'.');draw();
  }).catch(function(err){if(token===state.serial)feedback('ERRORE: '+err.message)})}
 function refresh(){var t=++state.serial;state.limit=60;setActive();
  if(state.tab==='history'){if(state.loaded.history){feedback('Storico approvato caricato.');draw()}else loadHistory(t)}
  else if(state.tab==='careers'){if(state.loaded.careers===state.season){draw()}else loadCareers(t)}
  else if(state.loaded.records===state.season){draw()}else loadRecords(t);
 }
 function init(){if(!api){feedback('ERRORE: decoder Top & Flop v2 non trovato.');return}
  var previous='mauzstrom';try{previous=root.sessionStorage.getItem('rn4-recordsnext-skin')||previous}catch(e){}
  setTheme(previous);$('tf-skin').addEventListener('change',function(){setTheme(this.value)});
  api.loadFacade('../js').then(function(f){var select=$('tf-season');clear(select);
   var seasons=f.stagioni.map(function(s){return s.stagione}).sort().reverse();
   if(!seasons.length)throw new Error('Nessuna stagione nel cutoff del sito');
   seasons.forEach(function(s){addOpt(select,s,seasonLabel(s))});state.season=seasons[0];select.value=state.season;
   select.addEventListener('change',function(){state.season=this.value;refresh()});
   $('tf-record').addEventListener('change',function(){state.limit=60;draw()});
   $('tf-search').addEventListener('input',function(){state.limit=60;draw()});
   [['records','tf-tab-records'],['careers','tf-tab-careers'],['history','tf-tab-history']].forEach(function(x){$(x[1]).addEventListener('click',function(){state.tab=x[0];refresh()})});
   $('tf-more').addEventListener('click',function(){state.limit+=60;draw()});
   refresh();
  }).catch(function(err){feedback('ERRORE: '+err.message)});
 }
 if(doc.readyState==='loading')doc.addEventListener('DOMContentLoaded',init);else init();
})(window,document);
