(function (global) {
  'use strict';

  function asArray(value) {
    if (Array.isArray(value)) return value;
    if (value === null || value === undefined) return [];
    return [value];
  }

  function text(value, fallback) {
    if (value === null || value === undefined || value === '') return fallback || '';
    return String(value);
  }

  function escapeHtml(value) {
    return text(value).replace(/[&<>'"]/g, function (ch) {
      return ({'&':'&amp;','<':'&lt;','>':'&gt;',"'":'&#39;','"':'&quot;'})[ch];
    });
  }

  function humanize(value) {
    return text(value)
      .replace(/[_-]+/g, ' ')
      .replace(/([a-z0-9])([A-Z])/g, '$1 $2')
      .replace(/\b\w/g, function (c) { return c.toUpperCase(); });
  }

  var ITALIAN_LABELS = {
    classics: 'Classici', series: 'Serie', ru: "Riserve d'ufficio",
    modifiers: 'Modificatori', thresholdsLuck: 'Soglie e fortuna', culometro: 'Culometro',
    serieVittorie: 'Vittorie consecutive', seriePareggi: 'Pareggi consecutivi',
    serieSconfitte: 'Sconfitte consecutive', serieSenzaSconfitte: 'Serie senza sconfitte',
    serieSenzaVittorie: 'Serie senza vittorie', cleanSheetPortiereSerieSquadre: 'Clean sheet consecutivi',
    puntiSquadraMax: 'Maggior punteggio in una partita',
    fattoreCampoDecisivo: 'Fattore Campo decisivo',
    fattoreCampoTotaleSquadre: 'Saldo Fattore Campo',
    fattoreCampoPuntiGuadagnatiSquadre: 'Punti guadagnati col Fattore Campo',
    fattoreCampoPuntiPersiSquadre: 'Punti persi per il Fattore Campo',
    modDifesaMax: 'Miglior Modificatore Difesa', modDifesaTotaleSquadre: 'Totale Modificatore Difesa',
    modDifesaMediaSquadre: 'Media Modificatore Difesa', modDifesaUtilizziSquadre: 'Utilizzi Modificatore Difesa',
    capitanoTotaleSquadre: 'Bonus Capitano totali', capitanoUtilizziSquadre: 'Utilizzi del Capitano',
    position: 'Posizione', rank: 'Posizione', stagione: 'Stagione', seasonId: 'Stagione',
    competizioneNome: 'Competizione', competitionName: 'Competizione', competizioneId: 'ID competizione',
    recordId: 'Record', nome: 'Nome', eventType: 'Tipo evento', direction: 'Direzione', label: 'Descrizione',
    squadra: 'Squadra', team: 'Squadra', opponent: 'Avversaria', avversaria: 'Avversaria',
    valore: 'Valore', value: 'Valore', index: 'Indice', perMatch: 'Per partita', matches: 'Partite',
    partite: 'Partite', giornata: 'Giornata', round: 'Giornata', giornataDiA: 'Giornata di Serie A',
    serieARound: 'Giornata di Serie A', risultato: 'Risultato', result: 'Risultato',
    punteggio: 'Punteggio', scoreFor: 'Punti fatti', scoreAgainst: 'Punti subiti',
    detail: 'Dettaglio', dettaglioRU: 'Dettaglio RU', parametro: 'Parametro',
    GENERATED_COMPLETE: 'Generato completamente', GENERATED_PARTIAL: 'Generato parzialmente',
    SKIPPED_REQUIRED_DEPENDENCY: 'Non generato: dipendenza obbligatoria mancante',
    SKIPPED_NOT_SELECTED: 'Non selezionato', FAILED: 'Errore'
  };

  var EVENT_LABELS = {
    EXACT_THRESHOLD: 'Soglia precisa', JUST_ENOUGH: 'Giusto giusto',
    MISSED_WIN_HALF_POINT: 'Vittoria mancata per mezzo punto',
    LOSS_BY_A_WHISKER: 'Sconfitta per un soffio', MIRACLE_DRAW: 'Pareggio miracolato',
    TIGHT_DRAW: 'Pareggio stretto', ONE_GOAL_WIN: 'Vittoria di misura',
    ONE_GOAL_LOSS: 'Sconfitta di misura', UNUSED_BAND_POINTS: 'Punti sprecati nella fascia'
  };

  function italianLabel(value) {
    var key = text(value);
    return ITALIAN_LABELS[key] || EVENT_LABELS[key] || humanize(key);
  }

  function formatValue(value) {
    if (value === null || value === undefined || value === '') return '—';
    if (typeof value === 'number') {
      return Number.isInteger(value) ? String(value) : value.toLocaleString('it-IT', {maximumFractionDigits: 4});
    }
    if (typeof value === 'boolean') return value ? 'Sì' : 'No';
    if (Array.isArray(value)) return value.map(formatValue).join(', ');
    if (typeof value === 'object') return JSON.stringify(value);
    return String(value);
  }

  function getPath(obj, path) {
    if (!path) return obj;
    return path.split('.').reduce(function (acc, key) {
      return acc && Object.prototype.hasOwnProperty.call(acc, key) ? acc[key] : undefined;
    }, obj);
  }

  function firstExisting(obj, paths, fallback) {
    for (var i = 0; i < paths.length; i += 1) {
      var value = getPath(obj, paths[i]);
      if (value !== undefined && value !== null && value !== '') return value;
    }
    return fallback;
  }

  function flattenObject(obj, prefix, out) {
    out = out || {};
    prefix = prefix || '';
    Object.keys(obj || {}).forEach(function (key) {
      var value = obj[key];
      var full = prefix ? prefix + '.' + key : key;
      if (value && typeof value === 'object' && !Array.isArray(value)) {
        flattenObject(value, full, out);
      } else {
        out[full] = value;
      }
    });
    return out;
  }

  function downloadCsv(filename, rows) {
    if (!rows.length) return;
    var columns = [];
    rows.forEach(function (row) {
      Object.keys(flattenObject(row)).forEach(function (key) {
        if (columns.indexOf(key) < 0) columns.push(key);
      });
    });
    function q(v) { return '"' + formatValue(v).replace(/"/g, '""') + '"'; }
    var lines = [columns.map(q).join(';')];
    rows.forEach(function (row) {
      var flat = flattenObject(row);
      lines.push(columns.map(function (c) { return q(flat[c]); }).join(';'));
    });
    var blob = new Blob(['\ufeff' + lines.join('\r\n')], {type: 'text/csv;charset=utf-8'});
    var url = URL.createObjectURL(blob);
    var a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);
  }

  global.RecordsNextCommon = {
    asArray: asArray,
    text: text,
    escapeHtml: escapeHtml,
    humanize: humanize,
    italianLabel: italianLabel,
    formatValue: formatValue,
    getPath: getPath,
    firstExisting: firstExisting,
    flattenObject: flattenObject,
    downloadCsv: downloadCsv
  };
}(window));
