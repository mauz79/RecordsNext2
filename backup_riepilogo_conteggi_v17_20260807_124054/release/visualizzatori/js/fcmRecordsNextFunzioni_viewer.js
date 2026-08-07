(function (global, doc) {
  'use strict';

  var C = global.RecordsNextCommon;
  var familyGlobals = {
    classics: ['fcmRecordsNextClassics'],
    series: ['fcmRecordsNextSeries'],
    ru: ['fcmRecordsNextRU'],
    modifiers: ['fcmRecordsNextModifiers'],
    thresholdsLuck: ['fcmRecordsNextThresholdsLuck'],
    culometro: ['fcmRecordsNextCulometro']
  };

  var state = {
    familyId: '', data: null, views: [], section: '', rows: [], filtered: [],
    sortKey: '', sortDirection: -1
  };

  function findGlobal(names) {
    for (var i = 0; i < names.length; i += 1) {
      if (global[names[i]] !== undefined) return global[names[i]];
    }
    return null;
  }

  function familyData(id) { return findGlobal(familyGlobals[id] || []); }
  function manifest() { return findGlobal(['fcmRecordsNextManifest']) || {}; }
  function core() { return findGlobal(['fcmRecordsNextCore']) || {}; }

  function copyRow(row, context) {
    var result = {};
    Object.keys(context || {}).forEach(function (key) { result[key] = context[key]; });
    if (row && typeof row === 'object' && !Array.isArray(row)) {
      Object.keys(row).forEach(function (key) { result[key] = row[key]; });
    } else {
      result.valore = row;
    }
    return result;
  }

  function addView(map, id, label, rows, order) {
    if (!rows || !rows.length) return;
    if (!map[id]) map[id] = { id: id, label: label, rows: [], order: order || 100 };
    Array.prototype.push.apply(map[id].rows, rows);
  }

  function nestedRecordViews(data) {
    var map = {};
    C.asArray(data.seasonAggregates).forEach(function (aggregate) {
      var records = aggregate && aggregate.data && aggregate.data.records;
      if (!records || typeof records !== 'object') return;
      Object.keys(records).forEach(function (recordId) {
        var rows = C.asArray(records[recordId]).map(function (row) {
          var competitionId = aggregate.competizioneId || aggregate.id || '';
          var competitionName = aggregate.competizioneNome || aggregate.competitionName || aggregate.competizione || competitionId;
          return copyRow(row, {
            stagione: aggregate.stagione || aggregate.seasonId || '',
            competizioneId: competitionId,
            competizioneNome: competitionName
          });
        });
        addView(map, recordId, C.humanize(recordId), rows, 10);
      });
    });
    return Object.keys(map).map(function (key) { return map[key]; });
  }


  function competitionNameFromCore(seasonId, competitionId, fallback) {
    if (fallback && String(fallback).trim()) return fallback;
    var source = String(competitionId || '').trim();
    if (!source) return '';

    var rows = C.asArray(core().seasonCompetitions);
    for (var i = 0; i < rows.length; i += 1) {
      var row = rows[i] || {};
      if (seasonId && String(row.seasonId || '') !== String(seasonId)) continue;

      var candidates = [
        row.sourceCompetitionId,
        row.competitionSeasonId,
        row.canonicalCompetitionId,
        row.normalizedName,
        row.sourceName,
        row.canonicalName
      ].map(function (value) { return String(value === undefined || value === null ? '' : value); });

      if (candidates.indexOf(source) >= 0) {
        return row.canonicalName || row.sourceName || source;
      }
    }

    var normalized = source.toLowerCase().replace(/[_-]+/g, ' ').trim();
    for (var j = 0; j < rows.length; j += 1) {
      var candidate = rows[j] || {};
      var names = [candidate.normalizedName, candidate.sourceName, candidate.canonicalName]
        .map(function (value) { return String(value || '').toLowerCase().replace(/[_-]+/g, ' ').trim(); });
      if (names.indexOf(normalized) >= 0) {
        return candidate.canonicalName || candidate.sourceName || source;
      }
    }
    return C.humanize(source);
  }

  function enrichCompetition(row) {
    var season = C.firstExisting(row || {}, ['stagione','seasonId'], '');
    var id = C.firstExisting(row || {}, ['competizioneId','competitionId','competizioneStoricaId','competizione'], '');
    var name = C.firstExisting(row || {}, ['competizioneNome','competitionName'], '');
    return copyRow(row, {
      stagione: season,
      competizioneId: id,
      competizioneNome: competitionNameFromCore(season, id, name)
    });
  }

  function normalizeCompetition(row, aggregate) {
    var id = C.firstExisting(row || {}, ['competizioneId','competitionId','competizioneStoricaId'], '');
    var name = C.firstExisting(row || {}, ['competizioneNome','competitionName','competizione'], '');
    if (!id) id = C.firstExisting(aggregate || {}, ['competizioneId','competitionId','id'], '');
    if (!name) name = C.firstExisting(aggregate || {}, ['competizioneNome','competitionName','competizione'], '');
    if (!name && id) name = C.italianLabel(id);
    return { competizioneId: id, competizioneNome: name };
  }

  function ruViews(data) {
    var map = {};
    C.asArray(data.seasonAggregates).forEach(function (aggregate) {
      var payload = aggregate.data || {};
      ['views', 'dettaglio'].forEach(function (group) {
        var source = payload[group];
        if (!source || typeof source !== 'object') return;
        Object.keys(source).forEach(function (viewId) {
          var rows = C.asArray(source[viewId]).map(function (row) {
            var competition = normalizeCompetition(row, aggregate);
            return copyRow(row, {
              stagione: aggregate.stagione || aggregate.seasonId || '',
              competizioneId: competition.competizioneId,
              competizioneNome: competition.competizioneNome
            });
          });
          addView(map, group + '.' + viewId, C.italianLabel(viewId), rows, group === 'views' ? 10 : 30);
        });
      });
      if (Array.isArray(payload.curiosita) && payload.curiosita.length) {
        addView(map, 'curiosita', 'Curiosità', payload.curiosita.map(function (row) {
          var competition = normalizeCompetition(row, aggregate);
          return copyRow(row, {
            stagione: aggregate.stagione || aggregate.seasonId || '',
            competizioneId: competition.competizioneId,
            competizioneNome: competition.competizioneNome
          });
        }), 40);
      }
    });
    return Object.keys(map).map(function (key) { return map[key]; });
  }

  function thresholdViews(data) {
    var views = [];
    var seasonRows = C.asArray(data.seasonAggregates).map(function (row) {
      return enrichCompetition(copyRow(row, {
        stagione: row.seasonId || row.stagione || '',
        squadra: row.team || row.squadra || ''
      }));
    });
    if (seasonRows.length) views.push({ id: 'classificaStagionale', label: 'Classifica per stagione e squadra', rows: seasonRows, order: 10 });

    var totals = {};
    seasonRows.forEach(function (row) {
      var key = row.teamId || row.squadra || row.team || '';
      if (!key) return;
      if (!totals[key]) totals[key] = { squadra: row.squadra || row.team || '', eventiFavorevoli: 0, eventiSfavorevoli: 0, eventiNeutrali: 0, saldoFortunaSfortuna: 0, puntiSprecati: 0, stagioni: 0 };
      totals[key].eventiFavorevoli += Number(row.favourableEvents || 0);
      totals[key].eventiSfavorevoli += Number(row.unfavourableEvents || 0);
      totals[key].eventiNeutrali += Number(row.neutralEvents || 0);
      totals[key].saldoFortunaSfortuna += Number(row.luckBalance || 0);
      totals[key].puntiSprecati += Number(row.unusedBandPoints || 0);
      totals[key].stagioni += 1;
    });
    var storico = Object.keys(totals).map(function (key) { return totals[key]; });
    storico.sort(function (a, b) { return b.saldoFortunaSfortuna - a.saldoFortunaSfortuna || b.eventiFavorevoli - a.eventiFavorevoli; });
    if (storico.length) views.push({ id: 'classificaStorica', label: 'Classifica storica per squadra', rows: storico, order: 20 });

    var byType = {};
    C.asArray(data.events).forEach(function (event) {
      var key = event.eventType || 'ALTRO';
      if (!byType[key]) byType[key] = { tipoEvento: C.italianLabel(key), occorrenze: 0, favorevoli: 0, sfavorevoli: 0, neutrali: 0 };
      byType[key].occorrenze += 1;
      if (event.direction === 'FAVOURABLE') byType[key].favorevoli += 1;
      else if (event.direction === 'UNFAVOURABLE') byType[key].sfavorevoli += 1;
      else byType[key].neutrali += 1;
    });
    var tipi = Object.keys(byType).map(function (key) { return byType[key]; });
    tipi.sort(function (a, b) { return b.occorrenze - a.occorrenze; });
    if (tipi.length) views.push({ id: 'tipiEvento', label: 'Riepilogo dei tipi di evento', rows: tipi, order: 30 });

    var eventi = C.asArray(data.events).map(function (row) {
      return copyRow(row, {
        stagione: row.seasonId || row.stagione || '',
        competizioneId: row.competitionId || row.competizioneId || '',
        competizioneNome: row.competitionName || row.competizioneNome || row.competizione || '',
        squadra: row.team || row.squadra || ''
      });
    });
    if (eventi.length) views.push({ id: 'dettaglioEventi', label: 'Dettaglio degli eventi', rows: eventi, order: 40 });
    return views;
  }

  function numericValue(row) {
    var keys = ['valore','value','totale','total','occorrenze','matches','partite','serie','lunghezza','puntiClassificaGuadagnati','puntiClassificaPersi','numeroRU','valoreRUTotale','index','score'];
    for (var i = 0; i < keys.length; i += 1) {
      var value = C.getPath(row, keys[i]);
      if (value !== undefined && value !== null && value !== '' && !isNaN(Number(value))) return Number(value);
    }
    return null;
  }

  function allGeneratedViews() {
    var families = ['classics','series','ru','modifiers','thresholdsLuck','culometro'];
    var result = [];
    families.forEach(function (familyId) {
      var data = familyData(familyId);
      if (!data) return;
      var views;
      if (familyId === 'classics' || familyId === 'series' || familyId === 'modifiers') views = nestedRecordViews(data);
      else if (familyId === 'ru') views = ruViews(data);
      else if (familyId === 'thresholdsLuck') views = thresholdViews(data);
      else views = directViews(data);
      views.forEach(function (view) { result.push({ familyId: familyId, familyLabel: C.italianLabel(familyId), view: view }); });
    });
    return result;
  }

  function leagueRecordViews() {
    var rows = [];
    allGeneratedViews().forEach(function (entry) {
      var candidates = entry.view.rows.map(function (row) {
        return { row: row, score: numericValue(row) };
      }).filter(function (item) { return item.score !== null; });
      if (!candidates.length) return;
      candidates.sort(function (a, b) { return b.score - a.score; });

      var bySeasonCompetition = {};
      candidates.forEach(function (item) {
        var season = C.firstExisting(item.row, ['stagione','seasonId'], '');
        var competition = C.firstExisting(item.row, ['competizioneNome','competitionName','competizione','competizioneId'], 'Tutte le competizioni');
        var key = season + '|' + competition;
        if (!bySeasonCompetition[key]) bySeasonCompetition[key] = item;
      });

      Object.keys(bySeasonCompetition).forEach(function (key) {
        var item = bySeasonCompetition[key];
        rows.push(copyRow(item.row, {
          famiglia: entry.familyLabel,
          record: entry.view.label,
          ambito: 'Migliore della stagione',
          valoreRecord: item.score
        }));
      });

      var best = candidates[0];
      rows.push(copyRow(best.row, {
        famiglia: entry.familyLabel,
        record: entry.view.label,
        ambito: 'Migliore assoluto',
        valoreRecord: best.score
      }));
    });
    rows.sort(function (a, b) {
      return String(a.famiglia).localeCompare(String(b.famiglia), 'it') || String(a.record).localeCompare(String(b.record), 'it') || Number(b.valoreRecord || 0) - Number(a.valoreRecord || 0);
    });
    return [{ id: 'recordDiLega', label: 'Record di lega', rows: rows, order: 10 }];
  }

  function directViews(data) {
    var labels = {
      ranking: 'Classifica generale',
      competitionRanking: 'Classifica per competizione',
      events: 'Eventi',
      seasonAggregates: 'Riepilogo per stagione e squadra',
      globalAggregates: 'Riepilogo globale',
      absoluteOccurrences: 'Occorrenze assolute',
      configuration: 'Configurazione'
    };
    var order = ['ranking', 'events', 'seasonAggregates', 'globalAggregates', 'absoluteOccurrences', 'configuration'];
    var views = [];
    order.forEach(function (key, index) {
      var value = data[key];
      var rows = [];
      if (Array.isArray(value)) rows = value;
      else if (value && typeof value === 'object') {
        rows = Object.keys(value).map(function (name) {
          return copyRow(value[name], { parametro: name });
        });
      }
      if (rows.length) views.push({ id: key, label: labels[key] || C.humanize(key), rows: rows, order: index });
    });
    return views;
  }

  function buildViews(id, data) {
    var views;
    if (id === 'classics' || id === 'series' || id === 'modifiers') views = nestedRecordViews(data);
    else if (id === 'ru') views = ruViews(data);
    else if (id === 'thresholdsLuck') views = thresholdViews(data);
    else if (id === 'league') views = leagueRecordViews();
    else views = directViews(data);
    return views.sort(function (a, b) { return a.order - b.order || a.label.localeCompare(b.label, 'it'); });
  }

  function preferredColumns(rows) {
    var priority = [
      'position','rank','ambito','famiglia','record','stagione','seasonId','competizioneNome','competitionName','competizioneId','competizione','girone',
      'recordId','nome','eventType','direction','label','squadra','team','opponent','avversaria',
      'valoreRecord','valore','value','occorrenze','eventiFavorevoli','eventiSfavorevoli','saldoFortunaSfortuna','puntiSprecati','index','perMatch','matches','partite','giornata','round','giornataDiA','serieARound',
      'risultato','result','punteggio','scoreFor','scoreAgainst','detail','dettaglioRU',
      'urlTabellino','scorecardUrl','matchUrl'
    ];
    var keys = [];
    rows.slice(0, 250).forEach(function (row) {
      Object.keys(C.flattenObject(row)).forEach(function (key) {
        if (keys.indexOf(key) < 0) keys.push(key);
      });
    });
    keys.sort(function (a, b) {
      var ai = priority.indexOf(a), bi = priority.indexOf(b);
      if (ai < 0) ai = 999;
      if (bi < 0) bi = 999;
      return ai - bi || a.localeCompare(b, 'it');
    });
    return keys.slice(0, 16);
  }

  function matchUrl(row) {
    return C.firstExisting(row, [
      'urlTabellino','scorecardUrl','matchUrl','tabellinoUrl','url',
      'details.matchUrl','dettagli.linkTabellino','dettagli.url'
    ], '');
  }

  function renderCell(row, key) {
    var value = C.getPath(row, key);
    var url = matchUrl(row);
    var lower = key.toLowerCase();
    if (url && (lower.indexOf('url') >= 0 || lower.indexOf('tabellino') >= 0 || lower.indexOf('scorecard') >= 0)) {
      return '<a class="rn-match-link" href="' + C.escapeHtml(url) + '" target="_blank" rel="noopener">Tabellino</a>';
    }
    return C.escapeHtml(C.formatValue(value));
  }

  function renderTable(rows) {
    var host = doc.getElementById('rn-results');
    if (!rows.length) {
      host.innerHTML = '<div class="rn-empty-state"><strong>Nessun dato disponibile.</strong><span>La vista selezionata non contiene righe visualizzabili.</span></div>';
      return;
    }
    var columns = preferredColumns(rows);
    var html = '<div class="rn-table-wrap"><table class="rn-table"><thead><tr>';
    columns.forEach(function (column) {
      var marker = state.sortKey === column ? (state.sortDirection > 0 ? ' ▲' : ' ▼') : '';
      html += '<th data-sort="' + C.escapeHtml(column) + '">' + C.escapeHtml(C.humanize(column) + marker) + '</th>';
    });
    html += '</tr></thead><tbody>';
    rows.forEach(function (row) {
      html += '<tr>';
      columns.forEach(function (column) { html += '<td>' + renderCell(row, column) + '</td>'; });
      html += '</tr>';
    });
    html += '</tbody></table></div>';
    host.innerHTML = html;
    host.querySelectorAll('th[data-sort]').forEach(function (th) {
      th.addEventListener('click', function () { sortBy(th.getAttribute('data-sort')); });
    });
  }

  function populateContextFilters(rows) {
    var season = doc.getElementById('rn-season');
    var competition = doc.getElementById('rn-competition');
    if (!season || !competition) return;
    var seasons = {}, competitions = {};
    rows.forEach(function (row) {
      var s = C.firstExisting(row, ['stagione','seasonId'], '');
      var c = C.firstExisting(row, ['competizioneNome','competitionName','competizione','girone','competizioneId'], '');
      if (s) seasons[s] = true;
      if (c) competitions[c] = true;
    });
    season.innerHTML = '<option value="">Tutte le stagioni</option>' + Object.keys(seasons).sort().map(function (s) {
      return '<option value="' + C.escapeHtml(s) + '">' + C.escapeHtml(s.replace('_','/')) + '</option>';
    }).join('');
    competition.innerHTML = '<option value="">Tutte le competizioni</option>' + Object.keys(competitions).sort(function(a,b){return a.localeCompare(b,'it');}).map(function (c) {
      return '<option value="' + C.escapeHtml(c) + '">' + C.escapeHtml(c) + '</option>';
    }).join('');
    competition.disabled = Object.keys(competitions).length === 0;

    var eventLabel = doc.getElementById('rn-event-type-label');
    var eventSelect = doc.getElementById('rn-event-type');
    if (eventLabel && eventSelect) {
      var eventTypes = {};
      rows.forEach(function (row) {
        var eventType = C.firstExisting(row, ['eventType','tipoEvento'], '');
        if (eventType) eventTypes[eventType] = true;
      });
      var eventKeys = Object.keys(eventTypes).sort(function (a, b) {
        var al = C.italianLabel ? C.italianLabel(a) : C.humanize(a);
        var bl = C.italianLabel ? C.italianLabel(b) : C.humanize(b);
        return al.localeCompare(bl, 'it');
      });
      eventSelect.innerHTML = '<option value="">Tutti gli eventi</option>' + eventKeys.map(function (eventType) {
        var label = C.italianLabel ? C.italianLabel(eventType) : C.humanize(eventType);
        return '<option value="' + C.escapeHtml(eventType) + '">' + C.escapeHtml(label) + '</option>';
      }).join('');
      eventLabel.hidden = eventKeys.length === 0;
      eventSelect.disabled = eventKeys.length === 0;
    }
  }

  function applyFilter() {
    var q = (doc.getElementById('rn-search').value || '').trim().toLowerCase();
    var season = doc.getElementById('rn-season') ? doc.getElementById('rn-season').value : '';
    var competition = doc.getElementById('rn-competition') ? doc.getElementById('rn-competition').value : '';
    var eventType = doc.getElementById('rn-event-type') ? doc.getElementById('rn-event-type').value : '';
    state.filtered = state.rows.filter(function (row) {
      var rowSeason = C.firstExisting(row, ['stagione','seasonId'], '');
      var rowCompetition = C.firstExisting(row, ['competizioneNome','competitionName','competizione','girone','competizioneId'], '');
      if (season && rowSeason !== season) return false;
      if (competition && rowCompetition !== competition) return false;
      var rowEventType = C.firstExisting(row, ['eventType','tipoEvento'], '');
      if (eventType && rowEventType !== eventType) return false;
      return !q || JSON.stringify(row).toLowerCase().indexOf(q) >= 0;
    });
    doc.getElementById('rn-count').textContent = state.filtered.length + ' righe';
    renderTable(state.filtered);
  }

  function sortBy(key) {
    if (state.sortKey === key) state.sortDirection *= -1;
    else { state.sortKey = key; state.sortDirection = -1; }
    state.filtered.sort(function (a, b) {
      var av = C.getPath(a, key), bv = C.getPath(b, key), cmp;
      if (typeof av === 'number' && typeof bv === 'number') cmp = av - bv;
      else cmp = C.formatValue(av).localeCompare(C.formatValue(bv), 'it', { numeric: true });
      return cmp * state.sortDirection;
    });
    renderTable(state.filtered);
  }

  function setSection(name) {
    state.section = name;
    var view = state.views.filter(function (item) { return item.id === name; })[0];
    state.rows = view ? view.rows.slice() : [];
    state.filtered = state.rows.slice();
    state.sortKey = '';
    doc.getElementById('rn-search').value = '';
    populateContextFilters(state.rows);
    doc.getElementById('rn-count').textContent = state.rows.length + ' righe';
    renderTable(state.rows);
  }

  function manifestGeneratedAt(m) {
    return m.generatedAt || (m.metadata && m.metadata.generatedAt) || (m.meta && m.meta.generatedAt) || '—';
  }

  function currentSeason(m, c) {
    return m.currentSeasonId || (m.metadata && m.metadata.currentSeasonId) || c.currentSeasonId ||
      (c.seasons && c.seasons.length ? C.firstExisting(c.seasons[c.seasons.length - 1], ['seasonId','stagione'], '—') : '—');
  }

  function renderMetadata() {
    var m = manifest(), meta = state.data.metadata || {}, c = core();
    var items = [
      ['Schema', state.data.schemaVersion || m.schemaVersion || '—'],
      ['Generato', manifestGeneratedAt(m)],
      ['Stagione corrente', currentSeason(m, c)],
      ['Famiglia', state.data.familyId || state.familyId],
      ['Viste disponibili', state.views.length]
    ];
    doc.getElementById('rn-meta').innerHTML = items.map(function (item) {
      return '<div class="rn-meta-item"><span>' + C.escapeHtml(item[0]) + '</span><strong>' + C.escapeHtml(item[1]) + '</strong></div>';
    }).join('');
  }

  function renderStatus() {
    var statuses = C.asArray(state.data.outputStatus);
    var host = doc.getElementById('rn-status');
    if (!statuses.length) { host.innerHTML = ''; return; }
    host.innerHTML = statuses.map(function (item) {
      var status = item.status || 'INFO';
      var cls = status.indexOf('FAILED') >= 0 ? 'is-error' :
        ((status.indexOf('SKIPPED') >= 0 || status.indexOf('PARTIAL') >= 0) ? 'is-warning' : 'is-ok');
      return '<div class="rn-status-row ' + cls + '"><strong>' + C.escapeHtml(item.childId || status) + '</strong><span>' + C.escapeHtml(item.detail || item.message || status) + '</span></div>';
    }).join('');
  }

  function initFamilyPage() {
    state.familyId = doc.body.getAttribute('data-family') || '';
    state.data = state.familyId === 'league' ? { schemaVersion: '2.0', familyId: 'league', metadata: {}, outputStatus: [] } : familyData(state.familyId);
    var missing = doc.getElementById('rn-missing');
    var app = doc.getElementById('rn-app');
    if (!state.data) {
      app.hidden = true;
      missing.hidden = false;
      return;
    }
    state.views = buildViews(state.familyId, state.data);
    missing.hidden = true;
    app.hidden = false;
    renderMetadata();
    renderStatus();
    var select = doc.getElementById('rn-section');
    select.innerHTML = state.views.map(function (view) {
      return '<option value="' + C.escapeHtml(view.id) + '">' + C.escapeHtml(view.label) + ' (' + view.rows.length + ')</option>';
    }).join('');
    if (!state.views.length) {
      doc.getElementById('rn-results').innerHTML = '<div class="rn-empty-state"><strong>Dati presenti ma nessuna vista valorizzata.</strong></div>';
      return;
    }
    select.addEventListener('change', function () { setSection(select.value); });
    doc.getElementById('rn-search').addEventListener('input', applyFilter);
    doc.getElementById('rn-season').addEventListener('change', applyFilter);
    doc.getElementById('rn-competition').addEventListener('change', applyFilter);
    var eventTypeSelect = doc.getElementById('rn-event-type');
    if (eventTypeSelect) eventTypeSelect.addEventListener('change', applyFilter);
    doc.getElementById('rn-export').addEventListener('click', function () {
      C.downloadCsv('recordsnext_' + state.familyId + '_' + state.section.replace(/\./g, '_') + '.csv', state.filtered);
    });
    setSection(state.views[0].id);
  }

  function manifestFamilyStatus(m, id) {
    var families = m.families || m.outputs || [];
    if (Array.isArray(families)) {
      for (var i = 0; i < families.length; i += 1) {
        if (families[i].familyId === id || families[i].id === id) return families[i].status || '';
      }
    }
    return '';
  }

  function initIndex() {
    var m = manifest();
    var generated = manifestGeneratedAt(m);
    doc.getElementById('rn-index-meta').textContent = generated !== '—' ? ('Dati generati: ' + generated) : 'Manifest non disponibile';
    doc.querySelectorAll('[data-family-card]').forEach(function (card) {
      var id = card.getAttribute('data-family-card');
      var data = familyData(id);
      var available = !!data;
      var status = data && data.outputStatus && data.outputStatus[0] ? data.outputStatus[0].status : manifestFamilyStatus(m, id);
      card.classList.toggle('is-disabled', !available);
      var badge = card.querySelector('.rn-availability');
      badge.textContent = !available ? 'Non generato' : (status && status.indexOf('PARTIAL') >= 0 ? 'Parziale' : 'Disponibile');
      badge.className = 'rn-availability ' + (!available ? 'is-off' : (status && status.indexOf('PARTIAL') >= 0 ? 'is-warning' : 'is-ok'));
      var link = card.querySelector('a');
      if (!available) link.setAttribute('aria-disabled', 'true');
    });
  }

  doc.addEventListener('DOMContentLoaded', function () {
    if (doc.body.hasAttribute('data-family')) initFamilyPage();
    else initIndex();
  });
}(window, document));
