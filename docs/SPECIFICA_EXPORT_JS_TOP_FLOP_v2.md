# RecordsNext 4.0 — Records Top & Flop — Specifica JS compatto v2

## Scopo e principio

La copia AUTORITATIVA dei fatti storici resta il database SQLite derivato dagli archivi FCM/FCA, tramite l'importazione della 3.2. I JS pubblici sono solo una proiezione per la consultazione web. Non creare una seconda banca dati completa e permanente né nuove logiche di pubblicazione.

La v2 ottimizza la codifica dello staging nativo Java già presente, senza cambiare i record o l'interpretazione dei campi. Non è ancora collegata al manifest/publisher multisito della 3.2 e quindi NON si pubblica direttamente.

## Struttura dei file

- `fcmRecordsNext_TopFlop.js` — facade leggera, per ogni stagione con `summaryFiles`, `detailFiles`, `linksFiles` e dizionari `teams`, `competitions`, `eventNames`, `roundDescriptions`, `usageEvidence`.
- `fcmRecordsNext_TopFlop.Summary.AAAA_AAAA.NNN.js` — una riga per giocatore stagionale, recap di voti reali, apporto per squadra e competizione, indice dello shard individuale. Usare i metadati della stessa stagione per decodificare i nomi.
- `fcmRecordsNext_TopFlop.Detail.AAAA_AAAA.NNN.js` — UNA riga compatta per giocatore, con tutte le giornate reali e tutti i contesti FCM ammessi. Stato del voto conservato anche per giornate senza voto; mai sostituirlo con zero.
- `fcmRecordsNext_TopFlop.Links.AAAA_AAAA.NNN.js` — ogni URL canonico unico per `(matchId,teamId)` nella stagione, invece di ripeterlo per ogni calciatore dello stesso incontro.
- `visualizzatori/js/fcmRecordsNextTopFlopCompactDecoder-v2.js` — decoder e caricatore differito (facade → riepilogo stagione → solo dettaglio calciatore → link della stagione su richiesta).

I valori giornalieri e fantasy sono array posizionali e gli eventi non-zero sono vettori alternati `[indiceEvento, valore, ...]`. Gli indici sono relativi ai dizionari della propria stagione. L'identità storica (`gazzaCode`) e l'ID FCA locale restano distinti.

### Contract degli array

Dettaglio: `[localFcaId,gazzaCode,nome,realDays,fantasy]`.

Giornata FCA: `[giornataSerieA,statoVotoOrdinal,voto?,eventi?]`; se non ha voto e non ha eventi sono presenti solo i primi due valori. Stati: `VALID,NO_VOTE,SPECIAL,NOT_PLAYED,UNKNOWN`.

Contesto FCM: `[matchId,competitionId,competitionRound,roundDescriptionIndex,giornataSerieA,teamId,usageOrdinal,titolareIniziale,tabellinoVote,fantasyVote,eventi,pos,entrato,entratoRiserva,firstEleven,modificatore,usageEvidenceIndex]`.

Riepilogo: `[localFcaId,gazzaCode,nome,detailFileIndex,days,withVote,voteSum,meanVote,eventi,contexts,contributions]`; ciascuna contribuzione `[teamId,competitionId,contexts,counted,voted,voteSum,meanVote,fantasySum,meanFantasy,eventi]`.

**Non usare gli ordini o gli indici di una stagione per decodificare file di un'altra stagione.** Il decoder è versione 2; il sito non deve mescolare shard v1 e v2.

## Identità e numerazioni

- Giornata reale: `serieARound` FCA/FCM reale; giornata della competizione: `competitionRound` canonica ricavata da `rn_event` della 3.2, mai `IDGIORNATA` FCM. Incontri non mappati: `competitionRound=null` e descrizione FCM originale.
- Nome squadra e nome competizione per anno sono metadati, non colonne ripetute in ogni partita.
- I link si risolvono per `(stagione,matchId,teamId)` esclusivamente dai report normalizzati della 3.2. Nessun URL dedotto.
- Youth League esclusa; Play Off/Play Out e Spareggio Serie B nel perimetro CAMPIONATO come deciso. Il problema canonico storico delle 100 righe 2022/23 resta aperto, senza alterare il mapping esistente.

## Limiti e caricamento

- TARGET 950.000 byte UTF-8 per singolo JS, LIMITE BLOCCANTE 1.500.000 byte; ogni file scritto viene ricontrollato su disco. Nessuna riga troncata.
- Una pagina web iniziale carica la facade e i riepiloghi delle sole stagioni richieste. Per la scheda del giocatore carica solo il relativo shard Detail e i link della stagione interessata.
- Lo staging compatto è deterministico rispetto all'hash del database e dei report normalizzati: eseguire di nuovo il runner sugli stessi dati riutilizza i file già verificati, senza creare copie di altri 60 MB.
- I vecchi staging v1 non vengono cancellati automaticamente. Prima ispezionarli e, solo dopo conferma dei dati, eliminarli esplicitamente; mai cancellare DB, export pubblicati o archivi storici.

## Contabilità e test obbligatori

Il v2 conserva esattamente il contratto semantico v1: voti e stati, fonti bonus/malus, apporto `COUNTED`, partecipazioni distinte in campionato/coppe, scorecard links e assenza di esclusioni inventate.

Test da effettuare: (1) confronto esaustivo v1/v2 decodificato sulle stagioni 2006/07 e 2026/27; unica differenza ammessa `detailFile` per il nuovo shard; (2) cardinalità sui 21 anni; (3) decodifica JavaScript del browser; (4) URL unici e invariati per la stessa partita/squadra; (5) file fisici <=1.500.000 byte e <=950.000 target; (6) database con hash identico; (7) nessuna modifica a pipeline, publisher, CSS, GUI o sito della 3.2.

## Esito sulla copia di riferimento del 6 novembre 2026

| Metrica | Staging v1 | Staging v2 |
|---|---:|---:|
| JS per 21 stagioni | 430 | 106 |
| Dimensione totale | 372.470.000 byte circa | **59.898.532 byte** |
| File più grande | 949.991 byte | **949.971 byte** |
| Giocatori stagionali | 16.262 | 16.262 |
| Giornate reali | 617.956 | 617.956 |
| Contesti fantasy | 554.425 | 554.425 |
| Riferimenti URL risolti nei contesti | 546.101 | 546.101 |

Riduzione totale circa 83,9%; confronto semantico esatto su 1.419 calciatori delle due stagioni campione e controllo di cardinalità integrale sui 21 anni. **Stato: staging verificato localmente, non ancora pubblicato; verifica Windows da effettuare.**
