#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""RecordsNext 4.0: Bibbia v45, snapshot continuita' v61. Solo documentazione.
Legge log, impostazioni e sorgenti in sola lettura. Non pubblica e non tocca SQLite.
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

BIBLE_NAME='CODICE_FUNZIONANTE_RECORDSNEXT4_v45.md'
DOCS=[BIBLE_NAME,'DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v45.md',
      'REGISTRO_DECISIONI_RECORDSNEXT4_v38.md','VERIFICA_RILASCIO_MULTISITO_v60.md']
GEN_NAME='Create-RecordsNext4WorkingCodeMd-v45.py'

def sha(p:Path)->str:
    h=hashlib.sha256()
    with p.open('rb') as f:
        for b in iter(lambda:f.read(1048576),b''): h.update(b)
    return h.hexdigest()

def txt(p:Path)->str:
    return p.read_text(encoding='utf-8-sig')

def jsonfile(p:Path):
    try: return json.loads(txt(p))
    except (OSError,UnicodeError,ValueError): return None

def latest(parent:Path,pat:str)->Path|None:
    files=list(parent.glob(pat)) if parent.exists() else []
    return max(files,key=lambda p:(p.stat().st_mtime,p.name)) if files else None

def fmt(p:Path|None)->str:
    return '`'+str(p)+'`' if p is not None else '**NON TROVATO**'

def main():
    ap=argparse.ArgumentParser()
    ap.add_argument('--root',default=r'D:\DEV_APPS\RecordsNext4.0_TEST_v37')
    ap.add_argument('--downloads',default=r'D:\DEV_APPS\downloads')
    args=ap.parse_args()
    root=Path(args.root); down=Path(args.downloads)
    docs=root/'docs'; tools=root/'tools'
    old=docs/'CODICE_FUNZIONANTE_RECORDSNEXT4_v44.md'
    changelog=root/'CHANGELOG.md'
    for f in (old,changelog,root/'config/processing.json',root/'config/recordsnext-gui.properties'):
        if not f.is_file(): raise RuntimeError('Prerequisito mancante: '+str(f))
    for n in DOCS:
        if (docs/n).exists():raise RuntimeError('Esiste gia, non sovrascrivo: '+str(docs/n))
    generator_dest=tools/GEN_NAME
    if generator_dest.exists():raise RuntimeError('Generatore v45 gia presente: '+str(generator_dest))
    code=Path(__file__).resolve()
    proc=jsonfile(root/'config/processing.json')
    pub=proc.get('processing',{}).get('output',{}).get('publishToSite') if isinstance(proc,dict) else 'NON LEGGIBILE'
    props=txt(root/'config/recordsnext-gui.properties')
    prop=(re.search(r'(?m)^processing\.publish\s*=\s*(.+?)\s*$',props) or [])
    guipub=prop.group(1) if prop else 'NON PRESENTE'
    records=[]
    for f in down.glob('RN4_RELEASE_GATE_v59_*.json'):
        j=jsonfile(f)
        if j is not None:records.append((f.stat().st_mtime,f,j))
    records.sort(key=lambda t:(t[0],t[1].name))
    passed=next(((f,j) for _,f,j in reversed(records) if j.get('status')=='STAGING_VERIFICATO_NON_RELEASE'),None)
    blocked=next(((f,j) for _,f,j in reversed(records) if j.get('status')=='BLOCCATO'),None)
    inst=latest(down,'RN4_V58C_INSTALL_ESITO_*.json')
    ij=jsonfile(inst) if inst else None
    jar=root/'target/RecordsNext.jar'
    staging_root=root/'data/site-export-staging'
    candidates=[p/'js' for p in staging_root.glob('records2026_*') if (p/'js').is_dir()] if staging_root.is_dir() else []
    staging=max(candidates,key=lambda p:(p.stat().st_mtime,str(p))) if candidates else None
    js=list(staging.glob('fcmRecordsNext_*.js')) if staging else []
    maxbytes=max((p.stat().st_size for p in js),default=None)
    sandboxstate=root/'data/publish-sandbox-v52/state.json'
    state=jsonfile(sandboxstate)
    state_sections=[]
    if state is not None:
        state_sections=['- Stato sandbox `state.json` presente; SHA256: `'+sha(sandboxstate)+'`.']
        if isinstance(state,dict):
            state_sections.append('- Campi dello stato: `'+', '.join(sorted(state.keys()))+'`.')
    else: state_sections=['- Stato sandbox non trovato / non leggibile.']
    now=dt.datetime.now().astimezone().isoformat(timespec='seconds')
    passed_str=fmt(passed[0]) if passed else '**ASSENTE**'
    blocked_str=fmt(blocked[0]) if blocked else '**ASSENTE**'
    install_str=fmt(inst)
    install_status=ij.get('status','NON LEGGIBILE') if isinstance(ij,dict) else 'NON TROVATO'
    jar_sha=sha(jar) if jar.is_file() else 'NON TROVATO'
    old_sha=sha(old)
    pieces=[
        '# RecordsNext4 - Stato di continuita v61 / verifica rilascio v60\n',
        f'Rilevazione locale: **{now}**. Ambiente TEST: `{root}`.\n',
        '\n## Risultati verificati nei log del 16 novembre 2026\n',
        '- Installazione **v58c**: build Maven, test, smoke FCA e Bibbia v44 riusciti.\n',
        '- Modalita Completa: **87/87** record elaborati, **21** stagioni gestite, **339 JS** generati nello staging; **147 legacy flat**, **183 Top & Flop**.\n',
        '- Limite **1.500.000 byte per JS**, massimo riscontrato **977.154 byte**.\n',
        '- FCA 2012/2013: prima giornata **255** voti, totale **9.936**; import SQLite n. **59**.\n',
        '- Gate **v59 SUPERATO** nel comando `prepare` delle 00:29; **10.492 FMT FCA** verificate.\n',
        '- Sandbox **v60b preparata e isolamento OK** per **21** destinazioni; stagione manuale `2005_2006` esclusa correttamente per assenza di FCM.\n',
        '- Comando `-Mode audit` successivo **BLOCCATO** alle 00:32: `Pubblicazione nel sito ATTIVA! Bloccare e salvare dalla GUI.`\n',
        '- **NON CERTIFICATI**: distribuzione effettiva multisito, audit dei file distribuiti, pagine HTML/skin nei browser, pubblicazione reale, sostituzione della 3.2.\n',
        '- **NON INFERIRE** dal messaggio del gate che siano stati o non siano stati scritti file nella sandbox: il gate ha fermato il proprio audit prima della verifica.\n',
        '\n## Stato locale osservato dal generatore (sola lettura)\n',
        f'- `processing.output.publishToSite`: **`{pub}`**.\n',
        f'- `recordsnext-gui.properties`: `processing.publish={guipub}`.\n',
        f'- Installazione v58c: {install_str} (stato `{install_status}`).\n',
        f'- `target/RecordsNext.jar` SHA256: `{jar_sha}`.\n',
        f'- Ultimo staging: {fmt(staging)}. File JS `fcmRecordsNext_*.js` presenti: **{len(js)}**. Dimensione massima: **{maxbytes}** byte.\n',
        f'- Ultimo gate v59 superato: {passed_str}.\n',
        f'- Ultimo gate v59 bloccato: {blocked_str}.\n',
        *[x+'\n' for x in state_sections],
        '\n## Prossima sessione: percorso di chiusura senza interventi collaterali\n',
        '1. Verificare l’origine del flag di pubblicazione attiva (`processing.json` e GUI). **Non forzare/saltare il gate v59** e non modificare manualmente SQLite. Accertare che i target restino nella sandbox.\n',
        '2. Quando il gate è coerente, eseguire il comando `-Mode audit` v60b per certificare i **JS distribuiti per tutte le 21 stagioni**, gli hash, il cutoff, le dimensioni e i percorsi. Non ripetere l’elaborazione Completa se non necessario.\n',
        '3. Eseguire `-Mode frontend` **solo dopo l’audit positivo**, installare/verificare le skin in sandbox e collaudare nel browser le viste 3.2 e Top & Flop.\n',
        '4. Verificare compatibilita delle 9 facade classiche, filtri competizioni, link tabellini e skin, con confronto rispetto all’output 3.2.\n',
        '5. Solo dopo: piano di rilascio con backup, rollback e passaggio dei JS ai siti reali; poi documentazione conclusiva, commit e push GitHub nel ramo di lavoro **non congelato**.\n',
        '\n## Vincoli invariati\n',
        '- L’output primario è il set di **JS per i siti FCM**; HTML di prova non è fine a sé stesso.\n',
        '- Ogni singolo JS deve restare **≤1.500.000 byte**, controllato sui byte effettivi, caricando dettaglio tramite shard su richiesta.\n',
        '- La GUI governa le selezioni e la pubblicazione. **Nessuna destinazione `E:`** durante i test.\n',
        '- **2005/2006** è manuale/non importabile per assenza FCM; non va artificialmente resa selezionabile.\n',
        '- `D:\\DEV_APPS\\RecordsNext4_PUBLISH_v46` e il ramo GitHub v46 review restano congelati.\n',
        '- Repository: `https://github.com/mauz79/RecordsNext2`; branch noto v46: `feature/recordsnext4-v46-review`. Lo stato del repository locale/push non è stato verificato, **NON dichiarare un commit o un push eseguiti**.\n',
        '- Non modificare FCA/FCM, SQLite, JAR, Java o JS per aggiornare questa documentazione.\n',
        '\n## Script e dati da usare per la ripresa\n',
        '- Launcher: `tools/Start-RecordsNext4-Test-v46.ps1`.\n',
        '- Gate: `D:\\DEV_APPS\\downloads\\RN4_GATE_RC_v59\\Esegui-RN4-ReleaseGate-v59.ps1`.\n',
        '- Sandbox: `D:\\DEV_APPS\\downloads\\RN4_SANDBOX_RELEASE_v60b\\Esegui-RN4-Sandbox-v60b.ps1`, opzioni `prepare`, `verify`, `audit`, `frontend`, `gitplan`.\n',
        '- Rapporti del gate v59 in `D:\\DEV_APPS\\downloads\\RN4_RELEASE_GATE_v59_*.json`.\n',
        '- Bibbia v44 **non viene sovrascritta**; v45 è la base della prossima sessione.\n',
    ]
    verifier=''.join(pieces)
    diary='\n'.join([
        '# RecordsNext 4.0 - Diario v45 (continuita e stop notturno)',
        '',f'Generato: {now}',
        '', '- v58c installata; Maven e smoke FCA superati; 87/87 record elaborati.',
        '- Ultima completa: 339 JS in staging, pubblicazione disabilitata durante la generazione.',
        '- Gate v59 passato e sandbox v60b preparata con isolamento di 21 destinazioni.',
        '- Audit v60b non passato: gate segnala flag `publishToSite` attivo. Stato effettivo dei file distribuiti da verificare.',
        '- Non applicate correzioni al motore, DB, configurazione o pubblicazione durante questo aggiornamento documentale.',
        '- Proseguire con diagnosi flag, audit sandbox, collaudo HTML, promozione controllata, GitHub.',
        '', 'Vedere `VERIFICA_RILASCIO_MULTISITO_v60.md` e Bibbia v45 per percorsi esatti.', '' ])
    decisions='\n'.join([
        '# RecordsNext 4.0 - Registro decisioni v38', '',
        '## DEC-v61-001: nessun rilascio senza certificazione multisito',
        'Gate v59 e isolamento v60b positivi NON sono prova di corretta pubblicazione.',
        'Blocco audit al 2026-11-16 00:32: flag di pubblicazione attivo; approfondire la causa e ripetere il gate nella configurazione corretta, senza bypass.', '',
        '## DEC-v61-002: differenziare la stagione manuale 2005/2006',
        'Non selezionata, senza FCM e senza destinazione: non va inclusa nelle 21 stagioni pubblicabili.', '',
        '## DEC-v61-003: priorita assoluta uscita dalla 3.2',
        'Non aprire nuove analisi FCA o patch al motore senza difetti bloccanti verificati; priorita a collaudo multisito, frontend, compatibilita 3.2, rollback e release.', '',
        '## DEC-v61-004: mantenere integra la documentazione versionata',
        'v44 invariata; creare Bibbia v45 e relativo generatore, diario v45, registro v38, rapporto stato; CHANGELOG con backup.', ''
    ])
    changelog_old=txt(changelog)
    addition=('## RecordsNext 4.0 - v61: continuita rilascio TEST (non pubblicato)\n'
      '- v58c: build, test e 87/87 record completati; 339 JS staging e gate v59 superato.\n'
      '- Sandbox v60b: isolamento 21 destinazioni, 2005/06 esclusa per assenza FCM.\n'
      '- Audit v60b bloccato da flag pubblicazione attivo; distribuzione, skin e promozione 3.2 da certificare.\n'
      '- Documentazione: Bibbia v45, Diario v45, Registro decisioni v38, verifica rilascio v60.\n\n')
    # Snapshot codice: embed dei file di rilascio dal pacchetto installato in Downloads,
    # rendendo la Bibbia autosufficiente anche senza conservare gli ZIP.
    relfiles=[
       down/'RN4_SANDBOX_RELEASE_v60b/Esegui-RN4-Sandbox-v60b.ps1',
       down/'RN4_SANDBOX_RELEASE_v60b/Verifica-RN4-Multisito-v60.py',
       down/'RN4_SANDBOX_RELEASE_v60b/tools/v52/Prepara-Pubblicazione-Test-v52.py',
       down/'RN4_SANDBOX_RELEASE_v60b/tools/v59/Verifica-RN4-ReleaseCandidate-v59.py',
       down/'RN4_GATE_RC_v59/Esegui-RN4-ReleaseGate-v59.ps1',
       root/'config/processing.json',
    ]
    # Non includiamo il processing.json integrale: solo hash e due valori sicurezza.
    chunks=[txt(old).rstrip(),
       '\n\n# AGGIORNAMENTO DI CONTINUITA v61 / BIBBIA v45\n\n',
       f'Generato: `{now}`. SHA256 Bibbia v44: `{old_sha}`.\n\n',
       '**STATO: TEST, NON RELEASE 4.0 CERTIFICATA, nessun push GitHub attestato.**\n\n',
    ]
    for name,data in [
        ('docs/VERIFICA_RILASCIO_MULTISITO_v60.md',verifier),
        ('docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v45.md',diary),
        ('docs/REGISTRO_DECISIONI_RECORDSNEXT4_v38.md',decisions),
        ('CHANGELOG.md (nuova intestazione v61)',addition),
        ]:
        h=hashlib.sha256(data.encode('utf-8')).hexdigest()
        chunks.extend([f'\n## `{name}`\n\nSHA256 sezione: `{h}`\n\n',data.rstrip(),'\n'])
    for f in relfiles:
        if f==root/'config/processing.json':
            chunks.extend(['\n## Configurazione elaborazione (snapshot limitato)\n\n',
                f'SHA256 file: `{sha(f)}`. `publishToSite={pub}`. Contenuto completo **non incluso**.\n'])
            continue
        if not f.is_file():
            chunks.append(f'\n## SCRIPT NON PRESENTE DURANTE SNAPSHOT: `{f}`\n')
            continue
        data=txt(f)
        lang='powershell' if f.suffix.lower()=='.ps1' else 'python'
        chunks.extend([f'\n## `{f}`\n\nSHA256: `{sha(f)}`\n\n',
                       f'~~~~~{lang}\n',data.rstrip(),'\n~~~~~\n'])
    chunks.extend(['\n## `tools/'+GEN_NAME+'`\n\n',
       f'SHA256 sorgente: `{sha(code)}`. Viene conservato sotto `tools` per rigenerazioni/continuita.\n\n',
       '~~~~~python\n',txt(code).rstrip(),'\n~~~~~\n'])
    bible=''.join(chunks)
    datafiles={docs/DOCS[1]:diary+'\n', docs/DOCS[2]:decisions+'\n',docs/DOCS[3]:verifier,
               docs/DOCS[0]:bible, changelog:addition+changelog_old}
    down.mkdir(parents=True,exist_ok=True)
    timestamp=dt.datetime.now().strftime('%Y%m%d_%H%M%S')
    backup=down/('RN4_CHANGELOG_PRE_V45_'+timestamp+'.md')
    if backup.exists():raise RuntimeError('Backup timestamp gia esistente: '+str(backup))
    shutil.copy2(changelog,backup)
    created=[]; changelog_changed=False
    try:
        # Salvataggio con nomi nuovi; nessuna sovrascrittura degli snapshot precedenti.
        for f in [docs/DOCS[1],docs/DOCS[2],docs/DOCS[3],docs/DOCS[0]]:
            with f.open('x',encoding='utf-8',newline='\n') as out:out.write(datafiles[f])
            created.append(f)
        with generator_dest.open('xb') as out:out.write(code.read_bytes())
        created.append(generator_dest)
        changelog.write_text(datafiles[changelog],encoding='utf-8')
        changelog_changed=True
    except Exception:
        if changelog_changed:shutil.copy2(backup,changelog)
        for f in reversed(created):
            if f.exists():f.unlink()
        raise
    print('BIBBIA v45 OK:',docs/DOCS[0]);print('SHA256:',sha(docs/DOCS[0]));print('BYTES:',(docs/DOCS[0]).stat().st_size)
    print('DIARIO v45 + REGISTRO v38 + VERIFICA v60: OK')
    print('GENERATORE v45:',generator_dest)
    print('CHANGELOG aggiornato, backup:',backup)
    print('FLAG PUBBLICAZIONE ATTUALE: processing.json=',pub,'; GUI=',guipub)
    print('GATE v59 PASSATO:',passed_str)
    print('GATE v59 BLOCCATO:',blocked_str)
    print('RISULTATO: SOLO DOCUMENTAZIONE. DB/FCM/FCA/JS/CONFIG/SITI/GIT NON MODIFICATI.')

if __name__=='__main__':
    try:main()
    except Exception as e:
        print('ERRORE DOCUMENTAZIONE:',e,file=sys.stderr)
        sys.exit(2)
