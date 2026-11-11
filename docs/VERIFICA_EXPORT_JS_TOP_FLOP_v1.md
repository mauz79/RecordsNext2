# Verifiche esportatore JS Top & Flop — fase di staging v1

Stato: verificato in ambiente di sviluppo su copie ricevute, attesa validazione Windows nel worktree 4.0.

- JDK 21: compilazione completa di sorgenti 3.2 + codice Top & Flop.
- Due stagioni: 2006/07 e 2026/27 con dati reali, 1419 giocatori stagionali, 53922 giornate reali, 13700 contesti FCM, 17 JS, file massimo 949798 byte.
- Link 2006/07: 6050 contesti con scorecard URL preso dai report normalizzati 3.2 e validato per match/team; 2026/27: nessun report aggiornato disponibile, link correttamente non risolti.
- Test 21 stagioni, senza report normativi: 16262 giocatori stagionali, 617956 giornate, 554425 contesti fantasy ammessi, 389 JS, massimo 949991 byte. (Il numero di file cambia con URL disponibili, e questo è normale.)
- Verifica parser su tutti i file JSON-wrapped: conteggi e identità esatti, nessuna perdita/duplicazione dei contesti, nessuna Youth League, dimensioni inferiori a 1.500.000 byte.
- Prova integrale con tutti i report normalizzati 3.2: 21 stagioni, 430 JS, 546101 collegamenti canonici trovati, massimo 949991 byte e 372471522 byte complessivi nello staging; 8324 contesti senza link, non sintetizzati.
- Test negativo sul budget: una riga giocatore sintetica >1500000 byte viene respinta prima della scrittura.
- Test browser con Node `vm`: facade -> shard Summary -> dettaglio specifico, URL storico 2006/07 corretto, 2026/27 link non inventato.
- Nessun file pubblicato, nessun CSS/GUI/publisher modificato.

Il test Windows locale produrrà conteggi e `result.json` aggiuntivi; non dichiarare la fase completamente convalidata finché non si riceve e verifica lo ZIP diagnostico.
