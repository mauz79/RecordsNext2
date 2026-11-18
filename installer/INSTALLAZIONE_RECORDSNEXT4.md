# RecordsNext by mauz79 — versione 4.0

## Nuova installazione
Selezionare la cartella dal wizard e avviare `RecordsNext.bat` dal menu Start.
E' richiesto Java 21 o superiore. Il setup contiene le dipendenze UCanAccess.
Al primo avvio RecordsNext crea database SQLite e configurazione base: configurare
stagioni, archivi FCM/FCA, sito della lega e associazioni storiche prima di elaborare.

## Aggiornamento dalla versione 4.0
Chiudere la GUI e reinstallare nella STESSA cartella. Il setup sovrascrive solo
programma, asset statici e runtime; NON contiene e NON elimina cartelle `config`
e `data`. Conservare comunque un backup del database prima dell'aggiornamento.

## Installazioni 3.2
NON sovrascrivere direttamente una 3.2 con questo setup. Migrare prima dati,
configurazioni e identita' con una procedura dedicata: gli schemi non sono identici.

## Disinstallazione
La disinstallazione rimuove i file di programma installati dal setup.
Le configurazioni e il database creati dall'utente NON vengono cancellati:
per eliminarli definitivamente occorre rimuoverli manualmente dopo backup.

## Pubblicazione
La GUI pubblica i file JS e le pagine nelle directory dei siti configurati
usando un cutoff storico per ogni sito. L'eventuale upload FTP e' separato.
I JS RecordsNext rispettano il limite di 1.500.000 byte per file.
