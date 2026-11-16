# RecordsNext 4.0 — GUI dashboard v66

Destinazione **solo RC v64**: `D:\DEV_APPS\RecordsNext4.0_RC_v64`. Chiudere la GUI prima dell'installazione.

Questa patch corregge tre aspetti della dashboard: (1) rimuove «(sviluppo)» e «dev» dal titolo Windows, dall'intestazione, dalle finestre errore e dal logo laterale; (2) sostituisce le altezze rigide delle card con un layout 2×2 flessibile che usa lo spazio disponibile, riduce il testo ridondante e aumenta leggermente la dimensione minima della finestra; (3) rende coerente il menu laterale con tutte e quattro le sezioni delle card: **Configurazione stagioni, Famiglie record, Top & Flop, Soglie e Culometro**. Restano le voci tecniche esistenti.

La correzione v65 per il primo clic e l'apertura stagioni senza rilettura dei FCM deve essere gia' presente: il preflight lo controlla. Viene modificato un solo sorgente Java. Il pacchetto verifica SHA-256 dell'FCA corretto 2012/13, crea un backup, compila e testa tramite `mvnw.cmd` ed esegue rollback in caso di errore. Non cambia config, SQLite, FCM, FCA, JS, siti, branch Git o Bibbia v46.

## Installazione

```powershell
Expand-Archive -LiteralPath 'D:\DEV_APPS\downloads\RN4_GUI_DASHBOARD_v66.zip' -DestinationPath 'D:\DEV_APPS\downloads' -Force
powershell.exe -NoProfile -ExecutionPolicy Bypass -File 'D:\DEV_APPS\downloads\RN4_GUI_DASHBOARD_v66\Esegui-RN4-Dashboard-v66.ps1' -Apply
```

## Dopo il risultato OK

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File 'D:\DEV_APPS\RecordsNext4.0_RC_v64\Avvia-RecordsNext4-RC-v64.ps1'
```

Controllare la dashboard e ridimensionare la finestra. Compilazione e test Java non dimostrano da soli la resa visiva: approvare in GUI prima di consolidare la Bibbia e GitHub.
