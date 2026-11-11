/* RecordsNext 4.0 Top & Flop — reversible, lazy browser decoder for staging v2.
 * Does not download all historical player details on page load.
 * No publication logic, CSS changes or inferred scorecard URLs.
 */
(function (global) {
  'use strict';
  var VoteState = ['VALID', 'NO_VOTE', 'SPECIAL', 'NOT_PLAYED', 'UNKNOWN'];
  var Usage = ['COUNTED', 'BENCH_UNUSED', 'OUTSIDE_LIST', 'EXCLUDED_CONFIRMED', 'UNKNOWN'];
  var pending = Object.create(null);
  function facade() {
    var f = global.fcmRecordsNextTopFlop;
    if (!f || f.schemaVersion !== '4.0-stage-v2-compact') {
      throw new Error('Top & Flop compact v2: missing or incompatible facade');
    }
    return f;
  }
  function seasonMetadata(season) {
    var match = facade().seasons.find(function (s) { return s.seasonId === season; });
    if (!match) throw new Error('Unknown Top & Flop season: ' + season);
    return match;
  }
  function pairs(values, names) {
    var result = {};
    if (values === null || values === undefined) return result;
    if (values.length % 2 !== 0) throw new Error('Invalid compact event vector');
    for (var i = 0; i < values.length; i += 2) {
      var name = names[values[i]];
      if (name === undefined || Object.prototype.hasOwnProperty.call(result, name)) {
        throw new Error('Invalid compact event index');
      }
      result[name] = values[i + 1];
    }
    return result;
  }
  function lookups(meta) {
    var teams = Object.create(null), competitions = Object.create(null);
    meta.teams.forEach(function (r) { teams[r[0]] = r[1]; });
    meta.competitions.forEach(function (r) { competitions[r[0]] = r; });
    return { teams: teams, competitions: competitions };
  }
  function decodeSummary(row, meta) {
    var l = lookups(meta);
    var contributors = row[10].map(function (c) {
      var comp = l.competitions[c[1]];
      return {
        teamId: c[0], team: l.teams[c[0]], competitionId: c[1],
        competition: comp[1], family: comp[2], contexts: c[2], counted: c[3],
        countedWithRealVote: c[4], voteSum: c[5], meanVote: c[6],
        fantasySum: c[7], meanFantasy: c[8],
        sourceEventsApportioned: pairs(c[9], meta.eventNames)
      };
    });
    return {
      seasonId: meta.seasonId, localFcaId: row[0], gazzaCode: row[1],
      name: row[2], detailFile: meta.detailFiles[row[3]],
      real: { days: row[4], withVote: row[5], voteSum: row[6],
              meanVote: row[7], sourceEvents: pairs(row[8], meta.eventNames) },
      fantasyContexts: row[9], contributions: contributors
    };
  }
  function indexLinks(rows) {
    var index = Object.create(null);
    (rows || []).forEach(function (r) {
      var key = r[0] + '|' + r[1];
      if (index[key] !== undefined && index[key] !== r[2]) {
        throw new Error('Conflicting Top & Flop scorecard URL: ' + key);
      }
      index[key] = r[2];
    });
    return index;
  }
  function decodeDetail(row, meta, linkIndex) {
    var l = lookups(meta), urls = linkIndex || Object.create(null);
    var real = row[3].map(function (r) {
      return { serieARound: r[0], voteState: VoteState[r[1]],
               vote: r.length > 2 ? r[2] : null,
               sourceEvents: pairs(r.length > 3 ? r[3] : null, meta.eventNames) };
    });
    var fantasy = row[4].map(function (r) {
      var comp = l.competitions[r[1]], url = urls[r[0] + '|' + r[5]];
      return {
        matchId: r[0], competitionId: r[1], competition: comp[1], family: comp[2],
        competitionRound: r[2],
        roundDescription: r[3] === null ? null : meta.roundDescriptions[r[3]],
        serieARound: r[4], teamId: r[5], team: l.teams[r[5]],
        usage: Usage[r[6]], initialStarter: r[7] === 1,
        tabellinoVote: r[8], fantasyVote: r[9], sourceEvents: pairs(r[10], meta.eventNames),
        scorecardUrl: url === undefined ? null : url,
        position: r[11], entered: r[12], enteredAsReserve: r[13],
        firstEleven: r[14], modifier: r[15],
        usageEvidence: r[16] === null ? null : meta.usageEvidence[r[16]]
      };
    });
    return {seasonId: meta.seasonId, localFcaId: row[0], gazzaCode: row[1],
            name: row[2], realDays: real, fantasy: fantasy};
  }
  function dataStore() {
    return global.fcmRecordsNextTopFlopData || (global.fcmRecordsNextTopFlopData = {});
  }
  function normalizeBase(base) { return String(base || '.').replace(/\/+$/, ''); }
  function loadScript(url) {
    if (pending[url]) return pending[url];
    pending[url] = new Promise(function (resolve, reject) {
      var element = document.createElement('script');
      element.async = true;
      element.src = url;
      element.onload = function () { resolve(); };
      element.onerror = function () { delete pending[url]; reject(new Error('JS load failed: ' + url)); };
      document.head.appendChild(element);
    });
    return pending[url];
  }
  function loadFiles(base, files) {
    return Promise.all(files.map(function (name) {
      return loadScript(normalizeBase(base) + '/' + name);
    }));
  }
  function loadFacade(base) {
    if (global.fcmRecordsNextTopFlop) return Promise.resolve(facade());
    return loadFiles(base, ['fcmRecordsNext_TopFlop.js']).then(facade);
  }
  function loadSummaries(base, season) {
    return loadFacade(base).then(function () {
      var md = seasonMetadata(season);
      return loadFiles(base, md.summaryFiles).then(function () {
        return (dataStore().summaries || {})[season] || [];
      });
    });
  }
  function loadLinks(base, season) {
    return loadFacade(base).then(function () {
      var md = seasonMetadata(season);
      return loadFiles(base, md.linksFiles).then(function () {
        return indexLinks((dataStore().links || {})[season] || []);
      });
    });
  }
  function loadPlayer(base, season, localFcaId) {
    return loadSummaries(base, season).then(function (rows) {
      var row = rows.find(function (r) { return r[0] === localFcaId; });
      if (!row) return null;
      var md = seasonMetadata(season);
      return Promise.all([loadFiles(base, [md.detailFiles[row[3]]]), loadLinks(base, season)])
        .then(function (result) {
          var details = (dataStore().details || {})[season] || [];
          var player = details.find(function (r) { return r[0] === localFcaId; });
          if (!player) throw new Error('Missing player from declared detail shard');
          return decodeDetail(player, md, result[1]);
        });
    });
  }
  global.RecordsNextTopFlopCompactV2 = Object.freeze({
    loadFacade: loadFacade, loadSummaries: loadSummaries, loadLinks: loadLinks,
    loadPlayer: loadPlayer, seasonMetadata: seasonMetadata,
    decodeSummary: decodeSummary, decodeDetail: decodeDetail, indexLinks: indexLinks
  });
}(window));
