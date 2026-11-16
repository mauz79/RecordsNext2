#!/usr/bin/env python3
"""Append immutable v47 implementation and source SHAs to bible v38 -> v39.

Never overwrite existing v39; history is an append-only documentation chain.
"""
from pathlib import Path
import hashlib

root = Path(__file__).resolve().parent.parent
old = root / 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v38.md'
new = root / 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v39.md'
source_files = [
    'src/main/java/it/alterlega/recordsnext/app/config/ProcessingConfigWriter.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopPublicViewsStageExporter.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopViewsPublication.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopHistoricalViews.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopHistoricAnomalyWinners.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopEconomicEvidence.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopRealFantasyRules.java',
    'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopHistoricalAnomalyV47SmokeTest.java',
    'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopEconomicV47SmokeTest.java',
    'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopRealFantasyV47SmokeTest.java',
    'visualizzatori/js/fcmRecordsNextTopFlopViewsDecoder-v2.js',
    'tools/Test-RecordsNext4ViewsDecoder-v47.js',
    'tools/Create-RecordsNext4WorkingCodeMd-v39.py',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v39.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v32.md',
    'docs/VERIFICA_METRICHE_TOPFLOP_v47.md',
    'docs/SPECIFICA_CONFIG_REGOLE_REALI_v47.md',
    'README.md', 'INSTALL.md', 'CHANGELOG.md',
]
if new.exists():
    raise SystemExit('Bibbia v39 gia esistente: non si sovrascrive')
if not old.is_file():
    raise SystemExit('Bibbia precedente v38 mancante')
parts = [old.read_text(encoding='utf-8'), '\n\n# AGGIORNAMENTO SVILUPPO v47 / BIBBIA v39\n\n',
         'Base v38 SHA-256: `' + hashlib.sha256(old.read_bytes()).hexdigest() + '`\n\n']
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
print('Bibbia v39 generata:',new,'byte:',new.stat().st_size)
