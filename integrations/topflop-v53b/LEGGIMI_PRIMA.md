# RecordsNext 4.0 - correzione installer skin Top & Flop v53b

Corregge il rifiuto di `recordsnext.html` da parte del controllo troppo rigido della v53 (`data-family-card="classics"`). Non sostituisce le skin originali. Aggiunge Top & Flop alle pagine gia presenti, usando i JS pubblicati da RecordsNext 4.0. Gestisce sia le griglie `rn-card-grid` esistenti sia le varianti di indice con struttura diversa.

L'installazione e consentita **solo** sulle copie sandbox di 2025/26 e 2026/27, sotto `D:\DEV_APPS\RecordsNext4.0_TEST_v37\data\publish-sandbox-v52\sites`. Preflight strutturale su entrambi gli indici, manifest SHA-256, rifiuto delle junction/symlink, backup dei file modificati e rollback. Nessuna scrittura alla 3.2 o al sito FCM `E:`. Non modifica Java, SQLite, identita o configurazione.

## Comandi PowerShell (uno per riga)

```powershell
Expand-Archive -LiteralPath 'D:\DEV_APPS\downloads\RecordsNext4_SkinTopFlop_v53b.zip' -DestinationPath 'D:\DEV_APPS\downloads\RecordsNext4_SkinTopFlop_v53b' -Force
powershell.exe -NoProfile -ExecutionPolicy Bypass -File 'D:\DEV_APPS\downloads\RecordsNext4_SkinTopFlop_v53b\tools\Installa-TopFlop-Skin-v53b.ps1' -Mode plan
powershell.exe -NoProfile -ExecutionPolicy Bypass -File 'D:\DEV_APPS\downloads\RecordsNext4_SkinTopFlop_v53b\tools\Installa-TopFlop-Skin-v53b.ps1' -Mode apply
powershell.exe -NoProfile -ExecutionPolicy Bypass -File 'D:\DEV_APPS\downloads\RecordsNext4_SkinTopFlop_v53b\tools\Installa-TopFlop-Skin-v53b.ps1' -Mode verify
```

Aprire `http://127.0.0.1:8765/recordsnext.html` col server locale gia usato nel collaudo 2026/27, poi la card **Records Top & Flop**. Non serve rielaborare e non serve ripubblicare.

**Nota:** la pagina Top & Flop v53 usa realmente i JS 4.0; i calcoli e il cutoff restano nel plugin. Renewo e separato: si veda `docs/CONTRATTO_RENEWO_RECORDSNEXT4_v53.md`.
