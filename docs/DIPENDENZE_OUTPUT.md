# Dipendenze output RecordsNext 2.0

## Principio

Una dipendenza mancante blocca soltanto il figlio interessato ed e dichiarata nel manifest.

## Stati

- `GENERATED_COMPLETE`
- `GENERATED_PARTIAL`
- `SKIPPED_REQUIRED_DEPENDENCY`
- `SKIPPED_NOT_SELECTED`
- `SKIPPED_NO_DATA`

## Capitano

Senza Capitano:

```text
modifiers.captain-total -> SKIPPED_NOT_SELECTED
series.captain-bonus    -> SKIPPED_REQUIRED_DEPENDENCY
```

Le altre serie continuano.

## Fattore Campo

Richiede punteggi, risultato, bonus casa e configurazione delle soglie gol.

## RU decisive

Richiedono RU identificate, formazione, punteggio e simulazione senza RU. I semplici conteggi possono essere prodotti senza simulazione.

## Soglie e Fortuna

Gli eventi soglia richiedono punteggi, soglie gol e risultato. Gli indicatori derivati richiedono gli eventi selezionati.

## Culometro

Richiede selezione esplicita, configurazione dedicata, componenti, pesi e normalizzazione. Se non selezionato: `SKIPPED_NOT_SELECTED`. Questo non rende incompleta la famiglia.


## Aggiornamento Serie complete v2

La famiglia Serie include vittorie consecutive, pareggi consecutivi, sconfitte consecutive, imbattibilita, serie senza vittorie, serie Capitano e serie clean sheet. Stato: `GENERATED_COMPLETE`.
