/* RecordsNext 4.0 - TF01..TF20 ranking semantics v69.
 * Data contract: VOTO1 FCA once per real player/day, actual COUNTED FCM TOT
 * per independent team/competition/match. Identity is never inferred by name.
 */
(function(root){'use strict';
 const EVENTS=['GOLFATTI1','GOLFATTISURIGORE1','ASSIST','GOLSUBITI','GOLSUBITISURIGORE','RIGPAR','RIGSBA','AUTOGOL1','AMM','ESP'];
 const LABELS={TF01:'Migliori voti reali',TF02:'Peggiori voti reali',TF03:'Migliori fantavoti apportati',TF04:'Peggiori fantavoti apportati',TF05:'Media voto reale',TF06:'Fantamedia apportata',TF07:'Gol segnati',TF08:'Assist',TF09:'Eventi negativi',TF10:'Presenze con voto / incontri conteggiati',TF11:'L’incompreso: media voto non utilizzato',TF12:'Errore pesato sui voti non utilizzati',TF13:'Fantavoto non utilizzato',TF14:'Stagione schillaciana',TF15:'Stagione sbagliata',TF16:'Rendimento con una fantasquadra',TF17:'Rigori parati',TF18:'Gol subiti',TF19:'Giornate senza gol subiti',TF20:'Gol e assist dei portieri'};
 const KEYS=Object.keys(LABELS);
 const isNum=v=>v!==null&&v!==undefined&&v!==''&&Number.isFinite(Number(v));
 const num=v=>isNum(v)?Number(v):null;
 const val=(events,k)=>num((events||{})[k])||0;
 function decodedPairs(vec){const result={};if(!Array.isArray(vec)||vec.length%2!==0)throw Error('Eventi non validi');for(let i=0;i<vec.length;i+=2){let k=EVENTS[vec[i]];if(!k)throw Error('Indice evento non valido');result[k]=Number(vec[i+1]);}return result;}
 function indexedSeason(data){
   const careers=data.careers||[],teams=data.teams||[];
   const players=Object.create(null);
   careers.forEach(p=>{if(players[p.playerKey])throw Error('Doppio calciatore '+p.playerKey);players[p.playerKey]=p;});
   const byPlayer=Object.create(null);
   teams.forEach(t=>{(byPlayer[t.playerKey]||(byPlayer[t.playerKey]=[])).push(t);});
   return {season:data.season,real:data.real||[],counted:data.counted||[],careers,teams,bench:data.bench||[],players,byPlayer,dictionaries:data.dictionaries||{}};
 }
 function label(row,id,metric,amount,extra){return Object.assign({id,season:row.season,playerKey:row.playerKey,name:row.name,metric,value:amount,round:null,team:'',competition:'',sample:null,source:''},extra||{});}
 function listRank(s,opts){
   const id=opts.id, out=[], context=opts.context||'real', event=opts.event||'AMM', metric=opts.metric||'fantamediaApportata';
   const threshold=opts.thresholds||{};
   function pushPlayer(p,amount,key,extra){if(!isNum(amount))return;
     out.push(label({season:s.season,playerKey:p.playerKey,name:p.nome||''},id,key,num(amount),Object.assign({code:p.codiceStorico||''},extra||{})));}
   if(['TF01','TF02'].includes(id)){
     s.real.forEach(a=>{if(!isNum(a[2]))return;const key=s.season+':'+a[0],p=s.players[key];if(!p)throw Error('Calciatore FCA mancante '+key);
       pushPlayer(p,a[2],'votoReale',{round:a[1],sample:1,source:'FCA VOTO1'});
     });
   }else if(['TF03','TF04'].includes(id)){
     s.counted.forEach(a=>{const key=s.season+':'+a[0],p=s.players[key];if(!p)throw Error('Calciatore FCM mancante '+key);
       const t=(s.dictionaries.squadre||[]).find(x=>x[0]===a[3]);const c=(s.dictionaries.competizioni||[]).find(x=>x[0]===a[4]);
       if(!t||!c)throw Error('Squadra/competizione sconosciuta '+s.season);
       pushPlayer(p,a[6],'fantavotoApportato',{round:a[1],fantasyRound:a[2]||null,team:t[1],competition:c[1],sample:1,source:'FCM TOT COUNTED',matchId:a[5],url:a[9]||null});
     });
   }else if(id==='TF05'){
     s.careers.forEach(p=>{const r=p.reale||{};
       if(r.votiValidi < (Number(threshold.votiMinimiStagione)||1))return;
       pushPlayer(p,r.mediaVoto,'mediaVotoRealeStagione',{sample:r.votiValidi,source:'FCA VOTO1'});
     });
   }else if(id==='TF06'){
     s.teams.forEach(t=>{const p=s.players[t.playerKey],a=t.apporto||{},minimum=t.famiglia==='CAMPIONATO'?(Number(threshold.gareMinimeCampionato)||1):(Number(threshold.gareMinimeCoppe)||1);
       if(!p||a.conteggiati<minimum)return;
       pushPlayer(p,a.fantamediaApportata,'fantamediaApportata',{sample:a.conteggiati,team:t.fantasquadra,competition:t.competizione,source:'FCM TOT COUNTED'});
     });
   }else if(['TF07','TF08','TF09','TF10','TF17','TF18','TF19','TF20'].includes(id)){
     const eventName=id==='TF07'?'GOLFATTI1':id==='TF08'?'ASSIST':id==='TF09'?event:id==='TF17'?'RIGPAR':id==='TF18'?'GOLSUBITI':id==='TF20'?opts.keeperEvent||'GOLFATTI1':null;
     s.careers.forEach(p=>{
       if(['TF17','TF18','TF19','TF20'].includes(id)&&Number(p.ruoloFca)!==1)return;
       const real=p.reale||{},fan=p.fantasy||{};
       let amount;
       if(id==='TF10')amount=context==='real'?real.votiValidi:fan.conteggiati;
       else if(id==='TF19'){if(context!=='real')return;amount=real.giornateZeroGolSubitiRegistrati;}
       else if(id==='TF20'){
         const e=context==='real'?real.eventi:fan.eventiApportati;
         const goals=val(e,'GOLFATTI1'),penaltyGoals=val(e,'GOLFATTISURIGORE1'),assists=val(e,'ASSIST');
         if(penaltyGoals>goals)throw Error('TF20: gol su rigore oltre i gol totali per '+p.playerKey);
         amount=opts.keeperEvent==='TOTAL_OFFENSE'?goals+assists:
           val(e,opts.keeperEvent||'GOLFATTI1');
       }else amount=val(context==='real'?real.eventi:fan.eventiApportati,eventName);
       if(amount===null || !Number.isFinite(Number(amount)))return;
       // An event RECORD needs a positive event, unlike an ordinary detail where zero is valid.
       // Do not turn an unknown / missing amount into a first place with zero.
       if(['TF07','TF08','TF10','TF17','TF18','TF19','TF20'].includes(id)&&Number(amount)<=0)return;
       const sample=context==='real'?Number(real.votiValidi||0):Number(fan.conteggiati||0);
       if(['TF17','TF18','TF19','TF20'].includes(id)&&sample<=0)return;
       const keeper= id==='TF20'?{
          gol:val(context==='real'?real.eventi:fan.eventiApportati,'GOLFATTI1'),
          golSuRigore:val(context==='real'?real.eventi:fan.eventiApportati,'GOLFATTISURIGORE1'),
          assist:val(context==='real'?real.eventi:fan.eventiApportati,'ASSIST'),
          totaleOffensivo:val(context==='real'?real.eventi:fan.eventiApportati,'GOLFATTI1')+val(context==='real'?real.eventi:fan.eventiApportati,'ASSIST')}:{};
       pushPlayer(p,amount,id==='TF10'?(context==='real'?'giornateRealiConVoto':'incontriFantasyConteggiati'):
           id==='TF20'?(opts.keeperEvent==='TOTAL_OFFENSE'?'Gol + assist':opts.keeperEvent==='ASSIST'?'Assist':opts.keeperEvent==='GOLFATTISURIGORE1'?'Gol su rigore':'Gol segnati'):eventName,
         {...keeper,sample:id==='TF10'?null:sample,source:context==='real'?'FCA':'FCM COUNTED'});
     });
   }else if(['TF11','TF12','TF13'].includes(id)){
     s.bench.forEach(b=>{const p=s.players[b.playerKey];if(!p)return;
       const E=Number(b.esclusioniCerte)||0,PV=Number(b.esclusioniConVotoReale)||0,PF=Number(b.esclusioniConFantavotoTabellino)||0;
       const minE=Number(threshold.esclusioniDocumentateMinime)||1;
       const minP=Number(threshold.esclusioniConVotoMinime)||1;
       if(E<minE)return;
       const field=id==='TF11'?'mediaVotoReale':id==='TF12'?'errorePesatoVoto':opts.benchMetric||'fantamediaTabellino';
       if(id==='TF11'&&PV<minP || id==='TF12'&&PV===0 || id==='TF13'&&PF<minP)return;
       pushPlayer(p,b[field],field,{team:b.fantasquadra,competition:b.competizione,sample:id==='TF11'?PV:id==='TF12'?E:field==='fantamediaTabellino'?PF:E,
          coverage:'SOLO_PANCHINA_FCM_DOCUMENTATA',source:'FCM PANCHINA VERIFICATA'});
     });
   }else if(id==='TF16'){
     s.teams.forEach(t=>{const p=s.players[t.playerKey];if(!p)return;
       const a=t.apporto||{};let amount;
       if(metric==='conteggiati')amount=a.conteggiati;
       else if(metric==='mediaVotoRealeApportato')amount=a.mediaVotoRealeApportato;
       else if(metric==='gol')amount=val(a.eventiApportati,'GOLFATTI1');
       else if(metric==='assist')amount=val(a.eventiApportati,'ASSIST');
       else amount=a.fantamediaApportata;
       if(metric!=='conteggiati'&&metric!=='gol'&&metric!=='assist'&&a.conteggiati<1)return;
       pushPlayer(p,amount,metric,{team:t.fantasquadra,competition:t.competizione,sample:a.conteggiati,source:'FCM COUNTED'});
     });
   }
   return out;
 }
 // RN4 TF14/TF15 v116: all comparison values use the already published approved History.
 // A missing verified FCA fantasy average remains null; never use FCM COUNTED as a substitute.
 function historicAnomalies(history,id,seasons){const out=[];history.forEach(h=>{(h.anomalieStagionali||[]).forEach(a=>{
   if(a.recordId!==id||!seasons.includes(a.stagione))return;
   const years=h.stagioni||[],year=years.find(x=>x.stagione===a.stagione);if(!year)return;
   const otherYears=years.filter(x=>x.stagione!==a.stagione&&num(x.votiValidi)!==null&&num(x.votiValidi)>0);
   const verified=otherYears.length>0&&otherYears.every(x=>num(x.fantamediaReale)!==null);
   const otherVotes=otherYears.reduce((n,x)=>n+Number(x.votiValidi),0);
   const fantasyBaseline=verified&&otherVotes>0?
     otherYears.reduce((sum,x)=>sum+Number(x.votiValidi)*Number(x.fantamediaReale),0)/otherVotes:null;
   const fantasySeason=num(year.fantamediaReale);
   const fantasyDelta=fantasySeason===null||fantasyBaseline===null?null:fantasySeason-fantasyBaseline;
   out.push(label({season:a.stagione,playerKey:year.playerKey,name:year.nome},id,'deltaMediaVoto',num(a.delta),
     {sample:num(a.votiValidiStagione),mediaStagione:num(a.mediaStagione),baseline:num(a.mediaBaseline),
      mediaCarriera:num(h.mediaVotoReale),baselineVotes:num(a.votiValidiBaseline),
      fantamediaStagione:fantasySeason,fantamediaBaseline:fantasyBaseline,fantamediaDelta:fantasyDelta,
      source:'FCA reale: stagione confrontata con altre stagioni, ponderate per voti validi'}));
 });});return out;}
 function build(all,opts){const ss=all.seasons||[];const id=opts.id;let rows=[];
   ss.forEach(s=>{if(id==='TF14'||id==='TF15')return;rows.push(...listRank(s,opts));});
   if(id==='TF14'||id==='TF15')rows=historicAnomalies(all.history||[],id,ss.map(s=>s.season));
   const q=(opts.search||'').trim().toLocaleLowerCase('it-IT');
   const asc=opts.direction==='MIN';
   rows.sort((a,b)=>{const d=asc?a.value-b.value:b.value-a.value;
     return d||a.season.localeCompare(b.season)||a.playerKey.localeCompare(b.playerKey)||(a.round||0)-(b.round||0)||(a.team||'').localeCompare(b.team||'');});
   let last=null,rank=0;rows.forEach((r,i)=>{if(last===null||r.value!==last){rank=i+1;last=r.value;}r.position=rank;});
   if(q)rows=rows.filter(r=>(r.name+' '+(r.code||'')+' '+r.season.replace('_','/')+' '+r.team+' '+r.competition+' '+r.playerKey).toLocaleLowerCase('it-IT').includes(q));
   return rows;
 }
 root.RecordsNextRankingEngineV69=Object.freeze({LABELS,KEYS,EVENTS,decodedPairs,indexedSeason,listRank,build,num});
})(typeof window!=='undefined'?window:globalThis);
