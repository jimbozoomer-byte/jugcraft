# Changelog

Every change to Jugcraft, newest first, grouped by pull request. For what each thing does in game, see [docs/TECH_TREE.md](docs/TECH_TREE.md). For a full inventory with APIs and file locations, see [docs/WHAT_EXISTS.md](docs/WHAT_EXISTS.md).

**Testing so far:** everything compiles in CI, and the in-game tests (from #14 on) pass on a headless server. Nothing has been play-tested in a client or with two players on a dedicated server yet.

## Unreleased

No numbered release yet. Entries on feature branches remain proposed until their pull requests merge.


### Unmerged: The Hollow Acre: the lairs and the Last Rites (boss 1, part 1)
- **The Last Rites** open the Hollow Acre:
  - at night, at any Jugcraft headstone in the Overworld, with four lit candles round it and a **Mourning Wreath** (four mourning flowers round a vine) laid on it;
  - ring the **Death Knell** (a gold ingot over an iron nugget over a bone) beside it.
  - The wreath is taken, and a gate of grey mist stands over the grave for 60 seconds: the ringer goes through, and anyone who uses the gate follows, four at most.
  - When a step is missing the knell says which, and nothing is used up.
- **The Hollow Acre**, a pocket dimension of its own: a floating island of black earth under an endless starless night and a vast harvest moon. Players arrive at the graveyard's Lych Gate, then cross a field of black wheat and weathered headstones to the Mown Circle, its arena ringed with soul braziers. At the north end stands a ruined bone chapel, with the Bone Throne at its open side. Vesperine comes in part 2.
- **The lairs' rules**, shared with the Spindle Loft to come:
  - every ritual places the lair fresh into its own instance;
  - nothing in a lair can be built or broken;
  - the lych gate's Grey Mist or `/jugcraft lair leave` takes a player back to exactly where they stood;
  - straying off the island costs 4 health, never below half a heart;
  - dying there keeps everything as **Grave Goods**, handed back on respawning;
  - empty instances close after 30 seconds, and a restart closes all of them.
- Settings: `lairs.instances`, `lairs.party_size`, `lairs.gate_seconds`, `lairs.off_season`. Record: [hollow-acre.md](docs/features/hollow-acre.md).
