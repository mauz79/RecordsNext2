#!/usr/bin/env python3
"""Append-only RecordsNext4 code bible v22, exact installed source and SHA-256."""
from pathlib import Path
import argparse,hashlib
PREVIOUS='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v21.md'
DEST='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v22.md'
FILES=[
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopJdbcArchiveAssembler.java',
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopContributionSummary.java',
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopContributionReport.java',
 'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopContributionSmokeTest.java',
 'docs/SPECIFICA_APPORTO_TABELLINO_TOP_FLOP_v1.md',
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v23.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v22.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v22.md',
 'tools/Invoke-RecordsNext4Contributions-v1.py',
 'tools/Create-RecordsNext4WorkingCodeMd-v22.py'
]
def sha(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def main():
 p=argparse.ArgumentParser();p.add_argument('--project-dir',required=True);a=p.parse_args();root=Path(a.project_dir).resolve()
 for f in [PREVIOUS,*FILES]:
  if not (root/f).is_file():raise RuntimeError('Input bibbia mancante: '+f)
 lines=['# RecordsNext 4.0 - Bibbia v22 - Contributi FCM e statistiche individuali','',
        'Base: RecordsNext 3.2.0, worktree Top & Flop. Nessun publisher o database modificato.','',
        'Bibbia precedente: `'+PREVIOUS+'`; SHA-256: `'+sha(root/PREVIOUS)+'`','']
 for f in FILES:
  path=root/f;language={'java':'java','py':'python','md':'markdown'}.get(path.suffix[1:],'text')
  lines.extend(['## `'+f+'`','', 'SHA-256: `'+sha(path)+'`','','~~~~'+language,path.read_text(encoding='utf-8-sig').rstrip(),'~~~~',''])
 contents='\n'.join(lines).rstrip()+'\n'
 output=root/DEST
 if output.exists():
  if output.read_text(encoding='utf-8')!=contents:raise RuntimeError('Bibbia v22 gia esistente diversa, non sovrascrivo')
  print('BIBBIA v22: GIA IDENTICA')
 else:
  with output.open('x',encoding='utf-8') as stream:stream.write(contents)
  print('BIBBIA v22: OK',output)
if __name__=='__main__':main()
