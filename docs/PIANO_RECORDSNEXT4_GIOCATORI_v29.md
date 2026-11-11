# RecordsNext 4.0 - piano lavoro dopo integrazione v29

Realizzato e in collaudo: gestione delle identita' (GUI e config persistente), nuove carriere storiche per identita' approvata, statistiche fuori/dentro osservate, costi di acquisto, pipeline storica con cutoff, decoder compatibile.

Prossimi gate di completamento nell'installazione Windows:

1. Compilazione Maven/Java 21 con le dipendenze SQLite reali; avvio GUI e salvataggio/revoca associazioni; conferma separazione ASCOLI/CRIVELLO 12302.
2. Elaborazione Completa/Consolidata con Top & Flop su 21 annate; test bilancio realDays, contesti fantasy, prezzi, non conteggiati e controllo sha/byte di tutti i JS.
3. Validazione dei totali reali e delle medie sulle prestazioni originali; una sola giornata reale per calciatore e un contesto indipendente per ogni fantasquadra.
4. Fonte di fantavoto reale unica: verificare scoring Gazzetta/FCM senza dedurlo dal contributo di una fantasquadra.
5. Ricostruzione del possesso giornaliero solo se documentabile, altrimenti limitare gli indicatori di fuori formazione a quelli realmente osservati.
6. Costruire le graduatorie storiche e annuali per real/fantasy, con soglie lato Renewo; sfruttare le associazioni squadre della 3.2; definire IVS con parametri configurabili.
7. Verificare percorsi immagini storiche e completare l'indice foto con fallback.
8. Visualizzatore di prova che legge unicamente JS appena pubblicati dal plugin; regressione senza perdita degli output classici della 3.2.
9. Pubblicazione protetta e release finale con GUI verificata su Windows.

Non scambiare una milestone compilata con una release completa: i gate sopra devono essere superati.
