# Registro decisioni RecordsNext 4.0 v27 — integrazione nativa

Addendum 2026-11-06, preserva `REGISTRO_DECISIONI_RECORDSNEXT4_v26.md`.

| ID | Stato | Decisione |
|---|---|---|
| D-188 | IMPLEMENTATA/TEST LOCALE | `TOP_FLOP` entra nel catalogo: 20 ID `topFlop.tf01...tf20`, senza rinumerare o modificare i precedenti 94 record. |
| D-189 | IMPLEMENTATA/TEST LOCALE | GUI con pannello dedicato e scelta granulare persistente nel `processing.json`; default storico disattivo per compatibilità con i siti 3.2. |
| D-190 | VINCOLANTE | TF05–TF06, TF11–TF15 non selezionabili finché soglie, baseline ed esclusioni non sono verificati; una configurazione che li attivi va rifiutata. |
| D-191 | IMPLEMENTATA/TEST LOCALE | Export TF una volta per elaborazione, poi scope per sito da `SeasonPublicationTargetRepository.scope`, senza copiare futuro su sito storico. |
| D-192 | IMPLEMENTATA/TEST LOCALE | `ManifestPublishingSupport` e `SeasonFamilyShardPublisher.prepareFlat` della 3.2; ultimo passaggio attraverso `publishGeneratedDirectory` con backup e rollback esistenti. |
| D-193 | IMPLEMENTATA/TEST LOCALE | JS TopFlop obsoleti (compresa facade) eliminati con backup/rollback della 3.2 quando la famiglia viene disattivata o si restringe lo scope. |
| D-194 | VINCOLANTE | **Ogni singolo JS non supera 1.500.000 byte.** Validazione SHA-256 per le viste e validazione dimensione dell'intero staging prima di pubblicare. |
| D-195 | DA VERIFICARE SU WINDOWS | Eseguire runner su copia del worktree, controllare visualizzatore HTML, FULL/CONSOLIDATED e pubblicazione multisito prima di chiamare il rilascio 4.0.0 pronto. |
