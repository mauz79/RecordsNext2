# Diario implementazione — v107

18/11/2026: acquisito esito reale v106: database SQLite 4.0 OK, 21 siti preservati, backup 3.2 da 5.757.930.755 byte eliminato.

Il lavoro v107 riguarda SOLO la distribuzione pubblica e la documentazione: sorgente Inno Setup, launcher Java 21+, packaging selettivo, manifest SHA256, test di privacy e aggiornamento conservativo.

La distribuzione pubblica deriva dal JAR ricompilato e testato nel worktree Git; asset statici **tracciati dal repository**, runtime UCanAccess della installazione 4.0 verificata. Non si pubblicano database, configurazioni, indici calciatori o siti HTML/JS generati dell'utente.

Workflow Windows previsto: `preflight` → `build` → `smoke` automatico su Windows → `publish` (commit e push solo dopo test).

Il .exe non puo' essere compilato in questo ambiente Linux: la verifica reale del compiler Inno Setup e dell'installazione Windows resta un gate. Nessun push viene dichiarato prima dell'esito sul PC.
