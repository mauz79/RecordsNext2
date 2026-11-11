#!/usr/bin/env python3
"""Conservative native FCA reader installer. Java 21; 3.2 source/JDBC reused read-only."""
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
import traceback
from zipfile import ZipFile, ZIP_DEFLATED

NEW_FILES = [
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopFcaJdbcReader.java',
    'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopFcaJdbcSmokeTest.java',
    'docs/SPECIFICA_LETTORE_JAVA_FCA_TOP_FLOP_v1.md',
    'docs/VERIFICA_LETTORE_JAVA_FCA_TOP_FLOP_v1.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v17.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v16.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v16.md',
    'tools/Create-RecordsNext4WorkingCodeMd-v16.py',
    'tools/Invoke-RecordsNext4FcaJdbc-v1.py',
]
REQUIRED = [
    'pom.xml',
    'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v15.md',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopArchive.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopFacts.java',
    'src/main/java/it/alterlega/recordsnext/app/model/RecordFamily.java',
]
PRESERVE = [
    'pom.xml',
    'src/main/java/it/alterlega/recordsnext/app/model/RecordFamily.java',
    'src/main/java/it/alterlega/recordsnext/app/model/CoreRecordCatalog.java',
    'src/main/java/it/alterlega/recordsnext/app/RecordsNextPipeline.java',
    'src/main/java/it/alterlega/recordsnext/Records2026SitePublisher.java',
    'src/main/java/it/alterlega/recordsnext/app/output/SeasonFamilyShardPublisher.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopArchive.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopFacts.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopMetrics.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopCompetitionPolicy.java',
]
SMOKE_OLD = 'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopArchiveSmokeTest.java'
SMOKE_NEW = 'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopFcaJdbcSmokeTest.java'
MAIN_OLD = 'it.alterlega.recordsnext.app.topflop.TopFlopArchiveSmokeTest'
MAIN_NEW = 'it.alterlega.recordsnext.app.topflop.TopFlopFcaJdbcSmokeTest'


def sha(path: Path) -> str:
    h = hashlib.sha256()
    with path.open('rb') as f:
        for part in iter(lambda: f.read(1048576), b''):
            h.update(part)
    return h.hexdigest()


def invoke(cmd: list[str]) -> tuple[int, str]:
    proc = subprocess.run(cmd, capture_output=True, encoding='utf-8', errors='replace',
                          env=dict(os.environ, PYTHONDONTWRITEBYTECODE='1'))
    return proc.returncode, proc.stdout + proc.stderr


def git(root: Path, *args: str) -> str:
    status, output = invoke(['git', '-C', str(root), *args])
    if status:
        raise RuntimeError('Git: ' + output[-2000:])
    return output.strip()


def java_jar(candidates: list[Path]) -> Path:
    for candidate in candidates:
        if not candidate.is_file():
            continue
        try:
            with ZipFile(candidate) as z:
                if 'org/sqlite/JDBC.class' in z.namelist():
                    return candidate
        except Exception:
            continue
    raise RuntimeError('JAR 3.2 con org.sqlite.JDBC non trovato. Nessun file installato.')


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument('--work-dir', type=Path, default=Path('D:/DEV_APPS/RecordsNext4.0'))
    parser.add_argument('--base-dir', type=Path, default=Path('D:/DEV_APPS/RecordsNext2.0'))
    parser.add_argument('--output-dir', type=Path, default=Path('D:/DEV_APPS/downloads'))
    parser.add_argument('--test-db', choices=('both','main','sandbox'), default='both',
                        help='Default both; use single DB only for local QA')
    args = parser.parse_args()
    kit = Path(__file__).resolve().parent.parent
    output = args.output_dir.resolve(strict=True)
    stamp = datetime.now().strftime('%Y%m%d_%H%M%S_%f')
    result_zip = output / ('RecordsNext4_FcaJdbc_OUTPUT_v1_' + stamp + '.zip')
    build = output / ('RecordsNext4_FcaJdbc_BUILD_v1_' + stamp)
    report = {'fase': 'TopFlop-Java-FCA-JDBC-v1', 'stato': 'ERRORE',
              'pipeline32Alterata': False, 'databaseModificato': False,
              'pubblicazioneEseguita': False, 'bibbia': 'v16', 'filesInstallati': []}
    logs: list[tuple[str, int, str]] = []
    work = None
    success = False
    try:
        work = args.work_dir.resolve(strict=True)
        base = args.base_dir.resolve(strict=True)
        if work == kit or work == base:
            raise RuntimeError('Il worktree 4.0 deve essere distinto dal kit e dalla 3.2')
        for name in REQUIRED:
            if not (work / name).is_file():
                raise RuntimeError('Prerequisito worktree 4.0 mancante: ' + name)
        if Path(git(work, 'rev-parse', '--show-toplevel')).resolve(strict=True) != work:
            raise RuntimeError('Worktree 4.0 non coincide con la root Git')
        branch = git(work, 'branch', '--show-current')
        if branch != 'feature/recordsnext4-players':
            raise RuntimeError('Branch 4.0 errato: ' + branch)
        report['branch'] = branch
        report['head'] = git(work, 'rev-parse', 'HEAD')
        before = {name: sha(work / name) for name in PRESERVE}
        report['preservedSha256Before'] = before
        for name in NEW_FILES:
            source, target = kit / name, work / name
            if not source.is_file():
                raise RuntimeError('File mancante nel kit: ' + name)
            if target.exists() and (not target.is_file() or sha(target) != sha(source)):
                raise RuntimeError('File gia presente ma DIVERSO: ' + name)
        database_paths = [base / 'data/database/recordsnext.db',
                          base / 'data/database/recordsnext-sandbox.db']
        if not all(p.is_file() for p in database_paths):
            raise RuntimeError('Manca almeno uno dei database 3.2, non avvio i test')
        database_before = {p.name: sha(p) for p in database_paths}
        report['databaseSha256Before'] = database_before
        jar = java_jar([base / 'release/RecordsNext_3.2.0_FULL/payload/RecordsNext.jar',
                        base / 'target/RecordsNext.jar',
                        work / 'target/RecordsNext.jar'])
        report['jar3_2'] = str(jar)
        javac, java = shutil.which('javac'), shutil.which('java')
        if not javac or not java:
            raise RuntimeError('JDK 21 richiesto, java/javac non trovati')
        status, version = invoke([javac, '-version'])
        logs.append(('JDK', status, version))
        match = re.search(r'\bjavac\s+(\d+)(?:\.|\s|$)', version)
        if status or match is None or int(match.group(1)) < 21:
            raise RuntimeError('JDK 21 o successivo richiesto: ' + version)
        build.mkdir(exist_ok=False)
        added = sorted((kit / 'src/main/java').rglob('*.java'))
        overlays = {file.relative_to(kit).as_posix() for file in added}
        existing = sorted(p for p in (work / 'src/main/java').rglob('*.java')
                          if p.relative_to(work).as_posix() not in overlays)
        if len(added) != 1 or len(existing) < 70:
            raise RuntimeError('Numero sorgenti inatteso: ' + str((len(added), len(existing))))
        command = [javac, '-encoding', 'UTF-8', '--release', '21',
                   '-cp', str(jar), '-d', str(build)] + [str(p) for p in existing + added] + [
                       str(work / SMOKE_OLD), str(kit / SMOKE_NEW)]
        status, result = invoke(command)
        logs.append(('COMPILA_JAVA', status, result))
        print('Compilazione Java completa:', 'OK' if not status else 'ERRORE', flush=True)
        if status:
            raise RuntimeError('Compilazione fallita: ' + result[-2500:])
        classpath = os.pathsep.join((str(build), str(jar)))
        status, result = invoke([java, '-ea', '-cp', classpath, MAIN_OLD])
        logs.append(('REGRESSIONE_52', status, result))
        print('Regressione 52 verifiche:', 'OK' if not status else 'ERRORE', flush=True)
        if status or 'TOP_FLOP_JAVA_TEST_OK checks=52' not in result:
            raise RuntimeError('Regressione 52 non superata: ' + result[-2500:])
        selected = database_paths if args.test_db == 'both' else [
            p for p in database_paths if (p.name == 'recordsnext.db') == (args.test_db == 'main')]
        report['testedDatabaseNames'] = [p.name for p in selected]
        for db in selected:
            status, result = invoke([java, '-Xmx2048m', '-cp', classpath, MAIN_NEW, str(db), '--quick'])
            logs.append(('FCA_' + db.stem.replace('-', '_'), status, result))
            last = result.strip().splitlines()[-1] if result.strip() else ''
            print('Verifica FCA', db.name, ':', last if not status else 'ERRORE', flush=True)
            if status or 'TOP_FLOP_FCA_JDBC_OK seasons=21' not in result:
                raise RuntimeError('Verifica FCA fallita (' + db.name + '): ' + result[-2500:])
        database_after = {p.name: sha(p) for p in database_paths}
        report['databaseSha256After'] = database_after
        if database_after != database_before:
            raise RuntimeError('Database 3.2 modificato: STOP')
        for name in NEW_FILES:
            source, target = kit / name, work / name
            if target.is_file():
                print('GIA PRESENTE IDENTICO:', name, flush=True)
            else:
                target.parent.mkdir(parents=True, exist_ok=True)
                with target.open('xb') as stream:
                    stream.write(source.read_bytes())
                report['filesInstallati'].append(name)
                print('INSTALLATO:', name, flush=True)
        status, result = invoke([sys.executable,
                                 str(work / 'tools/Create-RecordsNext4WorkingCodeMd-v16.py'),
                                 '--project-dir', str(work)])
        logs.append(('BIBBIA_V16', status, result))
        if status:
            raise RuntimeError('Bibbia v16 non generata: ' + result[-2500:])
        print('Bibbia v16: OK', flush=True)
        if {name: sha(work / name) for name in PRESERVE} != before:
            raise RuntimeError('File protetti della 3.2/TopFlop alterati: STOP')
        report['preservedSha256After'] = before
        report['bibbiaSha256'] = sha(work / 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v16.md')
        report['stato'] = 'OK'
        success = True
    except Exception:
        error = traceback.format_exc()
        logs.append(('ERRORE', 1, error))
        print('ERRORE FCA JDBC:\n' + error[-3500:], file=sys.stderr, flush=True)
    finally:
        if build.exists():
            shutil.rmtree(build)
        with ZipFile(result_zip, 'x', ZIP_DEFLATED) as z:
            z.writestr('MANIFEST.json', json.dumps(report, indent=2, ensure_ascii=False) + '\n')
            for label, code, result in logs:
                z.writestr('log/' + label + '.txt', 'exit_code=' + str(code) + '\n' + result)
            if work is not None:
                for name in NEW_FILES:
                    path = work / name
                    if path.is_file():
                        z.write(path, name)
                path = work / 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v16.md'
                if path.is_file():
                    z.write(path, 'docs/' + path.name)
        print('ESITO:', 'OK' if success else 'ERRORE', flush=True)
        print('ZIP DA CARICARE:', result_zip, flush=True)
    return 0 if success else 1


if __name__ == '__main__':
    sys.exit(main())
