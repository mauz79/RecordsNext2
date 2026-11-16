#!/usr/bin/env python3
"""Append versioned v49 verified source snapshot to v40 bible (non destructive)."""
from pathlib import Path
import hashlib
root=Path(__file__).resolve().parent.parent
old=root/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v40.md'
new=root/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v41.md'
if new.exists():raise SystemExit('Bibbia v41 gia esistente')
if not old.is_file():raise SystemExit('Bibbia v40 mancante')
files=[
 'src/main/java/it/alterlega/recordsnext/gui/RecordsNext2Dashboard.java',
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopTribunaAuditV49.java',
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopPublicViewsStageExporter.java',
 'src/test/java/it/alterlega/recordsnext/app/TopFlopSettingsGuiV49SmokeTest.java',
 'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopTribunaAuditV49SmokeTest.java',
 'docs/VERIFICA_TRIBUNA_GUI_v49.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v41.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v34.md',
 'tools/Create-RecordsNext4WorkingCodeMd-v41.py',
 'README.md','INSTALL.md','CHANGELOG.md',
]
parts=[old.read_text(encoding='utf-8'),'\n\n# AGGIORNAMENTO SVILUPPO v49 / BIBBIA v41\n\n',
       'Bibbia v40 SHA256: `'+hashlib.sha256(old.read_bytes()).hexdigest()+'`\n']
for relative in files:
 raw=(root/relative).read_bytes()
 language='java' if relative.endswith('.java') else 'python' if relative.endswith('.py') else 'markdown'
 parts.append(f'\n## `{relative}`\n\nSHA256: `{hashlib.sha256(raw).hexdigest()}`\n\n~~~~~{language}\n'+raw.decode('utf-8').rstrip()+'\n~~~~~\n')
new.write_text(''.join(parts),encoding='utf-8')
print('Bibbia v41 generata:',new, 'bytes:',new.stat().st_size)
