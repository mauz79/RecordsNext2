# RecordsNext 4.0 - Registro decisioni v39

## DEC-v63-001 - certificazione sandbox separata dalla produzione
Il gate v62 `AUDIT_SANDBOX_OK_NON_RELEASE` certifica 21 siti, 3730 JS e 1617 shard, non il rendering nel browser o la pubblicazione E:.

## DEC-v63-002 - integrita' dati e stagioni
Confermati 9936 voti FCA 2012/13 e 255 G1; 2005/06 rimane manuale, non elaborabile in assenza di FCM.

## DEC-v63-003 - limite JS e prestazioni
Vincolo rigido 1.500.000 byte per singolo JS. 339 file staging con massimo 977154 byte.

## DEC-v63-004 - GitHub senza alterare v46
Checkpoint su branch `feature/recordsnext4-v63-sandbox-validated`, basato sul branch v46 ma con sorgenti e documentazione correnti; non scrivere su `feature/recordsnext4-v46-review` e non fare tag di release definitiva prima del collaudo browser e della promozione reale.

## DEC-v63-005 - safety dei prossimi comandi
Non avviare una nuova Completa finche' `processing.output.publishToSite` e le destinazioni non sono state ricontrollate.
