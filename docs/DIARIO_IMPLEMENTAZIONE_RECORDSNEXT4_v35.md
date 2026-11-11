# RecordsNext 4.0 — Correzione v43 (bibbia v35)

Data: 11 novembre 2026. Destinazione esclusiva: copia `RecordsNext4.0_TEST_v37`.

## Anomalia riprodotta

Nella v42 la GUI indica `codici da decidere: 0`, ma tentando di approvare una proposta
intercodice (per esempio Okaka `13032` + `73032`) visualizza:
`Risolvere prima il codice ambiguo 13032`.
Il controllo nel pulsante "Approva proposte selezionate" considera risolto il codice
solo se marcato `PERSONE DISTINTE`; ignora `UNIONE APPROVATA`.

## Correzione

- `TopFlopPlayerIdentities.requireResolvedCollisions`: guardia unica, di sola lettura,
  identica alla regola del contatore: `isDistinct(collision) || isMerged(collision)`.
- La GUI invoca la guardia prima dell'approvazione della proposta. Un codice ancora
  ambiguo resta bloccante; un codice gia' approvato non viene esaminato di nuovo.
- Non si cambia la logica della persistente `config/player-identities.json`, ne'
  le scelte gia' registrate; non si modificano FCA/FCM, 3.2 o siti.
- `TopFlopResolvedCollisionGuardV43SmokeTest` verifica caso Okaka, caricamento dopo
  salvataggio e un codice realmente non risolto che resta bloccante.
- I vincoli gia' approvati fra persone distinte, sovrapposizioni di stagione e
  rollback restano quelli della v42.

## Collaudo su Windows

Preflight con SHA-256 e backup, compilazione Maven Java 21, test v43 e regressioni
v42/v41/v40/v39. Non e' previsto alcun aggiornamento automatico delle identita'.
Al primo avvio, filtrare `Proposte da verificare` per `okaka`, selezionare la
riga `Codice storico riutilizzato` e premere `Approva proposte selezionate`.
La conferma mostrera' le occorrenze; nulla viene applicato senza il SI' dell'utente.
