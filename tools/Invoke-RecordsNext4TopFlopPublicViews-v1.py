#!/usr/bin/env python3
"""Conservative Windows installer for native TopFlop PUBLIC VIEWS stage v1.
No site publishing; no modification of sqlite, CSS, GUI, publisher or 3.2.
"""
from __future__ import annotations
import argparse,datetime,hashlib,json,os,re,shutil,sqlite3,subprocess,sys,traceback,zipfile
from pathlib import Path

KIT=Path(__file__).resolve().parent.parent
JAVA='it.alterlega.recordsnext.app.topflop.TopFlopPublicViewsStageExporter'
TEST='it.alterlega.recordsnext.app.topflop.TopFlopPublicViewsStageSmokeTest'
INSTALL=[
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopPublicViewsStageExporter.java',
 'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopPublicViewsStageSmokeTest.java',
 'visualizzatori/js/fcmRecordsNextTopFlopViewsDecoder-v1.js',
 'tools/Validate-RecordsNext4TopFlopPublicViews-v1.py',
 'tools/Test-RecordsNext4TopFlopPublicViewsBrowser-v1.js',
 'tools/Invoke-RecordsNext4TopFlopPublicViews-v1.py',
 'tools/Create-RecordsNext4WorkingCodeMd-v25.py',
 'docs/SPECIFICA_VISTE_PUBBLICHE_TOP_FLOP_v1.md',
 'docs/RecordsNext4_CATALOGO_RECORD_E_VISTE_TOPFLOP_v1.md',
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v26.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v25.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v25.md',
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v27.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v26.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v26.md',
]
def sha(p:Path)->str:
 h=hashlib.sha256()
 with p.open('rb') as st:
  for block in iter(lambda:st.read(1048576),b''):h.update(block)
 return h.hexdigest()

def invoke(command,log):
 cmd=list(map(str,command))
 p=subprocess.run(cmd,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,
     text=True,encoding='utf-8',errors='replace')
 log.append('COMMAND: '+Path(cmd[0]).name+' (arguments abbreviated)')
 log.append((p.stdout or '')[-20000:])
 if p.returncode:raise RuntimeError('Command failed: '+str(p.returncode)+'\n'+(p.stdout or '')[-9000:])
 return p.stdout or ''

def reports_hash(folder:Path):
 reports=sorted(folder.rglob('season_normalized_*.json'))
 if not reports:raise RuntimeError('No canonical 3.2 reports in '+str(folder))
 h=hashlib.sha256()
 for report in reports:
  h.update(report.relative_to(folder).as_posix().encode('utf-8'))
  h.update(bytes.fromhex(sha(report)))
 return h.hexdigest()

def main()->int:
 cli=argparse.ArgumentParser()
 cli.add_argument('--project-dir',default=r'D:\DEV_APPS\RecordsNext4.0')
 cli.add_argument('--downloads-dir',default=r'D:\DEV_APPS\downloads')
 cli.add_argument('--database');cli.add_argument('--reports-dir');cli.add_argument('--jar')
 cli.add_argument('--seasons',help='Comma-separated pilot season list, otherwise all 21')
 args=cli.parse_args()
 root=Path(args.project_dir).resolve();downloads=Path(args.downloads_dir).resolve()
 downloads.mkdir(parents=True,exist_ok=True)
 stamp=datetime.datetime.now().strftime('%Y%m%d_%H%M%S_%f')
 report=downloads/('RecordsNext4_TopFlopVistePubbliche_OUTPUT_v1_'+stamp+'.zip')
 build=downloads/('RecordsNext4_TopFlopVistePubbliche_BUILD_v1_'+stamp)
 state={'status':'ERROR','version':'v1-public-views','checks':[],
     'installed':[],'alreadyIdentical':[],'project':str(root)}
 logs=[];installed=[]
 try:
  if not (root/'pom.xml').is_file():raise RuntimeError('Missing 4.0 project: '+str(root))
  if not (root/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v24.md').is_file():
   raise RuntimeError('Bibbia v24 missing: run verified Compact kit v2 first')
  if (root/'.git').exists() and shutil.which('git'):
   branch=invoke(['git','-C',root,'branch','--show-current'],logs).strip()
   if branch!='feature/recordsnext4-players':raise RuntimeError('Wrong Git branch: '+branch)
  manifest=json.loads((KIT/'manifest-sha256.json').read_text(encoding='utf-8'))
  if set(manifest['files'])!=set(INSTALL):raise RuntimeError('Kit manifest mismatch')
  baseline=json.loads((KIT/'baseline-hashes.json').read_text(encoding='utf-8'))
  for rel,expected in baseline.items():
   if not (root/rel).is_file() or sha(root/rel)!=expected:
    raise RuntimeError('Existing 3.2/4.0 source changed; refusing unreviewed patch: '+rel)
  for rel in INSTALL:
   src=KIT/rel;dst=root/rel
   if not src.is_file() or sha(src)!=manifest['files'][rel]:
    raise RuntimeError('Invalid kit file SHA-256: '+rel)
   if dst.exists():
    if not dst.is_file() or sha(dst)!=sha(src):
     raise RuntimeError('Existing file differs: no overwrite: '+rel)
    state['alreadyIdentical'].append(rel)
  javac=shutil.which('javac');java=shutil.which('java')
  if not javac or not java:raise RuntimeError('Java JDK 21 missing')
  version=invoke([javac,'-version'],logs)
  m=re.search(r'javac\s+(\d+)',version)
  if not m or int(m.group(1))<21:raise RuntimeError('JDK >=21 required: '+version)
  db=(Path(args.database).resolve() if args.database else
      next(iter(sorted((root/'data/database').glob('recordsnext-2026_2027-verifica-v1_*.db'))),None))
  if db is None or not db.is_file():raise RuntimeError('Updated 2026/27 SQLite missing; use --database')
  jar=(Path(args.jar).resolve() if args.jar else next((x for x in [
       root/'target/RecordsNext.jar',
       root.parent/'RecordsNext2.0/target/RecordsNext.jar',
       root.parent/'RecordsNext2.0/release/RecordsNext_3.2.0_FULL/payload/RecordsNext.jar']
       if x.is_file()),None))
  if jar is None:raise RuntimeError('3.2 runtime JAR missing; use --jar')
  normalized=(Path(args.reports_dir).resolve() if args.reports_dir else
              (root.parent/'RecordsNext2.0/data/reports').resolve())
  if not normalized.is_dir():raise RuntimeError('Canonical 3.2 reports directory missing: '+str(normalized))
  dbBefore=sha(db);norm=reports_hash(normalized)
  sourceFingerprint=hashlib.sha256((json.dumps(baseline,sort_keys=True)+
      sha(KIT/INSTALL[0])).encode('utf-8')).hexdigest()
  state.update(database=str(db),databaseSha256Before=dbBefore,
    canonicalReportsSha256=norm,sourceFingerprint=sourceFingerprint)
  with sqlite3.connect(db.as_uri()+'?mode=ro',uri=True) as c:
   tables=[x[0] for x in c.execute("SELECT name FROM sqlite_master WHERE type='table'")]
  available=sorted({m.group(1) for x in tables
     if (m:=re.fullmatch(r'raw_(\d{4}_\d{4})_fcm_formazione',x))})
  if not available:raise RuntimeError('No FCM formation seasons in SQLite')
  seasons=available
  pilot=''
  if args.seasons:
   seasons=[x.strip() for x in args.seasons.split(',')]
   if not seasons or len(set(seasons))!=len(seasons) or any(s not in available for s in seasons):
    raise RuntimeError('Invalid pilot seasons')
   pilot='_PILOT_'+'_'.join(seasons)
  state['seasons']=seasons
  rootStage=root/'data/site-export-staging'/('topflop-public-views-v1'+pilot+'_'+
      dbBefore[:10]+'_'+norm[:10]+'_'+sourceFingerprint[:10])
  stage=rootStage/'js';state['stage']=str(stage)
  classes=build/'classes';classes.mkdir(parents=True,exist_ok=True)
  src=INSTALL[0]
  existing=[f for f in (root/'src/main/java').rglob('*.java')
            if f.relative_to(root).as_posix()!=src]
  invoke([javac,'--release','21','-encoding','UTF-8','-d',classes,*existing,KIT/src],logs)
  state['checks'].append('Java 21: 3.2 + TopFlop source compilation')
  baselineTest=root/'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopArchiveSmokeTest.java'
  if not baselineTest.is_file():raise RuntimeError('Missing baseline 52-check test')
  invoke([javac,'--release','21','-encoding','UTF-8','-cp',classes,'-d',classes,
       baselineTest,KIT/INSTALL[1]],logs)
  output=invoke([java,'-ea','-cp',classes,
    'it.alterlega.recordsnext.app.topflop.TopFlopArchiveSmokeTest'],logs)
  if 'checks=52' not in output:raise RuntimeError('Base model 52-check suite incomplete')
  output=invoke([java,'-ea','-cp',classes,TEST],logs)
  if 'TOP_FLOP_PUBLIC_VIEWS_KEEPER_AND_DOUBLE_CONTEXT_TEST_OK' not in output:
   raise RuntimeError('Keeper and double-team smoke test missing')
  state['checks'].append('52 model checks; goalkeeper goal/assist/saves/penalty/duplicate fantasy test')
  reused=stage.is_dir()
  if stage.exists() and not reused:raise RuntimeError('Staging path not directory')
  state['reusedStage']=reused
  partial=rootStage/'js.partial'
  if not reused:
   if partial.exists():
    if not partial.is_dir():raise RuntimeError('Invalid incomplete stage directory')
    invoke([sys.executable,KIT/INSTALL[3],partial,'--database',db],logs)
    state['checks'].append('Recovered complete previous interrupted stage')
   else:
    rootStage.mkdir(parents=True,exist_ok=True)
    result=invoke([java,'-Xmx1800m','-cp',str(classes)+os.pathsep+str(jar),
        JAVA,db,partial,normalized,*seasons],logs)
    if 'TOP_FLOP_VIEWS_STAGE_OK' not in result:
     raise RuntimeError('Java public views export signature not found')
  validationFile=build/'validation.json'
  out=invoke([sys.executable,KIT/INSTALL[3],stage if reused else partial,
      '--database',db,'--json-output',validationFile],logs)
  if 'TOP_FLOP_PUBLIC_VIEWS_VALIDATED' not in out:raise RuntimeError('Mandatory validator failed')
  data=json.loads(validationFile.read_text(encoding='utf-8'))
  if data['seasons']!=len(seasons) or data['maxJsBytes']>950000:
   raise RuntimeError('Wrong number of seasons or oversize JS')
  state['validation']=data
  state['checks'].append('JS SHA, syntax, source totals, ties, source events, Youth exclusion, 1.5MB limit')
  node=shutil.which('node')
  if node:
   output=invoke([node,KIT/INSTALL[4],stage if reused else partial,KIT/INSTALL[2]],logs)
   if 'TOP_FLOP_PUBLIC_VIEWS_BROWSER_OK' not in output:raise RuntimeError('Node decoder browser test missing')
   state['checks'].append('Node JS decoder on full selected seasons')
  else:
   state['checks'].append('Node unavailable: Python mandatory JS validation completed')
  if sha(db)!=dbBefore:raise RuntimeError('Database changed despite read-only mode')
  state['databaseSha256After']=sha(db)
  state['checks'].append('SQLite SHA-256 unchanged; site publisher never invoked')
  if not reused:partial.rename(stage)
  for rel in INSTALL:
   dst=root/rel
   if dst.exists():continue
   dst.parent.mkdir(parents=True,exist_ok=True)
   with dst.open('xb') as stream:stream.write((KIT/rel).read_bytes())
   installed.append(rel)
  bible=root/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v25.md'
  existingBible=bible.is_file()
  try:invoke([sys.executable,root/'tools/Create-RecordsNext4WorkingCodeMd-v25.py',
       '--project-dir',root],logs)
  except Exception:
   for rel in reversed(installed):(root/rel).unlink(missing_ok=True)
   if not existingBible:bible.unlink(missing_ok=True)
   raise
  state['installed']=installed
  state['status']='OK'
  print('ESITO: OK')
  print('JS STAGING (NON PUBBLICATO):',stage)
  print('JS FILES:',data['jsFiles'],'TOTAL BYTES:',data['totalJsBytes'],
    'MAX SINGLE BYTES:',data['maxJsBytes'])
  print('STAGING REUSED:',reused,'BIBBIA: docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v25.md')
 except Exception as ex:
  state['error']=repr(ex);logs.append(traceback.format_exc())
  print('ESITO: ERRORE',file=sys.stderr);print(str(ex),file=sys.stderr)
 finally:
  with zipfile.ZipFile(report,'w',zipfile.ZIP_DEFLATED) as z:
   z.writestr('result.json',json.dumps(state,indent=2,ensure_ascii=False))
   z.writestr('execution.txt','\n'.join(logs))
   if (build/'validation.json').is_file():z.write(build/'validation.json','validation.json')
  shutil.rmtree(build,ignore_errors=True)
  print('ZIP DA CARICARE:',report)
 return 0 if state['status']=='OK' else 1
if __name__=='__main__':sys.exit(main())
