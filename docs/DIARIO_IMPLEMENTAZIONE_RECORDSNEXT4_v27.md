# Diario implementazione RecordsNext 4.0 v27

2026-11-06. Sorgente di partenza: ZIP del worktree `RecordsNext4_SORGENTI_ATTUALI_20261106.zip` più aggiornamenti CanonicalRounds, Contributions, TopFlop JS, Compact e PublicViews v1, ultimo output Windows `RecordsNext4_TopFlopVistePubbliche_OUTPUT_v1_20261106_145346_272773.zip`, esito OK, 85 JS per 5.355.536 byte, massimo 208.689 byte, bibbia v25 generata sul PC.

Implementazione locale: nuovo enum `TOP_FLOP`; catalogo allargato a 114 record; persistenza `topFlop` nel JSON compatibile con la 3.2; GUI con pannello dedicato e venti voci; blocco delle sette voci non ancora affidabili; selezione record effettiva nei JS; nuovo `TopFlopViewsPublication` per una sola estrazione locale e cutoff per sito; aggiunta in `RecordsNextPipeline` nel flusso generate-only e multisito; manifest aggiornato e rimozione transactional dei TF obsoleti.

**Verifiche locali:** `javac` Java 21 completo; 52 test modello; test portiere; test selezione e scope su SQLite reale 2006/07 e 2026/27; test di pubblicazione senza sito reale con manifest e pulizia stale; smoke Swing con display virtuale. Nessuna copia nei siti reali. Mancano le verifiche Windows sulle parti nuove.

**Prossimo passo:** installer con SHA-256 conservativo e generatore bibbia v26; dopo ZIP Windows positivo completare visualizzatore HTML e test di integrazione FULL/CONSOLIDATED/multisito.
