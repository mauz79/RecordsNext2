#!/usr/bin/env python3
"""Installa e verifica in maniera conservativa Fase 1 reale v1, con ZIP diagnostico.

Non crea worktree, non modifica DB né codice Java, non esegue deploy o git commit.
Solo nuovi file versionati e rapporti in directory di output.
"""
from __future__ import annotations
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
F0_PATCH = [
    'docs/VERIFICA_FASE0_RECORDSNEXT4_v1.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v5.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v4.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v4.md',
    'tools/Probe-RecordsNext4Fonti-v2.py',
    'tools/Create-RecordsNext4WorkingCodeMd-v4.py',
]
F1_PATCH = [
    'docs/SPECIFICA_ESTRAZIONE_REALE_RECORDSNEXT4_v1.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v6.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v5.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v5.md',
    'docs/VERIFICA_ESTRAZIONE_REALE_RECORDSNEXT4_v1.md',
    'tools/Estrai-RecordsNext4Reali-v1.py',
    'tools/Test-RecordsNext4Reali-v1.py',
    'tools/Create-RecordsNext4WorkingCodeMd-v5.py',
    'tools/Invoke-RecordsNext4-Fase1-v1.py',
]
ALL_PATCH = F0_PATCH + F1_PATCH
REQUIRED = [
    'pom.xml', 'docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md',
    'docs/REVISIONE_BASE_RECORDSNEXT_3_2_v1.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v4.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v3.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v3.md',
    'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v3.md',
    'tools/Audit-RecordsNext4Giocatori-v1.py',
    'tools/Create-RecordsNext4WorkingCodeMd-v3.py',
    'tools/Invoke-RecordsNext4-Fase0-v4.py',
]


def sha256(path: Path) -> str:
    h = hashlib.sha256()
    with path.open('rb') as f:
        for block in iter(lambda: f.read(4 * 1024 * 1024), b''):
            h.update(block)
    return h.hexdigest()


def git(project: Path, *args: str) -> str:
    p = subprocess.run(['git', '-C', str(project), *args],
                       stdout=subprocess.PIPE, stderr=subprocess.PIPE,
                       text=True, encoding='utf-8', errors='replace')
    if p.returncode:
        raise RuntimeError('Git: ' + ' '.join(args) + ': ' + p.stderr.strip())
    return p.stdout.strip()


def same_path(a: Path, b: Path) -> bool:
    return os.path.normcase(str(a.resolve())) == os.path.normcase(str(b.resolve()))


def check_project(project: Path) -> dict:
    missing = [p for p in REQUIRED if not (project / p).is_file()]
    if missing:
        raise RuntimeError('Il worktree Fase 0 manca di: ' + ', '.join(missing))
    if not same_path(Path(git(project, 'rev-parse', '--show-toplevel')), project):
        raise RuntimeError('Il progetto indicato non coincide con la radice del worktree')
    branch = git(project, 'branch', '--show-current')
    head = git(project, 'rev-parse', 'HEAD')
    if branch != 'feature/recordsnext4-players':
        raise RuntimeError('Branch inatteso: ' + branch)
    git(project, 'merge-base', '--is-ancestor', BASE_COMMIT, head)
    return {'branch': branch, 'head': head, 'statusBefore': git(project, 'status', '--short', '--branch')}


def install(root: Path, project: Path) -> list[str]:
    # Validazione TOTALE prima di copiare un solo file.
    for path in ALL_PATCH:
        src, dst = root / path, project / path
        if not src.is_file():
            raise RuntimeError('File non incluso nel kit: ' + path)
        if dst.exists():
            if not dst.is_file() or sha256(dst) != sha256(src):
                raise RuntimeError('File gia presente ma differente; nessuna sovrascrittura: ' + path)
    added = []
    for path in ALL_PATCH:
        src, dst = root / path, project / path
        if dst.exists():
            print('GIA PRESENTE IDENTICO:', path, flush=True)
            continue
        dst.parent.mkdir(parents=True, exist_ok=True)
        with dst.open('xb') as f:
            f.write(src.read_bytes())
        added.append(path)
        print('INSTALLATO:', path, flush=True)
    return added


def run_python(source: Path, options: list[str], log: Path) -> str:
    env = dict(os.environ, PYTHONDONTWRITEBYTECODE='1')
    proc = subprocess.run([sys.executable, str(source), *options],
                          stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                          text=True, encoding='utf-8', errors='replace', env=env)
    log.write_text(proc.stdout, encoding='utf-8')
    print(source.name, '(uscita %d)' % proc.returncode, flush=True)
    print(proc.stdout[-900:].strip(), flush=True)
    if proc.returncode:
        raise RuntimeError('Comando non riuscito: ' + source.name + ', controllare ' + log.name)
    return proc.stdout


def main() -> int:
    p = argparse.ArgumentParser(description='Fase 1 RecordsNext 4.0: installazione e audit reali v1')
    p.add_argument('--work-dir', type=Path, default=Path('D:/DEV_APPS/RecordsNext4.0'))
    p.add_argument('--database', type=Path, default=Path('D:/DEV_APPS/RecordsNext2.0/data/database/recordsnext-sandbox.db'))
    p.add_argument('--output-dir', type=Path, default=Path('D:/DEV_APPS/downloads'))
    args = p.parse_args()
    root = Path(__file__).resolve().parent.parent
    project, db, out = args.work_dir.resolve(), args.database.resolve(), args.output_dir.resolve()
    out.mkdir(parents=True, exist_ok=True)
    stamp = datetime.now().strftime('%Y%m%d_%H%M%S_%f')
    bundle = out / ('RecordsNext4_Fase1_OUTPUT_v1_' + stamp)
    bundle.mkdir()
    zip_path = out / (bundle.name + '.zip')
    state = 'ERRORE'
    manifest = {'fase': 1, 'versioneKit': 'v1', 'stato': state,
                'worktree': str(project), 'database': str(db),
                'nessunDeploy': True, 'nessunCommit': True}
    try:
        if not db.is_file():
            raise RuntimeError('Database sandbox non trovato: ' + str(db))
        before = sha256(db)
        check = check_project(project)
        manifest.update(check)
        manifest['databaseSha256Prima'] = before
        print('BASE:', check['head'], 'RAMO:', check['branch'], flush=True)
        added = install(root, project)
        manifest['fileInstallati'] = added
        run_python(project / 'tools/Create-RecordsNext4WorkingCodeMd-v4.py',
                   ['--project-dir', str(project)], bundle / 'LOG_BIBBIA_FASE0_v4.txt')
        run_python(project / 'tools/Test-RecordsNext4Reali-v1.py',
                   ['--database', str(db)], bundle / 'LOG_TEST_REAL_v1.txt')
        pilot = bundle / 'dati_pilota'
        pilot.mkdir()
        for code in (100843, 12302):
            run_python(project / 'tools/Estrai-RecordsNext4Reali-v1.py',
                       ['--database', str(db), '--output-dir', str(pilot), '--code', str(code)],
                       bundle / ('LOG_GIOCATORE_%s_v1.txt' % code))
        for year in ('2006_2007', '2025_2026'):
            run_python(project / 'tools/Estrai-RecordsNext4Reali-v1.py',
                       ['--database', str(db), '--output-dir', str(pilot), '--all', '--season', year],
                       bundle / ('LOG_ALL_%s_v1.txt' % year))
        run_python(project / 'tools/Create-RecordsNext4WorkingCodeMd-v5.py',
                   ['--project-dir', str(project)], bundle / 'LOG_BIBBIA_FASE1_v5.txt')
        docs = bundle / 'documentazione'
        docs.mkdir()
        for name in ('CODICE_FUNZIONANTE_RECORDSNEXT4_v5.md',
                     'PIANO_RECORDSNEXT4_GIOCATORI_v6.md',
                     'REGISTRO_DECISIONI_RECORDSNEXT4_v5.md',
                     'DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v5.md',
                     'SPECIFICA_ESTRAZIONE_REALE_RECORDSNEXT4_v1.md',
                     'VERIFICA_ESTRAZIONE_REALE_RECORDSNEXT4_v1.md'):
            shutil.copy2(project / 'docs' / name, docs / name)
        after = sha256(db)
        if before != after:
            raise RuntimeError('Database modificato DURANTE le verifiche: interrompere altre elaborazioni prima di riprovare')
        manifest['databaseSha256Dopo'] = after
        manifest['statusAfter'] = git(project, 'status', '--short', '--branch')
        manifest['stato'] = 'FASE1_AUDIT_ESEGUITI'
        state = 'OK'
    except Exception:
        error = traceback.format_exc()
        (bundle / 'ERRORE_FASE1_v1.txt').write_text(error, encoding='utf-8')
        print('FASE1 ERRORE:', error[-3200:], file=sys.stderr, flush=True)
    finally:
        (bundle / 'MANIFEST_FASE1_v1.json').write_text(
            json.dumps(manifest, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
        with zipfile.ZipFile(zip_path, 'x', zipfile.ZIP_DEFLATED, compresslevel=6) as z:
            for f in sorted(bundle.rglob('*')):
                if f.is_file():
                    z.write(f, f.relative_to(bundle).as_posix())
        print('ESITO:', state, flush=True)
        print('ZIP DA CARICARE:', zip_path, flush=True)
    return 0 if state == 'OK' else 1


if __name__ == '__main__':
    sys.exit(main())
