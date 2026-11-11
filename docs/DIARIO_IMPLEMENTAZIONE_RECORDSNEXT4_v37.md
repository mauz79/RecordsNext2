# RecordsNext 4.0 — Diario v37 (patch di sviluppo v45)

Data: 11 novembre 2026. Destinazione: esclusivamente `RecordsNext4.0_TEST_v37` derivata dalla v43.

## Implementato nel programma Java

- `TopFlopViewsPublication.attachScoped`: genera la vista storica solo per le stagioni autorizzate dal sito, usando `Careers` e `Teams` del medesimo staging, senza pubblicare direttamente.
- `TopFlopHistoricalViews`: aggregazione reale basata esclusivamente sulle identita approvate e voti validi ponderati; aggiunge `fantasquadrePerStagione` con fantasquadra, competizione, stagione, gare conteggiate, media voto apportata, fantamedia apportata, panchina e fuori lista osservati.
- Nessuna somma universale di fantapunti o utilizzi fantasy tra squadre e competizioni: il rendimento rimane attribuito al singolo contesto.
- Dove presente nel formato piu recente, riporta i prezzi registrati FCA/FCM e le statistiche fuori campo osservate. Non deduce possesso continuativo ne punti recuperabili.
- I prezzi storici non sono normalizzati tra budget di stagioni diverse: metadato esplicito.
- `fantamediaReale` resta `null` finche non esiste un calcolo indipendente, verificato dalle regole di lega; NON si sostituisce con medie di apporto fantasy.
- ID Gazzetta e nomi non sono usati per fusioni automatiche. Il codice di approvazione resta quello installato nella v43; non si modifica `config/player-identities.json`.
- Limite per JS fisico: 1.500.000 byte; normalizzazione, SHA, rollback e pubblicazione rimangono governati dalla pipeline 3.2.

## Verifiche eseguite sul codice con Java 21

- Compilazione sorgenti e test smoke, compresi casi Ascoli/Crivello e Okaka cross-code, superata.
- Sorgenti JS reali del 6 novembre: 21 stagioni, 16.262 `Careers`, 45.787 `Teams`.
- Higuaín di test: 8 stagioni, 217 voti reali validi senza duplicati, 69 contesti fantasia distinti.
- Cutoff storico di due stagioni: nessuna informazione delle stagioni future.
- Integrazione reale `attachScoped`: shard `History` con rendimenti per fantasquadra e validazione limite byte.

## Non ancora concluso

- Registrazione effettiva della test-copy Windows e conferma di tutti i JS generati sulla mappatura locale dell'utente.
- Calcolo della fantamedia reale indipendente dalle fantasquadre, recupero foto e indice economico stagionale configurabile.
- Le 351 proposte ancora sospese restano soggette alle decisioni esplicite dell'utente, non vengono fuse.
- La vecchia v3/v29 e la patch v44 mai installata non sono base di questa modifica.

## Installazione e ripristino

Solo `TEST_v37` con preflight SHA-256, backup sorgenti e JAR, compilazione Maven, smoke test e ripristino in caso di errore. No pubblicazione siti.
