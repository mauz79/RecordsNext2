#!/usr/bin/env python3
"""Bibbia incrementale Fase 1 v5: include sorgenti esatti, SHA, decisioni e test.

Non tocca docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md e non sovrascrive
un precedente output diverso. Deve essere eseguito dopo installazione kit.
"""
from __future__ import annotations
import argparse
import hashlib
from pathlib import Path
import subprocess
import sys

BASE_COMMIT = 'f9bf30323d9204d70e91e242bddfc72c2060ecf9'
PREVIOUS = 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v4.md'
BASE = 'docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md'
OUTPUT = 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v5.md'
NEW_FILES = [
    'docs/SPECIFICA_ESTRAZIONE_REALE_RECORDSNEXT4_v1.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v6.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v5.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v5.md',
    'docs/VERIFICA_ESTRAZIONE_REALE_RECORDSNEXT4_v1.md',
    'tools/Estrai-RecordsNext4Reali-v1.py',
    'tools/Test-RecordsNext4Reali-v1.py',
    'tools/Create-RecordsNext4WorkingCodeMd-v5.py',
    'tools/Invoke-RecordsNext4-Fase1-v1.py',
]


def digest(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def git(project: Path, *args: str) -> str:
    p = subprocess.run(['git', '-C', str(project), *args],
                       stdout=subprocess.PIPE, stderr=subprocess.PIPE,
                       text=True, encoding='utf-8', errors='replace')
    if p.returncode:
        raise RuntimeError('git ' + ' '.join(args) + ': ' + p.stderr.strip())
    return p.stdout.strip()


def main() -> int:
    p = argparse.ArgumentParser()
    p.add_argument('--project-dir', type=Path, required=True)
    args = p.parse_args()
    project = args.project_dir.resolve()
    for name in (BASE, PREVIOUS, *NEW_FILES):
        if not (project / name).is_file():
            raise RuntimeError('File mancante per la bibbia: ' + name)
    lines = [
        '# RecordsNext 4.0 — Bibbia incrementale v5 (Fase 1 reali)', '',
        '> Questa NON e la release 4.0.0; la pipeline Java 3.2 e invariata.',
        '> Conservare intatta la bibbia 3.2 e le bibbie 4.0 precedenti.', '',
        '## Riferimenti verificabili', '',
        '- Commit base 3.2: `' + BASE_COMMIT + '`',
        '- Branch: `' + git(project, 'branch', '--show-current') + '`',
        '- HEAD al momento della generazione: `' + git(project, 'rev-parse', 'HEAD') + '`',
        '- Bibbia 3.2 SHA-256: `' + digest(project / BASE) + '`',
        '- Bibbia 4.0 v4 SHA-256: `' + digest(project / PREVIOUS) + '`',
        '', '## Stato tecnico', '',
        '- Estrattore FCA read-only Python v1 funzionante sulla copia DB.',
        '- Tutte le 21 stagioni: 16.245 giocatori stagionali, 617.310 righe GiocaIn.',
        '- 7 test automatici superati sulla copia DB; esito Windows da verificare.',
        '- Nessuna scrittura SQLite, modifica FCM/FCA, commit, JS, HTML, GUI o release.',
        '- Mancato abbinamento con IncontroInA non significa errore; vedere specifica.',
        '', '## Codice e documentazione aggiunti (contenuto integrale)', '',
    ]
    for name in NEW_FILES:
        source = project / name
        body = source.read_text(encoding='utf-8-sig')
        lines.extend(['### `' + name + '`', '', 'SHA256: `' + digest(source) + '`',
                      '', '~~~~' + ('python' if name.endswith('.py') else 'markdown'),
                      body.rstrip('\n'), '~~~~', ''])
    payload = ('\n'.join(lines).rstrip() + '\n').encode('utf-8')
    target = project / OUTPUT
    if target.exists():
        if target.read_bytes() != payload:
            raise RuntimeError('Bibbia v5 diversa gia esistente, non sovrascritta: ' + str(target))
        print('Bibbia v5 gia presente e identica:', target, flush=True)
        return 0
    with target.open('xb') as f:
        f.write(payload)
    print('Bibbia v5 generata:', target, flush=True)
    return 0


if __name__ == '__main__':
    try:
        sys.exit(main())
    except Exception as e:
        print('ERRORE BIBBIA v5:', e, file=sys.stderr, flush=True)
        sys.exit(1)
