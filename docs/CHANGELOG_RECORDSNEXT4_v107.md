# Changelog RecordsNext 4.0 — v107 (18/11/2026)

- Conclusa la migrazione personale da 3.2 a 4.0, con verifica SQLite e rimozione della precedente installazione (log `RN4_V106_3_2_ELIMINATA`).
- Pubblicati 5.417 JS sui 21 siti locali, ciascuno con cutoff storico. Vecchi JS oltre 1,5 MB di altro plugin non modificati.
- Git v103 sul branch `feature/recordsnext4-v67-rc-gui-migrated`, commit verificato remoto `a16f21ffdd045ff88ad26317a3b594737783c60c`.
- Nuova release: versione Maven `4.0.0`, installer Inno Setup 7 per installazione **pulita** e aggiornamenti **4.0→4.0** che non sovrascrivono dati/configurazioni.
- Setup privo di DB privato, `config/`, archivi FCA/FCM, `player-identities.json` e log personali; runtime UCanAccess incluso; scelta della cartella nel wizard; Java 21+ richiesto.
- Al primo avvio la GUI inizializza SQLite e `processing.json` assenti, poi l'utente configura le stagioni.
- Nessun cambiamento ai calcoli, Java publisher, identificazioni approvate o sito pubblico.
- In sospeso: certificazione delle carriere potenzialmente spezzate nel registro attuale; nessuna associazione automatica.
