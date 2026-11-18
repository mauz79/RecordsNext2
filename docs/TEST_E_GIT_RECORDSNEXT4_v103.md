# RecordsNext 4.0 - Test e Git v103

## Evidenze precedenti
- v94: push remoto verificato sul branch `feature/recordsnext4-v67-rc-gui-migrated`, commit `5c10ec5b729783747c5a8d33f17b5be84f70de74` e 66 test Maven superati nel worktree.
- v102: `RN4_V102_PREFLIGHT_READY`, `RN4_V102_INSTALL_OK file=13` (Maven `test package` nella RC all'interno dell'installatore), `RN4_V102_VERIFY_OK`. La relazione con il numero preciso di test della v102 richiede il log `RN4_MAVEN_FRONTEND_v102.txt`; non inventare un conteggio.

## Gate v103
1. Repo esatto/branch/head/remote, worktree pulito, stato installazione v102 valido, SHA256 RC e tre file produzione conformi al manifest.
2. Copiare soltanto whitelist verificata RC->Git e documenti v103; backup locale prima di scrivere i nuovi documenti RC.
3. Verificare corrispondenza SHA256 anche degli oggetti nello **stage Git**, eseguire `git diff --cached --check`.
4. `mvnw.cmd -B -ntp clean test package` nel worktree. Fallimento => nessun commit.
5. Commit locale solo con test superati, push non forzato solo se il remote HEAD coincide con il commit v94 o con il commit nuovo, confronto `ls-remote` post-push.
6. Rapporto `RN4_ESITO_GITHUB_v103_*.zip` in `D:\DEV_APPS\downloads`, con log e hash verificati; il rapporto non equivale a pubblicazione sul server web pubblico.

## Limitazioni
Nessuna riapprovazione delle identita' gia' registrate. Nessun rinnovo dei 21 archivi. Nessun test browser completo post-v102 attestato in questa fase.
