# RecordsNext 4.0 - Bibbia incrementale v12

Data: 2026-11-01. Modello completo del calciatore e audit contesti, senza modifiche Java/DB/sito.

## Provenienza e continuita

- Branch: `feature/recordsnext4-players`
- Commit base: `f9bf30323d9204d70e91e242bddfc72c2060ecf9`
- Bibbia precedente v11 SHA-256: `0b05b05700c67e37d6c8a3f4dfbcac3341e0d0b760a776eb6e34727839e3904a`
- La presente bibbia INTEGRA, non sostituisce, la v11.

## Testo integrale di sorgenti e documenti con impronta

### `docs/PIANO_RECORDSNEXT4_GIOCATORI_v13.md`

SHA-256: `0359da489cf09cee61de4f263ea13462c2e90f6f9a3216fc37ea40bbb423298b`

~~~~markdown
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
~~~~

### `docs/REGISTRO_DECISIONI_RECORDSNEXT4_v12.md`

SHA-256: `f7d0de6bbed1662ec8d23ef83b619309a6291a1ac59b8f6890109cf89a8454e3`

~~~~markdown
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
~~~~

### `docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v12.md`

SHA-256: `c59be1de71ddbfa7b5e420528d8dc05745018ebc13b8a2ffc4b0e1fc09105989`

~~~~markdown
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
~~~~

### `docs/SPECIFICA_ARCHIVIO_COMPLETO_GIOCATORE_v1.md`

SHA-256: `07617ab2d936f31b6d0887f2344ab80f7e1039ca4fac599ddb3a69036eef7a68`

~~~~markdown
# RecordsNext 4.0 - Specifica dell'archivio completo per calciatore v1

Data: 2026-11-01. Specifica di modello e contratto; il codice Fase 3C0 implementa per ora un audit di contesti, non l'archivio canonico definitivo.

## Obiettivo approvato

**Ogni calciatore** ha un set completo di dati reali e fantasy, anche se non acquista mai valore per un record o per una classifica. Le statistiche devono includere **titolari iniziali, giocatori realmente conteggiati, subentrati, panchinari non utilizzati, fuori formazione e, dove verificabili, tesserati non inseriti in Formazione**. I record e la scheda giocatore sono viste sullo stesso dataset, non raccolte di dati ridotti in partenza.

### 1. Identita e prestazione reale (livello non replicato)

- **Calciatore storico**: codice `GiocatoreA.CODICEGAZZA`, con associazione manuale canonica per eventuali collisioni, alias e cambi codice; non confondere con `GiocatoreA.ID` annuale.
- **Anagrafica stagionale**: `(stagione, GiocatoreA.ID)`; conserva nome, ruolo, squadra reale secondo FCA, dati di identita e riferimenti foto se disponibili.
- **Giornata reale**: `(stagione, GiocatoreA.ID, GiocaIn.GIORNATA)` univoca. Conserva `GiocaIn`, `Punteggio` e statistiche originali, voto di fonte, bonus/malus originali e informazioni sulla partita reale. Nessuna duplicazione per presenza del calciatore in piu fantasquadre o competizioni.
- `GiocaIn` non equivale automaticamente a presenza effettiva o voto valido; `Storico` non deve essere sommato nuovamente a giornate gia conteggiate.

### 2. Tesseramento fantasy (livello per fantasquadra)

- **Collegamento**: `(stagione, GiocatoreA.ID, Fantasquadra.ID)`; un giocatore puo avere piu squadre nella stessa stagione, persino in serie diverse.
- La tabella FCM `Tesserato` nella copia 3.2 contiene ID, giocatore, squadra, ruolo, anno contratto, venduto e altri campi, **ma non un intervallo cronologico affidabile di inizio/fine tesseramento**. La riga e quindi uno snapshot, non una prova che fosse disponibile in qualunque giornata.
- Non classificare automaticamente come esclusione ogni partita della squadra in cui manca `Formazione`: bisogna provare il tesseramento valido in quella data e il fatto che la gara fantasy sia valida, disputata e non un riposo.

### 3. Evento di utilizzo fantasy (livello per singolo incontro)

Chiave di origine minima: `(stagione, Incontro.ID, Fantasquadra.ID, GiocatoreA.ID)`. Ogni evento contiene:

- Squadra fantasy, avversario, competizione, girone, famiglia (`CAMPIONATO` o `COPPE`), giornata fantasy e giornata Serie A, ID incontro e futura URL canonica del tabellino.
- Schieramento FCM originale: `Formazione.POS`, `RIG`, `ENTRATO`, `ENTRATORISERVA`, `MANUALE`; presenza/ordine in `Tabellino.LISTA`; voto/tabellino/modificatore/totale grezzi.
- Collegamento alla **stessa** prestazione reale FCA, se presente. Regole originali della competizione per fonti voto, bonus/malus, RU e sostituzioni (anche 3, 5, 8, 11).
- Stato iniziale (titolare/panchina/fuori lista) e **stato di effettivo utilizzo** sono campi distinti. `POS=0` descrive il titolare iniziale e non dimostra da solo che abbia contribuito; `ENTRATO` non basta per i casi anomali documentati.
- RU e codici speciali 0/-1 sono slot artificiali di tabellino, **non calciatori**: non generano prestazioni reali o bonus/malus di un calciatore.
- Se un calciatore compare in due competizioni nella stessa giornata, si conservano due eventi fantasy e un solo riferimento alla prestazione reale.

### 4. Famiglie di competizione

Le famiglie richieste dall'utente sono esattamente **CAMPIONATO** e **COPPE**; mantenere inoltre l'identita della singola competizione. La famiglia e una classificazione **esplicita e storicizzata**, non una deduzione dal nome in runtime. Censite 25 denominazioni storiche nelle 180 configurazioni FCM.

- Mappatura iniziale verificabile da denominazione per 22 nomi palesi (4 campionati, 18 coppe/supercoppe), come riportato nel nuovo audit: e una **proposta di classificazione**, non una decisione irreversibile.
- **Da approvare:** `Play Off - Play Out`, `Spareggio Serie B`, `Youth League`. Conservare le righe con famiglia non assegnata, non eliminarle e non inventare una terza famiglia di record.
- Anche dentro la stessa famiglia possono esserci piu incontri nella stessa giornata reale: mai accorparli prima di aver deciso la semantica di aggregazione.

### 5. Tutte le statistiche: utilizzati ed esclusi

Per ogni contesto, archiviare **voto originale, fantavoto con bonus/malus, dettaglio dei singoli eventi e stato di validita**, indipendentemente dall'uso fantasy del calciatore. Le medesime statistiche derivate vanno disponibili per i **conteggiati**, per gli **esclusi** e per tutti i casi classificabili separatamente.

Per il sottoinsieme **escluso**, confermate le quattro metriche della specifica v2:

- `E` = esclusioni valide; `P` = occasioni escluse con voto valido; `SV` = somma voti validi; `SF` = somma fantavoti relativi.
- Media voto sprecato `SV/P`; errore pesato voto `SV/E`; fantamedia sprecata `SF/P`; errore pesato fantavoto `SF/E`; quota voto `P/E`.
- Nessuna sottrazione di 6 o simulazione di un'altra formazione; esclusioni senza voto non danno errore, pur rimanendo nel denominatore `E`.

Per il sottoinsieme **conteggiato** prevedere in modo simmetrico numero incontri conteggiati, numero con voto valido, somme e medie di voto/fantavoto, eventi, bonus/malus, massimi, minimi e serie cronologiche. Le formule dei record specifici restano demandate al catalogo record; il dato va comunque conservato per ogni singola occorrenza.

### 6. Aggregazioni per record e scheda

Filtri indipendenti: `calciatore`, `stagione`, `fantasquadra`, `competizione`, `famiglia`, `giornata reale`, `partita fantasy`, `iniziale/conteggiato/escluso`, `voto valido`, `ruolo`.

- **Aggregati reali**: un solo record a calciatore/stagione/giornata, senza moltiplicare gol e assist per le fantasquadre.
- **Aggregati fantasy**: possono includere piu fatti per calciatore/giornata se giocati in squadre/competizioni diverse; esplicitare sempre il perimetro della classifica.
- Per `tutte le squadre` o `tutte le coppe` **non sommare medie**: sommare numeratori e denominatori e ricalcolare. Non sommare ciecamente due aggregati gia costruiti con diverse politiche di deduplicazione.
- Se lo stesso calciatore era nelle rose di tre fantasquadre, le esclusioni, le titolarita, i punteggi e gli indici saranno specifici di ogni squadra; la carriera all-time puo presentarli separati e, con una regola approvata, riaggregati.

### 7. Stato dei dati e questioni aperte

Il nuovo audit `Audit-RecordsNext4Contesti-v1.py` usa solo le 552.025 righe `Formazione` gia esistenti. Non attesta ancora le **esclusioni per assenza totale dalla formazione**; senza la storia temporale dei tesseramenti e una verifica delle partite/riposi non e corretto generarle. Non calcola voti, fantavoti o punti sprecati. Sono invece testate la tenuta delle chiavi dei contesti e l'esistenza di contesti paralleli.

Prima del dataset definitivo:

1. Accertare la cronologia dei tesseramenti e la copertura degli incontri non presenti in `Formazione`.
2. Verificare il voto/fantavoto di un non schierato, gli SV, il voto d'ufficio e la variazione dei bonus/malus per competizione.
3. Approvare classificazione delle tre competizioni rimaste senza famiglia.
4. Decidere il perimetro delle classifiche trasversali con piu gare nella stessa giornata e nella stessa famiglia.
5. Aggiungere eventi di fonte e ID/link stabili per la scheda individuale, rispettando regole di pubblicazione JS della 3.2.

Non modificare la pipeline Java, il database, la GUI o il sito fino ai test di integrazione.
~~~~

### `tools/Audit-RecordsNext4Contesti-v1.py`

SHA-256: `dc319a1395735f7395f94334a4983aac4e9a93e7178fd02bab8185ea0f73c311`

~~~~python
#!/usr/bin/env python3
"""Read-only audit of player/team/competition/match contexts, NOT final player facts."""
from __future__ import annotations
import argparse
from collections import Counter, defaultdict
import csv
from datetime import datetime
import gzip
import hashlib
import json
from pathlib import Path
import re
import sqlite3
import sys

SEASON_RE = re.compile(r'^raw_(\d{4}_\d{4})_fcm_formazione$')
CAMPIONATO = {'Campionato', 'Serie A', 'Serie B', 'Serie C'}
COPPE = {
    'Coppa AlterEgo', 'Coppa dei Campioni', 'Coppa di Lega',
    'Coppa Reame Perduto', 'Coppa Serie A', 'Coppa Serie B',
    'Coppa Serie C', 'Coppa tra le Coppe', 'Coppa UEFA',
    'Europa Pipps', 'Mitropa Cup', 'Super Coppa Reame Perduto',
    'Supercoppa AlterEgo', 'Supercoppa di Lega', 'Supercoppa Serie A',
    'Supercoppa Serie B', 'Supercoppa Serie C',
    'Vera finale di Coppa tra le Coppe'
}
# Explicitly unresolved: Play Off - Play Out, Spareggio Serie B, Youth League.
FAMILY = ('CAMPIONATO', 'COPPE')


def family(name):
    if name in CAMPIONATO:
        return 'CAMPIONATO'
    if name in COPPE:
        return 'COPPE'
    return None


def initial_status(pos):
    if pos == 0:
        return "TITOLARE_INIZIALE"
    if isinstance(pos, int) and pos > 0:
        return "PANCHINA"
    if pos == -1:
        return "FUORI_LISTA_FORMAZIONE"
    return "POS_DA_VERIFICARE"


def sha(path):
    h = hashlib.sha256()
    with Path(path).open('rb') as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b''):
            h.update(block)
    return h.hexdigest()


def table(season, source, name):
    if not re.fullmatch(r'\d{4}_\d{4}', season):
        raise ValueError('Stagione non valida')
    if source not in ('fcm', 'fca') or not re.fullmatch(r'[a-z]+', name):
        raise ValueError('Tabella non valida')
    return '"raw_' + season + '_' + source + '_' + name + '"'


def write_csv(path, rows, fields):
    with Path(path).open('w', encoding='utf-8-sig', newline='') as stream:
        writer = csv.DictWriter(stream, fieldnames=fields, extrasaction='ignore', delimiter=';')
        writer.writeheader()
        writer.writerows(rows)


def run(db, output, code=100843):
    db = Path(db).resolve(strict=True)
    output = Path(output).resolve()
    output.mkdir(parents=True, exist_ok=True)
    db_hash_before = sha(db)
    con = sqlite3.connect(db.as_uri() + '?mode=ro', uri=True)
    con.row_factory = sqlite3.Row
    all_tables = {r[0].lower() for r in con.execute("SELECT name FROM sqlite_master WHERE type='table'")}
    seasons = sorted({m.group(1) for name in all_tables if (m := SEASON_RE.fullmatch(name))})
    if not seasons:
        raise RuntimeError('Nessuna stagione con Formazione')
    required = ('formazione', 'incontro', 'girone', 'competizione', 'fantasquadra', 'tesserato')
    comp_report, cover_report, groups_report, overlaps_report, player_report = [], [], [], [], []
    sample = []
    total_rows = 0
    all_unclassified = set()
    for season in seasons:
        for fam in required:
            if ('raw_' + season + '_fcm_' + fam) not in all_tables:
                raise RuntimeError('Tabella mancante: ' + season + ' ' + fam)
        if 'raw_' + season + '_fca_giocatorea' not in all_tables:
            raise RuntimeError('GiocatoreA mancante: ' + season)
        f = table(season, 'fcm', 'formazione')
        m = table(season, 'fcm', 'incontro')
        g = table(season, 'fcm', 'girone')
        c = table(season, 'fcm', 'competizione')
        t = table(season, 'fcm', 'fantasquadra')
        p = table(season, 'fca', 'giocatorea')
        roster = table(season, 'fcm', 'tesserato')
        memberships = {(r[0], r[1]) for r in con.execute('SELECT IDGIOCATORE, IDSQUADRA FROM ' + roster)}
        competitions = {r['ID']: r['NOME'] for r in con.execute('SELECT ID, NOME FROM ' + c)}
        for comp_id, comp_name in sorted(competitions.items()):
            cf = family(comp_name)
            comp_report.append({'stagione': season, 'idCompetizioneFcm': comp_id,
                                'competizione': comp_name, 'famiglia': cf or '',
                                'verifica': 'MAPPATA' if cf else 'DA_APPROVARE'})
            if cf is None:
                all_unclassified.add((season, comp_name))
        count_source = con.execute('SELECT COUNT(*) FROM ' + f).fetchone()[0]
        sql = f'''SELECT x.IDGIOC AS idGioc, x.IDSQUADRA AS idTeam,
                         x.IDINCONTRO AS idMatch, x.POS AS pos,
                         x.ENTRATO AS entrato, x.ENTRATORISERVA AS entratoRiserva,
                         i.GIORNATADIA AS giornataA, c.ID AS idCompetizione,
                         c.NOME AS competizione, s.NOME AS nomeSquadra,
                         p.CODICEGAZZA AS codiceStorico, p.NOME AS nomeGiocatore
                   FROM {f} x
                   LEFT JOIN {m} i ON i.ID = x.IDINCONTRO
                   LEFT JOIN {g} g ON g.ID = i.IDGIRONE
                   LEFT JOIN {c} c ON c.ID = g.IDCOMPETIZIONE
                   LEFT JOIN {t} s ON s.ID = x.IDSQUADRA
                   LEFT JOIN {p} p ON p.ID = x.IDGIOC
                   ORDER BY x.IDINCONTRO, x.IDSQUADRA, x.IDGIOC'''
        count_join, counters = 0, Counter()
        group_counts = defaultdict(lambda: [0, 0, 0, 0])
        contexts = defaultdict(list)
        team_by_player = defaultdict(set)
        for r in con.execute(sql):
            row = dict(r)
            count_join += 1
            if row['idCompetizione'] is None or row['codiceStorico'] is None:
                raise RuntimeError('Join mancante nella stagione ' + season + ' match ' + str(row['idMatch']))
            cf = family(row['competizione'])
            st = initial_status(row['pos'])
            row['stagione'] = season
            row['famiglia'] = cf
            row['statoSchieramentoIniziale'] = st
            row['tesseratoSnapshot'] = (row['idGioc'], row['idTeam']) in memberships
            # Do not mark player as used/excluded solely from POS or ENTRATO.
            k = (row['idGioc'], row['idTeam'], row['idCompetizione'], cf)
            vals = group_counts[k]
            vals[0] += 1
            if st == 'TITOLARE_INIZIALE': vals[1] += 1
            elif st == 'PANCHINA': vals[2] += 1
            else: vals[3] += 1
            team_by_player[row['idGioc']].add(row['idTeam'])
            if row['giornataA'] is not None and row['giornataA'] > 0:
                contexts[(row['idGioc'], row['idTeam'], row['giornataA'])].append(
                    (row['idMatch'], row['idCompetizione'], row['competizione'], cf))
            if row['codiceStorico'] == code:
                sample.append(row)
            counters[cf or 'DA_APPROVARE'] += 1
            counters[st] += 1
            if not row['tesseratoSnapshot']:
                counters['SENZA_TESSERAMENTO_SNAPSHOT'] += 1
        if count_join != count_source:
            raise RuntimeError(f'Perdita o duplicazione righe {season}: source {count_source} join {count_join}')
        total_rows += count_source
        for (pid, tid, cid, cf), counts in sorted(group_counts.items()):
            groups_report.append({
                'stagione': season, 'idGiocatoreFca': pid, 'idSquadraFcm': tid,
                'idCompetizioneFcm': cid, 'famiglia': cf or '',
                'righeFormazione': counts[0], 'titolareIniziale': counts[1],
                'inPanchina': counts[2], 'fuoriListaFormazione': counts[3]})
        for (pid, tid, day), events in sorted(contexts.items()):
            if len(events) > 1:
                overlaps_report.append({'stagione': season, 'idGiocatoreFca': pid,
                    'idSquadraFcm': tid, 'giornataReale': day,
                    'numeroIncontriFantasy': len(events),
                    'numeroCompetizioni': len({x[1] for x in events}),
                    'famiglieOsservate': '|'.join(sorted({x[3] or 'DA_APPROVARE' for x in events})),
                    'incontriFcm': '|'.join(str(x[0]) for x in events),
                    'competizioni': '|'.join(x[2] for x in events)})
        cover_report.append({'stagione': season, 'righeFormazioneOriginali': count_source,
            'righeContestiVerificate': count_join,
            'giocatoriIdFcaDistinti': len(team_by_player),
            'giocatoriPresentiPiuSquadre': sum(len(v) > 1 for v in team_by_player.values()),
            'contestiCampionato': counters['CAMPIONATO'], 'contestiCoppe': counters['COPPE'],
            'contestiDaApprovare': counters['DA_APPROVARE'],
            'titolariIniziali': counters['TITOLARE_INIZIALE'],
            'panchinari': counters['PANCHINA'],
            'fuoriListaFormazione': counters['FUORI_LISTA_FORMAZIONE'],
            'senzaTesseramentoSnapshot': counters['SENZA_TESSERAMENTO_SNAPSHOT']})
    con.close()
    stamp = datetime.now().strftime('%Y%m%d_%H%M%S_%f')
    base = 'RecordsNext4_Contesti_v1_' + stamp
    files = {}
    def save_csv(suffix, data, fields):
        path = output / (base + '_' + suffix + '.csv')
        write_csv(path, data, fields)
        files[suffix] = {'name': path.name, 'sha256': sha(path), 'righe': len(data)}
    save_csv('competizioni', comp_report, ['stagione', 'idCompetizioneFcm', 'competizione', 'famiglia', 'verifica'])
    save_csv('copertura', cover_report, list(cover_report[0]))
    save_csv('per_giocatore_squadra_competizione', groups_report, list(groups_report[0]))
    save_csv('sovrapposizioni', overlaps_report,
             ['stagione', 'idGiocatoreFca', 'idSquadraFcm', 'giornataReale',
              'numeroIncontriFantasy', 'numeroCompetizioni', 'famiglieOsservate',
              'incontriFcm', 'competizioni'])
    path = output / (base + '_esempio_giocatore_' + str(code) + '.jsonl.gz')
    with gzip.open(path, 'wt', encoding='utf-8') as stream:
        for row in sample:
            stream.write(json.dumps(row, ensure_ascii=False, sort_keys=True) + '\n')
    files['esempioGiocatore'] = {'name': path.name, 'sha256': sha(path), 'righe': len(sample)}
    if sha(db) != db_hash_before:
        raise RuntimeError('Database cambiato durante audit')
    manifest = {'format': 'RecordsNext4.ContestiAudit.v1', 'stato': 'OK', 'stagioni': len(seasons),
                'righeFormazione': total_rows, 'codiceEsempio': code,
                'righeEsempio': len(sample), 'sovrapposizioni': len(overlaps_report),
                'competizioniDaApprovare': sorted({x[1] for x in all_unclassified}),
                'fonte': str(db), 'sha256Database': db_hash_before,
                'limitazioni': ['Solo righe Formazione esistenti',
                               'Tesserato e una fotografia senza intervalli temporali',
                               'POS non significa contributo effettivo al punteggio',
                               'Famiglia Play Off, Spareggio, Youth League non decisa',
                               'Nessun voto/fantavoto aggregato in questo audit'],
                'files': files}
    manifest_path = output / (base + '_manifest.json')
    manifest_path.write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print('STATO: OK')
    print('STAGIONI:', len(seasons))
    print('RIGHE FORMAZIONE:', total_rows)
    print('SOVRAPPOSIZIONI GIOCATORE-SQUADRA-GIORNATA:', len(overlaps_report))
    print('COMPETIZIONI DA CLASSIFICARE:', ', '.join(manifest['competizioniDaApprovare']))
    print('MANIFEST:', manifest_path)
    return manifest_path


def main():
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument('--database', type=Path, required=True)
    ap.add_argument('--output-dir', type=Path, required=True)
    ap.add_argument('--code', type=int, default=100843)
    args = ap.parse_args()
    run(args.database, args.output_dir, args.code)
    return 0

if __name__ == '__main__':
    try:
        sys.exit(main())
    except Exception as exc:
        print('ERRORE:', exc, file=sys.stderr)
        sys.exit(1)
~~~~

### `tools/Test-RecordsNext4Contesti-v1.py`

SHA-256: `52f436d4257db52fc82e3a8382e41e059fcdadbbabba572977dedfc8b1160256`

~~~~python
#!/usr/bin/env python3
"""Pure regression tests: no databases or worktree mutations."""
from __future__ import annotations
import importlib.util
from pathlib import Path
import unittest

s = Path(__file__).with_name('Audit-RecordsNext4Contesti-v1.py')
spec = importlib.util.spec_from_file_location('rn4_contesti_audit', s)
audit = importlib.util.module_from_spec(spec)
spec.loader.exec_module(audit)

class Contract(unittest.TestCase):
    def test_campionato(self):
        for name in ('Campionato', 'Serie A', 'Serie B', 'Serie C'):
            self.assertEqual(audit.family(name), 'CAMPIONATO')

    def test_coppe(self):
        for name in ('Coppa Serie A', 'Coppa di Lega', 'Supercoppa Serie B',
                     'Europa Pipps', 'Mitropa Cup', 'Coppa Reame Perduto'):
            self.assertEqual(audit.family(name), 'COPPE')

    def test_competizioni_da_approvare(self):
        for name in ('Play Off - Play Out', 'Spareggio Serie B', 'Youth League'):
            self.assertIsNone(audit.family(name))

    def test_no_famiglie_inventate(self):
        self.assertIsNone(audit.family('Coppa nuova da verificare'))

    def test_titolare_iniziale_non_e_giocatore_conteggiato(self):
        self.assertEqual(audit.initial_status(0), 'TITOLARE_INIZIALE')

    def test_panchina_e_fuori_lista_distinti(self):
        self.assertEqual(audit.initial_status(3), 'PANCHINA')
        self.assertEqual(audit.initial_status(-1), 'FUORI_LISTA_FORMAZIONE')

    def test_pos_anomale_non_inventate(self):
        self.assertEqual(audit.initial_status(None), 'POS_DA_VERIFICARE')
        self.assertEqual(audit.initial_status(-7), 'POS_DA_VERIFICARE')

    def test_nomi_tabelle_whitelist(self):
        self.assertEqual(audit.table('2025_2026', 'fcm', 'formazione'),
                         '"raw_2025_2026_fcm_formazione"')
        with self.assertRaises(ValueError):
            audit.table('2025_2026; DROP TABLE x', 'fcm', 'formazione')

    def test_two_competitions_not_one_fact(self):
        # Same real day and team, different competition/match remains two facts.
        facts = [('2025_2026', 366, 8, 4, 32157, 4),
                 ('2025_2026', 366, 8, 5, 32649, 4)]
        self.assertEqual(len(set(facts)), 2)
        self.assertEqual(len({(x[0], x[1], x[5]) for x in facts}), 1)

    def test_three_teams_are_distinct(self):
        facts = {('2025_2026', 366, x, 32157+x) for x in (8, 49, 77)}
        self.assertEqual(len(facts), 3)

if __name__ == '__main__':
    unittest.main(verbosity=2)
~~~~

### `tools/Create-RecordsNext4WorkingCodeMd-v12.py`

SHA-256: `753539f8af84a34e3eee5d4a912eaea97fdd2de8e910d5610ea1d984db100d60`

~~~~python
#!/usr/bin/env python3
"""Additive code bible v12: previous bible SHA256 plus full new versioned files."""
from __future__ import annotations
import argparse
import hashlib
from pathlib import Path
import subprocess
import sys

PREVIOUS = 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v11.md'
DEST = 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v12.md'
NEW_FILES = [
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v13.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v12.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v12.md',
    'docs/SPECIFICA_ARCHIVIO_COMPLETO_GIOCATORE_v1.md',
    'tools/Audit-RecordsNext4Contesti-v1.py',
    'tools/Test-RecordsNext4Contesti-v1.py',
    'tools/Create-RecordsNext4WorkingCodeMd-v12.py',
    'tools/Invoke-RecordsNext4Contesti-v1.py',
]


def sha(path):
    h = hashlib.sha256()
    with Path(path).open('rb') as stream:
        for block in iter(lambda: stream.read(1024*1024), b''):
            h.update(block)
    return h.hexdigest()


def git(project, *args):
    proc = subprocess.run(['git', '-C', str(project), *args],
                          capture_output=True, text=True, encoding='utf-8', errors='replace')
    if proc.returncode:
        raise RuntimeError('Git: ' + proc.stderr.strip())
    return proc.stdout.strip()


def main():
    cli = argparse.ArgumentParser()
    cli.add_argument('--project-dir', type=Path, required=True)
    project = cli.parse_args().project_dir.resolve(strict=True)
    for rel in [PREVIOUS, *NEW_FILES]:
        if not (project / rel).is_file():
            raise RuntimeError('File mancante per bibbia v12: ' + rel)
    lines = [
        '# RecordsNext 4.0 - Bibbia incrementale v12', '',
        'Data: 2026-11-01. Modello completo del calciatore e audit contesti, senza modifiche Java/DB/sito.', '',
        '## Provenienza e continuita', '',
        '- Branch: `' + git(project, 'branch', '--show-current') + '`',
        '- Commit base: `' + git(project, 'rev-parse', 'HEAD') + '`',
        '- Bibbia precedente v11 SHA-256: `' + sha(project/PREVIOUS) + '`',
        '- La presente bibbia INTEGRA, non sostituisce, la v11.', '',
        '## Testo integrale di sorgenti e documenti con impronta', ''
    ]
    for rel in NEW_FILES:
        path = project / rel
        lang = 'python' if rel.endswith('.py') else 'markdown'
        lines.extend(['### `' + rel + '`', '', 'SHA-256: `' + sha(path) + '`', '',
                      '~~~~' + lang, path.read_text(encoding='utf-8-sig').rstrip('\n'),
                      '~~~~', ''])
    blob = ('\n'.join(lines).rstrip() + '\n').encode('utf-8')
    path = project / DEST
    if path.exists():
        if not path.is_file() or path.read_bytes() != blob:
            raise RuntimeError('Bibbia v12 gia presente ma diversa: non sovrascrivo')
        print('BIBBIA v12 gia presente identica:', path)
    else:
        with path.open('xb') as stream:
            stream.write(blob)
        print('BIBBIA v12 generata:', path)
    return 0

if __name__ == '__main__':
    try:
        sys.exit(main())
    except Exception as exc:
        print('ERRORE BIBBIA v12:', exc, file=sys.stderr)
        sys.exit(1)
~~~~

### `tools/Invoke-RecordsNext4Contesti-v1.py`

SHA-256: `677b6047ec712c357c303b58ed3a2cdaf27bb91197b4715bd36bc58e4a2f76ec`

~~~~python
#!/usr/bin/env python3
"""Conservative installation, read-only audit, tests, code bible and diagnostic ZIP."""
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
from zipfile import ZipFile, ZIP_DEFLATED

NEW_FILES = [
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v13.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v12.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v12.md',
    'docs/SPECIFICA_ARCHIVIO_COMPLETO_GIOCATORE_v1.md',
    'tools/Audit-RecordsNext4Contesti-v1.py',
    'tools/Test-RecordsNext4Contesti-v1.py',
    'tools/Create-RecordsNext4WorkingCodeMd-v12.py',
    'tools/Invoke-RecordsNext4Contesti-v1.py',
]
REQUIRED = [
    'pom.xml',
    'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v11.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v12.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v11.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v11.md',
]


def sha(path):
    h = hashlib.sha256()
    with Path(path).open('rb') as stream:
        for chunk in iter(lambda: stream.read(1024*1024), b''):
            h.update(chunk)
    return h.hexdigest()


def git(project, *args):
    proc = subprocess.run(['git', '-C', str(project), *args], capture_output=True,
                          text=True, encoding='utf-8', errors='replace')
    if proc.returncode:
        raise RuntimeError('Git: ' + proc.stderr.strip())
    return proc.stdout.strip()


def invoke(args, env):
    process = subprocess.run([sys.executable, *map(str,args)], capture_output=True,
                             text=True, encoding='utf-8', errors='replace', env=env)
    return process.returncode, process.stdout + process.stderr


def main():
    cli = argparse.ArgumentParser()
    cli.add_argument('--work-dir', type=Path, default=Path('D:/DEV_APPS/RecordsNext4.0'))
    cli.add_argument('--output-dir', type=Path, default=Path('D:/DEV_APPS/downloads'))
    cli.add_argument('--database', type=Path, default=Path('D:/DEV_APPS/RecordsNext2.0/data/database/recordsnext-sandbox.db'))
    opts = cli.parse_args()
    work = opts.work_dir.resolve(strict=True)
    out = opts.output_dir.resolve(strict=True)
    db = opts.database.resolve(strict=True)
    kit = Path(__file__).resolve().parent.parent
    ts = datetime.now().strftime('%Y%m%d_%H%M%S_%f')
    result = out / ('RecordsNext4_Contesti_OUTPUT_v1_' + ts + '.zip')
    audit_dir = out / ('RecordsNext4_ContestiAudit_v1_' + ts)
    report = {'fase':'3C0-contesti-paralleli', 'stato':'ERRORE', 'installati':[],
              'bibbia':'v12', 'nessunCommit':True, 'nessunDeploy':True,
              'auditDir':str(audit_dir), 'databaseModificato':None}
    logs = []
    ok = False
    try:
        for rel in REQUIRED:
            if not (work/rel).is_file():
                raise RuntimeError('Prerequisito mancante: ' + rel)
        gitroot = Path(git(work, 'rev-parse', '--show-toplevel')).resolve(strict=True)
        if os.path.normcase(str(gitroot)).casefold() != os.path.normcase(str(work)).casefold():
            raise RuntimeError('Worktree inatteso: ' + str(gitroot))
        branch = git(work, 'branch', '--show-current')
        if branch != 'feature/recordsnext4-players':
            raise RuntimeError('Branch inatteso: ' + branch)
        report['branch'] = branch
        report['head'] = git(work, 'rev-parse', 'HEAD')
        dbhash = sha(db)
        report['databaseSha256Prima'] = dbhash
        for rel in NEW_FILES:
            src, dest = kit/rel, work/rel
            if not src.is_file():
                raise RuntimeError('File kit mancante: ' + rel)
            if dest.exists() and (not dest.is_file() or sha(src) != sha(dest)):
                raise RuntimeError('File esistente diverso. Non sovrascrivo: ' + rel)
        for rel in NEW_FILES:
            src, dest = kit/rel, work/rel
            if dest.is_file():
                print('GIA PRESENTE IDENTICO:', rel, flush=True)
            else:
                dest.parent.mkdir(parents=True, exist_ok=True)
                with dest.open('xb') as stream:
                    stream.write(src.read_bytes())
                print('INSTALLATO:', rel, flush=True)
                report['installati'].append(rel)
        env = dict(os.environ, PYTHONDONTWRITEBYTECODE='1')
        commands = [
            ('Test-RecordsNext4Contesti-v1.py',
             [work/'tools/Test-RecordsNext4Contesti-v1.py']),
            ('Audit-RecordsNext4Contesti-v1.py',
             [work/'tools/Audit-RecordsNext4Contesti-v1.py', '--database', db,
              '--output-dir', audit_dir, '--code', '100843']),
            ('Create-RecordsNext4WorkingCodeMd-v12.py',
             [work/'tools/Create-RecordsNext4WorkingCodeMd-v12.py', '--project-dir', work]),
        ]
        for label, argv in commands:
            status, output = invoke(argv, env)
            logs.append((label, status, output))
            print(label, 'codice:', status, flush=True)
            if status:
                raise RuntimeError('Esecuzione fallita: ' + label + '\n' + output[-3500:])
        manifests = list(audit_dir.glob('*_manifest.json'))
        if len(manifests) != 1:
            raise RuntimeError('Manifest audit mancante o multiplo')
        audit = json.loads(manifests[0].read_text(encoding='utf-8'))
        if audit.get('stato') != 'OK' or audit.get('righeFormazione') != 552025 or audit.get('stagioni') != 21:
            raise RuntimeError('Audit inatteso: verificare copertura')
        report['audit'] = {'stagioni': audit['stagioni'],
                           'righeFormazione':audit['righeFormazione'],
                           'sovrapposizioni':audit['sovrapposizioni'],
                           'codiceEsempio':audit['codiceEsempio'],
                           'righeEsempio':audit['righeEsempio'],
                           'competizioniDaApprovare':audit['competizioniDaApprovare']}
        report['databaseSha256Dopo'] = sha(db)
        report['databaseModificato'] = (report['databaseSha256Dopo'] != dbhash)
        if report['databaseModificato']:
            raise RuntimeError('SHA256 DB variato')
        report['stato'] = 'OK'
        ok = True
    except Exception:
        logs.append(('ERRORE', 1, traceback.format_exc()))
        print('ERRORE:', logs[-1][2], file=sys.stderr, flush=True)
    finally:
        # Always produce a ZIP, including logs and partial outputs on failure.
        with ZipFile(result, 'x', compression=ZIP_DEFLATED) as z:
            z.writestr('MANIFEST.json', json.dumps(report, ensure_ascii=False, indent=2) + '\n')
            for label, status, content in logs:
                z.writestr('log/' + label.replace('.py', '.txt'),
                           'codice: ' + str(status) + '\n' + content)
            for rel in NEW_FILES:
                p = work/rel
                if p.is_file():
                    z.write(p, rel)
            bible = work/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v12.md'
            if bible.is_file():
                z.write(bible, 'docs/' + bible.name)
            if audit_dir.is_dir():
                for file in sorted(audit_dir.glob('*')):
                    if file.is_file():
                        z.write(file, 'audit/' + file.name)
        print('ESITO:', 'OK' if ok else 'ERRORE', flush=True)
        print('ZIP DA CARICARE:', result, flush=True)
    return 0 if ok else 1

if __name__ == '__main__':
    try:
        sys.exit(main())
    except Exception as exc:
        print('ERRORE FATALE:', exc, file=sys.stderr, flush=True)
        sys.exit(1)
~~~~
