# Changelog v109 — riconciliazione guidata delle carriere storiche

- Aggiunta finestra modale `Carriere spezzate (audit v108)` all'interno della GUI Top & Flop.
- Lettura SQLite `mode=ro` e controllo `query_only`, senza scrittura sul database.
- Evidenza per 21 stagioni, raggruppamento solo per candidature nome+nascita (non fusione automatica).
- Selezione multipla verificata in memoria; backup e salvataggio atomico registro solo dopo conferma esplicita.
- Conferma individuale rafforzata per canoniche già approvate distintamente; collisioni stagionali escluse.
- Installazione JAR con backup, verifica SHA256, test Maven e rollback.
- Nessuna modifica al motore di calcolo, all'exporter, ai 21 siti o al database FCA/FCM.
- Gate release 4.0.0 SOSPESO fino all'esito del nuovo audit v108.
