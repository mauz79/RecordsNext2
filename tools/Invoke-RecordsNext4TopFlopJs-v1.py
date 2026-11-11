#!/usr/bin/env python3
"""Conservative, staging-only native TopFlop JS exporter installer.
Works in Windows PowerShell 5.1 via `py -3 this-script.py`.
No CSS, site, publisher, manifest or operational database writes.
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
PATCH=[
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopArchive.java',
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopStageJsExporter.java',
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopCanonicalScorecards.java',
 'src/main/java/it/alterlega/recordsnext/app/config/TopFlopExistingJson.java',
 'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopStageContractSmokeTest.java',
 'tools/Test-RecordsNext4TopFlopJs-v1.py',
 'tools/Create-RecordsNext4WorkingCodeMd-v23.py',
 'tools/Invoke-RecordsNext4TopFlopJs-v1.py',
 'docs/SPECIFICA_EXPORT_JS_TOP_FLOP_v1.md',
 'docs/VERIFICA_EXPORT_JS_TOP_FLOP_v1.md',
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v24.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v23.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v23.md'
]
ARCHIVE='src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopArchive.java'
STAGE_CLASS='it.alterlega.recordsnext.app.topflop.TopFlopStageJsExporter'
TEST_CLASS='it.alterlega.recordsnext.app.topflop.TopFlopStageContractSmokeTest'


def sha(path:Path)->str:
 h=hashlib.sha256()
 with path.open('rb') as inp:
  for part in iter(lambda:inp.read(1048576),b''):h.update(part)
 return h.hexdigest()


def run(args,logs, max_stdout=30000):
 command=[str(x) for x in args]
 proc=subprocess.run(command,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,
                     text=True,encoding='utf-8',errors='replace')
 output=proc.stdout or ''
 logs.append('COMMAND: '+Path(command[0]).name+' (args in installed runner source)')
 logs.append(output[-max_stdout:])
 if proc.returncode:raise RuntimeError('Command failed '+str(proc.returncode)+'\n'+output[-5000:])
 return output


def main()->int:
 p=argparse.ArgumentParser()
 p.add_argument('--project-dir',default=r'D:\DEV_APPS\RecordsNext4.0')
 p.add_argument('--downloads-dir',default=r'D:\DEV_APPS\downloads')
 p.add_argument('--database')
 p.add_argument('--jar')
 p.add_argument('--reports-dir')
 p.add_argument('--seasons',help='Optional comma-separated pilot; default ALL seasons')
 a=p.parse_args()
 root=Path(a.project_dir).resolve()
 downloads=Path(a.downloads_dir).resolve()
 downloads.mkdir(parents=True,exist_ok=True)
 stamp=datetime.now().strftime('%Y%m%d_%H%M%S_%f')
 report=downloads/('RecordsNext4_TopFlopJs_OUTPUT_v1_'+stamp+'.zip')
 build=downloads/('RecordsNext4_TopFlopJs_BUILD_v1_'+stamp)
 staging=root/'data/site-export-staging'/('topflop-stage-v1_'+stamp)/'js'
 state={'status':'ERROR','project':str(root),'stage':str(staging),
        'installed':[],'alreadyIdentical':[],'checks':[], 'historicalCount':None}
 logs=[];changed=[];preexisting={};was_there=set()
 manifest=None
 try:
  if not (root/'pom.xml').is_file() or not (root/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v22.md').is_file():
   raise RuntimeError('Worktree 4.0 with bible v22 not found: '+str(root))
  if (root/'.git').exists() and shutil.which('git'):
   branch=run(['git','-C',root,'branch','--show-current'],logs).strip()
   if branch!='feature/recordsnext4-players':
    raise RuntimeError('Unexpected Git branch: '+branch)
  manifest=json.loads((KIT/'manifest-sha256.json').read_text(encoding='utf-8'))
  if manifest.get('baseArchiveSha256') is None:raise RuntimeError('Invalid kit manifest')
  for rel in PATCH:
   src=KIT/rel;dst=root/rel
   if not src.is_file() or manifest['files'].get(rel)!=sha(src):
    raise RuntimeError('Kit integrity error: '+rel)
   if dst.exists():
    was_there.add(rel)
    if not dst.is_file():raise RuntimeError('Destination is not a file: '+rel)
    if sha(dst)==sha(src):state['alreadyIdentical'].append(rel)
    elif rel==ARCHIVE and sha(dst)==manifest['baseArchiveSha256']:
     preexisting[rel]=dst.read_bytes()
    else:raise RuntimeError('Different existing file, refusing overwrite: '+rel)
  java=shutil.which('java');javac=shutil.which('javac')
  if not java or not javac:raise RuntimeError('JDK 21+ not found in PATH')
  if '21' not in run([javac,'-version'],logs):
   raise RuntimeError('JDK 21 required for this tested build')
  db=Path(a.database) if a.database else next(iter(sorted(
   (root/'data/database').glob('recordsnext-2026_2027-verifica-v1_*.db'))),None)
  if db is None or not db.is_file():
   raise RuntimeError('Updated 2026/27 test SQLite missing: use --database')
  db=db.resolve();db_before=sha(db);state['database']=str(db)
  jar=Path(a.jar) if a.jar else next((x for x in [
   root/'target/RecordsNext.jar',
   root.parent/'RecordsNext2.0/target/RecordsNext.jar',
   root.parent/'RecordsNext2.0/release/RecordsNext_3.2.0_FULL/payload/RecordsNext.jar'
  ] if x.is_file()),None)
  if jar is None:raise RuntimeError('Original 3.2 runtime JAR not found: use --jar')
  reports=Path(a.reports_dir) if a.reports_dir else root.parent/'RecordsNext2.0/data/reports'
  reports=reports.resolve()
  if not reports.is_dir():raise RuntimeError('Official 3.2 normalized reports not found: '+str(reports)+' ; use --reports-dir')
  reports_arg=str(reports)
  state['normalizedReportsRoot']=reports_arg
  with sqlite3.connect(db.as_uri()+'?mode=ro',uri=True) as con:
   tables=[r[0] for r in con.execute("select name from sqlite_master where type='table'")]
  all_seasons=sorted({x[4:13] for x in tables if re.fullmatch(r'raw_\d{4}_\d{4}_fcm_formazione',x)})
  if a.seasons:
   seasons=a.seasons.split(',')
   if not seasons or len(set(seasons))!=len(seasons) or any(x not in all_seasons for x in seasons):
    raise RuntimeError('Invalid --seasons list')
  else:seasons=all_seasons
  if not seasons:raise RuntimeError('No FCM seasons in updated SQLite')
  state['seasonIds']=seasons
  classes=build/'classes';classes.mkdir(parents=True,exist_ok=True)
  replacements={x for x in PATCH if x.startswith('src/main/java/')}
  all_sources=[x for x in (root/'src/main/java').rglob('*.java') if x.relative_to(root).as_posix() not in replacements]
  all_sources.extend(KIT/x for x in sorted(replacements))
  run([javac,'--release','21','-encoding','UTF-8','-d',classes,*all_sources],logs)
  state['checks'].append('Full Java compile OK')
  tests=[root/'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopArchiveSmokeTest.java',
         KIT/'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopStageContractSmokeTest.java']
  run([javac,'--release','21','-encoding','UTF-8','-cp',classes,'-d',classes,*tests],logs)
  output=run([java,'-ea','-cp',classes,'it.alterlega.recordsnext.app.topflop.TopFlopArchiveSmokeTest'],logs)
  if 'checks=52' not in output:raise RuntimeError('52-check regression signature absent')
  state['checks'].append('TopFlop baseline 52 assertions OK')
  output=run([java,'-ea','-cp',classes,TEST_CLASS,reports_arg,build/'oversize_negative_test'],logs)
  if 'TOP_FLOP_STAGE_CONTRACT_OK' not in output:
   raise RuntimeError('New exporter smoke test incomplete')
  state['checks'].append('Canonical 3.2 links and JSON contract test OK')
  # ALWAYS staging only; never pass a real site's js directory.
  output=run([java,'-Xmx1700m','-cp',str(classes)+os.pathsep+str(jar),
      STAGE_CLASS,db,staging,reports_arg,*seasons],logs)
  if 'TOP_FLOP_STAGE_JS_OK' not in output:raise RuntimeError('Exporter signature absent')
  state['checks'].append('Native JS staging export OK')
  validation=build/'validation.json'
  output=run([sys.executable,KIT/'tools/Test-RecordsNext4TopFlopJs-v1.py',
        staging,'--json-output',validation],logs)
  if 'TOP_FLOP_STAGE_JS_VALIDATE_OK' not in output:
   raise RuntimeError('JS content validation incomplete')
  state['historicalCount']=json.loads(validation.read_text(encoding='utf-8'))
  if state['historicalCount']['seasons']!=len(seasons):
   raise RuntimeError('Stage season scope mismatch')
  state['checks'].append('Every JS file <=1,500,000 bytes, lazy indexes and row coverage OK')
  if sha(db)!=db_before:raise RuntimeError('Source SQLite changed during read-only export')
  state['checks'].append('SQLite SHA-256 unchanged')
  backup=root/'backups/topflop-js-exporter-v23'/ARCHIVE
  if ARCHIVE in preexisting:
   if backup.exists() and backup.read_bytes()!=preexisting[ARCHIVE]:
    raise RuntimeError('Conflicting backup for original TopFlopArchive.java')
   if not backup.exists():
    backup.parent.mkdir(parents=True,exist_ok=True)
    with backup.open('xb') as out:out.write(preexisting[ARCHIVE])
  bible=root/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v23.md'
  bible_existed=bible.exists()
  try:
   for rel in PATCH:
    src=KIT/rel;dest=root/rel
    if dest.is_file() and sha(src)==sha(dest):continue
    dest.parent.mkdir(parents=True,exist_ok=True)
    dest.write_bytes(src.read_bytes())
    changed.append(rel)
   run([sys.executable,root/'tools/Create-RecordsNext4WorkingCodeMd-v23.py',
        '--project-dir',root],logs)
  except Exception:
   for rel in reversed(changed):
    dest=root/rel
    if rel in preexisting:dest.write_bytes(preexisting[rel])
    elif rel not in was_there:dest.unlink(missing_ok=True)
   if not bible_existed:bible.unlink(missing_ok=True)
   raise
  state['installed']=list(changed)
  state['checks'].append('Code docs and bible v23 installed, SHA-256 validated')
  state['status']='OK'
  print('ESITO: OK')
  print('STAGING (NON PUBBLICATO):',staging)
  print('JS:',state['historicalCount']['files'],'MAX_BYTES:',state['historicalCount']['maxJsBytes'])
  print('BIBBIA: docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v23.md')
 except Exception as ex:
  state['error']=repr(ex)
  logs.append(traceback.format_exc())
  print('ESITO: ERRORE',file=sys.stderr)
  print(str(ex),file=sys.stderr)
 finally:
  with zipfile.ZipFile(report,'w',zipfile.ZIP_DEFLATED) as out:
   out.writestr('result.json',json.dumps(state,indent=2,ensure_ascii=False))
   out.writestr('execution.txt','\n'.join(logs))
   if (build/'validation.json').is_file():
    out.write(build/'validation.json','validation.json')
   if (staging/'fcmRecordsNext_TopFlop.js').is_file():
    out.write(staging/'fcmRecordsNext_TopFlop.js','fcmRecordsNext_TopFlop.js')
  shutil.rmtree(build,ignore_errors=True)
  print('ZIP DA CARICARE:',report)
 return 0 if state['status']=='OK' else 1

if __name__=='__main__':sys.exit(main())
