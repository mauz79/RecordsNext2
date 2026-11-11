#!/usr/bin/env python3
"""Rigenera la bibbia versionata v33, partendo dai file reali del progetto v40."""
from pathlib import Path
root=Path(__file__).resolve().parent.parent
previous=(root/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v32.md').read_text(encoding='utf-8')
diary=(root/'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v33.md').read_text(encoding='utf-8')
paths=[
 'src/main/java/it/alterlega/recordsnext/gui/TopFlopIdentityReviewDialog.java',
 'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopBatchReviewSmokeTest.java'
]
text=previous+'\n\n# AGGIORNAMENTO v33 - 11 novembre 2026\n\n'+diary+'\n'
for name in paths:
 text+='\n## File aggiornato: '+name+'\n\n```java\n'+(root/name).read_text(encoding='utf-8').rstrip()+'\n```\n'
out=root/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v33.md'
out.write_text(text,encoding='utf-8')
print('Bibbia v33:',out,'bytes',out.stat().st_size)
