#!/usr/bin/env python3
"""Conservative installation, read-only audit, tests, code bible and diagnostic ZIP."""
from __future__ import annotations
import argparse
from datetime import datetime
import hashlib
import json
import os
from pathlib import Path
import subprocess
import sys
import traceback
from zipfile import ZipFile, ZIP_DEFLATED

NEW_FILES = [
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v14.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v13.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v13.md',
    'docs/SPECIFICA_ARCHIVIO_COMPLETO_GIOCATORE_v2.md',
    'tools/Audit-RecordsNext4Contesti-v2.py',
    'tools/Test-RecordsNext4Contesti-v2.py',
    'tools/Create-RecordsNext4WorkingCodeMd-v13.py',
    'tools/Invoke-RecordsNext4Contesti-v2.py',
]
REQUIRED = [
    'pom.xml',
    'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v12.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v13.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v12.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v12.md',
]


def sha(path):
    h = hashlib.sha256()
    with Path(path).open('rb') as stream:
        for chunk in iter(lambda: stream.read(1024*1024), b''):
            h.update(chunk)
    return h.hexdigest()


def git(project, *args):
    proc = subprocess.run(['git', '-C', str(project), *args], capture_output=True,
                          text=True, encoding='utf-8', errors='replace')
    if proc.returncode:
        raise RuntimeError('Git: ' + proc.stderr.strip())
    return proc.stdout.strip()


def invoke(args, env):
    process = subprocess.run([sys.executable, *map(str,args)], capture_output=True,
                             text=True, encoding='utf-8', errors='replace', env=env)
    return process.returncode, process.stdout + process.stderr


def main():
    cli = argparse.ArgumentParser()
    cli.add_argument('--work-dir', type=Path, default=Path('D:/DEV_APPS/RecordsNext4.0'))
    cli.add_argument('--output-dir', type=Path, default=Path('D:/DEV_APPS/downloads'))
    cli.add_argument('--database', type=Path, default=Path('D:/DEV_APPS/RecordsNext2.0/data/database/recordsnext-sandbox.db'))
    opts = cli.parse_args()
    work = opts.work_dir.resolve(strict=True)
    out = opts.output_dir.resolve(strict=True)
    db = opts.database.resolve(strict=True)
    kit = Path(__file__).resolve().parent.parent
    ts = datetime.now().strftime('%Y%m%d_%H%M%S_%f')
    result = out / ('RecordsNext4_Contesti_OUTPUT_v2_' + ts + '.zip')
    audit_dir = out / ('RecordsNext4_ContestiAudit_v2_' + ts)
    report = {'fase':'3C1-classificazione-e-link', 'stato':'ERRORE', 'installati':[],
              'bibbia':'v13', 'nessunCommit':True, 'nessunDeploy':True,
              'auditDir':str(audit_dir), 'databaseModificato':None}
    logs = []
    ok = False
    try:
        for rel in REQUIRED:
            if not (work/rel).is_file():
                raise RuntimeError('Prerequisito mancante: ' + rel)
        gitroot = Path(git(work, 'rev-parse', '--show-toplevel')).resolve(strict=True)
        if os.path.normcase(str(gitroot)).casefold() != os.path.normcase(str(work)).casefold():
            raise RuntimeError('Worktree inatteso: ' + str(gitroot))
        branch = git(work, 'branch', '--show-current')
        if branch != 'feature/recordsnext4-players':
            raise RuntimeError('Branch inatteso: ' + branch)
        report['branch'] = branch
        report['head'] = git(work, 'rev-parse', 'HEAD')
        dbhash = sha(db)
        report['databaseSha256Prima'] = dbhash
        for rel in NEW_FILES:
            src, dest = kit/rel, work/rel
            if not src.is_file():
                raise RuntimeError('File kit mancante: ' + rel)
            if dest.exists() and (not dest.is_file() or sha(src) != sha(dest)):
                raise RuntimeError('File esistente diverso. Non sovrascrivo: ' + rel)
        for rel in NEW_FILES:
            src, dest = kit/rel, work/rel
            if dest.is_file():
                print('GIA PRESENTE IDENTICO:', rel, flush=True)
            else:
                dest.parent.mkdir(parents=True, exist_ok=True)
                with dest.open('xb') as stream:
                    stream.write(src.read_bytes())
                print('INSTALLATO:', rel, flush=True)
                report['installati'].append(rel)
        env = dict(os.environ, PYTHONDONTWRITEBYTECODE='1')
        commands = [
            ('Test-RecordsNext4Contesti-v2.py',
             [work/'tools/Test-RecordsNext4Contesti-v2.py']),
            ('Audit-RecordsNext4Contesti-v2.py',
             [work/'tools/Audit-RecordsNext4Contesti-v2.py', '--database', db,
              '--output-dir', audit_dir, '--code', '100843']),
            ('Create-RecordsNext4WorkingCodeMd-v13.py',
             [work/'tools/Create-RecordsNext4WorkingCodeMd-v13.py', '--project-dir', work]),
        ]
        for label, argv in commands:
            status, output = invoke(argv, env)
            logs.append((label, status, output))
            print(label, 'codice:', status, flush=True)
            if status:
                raise RuntimeError('Esecuzione fallita: ' + label + '\n' + output[-3500:])
        manifests = list(audit_dir.glob('*_manifest.json'))
        if len(manifests) != 1:
            raise RuntimeError('Manifest audit mancante o multiplo')
        audit = json.loads(manifests[0].read_text(encoding='utf-8'))
        if audit.get('stato') != 'OK' or audit.get('righeFormazione') != 552025 or audit.get('stagioni') != 21:
            raise RuntimeError('Audit inatteso: verificare copertura')
        if audit['competizioniDaApprovare']:
            raise RuntimeError('Competizioni non classificate: ' + ', '.join(audit['competizioniDaApprovare']))
        if audit['righeFormazioneAmmesse'] + audit['righeYouthLeagueEscluse'] != audit['righeFormazione']:
            raise RuntimeError('Copertura righe errata dopo esclusioni')
        report['audit'] = {'stagioni': audit['stagioni'],
                           'righeFormazione':audit['righeFormazione'],
                           'sovrapposizioni':audit['sovrapposizioni'],
                           'codiceEsempio':audit['codiceEsempio'],
                           'righeEsempio':audit['righeEsempio'],
                           'competizioniDaApprovare':audit['competizioniDaApprovare'],
                           'righeFormazioneAmmesse':audit['righeFormazioneAmmesse'],
                           'righeYouthLeagueEscluse':audit['righeYouthLeagueEscluse']}
        report['databaseSha256Dopo'] = sha(db)
        report['databaseModificato'] = (report['databaseSha256Dopo'] != dbhash)
        if report['databaseModificato']:
            raise RuntimeError('SHA256 DB variato')
        report['stato'] = 'OK'
        ok = True
    except Exception:
        logs.append(('ERRORE', 1, traceback.format_exc()))
        print('ERRORE:', logs[-1][2], file=sys.stderr, flush=True)
    finally:
        # Always produce a ZIP, including logs and partial outputs on failure.
        with ZipFile(result, 'x', compression=ZIP_DEFLATED) as z:
            z.writestr('MANIFEST.json', json.dumps(report, ensure_ascii=False, indent=2) + '\n')
            for label, status, content in logs:
                z.writestr('log/' + label.replace('.py', '.txt'),
                           'codice: ' + str(status) + '\n' + content)
            for rel in NEW_FILES:
                p = work/rel
                if p.is_file():
                    z.write(p, rel)
            bible = work/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v13.md'
            if bible.is_file():
                z.write(bible, 'docs/' + bible.name)
            if audit_dir.is_dir():
                for file in sorted(audit_dir.glob('*')):
                    if file.is_file():
                        z.write(file, 'audit/' + file.name)
        print('ESITO:', 'OK' if ok else 'ERRORE', flush=True)
        print('ZIP DA CARICARE:', result, flush=True)
    return 0 if ok else 1

if __name__ == '__main__':
    try:
        sys.exit(main())
    except Exception as exc:
        print('ERRORE FATALE:', exc, file=sys.stderr, flush=True)
        sys.exit(1)
