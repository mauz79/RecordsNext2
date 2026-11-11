#!/usr/bin/env python3
"""RecordsNext4 native TopFlop patch, append-only code bible v21."""
from pathlib import Path
import argparse, hashlib, subprocess
PREVIOUS='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v20.md'
DEST='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v21.md'
FILES=[
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopFacts.java',
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopJdbcArchiveAssembler.java',
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopCanonicalRoundResolver.java',
 'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopCanonicalRoundSmokeTest.java',
 'docs/TOP_FLOP_CANONICAL_ROUNDS_PATCH_v1.md',
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v22.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v21.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v21.md',
 'tools/Invoke-RecordsNext4CanonicalRounds-v1.py',
 'tools/Create-RecordsNext4WorkingCodeMd-v21.py'
]
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
 ap=argparse.ArgumentParser(); ap.add_argument('--project-dir',required=True); args=ap.parse_args()
 root=Path(args.project_dir).resolve()
 for n in [PREVIOUS,*FILES]:
  if not (root/n).is_file():raise RuntimeError('Missing bible input: '+n)
 lines=['# RecordsNext 4.0 - Bibbia v21 - round canonico Top & Flop','','Base invariata: RecordsNext 3.2.0. Non si abilita la famiglia Top & Flop alla pubblicazione.','',
  'Previous bible: `'+PREVIOUS+'`', 'Previous SHA-256: `'+sha(root/PREVIOUS)+'`','']
 for n in FILES:
  f=root/n;lang={'java':'java','py':'python','md':'markdown'}.get(f.suffix.lstrip('.'),'text')
  lines+=['## `'+n+'`','', 'SHA-256: `'+sha(f)+'`','','~~~~'+lang,f.read_text(encoding='utf-8-sig').rstrip(),'~~~~','']
 content='\n'.join(lines).rstrip()+'\n'
 dest=root/DEST
 if dest.exists():
  if dest.read_text(encoding='utf-8')!=content:raise RuntimeError('Existing bible v21 differs: '+str(dest))
  print('BIBBIA v21 GIA PRESENTE IDENTICA')
 else:
  with dest.open('x',encoding='utf-8') as h:h.write(content)
  print('BIBBIA v21 GENERATA',dest)
if __name__=='__main__':main()
