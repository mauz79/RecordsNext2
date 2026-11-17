# RecordsNext 4.0 - Continuita operativa v93 (pre-consolidamento Git)

Data: 18 novembre 2026. Fonte primaria: risultato Windows `RN4_ESITO_INTERFACCE_v92_20261118_003753.zip`, pacchetto integrato v92 e catena documentale v90 -> v89 -> v88 -> v87 -> v86 -> v85 -> v82/v81. Non e' una nuova release applicativa.

## Stato reale al checkpoint

- RC Windows: `D:\DEV_APPS\RecordsNext4.0_RC_v64` (NON e' un repository Git).
- Worktree Git separato: `D:\DEV_APPS\downloads\RN4_GITHUB_WORK_v63_20261116_174243`.
- Remoto canonico: `https://github.com/mauz79/RecordsNext2.git`.
- Branch: `feature/recordsnext4-v67-rc-gui-migrated`. HEAD ultimo verificato prima di v86: `3e8d53f80e2f8b9c7d8be7a7f26b8fa47339d921`; worktree all'epoca pulito. Al consolidamento il programma deve verificarlo di nuovo.
- Sandbox: `D:\DEV_APPS\RecordsNext4.0_TEST_v37\data\publish-sandbox-v52\sites\2026_2027`.
- Sito reale su `E:\fantacalcio\Lega2026\js`: NON pubblicato in questa fase. XAMPP `D:\XAMPP`, URL `http://localhost/rn4-collaudo/RecordsNext/top_flop.html` e `calciatori.html`.
- Cartella obbligatoria per report e ZIP: `D:\DEV_APPS\downloads`.

## Risultati gia' confermati, da non rieseguire

1. v81: 21 stagioni; oracoli FCA; 205.031 voti reali positivi; 2026/27: nessun gol portieri e un assist di Carnesecchi.
2. v85: ricognizione PC e fonti storiche, file e generatori, percorsi Git/RC.
3. v86: 34 file installati, backup `D:\DEV_APPS\downloads\RN4_BACKUP_v86_20261117_235302`; Java build OK, SHA256 JAR `8bfefbf892a54ad80200176c398dd15b917867d1be92abcda8358ad7ddb7fcdf`.
4. GUI: 21 stagioni invarianti, record 87/87 generabili, 482 JS verificati, 0 pubblicati.
5. v88 staging: 0 errori/avvisi nei controlli coperti; 240.096 FCM COUNTED, 35.111 contesti, 16.264 righe calciatore/stagione FCA, 80 ancore voti, massimo JS 977.154 byte. Non e' certificazione numerica di ogni record.
6. v89: fonti per eventuale anti-regressione delle famiglie classiche acquisite; classici, serie, RU, modificatori, soglie, fortuna e Culometro SONO GIA' OPERATIVI dalla 3.2. Non riaprirli senza anomalie concrete.
7. v91: pubblicazione controllata in sandbox dei 482 JS e dei 2 HTML; risorse HTTP 7/7, produzione non toccata.
8. v92: fix UI delle due pagine: layout responsivo, filtri/colonne contestuali, TF20-G (gol) + TF20-A (assist) separati, TF17 (rigori parati) autonomo, dettagli fantasquadra aperti nella riga sottostante con 15 indicatori. Preservati engine, modello dati e 87 voci Java.
9. RAPPORTO WINDOWS v92 del 18/11/2026 00:37:53: `DEPLOYED`, `files=16`, `build=PASS`, JAR SHA256 invariato `8bfefbf892a54ad80200176c398dd15b917867d1be92abcda8358ad7ddb7fcdf`; Maven 67 test, 0 failure/errore/skipped; HTTP 200 per top_flop, calciatori, CSS e JS v92 (5/5); 0 modifiche produzione, Git e SQLite. Backup: `D:\DEV_APPS\downloads\RN4_BACKUP_INTERFACCE_v92_20261118_003746`.
10. L'utente ha comunicato che l'interfaccia v92 *sembra andare bene* e ha chiesto documentazione e comandi Git. Questo e' feedback visivo preliminare. Il report v92 conserva `browser_real: PENDING`: non attestare come completate verifiche non documentate di quattro skin, cutoff multisito o ogni classifica.

## Prossima azione unica

Consolidare v86+v92 nel worktree Git separato, con whitelist e confronto SHA256, preservando ogni versione storica della Bibbia. Documentazione v93 e Bibbia v50 devono essere versionate insieme ai sorgenti e ai test. Procedura: `RN4_GitHub-v93.py preflight`, `stage`, `commit`, `push`, una alla volta. Check Git obbligatori: commit/branch/origin attesi, clean tree iniziale, Maven test package dal worktree, nessun file privato pubblicato, no force push. Report Git finale direttamente in downloads. I documenti v93 non dichiarano anticipatamente commit o push riusciti.

## Decisioni da preservare

- FCA prestazioni reali contate una volta per atleta/giornata, FCM soltanto prestazioni COUNTED; saldo FCM certo `MODIF = TOT - VOTO`, split bonus/malus soltanto se ricostruzione univoca; `0` non equivale a `null`.
- Unione identita storiche soltanto tramite associazioni approvate; Simeone e' un caso approvato. Scheda 15 indicatori per stagione x fantasquadra e competizione, bonus/malus in PUNTI e badge saldi annui.
- Due HTML: `top_flop.html` classifica e `calciatori.html` ricerca globale; unico flusso GUI/export/publish.
- TF20-G gol e TF20-A assist sono due VISTE indipendenti su stesso dato; TF17 resta rigori parati. Non duplicare statistiche e non alterare il catalogo dei 87 record.
- Quattro skin esistenti, shard <= 1.500.000 byte reali, cutoff per sito, nessun URL con parametri source=chatgpt/utm_source=chatgpt.
- Nessuna pubblicazione sul sito operativo E: prima di consenso e verifica dedicata.

## Artefatti da allegare per riprendere in una nuova chat

Questo checkpoint Git v93 contiene: documentazione, generatore, whitelist SHA, programma Git e **ZIP v92 originale**, che a sua volta contiene v90 e la catena precedente fino a v81/v82. Include anche il risultato reale v92. Dopo esecuzione Git aggiungere soltanto `D:\DEV_APPS\downloads\RN4_ESITO_GITHUB_v93_*.zip` per documentare l'hash reale e l'esito push. Non richiedere gli archivi gia' conservati.
