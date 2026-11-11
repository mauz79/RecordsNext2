# RecordsNext 4.0 - Registro decisioni v18

# RecordsNext 4.0 - Registro decisioni v16

| ID | Stato | Decisione / evidenza |
|---|---|---|
| D-124 | IMPLEMENTATA E VERIFICATA | L'adattatore FCA Top & Flop usa JDBC SQLite e le **stesse tabelle raw importate dalla 3.2**, senza copie o nuove pipeline. |
| D-125 | VERIFICATA SU FONTI | `recordsnext.db`: 617310 GiocaIn e 16245 GiocatoreA stagionali; `recordsnext-sandbox.db`: 616018 e 16211. Differenza tutta nel 2026/27: usare test di consistenza dinamica. |
| D-126 | REGOLA TECNICA | Conservare nei fatti la distinzione `GiocatoreA.ID` locale vs `CodiceGazza` storico, senza attribuire identita canoniche nei casi dubbi. |
| D-127 | REGOLA TECNICA | Un record `GiocaIn` non prova da solo una presenza reale: interpretare `VOTO1>0` come voto valido, valori negativi come senza voto e zero/NULL come sconosciuti; preservare i valori originali. |
| D-128 | IMPLEMENTATA | Conservare integralmente campi GiocatoreA/GiocaIn/Punteggio nel campo diagnostico `sourceFields`, con marcatore esplicito per SQL NULL; nessuna interpretazione dei coefficienti bonus/malus prima della fonte FCM. |
| D-129 | APERTA | Completare la lettura di Statistiche/Storico/IncontroInA e gli eventi FCM; definire il modello temporale dei tesseramenti e la validita degli esclusi. |
| D-130 | REGOLA DI PROTEZIONE | La 3.2 operativa, il DB, la GUI, la selezione catalogo, i publisher e i siti non si modificano in questa fase. |


---

# RecordsNext 4.0 - Registro decisioni v15

Le decisioni D-001--D-112 sono preservate integralmente dalla v14. Nuove D-113--D-123:

| ID | Stato | Decisione / evidenza |
|---|---|---|
| D-113 | IMPLEMENTATA: MODELLO | Aggiungere la famiglia `app/topflop` al codice Java esistente, senza modificare le classi 3.2 ne attivare prematuramente `RecordFamily.TOP_FLOP`. |
| D-114 | APPROVATA/IMPLEMENTATA | Prestazione reale unica per stagione+ID FCA+giornata Serie A. Eventi fantasy separati per stagione+competizione FCM+incontro+fantasquadra+ID FCA. |
| D-115 | APPROVATA/IMPLEMENTATA | Conservare `CodiceGazza` storico e ID FCA stagionale distinti; non fondere automaticamente collisioni storiche. |
| D-116 | APPROVATA/IMPLEMENTATA | `Tesserato` e una fotografia della rosa, non dimostra tesseramento in una giornata non documentata. Nessuna esclusione inferita dal silenzio FCM. |
| D-117 | APPROVATA/IMPLEMENTATA | Youth League esclusa categoricamente; Play Off - Play Out e Spareggio Serie B = CAMPIONATO; 24 nomi ammessi codificati esplicitamente, altri nomi in revisione obbligatoria. |
| D-118 | APPROVATA/IMPLEMENTATA | Dati per titolari, conteggiati, panchinari e fuori lista; stato `EXCLUDED_CONFIRMED` utilizzabile solo dopo prova sorgente. |
| D-119 | APPROVATA/IMPLEMENTATA | Quattro metriche personali calcolate da voti/fantavoti integrali, `SV/P`, `SV/E`, `SF/P`, `SF/E`. Sulle fonti incomplete mantenere separati contatori voto/fantavoto, non inventare valori. |
| D-120 | APPROVATA/IMPLEMENTATA | Non sintetizzare mai URL tabellini: copiare dal normalizzato/link canonico 3.2; conservare ID incontro e giornata distinti. |
| D-121 | IMPLEMENTATA: TEST | Test Java standard con 52 assert e wrapper JUnit 5; compilazione completa codice Java 3.2+nuovo modulo verificata localmente con JDK 21, non suite Maven completa. |
| D-122 | REGOLA OPERATIVA | Il runner deve verificare JDK 21, git branch, file preesistenti e collisioni prima dell'installazione; output diagnostico sotto `D:\DEV_APPS\downloads`, mai `%TEMP%`; niente sovrascritture. |
| D-123 | APERTA | Realizzazione estrattore JDBC, identificazione esclusioni provate, ambito tesseramento, classifiche e soglie, integrazione piena RecordFamily/pipeline/exporter/GUI. Non segnare come implementati in v15. |

---

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


## Decisioni D-131--D-139 - Collegamento JDBC FCM v1

| ID | Stato | Decisione / verifica |
|---|---|---|
| D-131 | APPROVATA DALL'UTENTE | Il modulo e' la famiglia nativa Records Top & Flop, estesa dalla 3.2, non una pipeline separata. |
| D-132 | IMPLEMENTATA E VERIFICATA | Il lettore FCM usa le relazioni originali Formazione/Incontro/Girone/Competizione/Tabellino e ID FCA della medesima stagione. |
| D-133 | IMPLEMENTATA E VERIFICATA | Competizioni parallele e piu fantasquadre non sono aggregate: una riga per giocatore/incontro/team; 552025 contesti originari. |
| D-134 | APPROVATA DALL'UTENTE | Youth League esclusa dai dati utilizzabili per record/schede; 5250 righe sono comunque contate nell'audit. Play Off e Spareggio Serie B restano CAMPIONATO. |
| D-135 | REGOLA TECNICA | `IDGIORNATA` FCM resta chiave dell'archivio, distinta da `GIORNATADIA`; non dedurre un ordinale e non costruire URL arbitrari. |
| D-136 | REGOLA TECNICA | Il posto nei primi undici slot del Tabellino non certifica da solo la prestazione legalmente conteggiata; ENTRATO/ENTRATORISERVA restano campi fonte distinti. |
| D-137 | REGOLA TECNICA | Tesserato FCM e' snapshot, non archivio cronologico; non inventare esclusioni dove manca una riga Formazione. |
| D-138 | IMPLEMENTATA | Fonte JSON/JS pubblica non ancora generata: lettore Java FCM in streaming e test JDBC senza modifica a DB, pipeline o GUI. |
| D-139 | APERTA | Integrare nella stessa istanza `TopFlopArchive` FCA+FCM, provenienza del fantavoto per esclusi, regole voti speciali e URL tabellini con normalizzatore 3.2. |


## Decisioni D-140--D-147 - Assemblaggio nativo per stagione

| ID | Stato | Decisione / verifica |
|---|---|---|
| D-140 | IMPLEMENTATA IN FASE 4A | Assemblare fatti FCA e FCM nella stessa istanza `TopFlopArchive`, partendo da FCA e collegando ogni formazione mediante ID locale e giornata reali. |
| D-141 | VERIFICATA SU FONTE | `Tesserato` presenta righe ripetute per stessa fantasquadra/giocatore: vendite e successivi riacquisti possibili. Chiave distinta dell'evento: `Tesserato.ID` per stagione; non usare solo giocatore+squadra per conservare la cronologia sorgente. |
| D-142 | IMPLEMENTATA IN FASE 4A | `LoadedSeason.rosterEvents` conserva tutte le righe originali e i rispettivi ID; `TopFlopArchive.rosters` rimane vista sintetica di membership. Non inferire intervalli di appartenenza. |
| D-143 | IMPLEMENTATA IN FASE 4A | I token di voto/fantavoto dei tabellini FCM restano nei campi sorgente; convertire in BigDecimal solo i numerici con virgola/punto, senza convertire `sv/ng` in zero. |
| D-144 | REGOLA CONSERVATIVA | Gli slot del tabellino registrano evidenza ma non certificano utilizzo legalmente conteggiato: non produrre automaticamente `EXCLUDED_CONFIRMED` e non gonfiare i record. |
| D-145 | REGOLA CONSERVATIVA | `scorecardUrl` rimane null durante l'assemblaggio JDBC; risolverlo esclusivamente con le logiche normalizzate 3.2, al momento dell'integrazione nella pipeline. |
| D-146 | DA VERIFICARE | La provenienza della fantamedia per giocatori fuori da Formazione, l'esclusione certa durante un tesseramento storico e le regole RU/sostituzione sono ancora aperte. |
| D-147 | REGOLA DI REGRESSIONE | La nuova classe e i test non modificano pipeline, catalogo, GUI, sito o DB; testare gli hash e mantenere separati dati reali e contesti fantasy. |


## D-148--D-157 - Conferme 2026/27 e cronologia mercato

| ID | Stato | Regola o evidenza |
|---|---|---|
| D-148 | CONFERMATA DALL'UTENTE | 9 giornate reali di Serie A disponibili nello snapshot 2026/27. |
| D-149 | CONFERMATA DALL'UTENTE | La AlterLega 2026/27 ha cominciato dalla giornata reale 3; assenza di incontri nelle giornate 1 e 2 NON e' errore. |
| D-150 | IPOTESI DELL'UTENTE - DA VERIFICARE | La cronologia dei tesseramenti potrebbe essere legata a giornate della Serie A vera. Non assumere che `ULTIMAGIORNATA` sia la prima giornata eleggibile. |
| D-151 | FONTE VERIFICATA | `Tesserato` non contiene date di validita'. `EntryInBilancio` contiene `DATATRANSAZIONE` e `ULTIMAGIORNATA` e deve restare distinto dalla membership. |
| D-152 | CONFERMATA DALL'UTENTE | Calciatore ceduto e riacquistato in un'asta successiva e' operazione normale; riacquisti nella stessa sessione possono essere correzioni. Le sole date non provano la sessione. |
| D-153 | CONFERMATA DALL'UTENTE | Varela/FortisSIMY 2026/27 e' una correzione da svincolo accidentale; preservare tesseramenti 699 e 752 e relativi movimenti contabili. |
| D-154 | VERIFICATA: SNAPSHOT IMPORTATO 3.2 | 2026/27 aggiornato: 28956 righe reali FCA, 7650 `Formazione`, 813 `Tesserato`, 876 `EntryInBilancio`; 3..9 giornate reali con formazioni fantasy. |
| D-155 | REGOLA IMPLEMENTATIVA | Il nuovo assemblatore conserva tutte le righe `Tesserato` con ID sorgente e usa vista membership sintetica; nessuna esclusione senza prova di tesseramento in quel giorno. |
| D-156 | TEST AGGIUNTIVO | Verifica specifica sul DB 2026/27 aggiornato, compreso Varela e l'inizio della fantasy alla giornata 3. I dati di output test non sono regole eterne per altre stagioni. |
| D-157 | RIMANE APERTO | Non integrare automaticamente la datazione del possesso e il dettaglio `EntryInBilancio` in record pubblico fino a chiarimento dei campi originali FCM. |
