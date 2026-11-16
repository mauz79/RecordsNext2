# RecordsNext 4.0 — Preparazione release candidate v64 senza ricominciare

## Cosa fa

Crea `D:\DEV_APPS\RecordsNext4.0_RC_v64` partendo dalla **TEST v58c validata**, preservando database SQLite, configurazione delle 21 stagioni, identità, Top & Flop e **339 JS dello staging**. Ripristina automaticamente le destinazioni reali originali dal manifest *pre-sandbox* v52; legge la configurazione 3.2 solo come fallback per campi vuoti, senza sovrascrivere le decisioni successive della 4.0. Non copia le vecchie Bibbie dal GitHub: usa lo stato effettivo TEST, compresa la Bibbia v46. Non esegue import o calcoli.

**Vincolo fondamentale: FCA 2012/2013**. Mantiene esclusivamente il file `E:\FCM\data\ArchivioA2012SerieAmauz_G1_RECUPERATA_v1.fca`, SHA256 `e3c34b941aedb35951ba030701fe47072afa00941f7fa7884d0658442cfcf766`. Verifica nel database l'ultimo import FCA `COMPLETED`, il file fisico e 9.936 voti (255 in giornata 1). Blocca il passaggio se un vecchio FCA della 3.2 tenta di sostituirlo. Non tocca l'originale.

La pubblicazione nel candidato è disattivata in entrambe le configurazioni (`processing.json` e `recordsnext-gui.properties`). Nessun file viene scritto nei siti E:. La 3.2 operativa e la cartella TEST rimangono **inalterate**. La GUI del candidato si avvia con il nuovo launcher `Avvia-RecordsNext4-RC-v64.ps1`, non con il launcher TEST vincolato alla vecchia directory.

## Uso

Salvare ed estrarre lo ZIP in `D:\DEV_APPS\downloads`. Con tutte le GUI chiuse:

```powershell
Expand-Archive -LiteralPath 'D:\DEV_APPS\downloads\RN4_MIGRAZIONE_RC_v64.zip' -DestinationPath 'D:\DEV_APPS\downloads' -Force
powershell.exe -NoProfile -ExecutionPolicy Bypass -File 'D:\DEV_APPS\downloads\RN4_MIGRAZIONE_RC_v64\Esegui-RN4-MigrazioneRC-v64.ps1' -Mode plan
powershell.exe -NoProfile -ExecutionPolicy Bypass -File 'D:\DEV_APPS\downloads\RN4_MIGRAZIONE_RC_v64\Esegui-RN4-MigrazioneRC-v64.ps1' -Mode prepare
```

Dopo `MIGRAZIONE RC v64 OK`:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File 'D:\DEV_APPS\RecordsNext4.0_RC_v64\Avvia-RecordsNext4-RC-v64.ps1'
```

## Rimane prima del rilascio reale

- Collaudo browser delle pagine `recordsnext.html` e `top_flop.html`; le verifiche v62b sono **solo statiche**.
- Integrazione delle skin nella pubblicazione nativa GUI: al momento la v53b si installa con script esterno, **non è ancora nativa**.
- Promozione controllata della RC nel percorso operativo della 3.2 con backup atomico/rollback; **non viene tentata dalla v64**.
- Configurare/accettare in GUI la destinazione di pubblicazione solo dopo avere verificato i target. I percorsi per stagione sono conservati nel DB, ma i flag restano spenti.

## Guardie

- Nessuna sovrascrittura di una RC preesistente.
- Se la preparazione si interrompe, la cartella `RecordsNext4.0_RC_v64.PREPARAZIONE` resta per ispezione; i sorgenti originali non sono modificati.
- Esclude sandbox v52 e vecchi backup, preserva l'ultimo staging validato.
- Le stagioni manuali senza FCM (2005/06) restano escluse.
