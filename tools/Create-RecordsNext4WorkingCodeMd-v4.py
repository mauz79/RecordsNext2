#!/usr/bin/env python3
"""Genera la bibbia incrementale RecordsNext 4, Fase 0 (v4).

Non esegue il precedente generatore PowerShell v2, che contiene una regex invalida.
Non modifica codice o database. Non sovrascrive una bibbia gia generata diversa.
"""
import argparse
import hashlib
import os
from pathlib import Path
import subprocess
import sys

BASE_COMMIT = 'f9bf30323d9204d70e91e242bddfc72c2060ecf9'
FILES = [
    'docs/REVISIONE_BASE_RECORDSNEXT_3_2_v1.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v3.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v2.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v2.md',
    'docs/CORREZIONE_FASE0_RECORDSNEXT4_v1.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v4.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v3.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v3.md',
    'tools/Audit-RecordsNext4Giocatori-v1.py',
    'tools/Probe-RecordsNext4Fonti-v1.py',
    'tools/Probe-RecordsNext4Tabellini-v1.py',
    'tools/Invoke-RecordsNext4-Fase0-v3.ps1',
    'tools/Create-RecordsNext4WorkingCodeMd-v2.ps1',
    'tools/Create-RecordsNext4WorkingCodeMd-v3.py',
    'tools/Invoke-RecordsNext4-Fase0-v4.py',
    'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v3.md',
    'docs/VERIFICA_FASE0_RECORDSNEXT4_v1.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v5.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v4.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v4.md',
    'tools/Probe-RecordsNext4Fonti-v2.py',
    'tools/Create-RecordsNext4WorkingCodeMd-v4.py',
]
OUTPUT = 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v4.md'


def git(project: Path, *args: str) -> str:
    proc = subprocess.run(['git', '-C', str(project), *args], text=True,
                          encoding='utf-8', errors='replace', capture_output=True)
    if proc.returncode:
        raise RuntimeError('Git failed: ' + ' '.join(args) + ': ' + proc.stderr.strip())
    return proc.stdout.strip()


def make(project: Path) -> Path:
    missing = [name for name in FILES if not (project / name).is_file()]
    if missing:
        raise RuntimeError('Missing inputs for documentation: ' + ', '.join(missing))
    base_doc = project / 'docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md'
    if not base_doc.is_file():
        raise RuntimeError('Bibbia storica 3.2 mancante (non la modifichiamo).')
    commit = git(project, 'rev-parse', 'HEAD')
    branch = git(project, 'branch', '--show-current')
    lines = [
        '# RecordsNext 4.0 - Bibbia incrementale v4 (Fase 0)', '',
        '> DOCUMENTAZIONE DIAGNOSTICA. Non equivale alla release 4.0.',
        '> Base immutata: docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md.',
        '> Il vecchio generatore PowerShell v2 e conservato solo come storico:',
        '> falliva su -replace con regex backslash non valida.', '',
        f'Commit di partenza 3.2: `{BASE_COMMIT}`',
        f'Worktree branch: `{branch}`', f'Worktree HEAD: `{commit}`',
        'Output diagnostici: D:/DEV_APPS/downloads/RecordsNext4_Fase0_OUTPUT_v4_*.zip',
        '', '## Stato', '',
        '- Nessuna modifica a Java, SQLite, GUI e JS di produzione.',
        '- Runner fase 0 v4 scritto in Python per evitare fragilita della sintassi PS 5.1.',
        '- Audit FCM/FCA: 21 stagioni, 12 famiglie, 252/252 tabelle presenti (probe v2 corretto).',
        '- Relazioni: 105 controllate, 90 OK, 15 da verificare (168 righe Tabellino).',
        '- Gli audit non certificano di per se le identita all-time.',
        '- I tabellini con Incontro mancante restano non classificati fino alla verifica.',
        '- Gli esiti Windows vanno letti nel manifest e nei log dello ZIP, non dedotti da questa bibbia.',
        '',
        '## Base documentale storica', '',
        f'- `docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md` SHA256: `{hashlib.sha256(base_doc.read_bytes()).hexdigest()}`',
        '',
    ]
    for name in FILES:
        source = project / name
        payload = source.read_bytes()
        digest = hashlib.sha256(payload).hexdigest()
        text = payload.decode('utf-8-sig')
        lines.extend([f'## `{name}`', '', f'SHA256: `{digest}`', '', '~~~~text',
                      text.rstrip('\n'), '~~~~', ''])
    result = ('\n'.join(lines).rstrip() + '\n').encode('utf-8')
    target = project / OUTPUT
    if target.exists():
        if target.read_bytes() != result:
            raise RuntimeError('Bibbia v4 esistente diversa, non sovrascritta: ' + str(target))
        print('Bibbia v4 gia presente identica: ' + str(target), flush=True)
        return target
    try:
        with target.open('xb') as handle:
            handle.write(result)
    except FileExistsError:
        raise RuntimeError('Bibbia v4 comparsa durante la generazione: ' + str(target))
    print('Bibbia v4 generata: ' + str(target), flush=True)
    return target


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument('--project-dir', type=Path, required=True)
    args = parser.parse_args()
    try:
        make(args.project_dir.resolve())
    except Exception as error:
        print('ERRORE GENERATORE: ' + str(error), file=sys.stderr)
        sys.exit(1)


if __name__ == '__main__':
    main()
