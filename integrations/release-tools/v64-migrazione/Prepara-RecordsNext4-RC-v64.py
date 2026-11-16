#!/usr/bin/env python3
"""One-time RecordsNext 3.2 -> 4.0 release candidate preparation.

Source TEST and legacy 3.2 are READ ONLY. No writes to E:, live sites,
existing TEST or any already-existing candidate directory.
The candidate is never configured for automatic publication.
"""
from __future__ import annotations
import argparse, datetime, hashlib, json, ntpath, os, re, shutil, sqlite3, stat, sys
from pathlib import Path

TEST = Path(r'D:\DEV_APPS\RecordsNext4.0_TEST_v37')
RC = Path(r'D:\DEV_APPS\RecordsNext4.0_RC_v64')
OLD = Path(r'E:\fcm\plugin\Mauz_strom2026\RecordsNext')
CORRECTED = Path(r'E:\FCM\data\ArchivioA2012SerieAmauz_G1_RECUPERATA_v1.fca')
CORRECTED_SHA = 'e3c34b941aedb35951ba030701fe47072afa00941f7fa7884d0658442cfcf766'
SANDBOX = 'publish-sandbox-v52'
MIGRATION_FIELDS = ('configured_fcm_path','configured_fca_path','local_site_path','online_site_url','dataa_path')
REQUIRED_FACADES = ('Core','Classics','Series','RU','Modifiers','ThresholdsLuck','Culometro','Matches','Manifest')

def require(ok, msg):
    if not ok: raise RuntimeError(msg)

def digest(p):
    h=hashlib.sha256()
    with p.open('rb') as f:
        for chunk in iter(lambda:f.read(1024*1024),b''): h.update(chunk)
    return h.hexdigest()

def ro(db):
    return sqlite3.connect(db.resolve().as_uri()+'?mode=ro',uri=True,timeout=3)

def loadjson(p): return json.loads(p.read_text(encoding='utf-8-sig'))

def props(p):
    out={}
    for ln in p.read_text(encoding='latin-1').splitlines():
        ln=ln.lstrip()
        if not ln or ln[0] in '#!':continue
        m=re.match(r'^([^\s=:]+)\s*[=:]\s*(.*)$',ln)
        if m:
            s=m.group(2)
            out[m.group(1)]=re.sub(r'\\([:=\\])', lambda x:x.group(1),s)
    return out

def prop_rewrite(p,updates):
    orig=p.read_text(encoding='latin-1').splitlines(keepends=True)
    out=[];seen=set()
    for ln in orig:
        m=re.match(r'^\s*([^\s=:]+)\s*[=:]',ln)
        if m and m.group(1) in updates:
            key=m.group(1)
            if key not in seen:
                val=str(updates[key]).replace('\\','\\\\').replace(':','\\:').replace('=','\\=')
                out.append(f'{key}={val}\n');seen.add(key)
        else:out.append(ln)
    for key,val in updates.items():
        if key not in seen:
            val=str(val).replace('\\','\\\\').replace(':','\\:').replace('=','\\=')
            out.append(f'{key}={val}\n')
    p.write_text(''.join(out),encoding='latin-1')

def get_rows(db):
    cols={r[1] for r in db.execute('PRAGMA table_info(rn_season_configuration)')}
    require(set(MIGRATION_FIELDS).issubset(cols),'Schema rn_season_configuration non compatibile')
    db.row_factory=sqlite3.Row
    return {r['season_id']:dict(r) for r in db.execute('''
      SELECT s.season_id, s.display_name, c.management_type, c.configuration_status,
             c.configured_fcm_path, c.configured_fca_path, c.local_site_path,
             c.online_site_url, c.dataa_path
      FROM rn_season s LEFT JOIN rn_season_configuration c ON c.season_id=s.season_id
      ORDER BY s.season_id''')}

def fca_check(db,fcafile):
    row=db.execute("""SELECT import_id,source_path FROM rn_import
       WHERE season_id='2012_2013' AND source_type='FCA' AND status='COMPLETED'
       ORDER BY import_id DESC LIMIT 1""").fetchone()
    require(row is not None,'Import FCA 2012/13 non trovato nel database TEST')
    ident,importpath=row
    require(ntpath.basename(importpath).casefold()==fcafile.name.casefold(),
            'STOP: FCA 2012/13 importato diverso dal recuperato: '+str(importpath))
    require(fcafile.is_file(),'STOP: FCA recuperato non trovato: '+str(fcafile))
    actual=digest(fcafile)
    require(actual==CORRECTED_SHA,'STOP: SHA256 FCA recuperato inatteso: '+actual)
    f='raw_2012_2013_fca_'
    total=db.execute(f'''SELECT COUNT(*) FROM "{f}giocain" g
                         JOIN "{f}punteggio" p ON p.ID=g.IDPUNTEGGIO WHERE p.VOTO1>0''').fetchone()[0]
    day1=db.execute(f'''SELECT COUNT(*) FROM "{f}giocain" g
                         JOIN "{f}punteggio" p ON p.ID=g.IDPUNTEGGIO
                         WHERE p.VOTO1>0 AND g.GIORNATA=1''').fetchone()[0]
    require((total,day1)==(9936,255),f'STOP: voti FCA 2012/13 {(total,day1)} != (9936,255)')
    return {'import_id':ident,'source_path':importpath,'sha256':actual,'voti':total,'voti_g1':day1}

def inspect(test,rc,old,fcafile):
    require(test.is_dir() and 'TEST' in test.name.upper(),'Sorgente TEST non valida')
    require(rc.resolve()!=test.resolve() and not rc.resolve().is_relative_to(test.resolve()),'RC dentro TEST')
    require(rc.resolve().parent==test.resolve().parent,'RC deve stare accanto alla TEST, non su E: o nei siti')
    require(not str(rc.resolve()).lower().startswith(str(old.resolve()).lower()+os.sep.lower()),'RC non puo stare nella 3.2')
    require(rc.name=='RecordsNext4.0_RC_v64','Nome destinazione RC differente dal previsto')
    for p in [test/'target/RecordsNext.jar',test/'config/processing.json',test/'config/recordsnext-gui.properties',
              test/'config/topflop.json',test/'config/player-identities.json',test/'data/database/recordsnext.db',
              test/f'data/{SANDBOX}/state.json',test/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v46.md']:
        require(p.is_file(),'File TEST mancante: '+str(p))
    state=loadjson(test/f'data/{SANDBOX}/state.json')
    require(state.get('schema')=='rn4-publish-sandbox-v52','Sandbox manifest inatteso')
    require(state.get('root','').casefold()==str(test).casefold(),'Manifest sandbox appartiene ad altra TEST')
    originals=state.get('original',{});selected=state.get('selected',[])
    require(len(selected)==21 and len(set(selected))==21,'Attese esattamente 21 stagioni selezionate')
    require('2005_2006' not in selected and originals.get('2005_2006') is None,
            '2005/06 non deve essere elaborabile/pubblicabile')
    require(all(originals.get(s) for s in selected),'Stagione selezionata senza percorso originale 3.2')
    require(all('publish-sandbox' not in originals[s].casefold() and 'recordsnext4.0_test' not in originals[s].casefold() for s in selected),
            'Manifest sandbox contiene destinazioni di test come destinazioni originali')
    require(len(set(originals))==len(originals),'Destinazioni duplicate')
    sqlitepath=test/'data/database/recordsnext.db'
    with ro(sqlitepath) as db:
        seasonrows=get_rows(db)
        require(set(seasonrows)==set(originals),'Differenza stagioni TEST e manifest sandbox')
        for sid,row in seasonrows.items():
            if sid not in selected:continue
            loc=row['local_site_path']
            sandbox=(test/'data'/SANDBOX/'sites'/sid).resolve()
            require(loc and Path(loc).resolve()==sandbox,
                    f'STOP: SQLite TEST non punta piu alla sandbox per {sid}: {loc}')
        fixed=fca_check(db,fcafile)
        quick=db.execute('PRAGMA quick_check').fetchone()[0]
        require(quick=='ok','SQLite TEST: quick_check fallito: '+str(quick))
    legacy={}
    legacy_db=old/'data/database/recordsnext.db'
    if legacy_db.is_file():
        with ro(legacy_db) as db: legacy=get_rows(db)
    else:
        print('ATTENZIONE: DB 3.2 non disponibile, si conserva la configurazione pre-sandbox gia in TEST')
    current=sorted([p for p in (test/'data/site-export-staging').glob('records2026_*') if (p/'js').is_dir()],
                   key=lambda p:(p.stat().st_mtime,p.name),reverse=True)
    require(current,'Staging JS v58c assente')
    stage=current[0]
    files=list((stage/'js').glob('fcmRecordsNext_*.js'))
    require(len(files)==339,f'Staging piu recente inatteso: {len(files)} JS, richiesti 339')
    for f in files:
        require(0<f.stat().st_size<=1500000,'JS non valido: '+str(f))
    require(all((stage/'js'/('fcmRecordsNext_'+x+'.js')).is_file() for x in REQUIRED_FACADES),
            'Facade 3.2 assenti nello staging')
    top_hash={x:digest(test/'config'/x) for x in ('topflop.json','player-identities.json')}
    return {'originals':originals,'selected':selected,'rows':seasonrows,'legacy':legacy,'fixed':fixed,
            'stage':stage,'protected':top_hash,'jar_sha256':digest(test/'target/RecordsNext.jar')}

IGNORE_ROOT={'.git','backups','reports','target'}
IGNORE_DATA={'database',SANDBOX,'site-export-staging'}

def copy_clean(src,dest,report):
    # Prevent following any symlink or Windows junction in the source tree.
    require(not src.is_symlink(),'Collegamento sorgente non ammesso: '+str(src))
    dest.mkdir(parents=True,exist_ok=False)
    for p in src.iterdir():
        if p.name in IGNORE_ROOT or p.name=='data':continue
        copyitem(p,dest/p.name,report)
    data=src/'data'
    if data.is_dir():
        out=dest/'data';out.mkdir()
        for p in data.iterdir():
            if p.name in IGNORE_DATA:continue
            copyitem(p,out/p.name,report)
    (dest/'target').mkdir(exist_ok=True)
    shutil.copy2(src/'target/RecordsNext.jar',dest/'target/RecordsNext.jar')

def junction(p):
    return p.is_symlink() or bool(getattr(p.lstat(),'st_file_attributes',0) & getattr(stat,'FILE_ATTRIBUTE_REPARSE_POINT',0x400))

def copyitem(src,dst,report):
    if junction(src):
        report['skipped_links'].append(str(src)); return
    if src.is_dir():
        dst.mkdir()
        for p in src.iterdir(): copyitem(p,dst/p.name,report)
    elif src.is_file():shutil.copy2(src,dst)

def build_launcher(rc):
    s=r'''param()
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$root = [System.IO.Path]::GetFullPath($PSScriptRoot)
if ((Split-Path -Leaf $root) -ne 'RecordsNext4.0_RC_v64') { throw 'Cartella RC inattesa' }
$jar = Join-Path $root 'target\RecordsNext.jar'
$runtime = Join-Path $root 'runtime\ucanaccess'
if (!(Test-Path -LiteralPath $jar)) { throw "JAR mancante: $jar" }
if (!(Test-Path -LiteralPath (Join-Path $runtime 'ucanaccess-2.0.9.5.jar'))) { throw 'UCanAccess mancante nella RC' }
$dependencies = @(Get-ChildItem -LiteralPath $runtime -Filter '*.jar' -File -Recurse)
if ($dependencies.Count -lt 5) { throw "Runtime incompleto: $($dependencies.Count) JAR" }
$cp = (@($jar) + @($dependencies | ForEach-Object FullName)) -join ';'
Write-Host 'RecordsNext 4.0 RC - pubblicazione automatica DISATTIVATA'
Push-Location $root
try {
    & java -cp $cp it.alterlega.recordsnext.gui.RecordsNext2Dashboard
    if ($LASTEXITCODE -ne 0) {throw "GUI terminata con errore $LASTEXITCODE"}
} finally {Pop-Location}
'''
    (rc/'Avvia-RecordsNext4-RC-v64.ps1').write_text(s,encoding='utf-8-sig')

def prepare(test,rc,old,fcafile,ctx):
    require(not rc.exists(),'RC gia presente: non sovrascrivo '+str(rc))
    temp=rc.with_name(rc.name+'.PREPARAZIONE')
    require(not temp.exists(),'Preparazione precedente da controllare: '+str(temp))
    report={'created_at':datetime.datetime.now().astimezone().isoformat(),'status':'IN_PREPARAZIONE',
            'source_test':str(test),'rc_path':str(rc),'legacy_3_2_read_only':str(old),
            'seasons':len(ctx['selected']),'fca_2012_2013':ctx['fixed'],
            'js_staging_files':339,'skipped_links':[],'3_2_fields_filled':[],
            'sites_original_restored':[], 'publication_enabled':False}
    try:
        print('COPIA BASE 4.0 TEST -> RC separata (esclusi sandbox e vecchi backup)',flush=True)
        copy_clean(test,temp,report)
        # Import the entire latest validated staging without forcing a new processing cycle.
        stage_dst=temp/'data/site-export-staging'/ctx['stage'].name
        stage_dst.parent.mkdir(parents=True,exist_ok=True)
        copyitem(ctx['stage'],stage_dst,report)
        (temp/'data/database').mkdir(parents=True,exist_ok=True)
        with sqlite3.connect(test/'data/database/recordsnext.db',timeout=10) as src, \
             sqlite3.connect(temp/'data/database/recordsnext.db') as dst:
            src.backup(dst,pages=2000,sleep=0.02)
        with sqlite3.connect(temp/'data/database/recordsnext.db',timeout=10) as db:
            db.execute('BEGIN IMMEDIATE')
            for sid,orig in ctx['originals'].items():
                row=ctx['rows'][sid]
                # The 2005/06 manual season has no FCM and remains unpublishable.
                if sid=='2005_2006':
                    require(not row['configured_fcm_path'],'2005/06 has unexpected FCM')
                    continue
                require(orig and isinstance(orig,str),f'Destinazione originale mancante: {sid}')
                fields={'local_site_path':orig}
                oldrow=ctx['legacy'].get(sid,{})
                for col in MIGRATION_FIELDS:
                    if col=='local_site_path':continue
                    # v4 TEST already contains the latest mapped configuration.
                    # 3.2 is ONLY a fallback for fields currently empty, never an overwrite.
                    if (row[col] is None or str(row[col]).strip()=='') and oldrow.get(col):
                        if sid=='2012_2013' and col=='configured_fca_path':continue
                        fields[col]=oldrow[col]
                        report['3_2_fields_filled'].append({'season':sid,'field':col})
                if sid=='2012_2013':
                    # Require the corrected physical FCA, irrespective of 3.2's old path.
                    fields['configured_fca_path']=str(fcafile)
                changes=','.join(k+'=?' for k in fields)
                vals=[*fields.values(),sid]
                db.execute('UPDATE rn_season_configuration SET '+changes+' WHERE season_id=?',vals)
                require(db.execute('SELECT changes()').fetchone()[0]==1,
                        'Configurazione RC assente: '+sid)
                report['sites_original_restored'].append(sid)
            db.commit()
        p=temp/'config/processing.json'; j=loadjson(p)
        j.setdefault('processing',{}).setdefault('output',{})['publishToSite']=False
        p.write_text(json.dumps(j,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
        propsfile=temp/'config/recordsnext-gui.properties'
        oldprops=props(old/'config/recordsnext-gui.properties') if (old/'config/recordsnext-gui.properties').is_file() else {}
        srcprops=props(propsfile)
        require(set(ctx['selected'])==set(x.strip() for x in srcprops.get('seasons','').split(',') if x.strip()),
                'La lista elaborabile in properties non corrisponde al manifest sandbox')
        safe_js=str(rc/'data/site-export-staging/publish-disabled')
        gui_updates={'processing.publish':'false','publish.destinationMode':'custom',
                     'publish.customDirectory':safe_js}
        legacy_js=oldprops.get('siteJs','')
        if legacy_js and 'publish-sandbox' not in legacy_js.lower():
            gui_updates['siteJs']=legacy_js
            gui_updates['chooser.lastSiteDirectory']=ntpath.dirname(legacy_js)
        elif 'publish-sandbox' in srcprops.get('siteJs','').lower():
            gui_updates['siteJs']=safe_js
        prop_rewrite(propsfile,gui_updates)
        # Ship UCanAccess with the candidate: no dependency on the old installation at run time.
        legacy_runtime=Path(r'D:\DEV_APPS\RecordsNext2.0\release\RecordsNext_3.2.0_FULL\payload\runtime\ucanaccess')
        runtime_candidates=(test/'runtime/ucanaccess',old/'runtime/ucanaccess',legacy_runtime)
        rt=next((x for x in runtime_candidates if (x/'ucanaccess-2.0.9.5.jar').is_file()),None)
        require(rt is not None,'UCanAccess 2.0.9.5 non trovato (necessario per lanciare RC)')
        if not (temp/'runtime/ucanaccess').exists():
            (temp/'runtime').mkdir(exist_ok=True)
            copyitem(rt,temp/'runtime/ucanaccess',report)
        require(len(list((temp/'runtime/ucanaccess').rglob('*.jar')))>=5,'Runtime UCanAccess incompleto')
        build_launcher(temp)
        # No stage data? Wrong / manual FCA? Invalid output? Abort before exposing RC.
        with ro(temp/'data/database/recordsnext.db') as dbc:
            final=get_rows(dbc)
            require(len(final)==len(ctx['rows']),'Numero stagioni SQLite alterato')
            require(final['2012_2013']['configured_fca_path']==str(fcafile),'FCA 2012/13 sovrascritto!')
            require(all(final[s]['local_site_path']==ctx['originals'][s] for s in ctx['selected']),
                    'Restauro destinazioni per stagione incompleto')
            require(dbc.execute('PRAGMA quick_check').fetchone()[0]=='ok','DB RC corrotto')
            fca_check(dbc,fcafile)
        for n,h in ctx['protected'].items():
            require(digest(temp/'config'/n)==h,'Config protetta diversa: '+n)
        require(digest(temp/'target/RecordsNext.jar')==ctx['jar_sha256'],'JAR differente')
        require(loadjson(temp/'config/processing.json')['processing']['output']['publishToSite'] is False,
                'Flag pubblicazione JS attivo in RC')
        require(props(temp/'config/recordsnext-gui.properties')['processing.publish']=='false',
                'Checkbox pubblicazione RC attiva')
        require(len(list((stage_dst/'js').glob('fcmRecordsNext_*.js')))==339,'Staging RC incompleto')
        report['status']='RC_PREPARATA_NON_RILASCIATA'
        (temp/'REPORT_MIGRAZIONE_RC_v64.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
        temp.rename(rc)
    except BaseException:
        print('ERRORE: nessuna modifica a TEST, 3.2 o siti reali. Cartella incompleta: '+str(temp),file=sys.stderr)
        raise
    print('MIGRAZIONE RC v64 OK:',rc)
    print('STAGIONI:',len(ctx['selected']),'| JS staging preservati: 339 | FCM/FCA gia importati: conservati')
    print('2012/13: FCA G1 RECUPERATA, SHA256',CORRECTED_SHA,'| G1 255 / Totale 9936')
    print('DESTINAZIONI ORIGINALI RESTAURATE nel DB RC:',len(report['sites_original_restored']))
    print('PUBBLICAZIONE RC: DISATTIVATA. 3.2 E SITI REALI: INALTERATI')
    print('AVVIO GUI:',rc/'Avvia-RecordsNext4-RC-v64.ps1')

def main():
    ap=argparse.ArgumentParser()
    ap.add_argument('--mode',choices=('plan','prepare'),default='plan')
    ap.add_argument('--test',type=Path,default=TEST)
    ap.add_argument('--rc',type=Path,default=RC)
    ap.add_argument('--legacy',type=Path,default=OLD)
    ap.add_argument('--corrected-fca',type=Path,default=CORRECTED)
    a=ap.parse_args()
    if a.rc.exists():
        require(a.mode=='plan','La RC esiste gia: non sovrascrivo nulla')
        print('RC GIA ESISTENTE:',a.rc)
        return
    ctx=inspect(a.test,a.rc,a.legacy,a.corrected_fca)
    print('SORGENTE 4.0 TEST:',a.test)
    print('3.2 CONFIGURAZIONE (READ-ONLY):',a.legacy,'righe=',len(ctx['legacy']))
    print('STAGIONI DA PRESERVARE:',len(ctx['selected']))
    print('STAGING SENZA RIELABORAZIONE:',ctx['stage'])
    print('FCA CORRETTO 2012/13:',ctx['fixed']['source_path'])
    print('FCA CORRETTO SHA256:',ctx['fixed']['sha256'])
    print('DESTINAZIONE RC:',a.rc)
    if a.mode=='plan':
        print('ANTEPRIMA OK - nessuna modifica. Eseguire -Mode prepare per creare RC.')
    else:prepare(a.test,a.rc,a.legacy,a.corrected_fca,ctx)

if __name__=='__main__':
    try:main()
    except Exception as e:
        print('STOP MIGRAZIONE v64:',str(e),file=sys.stderr)
        sys.exit(2)
