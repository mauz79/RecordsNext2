# RecordsNext 4.0 - consolidamento v67 (RC, non produzione)

Data consolidamento: 2026-11-16T18:26:14+01:00. Ambiente: `D:\DEV_APPS\RecordsNext4.0_RC_v64`.

## Risultati verificati

- Motore v58c: 87/87 record, 21 stagioni elaborabili, 339 JS staging; limite massimo 1.500.000 byte per JS (massimo corrente: 977154).
- Sandbox v62b: 21 siti, 3.730 JS, 1.617 shard, cutoff e SHA-256 verificati; frontend TopFlop v53b staticamente integrato in 2025/26 e 2026/27.
- GitHub: checkpoint **gia pubblicato** nel branch `feature/recordsnext4-v63-sandbox-validated`, commit `3b008a8`; Maven 58/58 test passati nel commit v63b.
- **Migrazione v64b completata**: `D:\DEV_APPS\RecordsNext4.0_RC_v64` e' copia RC autonoma dalla TEST e conserva le configurazioni della 3.2 per 21 stagioni; nessun nuovo import richiesto.
- `2005_2006`: stagione manuale, senza FCM; e' esclusa dalla elaborazione/pubblicazione.
- FCA 2012/13 corretto: `E:\FCM\data\ArchivioA2012SerieAmauz_G1_RECUPERATA_v1.fca`, SHA-256 `e3c34b941aedb35951ba030701fe47072afa00941f7fa7884d0658442cfcf766`; SQLite RC 9.936 voti, 255 in G1. **Non sostituirlo con FCA precedente della 3.2.**
- Intervento GUI v65: la card Configurazione stagioni apre al primo clic su tutta la superficie; apertura configurazione usa metadati archiviati e non rilegge 21 FCM con UCanAccess.
- Intervento GUI v66: rimosse diciture "sviluppo" e "dev"; card 2x2 flessibili senza tagli; menu laterale coerente con tutte le quattro card (Configurazione stagioni, Famiglie record, Top & Flop, Soglie e Culometro).
- Java Dashboard SHA-256 `7129a3ece37f2e410bd7dec81bb7e3812476e9aeb14a629cc4ac40eccfa4df6e`; dialog configurazione SHA-256 `211f0319b9eaf9740e116a0c3f6b27c318fae8110c69317a90873c36766585af`.
- Pubblicazione automatica RC: **disattivata** nel processing.json e nella GUI. SQLite verificato `quick_check=ok`.

## Non ancora certificato

- Collaudo funzionale/visivo finale della GUI v65/v66: il controllo sorgenti e i report locali non equivalgono all'approvazione di tutte le interazioni.
- Collaudo browser reale di `recordsnext.html` e `RecordsNext/top_flop.html` in almeno due skin: JS caricati, filtri, link, errori in console, compatibilita' con le pagine della 3.2.
- Backup e rilascio effettivo dei siti `E:` dalla **GUI 4.0**; verifica successiva e piano rollback della 3.2. **Non eseguiti in questa procedura.**

## Continuita' operativa e prossime operazioni

1. Avviare **solo** `D:\DEV_APPS\RecordsNext4.0_RC_v64\Avvia-RecordsNext4-RC-v64.ps1`; verificare le 22 stagioni (21 elaborabili), le quattro card/menu e che 2012/13 punti all'FCA recuperato. Non premere Elabora/Pubblica prima del rilascio.
2. Testare nel browser le pagine sandbox `D:\DEV_APPS\RecordsNext4.0_TEST_v37\data\publish-sandbox-v52\sites\2026_2027\recordsnext.html` e `...\RecordsNext\top_flop.html`, oltre al 2025_2026. Esaminare console e comportamento skin.
3. Usare la GUI per il passaggio reale 3.2->4.0 solo con backup dei file preesistenti, destinazioni ricontrollate e rollback predisposto. Non ripetere l'associazione di 21 stagioni o l'import FCA 2012/13.
4. GitHub: il branch v63 e' base stabile; il consolidamento GUI v67 va su un **nuovo branch** e non altera il branch v46 congelato.

## Integrita' e riferimenti

- Report migrazione: `D:\DEV_APPS\RecordsNext4.0_RC_v64\REPORT_MIGRAZIONE_RC_v64.json`.
- Staging: `D:\DEV_APPS\RecordsNext4.0_RC_v64\data\site-export-staging\records2026_20261116_003207_9f9fad09\js`.
- Bibbia v46 (base codice): SHA-256 `cab3e184887d86f235363973c05bb35a7fa163e93f70c2555f37e936da8e82d1`.
- Report v65: `docs/VERIFICA_GUI_CONFIG_v65.md`.
- Report v66: `docs/VERIFICA_GUI_DASHBOARD_v66.md`.
- Modifiche reali ai sorgenti e certificazione di output riportate nei documenti v46 + v47; nessuna correzione FCA/FCM/SQLite eseguita dal consolidamento.
