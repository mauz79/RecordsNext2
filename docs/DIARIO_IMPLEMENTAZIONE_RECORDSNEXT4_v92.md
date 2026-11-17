# RecordsNext 4.0 — Diario v92

18 novembre 2026. Input: quattro screenshot reali dopo deploy v91, due schermate Top & Flop e una dell'Archivio. L'utente ha segnalato layout non leggibile, TF20 combinato, colonne senza dati e dettaglio competizioni aperto lateralmente.

**Diagnosi nel codice v86:** URL skin `../profiles/<skin>/recordsnext.css` non presente nello snapshot v85 del sito, CSS v83 minimale, `min-width` 1680/1440 e stili inline 1750/1500. La regola `.rn-toolbar label{display:flex}` prevale su `[hidden]`, rendendo visibili i filtri non pertinenti. La funzione `teamRows` crea un `details` come cella a destra della riga, non una riga sottostante.

**Decisione:** preservare TF01–TF20 e il motore Java; TF20 conserva un solo dataset/ID Java, con due viste selezionabili indipendenti Gol/Assist. TF17 e' gia' la classifica rigori parati. Gli indicatori FCM e i calcoli v79 rimangono invariati.

**Implementazione:** cinque risorse di pagina/JS versionate (due HTML canonici, 2 JS v92, CSS v92), riferimento nativo Java `RecordsNextPipeline.java`, documentazione, Bibbia v49, installatore con backup e rollback. Gli altri file v83 e gli archivi restano intatti.

**Test:** Browser automatizzato su fixture sintetica; le screenshot prodotte in `/tests/visual` non rappresentano valori del database reale. Java compilato contro le classi v86. L'installazione Windows e la navigazione nel sito reale richiedono verifica dell'utente.
