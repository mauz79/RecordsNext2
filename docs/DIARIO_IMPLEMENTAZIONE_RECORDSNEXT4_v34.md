# RecordsNext 4.0 – Diario v34 – 11 novembre 2026

## Requisiti consolidati
- Codice Gazzetta non univoco, dati FCA/FCM originali immutabili.
- LCS 2015/2019/2020 come evidenze e nessuna approvazione senza clic.
- Regole RecordsNext 3.2 squadre e competizioni inalterate.
- Registro decisioni persistente in `config/player-identities.json`.

## Implementazione v42
- `TopFlopPlayerIdentities.Registry.approve()` può associare con conferma esplicita gruppi già approvati con codici Gazzetta diversi.
- Verifica delle stagioni duplicate nell'intero insieme delle carriere, non solo nella proposta.
- Conserva le protezioni su persone già dichiarate distinte e la transazione atomica su errori.
- Memorizza in `mergeUndo` le associazioni precedenti delle fusioni sovrapposte per una revoca prudente; eventuali decisioni dipendenti richiedono revoca in ordine inverso.
- La finestra di conferma delle proposte mostra nomi, anni, codici, date e il numero delle carriere preesistenti da raccordare.
- L'audit distingue date giorno/mese invertiti da altri conflitti anagrafici.
- Nessuna pubblicazione, nessuna riconciliazione implicita, nessuna modifica alle identità già registrate da parte dell'installatore.

## Evidenze
L'audit del DB storico di riferimento, sui 16.262 record FCA, produce 3.165 proposte di cui 389 da verificare inizialmente. Tra queste ultime, 173 hanno semplicemente due date giorno/mese invertiti. Nella GUI Windows al 11/11/2026 le approvazioni registrate sono 2.840, le proposte sospese 353 e le collisioni ancora da decidere 0. Questa copia del registro non è presente nello ZIP del codice: resta sul PC e non viene sovrascritta.

## Test
- Compilation all main Java 21: OK.
- `TopFlopCrossCodeIdentitySmokeTest`: OK, tre codici Okaka, serializzazione, revoca, conflitto stessa stagione.
- `TopFlopSeparatedIdentitySmokeTest`: OK, protezione Ascoli/Crivello.
- `TopFlopIdentityEvidenceSmokeTest`: OK, fonti LCS e varianti.
- `TopFlopBatchReviewSmokeTest`: OK, conferme esplicite e persistenza.
