#!/usr/bin/env python3
"""Safe, idempotent RecordsNext 4.0 Top & Flop canonical-round patch."""
from __future__ import annotations
import argparse
import datetime
import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys
import traceback
import zipfile

KIT = Path(__file__).resolve().parent.parent
PATCH = [
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopFacts.java',
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopJdbcArchiveAssembler.java',
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopCanonicalRoundResolver.java',
 'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopCanonicalRoundSmokeTest.java',
 'docs/TOP_FLOP_CANONICAL_ROUNDS_PATCH_v1.md',
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v22.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v21.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v21.md',
 'tools/Create-RecordsNext4WorkingCodeMd-v21.py',
 'tools/Invoke-RecordsNext4CanonicalRounds-v1.py',
]
BASE = 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v20.md'
BACKUP = 'backups/topflop-canonical-rounds-v1'

def sha(p: Path) -> str: return hashlib.sha256(p.read_bytes()).hexdigest()

def run(command, log):
    r = subprocess.run([str(x) for x in command], text=True, encoding='utf-8', errors='replace',
                       stdout=subprocess.PIPE, stderr=subprocess.STDOUT, check=False)
    log.append('COMMAND: ' + ' '.join(Path(str(x)).name if i>3 else str(x) for i,x in enumerate(command[:5])))
    log.append(r.stdout[-20000:])
    if r.returncode != 0: raise RuntimeError('Command failed with code ' + str(r.returncode) + ':\n' + r.stdout[-4500:])
    return r.stdout

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--project-dir', default=r'D:\DEV_APPS\RecordsNext4.0')
    ap.add_argument('--download-dir', default=r'D:\DEV_APPS\downloads')
    ap.add_argument('--database', default=None)
    ap.add_argument('--jar', default=None)
    a = ap.parse_args()
    root=Path(a.project_dir).resolve()
    downloads=Path(a.download_dir).resolve()
    downloads.mkdir(parents=True,exist_ok=True)
    stamp=datetime.datetime.now().strftime('%Y%m%d_%H%M%S_%f')
    out=downloads/('RecordsNext4_CanonicalRounds_OUTPUT_v1_'+stamp+'.zip')
    logs=[]; state={'status':'ERROR','database':None,'project':str(root),'checks':[],'installed':[],'planned':[],'identical':[]}
    build=downloads/('RecordsNext4_CanonicalRounds_BUILD_v1_'+stamp)
    changed=[]
    originals={}
    try:
        if not (root/'pom.xml').is_file() or not (root/BASE).is_file():
            raise RuntimeError('Not the expected RecordsNext4.0 worktree or missing bible v20')
        manifest=json.loads((KIT/'manifest-sha256.json').read_text(encoding='utf-8'))
        for rel in PATCH:
            source=KIT/rel; target=root/rel
            if not source.is_file():raise RuntimeError('Missing patch file: '+rel)
            if target.exists():
                if not target.is_file():raise RuntimeError('Destination is not a file: '+rel)
                if sha(target)!=sha(source):
                    if rel in manifest['expected_existing'] and sha(target)==manifest['expected_existing'][rel]:
                        originals[rel]=target.read_bytes()
                    else: raise RuntimeError('Existing file differs, safe install stopped: '+rel)
                else:state['identical'].append(rel)
            else:state['planned'].append(rel)
        for rel in originals:state['planned'].append(rel)
        # Compile patched Java in isolation, BEFORE touching the project.
        classes=build/'classes'; classes.mkdir(parents=True,exist_ok=True)
        replacement={x for x in manifest['expected_existing']} | {'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopCanonicalRoundResolver.java'}
        source_files=[x for x in (root/'src/main/java').rglob('*.java')
                      if x.relative_to(root).as_posix() not in replacement]
        for rel in manifest['expected_existing']:
            source_files.append(KIT/rel)
        source_files.append(KIT/'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopCanonicalRoundResolver.java')
        java=shutil.which('java');javac=shutil.which('javac')
        if not java or not javac:raise RuntimeError('JDK 21+ not found in PATH')
        run([javac,'--release','21','-encoding','UTF-8','-d',classes,*source_files],logs)
        state['checks'].append('Java 21 full source compilation OK')
        tests=[root/'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopArchiveSmokeTest.java',
               KIT/'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopCanonicalRoundSmokeTest.java']
        run([javac,'--release','21','-encoding','UTF-8','-cp',classes,'-d',classes,*tests],logs)
        main_test='it.alterlega.recordsnext.app.topflop.TopFlopArchiveSmokeTest'
        testout=run([java,'-ea','-cp',classes,main_test],logs)
        if 'checks=52' not in testout:raise RuntimeError('52-check regression signature absent')
        state['checks'].append('52 Java regression checks OK')
        jar=Path(a.jar) if a.jar else next((x for x in [root/'target/RecordsNext.jar',
                root.parent/'RecordsNext2.0/target/RecordsNext.jar',
                root.parent/'RecordsNext2.0/release/RecordsNext_3.2.0_FULL/payload/RecordsNext.jar'] if x.is_file()),None)
        db=Path(a.database) if a.database else next(iter(sorted((root/'data/database').glob('recordsnext-2026_2027-verifica-v1_*.db'))),None)
        if jar and db and jar.is_file() and db.is_file():
            test='it.alterlega.recordsnext.app.topflop.TopFlopCanonicalRoundSmokeTest'
            o=run([java,'-ea','-cp',str(classes)+os.pathsep+str(jar),test,db],logs)
            if 'unmapped2022Facts=100' not in o:
                raise RuntimeError('2022 unmapped-competition regression signature absent')
            state['checks'].append('21 seasons and 2022/2026 canonical matches OK')
            state['database']=str(db)
        else:
            state['checks'].append('JDBC sample skipped (updated SQLite or shaded 3.2 JAR not found)')
        # All preflight checks passed. Keep full original bytes before any replacement.
        for rel,data in originals.items():
            back=root/BACKUP/rel
            back.parent.mkdir(parents=True,exist_ok=True)
            if back.exists() and back.read_bytes()!=data:
                raise RuntimeError('Conflicting backup, nothing replaced: '+str(back))
            if not back.exists(): back.write_bytes(data)
        bible_preexisting=(root/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v21.md').exists()
        try:
            for rel in PATCH:
                target=root/rel
                if target.is_file() and sha(target)==sha(KIT/rel):continue
                target.parent.mkdir(parents=True,exist_ok=True)
                target.write_bytes((KIT/rel).read_bytes())
                changed.append(rel)
            state['installed']=changed.copy()
            generator=root/'tools/Create-RecordsNext4WorkingCodeMd-v21.py'
            run([sys.executable,generator,'--project-dir',root],logs)
            state['checks'].append('Versioned bible v21 generated')
        except Exception:
            for rel in reversed(changed):
                target=root/rel
                if rel in originals:target.write_bytes(originals[rel])
                else:target.unlink(missing_ok=True)
            if not bible_preexisting:
                (root/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v21.md').unlink(missing_ok=True)
            raise
        state['status']='OK'
        print('ESITO: OK')
        for label in state['checks']:print('VERIFICA:',label)
        print('FILE INSTALLATI/AGGIORNATI:',len(state['installed']))
        print('FILE GIA IDENTICI:',len(state['identical']))
        print('BIBBIA: docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v21.md')
    except Exception as e:
        state['error']=repr(e)
        logs.append(traceback.format_exc())
        print('ESITO: ERRORE\n'+str(e),file=sys.stderr)
    finally:
        with zipfile.ZipFile(out,'w',zipfile.ZIP_DEFLATED) as z:
            z.writestr('result.json',json.dumps(state,ensure_ascii=False,indent=2))
            z.writestr('execution.txt','\n'.join(logs))
            z.write(KIT/'docs/TOP_FLOP_CANONICAL_ROUNDS_PATCH_v1.md','specification.md')
        shutil.rmtree(build,ignore_errors=True)
        print('ZIP DA CARICARE:',out)
    return 0 if state['status']=='OK' else 1

if __name__=='__main__':sys.exit(main())
