#!/usr/bin/env python3
"""Append-only v15 bible; includes exact Java/Python/Markdown code and SHA-256."""
from __future__ import annotations
import argparse
import hashlib
from pathlib import Path
import subprocess
import sys

PREVIOUS = 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v14.md'
DEST = 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v15.md'
NEW_FILES = [
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopFacts.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopArchive.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopMetrics.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopCompetitionPolicy.java',
    'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopArchiveSmokeTest.java',
    'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopArchiveJUnitTest.java',
    'docs/SPECIFICA_MODELLO_JAVA_TOP_FLOP_v1.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v16.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v15.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v15.md',
    'tools/Create-RecordsNext4WorkingCodeMd-v15.py',
    'tools/Invoke-RecordsNext4TopFlopJava-v1.py',
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
        '# RecordsNext 4.0 - Bibbia incrementale v15', '',
        'Primo modello canonico Java della famiglia Records Top & Flop.',
        '**Non integrato ancora nel motore di elaborazione/pubblicazione.**', '',
        '## Provenienza', '',
        '- Branch: `' + git(root, 'branch', '--show-current') + '`',
        '- HEAD: `' + git(root, 'rev-parse', 'HEAD') + '`',
        '- Riferimento preservato: `'+ PREVIOUS +'`',
        '- SHA-256 bibbia v14: `' + sha(root / PREVIOUS) + '`',
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
            raise RuntimeError('Bibbia v15 esistente ma diversa, non sovrascrivo')
        print('BIBBIA v15 GIA PRESENTE IDENTICA:', target, flush=True)
    else:
        with target.open('xb') as stream:
            stream.write(content)
        print('BIBBIA v15 GENERATA:', target, flush=True)
    return 0


if __name__ == '__main__':
    try:
        sys.exit(main())
    except Exception as exc:
        print('ERRORE BIBBIA v15:', exc, file=sys.stderr)
        sys.exit(1)
