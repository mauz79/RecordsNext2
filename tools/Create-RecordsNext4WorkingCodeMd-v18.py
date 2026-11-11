#!/usr/bin/env python3
"""Additive, versioned continuity bible: complete code, docs and SHA-256."""
from __future__ import annotations
import argparse
import hashlib
from pathlib import Path
import subprocess

PREVIOUS = 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v17.md'
DEST = 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v18.md'
NEW_FILES = [
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopJdbcArchiveAssembler.java',
    'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopJdbcArchiveAssemblerSmokeTest.java',
    'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopJdbcArchive2026SmokeTest.java',
    'docs/SPECIFICA_ASSEMBLATORE_JAVA_TOP_FLOP_v1.md',
    'docs/VERIFICA_ASSEMBLATORE_JAVA_TOP_FLOP_v1.md',
    'docs/NOTA_FCM_GIORNATE_MERCATO_2026_2027_v1.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v19.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v18.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v18.md',
    'tools/Create-RecordsNext4WorkingCodeMd-v18.py',
    'tools/Invoke-RecordsNext4ArchiveBridge-v2.py',
]

def sha(path: Path) -> str:
    h = hashlib.sha256()
    with path.open('rb') as stream:
        for block in iter(lambda: stream.read(1048576), b''):
            h.update(block)
    return h.hexdigest()

def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument('--project-dir', required=True, type=Path)
    root = parser.parse_args().project_dir.resolve(strict=True)
    for name in [PREVIOUS, *NEW_FILES]:
        if not (root/name).is_file(): raise RuntimeError('Bibbia: file mancante: '+name)
    branch = subprocess.check_output(['git','-C',str(root),'branch','--show-current'],text=True).strip()
    head = subprocess.check_output(['git','-C',str(root),'rev-parse','HEAD'],text=True).strip()
    lines = ['# RecordsNext 4.0 - Bibbia incrementale v18', '',
             'Assemblaggio stagionale FCA+FCM in memoria, codice completo.',
             '**Top & Flop non ancora abilitato nel catalogo, nella GUI o nel publisher.**', '',
             '## Continuita', '', '- Branch: `'+branch+'`', '- HEAD: `'+head+'`',
             '- Bibbia precedente: `'+PREVIOUS+'`',
             '- SHA-256 bibbia precedente: `'+sha(root/PREVIOUS)+'`', '',
             '## File integrali di questa revisione', '']
    for name in NEW_FILES:
        file = root/name
        language = 'java' if name.endswith('.java') else 'python' if name.endswith('.py') else 'markdown'
        lines.extend(['### `'+name+'`','', 'SHA-256: `'+sha(file)+'`', '',
                      '~~~~'+language, file.read_text(encoding='utf-8-sig').rstrip('\n'), '~~~~',''])
    data = ('\n'.join(lines).rstrip()+'\n').encode('utf-8')
    dest = root/DEST
    if dest.exists():
        if not dest.is_file() or dest.read_bytes() != data:
            raise RuntimeError('Bibbia v18 gia presente DIVERSA: '+str(dest))
        print('BIBBIA v18 GIA PRESENTE IDENTICA:',dest,flush=True)
    else:
        with dest.open('xb') as stream: stream.write(data)
        print('BIBBIA v18 GENERATA:',dest,flush=True)

if __name__ == '__main__': main()
