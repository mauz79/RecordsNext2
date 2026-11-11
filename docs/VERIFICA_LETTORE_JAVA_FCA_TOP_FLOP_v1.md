# RecordsNext 4.0 - Verifica lettore FCA JDBC v1

Verifica locale 2026-11-01 contro **due distinti SQLite originali** provenienti da `RecordsNext2.0.zip`, Java 21, JDBC SQLite incluso nel JAR originale di RecordsNext 3.2.

- Compilazione del modulo `TopFlopFcaJdbcReader` insieme alle quattro classi TopFlop gia presenti: superata.
- Test Java di lettura completa, stagione per stagione, sui 21 FCA del DB principale e del sandbox: superati.
- Inventario indipendente `SELECT count(*)` sulle tabelle raw per ciascuna stagione: coincidente con il numero di fatti letti; no duplicati incompatibili osservati.
- Anagrafiche anche senza righe giornata: conservate in `SeasonLoad.playersByLocalId`.
- Lautaro 2025/26: 38 giornate, 30 voti validi, 9 stagioni per codice storico. Codice 12302: tre stagioni, senza risoluzione identita automatica.
- Valori zero, -1 e speciali preservati senza interpretarli come voto valido o gol/bonus impliciti.
- Rifiuto di stagione non valida verificato.
- Nessuna modifica al database nelle prove; nella verifica Windows il runner ricalcolera SHA-256 prima/dopo per entrambi gli archivi.

**Limite**: la validazione di questo modulo riguarda FCA; i test Java/JUnit completi e le prove d'integrazione pipeline 3.2 non sono ancora effettuati. Le classifiche e l'export Top & Flop non esistono ancora.
