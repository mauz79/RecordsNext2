# Diario implementazione — v114

Situazione utente: v113 installata; le carriere storiche segnalate dalla GUI sono state riconciliate manualmente; 21 siti pubblicati, record 87/87, 5.475 file. Controllo visivo della pagina `calciatori.html` per KLOSE Miroslav evidenzia tre miglioramenti: ruolo assente, massimo/minimo punteggio singolo assente, colonna «Stato punti» superflua.

Modificati con interventi chirurgici: `TopFlopPublicViewsStageExporter.java`, `TopFlopHistoricalViews.java`, `fcmRecordsNextTopFlopViewsDecoder-v2.js`, `fcmRecordsNextPlayerProfile_v101.js`, `fcmRecordsNextCalciatori_v101.js` e i riferimenti alle versioni JS in `calciatori.html`/`top_flop.html`. Le estensioni non alterano le associazioni d'identità, i dati FCM/FCA, né l'aritmetica MODIF=TOT−VOTO. Prima della pubblicazione v114, il vecchio formato rimane leggibile; dopo pubblicazione le nuove card mostrano solo valori documentati.

Test: compilazione Java 21 sul codice di riferimento, Java test `TestCareerV114`, Node test `test_frontend_v114.js`, Python test `test_installer_v114.py`. L'esito dell'installazione reale deve riportare `RN4_V114_INSTALL_OK`, seguito da una sola elaborazione multisito tramite GUI.
