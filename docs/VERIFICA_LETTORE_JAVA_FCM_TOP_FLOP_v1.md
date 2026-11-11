# RecordsNext 4.0 - Evidenze FCM JDBC v1

- Base: `RecordsNext2.0.zip` fornito dall'utente, worktree 4.0 del ramo `feature/recordsnext4-players`.
- Output precedente ricevuto: `RecordsNext4_FcaJdbc_OUTPUT_v1_20261101_205632_731016.zip`, `MANIFEST.json` dichiara `stato=OK`, JDK 21, test precedenti 52, entrambi i database invariati, bibbia v16 generata. Il test Windows precedente ha letto tre stagioni per DB; il test completo FCA era separato.
- Questo intervento: compilazione Java 21 e scansione effettiva **integrale** FCM su entrambi i database della copia di sviluppo, senza scritture.
- 21 stagioni; 552.025 Formazione; 546.775 ammesse; 5.250 Youth League escluse; 14.320 Tesserato; nessun record Formazione perso nelle join; slot corrispondenti verificati.
- Test Windows 5.1 da confermare con ZIP `RecordsNext4_FcmJdbc_OUTPUT_v1_*.zip`.
- Non sono state modificate le classi della 3.2, il database, la pipeline, la GUI, il sito o le regole di competizione.
- Anche in caso di 52/52 test superati non attribuire il voto sprecato prima di classificare le vere esclusioni.
