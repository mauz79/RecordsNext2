# RecordsNext 4 — Specifica Fase 2: archivio partecipazioni fantasy v1

## Fonte, unità e confini

- Fonte SQLite raw 3.2 in **sola lettura**, 21 stagioni FCM/FCA.
- Unità: `(stagione, idIncontroFCM, idSquadraFCM, Formazione.IDGioc)`; non si deduce che ogni riga sia entrata o abbia contribuito al punteggio.
- `GiocatoreA.ID` e `Formazione.IDGioc` sono ID **annuali**. `GiocatoreA.CodiceGazza` collega la ricerca storica ma necessita validazione di collisioni prima delle aggregazioni all-time.
- Nessuna creazione di link web storici prima di recuperare i percorsi canonici della 3.2; conservare l'ID incontro e tutti i dati necessari.

## Catena dati esatta

`Formazione.IDIncontro -> Incontro.ID -> Girone.ID -> Competizione.ID`; `Incontro.IDGiornata -> Giornata.ID`; `Formazione.IDSquadra -> Fantasquadra.ID` (solo ID/nome esportati); `Formazione.IDGioc -> GiocatoreA.ID` (stessa stagione); `Incontro.GiornataDiA + Formazione.IDGioc -> GiocaIn.Giornata + IDGiocatore -> Punteggio.ID`; `Tabellino(IDIncontro,IDSquadra)` con `LISTA` esplosa e confronto sull'ID annuale.

## Struttura JSON

`RecordsNext4.PlayerFantasy.v1`:

- `stagione`, `giocatoreIdFca`, `codiceStoricoSorgente`, `giocatoreFca`.
- `idIncontroFcm`, `idSquadraFcm`, `squadraFantasy` (solo ID/nome), `avversarioFantasy`, `lato`.
- `competizioneIdFcm`, `competizioneNomeFcm`, `gironeIdFcm`, `gironeNomeFcm`, `giornataFcm`, `giornataReale`.
- `incontroFcm`, `formazioneFcm` originali; `giocaInFca`, `punteggioFca` originali.
- `statoTabellino`: `SLOT_UNICO | SLOT_ASSENTE | SLOT_MULTIPLO | LUNGHEZZE_DIVERSE | TABELLINO_ASSENTE | TABELLINO_MULTIPLO`; `slotTabellino` con `ordine`, ID, ruolo, voto, modificatore, totale **tutti stringhe raw**; `riepilogoSquadraTabellino` contiene solo campi aggregati non individuali.
- `tesseramentiFcmSnapshot`: righe `Tesserato` combacianti con coppia squadra/giocatore al momento dello snapshot, **senza inferire trasferimenti**.

`--code 100843` produce JSON con tutte le partecipazioni delle stagioni disponibili e manifest SHA-256. `--all --season 2025_2026` produce JSONL.gz per riga Formazione e manifest, senza aggregazioni.

## Casi speciali e restrizioni

- I codici `LISTA=-1` (ufficio) e `LISTA=0` (segnaposto) **non** sono ID di calciatori. Si mantengono separati dalla join dei giocatori; il loro audit complessivo e una fase distinta.
- Slot `VOTO`, `MODIF`, `TOT` con virgole decimali e valori `sv/ng` sono preservati **senza conversione numerica o somma**.
- Non equivalere `Formazione.ENTRATO` a prestazione reale, ne `ENTRATORISERVA` a una sostituzione valida senza interpretare la regola FCM.
- Se manca `Incontro`, `GiocaIn` o slot, conservare la riga FCM e marcare assenza; non cancellare né fabbricare dati.
- I tabellini privi di `Incontro` restano fuori da questa estrazione per Formazione e sono reportati separatamente in Fase 0; potrebbero riferirsi anche a particolarità/riposi.
- Il dato 2026/27 nella copia DB non ha ancora formazioni, dunque un file vuoto non certifica l'assenza nel FCM originale corrente.
- Nessuna fusione storica automatica dei codici riutilizzati (es. 12302); nessun calcolo punti inutilizzati/simulazione, nessuna produzione JS, GUI o commit.

## Sicurezza dati

La tabella `Fantasquadra` contiene anche contatti e campi password: il prototipo estrae **solo ID e nome** delle squadre per evitare di inserire dati privati nei file di output/diagnostica.
