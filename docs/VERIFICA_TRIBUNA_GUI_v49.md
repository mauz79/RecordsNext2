# RecordsNext 4.0 v49 — Diagnostica tribuna e correzioni GUI

Data 12/11/2026; solo copia Windows TEST, non release pubblica.

## Osservazione utente e correzione

- Un giocatore che apparteneva **alla rosa nella giornata** e non fu inserito nella formazione puo' essere un errore, non soltanto chi rimase inutilizzato in panchina. La v48 non includeva le tribune: TF11–TF13 sono quindi una **sottoanalisi**, non la misura completa degli errori.
- Nel FCM storico `tesserato` conserva l'archivio di stagionalita' (e `VENDUTO`, `IDSOSTITUITO`), **non un libro movimenti con data certa**. Per esempio, nello snapshot 2023/24 risultano 210 righe con VENDUTO=1 e 304 ID giocatore associati a piu' fantasquadre. Non attribuire arbitrariamente un calciatore a una rosa a ogni giornata.
- L'audit Java `TopFlopTribunaAuditV49` conta separatamente (a) assenze da una formazione per le quali esiste almeno un'altra formazione della stessa squadra nella stessa giornata Serie A che include il calciatore; (b) assenze sostenute **solo** da uno snapshot stagionale del tesseramento, indimostrabili alla data. Distingue per entrambi i casi la disponibilita' di un voto FCA reale valido.
- I contatori sono esportati nel `contatori.auditTribuna` di **ogni stagione** della facade `fcmRecordsNext_TopFlopViews.js`. Nessuna lista nominativa di presunte esclusioni viene pubblicata; nessun numero di questo audit altera TF11–TF13, punteggi, denominatori, statistiche di carriera, identita' o JS precedenti.
- **Importante:** una formazione nella stessa giornata Serie A e' una prova di presenza nelle formazioni di quel turno, non certifica da sola proprieta' e disponibilita' tra due diverse date di consegna; per questo anche la categoria (a) resta diagnostica.

## GUI v49

- **Soglie**: 11 `JSpinner` (campionato, coppa, carriera reale, carriera fantasy, esclusioni e anomalie) devono rimanere visibili nella stessa larghezza della scheda. Eliminato lo scroll orizzontale e costretto l'HTML della nota a capo; il pannello segue la larghezza del viewport. Valori configurabili nei limiti e persistenti in `config/topflop.json`.
- **Identita'**: rimosso l'avviso automatico all'apertura di Top & Flop. Le 351 proposte sospese restano tali e sono rivedibili **solo** col pulsante `Riconcilia identita' storiche dei calciatori`. Le 2.839 approvazioni non vengono toccate.

## Soglie predefinite v48, confermate v49

| Chiave | Valore predefinito | Significato |
|---|---:|---|
| `realSeasonVotes` | 5 | voti minimi per media reale stagionale TF05 |
| `fantasyLeagueMatches` | 5 | gare fantasy minime di campionato TF06 |
| `fantasyCupMatches` | 3 | gare fantasy minime nelle coppe TF06 |
| `realCareerVotes` | 20 | voti minimi carriera reale |
| `fantasyCareerMatches` | 20 | gare minime carriera fantasy |
| `verifiedExclusions` | 3 | panchine documentate minime TF11–TF13 |
| `exclusionsWithVote` | 2 | panchine documentate con voto minimo TF11/13 |
| `anomalySeasonVotes` | 8 | voti minimi nella stagione candidata TF14/15 |
| `anomalyBaselineVotes` | 20 | voti minimi nelle altre stagioni TF14/15 |
| `anomalyBaselineSeasons` | 2 | stagioni alternative minime TF14/15 |
| `anomalyDelta` | 0.75 | differenza minima fra media voto candidata e baseline TF14/15 |

## Validazioni e limiti

- Java 21: 95+ sorgenti compilati, smoke `TopFlopTribunaAuditV49SmokeTest` e `TopFlopSettingsGuiV49SmokeTest` superati in modalita headless con viewport 730/810 px. Integrare i precedenti smoke per identita', panchina, anomalie e sharding.
- Il calcolo delle tribune certe **per tutto lo storico** e' ancora sospeso finche' non esiste un registro di possesso datato o un criterio attendibile da approvare. Non confondere la diagnostica con un errore certo.
- Prossimo gate: audit dei 302 JS reali v48 e nuova facade v49, poi prova multisito su **copie**. Non ripetere elaborazioni complete per provare soltanto la disposizione dei campi o il popup.
