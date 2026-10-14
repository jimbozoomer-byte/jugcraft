# Concordance Chunk B: winter and stone (contract and handoff)

Status: contract written. Unit 1 (B-01 and B-02, the Yeti King and the Cinder Tyrant) and unit 3 (B-05 to B-07) are
implemented on branch `claude/chunk-b-winter-stone`, based on main `feb6d04fb`
([#352](https://github.com/jimbozoomer-byte/jugcraft/pull/352), which carries Chunk A); not merged. Their tests pass in
CI: run 38514144531 on `579ab5f6f` (unit 1) and run 38519234934 on `06dc5ea5f` (units 1 and 3) succeeded in every job
(see the Validation plan). The first run, 38509414542, failed only the two four-player tests, which expected more health
than the game's 1024 cap allows; the tests now expect the cap. Every other unit is Not started. The contract was written
at `d4c07bf56`, the head of Chunk A ([#337](https://github.com/jimbozoomer-byte/jugcraft/pull/337)), when main was
`b73f51c8a` ([#321](https://github.com/jimbozoomer-byte/jugcraft/pull/321)).
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
| 1 | B-01, B-02 | The Yeti King on Chunk A's encounter credit, kept rewards and exactly-once payment; then the Cinder Tyrant in a separate commit (B-D1); both keepers' dossiers, route step and JEI pages | `lair-encounter-credit.md`, `yeti-king.md`, `cinder-tyrant.md` | Implemented, not merged; CI passed (runs 38514144531 and 38519234934): B-01 and B-02 committed for the Yeti King and the Cinder Tyrant, each keeper in his own commits (B-D1) |
| 2 | B-03, B-04 | Glacier Hall access (ordinary and designed worlds) and lifecycle; the Yeti fight held to the completion evidence (charge and columns, footing, cleanup, frost) | `glacier-hall.md`, `yeti-king.md` | Not started |
| 3 | B-05, B-06, B-07 | Rime and Strata research and evidence; Rimebind and Brace; Preserve on the crucible, with Jade's hold. The crafted upgrade (09:94) is unit 4's water foci (B-P14). One unit, because research may unlock only invocations that exist and each invocation must be unlocked by its research (`tools/check_mod_data.py:10321-10323,10688-10693`) | [arcane-concordance-rime-strata.md](arcane-concordance-rime-strata.md) | Implemented, not merged; CI passed (run 38519234934) |
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
- **The Tyrant, in a separate commit** (B-D1, done): the same changes at
  `CinderTyrantEntity.java:270,492-497,577,1533-1547,1570,1602-1618,1686-1688`, `CinderlingEntity.java:137-140`,
  `CinderTyrantLoot.java` (`BOSS = "cinder_tyrant"`; `pay`; `roll` by origin beside the entity overload; `:74` uses
  `tyrant.position()`) and `JugcraftTyrant.java:41`. His fight begins again after a load once he has landed: a load
  turns waking into the Kiln and erupting into the Eruption (`CinderTyrantEntity.java:1807-1811`). Waking on a blow
  still needs a player (`:1677`), so a pet's bite does not wake him.
  - No deed (B-P4): nothing records who turned a sluice. `SluiceGateBlock` is a plain block with no block entity
    (`SluiceGateBlock.java:46`); turning it writes only its flow and its trough's flood (`:243-259`), and the quench
    reads only the flood (`CinderTyrantEntity.java:1050-1062`).
  - Of his harm, his blows and his own fire are the fight's hurt (the source's entity is he or a Cinderling: the sweep
    and slam, `mobAttack`; the breath, wave and fire patches, his flame `DamageSource`, `:474-476`; the gobs and
    cinders, `mobProjectile` with him as owner, whose entity is the owner in vanilla's build, not asserted by a test; a
    Cinderling's bite, vanilla's `mobAttack`, likewise). The slag's heat (`hotFloor()`, `MoltenSlagBlock.java:56`) and
    burning (`onFire()`) have no entity, so healing them earns nothing (B-P5). Giving them him as their cause was
    rejected: the slag would scale with difficulty, knock players away from him, turn their pets on him and count the
    crucible's permanent slag, and burning would need a mixin.

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
- `CinderTyrantGameTests`, the same for the Tyrant without the column cases: `supportSharesHisHoardOnce` (a healer of
  his Tail Sweep qualifies; one still down as he falls is kept and paid on their feet; one who left is kept in the
  world; a bystander, a wanderer and the one he only hurt are not paid; a second `die()` pays nobody),
  `aKillingBlowAloneIsPaid`, `aPetsBlowIsItsOwners` (on a Cinderling, and the felling bite on him; a wild wolf's bite
  is no one's), `healingHisSlagAndBurningEarnsNothing` (B-P5: mending slag and burning earns nothing, mending his
  Kiln Breath qualifies), `fourPlayersAreEachPaidOnce` and `aFightLoadedAwakeIsRecordedAfresh` (loaded waking, he
  lands in the Kiln). `Encounter.REACH` (64) is wider than his arena's spacing from other tests (at least 43.5 blocks
  from his centre), so exact counts and experience shares are asserted only for a fight begun and ended in one
  synchronous block. `LairEncounterGameTests.aKeptCinderTyrantRewardIsSavedAndPaid`: a kept `cinder_tyrant` Due
  survives a save and is paid by his payer.
- Unchanged, and passed in CI with B-01 (runs 38509414542, 38514144531 and 38519234934):
  `YetiKingGameTests.hisLootIsEachParticipantsOwn` (`:416`), the Tyrant's test of the same name
  (`CinderTyrantGameTests.java:673`), and the kill in `YetiKingClientGameTests` (`took` then `genericKill`,
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
| `concordance/encyclopedia/FirstLightRoute.java:92-93,126-127,142` | `FIRST_WINS` removed: the lair step is done by `LairDossiers.anyBeaten` (every keeper's first-win advancement) and shown when `LairDossiers.canOpen` (any token's recipe) |
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
  Exact counts, every keeper found and beaten, with no town, kept share or visit: 71 rows (Vesperine 20, Madame
  Tatterlace 17, the Yeti 17, the Tyrant 17) and 12 grids; at worst about 79 rows (a town's three sales, a kept share
  for each keeper, one fight line) against the 95 shown before the "more" row (`EncyclopediaPayload.Page.MAX_ROWS` 96).
  The test asserts the grids exactly and that nothing is dropped, not a row count, which B-P29's fact would move.
  `check_encyclopedia` counts the grids statically and refuses a table past `MAX_GRIDS`. A fifth keeper needs
  `MAX_GRIDS` and `MAX_ROWS` raised as a recorded bound change, not a page per lair (`MAX_PAGES` is 6,
  `EncyclopediaPayload.java:27`, with four taken). After the Glacier Hall's commit, before the Kiln's: 54 rows and 10
  grids.
- `check_mod_data.py` mutation checks, as in Chunk A.

**As built (the Glacier Hall's commit; passed in CI runs 38509414542, 38514144531 and 38519234934):**
`EncyclopediaGameTests.theGlacierHallsDossier`, `eachRiteSaysWhenItWorks`, `aWinOverAnyKeeperIsTheLairStep` and
`theExpeditionsPageHoldsEveryKeeper`; `GlacierHallGameTests.theHornIsNoHalloweenRite`;
`YetiKingGameTests.hisLootIsWhatHisPagesSay` (400 shares, each count within five standard deviations of its page's
chance; never both trophies). Run here: `check_mod_data.py`, `check_repository.py` and `concordance_journey.py` pass; on
a scratch copy, 30 faults against `check_acquisition` and 8 against `check_encyclopedia` (the Yeti's trophies in the
tool, his mitten moved into another keeper's entry, his rite made a Halloween one, the grid bound lowered to 9, his rite
recipe needing his fur, a missing lang key, his snow sale dropped from the entry, the Hollow Acre's season words
dropped) were each reported, and #336's Mire Hag table with no pages was not. Adding `white_wool_from_yeti_fur` to his
`made_from` passes at this commit (11 grids of 12); with the Kiln's two it would be 13, which the audit refuses.

**As built (the Cinder Kiln's commit; passed in the same three runs):** `EncyclopediaGameTests.theCinderKilnsDossier`,
the Kiln Seal and the Cinder Kiln added to `eachRiteSaysWhenItWorks`; `CinderKilnGameTests.theSealIsNoHalloweenRite`;
`CinderTyrantGameTests.hisLootIsWhatHisPagesSay` (400 shares, as the Yeti King's). `aWinOverAnyKeeperIsTheLairStep` and
`theExpeditionsPageHoldsEveryKeeper` read `LairDossiers.Boss.values()`, so they now take in the Tyrant: four first wins,
and 12 grids of 12 in 71 rows (79 at most). Run here: `check_mod_data.py`, `check_repository.py` and
`concordance_journey.py` pass. On a scratch copy, 13 faults were each reported: 5 against `check_acquisition` (his row
naming an objective; a deed credited in `lair/tyrant`; his trophies split into two pools; the Salamander Charm's chance
moved; his payer dropped) and 8 against `check_encyclopedia` (one trophy in the tool; the Salamander Charm dropped from
his Java entry; his rite made a Halloween one in the tool; `KilnSealRite` reading `Lairs.offSeason()`;
`magma_cream_from_tyrant_scale` added to his `made_from`, 13 grids; his lair's leave words missing; his Java entry
removed; a sale named in it).

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

Built with B-06 and B-07 as unit 3 ([record](arcane-concordance-rime-strata.md)): a research entry may unlock only
invocations that exist, and each invocation must be unlocked by its research (`check_invocations`). B-05 to B-07 were
rewritten on 14 October 2026 to what the unit built, on main `feb6d04fb`; the choices that changed are in the revised
B-P12, B-P15 and B-P16, the new B-P30 to B-P35, and the record's "Changes from the plan".

**What the player sees.** Two research entries with typed evidence, each piece counted once by its key (02:129, 02:153),
and the next step named in the journal and the codex.

| Research | Tradition, stage | Encountered | Observed | Understood (and what it unlocks) | Mastered |
|---|---|---|---|---|---|
| `jugcraft:rimekeeping` (Rime) | Rimekeepers, practitioner; requires First Light understood | Examine an item in the new `#jugcraft:rime_specimens` (snowball, snow block, ice, packed ice, blue ice) | Examine something in `#jugcraft:ember_specimens`, something that holds fire (02:132's warm surface); either may come first | Any of: study snow or ice at the Lampwright's Bench; notes; the **winter survey** `jugcraft:winter_survey`, gated by First Light alone so either route advances understanding (02:136): `glacier_hall`, arriving in the Glacier Hall by any route (Fabric's `AFTER_PLAYER_CHANGE_LEVEL`; no lair code), or `crucible`, a dose carrying Rime bottled by hand (`CrucibleBlockEntity.bottle`). Unlocks Rimebind and Preserve | `jugcraft:rimework`, all three kinds: `preserve:cold` and `preserve:heated` (Preserve on a crucible holding a mixture, in the cold band or above it: 02:133-134's preserve and compare), `restraint` (a Rimebound taking on a hostile creature at normal tolerance, 02:135). The Yeti column elective (02:137) comes with B-09 |
| `jugcraft:stratawrighting` (Strata) | Stratawrights, practitioner; requires First Light understood | Examine an item in the new `#jugcraft:strata_specimens` (granite, diorite, andesite, tuff, raw Thallite; 02:140) | Three distinct | Study at the bench; notes. Unlocks Brace | `jugcraft:anchoring`, three of: `ground:living` and `ground:earthen` (a Brace taking on someone standing on that ground, read with Thallite's ground tags; 02:141-142), `held` (a Brace the caster put on meets a push); and the electives `ally` (a party member braced), `foundation` (a ritual completed at a Circle Anchor, 02:143) and `rooted` (struck in Earthbound Thallite on natural ground, B-P26) |

**Reconciled with `assay`** (09:13; revised B-P15):
- `#jugcraft:assay_specimens` holds `#jugcraft:strata_specimens`, so one examination serves Assay and Stratawrighting
  (the research engine applies each piece of evidence against every entry's rules); a test confirms it. Assay becomes
  easier to begin; its mastery is unchanged.
- Nothing new reaches the Assayer's Scale, which weighs against the equivalence catalogue: stone and raw Thallite are
  examined, never weighed.
- Cobblestone, stone, cobbled deepslate and deepslate are never specimens: sneak-use in the air examines, and players
  bridge with them (`check_strata`). Nor is the Thallite ingot.
- Strata can be completed with the `thallite` feature off: the four vanilla stones suffice.

**Progression** (revised by the unit review): the Glacier Hall route understands Rimekeeping without a study (02:136),
but Rimekeeping's mastery needs the Alembic Arts (Preserve counts a crucible holding a mixture, and only an alembist
fills one by hand), which are understood at the bench or from notes. The graph's `rimework` route asks for them, so the
bench stays mandatory at Adept; there is no bench-free Adept.

**Not in this ticket:** Strata's civic-project or structural-boss-opening step (02:144); the Yeti column elective
(02:137), with B-09. See Deferrals.

**Files:** new generators `tools/concordance_rime.py` and `tools/concordance_strata.py`, merged in
`tools/concordance.py`; `tools/concordance_progression.py` (the three practices, the winter survey's two routes in
`graph()`, sources for snow, ice, stone, goat horns, raw Thallite and Yeti Fur); `tools/concordance_conclave.py`
(`TRADITIONS`); `tools/concordance_equivalence.py` (Assay's texts); Java `concordance/rime/Rime.java`, `RimeSurvey.java`
and `concordance/strata/Strata.java`, registered beside `Ember.register()`; `Examination`'s messages; the guide block,
the journey routes and the mandatory table, regenerated; `check_rime` and `check_strata`, mutation-tested.

**Tests:** `ConcordanceRimeStrataGameTests`; `ConcordanceProgressionGameTests` (the live graph, First Light alone gating
winter and stone, one examination serving both entries, an Adept by the Glacier Hall);
`ThalliteGameTests.thalliteIsAStrataSpecimen` (09:112); `GlacierHallClientGameTests` (arriving is the field study).

**Save effects:** new research ids, evidence keys `practice:jugcraft:{winter_survey,rimework,anchoring}:<key>` in the
existing knowledge record, and advancements `concordance_<research>_<state>`, stable once released.
`rimeAndStrataResearchSurviveASave` round-trips them; an older build keeps the unknown entries and relearns nothing from
them.

### B-06. Rimebind and Brace (plan B2, part 2)

**Budget.** Ten invocations before and thirteen after, against a cap of 16 (an instrument's `Tunings.MAX`,
`check_invocations`); signatures unique; the six roles still covered. Roles (B-P33): Rimebind **defense**
(`Benchmark.choose` never casts a utility), Brace **support**.
- **Rimebind:** `ray struck rimegrip`, signature `ray/struck:status/harmful`; work 2 (the ray 1, a status on one target
  1); persists 100 (the binding and its thaw).
- **Brace:** `here allies bracing`, signature `here/allies:status/helpful`. `allies` is the caster and their party only
  (`concordance/ComposedSpells.java`), so the party is the consent (B-P7). Work 4; persists 100.
- Both are authored: no word a player composes is added (B-P6).

**Rimebind** (B-P9, B-P10, B-P34):
- `jugcraft:rimebound` slows by 20% a level for 60 ticks through an attribute modifier on the effect,
  `jugcraft:rimebound` (the pattern of `agriculture/Hexes.java`).
- **Grace:** every Rimebound that takes starts a hidden `jugcraft:rime_thaw` for its own time plus 40 ticks
  (`Rimebind.bound`, from the boundary's STATUS branch), and a `ConcordanceEffects` Guard (the pattern of
  `concordance/sympathy/Sympathy.java`) refuses Rimebound while it lasts: IMMUNE, nothing spent, the cast fizzles at no
  cost. At most 3 s bound in every 5, shared by every caster. The grace covers Rimebind's slow only (B-P10).
- **Targets** (B-P34): the shared boundary's (monsters; creatures the caster could strike by hand; players only under
  PvP and outside the party), not the plan's "hostile ordinary creature" only. Only a hostile creature (`Enemy`) at
  normal tolerance counts as restraint.
- **Tolerance** (B-P8): only the Yeti King and the Cinder Tyrant join `#jugcraft:concordance/resistant` (beside the iron
  golem, ravager and piglin brute). That halves every harmful Concordance status and push on them, not only Rimebind's:
  Rimebound lasts 30 ticks and its thaw 70; Smoulder on the Yeti (the Tyrant is immune to fire), Dazzle and the Ice
  Breaker wave's slow (`concordance/trinket/Wayfaring.java`) are halved; pushes already did nothing (knockback
  resistance 1.0). The Ember, trinket and keepers' records say so, and `onlyTheYetiAndTheTyrantResist` covers it.
  Vesperine, Madame Tatterlace, the Mire Hag and the Crypt Lich are unchanged (`check_rime`).
- The Yeti moves by fixed steps, not by his movement-speed attribute (`YetiKingEntity.move`; the charge), so a slow
  alone does nothing to him (an inference until tested); B-09 gives him a visible change instead of a silent one
  (04:188).

**Brace** (B-P11, B-P12, B-P30): `jugcraft:braced` for 100 ticks, +0.5 knockback resistance (the modifier
`jugcraft:braced`), exclusive. It is spent by the first push it meets, braces the rest of that server tick and goes at
its end (`ServerTickEvents.END_SERVER_TICK`); spent between ticks, by a push in a cast's packet, it goes when the next
tick starts (`START_SERVER_TICK`). Vanilla's knockback respects knockback resistance (mc263src
`LivingEntity.java:1543-1544`).
- **A blow vanilla knocks back** spends it (`ServerLivingEntityEvents.AFTER_DAMAGE`: full damage this tick, not
  `#minecraft:no_knockback`, not wholly stopped by a shield). Vanilla's knockback inside `hurtServer` and the extra
  knockback of sprints, the Knockback enchantment and mob attacks that follows it (mc263src `LivingEntity.java:1205`)
  fall in the same tick, so the whole blow is braced (B-P30).
- **A harmful Concordance push** reads the resistance with the Brace and spends it. **A helpful one** (one's own
  Flashstep, a friend's helpful push) reads it without (`Brace.resistanceWithout`: the effect's own modifiers off for
  the reading and back at once) and leaves it (B-P12).
- **A scripted push** asks through `Brace.spend(target)`, which spends it and returns the share to let through, 1 − 0.5
  a level, and is safe to call more than once a blow. B-09's Yeti calls it inside his `push`, before or after
  `hurtServer`: the order no longer matters.
- **The Leaf Blower's stream** (`agriculture/LeafBlowerItem.java`), which reaches another player only under PvP, spends
  it with `Brace.spend` before it reads knockback resistance.
- **Accepted exception** (B-P30): vanilla's knockback that comes without a damaging blow (the review names a mace
  smash's bystanders) reads the Brace's resistance but does not spend it.
- Teleports are not pushes: `Lairs.edges`, the Grey Mist, portals and the whirl move the braced as anyone.
- Who put it on is kept in a non-saved attachment, `jugcraft:brace_caster` (`Brace.caster`, for B-09's deed). The
  caster's practice: the target's ground (`ground:living`, `ground:earthen`), an ally braced, and `held` when it is
  spent.
- Its texts keep it apart from the war fork's BRACE trait and the tower shield's brace (`weapons/JugcraftArms.java`);
  "Stonebrace" is offered at the checkpoint as a display name.

**Costs** (hypotheses, 02:73): Rimebind 4 Focus (3 mastered), 2 s cooldown; Brace 3 (2), 8 s; on the scale of today's 3
to 7, settled once when Spell Engine consumes the cast's cost (`concordance/ConcordanceSpells.java`), however many it
braces.

**Benchmark** (B-P32). `Benchmark` models Rimebound (a fifth of a foe's steps a level for the binding, then the thaw
refusing Rimebind; every other harmful status keeps the 15% rule), starts no cast that would change nothing (a status
already held by its stacking rule; Rimebind on a thawing foe), casts a defense with a harmful step at any foe in reach,
and measures knockback taken without moving anyone (`Baselines`' "Knockback (blocks/min)" column). New acceptance
checks: 5, Rimebind's control at most 7.8 foe-seconds a minute; 6, Brace takes knockback off beside the unkillable
brute. The baselines record holds CI's tables (run 38519234934): Rimebind alone 6.6 foe-seconds a minute, Brace alone
5.2 blocks of knockback.

**Assets** (B-P31). The owner's files byte for byte, at their own sizes: Rimebind
`ars_jimbaux/textures/item/freeze.png`; Rimebound `ars_jimbaux/textures/mob_effect/freezing.png`; the Thaw
`ars_nouveau/textures/mob_effect/soaked.png`; Brace and Braced `ars_jimbaux/textures/item/gravity.png` and
`mob_effect/gravity.png` (the owner's own pair, byte-identical). Spell Engine draws a spell icon into 16×16 from the
whole texture, so nothing is scaled. Brace's release is the owner's `gaia_1.ogg` to `gaia_4.ogg`; Rimebind's release and
the gathering cues are synthesised, as Ember's were. The earlier proposals (`shield.png`, `shielding.png`, `frozen.png`,
scaled to 32) are checkpoint alternatives: shielding reads as "reduces damage", which Brace never does, and frozen as
"cannot heal".

**Tests:**
- `ConcordanceEffectGameTests` (09:112): `rimebindThawsBeforeItBindsAgain`; `onlyTheYetiAndTheTyrantResist`;
  `braceHoldsTheWholeBlow`; `braceIsTakenBeforeTheBlow`; `harmfulPushesSpendTheBraceAndHelpfulOnesDoNot`;
  `rimeAndBraceSurviveASave`.
- `repeatedSlowDoesNotPermanentlyImmobilize` (09:96), scoped to Rimebind (B-P10): three casters recasting on one target
  every tick for 15 s; no bound run longer than 3 s, no gap shorter than 2 s. The trophies' Frost boon, `dazzle`, DAZE
  and the Ice Breaker wave stay outside it.
- `braceDoesNotCancelBoundaryEnforcement` (09:96): the Grey Mist sends a braced player home and the Brace is untouched;
  in `GlacierHallClientGameTests`, `Lairs.edges` returns a braced player past the bounds without spending it, and
  leaving and following a whirl still move them.
- `ConcordanceInvocationGameTests`: `focusIsChargedOnce` (09:96) and `braceDoesNotShortenFlashstep`.
- `ConcordanceRimeStrataClientGameTests`: the icons in the client's resources; spell bar, HUD and inventory screenshots.

**Save effects:** new mob-effect, attribute-modifier, component, invocation and spell ids, stable once released. The
three effects save in `active_effects`, which vanilla puts back without re-adding their modifiers; those come back from
the saved `attributes` (mc263src `LivingEntity.java:780-786`; `rimeAndBraceSurviveASave` checks it). `brace_caster` is
not saved. An older build can drop all of a creature's effects on meeting an unknown one and keep their modifiers (a
fifth off speed, +0.5 knockback resistance) until `/attribute ... modifier remove` takes them off (the record's
downgrade guidance).

### B-07. Preserve (plan B2, part 3)

- **Shape:** `touch struck rimehold`, a helpful alteration; signature `touch/struck:alteration/helpful` (Hearthspark
  holds `ray/struck:alteration/helpful`). `touch struck` reaches a block. Role **utility** (B-P33); 3 Focus (2
  mastered), 0.6 s cast, 3 s cooldown, no tunings; work 4, persists 1200.
- **Dispatch:** on Rime's school, `spell_power:frost`, as Ember's alteration is on fire: `Rime.preserve` in the
  boundary's block path, before the default alteration (which removes fire, and only fire), and `Rime.preservable` in
  `accepts`. `check_rime` holds the order and that only the boundary calls `Rime.preserve`.
- **Target** (B-P13): `#jugcraft:concordance/preservable`, holding only the Alembic Crucible. Preserve holds its
  temperature band for 60 s: the heat still moves a degree a tick toward its source, kept inside the band (`Band.keep`),
  whatever burns or goes out beneath. That is Rime's reliable workstation loop (02:54).
- **The hold** (B-P35): `Rime.HOLD_TICKS = 1200`, a Java constant (an alteration takes no duration, so Spell Power does
  not scale it). A recast restarts it and never adds. It runs on the world clock (`held_until`, game time). The crucible
  accepts holds up to `Rime.MAX_HOLD_TICKS` (2400, `EffectSpec.MAX_DURATION`) and cuts anything further ahead, so
  B-P24's water foci need one line in `Rime.preserve`.
- **Refused:** anything else returns `Result.NOTHING` and changes nothing: chests, barrels, hoppers, furnaces, machines
  and every other inventory (02:77; 06:96; 09:96). A crucible the caster may not change returns `NOT_ALLOWED`
  (`Authority.mayChange`: claims, protected towns, spawn protection, adventure mode, an absent caster).
- **Practice:** a Preserve on a crucible holding a mixture is Rimekeeping's cold work, `preserve:cold` or
  `preserve:heated` by its band.
- **Guidance:** Jade's crucible lines and the crucible's empty-hand reading show the seconds held (moved here from
  B-08).
- **No Thallite Focus and no crafted upgrade here** (B-P14). With the Initiate's Wand's limits and no upgrade a Thallite
  Focus would add nothing; the approved record pairs it with Earthen Steps and Bulwark in slice 5 (`thallite.md`). The
  chunk's crafted upgrade from existing materials (09:94) is unit 4's water foci (B-10, B-P24). `thallite.md` records
  this.

**Tests:** `preservationCannotWrapArbitraryInventories` (09:96); `preserveHoldsOneBandAndExpires` (with Jade's seconds
when Jade is loaded); `preserveCountsTheCrucibleBand`; `preserveIsChargedOnceAndCountsOnce`; in
`ConcordanceEffectGameTests`, `preserveNeverRemovesABlock` and `preserveIsRefusedWhereItsCasterMayNotBuild` (renamed
from `preserveIsRefusedUnderTownProtection`: it covers a neighbour's claim, an absent caster and adventure mode).

**Save effects:** the crucible's optional `held_until` (game time), written only while held and read with a default of
0; it still stamps version 1. `ConcordancePersistenceGameTests.theCrucibleKeepsPreservesHoldThroughASave`: a round trip
keeps the hold; a save without the field, or also without the version, reads 0 and keeps the temperature; an ended hold
writes nothing; a forged hold far ahead is cut to 2400 ticks. #323's crucible change reached main with #352, and unit 3
is built on it.

**Review boundary (unit 3):** magic data, effects, the crucible's hold, Jade and tests; no lair code (the hall is heard
through Fabric's level-change event).

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

**Guidance:** JEI's alchemy ingredient pages (snowball, amethyst, gloamcap) name Frostward; Jade's crucible lines
already show Preserve's hold (moved into unit 3, B-07); the Glacier Hall dossier gains a preparation fact naming Winter
Brine and the Mitten (text and icons, B-P29).

**Tests:** `aFrostwardDrinkerIsNotChilledByTheYeti` (`YetiKingGameTests`); `winterBrineNeedsTheColdBand` (stirred cold it
gives Frostward, stirred warm it loses it); `aRimeBottleFromBeforeStillGivesResistance`; must-fail economy loops
(06:146-158): Yeti Fur makes Frostward no cheaper (fur already cheapens the horn: `recipe/frost_horn_from_fur.json`
takes one gold and no leather where `frost_horn.json` takes two of each); the alchemy tests that expect
Resistance from Rime, updated.

**Acceptance:** "one useful cold preparation route and a non-boss resistance option" (09:106); "Every required cold
preparation has a non-boss route" (09:110).

**Save effects:** a new effect id; the property change reaches only new bottles.

### B-09. Optional school counters in the Yeti fight (plan B3; only after B-04 is green, 09:102)

- **Brace** (B-P20, B-P12, B-P30): `Brace.spend` inside `YetiKingEntity.push` (`:829-832`), before or after the blow's
  `hurtServer` (a spent Brace braces the rest of that tick), halves that blow's push, every push through it, the spike's
  lift included. Other knockback resistance stays
  ignored, so netherite, Rooted, shields and the Ice Breaker change nothing: this is the plan's "bracable knockback"
  (04:186).
- **The Brace caster's deed** (B-P22; 02:187; 04:139, 04:156): when a Brace that one player put on another absorbs his
  push, the caster earns `encounter.deed(caster, "brace " + braced player + " " + game time)`, one key per occurrence.
  The caster is `Brace.caster`, kept in the non-saved attachment `jugcraft:brace_caster` (unit 3), so a Brace carried
  through a relog keeps its time but credits no one.
  A self-Brace earns nothing, as healing oneself does. Lair support credit sees only restoration and protection
  (`ConcordanceEffects.java:223-228`), so without this deed a Brace caster could never qualify.
- **Rimebind transforms, and is never silently dropped** (B-P21; 04:188): once per thaw window, a rimebound Yeti's next
  wind-up is a few ticks longer. The number is set by test and called untuned until people play it (04:194).
- **The column elective** (02:137; B-P16): a Yeti charge broken on a column becomes Rimekeeping evidence, the key
  `column` in `jugcraft:rimework` (an elective: the mastery keeps needing its own three kinds), with its recorder and
  its codex line, added here rather than in unit 3 because it needs B-01's deed and lair code.
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
- **Trophies** (09:106): a slow from the Glacier Maul's or Rimeclaw's Frost boon counts as Rime practice, an elective
  key `frost_boon` in `jugcraft:rimework` (recorded only for a wielder who understands Rimekeeping, the practice's gate;
  the mastery keeps needing its own three kinds). The boon itself is unchanged
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
- **Combined fixes** (unit 5's list; each found in the units' CI and not fixed yet):
  - The encyclopedia's home page (`jugcraft_encyclopedia_home`, run 38519234934, shard 0): the Magic card's link is cut
    to "Open Magic: the Arcane Concorda…", and the body's last line is clipped above it (the Machinery card's body also
    stops mid-line). A Chunk A defect.
- **Not run, expected:** two clients in one fight; play by hand; timings against guns and mobility items; completion
  times (04:192-194).
- **The checkpoint:** 300 to 500 words under the fixed headings (10:87-121), with 2 to 4 screenshots and a short
  demonstration route. Ask about movement feel, difficulty and the next school (09:114); do not ask whether to run save
  tests. The pull request states the player result, progression links, actual validation, save compatibility, known
  limits and the AI attribution (CLAUDE.md).

## Interfaces expected to change

- **Saved data:** no new saved format. New boss ids `yeti_king` and `cinder_tyrant` inside `jugcraft:lair_rewards`; the
  crucible's optional `held_until`; saved mob effects and their attribute modifiers; new research keys; new item ids.
  Not saved: the attachment `jugcraft:brace_caster`.
- **Registries (stable once released):** mob effects `jugcraft:rimebound`, `rime_thaw`, `braced` and `frostward`, and
  the attribute modifiers `jugcraft:rimebound` and `jugcraft:braced`; components `rimegrip`, `bracing` and `rimehold`;
  invocations and spells `rimebind`, `brace` and `preserve`; research `rimekeeping` and `stratawrighting`, with their
  practices (`winter_survey`, `rimework`, `anchoring`) and advancements; fifteen kit items; tags
  `concordance/preservable`, `rime_specimens` and `strata_specimens`; additions to `assay_specimens` (which nests
  `strata_specimens`), `concordance/resistant`, `minecraft:freeze_immune_wearables` and the trinket slot tags.
- **Shared code:** `YetiKingLoot` and `CinderTyrantLoot` gain `BOSS`, `pay` and `roll` by origin (the entity overloads
  kept); `LairDossiers.Boss` gains `trophies` (one, or a pair), `offSeason` (null for a rite that works all year) and `sales`, and
  `LairDossiers` gains `beaten`, `anyBeaten` and `canOpen`; `FirstLightRoute.FIRST_WINS` is removed (B-02 as built);
  `ConcordanceEffects` gains Rime's alteration and `accepts` case, the helpful-push exemption and, after a status takes,
  Rimebind's thaw and Brace's caster; a new Guard; `Band.keep`; `Mixture` gains the Rime heat rule;
  `CrucibleBlockEntity` gains the hold and records Rime brew practice; `YetiKingEntity` gains the Frostward check, the
  taking of Brace and the Rimebind wind-up; `Benchmark` learns `rimebound`, its thaw and knockback. Unit 3's interfaces
  for later units: `Brace.spend`, `Brace.caster`, `Rimebind.RIME_THAW` and `Rime.MAX_HOLD_TICKS`. Practice hooks:
  Fabric's level change (`glacier_hall`; no lair code, and no `Encounter.onDeed`), `CrucibleBlockEntity.bottle`
  (`crucible`), `Rituals.listen` (`foundation`) and `AFTER_DAMAGE` (`rooted`, `held`) in unit 3; `ArmItem`'s Frost boon
  (`frost_boon`) in unit 4; the Yeti's column (`column`) in unit 5.
- **Tools:** `acquisition.py`, `check_acquisition`, `encyclopedia.py`, the `concordance.py` merge points,
  `concordance_alchemy.py`, `concordance_equivalence.py`, `concordance_progression.py`, `concordance_conclave.py`,
  `owner_art.py`; new `concordance_rime.py`, `concordance_strata.py` and their checks.
- **Network:** no new payload. Jade's crucible snapshot carries one more value (unit 3).

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
- **The rite words name the default tags** (`#jugcraft:frost_horn_ground`, `#jugcraft:kiln_seal_lands`): a data pack
  that changes them is not reflected in the dossier, as with the items' tooltips.
- **A used-up token hides its lair again:** discovery is read, not saved (A-D9), so a player who went in and used up
  their Frost Horn (or Kiln Seal) reads as not having found the Glacier Hall (or the Cinder Kiln) until they carry one
  again or win; the same as the Witching Season's lairs, now for every lair.
- **Two bosses stay on the old path:** the Mire Hag and the Crypt Lich keep damage-only rewards in their pull requests;
  after Chunk B, four bosses are on the encounter credit and two are not.
- **Overlap with Chunk C's preservation wash** (09:124): Winter Brine stays a frost counter.
- **Unit 3, as built** ([record](arcane-concordance-rime-strata.md#rollout-and-open-questions)): sneak-use in the air
  now examines snow, ice and four stones, so a sneaking player cannot throw a snowball; the Glacier Hall, whose Frost
  Horn recipe sits under the `agriculture` switch, understands Rimekeeping without a bench, but its mastery needs the
  Alembic Arts, so the bench stays mandatory at Adept; milk clears the Thaw with Rimebound; recasting Preserve each
  minute holds one crucible indefinitely; an older build can drop a creature's effects on meeting an unknown one and
  keep their modifiers until `/attribute` removes them; Spell Engine keys only nine spells, so a player with more than
  nine invocations loses the last ones from the bar (the #250 order question); vanilla's knockback without a damaging
  blow is braced without spending the Brace. B-P24's 120 s hold from a focus worn with the Initiate's Wand exceeds that
  wand's 1200-tick duration limit: unit 4 caps it or records why.
- **Found in CI** (runs 38509414542 and 38519234934): four or more players scale the Yeti King's health to 1050 and the
  Cinder Tyrant's to 1100, but the game caps a creature's health at 1024, so both stop there (the scale is unchanged);
  Spell Engine's spell bar starts at a fixed place left of the screen's bottom centre, so a bar of 5 or more spells runs
  off the left edge of CI's 427-pixel-wide GUI, and 8 or more at 1920×1080 (players can move it in Spell Engine's HUD
  settings; [record](arcane-concordance-rime-strata.md#rollout-and-open-questions)).

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
| 1 | `YetiKingGameTests`, `CinderTyrantGameTests`, `LairEncounterGameTests`, `EncyclopediaGameTests`; passed (runs 38514144531 and 38519234934) | `YetiKingClientGameTests` and `CinderTyrantClientGameTests` unchanged; `EncyclopediaClientGameTests` if the page layout changes (the test is unchanged); each passed by its shard | `check_acquisition`, `check_encyclopedia`, with mutations | A kept Yeti and Tyrant Due across a save; passed |
| 2 | `GlacierHallGameTests`, `YetiKingGameTests` | `WorldDesignerClientGameTests` (an exported preset); `LairClientGameTests` step 7 (the hall); `GlacierHallClientGameTests` | `check_glacier_hall`, `check_yeti` | None needed |
| 3 | `ConcordanceRimeStrataGameTests`, `ConcordanceEffectGameTests`, `ConcordanceProgressionGameTests`, `ConcordanceInvocationGameTests`, `ThalliteGameTests`, `ConcordancePersistenceGameTests`; passed (run 38519234934) | `ConcordanceRimeStrataClientGameTests` (icons, spell bar, HUD and inventory screenshots); `GlacierHallClientGameTests` (the field study; a braced player at the edge, the mist and the whirl); each passed by its shard, but toasts hid the status icons | `check_rime`, `check_strata`, `check_rime_strata_assets` (mutation-tested), `check_invocations`, `check_progression`, `check_conclave`, `check_delivery`, `owner_art.py --check` | The research record (`rimeAndStrataResearchSurviveASave`); saved effects (`rimeAndBraceSurviveASave`); the crucible's field (`ConcordancePersistenceGameTests`); all passed |
| 4 | `ConcordanceAlchemyGameTests`, `YetiKingGameTests`, the kit's tests | The kit worn and slotted (screenshots) | `check_alchemy`, `check_rime_kit`, `check_acquisition` | A Rime bottle from before the change |
| 5 | `YetiKingGameTests` (counters) | `YetiKingClientGameTests` (a charge into a column) | Every check; the CI candidate | Re-run of all of the above |

Every unit also runs `tools/check_mod_data.py`, `scripts/check_repository.py` and the build with its server game tests,
and the build without the optional integrations where it touches an optional boundary.

**Results so far** (units 1 and 3, not merged; each run by workflow_dispatch of the Build workflow, with every client
test class):
- Run 38509414542, unit 1 before its last fix: everything passed but `YetiKingGameTests.fourPlayersAreEachPaidOnce` and
  `CinderTyrantGameTests.fourPlayersAreEachPaidOnce`, which expected a party-scaled health above the game's
  `max_health` cap of 1024 (the Yeti King 420 × 2.5 = 1050; the Tyrant 440 × 2.5 = 1100). `579ab5f6f` made the tests
  expect the cap.
- Run 38514144531 on `579ab5f6f` (unit 1): all 9 jobs succeeded: `mod`, `optional integrations absent`,
  `choose client tests`, client shards 0 to 4, and `client`.
- Run 38519234934 on `06dc5ea5f` (units 1 and 3): all 9 jobs succeeded. `mod`: "All 1639 required tests passed" in 3.994
  min ("BUILD SUCCESSFUL in 6m 3s"); `optional integrations absent`: the same in 3.930 min. The server logs do not name
  the tests that pass; every server test units 1 and 3 name is registered and required (none sets `required = false`),
  so each passed as one of the 1639. `choose client tests` chose all 163 client test classes in five shards, and each
  succeeded (0 in 21m35s, 1 in 39m20s, 2 in 22m46s, 3 in 53m33s, 4 in 23m51s). The client logs print no per-class
  result, but a failing class stops the client with its error and fails its shard's `runClientGameTest`, so each class
  passed by its shard: `EncyclopediaClientGameTests` in shard 0; `CinderKilnClientGameTests` in 1;
  `CinderTyrantClientGameTests`, `LairClientGameTests` and `ConcordanceRimeStrataClientGameTests` in 2;
  `GlacierHallClientGameTests` and `YetiKingClientGameTests` in 3. The baselines tables went to
  [the baselines record](arcane-concordance-baselines.md).
- The server logs' warnings that name the lairs are expected on a game-test server, which has no lair dimensions:
  "[lairs] No dimension jugcraft:glacier_hall: the lair cannot open" twice, and the same for `cinder_kiln` (and other
  lairs). No WARN or ERROR line names Rime, Strata, Brace, Rimebind, Yeti, `LairRewards` or `Encounter`.
- Screenshots (480×270 previews in the job logs): unit 3's status shots were hidden by advancement toasts, and the test
  now clears them (not yet run); its spell bar runs off the screen's left edge (Risks); the Glacier Hall's shots are in
  [its record](glacier-hall.md#verification); the Expeditions shot shows only the top of the page, so no shot shows the
  Glacier Hall's or the Cinder Kiln's dossier (the server tests check their content); the encyclopedia home shot shows a
  Chunk A defect, on unit 5's list (B-11).

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
- A Cinder Tyrant objective deed; the sluice records no turner (B-P4, confirmed in B-01). One would need the turner
  noted as the wheel turns (a transient hook like `Lairs.onRelit`), a key for each turn, and credit only for a player
  already in his fight.
- Slag, burning and freezing as a fight's hurt (B-P5): if the owner wants them to count, an opt-in hazard rule in
  `Encounter`, narrowed to the Tyrant's surge cells and to burning soon after one of his blows, as its own ticket.
- Thallite slices 3 to 5: arms, the arrow and horse armour, the Thallite Focus, Earthen Steps and Bulwark.
- The owner's earth sets (Nymph's, Geomancer's, Wealdguard's) as Strata gear.
- Strata's civic-project and structural-boss-opening step (02:144); the Yeti column elective of Rimekeeping (02:137),
  with B-09.
- Syncing the Yeti's elapsed fight time to late joiners (08:127).
- One grace shared by every Concordance and trophy slow (an owner question, B-P10).
- The Mire Hag and the Crypt Lich on the encounter credit (their own pull requests).
- The Reagent Cabinet, Stillwater Keeping, and Root and Rivet (Chunk C: 06:119; 09:132).
- The Ice Breaker's anti-slip from the owner's text, and the Winter Crown pattern's Fire Resistance attunement *(survey)*,
  left as they are.

## Where to resume

Units 1 and 3 are implemented on branch `claude/chunk-b-winter-stone`, not merged, and their tests pass in CI (run
38519234934 on `06dc5ea5f`, every job; see the Validation plan). Unit 1, in four commits and two fixes: B-01, the Yeti
King on `Encounter` and `LairRewards` with both payout defects fixed and the column deed, then the Cinder Tyrant the
same way, with no deed (B-P4); B-02, the Glacier Hall's dossier and the Yeti King's JEI pages, with the lair step done
by any keeper's first win and the generalised `check_acquisition` and `check_encyclopedia`, then the Cinder Kiln's
dossier and the Cinder Tyrant's JEI pages (B-D1), which fill the Expeditions page's 12 grids. Unit 3 (B-05 to B-07): see
[its record](arcane-concordance-rime-strata.md#verification); after its CI, its status shots clear the advancement
toasts (not yet run). Next: units 2 (B-03, B-04: the Glacier Hall's access and lifecycle, and the Yeti fight held to the
completion evidence) and 4 (B-08, B-10: the winter workshop and gear), built in parallel; then unit 5 (B-09, B-11), with
its combined fixes and the owner's checkpoint. Open for the owner: B-P2, B-P3, B-P4, B-P5 and B-P36; from CI, whether to
lower the four-player health scale under the 1024 cap, and the spell bar's place.
#352 has merged, so the base is main; refresh the open pull requests first.

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
| B-P2 | 14 October 2026 | The dossier wording every lair shares (`won`, `after_win`, `made_from`, `owed`, `credit.wards`, the route's `support.credit`) becomes neutral ("the keeper"); JEI's pages are each one boss's, so they keep that boss's pronoun (her, his) and the ten existing pages stay as they were | Proposed (refined 14 October 2026 in B-02) | B-02 |
| B-P3 | 14 October 2026 | The column deed: key `column <player>`, one per baiter per fight, for the player the charge was aimed at as he set off, only if they already had a part (a share) then; being struck by his fight is not a part | Proposed (revised 14 October 2026 from "moved 2 blocks or more during the wind-up", which a late arrival walking in would pass) | B-01 |
| B-P4 | 14 October 2026 | No Cinder Tyrant objective deed in Chunk B: nothing records who turned a sluice (`SluiceGateBlock.java:46,243-259`; `CinderTyrantEntity.java:1050-1062`) | Proposed | B-01 |
| B-P5 | 14 October 2026 | Harm with no attacking creature (slag, fire ticks, freezing) stays outside the fight's hurt (`Encounter.java:132-134`). Applied to both bosses in B-01, with no change to `Encounter`; pinned for the Tyrant by `CinderTyrantGameTests.healingHisSlagAndBurningEarnsNothing` | Proposed | B-01 |
| B-P6 | 14 October 2026 | Authored invocations only (13 of 16); no words a player composes for Rime or Strata | Proposed | B-06, B-07 |
| B-P7 | 14 October 2026 | Brace's consenting ally is the party (`here allies`) | Proposed | B-06 |
| B-P8 | 14 October 2026 | Only the Yeti King and the Cinder Tyrant join `#jugcraft:concordance/resistant`; Vesperine and Madame Tatterlace do not | Proposed | B-06 |
| B-P9 | 14 October 2026; revised 14 October 2026 (unit 3) | Rimebind: `rimebound`, 20% slower for 60 ticks; a hidden `rime_thaw` refuses a recast for the binding's own time plus 40 ticks: 100 normally, 70 on a resistant creature (bound 30) | Proposed | B-06 |
| B-P10 | 14 October 2026 | The thaw grace covers Rimebind only; the trophies' Frost boon (accepted), `dazzle`, DAZE and the Ice Breaker wave keep their own slows. The alternative, for the owner: one grace for every Concordance and trophy slow | Proposed | B-06, B-10 |
| B-P11 | 14 October 2026; revised 14 October 2026 (unit 3) | Brace: +0.5 knockback resistance for 100 ticks, not stacking, spent by the first push it meets and bracing the rest of that tick (B-P30) rather than one hit | Proposed | B-06 |
| B-P12 | 14 October 2026; revised 14 October 2026 (unit 3) | Brace's order: a Brace is spent by the first push it meets (a blow vanilla knocks back, a harmful Concordance push, or a scripted push through `Brace.spend`, as the Yeti's in B-09) and braces the rest of that tick (B-P30), so the Yeti may call `Brace.spend` before or after his blow's `hurtServer`; a helpful push (one's own Flashstep) neither uses it nor is shortened by it | Proposed | B-06, B-09 |
| B-P13 | 14 October 2026 | Preserve: `touch struck`, the crucible only, holding its temperature band for 60 s | Proposed | B-07 |
| B-P14 | 14 October 2026 | No Thallite Focus in Chunk B (it stays in Thallite slice 5); the crafted upgrade (09:94) is the water foci | Proposed | B-07, B-10 |
| B-P15 | 14 October 2026; revised 14 October 2026 (unit 3) | Stratawrighting reads its own `#jugcraft:strata_specimens` (granite, diorite, andesite, tuff, raw Thallite), nested inside `#jugcraft:assay_specimens`, so one examination serves both entries. Cobblestone, stone, cobbled deepslate and deepslate are never specimens (sneak-use in the air would examine a bridging block), nor is the Thallite ingot | Proposed | B-05 |
| B-P16 | 14 October 2026; revised 14 October 2026 (unit 3) | Rimekeeping understood by bench study, by the winter survey gated by First Light alone (arriving in the Glacier Hall, or bottling a Rime brew by hand), or by notes; mastered by three kinds of cold work (Preserve a crucible holding a mixture while cold, and while warm or hotter, and Rimebind a hostile creature), which stand for the plan's preserve and compare steps; the Yeti column charge joins as an elective with B-09 | Proposed | B-05, B-09 |
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
| B-P30 | 14 October 2026; revised 14 October 2026 (unit 3 review) | A spent Brace braces the rest of that server tick and goes at its end, so the extra knockback of sprints, weapons and mob attacks after `hurtServer` is braced too; two blows in one tick are both braced. Spent between ticks (a push in a cast's packet), it goes when the next tick starts. The Leaf Blower's stream spends it. Accepted exception: vanilla's knockback without a damaging blow (a mace smash's bystanders) is braced but does not spend it | Proposed | B-06, B-09 |
| B-P31 | 14 October 2026 | The owner's icons and sounds byte for byte at their own sizes: Rimebind `freeze.png`, Preserve `extend_time.png`, Brace and Braced `gravity.png` (the owner's own pair), Rimebound `freezing.png`, the Thaw `soaked.png`; Brace's release `gaia_1` to `gaia_4`; the other cues drawn. It replaces the earlier `shield`, `shielding` and `frozen`, scaled to 32 | Proposed | B-06, B-07 |
| B-P32 | 14 October 2026 | The benchmark models Rimebound and its thaw, starts no cast that would change nothing, casts a harmful defense at any foe in reach, and measures knockback taken (never moved), with two acceptance checks | Proposed | B-06 |
| B-P33 | 14 October 2026 | Roles: Rimebind defense, Brace support, Preserve utility | Proposed | B-06, B-07 |
| B-P34 | 14 October 2026 | Rimebind's targets follow the shared boundary (monsters; creatures the caster could strike by hand; players only under PvP and outside the party), and only a hostile creature at normal tolerance counts as restraint | Proposed | B-05, B-06 |
| B-P35 | 14 October 2026 | Preserve's hold is a fixed Java constant (60 s, not scaled by Spell Power), restarts on a recast and never adds, runs on the world clock, and is capped at 2400 ticks so B-P24 fits | Proposed | B-07, B-10 |
| B-P36 | 14 October 2026 | The Glacier Hall's dossier names the Seasonal Stall's snow blocks (8 for 3 Jugs, under its winter and December themes) as a sale that helps with the horn, shown only where a town stands and while it is on sale | Proposed | B-02 |

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
