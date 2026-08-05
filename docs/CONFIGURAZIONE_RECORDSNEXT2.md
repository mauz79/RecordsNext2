# Configurazione RecordsNext 2.0

## Scopo

Questi file costituiscono la prima configurazione concreta del progetto.

## File

- `config/league.json`: identità generale della lega.
- `config/seasons.json`: stagioni gestite, manuali e correnti.
- `config/teams.json`: squadre canoniche e squadre stagionali.
- `config/competitions.json`: competizioni canoniche e stagionali.
- `config/processing.json`: famiglie e figli richiesti.
- `config/culometro.json`: configurazione separata dell'easter egg.
- `config/manifest.example.json`: forma preliminare del manifest prodotto.

## Regole consolidate

- Il file FCM e il file FCA sono configurati per stagione.
- La cartella `js` non viene configurata separatamente: è interna alla root del sito.
- Ogni stagione può avere sito locale e online.
- Il nome della pagina tabellino è configurato per stagione.
- Squadre e competizioni conservano identità stagionale e canonica.
- Le famiglie possono essere elaborate separatamente.
- Ogni figlio può dipendere da altri dati o moduli.
- Il Capitano può essere disattivato senza bloccare le altre Serie.
- Il Culometro è disattivato per impostazione predefinita.
- Il Culometro richiede configurazione esplicita.
- Le competizioni canoniche rispettano l'ordine stabilito.
- Play Off e Play Out non appartengono all'ordine principale.

## Nota importante

I valori presenti sono iniziali o di esempio. Prima dell'elaborazione reale sarà necessario importare e verificare:

- tutte le stagioni;
- i percorsi FCM/FCA;
- le root locali e online;
- il formato del tabellino di ogni stagione;
- i mapping delle squadre;
- i mapping delle competizioni.
