# RecordsNext 4.0 — CONTINUITÀ OPERATIVA v92

Data: 18 novembre 2026. Questa nota amplia la catena v82 → v81 → v85 → v86 → v87 → v88 → v89 → v90 → v91. Non sostituisce il dossier integrale v82.

## PERCORSI AUTORITATIVI

- RC nativa in esercizio: `D:\DEV_APPS\RecordsNext4.0_RC_v64`. NON e' un repository Git.
- Worktree Git separato e pulito prima di v86: `D:\DEV_APPS\downloads\RN4_GITHUB_WORK_v63_20261116_174243`, remoto `https://github.com/mauz79/RecordsNext2.git`, branch `feature/recordsnext4-v67-rc-gui-migrated`, commit noto `3e8d53f80e2f8b9c7d8be7a7f26b8fa47339d921`.
- Sandbox 2026/27: `D:\DEV_APPS\RecordsNext4.0_TEST_v37\data\publish-sandbox-v52\sites\2026_2027`.
- Collaudo XAMPP: `D:\XAMPP`, `http://localhost/rn4-collaudo/RecordsNext/top_flop.html` e `calciatori.html`.
- TUTTI i file di scambio/rapporti da caricare: `D:\DEV_APPS\downloads`.
- Il sito operativo su `E:` NON deve essere modificato durante questa revisione.

## BASELINE INSTALLATA PRIMA DI v92

- Integrazione v86 installata: 34 file; backup `RN4_BACKUP_v86_20261117_235302`.
- Compilazione e JAR verificato: `target/RecordsNext.jar`, SHA256 `8bfefbf892a54ad80200176c398dd15b917867d1be92abcda8358ad7ddb7fcdf`.
- GUI su 21 stagioni: preflight 87/87, generazione 482 JS, 0 pubblicati, dimensione singolo JS entro 1.500.000 byte.
- Verifica staging v88: 0 errori, 0 JS oltre limite; 240.096 COUNTED, 35.111 contesti, 16.264 righe FCA, 80 ancore voti e 906 righe TF20 nel campo di controllo. NON equivale a certificazione integrale dei record storici.
- v91: 482 JS copiati in sandbox, 7/7 risorse HTTP 200, 0 scritture produzione. Backup: `D:\DEV_APPS\downloads\RN4_SANDBOX_BACKUP_v91_20261118_001655`.
- Classici, serie, RU, modificatori, soglie, fortuna, Culometro sono funzionalità operative dalla 3.2. Non riaprire audit completi senza un difetto concreto.

## v92 — CORREZIONI RICHIESTE E REALIZZATE

1. CSS e layout fluido (1400px e mobile) con filtri nascosti quando non pertinenti.
2. TF20-G gol portieri e TF20-A assist portieri, due voci separate di classifica, TF17 rigori parati come terza; stesso TF20 Java, nessuna duplicazione dati o 88esimo record nativo.
3. Colonne specifiche per record; evitare colonne costantemente `null` o assenti; non confondere zero e mancanza.
4. Archivio calciatori: tabella riassuntiva compatta, riga espansa SOTTO ogni riga squadra × stagione; 15 indicatori nel riepilogo e nella singola competizione, senza ricalcolo di punteggi.
5. Aggiornamento della pipeline di pubblicazione Java ai JS v92 e al CSS v92; due pagine HTML condividono i medesimi shard.
6. Bibbia canonica v49 e generatore versionato; changelog e diario v92.

## Test e limiti

Browser Chromium headless su fixture sintetiche: OK; Java 21 compila contro classi v86; regressione Node v83 OK. Test Windows Maven, lettura dei dati reali in pagina e verifica quattro skin PENDING. Nessun commit Git ancora creato. Il pacchetto `RN4_REVISIONE_INTERFACCE_v92.zip` fornisce un installatore Python in sola RC+sandbox con backup, senza query o modifiche a SQLite.

## SEQUENZA FUTURA

Eseguire un solo deploy v92 sul PC e caricare il rapporto `RN4_ESITO_INTERFACCE_v92_*.zip`, direttamente in downloads. Poi verificare TF20-G (0 gol stagione 2026/27), TF20-A (Carnesecchi 1 assist), TF17, Cristante, Simeone, Klose, 15 metriche e le 4 skin. SOLO DOPO un collaudo reale approvato aggiornare Git/Bibbia e chiudere il progetto. Non ripetere import, rigenerazione o audit 3.2 già riusciti.
