#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Release pubblica RecordsNext 4.0: preflight -> build -> publish.

Non tocca mai la installazione personale E: ne' il sito; legge soltanto i
runtime da essa. Mai includere config/, data/ o repository SQLite nel setup.
I file generati e i rapporti sono direttamente nella cartella downloads.
"""
from __future__ import annotations

import argparse
from datetime import datetime
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
import tempfile
import zipfile

VERSION = 'v107'
EXPECTED_GIT = 'a16f21ffdd045ff88ad26317a3b594737783c60c'
BRANCH = 'feature/recordsnext4-v67-rc-gui-migrated'
ORIGIN = 'https://github.com/mauz79/RecordsNext2.git'
REPO = Path(r'D:\DEV_APPS\downloads\RN4_GITHUB_WORK_v63_20261116_174243')
PLUGIN = Path(r'E:\FCM\plugin\Mauz_strom2026\RecordsNext')
DOWNLOADS = Path(r'D:\DEV_APPS\downloads')
PROJECT = Path(__file__).resolve().parents[1]
BUILD = DOWNLOADS / 'RN4_BUILD_SETUP_v107'
STATE = DOWNLOADS / 'RN4_STATO_INSTALLER_v107.json'
STAGE = BUILD / 'payload'
OUT = BUILD / 'output'
EXE = OUT / 'RecordsNext_4.0.0_SETUP.exe'
LOG = DOWNLOADS / 'RN4_BUILD_INSTALLER_v107.log'
APP_REL = Path('runtime/ucanaccess')
EXTRA = [
    'pom.xml',
    'installer/RecordsNext4_v107.iss',
    'installer/RecordsNext.bat',
    'installer/Avvia-RecordsNext4.ps1',
    'installer/INSTALLAZIONE_RECORDSNEXT4.md',
    'tools/RN4_Crea-Installer-v107.py',
    'tools/Genera-Bibbia-RecordsNext-v107.py',
    'tests/test_release_v107.py',
    'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v55.md',
    'docs/CHANGELOG_RECORDSNEXT4_v107.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v107.md',
    'docs/CONTINUITA_RECORDSNEXT4_v107.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v107.md',
    'docs/TEST_RELEASE_RECORDSNEXT4_v107.md',
]
TEXT_TYPES = {'.js', '.html', '.css', '.json', '.md', '.txt', '.svg', '.xml'}
STATIC_TYPES = {'.js', '.html', '.css', '.png', '.svg', '.ico', '.webp', '.jpg', '.jpeg', '.gif'}
PRIVATE_PATH = re.compile(r'(?i)(?:[CDE]:(?:\\{1,2}|/)(?:DEV_APPS|Users|FCM|fantacalcio)|/Users/|/home/[^ /]+/)')


def ensure(flag, message):
    if not flag:
        raise RuntimeError(message)


def sha(path):
    h = hashlib.sha256()
    with Path(path).open('rb') as stream:
        for part in iter(lambda: stream.read(1024 * 1024), b''):
            h.update(part)
    return h.hexdigest()


def run(argv, cwd=None, timeout=300, check=True):
    p = subprocess.run([str(x) for x in argv], cwd=cwd, capture_output=True,
                       timeout=timeout, text=True, encoding='utf-8', errors='replace')
    if check and p.returncode != 0:
        raise RuntimeError('Comando fallito (' + str(p.returncode) + '): ' +
                           ' '.join(map(str, argv)) + '\n' + (p.stdout + p.stderr)[-3000:])
    return p


def git(*args, check=True):
    return run(['git', '-C', REPO, *args], timeout=45, check=check).stdout.strip()


def ensure_parent(path):
    Path(path).parent.mkdir(parents=True, exist_ok=True)


def json_write(path, obj):
    ensure_parent(path)
    t = Path(str(path) + '.writing')
    t.write_text(json.dumps(obj, indent=2, ensure_ascii=False) + '\n', encoding='utf-8')
    t.replace(path)


def report(name, obj):
    DOWNLOADS.mkdir(parents=True, exist_ok=True)
    dst = DOWNLOADS / ('RN4_REPORT_' + name + '_v107.json')
    json_write(dst, obj)
    print('RN4_V107_REPORT', dst)
    return dst


def compiler():
    guesses = [
        Path(r'C:\Program Files (x86)\Inno Setup 7\ISCC.exe'),
        Path(r'C:\Program Files\Inno Setup 7\ISCC.exe'),
        Path(r'C:\Program Files (x86)\Inno Setup 6\ISCC.exe'),
        Path(r'C:\Program Files\Inno Setup 6\ISCC.exe'),
    ]
    from_path = shutil.which('ISCC.exe') or shutil.which('iscc')
    if from_path:
        guesses.insert(0, Path(from_path))
    return next((p for p in guesses if p.is_file()), None)


def java_version():
    q = run(['java', '-version'], timeout=15)
    output = q.stderr + q.stdout
    m = re.search(r'(?i)(?:version\s+["\']?|openjdk\s+)(\d+)', output)
    ensure(m and int(m.group(1)) >= 21, 'Java 21+ richiesto: ' + output[:220])
    return int(m.group(1))


def no_link_tree(p):
    p = Path(p)
    if not p.exists():
        return
    ensure(not p.is_symlink(), 'Symlink vietato: ' + str(p))
    for path in p.rglob('*'):
        ensure(not path.is_symlink(), 'Symlink vietato: ' + str(path))
        if hasattr(path, 'is_junction'):
            ensure(not path.is_junction(), 'Junction vietata: ' + str(path))


def check_git_baseline():
    ensure(REPO.is_dir(), 'Worktree Git assente')
    ensure(git('rev-parse', 'HEAD') == EXPECTED_GIT, 'Git HEAD differente dal consolidamento v103')
    ensure(git('branch', '--show-current') == BRANCH, 'Branch Git non previsto')
    ensure(git('remote', 'get-url', 'origin').rstrip('/') == ORIGIN, 'Remote Git non previsto')
    ensure(not git('status', '--porcelain'), 'Worktree non pulito: non sovrascrivo modifiche')


def listed_assets():
    raw = run(['git', '-C', REPO, 'ls-files', '-z', '--', 'assets'], timeout=30).stdout
    names = [n for n in raw.split('\x00') if n]
    ensure(len(names) >= 10, 'Asset statici Git incompleti, trovati ' + str(len(names)))
    for n in names:
        rel = Path(n)
        ensure(rel.parts[0] == 'assets' and '..' not in rel.parts and
               rel.suffix.lower() in STATIC_TYPES, 'Asset non ammesso: ' + n)
        ensure((REPO / rel).is_file(), 'Asset Git mancante: ' + n)
        if rel.suffix.lower() == '.js':
            ensure((REPO / rel).stat().st_size <= 1_500_000, 'Asset JS RecordsNext oltre limite: '+n)
    return names


def read_careers():
    """Diagnostica prudente: candidati v98 non sono errori certificati.

    Non scrive nel registro. Conta associazioni mancanti/distinte nel registro
    corrente; non stabilisce automaticamente la identita' reale.
    """
    import csv
    source = sorted(DOWNLOADS.glob('RN4_CARRIERE_CANDIDATE_v98_*.csv'), reverse=True)
    ident = PLUGIN / 'config/player-identities.json'
    if not source or not ident.is_file():
        return {'status': 'NON_DISPONIBILE', 'motivo': 'CSV v98 o registro assente'}
    approved = json.loads(ident.read_text('utf-8-sig')).get('approved', {})
    groups = 0
    unresolved = []
    with source[0].open('r', encoding='utf-8-sig', newline='') as f:
        for row in csv.DictReader(f, delimiter=';'):
            keys = [s.strip() for s in row['chiavi_fca'].split('|') if s.strip()]
            canonical = {approved.get(k) for k in keys if approved.get(k)}
            missing = sum(k not in approved for k in keys)
            if len(canonical) > 1 or missing:
                groups += 1
                unresolved.append({'nome': row['nome_somigliante'],
                                   'nascita': row['data_nascita'],
                                   'stagioni': len(keys),
                                   'senza_collegamento': missing,
                                   'identita_distinte': len(canonical)})
    return {'status': 'CANDIDATI_NON_CERTIFICATI', 'gruppi_candidati_residui': groups,
            'campione': unresolved[:30], 'csv': source[0].name,
            'non_e_una_proposta_di_fusione': True}


def preflight():
    check_git_baseline()
    ensure(PLUGIN.is_dir(), 'Plugin 4.0 definitivo non presente')
    jar = PLUGIN / 'RecordsNext.jar'
    ensure(jar.is_file(), 'JAR plugin non presente')
    with zipfile.ZipFile(jar) as z:
        ensure('it/alterlega/recordsnext/gui/RecordsNext2Dashboard.class' in z.namelist(),
               'JAR senza GUI RecordsNext')
    rt = PLUGIN / APP_REL
    ensure((rt / 'ucanaccess-2.0.9.5.jar').is_file(), 'UCanAccess 2.0.9.5 mancante')
    rt_jars = list(rt.rglob('*.jar'))
    ensure(len(rt_jars) >= 5, 'Runtime incompleto')
    no_link_tree(rt)
    assets = listed_assets()
    no_link_tree(PLUGIN / 'visualizzatori')
    jav = java_version()
    ensure((REPO / 'mvnw.cmd').is_file(), 'Maven wrapper assente')
    ensure((PROJECT / 'installer/RecordsNext4_v107.iss').is_file(), 'Sorgente .iss assente')
    free = shutil.disk_usage(DOWNLOADS).free
    ensure(free > 300 * 1024 * 1024, 'Spazio download insufficiente: servono almeno 300 MiB')
    exe_compiler = compiler()
    obj = {'status': 'READY' if exe_compiler else 'MANCANTE_ISCC',
           'git_head': EXPECTED_GIT, 'branch': BRANCH, 'java_major': jav,
           'asset_tracciati': len(assets), 'jars_ucanaccess': len(rt_jars),
           'iscc': str(exe_compiler) if exe_compiler else None,
           'jar_programma_sha256': sha(jar), 'careers': read_careers(),
           'note': 'Nessuna modifica ai 21 siti, a database/config e al plugin operativo.'}
    report('PREFLIGHT', obj)
    if not exe_compiler:
        print('RN4_V107_BLOCCATO ISCC.exe non trovato: serve Inno Setup 7 per creare il setup Windows')
    else:
        print('RN4_V107_PREFLIGHT_READY asset=', len(assets), 'runtime=',len(rt_jars))
    return obj


def copy_regular(src, dest):
    src = Path(src)
    ensure(src.is_file() and not src.is_symlink(), 'File mancante o symlink: ' + str(src))
    ensure_parent(dest)
    shutil.copy2(src, dest)


def private_in_payload(payload):
    forbidden_dirs = {'config','data','database','reports','archive','backup','.git'}
    names = []
    for p in sorted(payload.rglob('*')):
        if p.is_dir():
            continue
        rel = p.relative_to(payload)
        ensure(not any(x.lower() in forbidden_dirs for x in rel.parts),
               'File personale nel payload: ' + str(rel))
        ensure(not p.is_symlink(), 'Symlink nel payload: ' + str(rel))
        if p.suffix.lower() in TEXT_TYPES:
            content = p.read_text(encoding='utf-8-sig', errors='replace')
            ensure(not PRIVATE_PATH.search(content), 'Percorso personale nel payload: '+str(rel))
        names.append({'file': rel.as_posix(), 'bytes': p.stat().st_size,
                      'sha256': sha(p)})
    ensure(any(x['file'] == 'RecordsNext.jar' for x in names), 'JAR non incluso')
    ensure(not any(x['file'].endswith(('recordsnext.db','player-identities.json')) for x in names),
           'Dati personali nel payload')
    return names


def privacy_upgrade_simulation(payload):
    with tempfile.TemporaryDirectory(prefix='rn4-v107-fixture-') as td:
        root = Path(td) / 'installazione'
        (root / 'config').mkdir(parents=True)
        (root / 'data/database').mkdir(parents=True)
        (root / 'config/processing.json').write_bytes(b'CONFIG-UTENTE-SENTINEL')
        (root / 'data/database/recordsnext.db').write_bytes(b'DB-UTENTE-SENTINEL')
        for source in payload.rglob('*'):
            if source.is_file():
                dest = root / source.relative_to(payload)
                copy_regular(source, dest)
        ensure((root / 'config/processing.json').read_bytes() == b'CONFIG-UTENTE-SENTINEL',
               'Simulazione upgrade ha sovrascritto la configurazione')
        ensure((root / 'data/database/recordsnext.db').read_bytes() == b'DB-UTENTE-SENTINEL',
               'Simulazione upgrade ha sovrascritto SQLite')
    return 'PASS'


def prepare_repo_and_build():
    """POM e documenti si sincronizzano nel worktree, poi Maven/test.
    Nessun commit automatico in questa fase.
    """
    pom = REPO / 'pom.xml'
    text = pom.read_text(encoding='utf-8')
    ensure('<version>3.2.0</version>' in text and '<version>4.0.0</version>' not in text,
           'Versione pom.xml inattesa: non modifico')
    pom.write_text(text.replace('<version>3.2.0</version>', '<version>4.0.0</version>', 1),
                   encoding='utf-8')
    for rel in EXTRA:
        if rel == 'pom.xml':
            continue
        src = PROJECT / rel
        ensure(src.is_file(), 'Artefatto release assente: ' + rel)
        copy_regular(src, REPO / rel)
    print('RN4_V107_MAVEN_IN_CORSO')
    p = run([REPO / 'mvnw.cmd', '-B', '-ntp', 'clean', 'test', 'package'],
            cwd=REPO, timeout=600, check=False)
    LOG.write_text('COMMAND: mvnw.cmd -B -ntp clean test package\n' +
                   p.stdout + '\n' + p.stderr, encoding='utf-8')
    ensure(p.returncode == 0, 'Build Maven FALLITA, leggi '+str(LOG))
    test_match = re.search(r'Tests run:\s*(\d+),\s*Failures:\s*(\d+),\s*Errors:\s*(\d+)',
                           p.stdout, re.I)
    ensure('BUILD SUCCESS' in p.stdout, 'Output Maven senza BUILD SUCCESS')
    ensure(test_match is not None, 'Nessun test rilevato nel log Maven')
    ensure(int(test_match.group(2)) == 0 and int(test_match.group(3)) == 0,
           'Test Maven con errori')
    jar = REPO / 'target/RecordsNext.jar'
    ensure(jar.is_file(), 'JAR compilato mancante')
    with zipfile.ZipFile(jar) as z:
        ensure('it/alterlega/recordsnext/gui/RecordsNext2Dashboard.class' in z.namelist(),
               'JAR compilato senza GUI')
    return {'jar_sha256': sha(jar), 'maven_build_success': True,
            'tests_last_line': test_match.group(0), 'maven_log': str(LOG)}


def stage_public_payload():
    ensure(not BUILD.exists(), 'Staging v107 gia presente: non sovrascrivo ('+str(BUILD)+')')
    STAGE.mkdir(parents=True)
    copy_regular(REPO / 'target/RecordsNext.jar', STAGE / 'RecordsNext.jar')
    for n in ['RecordsNext.bat', 'Avvia-RecordsNext4.ps1', 'INSTALLAZIONE_RECORDSNEXT4.md']:
        copy_regular(PROJECT/'installer'/n, STAGE/n)
    for rel in listed_assets():
        copy_regular(REPO / rel, STAGE / rel)
    for src in sorted((PLUGIN / APP_REL).rglob('*.jar')):
        copy_regular(src, STAGE / APP_REL / src.relative_to(PLUGIN / APP_REL))
    visual = PLUGIN / 'visualizzatori'
    copies = 0
    if visual.is_dir():
        for src in sorted(visual.rglob('*')):
            if src.is_dir():
                continue
            ensure(src.suffix.lower() in STATIC_TYPES,
                   'Visualizzatore con estensione inattesa: '+str(src))
            copy_regular(src, STAGE/'visualizzatori'/src.relative_to(visual))
            copies += 1
    manifest = private_in_payload(STAGE)
    ensure(privacy_upgrade_simulation(STAGE) == 'PASS', 'Simulazione upgrade fallita')
    iss = (PROJECT / 'installer/RecordsNext4_v107.iss').read_text('utf-8')
    iss = iss.replace(';VISUALIZZATORI_FILES',
           'Source: "payload\\visualizzatori\\*"; DestDir: "{app}\\visualizzatori"; Flags: ignoreversion recursesubdirs createallsubdirs'
           if copies else '; Nessun visualizzatore opzionale nel payload')
    iscc = compiler()
    if (iscc and (iscc.parent / 'Languages/Italian.isl').is_file()):
        iss = iss.replace('; Il builder aggiunge Italian.isl quando presente nel compilatore installato.',
                          'Name: "italian"; MessagesFile: "compiler:Languages\\Italian.isl"')
    (BUILD / 'RecordsNext4_v107.iss').write_text(iss, encoding='utf-8-sig')
    json_write(BUILD / 'MANIFEST_PUBBLICO_v107.json',
               {'program':'RecordsNext 4.0','files': manifest,
                'private_files_included': False, 'visualizzatori_statici':copies,
                'upgrade_fixture':'PASS'})
    return {'file_payload':len(manifest), 'visualizzatori':copies,
            'bytes_payload':sum(x['bytes'] for x in manifest)}


def build():
    readiness=preflight()
    ensure(readiness['status'] == 'READY', 'Inno Setup ISCC.exe mancante')
    ensure(not BUILD.exists(), 'Build gia esistente: non sovrascrivo '+str(BUILD))
    mvn=prepare_repo_and_build()
    try:
        pack=stage_public_payload()
        iscc = compiler()
        print('RN4_V107_ISCC_IN_CORSO')
        p=run([iscc, BUILD / 'RecordsNext4_v107.iss'], cwd=BUILD,timeout=300, check=False)
        with LOG.open('a', encoding='utf-8') as f:
            f.write('\nINNO SETUP:\n'+p.stdout+'\n'+p.stderr)
        ensure(p.returncode==0 and EXE.is_file() and EXE.stat().st_size>100_000,
               'Compilazione Inno Setup fallita, leggi '+str(LOG))
        release_exe= DOWNLOADS / EXE.name
        ensure(not release_exe.exists(), 'Setup pubblico gia esistente, non sovrascrivo')
        copy_regular(EXE, release_exe)
        info={**mvn, **pack, 'status':'BUILT',
              'git_head_before':EXPECTED_GIT, 'exe_name':release_exe.name,
              'exe_sha256':sha(release_exe), 'exe_bytes':release_exe.stat().st_size,
              'carriere':readiness['careers'],
              'next':'smoke: collaudo Windows, poi publish: commit e push'}
        json_write(STATE, info)
        report('BUILD',info)
        print('RN4_V107_BUILD_OK', release_exe, 'SHA256',info['exe_sha256'])
    except Exception:
        # Non cancellare il lavoro e gli output diagnostici: non fare commit/push.
        raise



def smoke():
    """Collaudo reale installer: nuova installazione e upgrade in directory temporanea.

    Sfrutta l'EXE Inno verificato; disinstalla il test e lascia il vero plugin
    su E: e i 21 siti completamente intatti.
    """
    ensure(STATE.is_file(), 'Stato build v107 mancante')
    state = json.loads(STATE.read_text('utf-8'))
    ensure(state.get('status') == 'BUILT', 'Eseguire smoke solo dopo BUILD_OK')
    exe = DOWNLOADS / 'RecordsNext_4.0.0_SETUP.exe'
    ensure(exe.is_file() and sha(exe)==state.get('exe_sha256'), 'Installer cambiato')
    root = DOWNLOADS / 'RN4_INSTALLER_TEST_v107'
    ensure(not root.exists(), 'Directory test esistente: non la sovrascrivo')
    testlog = DOWNLOADS / 'RN4_TEST_INSTALLER_v107.log'
    def install_round(label):
        cmd = [exe, '/VERYSILENT', '/SUPPRESSMSGBOXES', '/NORESTART', '/SP-',
               '/NOICONS', '/DIR=' + str(root), '/LOG=' + str(testlog)]
        p = run(cmd, timeout=180,check=False)
        ensure(p.returncode == 0, 'Installazione '+label+' fallita ('+str(p.returncode)+')')
    install_round('pulita')
    ensure((root/'RecordsNext.jar').is_file(),'Installazione pulita senza JAR')
    ensure((root/'runtime/ucanaccess/ucanaccess-2.0.9.5.jar').is_file(),
           'Runtime non installato')
    ensure(not (root/'data/database/recordsnext.db').exists(),
           'Setup ha distribuito un database utente: NON pubblico')
    ensure(not (root/'config/player-identities.json').exists(),
           'Setup ha distribuito identita personali: NON pubblico')
    (root/'config').mkdir(parents=True,exist_ok=True)
    (root/'data/database').mkdir(parents=True,exist_ok=True)
    (root/'config/preserve-v107.txt').write_bytes(b'RN4 CONFIG PRESERVED')
    (root/'data/database/preserve-v107.txt').write_bytes(b'RN4 DATA PRESERVED')
    install_round('aggiornamento 4.0')
    ensure((root/'config/preserve-v107.txt').read_bytes()==b'RN4 CONFIG PRESERVED',
           'Aggiornamento ha alterato config')
    ensure((root/'data/database/preserve-v107.txt').read_bytes()==b'RN4 DATA PRESERVED',
           'Aggiornamento ha alterato data')
    uninstaller=root/'unins000.exe'
    ensure(uninstaller.is_file(), 'Uninstaller Inno non presente')
    p=run([uninstaller,'/VERYSILENT','/SUPPRESSMSGBOXES','/NORESTART'],
          timeout=180,check=False)
    ensure(p.returncode==0, 'Uninstall test fallito, verifica cartella '+str(root))
    ensure((root/'config/preserve-v107.txt').read_bytes()==b'RN4 CONFIG PRESERVED',
           'Uninstaller ha cancellato config utente')
    ensure((root/'data/database/preserve-v107.txt').read_bytes()==b'RN4 DATA PRESERVED',
           'Uninstaller ha cancellato dati utente')
    shutil.rmtree(root)
    state.update({'status':'SMOKE_OK', 'smoke':{
        'clean_install': 'PASS','upgrade_preserves_user_files':'PASS',
        'uninstall_preserves_user_files':'PASS', 'test_directory_removed':True}})
    json_write(STATE,state)
    report('SMOKE',state)
    print('RN4_V107_SMOKE_OK installazione_pulita=OK upgrade=OK dati_conservati=OK')


def publish():
    ensure(STATE.is_file(), 'Manca stato BUILD v107')
    st=json.loads(STATE.read_text('utf-8'))
    ensure(st.get('status')=='SMOKE_OK', 'Collaudo installer Windows non completato: eseguire smoke')
    ensure(EXE.is_file() and sha(EXE) == st['exe_sha256'], 'EXE build modificato')
    ensure((DOWNLOADS / EXE.name).is_file() and
           sha(DOWNLOADS / EXE.name) == st['exe_sha256'], 'EXE pubblico modificato')
    ensure(git('rev-parse','HEAD')==EXPECTED_GIT, 'Git cambiato dopo il build')
    ensure(git('branch','--show-current')==BRANCH, 'Branch diverso')
    # Solo i file v107 previsti, mai data/config/JAR/EXE dentro Git.
    status = git('status', '--porcelain', '--untracked-files=all')
    unexpected=[]
    allowed=set(EXTRA)
    for line in status.splitlines():
        rel=line[3:].replace('\\','/')
        if ' -> ' in rel: rel=rel.split(' -> ')[-1]
        if rel not in allowed:
            unexpected.append(rel)
    ensure(not unexpected, 'Modifiche Git estranee: ' + ', '.join(unexpected[:15]))
    git('add','--',*EXTRA)
    ensure('pom.xml' in git('diff','--cached','--name-only'), 'Pom v4 non staged')
    print('RN4_V107_COMMIT_IN_CORSO')
    git('commit', '-m', 'RecordsNext4 v107: installer pubblico 4.0 e chiusura migrazione')
    new_head=git('rev-parse','HEAD')
    ensure(not git('status','--porcelain'), 'Worktree non pulito dopo commit')
    st.update({'status':'COMMITTED','git_head':new_head})
    json_write(STATE, st)
    push_committed(st)
    return


def push_committed(st=None):
    if st is None:
        ensure(STATE.is_file(), 'Stato release v107 non trovato')
        st=json.loads(STATE.read_text('utf-8'))
    ensure(st.get('status')=='COMMITTED','Nessun commit in attesa di push')
    new_head=st['git_head']
    ensure(git('rev-parse','HEAD')==new_head,'HEAD locale divergente dal commit release')
    ensure(git('branch','--show-current')==BRANCH,'Branch diverso')
    ensure(not git('status','--porcelain'),'Worktree non pulito')
    print('RN4_V107_PUSH_IN_CORSO')
    git('push','origin','HEAD:refs/heads/'+BRANCH)
    remote = git('ls-remote','origin','refs/heads/'+BRANCH).split()[0]
    ensure(remote==new_head,'Push non verificato remoto='+remote+' locale='+new_head)
    st.update({'status':'PUSH_VERIFIED','remote_head':remote})
    json_write(STATE,st)
    report('RELEASE',st)
    z=DOWNLOADS / ('RN4_ESITO_RELEASE_v107_'+datetime.now().strftime('%Y%m%d_%H%M%S')+'.zip')
    with zipfile.ZipFile(z,'w',zipfile.ZIP_DEFLATED) as dst:
        dst.write(STATE, 'RN4_STATO_INSTALLER_v107.json')
        dst.write(BUILD/'MANIFEST_PUBBLICO_v107.json', 'MANIFEST_PUBBLICO_v107.json')
        dst.write(LOG, 'RN4_BUILD_INSTALLER_v107.log')
        for file in ['docs/CONTINUITA_RECORDSNEXT4_v107.md',
                     'docs/TEST_RELEASE_RECORDSNEXT4_v107.md']:
            dst.write(PROJECT / file, file)
    print('RN4_V107_PUSH_VERIFIED',new_head,'RAPPORTO',z)


def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument('mode',choices=['preflight','build','smoke','publish','push'])
    args=p.parse_args()
    try:
        if args.mode=='preflight':
            status=preflight()
            return 0 if status['status']=='READY' else 3
        if args.mode=='build':
            build();return 0
        if args.mode=='smoke':
            smoke();return 0
        if args.mode=='publish':
            publish();return 0
        if args.mode=='push':
            push_committed();return 0
    except Exception as ex:
        print('RN4_V107_BLOCCATO',str(ex),file=sys.stderr)
        return 2


if __name__=='__main__':
    sys.exit(main())
