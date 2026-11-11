# RecordsNext 4.0 — Definizioni delle sette metriche TF sospese — proposta v1

Data: 2026-11-06. **Stato: proposta tecnica, da confermare. NON attivare automaticamente TF05/TF06/TF11–TF15.**

## Regole comuni

- Archivio integrale e aggregati nel SQLite locale. Solo classifiche, schede e highlights significativi nei JS; cutoff storico e limite fisico 1.500.000 byte/JS.
- `VOTO1` FCA valido = voto reale; `TOT` FCM = fantavoto **registrato nel tabellino** per giocatore effettivamente COUNTED. Mai ricostruire TOT di un escluso con coefficienti generici.
- Raggruppamenti e ranking per stagione, carriera, ruolo (portieri inclusi), fantasquadra, competizione e famiglia CAMPIONATO/COPPE; evitare confronti fra popolazioni diverse. Ordinare numericamente e mantenere i pari merito. Denominatori sempre visibili.
- Soglie candidate CONFIGURABILI nella GUI; non trattare le stagioni incomplete come stagioni da 38 giornate e non valutare medie con `N=0`.

## TF05 — medie reali

`mediaReale = SUM(VOTO1 valido) / N_R`, una sola riga per calciatore/giornata reale, senza moltiplicare per serie o coppe fantasy. Graduatorie TOP e FLOP. **Proposta:** `N_R >= 5` per stagione, `N_R >= 20` per carriera. Visualizzare N e soglia; massimi/minimi della singola gara restano TF01/TF02 senza soglia.

## TF06 — fantamedie apportate

`fantamediaApportata = SUM(FCM.Tabellino.TOT) / N_A` solo per contesti COUNTED con TOT numerico. **Proposta:** `N_A >= 5` per stagione CAMPIONATO, `N_A >= 3` per stagione COPPE, `N_A >= 20` per graduatoria carriera; se si raggruppa campionato+coppe esporre sempre N per famiglia. Una prestazione reale puo apportare in piu leghe/coppe ed e contato una volta per ciascun incontro fantasy reale. TOP e FLOP separati.

## TF11, TF12, TF13 — quattro misure Incompresi

Unità: occasione di esclusione in un incontro fantasy giocato per una rosa che **possedeva il calciatore in quella giornata**, sulla base della cronologia di tesseramento, transazioni e formazione; non creare E da assenza in `Formazione` senza prova del possesso. Tenere separati gli incontri di campionato/coppa effettivamente distinti.

- `E` = esclusioni certe; `P_V` = esclusioni con voto reale valido; `P_F` = esclusioni con fantavoto attendibile; `S_V` = somma voti su `P_V`; `S_F` = somma fantavoti su `P_F`.
- **TF11:** `S_V / P_V` (media voto sprecato), con `P_V >= 2` e `E >= 3` proposti.
- **TF12:** `S_V / E` (errore pesato voto), con `E >= 3` proposto. Si somma il voto INTERO, non `voto - 6`; esclusioni senza voto restano nel denominatore.
- **TF13:** DUE classifiche, `S_F / P_F` (fantamedia sprecata) e `S_F / E` (errore pesato fantavoto); `P_F >= 2` e `E >= 3` proposti. **Non attivare il ranking finché la disponibilità dei fantavoti degli esclusi non è comprovata e la copertura per il confronto non è omogenea.**
- `E=0`: valori pesati NULL; `P=0`: medie NULL. Assenza di dato ≠ voto zero. In caso di periodi di proprietà incerti escludere dalla classifica e indicare copertura, non fingere E completo. Ricongiungere compravendita solo se non c'e una giornata reale senza possesso (esempio Varela).

## TF14 e TF15 — stagioni eccezionali / sbagliate

Per ogni calciatore e stagione: `mediaStagione = SV_stagione/N_stagione`; `baseline = SUM(VOTO1 valido in TUTTE le altre stagioni) / N_altre`; `delta = mediaStagione - baseline`. La baseline e ponderata per ciascun voto, non e la media aritmetica delle medie stagionali; non usare la stagione esaminata nel riferimento.

**Proposta:** `N_stagione >= 8`, `N_altre >= 20` distribuiti su almeno **due altre stagioni**. TF14 ordina `delta` decrescente e identifica delta **>= +0,75**; TF15 ordina delta crescente e identifica delta **<= -0,75**. Entrambi espongono media, baseline, N, delta e anni di confronto. Una stagione in cui una fantasquadra ha acquistato un giocatore al momento sbagliato va mostrata come CONTESTO fantasy verificato, non confusa con la definizione del record reale.

## Stato e attivazione

TF05/TF06 e TF14/TF15: implementabili dopo conferma delle soglie e confronto con distribuzione reale su campionato/coppe. TF11–TF13: richiedono prima chiusura tecnica della cronologia di possesso e, per TF13, prova delle fonti fantavoto non impiegate. **La GUI attuale deve mantenere sette voci non attivabili** fino a completamento; niente valori dimostrativi pubblicati.
