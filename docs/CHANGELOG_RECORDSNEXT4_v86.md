# RecordsNext4 — Changelog tecnico v86 (candidato)

**Base:** RC v64 reale + Git v67 `3e8d53f`; allineamento documento v85.

1. Ripreso payload unico nativo non installato v83b, preservando le due pagine `top_flop.html`/`calciatori.html`, JS condivisi e 15 indicatori.
2. Corretto algoritmo di deduplicazione apporti FCM `TopFlopIntegratedV83.points`: aggiunto `sourceCompetitionId` alla chiave. Aggiunta regressione JUnit dedicata a matchId comune in campionato/coppa.
3. Nuovo gestore Windows in Python 3.13 `Gestisci-RN4-v86.py` con preflight SHA e baseline completa del PC v85 (575 file inventariati, controllo bloccante dei payload modificati), backup/rollback e validazione in downloads. Non richiede l'esecuzione di script PowerShell.
4. Generatore Bibbia `Genera-Bibbia-RecordsNext-v86.py`, Bibbia candidata v48 ricostruita dalla v47 completa e dai sorgenti effettivi della candidata v86. Aggiornati diario, decisioni, test/Git e continuità.
5. Nessun intervento applicato alla RC Windows o a Git, nessuna certificazione numerica totale dichiarata. Il test in Windows, i gate RAW di record mancanti e Git restano obbligatori.
