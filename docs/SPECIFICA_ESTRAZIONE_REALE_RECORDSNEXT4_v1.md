# RecordsNext 4.0 — Specifica Fase 1: archivio prestazioni reali v1

Data: 2026-10-31. Stato: **implementazione sperimentale verificata sulla copia SQLite fornita**; **NON ancora integrata nella pipeline Java, GUI o pubblicazione**. Non e una release 4.0.

## Scopo e limiti

Esportare tutte le informazioni FCA disponibili per stagione e giocatore, comprese le giornate senza voto, senza usare FCM/Formazione come filtro. I giocatori non tesserati da fantasquadre restano inclusi nella modalita completa `--all`. Nessuna attribuzione inventata di presenza in campo o contributo fantasy.

### Identita

- `GiocatoreA.ID`: ID FCA **locale** alla stagione. Identifica le join di `GiocaIn.IDGiocatore`, `Statistiche.ID`, `Storico.IDGiocatore`.
- `GiocatoreA.CODICEGAZZA`: codice storico indicato negli FCA e coerente col codice foto in molti casi. Il campo esatto nell'FCM resta **da verificare** secondo D-003/Q-001.
- La query per codice `100843` non dichiara automaticamente un'identita canonica verificata. Se nome/data nascita differiscono tra stagioni il file contiene `avvertenzeIdentita`, non fonde l'anagrafica.

### Famiglie FCA (otto)

| Tabella | Uso nell'output |
|---|---|
| `GiocatoreA` | Anagrafica stagionale completa, **tutte** le colonne |
| `GiocaIn` | Una riga per `IDGiocatore` e `Giornata`, mantenuta integralmente |
| `Punteggio` | Oggetto originale completo referenziato da `GiocaIn.IDPunteggio` |
| `Statistiche` | Riepilogo **dello snapshot FCA di quella stagione**, non origine di presenza inventata |
| `Storico` | Istantanea di valori annui (inclusi anni precedenti) ripetuta tra FCA: **MAI sommare across snapshot** |
| `SquadraDiA` | Nome della squadra reale da `GiocaIn.IDSquadra` |
| `GiornataDiA` | Stato raw `GIOCATA` e altri dati della giornata, senza equipararlo a presenza |
| `IncontroInA` | Candidati incontro reale per `(giornata, squadra reale)`; 0/1/>1 risolti separatamente |

La riga `GiocaIn` esiste anche per giornate non disputate. `Punteggio.VOTO1=0` e `-1` si mantengono distinti; sono etichette di osservazione, non una regola completa di SV. Conserviamo `VOTO1/2/3` e gli altri valori di punteggio **cosi come presenti** senza selezionare una fonte voti ufficiale e senza ricalcolare il fantavoto.

### Modello dati JSON

- `formato = RecordsNext4.PlayerReal.v1` (per `--code`): `codiceRichiesto`, `avvertenzeIdentita`, `coperturaStagioni`, `giocatoriStagionali`.
- Per ciascun giocatore stagionale: `stagione`, `giocatoreIdFca`, `codiceStoricoSorgente`, `giocatoreAFca`, `statisticheFca[]`, `storicoFcaSnapshot[]`, `giornateReali[]`.
- Per ciascuna giornata: `giornata`, `giornataFca`, `squadraRealeId/nome`, `giocaInFca`, `punteggioFca`, `statoVoto1`, `incontroReale`.
- `incontroReale.stato = COLLEGATO | NON_COLLEGATO | AMBIGUO`: non collegato NON significa errore del file e non si forza il match.
- `--all` crea un file `.jsonl.gz` per stagione con una riga JSON per giocatore e la stessa struttura stagionale; manifest separato contiene conteggi e SHA-256 di output.

Il file `_MANIFEST` ha hash del DB e degli output per ripetibilita. Il database viene aperto come URI `mode=ro` e PRAGMA `query_only=ON`.

## Test sulla copia SQLite dell'utente

- 21 stagioni raw FCA: 2006/07–2026/27.
- Estratti in modalita completa: **16.245 anagrafiche stagionali**, **617.310 righe `GiocaIn`** tutte conservate, **0 IDPunteggio irrisolti** (con riferimento valido nel `Punteggio` stagionale).
- `IncontroInA`: 143.258 righe `GiocaIn` non risolte univocamente con il semplice match su giornata e squadra; **non classificare come dato errato**. Da analizzare struttura e provenienza, non confondere con riposi fantasy `Incontro`/`Tabellino`.
- `100843`: Lautaro, 9 anagrafiche stagionali (2018/19–2026/27), 342 righe `GiocaIn`. Nell'FCA 2025/26 `Statistiche.GolFatti=17`, `Assist=6`, in accordo con le somme degli eventi giornalieri `Punteggio.GolFatti1` e `Punteggio.Assist`. Questo confronto **non** valida ogni schema storico né definisce il campo voto ufficiale.
- `12302`: 3 anagrafiche stagionali, identita incompatibili → `IDENTITA_DA_VERIFICARE`. Nessun calcolo all-time eseguito.
- 7 test automatici standard library superati sulla copia SQLite. Test Windows: da confermare con ZIP utente.

## Limiti noti e prossime fasi

1. Stagione manuale 2005/06 non presente fra le raw SQLite: serviranno un'importazione distinta o utilizzo delle sorgenti FCA originali per includerla; NON e inclusa nei conteggi di questa fase.
2. L'istantanea SQLite non rappresenta necessariamente lo stato piu recente dei file FCA sul disco, specialmente 2026/27.
3. `Storico` non e un archivio assoluto deduplicato: serviranno criteri di priorita per record storici in anni senza FCA originale.
4. La forma JSON v1 e un **archivio di ricerca**: schema canonico SQL, output JS, GUI, ricerca veloce e record sono fasi successive.
5. Non generiamo media voto, presenze ufficiali o punti sprecati senza fonte/regole concordate.
6. Il DB `recordsnext-sandbox.db` e utilizzato in sola lettura. I due programmi possono presentare differenze rispetto al file originale, quindi i confronti sono fondati sulla **copia** fornita.

## Comandi v1

Per un codice su tutte le stagioni:

```powershell
py -3 "D:\DEV_APPS\RecordsNext4.0\tools\Estrai-RecordsNext4Reali-v1.py" --database "D:\DEV_APPS\RecordsNext2.0\data\database\recordsnext-sandbox.db" --output-dir "D:\DEV_APPS\downloads" --code 100843
```

Per tutti i calciatori di una sola stagione:

```powershell
py -3 "D:\DEV_APPS\RecordsNext4.0\tools\Estrai-RecordsNext4Reali-v1.py" --database "D:\DEV_APPS\RecordsNext2.0\data\database\recordsnext-sandbox.db" --output-dir "D:\DEV_APPS\downloads" --all --season 2025_2026
```

Per tutte le stagioni, omettere `--season` con `--all`; **non e necessario** durante il primo collaudo sul PC: sono gia stati testati 21 esporti sul database caricato e richiede piu spazio/tempo per l'esecuzione.
