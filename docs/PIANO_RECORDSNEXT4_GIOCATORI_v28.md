# Piano RecordsNext 4.0 v28 — catalogo e GUI native, pubblicazione 3.2

Data 2026-11-06. Addendum conservativo a `PIANO_RECORDSNEXT4_GIOCATORI_v27.md`.

**Implementato nel codice da installare e da verificare su Windows:** famiglia TOP_FLOP opt-in nel catalogo e nella GUI; pannello di scelta TF01–TF20, filtro nell'exporter delle sole viste approvate, adapter per scope storico, manifest e publisher esistenti, recupero dei file TF non più selezionati. Rimane il limite bloccante 1.500.000 byte/JS. Il test locale ha confermato le scelte granulari e la rimozione di file obsoleti nella copia temporanea del sito.

**Non è una release:** servono verifica con runner Windows, FULL/CONSOLIDATED reali, GUI effettiva, visualizzatore HTML RecordsNext/ReNewo, validazione del markup e skin, record incompleti TF05/06 e TF11–15 soltanto dopo evidenze e criteri approvati. Prima dei test sul sito reale il publish della GUI deve rimanere disabilitato.

Nessuna modifica alla directory `D:\DEV_APPS\RecordsNext2.0`; lavori solo sul branch `feature/recordsnext4-players` in `D:\DEV_APPS\RecordsNext4.0`.
