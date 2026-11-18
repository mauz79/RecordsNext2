# Continuita RecordsNext 4.0 - consolidamento Git v117

**Ambito:** portare il codice effettivamente in esecuzione nella installazione 4.0 dentro il worktree Git, aggiungendo asset TF14/TF15 v116 e README pubblico.

## Fonti da usare

- Java corrente: RC di sviluppo `D:\DEV_APPS\RecordsNext4.0_RC_v64` (patch v113, v114, v115 gia applicate).
- Asset HTML/JS correnti: plugin `E:\FCM\plugin\Mauz_strom2026\RecordsNext` (v114+v116).
- Worktree: `D:\DEV_APPS\downloads\RN4_GITHUB_WORK_v63_20261116_174243`.
- Branch: `feature/recordsnext4-v67-rc-gui-migrated`.
- Ultimo HEAD remoto CERTIFICATO prima del lavoro v117: `a16f21ffdd045ff88ad26317a3b594737783c60c` (non presumere che sia ancora HEAD, verificare).

## Stato funzionale noto

- v106: versione 3.2 rimossa; plugin 4.0 e 21 siti locali conservati.
- Rigenerazione dopo riconciliazioni: 87/87 record, 21 siti, 5475/5475 JS validi e pubblicati.
- v113: GUI carriere spezzate e approvazione di alias FCA con controlli prestazioni.
- v114: carriere con ruolo, TOP/FLOP della singola gara, niente Stato punti.
- v115: fix NullPointerException durante Top & Flop.
- v116: colonne di confronto TF14/TF15 visualizzate dall'utente.
- Registro identita e DB restano privati e locali: non copiare su Git.

## Limitazioni e prossimi gate

- Non confondere il successo della pubblicazione locale con un upload FTP eseguito o con la certificazione completa di tutte le carriere storiche.
- L'eseguibile setup v107 e precedente alle patch 113-116 e non va allegato come installer definitivo.
- Dopo il commit e push del branch, il passo successivo e compilare e collaudare un nuovo setup pubblico con sorgenti v117; solo poi tag e GitHub Release.
- Il worktree puo contenere modifiche gia preparate dalla build v107; stage soltanto percorsi sorgente, asset statici, documentazione, installer e tool elencati nella procedura v117.
- Verificare su Windows l'esito di Maven e `git push`; il presente pacchetto non dichiara commit/push eseguiti.
