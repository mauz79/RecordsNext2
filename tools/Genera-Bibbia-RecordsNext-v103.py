#!/usr/bin/env python3
"""Rebuild/check v54 without replacing the historical full v53 Bible."""
from pathlib import Path
import argparse,hashlib,sys

ENTRIES=(
    ('docs/CONTINUITA_RECORDSNEXT4_v103.md','markdown'),
    ('docs/CHANGELOG_RECORDSNEXT4_v103.md','markdown'),
    ('docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v103.md','markdown'),
    ('docs/TEST_E_GIT_RECORDSNEXT4_v103.md','markdown'),
    ('tools/RN4_Consolida-Git-v103.py','python'),
    ('tools/Genera-Bibbia-RecordsNext-v103.py','python'),
)

def sha(data):return hashlib.sha256(data).hexdigest()

def build(root):
    base=root/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v53.md'
    if not base.is_file():raise RuntimeError('Manca Bibbia v53 completa: '+str(base))
    content=[base.read_text(encoding='utf-8').rstrip(),
             '\n\n---\n\n## Appendice v54: v102 verificata su Windows e consolidamento Git v103\n\n',
             'Stato: v102 installata e verificata su RC e sito locale; Git v103 da confermare con hash remoto.\n']
    for rel,lang in ENTRIES:
        f=root/rel
        if not f.is_file():raise RuntimeError('Manca: '+rel)
        t=f.read_text(encoding='utf-8')
        content.append('\n### `'+rel+'`\n\nSHA256: `'+sha(f.read_bytes())+'`\n\n```'+lang+'\n'+t.rstrip()+'\n```\n')
    return ''.join(content)

def main():
    a=argparse.ArgumentParser();a.add_argument('--root',type=Path,default=Path(__file__).resolve().parents[1]);x=a.parse_args()
    target=x.root/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v54.md'
    value=build(x.root)
    if target.exists():
        if target.read_text(encoding='utf-8')!=value:raise RuntimeError('Bibbia v54 non corrisponde al generatore')
        print('RN4_BIBBIA_V54_VERIFICATA',len(value),'caratteri')
    else:
        target.write_text(value,encoding='utf-8')
        print('RN4_BIBBIA_V54_CREATA',target)
    return 0
if __name__=='__main__':
    try:sys.exit(main())
    except Exception as exc:print('RN4_BIBBIA_V54_BLOCCATA',str(exc),file=sys.stderr);sys.exit(3)
