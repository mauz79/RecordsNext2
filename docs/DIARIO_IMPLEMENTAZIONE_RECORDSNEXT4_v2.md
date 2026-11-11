# RecordsNext 4 — Diario implementazione v2

## 2026-10-31 — Revisione dell'archivio 3.2.0 e correzione Fase 0

- Ricevuto e analizzato `RecordsNext2.0.zip` contenente codice, script, documentazione, dati e metadati Git della release 3.2.0.
- Confermato commit `f9bf303...`, tag annotato `v3.2.0` e registrazione di `D:\DEV_APPS\RecordsNext4.0` come worktree sul ramo `feature/recordsnext4-players` nei metadati del repository. Non equivale a prova che ogni file sia presente attualmente sul PC: verifica nel runner.
- L'errore del runner Fase 0 v2 era nel preflight di verifica del tag: non procedere a ricreare il worktree esistente. Causa nativa esatta non disponibile dall'output; il runner v3 evita la verifica del tag quando il worktree e gia valido.
- Letta la struttura del generatore originale `tools/Create-RecordsNext2WorkingCodeMd.ps1`. La nuova bibbia v2 elenca file e SHA256 delle modifiche 4.0, lasciando inalterata la bibbia 3.2.
- Creato `tools/Probe-RecordsNext4Tabellini-v1.py` per distinguere statisticamente record `Incontro` con un lato squadra nullo/zero dai tabellini privi di riferimento `Incontro`.
- Verifica Python sul database caricato precedentemente: 21 stagioni, 15 con ID tabellini da verificare, 168 righe `Tabellino` senza `Incontro`. Il dato NON dimostra errore ne riposo.
- Nessuna modifica a Java, GUI, pubblicazione, archivi originali o database.

## Verifica necessaria sul PC Windows dell'utente

Avviare `tools/Invoke-RecordsNext4-Fase0-v3.ps1` dal kit v3 ed esaminare lo ZIP in `D:\DEV_APPS\downloads`. Non eseguire la versione v2 difettosa. Lo script PowerShell e stato revisionato staticamente per PS 5.1; **non e stato collaudato su una macchina Windows in questa sessione**. Gli script Python sono stati compilati e il nuovo audit tabellini e stato eseguito sul database di copia.

## Prossimi interventi

Dopo i report: verificare gli ID FCM/FCA originali e il codice foto, stabilire regole per riposi e tabellini residuali e progettare l'estrattore reale della fase 1. Nessun commit o release automatici.
