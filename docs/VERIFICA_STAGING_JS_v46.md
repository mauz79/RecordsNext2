# Verifica staging e dimensioni JS — RecordsNext 4.0 v46

**Regola vincolante:** ogni file JS fisico destinato al sito deve essere <= 1.500.000 byte. Nessuna eccezione per i file Classici/RU/Matches/Top & Flop. Non alzare il limite se un file e' troppo grande: usare shard ulteriori solo dopo un progetto compatibile verificato.

1. GUI test: modalita **Completa**, generazione JS attiva, **pubblicazione disattivata**.
2. Il passo `SeasonFamilyShardPublisher.prepareFlat` deve precedere il controllo di `TopFlopViewsPublication.validateStaging` per tutti i JS.
3. Verificare nello staging la facade `fcmRecordsNext_Classics.js` e gli shard `fcmRecordsNext_Classics.AAAA_AAAA.js`.
4. Verificare che `fcmRecordsNext_Manifest.js` contenga i nomi degli shard effettivamente generati.
5. Nessun file generato deve superare 1.500.000 byte; non ci devono essere file JS vuoti.
6. Validare i JS Top & Flop e `History` con i dati della copia TEST, inclusi identita' approvate e cutoff stagionale.
7. Solo dopo, collaudare il publish su copie isolate dei siti; **mai** direttamente sui siti reali in questa fase.

Esempio verifica PowerShell (una singola riga):

```powershell
$root='D:\DEV_APPS\RecordsNext4.0_TEST_v37'; Get-ChildItem (Join-Path $root 'data') -Recurse -Filter '*.js' -File | Where-Object { $_.Length -gt 1500000 } | Select-Object FullName,Length
```

Nota: il comando sopra scandisce anche eventuali staging storici non generati dalla v46. Per convalidare **la singola esecuzione**, usare la cartella `records2026_<timestamp>/js` mostrata nel log piuttosto che contare file residui di test precedenti.
