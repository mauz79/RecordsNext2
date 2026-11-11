#!/usr/bin/env python3
"""Additive, conservatively versioned code bible for TopFlop demo v19."""
from __future__ import annotations
import argparse
import hashlib
import subprocess
from pathlib import Path

PREVIOUS='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v18.md'
DEST='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v19.md'
FILES=[
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v20.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v19.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v19.md',
 'docs/SPECIFICA_DEMO_TOP_FLOP_RECORDSNEXT4_v1.md',
 'release/visualizzatori/RecordsNext/top_flop_demo.html',
 'release/visualizzatori/RecordsNext/top_flop_demo.css',
 'release/visualizzatori/js/fcmRecordsNextFunzioni_topflop_v1.js',
 'release/visualizzatori/js/fcmRecordsNext_TopFlop.js',
 'release/visualizzatori/js/fcmRecordsNext_TopFlop.2006_2007.js',
 'release/visualizzatori/js/fcmRecordsNext_TopFlop.2026_2027.js',
 'release/visualizzatori/js/TOPFLOP_PREVIEW_MANIFEST_v1.json',
 'release/visualizzatori/profiles/mauzstrom/recordsnext.css',
 'release/visualizzatori/profiles/maelstrom/recordsnext.css',
 'release/visualizzatori/profiles/fantablue2/recordsnext.css',
 'release/visualizzatori/profiles/neutral/recordsnext.css',
 'tools/Build-RecordsNext4TopFlopDemo-v1.py',
 'tools/Test-RecordsNext4TopFlopFacade-v1.js',
 'tools/Create-RecordsNext4WorkingCodeMd-v19.py',
 'tools/Invoke-RecordsNext4TopFlopDemo-v1.py',
]

def sha(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def main():
 ap=argparse.ArgumentParser();ap.add_argument('--project-dir',type=Path,required=True);args=ap.parse_args()
 root=args.project_dir.resolve(strict=True)
 for item in [PREVIOUS,*FILES]:
  if not (root/item).is_file():raise RuntimeError('File necessario non presente: '+item)
 try:
  branch=subprocess.check_output(['git','-C',str(root),'branch','--show-current'],text=True).strip()
  commit=subprocess.check_output(['git','-C',str(root),'rev-parse','HEAD'],text=True).strip()
 except Exception:branch='NO_GIT';commit='NO_GIT'
 lines=['# RecordsNext 4.0 — Bibbia incrementale v19','',
        '**Ambito: demo storica Top & Flop con dati reali, non un rilascio del publisher.**','',
        '- Branch: `'+branch+'`','- Commit: `'+commit+'`','- Documento precedente: `'+PREVIOUS+'`',
        '- SHA-256 precedente: `'+sha(root/PREVIOUS)+'`','',
        '## File effettivamente presenti: testo integrale e hash', '']
 for f in FILES:
  p=root/f
  ext=p.suffix.lower().lstrip('.')
  lang={'py':'python','md':'markdown','html':'html','css':'css','js':'javascript','json':'json'}.get(ext,'text')
  lines.extend(['### `'+f+'`','','SHA-256: `'+sha(p)+'`','','~~~~'+lang,p.read_text(encoding='utf-8-sig').rstrip(),'~~~~',''])
 content='\n'.join(lines).rstrip()+'\n'
 dest=root/DEST
 if dest.exists():
  if dest.read_text(encoding='utf-8')!=content:raise RuntimeError('Bibbia v19 gia esistente diversa: '+str(dest))
  print('BIBBIA v19 GIA PRESENTE IDENTICA: '+str(dest))
 else:
  with dest.open('x',encoding='utf-8') as o:o.write(content)
  print('BIBBIA v19 GENERATA: '+str(dest))
if __name__=='__main__':main()
