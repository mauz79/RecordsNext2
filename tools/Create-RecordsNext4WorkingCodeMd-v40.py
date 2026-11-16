#!/usr/bin/env python3
"""Append immutable v48 implementation and source SHAs to bible v39 -> v40.

Never overwrite existing v40; history is an append-only documentation chain.
"""
from pathlib import Path
import hashlib

root = Path(__file__).resolve().parent.parent
old = root / 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v39.md'
new = root / 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v40.md'
source_files = [
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopBenchEvidence.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopPublicViewsStageExporter.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopViewsPublication.java',
    'src/main/java/it/alterlega/recordsnext/app/config/ProcessingConfigWriter.java',
    'src/main/java/it/alterlega/recordsnext/gui/RecordsNext2Dashboard.java',
    'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopBenchEvidenceV48SmokeTest.java',
    'visualizzatori/js/fcmRecordsNextTopFlopViewsDecoder-v2.js',
    'tools/Create-RecordsNext4WorkingCodeMd-v40.py',
    'tools/Test-RecordsNext4ViewsDecoder-v48.js',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v40.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v33.md',
    'docs/VERIFICA_TF11_TF13_PANCHINA_v48.md',
    'README.md',
    'INSTALL.md',
    'CHANGELOG.md',
]
if new.exists():
    raise SystemExit('Bibbia v40 gia esistente: non si sovrascrive')
if not old.is_file():
    raise SystemExit('Bibbia precedente v39 mancante')
parts = [old.read_text(encoding='utf-8'), '\n\n# AGGIORNAMENTO SVILUPPO v48 / BIBBIA v40\n\n',
         'Base v39 SHA-256: `' + hashlib.sha256(old.read_bytes()).hexdigest() + '`\n\n']
for name in source_files:
    f = root / name
    raw = f.read_bytes()
    if name.endswith('.java'): language = 'java'
    elif name.endswith('.js'): language = 'javascript'
    elif name.endswith('.py'): language = 'python'
    else: language = 'markdown'
    parts.append(f'\n## `{name}`\n\nSHA-256: `{hashlib.sha256(raw).hexdigest()}`\n\n~~~~~{language}\n'
                 + raw.decode('utf-8').rstrip() + '\n~~~~~\n')
new.write_text(''.join(parts),encoding='utf-8')
print('Bibbia v40 generata:',new,'byte:',new.stat().st_size)
