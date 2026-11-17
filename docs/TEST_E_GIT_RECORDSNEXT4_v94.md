# Test e Git v94 - Gate di accettazione

1. Prima di `repair`: worktree Git con identita `mauz79/RecordsNext2`, branch `feature/recordsnext4-v67-rc-gui-migrated`, HEAD `3e8d53f`, esattamente i 50 file del rapporto v93 in index, nessun file unstaged/untracked, SHA di tutti gli index blobs confrontati con il manifest v93.
2. `repair`: SHA controllati per i due Java mancanti dalla RC; nuovi documenti v94 aggiunti senza toccare i 50 originali; staging atteso: **59 file** (50+2 Java+7 file documentali/strumenti). Report `RN4_GITHUB_STAGE_v94.json`.
3. `commit`: `mvnw.cmd -B -ntp clean test package` direttamente nel worktree Git, non RC; commit solo con Maven exit 0 e index invariato; report `RN4_GITHUB_COMMIT_v94.json`. Nessun commit nel caso di errori.
4. `push`: solo fast-forward, branch remoto esistente e ancorato a `3e8d53f` (oppure identico al nuovo commit per retry); mai forzare; verifica con `git ls-remote`, ZIP di esito in `D:\DEV_APPS\downloads`.
5. Dati: SQLite, file FCA/FCM e sito produttivo esclusi da ogni operazione v94.

Il rapporto v92 attestava **67 test Maven** sulla RC, ma la compilazione sul Git v94 va ancora verificata su Windows. Il dossier v81/v82, i rapporti v88 e il checkpoint v93 rimangono baseline documentale.
