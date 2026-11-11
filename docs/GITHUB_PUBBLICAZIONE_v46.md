# GitHub — aggiornamento RecordsNext 4.0 v46

**Non dichiarare Github aggiornato** finche' `git push` non ha restituito un successo confermato. Il connettore GitHub della sessione e' disabilitato dall'amministratore e l'ambiente di sviluppo non puo' raggiungere `github.com`; nessun commit o push remoto e' stato eseguito qui.

Repository destinatario: `mauz79/RecordsNext2`. Ramo di lavoro previsto: `feature/recordsnext4-players` (non `main`). La 3.2 operativa deve restare intatta.

1. Collaudare v46 nella copia TEST fino alla generazione JS completa, con `publish=false` e file fisici <= 1.500.000 byte.
2. Integrare le modifiche cumulative 4.0 nella **vera** directory Git `D:\DEV_APPS\RecordsNext4.0`. Non copiare il contenuto del TEST in maniera indiscriminata: contiene SQLite e dati personali; seguire il consolidamento del codice e dei documenti soltanto.
3. Eseguire `tools/Publish-RecordsNext4-v46-GitHub.ps1` senza parametri per il preflight. Rifiuta branch non `feature/recordsnext4-players`, remote diverso, staged files preesistenti e file mancanti.
4. Dopo verifica, eseguire lo stesso script con `-Publish`: stage dei soli file esplicitamente elencati, `git diff --cached --check`, commit e push. **Non** aggiunge database, log, siti, dati personali, config identita' o release 3.2.
5. Annotare commit SHA e confermare che compaia nel remoto.

Il pacchetto v46 installabile e' limitato a `RecordsNext4.0_TEST_v37` proprio per non mescolare una versione 4.0 ancora in collaudo con la release storica 3.2 pubblicata.
