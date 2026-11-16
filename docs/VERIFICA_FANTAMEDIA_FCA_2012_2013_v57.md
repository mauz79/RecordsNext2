# RecordsNext4 - Verifica approfondita FMT FCA 2012/2013 (v57)

Audit in sola lettura eseguito il 2026-11-15 21:44:30.

| Verifica | Valore |
|---|---:|
| Voti validi G1 | 255 |
| Voti validi totali | 9936 |
| Statistiche.PRESENZE diverse dal raw | 0 |
| Storico.PRESENZE diverse dal raw | 235 |
| FMT diversa da FANTAMEDIA1 (>0,011) | 180 |
| Storico allineato alle giornate 2-38 con voto G1 | 235 |
| Differenze FMT coerenti con un fantavoto G1 a passi di 0,5 | 180 |
| Differenze FMT NON spiegate dal controllo | 0 |

## Interpretazione
Il confronto fra riepiloghi ufficiali e voti raw verifica se `Statistiche` usa un campione che include G1, mentre `Storico` usa le sole giornate 2-38.
Per ogni giocatore con una presenza aggiuntiva in `Statistiche`, il fantavoto G1 *implicito* e calcolato come `Stat.PRESENZE*Stat.FMT - Storico.PRESENZE*Storico.FANTAMEDIA1`.
La vicinanza ai multipli di 0,5 e un controllo di plausibilita compatibile con arrotondamento delle medie; non equivale al calcolo certificato di bonus/malus.
La preferenza di `Statistiche.FMT` nel 2012/13 e motivabile per la completezza del campione **solo per i giocatori verificati**, non come regola generale per tutte le stagioni.
Non modificare Storico, Statistiche, GiocaIn o Punteggio. Non derivare FMT da `FCM.Tabellino.TOT`.

## Differenze non spiegate (massimo 25)

Nessuna fra quelle con differenza >0,011 (secondo i criteri indicati).

## Esempi di fantavoto implicito per G1

| Calciatore | Voto G1 | FV G1 implicito | Scarto da multiplo di 0,5 |
|---|---:|---:|---:|
| AGAZZI Michael | 6.5 | 4.501 | 0.001 |
| AGLIARDI Federico | 5.5 | 3.492 | 0.008 |
| ALMIRON Sergio Bernardo | 7.0 | 6.98 | 0.02 |
| ALVAREZ Pablo Sebastian | 5.0 | 4.508 | 0.008 |
| ANDUJAR Mariano Gonzalo | 6.0 | 4.021 | 0.021 |
| ANTONINI Luca | 6.0 | 5.998 | 0.002 |
| ANTONSSON Mikael | 5.0 | 4.517 | 0.017 |
| AREVALO RIOS Egidio Raul | 5.5 | 5.502 | 0.002 |
| ARONICA Salvatore | 6.5 | 5.994 | 0.006 |
| ASAMOAH Kwadwo | 7.0 | 7.997 | 0.003 |
| ASTORI Davide | 4.0 | 3.475 | 0.025 |
| AVELAR Danilo Fernando | 5.0 | 4.997 | 0.003 |
| BALZANO Antonio | 5.0 | 4.99 | 0.01 |
| BALZARETTI Federico | 6.0 | 6.004 | 0.004 |
| BARRETO Edgar Osvaldo | 5.5 | 5.026 | 0.026 |
| BARRIENTOS Pablo Cesar | 6.0 | 6.013 | 0.013 |
| BEHRAMI Valon | 6.5 | 6.512 | 0.012 |
| BELFODIL Ishak | 5.0 | 4.993 | 0.007 |

## Limiti

- Il confronto con il fantavoto reale ufficiale Gazzetta richiede la verifica dei coefficienti bonus/malus, dei voti speciali e degli arrotondamenti; i numeri impliciti non sono un sostituto della fonte.
- Nessuna nuova elaborazione o pubblicazione e stata eseguita.
- Dati FCA/FCM, SQLite, JS, schede e record classici non sono stati modificati.
