#!/usr/bin/env python3
"""Additive RecordsNext4 bible v8, full file bytes and SHA-256."""
from __future__ import annotations
import argparse
import hashlib
from pathlib import Path
import subprocess
import sys

BASE='docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md'
PREVIOUS='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v7.md'
DEST='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v8.md'
NEW_FILES=[
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


def sha(path: Path) -> str:
    h=hashlib.sha256()
    with path.open('rb') as f:
        for block in iter(lambda:f.read(1024*1024),b''):
            h.update(block)
    return h.hexdigest()


def git(pr:Path,*args:str)->str:
    p=subprocess.run(['git','-C',str(pr),*args],capture_output=True,text=True,encoding='utf8',errors='replace')
    if p.returncode:raise RuntimeError('git '+ ' '.join(args) + ': ' +p.stderr.strip())
    return p.stdout.strip()


def main()->int:
    ap=argparse.ArgumentParser()
    ap.add_argument('--project-dir',required=True,type=Path)
    pr=ap.parse_args().project_dir.resolve(strict=True)
    for rel in (BASE,PREVIOUS,*NEW_FILES):
        if not (pr/rel).is_file():raise RuntimeError('File richiesto mancante: '+rel)
    lines=['# RecordsNext 4.0 - Codice funzionante / bibbia incrementale v8',
           '', 'Data: 2026-11-01. Stato: Fase 3B1, audit osservazionale, non release.',
           'Le bibbie della 3.2 e della 4.0 v3-v7 restano intatte.',
           '', '## Provenienza',
           '', '- Branch: `'+git(pr,'branch','--show-current')+'`',
           '- Commit HEAD: `'+git(pr,'rev-parse','HEAD')+'`',
           '- SHA-256 bibbia base 3.2: `'+sha(pr/BASE)+'`',
           '- SHA-256 bibbia precedente v7: `'+sha(pr/PREVIOUS)+'`',
           '- Fase 3A Windows: 9 test, 552.025 Formazione, report hash verificati.',
           '- Fase 3B1 locale: 7 test, 21 stagioni, 552.025 Formazione, 22.265 Tabellino.',
           '- Nessun calcolo di punti persi, nessuna scrittura database.',
           '', '## Nuovi file integri (testo integrale e SHA-256)', '']
    for rel in NEW_FILES:
        path=pr/rel
        lang='python' if rel.endswith('.py') else 'markdown'
        lines.extend(['### `'+rel+'`','', 'SHA256: `'+sha(path)+'`', '', '~~~~'+lang,
                      path.read_text(encoding='utf-8-sig').rstrip('\n'),'~~~~',''])
    data=('\n'.join(lines).rstrip()+'\n').encode('utf8')
    dest=pr/DEST
    if dest.exists():
        if not dest.is_file() or dest.read_bytes()!=data:
            raise RuntimeError('Bibbia v8 differente; non sovrascrivo: '+str(dest))
        print('BIBBIA v8 gia presente identica: '+str(dest))
    else:
        with dest.open('xb') as f:f.write(data)
        print('BIBBIA v8 generata: '+str(dest))
    return 0


if __name__=='__main__':
    try:sys.exit(main())
    except Exception as exc:
        print('ERRORE GENERAZIONE BIBBIA:',str(exc),file=sys.stderr,flush=True)
        sys.exit(1)
