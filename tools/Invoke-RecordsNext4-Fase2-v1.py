#!/usr/bin/env python3
"""Runner Fase 2 v1 per Windows: installazione conservativa e ZIP output.

Non tocca DB 3.2, FCM/FCA originali, codice Java, JS sito o branch Git.
"""
from __future__ import annotations
import argparse
from datetime import datetime
import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys
import traceback
from zipfile import ZipFile,ZIP_DEFLATED

BASE='f9bf30323d9204d70e91e242bddfc72c2060ecf9'
FILES=[
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v7.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v6.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v6.md',
 'docs/SPECIFICA_ESTRAZIONE_FANTASY_RECORDSNEXT4_v1.md',
 'docs/VERIFICA_ESTRAZIONE_FANTASY_RECORDSNEXT4_v1.md',
 'tools/Estrai-RecordsNext4Fantasy-v1.py',
 'tools/Test-RecordsNext4Fantasy-v1.py',
 'tools/Create-RecordsNext4WorkingCodeMd-v6.py',
 'tools/Invoke-RecordsNext4-Fase2-v1.py',
]
REQUIRED=[
 'pom.xml','docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md',
 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v5.md',
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v6.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v5.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v5.md',
 'tools/Estrai-RecordsNext4Reali-v1.py',
 'tools/Test-RecordsNext4Reali-v1.py',
]


def sha(p:Path)->str:
    h=hashlib.sha256()
    with p.open('rb') as f:
        for chunk in iter(lambda:f.read(1024*1024),b''):h.update(chunk)
    return h.hexdigest()


def git(project:Path,*args:str)->str:
    p=subprocess.run(['git','-C',str(project),*args],capture_output=True,text=True,encoding='utf8',errors='replace')
    if p.returncode:raise RuntimeError('git '+ ' '.join(args)+': '+p.stderr.strip())
    return p.stdout.strip()


def validate_project(project:Path)->dict:
    for n in REQUIRED:
        if not (project/n).is_file(): raise RuntimeError('Worktree non pronto, manca: '+n)
    root=Path(git(project,'rev-parse','--show-toplevel')).resolve()
    if os.path.normcase(str(root))!=os.path.normcase(str(project.resolve())):
        raise RuntimeError('La cartella non e la radice del worktree')
    branch=git(project,'branch','--show-current')
    if branch!='feature/recordsnext4-players':raise RuntimeError('Branch inatteso: '+branch)
    head=git(project,'rev-parse','HEAD')
    git(project,'merge-base','--is-ancestor',BASE,head)
    return {'branch':branch,'head':head,'statusBefore':git(project,'status','-sb')}


def install(root:Path,project:Path)->list[str]:
    # Controlla intero kit prima di scrivere un singolo file.
    for n in FILES:
        src=root/n; dst=project/n
        if not src.is_file():raise RuntimeError('Manca file nel kit: '+n)
        if dst.exists() and (not dst.is_file() or sha(src)!=sha(dst)):
            raise RuntimeError('File gia presente e diverso; nessuna sovrascrittura: '+n)
    added=[]
    for n in FILES:
        src=root/n;dst=project/n
        if dst.is_file():
            print('GIA PRESENTE IDENTICO:',n,flush=True)
            continue
        dst.parent.mkdir(parents=True,exist_ok=True)
        with dst.open('xb') as f:f.write(src.read_bytes())
        added.append(n)
        print('INSTALLATO:',n,flush=True)
    return added


def run(source:Path,args:list[str],log:Path)->None:
    env=dict(os.environ, PYTHONDONTWRITEBYTECODE='1')
    p=subprocess.run([sys.executable,str(source),*args],
        stdout=subprocess.PIPE,stderr=subprocess.STDOUT,encoding='utf8',errors='replace',text=True,env=env)
    log.write_text(p.stdout,encoding='utf8')
    print(source.name,'codice uscita',p.returncode,'\n',p.stdout[-550:],flush=True)
    if p.returncode:raise RuntimeError('Fallito '+source.name+'; vedere '+log.name)


def main() -> int:
    parser=argparse.ArgumentParser()
    parser.add_argument('--work-dir',type=Path,default=Path('D:/DEV_APPS/RecordsNext4.0'))
    parser.add_argument('--database',type=Path,default=Path('D:/DEV_APPS/RecordsNext2.0/data/database/recordsnext-sandbox.db'))
    parser.add_argument('--output-dir',type=Path,default=Path('D:/DEV_APPS/downloads'))
    a=parser.parse_args()
    project=a.work_dir.resolve();db=a.database.resolve();out=a.output_dir.resolve()
    if not out.is_dir():
        raise RuntimeError('Cartella output assente: '+str(out))
    stamp=datetime.now().strftime('%Y%m%d_%H%M%S_%f')
    folder=out/('RecordsNext4_Fase2_OUTPUT_v1_'+stamp)
    folder.mkdir()
    zipfile=out/(folder.name+'.zip')
    kit=Path(__file__).resolve().parent.parent
    status='ERRORE'
    manifest={'fase':2,'versioneKit':'v1','stato':status,'database':str(db),
              'worktree':str(project),'nessunDeploy':True,'nessunCommit':True}
    try:
        if not db.is_file():raise RuntimeError('DB sandbox assente: '+str(db))
        manifest.update(validate_project(project))
        before=sha(db);manifest['dbShaPrima']=before
        manifest['fileInstallati']=install(kit,project)
        run(project/'tools/Test-RecordsNext4Fantasy-v1.py',
            ['--database',str(db)],folder/'LOG_TEST_FANTASY_v1.txt')
        pilot=folder/'dati_pilota';pilot.mkdir()
        for code in (100843,12302):
            run(project/'tools/Estrai-RecordsNext4Fantasy-v1.py',
                ['--database',str(db),'--output-dir',str(pilot),'--code',str(code)],
                folder/('LOG_CODICE_'+str(code)+'_v1.txt'))
        for year in ('2006_2007','2025_2026'):
            run(project/'tools/Estrai-RecordsNext4Fantasy-v1.py',
                ['--database',str(db),'--output-dir',str(pilot),'--all','--season',year],
                folder/('LOG_ANNO_'+year+'_v1.txt'))
        run(project/'tools/Create-RecordsNext4WorkingCodeMd-v6.py',
            ['--project-dir',str(project)],folder/'LOG_BIBBIA_v6.txt')
        docs=folder/'documentazione';docs.mkdir()
        for n in (f.split('/',1)[1] for f in FILES if f.startswith('docs/')):
            shutil.copy2(project/'docs'/n,docs/n)
        shutil.copy2(project/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v6.md',
                     docs/'CODICE_FUNZIONANTE_RECORDSNEXT4_v6.md')
        after=sha(db);manifest['dbShaDopo']=after
        if before!=after:raise RuntimeError('SHA database cambiato durante audit!')
        manifest['statusAfter']=git(project,'status','-sb')
        manifest['stato']='FASE2_AUDIT_ESEGUITI'
        status='OK'
    except Exception:
        err=traceback.format_exc()
        (folder/'ERRORE_FASE2_v1.txt').write_text(err,encoding='utf8')
        print('ERRORE FASE2:',err[-2100:],file=sys.stderr,flush=True)
    finally:
        (folder/'MANIFEST_FASE2_v1.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
        with ZipFile(zipfile,'x',ZIP_DEFLATED,compresslevel=6) as z:
            for f in sorted(folder.rglob('*')):
                if f.is_file():z.write(f,f.relative_to(folder).as_posix())
        print('ESITO:',status,'\nZIP DA CARICARE:',zipfile,flush=True)
    return 0 if status=='OK' else 1

if __name__=='__main__':
    sys.exit(main())
