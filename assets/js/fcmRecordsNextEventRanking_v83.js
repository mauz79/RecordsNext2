/* RecordsNext 4.0 v72 - event counts: separate FCA and FCM, season and approved career.
 * No implicit historic scoring coefficients, no double counting penalty subsets.
 */
(function(root){'use strict';
  const GROUPS={
    MALUS:['AMM','ESP','AUTOGOL1','RIGSBA'],
    BONUS:['GOLFATTI1','ASSIST','RIGPAR']
  };
  const LABELS={AMM:'Ammonizioni',ESP:'Espulsioni',AUTOGOL1:'Autogol',RIGSBA:'Rigori sbagliati',
    GOLSUBITI:'Gol subiti',GOLSUBITISURIGORE:'Gol subiti su rigore',
    GOLFATTI1:'Gol segnati',ASSIST:'Assist',RIGPAR:'Rigori parati',
    GOLFATTISURIGORE1:'Gol segnati su rigore',TOTAL_MALUS:'Totale eventi negativi',
    TOTAL_BONUS:'Totale eventi positivi'};
  const TYPES={MALUS:['TOTAL_MALUS',...GROUPS.MALUS,'GOLSUBITI','GOLSUBITISURIGORE'],
    BONUS:['TOTAL_BONUS',...GROUPS.BONUS,'GOLFATTISURIGORE1']};
  const number=v=>v!==null&&v!==undefined&&v!==''&&Number.isFinite(Number(v))?Number(v):null;
  function integer(v,context){const n=number(v);if(n===null)return 0;
    if(!Number.isSafeInteger(n)||n<0)throw Error('Conteggio evento non valido: '+context);
    return n;}
  function positiveInt(v,context){return integer(v,context);}
  function eventsFor(player,context){return context==='real'?(player.reale||{}).eventi||{}:(player.fantasy||{}).eventiApportati||{};}
  function sampleFor(player,context){return positiveInt(context==='real'?(player.reale||{}).votiValidi:(player.fantasy||{}).conteggiati,'denominatore '+player.playerKey);}
  function partsOf(events,group){const result={};
    for(const key of GROUPS[group])result[key]=integer(events[key],key);
    // Breakdown only; special subsets are NEVER added to the group total.
    if(group==='MALUS'){
      result.GOLSUBITI=integer(events.GOLSUBITI,'GOLSUBITI');
      result.GOLSUBITISURIGORE=integer(events.GOLSUBITISURIGORE,'GOLSUBITISURIGORE');
      if('GOLSUBITI' in events && 'GOLSUBITISURIGORE' in events && result.GOLSUBITISURIGORE>result.GOLSUBITI)throw Error('Gol subiti su rigore > gol subiti');
    }else{
      result.GOLFATTISURIGORE1=integer(events.GOLFATTISURIGORE1,'GOLFATTISURIGORE1');
      if('GOLFATTI1' in events && 'GOLFATTISURIGORE1' in events && result.GOLFATTISURIGORE1>result.GOLFATTI1)throw Error('Gol su rigore > gol segnati');
    }
    return result;
  }
  function total(parts,group){return GROUPS[group].reduce((sum,k)=>sum+parts[k],0);}
  function selectedAmount(parts,type,group){return type==='TOTAL_'+group?total(parts,group):parts[type];}
  function freshRow(player,season,group,context){const sample=sampleFor(player,context),parts=partsOf(eventsFor(player,context),group);
    const active=sample>0||Object.values(parts).some(v=>v>0);
    return active?{season,seasonText:season.replace('_','/'),playerKey:player.playerKey,name:player.nome||'',
      role:player.ruoloFca,parts,sample,years:1,observedSeasons:[season],yearly:[],context}:null;
  }
  function measure(row,opts){const type=opts.type,aggregate=opts.aggregate;
    const amount=selectedAmount(row.parts,type,opts.group);
    row.amount=amount;
    row.total=total(row.parts,opts.group);
    row.averagePerSeason=amount/row.years;
    row.per100=row.sample>0?100*amount/row.sample:null;
    row.value=aggregate==='AVERAGE'?row.averagePerSeason:aggregate==='PER100'?row.per100:amount;
    row.metric=type;
    row.source=opts.context==='real'?'FCA - eventi reali':'FCM - eventi COUNTED';
    return row;
  }
  function approvedMap(history){const lookup=new Map();
    (history||[]).forEach((h,index)=>{
      const seasons=h.stagioni||[];
      if(seasons.length<2)return;
      seasons.forEach(y=>{if(!y.playerKey)throw Error('Stagione approvata senza playerKey');
        if(lookup.has(y.playerKey))throw Error('Identita approvata multipla: '+y.playerKey);
        lookup.set(y.playerKey,index);
      });
    });
    return lookup;
  }
  function build(seasons,history,opts){
    if(!GROUPS[opts.group]||!TYPES[opts.group].includes(opts.type))throw Error('Tipo bonus/malus non valido');
    if(!['real','fantasy'].includes(opts.context))throw Error('Fonte evento non valida');
    if(!['SEASON','CAREER','AVERAGE','PER100'].includes(opts.aggregate))throw Error('Periodo aggregazione non valido');
    if(opts.aggregate!=='SEASON'&&seasons.length<2)throw Error('Carriera richiede ambito Assoluta');
    const rows=[];
    for(const s of seasons){for(const p of s.careers||[]){const item=freshRow(p,s.season,opts.group,opts.context);
      if(item){item.yearly=[item];rows.push(item);}
    }}
    let combined;
    if(opts.aggregate==='SEASON')combined=rows.map(r=>measure({...r},opts));
    else{
      const allowed=approvedMap(history),groups=new Map();
      for(const r of rows){const gid=allowed.get(r.playerKey);
        // Never merge an unapproved identity, even if the name and numeric code match.
        if(gid===undefined)continue;
        let items=groups.get(gid);if(!items){items=[];groups.set(gid,items);}items.push(r);
      }
      combined=[];
      for(const list of groups.values()){
        const ordered=list.slice().sort((a,b)=>a.season.localeCompare(b.season));
        const end=ordered[ordered.length-1],start=ordered[0];
        const parts={};for(const key of Object.keys(start.parts))parts[key]=ordered.reduce((n,r)=>n+r.parts[key],0);
        const sample=ordered.reduce((n,r)=>n+r.sample,0);
        const item={...end,parts,sample,years:ordered.length,
          seasonText:start.season.replace('_','/')+' – '+end.season.replace('_','/'),
          observedSeasons:ordered.map(r=>r.season),yearly:ordered};
        combined.push(measure(item,opts));
      }
    }
    // Undefined rates (no denominator) are excluded, not interpreted as zero.
    combined=combined.filter(r=>r.value!==null&&Number.isFinite(r.value)&&r.amount>0);
    const search=String(opts.search||'').trim().toLocaleLowerCase('it-IT');
    if(search)combined=combined.filter(r=>(r.name+' '+r.playerKey+' '+r.seasonText).toLocaleLowerCase('it-IT').includes(search));
    const asc=opts.direction==='MIN';
    combined.sort((a,b)=>(asc?a.value-b.value:b.value-a.value)||
      a.season.localeCompare(b.season)||a.playerKey.localeCompare(b.playerKey));
    let last=null,rank=0;
    combined.forEach((row,i)=>{if(last===null||row.value!==last)rank=i+1;last=row.value;row.position=rank;});
    return combined;
  }
  root.RecordsNextEventsV72=Object.freeze({GROUPS,TYPES,LABELS,partsOf,total,approvedMap,build});
})(typeof window!=='undefined'?window:globalThis);
