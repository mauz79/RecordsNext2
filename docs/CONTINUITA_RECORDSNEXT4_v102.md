# RecordsNext 4.0 — Continuita' v102, 18/11/2026

## Stato conservato
- RC locale `D:\DEV_APPS\RecordsNext4.0_RC_v64`, JAR compilato, 21 stagioni.
- Git worktree `D:\DEV_APPS\downloads\RN4_GITHUB_WORK_v63_20261116_174243`, remote `mauz79/RecordsNext2`, branch `feature/recordsnext4-v67-rc-gui-migrated`, commit remoto verificato `5c10ec5b729783747c5a8d33f17b5be84f70de74`.
- Sito reale locale `E:\fantacalcio\Lega2026`: migrazione v96 completata, generazione GUI successiva 485 JS pubblicati.
- Higuain: 8 stagioni unite e verificate in indice/history, risultato v98.
- GUI identita': 2.841 approvazioni, 0 codici da decidere, nessuna proposta esatta ancora da approvare; 349 in sospeso (non approvare automaticamente).
- Due JS del sito sopra limite appartengono ad altro plugin: accettati dall'utente; limite 1.500.000 byte per ogni JS RecordsNext rimane.
- Moduli classici/serie/RU/modificatori/soglie/fortuna/Culometro funzionanti dalla 3.2: non riaprire audit senza anomalie.
- Renewo escluso esplicitamente da questo lavoro.

## Difetto e correzione
Il v101 preflight/install si e' fermato su `RecordsNextPipeline.java` SHA `12a1b660...` per baseline errata (`47bafda8...`, v86). Non e' stata installata la v101. `RN4_CORREZIONE_PUBLISHER_v102` usa la vera v92 come base, aggiunge due JS v101 ed evita regressioni.

## Sequenza rimanente
1. Con GUI chiusa, estrarre v102 in `D:\DEV_APPS\downloads`, eseguire `RN4_Finisci-Frontend-v102.py preflight`, `install`, `verify`.
2. Controllare la scheda pubblicata su `http://localhost/rn4-produzione/RecordsNext/calciatori.html` con `Ctrl+F5` e stati dei punti.
3. Non rigenerare le 21 stagioni per questa modifica frontend; la pipeline aggiornata usera' i file nelle prossime elaborazioni.
4. Trattare le carriere ancora divise separatamente dopo un esame del registro attuale, senza approvazioni automatiche. Non usare il CSV v98 come 328 difetti certi.
5. Consolidare v102 in Git solo dopo esito Windows e aggiornare Bibbia/generatore/changelog/diario con esito reale.

## Limitazioni
Il collaudo Windows v102 non e' presunto. Non dichiarare pubblicata la versione v102 prima del suo `INSTALL_OK` e `VERIFY_OK`.
