# Concordance Chunk B: winter and stone (contract and handoff)

Status: contract written; unit 1 in progress (the Yeti King's B-01 committed, its tests not yet run in CI); the other
units Not started. The base is `d4c07bf56`, the head of Chunk A
([#337](https://github.com/jimbozoomer-byte/jugcraft/pull/337), not merged); main is `b73f51c8a`
([#321](https://github.com/jimbozoomer-byte/jugcraft/pull/321)).
Proposal: the owner chose Chunk B on 13 October 2026 ("Ok lets begin Chunk B"), from
[the expansion plan's delivery chunks](../plans/concordance-expansion-2026-10-10/09-claude-delivery-chunks.md) (section 4,
B1 to B4), with [spellcraft](../plans/concordance-expansion-2026-10-10/02-magic-progression-and-spellcraft.md),
[workshops](../plans/concordance-expansion-2026-10-10/03-workshops-ecology-and-pacts.md) and
[the existing bosses](../plans/concordance-expansion-2026-10-10/04-existing-bosses-and-shared-encounters.md). The same day
the owner put Chunk B's commits on the stacked branch `claude/chunk-b-winter-stone` (on Chunk A) and answered three
decisions (B-D1 to B-D3, under Decisions).
Owner: @jimbozoomer-byte. AI-assisted implementation with Claude Code; the model is named in the commit trailers.

This page is the chunk's contract (chapter 9 §1 step 7 of the plan) and, as the units land, its technical handoff
(chapter 10 §7). Plan chapters are cited as `NN:line`. Code is cited as `file:line` at `d4c07bf56`: Java paths are
under `src/main/java/io/github/jimbozoomer/jugcraft/`, data under `src/main/resources/data/jugcraft/`, tests under
`src/gametest/java/io/github/jimbozoomer/jugcraft/test/`, and `mc263src` is the decompiled 26.3 reference. A fact
marked *(survey)* comes from this chunk's survey reports or the plan's review and was not re-read for this page.

## Player outcome

The plan's outcome is to practise Rime and Strata in ordinary play, use existing Thallite, prepare for cold exploration,
reach a Rime/Strata workshop and fight the Yeti in his accepted hall (09:27, 09:86). Corrected for what is already built
(see the status delta), a survival player can:

- reach the Glacier Hall in an ordinary world, and in a designed world once B-03 confirms it;
- share the Yeti King's reward, or the Cinder Tyrant's, by damage, healing, warding or, against the Yeti King, an
  objective; keep it if they fall or leave the game in the fight; and be paid exactly once, a killing blow included;
- read both keepers' dossiers and JEI's pages for their loot;
- learn Rime and Strata through typed evidence, and cast Rimebind, Brace and Preserve;
- brew Winter Brine at the Alembic Crucible, the winter workshop: a timed Frostward that the Yeti's frost respects, while
  the Yeti Mitten stays the permanent prize;
- make and wear the owner's water sets, foci and bangle as Rime's travel kit, from ordinary materials;
- use Brace and Rimebind against the Yeti if they choose; neither is needed to win.

Already built, and not rebuilt here: the Glacier Hall, the Frost Horn, the Yeti King, the Cinder Kiln, the Kiln Seal and
the Cinder Tyrant (#320); Thallite ore and gear (slices 1 and 2); the Alembic Crucible; Chunk A's encounter credit and
kept rewards.

## Units

Each unit is a reviewable boundary: its own run of commits on `claude/chunk-b-winter-stone`, stacked on #337 until #337
(or a pull request that carries it, see #352 below) merges, with its own record and green checks before the next unit
starts.

| Unit | Tickets | What it delivers | Record (proposed) | State |
|---|---|---|---|---|
| 1 | B-01, B-02 | The Yeti King on Chunk A's encounter credit, kept rewards and exactly-once payment; then the Cinder Tyrant in a separate commit (B-D1); both keepers' dossiers, route step and JEI pages | `lair-encounter-credit.md`, `yeti-king.md`, `cinder-tyrant.md` | In progress: the Yeti King's B-01 committed (CI pending); the Cinder Tyrant's B-01 and B-02 to come |
| 2 | B-03, B-04 | Glacier Hall access (ordinary and designed worlds) and lifecycle; the Yeti fight held to the completion evidence (charge and columns, footing, cleanup, frost) | `glacier-hall.md`, `yeti-king.md` | Not started |
| 3 | B-05, B-06, B-07 | Rime and Strata research and evidence; Rimebind and Brace; Preserve on the crucible. One unit, because research may unlock only invocations that exist and each invocation must be unlocked by its research (`tools/check_mod_data.py:10321-10323,10688-10693`) | new `arcane-concordance-rime-strata.md` | Not started |
| 4 | B-08, B-10 | The winter workshop and gear: Winter Brine and Frostward at the crucible (B-D3); the Rime travel kit from the owner's water sets (B-D2); Thallite and the trophies linked without stat changes | new `arcane-concordance-winter.md`; `arcane-concordance-alchemy.md` | Not started |
| 5 | B-09, B-11 | Brace and Rimebind as optional counters in the Yeti fight; combined fixes, evidence and the owner checkpoint | `yeti-king.md`; this page | Not started |

## Baseline (B-00)

- Base: `d4c07bf56` (worktree branch `wip/chunk-b`, to be pushed as `claude/chunk-b-winter-stone`); main `b73f51c8a`,
  unmoved when checked by REST on 14 October 2026. The working tree was clean.
- Framework lock: no pin or dependency change is planned.
- Base checks, run on 14 October 2026 at `d4c07bf56`: `python3 tools/check_mod_data.py` passed (2189 IDs; data and recipe
  audit, no build or game test) and `python3 scripts/check_repository.py` passed (structure and links). #337's CI on
  `d4c07bf56` passed every job: 10 check runs, all successful (`mod`, `optional integrations absent`, `repository`,
  `choose client tests`, five client shards and `client`), read by REST on 14 October 2026.
- Chunk A changed nothing under `lair/yeti/`, `lair/tyrant/`, `concordance/alchemy/`, `concordance/CrucibleBlockEntity.java`
  or `tools/concordance_alchemy.py`: `git diff --stat b73f51c8a d4c07bf56` on those paths is empty.

### Status delta: the plan's text against the code at `d4c07bf56`

| The plan says | What is true | Evidence |
|---|---|---|
| B3: "Implement the existing planned behavior in chapter 04" (09:100); the Yeti is an "existing plan, not yet an entity" (04:3, 04:120) | Built and on main through #320: wake, hunt, charge and column stun, roar and blizzard, whelps, fury, reset, loot and the Grey Mist. B3 becomes integration, hardening and counters | `lair/yeti/YetiKingEntity.java:463-479` (wake), `:975-1016` (charge), `:1001-1003`, `:1047-1059` (column stun), `:1306-1325` (reset), `:1345-1370` (death); `docs/branches/BOSSES.md:22` |
| B1: "Inspect PR300 or its merged successor" (09:90) | The successor is `c095d57d3` (#320): hall, horn, dimension and lair blocks are merged, and Chunk A left them alone | the `git diff --stat` above; `concordance-chunk-a.md:40-47` |
| Rewards: a "Glacier Maul/Rimeclaw trophy choice" (04:135) | One of the two at random, as the accepted brief says | `loot_table/entities/yeti_king.json` (one pool at 0.15 holding both); `loot_table/bosses/yeti_king.json`; `lair/yeti/YetiKingLoot.java:59-65`; `BOSSES.md:109` |
| Fair contribution (04:149-174) | The Yeti and the Tyrant still use the old path: a private `damageBy`; `participants()` counts players who are alive, in the level and within 64 blocks; direct payment; no `claim()`; a tamed creature's blow counts for no one | `YetiKingEntity.java:242,1282-1296,1359,1406`; `lair/yeti/YetiWhelpEntity.java:103-104`; `lair/tyrant/CinderTyrantEntity.java:270,1533-1547,1618,1687` |
| Preserve, Rimebind and Brace (02:77-79) | Absent: ten invocations, none of them Rime or Strata | `concordance/invocation/` (aegis, cinderbolt, flashstep, hearthflare, hearthguard, hearthspark, kindle, lance, lanternward, revelation) |
| Strata research (taken as missing) | Strata already has two entries: `assay` (Balancewrights; examines `#jugcraft:assay_specimens`) and `runesmithing` (Runesmiths). Rime has none. The Rimekeepers' and Stratawrights' rows give only their roadmap step | `concordance/research/assay.json`, `runesmithing.json`; `concordance/practice/assay.json`; `docs/ARCANE_CONCORDANCE.md:121,123` (the Hearthbinders' row 120 says "implemented") |
| "Use existing Thallite" (09:86; 01:38) | Ore and gear (slices 1 and 2) are merged and tested; slices 3 to 5 (arms; arrow and horse armour; the Focus, Earthen Steps and Bulwark) are not built. The record's status lines are stale. No Concordance data mentions Thallite | `docs/features/thallite.md:5-8,136-141,194`; `ThalliteGearGameTests.java:60-222`; no match for "thallite" under `concordance/` data |
| "A boss drop offers a distinctive variant, not the first and only cold protection" (04:141) | The Yeti Mitten is the only full counter to his frost: `chill()` checks the Mitten and not `canFreeze()`. Jugcraft adds only knitwear to `freeze_immune_wearables`. **Inference, untested:** knitwear stops freeze damage through vanilla's gate but not his frost slowdown (vanilla's freeze damage needs `canFreeze()`, which one listed piece turns off; the frost slowdown does not ask it) | `YetiKingEntity.java:960-967` (the Mitten at `:961`); `lair/yeti/YetiMitten.java:23-35`; `data/minecraft/tags/item/freeze_immune_wearables.json`; mc263src `LivingEntity.java:534-543` (slowdown), `:2938` (damage), `:3756-3760` (`canFreeze`) |
| Winter Brine: "Too much heat consumes the temporary cold property" (03:36) | Heat never consumes Rime: a searing stir damages only Radiance and Verdance. An unheated crucible sits at 20, inside the cold band, and nothing cools one below that. The Rime property gives Resistance | `concordance/alchemy/Mixture.java:26,82-85`; `Band.java:12-15`; `Heat.java:12-20`; `tools/concordance_alchemy.py:95-103,121-122` |
| A late-joining client receives phase and elapsed state (08:127) | Determinable: a player who comes into view joins the boss bar, and his action and fury are synced; the elapsed time is not | `YetiKingEntity.java:194-195,1457-1460` |
| Designed-world access (09:90; 07:13) | Undetermined. Nothing in `world/design/` mentions a lair or the Glacier Hall (searched). The designer's client test builds a NORMAL world, swaps the Overworld's generator and runs the export; it never creates a world from an exported preset | `WorldDesignerClientGameTests.java:33-61,64,89`; `docs/WORLD_DESIGNER.md:55` |
| Open PRs #299, #300 and #301 (09:11) | 34 open (#322 to #355) at 00:30 UTC on 14 October 2026; #354 and #355 opened after midnight that day | the table below |

### Two payout defects (both bosses)

1. **A player whose only hit is the killing blow is not paid.** Vanilla calls `die()` inside `hurtServer` (mc263src
   `LivingEntity.java:1145,1218`). The Yeti credits a blow only after `super.hurtServer` returns
   (`YetiKingEntity.java:1405-1407`), and the Tyrant likewise (`CinderTyrantEntity.java:1686-1688`), so the payout comes
   first. Chunk A fixed this for Vesperine (`lair/vesperine/VesperineEntity.java:1271-1281`).
2. **A second `die()` pays a second time.** `defeated()` has no guard (`YetiKingEntity.java:1356-1359`;
   `CinderTyrantEntity.java:1614-1618`). Vesperine asks `encounter.claim()` first (`VesperineEntity.java:1289-1291`;
   `lair/Encounter.java:390-397`).

B-01 fixes both, for both bosses (B-D1).

### Open pull requests (REST, 14 October 2026; refresh before each unit, 09:11)

| PR | What it is | Bearing on Chunk B |
|---|---|---|
| [#337](https://github.com/jimbozoomer-byte/jugcraft/pull/337) | Chunk A, head `d4c07bf56`, not merged | The base. A review change means a rebase |
| [#352](https://github.com/jimbozoomer-byte/jugcraft/pull/352) | "Integrate October 13 community content", base main, 5,840 files, head `c692e2943`; combines 31 pull requests, #322 to #351 and #353, #337 at `d4c07bf56` among them (its record `docs/features/oct13-community-integration.md`, on that branch). It carries #351 at `b7f0ec1` and #353 at `7ed1804`; both have moved on since (`c619e89b5`, `9cfe0fdab`). Integration and fixes by OpenAI Codex, per its body; open until its combined checks pass | If it merges, Chunk B rebases onto main. At its head, `LairDossiers`, `Encounter`, `LairRewards`, `Lairs`, `YetiKingEntity`, `YetiKingLoot`, `CinderTyrantEntity`, `ConcordanceEffects`, `PageWriter`, `FirstLightRoute`, `tools/acquisition.py`, `tools/encyclopedia.py` and `tools/concordance_alchemy.py` equal the base; `CrucibleBlockEntity.java` differs (from #323). It keeps #337's encyclopedia as the live one, with #322's guides as a child screen |
| #323 | Peepo companion jobs, 154 files | Changes `CrucibleBlockEntity.java` (+13 −4: a companion field, an extracted `matchesIngredient`, and filter save and load in `saveAdditional` and `loadAdditional`), the methods B-07 and B-08 touch |
| #322 | The fork's encyclopedia | Add/add conflicts with #337 *(survey)*; reconciled inside #352 |
| #331, #336 (Mire Hag); #350, #353 (Crypt Lich) | New lairs and bosses | Shared hot spots: `BOSSES.md`, `CHANGELOG.md`, the gametest `fabric.mod.json`, `check_mod_data.py`, lair tools and tags. Both bosses keep the old damage-only path (`damageBy` at `MireHagEntity.java:249` and `CryptLichEntity.java:258`, read at #352's head), so B-02's checks must not require JEI pages of every boss with a loot table. #336's `FenLampBlock` has its own relit listener *(survey)* |
| #330 | Cinder Kiln light, 37 files | No Java change to the Tyrant: art, the kiln's structure, its tools and records. It shares `CHANGELOG.md`, `tools/check_mod_data.py` and `docs/features/cinder-tyrant.md`, which B-01 and B-02 update. Its light-flood audit could serve the hall *(survey)* |
| #332, #338, #344, #355 | Guns 12A to 12C and 13A | #332 gives Levitation to every creature an End gun hurts *(survey)*; B-04 tests the Yeti and his whelps against it if #332 or #352 merges first. #344 (29 files, on #338) adds no on-hit effect *(survey)*. #355 (30 files, on #344) shares `CHANGELOG.md` and the gametest `fabric.mod.json` |
| #343 | Snowy-region fluorite, 59 files at `6256dd864` | A new cold ore. It also changes `tools/owner_art.py` (which units 3 and 4 import through), `recipe_view.json` (B-02, B-08), `check_mod_data.py`, the gametest `fabric.mod.json`, `JugcraftMaterials`, `OreSurvey` and a `has_surface_ore` biome tag. Chunk B must not create or depend on an ore (09:94) |
| #351, #354 | Spodumene deposits (91 files at `c619e89b5`); regional mineral sands (draft, 92 files) | Both change `tools/owner_art.py`, `recipe_view.json`, `check_mod_data.py` and the gametest `fabric.mod.json`, as #343 does; no lair, Concordance or Thallite file |
| #324 to #329, #333 to #335, #339 to #342, #345 to #349 | Industrial, power and nuclear work | No Chunk B surface by title. From #333 on they add Thallite chloride leaching *(survey)*, so Chunk B must not rename Thallite's ids or features |

### Checks before each unit lands

- `python3 tools/check_mod_data.py` and `python3 scripts/check_repository.py`.
- Refresh the open pull requests; rebase if #337 or #352 has merged.
- The unit's own tests (see the Validation plan).

## Tickets

### B-01. The Yeti King, then the Cinder Tyrant, on Chunk A's encounter credit

**What the player sees.** A player who only heals or wards others against the Yeti qualifies for his reward. One who
fell, or left the game, in the fight is paid later. A killing blow alone qualifies. A tamed creature's blow credits its
owner. A player who already has a part in his fight and baits his charge into a column qualifies by that deed. Every
reward is paid once.

**Changes** (the pattern is Vesperine's: `VesperineEntity.java:236,1271-1296`; `JugcraftVesperine.java:58`;
`VesperineLoot.java:42,66,78,83`):
- `YetiKingEntity.java`:
  - `damageBy` (`:242`) becomes an `Encounter` round `centre()`, with an `encounter()` accessor for the tests;
  - `wake` (`:463-479`) calls `begin()`, and the fight begins again after a load if he is awake;
  - `took` (`:1282-1284`) feeds `encounter.damage`; `participants` (`:1287-1296`) reads `encounter.qualified()` and
    keeps its signature;
  - `reset` (`:1316`) calls `abandon()`;
  - `die` (`:1345-1350`) credits `Encounter.striker(source)` first; `defeated` (`:1356-1370`) asks `claim()` before
    anything else and pays through `LairRewards.pay(level, YetiKingLoot.BOSS, encounter, position(), centre(), EXPERIENCE)`;
  - `hurtServer` (`:1406`) and the whelp (`YetiWhelpEntity.java:103`) credit `Encounter.striker(source)`, so pets count.
- `YetiKingLoot.java`: `BOSS = "yeti_king"`; `pay(ServerLevel, Vec3, ServerPlayer, int)`; `roll` by origin beside the
  entity overload, which `YetiKingGameTests.java:371` calls; `table` takes the origin instead of `king.position()`
  (`:72-77`); `reward(...)` delegates.
- `JugcraftYeti.register` (`:42`): `LairRewards.payer(YetiKingLoot.BOSS, YetiKingLoot::pay)`.
- **The column deed** (B-P3, revised 14 October 2026). On the last tick of the charge's wind-up, where its line is fixed
  (`YetiKingEntity.java:983-990`), he notes whom it is aimed at, but only if that player already has a part in his
  fight: a share (`Encounter.share(player) != null`, `Encounter.java:350-353`), which any blow, credited healing or a
  ward cast on another player in the fight creates, even short of qualifying (`:274-279`, `:316-322`, `:326-330`). When
  the charge ends in a column stun (`YetiKingEntity.java:1001-1003`), that player, if still eligible and in his fight,
  earns `encounter.deed(player, "column " + their uuid)`:
  - one key per player, so two baiters each earn theirs; a constant key would credit only the first, because a fight
    keeps one set of deed keys (`Encounter.java:282-288`);
  - being struck by his fight is not a part: the hurt map (`Encounter.java:72,292-296`) is not read. `target()` falls
    back to the nearest eligible player (`YetiKingEntity.java:604-623`), and one deed qualifies
    (`Encounter.java:101-103`), so an idle late arrival he turns on earns nothing by where the charge ends (04:170;
    10:58), and one trampled by a Fury charge cannot earn the chained one that follows (`YetiKingEntity.java:1037-1044`);
  - a stun called any other way (`stun()`, `:1047`) credits no one.

  The earlier rule, "only a foe who moved 2 blocks or more during the wind-up", was dropped: a late arrival who walks in
  during a wind-up would pass it, and it needs each player's position tracked every tick. A pure dodger with no blow,
  healing or ward earns nothing from the deed; it mostly helps support players short of the threshold.
- **The Tyrant, in a separate commit** (B-D1): the same changes at
  `CinderTyrantEntity.java:270,565,1533-1547,1570,1602-1618,1686-1688`, `CinderlingEntity.java:137-140`,
  `CinderTyrantLoot.java` (`BOSS = "cinder_tyrant"`; `pay`; `roll` by origin beside the entity overload; `:74` uses
  `tyrant.position()`) and `JugcraftTyrant.java:41`. No deed (B-P4).

**Reuses, unchanged:** `Encounter` and `LairRewards`; the effect observer at `lair/Lairs.java:82-83`; the saved formats
of `jugcraft:lair_rewards`.

**Tests (server):**
- `YetiKingGameTests.supportSharesHisLootOnce`, modelled on `VesperineGameTests.supportSharesHerHarvestOnce` (`:342`): a
  healer qualifies; a fallen striker is kept and paid on respawn; a player who left the game in the fight is paid on
  joining; a bystander and a wanderer are not paid; the experience is shared among those paid; a second `die()` pays
  nobody.
- `aKillingBlowAloneIsPaid`; `aPetsBlowIsItsOwners` (on him and on a whelp).
- `twoBaitersEachEarnTheirDeed`: two players, each with a part short of qualifying, charged into the column in turn,
  earn one deed each and qualify by it; the key is `column <player>`, once a fight. `anIdleLateArrivalEarnsNothing`: a
  player his fight has struck but who has no part, charged into a column, gets no deed and no reward.
- `fourPlayersAreEachPaidOnce`, the party cap (04:194): his health scales, and four shares are paid once each.
- `aFightLoadedAwakeIsRecordedAfresh`: a real entity save and load (`saveWithoutId`/`load`); loaded awake he holds no
  one's part and begins a fresh record.
- The records state how each lifecycle event is produced:
  - *death:* the Yeti's support test performs one real `ServerPlayer.die` on a mock player, so Fabric's `AFTER_DEATH`
    (fabric-api 0.161.0 `ServerPlayerMixin.java:76-79`) reaches `Encounter.java:148-153` and `Lairs.java:93-97`; the
    blow that would kill is not real. The Tyrant's marks death with `encounter.excuse`, as Chunk A's tests do: the death
    path is shared code, so proving it once is enough;
  - *leaving:* marked with `encounter.left`, as Chunk A's tests mark it (`concordance-chunk-a.md:134-135`), on a Fabric
    `FakePlayer` the player list never held (it has a connection, so `LairRewards.welcome` can run); the real `LEAVE`
    path (`Encounter.java:156-162`) stays not run;
  - *respawn and join:* `LairRewards.handOver` and `welcome` are called directly (their events are at
    `LairRewards.java:105-106`);
  - *save:* in-process entity and attachment round trips, not a restart;
  - *party:* four mock players, not four people;
  - unchanged from Chunk A: no real disconnect and no two clients.
- `LairEncounterGameTests.aKeptYetiKingRewardIsSavedAndPaid`: a kept `yeti_king` Due survives a save and is paid by the
  Yeti's payer (the pattern of `keptRewardsSurviveASave`, `:322`).
- `CinderTyrantGameTests`: the same for the Tyrant, without the column cases.
- Unchanged and still passing: `YetiKingGameTests.hisLootIsEachParticipantsOwn` (`:356`), the Tyrant's test of the same
  name (`CinderTyrantGameTests.java:629`), and the kill in `YetiKingClientGameTests` (`took` then `genericKill`,
  `:204-205`).

**Acceptance:** "Reward and return survive relevant lifecycle events" (09:110) for death, disconnect and a repeated fall;
exactly-once rewards (04:20); no reward for a late arrival who did nothing (04:170).

**Save effects:**
- New boss ids `yeti_king` and `cinder_tyrant` inside the existing `jugcraft:lair_rewards` attachment and world record,
  stable once released.
- Downgrade: an older build has no payer for them, takes the kept reward off the player and skips it
  (`LairRewards.java:184-189`).
- The bosses' own saved data does not change: `damageBy` was never saved, and `Encounter` is not saved by design
  (`Encounter.java:47-48`).

**Review boundary:** reward code and its tests. The only change in the fight is the column deed, which adds credit.

### B-02. Dossiers, the route step and JEI pages for the Glacier Hall and the Cinder Kiln

**What the player sees.**
- Carrying a Frost Horn shows the Glacier Hall's rite: all year, at night, standing on snow or ice, and the party size.
  A Kiln Seal shows the Kiln's: any hour, on magma in the Nether or a volcanic land of the Overworld
  (`cinder-kiln.md:14`).
- Meeting or beating a keeper shows how support earns a share, his loot and the recipe a win opens
  (`frost_horn_from_fur`, `kiln_seal_from_scales`).
- JEI has pages for Yeti Fur, the Yeti Mitten, the Yeti King's Crown, the Glacier Maul and the Rimeclaw, and the Tyrant's
  loot, with exact chances "with the default data" (A-D15).
- A first win over either keeper counts on the First Light route's lair step.

**Files:**

| File | Change |
|---|---|
| `lair/LairDossiers.java:44-52` | `YETI_KING` and `CINDER_TYRANT` entries |
| `lair/LairDossiers.java:58-59,163-169` | The trophy becomes one of a pair |
| `lair/LairDossiers.java:134-139` | The exhaustive `rite()` switch gains both cases and their lang keys |
| `lair/LairDossiers.java:151-153` | The season fact assumes the Halloween gate; an "all year" variant for rites that ignore it |
| `concordance/encyclopedia/FirstLightRoute.java:92-93` | `abominable` and `tempered` join `FIRST_WINS` |
| `tools/encyclopedia.py` (dossiers; shared credit and route strings) | New entries; pronoun-neutral shared strings (B-P2) |
| `tools/acquisition.py:41-50,96-117` | `event` optional; a trophy pair as one pool ("a 15% chance of one of the two"), not 15% each; new rows |
| `tools/check_mod_data.py:9697-9733` (`check_acquisition`) | An explicit boss-to-loot-class map (today the path is built from the boss name, `:9698`, which finds no `lair/yeti_king/Yeti_kingLoot.java`); the Halloween chance optional (`:9725-9728`); a first-kill rule that accepts the `TROPHIES` table shape (`:9729-9733`); bosses without pages (the Mire Hag, the Crypt Lich) allowed |
| Generated | `lang/en_us.json`; `recipe_view.json` (`acquisition_info`) |
| Stale records | `concordance-first-loadout.md:117,269-270`; `lair-encounter-credit.md:167-170`; `BOSSES.md:25`; the loot sections of `yeti-king.md` and `cinder-tyrant.md` |

**Reuses:** `LairDossiers`, `PageWriter`, the `acquisition_info` pipeline, `check_encyclopedia`.

**Tests:**
- `EncyclopediaGameTests` (pattern `aBossIsHiddenUntilDiscovered`, `:351`): a horn carried shows the rite; a visit to the
  hall marks the Yeti met; Abominable marks him beaten; the after-win recipe is optional; the same for the Kiln.
- Route completion by `abominable` and by `tempered`.
- `theExpeditionsPageHoldsEveryKeeper`: with all four keepers beaten, every recipe grid is drawn and no row is dropped.
  The four need 12 grids (Vesperine 3 + 1 + 1, Madame Tatterlace 2 + 1, the Yeti 1 + 1, the Tyrant 1 + 1; the review's
  count from `LairDossiers.java:45-52` and the new entries), exactly `PageWriter.MAX_GRIDS`
  (`guide/encyclopedia/PageWriter.java:54`).
  Past it a grid keeps only its text, and rows past the page's maximum are dropped (`:48-49,129,256`). The test warns
  the next keeper; B-08's preparation fact is text and icons, with no grid (B-P29).
- `check_mod_data.py` mutation checks, as in Chunk A.

**Acceptance:** B4's encyclopedia guidance (09:106); closes Chunk A's deferral (`concordance-chunk-a.md:157-160`).

**Save effects:** none; the encyclopedia keeps no saved state (A-D9).

**Review boundary:** pages, tools and checks. #352 keeps #337's encyclopedia as the live one, so this ticket stands
whichever merges.

### B-03. Glacier Hall access and lifecycle (plan B1)

**What the player sees.** Nothing new if all works; otherwise the defects found are fixed.

- **Designed world** (undetermined today): a client test exports a design, creates a world from the exported preset and
  checks that `jugcraft:glacier_hall` and `jugcraft:cinder_kiln` exist and a horn opens a slot (07:13, 07:147-148). If the
  datapack dimensions are missing, the fix belongs in the designer's export, not in the lair.
- **Horn rules:** the refusals are listed in `glacier-hall.md:17-22`; `GlacierHallGameTests` covers night and snow
  (`:113`) and a full slot (`:141`). Add any missing case, such as Peaceful.
- **Restart:** instances clear when the server starts and stops (`Lairs.java:88-89`). Extend step 7 of
  `LairClientGameTests` (`:253`, which already calls `Lairs.reset()`) to the Glacier Hall instead of writing a second
  restart test. With B-01, a Due kept before the restart is paid after it (07:155). This is not "persistent lairs" (04:9).

**Reuses:** `Lairs`, `LairVisit`, `MistGateEntity`, `GlacierHallGameTests`, `GlacierHallClientGameTests`,
`LairClientGameTests`, `WorldDesignerClientGameTests`.

**Save effects:** none expected.

**Review boundary:** tests, and fixes only where a test fails. The hall's structure, blocks and recipe stay (04:118;
09:90).

### B-04. The Yeti fight held to the completion evidence (plan B3)

- **Edge determinism** (09:102). The charge moves by a fixed step and stops at leash − 1, 29 blocks from the centre
  (`YetiKingEntity.java:1006-1011`); a column stuns when the next step comes within 5.2 of one (`:1001-1003`). Server
  tests, each giving the same result every run: a charge that grazes a column; a charge toward the dais or a den wall; a
  charge that starts at the leash; a chained charge in his fury. The dais and wall geometry live in
  `tools/glacier_hall.py`, not yet read.
- **Safe footing** (09:110): trampled routes survive the slam, the fury ring, the drift back, `driftAll` on reset and on
  death, and the drift on load (`bare` at `:1133`, `drift` at `:1168`, `driftAll` at `:1182-1187`).
- **Cleanup** (09:110): whelps, boulders, icicles and spikes are not saved (`lair/yeti/JugcraftYeti.java:46-54`). Test
  reset, death and an unload mid-fight: all gone, and the glare ice drifted back.
- **Frost facts:** knitwear and leather against `chill()`: freeze damage stopped or not, slowdown present or not. This
  turns the inference in the status delta into evidence for B-08 and B-10.
- **Late joiners:** a test that a player arriving mid-fight joins the boss bar (`:1457-1460`). The elapsed time stays
  unsynced (Deferrals).
- **Levitation:** if #332 or #352 merges first, End-gun Levitation does not lift the king or his whelps.

**Acceptance:** 09:108-110. "Solo melee, solo caster and a mixed party" is automated: one or more test players in a
server game test, the caster's spell cast through Spell Engine. A party of people and two clients stay not run (09:20).

**Save effects:** none. A fix that changes his saved data needs a versioned read.

**Review boundary:** the Yeti and the hall. No tuning change without a failing test.

### B-05. Rime and Strata research and evidence (plan B2, part 1)

Ships with B-06 and B-07 as unit 3: a research entry may unlock only invocations that exist
(`tools/check_mod_data.py:10321-10323`), and each invocation must be unlocked by its research (`:10688-10693`).

**What the player sees.** Two research entries with typed evidence, each piece counted once by its key (02:129, 02:153),
and the next step named in the journal and the encyclopedia.

| Research (proposed id) | Tradition, stage | Encountered | Observed | Understood (and what it unlocks) | Mastered |
|---|---|---|---|---|---|
| `jugcraft:rimekeeping` (Rime) | Rimekeepers, practitioner; requires First Light understood | Examine an item in a new `#jugcraft:rime_specimens` (snowball, snow block, ice, packed ice) | Two distinct | Any of: study at the Lampwright's Bench; notes; compare a cold and a warm Rime brew (practice keys `brew_cold`, `brew_warm`; 02:134); study the Glacier Hall (practice key `glacier_hall`, recorded on entering through `FrostHornRite.follow`, `lair/FrostHornRite.java:133-136`, which the blower and the whirl both use, `:80` and `MistGateEntity.java:132`; 02:136). Unlocks Rimebind and Preserve | Varied practice with distinct keys: `preserve`, `rimebind <creature type>` (restraint within tolerance, 02:135), the brews, `glacier_hall`, `frost_boon` (B-10), and the elective `yeti_column`, given with the B-01 column deed (02:137) |
| `jugcraft:stratawrighting` (Strata) | Stratawrights, practitioner; requires First Light understood | Examine an item in `#jugcraft:assay_specimens`, the tag the existing `assay` research reads (02:140) | Three distinct | Any of: study at the bench; notes; practice `rooted <ground>` (an Earthbound Thallite wearer struck while Rooted holds; 02:141). Unlocks Brace | Varied practice: `brace living` and `brace earthen` (a Brace that took a push on living or earthen ground; 02:142), `brace ally`, `rooted <ground>`; casts in distinct chunks |

The thresholds are set, and tested, in the unit.

**Reconciled with `assay`** (09:13): no Strata specimen tag and no second examination.
- `#jugcraft:assay_specimens` gains raw Thallite, the Thallite ingot, stone and deepslate (its generator,
  `tools/concordance_equivalence.py:194-199`).
- The tag is read only as research evidence: examination, through `#jugcraft:concordance_specimens`, which includes it
  (`concordance/Examination.java:55`), and study at the bench (`concordance/research/assay.json`).
  `Assaying.SPECIMENS` (`concordance/assay/Assaying.java:88`) names it but nothing reads that constant, and the
  Assayer's Scale weighs against the equivalence catalogue (`Assaying.java:71`, `:187`), so nothing new reaches the
  Scale.
- The research engine applies each piece of evidence against every entry's rules
  (`concordance/ConcordanceProgress.java:99-101`), so one examination serves `assay` and `stratawrighting`; a test
  confirms it. `assay` becomes easier to begin (stone counts); its mastery is unchanged.
- Strata can be completed with the `thallite` feature off: stone and the existing five specimens suffice.

**Not in this ticket:** Strata's steps 4 and 5 (a stable ritual foundation; a civic project or a structural boss opening,
02:143-144) and the "warm surface" half of Rime's step 1 (an examination records only the item and the light
*(survey)*). See Deferrals.

**Files** (the template is Ember part 1, `9a0fab5a5`, 67 files):
- New generators `tools/concordance_rime.py` and `tools/concordance_strata.py`, merged in `tools/concordance.py`
  (research, components, invocations, status names, particles, codex positions and categories after Hearth's 16, sounds,
  messages, tags *(survey: merge points)*).
- `tools/concordance_progression.py`: `PRACTICES` rows, and `VANILLA_SOURCES` rows for snowball, snow block, ice, packed
  ice, stone and deepslate. No step may need the Nether or the End.
- `tools/concordance_conclave.py` `TRADITIONS`: Rimekeepers and Stratawrights (held equal to the research traditions
  *(survey)*).
- Java: new `concordance/rime/Rime.java` and `concordance/strata/Strata.java`, each with `ACTIVITY` and `RESEARCH`
  constants, registered beside `Ember.register()` (`concordance/JugcraftConcordance.java:292`); their `Examination`
  messages.
- Generated and written docs: the guide block in `docs/ARCANE_CONCORDANCE_GUIDE.md` (made by
  `tools/concordance_delivery.py:229-245`, held by `check_delivery`, `tools/check_mod_data.py:14111-14117`);
  `docs/ARCANE_CONCORDANCE.md:121,123`; `docs/WHAT_EXISTS.md`; `CHANGELOG.md`.
- Audits: `check_rime` and `check_strata`, called from `check_concordance` and mutation-tested.

**Tests:** a new `ConcordanceRimeStrataGameTests` (its entrypoint in the gametest `fabric.mod.json`);
`ConcordanceProgressionGameTests` (both routes, no Nether or End, `strataNeedsNoThallite`, one examination serving both
Strata entries); `ThalliteGameTests.thalliteIsAStrataSpecimen` (09:112).

**Save effects:** new research keys in the existing knowledge record and new advancements
`concordance_<research>_<state>`, stable once released. The research record's round trip in `ConcordanceGameTests`
carries the new ids.

### B-06. Rimebind and Brace (plan B2, part 2)

**Budget.** Ten invocations against a cap of 16 (`tools/check_mod_data.py:10622-10623`; `concordance/Tunings.java:22`);
signatures unique (`:10668-10671`); the six roles still covered (`:10626-10627`). After this unit, 13 of 16.
- **Rimebind:** `ray struck` with a harmful status, `jugcraft:rimebound`; signature `ray/struck:status/harmful`, free.
- **Brace:** `here allies` with a helpful status, `jugcraft:braced`; signature `here/allies:status/helpful`, free.
  `allies` is the caster and their party only (`concordance/ComposedSpells.java:268-273`), so the party is the consent
  (B-P7). A self-Brace written `here struck` would collide with Hearthguard.
- Both are authored: no word a player composes is added, and the composer's locked count stays 10
  (`ConcordanceComposeGameTests.java:480`) (B-P6).

**Rimebind** (B-P9):
- `rimebound` slows by 20% for 60 ticks through an attribute modifier on the effect (the pattern of
  `agriculture/Hexes.java:61-69`).
- **Grace:** a hidden `jugcraft:rime_thaw` for 100 ticks (3 s slowed, then 2 s of grace), and a `ConcordanceEffects`
  Guard (the pattern of `concordance/sympathy/Sympathy.java:134`) that refuses Rimebind while it lasts. Nothing like it
  exists: a lasting effect combines only by its stacking rule (`concordance/ConcordanceEffects.java:379-391`), and a
  composed spell's cooldown ignores how long its status lasts (`concordance/compose/Compiler.java:151`). The grace covers
  Rimebind's slow only (B-P10).
- **Tolerance** (B-P8): harmful statuses and pushes fall under `Tolerance` (`concordance/effect/Tolerance.java:24-43`;
  `ConcordanceEffects.java:187-191,344-349`). Only the Yeti King and the Cinder Tyrant join
  `#jugcraft:concordance/resistant` (today the iron golem, ravager and piglin brute). That halves every harmful
  Concordance status and push on them, not only Rimebind's: Smoulder, Dazzle, and the Ice Breaker wave's slow and push
  (`concordance/trinket/Wayfaring.java:129-132`) among them. The Ember and trinket records say so, and
  `ConcordanceEffectGameTests.friendlyFireToleranceAndTriggeredCause` (`:121`) covers it. Vesperine and Madame Tatterlace
  are unchanged.
- The Yeti moves by fixed steps, not by his movement-speed attribute (`move`, `YetiKingEntity.java:636-650`; the charge,
  `:1011`), so a slow alone does nothing to him (an inference until tested); B-09 gives him a visible change instead of
  a silent one (04:188).

**Brace** (B-P11, B-P12): `braced` for 100 ticks, +0.5 knockback resistance, EXCLUSIVE, one hit. Vanilla's knockback
respects knockback resistance (mc263src `LivingEntity.java:1543-1544`). The order is fixed here because B-09 depends on
it:
- **Most blows** remove it after damage (`ServerLivingEntityEvents.AFTER_DAMAGE`, the pattern of
  `concordance/ember/EmberGear.java:120`). Vanilla's default knockback runs inside `hurtServer`, before that event
  (mc263src `LivingEntity.java:1209`), so it is reduced.
- **The Yeti's blows** call `hurtServer` first and his own `push` afterwards: swipe `YetiKingEntity.java:817-819`, slam
  `:883-886`, trample `:1027-1031`, spike `lair/yeti/GlacialSpikeEntity.java:60-63`. His `push` ignores knockback
  resistance (`YetiKingEntity.java:829-832`; mc263src `Entity.java:1869`). So the Yeti takes the Brace **before** calling
  `hurtServer` and reduces that blow's push himself (B-09); the removal after damage then finds none. Without this order
  the Brace would be gone before his push.
- **A harmful Concordance push** uses it up. **A helpful one** (Flashstep on oneself) neither uses it nor is shortened by
  it: Concordance pushes scale by 1 − knockback resistance (`ConcordanceEffects.java:406-407`).
- It cannot touch boundary returns, which are teleports (`Lairs.java:541-561`).

**Costs** (hypotheses, 02:73): Rimebind 4 Focus (3 mastered), Brace 3 (2 mastered), on the scale of today's 3 to 7
(`tools/concordance.py` `INVOCATIONS`), settled once when Spell Engine consumes the cast's cost
(`concordance/ConcordanceSpells.java:42,74,185`).

**Benchmark.** An "Only X" kit is generated for every invocation (`concordance/balance/Baselines.java:154-158`), but
`Benchmark` treats any harmful status as Slowness at 15% a level with no grace, and a helpful status as nothing
(`concordance/balance/Benchmark.java:504-508,553-558`). Teach it `rimebound`'s 20% and the grace; record that Brace has no
measure there.

**Assets.** Icons from the owner's library, copied into runtime with their provenance
([MAGIC_ASSETS.md](../../art/owner-library/MAGIC_ASSETS.md)): Rimebind `ars_jimbaux/textures/item/freeze.png`, Brace
`ars_jimbaux/textures/item/shield.png`, and the status icons `ars_jymbaumental/textures/mob_effect/frozen.png`
(rimebound) and `ars_jimbaux/textures/mob_effect/shielding.png` (braced); the choices are proposals. The two glyphs and
`shielding.png` are 16×16 and `frozen.png` 18×18, as Jugcraft's own status icons are 16×16 or 18×18 (`smoulder.png`).
Spell icons are 32×32 at runtime (`textures/spell/aegis.png`), so the 16×16 glyphs are scaled by nearest neighbour, a
technical change in the copy. Strata's casts use the owner's `ars_jimbaux/sounds/gaia_1.ogg` to `gaia_4.ogg`. No ice or
water casting recording was found in the owner's sound families; the one water-named file,
`originals/Magic/sounds/item/pewatermagic.ogg`, is loose and sits with `projecte:` models *(survey)*. So Rime's cues are
synthesised, as Ember's were, or vanilla. Tooltips keep Brace apart from the war fork's BRACE trait and the shield's brace
(`weapons/JugcraftArms.java:127,171-172,526`).

**Tests:**
- `ConcordanceEffectGameTests` (09:112): the thaw Guard; Brace's one hit; the tolerance tag.
- `repeatedSlowDoesNotPermanentlyImmobilize` (09:96), scoped to Rimebind: however many casters recast it on one target,
  the target is slowed for at most 3 s in every 5 s over a minute. The other slows stay outside it (B-P10): the
  trophies' Frost boon, refreshed by every hit (`weapons/ArmItem.java:240-250`), the `dazzle` word, the DAZE trait
  (`weapons/JugcraftArms.java:144-146`) and the Ice Breaker wave.
- `braceDoesNotCancelBoundaryEnforcement` (09:96), on the player's returns, not the Yeti's leash (which teleports him,
  `YetiKingEntity.java:589-601`): a braced player past the bounds is returned by `Lairs.edges`; the Grey Mist sends them
  home (`Lairs.java:368`); following through a whirl still enters.
- `braceIsTakenBeforeTheBlow` (the order above); `braceDoesNotShortenFlashstep`.
- `focusIsChargedOnce` (09:96), extending `ConcordanceInvocationGameTests`.
- Saved effects: a relog or a save round trip keeps `rimebound`, `rime_thaw` and `braced` with their time left, so the
  grace survives a logout.

**Save effects:** new mob-effect, invocation and spell ids, stable once released.

### B-07. Preserve (plan B2, part 3)

- **Shape:** `touch struck` with a helpful alteration; signature `touch/struck:alteration/helpful`, free (Hearthspark holds
  `ray/struck:alteration/helpful`). `touch struck` reaches a block (`concordance/compose/Component.java:31-32,56-57`).
- **Dispatch:** on Rime's school, `spell_power:frost`, as Ember's alteration is on fire (`ConcordanceEffects.java:270-277`,
  `accepts` at `:291-300`; `concordance/ember/Ember.java:47,61-63`). Preserve must be in both places: the default
  alteration removes the block (`ConcordanceEffects.java:274`).
- **Target** (B-P13): `#jugcraft:concordance/preservable`, holding only the Alembic Crucible. Preserve holds the
  crucible's temperature band for 60 s; heat otherwise moves one degree a tick toward its source
  (`concordance/CrucibleBlockEntity.java:177-191`; `concordance/alchemy/Heat.java:13`). That is Rime's reliable
  workstation loop (02:54).
- **Refused:** anything else returns `Result.NOTHING` and changes nothing (`ConcordanceEffects.java:246-247`): chests,
  barrels, hoppers, machines and every other inventory (02:77; 06:96; 09:96). A crucible the caster may not change returns
  `NOT_ALLOWED` (`:249-250`, through `Authority.mayChange`).
- **No Thallite Focus here** (B-P14). With the Initiate's Wand's limits (`tools/concordance.py:381-384`) and no upgrade
  (the Adept's Attunement takes only the Initiate's Wand, `concordance/ritual/adept_attunement.json`) it would add
  nothing; the approved record pairs it with Earthen Steps and Bulwark in slice 5 (`thallite.md:136-141,194`). The
  chunk's crafted upgrade from existing materials (09:94) is the travel kit's water foci (B-10). `thallite.md` records
  this.

**Tests:** `preservationCannotWrapArbitraryInventories` (09:96); `preserveHoldsOneBandAndExpires`; in
`ConcordanceEffectGameTests` (beside `blockOperationsActAsTheActor`, `:190`), `preserveNeverRemovesABlock` and
`preserveIsRefusedUnderTownProtection`.

**Save effects:** the crucible gains an optional saved `held_until` (game time), 0 when absent. It stamps version 1
(`CrucibleBlockEntity.java:619`) and reads with defaults (`:587-614`). In `ConcordancePersistenceGameTests`
(`everyRecordReadsItsSaveFromBeforeVersions`, `:109`; `blockEntitiesStampTheirVersion`, `:157`): a round trip keeps the
hold, and a save without the field reads 0. #323 changes the same two methods.

**Review boundary (unit 3):** magic data, effects, the crucible's hold and tests; no lair code.

### B-08. Winter Brine and Frostward at the Alembic Crucible (plan B4; B-D3)

The winter workshop is the existing Alembic Crucible: Rime's brew and Preserve's hold are its loop.

- **Frostward,** a new status `jugcraft:frostward`. While it lasts the Yeti's frost does not land (`YetiKingEntity.chill`,
  `:960-967`, checks it beside the Mitten), and frost already gathered is cleared on a bounded check (the Mitten's
  pattern, `YetiMitten.java:27-35`). It is timed (B-P19: 1200 ticks a unit, at most 2400, as Ember's Fire Resistance
  brew, `tools/concordance_alchemy.py:119-120`). The Mitten, which works for as long as it is held, stays the permanent
  prize (04:141).
- **From the generator,** not hand-edited data: `PROPERTIES["rime"]` (`tools/concordance_alchemy.py:121-122`) gives
  `jugcraft:frostward`, and the status names (`:249-251`) name it; `property/rime.json` is regenerated.
- **The heat rule** ("Too much heat consumes the temporary cold property", 03:36; B-P18): a stir above the cold band keeps
  only part of the dissolved Rime, beside the searing rule (`Mixture.java:26,82-85`). It is mirrored in
  `tools/concordance_alchemy.py` (`:25-27`) and held by `check_alchemy`'s constant table (`tools/check_mod_data.py:11599`).
  It also changes existing brews stirred warm, because amethyst and gloamcap carry Rime (`:96,103`);
  `arcane-concordance-alchemy.md` says so.
- **Winter Brine** names the cold-stirred Rime brew in JEI and the dossier. A snowball carries enough Rime on its own
  (1.0 a part, against the 0.8 threshold, `:95,121-122`), so salt is not added (B-P17).
- **Bottles already made** keep the status they were bottled with (`concordance/Brew.java:19`); a written formula replays
  with the new effect. Knitwear is unchanged.

**Guidance:** JEI's alchemy ingredient pages (snowball, amethyst, gloamcap) name Frostward; Jade's crucible lines add
Preserve's hold time (the crucible's snapshot, `compat/jade/ConcordanceDataProvider.java:142-156`); the Glacier Hall
dossier gains a preparation fact naming Winter Brine and the Mitten (text and icons, B-P29).

**Tests:** `aFrostwardDrinkerIsNotChilledByTheYeti` (`YetiKingGameTests`); `winterBrineNeedsTheColdBand` (stirred cold it
gives Frostward, stirred warm it loses it); `aRimeBottleFromBeforeStillGivesResistance`; must-fail economy loops
(06:146-158): Yeti Fur makes Frostward no cheaper (fur already cheapens the horn: `recipe/frost_horn_from_fur.json`
takes one gold and no leather where `frost_horn.json` takes two of each); the alchemy tests that expect
Resistance from Rime, updated.

**Acceptance:** "one useful cold preparation route and a non-boss resistance option" (09:106); "Every required cold
preparation has a non-boss route" (09:110).

**Save effects:** a new effect id; the property change reaches only new bottles.

### B-09. Optional school counters in the Yeti fight (plan B3; only after B-04 is green, 09:102)

- **Brace** (B-P20, B-P12): before a blow's `hurtServer`, the Yeti takes `braced` from the player and halves that blow's
  push, every push through `YetiKingEntity.push` (`:829-832`), the spike's lift included. Other knockback resistance stays
  ignored, so netherite, Rooted, shields and the Ice Breaker change nothing: this is the plan's "bracable knockback"
  (04:186).
- **The Brace caster's deed** (B-P22; 02:187; 04:139, 04:156): when a Brace that one player put on another absorbs his
  push, the caster earns `encounter.deed(caster, "brace " + braced player + " " + game time)`, one key per occurrence.
  The caster is kept with the effect and not saved, so a Brace carried through a relog keeps its time but credits no one.
  A self-Brace earns nothing, as healing oneself does. Lair support credit sees only restoration and protection
  (`ConcordanceEffects.java:223-228`), so without this deed a Brace caster could never qualify.
- **Rimebind transforms, and is never silently dropped** (B-P21; 04:188): once per thaw window, a rimebound Yeti's next
  wind-up is a few ticks longer. The number is set by test and called untuned until people play it (04:194).
- **Not here:** Rime's "preserved snow" footing (authored anchors only, 04:101; the trampled routes already do this job);
  Ember and Tempest counters.

**Tests** (`YetiKingGameTests`): a braced player is pushed less, exactly once; the caster qualifies by it and a
self-braced player does not; a rimebound Yeti telegraphs longer; with no counters the B-04 suite is unchanged.

### B-10. Winter gear: the Rime travel kit, Thallite and the trophies, without stat inflation (plan B4; B-D2)

The owner's water sets become Rime's travel kit (02:168: a preservation focus, snow-safe boots and a short Brace
accessory, resisting environmental freezing without permanent boss immunity), made in survival like
[Ember's regalia](arcane-concordance-ember-regalia.md). Names and art are the owner's; the numbers are Proposed (B-P23 to
B-P25). Sources are under `art/owner-library/originals/Magic/assets/ars_jymbaumental/`.

| Pieces (the owner's names) | Source | Proposed ids | Role |
|---|---|---|---|
| Nereid's Hood, Tunic, Pants and Shoes (light) | `textures/item/aqua_hood.png`, `aqua_tunic.png`, `aqua_pants.png`, `aqua_shoes.png`; `textures/armor/light_armor_water.png` | `nereids_*` | Protection no greater than the Pyromaniac's set (5 in all); worn on the imported medium model (`geckolib/models/armor/pyromancers.geo.json`) |
| Aquamancer's Hat, Robes, Leggings and Boots (medium) | `aqua_hat.png`, `aqua_robes.png`, `aqua_leggings.png`, `aqua_boots.png`; `medium_armor_water.png` | `aquamancers_*` | Leather's protection (7); the same model |
| Winterguard's Helmet, Chestplate, Leggings and Boots (heavy) | `aqua_helmet.png`, `aqua_chestplate.png`, `aqua_leggings_heavy.png`, `aqua_boots_heavy.png`; `heavy_armor_water.png`; `geo/heavy_armor_water.geo.json` | `winterguards_*` | No more than iron's protection (15), toughness 0; its own model, imported |
| Lesser Focus of Water and Focus of Water | `lesser_water_focus.png`, `water_focus.png` | `lesser_water_focus`, `water_focus` | The preservation focus, in the Spell Focus slot: Preserve holds 90 s or 120 s instead of 60 s |
| Water Bangle | `water_bangle.png` | `water_bangle` | The short Brace accessory, in a Bracelet slot: with Rimekeeping understood, a Brace for its wearer when they have had none for 30 s |

- **Freezing:** every set piece joins `#minecraft:freeze_immune_wearables`, so powder snow and a full freeze deal no
  damage. The Yeti's frost slowdown stays: only Frostward and the Mitten clear his frost, so there is no boss immunity.
- **No stat inflation:** no Spell Power and no knockback resistance on any piece. Frost Spell Power would scale nothing,
  because Rime's invocations deal no damage and Spell Power scales only damage (`concordance/compose/Plan.java:64-81`).
- **Snow-safe boots:** if vanilla 26.3 lets boots other than leather walk on powder snow through a tag, the three boots
  join it; if that is fixed to leather boots, the claim is dropped rather than added by a mixin. Undetermined: the
  reference lacks `PowderSnowBlock`.
- **Made in survival:** crafting-table recipes from Overworld materials (wool, string, dyes, snow, ice, gold, iron); no
  boss item, and no Nether or End step; a `check_rime_kit` like `check_ember_regalia` (`tools/check_mod_data.py:10968`);
  the items kept off the Assayer's Scale like Ember's; rows in `tools/acquisition.py`.
- **Imports** go through `tools/owner_art.py`, with the originals untouched and the provenance recorded (paths,
  checksums, changes); `ars_jymbaumental` references become `jugcraft:` in the copies; the library's language files are
  not imported ([PROVENANCE.md](../../art/owner-library/PROVENANCE.md), its working rule at line 14 and its located
  metadata). Whether the heavy texture fits
  its model has not been checked *(survey)*.
- **Thallite** (09:106) connects through Strata's examination and Rooted practice (B-05); `earthen_ground` and every
  Thallite number are unchanged (B-P28).
- **Trophies** (09:106): a slow from the Glacier Maul's or Rimeclaw's Frost boon counts as Rime restraint practice
  (`frost_boon`) for a wielder who has encountered Rimekeeping. The boon itself is unchanged
  (`weapons/ArmVariants.java:123-125`; B-P10), and their JEI pages come with B-02.
- **Creative-only sets stay so:** the Frost Knight and Wight King (`tools/acquisition.py:74-78`; B-P27).

**Tests:** the kit is made in survival (`check_acquisition`, `check_rime_kit`); a kit wearer takes no freeze damage from
the Yeti and is still slowed (`YetiKingGameTests`); the foci lengthen Preserve's hold; the bangle's Brace recharges;
`ThalliteGameTests` for the Strata link. No test copies the Thallite numbers again:
`ThalliteGearGameTests.thalliteGearMatchesItsNumbers` (`:60`) already holds them (10:63).

**Save effects:** fifteen new item ids and their equipment assets, stable once released.

**Review boundary (unit 4, with B-08):** alchemy, items, gear and tests. In lair code, only `chill()` checking Frostward.

### B-11. Evidence and the owner checkpoint

- **CI candidate** by workflow_dispatch, every job: `mod` (the generated-file and material-data checks, the build and the
  server game tests), the build and server game tests without the optional integrations
  (`-PjugcraftOptionalIntegrations=false`), and the client shards. Locally, `tools/check_mod_data.py` and
  `scripts/check_repository.py`. Each is reported separately.
- **Demonstration table,** tied to 09:108-112: a charge into a column by solo melee, a solo caster and two test players
  (automated); footing through every floor change; cleanup; reward and return across death, disconnect, save and
  restart; the non-boss cold route (Winter Brine; the kit).
- **Client evidence** (10:109-111): screenshots of the three spell icons and the status icons in the HUD; the three sets
  worn, front and back, and the foci and bangle in their slots; the Yeti charging into a column and reeling. New client
  test classes are added to `tools/select_client_tests.py` and the gametest `fabric.mod.json`.
- **Not run, expected:** two clients in one fight; play by hand; timings against guns and mobility items; completion
  times (04:192-194).
- **The checkpoint:** 300 to 500 words under the fixed headings (10:87-121), with 2 to 4 screenshots and a short
  demonstration route. Ask about movement feel, difficulty and the next school (09:114); do not ask whether to run save
  tests. The pull request states the player result, progression links, actual validation, save compatibility, known
  limits and the AI attribution (CLAUDE.md).

## Interfaces expected to change

- **Saved data:** no new saved format. New boss ids `yeti_king` and `cinder_tyrant` inside `jugcraft:lair_rewards`; the
  crucible's optional `held_until`; saved mob effects; new research keys; new item ids.
- **Registries (stable once released):** mob effects `jugcraft:rimebound`, `rime_thaw`, `braced` and `frostward`;
  invocations and spells `rimebind`, `brace` and `preserve`; research `rimekeeping` and `stratawrighting`, with their
  practices and advancements; fifteen kit items; tags `concordance/preservable` and `rime_specimens`; additions to
  `assay_specimens`, `concordance/resistant`, `minecraft:freeze_immune_wearables` and the trinket slot tags.
- **Shared code:** `YetiKingLoot` and `CinderTyrantLoot` gain `BOSS`, `pay` and `roll` by origin (the entity overloads
  kept); `LairDossiers.Boss` gains a trophy pair and an all-year rite; `FirstLightRoute.FIRST_WINS` gains two wins;
  `ConcordanceEffects` gains Rime's alteration and `accepts` case and the helpful-push exemption; a new Guard; `Mixture`
  gains the Rime heat rule; `CrucibleBlockEntity` gains the hold and records Rime brew practice; `YetiKingEntity` gains
  the Frostward check, the taking of Brace and the Rimebind wind-up; `Benchmark` learns `rimebound`. New practice hooks:
  `FrostHornRite.follow` (`glacier_hall`), `ArmItem`'s Frost boon (`frost_boon`) and Thallite's Rooted (`rooted`).
- **Tools:** `acquisition.py`, `check_acquisition`, `encyclopedia.py`, the `concordance.py` merge points,
  `concordance_alchemy.py`, `concordance_equivalence.py`, `concordance_progression.py`, `concordance_conclave.py`,
  `owner_art.py`; new `concordance_rime.py`, `concordance_strata.py` and their checks.
- **Network:** no new payload. Jade's crucible snapshot carries one more value.

## Dependencies and order

1. B-00, then unit 1: B-01, then B-02. B-02 builds on #337's encyclopedia, which #352 keeps.
2. Unit 2 (B-03, B-04) after unit 1: B-03's restart test pays a Due through B-01.
3. Unit 3: B-05, B-06 and B-07 together.
4. Unit 4 (B-08, B-10) after unit 3: Rime research gates the kit's behaviour and the brew evidence, and the foci lengthen
   Preserve.
5. Unit 5 (B-09, B-11) after units 2 to 4.

Can start now, with no further answer: B-01 (the Yeti, then the Tyrant's commit), B-02's generic tooling, B-03 and B-04.
B-05 cannot start on its own.

## Risks

- **Base:** #337 is not merged, and #352 carries it with 30 other pull requests. Whichever merges, Chunk B rebases, and
  #323's crucible change meets B-07's hold field.
- **Invocation budget:** 3 of 16 slots remain after Chunk B; a later Rime or Strata spell must avoid the 13 signatures
  then taken.
- **Alchemy outcomes change:** new Rime brews give Frostward, and a warm stir now costs Rime, in amethyst and gloamcap
  brews too. Bottles already made keep Resistance.
- **Preserve's alteration:** the default branch removes blocks (`ConcordanceEffects.java:274`); a missing `accepts` case
  would delete them. Tested.
- **Brace stacking:** with netherite, Rooted, the Ice Breaker and a shield, one hit may reach full knockback resistance.
  Proposed as tolerable for one hit in five seconds (B-P11); the Yeti ignores all but Brace (B-P20, Proposed).
- **Edge determinism** is untested, and `tools/glacier_hall.py` is unread.
- **Designed worlds** may lack the lair dimensions (undetermined).
- **Downgrade:** an older build drops kept Yeti or Tyrant rewards (`LairRewards.java:184-189`).
- **Names:** "Focus" is both the resource and the foci; "Brace" is also a war fork trait and the shield's brace.
  Tooltips keep them apart.
- **Size:** unit 4 adds fifteen items and a second armour model; the heavy model's fit is unchecked.
- **The Water Bangle's role** (a Brace) differs from the owner's tooltip text in the library (a chill on each blow); see
  B-P25.
- **The Expeditions page** reaches its grid limit with four keepers; a fifth needs a layout change.
- **Two bosses stay on the old path:** the Mire Hag and the Crypt Lich keep damage-only rewards in their pull requests;
  after Chunk B, four bosses are on the encounter credit and two are not.
- **Overlap with Chunk C's preservation wash** (09:124): Winter Brine stays a frost counter.

## Explicit don'ts

1. Do not rebuild, re-namespace or re-route the Glacier Hall, its blocks or the Frost Horn recipe (09:90; 04:118).
2. Do not rebuild the Yeti King, the Cinder Tyrant, Thallite, `Encounter` or `LairRewards`.
3. Do not create an ore, or let terrain magic pass town protections (09:94).
4. Preserve never stops machine time and never wraps an inventory (02:77; 06:96).
5. No permanent root and no mandatory counter (02:78; 04:141, 04:188).
6. No raw-stat upgrade to the trophies, Thallite or the kit (02:173; 09:106).
7. No boss-only cold requirement (04:141).
8. Do not raise the compiler caps or the 16-invocation cap (02:65-67).
9. Do not change Vesperine's or Madame Tatterlace's tolerance.
10. Do not hand-edit generated data such as `property/rime.json`; change the generator and regenerate.
11. Do not change the trophies' accepted boon without the owner (B-P10).
12. Do not make the Frost Knight or Wight King sets obtainable (B-P27).
13. Do not import whole language files from the owner's library, or execute anything in it.
14. Do not mark a Proposed decision approved because no reply came (10:162). Do not count one client as two, or a marked
    disconnect as a real one (09:20).
15. Do not claim "persistent lairs" (04:9) or "balanced" (04:194).
16. Do not edit the Mire Hag's or the Crypt Lich's files beyond shared docs and lists.

## Validation plan

| Unit | Server game tests | Client game tests | Checks | Save tests |
|---|---|---|---|---|
| 1 | `YetiKingGameTests`, `CinderTyrantGameTests`, `LairEncounterGameTests`, `EncyclopediaGameTests` | `YetiKingClientGameTests` and `CinderTyrantClientGameTests` unchanged; `EncyclopediaClientGameTests` if the page layout changes | `check_acquisition`, `check_encyclopedia`, with mutations | A kept Yeti and Tyrant Due across a save |
| 2 | `GlacierHallGameTests`, `YetiKingGameTests` | `WorldDesignerClientGameTests` (an exported preset); `LairClientGameTests` step 7 (the hall); `GlacierHallClientGameTests` | `check_glacier_hall`, `check_yeti` | None needed |
| 3 | `ConcordanceRimeStrataGameTests`, `ConcordanceEffectGameTests`, `ConcordanceProgressionGameTests`, `ConcordanceInvocationGameTests`, `ThalliteGameTests`, `ConcordanceGameTests` | A new Rime and Strata client test (icons, a cast) | `check_rime`, `check_strata`, `check_invocations`, `check_progression`, `check_delivery` | The research record; saved effects; the crucible's field (`ConcordancePersistenceGameTests`) |
| 4 | `ConcordanceAlchemyGameTests`, `YetiKingGameTests`, the kit's tests | The kit worn and slotted (screenshots) | `check_alchemy`, `check_rime_kit`, `check_acquisition` | A Rime bottle from before the change |
| 5 | `YetiKingGameTests` (counters) | `YetiKingClientGameTests` (a charge into a column) | Every check; the CI candidate | Re-run of all of the above |

Every unit also runs `tools/check_mod_data.py`, `scripts/check_repository.py` and the build with its server game tests,
and the build without the optional integrations where it touches an optional boundary.

**Evidence rules** (10:5-22):
- Each command is reported on its own as run and passed, run and failed, or not run. CI on a commit counts for that
  commit only.
- A server game test with several test players is not a party of people. One client on a dedicated server is not two
  clients. A disconnect marked in a test is not a real one.
- A screenshot shows presentation, not balance. Nothing is called "balanced" without a human playtest.
- Inferences in this page (knitwear against the Yeti; Rimebind doing nothing to him) stay inferences until a test runs.

## Deferrals

- Ember and Tempest counters in the Yeti fight; Rime footing at authored anchors (04:101).
- Words a player can compose for Rime and Strata, and "Winter's Courtesy" as a composed example (02:94).
- A Cinder Tyrant objective deed; the sluice records no turner *(survey)*.
- Thallite slices 3 to 5: arms, the arrow and horse armour, the Thallite Focus, Earthen Steps and Bulwark.
- The owner's earth sets (Nymph's, Geomancer's, Wealdguard's) as Strata gear.
- Strata's ritual foundation and civic-project steps (02:143-144); the "warm surface" half of Rime's first step (02:132).
- Syncing the Yeti's elapsed fight time to late joiners (08:127).
- One grace shared by every Concordance and trophy slow (an owner question, B-P10).
- The Mire Hag and the Crypt Lich on the encounter credit (their own pull requests).
- The Reagent Cabinet, Stillwater Keeping, and Root and Rivet (Chunk C: 06:119; 09:132).
- The Ice Breaker's anti-slip from the owner's text, and the Winter Crown pattern's Fire Resistance attunement *(survey)*,
  left as they are.

## Where to resume

B-01's Yeti King commit is in: the Yeti King on `Encounter` and `LairRewards` with both payout defects fixed and the
column deed; its game tests have not run yet. Next, the Cinder Tyrant's B-01 commit, then B-02. Refresh the open pull
requests first: #352 decides the rebase.

## Decisions

Chunk A's decisions are unchanged by these answers: A-D1 is Accepted (owner), A-D2 to A-D13 and A-D15 remain Proposed,
A-D14 is Superseded and A-D16 Open (`concordance-chunk-a.md:164-181`).

| ID | Date | Decision | Status | Affects |
|---|---|---|---|---|
| B-D0 | 13 October 2026 | Chunk B next ("Ok lets begin Chunk B"); its commits on the stacked branch `claude/chunk-b-winter-stone`, on Chunk A (#337) | Accepted (owner) | All of Chunk B |
| B-D1 | 13 October 2026 | "Yes, include him (Recommended)": the Cinder Tyrant joins unit 1 (support credit, kept rewards, dossier, JEI pages) as a separate commit; his two payout defects are fixed either way | Accepted (owner) | B-01, B-02 |
| B-D2 | 13 October 2026 | "Use your water sets as Rime (Recommended)": the owner's water sets in the library (Winterguard's, Aquamancer's, Nereid's, with the water focus, lesser water focus and water bangle) become Rime's winter gear, the plan's "Rime travel kit" (02:168: preservation focus, snow-safe boots, a short Brace accessory; resist environmental freezing without permanent boss immunity), made in survival like Ember's regalia; names and art stay the owner's; no stat inflation | Accepted (owner) | B-10 |
| B-D3 | 13 October 2026 | "A timed brew (Recommended)": Winter Brine from the Alembic Crucible gives a timed Frostward status that the Yeti King's frost respects; the Yeti Mitten stays the permanent prize | Accepted (owner) | B-08 |
| B-P1 | 14 October 2026 | The trophy stays one of the two at random (`BOSSES.md:106-112`); 04:135's "choice" is stale | Proposed | B-02 |
| B-P2 | 14 October 2026 | Shared dossier and JEI wording becomes pronoun-neutral | Proposed | B-02 |
| B-P3 | 14 October 2026 | The column deed: key `column <player>`, one per baiter per fight, for the player the charge was aimed at as he set off, only if they already had a part (a share) then; being struck by his fight is not a part | Proposed (revised 14 October 2026 from "moved 2 blocks or more during the wind-up", which a late arrival walking in would pass) | B-01 |
| B-P4 | 14 October 2026 | No Cinder Tyrant objective deed in Chunk B | Proposed | B-01 |
| B-P5 | 14 October 2026 | Harm with no attacking creature (slag, fire ticks, freezing) stays outside the fight's hurt (`Encounter.java:132-134`) | Proposed | B-01 |
| B-P6 | 14 October 2026 | Authored invocations only (13 of 16); no words a player composes for Rime or Strata | Proposed | B-06, B-07 |
| B-P7 | 14 October 2026 | Brace's consenting ally is the party (`here allies`) | Proposed | B-06 |
| B-P8 | 14 October 2026 | Only the Yeti King and the Cinder Tyrant join `#jugcraft:concordance/resistant`; Vesperine and Madame Tatterlace do not | Proposed | B-06 |
| B-P9 | 14 October 2026 | Rimebind: `rimebound`, 20% slower for 60 ticks; a hidden `rime_thaw` for 100 ticks refuses a recast | Proposed | B-06 |
| B-P10 | 14 October 2026 | The thaw grace covers Rimebind only; the trophies' Frost boon (accepted), `dazzle`, DAZE and the Ice Breaker wave keep their own slows. The alternative, for the owner: one grace for every Concordance and trophy slow | Proposed | B-06, B-10 |
| B-P11 | 14 October 2026 | Brace: +0.5 knockback resistance for 100 ticks, one hit, not stacking | Proposed | B-06 |
| B-P12 | 14 October 2026 | Brace's order: the Yeti takes it before his blow's `hurtServer`; other blows remove it after damage; a harmful Concordance push uses it; a helpful push neither uses it nor is shortened by it | Proposed | B-06, B-09 |
| B-P13 | 14 October 2026 | Preserve: `touch struck`, the crucible only, holding its temperature band for 60 s | Proposed | B-07 |
| B-P14 | 14 October 2026 | No Thallite Focus in Chunk B (it stays in Thallite slice 5); the crafted upgrade (09:94) is the water foci | Proposed | B-07, B-10 |
| B-P15 | 14 October 2026 | Stratawrighting reads `#jugcraft:assay_specimens`, which gains raw Thallite, the Thallite ingot, stone and deepslate; no Strata specimen tag | Proposed | B-05 |
| B-P16 | 14 October 2026 | Rimekeeping understood by comparing a cold and a warm brew or by studying the Glacier Hall; mastered by varied practice, the column charge elective | Proposed | B-05 |
| B-P17 | 14 October 2026 | Winter Brine needs no salt: a snowball carries the Rime, and salt has its own feature switch (`config/JugcraftConfig.java:24`) | Proposed | B-08 |
| B-P18 | 14 October 2026 | A stir above the cold band keeps part of the dissolved Rime: warm 600, hot 300, searing 0 per thousand (starting values) | Proposed | B-08 |
| B-P19 | 14 October 2026 | Frostward lasts as Ember's Fire Resistance brew does: 1200 ticks a unit, at most 2400 | Proposed | B-08 |
| B-P20 | 14 October 2026 | The Yeti's knockback respects only Brace, not all knockback resistance: the plan's former B-D3, not asked of the owner. Its source is 04:186's "bracable knockback" (the plan cited 04:184) | Proposed | B-09 |
| B-P21 | 14 October 2026 | Rimebind on the Yeti: once per thaw window, his next wind-up is a few ticks longer (set by test) | Proposed | B-09 |
| B-P22 | 14 October 2026 | The Brace caster earns a deed, one per occurrence, when a Brace they put on another player absorbs a Yeti push | Proposed | B-09 |
| B-P23 | 14 October 2026 | The kit's sets: freeze-immune; no Spell Power or knockback resistance; protection no greater than the Pyromaniac's (light), leather's (medium) and iron's (heavy), toughness 0 | Proposed | B-10 |
| B-P24 | 14 October 2026 | The water foci are the preservation focus: Preserve holds 90 s (lesser) or 120 s | Proposed | B-10 |
| B-P25 | 14 October 2026 | The Water Bangle is the short Brace accessory, a Brace every 30 s with Rimekeeping understood. The alternative is the owner's own text: a chill on each blow, like the Fire Bangle | Proposed | B-10 |
| B-P26 | 14 October 2026 | A Frost boon slow counts as Rime practice, and Rooted holding as Strata practice; no numbers change | Proposed | B-05, B-10 |
| B-P27 | 14 October 2026 | The Frost Knight and Wight King sets stay creative-only (continuing A-D10) | Proposed | B-10 |
| B-P28 | 14 October 2026 | Nothing is added to `earthen_ground`; no Thallite or trophy number changes | Proposed | B-10 |
| B-P29 | 14 October 2026 | Winter Brine's dossier fact is text and icons, with no recipe grid | Proposed | B-02, B-08 |

## How this contract was checked

- Re-read at `d4c07bf56` for this page: the Yeti King's fight, rewards, reset, death, hurt, chill, charge, push and boss
  bar; the whelp; the Yeti's registration and loot; the Mitten; the glacial spike; the Tyrant's reward, reset, death and
  hurt; Vesperine's Chunk A pattern and the deed keys; `Encounter`, `LairRewards`, `Lairs` (hooks, restart, edges);
  `ConcordanceEffects` (creature and block paths, tolerance, lasting effects, push), `Tolerance`, the resistant tag;
  `Mixture`, `Band`, `Heat`, `Brew`, the crucible's tick and save; `tools/concordance_alchemy.py`; `ArmItem`'s boon,
  `ArmVariants`, `JugcraftArms`, `Wayfaring`'s wave; `Plan.scaled`, `Compiler`, `ComposedSpells`, `Component`,
  `Ember`; `Baselines` and `Benchmark`; `Hexes`, `Sympathy`'s guard, `EmberGear`; `ConcordanceProgress`, `Assaying`,
  the `assay` research and practice and its generator; `LairDossiers`, `FirstLightRoute`, `PageWriter`;
  `tools/acquisition.py`, `check_acquisition`, the invocation checks, `check_alchemy`, `check_delivery`,
  `concordance_delivery.py`; the Initiate's Wand limits and the Adept's Attunement; the loot tables; the freeze and
  earthen tags; the test methods named; the Thallite, Ember regalia, Cinder Tyrant and Glacier Hall records; the
  owner's water-set files and their names in the library's language file; plan chapters 02, 03, 04, 06, 07, 08, 09 and 10
  at the cited lines; mc263src `LivingEntity` (hurt, death, knockback, freezing) and `Entity.push`.
- Checked again for this page's review on 14 October 2026: every code, data, test and plan citation above at
  `d4c07bf56`; the research-unlock check; `Examination` and the specimen tags; the World Designer client test and
  `world/design/`; the Kiln Seal's rules; Ember part 1's commit (67 files); `ConcordanceSpells`' cost settlement; the
  Jade crucible snapshot; the icon files' sizes and the runtime spell and status icons; the owner's `gaia` recordings;
  `bare`, `drift` and `driftAll`; the Frost Horn recipes; the salt feature switch; the existing invocations' signatures
  and Focus costs.
- Read by REST on 14 October 2026 (the PR list at 00:30 UTC): the open pull requests and their file counts; #352's body
  and record, and at its head the files compared above and the Mire Hag's and Crypt Lich's reward fields; the file lists
  of #323, #330, #343, #351, #354 and #355; #337's check runs on `d4c07bf56`; main's head.
- Taken from the survey reports or the plan's review without re-reading, and marked *(survey)*: #322's conflicts with
  #337; #336's lamp listener; #330's audit; #332's Levitation; #344's contents; the Thallite leaching pull requests; the
  merge points in `tools/concordance.py`; the Conclave check; the context of `pewatermagic.ogg`; the heavy model's
  unchecked fit; whether each listed recipe draws one grid (the Expeditions count); what an examination records; the
  sluice's turner; the Winter Crown pattern.
- Undetermined until something runs or is read: lair dimensions in a world made from an exported preset; charge
  behaviour at the dais and walls; powder-snow walking for boots other than leather in 26.3; the Yeti under Levitation;
  whether a game-test player can be killed so that a real death event fires; the state of the pull requests after
  00:30 UTC on 14 October.
