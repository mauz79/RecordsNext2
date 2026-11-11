# RecordsNext 4 — Registro decisioni v5

Fonte: versioni precedenti `REGISTRO_DECISIONI_RECORDSNEXT4_v2/v3/v4.md` **mantenute**, questa versione le integra senza riscrivere la storia. Stato CONFERMATA = requisito utente o test tecnico; PROPOSTA = scelta reversibile in attesa di approvazione; APERTA = da accertare.

## Decisioni consolidate (D-001–D-018)

Restano applicabili le decisioni registrate nella v4: sviluppo separato dalla 3.2, ID FCA stagionale, codice storico distinto, non alterare DB/fonti e HTML/JS, non duplicare dati reali per competizioni fantasy, output in `D:\DEV_APPS\downloads`, file versionati, preservare riposi e tabellini non collegati, bibbia incrementale, correzione probe `incontraina`.

## Nuove decisioni/verifiche

| ID | Stato | Contenuto | Riscontro |
|---|---|---|---|
| D-019 | CONFERMATA | Estrarre tutte le righe `GiocaIn` per stagione/giocatore, non solo i tesserati nelle nostre fantasquadre | Requisito utente; export reale v1 |
| D-020 | CONFERMATA | Mai identificare automaticamente `GiocaIn` con una presenza effettiva: conservarne sorgente e stato `GiornataDiA` | 2026/27: 38 righe per Lautaro, `Statistiche.Presenze=2` nell'istantanea |
| D-021 | CONFERMATA | `Punteggio.VOTO1` 0 e -1 restano valori distinti, e i tre voti sono salvati senza decidere la fonte ufficiale | Schema FCA e casi reali |
| D-022 | CONFERMATA | `Storico` e uno snapshot annuale ripetibile in archivi FCA diversi: vietata la somma tra snapshot per all-time | Confronto storico Lautaro |
| D-023 | CONFERMATA | Per un codice con anagrafiche incompatibili, segnalare e non unire automaticamente le stagioni nella carriera canonica | Codice 12302, test regressione |
| D-024 | PROPOSTA TECNICA, IMPLEMENTATA NEL PROTOTIPO | Archivio esplorativo JSON/JSONL.gz con tutti i valori raw + riferimenti contestuali; non e ancora lo schema canonico del motore | Estrattore reale v1, test su copia DB |
| D-025 | PROPOSTA TECNICA, IMPLEMENTATA NEL PROTOTIPO | `IncontroInA` collegato solo con candidato unico per giornata/squadra; candidati 0 o multipli restano non risolti | 143.258 righe su 617.310 non risolte; non equivalgono ai riposi fantasy |
| D-026 | CONFERMATA | Test e bibbia devono documentare esattamente codice eseguito, SHA e risultati; nessuna modifica alla pipeline 3.2 | Richiesta utente; generatore bibbia v5 |

## Questioni aperte (v4 + Fase 1)

- Q-001: individuare e verificare il campo preciso che conserva il codice permanente nel file FCM, differenziandolo dall'ID FCA locale.
- Q-002: definire trattamento delle collisioni `CodiceGazza` e delle anagrafiche mutate (ad es. 12302).
- Q-003/Q-009: distinguere tipologie dei riposi fantasy e tabellini con ID incontro assente (168 righe audit).
- Q-004: semantica esatta schieramento/riserva/entrato, SV, RU, bonus e modificatori, per la futura Fase 2.
- Q-005: definizione punti non sfruttati, regole e simulazione formazioni valide.
- Q-006: intervalli reali di tesseramento e trasferimenti.
- Q-007: filtri, denominatori, parita e ranking record.
- Q-010: interpretare il mancato abbinamento fra molte righe `GiocaIn` e `IncontroInA` prima di definire una partita reale ufficiale.
- Q-011: scelta ufficiale `Voto1/2/3` e regole particolari per presenza/SV/bonus, da fondare su FCM e regolamento.
- Q-012: integrare la stagione manuale 2005/06 dall'FCA originale non presente nelle tabelle raw SQLite attuali.

Nessuna di queste questioni viene risolta per deduzione dal prototipo.
