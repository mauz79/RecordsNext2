#!/usr/bin/env python3
"""Install only new versioned files after complete Java/source tests; no DB writes."""
from __future__ import annotations
import argparse
from datetime import datetime
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
import traceback
from zipfile import ZipFile, ZIP_DEFLATED

NEW_FILES = [
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopJdbcArchiveAssembler.java',
    'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopJdbcArchiveAssemblerSmokeTest.java',
    'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopJdbcArchive2026SmokeTest.java',
    'docs/SPECIFICA_ASSEMBLATORE_JAVA_TOP_FLOP_v1.md',
    'docs/VERIFICA_ASSEMBLATORE_JAVA_TOP_FLOP_v1.md',
    'docs/NOTA_FCM_GIORNATE_MERCATO_2026_2027_v1.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v19.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v18.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v18.md',
    'tools/Create-RecordsNext4WorkingCodeMd-v18.py',
    'tools/Invoke-RecordsNext4ArchiveBridge-v2.py',
]
REQUIRED = [
    'pom.xml', 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v17.md',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopFcaJdbcReader.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopFcmJdbcReader.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopArchive.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopFacts.java',
    'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopArchiveSmokeTest.java',
    'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopFcaJdbcSmokeTest.java',
    'src/test/java/it/alterlega/recordsnext/app/topflop/TopFlopFcmJdbcSmokeTest.java',
]
PRESERVED = [
    'pom.xml',
    'src/main/java/it/alterlega/recordsnext/app/model/RecordFamily.java',
    'src/main/java/it/alterlega/recordsnext/app/model/CoreRecordCatalog.java',
    'src/main/java/it/alterlega/recordsnext/app/RecordsNextPipeline.java',
    'src/main/java/it/alterlega/recordsnext/Records2026SitePublisher.java',
    'src/main/java/it/alterlega/recordsnext/app/output/SeasonFamilyShardPublisher.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopFcaJdbcReader.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopFcmJdbcReader.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopArchive.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopFacts.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopMetrics.java',
    'src/main/java/it/alterlega/recordsnext/app/topflop/TopFlopCompetitionPolicy.java',
]

def sha(path: Path) -> str:
    h=hashlib.sha256()
    with path.open('rb') as stream:
        for block in iter(lambda: stream.read(1048576),b''):
            h.update(block)
    return h.hexdigest()

def run(cmd: list[str]) -> tuple[int,str]:
    proc=subprocess.run(cmd,capture_output=True,text=True,encoding='utf-8',errors='replace',
                        env=dict(os.environ,PYTHONDONTWRITEBYTECODE='1'),timeout=120)
    return proc.returncode,proc.stdout+proc.stderr

def git(root: Path,*args:str) -> str:
    code,msg=run(['git','-C',str(root),*args])
    if code: raise RuntimeError('Git: '+msg[-1500:])
    return msg.strip()

def shaded_jar(paths:list[Path]) -> Path:
    for path in paths:
        if not path.is_file():continue
        with ZipFile(path) as z:
            if 'org/sqlite/JDBC.class' in z.namelist():return path
    raise RuntimeError('JAR 3.2 con sqlite-jdbc non trovato')

def main() -> int:
    parser=argparse.ArgumentParser()
    parser.add_argument('--work-dir',type=Path,default=Path('D:/DEV_APPS/RecordsNext4.0'))
    parser.add_argument('--base-dir',type=Path,default=Path('D:/DEV_APPS/RecordsNext2.0'))
    parser.add_argument('--output-dir',type=Path,default=Path('D:/DEV_APPS/downloads'))
    parser.add_argument('--jar',type=Path,default=None)
    parser.add_argument('--updated-db',type=Path,default=None,help='Copia 2026/27 importata dalla 3.2')
    parser.add_argument('--local-smoke',action='store_true',help='Solo test rapido di packaging nel laboratorio')
    args=parser.parse_args()
    kit=Path(__file__).resolve().parent.parent
    output=args.output_dir.resolve(strict=True)
    stamp=datetime.now().strftime('%Y%m%d_%H%M%S_%f')
    result=output/('RecordsNext4_ArchiveBridge_OUTPUT_v2_'+stamp+'.zip')
    build=output/('RecordsNext4_ArchiveBridge_BUILD_v2_'+stamp)
    report={'phase':'TopFlop-ArchiveBridge-Java-v2','status':'ERROR','bible':'v18',
            'installedFiles':[],'published':False,'databaseModified':None}
    logs=[]
    work=None
    success=False
    try:
        work=args.work_dir.resolve(strict=True)
        base=args.base_dir.resolve(strict=True)
        if work==base or work==kit: raise RuntimeError('Cartelle progetto 4.0 e 3.2/kit devono essere distinte')
        for name in REQUIRED+PRESERVED:
            if not (work/name).is_file():raise RuntimeError('Prerequisito mancante: '+name)
        if Path(git(work,'rev-parse','--show-toplevel')).resolve(strict=True)!=work:
            raise RuntimeError('Worktree 4.0 non coincide con la root Git')
        branch=git(work,'branch','--show-current')
        if branch!='feature/recordsnext4-players':raise RuntimeError('Branch inatteso: '+branch)
        head=git(work,'rev-parse','HEAD')
        report.update(branch=branch,head=head)
        preserved={p:sha(work/p) for p in PRESERVED}
        report['preservedSha256Before']=preserved
        for name in NEW_FILES:
            source,dest=kit/name,work/name
            if not source.is_file():raise RuntimeError('Kit incompleto: '+name)
            if dest.exists() and (not dest.is_file() or sha(dest)!=sha(source)):
                raise RuntimeError('File esistente DIVERSO, non sovrascrivo: '+name)
        dbpaths=[base/'data/database/recordsnext.db',base/'data/database/recordsnext-sandbox.db']
        if not all(p.is_file() for p in dbpaths):raise RuntimeError('Uno dei due database 3.2 non esiste')
        before={p.name:sha(p) for p in dbpaths}
        report['databaseSha256Before']=before
        jar=args.jar.resolve(strict=True) if args.jar else shaded_jar([
            base/'release/RecordsNext_3.2.0_FULL/payload/RecordsNext.jar',
            base/'target/RecordsNext.jar',work/'target/RecordsNext.jar'])
        if not jar.is_file():raise RuntimeError('JAR mancante: '+str(jar))
        java,javac=shutil.which('java'),shutil.which('javac')
        if not java or not javac:raise RuntimeError('Java e javac devono essere nel PATH')
        rc,msg=run([javac,'-version'])
        logs.append(('JDK',rc,msg))
        m=re.search(r'\bjavac\s+(\d+)(?:\.|\s|$)',msg)
        if rc or not m or int(m.group(1))<21:raise RuntimeError('Serve JDK 21+: '+msg)
        build.mkdir(exist_ok=False)
        addjava=sorted((kit/'src/main/java').rglob('*.java'))
        addtest=sorted((kit/'src/test/java').rglob('*.java'))
        addedpaths={p.relative_to(kit).as_posix() for p in addjava}
        existing=sorted(p for p in (work/'src/main/java').rglob('*.java')
                        if p.relative_to(work).as_posix() not in addedpaths)
        if len(addjava)!=1 or len(addtest)!=2 or len(existing)<70:
            raise RuntimeError('Numero sorgenti Java inatteso')
        testnames=['TopFlopArchiveSmokeTest.java','TopFlopFcaJdbcSmokeTest.java',
                   'TopFlopFcmJdbcSmokeTest.java']
        existingtests=[work/'src/test/java/it/alterlega/recordsnext/app/topflop'/n for n in testnames]
        sources=[str(p) for p in existing+addjava+existingtests+addtest]
        rc,msg=run([javac,'-encoding','UTF-8','--release','21','-cp',str(jar),
                    '-d',str(build),*sources])
        logs.append(('COMPILA_COMPLETA',rc,msg))
        print('Compilazione Java:', 'OK' if not rc else 'ERRORE',flush=True)
        if rc:raise RuntimeError('Java: '+msg[-3000:])
        cp=os.pathsep.join((str(build),str(jar)))
        # FCA/FCM source adapters were already validated in previous Windows kits.
        # This patch tests the new assembler and its existing model integration.
        regressions = [
            ('REGRESSIONE_52','TopFlopArchiveSmokeTest',[], 'TOP_FLOP_JAVA_TEST_OK checks=52')]
        for tag,clazz,arglist,marker in ([] if args.local_smoke else regressions):
            rc,msg=run([java,'-Xmx2048m','-ea','-cp',cp,
                        'it.alterlega.recordsnext.app.topflop.'+clazz,*arglist])
            logs.append((tag,rc,msg))
            if rc or marker not in msg:raise RuntimeError(tag+': '+msg[-2500:])
            print(tag+': OK',flush=True)
        clazz='it.alterlega.recordsnext.app.topflop.TopFlopJdbcArchiveAssemblerSmokeTest'
        # One baseline with repeated Tesserato; refreshed 2026 test below covers a second snapshot.
        # Separate JVM per database to avoid state carry-over and bound runtime.
        db_cases=[] if args.local_smoke else [
            ('ASSEMBLER_2006', dbpaths[1], '--season=2006_2007',
             'TOP_FLOP_ASSEMBLER_OK seasons=1'),
        ]
        for tag,db,opt,marker in db_cases:
            cli=[java,'-Xmx2048m','-ea','-cp',cp,clazz,str(db)]
            if opt:cli.append(opt)
            rc,msg=run(cli)
            logs.append((tag,rc,msg))
            if rc or marker not in msg:
                raise RuntimeError(tag+': '+msg[-2500:])
            print(tag+': OK',flush=True)
        # The refreshed source is a separate RAW import created by the working
        # RecordsNext 3.2 importer. Never overwrite it or the 3.2 databases.
        if not args.local_smoke:
            if args.updated_db:
                updated=args.updated_db.resolve(strict=True)
            else:
                matches=sorted((work/'data/database').glob('recordsnext-2026_2027-verifica-v1_*.db'),
                               key=lambda path: (path.stat().st_mtime, path.name))
                if not matches:
                    raise RuntimeError('DB 2026/27 aggiornato non trovato in worktree/data/database. '
                        'Usa --updated-db con il file prodotto dalla verifica/import originale 3.2.')
                updated=matches[-1]
            if not updated.is_file() or updated in dbpaths:
                raise RuntimeError('Lo snapshot aggiornato deve essere un file DB separato dai database 3.2')
            # This smoke test is tied intentionally to the supplied 2026/27 snapshot;
            # it detects when a different or later snapshot is provided.
            updated_hash=sha(updated)
            report['updatedDatabase']={'path':str(updated),'sha256Before':updated_hash}
            rc,msg=run([java,'-Xmx2048m','-ea','-cp',cp,
                'it.alterlega.recordsnext.app.topflop.TopFlopJdbcArchive2026SmokeTest',
                str(updated)])
            logs.append(('ASSEMBLER_2026_AGGIORNATO',rc,msg))
            if rc or 'TOP_FLOP_2026_UPDATED_OK realDays=28956' not in msg:
                raise RuntimeError('Test snapshot 2026/27 aggiornato: '+msg[-2500:])
            report['updatedDatabase']['sha256After']=sha(updated)
            if sha(updated)!=updated_hash:raise RuntimeError('Il DB test aggiornato e stato modificato')
            print('ASSEMBLER_2026_AGGIORNATO: OK',flush=True)
        after={p.name:sha(p) for p in dbpaths}
        report['databaseSha256After']=after
        report['databaseModified']=(after!=before)
        if after!=before:raise RuntimeError('Hash dei database cambiato!')
        for name in NEW_FILES:
            src,dst=kit/name,work/name
            if dst.exists(): print('GIA PRESENTE IDENTICO:',name,flush=True)
            else:
                dst.parent.mkdir(parents=True,exist_ok=True)
                with dst.open('xb') as stream:stream.write(src.read_bytes())
                report['installedFiles'].append(name)
                print('INSTALLATO:',name,flush=True)
        rc,msg=run([sys.executable,str(work/'tools/Create-RecordsNext4WorkingCodeMd-v18.py'),
                    '--project-dir',str(work)])
        logs.append(('BIBBIA_V18',rc,msg))
        if rc:raise RuntimeError('Bibbia v18: '+msg[-1800:])
        print('Bibbia v18: OK',flush=True)
        if {p:sha(work/p) for p in PRESERVED}!=preserved:
            raise RuntimeError('File originale del progetto modificato!')
        report['preservedSha256After']={p:sha(work/p) for p in PRESERVED}
        report['bibleSha256']=sha(work/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v18.md')
        report['coverage']='local-smoke' if args.local_smoke else 'full'
        report['status']='OK'
        success=True
    except Exception as exc:
        report['error']=str(exc)
        report['trace']=traceback.format_exc()[-3000:]
        print('ERRORE:',str(exc),file=sys.stderr,flush=True)
    finally:
        shutil.rmtree(build,ignore_errors=True)
        try:
            with ZipFile(result,'w',compression=ZIP_DEFLATED,compresslevel=8) as z:
                z.writestr('MANIFEST.json',json.dumps(report,ensure_ascii=False,indent=2))
                for tag,code,log in logs:z.writestr('log/'+tag+'.txt','exit_code='+str(code)+'\n'+log)
                for name in NEW_FILES:
                    p=work/name if work and (work/name).is_file() else kit/name
                    if p.is_file():z.write(p,name)
                if work:
                    bible=work/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v18.md'
                    if bible.is_file():z.write(bible,'docs/'+bible.name)
        except Exception as exc:
            print('ERRORE ZIP diagnostico:',exc,file=sys.stderr,flush=True)
            return 1
        print('ESITO:','OK' if success else 'ERRORE',flush=True)
        print('ZIP DA CARICARE:',result,flush=True)
    return 0 if success else 1

if __name__=='__main__':sys.exit(main())
