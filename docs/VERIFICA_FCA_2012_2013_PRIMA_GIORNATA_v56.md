# RecordsNext 4.0 TEST — Recupero FCA 2012/2013 (verifica post-v55)

Data verifica: 2026-11-15 21:39:34.

## Fatto accertato

- Fonte configurata per `2012_2013`: `E:\FCM\data\ArchivioA2012SerieAmauz_G1_RECUPERATA_v1.fca`. SHA-256 `e3c34b941aedb35951ba030701fe47072afa00941f7fa7884d0658442cfcf766`.
- File originale `E:\FCM\data\ArchivioA2012SerieA.fca` mantenuto separato; nessuna modifica ai suoi contenuti durante questi controlli.
- Prima giornata recuperata: **255 voti validi**. Totale importato: **9.936**; 38 giornate.
- Collaudo UCanAccess gia eseguito: 15 tabelle, 170 colonne, 36.718 righe, 11 nuovi punteggi; giornate 2-38 e altre tabelle identiche al Mauz originale.
- Ultima importazione FCA TEST: `rn_import.import_id=59`, `COMPLETED`.
- Elaborazione Completa eseguita dall'utente: 87/87 record completi, 317 JS validi, 0 pubblicati (log del 15 novembre 2026); il controllo attuale vede 317 file JS RecordsNext nello staging generato.
- Il primo audit post-FCA usava la sandbox v52 preesistente ed ha riscontrato 9.681 voti JS (349 discrepanze); il presente audit usa lo staging prodotto dalla elaborazione Completa.
- Audit post-rigenerazione: `Careers` 9936 voti JS = 9936 raw, 0 differenze; audit complessivo Careers senza errori.

## Riepiloghi FCA ancora da trattare

- `Statistiche.PRESENZE` vs voti raw: **0** discrepanze (prima 235).
- `Storico.PRESENZE` vs voti raw: **235** discrepanze (prima 0). Le righe `Storico` non sono state modificate.
- `Statistiche.FMT` e `Storico.FANTAMEDIA1` differiscono oltre 0,011 per **180** giocatori. NON dichiarate certificate; non riscrivere automaticamente.
- I calcoli Java, le graduatorie TF01-TF20, FCM, `Teams`, `Records`, `History`, il cutoff e l'intera semantica classica non sono certificati da questo audit.

## Percorsi ed esito

- SQLite TEST: `D:\DEV_APPS\RecordsNext4.0_TEST_v37\data\database\recordsnext.db` (aperto soltanto in sola lettura).
- Rapporto post-v55: `D:\DEV_APPS\RecordsNext4.0_TEST_v37\reports\audit-fonti-v56-post-fca2012-staging-v2`.
- JS staging TEST: `D:\DEV_APPS\RecordsNext4.0_TEST_v37\data\site-export-staging\records2026_20261115_212627_fea7e0b9\js`.
- Pubblicazione disabilitata (`processing.publish=false`, `publishToSite=false`). Nessuna scrittura ai siti.
- Nessuna variazione a identita approvate, FCM, FCA originali, sorgenti Java, SQLite o JS durante audit+documentazione.
- I report v55 precedenti descrivono lo stato **prima del recupero**; conservarli e non sovrascriverli.

## Decisione successiva

Verificare semanticamente `FMT` vs `FANTAMEDIA1` 2012/13 prima di modificare i calcoli Java e di pubblicare.
