# RecordsNext 4.0 — Diario v38 / correzione staging v46

Data: 11 novembre 2026. Base operativa: v45b installata nella copia `D:\DEV_APPS\RecordsNext4.0_TEST_v37`.

## Difetto riprodotto

Durante **Completa / Elabora senza pubblicare**, l'importazione e la normalizzazione di tutte le stagioni, i Classici e le 81 viste Top & Flop sono terminati. La generazione JS si e' poi fermata con:

`java.io.IOException: JS oltre 1.500.000 byte: fcmRecordsNext_Classics.js=8398766`

`Classics` e' un output 3.2 preesistente, non un output Top & Flop. Il ramo `RecordsNextPipeline` con `publish=false` validava l'output cumulativo prima di applicare il sistema 3.2 di shard flat. Il ramo `publish=true` gia' chiamava `SeasonFamilyShardPublisher.prepareFlat`.

## Correzione integrata (non una nuova pipeline)

- `RecordsNextPipeline.run`, ramo senza pubblicazione: dopo l'esportazione 3.2 invoca `SeasonFamilyShardPublisher.prepareFlat` sullo staging generato, prima del controllo sul peso.
- Aggiunta la facade Top & Flop dopo lo split dei Classici: nessuna duplicazione dei dati gia' presenti e nessun cambiamento ai contratti legacy.
- `ManifestPublishingSupport.write` rigenera il manifest con i nomi effettivi degli shard e delle facade anche nell'anteprima.
- `TopFlopViewsPublication.validateStaging` controlla **tutti** i file JS fisici presenti, anche senza Top & Flop.
- Il risultato `Result.files` descrive il numero di file realmente validati dopo lo split, non la somma inaccurata degli originali e dei soli Top & Flop.
- Il ramo multisito utilizza lo stesso limite di 1.500.000 byte e convalida gli output dopo lo sharding. Il cutoff, il rollback, le associazioni di squadre 3.2 e le regole di identita' 4.0 restano invariati.
- Il valore predefinito degli shard flat e' ora **1.500.000 byte esatti**. Il precedente margine tecnico 3.2 da 1536 KiB non e' equivalente.

## Test eseguiti localmente

- Compilazione Java 21 dei sorgenti 4.0 integrati, senza errori.
- `RecordsNextFlatStagingV46SmokeTest`: Classics cumulativo **9.012.856 byte** diviso in 20 shard e facade leggera; massimo shard **452.641 byte**, 21 file JS convalidati.
- I 250 record per ciascuna delle 20 stagioni sono conservati negli shard; la facade elenca i file stagionali, senza sottocartelle.
- Rifiutato sia un singolo JS non shardabile oltre limite, sia uno shard stagionale che oltrepassi 1.500.000 byte.
- Smoke test di identita' e fantasy 4.0 rieseguiti; il registro dell'utente non e' stato modificato.

Il test di 9 MB usa **dati sintetici nel formato Classici**, poiche' il cumulativo effettivamente generato da Windows non e' disponibile in questo ambiente. L'esecuzione end-to-end sulla copia Windows dell'utente resta un collaudo obbligatorio, non ancora certificato.

## Sicurezza e operativita'

- Patch valida solo per la copia TEST e solo se gli hash SHA-256 coincidono con la v45b realmente installata.
- Backup versionato di sorgenti, documentazione e JAR prima delle modifiche; rollback automatico se la compilazione/test falliscono.
- Non modifica `config/player-identities.json`, DB SQLite, file FCA/FCM, cartelle di pubblicazione o RecordsNext 3.2.
- Il collaudo si esegue sempre con `publish=false`. Avvio Java 21 con dipendenze UCanAccess 2.0.9.5 esterne in sola lettura.
- Nessuna pubblicazione GitHub e' attestata finche' non esiste un commit/push confermato dal repository remoto.

## Ancora da completare per Renewo

351 proposte di identita' sospese; fantamedia reale indipendente, fotografie storiche e indice tecnico/economico configurabile, oltre alla verifica di export completo e dei cutoff su siti di prova. La patch **non** dichiara RecordsNext 4.0 concluso.
