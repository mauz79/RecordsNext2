#!/usr/bin/env python3
"""RecordsNext4: forensic FCA FMT audit for 2012/13, optional versioned docs.

Input SQLite and FCA are read only. Never modifies data, JS or sites.
Documentation is appended only on a verified source and after an audit report.
"""
from __future__ import annotations
import argparse
import csv
from datetime import datetime
import hashlib
import json
from pathlib import Path
import shutil
import sqlite3
import sys

SEASON = '2012_2013'
YEAR = 2012
EXPECTED_HASH = 'e3c34b941aedb35951ba030701fe47072afa00941f7fa7884d0658442cfcf766'
EXPECTED_COUNTS = {'day1': 255, 'total': 9936, 'stats_mismatch': 0, 'historical_mismatch': 235, 'fantamedia_mismatch': 180}
HEADER = ['player_id', 'name', 'raw_votes', 'old_votes_2_38', 'g1_vote', 'stat_presences', 'historical_presences', 'stat_FMT', 'historical_FANTAMEDIA1', 'fm_delta', 'g1_implied_fantavote', 'g1_implied_bonus_minus_vote', 'halfstep_residual', 'status', 'g1_events']


def number(value):
    if value is None: return None
    try: return float(str(value).replace(',', '.'))
    except (TypeError, ValueError): return None


def as_int(value):
    f = number(value)
    return int(round(f)) if f is not None and abs(f-round(f)) < 0.000001 else None


def same_count(a, b):
    return a is not None and b is not None and a == b


def sha(path):
    h = hashlib.sha256()
    with path.open('rb') as f:
        for block in iter(lambda: f.read(1024*1024), b''):
            h.update(block)
    return h.hexdigest()


def require(ok, message):
    if not ok: raise RuntimeError(message)


def write_csv(path, rows):
    with path.open('w', encoding='utf-8-sig', newline='') as f:
        w = csv.DictWriter(f, fieldnames=HEADER, delimiter=';')
        w.writeheader()
        for r in rows:
            w.writerow({k: json.dumps(r[k], ensure_ascii=False) if isinstance(r.get(k), (dict, list)) else r.get(k, '') for k in HEADER})


def read_data(root):
    db = root / 'data/database/recordsnext.db'
    require(db.is_file(), 'SQLite TEST mancante: ' + str(db))
    c = sqlite3.connect(db.resolve().as_uri() + '?mode=ro', uri=True)
    try:
        c.execute('PRAGMA query_only=ON')
        pathrow = c.execute('SELECT configured_fca_path FROM rn_season_configuration WHERE season_id=?', (SEASON,)).fetchone()
        require(pathrow and pathrow[0], 'Percorso FCA 2012_2013 non configurato')
        fca = Path(pathrow[0]); require(fca.is_file(), 'FCA configurato non trovato: ' + str(fca))
        actual_hash = sha(fca)
        require(actual_hash == EXPECTED_HASH, 'FCA diverso dal file collaudato. SHA256=' + actual_hash)
        source = c.execute("SELECT import_id, source_path, status FROM rn_import WHERE season_id=? AND source_type='FCA' ORDER BY import_id DESC LIMIT 1", (SEASON,)).fetchone()
        require(source and source[2] == 'COMPLETED', 'Importazione FCA non completata')
        require(Path(source[1]).resolve() == fca.resolve(), 'L importazione registrata usa un FCA diverso da quello configurato')
        base = 'raw_2012_2013_fca_'
        required = ['giocatorea','giocain','punteggio','statistiche','storico']
        for table in required:
            require(c.execute('SELECT 1 FROM sqlite_master WHERE type="table" AND name=?',(base+table,)).fetchone(), 'Tabella raw mancante: '+table)
        pcols = [r[1] for r in c.execute(f'PRAGMA table_info("{base}punteggio")')]
        available_events = [x for x in ('GOLFATTI1','GOLFATTISURIGORE1','ASSIST','GOLSUBITI','GOLSUBITISURIGORE','RIGPAR','RIGSBA','AUTOGOL1','AMM','ESP') if x in pcols]
        day_rows = {}
        raw_count = {}
        prior = {}
        g1 = {}
        for rec in c.execute(f'''SELECT g.IDGIOCATORE, g.GIORNATA, p.* FROM "{base}giocain" g JOIN "{base}punteggio" p ON p.ID=g.IDPUNTEGGIO ORDER BY g.IDGIOCATORE,g.GIORNATA'''):
            pid, day = int(rec[0]), int(rec[1])
            cols = ['IDGIOCATORE', 'GIORNATA'] + pcols
            entry = dict(zip(cols, rec))
            if pid not in day_rows: day_rows[pid] = set()
            require(day not in day_rows[pid], 'Giocatore/giornata duplicati: '+str(pid)+'/'+str(day))
            day_rows[pid].add(day)
            vote = number(entry.get('VOTO1'))
            if vote is not None and vote > 0:
                raw_count[pid] = raw_count.get(pid, 0) + 1
                if day > 1: prior[pid] = prior.get(pid, 0) + 1
            if day == 1:
                g1[pid] = {'vote': vote if vote is not None and vote > 0 else None,
                           'events': {ev: number(entry.get(ev)) for ev in available_events}}
        total_votes = sum(raw_count.values())
        g1_votes = sum(1 for rec in g1.values() if rec['vote'] is not None)
        require(total_votes == EXPECTED_COUNTS['total'] and g1_votes == EXPECTED_COUNTS['day1'], 'Voti FCA inattesi: G1='+str(g1_votes)+' totale='+str(total_votes))
        players = c.execute(f'''SELECT a.ID, a.NOME, st.PRESENZE, st.FMT, h.PRESENZE, h.FANTAMEDIA1 FROM "{base}giocatorea" a LEFT JOIN "{base}statistiche" st ON st.ID=a.ID LEFT JOIN "{base}storico" h ON h.IDGIOCATORE=a.ID AND h.ANNO=? ORDER BY a.ID''', (YEAR,)).fetchall()
        require(len(players)==816, 'Anagrafica/Join 2012/13 inatteso: '+str(len(players)))
        result = []
        stats_mismatch = historical_mismatch = fm_mismatch = consistent_halfstep = 0
        fmt_diff_unexplained = []
        cases_hist_missing_g1 = 0
        cases_mismatch_without_g1 = 0
        for pid, name, sp, sf, hp, hf in players:
            n = raw_count.get(pid, 0)
            old = prior.get(pid, 0)
            g1rec = g1.get(pid, {})
            g1vote = g1rec.get('vote')
            statn = as_int(sp); histn = as_int(hp)
            statfmt = number(sf); histfmt = number(hf)
            if statn is not None and statn != n: stats_mismatch += 1
            if histn is not None and histn != n: historical_mismatch += 1
            delta = abs(statfmt-histfmt) if statfmt is not None and histfmt is not None else None
            diverges = delta is not None and delta > .011
            if diverges: fm_mismatch += 1
            aligned = same_count(statn, n) and same_count(histn, old)
            missing_day = g1vote is not None and statn is not None and histn is not None and statn==histn+1
            if missing_day and aligned: cases_hist_missing_g1 += 1
            implied = None; residual = None; bonus = None
            if missing_day and statfmt is not None and histfmt is not None:
                implied = statn*statfmt - histn*histfmt
                residual = abs(implied - round(implied*2)/2)
                bonus = implied-g1vote
            if diverges:
                if implied is not None and residual <= .08 and aligned:
                    consistent_halfstep += 1
                else:
                    fmt_diff_unexplained.append(int(pid))
            if diverges and g1vote is None: cases_mismatch_without_g1 += 1
            status = ('G1_WITH_WEIGHTED_MATCH' if implied is not None and residual <= .08 and aligned
                      else 'G1_PRESENT_UNVERIFIED' if g1vote is not None
                      else 'NO_G1_VOTE')
            result.append({'player_id': pid, 'name': name, 'raw_votes': n, 'old_votes_2_38': old,
                'g1_vote': g1vote, 'stat_presences': statn, 'historical_presences': histn,
                'stat_FMT': statfmt, 'historical_FANTAMEDIA1': histfmt, 'fm_delta': delta,
                'g1_implied_fantavote': round(implied,5) if implied is not None else None,
                'g1_implied_bonus_minus_vote': round(bonus,5) if bonus is not None else None,
                'halfstep_residual': round(residual,6) if residual is not None else None,
                'status': status, 'g1_events': g1rec.get('events', {})})
        summary = {'season': SEASON, 'fca_path': str(fca), 'fca_sha256': actual_hash,
            'sqlite': str(db), 'import_id': source[0], 'import_status': source[2],
            'players': len(players), 'g1_votes': g1_votes, 'all_votes': total_votes,
            'stats_presence_mismatch': stats_mismatch, 'historical_presence_mismatch': historical_mismatch,
            'fantamedia_diff_over_0_011': fm_mismatch,
            'historical_without_g1_aligned': cases_hist_missing_g1,
            'fmt_differences_consistent_with_halfstep_g1': consistent_halfstep,
            'fmt_differences_not_explained': len(fmt_diff_unexplained),
            'unexplained_player_ids': fmt_diff_unexplained,
            'fmt_differences_without_g1_vote': cases_mismatch_without_g1,
            'method': 'Implied G1 FV = Stat.PRESENZE*Stat.FMT - Storico.PRESENZE*Storico.FANTAMEDIA1. Halfstep residual <=0.08 is corroboration only, not official scoring certification.',
            'limitations': ['Nessuna modifica a Storico o Statistiche', 'La formula osservazionale non certifica i bonus Gazzetta', 'Nessun calcolo Java/record TF o FCM certificato']}
        return summary, result
    finally:
        c.close()


def markdown(summary, rows):
    s=summary
    mismatches=[r for r in rows if r['fm_delta'] is not None and r['fm_delta']>.011]
    bad=[r for r in mismatches if r['player_id'] in s['unexplained_player_ids']]
    lines=['# RecordsNext4 - Verifica approfondita FMT FCA 2012/2013 (v57)', '',
      'Audit in sola lettura eseguito il '+datetime.now().strftime('%Y-%m-%d %H:%M:%S')+'.', '',
      '| Verifica | Valore |', '|---|---:|',
      f'| Voti validi G1 | {s["g1_votes"]} |',f'| Voti validi totali | {s["all_votes"]} |',
      f'| Statistiche.PRESENZE diverse dal raw | {s["stats_presence_mismatch"]} |',
      f'| Storico.PRESENZE diverse dal raw | {s["historical_presence_mismatch"]} |',
      f'| FMT diversa da FANTAMEDIA1 (>0,011) | {s["fantamedia_diff_over_0_011"]} |',
      f'| Storico allineato alle giornate 2-38 con voto G1 | {s["historical_without_g1_aligned"]} |',
      f'| Differenze FMT coerenti con un fantavoto G1 a passi di 0,5 | {s["fmt_differences_consistent_with_halfstep_g1"]} |',
      f'| Differenze FMT NON spiegate dal controllo | {s["fmt_differences_not_explained"]} |',
      '', '## Interpretazione',
      'Il confronto fra riepiloghi ufficiali e voti raw verifica se `Statistiche` usa un campione che include G1, mentre `Storico` usa le sole giornate 2-38.',
      'Per ogni giocatore con una presenza aggiuntiva in `Statistiche`, il fantavoto G1 *implicito* e calcolato come `Stat.PRESENZE*Stat.FMT - Storico.PRESENZE*Storico.FANTAMEDIA1`.',
      'La vicinanza ai multipli di 0,5 e un controllo di plausibilita compatibile con arrotondamento delle medie; non equivale al calcolo certificato di bonus/malus.',
      'La preferenza di `Statistiche.FMT` nel 2012/13 e motivabile per la completezza del campione **solo per i giocatori verificati**, non come regola generale per tutte le stagioni.',
      'Non modificare Storico, Statistiche, GiocaIn o Punteggio. Non derivare FMT da `FCM.Tabellino.TOT`.',
      '', '## Differenze non spiegate (massimo 25)', '']
    if not bad:
        lines.append('Nessuna fra quelle con differenza >0,011 (secondo i criteri indicati).')
    else:
        lines.append('| ID | Calciatore | G1 voto | Pres. Stat / Storico | FMT / Fantamedia1 |')
        lines.append('|---:|---|---:|---:|---|')
        for r in bad[:25]:
            lines.append(f'| {r["player_id"]} | {r["name"]} | {r["g1_vote"]} | {r["stat_presences"]}/{r["historical_presences"]} | {r["stat_FMT"]}/{r["historical_FANTAMEDIA1"]} |')
    lines += ['', '## Esempi di fantavoto implicito per G1', '',
        '| Calciatore | Voto G1 | FV G1 implicito | Scarto da multiplo di 0,5 |',
        '|---|---:|---:|---:|']
    for r in [r for r in mismatches if r['g1_implied_fantavote'] is not None][:18]:
        lines.append(f'| {r["name"]} | {r["g1_vote"]} | {r["g1_implied_fantavote"]} | {r["halfstep_residual"]} |')
    lines += ['', '## Limiti', '',
      '- Il confronto con il fantavoto reale ufficiale Gazzetta richiede la verifica dei coefficienti bonus/malus, dei voti speciali e degli arrotondamenti; i numeri impliciti non sono un sostituto della fonte.',
      '- Nessuna nuova elaborazione o pubblicazione e stata eseguita.',
      '- Dati FCA/FCM, SQLite, JS, schede e record classici non sono stati modificati.', '']
    return '\n'.join(lines)


def append_documentation(root, report_dir, summary, note, package_script):
    """Append-only Bible v43, diary v43, decision v36, changelog backup."""
    docs=root/'docs'; tools=root/'tools'
    old=docs/'CODICE_FUNZIONANTE_RECORDSNEXT4_v42.md'
    new=docs/'CODICE_FUNZIONANTE_RECORDSNEXT4_v43.md'
    note_dest=docs/'VERIFICA_FANTAMEDIA_FCA_2012_2013_v57.md'
    diary=docs/'DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v43.md'
    decisions=docs/'REGISTRO_DECISIONI_RECORDSNEXT4_v36.md'
    script_dest=tools/'Audit-Fantamedia-FCA-2012-v57.py'
    changelog=root/'CHANGELOG.md'
    targets=[new,note_dest,diary,decisions,script_dest]
    require(old.is_file() and changelog.is_file(), 'Bibbia v42 o CHANGELOG non trovati: nessun documento modificato')
    require(not any(x.exists() for x in targets), 'Documentazione v43/v57 gia presente: non sovrascrivo')
    backup = Path(r'D:\DEV_APPS\downloads') / ('RN4_CHANGELOG_PRE_V43_'+datetime.now().strftime('%Y%m%d_%H%M%S')+'.md')
    before = changelog.read_bytes()
    created=[]
    intro=(f'# Diario RecordsNext4 v43 - audit FCA 2012/13\n\n'
        f'Scritto il {datetime.now().isoformat(timespec="seconds")}. Nessuna modifica a Java/SQLite/FCM/FCA/JS.\n'
        f'Audit completo su {summary["players"]} giocatori: G1={summary["g1_votes"]}, totale={summary["all_votes"]}.\n'
        f'St.PRES mismatch={summary["stats_presence_mismatch"]}, Storico mismatch={summary["historical_presence_mismatch"]}, FMT diff={summary["fantamedia_diff_over_0_011"]}.\n'
        f'Differenze FMT coerenti con la ricostruzione G1={summary["fmt_differences_consistent_with_halfstep_g1"]}; da approfondire={summary["fmt_differences_not_explained"]}.\n'
        'L audit non certifica coefficienti fantavoto o ranking Java. Vedi verifica v57.\n')
    dec=(f'# Registro decisioni RecordsNext4 v36\n\n'
      '1. Solo audit e analisi: nessuna modifica ai dati FCA/FCM, a SQLite, ai JS e al sito.\n'
      '2. Confermata la distinzione fra `Statistiche.FMT` e `Storico.FANTAMEDIA1` dovuta al numero di presenze, da verificare anche sulla semantica dei bonus.\n'
      '3. Per 2012/13 `Statistiche.FMT` e candidato per la stagione intera soltanto dove presenze e fonti sono coerenti; non imporre ancora una correzione globale Java.\n'
      '4. I report v55/v56 restano conservati. Pubblicazione disabilitata.\n'
      '5. Proseguire il controllo completo delle metriche TF prima di ogni rilascio.\n')
    entry=(f'# Audit FMT 2012/13 v57 - {datetime.now().strftime("%Y-%m-%d")}\n\n'
           f'- Confrontati {summary["players"]} giocatori nel SQLite TEST.\n'
           f'- Differenze oltre soglia FMT {summary["fantamedia_diff_over_0_011"]}; coerenti con G1 {summary["fmt_differences_consistent_with_halfstep_g1"]}; residue {summary["fmt_differences_not_explained"]}.\n'
           '- Nessun dato sorgente, JS, DB o Java modificato. Vedi rapporto v57.\n\n---\n\n')
    try:
        backup.write_bytes(before)
        for target,content in [(note_dest,note),(diary,intro),(decisions,dec)]:
            target.write_text(content,encoding='utf-8');created.append(target)
        shutil.copy2(package_script,script_dest);created.append(script_dest)
        changelog.write_text(entry+before.decode('utf-8-sig'),encoding='utf-8')
        includes=[note_dest,diary,decisions,script_dest,report_dir/'audit-fantamedia-2012-v57.json',changelog]
        parts=[old.read_text(encoding='utf-8').rstrip(), '\n\n# Aggiornamento Bibbia v43 - audit FMT 2012/13\n\n']
        for path in includes:
            data=path.read_bytes()
            ext='json' if path.suffix=='.json' else 'python' if path.suffix=='.py' else 'markdown'
            parts.append('\n## '+str(path.relative_to(root)).replace('\\','/')+'\n\nSHA256: `'+hashlib.sha256(data).hexdigest()+'`\n\n~~~~~'+ext+'\n'+data.decode('utf-8-sig').rstrip()+'\n~~~~~\n')
        tmp=new.with_suffix('.md.part')
        with tmp.open('x',encoding='utf-8',newline='\n') as f:
            f.write(''.join(parts))
        tmp.replace(new);created.append(new)
    except BaseException:
        changelog.write_bytes(before)
        for file in created: file.unlink(missing_ok=True)
        tmp=new.with_suffix('.md.part');tmp.unlink(missing_ok=True)
        raise
    print('DOCUMENTAZIONE: Bibbia v43, Diario v43, Registro v36, Verifica FMT v57, CHANGELOG.')
    print('BACKUP CHANGELOG:',backup)
    print('BIBBIA SHA256:',sha(new))


def main():
    p=argparse.ArgumentParser()
    p.add_argument('--root',default=r'D:\DEV_APPS\RecordsNext4.0_TEST_v37')
    p.add_argument('--no-docs',action='store_true',help='Solo rapporti, senza installare documentazione')
    args=p.parse_args()
    root=Path(args.root)
    if not root.is_dir(): raise RuntimeError('Cartella TEST mancante: '+str(root))
    summary, rows=read_data(root)
    report=root/'reports'/'audit-fantamedia-fca-2012-v57'
    report.mkdir(parents=True,exist_ok=True)
    summary['report_dir']=str(report)
    note=markdown(summary,rows)
    (report/'audit-fantamedia-2012-v57.json').write_text(json.dumps({'summary':summary,'players':rows},ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    (report/'audit-fantamedia-2012-v57.md').write_text(note,encoding='utf-8')
    write_csv(report/'audit-fantamedia-2012-v57.csv',rows)
    print('=== AUDIT FMT 2012/13 v57 ===')
    for key in ('g1_votes','all_votes','stats_presence_mismatch','historical_presence_mismatch','fantamedia_diff_over_0_011','historical_without_g1_aligned','fmt_differences_consistent_with_halfstep_g1','fmt_differences_not_explained','fmt_differences_without_g1_vote'):
        print(key+' = '+str(summary[key]))
    print('REPORT:',report/'audit-fantamedia-2012-v57.md')
    checked={ 'day1':summary['g1_votes'], 'total':summary['all_votes'], 'stats_mismatch':summary['stats_presence_mismatch'], 'historical_mismatch':summary['historical_presence_mismatch'], 'fantamedia_mismatch':summary['fantamedia_diff_over_0_011'] }
    if checked != EXPECTED_COUNTS:
        print('PRECHECK NON SUPERATO: conta diversa da baseline v56. Conservati i report, documentazione non aggiornata.')
        return 2
    if not args.no_docs:
        append_documentation(root,report,summary,note,Path(__file__).resolve())
    print('ESITO: AUDIT COMPLETATO, NESSUNA MODIFICA A FCA/FCM/SQLITE/JS.')
    return 0

if __name__=='__main__':
    try: sys.exit(main())
    except Exception as e:
        print('ERRORE:',str(e), file=sys.stderr)
        sys.exit(2)
