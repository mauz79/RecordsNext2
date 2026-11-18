# RecordsNext 4.0 — continuita e chiusura distribuzione v107

## Baseline operativa — FATTI verificati
- **Sito reale locale:** `E:\fantacalcio\Lega2026`, e storici `E:\fantacalcio\Lega2006`…`Lega2025`.
- **Plugin operativo 4.0:** `E:\FCM\plugin\Mauz_strom2026\RecordsNext`.
- **RC di sviluppo:** `D:\DEV_APPS\RecordsNext4.0_RC_v64` (non destinazione utente).
- **Git worktree:** `D:\DEV_APPS\downloads\RN4_GITHUB_WORK_v63_20261116_174243`.
- **Git remoto:** `https://github.com/mauz79/RecordsNext2.git`, branch `feature/recordsnext4-v67-rc-gui-migrated`, commit pre-release `a16f21ffdd045ff88ad26317a3b594737783c60c`, pulito alla verifica utente.
- **Produzione:** GUI RecordsNext4 operativa, 87/87 record generati, 21/21 siti pubblicati con cutoff, 5.417 JS validi/pubblicati. HTML Top & Flop / Archivio Calciatori e skin presenti.
- **Rimozione 3.2:** v106 ha restituito `RN4_V106_3_2_ELIMINATA`, con recupero stimato 5.757.930.755 byte e mantenimento installazione 4.0 e 21 siti. Non ripetere.
- **Identita':** 2.841 approvazioni mostrate dalla GUI, 0 codici da decidere, 349 proposte non esatte in sospeso. Higuain 8 stagioni verificato da v98; NON certificare tutte le altre carriere. Il CSV v98 dei candidati non e' un elenco di fusioni autorizzate.
- **Due JS oltre 1.500.000 byte:** di altro plugin, accettati dall'utente; non modificarli. RecordsNext mantiene limite 1.500.000 byte propri.

## Contratto release v107
- Costruire `.exe` pubblico con Inno Setup 7 usando **solo** JAR compilato dal repository, JS/HTML/CSS statici tracciati Git, visualizzatori statici controllati e UCanAccess 2.0.9.5.
- Escludere assolutamente `config`, `data`, database, approvazioni calciatori, output generati, script personali e file FCA/FCM dall'installer pubblico.
- Maven `pom.xml` passa a versione `4.0.0` e ricompila con Java 21+.
- Primo utilizzo: GUI crea database e file processing base; l'utente configura stagioni, competizioni, sito e dati di lega.
- Aggiornamento da una 4.0: sovrascrivere solo programma e statici, preservare `config/` e `data/` (backup consigliato).
- Non sovrascrivere automaticamente una 3.2: schema e configurazioni vanno migrati con una procedura dedicata.

## Comandi Windows (uno per volta)
1. Estrarre `RN4_RELEASE_INSTALLER_v107.zip` in `D:\DEV_APPS\downloads`.
2. `python RN4_Crea-Installer-v107.py preflight` con percorso assoluto sotto `tools/`.
3. `python RN4_Crea-Installer-v107.py build`: patch controllata versione POM, copia 14 artefatti Git, `mvnw.cmd -B -ntp clean test package`, payload privato escluso, compilazione Inno, manifest SHA256. Risultato `D:\DEV_APPS\downloads\RecordsNext_4.0.0_SETUP.exe`.
4. `python RN4_Crea-Installer-v107.py smoke`: collaudo Windows automatico in directory temporanea (installazione pulita, upgrade con sentinelle, disinstallazione che conserva i dati).
5. `python RN4_Crea-Installer-v107.py publish`: Git commit e push solo dopo build superata. Rapporto `RN4_ESITO_RELEASE_v107_*.zip` in downloads.

## Non ancora certificato
- Questo pacchetto di sorgenti non equivale a `.exe` compilato e installato Windows. Il build/installer va provato dall'utente.
- Non tutte le 328 carriere candidate del CSV v98 sono approvate/unificate. La diagnostica v107 e' **sola lettura**, fornisce un conteggio sul registro attuale e non opera fusioni.
- La pubblicazione locale non prova la sincronizzazione FTP del sito remoto (fuori ambito release).

## Regole permanenti
Non chiedere file gia' allegati nella catena v82–v106. Non rifare audit sullo storico 3.2 consolidato. Tenere gli allegati e rapporti in `D:\DEV_APPS\downloads`. Nessun URL con source=chatgpt o utm_source=chatgpt. Dopo ogni modifica aggiornare Bibbia/generatore/changelog/diario/decisioni/checkpoint/Git.
