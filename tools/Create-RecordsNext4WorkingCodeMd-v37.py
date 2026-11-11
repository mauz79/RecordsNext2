#!/usr/bin/env python3
"""Regenerate the versioned v37 bible from v35 + installed Java code."""
from pathlib import Path
root=Path(__file__).resolve().parent.parent
base=root/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v35.md'
diario=root/'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v37.md'
paths=['src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopHistoricalViews.java', 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopViewsPublication.java', 'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopHistoricalFantasySmokeTest.java', 'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopHistoricalFantasyContractSmokeTest.java']
out=root/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v37.md'
if out.exists(): raise SystemExit('Bibbia v37 esistente: non sovrascrivo. Salvare un nome versionato ulteriore.')
text=base.read_text(encoding='utf8')+'\n\n# AGGIORNAMENTO v37 (patch 4.0 v45)\n\n'+diario.read_text(encoding='utf8')+'\n'
for relative in paths:
    text+='\n## File aggiornato: '+relative+'\n\n```java\n'+(root/relative).read_text(encoding='utf8').rstrip()+'\n```\n'
out.write_text(text,encoding='utf8')
print(out,'bytes',out.stat().st_size)
