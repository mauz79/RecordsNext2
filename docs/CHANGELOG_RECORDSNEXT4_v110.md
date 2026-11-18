# Changelog RecordsNext 4.0 — v110, 18/11/2026

- Aggiunto `sameSeasonAliases` al registro locale, con validazione delle collisioni autorizzate e salvataggio persistente.
- Aggiunta fusione atomica di carriere Taïder e Neuton con documentazione delle due sorgenti FCA, soltanto dopo approvazione GUI e prova di giornate disgiunte.
- Integrato controllo disgiunzione `raw_{stagione}_fca_giocain` prima della distribuzione e **a ogni successiva pubblicazione**.
- Aggregazione History unisce i due record di una stagione conservando eventi, voti e `originiFCA` e non altera le statistiche delle altre stagioni.
- Eliminato falso positivo dell'audit per i gemelli El Azzouzi (Oussama, Anouar), senza modificarne le identità.
- Mbaye già unificato: nessuna modifica in questa patch.
- Versione v107 dell'installer pubblico NON aggiornata: release GitHub e FTP restano sospesi.
