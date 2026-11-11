# RecordsNext 4.0 - Diario implementazione v7

Data: 2026-10-31. Base 3.2.0, commit `f9bf30323d9204d70e91e242bddfc72c2060ecf9`.

## Verifica ZIP Windows Fase 2

- Acquisito `RecordsNext4_Fase2_OUTPUT_v1_20261031_214006_892950.zip`.
- Log: 8 test superati; anno 2006/07 = 6.050 righe, anno 2025/26 = 39.850; codici 100843 = 1.327, 12302 = 31.
- Quattro hash SHA-256 degli output coincidono con i manifest.
- Manifest database SHA prima/dopo = `57923f4e75986c2b81533e4beef15b9133635258d7de1d36827a1bdbe576671b`.
- Bibbia v6 generata, nessun commit/deploy dichiarato dal runner.

## Fase 3A realizzata

- Aggiunto `tools/Audit-RecordsNext4Utilizzo-v1.py`, sola lettura.
- Aggiunto `tools/Test-RecordsNext4Utilizzo-v1.py` (9 test automatici).
- Aggiunto runner `tools/Invoke-RecordsNext4-Fase3A-v1.py` che installa conservativamente, testa, estrae un report all-time in CSV/JSON e genera ZIP diagnostico.
- Bibbia v7 generata da `tools/Create-RecordsNext4WorkingCodeMd-v7.py` con codice completo e hash.
- L'audit locale ha letto 552.025 Formazione in 21 stagioni; tutte le righe sono rappresentate nelle combinazioni. Niente modifica database, niente punteggi persi stimati.

## Limiti e prossimi controlli

- Certificare la Fase 3A su Windows mediante ZIP del runner.
- Esaminare le combinazioni anomale e i campioni con il codice RU preesistente prima di definire una metrica pubblica.
- Stabilire regole per ruoli, entrate, riserve d'ufficio, moduli e numero massimo sostituzioni per periodo/competizione.
- Nessuna modifica Java, GUI, siti e release 4.0 finora.
