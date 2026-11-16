# Ripresa release candidate v64b dopo WinError 5

Il passaggio v64 ha completato la preparazione ma si e fermato nella rinomina finale, verosimilmente per connessioni SQLite lasciate aperte dallo stesso processo Python su Windows (`with sqlite3.connect(...)` non chiude la connessione). La v64b e un **completamento** della preparazione, non una nuova migrazione.

Questa procedura **non copia, modifica o rigenera** alcun database, codice, JS, file FCA, FCM, sito o configurazione. Esegue verifiche solo lettura sulla cartella `D:\DEV_APPS\RecordsNext4.0_RC_v64.PREPARAZIONE` e, se tutte passano, la rinomina nella cartella definitiva `D:\DEV_APPS\RecordsNext4.0_RC_v64`.

Guards: 21 destinazioni originali da manifest pre-sandbox, 2005/06 manuale esclusa, integrita SQLite, ultimo FCA 2012/13 `ArchivioA2012SerieAmauz_G1_RECUPERATA_v1.fca` con hash SHA-256 esatto e 9936 voti (255 G1), identita/TopFlop protetti, JAR uguale al TEST, 339 JS uguali allo staging, dimensioni individuali max 1500000 byte, pubblicazione disattivata. Chiude **esplicitamente** ogni handle SQLite prima della rinomina.

In PowerShell, dopo avere estratto lo ZIP in `D:\DEV_APPS\downloads`, e con la GUI chiusa:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File 'D:\DEV_APPS\downloads\RN4_MIGRAZIONE_RC_v64b\Esegui-RN4-RipresaRC-v64b.ps1' -Mode resume
```

Se Windows blocca ancora la rinomina, il controllo lascia intatta la cartella `.PREPARAZIONE`: non cancellarla. Nessun target E: e modificato.
