# Verifica locale Fase 2 — archivio fantasy v1

Fonte: `recordsnext.db` estratto dallo ZIP già fornito; sola lettura. Questa NON e una verifica Windows fino al prossimo ZIP utente.

| Controllo | Esito |
|---|---|
| Stagioni FCM/FCA | 21 |
| `Formazione` totale | 552.025 |
| Slot individuali `Tabellino` esatti | 552.025 / 552.025 |
| `Formazione` senza `Incontro` | 0 |
| `Formazione` senza `GiocaIn` della giornata reale | 0 |
| 2025/26 | 39.850 righe e slot univoci |
| 2006/07 | 6.050 righe e slot univoci |
| 2026/27 | 0 righe nell'istantanea |
| Test Python | 8/8 superati |
| Lautaro, codice 100843 | 1.327 righe fantasy sulle stagioni 2018/19–2025/26; 162 nel 2025/26 |

**Limiti:** non classifica i 168 tabellini non riferiti a `Incontro`; questo audit riguarda solo `Formazione` esistenti. Non calcola punti non sfruttati, non prova tesseramenti temporali e non identifica tutte le anomalie storiche. I percorsi delle foto e i link ai tabellini dovranno seguire la risoluzione esistente della 3.2. Nessuna pubblicazione o integrazione Java/SQLite canonica.
