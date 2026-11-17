#!/usr/bin/env python3
"""Rebuilds complete, versioned RN4 Bible v48 from original canonical v47
and ALL current v83 integration code/docs. No destructive overwrites.
Does not run untrusted scripts and does not remove any previous Bible/report.
"""
from pathlib import Path
import argparse, datetime, hashlib, json, sys

def sha(path):return hashlib.sha256(path.read_bytes()).hexdigest()

def emit(project,baseline,output):
    if not baseline.is_file():raise FileNotFoundError('Bibbia v47 canonica assente: '+str(baseline))
    if output.exists():raise FileExistsError('Versione v48 esistente: preservare, non sovrascrivere: '+str(output))
    snippets=[
        'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopIntegratedV83.java',
        'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopViewsPublication.java',
        'src/main/java/it/alterlega/recordsnext/app/RecordsNextPipeline.java',
        'src/main/java/it/alterlega/recordsnext/gui/RecordsNext2Dashboard.java',
        'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopIntegratedV83Test.java',
        'src/test/java/it/alterlega/recordsnext/app/RecordsNextPipelineV83PublishTest.java',
        'tests/regression_v83.js',
        *('assets/js/'+s for s in (
          'fcmRecordsNextTopFlopRankingEngine_v83.js','fcmRecordsNextEventRanking_v83.js',
          'fcmRecordsNextFunzioni_topflop_v83.js','fcmRecordsNextCalciatori_v83.js',
          'fcmRecordsNextTopFlopViewsDecoder-v2.js', 'fcmRecordsNextPlayerProfile_v79.js',
          'fcmRecordsNextGlobalSearch_v79.js')),
        *('assets/RecordsNext/'+s for s in ('top_flop.html','calciatori.html','top_flop_v83.css')),
        'tools/Genera-Bibbia-RecordsNext-v83.py',
        'tools/Verifica-RN4-Integrazione-v83.py',
        'tools/Oracolo-FCM-v79-readonly.py',
        *('docs/'+s for s in (
          'RECORDSNEXT4_INTEGRAZIONE_NATIVA_v83.md',
          'DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v83.md',
          'REGISTRO_DECISIONI_RECORDSNEXT4_v43.md',
          'CHANGELOG_RECORDSNEXT4_v83.md',
          'CATALOGO_VERIFICHE_RECORDSNEXT4_v83.md',
          'TEST_E_GIT_RECORDSNEXT4_v83.md')),
    ]
    missing=[s for s in snippets if not (project/s).is_file()]
    if missing:raise FileNotFoundError('File di integrazione assenti: '+', '.join(missing))
    output.parent.mkdir(parents=True,exist_ok=True)
    with output.open('w',encoding='utf-8',newline='\n') as w:
        w.write('# CODICE FUNZIONANTE RECORDSNEXT4 — Bibbia canonica v48 (integrazione v83)\n\n')
        w.write('Stato: **candidato compilato e testato su fixture, NON CERTIFICATO sui 21 RAW FCM/classici/serie/RU/Culometro**.\n\n')
        w.write('La sezione A conserva byte-per-contenuto l’intera Bibbia v47 della RC v81.\n')
        w.write('La sezione B contiene i FILE V83 COMPLETI che prevalgono sulle copie precedenti della v47.\n')
        w.write('La verifica del DB Windows e Git restano gate separati.\n\n')
        w.write('## A — Bibbia canonica storica v47, integra\n\n')
        w.write('Origine SHA256 `'+sha(baseline)+'`. I passaggi di codice sostituiti dalla sezione B non sono piu autoritativi.\n\n')
        w.write(baseline.read_text(encoding='utf-8-sig'))
        w.write('\n\n## B — Codice nativo v83 e documentazione completa, autorevoli\n\n')
        for relative in snippets:
            file=project/relative
            ext=file.suffix.lower()
            lang={'.java':'java','.js':'javascript','.py':'python','.html':'html','.css':'css','.md':'markdown'}.get(ext,'text')
            content=file.read_text(encoding='utf-8-sig')
            fence='`'*(max((len(x) for x in __import__('re').findall(r'`+',content)),default=2)+2)
            w.write('\n### `'+relative+'`\n\nSHA256 `'+sha(file)+'`; bytes '+str(file.stat().st_size)+'\n\n')
            w.write(fence+lang+'\n'+content.rstrip()+'\n'+fence+'\n')
    return {'path':str(output),'bytes':output.stat().st_size,'sha256':sha(output),'sources':len(snippets)}

if __name__=='__main__':
    a=argparse.ArgumentParser()
    a.add_argument('--root',type=Path,required=True)
    a.add_argument('--baseline',type=Path,default=None)
    a.add_argument('--output',type=Path,default=None)
    args=a.parse_args()
    p=args.root.resolve();baseline=(args.baseline or p/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v47.md').resolve()
    output=(args.output or p/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v48.md').resolve()
    try:print('RN4_V83_BIBBIA_GENERATA',json.dumps(emit(p,baseline,output),ensure_ascii=False))
    except Exception as ex:print('RN4_V83_BIBBIA_BLOCCATA',ex,file=sys.stderr);sys.exit(2)
