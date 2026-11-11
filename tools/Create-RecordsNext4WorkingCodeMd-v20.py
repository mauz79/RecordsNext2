#!/usr/bin/env python3
"""Append-only v20 working code bible for the skin-preserving Top & Flop demo installer."""
from __future__ import annotations
import argparse, hashlib, subprocess
from pathlib import Path
PREVIOUS='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v19.md'
DEST='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v20.md'
FILES=[
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v21.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v20.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v20.md',
 'docs/NOTA_COMPATIBILITA_SKIN_TOP_FLOP_v2.md',
 'tools/Invoke-RecordsNext4TopFlopDemo-v2.py',
 'tools/Create-RecordsNext4WorkingCodeMd-v20.py',
]
SKINS=[
 'release/visualizzatori/profiles/mauzstrom/recordsnext.css',
 'release/visualizzatori/profiles/maelstrom/recordsnext.css',
 'release/visualizzatori/profiles/fantablue2/recordsnext.css',
 'release/visualizzatori/profiles/neutral/recordsnext.css',
]
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
 ap=argparse.ArgumentParser();ap.add_argument('--project-dir',required=True,type=Path);args=ap.parse_args()
 root=args.project_dir.resolve(strict=True)
 for n in [PREVIOUS,*FILES,*SKINS]:
  if not (root/n).is_file():raise RuntimeError('File necessario non presente: '+n)
 try:
  branch=subprocess.check_output(['git','-C',str(root),'branch','--show-current'],text=True).strip()
  commit=subprocess.check_output(['git','-C',str(root),'rev-parse','HEAD'],text=True).strip()
 except Exception:branch='NO_GIT';commit='NO_GIT'
 lines=['# RecordsNext 4.0 — Bibbia incrementale v20','',
        '**Installazione conservativa demo Top & Flop, con CSS originali rispettati.**','',
        '- Branch: `'+branch+'`','- Commit: `'+commit+'`',
        '- Documento precedente: `'+PREVIOUS+'`',
        '- SHA-256 precedente: `'+sha(root/PREVIOUS)+'`','',
        '## Codice e decisioni introdotti dalla correzione','']
 for f in FILES:
  p=root/f;ext=p.suffix.lower().lstrip('.')
  lang={'py':'python','md':'markdown'}.get(ext,'text')
  lines.extend(['### `'+f+'`','','SHA-256: `'+sha(p)+'`','','~~~~'+lang,p.read_text(encoding='utf-8-sig').rstrip(),'~~~~',''])
 lines.append('## Hash delle skin effettive, conservate nel worktree')
 lines.append('')
 for f in SKINS: lines.extend(['- `'+f+'`: `'+sha(root/f)+'`'])
 content='\n'.join(lines).rstrip()+'\n'
 dest=root/DEST
 if dest.exists():
  if dest.read_text(encoding='utf-8')!=content:raise RuntimeError('Bibbia v20 esistente con contenuto diverso: '+str(dest))
  print('BIBBIA v20 GIA PRESENTE IDENTICA: '+str(dest))
 else:
  with dest.open('x',encoding='utf-8') as fp:fp.write(content)
  print('BIBBIA v20 GENERATA: '+str(dest))
if __name__=='__main__':main()
