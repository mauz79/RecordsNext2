# RecordsNext 4.0 - Diario implementazione v9

Data: 2026-11-01.

## Revisione dello ZIP Windows 3B1

- ZIP `RecordsNext4_Fase3B1_OUTPUT_v1_20261101_001744_607896.zip`: 14 voci; `MANIFEST_FASE3B1_v1.json` con `stato=OK`.
- Log `LOG_TEST_CONTRIBUZIONI_v1.txt`: sette test su Windows, tutti superati.
- SHA-256 dei file `combinazioni.csv`, `campioni.csv`, `sintesi.json` controllati confrontando gli hash dichiarati nel manifest con i byte nello ZIP.
- Invariato lo SHA del database sandbox: `57923f4e75986c2b81533e4beef15b9133635258d7de1d36827a1bdbe576671b`.
- 22.265 tabellini, 552.025 righe Formazione, 237.686 giocatori nei primi undici slot, 22.028 panchinari nei primi undici, 27.319 titolari iniziali fuori dai primi undici; nessuna attribuzione di punti persi.
- `CODICE_FUNZIONANTE_RECORDSNEXT4_v8.md` generato; nessun commit/deploy eseguito dal runner.

## Sviluppo Fase 3B2

- Riletto lo schema raw FCM originale della 3.2: `Competizione`, `Girone`, `Incontro`, `Tabellino`, `Modulo`, `ModuloAmmesso` e analisi RU personali.
- Implementato `Audit-RecordsNext4Regole-v1.py` in modalita SQLite `mode=ro` + `query_only=ON`.
- Il censimento e per coppia stagione/competizione, con mantenimento di tutte le colonne originali `Competizione` e dei moduli ammessi. La tabella di dettaglio non contiene dati personali delle fantasquadre.
- Osservati 21 archivi, 180 configurazioni, 13.072 incontri (compresi quelli programmati/riposi), 22.265 tabellini; 168 senza ID incontro valido e 23 collegati a incontri con una squadra 0.
- Test automatici `Test-RecordsNext4Regole-v1.py`: otto test sintetici e su DB reale, superati in ambiente di prova. Il database sintetico temporaneo viene generato sotto la cartella diagnostica `downloads`, mai in `%TEMP%`.
- Progettati runner versionato v1, specifica e verifica v1, piano v10, decisioni v9 e bibbia v9 con SHA-256 dei nuovi file.
- La verifica esecutiva del nuovo runner su PowerShell 5.1/Windows e ancora da effettuare; non presentarla come completata.

## Esclusioni

Nessuna modifica ai dati, alle impostazioni delle competizioni, ai 4 script RU personali, al Java, al sito o alla GUI. Nessun calcolo di voti recuperabili, nessun commit.
