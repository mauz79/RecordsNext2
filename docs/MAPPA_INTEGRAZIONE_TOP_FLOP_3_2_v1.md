
# RecordsNext 4.0 - Mappa integrazione Records Top & Flop nella 3.2 - v1

**Fonte verificata**: `RecordsNext2.0.zip` caricato dall'utente, percorso `src/main/java/it/alterlega/recordsnext/`. File reali ispezionati; nessuna progettazione di publisher alternativo.

| Fase tecnica | File/classe esistente | Intervento 4.0 richiesto (non ancora fatto) | Regressione minima |
|---|---|---|---|
| Catalogo famiglia | `app/model/RecordFamily.java`, `CoreRecordCatalog.java` | Aggiunta `TOP_FLOP` e figli granulari reali, dipendenze appropriate | `CoreRecordCatalogCompletenessTest`, `ModularProcessingModelTest` |
| Configurazione | `app/config/ProcessingConfigWriter.java`, `ProcessingConfigLoader.java`, `config/processing.json` | Selezione `topFlop` e figli con compatibilita read/write delle configurazioni 3.2 | `ProcessingConfigWriter*Test`, `ProcessingConfigLoaderTest` |
| Validazione prerequisiti | `app/PipelinePreflight.java`, `app/model/DependencyInventory.java`, `ExecutionPlanner.java` | Verificare sorgenti FCA/FCM, dati disponibili, eventuale records disabilitato; non mostrare generato se mancano dati | `ExecutionPlannerTest`, `PipelinePreflightTest` |
| GUI nativa | `gui/RecordsNext2Dashboard.java` e stato ProcessingConfigWriter.State | Aggiungere pagina/checkbox famiglia e figli; non scegliere competizioni nella GUI | Test GUI/configurazione, apertura GUI, persistenza |
| Normalizzazione e origine link | `SeasonNormalizedExporter.java`, `app/matches/MatchesJsExporter.java` | Reimpiego ID partita e `scorecardUrl` normalizzato; never build URL manuale | `SeasonNormalizedExporter`/`MatchesJsExporter` test contratto, test siti storici |
| Archivio stagionale | `SeasonRecordsArchiveBuilder.java`, `RecordsNextPreparationService.java`, `app/RecordsNextPipeline.java` | Nuovo archivio Top & Flop e ricostruzione FULL/incrementale CONSOLIDATED nello stesso ciclo | `RecordsNextPipelineSeasonScopeTest`, `RecordsNextPipelinePublicationModeTest` |
| Esportazione famiglia | `Records2026SitePublisher.java`, `app/*/*FamilyJsExporter.java` | `TopFlopFamilyJsExporter` nello STESSO publisher con prefisso/file/globale ufficiale; allineare `validateGenerated` e `expectedTotal` | Test exporter, validazione file, rollback |
| JS stagionali | `app/output/SeasonFamilyShardPublisher.java` | Inserire spec TopFlop nella lista privata `FAMILIES` rispettando flat shards in `js` e massimo 1536 KiB; niente sottocartelle | `SeasonFamilyShardPublisherFlatTest`, `SeasonFamilyShardPublisherTest` |
| Manifest e dipendenze | `app/manifest/ManifestPublishingSupport.java`, `ManifestJsWriter.java` | Aggiungere nuova famiglia ai metadati validati senza perdere file preesistenti | `ManifestJsWriterTest`, `ManifestPublishingSupportTest` |
| Siti e rollback | `RecordsNextPipeline.java`, `app/output/SeasonPublicationTargetRepository.java`, `Records2026SitePublisher.java` | Scope stagione/sito e target storici per shards, publish atomica + rollback gia 3.2 | `RecordsNextPipelinePublicationModeTest`, `SeasonPublicationScopeRepositoryTest` |
| Visualizzazione | Visualizzatori HTML e installazione attuali | Integrare Records Top & Flop come nuova sezione e scheda giocatore, consumando gli stessi JS; senza imporre filtro competizioni in GUI | test HTML link storici, siti e filtri, zero Youth League |

## Vincoli esistenti verificati nel codice

1. `RecordsNextPipeline.IMPLEMENTED_FAMILIES` elenca le famiglie ammesse: Top & Flop **non** deve esservi aggiunta prima del motore utilizzabile, altrimenti si rischia esecuzione fittizia.
2. `Records2026SitePublisher.validateGenerated` valida prefisso, nomi e numero atteso JS: ogni nuova famiglia richiede modifica coordinata al conteggio e ai test.
3. `SeasonFamilyShardPublisher.FAMILIES` e una lista esplicita, shard flat per stagione sotto la `js` del sito, con `DEFAULT_FLAT_MAX_SHARD_BYTES=1536*1024`.
4. `RecordsNextPipeline` supporta modalita FULL/CONSOLIDATED e scope di pubblicazione per singolo sito o tutti quelli configurati. Riutilizzare quel ciclo e `publishGeneratedDirectory`, non creare deploy personalizzati.
5. `MatchesJsExporter` esporta `seasonId`, `competitionId`, `matchId`, `scorecardUrl`. Il collegamento non va ricostruito con solo numero di giornata.
6. `ProcessingConfigWriter.State` espone campi fissi: aggiungere la famiglia richiede adeguare anche costruzione e salvataggio dalla GUI, con compatibilita dei vecchi file JSON.

## Ordine attuativo

**Primo commit tecnico futuro:** modello fatti individuali canonicali + regressione su sample/stagioni reali, senza ancora cambiare GUI/pubblicazione. **Secondo blocco:** exporter + family catalog/config + GUI + shard/manifest/pubblicazione e relativi test. Versionare ogni sorgente modificato, consolidare in Java solo dopo verifica. La versione 3.2 operativa non viene mai modificata.
