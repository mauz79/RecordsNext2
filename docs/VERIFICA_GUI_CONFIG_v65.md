# RecordsNext4 - verifica GUI configurazione v65

Data: 2026-11-16 18:16:52

- RC v64: patch applicata a RecordsNext2Dashboard.actionCard e RecordsNextConfigurationDialog.loadSeasons.
- Card: ogni zona riceve il primo clic, anche gli spaziatori.
- Apertura stagioni: metadati gia conservati in SQLite, senza riaprire 21 FCM con UCanAccess.
- Maven full build e 58 test: passati sul computer Windows.
- Database/FCM/FCA/siti: non modificati. SHA256 FCA 2012/13 verificato prima della patch.
- Collaudo funzionale GUI da confermare manualmente; nessuna pubblicazione reale.
- Bibbia v46 resta il checkpoint precedente; integrare patch in Bibbia finale dopo collaudo.
