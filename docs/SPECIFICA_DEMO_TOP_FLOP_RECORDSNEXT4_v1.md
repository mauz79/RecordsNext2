# RecordsNext 4.0 — Demo Top & Flop v1

Data: 2026-11-02. **Anteprima di integrazione, non rilascio 4.0.0 e non pubblicazione.**

## Architettura 3.2 rispettata

- Un visualizzatore HTML in `release/visualizzatori/RecordsNext/top_flop_demo.html`; CSS e JS separati.
- Selettore dei quattro CSS **originali 3.2.0**: neutral (profilo ispirato a ReNewo), mauzstrom, maelstrom, fantablue2.
- `fcmRecordsNext_TopFlop.js` facade e `fcmRecordsNext_TopFlop.YYYY_YYYY.js` shard nella **stessa directory `js`**. Nessuna sottocartella di shard, nessuna richiesta verso un altro sito per caricare dati.
- Lo stato è **preview**: la facade è un prototipo della convenzione di `SeasonFamilyShardPublisher`, non un'aggiunta a `FAMILIES`. Non sono alterati `RecordFamily`, GUI, preflight, pipeline, publisher e manifest 3.2.
- Ogni shard è verificato entro 1536 KiB. Se una stagione più ricca supera la soglia, il generatore **si ferma**, non tronca i dati. Occorre una strategia di shard aggiuntivi integrata nativamente in 3.2 prima della pubblicazione.
- HTML senza dati incorporati; usa esclusivamente JS generati. Mancanza dati gestita senza creare valori fittizi.

## Fonti e contenuto della demo

Il generatore diagnostico legge in sola lettura lo SQLite importato dal motore 3.2. Due stagioni campione: 2006_2007 e 2026_2027 (archivio aggiornato dell'utente). Sono presenti 6050 + 7650 righe `Formazione` ammesse alla demo. Non si pretende copertura integrale dello storico in questa fase. Le altre stagioni saranno esportate dalla pipeline nativa prima del rilascio.

Campi nel vettore `rows`, nell'ordine dichiarato da `columns` nella facade:

`p,t,c,m,r,fr,pos,en,es,slot,v,fv,gv,go,gr,as,am,ex`

`p`: ID FCA della stagione; `t`: ID fantasquadra FCM; `c`: ID competizione FCM; `m`: ID incontro; `r`: giornata reale; `fr`: ID giornata FCM (non numero progressivo); `pos`: posizione FCM; `en`, `es`: flag FCM; `slot`: posizione tabellino; `v` e `fv`: voto e totale **del tabellino FCM** (non voti inventati); `gv`: voto FCA Gazzetta; `go`, `gr`, `as`, `am`, `ex`: eventi FCA (gol, gol su rigore, assist, ammonizioni, espulsioni). I riferimenti a giocatore, squadra e competizione sono normalizzati in dizionari per risparmiare byte.

Le prestazioni reali FCA si sommano una volta per giornata reale; nella demo gli eventi sono ripetuti nel contesto fantasy per visualizzare il dettaglio senza dichiarare somma all-time. La struttura Java canonica `TopFlopArchive` conserva invece la prestazione una sola volta. Con `pos==0` identifichiamo **titolare iniziale**, NON dimostriamo che il voto sia stato conteggiato; con `pos>0` panchina e con `pos<0` fuori lista. Non si classificano ancora esclusioni certe; non si calcolano i quattro indicatori di errore pesato. Youth League sempre esclusa.

## Link tabellini

I collegamenti 2006_2007 provengono dal **file Matches canonico della 3.2** per matchId + teamId, e l'URL online viene risolto attraverso `rn_season_configuration.online_site_url` della **stessa stagione**. Non si crea mai un URL dall'ID partita o dalla sola giornata. Nel 2026_2027 non sono disponibili link canonici nella fonte Matches fornita, quindi il visualizzatore mostra `Non disponibile`.

I link così risolti sono riferimenti configurati, non test della raggiungibilità del server remoto.

## Cosa manca per il rilascio

1. Fonte temporale di appartenenza: ingresso/uscita giornaliera, unione degli intervalli senza giornata di assenza; Varela/FortisSIMY come regressione.
2. Aggregazioni e quattro metriche individuali in Java per team/competizione/famiglia/stagione e all-time, con eventi originali e denominatori corretti.
3. Motore nativo records con ranking TOP e FLOP, statistiche titolari/panchina/esclusioni, ID record stabili.
4. Estensione **coordinata** di catalogo, selezione GUI, pipeline FULL/CONSOLIDATED, exporter, manifest, validazione file del publisher, shard e rollback, cutoff storico multisito.
5. Copertura di tutte le stagioni; no falsi primati sui soli campioni. Link delle stagioni da fonte normalizzata; demo finale con test skin e Renewo vero.
6. Test Java e browser su Windows dell'utente, compresa pubblicazione su **copia di sito** prima di toccare quello reale.

## Come usare la demo

Dopo installazione aprire `D:\DEV_APPS\RecordsNext4.0\release\visualizzatori\RecordsNext\top_flop_demo.html` in un browser. La pagina permette selezione skin, filtri, ordinamento, dettaglio e link se disponibili. Non è collegata al menu `recordsnext.html` per non presentare il modulo incompleto come attivo.

## Test disponibili

`node tools/Test-RecordsNext4TopFlopFacade-v1.js` verifica il caricamento facade+shard, le due stagioni, le 13.700 righe e la risoluzione URL del 2006. Il runner installa soltanto asset identici oppure nuovi, senza sostituzioni non consentite.
