#!/usr/bin/env python3
"""Conservative Windows-friendly installer: verify, compile and test before copying."""
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

from pathlib import Path as _P

NEW_FILES = [
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopFacts.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopArchive.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopMetrics.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopCompetitionPolicy.java',
    'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopArchiveSmokeTest.java',
    'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopArchiveJUnitTest.java',
    'docs/SPECIFICA_MODELLO_JAVA_TOP_FLOP_v1.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v16.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v15.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v15.md',
    'tools/Create-RecordsNext4WorkingCodeMd-v15.py',
    'tools/Invoke-RecordsNext4TopFlopJava-v1.py',
]
REQUIRED = [
    'pom.xml',
    'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v14.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v15.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v14.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v14.md',
    'src/main/java/it/alterlega/recordsnext/app/model/RecordFamily.java',
    'src/main/java/it/alterlega/recordsnext/app/model/CoreRecordCatalog.java',
]
PRESERVE = [
    'pom.xml',
    'src/main/java/it/alterlega/recordsnext/app/model/RecordFamily.java',
    'src/main/java/it/alterlega/recordsnext/app/model/CoreRecordCatalog.java',
    'src/main/java/it/alterlega/recordsnext/app/RecordsNextPipeline.java',
    'src/main/java/it/alterlega/recordsnext/Records2026SitePublisher.java',
    'src/main/java/it/alterlega/recordsnext/app/output/SeasonFamilyShardPublisher.java',
]
SMOKE = 'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopArchiveSmokeTest.java'
MAIN = 'it.alterlega.recordsnext.app.topflop.TopFlopArchiveSmokeTest'


def sha(path: Path) -> str:
    h = hashlib.sha256()
    with path.open('rb') as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b''):
            h.update(chunk)
    return h.hexdigest()


def git(root: Path, *args: str) -> str:
    p = subprocess.run(['git', '-C', str(root), *args], capture_output=True,
                       text=True, encoding='utf-8', errors='replace')
    if p.returncode:
        raise RuntimeError('Git error: ' + p.stderr.strip())
    return p.stdout.strip()


def call(command: list[str], cwd: Path | None = None) -> tuple[int, str]:
    process = subprocess.run(command, cwd=str(cwd) if cwd else None,
                             capture_output=True, text=True,
                             encoding='utf-8', errors='replace',
                             env=dict(os.environ, PYTHONDONTWRITEBYTECODE='1'))
    return process.returncode, process.stdout + process.stderr


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument('--work-dir', type=Path, default=Path('D:/DEV_APPS/RecordsNext4.0'))
    ap.add_argument('--output-dir', type=Path, default=Path('D:/DEV_APPS/downloads'))
    opts = ap.parse_args()
    kit = Path(__file__).resolve().parent.parent
    outputs = opts.output_dir.resolve(strict=True)
    stamp = datetime.now().strftime('%Y%m%d_%H%M%S_%f')
    archive = outputs / ('RecordsNext4_TopFlopJava_OUTPUT_v1_' + stamp + '.zip')
    build = outputs / ('RecordsNext4_TopFlopJava_BUILD_v1_' + stamp)
    report = {'fase': 'top-flop-java-model-v1', 'stato': 'ERRORE',
              'javaNuovo': True, 'javaEsistenteModificato': False,
              'databaseModificato': False, 'pubblicazioneEseguita': False,
              'mavenTestCompleto': False, 'bibbia': 'v15', 'filesInstallati': []}
    logs: list[tuple[str, int, str]] = []
    work = None
    succeeded = False
    try:
        work = opts.work_dir.resolve(strict=True)
        if kit == work:
            raise RuntimeError('Il kit non puo coincidere con il worktree')
        for name in REQUIRED:
            if not (work / name).is_file():
                raise RuntimeError('Prerequisito 4.0 mancante: ' + name)
        root = Path(git(work, 'rev-parse', '--show-toplevel')).resolve(strict=True)
        if os.path.normcase(str(root)).casefold() != os.path.normcase(str(work)).casefold():
            raise RuntimeError('Worktree non corrispondente a Git root: ' + str(root))
        branch = git(work, 'branch', '--show-current')
        if branch != 'feature/recordsnext4-players':
            raise RuntimeError('Branch inatteso: ' + branch)
        report['branch'] = branch
        report['head'] = git(work, 'rev-parse', 'HEAD')
        preserved = {name: sha(work / name) for name in PRESERVE}
        report['preservedSha256Before'] = preserved
        for name in NEW_FILES:
            source, destination = kit / name, work / name
            if not source.is_file():
                raise RuntimeError('File mancante nel kit: ' + name)
            if destination.exists() and (not destination.is_file() or sha(destination) != sha(source)):
                raise RuntimeError('Destinazione diversa; fermo senza sovrascrivere: ' + name)
        javac = shutil.which('javac')
        java = shutil.which('java')
        if not javac or not java:
            raise RuntimeError('JDK 21 o successivo richiesto: java/javac non trovati. Nessun file installato.')
        status, version = call([javac, '-version'])
        logs.append(('JDK', status, version))
        match = re.search(r'\bjavac\s+(\d+)(?:\.|\s|$)', version)
        if status or match is None or int(match.group(1)) < 21:
            raise RuntimeError('Richiesto JDK 21 o successivo, trovato: ' + version.strip())
        build.mkdir(parents=False, exist_ok=False)
        kit_sources = sorted((kit / 'src/main/java').rglob('*.java'))
        kit_source_names = {path.relative_to(kit).as_posix() for path in kit_sources}
        base_sources = sorted(path for path in (work / 'src/main/java').rglob('*.java')
                              if path.relative_to(work).as_posix() not in kit_source_names)
        if len(base_sources) < 50 or len(kit_sources) != 4:
            raise RuntimeError('Sorgenti Java inattesi: ' + str((len(base_sources), len(kit_sources))))
        command = [javac, '-encoding', 'UTF-8', '--release', '21', '-d', str(build)]
        command += [str(s) for s in [*base_sources, *kit_sources, kit / SMOKE]]
        status, output = call(command)
        logs.append(('COMPILA_JAVA21_COMPLETO', status, output))
        print('Compilazione Java 3.2 + TopFlop:', 'OK' if status == 0 else 'ERRORE', flush=True)
        if status:
            raise RuntimeError('Compilazione Java fallita: ' + output[-3000:])
        status, output = call([java, '-ea', '-cp', str(build), MAIN])
        logs.append(('TEST_TOPFLOP_52_ASSERT', status, output))
        print('Test TopFlop:', output.strip() if status == 0 else 'ERRORE', flush=True)
        if status or 'TOP_FLOP_JAVA_TEST_OK checks=52' not in output:
            raise RuntimeError('Test Java non superato: ' + output[-2000:])
        for name in NEW_FILES:
            source, destination = kit / name, work / name
            if destination.is_file():
                print('GIA PRESENTE IDENTICO:', name, flush=True)
            else:
                destination.parent.mkdir(parents=True, exist_ok=True)
                with destination.open('xb') as stream:
                    stream.write(source.read_bytes())
                report['filesInstallati'].append(name)
                print('INSTALLATO:', name, flush=True)
        command = [sys.executable, str(work / 'tools/Create-RecordsNext4WorkingCodeMd-v15.py'),
                   '--project-dir', str(work)]
        status, output = call(command)
        logs.append(('BIBBIA_V15', status, output))
        print('Bibbia v15:', 'OK' if status == 0 else 'ERRORE', flush=True)
        if status:
            raise RuntimeError('Bibbia non generata: ' + output[-2000:])
        after = {name: sha(work / name) for name in PRESERVE}
        if after != preserved:
            raise RuntimeError('Un file Java/pom 3.2 protetto e stato alterato: STOP')
        report['preservedSha256After'] = after
        report['bibbiaSha256'] = sha(work / 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v15.md')
        report['javaChecks'] = 52
        report['javaBaseSources'] = len(base_sources)
        report['javaNewSources'] = len(kit_sources)
        report['stato'] = 'OK'
        succeeded = True
    except Exception:
        error = traceback.format_exc()
        logs.append(('ERRORE', 1, error))
        print('Fase Java TopFlop fallita:\n' + error[-3500:], file=sys.stderr, flush=True)
    finally:
        if build.exists():
            shutil.rmtree(build)
        with ZipFile(archive, 'x', compression=ZIP_DEFLATED) as output_zip:
            output_zip.writestr('MANIFEST.json', json.dumps(report, indent=2, ensure_ascii=False) + '\n')
            for label, code, body in logs:
                output_zip.writestr('log/' + label + '.txt', 'exit_code=' + str(code) + '\n' + body)
            for name in NEW_FILES:
                file = work / name if work is not None else None
                if file is not None and file.is_file():
                    output_zip.write(file, name)
            if work is not None:
                bible = work / 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v15.md'
                if bible.is_file():
                    output_zip.write(bible, 'docs/' + bible.name)
        print('ESITO:', 'OK' if succeeded else 'ERRORE', flush=True)
        print('ZIP DA CARICARE:', archive, flush=True)
    return 0 if succeeded else 1


if __name__ == '__main__':
    sys.exit(main())
