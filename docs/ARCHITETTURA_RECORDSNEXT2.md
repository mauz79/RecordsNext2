# Architettura RecordsNext 2.0

## Scopo

RecordsNext 2.0 genera viste dati tematiche complete e filtrabili, non semplici classifiche finali.

## Flusso

```text
FCM / FCA / configurazioni
            |
            v
normalizzazione stagioni, squadre e competizioni
            |
            v
dataset comune delle partite
            |
            v
elaboratori delle famiglie
            |
            v
viste dati JS modulari
            |
            v
viste HTML filtrabili
```

## Famiglie

### Classici

Punteggi, medie, somme, risultati, gol, disciplinari, assist, autogol, rigori, clean sheet e aggregati per squadra, giocatore e portiere. Le sequenze appartengono a Serie.

Output: `fcmRecordsNext_Classics.js`

### Serie

Serie positive e negative, vittorie, pareggi e sconfitte consecutive, clean sheet consecutivi e serie dipendenti da eventi o modificatori.

Output: `fcmRecordsNext_Series.js`

### Riserve d'Ufficio

PU, DU, CU, AU, partite con e contro uffici, uffici decisivi, bilanci, medie e distribuzioni.

Output: `fcmRecordsNext_RU.js`

### Modificatori

Difesa, Capitano, altri modificatori selezionati e Fattore Campo.

Output: `fcmRecordsNext_Modifiers.js`

### Soglie e Fortuna

Vittorie chirurgiche, sconfitte beffa, pareggi miracolati e stretti, mezzo punto, soglie precise, spreco punti e indicatori di fortuna/sfortuna.

Output: `fcmRecordsNext_ThresholdsLuck.js`

## Culometro

Easter egg opzionale: non viene generato automaticamente; richiede selezione e configurazione dedicate.

Output: `fcmRecordsNext_Culometro.js`

## Dipendenze

Ogni figlio dichiara dipendenze obbligatorie e opzionali. Una dipendenza mancante blocca solo il figlio interessato.

Esempio: senza Capitano non vengono generati i figli Capitano e le relative serie, ma le altre serie continuano.

## Ambiti temporali

- stagionale: valore per squadra e stagione;
- globale: aggregato su tutte le stagioni dell'identita canonica;
- assoluto: migliore o peggiore occorrenza fra tutte le stagioni;
- storico continuo: sequenza che puo attraversare piu stagioni.

Non tutti i record supportano tutti gli ambiti.

## Identita canoniche

Squadre e competizioni conservano identita e nome stagionale, identita e nome canonico, stagione e stato dell'associazione.

## Filtri minimi

Famiglia, figlio, squadra stagionale e canonica, stagione, competizione stagionale e canonica, casa/trasferta/neutro, squadre attuali/tutte e ambito temporale.

## Tabellini

Ogni record riferito a una partita specifica conserva identificativo, link locale e online, stagione, competizione, giornata, squadre, risultato e data.

## Fuori perimetro iniziale

Palmares, albo d'oro, promozioni, retrocessioni, bilancio prossimo turno, confronti diretti e altre funzioni non-record.
