#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""RecordsNext 4.0 TEST: documentazione v46 post audit sandbox/front-end v62b.
Non modifica dati o software. Non dichiara superato il collaudo visivo.
"""
from __future__ import annotations
import argparse
import datetime as dt
import hashlib
import json
from pathlib import Path
import re
import shutil
import sys

DOC_BIB='CODICE_FUNZIONANTE_RECORDSNEXT4_v46.md'
DOC_DIA='DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v46.md'
DOC_DEC='REGISTRO_DECISIONI_RECORDSNEXT4_v39.md'
DOC_VER='VERIFICA_RILASCIO_MULTISITO_v63.md'
SELF_NAME='Create-RecordsNext4WorkingCodeMd-v46.py'

def sha(p:Path)->str:
    h=hashlib.sha256()
    with p.open('rb') as f:
        for chunk in iter(lambda:f.read(1048576),b''): h.update(chunk)
    return h.hexdigest()

def get_text(p:Path)->str:
    return p.read_text(encoding='utf-8-sig')

def jfile(p:Path):
    return json.loads(get_text(p))

def req(cond,msg):
    if not cond: raise RuntimeError(msg)

def latest_good(downloads):
    files=sorted(downloads.glob('RN4_GATE_POSTPUB_v62_*.json'),key=lambda p:(p.stat().st_mtime,p.name),reverse=True)
    for p in files:
        try:
            j=jfile(p)
            if j.get('status')=='AUDIT_SANDBOX_OK_NON_RELEASE' and j.get('selected_sites')==21 and j.get('total_js')==3730 and j.get('shard_manifest_matches')==1617 and j.get('fca_2012_votes')==9936:
                return p,j
        except Exception:
            continue
    raise RuntimeError('Nessun report v62 valido: attesi 21 siti, 3730 JS, 1617 shard, 9936 voti FCA.')

def verify_ui(root:Path, down:Path):
    pkg=down/'RN4_FINISH_SANDBOX_v62b/tools/v53b'
    manifest=pkg/'MANIFEST_SHA256.json'
    req(manifest.is_file(),'Manifest v53b non trovato: '+str(manifest))
    package=jfile(manifest)
    expected={f['path']:(f['sha256'].lower(),f['size']) for f in package['files']}
    for name in ('RecordsNext/top_flop.html','RecordsNext/top_flop_v53.css','js/fcmRecordsNextFunzioni_topflop_v53.js','js/fcmRecordsNextFunzioni_topflop_index_v53.js','js/fcmRecordsNextTopFlopViewsDecoder-v2.js'):
        req(name in expected,'Manifest skin incompleto: '+name)
        src=pkg/name
        req(src.is_file() and sha(src)==expected[name][0], 'File sorgente skin non valido: '+str(src))
    sites=root/'data/publish-sandbox-v52/sites'
    observations=[]
    for season in ('2025_2026','2026_2027'):
        site=sites/season
        req(site.resolve().is_relative_to(sites.resolve()),'Destinazione fuori sandbox: '+season)
        for name in ('RecordsNext/top_flop.html','RecordsNext/top_flop_v53.css','js/fcmRecordsNextFunzioni_topflop_v53.js','js/fcmRecordsNextFunzioni_topflop_index_v53.js','js/fcmRecordsNextTopFlopViewsDecoder-v2.js'):
            p=site/name
            req(p.is_file(),'Frontend mancante: '+str(p))
            req(sha(p)==expected[name][0], 'Frontend differente dal manifest: '+str(p))
        index=site/'recordsnext.html'
        req(index.is_file(),'Indice RecordsNext assente: '+str(index))
        html=get_text(index)
        req(bool(re.search(r'data-family-card\s*=\s*[\"\']topFlop[\"\']',html,re.I)),'Card TopFlop assente: '+str(index))
        req('fcmRecordsNextFunzioni_topflop_index_v53.js' in html,'Bridge TopFlop assente: '+str(index))
        observations.append((season,sha(site/'RecordsNext/top_flop.html'),sha(index)))
    return observations

def main():
    ap=argparse.ArgumentParser()
    ap.add_argument('--root',default=r'D:\DEV_APPS\RecordsNext4.0_TEST_v37')
    ap.add_argument('--downloads',default=r'D:\DEV_APPS\downloads')
    a=ap.parse_args()
    root=Path(a.root); down=Path(a.downloads); docs=root/'docs'; tools=root/'tools'
    old=docs/'CODICE_FUNZIONANTE_RECORDSNEXT4_v45.md'
    changelog=root/'CHANGELOG.md'
    newfiles=[docs/DOC_BIB,docs/DOC_DIA,docs/DOC_DEC,docs/DOC_VER,tools/SELF_NAME,tools/'Esegui-RN4-Documentazione-Github-v63.ps1']
    req(root.is_dir() and 'TEST' in root.name.upper(),'Ambiente NON TEST.')
    req(old.is_file(),'Bibbia v45 assente, eseguire prima il generatore v45.')
    req(changelog.is_file(),'CHANGELOG mancante.')
    req(all(not f.exists() for f in newfiles),'File v46/v39/v63 gia presente: non sovrascrivo. '+repr([str(f) for f in newfiles if f.exists()]))
    rep_path,rep=latest_good(down)
    skin=verify_ui(root,down)
    state=root/'data/publish-sandbox-v52/state.json'
    req(state.is_file(),'Manifest sandbox mancante')
    # Rileva il fatto che l'ambiente e stato verificato, senza rimuovere protezioni.
    manifest=jfile(state)
    req(manifest.get('schema')=='rn4-publish-sandbox-v52' and len(manifest.get('selected',[]))==21,
        'Manifest sandbox inatteso')
    process=root/'config/processing.json'
    req(process.is_file(),'Config elaborazione assente')
    flag=jfile(process).get('processing',{}).get('output',{}).get('publishToSite')
    req(type(flag) is bool,'Flag publishToSite non interpretabile')
    prop=root/'config/recordsnext-gui.properties'
    req(prop.is_file(),'Config GUI assente')
    matches=re.findall(r'^processing\.publish\s*=\s*(.*)$',get_text(prop),flags=re.M)
    guiflag=matches[-1].strip() if matches else 'NON PRESENTE'
    now=dt.datetime.now().astimezone().isoformat(timespec='seconds')
    reporttext=f'''# RecordsNext 4.0 - Verifica rilascio multisito v63

Data verifica: {now}. Ambiente TEST: `{root}`.

## Evidenze superate

- Installazione **v58c**: Maven e smoke FCA superati nel TEST il 16/11/2026.
- Elaborazione Completa: **87/87** record; **21** stagioni importabili. **339 JS** nello staging, massimo **977.154 byte**.
- FCA 2012/2013: **9.936 voti**, inclusi i **255** della prima giornata recuperata.
- Gate v59: **9 facade compatibili con la 3.2**, copertura **21 stagioni**, **10.492 FMT ufficiali FCA** controllate.
- Gate post-pubblicazione v62: **21 siti**, **3.730 JS**, **1.617 shard**, cutoff storico per ogni sito, manifest/hash collegati all'ultimo staging. Esito **`AUDIT_SANDBOX_OK_NON_RELEASE`**.
- Stagione `2005_2006` manuale e priva di FCM: esclusa dai 21 siti pubblicabili.
- Skin v53b: integrata nelle **copie sandbox 2025/26 e 2026/27**, CSS e bridge JS verificati contro manifest SHA-256, card Top & Flop presente nell'indice.
- Siti operativi `E:` e branch congelato v46: **non modificati dagli audit/installazioni TEST documentati**.

## Rapporti, percorsi e SHA-256

- Gate post-pubblicazione: `{rep_path}` SHA256 `{sha(rep_path)}`.
- Manifest sandbox: `{state}` SHA256 `{sha(state)}`.
- Config elaborazione: SHA256 `{sha(process)}`; `processing.output.publishToSite={flag}`.
- Config GUI: SHA256 `{sha(prop)}`; `processing.publish={guiflag}`.
'''
    for season, pagehash,indexhash in skin:
        reporttext+=f'- `{season}`: `RecordsNext/top_flop.html` SHA256 `{pagehash}`, `recordsnext.html` SHA256 `{indexhash}`.\n'
    reporttext+='''
## Limiti espliciti / cose ancora aperte

- **Non certificato nel browser**: resa visiva, interazioni, filtri per competizione, link tabellini, comportamento delle 4 skin e compatibilita' reale con la 3.2. L'audit dei file e' statico.
- **Non eseguita la pubblicazione reale** sui siti `E:`. Prima serve prova visiva e backup/rollback operativo.
- **Attenzione**: `processing.json` mantiene `publishToSite=true` nel log v62, mentre la GUI aveva `processing.publish=false`. Riportare il flag a `false` tramite GUI/configurazione corretta prima di una nuova elaborazione, dopo il collaudo sandbox e verificando le destinazioni.
- Il push GitHub v63, se eseguito, e' un **checkpoint di release candidate**, non una certificazione di rilascio production; GitHub resta da attestare separatamente con il risultato del comando push.

## Sequenza minima per finire

1. Aprire nel browser i due siti sandbox `2026_2027` e `2025_2026` (indice `recordsnext.html`, pagina `RecordsNext/top_flop.html`), controllare caricamento JS, rendering, filtri, link, console browser e almeno due skin. Non usare il sito `E:`.
2. Allineare/chiudere la flag di pubblicazione, mantenendo la sandbox isolata finche' non si promuove la release.
3. Eseguire confronto visualizzatori e compatibilita' output 3.2, backup degli asset operativi e test di ripristino.
4. Aggiornare il checkpoint GitHub e promuovere in modo controllato. **Non dichiarare conclusa la sostituzione 3.2 finche' il passaggio reale non e' avvenuto.**
'''
    diary=f'''# RecordsNext 4.0 - Diario v46\n\n- {now}: v58c confermata; 87/87 record, 339 JS staging.\n- Gate post-pub v62b: 21 siti, 3730 JS, 1617 shard, audit dati e cutoff passati.\n- Skin v53b: integrata/verificata staticamente in sandbox per 2025/26 e 2026/27.\n- Non ancora passati: browser, pubblicazione reale e sostituzione 3.2.\n- GitHub: preparato checkpoint dedicato v63, lo stato di push e' da verificare separatamente.\n- Flag processing.json publishToSite={flag}; GUI processing.publish={guiflag}.\n'''
    decisions=f'''# RecordsNext 4.0 - Registro decisioni v39\n\n## DEC-v63-001 - certificazione sandbox separata dalla produzione\nIl gate v62 `AUDIT_SANDBOX_OK_NON_RELEASE` certifica 21 siti, 3730 JS e 1617 shard, non il rendering nel browser o la pubblicazione E:.\n\n## DEC-v63-002 - integrita' dati e stagioni\nConfermati 9936 voti FCA 2012/13 e 255 G1; 2005/06 rimane manuale, non elaborabile in assenza di FCM.\n\n## DEC-v63-003 - limite JS e prestazioni\nVincolo rigido 1.500.000 byte per singolo JS. 339 file staging con massimo 977154 byte.\n\n## DEC-v63-004 - GitHub senza alterare v46\nCheckpoint su branch `feature/recordsnext4-v63-sandbox-validated`, basato sul branch v46 ma con sorgenti e documentazione correnti; non scrivere su `feature/recordsnext4-v46-review` e non fare tag di release definitiva prima del collaudo browser e della promozione reale.\n\n## DEC-v63-005 - safety dei prossimi comandi\nNon avviare una nuova Completa finche' `processing.output.publishToSite` e le destinazioni non sono state ricontrollate.\n'''
    change=f'''## RecordsNext 4.0 - v63: sandbox multisito certificata, frontend statico integrato (RC, NON release)\n- 21 siti in sandbox con 3730 JS e 1617 shard verificati; 339 JS staging, cutoff storico e vincolo 1.500.000 byte OK.\n- Skin v53b integrata e verificata staticamente per 2025/26 e 2026/27.\n- Pubblicazione reale, interazioni browser e sostituzione 3.2 ancora da certificare.\n- Documentazione versionata: Bibbia v46, Diario v46, Registro decisioni v39, rapporto v63.\n\n'''
    generator=Path(__file__).resolve()
    # Appendice alla Bibbia precedente. Mantiene integralmente i blocchi di codice preesistenti.
    appendix='\n\n# AGGIORNAMENTO v63 / BIBBIA v46 - COLLAUDO MULTISITO E FRONTEND\n\n'
    appendix+=f'Generato `{now}`. Bibbia v45 SHA256 `{sha(old)}`. Stato RC, NON produzione.\n\n'
    appendix+='## '+DOC_VER+'\n\n'+reporttext+'\n'
    appendix+='## '+DOC_DIA+'\n\n'+diary+'\n'
    appendix+='## '+DOC_DEC+'\n\n'+decisions+'\n'
    script_sources=[
        Path(__file__).resolve().parent/'Esegui-RN4-Documentazione-Github-v63.ps1',
        down/'RN4_FINISH_SANDBOX_v62b/Verifica-RN4-PostPubblicazione-v62.py',
        down/'RN4_FINISH_SANDBOX_v62b/Esegui-RN4-FinishSandbox-v62.ps1',
        down/'RN4_FINISH_SANDBOX_v62b/tools/v53b/tools/Installa-TopFlop-Skin-v53b.ps1',
        down/'RN4_FINISH_SANDBOX_v62b/tools/v53b/js/fcmRecordsNextFunzioni_topflop_v53.js',
        down/'RN4_FINISH_SANDBOX_v62b/tools/v53b/js/fcmRecordsNextFunzioni_topflop_index_v53.js',
        down/'RN4_FINISH_SANDBOX_v62b/tools/v53b/js/fcmRecordsNextTopFlopViewsDecoder-v2.js',
    ]
    for p in script_sources:
        req(p.is_file(),'Script v62b non trovato: '+str(p))
        lang={'.ps1':'powershell','.py':'python','.js':'javascript'}.get(p.suffix,'text')
        appendix+=f'\n## Sorgente `{p}`\n\nSHA256 `{sha(p)}`\n\n~~~~~{lang}\n{get_text(p).rstrip()}\n~~~~~\n'
    appendix+=f'\n## Generatore `tools/{SELF_NAME}`\n\nSHA256 `{sha(generator)}`\n\n~~~~~python\n{get_text(generator).rstrip()}\n~~~~~\n'
    bibl=get_text(old).rstrip()+appendix
    timestamp=dt.datetime.now().strftime('%Y%m%d_%H%M%S')
    down.mkdir(parents=True,exist_ok=True)
    changelog_backup=down/f'RN4_CHANGELOG_PRE_BIBBIA_V46_{timestamp}.md'
    req(not changelog_backup.exists(),'Backup CHANGELOG esistente')
    shutil.copy2(changelog,changelog_backup)
    pending={docs/DOC_BIB:bibl,docs/DOC_DIA:diary,docs/DOC_DEC:decisions,docs/DOC_VER:reporttext}
    created=[]
    try:
        for p,data in pending.items():
            with p.open('x',encoding='utf-8',newline='\n') as f:f.write(data)
            created.append(p)
        with (tools/SELF_NAME).open('xb') as f:f.write(generator.read_bytes())
        created.append(tools/SELF_NAME)
        runner_source=generator.parent/'Esegui-RN4-Documentazione-Github-v63.ps1'
        req(runner_source.is_file(),'Runner PowerShell v63 assente dal pacchetto')
        with (tools/runner_source.name).open('xb') as f:f.write(runner_source.read_bytes())
        created.append(tools/runner_source.name)
        changelog.write_text(change+get_text(changelog),encoding='utf-8')
    except Exception:
        shutil.copy2(changelog_backup,changelog)
        for p in created:
            if p.exists():p.unlink()
        raise
    snap={'status':'DOCUMENTAZIONE_V46_OK_RC_NON_RELEASE','created_at':now,
          'bible_path':str(docs/DOC_BIB),'bible_sha256':sha(docs/DOC_BIB),
          'audit_report':str(rep_path),'audit_sha256':sha(rep_path),
          'selected_sites':21,'js_published_sandbox':3730,'shards_verified':1617,
          'browser_verified':False,'production_published':False,
          'frontend_sandbox_verified_seasons':[x[0] for x in skin],
          'processing_publish_to_site':flag,'gui_publish':guiflag}
    p=down/f'RN4_DOC_V46_ESITO_{timestamp}.json'
    p.write_text(json.dumps(snap,ensure_ascii=False,indent=2),encoding='utf-8')
    print('BIBBIA v46 OK:',docs/DOC_BIB)
    print('SHA256:',snap['bible_sha256'],'bytes:',(docs/DOC_BIB).stat().st_size)
    print('DIARIO v46, REGISTRO v39, VERIFICA v63, CHANGELOG: OK')
    print('GATE POSTPUB:',str(rep_path))
    print('STATO: 21 siti/3730 JS/1617 shard, skin statica 2 siti. BROWSER e RELEASE E: PENDENTI.')
    print('FLAG publishToSite:',flag,'GUI:',guiflag)
    print('ESITO:',p)
    print('NESSUNA MODIFICA A DB/FCA/FCM/JAVA/JS/SITI.')

if __name__=='__main__':
    try:main()
    except Exception as e:
        print('ERRORE v63 DOC:',str(e),file=sys.stderr)
        sys.exit(2)
