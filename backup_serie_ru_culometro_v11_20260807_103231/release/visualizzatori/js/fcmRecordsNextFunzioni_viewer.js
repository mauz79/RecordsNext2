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

  var HIDDEN_COLUMNS = {
    team: true, teamId: true, squadra: true, squadraId: true,
    seasonId: true, competitionId: true, competizioneId: true,
    recordId: true, familyId: true, schemaVersion: true, outputStatus: true,
    sourceCompetitionId: true, canonicalCompetitionId: true,
    competitionSeasonId: true, id: true
  };

  var COLUMN_ORDER = [
    'ambito','famiglia','record','stagione','competizioneNome',
    'squadraNome','avversariaNome','partita','giornata','risultato',
    'valoreRecord','valore','occorrenze','eventiFavorevoli',
    'eventiSfavorevoli','eventiNeutrali','saldoFortunaSfortuna',
    'puntiSprecati','dettaglioRU','dettaglio','tabellino'
  ];

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

  function competitionNameFromCore(seasonId, competitionId, fallback) {
    if (fallback && String(fallback).trim()) return String(fallback);
    var id = String(competitionId || '').trim();
    if (!id) return '';

    var entries = C.asArray(core().seasonCompetitions);
    var normalizedId = id.toLowerCase().replace(/[_-]+/g, ' ').trim();

    for (var i = 0; i < entries.length; i += 1) {
      var row = entries[i] || {};
      if (seasonId && row.seasonId && String(row.seasonId) !== String(seasonId)) continue;
      var values = [
        row.sourceCompetitionId, row.competitionSeasonId, row.canonicalCompetitionId,
        row.sourceName, row.canonicalName, row.normalizedName
      ].map(function (value) {
        return String(value === undefined || value === null ? '' : value)
          .toLowerCase().replace(/[_-]+/g, ' ').trim();
      });
      if (values.indexOf(normalizedId) >= 0) {
        return row.canonicalName || row.sourceName || C.humanize(id);
      }
    }
    return C.humanize(id);
  }

  function normalizeRow(row, context) {
    var source = copyRow(row, context || {});
    var season = C.firstExisting(source, ['stagione','seasonId'], '');
    var competitionId = C.firstExisting(source, [
      'competizioneId','competitionId','competizioneStoricaId'
    ], '');
    var competitionName = C.firstExisting(source, [
      'competizioneNome','competitionName','competizione'
    ], '');
    var teamName = C.firstExisting(source, [
      'squadraNome','squadra','team','nomeSquadra','fantasquadra'
    ], '');
    var opponentName = C.firstExisting(source, [
      'avversariaNome','avversaria','opponent','opponentName'
    ], '');

    source.stagione = season;
    source.competizioneNome = competitionNameFromCore(season, competitionId || competitionName, competitionName);
    if (teamName) source.squadraNome = teamName;
    if (opponentName) source.avversariaNome = opponentName;

    var url = C.firstExisting(source, [
      'urlTabellino','scorecardUrl','matchUrl','tabellinoUrl','url',
      'details.matchUrl','dettagli.linkTabellino','dettagli.url'
    ], '');
    if (url) source.tabellino = url;

    delete source.team;
    delete source.teamId;
    delete source.squadra;
    delete source.squadraId;
    delete source.seasonId;
    delete source.competitionId;
    delete source.competizioneId;
    delete source.competitionName;
    delete source.recordId;
    delete source.familyId;
    delete source.schemaVersion;
    return source;
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
        var context = {
          stagione: aggregate.stagione || aggregate.seasonId || '',
          competizioneId: aggregate.competizioneId || aggregate.id || '',
          competizioneNome: aggregate.competizioneNome || aggregate.competitionName || aggregate.competizione || ''
        };
        var rows = C.asArray(records[recordId]).map(function (row) {
          return normalizeRow(row, context);
        });
        addView(map, recordId, C.italianLabel(recordId), rows, 10);
      });
    });
    return Object.keys(map).map(function (key) { return map[key]; });
  }

  function ruViews(data) {
    var map = {};
    C.asArray(data.seasonAggregates).forEach(function (aggregate) {
      var payload = aggregate && aggregate.data ? aggregate.data : {};
      ['views','dettaglio'].forEach(function (group) {
        var source = payload[group];
        if (!source || typeof source !== 'object') return;
        Object.keys(source).forEach(function (viewId) {
          var rows = C.asArray(source[viewId]).map(function (row) {
            return normalizeRow(row, {
              stagione: aggregate.stagione || aggregate.seasonId || '',
              competizioneId: aggregate.competizioneId || aggregate.id || '',
              competizioneNome: aggregate.competizioneNome || ''
            });
          });
          addView(map, group + '.' + viewId, C.italianLabel(viewId), rows, group === 'views' ? 10 : 20);
        });
      });
      C.asArray(payload.curiosita).forEach(function (row) {
        addView(map, 'curiosita', 'Curiosità', [
          normalizeRow(row, { stagione: aggregate.stagione || aggregate.seasonId || '' })
        ], 30);
      });
    });
    return Object.keys(map).map(function (key) { return map[key]; });
  }

  function thresholdViews(data) {
    var views = [];
    var seasonRows = C.asArray(data.seasonAggregates).map(function (row) {
      return normalizeRow(row, {
        stagione: row.seasonId || row.stagione || ''
      });
    });

    if (seasonRows.length) {
      views.push({
        id: 'classifica_stagionale',
        label: 'Classifica per stagione e squadra',
        rows: seasonRows,
        order: 10
      });
    }

    var totals = {};
    seasonRows.forEach(function (row) {
      var key = row.squadraNome || '';
      if (!key) return;
      if (!totals[key]) {
        totals[key] = {
          squadraNome: key,
          eventiFavorevoli: 0,
          eventiSfavorevoli: 0,
          eventiNeutrali: 0,
          saldoFortunaSfortuna: 0,
          puntiSprecati: 0
        };
      }
      totals[key].eventiFavorevoli += Number(row.favourableEvents || row.eventiFavorevoli || 0);
      totals[key].eventiSfavorevoli += Number(row.unfavourableEvents || row.eventiSfavorevoli || 0);
      totals[key].eventiNeutrali += Number(row.neutralEvents || row.eventiNeutrali || 0);
      totals[key].saldoFortunaSfortuna += Number(row.luckBalance || row.saldoFortunaSfortuna || 0);
      totals[key].puntiSprecati += Number(row.unusedBandPoints || row.puntiSprecati || 0);
    });

    var historical = Object.keys(totals).map(function (key) { return totals[key]; });
    historical.sort(function (a, b) {
      return b.saldoFortunaSfortuna - a.saldoFortunaSfortuna ||
        b.eventiFavorevoli - a.eventiFavorevoli ||
        a.squadraNome.localeCompare(b.squadraNome, 'it');
    });
    if (historical.length) {
      views.push({
        id: 'classifica_storica',
        label: 'Classifica storica per squadra',
        rows: historical,
        order: 20
      });
    }

    var byType = {};
    C.asArray(data.events).forEach(function (event) {
      var key = event.eventType || 'ALTRO';
      if (!byType[key]) {
        byType[key] = {
          tipoEvento: C.italianLabel(key),
          occorrenze: 0,
          eventiFavorevoli: 0,
          eventiSfavorevoli: 0,
          eventiNeutrali: 0
        };
      }
      byType[key].occorrenze += 1;
      if (event.direction === 'FAVOURABLE') byType[key].eventiFavorevoli += 1;
      else if (event.direction === 'UNFAVOURABLE') byType[key].eventiSfavorevoli += 1;
      else byType[key].eventiNeutrali += 1;
    });
    var typeRows = Object.keys(byType).map(function (key) { return byType[key]; });
    typeRows.sort(function (a, b) { return b.occorrenze - a.occorrenze; });
    if (typeRows.length) {
      views.push({
        id: 'riepilogo_eventi',
        label: 'Riepilogo dei tipi di evento',
        rows: typeRows,
        order: 30
      });
    }

    var eventRows = C.asArray(data.events).map(function (row) {
      return normalizeRow(row, {
        stagione: row.seasonId || '',
        competizioneId: row.competitionId || '',
        competizioneNome: row.competitionName || ''
      });
    });
    if (eventRows.length) {
      views.push({
        id: 'dettaglio_eventi',
        label: 'Dettaglio degli eventi',
        rows: eventRows,
        order: 40
      });
    }
    return views;
  }

  function directViews(data) {
    var labels = {
      ranking: 'Classifica',
      events: 'Eventi',
      seasonAggregates: 'Riepilogo per stagione e squadra',
      globalAggregates: 'Riepilogo globale',
      absoluteOccurrences: 'Occorrenze assolute',
      configuration: 'Configurazione'
    };
    var order = ['ranking','events','seasonAggregates','globalAggregates','absoluteOccurrences','configuration'];
    var views = [];
    order.forEach(function (key, index) {
      var value = data[key], rows = [];
      if (Array.isArray(value)) {
        rows = value.map(function (row) { return normalizeRow(row); });
      } else if (value && typeof value === 'object') {
        rows = Object.keys(value).map(function (name) {
          return normalizeRow(copyRow(value[name], { parametro: C.italianLabel(name) }));
        });
      }
      if (rows.length) views.push({ id: key, label: labels[key], rows: rows, order: index });
    });
    return views;
  }

  function numericValue(row) {
    var keys = [
      'valoreRecord','valore','value','totale','total','numeroRU','valoreRUTotale',
      'serie','lunghezza','punti','score','index','perMatch','matches','partite',
      'eventiFavorevoli','favourableEvents','saldoFortunaSfortuna','luckBalance'
    ];
    for (var i = 0; i < keys.length; i += 1) {
      var value = Number(C.getPath(row, keys[i]));
      if (Number.isFinite(value)) return value;
    }
    return null;
  }

  function allGeneratedViews() {
    var ids = ['classics','series','ru','modifiers','thresholdsLuck','culometro'];
    var result = [];
    ids.forEach(function (familyId) {
      var data = familyData(familyId);
      if (!data) return;
      var views;
      if (familyId === 'classics' || familyId === 'series' || familyId === 'modifiers') views = nestedRecordViews(data);
      else if (familyId === 'ru') views = ruViews(data);
      else if (familyId === 'thresholdsLuck') views = thresholdViews(data);
      else views = directViews(data);
      views.forEach(function (view) {
        result.push({ familyId: familyId, familyLabel: C.italianLabel(familyId), view: view });
      });
    });
    return result;
  }

  function leagueRecordViews() {
    var views = [];
    allGeneratedViews().forEach(function (entry) {
      var candidates = entry.view.rows.map(function (row) {
        return { row: normalizeRow(row), score: numericValue(row) };
      }).filter(function (item) { return item.score !== null; });
      if (!candidates.length) return;

      candidates.sort(function (a, b) { return b.score - a.score; });
      var rows = [];
      var grouped = {};

      candidates.forEach(function (item) {
        var season = item.row.stagione || '';
        var competition = item.row.competizioneNome || 'Tutte le competizioni';
        var key = season + '|' + competition;
        if (!grouped[key]) grouped[key] = item;
      });

      Object.keys(grouped).forEach(function (key) {
        var item = grouped[key];
        rows.push(normalizeRow(item.row, {
          ambito: 'Migliore della stagione',
          famiglia: entry.familyLabel,
          record: entry.view.label,
          valoreRecord: item.score
        }));
      });

      var best = candidates[0];
      rows.push(normalizeRow(best.row, {
        ambito: 'Migliore assoluto',
        famiglia: entry.familyLabel,
        record: entry.view.label,
        valoreRecord: best.score
      }));

      views.push({
        id: entry.familyId + '.' + entry.view.id,
        label: entry.familyLabel + ' — ' + entry.view.label,
        rows: rows,
        order: views.length + 1
      });
    });

    return views.sort(function (a, b) {
      return a.label.localeCompare(b.label, 'it');
    });
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
    var seen = {};
    rows.slice(0, 300).forEach(function (row) {
      Object.keys(C.flattenObject(row)).forEach(function (key) {
        var root = key.split('.')[0];
        if (!HIDDEN_COLUMNS[root]) seen[key] = true;
      });
    });

    var columns = Object.keys(seen);
    columns.sort(function (a, b) {
      var ai = COLUMN_ORDER.indexOf(a), bi = COLUMN_ORDER.indexOf(b);
      if (ai < 0) ai = 999;
      if (bi < 0) bi = 999;
      return ai - bi || a.localeCompare(b, 'it');
    });
    return columns.slice(0, 14);
  }

  function renderCell(row, key) {
    var value = C.getPath(row, key);
    if (key === 'tabellino' && value) {
      return '<a class="rn-match-link" href="' + C.escapeHtml(value) + '" target="_blank" rel="noopener">Apri</a>';
    }
    if (key === 'eventType' || key === 'direction' || key === 'tipoEvento') {
      return C.escapeHtml(C.italianLabel(value));
    }
    return C.escapeHtml(C.formatValue(value));
  }

  function renderTable(rows) {
    var host = doc.getElementById('rn-results');
    if (!rows.length) {
      host.innerHTML = '<div class="rn-empty-state"><strong>Nessun dato disponibile.</strong></div>';
      return;
    }
    var columns = preferredColumns(rows);
    var html = '<div class="rn-table-wrap"><table class="rn-table"><thead><tr>';
    columns.forEach(function (column) {
      var marker = state.sortKey === column ? (state.sortDirection > 0 ? ' ▲' : ' ▼') : '';
      html += '<th data-sort="' + C.escapeHtml(column) + '">' +
        C.escapeHtml(C.italianLabel(column) + marker) + '</th>';
    });
    html += '</tr></thead><tbody>';
    rows.forEach(function (row) {
      html += '<tr>';
      columns.forEach(function (column) {
        html += '<td>' + renderCell(row, column) + '</td>';
      });
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
      if (row.stagione) seasons[row.stagione] = true;
      if (row.competizioneNome) competitions[row.competizioneNome] = true;
    });
    season.innerHTML = '<option value="">Tutte le stagioni</option>' +
      Object.keys(seasons).sort().map(function (s) {
        return '<option value="' + C.escapeHtml(s) + '">' +
          C.escapeHtml(s.replace('_','/')) + '</option>';
      }).join('');
    competition.innerHTML = '<option value="">Tutte le competizioni</option>' +
      Object.keys(competitions).sort(function (a,b) { return a.localeCompare(b,'it'); }).map(function (c) {
        return '<option value="' + C.escapeHtml(c) + '">' + C.escapeHtml(c) + '</option>';
      }).join('');
    competition.disabled = Object.keys(competitions).length === 0;
  }

  function applyFilter() {
    var q = (doc.getElementById('rn-search').value || '').trim().toLowerCase();
    var season = doc.getElementById('rn-season') ? doc.getElementById('rn-season').value : '';
    var competition = doc.getElementById('rn-competition') ? doc.getElementById('rn-competition').value : '';
    state.filtered = state.rows.filter(function (row) {
      if (season && String(row.stagione || '') !== season) return false;
      if (competition && String(row.competizioneNome || '') !== competition) return false;
      if (q && JSON.stringify(row).toLowerCase().indexOf(q) < 0) return false;
      return true;
    });
    sortCurrent();
    doc.getElementById('rn-count').textContent = state.filtered.length + ' righe';
    renderTable(state.filtered);
  }

  function sortCurrent() {
    if (!state.sortKey) return;
    state.filtered.sort(function (a, b) {
      var av = C.getPath(a, state.sortKey);
      var bv = C.getPath(b, state.sortKey);
      var an = Number(av), bn = Number(bv);
      var cmp = Number.isFinite(an) && Number.isFinite(bn)
        ? an - bn
        : String(av || '').localeCompare(String(bv || ''), 'it');
      return cmp * state.sortDirection;
    });
  }

  function sortBy(key) {
    if (state.sortKey === key) state.sortDirection *= -1;
    else {
      state.sortKey = key;
      state.sortDirection = -1;
    }
    applyFilter();
  }

  function setSection(id) {
    state.section = id;
    var view = state.views.filter(function (item) { return item.id === id; })[0];
    state.rows = view ? view.rows.map(function (row) { return normalizeRow(row); }) : [];
    state.sortKey = '';
    populateContextFilters(state.rows);
    applyFilter();
  }

  function renderMetadata() {
    var host = doc.getElementById('rn-meta');
    if (!host) return;
    var metadata = state.data && state.data.metadata ? state.data.metadata : {};
    var cards = [];
    Object.keys(metadata).forEach(function (key) {
      var value = metadata[key];
      if (typeof value === 'object') return;
      cards.push('<article><strong>' + C.escapeHtml(C.italianLabel(key)) +
        '</strong><span>' + C.escapeHtml(C.formatValue(value)) + '</span></article>');
    });
    host.innerHTML = cards.join('');
  }

  function renderStatus() {
    var host = doc.getElementById('rn-status');
    if (!host) return;
    var statuses = C.asArray(state.data && state.data.outputStatus);
    host.innerHTML = statuses.map(function (item) {
      return '<span>' + C.escapeHtml(C.italianLabel(item.status || '')) +
        (item.detail ? ': ' + C.escapeHtml(item.detail) : '') + '</span>';
    }).join('');
  }

  function initFamilyPage() {
    state.familyId = doc.body.getAttribute('data-family') || '';
    state.data = state.familyId === 'league'
      ? { schemaVersion: '2.0', familyId: 'league', metadata: {}, outputStatus: [] }
      : familyData(state.familyId);

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
      return '<option value="' + C.escapeHtml(view.id) + '">' +
        C.escapeHtml(view.label) + ' (' + view.rows.length + ')</option>';
    }).join('');

    if (!state.views.length) {
      doc.getElementById('rn-results').innerHTML =
        '<div class="rn-empty-state"><strong>Dati presenti ma nessuna vista valorizzata.</strong></div>';
      return;
    }

    select.addEventListener('change', function () { setSection(select.value); });
    doc.getElementById('rn-search').addEventListener('input', applyFilter);
    doc.getElementById('rn-season').addEventListener('change', applyFilter);
    doc.getElementById('rn-competition').addEventListener('change', applyFilter);
    doc.getElementById('rn-export').addEventListener('click', function () {
      C.downloadCsv('recordsnext_' + state.familyId + '_' +
        state.section.replace(/\./g, '_') + '.csv', state.filtered);
    });
    setSection(state.views[0].id);
  }

  function manifestGeneratedAt(m) {
    return m.generatedAt || m.metadata && m.metadata.generatedAt || '—';
  }

  function initIndex() {
    var m = manifest();
    var generated = manifestGeneratedAt(m);
    var meta = doc.getElementById('rn-index-meta');
    if (meta) meta.textContent = generated !== '—' ? ('Dati generati: ' + generated) : 'Manifest non disponibile';

    doc.querySelectorAll('[data-family-card]').forEach(function (card) {
      var id = card.getAttribute('data-family-card');
      if (id === 'league') return;
      var available = !!familyData(id);
      card.classList.toggle('is-disabled', !available);
      var badge = card.querySelector('.rn-availability');
      if (badge) {
        badge.textContent = available ? 'Disponibile' : 'Non generato';
        badge.className = 'rn-availability ' + (available ? 'is-ok' : 'is-off');
      }
    });
  }

  doc.addEventListener('DOMContentLoaded', function () {
    if (doc.body.hasAttribute('data-family')) initFamilyPage();
    else initIndex();
  });
}(window, document));
