#!/usr/bin/env python3
"""Non-destructive Bible v50: append verified v93 docs and Git tool to full Bible v49."""
import argparse
import hashlib
from pathlib import Path
import re
import sys

FILES = [
    'docs/CONTINUITA_RECORDSNEXT4_v93.md',
    'docs/CHANGELOG_RECORDSNEXT4_v93.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v93.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v45.md',
    'docs/TEST_E_GIT_RECORDSNEXT4_v93.md',
    'tools/Genera-Bibbia-RecordsNext-v93.py',
    'tools/RN4_GitHub-v93.py',
]


def sha(p):
    return hashlib.sha256(p.read_bytes()).hexdigest()


def generate(root, base, dest):
    if not base.is_file():
        raise FileNotFoundError('Bibbia v49 assente: ' + str(base))
    if dest.exists():
        raise FileExistsError('Bibbia v50 gia presente: non sovrascrivere ' + str(dest))
    sources = []
    for rel in FILES:
        p = root / rel
        if not p.is_file():
            raise FileNotFoundError('Fonte v93 assente: ' + str(p))
        sources.append((rel, p, p.read_text(encoding='utf-8-sig')))
    dest.parent.mkdir(parents=True, exist_ok=True)
    with dest.open('w', encoding='utf-8', newline='\n') as f:
        f.write('# CODICE FUNZIONANTE RECORDSNEXT4 - Bibbia v50\n\n')
        f.write('**Data:** 18/11/2026. Aggiornamento documentale successivo al deploy reale v92.\n\n')
        f.write('La sezione A preserva INTEGRALMENTE la Bibbia v49, senza sostituirne i sorgenti.\n')
        f.write('La sezione B registra il rapporto v92 e le decisioni v93; nessun commit Git e dato per riuscito in anticipo.\n')
        f.write('Il report v92 attesta Maven 67 test e HTTP 5/5. Il browser_real resta formalmente PENDING.\n\n')
        f.write('## A - Bibbia v49 integrale\n\nSHA256 origine: `' + sha(base) + '`\n\n')
        f.write(base.read_text(encoding='utf-8-sig'))
        f.write('\n\n## B - Continuita, decisioni, test e automazione Git v93\n')
        for rel, path, content in sources:
            ext = path.suffix.lower()
            lang = {'.md': 'markdown', '.py': 'python'}.get(ext, 'text')
            fence = '`' * (max([len(t) for t in re.findall(r'`+', content)] or [2]) + 2)
            f.write('\n### `' + rel + '`\n\nSHA256 `' + sha(path) + '`\n\n')
            f.write(fence + lang + '\n' + content.rstrip() + '\n' + fence + '\n')
    print('RN4_V93_BIBBIA_OK', str(dest), dest.stat().st_size, sha(dest))


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--root', type=Path, required=True)
    ap.add_argument('--baseline', type=Path)
    ap.add_argument('--output', type=Path)
    args = ap.parse_args()
    root = args.root.resolve()
    baseline = (args.baseline or root/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v49.md').resolve()
    output = (args.output or root/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v50.md').resolve()
    try:
        generate(root, baseline, output)
    except (OSError, ValueError) as exc:
        print('RN4_V93_BIBBIA_BLOCCATA', str(exc), file=sys.stderr)
        return 3
    return 0


if __name__ == '__main__':
    sys.exit(main())
