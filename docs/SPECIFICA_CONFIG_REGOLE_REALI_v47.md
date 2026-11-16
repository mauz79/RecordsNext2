# Regolamenti della fantamedia reale indipendente - v47

**Non esiste nessun coefficiente implicito.** RecordsNext non ricostruisce i punteggi reali FCA da `FCM.Tabellino.TOT`. Senza il file di regole, o in assenza della regola per una stagione, `RealFantasy` per quella stagione non viene scritto e la fantamedia reale resta `null`.

Il file opzionale si trova nella directory di configurazione locale, `config/topflop-real-rules.json`; NON e' creato o sovrascritto dall'installatore. Il JSON deve avere:

- `schemaVersion`: esattamente `4.0-real-fantasy-rules-v1`;
- `seasons`: oggetto le cui chiavi sono stagioni `YYYY_YYYY`;
- per ogni stagione: `verified:true`, `source` riferimento documentale preciso alla regola validata, `penaltyGoalsIncludedInGoals:true`, `penaltyConcededIncludedInGoalsAgainst:true`;
- `coefficients`: **tutti e soltanto** gli otto coefficienti numerici, anche se zero: `GOLFATTI1`, `ASSIST`, `GOLSUBITI`, `RIGPAR`, `RIGSBA`, `AUTOGOL1`, `AMM`, `ESP`.

**Esempio strutturale NON utilizzabile come regolamento** (numeri illustrativi, non verificati):

```json
{
  "schemaVersion": "4.0-real-fantasy-rules-v1",
  "seasons": {
    "2020_2021": {
      "verified": false,
      "source": "DOCUMENTO_DA_VERIFICARE",
      "penaltyGoalsIncludedInGoals": true,
      "penaltyConcededIncludedInGoalsAgainst": true,
      "coefficients": {"GOLFATTI1": 0, "ASSIST": 0, "GOLSUBITI": 0, "RIGPAR": 0, "RIGSBA": 0, "AUTOGOL1": 0, "AMM": 0, "ESP": 0}
    }
  }
}
```

**L'esempio viene rifiutato** perché `verified:false`. Non impostare true finche' non hai verificato davvero tutti i coefficienti e l'inclusione dei rigori per la stagione. Per partite con `SV`, `SPECIAL` o `NOT_PLAYED` la media non inventa un voto numerico. Per lo storico globale servono tutti i voti validi coperti da regole verificate.
