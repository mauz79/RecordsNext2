# RecordsNext 4 — Diario implementazione v5

Le versioni v2-v4 e `VERIFICA_FASE0_RECORDSNEXT4_v1.md` restano la cronologia della Fase 0. Non alterate.

## 2026-10-31 — Fase 1, prototipo archivio reali v1

- Letti codice 3.2, schema raw FCA, modelli e rapporti di Fase 0; identificato esattamente il nome della tabella `IncontroInA` normalizzata `incontraina`.
- Implementato `tools/Estrai-RecordsNext4Reali-v1.py`: lettura SQLite `mode=ro`, selezione per codice oppure intera stagione, conservazione di tutte le colonne raw FCA, giornate non giocate e valori voto speciali, snapshot `Storico` separato.
- Collegamento opzionale a `IncontroInA` solo quando giornata+squadra restituiscono un candidato unico; nessuna assunzione su casi non risolti.
- Implementati `tools/Test-RecordsNext4Reali-v1.py` (7 controlli), inclusi confronto record Lautaro 2025/26 e collisione codice 12302.
- Eseguiti 21 export completi sulla copia DB 3.2: 16.245 righe anagrafiche stagionali, 617.310 righe `GiocaIn`, 0 `IDPunteggio` senza score. 143.258 righe non risolte per match reale, senza classificarle come errori.
- Eseguiti export per `100843`: 9 stagioni, 342 righe; per `12302`: 3 anagrafiche, warning storico. 7 test superati. Database immutato.
- Preparati piano v6, decisioni v5, specifica v1, generatore bibbia v5, runner di installazione v1 e kit zip. Test su Windows e audit restituito dall'utente ancora da eseguire.

## Stato veritiero

- Python esplorativo funzionante sulla copia di SQLite; **non** codice Java della pipeline, non generazione di record, non pubblicazione JS, non GUI.
- Nessun commit e nessuna release 4.0.0.
- Per successivi cambiamenti versionare tutti i file ed includerne il testo integrale e gli hash nella bibbia successiva.
