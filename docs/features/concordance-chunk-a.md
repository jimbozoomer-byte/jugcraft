# Concordance Chunk A: usable foundations (contract and handoff)

Status: in progress (units 1 to 4 implemented, not yet compiled in CI); see each unit's record.
Proposal: the owner chose Chunk A ("usable magic") on 10 October 2026, from
[the expansion plan's delivery chunks](../plans/concordance-expansion-2026-10-10/09-claude-delivery-chunks.md) and
[its first-chunk tickets](../plans/concordance-expansion-2026-10-10/12-detailed-first-chunk-tickets.md) (A-00 to A-08).
Owner: @jimbozoomer-byte. AI-assisted implementation with Claude Code; the model is named in the commit trailers.

This page is the chunk's contract (chapter 9 §1 step 7 of the plan) and its technical handoff (chapter 10 §7). Each
unit's player-facing record is linked from the table below.

## Player outcome

A new survival player can discover a first magical practice, compose it without commands, inspect its costs, acquire
a useful loadout, enter an existing lair and qualify for reward through support as well as damage.

## Units

| Unit | Tickets | What it delivers | Record | State |
|---|---|---|---|---|
| 1 | A-00, A-01 | Baseline; one composition service behind the command and the screen | [Spell Composer](arcane-concordance-composer.md) | Implemented; CI not yet run |
| 2 | A-02, A-03 | Bounded screen protocol; the plain composer screen and the GuiLib workspace over one draft | [Spell Composer](arcane-concordance-composer.md) | Implemented; CI not yet run |
| 3 | A-05, A-06 | Encounter contribution (healing, warding, objectives) and the reward lifecycle | [Lair encounter credit](lair-encounter-credit.md) | Implemented; CI not yet run |
| 4 | A-04, A-07 | The encyclopedia's entry and first route; acquisition and presentation | [The encyclopedia's first route](arcane-concordance-first-route.md); [first loadout and acquisition](concordance-first-loadout.md) | Implemented (cast poses to come); CI not yet run |
| 5 | A-08 | Combined fixes and the evidence for the owner's checkpoint | this page | Not started |

## Baseline (A-00)

- Base: `c3b4fcbdf` (main `240d73e1c` with Wayfaring part 1c merged in, as in
  [#315](https://github.com/jimbozoomer-byte/jugcraft/pull/315)). The working tree was clean.
- Framework lock unchanged since [#239](https://github.com/jimbozoomer-byte/jugcraft/pull/239) (verified 5 October, 18
  dependencies); no pin changes in this chunk.
- Baseline checks at the base: `scripts/check_repository.py` and `tools/check_mod_data.py` passed. CI at the base
  failed one known flaky game test (`ArmsVIIIGameTests.javelinStrikesAndComesDown`), fixed by
  [#303](https://github.com/jimbozoomer-byte/jugcraft/pull/303) and carried by #315.
- Open pull requests touching this chunk's seams: #300 (Glacier Hall) and #313 (Cinder Kiln) edit the lair registry
  files; #304 (Yeti King) adds a third boss with the same damage-only reward pattern. Unit 3 keeps the existing
  `took`, `participants` and `reward` signatures so those still compile.
- The plan's status text that had gone stale (feature records' "on branch" lines, WHAT_EXISTS, BOSSES.md, the
  delivery record) was corrected in unit 4, after the merge state was checked again on 11 October. The CHANGELOG's
  stale headings are reconciled in a docs-only pull request of their own (A-D14).

## Interfaces changed

- New common service `concordance/Composing` (compose, show, clear; `Held`), used by `/jugcraft concordance compose`
  and the composer screen. `COMPOSE_RATE_TICKS` moved there from `ConcordanceCommand`.
- `ConcordanceData.revision()`: counts rule loads and replacements.
- New payloads (play phase): `jugcraft:concordance_composer_request` (to the server),
  `jugcraft:concordance_composer_state` and `jugcraft:concordance_composer_result` (to the client).
- New pure model `concordance/compose/Draft`.
- `Compiler.changes` and `Composition.shortName` are now public (unchanged behaviour).
- `JugcraftConfig.setFeatureEnabled` for game tests.
- New `lair/Encounter` (a boss fight's record) and `lair/LairRewards` (rewards paid once, kept for the fallen and the
  away); the bosses' `took`, `participants` and `reward` keep their signatures.
- `ConcordanceEffects.observe`: observers told of each restoration and protection that took.
- `Lairs.onRelit` and `Lairs.relit`: a lair brazier's unlit-to-lit turn.
- New saved data: the player attachment `jugcraft:lair_rewards` and the overworld record `jugcraft:lair_rewards`.
- Unit 4: new payloads `jugcraft:encyclopedia_request` (to the server: a sequence and, optionally, the block looked at)
  and `jugcraft:encyclopedia` (to the client: notices and pages); the key `key.jugcraft.encyclopedia`; the server service
  `guide/encyclopedia/Encyclopedia` with `PageWriter` (the one place a page shows a recipe, a sale or a codex link) and
  the pure page model `Navigator`; the pages `concordance/encyclopedia/FirstLightRoute`, `SpellcraftPage`,
  `InspectPage` and `lair/LairDossiers`.
- `CircleAnchorBlockEntity.status(ServerLevel, Consumer<Component>)` (chat's lines, from the cached report) and
  `LeyPylonBlockEntity.status()`; `TownShops.sales(Item, String)`.
- The Adept's Wand joins the equivalence exclusions; JEI information pages from `tools/acquisition.py`.

## Validation plan

- Server game tests for every A-01 case and the protocol's bounds, revisions and rate.
- A client game test on a dedicated server (so messages are encoded) for typing, an illegal edit, undo, inscribing,
  casting and the screen at other sizes, with screenshots.
- Units 3 and 4 add their own (support-only reward, overheal and spam earn nothing, duplicate defeat, the route page).
- Not available here: a person playing, two independent clients, the narrator heard by a person. These are recorded as
  not run in each record.

## Deferrals

- New delivery types, moving projectiles, unrestricted scripting and raised compiler caps (A-03 says so).
- The `compose` command's tab suggestions still list every word in the rules, learned or not; the screen offers only
  learned words. Changing the command is an owner choice (see Decisions).
- Problems do not carry the position of the word they concern; the screen marks every word a problem names.

## Decisions

| ID | Date | Decision | Status | Affects |
|---|---|---|---|---|
| A-D1 | 10 October 2026 | Chunk A first, before winter/stone or the workshop | Accepted (owner) | All of Chunk A |
| A-D2 | 10 October 2026 | The composer key is unbound until a player binds it; the journal's Compose button is the way in | Proposed | Unit 2 |
| A-D3 | 10 October 2026 | The command's suggestions keep listing every word (a diagnostic path) | Proposed | Unit 1 |
| A-D4 | 11 October 2026 | Support qualifies at 4 healing or 4 warding (one Mend or one Ward); any blow still qualifies; one objective qualifies | Proposed | Unit 3 |
| A-D5 | 11 October 2026 | A boss's experience is shared among everyone paid, now or later; one who died in the fight is paid wherever they are, and one who left the game in it is paid on joining if still away when it falls | Proposed | Unit 3 |
| A-D6 | 11 October 2026 | Only the Concordance's healing and wards are credited (vanilla healing names no healer; crediting it would need a mixin) | Proposed | Unit 3 |
| A-D7 | 11 October 2026 | The encyclopedia's key defaults to I (vanilla binds I only as F3+I, a debug combination that does not press it alone; no pinned mod binds it); a client test compares it with every loaded default | Proposed | Unit 4 |
| A-D8 | 11 October 2026 | The inventory button is the owner's 16×16 scroll sprite, right of the recipe-book button, and on the creative Survival Inventory tab only; it closes the inventory, and Back reopens it | Proposed | Unit 4 |
| A-D9 | 11 October 2026 | Discovery: research, invocations and words named only once met; a lair and its rite once its token is carried; its keeper once met or beaten; no saved "entered" state | Proposed | Unit 4 |
| A-D10 | 11 October 2026 | The first loadout is what survival already makes; the Wayfarer and the other owner armour tiers stay creative-only | Proposed | Unit 4 |
| A-D11 | 11 October 2026 | Trade ways come only from the town's existing shops, for offers on sale today; no shop changes | Proposed | Unit 4 |
| A-D12 | 11 October 2026 | Codex links open an unlocked entry without the codex being carried | Proposed | Unit 4 |
| A-D13 | 11 October 2026 | The route's support spell is `touch struck ward`, added to the codex examples | Proposed | Unit 4 |
| A-D14 | 11 October 2026 | The CHANGELOG's reconciliation (stale "Unmerged" headings, the entries #277 dropped) goes in its own docs-only pull request | Proposed | Unit 4 |
