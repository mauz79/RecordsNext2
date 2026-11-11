# RecordsNext 4.0 — Specifica VISTE PUBBLICHE Top & Flop — v1

2026-11-06 — Stato: exporter Java e decoder **verificati in STAGING**; nessuna integrazione della 3.2 e nessuna pubblicazione autorizzata.

## Architettura

- L'archivio FCM/FCA completo rimane nel database SQLite locale. La nuova famiglia contiene ESCLUSIVAMENTE viste calcolate, non copie dei dati-giornata o dei tabellini per tutti i calciatori.
- Nel sito una facade leggera, un archivio di schede aggregate stagionali (**Careers**), aggregati per calciatore-fantasquadra-competizione (**Teams**), record con tutti i pari merito (**Records**) ed episodi selezionati (**Highlights**).
- Righe Careers e Teams in codifica posizionale con dizionari di nomi squadre, competizioni ed eventi, decoder JS reversibile `fcmRecordsNextTopFlopViewsDecoder-v1.js`. Nessuna nuova cartella sotto `js` del sito.
- Il dettaglio selezionato contiene stagioni, giornata Serie A, giornata della competizione se canonica, fantasquadra, schieramento, conteggiato, voto, fantavoto, bonus/malus ed eventuale URL dei report canonici 3.2. Le prestazioni REALI non fingono di avere un unico tabellino fantasy; possono mostrare zero o piu contesti FCM documentati.
- Nessuna fusione automatica di CodiceGazza tra anni: `playerKey` = stagione + ID locale FCA, codice storico disponibile come candidato. Ruolo 1 per portieri verificato nelle 21 stagioni del database campione.
- Gol su rigore sottoinsieme dei gol; gol subiti su rigore sottoinsieme dei gol subiti. Portieri ammessi anche fra marcatori e assistman.
- Classifica media voto TF05 e fantamedia TF06: aggregati completi e denominatori, **nessuna classifica definitiva** fino all'approvazione delle soglie minime. TF11–TF13 disabilitati finche non si documenta il possesso per giorno e il fantavoto di esclusi. TF14–TF15 disabilitati finche non si autorizzano baseline, N minimo e riconciliazione identitaria cross-season. TF16 confronti disponibili senza metrica unica arbitraria.
- TF01–TF04, TF07–TF10 e TF17–TF20 pubblicano record sostenuti dalle fonti; TF09 per evento negativo distinto. Pareggi di merito tutti conservati, senza top N arbitrario.
- Youth League esclusa anche dagli aggregati. Play Off–Play Out e Spareggio Serie B considerate CAMPIONATO. 100 contesti eccezionali 2022/23 restano con giornata competizione NULL se non canonica, conservati nelle somme.

## Esito misurato su 21 stagioni

Database test aggiornato al 2026/27, ultima prova con i contesti/link selezionati:

| Metadato | Risultato |
|---|---:|
| Stagioni | 21 |
| Prestazioni reali presenti nello SQLite di origine | 617.956 |
| Contesti fantasy ammessi nello SQLite | 554.425 |
| Contesti con apporto COUNTED | 238.921 |
| Schede statistiche stagionali | 16.262 |
| Riepiloghi fantasquadra/competizione | 45.787 |
| Gruppi di record | 540 |
| Episodi selezionati | 1.081 |
| File JS | 85 |
| **Byte JS complessivi** | **5.355.536** |
| **Singolo JS massimo** | **208.689 byte** |

Dimensione accumulata in base alle stagioni del sito (senza ancora pubblicare dal publisher 3.2): 2006/07 0,12 MB; 2010/11 0,66 MB; 2018/19 2,51 MB; 2025/26 5,08 MB; 2026/27 5,32 MB (solo shard stagionali, da aggiungere la facade con cutoff).

Il numero 5,36 MB è **per lo storico di 21 anni**, NON per stagione. I dati sono raggruppati per anno; al caricamento della pagina si possono leggere solo index/Records; Careers e Teams si caricano su richiesta.

## Test e limiti

- Compilazione Java 21 contro i sorgenti 3.2+TopFlop; smoke test sintetico per portiere goleador che para, segna, serve assist, subisce gol e compare in due fantasquadre. Prove negative: una vista oltre 1.500.000 byte viene bloccata e una riserva non entrata non viene mai etichettata come conteggiata.
- Parser Python indipendente di tutti i 85 JS, ricostruzione vettori/eventi e verifica dei conteggi origine SQLite, SHA-256, dimensioni e inclusione degli highlight citati.
- Node browser decoder su tutte le 21 stagioni, nessuna Youth League, nessun record TF11–TF15 attivato erroneamente.
- URL tabellino solo da `season_normalized_*.json` della 3.2, NULL se mancante; nessun URI ricostruito con ID tecnici.
- **FASE SUCCESSIVA NON REALIZZATA:** estensione controllata `RecordFamily`/catalogo/pipeline/manifest/publisher/GUI e HTML ReNewo; verifica multisito con cutoff e rollback. La facade dello staging contiene tutte le 21 stagioni e NON deve essere copiata nei siti storici senza trasformazione e validazione specifica del publisher.
- Il runner lascia intatti SQLite, JS della v1/v2, HTML, CSS, GUI, 3.2 e siti esistenti.
