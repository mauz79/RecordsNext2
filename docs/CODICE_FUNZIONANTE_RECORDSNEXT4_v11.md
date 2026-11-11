# RecordsNext 4.0 - Codice funzionante / bibbia incrementale v11

Data: 2026-11-01. Revisione di formule; nessun calcolo FCM/FCA implementato.

## Provenienza

- Branch: `feature/recordsnext4-players`
- HEAD: `f9bf30323d9204d70e91e242bddfc72c2060ecf9`
- SHA-256 bibbia precedente v10: `4164653e4c64b5fb9f9139febce44d71a280f28eaba19b55671b0946f8dd7fff`
- Questo documento INTEGRA la bibbia v10, che rimane conservata.

## Codice e documentazione nuovi, integrali con SHA-256

### `docs/PIANO_RECORDSNEXT4_GIOCATORI_v12.md`

SHA256: `e23f47e096d88376d35f90d618fd304243c26ad99bf9e66b9e7e4e0618dc4b9a`

~~~~markdown
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
~~~~

### `docs/REGISTRO_DECISIONI_RECORDSNEXT4_v11.md`

SHA256: `b0be08800767070f3777c9427ce890975ef4e93820d8b60457c183fee1bfe216`

~~~~markdown
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
~~~~

### `docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v11.md`

SHA256: `b97c8f0cecf58166bef1b0c4406daa4e7980763fd277060697df64dfbb2d5aa9`

~~~~markdown
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
~~~~

### `docs/SPECIFICA_METRICA_VOTO_SPRECATO_GIOCATORE_v2.md`

SHA256: `2d7eaaa853324bf86d2adf37903190f04cc3c046ae345422a12c046aa9f779ab`

~~~~markdown
# RecordsNext 4.0 - Specifica metriche rendimento individuale non utilizzato v2

Data 2026-11-01. Sostituisce soltanto la **formulazione** della specifica v1, che resta conservata.

## Scopo

Per un singolo calciatore appartenente a una fantasquadra, misurare il valore dei voti e fantavoti conseguiti quando non ha contribuito al punteggio fantasy. Non simulare chi si sarebbe dovuto schierare o quante sostituzioni erano possibili.

## Contratto matematico approvato

Per il perimetro `stagione + fantacalciatore storico + fantasquadra + competizione` (ulteriori aggregazioni da approvare):

- `E`: numero totale di partite fantasy valide nelle quali il calciatore era effettivamente disponibile per quella fantasquadra ma e stato lasciato fuori dal punteggio.
- `P`: numero fra quelle `E` partite in cui il calciatore ha ricevuto un voto reale valido.
- `SV`: somma dei **voti reali validi integrali**, per le `P` occasioni.
- `SF`: somma dei **fantavoti validi integrali**, per le stesse `P` occasioni, comprensivi di bonus/malus realmente applicabili.

| Metrica | Formula | Quando definita |
|---|---|---|
| Media voto sprecato | `SV / P` | `P > 0` |
| Errore pesato sul voto | `SV / E` | `E > 0` |
| Fantamedia sprecata | `SF / P` | `P > 0` |
| Errore pesato sul fantavoto | `SF / E` | `E > 0` |
| Quota esclusioni con voto | `P / E` | `E > 0` |

Le medie quando `P=0` sono `null` / non definite. Se `E>0, P=0`, i due errori pesati valgono 0. Se `E=0`, **tutti** gli indicatori sono non definiti. Le esclusioni senza voto non generano valori inventati ma contano nel denominatore `E`. Non usare `SV-6*P`, non tagliare i voti a 6, non imporre cap numerici.

Equivalenza di controllo: `pesatoVoto = mediaVoto * P/E` e `pesatoFantavoto = fantamediaSprecata * P/E`, se `P>0`.

## Esempi

- Tre esclusioni con voto `5.5, 7, 8`: `SV=20.5`, `P=E=3`, media voto=6.833333..., errore pesato=6.833333...
- Cinque esclusioni, ma solo tre con voto `5.5, 7, 8`: `SV=20.5`, `P=3`, `E=5`, media voto=6.833333..., errore pesato=4.1.
- Esempio **solo illustrativo** di fantavoti per gli stessi tre voti: `5.5, 10, 7.5`, ottenuti con nessun evento, `gol +3`, `ammonizione -0.5` rispettivamente. `SF=23`, fantamedia sprecata=7.666666..., errore pesato su 5 esclusioni=4.6. **Non dedurre** da questi numeri coefficienti universali di FCM.
- `E=23`, `P=3`, `SV=18`: media voto=6 e errore pesato=18/23=0.782608...

## Granularita e fonti da verificare prima dell'implementazione sui dati

Una riga per esclusione verificata, con: identificativo storico calciatore, ID FCA della stagione, stagione, fantasquadra (e prova di tesseramento in quel momento), competizione, incontro fantasy, giornata reale, voto originale fonte Gazzetta, fantavoto completo e relativo metodo/fonte di calcolo, dettaglio eventi con bonus/malus, stato voto valido/SV, classificazione fuori punteggio e tabellino/scorecard URL.

Il fantavoto di un giocatore lasciato fuori **non e necessariamente** il totale applicato al punteggio squadra: prima verificare da quali tabelle sorgente si possa ricostruire per tutte le esclusioni e secondo le regole della stagione/competizione. Non trasformare automaticamente `Tabellino.Tot` in fantavoto universale senza verifica.

Una prestazione reale FCA deve esistere una sola volta per calciatore+stagione+giornata; piu esclusioni in competizioni fantasy parallele rimangono fatti fantasy distinti. Prima di produrre una classifica all-time scegliere esplicitamente il perimetro di aggregazione.

Riserve d'ufficio (RU), codici artificiali `0`/`-1`, voti d'ufficio senza prestazione reale e SV non vanno confusi con un voto effettivo del calciatore. I riposi e gli incontri non disputati non diventano automaticamente esclusioni. Non simulare le sostituzioni (che possono essere 3, 5, 8, 11 secondo le impostazioni FCM).

## Questioni ancora aperte

1. Definizione verificabile di `lasciato fuori` nei casi panchina non utilizzata, giocatore non inserito in `Formazione`, `POS=-1` e anomalia dei flag di entrata.
2. Fonte e regole esatte dei fantavoti per un giocatore fuori dagli undici, inclusi bonus/malus diversi per stagione/competizione.
3. Criterio di voto reale valido vs SV e speciali, rinvii e partite recuperate; verifica su campioni FCM/FCA.
4. Perimetro di classifiche multi-competizione e stagionali/all-time: evitare doppi conteggi senza eliminare fatti fantasy.
5. Facoltativo: massimo voto/fantavoto di una singola esclusione; nessun cap imposto.

## Stato implementativo

Contratto matematico approvato e testato **solo su dati sintetici**. Nessun record storico reale calcolato, nessuna modifica a Java/SQLite/GUI/sito.
~~~~

### `tools/Test-RecordsNext4MetricheIndividuali-v2.py`

SHA256: `76661d529bc984f1a7a59e598a9b8c983f3e99fc3557b73111e99c4678722721`

~~~~python
#!/usr/bin/env python3
"""Test sintetici delle quattro metriche; non interpreta dati FCM/FCA."""
from __future__ import annotations
from dataclasses import dataclass
from decimal import Decimal
import unittest


@dataclass(frozen=True)
class Metriche:
    esclusioni: int
    con_voto: int
    somma_voti: Decimal
    somma_fantavoti: Decimal
    media_voto: Decimal | None
    errore_pesato_voto: Decimal | None
    fantamedia: Decimal | None
    errore_pesato_fantavoto: Decimal | None
    quota_con_voto: Decimal | None


def calcola(esclusioni: int, voti: list[Decimal], fantavoti: list[Decimal]) -> Metriche:
    if isinstance(esclusioni, bool) or not isinstance(esclusioni, int) or esclusioni < 0:
        raise ValueError('esclusioni invalide')
    if len(voti) != len(fantavoti):
        raise ValueError('voti e fantavoti devono avere gli stessi eventi validi')
    p = len(voti)
    if p > esclusioni:
        raise ValueError('prestazioni valide superiori alle esclusioni')
    if any(not isinstance(v, Decimal) or not v.is_finite() for v in voti + fantavoti):
        raise ValueError('richiesti Decimal finiti (niente float/NaN/Infinity)')
    sv = sum(voti, Decimal(0))
    sf = sum(fantavoti, Decimal(0))
    e = Decimal(esclusioni)
    pd = Decimal(p)
    return Metriche(esclusioni, p, sv, sf,
                   sv / pd if p else None,
                   sv / e if esclusioni else None,
                   sf / pd if p else None,
                   sf / e if esclusioni else None,
                   pd / e if esclusioni else None)


class ContrattoMetriche(unittest.TestCase):
    def test_tre_esclusioni_tre_voti(self):
        x = calcola(3, [Decimal('5.5'), Decimal('7'), Decimal('8')],
                    [Decimal('5.5'), Decimal('10'), Decimal('7.5')])
        self.assertEqual(x.media_voto, Decimal('20.5') / 3)
        self.assertEqual(x.errore_pesato_voto, Decimal('20.5') / 3)
        self.assertEqual(x.fantamedia, Decimal('23') / 3)
        self.assertEqual(x.errore_pesato_fantavoto, Decimal('23') / 3)
        self.assertEqual(x.quota_con_voto, Decimal('1'))

    def test_cinque_esclusioni_tre_voti(self):
        x = calcola(5, [Decimal('5.5'), Decimal('7'), Decimal('8')],
                    [Decimal('5.5'), Decimal('10'), Decimal('7.5')])
        self.assertEqual(x.media_voto, Decimal('20.5') / 3)
        self.assertEqual(x.errore_pesato_voto, Decimal('4.1'))
        self.assertEqual(x.fantamedia, Decimal('23') / 3)
        self.assertEqual(x.errore_pesato_fantavoto, Decimal('4.6'))
        self.assertEqual(x.quota_con_voto, Decimal('0.6'))

    def test_ventitre_esclusioni_tre_6(self):
        x = calcola(23, [Decimal('6')] * 3, [Decimal('6')] * 3)
        self.assertEqual(x.media_voto, Decimal('6'))
        self.assertEqual(x.errore_pesato_voto, Decimal('18') / 23)

    def test_sette_integrale_non_sopra_sei(self):
        x = calcola(1, [Decimal('7')], [Decimal('7')])
        self.assertEqual(x.media_voto, Decimal('7'))
        self.assertEqual(x.errore_pesato_voto, Decimal('7'))

    def test_solo_esclusioni_senza_voto(self):
        x = calcola(4, [], [])
        self.assertIsNone(x.media_voto)
        self.assertIsNone(x.fantamedia)
        self.assertEqual(x.errore_pesato_voto, Decimal('0'))
        self.assertEqual(x.errore_pesato_fantavoto, Decimal('0'))
        self.assertEqual(x.quota_con_voto, Decimal('0'))

    def test_nessuna_esclusione(self):
        x = calcola(0, [], [])
        self.assertIsNone(x.media_voto)
        self.assertIsNone(x.errore_pesato_voto)
        self.assertIsNone(x.fantamedia)
        self.assertIsNone(x.errore_pesato_fantavoto)
        self.assertIsNone(x.quota_con_voto)

    def test_malus_puo_ridurre_fantavoto(self):
        x = calcola(1, [Decimal('7')], [Decimal('6.5')])
        self.assertEqual(x.fantamedia, Decimal('6.5'))

    def test_restrizioni_input(self):
        for args in [(-1, [], []), (True, [], []), (1, [Decimal('7')], []),
                     (1, [Decimal('7')]*2, [Decimal('7')]*2),
                     (1, [Decimal('NaN')], [Decimal('7')]),
                     (1, [7.0], [Decimal('7')])]:
            with self.subTest(args=args), self.assertRaises(ValueError):
                calcola(*args)


if __name__ == '__main__':
    unittest.main(verbosity=2)
~~~~

### `tools/Create-RecordsNext4WorkingCodeMd-v11.py`

SHA256: `598f7c1b1157967d84773676645ec784a23c8df0fcbc36d82b924893add29227`

~~~~python
#!/usr/bin/env python3
"""Bibbia incrementale v11: precedente v10 + testo completo dei file di questa revisione."""
from __future__ import annotations
import argparse
import hashlib
from pathlib import Path
import subprocess
import sys

PREVIOUS = 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v10.md'
DEST = 'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v11.md'
NEW_FILES = [
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v12.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v11.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v11.md',
    'docs/SPECIFICA_METRICA_VOTO_SPRECATO_GIOCATORE_v2.md',
    'tools/Test-RecordsNext4MetricheIndividuali-v2.py',
    'tools/Create-RecordsNext4WorkingCodeMd-v11.py',
    'tools/Invoke-RecordsNext4Metriche-v1.py',
]


def sha(path: Path) -> str:
    h=hashlib.sha256()
    with path.open('rb') as f:
        for block in iter(lambda: f.read(1024 * 1024), b''):
            h.update(block)
    return h.hexdigest()


def git(project: Path, *args: str) -> str:
    p=subprocess.run(['git','-C',str(project),*args],capture_output=True,text=True,encoding='utf8',errors='replace')
    if p.returncode:
        raise RuntimeError('Git fallito: ' + p.stderr.strip())
    return p.stdout.strip()


def main() -> int:
    parser=argparse.ArgumentParser()
    parser.add_argument('--project-dir',type=Path,required=True)
    pr=parser.parse_args().project_dir.resolve(strict=True)
    for rel in [PREVIOUS,*NEW_FILES]:
        if not (pr/rel).is_file():
            raise RuntimeError('File richiesto mancante: '+rel)
    lines=['# RecordsNext 4.0 - Codice funzionante / bibbia incrementale v11','',
           'Data: 2026-11-01. Revisione di formule; nessun calcolo FCM/FCA implementato.','',
           '## Provenienza','',
           '- Branch: `'+git(pr,'branch','--show-current')+'`',
           '- HEAD: `'+git(pr,'rev-parse','HEAD')+'`',
           '- SHA-256 bibbia precedente v10: `'+sha(pr/PREVIOUS)+'`',
           '- Questo documento INTEGRA la bibbia v10, che rimane conservata.',
           '', '## Codice e documentazione nuovi, integrali con SHA-256','']
    for rel in NEW_FILES:
        path=pr/rel
        lang='python' if rel.endswith('.py') else 'markdown'
        lines.extend(['### `'+rel+'`','', 'SHA256: `'+sha(path)+'`','',
                      '~~~~'+lang,path.read_text(encoding='utf-8-sig').rstrip('\n'),'~~~~',''])
    content=('\n'.join(lines).rstrip()+'\n').encode('utf-8')
    dest=pr/DEST
    if dest.exists():
        if not dest.is_file() or dest.read_bytes()!=content:
            raise RuntimeError('Bibbia v11 gia presente ma differente: non sovrascrivo')
        print('BIBBIA v11 gia presente identica:',dest)
    else:
        with dest.open('xb') as f: f.write(content)
        print('BIBBIA v11 generata:',dest)
    return 0


if __name__=='__main__':
    try:
        sys.exit(main())
    except Exception as exc:
        print('ERRORE BIBBIA:',str(exc),file=sys.stderr)
        sys.exit(1)
~~~~

### `tools/Invoke-RecordsNext4Metriche-v1.py`

SHA256: `81e90fc97f97d4b861de5577c8c55b3e8db6d7bfd60aff6c8d289ce5df110a79`

~~~~python
#!/usr/bin/env python3
"""Installazione additiva revisione metriche, test e bibbia v11. Non apre DB."""
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

FILES = [
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v12.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v11.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v11.md',
    'docs/SPECIFICA_METRICA_VOTO_SPRECATO_GIOCATORE_v2.md',
    'tools/Test-RecordsNext4MetricheIndividuali-v2.py',
    'tools/Create-RecordsNext4WorkingCodeMd-v11.py',
    'tools/Invoke-RecordsNext4Metriche-v1.py',
]
REQUIRED = [
    'pom.xml',
    'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v10.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v10.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v11.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v10.md',
]


def sha(path: Path) -> str:
    h=hashlib.sha256()
    with path.open('rb') as f:
        for chunk in iter(lambda: f.read(1024 * 1024), b''):
            h.update(chunk)
    return h.hexdigest()


def git(project: Path, *args: str) -> str:
    p=subprocess.run(['git','-C',str(project),*args],capture_output=True,text=True,encoding='utf-8',errors='replace')
    if p.returncode:
        raise RuntimeError('Git fallito: '+ ' '.join(args) + ': ' + p.stderr.strip())
    return p.stdout.strip()


def main() -> int:
    parser=argparse.ArgumentParser()
    parser.add_argument('--work-dir', type=Path, default=Path('D:/DEV_APPS/RecordsNext4.0'))
    parser.add_argument('--output-dir', type=Path, default=Path('D:/DEV_APPS/downloads'))
    args=parser.parse_args()
    work=args.work_dir.resolve(strict=True)
    output=args.output_dir.resolve(strict=True)
    kit=Path(__file__).resolve().parent.parent
    stamp=datetime.now().strftime('%Y%m%d_%H%M%S_%f')
    package=output/('RecordsNext4_Metriche_OUTPUT_v1_'+stamp+'.zip')
    report={'fase':'revisione-metriche-v2','stato':'ERRORE','installati':[],
            'bibbia':'v11','nessunDeploy':True,'nessunCommit':True}
    logs=[]
    ok=False
    try:
        for rel in REQUIRED:
            if not (work/rel).is_file():
                raise RuntimeError('File prerequisito mancante: '+rel)
        root=Path(git(work,'rev-parse','--show-toplevel')).resolve()
        if os.path.normcase(str(root))!=os.path.normcase(str(work)):
            raise RuntimeError('Worktree non corrispondente: '+str(root))
        branch=git(work,'branch','--show-current')
        if branch!='feature/recordsnext4-players':
            raise RuntimeError('Branch inatteso: '+branch)
        report['branch']=branch
        report['head']=git(work,'rev-parse','HEAD')
        for rel in FILES:
            src,dst=kit/rel,work/rel
            if not src.is_file():
                raise RuntimeError('File kit assente: '+rel)
            if dst.exists() and (not dst.is_file() or sha(src)!=sha(dst)):
                raise RuntimeError('File gia esistente differente; non sovrascrivo: '+rel)
        for rel in FILES:
            src,dst=kit/rel,work/rel
            if dst.is_file():
                print('GIA PRESENTE IDENTICO:',rel,flush=True)
                continue
            dst.parent.mkdir(parents=True,exist_ok=True)
            with dst.open('xb') as f: f.write(src.read_bytes())
            report['installati'].append(rel)
            print('INSTALLATO:',rel,flush=True)
        env=dict(os.environ,PYTHONDONTWRITEBYTECODE='1')
        for script,argv in [
            ('tools/Test-RecordsNext4MetricheIndividuali-v2.py',[]),
            ('tools/Create-RecordsNext4WorkingCodeMd-v11.py',['--project-dir',str(work)])
        ]:
            p=subprocess.run([sys.executable,str(work/script),*argv],capture_output=True,
                             text=True,encoding='utf-8',errors='replace',env=env)
            logs.append((script,p.returncode,p.stdout+p.stderr))
            print(script,'codice:',p.returncode,flush=True)
            if p.returncode:
                raise RuntimeError('Errore script: '+script)
        report['stato']='OK'; ok=True
    except Exception:
        logs.append(('ERRORE',1,traceback.format_exc()))
        print('ERRORE:',logs[-1][2],file=sys.stderr,flush=True)
    finally:
        with ZipFile(package,'x',compression=ZIP_DEFLATED) as z:
            z.writestr('MANIFEST.json',json.dumps(report,indent=2,ensure_ascii=False)+'\n')
            for script,code,log in logs:
                z.writestr('log/'+Path(script).name.replace('.py','.txt'),log)
            for rel in FILES:
                target=work/rel
                if target.is_file(): z.write(target,rel)
            bible=work/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v11.md'
            if bible.is_file(): z.write(bible,'docs/'+bible.name)
        print('ESITO:', 'OK' if ok else 'ERRORE',flush=True)
        print('ZIP DA CARICARE:',package,flush=True)
    return 0 if ok else 1


if __name__=='__main__':
    try:
        sys.exit(main())
    except Exception as exc:
        print('ERRORE FATALE:',str(exc),file=sys.stderr,flush=True)
        sys.exit(1)
~~~~
