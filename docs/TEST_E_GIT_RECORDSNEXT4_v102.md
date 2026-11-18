# RecordsNext 4.0 — Test e Git v102

- Pipeline Java v102 costruita per sostituzione mirata partendo dal sorgente *esatto* v92 SHA `12a1b660...`, non dalla v86.
- Diff controllato: due soli nomi JS aggiunti nell'elenco del publisher. Metodi e CSS v92 preservati.
- JS e test regressione v101 riutilizzati intatti, checksum manifest.
- Test eseguibile: `node tests/regression_v101.js assets/js/fcmRecordsNextPlayerProfile_v101.js`.
- Il programma di installazione esegue Maven `test package` sulla RC Windows e crea backup verificabili; se fallisce ripristina RC/JAR/sito.
- Il preflight verifica anche la destinazione del sito in SQLite in modalita' read-only.
- Commit remoto attuale `5c10ec5...`; **nessun commit/push v102 ancora eseguito**.
