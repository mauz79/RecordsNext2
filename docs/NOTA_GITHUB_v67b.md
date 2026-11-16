# RecordsNext 4.0 - ripresa GitHub v67b (16/11/2026)

Bibbia v47, Diario v47, Registro v40 e documentazione v67 risultano generati correttamente nella RC v64.
La fase GitHub v67 si e fermata durante la rilevazione del branch Git, prima di creare il branch v67.
Il nuovo runner usa `git branch --show-current` e cattura esplicitamente exit code e diagnostica, senza pipeline `Select-Object -First 1`.
La base del checkpoint resta `3b008a8` sul branch `feature/recordsnext4-v63-sandbox-validated`.
Il target Git e `feature/recordsnext4-v67-rc-gui-migrated`, con due classi GUI v65/v66, documentazione v47, script migrazione v64/v64b, patch GUI e test Maven.
La migrazione RC v64b ha verificato 21 stagioni, 339 JS e l'FCA recuperato 2012/13 con SHA-256 `e3c34b941aedb35951ba030701fe47072afa00941f7fa7884d0658442cfcf766` (9936 voti, 255 G1).
Punti aperti per il rilascio: collaudo visivo nel browser delle pagine ed esecuzione della pubblicazione reale controllata dalla GUI.
