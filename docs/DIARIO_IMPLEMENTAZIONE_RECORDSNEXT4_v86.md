# RecordsNext4 — Diario implementazione v86 (17/11/2026)

- Acquisito archivio REALE del PC `RN4_CONTESTO_PC_v85_20261117_233918_22252.zip`; nessuna nuova richiesta di file storici.
- Confrontati 575 file RC e 516 file Git: 510 identici, 6 differenti, 59 solo RC, nessuno solo Git. Branch corretto separato; RC non è git.
- Recuperate Bibbia v47 e generatore v47 canonici. Creati generatore e Bibbia candidata v48 basati sull'integrale della v47, non su uno schema breve.
- Acquisiti rapporti completi `ANALISI_TABELLINI_RUOLI_v78.json`, `AUDIT_SCHEDA_v79.json`, `ECCEZIONI_PUNTI_v79.json` e snapshot fonti e oracoli v81, senza ri-eseguire gli audit.
- Ripresa l'integrazione Java/GUI/exporter/top_flop/calciatori del candidato v83b, senza installarlo direttamente. Corretta chiave duplicati `COUNTED` includendo ID competizione; regression Java/JUnit aggiunta.
- Creato `Gestisci-RN4-v86.py` al posto dell'installatore PowerShell v83b non parsabile. Preflight read-only per RC/Git/SQLite/sito, installazione con backup, build Maven wrapper, validazione oracoli, rollback.
- Eseguite compile Java 21 e regressioni Java/Node sintetiche offline (vedere `TEST_E_GIT_RECORDSNEXT4_v86.md`). Non eseguito test 21 stagioni sul DB Windows; non certificato classici/serie/RU/modificatori/Culometro.
- Nessuna modifica al repository remoto, alla RC Windows, al database o alla sandbox Windows in questa fase.
