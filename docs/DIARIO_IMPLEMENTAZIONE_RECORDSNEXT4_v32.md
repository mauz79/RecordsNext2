# RecordsNext 4.0 - Diario implementazione v32 (identita distinte, 2026-11-11)

## Vincoli invariati
- Nessuna fusione di calciatori senza approvazione esplicita in GUI.
- FCA, FCM, SQLite, motore squadre/competizioni RecordsNext 3.2 e JS legacy immutati.
- Lavoro sulla copia isolata TEST_v37 gia aggiornata a v39.
- `config/player-identities.json` NON e modificato dall'installazione.
- Nessun sito pubblicato dall'installatore.

## Bug v39 risolto
La decisione `PERSONE DISTINTE` per un codice Gazzetta riutilizzato era persistente,
ma non imponeva un vincolo in fase di collegamenti successivi. Inoltre non era chiaro
come approvare una carriera interna al codice, ad esempio:
- `2006_2007:224`: ASCOLI Nicola, data 1979-11-09
- `2007_2008:364`: ASCOLI Nicola, data 1979-09-11 (giorno e mese invertiti)
- `2015_2016:175`: CRIVELLO Roberto, data 1991-09-14

La v40 conserva la separazione, offre il pulsante `Approva gruppi interni` dopo
la scelta `PERSONE DISTINTE`, con anteprima e conferma esplicita. I due Ascoli
formano un gruppo proposto, senza includere Crivello. Prima del click nessuna
nuova identita viene approvata.

`Registry.bindAudit` ricostruisce in sola memoria i vincoli delle separazioni
gia decise a partire dal fingerprint persistente e dai dati FCA correnti.
`Registry.approve` rifiuta e annulla fusioni che attraversano gruppi diversi
in un codice dichiarato distinto. Lo stesso controllo e richiamato dal publisher
prima di produrre le viste storiche per Renewo. Nessun cambio di schema del JSON.

Revocando la separazione non si cancellano i gruppi interni approvati; per
revocarli occorre selezionare e revocare la proposta corrispondente.

## Verifiche eseguite
- Compilazione completa Java 21 di tutti i sorgenti `src/main/java` ricostruiti
  da progetto v28 + patch v37 + patch v39 + v40.
- `TopFlopSeparatedIdentitySmokeTest`: OK.
- `TopFlopHistoricIdentitySmokeTest`: OK.
- `TopFlopIdentityEvidenceSmokeTest`: OK, CSV: 569/1531/2133.
- Audit integrale su 16.262 anagrafiche: 3165 proposte, 71 codici ambigui.
- Caso 12302: un solo sottogruppo Ascoli, due annate, Crivello escluso.

## Ambito e collaudo rimanente
Non e stato possibile provare fisicamente il click Swing su Windows ne la build
Maven con driver SQLite nell'ambiente Windows dell'utente: li verifica
l'installatore nella copia di test. Non installare sulla 4.0 principale.
