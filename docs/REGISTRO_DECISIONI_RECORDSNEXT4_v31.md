# Registro decisioni RecordsNext 4.0 — v30 — 2026-11-06

- Storicizzare tutto in SQLite; pubblicare solo viste TF01–TF20 e riepiloghi, non il dump FCM/FCA.
- Parametri Top & Flop separati in `config/topflop.json`, gestiti nella pagina stessa. TF05/TF06 applicano soglie attive; TF11–TF15 rimangono disabilitati.
- Completa/consolidata Top & Flop riusano gli stessi meccanismi della 3.2 e sono sincronizzate col selettore globale. Non sbloccare una consolidata senza consolidamento.
- Dashboard con card Top & Flop; le skin non vanno mai sovrascritte dalla preview.
- Fallback CSS 3.2 contenuto nel kit e limitato alla preview locale; nessuna modifica alla 3.2 operativa.


## Aggiornamento sviluppo v46 - 11/11/2026

- Approvato in precedenza il limite massimo fisico di **1.500.000 byte per ciascun JS**. Il precedente valore 1536 KiB del sistema flat 3.2 non e' equivalente.
- I record della 3.2 non vanno riscritti: nel ramo 4.0 generazione senza pubblicazione occorre applicare la suddivisione flat gia' disponibile prima della validazione finale.
- Preservare gli output Classici/RU/Serie/Modificatori/Soglie/Fortuna/Culometro/Matches e i nomi delle facade. Pubblicazione multisito e cutoff storico invariati.
- L'elaborazione di test deve essere eseguibile senza accesso in scrittura ai siti; validazione fisica di tutti i JS dopo lo split.
- Identita' dei calciatori approvate dall'utente, registro persistente e motore squadre 3.2 invariati.
- Aggiornare Github soltanto dopo il collaudo su Windows e con commit verificato sul ramo di sviluppo; non simulare una pubblicazione riuscita.
