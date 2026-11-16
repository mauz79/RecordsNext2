## RecordsNext 4.0 - v63: sandbox multisito certificata, frontend statico integrato (RC, NON release)
- 21 siti in sandbox con 3730 JS e 1617 shard verificati; 339 JS staging, cutoff storico e vincolo 1.500.000 byte OK.
- Skin v53b integrata e verificata staticamente per 2025/26 e 2026/27.
- Pubblicazione reale, interazioni browser e sostituzione 3.2 ancora da certificare.
- Documentazione versionata: Bibbia v46, Diario v46, Registro decisioni v39, rapporto v63.

## RecordsNext 4.0 - v61: continuita rilascio TEST (non pubblicato)
- v58c: build, test e 87/87 record completati; 339 JS staging e gate v59 superato.
- Sandbox v60b: isolamento 21 destinazioni, 2005/06 esclusa per assenza FCM.
- Audit v60b bloccato da flag pubblicazione attivo; distribuzione, skin e promozione 3.2 da certificare.
- Documentazione: Bibbia v45, Diario v45, Registro decisioni v38, verifica rilascio v60.

## 4.0 v58c TEST - FMT ufficiale FCA e uscita dalla fase di audit
- RealFantasy usa Statistiche.FMT/PRESENZE con verifica voti raw, senza coefficienti inventati.
- Fonte, campione, precisione e copertura espliciti; fallback solo con regolamento verificato.
- Test planner a sei famiglie, build Maven e backup eseguiti. Staging/sandbox e rilascio da completare.

# Audit FMT 2012/13 v57 - 2026-11-15

- Confrontati 816 giocatori nel SQLite TEST.
- Differenze oltre soglia FMT 180; coerenti con G1 180; residue 0.
- Nessun dato sorgente, JS, DB o Java modificato. Vedi rapporto v57.

---

# RecordsNext 4.0 TEST â€” verifica dati post-v55 (2026-11-15 21:39:34)



- Fonte FCA 2012/13 corretta e importata (`rn_import=59`), voti validi **9.681 -> 9.936** (+255 nella G1).

- Elaborazione Completa eseguita: 87/87 record, 317 file JS verificati, pubblicati=0.

- Controllo sullo staging appena generato raw FCA/JS Careers: 9.936/9.936, 0 differenze. La vecchia publish-sandbox-v52 non e stata aggiornata (pubblicati=0).

- Presenze: `Statistiche` 0 difformita; `Storico` 235 difformita, e FMT/FANTAMEDIA1 180 da approfondire.

- Documentazione v42/v35; sorgenti Java, FCM, FCA originali, SQLite e file pubblicati NON modificati durante il presente audit.

- Audit conservati in `D:\DEV_APPS\RecordsNext4.0_TEST_v37\reports\audit-fonti-v56-post-fca2012-staging-v2`; nessuna certificazione completa dei calcoli Top & Flop.



---



# RecordsNext 4.0 v49 â€” 12 novembre 2026 (copia TEST)



- GUI Top & Flop: 11 soglie numeriche accessibili nella larghezza della finestra, senza barra di scorrimento orizzontale; salvataggio in `config/topflop.json` invariato.

- Rimosso l'avviso automatico di riconciliazione identita' ad ogni riavvio; accesso manuale immutato.

- Nuovo audit tribuna `TopFlopTribunaAuditV49` e contatori `contatori.auditTribuna` per stagione nella facade. Le assenze da formazione con solo tesseramento stagionale sono separate dalle presenze in un'altra formazione nello stesso turno. Nessuna presunzione di data di trasferimento, nessuna contaminazione di TF11-TF13.

- Smoke headless GUI e audit tribuna; compilazione Java 21. Staging e publish reali ancora da certificare prima di release.



---



# RecordsNext 4.0 v48 - 12 novembre 2026 (TEST)



- TF11, TF12 e TF13 attivati con soglie configurabili per le sole riserve FCM documentate, non conteggiate e presenti nella formazione del match giocato: senza deduzioni di possesso continuo, nessun dato assente inventato.

- Quattro punteggi: media voto reale, errore pesato voto, media fantavoto numerico da tabellino, errore pesato fantavoto. Separazione per fantasquadra, competizione, stagione e famiglia; denominatori e copertura pubblicati.

- Nuova vista `Bench` caricabile a richiesta, record e highlights con prove partita, supporto publisher multisito e decoder V2; limite file JS 1.500.000 byte confermato.

- Etichette TF14/TF15 definitive aggiornate; TF11â€“TF13 selezionabili. 3.2 e v47 RealFantasy/Economy non alterate.

- Java21 compilato; smoke v48 con casi di scarto e soglie; audit fonte FCM su snapshot. Resta obbligatorio il collaudo v48 sul Windows reale.



---



# RecordsNext 4.0 v47 - 12 novembre 2026 (solo TEST)



- Confronto storico TF14/TF15 su identita' approvate, baseline ponderata leave-one-season-out, soglie locali e cutoff 3.2 per sito.

- Nuove viste `Economy` per contratti FCM, costi acquisto/cessione, null vs zero, outlier costo>crediti totali, medie soltanto su prezzi positivi; nessun indice inventato.

- Calcolatore indipendente `RealFantasy` condizionato a regolamenti per stagione documentati e verificati, nessun fantavoto desunto da `FCM.Tabellino.TOT` e nessun doppio conteggio rigori.

- Decoder browser v2 e regressione browser su JS storici v46; invariati nome facade e compatibilita' decoder v1.

- Java 21 e smoke test su TF14/15, costi, condizioni dei regolamenti, identita'/collisioni e sharding v46.

- Attivazione UI TF14/15; TF11-TF13 non attivi per mancanza di prova temporale. Indice economico interstagionale ancora non attivo.

- Stato: richiede collaudo intero dalla GUI Windows e successivo audit JS/publish multisito.



---



# Modifiche di sviluppo 4.0 â€” v46 (11 novembre 2026, non pubblicata)



- Corretto `RecordsNextPipeline` in modalita generazione senza pubblicare: shard flat 3.2 prima del limite fisico JS.

- Validazione finale di tutti i JS anche senza la famiglia Top & Flop.

- Limite rigoroso **1.500.000 byte** applicato a facade, shard e altri JS, anche nel multisito; il precedente valore 1536 KiB non e' la soglia 4.0.

- Manifest staging riallineato ai file effettivi; conteggio output corretto.

- Regressione con Classics cumulativo di oltre 9 MB, verificati 20 shard (20 stagioni) sotto il limite.

- Documentazione 4.0 versionata v38, README e INSTALL aggiornati; nessuna modifica ai calcoli dei Classici o al motore storico squadre 3.2.

- Stato: build Java 21 testata; esecuzione integrale sulla copia Windows da confermare prima del push/release.



---



# Changelog



## RecordsNext 3.2.0 - 2026-09-16



### Catalogo modulare e preflight



- completato il catalogo modulare RecordsNext: 94 record/figli complessivi, di cui 93 configurabili e Culometro opzionale;

- completata la corrispondenza tra controlli GUI e viste generate;

- dipendenze valutate a livello di singolo record;

- corretta la capability `modifier.home-field` nel preflight;

- `modifiers.home-field-deciding` viene ora eseguito quando il Fattore Campo e selezionato.



### Culometro



- aggiunto il componente `HOME_FIELD_DECISIVE`;

- integrati gli eventi derivati da `fattoreCampoDecisivo`;

- aggiunti frequenza storica dell'evento, frequenza storica della configurazione, impatto e contributo;

- verifica reale: `homeFieldCandidateCount=1304` e 2608 occorrenze `HOME_FIELD_DECISIVE`.



### Verifiche



- contratto checkbox/vista: 96 controlli verificati, 0 problemi;

- audit semantici Classici, Serie, RU, Modificatori, Soglie/Fortuna e Culometro superati;

- suite Maven: 57 test, 0 failure, 0 errori, 0 skipped;

- collaudo operativo finale in modalita Completa: 72 selezionati, 72 eseguibili, 72 completi, 0 parziali, 0 saltati;

- storico `2006_2007-2025_2026` riconosciuto invariato;

- stagione corrente `2026_2027` reimportata e normalizzata;

- generazione finale: 9 file validi, 0 pubblicati;

- consolidamento aggiornato correttamente.



### Packaging 3.2.0



- `RecordsNext_3.2.0_FULL.zip` - SHA256 `BFE32FBBA14A5E45E3EA110C31A6C207F0E82048162B7572610D6E5A0A38AD71`;

- `RecordsNext_3.2.0_SETUP.exe` - SHA256 `760FF711920869BF3FFA031747A2BF192C46266A76C3698F520D55BE9FC14B22`;

- installer pubblico compilato con Inno Setup 7;

- JAR 3.2.0 copiato anche nell'installazione operativa usata da FCM.









## RecordsNext 3.1.1 Ã¢â‚¬â€ 2026-09-06



### Correzione output JavaScript



- corretta la regressione della 3.1.0 che, nel percorso multisito, pubblicava nuovamente file cumulativi da circa 6-11 MiB;

- introdotta pubblicazione shard stagionali **flat** direttamente nella cartella `js` del sito;

- eliminata la dipendenza operativa dalla sottocartella `js/recordsnext-data`;

- mantenuti invariati i nomi canonici `fcmRecordsNext_Classics.js`, `Series`, `RU`, `Modifiers`, `ThresholdsLuck`, `Culometro` e `Matches` come facade leggere;

- ogni sito resta autosufficiente e contiene localmente gli shard ammessi dal proprio cutoff storico;

- mantenuto il cutoff multisito basato su `rn_season.sort_order`;

- pulizia controllata degli shard flat obsoleti durante la ripubblicazione.



### Verifiche



- suite automatica: 52 test, 0 failure, 0 errori;

- pubblicazione sito corrente verificata: 140 shard + 9 file canonici = 149 file;

- shard massimo osservato: circa 0,93 MiB;

- nessun file RecordsNext oltre 1,5 MiB;

- pubblicazione multisito reale verificata su tutte le stagioni configurate;

- numero file progressivo coerente col cutoff storico (16 sul sito 2006, +7 per ogni stagione successiva con dati, 149 sui siti piÃƒÂ¹ recenti);

- nessuna sottocartella RecordsNext richiesta sotto `js`;

- vecchie cartelle `recordsnext-data` rimosse dai siti storici dopo il collaudo.

## RecordsNext 3.1.0 â€” 2026-09-02



### Architettura e storico

- separazione definitiva tra sorgenti storiche e destinazioni di pubblicazione;

- FCM/FCA/DataA definiscono lo storico;

- sito locale opzionale per stagione;

- `DataA` canonico in `data/calendars/DataA-YYYY.js`;

- archivio calendari 1991-2026 incluso nella distribuzione;

- fallback da `<sito>\js\DataA.js` solo quando il calendario canonico manca;

- nessuna normalizzazione distruttiva dei diversi formati DataA storici.



### Pubblicazione multisito

- pubblicazione ordinaria nel solo sito della stagione corrente;

- comando `Pubblica i siti delle stagioni selezionate`;

- ogni target riceve esclusivamente lo storico fino al proprio `sort_order`;

- `Core` e `Manifest` coerenti con la stagione target;

- identitÃ  canoniche riancorate allo scope storico disponibile;

- Classici, Serie, RU, Modificatori, Soglie/Fortuna, Culometro e Matches generati nello scope del target;

- rimosso il fallback hardcoded verso `E:/fantacalcio/Lega2025/js`;

- corretto lo staging di `culometro.json` nel flusso multisito.



### GUI

- versione GUI 3.1;

- semantica distinta fra pubblicazione corrente e pubblicazione multisito;

- sito locale non piÃ¹ obbligatorio;

- stato calendario mostrato esplicitamente;

- recupero automatico del DataA dal sito quando l'archivio canonico Ã¨ assente;

- ID lega rimosso dai campi richiesti all'utente;

- nome lega opzionale, con tentativo di derivazione dal primo FCM;

- modalitÃ  di aggiunta stagione Manuale semplificata, senza controlli FCM/FCA inutili;

- testo informativo stagioni aggiornato al modello 3.1;

- normalizzazione URL online con `http://` come protocollo predefinito quando assente.



### Modello dati e robustezza

- introdotto `SeasonPublicationTargetRepository`;

- scope di pubblicazione basato su `sort_order`;

- cancellazione stagioni transazionale e coerente con foreign key;

- riancoraggio delle identitÃ  canoniche dopo cancellazione;

- promozione della stagione gestita piÃ¹ recente rimasta come anchor;

- `fcmRecordsNext_Matches.js` mantenuto come dataset canonico partita-per-partita.



### Packaging

- versione Maven 3.1.0;

- installer Windows realizzato con Inno Setup 7;

- scelta della directory sempre visibile;

- convenzione pubblica `<cartella FCM>\plugin\RecordsNext`;

- distribuzione pubblica **solo clean install**;

- eliminato il flusso UPDATE pubblico;

- UCanAccess 2.0.9.5 mantenuto nel runtime;

- setup senza database personali, FCM/FCA o configurazioni utente.



### Verifiche finali

- suite automatica: **50 test, 0 failure, 0 errori**;

- build Maven completata con successo;

- pubblicazione multisito reale verificata su **21 siti**, dalle stagioni 2006/07 a 2026/27;

- **189 file validati e 189 pubblicati**;

- verificato il cutoff storico senza contaminazione da stagioni future;

- setup finale compilato con successo.



SHA256 `RecordsNext_3.1.0_SETUP.exe`:



`C39B0EA205F7D81660CD1FF09EC3F6014090932FF3E0D4DA721870D2740CA6EB`



## RecordsNext 3.0.0 â€” 2026-08-28



### Architettura dati stagionale



- introdotti shard JavaScript separati per stagione per Classics, Series, RU, Modifiers, ThresholdsLuck, Culometro e Matches;

- ogni stagione conserva i propri sette shard nella cartella `js/recordsnext-data` del relativo sito FCM;

- il sito corrente mantiene gli stessi nomi pubblici `fcmRecordsNext_*.js`, ora trasformati in facade leggere;

- API JavaScript pubblica e strutture dati esposte ai visualizzatori restano compatibili con RecordsNext 2.1;

- caricamento locale e online degli shard basato sui percorsi configurati per ciascuna stagione, senza dipendenze specifiche da Altervista;

- introdotta la disponibilita' progressiva locale/online delle stagioni storiche;

- introdotto lo stato persistente `data/consolidation/recordsnext-shards.properties`;

- aggiunta `SeasonShardAvailabilityCli` per la gestione della migrazione progressiva;

- shard invariati non vengono riscritti;

- una stagione corrente ancora senza partite testa-a-testa non genera shard vuoti ne' riferimenti a file inesistenti.



### Elaborazione consolidata



- la modalita' `CONSOLIDATED` aggiorna soltanto la stagione corrente quando storico e configurazione risultano invariati;

- corretta la gestione di una nuova stagione senza `season_normalized_*.json`: Classic e RU restano invariati invece di terminare con errore;

- validato il passaggio da circa 103 secondi in FULL a circa 5-6 secondi in CONSOLIDATED sul dataset operativo corrente.



### Pubblicazione



- restano pubblicati soltanto i 9 output moderni RecordsNext;

- gli output pubblici legacy `records2026.*` non vengono piu' generati;

- validata la distribuzione di 20 stagioni storiche, dal 2006_2007 al 2025_2026;

- validati 140 shard complessivi, 7 per stagione;

- tutti i singoli shard restano sotto 1 MiB nel dataset operativo validato;

- le sette facade del sito corrente referenziano correttamente tutte le stagioni storiche abilitate;

- corretta la rotta online della stagione 2022_2023 verso `/lega2022/`.



### Versione



- versione Maven 3.0.0;

- manifest applicativo 3.0.0;

- runner console identificato come RecordsNext 3.0;

- Java 21 o superiore;

- UCanAccess 2.0.9.5 invariato.



## RecordsNext 2.1.0 Ã¢â‚¬â€ 2026-08-26



Aggiornamento della linea RecordsNext 2.x orientato alla completezza degli output,

alla paritÃƒÂ  dei dati disponibili sul sito e alla semplificazione dell'aggiornamento

delle installazioni esistenti.



### Output e dati



- aggiunto l'output canonico `fcmRecordsNext_Matches.js`;

- una riga per squadra per ogni incontro reale, quindi due righe per partita;

- esclusione dei turni di riposo dall'output Matches;

- risultati standardizzati `V/N/P`;

- link ai tabellini storici corretti anche per stagioni precedenti;

- arricchiti i dettagli delle Serie con stagione, competizione, giornata, incontro e tabellino;

- arricchiti gli eventi del Culometro con frequenza storica dell'evento,

  frequenza storica della configurazione e impatto;

- invariata la formula di calcolo e la classifica del Culometro.



### Bonifica output legacy



- rimossa dalla pubblicazione la famiglia di file `records2026.*` non piu consumata dal sito moderno;

- `Records2026ClassicJsExporter` e `Records2026RuJsExporter` restano usati solo internamente dai wrapper RecordsNext per preservare la compatibilita dei dati;

- `fcmRecordsNext_Classics.js` e `fcmRecordsNext_RU.js` verificati byte-per-byte invariati rispetto agli output 2.1 precedenti;

- eliminati circa 28 MiB di output legacy per sito senza modifiche al frontend.



### Configurazione storica



- dopo l'aggiunta di una stagione gestita, la finestra delle associazioni storiche

  si apre direttamente sulla stagione appena aggiunta;

- il comando generale di configurazione delle associazioni conserva invece

  il comportamento newest-first.



### Aggiornamento da RecordsNext 2.0



- introdotto un pacchetto UPDATE dedicato;

- l'aggiornamento sostituisce soltanto programma e launcher;

- configurazione, database, stagioni e associazioni storiche vengono conservati;

- backup automatico di sicurezza prima della sostituzione;

- aggiornamento 2.0 -> 2.1 verificato su un'installazione reale giÃƒÂ  configurata.



### Release e runtime



- versione Maven 2.1.0;

- manifest applicativo 2.1.0;

- GUI identificata come RecordsNext 2.1;

- runtime UCanAccess 2.0.9.5 invariato;

- Java 21 o superiore.



### Verifiche



- suite Maven completata con 41 test superati;

- regressione completa sui dati storici verificata;

- Matches verificato su 20 stagioni;

- 10.930 partite reali e 21.860 righe squadra verificate;

- nessun gruppo Matches diverso da due righe;

- nessuna coppia non speculare;

- confronto semantico delle famiglie preesistenti superato;

- aggiornamento reale 2.0 -> 2.1 verificato senza modifica di configurazione e database;

- generazione del sito tramite il flusso reale FCM verificata con RecordsNext 2.1.



## RecordsNext 2.0.0 Ã¢â‚¬â€ 2026-08-10



Prima release stabile della nuova linea RecordsNext 2.0.



### NovitÃƒÂ  principali



- nuova GUI Swing dedicata `RecordsNext by mauz79`;

- configurazione guidata di lega e stagioni;

- supporto stagioni GESTITE e MANUALI;

- associazioni storiche/canoniche di squadre e competizioni;

- lettura FCM/FCA tramite UCanAccess 2.0.9.5;

- pipeline modulare per famiglie di record;

- modalitÃƒÂ  di elaborazione Consolidata e Completa;

- output JavaScript separati per famiglia;

- pubblicazione verso il sito FCM;

- visualizzatori HTML statici;

- supporto ai link tabellini `.htm`, `.html` e `.php`;

- record di lega;

- Culometro opzionale.



### Famiglie disponibili



- Classici

- Serie

- Riserve d'Ufficio

- Modificatori

- Soglie e Fortuna

- Culometro



### Modificatori



- supporto modificatori standard FCM;

- supporto MODM1PERS, MODM2PERS e MODM3PERS;

- nomi personalizzabili dei modificatori personali;

- selezione granulare delle statistiche;

- supporto serie dei modificatori;

- supporto Fattore Campo.



### Storico



- associazioni canoniche delle squadre;

- associazioni canoniche delle competizioni;

- gestione differenze di denominazione tra stagioni;

- esclusione delle stagioni MANUALI dall'elaborazione FCM;

- ricostruzione completa delle stagioni GESTITE.



### Visualizzatori



- home RecordsNext;

- Classici;

- Serie;

- Riserve d'Ufficio;

- Modificatori;

- Soglie e Fortuna;

- Culometro;

- Record di lega;

- filtri per stagione e competizione;

- nomi competizioni leggibili;

- selezione eventi Culometro nella sola vista Eventi;

- profili `mauzstrom`, `maelstrom`, `fantablue2`, `neutral`.



### Release e runtime



- versione Maven 2.0.0;

- manifest applicativo 2.0.0;

- launcher `RecordsNext.bat`;

- runtime UCanAccess 2.0.9.5 distribuito con dipendenze e licenze;

- configurazione `league.json` generata automaticamente dalla GUI.



### Verifiche effettuate



- build Maven completato con successo;

- test automatici superati;

- audit contratto checkbox Ã¢â€ â€™ vista superato;

- audit semantici Classici, Serie, RU, Modificatori, Fattore Campo, Soglie/Fortuna e Culometro superati;

- smoke test GUI e visualizzatori superato;

- lettura FCM dalla distribuzione di prova verificata;

- generazione automatica `config/league.json` verificata.



### Note



Le stringhe interne che citano RecordsNext 1.0.2 negli exporter indicano la provenienza/compatibilitÃƒÂ  degli archivi normalizzati e non la versione corrente del prodotto.
