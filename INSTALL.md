# Installazione RecordsNext 3.1

## Requisiti

- Windows
- Java 21 o superiore
- Fantacalcio Manager
- file FCM/FCA delle stagioni da gestire

UCanAccess 2.0.9.5 e le sue dipendenze sono inclusi nel setup.

## Installazione pulita

1. Scaricare `RecordsNext_3.1.0_SETUP.exe`.
2. Fare doppio click sul setup.
3. Scegliere la cartella di destinazione.
4. Convenzione consigliata: `<cartella FCM>\plugin\RecordsNext`.
5. Terminare il wizard e, se desiderato, avviare RecordsNext.

La pagina di scelta della cartella viene sempre mostrata.

Il setup:

- installa programma, runtime e visualizzatori;
- installa l'archivio calendari DataA 1991-2026;
- crea le directory dati vuote necessarie;
- non contiene database personali;
- non contiene FCM/FCA;
- non modifica né pubblica automaticamente alcun sito FCM.

La 3.1 viene distribuita come **installazione pulita**. Non è previsto un pacchetto UPDATE pubblico.

## Primo avvio

Aprire `RecordsNext.bat`.

In **Configurazione stagioni**:

- lasciare vuoto il nome lega se si vuole farlo ricavare dal primo FCM;
- aggiungere una stagione Gestita indicando FCM e FCA;
- configurare il sito locale solo se si desidera pubblicare quella stagione;
- configurare il sito online se servono link web ai tabellini.

## Calendari

RecordsNext include:

`data\calendars\DataA-1991.js` ... `DataA-2026.js`

Se in futuro manca un calendario per una stagione e il relativo sito locale contiene `js\DataA.js`, RecordsNext prova a copiarlo automaticamente nell'archivio interno.

## Pubblicazione

**Pubblica nel sito della stagione corrente al termine**  
Uso normale: aggiorna soltanto il sito corrente.

**Pubblica i siti delle stagioni selezionate**  
Uso straordinario: riallinea i siti storici selezionati. Ogni target riceve soltanto lo storico fino alla propria stagione.
