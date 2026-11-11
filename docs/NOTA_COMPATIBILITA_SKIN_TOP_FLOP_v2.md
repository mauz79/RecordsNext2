# RecordsNext 4.0 — Nota di compatibilita demo Top & Flop v2

Data: 2026-11-02.

## Motivo della correzione

Nel worktree Windows dell'utente la skin `release/visualizzatori/profiles/neutral/recordsnext.css` era diversa dalla copia nel kit demo v1. Il preflight ha bloccato l'installazione prima di scrivere qualsiasi file.

## Contratto di installazione

1. Le quattro skin originali (`mauzstrom`, `maelstrom`, `fantablue2`, `neutral`) non vanno modificate quando sono gia presenti. Sono la fonte di verita per la demo.
2. Quando un CSS non e presente, il runner puo installare la copia del kit.
3. Tutti gli altri file mantengono il preflight conservativo: in caso di contenuto differente, arresto senza sovrascrittura.
4. Per le skin conservate il runner registra nel report il percorso e l'hash SHA-256 iniziale e ne verifica la conservazione a fine installazione.
5. La bibbia v19 fotografa le skin effettivamente presenti nel worktree: puo quindi riportare SHA-256 diversi da quelli del kit, correttamente. La bibbia v20 aggiunge questa decisione, il runner correttivo e i controlli della compatibilita.
6. Nessun file della 3.2 operativa e toccato. Il sistema di pubblicazione 3.2 rimane invariato.

## Limiti

La demo e ancora sperimentale su due stagioni; non equivale ad attivare TOP_FLOP nella pipeline o nella GUI. La verifica visiva nella skin Neutral/ReNewo deve ancora essere eseguita sul PC utente.
