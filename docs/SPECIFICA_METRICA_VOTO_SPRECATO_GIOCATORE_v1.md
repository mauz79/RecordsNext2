# RecordsNext 4.0 - Specifica metrica voto sprecato del calciatore v1

Data: 2026-11-01. Fonte della formula: decisione esplicita dell'utente, dopo il completamento dell'audit 3B2.

## Oggetto

Il record e del **calciatore**, osservato mentre appartiene a una fantasquadra. Non e una ricostruzione del punteggio alternativo della squadra, non richiede di rigiocare sostituzioni, ruoli o moduli.

## Formula approvata

Per ogni calciatore e per il perimetro scelto (stagione, fantasquadra, competizione, storico):

- `E`: numero di occasioni/partite fantasy valide nelle quali il calciatore e stato lasciato fuori;
- `V`: numero di tali occasioni nelle quali il calciatore ha una **prestazione reale con voto valido**;
- `S`: somma dei valori di voto validi riferiti a quelle `V` occasioni.
- `M = S / E`, se `E > 0`. Se `E == 0`, la media e **non definita**, non zero.

La metrica e **M**, non `S / V`. Mostrare facoltativamente `S / V` con un'etichetta diversa, per non confondere le medie. Le `E - V` altre occasioni non producono un voto fittizio ma non spariscono dal denominatore.

Esempio approvato: E=23, V=3, voti 6+6+6, S=18, M=18/23 circa 0,783. La media delle prestazioni valide e 6,00, ma NON coincide con M.

La parola "massimo" dell'utente resta da chiarire: non fissare un cap numerico; mantenere eventuale record del massimo separato dalla media finche non specificato.

## Riserve d'ufficio

Le RU sono voti sostitutivi assegnati d'ufficio al tabellino, non calciatori reali. PU, DU, CU e AU sono categorie storiche del fenomeno. Non generano alcun voto sprecato del calciatore e non devono identificare un `GiocatoreA`.

## Relazioni da usare

- Fonte voto/presenza: dati FCA per giocatore e giornata, sorgente voti Gazzetta della lega.
- Identificativo FCA `GiocatoreA.ID`: solo locale alla stagione; collegamento per `Formazione.IDGioc` e `GiocaIn.IDGiocatore`.
- Identificativo storico: `GiocatoreA.CodiceGazza`, da controllare per collisioni di anagrafiche. Nessuna aggregazione cross-season per ID locale.
- Fonte esclusione: FCM `Formazione` e collocazione in `Tabellino.LISTA`; presenza nei primi 11 slot e indizio di utilizzo, **non prova sufficiente isolata** nei tabellini eccezionali gia documentati.
- Ogni record deve mantenere partita fantasy, fantasquadra, competizione, giornata reale e identificativo del calciatore; non duplicare la prestazione FCA reale per il solo fatto che compare in piu competizioni fantasy.

## Da validare prima dell'estrattore finale

1. Che cosa si considera "lasciato fuori": riserva non entrata, non schierato (`POS<0`), fuori dagli undici finali del tabellino; usare esempi reali e non la sola interpretazione del flag `ENTRATO`.
2. Come si identificano le partite fantasy valide effettivamente disputate; preservare riposi, incontri con squadra 0, tabellini non collegati e partite non giocate.
3. Quale valore numerico sommare: `Punteggio.Voto1` (Gazzetta, voto base) oppure fantavoto `Tot`; conservarli entrambi, non assumere che siano intercambiabili.
4. Come trattare `SV`, voti speciali, partite reali rinviate e giocatori con voto d'ufficio: nessuna trasformazione in voto reale arbitrario.
5. Rischio duplicazioni quando competizioni diverse hanno lo stesso giocatore per la stessa giornata reale: conservare la granularita fantasy, concordare la chiave per classifiche stagionali/all-time.
6. Eventuale denominazione o soglia del "valore massimo" rimane indefinita.

## Test di contratto

`tools/Test-RecordsNext4FormulaVotoSprecato-v1.py` e un test di formula su **dati sintetici**, non una prova della corretta attribuzione dei voti reali in FCM/FCA.

## Non obiettivo

Niente simulazioni di formazione vincente, recuperabilita dei punti, vincoli di sostituzione, calcolo controfattuale del risultato. Le differenze storiche tra 3, 5, 8 o 11 sostituzioni restano registrate, ma non sono un prerequisito della formula individuale.
