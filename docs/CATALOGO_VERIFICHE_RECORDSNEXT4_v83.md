# Matrice verifiche v83 — unita e fonte di verita

**Criterio:** implementato non significa verificato. I controlli locali Java/Node sono *SINTETICI*. Gli oracoli v81 FCA sono *REAL*. Lo schema FCM v83 verra confrontato sul RAW locale dall'auditor, senza ripetere l'estrazione v81 FCA.

| Record | Unita e origine | Test disponibile | Stato al confezionamento |
|---|---|---|---|
| TF01 / TF02 | FCA VOTO1 per player-giornata, voto positivo | Oracolo 21 stagioni v81 + ancora estremi; confronto JS predisposto | PARZIALE (non eseguito v83 su DB) |
| TF03 / TF04 | FCM TOT singolo slot COUNTED | Exporter Java Full; auditor punti FCM RAW | PARZIALE |
| TF05 | FCA voto medio pesato da numeratori/denominatori | Oracolo voti v81 per player/stagione | PARZIALE |
| TF06 | FCM TOT media su gare COUNTED | Auditor indipendente contesti FCM | PARZIALE |
| TF07 / TF08 | FCA reale e FCM apportato, eventi positivi | Regressione Node + oracolo FCA eventi v81 | PARZIALE |
| TF09 | episodi per tipo, picco/anni/carriera/100 presenze | Test Node schemi; oracolo FCA eventi v81 | PARZIALE |
| TF10 | FCA giornate voto / FCM gare COUNTED | Audit v81 + conto RAW v79 | PARZIALE |
| TF11–TF13 | solo panchine FCM documentate | Policy nativa preesistente, nessuna deduzione tribuna | NON CERTIFICATO |
| TF14 / TF15 | differenze voti vs baseline identita approvata | Viste gia presenti; cutoff da collaudare | NON CERTIFICATO |
| TF16 | confronto squadre utilizzatrici (FCM COUNTED) | Auditor 15 indicatori/contesti | PARZIALE |
| TF17 | FCA/FCM rigori parati | Oracolo eventi v81 + test positivo | PARZIALE |
| TF18 | gol subiti portieri, rigori sottoinsieme | Oracolo eventi v81 + test positivo | PARZIALE |
| TF19 | gare con zero gol subiti e campione reale | Policy filtro e conteggio esistenti | NON CERTIFICATO |
| TF20 | gol+assist portieri con campione e zero non-primato | bug reale v81 + Node v83, Carnesecchi 1 assist | PARZIALE (fix sintetico verificato) |
| Scheda 15 indicatori | FCA reale separato FCM COUNTED | Smoke Java e Node; auditor RAW FCM 21 annate pronto | PARZIALE |
| Classici | score, gare, punti, risultati e gol | matrici documento maestro v81 | NON CERTIFICATO |
| Serie | contiguita turni, partite differite | matrici documento maestro v81 | NON CERTIFICATO |
| Riserve PU/DU/CU/AU | regolamento, eventi e risultati | matrici documento maestro v81 | NON CERTIFICATO |
| Modificatori Difesa/Capitano/Campo | evidenze FCM e regolamento | matrici documento maestro v81 | NON CERTIFICATO |
| Soglie e fortuna | differenza punteggi reali/soglie | matrici documento maestro v81 | NON CERTIFICATO |
| Culometro | frequenze evento/configurazione, impatto | matrici documento maestro v81 | NON CERTIFICATO |
| Quattro skin / cutoff / URL tabellini | UI XAMPP + dati per sito | collaudo Windows necessario | NON CERTIFICATO |
| Limite singolo JS RecordsNext | byte sul disco <=1.500.000 | vincolo Java e auditor sul disco | PARZIALE |

Report certificante: `D:\DEV_APPS\downloads\RN4_VERIFICA_INTEGRAZIONE_v83.json` dopo esecuzione sul DB reale; `errors_total=0` significa solo parita **dei confronti coperti**, non di classici/serie/RU/Culometro.
