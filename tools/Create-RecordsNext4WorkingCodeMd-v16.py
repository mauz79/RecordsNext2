#!/usr/bin/env python3
"""Append-only v16 bible; includes exact Java/Python/Markdown code and SHA-256."""
from __future__ import annotations
import argparse
import hashlib
from pathlib import Path
import subprocess
import sys

PREVIOUS = 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v15.md'
DEST = 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v16.md'
NEW_FILES = [
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopFcaJdbcReader.java',
    'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopFcaJdbcSmokeTest.java',
    'docs/SPECIFICA_LETTORE_JAVA_FCA_TOP_FLOP_v1.md',
    'docs/VERIFICA_LETTORE_JAVA_FCA_TOP_FLOP_v1.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v17.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v16.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v16.md',
    'tools/Create-RecordsNext4WorkingCodeMd-v16.py',
    'tools/Invoke-RecordsNext4FcaJdbc-v1.py',
]


def sha(path: Path) -> str:
    h = hashlib.sha256()
    with path.open('rb') as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b''):
            h.update(block)
    return h.hexdigest()


def git(root: Path, *args: str) -> str:
    p = subprocess.run(['git', '-C', str(root), *args], capture_output=True,
                       text=True, encoding='utf-8', errors='replace')
    if p.returncode:
        raise RuntimeError('Git: ' + p.stderr.strip())
    return p.stdout.strip()


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument('--project-dir', required=True, type=Path)
    root = ap.parse_args().project_dir.resolve(strict=True)
    for name in [PREVIOUS, *NEW_FILES]:
        if not (root / name).is_file():
            raise RuntimeError('Prerequisito bibbia non trovato: ' + name)
    lines = [
        '# RecordsNext 4.0 - Bibbia incrementale v16', '',
        'Lettore JDBC FCA di sola lettura collegato al modello Java Top & Flop.',
        '**Non integrato ancora nel motore di elaborazione/pubblicazione.**', '',
        '## Provenienza', '',
        '- Branch: `' + git(root, 'branch', '--show-current') + '`',
        '- HEAD: `' + git(root, 'rev-parse', 'HEAD') + '`',
        '- Riferimento preservato: `'+ PREVIOUS +'`',
        '- SHA-256 bibbia v15: `' + sha(root / PREVIOUS) + '`',
        '- Codice Java 3.2 esistente: invariato in questa installazione.', '',
        '## File integrali e impronte SHA-256', '',
    ]
    for name in NEW_FILES:
        path = root / name
        language = 'java' if name.endswith('.java') else ('python' if name.endswith('.py') else 'markdown')
        lines += ['### `' + name + '`', '', 'SHA-256: `' + sha(path) + '`', '',
                  '~~~~' + language, path.read_text(encoding='utf-8-sig').rstrip('\n'),
                  '~~~~', '']
    content = ('\n'.join(lines).rstrip() + '\n').encode('utf-8')
    target = root / DEST
    if target.exists():
        if not target.is_file() or target.read_bytes() != content:
            raise RuntimeError('Bibbia v16 esistente ma diversa, non sovrascrivo')
        print('BIBBIA v16 GIA PRESENTE IDENTICA:', target, flush=True)
    else:
        with target.open('xb') as stream:
            stream.write(content)
        print('BIBBIA v16 GENERATA:', target, flush=True)
    return 0


if __name__ == '__main__':
    try:
        sys.exit(main())
    except Exception as exc:
        print('ERRORE BIBBIA v16:', exc, file=sys.stderr)
        sys.exit(1)
