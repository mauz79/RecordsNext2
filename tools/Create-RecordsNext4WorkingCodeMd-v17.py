#!/usr/bin/env python3
"""Generate one additive, versioned bible with entire new files and their SHA-256."""
from __future__ import annotations
import argparse
import hashlib
import subprocess
from pathlib import Path

PREVIOUS = 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v16.md'
DEST = 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v17.md'
NEW_FILES = [
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopFcmJdbcReader.java',
    'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopFcmJdbcSmokeTest.java',
    'docs/SPECIFICA_LETTORE_JAVA_FCM_TOP_FLOP_v1.md',
    'docs/VERIFICA_LETTORE_JAVA_FCM_TOP_FLOP_v1.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v18.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v17.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v17.md',
    'tools/Create-RecordsNext4WorkingCodeMd-v17.py',
    'tools/Invoke-RecordsNext4FcmJdbc-v1.py',
]

def sha(p: Path) -> str:
    h = hashlib.sha256()
    with p.open('rb') as stream:
        for block in iter(lambda: stream.read(1048576), b''):
            h.update(block)
    return h.hexdigest()

def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument('--project-dir', required=True, type=Path)
    root = parser.parse_args().project_dir.resolve(strict=True)
    for name in [PREVIOUS, *NEW_FILES]:
        if not (root/name).is_file():
            raise RuntimeError('Missing bible input: ' + name)
    branch = subprocess.check_output(['git', '-C', str(root), 'branch', '--show-current'], text=True).strip()
    head = subprocess.check_output(['git', '-C', str(root), 'rev-parse', 'HEAD'], text=True).strip()
    lines = [
        '# RecordsNext 4.0 - Bibbia incrementale v17', '',
        'Lettore FCM JDBC read-only per la famiglia nativa Top & Flop.',
        '**Nessuna integrazione ancora nel publisher, nella pipeline o nella GUI.**', '',
        '## Origine e continuita', '',
        '- Branch: `' + branch + '`', '- HEAD: `' + head + '`',
        '- Bibbia precedente: `' + PREVIOUS + '`',
        '- SHA-256 bibbia precedente: `' + sha(root/PREVIOUS) + '`', '',
        '## Codice e documenti integrali', '',
    ]
    for name in NEW_FILES:
        path = root/name
        language = 'java' if name.endswith('.java') else 'python' if name.endswith('.py') else 'markdown'
        lines.extend(['### `' + name + '`', '', 'SHA-256: `' + sha(path) + '`', '',
                      '~~~~' + language, path.read_text(encoding='utf-8-sig').rstrip('\n'),
                      '~~~~', ''])
    data = ('\n'.join(lines).rstrip() + '\n').encode('utf-8')
    dest = root/DEST
    if dest.exists():
        if not dest.is_file() or dest.read_bytes() != data:
            raise RuntimeError('Versioned bible exists but differs: ' + str(dest))
        print('BIBBIA v17 GIA PRESENTE IDENTICA:', dest, flush=True)
    else:
        dest.write_bytes(data) if not dest.exists() else None
        print('BIBBIA v17 GENERATA:', dest, flush=True)

if __name__ == '__main__':
    main()
