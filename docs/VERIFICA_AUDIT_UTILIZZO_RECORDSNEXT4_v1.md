# RecordsNext 4.0 - Verifica locale Fase 3A v1

Database: copia di `recordsnext-sandbox.db` 3.2 fornita. Esecuzione locale Linux in sola lettura, **non ancora certificata Windows**.

| Controllo | Esito |
|---|---|
| Stagioni FCM | 21 |
| Righe `Formazione` analizzate | 552.025 |
| Test Python | 9/9 |
| Panchina non entrata / TOT numerico (GIOCATO=1) | 94.992 |
| POS negativo / TOT numerico (GIOCATO=1) | 38.646 |
| Panchina entrata / ENTRATORISERVA=0 (GIOCATO=1) | 20.295 |
| Titolare non entrato / TOT numerico (GIOCATO=1) | 150 |
| ENTRATORISERVA=1, ENTRATO=0 | 43 |
| Stagione 2026/27 | 0 Formazione nella snapshot |

Questi numeri sono *righe di partecipazioni di formazione*, non giocatori unici ne punti non sfruttati. La stessa prestazione reale puo apparire in molte competizioni. Non trarre record all-time prima delle regole.

## Riferimenti codice 3.2 gia riletti

- `docs/ANALISI_PERSONALE_RU.md`: diagnostica personale, non pubblica.
- `tools/Report_RU_Storico_Completo_v8_4.ps1`: casi di 11 titolari `POS=0`, utilizzo di `ENTRATORISERVA` per sostituzioni.
- `src/main/java/it/alterlega/recordsnext/SeasonNormalizedExporter.java`: alcune metriche correnti selezionano `f.ENTRATO <> 0`.

Prima di costruire record su punti persi bisogna interpretare i 20.295/43 casi e le regole di campionato. Il presente audit conserva questi casi senza modificarli.
