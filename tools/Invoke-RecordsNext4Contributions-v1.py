#!/usr/bin/env python3
"""Install a verified TopFlop contribution patch in a RecordsNext4 worktree.
No DB writes, no CSS/publisher/GUI changes, no arbitrary source overwrites.
"""
from __future__ import annotations
import argparse
from datetime import datetime
import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys
import traceback
import zipfile

KIT=Path(__file__).resolve().parent.parent
ASM='src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopJdbcArchiveAssembler.java'
PATCH=[
 ASM,
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopContributionSummary.java',
 'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopContributionReport.java',
 'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopContributionSmokeTest.java',
 'docs/SPECIFICA_APPORTO_TABELLINO_TOP_FLOP_v1.md',
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v23.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v22.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v22.md',
 'tools/Create-RecordsNext4WorkingCodeMd-v22.py',
 'tools/Invoke-RecordsNext4Contributions-v1.py',
]
PREVIOUS_ASM_SHA=''

def sha(path:Path)->str:return hashlib.sha256(path.read_bytes()).hexdigest()

def run(args:list[str|Path], log:list[str])->str:
    c=[str(a) for a in args]
    r=subprocess.run(c,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True,encoding='utf-8',errors='replace')
    log.append('COMMAND: '+str(Path(c[0]).name)+' [arguments redacted, see source runner]')
    log.append(r.stdout[-30000:])
    if r.returncode:raise RuntimeError('Command failed ('+str(r.returncode)+'): '+r.stdout[-5000:])
    return r.stdout

def main()->int:
    p=argparse.ArgumentParser()
    p.add_argument('--project-dir',default=r'D:\DEV_APPS\RecordsNext4.0')
    p.add_argument('--downloads-dir',default=r'D:\DEV_APPS\downloads')
    p.add_argument('--database')
    p.add_argument('--jar')
    args=p.parse_args()
    root=Path(args.project_dir).resolve()
    downloads=Path(args.downloads_dir).resolve()
    downloads.mkdir(parents=True,exist_ok=True)
    stamp=datetime.now().strftime('%Y%m%d_%H%M%S_%f')
    report=downloads/('RecordsNext4_Contributions_OUTPUT_v1_'+stamp+'.zip')
    temp=downloads/('RecordsNext4_Contributions_BUILD_v1_'+stamp)
    state={'status':'ERROR','installed':[],'identical':[],'checks':[], 'database':None,'previewCsv':None}
    logs=[]; changed=[]; backups={}; originally_there=set()
    try:
        if not (root/'pom.xml').is_file() or not (root/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v21.md').is_file():
            raise RuntimeError('Worktree 4.0 non conforme: pom.xml o bibbia v21 assente')
        if (root/'.git').exists() and shutil.which('git'):
            branch=run(['git','-C',root,'branch','--show-current'],logs).strip()
            if branch != 'feature/recordsnext4-players':
                raise RuntimeError('Ramo Git inatteso: '+branch)
        manifest=json.loads((KIT/'manifest-sha256.json').read_text(encoding='utf-8'))
        # Preflight the WHOLE patch before touching the worktree.
        for rel in PATCH:
            src=KIT/rel;dst=root/rel
            if not src.is_file():raise RuntimeError('File del kit mancante: '+rel)
            if dst.exists():
                originally_there.add(rel)
                if not dst.is_file():raise RuntimeError('Percorso destinazione non file: '+rel)
                if sha(dst)==sha(src):state['identical'].append(rel)
                elif rel==ASM and sha(dst)==manifest['expected_v21_assembler_sha256']:
                    backups[rel]=dst.read_bytes()
                else:raise RuntimeError('Collisione: file esistente diverso, non sovrascrivo: '+rel)
        java=shutil.which('java');javac=shutil.which('javac')
        if not java or not javac:raise RuntimeError('JDK 21 richiesto in PATH (java, javac)')
        db=Path(args.database) if args.database else next(iter(sorted(
            (root/'data/database').glob('recordsnext-2026_2027-verifica-v1_*.db'))),None)
        if db is None or not db.is_file():
            raise RuntimeError('Database di test 2026/27 aggiornato non trovato in RecordsNext4.0/data/database; usa --database')
        state['database']=str(db)
        db_hash=sha(db)
        jar=Path(args.jar) if args.jar else next((candidate for candidate in [
            root/'target/RecordsNext.jar',
            root.parent/'RecordsNext2.0/target/RecordsNext.jar',
            root.parent/'RecordsNext2.0/release/RecordsNext_3.2.0_FULL/payload/RecordsNext.jar'
        ] if candidate.is_file()),None)
        if jar is None:raise RuntimeError('JAR ufficiale RecordsNext 3.2 non trovato; usa --jar')
        classes=temp/'classes';classes.mkdir(parents=True,exist_ok=True)
        replacing={ASM, *[x for x in PATCH if x.startswith('src/main/java/')]}
        sources=[x for x in (root/'src/main/java').rglob('*.java')
                 if x.relative_to(root).as_posix() not in replacing]
        sources += [KIT/x for x in PATCH if x.startswith('src/main/java/')]
        run([javac,'--release','21','-encoding','UTF-8','-d',classes,*sources],logs)
        state['checks'].append('Compilazione di TUTTI i sorgenti Java + contributi OK')
        tests=[root/'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopArchiveSmokeTest.java',
               KIT/'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopContributionSmokeTest.java']
        run([javac,'--release','21','-encoding','UTF-8','-cp',classes,'-d',classes,*tests],logs)
        r=run([java,'-ea','-cp',classes,
            'it.alterlega.recordsnext.app.topflop.TopFlopArchiveSmokeTest'],logs)
        if 'checks=52' not in r:raise RuntimeError('Firma di regressione 52 mancante')
        state['checks'].append('Regressione 52 controlli OK')
        r=run([java,'-ea','-cp',str(classes)+os.pathsep+str(jar),
            'it.alterlega.recordsnext.app.topflop.TopFlopContributionSmokeTest',db],logs)
        if '2026_2027 counted=3327' not in r or 'TOP_FLOP_CONTRIBUTION_OK' not in r:
            raise RuntimeError('Test FCM aggiornato 2026 incompleto: '+r[-1000:])
        state['checks'].append('Test 2006/07 e 2026/27: apporti tabellino OK')
        preview=temp/'Lautaro_2026_2027_apporto_v1.csv'
        r=run([java,'-ea','-cp',str(classes)+os.pathsep+str(jar),
            'it.alterlega.recordsnext.app.topflop.TopFlopContributionReport',
            db,'2026_2027','100843',preview],logs)
        if 'groups=9' not in r:raise RuntimeError('Report Lautaro: attesi 9 contesti aggregati')
        state['checks'].append('CSV reale Lautaro (3 fantasquadre, 9 gruppi) OK')
        if sha(db)!=db_hash:raise RuntimeError('Il database di test e cambiato durante la lettura')
        state['checks'].append('SHA-256 database invariato')
        # No patch may be installed if its backup would overwrite another backup.
        backup=root/'backups/topflop-contributions-v22'/ASM
        if ASM in backups and backup.exists() and backup.read_bytes()!=backups[ASM]:
            raise RuntimeError('Backup non coincidente: '+str(backup))
        if ASM in backups and not backup.exists():
            backup.parent.mkdir(parents=True,exist_ok=True)
            with backup.open('xb') as h:h.write(backups[ASM])
        bible=root/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v22.md'
        had_bible=bible.exists()
        try:
            for rel in PATCH:
                src=KIT/rel;dst=root/rel
                if dst.is_file() and sha(dst)==sha(src):continue
                dst.parent.mkdir(parents=True,exist_ok=True)
                dst.write_bytes(src.read_bytes())
                changed.append(rel)
            run([sys.executable,root/'tools/Create-RecordsNext4WorkingCodeMd-v22.py',
                '--project-dir',root],logs)
        except Exception:
            for rel in reversed(changed):
                dst=root/rel
                if rel in backups:dst.write_bytes(backups[rel])
                elif rel not in originally_there:dst.unlink(missing_ok=True)
            if not had_bible:bible.unlink(missing_ok=True)
            raise
        state['installed']=changed.copy()
        state['checks'].append('Bibbia v22 generata con codice e SHA-256')
        state['status']='OK'
        state['previewCsv']='Lautaro_2026_2027_apporto_v1.csv'
        print('ESITO: OK')
        print('APPORTI 2026/27: 3327; 2006/07: 2585')
        print('REGRESSIONE: 52 controlli OK')
        print('FILE INSTALLATI:',len(changed))
        print('BIBBIA: docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v22.md')
    except Exception as ex:
        state['error']=repr(ex)
        logs.append(traceback.format_exc())
        print('ESITO: ERRORE',file=sys.stderr)
        print(str(ex),file=sys.stderr)
    finally:
        with zipfile.ZipFile(report,'w',zipfile.ZIP_DEFLATED) as archive:
            archive.writestr('result.json',json.dumps(state,ensure_ascii=False,indent=2))
            archive.writestr('execution.txt','\n'.join(logs))
            if (temp/'Lautaro_2026_2027_apporto_v1.csv').is_file():
                archive.write(temp/'Lautaro_2026_2027_apporto_v1.csv','Lautaro_2026_2027_apporto_v1.csv')
        shutil.rmtree(temp,ignore_errors=True)
        print('ZIP DA CARICARE:',report)
    return 0 if state['status']=='OK' else 1
if __name__=='__main__':sys.exit(main())
