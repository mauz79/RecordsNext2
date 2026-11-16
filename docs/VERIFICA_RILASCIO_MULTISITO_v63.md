# RecordsNext 4.0 - Verifica rilascio multisito v63

Data verifica: 2026-11-16T17:42:43+01:00. Ambiente TEST: `D:\DEV_APPS\RecordsNext4.0_TEST_v37`.

## Evidenze superate

- Installazione **v58c**: Maven e smoke FCA superati nel TEST il 16/11/2026.
- Elaborazione Completa: **87/87** record; **21** stagioni importabili. **339 JS** nello staging, massimo **977.154 byte**.
- FCA 2012/2013: **9.936 voti**, inclusi i **255** della prima giornata recuperata.
- Gate v59: **9 facade compatibili con la 3.2**, copertura **21 stagioni**, **10.492 FMT ufficiali FCA** controllate.
- Gate post-pubblicazione v62: **21 siti**, **3.730 JS**, **1.617 shard**, cutoff storico per ogni sito, manifest/hash collegati all'ultimo staging. Esito **`AUDIT_SANDBOX_OK_NON_RELEASE`**.
- Stagione `2005_2006` manuale e priva di FCM: esclusa dai 21 siti pubblicabili.
- Skin v53b: integrata nelle **copie sandbox 2025/26 e 2026/27**, CSS e bridge JS verificati contro manifest SHA-256, card Top & Flop presente nell'indice.
- Siti operativi `E:` e branch congelato v46: **non modificati dagli audit/installazioni TEST documentati**.

## Rapporti, percorsi e SHA-256

- Gate post-pubblicazione: `D:\DEV_APPS\downloads\RN4_GATE_POSTPUB_v62_20261116_173912.json` SHA256 `b4745d3717e5bd787294165a99cd8228d4f9b781aa50fd756934d096cdcc5825`.
- Manifest sandbox: `D:\DEV_APPS\RecordsNext4.0_TEST_v37\data\publish-sandbox-v52\state.json` SHA256 `acd072f23ca3e1551271683f9b3fbb8845cb0b526b34a634a5b083030fa47e16`.
- Config elaborazione: SHA256 `7043e8ecb9a26bf648419b0b198874a29af81af7a3ee7c35bbdf569d2b1f4812`; `processing.output.publishToSite=True`.
- Config GUI: SHA256 `88588c72e22264a1ea46143b6ed1dbebdb2dcd33090886d5539abde541b46371`; `processing.publish=false`.
- `2025_2026`: `RecordsNext/top_flop.html` SHA256 `bdfcd5ec682820164cc8e40d59f50d2b53c970afb4e153e6918ab9e29b0e3d51`, `recordsnext.html` SHA256 `67d9a8c8de826370408d2fa54d14eb79c02c868b6188b0cf6815c510f157b641`.
- `2026_2027`: `RecordsNext/top_flop.html` SHA256 `bdfcd5ec682820164cc8e40d59f50d2b53c970afb4e153e6918ab9e29b0e3d51`, `recordsnext.html` SHA256 `dc111e7a61d251f21bb425a3d0776cfd876886aebd81652324cedf5c838fcdaf`.

## Limiti espliciti / cose ancora aperte

- **Non certificato nel browser**: resa visiva, interazioni, filtri per competizione, link tabellini, comportamento delle 4 skin e compatibilita' reale con la 3.2. L'audit dei file e' statico.
- **Non eseguita la pubblicazione reale** sui siti `E:`. Prima serve prova visiva e backup/rollback operativo.
- **Attenzione**: `processing.json` mantiene `publishToSite=true` nel log v62, mentre la GUI aveva `processing.publish=false`. Riportare il flag a `false` tramite GUI/configurazione corretta prima di una nuova elaborazione, dopo il collaudo sandbox e verificando le destinazioni.
- Il push GitHub v63, se eseguito, e' un **checkpoint di release candidate**, non una certificazione di rilascio production; GitHub resta da attestare separatamente con il risultato del comando push.

## Sequenza minima per finire

1. Aprire nel browser i due siti sandbox `2026_2027` e `2025_2026` (indice `recordsnext.html`, pagina `RecordsNext/top_flop.html`), controllare caricamento JS, rendering, filtri, link, console browser e almeno due skin. Non usare il sito `E:`.
2. Allineare/chiudere la flag di pubblicazione, mantenendo la sandbox isolata finche' non si promuove la release.
3. Eseguire confronto visualizzatori e compatibilita' output 3.2, backup degli asset operativi e test di ripristino.
4. Aggiornare il checkpoint GitHub e promuovere in modo controllato. **Non dichiarare conclusa la sostituzione 3.2 finche' il passaggio reale non e' avvenuto.**
