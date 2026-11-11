#!/usr/bin/env python3
"""Ripresa conservativa Fase 0 RecordsNext 4.0 dopo errore bibbia v2.

Esecuzione offline, Python standard library, nessuna operazione sul DB in scrittura.
Produce ZIP diagnostico anche in caso di errore.
"""
import argparse
from datetime import datetime
import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys
import traceback
import zipfile

BASE_COMMIT = 'f9bf30323d9204d70e91e242bddfc72c2060ecf9'
PATCH_FILES = [
    'docs/CORREZIONE_FASE0_RECORDSNEXT4_v1.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v4.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v3.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v3.md',
    'tools/Create-RecordsNext4WorkingCodeMd-v3.py',
    'tools/Invoke-RecordsNext4-Fase0-v4.py',
]
REQUIRED_PREVIOUS = [
    'docs/REVISIONE_BASE_RECORDSNEXT_3_2_v1.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v3.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v2.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v2.md',
    'tools/Audit-RecordsNext4Giocatori-v1.py',
    'tools/Probe-RecordsNext4Fonti-v1.py',
    'tools/Probe-RecordsNext4Tabellini-v1.py',
    'tools/Invoke-RecordsNext4-Fase0-v3.ps1',
    'tools/Create-RecordsNext4WorkingCodeMd-v2.ps1',
]


def run_checked(args, cwd=None):
    p = subprocess.run(args, cwd=cwd, text=True, encoding='utf-8',
                       errors='replace', capture_output=True)
    if p.returncode:
        raise RuntimeError('Comando fallito (%d): %s\n%s' %
                           (p.returncode, ' '.join(map(str, args)), p.stderr[-4000:]))
    return p.stdout.strip()


def sha256(path):
    digest = hashlib.sha256()
    with path.open('rb') as handle:
        for block in iter(lambda: handle.read(4 * 1024 * 1024), b''):
            digest.update(block)
    return digest.hexdigest()


def same_path(a, b):
    return os.path.normcase(str(a.resolve())) == os.path.normcase(str(b.resolve()))


def write_text(path, text):
    path.write_text(text, encoding='utf-8')


def check_project(project):
    if not (project / 'pom.xml').is_file():
        raise RuntimeError('Worktree 4.0 senza pom.xml: ' + str(project))
    root = Path(run_checked(['git', '-C', str(project), 'rev-parse', '--show-toplevel']))
    if not same_path(root, project):
        raise RuntimeError('Radice worktree inattesa: ' + str(root))
    branch = run_checked(['git', '-C', str(project), 'branch', '--show-current'])
    if branch != 'feature/recordsnext4-players':
        raise RuntimeError('Branch inatteso: ' + branch)
    head = run_checked(['git', '-C', str(project), 'rev-parse', 'HEAD'])
    run_checked(['git', '-C', str(project), 'merge-base', '--is-ancestor', BASE_COMMIT, head])
    for name in REQUIRED_PREVIOUS:
        if not (project / name).is_file():
            raise RuntimeError('File v3 non trovato nel worktree: ' + name)
    return head


def install(patch_root, project):
    for name in PATCH_FILES:
        src, dst = patch_root / name, project / name
        if not src.is_file():
            raise RuntimeError('File del kit v4 mancante: ' + name)
        if dst.exists() and sha256(src) != sha256(dst):
            raise RuntimeError('File v4 diverso gia presente, nessuna sovrascrittura: ' + name)
    for name in PATCH_FILES:
        src, dst = patch_root / name, project / name
        if dst.exists():
            print('Gia presente identico:', name, flush=True)
            continue
        dst.parent.mkdir(parents=True, exist_ok=True)
        with dst.open('xb') as handle:
            handle.write(src.read_bytes())
        print('Installato:', name, flush=True)


def execute_audit(script, options, log):
    print('Verifica:', script.name, flush=True)
    p = subprocess.run([sys.executable, str(script), *options],
                       text=True, encoding='utf-8', errors='replace',
                       stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    write_text(log, p.stdout)
    print(p.stdout.strip()[-1300:], flush=True)
    if p.returncode:
        raise RuntimeError('Audit fallito (%d): %s; leggere %s' %
                           (p.returncode, script.name, log.name))


def main():
    parser = argparse.ArgumentParser(description='Riprende Fase 0 v4 (senza rieseguire il runner PS v3)')
    parser.add_argument('--work-dir', type=Path, default=Path('D:/DEV_APPS/RecordsNext4.0'))
    parser.add_argument('--database', type=Path, default=Path('D:/DEV_APPS/RecordsNext2.0/data/database/recordsnext-sandbox.db'))
    parser.add_argument('--output-dir', type=Path, default=Path('D:/DEV_APPS/downloads'))
    args = parser.parse_args()
    root = Path(__file__).resolve().parent.parent
    project = args.work_dir.resolve()
    db = args.database.resolve()
    out = args.output_dir.resolve()
    out.mkdir(parents=True, exist_ok=True)
    stamp = datetime.now().strftime('%Y%m%d_%H%M%S_%f')
    folder = out / ('RecordsNext4_Fase0_OUTPUT_v4_' + stamp)
    folder.mkdir()
    zip_path = out / (folder.name + '.zip')
    ok, head = False, ''
    try:
        if same_path(root, project):
            print('Kit v4 eseguito nel worktree: installazione identica.', flush=True)
        if not db.is_file():
            raise RuntimeError('Database sandbox mancante: ' + str(db))
        head = check_project(project)
        print('Worktree esistente, ramo corretto, base 3.2 verificata:', head, flush=True)
        install(root, project)
        # Nessuna chiamata al generatore PowerShell v2 difettoso.
        execute_audit(project / 'tools/Create-RecordsNext4WorkingCodeMd-v3.py',
                      ['--project-dir', str(project)], folder / 'LOG_BIBBIA_v3.txt')
        for code in ('100843', '12302'):
            execute_audit(project / 'tools/Audit-RecordsNext4Giocatori-v1.py',
                          ['--database', str(db), '--code', code, '--output-dir', str(folder)],
                          folder / ('LOG_GIOCATORE_' + code + '.txt'))
        execute_audit(project / 'tools/Probe-RecordsNext4Fonti-v1.py',
                      ['--database', str(db), '--output-dir', str(folder)], folder / 'LOG_FONTI.txt')
        execute_audit(project / 'tools/Probe-RecordsNext4Tabellini-v1.py',
                      ['--database', str(db), '--output-dir', str(folder)], folder / 'LOG_TABELLINI.txt')
        doc_dir = folder / 'documentazione'
        doc_dir.mkdir()
        for name in ('CODICE_FUNZIONANTE_RECORDSNEXT4_v3.md',
                     'PIANO_RECORDSNEXT4_GIOCATORI_v4.md',
                     'REGISTRO_DECISIONI_RECORDSNEXT4_v3.md',
                     'DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v3.md',
                     'CORREZIONE_FASE0_RECORDSNEXT4_v1.md'):
            shutil.copy2(project / 'docs' / name, doc_dir / name)
        write_text(folder / 'GIT_WORKTREE_v4.txt',
                   run_checked(['git', '-C', str(project), 'status', '--short', '--branch']) + '\n')
        manifest = {
            'fase': '0', 'kit': 'v4', 'stato': 'AUDIT_ESEGUITI', 'worktree': str(project),
            'worktree_head': head, 'db_sola_lettura': str(db),
            'db_sha256': sha256(db), 'nessun_aggiornamento_git': True,
            'nessuna_modifica_db': True,
            'nota': 'Tabellini non collegati: classificazione aperta; attenzione ai possibili riposi.',
        }
        write_text(folder / 'MANIFEST_v4.json', json.dumps(manifest, ensure_ascii=False, indent=2))
        ok = True
    except Exception:
        error = traceback.format_exc()
        write_text(folder / 'ERRORE_v4.txt', error)
        print('ERRORE FASE 0 v4:\n' + error[-2500:], file=sys.stderr, flush=True)
    finally:
        with zipfile.ZipFile(zip_path, 'x', zipfile.ZIP_DEFLATED, compresslevel=6) as archive:
            for p in sorted(folder.rglob('*')):
                if p.is_file():
                    archive.write(p, p.relative_to(folder).as_posix())
        print('ZIP DA CARICARE:', zip_path, flush=True)
        print('ESITO:', 'OK' if ok else 'ERRORE', flush=True)
    return 0 if ok else 1


if __name__ == '__main__':
    sys.exit(main())
