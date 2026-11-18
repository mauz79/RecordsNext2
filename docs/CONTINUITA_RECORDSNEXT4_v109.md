# RecordsNext 4.0 — continuità v109: carriere storiche (18/11/2026)

## Baseline verificata dai log reali dell'utente

- Plugin operativo: `E:\FCM\plugin\Mauz_strom2026\RecordsNext` (JAR in root).
- RC sorgenti/build: `D:\DEV_APPS\RecordsNext4.0_RC_v64`.
- Git worktree separato: `D:\DEV_APPS\downloads\RN4_GITHUB_WORK_v63_20261116_174243`, branch `feature/recordsnext4-v67-rc-gui-migrated`, HEAD/remoto `a16f21ffdd045ff88ad26317a3b594737783c60c` (come verificato dall'utente).
- 21 siti locali pubblicati (2006/07–2026/27), 5.417 JS; gli ultimi output potrebbero essere caricati sul web via FTP. STOP a ogni upload fino al gate carriere.
- La vecchia 3.2 è stata eliminata dopo `RN4_V106_3_2_ELIMINATA`. Non reinstallarla.
- Installer `RecordsNext_4.0.0_SETUP.exe` compilato e smoke test Windows OK; release/tag ufficiale ancora **SOSPESI**.
- Audit Windows v108 (18/11/2026): `gruppi=3139`, `non_unificati=328`, `errori_indice=0`, `history_mancanti=0`, `RELEASE_GATE=BLOCKED`. Non confondere presenza di History con identità unificata.
- GUI v101 già vista: `approvazioni=2841`, `codici da decidere=0`, `349 proposte in sospeso`. Le 349 proposte NON sono direttamente le 328 carriere del v108.
- Higuaín (8 annate) e Simeone (11) erano già stati verificati. Conservare tutte le decisioni precedenti.

## Scopo e limite v109

L'utente ha chiesto di riconciliare **dalla GUI del plugin**, non manipolando il JSON con uno script esterno. Si aggiunge nella finestra esistente `Top & Flop` → `Riconcilia identità storiche dei calciatori` il pulsante `Carriere spezzate (audit v108)`.

La nuova finestra:

1. legge **solo** il database SQLite operativo, con `PRAGMA query_only=ON`;
2. raggruppa per data di nascita + cognome + primo nome normalizzati, come v108; tale criterio produce **candidature**, non prove definitive;
3. evidenzia stati `NON COLLEGATA`, `COLLEGAMENTO PARZIALE`, `CANONICHE DISTINTE`, `COLLISIONE STAGIONE` e gruppi `BLOCCATA` per conflitto;
4. permette **selezione multipla** dei soli gruppi senza sovrapposizioni né conflitti approvati, richiede checkbox e conferma dettagliata; nessuna autoapprovazione all'apertura;
5. chiede una doppia conferma **individuale** per unire canoniche già distinte, con protezione delle decisioni `PERSONE DISTINTE`;
6. impedisce l'unificazione di due anagrafiche della stessa stagione;
7. salva atomicamente il solo `config/player-identities.json`, preceduto da backup `player-identities-PRE_v109_*.json` nel plugin. Non modifica righe FCA/FCM o SQLite.

Lo script di installazione `RN4_Installa-GUI-Carriere-v109.py` modifica SOLO due sorgenti Java nella **RC** e SOLO il JAR eseguibile nel **plugin** dopo Maven `test package`; crea backup completo del codice/JAR in `D:\DEV_APPS\downloads`, verifica SHA e offre rollback. Non pubblica siti, non cambia registri identità.

## Sequenza obbligatoria

1. Chiudere la GUI, estrarre ZIP v109 in `D:\DEV_APPS\downloads`.
2. `python RN4_Installa-GUI-Carriere-v109.py preflight`, poi `install`, poi `verify`.
3. Avviare `E:\FCM\plugin\Mauz_strom2026\RecordsNext\RecordsNext.bat`.
4. Top & Flop → Riconcilia identità storiche dei calciatori → `Carriere spezzate (audit v108)`.
5. Rivedere l'elenco, selezionare gruppi senza collisione, confermare esplicitamente. Non forzare gli stati bloccati. Gli eventuali gruppi con canoniche distinte richiedono conferma individuale; le collisioni stessa stagione sono eccezioni da documentare.
6. **Prima di ogni nuovo upload FTP** chiudere finestre, eseguire `Elabora` in modalità Consolidata con pubblicazione della stagione corrente abilitata; quindi `Debug e strumenti` → `Pubblica i siti delle stagioni selezionate`.
7. Rieseguire **solo** `RN4_Audit-Carriere-Pubblicate-v108.py` già disponibile e leggere il nuovo conteggio + stato delle eccezioni. Non ripetere audit numerici FCA/FCM precedenti.
8. Registrare i risultati, decidere le eccezioni reali, poi autorizzare upload online e riprendere la release v107b. **Non dichiarare chiuso finché il gate carriere non è risolto con eventuali eccezioni documentate.**

## Test eseguiti in ambiente isolato

- Compilazione `javac --release 21` della nuova finestra e della GUI attuale: OK.
- Test Java `TestCareerApprovalV109`: approvazione, protezione da stagioni sovrapposte, ripristino in memoria: OK.
- Test Python `test_install_v109.py`: preflight, backup, installazione, verify, rollback su cartelle/JAR sintetici: OK.
- Test Python `test_installer_fail_v109.py`: Maven fallito, ripristino automatico della vecchia versione: OK.

**NON** ancora provato sul database reale Windows della 4.0: la verifica vera avverrà nella GUI operativa dopo l'installazione.
