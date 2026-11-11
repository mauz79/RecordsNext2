#!/usr/bin/env python3
"""Append-only complete source bible v25 with SHA-256, no overwrite on divergence."""
from __future__ import annotations
import argparse,hashlib
from pathlib import Path

PREVIOUS='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v24.md'
DEST='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v25.md'
FILES=[
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopPublicViewsStageExporter.java',
 'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopPublicViewsStageSmokeTest.java',
 'visualizzatori/js/fcmRecordsNextTopFlopViewsDecoder-v1.js',
 'tools/Validate-RecordsNext4TopFlopPublicViews-v1.py',
 'tools/Test-RecordsNext4TopFlopPublicViewsBrowser-v1.js',
 'tools/Invoke-RecordsNext4TopFlopPublicViews-v1.py',
 'tools/Create-RecordsNext4WorkingCodeMd-v25.py',
 'docs/SPECIFICA_VISTE_PUBBLICHE_TOP_FLOP_v1.md',
 'docs/RecordsNext4_CATALOGO_RECORD_E_VISTE_TOPFLOP_v1.md',
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v26.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v25.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v25.md',
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v27.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v26.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v26.md',
]
def sha(p:Path)->str:return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
 p=argparse.ArgumentParser();p.add_argument('--project-dir',required=True)
 root=Path(p.parse_args().project_dir).resolve()
 for f in [PREVIOUS,*FILES]:
  if not (root/f).is_file():raise RuntimeError('Missing required source: '+f)
 lines=['# RecordsNext 4.0 — Bibbia v25 — Viste pubbliche Top & Flop','',
   'Storico integrale FCA/FCM solo SQLite; HTML e JS pubblici contengono viste interessanti TF01–TF20, non lo storico di tutte le giornate.',
   'Questa versione e STAGING; non incorpora ancora RecordFamily, pipeline, publisher, GUI, CSS ne pubblica sui siti.',
   'Bibbia precedente: `'+PREVIOUS+'` SHA-256: `'+sha(root/PREVIOUS)+'`','']
 for f in FILES:
  dest=root/f;language={'.java':'java','.py':'python','.md':'markdown','.js':'javascript'}.get(dest.suffix,'text')
  lines.extend(['## `'+f+'`','', 'SHA-256: `'+sha(dest)+'`','',
                '~~~~'+language,dest.read_text(encoding='utf-8-sig').rstrip(),'~~~~',''])
 content='\n'.join(lines).rstrip()+'\n'
 target=root/DEST
 if target.exists():
  if target.read_text(encoding='utf-8')!=content:
   raise RuntimeError('Existing bible diverges, never overwrite: '+str(target))
  print('BIBBIA v25: GIA IDENTICA')
 else:
  with target.open('x',encoding='utf-8') as stream:stream.write(content)
  print('BIBBIA v25: OK',target)
if __name__=='__main__':main()
