# RecordsNext 4.0 - Registro decisioni v7

Sono conservate senza modifica le decisioni D-001 - D-036 nei registri precedenti. Nessuna decisione precedente viene considerata implicitamente revocata.

## Fase 2 verificata

| ID | Stato | Decisione / evidenza |
|---|---|---|
| D-037 | CONFERMATA (esito test) | Lo ZIP Windows `RecordsNext4_Fase2_OUTPUT_v1_20261031_214006_892950.zip` riporta 8/8 test riusciti, 6.050 righe 2006/07, 39.850 righe 2025/26, 1.327 partecipazioni fantasy del codice 100843, SHA dei quattro estratti validi, DB SHA prima/dopo uguale. Nessuna integrazione Java. |

## Fase 3A

| ID | Stato | Decisione / evidenza |
|---|---|---|
| D-038 | CONFERMATA (documentazione 3.2) | `Formazione.POS=0` indica gli iniziali; le posizioni positive sono la panchina; `ENTRATORISERVA` e usato nelle diagnostiche RU per contare gli ingressi effettivi. La 3.2 usa anche `ENTRATO <> 0` per alcune statistiche. Non sono sinonimi universali. |
| D-039 | IMPLEMENTATA PROTOTIPO | Audit 3A classifica senza alterare FCM: POS, ENTRATO, ENTRATORISERVA, GIOCATO e presenza di un totale individuale numerico nel Tabellino. Nessuna somma di punti. |
| D-040 | CONFERMATA (osservazione) | La panchina con `TOT` numerico e `ENTRATO=0` e distinta da `POS<0` con `TOT` numerico: non sono la stessa opportunita di scelta. Non sommarle in un singolo record. |
| D-041 | APERTA | Significato operativo dei casi `POS>0, ENTRATO=1, ENTRATORISERVA=0` e `ENTRATORISERVA=1, ENTRATO=0` prima di determinare i giocatori effettivamente conteggiati. |
| D-042 | APERTA | Punteggi sprecati: voto valido, voto non conteggiato, punti recuperabili a regolamento ed esito controfattuale sono metriche diverse; non calcolarle senza regole verificate. |
| D-043 | CONFERMATA (vincolo storico) | Le analisi RU della 3.2 sono diagnostica privata e non una famiglia di record pubblici; i quattro script RU esistenti vanno preservati. |
| D-044 | CONFERMATA (vincolo) | I tabellini non collegati e le giornate di riposo restano in un audit separato; non sono automaticamente errori. |

## Evidenze locali (da certificare con ZIP Windows 3A)

Su SQLite 3.2 ricevuto: 21 stagioni e 552.025 Formazione. Audit Fase 3A locale: 94.992 righe panchina/non entrata/TOT numerico; 38.646 POS negativo/TOT numerico; 20.295 panchina/ENTRATO=1/ENTRATORISERVA=0; 150 titolare/ENTRATO=0/TOT numerico; 43 riserva flag senza entrato. I gruppi possono sovrapporsi quando hanno criteri diversi e NON equivalgono a punti persi.
