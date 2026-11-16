# Verifica metriche RecordsNext 4.0 v47

## Gate 3 - dati e calcoli

- **TF05 e TF06**: gia' attivi nella v46, regressione invariata; ogni media ha numeratore e N corretti, distinzione apportato fantasy per squadra/competizione.
- **TF14 e TF15**: effettivamente implementati. Per ogni identita' storica approvata, confrontare la media dei voti veri validi della stagione con la somma dei voti sulle altre stagioni divisa per il loro N totale. Non includere la stagione stessa nella baseline. TF14 = delta positivo, TF15 = delta negativo; con cutoff del sito, selezione GUI e soglie topflop.json. Risultati e pareggi in `facade.storico.recordStorici`, dettaglio `History[].anomalieStagionali`.
- **Costo/cessione**: origine FCM `tesserato`, **per contratto ID**. La pubblicazione `Economy` conserva valore, null vs zero, outlier contro `lega.CREDITITOTALI`; profili condensati `Careers/Teams` conservano min/max/media solo di acquisti positivi; il decoder v1 continua a leggere il vettore compatto storico a 4 elementi.
- **Fantamedia reale**: richiede file `config/topflop-real-rules.json` con regole verificate per stagione; solo allora calcolare da `FCA RealDay` con voto valido, una volta per giornata, applicando coeff ai 8 eventi indipendenti. Rigori realizzati sono sottoinsieme dei gol; rigori subiti sono sottoinsieme dei gol subiti; `SV/0/nullo` non diventa fantavoto. La media storica aggregata esiste solo con copertura completa dei voti reali, altrimenti null e conteggio coperto.
- **Indice economico**: non attivo (campo `indiceEconomico=null`) finche' non esistono formula approvata e rendicontazione dei budget/costi verificata. Registrata anomalia 2023/24 costo 406 vs CREDITITOTALI 320; non correggere il dato sorgente.
- **TF11–TF13**: non attivi; `panchina` e `roster` annuale non sono prove di esclusione e possesso giorno per giorno.

## Gate 1 - audit JS reali, ANCORA DA ESEGUIRE IN WINDOWS

Una sola esecuzione v47 da GUI, `Completa` e pubblicazione disattivata. Verificare 2006/07-2025/26, record TF14/TF15, costi, link, identità, numeratori, denominatori, compatibilità dei decoder e MAX(byte_per_js)<=1.500.000. Non dichiarare 261 JS come conteggio v47: cambia con le viste `Economy`.

## Gate 2 - pubblicazione, ANCORA DA ESEGUIRE

Su copie isolate di almeno due siti storici/correnti: test FULL e CONSOLIDATED, cutoff, stagioni assenti e rollback. Confronto completo SHA e manifest. Nessun sito operativo.
