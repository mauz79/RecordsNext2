# RecordsNext 4.0 - decisioni consolidate v29 integrata

1. La GUI RecordsNext 4.0 e' l'unico punto di avvio dell'elaborazione, completa o consolidata. Nessun secondo comando di elaborazione dentro Top & Flop.
2. Preservare output 3.2 e motore canonico associazioni storico/attuali fantasquadre e competizioni. Gli output Top & Flop si aggiungono nel normale staging e nella pubblicazione esistente.
3. La carriera storica del calciatore richiede `playerIdentityId` approvato in configurazione iniziale. Il codice Gazzetta non e' identita' universalmente unica. Nessuna riconciliazione silenziosa, neanche dei nomi esatti.
4. Persistenza in `config/player-identities.json`. Le proposte esatte possono essere approvate in blocco; nomi diversi, collisioni di codice e omonimie devono poter essere accettati o distinti manualmente e revocati.
5. Le prestazioni reali delle singole giornate FCA si contano una volta sola; i contesti fantasquadra/competizione FCM restano distinti. Non sommare gli utilizzi fantasy come fossero partite reali del calciatore.
6. Statistiche 'dentro/fuori': media voto, fantamedia tabellino quando disponibile, bonus/malus osservati, senza simulare formazioni, moduli, sostituzioni e punti recuperabili.
7. Costi: min, max, media dei costi Tesserato.COSTOACQ documentati, con numerosita' campione. Valore tecnico IVS solo dopo calibrazione della formula da GUI.
8. Le soglie di ranking e i filtri saranno preferibilmente nel nuovo sito Renewo; non pre-tagliare l'insieme delle carriere e dei Teams pubblicati.
9. Il contratto principale sono i JS prodotti dal plugin, non la pagina HTML di anteprima. Gli shard devono essere <=1.500.000 byte reali.
10. Le nuove viste History devono rispettare rigorosamente il cutoff temporale della 3.2 per ciascun sito destinatario; non inviare dati futuri ai siti storici.
11. Nessuna modifica ai siti ne' ai database in sede di patch: backup, compilazione e test locali prima dell'elaborazione pubblicante.
