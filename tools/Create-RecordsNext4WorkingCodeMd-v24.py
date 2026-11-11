#!/usr/bin/env python3
"""Append-only bible v24: complete compact exporter code with per-file SHA-256."""
from __future__ import annotations
import argparse
import hashlib
from pathlib import Path

PREVIOUS='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v23.md'
DEST='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v24.md'
FILES=[
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopCompactStageJsExporter.java',
 'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopCompactStageSmokeTest.java',
 'visualizzatori/js/fcmRecordsNextTopFlopCompactDecoder-v2.js',
 'tools/Validate-RecordsNext4TopFlopCompact-v2.py',
 'tools/Test-RecordsNext4TopFlopCompactBrowser-v2.js',
 'tools/Invoke-RecordsNext4TopFlopCompact-v2.py',
 'tools/Create-RecordsNext4WorkingCodeMd-v24.py',
 'docs/SPECIFICA_EXPORT_JS_TOP_FLOP_v2.md',
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v25.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v24.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v24.md',
]
def sha(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def main():
    p=argparse.ArgumentParser()
    p.add_argument('--project-dir',required=True)
    args=p.parse_args()
    root=Path(args.project_dir).resolve()
    for name in [PREVIOUS,*FILES]:
        if not (root/name).is_file():raise RuntimeError('Missing installed file: '+name)
    lines=['# RecordsNext 4.0 — Bibbia v24 — Exporter Top & Flop compatto','',
        'Base: RecordsNext 3.2.0. Fonte completa SQLite; JS staging sotto 1.500.000 byte ciascuno.',
        'Pipeline, CSS, GUI, database e siti operativi invariati.',
        'Bibbia precedente: `'+PREVIOUS+'`; SHA-256: `'+sha(root/PREVIOUS)+'`','']
    for name in FILES:
        file=root/name
        lang={'.java':'java','.py':'python','.md':'markdown','.js':'javascript'}.get(file.suffix,'text')
        lines.extend(['## `'+name+'`','', 'SHA-256: `'+sha(file)+'`','',
                      '~~~~'+lang,file.read_text(encoding='utf-8-sig').rstrip(),'~~~~',''])
    output=root/DEST
    body='\n'.join(lines).rstrip()+'\n'
    if output.exists():
        if output.read_text(encoding='utf-8')!=body:
            raise RuntimeError('Existing bible differs, refusing overwrite: '+str(output))
        print('BIBBIA v24: GIA IDENTICA')
    else:
        with output.open('x',encoding='utf-8') as out:out.write(body)
        print('BIBBIA v24: OK',output)
if __name__=='__main__':main()
