#!/usr/bin/env python3
"""Append documented v58 tested changes to v43 Bible, preserving previous versions."""
from pathlib import Path
import hashlib
import sys
root = Path(__file__).resolve().parent.parent
old = root/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v43.md'
new = root/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v44.md'
if not old.is_file() or new.exists():
    raise SystemExit('Bibbia v43 mancante oppure v44 gia presente; non sovrascrivo')
paths = [
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopFcaOfficialFantasy.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopPublicViewsStageExporter.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopHistoricalViews.java',
    'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopOfficialFmtV58SmokeTest.java',
    'src/test/java/it/alterlega/recordsnext/app/model/ExecutionPlannerTest.java',
    'docs/VERIFICA_FANTAMEDIA_REALE_FCA_v58.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v44.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v37.md',
    'tools/Create-RecordsNext4WorkingCodeMd-v44.py',
    'CHANGELOG.md',
]
if any(not (root/p).is_file() for p in paths):
    raise SystemExit('Sorgente/documento v58 mancante: '+str([p for p in paths if not (root/p).is_file()]))
chunks=[old.read_text(encoding='utf-8'), '\n\n# AGGIORNAMENTO SVILUPPO v58b / BIBBIA v44\n\n',
        'Base v43 SHA256: `'+hashlib.sha256(old.read_bytes()).hexdigest()+'`\n\n',
        'ATTENZIONE: patch testata, non release 4.0 certificata finche non passa la pubblicazione sandbox.\n']
for p in paths:
    b=(root/p).read_bytes()
    ext=Path(p).suffix
    lang='java' if ext=='.java' else 'python' if ext=='.py' else 'markdown'
    chunks.extend([f'\n## `{p}`\n\nSHA256: `{hashlib.sha256(b).hexdigest()}`\n\n',
                   f'~~~~~{lang}\n',b.decode('utf-8-sig').rstrip(),'\n~~~~~\n'])
new.write_text(''.join(chunks),encoding='utf-8')
print(f'BIBBIA_V44_OK {new} bytes={new.stat().st_size} sha256={hashlib.sha256(new.read_bytes()).hexdigest()}')
