#!/usr/bin/env python3
"""RecordsNext4 opt-in NativeGUI/Pipeline installer; never publishes live sites.

Baseline SHA check -> sandbox compilation/tests -> backup -> install -> bible -> report.
Defaults are explicit Windows working copy and downloads; no editing 3.2.
"""
from __future__ import annotations
import argparse,datetime,hashlib,json,os,re,shutil,subprocess,sys,traceback,zipfile
from pathlib import Path
KIT=Path(__file__).resolve().parent.parent
SOURCE_FILES=[
 'src/main/java/it/alterlega/recordsnext/app/model/RecordFamily.java',
 'src/main/java/it/alterlega/recordsnext/app/model/CoreRecordCatalog.java',
 'src/main/java/it/alterlega/recordsnext/app/config/ProcessingConfigWriter.java',
 'src/main/java/it/alterlega/recordsnext/app/config/ProcessingConfigLoader.java',
 'src/main/java/it/alterlega/recordsnext/app/config/MiniJson.java',
 'src/main/java/it/alterlega/recordsnext/gui/RecordsNext2Dashboard.java',
 'src/main/java/it/alterlega/recordsnext/app/RecordsNextPipeline.java',
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopPublicViewsStageExporter.java',
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopViewsPublication.java',
 'src/test/java/it/alterlega/recordsnext/app/model/CoreRecordCatalogCompletenessTest.java',
 'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopNativeIntegrationSmokeTest.java',
 'src/test/java/it/alterlega/recordsnext/app/TopFlopNativePublisherSmokeTest.java',
 'src/test/java/it/alterlega/recordsnext/app/TopFlopGuiPanelSmokeTest.java',
]
SUPPORT_FILES=[
 'tools/Invoke-RecordsNext4NativeGuiPipeline-v2.py',
 'tools/Create-RecordsNext4WorkingCodeMd-v26.py',
 'docs/SPECIFICA_INTEGRAZIONE_NATIVA_TOP_FLOP_v1.md',
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v28.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v27.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v27.md',
 'docs/SPECIFICA_METRICHE_TOPFLOP_v1.md',
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v29.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v28.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v28.md',
]
INSTALL=SOURCE_FILES+SUPPORT_FILES
SMOKES=[
 'it.alterlega.recordsnext.app.topflop.TopFlopArchiveSmokeTest',
 'it.alterlega.recordsnext.app.topflop.TopFlopPublicViewsStageSmokeTest',
 'it.alterlega.recordsnext.app.topflop.TopFlopNativeIntegrationSmokeTest',
 'it.alterlega.recordsnext.app.TopFlopNativePublisherSmokeTest',
 'it.alterlega.recordsnext.app.TopFlopGuiPanelSmokeTest',
]

def sha(p:Path):
 h=hashlib.sha256()
 with p.open('rb') as f:
  for b in iter(lambda:f.read(1024*1024),b''):h.update(b)
 return h.hexdigest()

def command(argv, log, *, cwd=None, timeout=240):
 argv=list(map(str,argv))
 p=subprocess.run(argv,cwd=cwd,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,
                  text=True,encoding='utf-8',errors='replace',timeout=timeout)
 line='COMMAND '+Path(argv[0]).name+' => '+str(p.returncode)
 log.append(line+'\n'+(p.stdout or '')[-14000:])
 if p.returncode:raise RuntimeError(line+'\n'+(p.stdout or '')[-8500:])
 return p.stdout or ''


def javac_arg_path(p):
  """Use forward slashes in javac @argfile: raw Windows backslashes are escapes."""
  value=str(p).replace('\\','/')
  if any(c in value for c in ('"','\n','\r')):
   raise RuntimeError('Percorso non sicuro per javac @argfile: '+repr(value))
  return '"'+value+'"'

def write_javac_argfile(dest,paths):
  dest.write_text(''.join(javac_arg_path(p)+'\n' for p in paths),encoding='utf-8')

def putzip(z:zipfile.ZipFile,name,obj):
 z.writestr(name,json.dumps(obj,ensure_ascii=False,indent=2) if not isinstance(obj,str) else obj)

def main():
 parser=argparse.ArgumentParser()
 parser.add_argument('--project-dir',default=r'D:\DEV_APPS\RecordsNext4.0')
 parser.add_argument('--downloads-dir',default=r'D:\DEV_APPS\downloads')
 parser.add_argument('--database')
 parser.add_argument('--reports-dir')
 parser.add_argument('--jar')
 args=parser.parse_args()
 root=Path(args.project_dir).resolve();downloads=Path(args.downloads_dir).resolve()
 downloads.mkdir(parents=True,exist_ok=True)
 stamp=datetime.datetime.now().strftime('%Y%m%d_%H%M%S_%f')
 output=downloads/('RecordsNext4_NativeGuiPipeline_OUTPUT_v2_'+stamp+'.zip')
 work=downloads/('RecordsNext4_NativeGuiPipeline_TEST_v2_'+stamp)
 backup=downloads/('RecordsNext4_NativeGuiPipeline_BACKUP_v2_'+stamp+'.zip')
 status={'status':'ERROR','version':'native-gui-pipeline-v2','installed':[],
         'alreadyIdentical':[],'project':str(root),'sitesTouched':False,
         'sqliteTouched':False,'published':False,'checks':[]}
 log=[];changed=[];created=[];backupBytes={}
 try:
  if not (root/'pom.xml').is_file():raise RuntimeError('Worktree 4.0 inesistente: '+str(root))
  if not (root/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v25.md').is_file():
   raise RuntimeError('Manca bibbia v25 della precedente installazione verificata')
  if (root/'.git').exists() and shutil.which('git'):
   branch=command(['git','-C',root,'branch','--show-current'],log).strip()
   if branch!='feature/recordsnext4-players':raise RuntimeError('Branch non autorizzato: '+branch)
  baseline=json.loads((KIT/'baseline-sha256.json').read_text('utf-8'))
  manifest=json.loads((KIT/'manifest-sha256.json').read_text('utf-8'))
  if set(manifest['files'])!=set(INSTALL):raise RuntimeError('Manifest interno kit non coincide')
  for rel in INSTALL:
   src=KIT/rel;dst=root/rel
   if not src.is_file() or sha(src)!=manifest['files'][rel]:
    raise RuntimeError('Kit alterato: '+rel)
   if dst.exists():
    if not dst.is_file():raise RuntimeError('Destinazione non e un file: '+rel)
    cur=sha(dst)
    if cur==sha(src):status['alreadyIdentical'].append(rel)
    elif cur!=baseline.get(rel):raise RuntimeError('File di lavoro diverso dalla baseline; stop senza sovrascrivere: '+rel)
  java=shutil.which('java');javac=shutil.which('javac')
  if not java or not javac:raise RuntimeError('JDK 21 non trovato')
  v=command([javac,'-version'],log)
  if not (m:=re.search(r'javac\s+(\d+)',v)) or int(m.group(1))<21:
   raise RuntimeError('Serve javac 21+: '+v)
  db=(Path(args.database).resolve() if args.database else
      next(iter(sorted((root/'data/database').glob('recordsnext-2026_2027-verifica-v1_*.db'))),None))
  if db is None or not db.is_file():raise RuntimeError('SQLite aggiornato mancante; usare --database')
  reports=(Path(args.reports_dir).resolve() if args.reports_dir else
       root.parent/'RecordsNext2.0'/'data'/'reports')
  if not reports.is_dir():raise RuntimeError('Report canonici 3.2 mancanti: '+str(reports))
  jar=(Path(args.jar).resolve() if args.jar else
       next((p for p in (root/'target/RecordsNext.jar',
         root.parent/'RecordsNext2.0'/'target/RecordsNext.jar',
         root.parent/'RecordsNext2.0'/'release/RecordsNext_3.2.0_FULL/payload/RecordsNext.jar')
         if p.is_file()),None))
  if jar is None or not jar.is_file():raise RuntimeError('JAR runtime SQLite 3.2 non trovato')
  status.update(database=str(db),databaseSha256Before=sha(db),reports=str(reports),
                sourceBaselineVerified=True)
  # All test work is performed outside the user's project.
  src=work/'src/main/java';shutil.copytree(root/'src/main/java',src)
  for rel in SOURCE_FILES:
   if not rel.startswith('src/main/java/'):continue
   target=work/rel;target.parent.mkdir(parents=True,exist_ok=True)
   shutil.copy2(KIT/rel,target)
  cls=work/'classes';cls.mkdir(parents=True)
  sources=sorted(src.rglob('*.java'))
  argfile=work/'sources.txt';write_javac_argfile(argfile, sources)
  command([javac,'-encoding','UTF-8','-cp',jar,'-d',cls,'@'+str(argfile).replace('\\','/')],log,timeout=240)
  status['checks'].append('Compilazione sorgenti Java 21 FULL: OK')
  tests=[root/'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopArchiveSmokeTest.java',
         root/'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopPublicViewsStageSmokeTest.java']
  tests += [KIT/f for f in SOURCE_FILES if f.endswith('SmokeTest.java')]
  testfile=work/'tests.txt'
  write_javac_argfile(testfile, tests)
  cp=os.pathsep.join([str(cls),str(jar)])
  command([javac,'-encoding','UTF-8','-cp',cp,'-d',cls,'@'+str(testfile).replace('\\','/')],log,timeout=120)
  for name in SMOKES:
   testScratch=work/name.split('.')[-1]
   testScratch.mkdir(parents=True,exist_ok=True)
   argv=[java,'-cp',cp,name]
   if name.endswith('TopFlopNativeIntegrationSmokeTest') or name.endswith('TopFlopNativePublisherSmokeTest'):
    argv += [db,reports,testScratch]
   if name.endswith('TopFlopGuiPanelSmokeTest'):
    # Headless Linux CI: xvfb; ordinary Windows desktop runs Swing directly.
    if sys.platform.startswith('linux'):
     xvfb=shutil.which('xvfb-run')
     if xvfb:argv=[xvfb,'-a',*argv]
     else:raise RuntimeError('Test GUI impossibile: Xvfb assente')
   result=command(argv,log,cwd=testScratch,timeout=180)
   if '_OK' not in result:raise RuntimeError('Test non ha confermato OK: '+name)
   status['checks'].append(name.split('.')[-1]+': OK')
  if sha(db)!=status['databaseSha256Before']:
   raise RuntimeError('Hash database modificato dai test: installazione bloccata')
  status['checks'].append('Database invariato SHA256: OK')
  # Backup of precisely the files which will be replaced.
  for rel in INSTALL:
   src=KIT/rel;dst=root/rel
   if dst.exists() and sha(dst)!=sha(src):backupBytes[rel]=dst.read_bytes()
  if backupBytes:
   with zipfile.ZipFile(backup,'x',zipfile.ZIP_DEFLATED,compresslevel=8) as z:
    for rel,content in backupBytes.items():z.writestr(rel,content)
  for rel in INSTALL:
   src=KIT/rel;dst=root/rel
   if dst.exists() and sha(dst)==sha(src):continue
   dst.parent.mkdir(parents=True,exist_ok=True)
   temp=dst.with_name('.'+dst.name+'.rn4-native-v2-tmp')
   try:
    temp.write_bytes(src.read_bytes())
    if sha(temp)!=sha(src):raise RuntimeError('Verifica post-copia fallita: '+rel)
    os.replace(temp,dst)
   finally:
    if temp.exists():temp.unlink()
   (changed if rel in backupBytes else created).append(rel)
   status['installed'].append(rel)
  gen=root/'tools/Create-RecordsNext4WorkingCodeMd-v26.py'
  command([sys.executable,gen,'--project-dir',root],log,timeout=120)
  bible=root/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v26.md'
  if bible.is_file():
   status['checks'].append('Bibbia v26: '+sha(bible))
  if sha(db)!=status['databaseSha256Before']:
   raise RuntimeError('Hash database post-installazione modificato')
  status.update(status='OK',backup=str(backup) if backupBytes else None,
                databaseSha256After=sha(db),bible=str(bible))
 except Exception as ex:
  log.append(traceback.format_exc())
  status['error']=str(ex)
  # Restore precisely altered original sources and remove only newly-created files.
  for rel in reversed(changed):
   try:(root/rel).write_bytes(backupBytes[rel])
   except Exception as restoreErr:log.append('ROLLBACK ERROR '+rel+': '+str(restoreErr))
  for rel in reversed(created):
   try:(root/rel).unlink(missing_ok=True)
   except Exception as restoreErr:log.append('ROLLBACK ERROR '+rel+': '+str(restoreErr))
  status['status']='ERROR'
 finally:
  try:
   with zipfile.ZipFile(output,'w',zipfile.ZIP_DEFLATED,compresslevel=8) as z:
    putzip(z,'result.json',status)
    putzip(z,'execution.txt','\n\n'.join(log))
  finally:
   if work.exists():shutil.rmtree(work,ignore_errors=True)
  print('ESITO:',status['status'])
  if status['status']=='OK':
   print('GUI TOP & FLOP: 20 record; 13 attivi, 7 non verificati bloccati')
   print('PUBLISHER: 3.2 originale, cutoff/manifest/rollback testati in cartella temporanea')
   print('BIBBIA:',status.get('bible'))
   print('SORGENTI INSTALLATI:',len(status['installed']),'GIA IDENTICI:',len(status['alreadyIdentical']))
  else:print('ERRORE:',status.get('error'))
  print('ZIP DA CARICARE:',output)
 return 0 if status['status']=='OK' else 1

if __name__=='__main__':sys.exit(main())
