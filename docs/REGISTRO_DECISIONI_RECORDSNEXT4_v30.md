# Registro decisioni RecordsNext 4.0 — v30 — 2026-11-06

- Storicizzare tutto in SQLite; pubblicare solo viste TF01–TF20 e riepiloghi, non il dump FCM/FCA.
- Parametri Top & Flop separati in `config/topflop.json`, gestiti nella pagina stessa. TF05/TF06 applicano soglie attive; TF11–TF15 rimangono disabilitati.
- Completa/consolidata Top & Flop riusano gli stessi meccanismi della 3.2 e sono sincronizzate col selettore globale. Non sbloccare una consolidata senza consolidamento.
- Dashboard con card Top & Flop; le skin non vanno mai sovrascritte dalla preview.
- Fallback CSS 3.2 contenuto nel kit e limitato alla preview locale; nessuna modifica alla 3.2 operativa.
