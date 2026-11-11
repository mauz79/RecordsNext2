# RecordsNext 4.0 — integrazione nativa Top & Flop v1

Data: 2026-11-06. **Stato: codice compilato e smoke test locale; release e test FULL/CONSOLIDATED Windows da confermare.**

## Principio vincolante

- Nessun database parallelo, nessuna cartella condivisa ReNewo, nessun asset CSS sovrascritto.
- Fonte integrale: `data/database/recordsnext.db` e normalizzati 3.2 già configurati.
- Siti: soltanto viste Careers, Teams, Records, Highlights (non fatti completi FCM/FCA), suddivise per stagione; **ogni JS ≤ 1.500.000 byte fisici**.
- L'exporter gira **una sola volta per elaborazione**, poi `attachScoped` assembla per ciascun sito gli shard delle sole stagioni ammesse da `SeasonPublicationTargetRepository.scope`, secondo il suo `sort_order`.
- JS nella cartella `js` (flat), manifest scritto tramite `ManifestPublishingSupport`, conversione shard 3.2 con `SeasonFamilyShardPublisher.prepareFlat`, pubblicazione e rollback con `RecordsNextPipeline.publishGeneratedDirectory`.
- Non è introdotto alcun nuovo publisher: l'adattatore Top & Flop aggiunge file **solo allo staging**. La pubblicazione resta prerogativa del 3.2.
- Lo SHA-256 di ogni shard sorgente è verificato con i metadati della facade. Collisioni e superamento del limite dimensionale bloccano la procedura.
- Nuova pulizia dei TF obsoleti integrata nella gestione stale+backup+rollback del publisher multisito originale, inclusa rimozione della facade quando la famiglia viene deselezionata.

## GUI

- Nuova voce laterale **Top & Flop**, con **20 checkbox TF01–TF20** e interruttore generale di famiglia.
- Le scelte sono conservate nel consolidato `config/processing.json` usando `processing.families.topFlop.enabled` e `children.tf01...tf20`. Nessuna selezione si attiva automaticamente aggiornando un vecchio file della 3.2.
- Registrazione in `RecordFamily`, `CoreRecordCatalog` e `ProcessingConfigLoader`, senza rinominare i 94 record originali (catalogo ora 114).
- **Attivabili adesso:** TF01–TF04, TF07–TF10, TF16–TF20 (13 record). Per TF16 si espone il confronto per squadra senza vincitore arbitrario; le viste riepilogative si mantengono come supporto.
- **Visibili ma disabilitati:** TF05–TF06 (soglie non fissate), TF11–TF13 (esclusioni/tesseramenti non verificati), TF14–TF15 (baseline non validata). L'attivazione a mano tramite JSON viene rifiutata invece di simulare classifiche.
- I portieri partecipano a TF01–TF10 e a TF17–TF20 (rigori parati, gol subiti, giornate a zero gol subiti registrati, gol/rigori/assist di portieri). Gol su rigore e gol subiti su rigore restano sottocategorie, non somme aggiuntive.

## Viste e selezione

- `TopFlopPublicViewsStageExporter.export(..., selectedIds)` filtra i record TF selezionati e pubblica gli highlight strettamente collegati, mantenendo per TF20 episodi eccezionali di gol/assist dei portieri.
- Anche quando un record non è attivato, lo SQLite mantiene tutti i dati originali. Le viste Careers/Teams fungono da base per la futura scheda di carriera e i confronti di utilizzo.
- `window.fcmRecordsNextTopFlopViews.stagioni` contiene solo le stagioni dello scope di pubblicazione. Il sito di una stagione storica non deve mai includere file o metadati successivi.

## Prove eseguite localmente

- Compilazione Java 21 completa dei sorgenti principali.
- Test 52 verifiche del modello e test sintetico del portiere con doppio contesto fantasy.
- Test lettura/scrittura `processing.json` e scelta granulare TF01/TF04/TF17/TF20.
- Dati reali 2006/07+2026/27: test di scope e dimensioni, facade 2006/07 priva di riferimenti 2026/27.
- Pipeline di pubblicazione 3.2 su cartella **temporanea di prova**: manifest aggiornato, file TF presenti, disabilitazione rimuove facade+shard senza rimuovere file non TF.
- UI Swing con schermo virtuale: nuovo pannello presente, 20 checkbox, indisponibili i record in verifica, selezionabili i portieri.
- **Non ancora dimostrati:** esecuzione completa FULL e CONSOLIDATED sul computer Windows dell'utente, deploy ReNewo e reali link site-wide, visualizzatori HTML definitivi. Questa patch non realizza le classifiche Incompresi incomplete e non è un rilascio 4.0.0.

## Installazione

Installer `tools/Invoke-RecordsNext4NativeGuiPipeline-v1.py`, con backup degli originali, validazione SHA-256, 21+ test e bibbia `docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v26.md`. **L'installer non esegue publish sui siti e non altera il database.** Dopo il test Windows caricare solamente il suo ZIP diagnostico.
