# Stato implementazione RecordsNext 3.2.0

## RecordsNext 3.2.0 - 2026-09-16

Stato corrente verificato della linea 3.2:

- catalogo modulare completo: 94 record/figli complessivi, inclusi i 93 record configurabili e il Culometro opzionale;
- preflight corretto per il Fattore Campo: la capability `modifier.home-field` viene resa disponibile quando sono selezionati record Fattore Campo;
- `modifiers.home-field-deciding` non viene piu saltato erroneamente per dipendenza mancante;
- Culometro integrato con gli eventi `HOME_FIELD_DECISIVE`;
- `config/culometro.json` include il componente `HOME_FIELD_DECISIVE`;
- gli eventi Culometro espongono frequenza storica evento, frequenza storica configurazione, chiave configurazione, contributo e impatto;
- verifica reale Culometro con Fattore Campo completata: `homeFieldCandidateCount=1304`, 2608 occorrenze `HOME_FIELD_DECISIVE` nel JS generato;
- contratto checkbox -> vista verificato con `Test_RecordsNext2_CheckboxViews_v15.ps1`: 96 controlli, 0 problemi;
- Manifest verificato coerente con la selezione reale: 67 selezionati, 67 eseguibili, 67 completi, 0 parziali, 0 saltati;
- audit finale `Test_RecordsNext2_FinalSemantic_v31.ps1` superato:
  - Culometro - contributi: OK;
  - Culometro - overlap: OK;
  - Culometro - ranking: OK;
  - Culometro - ranking competizione: OK;
  - Culometro - metadata: OK;
  - Record di lega: OK;
  - eventi Culometro verificati: 17083;
  - ranking Culometro: 438;
  - ranking per competizione: 1403;
  - righe `puntiSquadraMin`: 2312;
  - problemi totali: 0;
  - audit semantico `Test_RecordsNext2_ModifiersSemantic_v28.ps1` superato:
  - 156 competizioni controllate;
  - 10210 righe modificatori sorgente;
  - 10930 righe squadra in casa;
  - `modDifesaMax`: OK;
  - `modDifesaTotaleSquadre`: OK;
  - `modDifesaMediaSquadre`: OK;
  - `modDifesaUtilizziSquadre`: OK;
  - `capitanoTotaleSquadre`: OK;
  - `capitanoUtilizziSquadre`: OK;
  - `modDifesaFcmMax`: OK;
  - `modDifesaFcmTotaleSquadre`: OK;
  - `modDifesaFcmMediaSquadre`: OK;
  - `modDifesaFcmUtilizziSquadre`: OK;
  - `fattoreCampoDecisivo`: OK;
  - `fattoreCampoTotaleSquadre`: OK;
  - `fattoreCampoPuntiGuadagnatiSquadre`: OK;
  - `fattoreCampoPuntiPersiSquadre`: OK;
  - problemi totali: 0;
  - audit semantico `Test_RecordsNext2_RUSemantic_v26.ps1` superato:
  - 20 stagioni controllate;
  - 3685 righe RU di dettaglio;
  - 3046 partite-squadra con RU;
  - `partiteConPiuRU`: OK;
  - `partiteConRU`: OK;
  - `partiteControRU`: OK;
  - `ruDecisiva`: OK;
  - `bilancioRUDecisiva`: OK;
  - `ruDecisivaContro`: OK;
  - `bilancioRUDecisivaContro`: OK;
  - `bilancioConRU`: OK;
  - `bilancioControRU`: OK;
  - `mediaPuntiConRU`: OK;
  - `mediaPuntiControRU`: OK;
  - `tipoRUUsata`: OK;
  - problemi totali: 0;
  - audit semantico `Test_RecordsNext2_SerieSemantic_v24.ps1` superato:
  - 156 file competizione controllati;
  - 21860 righe partita valide;
  - 1465 squadre controllate;
  - `Clean sheet consecutivi`: OK;
  - `Pareggi consecutivi`: OK;
  - `Sconfitte consecutive`: OK;
  - `Senza sconfitte`: OK;
  - `Senza vittorie`: OK;
  - `Serie Capitano`: OK;
  - `Serie Modificatore Difesa`: OK;
  - `Serie Modificatore Difesa FCM`: OK;
  - `Vittorie consecutive`: OK;
  - problemi totali: 0;
  - audit semantico `Test_RecordsNext2_ThresholdsSemantic_v29.ps1` superato:
  - 156 file normalizzati;
  - 24318 righe partita analizzate;
  - 17685 eventi attesi;
  - 17685 eventi esportati;
  - 438 aggregati squadra attesi;
  - 438 aggregati squadra esportati;
  - `EXACT_THRESHOLD`: 1906 / 1906, OK;
  - `JUST_ENOUGH`: 491 / 491, OK;
  - `MISSED_WIN_HALF_POINT`: 501 / 501, OK;
  - `LOSS_BY_A_WHISKER`: 377 / 377, OK;
  - `MIRACLE_DRAW`: 2620 / 2620, OK;
  - `TIGHT_DRAW`: 2620 / 2620, OK;
  - `ONE_GOAL_WIN`: 4585 / 4585, OK;
  - `ONE_GOAL_LOSS`: 4585 / 4585, OK;
  - `UNUSED_BAND_POINTS`: 0 / 0, OK;
  - problemi totali: 0;
  - audit semantico `Test_RecordsNext2_ClassiciSemantic_v21.ps1` superato:
  - 156 file competizione controllati;
  - 21860 righe partita valide;
  - 1465 aggregati squadra controllati;
  - `Minor punteggio`: OK;
  - `Più gol regolamentari`: OK;
  - `Maggior scarto regolamentare`: OK;
  - `Media punti`: OK;
  - `Somma punti`: OK;
  - `Punti classifica`: OK;
  - `Vittorie`: OK;
  - `Pareggi`: OK;
  - `Sconfitte`: OK;
  - `Gol fatti`: OK;
  - `Gol subiti`: OK;
  - problemi totali: 0;
  - regressione Maven finale dopo gli audit semantici:
  - 57 test eseguiti;
  - 0 failure;
  - 0 errori;
  - 0 skipped;
  - build success.
  - collaudo operativo finale in modalita Completa con il JAR 3.2 candidato: preflight 72 selezionati, 72 eseguibili, 72 completi, 0 parziali, 0 saltati; storico 2006_2007-2025_2026 riconosciuto invariato, stagione corrente 2026_2027 reimportata e normalizzata; elaborazione completata con consolidamento aggiornato, 9 file validi e 0 pubblicati;
- regressione Maven finale sulla versione 3.2.0:
  - 57 test eseguiti;
  - 0 failure;
  - 0 errori;
  - 0 skipped;
  - BUILD SUCCESS;
- gli `outputStatus` dei family JS descrivono lo stato tecnico del dataset generato e non devono essere reinterpretati come duplicazione dello stato del preflight/Manifest;
- il Culometro resta un output opzionale separato dalla famiglia dati Soglie/Fortuna.
- packaging finale completato:
  - `RecordsNext_3.2.0_FULL.zip` - SHA256 `BFE32FBBA14A5E45E3EA110C31A6C207F0E82048162B7572610D6E5A0A38AD71`;
  - `RecordsNext_3.2.0_SETUP.exe` - SHA256 `760FF711920869BF3FFA031747A2BF192C46266A76C3698F520D55BE9FC14B22`;
  - JAR 3.2.0 installato anche nella directory operativa usata da FCM;

La versione applicativa e Maven e stata portata a 3.2.0 dopo il completamento dei test semantici e del collaudo operativo finale. Packaging FULL e SETUP completato; resta da completare la pubblicazione della release.

## RecordsNext 3.1.1 - flat JS - 2026-09-06

La 3.1.1 corregge la regressione di pubblicazione JavaScript emersa dopo l'introduzione del multisito 3.1.0.

Stato verificato:

- sette famiglie dati pubblicate come shard stagionali flat nella cartella `js`;
- nessuna cartella `recordsnext-data` richiesta o creata dalla 3.1.1;
- facade canoniche leggere;
- pubblicazione sito corrente riuscita con 149 file complessivi (140 shard + 9 canonici);
- pubblicazione multisito reale riuscita e progressione dei file coerente col cutoff storico;
- shard massimo osservato circa 0,93 MiB;
- nessun file oltre 1,5 MiB;
- 52 test automatici, 0 failure, 0 errori;
- vecchie cartelle `recordsnext-data` rimosse dai siti storici dopo il collaudo.

La 3.1.0 resta storicamente documentata come release precedente; tag, hash e riferimenti della 3.1.0 non devono essere riscritti.

## Release RecordsNext 3.1.0 - 2026-09-02

RecordsNext 3.1.0 è stato completato, collaudato, versionato e pubblicato. La 3.1 sostituisce il modello precedente in cui il sito storico tendeva a essere trattato come parte necessaria della sorgente.

Verifiche finali della release:

- suite automatica: 50 test, 0 failure, 0 errori;
- build Maven completata con successo;
- pubblicazione reale su 21 siti, da 2006_2007 a 2026_2027;
- 189 file validati e 189 pubblicati;
- installer Windows Inno Setup compilato;
- distribuzione pubblica clean-install-only.

SHA256 setup:
`C39B0EA205F7D81660CD1FF09EC3F6014090932FF3E0D4DA721870D2740CA6EB`

### Architettura multisito consolidata

Principio vincolante:

> FCM/FCA/DataA definiscono lo storico; i siti sono destinazioni opzionali di pubblicazione.

Comportamento implementato e verificato:

- ogni stagione GESTITA continua a richiedere FCM e FCA;
- la cartella del sito locale e' opzionale;
- una stagione senza sito resta parte dello storico e puo' contribuire ai record;
- `DataA` canonico e' `data/calendars/DataA-YYYY.js`; se manca, la GUI può recuperare `<sito>/js/DataA.js` come fallback e archiviarlo internamente;
- ogni stagione puo' avere una propria destinazione `local_site_path`;
- la cartella `js` non viene configurata separatamente, ma deriva sempre da `<local_site_path>/js`;
- la pubblicazione multisito usa come cutoff `rn_season.sort_order`, non il confronto lessicografico del `season_id`;
- ogni sito riceve soltanto le stagioni fino alla propria stagione target;
- `fcmRecordsNext_Core.js` viene filtrato per il target, comprese identita canoniche, mapping e anchor coerenti con lo storico disponibile a quella data;
- `fcmRecordsNext_Manifest.js` usa come `currentSeasonId` la stagione del sito target;
- Classici, Serie, RU, Modificatori, Soglie/Fortuna, Matches e Culometro vengono generati su uno scope stagionale limitato al target;
- il Culometro multisito usa una copia controllata di `config/culometro.json` nello staging di scope;
- il fallback hardcoded a `E:/fantacalcio/Lega2025/js` e' stato eliminato: in assenza di una destinazione reale la pipeline non deve inventare un sito esterno.

### Semantica GUI 3.1

La GUI distingue due casi:

- `Pubblica nel sito della stagione corrente al termine`: uso normale, tipicamente dopo ogni aggiornamento FCM; aggiorna soltanto il sito della stagione corrente;
- `Pubblica i siti delle stagioni selezionate`: manutenzione/riallineamento storico; pubblica i siti configurati delle sole stagioni selezionate. Ogni sito riceve comunque solo lo storico disponibile fino alla propria stagione.

Le checkbox `Elabora` restano quindi anche un controllo esplicito dell'ambito della pubblicazione storica.

### Collaudo sandbox

Il multisito e' stato provato in una working copy isolata, senza scrivere nei siti reali.

Target sandbox verificati:

- `2024_2025` -> `data/test-sites/Lega2024`;
- `2025_2026` -> `data/test-sites/Lega2025`;
- `2026_2027` -> `data/test-sites/Lega2026` quando la stagione e' selezionata.

Risultato del primo collaudo completo sulle stagioni selezionate:

- pubblicazione riuscita su Lega2024 e Lega2025;
- 9 file JS canonici per sito;
- 18 file complessivamente pubblicati;
- nessun errore di pubblicazione;
- Lega2024 verificata senza `2025_2026` e `2026_2027`;
- Lega2025 verificata senza `2026_2027`.

La mancata pubblicazione di `2026_2027` nel primo test era dovuta alla checkbox `Elabora` non selezionata ed e' coerente con la semantica scelta per `Pubblica i siti delle stagioni selezionate`.

### Runtime e test

- Maven: `3.1.0`;
- Java: 21 o superiore;
- UCanAccess operativo: `2.0.9.5`, caricato dal runtime distribuito (`runtime/ucanaccess` e `runtime/ucanaccess/lib`);
- avviare la GUI di sviluppo con il launcher/script che include il runtime UCanAccess: l'avvio tramite solo `mvn exec:java` non rappresenta il classpath dell'installazione completa;
- suite automatica verificata prima dell'aggiornamento documentale: 50 test, 0 failure, 0 errori, BUILD SUCCESS;
- test specifici aggiunti per eliminazione stagione, target di pubblicazione, cutoff `sort_order`, Core stagionale e modalita di pubblicazione.

### Stato release

La 3.1.0 non deve ancora essere considerata rilasciata.

Prima della release restano da completare:

- pulizia finale di packaging e launcher alla versione 3.1;
- verifica dei file statici/visualizzatori e della decisione definitiva sugli shard `recordsnext-data`;
- generazione dei pacchetti FULL e UPDATE 3.1;
- installazione della nuova GUI nell'ambiente operativo;
- primo collaudo controllato sui siti reali.


## Correzione eliminazione stagioni - 2026-09-02

Implementata e verificata la cancellazione reale di una stagione dalla configurazione RecordsNext.

Comportamento consolidato:

- la rimozione non elimina soltanto `rn_season_configuration`;
- vengono rimossi i dati interni RecordsNext appartenenti alla stagione;
- vengono rimossi i riferimenti in `rn_source_file`;
- vengono rimossi team e competizioni stagionali e i relativi mapping;
- vengono rimossi calendario e sorgente calendario della stagione;
- le identita canoniche condivise con altre stagioni vengono conservate;
- se una identita era ancorata alla stagione eliminata, viene riancorata alla stagione mappata piu recente ancora disponibile;
- se una identita esisteva esclusivamente nella stagione eliminata, viene eliminata;
- se viene eliminata la stagione anchor, la stagione gestita piu recente rimasta viene promossa automaticamente ad anchor;
- i file fisici FCM/FCA/DataA e le directory dei siti non vengono cancellati.

Verifica effettuata anche su una copia del database operativo reale:

- eliminata `2026_2027`;
- nessun residuo nelle principali tabelle stagionali;
- nessuna identita rimasta ancorata a `2026_2027`;
- `2025_2026` promossa automaticamente a nuova anchor.

Suite automatica consolidata: 44 test, 0 failure, 0 errori, 0 skipped.


Aggiornamento: 27 agosto 2026.

## Stato release 2.1.0

RecordsNext 2.1.0 Ã¨ completato, testato, taggato e pubblicato nel repository.

Sono implementate e operative:

- GUI completa `RecordsNext by mauz79`;
- configurazione guidata della lega e delle stagioni;
- stagioni GESTITE e MANUALI;
- associazioni storiche di squadre e competizioni;
- normalizzazione e consolidamento multistagione;
- modalitÃ  Completa e Consolidata;
- Classici;
- Serie;
- Riserve d'Ufficio;
- Modificatori;
- Fattore Campo;
- Soglie e Fortuna;
- Culometro opzionale;
- visualizzatori HTML statici;
- pubblicazione nel sito FCM;
- output Matches canonico;
- manifest;
- link ai tabellini storici.

- stagione operativa corrente verificata: `2026_2027`;
- sito corrente configurato: `E:\\fantacalcio\\Lega2026\\js`;
- gli output legacy `records2026.*` non fanno piu parte della pubblicazione; gli exporter legacy Classici/RU restano solo come implementazione interna di compatibilita.

Output pubblico corrente:

- `fcmRecordsNext_Core.js`;
- `fcmRecordsNext_Classics.js`;
- `fcmRecordsNext_Series.js`;
- `fcmRecordsNext_RU.js`;
- `fcmRecordsNext_Modifiers.js`;
- `fcmRecordsNext_ThresholdsLuck.js`;
- `fcmRecordsNext_Culometro.js`;
- `fcmRecordsNext_Matches.js`;
- `fcmRecordsNext_Manifest.js`.

### NovitÃ  specifiche 2.1

`fcmRecordsNext_Matches.js` espone il dataset canonico delle gare,
con due righe per ogni partita reale e una riga per squadra.

Le Serie espongono ora nel dettaglio:

- stagione;
- competizione;
- giornata;
- identificativo incontro;
- link al tabellino.

Gli eventi Culometro espongono inoltre:

- frequenza storica evento;
- frequenza storica configurazione;
- chiave configurazione;
- impatto.

L'aggiunta di una nuova stagione storica apre direttamente le associazioni
sulla stagione appena importata.

### Test finali

Ultimo stato verificato il 26 agosto 2026:

- 41 test automatici;
- 0 failure;
- 0 errori;
- BUILD SUCCESS;
- regressione reale su 20 stagioni completata;
- generazione reale del sito FCM completata;
- test di bonifica publisher sui dati operativi completato: 9 output moderni validati, 0 output `records2026.*`;
- `fcmRecordsNext_Classics.js` e `fcmRecordsNext_RU.js` confrontati SHA-256/byte-per-byte con la staging 2.1: identici;
- aggiornamento reale 2.0 -> 2.1 completato senza perdita di configurazione.

### Release

- Maven: `2.1.0`;
- tag Git: `v2.1.0`;
- commit release: `682f0f6`;
- UCanAccess: `2.0.9.5`;
- Java: 21 o superiore.

Sono disponibili:

- `RecordsNext_2.1.0.zip`;
- `RecordsNext_2.1.0_UPDATE.zip`.

Le sezioni storiche sottostanti descrivono le tappe di sviluppo precedenti
e non prevalgono sullo stato corrente qui riportato.

## Base tecnica verificata

RecordsNext 2.0 nasce dal codice funzionante di RecordsNext 1.0.2.

Sono stati importati e mantenuti:

- progetto Maven e Maven Wrapper;
- Java 21;
- accesso FCM/FCA tramite UCanAccess;
- importazione raw in SQLite;
- configurazione delle stagioni;
- stagioni gestite e manuali;
- associazioni canoniche di squadre e competizioni;
- viste canoniche;
- normalizzazione stagionale;
- consolidamento;
- cache di normalizzazione;
- esportatori Classici e Riserve d'Ufficio esistenti;
- staging, validazione, pubblicazione e rollback;
- GUI esistente come base da evolvere.

UCanAccess 2.0.9.5 resta candidato obbligatorio nei benchmark comparativi.

## Modello modulare implementato

Sono state introdotte le strutture Java per:

- cinque famiglie elaborabili;
- figli di famiglia;
- dipendenze obbligatorie e opzionali;
- stati degli output;
- selezione modulare;
- catalogo iniziale;
- valutazione delle dipendenze.

Famiglie:

1. Classici;
2. Serie;
3. Riserve d'Ufficio;
4. Modificatori;
5. Soglie e Fortuna.

Il Culometro resta un easter egg opzionale, non selezionato automaticamente.

## CompatibilitÃ  con la pipeline 1.0.2

ProcessingOptions e RecordsNextPipeline sono stati estesi mantenendo la compatibilitÃ  con il costruttore legacy basato su:

- Classici;
- Riserve d'Ufficio;
- generazione JavaScript;
- pubblicazione.

La pipeline usa internamente la selezione modulare.

Le famiglie non ancora dotate di elaboratore non vengono ignorate silenziosamente.

## Planner e preflight

Sono implementati:

- inventario delle capacitÃ  disponibili;
- piano di esecuzione;
- valutazione preventiva delle dipendenze;
- conteggio di figli completi, parziali e saltati;
- preflight eseguibile prima della produzione degli archivi e degli output.

La mancanza di una dipendenza blocca soltanto il figlio interessato.

Esempio consolidato:

- Capitano disattivato;
- output del Capitano saltati;
- serie dipendenti dal Capitano saltate;
- tutte le altre serie e famiglie continuano.

## Configurazione modulare

La configurazione processing.json viene letta dal codice Java.

Sono supportati:

- selezione delle cinque famiglie;
- selezione dei figli dei Modificatori;
- Capitano disattivabile;
- Culometro abilitabile solo esplicitamente;
- generazione JavaScript;
- pubblicazione.

Il percorso legacy della GUI resta disponibile durante la migrazione.

## Manifest 2.0

E' implementato il writer di:

fcmRecordsNext_Manifest.js

Il manifest contiene almeno:

- versione programma e schema;
- lega;
- stagione corrente;
- stagioni elaborate;
- famiglie richieste;
- famiglie eseguibili;
- figli generati;
- figli saltati;
- dipendenze mancanti;
- file prodotti;
- stato Culometro;
- riepilogo preflight.

Il manifest viene:

- generato nella staging;
- validato;
- incluso nel conteggio dei file;
- pubblicato insieme agli altri JavaScript;
- protetto dal rollback transazionale esistente.

## Test

Ultimo stato verificato il 6 agosto 2026:

- 38 test eseguiti;
- 0 failure;
- 0 errori;
- 0 test saltati;
- BUILD SUCCESS.

La verifica Ã¨ stata eseguita anche sullâ€™elaborazione reale delle stagioni configurate.

## Famiglia Modificatori

La famiglia Modificatori Ã¨ implementata e verificata.

La configurazione GUI permette di selezionare separatamente, per ciascun modificatore:

- Massimo;
- Totale;
- Media;
- Utilizzi.

Modificatori personalizzati gestiti:

- `MODM1PERS`, con nome configurabile;
- `MODM2PERS`, con nome configurabile;
- `MODM3PERS`, con nome configurabile.

Modificatori standard FCM gestiti separatamente:

- `MODPORTIERE`;
- `MODDIFESA`;
- `MODCENTROCAMPO`;
- `MODATTACCO`;
- `MODMODULO`.

Il file `fcmRecordsNext_Modifiers.js` viene costruito direttamente dagli archivi `season_records_*.json`, senza dipendere dallâ€™esportatore Classici legacy.

I metadati distinguono:

- `availableSections`: sezioni supportate dallâ€™esportatore;
- `generatedSections`: sezioni effettivamente prodotte in base alla configurazione e ai dati disponibili.

Ãˆ stata verificata nellâ€™output reale la presenza di:

- Massimo, Totale, Media e Utilizzi per `MODM1PERS`;
- Massimo, Totale, Media e Utilizzi per `MODM2PERS`;
- Massimo, Totale, Media e Utilizzi per `MODDIFESA`;
- stagione `2006_2007` nei dati del Modificatore Difesa FCM;
- nomi configurati corretti;
- esclusione delle statistiche disattivate.

## Non ancora implementato

Non sono ancora implementati come nuovi elaboratori nativi 2.0:

- Serie;
- Soglie e Fortuna;
- Culometro;
- nuovo output Classici completo secondo lo schema 2.0;
- nuovo output RU completo secondo lo schema 2.0;
- fcmRecordsNext_Core.js;
- completamento e consolidamento della GUI 2.0;
- viste HTML 2.0 definitive;
- JS statici di rendering dei visualizzatori;
- installer effettivo dei visualizzatori e dei profili CSS.

Gli esportatori Classici e RU della 1.0.2 restano operativi come ponte.

## Regole da non perdere

- Non riscrivere accesso FCM/FCA, mapping, normalizzazione e consolidamento senza una necessitÃ  verificata.
- Basarsi sul codice reale di RecordsNext 1.0.2.
- Tutti i JavaScript pubblici vanno nella cartella js del sito FCM.
- Nella root del sito deve esserci un solo HTML indice.
- Le viste e gli asset vanno nella cartella RecordsNext.
- Ogni record riferito a una partita specifica conserva il link al tabellino.
- Squadre e competizioni conservano identitÃ  stagionale e canonica.
- Gli output devono essere viste dati complete, non top list tagliate.
- Gli ZIP temporanei applicati, testati e committati vanno eliminati periodicamente da D:\DEV_APPS\downloads.


## GUI e visualizzatori: stato al 6 agosto 2026

La dashboard GUI e stata riorganizzata con tre aree principali:

- Configurazione stagioni;
- Famiglie record;
- Soglie, Fortuna e Culometro.

La GUI consente gia la configurazione granulare delle famiglie e del Culometro. L'avvio operativo deve includere UCanAccess nel classpath.

Sono consolidate, ma non ancora implementate integralmente, le seguenti decisioni sui visualizzatori:

- gli HTML non contengono dati;
- gli HTML leggono esclusivamente i JS pubblici;
- i JS statici di rendering saranno `fcmRecordsNextFunzioni_common.js` e `fcmRecordsNextFunzioni_viewer.js`;
- tali file saranno installati nella cartella `js` della skin;
- i nuovi nomi file usano underscore e non trattini;
- i profili grafici iniziali saranno `mauzstrom`, `fantablue2` e `neutral`;
- `mauzstrom` usera Trebuchet MS;
- il profilo `neutral` sara moderno, autonomo e ispirato ai principi grafici di ReNewo;
- la presenza degli HTML nella skin non implica la generazione dei relativi dati;
- una pagina priva del proprio JS deve mostrare uno stato vuoto senza errori.

Prossima fase:

1. definire il contratto JavaScript pubblico definitivo;
2. creare i due JS statici di rendering;
3. creare l'indice e le pagine famiglia senza dati incorporati;
4. creare e verificare i tre profili CSS;
5. implementare l'installer nella GUI;
6. aggiungere test automatici per struttura, riferimenti e assenza di dati negli HTML.


## Chiusura release 3.1.0

La release 3.1.0 è considerata funzionalmente chiusa.

Distribuzione pubblica:
- `RecordsNext_3.1.0_SETUP.exe`;
- nessun pacchetto UPDATE pubblico;
- installazione in directory scelta dall'utente;
- convenzione consigliata `<FCM>\plugin\RecordsNext`.

La documentazione utente di riferimento è `README.md` e `INSTALL.md`. La continuità tecnica è mantenuta da `docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md`, generato dallo script dedicato.
