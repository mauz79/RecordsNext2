# RecordsNext 4 — Registro decisioni v3

Regola: APPROVATA/CONFERMATA solo per decisioni esplicite o gia consolidate; PROPOSTA/DA VERIFICARE per ipotesi. Nessuna modifica al codice produttivo al solo aggiornamento del registro.

| ID | Stato | Decisione / requisito | Evidenza |
|---|---|---|---|
| D-001 | CONFERMATA | Obiettivo versione 4.0.0, sviluppo separato da v3.2.0 | Utente; tag v3.2.0 e commit f9bf303 nell'archivio Git |
| D-002 | CONFERMATA | `GiocatoreA.ID` e un ID/posizione locale a una stagione FCA | Utente; relazioni raw stagionali |
| D-003 | CONFERMATA CON VERIFICA APERTA | Codice persistente giocatore = esempio 100843; `GiocatoreA.CODICEGAZZA` lo contiene negli FCA esaminati; localizzazione esatta nel file FCM ancora da verificare | Utente, FCA, confronto foto |
| D-004 | CONFERMATA | Non toccare la 3.2 funzionante, i database sorgente o i file FCM/FCA | Base progetto |
| D-005 | CONFERMATA | Prestazione reale unica per giornata, distinta dai contributi nelle partite fantasy | Requisito utente |
| D-006 | CONFERMATA | Report e pacchetti in `D:\DEV_APPS\downloads`; nuovi sorgenti in `D:\DEV_APPS\RecordsNext4.0` | Utente |
| D-007 | CONFERMATA | Ogni revisione di file consegnato ha nome versionato; no overwrite silenzioso | Utente |
| D-008 | CONFERMATA | Bibbia 3.2 conservata, bibbia 4.0 incrementale con stato reale di test e codice completo delle modifiche | Documentazione 3.2 e richiesta utente |
| D-009 | CONFERMATA | I riposi non entrano nel dataset pubblico `Matches` | `ARCHITETTURA_RECORDSNEXT2.md` |
| D-010 | CONFERMATA | I tabellini con ID incontro non presente vanno preservati e analizzati prima di classificarli | Osservazione utente + audit; niente assegnazioni inventate |
| D-011 | CONFERMATA | La fase 0 esistente puo essere recuperata: se il worktree valido esiste, NON ricrearlo e non richiedere tag come precondizione alla sua lettura | Metadati .git 3.2 e errore runner v2 |
| D-012 | CONFERMATA | Prima verificare fonte e regole per riposi, SV, panchina, RU e punti non sfruttati; poi calcolare i record | Requisito di correttezza |
| Q-001 | APERTA | Quale tabella/campo del file FCM contiene il codice storico del calciatore? | Per ora `Formazione.IDGIOC` usa ID FCA locale |
| Q-002 | APERTA | Riutilizzo codici o errori d'archivio: come documentare eccezioni 12302, 15276 e altri? | Confronti storici e fotografie da verificare sugli originali |
| Q-003 | APERTA | Tipologia completa dei riposi in `Incontro` e origine dei `Tabellino.IDINCONTRO` senza `Incontro` | Audit v1 diagnostico, non conclusivo |
| Q-004 | APERTA | Esatta semantica `Entrato`, `EntratoRiserva`, `Pos`, `Rig`, SV, RU, modificatori | Manuale FCM e casi reali |
| Q-005 | APERTA | Definizione di punti non sfruttati e delta legalmente recuperabile | Regolamento e tabelle disponibili |
| Q-006 | APERTA | Intervalli reali dei tesseramenti e trasferimenti | Fonte datata da identificare |
| Q-007 | APERTA | Soglie/denominatori/ambiti dei record e ranking con pari merito | Da progettare dopo estrazione |

## Aggiornamento Fase 0 v4

| ID | Stato | Decisione / requisito | Evidenza |
|---|---|---|---|
| D-013 | CONFERMATA | Trattare l'errore regex del generatore PS v2 come difetto dello script; la correzione conserva il file v2 e introduce v3 Python | Log Windows 31/10/2026 e codice v2 |
| D-014 | CONFERMATA | Riprendere dal worktree 4.0 gia creato; evitare duplicazioni, sovrascritture e riesecuzioni del runner PS v3 | Log Windows: riconoscimento worktree e installazione completati |
| D-015 | CONFERMATA | I tabellini apparentemente orfani possono essere collegati a riposi: nessuna conclusione senza riscontro FCM, nessuna riga da scartare | Indicazione utente |
| Q-008 | APERTA | Confermare operativamente i probe ed esaminare ZIP v4 prima di chiudere la Fase 0 | Output v4 da ricevere |

I documenti v2 rimangono storici. Nessun cambiamento al programma di produzione.

## Esiti confermati dallo ZIP v4 e correzione probe v2

| ID | Stato | Decisione / risultato | Evidenza |
|---|---|---|---|
| D-016 | CONFERMATA | Esecuzione Fase 0 v4 conclusa: manifest AUDIT_ESEGUITI; ZIP senza errori e base Git invariata | ZIP utente 31/10/2026, SHA-256 database coincidente |
| D-017 | CONFERMATA | `incontraina` e il nome raw FCA corretto; `incontroina` nel probe v1 era un refuso di codice e generava 21 falsi mancanti | Ispezione SQLite e nuovo `Probe-RecordsNext4Fonti-v2.py` eseguito sul medesimo database |
| D-018 | CONFERMATA | Preservare la documentazione del difetto v1, usare la copertura v2 252/252 tabelle nelle verifiche successive | Audit locale ripetibile e report versionati |
| Q-008 | CHIUSA | Output v4 esaminato e audit Fase 0 eseguiti | 24 file ZIP integri e report v2 corretti |
| Q-009 | APERTA | Classificazione 168 righe `Tabellino` senza `Incontro`: verificare riposi, residuali e casi speciali usando gli originali | Nessuna classificazione automatica |
