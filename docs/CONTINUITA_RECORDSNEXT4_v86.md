# RecordsNext 4.0 — CONTINUITÀ OPERATIVA VERIFICATA v86

**Data di riferimento:** 17 novembre 2026, dopo acquisizione del pacchetto reale `RN4_CONTESTO_PC_v85_20261117_233918_22252.zip`.
**Stato della versione v86:** candidato di integrazione nativa Java/GUI/JS/HTML; test offline eseguiti, **NON installato né certificato su Windows**. Stato Git sul PC v85: fermo a v67.
**Non cancellare né rigenerare da zero la Bibbia precedente; non usare v83/v83b.**

## COME SI RIPARTE NELLA CHAT SUCCESSIVA

Allegare **solo `RN4_CONSOLIDAMENTO_v86.zip`**; al suo interno ci sono questo documento, il dossier v82 originale, tutto lo snapshot PC v85, l'integrazione candidata v83b a fini storici, Bibbia v48 candidata, generatore, sorgenti aggiornati e i riferimenti agli oracoli v81. Leggere questo file, `DOCUMENTO_MAESTRO_RECORDSNEXT4_v81.md`, `CONTESTO_INTEGRALE_RICOSTRUITO_RECORDSNEXT4_v82.md` e poi il codice in `RC/` prima di proporre modifiche. **Non richiedere ulteriori ZIP già inclusi.**

## POSIZIONI WINDOWS: VERIFICATE DAL PC, NON IPOTIZZATE

| Componente | Percorso reale |
|---|---|
| RC operativa (NON una repository Git) | `D:\DEV_APPS\RecordsNext4.0_RC_v64` |
| Database SQLite | `D:\DEV_APPS\RecordsNext4.0_RC_v64\data\database\recordsnext.db` |
| Registro identità approvate | `D:\DEV_APPS\RecordsNext4.0_RC_v64\config\player-identities.json` |
| Worktree Git pulito (separato dalla RC) | `D:\DEV_APPS\downloads\RN4_GITHUB_WORK_v63_20261116_174243` |
| Repository GitHub | `https://github.com/mauz79/RecordsNext2.git` |
| Branch worktree | `feature/recordsnext4-v67-rc-gui-migrated` |
| HEAD worktree v85 | `3e8d53f80e2f8b9c7d8be7a7f26b8fa47339d921` |
| Sandbox sito | `D:\DEV_APPS\RecordsNext4.0_TEST_v37\data\publish-sandbox-v52\sites\2026_2027` |
| Server | `D:\XAMPP` |
| Rapporti/allegati che l'utente dovrà caricare | `D:\DEV_APPS\downloads` |
| FCA 2012/13 recuperato | `E:\FCM\data\ArchivioA2012SerieAmauz_G1_RECUPERATA_v1.fca` |
| Sito FCM attuale precedente | `E:\fantacalcio\Lega2025` |

**NON USARE `git -C` sulla RC.** Il flusso Git corretto era documentato in `tools/Esegui-RN4-Consolida-v67b.ps1` e nel pacchetto `RN4_GITHUB_RIPRESA_v67b`. Il worktree Git è distinto. `RecordsNext4.0` è un'altra directory con branch `feature/recordsnext4-players`, HEAD `f9bf303`, molte modifiche locali e file non tracciati: **non usare come base di commit, non pulire/reset**. Esistono anche `RecordsNext2.0`, `RecordsNext2.0_SANDBOX`, `RecordsNext4.0_TEST_v37` e `RecordsNext4_PUBLISH_v46`; v85 ne ha acquisito file ma non sono la RC canonica.

## INVENTARIO REALE V85, VERIFICATO SU CONTENUTI ACQUISITI

- Il pacchetto v85 contiene **2.975 file** ZIP (stato e snapshot), circa **112,8 MB** estratti; 575 file copiati dalla RC e 516 dal worktree Git.
- Confronto per percorso e SHA256 fra i file acquisiti della RC e quelli del worktree: **510 identici, 6 diversi, 59 presenti solo nella RC, 0 solo nel worktree**. Inventario completo `RC_vs_GIT_v86.csv`.
- I **6 diversi**: `config/league.json`, `config/processing.json`, `config/recordsnext-gui.properties` e tre test JS `tools/Test-RecordsNext4TopFlopCompactBrowser-v2.js`, `tools/Test-RecordsNext4TopFlopFacade-v1.js`, `tools/Test-RecordsNext4TopFlopPublicViewsBrowser-v1.js`. Gli ultimi tre possono differire per contesto di collaudo: confrontare prima di riportarli in Git.
- Le **59 aggiunte RC** comprendono `config/player-identities.json`, `config/topflop.json`, evidenze identità, audit e i sorgenti aggiunti di prova v69/v73: **non copiarle indiscriminatamente nel repository**. Non pubblicare configurazioni locali, evidenze private, database o log.
- La directory sandbox acquisita selettivamente include `top_flop.html`, ma **non** `calciatori.html` al momento v85. La Bibbia canonica RC v47 esiste, la v48 non era installata.
- Lo snapshot PC contiene `ANALISI_TABELLINI_RUOLI_v78.json` (1.849.572 byte), `AUDIT_SCHEDA_v79.json`, `ECCEZIONI_PUNTI_v79.json`, `ESITO_TABELLINI_RUOLI_v78.md`: non richiedere questi file.
- Browser/XAMPP disponibili in Windows; Git, Java, javac, Python 3.13 e `mvnw.cmd` sono disponibili. `node` e `mvn` **non erano nel PATH**: la procedura v86 non presume Node presente.
- La scansione dimensioni dei `js/` nella sandbox (726 file; max `fcmRecordsNext_Series.2020_2021.js` 977.154 byte) rispetta il limite. Il vecchio audit v81 aveva **2 file in `persjs/` oltre 1,5 MB**, non attribuiti automaticamente a RN4: quell'altra directory non è coperta dal solo CSV `js/` v85.

## BIBBIE E GENERATORI — FONTE CANONICA

- `D:\DEV_APPS\RecordsNext4.0_RC_v64\docs\CODICE_FUNZIONANTE_RECORDSNEXT4_v47.md` = Bibbia **installata canonica**; 2.336.740 byte acquisiti.
- `D:\DEV_APPS\RecordsNext4.0_RC_v64\tools\Create-RecordsNext4WorkingCodeMd-v47.py` = generatore canonico v47, presente identico nel worktree Git. Preservarlo.
- I generatori storici v2–v46 e Bibbie v10–v46 sono negli snapshot v85; non sono la fonte da cui rigenerare la v48.
- La `v48` di v83b **non è installata**. In questo pacchetto la candidata v48 è stata rigenerata dalla v47 completa e dalle **fonti consolidate v86**, mediante `RC/tools/Genera-Bibbia-RecordsNext-v86.py`. Il file di output è `RC/docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v48.md`. La v48 è candidata finché non si completa il gate Windows; non chiamarla «release definitiva».
- Aggiornare generatore e Bibbia **insieme**, mantenendo vecchi file versionati e report necessari. Nessun auto-commit Git o push in v86.

## EVIDENZA NUMERICA PREESISTENTE — NON RIFARE AUDIT ESEGUITI

- 21 stagioni 2006/07–2026/27, 205.031 voti FCA positivi (audit reale v81), 16.264 righe calciatore/stagione.
- 240.096 contributi FCM `COUNTED` e 35.111 contesti da audit reale v77–v79; su 240.096 casi **`TOT−VOTO=MODIF`** (v78). L'audit v79 registra **32.488 contesti riconciliati completamente**, **5.507 tabellini con delta non ricostruito** e **11 ambigui**. La mancata ricostruzione NON rende il saldo FCM ignoto.
- Vero TF20 2026/27: nessun gol dei portieri, **1 assist di Carnesecchi Marco (`2026_2027:136`)**. La pagina v79 mostrava 87 portieri tutti al primo posto con zero gol. Non ricreare quel difetto.
- La FCA 2012/13 corretta ha SHA256 `e3c34b941aedb35951ba030701fe47072afa00941f7fa7884d0658442cfcf766`, 9.936 voti, 255 voti alla prima giornata; non sovrascriverla.
- 558 eventi FCA senza voto valido v81: **avviso da classificare per metrica**, non da convertire in voto zero né errore generale.
- Identità storiche: solo registro approvato, nessun merge nome/codice implicito. Simeone 11 stagioni approvate (2016/17 codice diverso); Cristante scheda complessa; Klose ricercabile anche se assente dalla stagione corrente.

## VINCOLI DA RISPETTARE

- Prodotto RecordsNext by mauz79; JS sono output principale; GUI **un unico** flusso Genera/Verifica/Pubblica. Le scelte di competizione sono lato HTML.
- `top_flop.html` = solo classifiche TF01–TF20, una ricerca di filtro; `calciatori.html` = ricerca storica globale e schede. Entrambe stesse fonti con shard e cutoff sito. Classifiche integrali, ordinamenti, Top 1/10/20/50/Tutti.
- Scheda contesto stagione × playerKey × fantasquadra, 15 indicatori: FCM TOT medio, VOTO medio, gol, assist, gol rigore, rigori sbagliati, ammonizioni, espulsioni, autogol, gol subiti, di cui rigore, rigori parati, **bonus punti**, **malus punti**, saldo `SUM(MODIF)`; dettaglio per competizione, metadati e badge saldo annuo max/min. Non confondere punti con numero eventi.
- Coefficienti sempre validi: gol +3, assist +1, rigore parato +3, ammonizione −0.5, espulsione −1, autogol −2, gol subito −1. Su rigore segnato +2 o +3, sbagliato −2 o −3 secondo stagione/regolamento. Split bonus/malus **solo univoco e riconciliato al MODIF**; altrimenti null/—, saldo osservato comunque presente.
- FCA giorno reale deduplicato per atleta e giornata; FCM solo prestazioni COUNTED, anche su più competizioni. Gol su rigore e gol subito su rigore sono sottoinsiemi, non eventi aggiuntivi. Non inventare rosa posseduta alla data per panchine/tribune. Zero ≠ null.
- 4 skin: Mauzstrom, Fantablue2, Maelstrom, Neutral. JS singolo max **1.500.000 byte reali**, meglio 1 MB, con shard. No `source=chatgpt` o `utm_source=chatgpt` negli URL.
- I siti storici rispettano il cutoff dell'ultimo FCM effettivo di ciascun sito, non necessariamente quello attuale 2026/27.

## INTEGRAZIONE CANDIDATA V86 — FATTO VERIFICABILE

- Provenienza: payload v83b non installato, corretto nella **stessa unica integrazione** v86. File Java nuovo `TopFlopIntegratedV83.java` (nome di classe mantenuto per compatibilità del progetto; patch v86 documentata).
- Correzione in più rispetto al v83b: controllo duplicati FCM usa ora `(playerId, fantasyTeamId, sourceCompetitionId, matchId)`, non soltanto `(playerId, fantasyTeamId, matchId)`. Questo rispetta `TopFlopFacts.FantasyKey` e preserva apporti dello stesso giocatore in coppe distinte. Regressione Java `Rn4V86JavaSmoke` e JUnit `TopFlopIntegratedV86MultiCompetitionTest`.
- L'installatore PowerShell v83b era **sintatticamente invalido** alla riga 283 (`$family:`); v83 presum(eva) Git nella RC anziché nel worktree separato. Entrambi **sospesi e non usare**. v86 usa `Gestisci-RN4-v86.py` con Python 3.13 già presente sul PC.
- v86 preflight verifica gli hash dei file payload e la baseline v85, il branch Git separato e la presenza dei percorsi; `preflight` non tocca RC/Git/SQLite/sito. `install` usa backup versionato e rollback; `build` usa `mvnw.cmd`; `validate` usa l'oracolo SQLite in sola lettura v83 e produce esiti in downloads.
- `git commit`/push **non** vengono effettuati da v86, perché **restano gate di parità numerica scoperti**. L'utente deciderà l'esecuzione quando i controlli reali saranno superati; nessuno deve chiamare questa versione «completata» oggi.

## TEST ESEGUITI LOCALMENTE QUI E TEST NON ESEGUITI

- `javac --release 21` su **RC v85 + tutti i sorgenti integrati v83b**: OK, 291 class compilate nella prima prova; `TopFlopIntegratedV83` aggiornato v86 ricompilato.
- `Rn4V83JavaSmoke`: OK, regolamenti/split e rollback integrato di JS+HTML.
- `Rn4V86JavaSmoke`: OK, stesso matchId in due competizioni FCM distinte.
- `node tests/regression_v83.js`: OK, test sintetici TF20/15 indicatori/ricerca; NON prova dati veri.
- **Non testati in questa sandbox**: Maven JUnit sul PC Windows, flusso GUI su SQLite reale, confronto completo RAW→Java→JS→UI 21 stagioni e 4 skin, classici, serie, RU, modificatori, soglie/Culometro, pubblicazione reale Git. Questi sono gate aperti, **non** blocchi di cui si possa inventare l'esito.

## PROSSIMA AZIONE E REPORT

1. Usare **solo** il pacchetto v86. Estrarlo sotto `D:\DEV_APPS\downloads\RN4_CONSOLIDAMENTO_v86` e verificare `Gestisci-RN4-v86.py` con Python, **prima solo `preflight`**. Non usare script PowerShell v83/b.
2. L'installazione, soltanto dopo preflight `READY`, deve fare backup di ogni file cambiato e non toccare SQLite, config locali o sito. `Build` usa Maven wrapper. La GUI pubblica sito sandbox, poi `validate` crea direttamente in downloads `RN4_ESITI_WINDOWS_v86.zip` (da allegare). In assenza di output non fingere un test PASS.
3. Verificare gli oracoli TF01–TF20 RAW/Java/JS/UI con stagioni/cutoff, TF20 Carnesecchi, profili Simeone/Cristante/Klose e soprattutto classici/serie/RU/modificatori/soglie/Culometro con nuovi oracoli indipendenti **mirati**, non rifare v78/v79/v81.
4. Solo dopo gate A–E: chiusura Bibbia e generatore, README, diario, decisioni, changelog, suite test su PC, confronto commit/stato Git e commit locale/push solo autorizzato.

**Non servono altri file adesso**: questo pacchetto contiene sorgenti reali v85, storico v82, audit v81 e i rapporti v78/v79 recuperati, con indicazioni verificabili.
