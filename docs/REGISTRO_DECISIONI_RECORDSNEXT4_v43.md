# Registro decisioni — v43 (integrazione v83)

- D-083-01: un solo exporter Java, una sola GUI e una sola transazione di pubblicazione HTML+JS; nessun exporter Python di produzione.
- D-083-02: `top_flop.html` solo classifiche, `calciatori.html` archivio storico e schede; manifest/JS condivisi.
- D-083-03: TF20 non ha primati con valore 0; insieme gol, rigori segnati (sottoinsieme), assist, gol+assist; zero nel dettaglio e consentito.
- D-083-04: 15 indicatori identici per stagione x fantasquadra e competizione, con mediane NO, medie ponderate SI; saldo `SUM(FCM.MODIF)`.
- D-083-05: split bonus/malus solo se univoco e riconciliato per ogni COUNTED; altrimenti `null`, saldo osservato preservato.
- D-083-06: carriere solo identita esplicitamente approvate; cutoff di ogni sito prevale su anno corrente del calendario.
- D-083-07: il record privo di una fonte non e certificabile. I vecchi test sintetici non certificano i numeri reali.
- D-083-08: script e rapporti futuri da scambiare solo direttamente sotto `D:\DEV_APPS\downloads`.
- D-083-09: il commit Git e la certificazione dei record classici dipendono dall'esito dei gate Windows, non sono automaticamente soddisfatti.
