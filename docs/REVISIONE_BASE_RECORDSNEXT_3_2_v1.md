# RecordsNext 4.0 — Revisione della base 3.2.0, v1

Data: 2026-10-31. Documento ricavato dall'archivio locale `RecordsNext2.0.zip` fornito dall'utente, NON da una supposizione sulla release. La versione 3.2 e la base invariata di riferimento.

## Fonti effettivamente ricevute e consultate

- `README.md`, `CHANGELOG.md`, `INSTALL.md`, `pom.xml`.
- `docs/ARCHITETTURA_RECORDSNEXT2.md`, `MODELLO_DATI_RECORDSNEXT2.md`, `STATO_IMPLEMENTAZIONE_RECORDSNEXT2.md`, `CONFIGURAZIONE_RECORDSNEXT2.md`, `CATALOGO_RECORD.md`, `DECISIONI_APERTE.md`, `DIPENDENZE_OUTPUT.md`, `INSTALLAZIONE_VISUALIZZATORI_HTML.md` e la restante documentazione sotto `docs/`.
- `docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md`: bibbia di circa 1,8 MB / 43.583 righe, con codice Java/test/scripts inglobato nel documento.
- `tools/Create-RecordsNext2WorkingCodeMd.ps1`: generatore della bibbia 3.2; NON va invocato per fingere che il codice di Fase 0 4.0 sia gia integrato nella base funzionante.
- Cartelle `src/main/java`, `src/test/java`, `tools`, configurazioni, metadati `.git` dell'archivio.

## Stato della 3.2 documentato nel progetto

- Release 3.2.0, commit base `f9bf30323d9204d70e91e242bddfc72c2060ecf9`; il tag annotato `v3.2.0` esiste nell'archivio e punta a tale commit.
- Il worktree 4.0 `feature/recordsnext4-players` risulta gia registrato nei metadati Git forniti; l'esistenza reale sul PC deve essere confermata dal runner.
- Modello modulare con 94 record/figli (93 configurabili + Culometro opzionale); pipeline, preflight, manifest e GUI gia funzionanti nella 3.2.
- Nove output pubblici RecordsNext; shard per stagione in formato **flat nella cartella `js` di ogni sito**, soggetti a cutoff cronologico per sito e a disponibilita storica.
- Le identita canoniche di fantasquadre e competizioni esistono gia; evitare mapping inventati.
- La bibbia 3.2 e una fotografia storica che include anche sezioni di versioni precedenti: le sezioni piu recenti prevalgono sulle note legacy.
- Nel modello `MODELLO_DATI_RECORDSNEXT2.md` sezioni 15-17 sono descritti concettualmente `Giocatore`, `Presenza giocatore`, `Evento giocatore`. Sono specifiche concettuali, non prova di tabelle canoniche gia implementate.

## Identificativi individuali

- `FCA.GiocatoreA.ID` e stagionale; e il riferimento usato nelle relazioni `FCM.Formazione.IDGioc`, `FCM.Tesserato.IDGiocatore`, `FCA.GiocaIn.IDGiocatore` per QUELLA stagione.
- Il codice storico usato per le fotografie risulta in `FCA.GiocatoreA.CODICEGAZZA` nelle tabelle della copia SQLite ricevuta. L'utente indica il codice stabile nel sistema FCM; va verificato il campo esatto del sorgente FCM senza confonderlo con ID di Formazione.
- Non unire automaticamente persone diverse con identico codice, ne trasformare ID annuali in identita all-time.

## Incontri a riposo e tabellini senza incontro: DISTINZIONE OBBLIGATORIA

La documentazione `ARCHITETTURA_RECORDSNEXT2.md` stabilisce esplicitamente: **i turni di riposo non fanno parte di Matches**. Questo non nega che un turno di riposo possa comparire nella sorgente FCM.

Nella copia SQLite analizzata:

- 2025/26: 90 record in `Incontro` con `IDTIPO=2` e un lato squadra uguale a zero; sono candidati coerenti con i riposi, ma la semantica va confermata sul manuale FCM e sugli esempi.
- Esistono anche tabellini con `IDINCONTRO` assente dalla tabella `Incontro`: 168 righe su 15 stagioni nella copia ricevuta. Nei casi esaminati non compaiono righe `Formazione` con lo stesso ID incontro. Alcuni tabellini contengono punteggi plausibili, quindi l'assenza non dimostra un riposo, ne corruzione.
- I due insiemi **non sono intercambiabili**: per classificare un ID mancante servono altri dati/campi o evidenze della fonte originale. Nel frattempo conservarlo come non classificato; non cancellare e non assegnare una competizione o giornata inventata.

## Implicazioni per fase 0

- Corretto il runner v2: controllava il tag prima del worktree esistente e non produceva diagnosi affidabili degli errori Git. La causa interna precisa dell'errore sul PC non e dimostrata; non affermare che il tag manchi.
- Runner v3: verifica prima il worktree esistente, ne controlla ramo e discendenza dal commit 3.2, copia soltanto nuovi file versionati, non sovrascrive file diversi, scrive report in `D:\DEV_APPS\downloads`.
- Sonda aggiuntiva v1 dedicata a possibili riposi e tabellini senza incontro, senza interpretazioni arbitrarie.
- Non cambiare versione Maven, importatori, schema canonico, visualizzatori o GUI in fase 0.

## Verifiche ancora da fare

- Esecuzione PS 5.1 sul PC dell'utente (qui sono stati verificati gli script Python; non e disponibile un runtime PowerShell Windows per collaudare i runner).
- Verifica dei codici storici direttamente sui FCM/FCA originali e del campo FCM esatto, con audit degli ID e delle fotografie.
- Semantica di `Entrato`, `EntratoRiserva`, `Pos`, `Rig`, RU, SV, modificatori e contabilizzazione dei punti non sfruttati.
- Trattamento documentato della stagione manuale senza FCM.
