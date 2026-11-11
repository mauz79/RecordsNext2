# RecordsNext 4.0 - Bibbia incrementale v14

Fase integrazione architetturale Records Top & Flop: nessun Java, database o output sito modificato.

## Provenienza

- Branch: `feature/recordsnext4-players`
- HEAD: `f9bf30323d9204d70e91e242bddfc72c2060ecf9`
- Bibbia v13 SHA-256: `be5ba147234201d54326d569a80c9e6c20effc067c6ce4d98ae2500513eaafc1`
- Questo documento e additivo e non sostituisce v13.

## File completi e SHA-256

### `docs/PIANO_RECORDSNEXT4_GIOCATORI_v15.md`

SHA-256: `67999a01c88ce5cfd3b39d23aa26bb5cb26784703af1b233bf1dd91ee9c34b47`

~~~~markdown
# RecordsNext 4.0 - Piano operativo v15

# RecordsNext 4.0 - Piano operativo v14

# RecordsNext 4.0 - Continuita documentale PIANO_RECORDSNEXT4_GIOCATORI_v13

Versione additiva del 2026-11-01. La cronologia integrale precedente e preservata sotto.

# RecordsNext 4.0 - Piano operativo v11

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


## Aggiornamento dopo verifica Windows 3B2 e decisione sul record individuale

- 3B2 completata anche su Windows (`ESITO: OK`, 8/8 test): audit documentale delle regole. Non sono state apportate modifiche al database o al motore Java.
- Priorita della fase successiva: **voti non utilizzati dal singolo calciatore**. Niente simulazione del punteggio di fantasquadra e niente ottimizzazione delle sostituzioni.
- Dati minimi da raccogliere per un calciatore: codice storico, ID locale FCA, stagione, fantasquadra, partita e competizione fantasy, giornata reale, indicatore di effettivo utilizzo nei primi 11 del tabellino, presenza di voto reale valido, valore sorgente del voto, riferimento al tabellino.
- Metrica stabilita: somma dei soli voti reali validi nelle esclusioni / **tutte le esclusioni**; conservare separati i due conteggi, non dividerli fra loro.
- Implementazione successiva: un campione reale verificato a mano, poi test automatici sul campione, quindi calcolo storico. Registrare i casi ambigui anziche correggerli arbitrariamente.
- Il campo "valore massimo" menzionato dall'utente rimane **aperto** (nessun cap numerico assunto).
- Continuano a valere l'identita per `CodiceGazza` con verifica collisioni, l'ID FCA locale, l'assenza di duplicazioni delle prestazioni reali e la conservazione dei record precedenti.


## Nuovo assetto approvato per il record individuale (2026-11-01)

- Conservare i quattro indicatori con due numeratori distinti (voto reale valido, fantavoto regolamentare completo) e due denominatori distinti (esclusioni totali, esclusioni con voto valido).
- La Fase 3C si articola in: (a) campioni di esclusioni FCM/FCA con provenienza voto/fantavoto e bonus-malus; (b) audit di tesseramento e partecipazione effettiva per stabilire `E` e `P`; (c) calcolo su casi reali e confronto con tabellino, senza simulare cambi di formazione.
- La stessa prestazione FCA resta un solo fatto reale; un'esclusione e invece un fatto fantasy legato a stagione, fantasquadra, competizione e incontro. Non aggregare ciecamente incontri di competizioni parallele.
- Implementare prima unit test sintetici per i quattro indicatori (v2); i test sintetici NON dimostrano ancora la classificazione delle esclusioni negli archivi.
- Se `E > 0, P = 0`, gli indici pesati valgono zero; le medie sulle gare valide sono non definite. Se `E = 0`, tutte le metriche sono non definite.
- Le RU non sono calciatori e non devono generare un voto individuale sprecato; le regole di sostituzione per competizione restano variabili e indipendenti da questo calcolo.
- Nessun limite artificiale al voto o fantavoto; la voce 'massimo' resta proposta separata e non adottata come soglia.


## Nuova fase 3C0 - Modello archivio completo e audit contesti paralleli

- Definita specifica `SPECIFICA_ARCHIVIO_COMPLETO_GIOCATORE_v1.md` con livello reale, tesseramento e livello evento fantasy, valido anche per titolari.
- Documentate due famiglie, competizioni singole e sovrapposizioni nella stessa giornata; conservazione dei dati originali prima dei filtri.
- Audit `Audit-RecordsNext4Contesti-v1.py` READ-ONLY: report per competizione, copertura, giocatore-squadra-competizione, sovrapposizioni e caso 100843.
- **Prossimo sviluppo**: storicizzare il tesseramento e definire le assenze da Formazione in modo dimostrabile; quindi costruire il dataset sorgente completo e l'estrazione simmetrica voti/fantavoti per utilizzati ed esclusi.
- La verifica Windows del nuovo runner rimane pendente; non toccare Java/GUI/sito.

## Aggiornamento v14 - fase 3C1

- Validazione Windows della 3C0: ZIP `RecordsNext4_Contesti_OUTPUT_v1_20261101_004840_254541.zip`, stato OK, 10 test, 21 stagioni, 552025 righe, SHA256 sandbox uguale prima/dopo, bibbia v12 presente. Non ancora dataset canonico.
- Classificare Play Off - Play Out e Spareggio Serie B CAMPIONATO; escludere Youth League da ogni output record/scheda.
- Audit v2: verificare nuovamente le 21 stagioni, separare originali/ammessi/esclusi, non eliminare dati sorgente.
- Prossima implementazione: associare a ogni evento fantasy la partita FCM e la `scorecardUrl` canonica della stagione corretta, verificata sui file normalizzati 3.2. URL non verificati restano null.
- Solo successivamente generare il dataset completo dei giocatori in Java/SQLite e i record con prova del singolo episodio.


## Aggiornamento v15 - Records Top & Flop come famiglia nativa

**Decisione vincolante dell'utente (2026-11-01):** tutti gli audit dei giocatori delle fasi 0-3C1 sono lavori preparatori di una nuova famiglia nativa **Records Top & Flop**, integrata in RecordsNext 4.0. La pipeline 3.2.0 resta la base esclusiva da estendere. Non costruire sistemi di export, pubblicazione, configurazione, manifest o visualizzazione paralleli.

**Stato attuale:** 3C1 eseguita su Windows, ZIP `RecordsNext4_Contesti_OUTPUT_v2_20261101_005413_578565.zip` verificato: stato OK; 13 test; 21 stagioni; 552025 righe origine, 546775 ammesse, 5250 Youth League escluse; SHA256 database sandbox prima=dopo; bibbia v13 generata. Non e ancora implementata una famiglia Java Top & Flop.

**Ordine vincolato di implementazione**, rispettando la 3.2:
1. Definire fatti canonici: giocatore storico + prestazione FCA unica e contesti per fantasquadra, incontro, competizione; provenienza del voto e dei bonus/malus; link canonico tabellino. Produrre test sui casi 100843, competizioni parallele, Youth League e anomalie.
2. Implementare una sola volta in Java/SQLite i fatti riutilizzabili da scheda giocatore e records; mantenere gli audit Python come verificatori, non produttori operativi permanenti.
3. Inserire `TOP_FLOP` nel catalogo (`RecordFamily`, `CoreRecordCatalog`, dipendenze, selezione e configurazione), senza modificare valori preesistenti e mantenendo la lettura delle configurazioni 3.2.
4. Collegare il calcolo alla `RecordsNextPipeline` con logiche FULL/CONSOLIDATED e archivio storico separato per stagione; eseguire regressione su output invariati delle altre famiglie.
5. Collegare `TopFlopFamilyJsExporter` a `Records2026SitePublisher`, relativa validazione, manifest, `SeasonFamilyShardPublisher`, publisher multisito e link tabellini canonici. Nessun nuovo flusso di deploy.
6. GUI: aggiungere selezione famiglia e singoli records in `RecordsNext2Dashboard` / configurazione vigente. HTML sito: nuova sezione e schede individuali derivate dagli stessi dati senza duplicazione dei calcoli.
7. Eseguire test integrati: nessun impatto su famiglie preesistenti, non oltre 1.5 MiB per shard, siti storici corretti, FULL e CONSOLIDATED, JS valido, nessuna Youth League. Aggiornare bibbia e rilasciare come 4.0.0 soltanto dopo approvazione.

La classificazione CAMPIONATO/COPPE e distinta dalla classificazione **famiglie di record**: `TOP_FLOP` e una famiglia records; CAMPIONATO/COPPE sono filtri del dominio sportivo. Non confonderle nel codice.

**Prossimo passo di implementazione:** realizzazione nel Java 4.0 del modello dati canonico individuale e test di regressione, senza ancora selezionare/pubblicare la famiglia finche il suo calcolo non esiste.
~~~~

### `docs/REGISTRO_DECISIONI_RECORDSNEXT4_v14.md`

SHA-256: `47170ad35b752879123683745dee8f916cad00785672352428a52340b4f3a93e`

~~~~markdown
# RecordsNext 4.0 - Registro decisioni v14

# RecordsNext 4.0 - Registro decisioni v13

# RecordsNext 4.0 - Continuita documentale REGISTRO_DECISIONI_RECORDSNEXT4_v12

Versione additiva del 2026-11-01. La cronologia integrale precedente e preservata sotto.

# RecordsNext 4.0 - Registro decisioni v10

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


## Integrazione successiva a 3B2: metrica individuale (2026-11-01)

| ID | Stato | Decisione / motivazione |
|---|---|---|
| D-062 | CONFERMATA: verifica Windows 3B2 | ZIP `RecordsNext4_Fase3B2_OUTPUT_v1_20261101_002713_328984.zip`, stato OK; 8 test superati, 21 stagioni, 180 configurazioni, 13.072 incontri e 22.265 tabellini. SHA-256 del sandbox prima e dopo identico: `57923f4e75986c2b81533e4beef15b9133635258d7de1d36827a1bdbe576671b`. Bibbia v9 generata; nessun commit/deploy dichiarato. |
| D-063 | APPROVATA DALL'UTENTE: obiettivo | Il voto sprecato e un indicatore riferito a **uno specifico calciatore** lasciato fuori da una fantasquadra, non il vantaggio teorico che la fantasquadra avrebbe potuto ottenere sostituendo altri giocatori. Nessuna simulazione delle sostituzioni necessaria per questa metrica. |
| D-064 | APPROVATA DALL'UTENTE: formula | `mediaVotoSprecato = somma(voti validi del calciatore nelle occasioni in cui e rimasto fuori) / numero totale di partite in cui e stato lasciato fuori`. Le esclusioni senza prestazione utile contano nel denominatore e zero nel numeratore. Esempio: 23 esclusioni, 3 voti pari a 6: `18/23 = 0,782608...`. Non usare come media primaria `18/3 = 6`. |
| D-065 | CONFERMATA: RU e fonti 3.2 | La Riserva d'Ufficio e un voto regolamentare assegnato per un posto scoperto quando le sostituzioni ordinarie non bastano. Non e un calciatore ne un voto del calciatore lasciato fuori; non entra nella somma dei suoi voti sprecati. Preservare il sistema RU e gli strumenti personali esistenti. Fonti: `docs/ANALISI_PERSONALE_RU.md`, `docs/MODELLO_DATI_RECORDSNEXT2.md` e `tools/Report_RU_Storico_Completo_v8_4.ps1` (glossario). |
| D-066 | APPROVATA DALL'UTENTE: sostituzioni | Non esiste una regola generale di tre sostituzioni: si osservano anche 5 e, per recuperi a distanza di mesi e configurazioni eccezionali, anche 11. Mantenere il valore FCM della competizione senza normalizzarlo. Per la media individuale non simulare sostituzioni alternative. |
| D-067 | DA VERIFICARE PRIMA DEL CALCOLO | Stabilire con esempi reali il significato operativo di "lasciato fuori" (non nei primi 11 del tabellino finale, riserva non utilizzata, non schierato), il tipo di voto da sommare (voto Gazzetta base vs fantavoto totale), il trattamento di SV e voti speciali e il perimetro delle sole partite valide. Non inventare questi criteri. |
| D-068 | DA VERIFICARE PRIMA DEI RANKING | Evitare conteggi erronei di una stessa prestazione reale se ci sono piu competizioni fantasy nella stessa giornata. Conservare il dettaglio delle partite fantasy ed esplicitare la chiave di aggregazione del record personale. |
| D-069 | APERTA | L'utente ha menzionato un "valore massimo" senza definirne numericamente l'eventuale soglia o se si riferisca al massimo voto sprecato. Non fissare cap o limiti arbitrari. |
| D-070 | PROCEDURA | Comandi pronti per copia-incolla PowerShell 5.1, senza blocchi `else` separabili; file sempre versionati; ogni fase aggiorna piano, registro, diario, codice verificato e bibbia con SHA-256. |

### Nota di compatibilita

Le D-060 e D-061 riguardano anche un **possibile** studio dei punti recuperabili della fantasquadra. Per il nuovo record individuale quell'analisi non costituisce un prerequisito: questa decisione ne restringe il perimetro, senza cancellare la cronologia.


## Revisione metriche individuali (2026-11-01) - decisioni D-071--D-079

| ID | Stato | Decisione / motivazione |
|---|---|---|
| D-071 | APPROVATA DALL'UTENTE | Il voto di un calciatore non utilizzato e il **valore integrale del voto** (es. 7 resta 7), non il voto meno 6 e non il vantaggio potenziale della fantasquadra. |
| D-072 | APPROVATA DALL'UTENTE | Quattro indicatori distinti: media voto sprecato, errore pesato sul voto, fantamedia sprecata, errore pesato sul fantavoto. La formula v1 `S/E` rimane valida come **errore pesato**, ma non come unica misura principale. |
| D-073 | APPROVATA DALL'UTENTE | Con `E` esclusioni, `P` prestazioni valide, `SV` somma voti e `SF` somma fantavoti: `mediaVoto = SV/P`, `pesatoVoto = SV/E`, `mediaFantavoto = SF/P`, `pesatoFantavoto = SF/E`. Dove il denominatore e zero il risultato e non definito. |
| D-074 | APPROVATA DALL'UTENTE | Una gara in cui il calciatore non ha giocato/non ha voto non produce errore: somma zero al numeratore; se e una esclusione effettiva valida, rimane nel denominatore `E` del peso stagionale. |
| D-075 | APPROVATA DALL'UTENTE | Fantavoto comprensivo di eventi e relativi bonus/malus: gol normali, gol su rigore, assist, ammonizioni e altre categorie disponibili. Conservare separatamente voto, fantavoto ed eventi originali. Valori regolamentari da sorgenti reali, mai coefficienti unificati arbitrariamente. |
| D-076 | DERIVAZIONE MATEMATICA | `quotaConVoto=P/E`, `errorePesatoVoto=mediaVoto*quotaConVoto` e `errorePesatoFantavoto=fantamediaSprecata*quotaConVoto`, quando denominatori disponibili. Se `E>0, P=0`: indici pesati pari a zero, medie non definite. |
| D-077 | PROPOSTA, DA APPROVARE | Aggiungere massimo voto/fantavoto in singola esclusione, numero di esclusioni e quota con voto come indicatori di dettaglio, senza applicare alcun cap artificiale. |
| D-078 | APERTA - FCM/FCA | Definire con casi reali l'esclusione (panchinaro non usato, fuori lista, non schierato ma tesserato), la provenienza del fantavoto per giocatore lasciato fuori e la gestione di `SV`, voti d'ufficio e competizioni parallele. Non calcolare record reali prima della verifica. |
| D-079 | PROCEDURA / IMPLEMENTAZIONE | Aggiunto solo un test sintetico e la specifica v2. Nessuna modifica a database, Java, GUI, regolamenti, competizioni o siti. Bibbia incrementale v11 e SHA-256 dei nuovi file; vecchie versioni conservate. |

**Cronologia invariata:** D-064 `S/E` non e annullata: assume il nome corretto di **errore pesato stagionale** e si affianca alla nuova media `S/P` richiesta dall'utente.


## Decisioni D-080--D-090: archivio completo per giocatore e contesti paralleli

| ID | Stato | Decisione / evidenza |
|---|---|---|
| D-080 | APPROVATA DALL'UTENTE | Il prodotto deve conservare un set completo di dati per OGNI calciatore, con scheda individuale e record derivati dallo stesso archivio. Non estrarre soltanto le statistiche utili a record gia previsti. |
| D-081 | APPROVATA DALL'UTENTE | Un calciatore puo essere tesserato nello stesso anno da 2-3 fantasquadre diverse (serie distinte). Separare rigorosamente le osservazioni fantasy per squadra. |
| D-082 | APPROVATA DALL'UTENTE | Lo stesso calciatore nella stessa giornata puo essere usato in campionato ed escluso in coppa, e viceversa. Ogni incontro fantasy e un contesto indipendente; la prestazione reale e unica. |
| D-083 | APPROVATA DALL'UTENTE | Esattamente due famiglie per il filtro richiesto: CAMPIONATO e COPPE; conservare sempre anche la singola competizione e la sua identita stagionale. |
| D-084 | APPROVATA DALL'UTENTE | Dati individuali completi anche per titolari, giocatori conteggiati e subentrati, non soltanto per gli esclusi: voto, fantavoto, dettaglio bonus/malus, presenze, medie e contesti. |
| D-085 | CONFERMATA: FCM 2025/26 | `Tesserato` mostra il calciatore codice 100843 in tre squadre: 8 (River Pino), 49 (F.C. Pitbull), 77 (F.C. Boliverz). In giornata Serie A 4 River Pino lo ha in Serie B e Coppa Serie B (due incontri diversi). |
| D-086 | INFERENZA DA VERIFICARE | `Tesserato` e uno snapshot senza campi data di validita; non considerare automaticamente escluso il calciatore mancante da `Formazione` in ogni incontro di una squadra. |
| D-087 | PROPOSTA OPERATIVA | Mappa esplicita delle denominazioni FCM alle due famiglie, per stagione e competizione, invece di classificazione runtime con parole chiave. Per ora tre nomi senza assegnazione (Play Off - Play Out, Spareggio Serie B, Youth League), da approvare. |
| D-088 | REGOLA DI INTEGRITA | Chiave prestazione reale diversa dalla chiave evento fantasy; non moltiplicare gol/assist reali nelle aggregazioni all-time quando la stessa prestazione alimenta piu eventi fantasy. |
| D-089 | IMPLEMENTATA: AUDIT PROTOTIPO | Nuovo audit Fase 3C0 di sola lettura sui contesti. Verifica 552.025 righe Formazione, 21 stagioni, mappa delle competizioni, sovrapposizioni e caso reale codice 100843. Non crea ancora il dataset completo canonico. |
| D-090 | APERTA | Regole affidabili per esclusioni senza Formazione, coesistenza di piu partite della stessa famiglia nella giornata, disponibilita storica tesseramento, fantavoto del non schierato e trattamento Play Off/Spareggio/Youth League. |

## Decisioni D-091--D-098: classificazione completa e link ai tabellini

| ID | Stato | Decisione e verifica |
|---|---|---|
| D-091 | APPROVATA DALL'UTENTE | `Play Off - Play Out` appartiene alla famiglia CAMPIONATO. |
| D-092 | APPROVATA DALL'UTENTE | `Spareggio Serie B` appartiene alla famiglia CAMPIONATO. |
| D-093 | APPROVATA DALL'UTENTE | `Youth League` non deve essere considerata MAI nei record/statistiche/schede individuali. Nei soli audit fonti conservare l'evidenza della esclusione. |
| D-094 | APPROVATA DALL'UTENTE | I record individuali di singola partita devono contenere stagione, competizione, fantasquadra, ID incontro e link del tabellino di quella partita sul sito della stagione corrispondente. |
| D-095 | VERIFICATA NEL CODICE 3.2 | Esistono `urlTabellino`, `urlTabellinoLocale`, `urlTabellinoOnline`, `scorecardUrl`; la risoluzione della URL e gia implementata nel normalized exporter 3.2. Non fare concatenazioni inventate con la root del sito corrente. |
| D-096 | APERTA - LINK REALI | Prima della pubblicazione, verificare per ciascun sito/anno la rotta reale e la eventuale possibilita di puntare direttamente al singolo incontro. I link delle pagine di giornata non garantiscono un'ancora per il match. |
| D-097 | REGOLA DATI | L'ID incontro FCM e distinto dalla giornata Serie A e dalla competizione. Conservare tutti i campi per il record e per la scheda giocatore, anche in competizioni parallele. |
| D-098 | IMPLEMENTAZIONE AUDIT | L'audit v2 classifica Play Off/Spareggio, esclude Youth League dalle osservazioni utilizzabili e conserva contatori di esclusione. I riferimenti URL negli esempi rimangono null finche non risolti tramite 3.2. Nessun DB/Java/sito modificato. |


## Decisioni D-099--D-112: integrazione nativa Records Top & Flop

| ID | Stato | Decisione / evidenza |
|---|---|---|
| D-099 | APPROVATA DALL'UTENTE | La nuova famiglia records si chiama **Records Top & Flop**. E parte di RecordsNext 4.0, non un progetto o publisher distinto. |
| D-100 | APPROVATA DALL'UTENTE | Riutilizzare le logiche 3.2.0 FUNZIONANTI per elaborazione, archivio, configurazione, GUI, esportazione, manifest, link e pubblicazione multisito; la 3.2 produttiva rimane intatta. |
| D-101 | APPROVATA DALL'UTENTE | Tutte le statistiche individuali complete costituiscono la base riutilizzabile per schede e Records Top & Flop: calcolo dei record come aggregazione derivata, non raccolta dati autonoma. |
| D-102 | VERIFICATA SORGENTE 3.2 | Il catalogo record e in `RecordFamily`, `CoreRecordCatalog`; configurazione in `ProcessingConfigWriter` e `ProcessingConfigLoader`; `PipelinePreflight` e `DependencyInventory` gestiscono i prerequisiti. |
| D-103 | VERIFICATA SORGENTE 3.2 | La pipeline centrale e `RecordsNextPipeline` e il publisher JS e `Records2026SitePublisher`, con controllo esplicito del numero e dei prefissi dei JS prodotti. |
| D-104 | VERIFICATA SORGENTE 3.2 | `SeasonFamilyShardPublisher` divide per stagione, crea facade e shard JS flat nella cartella js con massimo predefinito 1536 KiB. Estendere la lista FAMILIES e i relativi test. |
| D-105 | VERIFICATA SORGENTE 3.2 | `SeasonNormalizedExporter` produce i riferimenti canonici al tabellino; `MatchesJsExporter` li espone tramite `scorecardUrl`. Nessun URL storico sintetizzato a mano. |
| D-106 | REGOLA IMPLEMENTATIVA | Il filtro sportivo CAMPIONATO/COPPE e separato dalla famiglia catalogo `TOP_FLOP`. Play Off - Play Out e Spareggio Serie B=CAMPIONATO; Youth League sempre esclusa dai dati pubblici. |
| D-107 | REGOLA IMPLEMENTATIVA | Stessa prestazione reale FCA non conteggiata piu volte nei record reali, anche se utilizzata in molte partite/competizioni fantasy; ogni match fantasy resta distinto nei record fantasy. |
| D-108 | REGOLA IMPLEMENTATIVA | Prevedere FULL e CONSOLIDATED, multisito, rollback e validazioni JS 3.2: nessuna scorciatoia per i nuovi dati e nessun deploy autonomo. |
| D-109 | REGOLA IMPLEMENTATIVA | Non attivare la nuova famiglia nel catalogo selezionabile o in pubblicazione finche archivio e exporter non sono funzionanti e testati; impedire successo apparente con output vuoti. |
| D-110 | VERIFICATA FASE 3C1 | ZIP Windows 3C1: `stato=OK`, 13 test, 552025 originali, 546775 ammessi, 5250 Youth esclusi, DB sandbox invariato, bibbia v13 generata. |
| D-111 | APERTA | Elenco definitivo e soglie dei singoli records Top & Flop, definizione formale delle esclusioni senza Formazione e validita storica dei tesseramenti. Non inventare classifiche o soglie. |
| D-112 | IMPLEMENTATA: DOCUMENTAZIONE | Questa fase aggiunge mappa integrazione e regole alla continuita documentale, senza modificare la pipeline Java, la GUI, gli output o il database. La famiglia non e ancora implementata. |
~~~~

### `docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v14.md`

SHA-256: `66860741d0825e0f5c983e7f2506674443a175e421a4e43ceff2ff8c92f12237`

~~~~markdown
# RecordsNext 4.0 - Diario implementazione v14

# RecordsNext 4.0 - Diario implementazione v13

# RecordsNext 4.0 - Continuita documentale DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v12

Versione additiva del 2026-11-01. La cronologia integrale precedente e preservata sotto.

# RecordsNext 4.0 - Diario implementazione v10

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


## Verifica dell'output Fase 3B2 sul PC e decisione operativa

- Esaminato ZIP Windows `RecordsNext4_Fase3B2_OUTPUT_v1_20261101_002713_328984.zip`.
- `MANIFEST_FASE3B2_v1.json`: `stato=OK`, branch `feature/recordsnext4-players`, base `f9bf30323d9204d70e91e242bddfc72c2060ecf9`; stessa impronta SHA-256 del sandbox prima/dopo.
- `LOG_TEST_REGOLE_v1.txt`: otto test superati. `LOG_AUDIT_REGOLE_v1.txt`: 21 stagioni, 180 competizioni, 13.072 incontri, 22.265 tabellini; 168 riferimenti senza incontro rimangono non classificati.
- Riletto `docs/ANALISI_PERSONALE_RU.md` e il glossario di `tools/Report_RU_Storico_Completo_v8_4.ps1`: RU = voto assegnato d'ufficio a copertura di una mancanza. Non associare una RU a un giocatore storico reale.
- L'utente ha approvato la metrica **individuale**, non il controfattuale di squadra: 23 occasioni fuori, 3 gare con voto 6 -> 18/23. Nessun calcolo effettivo sui giocatori e nessun cap implementati in questa revisione.
- Creato test unitario della **sola formula** su dati sintetici. Prima della successiva implementazione su SQLite occorre verificare su esempi reali quali righe costituiscano esclusioni e quale voto sia valido.
- Questa revisione installa soltanto documentazione, un test di formula, il generatore v10 e un runner conservativo. Non modifica Java, GUI, pipeline, archivi 3.2 o siti.


## Revisione della metrica su richiesta utente (2026-11-01)

- Esaminato lo ZIP di esecuzione Windows `RecordsNext4_DecisioniGiocatori_OUTPUT_v1_20261101_003202_168980.zip`: contiene bibbia v10, registro v10, test formula v1 e manifest `stato=OK`.
- Distinti il rendimento medio nelle sole esclusioni con voto (`S/P`) e la misura stagionale su tutte le esclusioni (`S/E`), per voto reale e fantavoto completo di bonus/malus.
- Creati `SPECIFICA_METRICA_VOTO_SPRECATO_GIOCATORE_v2.md` e `Test-RecordsNext4MetricheIndividuali-v2.py` per il contratto puramente aritmetico; test su dati sintetici, nessuna lettura FCM/FCA in questo intervento.
- Creati piano v12, registro decisioni v11, diario v11 e generatore bibbia v11. Conservate le versioni precedenti.
- Restano da verificare sui dati reali la definizione operativa di esclusione, voto valido, fantavoto disponibile per esclusi, bonus/malus storici e deduplicazione delle partite fantasy parallele.
- Nessuna implementazione di record nei sorgenti Java e nessun deploy; database invariato perche non viene aperto.


## Fase 3C0 - 2026-11-01

- Verificato lo ZIP Windows `RecordsNext4_Metriche_OUTPUT_v1_20261101_004055_490269.zip`: manifest OK, 8 test metriche superati, bibbia v11 prodotta; nessun deploy/commit dichiarato.
- Esaminato il database sandbox 3.2 in sola lettura: 21 stagioni, 552.025 righe Formazione, 180 righe Competizione con 25 nomi distinti; la stessa giornata puo contenere piu incontri fantasy per giocatore-squadra.
- Caso campione 2025/26, codice 100843: tre tesseramenti snapshot, presenza nello stesso giorno in campionato e coppa per River Pino.
- Il prototipo di audit Fase 3C0 verifica l'integrita delle join ed elenca le competizioni non assegnate, senza interpretare le esclusioni o sommare i voti.
- Stato: audit testato localmente, codice integrale nella bibbia incrementale v12; installazione e test Windows ancora da confermare.

## Fase 3C1 - revisione classificazione e riferimenti tabellino

- Ricevuto e verificato output 3C0 Windows: `ESITO OK`, 10/10 test, 552025 righe Formazione; SHA256 del sandbox invariato, bibbia v12. Le 159678 sovrapposizioni sono contesti fantasy distinti, non duplicazioni reali.
- L'utente ha classificato `Play Off - Play Out` e `Spareggio Serie B` come campionati e richiesto l'esclusione completa di `Youth League` da record/schede.
- Fonte Java 3.2 esaminata: `SeasonNormalizedExporter.java` espone `urlTabellino`, `urlTabellinoLocale`, `urlTabellinoOnline` e conserva `source_event_id`; `MatchesJsExporter.java` espone `matchId`, `competitionId`, `seasonId`, `scorecardUrl`.
- L'audit v2 mantiene ID incontro e giornata; aggiunge `idGiornataFcm` e `scorecardLinkStatus` negli esempi. URL effettivo assente nell'audit raw: resta null.
- Nessuna simulazione RU/sostituzioni, nessun calcolo degli esclusi mancanti da Formazione, nessuna modifica alla pipeline o al DB.


## Revisione 2026-11-01 - nuova famiglia Records Top & Flop

- L'utente ha chiarito che l'intero modulo giocatori e **una famiglia record del RecordsNext esistente**, con il nome pubblico **Records Top & Flop**.
- Rianalizzati gli effettivi sorgenti 3.2.0 forniti (`RecordsNext2.0.zip`), in particolare `RecordFamily`, `CoreRecordCatalog`, `ProcessingConfigWriter`, `ProcessingConfigLoader`, `RecordsNextPipeline`, `Records2026SitePublisher`, `SeasonFamilyShardPublisher`, `MatchesJsExporter`, `SeasonNormalizedExporter`, `RecordsNext2Dashboard`.
- `Records2026SitePublisher.validateGenerated` valida anche **il conteggio totale** dei file JS e i loro prefissi: richiede modifica coordinata prima di aggiungere un nuovo exporter. `SeasonFamilyShardPublisher.FAMILIES` e la lista esplicita delle famiglie sharded: richiede aggiunta con test flat.
- La 3.2 usa facade/shard per stagione nei js dei siti, con scope multisito e protezione rollback. Va **estesa**, non replicata con tool dedicati.
- Verificato output Windows Fase 3C1: 13/13 test, 21 stagioni, 552025 Formazione, 5250 Youth League escluse, 546775 ammesse, DB immutato.
- Stato codice: **nessun Java/SQLite/GUI/sito modificato in questa revisione**. Restano prototipi audit; prossimo sviluppo sul codice Java nel worktree 4.0. Documenti e script presenti nella bibbia incrementale v14.
~~~~

### `docs/SPECIFICA_RECORDS_TOP_FLOP_v1.md`

SHA-256: `bd17655762c5812e1c8d6ca2c8c06684990d62ddce22613e72ca516215aa7cce`

~~~~markdown

# RecordsNext 4.0 - Famiglia Records Top & Flop - Specifica v1

Data 2026-11-01. **Decisione di prodotto approvata**, implementazione Java NON ancora eseguita.

## Scopo

**Records Top & Flop** e una nuova famiglia nativa del sistema RecordsNext 4.0, accanto a Classici, Serie, RU, Modificatori, Soglie/Fortuna. Ha una selezione di records nella GUI, viene elaborata dalla pipeline standard, archiviata per stagione e pubblicata mediante il publisher e lo shard manager esistenti. La scheda giocatore non e una famiglia aggiuntiva: consulta lo stesso dataset canonico che alimenta Top & Flop.

**Distinzione semantica:** `TOP_FLOP` = famiglia record nel catalogo tecnico; `CAMPIONATO`, `COPPE` = famiglie sportive di competizioni usate come filtro. Nessuna Youth League nei dati pubblici. Play Off - Play Out e Spareggio Serie B ricadono in CAMPIONATO.

## Modello dei fatti e chiavi

- Entita giocatore storico (codice Gazzetta dal FCA, identita canonica esplicita per collisioni), ID FCA locale di stagione, anagrafica storica originale e provenienza.
- Prestazione reale: una sola riga per giocatore storico verificato + stagione + giornata Serie A, anche se lo stesso voto compare in piu incontri fantasy. Stato senza voto/speciale distinto dalla presenza effettiva.
- Tesseramento FCM: stagione, fantasquadra, giocatore annuale, eventuale cronologia realmente verificabile; lo snapshot Tesserato non prova la durata temporale.
- Evento fantasy: stagione + competizione ID + incontro FCM ID + fantasquadra ID + calciatore annuale, anche per campionato e coppa nello stesso turno. Distinguere titolare iniziale, conteggiato, panchinaro, riserva, fuori lista, non schierato provato, voto e fantavoto e bonus/malus; non inventare assenze.
- Collegamento record puntuale: stagione, competizione, fantasquadra, id incontro, giornata FCM, giornata Serie A e link `scorecardUrl` risolto dal normalizzato 3.2; URL ignoto = null/stato da risolvere, mai concatenare la root del sito corrente a un sito storico.

## Indicatori personali approvati

Per ogni contesto fantasy appropriato, sia per titolari sia per non utilizzati, conservare tutti i voti, fantavoti ed eventi. Per le esclusioni certe:

`E` = esclusioni totali, `P` = esclusioni con voto valido, `SV` = somma voti validi, `SF` = somma fantavoti validi con bonus/malus storici.

- Media voto sprecato: `SV/P` (P>0).
- Indice pesato stagionale voto: `SV/E` (E>0).
- Fantamedia sprecata: `SF/P` (P>0).
- Indice pesato stagionale fantavoto: `SF/E` (E>0).

Un 7 rimasto fuori contribuisce 7, non 1; assenza senza voto contribuisce 0 al numeratore e rimane nel denominatore se l'esclusione e accertata. RU e sostituzioni storiche (3,5,11 ecc.) rispettano gli originali ma una RU non e una prestazione individuale reale.

Gli stessi fatti di voto e fantavoto sono disponibili **per i titolari e i conteggiati**: la scheda e i record devono essere simmetrici. Non simulare formazioni alternative o risultati di squadra per stimare l'errore individuale.

## Scoping e doppio conteggio

Ogni primato ha dimensioni esplicite: giocatore, stagione, squadra fantasy, competizione, famiglia sportiva CAMPIONATO/COPPE, insieme delle competizioni, carriera, partita. Non aggregare piu competizioni come se fossero giorni reali differenti. Le graduatorie reali usano le prestazioni FCA una sola volta; le statistiche di impiego fantasy possono legittimamente avere piu eventi nella stessa giornata e devono mostrarlo.

## Registro dei record

Categorie candidabili, da definire con il catalogo 3.2 e il successivo confronto col regolamento: migliori/peggiori voti e fantavoti, medie, bonus/malus, presenze, titolarita, frequenze di esclusione, medie e indici di spreco, singolo picco per partita, dati stagionali e all-time. Questo elenco **non costituisce ancora un catalogo approvato di singoli record**: nomi, soglie minime, limiti e tie-break rimangono aperti.

## Stato di implementazione

In questa fase sono stati ricontrollati i punti di estensione della 3.2. Gli exporter reali/fantasy sviluppati nelle fasi precedenti sono prototipi di sola lettura, non costituiscono il motore records in Java. La famiglia TOP_FLOP non deve essere selezionabile dalla GUI finche pipeline, exporter, manifest, shard e test non saranno completi.
~~~~

### `docs/MAPPA_INTEGRAZIONE_TOP_FLOP_3_2_v1.md`

SHA-256: `a70a8ae35af7725794127ce70a0c30a292d2666fc87ad6e0b995dec44ee29fae`

~~~~markdown

# RecordsNext 4.0 - Mappa integrazione Records Top & Flop nella 3.2 - v1

**Fonte verificata**: `RecordsNext2.0.zip` caricato dall'utente, percorso `src/main/java/it/alterlega/recordsnext/`. File reali ispezionati; nessuna progettazione di publisher alternativo.

| Fase tecnica | File/classe esistente | Intervento 4.0 richiesto (non ancora fatto) | Regressione minima |
|---|---|---|---|
| Catalogo famiglia | `app/model/RecordFamily.java`, `CoreRecordCatalog.java` | Aggiunta `TOP_FLOP` e figli granulari reali, dipendenze appropriate | `CoreRecordCatalogCompletenessTest`, `ModularProcessingModelTest` |
| Configurazione | `app/config/ProcessingConfigWriter.java`, `ProcessingConfigLoader.java`, `config/processing.json` | Selezione `topFlop` e figli con compatibilita read/write delle configurazioni 3.2 | `ProcessingConfigWriter*Test`, `ProcessingConfigLoaderTest` |
| Validazione prerequisiti | `app/PipelinePreflight.java`, `app/model/DependencyInventory.java`, `ExecutionPlanner.java` | Verificare sorgenti FCA/FCM, dati disponibili, eventuale records disabilitato; non mostrare generato se mancano dati | `ExecutionPlannerTest`, `PipelinePreflightTest` |
| GUI nativa | `gui/RecordsNext2Dashboard.java` e stato ProcessingConfigWriter.State | Aggiungere pagina/checkbox famiglia e figli; non scegliere competizioni nella GUI | Test GUI/configurazione, apertura GUI, persistenza |
| Normalizzazione e origine link | `SeasonNormalizedExporter.java`, `app/matches/MatchesJsExporter.java` | Reimpiego ID partita e `scorecardUrl` normalizzato; never build URL manuale | `SeasonNormalizedExporter`/`MatchesJsExporter` test contratto, test siti storici |
| Archivio stagionale | `SeasonRecordsArchiveBuilder.java`, `RecordsNextPreparationService.java`, `app/RecordsNextPipeline.java` | Nuovo archivio Top & Flop e ricostruzione FULL/incrementale CONSOLIDATED nello stesso ciclo | `RecordsNextPipelineSeasonScopeTest`, `RecordsNextPipelinePublicationModeTest` |
| Esportazione famiglia | `Records2026SitePublisher.java`, `app/*/*FamilyJsExporter.java` | `TopFlopFamilyJsExporter` nello STESSO publisher con prefisso/file/globale ufficiale; allineare `validateGenerated` e `expectedTotal` | Test exporter, validazione file, rollback |
| JS stagionali | `app/output/SeasonFamilyShardPublisher.java` | Inserire spec TopFlop nella lista privata `FAMILIES` rispettando flat shards in `js` e massimo 1536 KiB; niente sottocartelle | `SeasonFamilyShardPublisherFlatTest`, `SeasonFamilyShardPublisherTest` |
| Manifest e dipendenze | `app/manifest/ManifestPublishingSupport.java`, `ManifestJsWriter.java` | Aggiungere nuova famiglia ai metadati validati senza perdere file preesistenti | `ManifestJsWriterTest`, `ManifestPublishingSupportTest` |
| Siti e rollback | `RecordsNextPipeline.java`, `app/output/SeasonPublicationTargetRepository.java`, `Records2026SitePublisher.java` | Scope stagione/sito e target storici per shards, publish atomica + rollback gia 3.2 | `RecordsNextPipelinePublicationModeTest`, `SeasonPublicationScopeRepositoryTest` |
| Visualizzazione | Visualizzatori HTML e installazione attuali | Integrare Records Top & Flop come nuova sezione e scheda giocatore, consumando gli stessi JS; senza imporre filtro competizioni in GUI | test HTML link storici, siti e filtri, zero Youth League |

## Vincoli esistenti verificati nel codice

1. `RecordsNextPipeline.IMPLEMENTED_FAMILIES` elenca le famiglie ammesse: Top & Flop **non** deve esservi aggiunta prima del motore utilizzabile, altrimenti si rischia esecuzione fittizia.
2. `Records2026SitePublisher.validateGenerated` valida prefisso, nomi e numero atteso JS: ogni nuova famiglia richiede modifica coordinata al conteggio e ai test.
3. `SeasonFamilyShardPublisher.FAMILIES` e una lista esplicita, shard flat per stagione sotto la `js` del sito, con `DEFAULT_FLAT_MAX_SHARD_BYTES=1536*1024`.
4. `RecordsNextPipeline` supporta modalita FULL/CONSOLIDATED e scope di pubblicazione per singolo sito o tutti quelli configurati. Riutilizzare quel ciclo e `publishGeneratedDirectory`, non creare deploy personalizzati.
5. `MatchesJsExporter` esporta `seasonId`, `competitionId`, `matchId`, `scorecardUrl`. Il collegamento non va ricostruito con solo numero di giornata.
6. `ProcessingConfigWriter.State` espone campi fissi: aggiungere la famiglia richiede adeguare anche costruzione e salvataggio dalla GUI, con compatibilita dei vecchi file JSON.

## Ordine attuativo

**Primo commit tecnico futuro:** modello fatti individuali canonicali + regressione su sample/stagioni reali, senza ancora cambiare GUI/pubblicazione. **Secondo blocco:** exporter + family catalog/config + GUI + shard/manifest/pubblicazione e relativi test. Versionare ogni sorgente modificato, consolidare in Java solo dopo verifica. La versione 3.2 operativa non viene mai modificata.
~~~~

### `tools/Test-RecordsNext4TopFlopContract-v1.py`

SHA-256: `91a8cbed8a3e4e5dc53872aeca7e9873bf48bbce4c89bd8321795830090aebb8`

~~~~python
#!/usr/bin/env python3
"""Read-only regression anchors for the actual RecordsNext 3.2 Java integration."""
from pathlib import Path
import argparse
import unittest

ROOT = None


def read(rel):
    p = ROOT / rel
    if not p.is_file():
        raise AssertionError('Sorgente 3.2 mancante: '+rel)
    return p.read_text(encoding='utf-8-sig')


PREFIX = 'src/main/java/it/alterlega/recordsnext/'


class Contract(unittest.TestCase):
    def test_family_catalog(self):
        text = read(PREFIX+'app/model/RecordFamily.java')
        self.assertIn('CLASSICS(',text)
        self.assertIn('THRESHOLDS_LUCK(',text)

    def test_children_catalog(self):
        text=read(PREFIX+'app/model/CoreRecordCatalog.java')
        self.assertIn('ProcessingConfigWriter.CLASSICS',text)
        self.assertIn('addFamilyChildren(',text)

    def test_writer_state_and_config(self):
        text=read(PREFIX+'app/config/ProcessingConfigWriter.java')
        self.assertIn('public record State(',text)
        self.assertIn('public static void save(',text)
        self.assertIn('"schemaVersion": "2.0"',text)

    def test_config_loader(self):
        text=read(PREFIX+'app/config/ProcessingConfigLoader.java')
        self.assertIn('RecordFamily.values()',text)
        self.assertIn('CoreRecordCatalog',text)

    def test_dependencies_preflight(self):
        text=read(PREFIX+'app/PipelinePreflight.java')
        self.assertIn('DependencyInventory',text)
        self.assertIn('ExecutionPlanner.plan(',text)

    def test_pipeline_lifecycle(self):
        text=read(PREFIX+'app/RecordsNextPipeline.java')
        for token in ['IMPLEMENTED_FAMILIES', 'RecordsNextPreparationService', 'SeasonRecordsArchiveBuilder.build(', 'PublicationMode.ALL_CONFIGURED_SITES','SeasonFamilyShardPublisher.prepareFlat(', 'publishGeneratedDirectory(']:
            self.assertIn(token,text)

    def test_publisher_validation_and_rollback(self):
        text=read(PREFIX+'Records2026SitePublisher.java')
        for token in ['validateGenerated(', 'expectedTotal', 'publishWithRollback(', 'ManifestPublishingSupport.write(', 'SeasonFamilyShardPublisher.prepareFlat(']:
            self.assertIn(token,text)

    def test_shard_limit_and_family_specs(self):
        text=read(PREFIX+'app/output/SeasonFamilyShardPublisher.java')
        for token in ['DEFAULT_FLAT_MAX_SHARD_BYTES', '1536L * 1024L', 'private static final List<FamilySpec> FAMILIES', 'prepareFlat(']:
            self.assertIn(token,text)

    def test_match_public_contract(self):
        text=read(PREFIX+'app/matches/MatchesJsExporter.java')
        for token in ['"seasonId"', '"competitionId"', '"matchId"', '"scorecardUrl"']:
            self.assertIn(token,text)

    def test_normalized_links(self):
        text=read(PREFIX+'SeasonNormalizedExporter.java')
        for token in ['urlTabellino', 'urlTabellinoLocale', 'urlTabellinoOnline']:
            self.assertIn(token,text)

    def test_gui_family_checks(self):
        text=read(PREFIX+'gui/RecordsNext2Dashboard.java')
        for token in ['ProcessingConfigWriter', 'JTabbedPane', 'familySelectionPanel(']:
            self.assertIn(token,text)

    def test_existing_regression_test_sources(self):
        base=ROOT/'src/test/java/it/alterlega/recordsnext/'
        files=['app/model/CoreRecordCatalogCompletenessTest.java', 'app/RecordsNextPipelinePublicationModeTest.java', 'app/output/SeasonFamilyShardPublisherFlatTest.java']
        for rel in files:
            self.assertTrue((base/rel).is_file(), str(base/rel))


if __name__ == '__main__':
    p=argparse.ArgumentParser()
    p.add_argument('--project-dir',type=Path,required=True)
    args=p.parse_args()
    ROOT=args.project_dir.resolve(strict=True)
    suite=unittest.defaultTestLoader.loadTestsFromTestCase(Contract)
    result=unittest.TextTestRunner(verbosity=2).run(suite)
    raise SystemExit(0 if result.wasSuccessful() else 1)
~~~~

### `tools/Create-RecordsNext4WorkingCodeMd-v14.py`

SHA-256: `719f1b0ef2f530357f6733e7ef81049ca620f1abb41db450b8bdee9ff64af2e0`

~~~~python
#!/usr/bin/env python3
"""Conservative incremental bible preserving the verified v13 reference."""
from __future__ import annotations
import argparse
import hashlib
from pathlib import Path
import subprocess
import sys

PREVIOUS='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v13.md'
DEST='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v14.md'
NEW_FILES=[
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v15.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v14.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v14.md',
    'docs/SPECIFICA_RECORDS_TOP_FLOP_v1.md',
    'docs/MAPPA_INTEGRAZIONE_TOP_FLOP_3_2_v1.md',
    'tools/Test-RecordsNext4TopFlopContract-v1.py',
    'tools/Create-RecordsNext4WorkingCodeMd-v14.py',
    'tools/Invoke-RecordsNext4TopFlop-v1.py',
]


def sha(p):
    h=hashlib.sha256()
    with p.open('rb') as stream:
        for block in iter(lambda:stream.read(1024*1024),b''):
            h.update(block)
    return h.hexdigest()


def git(root,*args):
    proc=subprocess.run(['git','-C',str(root),*args],capture_output=True,
                        text=True,encoding='utf-8',errors='replace')
    if proc.returncode:
        raise RuntimeError('Git: '+proc.stderr.strip())
    return proc.stdout.strip()


def main():
    ap=argparse.ArgumentParser()
    ap.add_argument('--project-dir',required=True,type=Path)
    root=ap.parse_args().project_dir.resolve(strict=True)
    for name in [PREVIOUS,*NEW_FILES]:
        if not (root/name).is_file():
            raise RuntimeError('Sorgente bibbia mancante: '+name)
    lines=['# RecordsNext 4.0 - Bibbia incrementale v14','',
           'Fase integrazione architetturale Records Top & Flop: nessun Java, database o output sito modificato.','',
           '## Provenienza', '',
           '- Branch: `'+git(root,'branch','--show-current')+'`',
           '- HEAD: `'+git(root,'rev-parse','HEAD')+'`',
           '- Bibbia v13 SHA-256: `'+sha(root/PREVIOUS)+'`',
           '- Questo documento e additivo e non sostituisce v13.','',
           '## File completi e SHA-256','']
    for name in NEW_FILES:
        file=root/name
        lang='python' if name.endswith('.py') else 'markdown'
        lines.extend(['### `'+name+'`','','SHA-256: `'+sha(file)+'`','',
                      '~~~~'+lang,file.read_text(encoding='utf-8-sig').rstrip('\n'),
                      '~~~~',''])
    out=('\n'.join(lines).rstrip()+'\n').encode('utf-8')
    target=root/DEST
    if target.exists():
        if not target.is_file() or target.read_bytes()!=out:
            raise RuntimeError('Bibbia v14 gia presente ma diversa: stop')
        print('BIBBIA v14 GIA PRESENTE IDENTICA:',target,flush=True)
    else:
        with target.open('xb') as stream:
            stream.write(out)
        print('BIBBIA v14 GENERATA:',target,flush=True)
    return 0


if __name__=='__main__':
    try:
        sys.exit(main())
    except Exception as exc:
        print('ERRORE BIBBIA v14:',exc,file=sys.stderr)
        sys.exit(1)
~~~~

### `tools/Invoke-RecordsNext4TopFlop-v1.py`

SHA-256: `5b866865747232e5ff717034d833c6d48922f0cc16fcd7b032295fb292000841`

~~~~python
#!/usr/bin/env python3
"""Install versioned docs, verify 3.2 anchors, generate v14 bible; no DB changes."""
from __future__ import annotations
import argparse
from datetime import datetime
import hashlib
import json
import os
from pathlib import Path
import subprocess
import sys
import traceback
from zipfile import ZipFile,ZIP_DEFLATED

NEW_FILES=[
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v15.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v14.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v14.md',
    'docs/SPECIFICA_RECORDS_TOP_FLOP_v1.md',
    'docs/MAPPA_INTEGRAZIONE_TOP_FLOP_3_2_v1.md',
    'tools/Test-RecordsNext4TopFlopContract-v1.py',
    'tools/Create-RecordsNext4WorkingCodeMd-v14.py',
    'tools/Invoke-RecordsNext4TopFlop-v1.py',
]
REQUIRED=[
    'pom.xml',
    'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v13.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v14.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v13.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v13.md',
]


def sha(path):
    h=hashlib.sha256()
    with path.open('rb') as s:
        for chunk in iter(lambda:s.read(1024*1024),b''):
            h.update(chunk)
    return h.hexdigest()


def git(root,*args):
    proc=subprocess.run(['git','-C',str(root),*args],capture_output=True,text=True,
                        encoding='utf-8',errors='replace')
    if proc.returncode:
        raise RuntimeError('Git: '+proc.stderr.strip())
    return proc.stdout.strip()


def run(argv):
    proc=subprocess.run([sys.executable,*map(str,argv)],capture_output=True,text=True,
                        encoding='utf-8',errors='replace',env=dict(os.environ,PYTHONDONTWRITEBYTECODE='1'))
    return proc.returncode,proc.stdout+proc.stderr


def main():
    ap=argparse.ArgumentParser()
    ap.add_argument('--work-dir',type=Path,default=Path('D:/DEV_APPS/RecordsNext4.0'))
    ap.add_argument('--output-dir',type=Path,default=Path('D:/DEV_APPS/downloads'))
    args=ap.parse_args()
    kit=Path(__file__).resolve().parent.parent
    output=args.output_dir.resolve(strict=True)
    timestamp=datetime.now().strftime('%Y%m%d_%H%M%S_%f')
    result=output/('RecordsNext4_TopFlopIntegrazione_OUTPUT_v1_'+timestamp+'.zip')
    report={'fase':'integrazione-architetturale-topflop','stato':'ERRORE',
            'javaModificato':False,'databaseModificato':False,'pubblicazioneEseguita':False,
            'bibbia':'v14','installati':[]}
    logs=[]
    ok=False
    work=None
    try:
        work=args.work_dir.resolve(strict=True)
        for name in REQUIRED:
            if not (work/name).is_file():
                raise RuntimeError('Prerequisito mancante: '+name)
        root=Path(git(work,'rev-parse','--show-toplevel')).resolve(strict=True)
        if os.path.normcase(str(root)).casefold()!=os.path.normcase(str(work)).casefold():
            raise RuntimeError('Il path non e la root del worktree: '+str(root))
        branch=git(work,'branch','--show-current')
        if branch!='feature/recordsnext4-players':
            raise RuntimeError('Branch inatteso: '+branch)
        report['branch']=branch
        report['head']=git(work,'rev-parse','HEAD')
        for name in NEW_FILES:
            source,target=kit/name,work/name
            if not source.is_file():
                raise RuntimeError('File kit mancante: '+name)
            if target.exists() and (not target.is_file() or sha(source)!=sha(target)):
                raise RuntimeError('File esistente diverso; non sovrascrivo: '+name)
        for name in NEW_FILES:
            source,target=kit/name,work/name
            if target.is_file():
                print('GIA PRESENTE IDENTICO:',name,flush=True)
            else:
                target.parent.mkdir(parents=True,exist_ok=True)
                with target.open('xb') as s:
                    s.write(source.read_bytes())
                report['installati'].append(name)
                print('INSTALLATO:',name,flush=True)
        operations=[
            ('Test-RecordsNext4TopFlopContract-v1.py',
             [work/'tools/Test-RecordsNext4TopFlopContract-v1.py','--project-dir',work]),
            ('Create-RecordsNext4WorkingCodeMd-v14.py',
             [work/'tools/Create-RecordsNext4WorkingCodeMd-v14.py','--project-dir',work]),
        ]
        for label,cmd in operations:
            status,outputText=run(cmd)
            logs.append((label,status,outputText))
            print(label,'codice:',status,flush=True)
            if status:
                raise RuntimeError('Fallito '+label+': '+outputText[-2500:])
        report['bibbiaSha256']=sha(work/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v14.md')
        report['stato']='OK'
        ok=True
    except Exception:
        error=traceback.format_exc()
        logs.append(('ERRORE',1,error))
        print(error,file=sys.stderr,flush=True)
    finally:
        with ZipFile(result,'x',compression=ZIP_DEFLATED) as z:
            z.writestr('MANIFEST.json',json.dumps(report,indent=2,ensure_ascii=False)+'\n')
            for label,status,body in logs:
                z.writestr('log/'+label.replace('.py','.txt'),'codice: '+str(status)+'\n'+body)
            for name in NEW_FILES:
                path=work/name if work is not None else None
                if path is not None and path.is_file():
                    z.write(path,name)
            if work is not None:
                bible=work/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v14.md'
                if bible.is_file():
                    z.write(bible,'docs/'+bible.name)
        print('ESITO:','OK' if ok else 'ERRORE',flush=True)
        print('ZIP DA CARICARE:',result,flush=True)
    return 0 if ok else 1


if __name__=='__main__':
    sys.exit(main())
~~~~
