# RecordsNext 4.0 - Specifica archivio completo v2

# RecordsNext 4.0 - Specifica dell'archivio completo per calciatore v1

Data: 2026-11-01. Specifica di modello e contratto; il codice Fase 3C0 implementa per ora un audit di contesti, non l'archivio canonico definitivo.

## Obiettivo approvato

**Ogni calciatore** ha un set completo di dati reali e fantasy, anche se non acquista mai valore per un record o per una classifica. Le statistiche devono includere **titolari iniziali, giocatori realmente conteggiati, subentrati, panchinari non utilizzati, fuori formazione e, dove verificabili, tesserati non inseriti in Formazione**. I record e la scheda giocatore sono viste sullo stesso dataset, non raccolte di dati ridotti in partenza.

### 1. Identita e prestazione reale (livello non replicato)

- **Calciatore storico**: codice `GiocatoreA.CODICEGAZZA`, con associazione manuale canonica per eventuali collisioni, alias e cambi codice; non confondere con `GiocatoreA.ID` annuale.
- **Anagrafica stagionale**: `(stagione, GiocatoreA.ID)`; conserva nome, ruolo, squadra reale secondo FCA, dati di identita e riferimenti foto se disponibili.
- **Giornata reale**: `(stagione, GiocatoreA.ID, GiocaIn.GIORNATA)` univoca. Conserva `GiocaIn`, `Punteggio` e statistiche originali, voto di fonte, bonus/malus originali e informazioni sulla partita reale. Nessuna duplicazione per presenza del calciatore in piu fantasquadre o competizioni.
- `GiocaIn` non equivale automaticamente a presenza effettiva o voto valido; `Storico` non deve essere sommato nuovamente a giornate gia conteggiate.

### 2. Tesseramento fantasy (livello per fantasquadra)

- **Collegamento**: `(stagione, GiocatoreA.ID, Fantasquadra.ID)`; un giocatore puo avere piu squadre nella stessa stagione, persino in serie diverse.
- La tabella FCM `Tesserato` nella copia 3.2 contiene ID, giocatore, squadra, ruolo, anno contratto, venduto e altri campi, **ma non un intervallo cronologico affidabile di inizio/fine tesseramento**. La riga e quindi uno snapshot, non una prova che fosse disponibile in qualunque giornata.
- Non classificare automaticamente come esclusione ogni partita della squadra in cui manca `Formazione`: bisogna provare il tesseramento valido in quella data e il fatto che la gara fantasy sia valida, disputata e non un riposo.

### 3. Evento di utilizzo fantasy (livello per singolo incontro)

Chiave di origine minima: `(stagione, Incontro.ID, Fantasquadra.ID, GiocatoreA.ID)`. Ogni evento contiene:

- Squadra fantasy, avversario, competizione, girone, famiglia (`CAMPIONATO` o `COPPE`), giornata fantasy e giornata Serie A, ID incontro e futura URL canonica del tabellino.
- Schieramento FCM originale: `Formazione.POS`, `RIG`, `ENTRATO`, `ENTRATORISERVA`, `MANUALE`; presenza/ordine in `Tabellino.LISTA`; voto/tabellino/modificatore/totale grezzi.
- Collegamento alla **stessa** prestazione reale FCA, se presente. Regole originali della competizione per fonti voto, bonus/malus, RU e sostituzioni (anche 3, 5, 8, 11).
- Stato iniziale (titolare/panchina/fuori lista) e **stato di effettivo utilizzo** sono campi distinti. `POS=0` descrive il titolare iniziale e non dimostra da solo che abbia contribuito; `ENTRATO` non basta per i casi anomali documentati.
- RU e codici speciali 0/-1 sono slot artificiali di tabellino, **non calciatori**: non generano prestazioni reali o bonus/malus di un calciatore.
- Se un calciatore compare in due competizioni nella stessa giornata, si conservano due eventi fantasy e un solo riferimento alla prestazione reale.

### 4. Famiglie di competizione

Le famiglie richieste dall'utente sono esattamente **CAMPIONATO** e **COPPE**; mantenere inoltre l'identita della singola competizione. La famiglia e una classificazione **esplicita e storicizzata**, non una deduzione dal nome in runtime. Censite 25 denominazioni storiche nelle 180 configurazioni FCM.

- Mappatura iniziale verificabile da denominazione per 22 nomi palesi (4 campionati, 18 coppe/supercoppe), come riportato nel nuovo audit: e una **proposta di classificazione**, non una decisione irreversibile.
- **Da approvare:** `Play Off - Play Out`, `Spareggio Serie B`, `Youth League`. Conservare le righe con famiglia non assegnata, non eliminarle e non inventare una terza famiglia di record.
- Anche dentro la stessa famiglia possono esserci piu incontri nella stessa giornata reale: mai accorparli prima di aver deciso la semantica di aggregazione.

### 5. Tutte le statistiche: utilizzati ed esclusi

Per ogni contesto, archiviare **voto originale, fantavoto con bonus/malus, dettaglio dei singoli eventi e stato di validita**, indipendentemente dall'uso fantasy del calciatore. Le medesime statistiche derivate vanno disponibili per i **conteggiati**, per gli **esclusi** e per tutti i casi classificabili separatamente.

Per il sottoinsieme **escluso**, confermate le quattro metriche della specifica v2:

- `E` = esclusioni valide; `P` = occasioni escluse con voto valido; `SV` = somma voti validi; `SF` = somma fantavoti relativi.
- Media voto sprecato `SV/P`; errore pesato voto `SV/E`; fantamedia sprecata `SF/P`; errore pesato fantavoto `SF/E`; quota voto `P/E`.
- Nessuna sottrazione di 6 o simulazione di un'altra formazione; esclusioni senza voto non danno errore, pur rimanendo nel denominatore `E`.

Per il sottoinsieme **conteggiato** prevedere in modo simmetrico numero incontri conteggiati, numero con voto valido, somme e medie di voto/fantavoto, eventi, bonus/malus, massimi, minimi e serie cronologiche. Le formule dei record specifici restano demandate al catalogo record; il dato va comunque conservato per ogni singola occorrenza.

### 6. Aggregazioni per record e scheda

Filtri indipendenti: `calciatore`, `stagione`, `fantasquadra`, `competizione`, `famiglia`, `giornata reale`, `partita fantasy`, `iniziale/conteggiato/escluso`, `voto valido`, `ruolo`.

- **Aggregati reali**: un solo record a calciatore/stagione/giornata, senza moltiplicare gol e assist per le fantasquadre.
- **Aggregati fantasy**: possono includere piu fatti per calciatore/giornata se giocati in squadre/competizioni diverse; esplicitare sempre il perimetro della classifica.
- Per `tutte le squadre` o `tutte le coppe` **non sommare medie**: sommare numeratori e denominatori e ricalcolare. Non sommare ciecamente due aggregati gia costruiti con diverse politiche di deduplicazione.
- Se lo stesso calciatore era nelle rose di tre fantasquadre, le esclusioni, le titolarita, i punteggi e gli indici saranno specifici di ogni squadra; la carriera all-time puo presentarli separati e, con una regola approvata, riaggregati.

### 7. Stato dei dati e questioni aperte

Il nuovo audit `Audit-RecordsNext4Contesti-v1.py` usa solo le 552.025 righe `Formazione` gia esistenti. Non attesta ancora le **esclusioni per assenza totale dalla formazione**; senza la storia temporale dei tesseramenti e una verifica delle partite/riposi non e corretto generarle. Non calcola voti, fantavoti o punti sprecati. Sono invece testate la tenuta delle chiavi dei contesti e l'esistenza di contesti paralleli.

Prima del dataset definitivo:

1. Accertare la cronologia dei tesseramenti e la copertura degli incontri non presenti in `Formazione`.
2. Verificare il voto/fantavoto di un non schierato, gli SV, il voto d'ufficio e la variazione dei bonus/malus per competizione.
3. Approvare classificazione delle tre competizioni rimaste senza famiglia.
4. Decidere il perimetro delle classifiche trasversali con piu gare nella stessa giornata e nella stessa famiglia.
5. Aggiungere eventi di fonte e ID/link stabili per la scheda individuale, rispettando regole di pubblicazione JS della 3.2.

Non modificare la pipeline Java, il database, la GUI o il sito fino ai test di integrazione.

## Addendum v2 - decisioni vincolanti del 2026-11-01

### Classificazione approvata

- `Play Off - Play Out` = **CAMPIONATO**.
- `Spareggio Serie B` = **CAMPIONATO**.
- `Youth League` = **ESCLUSA SEMPRE** dai dataset pubblici individuali, aggregazioni, record, medie e schede. Può comparire soltanto in report tecnici di audit delle fonti, con conteggio esplicito delle righe escluse. Non introdurre una terza famiglia.
- Le due famiglie restano CAMPIONATO/COPPE; conservare sempre `competitionId` originario, nome esatto e stagione.

### Provenienza e link della singola prestazione fantasy

Per un record legato a un incontro FCM, il fatto deve conservare almeno:
`playerCode` (e ID FCA locale), `seasonId`, `teamId`, `competitionId`, `competitionName`, `competitionFamily`, `matchId` (= Incontro.ID), `serieARound` (= Incontro.GIORNATADIA), `fantasyDayId` (= Incontro.IDGIORNATA), `scorecardUrl`, `scorecardLocalUrl`, `scorecardOnlineUrl`, `scorecardLinkStatus`.

- Recuperare gli URL **dall'export canonico della 3.2 per la stagione e il sito di destinazione**. Il codice della 3.2 espone `urlTabellino`, `urlTabellinoLocale`, `urlTabellinoOnline`, ed esporta `scorecardUrl`. La fonte della rotta deve essere quella storica, non il percorso della stagione corrente.
- Il generatore 3.2 usa per un URL relativo un pattern `../lega{anno}/ris.htm?Gio={giornata}` e per i siti configurati `onlineSiteUrl` un link risolto alla root di quel sito. **Non generalizzare la estensione**: alcuni siti legacy possono usare `ris.php`; servono URL reali risolti e testati sul sito di destinazione. Non concatenare `lega2025/lega2018`.
- Non ricavare il `matchId` dalla sola giornata: piu incontri e competizioni possono condividere `GIORNATADIA`. Salvare l'ID evento e, se possibile, un riferimento a una specifica ancora/elemento nella pagina solo dopo averne verificato l'esistenza.
- Se l'URL non e ancora disponibile dai dati normalizzati, memorizzare `null` con stato `DA_RISOLVERE_DA_EXPORT_CANONICO_3_2`; **mai inventare il link**.
- Applicare la stessa tracciabilita ai record individuali di voto/fantavoto, gol, assist, espulsioni e singola partita. Per record multi-partita conservare gli ID/link degli episodi di supporto, non un unico link fuorviante.

### Limite dell'audit 3C1

`Audit-RecordsNext4Contesti-v2.py` corregge la classificazione e non include Youth League negli aggregati o esempi utilizzabili. Conta comunque le righe Youth negli indicatori di provenienza, e mette `scorecardUrl=null` negli esempi finche l'export 3.2 non fornisce la rotta. **Non implementa ancora il nuovo archivio Java, il join ai link pubblici o record finali.**
