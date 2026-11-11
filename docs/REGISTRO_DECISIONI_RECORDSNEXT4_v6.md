# RecordsNext 4.0 — Registro decisioni v6

Si applicano tutte le decisioni D-001–D-026 del documento v5, che NON viene sostituito. Stati: **CONFERMATA** (richiesta utente o dato verificato), **IMPLEMENTATA PROTOTIPO** (scelta tecnica reversibile), **APERTA** (da decidere / verificare).

## Esiti Fase 1 Windows

- **D-027 — CONFERMATA:** lo ZIP Windows `RecordsNext4_Fase1_OUTPUT_v1_20261031_213342_462754.zip` contiene 7 test riusciti; reali 2006/07: 657 giocatori e 24.966 righe `GiocaIn`; reali 2025/26: 783 giocatori e 29.754 righe `GiocaIn`. Il numero 6.050 si riferisce invece a `Formazione` fantasy nel 2006/07.
- **D-028 — CONFERMATA:** hash sandbox prima/dopo Fase 1 uguali (`57923f4e75986c2b81533e4beef15b9133635258d7de1d36827a1bdbe576671b`). Nessuna modifica al database per questo prototipo.

## Decisioni Fase 2

| ID | Stato | Testo | Evidenza |
|---|---|---|---|
| D-029 | CONFERMATA | L'unità di partecipazione fantasy è `(stagione, idIncontroFCM, idSquadraFCM, idGiocatoreFCA)`, non solo `codice + giornataSerieA`. | 552.025 `Formazione` su 21 stagioni, senza duplicati su questa chiave. |
| D-030 | CONFERMATA | `Formazione` e `Tabellino` vanno collegati attraverso incontro, squadra e ID giocatore **locale**; `CodiceGazza` serve per trovare il giocatore nello storico. | Tutte le 552.025 righe Formazione trovano slot univoco in LISTA, oltre a GiocaIn per giornata reale. |
| D-031 | CONFERMATA | Le liste `LISTA`, `RUOLO`, `VOTO`, `MODIF`, `TOT` sono allineate per posizione. Il codice `-1` rappresenta una voce d'ufficio; `0` è segnaposto: non attribuire questi valori a un calciatore. | Esempi FCM ed export precedente; preservare testi `sv/ng` e virgole decimali. |
| D-032 | CONFERMATA | Le righe fantasy in `Formazione` includono anche riserve non entrate e incontri non disputati; non equiparare un inserimento in Formazione a punti realmente conteggiati. | `ENTRATO` / `ENTRATORISERVA`, `Incontro.GIOCATO` disponibili in raw. |
| D-033 | IMPLEMENTATA PROTOTIPO | Esportare ogni riga FCM con score FCA e **solo lo slot personale del tabellino**, conservando raw e metadati delle competizioni. Non esportare indirizzi email/password delle fantasquadre. | `Estrai-RecordsNext4Fantasy-v1.py`, test database e verifica su un giocatore. |
| D-034 | IMPLEMENTATA PROTOTIPO | `Tesserato` è snapshot: esporre le possibili righe di tesseramento con la fantasquadra, ma NON assumere intervalli di contratto o trasferimento. | Un giocatore può risultare in più squadre nella stessa stagione. |
| D-035 | APERTA | Punti non sfruttati: definire prima differenza fra voto lasciato in panchina, contributo recuperabile, totale realmente conteggiato e simulazione di formazione valida. | Ancora nessuna metrica calcolata. |
| D-036 | APERTA | Identità storica canonica, link tabellini storico/multisito, appartenenza competitiva canonica e RU saranno integrati dopo i test semantici. | Nessuna attribuzione arbitraria nel prototipo. |

## Questioni ancora aperte

Rimangono valide Q-001–Q-012 della v5, incluso campo FCM dell'ID storico da verificare, collisioni di codice, 168 riferimenti Tabellino senza Incontro, regole ufficiali, giorni reali non abbinati, FCA manuale 2005/06 e dati 2026/27 aggiornati. Nessuno di questi casi è stato risolto per supposizione.
