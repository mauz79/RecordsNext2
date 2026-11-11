# RecordsNext 4.0 — Configurazione Top & Flop e preview — specifica v2

## Decisioni implementate

1. Dashboard: card dedicata Top & Flop.
2. Top & Flop: schede separate per scelta record, soglie locali, elaborazione completa/consolidata. Il selettore di modalità **usa la stessa modalità globale RecordsNext 3.2**, senza un publisher parallelo.
3. CONSOLIDATED è abilitabile solo se esiste un consolidamento valido della pipeline 3.2, riconosciuto dal file `data/consolidation/recordsnext-consolidation.properties`; in mancanza è correttamente disabilitato e non si finge di poter ricostruire dati dagli shard.
4. Parametri salvati nel nuovo `config/topflop.json`, nessuna modifica al `config/culometro.json` o alle soglie generiche. Persistenza atomica, validazione dei limiti e rifiuto di avvio se il salvataggio fallisce.
5. **TF05:** per stagione, massimo e minimo della media dei voti reali con almeno `realSeasonVotes` voti validi. Non merge storico fra `GiocatoreA.ID` di stagioni diverse.
6. **TF06:** massimo e minimo della fantamedia **conteggiata** per coppia calciatore–fantasquadra–competizione con minimo `fantasyLeagueMatches` in campionato, `fantasyCupMatches` nelle coppe. I calciatori non entrati non contribuiscono.
7. Gli altri campi configurabili (`realCareerVotes`, `fantasyCareerMatches`, `verifiedExclusions`, `exclusionsWithVote`, `anomalySeasonVotes`, `anomalyBaselineVotes`, `anomalyBaselineSeasons`, `anomalyDelta`) sono conservati per successive implementazioni **ma non attivano TF11–TF15 né la carriera pluristagionale**. Non produrre valori fittizi.
8. La preview riusa i JS già generati nello staging: nessuna elaborazione dei DB e nessuna pubblicazione. I quattro CSS 3.2 sono forniti come fallback nel kit e sono copiati solo nella directory di anteprima. Se un CSS del worktree esiste, non si sostituisce.
9. Limite permanente 1.500.000 byte per file JS. Conservati il cutoff e il manifest della 3.2.

## Lavoro non completato

- Esclusioni certe richiedono ricostruzione del possesso e quindi TF11, TF12, TF13 restano in pausa.
- Anomalie pluristagionali TF14 e TF15 richiedono confronto fra identità storiche verificate; non vengono calcolate ancora.
- La configurazione per una carriera complessiva è preparata, ma non autorizza il merge basato sul solo CodiceGazza.
- GUI JAR è eseguibile **DEV** e il visualizzatore non è stato installato nei siti reali.
