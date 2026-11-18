# Continuità RecordsNext 4.0 v116

Baseline: plugin installato in `E:\FCM\plugin\Mauz_strom2026\RecordsNext`; 21 siti locali sotto `E:\fantacalcio\Lega2006..Lega2026`. La v115 è la patch del NPE durante la generazione FCM, indipendente dalla presente correzione JS. TF14 e TF15 in `History` disponevano già di `mediaStagione`, `mediaBaseline`, `votiValidiStagione`, `votiValidiBaseline`, `delta`; `stagioni[].fantamediaReale` è opzionale, solo fonte verificata. Correggere SOLO le colonne JS, non rielaborare FCA o fondere identità. I sorgenti Java e l’installer pubblico attendono ancora consolidamento GitHub dopo ultima verifica UI.

Controllo dopo l’aggiornamento: aprire `http://localhost/rn4-produzione/RecordsNext/top_flop.html`, selezionare TF14 o TF15, Periodo Assoluta, Ctrl+F5; verificare «MV stagione», «MV altre stagioni», «Δ MV» e, dove disponibili, colonne FM. Nessun upload FTP automatico.
