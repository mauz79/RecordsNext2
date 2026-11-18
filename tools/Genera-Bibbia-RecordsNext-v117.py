#!/usr/bin/env python3
"""Append source fingerprints and exact current code to the full Bible v59.

Read-only on src/assets; writes only docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v60.md.
Execute from any directory after consolidating the current sources into Git.
"""
from pathlib import Path
from hashlib import sha256
ROOT=Path(__file__).resolve().parent.parent
DOCS=ROOT/'docs'
BASE=DOCS/'CODICE_FUNZIONANTE_RECORDSNEXT4_v59.md'
OUT=DOCS/'CODICE_FUNZIONANTE_RECORDSNEXT4_v60.md'
FILES=[
'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopPlayerIdentities.java',
'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopHistoricalViews.java',
'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopViewsPublication.java',
'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopPublicViewsStageExporter.java',
'src/main/java/it/alterlega/recordsnext/gui/TopFlopHistoricalCareerDialogV109.java',
'src/main/java/it/alterlega/recordsnext/gui/TopFlopIdentityReviewDialog.java',
'assets/js/fcmRecordsNextTopFlopViewsDecoder-v2.js',
'assets/js/fcmRecordsNextPlayerProfile_v101.js',
'assets/js/fcmRecordsNextCalciatori_v101.js',
'assets/js/fcmRecordsNextTopFlopRankingEngine_v83.js',
'assets/js/fcmRecordsNextFunzioni_topflop_v92.js',
'assets/RecordsNext/calciatori.html',
'assets/RecordsNext/top_flop.html',
'README.md',
'docs/CHANGELOG_RECORDSNEXT4_v117.md',
'docs/CONTINUITA_RECORDSNEXT4_v117.md',
'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v117.md',
]
def main():
    if not BASE.is_file() or BASE.stat().st_size<3_000_000:
        raise SystemExit('Bibbia v59 assente o tronca: rifiuto di generare v60')
    intro='\n\n---\n\n# Appendice v60 - consolidamento sorgenti RecordsNext4 v117\n\n'
    intro+='Questa appendice conserva integralmente la v59 e riporta i file correnti della RC e del repository.\n'
    parts=[BASE.read_text(encoding='utf-8-sig').rstrip(),intro]
    for rel in FILES:
        path=ROOT/rel
        if not path.is_file():raise SystemExit('Sorgente mancante: '+rel)
        data=path.read_bytes()
        txt=data.decode('utf-8-sig')
        label=rel.rsplit('.',1)[-1]
        # Match fenced content without nesting triple-backticks.
        fence='~~~~'
        parts.append('\n## '+rel+'\n\nSHA256: `'+sha256(data).hexdigest()+'`\n\n'+fence+label+'\n'+txt.rstrip()+'\n'+fence+'\n')
    content='\n'.join(parts)+'\n'
    OUT.write_text(content,encoding='utf-8')
    print('RN4_V117_BIBBIA_V60_OK',OUT,OUT.stat().st_size,'byte')
if __name__=='__main__':main()
