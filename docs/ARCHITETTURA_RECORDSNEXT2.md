# Architettura RecordsNext 3.1.0

## Architettura multisito 3.1

La 3.1 separa definitivamente le sorgenti storiche dalle destinazioni di pubblicazione.

```text
FCM / FCA / DataA canonico
            |
            v
database + report + archivi RecordsNext
            |
            v
scope per stagione target
            |
            +--> sito stagione corrente
            |
            +--> siti storici selezionati
```

Regole:

- FCM/FCA/DataA descrivono i dati storici;
- il sito locale e' opzionale e non e' una sorgente necessaria dello storico;
- `DataA` canonico: `data/calendars/DataA-YYYY.js`;
- un sito configurato e disponibile e' soltanto una destinazione;
- una stagione senza sito continua a contribuire allo storico;
- il cutoff temporale usa `rn_season.sort_order`;
- un target non puo' ricevere stagioni successive alla propria;
- Core e Manifest vengono rigenerati coerentemente con il target;
- gli altri dataset vengono generati da viste temporanee contenenti soltanto le stagioni ammesse;
- il publisher non deve creare interi siti FCM mancanti: pubblica soltanto dove esiste una destinazione configurata.

### Modalita di pubblicazione

`CURRENT_SITE`:
- usata dall'elaborazione normale quando e' selezionata la pubblicazione;
- aggiorna soltanto il sito associato alla stagione corrente.

`ALL_CONFIGURED_SITES`:
- esposta in GUI come `Pubblica i siti delle stagioni selezionate`;
- considera soltanto i target delle stagioni selezionate;
- salta le destinazioni configurate ma non disponibili;
- per ogni target costruisce lo scope storico fino a quel `sort_order`.


## Ciclo di vita delle stagioni

Una stagione RecordsNext e una entita persistente del database e non coincide con i soli parametri della GUI.

La cancellazione di una stagione deve quindi essere transazionale e coinvolgere tutti i dati interni collegati.

Principi consolidati:

1. eliminare i mapping appartenenti alla stagione;
2. eliminare team e competizioni stagionali;
3. eliminare calendario e sorgenti associate alla stagione;
4. eliminare registrazioni FCM/FCA interne (`rn_source_file`);
5. eliminare la configurazione della stagione;
6. eliminare infine `rn_season`.

Le identita canoniche di squadra e competizione non devono essere distrutte se sono utilizzate da altre stagioni.

Quando una identita canonica e ancorata alla stagione eliminata:

- viene cercata la stagione mappata piu recente ancora esistente;
- `anchor_season_id` e l'anchor stagionale dell'identita vengono spostati su tale stagione;
- se non esiste alcuna altra occorrenza, l'identita viene eliminata.

Dopo la cancellazione viene inoltre ricalcolata l'anchor globale: la stagione gestita piu recente rimasta diventa `rn_season.is_anchor = 1`.

La rimozione dal database RecordsNext non comporta la cancellazione dei file sorgente FCM/FCA/DataA dal filesystem ne delle directory dei siti FCM.


## Scopo

RecordsNext 2.1 genera viste dati tematiche complete e filtrabili, non semplici classifiche finali.

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

## Dataset canonico Matches

RecordsNext 2.1 introduce:

`fcmRecordsNext_Matches.js`

Ãˆ il dataset pubblico canonico delle partite.

Per ogni incontro reale contiene esattamente due righe:

- una dalla prospettiva della prima squadra;
- una dalla prospettiva dell'avversaria.

Ogni riga contiene almeno:

- stagione;
- competizione;
- identificativo incontro;
- giornata;
- numero giornata;
- link tabellino;
- squadra e identificativo canonico;
- avversario e identificativo canonico;
- gol fatti;
- gol subiti;
- risultato standardizzato `V/N/P`.

I turni di riposo non appartengono al dataset Matches.

Questo output permette ai visualizzatori e ad altri consumer di ricostruire
le statistiche basate sulle singole gare senza dipendere da HTML o JavaScript legacy.

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


## Architettura dei visualizzatori HTML

### Regola vincolante

Gli HTML a corredo di RecordsNext 2.1 non devono contenere dati incorporati, record precalcolati, nomi di squadre o classifiche scritte nel markup.

Devono essere visualizzatori statici dei JavaScript generati dal programma. Questa separazione consente di:

- distribuire gli HTML insieme al plugin;
- installarli nella skin una sola volta;
- aggiornare i dati senza riscrivere le pagine;
- cambiare grafica senza rigenerare i dati;
- offrire una base utile a chi vuole costruire visualizzatori propri.

### Strati

```text
HTML statici
    |
    v
fcmRecordsNextFunzioni_common.js
fcmRecordsNextFunzioni_viewer.js
    |
    v
fcmRecordsNext_Manifest.js
fcmRecordsNext_Core.js
fcmRecordsNext_*.js
```

Responsabilita:

- HTML: struttura semantica, aree filtri, navigazione e contenitori risultati;
- CSS: resa grafica del profilo scelto;
- JS funzioni: controllo disponibilita, lettura dati, filtri, ordinamenti e rendering;
- JS dati: dati completi, metadati, stato degli output e link ai tabellini.

### File statici installabili nella skin

```text
recordsnext.html
RecordsNext\classici.html
RecordsNext\serie.html
RecordsNext\riserve_ufficio.html
RecordsNext\modificatori.html
RecordsNext\soglie_fortuna.html
RecordsNext\culometro.html
RecordsNext\recordsnext.css
js\fcmRecordsNextFunzioni_common.js
js\fcmRecordsNextFunzioni_viewer.js
```

Nei nomi dei nuovi file RecordsNext si usa l'underscore, non il trattino.

### File dati generati

I file `fcmRecordsNext_*.js` vengono prodotti dalla pipeline e pubblicati nella cartella `js` del sito. Non fanno parte del pacchetto statico della skin.

### Profili grafici

I profili iniziali sono:

- `mauzstrom`: Trebuchet MS come font principale;
- `fantablue2`;
- `neutral`: stile moderno autonomo ispirato ai principi grafici di ReNewo.

La GUI installa sempre gli stessi HTML e JS di rendering. Il profilo scelto determina soltanto il file copiato come `RecordsNext\recordsnext.css`.

### Dati mancanti

La presenza di una pagina HTML non implica la presenza del relativo output dati. In assenza del JS richiesto, la pagina deve mostrare uno stato vuoto leggibile e non produrre errori JavaScript.

### Soglie, Fortuna e Culometro

Soglie e Fortuna resta una famiglia dati autonoma. Nella GUI viene raggruppata con il Culometro perche il Culometro dipende da questi indicatori e puo dipendere anche da componenti delle Riserve d'Ufficio.

Il Culometro resta opzionale e viene generato soltanto quando selezionato esplicitamente. Anche `culometro.html` resta un visualizzatore statico senza dati incorporati.


## Stato finale della release 3.1.0

L'architettura multisito descritta in questo documento è stata validata anche su siti reali:

- 21 target, da `2006_2007` a `2026_2027`;
- 9 file JavaScript canonici per target;
- 189 file validati;
- 189 file pubblicati;
- nessun errore di pubblicazione;
- cutoff storico verificato.

La distribuzione pubblica della 3.1.0 avviene esclusivamente tramite installer clean-install Inno Setup. Il pacchetto UPDATE non fa parte dell'architettura di rilascio 3.1.
