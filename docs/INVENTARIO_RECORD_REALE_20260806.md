# Inventario record reale RecordsNext 2.0 — 2026-08-06

## Decisioni consolidate

- Ogni record appartiene a una sola famiglia.
- Le serie dei modificatori si configurano nella card Modificatori.
- Ogni modificatore personale o FCM puo abilitare separatamente Massimo, Totale, Media, Utilizzi e Serie.
- Nessuna metrica viene imposta in base al nome del modificatore: decide l’utente.
- Il Fattore Campo resta nella famiglia Modificatori ma deve avere una sezione visiva esplicita.
- Il filtro competizione deve funzionare per tutti i record che possiedono un ambito competitivo.
- Soglie e Fortuna deve mostrare classifiche e record aggregati, non un elenco grezzo di migliaia di eventi.
- Serve una vista trasversale Record di lega: migliori stagionali e assoluti storici.

## Classici previsti

- Maggior punteggio in una partita.
- Minor punteggio in una partita.
- Partita con piu gol regolamentari.
- Maggior scarto regolamentare.
- Media punteggio.
- Somma totale punti.
- Punti classifica.
- Vittorie, pareggi e sconfitte.
- Gol fatti e gol subiti.
- Ammonizioni ed espulsioni.
- Assist e autogol.
- Rigori segnati, sbagliati e parati.
- Clean sheet.

## Serie previste

- Vittorie consecutive.
- Pareggi consecutivi.
- Sconfitte consecutive.
- Serie senza sconfitte.
- Serie senza vittorie.
- Clean sheet consecutivi.
- Serie di ogni modificatore per cui l’utente seleziona la checkbox Serie nella card Modificatori.

## Riserve d’Ufficio previste

- Massimo RU in una partita.
- Partite con e contro RU.
- RU decisive e RU decisive subite.
- Bilanci con e contro RU.
- Medie punti con e contro RU.
- Distribuzione PU, DU, CU e AU.

## Modificatori e Fattore Campo

Per ciascun modificatore personale o standard FCM: Massimo, Totale, Media, Utilizzi e Serie, tutti facoltativi e configurabili.

Fattore Campo:
- decisivo;
- totale bonus ricevuto;
- punti classifica guadagnati;
- punti classifica persi per il fattore campo avversario;
- saldo, da esporre con nome non ambiguo.

## Soglie e Fortuna

La vista deve aggregare per squadra, stagione, competizione e storico assoluto almeno:
- vittorie chirurgiche;
- sconfitte beffa;
- pareggi miracolati e pareggi stretti;
- vittorie mancate per mezzo punto;
- sconfitte per un pelo;
- soglie precise e giusto giusto;
- sprechi;
- eventi favorevoli, sfavorevoli e saldo.

## Record di lega

Vista trasversale, senza nuovo calcolo quando i dati esistono gia:
- migliore per singola stagione;
- migliore assoluto su tutte le stagioni;
- filtro competizione;
- record partita, squadra, serie, modificatori, RU e soglie/fortuna.

## Lacune reali da correggere nel builder/exporter/viewer

- Classici principali ancora mancanti o non esportati.
- Serie pareggi calcolata ma non sempre esportata/visualizzata.
- Serie modificatori ancora hardcoded sul Capitano.
- Fattore Campo presente ma disperso e con denominazioni ambigue.
- Soglie e Fortuna visualizzata come dati grezzi.
- Record di lega non ancora disponibile.
- Coerenza del filtro competizione da verificare su ogni sezione.
