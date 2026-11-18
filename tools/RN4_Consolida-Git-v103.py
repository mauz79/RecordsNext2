#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""RecordsNext 4.0 v103: version-locked Git consolidation of Windows-verified v102.

Modes: preflight (read-only), stage (whitelist + backup), commit (Maven),
       push (fast-forward only), release (preflight + stage + commit + push).
No writes to production site, database, historical identities, or external plugins.
"""
from __future__ import annotations
import datetime as dt
import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys
import zipfile

PKG=Path(__file__).resolve().parent
MANIFEST=PKG/'MANIFEST_RELEASE_v103.json'
RC=Path(r'D:\DEV_APPS\RecordsNext4.0_RC_v64')
WORK=Path(r'D:\DEV_APPS\downloads\RN4_GITHUB_WORK_v63_20261116_174243')
SITE=Path(r'E:\fantacalcio\Lega2026')
DOWN=Path(r'D:\DEV_APPS\downloads')
STATE=DOWN/'RN4_GITHUB_STATO_v103.json'
INSTALLED=DOWN/'RN4_FRONTEND_STATO_v102.json'
BASE='5c10ec5b729783747c5a8d33f17b5be84f70de74'
BRANCH='feature/recordsnext4-v67-rc-gui-migrated'
ORIGINS={
    'https://github.com/mauz79/RecordsNext2.git',
    'https://github.com/mauz79/RecordsNext2',
    'git@github.com:mauz79/RecordsNext2.git',
    'git@github.com:mauz79/RecordsNext2'
}
V102_FILES=(
    'assets/RecordsNext/calciatori.html',
    'assets/js/fcmRecordsNextCalciatori_v101.js',
    'assets/js/fcmRecordsNextPlayerProfile_v101.js',
    'src/main/java/it/alterlega/recordsnext/app/RecordsNextPipeline.java',
    'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v53.md',
    'docs/CONTINUITA_RECORDSNEXT4_v102.md',
    'docs/CHANGELOG_RECORDSNEXT4_v102.md',
    'docs/TEST_E_GIT_RECORDSNEXT4_v102.md',
    'tools/Genera-Bibbia-RecordsNext-v102.py',
    'tests/regression_v101.js',
)
NEW_FILES=(
    'docs/CONTINUITA_RECORDSNEXT4_v103.md',
    'docs/CHANGELOG_RECORDSNEXT4_v103.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v103.md',
    'docs/TEST_E_GIT_RECORDSNEXT4_v103.md',
    'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v54.md',
    'tools/Genera-Bibbia-RecordsNext-v103.py',
    'tools/RN4_Consolida-Git-v103.py',
)
PRODUCTION_ASSETS={
    'assets/RecordsNext/calciatori.html':'RecordsNext/calciatori.html',
    'assets/js/fcmRecordsNextCalciatori_v101.js':'js/fcmRecordsNextCalciatori_v101.js',
    'assets/js/fcmRecordsNextPlayerProfile_v101.js':'js/fcmRecordsNextPlayerProfile_v101.js',
}


def need(condition, message):
    if not condition:
        raise RuntimeError(message)


def sha(path):
    h=hashlib.sha256()
    with Path(path).open('rb') as f:
        for chunk in iter(lambda:f.read(1<<20),b''):
            h.update(chunk)
    return h.hexdigest()


def stamp():
    return dt.datetime.now().strftime('%Y%m%d_%H%M%S_%f')


def write_json(path, obj):
    path=Path(path)
    path.parent.mkdir(parents=True,exist_ok=True)
    tmp=path.with_name(path.name+'.tmp')
    tmp.write_text(json.dumps(obj,indent=2,ensure_ascii=False)+'\n',encoding='utf-8')
    os.replace(tmp,path)


def run(args, binary=False, check=True, **kwargs):
    cp=subprocess.run(args,stdout=subprocess.PIPE,stderr=subprocess.PIPE,
                      **({} if binary else {'text':True,'encoding':'utf-8','errors':'replace'}),
                      **kwargs)
    if check and cp.returncode:
        err=cp.stderr.decode('utf-8','replace') if binary else cp.stderr
        out=cp.stdout.decode('utf-8','replace') if binary else cp.stdout
        raise RuntimeError('Comando fallito: '+repr(args)+'\n'+(err+'\n'+out)[-2500:])
    return cp


def git(*args, binary=False, check=True):
    return run(['git','-C',str(WORK),*args],binary=binary,check=check)


def gtext(*args):
    return git(*args).stdout.strip()


def names(*args):
    v=gtext(*args)
    return v.splitlines() if v else []


def git_id(expected_head):
    need(WORK.is_dir(),'Worktree Git mancante')
    actual=Path(gtext('rev-parse','--show-toplevel')).resolve()
    need(os.path.normcase(str(actual))==os.path.normcase(str(WORK.resolve())),
         'Repository diverso: '+str(actual))
    branch=gtext('branch','--show-current')
    origin=gtext('remote','get-url','origin')
    head=gtext('rev-parse','HEAD')
    need(branch==BRANCH and origin in ORIGINS and head==expected_head,
         'Git differente dalla baseline v94: branch='+branch+' origin='+origin+' HEAD='+head)
    return {'branch':branch,'origin':origin,'head':head}


def load_manifest():
    obj=json.loads(MANIFEST.read_text(encoding='utf-8'))
    need(obj.get('baseline')==BASE and obj.get('version')=='v103','Manifest v103 non atteso')
    expected=set(V102_FILES)|set(NEW_FILES)
    need(set(obj['files'])==expected and len(expected)==len(V102_FILES)+len(NEW_FILES),
         'Manifest v103 incompleto o lista duplicata')
    for rel in expected:
        need('..' not in Path(rel).parts and not Path(rel).is_absolute(),'Percorso non sicuro: '+rel)
    return obj['files']


def check_sources():
    files=load_manifest()
    need(RC.is_dir() and SITE.is_dir() and DOWN.is_dir(),'RC/SITO/DOWNLOADS assente')
    need(INSTALLED.is_file(),'Stato v102 non trovato: '+str(INSTALLED))
    st=json.loads(INSTALLED.read_text(encoding='utf-8'))
    need(st.get('status')=='INSTALLED' and len(st.get('entries',[]))==13,
         'v102 non risulta installata e verificabile')
    jar=RC/'target'/'RecordsNext.jar'
    need(jar.is_file() and st.get('jar_after')==sha(jar),
         'JAR RC diverso da quello installato dalla v102')
    for rel in V102_FILES:
        source=RC/rel
        expected=files[rel]
        need(source.is_file() and sha(source)==expected,
             'Sorgente RC v102 divergente: '+rel)
        pkg_copy=PKG/'payload'/rel
        need(pkg_copy.is_file() and sha(pkg_copy)==expected,
             'Payload v103 divergente: '+rel)
    for rel in NEW_FILES:
        source=PKG/rel
        need(source.is_file() and sha(source)==files[rel],
             'Documento v103 non integro: '+rel)
    for rel,site_rel in PRODUCTION_ASSETS.items():
        target=SITE/site_rel
        need(target.is_file() and sha(target)==files[rel],
             'Asset produzione v102 differente, STOP: '+site_rel)
    return files


def ensure_clean():
    v=gtext('status','--porcelain','--untracked-files=normal')
    need(not v,'Worktree Git con modifiche preesistenti, STOP: '+v[:600])


def preflight():
    files=check_sources()
    meta=git_id(BASE)
    ensure_clean()
    # Refuse to overwrite divergent new docs even if not tracked by Git.
    for rel in NEW_FILES:
        for dest in (RC/rel,WORK/rel):
            if dest.exists():
                need(dest.is_file() and not dest.is_symlink() and sha(dest)==files[rel],
                     'Esiste documento v103 diverso: '+str(dest))
    for rel in V102_FILES:
        expected=files[rel]
        # Originals may differ in Git (new version), but should have exactly one source file.
        need((RC/rel).is_file() and sha(RC/rel)==expected,'File RC non stabile: '+rel)
    p=DOWN/'RN4_GITHUB_PREFLIGHT_v103.json'
    write_json(p,dict(status='READY',baseline=BASE,files=len(files),windows_v102='VERIFIED_BY_STATE',**meta))
    print('RN4_V103_PREFLIGHT_READY',len(files),'file; baseline=',BASE)
    print('RN4_V103_RAPPORTO',p)
    return files


def source_for(rel):
    return (RC/rel) if rel in V102_FILES else (PKG/rel)


def stage():
    files=preflight()
    backup=DOWN/('RN4_BACKUP_GITHUB_v103_'+stamp())
    backup.mkdir(parents=True,exist_ok=False)
    backed=[]
    dests=[]
    for rel in sorted(files):
        dests.append(('GIT',WORK/rel,rel))
        if rel in NEW_FILES:
            dests.append(('RC',RC/rel,rel))
    for scope,dest,rel in dests:
        need(not dest.is_symlink() and (not dest.exists() or dest.is_file()),
             'Target non sicuro: '+str(dest))
        if dest.is_file():
            backup_dest=backup/scope/rel
            backup_dest.parent.mkdir(parents=True,exist_ok=True)
            shutil.copy2(dest,backup_dest)
            need(sha(backup_dest)==sha(dest),'Backup non valido: '+str(dest))
            backed.append({'scope':scope,'path':rel,'sha':sha(dest)})
    write_json(backup/'MANIFEST_BACKUP_v103.json',{'existing':backed,'files':len(files),'base_git':BASE})
    try:
        for scope,dest,rel in dests:
            src=source_for(rel)
            if not dest.is_file() or sha(dest)!=files[rel]:
                dest.parent.mkdir(parents=True,exist_ok=True)
                shutil.copy2(src,dest)
                need(sha(dest)==files[rel],'Errore SHA dopo copia: '+rel)
        git('-c','core.autocrlf=false','add','--',*sorted(files))
        verify_stage(files)
    except Exception:
        # Git index reset to baseline; restore only files modified in this operation.
        git('reset','-q','--',*sorted(files),check=False)
        for scope,dest,rel in reversed(dests):
            src=backup/scope/rel
            if src.is_file():
                dest.parent.mkdir(parents=True,exist_ok=True)
                shutil.copy2(src,dest)
            elif dest.is_file() and sha(dest)==files[rel]:
                dest.unlink()
        raise
    state={'status':'STAGED','base':BASE,'paths':sorted(files),
           'count':len(files),'backup':str(backup),'at':stamp()}
    write_json(STATE,state)
    print('RN4_V103_STAGE_OK',len(files),'file; backup=',backup)


def verify_stage(files):
    git_id(BASE)
    unstaged=names('diff','--name-only')
    untracked=names('ls-files','--others','--exclude-standard')
    need(not unstaged and not untracked,'Worktree contiene modifiche non staged: '+str(unstaged+untracked)[:700])
    staged=set(names('diff','--cached','--name-only'))
    need(staged==set(files),
         'Stage Git non corrisponde alla whitelist v103: staged='+str(len(staged))+' attesi='+str(len(files)))
    for rel,digest in files.items():
        data=git('show',':'+rel,binary=True).stdout
        need(hashlib.sha256(data).hexdigest()==digest,'SHA oggetto Git diverso: '+rel)
        need((WORK/rel).is_file() and sha(WORK/rel)==digest,'SHA file Git diverso: '+rel)
    cp=git('diff','--cached','--check',check=False)
    need(cp.returncode==0,'git diff --cached --check fallito: '+cp.stdout+cp.stderr)


def commit():
    need(STATE.is_file(),'Stage v103 non presente; eseguire stage o release')
    state=json.loads(STATE.read_text(encoding='utf-8'))
    need(state.get('status')=='STAGED' and state.get('base')==BASE,
         'Stato v103 non pronto per il commit')
    files=check_sources()
    verify_stage(files)
    need((WORK/'mvnw.cmd').is_file(),'Maven Wrapper mancante nel worktree Git')
    log=DOWN/'RN4_GITHUB_MAVEN_v103.txt'
    command=['cmd.exe','/d','/c','mvnw.cmd','-B','-ntp','clean','test','package']
    with log.open('w',encoding='utf-8') as f:
        f.write('COMMAND: mvnw.cmd -B -ntp clean test package\n')
        f.flush()
        try:
            cp=subprocess.run(command,cwd=WORK,stdout=f,stderr=subprocess.STDOUT,timeout=1200)
        except subprocess.TimeoutExpired:
            raise RuntimeError('Maven ha superato il timeout, nessun commit: '+str(log))
    need(cp.returncode==0,'Maven FALLITO nel worktree, commit non eseguito. Log: '+str(log))
    result=log.read_text(encoding='utf-8',errors='replace')
    need('BUILD SUCCESS' in result,'Build con exit 0 ma senza marker BUILD SUCCESS: '+str(log))
    need('Tests run:' in result,'Build priva di prove Maven nel log: '+str(log))
    verify_stage(files)
    git('commit','-m','RecordsNext4 v103: publisher v102 verificato e Bibbia v54')
    current=gtext('rev-parse','HEAD')
    need(gtext('rev-parse','HEAD^')==BASE,'Parent Git inatteso dopo commit; NON fare push')
    ensure_clean()
    state.update(status='COMMITTED_NOT_PUSHED',head=current,at_commit=stamp())
    write_json(STATE,state)
    print('RN4_V103_COMMIT_OK',current)
    print('RN4_V103_PUSH_NON_ESEGUITO')


def push():
    need(STATE.is_file(),'Stato Git v103 assente')
    state=json.loads(STATE.read_text(encoding='utf-8'))
    need(state.get('status') in ('COMMITTED_NOT_PUSHED','PUSH_VERIFIED'),
         'Commit v103 non verificato; nessun push')
    head=state['head']
    meta=git_id(head)
    need(gtext('rev-parse','HEAD^')==BASE,'Parent diverso da v94, nessun push')
    ensure_clean()
    remote=gtext('ls-remote','--heads','origin',BRANCH).splitlines()
    need(len(remote)==1 and len(remote[0].split())>=2,'Impossibile leggere branch remoto')
    remote_head=remote[0].split()[0]
    need(remote_head in (BASE,head),'Branch remoto cambiato: '+remote_head+'. Nessun force-push.')
    if remote_head==BASE:
        git('push','origin','HEAD:refs/heads/'+BRANCH)
    remote_after=gtext('ls-remote','--heads','origin',BRANCH).split()[0]
    need(remote_after==head,'Hash remoto diverso dopo push: '+remote_after)
    state.update(status='PUSH_VERIFIED',remote_head=remote_after,at_push=stamp())
    write_json(STATE,state)
    result=DOWN/'RN4_GITHUB_RISULTATO_v103.json'
    write_json(result,{
        'status':'PUSH_VERIFIED','branch':BRANCH,'origin':meta['origin'],
        'base':BASE,'head':head,'remote_head':remote_after,
        'files_changed':state['count'],'timestamp':stamp(),
        'production_writes':0,'database_writes':0,'identities_writes':0,
        'maven_log':str(DOWN/'RN4_GITHUB_MAVEN_v103.txt')
    })
    zpath=DOWN/('RN4_ESITO_GITHUB_v103_'+stamp()+'.zip')
    with zipfile.ZipFile(zpath,'w',zipfile.ZIP_DEFLATED,compresslevel=8) as z:
        for src,arc in ((STATE,'RN4_GITHUB_STATO_v103.json'),
                        (result,'RN4_GITHUB_RISULTATO_v103.json'),
                        (DOWN/'RN4_GITHUB_MAVEN_v103.txt','RN4_GITHUB_MAVEN_v103.txt'),
                        (DOWN/'RN4_GITHUB_PREFLIGHT_v103.json','RN4_GITHUB_PREFLIGHT_v103.json')):
            if src.is_file():z.write(src,arc)
        for rel in ('docs/CONTINUITA_RECORDSNEXT4_v103.md',
                    'docs/CHANGELOG_RECORDSNEXT4_v103.md',
                    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v103.md',
                    'docs/TEST_E_GIT_RECORDSNEXT4_v103.md'):
            z.write(PKG/rel,rel)
    print('RN4_V103_PUSH_VERIFIED',remote_after)
    print('RN4_V103_ALLEGA',zpath)


def main():
    if len(sys.argv)!=2 or sys.argv[1] not in ('preflight','stage','commit','push','release'):
        print('Uso: python RN4_Consolida-Git-v103.py {preflight|stage|commit|push|release}',file=sys.stderr)
        return 2
    try:
        action=sys.argv[1]
        if action=='preflight':preflight()
        elif action=='stage':stage()
        elif action=='commit':commit()
        elif action=='push':push()
        elif action=='release':stage();commit();push()
        return 0
    except Exception as exc:
        print('RN4_V103_BLOCCATO',str(exc),file=sys.stderr)
        return 3

if __name__=='__main__':
    sys.exit(main())
