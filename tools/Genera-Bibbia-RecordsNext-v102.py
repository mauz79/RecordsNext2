#!/usr/bin/env python3
from pathlib import Path
import argparse,hashlib,sys

def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()

def build(root):
    ref=root/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v51.md'
    if not ref.is_file():raise RuntimeError('Manca Bibbia v51 completa: '+str(ref))
    base=ref.read_text(encoding='utf-8')
    entries=[
      ('src/main/java/it/alterlega/recordsnext/app/RecordsNextPipeline.java','java'),
      ('assets/RecordsNext/calciatori.html','html'),
      ('assets/js/fcmRecordsNextPlayerProfile_v101.js','javascript'),
      ('assets/js/fcmRecordsNextCalciatori_v101.js','javascript'),
      ('docs/CHANGELOG_RECORDSNEXT4_v102.md','markdown'),
      ('docs/CONTINUITA_RECORDSNEXT4_v102.md','markdown'),
      ('docs/TEST_E_GIT_RECORDSNEXT4_v102.md','markdown'),
    ]
    sections=[base.rstrip(),'\n\n---\n\n## Appendice v53: correzione publisher v102 (baseline v92)\n',
      'Stato: candidato testato localmente; installazione Windows e Git da certificare.\n']
    for rel,language in entries:
        p=root/rel
        if not p.is_file():raise RuntimeError('File canonico mancante '+str(p))
        content=p.read_text(encoding='utf-8')
        sections.append('\n### `'+rel+'`\n\nSHA256: `'+sha(p)+'`\n\n```'+language+'\n'+content.rstrip()+'\n```\n')
    sections.append('\nGeneratore: `tools/Genera-Bibbia-RecordsNext-v102.py`.\n')
    return ''.join(sections)

def main():
    a=argparse.ArgumentParser();a.add_argument('--root',type=Path,default=Path(__file__).resolve().parents[1]);x=a.parse_args()
    t=build(x.root); p=x.root/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v53.md'
    if p.exists():
        if p.read_text(encoding='utf-8')!=t:raise RuntimeError('Bibbia v53 diverge')
        print('RN4_BIBBIA_V53_VERIFICATA',len(t),'caratteri')
    else:
        p.write_text(t,encoding='utf-8');print('RN4_BIBBIA_V53_CREATA',p)
    return 0
if __name__=='__main__':
    try:sys.exit(main())
    except Exception as e:print('RN4_BIBBIA_V53_BLOCCATA',str(e),file=sys.stderr);sys.exit(3)
