# Checklist release RecordsNext 3.1.0

1. `git diff --check`
2. `.\mvnw.cmd clean test package`
3. 50 test, 0 failure, 0 errori.
4. Rigenerare la bibbia.
5. Costruire FULL v5 con runtime UCanAccess 2.0.9.5.
6. Compilare `tools\RecordsNext_3.1.0.iss` con Inno Setup 7.
7. Installazione vergine in una cartella nuova.
8. Verificare 36 DataA installati.
9. Verificare GUI, aggiunta Gestita/Manuale e URL normalizzati.
10. Eseguire una elaborazione/publish controllata.
11. Pulire release e tool obsoleti.
12. Commit e push.
13. Tag `v3.1.0`.
14. GitHub Release: pubblicare soltanto `RecordsNext_3.1.0_SETUP.exe` e SHA256.
