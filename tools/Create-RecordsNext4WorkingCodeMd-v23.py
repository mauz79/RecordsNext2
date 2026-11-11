#!/usr/bin/env python3
"""Append-only TopFlop code bible v23: exact installed sources and SHA-256."""
from __future__ import annotations
import argparse
import hashlib
from pathlib import Path

PREVIOUS='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v22.md'
DEST='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v23.md'
FILES=[
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopArchive.java',
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopStageJsExporter.java',
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopCanonicalScorecards.java',
 'src/main/java/it/alterlega/recordsnext/app/config/TopFlopExistingJson.java',
 'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopStageContractSmokeTest.java',
 'tools/Test-RecordsNext4TopFlopJs-v1.py',
 'tools/Invoke-RecordsNext4TopFlopJs-v1.py',
 'tools/Create-RecordsNext4WorkingCodeMd-v23.py',
 'docs/SPECIFICA_EXPORT_JS_TOP_FLOP_v1.md',
 'docs/VERIFICA_EXPORT_JS_TOP_FLOP_v1.md',
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v24.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v23.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v23.md'
]

def digest(p):return hashlib.sha256(p.read_bytes()).hexdigest()

def main():
 ap=argparse.ArgumentParser();ap.add_argument('--project-dir',required=True);a=ap.parse_args()
 root=Path(a.project_dir).resolve()
 for rel in [PREVIOUS,*FILES]:
  if not (root/rel).is_file():raise RuntimeError('Required file missing: '+rel)
 lines=['# RecordsNext 4.0 — Bibbia v23 — JS Top & Flop in staging','',
        'Base: RecordsNext 3.2.0. Publisher, GUI, dati e siti operativi invariati.',
        'Bibbia precedente: `'+PREVIOUS+'`',
        'SHA-256 bibbia precedente: `'+digest(root/PREVIOUS)+'`','']
 for rel in FILES:
  p=root/rel
  language={'.java':'java','.py':'python','.md':'markdown'}.get(p.suffix,'text')
  lines.extend(['## `'+rel+'`','','SHA-256: `'+digest(p)+'`','','~~~~'+language,
                p.read_text(encoding='utf-8-sig').rstrip(),'~~~~',''])
 final='\n'.join(lines).rstrip()+'\n'
 target=root/DEST
 if target.exists():
  if target.read_text(encoding='utf-8')!=final:raise RuntimeError('Existing bible v23 differs; no overwrite')
  print('BIBBIA v23: GIA IDENTICA')
 else:
  with target.open('x',encoding='utf-8') as stream:stream.write(final)
  print('BIBBIA v23: OK',target)

if __name__=='__main__':main()
