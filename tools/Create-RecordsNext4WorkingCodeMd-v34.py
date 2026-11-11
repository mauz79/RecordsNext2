#!/usr/bin/env python3
"""Rigenera la bibbia incrementale v34 a partire dalla v33 e dai sorgenti veri."""
from pathlib import Path
root=Path(__file__).resolve().parent.parent
prev=(root/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v33.md').read_text(encoding='utf-8')
diario=(root/'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v34.md').read_text(encoding='utf-8')
paths=['src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopPlayerIdentities.java','src/main/java/it/alterlega/recordsnext/gui/TopFlopIdentityReviewDialog.java','src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopCrossCodeIdentitySmokeTest.java']
body=prev+'\n\n# AGGIORNAMENTO v34 – 11 novembre 2026\n\n'+diario+'\n'
for name in paths:
 body+='\n## File aggiornato: '+name+'\n\n```java\n'+(root/name).read_text(encoding='utf-8').rstrip()+'\n```\n'
out=root/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v34.md'
if out.exists():raise SystemExit('Bibbia v34 esistente: non sovrascrivo')
out.write_text(body,encoding='utf-8')
print('Bibbia v34:',out,'bytes',out.stat().st_size)
