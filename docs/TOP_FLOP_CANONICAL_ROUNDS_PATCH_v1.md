# RecordsNext 4.0 - Canonical rounds patch v1 (2026-11-06)

## Scope
- Base: original 3.2 Java pipeline and `rn_event`; no changes to pipeline, publisher, CSS, GUI, site or DB.
- Fixed: `TopFlopJdbcArchiveAssembler` previously used FCM `IDGIORNATA` as a competition round; now uses `rn_event.competition_round`, and the original `Giornata.DESC` is retained for display.
- `FantasyFact.fantasyRound` is nullable for a competition explicitly EXCLUDED from the canonical 3.2 mapping; source FCM ID is retained in `sourceFields` but must never be displayed as a round.
- `TopFlop.CompetitionRoundStatus`: `CANONICAL_3_2` or `UNMAPPED_SOURCE_ONLY`.

## Concrete 3.2 data anomaly
- 21 seasons, 559675 total Formazione rows in the updated SQLite (552025 in the older pre-update snapshot).
- 5350 `Formazione` rows lack an `rn_event` match: 5250 Youth League, correctly excluded, and **100 in 2022/23**, 50 Spareggio Serie B and 50 Vera finale di Coppa tra le Coppe. The last two have `mapping_status=ESCLUSA` in 3.2 but Top & Flop policy includes these competitions. We preserve all 100 as source facts with unknown competition ordinal; no fabricated link or round.
- Example 2026/27, encounter 33814: competition round 5, Serie A day 7, source FCM round ID 2056; original description `5a Camp. (1a Andata)`.

## Installation / next work
- Java replacements are only permitted after checking exact source SHA256; installer protects previous versions under `backups/topflop-canonical-rounds-v1`.
- Do not enable Top & Flop in the catalog, GUI or publisher yet. Next: native aggregator/rankings with usage explicitly proven; integration into existing 3.2 exporter, shards, manifest and HTML with skin, not a parallel pipeline.
- This patch makes round provenance explicit, it does not complete Top & Flop 4.0.0.
