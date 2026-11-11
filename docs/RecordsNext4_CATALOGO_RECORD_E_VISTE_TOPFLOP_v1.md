# RecordsNext 4.0 — Catalogo Records Top & Flop e viste pubbliche — v1

Data: 2026-11-06  |  Stato: **CATALOGO FUNZIONALE APPROVATO; CONTRATTO DI CALCOLO, NON CODICE IMPLEMENTATO**

## 0. Decisione architetturale

- Conservare nel **SQLite locale** tutto lo storico originale FCM/FCA, fatti, evidenze, associazioni e aggregati; l'archivio e la sorgente dei nuovi record.
- Pubblicare sul sito esclusivamente **viste interessanti predefinite**, classifiche, aggregati di carriera e **dettagli degli episodi selezionati**, non un secondo archivio completo di ogni giornata/contesto.
- Riutilizzare catalogo, `RecordsNextPipeline`, manifest, shard stagionali nella cartella `js`, cutoff storico multisito, staging, controllo di integrita, rollback e skin della **3.2**. Niente directory condivisa ReNewo obbligatoria, niente pipeline parallela.
- Singolo JS UTF-8: obiettivo <= 1.000.000 byte; **limite invalicabile <= 1.500.000 byte** misurato sul file generato. Pubblicazione vietata se un file eccede o un controllo fallisce. Misurare anche il peso complessivo effettivo per ciascun sito/stagione; non pubblicare preventivamente lo staging v1 da 372 MB o v2 da 59,9 MB.
- Distinguere sempre **REALI** (una prestazione FCA per giocatore/stagione/giornata Serie A), **APPORTATI** (una prestazione per contesto fantasquadra/competizione/incontro comprovato come conteggiato), **SPRECATI** (esclusione certa di giocatore effettivamente tesserato in quella giornata; fantavoto disponibile da fonte verificata). Lo stesso atleta puo apportare a piu fantasquadre e campionato/coppe nella stessa giornata senza moltiplicare statistiche reali.
- Identita: `(stagione, GiocatoreA.ID)` per l'archivio stagionale; `CodiceGazza` e chiave candidata storica con collisioni da verificare, **mai fondere automaticamente** identita conflittuali. Ruolo storico stagionale, non solo quello attuale.
- **Portieri inclusi in tutte le viste applicabili**, anche marcatori e assistman; mai escludere gol segnati o assist dei portieri per la posizione.

## 1. Campi fonte FCA verificati (schema RAW 2026/27 e lettori Java)

`raw_2026_2027_fca_giocatorea.RUOLO = 1` identifica i portieri nello snapshot verificato (87 soggetti; 2, 3, 4 altri ruoli). Verificare la corrispondenza nelle stagioni precedenti, non fissare il valore 1 per ogni schema senza controllo.

`Punteggio.VOTO1` = voto reale fonte 1 (Gazzetta); `VOTO2`, `VOTO3` = altre fonti, **non sommarle**. `GiocaIn.GIORNATA` = giornata reale Serie A. Gli eventi FCA presenti nello schema 2026/27 comprendono:

| Codice sorgente | Significato operativo | Regola non negoziabile |
|---|---|---|
| `GOLFATTI1` | Gol segnati, fonte Gazzetta | Fonte 1 per i record standard; anche portieri. Non sommare `GOLFATTI1/2/3`. |
| `GOLFATTISURIGORE1` | Gol su rigore | **Sottoinsieme** di `GOLFATTI1`, NON gol aggiuntivi. Gol non su rigore = totale meno rigori, solo se campi coerenti. |
| `ASSIST` | Assist | Per tutti i ruoli, portieri inclusi. |
| `GOLSUBITI` | Gol subiti | Particolarmente per portieri; non dedurre da risultato FCM. |
| `GOLSUBITISURIGORE` | Gol subiti su rigore | **Sottoinsieme** di `GOLSUBITI`, non sommare a questi. |
| `RIGPAR` | Rigori parati | Record specifici portieri ma non sopprimere altri ruoli se la fonte segnala l'evento. |
| `RIGSBA` | Rigori sbagliati | Malus/evento per ogni ruolo. |
| `AUTOGOL1` | Autogol Gazzetta | Non sommare `AUTOGOL1/2/3`. |
| `AMM`, `ESP` | Ammonizioni, espulsioni | Conteggi distinti. |
| `GOLVITTORIA`, `GOLPAREGGIO`, `DEF1/2/3` | Altri dettagli originali | Conservare localmente; pubblicare come evento solo dopo verifica semantica del regolamento storico. |

Per i fantasy apportati usare il valore effettivo `FCM.Tabellino.TOT` **soltanto per slot conteggiati** e conservare distinto `FCM.Tabellino.VOTO`. Per i non schierati il fantavoto storico potrebbe non essere disponibile: **NULL / da verificare**, mai ricostruito con coefficienti universali. Eventi FCA dell'incontro fantasy sono attribuiti come *apportati* solo con evidenza `COUNTED`.

Un portiere con gol segnato accumula gol in TF07 e TF20; un portiere che subisce gol compare in TF18. **Il fantavoto non si ricalcola sommando o sottraendo questi eventi** senza le regole storiche; usare il TOT reale registrato.

## 2. Filtri e denominatori comuni

Asse temporale: stagione calcistica YYYY/YYYY, numero stagione AlterLega, carriera fino al cutoff del sito; nessun riferimento a stagioni future per siti precedenti. Asse fantasy: fantasquadra, singola competizione, famiglia `CAMPIONATO` o `COPPE`, tutte; risultati reali e fantasy separati. `Play Off - Play Out` e `Spareggio Serie B` = CAMPIONATO; Youth League esclusa dai dati pubblici (contatore di esclusione solo audit). Il caso 2022/23 di 100 righe con mapping canonico ESCLUSA va conservato come anomalia tracciata, non normalizzato inventando la giornata.

Sia `N_R` il numero di *giornate reali con voto FCA valido*; `N_A` il numero di *contesti fantasy con apporto COUNTED e valore valido*; `N_VA` il numero di apporto COUNTED con voto reale valido (distinto da `N_A` quando necessario). Presenze reali/non giocate e presenze con voto sono concetti differenti. `SV_R` = somma VOTO1 su N_R; `SV_A` = somma VOTO1 sui contesti N_VA; `SF_A` = somma TOT su N_A.

- `mediaReale = SV_R/N_R`, `mediaVotoApportato = SV_A/N_VA`, `fantamediaApportata = SF_A/N_A`, se il rispettivo N > 0; altrimenti `null`, non 0.
- Per Incompresi, `E` = esclusioni confermate con proprieta della rosa verificata su effettiva giornata e incontro giocato; `P_V` = esclusioni con VOTO1 valido, `P_F` = esclusioni con fantavoto attendibile; `S_V`, `S_F` le somme sui rispettivi casi. `mediaVotoSprecato = S_V/P_V`, `errorePesatoVoto = S_V/E`, `fantamediaSprecata = S_F/P_F`, `errorePesatoFantavoto = S_F/E`. Se tutti i voti e fantavoti sono disponibili, `P_V=P_F=P` come nel modello originario. Esporre sempre `E`, `P_V`, `P_F`; se `E=0` il pesato e null, se `E>0` e nessun voto valido il pesato matematico e 0 ma le medie restano null. **Se fantavoti mancanti, non assegnare zero a prestazioni note ma prive di fonti**: segnalare la copertura e non ammettere confronti fuorvianti.
- In lista con ordinamento su **medie** o **tassi** applicare soglie minime configurabili e mostrare comunque il numero osservazioni. Proposta tecnica NON ancora approvata: minimo 5 per media stagionale di campionato, 20 per carriera, adeguamento alla durata effettiva delle coppe e alle stagioni appena iniziate. Per **massimi/minimi di singola prestazione**, conteggi assoluti, gol o rigori parati non esistono soglie artificiali.
- Parimerito: conservare tutti i pari merito matematici; mai ordinare per nome come criterio di merito. Filtro portieri = ruolo della stagione FCA verificato; classifica generale valida per tutti i ruoli e, quando opportuno, tab ruolo separato.

## 3. Catalogo dei record individuali approvati

| ID | Titolo pubblico | Aggregazione e ordinamento | Evidenza e guardrail |
|---|---|---|---|
| TF01 | Miglior voto reale | MAX `VOTO1` per singola giornata FCA | `VoteState.VALID`, una sola giornata reale, link fantasy eventuale, non necessario. |
| TF02 | Peggior voto reale | MIN `VOTO1` valido per giornata FCA | Mai includere `SV` o `NULL` come zero. |
| TF03 | Miglior fantavoto apportato | MAX `TOT` tra contesti fantasy `COUNTED` | Match, team, competizione, doppia giornata e tabellino. |
| TF04 | Peggior fantavoto apportato | MIN `TOT` tra contesti `COUNTED` e TOT valido | RU/voto d'ufficio non attribuiti a giocatore reale. |
| TF05 | Migliori/peggiori medie voto reale | AVG `VOTO1` per stagione/carriera su N_R | Graduatorie TOP e FLOP distinte; N e soglia visibili. |
| TF06 | Migliori/peggiori fantamedie apportate | AVG `TOT` su N_A per fantasquadra, competizione e stagione/carriera | Non mischiare prestazioni non schierate o contesti paralleli; contesti distinti espliciti. |
| TF07 | Marcatori reali / gol apportati | SUM `GOLFATTI1` reale, oppure SUM stesso evento sui contesti COUNTED | DUE classifiche separate; rigori segnati sottoinsieme; portieri inclusi. |
| TF08 | Assist reali / apportati | SUM `ASSIST` su reali, oppure sui contesti COUNTED | DUE classifiche separate, portieri inclusi. |
| TF09 | Re degli eventi negativi | SUM per tipo (`AMM`, `ESP`, `AUTOGOL1`, `RIGSBA`, altri verificati) | Evitare un punteggio unico di malus senza coefficienti storici espliciti. |
| TF10 | Record di continuita/presenze | COUNT di giornate reali con voto o COUNT di contesti COUNTED | Etichette **distinte**; non chiamare presenze i semplici record GiocaIn. |
| TF11 | L'incompreso: media voto sprecato | DESC `S_V/P_V` su esclusioni certe | N e copertura. Non inferire titolarita da un solo snapshot tesserato. |
| TF12 | L'errore pesato sul voto | DESC `S_V/E` | La somma voto include voto intero, non `voto - 6`; esclusioni senza voto nel denominatore. |
| TF13 | Fantavoto sprecato | DESC `S_F/P_F` e `S_F/E` come due graduatorie | Fonti fantavoto per esclusi da verificare; se non complete, **vista non attivabile** come classifica definitiva. |
| TF14 | Stagione schillaciana | Differenza positiva fra media voto **della stagione** e baseline delle altre stagioni con voto dello stesso calciatore | `delta=meanSeason - meanOtherSeasons` con baseline pesata per giornate; almeno un'altra stagione comparabile; N e soglia; versionare il criterio. |
| TF15 | Stagione sbagliata | Differenza negativa rispetto alla baseline come TF14 | Non confondere una brutta stagione reale con una stagione poco sfruttata dalla singola fantasquadra; eventuale confronto apporto in sezione separata. |
| TF16 | Chi lo ha valorizzato meglio | Per stesso calciatore confronto fantasquadre: COUNT COUNTED, AVG voto e TOT, SUM gol/assist apportati | Evidenziare volume e media, non attribuire una qualita unica arbitraria; squadre e competizioni comparabili. |

`TF05/TF06` includono le versioni FLOP delle medie senza aggiungere ID duplicati. `TF09` pubblica graduatorie per tipo di evento, con opportuni filtri di ruolo e stagione. Gol subiti/rigori parati sono parte delle viste evento e dei record portieri dedicati.

## 4. Record dedicati ai portieri (estensione espressamente richiesta e accolta)

| ID | Titolo pubblico | Metrica | Condizioni |
|---|---|---|---|
| TF17 | Para-rigori | SUM `RIGPAR` per portiere e record MAX in singola giornata, sia reale sia apportato | Versioni distinte. Fantasquadra premiata solo se il portiere e stato effettivamente COUNTED. |
| TF18 | Gol subiti dai portieri | SUM `GOLSUBITI`, MAX in singola giornata e media per giornata reale valida / match fantasy conteggiato | In classifica comparare i **denominatori**; mai definire una metrica per 90 minuti senza minuti verificati. `GOLSUBITISURIGORE` e un sottoinsieme. |
| TF19 | Giornate senza gol subiti | COUNT giornate portiere con voto valido, presenza plausibile verificata e `GOLSUBITI=0` esplicito | Etichetta prudente "giornate con zero gol subiti registrati". **Non dichiarare clean sheet ufficiale** senza evidenza sulla partecipazione e sull'intera gara. |
| TF20 | Portieri goleador | SUM `GOLFATTI1`, rigori segnati subset e SUM `ASSIST`, con eventi singoli | Anche fantasquadre beneficiarie se COUNTED. Record validi pure in TF07/TF08 generali. |

Tutti i record generali TF01–TF16 rimangono aperti ai portieri. I record TF17–TF20 sono viste specializzate, non una famiglia/publisher aggiuntiva. Gol subiti, rigori sbagliati, ammonizioni, espulsioni e autogol possono apparire nei record di eventi di qualsiasi ruolo **se effettivamente presenti nella fonte**.

## 5. Le cinque viste HTML e il contratto dati minimo

**A. Top/Flop:** classifiche TF01–TF10, TF14–TF15, TF17–TF20; per ogni risultato pubblicare `recordId`, scope, posizione/pari merito, `playerKey`, `name`, `role`, `value`, `sampleN`/denominatore, unità, anno, giornate (se record di gara), competizione, fantasquadra se pertinente, `scorecardUrl` canonica quando presente, identificativo interno per join nascosto.

**B. Carriere:** ricerca nome/codice storico, ruolo per stagione, quadro reale annuale e cumulato (N, media voto, gol, assist, rigori parati, gol subiti ecc. per ruolo), quadro fantasy di contesti COUNTED (N, media, fantamedia, eventi apportati) per squadra/famiglia/competizione, progressione per anno, record personali. Niente elenco automatico di ogni giornata reale.

**C. Incompresi:** TF11–TF13 con quattro metriche separate e `E`, `P_V`, `P_F`, copertura, fantasquadra, stagione/competizione; esempi con incontro e tabellino solo per esclusioni certe; se storico di possesso/fantavoto non dimostrabile, evidenziare **non disponibile/da verificare** e non inventare graduatoria.

**D. Fantasquadre:** confronti TF16, ranking di contributi reali e fantasy per calciatore/team/competizione, differenza di utilizzo e prestazioni non sfruttate soltanto quando confermate. I contesti di coppa/campionato rimangono distinti.

**E. Eventi e scheda personale:** filtri per gol, gol su rigore, assist, rigori parati, gol subiti, gol subiti su rigore, autogol, ammonizioni, espulsioni, rigori sbagliati e dati storicamente disponibili; dettaglio limitato agli episodi classificati e alle prestazioni significative. La scheda linka il tabellino corretto per stagione; per la prestazione FCA priva di collegamento FCM non inventare URL.

Un risultato con record partita deve contenere **stagione, numero storico AlterLega, competizione e giornata DELLA COMPETIZIONE (se mappata), giornata REALE Serie A, fantasquadra, schieramento e `conteggiato`**, voto, fantavoto (se disponibili), bonus/malus separati e link al tabellino originale (se disponibile). Gli ID FCM tecnici non sono titoli pubblici.

## 6. Proiezione JS proposta, non copia del database

- `Index/Manifest` familiare: versioni contratto, stagioni incluse fino a cutoff, riferimenti agli shard delle viste, contatori/capacita, dizionari strettamente necessari.
- `Records` (shard piccoli): righe ranking dei soli TF attivati con metrica, contesto, N e riferimenti agli episodi significativi.
- `Careers` (shard piccoli): un profilo aggregato per giocatore/anno piu indici per carriera, fantasquadra, competizione/famiglia; no ripetizione delle 617.956 giornate e 554.425 contesti sorgente.
- `Highlights` (on demand): **solo** episodi che compaiono nelle classifiche o nei record/schede, deduplicati per chiave reale o fantasy, con bonus/malus e URL. Non esportare dettagli non interessanti.
- Ogni sito pubblica solo le **viste con cutoff** tramite meccanismi 3.2. Testare peso JS per ciascuna stagione/sito oltre al limite per file. Evitare nuove sottocartelle sotto `js` se la 3.2 non le prevede. Il numero massimo di record per vista e il livello di dettaglio sono configurazioni del catalogo versionate, non `TOP N` arbitrario nel browser.
- Nuove viste future = nuovo calcolo da SQLite; nessuna reimportazione obbligatoria perche non si e pubblicata ogni giornata in JS.

## 7. Criteri di accettazione / test prima dell'integrazione

1. Campo reale `VOTO1` valido, `SV` e `NULL` distinti; nessuna presenza fittizia, nessuna media con zero inventato.
2. Tre fantasquadre con stesso giocatore nella stessa giornata = un solo fatto reale e piu apports distinti; campionato e coppa differenziati anche per stesso team.
3. Portiere con `RIGPAR=1`, `GOLSUBITI=2` e `GOLSUBITISURIGORE=1`: rigori parati +1, gol subiti **2 e non 3**; distinguere sottocategoria.
4. Portiere con `GOLFATTI1=1`, `GOLFATTISURIGORE1=1`: gol **1 e non 2**; entra nelle classifiche TF07 e TF20 e, se COUNTED, nei gol apportati.
5. Portiere con assist: in TF08 e TF20, senza filtri preconcetti. Prova sintetica se il campione disponibile non contiene il caso.
6. Un giocatore senza prova di possesso non puo alimentare `E`; un fantavoto mancante non diventa `0`. Valutare casi di cessione/riacquisto e continuita fra giornate (Varela).
7. Youth League sempre assente da ogni JS, mentre Play Off e Spareggio sono considerati CAMPIONATO; conflitto 2022/23 gestito esplicitamente.
8. Record di partita con giornata e scorecard canonica del sito corretto; nessun `IDGIORNATA` mostrato al posto dell'ordinale.
9. Stesso identico record filtrato per stagione, ruolo, team, competizione e cutoff ricostruibile dalle tabelle SQLite; pari merito e N coerenti.
10. Misurare file e totale JS per sito, validare schema/facade/shard, manifest, rollback e non-regressione delle famiglie 3.2 prima di installare sui siti reali.

## 8. Questioni tecniche aperte (non fingere approvazione di coefficienti)

- Soglie numeriche minime per medie/indici: parametri configurabili, proposta preliminare al §2; da confermare con la distribuzione reale per campionati/coppe/stagioni in corso.
- Verifica delle esclusioni fuori dalla lista Formazione su cronologia tesseramento, sostituzioni e fantavoto verificabile degli esclusi; TF11–TF13 non vanno dichiarati definitivi finche incompleti.
- Verifica delle etichette originali e del significato dei campi in FCA delle stagioni precedenti; mantenere il mapping per anno/fonte e segnalare i campi assenti.
- Confronto di impiego e "stagione schillaciana/sbagliata" e solo comparabile su dati con campionamento sufficiente e baseline disponibile; il delta voto NON e un giudizio sulla squadra o sull'atleta.
- Numero preciso di JS, volume per sito e quota `Highlights` = da **misurare mediante l'implementazione**, non promessa prima dell'export.

## 9. Provenienza e stato

Contratto precedente: `RecordsNext4_TopFlop_ContrattoVistaERecord_v1.md`; decisioni v24 e specifica exporter compatto v2 (quest'ultimo e **staging di fatti integrali**, non il formato pubblico finale). Verifica diretta sul DB di test `recordsnext-2026_2027-verifica-v1_20261102_151419_388732.db`: tabella `raw_2026_2027_fca_punteggio` con i campi elencati, `raw_2026_2027_fca_giocatorea` con `RUOLO`, `raw_2026_2027_fcm_tabellino` con `TOT`/`VOTO`. Gli schemi storici e le regole regolamentari restano da convalidare prima di attivare i rispettivi record.

**Nessun sorgente Java, database, visualizzatore, CSS, GUI, publisher o sito e modificato dalla presente specifica.**
