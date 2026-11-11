
# RecordsNext 4.0 - Famiglia Records Top & Flop - Specifica v1

Data 2026-11-01. **Decisione di prodotto approvata**, implementazione Java NON ancora eseguita.

## Scopo

**Records Top & Flop** e una nuova famiglia nativa del sistema RecordsNext 4.0, accanto a Classici, Serie, RU, Modificatori, Soglie/Fortuna. Ha una selezione di records nella GUI, viene elaborata dalla pipeline standard, archiviata per stagione e pubblicata mediante il publisher e lo shard manager esistenti. La scheda giocatore non e una famiglia aggiuntiva: consulta lo stesso dataset canonico che alimenta Top & Flop.

**Distinzione semantica:** `TOP_FLOP` = famiglia record nel catalogo tecnico; `CAMPIONATO`, `COPPE` = famiglie sportive di competizioni usate come filtro. Nessuna Youth League nei dati pubblici. Play Off - Play Out e Spareggio Serie B ricadono in CAMPIONATO.

## Modello dei fatti e chiavi

- Entita giocatore storico (codice Gazzetta dal FCA, identita canonica esplicita per collisioni), ID FCA locale di stagione, anagrafica storica originale e provenienza.
- Prestazione reale: una sola riga per giocatore storico verificato + stagione + giornata Serie A, anche se lo stesso voto compare in piu incontri fantasy. Stato senza voto/speciale distinto dalla presenza effettiva.
- Tesseramento FCM: stagione, fantasquadra, giocatore annuale, eventuale cronologia realmente verificabile; lo snapshot Tesserato non prova la durata temporale.
- Evento fantasy: stagione + competizione ID + incontro FCM ID + fantasquadra ID + calciatore annuale, anche per campionato e coppa nello stesso turno. Distinguere titolare iniziale, conteggiato, panchinaro, riserva, fuori lista, non schierato provato, voto e fantavoto e bonus/malus; non inventare assenze.
- Collegamento record puntuale: stagione, competizione, fantasquadra, id incontro, giornata FCM, giornata Serie A e link `scorecardUrl` risolto dal normalizzato 3.2; URL ignoto = null/stato da risolvere, mai concatenare la root del sito corrente a un sito storico.

## Indicatori personali approvati

Per ogni contesto fantasy appropriato, sia per titolari sia per non utilizzati, conservare tutti i voti, fantavoti ed eventi. Per le esclusioni certe:

`E` = esclusioni totali, `P` = esclusioni con voto valido, `SV` = somma voti validi, `SF` = somma fantavoti validi con bonus/malus storici.

- Media voto sprecato: `SV/P` (P>0).
- Indice pesato stagionale voto: `SV/E` (E>0).
- Fantamedia sprecata: `SF/P` (P>0).
- Indice pesato stagionale fantavoto: `SF/E` (E>0).

Un 7 rimasto fuori contribuisce 7, non 1; assenza senza voto contribuisce 0 al numeratore e rimane nel denominatore se l'esclusione e accertata. RU e sostituzioni storiche (3,5,11 ecc.) rispettano gli originali ma una RU non e una prestazione individuale reale.

Gli stessi fatti di voto e fantavoto sono disponibili **per i titolari e i conteggiati**: la scheda e i record devono essere simmetrici. Non simulare formazioni alternative o risultati di squadra per stimare l'errore individuale.

## Scoping e doppio conteggio

Ogni primato ha dimensioni esplicite: giocatore, stagione, squadra fantasy, competizione, famiglia sportiva CAMPIONATO/COPPE, insieme delle competizioni, carriera, partita. Non aggregare piu competizioni come se fossero giorni reali differenti. Le graduatorie reali usano le prestazioni FCA una sola volta; le statistiche di impiego fantasy possono legittimamente avere piu eventi nella stessa giornata e devono mostrarlo.

## Registro dei record

Categorie candidabili, da definire con il catalogo 3.2 e il successivo confronto col regolamento: migliori/peggiori voti e fantavoti, medie, bonus/malus, presenze, titolarita, frequenze di esclusione, medie e indici di spreco, singolo picco per partita, dati stagionali e all-time. Questo elenco **non costituisce ancora un catalogo approvato di singoli record**: nomi, soglie minime, limiti e tie-break rimangono aperti.

## Stato di implementazione

In questa fase sono stati ricontrollati i punti di estensione della 3.2. Gli exporter reali/fantasy sviluppati nelle fasi precedenti sono prototipi di sola lettura, non costituiscono il motore records in Java. La famiglia TOP_FLOP non deve essere selezionabile dalla GUI finche pipeline, exporter, manifest, shard e test non saranno completi.
