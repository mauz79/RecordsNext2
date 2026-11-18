# Gate release v107

- A — Preflight: repo Git al commit previsto, branch/remoto corretto, worktree pulito; Java >=21, Inno Setup ISCC disponibile; UCanAccess completo; asset Git statici; installazione 4.0 presente.
- B — Build: `pom.xml` aggiornato a 4.0.0, `mvnw.cmd clean test package` con `BUILD SUCCESS` e 0 errori.
- C — Privacy: payload senza `config`, `data`, SQLite, identita' approvate e percorsi personali. Manifest SHA256 di ogni file.
- D — Inno: `RecordsNext_4.0.0_SETUP.exe` creato e SHA256 registrato; installer con cartella selezionabile e installazione per-user.
- E — Upgrade: fixture verifica che `config/processing.json` e `data/database/recordsnext.db` preesistenti rimangano identici. Il comando `smoke` collauda realmente Inno su Windows (nuova installazione, secondo setup upgrade, disinstallazione, sentinelle preservate). Rimane utile aprire manualmente la GUI sul PC di prova per il test visivo.
- F — Git: commit soltanto dei sorgenti, launcher, tester, generatore e documenti v107; push sul branch senza force; remote SHA uguale al locale. Non versionare EXE, DB o dati personali.
- G — Carriere: CSV v98 confrontato **in lettura** con il registro attuale; segnala casi non unificati, non eseguire fusioni. La salute del setup NON certifica i dati storici.

I gate A–D e F richiedono ancora esecuzione reale Windows. Eventuali test fixture locali qui eseguiti non certificano il funzionamento della GUI in un PC nuovo.
