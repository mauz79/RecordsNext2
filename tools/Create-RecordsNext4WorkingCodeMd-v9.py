#!/usr/bin/env python3
"""Versioned, additive RecordsNext4 bible including complete phase-3B2 source files."""
from __future__ import annotations
import argparse
import hashlib
from pathlib import Path
import subprocess
import sys

BASE='docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md'
PREVIOUS='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v8.md'
DEST='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v9.md'
NEW_FILES=[
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v10.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v9.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v9.md',
    'docs/SPECIFICA_AUDIT_REGOLE_RECORDSNEXT4_v1.md',
    'docs/VERIFICA_AUDIT_REGOLE_RECORDSNEXT4_v1.md',
    'tools/Audit-RecordsNext4Regole-v1.py',
    'tools/Test-RecordsNext4Regole-v1.py',
    'tools/Create-RecordsNext4WorkingCodeMd-v9.py',
    'tools/Invoke-RecordsNext4-Fase3B2-v1.py',
]


def sha(path:Path)->str:
    h=hashlib.sha256()
    with path.open('rb') as f:
        for block in iter(lambda:f.read(1024*1024),b''):
            h.update(block)
    return h.hexdigest()


def git(pr:Path,*args:str)->str:
    p=subprocess.run(['git','-C',str(pr),*args],capture_output=True,text=True,encoding='utf8',errors='replace')
    if p.returncode:
        raise RuntimeError('git '+ ' '.join(args) + ': ' +p.stderr.strip())
    return p.stdout.strip()


def main()->int:
    ap=argparse.ArgumentParser()
    ap.add_argument('--project-dir',required=True,type=Path)
    pr=ap.parse_args().project_dir.resolve(strict=True)
    for rel in (BASE,PREVIOUS,*NEW_FILES):
        if not (pr/rel).is_file():
            raise RuntimeError('File richiesto mancante: '+rel)
    lines=[
        '# RecordsNext 4.0 - Codice funzionante / bibbia incrementale v9',
        '', 'Data: 2026-11-01. Fase 3B2, censimento regolamentare, non release.',
        'Le bibbie 3.2 e 4.0 v3--v8 restano intatte.',
        '', '## Provenienza', '',
        '- Branch: `'+git(pr,'branch','--show-current')+'`',
        '- Commit HEAD: `'+git(pr,'rev-parse','HEAD')+'`',
        '- SHA-256 bibbia base 3.2: `'+sha(pr/BASE)+'`',
        '- SHA-256 bibbia precedente v8: `'+sha(pr/PREVIOUS)+'`',
        '- Fase 3B1 Windows: 7 test, 21 stagioni, 22.265 tabellini, 552.025 Formazione, hash rapporti verificati.',
        '- Fase 3B2 locale: 8 test, 180 configurazioni Competizione, 22.265 tabellini.',
        '- Nessun punto recuperabile calcolato; nessuna scrittura database.',
        '', '## Nuovi file integri (testo integrale e SHA-256)', ''
    ]
    for rel in NEW_FILES:
        path=pr/rel
        lang='python' if rel.endswith('.py') else 'markdown'
        lines.extend([
            '### `'+rel+'`','', 'SHA256: `'+sha(path)+'`', '',
            '~~~~'+lang,path.read_text(encoding='utf-8-sig').rstrip('\n'),'~~~~',''
        ])
    content=('\n'.join(lines).rstrip()+'\n').encode('utf8')
    dest=pr/DEST
    if dest.exists():
        if not dest.is_file() or dest.read_bytes()!=content:
            raise RuntimeError('Bibbia v9 differente; non sovrascrivo: '+str(dest))
        print('BIBBIA v9 gia presente identica:',dest)
    else:
        with dest.open('xb') as f:
            f.write(content)
        print('BIBBIA v9 generata:',dest)
    return 0


if __name__=='__main__':
    try:
        sys.exit(main())
    except Exception as exc:
        print('ERRORE GENERAZIONE BIBBIA:',str(exc),file=sys.stderr,flush=True)
        sys.exit(1)
