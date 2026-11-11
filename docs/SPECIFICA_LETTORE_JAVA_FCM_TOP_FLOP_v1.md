# RecordsNext 4.0 - Records Top & Flop - Lettore FCM JDBC v1

Data: 2026-11-01. Modulo nativo Java, **read-only**, additivo. Non sostituisce il motore di importazione, gli archivi, gli exporter, la GUI o la pubblicazione di RecordsNext 3.2.

## Fonti e relazioni effettivamente usate

Per ogni stagione `AAAA_AAAA` importata nel medesimo SQLite della 3.2:

- `raw_<stagione>_fcm_formazione.IDINCONTRO -> fcm_incontro.ID`.
- `fcm_incontro.IDGIRONE -> fcm_girone.ID -> fcm_competizione.ID`.
- `(Formazione.IDINCONTRO, Formazione.IDSQUADRA) -> Tabellino.(IDINCONTRO, IDSQUADRA)`.
- `Formazione.IDGIOC -> fca_giocatorea.ID` **nella stessa stagione**, da cui `CODICEGAZZA`.
- `fca_giocain.(IDGIOCATORE, GIORNATA) = (Formazione.IDGIOC, Incontro.GIORNATADIA)`.
- `Formazione.IDSQUADRA -> fcm_fantasquadra.ID`; si legge esclusivamente `NOME`, **non** indirizzi, contatti o password.
- `fcm_tesserato.IDGIOCATORE -> fca_giocatorea.ID`; i tesseramenti sono letti separatamente per non moltiplicare le righe `Formazione`.

`TopFlopFcmJdbcReader.loadSeason(Connection, seasonId, Sink)` fornisce al chiamante le righe in streaming. Non conserva l'intera stagione in RAM. Il chiamante fornisce una connessione JDBC come nella 3.2; il test usa esclusivamente `mode=ro`.

## Dati esposti per ogni episodio FCM

- Identita annuale FCA, codice storico Gazzetta, fantasia squadra (ID e nome), ID incontro, ID competizione, nome e famiglia CAMPIONATO/COPPE.
- `GIORNATADIA` reale e `IDGIORNATA` FCM **distinti**; `IDGIORNATA` e una chiave, NON un ordinale di calendario. Conservata anche la descrizione `Giornata.DESC`.
- Stato fonte `Formazione.POS`, `ENTRATO`, `ENTRATORISERVA`, `MANUALE` e il ruolo.
- Posizione verificata nella lista `Tabellino.LISTA` e i corrispondenti valori TESTUALI `VOTO`, `MODIF`, `TOT` (inclusi decimali con virgola e `sv/ng`).
- `firstElevenSlot` indica unicamente la posizione nei primi undici slot; **non** attesta la legalita delle sostituzioni, la presenza effettiva o il contributo regolamentare.
- L'incontro conserva `GIOCATO`, `IDTIPO`, ID squadra casa/fuori e ID girone; la disponibilita dello storico non e inferita.
- `Tesserato` e una fotografia sorgente con ID riga e dettagli originali; non prova la titolarita del tesseramento in ogni singola giornata.

## Limiti intenzionali

- **Non** costruisce URL tabellini: questi saranno ottenuti con le logiche **gia funzionanti della 3.2**, che includono le configurazioni del sito/stagione. Non costruire link da ID incontro o root corrente.
- Non assegna `Usage.EXCLUDED_CONFIRMED` da `Formazione` assente o da `Tesserato`.
- Non determina ancora fantavoti dei giocatori completamente non schierati; i valori del tabellino di una riga presente sono i valori memorizzati nella sorgente, non un voto inventato.
- Non sostituisce `TopFlopArchive`, non calcola record/medie, non attiva la famiglia `TOP_FLOP` nella GUI/pipeline/publisher.
- Ignora per record e schede pubbliche la Youth League, ma ne conserva il **conteggio tecnico**.
- I codici `0`/`-1` nei tabellini sono slot speciali e non generano righe giocatore.
- Il modello e' un estrattore di FCM disponibile **per righe `Formazione` esistenti**. Le assenze da formazione e la disponibilita storica saranno esaminate separatamente.

## Verifiche gia eseguite sui database 3.2 disponibili

Sia `recordsnext.db` sia `recordsnext-sandbox.db`: 21 stagioni, 552.025 righe `Formazione` originali, 546.775 ammesse, 5.250 Youth League escluse; tutti gli slot dei giocatori ammessi trovati, 14.320 righe `Tesserato` lette senza perdite. FCM 2026/27 non contiene ancora righe `Formazione` ma ha tesserati. I due database differiscono per una parte dell'FCA 2026/27, non per questi conteggi FCM.

I link, le metriche, gli aggregati storici e la connessione definitiva al modello Top & Flop saranno sviluppi **successivi**, sempre nel perimetro della 3.2.
