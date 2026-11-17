# RecordsNext4 — Registro decisioni v44

- **DEC-v86-001, unica baseline**: `RecordsNext4.0_RC_v64` è installazione e codice operativo, distinta dal worktree Git `RN4_GITHUB_WORK_v63_20261116_174243`. Non usare `git -C` sulla RC e non trattare branch `feature/recordsnext4-players` sporco come attuale.
- **DEC-v86-002, generatore canonico**: preservare `Create-RecordsNext4WorkingCodeMd-v47.py` e v47 intera; l'aggiornamento v48 è candidato e va creato con generatore versionato v86.
- **DEC-v86-003, niente micro-patch**: un solo payload nativo Java/GUI/exporter/HTML/JS, con due pagine separate e pubblicazione atomica; nessuna applicazione in sequenza di v69–v79 o v83/b.
- **DEC-v86-004, chiave FCM completa**: l'unicità di ogni slot `COUNTED` comprende giocatore stagionale, fantasquadra, ID competizione e matchId. MatchId coincidenti in competizioni distinte non si deduplicano.
- **DEC-v86-005, ambiente Windows reale**: Python 3.13 è già presente; utilizzare procedura Python anziché PowerShell v83b che fallisce il parser. Il preflight deve essere eseguibile da qualsiasi directory e non deve scrivere su RC/Git/DB/sito.
- **DEC-v86-006, limiti della prova**: il saldo FCM è `SUM(MODIF)` certo quando `TOT−VOTO=MODIF`; split bonus/malus nullo se non riconciliato in modo univoco. Rispettare 5.507 scorecard non ricostruibili e 11 ambigue documentate v79; nessun workaround inventato.
- **DEC-v86-007, blocco rilascio**: senza gate RAW→Java→JS→UI di tutte le famiglie di record, nessun commit definitivo né dichiarazione di release completata. Distinguere verifiche reali da smoke sintetici.
- **DEC-v86-008, documenti autosufficienti**: un solo archivio di continuità contiene snapshot v85, dossier v82, candidato, Bibbia e manifest: in una nuova chat non chiedere nuovamente file già inclusi.
