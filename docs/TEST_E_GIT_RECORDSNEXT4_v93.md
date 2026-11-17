# Test e Git RecordsNext 4.0 - v93

## Evidenze Windows verificate

- Install v86 34 file, backup; build del JAR SHA256 `8bfefbf892a54ad80200176c398dd15b917867d1be92abcda8358ad7ddb7fcdf`.
- GUI: 21 stagioni, 87/87 record, 482 JS <=1.500.000 byte, nessun publish operativo.
- Staging v88: 0 errori/avvisi nel perimetro controllato, non certificazione integrale.
- Sandbox v91: 482 JS + 2 HTML pubblicati, HTTP 7/7, nessun sito reale toccato.
- Rapporto v92: status DEPLOYED, 16 file, `build=PASS`; Maven su RC: Tests run 67, Failures 0, Errors 0, Skipped 0; JAR SHA256 invariato; HTTP 200 per due HTML, CSS e due JS, 0 scritture produzione, Git e SQLite.
- `browser_real` nel rapporto v92 = `PENDING`, con riscontro preliminare utente positivo; evitare di inventare una certificazione completa.

## GitHub

- Remote `https://github.com/mauz79/RecordsNext2.git`, branch `feature/recordsnext4-v67-rc-gui-migrated`, base `3e8d53f80e2f8b9c7d8be7a7f26b8fa47339d921`.
- Script v93: controlla SHA256 **42 file** di RC versionati v86/v92, codice e test; aggiunge 8 documenti/strumenti v93. Nessuna copia ricorsiva dell'installazione. No DB, player-identities, configs, archive, reports generated, site data o jar.
- Fasi: `preflight` read-only; `stage` backup e Git add whitelist; `commit` esegue `mvnw.cmd -B -ntp clean test package` dal worktree e crea commit; `push` confronta branch remoto e usa push fast-forward senza force.
- La prova finale e' `RN4_ESITO_GITHUB_v93_*.zip` in downloads, con hash commit e stato remoto. Prima del suo esito Git resta PENDING.
