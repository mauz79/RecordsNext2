# RecordsNext 4.0 - changelog v115

- Corretto NullPointerException introdotto in v114 in `TopFlopPublicViewsStageExporter.packExtremo`, chiamato durante generazione Top & Flop.
- La giornata di competizione FCM e opzionale nel modello: il payload compatto ora conserva null in un elenco modificabile invece di costruirlo con `List.of`.
- Test riproduzione NPE v114, test serializzazione/decodifica valori null, smoke del JAR Windows prima della sostituzione.
- Nessuna modifica ai database, ai registri identita o ai 21 siti durante l'installazione.
- GitHub release e installer pubblico v107 restano da ricompilare sulla base v115.
