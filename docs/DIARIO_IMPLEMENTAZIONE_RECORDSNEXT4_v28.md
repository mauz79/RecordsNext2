# Diario implementazione RecordsNext 4.0 v28

2026-11-06. Ricevuto `RecordsNext4_NativeGuiPipeline_OUTPUT_v1_20261106_162900_806104.zip`.

Diagnosi: `javac.EXE => 2`, `Invalid filename: D:DEV_APPSdownloads...`; i percorsi con backslash non escapati nel file di argomenti `@sources.txt` sono analizzati dal JDK come sequenze di escape (`\r`, ecc.). Il `result.json` indica baseline verificata, `installed: []`, siti e database intatti.

Intervento v2: `Invoke-RecordsNext4NativeGuiPipeline-v2.py` conserva il kit Java del v1 byte per byte, normalizza gli argomenti sorgente/test a slash forward con virgolette, produce output/backup propri v2 e verifica la regola dei percorsi; manifest SHA-256 ricalcolato.

Fissata la bozza di sette metriche pendenti con numeratori, denominatori, soglie e regole sulle assenze, in `SPECIFICA_METRICHE_TOPFLOP_v1.md`. **Non sono attivate** e non sono presentate come già approvate.

Prossima verifica: runner v2 Windows, quindi JAR GUI con test FULL/CONSOLIDATED e visualizzatore.
