#!/usr/bin/env python3
"""Install versioned docs, verify 3.2 anchors, generate v14 bible; no DB changes."""
from __future__ import annotations
import argparse
from datetime import datetime
import hashlib
import json
import os
from pathlib import Path
import subprocess
import sys
import traceback
from zipfile import ZipFile,ZIP_DEFLATED

NEW_FILES=[
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v15.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v14.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v14.md',
    'docs/SPECIFICA_RECORDS_TOP_FLOP_v1.md',
    'docs/MAPPA_INTEGRAZIONE_TOP_FLOP_3_2_v1.md',
    'tools/Test-RecordsNext4TopFlopContract-v1.py',
    'tools/Create-RecordsNext4WorkingCodeMd-v14.py',
    'tools/Invoke-RecordsNext4TopFlop-v1.py',
]
REQUIRED=[
    'pom.xml',
    'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v13.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v14.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v13.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v13.md',
]


def sha(path):
    h=hashlib.sha256()
    with path.open('rb') as s:
        for chunk in iter(lambda:s.read(1024*1024),b''):
            h.update(chunk)
    return h.hexdigest()


def git(root,*args):
    proc=subprocess.run(['git','-C',str(root),*args],capture_output=True,text=True,
                        encoding='utf-8',errors='replace')
    if proc.returncode:
        raise RuntimeError('Git: '+proc.stderr.strip())
    return proc.stdout.strip()


def run(argv):
    proc=subprocess.run([sys.executable,*map(str,argv)],capture_output=True,text=True,
                        encoding='utf-8',errors='replace',env=dict(os.environ,PYTHONDONTWRITEBYTECODE='1'))
    return proc.returncode,proc.stdout+proc.stderr


def main():
    ap=argparse.ArgumentParser()
    ap.add_argument('--work-dir',type=Path,default=Path('D:/DEV_APPS/RecordsNext4.0'))
    ap.add_argument('--output-dir',type=Path,default=Path('D:/DEV_APPS/downloads'))
    args=ap.parse_args()
    kit=Path(__file__).resolve().parent.parent
    output=args.output_dir.resolve(strict=True)
    timestamp=datetime.now().strftime('%Y%m%d_%H%M%S_%f')
    result=output/('RecordsNext4_TopFlopIntegrazione_OUTPUT_v1_'+timestamp+'.zip')
    report={'fase':'integrazione-architetturale-topflop','stato':'ERRORE',
            'javaModificato':False,'databaseModificato':False,'pubblicazioneEseguita':False,
            'bibbia':'v14','installati':[]}
    logs=[]
    ok=False
    work=None
    try:
        work=args.work_dir.resolve(strict=True)
        for name in REQUIRED:
            if not (work/name).is_file():
                raise RuntimeError('Prerequisito mancante: '+name)
        root=Path(git(work,'rev-parse','--show-toplevel')).resolve(strict=True)
        if os.path.normcase(str(root)).casefold()!=os.path.normcase(str(work)).casefold():
            raise RuntimeError('Il path non e la root del worktree: '+str(root))
        branch=git(work,'branch','--show-current')
        if branch!='feature/recordsnext4-players':
            raise RuntimeError('Branch inatteso: '+branch)
        report['branch']=branch
        report['head']=git(work,'rev-parse','HEAD')
        for name in NEW_FILES:
            source,target=kit/name,work/name
            if not source.is_file():
                raise RuntimeError('File kit mancante: '+name)
            if target.exists() and (not target.is_file() or sha(source)!=sha(target)):
                raise RuntimeError('File esistente diverso; non sovrascrivo: '+name)
        for name in NEW_FILES:
            source,target=kit/name,work/name
            if target.is_file():
                print('GIA PRESENTE IDENTICO:',name,flush=True)
            else:
                target.parent.mkdir(parents=True,exist_ok=True)
                with target.open('xb') as s:
                    s.write(source.read_bytes())
                report['installati'].append(name)
                print('INSTALLATO:',name,flush=True)
        operations=[
            ('Test-RecordsNext4TopFlopContract-v1.py',
             [work/'tools/Test-RecordsNext4TopFlopContract-v1.py','--project-dir',work]),
            ('Create-RecordsNext4WorkingCodeMd-v14.py',
             [work/'tools/Create-RecordsNext4WorkingCodeMd-v14.py','--project-dir',work]),
        ]
        for label,cmd in operations:
            status,outputText=run(cmd)
            logs.append((label,status,outputText))
            print(label,'codice:',status,flush=True)
            if status:
                raise RuntimeError('Fallito '+label+': '+outputText[-2500:])
        report['bibbiaSha256']=sha(work/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v14.md')
        report['stato']='OK'
        ok=True
    except Exception:
        error=traceback.format_exc()
        logs.append(('ERRORE',1,error))
        print(error,file=sys.stderr,flush=True)
    finally:
        with ZipFile(result,'x',compression=ZIP_DEFLATED) as z:
            z.writestr('MANIFEST.json',json.dumps(report,indent=2,ensure_ascii=False)+'\n')
            for label,status,body in logs:
                z.writestr('log/'+label.replace('.py','.txt'),'codice: '+str(status)+'\n'+body)
            for name in NEW_FILES:
                path=work/name if work is not None else None
                if path is not None and path.is_file():
                    z.write(path,name)
            if work is not None:
                bible=work/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v14.md'
                if bible.is_file():
                    z.write(bible,'docs/'+bible.name)
        print('ESITO:','OK' if ok else 'ERRORE',flush=True)
        print('ZIP DA CARICARE:',result,flush=True)
    return 0 if ok else 1


if __name__=='__main__':
    sys.exit(main())
