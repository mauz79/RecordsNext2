# RecordsNext 4.0 — continuità v114, carriere giocatori

**18 novembre 2026.** Baseline operativa installata v113 in `E:\FCM\plugin\Mauz_strom2026\RecordsNext`, RC Java in `D:\DEV_APPS\RecordsNext4.0_RC_v64`. La v113 è stata installata con `RN4_V113_INSTALL_OK`; approvati dalla GUI Taïder (123046,124530,100558) e Neuton (123028,123130), Mbaye già unificato, Reina e le restanti quattro proposte approvate; Oussama e Anouar El Azzouzi sono persone diverse e **non vanno uniti**. L'utente ha rigenerato **21 siti**, 87/87 record e 5.475 file validi/pubblicati. Prima della v114 non toccare altro del registro identità.

## v114: migliorie richieste dall'utente durante il controllo visivo della scheda Klose

- Ruolo FCA da `GiocatoreA.RUOLO`: 1 Portiere, 2 Difensore, 3 Centrocampista, 4 Attaccante; visualizzazione in testata e nella tabella per stagione, supportando variazioni storiche di ruolo. Nessuna inferenza anagrafica.
- Due card **Punteggio TOP**/**Punteggio FLOP**, definite rigorosamente come massimo e minimo `FantasyFact.fantasyVote()` in **singola partita FCM con `Usage.COUNTED`**, includendo stagione, fantasquadra, competizione e giornata. Non sono saldi, medie stagionali o somme. I record sono raccolti una sola volta nella generazione delle viste per stagione, due occasioni al massimo per calciatore; limite per singolo JS di 1.500.000 byte invariato.
- Rimozione della colonna **Stato punti** dalla tabella principale e del messaggio omonimo nei dettagli. Il controllo interno su saldi/bonus/malus e il suo stato **rimangono** nel motore, non vengono bypassati.
- Estensione compatibile della riga compatta `Careers`: elemento [7] per estremi FCM. Java e decoder JS accettano sia le vecchie 6/7 colonne sia le nuove 8. Le stagioni nella vista storica approvata conservano `ruoloFca`, TOP e FLOP; la fusione approvata di due schede nella stessa stagione sceglie gli estremi effettivi delle due fonti, senza duplicare righe.
- URL JS dei due HTML con query di versione `?v=114` per evitare cache browser dopo la pubblicazione; nessun tracking.

## Procedura

Un solo installatore `RN4_Installa-UI-Carriere-v114.py install` (Java 21 / Maven); crea backup di sorgenti/asset/JAR/docs, compila, installa JAR e asset nel plugin **senza toccare** DB, registro identità o 21 siti. In caso di errore ripristina tutto.

**Dopo `RN4_V114_INSTALL_OK`:** dalla GUI 4.0 selezionare modalità **Consolidata**, tutte le 21 stagioni e **Elabora** una sola volta, con pubblicazione su tutti i siti locali. L'operazione rigenera dati `Careers` con nuovi campi e aggiorna HTML/JS su tutti i siti. Prima di quella rigenerazione le nuove card non hanno i dati e i JS vecchi potrebbero mostrare `—`.

La release pubblica `RecordsNext_4.0.0_SETUP.exe` preparata in v107 è precedente alle modifiche v109-v114 e **non deve essere pubblicata come release finale** prima di essere ricompilata e collaudata. Git worktree separato `D:\DEV_APPS\downloads\RN4_GITHUB_WORK_v63_20261116_174243`, branch `feature/recordsnext4-v67-rc-gui-migrated`. Non committare mai DB/identità personali o siti pubblicati.

## Esiti test locali (non sostituiscono il test nel Windows dell'utente)

- Compilazione `javac` Java 21 con i sorgenti della RC v85 e overlay v113/v114.
- Test sintetico Java per codifica/decodifica carriere, ruolo e TOP/FLOP, storico approvato e duplicati FCA.
- Test Node per decode `Careers` e calcolo TOP/FLOP su più stagioni e rimozione `Stato punti`.
- Test sintetico installazione/verify/rollback v114 che lascia DB e identità invariati.
