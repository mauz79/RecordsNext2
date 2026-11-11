#!/usr/bin/env python3
"""Conservative incremental bible preserving the verified v13 reference."""
from __future__ import annotations
import argparse
import hashlib
from pathlib import Path
import subprocess
import sys

PREVIOUS='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v13.md'
DEST='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v14.md'
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


def sha(p):
    h=hashlib.sha256()
    with p.open('rb') as stream:
        for block in iter(lambda:stream.read(1024*1024),b''):
            h.update(block)
    return h.hexdigest()


def git(root,*args):
    proc=subprocess.run(['git','-C',str(root),*args],capture_output=True,
                        text=True,encoding='utf-8',errors='replace')
    if proc.returncode:
        raise RuntimeError('Git: '+proc.stderr.strip())
    return proc.stdout.strip()


def main():
    ap=argparse.ArgumentParser()
    ap.add_argument('--project-dir',required=True,type=Path)
    root=ap.parse_args().project_dir.resolve(strict=True)
    for name in [PREVIOUS,*NEW_FILES]:
        if not (root/name).is_file():
            raise RuntimeError('Sorgente bibbia mancante: '+name)
    lines=['# RecordsNext 4.0 - Bibbia incrementale v14','',
           'Fase integrazione architetturale Records Top & Flop: nessun Java, database o output sito modificato.','',
           '## Provenienza', '',
           '- Branch: `'+git(root,'branch','--show-current')+'`',
           '- HEAD: `'+git(root,'rev-parse','HEAD')+'`',
           '- Bibbia v13 SHA-256: `'+sha(root/PREVIOUS)+'`',
           '- Questo documento e additivo e non sostituisce v13.','',
           '## File completi e SHA-256','']
    for name in NEW_FILES:
        file=root/name
        lang='python' if name.endswith('.py') else 'markdown'
        lines.extend(['### `'+name+'`','','SHA-256: `'+sha(file)+'`','',
                      '~~~~'+lang,file.read_text(encoding='utf-8-sig').rstrip('\n'),
                      '~~~~',''])
    out=('\n'.join(lines).rstrip()+'\n').encode('utf-8')
    target=root/DEST
    if target.exists():
        if not target.is_file() or target.read_bytes()!=out:
            raise RuntimeError('Bibbia v14 gia presente ma diversa: stop')
        print('BIBBIA v14 GIA PRESENTE IDENTICA:',target,flush=True)
    else:
        with target.open('xb') as stream:
            stream.write(out)
        print('BIBBIA v14 GENERATA:',target,flush=True)
    return 0


if __name__=='__main__':
    try:
        sys.exit(main())
    except Exception as exc:
        print('ERRORE BIBBIA v14:',exc,file=sys.stderr)
        sys.exit(1)
