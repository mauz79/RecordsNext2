# RecordsNext 4.0 - Piano operativo v8

Base 3.2.0: commit `f9bf30323d9204d70e91e242bddfc72c2060ecf9`.
Worktree: `D:\DEV_APPS\RecordsNext4.0`.

Tutte le decisioni precedenti restano nei documenti versionati precedenti, che non vengono sovrascritti.

## Stato

- Fase 0: audit fonti/identita completati; casi ambigui documentati e aperti.
- Fase 1: estrattore dati reali Python testato in Windows, 7/7; nessuna integrazione Java.
- Fase 2: estrattore dati fantasy Python testato in Windows, 8/8; 552.025 righe Formazione, collegamenti univoci ai tabellini; DB immutato.
- Fase 3A: audit semantico di `Formazione.POS`, `ENTRATO`, `ENTRATORISERVA`, `Incontro.GIOCATO`, slot `Tabellino.TOT` (nessun calcolo punti persi).
- Fase 3B: solo dopo verifica, regole su utilizzo, riserve, RU, contribuzione e punti non sfruttati.
- Fase 4: integrazione nel modello canonico SQLite/Java, export JS, pagine HTML, GUI.
- Fase 5: regressioni, aggiornamento bibbia, commit, release 4.0.

## Criteri per la Fase 3B

1. Separare titolari POS=0, panchina POS>0, fuori formazione POS<0.
2. `TOT` numerico NON significa voto conteggiato; `ENTRATO` e `ENTRATORISERVA` devono essere interpretati alla luce di FCM e RU.
3. Distinguere almeno: contributo realmente utilizzato, voto rimasto inutilizzato in panchina, prestazione di un calciatore non schierato, eventuale contributo recuperabile sotto le regole reali, simulazioni controfattuali.
4. Documentare il denominatore per squadra-partita, stagione, competizione; non sommare la stessa prestazione FCA attraverso gare fantasy differenti.
5. Per RU e sostituzioni, confrontare il codice esistente della 3.2: `docs/ANALISI_PERSONALE_RU.md`, `tools/Report_RU_Storico_Completo_v8_4.ps1` e gli altri report RU; non cancellarli o rietichettarli come record pubblici.
6. Preservare riposi e tabellini orfani in diagnostica separata.

## Vincoli

Nessuna modifica 3.2, DB, pipeline pubblica, HTML, JS, GUI o branch Git. Niente deployment o commit. Un nuovo file richiede suffisso versione; includere sempre codice, test, SHA-256, piano, registro, diario e bibbia. Output per l'utente in `D:\DEV_APPS\downloads`.
