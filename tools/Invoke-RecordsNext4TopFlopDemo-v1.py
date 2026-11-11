#!/usr/bin/env python3
"""Safely install preview assets only, never execute site publishing or database writes."""
from __future__ import annotations
import argparse
import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys
import traceback
from datetime import datetime
from zipfile import ZipFile,ZIP_DEFLATED

REQUIRED=[
 'pom.xml',
 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v18.md',
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopJdbcArchiveAssembler.java',
 'src/main/java/it/alterlega/recordsnext/app/output/SeasonFamilyShardPublisher.java',
]
SAFETY=[
 'src/main/java/it/alterlega/recordsnext/app/RecordsNextPipeline.java',
 'src/main/java/it/alterlega/recordsnext/Records2026SitePublisher.java',
 'src/main/java/it/alterlega/recordsnext/app/output/SeasonFamilyShardPublisher.java',
 'src/main/java/it/alterlega/recordsnext/app/model/CoreRecordCatalog.java',
 'src/main/java/it/alterlega/recordsnext/app/model/RecordFamily.java',
 'pom.xml',
]
FILES=[
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v20.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v19.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v19.md',
 'docs/SPECIFICA_DEMO_TOP_FLOP_RECORDSNEXT4_v1.md',
 'release/visualizzatori/RecordsNext/top_flop_demo.html',
 'release/visualizzatori/RecordsNext/top_flop_demo.css',
 'release/visualizzatori/js/fcmRecordsNextFunzioni_topflop_v1.js',
 'release/visualizzatori/js/fcmRecordsNext_TopFlop.js',
 'release/visualizzatori/js/fcmRecordsNext_TopFlop.2006_2007.js',
 'release/visualizzatori/js/fcmRecordsNext_TopFlop.2026_2027.js',
 'release/visualizzatori/js/TOPFLOP_PREVIEW_MANIFEST_v1.json',
 'release/visualizzatori/profiles/mauzstrom/recordsnext.css',
 'release/visualizzatori/profiles/maelstrom/recordsnext.css',
 'release/visualizzatori/profiles/fantablue2/recordsnext.css',
 'release/visualizzatori/profiles/neutral/recordsnext.css',
 'tools/Build-RecordsNext4TopFlopDemo-v1.py',
 'tools/Test-RecordsNext4TopFlopFacade-v1.js',
 'tools/Create-RecordsNext4WorkingCodeMd-v19.py',
 'tools/Invoke-RecordsNext4TopFlopDemo-v1.py',
]

def hashfile(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def ensure(cond,message):
 if not cond:raise RuntimeError(message)
def main():
 ap=argparse.ArgumentParser();ap.add_argument('--project-dir',type=Path,default=Path(r'D:\DEV_APPS\RecordsNext4.0'));ap.add_argument('--downloads-dir',type=Path,default=Path(r'D:\DEV_APPS\downloads'));a=ap.parse_args()
 project=a.project_dir.resolve();down=a.downloads_dir.resolve();kit=Path(__file__).resolve().parent.parent
 down.mkdir(parents=True,exist_ok=True)
 stamp=datetime.now().strftime('%Y%m%d_%H%M%S_%f');outfile=down/('RecordsNext4_TopFlopDemo_OUTPUT_v1_'+stamp+'.zip')
 log=[];state='ERRORE';changes=[];safeBefore={}
 try:
  ensure(project.is_dir(),'Worktree RecordsNext4.0 non trovato: '+str(project))
  for f in REQUIRED:ensure((project/f).is_file(),'Prerequisito assente: '+f)
  branch=subprocess.check_output(['git','-C',str(project),'branch','--show-current'],text=True,stderr=subprocess.STDOUT).strip()
  ensure(branch=='feature/recordsnext4-players','Branch inatteso: '+branch)
  for f in SAFETY:ensure((project/f).is_file(),'Sorgente 3.2 assente: '+f)
  safeBefore={f:hashfile(project/f) for f in SAFETY}
  manifest=json.loads((kit/'release/visualizzatori/js/TOPFLOP_PREVIEW_MANIFEST_v1.json').read_text(encoding='utf-8'))
  ensure(manifest['previewOnly'] is True,'Manifest non preview')
  for item in manifest['seasons']:
   f='release/visualizzatori/js/fcmRecordsNext_TopFlop.'+item['seasonId']+'.js'
   original=kit/f
   ensure(original.is_file() and original.stat().st_size<=1536*1024,'Shard non valido: '+f)
   ensure(hashfile(original)==item['sha256'],'Hash shard differente: '+f)
  # Preflight all paths before writing a single byte. Any mismatch aborts.
  for name in FILES:
   origin=kit/name;dest=project/name
   ensure(origin.is_file(),'File del kit mancante: '+name)
   if dest.exists():ensure(dest.is_file() and hashfile(origin)==hashfile(dest),'File gia presente diverso, nessuna sovrascrittura: '+name)
  for name in FILES:
   source=kit/name;dest=project/name
   if dest.exists():log.append('GIA PRESENTE IDENTICO: '+name);continue
   dest.parent.mkdir(parents=True,exist_ok=True)
   with source.open('rb') as input, dest.open('xb') as output:shutil.copyfileobj(input,output)
   ensure(hashfile(source)==hashfile(dest),'Errore di copia: '+name)
   changes.append(name);log.append('INSTALLATO: '+name)
  bible=[sys.executable,str(project/'tools/Create-RecordsNext4WorkingCodeMd-v19.py'),'--project-dir',str(project)]
  b=subprocess.run(bible,capture_output=True,text=True,encoding='utf-8',errors='replace')
  ensure(b.returncode==0,'Generatore bibbia v19 fallito: '+b.stdout+' '+b.stderr)
  log.append(b.stdout.strip())
  for name,value in safeBefore.items():ensure(hashfile(project/name)==value,'Sorgente 3.2 MODIFICATO: '+name)
  ensure((project/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v19.md').is_file(),'Bibbia v19 non generata')
  state='OK'
 except Exception as e:
  log.append('ERRORE: '+repr(e))
  log.append(traceback.format_exc())
 finally:
  report={'esito':state,'project':str(project),'installed':changes,'log':log,'unchangedOriginalFiles':safeBefore,
          'demoPath':str(project/'release/visualizzatori/RecordsNext/top_flop_demo.html')}
  with ZipFile(outfile,'w',ZIP_DEFLATED,compresslevel=7) as z:
   z.writestr('RISULTATO_v1.json',json.dumps(report,ensure_ascii=False,indent=2))
   z.writestr('LOG_v1.txt','\n'.join(log)+'\n')
   if state=='OK':
    doc=project/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v19.md'
    z.writestr('BIBBIA_v19_SHA256.txt',hashfile(doc)+'  '+doc.name+'\n')
    z.write(project/'release/visualizzatori/js/TOPFLOP_PREVIEW_MANIFEST_v1.json',arcname='TOPFLOP_PREVIEW_MANIFEST_v1.json')
  print('ESITO: '+state,flush=True)
  print('ZIP DA CARICARE: '+str(outfile),flush=True)
  if state=='OK':print('APRI DEMO: '+str(project/'release/visualizzatori/RecordsNext/top_flop_demo.html'),flush=True)
  else:print('\n'.join(log[-2:]),file=sys.stderr,flush=True)
 if state!='OK':sys.exit(1)
if __name__=='__main__':main()
