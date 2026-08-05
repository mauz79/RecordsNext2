# Changelog

## 2.0.0-dev

### Fondazione

- creato progetto separato RecordsNext2.0;
- importata la base funzionante RecordsNext 1.0.2;
- confermato accesso FCM/FCA tramite UCanAccess;
- mantenuti SQLite, mapping canonici, normalizzazione, consolidamento e pubblicazione.

### Architettura modulare

- definite cinque famiglie elaborabili;
- introdotti figli, dipendenze e stati degli output;
- introdotta selezione modulare;
- mantenuta compatibilita temporanea con Classici e RU legacy;
- introdotti planner e preflight;
- collegato config\processing.json alla pipeline.

### Manifest

- introdotto fcmRecordsNext_Manifest.js;
- integrata generazione nella staging;
- integrata validazione;
- integrata pubblicazione con rollback.

### Verifica

- ultimo stato: 22 test superati, BUILD SUCCESS.
