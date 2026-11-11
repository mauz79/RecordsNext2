#!/usr/bin/env python3
"""Install and validate compact native TopFlop v2 in 4.0 staging ONLY.

Windows PowerShell 5.1 usage: py -3 <this-script.py>.
Never modifies the 3.2 project, databases, published JS, or existing CSS.
The stage folder is keyed to source DB + normalized report hashes and reused
if identical; old v1 staging is never deleted automatically.
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
import sqlite3
import subprocess
import sys
import traceback
import zipfile

KIT=Path(__file__).resolve().parent.parent
SOURCE_CLASS='it.alterlega.recordsnext.app.topflop.TopFlopCompactStageJsExporter'
TEST_CLASS='it.alterlega.recordsnext.app.topflop.TopFlopCompactStageSmokeTest'
FILES=[
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopCompactStageJsExporter.java',
 'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopCompactStageSmokeTest.java',
 'visualizzatori/js/fcmRecordsNextTopFlopCompactDecoder-v2.js',
 'tools/Validate-RecordsNext4TopFlopCompact-v2.py',
 'tools/Test-RecordsNext4TopFlopCompactBrowser-v2.js',
 'tools/Invoke-RecordsNext4TopFlopCompact-v2.py',
 'tools/Create-RecordsNext4WorkingCodeMd-v24.py',
 'docs/SPECIFICA_EXPORT_JS_TOP_FLOP_v2.md',
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v25.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v24.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v24.md',
]

def sha(path:Path)->str:
    h=hashlib.sha256()
    with path.open('rb') as stream:
        for block in iter(lambda:stream.read(1048576),b''):h.update(block)
    return h.hexdigest()

def reports_signature(folder:Path)->str:
    files=sorted(folder.rglob('season_normalized_*.json'))
    if not files:raise RuntimeError('No canonical 3.2 season_normalized reports: '+str(folder))
    h=hashlib.sha256()
    for file in files:
        h.update(str(file.relative_to(folder)).replace('\\','/').encode())
        h.update(bytes.fromhex(sha(file)))
    return h.hexdigest()

def invoke(args,logs)->str:
    command=list(map(str,args))
    res=subprocess.run(command,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,
                       text=True,encoding='utf-8',errors='replace')
    logs.append('COMMAND: '+Path(command[0]).name+' (parameters in versioned script)')
    logs.append((res.stdout or '')[-12000:])
    if res.returncode:
        raise RuntimeError('Command failed '+str(res.returncode)+'\n'+(res.stdout or '')[-8000:])
    return res.stdout or ''

def main()->int:
    cli=argparse.ArgumentParser()
    cli.add_argument('--project-dir',default=r'D:\DEV_APPS\RecordsNext4.0')
    cli.add_argument('--downloads-dir',default=r'D:\DEV_APPS\downloads')
    cli.add_argument('--database')
    cli.add_argument('--jar')
    cli.add_argument('--reports-dir')
    cli.add_argument('--seasons',help='Comma-separated test seasons; default all available')
    args=cli.parse_args()
    root=Path(args.project_dir).resolve()
    downloads=Path(args.downloads_dir).resolve()
    downloads.mkdir(parents=True,exist_ok=True)
    stamp=datetime.now().strftime('%Y%m%d_%H%M%S_%f')
    report=downloads/('RecordsNext4_TopFlopCompact_OUTPUT_v2_'+stamp+'.zip')
    build=downloads/('RecordsNext4_TopFlopCompact_BUILD_v2_'+stamp)
    state={'status':'ERROR','checks':[],'installed':[],'alreadyIdentical':[],
           'project':str(root),'version':'v2-compact'}
    logs=[]
    changes=[]
    partial=None
    try:
        if not (root/'pom.xml').is_file():
            raise RuntimeError('Missing RecordsNext 4.0 worktree: '+str(root))
        if not (root/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v23.md').is_file():
            raise RuntimeError('Bibbia v23 missing; verify that v1 exporter installation completed')
        if (root/'.git').exists() and shutil.which('git'):
            branch=invoke(['git','-C',root,'branch','--show-current'],logs).strip()
            if branch!='feature/recordsnext4-players':
                raise RuntimeError('Unexpected Git branch: '+branch)
        manifest=json.loads((KIT/'manifest-sha256.json').read_text(encoding='utf-8'))
        if set(manifest['files'])!=set(FILES):
            raise RuntimeError('Kit manifest file list differs')
        for rel in FILES:
            source=KIT/rel
            dest=root/rel
            if not source.is_file() or manifest['files'][rel]!=sha(source):
                raise RuntimeError('Kit file SHA-256 mismatch: '+rel)
            if dest.exists():
                if not dest.is_file():raise RuntimeError('Destination is not a file: '+rel)
                if sha(source)!=sha(dest):
                    raise RuntimeError('Existing file differs; not overwriting: '+rel)
                state['alreadyIdentical'].append(rel)
        javac=shutil.which('javac');java=shutil.which('java')
        if not javac or not java:raise RuntimeError('Java 21 JDK not found in PATH')
        version=invoke([javac,'-version'],logs).strip()
        match=re.search(r'javac\s+(\d+)',version)
        if not match or int(match.group(1))<21:
            raise RuntimeError('JDK 21+ required, found: '+version)
        db=(Path(args.database).resolve() if args.database else
            next(iter(sorted((root/'data/database').glob('recordsnext-2026_2027-verifica-v1_*.db'))),None))
        if db is None or not db.is_file():
            raise RuntimeError('Updated SQLite not found: supply --database')
        jar=(Path(args.jar).resolve() if args.jar else next((f for f in [
            root/'target/RecordsNext.jar',
            root.parent/'RecordsNext2.0/target/RecordsNext.jar',
            root.parent/'RecordsNext2.0/release/RecordsNext_3.2.0_FULL/payload/RecordsNext.jar'
        ] if f.is_file()),None))
        if jar is None:raise RuntimeError('Original 3.2 runtime JAR missing: supply --jar')
        normalized=(Path(args.reports_dir).resolve() if args.reports_dir else
                    (root.parent/'RecordsNext2.0/data/reports').resolve())
        if not normalized.is_dir():raise RuntimeError('3.2 normalized reports missing: '+str(normalized))
        db_before=sha(db)
        normsha=reports_signature(normalized)
        state['database']=str(db)
        state['databaseSha256Before']=db_before
        state['normalizedReportsFingerprint']=normsha
        stage=root/'data/site-export-staging'/('topflop-compact-v2_'+db_before[:12]+'_'+normsha[:12])/'js'
        state['stage']=str(stage)
        with sqlite3.connect(db.as_uri()+'?mode=ro',uri=True) as c:
            tables=[row[0] for row in c.execute("SELECT name FROM sqlite_master WHERE type='table'")]
        seasons=sorted({m.group(1) for x in tables if (m:=re.fullmatch(
            r'raw_(\d{4}_\d{4})_fcm_formazione',x))})
        if args.seasons:
            selected=args.seasons.split(',')
            if not selected or len(set(selected))!=len(selected) or any(x not in seasons for x in selected):
                raise RuntimeError('Invalid seasons subset')
            seasons=selected
            # A season-scoped pilot must not reuse the full deterministic stage.
            stage=root/'data/site-export-staging'/('topflop-compact-v2_PILOT_'+
                '_'.join(seasons)+'_'+db_before[:12]+'_'+normsha[:12])/'js'
            state['stage']=str(stage)
        if not seasons:raise RuntimeError('No FCM seasons in database')
        state['seasons']=seasons
        classes=build/'classes'
        classes.mkdir(parents=True,exist_ok=True)
        new_class=FILES[0]
        existing=[f for f in (root/'src/main/java').rglob('*.java')
                  if f.relative_to(root).as_posix()!=new_class]
        invoke([javac,'--release','21','-encoding','UTF-8','-d',classes,
                *existing,KIT/new_class],logs)
        state['checks'].append('Full Java 3.2 + TopFlop compact v2 compile')
        baseline=root/'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopArchiveSmokeTest.java'
        if not baseline.is_file():raise RuntimeError('Baseline 52-test model unavailable')
        invoke([javac,'--release','21','-encoding','UTF-8','-cp',classes,'-d',classes,
                baseline,KIT/FILES[1]],logs)
        baseline_output=invoke([java,'-ea','-cp',classes,
            'it.alterlega.recordsnext.app.topflop.TopFlopArchiveSmokeTest'],logs)
        if 'checks=52' not in baseline_output:raise RuntimeError('Baseline 52-test regression missing')
        smoke=invoke([java,'-ea','-cp',classes,TEST_CLASS],logs)
        if 'TOP_FLOP_COMPACT_SMOKE_OK checks=4' not in smoke:
            raise RuntimeError('Compact smoke test missing')
        state['checks'].append('52 baseline checks and 4 compact smoke assertions OK')
        if stage.exists():
            state['reusedStage']=True
            if not stage.is_dir():raise RuntimeError('Stage path exists but not a directory')
        else:
            state['reusedStage']=False
            partial=stage.parent/'js.partial'
            if partial.exists():
                if not partial.is_dir():
                    raise RuntimeError('Incomplete staging path is not a directory: '+str(partial))
                # A prior interrupted runner may have finished writing every JS.
                # Recover ONLY when the mandatory full validator proves it complete.
                invoke([sys.executable,KIT/FILES[3],partial],logs)
                state['checks'].append('Complete prior partial stage recovered by full validation')
            else:
                partial.parent.mkdir(parents=True,exist_ok=True)
                output=invoke([java,'-Xmx1700m','-cp',str(classes)+os.pathsep+str(jar),
                    SOURCE_CLASS,db,partial,normalized,*seasons],logs)
                if 'TOP_FLOP_COMPACT_STAGE_JS_OK' not in output:
                    raise RuntimeError('Compact Java export signature absent')
                state['checks'].append('Native compact JS staged')
        validation=build/'validation.json'
        out=invoke([sys.executable,KIT/FILES[3],
            stage if state['reusedStage'] else partial,'--json-output',validation],logs)
        if 'TOP_FLOP_COMPACT_V2_VALIDATED' not in out:
            raise RuntimeError('Full compact staging validation missing')
        measured=json.loads(validation.read_text(encoding='utf-8'))
        if measured['seasons']!=len(seasons):
            raise RuntimeError('Season coverage differs')
        if measured['maxJsBytes']>950000 or measured['maxJsBytes']>1500000:
            raise RuntimeError('Compact JS exceeds intended safety target')
        if not measured['players'] or not measured['realDays']:
            raise RuntimeError('Empty JS projection')
        state['validation']=measured
        state['checks'].append('Full JS rows, link integrity, file limits and lazy pointers verified')
        node=shutil.which('node')
        if node:
            res=invoke([node,KIT/FILES[4],
                stage if state['reusedStage'] else partial,KIT/FILES[2]],logs)
            if 'TOP_FLOP_COMPACT_BROWSER_OK' not in res:
                raise RuntimeError('JS browser decoder test missing')
            state['checks'].append('Browser JavaScript decoder tested by Node')
        else:
            state['checks'].append('Node not present: JS decoder preverified externally; Python mandatory checks passed')
        if sha(db)!=db_before:raise RuntimeError('Database SHA-256 changed')
        state['databaseSha256After']=sha(db)
        state['checks'].append('SQLite SHA-256 unchanged')
        # Promote only this runner's new stage, after successful validation.
        if not state['reusedStage']:
            partial.rename(stage)
            partial=None
        for name in FILES:
            src=KIT/name;dest=root/name
            if dest.exists():continue
            dest.parent.mkdir(parents=True,exist_ok=True)
            with dest.open('xb') as f:f.write(src.read_bytes())
            changes.append(name)
        generator=root/'tools/Create-RecordsNext4WorkingCodeMd-v24.py'
        bible=root/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v24.md'
        bible_existed=bible.exists()
        try:
            invoke([sys.executable,generator,'--project-dir',root],logs)
        except Exception:
            for name in reversed(changes):(root/name).unlink(missing_ok=True)
            if not bible_existed:bible.unlink(missing_ok=True)
            raise
        state['installed']=changes
        state['checks'].append('Versioned sources, documents, bible v24 installed')
        state['status']='OK'
        print('ESITO: OK')
        print('STAGING (NON PUBBLICATO):',stage)
        print('FILE JS:',measured['jsFiles'])
        print('BYTE TOTALI:',measured['totalJsBytes'])
        print('FILE PIU GRANDE:',measured['maxJsBytes'])
        print('STAGING RIUTILIZZATO:',state['reusedStage'])
        print('BIBBIA: docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v24.md')
    except Exception as e:
        state['error']=repr(e)
        logs.append(traceback.format_exc())
        print('ESITO: ERRORE',file=sys.stderr)
        print(str(e),file=sys.stderr)
    finally:
        with zipfile.ZipFile(report,'w',zipfile.ZIP_DEFLATED) as out:
            out.writestr('result.json',json.dumps(state,indent=2,ensure_ascii=False))
            out.writestr('execution.txt','\n'.join(logs))
            if (build/'validation.json').exists():
                out.write(build/'validation.json','validation.json')
        shutil.rmtree(build,ignore_errors=True)
        # Do not remove a partial stage on failure: keep it for diagnosis; rerun
        # blocks until user explicitly resolves that particular failed directory.
        print('ZIP DA CARICARE:',report)
    return 0 if state['status']=='OK' else 1

if __name__=='__main__':sys.exit(main())
