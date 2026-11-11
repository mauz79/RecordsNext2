# Verifica locale tecnica — Estrattore reale v1

Database: copia `recordsnext.db` ricevuta dall'utente, SHA256 `d95e4597ec9f1747ca1576876c6443f8c4cbdec92cd66f0a264c5d11da1caf76`.

- Implementazione: `tools/Estrai-RecordsNext4Reali-v1.py` (sola lettura).
- Test: `tools/Test-RecordsNext4Reali-v1.py`, **7/7 superati** sull'ambiente di prova disponibile.
- Esportazione `--all` su tutte le **21** stagioni: **16.245** giocatori stagionali, **617.310** righe `GiocaIn`, 0 score mancanti, 143.258 potenziali incontri reali non abbinati. File `.jsonl.gz` generati singolarmente per anno, senza perdita di righe.
- Esempio codice `100843`: **9 stagioni / 342 giornate FCA**. Esempio `12302`: **3 stagioni / 114 giornate FCA**, segnala il conflitto storico.
- `Statistiche` 2025/26 di Lautaro: 17 gol e 6 assist, coerenti con `GolFatti1`/`Assist` ricostruiti da `GiocaIn`+`Punteggio` per quella stagione.

La mancata attribuzione univoca a `IncontroInA` non significa dato danneggiato. Questa prova non certifica tutte le statistiche storiche, ne una fonte voti ufficiale, ne la fase di integrazione nella pipeline Java. Aspetti mancanti e problemi da approfondire sono riportati nella specifica v1 e nel registro decisioni v5.

**Le verifiche PowerShell/Windows devono essere confermate attraverso lo ZIP diagnostico Fase 1.**
