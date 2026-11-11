#!/usr/bin/env python3
"""Append-only continuity bible for the integrated 4.0 patch. Never overwrites divergent files."""
import argparse,hashlib
from pathlib import Path
PREV='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v28.md'
OUT='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v29.md'
FILES=[
 'src/main/java/it/alterlega/recordsnext/gui/RecordsNext2Dashboard.java',
 'src/main/java/it/alterlega/recordsnext/gui/TopFlopIdentityReviewDialog.java',
 'src/main/java/it/alterlega/recordsnext/app/RecordsNextPipeline.java',
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopPublicViewsStageExporter.java',
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopViewsPublication.java',
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopPlayerIdentities.java',
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopHistoricalViews.java',
 'visualizzatori/js/fcmRecordsNextTopFlopViewsDecoder-v1.js',
 'src/test/java/it/alterlega/recordsnext/app/TopFlopGuiPanelSmokeTest.java',
 'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopHistoricIdentitySmokeTest.java',
 'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopHistoricalStagingSmokeTest.java',
 'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopScopedPublicationSmokeTest.java',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v29.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v29.md',
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v29.md'
]
def sha(x):return hashlib.sha256(x.read_bytes()).hexdigest()
def main():
 p=argparse.ArgumentParser();p.add_argument('--project-dir',required=True);a=p.parse_args()
 root=Path(a.project_dir).resolve()
 if not (root/PREV).is_file():raise RuntimeError('Bibbia v28 assente')
 lines=['# RecordsNext 4.0 - CODICE FUNZIONANTE v29','',
  'Appendice cumulativa verificabile alla v28, preservata byte per byte.','',
  f'Bibbia precedente: `{PREV}` SHA256 `{sha(root/PREV)}`','']
 for rel in FILES:
  x=root/rel
  if not x.is_file():raise RuntimeError('Manca: '+rel)
  lang={'.java':'java','.py':'python','.js':'javascript','.md':'markdown'}.get(x.suffix,'text')
  lines.extend([f'## `{rel}`','',f'SHA256: `{sha(x)}`','','~~~~'+lang,
   x.read_text(encoding='utf-8-sig').rstrip(),'~~~~',''])
 content='\n'.join(lines).rstrip()+'\n';dst=root/OUT
 if dst.exists() and dst.read_text(encoding='utf-8')!=content:
  raise RuntimeError('Bibbia v29 divergente; nessuna sovrascrittura')
 if not dst.exists():dst.write_text(content,encoding='utf-8')
 print('BIBBIA v29:',dst,'SHA256:',sha(dst))
if __name__=='__main__':main()
