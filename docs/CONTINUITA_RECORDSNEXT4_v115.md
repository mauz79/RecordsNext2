# CONTINUITA RECORDSNEXT4 v115 - 18 novembre 2026

Situazione: RecordsNext 4.0 installato in `E:\FCM\plugin\Mauz_strom2026\RecordsNext`, vecchia 3.2 eliminata, database e player-identities.json solo locali. 21 siti in `E:\fantacalcio\Lega2006`..`Lega2026`; ultima pubblicazione riuscita v113 (5475 file).

Gli utenti hanno approvato le carriere storiche Taider, Neuton, Mbaye, Reina e le ultime proposte di identita (222->1->0 riferite dalla GUI). Gemelli Oussama e Anouar El Azzouzi sono distinti.

La patch v114 aggiunge ruolo, TOP/FLOP per singola prestazione e rimuove la colonna Stato punti, ma la rigenerazione ha generato `java.lang.NullPointerException` nella fase Top & Flop, prima di pubblicare siti. v115 corregge concretamente `TopFlopPublicViewsStageExporter.packExtremo` su campo nullable `giornataCompetizione`; riprodotto errore mediante harness Java v114, superato con versione corretta.

Pacchetto `RN4_FIX_NULL_V115.zip`: eseguire unicamente `RN4_Correggi-Null-Carriere-v115.py install` dopo chiusura GUI. Script effettua backup, compila Maven, smoke Java bytecode, aggiorna SOLO il JAR plugin e documentazione. Nessuna identita o DB modificati, nessun sito pubblicato.

Dopo `RN4_V115_INSTALL_OK` riaprire plugin con `E:\FCM\plugin\Mauz_strom2026\RecordsNext\RecordsNext.bat`; 21 stagioni + Consolidata + Elabora con pubblicazione multisito. Verificare file validi=pubblicati 21/21 e pagina Klose TOP/FLOP. Non reimportare, non riapprovare. In caso di blocco non reiterare Elabora, riportare intero log.

Installer pubblico v107 precedente alla v109-v115, Git push/release non ancora conclusi. Dopo prova v115 ripreparare installer pubblico e Git senza dati personali.
