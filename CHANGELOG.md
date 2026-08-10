# Changelog

## RecordsNext 2.0.0 — 2026-08-10

Prima release stabile della nuova linea RecordsNext 2.0.

### Novità principali

- nuova GUI Swing dedicata `RecordsNext by mauz79`;
- configurazione guidata di lega e stagioni;
- supporto stagioni GESTITE e MANUALI;
- associazioni storiche/canoniche di squadre e competizioni;
- lettura FCM/FCA tramite UCanAccess 2.0.9.5;
- pipeline modulare per famiglie di record;
- modalità di elaborazione Consolidata e Completa;
- output JavaScript separati per famiglia;
- pubblicazione verso il sito FCM;
- visualizzatori HTML statici;
- supporto ai link tabellini `.htm`, `.html` e `.php`;
- record di lega;
- Culometro opzionale.

### Famiglie disponibili

- Classici
- Serie
- Riserve d'Ufficio
- Modificatori
- Soglie e Fortuna
- Culometro

### Modificatori

- supporto modificatori standard FCM;
- supporto MODM1PERS, MODM2PERS e MODM3PERS;
- nomi personalizzabili dei modificatori personali;
- selezione granulare delle statistiche;
- supporto serie dei modificatori;
- supporto Fattore Campo.

### Storico

- associazioni canoniche delle squadre;
- associazioni canoniche delle competizioni;
- gestione differenze di denominazione tra stagioni;
- esclusione delle stagioni MANUALI dall'elaborazione FCM;
- ricostruzione completa delle stagioni GESTITE.

### Visualizzatori

- home RecordsNext;
- Classici;
- Serie;
- Riserve d'Ufficio;
- Modificatori;
- Soglie e Fortuna;
- Culometro;
- Record di lega;
- filtri per stagione e competizione;
- nomi competizioni leggibili;
- selezione eventi Culometro nella sola vista Eventi;
- profili `mauzstrom`, `fantablue2`, `neutral`.

### Release e runtime

- versione Maven 2.0.0;
- manifest applicativo 2.0.0;
- launcher `RecordsNext.bat`;
- runtime UCanAccess 2.0.9.5 distribuito con dipendenze e licenze;
- configurazione `league.json` generata automaticamente dalla GUI.

### Verifiche effettuate

- build Maven completato con successo;
- test automatici superati;
- audit contratto checkbox → vista superato;
- audit semantici Classici, Serie, RU, Modificatori, Fattore Campo, Soglie/Fortuna e Culometro superati;
- smoke test GUI e visualizzatori superato;
- lettura FCM dalla distribuzione di prova verificata;
- generazione automatica `config/league.json` verificata.

### Note

Le stringhe interne che citano RecordsNext 1.0.2 negli exporter indicano la provenienza/compatibilità degli archivi normalizzati e non la versione corrente del prodotto.
