#!/usr/bin/env python3
"""Read-only audit of player/team/competition/match contexts, NOT final player facts."""
from __future__ import annotations
import argparse
from collections import Counter, defaultdict
import csv
from datetime import datetime
import gzip
import hashlib
import json
from pathlib import Path
import re
import sqlite3
import sys

SEASON_RE = re.compile(r'^raw_(\d{4}_\d{4})_fcm_formazione$')
CAMPIONATO = {'Campionato', 'Serie A', 'Serie B', 'Serie C', 'Play Off - Play Out', 'Spareggio Serie B'}
COPPE = {
    'Coppa AlterEgo', 'Coppa dei Campioni', 'Coppa di Lega',
    'Coppa Reame Perduto', 'Coppa Serie A', 'Coppa Serie B',
    'Coppa Serie C', 'Coppa tra le Coppe', 'Coppa UEFA',
    'Europa Pipps', 'Mitropa Cup', 'Super Coppa Reame Perduto',
    'Supercoppa AlterEgo', 'Supercoppa di Lega', 'Supercoppa Serie A',
    'Supercoppa Serie B', 'Supercoppa Serie C',
    'Vera finale di Coppa tra le Coppe'
}
# Youth League is always out of public player records and player sheets.
# Raw source counts remain available for provenance and audit.
EXCLUDED = frozenset({'Youth League'})
FAMILY = ('CAMPIONATO', 'COPPE')

def included(name):
    return name not in EXCLUDED


def family(name):
    if name in CAMPIONATO:
        return 'CAMPIONATO'
    if name in COPPE:
        return 'COPPE'
    return None


def initial_status(pos):
    if pos == 0:
        return "TITOLARE_INIZIALE"
    if isinstance(pos, int) and pos > 0:
        return "PANCHINA"
    if pos == -1:
        return "FUORI_LISTA_FORMAZIONE"
    return "POS_DA_VERIFICARE"


def sha(path):
    h = hashlib.sha256()
    with Path(path).open('rb') as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b''):
            h.update(block)
    return h.hexdigest()


def table(season, source, name):
    if not re.fullmatch(r'\d{4}_\d{4}', season):
        raise ValueError('Stagione non valida')
    if source not in ('fcm', 'fca') or not re.fullmatch(r'[a-z]+', name):
        raise ValueError('Tabella non valida')
    return '"raw_' + season + '_' + source + '_' + name + '"'


def write_csv(path, rows, fields):
    with Path(path).open('w', encoding='utf-8-sig', newline='') as stream:
        writer = csv.DictWriter(stream, fieldnames=fields, extrasaction='ignore', delimiter=';')
        writer.writeheader()
        writer.writerows(rows)


def run(db, output, code=100843):
    db = Path(db).resolve(strict=True)
    output = Path(output).resolve()
    output.mkdir(parents=True, exist_ok=True)
    db_hash_before = sha(db)
    con = sqlite3.connect(db.as_uri() + '?mode=ro', uri=True)
    con.row_factory = sqlite3.Row
    all_tables = {r[0].lower() for r in con.execute("SELECT name FROM sqlite_master WHERE type='table'")}
    seasons = sorted({m.group(1) for name in all_tables if (m := SEASON_RE.fullmatch(name))})
    if not seasons:
        raise RuntimeError('Nessuna stagione con Formazione')
    required = ('formazione', 'incontro', 'girone', 'competizione', 'fantasquadra', 'tesserato')
    comp_report, cover_report, groups_report, overlaps_report, player_report = [], [], [], [], []
    sample = []
    total_rows = 0
    all_unclassified = set()
    excluded_competitions = set()
    total_youth_excluded = 0
    for season in seasons:
        for fam in required:
            if ('raw_' + season + '_fcm_' + fam) not in all_tables:
                raise RuntimeError('Tabella mancante: ' + season + ' ' + fam)
        if 'raw_' + season + '_fca_giocatorea' not in all_tables:
            raise RuntimeError('GiocatoreA mancante: ' + season)
        f = table(season, 'fcm', 'formazione')
        m = table(season, 'fcm', 'incontro')
        g = table(season, 'fcm', 'girone')
        c = table(season, 'fcm', 'competizione')
        t = table(season, 'fcm', 'fantasquadra')
        p = table(season, 'fca', 'giocatorea')
        roster = table(season, 'fcm', 'tesserato')
        memberships = {(r[0], r[1]) for r in con.execute('SELECT IDGIOCATORE, IDSQUADRA FROM ' + roster)}
        competitions = {r['ID']: r['NOME'] for r in con.execute('SELECT ID, NOME FROM ' + c)}
        for comp_id, comp_name in sorted(competitions.items()):
            cf = family(comp_name)
            status = 'ESCLUSA' if not included(comp_name) else ('MAPPATA' if cf else 'DA_APPROVARE')
            comp_report.append({'stagione': season, 'idCompetizioneFcm': comp_id,
                                'competizione': comp_name, 'famiglia': cf or '',
                                'verifica': status})
            if status == 'DA_APPROVARE':
                all_unclassified.add((season, comp_name))
            if status == 'ESCLUSA':
                excluded_competitions.add((season, comp_name))
        count_source = con.execute('SELECT COUNT(*) FROM ' + f).fetchone()[0]
        sql = f'''SELECT x.IDGIOC AS idGioc, x.IDSQUADRA AS idTeam,
                         x.IDINCONTRO AS idMatch, x.POS AS pos,
                         x.ENTRATO AS entrato, x.ENTRATORISERVA AS entratoRiserva,
                         i.GIORNATADIA AS giornataA,
                         i.IDGIORNATA AS idGiornataFcm,
                         c.ID AS idCompetizione,
                         c.NOME AS competizione, s.NOME AS nomeSquadra,
                         p.CODICEGAZZA AS codiceStorico, p.NOME AS nomeGiocatore
                   FROM {f} x
                   LEFT JOIN {m} i ON i.ID = x.IDINCONTRO
                   LEFT JOIN {g} g ON g.ID = i.IDGIRONE
                   LEFT JOIN {c} c ON c.ID = g.IDCOMPETIZIONE
                   LEFT JOIN {t} s ON s.ID = x.IDSQUADRA
                   LEFT JOIN {p} p ON p.ID = x.IDGIOC
                   ORDER BY x.IDINCONTRO, x.IDSQUADRA, x.IDGIOC'''
        count_join, counters = 0, Counter()
        group_counts = defaultdict(lambda: [0, 0, 0, 0])
        contexts = defaultdict(list)
        team_by_player = defaultdict(set)
        for r in con.execute(sql):
            row = dict(r)
            count_join += 1
            if row['idCompetizione'] is None or row['codiceStorico'] is None:
                raise RuntimeError('Join mancante nella stagione ' + season + ' match ' + str(row['idMatch']))
            cf = family(row['competizione'])
            if not included(row['competizione']):
                counters['ESCLUSA_YOUTH_LEAGUE'] += 1
                continue
            st = initial_status(row['pos'])
            row['stagione'] = season
            row['famiglia'] = cf
            row['statoSchieramentoIniziale'] = st
            row['tesseratoSnapshot'] = (row['idGioc'], row['idTeam']) in memberships
            # Do not mark player as used/excluded solely from POS or ENTRATO.
            k = (row['idGioc'], row['idTeam'], row['idCompetizione'], cf)
            vals = group_counts[k]
            vals[0] += 1
            if st == 'TITOLARE_INIZIALE': vals[1] += 1
            elif st == 'PANCHINA': vals[2] += 1
            else: vals[3] += 1
            team_by_player[row['idGioc']].add(row['idTeam'])
            if row['giornataA'] is not None and row['giornataA'] > 0:
                contexts[(row['idGioc'], row['idTeam'], row['giornataA'])].append(
                    (row['idMatch'], row['idCompetizione'], row['competizione'], cf))
            if row['codiceStorico'] == code:
                # Do not invent the URL: the 3.2 normalized export owns its resolution.
                # Keep all IDs needed for the exact record/match link.
                row['scorecardUrl'] = None
                row['scorecardLinkStatus'] = 'DA_RISOLVERE_DA_EXPORT_CANONICO_3_2'
                sample.append(row)
            counters[cf or 'DA_APPROVARE'] += 1
            counters[st] += 1
            if not row['tesseratoSnapshot']:
                counters['SENZA_TESSERAMENTO_SNAPSHOT'] += 1
        if count_join != count_source:
            raise RuntimeError(f'Perdita o duplicazione righe {season}: source {count_source} join {count_join}')
        total_rows += count_source
        total_youth_excluded += counters['ESCLUSA_YOUTH_LEAGUE']
        for (pid, tid, cid, cf), counts in sorted(group_counts.items()):
            groups_report.append({
                'stagione': season, 'idGiocatoreFca': pid, 'idSquadraFcm': tid,
                'idCompetizioneFcm': cid, 'famiglia': cf or '',
                'righeFormazione': counts[0], 'titolareIniziale': counts[1],
                'inPanchina': counts[2], 'fuoriListaFormazione': counts[3]})
        for (pid, tid, day), events in sorted(contexts.items()):
            if len(events) > 1:
                overlaps_report.append({'stagione': season, 'idGiocatoreFca': pid,
                    'idSquadraFcm': tid, 'giornataReale': day,
                    'numeroIncontriFantasy': len(events),
                    'numeroCompetizioni': len({x[1] for x in events}),
                    'famiglieOsservate': '|'.join(sorted({x[3] or 'DA_APPROVARE' for x in events})),
                    'incontriFcm': '|'.join(str(x[0]) for x in events),
                    'competizioni': '|'.join(x[2] for x in events)})
        cover_report.append({'stagione': season, 'righeFormazioneOriginali': count_source,
            'righeContestiVerificate': count_join,
            'giocatoriIdFcaDistinti': len(team_by_player),
            'giocatoriPresentiPiuSquadre': sum(len(v) > 1 for v in team_by_player.values()),
            'contestiCampionato': counters['CAMPIONATO'], 'contestiCoppe': counters['COPPE'],
            'contestiDaApprovare': counters['DA_APPROVARE'],
            'contestiYouthLeagueEsclusi': counters['ESCLUSA_YOUTH_LEAGUE'],
            'righeContestiAmmessi': count_join - counters['ESCLUSA_YOUTH_LEAGUE'],
            'titolariIniziali': counters['TITOLARE_INIZIALE'],
            'panchinari': counters['PANCHINA'],
            'fuoriListaFormazione': counters['FUORI_LISTA_FORMAZIONE'],
            'senzaTesseramentoSnapshot': counters['SENZA_TESSERAMENTO_SNAPSHOT']})
    con.close()
    stamp = datetime.now().strftime('%Y%m%d_%H%M%S_%f')
    base = 'RecordsNext4_Contesti_v2_' + stamp
    files = {}
    def save_csv(suffix, data, fields):
        path = output / (base + '_' + suffix + '.csv')
        write_csv(path, data, fields)
        files[suffix] = {'name': path.name, 'sha256': sha(path), 'righe': len(data)}
    save_csv('competizioni', comp_report, ['stagione', 'idCompetizioneFcm', 'competizione', 'famiglia', 'verifica'])
    save_csv('copertura', cover_report, list(cover_report[0]))
    save_csv('per_giocatore_squadra_competizione', groups_report, list(groups_report[0]))
    save_csv('sovrapposizioni', overlaps_report,
             ['stagione', 'idGiocatoreFca', 'idSquadraFcm', 'giornataReale',
              'numeroIncontriFantasy', 'numeroCompetizioni', 'famiglieOsservate',
              'incontriFcm', 'competizioni'])
    path = output / (base + '_esempio_giocatore_' + str(code) + '.jsonl.gz')
    with gzip.open(path, 'wt', encoding='utf-8') as stream:
        for row in sample:
            stream.write(json.dumps(row, ensure_ascii=False, sort_keys=True) + '\n')
    files['esempioGiocatore'] = {'name': path.name, 'sha256': sha(path), 'righe': len(sample)}
    if sha(db) != db_hash_before:
        raise RuntimeError('Database cambiato durante audit')
    manifest = {'format': 'RecordsNext4.ContestiAudit.v2', 'stato': 'OK', 'stagioni': len(seasons),
                'righeFormazione': total_rows,
                'righeFormazioneAmmesse': total_rows - total_youth_excluded,
                'righeYouthLeagueEscluse': total_youth_excluded,
                'competizioniEscluse': sorted({x[1] for x in excluded_competitions}),
                'codiceEsempio': code,
                'righeEsempio': len(sample), 'sovrapposizioni': len(overlaps_report),
                'competizioniDaApprovare': sorted({x[1] for x in all_unclassified}),
                'fonte': str(db), 'sha256Database': db_hash_before,
                'limitazioni': ['Solo righe Formazione esistenti',
                               'Tesserato e una fotografia senza intervalli temporali',
                               'POS non significa contributo effettivo al punteggio',
                               'Play Off e Spareggio sono campionato, Youth League esclusa da record e schede',
                               'I link al tabellino devono essere risolti con dati normalizzati 3.2 per stagione e sito: non inventati',
                               'Nessun voto/fantavoto aggregato in questo audit'],
                'files': files}
    manifest_path = output / (base + '_manifest.json')
    manifest_path.write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print('STATO: OK')
    print('STAGIONI:', len(seasons))
    print('RIGHE FORMAZIONE ORIGINE:', total_rows)
    print('RIGHE FORMAZIONE AMMESSE:', total_rows - total_youth_excluded)
    print('RIGHE YOUTH LEAGUE ESCLUSE:', total_youth_excluded)
    print('SOVRAPPOSIZIONI GIOCATORE-SQUADRA-GIORNATA:', len(overlaps_report))
    print('COMPETIZIONI DA CLASSIFICARE:', ', '.join(manifest['competizioniDaApprovare']))
    print('MANIFEST:', manifest_path)
    return manifest_path


def main():
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument('--database', type=Path, required=True)
    ap.add_argument('--output-dir', type=Path, required=True)
    ap.add_argument('--code', type=int, default=100843)
    args = ap.parse_args()
    run(args.database, args.output_dir, args.code)
    return 0

if __name__ == '__main__':
    try:
        sys.exit(main())
    except Exception as exc:
        print('ERRORE:', exc, file=sys.stderr)
        sys.exit(1)
