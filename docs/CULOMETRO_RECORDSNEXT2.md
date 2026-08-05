# Culometro RecordsNext 2.0

Modulo opzionale, disattivato di default. Genera `fcmRecordsNext_Culometro.js` solo quando `processing.culometro.enabled` e `culometro.json.enabled` sono entrambi veri.

## Principi

- indice 0-100 centrato sulla media storica globale;
- correzione di affidabilita per campioni piccoli;
- un evento primario al 100%, un solo secondario con peso 10%-35%, tag senza peso;
- rarita con moltiplicatore limitato;
- pesi dei componenti vincolati ai range dichiarati;
- etichette completamente modificabili, con preset neutro come default;
- RU considerate solo quando il dataset dimostra decisivita o cambio di esito. La semplice presenza di RU non modifica l'indice.

## Vincoli GUI

- minimumMatches: 10-40;
- kScale: 3.00-6.00;
- secondaryWeight: 0.10-0.35;
- maximumRarityMultiplier: 3.00-6.50;
- peso componente entro allowedRange;
- ultima fascia etichetta obbligatoriamente a 0;
- etichette non vuote, massimo 80 caratteri.

La GUI dovra offrire modalita semplice e avanzata, anteprima etichette e ripristino dei valori predefiniti.


## Preset etichette resettable

Le etichette sono dati di configurazione e non sono fissate nel motore.

Preset disponibili:

- `GOLIARDICO`: base storica del Culometro Plus;
- `NEUTRAL`: terminologia sobria;
- `CUSTOM`: set interamente personalizzato.

Ogni preset può essere modificato. La configurazione conserva separatamente:

- le `bands` attive;
- il flag `customized`;
- la sorgente `resetSource`;
- le copie immutabili in `presetDefaults`.

La futura GUI potrà quindi offrire **Ripristina Goliardico**, **Ripristina Neutro** e **Salva come Personalizzato** senza perdere i testi originali. Il preset goliardico è quello predefinito.
