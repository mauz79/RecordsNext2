# RecordsNext 4.0 — integrazione nativa v83

Data: 17 novembre 2026. Baseline: RC v64 / checkpoint v67 `3e8d53f` dichiarato (da confermare sul PC).
Origine: dossier v82, maestro v81, audit v81, sorgenti RC reali v81 (243 sorgenti).

## Ambito definitivo di questa revisione

Unico flusso GUI `Genera / Verifica / Pubblica`. Nessuna seconda esportazione autonoma.
Il motore Java aggiunge allo staging esistente: RealPerformances FCA, CountedPerformances FCM, fantamedia FCA ufficiale accettata solo con presenze verificabili, saldo tabellino e bonus/malus FCM, indice globale di identita approvate; la pipeline pubblica insieme JS e `RecordsNext/top_flop.html`, `RecordsNext/calciatori.html` e CSS.
Il frontend Top & Flop mostra esclusivamente classifiche e filtro; l'Archivio calciatori cerca tutto lo storico del sito (es. Klose), apre schede stagionali o carriere approvate e permette collegamenti diretti dalla classifica.

### Contratti dati

- Tutti i risultati FCM si riferiscono SOLO a `COUNTED`; stessa prestazione FCA reale non viene moltiplicata dalle coppe.
- `MODIF=TOT-VOTO`. Saldo annuale per fantasquadra = somma dei MODIF dei COUNTED. Mai aggiungere MODIF due volte.
- Gol +3, assist +1, rigore parato +3, ammonizione -0,5, espulsione -1, autogol -2, gol subito -1; gol su rigore +2/+3, rigore sbagliato -2/-3 solo se ricostruzione univoca col saldo osservato.
- Bonus e malus **punti** = valori separati SOLO quando ogni COUNTED del contesto e riconciliabile e lo split e univoco. Altrimenti entrambi `null` e motivazione; saldo resta disponibile se verificato.
- `GOLFATTISURIGORE1` sottoinsieme di `GOLFATTI1`; `GOLSUBITISURIGORE` sottoinsieme di `GOLSUBITI`. Una statistica zero non e un valore mancante.
- Scheda 15 indicatori per stagione x fantasquadra e per competizione: fantamedia, media voto, gol, assist, gol su rigore, rigori sbagliati, ammonizioni, espulsioni, autogol, gol subiti, gol subiti su rigore, rigori parati, bonus punti, malus punti, saldo; con numero gare utilizzate e badge saldo max/min annuali.
- Carriere pluriennali solo con approvazioni presenti in `config/player-identities.json`; niente inferenza per nome o Gazzetta.
- L'ultimo FCM di ogni sito stabilisce il cutoff, i JS sono partizionati e non eccedono 1.500.000 byte **reali** per file.
- Le competizioni sono filtrate dalla pagina consumatrice; Java esporta tutte le competizioni presenti, GUI non aggiunge selettori di competizioni.
- Quattro skin: Mauzstrom, Fantablue2, Maelstrom, Neutral.

### Modifiche software (rispettano la baseline Git)

- `src/main/java/.../app/topflop/TopFlopIntegratedV83.java` — nuovo exporter Java nativo, read-only, basato su topflop canonico JDBC.
- `TopFlopViewsPublication.java` — integrazione unica in `prepare()` e `attachScoped()`.
- `RecordsNextPipeline.java` — asset JS nel normale staging + pubblicazione congiunta JS/pagine/CSS con backup e rollback.
- `gui/RecordsNext2Dashboard.java` — etichetta esplicita, pulsante unico invariato.
- `assets/js/fcmRecordsNextTopFlopRankingEngine_v83.js` — semantica zeri: TF20 gol/rigori/assist/totale visibili insieme e nessun primo classificato con 0.
- `assets/js/fcmRecordsNextEventRanking_v83.js`, `fcmRecordsNextFunzioni_topflop_v83.js`, `fcmRecordsNextCalciatori_v83.js` — classifica eventi, separazione ricerca, profili.
- `assets/RecordsNext/top_flop.html`, `calciatori.html`, `top_flop_v83.css` — pagine native; gli asset condivisi v79 sono riusati, non ricalcolati in JavaScript.
- `tools/Verifica-RN4-Integrazione-v83.py` — controllo RAW FCM vs JS nativo, riprende oracoli FCA v81 senza rifare audit.
- `src/test/...` e `tools/Genera-Bibbia-RecordsNext-v83.py` — regressioni e Bibbia rigenerabile.

## Limiti della verifica

Il pacchetto non include il `recordsnext.db` reale e non puo eseguire qui un test end-to-end sui 21 archivi. Sono stati eseguiti **javac** sui sorgenti principali e test sintetici Java/Node. I report RAW FCM e browser reale restano **da acquisire** sul PC.
L'audit v81 ha gia provato 205.031 voti FCA, 240.096 COUNTED (v78), 16.264 righe giocatore/stagione, Simeone 11 annate e TF20 Carnesecchi 1 assist / 0 gol.
Le famiglie classici, serie, riserve d'ufficio, modificatori, soglie/fortuna e Culometro **non sono ancora certificate da oracoli RAW indipendenti** in questo pacchetto. Le relative implementazioni esistenti non sono state alterate arbitrariamente; la parita resta un **gate bloccante per proclamare release finale**.
Due file esterni RecordsNext `persjs/fcmAlmanaccoDati.js` e `persjs/fcmTuttiGiocatoriDati.js` superavano 1.500.000 byte nell'audit v81; non attribuirli al publisher senza prova di proprieta.
558 eventi FCA senza voto valido richiedono esame per tipo di record; non vanno cancellati ne interpretati come zero.

## Sequenza di accettazione

1. Preflight stato Git, SHA originali v81 e backup. NON applicare se file sorgenti divergono dallo snapshot senza controllo.
2. Installare la singola integrazione con `Installa-RN4-Integrazione-v83.ps1 -Mode Install` in `D:\DEV_APPS\downloads`.
3. Lanciare `mvn test`, verifica Java e Node e genera/verifica/pubblica da GUI nella sandbox XAMPP, senza toccare produzione.
4. Eseguire verifica read-only con oracoli v81 e database reale. Rapporti e ZIP `RN4_COLLAUDO_RISULTATI_v83.zip` direttamente in `D:\DEV_APPS\downloads`.
5. Collaudare TF20, Simeone, Cristante, Klose, 4 skin, filtri, scorecard, cutoff precedenti, 15 indicatori e dimensioni JS.
6. Soltanto con gate RAW completi e senza regressioni, commit sul branch autorizzato e aggiornamento changelog/diario/Bibbia. Il commit non viene simulato nel pacchetto.

**Stato documento:** implementazione candidata e test locali; verifica e consolidamento Windows/Git ancora aperti.
