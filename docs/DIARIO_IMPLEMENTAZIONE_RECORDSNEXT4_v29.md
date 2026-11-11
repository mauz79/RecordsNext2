# RecordsNext 4.0 - diario implementazione v29 integrata (10 novembre 2026)

Base: sorgenti della GUI v28 estratti dal progetto originale del 6 novembre 2026.
Nessun codice delle famiglie 3.2 (Classics, Series, RU, Modifiers, ThresholdsLuck)
e nessun mapping storico di squadre/competizioni sono stati modificati.

## Componenti Java realmente modificati

- RecordsNext2Dashboard: rimossi seconda modalita' Completa/Consolidata e pulsante Top & Flop duplicati; aggiunto comando di riconciliazione delle identita'; alert non bloccante al primo ingresso in Top & Flop.
- TopFlopPlayerIdentities (nuovo): lettura delle anagrafiche FCA, proposte di associazione, identificazione dei codici storici ambigui, scelte esplicite salvate in config/player-identities.json. Le proposte non sono applicate automaticamente. Distinguere, unire manualmente, rinviare e revocare sono disponibili.
- TopFlopIdentityReviewDialog (nuovo): configurazione tramite GUI dopo importazione FCA; approvazione massiva SOLO dei casi esatti, verifica manuale degli altri.
- TopFlopPublicViewsStageExporter: usa il tabellino FCM per le prestazioni non conteggiate ma osservate (panchina/fuori lista); calcola media voto reale e fantamedia tabellino, eventi bonus/malus; espone i costi Tesserato.COSTOACQ (min, max, media, campione) per calciatore e fantasquadra.
- TopFlopHistoricalViews (nuovo): carriere storiche dai soli playerKey con identityId approvato; medie ponderate su voti e contatori, non sulle medie; nessuna duplicazione dei voti per fantasquadre.
- TopFlopViewsPublication: nel normale attachScoped 3.2 genera nuovi shard History aggregati solo sulle annate disponibili per ciascun sito; size limit 1.500.000 byte reale; hash e metadata nella facade.
- RecordsNextPipeline: carica le decisioni dal project root ed esegue il nuovo calcolo nella pipeline gia' consolidata.
- fcmRecordsNextTopFlopViewsDecoder-v1.js: supporta senza regressioni sia i vecchi array compatti (6/4) che le nuove estensioni (7/5); espone loadHistory().

## Contratto JS aggiuntivo

- `Career` conserva tutti i primi 6 elementi; 7o elemento `[numeroRegistrazioniCosto,min,max,media]`.
- `Team` conserva tutti i primi 4 elementi; 5o elemento con lo stesso vettore costi.
- Vettore `fantasy` conserva i 9 elementi originali; 10o elemento `[giornateFuoriConVoto,sommaVotiFuori,gareConFantavotoTabellino,sommaFantavotiFuori,eventiNonApportati]`.
- `History` contiene record storici `[identita, stagioni, voti, media reale, eventi]`, formato oggetto, in `fcmRecordsNext_TopFlopViews.History.Storico.XXX.js`.
- La facade, senza cambiare schemaVersion, espone `storico.file` e `storico.identita` del SOLO cutoff destinazione.

## Verifiche eseguite nell'ambiente di sviluppo

- Java 21: compilazione completa di tutti i sorgenti main (nessun errore).
- TopFlopPublicViewsStageSmokeTest: superato, inclusi portieri, rigori, contesti distinti e no >1.5MB.
- TopFlopHistoricIdentitySmokeTest: superato; nessun join senza approvazione; media storica ponderata; revoca e persistenza.
- TopFlopHistoricalStagingSmokeTest: letti 21 anni di 85 JS reali, 16.262 carriere, 8 stagioni di Higuaín; aggregazione reale 217 voti validi (sorgente), non somma contesti fantasy.
- TopFlopScopedPublicationSmokeTest: superato; sito con cutoff 2015/16-2016/17 non riceve 2017/18; History generato nella stessa staging 3.2.
- Test JS Node: decoder vecchio/nuovo Career e Team, costi e dentro/fuori.

## Resta da collaudare / implementare

- Build Maven completa e GUI Windows sul progetto effettivo. Ambiente Linux privo di dipendenze sqlite-jdbc e Maven scaricabili: non dichiarare eseguito un test SQLite Java completo.
- Calcolo della fantamedia reale indipendente dai contesti fantasy: il solo FCA non contiene un punteggio finale pronto; non inventare coefficienti, completare con le regole di scoring verificate.
- Fuori formazione non documentati in FCM.Formazione: non dedurre la rosa giorno per giorno dai soli tesseramenti a snapshot. I dati 'non utilizzati' coprono soltanto i casi osservati (panchina e fuori lista).
- Valore tecnico economico IVS da calibrare e configurare. Il costo di acquisto e' stato aggiunto; nessuna stima di valore economico artificiale.
- Fotografie storiche: percorsi fisici non ancora verificati, da completare.
- Classifiche storiche per rendimento delle fantasquadre canoniche: conservare i mapping 3.2, da sviluppare separatamente.

Non sono state modificate le basi FCA/FCM originali, il database SQLite e i siti pubblicati.
