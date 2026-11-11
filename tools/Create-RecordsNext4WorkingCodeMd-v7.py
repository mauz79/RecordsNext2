#!/usr/bin/env python3
"""Generate additive Bible v7 with exact Phase 3A files and sha256 hashes."""
from __future__ import annotations
import argparse
import hashlib
from pathlib import Path
import subprocess
import sys

BASE='docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md'
PREVIOUS='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v6.md'
DEST='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v7.md'
NEW_FILES=[
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

def sha(p:Path)->str:
    h=hashlib.sha256()
    with p.open('rb') as f:
        for chunk in iter(lambda:f.read(1024*1024),b''):h.update(chunk)
    return h.hexdigest()

def git(project:Path,*args:str)->str:
    p=subprocess.run(['git','-C',str(project),*args],capture_output=True,text=True,
                     encoding='utf8',errors='replace')
    if p.returncode:raise RuntimeError('Git command failed: '+' '.join(args)+' '+p.stderr)
    return p.stdout.strip()

def main():
    ap=argparse.ArgumentParser();ap.add_argument('--project-dir',required=True,type=Path)
    pr=ap.parse_args().project_dir.resolve()
    for f in (BASE,PREVIOUS,*NEW_FILES):
        if not (pr/f).is_file():raise RuntimeError('Missing file: '+f)
    lines=['# RecordsNext 4 - Codice funzionante / bibbia incrementale v7','',
           'Stato: Fase 3A audit osservazionale Python, non release e non pipeline Java.',
           'Le bibbie 3.2 e 4.0 precedenti rimangono immutate.','',
           '## Provenienza','',
           '- Branch: `'+git(pr,'branch','--show-current')+'`',
           '- HEAD: `'+git(pr,'rev-parse','HEAD')+'`',
           '- SHA bibbia 3.2: `'+sha(pr/BASE)+'`',
           '- SHA bibbia 4.0 v6: `'+sha(pr/PREVIOUS)+'`',
           '- Fase 2: audit Windows 8/8, quattro output con SHA verificati.',
           '- Fase 3A: 9/9 test locali, 21 stagioni, 552.025 righe.',
           '- Non sono stati stimati punti persi o simulazioni controfattuali.','',
           '## File aggiunti nella Fase 3A','']
    for f in NEW_FILES:
        p=pr/f
        lang='python' if f.endswith('.py') else 'markdown'
        lines.extend(['### `'+f+'`','','SHA256: `'+sha(p)+'`','','~~~~'+lang,
                      p.read_text(encoding='utf-8-sig').rstrip('\n'),'~~~~',''])
    data=('\n'.join(lines).rstrip()+'\n').encode('utf8')
    target=pr/DEST
    if target.exists():
        if target.read_bytes()!=data:raise RuntimeError('Bible v7 differs; refusing to overwrite: '+str(target))
        print('BIBBIA v7 gia presente identica: '+str(target))
    else:
        with target.open('xb') as f:f.write(data)
        print('BIBBIA v7 generata: '+str(target))
    return 0

if __name__=='__main__':
    try:sys.exit(main())
    except Exception as exc:
        print('ERRORE BIBBIA v7: '+str(exc),file=sys.stderr);sys.exit(1)
