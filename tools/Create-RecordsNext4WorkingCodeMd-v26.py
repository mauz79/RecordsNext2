#!/usr/bin/env python3
"""Bibbia v26 append-only: tutte le modifiche native e il loro SHA-256."""
from __future__ import annotations
import argparse,hashlib
from pathlib import Path
PREVIOUS='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v25.md'
DEST='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v26.md'
FILES=[
 'src/main/java/it/alterlega/recordsnext/app/model/RecordFamily.java',
 'src/main/java/it/alterlega/recordsnext/app/model/CoreRecordCatalog.java',
 'src/main/java/it/alterlega/recordsnext/app/config/ProcessingConfigWriter.java',
 'src/main/java/it/alterlega/recordsnext/app/config/ProcessingConfigLoader.java',
 'src/main/java/it/alterlega/recordsnext/app/config/MiniJson.java',
 'src/main/java/it/alterlega/recordsnext/gui/RecordsNext2Dashboard.java',
 'src/main/java/it/alterlega/recordsnext/app/RecordsNextPipeline.java',
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopPublicViewsStageExporter.java',
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopViewsPublication.java',
 'src/test/java/it/alterlega/recordsnext/app/model/CoreRecordCatalogCompletenessTest.java',
 'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopNativeIntegrationSmokeTest.java',
 'src/test/java/it/alterlega/recordsnext/app/TopFlopNativePublisherSmokeTest.java',
 'src/test/java/it/alterlega/recordsnext/app/TopFlopGuiPanelSmokeTest.java',
 'tools/Invoke-RecordsNext4NativeGuiPipeline-v2.py',
 'docs/SPECIFICA_INTEGRAZIONE_NATIVA_TOP_FLOP_v1.md',
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v28.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v27.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v27.md',
 'docs/SPECIFICA_METRICHE_TOPFLOP_v1.md',
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v29.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v28.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v28.md',
]
def sha(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def main():
 ap=argparse.ArgumentParser();ap.add_argument('--project-dir',required=True)
 root=Path(ap.parse_args().project_dir).resolve()
 if not (root/PREVIOUS).is_file():raise RuntimeError('Bibbia precedente v25 mancante')
 lines=['# RecordsNext 4.0 — CODICE FUNZIONANTE v26 — GUI e pubblicazione Top & Flop (fix argfile v2)',
   '','Aggiornamento additivo. Nessun publisher parallelo e nessuna alterazione dei dati originali.',
   '','Bibbia precedente: `'+PREVIOUS+'` SHA-256: `'+sha(root/PREVIOUS)+'`','']
 for f in FILES:
  p=root/f
  if not p.is_file():raise RuntimeError('File mancante dalla bibbia: '+f)
  language={'.java':'java','.py':'python','.md':'markdown'}.get(p.suffix,'text')
  lines+=['## `'+f+'`','','SHA-256: `'+sha(p)+'`','',
          '~~~~'+language,p.read_text(encoding='utf-8-sig').rstrip(),'~~~~','']
 content='\n'.join(lines).rstrip()+'\n'
 dest=root/DEST
 if dest.exists():
  if dest.read_text(encoding='utf-8')!=content:raise RuntimeError('Bibbia v26 divergente: non sovrascrivere')
  print('BIBBIA v26: GIA IDENTICA')
 else:
  with dest.open('x',encoding='utf-8') as w:w.write(content)
  print('BIBBIA v26: OK',dest)
if __name__=='__main__':main()
