# RecordsNext by mauz79 4.0

**Record, statistiche storiche, Top & Flop e carriere dei calciatori per le leghe Fantacalcio Manager (FCM/FCA).**

RecordsNext elabora gli archivi della lega in un database SQLite locale, ricostruisce le statistiche e genera pagine HTML e file JavaScript statici da pubblicare sul sito FCM. Le associazioni storiche tra calciatori, squadre e competizioni sono governate dall'utente, non determinate da fusioni automatiche arbitrarie.

> **Stato del progetto:** 4.0 operativa nell'installazione di sviluppo. Il repository contiene il codice e le risorse; l'installer pubblico e la GitHub Release v4.0.0 devono essere costruiti e verificati sul codice consolidato. Gli archivi personali FCM/FCA e il database della lega **non** fanno parte della distribuzione.

## Funzionalità

| Area | Cosa offre |
| --- | --- |
| **Record classici** | Punteggi, gol, risultati, vittorie, pareggi, sconfitte, classifiche, cartellini, assist, rigori e reti inviolate. |
| **Serie e primati** | Serie positive e negative, risultati consecutivi e clean sheet. |
| **Riserve d'ufficio (RU)** | Impatto, ricorrenza, distribuzione per ruolo, bilancio e rendimento. |
| **Modificatori** | Analisi dei modificatori e del fattore campo, con riconciliazione del saldo FCM. |
| **Soglie e Culometro** | Vittorie e sconfitte al limite, pareggi, eventi favorevoli/sfavorevoli, frequenze e impatto storico. |
| **Top & Flop (TF01-TF20)** | Graduatorie da prestazioni reali e FCM, incluse anomalie stagionali TF14/TF15 e primati dei portieri. |
| **Archivio calciatori** | Ricerca trasversale, ruoli FCA, carriere stagionali, voti, medie, fantamedie, gol, assist e utilizzi nelle fantasquadre. |
| **TOP/FLOP di carriera** | Migliore e peggiore fantavoto FCM conteggiato in una singola gara, con contesto della prestazione. |
| **Identità storiche** | Gestione dalla GUI di alias, codici e date discordanti, carriere spezzate e duplicati di scheda, con approvazioni esplicite. |
| **Pubblicazione multisito** | Asset statici dedicati a ciascun sito, con cutoff storico della relativa stagione. |

### Statistiche trasparenti

- **Voto reale FCA** e contributo alla fantasquadra **FCM** sono fonti distinte e non vengono mescolati.
- Le medie di carriera sono ponderate sulle prestazioni valide; dati mancanti non sono trasformati in zero.
- I saldi FCM sono controllati con l'invariante `MODIF = TOT - VOTO`; bonus e malus sono distinti solo quando ricostruibili.
- **TF14 - Stagione schillaciana** e **TF15 - Stagione sbagliata** confrontano la media voto della stagione con quella delle altre stagioni della carriera. La pagina espone entrambe le medie, lo scostamento, il campione di voti e, dove verificabile, le fantamedie. Le soglie di significatività sono configurabili.
- Il motore conserva nel database locale lo storico completo; la pubblicazione espone solo i record e i dettagli necessari alla consultazione.

## Pagine generate

| Pagina | Funzione |
| --- | --- |
| `RecordsNext/top_flop.html` | Classifiche Top & Flop, periodi assoluti/stagionali, ordinamento e ricerca. |
| `RecordsNext/calciatori.html` | Ricerca e schede carriera, ruoli, estremi TOP/FLOP FCM e dettaglio per fantasquadra. |

Le pagine usano i JavaScript pubblicati nella cartella `js` del **singolo sito**. Ogni file JS RecordsNext deve restare entro **1.500.000 byte**: dataset grandi sono suddivisi in shard e caricati su richiesta.

La pubblicazione multisito rispetta il cutoff: un sito di una stagione passata non deve mostrare risultati di stagioni successive. La procedura crea file **locali**; il trasferimento FTP o la sincronizzazione con l'hosting pubblico rimane separato.

## Requisiti

- Windows; **Java 21** o superiore per eseguire il programma.
- FCM e archivi FCA/FCM della propria lega per importare i dati.
- Per compilare il codice: **JDK 21+**, Maven Wrapper (`mvnw.cmd`).
- Per generare l'installer Windows: **Inno Setup**. Il progetto include le dipendenze UCanAccess 2.0.9.5 per l'accesso agli archivi Microsoft Access.
- Un server HTTP locale (ad esempio Apache/XAMPP) per collaudare le pagine che caricano JS dinamicamente.

## Installazione e primo avvio

Per l'installazione pubblica utilizzare **esclusivamente l'installer di una GitHub Release effettivamente pubblicata e collaudata**. Non usare come release finale un setup di prova precedente alle ultime correzioni.

1. Installare RecordsNext in una directory a scelta e avviare `RecordsNext.bat`.
2. Nella GUI configurare gli archivi FCM/FCA e i percorsi dei siti delle stagioni interessate.
3. Importare i dati, verificare le associazioni storiche di squadre, competizioni e calciatori.
4. Selezionare le stagioni, scegliere la modalità **Consolidata** o **Completa** e premere **Elabora**.
5. Per la pubblicazione multisito usare **Debug e strumenti > Pubblica i siti delle stagioni selezionate**, oppure il flusso di elaborazione con pubblicazione abilitata.
6. Aprire le pagine tramite un server HTTP locale prima di caricarle online.

**Aggiornamento 4.0:** eseguire il setup nella stessa directory dopo aver chiuso la GUI. Le cartelle locali `config/` e `data/` (compreso il database SQLite e il registro delle identità) devono essere preservate. Fare sempre un backup prima di un aggiornamento.

**Migrazione 3.2 > 4.0:** richiede una procedura dedicata; non sovrascrivere una 3.2 con l'installer generico 4.0.

## Identità e carriere storiche

La GUI presenta proposte e casi da verificare, ma **nessuna proposta equivale a un'approvazione automatica**. I nomi, i codici e le date di nascita possono variare negli archivi storici; alcuni casi richiedono distinguere omonimi o doppie schede FCA presenti nella stessa stagione. Le approvazioni sono salvate **solo nell'installazione locale**.

Le modifiche al registro delle identità richiedono una nuova generazione dei JS destinati ai siti. Il buon esito della pipeline non costituisce, da solo, certificazione anagrafica di tutte le carriere.

## Struttura del repository

```text
src/main/java/       Applicazione Java, motore, publisher e GUI
assets/              HTML, CSS, JavaScript di pubblicazione
installer/           Progetto Inno Setup e launcher Windows
runtime/             Dipendenze di esecuzione (dove previste)
docs/                Bibbia tecnica, continuità, changelog e decisioni
tools/               Generatori e strumenti di sviluppo
tests/               Verifiche riproducibili
pom.xml              Progetto Maven
```

Compilazione da PowerShell nella root del repository:

```powershell
.\mvnw.cmd -B -ntp test package
```

La **Bibbia** in `docs/` conserva i riferimenti alle implementazioni e le decisioni consolidate; il **codice sorgente corrente** resta il riferimento operativo per le nuove build.

## Privacy e dati locali

**Non versionare e non distribuire**:

- `data/`, SQLite e archivi FCM/FCA della lega;
- `config/player-identities.json` e altre configurazioni personali;
- percorsi locali, esportazioni temporanee, backup, report contenenti dati dell'utente;
- asset generati per uno specifico sito che non siano sorgenti statici generici del progetto.

Gli esempi presenti nella documentazione descrivono le funzionalità, **non** includono le 21 stagioni della lega di sviluppo come dati redistribuibili.

## Crediti

**RecordsNext by mauz79** - progetto per l'analisi storica delle leghe FCM/FCA.

Repository: [mauz79/RecordsNext2](https://github.com/mauz79/RecordsNext2)
