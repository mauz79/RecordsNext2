#!/usr/bin/env python3
"""Build versioned RN4 v35 bible from v34 and actual installed sources; never overwrite."""
from pathlib import Path
root=Path(__file__).resolve().parent.parent
base=root/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v34.md'
diario=root/'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v35.md'
paths=[
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopPlayerIdentities.java',
    'src/main/java/it/alterlega/recordsnext/gui/TopFlopIdentityReviewDialog.java',
    'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopResolvedCollisionGuardV43SmokeTest.java',
]
out=root/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v35.md'
if out.exists():
    raise SystemExit('Bibbia v35 gia presente; non sovrascrivo. Usare una nuova versione.')
body=base.read_text(encoding='utf-8')+'\n\n# AGGIORNAMENTO v35 - 11 novembre 2026\n\n'+diario.read_text(encoding='utf-8')+'\n'
for relative in paths:
    body+='\n## File aggiornato: '+relative+'\n\n```java\n'+(root/relative).read_text(encoding='utf-8').rstrip()+'\n```\n'
out.write_text(body,encoding='utf-8')
print('Bibbia v35:',out,'bytes',out.stat().st_size)
