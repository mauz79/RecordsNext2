#!/usr/bin/env python3
"""Bibbia incrementale v11: precedente v10 + testo completo dei file di questa revisione."""
from __future__ import annotations
import argparse
import hashlib
from pathlib import Path
import subprocess
import sys

PREVIOUS = 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v10.md'
DEST = 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v11.md'
NEW_FILES = [
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v12.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v11.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v11.md',
    'docs/SPECIFICA_METRICA_VOTO_SPRECATO_GIOCATORE_v2.md',
    'tools/Test-RecordsNext4MetricheIndividuali-v2.py',
    'tools/Create-RecordsNext4WorkingCodeMd-v11.py',
    'tools/Invoke-RecordsNext4Metriche-v1.py',
]


def sha(path: Path) -> str:
    h=hashlib.sha256()
    with path.open('rb') as f:
        for block in iter(lambda: f.read(1024 * 1024), b''):
            h.update(block)
    return h.hexdigest()


def git(project: Path, *args: str) -> str:
    p=subprocess.run(['git','-C',str(project),*args],capture_output=True,text=True,encoding='utf8',errors='replace')
    if p.returncode:
        raise RuntimeError('Git fallito: ' + p.stderr.strip())
    return p.stdout.strip()


def main() -> int:
    parser=argparse.ArgumentParser()
    parser.add_argument('--project-dir',type=Path,required=True)
    pr=parser.parse_args().project_dir.resolve(strict=True)
    for rel in [PREVIOUS,*NEW_FILES]:
        if not (pr/rel).is_file():
            raise RuntimeError('File richiesto mancante: '+rel)
    lines=['# RecordsNext 4.0 - Codice funzionante / bibbia incrementale v11','',
           'Data: 2026-11-01. Revisione di formule; nessun calcolo FCM/FCA implementato.','',
           '## Provenienza','',
           '- Branch: `'+git(pr,'branch','--show-current')+'`',
           '- HEAD: `'+git(pr,'rev-parse','HEAD')+'`',
           '- SHA-256 bibbia precedente v10: `'+sha(pr/PREVIOUS)+'`',
           '- Questo documento INTEGRA la bibbia v10, che rimane conservata.',
           '', '## Codice e documentazione nuovi, integrali con SHA-256','']
    for rel in NEW_FILES:
        path=pr/rel
        lang='python' if rel.endswith('.py') else 'markdown'
        lines.extend(['### `'+rel+'`','', 'SHA256: `'+sha(path)+'`','',
                      '~~~~'+lang,path.read_text(encoding='utf-8-sig').rstrip('\n'),'~~~~',''])
    content=('\n'.join(lines).rstrip()+'\n').encode('utf-8')
    dest=pr/DEST
    if dest.exists():
        if not dest.is_file() or dest.read_bytes()!=content:
            raise RuntimeError('Bibbia v11 gia presente ma differente: non sovrascrivo')
        print('BIBBIA v11 gia presente identica:',dest)
    else:
        with dest.open('xb') as f: f.write(content)
        print('BIBBIA v11 generata:',dest)
    return 0


if __name__=='__main__':
    try:
        sys.exit(main())
    except Exception as exc:
        print('ERRORE BIBBIA:',str(exc),file=sys.stderr)
        sys.exit(1)
