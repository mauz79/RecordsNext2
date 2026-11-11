#!/usr/bin/env python3
"""Generate immutable v38 code bible from installed v37 + v46 changed files."""
from pathlib import Path
import hashlib

root = Path(__file__).resolve().parent.parent
base = root / 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v37.md'
new = root / 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v38.md'
log = root / 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v38.md'
files = [
    'src/main/java/it/alterlega/recordsnext/app/RecordsNextPipeline.java',
    'src/main/java/it/alterlega/recordsnext/app/output/SeasonFamilyShardPublisher.java',
    'src/test/java/it/alterlega/recordsnext/app/RecordsNextFlatStagingV46SmokeTest.java',
    'tools/Start-RecordsNext4-Test-v46.ps1',
    'tools/Publish-RecordsNext4-v46-GitHub.ps1',
    'docs/GITHUB_PUBBLICAZIONE_v46.md',
    'README.md',
    'INSTALL.md',
    'CHANGELOG.md',
]
if new.exists():
    raise SystemExit('La bibbia v38 esiste gia: non sovrascrivo.')
content = [base.read_text(encoding='utf-8'),
           '\n\n# AGGIORNAMENTO SVILUPPO 4.0 - v38 / v46\n',
           log.read_text(encoding='utf-8')]
for relative in files:
    p = root / relative
    raw = p.read_bytes()
    language = 'java' if relative.endswith('.java') else 'powershell' if relative.endswith('.ps1') else 'markdown'
    content.append(f'\n## {relative}\n\nSHA-256: `{hashlib.sha256(raw).hexdigest()}`\n\n```{language}\n'
                   + raw.decode('utf-8').rstrip() + '\n```\n')
new.write_text(''.join(content), encoding='utf-8')
print('Bibbia v38:', new, 'byte:', new.stat().st_size)
