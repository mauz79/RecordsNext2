# RecordsNext 4.0.0 — Piano giocatori v3

Data: 2026-10-31. Stato: **FASE 0, AUDIT; NESSUNA RELEASE 4.0.0 ANCORA REALIZZATA**.

## Obiettivo

Per ciascun calciatore estrarre TUTTI i dati reali disponibili per stagione e giornata FCA, TUTTI i dati fantasy per stagione, fantasquadra, competizione e partita FCM, quindi produrre statistiche e record reali e fantasy per stagione, squadra, competizione e all time.

## Base tecnica vincolante

Il progetto di riferimento e **RecordsNext v3.2.0** del 2026-09-16, commit `f9bf303`, con documentazione effettiva e sorgenti presenti in `RecordsNext2.0.zip`. Il registro delle decisioni e la revisione `REVISIONE_BASE_RECORDSNEXT_3_2_v1.md` fanno parte della continuita operativa. Il worktree `D:\DEV_APPS\RecordsNext4.0` puo gia esistere e non deve essere ricreato automaticamente se valido.

La 3.2 ha 9 output, architettura multistagione e multisito, JS stagionali flat direttamente nella `js` del relativo sito e cutoff di ogni sito alla sua stagione. Mantenerli invariati finche il nuovo modulo non e stato implementato e testato.

## Identificativi

- `GiocatoreA.ID`: ID **locale FCA stagionale**.
- `Formazione.IDGioc`, `Tesserato.IDGiocatore`, `GiocaIn.IDGiocatore`: collegano l'ID locale alla stagione corrispondente.
- `GiocatoreA.CodiceGazza` negli FCA analizzati: codice storico usato nelle foto; l'utente riferisce il codice persistente nel sistema FCM, ma la sua ubicazione esatta nel file FCM va accertata. Esempio 100843 = Lautaro.
- Gli accoppiamenti pluristagionali richiedono trattamento di riutilizzi/anomalie nei codici; non si deve cambiare il significato dei campi esistenti.

## Entita logiche DA PROGETTARE (non implementate)

1. Identita canonica del calciatore / eventuale alias storicizzato.
2. Anagrafica stagionale: stagione + ID FCA locale e tutti i dati sorgente.
3. Prestazione reale: stagione + ID giocatore locale + giornata di Serie A; voti/eventi/contestualizzazione, con SV e dati mancanti separati da zero.
4. Partecipazione fantasy: stagione + partita FCM + fantasquadra + ID locale + occorrenza; ruolo e stato titolare/riserva/utilizzato, dettaglio tabellino e contributo ufficiale.
5. Evidenze di tesseramento: nessuna data trasferimento dedotta da righe non datate.
6. Record: dimensione reale o fantasy, ambito, filtro, soglie, denominatore, parita e provenienza fino al dato elementare.

Non contare una prestazione reale piu volte solo perche il fantacalciatore ha disputato piu competizioni legate alla stessa giornata.

## Incontri speciali e tabellini

`Matches` ESCLUDE i turni di riposo secondo architettura 3.2. Incontro con un lato squadra a zero e tabellino il cui `IDINCONTRO` manca da `Incontro` vanno censiti SEPARATAMENTE: non sono sinonimi, non eliminare nessuna riga e non costruire collegamenti fittizi.

## Roadmap e criteri di uscita

| Fase | Lavoro | Verifica | Stato |
|---|---|---|---|
| 0 | Worktree isolato; audit codici, schema, referenze, riposi/tabellini | Esiti riproducibili e Git 3.2 invariato | In corso |
| 1 | Dati reali per stagione/giornata, anche non tesserati | Confronto FCA `GiocaIn`, `Punteggio`, `Statistiche`, `Storico`; nessun doppio conteggio | Da iniziare |
| 2 | Dati fantasy per gara/competizione/squadra | Accordo con FCM `Formazione` e `Tabellino`; gestione riserve, RU, match speciali | Da iniziare |
| 3 | Catalogo record reali, fantasy e punti non sfruttati | Definizioni statistico-regolamentari e casi limite verificati | Da iniziare |
| 4 | Output JS e HTML/GUI | Cutoff multisito, shard flat, nessuna regressione 3.2 | Da iniziare |
| 5 | Regressioni, bibbia, FULL/UPDATE, release 4.0.0 | Test, commit e tag solo dopo approvazione | Da iniziare |

## Documentazione vivente

- Base storica 3.2: `docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md` (NON toccare).
- Nuova bibbia incrementale: `docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v2.md`, generata da `tools/Create-RecordsNext4WorkingCodeMd-v2.ps1`.
- Piano: questo file v3.
- Decisioni: `docs/REGISTRO_DECISIONI_RECORDSNEXT4_v2.md`.
- Diario: `docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v2.md`.
- Verifica base: `docs/REVISIONE_BASE_RECORDSNEXT_3_2_v1.md`.

Non dichiarare "funzionante" cio che e solamente progettato. Ogni successiva revisione sostituisce i nomi con nuove versioni; tenere traccia del codice completo e dei test nel generatore.
