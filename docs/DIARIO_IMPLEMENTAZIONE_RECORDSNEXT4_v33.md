# RecordsNext 4.0 - Diario v33: riconciliazione GUI (11 novembre 2026)

## Base e regole immutate
- Patch incrementale sulla copia isolata `D:\DEV_APPS\RecordsNext4.0_TEST_v37` con v40 installata.
- Nessuna modifica a 3.2, FCA/FCM, SQLite, siti pubblicati, manifest/shard legacy o archivio approvazioni.
- **Nessuna approvazione automatica** delle identita calciatori. Le decisioni continuano a essere registrate soltanto su esplicita conferma dell'operatore.
- Le scelte `PERSONE DISTINTE` restano protette dal motore v40, comprese le fusioni indirette.

## Difetto v40 e correzione v41
- In v40 la selezione multipla con almeno un caso `requiresIndividualReview` veniva bloccata integralmente (`I casi incerti o gravi vanno confermati uno alla volta`).
- Nella stessa finestra era richiesto di digitare `UNISCI <codice>` prima della conferma.
- In v41 le varianti non critiche selezionate sono approvabili in un unico passo **previa anteprima e conferma SI esplicita**.
- I casi critici eventualmente compresi nella stessa selezione richiedono **ciascuno una conferma SI/NO distinta**, con nomi, stagioni, date di nascita, valutazione e fonti LCS a schermo. Annulla non apporta nessuna modifica, NO lascia sospeso soltanto quel caso.
- Non e piu richiesto digitare parole o codici. Il pulsante non puo unire un codice gia impostato `PERSONE DISTINTE`: per correggere la decisione bisogna revocarla esplicitamente.
- Le scelte approvate di una selezione vengono persistite in una sola operazione transazionale con rollback in caso di errore; non si salva una fusione parziale se un'altra e incompatibile.

## Verifiche locali
- Compilazione Java 21 di 91 sorgenti `src/main/java` ricostruiti dalla base v40.
- `TopFlopBatchReviewSmokeTest`: OK, separazione rischio, annullamento, salvataggio e ricaricamento.
- `TopFlopIdentityEvidenceSmokeTest`: OK, 569 alias correnti, 1531 storici, 2133 controlli.
- `TopFlopSeparatedIdentitySmokeTest`: OK, Nicola Ascoli 2 annate / Roberto Crivello separato.
- `TopFlopHistoricIdentitySmokeTest`: OK, media ponderata e cutoff storico.
- Colloquio Swing fisico su Windows e ricompilazione Maven da effettuare con lo script protetto nella copia TEST.

## Evidenze storiche controllate, non ancora correzioni canoniche attive
- Okaka Stefano Chuka: CodiceGazza `13032` (2006/07, 2008/09-2010/11), `73032` (2011/12-2014/15) e `100981` (2018/19-2021/22). Le connessioni fra i diversi codici devono essere APPROVATE esplicitamente nella GUI, senza alterare gli ID FCA originali. Il semplice riconoscimento delle varianti dentro ciascun codice non garantisce da solo un'unica identita storica.
- Eduardo Wilson Lino `15872`: presente in FCA 2008/09 come `LINO`, con la squadra `Lecce` in una giornata e 37 giornate `SERIE ESTERA`; nel 2011/12 come `LINO Eduardo Wilson`, associato al `Lecce`. Nessun tesseramento FCM nelle due stagioni. Gli FCA riportano nascita `01/01/1990`; data corretta fornita dall'utente `22/03/1991`. Non riscrivere FCA.
- Ragatzu (`14726`): il nome `Mauro` nel 2008/09 e errato, secondo l'utente e la data FCA 21/09/1991 corrisponde a Daniele.
- Rojas (`101381`): nome canonico dichiarato `Luis Jose Esteban Rojas Zamora`, nato `06/03/2002`.
- Cabrera (`116104`): nome canonico dichiarato `Jonni Zacarias Cabrera Mendez`, nato `14/06/1989`.
- Mbaye (`128901`): nome canonico dichiarato `Maodo Malick Mbaye`, non `Serigne`.
Queste evidenze sono registrate per la prossima funzione di approvazione dei NOMI e delle DATE canoniche. Non sono ancora modifiche ai JS, alle identita salvate o alla GUI.
