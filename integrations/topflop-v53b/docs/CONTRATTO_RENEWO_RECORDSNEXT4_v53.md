# RecordsNext 4.0 — contratto per i nuovi HTML Renewo (v53)

## Separazione obbligatoria

- **RecordsNext / FCM**: il plugin Java legge FCM e FCA, elabora i dati in SQLite, pubblica i JS; gli HTML RecordsNext già esistenti consumano quei JS e utilizzano i quattro profili CSS originali (`mauzstrom`, `fantablue2`, `maelstrom`, `neutral`). La pagina integrata è `RecordsNext/top_flop.html`; il suo codice non calcola classifiche che non siano nell'export.
- **Renewo**: non riceve HTML o CSS della skin RecordsNext. Riceve solo JS, schema, decodifica, condizioni di validità e questo contratto; può generare autonomamente nuovi HTML.
- **FCM 3.2**: famiglie Classici, Serie, RU, Modificatori, Soglie/Fortuna, Culometro, Lega restano indipendenti e utilizzabili. Il frontend Top & Flop si aggiunge, non le sostituisce.

## JS canonici

`js/fcmRecordsNext_TopFlopViews.js` espone `window.fcmRecordsNextTopFlopViews` (`schemaVersion: "4.0-public-views-v1"`):
- `stagioni[]`: stagioni effettivamente disponibili **nel cutoff storico del singolo sito**; per ognuna: `stagione`, `contatori`, `dizionari`, `files[]` (`nome`, `tipo`, `righe`, `byte`, `sha256`).
- `eventi[]`, `attivazioneRecord`, `soglie`, `storico` (se sono disponibili approvazioni), `coperturaTF11_TF13`.
- Gli shard `fcmRecordsNext_TopFlopViews.<Tipo>.<AAAA_AAAA>.<NNN>.js` sono caricati **solo quando richiesti**. Tipi: `Records`, `Highlights`, `Careers`, `Teams`, `Bench`, `Economy`, `RealFantasy`; `History.Storico` contiene carriere unificate approvate.
- Alcuni dettagli FCM possono essere in `fcmRecordsNext_TopFlop.js` e shard Compact separati; non includerli automaticamente nelle pagine di classifica.

## Decoder web

`js/fcmRecordsNextTopFlopViewsDecoder-v2.js` espone `window.RecordsNextTopFlopViewsV2`:

```js
const D = window.RecordsNextTopFlopViewsV2;
const facade = await D.loadFacade('js');
const current = facade.stagioni.at(-1).stagione;
const records = await D.loadView('js', current, 'Records');
const highlights = await D.loadView('js', current, 'Highlights');
const careers = await D.loadView('js', current, 'Careers');
const contexts = await D.loadView('js', current, 'Teams');
const verifiedHistory = await D.loadHistory('js');
const historicalWinners = await D.loadHistoricalRecords('js');
```

I percorsi sono relativi alla pagina che li richiama. Il decoder applica la verifica sul numero di righe dichiarate. Il produttore Java impone 1.500.000 byte massimi **a ogni singolo JS**; un file sopra tale soglia è un errore di pubblicazione.

## Contenuti visualizzabili

| Vista | Possibile visualizzazione | Limite / interpretazione |
| --- | --- | --- |
| `Records` | Vincitori TF01–TF20 effettivamente calcolati, metriche, pari merito | Non inventare vincitori per record disattivati o assenti. |
| `Highlights` | Prestazioni, stagione, squadra/competizione quando documentate, URL tabellino FCM | URL solo se presente nello shard; mai costruirlo da un numero di giornata. |
| `Careers` | Voti FCA stagionali validi, media voto reale, eventi e costi osservati | Le statistiche fantasy non sono automaticamente presenze reali uniche. |
| `Teams` | Apporto separato per calciatore/fantasquadra/competizione/stagione | Non sommare presenze e fantavoti delle competizioni in un totale fittizio di carriera. |
| `History` | Stagioni di persone unite manualmente, voti reali e media ponderata, TF14/TF15 | Solo identità **esplicitamente approvate** nel registro; nessuna fusione automatica da nome o codice Gazzetta. |
| `Bench` | TF11–TF13 per panchine FCM documentate non conteggiate | **Non** equivale a tutte le tribune o a tutti gli errori di formazione: possesso alla data non sempre dimostrabile. |
| `Economy` | Costi di tesseramento, valori min/max/medi registrati, note sulle anomalie | I budget stagionali non sono automaticamente comparabili; non calcolare un indice economico fittizio. |
| `RealFantasy` | Fantamedia reale indipendente **solo dove il regolamento stagionale è verificato** | Se copertura mancante/incompleta, mostrare “Non disponibile”, non zero o valori FCM TOT. |

## Comportamento UI richiesto

1. Offrire filtri per stagione, codice/nome giocatore, record TF e contesto quando il dato lo consente.
2. Caricare di default soltanto le classifiche della stagione corrente del sito; scaricare `Careers`, `Teams`, `History` e dettagli **su richiesta**.
3. Mostrare sempre la copertura effettiva e le soglie dichiarate. Distinguere `0` da campo nullo/non disponibile.
4. Non confondere i risultati delle partite con prestazioni reali FCA; non ricostruire punteggi alternativi.
5. Rispettare cutoff e storico manuale 2005/06; la prima stagione priva di FCM non può generare dati FCM.
6. Preservare `scorecardUrl`/`urlTabellino` solo quando provengono dalla fonte, con link esterni sicuri.
7. Non pubblicare i JS derivati di sviluppo in assenza del controllo completo e del cutoff per sito.

## Stato verificato

Pipeline multipubblicazione SANDBOX v52: 21 stagioni 2006/07–2026/27, 3.492 JS sotto 1,5 MB, destinazioni isolate. Questo prova la produzione e distribuzione, **non** ogni metrica numerica né il frontend Renewo. Frontend RecordsNext v53: integrazione nella skin esistente, da provare sulla sandbox Windows prima di una release ufficiale.
