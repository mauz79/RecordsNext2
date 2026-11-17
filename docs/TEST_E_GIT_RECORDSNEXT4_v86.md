# RecordsNext4 — Test/Git v86

## Test offline (contenuti realmente acquisiti)

- Copia RC v85 + overlay payload v83b: `javac --release 21` su tutto `src/main/java` compilato (291 class iniziali).
- Java standalone `Rn4V83JavaSmoke`: OK (coefficienti/split/ambiguità, rollback publisher JS e pagina).
- Java standalone `Rn4V86JavaSmoke`: OK (matchId identico in due competizioni FCM, chiavi separate).
- Node `RC/tests/regression_v83.js`: OK (TF20 goleador senza falsi zero, assist Carnesecchi fixture, 15 indicatori fixture, ricerca Klose, asset <=1.5MB).
- Verificare da PowerShell Windows che `mvnw.cmd -B -ntp test package` completi. Nodo JUnit v86 aggiunto ma non ancora eseguito via Maven offline.

## Gate mancanti

- Audit v81 ha origine reale FCA ma non certifica tutti i record.
- v79: 240.096 COUNTED; 35.111 contesti; 32.488 certificati con split; 5.507 scorecard non ricostruite e 11 ambigue.
- Gate indipendenti da completare sui 21 RAW: TF01–TF20, classici, serie, RU, modificatori, soglie/Culometro. Verifica GUI XAMPP con quattro skin, cutoff per siti diversi, test Carnesecchi/Simeone/Cristante/Klose.
- `preflight` non installa, `install` modifica solo codice/asset/docs/tools della RC con backup, `build` compila, `validate` legge il DB in sola lettura e verifica sandbox. Nessun publish automatico né commit.
- Worktree Git è separato, `feature/recordsnext4-v67-rc-gui-migrated` commit `3e8d53f`; non copiare le config runtime (`config/player-identities.json`, processing/GUI proprietà locali) nel Git e non fare push fino a certificazione e approvazione.

## Comando unico preflight (qualsiasi directory PowerShell)

```powershell
& 'C:\Program Files\Python313\python.exe' 'D:\DEV_APPS\downloads\RN4_CONSOLIDAMENTO_v86\Gestisci-RN4-v86.py' preflight
```

L'output si trova direttamente in `D:\DEV_APPS\downloads\RN4_PREFLIGHT_v86.json`. **Non avviare install/build prima di un esito READY.** Il resto è documentato nel file `LEGGIMI_v86.md` della cartella del pacchetto; nessuna ulteriore ricerca di paths è richiesta.
