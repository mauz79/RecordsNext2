# Diario implementazione — RecordsNext 4.0 v83

17/11/2026 — Ripartenza dal passaggio chat v82, lettura dossier completo e documenti v81; acquisiti snapshot 243 sorgenti Java/GUI/frontend, oracoli v81 e preview v79.

Riscontro difetto TF20 nella funzione listRank v69: zero dei gol trattato come vincitore e colonna limitata a evento selezionato. Oracolo v81 conferma 0 gol e 1 assist di Carnesecchi.

Intervento unico preparato: exporter nativo Java basato su archivio JDBC canonico; shards FCM Points, FCA Real/Counted e FMT, indice identita; adapter TopFlopViewsPublication; pipeline Java con due HTML e JS atomici, GUI invariata nella modalita di funzionamento; ricerca globale spostata in `calciatori.html`.

Verifiche in ambiente di sviluppo: compilazione `javac` della totalita dei sorgenti principali Java modificati; smoke Java per bonus/malus e rollback; Node per primati zero, evento portiere, punti null e ricerca. **Non e stato eseguito il vero SQLite Windows**, ne il collaudo reale XAMPP/Git: gli esiti non sono intercambiabili.

Per riprodurre e completare: installare con un solo PowerShell dall'archivio in Downloads, avviare la GUI RC, generare e pubblicare nella sandbox, eseguire verifier indipendente e leggere i report creati direttamente in Downloads. Bloccare una release dichiarata definitiva quando non sono certificati classici, serie, RU, modificatori, soglie/fortuna, Culometro.
