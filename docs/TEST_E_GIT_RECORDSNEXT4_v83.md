# Test e Git — candidato v83

## Eseguiti nella sandbox di sviluppo

- `javac -encoding UTF-8` sull'intero albero Java main dell'RC snapshot con sostituzione file v83: **PASS** (Java 21).
- `java it.alterlega.recordsnext.app.Rn4V83JavaSmoke` con reflection del `split` e pubblicazione transazionale JS/HTML: **PASS**.
- `node tests/regression_v83.js`: **PASS** su fixture TF20 portiere zero gol e un assist, split punti/null, ricerca, JS size.
- `python -m py_compile` degli auditor Python: **PASS**.

Questi controlli NON includono SQLite reale, XAMPP e `mvn test` (Maven non installato nell'ambiente di sviluppo). Sono inclusi JUnit reali da eseguire con Maven su Windows.

## Da eseguire in RC Windows

- Controllo SHA dei file originali e `git status`: obbligatorio PRIMA dell'installazione.
- `-Mode Build` esegue `mvn test package` su tutti i test vecchi e nuovi e produce `target/RecordsNext.jar`.
- GUI produzione di output e publish-sandbox per ciascun cutoff; `mvn package` se si desidera testare JAR.
- `Verifica-RN4-Integrazione-v83.py` su SQLite reale read-only e oracoli v81; report direttamente in Downloads.
- XAMPP su URL locale, caso Carnesecchi, Simeone 11 stagioni, Cristante, Klose, classifiche Top & Flop, 4 skin e perfomance shard.
- Audit indipendente mancanti per classici/serie/RU/modificatori/Culometro: prerequisito dichiarazione finale.

## Git

Branch base dichiarato `feature/recordsnext4-v67-rc-gui-migrated`; checkpoint `3e8d53f` solo da confrontare. Nessuna simulazione commit locale in questa chat. Inserire commit reale soltanto dopo validazione e stato working tree verificato; annotare hash nel rapporto finale. L'installer contiene modalita `Commit` ma e condizionata a controlli locali e non va usata per mascherare gate numerici incompleti.

## Allegato unico per il rientro in chat

Dopo `-Mode Validate`, allegare `D:\DEV_APPS\downloads\RN4_COLLAUDO_RISULTATI_v83.zip`. Contiene i risultati (compresi eventuali fallimenti), non il database personale.
