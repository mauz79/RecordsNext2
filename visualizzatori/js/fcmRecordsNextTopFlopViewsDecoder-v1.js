/* RecordsNext 4.0 - public Top & Flop VIEWS v1.
 * Only selected records and aggregated careers/teams; no complete FCM/FCA dump.
 * Lazy loading: do NOT preload all years or sections. Caller applies historical cutoff.
 */
(function (root) {
  'use strict';
  var pending = Object.create(null);
  function facade() {
    var f = root.fcmRecordsNextTopFlopViews;
    if (!f || f.schemaVersion !== '4.0-public-views-v1') {
      throw new Error('RecordsNext TopFlop: incompatible views facade');
    }
    return f;
  }
  function meta(season) {
    var found = facade().stagioni.find(function (s) { return s.stagione === season; });
    if (!found) throw new Error('Unknown season (or past cutoff): ' + season);
    return found;
  }
  function pairs(vector) {
    var keys = facade().eventi, out = {};
    if (!Array.isArray(vector) || vector.length % 2 !== 0) throw new Error('Invalid event vector');
    for (var i = 0; i < vector.length; i += 2) {
      if (!Number.isInteger(vector[i]) || !keys[vector[i]] || Object.hasOwn(out,keys[vector[i]]))
        throw new Error('Invalid event index');
      out[keys[vector[i]]] = vector[i + 1];
    }
    return out;
  }
  function average(sum,n) { return n > 0 ? sum / n : null; }
  function cost(row) {
    if (!row) return null;
    if (!Array.isArray(row) || row.length !== 4) throw new Error('Invalid cost vector');
    return {registrazioniConCosto:row[0],minimo:row[1] === ''?null:row[1],
      massimo:row[2] === ''?null:row[2],medio:row[3] === ''?null:row[3],
      origine:'FCM.Tesserato.COSTOACQ'};
  }
  function unused(row) {
    if (!row) return null;
    if (!Array.isArray(row) || row.length !== 5) throw new Error('Invalid unused vector');
    return {gareConVotoReale:row[0],sommaVotiReali:row[1],
      mediaVotoReale:average(row[1],row[0]),gareConFantavotoNumerico:row[2],
      sommaFantavotiTabellino:row[3],fantamediaTabellino:average(row[3],row[2]),
      eventiRealiNonApportati:pairs(row[4]),
      origine:'FCM_FORMAZIONE_NON_CONTEGGIATA_E_VOTO_FCA'};
  }
  function decodeCareer(row) {
    if (!Array.isArray(row) || (row.length !== 6 && row.length !== 7)) throw new Error('Invalid career row');
    var r = row[4], f = row[5];
    if (r.length !== 6 || (f.length !== 9 && f.length !== 10)) throw new Error('Invalid aggregates');
    return {
      playerKey:row[0],nome:row[1],ruoloFca:row[2],
      codiceStorico:row[3] === 0 ? null : row[3],identitaStoricaVerificata:false,
      reale:{votiValidi:r[0],sommaVoti:r[1],mediaVoto:average(r[1],r[0]),
        eventi:pairs(r[2]),giornateGolSubitiConVoto:r[3],
        giornateZeroGolSubitiRegistrati:r[4],
        mediaGolSubitiSuGiornateVerificate:r[5] === '' ? null : r[5]},
      fantasy:{contestiOsservati:f[0],conteggiati:f[1],conVotoRealeValido:f[2],
        sommaVotiRealiApportati:f[3],mediaVotoRealeApportato:average(f[3],f[2]),
        sommaFantavotiApportati:f[4],fantamediaApportata:average(f[4],f[1]),
        panchinaNonConteggiata:f[5],fuoriLista:f[6],utilizzoDaVerificare:f[7],
        eventiApportati:pairs(f[8]),nonUtilizzatoOsservato:unused(f[9])},
      costoAcquisto:cost(row[6])
    };
  }
  function dict(rows) {
    var obj=Object.create(null);
    (rows || []).forEach(function (r) {
      if (obj[r[0]] !== undefined) throw new Error('Duplicate dictionary key');
      obj[r[0]] = r;
    });
    return obj;
  }
  function decodeTeam(row, season) {
    if (!Array.isArray(row) || (row.length !== 4 && row.length !== 5) ||
      (row[3].length !== 9 && row[3].length !== 10))
      throw new Error('Invalid team row');
    var md=meta(season), ts=dict(md.dizionari.squadre), cs=dict(md.dizionari.competizioni),f=row[3];
    if (!ts[row[1]] || !cs[row[2]]) throw new Error('Missing team/competition dictionary');
    return {playerKey:row[0],idSquadraInterno:row[1],fantasquadra:ts[row[1]][1],
      idCompetizioneInterno:row[2],competizione:cs[row[2]][1],famiglia:cs[row[2]][2],
      apporto:{contestiOsservati:f[0],conteggiati:f[1],conVotoRealeValido:f[2],
        sommaVotiRealiApportati:f[3],mediaVotoRealeApportato:average(f[3],f[2]),
        sommaFantavotiApportati:f[4],fantamediaApportata:average(f[4],f[1]),
        panchinaNonConteggiata:f[5],fuoriLista:f[6],utilizzoDaVerificare:f[7],
        eventiApportati:pairs(f[8]),nonUtilizzatoOsservato:unused(f[9])},
      costoAcquisto:cost(row[4])};
  }
  function store() {
    return root.fcmRecordsNextTopFlopViewsData || (root.fcmRecordsNextTopFlopViewsData={});
  }
  function loadScript(url) {
    if (pending[url]) return pending[url];
    pending[url] = new Promise(function (resolve,reject) {
      var script=document.createElement('script');script.async=true;script.src=url;
      script.onload=resolve;
      script.onerror=function () {delete pending[url];reject(new Error('Failed to load '+url));};
      document.head.appendChild(script);
    });
    return pending[url];
  }
  function baseUrl(base,name) {return String(base || '.').replace(/\/+$/,'')+'/'+name;}
  function loadFacade(base) {
    if (root.fcmRecordsNextTopFlopViews) return Promise.resolve(facade());
    return loadScript(baseUrl(base,'fcmRecordsNext_TopFlopViews.js')).then(facade);
  }
  function loadView(base,season,kind) {
    if (!['Careers','Teams','Records','Highlights'].includes(kind))
      return Promise.reject(new Error('Unknown view section'));
    return loadFacade(base).then(function () {
      var files=meta(season).files.filter(function (f) {return f.tipo === kind;});
      return Promise.all(files.map(function (f) {return loadScript(baseUrl(base,f.nome));})).then(function () {
        var data=(store()[kind] || {})[season] || [];
        if (data.length !== files.reduce(function (n,f) {return n+f.righe;},0))
          throw new Error('Incomplete '+kind+' shard coverage for '+season);
        if (kind === 'Careers') return data.map(decodeCareer);
        if (kind === 'Teams') return data.map(function (r) {return decodeTeam(r,season);});
        return data;
      });
    });
  }
  function loadHistory(base) {
    return loadFacade(base).then(function(f) {
      var meta=f.storico;
      if(!meta || !meta.file || !meta.file.length) return [];
      return Promise.all(meta.file.map(function(x){return loadScript(baseUrl(base,x.nome));}))
        .then(function(){
          var data=(store().History || {}).Storico || [];
          if(data.length!==meta.file.reduce(function(n,x){return n+x.righe;},0))
            throw new Error('Incomplete historical identity shards');
          return data;
        });
    });
  }
  root.RecordsNextTopFlopViewsV1 = Object.freeze({
    facade:facade,meta:meta,pairs:pairs,
    decodeCareer:decodeCareer,decodeTeam:decodeTeam,
    loadFacade:loadFacade,loadView:loadView,loadHistory:loadHistory
  });
})(typeof window !== 'undefined' ? window : globalThis);
