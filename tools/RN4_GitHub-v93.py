#!/usr/bin/env python3
"""RecordsNext4 v93: checked RC->Git staging, local test, commit, explicit FF push.

Run from the extracted v93 package. Never sync config, DB, user data or generated JS.
"""
import datetime
import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys
import zipfile

RC = Path(r'D:\DEV_APPS\RecordsNext4.0_RC_v64')
WORK = Path(r'D:\DEV_APPS\downloads\RN4_GITHUB_WORK_v63_20261116_174243')
DOWN = Path(r'D:\DEV_APPS\downloads')
PKG = Path(__file__).resolve().parent
MANIFEST_PATH = PKG / 'data' / 'MANIFEST_CODICE_v93.json'
EXPECTED_BRANCH = 'feature/recordsnext4-v67-rc-gui-migrated'
EXPECTED_BASE = '3e8d53f80e2f8b9c7d8be7a7f26b8fa47339d921'
REMOTE_ALLOWED = {
    'https://github.com/mauz79/RecordsNext2.git',
    'https://github.com/mauz79/RecordsNext2',
    'git@github.com:mauz79/RecordsNext2.git',
    'git@github.com:mauz79/RecordsNext2',
}
RELEASE_DOCS = [
    'docs/CONTINUITA_RECORDSNEXT4_v93.md',
    'docs/CHANGELOG_RECORDSNEXT4_v93.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v93.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v45.md',
    'docs/TEST_E_GIT_RECORDSNEXT4_v93.md',
    'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v50.md',
    'tools/Genera-Bibbia-RecordsNext-v93.py',
    'tools/RN4_GitHub-v93.py',
]
COMMIT_MSG = 'RecordsNext4 v93: integrazione nativa, UI v92 e Bibbia v50'


def sha(path):
    h = hashlib.sha256()
    with path.open('rb') as f:
        for chunk in iter(lambda: f.read(1024 * 1024), b''):
            h.update(chunk)
    return h.hexdigest()


def git(*argv, check=True, output=True):
    cp = subprocess.run(['git', '-C', str(WORK), *argv], stdout=subprocess.PIPE,
                        stderr=subprocess.PIPE, text=True, encoding='utf-8', errors='replace')
    if check and cp.returncode:
        raise RuntimeError('git ' + ' '.join(argv) + ' (exit=' + str(cp.returncode) + '): ' + cp.stderr.strip())
    return cp.stdout.strip() if output else cp


def load_manifest():
    d = json.loads(MANIFEST_PATH.read_text(encoding='utf-8'))
    if d.get('baseline_commit') != EXPECTED_BASE or d.get('branch') != EXPECTED_BRANCH:
        raise RuntimeError('Manifest baseline Git differente dall attesa')
    return d


def safe_rel(s):
    p = Path(s)
    if not s or p.is_absolute() or '..' in p.parts or ':' in s or '\\' in s:
        raise RuntimeError('Percorso non sicuro: ' + s)
    if not (s.startswith(('src/main/java/it/alterlega/recordsnext/',
                          'src/test/java/it/alterlega/recordsnext/',
                          'assets/RecordsNext/', 'assets/js/fcmRecordsNext',
                          'docs/', 'tools/', 'tests/'))):
        raise RuntimeError('Percorso non in whitelist: ' + s)
    if s.startswith(('config/', 'data/', 'reports/')) or p.suffix.lower() in {'.zip', '.fcm', '.fca', '.db', '.jar', '.class', '.env'}:
        raise RuntimeError('File privato o generato non ammesso: ' + s)
    return p


def checked_files(manifest):
    out = {}
    for rel, expected in manifest['rc_files'].items():
        safe_rel(rel)
        source = RC / rel
        if not source.is_file():
            raise RuntimeError('Sorgente RC assente: ' + str(source))
        actual = sha(source)
        if actual.lower() != expected.lower():
            raise RuntimeError('DRIFT RC: ' + rel + ' expected=' + expected + ' actual=' + actual)
        out[rel] = source
    for rel in RELEASE_DOCS:
        safe_rel(rel)
        source = PKG / rel
        if not source.is_file():
            raise RuntimeError('Documento v93 assente: ' + str(source))
        expected = manifest['release_files'].get(rel)
        if not expected or sha(source).lower() != expected.lower():
            raise RuntimeError('SHA differente pacchetto v93: ' + rel)
        if rel in out:
            raise RuntimeError('Doppio path nella whitelist: ' + rel)
        out[rel] = source
    if len(manifest['rc_files']) != 42 or len(RELEASE_DOCS) != 8 or len(out) != 50:
        raise RuntimeError('Whitelist incompleta: RC=' + str(len(manifest['rc_files'])) + ' total=' + str(len(out)))
    return out


def git_identity():
    top = Path(git('rev-parse', '--show-toplevel')).resolve()
    if os.path.normcase(str(top)) != os.path.normcase(str(WORK.resolve())):
        raise RuntimeError('Root Git inattesa: ' + str(top))
    branch = git('branch', '--show-current')
    origin = git('remote', 'get-url', 'origin')
    head = git('rev-parse', 'HEAD')
    if branch != EXPECTED_BRANCH:
        raise RuntimeError('Branch inatteso: ' + branch)
    if origin not in REMOTE_ALLOWED:
        raise RuntimeError('Remoto inatteso: ' + origin)
    return {'branch': branch, 'origin': origin, 'head': head}


def local_clean():
    dirty = git('status', '--porcelain', '--untracked-files=normal')
    if dirty:
        raise RuntimeError('Worktree Git NON pulito: controllare senza sovrascrivere. ' + dirty[:1000])


def stamp():
    return datetime.datetime.now().strftime('%Y%m%d_%H%M%S')


def write_json(name, obj):
    DOWN.mkdir(parents=True, exist_ok=True)
    dst = DOWN / name
    dst.write_text(json.dumps(obj, indent=2, ensure_ascii=False) + '\n', encoding='utf-8')
    return dst


def base_preflight(write=True):
    if not RC.is_dir() or not WORK.is_dir() or not PKG.is_dir():
        raise RuntimeError('RC, worktree o package v93 assenti')
    info = git_identity()
    if info['head'] != EXPECTED_BASE:
        raise RuntimeError('Git HEAD diverso dalla base v67: ' + info['head'])
    local_clean()
    manifest = load_manifest()
    files = checked_files(manifest)
    info.update({'mode': 'PREFLIGHT', 'status': 'READY', 'rc_files': 42,
                 'release_files': 8, 'files_to_sync': 50,
                 'source': str(RC), 'destination': str(WORK),
                 'remote_push': 'NOT_RUN', 'timestamp': stamp()})
    if write:
        dst = write_json('RN4_GITHUB_PREFLIGHT_v93.json', info)
        print('RN4_V93_PREFLIGHT_OK 50 file; report=' + str(dst))
    return info, files


def create_backups(targets):
    folder = DOWN / ('RN4_BACKUP_GITHUB_v93_' + stamp())
    folder.mkdir(parents=True, exist_ok=False)
    previous = []
    for kind, rel, dest in targets:
        had = dest.exists()
        if had and not dest.is_file():
            raise RuntimeError('Destinazione non file: ' + str(dest))
        data = {'scope': kind, 'relative': rel, 'had_file': had,
                'sha256_before': sha(dest) if had else None}
        if had:
            backup = folder / kind / rel
            backup.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(dest, backup)
        previous.append((kind, rel, dest, had))
    (folder / 'MANIFEST_BACKUP_v93.json').write_text(
        json.dumps({'files': [{'scope': k, 'relative': r, 'existed': was} for k, r, d, was in previous]},
                   ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    return folder, previous


def stage():
    info, files = base_preflight(write=False)
    targets = []
    for rel in RELEASE_DOCS:
        targets.append(('RC', rel, RC / rel))
    for rel in files:
        targets.append(('GIT', rel, WORK / rel))
    backup, previous = create_backups(targets)
    try:
        for rel in RELEASE_DOCS:
            dest = RC / rel
            dest.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(PKG / rel, dest)
        for rel in sorted(files):
            src = RC / rel
            expected = sha(files[rel])
            if not src.is_file() or sha(src) != expected:
                raise RuntimeError('Source drift durante lo stage: ' + rel)
            dst = WORK / rel
            dst.parent.mkdir(parents=True, exist_ok=True)
            if not dst.exists() or sha(dst) != expected:
                shutil.copy2(src, dst)
        paths = sorted(files)
        git('-c', 'core.autocrlf=false', 'add', '--', *paths)
        changed = git('diff', '--cached', '--name-only').splitlines()
        if not changed or set(changed) - set(paths):
            raise RuntimeError('Git staged inatteso: ' + repr(changed))
        if git('diff', '--name-only'):
            raise RuntimeError('Presenti modifiche NON staged dopo la sincronizzazione')
        untracked = git('ls-files', '--others', '--exclude-standard')
        if untracked:
            raise RuntimeError('File non tracciati inattesi: ' + untracked[:500])
        check = git('diff', '--cached', '--check', check=False, output=False)
        if check.returncode:
            raise RuntimeError('Git whitespace check fallito: ' + check.stdout + ' ' + check.stderr)
        summary = info | {'mode': 'STAGE', 'status': 'STAGED', 'backup': str(backup),
                          'staged_count': len(changed), 'staged_files': changed,
                          'git_stat': git('diff', '--cached', '--stat')}
        dst = write_json('RN4_GITHUB_STAGE_v93.json', summary)
        print('RN4_V93_STAGE_OK staged=' + str(len(changed)) + ' backup=' + str(backup))
        print('RN4_V93_STAGE_REPORT ' + str(dst))
        print('RN4_V93_NESSUN_COMMIT_NESSUN_PUSH')
    except Exception:
        try:
            git('reset', '-q', '--', *sorted(files), check=False)
            for kind, rel, dest, had in reversed(previous):
                if had:
                    src = backup / kind / rel
                    dest.parent.mkdir(parents=True, exist_ok=True)
                    shutil.copy2(src, dest)
                elif dest.exists() and dest.is_file():
                    dest.unlink()
        except Exception as recover:
            print('RN4_V93_ROLLBACK_NON_COMPLETO: ' + str(recover), file=sys.stderr)
        raise


def rollback_stage():
    report_path = DOWN / 'RN4_GITHUB_STAGE_v93.json'
    if not report_path.is_file():
        raise RuntimeError('Stage report assente: nessun rollback automatico possibile')
    report = json.loads(report_path.read_text(encoding='utf-8'))
    backup = Path(report['backup'])
    backup_manifest = backup / 'MANIFEST_BACKUP_v93.json'
    if not backup_manifest.is_file():
        raise RuntimeError('Manifest backup assente, fermo')
    info = git_identity()
    if info['head'] != EXPECTED_BASE:
        raise RuntimeError('Commit creato: rollback stage vietato, usare procedura Git dedicata')
    manifest = load_manifest()
    files = checked_files(manifest)
    staged = git('diff', '--cached', '--name-only').splitlines()
    if not staged or set(staged) - set(files):
        raise RuntimeError('Stage modificato da terzi: rollback bloccato')
    if git('diff', '--name-only') or git('ls-files', '--others', '--exclude-standard'):
        raise RuntimeError('Modifiche non previste: rollback bloccato')
    for rel, source in files.items():
        if not (WORK / rel).is_file() or sha(WORK / rel) != sha(source):
            raise RuntimeError('File cambiato dopo stage: ' + rel)
    metadata = json.loads(backup_manifest.read_text(encoding='utf-8'))['files']
    git('reset', '-q', '--', *sorted(files))
    for item in reversed(metadata):
        kind = item['scope']
        rel = item['relative']
        safe_rel(rel)
        dest = (RC if kind == 'RC' else WORK) / rel
        if item['existed']:
            source = backup / kind / rel
            if not source.is_file():
                raise RuntimeError('Backup non disponibile: ' + str(source))
            dest.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(source, dest)
        elif dest.is_file():
            dest.unlink()
    local_clean()
    record = write_json('RN4_GITHUB_ROLLBACK_v93.json',
                        {'status': 'STAGING_ROLLED_BACK', 'backup': str(backup), 'head': EXPECTED_BASE})
    print('RN4_V93_ROLLBACK_OK ' + str(record))


def verify_staged():
    info = git_identity()
    if info['head'] != EXPECTED_BASE:
        raise RuntimeError('HEAD modificato dopo stage: ' + info['head'])
    manifest = load_manifest()
    files = checked_files(manifest)
    actual = git('diff', '--cached', '--name-only').splitlines()
    if not actual or set(actual) - set(files):
        raise RuntimeError('Staging vuoto o file extra: ' + repr(actual))
    if git('diff', '--name-only') or git('ls-files', '--others', '--exclude-standard'):
        raise RuntimeError('Worktree con file non staged o inattesi')
    status = git('diff', '--cached', '--name-status').splitlines()
    if any(not line.startswith(('A\t', 'M\t')) for line in status):
        raise RuntimeError('Operazioni Git diverse da add/modify: ' + repr(status))
    for rel, src in files.items():
        if not (WORK / rel).is_file() or sha(WORK / rel) != sha(src):
            raise RuntimeError('File nel worktree non corrisponde a RC/payload: ' + rel)
    return info, files, actual


def commit():
    info, files, staged = verify_staged()
    if not (WORK / 'mvnw.cmd').is_file():
        raise RuntimeError('Maven Wrapper assente nel worktree')
    log = DOWN / 'RN4_GITHUB_MAVEN_v93.txt'
    with log.open('w', encoding='utf-8', errors='replace') as out:
        out.write('COMMAND: mvnw.cmd -B -ntp clean test package\n')
        out.flush()
        cp = subprocess.run(['cmd.exe', '/d', '/c', 'mvnw.cmd', '-B', '-ntp', 'clean', 'test', 'package'],
                            cwd=WORK, stdout=out, stderr=subprocess.STDOUT, timeout=1200)
    if cp.returncode:
        raise RuntimeError('Maven dal worktree FALLITO: ' + str(log) + ' exit=' + str(cp.returncode) + '; commit NON eseguito')
    verify_staged()
    git('commit', '-m', COMMIT_MSG)
    head = git('rev-parse', 'HEAD')
    local_clean()
    report = {'status': 'COMMITTED_NOT_PUSHED', 'base': EXPECTED_BASE, 'head': head,
              'branch': EXPECTED_BRANCH, 'origin': info['origin'],
              'staged_files': staged, 'maven': 'PASS', 'maven_log': str(log),
              'production_writes': 0, 'timestamp': stamp()}
    dst = write_json('RN4_GITHUB_COMMIT_v93.json', report)
    print('RN4_V93_COMMIT_OK ' + head)
    print('RN4_V93_COMMIT_REPORT ' + str(dst))
    print('RN4_V93_PUSH_NON_ANCORA_ESEGUITO')


def push():
    commit_report = DOWN / 'RN4_GITHUB_COMMIT_v93.json'
    if not commit_report.is_file():
        raise RuntimeError('Rapporto commit mancante: eseguire prima commit')
    recorded = json.loads(commit_report.read_text(encoding='utf-8'))
    info = git_identity()
    local_clean()
    if info['head'] != recorded.get('head') or recorded.get('base') != EXPECTED_BASE:
        raise RuntimeError('HEAD attuale non coincide con commit v93 registrato')
    parent = git('rev-parse', 'HEAD^')
    if parent != EXPECTED_BASE:
        raise RuntimeError('Commit v93 non discende direttamente dalla base prevista')
    line = git('ls-remote', '--heads', 'origin', EXPECTED_BRANCH)
    if not line.strip():
        raise RuntimeError('Branch remoto assente: nessun push automatico su nuovo branch')
    remote = line.split()[0]
    if remote not in (EXPECTED_BASE, info['head']):
        raise RuntimeError('Branch remoto cambiato, STOP senza force push: ' + remote)
    if remote == EXPECTED_BASE:
        git('push', 'origin', 'HEAD:refs/heads/' + EXPECTED_BRANCH)
    verified = git('ls-remote', '--heads', 'origin', EXPECTED_BRANCH).split()[0]
    if verified != info['head']:
        raise RuntimeError('Push non verificato: remoto=' + verified + ' locale=' + info['head'])
    result = {'status': 'PUSH_VERIFIED', 'branch': EXPECTED_BRANCH,
              'remote': info['origin'], 'old_head': EXPECTED_BASE,
              'new_head': info['head'], 'remote_head': verified,
              'source_rc': str(RC), 'worktree': str(WORK),
              'files_changed': len(recorded['staged_files']),
              'maven': recorded['maven'], 'sqlite_writes': 0,
              'production_writes': 0, 'timestamp': stamp()}
    report = write_json('RN4_GITHUB_RISULTATO_v93.json', result)
    zip_out = DOWN / ('RN4_ESITO_GITHUB_v93_' + stamp() + '.zip')
    include = [report, commit_report, DOWN / 'RN4_GITHUB_STAGE_v93.json',
               DOWN / 'RN4_GITHUB_MAVEN_v93.txt']
    with zipfile.ZipFile(zip_out, 'w', compression=zipfile.ZIP_DEFLATED, compresslevel=8) as z:
        for path in include:
            if path.is_file():
                z.write(path, path.name)
        for rel in RELEASE_DOCS:
            if rel.startswith('docs/'):
                z.write(PKG / rel, rel)
    print('RN4_V93_PUSH_VERIFICATO ' + verified)
    print('RN4_V93_ALLEGA ' + str(zip_out))


def main():
    if len(sys.argv) != 2 or sys.argv[1] not in ('preflight', 'stage', 'commit', 'push', 'rollback'):
        print('Uso: python RN4_GitHub-v93.py {preflight|stage|commit|push|rollback}', file=sys.stderr)
        return 2
    try:
        {'preflight': lambda: base_preflight(), 'stage': stage,
         'commit': commit, 'push': push, 'rollback': rollback_stage}[sys.argv[1]]()
        return 0
    except (OSError, ValueError, RuntimeError, subprocess.TimeoutExpired) as error:
        print('RN4_V93_BLOCCATO ' + str(error), file=sys.stderr)
        return 3


if __name__ == '__main__':
    sys.exit(main())
