# Diario RecordsNext 4.0 — v30 — 2026-11-06

- Riprodotto errore preview su worktree senza `visualizzatori/profiles/{skin}/recordsnext.css`.
- Nuovo script preview v2 usa i quattro CSS ufficiali distribuiti nel kit, solo quando il file locale manca.
- Modificati GUI, ProcessingConfigWriter (abilitazione TF05/TF06), pipeline/adapter ed exporter di viste; aggiunto TopFlopSettings con persistenza JSON e controllo limiti.
- Test: `TopFlopSettingsSmokeTest`, keeper/doppio contesto, GUI 20 record e 11 soglie, compilazione JDK 21, export su 2006/07 e 2026/27, validazione dimensioni.
- Limite 1.500.000 byte per file; nessuna pubblicazione reale. JAR DEV versionato v2 separato dalla 3.2.
