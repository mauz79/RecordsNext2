# RecordsNext 4.0 - Specifica metriche rendimento individuale non utilizzato v2

Data 2026-11-01. Sostituisce soltanto la **formulazione** della specifica v1, che resta conservata.

## Scopo

Per un singolo calciatore appartenente a una fantasquadra, misurare il valore dei voti e fantavoti conseguiti quando non ha contribuito al punteggio fantasy. Non simulare chi si sarebbe dovuto schierare o quante sostituzioni erano possibili.

## Contratto matematico approvato

Per il perimetro `stagione + fantacalciatore storico + fantasquadra + competizione` (ulteriori aggregazioni da approvare):

- `E`: numero totale di partite fantasy valide nelle quali il calciatore era effettivamente disponibile per quella fantasquadra ma e stato lasciato fuori dal punteggio.
- `P`: numero fra quelle `E` partite in cui il calciatore ha ricevuto un voto reale valido.
- `SV`: somma dei **voti reali validi integrali**, per le `P` occasioni.
- `SF`: somma dei **fantavoti validi integrali**, per le stesse `P` occasioni, comprensivi di bonus/malus realmente applicabili.

| Metrica | Formula | Quando definita |
|---|---|---|
| Media voto sprecato | `SV / P` | `P > 0` |
| Errore pesato sul voto | `SV / E` | `E > 0` |
| Fantamedia sprecata | `SF / P` | `P > 0` |
| Errore pesato sul fantavoto | `SF / E` | `E > 0` |
| Quota esclusioni con voto | `P / E` | `E > 0` |

Le medie quando `P=0` sono `null` / non definite. Se `E>0, P=0`, i due errori pesati valgono 0. Se `E=0`, **tutti** gli indicatori sono non definiti. Le esclusioni senza voto non generano valori inventati ma contano nel denominatore `E`. Non usare `SV-6*P`, non tagliare i voti a 6, non imporre cap numerici.

Equivalenza di controllo: `pesatoVoto = mediaVoto * P/E` e `pesatoFantavoto = fantamediaSprecata * P/E`, se `P>0`.

## Esempi

- Tre esclusioni con voto `5.5, 7, 8`: `SV=20.5`, `P=E=3`, media voto=6.833333..., errore pesato=6.833333...
- Cinque esclusioni, ma solo tre con voto `5.5, 7, 8`: `SV=20.5`, `P=3`, `E=5`, media voto=6.833333..., errore pesato=4.1.
- Esempio **solo illustrativo** di fantavoti per gli stessi tre voti: `5.5, 10, 7.5`, ottenuti con nessun evento, `gol +3`, `ammonizione -0.5` rispettivamente. `SF=23`, fantamedia sprecata=7.666666..., errore pesato su 5 esclusioni=4.6. **Non dedurre** da questi numeri coefficienti universali di FCM.
- `E=23`, `P=3`, `SV=18`: media voto=6 e errore pesato=18/23=0.782608...

## Granularita e fonti da verificare prima dell'implementazione sui dati

Una riga per esclusione verificata, con: identificativo storico calciatore, ID FCA della stagione, stagione, fantasquadra (e prova di tesseramento in quel momento), competizione, incontro fantasy, giornata reale, voto originale fonte Gazzetta, fantavoto completo e relativo metodo/fonte di calcolo, dettaglio eventi con bonus/malus, stato voto valido/SV, classificazione fuori punteggio e tabellino/scorecard URL.

Il fantavoto di un giocatore lasciato fuori **non e necessariamente** il totale applicato al punteggio squadra: prima verificare da quali tabelle sorgente si possa ricostruire per tutte le esclusioni e secondo le regole della stagione/competizione. Non trasformare automaticamente `Tabellino.Tot` in fantavoto universale senza verifica.

Una prestazione reale FCA deve esistere una sola volta per calciatore+stagione+giornata; piu esclusioni in competizioni fantasy parallele rimangono fatti fantasy distinti. Prima di produrre una classifica all-time scegliere esplicitamente il perimetro di aggregazione.

Riserve d'ufficio (RU), codici artificiali `0`/`-1`, voti d'ufficio senza prestazione reale e SV non vanno confusi con un voto effettivo del calciatore. I riposi e gli incontri non disputati non diventano automaticamente esclusioni. Non simulare le sostituzioni (che possono essere 3, 5, 8, 11 secondo le impostazioni FCM).

## Questioni ancora aperte

1. Definizione verificabile di `lasciato fuori` nei casi panchina non utilizzata, giocatore non inserito in `Formazione`, `POS=-1` e anomalia dei flag di entrata.
2. Fonte e regole esatte dei fantavoti per un giocatore fuori dagli undici, inclusi bonus/malus diversi per stagione/competizione.
3. Criterio di voto reale valido vs SV e speciali, rinvii e partite recuperate; verifica su campioni FCM/FCA.
4. Perimetro di classifiche multi-competizione e stagionali/all-time: evitare doppi conteggi senza eliminare fatti fantasy.
5. Facoltativo: massimo voto/fantavoto di una singola esclusione; nessun cap imposto.

## Stato implementativo

Contratto matematico approvato e testato **solo su dati sintetici**. Nessun record storico reale calcolato, nessuna modifica a Java/SQLite/GUI/sito.
