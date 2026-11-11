#!/usr/bin/env python3
"""Additive code bible v13: previous bible SHA256 plus full new versioned files."""
from __future__ import annotations
import argparse
import hashlib
from pathlib import Path
import subprocess
import sys

PREVIOUS = 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v12.md'
DEST = 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v13.md'
NEW_FILES = [
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v14.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v13.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v13.md',
    'docs/SPECIFICA_ARCHIVIO_COMPLETO_GIOCATORE_v2.md',
    'tools/Audit-RecordsNext4Contesti-v2.py',
    'tools/Test-RecordsNext4Contesti-v2.py',
    'tools/Create-RecordsNext4WorkingCodeMd-v13.py',
    'tools/Invoke-RecordsNext4Contesti-v2.py',
]


def sha(path):
    h = hashlib.sha256()
    with Path(path).open('rb') as stream:
        for block in iter(lambda: stream.read(1024*1024), b''):
            h.update(block)
    return h.hexdigest()


def git(project, *args):
    proc = subprocess.run(['git', '-C', str(project), *args],
                          capture_output=True, text=True, encoding='utf-8', errors='replace')
    if proc.returncode:
        raise RuntimeError('Git: ' + proc.stderr.strip())
    return proc.stdout.strip()


def main():
    cli = argparse.ArgumentParser()
    cli.add_argument('--project-dir', type=Path, required=True)
    project = cli.parse_args().project_dir.resolve(strict=True)
    for rel in [PREVIOUS, *NEW_FILES]:
        if not (project / rel).is_file():
            raise RuntimeError('File mancante per bibbia v13: ' + rel)
    lines = [
        '# RecordsNext 4.0 - Bibbia incrementale v13', '',
        'Data: 2026-11-01. Fase 3C1: classificazione definitiva e contratto link dei record, senza modifiche Java/DB/sito.', '',
        '## Provenienza e continuita', '',
        '- Branch: `' + git(project, 'branch', '--show-current') + '`',
        '- Commit base: `' + git(project, 'rev-parse', 'HEAD') + '`',
        '- Bibbia precedente v12 SHA-256: `' + sha(project/PREVIOUS) + '`',
        '- La presente bibbia INTEGRA, non sostituisce, la v12.', '',
        '## Testo integrale di sorgenti e documenti con impronta', ''
    ]
    for rel in NEW_FILES:
        path = project / rel
        lang = 'python' if rel.endswith('.py') else 'markdown'
        lines.extend(['### `' + rel + '`', '', 'SHA-256: `' + sha(path) + '`', '',
                      '~~~~' + lang, path.read_text(encoding='utf-8-sig').rstrip('\n'),
                      '~~~~', ''])
    blob = ('\n'.join(lines).rstrip() + '\n').encode('utf-8')
    path = project / DEST
    if path.exists():
        if not path.is_file() or path.read_bytes() != blob:
            raise RuntimeError('Bibbia v13 gia presente ma diversa: non sovrascrivo')
        print('BIBBIA v13 gia presente identica:', path)
    else:
        with path.open('xb') as stream:
            stream.write(blob)
        print('BIBBIA v13 generata:', path)
    return 0

if __name__ == '__main__':
    try:
        sys.exit(main())
    except Exception as exc:
        print('ERRORE BIBBIA v13:', exc, file=sys.stderr)
        sys.exit(1)
