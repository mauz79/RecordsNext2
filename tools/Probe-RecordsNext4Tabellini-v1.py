#!/usr/bin/env python3
"""Fase 0: audit senza scrittura dei tabellini senza Incontro e dei possibili riposi.
Non determina autonomamente la natura degli incontri cancellati o a riposo.
"""
import argparse
import csv
import json
import re
import sqlite3
from pathlib import Path
from datetime import datetime

def quote(value):
    if not re.fullmatch(r'[a-zA-Z0-9_]+', value):
        raise ValueError('Nome SQL imprevisto: ' + value)
    return '"' + value + '"'

def to_text(value):
    return '' if value is None else str(value)

def main():
    p = argparse.ArgumentParser(description='Audit diagnostico tabellini e possibili riposi FCM (sola lettura)')
    p.add_argument('--database', type=Path, required=True)
    p.add_argument('--output-dir', type=Path, required=True)
    a = p.parse_args()
    if not a.database.is_file():
        p.error('Database non trovato: ' + str(a.database))
    c = sqlite3.connect(a.database.resolve().as_uri() + '?mode=ro', uri=True)
    c.execute('PRAGMA query_only=ON')
    c.execute('PRAGMA busy_timeout=15000')
    names = {r[0] for r in c.execute("SELECT name FROM sqlite_master WHERE type='table'")}
    seasons = sorted({m.group(1) for n in names if (m := re.fullmatch(r'raw_(\d{4}_\d{4})_fcm_incontro', n))})
    detail = []
    summary = []
    try:
        for s in seasons:
            i = 'raw_' + s + '_fcm_incontro'
            t = 'raw_' + s + '_fcm_tabellino'
            f = 'raw_' + s + '_fcm_formazione'
            sq = 'raw_' + s + '_fcm_fantasquadra'
            if t not in names:
                summary.append({'stagione': s, 'tabellini_senza_incontro': '', 'id_incontro_senza_riscontro': '', 'incontri_con_lato_zero': '', 'incontri_tipo2_con_lato_zero': '', 'note': 'Tabellino non presente'})
                continue
            # Un ID mancante in Incontro non puo essere dichiarato riposo dal solo Tabellino.
            missing = c.execute(
                'SELECT t.IDINCONTRO, t.IDSQUADRA, COUNT(*) AS n '
                'FROM ' + quote(t) + ' t LEFT JOIN ' + quote(i) + ' i ON t.IDINCONTRO=i.ID '
                'WHERE i.ID IS NULL GROUP BY t.IDINCONTRO, t.IDSQUADRA '
                'ORDER BY t.IDINCONTRO, t.IDSQUADRA'
            ).fetchall()
            for match_id, team_id, n in missing:
                formation_count = ''
                if f in names:
                    formation_count = c.execute('SELECT COUNT(*) FROM ' + quote(f) + ' WHERE IDINCONTRO=? AND IDSQUADRA=?', (match_id,team_id)).fetchone()[0]
                team_name = ''
                if sq in names:
                    team_row = c.execute('SELECT NOME FROM ' + quote(sq) + ' WHERE ID=? LIMIT 1',(team_id,)).fetchone()
                    team_name = to_text(team_row[0]) if team_row else ''
                detail.append({'stagione':s, 'id_incontro': match_id, 'id_squadra':team_id,
                               'squadra': team_name, 'righe_tabellino': n,
                               'righe_formazione_stesso_incontro_squadra':formation_count,
                               'classificazione':'ID_INCONTRO_ASSENTE_DA_VERIFICARE',
                               'nota':'Senza record Incontro non si puo provare un turno di riposo.'})
            rest_count, type2_count = c.execute(
                'SELECT COUNT(*), COALESCE(SUM(CASE WHEN IDTIPO=2 THEN 1 ELSE 0 END),0) '
                'FROM ' + quote(i) + ' WHERE IDCASA=0 OR IDFUORI=0'
            ).fetchone()
            summary.append({'stagione':s, 'tabellini_senza_incontro':sum(r[2] for r in missing),
                            'id_incontro_senza_riscontro':len({r[0] for r in missing}),
                            'incontri_con_lato_zero':rest_count,
                            'incontri_tipo2_con_lato_zero':type2_count,
                            'note':'Il lato zero e candidato per analisi riposi, NON prova generale.'})
    finally:
        c.close()
    a.output_dir.mkdir(parents=True, exist_ok=True)
    stamp = datetime.now().strftime('%Y%m%d_%H%M%S')
    prefix = 'RecordsNext4_Tabellini_v1_' + stamp
    for suffix,rows,fields in [
       ('riepilogo',summary,['stagione','tabellini_senza_incontro','id_incontro_senza_riscontro','incontri_con_lato_zero','incontri_tipo2_con_lato_zero','note']),
       ('dettaglio',detail,['stagione','id_incontro','id_squadra','squadra','righe_tabellino','righe_formazione_stesso_incontro_squadra','classificazione','nota'])
    ]:
        with (a.output_dir / (prefix+'_'+suffix+'.csv')).open('w',encoding='utf-8-sig',newline='') as h:
            w=csv.DictWriter(h,fieldnames=fields,delimiter=';')
            w.writeheader()
            w.writerows(rows)
    extra={'database':str(a.database),'stagioni_con_incontro':len(seasons),
      'stagioni_con_id_mancanti':sum(1 for x in summary if x['id_incontro_senza_riscontro'] not in ('',0)),
      'righe_tabellino_senza_incontro':sum(x['tabellini_senza_incontro'] for x in summary if isinstance(x['tabellini_senza_incontro'],int)),
      'dettaglio_righe':len(detail), 'stato':'SOLO_DIAGNOSTICA',
      'regole':['Un incontro con lato zero puo essere un riposo, ma richiede conferma FCM.',
                'Un tabellino il cui Incontro non esiste non viene attribuito automaticamente a riposo o corruzione.',
                'Nessuna riga e cancellata o attribuita a una competizione senza prova.']}
    (a.output_dir/(prefix+'_sintesi.json')).write_text(json.dumps(extra,ensure_ascii=False,indent=2),encoding='utf-8')
    print('Stagioni:',len(seasons),'| stagioni con riferimenti tabellino da verificare:',extra['stagioni_con_id_mancanti'])
    print('Righe di tabellino senza Incontro:',extra['righe_tabellino_senza_incontro'])
    print('Report:',a.output_dir)
if __name__=='__main__': main()
