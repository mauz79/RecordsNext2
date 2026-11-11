#!/usr/bin/env python3
"""Safe Windows-compatible installation and read-only FCM JDBC audit."""
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
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopFcmJdbcReader.java',
    'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopFcmJdbcSmokeTest.java',
    'docs/SPECIFICA_LETTORE_JAVA_FCM_TOP_FLOP_v1.md',
    'docs/VERIFICA_LETTORE_JAVA_FCM_TOP_FLOP_v1.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v18.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v17.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v17.md',
    'tools/Create-RecordsNext4WorkingCodeMd-v17.py',
    'tools/Invoke-RecordsNext4FcmJdbc-v1.py',
]
REQUIRED = [
    'pom.xml',
    'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v16.md',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopFcaJdbcReader.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopArchive.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopFacts.java',
    'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopArchiveSmokeTest.java',
    'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopFcaJdbcSmokeTest.java',
]
PRESERVED = [
    'pom.xml',
    'src/main/java/it/alterlega/recordsnext/app/model/RecordFamily.java',
    'src/main/java/it/alterlega/recordsnext/app/model/CoreRecordCatalog.java',
    'src/main/java/it/alterlega/recordsnext/app/RecordsNextPipeline.java',
    'src/main/java/it/alterlega/recordsnext/Records2026SitePublisher.java',
    'src/main/java/it/alterlega/recordsnext/app/output/SeasonFamilyShardPublisher.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopFcaJdbcReader.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopArchive.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopMetrics.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopFacts.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopCompetitionPolicy.java',
]

def sha(path: Path) -> str:
    h = hashlib.sha256()
    with path.open('rb') as stream:
        for block in iter(lambda: stream.read(1048576), b''):
            h.update(block)
    return h.hexdigest()

def command(cmd: list[str]) -> tuple[int, str]:
    proc = subprocess.run(cmd, capture_output=True, text=True,
                          encoding='utf-8', errors='replace',
                          env=dict(os.environ, PYTHONDONTWRITEBYTECODE='1'))
    return proc.returncode, proc.stdout + proc.stderr

def git(root: Path, *args: str) -> str:
    code, output = command(['git', '-C', str(root), *args])
    if code: raise RuntimeError('Git: ' + output[-1200:])
    return output.strip()

def shaded_jar(paths: list[Path]) -> Path:
    for p in paths:
        if not p.is_file(): continue
        with ZipFile(p) as zipf:
            if 'org/sqlite/JDBC.class' in zipf.namelist(): return p
    raise RuntimeError('RecordsNext 3.2 shaded JAR con org.sqlite.JDBC non trovato')

def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument('--work-dir', type=Path, default=Path('D:/DEV_APPS/RecordsNext4.0'))
    parser.add_argument('--base-dir', type=Path, default=Path('D:/DEV_APPS/RecordsNext2.0'))
    parser.add_argument('--output-dir', type=Path, default=Path('D:/DEV_APPS/downloads'))
    parser.add_argument('--jar', type=Path, default=None, help='Solo per test: shaded JAR 3.2')
    args = parser.parse_args()
    kit = Path(__file__).resolve().parent.parent
    output = args.output_dir.resolve(strict=True)
    stamp = datetime.now().strftime('%Y%m%d_%H%M%S_%f')
    result = output/('RecordsNext4_FcmJdbc_OUTPUT_v1_' + stamp + '.zip')
    build = output/('RecordsNext4_FcmJdbc_BUILD_v1_' + stamp)
    report = {'phase': 'TopFlop-Java-FCM-JDBC-v1', 'status': 'ERROR',
              'branch': None, 'databaseModified': None, 'published': False,
              'bible': 'v17', 'installedFiles': []}
    logs: list[tuple[str, int, str]] = []
    work: Path | None = None
    success = False
    try:
        work = args.work_dir.resolve(strict=True)
        base = args.base_dir.resolve(strict=True)
        if work == base or work == kit:
            raise RuntimeError('Cartella 4.0 deve essere diversa da 3.2 e dal kit')
        for name in REQUIRED:
            if not (work/name).is_file(): raise RuntimeError('Prerequisito mancante: ' + name)
        if Path(git(work, 'rev-parse', '--show-toplevel')).resolve(strict=True) != work:
            raise RuntimeError('La cartella 4.0 non coincide con la root Git')
        branch = git(work, 'branch', '--show-current')
        if branch != 'feature/recordsnext4-players': raise RuntimeError('Branch errato: ' + branch)
        report['branch'] = branch
        report['head'] = git(work, 'rev-parse', 'HEAD')
        preserved = {name: sha(work/name) for name in PRESERVED}
        report['preservedSha256Before'] = preserved
        for name in NEW_FILES:
            source = kit/name
            dest = work/name
            if not source.is_file(): raise RuntimeError('File non presente nel kit: ' + name)
            if dest.exists() and (not dest.is_file() or sha(dest) != sha(source)):
                raise RuntimeError('File gia presente DIVERSO, nessuna sovrascrittura: ' + name)
        dbpaths = [base/'data/database/recordsnext.db',
                   base/'data/database/recordsnext-sandbox.db']
        if not all(path.is_file() for path in dbpaths):
            raise RuntimeError('Database 3.2 assente: non avvio la verifica')
        db_before = {p.name: sha(p) for p in dbpaths}
        report['databaseSha256Before'] = db_before
        jar = (args.jar.resolve(strict=True) if args.jar else shaded_jar([
            base/'release/RecordsNext_3.2.0_FULL/payload/RecordsNext.jar',
            base/'target/RecordsNext.jar', work/'target/RecordsNext.jar']))
        if not jar.is_file(): raise RuntimeError('JAR non trovato: ' + str(jar))
        report['jar'] = str(jar)
        java, javac = shutil.which('java'), shutil.which('javac')
        if not java or not javac: raise RuntimeError('JDK 21: java o javac mancanti')
        code, text = command([javac, '-version'])
        logs.append(('JDK', code, text))
        version = re.search(r'\bjavac\s+(\d+)(?:\.|\s|$)', text)
        if code or version is None or int(version.group(1)) < 21:
            raise RuntimeError('Richiesto JDK 21+: ' + text)
        build.mkdir(exist_ok=False)
        new_java = list((kit/'src/main/java').rglob('*.java'))
        new_paths = {p.relative_to(kit).as_posix() for p in new_java}
        existing = sorted(p for p in (work/'src/main/java').rglob('*.java')
                          if p.relative_to(work).as_posix() not in new_paths)
        if len(new_java) != 1 or len(existing) < 70:
            raise RuntimeError('Conteggio sorgenti inatteso: ' + str((len(new_java),len(existing))))
        sources = [str(p) for p in existing + new_java] + [
            str(work/'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopArchiveSmokeTest.java'),
            str(work/'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopFcaJdbcSmokeTest.java'),
            str(kit/'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopFcmJdbcSmokeTest.java')]
        code, text = command([javac, '-encoding', 'UTF-8', '--release', '21',
                              '-cp', str(jar), '-d', str(build), *sources])
        logs.append(('COMPILA_COMPLETA', code, text))
        print('Compilazione Java 3.2 + TopFlop:', 'OK' if code == 0 else 'ERRORE', flush=True)
        if code: raise RuntimeError('Compilazione: ' + text[-2500:])
        cp = os.pathsep.join((str(build), str(jar)))
        main_old = 'it.alterlega.recordsnext.app.topflop.TopFlopArchiveSmokeTest'
        code, text = command([java, '-ea', '-cp', cp, main_old])
        logs.append(('REGRESSIONE_52', code, text))
        if code or 'TOP_FLOP_JAVA_TEST_OK checks=52' not in text:
            raise RuntimeError('52 test precedenti non superati: ' + text[-2500:])
        print('Regressione 52 test: OK', flush=True)
        main_fca = 'it.alterlega.recordsnext.app.topflop.TopFlopFcaJdbcSmokeTest'
        code, text = command([java, '-Xmx2048m', '-ea', '-cp', cp, main_fca,
                              str(dbpaths[1]), '--quick'])
        logs.append(('REGRESSIONE_FCA', code, text))
        if code or 'TOP_FLOP_FCA_JDBC_OK seasons=21' not in text:
            raise RuntimeError('Regressione FCA non superata: ' + text[-2500:])
        print('Regressione FCA: OK', flush=True)
        main_fcm = 'it.alterlega.recordsnext.app.topflop.TopFlopFcmJdbcSmokeTest'
        for database in dbpaths:
            code, text = command([java, '-Xmx2048m', '-ea', '-cp', cp, main_fcm,
                                  str(database)])
            logs.append(('FCM_' + database.stem.replace('-', '_'), code, text))
            last = text.strip().splitlines()[-1] if text.strip() else ''
            print('Verifica FCM', database.name, ':', last if code == 0 else 'ERRORE', flush=True)
            if code or 'TOP_FLOP_FCM_JDBC_OK seasons=4' not in text:
                raise RuntimeError('FCM non valido in ' + database.name + ': ' + text[-2500:])
        db_after = {p.name: sha(p) for p in dbpaths}
        report['databaseSha256After'] = db_after
        report['databaseModified'] = db_after != db_before
        if db_after != db_before: raise RuntimeError('Database modificato: STOP')
        for name in NEW_FILES:
            source, target = kit/name, work/name
            if target.exists():
                print('GIA PRESENTE IDENTICO:', name, flush=True)
            else:
                target.parent.mkdir(parents=True, exist_ok=True)
                with target.open('xb') as stream: stream.write(source.read_bytes())
                report['installedFiles'].append(name)
                print('INSTALLATO:', name, flush=True)
        code, text = command([sys.executable, str(work/'tools/Create-RecordsNext4WorkingCodeMd-v17.py'),
                              '--project-dir', str(work)])
        logs.append(('BIBBIA_V17', code, text))
        if code: raise RuntimeError('Bibbia v17: ' + text[-1500:])
        print('Bibbia v17: OK', flush=True)
        if {name: sha(work/name) for name in PRESERVED} != preserved:
            raise RuntimeError('Un file Java/3.2 preesistente e stato modificato!')
        report['preservedSha256After'] = preserved
        report['bibleSha256'] = sha(work/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v17.md')
        success = True
        report['status'] = 'OK'
    except Exception as exc:
        report['error'] = str(exc)
        report['trace'] = traceback.format_exc()[-3500:]
        print('ERRORE:', str(exc), file=sys.stderr, flush=True)
    finally:
        shutil.rmtree(build, ignore_errors=True)
        try:
            with ZipFile(result, 'w', compression=ZIP_DEFLATED, compresslevel=8) as z:
                z.writestr('MANIFEST.json', json.dumps(report, ensure_ascii=False, indent=2))
                for label, code, log in logs:
                    z.writestr('log/' + label + '.txt', 'exit_code=' + str(code) + '\n' + log)
                for name in NEW_FILES:
                    file = (work/name if work and (work/name).is_file() else kit/name)
                    if file.is_file(): z.write(file, name)
                if work:
                    bible = work/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v17.md'
                    if bible.is_file(): z.write(bible, 'docs/' + bible.name)
        except Exception as exc:
            print('ERRORE creazione ZIP:', exc, file=sys.stderr, flush=True)
            return 1
        print('ESITO:', 'OK' if success else 'ERRORE', flush=True)
        print('ZIP DA CARICARE:', result, flush=True)
    return 0 if success else 1

if __name__ == '__main__':
    sys.exit(main())
