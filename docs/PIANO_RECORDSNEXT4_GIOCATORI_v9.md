# RecordsNext 4.0 - Piano operativo v9

Data: 2026-11-01. Base immutata 3.2.0, commit `f9bf30323d9204d70e91e242bddfc72c2060ecf9`.

## Stato verificato

- Fase 0: fonti e identificativi; le eccezioni di identita storica restano aperte.
- Fase 1: estrattore delle prestazioni reali FCA, verificato su Windows, 7 test superati; non integrato nel Java.
- Fase 2: estrattore fantasy FCM, verificato su Windows, 8 test superati; non integrato nel Java.
- Fase 3A: ZIP Windows `RecordsNext4_Fase3A_OUTPUT_v1_20261101_001023_187079.zip` verificato; 9 test superati, 552.025 Formazione, SHA di tutti e tre i report corretti, DB invariato, bibbia v7 generata.
- Fase 3B1: nuova diagnostica osservazionale che incrocia l'ordine in `Tabellino.LISTA` con `Formazione.POS`, `ENTRATO`, `ENTRATORISERVA`, `RUOLO`, `GIOCATO`; prima verifica locale 21 stagioni. Non attribuisce punti sprecati.
- Fase 3B2: verificare il significato del blocco iniziale del tabellino, RU, sostituzioni, configurazioni regolamentari (da `Competizione` e fonti 3.2); definire metriche distinte e casi esclusi, poi implementarle.
- Fase 4: integrazione canonica SQLite/Java, export JS partizionato per sito, HTML, GUI.
- Fase 5: regressioni, documentazione, commit, release 4.0.0.

## Vincoli non modificabili senza decisione esplicita

1. Ogni prestazione reale FCA e una sola per codice storico, stagione e giornata, indipendentemente dalle gare fantasy in cui compare.
2. Identificativo annuale `GiocatoreA.ID` e codice storico `CodiceGazza` sono diversi.
3. `Tabellino.LISTA` ha slot speciali (`0`, `-1`) che non sono identita di calciatori.
4. Non equiparare `ENTRATO=1` a contributo certo senza tabellino: nel 2012/13 esistono undici giocatori in testa a `LISTA`, tutti con `ENTRATO=0`, ma con totale squadra presente.
5. Distinguere panchina non utilizzata da fuori formazione (`POS<0`); nessun conteggio punti persi prima di definire la recuperabilita regolamentare.
6. Preservare i 4 report personali RU 3.2. I tabellini senza match e gli incontri con squadra zero non sono automaticamente errori; distinguere le casistiche.
7. Nessuna modifica a 3.2, database, siti, codice Java, GUI, manifest, record pubblici o branch di produzione.
8. Gli script sono installati in `D:\DEV_APPS\RecordsNext4.0`; gli ZIP diagnostici in `D:\DEV_APPS\downloads`.
9. Un nuovo file e versionato; aggiornare piano, registro decisioni, diario e bibbia con hash SHA-256 del codice integrale.
