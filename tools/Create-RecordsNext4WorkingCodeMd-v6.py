#!/usr/bin/env python3
"""Bibbia incrementale v6 Fase 2, genera codice esatto e impronte SHA-256."""
from __future__ import annotations
import argparse
import hashlib
from pathlib import Path
import subprocess
import sys

BASE_COMMIT = 'f9bf30323d9204d70e91e242bddfc72c2060ecf9'
PREVIOUS = 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v5.md'
BASE = 'docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md'
OUTPUT = 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v6.md'
NEW_FILES = [
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


def sha256(p: Path) -> str:
    h=hashlib.sha256()
    with p.open('rb') as f:
        for block in iter(lambda: f.read(1024*1024),b''):
            h.update(block)
    return h.hexdigest()


def git(project: Path, *args: str) -> str:
    p=subprocess.run(['git','-C',str(project),*args],capture_output=True,text=True,encoding='utf8',errors='replace')
    if p.returncode:
        raise RuntimeError('Git: '+p.stderr.strip())
    return p.stdout.strip()


def main() -> int:
    ap=argparse.ArgumentParser()
    ap.add_argument('--project-dir',required=True,type=Path)
    project=ap.parse_args().project_dir.resolve()
    for n in (BASE,PREVIOUS,*NEW_FILES):
        if not (project/n).is_file(): raise RuntimeError('File non presente: '+n)
    lines=['# RecordsNext 4 — Codice funzionante / bibbia incrementale v6', '',
           'Stato: Fase 2 Python sperimentale, non release 4.0 e non pipeline Java.',
           'La bibbia 3.2 e le bibbie 4.0 precedenti NON vengono alterate.', '',
           '## Collegamenti e controlli', '',
           '- Commit base: `'+BASE_COMMIT+'`',
           '- Branch: `'+git(project,'branch','--show-current')+'`',
           '- HEAD: `'+git(project,'rev-parse','HEAD')+'`',
           '- SHA bibbia 3.2: `'+sha256(project/BASE)+'`',
           '- SHA bibbia 4.0 v5: `'+sha256(project/PREVIOUS)+'`',
           '- Fase 1 testata anche su Windows: 7/7, DB SHA invariato.',
           '- Fase 2: 8/8 test e 552.025 slot univoci sulla copia SQLite;',
           '  richiede ancora verifica Windows, nessun deploy o commit.',
           '', '## File esatti aggiunti nella fase 2', '']
    for n in NEW_FILES:
        p=project/n
        body=p.read_text(encoding='utf-8-sig')
        lang='python' if n.endswith('.py') else 'markdown'
        lines.extend(['### `'+n+'`','', 'SHA256: `'+sha256(p)+'`','',
                      '~~~~'+lang,body.rstrip('\n'),'~~~~',''])
    payload=('\n'.join(lines).rstrip()+'\n').encode('utf8')
    dest=project/OUTPUT
    if dest.exists():
        if dest.read_bytes()!=payload:
            raise RuntimeError('Bibbia v6 presente ma differente: nessuna sovrascrittura '+str(dest))
        print('Bibbia v6 gia presente identica:',dest)
    else:
        with dest.open('xb') as f: f.write(payload)
        print('Bibbia v6 generata:',dest)
    return 0

if __name__=='__main__':
    try: sys.exit(main())
    except Exception as e:
        print('ERRORE BIBBIA v6:',str(e),file=sys.stderr)
        sys.exit(1)
