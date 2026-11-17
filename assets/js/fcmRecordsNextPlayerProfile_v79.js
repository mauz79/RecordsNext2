/* RecordsNext 4.0 - player profiles v79: verified FCA real fantasy and weighted FCM club-season totals.
 * No invented TOT/FMT and no unapproved cross-season player identity links.
 */
(function(root){'use strict';
 const numeric=v=>v!==null&&v!==undefined&&v!==''&&Number.isFinite(Number(v))?Number(v):null;
 const n=v=>numeric(v)===null?0:Number(v);
 const avg=(sum,count)=>count>0?sum/count:null;
 const text=v=>v===undefined||v===null?'':String(v);
 // Counts of actual FCM COUNTED events; they are NEVER fantasy points.
 // Goal penalties are subsets, not additional goals; conceded goals are separate goalkeeper context.
 const EVENT_KEYS=Object.freeze(['GOLFATTI1','ASSIST','GOLFATTISURIGORE1','RIGPAR','AMM','ESP','AUTOGOL1','RIGSBA','GOLSUBITI','GOLSUBITISURIGORE']);
 function countedEvents(raw,location){
   if(raw===null||typeof raw!=='object'||Array.isArray(raw))
     throw Error('Eventi FCM mancanti o non validi: '+location);
   const result={};for(const key of EVENT_KEYS){
     const value=raw[key]===undefined?0:numeric(raw[key]);
     if(value===null||!Number.isSafeInteger(value)||value<0)
       throw Error('Numero eventi '+key+' non valido per '+location);
     result[key]=value;
   }
   return result;
 }
 function eventTotals(raw){
   const e=raw;
   return {gol:e.GOLFATTI1,assist:e.ASSIST,golSuRigore:e.GOLFATTISURIGORE1,rigParati:e.RIGPAR,
     ammonizioni:e.AMM,espulsioni:e.ESP,autogol:e.AUTOGOL1,rigoriSbagliati:e.RIGSBA,
     golSubiti:e.GOLSUBITI,golSubitiSuRigore:e.GOLSUBITISURIGORE,
     bonusEventi:e.GOLFATTI1+e.ASSIST+e.RIGPAR,
     malusEventi:e.AMM+e.ESP+e.AUTOGOL1+e.RIGSBA};
 }
 const EVENT_UI=Object.freeze({GOLFATTI1:'gol',ASSIST:'assist',GOLFATTISURIGORE1:'golSuRigore',
   RIGSBA:'rigoriSbagliati',AMM:'ammonizioni',ESP:'espulsioni',AUTOGOL1:'autogol',
   GOLSUBITI:'golSubiti',GOLSUBITISURIGORE:'golSubitiSuRigore',RIGPAR:'rigParati'});
 function exposedEvents(events,missing){const out=eventTotals(events),unknown=new Set(missing||[]);
   for(const name of unknown)if(EVENT_UI[name])out[EVENT_UI[name]]=null;
   if([...unknown].some(k=>['GOLFATTI1','ASSIST','RIGPAR'].includes(k)))out.bonusEventi=null;
   if([...unknown].some(k=>['AMM','ESP','AUTOGOL1','RIGSBA'].includes(k)))out.malusEventi=null;
   return out;
 }
 function addEvents(a,b){for(const key of EVENT_KEYS)a[key]+=b[key];return a;}
 function zeroEvents(){return Object.fromEntries(EVENT_KEYS.map(k=>[k,0]));}
 function seasonTeamContexts(teams,season,playerKey){return (teams||[]).filter(t=>t.playerKey===playerKey).map(t=>({
   stagione:season,playerKey,idSquadraInterno:t.idSquadraInterno,fantasquadra:t.fantasquadra,
   competizione:t.competizione,idCompetizioneInterno:t.idCompetizioneInterno,
   gareConteggiate:n(t.apporto.conteggiati),gareConVotoReale:n(t.apporto.conVotoRealeValido),
   sommaVotiRealiApportati:n(t.apporto.sommaVotiRealiApportati),
   sommaFantavotiApportati:n(t.apporto.sommaFantavotiApportati),
   eventiApportati:countedEvents(t.apporto.eventiApportati,season+' '+t.fantasquadra+' '+t.competizione)
 }));}
 function aggregateSeasonTeams(contexts){const map=new Map();
   for(const c of contexts){
     const team=text(c.fantasquadra).trim(), season=text(c.stagione), player=text(c.playerKey);
     if(!season||!player||!team)throw Error('Contesto FCM senza stagione/identita/fantasquadra');
     const sid=c.idSquadraInterno;
     const id=sid!==null&&sid!==undefined&&sid!==''?String(sid):'NAME:'+team;
     const key=JSON.stringify([season,player,id]);let out=map.get(key);
     if(!out){out={stagione:season,playerKey:player,idSquadraInterno:sid,fantasquadra:team,
       gare:0,voti:0,sommaVoti:0,sommaFantavoti:0,eventiApportati:zeroEvents(),competizioni:[],
       pointsComplete:true,pointsTotal:0,pointsBonus:0,pointsMalus:0,pointsNet:0,pointsDelta:0,deltaComplete:true,pointsSources:new Set(),missingEvents:new Set()};map.set(key,out);}
     if(out.fantasquadra!==team)throw Error('Conflitto nome della fantasquadra in '+season);
     const gare=n(c.gareConteggiate),voti=n(c.gareConVotoReale),sommaVoti=n(c.sommaVotiRealiApportati),
           sommaFantavoti=n(c.sommaFantavotiApportati);
     if(gare<0||voti<0||voti>gare||Math.abs(gare-Math.round(gare))>0||Math.abs(voti-Math.round(voti))>0)
       throw Error('Denominatori FCM non validi per '+team+' '+season);
     const name=text(c.competizione).trim();
     if(!name)throw Error('Competizione FCM assente '+team+' '+season);
     if(out.competizioni.some(x=>x.competizione===name&&x.idCompetizioneInterno===c.idCompetizioneInterno))
       throw Error('Competizione duplicata '+name+' '+team+' '+season);
     out.gare+=gare;out.voti+=voti;out.sommaVoti+=sommaVoti;out.sommaFantavoti+=sommaFantavoti;
     const ev=countedEvents(c.eventiApportati,season+' '+team+' '+name);
     addEvents(out.eventiApportati,ev);
     const score=c.scorecard||null;
     for(const name of score?.missingSourceFields||[])out.missingEvents.add(name);
     if(gare>0){
       if(!score)throw Error('Missing source FCM scorecard for '+season+' '+team+' '+name);
       if(Number(score.gare)!==gare||Math.abs(Number(score.totSum)-sommaFantavoti)>0.012)
         throw Error('FCM scorecard/Teams mismatch: '+season+' '+team+' '+name);
     }
     const good=!!score&&score.certified===true&&Number(score.pairs)===gare&&gare>0;
     const delta=score&&Number(score.pairs)===gare&&numeric(score.saldoPunti)!==null?
       Number(score.saldoPunti):null;
     if(gare>0){
       if(!good)out.pointsComplete=false;
       if(delta===null)out.deltaComplete=false;
       else out.pointsDelta+=delta;
       if(good){out.pointsBonus+=Number(score.bonusPunti);out.pointsMalus+=Number(score.malusPunti);
         out.pointsNet+=Number(score.saldoPunti);
         for(const s of score.ruleSource||[])out.pointsSources.add(s);
       }
     }
     out.competizioni.push({competizione:name,idCompetizioneInterno:c.idCompetizioneInterno,
       gare,voti,mediaVoto:avg(sommaVoti,voti),fantamedia:avg(sommaFantavoti,gare),
       bonusPunti:good?Number(score.bonusPunti):null,
       malusPunti:good?Number(score.malusPunti):null,
       saldoPunti:delta,
       deltaNetTabellino:delta,pointsCertified:good,
       riconciliazione:good?'OK':'Da verificare',
       eventiApportati:ev,...exposedEvents(ev,score?.missingSourceFields||[])});
   }
   return [...map.values()].map(c=>({...c,...exposedEvents(c.eventiApportati,c.missingEvents),mediaVoto:avg(c.sommaVoti,c.voti),
     fantamedia:avg(c.sommaFantavoti,c.gare),numeroCompetizioni:c.competizioni.length,
     bonusPunti:c.gare>0&&c.pointsComplete?c.pointsBonus:null,
     malusPunti:c.gare>0&&c.pointsComplete?c.pointsMalus:null,
     saldoPunti:c.gare>0&&c.deltaComplete?c.pointsDelta:null,
     deltaNetTabellino:c.gare>0&&c.deltaComplete?c.pointsDelta:null,
     riconciliazione:c.gare>0&&c.pointsComplete?'OK':'Da verificare',
     pointsCertified:c.gare>0&&c.pointsComplete,pointsSources:[...c.pointsSources],
     competizioni:c.competizioni.slice().sort((a,b)=>a.competizione.localeCompare(b.competizione,'it'))}));
 }
 // For historical club badges only, exact FCM names are grouped. No speculative team-ID mapping across seasons.
 function byTeamName(seasonTeams){const map=new Map();
   for(const t of seasonTeams){let o=map.get(t.fantasquadra);
     if(!o){o={fantasquadra:t.fantasquadra,stagioni:new Set(),gare:0,voti:0,sommaVoti:0,sommaFantavoti:0};map.set(t.fantasquadra,o);}
     o.stagioni.add(t.stagione);o.gare+=t.gare;o.voti+=t.voti;o.sommaVoti+=t.sommaVoti;o.sommaFantavoti+=t.sommaFantavoti;
   }
   return [...map.values()].map(o=>({fantasquadra:o.fantasquadra,stagioni:o.stagioni.size,
     gare:o.gare,mediaVoto:avg(o.sommaVoti,o.voti),fantamedia:avg(o.sommaFantavoti,o.gare)}));
 }
 function bestTeams(seasonTeams,minGare=5){const rows=byTeamName(seasonTeams),collator=new Intl.Collator('it',{sensitivity:'base'});
   const stable=(a,b)=>collator.compare(a.fantasquadra,b.fantasquadra);
   const mostSeasons=rows.slice().sort((a,b)=>b.stagioni-a.stagioni||b.gare-a.gare||stable(a,b))[0]||null;
   const mostGames=rows.slice().sort((a,b)=>b.gare-a.gare||b.stagioni-a.stagioni||stable(a,b))[0]||null;
   const eligible=rows.filter(x=>x.gare>=minGare&&x.fantamedia!==null);
   const max=eligible.slice().sort((a,b)=>b.fantamedia-a.fantamedia||b.gare-a.gare||stable(a,b))[0]||null;
   const min=eligible.slice().sort((a,b)=>a.fantamedia-b.fantamedia||b.gare-a.gare||stable(a,b))[0]||null;
   const eligibleSaldo=seasonTeams.filter(t=>t.gare>0&&numeric(t.saldoPunti)!==null);
   const saldoSort=(a,b)=>b.saldoPunti-a.saldoPunti||b.gare-a.gare||b.stagione.localeCompare(a.stagione)||stable(a,b);
   const saldoMax=eligibleSaldo.slice().sort(saldoSort)[0]||null;
   const saldoMin=eligibleSaldo.slice().sort((a,b)=>a.saldoPunti-b.saldoPunti||b.gare-a.gare||b.stagione.localeCompare(a.stagione)||stable(a,b))[0]||null;
   return {mostSeasons,mostGames,max,min,saldoMax,saldoMin,minGare,teams:rows};
 }
 function realFMT(years,official){let nVotes=0,sum=0,missing=0;
   const enriched=years.map(row=>{
     const yr={...row},season=text(yr.stagione),playerKey=text(yr.playerKey),id=playerKey.substring(playerKey.indexOf(':')+1);
     const record=official&&official[season]&&official[season][id];
     const count=n(yr.votiValidi);
     let fmt=null,source='';
     if(record&&record[2]==='CONFLITTO_FONTI'){} // A verified source conflict cannot be hidden by older summaries.
     else if(record&&Number(record[0])===count && numeric(record[1])>0){fmt=Number(record[1]);source=record[2]||'FCA Statistiche.FMT';}
     else if(numeric(yr.fantamediaReale)>0){fmt=Number(yr.fantamediaReale);source=yr.fantamediaRealeFonte||'FCA verificata';}
     yr.fantamediaReale=fmt;yr.fantamediaRealeFonte=source;
     if(count>0){if(fmt===null)missing+=count;else {sum+=fmt*count;nVotes+=count;}}
     return yr;
   });
   return {years:enriched,media:missing===0&&nVotes>0?sum/nVotes:null,
     covered:nVotes,missing,precision:'FMT stagionali ufficiali ponderate per voti validi (FMT arrotondata alla fonte)'};
 }

 function attachScorecards(contexts,rows){
   const byKey=new Map();
   for(const item of rows||[]){
     const key=JSON.stringify([item.playerKey,Number(item.idSquadraInterno),Number(item.idCompetizioneInterno)]);
     if(byKey.has(key))throw Error('Duplicate scorecard context '+key);
     byKey.set(key,item);
   }
   return contexts.map(c=>{
     const key=JSON.stringify([c.playerKey,Number(c.idSquadraInterno),Number(c.idCompetizioneInterno)]);
     const scorecard=byKey.get(key)||null;
     if(c.gareConteggiate>0&&!scorecard)throw Error('Missing scorecard row '+key);
     if(scorecard&&Number(scorecard.gare)!==Number(c.gareConteggiate))
       throw Error('Inconsistent counted match coverage '+key);
     return {...c,scorecard};
   });
 }
 function composeProfile(approved,individual,season,seasonTeams,official,allSeasonContexts,scorecardRows){
   const real=individual.reale||{};
   const years=approved?(approved.stagioni||[]):[{stagione:season,playerKey:individual.playerKey,nome:individual.nome,
       votiValidi:real.votiValidi,mediaVoto:real.mediaVoto,eventi:real.eventi||{}}];
   const contexts=approved?
     (allSeasonContexts||[]).slice():seasonTeamContexts(seasonTeams,season,individual.playerKey);
   if(approved){
     const yearsKeys=new Set(years.map(y=>y.stagione+'|'+y.playerKey));
     for(const c of contexts)if(!yearsKeys.has(c.stagione+'|'+c.playerKey))
       throw Error('Contesto FCM non appartenente alla carriera approvata: '+c.playerKey);
     if(!allSeasonContexts)throw Error('Mancano gli apporti FCM delle stagioni della carriera');
   }
   const fmt=realFMT(years,official||{});
   const seasonSummary=aggregateSeasonTeams(attachScorecards(contexts,scorecardRows));
   const realEvents=approved?(approved.eventiReali||{}):(real.eventi||{});
   const isGoalkeeper=Number(individual.ruoloFca)===1;
   return {years:fmt.years,fmt,contexts,seasonSummary,best:bestTeams(seasonSummary),
     ruoloFca:individual.ruoloFca,isGoalkeeper,
     events:realEvents,approved:!!approved,name:fmt.years.at(-1)?.nome||individual.nome,
     voti:approved?approved.votiRealiValidi:real.votiValidi,
     mediaVoto:approved?approved.mediaVotoReale:real.mediaVoto};
 }
 root.RecordsNextPlayerProfileV79=Object.freeze({seasonTeamContexts,aggregateSeasonTeams,attachScorecards,byTeamName,
   bestTeams,realFMT,composeProfile,eventTotals,countedEvents});
})(typeof window!=='undefined'?window:globalThis);
