#!/usr/bin/env python3
"""Generate v51 by appending v94 sources and reports to full unchanged v50."""
import argparse
import hashlib
from pathlib import Path
import re

FILES = [
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopFullPerformancesV69.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopSimeoneIdentityV73.java',
    'docs/CONTINUITA_RECORDSNEXT4_v94.md',
    'docs/CHANGELOG_RECORDSNEXT4_v94.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v94.md',
    'docs/TEST_E_GIT_RECORDSNEXT4_v94.md',
    'tools/Genera-Bibbia-RecordsNext-v94.py',
    'tools/RN4_Riprendi-Git-v94.py',
]

def sha(p):
    return hashlib.sha256(p.read_bytes()).hexdigest()


def generate(root, baseline, output):
    if not baseline.is_file():
        raise FileNotFoundError('Bibbia v50 mancante: ' + str(baseline))
    if output.exists():
        raise FileExistsError('Non sovrascrivere Bibbia v51: ' + str(output))
    text = baseline.read_text(encoding='utf-8-sig')
    sources = [(rel, root / rel) for rel in FILES]
    for rel, path in sources:
        if not path.is_file():
            raise FileNotFoundError('Fonte mancante per Bibbia v51: ' + str(path))
    output.parent.mkdir(parents=True, exist_ok=True)
    with output.open('w', encoding='utf-8', newline='\n') as out:
        out.write('# CODICE FUNZIONANTE RECORDSNEXT4 - Bibbia v51\n\n')
        out.write('**Data:** 18 novembre 2026. Ripresa del gate Git v93 bloccato da sorgenti omessi.\n\n')
        out.write('La sezione A conserva integralmente la Bibbia v50. La sezione B aggiunge i due sorgenti Java mancanti, documentazione e script v94. Il commit/push va verificato dai report Windows, non e dichiarato a priori.\n\n')
        out.write('## A - Bibbia v50 integrale\n\nSHA256 origine: `' + sha(baseline) + '`\n\n')
        out.write(text)
        out.write('\n\n## B - Sorgenti e continuita v94\n')
        for rel, path in sources:
            content = path.read_text(encoding='utf-8-sig').rstrip()
            fence = '`' * (max([len(x) for x in re.findall(r'`+', content)] or [2]) + 2)
            language = 'java' if path.suffix == '.java' else ('python' if path.suffix == '.py' else 'markdown')
            out.write('\n### `' + rel + '`\n\nSHA256 `' + sha(path) + '`\n\n')
            out.write(fence + language + '\n' + content + '\n' + fence + '\n')
    return sha(output)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--root', type=Path, required=True)
    parser.add_argument('--baseline', type=Path, required=True)
    parser.add_argument('--output', type=Path, required=True)
    args = parser.parse_args()
    print('RN4_V94_BIBBIA_OK', args.output, generate(args.root, args.baseline, args.output))


if __name__ == '__main__':
    main()
