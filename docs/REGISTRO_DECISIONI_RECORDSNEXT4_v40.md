# RecordsNext4 - Registro decisioni v40

## DEC-v67-001 - Migrazione invece di ricominciare
La RC v64 e' una copia dell'installazione 4.0 gia' validata: preservare SQLite, identita, record e configurazione recuperata dalla 3.2 (21 stagioni). Non riconfigurare a mano i 21 percorsi.

## DEC-v67-002 - FCA 2012/2013 immutabile
Preservare il file ArchivioA2012SerieAmauz_G1_RECUPERATA_v1.fca con SHA-256 e 9936 voti/255 G1. Bloccare migrazioni/pubblicazioni che tornino al vecchio FCA 3.2.

## DEC-v67-003 - Interfaccia utente
Apertura card al primo clic, metadati stagione da SQLite anziche' rilettura UCanAccess, nessuna etichetta di sviluppo, card senza clipping, stesso insieme di sezioni nella dashboard e nel menu laterale.

## DEC-v67-004 - Flusso integrato
L'operatore deve poter configurare, elaborare e pubblicare dalla GUI. Script esterni solo per migrazione iniziale, diagnostica e salvaguardia; niente rielaborazione o pubblicazione tramite comandi non richiesti.

## DEC-v67-005 - Rilascio reale NON certificato
La sandbox certifica dati/shard, non browser e sito reale. Prima backup, test browser, conferma percorsi, flag produzione e rollback; nessun commit GitHub equivale al deploy.

## DEC-v67-006 - GitHub incrementale
Partire da commit 3b008a8 branch feature/recordsnext4-v63-sandbox-validated. Conservare v46 congelato e v63; integrare solo GUI v65/v66, migrazione v64/v64b e documentazione aggiornata nel nuovo branch v67. Non committare DB, config privati, file FCA/FCM o JS staging.
