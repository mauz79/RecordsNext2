#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Consolidamento v67: genera Bibbia v47 e continuita' GUI/migrazione da RC v64.

Solo testo/documenti, mai dati operativi. I file di nuova versione NON si sovrascrivono.
"""
from __future__ import annotations
import argparse
from contextlib import closing
from datetime import datetime
import hashlib
import json
from pathlib import Path
import re
import shutil
import sqlite3
import sys

ROOT=Path(r'D:\DEV_APPS\RecordsNext4.0_RC_v64')
DOWN=Path(r'D:\DEV_APPS\downloads')
FCA=Path(r'E:\FCM\data\ArchivioA2012SerieAmauz_G1_RECUPERATA_v1.fca')
FCA_SHA='e3c34b941aedb35951ba030701fe47072afa00941f7fa7884d0658442cfcf766'
SHA_V63_GIT='3b008a8'
GUI_DASH=Path('src/main/java/it/alterlega/recordsnext/gui/RecordsNext2Dashboard.java')
GUI_CONFIG=Path('src/main/java/it/alterlega/recordsnext/gui/RecordsNextConfigurationDialog.java')
BIB46='CODICE_FUNZIONANTE_RECORDSNEXT4_v46.md'
DOCS=['CODICE_FUNZIONANTE_RECORDSNEXT4_v47.md',
      'DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v47.md',
      'REGISTRO_DECISIONI_RECORDSNEXT4_v40.md',
      'STATO_MIGRAZIONE_GUI_RELEASE_v67.md']
TITLE='RecordsNext 4.0 - consolidamento v67 (RC, non produzione)'

def req(ok,msg):
    if not ok: raise RuntimeError('STOP: '+msg)

def sha(p):
    h=hashlib.sha256()
    with p.open('rb') as f:
        for c in iter(lambda:f.read(1048576),b''): h.update(c)
    return h.hexdigest()

def content(p): return p.read_text(encoding='utf-8-sig')

def jsonfile(p): return json.loads(content(p))

def suffix_update(path,header,body):
    original=content(path)
    req(header not in original,'sezione gia presente in '+str(path))
    return original.rstrip()+'\n\n'+header+'\n'+body.rstrip()+'\n'

def verify(root,down,fcafile):
    req(root.name=='RecordsNext4.0_RC_v64','ambiente diverso dalla RC v64')
    req(root.is_dir(),'RC non trovata')
    req((root/'data/database/recordsnext.db').is_file(),'SQLite RC mancante')
    req((root/'target/RecordsNext.jar').is_file(),'JAR RC mancante')
    req((root/'docs'/BIB46).is_file(),'Bibbia v46 RC mancante')
    req((root/'pom.xml').is_file(),'pom.xml RC mancante')
    req((root/'config/player-identities.json').is_file(),'identita giocatori mancanti')
    req((root/'config/topflop.json').is_file(),'TopFlop config mancante')
    req((root/'REPORT_MIGRAZIONE_RC_v64.json').is_file(),'rapporto migrazione v64 mancante')
    report=jsonfile(root/'REPORT_MIGRAZIONE_RC_v64.json')
    req(report.get('status')=='RC_PREPARATA_NON_RILASCIATA','rapporto migrazione incoerente')
    req(report.get('seasons')==21,'migrazione non ha 21 stagioni')
    req(report.get('publication_enabled') is False,'migrazione segnala pubblicazione attiva')
    config=jsonfile(root/'config/processing.json')
    flag=config.get('processing',{}).get('output',{}).get('publishToSite')
    req(flag is False,'processing.output.publishToSite deve essere FALSE nella RC')
    gui=content(root/'config/recordsnext-gui.properties')
    req('processing.publish=false' in gui,'flag GUI deve essere FALSE nella RC')
    req(fcafile.is_file(),'FCA recuperato 2012/13 mancante')
    req(sha(fcafile)==FCA_SHA,'SHA256 FCA 2012/13 diverso da quello verificato')
    dash=content(root/GUI_DASH)
    dialog=content(root/GUI_CONFIG)
    req('for (Component child : card.getComponents())' in dash,'v65 clic intera card mancante')
    req('var row = loaded;' in dialog,'v65 metadati da repository mancante')
    req('refreshManagedMetadata(loaded)' not in dialog,'rilettura FCM v65 ancora attiva')
    req('new JFrame("RecordsNext by mauz79 · 4.0")' in dash,'titolo v66 non trovato')
    req('(sviluppo)' not in dash and '4.0 dev' not in dash,'titoli di sviluppo ancora presenti')
    req('addNav(side, group, "families", "Famiglie record")' in dash,'v66 menu Famiglie mancante')
    req('addNav(side, group, "culometro", "Soglie e Culometro")' in dash,'v66 menu Culometro mancante')
    req('page.add(cards, BorderLayout.CENTER)' in dash,'v66 card flessibili non presenti')
    for n in ('VERIFICA_GUI_CONFIG_v65.md','VERIFICA_GUI_DASHBOARD_v66.md'):
        req((root/'docs'/n).is_file(),f'report patch GUI assente: {n}; installazione v65/v66 da verificare')
    with closing(sqlite3.connect((root/'data/database/recordsnext.db').resolve().as_uri()+'?mode=ro',uri=True)) as db:
        req(db.execute('PRAGMA quick_check').fetchone()[0]=='ok','SQLite quick_check fallito')
        rows=db.execute('''SELECT s.season_id,c.configured_fcm_path,c.configured_fca_path,c.local_site_path
                   FROM rn_season s LEFT JOIN rn_season_configuration c ON c.season_id=s.season_id''').fetchall()
        req(len(rows)==22,'attese 22 stagioni tra manuali e gestite')
        seasons={r[0]:r for r in rows}
        req('2005_2006' in seasons and not seasons['2005_2006'][1], '2005/06 manuale mancante o con FCM')
        req(seasons['2012_2013'][2]==str(fcafile),'percorso FCA corretto non salvato nella RC')
        req(sum(bool(x[1]) for x in rows)==21,'numero di stagioni con FCM diverso da 21')
        prefix='raw_2012_2013_fca_'
        result=db.execute(f'''SELECT COUNT(*),SUM(CASE WHEN g.GIORNATA=1 THEN 1 ELSE 0 END)
                    FROM "{prefix}giocain" g JOIN "{prefix}punteggio" p ON g.IDPUNTEGGIO=p.ID
                    WHERE p.VOTO1 > 0''').fetchone()
        req(tuple(result)==(9936,255),'FCA 2012/13: voti non 9936/G1 255')
    staging=sorted((root/'data/site-export-staging').glob('records2026_*/js'),
        key=lambda p:p.stat().st_mtime,reverse=True)
    req(staging,'staging JS RC mancante')
    js=list(staging[0].glob('fcmRecordsNext_*.js'))
    req(len(js)==339,'339 JS nello staging RC non trovati')
    maxsize=max(x.stat().st_size for x in js)
    req(maxsize<=1500000,'dimensioni JS oltre 1.500.000 byte')
    return {'report':report, 'staging':staging[0], 'maxjs':maxsize,
            'dash_sha':sha(root/GUI_DASH),'dialog_sha':sha(root/GUI_CONFIG),
            'bib46_sha':sha(root/'docs'/BIB46), 'fca_sha':sha(fcafile)}

def create(root,down,fcafile):
    state=verify(root,down,fcafile)
    docs=root/'docs'; tools=root/'tools'
    req(tools.is_dir(),'directory tools RC mancante')
    targets=[docs/n for n in DOCS]+[tools/'Create-RecordsNext4WorkingCodeMd-v47.py',tools/'Esegui-RN4-Consolida-v67.ps1']
    req(all(not p.exists() for p in targets),'documentazione v47 gia creata: non sovrascrivo')
    for n in ('README.md','INSTALL.md','CHANGELOG.md'):
        req((root/n).is_file(),n+' RC mancante')
    now=datetime.now().astimezone().isoformat(timespec='seconds')
    report=f'''# {TITLE}

Data consolidamento: {now}. Ambiente: `{root}`.

## Risultati verificati

- Motore v58c: 87/87 record, 21 stagioni elaborabili, 339 JS staging; limite massimo 1.500.000 byte per JS (massimo corrente: {state['maxjs']}).
- Sandbox v62b: 21 siti, 3.730 JS, 1.617 shard, cutoff e SHA-256 verificati; frontend TopFlop v53b staticamente integrato in 2025/26 e 2026/27.
- GitHub: checkpoint **gia pubblicato** nel branch `feature/recordsnext4-v63-sandbox-validated`, commit `{SHA_V63_GIT}`; Maven 58/58 test passati nel commit v63b.
- **Migrazione v64b completata**: `D:\\DEV_APPS\\RecordsNext4.0_RC_v64` e' copia RC autonoma dalla TEST e conserva le configurazioni della 3.2 per 21 stagioni; nessun nuovo import richiesto.
- `2005_2006`: stagione manuale, senza FCM; e' esclusa dalla elaborazione/pubblicazione.
- FCA 2012/13 corretto: `{fcafile}`, SHA-256 `{state['fca_sha']}`; SQLite RC 9.936 voti, 255 in G1. **Non sostituirlo con FCA precedente della 3.2.**
- Intervento GUI v65: la card Configurazione stagioni apre al primo clic su tutta la superficie; apertura configurazione usa metadati archiviati e non rilegge 21 FCM con UCanAccess.
- Intervento GUI v66: rimosse diciture "sviluppo" e "dev"; card 2x2 flessibili senza tagli; menu laterale coerente con tutte le quattro card (Configurazione stagioni, Famiglie record, Top & Flop, Soglie e Culometro).
- Java Dashboard SHA-256 `{state['dash_sha']}`; dialog configurazione SHA-256 `{state['dialog_sha']}`.
- Pubblicazione automatica RC: **disattivata** nel processing.json e nella GUI. SQLite verificato `quick_check=ok`.

## Non ancora certificato

- Collaudo funzionale/visivo finale della GUI v65/v66: il controllo sorgenti e i report locali non equivalgono all'approvazione di tutte le interazioni.
- Collaudo browser reale di `recordsnext.html` e `RecordsNext/top_flop.html` in almeno due skin: JS caricati, filtri, link, errori in console, compatibilita' con le pagine della 3.2.
- Backup e rilascio effettivo dei siti `E:` dalla **GUI 4.0**; verifica successiva e piano rollback della 3.2. **Non eseguiti in questa procedura.**

## Continuita' operativa e prossime operazioni

1. Avviare **solo** `D:\\DEV_APPS\\RecordsNext4.0_RC_v64\\Avvia-RecordsNext4-RC-v64.ps1`; verificare le 22 stagioni (21 elaborabili), le quattro card/menu e che 2012/13 punti all'FCA recuperato. Non premere Elabora/Pubblica prima del rilascio.
2. Testare nel browser le pagine sandbox `D:\\DEV_APPS\\RecordsNext4.0_TEST_v37\\data\\publish-sandbox-v52\\sites\\2026_2027\\recordsnext.html` e `...\\RecordsNext\\top_flop.html`, oltre al 2025_2026. Esaminare console e comportamento skin.
3. Usare la GUI per il passaggio reale 3.2->4.0 solo con backup dei file preesistenti, destinazioni ricontrollate e rollback predisposto. Non ripetere l'associazione di 21 stagioni o l'import FCA 2012/13.
4. GitHub: il branch v63 e' base stabile; il consolidamento GUI v67 va su un **nuovo branch** e non altera il branch v46 congelato.

## Integrita' e riferimenti

- Report migrazione: `{root/'REPORT_MIGRAZIONE_RC_v64.json'}`.
- Staging: `{state['staging']}`.
- Bibbia v46 (base codice): SHA-256 `{state['bib46_sha']}`.
- Report v65: `docs/VERIFICA_GUI_CONFIG_v65.md`.
- Report v66: `docs/VERIFICA_GUI_DASHBOARD_v66.md`.
- Modifiche reali ai sorgenti e certificazione di output riportate nei documenti v46 + v47; nessuna correzione FCA/FCM/SQLite eseguita dal consolidamento.
'''
    diary=f'''# RecordsNext4 - Diario implementazione v47

- {now}: confermata migrazione v64b 3.2 -> 4.0 RC v64: 21 stagioni, percorsi reali recuperati, senza reimport o sostituzione 3.2.
- FCA 2012/13 corretto preservato e ricontrollato (SHA-256={state['fca_sha']}, 9936 voti, 255 G1).
- GUI v65: clic della card sistemato e rilettura FCM/UCanAccess eliminata per apertura Configurazione stagioni.
- GUI v66: titoli senza dev/sviluppo, card responsive, menu completo e coerente con le quattro card.
- Stato precedente: 87/87 record, 339 JS, sandbox 21 siti/3730 JS/1617 shard, skin TopFlop integrata staticamente, checkpoint GitHub v63=`{SHA_V63_GIT}` 58/58 test.
- Collaudo visuale browser, conferma funzionale completa della GUI, pubblicazione reale e rimpiazzo 3.2 ancora pendenti.
- Documentazione consolidata in RC v64 e candidato di commit GitHub su nuovo branch v67; lo stato push va letto dal risultato del comando GitHub, non inferito.
'''
    decisions='''# RecordsNext4 - Registro decisioni v40

## DEC-v67-001 - Migrazione invece di ricominciare
La RC v64 e' una copia dell'installazione 4.0 gia' validata: preservare SQLite, identita, record e configurazione recuperata dalla 3.2 (21 stagioni). Non riconfigurare a mano i 21 percorsi.

## DEC-v67-002 - FCA 2012/2013 immutabile
Preservare il file ArchivioA2012SerieAmauz_G1_RECUPERATA_v1.fca con SHA-256 e 9936 voti/255 G1. Bloccare migrazioni/pubblicazioni che tornino al vecchio FCA 3.2.

## DEC-v67-003 - Interfaccia utente
Apertura card al primo clic, metadati stagione da SQLite anziche' rilettura UCanAccess, nessuna etichetta di sviluppo, card senza clipping, stesso insieme di sezioni nella dashboard e nel menu laterale.

## DEC-v67-004 - Flusso integrato
L'operatore deve poter configurare, elaborare e pubblicare dalla GUI. Script esterni solo per migrazione iniziale, diagnostica e salvaguardia; niente rielaborazione o pubblicazione tramite comandi non richiesti.

## DEC-v67-005 - Rilascio reale NON certificato
La sandbox certifica dati/shard, non browser e sito reale. Prima backup, test browser, conferma percorsi, flag produzione e rollback; nessun commit GitHub equivale al deploy.

## DEC-v67-006 - GitHub incrementale
Partire da commit 3b008a8 branch feature/recordsnext4-v63-sandbox-validated. Conservare v46 congelato e v63; integrare solo GUI v65/v66, migrazione v64/v64b e documentazione aggiornata nel nuovo branch v67. Non committare DB, config privati, file FCA/FCM o JS staging.
'''
    oldbib=content(docs/BIB46)
    appendix=f'''\n\n# AGGIORNAMENTO v67 / BIBBIA v47 - MIGRAZIONE 3.2 -> 4.0 E GUI\n\nGenerato: {now}. Base Bibbia v46 SHA-256 `{state['bib46_sha']}`. Stato RC: NON rilascio reale.\n\n## Stato verificato\n\n{report}\n\n## Diario v47\n\n{diary}\n\n## Registro decisioni v40\n\n{decisions}\n'''
    sources=[root/GUI_DASH,root/GUI_CONFIG,
             down/'RN4_MIGRAZIONE_RC_v64/Prepara-RecordsNext4-RC-v64.py',
             down/'RN4_MIGRAZIONE_RC_v64b/Riprendi-RecordsNext4-RC-v64b.py',
             down/'RN4_GUI_CONFIG_v65/Applica-RN4-GuiConfig-v65.py',
             down/'RN4_GUI_DASHBOARD_v66/Applica-RN4-Dashboard-v66.py',
             Path(__file__).resolve()]
    for p in sources:
        req(p.is_file(),'sorgente per Bibbia mancante: '+str(p))
        lang={'.java':'java','.py':'python','.ps1':'powershell'}.get(p.suffix,'text')
        appendix+=f'\n## Sorgente `{p.name}`\n\nSHA-256 `{sha(p)}`\n\n~~~~~{lang}\n{content(p).rstrip()}\n~~~~~\n'
    bible=oldbib.rstrip()+appendix
    # La Bibbia v46 puo contenere frammenti CRLF storici: normalizzare solo il documento
    # generato evita migliaia di falsi avvisi Git senza modificare i sorgenti originali.
    bible=re.sub(r'[ \t]+(?=\n)', '', bible.replace('\r\n','\n').replace('\r','\n'))
    changed={docs/DOCS[0]:bible, docs/DOCS[1]:diary,
             docs/DOCS[2]:decisions, docs/DOCS[3]:report}
    overview='''## RecordsNext4 v67 - RC migrata e GUI corretta (non produzione)
- Configurazione 3.2 recuperata su 21 stagioni nella RC v64 senza importazioni; 2005/06 manuale esclusa.
- FCA corretto 2012/13 conservato: 9936 voti e 255 G1, SHA-256 verificato.
- GUI v65: card apribili al primo clic, niente scansione FCM/UCanAccess aprendo la configurazione.
- GUI v66: titoli senza dev/sviluppo, card flessibili, menu coerente.
- Motore 87/87 record; 339 JS staging, sandbox 21 siti/3730 JS/1617 shard certificata.
- Siti reali ancora intatti; browser e cutover 3.2->4.0 pendenti.
- Continuita': docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v47.md, docs/STATO_MIGRAZIONE_GUI_RELEASE_v67.md.
'''
    newtxt={root/'CHANGELOG.md':overview+'\n'+content(root/'CHANGELOG.md').lstrip(),
            root/'README.md':suffix_update(root/'README.md','## Stato RC 4.0 / v67',
                'Avvio: `Avvia-RecordsNext4-RC-v64.ps1`. Migrazione v64b e GUI v65/v66 documentate in `docs/STATO_MIGRAZIONE_GUI_RELEASE_v67.md`; codice completo in Bibbia v46 + aggiornamento v47. Pubblicazione reale e collaudo browser **non ancora eseguiti**.'),
            root/'INSTALL.md':suffix_update(root/'INSTALL.md','## Aggiornamento RC v67',
                'La RC migrata si trova in `D:\\DEV_APPS\\RecordsNext4.0_RC_v64`. Non reinstallare o reimportare le 21 stagioni; FCA 2012/13 recuperato obbligatorio. GUI `Avvia-RecordsNext4-RC-v64.ps1`. Prima della pubblicazione reale: controllare destinazioni, backup, browser e rollback. La documentazione v47 non pubblica nulla.')}
    stamp=datetime.now().strftime('%Y%m%d_%H%M%S')
    backup=down/f'RN4_DOC_V67_BACKUP_{stamp}'
    req(not backup.exists(),'backup preesistente')
    backup.mkdir(parents=True)
    # Compute all content before any writes. Backup on disk then write with rollback.
    created=[]
    try:
        for p in newtxt:
            shutil.copy2(p,backup/p.name)
        for p,txt in changed.items():
            with p.open('x',encoding='utf-8',newline='\n') as f:f.write(txt)
            created.append(p)
        for source,target in [(Path(__file__).resolve(),tools/'Create-RecordsNext4WorkingCodeMd-v47.py'),
                              (Path(__file__).resolve().parent/'Esegui-RN4-Consolida-v67.ps1',tools/'Esegui-RN4-Consolida-v67.ps1')]:
            req(source.is_file(),'script pacchetto mancante: '+str(source))
            with target.open('xb') as f:f.write(source.read_bytes())
            created.append(target)
        for p,txt in newtxt.items():p.write_text(txt,encoding='utf-8',newline='\n')
    except Exception:
        for p in created:
            if p.exists():p.unlink()
        for p in newtxt:
            src=backup/p.name
            if src.is_file():shutil.copy2(src,p)
        raise
    status={'status':'RN4_DOC_V47_OK_RC_NON_RELEASE','time':now,'root':str(root),
            'bible':str(docs/DOCS[0]),'bible_sha256':sha(docs/DOCS[0]),
            'migration': 'v64b OK','gui_v65_v66':'source verified',
            'record_families':87,'seasons':21,'stage_js':339,'js_max_bytes':state['maxjs'],
            'fca_2012_sha256':state['fca_sha'],'browser_verified':False,
            'production_published':False,'git_previous_commit':SHA_V63_GIT,
            'backup':str(backup)}
    result=down/f'RN4_DOC_V47_ESITO_{stamp}.json'
    result.write_text(json.dumps(status,indent=2,ensure_ascii=False),encoding='utf-8')
    print('BIBBIA v47 OK:',status['bible'])
    print('DIARIO v47, REGISTRO v40, REPORT v67, README, INSTALL, CHANGELOG OK')
    print('GUI v65/v66 sorgenti verificati | RC 21 stagioni | 2012/13 FCA corretto')
    print('BACKUP TESTI:',backup)
    print('RAPPORTO:',result)
    print('NESSUN DB/FCM/FCA/JS/SITO REALE MODIFICATO')

if __name__=='__main__':
    try:
        a=argparse.ArgumentParser()
        a.add_argument('--root',type=Path,default=ROOT)
        a.add_argument('--downloads',type=Path,default=DOWN)
        a.add_argument('--fca',type=Path,default=FCA)
        args=a.parse_args()
        create(args.root,args.downloads,args.fca)
    except Exception as e:
        print('ERRORE DOCUMENTAZIONE v67:',e,file=sys.stderr)
        sys.exit(2)
