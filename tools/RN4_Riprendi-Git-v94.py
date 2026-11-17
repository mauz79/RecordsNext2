#!/usr/bin/env python3
"""Ripresa controllata dello STAGE v93: solo dipendenze Java + documentazione v94.

Uso: python RN4_Riprendi-Git-v94.py {repair|commit|push}
Nessun intervento su SQLite, sito, sandbox o RC Java.
"""
import datetime as dt
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
PKG = Path(__file__).resolve().parent.parent
V93 = DOWN / 'RN4_GITHUB_CONSOLIDAMENTO_v93'
BASE = '3e8d53f80e2f8b9c7d8be7a7f26b8fa47339d921'
BRANCH = 'feature/recordsnext4-v67-rc-gui-migrated'
REMOTES = ('https://github.com/mauz79/RecordsNext2.git',
           'https://github.com/mauz79/RecordsNext2',
           'git@github.com:mauz79/RecordsNext2.git',
           'git@github.com:mauz79/RecordsNext2')
JAVA = {
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopFullPerformancesV69.java':
        'ed9688e325891b582a68623ccb0abe88a356c0ffb21d0f09a391824868815da4',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopSimeoneIdentityV73.java':
        '090b9cb56943658e94ccc0186fb8aaafdcdf0f5486c7d900b823980cb304bb5a',
}
RELEASE = [
    'docs/CONTINUITA_RECORDSNEXT4_v94.md',
    'docs/CHANGELOG_RECORDSNEXT4_v94.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v94.md',
    'docs/TEST_E_GIT_RECORDSNEXT4_v94.md',
    'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v51.md',
    'tools/Genera-Bibbia-RecordsNext-v94.py',
    'tools/RN4_Riprendi-Git-v94.py',
]


def sha_bytes(b):
    return hashlib.sha256(b).hexdigest()


def sha(path):
    return sha_bytes(path.read_bytes())


def now():
    return dt.datetime.now().strftime('%Y%m%d_%H%M%S')


def run(cmd, check=True, binary=False, **kwargs):
    cp = subprocess.run(cmd, stdout=subprocess.PIPE,
                        stderr=subprocess.PIPE, **({} if binary else dict(text=True, encoding='utf-8', errors='replace')),
                        **kwargs)
    if check and cp.returncode:
        stderr = cp.stderr if not binary else cp.stderr.decode('utf-8', 'replace')
        raise RuntimeError('Comando fallito ' + ' '.join(cmd) + ': ' + stderr.strip()[:1500])
    return cp


def git(*args, binary=False, check=True):
    return run(['git', '-C', str(WORK), *args], binary=binary, check=check)


def git_text(*args):
    return git(*args).stdout.strip()


def git_names(*args):
    x = git_text(*args)
    return x.splitlines() if x else []


def report(name, info):
    DOWN.mkdir(parents=True, exist_ok=True)
    p = DOWN / name
    p.write_text(json.dumps(info, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    return p


def baseline_git():
    actual = Path(git_text('rev-parse', '--show-toplevel')).resolve()
    if os.path.normcase(str(actual)) != os.path.normcase(str(WORK.resolve())):
        raise RuntimeError('Worktree diverso da quello documentato: ' + str(actual))
    branch = git_text('branch', '--show-current')
    origin = git_text('remote', 'get-url', 'origin')
    head = git_text('rev-parse', 'HEAD')
    if branch != BRANCH or origin not in REMOTES or head != BASE:
        raise RuntimeError('Branch/origin/HEAD non corrispondono a v93: ' + branch + ' ' + origin + ' ' + head)
    return {'branch': branch, 'origin': origin, 'head': head}


def ensure_no_extras():
    dirty = git_names('diff', '--name-only')
    other = git_names('ls-files', '--others', '--exclude-standard')
    if dirty or other:
        raise RuntimeError('Modifiche non staged/untracked, STOP: ' + repr((dirty + other)[:15]))
    for status in git_names('diff', '--cached', '--name-status'):
        if not status.startswith(('A\t', 'M\t')):
            raise RuntimeError('Cambio Git inatteso: ' + status)


def v93_files():
    stage = json.loads((DOWN / 'RN4_GITHUB_STAGE_v93.json').read_text(encoding='utf-8'))
    manifest = json.loads((V93 / 'data/MANIFEST_CODICE_v93.json').read_text(encoding='utf-8'))
    if (stage.get('status') != 'STAGED' or stage.get('head') != BASE or
        stage.get('branch') != BRANCH or stage.get('staged_count') != 50):
        raise RuntimeError('Rapporto STAGE v93 diverso dalla baseline attesa')
    code = dict(manifest['rc_files'])
    code.update(manifest['release_files'])
    expected = set(stage['staged_files'])
    if len(expected) != 50 or expected != set(code):
        raise RuntimeError('Whitelist v93 e stage v93 divergono')
    return code


def staged_sha(rel):
    return sha_bytes(git('show', ':' + rel, binary=True).stdout)


def validate_v93():
    meta = baseline_git()
    ensure_no_extras()
    code = v93_files()
    staged = set(git_names('diff', '--cached', '--name-only'))
    if staged != set(code):
        raise RuntimeError('I 50 file nello stage v93 non corrispondono al rapporto; STOP')
    for rel, digest in code.items():
        if staged_sha(rel) != digest:
            raise RuntimeError('Indice Git differente dal manifest v93: ' + rel)
        src = RC / rel
        if not src.is_file() or sha(src) != digest:
            raise RuntimeError('RC cambiata rispetto alla v93: ' + rel)
    return meta, code


def new_files():
    out = {}
    for rel, digest in JAVA.items():
        p = RC / rel
        if not p.is_file() or sha(p) != digest:
            raise RuntimeError('SHA sorgente RC diverso dalla baseline: ' + rel)
        out[rel] = (p, digest)
    manifest = json.loads((PKG / 'data/MANIFEST_EXTRA_v94.json').read_text(encoding='utf-8'))
    if set(manifest['files']) != set(RELEASE):
        raise RuntimeError('Manifest v94 incompleto')
    for rel in RELEASE:
        p = PKG / rel
        digest = manifest['files'][rel]
        if not p.is_file() or sha(p) != digest:
            raise RuntimeError('Payload v94 mancante o SHA errato: ' + rel)
        out[rel] = (p, digest)
    return out


def validate_v94():
    meta = baseline_git()
    ensure_no_extras()
    stage93 = v93_files()
    adds = new_files()
    if set(stage93) & set(adds):
        raise RuntimeError('Percorsi v93/v94 sovrapposti')
    expected = dict(stage93)
    expected.update({rel: dig for rel, (p, dig) in adds.items()})
    staged = set(git_names('diff', '--cached', '--name-only'))
    if staged != set(expected):
        raise RuntimeError('Stage differente dai 59 file attesi: ' + str(len(staged)))
    for rel, digest in expected.items():
        if staged_sha(rel) != digest or not (WORK / rel).is_file() or sha(WORK / rel) != digest:
            raise RuntimeError('File staged o worktree divergente: ' + rel)
    check = git('diff', '--cached', '--check', check=False)
    if check.returncode:
        raise RuntimeError('git diff --cached --check fallito: ' + check.stdout + check.stderr)
    return meta, expected


def repair():
    meta, stage93 = validate_v93()
    adds = new_files()
    targets = [('GIT', rel, WORK / rel) for rel in adds]
    targets += [('RC', rel, RC / rel) for rel in RELEASE]
    for _scope, rel, dest in targets:
        if dest.exists() and (not dest.is_file() or sha(dest) != adds[rel][1]):
            raise RuntimeError('File v94 gia presente ma differente: ' + str(dest))
    backup = DOWN / ('RN4_BACKUP_GITHUB_v94_' + now())
    backup.mkdir(parents=True, exist_ok=False)
    existing = []
    for scope, rel, dest in targets:
        if dest.is_file():
            target = backup / scope / rel
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(dest, target)
            existing.append([scope, rel])
    (backup / 'MANIFEST_v94.json').write_text(json.dumps({'existing': existing,
        'new_paths': list(adds), 'previous_stage': sorted(stage93)}, indent=2), encoding='utf-8')
    written = []
    try:
        for scope, rel, dest in targets:
            src = adds[rel][0]
            if not dest.exists():
                dest.parent.mkdir(parents=True, exist_ok=True)
                shutil.copy2(src, dest)
                written.append((scope, rel, dest))
        git('-c', 'core.autocrlf=false', 'add', '--', *sorted(adds))
        _meta, expected = validate_v94()
    except Exception:
        git('reset', '-q', '--', *sorted(adds), check=False)
        for scope, rel, dest in reversed(written):
            if dest.is_file():
                dest.unlink()
        raise
    res = dict(meta, status='STAGED', mode='REPAIR', v93_staged_files=50,
               new_files=sorted(adds), staged_files=sorted(expected), staged_count=len(expected),
               backup=str(backup), timestamp=now(), remote_push='NOT_RUN')
    p = report('RN4_GITHUB_STAGE_v94.json', res)
    print('RN4_V94_REPAIR_OK 50 file originali preservati; 9 aggiunti; totale=59; report=' + str(p))


def commit():
    meta, expected = validate_v94()
    if not (WORK / 'mvnw.cmd').is_file():
        raise RuntimeError('mvnw.cmd non trovato nel worktree')
    log = DOWN / 'RN4_GITHUB_MAVEN_v94.txt'
    cmd = ['cmd.exe', '/d', '/c', 'mvnw.cmd', '-B', '-ntp', 'clean', 'test', 'package']
    with log.open('w', encoding='utf-8') as output:
        output.write('COMMAND: mvnw.cmd -B -ntp clean test package\n')
        output.flush()
        try:
            cp = subprocess.run(cmd, cwd=WORK, stdout=output, stderr=subprocess.STDOUT, timeout=1200)
        except subprocess.TimeoutExpired:
            raise RuntimeError('Maven oltre timeout; nessun commit, vedere ' + str(log))
    if cp.returncode:
        raise RuntimeError('Maven fallito (exit=' + str(cp.returncode) + '), nessun commit. Log: ' + str(log))
    validate_v94()
    git('commit', '-m', 'RecordsNext4 v94: completamento sorgenti e documentazione UI 4.0')
    head = git_text('rev-parse', 'HEAD')
    if git_text('rev-parse', 'HEAD^') != BASE:
        raise RuntimeError('Commit creato ma parent non atteso: fermarsi')
    if git_text('status', '--porcelain'):
        raise RuntimeError('Commit creato, worktree non pulito: fermarsi')
    p = report('RN4_GITHUB_COMMIT_v94.json', dict(meta, status='COMMITTED_NOT_PUSHED',
        base=BASE, head=head, files_changed=len(expected), maven='PASS',
        maven_log=str(log), timestamp=now()))
    print('RN4_V94_COMMIT_OK ' + head + ' report=' + str(p))
    print('RN4_V94_PUSH_NON_ESEGUITO')


def push():
    data = json.loads((DOWN / 'RN4_GITHUB_COMMIT_v94.json').read_text(encoding='utf-8'))
    branch = git_text('branch', '--show-current')
    remote = git_text('remote', 'get-url', 'origin')
    current = git_text('rev-parse', 'HEAD')
    if (branch != BRANCH or remote not in REMOTES or current != data.get('head') or
        data.get('base') != BASE or git_text('rev-parse', 'HEAD^') != BASE or
        git_text('status', '--porcelain')):
        raise RuntimeError('Identita Git/commit non corrispondenti al gate v94')
    line = git_text('ls-remote', '--heads', 'origin', BRANCH)
    if not line:
        raise RuntimeError('Branch remoto inesistente: nessun push automatico')
    remote_head = line.split()[0]
    if remote_head not in (BASE, current):
        raise RuntimeError('Remote cambiato: ' + remote_head + '. Nessun force push.')
    if remote_head == BASE:
        git('push', 'origin', 'HEAD:refs/heads/' + BRANCH)
    final = git_text('ls-remote', '--heads', 'origin', BRANCH).split()[0]
    if final != current:
        raise RuntimeError('Hash remoto differente: ' + final)
    obj = dict(status='PUSH_VERIFIED', branch=BRANCH, origin=remote,
               base=BASE, head=current, remote_head=final, files_changed=data['files_changed'],
               maven='PASS', timestamp=now(), db_writes=0, production_writes=0)
    p = report('RN4_GITHUB_RISULTATO_v94.json', obj)
    zpath = DOWN / ('RN4_ESITO_GITHUB_v94_' + now() + '.zip')
    with zipfile.ZipFile(zpath, 'w', compression=zipfile.ZIP_DEFLATED, compresslevel=8) as z:
        for filename in ('RN4_GITHUB_PREFLIGHT_v93.json', 'RN4_GITHUB_STAGE_v93.json',
                         'RN4_GITHUB_MAVEN_v93.txt', 'RN4_GITHUB_STAGE_v94.json',
                         'RN4_GITHUB_MAVEN_v94.txt', 'RN4_GITHUB_COMMIT_v94.json',
                         'RN4_GITHUB_RISULTATO_v94.json'):
            f = DOWN / filename
            if f.is_file():
                z.write(f, f.name)
        for rel in RELEASE:
            z.write(PKG / rel, rel)
    print('RN4_V94_PUSH_VERIFICATO ' + final)
    print('RN4_V94_ALLEGA ' + str(zpath))


def main():
    if len(sys.argv) != 2 or sys.argv[1] not in ('repair', 'commit', 'push'):
        print('Uso: python RN4_Riprendi-Git-v94.py {repair|commit|push}', file=sys.stderr)
        return 2
    try:
        {'repair': repair, 'commit': commit, 'push': push}[sys.argv[1]]()
        return 0
    except (OSError, ValueError, KeyError, RuntimeError, subprocess.TimeoutExpired) as e:
        print('RN4_V94_BLOCCATO ' + str(e), file=sys.stderr)
        return 3


if __name__ == '__main__':
    sys.exit(main())
