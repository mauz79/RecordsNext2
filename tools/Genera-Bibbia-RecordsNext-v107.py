#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Genera Bibbia v55 dalla Bibbia v54 integra e dai sorgenti della release.
Conserva il testo canonico preesistente, aggiungendo senza tagli il codice v107.
Eseguire in repository o nel pacchetto release v107.
"""
from pathlib import Path
import hashlib
import sys

ROOT = Path(__file__).resolve().parents[1]
DOCS = ROOT / 'docs'
SRC = DOCS / 'CODICE_FUNZIONANTE_RECORDSNEXT4_v54.md'
DST = DOCS / 'CODICE_FUNZIONANTE_RECORDSNEXT4_v55.md'
INCLUDE = [
    ('Inno Setup 4.0', 'installer/RecordsNext4_v107.iss', 'ini'),
    ('Launcher Windows', 'installer/RecordsNext.bat', 'bat'),
    ('Launcher diagnostico', 'installer/Avvia-RecordsNext4.ps1', 'powershell'),
    ('Guida installazione pubblica', 'installer/INSTALLAZIONE_RECORDSNEXT4.md', 'markdown'),
    ('Builder installer v107', 'tools/RN4_Crea-Installer-v107.py', 'python'),
    ('Generatore Bibbia v55', 'tools/Genera-Bibbia-RecordsNext-v107.py', 'python'),
    ('Test release v107', 'tests/test_release_v107.py', 'python'),
    ('Changelog', 'docs/CHANGELOG_RECORDSNEXT4_v107.md', 'markdown'),
    ('Diario', 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v107.md', 'markdown'),
    ('Registro decisioni', 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v107.md', 'markdown'),
    ('Test e gate', 'docs/TEST_RELEASE_RECORDSNEXT4_v107.md', 'markdown'),
    ('Continuita', 'docs/CONTINUITA_RECORDSNEXT4_v107.md', 'markdown'),
]

def main():
    if not SRC.is_file():
        raise SystemExit('Bibbia v54 mancante: non rigenero per evitare amputazioni')
    base = SRC.read_text(encoding='utf-8-sig')
    if len(base) < 1_000_000:
        raise SystemExit('Bibbia v54 troppo breve: sospetta fonte incompleta')
    out = [base.rstrip(), '\n\n---\n', '# Appendice canonica v55 — release installer RecordsNext 4.0\n',
           '\nData: 18 novembre 2026. Stato: versione 4.0 operativa, installer pubblico compilabile su Windows.\n',
           '\n**Non considerare concluso il test installer solo perche il codice e presente:** il setup deve essere compilato e collaudato realmente sul PC Windows.\n',
           '\n**Stato identita:** Higuain 8 stagioni verificato. Le altre carriere candidate v98 non sono tutte certificate: nessuna fusione automatica.\n']
    for heading, relative, lang in INCLUDE:
        path = ROOT / relative
        if not path.is_file():
            raise SystemExit('Sorgente mancante: ' + str(path))
        content = path.read_text(encoding='utf-8-sig')
        digest = hashlib.sha256(path.read_bytes()).hexdigest()
        out.append(f'\n## {heading}\n\nSorgente `{relative}` — SHA256 `{digest}`\n\n```{lang}\n{content.rstrip()}\n```\n')
    DST.write_text('\n'.join(out).replace('\n\n\n\n','\n\n\n'), encoding='utf-8')
    print('RN4_V107_BIBBIA_v55_OK', DST, DST.stat().st_size)

if __name__ == '__main__':
    main()
