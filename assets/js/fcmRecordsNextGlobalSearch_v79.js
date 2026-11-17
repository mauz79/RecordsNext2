/* RN4 v79: lazy, cutoff-scoped global archive search. No speculative identity merges. */
(function(root){'use strict';
 function fold(s){return String(s||'').normalize('NFD').replace(/[\u0300-\u036f]/g,'').toLocaleLowerCase('it').replace(/[^\p{L}\p{N}]+/gu,' ').trim();}
 async function loadAll(loadScript,getManifest,getData){
   // The manifest is lazy loaded and the facade defines the cutoff.
   let m=getManifest();
   if(!m){await loadScript('fcmRecordsNext_ProfileManifest_v79.js');m=getManifest();}
   if(!m||m.schemaVersion!=='4.0-profile-v79')throw Error('Manifest ricerca v79 non disponibile');
   const files=m.index||[],expected=new Map();
   for(const item of files)expected.set(item.stagione,(expected.get(item.stagione)||0)+item.righe);
   for(const item of files.sort((a,b)=>a.stagione.localeCompare(b.stagione)||a.part-b.part)){
     // Every shard appends to its own season data; do not skip the later parts.
     await loadScript(item.nome);
   }
   for(const [season,count] of expected){const actual=(getData()||{})[season];
     if(!Array.isArray(actual)||actual.length!==count)throw Error('Indice ricerca incompleto '+season);
   }
   const grouped=new Map();
   for(const season of expected.keys()){
     for(const record of getData()[season]){
       if(!Array.isArray(record)||record.length!==6)throw Error('Riga anagrafica v79 invalida');
       const [key,canon,name,birth,gazza,role]=record;
       if(!key.startsWith(season+':'))throw Error('Stagione dell’indice incoerente');
       const approved=canon!==key;
       const id=String(canon),alias=String(name),already=grouped.get(id);
       if(!already){grouped.set(id,{id,approved,name:alias,aliases:new Set([alias]),
         playerKey:key,season,start:season,end:season,birth:String(birth),gazza:new Set(gazza?[String(gazza)]:[]),roles:new Set(role?[String(role)]:[])});}
       else {
         if(already.approved!==approved)throw Error('Identità storica con stato incoerente '+id);
         already.aliases.add(alias);if(gazza)already.gazza.add(String(gazza));if(role)already.roles.add(String(role));
         if(season<already.start)already.start=season;
         if(season>=already.end){already.end=season;already.season=season;already.playerKey=key;already.name=alias;}
       }
     }
   }
   return [...grouped.values()].map(x=>({...x,start:x.start.replace('_','/'),end:x.end.replace('_','/'),
       aliases:[...x.aliases],gazza:[...x.gazza],roles:[...x.roles]}));
 }
 function search(index,needle,limit=40){const q=fold(needle);if(!q)return [];
   const found=[];for(const x of index){
     const all=[x.name,...x.aliases,...x.gazza].map(fold);
     if(all.some(term=>term.includes(q))){
       const nameMatch=fold(x.name),exact=nameMatch===q||all.includes(q);
       const prefix=nameMatch.startsWith(q);found.push({item:x,priority:exact?0:prefix?1:2});
     }
   }
   return found.sort((a,b)=>a.priority-b.priority||a.item.name.localeCompare(b.item.name,'it')||
       a.item.start.localeCompare(b.item.start)).slice(0,limit).map(x=>x.item);
 }
 root.RecordsNextGlobalSearchV79=Object.freeze({fold,loadAll,search});
})(typeof window!=='undefined'?window:globalThis);
