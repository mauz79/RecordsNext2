#!/usr/bin/env python3
"""Conservative Phase 3B1 installer, test runner and diagnostic ZIP creator."""
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
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v9.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v8.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v8.md',
 'docs/SPECIFICA_AUDIT_CONTRIBUZIONI_RECORDSNEXT4_v1.md',
 'docs/VERIFICA_AUDIT_CONTRIBUZIONI_RECORDSNEXT4_v1.md',
 'tools/Audit-RecordsNext4Contribuzioni-v1.py',
 'tools/Test-RecordsNext4Contribuzioni-v1.py',
 'tools/Create-RecordsNext4WorkingCodeMd-v8.py',
 'tools/Invoke-RecordsNext4-Fase3B1-v1.py',
]
REQUIRED=[
 'pom.xml','docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md',
 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v7.md',
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v8.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v7.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v7.md',
 'tools/Audit-RecordsNext4Utilizzo-v1.py',
 'tools/Test-RecordsNext4Utilizzo-v1.py',
]


def sha(path: Path) -> str:
    h=hashlib.sha256()
    with path.open('rb') as f:
        for block in iter(lambda:f.read(1024*1024),b''):
            h.update(block)
    return h.hexdigest()


def git(project: Path,*args: str) -> str:
    p=subprocess.run(['git','-C',str(project),*args],capture_output=True,text=True,encoding='utf8',errors='replace')
    if p.returncode:
        raise RuntimeError('Git '+ ' '.join(args)+' non riuscito: '+p.stderr.strip())
    return p.stdout.strip()


def validate(project:Path)->dict:
    for rel in REQUIRED:
        if not (project/rel).is_file():raise RuntimeError('File mancante nel worktree: '+rel)
    root=Path(git(project,'rev-parse','--show-toplevel')).resolve()
    if os.path.normcase(str(root)) != os.path.normcase(str(project)):
        raise RuntimeError('Directory non radice del worktree: '+str(root))
    branch=git(project,'branch','--show-current')
    if branch!='feature/recordsnext4-players':
        raise RuntimeError('Branch errato: '+branch)
    git(project,'merge-base','--is-ancestor',BASE,'HEAD')
    return {'branch':branch,'head':git(project,'rev-parse','HEAD'),
            'gitStatusPrima':git(project,'status','-sb')}


def install(kit:Path,work:Path)->list[str]:
    for rel in FILES:
        src,dst=kit/rel,work/rel
        if not src.is_file():raise RuntimeError('File non incluso nello ZIP: '+rel)
        if dst.exists() and (not dst.is_file() or sha(src)!=sha(dst)):
            raise RuntimeError('File esistente differente; non sovrascrivo: '+rel)
    installed=[]
    for rel in FILES:
        src,dst=kit/rel,work/rel
        if dst.is_file():
            print('GIA PRESENTE IDENTICO:',rel,flush=True)
            continue
        dst.parent.mkdir(parents=True,exist_ok=True)
        with dst.open('xb') as f:f.write(src.read_bytes())
        installed.append(rel)
        print('INSTALLATO:',rel,flush=True)
    return installed


def execute(script:Path,args:list[str],log:Path)->None:
    env=dict(os.environ,PYTHONDONTWRITEBYTECODE='1')
    p=subprocess.run([sys.executable,str(script),*args],capture_output=True,text=True,
                     encoding='utf8',errors='replace',env=env)
    log.write_text(p.stdout+p.stderr,encoding='utf8')
    print(script.name,'codice:',p.returncode,flush=True)
    if p.returncode:
        raise RuntimeError('Script fallito: '+script.name+'; controllare '+str(log))


def main()->int:
    parser=argparse.ArgumentParser()
    parser.add_argument('--work-dir',type=Path,default=Path('D:/DEV_APPS/RecordsNext4.0'))
    parser.add_argument('--database',type=Path,default=Path('D:/DEV_APPS/RecordsNext2.0/data/database/recordsnext-sandbox.db'))
    parser.add_argument('--output-dir',type=Path,default=Path('D:/DEV_APPS/downloads'))
    args=parser.parse_args()
    work=args.work_dir.resolve()
    db=args.database.resolve()
    out=args.output_dir.resolve()
    if not out.is_dir():raise RuntimeError('Directory downloads assente: '+str(out))
    stamp=datetime.now().strftime('%Y%m%d_%H%M%S_%f')
    root=out/('RecordsNext4_Fase3B1_OUTPUT_v1_'+stamp)
    root.mkdir(exist_ok=False)
    zip_path=out/(root.name+'.zip')
    kit=Path(__file__).resolve().parent.parent
    manifest={'fase':'3B1','kitVersion':'v1','worktree':str(work),
              'database':str(db),'stato':'ERRORE','nessunCommit':True,'nessunDeploy':True}
    ok=False
    try:
        if not db.is_file():raise RuntimeError('Database sandbox non trovato: '+str(db))
        manifest.update(validate(work))
        manifest['dbShaPrima']=sha(db)
        manifest['fileInstallati']=install(kit,work)
        execute(work/'tools/Test-RecordsNext4Contribuzioni-v1.py',
                ['--database',str(db)],root/'LOG_TEST_CONTRIBUZIONI_v1.txt')
        data=root/'dati_audit'
        data.mkdir()
        execute(work/'tools/Audit-RecordsNext4Contribuzioni-v1.py',
                ['--database',str(db),'--output-dir',str(data)],
                root/'LOG_AUDIT_CONTRIBUZIONI_v1.txt')
        analysis=json.loads((data/'RecordsNext4_Contribuzioni_v1_MANIFEST.json').read_text(encoding='utf8'))
        manifest['righeFormazione']=analysis['formazioni']
        manifest['righeTabellino']=analysis['tabellini']
        manifest['stagioni']=analysis['stagioni']
        execute(work/'tools/Create-RecordsNext4WorkingCodeMd-v8.py',
                ['--project-dir',str(work)],root/'LOG_BIBBIA_v8.txt')
        docs=root/'documentazione'
        docs.mkdir()
        for rel in FILES:
            if rel.startswith('docs/'):
                shutil.copy2(work/rel,docs/Path(rel).name)
        shutil.copy2(work/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v8.md',
                     docs/'CODICE_FUNZIONANTE_RECORDSNEXT4_v8.md')
        manifest['dbShaDopo']=sha(db)
        if manifest['dbShaPrima']!=manifest['dbShaDopo']:
            raise RuntimeError('Il database e stato modificato durante la fase 3B1!')
        manifest['gitStatusDopo']=git(work,'status','-sb')
        manifest['stato']='OK'
        ok=True
    except Exception:
        detail=traceback.format_exc()
        (root/'ERRORE_FASE3B1_v1.txt').write_text(detail,encoding='utf8')
        print('ERRORE FASE 3B1:',detail[-1800:],file=sys.stderr,flush=True)
    finally:
        (root/'MANIFEST_FASE3B1_v1.json').write_text(json.dumps(manifest,indent=2,ensure_ascii=False)+'\n',encoding='utf8')
        with ZipFile(zip_path,'x',compression=ZIP_DEFLATED,compresslevel=6) as z:
            for path in sorted(root.rglob('*')):
                if path.is_file():z.write(path,path.relative_to(root).as_posix())
        print('ESITO:', 'OK' if ok else 'ERRORE',flush=True)
        print('ZIP DA CARICARE:',zip_path,flush=True)
    return 0 if ok else 1


if __name__=='__main__':sys.exit(main())
