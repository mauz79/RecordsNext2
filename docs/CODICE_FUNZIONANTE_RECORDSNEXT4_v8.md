# RecordsNext 4.0 - Codice funzionante / bibbia incrementale v8

Data: 2026-11-01. Stato: Fase 3B1, audit osservazionale, non release.
Le bibbie della 3.2 e della 4.0 v3-v7 restano intatte.

## Provenienza

- Branch: `feature/recordsnext4-players`
- Commit HEAD: `f9bf30323d9204d70e91e242bddfc72c2060ecf9`
- SHA-256 bibbia base 3.2: `91ecb0006d6d1c6fda501c7243ba65ec2f0625c40e93394079238eac558a7da3`
- SHA-256 bibbia precedente v7: `b1e446b5e214fc602ad8a4c55911285aecfd88ab7a57f3c54c2a0b0b855aebc0`
- Fase 3A Windows: 9 test, 552.025 Formazione, report hash verificati.
- Fase 3B1 locale: 7 test, 21 stagioni, 552.025 Formazione, 22.265 Tabellino.
- Nessun calcolo di punti persi, nessuna scrittura database.

## Nuovi file integri (testo integrale e SHA-256)

### `docs/PIANO_RECORDSNEXT4_GIOCATORI_v9.md`

SHA256: `f37268526c74132558872aaad2942549a15d959ec0e5e1f54d90fb19d15df8c1`

~~~~markdown
# RecordsNext 4.0 - Piano operativo v9

Data: 2026-11-01. Base immutata 3.2.0, commit `f9bf30323d9204d70e91e242bddfc72c2060ecf9`.

## Stato verificato

- Fase 0: fonti e identificativi; le eccezioni di identita storica restano aperte.
- Fase 1: estrattore delle prestazioni reali FCA, verificato su Windows, 7 test superati; non integrato nel Java.
- Fase 2: estrattore fantasy FCM, verificato su Windows, 8 test superati; non integrato nel Java.
- Fase 3A: ZIP Windows `RecordsNext4_Fase3A_OUTPUT_v1_20261101_001023_187079.zip` verificato; 9 test superati, 552.025 Formazione, SHA di tutti e tre i report corretti, DB invariato, bibbia v7 generata.
- Fase 3B1: nuova diagnostica osservazionale che incrocia l'ordine in `Tabellino.LISTA` con `Formazione.POS`, `ENTRATO`, `ENTRATORISERVA`, `RUOLO`, `GIOCATO`; prima verifica locale 21 stagioni. Non attribuisce punti sprecati.
- Fase 3B2: verificare il significato del blocco iniziale del tabellino, RU, sostituzioni, configurazioni regolamentari (da `Competizione` e fonti 3.2); definire metriche distinte e casi esclusi, poi implementarle.
- Fase 4: integrazione canonica SQLite/Java, export JS partizionato per sito, HTML, GUI.
- Fase 5: regressioni, documentazione, commit, release 4.0.0.

## Vincoli non modificabili senza decisione esplicita

1. Ogni prestazione reale FCA e una sola per codice storico, stagione e giornata, indipendentemente dalle gare fantasy in cui compare.
2. Identificativo annuale `GiocatoreA.ID` e codice storico `CodiceGazza` sono diversi.
3. `Tabellino.LISTA` ha slot speciali (`0`, `-1`) che non sono identita di calciatori.
4. Non equiparare `ENTRATO=1` a contributo certo senza tabellino: nel 2012/13 esistono undici giocatori in testa a `LISTA`, tutti con `ENTRATO=0`, ma con totale squadra presente.
5. Distinguere panchina non utilizzata da fuori formazione (`POS<0`); nessun conteggio punti persi prima di definire la recuperabilita regolamentare.
6. Preservare i 4 report personali RU 3.2. I tabellini senza match e gli incontri con squadra zero non sono automaticamente errori; distinguere le casistiche.
7. Nessuna modifica a 3.2, database, siti, codice Java, GUI, manifest, record pubblici o branch di produzione.
8. Gli script sono installati in `D:\DEV_APPS\RecordsNext4.0`; gli ZIP diagnostici in `D:\DEV_APPS\downloads`.
9. Un nuovo file e versionato; aggiornare piano, registro decisioni, diario e bibbia con hash SHA-256 del codice integrale.
~~~~

### `docs/REGISTRO_DECISIONI_RECORDSNEXT4_v8.md`

SHA256: `c0ea1432ec5650f0475331f2ae9b2031bb81b812440ba952a07eb0e0a50e719c`

~~~~markdown
# RecordsNext 4.0 - Registro decisioni v8

Data: 2026-11-01. Questo registro e additivo: D-001 - D-044 rimangono nelle versioni precedenti (`v2`-`v7`). Le decisioni aperte non sono state risolte implicitamente.

| ID | Stato | Decisione / evidenza |
|---|---|---|
| D-045 | CONFERMATA: test Windows 3A | Lo ZIP 3A consegnato riporta 9/9 test, 21 stagioni, 552.025 righe `Formazione`; tre SHA-256 dei report coincidenti; SHA del DB sandbox prima/dopo identico. Bibbia v7 generata. |
| D-046 | CONFERMATA: osservazione diretta | L'ordine `Tabellino.LISTA` distingue il blocco dei primi undici slot dagli elementi successivi; e necessario rilevarlo accanto a `ENTRATO` e `ENTRATORISERVA`, senza trattare i flag come equivalenti. |
| D-047 | CONFERMATA: caso sorgente | In stagione 2012/13, incontro FCM 3256, squadra 3, undici giocatori nei primi undici slot hanno `ENTRATO=0` ma `Tabellino.TOTALESQUADRAESISTE=1`; quindi `ENTRATO` non e una fonte autonoma affidabile per stabilire il contributo. |
| D-048 | IMPLEMENTATA: prototipo 3B1 | L'audit legge `Formazione`, `Incontro`, `Tabellino`, `GiocatoreA` in sola lettura, incrocia gli slot, genera combinazioni e campioni, senza calcolare punti. |
| D-049 | CONFERMATA: controllo locale | Le 552.025 righe `Formazione` analizzate trovano ciascuna uno slot personale nel tabellino; gli slot speciali 0/-1 sono contabilizzati separatamente. |
| D-050 | APERTA | Prima di denominare `conteggiato` o `voto utilizzato` lo slot iniziale bisogna verificare le eccezioni RU, numero ridotto di giocatori, modificatori e stati speciali. Per ora il nome tecnico e `PRIMI11`. |
| D-051 | CONFERMATA: vincolo | 168 tabellini con ID incontro assente, 178 senza `Formazione`, 23 relativi a incontri con squadra zero: categorie non esclusive, non trasformate automaticamente in errori. Possibili turni di riposo da esaminare a parte. |
| D-052 | APERTA | Definire separatamente voto presente, voto utilizzato, voto inutilizzato dalla panchina, voto fuori formazione, cambi disponibili e punti recuperabili con regole vere della competizione/stagione. |
| D-053 | APERTA | Verificare gli anni con configurazioni di sostituzioni e RU differenti prima dei record sui punti persi. Le analisi RU personali 3.2 sono diagnostiche, non record pubblici. |

*Nota*: i contatori del prototipo 3B1 sono ancora da certificare con lo ZIP Windows. Non alterare registri preesistenti.
~~~~

### `docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v8.md`

SHA256: `446b6b83708609dc5deb8ca8636cad9f5afececf07796c3decea67388c332af6`

~~~~markdown
# RecordsNext 4.0 - Diario implementazione v8

Data: 2026-11-01.

## Ricezione e verifica Fase 3A

- Ricevuto `RecordsNext4_Fase3A_OUTPUT_v1_20261101_001023_187079.zip`.
- Letto manifest: stato OK; branch `feature/recordsnext4-players`, commit base `f9bf30323d9204d70e91e242bddfc72c2060ecf9`.
- Verificato log unit test: nove test superati.
- Verificati i tre SHA-256 del manifest dati: CSV combinazioni, CSV campioni, JSON sintesi; tutti corrispondenti ai file ZIP.
- Confrontato SHA DB prima e dopo: `57923f4e75986c2b81533e4beef15b9133635258d7de1d36827a1bdbe576671b`.
- Bibbia v7 prodotta; nessun commit/deploy dichiarato dal runner.

## Controlli Fase 3B1

- Riletti `docs/ANALISI_PERSONALE_RU.md`, `tools/Report_RU_Storico_Completo_v8_4.ps1` e lo schema SQLite della 3.2 fornita.
- Verificato caso 2012_2013, ID incontro 3256, IDSQUADRA 3: undici `ENTRATO=0` nei primi slot; totale squadra calcolato.
- Nuovo strumento `tools/Audit-RecordsNext4Contribuzioni-v1.py` per l'incrocio osservazionale `Tabellino.LISTA` / `Formazione`.
- Nuovo `tools/Test-RecordsNext4Contribuzioni-v1.py` con casi sintetici e regressioni sull'archivio reale.
- Nuovo generatore `Create-RecordsNext4WorkingCodeMd-v8.py` e runner `Invoke-RecordsNext4-Fase3B1-v1.py`; installazione senza sovrascritture e con verifica SHA del database.
- Documentazione versionata: piano v9, decisioni v8, diario v8, specifica v1, verifica tecnica v1, bibbia incrementale v8.

## Non implementato

Nessun dato di contribuzione definitiva, nessun calcolo punti persi, nessuna simulazione regolamentare. Nessuna scrittura DB, modifica Java, HTML, JS, GUI o deploy. Nessun commit.
~~~~

### `docs/SPECIFICA_AUDIT_CONTRIBUZIONI_RECORDSNEXT4_v1.md`

SHA256: `7e16aa9b3f0d0f7d6f8b0eb04515d60e8b1beb7415f53a7c78a58fbe3512aa02`

~~~~markdown
# RecordsNext 4.0 - Specifica Fase 3B1 v1

## Perche serve

`Formazione.ENTRATO` e `ENTRATORISERVA` descrivono stati FCM differenti. Non sono una prova autonoma del voto effettivamente utilizzato. Incrociamo quindi ogni riga `Formazione` con la posizione della relativa occorrenza in `Tabellino.LISTA`, insieme allo stato di `Incontro`.

## Fonti

Tabelle SQLite `raw_<stagione>_fcm_formazione`, `raw_<stagione>_fcm_tabellino`, `raw_<stagione>_fcm_incontro`, `raw_<stagione>_fca_giocatorea`. Si usa `GiocatoreA.ID` solo per la relazione annuale; `CODICEGAZZA` e esposto solo per l'identita storica. Non si leggono credenziali o tabelle `Fantasquadra`.

## Categorie descrittive

- `PRIMI11`: il giocatore compare con indice zero-based inferiore a 11 in `Tabellino.LISTA`.
- `DOPO_PRIMI11`: indice pari o superiore a 11.
- `SLOT_SPECIALE_PRIMI11`: ID del tabellino <= 0 (per esempio RU), non e un giocatore.
- `PRIMI11_ENTRATO_ZERO`: incongruenza informativa dei flag da studiare, non un dato da correggere.
- `FLAG_RISERVA_FUORI_PRIMI11`: `ENTRATORISERVA=1` ma slot dopo i primi undici; non considerarlo automaticamente utilizzato.
- `TABELLO_ID_INCONTRO_ASSENTE`, `TABELLO_SENZA_FORMAZIONE`, `TABELLO_INCONTRO_SQUADRA_ZERO`: verifiche DISTINTE e potenzialmente sovrapposte; non etichettare automaticamente come errore o turno di riposo.

Vengono prodotti CSV aggregati per stagione/flag/stato, campioni limitati e sintesi JSON con SHA-256. Tutte le righe `Formazione` devono comparire in uno e un solo slot individuale; in caso contrario il test fallisce.

## Limiti espliciti

La posizione nei primi undici slot e un'evidenza del tabellino FCM, NON ancora una definizione approvata di voto/bonus conteggiato, ne di punto sprecato. Prima di attribuire punteggi bisogna testare RU, modulistica, ruoli, numero sostituzioni, configurazione per competizione, campionati in inferiorita e modificatori. Gli archivi RU personali della 3.2 restano separati.

Nessuna modifica al DB o alle funzioni Java. Nessuna pubblicazione.
~~~~

### `docs/VERIFICA_AUDIT_CONTRIBUZIONI_RECORDSNEXT4_v1.md`

SHA256: `a69fb374eee631e375de55e830321c668cfe7fdf0c8ac07effb051418625574c`

~~~~markdown
# RecordsNext 4.0 - Verifica tecnica Fase 3B1 v1

## Verifica sulla copia SQLite 3.2 ricevuta (ambiente Linux)

- 21 stagioni, 552.025 `Formazione`, 22.265 righe `Tabellino`.
- 552.025/552.025 righe `Formazione` coperte da slot personali univoci del tabellino.
- 237.686 giocatori negli undici slot iniziali (occorrenze giocatore-partita; non calcolare ancora presenze ufficiali).
- 22.028 panchinari iniziali fra i primi undici slot; 27.319 titolari iniziali oltre i primi undici slot.
- 5.421 slot speciali (ID <= 0) nei primi undici slot; non sono calciatori.
- 40 casi `PRIMI11_ENTRATO_ZERO`, inclusi i 11 dell'incontro 3256 del 2012_2013 con totale squadra presente.
- 43 casi `FLAG_RISERVA_FUORI_PRIMI11`, coerenti con l'anomalia distinta della Fase 3A.
- 168 tabellini con incontro assente; 178 tabellini senza formazione; 23 tabellini associati a incontri con una squadra zero. Queste categorie si sovrappongono potenzialmente.
- 7 test automatici superati (4 test sintetici e 3 regressioni su database).
- Il database e stato aperto solo con SQLite `mode=ro` e `PRAGMA query_only=ON`.

## Da certificare con esecuzione Windows

Il runner installa solo file versionati nuovi, controlla l'integrita della 3.2 e del worktree 4.0, ripete i test e il probe sul sandbox, genera la bibbia v8 e produce uno ZIP di verifica. I risultati locali non sostituiscono lo ZIP Windows.

## Ancora aperto

Semantica definitiva di conteggio dei voti, eccezioni RU e recuperabilita dei punti secondo regolamento per competizione/stagione. I tabellini senza incontro e i riposi non vengono trasformati in record giocatore.
~~~~

### `tools/Audit-RecordsNext4Contribuzioni-v1.py`

SHA256: `8a6ddc413e7dca3ffc2efcf53ad58dbd1bc43e868ef4fd59932db4f7d45b3f2c`

~~~~python
#!/usr/bin/env python3
"""RecordsNext4 Phase 3B1: evidence audit of individual scores in FCM tabellini.

READ ONLY. Records membership in the first eleven LISTA slots, not "points lost".
Never modifies SQLite, exported site assets, or fantasy results.
"""
from __future__ import annotations
import argparse
from collections import Counter, defaultdict
import csv
import hashlib
import json
from pathlib import Path
import re
import sqlite3
import sys

SEASON = re.compile(r'^\d{4}_\d{4}$')
FAMILIES = ('fcm_formazione', 'fcm_tabellino', 'fcm_incontro', 'fca_giocatorea')


def sha(path: Path) -> str:
    h = hashlib.sha256()
    with path.open('rb') as f:
        for block in iter(lambda: f.read(1024 * 1024), b''):
            h.update(block)
    return h.hexdigest()


def tbl(year: str, fam: str) -> str:
    if not SEASON.fullmatch(year) or fam not in FAMILIES:
        raise ValueError('Bad table request')
    return '"raw_' + year + '_' + fam + '"'


def seasons(c: sqlite3.Connection) -> list[str]:
    return sorted(set(m.group(1) for (n,) in c.execute("SELECT name FROM sqlite_master WHERE type='table'")
                      if (m := re.fullmatch(r'raw_(\d{4}_\d{4})_fcm_formazione', n.lower()))))


def split(raw) -> list[str]:
    return str(raw).split('%') if raw is not None and str(raw) != '' else []


def position(value) -> str:
    if value is None:
        return 'NULL'
    return 'TITOLARE_INIZIALE' if value == 0 else ('PANCHINA' if value > 0 else 'FUORI_FORMAZIONE')


def role_group(value: str) -> str:
    try:
        n = int(value)
    except (ValueError, TypeError):
        return 'ALTRO'
    return 'POSITIVO' if n > 0 else ('NEGATIVO' if n < 0 else 'ZERO')


def analyze(c: sqlite3.Connection, year: str, max_examples: int = 4) -> dict:
    required = {'raw_' + year + '_' + f for f in FAMILIES}
    actual = {r[0].lower() for r in c.execute("SELECT name FROM sqlite_master WHERE type='table'")}
    if missing := required - actual:
        raise ValueError('Missing tables ' + str(sorted(missing)))
    matches = {r['ID']: dict(r) for r in c.execute('SELECT ID,GIOCATO,IDTIPO,IDCASA,IDFUORI FROM '+tbl(year,'fcm_incontro'))}
    names = {r['ID']: (r['NOME'], r['CODICEGAZZA']) for r in c.execute('SELECT ID,NOME,CODICEGAZZA FROM '+tbl(year,'fca_giocatorea'))}
    formations = defaultdict(dict)
    count_formations = 0
    for r in c.execute('SELECT IDINCONTRO,IDSQUADRA,IDGIOC,POS,ENTRATO,ENTRATORISERVA,RIG FROM '+tbl(year,'fcm_formazione')):
        key = (r['IDINCONTRO'], r['IDSQUADRA'])
        pid = r['IDGIOC']
        if pid in formations[key]:
            raise ValueError('Duplicate formazione player '+repr((year,key,pid)))
        formations[key][pid] = dict(r)
        count_formations += 1
    counts = Counter()
    combinations = Counter()
    examples = defaultdict(list)
    seen_groups = set()

    def note(kind: str, match, team, pid=None, index=None, role=None, tot=None, form=None, info=None):
        counts[kind] += 1
        if len(examples[kind]) < max_examples:
            name, code = names.get(pid, ('', ''))
            examples[kind].append({
                'stagione':year,'categoria':kind,'idIncontro':match,'idSquadra':team,
                'idGiocatoreFca':pid,'codiceStorico':code,'nomeGiocatore':name,
                'indiceLista':index,'ruoloTabellino':role,'totRaw':tot,
                'pos':form.get('POS') if form else None,
                'entrato':form.get('ENTRATO') if form else None,
                'entratoRiserva':form.get('ENTRATORISERVA') if form else None,
                'informazione':info})

    for r in c.execute('SELECT IDINCONTRO,IDSQUADRA,LISTA,RUOLO,TOT,TOTALESQUADRAESISTE FROM '+tbl(year,'fcm_tabellino')):
        counts['TABELLO_RIGHE'] += 1
        mid, tid = r['IDINCONTRO'], r['IDSQUADRA']
        key = (mid, tid)
        if key in seen_groups:
            note('TABELLO_CHIAVE_DUPLICATA', mid, tid)
            continue
        seen_groups.add(key)
        match = matches.get(mid)
        if match is None:
            note('TABELLO_ID_INCONTRO_ASSENTE',mid,tid)
        else:
            if match['IDCASA'] == 0 or match['IDFUORI'] == 0:
                note('TABELLO_INCONTRO_SQUADRA_ZERO',mid,tid,info='tipo='+str(match['IDTIPO']))
            if tid not in (match['IDCASA'],match['IDFUORI']):
                note('TABELLO_SQUADRA_ESTRANEA_INCONTRO',mid,tid,info='tipo='+str(match['IDTIPO']))
        team_form = formations.get(key)
        if not team_form:
            note('TABELLO_SENZA_FORMAZIONE',mid,tid,info='incontroPresente='+str(match is not None))
        ids,roles,tots = split(r['LISTA']),split(r['RUOLO']),split(r['TOT'])
        if not (len(ids) == len(roles) == len(tots)):
            note('TABELLO_LISTE_NON_ALLINEATE',mid,tid,info=f'{len(ids)}:{len(roles)}:{len(tots)}')
            continue
        if len(ids) < 11:
            note('TABELLO_MENO_DI_11_SLOT',mid,tid,info='slot='+str(len(ids)))
        tab_seen=set()
        for ix, text in enumerate(ids):
            counts['SLOT_TUTTI'] += 1
            try:
                pid = int(text)
            except (ValueError, TypeError):
                note('SLOT_ID_NON_INTERO',mid,tid,index=ix,role=roles[ix],tot=tots[ix],info=text)
                continue
            if pid <= 0:
                counts['SLOT_SPECIALI'] += 1
                if ix < 11:
                    note('SLOT_SPECIALE_PRIMI11',mid,tid,pid,ix,roles[ix],tots[ix])
                continue
            if pid in tab_seen:
                note('SLOT_GIOCATORE_DUPLICATO',mid,tid,pid,ix,roles[ix],tots[ix])
                continue
            tab_seen.add(pid)
            if not team_form or pid not in team_form:
                if team_form:
                    note('SLOT_SENZA_RIGA_FORMAZIONE',mid,tid,pid,ix,roles[ix],tots[ix])
                continue
            f=team_form[pid]
            where = 'PRIMI11' if ix < 11 else 'DOPO_PRIMI11'
            pos=position(f['POS'])
            entr='1' if f['ENTRATO'] else '0'
            ersv='1' if f['ENTRATORISERVA'] else '0'
            played = str(match['GIOCATO']) if match is not None else 'ASSENTE'
            keyc=(year,where,pos,entr,ersv,role_group(roles[ix]),played)
            combinations[keyc] += 1
            counts['FORMAZIONI_CON_SLOT'] += 1
            if where=='PRIMI11':
                counts['GIOCATORI_PRIMI11'] += 1
                if entr == '0':
                    note('PRIMI11_ENTRATO_ZERO',mid,tid,pid,ix,roles[ix],tots[ix],f)
            elif entr == '1':
                note('DOPO_PRIMI11_ENTRATO_UNO',mid,tid,pid,ix,roles[ix],tots[ix],f)
            if ersv == '1' and where == 'DOPO_PRIMI11':
                note('FLAG_RISERVA_FUORI_PRIMI11',mid,tid,pid,ix,roles[ix],tots[ix],f)
            if pos == 'TITOLARE_INIZIALE' and where == 'DOPO_PRIMI11':
                counts['TITOLARI_INIZIALI_NON_NEI_PRIMI11'] += 1
            if pos == 'PANCHINA' and where == 'PRIMI11':
                counts['PANCHINARI_NEI_PRIMI11'] += 1
        if team_form:
            for pid,f in team_form.items():
                if pid not in tab_seen:
                    note('FORMAZIONE_SENZA_SLOT',mid,tid,pid,form=f)
    for mid,tid in formations:
        if (mid,tid) not in seen_groups:
            note('FORMAZIONE_SENZA_TABELLINO',mid,tid)
    if counts['FORMAZIONI_CON_SLOT'] + counts['FORMAZIONE_SENZA_SLOT'] != count_formations:
        raise ValueError('Formation coverage mismatch: '+year)
    counts['RIGHE_FORMAZIONE'] = count_formations
    counts['GRUPPI_FORMAZIONE'] = len(formations)
    return {'stagione':year,'contatori':dict(counts), 'combinazioni':combinations,'campioni':examples}


def export(c: sqlite3.Connection, years: list[str], output: Path) -> dict:
    all_counts = Counter()
    csv_combos=[]
    csv_examples=[]
    stats=[]
    for year in years:
        item=analyze(c,year)
        for k,v in sorted(item['combinazioni'].items()):
            csv_combos.append(dict(zip(('stagione','selezioneLista','pos','entrato','entratoRiserva','ruoloTabellino','giocato'),k),righe=v))
        for arr in item['campioni'].values():
            csv_examples.extend(arr)
        all_counts.update(item['contatori'])
        stats.append({'stagione':year,'contatori':item['contatori']})
        print('CONTROLLATO',year,'Formazione',item['contatori']['RIGHE_FORMAZIONE'],'Tabellino',item['contatori'].get('TABELLO_RIGHE',0),flush=True)
    files=[]
    def savecsv(name,columns,rows):
        dest=output/name
        with dest.open('x',encoding='utf-8-sig',newline='') as f:
            writer=csv.DictWriter(f,fieldnames=columns,delimiter=';',extrasaction='ignore')
            writer.writeheader();writer.writerows(rows)
        files.append(dest)
    savecsv('RecordsNext4_Contribuzioni_v1_combinazioni.csv',
            ('stagione','selezioneLista','pos','entrato','entratoRiserva','ruoloTabellino','giocato','righe'),csv_combos)
    savecsv('RecordsNext4_Contribuzioni_v1_campioni.csv',
            ('stagione','categoria','idIncontro','idSquadra','idGiocatoreFca','codiceStorico','nomeGiocatore',
             'indiceLista','ruoloTabellino','totRaw','pos','entrato','entratoRiserva','informazione'),csv_examples)
    s={'formato':'RecordsNext4.ContribuzioniOsservate.v1',
       'avvertenza':'PRIMI11 significa soltanto appartenenza ai primi undici slot del Tabellino; non prova autonomamente la regolarita del punteggio. Nessun punto perso calcolato.',
       'stagioni':stats,'totali':dict(all_counts)}
    dest=output/'RecordsNext4_Contribuzioni_v1_sintesi.json'
    with dest.open('x',encoding='utf-8') as f:
        json.dump(s,f,indent=2,ensure_ascii=False);f.write('\n')
    files.append(dest)
    return {'formato':s['formato'],'stagioni':len(years),'formazioni':all_counts['RIGHE_FORMAZIONE'],
            'tabellini':all_counts['TABELLO_RIGHE'],
            'file':[{'nome':p.name,'byte':p.stat().st_size,'sha256':sha(p)} for p in files]}


def main() -> int:
    ap=argparse.ArgumentParser(description=__doc__)
    ap.add_argument('--database',required=True,type=Path)
    ap.add_argument('--output-dir',required=True,type=Path)
    ap.add_argument('--season',default=None)
    args=ap.parse_args()
    db=args.database.resolve(strict=True)
    out=args.output_dir.resolve()
    out.mkdir(parents=True,exist_ok=True)
    with sqlite3.connect(db.as_uri()+'?mode=ro',uri=True) as c:
        c.row_factory=sqlite3.Row
        c.execute('PRAGMA query_only=ON')
        years=seasons(c)
        if args.season:
            if args.season not in years:ap.error('Stagione non valida')
            years=[args.season]
        if not years:raise RuntimeError('Nessuna stagione')
        result=export(c,years,out)
    result['databaseSha256']=sha(db)
    dest=out/'RecordsNext4_Contribuzioni_v1_MANIFEST.json'
    with dest.open('x',encoding='utf-8') as f:json.dump(result,f,indent=2,ensure_ascii=False);f.write('\n')
    print('FORMATIONS:',result['formazioni'],'SEASONS:',len(years),flush=True)
    return 0

if __name__ == '__main__':
    try:sys.exit(main())
    except Exception as exc:
        print('ERRORE AUDIT CONTRIBUZIONI:',repr(exc),file=sys.stderr,flush=True)
        sys.exit(1)
~~~~

### `tools/Test-RecordsNext4Contribuzioni-v1.py`

SHA256: `65e03b0ab0dfd044f32b5e15fef496096161d468fc323a4d71d99cc822ade9d1`

~~~~python
#!/usr/bin/env python3
"""Unit and actual-archive regression tests, read-only."""
from __future__ import annotations
import argparse
import importlib.util
from pathlib import Path
import sqlite3
import sys
import unittest

HERE=Path(__file__).resolve().parent
spec=importlib.util.spec_from_file_location('rn4_audit',HERE/'Audit-RecordsNext4Contribuzioni-v1.py')
audit=importlib.util.module_from_spec(spec)
spec.loader.exec_module(audit)


def setup(c,year='2025_2026'):
    for table,ddl in {
      'fcm_formazione':'IDINCONTRO INT, IDSQUADRA INT, IDGIOC INT, POS INT, ENTRATO INT, ENTRATORISERVA INT, RIG INT',
      'fcm_tabellino':'IDINCONTRO INT, IDSQUADRA INT, LISTA TEXT, RUOLO TEXT, TOT TEXT, TOTALESQUADRAESISTE INT',
      'fcm_incontro':'ID INT, GIOCATO INT, IDTIPO INT, IDCASA INT, IDFUORI INT',
      'fca_giocatorea':'ID INT, NOME TEXT, CODICEGAZZA INT',
    }.items():
        c.execute('CREATE TABLE raw_'+year+'_'+table+' ('+ddl+')')
    c.row_factory=sqlite3.Row


class UnitTests(unittest.TestCase):
    def test_season_validation(self):
        self.assertEqual(audit.tbl('2025_2026','fcm_formazione'),'"raw_2025_2026_fcm_formazione"')
        with self.assertRaises(ValueError):audit.tbl('2025_2026";drop','fcm_formazione')
        with self.assertRaises(ValueError):audit.tbl('2025_2026','fcm_password')

    def test_special_role_and_position(self):
        self.assertEqual(audit.position(0),'TITOLARE_INIZIALE')
        self.assertEqual(audit.position(3),'PANCHINA')
        self.assertEqual(audit.position(-1),'FUORI_FORMAZIONE')
        self.assertEqual(audit.role_group('-5'),'NEGATIVO')
        self.assertEqual(audit.split('1%2%3'),['1','2','3'])

    def test_first11_and_flag_without_inference(self):
        c=sqlite3.connect(':memory:'); setup(c)
        c.execute('INSERT INTO raw_2025_2026_fcm_incontro VALUES (1,1,1,10,20)')
        c.executemany('INSERT INTO raw_2025_2026_fca_giocatorea VALUES (?,?,?)',
                      [(1,'Uno',100843),(2,'Due',223),(3,'Tre',334)])
        c.executemany('INSERT INTO raw_2025_2026_fcm_formazione VALUES (?,?,?,?,?,?,?)',
                      [(1,10,1,0,0,0,0),(1,10,2,1,1,0,0),(1,10,3,2,0,1,0)])
        c.execute('INSERT INTO raw_2025_2026_fcm_tabellino VALUES (?,?,?,?,?,?)',
                  (1,10,'%'.join(['1','2']+['0']*9+['3']),'%'.join(['2','2']+['5']*9+['-6']),'%'.join(['7','8']+['3']*9+['6']),1))
        s=audit.analyze(c,'2025_2026')
        n=s['contatori']
        self.assertEqual(n['RIGHE_FORMAZIONE'],3)
        self.assertEqual(n['FORMAZIONI_CON_SLOT'],3)
        self.assertEqual(n['PRIMI11_ENTRATO_ZERO'],1)
        self.assertEqual(n['PANCHINARI_NEI_PRIMI11'],1)
        self.assertEqual(n['SLOT_SPECIALE_PRIMI11'],9)
        self.assertEqual(n['FLAG_RISERVA_FUORI_PRIMI11'],1)
        self.assertEqual(n.get('DOPO_PRIMI11_ENTRATO_UNO',0),0)
        self.assertEqual(n.get('FORMAZIONE_SENZA_SLOT',0),0)
        c.close()

    def test_tabellino_without_incontro_kept_separate(self):
        c=sqlite3.connect(':memory:');setup(c)
        c.execute('INSERT INTO raw_2025_2026_fcm_tabellino VALUES (?,?,?,?,?,?)',(9,10,'0','1','3',1))
        counts=audit.analyze(c,'2025_2026')['contatori']
        self.assertEqual(counts['TABELLO_ID_INCONTRO_ASSENTE'],1)
        self.assertEqual(counts['TABELLO_SENZA_FORMAZIONE'],1)
        self.assertEqual(counts.get('TABELLO_INCONTRO_SQUADRA_ZERO',0),0)
        c.close()


class ActualArchive(unittest.TestCase):
    db=None
    @classmethod
    def setUpClass(cls):
        if cls.db is None:raise unittest.SkipTest('No database provided')
        cls.c=sqlite3.connect(cls.db.as_uri()+'?mode=ro',uri=True)
        cls.c.row_factory=sqlite3.Row
        cls.c.execute('PRAGMA query_only=ON')
    @classmethod
    def tearDownClass(cls):
        if hasattr(cls,'c'):cls.c.close()

    def test_2025(self):
        n=audit.analyze(self.c,'2025_2026')['contatori']
        self.assertEqual(n['RIGHE_FORMAZIONE'],39850)
        self.assertEqual(n['FORMAZIONI_CON_SLOT'],39850)
        self.assertEqual(n['PRIMI11_ENTRATO_ZERO'],11)

    def test_2012_all_initial_flags_zero_in_a_scored_match(self):
        n=audit.analyze(self.c,'2012_2013')['contatori']
        self.assertEqual(n['PRIMI11_ENTRATO_ZERO'],11)
        r=self.c.execute('SELECT GIOCATO FROM raw_2012_2013_fcm_incontro WHERE ID=3256').fetchone()
        t=self.c.execute('SELECT TOTALESQUADRAESISTE FROM raw_2012_2013_fcm_tabellino WHERE IDINCONTRO=3256 AND IDSQUADRA=3').fetchone()
        self.assertEqual(r['GIOCATO'],1)
        self.assertEqual(t['TOTALESQUADRAESISTE'],1)

    def test_all_history(self):
        years=audit.seasons(self.c)
        self.assertEqual(len(years),21)
        total=0
        tab=0
        orphan=0
        formation_orphan=0
        reserve_unused=0
        for year in years:
            n=audit.analyze(self.c,year)['contatori']
            total+=n['RIGHE_FORMAZIONE']
            tab+=n.get('TABELLO_RIGHE',0)
            orphan+=n.get('TABELLO_ID_INCONTRO_ASSENTE',0)
            formation_orphan+=n.get('TABELLO_SENZA_FORMAZIONE',0)
            reserve_unused+=n.get('FLAG_RISERVA_FUORI_PRIMI11',0)
        self.assertEqual(total,552025)
        self.assertEqual(tab,22265)
        self.assertEqual(orphan,168)
        self.assertEqual(formation_orphan,178)
        self.assertEqual(reserve_unused,43)


def main()->int:
    ap=argparse.ArgumentParser()
    ap.add_argument('--database',type=Path)
    args=ap.parse_args()
    if args.database:ActualArchive.db=args.database.resolve(strict=True)
    suite=unittest.defaultTestLoader.loadTestsFromModule(sys.modules[__name__])
    result=unittest.TextTestRunner(verbosity=2).run(suite)
    return 0 if result.wasSuccessful() else 1


if __name__=='__main__':sys.exit(main())
~~~~

### `tools/Create-RecordsNext4WorkingCodeMd-v8.py`

SHA256: `9c5e19193c893fdfc5d57b983daf4e6028d20a0942e3d4f2caef92d42bc736fa`

~~~~python
#!/usr/bin/env python3
"""Additive RecordsNext4 bible v8, full file bytes and SHA-256."""
from __future__ import annotations
import argparse
import hashlib
from pathlib import Path
import subprocess
import sys

BASE='docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md'
PREVIOUS='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v7.md'
DEST='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v8.md'
NEW_FILES=[
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v9.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v8.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v8.md',
 'docs/SPECIFICA_AUDIT_CONTRIBUZIONI_RECORDSNEXT4_v1.md',
 'docs/VERIFICA_AUDIT_CONTRIBUZIONI_RECORDSNEXT4_v1.md',
 'tools/Audit-RecordsNext4Contribuzioni-v1.py',
 'tools/Test-RecordsNext4Contribuzioni-v1.py',
 'tools/Create-RecordsNext4WorkingCodeMd-v8.py',
 'tools/Invoke-RecordsNext4-Fase3B1-v1.py',
]


def sha(path: Path) -> str:
    h=hashlib.sha256()
    with path.open('rb') as f:
        for block in iter(lambda:f.read(1024*1024),b''):
            h.update(block)
    return h.hexdigest()


def git(pr:Path,*args:str)->str:
    p=subprocess.run(['git','-C',str(pr),*args],capture_output=True,text=True,encoding='utf8',errors='replace')
    if p.returncode:raise RuntimeError('git '+ ' '.join(args) + ': ' +p.stderr.strip())
    return p.stdout.strip()


def main()->int:
    ap=argparse.ArgumentParser()
    ap.add_argument('--project-dir',required=True,type=Path)
    pr=ap.parse_args().project_dir.resolve(strict=True)
    for rel in (BASE,PREVIOUS,*NEW_FILES):
        if not (pr/rel).is_file():raise RuntimeError('File richiesto mancante: '+rel)
    lines=['# RecordsNext 4.0 - Codice funzionante / bibbia incrementale v8',
           '', 'Data: 2026-11-01. Stato: Fase 3B1, audit osservazionale, non release.',
           'Le bibbie della 3.2 e della 4.0 v3-v7 restano intatte.',
           '', '## Provenienza',
           '', '- Branch: `'+git(pr,'branch','--show-current')+'`',
           '- Commit HEAD: `'+git(pr,'rev-parse','HEAD')+'`',
           '- SHA-256 bibbia base 3.2: `'+sha(pr/BASE)+'`',
           '- SHA-256 bibbia precedente v7: `'+sha(pr/PREVIOUS)+'`',
           '- Fase 3A Windows: 9 test, 552.025 Formazione, report hash verificati.',
           '- Fase 3B1 locale: 7 test, 21 stagioni, 552.025 Formazione, 22.265 Tabellino.',
           '- Nessun calcolo di punti persi, nessuna scrittura database.',
           '', '## Nuovi file integri (testo integrale e SHA-256)', '']
    for rel in NEW_FILES:
        path=pr/rel
        lang='python' if rel.endswith('.py') else 'markdown'
        lines.extend(['### `'+rel+'`','', 'SHA256: `'+sha(path)+'`', '', '~~~~'+lang,
                      path.read_text(encoding='utf-8-sig').rstrip('\n'),'~~~~',''])
    data=('\n'.join(lines).rstrip()+'\n').encode('utf8')
    dest=pr/DEST
    if dest.exists():
        if not dest.is_file() or dest.read_bytes()!=data:
            raise RuntimeError('Bibbia v8 differente; non sovrascrivo: '+str(dest))
        print('BIBBIA v8 gia presente identica: '+str(dest))
    else:
        with dest.open('xb') as f:f.write(data)
        print('BIBBIA v8 generata: '+str(dest))
    return 0


if __name__=='__main__':
    try:sys.exit(main())
    except Exception as exc:
        print('ERRORE GENERAZIONE BIBBIA:',str(exc),file=sys.stderr,flush=True)
        sys.exit(1)
~~~~

### `tools/Invoke-RecordsNext4-Fase3B1-v1.py`

SHA256: `fc341d9f54de2418dedf9ecf2b964ea3da2ced567428d830c93b7752abc741e5`

~~~~python
#!/usr/bin/env python3
"""Conservative Phase 3B1 installer, test runner and diagnostic ZIP creator."""
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
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v9.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v8.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v8.md',
 'docs/SPECIFICA_AUDIT_CONTRIBUZIONI_RECORDSNEXT4_v1.md',
 'docs/VERIFICA_AUDIT_CONTRIBUZIONI_RECORDSNEXT4_v1.md',
 'tools/Audit-RecordsNext4Contribuzioni-v1.py',
 'tools/Test-RecordsNext4Contribuzioni-v1.py',
 'tools/Create-RecordsNext4WorkingCodeMd-v8.py',
 'tools/Invoke-RecordsNext4-Fase3B1-v1.py',
]
REQUIRED=[
 'pom.xml','docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md',
 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v7.md',
 'docs/PIANO_RECORDSNEXT4_GIOCATORI_v8.md',
 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v7.md',
 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v7.md',
 'tools/Audit-RecordsNext4Utilizzo-v1.py',
 'tools/Test-RecordsNext4Utilizzo-v1.py',
]


def sha(path: Path) -> str:
    h=hashlib.sha256()
    with path.open('rb') as f:
        for block in iter(lambda:f.read(1024*1024),b''):
            h.update(block)
    return h.hexdigest()


def git(project: Path,*args: str) -> str:
    p=subprocess.run(['git','-C',str(project),*args],capture_output=True,text=True,encoding='utf8',errors='replace')
    if p.returncode:
        raise RuntimeError('Git '+ ' '.join(args)+' non riuscito: '+p.stderr.strip())
    return p.stdout.strip()


def validate(project:Path)->dict:
    for rel in REQUIRED:
        if not (project/rel).is_file():raise RuntimeError('File mancante nel worktree: '+rel)
    root=Path(git(project,'rev-parse','--show-toplevel')).resolve()
    if os.path.normcase(str(root)) != os.path.normcase(str(project)):
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
        if not src.is_file():raise RuntimeError('File non incluso nello ZIP: '+rel)
        if dst.exists() and (not dst.is_file() or sha(src)!=sha(dst)):
            raise RuntimeError('File esistente differente; non sovrascrivo: '+rel)
    installed=[]
    for rel in FILES:
        src,dst=kit/rel,work/rel
        if dst.is_file():
            print('GIA PRESENTE IDENTICO:',rel,flush=True)
            continue
        dst.parent.mkdir(parents=True,exist_ok=True)
        with dst.open('xb') as f:f.write(src.read_bytes())
        installed.append(rel)
        print('INSTALLATO:',rel,flush=True)
    return installed


def execute(script:Path,args:list[str],log:Path)->None:
    env=dict(os.environ,PYTHONDONTWRITEBYTECODE='1')
    p=subprocess.run([sys.executable,str(script),*args],capture_output=True,text=True,
                     encoding='utf8',errors='replace',env=env)
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
    if not out.is_dir():raise RuntimeError('Directory downloads assente: '+str(out))
    stamp=datetime.now().strftime('%Y%m%d_%H%M%S_%f')
    root=out/('RecordsNext4_Fase3B1_OUTPUT_v1_'+stamp)
    root.mkdir(exist_ok=False)
    zip_path=out/(root.name+'.zip')
    kit=Path(__file__).resolve().parent.parent
    manifest={'fase':'3B1','kitVersion':'v1','worktree':str(work),
              'database':str(db),'stato':'ERRORE','nessunCommit':True,'nessunDeploy':True}
    ok=False
    try:
        if not db.is_file():raise RuntimeError('Database sandbox non trovato: '+str(db))
        manifest.update(validate(work))
        manifest['dbShaPrima']=sha(db)
        manifest['fileInstallati']=install(kit,work)
        execute(work/'tools/Test-RecordsNext4Contribuzioni-v1.py',
                ['--database',str(db)],root/'LOG_TEST_CONTRIBUZIONI_v1.txt')
        data=root/'dati_audit'
        data.mkdir()
        execute(work/'tools/Audit-RecordsNext4Contribuzioni-v1.py',
                ['--database',str(db),'--output-dir',str(data)],
                root/'LOG_AUDIT_CONTRIBUZIONI_v1.txt')
        analysis=json.loads((data/'RecordsNext4_Contribuzioni_v1_MANIFEST.json').read_text(encoding='utf8'))
        manifest['righeFormazione']=analysis['formazioni']
        manifest['righeTabellino']=analysis['tabellini']
        manifest['stagioni']=analysis['stagioni']
        execute(work/'tools/Create-RecordsNext4WorkingCodeMd-v8.py',
                ['--project-dir',str(work)],root/'LOG_BIBBIA_v8.txt')
        docs=root/'documentazione'
        docs.mkdir()
        for rel in FILES:
            if rel.startswith('docs/'):
                shutil.copy2(work/rel,docs/Path(rel).name)
        shutil.copy2(work/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v8.md',
                     docs/'CODICE_FUNZIONANTE_RECORDSNEXT4_v8.md')
        manifest['dbShaDopo']=sha(db)
        if manifest['dbShaPrima']!=manifest['dbShaDopo']:
            raise RuntimeError('Il database e stato modificato durante la fase 3B1!')
        manifest['gitStatusDopo']=git(work,'status','-sb')
        manifest['stato']='OK'
        ok=True
    except Exception:
        detail=traceback.format_exc()
        (root/'ERRORE_FASE3B1_v1.txt').write_text(detail,encoding='utf8')
        print('ERRORE FASE 3B1:',detail[-1800:],file=sys.stderr,flush=True)
    finally:
        (root/'MANIFEST_FASE3B1_v1.json').write_text(json.dumps(manifest,indent=2,ensure_ascii=False)+'\n',encoding='utf8')
        with ZipFile(zip_path,'x',compression=ZIP_DEFLATED,compresslevel=6) as z:
            for path in sorted(root.rglob('*')):
                if path.is_file():z.write(path,path.relative_to(root).as_posix())
        print('ESITO:', 'OK' if ok else 'ERRORE',flush=True)
        print('ZIP DA CARICARE:',zip_path,flush=True)
    return 0 if ok else 1


if __name__=='__main__':sys.exit(main())
~~~~
