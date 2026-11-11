#!/usr/bin/env python3
"""RecordsNext 4.0 Phase 3A conservative installer and diagnostics archive.

Only creates new versioned files in the existing 4.0 worktree and reports
under downloads. Uses the operational 3.2 SQLite database read-only.
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
from zipfile import ZipFile, ZIP_DEFLATED

BASE='f9bf30323d9204d70e91e242bddfc72c2060ecf9'
FILES=[
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v8.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v7.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v7.md',
 'docs/SPECIFICA_AUDIT_UTILIZZO_RECORDSNEXT4_v1.md',
 'docs/VERIFICA_AUDIT_UTILIZZO_RECORDSNEXT4_v1.md',
 'tools/Audit-RecordsNext4Utilizzo-v1.py',
 'tools/Test-RecordsNext4Utilizzo-v1.py',
 'tools/Create-RecordsNext4WorkingCodeMd-v7.py',
 'tools/Invoke-RecordsNext4-Fase3A-v1.py',
]
REQUIRED=[
 'pom.xml','docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md',
 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v6.md',
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v7.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v6.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v6.md',
 'tools/Estrai-RecordsNext4Fantasy-v1.py',
 'tools/Test-RecordsNext4Fantasy-v1.py',
]

def sha(path:Path)->str:
    h=hashlib.sha256()
    with path.open('rb') as f:
        for chunk in iter(lambda:f.read(1024*1024),b''):h.update(chunk)
    return h.hexdigest()

def git(project:Path,*argv:str)->str:
    p=subprocess.run(['git','-C',str(project),*argv],capture_output=True,
                     text=True,encoding='utf8',errors='replace')
    if p.returncode:raise RuntimeError('git '+ ' '.join(argv)+': '+p.stderr.strip())
    return p.stdout.strip()

def validate_project(project:Path)->dict:
    for f in REQUIRED:
        if not (project/f).is_file():raise RuntimeError('Worktree incompleto: '+f)
    root=Path(git(project,'rev-parse','--show-toplevel')).resolve()
    if os.path.normcase(str(root))!=os.path.normcase(str(project)):
        raise RuntimeError('La directory non e radice del worktree: '+str(root))
    branch=git(project,'branch','--show-current')
    if branch!='feature/recordsnext4-players':raise RuntimeError('Branch inatteso: '+branch)
    git(project,'merge-base','--is-ancestor',BASE,'HEAD')
    return {'branch':branch,'head':git(project,'rev-parse','HEAD'),
            'gitStatusPrima':git(project,'status','-sb')}

def install(source:Path,project:Path)->list[str]:
    # Check everything first: refuse to replace a changed file.
    for f in FILES:
        src,dst=source/f,project/f
        if not src.is_file():raise RuntimeError('File kit mancante: '+f)
        if dst.exists() and (not dst.is_file() or sha(src)!=sha(dst)):
            raise RuntimeError('File esistente differente; nessuna sovrascrittura: '+f)
    result=[]
    for f in FILES:
        src,dst=source/f,project/f
        if dst.is_file():
            print('GIA PRESENTE IDENTICO:',f,flush=True)
            continue
        dst.parent.mkdir(parents=True,exist_ok=True)
        with dst.open('xb') as out:out.write(src.read_bytes())
        result.append(f)
        print('INSTALLATO:',f,flush=True)
    return result

def execute(script:Path,args:list[str],log:Path)->None:
    env=dict(os.environ,PYTHONDONTWRITEBYTECODE='1')
    p=subprocess.run([sys.executable,str(script),*args],capture_output=True,
                     text=True,encoding='utf8',errors='replace',env=env)
    output=p.stdout+p.stderr
    log.write_text(output,encoding='utf8')
    print(script.name, 'codice:',p.returncode,flush=True)
    if p.returncode:raise RuntimeError('Errore in '+script.name+'; log: '+str(log))

def main()->int:
    ap=argparse.ArgumentParser()
    ap.add_argument('--work-dir',type=Path,default=Path('D:/DEV_APPS/RecordsNext4.0'))
    ap.add_argument('--database',type=Path,default=Path('D:/DEV_APPS/RecordsNext2.0/data/database/recordsnext-sandbox.db'))
    ap.add_argument('--output-dir',type=Path,default=Path('D:/DEV_APPS/downloads'))
    args=ap.parse_args()
    work=args.work_dir.resolve()
    db=args.database.resolve()
    out=args.output_dir.resolve()
    if not out.is_dir():raise RuntimeError('Output dir assente: '+str(out))
    stamp=datetime.now().strftime('%Y%m%d_%H%M%S_%f')
    result_dir=out/('RecordsNext4_Fase3A_OUTPUT_v1_'+stamp)
    result_dir.mkdir(exist_ok=False)
    zip_path=out/(result_dir.name+'.zip')
    kit=Path(__file__).resolve().parent.parent
    manifest={'fase':'3A','kitVersion':'v1','worktree':str(work),'database':str(db),
              'nessunCommit':True,'nessunDeploy':True,'stato':'ERRORE'}
    ok=False
    try:
        if not db.is_file():raise RuntimeError('Database sandbox non trovato: '+str(db))
        manifest.update(validate_project(work))
        manifest['dbShaPrima']=sha(db)
        manifest['fileInstallati']=install(kit,work)
        execute(work/'tools/Test-RecordsNext4Utilizzo-v1.py',
                ['--database',str(db)],result_dir/'LOG_TEST_UTILIZZO_v1.txt')
        data=result_dir/'dati_audit'
        data.mkdir()
        execute(work/'tools/Audit-RecordsNext4Utilizzo-v1.py',
                ['--database',str(db),'--output-dir',str(data)],
                result_dir/'LOG_AUDIT_UTILIZZO_v1.txt')
        summary=json.loads((data/'RecordsNext4_Utilizzo_v1_MANIFEST.json').read_text(encoding='utf8'))
        manifest['righeAudit']=summary['totaleFormazioni']
        manifest['stagioniAudit']=summary['stagioniEsaminate']
        execute(work/'tools/Create-RecordsNext4WorkingCodeMd-v7.py',
                ['--project-dir',str(work)],result_dir/'LOG_BIBBIA_v7.txt')
        docs=result_dir/'documentazione'
        docs.mkdir()
        for f in FILES:
            if f.startswith('docs/'):
                shutil.copy2(work/f,docs/Path(f).name)
        shutil.copy2(work/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v7.md',
                     docs/'CODICE_FUNZIONANTE_RECORDSNEXT4_v7.md')
        manifest['dbShaDopo']=sha(db)
        if manifest['dbShaPrima']!=manifest['dbShaDopo']:
            raise RuntimeError('Il database e cambiato durante la fase 3A!')
        manifest['gitStatusDopo']=git(work,'status','-sb')
        manifest['stato']='OK'
        ok=True
    except Exception:
        error=traceback.format_exc()
        (result_dir/'ERRORE_FASE3A_v1.txt').write_text(error,encoding='utf8')
        print('ERRORE FASE 3A:',error[-1400:],file=sys.stderr,flush=True)
    finally:
        (result_dir/'MANIFEST_FASE3A_v1.json').write_text(json.dumps(manifest,indent=2,ensure_ascii=False)+'\n',encoding='utf8')
        with ZipFile(zip_path,'x',compression=ZIP_DEFLATED,compresslevel=6) as z:
            for file in sorted(result_dir.rglob('*')):
                if file.is_file():z.write(file,file.relative_to(result_dir).as_posix())
        print('ESITO:', 'OK' if ok else 'ERRORE',flush=True)
        print('ZIP DA CARICARE:',zip_path,flush=True)
    return 0 if ok else 1

if __name__=='__main__':sys.exit(main())
