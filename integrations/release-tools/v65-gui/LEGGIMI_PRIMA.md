# RN4 GUI v65 - configurazione stagioni al primo clic

**Destinazione esclusiva:** `D:\DEV_APPS\RecordsNext4.0_RC_v64`. Chiudere la GUI prima dell'applicazione.

Problemi riscontrati nel codice Java su branch `feature/recordsnext4-v63-sandbox-validated`:
1. `RecordsNext2Dashboard.actionCard`: i `MouseListener` erano collegati solo al pannello e a tre JLabel; gli spaziatori interni della card non ricevevano il clic.
2. `RecordsNextConfigurationDialog.loadSeasons`: in apertura di ogni finestra richiamava `refreshManagedMetadata` su ogni FCM, usando UCanAccess per ogni stagione. Il caricamento generava numerosi `WARNING: Cannot resolve db link` / metadata `Tesserato` e bloccava l'EDT Swing.

Correzione: tutte le superfici della card ora ricevono il clic; in apertura la configurazione usa numero stagione, FCM/FCA e altre informazioni **gia salvate in SQLite**. Il rilevamento FCM rimane disponibile quando si aggiunge una **nuova** stagione con la procedura guidata. Non viene nascosta globalmente alcuna categoria di warning.

Installa soltanto due modifiche testuali mirate ai sorgenti Java della RC, esegue `mvnw.cmd -B -ntp clean package` (test completi), mantiene backup e rollback in caso di errore. Non importa, pubblica, modifica il database, le associazioni storiche, i file FCM/FCA o i siti `E:`. Controlla SHA256 FCA 2012/13 *prima* di applicare.

I warning UCanAccess indicano link esterni non risolti e incongruenze nel conteggio metadata `Tesserato` dei file Access. Non implicano automaticamente perdita di record, ma rimangono diagnostiche utilizzabili durante import e analisi. Non riparare/modificare automaticamente i file sorgenti Access.

## Comando

```powershell
Expand-Archive -LiteralPath 'D:\DEV_APPS\downloads\RN4_GUI_CONFIG_v65.zip' -DestinationPath 'D:\DEV_APPS\downloads' -Force
powershell.exe -NoProfile -ExecutionPolicy Bypass -File 'D:\DEV_APPS\downloads\RN4_GUI_CONFIG_v65\Esegui-RN4-GuiConfig-v65.ps1' -Apply
```

**Nota:** i test Maven verificano la compilazione/regressioni ma il clic e il tempo di apertura devono essere verificati manualmente nella GUI Windows. Il patch non e' ancora un rilascio produttivo.
