# RecordsNext 4.0 - continuita operativa v103 (18/11/2026)

## Risultati reali confermati dall'utente
- RC: `D:\DEV_APPS\RecordsNext4.0_RC_v64`. Windows PowerShell 5.1, Python 3.13, Maven Wrapper `mvnw.cmd`.
- Sito locale di produzione: `E:\fantacalcio\Lega2026`. Il deploy v96 ha trasferito 487 file con backup completo e verifica SHA. La GUI ha successivamente elaborato 21 stagioni e pubblicato **485 JS** senza errori.
- Higuain: `RN4_V98_PRODUZIONE_VERIFICATA`, un'unica carriera di 8 stagioni, indice=8, history=8.
- Interfaccia e publisher v92 installati, collaudati nella sandbox e applicati al sito locale. Correzione v101 **non installata**, respinta dal preflight per baseline Java errata. La v102 e' stata costruita sulla pipeline v92 corretta.
- v102 Windows: `RN4_V102_PREFLIGHT_READY`; `RN4_V102_INSTALL_OK file=13`, backup `D:\DEV_APPS\downloads\RN4_BACKUP_FRONTEND_v102_20261118_181943_417712`; `RN4_V102_VERIFY_OK file=13; RC/PRODUZIONE allineate`. Non ripetere installazione, build o generazione.
- v102 cambia solo gli asset della scheda FCM (stati **OK**, **Saldo verificato**, **Nessun apporto**, **Dati insufficienti**) e la registrazione dei due JS v101 nel publisher nativo. Non cambia il motore dei punti, SQLite, il registro identita', i record classici o gli altri plugin.
- La prova visuale dopo v102 della scheda aggiornata non e' stata ancora allegata. Non presentarla come certificata per ogni calciatore.

## Repository e documentazione
- Worktree Git separato: `D:\DEV_APPS\downloads\RN4_GITHUB_WORK_v63_20261116_174243`; origin `https://github.com/mauz79/RecordsNext2.git`; branch `feature/recordsnext4-v67-rc-gui-migrated`.
- Ultimo commit remoto verificato PRIMA di v103: `5c10ec5b729783747c5a8d33f17b5be84f70de74` (v94, 59 file, Maven 66/66 nel worktree).
- La Bibbia installata v102 e' `docs/CODICE_FUNZIONANTE_RECORDSNEXT4_v53.md`, generata/controllata da `tools/Genera-Bibbia-RecordsNext-v102.py`. La presente integrazione Git introduce **v54** con `tools/Genera-Bibbia-RecordsNext-v103.py`, conservando v53 intatta.
- **NON** supporre push v103 avvenuto prima di vedere `RN4_V103_PUSH_VERIFIED` e rapporto remoto. Lo script produce un documento post-push con hash reali.

## Identita storiche: niente approvazioni ripetute
- Nella GUI: **2.841 approvazioni**, **0 codici da decidere**, **nessuna proposta esatta ancora da approvare**, **349 proposte sospese** (screenshot 18/11/2026). Non riaprire la finestra chiedendo di riapprovare le proposte esatte: operazione gia' conclusa.
- Il CSV v98 individua 328 gruppi **candidati**, non 328 errori certi. Le 71 ambiguita' di codice erano gia' state adjudicate nella fase precedente. Nessuna fusione automatica: usare il registro corrente, conferme anagrafiche e approvazione esplicita.

## Regole invarianti e moduli da non riaprire
- FCA: una prestazione per calciatore/giornata, 21 stagioni. FCM: soltanto contributi realmente COUNTED; saldo certo `MODIF = TOT - VOTO`. Bonus/malus separati solo se la riconciliazione per tabellino e' univoca. `null` non e' zero.
- Classici, serie, RU, modificatori, soglie, fortuna e Culometro funzionanti dalla 3.2: preservare, non riaprire audit generici.
- Tutti i JS **RecordsNext** <= 1.500.000 byte. I due JS sopra limite rilevati sul sito appartengono a un altro plugin; l'utente ha confermato che non vanno toccati.
- Renewo e' fuori da questo sviluppo. Non aggiungere mai parametri `source=chatgpt` o `utm_source=chatgpt`.
- Il lanciatore RC stampa una dicitura fissa «pubblicazione automatica DISATTIVATA» anche quando la GUI pubblica. E' un messaggio obsoleto, non prova che la pubblicazione sia disattivata.

## Passo corrente
Consolidare SOLO v102 nel worktree Git gia' allineato a v94. Il pacchetto v103 controlla versione e SHA in RC e sito, copia una whitelist, prova Maven nel **worktree Git**, crea commit e push senza force push. Non modifica sito, database, registro, altre applicazioni o Renewo. Se l'hash remoto e' mutato, si ferma.

Dopo push: controllare le pagine locali in browser se necessario, registrare hash reale nel checkpoint post-push; la produzione locale resta distinta dall'eventuale hosting remoto. L'eventuale audit mirato delle identita' residue e' separato e non blocca il consolidamento di v102.
