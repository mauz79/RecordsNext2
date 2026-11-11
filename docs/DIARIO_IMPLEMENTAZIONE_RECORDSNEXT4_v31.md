# RecordsNext 4.0 - Diario v31 / evidenze LCS storiche e installazione sicura

Data: 2026-11-11. Destinazione: esclusivamente RecordsNext4.0_TEST_v37.

## Intervento

- Correzione errore v38: diario e bibbia v30 gia esistenti nella v37 NON devono essere sovrascritti.
- Evidenze di origine archiviate separatamente: 2015 (401), 2019 (561), 2020 (569), piu file attuale (569) e controllo (2133).
- Il CSV 2020 risulta IDENTICO byte per byte al CSV attuale; anche la copia conservata mantiene la provenienza.
- Le versioni vecchie sono informazioni di confronto; non diventano automaticamente decisioni.
- In caso di destinazioni storiche discordanti la GUI mostra `FONTI LCS STORICHE DISCORDANTI` e richiede verifica individuale.
- Non cambiare la base FCA/FCM, gli ID originali, le decisioni persistenti, il motore squadre 3.2 o i JS pubblicati.
- La fonte corrente formula proposte, ma tutte le decisioni restano manuali e reversibili.

## Esempi di divergenza esaminati

- Maggio: cambiamenti sia nel nome sia nel giorno/mese di nascita tra 2015 e 2020.
- Okaka: normalizzazione differente del nome completo e data incerta in una versione vecchia.
- Felipe Dal Belo: due forme canoniche del nome tra 2015 e 2020.
- Caceres: differenze di apostrofo/accento tra 2019 e 2020, equivalenti dopo normalizzazione: non sono automaticamente una collisione di persone.

## Limiti

Il confronto LCS e' solo documentale: non autorizza fusioni. Se il target richiede un record di possesso o l'accorpamento di codici, decide sempre l'utente nella GUI.

## QA

- Test Java delle 21 stagioni e smoke test di riconciliazione da eseguire su copia di test.
- Installazione protetta con manifest SHA256, backup e rollback dei file patch.
- Nessun comando di elaborazione o pubblicazione viene avviato dall'installatore.
