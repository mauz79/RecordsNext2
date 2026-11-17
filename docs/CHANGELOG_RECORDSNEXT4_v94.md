# Changelog RecordsNext 4.0 v94 - Consolidamento Git

18 novembre 2026 - Ripresa del solo gate Git bloccato nella v93.

- Corretto il pacchetto di staging incompleto: aggiunta la dipendenza Java `TopFlopFullPerformancesV69.java`, senza alterarne il codice (SHA256 `ed9688e325891b582a68623ccb0abe88a356c0ffb21d0f09a391824868815da4`).
- Allineata anche `TopFlopSimeoneIdentityV73.java` (SHA256 `090b9cb56943658e94ccc0186fb8aaafdcdf0f5486c7d900b823980cb304bb5a`), seconda e ultima classe Java presente nella RC v85 ma non nel Git v85.
- Nessuna modifica a calcoli, GUI, exporter, HTML, JS, database o sandbox: correzione esclusivamente della completezza dei sorgenti versionati.
- Bibbia v51, generatore, diario e registro di stato v94 aggiunti senza sovrascrivere i documenti precedenti.
- Gate di sicurezza per gli esatti 50 file staged v93, per la compilazione Maven prima del commit e per push non-forzato sul branch esistente.

Stato al momento di creazione del pacchetto: **commit e push ancora non eseguiti**.
