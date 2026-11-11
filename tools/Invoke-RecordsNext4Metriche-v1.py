#!/usr/bin/env python3
"""Installazione additiva revisione metriche, test e bibbia v11. Non apre DB."""
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
from zipfile import ZipFile, ZIP_DEFLATED

FILES = [
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v12.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v11.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v11.md',
    'docs/SPECIFICA_METRICA_VOTO_SPRECATO_GIOCATORE_v2.md',
    'tools/Test-RecordsNext4MetricheIndividuali-v2.py',
    'tools/Create-RecordsNext4WorkingCodeMd-v11.py',
    'tools/Invoke-RecordsNext4Metriche-v1.py',
]
REQUIRED = [
    'pom.xml',
    'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v10.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v10.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v11.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v10.md',
]


def sha(path: Path) -> str:
    h=hashlib.sha256()
    with path.open('rb') as f:
        for chunk in iter(lambda: f.read(1024 * 1024), b''):
            h.update(chunk)
    return h.hexdigest()


def git(project: Path, *args: str) -> str:
    p=subprocess.run(['git','-C',str(project),*args],capture_output=True,text=True,encoding='utf-8',errors='replace')
    if p.returncode:
        raise RuntimeError('Git fallito: '+ ' '.join(args) + ': ' + p.stderr.strip())
    return p.stdout.strip()


def main() -> int:
    parser=argparse.ArgumentParser()
    parser.add_argument('--work-dir', type=Path, default=Path('D:/DEV_APPS/RecordsNext4.0'))
    parser.add_argument('--output-dir', type=Path, default=Path('D:/DEV_APPS/downloads'))
    args=parser.parse_args()
    work=args.work_dir.resolve(strict=True)
    output=args.output_dir.resolve(strict=True)
    kit=Path(__file__).resolve().parent.parent
    stamp=datetime.now().strftime('%Y%m%d_%H%M%S_%f')
    package=output/('RecordsNext4_Metriche_OUTPUT_v1_'+stamp+'.zip')
    report={'fase':'revisione-metriche-v2','stato':'ERRORE','installati':[],
            'bibbia':'v11','nessunDeploy':True,'nessunCommit':True}
    logs=[]
    ok=False
    try:
        for rel in REQUIRED:
            if not (work/rel).is_file():
                raise RuntimeError('File prerequisito mancante: '+rel)
        root=Path(git(work,'rev-parse','--show-toplevel')).resolve()
        if os.path.normcase(str(root))!=os.path.normcase(str(work)):
            raise RuntimeError('Worktree non corrispondente: '+str(root))
        branch=git(work,'branch','--show-current')
        if branch!='feature/recordsnext4-players':
            raise RuntimeError('Branch inatteso: '+branch)
        report['branch']=branch
        report['head']=git(work,'rev-parse','HEAD')
        for rel in FILES:
            src,dst=kit/rel,work/rel
            if not src.is_file():
                raise RuntimeError('File kit assente: '+rel)
            if dst.exists() and (not dst.is_file() or sha(src)!=sha(dst)):
                raise RuntimeError('File gia esistente differente; non sovrascrivo: '+rel)
        for rel in FILES:
            src,dst=kit/rel,work/rel
            if dst.is_file():
                print('GIA PRESENTE IDENTICO:',rel,flush=True)
                continue
            dst.parent.mkdir(parents=True,exist_ok=True)
            with dst.open('xb') as f: f.write(src.read_bytes())
            report['installati'].append(rel)
            print('INSTALLATO:',rel,flush=True)
        env=dict(os.environ,PYTHONDONTWRITEBYTECODE='1')
        for script,argv in [
            ('tools/Test-RecordsNext4MetricheIndividuali-v2.py',[]),
            ('tools/Create-RecordsNext4WorkingCodeMd-v11.py',['--project-dir',str(work)])
        ]:
            p=subprocess.run([sys.executable,str(work/script),*argv],capture_output=True,
                             text=True,encoding='utf-8',errors='replace',env=env)
            logs.append((script,p.returncode,p.stdout+p.stderr))
            print(script,'codice:',p.returncode,flush=True)
            if p.returncode:
                raise RuntimeError('Errore script: '+script)
        report['stato']='OK'; ok=True
    except Exception:
        logs.append(('ERRORE',1,traceback.format_exc()))
        print('ERRORE:',logs[-1][2],file=sys.stderr,flush=True)
    finally:
        with ZipFile(package,'x',compression=ZIP_DEFLATED) as z:
            z.writestr('MANIFEST.json',json.dumps(report,indent=2,ensure_ascii=False)+'\n')
            for script,code,log in logs:
                z.writestr('log/'+Path(script).name.replace('.py','.txt'),log)
            for rel in FILES:
                target=work/rel
                if target.is_file(): z.write(target,rel)
            bible=work/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v11.md'
            if bible.is_file(): z.write(bible,'docs/'+bible.name)
        print('ESITO:', 'OK' if ok else 'ERRORE',flush=True)
        print('ZIP DA CARICARE:',package,flush=True)
    return 0 if ok else 1


if __name__=='__main__':
    try:
        sys.exit(main())
    except Exception as exc:
        print('ERRORE FATALE:',str(exc),file=sys.stderr,flush=True)
        sys.exit(1)
