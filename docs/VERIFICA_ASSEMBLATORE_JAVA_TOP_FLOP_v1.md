# Verifica assemblatore Java Top & Flop v1

## Esito sulla copia SQLite

- Compilazione Java 21 dell'assemblatore e degli adapter esistenti: OK.
- Test 2006/07, 2023/24, 2025/26, 2026/27 sul sandbox: OK; `realDays=110618`, `fantasyFacts=85550`, `rosterEvents=3076`, `YouthExcluded=5250`.
- Tesseramenti univoci nel campione: 3071, rispetto a 3076 eventi `Tesserato`, preservati integralmente.
- Test separato 2019/20: OK (39750 fatti fantasy e 27968 giornate FCA).
- Il test integrale di tutte le stagioni nello stesso processo ha raggiunto 17/21 stagioni, poi il processo ha superato il limite temporale: **non dichiarato superato**. Le stagioni residue saranno verificate per lotti successivi.
- Solo lettura, nessuna modifica al DB o alla pipeline.
- Verifica definitiva con JDK e database Windows: in attesa dell'output dell'utente.


## Test additivo su 2026/27 aggiornato (2026-11-02)

- La classe `TopFlopJdbcArchive2026SmokeTest.java` verifica la copia RAW aggiornata: 28.956 fatti FCA, 7.650 fatti FCM, 813 eventi `Tesserato`, 812 membership, con due tesseramenti Varela/FortisSIMY (699, 752).
- Verifica che la giornata Serie A degli incontri fantasy nello snapshot vada da 3 a 9.
- Questo test richiede esplicitamente lo snapshot importato 2026/27 e NON deve essere eseguito sul vecchio DB 3.2 con dati 2026/27 non aggiornati.
