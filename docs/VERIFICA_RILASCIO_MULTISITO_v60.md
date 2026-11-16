# RecordsNext4 - Stato di continuita v61 / verifica rilascio v60
Rilevazione locale: **2026-11-16T00:35:10+01:00**. Ambiente TEST: `D:\DEV_APPS\RecordsNext4.0_TEST_v37`.

## Risultati verificati nei log del 16 novembre 2026
- Installazione **v58c**: build Maven, test, smoke FCA e Bibbia v44 riusciti.
- Modalita Completa: **87/87** record elaborati, **21** stagioni gestite, **339 JS** generati nello staging; **147 legacy flat**, **183 Top & Flop**.
- Limite **1.500.000 byte per JS**, massimo riscontrato **977.154 byte**.
- FCA 2012/2013: prima giornata **255** voti, totale **9.936**; import SQLite n. **59**.
- Gate **v59 SUPERATO** nel comando `prepare` delle 00:29; **10.492 FMT FCA** verificate.
- Sandbox **v60b preparata e isolamento OK** per **21** destinazioni; stagione manuale `2005_2006` esclusa correttamente per assenza di FCM.
- Comando `-Mode audit` successivo **BLOCCATO** alle 00:32: `Pubblicazione nel sito ATTIVA! Bloccare e salvare dalla GUI.`
- **NON CERTIFICATI**: distribuzione effettiva multisito, audit dei file distribuiti, pagine HTML/skin nei browser, pubblicazione reale, sostituzione della 3.2.
- **NON INFERIRE** dal messaggio del gate che siano stati o non siano stati scritti file nella sandbox: il gate ha fermato il proprio audit prima della verifica.

## Stato locale osservato dal generatore (sola lettura)
- `processing.output.publishToSite`: **`True`**.
- `recordsnext-gui.properties`: `processing.publish=false`.
- Installazione v58c: `D:\DEV_APPS\downloads\RN4_V58C_INSTALL_ESITO_20261116_000208.json` (stato `TEST_PATCH_INSTALLED_NOT_RELEASED`).
- `target/RecordsNext.jar` SHA256: `c5f01f486d3750b22abc5ca81b6568dab2be58ff1fd5943e205b813c8f4fe4d2`.
- Ultimo staging: `D:\DEV_APPS\RecordsNext4.0_TEST_v37\data\site-export-staging\records2026_20261116_003207_9f9fad09\js`. File JS `fcmRecordsNext_*.js` presenti: **339**. Dimensione massima: **977154** byte.
- Ultimo gate v59 superato: `D:\DEV_APPS\downloads\RN4_RELEASE_GATE_v59_20261116_002949.json`.
- Ultimo gate v59 bloccato: `D:\DEV_APPS\downloads\RN4_RELEASE_GATE_v59_20261116_003223.json`.
- Stato sandbox `state.json` presente; SHA256: `acd072f23ca3e1551271683f9b3fbb8845cb0b526b34a634a5b083030fa47e16`.
- Campi dello stato: `created, original, protectedHash, root, schema, selected, sites`.

## Prossima sessione: percorso di chiusura senza interventi collaterali
1. Verificare l’origine del flag di pubblicazione attiva (`processing.json` e GUI). **Non forzare/saltare il gate v59** e non modificare manualmente SQLite. Accertare che i target restino nella sandbox.
2. Quando il gate è coerente, eseguire il comando `-Mode audit` v60b per certificare i **JS distribuiti per tutte le 21 stagioni**, gli hash, il cutoff, le dimensioni e i percorsi. Non ripetere l’elaborazione Completa se non necessario.
3. Eseguire `-Mode frontend` **solo dopo l’audit positivo**, installare/verificare le skin in sandbox e collaudare nel browser le viste 3.2 e Top & Flop.
4. Verificare compatibilita delle 9 facade classiche, filtri competizioni, link tabellini e skin, con confronto rispetto all’output 3.2.
5. Solo dopo: piano di rilascio con backup, rollback e passaggio dei JS ai siti reali; poi documentazione conclusiva, commit e push GitHub nel ramo di lavoro **non congelato**.

## Vincoli invariati
- L’output primario è il set di **JS per i siti FCM**; HTML di prova non è fine a sé stesso.
- Ogni singolo JS deve restare **≤1.500.000 byte**, controllato sui byte effettivi, caricando dettaglio tramite shard su richiesta.
- La GUI governa le selezioni e la pubblicazione. **Nessuna destinazione `E:`** durante i test.
- **2005/2006** è manuale/non importabile per assenza FCM; non va artificialmente resa selezionabile.
- `D:\DEV_APPS\RecordsNext4_PUBLISH_v46` e il ramo GitHub v46 review restano congelati.
- Repository: `https://github.com/mauz79/RecordsNext2`; branch noto v46: `feature/recordsnext4-v46-review`. Lo stato del repository locale/push non è stato verificato, **NON dichiarare un commit o un push eseguiti**.
- Non modificare FCA/FCM, SQLite, JAR, Java o JS per aggiornare questa documentazione.

## Script e dati da usare per la ripresa
- Launcher: `tools/Start-RecordsNext4-Test-v46.ps1`.
- Gate: `D:\DEV_APPS\downloads\RN4_GATE_RC_v59\Esegui-RN4-ReleaseGate-v59.ps1`.
- Sandbox: `D:\DEV_APPS\downloads\RN4_SANDBOX_RELEASE_v60b\Esegui-RN4-Sandbox-v60b.ps1`, opzioni `prepare`, `verify`, `audit`, `frontend`, `gitplan`.
- Rapporti del gate v59 in `D:\DEV_APPS\downloads\RN4_RELEASE_GATE_v59_*.json`.
- Bibbia v44 **non viene sovrascritta**; v45 è la base della prossima sessione.
