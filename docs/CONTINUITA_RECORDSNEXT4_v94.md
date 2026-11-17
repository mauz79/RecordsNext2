# RecordsNext 4.0 - Continuita operativa v94

Data: 18 novembre 2026. Riferimento immediatamente precedente: Bibbia v50, checkpoint Git v93.

## Stato reale verificato

- RC Windows operativa: `D:\DEV_APPS\RecordsNext4.0_RC_v64`. GUI v86 e interfacce v92 installate, compilate e pubblicate **solo nella sandbox**; 21 stagioni, 87 record generati, 482 JS entro 1.500.000 byte, report di parita v88 sulle fonti controllate.
- L'utente ha confermato visivamente il miglioramento dell'interfaccia; il rapporto automatico v92 `DEPLOYED`, 67 test Maven, HTTP 5/5. Non confondere il feedback con certificazione numerica di tutti i record.
- Classici, serie, RU, modificatori, soglie, fortuna e Culometro gia operativi dalla versione 3.2: non ripetere audit generali senza anomalia concreta.
- RC e repository Git sono directory **diverse**. Git: `D:\DEV_APPS\downloads\RN4_GITHUB_WORK_v63_20261116_174243`, branch `feature/recordsnext4-v67-rc-gui-migrated`, origin `https://github.com/mauz79/RecordsNext2.git`, base `3e8d53f80e2f8b9c7d8be7a7f26b8fa47339d921`.
- `RN4_GITHUB_PREFLIGHT_v93.json`: READY, 50 file; `RN4_GITHUB_STAGE_v93.json`: STAGED, 50 file, backup `D:\DEV_APPS\downloads\RN4_BACKUP_GITHUB_v93_20261118_004320`.
- `RN4_GITHUB_MAVEN_v93.txt`: **BUILD FAILURE**: `TopFlopIntegratedV83.java` non trova `TopFlopFullPerformancesV69`, righe 210, 211, 226. Nessun commit e nessun push v93.
- Confronto snapshot RC/Git v85: mancavano **esattamente due** sorgenti Java nel Git pre-v93: `TopFlopFullPerformancesV69.java` e `TopFlopSimeoneIdentityV73.java`. Lo stage v93 non includeva questi due. Nel Git staged v93 vi sono inoltre i nuovi Java v83 e i test; completando con i due mancanti si ricostituisce la superficie Java della RC v92.

## Correzione v94 e controlli

- NON rifare preflight/stage v93: preservare i 50 file staged.
- Script `RN4_Riprendi-Git-v94.py`: controlla identita Git e SHA dei 50 file staged; recupera dalla RC soltanto i due Java mancanti, aggiunge documenti v94 e Bibbia v51, testa Maven nel worktree, effettua commit e solo su richiesta esplicita push non-forzato.
- La Bibbia v51 include integralmente la v50 e il testo completo delle due classi mancanti, documentazione e strumenti v94. Usare il generatore versionato `tools/Genera-Bibbia-RecordsNext-v94.py` per ricostruirla.
- Prima di eseguire v94 i comandi sono `repair`, `commit`, `push`, **uno alla volta**. Se un gate fallisce non eseguire i successivi.
- Qualunque report richiesto va scritto in `D:\DEV_APPS\downloads` e caricato una sola volta. Non richiedere archivi gia inclusi nei checkpoint.

## Vincoli di protezione

Non modificare SQLite, FCA/FCM, configurazioni identita, la sandbox gia collaudata, il sito operativo su E:, o la RC Java durante la ripresa Git. Mai aggiungere tracciamenti negli URL. Non dichiarare Git consolidato finche il commit, il push e l'hash remoto non sono verificati.

## Prossimo punto

Allegare solo `RN4_ESITO_GITHUB_v94_*.zip` dopo push. Registrare gli hash di commit/remote nella continuita successiva e non ripetere il lavoro gia riuscito.
