#!/usr/bin/env python3
"""RecordsNext 4, Fase 1: esportatore FCA reali senza scritture sul database.

Per codice storico: JSON completo con anagrafica per stagione, tutte le righe
GiocaIn/Punteggio, Statistiche, Storico (snapshot) e contesto FCA disponibile.
Per intera stagione: JSONL.gz, una riga per calciatore, con identico modello.
Nessun aggregato all-time automatico, nessuna deduzione di presenza da GiocaIn.
"""
from __future__ import annotations
import argparse
from collections import defaultdict
from datetime import datetime
import gzip
import hashlib
import json
from pathlib import Path
import re
import sqlite3
import sys
from typing import Any

VERSION = "1.0"
REQUIRED = ("giocatorea", "giocain", "punteggio", "statistiche", "storico",
            "squadradia", "giornatadia", "incontraina")
SEASON_RE = re.compile(r"^raw_(\d{4}_\d{4})_fca_giocatorea$", re.IGNORECASE)


def table_name(year: str, table: str) -> str:
    if not re.fullmatch(r"\d{4}_\d{4}", year) or table not in REQUIRED:
        raise ValueError("Identificativo di tabella non valido")
    return '"raw_' + year + '_fca_' + table + '"'


def rows(con: sqlite3.Connection, sql: str, params: tuple = ()) -> list[dict]:
    return [dict(r) for r in con.execute(sql, params)]


def sha256(path: Path) -> str:
    h = hashlib.sha256()
    with path.open('rb') as f:
        for b in iter(lambda: f.read(4 * 1024 * 1024), b''):
            h.update(b)
    return h.hexdigest()


def seasons(con: sqlite3.Connection) -> list[str]:
    found = []
    for (name,) in con.execute("SELECT name FROM sqlite_master WHERE type='table'"):
        m = SEASON_RE.fullmatch(name)
        if m:
            found.append(m.group(1))
    return sorted(set(found))


def validate_schema(con: sqlite3.Connection, year: str) -> None:
    existing = {row[0].lower() for row in con.execute("SELECT name FROM sqlite_master WHERE type='table'")}
    missing = [t for t in REQUIRED if f'raw_{year}_fca_{t}' not in existing]
    if missing:
        raise RuntimeError('FCA non completo, stagione %s: %s' % (year, ', '.join(missing)))


def load_references(con: sqlite3.Connection, year: str) -> dict:
    names = {int(r['ID']): r['NOME'] for r in rows(
        con, 'SELECT ID, NOME FROM ' + table_name(year, 'squadradia'))}
    days = {int(r['GIORNATA']): r for r in rows(
        con, 'SELECT * FROM ' + table_name(year, 'giornatadia'))}
    encounters: dict[tuple[int, int], list[dict]] = defaultdict(list)
    for item in rows(con, 'SELECT * FROM ' + table_name(year, 'incontraina')):
        day = int(item['GIORNATA'])
        for team in (item['IDCASA'], item['IDFUORI']):
            if team is not None and int(team) > 0:
                encounters[(day, int(team))].append(item)
    return {'teams': names, 'days': days, 'encounters': encounters}


def real_match(day: int, team: int | None, ref: dict) -> dict:
    matches = ref['encounters'].get((day, team), []) if team is not None else []
    if len(matches) != 1:
        return {'stato': 'NON_COLLEGATO' if not matches else 'AMBIGUO',
                'numeroCandidati': len(matches), 'dettaglio': None}
    src = matches[0]
    home = src['IDCASA']
    away = src['IDFUORI']
    opp = away if home == team else home
    return {'stato': 'COLLEGATO', 'numeroCandidati': 1,
            'dettaglio': {'sorgenteIncontroInA': src, 'avversarioId': opp,
                         'avversario': ref['teams'].get(opp),
                         'squadraCasa': ref['teams'].get(home),
                         'squadraFuori': ref['teams'].get(away),
                         'casa': bool(home == team)}}


def vote_state(p: dict | None) -> str:
    # Classifica solo il valore raw VOTO1: non e una regola di presenza o SV generale.
    if p is None or p.get('VOTO1') is None:
        return 'VOTO1_ASSENTE'
    v = p['VOTO1']
    if v == -1:
        return 'VOTO1_MENO_UNO'
    if v == 0:
        return 'VOTO1_ZERO'
    if v > 0:
        return 'VOTO1_POSITIVO'
    return 'VOTO1_ALTRO_NEGATIVO'


def build_season(con: sqlite3.Connection, year: str,
                 candidates: list[dict], ref: dict,
                 all_gioca: dict[int, list[dict]] | None = None,
                 scores: dict[int, dict] | None = None,
                 statistics: dict[int, list[dict]] | None = None,
                 history: dict[int, list[dict]] | None = None) -> tuple[list[dict], dict]:
    results = []
    counters = defaultdict(int)
    for player in candidates:
        pid = int(player['ID'])
        if all_gioca is None:
            giocain = rows(con, 'SELECT * FROM ' + table_name(year, 'giocain') +
                          ' WHERE IDGIOCATORE=? ORDER BY GIORNATA', (pid,))
        else:
            giocain = all_gioca.get(pid, [])
        if statistics is None:
            stat = rows(con, 'SELECT * FROM ' + table_name(year, 'statistiche') +
                        ' WHERE ID=?', (pid,))
        else:
            stat = statistics.get(pid, [])
        if history is None:
            hist = rows(con, 'SELECT * FROM ' + table_name(year, 'storico') +
                        ' WHERE IDGIOCATORE=? ORDER BY ANNO', (pid,))
        else:
            hist = history.get(pid, [])
        days = []
        seen = set()
        for g in giocain:
            round_num = int(g['GIORNATA'])
            key = (pid, round_num)
            if key in seen:
                raise RuntimeError('Duplicato GiocaIn nella stagione %s, giocatore %d giornata %d' %
                                   (year, pid, round_num))
            seen.add(key)
            point_id = g['IDPUNTEGGIO']
            if scores is None:
                pp = rows(con, 'SELECT * FROM ' + table_name(year, 'punteggio') +
                          ' WHERE ID=?', (point_id,)) if point_id is not None else []
                p = pp[0] if len(pp) == 1 else None
            else:
                p = scores.get(point_id)
            d = ref['days'].get(round_num)
            if p is None:
                counters['punteggiNonRisolti'] += 1
            if d is None:
                counters['giornateNonRisolte'] += 1
            team = int(g['IDSQUADRA']) if g['IDSQUADRA'] is not None else None
            match = real_match(round_num, team, ref)
            if match['stato'] != 'COLLEGATO':
                counters['partiteRealiNonCollegate'] += 1
            counters['righeGiocaIn'] += 1
            days.append({
                'stagione': year, 'giocatoreIdFca': pid,
                'codiceStoricoSorgente': player.get('CODICEGAZZA'),
                'giornata': round_num,
                'giornataFca': d,  # flag GIOCATA originale, senza reinterpretazione
                'squadraRealeId': team,
                'squadraReale': ref['teams'].get(team),
                'giocaInFca': g,
                'punteggioFca': p,
                'statoVoto1': vote_state(p),
                'incontroReale': match,
            })
        results.append({
            'stagione': year,
            'giocatoreIdFca': pid,
            'codiceStoricoSorgente': player.get('CODICEGAZZA'),
            'giocatoreAFca': player,
            'statisticheFca': stat,
            # Storico in ogni FCA e una *istantanea* (anche di anni precedenti).
            # Non sommare gli snapshot tra stagioni.
            'storicoFcaSnapshot': hist,
            'giornateReali': days,
        })
        counters['giocatori'] += 1
        counters['statisticheRighe'] += len(stat)
        counters['storicoSnapshotRighe'] += len(hist)
    return results, dict(counters)


def grouped(con: sqlite3.Connection, year: str, family: str, key: str) -> dict[int, list[dict]]:
    groups = defaultdict(list)
    for item in rows(con, 'SELECT * FROM ' + table_name(year, family)):
        if item[key] is not None:
            groups[int(item[key])].append(item)
    return groups


def identity_warning(records: list[dict]) -> list[dict]:
    # Per un codice, non presume un'identita all-time se cambiano i dati.
    name_sets = defaultdict(list)
    for r in records:
        a = r['giocatoreAFca']
        name = re.sub(r'\s+', ' ', (a.get('NOME') or '').strip()).casefold()
        birth = str(a.get('DATADINASCITA') or '')[:10]
        name_sets[(name, birth)].append(r['stagione'])
    if len(name_sets) < 2:
        return []
    return [{'tipo': 'IDENTITA_DA_VERIFICARE',
             'motivo': 'Anagrafiche differenti per lo stesso codice; NON aggregare all-time automaticamente',
             'gruppi': [{'nomeNormalizzato': name, 'dataNascita': birth, 'stagioni': years}
                        for (name, birth), years in sorted(name_sets.items())]}]


def select_players(con: sqlite3.Connection, year: str, code: int | None) -> list[dict]:
    name = table_name(year, 'giocatorea')
    if code is None:
        return rows(con, 'SELECT * FROM ' + name + ' ORDER BY ID')
    return rows(con, 'SELECT * FROM ' + name + ' WHERE CODICEGAZZA=? ORDER BY ID', (code,))


def export_code(con, code: int, chosen: list[str], destination: Path, db: Path) -> dict:
    records, cover = [], []
    for year in chosen:
        validate_schema(con, year)
        p = select_players(con, year, code)
        ref = load_references(con, year) if p else None
        part, counter = build_season(con, year, p, ref) if p else ([], {})
        records.extend(part)
        cover.append({'stagione': year, 'giocatoriTrovati': len(p), **counter})
    result = {
        'formato': 'RecordsNext4.PlayerReal.v1', 'tipo': 'SELEZIONE_CODICE',
        'codiceRichiesto': code,
        'nota': 'CODICEGAZZA e identita proposta, verificare le anagrafiche prima degli aggregati all-time.',
        'avvertenzeIdentita': identity_warning(records),
        'coperturaStagioni': cover,
        'giocatoriStagionali': records,
    }
    with destination.open('x', encoding='utf-8', newline='\n') as f:
        json.dump(result, f, ensure_ascii=False, indent=2)
        f.write('\n')
    return {'codice': code, 'file': destination.name,
            'giocatoriStagionali': len(records),
            'giornateReali': sum(len(r['giornateReali']) for r in records),
            'avvertenzeIdentita': len(result['avvertenzeIdentita']),
            'sha256Output': sha256(destination)}


def export_all_season(con, year: str, destination: Path) -> dict:
    validate_schema(con, year)
    ref = load_references(con, year)
    players = select_players(con, year, None)
    all_gioca = grouped(con, year, 'giocain', 'IDGIOCATORE')
    scores_list = rows(con, 'SELECT * FROM ' + table_name(year, 'punteggio'))
    scores = {p['ID']: p for p in scores_list}
    if len(scores) != len(scores_list):
        raise RuntimeError('Punteggio.ID duplicato nella stagione ' + year)
    statistics = grouped(con, year, 'statistiche', 'ID')
    history = grouped(con, year, 'storico', 'IDGIOCATORE')
    written, count = 0, defaultdict(int)
    with gzip.open(destination, 'xt', encoding='utf-8', newline='\n', compresslevel=6) as out:
        for player in players:
            subset, counters = build_season(con, year, [player], ref, all_gioca,
                                            scores, statistics, history)
            out.write(json.dumps(subset[0], ensure_ascii=False, separators=(',', ':')) + '\n')
            written += 1
            for k, v in counters.items():
                count[k] += v
    row_count = con.execute('SELECT COUNT(*) FROM ' + table_name(year, 'giocain')).fetchone()[0]
    if row_count != count['righeGiocaIn']:
        raise RuntimeError('Numero GiocaIn non conservato per %s: %s != %s' %
                           (year, count['righeGiocaIn'], row_count))
    return {'stagione': year, 'file': destination.name, 'giocatori': written,
            'righeGiocaIn': count['righeGiocaIn'],
            'punteggiNonRisolti': count['punteggiNonRisolti'],
            'partiteRealiNonCollegate': count['partiteRealiNonCollegate'],
            'sha256Output': sha256(destination)}


def main() -> int:
    parser = argparse.ArgumentParser(description='Estrazione FCA reale, v1 sola lettura')
    parser.add_argument('--database', type=Path, required=True)
    parser.add_argument('--output-dir', type=Path, required=True)
    x = parser.add_mutually_exclusive_group(required=True)
    x.add_argument('--code', type=int, help='Codice storico es. 100843')
    x.add_argument('--all', action='store_true', help='Tutti i giocatori, per stagione, gzip JSONL')
    parser.add_argument('--season', action='append', help='Stagione YYYY_YYYY; ripetibile; senza = tutte')
    args = parser.parse_args()
    if args.code is not None and args.code <= 0:
        parser.error('Il codice deve essere maggiore di zero')
    db = args.database.resolve(strict=True)
    out = args.output_dir.resolve()
    out.mkdir(parents=True, exist_ok=True)
    stamp = datetime.now().strftime('%Y%m%d_%H%M%S_%f')
    uri = db.as_uri() + '?mode=ro'
    connection = sqlite3.connect(uri, uri=True)
    connection.row_factory = sqlite3.Row
    connection.execute('PRAGMA query_only=ON')
    try:
        years = seasons(connection)
        selected = sorted(set(args.season)) if args.season else years
        unknown = set(selected) - set(years)
        if unknown:
            raise ValueError('Stagioni prive di GiocatoreA raw: ' + ', '.join(sorted(unknown)))
        manifest = {'formato': 'RecordsNext4.PlayerReal.Manifest.v1',
                    'versioneEstrattore': VERSION,
                    'database': str(db), 'databaseSha256': sha256(db),
                    'stagioni': selected, 'tipo': 'ALL' if args.all else 'CODICE',
                    'output': []}
        if args.all:
            for year in selected:
                path = out / ('RecordsNext4_Reali_v1_' + year + '_' + stamp + '.jsonl.gz')
                info = export_all_season(connection, year, path)
                manifest['output'].append(info)
                print('ESPORTATA', year, info['giocatori'], 'giocatori,', info['righeGiocaIn'], 'giornate FCA', flush=True)
        else:
            path = out / ('RecordsNext4_Reali_v1_' + str(args.code) + '_' + stamp + '.json')
            info = export_code(connection, args.code, selected, path, db)
            manifest['output'].append(info)
            print('ESPORTATO', args.code, info['giocatoriStagionali'], 'stagioni giocatore,', info['giornateReali'], 'giornate FCA', flush=True)
        manifest_path = out / ('RecordsNext4_Reali_MANIFEST_v1_' + stamp + '.json')
        with manifest_path.open('x', encoding='utf-8', newline='\n') as f:
            json.dump(manifest, f, ensure_ascii=False, indent=2)
            f.write('\n')
        print('MANIFEST:', manifest_path, flush=True)
        return 0
    finally:
        connection.close()


if __name__ == '__main__':
    try:
        sys.exit(main())
    except Exception as exc:
        print('ERRORE ESTRAZIONE REALI v1:', str(exc), file=sys.stderr, flush=True)
        sys.exit(1)
