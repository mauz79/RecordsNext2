# RecordsNext 4 - Correzione Fase 0, revisione v4

Data: 2026-10-31. Stato: correzione tecnica al runner, NON sviluppo di nuove tabelle o record.

## Errore riscontrato e causa verificata

Il 31/10/2026 il runner `Invoke-RecordsNext4-Fase0-v3.ps1` ha:

1. Verificato correttamente il worktree `D:\DEV_APPS\RecordsNext4.0`, ramo `feature/recordsnext4-players` e commit base 3.2 `f9bf303...`.
2. Installato nuovi file e riconosciuto identico lo script di audit giocatore v1.
3. Fallito durante la generazione della bibbia incrementale 4.0:
   `Create-RecordsNext4WorkingCodeMd-v2.ps1: Modello di espressione regolare \\ non valido`.

La causa esatta e la linea:

```powershell
($f -replace '\', '/')
```

In PowerShell `-replace` interpreta il pattern come regex: il backslash singolo non e un'espressione valida. La trasformazione letterale corretta e:

```powershell
$f.Replace('\', '/')
```

Il problema era nel codice consegnato, non nel sistema Windows dell'utente.
Non modificare in-place il generatore v2: rimane documento storico del difetto.

## Scelta correttiva

- Nuovo `tools/Create-RecordsNext4WorkingCodeMd-v3.py` e nuovo `tools/Invoke-RecordsNext4-Fase0-v4.py` (sola standard library Python, gia disponibile sul PC: 3.13.7).
- Nessun richiamo al generatore PowerShell v2 o al runner v3.
- Nessuna creazione del worktree: il v4 richiede quello gia installato e ne controlla il ramo e la discendenza da v3.2.0.
- Solo nuovi file con nome versionato; se un file preesiste con hash diverso, ci si ferma senza sovrascriverlo.
- Git viene interrogato ma non modifica commit, indice, branch o tag.
- Il database `recordsnext-sandbox.db` viene interrogato in sola lettura dai tool Python v1.
- ZIP dell'esecuzione anche su errore, sotto `D:\DEV_APPS\downloads`.

## Corretto trattamento delle anomalie FCM

L'utente segnala che i tabellini senza il normale incontro potrebbero riferirsi a squadre a riposo. La verifica resta aperta: non chiamarli automaticamente errori, ma neppure assegnarli a riposi senza riscontri nei file originali.

## Esecuzione

1. Scaricare `RecordsNext4_Fase0_Kit_v4.zip` in `D:\DEV_APPS\downloads`.
2. Estrarlo in `D:\DEV_APPS\downloads\RecordsNext4_Fase0_Kit_v4`.
3. Chiudere le elaborazioni che scrivono al database.
4. Lanciare con `py -3` lo script `tools\Invoke-RecordsNext4-Fase0-v4.py` dall'archivio estratto.
5. Caricare `RecordsNext4_Fase0_OUTPUT_v4_*.zip`, indipendentemente dal risultato, per controllare log e report.

## Condizioni di uscita

- Generatore bibbia v3 completato oppure errore dettagliato.
- Due audit codici, inventario fonti e probe tabellini completati e conservati in ZIP.
- Base 3.2.0 invariata. Nessun commit, nessuna pubblicazione.
