# RecordsNext 4.0 — Changelog v92 (revisione impaginazione)

Data: 18/11/2026. Base operativa: RC v64 con integrazione v86, generazione GUI 87/87 e verifica staging v88, pubblicazione di prova v91.

## Interventi

- `top_flop.html`: layout responsivo, barra filtri con soli campi pertinenti, intestazioni e breadcrumb leggibili. Corretto il conflitto CSS che rendeva visibili i label con attributo `hidden`.
- `top_flop_v92.css`: CSS autosufficiente quando gli URL opzionali delle skin `/profiles/.../recordsnext.css` sono assenti, mantenendo i quattro nomi di skin senza modificare i file originali. Rimossi i `min-width` forzati da 1500/1750 px.
- `fcmRecordsNextFunzioni_topflop_v92.js`: colonne contestuali, non mostra una colonna se l'intero risultato non contiene un valore utile; gli zeri rimangono valori numerici e non sono convertiti in `null`. TF20 presentato come due scelte autonome nel catalogo visuale: **TF20-G** (gol segnati dai portieri) e **TF20-A** (assist dei portieri). Rigori parati già autonomi in **TF17**. Le due scelte TF20 riutilizzano lo stesso ID di esportazione Java `topFlop.tf20` e lo stesso dato FCA/FCM, senza duplicarlo e senza alterare i 87 record GUI.
- `calciatori.html` / `fcmRecordsNextCalciatori_v92.js`: riepilogo compatto stagione × fantasquadra. Il pulsante `Apri` crea una **riga di dettaglio immediatamente sotto quella selezionata**, estesa a tutte le colonne. I 15 indicatori completi sono nel riepilogo espanso; ogni competizione è espandibile singolarmente con gli stessi 15 valori. Ordinamento conservato.
- `RecordsNextPipeline.java`: usa gli asset JS/CSS v92 nel normale processo Java `Genera / Verifica / Pubblica`, mantenendo la pubblicazione congiunta JS + due HTML + CSS e il rollback esistente. Non cambia importazione, schema SQLite, motori FCA/FCM, regole 3.2 o dati JS.
- Documentazione canonica v49 e generatore versionato `Genera-Bibbia-RecordsNext-v92.py`.

## Non modificato

- 21 stagioni, 240.096 apporti FCM COUNTED, identità approvate, punti `MODIF = TOT - VOTO`, split condizionato; classici, serie, RU, modificatori, soglie, fortuna e Culometro già funzionanti dalla 3.2.
- Motore di classifica e ranking `fcmRecordsNextTopFlopRankingEngine_v83.js`; solo presentazione differente.
- Limite 1.500.000 byte per JS, cutoff sito, configurazioni GUI e i dati staging v88.

## Verifiche

- `node --check` su entrambi i JS v92: OK.
- `javac` Java 21 contro classi v86: OK.
- `node tests/regression_v83.js` sul codice v86: OK (logica numerica preesistente).
- Browser Chromium headless su fixture sintetica: classifiche TF20-G / TF20-A e TF17, filtri hidden, colonne contestuali, espansione sotto la riga, 15 indicatori, due competizioni, layout 1440 e 390 px: OK.
- **Restano da svolgere sul PC:** Maven `test package` sui sorgenti RC aggiornati, deploy nella sandbox, verifica visiva con dati reali e quattro skin; esito commit Git.
