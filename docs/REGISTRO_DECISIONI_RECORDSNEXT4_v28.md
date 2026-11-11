# Registro decisioni RecordsNext 4.0 v28 — ripristino installer e metriche proposte

Addendum 2026-11-06 a `REGISTRO_DECISIONI_RECORDSNEXT4_v27.md`.

| ID | Stato | Decisione |
|---|---|---|
| D-196 | VERIFICATO DAL LOG WINDOWS | Runner NativeGuiPipeline v1 fallisce alla prima compilazione: i backslash dei percorsi sorgente in `@sources.txt` vengono interpretati come escape da javac; output `installed=[]`, nessun file di progetto alterato. |
| D-197 | CORREZIONE V2 DA VALIDARE SU WINDOWS | Scrivere percorsi con `/` e virgolette in ENTRAMBI gli argument file javac (`sources.txt`, `tests.txt`), mantenendo backup, controlli hash e installazione post-test. |
| D-198 | PROPOSTA, NON ATTIVA | Soglie e formule TF05/06/11/12/13/14/15 definite in `SPECIFICA_METRICHE_TOPFLOP_v1.md`; sottoporre a verifica su dati reali prima di abilitare le classifiche. |
| D-199 | VINCOLANTE | Incompresi: E dimostrato da possesso effettivo in un incontro fantasy giocato; TF13 non attivabile con fantavoti imputati o assenti; quattro metriche separate. |
| D-200 | VINCOLANTE | Niente modifiche alla 3.2 operativa, alle skin personalizzate o ai siti durante il fix del runner. |
