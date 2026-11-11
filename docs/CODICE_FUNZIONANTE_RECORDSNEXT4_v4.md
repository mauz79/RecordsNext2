# RecordsNext 4.0 - Bibbia incrementale v4 (Fase 0)

> DOCUMENTAZIONE DIAGNOSTICA. Non equivale alla release 4.0.
> Base immutata: docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md.
> Il vecchio generatore PowerShell v2 e conservato solo come storico:
> falliva su -replace con regex backslash non valida.

Commit di partenza 3.2: `f9bf30323d9204d70e91e242bddfc72c2060ecf9`
Worktree branch: `feature/recordsnext4-players`
Worktree HEAD: `f9bf30323d9204d70e91e242bddfc72c2060ecf9`
Output diagnostici: D:/DEV_APPS/downloads/RecordsNext4_Fase0_OUTPUT_v4_*.zip

## Stato

- Nessuna modifica a Java, SQLite, GUI e JS di produzione.
- Runner fase 0 v4 scritto in Python per evitare fragilita della sintassi PS 5.1.
- Audit FCM/FCA: 21 stagioni, 12 famiglie, 252/252 tabelle presenti (probe v2 corretto).
- Relazioni: 105 controllate, 90 OK, 15 da verificare (168 righe Tabellino).
- Gli audit non certificano di per se le identita all-time.
- I tabellini con Incontro mancante restano non classificati fino alla verifica.
- Gli esiti Windows vanno letti nel manifest e nei log dello ZIP, non dedotti da questa bibbia.

## Base documentale storica

- `docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md` SHA256: `91ecb0006d6d1c6fda501c7243ba65ec2f0625c40e93394079238eac558a7da3`

## `docs/REVISIONE_BASE_RECORDSNEXT_3_2_v1.md`

SHA256: `a9e18047460ad75116043a572e0c71f06bd40c1ddef96015fb81d790190ec636`

~~~~text
# RecordsNext 4.0 — Revisione della base 3.2.0, v1

Data: 2026-10-31. Documento ricavato dall'archivio locale `RecordsNext2.0.zip` fornito dall'utente, NON da una supposizione sulla release. La versione 3.2 e la base invariata di riferimento.

## Fonti effettivamente ricevute e consultate

- `README.md`, `CHANGELOG.md`, `INSTALL.md`, `pom.xml`.
- `docs/ARCHITETTURA_RECORDSNEXT2.md`, `MODELLO_DATI_RECORDSNEXT2.md`, `STATO_IMPLEMENTAZIONE_RECORDSNEXT2.md`, `CONFIGURAZIONE_RECORDSNEXT2.md`, `CATALOGO_RECORD.md`, `DECISIONI_APERTE.md`, `DIPENDENZE_OUTPUT.md`, `INSTALLAZIONE_VISUALIZZATORI_HTML.md` e la restante documentazione sotto `docs/`.
- `docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md`: bibbia di circa 1,8 MB / 43.583 righe, con codice Java/test/scripts inglobato nel documento.
- `tools/Create-RecordsNext2WorkingCodeMd.ps1`: generatore della bibbia 3.2; NON va invocato per fingere che il codice di Fase 0 4.0 sia gia integrato nella base funzionante.
- Cartelle `src/main/java`, `src/test/java`, `tools`, configurazioni, metadati `.git` dell'archivio.

## Stato della 3.2 documentato nel progetto

- Release 3.2.0, commit base `f9bf30323d9204d70e91e242bddfc72c2060ecf9`; il tag annotato `v3.2.0` esiste nell'archivio e punta a tale commit.
- Il worktree 4.0 `feature/recordsnext4-players` risulta gia registrato nei metadati Git forniti; l'esistenza reale sul PC deve essere confermata dal runner.
- Modello modulare con 94 record/figli (93 configurabili + Culometro opzionale); pipeline, preflight, manifest e GUI gia funzionanti nella 3.2.
- Nove output pubblici RecordsNext; shard per stagione in formato **flat nella cartella `js` di ogni sito**, soggetti a cutoff cronologico per sito e a disponibilita storica.
- Le identita canoniche di fantasquadre e competizioni esistono gia; evitare mapping inventati.
- La bibbia 3.2 e una fotografia storica che include anche sezioni di versioni precedenti: le sezioni piu recenti prevalgono sulle note legacy.
- Nel modello `MODELLO_DATI_RECORDSNEXT2.md` sezioni 15-17 sono descritti concettualmente `Giocatore`, `Presenza giocatore`, `Evento giocatore`. Sono specifiche concettuali, non prova di tabelle canoniche gia implementate.

## Identificativi individuali

- `FCA.GiocatoreA.ID` e stagionale; e il riferimento usato nelle relazioni `FCM.Formazione.IDGioc`, `FCM.Tesserato.IDGiocatore`, `FCA.GiocaIn.IDGiocatore` per QUELLA stagione.
- Il codice storico usato per le fotografie risulta in `FCA.GiocatoreA.CODICEGAZZA` nelle tabelle della copia SQLite ricevuta. L'utente indica il codice stabile nel sistema FCM; va verificato il campo esatto del sorgente FCM senza confonderlo con ID di Formazione.
- Non unire automaticamente persone diverse con identico codice, ne trasformare ID annuali in identita all-time.

## Incontri a riposo e tabellini senza incontro: DISTINZIONE OBBLIGATORIA

La documentazione `ARCHITETTURA_RECORDSNEXT2.md` stabilisce esplicitamente: **i turni di riposo non fanno parte di Matches**. Questo non nega che un turno di riposo possa comparire nella sorgente FCM.

Nella copia SQLite analizzata:

- 2025/26: 90 record in `Incontro` con `IDTIPO=2` e un lato squadra uguale a zero; sono candidati coerenti con i riposi, ma la semantica va confermata sul manuale FCM e sugli esempi.
- Esistono anche tabellini con `IDINCONTRO` assente dalla tabella `Incontro`: 168 righe su 15 stagioni nella copia ricevuta. Nei casi esaminati non compaiono righe `Formazione` con lo stesso ID incontro. Alcuni tabellini contengono punteggi plausibili, quindi l'assenza non dimostra un riposo, ne corruzione.
- I due insiemi **non sono intercambiabili**: per classificare un ID mancante servono altri dati/campi o evidenze della fonte originale. Nel frattempo conservarlo come non classificato; non cancellare e non assegnare una competizione o giornata inventata.

## Implicazioni per fase 0

- Corretto il runner v2: controllava il tag prima del worktree esistente e non produceva diagnosi affidabili degli errori Git. La causa interna precisa dell'errore sul PC non e dimostrata; non affermare che il tag manchi.
- Runner v3: verifica prima il worktree esistente, ne controlla ramo e discendenza dal commit 3.2, copia soltanto nuovi file versionati, non sovrascrive file diversi, scrive report in `D:\DEV_APPS\downloads`.
- Sonda aggiuntiva v1 dedicata a possibili riposi e tabellini senza incontro, senza interpretazioni arbitrarie.
- Non cambiare versione Maven, importatori, schema canonico, visualizzatori o GUI in fase 0.

## Verifiche ancora da fare

- Esecuzione PS 5.1 sul PC dell'utente (qui sono stati verificati gli script Python; non e disponibile un runtime PowerShell Windows per collaudare i runner).
- Verifica dei codici storici direttamente sui FCM/FCA originali e del campo FCM esatto, con audit degli ID e delle fotografie.
- Semantica di `Entrato`, `EntratoRiserva`, `Pos`, `Rig`, RU, SV, modificatori e contabilizzazione dei punti non sfruttati.
- Trattamento documentato della stagione manuale senza FCM.
~~~~

## `docs/PIANO_RECORDSNEXT4_GIOCATORI_v3.md`

SHA256: `35be9de9850f8e3bd5d85ff149f82701da757b3e5f21387777d971bca47d74c7`

~~~~text
# RecordsNext 4.0.0 — Piano giocatori v3

Data: 2026-10-31. Stato: **FASE 0, AUDIT; NESSUNA RELEASE 4.0.0 ANCORA REALIZZATA**.

## Obiettivo

Per ciascun calciatore estrarre TUTTI i dati reali disponibili per stagione e giornata FCA, TUTTI i dati fantasy per stagione, fantasquadra, competizione e partita FCM, quindi produrre statistiche e record reali e fantasy per stagione, squadra, competizione e all time.

## Base tecnica vincolante

Il progetto di riferimento e **RecordsNext v3.2.0** del 2026-09-16, commit `f9bf303`, con documentazione effettiva e sorgenti presenti in `RecordsNext2.0.zip`. Il registro delle decisioni e la revisione `REVISIONE_BASE_RECORDSNEXT_3_2_v1.md` fanno parte della continuita operativa. Il worktree `D:\DEV_APPS\RecordsNext4.0` puo gia esistere e non deve essere ricreato automaticamente se valido.

La 3.2 ha 9 output, architettura multistagione e multisito, JS stagionali flat direttamente nella `js` del relativo sito e cutoff di ogni sito alla sua stagione. Mantenerli invariati finche il nuovo modulo non e stato implementato e testato.

## Identificativi

- `GiocatoreA.ID`: ID **locale FCA stagionale**.
- `Formazione.IDGioc`, `Tesserato.IDGiocatore`, `GiocaIn.IDGiocatore`: collegano l'ID locale alla stagione corrispondente.
- `GiocatoreA.CodiceGazza` negli FCA analizzati: codice storico usato nelle foto; l'utente riferisce il codice persistente nel sistema FCM, ma la sua ubicazione esatta nel file FCM va accertata. Esempio 100843 = Lautaro.
- Gli accoppiamenti pluristagionali richiedono trattamento di riutilizzi/anomalie nei codici; non si deve cambiare il significato dei campi esistenti.

## Entita logiche DA PROGETTARE (non implementate)

1. Identita canonica del calciatore / eventuale alias storicizzato.
2. Anagrafica stagionale: stagione + ID FCA locale e tutti i dati sorgente.
3. Prestazione reale: stagione + ID giocatore locale + giornata di Serie A; voti/eventi/contestualizzazione, con SV e dati mancanti separati da zero.
4. Partecipazione fantasy: stagione + partita FCM + fantasquadra + ID locale + occorrenza; ruolo e stato titolare/riserva/utilizzato, dettaglio tabellino e contributo ufficiale.
5. Evidenze di tesseramento: nessuna data trasferimento dedotta da righe non datate.
6. Record: dimensione reale o fantasy, ambito, filtro, soglie, denominatore, parita e provenienza fino al dato elementare.

Non contare una prestazione reale piu volte solo perche il fantacalciatore ha disputato piu competizioni legate alla stessa giornata.

## Incontri speciali e tabellini

`Matches` ESCLUDE i turni di riposo secondo architettura 3.2. Incontro con un lato squadra a zero e tabellino il cui `IDINCONTRO` manca da `Incontro` vanno censiti SEPARATAMENTE: non sono sinonimi, non eliminare nessuna riga e non costruire collegamenti fittizi.

## Roadmap e criteri di uscita

| Fase | Lavoro | Verifica | Stato |
|---|---|---|---|
| 0 | Worktree isolato; audit codici, schema, referenze, riposi/tabellini | Esiti riproducibili e Git 3.2 invariato | In corso |
| 1 | Dati reali per stagione/giornata, anche non tesserati | Confronto FCA `GiocaIn`, `Punteggio`, `Statistiche`, `Storico`; nessun doppio conteggio | Da iniziare |
| 2 | Dati fantasy per gara/competizione/squadra | Accordo con FCM `Formazione` e `Tabellino`; gestione riserve, RU, match speciali | Da iniziare |
| 3 | Catalogo record reali, fantasy e punti non sfruttati | Definizioni statistico-regolamentari e casi limite verificati | Da iniziare |
| 4 | Output JS e HTML/GUI | Cutoff multisito, shard flat, nessuna regressione 3.2 | Da iniziare |
| 5 | Regressioni, bibbia, FULL/UPDATE, release 4.0.0 | Test, commit e tag solo dopo approvazione | Da iniziare |

## Documentazione vivente

- Base storica 3.2: `docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md` (NON toccare).
- Nuova bibbia incrementale: `docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v2.md`, generata da `tools/Create-RecordsNext4WorkingCodeMd-v2.ps1`.
- Piano: questo file v3.
- Decisioni: `docs/REGISTRO_DECISIONI_RECORDSNEXT4_v2.md`.
- Diario: `docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v2.md`.
- Verifica base: `docs/REVISIONE_BASE_RECORDSNEXT_3_2_v1.md`.

Non dichiarare "funzionante" cio che e solamente progettato. Ogni successiva revisione sostituisce i nomi con nuove versioni; tenere traccia del codice completo e dei test nel generatore.
~~~~

## `docs/REGISTRO_DECISIONI_RECORDSNEXT4_v2.md`

SHA256: `7f20a072bc6e2d9e785713d7b6fa77a7334cc15942631f8371623759e824d88c`

~~~~text
# RecordsNext 4 — Registro decisioni v2

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

Nuova fase: aggiornare questo documento con nome v3, segnando le decisioni effettivamente approvate e i report che ne forniscono la prova. Mantenere storia delle modifiche nel diario.
~~~~

## `docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v2.md`

SHA256: `f656cd68f48c8d710e5dde48dd442fc4d8254f040b196c29edf30879b6cdf803`

~~~~text
# RecordsNext 4 — Diario implementazione v2

## 2026-10-31 — Revisione dell'archivio 3.2.0 e correzione Fase 0

- Ricevuto e analizzato `RecordsNext2.0.zip` contenente codice, script, documentazione, dati e metadati Git della release 3.2.0.
- Confermato commit `f9bf303...`, tag annotato `v3.2.0` e registrazione di `D:\DEV_APPS\RecordsNext4.0` come worktree sul ramo `feature/recordsnext4-players` nei metadati del repository. Non equivale a prova che ogni file sia presente attualmente sul PC: verifica nel runner.
- L'errore del runner Fase 0 v2 era nel preflight di verifica del tag: non procedere a ricreare il worktree esistente. Causa nativa esatta non disponibile dall'output; il runner v3 evita la verifica del tag quando il worktree e gia valido.
- Letta la struttura del generatore originale `tools/Create-RecordsNext2WorkingCodeMd.ps1`. La nuova bibbia v2 elenca file e SHA256 delle modifiche 4.0, lasciando inalterata la bibbia 3.2.
- Creato `tools/Probe-RecordsNext4Tabellini-v1.py` per distinguere statisticamente record `Incontro` con un lato squadra nullo/zero dai tabellini privi di riferimento `Incontro`.
- Verifica Python sul database caricato precedentemente: 21 stagioni, 15 con ID tabellini da verificare, 168 righe `Tabellino` senza `Incontro`. Il dato NON dimostra errore ne riposo.
- Nessuna modifica a Java, GUI, pubblicazione, archivi originali o database.

## Verifica necessaria sul PC Windows dell'utente

Avviare `tools/Invoke-RecordsNext4-Fase0-v3.ps1` dal kit v3 ed esaminare lo ZIP in `D:\DEV_APPS\downloads`. Non eseguire la versione v2 difettosa. Lo script PowerShell e stato revisionato staticamente per PS 5.1; **non e stato collaudato su una macchina Windows in questa sessione**. Gli script Python sono stati compilati e il nuovo audit tabellini e stato eseguito sul database di copia.

## Prossimi interventi

Dopo i report: verificare gli ID FCM/FCA originali e il codice foto, stabilire regole per riposi e tabellini residuali e progettare l'estrattore reale della fase 1. Nessun commit o release automatici.
~~~~

## `docs/CORREZIONE_FASE0_RECORDSNEXT4_v1.md`

SHA256: `054de67f8bf4e04cceb488eea4e8aac6ba88db245eb2e64dae1323e82d746b94`

~~~~text
# RecordsNext 4 - Correzione Fase 0, revisione v4

Data: 2026-10-31. Stato: correzione tecnica al runner, NON sviluppo di nuove tabelle o record.

## Errore riscontrato e causa verificata

Il 31/10/2026 il runner `Invoke-RecordsNext4-Fase0-v3.ps1` ha:

1. Verificato correttamente il worktree `D:\DEV_APPS\RecordsNext4.0`, ramo `feature/recordsnext4-players` e commit base 3.2 `f9bf303...`.
2. Installato nuovi file e riconosciuto identico lo script di audit giocatore v1.
3. Fallito durante la generazione della bibbia incrementale 4.0:
   `Create-RecordsNext4WorkingCodeMd-v2.ps1: Modello di espressione regolare \\ non valido`.

La causa esatta e la linea:

```powershell
($f -replace '\', '/')
```

In PowerShell `-replace` interpreta il pattern come regex: il backslash singolo non e un'espressione valida. La trasformazione letterale corretta e:

```powershell
$f.Replace('\', '/')
```

Il problema era nel codice consegnato, non nel sistema Windows dell'utente.
Non modificare in-place il generatore v2: rimane documento storico del difetto.

## Scelta correttiva

- Nuovo `tools/Create-RecordsNext4WorkingCodeMd-v3.py` e nuovo `tools/Invoke-RecordsNext4-Fase0-v4.py` (sola standard library Python, gia disponibile sul PC: 3.13.7).
- Nessun richiamo al generatore PowerShell v2 o al runner v3.
- Nessuna creazione del worktree: il v4 richiede quello gia installato e ne controlla il ramo e la discendenza da v3.2.0.
- Solo nuovi file con nome versionato; se un file preesiste con hash diverso, ci si ferma senza sovrascriverlo.
- Git viene interrogato ma non modifica commit, indice, branch o tag.
- Il database `recordsnext-sandbox.db` viene interrogato in sola lettura dai tool Python v1.
- ZIP dell'esecuzione anche su errore, sotto `D:\DEV_APPS\downloads`.

## Corretto trattamento delle anomalie FCM

L'utente segnala che i tabellini senza il normale incontro potrebbero riferirsi a squadre a riposo. La verifica resta aperta: non chiamarli automaticamente errori, ma neppure assegnarli a riposi senza riscontri nei file originali.

## Esecuzione

1. Scaricare `RecordsNext4_Fase0_Kit_v4.zip` in `D:\DEV_APPS\downloads`.
2. Estrarlo in `D:\DEV_APPS\downloads\RecordsNext4_Fase0_Kit_v4`.
3. Chiudere le elaborazioni che scrivono al database.
4. Lanciare con `py -3` lo script `tools\Invoke-RecordsNext4-Fase0-v4.py` dall'archivio estratto.
5. Caricare `RecordsNext4_Fase0_OUTPUT_v4_*.zip`, indipendentemente dal risultato, per controllare log e report.

## Condizioni di uscita

- Generatore bibbia v3 completato oppure errore dettagliato.
- Due audit codici, inventario fonti e probe tabellini completati e conservati in ZIP.
- Base 3.2.0 invariata. Nessun commit, nessuna pubblicazione.
~~~~

## `docs/PIANO_RECORDSNEXT4_GIOCATORI_v4.md`

SHA256: `0ac587a933a0a1f36e9dfc3ba9ec0a75071c1102ae373968174376e0e5f36253`

~~~~text
# RecordsNext 4.0.0 — Piano giocatori v4

Data: 2026-10-31. Stato: **FASE 0, AUDIT; NESSUNA RELEASE 4.0.0 ANCORA REALIZZATA**.

## Obiettivo

Per ciascun calciatore estrarre TUTTI i dati reali disponibili per stagione e giornata FCA, TUTTI i dati fantasy per stagione, fantasquadra, competizione e partita FCM, quindi produrre statistiche e record reali e fantasy per stagione, squadra, competizione e all time.

## Base tecnica vincolante

Il progetto di riferimento e **RecordsNext v3.2.0** del 2026-09-16, commit `f9bf303`, con documentazione effettiva e sorgenti presenti in `RecordsNext2.0.zip`. Il registro delle decisioni e la revisione `REVISIONE_BASE_RECORDSNEXT_3_2_v1.md` fanno parte della continuita operativa. Il worktree `D:\DEV_APPS\RecordsNext4.0` puo gia esistere e non deve essere ricreato automaticamente se valido.

La 3.2 ha 9 output, architettura multistagione e multisito, JS stagionali flat direttamente nella `js` del relativo sito e cutoff di ogni sito alla sua stagione. Mantenerli invariati finche il nuovo modulo non e stato implementato e testato.

## Identificativi

- `GiocatoreA.ID`: ID **locale FCA stagionale**.
- `Formazione.IDGioc`, `Tesserato.IDGiocatore`, `GiocaIn.IDGiocatore`: collegano l'ID locale alla stagione corrispondente.
- `GiocatoreA.CodiceGazza` negli FCA analizzati: codice storico usato nelle foto; l'utente riferisce il codice persistente nel sistema FCM, ma la sua ubicazione esatta nel file FCM va accertata. Esempio 100843 = Lautaro.
- Gli accoppiamenti pluristagionali richiedono trattamento di riutilizzi/anomalie nei codici; non si deve cambiare il significato dei campi esistenti.

## Entita logiche DA PROGETTARE (non implementate)

1. Identita canonica del calciatore / eventuale alias storicizzato.
2. Anagrafica stagionale: stagione + ID FCA locale e tutti i dati sorgente.
3. Prestazione reale: stagione + ID giocatore locale + giornata di Serie A; voti/eventi/contestualizzazione, con SV e dati mancanti separati da zero.
4. Partecipazione fantasy: stagione + partita FCM + fantasquadra + ID locale + occorrenza; ruolo e stato titolare/riserva/utilizzato, dettaglio tabellino e contributo ufficiale.
5. Evidenze di tesseramento: nessuna data trasferimento dedotta da righe non datate.
6. Record: dimensione reale o fantasy, ambito, filtro, soglie, denominatore, parita e provenienza fino al dato elementare.

Non contare una prestazione reale piu volte solo perche il fantacalciatore ha disputato piu competizioni legate alla stessa giornata.

## Incontri speciali e tabellini

`Matches` ESCLUDE i turni di riposo secondo architettura 3.2. Incontro con un lato squadra a zero e tabellino il cui `IDINCONTRO` manca da `Incontro` vanno censiti SEPARATAMENTE: non sono sinonimi, non eliminare nessuna riga e non costruire collegamenti fittizi.

## Roadmap e criteri di uscita

| Fase | Lavoro | Verifica | Stato |
|---|---|---|---|
| 0 | Worktree isolato; audit codici, schema, referenze, riposi/tabellini | Esiti riproducibili e Git 3.2 invariato | In corso |
| 1 | Dati reali per stagione/giornata, anche non tesserati | Confronto FCA `GiocaIn`, `Punteggio`, `Statistiche`, `Storico`; nessun doppio conteggio | Da iniziare |
| 2 | Dati fantasy per gara/competizione/squadra | Accordo con FCM `Formazione` e `Tabellino`; gestione riserve, RU, match speciali | Da iniziare |
| 3 | Catalogo record reali, fantasy e punti non sfruttati | Definizioni statistico-regolamentari e casi limite verificati | Da iniziare |
| 4 | Output JS e HTML/GUI | Cutoff multisito, shard flat, nessuna regressione 3.2 | Da iniziare |
| 5 | Regressioni, bibbia, FULL/UPDATE, release 4.0.0 | Test, commit e tag solo dopo approvazione | Da iniziare |

## Aggiornamento Fase 0 - recupero v4

- Il runner PowerShell v3 e stato eseguito ma si e fermato nel generatore bibbia PowerShell v2; il worktree e gli strumenti diagnostici risultano installati.
- Il comando di ripresa usa `tools/Invoke-RecordsNext4-Fase0-v4.py`, non modifica i file della release 3.2.
- Il generatore nuovo e `tools/Create-RecordsNext4WorkingCodeMd-v3.py`; la bibbia aggiornata e `docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v3.md`.
- Il risultato corretto va attestato solo dopo analisi dello ZIP v4.
- I riferimenti di tabellino senza `Incontro` non sono automaticamente errori: verificare esplicitamente le squadre a riposo.

## Documentazione vivente

- Base storica 3.2: `docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md` (NON toccare).
- Nuova bibbia incrementale: `docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v3.md`, generata da `tools/Create-RecordsNext4WorkingCodeMd-v3.py` (v2 storico difettoso).
- Piano: questo file v4.
- Decisioni: `docs/REGISTRO_DECISIONI_RECORDSNEXT4_v3.md`.
- Diario: `docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v3.md`.
- Verifica base: `docs/REVISIONE_BASE_RECORDSNEXT_3_2_v1.md`.

Non dichiarare "funzionante" cio che e solamente progettato. Ogni successiva revisione sostituisce i nomi con nuove versioni; tenere traccia del codice completo e dei test nel generatore.
~~~~

## `docs/REGISTRO_DECISIONI_RECORDSNEXT4_v3.md`

SHA256: `f6ea5100fd01cef2bfccafbb2f22b39cd9ce3e36a020199bb2e1879494d27274`

~~~~text
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
~~~~

## `docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v3.md`

SHA256: `dc3f94dbb868153c600fac9ffbc771d04b1a226912a26a1f9c3570e2ec6458cf`

~~~~text
# RecordsNext 4 — Diario implementazione v3

## 2026-10-31 — Revisione dell'archivio 3.2.0 e correzione Fase 0

- Ricevuto e analizzato `RecordsNext2.0.zip` contenente codice, script, documentazione, dati e metadati Git della release 3.2.0.
- Confermato commit `f9bf303...`, tag annotato `v3.2.0` e registrazione di `D:\DEV_APPS\RecordsNext4.0` come worktree sul ramo `feature/recordsnext4-players` nei metadati del repository. Non equivale a prova che ogni file sia presente attualmente sul PC: verifica nel runner.
- L'errore del runner Fase 0 v2 era nel preflight di verifica del tag: non procedere a ricreare il worktree esistente. Causa nativa esatta non disponibile dall'output; il runner v3 evita la verifica del tag quando il worktree e gia valido.
- Letta la struttura del generatore originale `tools/Create-RecordsNext2WorkingCodeMd.ps1`. La nuova bibbia v2 elenca file e SHA256 delle modifiche 4.0, lasciando inalterata la bibbia 3.2.
- Creato `tools/Probe-RecordsNext4Tabellini-v1.py` per distinguere statisticamente record `Incontro` con un lato squadra nullo/zero dai tabellini privi di riferimento `Incontro`.
- Verifica Python sul database caricato precedentemente: 21 stagioni, 15 con ID tabellini da verificare, 168 righe `Tabellino` senza `Incontro`. Il dato NON dimostra errore ne riposo.
- Nessuna modifica a Java, GUI, pubblicazione, archivi originali o database.

## Verifica necessaria sul PC Windows dell'utente

Avviare `tools/Invoke-RecordsNext4-Fase0-v3.ps1` dal kit v3 ed esaminare lo ZIP in `D:\DEV_APPS\downloads`. Non eseguire la versione v2 difettosa. Lo script PowerShell e stato revisionato staticamente per PS 5.1; **non e stato collaudato su una macchina Windows in questa sessione**. Gli script Python sono stati compilati e il nuovo audit tabellini e stato eseguito sul database di copia.

## Prossimi interventi

Dopo i report: verificare gli ID FCM/FCA originali e il codice foto, stabilire regole per riposi e tabellini residuali e progettare l'estrattore reale della fase 1. Nessun commit o release automatici.

## 2026-10-31 - Correzione v4 dopo fallimento PowerShell v3

- Ricevuto output dell'utente: Python 3.13.7; worktree riconosciuto, base f9bf303 confermata.
- Errore nella bibbia v2: `($f -replace '\', '/')` con regex backslash singolo non valida.
- Conservato lo script v2 a fini di continuita; nessuna sovrascrittura.
- Introdotti generatore bibbia v3 Python e runner diagnostico v4 Python; la validazione sul PC deve essere confermata dal relativo ZIP.
- Preservata l'ipotesi riposi per alcuni tabellini non associati a incontri; non ancora classificati.
- Nessun cambiamento al codice produttivo Java, SQLite, GUI, configurazioni e JS dei siti.
- Prossimo passaggio: esame output v4 e aggiornamento stato Fase 0, poi progetto estrattore reale Fase 1.
~~~~

## `tools/Audit-RecordsNext4Giocatori-v1.py`

SHA256: `29f866e33eb9557d1a392648d2f144b1251225738f817379b4d0a66d8a859db5`

~~~~text
#!/usr/bin/env python3
"""RecordsNext 4.0 - audit di lettura dei codici giocatore negli FCA/FCM raw.

Non crea/modifica tabelle, non aggiorna configurazioni e non genera JS.
Usa esclusivamente Python 3 standard library.
"""

import argparse
import csv
import hashlib
import json
import re
import sqlite3
from datetime import datetime
from pathlib import Path


def quote_table(table):
    if not re.fullmatch(r"[A-Za-z0-9_]+", table):
        raise ValueError("Nome tabella non sicuro")
    return '"' + table + '"'


def scalar(conn, statement, values=()):
    return conn.execute(statement, values).fetchone()[0]


def file_sha256(path):
    hasher = hashlib.sha256()
    with path.open("rb") as handle:
        while True:
            chunk = handle.read(4 * 1024 * 1024)
            if not chunk:
                break
            hasher.update(chunk)
    return hasher.hexdigest()


def inspect_player(conn, known, season, code):
    prefix = "raw_" + season + "_"
    base = prefix + "fca_giocatorea"
    if base not in known:
        return [], False

    player_table = quote_table(base)
    player_rows = conn.execute(
        f"SELECT ID, NOME, CODICEGAZZA, RUOLO, DATADINASCITA, FOTO "
        f"FROM {player_table} WHERE CAST(CODICEGAZZA AS TEXT) = ? ORDER BY ID", (code,)
    ).fetchall()

    output = []
    for local_id, name, source_code, role, dob, photo in player_rows:
        row = {
            "stagione": season,
            "codice_storico_sorgente": str(source_code),
            "id_fca_stagionale": local_id,
            "nome_fca": name,
            "ruolo_fca": role,
            "data_nascita_fca": dob,
            "foto_fca": photo,
            "giornate_giocain": None,
            "giornate_con_id_punteggio": None,
            "righe_formazione_fcm": None,
            "righe_formazione_entrato": None,
            "stato": "OK",
        }
        gi = prefix + "fca_giocain"
        if gi in known:
            t = quote_table(gi)
            a, b = conn.execute(
                f"SELECT COUNT(*), SUM(CASE WHEN IDPUNTEGGIO IS NOT NULL "
                f"AND IDPUNTEGGIO <> 0 THEN 1 ELSE 0 END) "
                f"FROM {t} WHERE IDGIOCATORE = ?", (local_id,)
            ).fetchone()
            row["giornate_giocain"] = a
            row["giornate_con_id_punteggio"] = b or 0
        else:
            row["stato"] = "GIOCAIN_ASSENTE"

        f = prefix + "fcm_formazione"
        if f in known:
            t = quote_table(f)
            a, b = conn.execute(
                f"SELECT COUNT(*), SUM(CASE WHEN ENTRATO IS NOT NULL "
                f"AND ENTRATO <> 0 THEN 1 ELSE 0 END) "
                f"FROM {t} WHERE IDGIOC = ?", (local_id,)
            ).fetchone()
            row["righe_formazione_fcm"] = a
            row["righe_formazione_entrato"] = b or 0
        else:
            row["stato"] += ";FORMAZIONE_ASSENTE"
        output.append(row)

    if len(player_rows) > 1:
        for row in output:
            row["stato"] += ";CODICE_NON_UNIVOCO_NELLA_STAGIONE"
    return output, True


def main():
    ap = argparse.ArgumentParser(description="Audit giocatore per codice storico FCA (sola lettura)")
    ap.add_argument("--database", required=True, type=Path, help="Copia del recordsnext.db")
    ap.add_argument("--code", required=True, help="Codice storico (es. 100843)")
    ap.add_argument("--output-dir", required=True, type=Path, help="Cartella per i due report")
    args = ap.parse_args()

    if not re.fullmatch(r"[0-9]+", args.code):
        ap.error("--code deve essere numerico")
    db = args.database.resolve()
    if not db.is_file():
        ap.error("Database non trovato: " + str(db))
    if args.code == "0":
        ap.error("Il codice 0 non identifica univocamente un calciatore")

    con = sqlite3.connect(db.as_uri() + "?mode=ro", uri=True)
    try:
        con.execute("PRAGMA query_only=ON")
        con.execute("PRAGMA busy_timeout=10000")
        known = {name for (name,) in con.execute(
            "SELECT name FROM sqlite_master WHERE type='table'")}
        seasons = sorted({m.group(1) for name in known
                          if (m := re.fullmatch(r"raw_(\d{4}_\d{4})_fca_giocatorea", name))})
        rows = []
        for season in seasons:
            batch, _ = inspect_player(con, known, season, args.code)
            rows.extend(batch)
    finally:
        con.close()

    # L'identita all-time non viene approvata automaticamente: questo audit
    # mostra solo le anagrafiche legate al codice cercato.
    names = sorted({str(r["nome_fca"]).strip() for r in rows})
    birthdates = sorted({str(r["data_nascita_fca"]).strip() for r in rows
                         if r["data_nascita_fca"] is not None})
    status = "NON_TROVATO" if not rows else (
        "REVISIONE_MANUALE" if len(names) > 1 or len(birthdates) > 1
        or any("NON_UNIVOCO" in r["stato"] for r in rows)
        else "SENZA_ANOMALIE_RILEVATE")

    now = datetime.now().strftime("%Y%m%d_%H%M%S")
    args.output_dir.mkdir(parents=True, exist_ok=True)
    basename = "RecordsNext4_AuditGiocatore_v1_" + args.code + "_" + now
    path_csv = args.output_dir / (basename + ".csv")
    path_json = args.output_dir / (basename + ".json")
    fields = ["stagione", "codice_storico_sorgente", "id_fca_stagionale",
              "nome_fca", "ruolo_fca", "data_nascita_fca", "foto_fca",
              "giornate_giocain", "giornate_con_id_punteggio",
              "righe_formazione_fcm", "righe_formazione_entrato", "stato"]
    with path_csv.open("w", encoding="utf-8-sig", newline="") as out:
        writer = csv.DictWriter(out, fieldnames=fields, delimiter=";")
        writer.writeheader()
        writer.writerows(rows)
    result = {
        "audit_version": "1",
        "project_target": "RecordsNext 4.0.0",
        "data_only_no_modifications": True,
        "database": str(db),
        "database_size": db.stat().st_size,
        "database_sha256": file_sha256(db),
        "code": args.code,
        "status": status,
        "seasons_with_fca_player_table": len(seasons),
        "seasons_with_matching_player": len({r["stagione"] for r in rows}),
        "matching_rows": len(rows),
        "names_found": names,
        "birthdates_found": birthdates,
        "note": "ID FCA locale; le righe GiocaIn non equivalgono necessariamente a presenze reali. "
                "Le righe Formazione non equivalgono necessariamente a utilizzi effettivi. "
                "Nessun record all-time e generato da questo audit.",
        "rows": rows,
    }
    path_json.write_text(json.dumps(result, ensure_ascii=False, indent=2, default=str), encoding="utf-8")
    print("Audit:", status)
    print("Stagioni FCA:", len(seasons), "| stagioni con codice:", result["seasons_with_matching_player"])
    print("Righe corrispondenti:", len(rows))
    print("CSV:", path_csv)
    print("JSON:", path_json)


if __name__ == "__main__":
    main()
~~~~

## `tools/Probe-RecordsNext4Fonti-v1.py`

SHA256: `eee34f1b7cfb59a06f1e5cc83b639ab587c762892e8bcacaa949d29b269a9b6a`

~~~~text
#!/usr/bin/env python3
"""RecordsNext4 fase 0: inventario/raw e integrita referenziale. SOLO LETTURA.

Non interpreta voti, non deduce identita all time, non modifica database e non
pubblica output; richiede Python 3 standard library.
"""
import argparse
import csv
import json
import re
import sqlite3
from datetime import datetime
from pathlib import Path

FAMILIES = (
    "fca_giocatorea", "fca_giocain", "fca_punteggio", "fca_statistiche",
    "fca_storico", "fca_squadradia", "fca_incontroina",
    "fcm_formazione", "fcm_tesserato", "fcm_tabellino",
    "fcm_incontro", "fcm_fantasquadra",
)
LINKS = (
    ("fcm_formazione", "IDGIOC", "fca_giocatorea", "ID", "formazione_giocatore"),
    ("fcm_tesserato", "IDGIOCATORE", "fca_giocatorea", "ID", "tesserato_giocatore"),
    ("fca_giocain", "IDGIOCATORE", "fca_giocatorea", "ID", "giocain_giocatore"),
    ("fcm_formazione", "IDINCONTRO", "fcm_incontro", "ID", "formazione_incontro"),
    ("fcm_tabellino", "IDINCONTRO", "fcm_incontro", "ID", "tabellino_incontro"),
)


def quoted(name):
    if not re.fullmatch(r"[A-Za-z0-9_]+", name):
        raise ValueError("Nome tabella imprevisto: " + name)
    return '"' + name + '"'


def norm(value):
    if value is None:
        return ""
    s = str(value).strip()
    if re.fullmatch(r"[+-]?\d+\.0+", s):
        s = s.split(".")[0]
    return s


def unique_values(conn, table, field):
    return {norm(v) for (v,) in conn.execute(
        "SELECT " + quoted(field) + " FROM " + quoted(table)
    ) if v is not None}


def write_csv(path, records, columns):
    with path.open("w", encoding="utf-8-sig", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=columns, delimiter=";")
        writer.writeheader()
        writer.writerows(records)


def main():
    ap = argparse.ArgumentParser(description="Inventario raw e relazioni FCM/FCA, sola lettura")
    ap.add_argument("--database", type=Path, required=True)
    ap.add_argument("--output-dir", type=Path, required=True)
    args = ap.parse_args()
    db = args.database.resolve()
    if not db.is_file():
        ap.error("Database non trovato: " + str(db))
    conn = sqlite3.connect(db.as_uri() + "?mode=ro", uri=True)
    conn.execute("PRAGMA query_only=ON")
    try:
        names = {name for (name,) in conn.execute(
            "SELECT name FROM sqlite_master WHERE type='table'"
        )}
        seasons = sorted({m.group(1) for name in names
                          if (m := re.fullmatch(r"raw_(\d{4}_\d{4})_fca_giocatorea", name))})
        # Non assumere che ogni stagione contenga necessariamente FCA.
        seasons = sorted(set(seasons) | {m.group(1) for name in names
                         if (m := re.fullmatch(r"raw_(\d{4}_\d{4})_fcm_formazione", name))})
        coverage, columns, rel, anomalous_codes = [], [], [], []
        for season in seasons:
            info = {}
            for family in FAMILIES:
                table = "raw_" + season + "_" + family
                if table not in names:
                    coverage.append({"stagione": season, "famiglia": family,
                                     "tabella": table, "presente": 0, "righe": ""})
                    continue
                count = conn.execute("SELECT COUNT(*) FROM " + quoted(table)).fetchone()[0]
                coverage.append({"stagione": season, "famiglia": family,
                                 "tabella": table, "presente": 1, "righe": count})
                cols = [row[1] for row in conn.execute("PRAGMA table_info(" + quoted(table) + ")")]
                info[family] = (table, set(cols), count)
                for col in cols:
                    columns.append({"stagione": season, "famiglia": family, "campo": col})
            for src, srcfield, dst, dstfield, label in LINKS:
                if src not in info or dst not in info:
                    rel.append({"stagione": season, "relazione": label, "stato": "TABELLA_ASSENTE",
                                "righe_origine": "", "riferimenti_mancanti": "", "distinti_mancanti": ""})
                    continue
                source, sourcecols, cnt = info[src]
                target, targetcols, _ = info[dst]
                if srcfield not in sourcecols or dstfield not in targetcols:
                    rel.append({"stagione": season, "relazione": label, "stato": "COLONNA_ASSENTE",
                                "righe_origine": cnt, "riferimenti_mancanti": "", "distinti_mancanti": ""})
                    continue
                # Conteggi esatti, lettura dei soli identificativi, senza creare indici o tabelle.
                valid = unique_values(conn, target, dstfield)
                missing = 0
                distinct = set()
                for (raw,) in conn.execute("SELECT " + quoted(srcfield) + " FROM " + quoted(source)):
                    key = norm(raw)
                    if key not in valid:
                        missing += 1
                        if len(distinct) < 10000:
                            distinct.add(key)
                rel.append({"stagione": season, "relazione": label, "stato": "OK" if missing == 0 else "DA_VERIFICARE",
                            "righe_origine": cnt, "riferimenti_mancanti": missing,
                            "distinti_mancanti": len(distinct) if len(distinct) < 10000 else ">=10000"})
            if "fca_giocatorea" in info:
                t, c, _ = info["fca_giocatorea"]
                if {"CODICEGAZZA", "NOME", "ID"}.issubset(c):
                    for code, n in conn.execute(
                        "SELECT CODICEGAZZA, COUNT(*) FROM " + quoted(t) +
                        " WHERE CODICEGAZZA IS NOT NULL AND CAST(CODICEGAZZA AS TEXT) <> '0' "
                        " GROUP BY CODICEGAZZA HAVING COUNT(*) > 1"
                    ):
                        anomalous_codes.append({"stagione": season, "codice": norm(code),
                                                "righe_stessa_stagione": n})
    finally:
        conn.close()
    folder = args.output_dir
    folder.mkdir(parents=True, exist_ok=True)
    stamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    prefix = "RecordsNext4_Fonti_v1_" + stamp
    write_csv(folder / (prefix + "_copertura.csv"), coverage,
              ("stagione", "famiglia", "tabella", "presente", "righe"))
    write_csv(folder / (prefix + "_colonne.csv"), columns,
              ("stagione", "famiglia", "campo"))
    write_csv(folder / (prefix + "_relazioni.csv"), rel,
              ("stagione", "relazione", "stato", "righe_origine", "riferimenti_mancanti", "distinti_mancanti"))
    write_csv(folder / (prefix + "_codici_duplicati_nella_stagione.csv"), anomalous_codes,
              ("stagione", "codice", "righe_stessa_stagione"))
    summary = {
        "script": "Probe-RecordsNext4Fonti-v1.py",
        "database": str(db),
        "read_only": True,
        "stagioni_raw": len(seasons),
        "famiglie_censite": len(FAMILIES),
        "relazioni_controllate": len(rel),
        "relazioni_da_verificare": [r for r in rel if r["stato"] not in ("OK",)],
        "duplicazioni_codici_nella_stessa_stagione": len(anomalous_codes),
        "limitazioni": [
            "Riferimenti mancanti non sono automaticamente errori: analizzare casi speciali.",
            "Non testa la semantica di Entrato/EntratoRiserva, SV, voti e tabellini.",
            "Non classifica identita storiche, non aggrega gol o assist.",
            "Non rileva codice storico nei campi FCM: verifica nelle strutture sorgente separata.",
        ],
    }
    (folder / (prefix + "_sintesi.json")).write_text(
        json.dumps(summary, ensure_ascii=False, indent=2), encoding="utf-8"
    )
    print("Stagioni:", len(seasons))
    print("Famiglie:", len(FAMILIES))
    print("Relazioni da verificare:", len(summary["relazioni_da_verificare"]))
    print("Codici duplicati nella stessa stagione:", len(anomalous_codes))
    print("File generati nella cartella:", folder)


if __name__ == "__main__":
    main()
~~~~

## `tools/Probe-RecordsNext4Tabellini-v1.py`

SHA256: `3d7deb2283762f5031dceed5e7a88f5ba430226a21fdc8500b024f95f5fd01ee`

~~~~text
#!/usr/bin/env python3
"""Fase 0: audit senza scrittura dei tabellini senza Incontro e dei possibili riposi.
Non determina autonomamente la natura degli incontri cancellati o a riposo.
"""
import argparse
import csv
import json
import re
import sqlite3
from pathlib import Path
from datetime import datetime

def quote(value):
    if not re.fullmatch(r'[a-zA-Z0-9_]+', value):
        raise ValueError('Nome SQL imprevisto: ' + value)
    return '"' + value + '"'

def to_text(value):
    return '' if value is None else str(value)

def main():
    p = argparse.ArgumentParser(description='Audit diagnostico tabellini e possibili riposi FCM (sola lettura)')
    p.add_argument('--database', type=Path, required=True)
    p.add_argument('--output-dir', type=Path, required=True)
    a = p.parse_args()
    if not a.database.is_file():
        p.error('Database non trovato: ' + str(a.database))
    c = sqlite3.connect(a.database.resolve().as_uri() + '?mode=ro', uri=True)
    c.execute('PRAGMA query_only=ON')
    c.execute('PRAGMA busy_timeout=15000')
    names = {r[0] for r in c.execute("SELECT name FROM sqlite_master WHERE type='table'")}
    seasons = sorted({m.group(1) for n in names if (m := re.fullmatch(r'raw_(\d{4}_\d{4})_fcm_incontro', n))})
    detail = []
    summary = []
    try:
        for s in seasons:
            i = 'raw_' + s + '_fcm_incontro'
            t = 'raw_' + s + '_fcm_tabellino'
            f = 'raw_' + s + '_fcm_formazione'
            sq = 'raw_' + s + '_fcm_fantasquadra'
            if t not in names:
                summary.append({'stagione': s, 'tabellini_senza_incontro': '', 'id_incontro_senza_riscontro': '', 'incontri_con_lato_zero': '', 'incontri_tipo2_con_lato_zero': '', 'note': 'Tabellino non presente'})
                continue
            # Un ID mancante in Incontro non puo essere dichiarato riposo dal solo Tabellino.
            missing = c.execute(
                'SELECT t.IDINCONTRO, t.IDSQUADRA, COUNT(*) AS n '
                'FROM ' + quote(t) + ' t LEFT JOIN ' + quote(i) + ' i ON t.IDINCONTRO=i.ID '
                'WHERE i.ID IS NULL GROUP BY t.IDINCONTRO, t.IDSQUADRA '
                'ORDER BY t.IDINCONTRO, t.IDSQUADRA'
            ).fetchall()
            for match_id, team_id, n in missing:
                formation_count = ''
                if f in names:
                    formation_count = c.execute('SELECT COUNT(*) FROM ' + quote(f) + ' WHERE IDINCONTRO=? AND IDSQUADRA=?', (match_id,team_id)).fetchone()[0]
                team_name = ''
                if sq in names:
                    team_row = c.execute('SELECT NOME FROM ' + quote(sq) + ' WHERE ID=? LIMIT 1',(team_id,)).fetchone()
                    team_name = to_text(team_row[0]) if team_row else ''
                detail.append({'stagione':s, 'id_incontro': match_id, 'id_squadra':team_id,
                               'squadra': team_name, 'righe_tabellino': n,
                               'righe_formazione_stesso_incontro_squadra':formation_count,
                               'classificazione':'ID_INCONTRO_ASSENTE_DA_VERIFICARE',
                               'nota':'Senza record Incontro non si puo provare un turno di riposo.'})
            rest_count, type2_count = c.execute(
                'SELECT COUNT(*), COALESCE(SUM(CASE WHEN IDTIPO=2 THEN 1 ELSE 0 END),0) '
                'FROM ' + quote(i) + ' WHERE IDCASA=0 OR IDFUORI=0'
            ).fetchone()
            summary.append({'stagione':s, 'tabellini_senza_incontro':sum(r[2] for r in missing),
                            'id_incontro_senza_riscontro':len({r[0] for r in missing}),
                            'incontri_con_lato_zero':rest_count,
                            'incontri_tipo2_con_lato_zero':type2_count,
                            'note':'Il lato zero e candidato per analisi riposi, NON prova generale.'})
    finally:
        c.close()
    a.output_dir.mkdir(parents=True, exist_ok=True)
    stamp = datetime.now().strftime('%Y%m%d_%H%M%S')
    prefix = 'RecordsNext4_Tabellini_v1_' + stamp
    for suffix,rows,fields in [
       ('riepilogo',summary,['stagione','tabellini_senza_incontro','id_incontro_senza_riscontro','incontri_con_lato_zero','incontri_tipo2_con_lato_zero','note']),
       ('dettaglio',detail,['stagione','id_incontro','id_squadra','squadra','righe_tabellino','righe_formazione_stesso_incontro_squadra','classificazione','nota'])
    ]:
        with (a.output_dir / (prefix+'_'+suffix+'.csv')).open('w',encoding='utf-8-sig',newline='') as h:
            w=csv.DictWriter(h,fieldnames=fields,delimiter=';')
            w.writeheader()
            w.writerows(rows)
    extra={'database':str(a.database),'stagioni_con_incontro':len(seasons),
      'stagioni_con_id_mancanti':sum(1 for x in summary if x['id_incontro_senza_riscontro'] not in ('',0)),
      'righe_tabellino_senza_incontro':sum(x['tabellini_senza_incontro'] for x in summary if isinstance(x['tabellini_senza_incontro'],int)),
      'dettaglio_righe':len(detail), 'stato':'SOLO_DIAGNOSTICA',
      'regole':['Un incontro con lato zero puo essere un riposo, ma richiede conferma FCM.',
                'Un tabellino il cui Incontro non esiste non viene attribuito automaticamente a riposo o corruzione.',
                'Nessuna riga e cancellata o attribuita a una competizione senza prova.']}
    (a.output_dir/(prefix+'_sintesi.json')).write_text(json.dumps(extra,ensure_ascii=False,indent=2),encoding='utf-8')
    print('Stagioni:',len(seasons),'| stagioni con riferimenti tabellino da verificare:',extra['stagioni_con_id_mancanti'])
    print('Righe di tabellino senza Incontro:',extra['righe_tabellino_senza_incontro'])
    print('Report:',a.output_dir)
if __name__=='__main__': main()
~~~~

## `tools/Invoke-RecordsNext4-Fase0-v3.ps1`

SHA256: `5d8c7042ce28355ca996af787f3aa82100a0906fa1992820ba96d2f4660a8b7a`

~~~~text
[CmdletBinding()]
param(
    [string]$RepoDir = 'D:\DEV_APPS\RecordsNext2.0',
    [string]$WorkDir = 'D:\DEV_APPS\RecordsNext4.0',
    [string]$OutputDir = 'D:\DEV_APPS\downloads',
    [string]$Database = 'D:\DEV_APPS\RecordsNext2.0\data\database\recordsnext-sandbox.db'
)
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Git-Text {
    param([string]$Directory, [string[]]$GitArguments)
    $result = & git -C $Directory @GitArguments
    $code = $LASTEXITCODE
    if ($code -ne 0) {
        throw ('Git ha restituito codice ' + $code + ': git -C ' + $Directory + ' ' + ($GitArguments -join ' '))
    }
    return (($result -join "`n").Trim())
}
function Run-Python {
    param([string]$Script, [string[]]$ScriptArguments, [string]$LogFile)
    & py -3 $Script @ScriptArguments | Tee-Object -FilePath $LogFile
    $code = $LASTEXITCODE
    if ($code -ne 0) { throw ('Python ha restituito codice ' + $code + ' in ' + $Script + ' (log: ' + $LogFile + ')') }
}

if (-not (Test-Path -LiteralPath $OutputDir -PathType Container)) {
    New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null
}
$stamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$runDir = Join-Path $OutputDir ('RecordsNext4_Fase0_OUTPUT_v3_' + $stamp)
$zipOut = Join-Path $OutputDir ('RecordsNext4_Fase0_OUTPUT_v3_' + $stamp + '.zip')
if (Test-Path -LiteralPath $runDir) { throw ('Cartella output esistente: ' + $runDir) }
New-Item -ItemType Directory -Path $runDir | Out-Null
$ok = $false
try {
    $kitRoot = Split-Path -Parent $PSScriptRoot
    if (-not (Get-Command git -ErrorAction SilentlyContinue)) { throw 'Git non presente nel PATH.' }
    if (-not (Get-Command py -ErrorAction SilentlyContinue)) { throw 'Python Launcher py non presente nel PATH.' }
    & py -3 --version
    if ($LASTEXITCODE -ne 0) { throw 'Python 3 non disponibile.' }
    if (-not (Test-Path -LiteralPath (Join-Path $RepoDir 'pom.xml') -PathType Leaf)) {
        throw ('Repository operativo 3.2 non trovato: ' + $RepoDir)
    }
    if (-not (Test-Path -LiteralPath $Database -PathType Leaf)) {
        throw ('Database sandbox non trovato: ' + $Database)
    }
    $originalRoot = Git-Text -Directory $RepoDir -GitArguments @('rev-parse', '--show-toplevel')
    if ([System.IO.Path]::GetFullPath($originalRoot).TrimEnd('\') -ine [System.IO.Path]::GetFullPath($RepoDir).TrimEnd('\')) {
        throw ('Repository base inatteso: ' + $originalRoot)
    }
    $originalHead = Git-Text -Directory $RepoDir -GitArguments @('rev-parse', 'HEAD')
    Write-Host ('Repository 3.2 HEAD: ' + $originalHead)

    # IMPORTANTE: se il worktree esiste, prima lo riconosciamo. Non dipendiamo dal tag.
    if (Test-Path -LiteralPath $WorkDir -PathType Container) {
        if (-not (Test-Path -LiteralPath (Join-Path $WorkDir 'pom.xml') -PathType Leaf)) {
            throw ('Directory 4.0 esistente ma senza pom.xml: ' + $WorkDir)
        }
        $workRoot = Git-Text -Directory $WorkDir -GitArguments @('rev-parse', '--show-toplevel')
        if ([System.IO.Path]::GetFullPath($workRoot).TrimEnd('\') -ine [System.IO.Path]::GetFullPath($WorkDir).TrimEnd('\')) {
            throw ('Directory 4.0 diversa dalla radice Git: ' + $workRoot)
        }
        $branch = Git-Text -Directory $WorkDir -GitArguments @('branch', '--show-current')
        if ($branch -ne 'feature/recordsnext4-players') {
            throw ('Il worktree 4.0 esistente usa un branch inatteso: ' + $branch)
        }
        Write-Host 'Worktree 4.0 esistente riconosciuto. Non lo ricreo.' -ForegroundColor Green
    }
    else {
        # Solo se manca il worktree: verifichiamo il tag PRIMA di creare il branch.
        & git -C $RepoDir show-ref --verify --quiet 'refs/tags/v3.2.0'
        $tagResult = $LASTEXITCODE
        if ($tagResult -ne 0) {
            throw ('Impossibile creare worktree: refs/tags/v3.2.0 non verificabile (Git exit ' + $tagResult + ').')
        }
        Write-Host 'Creo il worktree 4.0 a partire dal tag v3.2.0...' -ForegroundColor Cyan
        & git -C $RepoDir worktree add -b 'feature/recordsnext4-players' $WorkDir 'v3.2.0'
        $addResult = $LASTEXITCODE
        if ($addResult -ne 0) { throw ('Creazione worktree fallita, Git exit ' + $addResult) }
    }

    $workHead = Git-Text -Directory $WorkDir -GitArguments @('rev-parse', 'HEAD')
    & git -C $WorkDir merge-base --is-ancestor 'f9bf30323d9204d70e91e242bddfc72c2060ecf9' $workHead
    $ancestorExit = $LASTEXITCODE
    if ($ancestorExit -ne 0) {
        throw ('Il worktree 4.0 non discende dal commit base f9bf303 (Git exit ' + $ancestorExit + ').')
    }
    Write-Host ('Worktree HEAD: ' + $workHead)
    $files = @(
        'docs\REVISIONE_BASE_RECORDSNEXT_3_2_v1.md',
        'docs\PIANO_RECORDSNEXT4_GIOCATORI_v3.md',
        'docs\REGISTRO_DECISIONI_RECORDSNEXT4_v2.md',
        'docs\DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v2.md',
        'tools\Audit-RecordsNext4Giocatori-v1.py',
        'tools\Probe-RecordsNext4Fonti-v1.py',
        'tools\Probe-RecordsNext4Tabellini-v1.py',
        'tools\Create-RecordsNext4WorkingCodeMd-v2.ps1',
        'tools\Invoke-RecordsNext4-Fase0-v3.ps1'
    )
    # Controllo preliminare: nessuna copia parziale se esiste un file differente.
    foreach ($f in $files) {
        $src = Join-Path $kitRoot $f
        $dst = Join-Path $WorkDir $f
        if (-not (Test-Path -LiteralPath $src -PathType Leaf)) { throw ('File kit mancante: ' + $src) }
        if (Test-Path -LiteralPath $dst -PathType Leaf) {
            if ((Get-FileHash -LiteralPath $src -Algorithm SHA256).Hash -ne (Get-FileHash -LiteralPath $dst -Algorithm SHA256).Hash) {
                throw ('File gia presente ma diverso. Nessuna sovrascrittura: ' + $dst)
            }
        }
    }
    foreach ($f in $files) {
        $src = Join-Path $kitRoot $f
        $dst = Join-Path $WorkDir $f
        if (-not (Test-Path -LiteralPath $dst -PathType Leaf)) {
            $parent = Split-Path -Parent $dst
            if (-not (Test-Path -LiteralPath $parent -PathType Container)) {
                New-Item -ItemType Directory -Path $parent -Force | Out-Null
            }
            Copy-Item -LiteralPath $src -Destination $dst
            Write-Host ('Installato: ' + $f)
        } else { Write-Host ('Gia presente identico: ' + $f) }
    }
    & (Join-Path $WorkDir 'tools\Create-RecordsNext4WorkingCodeMd-v2.ps1') -ProjectDir $WorkDir

    Run-Python -Script (Join-Path $WorkDir 'tools\Audit-RecordsNext4Giocatori-v1.py') -ScriptArguments @('--database',$Database,'--code','100843','--output-dir',$runDir) -LogFile (Join-Path $runDir 'LOG_giocatore_100843.txt')
    Run-Python -Script (Join-Path $WorkDir 'tools\Audit-RecordsNext4Giocatori-v1.py') -ScriptArguments @('--database',$Database,'--code','12302','--output-dir',$runDir) -LogFile (Join-Path $runDir 'LOG_giocatore_12302.txt')
    Run-Python -Script (Join-Path $WorkDir 'tools\Probe-RecordsNext4Fonti-v1.py') -ScriptArguments @('--database',$Database,'--output-dir',$runDir) -LogFile (Join-Path $runDir 'LOG_fonti.txt')
    Run-Python -Script (Join-Path $WorkDir 'tools\Probe-RecordsNext4Tabellini-v1.py') -ScriptArguments @('--database',$Database,'--output-dir',$runDir) -LogFile (Join-Path $runDir 'LOG_tabellini.txt')

    $manifest = @(
        'RECORDSNEXT4 FASE 0 - v3',
        ('Data: ' + (Get-Date -Format 'yyyy-MM-dd HH:mm:ss')),
        ('Repo 3.2: ' + $RepoDir), ('Repo HEAD: ' + $originalHead),
        ('Worktree 4.0: ' + $WorkDir), ('Worktree HEAD: ' + $workHead),
        ('Database: ' + $Database),
        ('SHA256 database: ' + (Get-FileHash -LiteralPath $Database -Algorithm SHA256).Hash),
        'SQLITE letto con mode=ro e query_only=ON. Nessun sito pubblicato.'
    )
    $manifest | Out-File -LiteralPath (Join-Path $runDir 'MANIFEST_RUN_v3.txt') -Encoding UTF8
    & git -C $RepoDir status -sb | Out-File -LiteralPath (Join-Path $runDir 'GIT_32_STATO.txt') -Encoding UTF8
    & git -C $WorkDir status -sb | Out-File -LiteralPath (Join-Path $runDir 'GIT_40_STATO.txt') -Encoding UTF8
    $docs = Join-Path $runDir 'documentazione'
    New-Item -ItemType Directory -Path $docs | Out-Null
    foreach ($f in @('CODICE_FUNZIONANTE_RECORDSNEXT4_v2.md','REVISIONE_BASE_RECORDSNEXT_3_2_v1.md','PIANO_RECORDSNEXT4_GIOCATORI_v3.md','REGISTRO_DECISIONI_RECORDSNEXT4_v2.md','DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v2.md')) {
        Copy-Item -LiteralPath (Join-Path (Join-Path $WorkDir 'docs') $f) -Destination $docs
    }
    $ok = $true
}
catch {
    ($_ | Out-String) | Out-File -LiteralPath (Join-Path $runDir 'ERRORE_FASE0_v3.txt') -Encoding UTF8
    Write-Host ('Fase 0 interrotta: ' + $_.Exception.Message) -ForegroundColor Red
    throw
}
finally {
    if (Test-Path -LiteralPath $runDir -PathType Container) {
        try {
            Compress-Archive -Path (Join-Path $runDir '*') -DestinationPath $zipOut -CompressionLevel Optimal
            Write-Host ('ZIP risultati/diagnostica da caricare: ' + $zipOut) -ForegroundColor Cyan
        }
        catch { Write-Warning ('ZIP diagnostico non creato: ' + $_.Exception.Message) }
    }
}
if ($ok) {
    Write-Host 'Fase 0 v3 eseguita. Non eseguire git add/commit o pubblicazioni.' -ForegroundColor Green
}
~~~~

## `tools/Create-RecordsNext4WorkingCodeMd-v2.ps1`

SHA256: `2d41d02881a7387fee7f4364810197a1e653f8fb59a3642ad9a58d9453859279`

~~~~text
[CmdletBinding()]
param(
    [string]$ProjectDir = 'D:\DEV_APPS\RecordsNext4.0'
)
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

if (-not (Test-Path -LiteralPath (Join-Path $ProjectDir 'pom.xml') -PathType Leaf)) {
    throw ('Progetto RecordsNext non trovato: ' + $ProjectDir)
}
$docDir = Join-Path $ProjectDir 'docs'
$destination = Join-Path $docDir 'CODICE_FUNZIONANTE_RECORDSNEXT4_v2.md'
$files = @(
    'docs\REVISIONE_BASE_RECORDSNEXT_3_2_v1.md',
    'docs\PIANO_RECORDSNEXT4_GIOCATORI_v3.md',
    'docs\REGISTRO_DECISIONI_RECORDSNEXT4_v2.md',
    'docs\DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v2.md',
    'tools\Audit-RecordsNext4Giocatori-v1.py',
    'tools\Probe-RecordsNext4Fonti-v1.py',
    'tools\Probe-RecordsNext4Tabellini-v1.py',
    'tools\Create-RecordsNext4WorkingCodeMd-v2.ps1',
    'tools\Invoke-RecordsNext4-Fase0-v3.ps1'
)
foreach ($f in $files) {
    if (-not (Test-Path -LiteralPath (Join-Path $ProjectDir $f) -PathType Leaf)) {
        throw ('File necessario alla bibbia non trovato: ' + $f)
    }
}
$commitLines = & git -C $ProjectDir rev-parse HEAD
$exitCode = $LASTEXITCODE
if ($exitCode -ne 0) { throw 'Impossibile leggere HEAD Git.' }
$branchLines = & git -C $ProjectDir branch --show-current
$exitCode = $LASTEXITCODE
if ($exitCode -ne 0) { throw 'Impossibile leggere il branch Git.' }
$commit = ($commitLines -join '').Trim()
$branch = ($branchLines -join '').Trim()
$b = New-Object System.Text.StringBuilder
[void]$b.AppendLine('# RecordsNext 4.0 - Bibbia incrementale v2 (Fase 0)')
[void]$b.AppendLine('')
[void]$b.AppendLine('> Documentazione e codice diagnostico, NON una release 4.0 funzionante.')
[void]$b.AppendLine('> Base 3.2 immutata: docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md.')
[void]$b.AppendLine('> I dati della fase 0 sono nei report di D:\DEV_APPS\downloads, non certificati da questo file.')
[void]$b.AppendLine('')
[void]$b.AppendLine(('Generato: ' + (Get-Date -Format 'yyyy-MM-dd HH:mm:ss')))
[void]$b.AppendLine(('Branch: ' + $branch))
[void]$b.AppendLine(('Commit: ' + $commit))
[void]$b.AppendLine('')
[void]$b.AppendLine('## Stato verificabile')
[void]$b.AppendLine('')
[void]$b.AppendLine('- Base sorgente v3.2.0; nessuna variazione a codice Java, schema SQL, GUI o JS pubblici.')
[void]$b.AppendLine('- Verifiche diagnostiche previste: identificativi, famiglie raw, riferimenti tabellini e candidati riposo.')
[void]$b.AppendLine('- I tabellini senza Incontro non vengono dichiarati corrotti o a riposo in assenza di prova.')
[void]$b.AppendLine('- Stato locale dei test: consultare log e report ZIP generati dal runner v3.')
[void]$b.AppendLine('')
foreach ($f in $files) {
    $p = Join-Path $ProjectDir $f
    $hash = (Get-FileHash -LiteralPath $p -Algorithm SHA256).Hash
    [void]$b.AppendLine(('## ' + ($f -replace '\', '/')))
    [void]$b.AppendLine('')
    [void]$b.AppendLine(('SHA256: ' + $hash))
    [void]$b.AppendLine('')
    [void]$b.AppendLine('~~~~text')
    foreach ($line in @(Get-Content -LiteralPath $p -Encoding UTF8)) {
        [void]$b.AppendLine($line)
    }
    [void]$b.AppendLine('~~~~')
    [void]$b.AppendLine('')
}
$enc = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllText($destination, $b.ToString(), $enc)
Write-Host ('Bibbia incrementale 4.0 v2 generata: ' + $destination)
~~~~

## `tools/Create-RecordsNext4WorkingCodeMd-v3.py`

SHA256: `1d83e6b6adf1610529f2445553aa65e72d6ab04ec7286b3ddb19a96bfd0a92dc`

~~~~text
#!/usr/bin/env python3
"""Genera la bibbia incrementale RecordsNext 4, Fase 0 (v3).

Non esegue il precedente generatore PowerShell v2, che contiene una regex invalida.
Non modifica codice o database. Non sovrascrive una bibbia gia generata diversa.
"""
import argparse
import hashlib
import os
from pathlib import Path
import subprocess
import sys

BASE_COMMIT = 'f9bf30323d9204d70e91e242bddfc72c2060ecf9'
FILES = [
    'docs/REVISIONE_BASE_RECORDSNEXT_3_2_v1.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v3.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v2.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v2.md',
    'docs/CORREZIONE_FASE0_RECORDSNEXT4_v1.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v4.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v3.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v3.md',
    'tools/Audit-RecordsNext4Giocatori-v1.py',
    'tools/Probe-RecordsNext4Fonti-v1.py',
    'tools/Probe-RecordsNext4Tabellini-v1.py',
    'tools/Invoke-RecordsNext4-Fase0-v3.ps1',
    'tools/Create-RecordsNext4WorkingCodeMd-v2.ps1',
    'tools/Create-RecordsNext4WorkingCodeMd-v3.py',
    'tools/Invoke-RecordsNext4-Fase0-v4.py',
]
OUTPUT = 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v3.md'


def git(project: Path, *args: str) -> str:
    proc = subprocess.run(['git', '-C', str(project), *args], text=True,
                          encoding='utf-8', errors='replace', capture_output=True)
    if proc.returncode:
        raise RuntimeError('Git failed: ' + ' '.join(args) + ': ' + proc.stderr.strip())
    return proc.stdout.strip()


def make(project: Path) -> Path:
    missing = [name for name in FILES if not (project / name).is_file()]
    if missing:
        raise RuntimeError('Missing inputs for documentation: ' + ', '.join(missing))
    base_doc = project / 'docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md'
    if not base_doc.is_file():
        raise RuntimeError('Bibbia storica 3.2 mancante (non la modifichiamo).')
    commit = git(project, 'rev-parse', 'HEAD')
    branch = git(project, 'branch', '--show-current')
    lines = [
        '# RecordsNext 4.0 - Bibbia incrementale v3 (Fase 0)', '',
        '> DOCUMENTAZIONE DIAGNOSTICA. Non equivale alla release 4.0.',
        '> Base immutata: docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md.',
        '> Il vecchio generatore PowerShell v2 e conservato solo come storico:',
        '> falliva su -replace con regex backslash non valida.', '',
        f'Commit di partenza 3.2: `{BASE_COMMIT}`',
        f'Worktree branch: `{branch}`', f'Worktree HEAD: `{commit}`',
        'Output diagnostici: D:/DEV_APPS/downloads/RecordsNext4_Fase0_OUTPUT_v4_*.zip',
        '', '## Stato', '',
        '- Nessuna modifica a Java, SQLite, GUI e JS di produzione.',
        '- Runner fase 0 v4 scritto in Python per evitare fragilita della sintassi PS 5.1.',
        '- Le sond(e) FCM/FCA sono audit; non certificano di per se le identita all-time.',
        '- I tabellini con Incontro mancante restano non classificati fino alla verifica.',
        '- Gli esiti Windows vanno letti nel manifest e nei log dello ZIP, non dedotti da questa bibbia.',
        '',
        '## Base documentale storica', '',
        f'- `docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md` SHA256: `{hashlib.sha256(base_doc.read_bytes()).hexdigest()}`',
        '',
    ]
    for name in FILES:
        source = project / name
        payload = source.read_bytes()
        digest = hashlib.sha256(payload).hexdigest()
        text = payload.decode('utf-8-sig')
        lines.extend([f'## `{name}`', '', f'SHA256: `{digest}`', '', '~~~~text',
                      text.rstrip('\n'), '~~~~', ''])
    result = ('\n'.join(lines).rstrip() + '\n').encode('utf-8')
    target = project / OUTPUT
    if target.exists():
        if target.read_bytes() != result:
            raise RuntimeError('Bibbia v3 esistente diversa, non sovrascritta: ' + str(target))
        print('Bibbia v3 gia presente identica: ' + str(target), flush=True)
        return target
    try:
        with target.open('xb') as handle:
            handle.write(result)
    except FileExistsError:
        raise RuntimeError('Bibbia v3 comparsa durante la generazione: ' + str(target))
    print('Bibbia v3 generata: ' + str(target), flush=True)
    return target


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument('--project-dir', type=Path, required=True)
    args = parser.parse_args()
    try:
        make(args.project_dir.resolve())
    except Exception as error:
        print('ERRORE GENERATORE: ' + str(error), file=sys.stderr)
        sys.exit(1)


if __name__ == '__main__':
    main()
~~~~

## `tools/Invoke-RecordsNext4-Fase0-v4.py`

SHA256: `44ce93622331c9b0d2c08eb52c7bceaaff48b9454651eaefbef1f2512a1d82e3`

~~~~text
#!/usr/bin/env python3
"""Ripresa conservativa Fase 0 RecordsNext 4.0 dopo errore bibbia v2.

Esecuzione offline, Python standard library, nessuna operazione sul DB in scrittura.
Produce ZIP diagnostico anche in caso di errore.
"""
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

BASE_COMMIT = 'f9bf30323d9204d70e91e242bddfc72c2060ecf9'
PATCH_FILES = [
    'docs/CORREZIONE_FASE0_RECORDSNEXT4_v1.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v4.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v3.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v3.md',
    'tools/Create-RecordsNext4WorkingCodeMd-v3.py',
    'tools/Invoke-RecordsNext4-Fase0-v4.py',
]
REQUIRED_PREVIOUS = [
    'docs/REVISIONE_BASE_RECORDSNEXT_3_2_v1.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v3.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v2.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v2.md',
    'tools/Audit-RecordsNext4Giocatori-v1.py',
    'tools/Probe-RecordsNext4Fonti-v1.py',
    'tools/Probe-RecordsNext4Tabellini-v1.py',
    'tools/Invoke-RecordsNext4-Fase0-v3.ps1',
    'tools/Create-RecordsNext4WorkingCodeMd-v2.ps1',
]


def run_checked(args, cwd=None):
    p = subprocess.run(args, cwd=cwd, text=True, encoding='utf-8',
                       errors='replace', capture_output=True)
    if p.returncode:
        raise RuntimeError('Comando fallito (%d): %s\n%s' %
                           (p.returncode, ' '.join(map(str, args)), p.stderr[-4000:]))
    return p.stdout.strip()


def sha256(path):
    digest = hashlib.sha256()
    with path.open('rb') as handle:
        for block in iter(lambda: handle.read(4 * 1024 * 1024), b''):
            digest.update(block)
    return digest.hexdigest()


def same_path(a, b):
    return os.path.normcase(str(a.resolve())) == os.path.normcase(str(b.resolve()))


def write_text(path, text):
    path.write_text(text, encoding='utf-8')


def check_project(project):
    if not (project / 'pom.xml').is_file():
        raise RuntimeError('Worktree 4.0 senza pom.xml: ' + str(project))
    root = Path(run_checked(['git', '-C', str(project), 'rev-parse', '--show-toplevel']))
    if not same_path(root, project):
        raise RuntimeError('Radice worktree inattesa: ' + str(root))
    branch = run_checked(['git', '-C', str(project), 'branch', '--show-current'])
    if branch != 'feature/recordsnext4-players':
        raise RuntimeError('Branch inatteso: ' + branch)
    head = run_checked(['git', '-C', str(project), 'rev-parse', 'HEAD'])
    run_checked(['git', '-C', str(project), 'merge-base', '--is-ancestor', BASE_COMMIT, head])
    for name in REQUIRED_PREVIOUS:
        if not (project / name).is_file():
            raise RuntimeError('File v3 non trovato nel worktree: ' + name)
    return head


def install(patch_root, project):
    for name in PATCH_FILES:
        src, dst = patch_root / name, project / name
        if not src.is_file():
            raise RuntimeError('File del kit v4 mancante: ' + name)
        if dst.exists() and sha256(src) != sha256(dst):
            raise RuntimeError('File v4 diverso gia presente, nessuna sovrascrittura: ' + name)
    for name in PATCH_FILES:
        src, dst = patch_root / name, project / name
        if dst.exists():
            print('Gia presente identico:', name, flush=True)
            continue
        dst.parent.mkdir(parents=True, exist_ok=True)
        with dst.open('xb') as handle:
            handle.write(src.read_bytes())
        print('Installato:', name, flush=True)


def execute_audit(script, options, log):
    print('Verifica:', script.name, flush=True)
    p = subprocess.run([sys.executable, str(script), *options],
                       text=True, encoding='utf-8', errors='replace',
                       stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    write_text(log, p.stdout)
    print(p.stdout.strip()[-1300:], flush=True)
    if p.returncode:
        raise RuntimeError('Audit fallito (%d): %s; leggere %s' %
                           (p.returncode, script.name, log.name))


def main():
    parser = argparse.ArgumentParser(description='Riprende Fase 0 v4 (senza rieseguire il runner PS v3)')
    parser.add_argument('--work-dir', type=Path, default=Path('D:/DEV_APPS/RecordsNext4.0'))
    parser.add_argument('--database', type=Path, default=Path('D:/DEV_APPS/RecordsNext2.0/data/database/recordsnext-sandbox.db'))
    parser.add_argument('--output-dir', type=Path, default=Path('D:/DEV_APPS/downloads'))
    args = parser.parse_args()
    root = Path(__file__).resolve().parent.parent
    project = args.work_dir.resolve()
    db = args.database.resolve()
    out = args.output_dir.resolve()
    out.mkdir(parents=True, exist_ok=True)
    stamp = datetime.now().strftime('%Y%m%d_%H%M%S_%f')
    folder = out / ('RecordsNext4_Fase0_OUTPUT_v4_' + stamp)
    folder.mkdir()
    zip_path = out / (folder.name + '.zip')
    ok, head = False, ''
    try:
        if same_path(root, project):
            print('Kit v4 eseguito nel worktree: installazione identica.', flush=True)
        if not db.is_file():
            raise RuntimeError('Database sandbox mancante: ' + str(db))
        head = check_project(project)
        print('Worktree esistente, ramo corretto, base 3.2 verificata:', head, flush=True)
        install(root, project)
        # Nessuna chiamata al generatore PowerShell v2 difettoso.
        execute_audit(project / 'tools/Create-RecordsNext4WorkingCodeMd-v3.py',
                      ['--project-dir', str(project)], folder / 'LOG_BIBBIA_v3.txt')
        for code in ('100843', '12302'):
            execute_audit(project / 'tools/Audit-RecordsNext4Giocatori-v1.py',
                          ['--database', str(db), '--code', code, '--output-dir', str(folder)],
                          folder / ('LOG_GIOCATORE_' + code + '.txt'))
        execute_audit(project / 'tools/Probe-RecordsNext4Fonti-v1.py',
                      ['--database', str(db), '--output-dir', str(folder)], folder / 'LOG_FONTI.txt')
        execute_audit(project / 'tools/Probe-RecordsNext4Tabellini-v1.py',
                      ['--database', str(db), '--output-dir', str(folder)], folder / 'LOG_TABELLINI.txt')
        doc_dir = folder / 'documentazione'
        doc_dir.mkdir()
        for name in ('CODICE_FUNZIONANTE_RECORDSNEXT4_v3.md',
                     'PIANO_RECORDSNEXT4_GIOCATORI_v4.md',
                     'REGISTRO_DECISIONI_RECORDSNEXT4_v3.md',
                     'DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v3.md',
                     'CORREZIONE_FASE0_RECORDSNEXT4_v1.md'):
            shutil.copy2(project / 'docs' / name, doc_dir / name)
        write_text(folder / 'GIT_WORKTREE_v4.txt',
                   run_checked(['git', '-C', str(project), 'status', '--short', '--branch']) + '\n')
        manifest = {
            'fase': '0', 'kit': 'v4', 'stato': 'AUDIT_ESEGUITI', 'worktree': str(project),
            'worktree_head': head, 'db_sola_lettura': str(db),
            'db_sha256': sha256(db), 'nessun_aggiornamento_git': True,
            'nessuna_modifica_db': True,
            'nota': 'Tabellini non collegati: classificazione aperta; attenzione ai possibili riposi.',
        }
        write_text(folder / 'MANIFEST_v4.json', json.dumps(manifest, ensure_ascii=False, indent=2))
        ok = True
    except Exception:
        error = traceback.format_exc()
        write_text(folder / 'ERRORE_v4.txt', error)
        print('ERRORE FASE 0 v4:\n' + error[-2500:], file=sys.stderr, flush=True)
    finally:
        with zipfile.ZipFile(zip_path, 'x', zipfile.ZIP_DEFLATED, compresslevel=6) as archive:
            for p in sorted(folder.rglob('*')):
                if p.is_file():
                    archive.write(p, p.relative_to(folder).as_posix())
        print('ZIP DA CARICARE:', zip_path, flush=True)
        print('ESITO:', 'OK' if ok else 'ERRORE', flush=True)
    return 0 if ok else 1


if __name__ == '__main__':
    sys.exit(main())
~~~~

## `docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v3.md`

SHA256: `6b5839325be557733136fe6c9b98c1fa239ebe30ffc57e0578ed847db1f45f7b`

~~~~text
# RecordsNext 4.0 - Bibbia incrementale v3 (Fase 0)

> DOCUMENTAZIONE DIAGNOSTICA. Non equivale alla release 4.0.
> Base immutata: docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md.
> Il vecchio generatore PowerShell v2 e conservato solo come storico:
> falliva su -replace con regex backslash non valida.

Commit di partenza 3.2: `f9bf30323d9204d70e91e242bddfc72c2060ecf9`
Worktree branch: `feature/recordsnext4-players`
Worktree HEAD: `f9bf30323d9204d70e91e242bddfc72c2060ecf9`
Output diagnostici: D:/DEV_APPS/downloads/RecordsNext4_Fase0_OUTPUT_v4_*.zip

## Stato

- Nessuna modifica a Java, SQLite, GUI e JS di produzione.
- Runner fase 0 v4 scritto in Python per evitare fragilita della sintassi PS 5.1.
- Le sond(e) FCM/FCA sono audit; non certificano di per se le identita all-time.
- I tabellini con Incontro mancante restano non classificati fino alla verifica.
- Gli esiti Windows vanno letti nel manifest e nei log dello ZIP, non dedotti da questa bibbia.

## Base documentale storica

- `docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md` SHA256: `91ecb0006d6d1c6fda501c7243ba65ec2f0625c40e93394079238eac558a7da3`

## `docs/REVISIONE_BASE_RECORDSNEXT_3_2_v1.md`

SHA256: `a9e18047460ad75116043a572e0c71f06bd40c1ddef96015fb81d790190ec636`

~~~~text
# RecordsNext 4.0 — Revisione della base 3.2.0, v1

Data: 2026-10-31. Documento ricavato dall'archivio locale `RecordsNext2.0.zip` fornito dall'utente, NON da una supposizione sulla release. La versione 3.2 e la base invariata di riferimento.

## Fonti effettivamente ricevute e consultate

- `README.md`, `CHANGELOG.md`, `INSTALL.md`, `pom.xml`.
- `docs/ARCHITETTURA_RECORDSNEXT2.md`, `MODELLO_DATI_RECORDSNEXT2.md`, `STATO_IMPLEMENTAZIONE_RECORDSNEXT2.md`, `CONFIGURAZIONE_RECORDSNEXT2.md`, `CATALOGO_RECORD.md`, `DECISIONI_APERTE.md`, `DIPENDENZE_OUTPUT.md`, `INSTALLAZIONE_VISUALIZZATORI_HTML.md` e la restante documentazione sotto `docs/`.
- `docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md`: bibbia di circa 1,8 MB / 43.583 righe, con codice Java/test/scripts inglobato nel documento.
- `tools/Create-RecordsNext2WorkingCodeMd.ps1`: generatore della bibbia 3.2; NON va invocato per fingere che il codice di Fase 0 4.0 sia gia integrato nella base funzionante.
- Cartelle `src/main/java`, `src/test/java`, `tools`, configurazioni, metadati `.git` dell'archivio.

## Stato della 3.2 documentato nel progetto

- Release 3.2.0, commit base `f9bf30323d9204d70e91e242bddfc72c2060ecf9`; il tag annotato `v3.2.0` esiste nell'archivio e punta a tale commit.
- Il worktree 4.0 `feature/recordsnext4-players` risulta gia registrato nei metadati Git forniti; l'esistenza reale sul PC deve essere confermata dal runner.
- Modello modulare con 94 record/figli (93 configurabili + Culometro opzionale); pipeline, preflight, manifest e GUI gia funzionanti nella 3.2.
- Nove output pubblici RecordsNext; shard per stagione in formato **flat nella cartella `js` di ogni sito**, soggetti a cutoff cronologico per sito e a disponibilita storica.
- Le identita canoniche di fantasquadre e competizioni esistono gia; evitare mapping inventati.
- La bibbia 3.2 e una fotografia storica che include anche sezioni di versioni precedenti: le sezioni piu recenti prevalgono sulle note legacy.
- Nel modello `MODELLO_DATI_RECORDSNEXT2.md` sezioni 15-17 sono descritti concettualmente `Giocatore`, `Presenza giocatore`, `Evento giocatore`. Sono specifiche concettuali, non prova di tabelle canoniche gia implementate.

## Identificativi individuali

- `FCA.GiocatoreA.ID` e stagionale; e il riferimento usato nelle relazioni `FCM.Formazione.IDGioc`, `FCM.Tesserato.IDGiocatore`, `FCA.GiocaIn.IDGiocatore` per QUELLA stagione.
- Il codice storico usato per le fotografie risulta in `FCA.GiocatoreA.CODICEGAZZA` nelle tabelle della copia SQLite ricevuta. L'utente indica il codice stabile nel sistema FCM; va verificato il campo esatto del sorgente FCM senza confonderlo con ID di Formazione.
- Non unire automaticamente persone diverse con identico codice, ne trasformare ID annuali in identita all-time.

## Incontri a riposo e tabellini senza incontro: DISTINZIONE OBBLIGATORIA

La documentazione `ARCHITETTURA_RECORDSNEXT2.md` stabilisce esplicitamente: **i turni di riposo non fanno parte di Matches**. Questo non nega che un turno di riposo possa comparire nella sorgente FCM.

Nella copia SQLite analizzata:

- 2025/26: 90 record in `Incontro` con `IDTIPO=2` e un lato squadra uguale a zero; sono candidati coerenti con i riposi, ma la semantica va confermata sul manuale FCM e sugli esempi.
- Esistono anche tabellini con `IDINCONTRO` assente dalla tabella `Incontro`: 168 righe su 15 stagioni nella copia ricevuta. Nei casi esaminati non compaiono righe `Formazione` con lo stesso ID incontro. Alcuni tabellini contengono punteggi plausibili, quindi l'assenza non dimostra un riposo, ne corruzione.
- I due insiemi **non sono intercambiabili**: per classificare un ID mancante servono altri dati/campi o evidenze della fonte originale. Nel frattempo conservarlo come non classificato; non cancellare e non assegnare una competizione o giornata inventata.

## Implicazioni per fase 0

- Corretto il runner v2: controllava il tag prima del worktree esistente e non produceva diagnosi affidabili degli errori Git. La causa interna precisa dell'errore sul PC non e dimostrata; non affermare che il tag manchi.
- Runner v3: verifica prima il worktree esistente, ne controlla ramo e discendenza dal commit 3.2, copia soltanto nuovi file versionati, non sovrascrive file diversi, scrive report in `D:\DEV_APPS\downloads`.
- Sonda aggiuntiva v1 dedicata a possibili riposi e tabellini senza incontro, senza interpretazioni arbitrarie.
- Non cambiare versione Maven, importatori, schema canonico, visualizzatori o GUI in fase 0.

## Verifiche ancora da fare

- Esecuzione PS 5.1 sul PC dell'utente (qui sono stati verificati gli script Python; non e disponibile un runtime PowerShell Windows per collaudare i runner).
- Verifica dei codici storici direttamente sui FCM/FCA originali e del campo FCM esatto, con audit degli ID e delle fotografie.
- Semantica di `Entrato`, `EntratoRiserva`, `Pos`, `Rig`, RU, SV, modificatori e contabilizzazione dei punti non sfruttati.
- Trattamento documentato della stagione manuale senza FCM.
~~~~

## `docs/PIANO_RECORDSNEXT4_GIOCATORI_v3.md`

SHA256: `35be9de9850f8e3bd5d85ff149f82701da757b3e5f21387777d971bca47d74c7`

~~~~text
# RecordsNext 4.0.0 — Piano giocatori v3

Data: 2026-10-31. Stato: **FASE 0, AUDIT; NESSUNA RELEASE 4.0.0 ANCORA REALIZZATA**.

## Obiettivo

Per ciascun calciatore estrarre TUTTI i dati reali disponibili per stagione e giornata FCA, TUTTI i dati fantasy per stagione, fantasquadra, competizione e partita FCM, quindi produrre statistiche e record reali e fantasy per stagione, squadra, competizione e all time.

## Base tecnica vincolante

Il progetto di riferimento e **RecordsNext v3.2.0** del 2026-09-16, commit `f9bf303`, con documentazione effettiva e sorgenti presenti in `RecordsNext2.0.zip`. Il registro delle decisioni e la revisione `REVISIONE_BASE_RECORDSNEXT_3_2_v1.md` fanno parte della continuita operativa. Il worktree `D:\DEV_APPS\RecordsNext4.0` puo gia esistere e non deve essere ricreato automaticamente se valido.

La 3.2 ha 9 output, architettura multistagione e multisito, JS stagionali flat direttamente nella `js` del relativo sito e cutoff di ogni sito alla sua stagione. Mantenerli invariati finche il nuovo modulo non e stato implementato e testato.

## Identificativi

- `GiocatoreA.ID`: ID **locale FCA stagionale**.
- `Formazione.IDGioc`, `Tesserato.IDGiocatore`, `GiocaIn.IDGiocatore`: collegano l'ID locale alla stagione corrispondente.
- `GiocatoreA.CodiceGazza` negli FCA analizzati: codice storico usato nelle foto; l'utente riferisce il codice persistente nel sistema FCM, ma la sua ubicazione esatta nel file FCM va accertata. Esempio 100843 = Lautaro.
- Gli accoppiamenti pluristagionali richiedono trattamento di riutilizzi/anomalie nei codici; non si deve cambiare il significato dei campi esistenti.

## Entita logiche DA PROGETTARE (non implementate)

1. Identita canonica del calciatore / eventuale alias storicizzato.
2. Anagrafica stagionale: stagione + ID FCA locale e tutti i dati sorgente.
3. Prestazione reale: stagione + ID giocatore locale + giornata di Serie A; voti/eventi/contestualizzazione, con SV e dati mancanti separati da zero.
4. Partecipazione fantasy: stagione + partita FCM + fantasquadra + ID locale + occorrenza; ruolo e stato titolare/riserva/utilizzato, dettaglio tabellino e contributo ufficiale.
5. Evidenze di tesseramento: nessuna data trasferimento dedotta da righe non datate.
6. Record: dimensione reale o fantasy, ambito, filtro, soglie, denominatore, parita e provenienza fino al dato elementare.

Non contare una prestazione reale piu volte solo perche il fantacalciatore ha disputato piu competizioni legate alla stessa giornata.

## Incontri speciali e tabellini

`Matches` ESCLUDE i turni di riposo secondo architettura 3.2. Incontro con un lato squadra a zero e tabellino il cui `IDINCONTRO` manca da `Incontro` vanno censiti SEPARATAMENTE: non sono sinonimi, non eliminare nessuna riga e non costruire collegamenti fittizi.

## Roadmap e criteri di uscita

| Fase | Lavoro | Verifica | Stato |
|---|---|---|---|
| 0 | Worktree isolato; audit codici, schema, referenze, riposi/tabellini | Esiti riproducibili e Git 3.2 invariato | In corso |
| 1 | Dati reali per stagione/giornata, anche non tesserati | Confronto FCA `GiocaIn`, `Punteggio`, `Statistiche`, `Storico`; nessun doppio conteggio | Da iniziare |
| 2 | Dati fantasy per gara/competizione/squadra | Accordo con FCM `Formazione` e `Tabellino`; gestione riserve, RU, match speciali | Da iniziare |
| 3 | Catalogo record reali, fantasy e punti non sfruttati | Definizioni statistico-regolamentari e casi limite verificati | Da iniziare |
| 4 | Output JS e HTML/GUI | Cutoff multisito, shard flat, nessuna regressione 3.2 | Da iniziare |
| 5 | Regressioni, bibbia, FULL/UPDATE, release 4.0.0 | Test, commit e tag solo dopo approvazione | Da iniziare |

## Documentazione vivente

- Base storica 3.2: `docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md` (NON toccare).
- Nuova bibbia incrementale: `docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v2.md`, generata da `tools/Create-RecordsNext4WorkingCodeMd-v2.ps1`.
- Piano: questo file v3.
- Decisioni: `docs/REGISTRO_DECISIONI_RECORDSNEXT4_v2.md`.
- Diario: `docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v2.md`.
- Verifica base: `docs/REVISIONE_BASE_RECORDSNEXT_3_2_v1.md`.

Non dichiarare "funzionante" cio che e solamente progettato. Ogni successiva revisione sostituisce i nomi con nuove versioni; tenere traccia del codice completo e dei test nel generatore.
~~~~

## `docs/REGISTRO_DECISIONI_RECORDSNEXT4_v2.md`

SHA256: `7f20a072bc6e2d9e785713d7b6fa77a7334cc15942631f8371623759e824d88c`

~~~~text
# RecordsNext 4 — Registro decisioni v2

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

Nuova fase: aggiornare questo documento con nome v3, segnando le decisioni effettivamente approvate e i report che ne forniscono la prova. Mantenere storia delle modifiche nel diario.
~~~~

## `docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v2.md`

SHA256: `f656cd68f48c8d710e5dde48dd442fc4d8254f040b196c29edf30879b6cdf803`

~~~~text
# RecordsNext 4 — Diario implementazione v2

## 2026-10-31 — Revisione dell'archivio 3.2.0 e correzione Fase 0

- Ricevuto e analizzato `RecordsNext2.0.zip` contenente codice, script, documentazione, dati e metadati Git della release 3.2.0.
- Confermato commit `f9bf303...`, tag annotato `v3.2.0` e registrazione di `D:\DEV_APPS\RecordsNext4.0` come worktree sul ramo `feature/recordsnext4-players` nei metadati del repository. Non equivale a prova che ogni file sia presente attualmente sul PC: verifica nel runner.
- L'errore del runner Fase 0 v2 era nel preflight di verifica del tag: non procedere a ricreare il worktree esistente. Causa nativa esatta non disponibile dall'output; il runner v3 evita la verifica del tag quando il worktree e gia valido.
- Letta la struttura del generatore originale `tools/Create-RecordsNext2WorkingCodeMd.ps1`. La nuova bibbia v2 elenca file e SHA256 delle modifiche 4.0, lasciando inalterata la bibbia 3.2.
- Creato `tools/Probe-RecordsNext4Tabellini-v1.py` per distinguere statisticamente record `Incontro` con un lato squadra nullo/zero dai tabellini privi di riferimento `Incontro`.
- Verifica Python sul database caricato precedentemente: 21 stagioni, 15 con ID tabellini da verificare, 168 righe `Tabellino` senza `Incontro`. Il dato NON dimostra errore ne riposo.
- Nessuna modifica a Java, GUI, pubblicazione, archivi originali o database.

## Verifica necessaria sul PC Windows dell'utente

Avviare `tools/Invoke-RecordsNext4-Fase0-v3.ps1` dal kit v3 ed esaminare lo ZIP in `D:\DEV_APPS\downloads`. Non eseguire la versione v2 difettosa. Lo script PowerShell e stato revisionato staticamente per PS 5.1; **non e stato collaudato su una macchina Windows in questa sessione**. Gli script Python sono stati compilati e il nuovo audit tabellini e stato eseguito sul database di copia.

## Prossimi interventi

Dopo i report: verificare gli ID FCM/FCA originali e il codice foto, stabilire regole per riposi e tabellini residuali e progettare l'estrattore reale della fase 1. Nessun commit o release automatici.
~~~~

## `docs/CORREZIONE_FASE0_RECORDSNEXT4_v1.md`

SHA256: `054de67f8bf4e04cceb488eea4e8aac6ba88db245eb2e64dae1323e82d746b94`

~~~~text
# RecordsNext 4 - Correzione Fase 0, revisione v4

Data: 2026-10-31. Stato: correzione tecnica al runner, NON sviluppo di nuove tabelle o record.

## Errore riscontrato e causa verificata

Il 31/10/2026 il runner `Invoke-RecordsNext4-Fase0-v3.ps1` ha:

1. Verificato correttamente il worktree `D:\DEV_APPS\RecordsNext4.0`, ramo `feature/recordsnext4-players` e commit base 3.2 `f9bf303...`.
2. Installato nuovi file e riconosciuto identico lo script di audit giocatore v1.
3. Fallito durante la generazione della bibbia incrementale 4.0:
   `Create-RecordsNext4WorkingCodeMd-v2.ps1: Modello di espressione regolare \\ non valido`.

La causa esatta e la linea:

```powershell
($f -replace '\', '/')
```

In PowerShell `-replace` interpreta il pattern come regex: il backslash singolo non e un'espressione valida. La trasformazione letterale corretta e:

```powershell
$f.Replace('\', '/')
```

Il problema era nel codice consegnato, non nel sistema Windows dell'utente.
Non modificare in-place il generatore v2: rimane documento storico del difetto.

## Scelta correttiva

- Nuovo `tools/Create-RecordsNext4WorkingCodeMd-v3.py` e nuovo `tools/Invoke-RecordsNext4-Fase0-v4.py` (sola standard library Python, gia disponibile sul PC: 3.13.7).
- Nessun richiamo al generatore PowerShell v2 o al runner v3.
- Nessuna creazione del worktree: il v4 richiede quello gia installato e ne controlla il ramo e la discendenza da v3.2.0.
- Solo nuovi file con nome versionato; se un file preesiste con hash diverso, ci si ferma senza sovrascriverlo.
- Git viene interrogato ma non modifica commit, indice, branch o tag.
- Il database `recordsnext-sandbox.db` viene interrogato in sola lettura dai tool Python v1.
- ZIP dell'esecuzione anche su errore, sotto `D:\DEV_APPS\downloads`.

## Corretto trattamento delle anomalie FCM

L'utente segnala che i tabellini senza il normale incontro potrebbero riferirsi a squadre a riposo. La verifica resta aperta: non chiamarli automaticamente errori, ma neppure assegnarli a riposi senza riscontri nei file originali.

## Esecuzione

1. Scaricare `RecordsNext4_Fase0_Kit_v4.zip` in `D:\DEV_APPS\downloads`.
2. Estrarlo in `D:\DEV_APPS\downloads\RecordsNext4_Fase0_Kit_v4`.
3. Chiudere le elaborazioni che scrivono al database.
4. Lanciare con `py -3` lo script `tools\Invoke-RecordsNext4-Fase0-v4.py` dall'archivio estratto.
5. Caricare `RecordsNext4_Fase0_OUTPUT_v4_*.zip`, indipendentemente dal risultato, per controllare log e report.

## Condizioni di uscita

- Generatore bibbia v3 completato oppure errore dettagliato.
- Due audit codici, inventario fonti e probe tabellini completati e conservati in ZIP.
- Base 3.2.0 invariata. Nessun commit, nessuna pubblicazione.
~~~~

## `docs/PIANO_RECORDSNEXT4_GIOCATORI_v4.md`

SHA256: `0ac587a933a0a1f36e9dfc3ba9ec0a75071c1102ae373968174376e0e5f36253`

~~~~text
# RecordsNext 4.0.0 — Piano giocatori v4

Data: 2026-10-31. Stato: **FASE 0, AUDIT; NESSUNA RELEASE 4.0.0 ANCORA REALIZZATA**.

## Obiettivo

Per ciascun calciatore estrarre TUTTI i dati reali disponibili per stagione e giornata FCA, TUTTI i dati fantasy per stagione, fantasquadra, competizione e partita FCM, quindi produrre statistiche e record reali e fantasy per stagione, squadra, competizione e all time.

## Base tecnica vincolante

Il progetto di riferimento e **RecordsNext v3.2.0** del 2026-09-16, commit `f9bf303`, con documentazione effettiva e sorgenti presenti in `RecordsNext2.0.zip`. Il registro delle decisioni e la revisione `REVISIONE_BASE_RECORDSNEXT_3_2_v1.md` fanno parte della continuita operativa. Il worktree `D:\DEV_APPS\RecordsNext4.0` puo gia esistere e non deve essere ricreato automaticamente se valido.

La 3.2 ha 9 output, architettura multistagione e multisito, JS stagionali flat direttamente nella `js` del relativo sito e cutoff di ogni sito alla sua stagione. Mantenerli invariati finche il nuovo modulo non e stato implementato e testato.

## Identificativi

- `GiocatoreA.ID`: ID **locale FCA stagionale**.
- `Formazione.IDGioc`, `Tesserato.IDGiocatore`, `GiocaIn.IDGiocatore`: collegano l'ID locale alla stagione corrispondente.
- `GiocatoreA.CodiceGazza` negli FCA analizzati: codice storico usato nelle foto; l'utente riferisce il codice persistente nel sistema FCM, ma la sua ubicazione esatta nel file FCM va accertata. Esempio 100843 = Lautaro.
- Gli accoppiamenti pluristagionali richiedono trattamento di riutilizzi/anomalie nei codici; non si deve cambiare il significato dei campi esistenti.

## Entita logiche DA PROGETTARE (non implementate)

1. Identita canonica del calciatore / eventuale alias storicizzato.
2. Anagrafica stagionale: stagione + ID FCA locale e tutti i dati sorgente.
3. Prestazione reale: stagione + ID giocatore locale + giornata di Serie A; voti/eventi/contestualizzazione, con SV e dati mancanti separati da zero.
4. Partecipazione fantasy: stagione + partita FCM + fantasquadra + ID locale + occorrenza; ruolo e stato titolare/riserva/utilizzato, dettaglio tabellino e contributo ufficiale.
5. Evidenze di tesseramento: nessuna data trasferimento dedotta da righe non datate.
6. Record: dimensione reale o fantasy, ambito, filtro, soglie, denominatore, parita e provenienza fino al dato elementare.

Non contare una prestazione reale piu volte solo perche il fantacalciatore ha disputato piu competizioni legate alla stessa giornata.

## Incontri speciali e tabellini

`Matches` ESCLUDE i turni di riposo secondo architettura 3.2. Incontro con un lato squadra a zero e tabellino il cui `IDINCONTRO` manca da `Incontro` vanno censiti SEPARATAMENTE: non sono sinonimi, non eliminare nessuna riga e non costruire collegamenti fittizi.

## Roadmap e criteri di uscita

| Fase | Lavoro | Verifica | Stato |
|---|---|---|---|
| 0 | Worktree isolato; audit codici, schema, referenze, riposi/tabellini | Esiti riproducibili e Git 3.2 invariato | In corso |
| 1 | Dati reali per stagione/giornata, anche non tesserati | Confronto FCA `GiocaIn`, `Punteggio`, `Statistiche`, `Storico`; nessun doppio conteggio | Da iniziare |
| 2 | Dati fantasy per gara/competizione/squadra | Accordo con FCM `Formazione` e `Tabellino`; gestione riserve, RU, match speciali | Da iniziare |
| 3 | Catalogo record reali, fantasy e punti non sfruttati | Definizioni statistico-regolamentari e casi limite verificati | Da iniziare |
| 4 | Output JS e HTML/GUI | Cutoff multisito, shard flat, nessuna regressione 3.2 | Da iniziare |
| 5 | Regressioni, bibbia, FULL/UPDATE, release 4.0.0 | Test, commit e tag solo dopo approvazione | Da iniziare |

## Aggiornamento Fase 0 - recupero v4

- Il runner PowerShell v3 e stato eseguito ma si e fermato nel generatore bibbia PowerShell v2; il worktree e gli strumenti diagnostici risultano installati.
- Il comando di ripresa usa `tools/Invoke-RecordsNext4-Fase0-v4.py`, non modifica i file della release 3.2.
- Il generatore nuovo e `tools/Create-RecordsNext4WorkingCodeMd-v3.py`; la bibbia aggiornata e `docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v3.md`.
- Il risultato corretto va attestato solo dopo analisi dello ZIP v4.
- I riferimenti di tabellino senza `Incontro` non sono automaticamente errori: verificare esplicitamente le squadre a riposo.

## Documentazione vivente

- Base storica 3.2: `docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md` (NON toccare).
- Nuova bibbia incrementale: `docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v3.md`, generata da `tools/Create-RecordsNext4WorkingCodeMd-v3.py` (v2 storico difettoso).
- Piano: questo file v4.
- Decisioni: `docs/REGISTRO_DECISIONI_RECORDSNEXT4_v3.md`.
- Diario: `docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v3.md`.
- Verifica base: `docs/REVISIONE_BASE_RECORDSNEXT_3_2_v1.md`.

Non dichiarare "funzionante" cio che e solamente progettato. Ogni successiva revisione sostituisce i nomi con nuove versioni; tenere traccia del codice completo e dei test nel generatore.
~~~~

## `docs/REGISTRO_DECISIONI_RECORDSNEXT4_v3.md`

SHA256: `f6ea5100fd01cef2bfccafbb2f22b39cd9ce3e36a020199bb2e1879494d27274`

~~~~text
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
~~~~

## `docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v3.md`

SHA256: `dc3f94dbb868153c600fac9ffbc771d04b1a226912a26a1f9c3570e2ec6458cf`

~~~~text
# RecordsNext 4 — Diario implementazione v3

## 2026-10-31 — Revisione dell'archivio 3.2.0 e correzione Fase 0

- Ricevuto e analizzato `RecordsNext2.0.zip` contenente codice, script, documentazione, dati e metadati Git della release 3.2.0.
- Confermato commit `f9bf303...`, tag annotato `v3.2.0` e registrazione di `D:\DEV_APPS\RecordsNext4.0` come worktree sul ramo `feature/recordsnext4-players` nei metadati del repository. Non equivale a prova che ogni file sia presente attualmente sul PC: verifica nel runner.
- L'errore del runner Fase 0 v2 era nel preflight di verifica del tag: non procedere a ricreare il worktree esistente. Causa nativa esatta non disponibile dall'output; il runner v3 evita la verifica del tag quando il worktree e gia valido.
- Letta la struttura del generatore originale `tools/Create-RecordsNext2WorkingCodeMd.ps1`. La nuova bibbia v2 elenca file e SHA256 delle modifiche 4.0, lasciando inalterata la bibbia 3.2.
- Creato `tools/Probe-RecordsNext4Tabellini-v1.py` per distinguere statisticamente record `Incontro` con un lato squadra nullo/zero dai tabellini privi di riferimento `Incontro`.
- Verifica Python sul database caricato precedentemente: 21 stagioni, 15 con ID tabellini da verificare, 168 righe `Tabellino` senza `Incontro`. Il dato NON dimostra errore ne riposo.
- Nessuna modifica a Java, GUI, pubblicazione, archivi originali o database.

## Verifica necessaria sul PC Windows dell'utente

Avviare `tools/Invoke-RecordsNext4-Fase0-v3.ps1` dal kit v3 ed esaminare lo ZIP in `D:\DEV_APPS\downloads`. Non eseguire la versione v2 difettosa. Lo script PowerShell e stato revisionato staticamente per PS 5.1; **non e stato collaudato su una macchina Windows in questa sessione**. Gli script Python sono stati compilati e il nuovo audit tabellini e stato eseguito sul database di copia.

## Prossimi interventi

Dopo i report: verificare gli ID FCM/FCA originali e il codice foto, stabilire regole per riposi e tabellini residuali e progettare l'estrattore reale della fase 1. Nessun commit o release automatici.

## 2026-10-31 - Correzione v4 dopo fallimento PowerShell v3

- Ricevuto output dell'utente: Python 3.13.7; worktree riconosciuto, base f9bf303 confermata.
- Errore nella bibbia v2: `($f -replace '\', '/')` con regex backslash singolo non valida.
- Conservato lo script v2 a fini di continuita; nessuna sovrascrittura.
- Introdotti generatore bibbia v3 Python e runner diagnostico v4 Python; la validazione sul PC deve essere confermata dal relativo ZIP.
- Preservata l'ipotesi riposi per alcuni tabellini non associati a incontri; non ancora classificati.
- Nessun cambiamento al codice produttivo Java, SQLite, GUI, configurazioni e JS dei siti.
- Prossimo passaggio: esame output v4 e aggiornamento stato Fase 0, poi progetto estrattore reale Fase 1.
~~~~

## `tools/Audit-RecordsNext4Giocatori-v1.py`

SHA256: `29f866e33eb9557d1a392648d2f144b1251225738f817379b4d0a66d8a859db5`

~~~~text
#!/usr/bin/env python3
"""RecordsNext 4.0 - audit di lettura dei codici giocatore negli FCA/FCM raw.

Non crea/modifica tabelle, non aggiorna configurazioni e non genera JS.
Usa esclusivamente Python 3 standard library.
"""

import argparse
import csv
import hashlib
import json
import re
import sqlite3
from datetime import datetime
from pathlib import Path


def quote_table(table):
    if not re.fullmatch(r"[A-Za-z0-9_]+", table):
        raise ValueError("Nome tabella non sicuro")
    return '"' + table + '"'


def scalar(conn, statement, values=()):
    return conn.execute(statement, values).fetchone()[0]


def file_sha256(path):
    hasher = hashlib.sha256()
    with path.open("rb") as handle:
        while True:
            chunk = handle.read(4 * 1024 * 1024)
            if not chunk:
                break
            hasher.update(chunk)
    return hasher.hexdigest()


def inspect_player(conn, known, season, code):
    prefix = "raw_" + season + "_"
    base = prefix + "fca_giocatorea"
    if base not in known:
        return [], False

    player_table = quote_table(base)
    player_rows = conn.execute(
        f"SELECT ID, NOME, CODICEGAZZA, RUOLO, DATADINASCITA, FOTO "
        f"FROM {player_table} WHERE CAST(CODICEGAZZA AS TEXT) = ? ORDER BY ID", (code,)
    ).fetchall()

    output = []
    for local_id, name, source_code, role, dob, photo in player_rows:
        row = {
            "stagione": season,
            "codice_storico_sorgente": str(source_code),
            "id_fca_stagionale": local_id,
            "nome_fca": name,
            "ruolo_fca": role,
            "data_nascita_fca": dob,
            "foto_fca": photo,
            "giornate_giocain": None,
            "giornate_con_id_punteggio": None,
            "righe_formazione_fcm": None,
            "righe_formazione_entrato": None,
            "stato": "OK",
        }
        gi = prefix + "fca_giocain"
        if gi in known:
            t = quote_table(gi)
            a, b = conn.execute(
                f"SELECT COUNT(*), SUM(CASE WHEN IDPUNTEGGIO IS NOT NULL "
                f"AND IDPUNTEGGIO <> 0 THEN 1 ELSE 0 END) "
                f"FROM {t} WHERE IDGIOCATORE = ?", (local_id,)
            ).fetchone()
            row["giornate_giocain"] = a
            row["giornate_con_id_punteggio"] = b or 0
        else:
            row["stato"] = "GIOCAIN_ASSENTE"

        f = prefix + "fcm_formazione"
        if f in known:
            t = quote_table(f)
            a, b = conn.execute(
                f"SELECT COUNT(*), SUM(CASE WHEN ENTRATO IS NOT NULL "
                f"AND ENTRATO <> 0 THEN 1 ELSE 0 END) "
                f"FROM {t} WHERE IDGIOC = ?", (local_id,)
            ).fetchone()
            row["righe_formazione_fcm"] = a
            row["righe_formazione_entrato"] = b or 0
        else:
            row["stato"] += ";FORMAZIONE_ASSENTE"
        output.append(row)

    if len(player_rows) > 1:
        for row in output:
            row["stato"] += ";CODICE_NON_UNIVOCO_NELLA_STAGIONE"
    return output, True


def main():
    ap = argparse.ArgumentParser(description="Audit giocatore per codice storico FCA (sola lettura)")
    ap.add_argument("--database", required=True, type=Path, help="Copia del recordsnext.db")
    ap.add_argument("--code", required=True, help="Codice storico (es. 100843)")
    ap.add_argument("--output-dir", required=True, type=Path, help="Cartella per i due report")
    args = ap.parse_args()

    if not re.fullmatch(r"[0-9]+", args.code):
        ap.error("--code deve essere numerico")
    db = args.database.resolve()
    if not db.is_file():
        ap.error("Database non trovato: " + str(db))
    if args.code == "0":
        ap.error("Il codice 0 non identifica univocamente un calciatore")

    con = sqlite3.connect(db.as_uri() + "?mode=ro", uri=True)
    try:
        con.execute("PRAGMA query_only=ON")
        con.execute("PRAGMA busy_timeout=10000")
        known = {name for (name,) in con.execute(
            "SELECT name FROM sqlite_master WHERE type='table'")}
        seasons = sorted({m.group(1) for name in known
                          if (m := re.fullmatch(r"raw_(\d{4}_\d{4})_fca_giocatorea", name))})
        rows = []
        for season in seasons:
            batch, _ = inspect_player(con, known, season, args.code)
            rows.extend(batch)
    finally:
        con.close()

    # L'identita all-time non viene approvata automaticamente: questo audit
    # mostra solo le anagrafiche legate al codice cercato.
    names = sorted({str(r["nome_fca"]).strip() for r in rows})
    birthdates = sorted({str(r["data_nascita_fca"]).strip() for r in rows
                         if r["data_nascita_fca"] is not None})
    status = "NON_TROVATO" if not rows else (
        "REVISIONE_MANUALE" if len(names) > 1 or len(birthdates) > 1
        or any("NON_UNIVOCO" in r["stato"] for r in rows)
        else "SENZA_ANOMALIE_RILEVATE")

    now = datetime.now().strftime("%Y%m%d_%H%M%S")
    args.output_dir.mkdir(parents=True, exist_ok=True)
    basename = "RecordsNext4_AuditGiocatore_v1_" + args.code + "_" + now
    path_csv = args.output_dir / (basename + ".csv")
    path_json = args.output_dir / (basename + ".json")
    fields = ["stagione", "codice_storico_sorgente", "id_fca_stagionale",
              "nome_fca", "ruolo_fca", "data_nascita_fca", "foto_fca",
              "giornate_giocain", "giornate_con_id_punteggio",
              "righe_formazione_fcm", "righe_formazione_entrato", "stato"]
    with path_csv.open("w", encoding="utf-8-sig", newline="") as out:
        writer = csv.DictWriter(out, fieldnames=fields, delimiter=";")
        writer.writeheader()
        writer.writerows(rows)
    result = {
        "audit_version": "1",
        "project_target": "RecordsNext 4.0.0",
        "data_only_no_modifications": True,
        "database": str(db),
        "database_size": db.stat().st_size,
        "database_sha256": file_sha256(db),
        "code": args.code,
        "status": status,
        "seasons_with_fca_player_table": len(seasons),
        "seasons_with_matching_player": len({r["stagione"] for r in rows}),
        "matching_rows": len(rows),
        "names_found": names,
        "birthdates_found": birthdates,
        "note": "ID FCA locale; le righe GiocaIn non equivalgono necessariamente a presenze reali. "
                "Le righe Formazione non equivalgono necessariamente a utilizzi effettivi. "
                "Nessun record all-time e generato da questo audit.",
        "rows": rows,
    }
    path_json.write_text(json.dumps(result, ensure_ascii=False, indent=2, default=str), encoding="utf-8")
    print("Audit:", status)
    print("Stagioni FCA:", len(seasons), "| stagioni con codice:", result["seasons_with_matching_player"])
    print("Righe corrispondenti:", len(rows))
    print("CSV:", path_csv)
    print("JSON:", path_json)


if __name__ == "__main__":
    main()
~~~~

## `tools/Probe-RecordsNext4Fonti-v1.py`

SHA256: `eee34f1b7cfb59a06f1e5cc83b639ab587c762892e8bcacaa949d29b269a9b6a`

~~~~text
#!/usr/bin/env python3
"""RecordsNext4 fase 0: inventario/raw e integrita referenziale. SOLO LETTURA.

Non interpreta voti, non deduce identita all time, non modifica database e non
pubblica output; richiede Python 3 standard library.
"""
import argparse
import csv
import json
import re
import sqlite3
from datetime import datetime
from pathlib import Path

FAMILIES = (
    "fca_giocatorea", "fca_giocain", "fca_punteggio", "fca_statistiche",
    "fca_storico", "fca_squadradia", "fca_incontroina",
    "fcm_formazione", "fcm_tesserato", "fcm_tabellino",
    "fcm_incontro", "fcm_fantasquadra",
)
LINKS = (
    ("fcm_formazione", "IDGIOC", "fca_giocatorea", "ID", "formazione_giocatore"),
    ("fcm_tesserato", "IDGIOCATORE", "fca_giocatorea", "ID", "tesserato_giocatore"),
    ("fca_giocain", "IDGIOCATORE", "fca_giocatorea", "ID", "giocain_giocatore"),
    ("fcm_formazione", "IDINCONTRO", "fcm_incontro", "ID", "formazione_incontro"),
    ("fcm_tabellino", "IDINCONTRO", "fcm_incontro", "ID", "tabellino_incontro"),
)


def quoted(name):
    if not re.fullmatch(r"[A-Za-z0-9_]+", name):
        raise ValueError("Nome tabella imprevisto: " + name)
    return '"' + name + '"'


def norm(value):
    if value is None:
        return ""
    s = str(value).strip()
    if re.fullmatch(r"[+-]?\d+\.0+", s):
        s = s.split(".")[0]
    return s


def unique_values(conn, table, field):
    return {norm(v) for (v,) in conn.execute(
        "SELECT " + quoted(field) + " FROM " + quoted(table)
    ) if v is not None}


def write_csv(path, records, columns):
    with path.open("w", encoding="utf-8-sig", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=columns, delimiter=";")
        writer.writeheader()
        writer.writerows(records)


def main():
    ap = argparse.ArgumentParser(description="Inventario raw e relazioni FCM/FCA, sola lettura")
    ap.add_argument("--database", type=Path, required=True)
    ap.add_argument("--output-dir", type=Path, required=True)
    args = ap.parse_args()
    db = args.database.resolve()
    if not db.is_file():
        ap.error("Database non trovato: " + str(db))
    conn = sqlite3.connect(db.as_uri() + "?mode=ro", uri=True)
    conn.execute("PRAGMA query_only=ON")
    try:
        names = {name for (name,) in conn.execute(
            "SELECT name FROM sqlite_master WHERE type='table'"
        )}
        seasons = sorted({m.group(1) for name in names
                          if (m := re.fullmatch(r"raw_(\d{4}_\d{4})_fca_giocatorea", name))})
        # Non assumere che ogni stagione contenga necessariamente FCA.
        seasons = sorted(set(seasons) | {m.group(1) for name in names
                         if (m := re.fullmatch(r"raw_(\d{4}_\d{4})_fcm_formazione", name))})
        coverage, columns, rel, anomalous_codes = [], [], [], []
        for season in seasons:
            info = {}
            for family in FAMILIES:
                table = "raw_" + season + "_" + family
                if table not in names:
                    coverage.append({"stagione": season, "famiglia": family,
                                     "tabella": table, "presente": 0, "righe": ""})
                    continue
                count = conn.execute("SELECT COUNT(*) FROM " + quoted(table)).fetchone()[0]
                coverage.append({"stagione": season, "famiglia": family,
                                 "tabella": table, "presente": 1, "righe": count})
                cols = [row[1] for row in conn.execute("PRAGMA table_info(" + quoted(table) + ")")]
                info[family] = (table, set(cols), count)
                for col in cols:
                    columns.append({"stagione": season, "famiglia": family, "campo": col})
            for src, srcfield, dst, dstfield, label in LINKS:
                if src not in info or dst not in info:
                    rel.append({"stagione": season, "relazione": label, "stato": "TABELLA_ASSENTE",
                                "righe_origine": "", "riferimenti_mancanti": "", "distinti_mancanti": ""})
                    continue
                source, sourcecols, cnt = info[src]
                target, targetcols, _ = info[dst]
                if srcfield not in sourcecols or dstfield not in targetcols:
                    rel.append({"stagione": season, "relazione": label, "stato": "COLONNA_ASSENTE",
                                "righe_origine": cnt, "riferimenti_mancanti": "", "distinti_mancanti": ""})
                    continue
                # Conteggi esatti, lettura dei soli identificativi, senza creare indici o tabelle.
                valid = unique_values(conn, target, dstfield)
                missing = 0
                distinct = set()
                for (raw,) in conn.execute("SELECT " + quoted(srcfield) + " FROM " + quoted(source)):
                    key = norm(raw)
                    if key not in valid:
                        missing += 1
                        if len(distinct) < 10000:
                            distinct.add(key)
                rel.append({"stagione": season, "relazione": label, "stato": "OK" if missing == 0 else "DA_VERIFICARE",
                            "righe_origine": cnt, "riferimenti_mancanti": missing,
                            "distinti_mancanti": len(distinct) if len(distinct) < 10000 else ">=10000"})
            if "fca_giocatorea" in info:
                t, c, _ = info["fca_giocatorea"]
                if {"CODICEGAZZA", "NOME", "ID"}.issubset(c):
                    for code, n in conn.execute(
                        "SELECT CODICEGAZZA, COUNT(*) FROM " + quoted(t) +
                        " WHERE CODICEGAZZA IS NOT NULL AND CAST(CODICEGAZZA AS TEXT) <> '0' "
                        " GROUP BY CODICEGAZZA HAVING COUNT(*) > 1"
                    ):
                        anomalous_codes.append({"stagione": season, "codice": norm(code),
                                                "righe_stessa_stagione": n})
    finally:
        conn.close()
    folder = args.output_dir
    folder.mkdir(parents=True, exist_ok=True)
    stamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    prefix = "RecordsNext4_Fonti_v1_" + stamp
    write_csv(folder / (prefix + "_copertura.csv"), coverage,
              ("stagione", "famiglia", "tabella", "presente", "righe"))
    write_csv(folder / (prefix + "_colonne.csv"), columns,
              ("stagione", "famiglia", "campo"))
    write_csv(folder / (prefix + "_relazioni.csv"), rel,
              ("stagione", "relazione", "stato", "righe_origine", "riferimenti_mancanti", "distinti_mancanti"))
    write_csv(folder / (prefix + "_codici_duplicati_nella_stagione.csv"), anomalous_codes,
              ("stagione", "codice", "righe_stessa_stagione"))
    summary = {
        "script": "Probe-RecordsNext4Fonti-v1.py",
        "database": str(db),
        "read_only": True,
        "stagioni_raw": len(seasons),
        "famiglie_censite": len(FAMILIES),
        "relazioni_controllate": len(rel),
        "relazioni_da_verificare": [r for r in rel if r["stato"] not in ("OK",)],
        "duplicazioni_codici_nella_stessa_stagione": len(anomalous_codes),
        "limitazioni": [
            "Riferimenti mancanti non sono automaticamente errori: analizzare casi speciali.",
            "Non testa la semantica di Entrato/EntratoRiserva, SV, voti e tabellini.",
            "Non classifica identita storiche, non aggrega gol o assist.",
            "Non rileva codice storico nei campi FCM: verifica nelle strutture sorgente separata.",
        ],
    }
    (folder / (prefix + "_sintesi.json")).write_text(
        json.dumps(summary, ensure_ascii=False, indent=2), encoding="utf-8"
    )
    print("Stagioni:", len(seasons))
    print("Famiglie:", len(FAMILIES))
    print("Relazioni da verificare:", len(summary["relazioni_da_verificare"]))
    print("Codici duplicati nella stessa stagione:", len(anomalous_codes))
    print("File generati nella cartella:", folder)


if __name__ == "__main__":
    main()
~~~~

## `tools/Probe-RecordsNext4Tabellini-v1.py`

SHA256: `3d7deb2283762f5031dceed5e7a88f5ba430226a21fdc8500b024f95f5fd01ee`

~~~~text
#!/usr/bin/env python3
"""Fase 0: audit senza scrittura dei tabellini senza Incontro e dei possibili riposi.
Non determina autonomamente la natura degli incontri cancellati o a riposo.
"""
import argparse
import csv
import json
import re
import sqlite3
from pathlib import Path
from datetime import datetime

def quote(value):
    if not re.fullmatch(r'[a-zA-Z0-9_]+', value):
        raise ValueError('Nome SQL imprevisto: ' + value)
    return '"' + value + '"'

def to_text(value):
    return '' if value is None else str(value)

def main():
    p = argparse.ArgumentParser(description='Audit diagnostico tabellini e possibili riposi FCM (sola lettura)')
    p.add_argument('--database', type=Path, required=True)
    p.add_argument('--output-dir', type=Path, required=True)
    a = p.parse_args()
    if not a.database.is_file():
        p.error('Database non trovato: ' + str(a.database))
    c = sqlite3.connect(a.database.resolve().as_uri() + '?mode=ro', uri=True)
    c.execute('PRAGMA query_only=ON')
    c.execute('PRAGMA busy_timeout=15000')
    names = {r[0] for r in c.execute("SELECT name FROM sqlite_master WHERE type='table'")}
    seasons = sorted({m.group(1) for n in names if (m := re.fullmatch(r'raw_(\d{4}_\d{4})_fcm_incontro', n))})
    detail = []
    summary = []
    try:
        for s in seasons:
            i = 'raw_' + s + '_fcm_incontro'
            t = 'raw_' + s + '_fcm_tabellino'
            f = 'raw_' + s + '_fcm_formazione'
            sq = 'raw_' + s + '_fcm_fantasquadra'
            if t not in names:
                summary.append({'stagione': s, 'tabellini_senza_incontro': '', 'id_incontro_senza_riscontro': '', 'incontri_con_lato_zero': '', 'incontri_tipo2_con_lato_zero': '', 'note': 'Tabellino non presente'})
                continue
            # Un ID mancante in Incontro non puo essere dichiarato riposo dal solo Tabellino.
            missing = c.execute(
                'SELECT t.IDINCONTRO, t.IDSQUADRA, COUNT(*) AS n '
                'FROM ' + quote(t) + ' t LEFT JOIN ' + quote(i) + ' i ON t.IDINCONTRO=i.ID '
                'WHERE i.ID IS NULL GROUP BY t.IDINCONTRO, t.IDSQUADRA '
                'ORDER BY t.IDINCONTRO, t.IDSQUADRA'
            ).fetchall()
            for match_id, team_id, n in missing:
                formation_count = ''
                if f in names:
                    formation_count = c.execute('SELECT COUNT(*) FROM ' + quote(f) + ' WHERE IDINCONTRO=? AND IDSQUADRA=?', (match_id,team_id)).fetchone()[0]
                team_name = ''
                if sq in names:
                    team_row = c.execute('SELECT NOME FROM ' + quote(sq) + ' WHERE ID=? LIMIT 1',(team_id,)).fetchone()
                    team_name = to_text(team_row[0]) if team_row else ''
                detail.append({'stagione':s, 'id_incontro': match_id, 'id_squadra':team_id,
                               'squadra': team_name, 'righe_tabellino': n,
                               'righe_formazione_stesso_incontro_squadra':formation_count,
                               'classificazione':'ID_INCONTRO_ASSENTE_DA_VERIFICARE',
                               'nota':'Senza record Incontro non si puo provare un turno di riposo.'})
            rest_count, type2_count = c.execute(
                'SELECT COUNT(*), COALESCE(SUM(CASE WHEN IDTIPO=2 THEN 1 ELSE 0 END),0) '
                'FROM ' + quote(i) + ' WHERE IDCASA=0 OR IDFUORI=0'
            ).fetchone()
            summary.append({'stagione':s, 'tabellini_senza_incontro':sum(r[2] for r in missing),
                            'id_incontro_senza_riscontro':len({r[0] for r in missing}),
                            'incontri_con_lato_zero':rest_count,
                            'incontri_tipo2_con_lato_zero':type2_count,
                            'note':'Il lato zero e candidato per analisi riposi, NON prova generale.'})
    finally:
        c.close()
    a.output_dir.mkdir(parents=True, exist_ok=True)
    stamp = datetime.now().strftime('%Y%m%d_%H%M%S')
    prefix = 'RecordsNext4_Tabellini_v1_' + stamp
    for suffix,rows,fields in [
       ('riepilogo',summary,['stagione','tabellini_senza_incontro','id_incontro_senza_riscontro','incontri_con_lato_zero','incontri_tipo2_con_lato_zero','note']),
       ('dettaglio',detail,['stagione','id_incontro','id_squadra','squadra','righe_tabellino','righe_formazione_stesso_incontro_squadra','classificazione','nota'])
    ]:
        with (a.output_dir / (prefix+'_'+suffix+'.csv')).open('w',encoding='utf-8-sig',newline='') as h:
            w=csv.DictWriter(h,fieldnames=fields,delimiter=';')
            w.writeheader()
            w.writerows(rows)
    extra={'database':str(a.database),'stagioni_con_incontro':len(seasons),
      'stagioni_con_id_mancanti':sum(1 for x in summary if x['id_incontro_senza_riscontro'] not in ('',0)),
      'righe_tabellino_senza_incontro':sum(x['tabellini_senza_incontro'] for x in summary if isinstance(x['tabellini_senza_incontro'],int)),
      'dettaglio_righe':len(detail), 'stato':'SOLO_DIAGNOSTICA',
      'regole':['Un incontro con lato zero puo essere un riposo, ma richiede conferma FCM.',
                'Un tabellino il cui Incontro non esiste non viene attribuito automaticamente a riposo o corruzione.',
                'Nessuna riga e cancellata o attribuita a una competizione senza prova.']}
    (a.output_dir/(prefix+'_sintesi.json')).write_text(json.dumps(extra,ensure_ascii=False,indent=2),encoding='utf-8')
    print('Stagioni:',len(seasons),'| stagioni con riferimenti tabellino da verificare:',extra['stagioni_con_id_mancanti'])
    print('Righe di tabellino senza Incontro:',extra['righe_tabellino_senza_incontro'])
    print('Report:',a.output_dir)
if __name__=='__main__': main()
~~~~

## `tools/Invoke-RecordsNext4-Fase0-v3.ps1`

SHA256: `5d8c7042ce28355ca996af787f3aa82100a0906fa1992820ba96d2f4660a8b7a`

~~~~text
[CmdletBinding()]
param(
    [string]$RepoDir = 'D:\DEV_APPS\RecordsNext2.0',
    [string]$WorkDir = 'D:\DEV_APPS\RecordsNext4.0',
    [string]$OutputDir = 'D:\DEV_APPS\downloads',
    [string]$Database = 'D:\DEV_APPS\RecordsNext2.0\data\database\recordsnext-sandbox.db'
)
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Git-Text {
    param([string]$Directory, [string[]]$GitArguments)
    $result = & git -C $Directory @GitArguments
    $code = $LASTEXITCODE
    if ($code -ne 0) {
        throw ('Git ha restituito codice ' + $code + ': git -C ' + $Directory + ' ' + ($GitArguments -join ' '))
    }
    return (($result -join "`n").Trim())
}
function Run-Python {
    param([string]$Script, [string[]]$ScriptArguments, [string]$LogFile)
    & py -3 $Script @ScriptArguments | Tee-Object -FilePath $LogFile
    $code = $LASTEXITCODE
    if ($code -ne 0) { throw ('Python ha restituito codice ' + $code + ' in ' + $Script + ' (log: ' + $LogFile + ')') }
}

if (-not (Test-Path -LiteralPath $OutputDir -PathType Container)) {
    New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null
}
$stamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$runDir = Join-Path $OutputDir ('RecordsNext4_Fase0_OUTPUT_v3_' + $stamp)
$zipOut = Join-Path $OutputDir ('RecordsNext4_Fase0_OUTPUT_v3_' + $stamp + '.zip')
if (Test-Path -LiteralPath $runDir) { throw ('Cartella output esistente: ' + $runDir) }
New-Item -ItemType Directory -Path $runDir | Out-Null
$ok = $false
try {
    $kitRoot = Split-Path -Parent $PSScriptRoot
    if (-not (Get-Command git -ErrorAction SilentlyContinue)) { throw 'Git non presente nel PATH.' }
    if (-not (Get-Command py -ErrorAction SilentlyContinue)) { throw 'Python Launcher py non presente nel PATH.' }
    & py -3 --version
    if ($LASTEXITCODE -ne 0) { throw 'Python 3 non disponibile.' }
    if (-not (Test-Path -LiteralPath (Join-Path $RepoDir 'pom.xml') -PathType Leaf)) {
        throw ('Repository operativo 3.2 non trovato: ' + $RepoDir)
    }
    if (-not (Test-Path -LiteralPath $Database -PathType Leaf)) {
        throw ('Database sandbox non trovato: ' + $Database)
    }
    $originalRoot = Git-Text -Directory $RepoDir -GitArguments @('rev-parse', '--show-toplevel')
    if ([System.IO.Path]::GetFullPath($originalRoot).TrimEnd('\') -ine [System.IO.Path]::GetFullPath($RepoDir).TrimEnd('\')) {
        throw ('Repository base inatteso: ' + $originalRoot)
    }
    $originalHead = Git-Text -Directory $RepoDir -GitArguments @('rev-parse', 'HEAD')
    Write-Host ('Repository 3.2 HEAD: ' + $originalHead)

    # IMPORTANTE: se il worktree esiste, prima lo riconosciamo. Non dipendiamo dal tag.
    if (Test-Path -LiteralPath $WorkDir -PathType Container) {
        if (-not (Test-Path -LiteralPath (Join-Path $WorkDir 'pom.xml') -PathType Leaf)) {
            throw ('Directory 4.0 esistente ma senza pom.xml: ' + $WorkDir)
        }
        $workRoot = Git-Text -Directory $WorkDir -GitArguments @('rev-parse', '--show-toplevel')
        if ([System.IO.Path]::GetFullPath($workRoot).TrimEnd('\') -ine [System.IO.Path]::GetFullPath($WorkDir).TrimEnd('\')) {
            throw ('Directory 4.0 diversa dalla radice Git: ' + $workRoot)
        }
        $branch = Git-Text -Directory $WorkDir -GitArguments @('branch', '--show-current')
        if ($branch -ne 'feature/recordsnext4-players') {
            throw ('Il worktree 4.0 esistente usa un branch inatteso: ' + $branch)
        }
        Write-Host 'Worktree 4.0 esistente riconosciuto. Non lo ricreo.' -ForegroundColor Green
    }
    else {
        # Solo se manca il worktree: verifichiamo il tag PRIMA di creare il branch.
        & git -C $RepoDir show-ref --verify --quiet 'refs/tags/v3.2.0'
        $tagResult = $LASTEXITCODE
        if ($tagResult -ne 0) {
            throw ('Impossibile creare worktree: refs/tags/v3.2.0 non verificabile (Git exit ' + $tagResult + ').')
        }
        Write-Host 'Creo il worktree 4.0 a partire dal tag v3.2.0...' -ForegroundColor Cyan
        & git -C $RepoDir worktree add -b 'feature/recordsnext4-players' $WorkDir 'v3.2.0'
        $addResult = $LASTEXITCODE
        if ($addResult -ne 0) { throw ('Creazione worktree fallita, Git exit ' + $addResult) }
    }

    $workHead = Git-Text -Directory $WorkDir -GitArguments @('rev-parse', 'HEAD')
    & git -C $WorkDir merge-base --is-ancestor 'f9bf30323d9204d70e91e242bddfc72c2060ecf9' $workHead
    $ancestorExit = $LASTEXITCODE
    if ($ancestorExit -ne 0) {
        throw ('Il worktree 4.0 non discende dal commit base f9bf303 (Git exit ' + $ancestorExit + ').')
    }
    Write-Host ('Worktree HEAD: ' + $workHead)
    $files = @(
        'docs\REVISIONE_BASE_RECORDSNEXT_3_2_v1.md',
        'docs\PIANO_RECORDSNEXT4_GIOCATORI_v3.md',
        'docs\REGISTRO_DECISIONI_RECORDSNEXT4_v2.md',
        'docs\DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v2.md',
        'tools\Audit-RecordsNext4Giocatori-v1.py',
        'tools\Probe-RecordsNext4Fonti-v1.py',
        'tools\Probe-RecordsNext4Tabellini-v1.py',
        'tools\Create-RecordsNext4WorkingCodeMd-v2.ps1',
        'tools\Invoke-RecordsNext4-Fase0-v3.ps1'
    )
    # Controllo preliminare: nessuna copia parziale se esiste un file differente.
    foreach ($f in $files) {
        $src = Join-Path $kitRoot $f
        $dst = Join-Path $WorkDir $f
        if (-not (Test-Path -LiteralPath $src -PathType Leaf)) { throw ('File kit mancante: ' + $src) }
        if (Test-Path -LiteralPath $dst -PathType Leaf) {
            if ((Get-FileHash -LiteralPath $src -Algorithm SHA256).Hash -ne (Get-FileHash -LiteralPath $dst -Algorithm SHA256).Hash) {
                throw ('File gia presente ma diverso. Nessuna sovrascrittura: ' + $dst)
            }
        }
    }
    foreach ($f in $files) {
        $src = Join-Path $kitRoot $f
        $dst = Join-Path $WorkDir $f
        if (-not (Test-Path -LiteralPath $dst -PathType Leaf)) {
            $parent = Split-Path -Parent $dst
            if (-not (Test-Path -LiteralPath $parent -PathType Container)) {
                New-Item -ItemType Directory -Path $parent -Force | Out-Null
            }
            Copy-Item -LiteralPath $src -Destination $dst
            Write-Host ('Installato: ' + $f)
        } else { Write-Host ('Gia presente identico: ' + $f) }
    }
    & (Join-Path $WorkDir 'tools\Create-RecordsNext4WorkingCodeMd-v2.ps1') -ProjectDir $WorkDir

    Run-Python -Script (Join-Path $WorkDir 'tools\Audit-RecordsNext4Giocatori-v1.py') -ScriptArguments @('--database',$Database,'--code','100843','--output-dir',$runDir) -LogFile (Join-Path $runDir 'LOG_giocatore_100843.txt')
    Run-Python -Script (Join-Path $WorkDir 'tools\Audit-RecordsNext4Giocatori-v1.py') -ScriptArguments @('--database',$Database,'--code','12302','--output-dir',$runDir) -LogFile (Join-Path $runDir 'LOG_giocatore_12302.txt')
    Run-Python -Script (Join-Path $WorkDir 'tools\Probe-RecordsNext4Fonti-v1.py') -ScriptArguments @('--database',$Database,'--output-dir',$runDir) -LogFile (Join-Path $runDir 'LOG_fonti.txt')
    Run-Python -Script (Join-Path $WorkDir 'tools\Probe-RecordsNext4Tabellini-v1.py') -ScriptArguments @('--database',$Database,'--output-dir',$runDir) -LogFile (Join-Path $runDir 'LOG_tabellini.txt')

    $manifest = @(
        'RECORDSNEXT4 FASE 0 - v3',
        ('Data: ' + (Get-Date -Format 'yyyy-MM-dd HH:mm:ss')),
        ('Repo 3.2: ' + $RepoDir), ('Repo HEAD: ' + $originalHead),
        ('Worktree 4.0: ' + $WorkDir), ('Worktree HEAD: ' + $workHead),
        ('Database: ' + $Database),
        ('SHA256 database: ' + (Get-FileHash -LiteralPath $Database -Algorithm SHA256).Hash),
        'SQLITE letto con mode=ro e query_only=ON. Nessun sito pubblicato.'
    )
    $manifest | Out-File -LiteralPath (Join-Path $runDir 'MANIFEST_RUN_v3.txt') -Encoding UTF8
    & git -C $RepoDir status -sb | Out-File -LiteralPath (Join-Path $runDir 'GIT_32_STATO.txt') -Encoding UTF8
    & git -C $WorkDir status -sb | Out-File -LiteralPath (Join-Path $runDir 'GIT_40_STATO.txt') -Encoding UTF8
    $docs = Join-Path $runDir 'documentazione'
    New-Item -ItemType Directory -Path $docs | Out-Null
    foreach ($f in @('CODICE_FUNZIONANTE_RECORDSNEXT4_v2.md','REVISIONE_BASE_RECORDSNEXT_3_2_v1.md','PIANO_RECORDSNEXT4_GIOCATORI_v3.md','REGISTRO_DECISIONI_RECORDSNEXT4_v2.md','DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v2.md')) {
        Copy-Item -LiteralPath (Join-Path (Join-Path $WorkDir 'docs') $f) -Destination $docs
    }
    $ok = $true
}
catch {
    ($_ | Out-String) | Out-File -LiteralPath (Join-Path $runDir 'ERRORE_FASE0_v3.txt') -Encoding UTF8
    Write-Host ('Fase 0 interrotta: ' + $_.Exception.Message) -ForegroundColor Red
    throw
}
finally {
    if (Test-Path -LiteralPath $runDir -PathType Container) {
        try {
            Compress-Archive -Path (Join-Path $runDir '*') -DestinationPath $zipOut -CompressionLevel Optimal
            Write-Host ('ZIP risultati/diagnostica da caricare: ' + $zipOut) -ForegroundColor Cyan
        }
        catch { Write-Warning ('ZIP diagnostico non creato: ' + $_.Exception.Message) }
    }
}
if ($ok) {
    Write-Host 'Fase 0 v3 eseguita. Non eseguire git add/commit o pubblicazioni.' -ForegroundColor Green
}
~~~~

## `tools/Create-RecordsNext4WorkingCodeMd-v2.ps1`

SHA256: `2d41d02881a7387fee7f4364810197a1e653f8fb59a3642ad9a58d9453859279`

~~~~text
[CmdletBinding()]
param(
    [string]$ProjectDir = 'D:\DEV_APPS\RecordsNext4.0'
)
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

if (-not (Test-Path -LiteralPath (Join-Path $ProjectDir 'pom.xml') -PathType Leaf)) {
    throw ('Progetto RecordsNext non trovato: ' + $ProjectDir)
}
$docDir = Join-Path $ProjectDir 'docs'
$destination = Join-Path $docDir 'CODICE_FUNZIONANTE_RECORDSNEXT4_v2.md'
$files = @(
    'docs\REVISIONE_BASE_RECORDSNEXT_3_2_v1.md',
    'docs\PIANO_RECORDSNEXT4_GIOCATORI_v3.md',
    'docs\REGISTRO_DECISIONI_RECORDSNEXT4_v2.md',
    'docs\DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v2.md',
    'tools\Audit-RecordsNext4Giocatori-v1.py',
    'tools\Probe-RecordsNext4Fonti-v1.py',
    'tools\Probe-RecordsNext4Tabellini-v1.py',
    'tools\Create-RecordsNext4WorkingCodeMd-v2.ps1',
    'tools\Invoke-RecordsNext4-Fase0-v3.ps1'
)
foreach ($f in $files) {
    if (-not (Test-Path -LiteralPath (Join-Path $ProjectDir $f) -PathType Leaf)) {
        throw ('File necessario alla bibbia non trovato: ' + $f)
    }
}
$commitLines = & git -C $ProjectDir rev-parse HEAD
$exitCode = $LASTEXITCODE
if ($exitCode -ne 0) { throw 'Impossibile leggere HEAD Git.' }
$branchLines = & git -C $ProjectDir branch --show-current
$exitCode = $LASTEXITCODE
if ($exitCode -ne 0) { throw 'Impossibile leggere il branch Git.' }
$commit = ($commitLines -join '').Trim()
$branch = ($branchLines -join '').Trim()
$b = New-Object System.Text.StringBuilder
[void]$b.AppendLine('# RecordsNext 4.0 - Bibbia incrementale v2 (Fase 0)')
[void]$b.AppendLine('')
[void]$b.AppendLine('> Documentazione e codice diagnostico, NON una release 4.0 funzionante.')
[void]$b.AppendLine('> Base 3.2 immutata: docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md.')
[void]$b.AppendLine('> I dati della fase 0 sono nei report di D:\DEV_APPS\downloads, non certificati da questo file.')
[void]$b.AppendLine('')
[void]$b.AppendLine(('Generato: ' + (Get-Date -Format 'yyyy-MM-dd HH:mm:ss')))
[void]$b.AppendLine(('Branch: ' + $branch))
[void]$b.AppendLine(('Commit: ' + $commit))
[void]$b.AppendLine('')
[void]$b.AppendLine('## Stato verificabile')
[void]$b.AppendLine('')
[void]$b.AppendLine('- Base sorgente v3.2.0; nessuna variazione a codice Java, schema SQL, GUI o JS pubblici.')
[void]$b.AppendLine('- Verifiche diagnostiche previste: identificativi, famiglie raw, riferimenti tabellini e candidati riposo.')
[void]$b.AppendLine('- I tabellini senza Incontro non vengono dichiarati corrotti o a riposo in assenza di prova.')
[void]$b.AppendLine('- Stato locale dei test: consultare log e report ZIP generati dal runner v3.')
[void]$b.AppendLine('')
foreach ($f in $files) {
    $p = Join-Path $ProjectDir $f
    $hash = (Get-FileHash -LiteralPath $p -Algorithm SHA256).Hash
    [void]$b.AppendLine(('## ' + ($f -replace '\', '/')))
    [void]$b.AppendLine('')
    [void]$b.AppendLine(('SHA256: ' + $hash))
    [void]$b.AppendLine('')
    [void]$b.AppendLine('~~~~text')
    foreach ($line in @(Get-Content -LiteralPath $p -Encoding UTF8)) {
        [void]$b.AppendLine($line)
    }
    [void]$b.AppendLine('~~~~')
    [void]$b.AppendLine('')
}
$enc = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllText($destination, $b.ToString(), $enc)
Write-Host ('Bibbia incrementale 4.0 v2 generata: ' + $destination)
~~~~

## `tools/Create-RecordsNext4WorkingCodeMd-v3.py`

SHA256: `1d83e6b6adf1610529f2445553aa65e72d6ab04ec7286b3ddb19a96bfd0a92dc`

~~~~text
#!/usr/bin/env python3
"""Genera la bibbia incrementale RecordsNext 4, Fase 0 (v3).

Non esegue il precedente generatore PowerShell v2, che contiene una regex invalida.
Non modifica codice o database. Non sovrascrive una bibbia gia generata diversa.
"""
import argparse
import hashlib
import os
from pathlib import Path
import subprocess
import sys

BASE_COMMIT = 'f9bf30323d9204d70e91e242bddfc72c2060ecf9'
FILES = [
    'docs/REVISIONE_BASE_RECORDSNEXT_3_2_v1.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v3.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v2.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v2.md',
    'docs/CORREZIONE_FASE0_RECORDSNEXT4_v1.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v4.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v3.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v3.md',
    'tools/Audit-RecordsNext4Giocatori-v1.py',
    'tools/Probe-RecordsNext4Fonti-v1.py',
    'tools/Probe-RecordsNext4Tabellini-v1.py',
    'tools/Invoke-RecordsNext4-Fase0-v3.ps1',
    'tools/Create-RecordsNext4WorkingCodeMd-v2.ps1',
    'tools/Create-RecordsNext4WorkingCodeMd-v3.py',
    'tools/Invoke-RecordsNext4-Fase0-v4.py',
]
OUTPUT = 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v3.md'


def git(project: Path, *args: str) -> str:
    proc = subprocess.run(['git', '-C', str(project), *args], text=True,
                          encoding='utf-8', errors='replace', capture_output=True)
    if proc.returncode:
        raise RuntimeError('Git failed: ' + ' '.join(args) + ': ' + proc.stderr.strip())
    return proc.stdout.strip()


def make(project: Path) -> Path:
    missing = [name for name in FILES if not (project / name).is_file()]
    if missing:
        raise RuntimeError('Missing inputs for documentation: ' + ', '.join(missing))
    base_doc = project / 'docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md'
    if not base_doc.is_file():
        raise RuntimeError('Bibbia storica 3.2 mancante (non la modifichiamo).')
    commit = git(project, 'rev-parse', 'HEAD')
    branch = git(project, 'branch', '--show-current')
    lines = [
        '# RecordsNext 4.0 - Bibbia incrementale v3 (Fase 0)', '',
        '> DOCUMENTAZIONE DIAGNOSTICA. Non equivale alla release 4.0.',
        '> Base immutata: docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md.',
        '> Il vecchio generatore PowerShell v2 e conservato solo come storico:',
        '> falliva su -replace con regex backslash non valida.', '',
        f'Commit di partenza 3.2: `{BASE_COMMIT}`',
        f'Worktree branch: `{branch}`', f'Worktree HEAD: `{commit}`',
        'Output diagnostici: D:/DEV_APPS/downloads/RecordsNext4_Fase0_OUTPUT_v4_*.zip',
        '', '## Stato', '',
        '- Nessuna modifica a Java, SQLite, GUI e JS di produzione.',
        '- Runner fase 0 v4 scritto in Python per evitare fragilita della sintassi PS 5.1.',
        '- Le sond(e) FCM/FCA sono audit; non certificano di per se le identita all-time.',
        '- I tabellini con Incontro mancante restano non classificati fino alla verifica.',
        '- Gli esiti Windows vanno letti nel manifest e nei log dello ZIP, non dedotti da questa bibbia.',
        '',
        '## Base documentale storica', '',
        f'- `docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md` SHA256: `{hashlib.sha256(base_doc.read_bytes()).hexdigest()}`',
        '',
    ]
    for name in FILES:
        source = project / name
        payload = source.read_bytes()
        digest = hashlib.sha256(payload).hexdigest()
        text = payload.decode('utf-8-sig')
        lines.extend([f'## `{name}`', '', f'SHA256: `{digest}`', '', '~~~~text',
                      text.rstrip('\n'), '~~~~', ''])
    result = ('\n'.join(lines).rstrip() + '\n').encode('utf-8')
    target = project / OUTPUT
    if target.exists():
        if target.read_bytes() != result:
            raise RuntimeError('Bibbia v3 esistente diversa, non sovrascritta: ' + str(target))
        print('Bibbia v3 gia presente identica: ' + str(target), flush=True)
        return target
    try:
        with target.open('xb') as handle:
            handle.write(result)
    except FileExistsError:
        raise RuntimeError('Bibbia v3 comparsa durante la generazione: ' + str(target))
    print('Bibbia v3 generata: ' + str(target), flush=True)
    return target


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument('--project-dir', type=Path, required=True)
    args = parser.parse_args()
    try:
        make(args.project_dir.resolve())
    except Exception as error:
        print('ERRORE GENERATORE: ' + str(error), file=sys.stderr)
        sys.exit(1)


if __name__ == '__main__':
    main()
~~~~

## `tools/Invoke-RecordsNext4-Fase0-v4.py`

SHA256: `44ce93622331c9b0d2c08eb52c7bceaaff48b9454651eaefbef1f2512a1d82e3`

~~~~text
#!/usr/bin/env python3
"""Ripresa conservativa Fase 0 RecordsNext 4.0 dopo errore bibbia v2.

Esecuzione offline, Python standard library, nessuna operazione sul DB in scrittura.
Produce ZIP diagnostico anche in caso di errore.
"""
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

BASE_COMMIT = 'f9bf30323d9204d70e91e242bddfc72c2060ecf9'
PATCH_FILES = [
    'docs/CORREZIONE_FASE0_RECORDSNEXT4_v1.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v4.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v3.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v3.md',
    'tools/Create-RecordsNext4WorkingCodeMd-v3.py',
    'tools/Invoke-RecordsNext4-Fase0-v4.py',
]
REQUIRED_PREVIOUS = [
    'docs/REVISIONE_BASE_RECORDSNEXT_3_2_v1.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v3.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v2.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v2.md',
    'tools/Audit-RecordsNext4Giocatori-v1.py',
    'tools/Probe-RecordsNext4Fonti-v1.py',
    'tools/Probe-RecordsNext4Tabellini-v1.py',
    'tools/Invoke-RecordsNext4-Fase0-v3.ps1',
    'tools/Create-RecordsNext4WorkingCodeMd-v2.ps1',
]


def run_checked(args, cwd=None):
    p = subprocess.run(args, cwd=cwd, text=True, encoding='utf-8',
                       errors='replace', capture_output=True)
    if p.returncode:
        raise RuntimeError('Comando fallito (%d): %s\n%s' %
                           (p.returncode, ' '.join(map(str, args)), p.stderr[-4000:]))
    return p.stdout.strip()


def sha256(path):
    digest = hashlib.sha256()
    with path.open('rb') as handle:
        for block in iter(lambda: handle.read(4 * 1024 * 1024), b''):
            digest.update(block)
    return digest.hexdigest()


def same_path(a, b):
    return os.path.normcase(str(a.resolve())) == os.path.normcase(str(b.resolve()))


def write_text(path, text):
    path.write_text(text, encoding='utf-8')


def check_project(project):
    if not (project / 'pom.xml').is_file():
        raise RuntimeError('Worktree 4.0 senza pom.xml: ' + str(project))
    root = Path(run_checked(['git', '-C', str(project), 'rev-parse', '--show-toplevel']))
    if not same_path(root, project):
        raise RuntimeError('Radice worktree inattesa: ' + str(root))
    branch = run_checked(['git', '-C', str(project), 'branch', '--show-current'])
    if branch != 'feature/recordsnext4-players':
        raise RuntimeError('Branch inatteso: ' + branch)
    head = run_checked(['git', '-C', str(project), 'rev-parse', 'HEAD'])
    run_checked(['git', '-C', str(project), 'merge-base', '--is-ancestor', BASE_COMMIT, head])
    for name in REQUIRED_PREVIOUS:
        if not (project / name).is_file():
            raise RuntimeError('File v3 non trovato nel worktree: ' + name)
    return head


def install(patch_root, project):
    for name in PATCH_FILES:
        src, dst = patch_root / name, project / name
        if not src.is_file():
            raise RuntimeError('File del kit v4 mancante: ' + name)
        if dst.exists() and sha256(src) != sha256(dst):
            raise RuntimeError('File v4 diverso gia presente, nessuna sovrascrittura: ' + name)
    for name in PATCH_FILES:
        src, dst = patch_root / name, project / name
        if dst.exists():
            print('Gia presente identico:', name, flush=True)
            continue
        dst.parent.mkdir(parents=True, exist_ok=True)
        with dst.open('xb') as handle:
            handle.write(src.read_bytes())
        print('Installato:', name, flush=True)


def execute_audit(script, options, log):
    print('Verifica:', script.name, flush=True)
    p = subprocess.run([sys.executable, str(script), *options],
                       text=True, encoding='utf-8', errors='replace',
                       stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    write_text(log, p.stdout)
    print(p.stdout.strip()[-1300:], flush=True)
    if p.returncode:
        raise RuntimeError('Audit fallito (%d): %s; leggere %s' %
                           (p.returncode, script.name, log.name))


def main():
    parser = argparse.ArgumentParser(description='Riprende Fase 0 v4 (senza rieseguire il runner PS v3)')
    parser.add_argument('--work-dir', type=Path, default=Path('D:/DEV_APPS/RecordsNext4.0'))
    parser.add_argument('--database', type=Path, default=Path('D:/DEV_APPS/RecordsNext2.0/data/database/recordsnext-sandbox.db'))
    parser.add_argument('--output-dir', type=Path, default=Path('D:/DEV_APPS/downloads'))
    args = parser.parse_args()
    root = Path(__file__).resolve().parent.parent
    project = args.work_dir.resolve()
    db = args.database.resolve()
    out = args.output_dir.resolve()
    out.mkdir(parents=True, exist_ok=True)
    stamp = datetime.now().strftime('%Y%m%d_%H%M%S_%f')
    folder = out / ('RecordsNext4_Fase0_OUTPUT_v4_' + stamp)
    folder.mkdir()
    zip_path = out / (folder.name + '.zip')
    ok, head = False, ''
    try:
        if same_path(root, project):
            print('Kit v4 eseguito nel worktree: installazione identica.', flush=True)
        if not db.is_file():
            raise RuntimeError('Database sandbox mancante: ' + str(db))
        head = check_project(project)
        print('Worktree esistente, ramo corretto, base 3.2 verificata:', head, flush=True)
        install(root, project)
        # Nessuna chiamata al generatore PowerShell v2 difettoso.
        execute_audit(project / 'tools/Create-RecordsNext4WorkingCodeMd-v3.py',
                      ['--project-dir', str(project)], folder / 'LOG_BIBBIA_v3.txt')
        for code in ('100843', '12302'):
            execute_audit(project / 'tools/Audit-RecordsNext4Giocatori-v1.py',
                          ['--database', str(db), '--code', code, '--output-dir', str(folder)],
                          folder / ('LOG_GIOCATORE_' + code + '.txt'))
        execute_audit(project / 'tools/Probe-RecordsNext4Fonti-v1.py',
                      ['--database', str(db), '--output-dir', str(folder)], folder / 'LOG_FONTI.txt')
        execute_audit(project / 'tools/Probe-RecordsNext4Tabellini-v1.py',
                      ['--database', str(db), '--output-dir', str(folder)], folder / 'LOG_TABELLINI.txt')
        doc_dir = folder / 'documentazione'
        doc_dir.mkdir()
        for name in ('CODICE_FUNZIONANTE_RECORDSNEXT4_v3.md',
                     'PIANO_RECORDSNEXT4_GIOCATORI_v4.md',
                     'REGISTRO_DECISIONI_RECORDSNEXT4_v3.md',
                     'DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v3.md',
                     'CORREZIONE_FASE0_RECORDSNEXT4_v1.md'):
            shutil.copy2(project / 'docs' / name, doc_dir / name)
        write_text(folder / 'GIT_WORKTREE_v4.txt',
                   run_checked(['git', '-C', str(project), 'status', '--short', '--branch']) + '\n')
        manifest = {
            'fase': '0', 'kit': 'v4', 'stato': 'AUDIT_ESEGUITI', 'worktree': str(project),
            'worktree_head': head, 'db_sola_lettura': str(db),
            'db_sha256': sha256(db), 'nessun_aggiornamento_git': True,
            'nessuna_modifica_db': True,
            'nota': 'Tabellini non collegati: classificazione aperta; attenzione ai possibili riposi.',
        }
        write_text(folder / 'MANIFEST_v4.json', json.dumps(manifest, ensure_ascii=False, indent=2))
        ok = True
    except Exception:
        error = traceback.format_exc()
        write_text(folder / 'ERRORE_v4.txt', error)
        print('ERRORE FASE 0 v4:\n' + error[-2500:], file=sys.stderr, flush=True)
    finally:
        with zipfile.ZipFile(zip_path, 'x', zipfile.ZIP_DEFLATED, compresslevel=6) as archive:
            for p in sorted(folder.rglob('*')):
                if p.is_file():
                    archive.write(p, p.relative_to(folder).as_posix())
        print('ZIP DA CARICARE:', zip_path, flush=True)
        print('ESITO:', 'OK' if ok else 'ERRORE', flush=True)
    return 0 if ok else 1


if __name__ == '__main__':
    sys.exit(main())
~~~~
~~~~

## `docs/VERIFICA_FASE0_RECORDSNEXT4_v1.md`

SHA256: `5820e2cafd1e2660de8792024c61da9f9f1a22ce2b3fda1cb0ee3f3deba07e85`

~~~~text
# RecordsNext 4.0 - Verifica Fase 0 v1

Data: 2026-10-31. Stato: **audit diagnostici completati; verifiche di dominio ancora aperte**.

## Provenienza e riproducibilita

- Ambiente utente: `D:\DEV_APPS\RecordsNext4.0`, branch `feature/recordsnext4-players`, HEAD `f9bf30323d9204d70e91e242bddfc72c2060ecf9`.
- Output originale ricevuto e controllato: `RecordsNext4_Fase0_OUTPUT_v4_20261031_212435_728403.zip`; 24 file, archivio ZIP integro, manifest `AUDIT_ESEGUITI`, nessun `ERRORE_v4.txt`.
- Database: copia esatta `recordsnext-sandbox.db` ricevuta nell'archivio `RecordsNext2.0.zip`, SHA-256 `57923f4e75986c2b81533e4beef15b9133635258d7de1d36827a1bdbe576671b` (coincide con il manifest dell'output utente).
- Nessuna modifica al database, agli originali FCM/FCA, al codice Java o ai JS pubblicati.

## Risultati verificati

| Verifica | Risultato | Nota |
| --- | ---: | --- |
| Stagioni raw FCM/FCA | 21 | archivio SQLite importato |
| Famiglie di tabelle esaminate | 12 | incluse anagrafiche, voti, formazioni, tabellini e incontri reali |
| Rilevazioni di presenza tabella | 252/252 presenti | **correzione v2 del probe** |
| Relazioni stagionali verificate | 105 | 90 OK, 15 da verificare |
| Righe `Tabellino` con ID incontro assente | 168 | in 15 stagioni, causa non ancora determinata |
| Codici Gazzetta ripetuti nella stessa stagione | 0 | non esclude riutilizzi attraverso stagioni |
| Codice giocatore 100843 (Lautaro Martinez) | 9 stagioni | audit anagrafico senza anomalie rilevate |
| Codice 12302 | 3 stagioni | audit richiede revisione manuale: Nicola Ascoli / Roberto Crivello |

## Correzione necessaria al probe v1

In `tools/Probe-RecordsNext4Fonti-v1.py` la costante `FAMILIES` conteneva la forma errata `fca_incontroina`. Il nome delle tabelle raw nel database e `fca_incontraina` (es. `raw_2025_2026_fca_incontraina`). Per questo il report v1 segnalava falsamente 21 tabelle mancanti.

La **v2** modifica soltanto l'identificativo della famiglia e versiona il prefisso dei report. E stata validata con `python -m py_compile` ed eseguita sulla copia esatta del database sandbox. Risultato: 252/252 tabelle presenti; gli altri 105 esiti referenziali restano invariati.

Non riutilizzare il report di copertura v1 per affermare che la tabella delle partite reali e assente. Conservare il v1 soltanto come documentazione storica del difetto.

## Distinzioni confermate e questioni aperte

- `GiocatoreA.ID` e locale alla stagione; il codice 100843 e quello persistente usato per la ricerca storica, ma la posizione esatta del codice nelle tabelle FCM sorgenti resta da identificare con prova diretta.
- `GiocaIn` non equivale automaticamente a presenze reali a voto; `Formazione` non equivale automaticamente a prestazioni conteggiate.
- `Incontro` con squadra a zero e `Tabellino.IDIncontro` senza corrispondenza sono fenomeni distinti. Non classificare i 168 casi come riposi o errori senza controllo sugli originali.
- Gli stati `Entrato`, `EntratoRiserva`, `Pos`, `Rig`, SV, RU e i criteri per punti non sfruttati richiedono verifica con regole e casi storici.

## Stato operativo

La parte **tecnica diagnostica** della Fase 0 e stata eseguita; non risulta alcuna modifica produttiva o release 4.0.0. Le questioni di dominio vanno affrontate nei test dell'estrattore e prima di calcolare i record.

**Nessun comando da eseguire ora sul PC:** la correzione v2 e stata verificata sul database gia fornito. Il prossimo kit di sviluppo dovra integrare il probe v2 e questa documentazione in `D:\DEV_APPS\RecordsNext4.0` con nuovi nomi versionati e test di non regressione.
~~~~

## `docs/PIANO_RECORDSNEXT4_GIOCATORI_v5.md`

SHA256: `504981a538347e13d65f02e7ded00cf5f60a7b6a0a237101f25385c4715f7c2d`

~~~~text
# RecordsNext 4.0.0 — Piano giocatori v4

Data: 2026-10-31. Stato: **FASE 0, AUDIT; NESSUNA RELEASE 4.0.0 ANCORA REALIZZATA**.

## Obiettivo

Per ciascun calciatore estrarre TUTTI i dati reali disponibili per stagione e giornata FCA, TUTTI i dati fantasy per stagione, fantasquadra, competizione e partita FCM, quindi produrre statistiche e record reali e fantasy per stagione, squadra, competizione e all time.

## Base tecnica vincolante

Il progetto di riferimento e **RecordsNext v3.2.0** del 2026-09-16, commit `f9bf303`, con documentazione effettiva e sorgenti presenti in `RecordsNext2.0.zip`. Il registro delle decisioni e la revisione `REVISIONE_BASE_RECORDSNEXT_3_2_v1.md` fanno parte della continuita operativa. Il worktree `D:\DEV_APPS\RecordsNext4.0` puo gia esistere e non deve essere ricreato automaticamente se valido.

La 3.2 ha 9 output, architettura multistagione e multisito, JS stagionali flat direttamente nella `js` del relativo sito e cutoff di ogni sito alla sua stagione. Mantenerli invariati finche il nuovo modulo non e stato implementato e testato.

## Identificativi

- `GiocatoreA.ID`: ID **locale FCA stagionale**.
- `Formazione.IDGioc`, `Tesserato.IDGiocatore`, `GiocaIn.IDGiocatore`: collegano l'ID locale alla stagione corrispondente.
- `GiocatoreA.CodiceGazza` negli FCA analizzati: codice storico usato nelle foto; l'utente riferisce il codice persistente nel sistema FCM, ma la sua ubicazione esatta nel file FCM va accertata. Esempio 100843 = Lautaro.
- Gli accoppiamenti pluristagionali richiedono trattamento di riutilizzi/anomalie nei codici; non si deve cambiare il significato dei campi esistenti.

## Entita logiche DA PROGETTARE (non implementate)

1. Identita canonica del calciatore / eventuale alias storicizzato.
2. Anagrafica stagionale: stagione + ID FCA locale e tutti i dati sorgente.
3. Prestazione reale: stagione + ID giocatore locale + giornata di Serie A; voti/eventi/contestualizzazione, con SV e dati mancanti separati da zero.
4. Partecipazione fantasy: stagione + partita FCM + fantasquadra + ID locale + occorrenza; ruolo e stato titolare/riserva/utilizzato, dettaglio tabellino e contributo ufficiale.
5. Evidenze di tesseramento: nessuna data trasferimento dedotta da righe non datate.
6. Record: dimensione reale o fantasy, ambito, filtro, soglie, denominatore, parita e provenienza fino al dato elementare.

Non contare una prestazione reale piu volte solo perche il fantacalciatore ha disputato piu competizioni legate alla stessa giornata.

## Incontri speciali e tabellini

`Matches` ESCLUDE i turni di riposo secondo architettura 3.2. Incontro con un lato squadra a zero e tabellino il cui `IDINCONTRO` manca da `Incontro` vanno censiti SEPARATAMENTE: non sono sinonimi, non eliminare nessuna riga e non costruire collegamenti fittizi.

## Roadmap e criteri di uscita

| Fase | Lavoro | Verifica | Stato |
|---|---|---|---|
| 0 | Worktree isolato; audit codici, schema, referenze, riposi/tabellini | Report eseguiti e riprodotti; eccezioni di dominio aperte | Audit tecnico eseguito |
| 1 | Dati reali per stagione/giornata, anche non tesserati | Confronto FCA `GiocaIn`, `Punteggio`, `Statistiche`, `Storico`; nessun doppio conteggio | Da iniziare |
| 2 | Dati fantasy per gara/competizione/squadra | Accordo con FCM `Formazione` e `Tabellino`; gestione riserve, RU, match speciali | Da iniziare |
| 3 | Catalogo record reali, fantasy e punti non sfruttati | Definizioni statistico-regolamentari e casi limite verificati | Da iniziare |
| 4 | Output JS e HTML/GUI | Cutoff multisito, shard flat, nessuna regressione 3.2 | Da iniziare |
| 5 | Regressioni, bibbia, FULL/UPDATE, release 4.0.0 | Test, commit e tag solo dopo approvazione | Da iniziare |

## Aggiornamento Fase 0 - recupero v4

- Il runner PowerShell v3 e stato eseguito ma si e fermato nel generatore bibbia PowerShell v2; il worktree e gli strumenti diagnostici risultano installati.
- Il comando di ripresa usa `tools/Invoke-RecordsNext4-Fase0-v4.py`, non modifica i file della release 3.2.
- Il generatore nuovo e `tools/Create-RecordsNext4WorkingCodeMd-v3.py`; la bibbia aggiornata e `docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v3.md`.
- Il risultato corretto va attestato solo dopo analisi dello ZIP v4.
- I riferimenti di tabellino senza `Incontro` non sono automaticamente errori: verificare esplicitamente le squadre a riposo.

## Documentazione vivente

- Base storica 3.2: `docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md` (NON toccare).
- Nuova bibbia incrementale: `docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v3.md`, generata da `tools/Create-RecordsNext4WorkingCodeMd-v3.py` (v2 storico difettoso).
- Piano: questo file v4.
- Decisioni: `docs/REGISTRO_DECISIONI_RECORDSNEXT4_v3.md`.
- Diario: `docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v3.md`.
- Verifica base: `docs/REVISIONE_BASE_RECORDSNEXT_3_2_v1.md`.

Non dichiarare "funzionante" cio che e solamente progettato. Ogni successiva revisione sostituisce i nomi con nuove versioni; tenere traccia del codice completo e dei test nel generatore.

## Aggiornamento Fase 0 - verifica reale del 31 ottobre 2026

- Ricevuto output v4 Windows e controllato ZIP integro; manifest `AUDIT_ESEGUITI`, nessun log di errore, worktree sul ramo e commit previsto.
- Corretto errore di inventario nella famiglia FCA `incontroina` (sbagliata) in `incontraina` (nome raw esistente) con `Probe-RecordsNext4Fonti-v2.py`.
- Rieseguito il probe sulla stessa copia binaria del database sandbox (SHA-256 nel documento `VERIFICA_FASE0_RECORDSNEXT4_v1.md`): 21 stagioni, 12 famiglie, **252/252 tabelle presenti**, 105 relazioni (90 OK e 15 da approfondire).
- Nessuna nuova esecuzione necessaria sul PC per certificare questi conteggi; includere nel successivo kit la correzione versionata e i test di regressione.
- Fase 1: concentrarsi sull'estrazione di tutte le prestazioni FCA reali, con SV e mancanti separati da zero e senza raddoppio dovuto alle competizioni fantasy.
~~~~

## `docs/REGISTRO_DECISIONI_RECORDSNEXT4_v4.md`

SHA256: `2891d418a2fabf0742687a0843c4b23aaf50089745720d39095ad0bc1dfa4653`

~~~~text
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
~~~~

## `docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v4.md`

SHA256: `f95f72d6a5b9de7771267bde489ad7dbbce1d24982c07063611bea2e212e75aa`

~~~~text
# RecordsNext 4 — Diario implementazione v3

## 2026-10-31 — Revisione dell'archivio 3.2.0 e correzione Fase 0

- Ricevuto e analizzato `RecordsNext2.0.zip` contenente codice, script, documentazione, dati e metadati Git della release 3.2.0.
- Confermato commit `f9bf303...`, tag annotato `v3.2.0` e registrazione di `D:\DEV_APPS\RecordsNext4.0` come worktree sul ramo `feature/recordsnext4-players` nei metadati del repository. Non equivale a prova che ogni file sia presente attualmente sul PC: verifica nel runner.
- L'errore del runner Fase 0 v2 era nel preflight di verifica del tag: non procedere a ricreare il worktree esistente. Causa nativa esatta non disponibile dall'output; il runner v3 evita la verifica del tag quando il worktree e gia valido.
- Letta la struttura del generatore originale `tools/Create-RecordsNext2WorkingCodeMd.ps1`. La nuova bibbia v2 elenca file e SHA256 delle modifiche 4.0, lasciando inalterata la bibbia 3.2.
- Creato `tools/Probe-RecordsNext4Tabellini-v1.py` per distinguere statisticamente record `Incontro` con un lato squadra nullo/zero dai tabellini privi di riferimento `Incontro`.
- Verifica Python sul database caricato precedentemente: 21 stagioni, 15 con ID tabellini da verificare, 168 righe `Tabellino` senza `Incontro`. Il dato NON dimostra errore ne riposo.
- Nessuna modifica a Java, GUI, pubblicazione, archivi originali o database.

## Verifica necessaria sul PC Windows dell'utente

Avviare `tools/Invoke-RecordsNext4-Fase0-v3.ps1` dal kit v3 ed esaminare lo ZIP in `D:\DEV_APPS\downloads`. Non eseguire la versione v2 difettosa. Lo script PowerShell e stato revisionato staticamente per PS 5.1; **non e stato collaudato su una macchina Windows in questa sessione**. Gli script Python sono stati compilati e il nuovo audit tabellini e stato eseguito sul database di copia.

## Prossimi interventi

Dopo i report: verificare gli ID FCM/FCA originali e il codice foto, stabilire regole per riposi e tabellini residuali e progettare l'estrattore reale della fase 1. Nessun commit o release automatici.

## 2026-10-31 - Correzione v4 dopo fallimento PowerShell v3

- Ricevuto output dell'utente: Python 3.13.7; worktree riconosciuto, base f9bf303 confermata.
- Errore nella bibbia v2: `($f -replace '\', '/')` con regex backslash singolo non valida.
- Conservato lo script v2 a fini di continuita; nessuna sovrascrittura.
- Introdotti generatore bibbia v3 Python e runner diagnostico v4 Python; la validazione sul PC deve essere confermata dal relativo ZIP.
- Preservata l'ipotesi riposi per alcuni tabellini non associati a incontri; non ancora classificati.
- Nessun cambiamento al codice produttivo Java, SQLite, GUI, configurazioni e JS dei siti.
- Prossimo passaggio: esame output v4 e aggiornamento stato Fase 0, poi progetto estrattore reale Fase 1.

## 2026-10-31 - Controllo output v4 e rettifica inventario

- Ricevuto e verificato lo ZIP `RecordsNext4_Fase0_OUTPUT_v4_20261031_212435_728403.zip`, completo e integro (24 file).
- Worktree `feature/recordsnext4-players`, HEAD `f9bf303...`, nessuna modifica a file tracciati nel report Git; i nuovi documenti e tool risultano non tracciati, nessun commit.
- I report originali v4 hanno segnalato 21 tabelle inesistenti perche il probe v1 cercava `fca_incontroina` invece del reale `fca_incontraina`. Nessun danno al database.
- Preparato `tools/Probe-RecordsNext4Fonti-v2.py`; compilazione Python superata; esecuzione sul database sandbox **con SHA-256 identico a quello del report Windows**.
- Risultato corretto: 21 stagioni, 12 famiglie, 252/252 tabelle presenti, 105 relazioni (90 OK, 15 da approfondire); 168 righe `Tabellino` restano da verificare.
- Documentato l'esito in `docs/VERIFICA_FASE0_RECORDSNEXT4_v1.md`. Il nuovo codice sara installato nella cartella di sviluppo da un kit successivo, senza sovrascrivere versioni precedenti.
- Prossimo step: progettazione ed estrazione dati reali per giocatore, con test sui valori originali e sui casi speciali; nessun record all-time ancora calcolato.
~~~~

## `tools/Probe-RecordsNext4Fonti-v2.py`

SHA256: `ba9cb9073ba0538a536369a8564c5a80242b3a0ca53791b0ca62b1db9b5e5407`

~~~~text
#!/usr/bin/env python3
"""RecordsNext4 fase 0: inventario/raw e integrita referenziale. SOLO LETTURA.

Non interpreta voti, non deduce identita all time, non modifica database e non
pubblica output; richiede Python 3 standard library.
"""
import argparse
import csv
import json
import re
import sqlite3
from datetime import datetime
from pathlib import Path

FAMILIES = (
    "fca_giocatorea", "fca_giocain", "fca_punteggio", "fca_statistiche",
    "fca_storico", "fca_squadradia", "fca_incontraina",
    "fcm_formazione", "fcm_tesserato", "fcm_tabellino",
    "fcm_incontro", "fcm_fantasquadra",
)
LINKS = (
    ("fcm_formazione", "IDGIOC", "fca_giocatorea", "ID", "formazione_giocatore"),
    ("fcm_tesserato", "IDGIOCATORE", "fca_giocatorea", "ID", "tesserato_giocatore"),
    ("fca_giocain", "IDGIOCATORE", "fca_giocatorea", "ID", "giocain_giocatore"),
    ("fcm_formazione", "IDINCONTRO", "fcm_incontro", "ID", "formazione_incontro"),
    ("fcm_tabellino", "IDINCONTRO", "fcm_incontro", "ID", "tabellino_incontro"),
)


def quoted(name):
    if not re.fullmatch(r"[A-Za-z0-9_]+", name):
        raise ValueError("Nome tabella imprevisto: " + name)
    return '"' + name + '"'


def norm(value):
    if value is None:
        return ""
    s = str(value).strip()
    if re.fullmatch(r"[+-]?\d+\.0+", s):
        s = s.split(".")[0]
    return s


def unique_values(conn, table, field):
    return {norm(v) for (v,) in conn.execute(
        "SELECT " + quoted(field) + " FROM " + quoted(table)
    ) if v is not None}


def write_csv(path, records, columns):
    with path.open("w", encoding="utf-8-sig", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=columns, delimiter=";")
        writer.writeheader()
        writer.writerows(records)


def main():
    ap = argparse.ArgumentParser(description="Inventario raw e relazioni FCM/FCA, sola lettura")
    ap.add_argument("--database", type=Path, required=True)
    ap.add_argument("--output-dir", type=Path, required=True)
    args = ap.parse_args()
    db = args.database.resolve()
    if not db.is_file():
        ap.error("Database non trovato: " + str(db))
    conn = sqlite3.connect(db.as_uri() + "?mode=ro", uri=True)
    conn.execute("PRAGMA query_only=ON")
    try:
        names = {name for (name,) in conn.execute(
            "SELECT name FROM sqlite_master WHERE type='table'"
        )}
        seasons = sorted({m.group(1) for name in names
                          if (m := re.fullmatch(r"raw_(\d{4}_\d{4})_fca_giocatorea", name))})
        # Non assumere che ogni stagione contenga necessariamente FCA.
        seasons = sorted(set(seasons) | {m.group(1) for name in names
                         if (m := re.fullmatch(r"raw_(\d{4}_\d{4})_fcm_formazione", name))})
        coverage, columns, rel, anomalous_codes = [], [], [], []
        for season in seasons:
            info = {}
            for family in FAMILIES:
                table = "raw_" + season + "_" + family
                if table not in names:
                    coverage.append({"stagione": season, "famiglia": family,
                                     "tabella": table, "presente": 0, "righe": ""})
                    continue
                count = conn.execute("SELECT COUNT(*) FROM " + quoted(table)).fetchone()[0]
                coverage.append({"stagione": season, "famiglia": family,
                                 "tabella": table, "presente": 1, "righe": count})
                cols = [row[1] for row in conn.execute("PRAGMA table_info(" + quoted(table) + ")")]
                info[family] = (table, set(cols), count)
                for col in cols:
                    columns.append({"stagione": season, "famiglia": family, "campo": col})
            for src, srcfield, dst, dstfield, label in LINKS:
                if src not in info or dst not in info:
                    rel.append({"stagione": season, "relazione": label, "stato": "TABELLA_ASSENTE",
                                "righe_origine": "", "riferimenti_mancanti": "", "distinti_mancanti": ""})
                    continue
                source, sourcecols, cnt = info[src]
                target, targetcols, _ = info[dst]
                if srcfield not in sourcecols or dstfield not in targetcols:
                    rel.append({"stagione": season, "relazione": label, "stato": "COLONNA_ASSENTE",
                                "righe_origine": cnt, "riferimenti_mancanti": "", "distinti_mancanti": ""})
                    continue
                # Conteggi esatti, lettura dei soli identificativi, senza creare indici o tabelle.
                valid = unique_values(conn, target, dstfield)
                missing = 0
                distinct = set()
                for (raw,) in conn.execute("SELECT " + quoted(srcfield) + " FROM " + quoted(source)):
                    key = norm(raw)
                    if key not in valid:
                        missing += 1
                        if len(distinct) < 10000:
                            distinct.add(key)
                rel.append({"stagione": season, "relazione": label, "stato": "OK" if missing == 0 else "DA_VERIFICARE",
                            "righe_origine": cnt, "riferimenti_mancanti": missing,
                            "distinti_mancanti": len(distinct) if len(distinct) < 10000 else ">=10000"})
            if "fca_giocatorea" in info:
                t, c, _ = info["fca_giocatorea"]
                if {"CODICEGAZZA", "NOME", "ID"}.issubset(c):
                    for code, n in conn.execute(
                        "SELECT CODICEGAZZA, COUNT(*) FROM " + quoted(t) +
                        " WHERE CODICEGAZZA IS NOT NULL AND CAST(CODICEGAZZA AS TEXT) <> '0' "
                        " GROUP BY CODICEGAZZA HAVING COUNT(*) > 1"
                    ):
                        anomalous_codes.append({"stagione": season, "codice": norm(code),
                                                "righe_stessa_stagione": n})
    finally:
        conn.close()
    folder = args.output_dir
    folder.mkdir(parents=True, exist_ok=True)
    stamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    prefix = "RecordsNext4_Fonti_v2_" + stamp
    write_csv(folder / (prefix + "_copertura.csv"), coverage,
              ("stagione", "famiglia", "tabella", "presente", "righe"))
    write_csv(folder / (prefix + "_colonne.csv"), columns,
              ("stagione", "famiglia", "campo"))
    write_csv(folder / (prefix + "_relazioni.csv"), rel,
              ("stagione", "relazione", "stato", "righe_origine", "riferimenti_mancanti", "distinti_mancanti"))
    write_csv(folder / (prefix + "_codici_duplicati_nella_stagione.csv"), anomalous_codes,
              ("stagione", "codice", "righe_stessa_stagione"))
    summary = {
        "script": "Probe-RecordsNext4Fonti-v2.py",
        "database": str(db),
        "read_only": True,
        "stagioni_raw": len(seasons),
        "famiglie_censite": len(FAMILIES),
        "relazioni_controllate": len(rel),
        "relazioni_da_verificare": [r for r in rel if r["stato"] not in ("OK",)],
        "duplicazioni_codici_nella_stessa_stagione": len(anomalous_codes),
        "limitazioni": [
            "Riferimenti mancanti non sono automaticamente errori: analizzare casi speciali.",
            "Non testa la semantica di Entrato/EntratoRiserva, SV, voti e tabellini.",
            "Non classifica identita storiche, non aggrega gol o assist.",
            "Non rileva codice storico nei campi FCM: verifica nelle strutture sorgente separata.",
        ],
    }
    (folder / (prefix + "_sintesi.json")).write_text(
        json.dumps(summary, ensure_ascii=False, indent=2), encoding="utf-8"
    )
    print("Stagioni:", len(seasons))
    print("Famiglie:", len(FAMILIES))
    print("Relazioni da verificare:", len(summary["relazioni_da_verificare"]))
    print("Codici duplicati nella stessa stagione:", len(anomalous_codes))
    print("File generati nella cartella:", folder)


if __name__ == "__main__":
    main()
~~~~

## `tools/Create-RecordsNext4WorkingCodeMd-v4.py`

SHA256: `c8434dee6277cad69973de3b32c7de9a3249ba9aefea284a9526b9eea0a0a542`

~~~~text
#!/usr/bin/env python3
"""Genera la bibbia incrementale RecordsNext 4, Fase 0 (v4).

Non esegue il precedente generatore PowerShell v2, che contiene una regex invalida.
Non modifica codice o database. Non sovrascrive una bibbia gia generata diversa.
"""
import argparse
import hashlib
import os
from pathlib import Path
import subprocess
import sys

BASE_COMMIT = 'f9bf30323d9204d70e91e242bddfc72c2060ecf9'
FILES = [
    'docs/REVISIONE_BASE_RECORDSNEXT_3_2_v1.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v3.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v2.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v2.md',
    'docs/CORREZIONE_FASE0_RECORDSNEXT4_v1.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v4.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v3.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v3.md',
    'tools/Audit-RecordsNext4Giocatori-v1.py',
    'tools/Probe-RecordsNext4Fonti-v1.py',
    'tools/Probe-RecordsNext4Tabellini-v1.py',
    'tools/Invoke-RecordsNext4-Fase0-v3.ps1',
    'tools/Create-RecordsNext4WorkingCodeMd-v2.ps1',
    'tools/Create-RecordsNext4WorkingCodeMd-v3.py',
    'tools/Invoke-RecordsNext4-Fase0-v4.py',
    'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v3.md',
    'docs/VERIFICA_FASE0_RECORDSNEXT4_v1.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v5.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v4.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v4.md',
    'tools/Probe-RecordsNext4Fonti-v2.py',
    'tools/Create-RecordsNext4WorkingCodeMd-v4.py',
]
OUTPUT = 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v4.md'


def git(project: Path, *args: str) -> str:
    proc = subprocess.run(['git', '-C', str(project), *args], text=True,
                          encoding='utf-8', errors='replace', capture_output=True)
    if proc.returncode:
        raise RuntimeError('Git failed: ' + ' '.join(args) + ': ' + proc.stderr.strip())
    return proc.stdout.strip()


def make(project: Path) -> Path:
    missing = [name for name in FILES if not (project / name).is_file()]
    if missing:
        raise RuntimeError('Missing inputs for documentation: ' + ', '.join(missing))
    base_doc = project / 'docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md'
    if not base_doc.is_file():
        raise RuntimeError('Bibbia storica 3.2 mancante (non la modifichiamo).')
    commit = git(project, 'rev-parse', 'HEAD')
    branch = git(project, 'branch', '--show-current')
    lines = [
        '# RecordsNext 4.0 - Bibbia incrementale v4 (Fase 0)', '',
        '> DOCUMENTAZIONE DIAGNOSTICA. Non equivale alla release 4.0.',
        '> Base immutata: docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md.',
        '> Il vecchio generatore PowerShell v2 e conservato solo come storico:',
        '> falliva su -replace con regex backslash non valida.', '',
        f'Commit di partenza 3.2: `{BASE_COMMIT}`',
        f'Worktree branch: `{branch}`', f'Worktree HEAD: `{commit}`',
        'Output diagnostici: D:/DEV_APPS/downloads/RecordsNext4_Fase0_OUTPUT_v4_*.zip',
        '', '## Stato', '',
        '- Nessuna modifica a Java, SQLite, GUI e JS di produzione.',
        '- Runner fase 0 v4 scritto in Python per evitare fragilita della sintassi PS 5.1.',
        '- Audit FCM/FCA: 21 stagioni, 12 famiglie, 252/252 tabelle presenti (probe v2 corretto).',
        '- Relazioni: 105 controllate, 90 OK, 15 da verificare (168 righe Tabellino).',
        '- Gli audit non certificano di per se le identita all-time.',
        '- I tabellini con Incontro mancante restano non classificati fino alla verifica.',
        '- Gli esiti Windows vanno letti nel manifest e nei log dello ZIP, non dedotti da questa bibbia.',
        '',
        '## Base documentale storica', '',
        f'- `docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md` SHA256: `{hashlib.sha256(base_doc.read_bytes()).hexdigest()}`',
        '',
    ]
    for name in FILES:
        source = project / name
        payload = source.read_bytes()
        digest = hashlib.sha256(payload).hexdigest()
        text = payload.decode('utf-8-sig')
        lines.extend([f'## `{name}`', '', f'SHA256: `{digest}`', '', '~~~~text',
                      text.rstrip('\n'), '~~~~', ''])
    result = ('\n'.join(lines).rstrip() + '\n').encode('utf-8')
    target = project / OUTPUT
    if target.exists():
        if target.read_bytes() != result:
            raise RuntimeError('Bibbia v4 esistente diversa, non sovrascritta: ' + str(target))
        print('Bibbia v4 gia presente identica: ' + str(target), flush=True)
        return target
    try:
        with target.open('xb') as handle:
            handle.write(result)
    except FileExistsError:
        raise RuntimeError('Bibbia v4 comparsa durante la generazione: ' + str(target))
    print('Bibbia v4 generata: ' + str(target), flush=True)
    return target


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument('--project-dir', type=Path, required=True)
    args = parser.parse_args()
    try:
        make(args.project_dir.resolve())
    except Exception as error:
        print('ERRORE GENERATORE: ' + str(error), file=sys.stderr)
        sys.exit(1)


if __name__ == '__main__':
    main()
~~~~
