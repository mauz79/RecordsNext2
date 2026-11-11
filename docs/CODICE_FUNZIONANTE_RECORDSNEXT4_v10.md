# RecordsNext 4.0 - Codice funzionante / bibbia incrementale v10

Data: 2026-11-01. Revisione decisioni dopo 3B2, test sintetico formula; non release.
Le bibbie 3.2 e 4.0 v3--v9 restano intatte.

## Provenienza

- Branch: `feature/recordsnext4-players`
- Commit HEAD: `f9bf30323d9204d70e91e242bddfc72c2060ecf9`
- SHA-256 bibbia base 3.2: `91ecb0006d6d1c6fda501c7243ba65ec2f0625c40e93394079238eac558a7da3`
- SHA-256 bibbia precedente v9: `20ce57e9bc3cff6b90c180f110ab1bb27269a6b97391a7202932761666d8a046`
- Fase 3B1 Windows: 7 test, 21 stagioni, 22.265 tabellini, 552.025 Formazione, hash rapporti verificati.
- Fase 3B2 Windows: 8 test, 180 configurazioni Competizione, 22.265 tabellini; SHA sandbox invariato.
- Formula di media individuale definita, test solo sintetico; nessuna scrittura database.

## Nuovi file integri (testo integrale e SHA-256)

### `docs/PIANO_RECORDSNEXT4_GIOCATORI_v11.md`

SHA256: `371489107f5f3e3e88f5881686f6a9a211bd337ac2efa32aae2eb57ec44be943`

~~~~markdown
# RecordsNext 4.0 - Piano operativo v11

Data: 2026-11-01. Base produttiva 3.2.0 immutata. Branch `feature/recordsnext4-players`; commit di riferimento `f9bf30323d9204d70e91e242bddfc72c2060ecf9`.

## Fasi completate e evidenze

- Fase 0: identita e fonti; eccezioni sui codici storici ancora aperte.
- Fase 1: estrattore FCA sperimentale; output Windows verificato, 7 test. Non integrato nel Java.
- Fase 2: estrattore FCM fantasy sperimentale; output Windows verificato, 8 test. Non integrato nel Java.
- Fase 3A: audit stati `Formazione`, output Windows verificato, 9 test.
- Fase 3B1: audit `Tabellino.LISTA` + `Formazione`, output Windows `RecordsNext4_Fase3B1_OUTPUT_v1_20261101_001744_607896.zip` verificato: 7 test, 21 stagioni, 22.265 tabellini, 552.025 righe `Formazione`, tre SHA-256 dei report corretti, sandbox invariato, bibbia v8 generata. Non equivale a prova definitiva di voto conteggiato.
- Fase 3B2: censimento osservazionale delle regole `Competizione` per stagione e competizione, legame `Incontro.IDGIRONE -> Girone.IDCOMPETIZIONE`, moduli ammessi e tabellini osservati. Strumento prototipo con 8 test locali; lo ZIP Windows deve ancora certificarne l'esecuzione sul PC.

## Passaggi successivi

- Fase 3B3: confrontare configurazioni di competizione, tabellini reali, RU, sostituzioni, eventuali voti speciali e regole per ruolo. Stabilire cosa significa realmente `conteggiato`, `disponibile`, `non sfruttato`, `recuperabile`; distinguere prove osservate, inferenze e casi non simulabili.
- Fase 3C: solo dopo approvazione della semantica: prototipo per metriche individuali fantasy (panchina non impiegata, fuori formazione, sostituzioni, eventuali controfattuali), test su casi reali verificati.
- Fase 4: integrare fonti canoniche in SQLite/Java, esportazione JS stagionale/sito, HTML e GUI. Tutte le componenti richieste fanno parte del traguardo 4.0.0.
- Fase 5: regressioni, documentazione, commit, release 4.0.0.

## Vincoli costanti

1. Prestazione FCA unica per giocatore storico, stagione, giornata: non duplicare se usata in competizioni fantasy diverse.
2. `GiocatoreA.ID` e annuale; `CodiceGazza` contiene il codice storico nella FCA. Le collisioni storiche non sono risolte automaticamente.
3. I codici speciali 0/-1 in `Tabellino.LISTA` non sono calciatori.
4. `ENTRATO`, `ENTRATORISERVA` e posizione nei primi undici slot non bastano, isolatamente, a certificare un voto conteggiato.
5. Il regolamento va ricavato per stagione e competizione, senza assumere tre sostituzioni ovunque. Un archivio FCM e uno snapshot delle impostazioni, non prova di eventuali variazioni avvenute durante la stagione.
6. Conservare integralmente le analisi RU personali della 3.2; non inserirle nei record pubblici senza decisione espressa.
7. Tabellini non collegati, incontri con squadra 0 e riposi: casistiche distinte, nessuna cancellazione.
8. Nessuna scrittura nel DB operativo/sandbox, nessuna modifica a Java, pipeline, siti, GUI, manifest o branch principale durante gli audit.
9. I file nuovi vanno in `D:\DEV_APPS\RecordsNext4.0`, i risultati in `D:\DEV_APPS\downloads`, con nomi versionati.
10. Ogni fase mantiene piano, registro decisioni, diario, specifiche, test e bibbia incrementale con codice integrale e SHA-256. Le versioni pregresse restano intatte.


## Aggiornamento dopo verifica Windows 3B2 e decisione sul record individuale

- 3B2 completata anche su Windows (`ESITO: OK`, 8/8 test): audit documentale delle regole. Non sono state apportate modifiche al database o al motore Java.
- Priorita della fase successiva: **voti non utilizzati dal singolo calciatore**. Niente simulazione del punteggio di fantasquadra e niente ottimizzazione delle sostituzioni.
- Dati minimi da raccogliere per un calciatore: codice storico, ID locale FCA, stagione, fantasquadra, partita e competizione fantasy, giornata reale, indicatore di effettivo utilizzo nei primi 11 del tabellino, presenza di voto reale valido, valore sorgente del voto, riferimento al tabellino.
- Metrica stabilita: somma dei soli voti reali validi nelle esclusioni / **tutte le esclusioni**; conservare separati i due conteggi, non dividerli fra loro.
- Implementazione successiva: un campione reale verificato a mano, poi test automatici sul campione, quindi calcolo storico. Registrare i casi ambigui anziche correggerli arbitrariamente.
- Il campo "valore massimo" menzionato dall'utente rimane **aperto** (nessun cap numerico assunto).
- Continuano a valere l'identita per `CodiceGazza` con verifica collisioni, l'ID FCA locale, l'assenza di duplicazioni delle prestazioni reali e la conservazione dei record precedenti.
~~~~

### `docs/REGISTRO_DECISIONI_RECORDSNEXT4_v10.md`

SHA256: `80f4e73a764ed8423d102fb031cd697256fa4ec605c25c1d2c8f213b2d7853ea`

~~~~markdown
# RecordsNext 4.0 - Registro decisioni v10

Data: 2026-11-01. Registro additivo: D-001--D-053 restano documentate nelle versioni v2--v8; i relativi stati non cambiano tacitamente.

| ID | Stato | Decisione / evidenza |
|---|---|---|
| D-054 | CONFERMATA: verifica Windows | ZIP 3B1 `RecordsNext4_Fase3B1_OUTPUT_v1_20261101_001744_607896.zip`: stato OK, sette test superati, 552.025 righe `Formazione`, 22.265 tabellini, tre SHA-256 dati verificati, SHA sandbox invariato. Bibbia v8 generata; il runner dichiara nessun commit/deploy. |
| D-055 | CONFERMATA: osservazione FCM | La catena per individuare il regolamento e `Incontro.IDGIRONE -> Girone.ID -> Girone.IDCOMPETIZIONE -> Competizione.ID` **della stessa stagione**. `Competizione.ID` non costituisce da solo identita storica. |
| D-056 | CONFERMATA: fonte | `Competizione` registra anche `NUMEROSOSTITUZIONI`, `NUMERORISUFFICIO`, voti d'ufficio, limiti panchina, ruoli sostituibili, regole speciali; `ModuloAmmesso` collega i moduli alla competizione. Nessun valore e convertito in punteggio recuperato. |
| D-057 | CONFERMATA: osservazione locale 3B2 | Sono presenti 180 righe `Competizione` su 21 stagioni. Nel 2019/20 tutte le 12 riportano `NUMEROSOSTITUZIONI=5` e `NUMERORISUFFICIO=3`; nel 2007/08 le tre riportano 3, 8, 11 sostituzioni e 0, 99, 99 RU. Valori non normalizzati arbitrariamente. |
| D-058 | CONFERMATA: separazione | Nel campione SQLite ci sono 168 tabellini senza incontro, 23 collegati a incontri con una squadra zero: non dichiararli automaticamente anomalie o riposi. I controlli si svolgono separatamente. |
| D-059 | APERTA | Interpretazione delle configurazioni elevate (8, 11, RU 99), possibili convenzioni per assenza di limite, rapporto con tabellini e modifiche al regolamento durante la stagione: da verificare sul modello FCM e su esempi reali. |
| D-060 | APERTA | Definizione rigorosa di prestazione conteggiata, prestazione disponibile in panchina e punti effettivamente recuperabili, per competizione, stagione e regole del ruolo. Niente calcoli prima dell'approvazione. |
| D-061 | IMPLEMENTATA: prototipo di sola lettura | Audit 3B2 con dati raw completi delle configurazioni, aggregati per competizione, moduli ammessi e verifiche integrita; 8 test locali superati. Certificazione Windows ancora da eseguire. |

Nota: non sono state prese nuove decisioni sui pesi dei record o sulle formule per i punti non sfruttati.


## Integrazione successiva a 3B2: metrica individuale (2026-11-01)

| ID | Stato | Decisione / motivazione |
|---|---|---|
| D-062 | CONFERMATA: verifica Windows 3B2 | ZIP `RecordsNext4_Fase3B2_OUTPUT_v1_20261101_002713_328984.zip`, stato OK; 8 test superati, 21 stagioni, 180 configurazioni, 13.072 incontri e 22.265 tabellini. SHA-256 del sandbox prima e dopo identico: `57923f4e75986c2b81533e4beef15b9133635258d7de1d36827a1bdbe576671b`. Bibbia v9 generata; nessun commit/deploy dichiarato. |
| D-063 | APPROVATA DALL'UTENTE: obiettivo | Il voto sprecato e un indicatore riferito a **uno specifico calciatore** lasciato fuori da una fantasquadra, non il vantaggio teorico che la fantasquadra avrebbe potuto ottenere sostituendo altri giocatori. Nessuna simulazione delle sostituzioni necessaria per questa metrica. |
| D-064 | APPROVATA DALL'UTENTE: formula | `mediaVotoSprecato = somma(voti validi del calciatore nelle occasioni in cui e rimasto fuori) / numero totale di partite in cui e stato lasciato fuori`. Le esclusioni senza prestazione utile contano nel denominatore e zero nel numeratore. Esempio: 23 esclusioni, 3 voti pari a 6: `18/23 = 0,782608...`. Non usare come media primaria `18/3 = 6`. |
| D-065 | CONFERMATA: RU e fonti 3.2 | La Riserva d'Ufficio e un voto regolamentare assegnato per un posto scoperto quando le sostituzioni ordinarie non bastano. Non e un calciatore ne un voto del calciatore lasciato fuori; non entra nella somma dei suoi voti sprecati. Preservare il sistema RU e gli strumenti personali esistenti. Fonti: `docs/ANALISI_PERSONALE_RU.md`, `docs/MODELLO_DATI_RECORDSNEXT2.md` e `tools/Report_RU_Storico_Completo_v8_4.ps1` (glossario). |
| D-066 | APPROVATA DALL'UTENTE: sostituzioni | Non esiste una regola generale di tre sostituzioni: si osservano anche 5 e, per recuperi a distanza di mesi e configurazioni eccezionali, anche 11. Mantenere il valore FCM della competizione senza normalizzarlo. Per la media individuale non simulare sostituzioni alternative. |
| D-067 | DA VERIFICARE PRIMA DEL CALCOLO | Stabilire con esempi reali il significato operativo di "lasciato fuori" (non nei primi 11 del tabellino finale, riserva non utilizzata, non schierato), il tipo di voto da sommare (voto Gazzetta base vs fantavoto totale), il trattamento di SV e voti speciali e il perimetro delle sole partite valide. Non inventare questi criteri. |
| D-068 | DA VERIFICARE PRIMA DEI RANKING | Evitare conteggi erronei di una stessa prestazione reale se ci sono piu competizioni fantasy nella stessa giornata. Conservare il dettaglio delle partite fantasy ed esplicitare la chiave di aggregazione del record personale. |
| D-069 | APERTA | L'utente ha menzionato un "valore massimo" senza definirne numericamente l'eventuale soglia o se si riferisca al massimo voto sprecato. Non fissare cap o limiti arbitrari. |
| D-070 | PROCEDURA | Comandi pronti per copia-incolla PowerShell 5.1, senza blocchi `else` separabili; file sempre versionati; ogni fase aggiorna piano, registro, diario, codice verificato e bibbia con SHA-256. |

### Nota di compatibilita

Le D-060 e D-061 riguardano anche un **possibile** studio dei punti recuperabili della fantasquadra. Per il nuovo record individuale quell'analisi non costituisce un prerequisito: questa decisione ne restringe il perimetro, senza cancellare la cronologia.
~~~~

### `docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v10.md`

SHA256: `7bc84eb9f93f0fc4799cf5f58c21b19c36b6954c5dcee9190e09e29db9dc2c57`

~~~~markdown
# RecordsNext 4.0 - Diario implementazione v10

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


## Verifica dell'output Fase 3B2 sul PC e decisione operativa

- Esaminato ZIP Windows `RecordsNext4_Fase3B2_OUTPUT_v1_20261101_002713_328984.zip`.
- `MANIFEST_FASE3B2_v1.json`: `stato=OK`, branch `feature/recordsnext4-players`, base `f9bf30323d9204d70e91e242bddfc72c2060ecf9`; stessa impronta SHA-256 del sandbox prima/dopo.
- `LOG_TEST_REGOLE_v1.txt`: otto test superati. `LOG_AUDIT_REGOLE_v1.txt`: 21 stagioni, 180 competizioni, 13.072 incontri, 22.265 tabellini; 168 riferimenti senza incontro rimangono non classificati.
- Riletto `docs/ANALISI_PERSONALE_RU.md` e il glossario di `tools/Report_RU_Storico_Completo_v8_4.ps1`: RU = voto assegnato d'ufficio a copertura di una mancanza. Non associare una RU a un giocatore storico reale.
- L'utente ha approvato la metrica **individuale**, non il controfattuale di squadra: 23 occasioni fuori, 3 gare con voto 6 -> 18/23. Nessun calcolo effettivo sui giocatori e nessun cap implementati in questa revisione.
- Creato test unitario della **sola formula** su dati sintetici. Prima della successiva implementazione su SQLite occorre verificare su esempi reali quali righe costituiscano esclusioni e quale voto sia valido.
- Questa revisione installa soltanto documentazione, un test di formula, il generatore v10 e un runner conservativo. Non modifica Java, GUI, pipeline, archivi 3.2 o siti.
~~~~

### `docs/SPECIFICA_METRICA_VOTO_SPRECATO_GIOCATORE_v1.md`

SHA256: `6286923844c1f246f84fd7c41774e319fa8041147be577a4c30d4144e4900038`

~~~~markdown
# RecordsNext 4.0 - Specifica metrica voto sprecato del calciatore v1

Data: 2026-11-01. Fonte della formula: decisione esplicita dell'utente, dopo il completamento dell'audit 3B2.

## Oggetto

Il record e del **calciatore**, osservato mentre appartiene a una fantasquadra. Non e una ricostruzione del punteggio alternativo della squadra, non richiede di rigiocare sostituzioni, ruoli o moduli.

## Formula approvata

Per ogni calciatore e per il perimetro scelto (stagione, fantasquadra, competizione, storico):

- `E`: numero di occasioni/partite fantasy valide nelle quali il calciatore e stato lasciato fuori;
- `V`: numero di tali occasioni nelle quali il calciatore ha una **prestazione reale con voto valido**;
- `S`: somma dei valori di voto validi riferiti a quelle `V` occasioni.
- `M = S / E`, se `E > 0`. Se `E == 0`, la media e **non definita**, non zero.

La metrica e **M**, non `S / V`. Mostrare facoltativamente `S / V` con un'etichetta diversa, per non confondere le medie. Le `E - V` altre occasioni non producono un voto fittizio ma non spariscono dal denominatore.

Esempio approvato: E=23, V=3, voti 6+6+6, S=18, M=18/23 circa 0,783. La media delle prestazioni valide e 6,00, ma NON coincide con M.

La parola "massimo" dell'utente resta da chiarire: non fissare un cap numerico; mantenere eventuale record del massimo separato dalla media finche non specificato.

## Riserve d'ufficio

Le RU sono voti sostitutivi assegnati d'ufficio al tabellino, non calciatori reali. PU, DU, CU e AU sono categorie storiche del fenomeno. Non generano alcun voto sprecato del calciatore e non devono identificare un `GiocatoreA`.

## Relazioni da usare

- Fonte voto/presenza: dati FCA per giocatore e giornata, sorgente voti Gazzetta della lega.
- Identificativo FCA `GiocatoreA.ID`: solo locale alla stagione; collegamento per `Formazione.IDGioc` e `GiocaIn.IDGiocatore`.
- Identificativo storico: `GiocatoreA.CodiceGazza`, da controllare per collisioni di anagrafiche. Nessuna aggregazione cross-season per ID locale.
- Fonte esclusione: FCM `Formazione` e collocazione in `Tabellino.LISTA`; presenza nei primi 11 slot e indizio di utilizzo, **non prova sufficiente isolata** nei tabellini eccezionali gia documentati.
- Ogni record deve mantenere partita fantasy, fantasquadra, competizione, giornata reale e identificativo del calciatore; non duplicare la prestazione FCA reale per il solo fatto che compare in piu competizioni fantasy.

## Da validare prima dell'estrattore finale

1. Che cosa si considera "lasciato fuori": riserva non entrata, non schierato (`POS<0`), fuori dagli undici finali del tabellino; usare esempi reali e non la sola interpretazione del flag `ENTRATO`.
2. Come si identificano le partite fantasy valide effettivamente disputate; preservare riposi, incontri con squadra 0, tabellini non collegati e partite non giocate.
3. Quale valore numerico sommare: `Punteggio.Voto1` (Gazzetta, voto base) oppure fantavoto `Tot`; conservarli entrambi, non assumere che siano intercambiabili.
4. Come trattare `SV`, voti speciali, partite reali rinviate e giocatori con voto d'ufficio: nessuna trasformazione in voto reale arbitrario.
5. Rischio duplicazioni quando competizioni diverse hanno lo stesso giocatore per la stessa giornata reale: conservare la granularita fantasy, concordare la chiave per classifiche stagionali/all-time.
6. Eventuale denominazione o soglia del "valore massimo" rimane indefinita.

## Test di contratto

`tools/Test-RecordsNext4FormulaVotoSprecato-v1.py` e un test di formula su **dati sintetici**, non una prova della corretta attribuzione dei voti reali in FCM/FCA.

## Non obiettivo

Niente simulazioni di formazione vincente, recuperabilita dei punti, vincoli di sostituzione, calcolo controfattuale del risultato. Le differenze storiche tra 3, 5, 8 o 11 sostituzioni restano registrate, ma non sono un prerequisito della formula individuale.
~~~~

### `docs/VERIFICA_OUTPUT_FASE3B2_RECORDSNEXT4_v1.md`

SHA256: `f63aa225bfd12e62e0038a5095329e6d5977e4a7ae087f6402adcb7808546f66`

~~~~markdown
# RecordsNext 4.0 - Verifica output Windows Fase 3B2 v1

Data: 2026-11-01.

- Sorgente: `RecordsNext4_Fase3B2_OUTPUT_v1_20261101_002713_328984.zip` caricato dall'utente.
- `MANIFEST_FASE3B2_v1.json`: stato `OK`; nessun commit/deploy dichiarato; branch `feature/recordsnext4-players`; HEAD `f9bf30323d9204d70e91e242bddfc72c2060ecf9`.
- Database sandbox prima e dopo: `57923f4e75986c2b81533e4beef15b9133635258d7de1d36827a1bdbe576671b`.
- Log test: 8/8 test superati; log audit: 21 stagioni, 180 competizioni, 13.072 incontri e 22.265 tabellini.
- Tabellini senza incontro corrispondente: 168; tabellini su incontri con squadra 0: 23. Non associare automaticamente questi fenomeni a errori o riposi.
- Bibbia v9 presente nello ZIP. Campi regolamentari conservati, tra cui 3, 5, 8, 11 sostituzioni.
- La Fase 3B2 certifica i dati di configurazione **osservati**. Non certifica la regola applicata a ogni evento storico se il regolamento e cambiato in corso di stagione.
- Nessun calcolo dei voti sprecati di giocatori in questa fase.
~~~~

### `tools/Test-RecordsNext4FormulaVotoSprecato-v1.py`

SHA256: `44415e6f88ba13999552a930ba34ea2bbb7b0e2dbd40bc56ea43313debcd96ca`

~~~~python
#!/usr/bin/env python3
"""Tests ONLY the approved arithmetic; it does not classify FCM/FCA events."""
from __future__ import annotations
from decimal import Decimal
import unittest


def media_voto_sprecato(esclusioni: int, voti_validi: list[Decimal]):
    if not isinstance(esclusioni, int) or esclusioni < 0:
        raise ValueError('esclusioni non valido')
    if len(voti_validi) > esclusioni:
        raise ValueError('piu voti che esclusioni')
    if esclusioni == 0:
        return None
    return sum(voti_validi, Decimal('0')) / Decimal(esclusioni)


class ContrattoMetrica(unittest.TestCase):
    def test_esempio_utente(self):
        self.assertEqual(media_voto_sprecato(23, [Decimal('6')]*3), Decimal(18)/Decimal(23))

    def test_zero_voto_ma_esclusioni(self):
        self.assertEqual(media_voto_sprecato(23, []), Decimal('0'))

    def test_nessuna_esclusione(self):
        self.assertIsNone(media_voto_sprecato(0, []))

    def test_non_diluire_solo_sulle_gare_con_voto(self):
        self.assertNotEqual(media_voto_sprecato(23, [Decimal('6')]*3), Decimal('6'))

    def test_validazioni(self):
        with self.assertRaises(ValueError):
            media_voto_sprecato(2, [Decimal('6')]*3)
        with self.assertRaises(ValueError):
            media_voto_sprecato(-1, [])


if __name__ == '__main__':
    unittest.main(verbosity=2)
~~~~

### `tools/Create-RecordsNext4WorkingCodeMd-v10.py`

SHA256: `aff5574d880baf83d51614c8032b4772fcc6117cb6786f41dcbb14b78b1e587c`

~~~~python
#!/usr/bin/env python3
"""Versioned, additive RecordsNext4 bible including complete post-3B2 metric decisions and formula."""
from __future__ import annotations
import argparse
import hashlib
from pathlib import Path
import subprocess
import sys

BASE='docs/CODICE_FUNZIONANTE_RECORDSNEXT2.md'
PREVIOUS='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v9.md'
DEST='docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v10.md'
NEW_FILES=[
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v11.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v10.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v10.md',
    'docs/SPECIFICA_METRICA_VOTO_SPRECATO_GIOCATORE_v1.md',
    'docs/VERIFICA_OUTPUT_FASE3B2_RECORDSNEXT4_v1.md',
    'tools/Test-RecordsNext4FormulaVotoSprecato-v1.py',
    'tools/Create-RecordsNext4WorkingCodeMd-v10.py',
    'tools/Invoke-RecordsNext4Decisioni-v1.py'
]

def sha(path:Path)->str:
    h=hashlib.sha256()
    with path.open('rb') as f:
        for block in iter(lambda:f.read(1024*1024),b''):
            h.update(block)
    return h.hexdigest()


def git(pr:Path,*args:str)->str:
    p=subprocess.run(['git','-C',str(pr),*args],capture_output=True,text=True,encoding='utf8',errors='replace')
    if p.returncode:
        raise RuntimeError('git '+ ' '.join(args) + ': ' +p.stderr.strip())
    return p.stdout.strip()


def main()->int:
    ap=argparse.ArgumentParser()
    ap.add_argument('--project-dir',required=True,type=Path)
    pr=ap.parse_args().project_dir.resolve(strict=True)
    for rel in (BASE,PREVIOUS,*NEW_FILES):
        if not (pr/rel).is_file():
            raise RuntimeError('File richiesto mancante: '+rel)
    lines=[
        '# RecordsNext 4.0 - Codice funzionante / bibbia incrementale v10',
        '', 'Data: 2026-11-01. Revisione decisioni dopo 3B2, test sintetico formula; non release.',
        'Le bibbie 3.2 e 4.0 v3--v9 restano intatte.',
        '', '## Provenienza', '',
        '- Branch: `'+git(pr,'branch','--show-current')+'`',
        '- Commit HEAD: `'+git(pr,'rev-parse','HEAD')+'`',
        '- SHA-256 bibbia base 3.2: `'+sha(pr/BASE)+'`',
        '- SHA-256 bibbia precedente v9: `'+sha(pr/PREVIOUS)+'`',
        '- Fase 3B1 Windows: 7 test, 21 stagioni, 22.265 tabellini, 552.025 Formazione, hash rapporti verificati.',
        '- Fase 3B2 Windows: 8 test, 180 configurazioni Competizione, 22.265 tabellini; SHA sandbox invariato.',
        '- Formula di media individuale definita, test solo sintetico; nessuna scrittura database.',
        '', '## Nuovi file integri (testo integrale e SHA-256)', ''
    ]
    for rel in NEW_FILES:
        path=pr/rel
        lang='python' if rel.endswith('.py') else 'markdown'
        lines.extend([
            '### `'+rel+'`','', 'SHA256: `'+sha(path)+'`', '',
            '~~~~'+lang,path.read_text(encoding='utf-8-sig').rstrip('\n'),'~~~~',''
        ])
    content=('\n'.join(lines).rstrip()+'\n').encode('utf8')
    dest=pr/DEST
    if dest.exists():
        if not dest.is_file() or dest.read_bytes()!=content:
            raise RuntimeError('Bibbia v9 differente; non sovrascrivo: '+str(dest))
        print('BIBBIA v10 gia presente identica:',dest)
    else:
        with dest.open('xb') as f:
            f.write(content)
        print('BIBBIA v10 generata:',dest)
    return 0


if __name__=='__main__':
    try:
        sys.exit(main())
    except Exception as exc:
        print('ERRORE GENERAZIONE BIBBIA:',str(exc),file=sys.stderr,flush=True)
        sys.exit(1)
~~~~

### `tools/Invoke-RecordsNext4Decisioni-v1.py`

SHA256: `cde1a0dfca2ded490261366456ca6e38e8fe2c3c0c5e1ef41a1c07a57f340c32`

~~~~python
#!/usr/bin/env python3
"""Additive documentation revision after 3B2. No database writes, commit or deploy."""
from __future__ import annotations
import argparse
from datetime import datetime
import hashlib
import json
import os
from pathlib import Path
import subprocess
import sys
import traceback
from zipfile import ZipFile, ZIP_DEFLATED

FILES = ['docs/PIANO_RECORDSNEXT4_GIOCATORI_v11.md', 'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v10.md', 'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v10.md', 'docs/SPECIFICA_METRICA_VOTO_SPRECATO_GIOCATORE_v1.md', 'docs/VERIFICA_OUTPUT_FASE3B2_RECORDSNEXT4_v1.md', 'tools/Test-RecordsNext4FormulaVotoSprecato-v1.py', 'tools/Create-RecordsNext4WorkingCodeMd-v10.py', 'tools/Invoke-RecordsNext4Decisioni-v1.py']
REQUIRED = [
    'pom.xml',
    'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v9.md',
    'docs/REGISTRO_DECISIONI_RECORDSNEXT4_v9.md',
    'docs/DIARIO_IMPLEMENTAZIONE_RECORDSNEXT4_v9.md',
    'docs/PIANO_RECORDSNEXT4_GIOCATORI_v10.md',
    'tools/Create-RecordsNext4WorkingCodeMd-v9.py',
]


def sha(path: Path) -> str:
    h = hashlib.sha256()
    with path.open('rb') as stream:
        for chunk in iter(lambda: stream.read(1024*1024), b''):
            h.update(chunk)
    return h.hexdigest()


def git(project: Path, *args: str) -> str:
    p = subprocess.run(['git','-C',str(project),*args],capture_output=True,text=True,encoding='utf8',errors='replace')
    if p.returncode:
        raise RuntimeError('Git fallito: ' + ' '.join(args) + ': ' + p.stderr.strip())
    return p.stdout.strip()


def main() -> int:
    ap=argparse.ArgumentParser()
    ap.add_argument('--work-dir', type=Path, default=Path('D:/DEV_APPS/RecordsNext4.0'))
    ap.add_argument('--output-dir', type=Path, default=Path('D:/DEV_APPS/downloads'))
    args=ap.parse_args()
    work=args.work_dir.resolve(strict=True)
    out=args.output_dir.resolve(strict=True)
    kit=Path(__file__).resolve().parent.parent
    stamp=datetime.now().strftime('%Y%m%d_%H%M%S_%f')
    pack=out/('RecordsNext4_DecisioniGiocatori_OUTPUT_v1_'+stamp+'.zip')
    state={'fase':'decisioni-post-3B2','stato':'ERRORE','installati':[],'bibbia':'v10','nessunDeploy':True,'nessunCommit':True}
    logs=[]
    ok=False
    try:
        for rel in REQUIRED:
            if not (work/rel).is_file():
                raise RuntimeError('File richiesto mancante: '+rel)
        root=Path(git(work,'rev-parse','--show-toplevel')).resolve()
        if os.path.normcase(str(root))!=os.path.normcase(str(work)):
            raise RuntimeError('Non sei nella radice worktree: '+str(root))
        branch=git(work,'branch','--show-current')
        if branch != 'feature/recordsnext4-players':
            raise RuntimeError('Branch imprevisto: '+branch)
        state['branch']=branch
        state['head']=git(work,'rev-parse','HEAD')
        state['gitStatusPrima']=git(work,'status','-sb')
        for rel in FILES:
            src,dst=kit/rel,work/rel
            if not src.is_file():
                raise RuntimeError('File kit assente: '+rel)
            if dst.exists() and (not dst.is_file() or sha(src)!=sha(dst)):
                raise RuntimeError('File differente: non sovrascrivo '+rel)
        for rel in FILES:
            src,dst=kit/rel,work/rel
            if dst.is_file():
                print('GIA PRESENTE IDENTICO:',rel,flush=True)
                continue
            dst.parent.mkdir(parents=True,exist_ok=True)
            with dst.open('xb') as fh: fh.write(src.read_bytes())
            state['installati'].append(rel)
            print('INSTALLATO:',rel,flush=True)
        runenv=dict(os.environ,PYTHONDONTWRITEBYTECODE='1')
        for script,argv in [
            ('tools/Test-RecordsNext4FormulaVotoSprecato-v1.py',[]),
            ('tools/Create-RecordsNext4WorkingCodeMd-v10.py',['--project-dir',str(work)]),
        ]:
            p=subprocess.run([sys.executable,str(work/script),*argv],capture_output=True,text=True,encoding='utf8',errors='replace',env=runenv)
            logs.append((script,p.returncode,p.stdout+p.stderr))
            print(script,'codice:',p.returncode,flush=True)
            if p.returncode:
                raise RuntimeError('Errore script: '+script)
        state['gitStatusDopo']=git(work,'status','-sb')
        state['stato']='OK'
        ok=True
    except Exception:
        logs.append(('ERRORE',1,traceback.format_exc()))
        print('ERRORE:',logs[-1][2],file=sys.stderr,flush=True)
    finally:
        with ZipFile(pack,'x',compression=ZIP_DEFLATED) as z:
            z.writestr('MANIFEST.json',json.dumps(state,indent=2,ensure_ascii=False)+'\n')
            for script,code,txt in logs:
                name=Path(script).name.replace('.py','.txt')
                z.writestr('log/'+name,txt)
            for rel in FILES:
                target=work/rel
                if target.is_file():
                    z.write(target,rel)
            bib=work/'docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v10.md'
            if bib.is_file(): z.write(bib,'docs/'+bib.name)
        print('ESITO:', 'OK' if ok else 'ERRORE',flush=True)
        print('ZIP DA CARICARE:',pack,flush=True)
    return 0 if ok else 1

if __name__=='__main__':
    try:
        sys.exit(main())
    except Exception as e:
        print('ERRORE FATALE:',str(e),file=sys.stderr,flush=True)
        sys.exit(1)
~~~~
