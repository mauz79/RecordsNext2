#!/usr/bin/env python3
"""Validate and finish the already-created RN4 v64 .PREPARAZIONE candidate.

No recopy, reimport, Java build, GUI launch, DB write or publication.
All SQLite connections are explicitly closed BEFORE the final rename.
"""
from __future__ import annotations
import argparse
from contextlib import closing
from hashlib import sha256
import json, os, ntpath, shutil, sqlite3, stat, sys
from pathlib import Path

TEST=Path(r'D:\DEV_APPS\RecordsNext4.0_TEST_v37')
RC=Path(r'D:\DEV_APPS\RecordsNext4.0_RC_v64')
FCA=Path(r'E:\FCM\data\ArchivioA2012SerieAmauz_G1_RECUPERATA_v1.fca')
FCA_SHA='e3c34b941aedb35951ba030701fe47072afa00941f7fa7884d0658442cfcf766'
FACADES=('Core','Classics','Series','RU','Modifiers','ThresholdsLuck','Culometro','Matches','Manifest')
PROTECTED=('topflop.json','player-identities.json')

def check(ok,msg):
    if not ok: raise RuntimeError('STOP: '+msg)

def hashfile(p):
    h=sha256()
    with p.open('rb') as f:
        for block in iter(lambda:f.read(1024*1024),b''): h.update(block)
    return h.hexdigest()

def loadjson(p): return json.loads(p.read_text(encoding='utf-8-sig'))

def ro(p): return sqlite3.connect(p.resolve().as_uri()+'?mode=ro',uri=True,timeout=4)

def pathsafe(root,p):
    check(p.resolve().is_relative_to(root.resolve()),'Percorso fuori radice di verifica: '+str(p))

def nonlink_tree(root):
    # Work only on owned files. A junction/symlink inside an RC tree is unsafe.
    flag=getattr(stat,'FILE_ATTRIBUTE_REPARSE_POINT',0x400)
    for dirpath,dirs,files in os.walk(root,followlinks=False):
        for name in dirs+files:
            p=Path(dirpath)/name
            check(not p.is_symlink() and not (getattr(p.lstat(),'st_file_attributes',0)&flag),
                  'Link o junction non consentito: '+str(p))

def verify(test,rc,fcafile):
    temp=rc.with_name(rc.name+'.PREPARAZIONE')
    check(rc.name=='RecordsNext4.0_RC_v64','Nome RC inatteso')
    check(temp.is_dir(),'Cartella PREPARAZIONE inesistente: '+str(temp))
    check(not rc.exists(),'La destinazione RC esiste gia: non sovrascrivo '+str(rc))
    check(test.is_dir(),'TEST sorgente mancante')
    check(temp.resolve().parent==test.resolve().parent,'PREPARAZIONE deve essere accanto a TEST')
    check(str(test.resolve()).casefold()!=str(temp.resolve()).casefold(),'TEST e PREPARAZIONE coincidono')
    nonlink_tree(temp)
    for p in (temp/'target/RecordsNext.jar',temp/'data/database/recordsnext.db',
              temp/'config/processing.json',temp/'config/recordsnext-gui.properties',
              temp/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v46.md',
              temp/'Avvia-RecordsNext4-RC-v64.ps1',temp/'REPORT_MIGRAZIONE_RC_v64.json'):
        check(p.is_file(),'File preparazione mancante: '+str(p))
    report=loadjson(temp/'REPORT_MIGRAZIONE_RC_v64.json')
    check(report.get('status')=='RC_PREPARATA_NON_RILASCIATA','Report v64 non ha status finale OK')
    check(report.get('rc_path','').casefold()==str(rc).casefold(),'Report v64 indica RC diversa')
    check(report.get('source_test','').casefold()==str(test).casefold(),'Report v64 indica TEST diversa')
    check(report.get('publication_enabled') is False,'Report segnala pubblicazione attiva')
    check(report.get('seasons')==21,'Report: stagioni diverse da 21')
    state=loadjson(test/'data/publish-sandbox-v52/state.json')
    check(state.get('schema')=='rn4-publish-sandbox-v52','Manifest di sandbox non valido')
    check(state.get('root','').casefold()==str(test).casefold(),'Manifest appartiene ad altra TEST')
    original=state.get('original',{}); selected=state.get('selected',[])
    check(len(selected)==21 and len(set(selected))==21,'Manifest non ha 21 stagioni uniche')
    check(original.get('2005_2006') is None and '2005_2006' not in selected,
          'Stagione manuale 2005/06 deve rimanere esclusa')
    check(set(report.get('sites_original_restored',[]))==set(selected),
          'Report non attesta ripristino delle 21 destinazioni')
    check(fcafile.is_file(),'FCA corretto 2012/13 non trovato')
    check(hashfile(fcafile)==FCA_SHA,'FCA corretto: SHA-256 differente')
    for n in PROTECTED:
        a=test/'config'/n; b=temp/'config'/n
        check(a.is_file() and b.is_file(),'Configurazione protetta mancante '+n)
        check(hashfile(a)==hashfile(b),'Configurazione protetta alterata '+n)
    check(hashfile(test/'target/RecordsNext.jar')==hashfile(temp/'target/RecordsNext.jar'),
          'JAR RC diverso dal TEST validato')
    j=loadjson(temp/'config/processing.json')
    check(j.get('processing',{}).get('output',{}).get('publishToSite') is False,
          'processing.json RC: pubblicazione attiva')
    prop=(temp/'config/recordsnext-gui.properties').read_text(encoding='latin-1')
    check(any(line.strip()=='processing.publish=false' for line in prop.splitlines()),
          'GUI RC: pubblicazione non disattivata')
    check(any(line.strip().startswith('publish.destinationMode=custom') for line in prop.splitlines()),
          'GUI RC: target personalizzato di sicurezza non trovato')
    launcher=(temp/'Avvia-RecordsNext4-RC-v64.ps1').read_text(encoding='utf-8-sig')
    check("RecordsNext4.0_RC_v64" in launcher and 'target\\RecordsNext.jar' in launcher,
          'Launcher RC inatteso')
    check(len(list((temp/'runtime/ucanaccess').rglob('*.jar')))>=5,'Runtime UCanAccess incompleto')
    rootsrc=test/'data/site-export-staging'; rootdst=temp/'data/site-export-staging'
    srcs=[p for p in rootsrc.glob('records2026_*') if (p/'js').is_dir()]
    srcs.sort(key=lambda p:(p.stat().st_mtime,p.name),reverse=True)
    check(len(srcs)>0,'Staging sorgente mancante')
    src=srcs[0]/'js'; dst=rootdst/srcs[0].name/'js'
    check(dst.is_dir(),'Staging non copiato: '+str(dst))
    fs={p.name:p for p in src.glob('fcmRecordsNext_*.js')}
    fd={p.name:p for p in dst.glob('fcmRecordsNext_*.js')}
    check(len(fs)==len(fd)==339,'Staging deve contenere 339 JS in entrambe le copie')
    check(set(fs)==set(fd),'Nomi JS staging non corrispondono')
    check(all(f'fcmRecordsNext_{n}.js' in fd for n in FACADES),'Mancano facade di compatibilita 3.2')
    for name,p in fd.items():
        check(0<p.stat().st_size<=1500000,'JS fuori limite: '+name)
        check(hashfile(fs[name])==hashfile(p),'JS non identico a staging TEST: '+name)
    # Explicit connection closing is required: with sqlite3.connect() alone does not close on Windows.
    database=temp/'data/database/recordsnext.db'
    with closing(ro(database)) as db:
        check(db.execute('PRAGMA quick_check').fetchone()[0]=='ok','SQLite RC quick_check fallito')
        season_rows=db.execute('''SELECT s.season_id,c.configured_fcm_path,c.configured_fca_path,
                             c.local_site_path FROM rn_season s LEFT JOIN rn_season_configuration c
                             ON c.season_id=s.season_id''').fetchall()
        records={x[0]:x for x in season_rows}
        check(set(records)==set(original),'Stagioni RC non corrispondono al manifest')
        check(not records['2005_2006'][1],'2005/06 ha un FCM inatteso')
        for sid in selected:
            check(records[sid][3]==original[sid],'Destinazione reale non ripristinata: '+sid)
        check(records['2012_2013'][2]==str(fcafile),'2012/13: percorso FCA corretto non preservato')
        imp=db.execute('''SELECT source_path FROM rn_import WHERE season_id='2012_2013'
                   AND source_type='FCA' AND status='COMPLETED'
                   ORDER BY import_id DESC LIMIT 1''').fetchone()
        check(imp is not None and ntpath.basename(imp[0]).casefold()==fcafile.name.casefold(),
              'Ultimo import FCA 2012/13 non e il file recuperato')
        prefix='raw_2012_2013_fca_'
        total=db.execute(f'''SELECT count(*) FROM "{prefix}giocain" g JOIN "{prefix}punteggio" p
                  ON g.IDPUNTEGGIO=p.ID WHERE p.VOTO1>0''').fetchone()[0]
        g1=db.execute(f'''SELECT count(*) FROM "{prefix}giocain" g JOIN "{prefix}punteggio" p
                  ON g.IDPUNTEGGIO=p.ID WHERE p.VOTO1>0 AND g.GIORNATA=1''').fetchone()[0]
        check((total,g1)==(9936,255),f'FCA 2012/13 voti inattesi: {total}, G1={g1}')
    print('VALIDAZIONE PREPARAZIONE OK: 21 stagioni, 339 JS identici, SQLite integro',flush=True)
    print('FCA 2012/13: SHA-256 OK | 9936 voti | 255 G1',flush=True)
    print('PUBBLICAZIONE DISATTIVATA; originale TEST e 3.2 invariati',flush=True)
    return temp

def main():
    a=argparse.ArgumentParser()
    a.add_argument('--mode',choices=('check','resume'),required=True)
    a.add_argument('--test',type=Path,default=TEST)
    a.add_argument('--rc',type=Path,default=RC)
    a.add_argument('--corrected-fca',type=Path,default=FCA)
    v=a.parse_args()
    temp=verify(v.test,v.rc,v.corrected_fca)
    if v.mode=='check':
        print('ANTEPRIMA OK: nessuna modifica e nessuna rinomina')
        return
    print('RINOMINA FINALE PREPARAZIONE -> RC ...',flush=True)
    try:
        temp.rename(v.rc)
    except PermissionError as e:
        print('STOP: Windows tiene aperto un file nella cartella di preparazione o nega la rinomina.',file=sys.stderr)
        print('Chiudere Explorer/antivirus/GUI che occupano la cartella e riprovare lo stesso comando; nessun file cancellato.',file=sys.stderr)
        raise
    check(v.rc.is_dir() and not temp.exists(),'Rinomina non completata')
    print('MIGRAZIONE RC v64b OK:',v.rc)
    print('CONFIGURAZIONE 3.2 RECUPERATA: 21 stagioni; 2005/06 esclusa')
    print('FCA 2012/13 RECUPERATO; dati e identita TEST conservati')
    print('PUBBLICAZIONE DISATTIVATA, nessun sito reale modificato')
    print('AVVIO GUI: powershell.exe -NoProfile -ExecutionPolicy Bypass -File',v.rc/'Avvia-RecordsNext4-RC-v64.ps1')

if __name__=='__main__':
    try:main()
    except Exception as e:
        print('ERRORE RIPRESA RC v64b:',e,file=sys.stderr)
        sys.exit(2)
