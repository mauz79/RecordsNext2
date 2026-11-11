# RecordsNext 4.0 - Top & Flop - Lettore FCA JDBC v1

Data: 2026-11-01. **Modulo nativo Java, additivo, sola lettura**. Connesso ai dati raw SQLite gia importati dal motore 3.2; non sostituisce l'importatore, l'exporter, la pipeline o la GUI della 3.2.

## Cosa legge

`TopFlopFcaJdbcReader.loadSeason(Connection, TopFlopArchive, seasonId, gazzaCode)` utilizza `raw_<stagione>_fca_giocatorea`, `raw_<stagione>_fca_giocain` e `raw_<stagione>_fca_punteggio`. La connessione JDBC e ricevuta dal chiamante; il test integrativo apre gli archivi SQLite con `mode=ro`.

- **Anagrafiche**: `SeasonLoad.playersByLocalId` comprende anche i calciatori senza una giornata `GiocaIn`. `PlayerProfile.originalFields` conserva tutti i campi originali di `GiocatoreA`.
- **Prestazioni giornaliere**: ogni `(stagione, GiocatoreA.ID, GiocaIn.GIORNATA)` genera **un solo** `RealDay` nell'archivio Top & Flop. Il collegamento `GiocaIn.IDPUNTEGGIO -> Punteggio.ID` replica la logica del modello 3.2.
- `GiocatoreA.ID` resta **locale alla stagione**. `CodiceGazza` rimane il codice esterno; valori 0/non positivi non sono trattati come identita storica valida. Collisioni storiche non risolte automaticamente.
- `RealDay.sourceFields` salva i valori non nulli di tutti i campi delle tre tabelle con prefissi `GiocatoreA.`, `GiocaIn.`, `Punteggio.`; i SQL NULL sono rappresentati da un campo tecnico `__SQL_NULL__.<origine>.<colonna>=true`. Questo evita di equiparare valore assente e stringa vuota. `RealDay.events` conserva le colonne evento numeriche di Punteggio senza applicare coefficienti.
- `Punteggio.VOTO1 > 0` -> `VALID`, con voto originale. `VOTO1 < 0` e nessun valore speciale -> `NO_VOTE`; voto 0/NULL -> `UNKNOWN` (non e prova di mancata presenza); `VALORESPECIALE != 0` senza voto positivo -> `SPECIAL`. Non e lecito dedurre automaticamente `NOT_PLAYED` dalla sola riga `GiocaIn`.
- `VOTO2`, `VOTO3`, `VALORESPECIALE` e i flag `FINDER` sono conservati nei campi fonte, **non** scambiati per eventi o per voto Gazzetta. Restano disponibili per le analisi successive.
- Nessun bonus/malus viene calcolato: i coefficienti vanno dalla competizione/anno quando leggeremo FCM.

## Integrita e limiti

- Il nome della stagione e validato come `AAAA_AAAA`, le colonne sono lette dal catalogo SQLite ed i nomi non validi vengono rifiutati.
- In modalita non filtrata, il numero di righe `GiocaIn` deve coincidere con le righe collegate al giocatore e con quelle collegate a `Punteggio`. Le chiavi duplicate incoerenti generano un errore, non una sovrascrittura.
- `loadSeason` e pensato per **una stagione alla volta**: non mantenere tutte le 21 stagioni simultaneamente in RAM.
- Lettore ancora limitato a `GiocatoreA`, `GiocaIn`, `Punteggio`. Non importa ancora `Statistiche`, `Storico`, `IncontroInA`, FCM, rose temporali o tesseramenti. Questi rimangono passaggi successivi. `RealDay` non e da solo una presenza reale certificata.
- Nessun `TOP_FLOP` attivato in `RecordFamily`, GUI, exporter, JS, manifest o siti. Nessun record pubblicato.

## Risultati verificati su due DB diversi, senza modificarli

Archivio ZIP 3.2 fornito dall'utente, dati `recordsnext.db` e `recordsnext-sandbox.db`:

| Fonte | Stagioni FCA | Anagrafiche stagionali | GiocaIn | VOTO1 validi |
|---|---:|---:|---:|---:|
| recordsnext.db | 21 | 16.245 | 617.310 | 202.145 |
| recordsnext-sandbox.db | 21 | 16.211 | 616.018 | 201.851 |

La differenza e **interamente nel 2026/27**, con 34 anagrafiche e 1.292 giornate in piu nel database principale. Non indichiamo un DB come errato: si tratta di due snapshot distinti. I test non codificano mai un totale globale fisso.

Caso 2025/26, Lautaro (`CodiceGazza=100843`): 38 giornate sorgente, 30 con `VOTO1` valido. Presente con questo codice in nove stagioni. Codice 12302 presente in tre stagioni, con identita storiche da verificare.

## Collegamento futuro, non attivo

I `RealDay` alimenteranno le schede e Top & Flop, ma il collegamento ai contesti FCM verra creato **solo tramite logiche gia verificabili della 3.2** e conservando scorecardUrl canonica, competizione, fantasquadra e matchId. Youth League esclusa da output pubblici; Play Off - Play Out e Spareggio Serie B = CAMPIONATO. Le classifiche sui voti inutilizzati richiederanno uno stato di esclusione comprovato, non la semplice assenza da `Formazione`.
