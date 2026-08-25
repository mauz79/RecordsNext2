# Analisi personale RU

## Scopo

Questa parte di RecordsNext è una diagnostica personale e NON costituisce
una famiglia di record pubblica.

Serve a valutare storicamente l'effetto delle Riserve d'Ufficio (RU) e delle
regole sulle sostituzioni, con particolare attenzione a:

- frequenza reale delle RU nel tempo;
- frequenza delle squadre rimaste sotto 11;
- casi sotto 11 senza RU;
- numero di giocatori mancanti;
- effetto delle RU sull'esito delle partite;
- evoluzione storica del valore/regola delle RU;
- confronto fra periodi regolamentari differenti;
- simulazione controfattuale con massimo 5 sostituzioni.

Gli output sono destinati all'analisi privata e non devono essere pubblicati
automaticamente nel sito RecordsNext.

## Separazione dagli output pubblici

Questa diagnostica:

- non è una RecordFamily;
- non entra nel Manifest pubblico;
- non modifica fcmRecordsNext_RU.js;
- non modifica il Culometro;
- non modifica score o ranking;
- non deve essere copiata automaticamente nel sito;
- scrive i propri risultati in data/personal-reports.

Gli archivi e i normalizzati RecordsNext sono la fonte dei dati, ma
l'elaborazione personale resta separata dalla pipeline pubblica.

## Analisi storica RU

L'analisi ricostruisce l'uso delle RU sull'intero storico disponibile.

Tra le informazioni analizzate:

- partite con almeno una RU;
- occorrenze RU;
- squadre-partita interessate;
- partite distinte interessate;
- RU di movimento;
- RU portiere;
- valore assegnato alle RU;
- RU decisive;
- effetto sull'esito:
  - sconfitta -> pareggio;
  - pareggio -> vittoria;
  - sconfitta -> vittoria;
  - nessun effetto.

La frequenza deve essere sempre rapportabile all'universo reale delle partite,
in modo da non giudicare una regola soltanto sui casi in cui il problema si
è già verificato.

## Evoluzione storica delle regole RU

L'analisi individua i valori RU osservati nelle diverse stagioni.

In particolare è stata implementata l'identificazione automatica del blocco
cronologico moderno e continuo compatibile con:

- RU movimento = 3;
- RU portiere = 2.

Questo permette di distinguere lo storico complessivo dal periodo regolamentare
moderno e di confrontare correttamente frequenze ed effetti.

## Squadre sotto 11

Una seconda parte misura quanto sia realmente frequente giocare in inferiorità
numerica.

Vengono distinti almeno:

- sotto 11 senza RU;
- 1 giocatore mancante;
- 2 giocatori mancanti;
- 3 giocatori mancanti;
- situazioni più estreme;
- tabellini vuoti/anomali da non usare nelle simulazioni.

La misura principale non è soltanto il numero di casi-squadra, ma anche il
numero di partite distinte e la percentuale rispetto a tutte le partite reali.

## Simulazione con 5 sostituzioni

È stata realizzata una simulazione controfattuale per rispondere alla domanda:

"Cosa sarebbe successo se il limite massimo fosse stato di 5 sostituzioni?"

La simulazione parte dai casi sotto 11 senza RU e ricostruisce, quando possibile:

- gli 11 titolari iniziali;
- le sostituzioni realmente utilizzate;
- i ruoli rimasti scoperti;
- i panchinari con voto valido e ruolo compatibile;
- il numero di ulteriori sostituzioni disponibili fino al limite di 5.

Per ogni caso vengono ricavati almeno:

- Simulabile;
- SostituzioniUsate;
- ExtraFinoA5;
- RuoliMancanti;
- PanchinaValidaPerRuolo;
- Compatibili;
- RisoltoCon5.

Un caso è considerato risolto solo se i ruoli mancanti possono essere coperti
da riserve valide e il numero necessario di ingressi rientra nelle sostituzioni
aggiuntive disponibili.

I risultati devono essere rapportati sia:

1. ai soli casi problematici simulabili;
2. all'intero universo storico delle partite.

Il secondo rapporto è indispensabile per valutare l'impatto reale di una
eventuale modifica regolamentare.

## Script da preservare

Attualmente gli strumenti personali RU da preservare sono:

- tools/Report-RU-InDieci-Storico.ps1
- tools/Report-RU-Storico-Personale.ps1
- tools/Report_RU_5Sostituzioni_v6_1.ps1
- tools/Report_RU_Storico_Completo_v8_4.ps1

Report_RU_5Sostituzioni_v6_1.ps1 contiene la simulazione specifica delle
5 sostituzioni.

Report_RU_Storico_Completo_v8_4.ps1 contiene l'analisi storica RU più estesa,
compresa la periodizzazione delle regole osservate.

Gli altri due script sono strumenti ausiliari/personali preesistenti e devono
essere conservati finché non venga verificato esplicitamente che ogni loro
funzione utile sia stata assorbita dallo strumento completo.

## Regola di manutenzione

Questi strumenti non devono essere eliminati come semplici probe o file
temporanei.

Prima di rimuovere o sostituire uno script personale RU occorre verificare:

1. quali domande diagnostiche risponde;
2. quali output produce;
3. se tali informazioni sono già riprodotte da uno strumento successivo;
4. se la riproduzione usa lo stesso universo di partite e le stesse esclusioni.

Le analisi personali possono evolvere indipendentemente dalle famiglie
pubbliche RecordsNext.

## Stato al 26 agosto 2026

La funzionalità esiste ed è stata usata durante lo sviluppo di RecordsNext 2.0,
ma non era stata consolidata adeguatamente nella documentazione principale.

Da questo momento deve essere considerata parte degli strumenti diagnostici
personali da preservare durante pulizie, refactoring e preparazione delle release.
