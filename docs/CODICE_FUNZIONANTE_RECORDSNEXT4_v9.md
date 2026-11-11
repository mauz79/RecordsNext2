# RecordsNext 4.0 - Codice funzionante / bibbia incrementale v9

Data: 2026-11-01. Fase 3B2, censimento regolamentare, non release.
Le bibbie 3.2 e 4.0 v3--v8 restano intatte.

## Provenienza

- Branch: `feature/recordsnext4-players`
- Commit HEAD: `f9bf30323d9204d70e91e242bddfc72c2060ecf9`
- SHA-256 bibbia base 3.2: `91ecb0006d6d1c6fda501c7243ba65ec2f0625c40e93394079238eac558a7da3`
- SHA-256 bibbia precedente v8: `077861d414b7ff6cb86058a6222219ac901685513df6447a06cf7011085c58d2`
- Fase 3B1 Windows: 7 test, 21 stagioni, 22.265 tabellini, 552.025 Formazione, hash rapporti verificati.
- Fase 3B2 locale: 8 test, 180 configurazioni Competizione, 22.265 tabellini.
- Nessun punto recuperabile calcolato; nessuna scrittura database.

## Nuovi file integri (testo integrale e SHA-256)

### `docs/PIANO_RECORDSNEXT4_GIOCATORI_v10.md`

SHA256: `b89c11d4f54f748fa840fd0ac1e9fea7a73710fc6cc207994f39edb73d2a3d3c`

~~~~markdown
# RecordsNext 4.0 - Piano operativo v10

Data: 2026-11-01. Base produttiva 3.2.0 immutata. Branch `feature/recordsnext4-players`; commit di riferimento `f9bf30323d9204d70e91e242bddfc72c2060ecf9`.

## Fasi completate e evidenze

- Fase 0: identita e fonti; eccezioni sui codici storici ancora aperte.
- Fase 1: estrattore FCA sperimentale; output Windows verificato, 7 test. Non integrato nel Java.
- Fase 2: estrattore FCM fantasy sperimentale; output Windows verificato, 8 test. Non integrato nel Java.
- Fase 3A: audit stati `Formazione`, output Windows verificato, 9 test.
- Fase 3B1: audit `Tabellino.LISTA` + `Formazione`, output Windows `RecordsNext4_Fase3B1_OUTPUT_v1_20261101_001744_607896.zip` verificato: 7 test, 21 stagioni, 22.265 tabellini, 552.025 righe `Formazione`, tre SHA-256 dei report corretti, sandbox invariato, bibbia v8 generata. Non equivale a prova definitiva di voto conteggiato.
- Fase 3B2: censimento osservazionale delle regole `Competizione` per stagione e competizione, legame `Incontro.IDGIRONE -> Girone.IDCOMPETIZIONE`, moduli ammessi e tabellini osservati. Strumento prototipo con 8 test locali; lo ZIP Windows deve ancora certificarne l'esecuzione sul PC.

## Passaggi successivi

- Fase 3B3: confrontare configurazioni di competizione, tabellini reali, RU, sostituzioni, eventuali voti speciali e regole per ruolo. Stabilire cosa significa realmente `conteggiato`, `disponibile`, `non sfruttato`, `recuperabile`; distinguere prove osservate, inferenze e casi non simulabili.
- Fase 3C: solo dopo approvazione della semantica: prototipo per metriche individuali fantasy (panchina non impiegata, fuori formazione, sostituzioni, eventuali controfattuali), test su casi reali verificati.
- Fase 4: integrare fonti canoniche in SQLite/Java, esportazione JS stagionale/sito, HTML e GUI. Tutte le componenti richieste fanno parte del traguardo 4.0.0.
- Fase 5: regressioni, documentazione, commit, release 4.0.0.

## Vincoli costanti

1. Prestazione FCA unica per giocatore storico, stagione, giornata: non duplicare se usata in competizioni fantasy diverse.
2. `GiocatoreA.ID` e annuale; `CodiceGazza` contiene il codice storico nella FCA. Le collisioni storiche non sono risolte automaticamente.
3. I codici speciali 0/-1 in `Tabellino.LISTA` non sono calciatori.
4. `ENTRATO`, `ENTRATORISERVA` e posizione nei primi undici slot non bastano, isolatamente, a certificare un voto conteggiato.
5. Il regolamento va ricavato per stagione e competizione, senza assumere tre sostituzioni ovunque. Un archivio FCM e uno snapshot delle impostazioni, non prova di eventuali variazioni avvenute durante la stagione.
6. Conservare integralmente le analisi RU personali della 3.2; non inserirle nei record pubblici senza decisione espressa.
7. Tabellini non collegati, incontri con squadra 0 e riposi: casistiche distinte, nessuna cancellazione.
8. Nessuna scrittura nel DB operativo/sandbox, nessuna modifica a Java, pipeline, siti, GUI, manifest o branch principale durante gli audit.
9. I file nuovi vanno in `D:\DEV_APPS\RecordsNext4.0`, i risultati in `D:\DEV_APPS\downloads`, con nomi versionati.
10. Ogni fase mantiene piano, registro decisioni, diario, specifiche, test e bibbia incrementale con codice integrale e SHA-256. Le versioni pregresse restano intatte.
~~~~

### `docs/REGISTRO_DECISIONI_RECORDSNEXT4_v9.md`

SHA256: `87addcf3497476b0d906a3cb7b91bc377e00b23b281feca3fa0b393488aef46b`

~~~~markdown
# RecordsNext 4.0 - Registro decisioni v9

Data: 2026-11-01. Registro additivo: D-001--D-053 restano documentate nelle versioni v2--v8; i relativi stati non cambiano tacitamente.

| ID | Stato | Decisione / evidenza |
|---|---|---|
| D-054 | CONFERMATA: verifica Windows | ZIP 3B1 `RecordsNext4_Fase3B1_OUTPUT_v1_20261101_001744_607896.zip`: stato OK, sette test superati, 552.025 righe `Formazione`, 22.265 tabellini, tre SHA-256 dati verificati, SHA sandbox invariato. Bibbia v8 generata; il runner dichiara nessun commit/deploy. |
| D-055 | CONFERMATA: osservazione FCM | La catena per individuare il regolamento e `Incontro.IDGIRONE -> Girone.ID -> Girone.IDCOMPETIZIONE -> Competizione.ID` **della stessa stagione**. `Competizione.ID` non costituisce da solo identita storica. |
| D-056 | CONFERMATA: fonte | `Competizione` registra anche `NUMEROSOSTITUZIONI`, `NUMERORISUFFICIO`, voti d'ufficio, limiti panchina, ruoli sostituibili, regole speciali; `ModuloAmmesso` collega i moduli alla competizione. Nessun valore e convertito in punteggio recuperato. |
| D-057 | CONFERMATA: osservazione locale 3B2 | Sono presenti 180 righe `Competizione` su 21 stagioni. Nel 2019/20 tutte le 12 riportano `NUMEROSOSTITUZIONI=5` e `NUMERORISUFFICIO=3`; nel 2007/08 le tre riportano 3, 8, 11 sostituzioni e 0, 99, 99 RU. Valori non normalizzati arbitrariamente. |
| D-058 | CONFERMATA: separazione | Nel campione SQLite ci sono 168 tabellini senza incontro, 23 collegati a incontri con una squadra zero: non dichiararli automaticamente anomalie o riposi. I controlli si svolgono separatamente. |
| D-059 | APERTA | Interpretazione delle configurazioni elevate (8, 11, RU 99), possibili convenzioni per assenza di limite, rapporto con tabellini e modifiche al regolamento durante la stagione: da verificare sul modello FCM e su esempi reali. |
| D-060 | APERTA | Definizione rigorosa di prestazione conteggiata, prestazione disponibile in panchina e punti effettivamente recuperabili, per competizione, stagione e regole del ruolo. Niente calcoli prima dell'approvazione. |
| D-061 | IMPLEMENTATA: prototipo di sola lettura | Audit 3B2 con dati raw completi delle configurazioni, aggregati per competizione, moduli ammessi e verifiche integrita; 8 test locali superati. Certificazione Windows ancora da eseguire. |

Nota: non sono state prese nuove decisioni sui pesi dei record o sulle formule per i punti non sfruttati.
~~~~

### `docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v9.md`

SHA256: `8da397427ed9d88bbb1378e92784a648b14483bda5a571eac78bf97f10b020f8`

~~~~markdown
# RecordsNext 4.0 - Diario implementazione v9

Data: 2026-11-01.

## Revisione dello ZIP Windows 3B1

- ZIP `RecordsNext4_Fase3B1_OUTPUT_v1_20261101_001744_607896.zip`: 14 voci; `MANIFEST_FASE3B1_v1.json` con `stato=OK`.
- Log `LOG_TEST_CONTRIBUZIONI_v1.txt`: sette test su Windows, tutti superati.
- SHA-256 dei file `combinazioni.csv`, `campioni.csv`, `sintesi.json` controllati confrontando gli hash dichiarati nel manifest con i byte nello ZIP.
- Invariato lo SHA del database sandbox: `57923f4e75986c2b81533e4beef15b9133635258d7de1d36827a1bdbe576671b`.
- 22.265 tabellini, 552.025 righe Formazione, 237.686 giocatori nei primi undici slot, 22.028 panchinari nei primi undici, 27.319 titolari iniziali fuori dai primi undici; nessuna attribuzione di punti persi.
- `CODICE_FUNZIONANTE_RECORDSNEXT4_v8.md` generato; nessun commit/deploy eseguito dal runner.

## Sviluppo Fase 3B2

- Riletto lo schema raw FCM originale della 3.2: `Competizione`, `Girone`, `Incontro`, `Tabellino`, `Modulo`, `ModuloAmmesso` e analisi RU personali.
- Implementato `Audit-RecordsNext4Regole-v1.py` in modalita SQLite `mode=ro` + `query_only=ON`.
- Il censimento e per coppia stagione/competizione, con mantenimento di tutte le colonne originali `Competizione` e dei moduli ammessi. La tabella di dettaglio non contiene dati personali delle fantasquadre.
- Osservati 21 archivi, 180 configurazioni, 13.072 incontri (compresi quelli programmati/riposi), 22.265 tabellini; 168 senza ID incontro valido e 23 collegati a incontri con una squadra 0.
- Test automatici `Test-RecordsNext4Regole-v1.py`: otto test sintetici e su DB reale, superati in ambiente di prova. Il database sintetico temporaneo viene generato sotto la cartella diagnostica `downloads`, mai in `%TEMP%`.
- Progettati runner versionato v1, specifica e verifica v1, piano v10, decisioni v9 e bibbia v9 con SHA-256 dei nuovi file.
- La verifica esecutiva del nuovo runner su PowerShell 5.1/Windows e ancora da effettuare; non presentarla come completata.

## Esclusioni

Nessuna modifica ai dati, alle impostazioni delle competizioni, ai 4 script RU personali, al Java, al sito o alla GUI. Nessun calcolo di voti recuperabili, nessun commit.
~~~~

### `docs/SPECIFICA_AUDIT_REGOLE_RECORDSNEXT4_v1.md`

SHA256: `fbf9713be13cf3626c732d4c0a5f35d3288a9d2b3cb6df207cb83b5fee5c5cc3`

~~~~markdown
# RecordsNext 4.0 - Specifica Fase 3B2 v1: regole osservate

## Obiettivo

Collegare ogni incontro fantasy alla configurazione originale della competizione **di quella stagione** prima di definire come conteggiare prestazioni fantasy e punti inutilizzati. Per il momento nessun calcolo di punti persi o di sostituzioni teoriche.

## Relazioni SQL

- `raw_<stagione>_fcm_incontro.IDGIRONE` -> `raw_<stagione>_fcm_girone.ID`
- `raw_<stagione>_fcm_girone.IDCOMPETIZIONE` -> `raw_<stagione>_fcm_competizione.ID`
- `raw_<stagione>_fcm_moduloammesso.IDCOMPETIZIONE` -> `Competizione.ID`, `IDMODULO` -> `Modulo.ID`
- `raw_<stagione>_fcm_tabellino.IDINCONTRO` -> `Incontro.ID`, se presente.

Un ID incontro inesistente si conserva come fatto separato. Non si cerca di inferire la competizione dal nome della squadra, dal campionato piu probabile o da un altro anno.

## Campi regolamentari preservati

La versione JSON integrale conserva tutte le colonne `Competizione` e ogni riga `ModuloAmmesso` corredata da `Modulo`.

Il CSV di confronto evidenzia: numero sostituzioni; RU; voti ufficio per giocatore/portiere; limiti panchina per ruolo; sostituzioni tra ruoli; regole portiere/difesa, voto e bonus speciali; fonti voto e impiego tabellino; numero incontri e tabellini effettivamente collegati.

## Limiti importanti

- I dati `Competizione` rappresentano la configurazione conservata nell'archivio FCM; non dimostrano che quella configurazione fosse uguale a tutte le giornate se le impostazioni sono state cambiate in corso d'anno.
- `NUMEROSOSTITUZIONI=11`, `NUMERORISUFFICIO=99` e altri valori elevati non vengono convertiti in convenzioni interpretate senza prova.
- Un incontro presente in `Incontro` non e necessariamente giocato; il censimento include anche incontri con `GIOCATO=0` e righe con squadra 0. Un tabellino senza incontro non equivale automaticamente a riposo o errore.
- Non utilizzare `NUMEROSOSTITUZIONI` da solo per simulare recuperabilita: servono anche RU, ruolo, formazione iniziale, modulistica, voto effettivo, eventi speciali e ulteriori regole.

## Output

- `RecordsNext4_Regole_v1_competizioni.csv`: una riga per stagione/competizione, con campi selezionati e conteggi.
- `RecordsNext4_Regole_v1_stagioni.csv`: totali per stagione.
- `RecordsNext4_Regole_v1_eccezioni_campione.csv`: campioni limitati, senza dati delle fantasquadre.
- `RecordsNext4_Regole_v1_configurazioni_complete.json`: configurazioni sorgenti e moduli per competizione.
- `RecordsNext4_Regole_v1_sintesi.json`: totali e distribuzione di valori.
- `RecordsNext4_Regole_v1_MANIFEST.json`: hash SHA-256 di file e database.

Sorgente aperta esclusivamente con SQLite `mode=ro` e `PRAGMA query_only=ON`. Il piccolo database sintetico dei test viene creato esclusivamente nella sottocartella diagnostica in `D:\DEV_APPS\downloads`, non in `%TEMP%`. Nessuna modifica ai dati.
~~~~

### `docs/VERIFICA_AUDIT_REGOLE_RECORDSNEXT4_v1.md`

SHA256: `21183d8a20649c7b3f82474dbac67da6268bfb689a9ba8878edd18564da9fb07`

~~~~markdown
# RecordsNext 4.0 - Verifica locale Fase 3B2 v1

## Base di test

Database sandbox della 3.2 fornita, SHA-256 `57923f4e75986c2b81533e4beef15b9133635258d7de1d36827a1bdbe576671b`.

## Risultati locali osservati

| Indicatore | Risultato |
|---|---:|
| Stagioni FCM | 21 |
| Configurazioni `Competizione` | 180 |
| Incontri (non necessariamente giocati) | 13.072 |
| Tabellini | 22.265 |
| Tabellini con incontro collegato | 22.097 |
| Tabellini senza incontro presente | 168 |
| Tabellini associati a incontro con squadra 0 | 23 |
| Incontri con squadra 0 (anche senza tabellino) | 1.334 |
| Test Python | 8/8 superati |

I valori 168 e 23 sono distinti; i 1.334 incontri con squadra 0 includono incontri non giocati o senza tabellino e non vanno interpretati come 1.334 errori. Lo studio dei turni di riposo resta separato.

### Esempi che impediscono una regola unica

- 2007/08: tre competizioni, sostituzioni configurate rispettivamente 3, 8, 11; RU 0, 99, 99.
- 2019/20: dodici competizioni con 5 sostituzioni e 3 RU.
- 2023/24: alcune competizioni hanno 11 sostituzioni e 11 RU; altre 3.
- 2025/26: le dodici competizioni hanno 3 sostituzioni, ma Play Off - Play Out registra 0 RU e le altre 1.
- 2026/27: 780 incontri presenti, zero tabellini: la presenza di un incontro non prova una gara giocata.

Il parser mantiene il valore originale e non lo interpreta come un diritto a recuperare un voto.

## Ancora da certificare

Il runner operativo e la generazione della bibbia v9 devono essere verificati sul worktree reale Windows tramite lo ZIP prodotto dall'utente. Fino ad allora questa verifica e **locale**, non una certificazione Windows.

Nessuna simulazione o pubblicazione.
~~~~

### `tools/Audit-RecordsNext4Regole-v1.py`

SHA256: `beaf21689805461aef41c8472e977034a8883ad3422d4c9c302943a80f5800f5`

~~~~python
#!/usr/bin/env python3
"""Read-only, season+competition-specific FCM regulation census (no score simulation)."""
from __future__ import annotations
import argparse
import csv
from datetime import datetime, timezone
import hashlib
import json
from pathlib import Path
import re
import sqlite3
import sys
from collections import Counter, defaultdict

FAMILY = 'RecordsNext4.RegoleOsservate.v1'
REQUIRED = ('competizione', 'girone', 'incontro', 'tabellino', 'modulo', 'moduloammesso')
SELECTED = (
    'NUMEROSOSTITUZIONI', 'NUMERORISUFFICIO', 'VOTODUFFICIO', 'VOTODUFFICIOPORT',
    'USATABELLINO', 'USAQUOT1', 'USAQUOT2', 'USAQUOT3',
    'GIOCINPANCHINAP', 'GIOCINPANCHINAD', 'GIOCINPANCHINAC',
    'GIOCINPANCHINAA', 'GIOCINPANCHINATOTALE',
    'DSOSTITUISCEC', 'DSOSTITUISCEA', 'CSOSTITUISCED',
    'CSOSTITUISCEA', 'ASOSTITUISCED', 'ASOSTITUISCEC',
    'REGOLAPORTIERE', 'REGOLADIFESA', 'REGOLAMIN59', 'REGOLAMIN60',
    'USASPECIALE1', 'USASPECIALE2', 'USASPECIALE3',
    'NOMESPECIALE1', 'NOMESPECIALE2', 'NOMESPECIALE3',
)


def sha_bytes(b:bytes)->str:
    return hashlib.sha256(b).hexdigest()


def sha_path(p:Path)->str:
    h=hashlib.sha256()
    with p.open('rb') as f:
        for chunk in iter(lambda:f.read(1024*1024),b''):
            h.update(chunk)
    return h.hexdigest()


def table(conn:sqlite3.Connection, names:set[str], season:str, family:str)->str:
    name = f'raw_{season}_fcm_{family}'
    if name not in names:
        raise ValueError(f'Tabella FCM mancante: {name}')
    return '"' + name + '"'


def discover(conn:sqlite3.Connection)->tuple[list[str],set[str]]:
    names={r[0].lower() for r in conn.execute("SELECT name FROM sqlite_master WHERE type='table'")}
    pattern=re.compile(r'^raw_(\d{4}_\d{4})_fcm_competizione$')
    seasons=sorted(m.group(1) for n in names if (m:=pattern.fullmatch(n)))
    if not seasons: raise ValueError('Nessuna stagione con tabella FCM Competizione')
    for season in seasons:
        for family in REQUIRED: table(conn,names,season,family)
    return seasons,names


def rows(conn:sqlite3.Connection, sql:str):
    for row in conn.execute(sql):
        yield dict(row)


def csv_write(dest:Path,fieldnames:list[str],records:list[dict]):
    with dest.open('w',encoding='utf-8-sig',newline='') as f:
        writer=csv.DictWriter(f,fieldnames=fieldnames,extrasaction='ignore',lineterminator='\n')
        writer.writeheader()
        writer.writerows(records)


def analyze(conn:sqlite3.Connection,max_samples:int=40)->dict:
    seasons,names=discover(conn)
    comp_export=[]
    full_config=[]
    season_export=[]
    incidents=[]
    global_stats=Counter()
    settings_count=Counter()
    samples=Counter()
    for season in seasons:
        ctab=table(conn,names,season,'competizione')
        gtab=table(conn,names,season,'girone')
        itab=table(conn,names,season,'incontro')
        ttab=table(conn,names,season,'tabellino')
        mtab=table(conn,names,season,'modulo')
        matab=table(conn,names,season,'moduloammesso')
        comps={int(d['ID']):d for d in rows(conn,f'SELECT * FROM {ctab} ORDER BY ID')}
        gironi={int(d['ID']):d for d in rows(conn,f'SELECT * FROM {gtab} ORDER BY ID')}
        moduli={int(d['ID']):d for d in rows(conn,f'SELECT * FROM {mtab} ORDER BY ID')}
        per_comp=defaultdict(Counter)
        all_comp_moduli=defaultdict(list)
        for d in rows(conn,f'SELECT * FROM {matab} ORDER BY IDCOMPETIZIONE,IDMODULO'):
            cid=d.get('IDCOMPETIZIONE');mid=d.get('IDMODULO')
            rec={**d,'modulo':moduli.get(mid)}
            all_comp_moduli[cid].append(rec)
            if cid not in comps: raise ValueError(f'{season}: modulo ammesso punta a competizione assente: {cid}')
        for id_girone,data in gironi.items():
            if data['IDCOMPETIZIONE'] not in comps:
                raise ValueError(f'{season}: girone {id_girone} ha competizione assente')
        incontri={}
        for match in rows(conn,f'SELECT ID, IDGIRONE, IDTIPO, IDCASA, IDFUORI, GIOCATO FROM {itab}'):
            mid=int(match['ID'])
            if mid in incontri: raise ValueError(f'{season}: ID incontro duplicato: {mid}')
            gid=match['IDGIRONE']
            if gid not in gironi:raise ValueError(f'{season}: incontro {mid} con girone inesistente {gid}')
            cid=gironi[gid]['IDCOMPETIZIONE']
            incontri[mid]=(cid,match)
            per_comp[cid]['incontri']+=1
            global_stats['incontri']+=1
            if not match['IDCASA'] or not match['IDFUORI']:
                per_comp[cid]['incontri_squadra_zero']+=1
                global_stats['incontri_squadra_zero']+=1
        for tab in rows(conn,f'SELECT IDINCONTRO,IDSQUADRA,TOTALESQUADRAESISTE,PARZIALESQUADRAESISTE,LISTA,RUOLO,TOT FROM {ttab}'):
            mid=tab['IDINCONTRO'];sid=tab['IDSQUADRA']
            global_stats['tabellini']+=1
            if mid not in incontri:
                global_stats['tabellini_incontro_assente']+=1
                if samples['incontro_assente']<max_samples:
                    incidents.append(dict(stagione=season,categoria='TABELLO_ID_INCONTRO_ASSENTE',idIncontro=mid,idSquadra=sid,idCompetizione='',nomeCompetizione='',dettaglio='IDIncontro non trovato; riposo non dimostrato'))
                    samples['incontro_assente']+=1
                continue
            cid,match=incontri[mid]
            per_comp[cid]['tabellini']+=1
            per_comp[cid]['tabellini_totale_esiste']+=int(bool(tab['TOTALESQUADRAESISTE']))
            per_comp[cid]['tabellini_parziale_esiste']+=int(bool(tab['PARZIALESQUADRAESISTE']))
            if not match['IDCASA'] or not match['IDFUORI']:
                per_comp[cid]['tabellini_incontro_squadra_zero']+=1
                global_stats['tabellini_incontro_squadra_zero']+=1
                if samples['riposo_potenziale']<max_samples:
                    incidents.append(dict(stagione=season,categoria='TABELLO_INCONTRO_SQUADRA_ZERO',idIncontro=mid,idSquadra=sid,idCompetizione=cid,nomeCompetizione=comps[cid]['NOME'],dettaglio='Incontro con almeno una squadra 0: potenziale riposo, verificare'))
                    samples['riposo_potenziale']+=1
        sc=Counter()
        for cid,d in sorted(comps.items()):
            num=d.get('NUMEROSOSTITUZIONI')
            ru=d.get('NUMERORISUFFICIO')
            settings_count[(num,ru)]+=1
            row={'stagione':season,'idCompetizione':cid,'competizione':d['NOME'],
                 'incontri':per_comp[cid]['incontri'],
                 'incontriSquadraZero':per_comp[cid]['incontri_squadra_zero'],
                 'tabellini':per_comp[cid]['tabellini'],
                 'tabelliniTotaleEsiste':per_comp[cid]['tabellini_totale_esiste'],
                 'tabelliniParzialeEsiste':per_comp[cid]['tabellini_parziale_esiste'],
                 'tabelliniIncontroSquadraZero':per_comp[cid]['tabellini_incontro_squadra_zero'],
                 'moduliAmmessi':len(all_comp_moduli[cid])}
            for col in SELECTED:
                if col not in d:raise ValueError(f'{season}: colonna Competizione mancante: {col}')
                row[col]=d[col]
            comp_export.append(row)
            full_config.append({'stagione':season,'idCompetizione':cid,'nome':d['NOME'],
                                'configurazioneFcmOriginale':d,'moduliAmmessi':all_comp_moduli[cid],
                                'conteggiCollegati':dict(per_comp[cid])})
            sc.update(per_comp[cid])
        season_export.append({'stagione':season,'competizioni':len(comps),'gironi':len(gironi),
                              'incontri':len(incontri),'tabellini':sum(per_comp[cid]['tabellini'] for cid in comps),
                              'tabelliniSenzaIncontro':global_stats['tabellini_incontro_assente']-sum(x['tabelliniSenzaIncontro'] for x in season_export),
                              'incontriSquadraZero':sc['incontri_squadra_zero'],
                              'tabelliniSuIncontroSquadraZero':sc['tabellini_incontro_squadra_zero']})
    global_stats['competizioni']=len(comp_export)
    global_stats['stagioni']=len(seasons)
    global_stats['tabellini_con_incontro']=sum(r['tabellini'] for r in comp_export)
    if global_stats['tabellini_con_incontro']+global_stats['tabellini_incontro_assente']!=global_stats['tabellini']:
        raise AssertionError('Totali Tabellino incoerenti')
    if sum(r['incontri'] for r in comp_export)!=global_stats['incontri']:
        raise AssertionError('Incontri non assegnati a competizioni')
    return {'formato':FAMILY,'avvertenza':'Configurazioni FCM finali osservate, non prova di regole applicate retroattivamente a ogni singola giornata. Nessun punto recuperabile calcolato.',
            'stagioni':season_export,'competizioni':comp_export,'configurazioniComplete':full_config,
            'campioniEccezioni':incidents,'totali':dict(global_stats),
            'distribuzioneSostituzioniRu':[{'numeroSostituzioni':k[0],'numeroRiserveUfficio':k[1],'competizioni':v}
                                        for k,v in sorted(settings_count.items(), key=lambda kv: (str(kv[0]),kv[1]))]}


def run(database:Path,output_dir:Path,max_samples:int):
    if not database.is_file():raise FileNotFoundError(database)
    if not output_dir.is_dir():raise FileNotFoundError(output_dir)
    uri=database.resolve().as_uri()+'?mode=ro'
    conn=sqlite3.connect(uri,uri=True)
    conn.row_factory=sqlite3.Row
    try:
        conn.execute('PRAGMA query_only=ON')
        result=analyze(conn,max_samples)
    finally:conn.close()
    prefix='RecordsNext4_Regole_v1'
    columns=['stagione','idCompetizione','competizione','incontri','incontriSquadraZero',
             'tabellini','tabelliniTotaleEsiste','tabelliniParzialeEsiste',
             'tabelliniIncontroSquadraZero','moduliAmmessi',*SELECTED]
    paths={
        'competizioni':output_dir/(prefix+'_competizioni.csv'),
        'stagioni':output_dir/(prefix+'_stagioni.csv'),
        'eccezioni':output_dir/(prefix+'_eccezioni_campione.csv'),
        'configurazioni':output_dir/(prefix+'_configurazioni_complete.json'),
        'sintesi':output_dir/(prefix+'_sintesi.json'),
    }
    csv_write(paths['competizioni'],columns,result['competizioni'])
    csv_write(paths['stagioni'],['stagione','competizioni','gironi','incontri','tabellini',
                               'tabelliniSenzaIncontro','incontriSquadraZero','tabelliniSuIncontroSquadraZero'],result['stagioni'])
    csv_write(paths['eccezioni'],['stagione','categoria','idIncontro','idSquadra','idCompetizione','nomeCompetizione','dettaglio'],result['campioniEccezioni'])
    paths['configurazioni'].write_text(json.dumps(result['configurazioniComplete'],ensure_ascii=False,indent=2)+'\n',encoding='utf8')
    short={k:v for k,v in result.items() if k not in ('competizioni','configurazioniComplete','campioniEccezioni')}
    paths['sintesi'].write_text(json.dumps(short,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
    manifest={'formato':FAMILY,'sha256Database':sha_path(database),'file':[
        {'nome':p.name,'byte':p.stat().st_size,'sha256':sha_path(p)} for p in paths.values()],
        'totali':result['totali']}
    (output_dir/(prefix+'_MANIFEST.json')).write_text(json.dumps(manifest,indent=2,ensure_ascii=False)+'\n',encoding='utf8')
    print('ESITO AUDIT: OK')
    print('STAGIONI:',result['totali']['stagioni'],'COMPETIZIONI:',result['totali']['competizioni'])
    print('INCONTRI:',result['totali']['incontri'],'TABELLINI:',result['totali']['tabellini'])
    print('TABELLINI ID INCONTRO ASSENTE:',result['totali']['tabellini_incontro_assente'])
    print('NESSUNA SIMULAZIONE O MODIFICA DB')


def main():
    parser=argparse.ArgumentParser()
    parser.add_argument('--database',type=Path,required=True)
    parser.add_argument('--output-dir',type=Path,required=True)
    parser.add_argument('--max-samples',type=int,default=40)
    args=parser.parse_args()
    if args.max_samples < 0 or args.max_samples > 10000: parser.error('max-samples fuori intervallo 0..10000')
    run(args.database,args.output_dir,args.max_samples)


if __name__=='__main__':
    try:main()
    except Exception as exc:
        print('ERRORE AUDIT REGOLE:',str(exc),file=sys.stderr)
        sys.exit(1)
~~~~

### `tools/Test-RecordsNext4Regole-v1.py`

SHA256: `2c7726a2c92ba847f9109c7a51ee929558ae75ccab88dff42e2d1e90ac0a1676`

~~~~python
#!/usr/bin/env python3
"""Synthetic read-only and real-database regression tests for Phase 3B2."""
from __future__ import annotations
import argparse
import importlib.util
from pathlib import Path
import sqlite3
import tempfile
import unittest

MODULE_PATH=Path(__file__).with_name('Audit-RecordsNext4Regole-v1.py')
spec=importlib.util.spec_from_file_location('rn4_regole',MODULE_PATH)
assert spec and spec.loader
mod=importlib.util.module_from_spec(spec)
spec.loader.exec_module(mod)
DB=None
SCRATCH=None


def inspect(path:Path):
    c=sqlite3.connect(path.resolve().as_uri()+'?mode=ro',uri=True)
    c.row_factory=sqlite3.Row
    c.execute('PRAGMA query_only=ON')
    try:return mod.analyze(c,max_samples=10)
    finally:c.close()


class SyntheticTests(unittest.TestCase):
    def test_minimal_fixture_and_no_mutation(self):
        with tempfile.TemporaryDirectory(prefix='rn4-regole-',dir=SCRATCH) as d:
            db=Path(d)/'test.db'
            c=sqlite3.connect(db)
            cols=', '.join('"'+k+'" INTEGER' for k in mod.SELECTED)
            c.execute('CREATE TABLE raw_2006_2007_fcm_competizione (ID INTEGER,NOME TEXT,'+cols+')')
            keys=['ID','NOME',*mod.SELECTED]
            values=[4,'Campionato',*[0]*len(mod.SELECTED)]
            values[keys.index('NUMEROSOSTITUZIONI')]=3
            values[keys.index('NUMERORISUFFICIO')]=1
            c.execute('INSERT INTO raw_2006_2007_fcm_competizione VALUES('+','.join('?'*len(keys))+')',values)
            c.execute('CREATE TABLE raw_2006_2007_fcm_girone (ID INTEGER,IDCOMPETIZIONE INTEGER)')
            c.execute('INSERT INTO raw_2006_2007_fcm_girone VALUES (9,4)')
            c.execute('CREATE TABLE raw_2006_2007_fcm_incontro (ID INTEGER,IDGIRONE INTEGER,IDTIPO INTEGER,IDCASA INTEGER,IDFUORI INTEGER,GIOCATO INTEGER)')
            c.execute('INSERT INTO raw_2006_2007_fcm_incontro VALUES (100,9,2,1,0,1)')
            c.execute('CREATE TABLE raw_2006_2007_fcm_tabellino (IDINCONTRO INTEGER,IDSQUADRA INTEGER,TOTALESQUADRAESISTE INTEGER,PARZIALESQUADRAESISTE INTEGER,LISTA TEXT,RUOLO TEXT,TOT TEXT)')
            c.execute("INSERT INTO raw_2006_2007_fcm_tabellino VALUES (100,1,1,1,'1','1','6')")
            c.execute("INSERT INTO raw_2006_2007_fcm_tabellino VALUES (999,3,0,0,'1','1','sv/ng')")
            c.execute('CREATE TABLE raw_2006_2007_fcm_modulo (ID INTEGER,NUMDIFE INTEGER,NUMCENT INTEGER,NUMATTA INTEGER)')
            c.execute('INSERT INTO raw_2006_2007_fcm_modulo VALUES (5,3,4,3)')
            c.execute('CREATE TABLE raw_2006_2007_fcm_moduloammesso (IDCOMPETIZIONE INTEGER,IDMODULO INTEGER,MODIF REAL,MODIFAVV REAL)')
            c.execute('INSERT INTO raw_2006_2007_fcm_moduloammesso VALUES (4,5,0,0)')
            c.commit();c.close()
            old=mod.sha_path(db)
            r=inspect(db)
            self.assertEqual(r['totali']['tabellini'],2)
            self.assertEqual(r['totali']['tabellini_incontro_assente'],1)
            self.assertEqual(r['totali']['tabellini_incontro_squadra_zero'],1)
            self.assertEqual(r['competizioni'][0]['NUMEROSOSTITUZIONI'],3)
            self.assertEqual(r['competizioni'][0]['moduliAmmessi'],1)
            self.assertEqual(old,mod.sha_path(db))

    def test_missing_reference_not_accepted(self):
        with sqlite3.connect(':memory:') as conn:
            self.assertRaises(ValueError,mod.analyze,conn)

    def test_selected_fields_no_duplicates(self):
        self.assertEqual(len(mod.SELECTED),len(set(mod.SELECTED)))
        self.assertIn('ASOSTITUISCEC',mod.SELECTED)


class RealDatabaseTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.r=inspect(DB)

    def test_global_counts(self):
        t=self.r['totali']
        self.assertEqual(t['stagioni'],21)
        self.assertEqual(t['competizioni'],180)
        self.assertEqual(t['tabellini'],22265)
        self.assertEqual(t['tabellini_incontro_assente'],168)
        self.assertEqual(t['tabellini_incontro_squadra_zero'],23)
        self.assertEqual(t['tabellini_con_incontro']+t['tabellini_incontro_assente'],t['tabellini'])

    def test_2007_rules_preserved_without_interpretation(self):
        matches=[r for r in self.r['competizioni'] if r['stagione']=='2007_2008']
        self.assertEqual(sorted(x['NUMEROSOSTITUZIONI'] for x in matches),[3,8,11])
        self.assertEqual(sorted(x['NUMERORISUFFICIO'] for x in matches),[0,99,99])

    def test_2019_five_substitutions(self):
        matches=[r for r in self.r['competizioni'] if r['stagione']=='2019_2020']
        self.assertEqual(len(matches),12)
        self.assertTrue(all(x['NUMEROSOSTITUZIONI']==5 and x['NUMERORISUFFICIO']==3 for x in matches))

    def test_2025_competition_specific(self):
        matches=[r for r in self.r['competizioni'] if r['stagione']=='2025_2026']
        self.assertEqual(len(matches),12)
        self.assertEqual([x['NUMERORISUFFICIO'] for x in matches if x['competizione']=='Play Off - Play Out'],[0])
        self.assertTrue(all(x['NUMEROSOSTITUZIONI']==3 for x in matches))

    def test_2026_no_tabellini(self):
        row=next(x for x in self.r['stagioni'] if x['stagione']=='2026_2027')
        self.assertEqual(row['tabellini'],0)


if __name__=='__main__':
    p=argparse.ArgumentParser()
    p.add_argument('--database',type=Path,required=True)
    p.add_argument('--scratch-dir',type=Path,required=True)
    args,remaining=p.parse_known_args()
    if not args.database.is_file():p.error('Database non trovato: '+str(args.database))
    if not args.scratch_dir.is_dir():p.error('Cartella test non trovata: '+str(args.scratch_dir))
    DB=args.database
    SCRATCH=args.scratch_dir
    unittest.main(argv=[__file__,*remaining],verbosity=2)
~~~~

### `tools/Create-RecordsNext4WorkingCodeMd-v9.py`

SHA256: `b9ca986697cf809bbb9342e6dfb041e612462515373438e249bb15f38dcdff2b`

~~~~python
#!/usr/bin/env python3
"""Versioned, additive RecordsNext4 bible including complete phase-3B2 source files."""
from __future__ import annotations
import argparse
import hashlib
from pathlib import Path
import subprocess
import sys

BASE='docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md'
PREVIOUS='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v8.md'
DEST='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v9.md'
NEW_FILES=[
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v10.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v9.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v9.md',
    'docs/SPECIFICA_AUDIT_REGOLE_RECORDSNEXT4_v1.md',
    'docs/VERIFICA_AUDIT_REGOLE_RECORDSNEXT4_v1.md',
    'tools/Audit-RecordsNext4Regole-v1.py',
    'tools/Test-RecordsNext4Regole-v1.py',
    'tools/Create-RecordsNext4WorkingCodeMd-v9.py',
    'tools/Invoke-RecordsNext4-Fase3B2-v1.py',
]


def sha(path:Path)->str:
    h=hashlib.sha256()
    with path.open('rb') as f:
        for block in iter(lambda:f.read(1024*1024),b''):
            h.update(block)
    return h.hexdigest()


def git(pr:Path,*args:str)->str:
    p=subprocess.run(['git','-C',str(pr),*args],capture_output=True,text=True,encoding='utf8',errors='replace')
    if p.returncode:
        raise RuntimeError('git '+ ' '.join(args) + ': ' +p.stderr.strip())
    return p.stdout.strip()


def main()->int:
    ap=argparse.ArgumentParser()
    ap.add_argument('--project-dir',required=True,type=Path)
    pr=ap.parse_args().project_dir.resolve(strict=True)
    for rel in (BASE,PREVIOUS,*NEW_FILES):
        if not (pr/rel).is_file():
            raise RuntimeError('File richiesto mancante: '+rel)
    lines=[
        '# RecordsNext 4.0 - Codice funzionante / bibbia incrementale v9',
        '', 'Data: 2026-11-01. Fase 3B2, censimento regolamentare, non release.',
        'Le bibbie 3.2 e 4.0 v3--v8 restano intatte.',
        '', '## Provenienza', '',
        '- Branch: `'+git(pr,'branch','--show-current')+'`',
        '- Commit HEAD: `'+git(pr,'rev-parse','HEAD')+'`',
        '- SHA-256 bibbia base 3.2: `'+sha(pr/BASE)+'`',
        '- SHA-256 bibbia precedente v8: `'+sha(pr/PREVIOUS)+'`',
        '- Fase 3B1 Windows: 7 test, 21 stagioni, 22.265 tabellini, 552.025 Formazione, hash rapporti verificati.',
        '- Fase 3B2 locale: 8 test, 180 configurazioni Competizione, 22.265 tabellini.',
        '- Nessun punto recuperabile calcolato; nessuna scrittura database.',
        '', '## Nuovi file integri (testo integrale e SHA-256)', ''
    ]
    for rel in NEW_FILES:
        path=pr/rel
        lang='python' if rel.endswith('.py') else 'markdown'
        lines.extend([
            '### `'+rel+'`','', 'SHA256: `'+sha(path)+'`', '',
            '~~~~'+lang,path.read_text(encoding='utf-8-sig').rstrip('\n'),'~~~~',''
        ])
    content=('\n'.join(lines).rstrip()+'\n').encode('utf8')
    dest=pr/DEST
    if dest.exists():
        if not dest.is_file() or dest.read_bytes()!=content:
            raise RuntimeError('Bibbia v9 differente; non sovrascrivo: '+str(dest))
        print('BIBBIA v9 gia presente identica:',dest)
    else:
        with dest.open('xb') as f:
            f.write(content)
        print('BIBBIA v9 generata:',dest)
    return 0


if __name__=='__main__':
    try:
        sys.exit(main())
    except Exception as exc:
        print('ERRORE GENERAZIONE BIBBIA:',str(exc),file=sys.stderr,flush=True)
        sys.exit(1)
~~~~

### `tools/Invoke-RecordsNext4-Fase3B2-v1.py`

SHA256: `dcf5c49abacc5affdcebf224c9f3081c63298133a62c994b6131f5df5e88de2c`

~~~~python
#!/usr/bin/env python3
"""Conservative Phase 3B2 installer, test runner and diagnostic ZIP creator."""
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
from zipfile import ZipFile, ZIP_DEFLATED

BASE='f9bf30323d9204d70e91e242bddfc72c2060ecf9'
FILES=[
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v10.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v9.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v9.md',
    'docs/SPECIFICA_AUDIT_REGOLE_RECORDSNEXT4_v1.md',
    'docs/VERIFICA_AUDIT_REGOLE_RECORDSNEXT4_v1.md',
    'tools/Audit-RecordsNext4Regole-v1.py',
    'tools/Test-RecordsNext4Regole-v1.py',
    'tools/Create-RecordsNext4WorkingCodeMd-v9.py',
    'tools/Invoke-RecordsNext4-Fase3B2-v1.py',
]
REQUIRED=[
    'pom.xml',
    'docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md',
    'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v8.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v9.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v8.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v8.md',
    'tools/Audit-RecordsNext4Contribuzioni-v1.py',
    'tools/Test-RecordsNext4Contribuzioni-v1.py',
]


def sha(path:Path)->str:
    h=hashlib.sha256()
    with path.open('rb') as f:
        for block in iter(lambda:f.read(1024*1024),b''):
            h.update(block)
    return h.hexdigest()


def git(project:Path,*args:str)->str:
    p=subprocess.run(['git','-C',str(project),*args],capture_output=True,
                     text=True,encoding='utf8',errors='replace')
    if p.returncode:
        raise RuntimeError('Git '+ ' '.join(args)+' non riuscito: '+p.stderr.strip())
    return p.stdout.strip()


def validate(project:Path)->dict:
    for rel in REQUIRED:
        if not (project/rel).is_file():
            raise RuntimeError('File mancante nel worktree: '+rel)
    root=Path(git(project,'rev-parse','--show-toplevel')).resolve()
    if os.path.normcase(str(root))!=os.path.normcase(str(project)):
        raise RuntimeError('Directory non radice del worktree: '+str(root))
    branch=git(project,'branch','--show-current')
    if branch!='feature/recordsnext4-players':
        raise RuntimeError('Branch errato: '+branch)
    git(project,'merge-base','--is-ancestor',BASE,'HEAD')
    return {'branch':branch,'head':git(project,'rev-parse','HEAD'),
            'gitStatusPrima':git(project,'status','-sb')}


def install(kit:Path,work:Path)->list[str]:
    for rel in FILES:
        src,dst=kit/rel,work/rel
        if not src.is_file():
            raise RuntimeError('File non incluso nello ZIP: '+rel)
        if dst.exists() and (not dst.is_file() or sha(src)!=sha(dst)):
            raise RuntimeError('File esistente differente; non sovrascrivo: '+rel)
    installed=[]
    for rel in FILES:
        src,dst=kit/rel,work/rel
        if dst.is_file():
            print('GIA PRESENTE IDENTICO:',rel,flush=True)
            continue
        dst.parent.mkdir(parents=True,exist_ok=True)
        with dst.open('xb') as f:
            f.write(src.read_bytes())
        installed.append(rel)
        print('INSTALLATO:',rel,flush=True)
    return installed


def execute(script:Path,args:list[str],log:Path):
    env=dict(os.environ,PYTHONDONTWRITEBYTECODE='1')
    p=subprocess.run([sys.executable,str(script),*args],capture_output=True,
                     text=True,encoding='utf8',errors='replace',env=env)
    log.write_text(p.stdout+p.stderr,encoding='utf8')
    print(script.name,'codice:',p.returncode,flush=True)
    if p.returncode:
        raise RuntimeError('Script fallito: '+script.name+'; controllare '+str(log))


def main()->int:
    parser=argparse.ArgumentParser()
    parser.add_argument('--work-dir',type=Path,default=Path('D:/DEV_APPS/RecordsNext4.0'))
    parser.add_argument('--database',type=Path,default=Path('D:/DEV_APPS/RecordsNext2.0/data/database/recordsnext-sandbox.db'))
    parser.add_argument('--output-dir',type=Path,default=Path('D:/DEV_APPS/downloads'))
    args=parser.parse_args()
    work=args.work_dir.resolve()
    db=args.database.resolve()
    out=args.output_dir.resolve()
    if not out.is_dir():
        raise RuntimeError('Directory downloads assente: '+str(out))
    stamp=datetime.now().strftime('%Y%m%d_%H%M%S_%f')
    root=out/('RecordsNext4_Fase3B2_OUTPUT_v1_'+stamp)
    root.mkdir(exist_ok=False)
    zip_path=out/(root.name+'.zip')
    kit=Path(__file__).resolve().parent.parent
    manifest={'fase':'3B2','kitVersion':'v1','worktree':str(work),'database':str(db),
              'stato':'ERRORE','nessunCommit':True,'nessunDeploy':True}
    ok=False
    try:
        if not db.is_file():
            raise RuntimeError('Database sandbox non trovato: '+str(db))
        manifest.update(validate(work))
        manifest['dbShaPrima']=sha(db)
        manifest['fileInstallati']=install(kit,work)
        execute(work/'tools/Test-RecordsNext4Regole-v1.py',
                ['--database',str(db),'--scratch-dir',str(root)],root/'LOG_TEST_REGOLE_v1.txt')
        data=root/'dati_audit'
        data.mkdir()
        execute(work/'tools/Audit-RecordsNext4Regole-v1.py',
                ['--database',str(db),'--output-dir',str(data)],
                root/'LOG_AUDIT_REGOLE_v1.txt')
        analysis=json.loads((data/'RecordsNext4_Regole_v1_MANIFEST.json').read_text(encoding='utf8'))
        for item in analysis['file']:
            p=data/item['nome']
            if not p.is_file() or sha(p)!=item['sha256']:
                raise RuntimeError('Integrita report fallita: '+item['nome'])
        manifest['totali']=analysis['totali']
        execute(work/'tools/Create-RecordsNext4WorkingCodeMd-v9.py',
                ['--project-dir',str(work)],root/'LOG_BIBBIA_v9.txt')
        docs=root/'documentazione'
        docs.mkdir()
        for rel in FILES:
            if rel.startswith('docs/'):
                shutil.copy2(work/rel,docs/Path(rel).name)
        shutil.copy2(work/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v9.md',
                     docs/'CODICE_FUNZIONANTE_RECORDSNEXT4_v9.md')
        manifest['dbShaDopo']=sha(db)
        if manifest['dbShaPrima']!=manifest['dbShaDopo']:
            raise RuntimeError('Database modificato durante Fase 3B2')
        manifest['gitStatusDopo']=git(work,'status','-sb')
        manifest['stato']='OK'
        ok=True
    except Exception:
        detail=traceback.format_exc()
        (root/'ERRORE_FASE3B2_v1.txt').write_text(detail,encoding='utf8')
        print('ERRORE FASE 3B2:',detail[-1800:],file=sys.stderr,flush=True)
    finally:
        (root/'MANIFEST_FASE3B2_v1.json').write_text(json.dumps(manifest,indent=2,ensure_ascii=False)+'\n',encoding='utf8')
        with ZipFile(zip_path,'x',compression=ZIP_DEFLATED,compresslevel=6) as z:
            for path in sorted(root.rglob('*')):
                if path.is_file():
                    z.write(path,path.relative_to(root).as_posix())
        print('ESITO:', 'OK' if ok else 'ERRORE',flush=True)
        print('ZIP DA CARICARE:',zip_path,flush=True)
    return 0 if ok else 1


if __name__=='__main__':
    sys.exit(main())
~~~~
