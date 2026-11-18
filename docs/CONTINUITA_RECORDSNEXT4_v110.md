# Continuità RecordsNext 4.0 — 18 novembre 2026 — v110

## Stato realmente confermato

- 4.0 nel plugin `E:\FCM\plugin\Mauz_strom2026\RecordsNext`, 3.2 disinstallata con recupero di 5.757.930.755 byte, 21 siti locali pubblicati e 5.417 file JS generati (prima delle ultime correzioni).
- v108 audit: 3.139 gruppi, 328 carriere non unificate in quel momento; nessun errore indice, nessuno History shard mancante. L'utente ha poi approvato carriere in GUI v109, incluso **MBAYE MAODO già risolto**; non riscrivere quelle decisioni.
- Restavano Taïder 2014/15 doppia anagrafica 606+738; Neuton 2013/14 doppia anagrafica 798+448; due El Azzouzi gemelli Oussama/Anouar che devono restare distinti.
- **NON è ancora confermato** che la v110 sia installata o che le due coppie abbiano giornate FCA disgiunte sul DB reale: occorre esecuzione preflight su Windows e approvazione nella GUI.
- Tutte le approvazioni personali in `config/player-identities.json` del plugin su E:, non in Git; mai caricarlo nel repository.
- La release pubblica 4.0.0 e l'upload Internet sono SOSPESI fino a nuovo audit post-pubblicazione e collaudo.

## Patch v110

- `TopFlopPlayerIdentities.java`: alias same-season espliciti, guard persistenti.
- `TopFlopHistoricalViews.java`: aggregazione esatta delle sorgenti della stessa stagione solo se alias approvato, provenienza conservata.
- `TopFlopViewsPublication.java`: giornate FCA non sovrapposte ricontrollate prima di pubblicare; alias passati al calcolo.
- `TopFlopHistoricalCareerDialogV109.java`: decisioni esclusivamente nominative Taïder e Neuton con controllo codici/nascita/giornate, mai fusione indiscriminata; cognome composto El Azzouzi distingue i due fratelli.
- `RN4_Audit-Carriere-Pubblicate-v110.py`: raggruppamento El Azzouzi corretto per i controlli successivi.
- Installer v110: controlla stato sorgenti, SQLite read-only, Maven, backup RC/JAR; non modifica mai database o siti.

## Procedura successiva

Installare v110; selezionare Taïder e Neuton uno per volta e approvare dalla GUI; aggiornare elenco; una sola elaborazione multisito dalla GUI; controllo con audit v110; solo con esito verificato riprendere Git + nuovo installer pubblico (il vecchio v107 NON contiene la patch).
