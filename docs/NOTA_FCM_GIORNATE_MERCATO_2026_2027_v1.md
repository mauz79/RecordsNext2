# RecordsNext 4.0 - Giornate reali, inizio AlterLega e movimenti di mercato (v1)

Data: 2026-11-02. Fonte: FCM/FCA 2026/27 forniti dall'utente e database RAW testato mediante importatore 3.2.

## Decisioni confermate dall'utente

- Al momento dello snapshot le giornate REALI di Serie A sono **nove** (1-9).
- Le competizioni fantasy AlterLega 2026/27 **iniziano alla terza giornata reale di Serie A** e arrivano alla nona nello snapshot. Le giornate 1-2 reali non sono quindi partite fantasy mancanti.
- E' probabile, ma non ancora approvato come regola tecnica, che la cronologia dei tesseramenti faccia riferimento alla giornata reale di Serie A.
- Varela / FortisSIMY: svincolo accidentale e successivo reinserimento sono una **correzione operativa** confermata, non una seconda identita' del calciatore.

## Riscontri nei campi sorgente, senza inferenze

- `raw_2026_2027_fcm_tesserato`: 813 righe; non contiene date di validita', comprende `ID`, `IDGIOCATORE`, `IDSQUADRA`, `VENDUTO`.
- `raw_2026_2027_fcm_entryinbilancio`: 876 righe; contiene `DATATRANSAZIONE` e `ULTIMAGIORNATA`, oltre alla descrizione e al riferimento di acquisto `IDTESSERATO` (seriale Access importato come data). Non confondere `ULTIMAGIORNATA` con un intervallo di validita' certificato.
- Movimenti osservati per data: 2026-08-31: `ULTIMAGIORNATA=2`; 2026-10-02: `5`; 2026-10-05: `6`; 2026-10-08: `6`.
- Varela/FortisSIMY: acquisto originale 31-08-2026 (tesseramento 699), svincolo 02-10-2026, reinserimento 05-10-2026 (tesseramento 752). Stesso giocatore FCA locale 629, codice storico 102681; entrambe le righe vanno conservate.
- Nell'archivio aggiornato l'assemblatore stagionale ricostruisce 28.956 fatti FCA, 7.650 fatti fantasy, 813 eventi `Tesserato`, 812 combinazioni distinte calciatore/fantasquadra; non deduce esclusioni da assenze in `Formazione`.

## Non ancora approvato o non provato

- `ULTIMAGIORNATA` non e' stato certificato quale giornata di decorrenza del trasferimento o di fine possesso; non derivare una data di validita' da `DATATRANSAZIONE` o da tale numero senza verificare le regole di FCM.
- Non classificare i tesseramenti ceduti e riacquistati solo sulla coincidenza delle date: conta la **sessione d'asta** e le correzioni possono avvenire in tempi diversi.
- Non produrre un record di voto sprecato per un calciatore mai tesserato nell'incontro o non effettivamente escluso, finche' la disponibilita' alla giornata non sia dimostrabile.
- Lo schema Java di questa fase conserva gli eventi `Tesserato` ma non integra ancora la cronologia completa di `EntryInBilancio` in `TopFlopArchive`.

## Prossime verifiche

1. Definire con l'utente la semantica temporale dei movimenti FCM sulla base di casi reali; convalidare `ULTIMAGIORNATA` e date contro le aste.
2. Aggiungere, se confermato, un modello distinto per operazioni di mercato, senza sostituire gli ID originari dei tesseramenti.
3. Verificare lo stesso codice su Windows e solo dopo proseguire con esclusioni/medie e pubblicazione 3.2.
