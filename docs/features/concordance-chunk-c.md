# Concordance Chunk C: the living workshop (contract and handoff)

Status: contract written; unit 1 (C-01 and C-02) built, in four commits on this contract (`8d61fdb7b`): `f4efbf82c`,
`ff11cc062`, `2b53fbff0` and `e1d69574e`; a fifth for its records (`807da7f5e`), a sixth with the unit review's fixes
(`d593ae61d`; see the Fixes from the unit review under C-02), and after it the Rime and Strata client tests' fix
(`0b1039312`: one research learned a tick). Unit 2 (C-03 and C-04) built on `807da7f5e`, in two commits (`a0e73881c`,
`2c98d6d68`) and a third for its records (`4df687cc7`), then merged with `d593ae61d` and `0b1039312` in a fourth
(`2263f851b`), and a fifth with the unit review's fixes (see the Fixes from the unit review under C-04). Every local
check passes; none of their game code has been compiled against Minecraft and none of their game tests has run (no CI
run yet; see the As built notes of C-01 to C-04). Unit 4 (C-07 to C-09) built on `23f28b90e` (unit 2's head, with its
review fixes), beside unit 3 rather than on it, in three commits (`5ba973632`, `d32ac3a61`, `d6edee4f4`), a fourth
for its records (`549a49d7e`) and a fifth with the unit review's fixes (see the Fixes from the unit review under C-09);
every local check passes, and none of its game code has been compiled against Minecraft or run (see the
As built notes of C-07 to C-09). Unit 3 (C-05, C-06) is being built from the same base, on its own commits, to be merged
with unit 4; unit 5 not started. Chunk C is developed on branch `claude/chunk-c-living-workshop`, whose base `e079ad4a3`
is Chunk B's head `ec06f16e0` with main `4d38e9712` merged in; CI run 38617471274 on that base passed every job (see The
merge and its CI run). Not merged; no pull request. Chunk B, beneath it, is not merged and has no pull request either.
Proposal: the owner chose Chunk C on 14 October 2026 ("ok begin chunk c"), from
[the expansion plan's delivery chunks](../plans/concordance-expansion-2026-10-10/09-claude-delivery-chunks.md) (section 5,
C1 to C4), with [workshops, ecology and pacts](../plans/concordance-expansion-2026-10-10/03-workshops-ecology-and-pacts.md),
[technology and economy](../plans/concordance-expansion-2026-10-10/06-technology-and-economy.md),
[interfaces](../plans/concordance-expansion-2026-10-10/08-interfaces-animation-and-assets.md) and
[validation](../plans/concordance-expansion-2026-10-10/10-validation-and-checkins.md). That answers only the next-school
question of Chunk B's checkpoint ([Chunk B's contract](concordance-chunk-b.md), lines 1328-1331); its other questions
(movement feel, B-P59, B-P60) are unanswered, so every B-P decision stays Proposed (10:162). The same day the owner chose
the branch and answered C-D1 to C-D3 (see Decisions).
Owner: @jimbozoomer-byte. AI-assisted implementation with Claude Code; the model is named in the commit trailers.

This page is the chunk's contract (chapter 9 §1 step 7 of the plan) and, as the units land, its technical handoff
(chapter 10 §7). Plan chapters are cited as `NN:line`; `MP` is the plan's `CLAUDE-MASTER-PROMPT.txt` and `SH` its
`START-HERE.md`. Code is cited as `file:line` at `e079ad4a3`: Java paths are under
`src/main/java/io/github/jimbozoomer/jugcraft/`, Peepo's under `src/main/java/` (`local/peepo/...`), client code under
`src/client/java/io/github/jimbozoomer/jugcraft/`, data under `src/main/resources/data/jugcraft/`, tests under
`src/gametest/java/io/github/jimbozoomer/jugcraft/test/` (Peepo's under `src/gametest/java/local/peepo/`), and `mc263src`
is the decompiled 26.3 reference. A fact marked *(survey)* comes from this chunk's survey reports, its delivery plan or
the plan's review and was not re-read for this page. **C-P*n*** is a proposed decision, not the owner's.

## Player outcome

The plan's outcome: "A player grows a magical crop, understands its assay, prepares a useful formula, powers a circle
and moves exact ingredients through a helper or ordinary logistics" (09:120); the chunk map's loop is "Garden → assay →
preparation → ritual → useful automated delivery" (09:28). Corrected for what is already built (see the status delta),
and with the owner's choices C-D1 to C-D3, a survival player can:

- **Alchemy (unit 1):** read an Alembic Crucible's heat, band, the block heating it, volume, tank water, hold, slots,
  formula step and why it waits, on the encyclopedia's Inspect page and in Jade; with the Alembic Arts and an Assay Glass,
  its makeup and contamination; with mastery, what the next dose and the formula would make. A formula that newer rules
  can no longer make stays set and says which step fails and why, instead of vanishing.
- **Two products (unit 1, C-D1):** brew the **Stillroot Poultice**, a Mendvetch healing salve, where a dirty batch heals
  less and the crucible says so before it is bottled; heal another player with it in a lair fight and earn a support
  share. Brew the **Kiln Draught**, a Fire Resistance draught that lets its drinker wade the Cinder Kiln's slag, named in
  the Cinder Kiln's dossier as Winter Brine is in the Glacier Hall's.
- **Ritual (unit 2, C-D2):** see everything a circle still needs before starting (offerings, Ley per pylon for a step and
  for the run, lending, participants, Focus, light, every fault with its position), what a run has spent and still
  reserves, and the rite's stage and payment table; perform **Root and Rivet**, which turns a Thallite plate into a
  **Rooted Thallite Foundation** with Ley from pylons fed by light, Verdance or JE.
- **Workshop (unit 3, C-D3):** build a **Crucible Stand** on that foundation under the crucible; it turns JE into the heat
  the running formula's next stir needs, while an Electric Pump pipes water into the crucible's tank. A campfire crucible
  works as before.
- **Logistics (unit 3, C-D4 Proposed):** have the formula's exact shortage brought by a Peepo through its existing
  crucible port, or by a Clockwork Porter through a courier request the crucible files itself; a full crucible keeps the
  rest aboard, an unloaded one waits, and nothing is lost or made twice.
- **Garden (unit 4):** read on the Inspect page and in Jade exactly what a plant needs and how far its neighbourhood
  reaches; grow Dewmoss and Gloamcap faster side by side (a mixed bed); see a harvested bed strain and recover with rest,
  compost or tending; carry one stable graft, Dewlit, on a Sunpetal bed.

Already built, and not rebuilt here:

| System | Where |
|---|---|
| The Alembic Crucible: mixture, heat by the block beneath, formula replay every 20 ticks, hopper buffer, bottle slot, output, water tank | `concordance/CrucibleBlockEntity.java:91-145, 199-307, 345-375` |
| Formulas (canonical operation text, replayed from empty) | `concordance/alchemy/Formula.java:14-68` |
| The sampling spoon, Assay Glass and mortar; reagents | `concordance/JugcraftConcordance.java:247-252`; `CrucibleBlockEntity.java:393-402` |
| Draughts and salves (a salve on another creature, the bowl back) | `concordance/BrewItem.java:39-87`; `JugcraftConcordance.java:253-255` |
| Verdance brews Regeneration (to level II), Ember Fire Resistance | `concordance/property/verdance.json`, `ember.json` |
| Preserve's hold on a crucible (Chunk B) | `CrucibleBlockEntity.java:182-195, 218-231`; `concordance/rime/Rime.java:120-136` |
| Winter Brine and Frostward, and the Glacier Hall's preparation fact (Chunk B) | `concordance/property/rime.json:3`; `concordance/rime/Frostward.java:23-32`; `lair/LairDossiers.java:213-233` |
| Verdant Beds and the five-factor habitat | `concordance/ecology/Sampler.java:15-65`; `concordance/garden/VerdantBedBlockEntity.java:140-160` |
| Sunpetal, Dewmoss, Gloamcap, Mendvetch and the Verdant Heart | `concordance/organism/*.json` |
| The living devices (Heart, Mulch Maw, Habitat Gauge, Gleaner) | `concordance/garden/VerdantHeartBlockEntity.java`, `MulchMawBlockEntity.java`, `HabitatGaugeBlockEntity.java`, `GleanerBlockEntity.java`; Jade providers at `compat/jade/JugcraftJadePlugin.java:37-40` |
| Circle Anchor, the ritual machine, the Lesser Circle and three rites | `concordance/CircleAnchorBlockEntity.java:290-372`; `concordance/ritual/` (`adept_attunement`, `lumen_vigil`, `spire_kindling`) |
| Ley Pylons, filled by Radiance, Verdance or JE | `concordance/LeyPylonBlockEntity.java:37-48, 157-163`; `JugcraftConcordance.java:242`; `concordance/conversion/` |
| The courier ledger, Courier Posts and the Clockwork Porter | `concordance/courier/CourierLedger.java:170-179`; `concordance/spirits/ClockworkPorterEntity.java:387-406` |
| Peepo's crucible port | `local/peepo/CrucibleCompanion.java:17-106` |
| Energy and fluid networks; the Electric Pump | `fluid/ElectricPumpBlockEntity.java:38-50, 90-126` |
| The encyclopedia's Inspect page (anchor and pylon) | `concordance/encyclopedia/InspectPage.java:26-96` |
| Chunk A's encounter credit | `lair/Encounter.java:204-222, 308-325`; `lair/Lairs.java:82-83` |
| The Cinder Kiln's slag spares anyone under Fire Resistance | `lair/MoltenSlagBlock.java:61-70` |

## Units

Each unit is a reviewable boundary: its own run of commits on `claude/chunk-c-living-workshop`, with its own record and
green checks before the next starts. The order lands the first end-to-end path (crop, crucible, product, rite, powered
station, delivery) in units 1 to 3, before unit 4's garden display and variants (09:16, "Implement the first end-to-end
path before multiplying content variants"; MP:66).

| Unit | Tickets | What it delivers | Record (proposed) | State |
|---|---|---|---|---|
| 1 | C-01, C-02 | The crucible's diagnostics and honest formula reload; the Stillroot Poultice and the Kiln Draught with their consumers, container returns and audits (C-D1) | `arcane-concordance-alchemy.md`, `lair-encounter-credit.md`, `cinder-kiln.md` | Built (`f4efbf82c`, `ff11cc062`, `2b53fbff0`, `e1d69574e`; records `807da7f5e`; review fixes `d593ae61d`): local checks pass; not compiled, no game test run, no CI run yet |
| 2 | C-03, C-04 | Ritual readiness, fault positions, spent and reserved; Root and Rivet with its stage and payment table (C-D2) | `arcane-concordance-rituals.md`, `thallite.md` | Built (`a0e73881c`, `2c98d6d68`; records `4df687cc7`; merged with unit 1's fixes `2263f851b`; review fixes in the next commit): checks run and passed: `check_mod_data`, `check_repository`, `owner_art --check`, `concordance_journey --check`, `check_icon_maps`, the World Designer's node tests, `scripts/tests`, `git diff --check`; not compiled against Minecraft, no game test run, no CI run yet |
| 3 | C-05, C-06 | The Crucible Stand with JE heat and piped water (C-D3); the shared "supply this crucible's formula" job (C-D4) | new `crucible-stand.md`; `arcane-concordance-logistics.md`, `arcane-concordance-economy.md` | Not started |
| 4 | C-07, C-08, C-09 | Habitat made visible; one mixed-bed rule with harvest strain and recovery; one stable graft trait | `arcane-concordance-ecology.md` | Built on `23f28b90e`, beside unit 3 (`5ba973632`, `d32ac3a61`, `d6edee4f4`; records `549a49d7e`; the unit review's fixes in the next commit): checks run and passed: `check_mod_data`, `check_repository`, `owner_art --check`, `concordance_journey --check`, `check_icon_maps`, the World Designer's node tests, `scripts/tests`, `git diff --check`; audit mutations 83 of 83 reported; not compiled against Minecraft, no game test run, no CI run yet |
| 5 | C-10 | The measured small-workshop load, the end-to-end journey, client evidence, combined fixes, the checkpoint and the handoff | this page | Not started |

The plan allows three to five units (09:3); this is five plus the baseline.

## Baseline (C-00)

### Facts

- Branch `claude/chunk-c-living-workshop` (local `wip/chunk-c-base`) at `e079ad4a3`, a merge commit whose parents are
  Chunk B's head `ec06f16e0` (pushed as `claude/chunk-b-winter-stone`, at the same SHA by `git ls-remote`) and main
  `4d38e9712` (#358; main's last three commits are #358, #356 `1ce4e3366` and #352 `feb6d04fb`, Chunk B's base). The
  working tree was clean.
- The planning pack is the same on both sides: `git diff --stat ec06f16e0 e079ad4a3` on
  `docs/plans/concordance-expansion-2026-10-10` is empty.
- Main changed no Concordance, Peepo, lair or Jade code since Chunk B's base: `git diff --stat feb6d04fb 4d38e9712` on
  `concordance/`, `local/peepo/`, `lair/`, `compat/jade/` and `client/compat/JugcraftJadeClient.java` is empty. In the
  merge, `tools/concordance_rituals.py` gained the pylon's power-port sockets (`:527-529`), and `docs/ART_DIRECTION.md`
  two rules (silver-grey cladding; "Support parts and give pipes a purpose").
- Framework lock: no pin or dependency change is planned.

### The merge and its CI run

The owner chose "New branch, main merged (Recommended)" (C-D0b). The merge brings #356 and #358 (4,386 files between
`ec06f16e0` and `e079ad4a3`). Two files needed resolving (the merge commit's message):
- **`src/gametest/resources/fabric.mod.json`:** main's file kept as it is (#358 reformatted it to two-space indentation
  and added nine server and seven client classes), with Chunk B's four classes in their places
  (`ConcordanceRimeStrataGameTests` and `ConcordanceWinterGameTests` at lines 30-31, their client classes at 338-339): 203
  server and 171 client entry points.
- **`CHANGELOG.md`:** #356 had cut main's file to its own 17-line entry (main still has 17 lines). The merge keeps the
  full history and adds #356's section ("The Howitzer Walker, playable", `CHANGELOG.md:36`) below Chunk B's unmerged
  section (`:11`); the branch's file has 1,909 lines. Restoring main's own file is outside Chunk C; a separate task was
  suggested for it.

Everything else merged without conflict. The merge commit records, locally: `owner_art.py` and `generate_material_data.py`
change nothing afterwards; `owner_art --check`, `check_mod_data` (2663 IDs), `check_repository`, `concordance_journey`,
`check_icon_maps` and the World Designer Node tests (12) pass. Those were not re-run for this page.

CI run [38617471274](https://github.com/jimbozoomer-byte/jugcraft/actions/runs/38617471274) (Build, workflow_dispatch,
on `e079ad4a3`), read by REST on 14 October 2026: conclusion success on attempt 2.
- Attempt 1: `choose client tests`, `optional integrations absent` (6m33s), client shards 0 to 4 (22m40s, 34m44s,
  44m02s, 46m51s, 33m19s) and `client` succeeded. `mod` (12m18s) failed in its "Build mod" step (5m33s), after "Check
  generated JSON is up to date", "Check material data" and "Test world designer and pack compiler" had passed. The
  session read that failure as the nuclear-plant site clash already seen on main (Chunk B's contract, lines 1322-1323);
  this page's requests for the job log were refused by the log host (403 Forbidden, again on this page's check) and the
  job's annotations say only "Process completed with exit code 1", so the cause is the session's reading, not
  confirmed here.
- Attempt 2 re-ran `mod` once: every step passed, the build, "Verify pinned library downloads" and "Package Jugcraft
  Complete" included (14m07s).
- The run's game-test counts were not read (the logs were not reachable for this page).

### Status delta: the plan's text against the code at `e079ad4a3`

| The plan says | What is true | Evidence |
|---|---|---|
| Formula schema: "Stable recipe ID and version" (03:46) | A formula is canonical operation text, 1 to 32 operations starting with water, with no id and no version. The Peepo port already names a formula by a hash of its text. The rules carry a revision, but it counts loads in this process (it restarts with the server), so it is not a stable version | `concordance/alchemy/Formula.java:14-20, 43-68`; `local/peepo/CrucibleCompanion.java:30-36`; `concordance/ConcordanceData.java:29, 40-52, 76-77` |
| "A previously learned formula must not silently become invalid … report the new incompatibility" (03:54) | A step refused under new rules clears the program with only a sound; setting a written formula that no longer replays says only "formula invalid"; a saved program that no longer parses loads as none | `CrucibleBlockEntity.java:282-289`, `:507-512`, `:662-663` |
| "If blocked, show output-full and pause … No voiding" (03:64) | Automated bottling pauses when the output holds a different or full stack and voids nothing, but no reason is kept or shown: one WANT sign, then silence | `CrucibleBlockEntity.java:345-359, 314-320, 119-120` |
| "Assay spoon and glass already expose composition" (03:17) | As chat lines, gated by the item and research. A client's update tag carries only temperature, parts, taste and murky; the tank's water is shown nowhere | `CrucibleBlockEntity.java:393-402, 706-715`; `concordance/Alchemy.java:101-106` |
| Seven products (03:31-39); Stillroot Poultice: "Dirty batch has lower useful yield, clearly predicted" (03:35) | Only Winter Brine exists (Chunk B, unmerged), named in JEI, the codex and a dossier; its bottle reads "Draught". Verdance already brews a Regeneration salve. Contamination never lowers a helpful effect: `Outcome.of` judges each property from the dose alone and adds Nausea (from 0.5 a part) for the contaminant; bone meal also brings Hollow, so Poison | `concordance/alchemy/Outcome.java:25-41`; data `concordance/property/verdance.json`, `contaminant.json`, `hollow.json` and `concordance/ingredient/bone_meal.json`; Chunk B's contract, line 1313 |
| "Controlled Crucible Stand: JE or ordinary fuel" (06:115) | A crucible's heat comes only from a fixed map of six vanilla blocks beneath it; no JE heater exists | `concordance/alchemy/Heat.java:14-20`; `CrucibleBlockEntity.java:233-241` |
| Companion planting: "count unique families, not identical copies" (03:74) | A diversity factor already counts distinct kinds of plant in the 5×5 at plant height, identical copies once. No declared pairing exists | `Sampler.java:41-58` |
| Grafting (03:76) | Absent: no graft or plant hybrid in `src/` or `tools/` | `grep -ri graft` finds nothing; "hybrid" names only a drone tier (`drone/DroneTier.java`) |
| "Recovery: rest, compost and tending reduce disturbance" (03:77) | Crops never die; stalling is the only failure. Disturbance is read fresh from blocks and has no memory | `concordance/garden/OrganismCropBlock.java:160-164`; `Sampler.java:52-55` |
| "maximum 9×9 horizontal habitat query" (03:91) | Today's sample is 5×5 (100 reads, cached 200 ticks). The Gleaner's search for Hearts is 9×9×9 = 729 positions, where the ecology record says 147 | `Sampler.java:16-22`; `concordance/garden/GleanerBlockEntity.java:44, 127`; `arcane-concordance-ecology.md:131` |
| Missing-part diagnostics "already exist" (03:97) | Shape faults with positions exist. An idle report checks shape only (no Ley, no owner); a start checks one step's Ley; refusals come one at a time | `CircleAnchorBlockEntity.java:454-465, 521-534 (531), 507-518 (508), 330-359, 467-473` |
| Show "already-spent and still-reserved resources separately" (03:107); "correct reserved/spent distinction" (10:48) | Not shown anywhere. The data exists: the run's done, joined and plan, and `RitualDefinition.leyPerRun`, which nothing calls | `CircleAnchorBlockEntity.java:126-128`; `concordance/ritual/RitualDefinition.java:100-102` |
| Stillwater Keeping or Root and Rivet (03:100-101) | Neither exists. A result is sealed `Transform` or `Effects`, so Root and Rivet fits `Transform` with no machine change | `RitualDefinition.java:56-71` |
| Pylons: 3 Radiance → 2 Ley (01:114) | Also 3 Verdance → 2 Ley | `concordance/conversion/verdance_to_ley.json` |
| "Reuse FluidRecipe … MachineCompanionPort and CourierLedger" (09:136) | The crucible uses neither `FluidRecipe` nor `MachineCompanionPort`. Its Peepo port, `CrucibleCompanion`, implements the same `CompanionLogistics.Port`; water reaches it through the shared fluid storage | `CrucibleCompanion.java:18`; `JugcraftConcordance.java:259`; `machine/MachineCompanionPort.java:18`; neither name appears in `concordance/` or `CrucibleCompanion.java` |
| "locate CourierLedger before adding delivery work" (01:90) | `concordance/courier/CourierLedger.java`; `file(...)` is public and takes any destination | `CourierLedger.java:170-179` |
| Thallite components (06:78) | `jugcraft:thallite_plate` has two recipes and no consumer; it appears only in the `c:plates/thallite` tag, which `c:plates` includes, and no recipe asks for either tag. Both recipes load only with the `machines` feature, the hand one also with `thallite`. Since unit 2, its first consumer is Root and Rivet (C-04) | `recipe/thallite_plate_by_hand.json:2-11`; `recipe/pressing/thallite_ingot.json:2-7`; `src/main/resources/data/c/tags/item/plates/thallite.json:4`; `src/main/resources/data/c/tags/item/plates.json:16` |
| Records cite pure harnesses ("61 checks", "52", "98", "41") | Not in Git: `src/test` holds only `VanillaReferences.java` | `find src/test` |

### Defects and gaps found

**Fixed in this chunk** (1 to 7 in unit 1; 8 to 10 in unit 2; 11 to 15 in unit 4; see each ticket's As built notes):

1. A formula refused under new rules vanishes with only a sound (`CrucibleBlockEntity.java:282-289`). C-01.
2. Setting a written formula that no longer replays says only "formula invalid", without the step or the reason
   (`:507-512`; `Formula.replay` returns the refusal but not where, `Formula.java:30-41`). C-01.
3. A saved program that no longer parses is dropped on load (`:662-663`). C-01.
4. The formula's tooltip says "sneak to set a written one" (`tools/concordance_alchemy.py:153-155`); setting is a plain
   use, and sneaking with an item in hand skips the block's use entirely (mc263src `MultiPlayerGameMode.java:335-337`).
   C-01.
5. `CrucibleCompanion.status` says FULL whenever the output holds anything (`CrucibleCompanion.java:96`), but the
   crucible keeps filling a matching stack (`CrucibleBlockEntity.java:356`). C-01.
6. A formula the crucible can no longer follow still drives Peepo demand: `shortage()` and `needed()` read the remaining
   operations whatever the formula's state (`CrucibleCompanion.java:37-57`). C-01, C-06.
7. Anyone with the Alembic Arts can work, set or clear the formula of any crucible: neither `CrucibleBlock.useItemOn`
   nor `CrucibleBlockEntity.use` asks who may change the block (`CrucibleBlock.java:55-69`;
   `CrucibleBlockEntity.java:387-461, 533-553`), while the garden asks (`OrganismCropBlock.java:213`;
   `concordance/Illumination.java:32-38` → `concordance/Authority.java:79-90`). C-01 (C-P5), because C-06 makes the
   formula's setter the courier requester.
8. A ritual's interruption reason and "the offerings are still in the anchor" both go to the action bar in one tick
   (`CircleAnchorBlockEntity.java:650-656, 892-898`), so the second probably replaces the first. A code reading; no client
   was run to see it. C-03. Fixed in unit 2: one line (`circle.ended`, `.ended.spent`), held by `check_rituals`.
9. Jade's gathering line prints "?" for the participants needed (`client/compat/JugcraftJadeClient.java:290`); the
   generated `tooltip.jugcraft.concordance.jade.offering` and `.participants` keys are unused
   (`src/main/resources/assets/jugcraft/lang/en_us.json:7953-7954`). C-03. Fixed in unit 2: the snapshot carries
   `needed`, and both keys are used.
10. A start checks one step's Ley (`CircleAnchorBlockEntity.java:508`), so a run can start that its pylons cannot finish,
    with nothing said beforehand. C-03 warns (C-P10). Warned in unit 2: readiness names the step a run would break off
    at; the start rule is unchanged.
11. The ecology docs say a canteen waters beds (`arcane-concordance-ecology.md:25`; the codex's habitats page), but
    `agriculture/GourdCanteenItem.java:87-100, 116` pours only on farmland, a giant pumpkin, fire or a cauldron, never a
    bed. C-07. Fixed in unit 4: a sip wets a bed below 7 through `VerdantBedBlock.pour` (C-P56).
12. The compost JEI page is generated but never used (`tools/concordance_ecology.py:554-557`). C-07. Fixed in unit 4:
    `recipe_view.json` `garden_compost` and `JugcraftJeiPlugin.addCompostInfo` (C-P57).
13. A crop's Jade lines hide the verdict once it is ripe (`JugcraftJadeClient.java:254`), and a bed's verdict is never
    cleared when its plant goes (`VerdantBedBlockEntity.java:168-188` is written only by a crop's growth and report and
    by a Verdant Heart's work: `OrganismCropBlock.java:152-161, 248`; `VerdantHeartBlockEntity.java:105`). C-07. Fixed
    in unit 4: Jade shows a ripe crop's last verdict as it grew, and a bed's random tick forgets it once neither a crop
    nor a Heart stands on it (C-P55).
14. A plain use of any item on a Verdant Bed reported the bed instead of the item's own use (planting, fertilizer, a
    block set on it needed sneaking): `VerdantBedBlock.java:140-154`; found in unit 4's design. C-07 (C-P56). Fixed in
    unit 4: an item in the main hand is its own use; only an empty hand reports, wakes or tends.
15. Bone meal or fertilizer used plainly on a growing Greenwarden crop reported the crop instead of feeding its bed,
    contrary to the codex (`OrganismCropBlock.java:204-228`); found in unit 4's design. C-08 (C-P61). Fixed in unit 4:
    fertilizer is its own use there, and bone meal feeds the bed by the bed's own rule.

**Record defects, fixed when each unit touches its record:** the alchemy record's table lists 11 ingredients
(`arcane-concordance-alchemy.md:26-38`) where the data has 15 files in `concordance/ingredient/` (Sunpetal, Dewmoss,
Gloamcap and Mendvetch are missing); the rituals record says thirteen tests
(`arcane-concordance-rituals.md:203`) where there are 18; the ecology record says nine
(`arcane-concordance-ecology.md:187`) where there are 12, and 147 positions (`:131`) where the Heart search is 729; the
alchemy record's "Nothing alchemy makes is a resource another system consumes, so there is no conversion loop"
(`arcane-concordance-alchemy.md:164`) stops being true with C-02; the pure harnesses those records cite are not in Git.
Unit 1 corrected the alchemy record's table (15 rows) and its line 164, and says there that its harness is not in Git.
Unit 2 corrected the rituals record's count (thirteen when step 12 was built, eighteen before Chunk C, 29 now) and says
there that its "98 checks" harness is not in Git.
Unit 4 corrected the ecology record's test count (twelve before Chunk C, 33 now) and scan figures (the Maw 147, the
Gleaner 147 for crops and 729 for Hearts), and says there that its "52 checks" harness is not in Git.

**Not Chunk C's:** main's shortened `CHANGELOG.md` (above).

### Open pull requests and the review route (REST, 14 October 2026; refresh before each unit, 09:11)

18 open at this page's check: the 17 of planning (#351, #353 to #355, #357, #359 to #370) and #371, opened at 20:18
UTC on 14 October, after the survey. Of the 17, only #370's head has moved since the pull-request survey (`b0746a5da` →
`01f62e6d3`, its child commit). Files compared: each pull request's changes from its merge base with main (#371 by
`/pulls/371/files`), against the files this contract plans to touch. None touches Concordance Java or data, the
crucible, Peepo's crucible port or `CompanionLogistics`, the Jade provider or client, `InspectPage`, `LairDossiers`,
`Encounter` or the canteen.

| PR | What it is | Files it shares with Chunk C's plan | Bearing |
|---|---|---|---|
| (Chunk B, no PR) | `claude/chunk-b-winter-stone` at `ec06f16e0` | — | Chunk C is stacked on it: the Kiln Draught's dossier fact sits on B-02's dossiers and B-08's preparation fact; Root and Rivet completions feed B's Strata listener (`concordance/strata/Strata.java:34`). A change to B in review means another merge |
| #357 | Flaky client tests: a Peepo stopping short of its food, a flail ball (owner, ready) | `local/peepo/PeepoEntity.java`, `local/peepo/CompanionHusbandry.java`, `PeepoDeliveryClientTests`, `CompanionPopulationChecks` | Chunk C must not edit those files. C-01 and C-06 edit `CrucibleCompanion.java`, which neither #357 nor #369 touches |
| #369 (on #367) | The Iron Dreadnought | `CompanionHusbandry.java`, `CompanionPopulationChecks`; `CHANGELOG.md`, the gametest `fabric.mod.json`, lang, `check_mod_data.py` | As #357. (The survey found `PeepoEntity` in it too; at head `d8ac567d2` the diff from its merge base does not change it) |
| #366 | Machine screens in five looks (owner, ready) | `CHANGELOG.md`, `docs/WHAT_EXISTS.md`, lang | The stand is a block entity with no menu, so no screen conflict; `WHAT_EXISTS.md` rows may conflict |
| #351, #354 | Spodumene deposits; regional mineral sands (base before #352) | `tools/owner_art.py`, `recipe_view.json`, `check_mod_data.py`, `fabric.mod.json`, lang | `owner_art.py` is the import route if the stand's art comes from the owner's library (C-05) |
| #361 to #365 | Fork drafts, one stack (base before #352) | `docs/INTEGRATION_STATUS.md`, `recipe_view.json`, `check_mod_data.py`, `fabric.mod.json`, lang | Shared files only; they need a rebase onto main |
| #353, #367 | The Crypt Lich, part 2; the Foundry Trench | `CHANGELOG.md`, `check_mod_data.py`, `fabric.mod.json`, lang | Shared files only |
| #355, #359, #368; #360 | Guns 13A to 13C; Chima the lion cub (fork draft) | `CHANGELOG.md` (not #360), `fabric.mod.json`, lang | Shared files only |
| #370 | Adam-ondi-Ahman, part 1 (draft) | `CHANGELOG.md`, `check_mod_data.py`, `fabric.mod.json`, lang | Its part 2 keystone "has a Concordance recipe", planned only *(survey)* |
| #371 | KV-1/W walker: model, textures and animation clips (owner, ready, head `36e35b509`) | none: seven new files under `art/kv_walker/`, `docs/images/` and `tools/` (`blockbench_export.py`, `box_preview.py`, `kv_walker.py`) | None today |

**How Chunk C will be reviewed is open.** Chunk B has no pull request, so a Chunk C pull request against main would show
Chunk B's 260 changed files (`git diff --shortstat 4d38e9712 e079ad4a3`: 260 files, the restored `CHANGELOG.md` history
among them) and Chunk C together: the "single enormous unreviewable PR" the pack rules out (SH:48; 09:3 asks for
reviewable units). This is a question for the owner's checkpoint, not one of the decisions below:
- either Chunk B's pull request is opened first and Chunk C's targets Chunk B's branch. For Chunk C's diff to show only
  its own work there, Chunk B's branch would first take the same main merge (`e079ad4a3` holds only Chunk B and main);
  that is the owner's step to approve;
- or Chunk B and Chunk C are reviewed together in one pull request against main, unit by unit through the commits.

### Checks before each unit lands

- `python3 tools/check_mod_data.py`, `python3 scripts/check_repository.py` and
  `python -m unittest discover -s scripts/tests`.
- Refresh the open pull requests and main; merge main again if it moved.
- The unit's own tests (see the Validation plan), with `-PjugcraftOptionalIntegrations=false` for every unit that
  changes Jade code (units 1 to 4).

## Tickets

### C-01. The crucible says what it is doing, and keeps a formula honestly (unit 1)

**What the player sees.**
- Jade on a crucible adds the tank (for example "Water 2/3 parts") and a waiting reason: needs water, needs an
  ingredient, needs a bottle or a bowl, output full (or holding a different brew), wrong heat (waiting for a band), held
  by Preserve, formula incompatible. Never the makeup.
- The encyclopedia's Inspect page, looking at a crucible within the player's block reach, shows (C-P3):
  - **to anyone:** heat, band, the target and the block giving it ("this block does not heat a crucible" otherwise),
    the hold, volume of 6, the tank, the slots, the formula's step and the waiting reason; and a **risk line** whenever
    the formula (or, with none, the mixture) would make a harmful brew or has a searing step (03:54: unknown recipes
    "must not hide destructive risk");
  - **with the Alembic Arts understood and an Assay Glass carried:** dissolved and pending per axis a part, the
    contaminant a part against the murky and Nausea thresholds and what it takes from a fouled property (C-02), and
    operations used of 32;
  - **with the Alembic Arts mastered:** the outcome now, the formula's replay preview under the current rules, and the
    reasons. These are the existing `Assay.read` levels (`CrucibleBlockEntity.java:397-401`).
- A formula the current rules can no longer make stays set: the crucible names the step that fails and why (Inspect,
  Jade, the empty-hand status line) and shows a PERIL sign once. When the rules change back, it carries on.
- Setting a written formula that no longer replays says which step fails and why; setting one that replays says its risk
  line too. The tooltip says "use" rather than "sneak".
- Only someone who may change the block can set or clear a formula, add, pour, stir or bottle (C-P5); reading is open.

**Changes.**
- **One shared diagnostic vocabulary** (03:9; C-P6): a pure package `concordance/diagnostic` holding stable codes with
  parameters (03:9's list, plus needs water, needs a container, held, incompatible and courier quota), localised by the
  client. It is added to the pure-package list (`tools/check_mod_data.py:12310-12314`). The crucible, the stand (C-05),
  ritual readiness (C-03) and crucible filing (C-06) use it; `local/peepo/CompanionStatus.java` and the porter's `Status`
  map from it at their edges and are not replaced.
- **The waiting reason:** `automate` (`CrucibleBlockEntity.java:244-307`) keeps a transient reason instead of the bare
  `waiting` flag (`:119-120`), including the silent band wait (`:276-280`).
- **Honest reload** (C-P1): on a refused step the crucible keeps `program` and `step` and holds an INCOMPATIBLE reason
  (step and the refusal's text) instead of clearing them (`CrucibleBlockEntity.java:282-289`). It re-runs
  `Formula.replay` (at most 32 operations, `Formula.java:30-41`) once whenever `ConcordanceData.revision()` differs from
  the revision it last checked against (`ConcordanceData.java:44-52`): the existing counter, not a new one.
  `Formula.replay` reports the index of the refused step. A saved program that no longer parses is kept as text, written
  back on save and reported, not dropped (`CrucibleBlockEntity.java:662-663`).
- **The formula item path:** `formula()` (`CrucibleBlockEntity.java:505-518`) says the step and reason instead of the
  bare `formula_invalid`, and adds the risk line when it sets one.
- **INCOMPATIBLE drives nothing:** `CrucibleCompanion.needed()` is 0 and its status is `RECIPE_MISSING`; C-05's stand
  idles and C-06 files nothing for it.
- **One source for status:** `CrucibleCompanion.status` (`CrucibleCompanion.java:93-105`) maps from the crucible's
  reason, which removes the FULL mismatch.
- **Authority** (C-P5): every state-changing crucible use (`CrucibleBlockEntity.use` past the spoon and glass, and
  `useEmpty`'s clear) asks `Authority.mayChange` (`Authority.java:79-90`).
- **Jade:** the crucible snapshot (`compat/jade/ConcordanceDataProvider.java:138-161`) adds the tank's parts and the
  reason's code and parameters; the client (`JugcraftJadeClient.java:350-367`) localises them. A new check holds the
  makeup out of that snapshot, beside the update-tag lock (`tools/check_mod_data.py:16290-16294`), which stays at four
  keys. Jade's "waiting for" line keeps showing the next operation to anyone (C-P4).
- **Inspect:** `InspectPage.write` (`InspectPage.java:41-57`) gains a crucible branch reusing the encyclopedia request,
  its loaded check and `PageWriter`; the crucible branch uses the player's block reach rather than the 16 blocks the
  anchor branch allows (`:47-50`; `ConcordanceCommand.java:55`), since glass-level makeup would otherwise read through
  walls (C-P3). The snapshot logic is pure (in `concordance/alchemy`), the writer outside it.
- **Text:** the tooltip fix in `tools/concordance_alchemy.py:153-155`; lang keys for every code.

**Reuses unchanged:** `Mixture`, `Band`, `Heat`, `Assay.read`'s gating, `ConcordanceData.revision()` and `replace()`,
the encyclopedia payloads, Signs, `RateGate`.

**Tests (server):**
- `ConcordanceAlchemyGameTests` (13 today):
  - `aChangedRuleKeepsTheFormulaAndSaysWhy` and `aRestoredRuleResumesTheFormula`. They stand for a reload with
    `ConcordanceData.replace` (used so at `ConcordanceComposeGameTests.java:361`). That is global and static: a catalogue
    missing an ingredient would reach every Concordance test running beside it, so the tests swap the rules only
    through `under()`, which restores them in a `finally` within the same callback (C-P28; `check_diagnostics`);
    vanilla batches by environment and dimension, 50 a batch (`mc263src` HEAD `GameTestBatchFactory.java:32-74`), which
    is not relied on;
  - `aWrittenFormulaThatNoLongerReplaysSaysWhichStep`;
  - `aSavedFormulaThatNoLongerReadsIsKeptAndReported`;
  - `anIncompatibleFormulaAsksForNothing` (`companion.needed(...)` is 0; no filing; no heat demand);
  - the hazards of 03:67 but the container return (C-02): `aFullOutputPausesAndVoidsNothing` (a matching stack at 16 and a
    different outcome: mixture, output and bottle count unchanged, the reason shown); `twoHoppersRaceForTheBuffer` (exact
    counts); `partialWaterWaitsThenDrawsExactly`; `aRestartJustBeforeOutputBottlesOnce` (a block-entity save and load at
    the last step);
  - `theCompanionAndTheCrucibleAgreeOnFull`;
  - `onlyThoseWhoMayChangeACrucibleWorkIt` (a claim or protected town; reading stays open);
  - `theVesselShowsWhatASpoonFinds` (`ConcordanceAlchemyGameTests.java:397`) unchanged.
- `EncyclopediaGameTests.theInspectPageReadsACrucibleByResearch`: spoon level and the risk line for anyone; glass level
  only with the research and the glass; preview only for a master; nothing beyond reach or in an unloaded chunk.

**Tests (client):** `EncyclopediaClientGameTests`: a crucible's Inspect page at the ordinary and a high GUI scale; a Jade
shot of a waiting crucible.

**Acceptance:** "Formula reload, output blockage … and save/reload are tested" (09:140); 03:54; 03:62-67; 10:49.

**Save effects:** none new. The program, step and mixture are already saved (`CrucibleBlockEntity.java:669-695`); the
waiting reason and the checked revision are not; an unreadable program's text is written back under the existing
`program` key.

**Review boundary:** crucible diagnostics, the shared vocabulary, authority on crucible use, and their tests. No brew
outcome changes.

**As built (unit 1: commits `f4efbf82c` and `ff11cc062`).**
- **Built:**
  - the pure package `concordance/diagnostic`: `Code` holds 03:9's eleven codes in its order and words, then Chunk C's
    eight, each with a kind; `Diagnostic` is a code with at most four parameters, worded under
    `compose.jugcraft.diagnostic.<id>` and sent to a client as `encode()`d text. Its table is
    `tools/concordance_diagnostics.py`;
  - the crucible's reason, `CrucibleBlockEntity.waiting()`, read by the empty-hand status line, Jade, the Inspect page
    and the Peepo port;
  - the honest reload. `incompatibility()` replays the formula once per `ConcordanceData.revision()` and after a load. A
    refused formula keeps its step, and an unreadable saved one is kept under `program`;
  - `formula()` names the refused step and why (`message.jugcraft.concordance.alchemy.formula_refused`), and gives the
    risk line (`Assay.risks`) when it sets one;
  - Authority on every change;
  - `CrucibleCompanion`'s status and demand, taken from the crucible's reason;
  - the Inspect page's crucible branch (`InspectPage`, and the pure `alchemy/CrucibleInspection`), and Jade's tank and
    reason;
  - the tooltip and the codex's formulas page.
- **Departures from the ticket,** each a proposal below:
  - the code ids follow 03:9's own wording (`missing_ingredient`, `insufficient_capacity`, ...). Each code's kind sets
    the sign it shows when it begins (C-P22). The wait before a repeat, silent before, now shows a WANT sign once
    (`needs_emptying`), and Peepo shows `OCCUPIED` for it;
  - an incompatible or unreadable formula bottles nothing automatically, not even a finished batch (C-P23);
  - recording onto a blank formula stays open. Every change, clearing included, still needs the Alembic Arts (C-P24);
  - the risk line names the harmful statuses and the first searing step (C-P25), and, since the unit review, the harm
    of a mixture that is not the formula's own batch;
  - the Inspect page reads a crucible within the reader's block reach plus the server's 1.0 use allowance. Every level
    above the spoon's needs an Assay Glass carried, a master's too (C-P26);
  - Jade's raw step now reads "Next step: ..." (C-P27);
  - the reload tests swap the rules through `under()`, not in a batch of their own (C-P28);
  - `follow(Formula)` sets a formula without the item's checks. The client test uses it, so spawn protection on its
    server cannot refuse the formula;
  - `onlyThoseWhoMayChangeACrucibleWorkIt` refuses a stranger inside a claim (`TestClaims`) and, outside it, in
    adventure mode. No game test can make a town, so a protected town is not tested.
- **From the spec review:**
  - the stranger's stick is tried before any stir, so only the claim can refuse it;
  - adventure mode is a second refusal;
  - `anIncompatibleFormulaAsksForNothing` runs a formula to its end and shows that no bottle is used while it is
    incompatible, then that one pulse bottles once the rules are back (C-P23);
  - `crucibleInspection` checks that neither a key nor a `jugcraft:` id shows in the reason or the page's rows.
- **Tests (none run):**
  - 13 new `ConcordanceAlchemyGameTests`: 11 in commit 1, `jadeIsToldTheTankAndWhyNeverTheMakeup` in commit 2, and
    `aHoldIsTheReasonOnlyWhileItKeepsTheHeat` from the unit review;
  - `EncyclopediaGameTests.theInspectPageReadsACrucibleByResearch`;
  - the client test `EncyclopediaClientGameTests.crucibleInspection`, with shots
    `jugcraft_encyclopedia_inspect_crucible` and `_scale4`, and `jugcraft_concordance_crucible_jade` when Jade is
    loaded.
  There is no Minecraft jar where they were written. The Jade test checks Jade's snapshot only when Jade is loaded, so a
  build with `-PjugcraftOptionalIntegrations=false` alone does not run those checks.
- **Audits:**
  - `check_diagnostics` (new);
  - `check_alchemy`: preparation names, and a written formula set by a plain use;
  - `check_signs`: Jade's crucible snapshot and its client;
  - `check_encyclopedia`: the crucible page's reach and glass, and that it only reads;
  - the crucible in `AUTHORITY_ROUTES`, and `diagnostic` in the pure-package list.
- **Commands,** for each commit:
  - run and passed: `python3 tools/check_mod_data.py`, `python3 scripts/check_repository.py`,
    `python3 tools/owner_art.py --check`, `python3 tools/concordance_journey.py --check`,
    `python -m unittest discover -s scripts/tests` and `git diff --check`;
  - the listed audit mutations (§1.7 and §2.3 of the unit's spec) were not run when commits 1 and 2 were made. They
    were run with the unit review's fixes, on a scratch copy: commit 1's twelve (20 variants) and commit 2's seven,
    each reported, but for dropping `ground` from `PREPARATION_NAMES`, which is reported only once the generator has
    rewritten the lang (the stale key hides it from `check_mod_data` alone);
  - not run: `./gradlew build` (with or without the optional integrations), `./gradlew runClientGameTest` and CI.

### C-02. The Stillroot Poultice and the Kiln Draught (unit 1; C-D1)

**What the player sees.**
- **The Stillroot Poultice:** Mendvetch, water and warm stirs give a salve of Regeneration II; the generator's model
  (`tools/concordance_alchemy.py` `simulate`, re-run for this page) gives 1 water, 2 Mendvetch as they come and four
  warm stirs → Verdance 2.127 a part, Regeneration II for 450 ticks (the cap, `property/verdance.json`) *(the model,
  not a game run)*. A dirty batch heals less, and the crucible says so before bottling: the Assay Glass shows what the
  contaminant takes from the Verdance, a master's preview shows the weaker outcome, and the outcome's reasons name what
  fouled it.
- Applying a poultice to another player hurt in a lair fight earns a support share for the healing its Regeneration
  actually gives (C-P8): the same share healing spells earn (two hearts of the fight's hurt qualifies,
  `lair/Encounter.java:55-56, 103-105`). It works in every lair on the encounter credit (Vesperine, Madame Tatterlace, the
  Yeti King, the Cinder Tyrant), not the Mire Hag's or the Crypt Lich's (still on the old path, Chunk B's contract, lines
  997-1000). The dossiers' credit line says so.
- **The Kiln Draught:** a Fire Resistance draught from existing ingredients; the model gives 1 water, 1 magma cream and
  two hot stirs → Ember 1.276 a part (with 0.2 contaminant, under Nausea's 0.5), 1531 ticks, or 2 sugar → 1.435, 1722
  ticks *(the model, re-run; not a game run)*. Drunk, it lets its drinker
  wade the Cinder Kiln's slag (`MoltenSlagBlock.java:65-70`). Carrying a Kiln Seal shows the Cinder Kiln's rite with a
  preparation fact naming the Kiln Draught, and the Salamander Charm (his drop,
  `loot_table/entities/cinder_tyrant.json:47`) as the lasting answer, as Winter Brine and the Yeti Mitten are named for
  the Glacier Hall (`LairDossiers.java:213-233`).
- Both are named on the item (C-P2), in JEI's pages for their ingredients and in the codex.

**Changes.**
- **The yield rule** (C-P7): a property may declare `fouling`, how many units of it each unit of contaminant a part
  takes away before its level and duration are worked out. Verdance alone declares it (4.0, a starting value). In
  `Outcome.of` (`Outcome.java:25-41`), pure Java, with a reason `alchemy.fouled` naming the foulers (the existing
  `foulers`, `:85-101`); in `Property` (`Property.java:14-37`), the parser's allow-list and the generator's `PROPERTIES`
  and `simulate`, mirrored and held by `check_alchemy` (`check_mod_data.py:13456`). With bone meal added before the
  four warm stirs, the model (re-run for this page) gives 2.735, 3.343 and 3.951 Verdance a part for one, two and three
  bone meal, with 0.2, 0.4 and 0.6 contaminant and 0.227, 0.455 and 0.683 Hollow a part. Today all three are
  Regeneration II (two add Poison, three also Nausea). Under the rule (arithmetic, not run): one bone meal leaves 1.935,
  Regeneration I; two 1.743, Regeneration I and Poison; three 1.551, Regeneration I, Poison and Nausea. A searing
  stir's 500 milli-units of contaminant (`Mixture.java:28-29`) now also costs Verdance: one in place of the poultice's
  last warm stir leaves a one-part batch 2.215 less 2.000, 0.215 Verdance a part, under the threshold, so Nausea only
  *(the model, re-run)*.
- **Products as data** (C-P2): `concordance/product/*.json`, generated by `tools/concordance_alchemy.py`: an id, a form
  (salve or draught), the status it must carry, "nothing harmful", a name key and a codex link. `Alchemy.brew`
  (`Alchemy.java:56-66`) names a matching stack with vanilla's item name. A stack is a product by what it carries, never
  by its name. Winter Brine is named the same way, closing Chunk B's known defect (Chunk B's contract, line 1313).
- **The poultice's credit** (C-P8): `BrewItem.applySalve` (`BrewItem.java:76-87`) tells the encounter when a poultice
  another player applied takes (`actor` non-null, `:56`). `Encounter` keeps a transient mending note (helper, patient,
  until) and, on each tick vanilla's Regeneration would heal the patient, credits the health they gained that tick, at
  most 1, through `Encounter.healed` (`Encounter.java:308-325`), which already credits only the fight's hurt not yet
  healed and nothing past full health. Regeneration's interval was read from the 26.3 sources (`mc263src` HEAD:
  `RegenerationMobEffect`, a heal every `50 >> amplifier` ticks; `MobEffectInstance.tickServer` passes the time left
  before it counts down). No mixin (CLAUDE.md). Before this, brews earned nothing because the boundary's
  observer sees only RESTORATION and PROTECTION (`ConcordanceEffects.java:230-237`; `Lairs.java:82-83`).
- **The Cinder Kiln's preparation fact** (C-P9): `LairDossiers.prepare` (`LairDossiers.java:220-233`, today the Glacier
  Hall only) gains the Cinder Kiln: text and icons, no recipe grid (the Expeditions page's grids are spoken for, Chunk
  B's contract, line 1314), a codex link at the Alembic Arts observed; the Charm only while the Concordance is off or
  the data's Ember no longer brews Fire Resistance.
- **Guidance:** JEI pages for Mendvetch, magma cream and sugar name their product as a property fact (as Chunk B's
  `RIME_JEI`, `tools/concordance_alchemy.py:209, 240`); codex entries `alembic/stillroot_poultice` and
  `alembic/kiln_draught`.
- **Audits** (`check_workshop_products`, new, after `check_winter_brine`, `check_mod_data.py:13678`), one per obligation
  of the alchemy survey's section 9 *(survey)*:
  - **consumers:** the poultice's is a lair share and a friend's healing; the draught's is the slag. Neither is decorative
    (09:124);
  - **not weighable:** no product is an ingredient or a Scale material (extending `:13725-13727`); each carries
    `jugcraft:brew`, so the Scale refuses it as magical (`jugcraft` is a magic namespace,
    `equivalence/Eligibility.java:29`, checked at `:39-62`);
  - **brews never feed brews:** refused by hand, by hopper (`CrucibleBlockEntity.java:577-583`) and by the mortar;
  - **reachable from a manual start:** Mendvetch is crafted from wheat seeds, bone meal and an amethyst shard
    (`tools/concordance_ecology.py:526-528`); magma cream and sugar are vanilla;
  - **containers net to zero:** a salve gives its bowl back, dropped at the user's feet if the inventory is full
    (`BrewItem.java:83`; mc263src `Inventory.java:273-283`); a draught gives its bottle back
    (`JugcraftConcordance.java:253-254`);
  - **dilution gains nothing:** a poultice diluted to 2 parts gives two doses of 1.063 a part, Regeneration I for 318
    ticks each (`Mixture.bottle` gives every dose of a batch the same share since the unit review; before, the last
    dose took the remainder, 1.064 for 319 ticks) *(the model, re-run)*. At vanilla's
    interval (above) the two heal 6 + 6 = 12, against 18 for one Regeneration II dose of 450 ticks
    (`dilutionNeverHealsMore` measures it). The credit is bounded by the fight's hurt, not by how many doses exist;
  - **no stacking:** `Stacking.STRONGEST` (`Alchemy.java:79-81`; `effect/Stacking.java:11-15, 68-76`): a second poultice
    replaces a weaker one or, as strong and longer, extends it to its own time; otherwise nothing changes, and it never
    adds a level;
  - **the boss route:** a Tyrant Scale makes 2 magma cream (`recipe/magma_cream_from_tyrant_scale.json`, under
    `agriculture`), a boss-to-ingredient edge found through `_made_from` (`check_mod_data.py:13623`). Proposed as a
    reward route, not a loop (C-P9): the draught makes no Kiln Seal (`kiln_seal_from_scales`, `LairDossiers.java:71-73`)
    and no scale, and magma cream has vanilla sources. A second route: the Crypt Lich's Grave Dust makes 2 bone meal
    (`recipe/bone_meal_from_grave_dust.json`), and bone meal goes into Mendvetch. Both are declared in
    `PRODUCT_REWARD_ROUTES` and found by recipes alone, not through the Assayer's Scale (C-P32);
  - **Vitae:** health is not a modelled resource; the Vigil offering stays bounded by `HOURLY_VITAE_BOUND` = 60
    whatever heals the giver (`check_mod_data.py:15597, 15652-15653`).
- **Records:** `arcane-concordance-alchemy.md:164` rewritten to name the consumers and these checks;
  `lair-encounter-credit.md`, `cinder-kiln.md`.

**Reuses unchanged:** the mixture rules, `BrewItem`'s salve and bowl, `Encounter.healed` and its bounds, `LairDossiers`'
rite section, Preserve's hold.

**Tests (server):**
- `ConcordanceAlchemyGameTests`: `aStillrootPoulticeRepeatsAndADirtyBatchHealsLess` (by hand and automated; the preview
  equals the outcome); `aKilnDraughtCarriesItsDrinkerAcrossTheSlag` (no slag damage under it; the bottle back);
  `aPoulticeReturnsItsBowlToAFullInventory`; `productsNeverFeedTheCrucible`; `twoPoulticesDoNotAdd`;
  `dilutionNeverHealsMore`; `anOldUnnamedBrewInTheOutputPausesAndSaysSo` (named stacks differ in components from older
  unnamed brews, so an automated crucible pauses until its output is emptied, `CrucibleBlockEntity.java:356`; now with a
  reason).
- `ConcordanceAssayGameTests` (6 today): `workshopProductsAreNeverWeighed` (eligibility refused, the ledger unchanged).
- `LairEncounterGameTests` (10 today): `aPoulticeOnAFriendIsCreditedForWhatItHeals` (no more than the fight's hurt and
  the poultice's own healing; qualifies at two hearts); `aPoulticeOnOneselfEarnsNothing`;
  `aPoulticeOnAnUnhurtFriendEarnsNothing`; `aSecondPoulticeAddsNoCredit`; `aPoulticeOutsideTheFightEarnsNothing`;
  `onlyAPoulticeMends` (a Frostward salve earns nothing, as Chunk B recorded, its contract line 1024);
  `blowsAndSpellsBetweenItsHealsAreNotThePoultices`; `regenerationHealsOnItsVanillaInterval`.
- `EncyclopediaGameTests.theCinderKilnsRiteNamesTheKilnDraught` (and the Charm alone with the Concordance off);
  `theDossiersNameThePoulticesCredit`.
- `ConcordanceAlchemyGameTests.jadeIsToldTheTankAndWhyNeverTheMakeup` (C-01's, in unit 1's second commit).

**Tests (client):** Inspect shots of a fouled poultice's preview and of a crucible holding a Kiln Draught formula.

**Acceptance:** "Implement two complete products first … no decorative recipes with no consumer" (09:124); "container
returns … are tested" (09:140); "The magical product cannot turn into an equivalence profit loop" (09:140; 06:146-164;
10:57); "Dirty batch has lower useful yield, clearly predicted" (03:35). Not the only medicine (06:26): vanilla healing
and vanilla's Fire Resistance potion remain.

**Save effects:** names and the fouling rule reach new stacks only; bottles already made keep what they carry (the
`jugcraft:brew` component). No new saved key: the mending note is not saved, like `Encounter` itself
(`Encounter.java:49-50`).

**Review boundary:** alchemy data and generator, product naming, one `Encounter` hook (the mending credit) in lair code,
the Cinder Kiln's preparation fact, the checks and the records.

**As built (unit 1: commits `2b53fbff0` and `e1d69574e`).**
- **Built:**
  - the yield rule: `Property.fouled`; in `Outcome.of` the fouling is taken, subtracted, then judged; Verdance declares
    4.0;
  - three products as data (`concordance/product/`, read by `RulesParser.product` into `AlchemyCatalog`):
    `stillroot_poultice`, `kiln_draught` and `winter_brine`. `Alchemy.brew` names a stack that `Alchemy.product`
    matches, with vanilla's item name;
  - the Assay Glass line (what the contaminant takes) and the fouled reason;
  - codex entries and the JEI facts;
  - the Cinder Kiln's preparation fact;
  - the poultice's credit: the `BrewItem.Observer`, registered by `Lairs`, calls `Encounter.mended`; the encounter reads
    the note at three points (C-P30); `creditHealing` holds `healed`'s old body;
  - the dossiers' poultice fact;
  - `check_workshop_products` and `check_poultice_credit` (new), and `check_alchemy`'s fouling rules.
- **Departures from the ticket:**
  - no `Products` class: `Alchemy.product` and `AlchemyCatalog.product`, with `Alchemy.STILLROOT_POULTICE`;
  - a product is any brew of its form that carries its status and nothing harmful, as the server judges harm (C-P29);
  - credit is counted from Regeneration's own time left, read at each tick's end, each blow and each Concordance heal.
    The ticket said "on each tick vanilla's Regeneration would heal" (C-P30);
  - the Kiln Draught's dossier fact also names his flame (C-P31);
  - Grave Dust is a second declared reward route (C-P32);
  - `_made_from` takes `scale=False` and reads the recipe files once a run;
  - `LairDossiers.poultices()` reuses `brews`, the helper the Kiln Draught's fact added;
  - two credit tests (`aPoulticeOnOneselfEarnsNothing`, `blowsAndSpellsBetweenItsHealsAreNotThePoultices`) check that
    none of their own players qualifies, rather than that nobody does: a fight in a neighbouring test within 64 blocks
    shares their players' hurt and notes;
  - the credit tests run their steps through a small timeline helper.
- **From the spec review:**
  - the order check (fouling taken, subtracted, then judged);
  - `REGENERATION_INTERVAL` held to `50 >> amplifier`, in `check_workshop_products`;
  - the bowl test's user stands in the test's own space, and only bowls inside its bounds are counted;
  - the recipe edges read once.
- **Tests (none run):**
  - `ConcordanceAlchemyGameTests` +7 (32 in all; 33 with the unit review's);
  - `ConcordanceAssayGameTests.workshopProductsAreNeverWeighed` (7);
  - `LairEncounterGameTests` +8 (18);
  - `EncyclopediaGameTests` `theCinderKilnsRiteNamesTheKilnDraught` and `theDossiersNameThePoulticesCredit` (20 with
    C-01's);
  - `crucibleInspection`'s poultice preview (shot `jugcraft_encyclopedia_inspect_poultice`).
  Every number in their assertions is the model's or vanilla's arithmetic until they run.
- **Commands,** for each commit:
  - run and passed: as C-01's;
  - the audit mutations on a scratch copy, each reported: 30 for commit 3 and 22 for commit 4;
  - not run: the build, the game tests and CI.

**Fixes from the unit review (unit 1, a sixth commit).** The review confirmed thirteen findings; each is fixed here, and
none of the changed Java has run in a game.
- **Equal doses** (C-02). `Mixture.bottle` gave each dose a part's share rounded down and left the remainder for the
  last, so a two-part poultice bottled 1.063 then 1.064: two different salves, and an automated crucible paused on its
  own second dose (`output_differs`). Now every dose of a batch is the same share (`Vector.times`); what does not divide
  is lost, at most a thousandth a part an axis, so nothing is gained. The generator's `bottle` mirrors it,
  `check_workshop_products` holds both, and `dilutionNeverHealsMore` expects 1.063 twice and runs the two-part batch as
  a formula into one stack of two.
- **The risk line hid a live batch** (C-01, 03:54). Set over a mixture already in the crucible, or under a formula the
  rules refuse, the risk line judged only the formula's replay, though a dose of the mixture can still be bottled by
  hand. `Assay.risks` now takes whether the mixture is the formula's own batch and names its harm and searing too;
  the Inspect page passes it through `CrucibleInspection.State`, and `formula()` passes the mixture it is set over.
- **Harm as the server judges it** (C-P29). The risk line asks `Alchemy.harmfulStatus` (a harmful or unknown status) as
  well as the property's data.
- **The refusal a formula item sends** is built by `CrucibleBlockEntity.refusal`, which
  `aWrittenFormulaThatNoLongerReplaysSaysWhichStep` asserts against step 2's refusal.
- **Preserve's hold** is the reason only while, unheld, the block beneath would move the heat towards the stir's band;
  over a magma block a hot stir waits for heat, held or not (`aHoldIsTheReasonOnlyWhileItKeepsTheHeat`, new).
- **The dossiers' poultice credit** is named only while a salve of Regeneration alone is the Stillroot Poultice, the
  match `Alchemy.product` makes for the credit.
- **Audits made exact:** `canPlaceItem`'s two returns and the mortar's whole refusal condition, so a widened condition
  fails; `formula()`'s refusal and risk line; the Inspect page's risk-line inputs; the dossier's product match.
- **Words:** the searing danger lines also say a searing stir thaws all Rime.
- **Records:** the Peepo mappings each test asserts
  ([companion-farm-stand-crucible.md](companion-farm-stand-crucible.md)), with the Peepo status now asserted at the
  `NEEDS_WATER`, `NEEDS_BOTTLES`, `NO_INPUT`, `OCCUPIED` and `WRONG_TEMPERATURE` waits;
  commits 1 and 2's mutations (C-01's Commands); every change needing the Alembic Arts
  ([arcane-concordance-authority.md](arcane-concordance-authority.md)); `follow(Formula)` and the credit row's chunks in
  `WHAT_EXISTS.md`; the client test's comment, and the Assay Glass put back after its crucible shots.
- **Commands:** as C-01's, run and passed; the fixes' twelve audit mutations on a scratch copy, each reported. The pure
  alchemy packages compiled with `javac` against a stub of jspecify's `@Nullable`, and a scratch harness replayed the
  new assertions' alchemy against the real data and agreed (the risk lines with a stand-in for the registry's harm
  test). Not run: the build, the game tests and CI.

### C-03. Ritual readiness, fault positions, spent and reserved (unit 2)

**What the player sees.**
- **The Inspect page on an anchor**, for the rite the offerings point to (or the closest known one), lists every
  shortfall at once: each offering short; Ley per pylon for one step and for the whole run against what it holds
  (`RitualDefinition.leyPerRun`, `:100-102`); pylons that will not lend; participants; the viewer's Focus; light; every
  shape fault with its relative and absolute position. Then the rite's **stage and payment table** (C-04's form):
  what Begin pays, what each step draws, what completion consumes, what an interruption keeps and loses, and the
  **backlash** a broken containment deals each participant, stated before the start (03:122).
- **During a run** (gathering and channelling), the Inspect page and Jade show what is **spent** (Focus × joined; Ley:
  done × Ley × channels) apart from what is **still reserved** (the offerings), and the interruption line says both
  (03:107; 10:48).
- A refused start, an empty-hand status reading and a chat status mark every faulted part with a WANT or PERIL sign, not
  only an interruption. The Inspect page shows none: it only reads.
- Jade shows the participants needed and the offerings. An interruption's reason stays readable, in one line with
  "kept".

**Changes.**
- A pure `concordance/ritual/Readiness`, built from the anchor's cached shape report
  (`CircleAnchorBlockEntity.java:521-534`), at most 16 pylon `ley()` and `lends()` reads (a pattern has at most
  `StructurePattern.MAX_CHANNELS` = 16 channels, `ritual/StructurePattern.java:21`; the Lesser Circle has four), the
  viewer's Focus and knowledge, participants present and the light. No second validator (12:174). Its lines use C-01's
  shared vocabulary.
- The selection loop (`CircleAnchorBlockEntity.java:306-329`) becomes one function that `begin` and readiness share;
  unknown rites stay unnamed (`InspectPage.java:66-80`).
- **The start does not change** (C-P10): one step's Ley (`CircleAnchorBlockEntity.java:508`). A run-level shortfall is a
  warning, so a pylon fed by JE (64 JE/t, about 2.56 Ley a 40-tick step by arithmetic, `LeyPylonBlockEntity.java:39-40`)
  can still sustain a run.
- **Signs only on the empty-hand, chat and refusal paths** (`CircleAnchorBlockEntity.java:216-258, 330-360`), reusing
  the interruption's loop (`:658-668`) within the budget (48 a tick plus 16 warnings,
  `concordance/sign/Signs.java:29-31`). Not inside the anchor's `status(level, out)`
  (`CircleAnchorBlockEntity.java:421-437`), which the Inspect page calls (`InspectPage.java:62`) and which must only
  read (`InspectPage.java:33`); `Signs.show` sends to every player tracking the block within its reach
  (`Signs.java:95-113`).
  `Signs.show` joins the encyclopedia's forbidden list (`check_mod_data.py:16174-16180`).
- The reason and "kept" become one combined overlay line (`CircleAnchorBlockEntity.java:650-656`).
- Jade: the anchor snapshot (`ConcordanceDataProvider.java:119-136`) adds the participants needed, the offerings and,
  during a run, spent and reserved; the client fixes the "?" (`JugcraftJadeClient.java:290`) and uses the dormant keys.
- A checker rule in `check_rituals` (`check_mod_data.py:12330`): steps × Ley ≤ a pylon's capacity (64,
  `LeyPylonBlockEntity.java:37`) for every rite. No rule compares them today: `check_rituals` holds each number to its
  own limit only, and those limits allow 30 steps × 16 Ley = 480 (`RitualDefinition.MAX_STEPS`, `MAX_LEY`,
  `ritual/RitualDefinition.java:28-29`).

**Reuses unchanged:** `RitualMachine`, `RitualRun`, `StructureValidator`, `Offerings`, the Signs budget, the encyclopedia
payloads.

**Tests (server):**
- `ConcordanceRitualGameTests` (18 today): `anIdleCircleSaysEverythingARiteStillNeeds`; `aRefusedStartMarksEveryFault`
  (`SignWatch`); `aStatusReadingMarksFaultsButTheInspectPageDoesNot`; `spentAndReservedAreShownApart` (mid-run and after
  each interruption kind); `theInterruptionReasonIsKept`; `unknownRitesStayUnnamed`.
- `theCircleIsCheckedPartByPart` must keep passing unchanged: its comment says charge and ownership count only when a
  ritual asks, and it asserts that someone else's pylon and a pylon short of a step's charge each refuse the start,
  taking no Focus (`ConcordanceRitualGameTests.java:229-240`).
- `EncyclopediaGameTests`: the anchor's Inspect readiness and payment lines
  (`theInspectPageShowsARitesReadinessAndPayments`).

**Tests (client):** an anchor's Inspect page (`jugcraft_encyclopedia_inspect_readiness`); a Jade shot of a gathering
anchor (`jugcraft_concordance_anchor_gathering_jade`, when Jade is loaded).

**Acceptance:** "Show missing positions and resource readiness" (09:132); 03:107; 03:122; 10:48.

**Save effects:** none. **Network:** no new payload; Jade's anchor snapshot carries more values.

**Review boundary:** ritual diagnostics only; no change to `RitualMachine`.

**As built (unit 2: commit `a0e73881c`).**
- **Built:**
  - the pure `ritual/Choice`: the one choice of rite a leader's start and readiness share (`CircleAnchorBlockEntity.choose`);
    nothing offered chooses nothing;
  - the pure `ritual/Readiness`: `before` (every shortfall before a start, each marked as refusing or not), `running`
    (spent apart from held, and what may stop the next step, the light included), `stages` (the stage and payment table
    from the rite's data, for every rite), `shapeFaults`; with `Spent`, `Held`, `Ending`, `Channel`, `Line` and
    `Reader`;
  - the anchor's `readiness` (the cached report, one reading of each channel, the reader's Focus and place, the light;
    no sign), `spent`, `ending` and `endingLine`; `participantsNeeded` made public for Jade;
  - signs: a reading in chat (an empty-hand reading, the command) marks every faulted part, once every `MARK_TICKS` (20)
    ticks a player (`markReadiness`); a refused start marks the parts that refused it (`markFaults`, which an
    interruption now shares). Nothing on the Inspect page's or Jade's path shows a sign;
  - an interruption in one line (`circle.ended`, `.ended.spent`: why, where the offerings are, the Focus and Ley Charge
    lost), its `Ending` kept in memory until the next start or a load; `circle.kept` gave way to `circle.release.kept`
    and `.release.dropped`;
  - fault positions as "x, y, z (dx, dy, dz from the anchor)" (`circle.where`); Jade keeps "x,y,z";
  - the Inspect page's anchor branch: readiness, or spent and held while a rite runs, the last attempt, and the stage
    table, for a rite the reader knows;
  - Jade: the participants needed (the "?" fixed), how many are channelling, the first three offerings and "+n",
    spent and held while a rite holds the anchor (`ConcordanceDataProvider.SHOWN_OFFERINGS`);
  - five diagnostic codes appended to `Code` and `tools/concordance_diagnostics.py` (`not_lent`, `needs_focus`,
    `too_bright`, `too_far`, `participants`); `unavailable_power` and `incomplete_pattern` given their parameters, and
    `missing_ingredient`'s words made general (C-P33); `ComposeText` names a `structure`;
  - audits: `check_rituals` (steps × Ley Charge within a full pylon's 64; every rite at the anchor on one structure; one
    choice; the read-only paths; the chat reading's marks and gate; a refused start's marks and the one-step start; one
    `tell` an interruption; the readiness words; Jade's anchor snapshot keys, and that it names no rite, Focus or
    research; the client's "?" gone) and `check_encyclopedia` (`markReadiness`, `markFaults` and the chat reading
    forbidden on the encyclopedia's path).
- **Departures from the ticket,** each a proposal below or a spec reconciliation:
  - "participants" is not an idle shortfall: a start needs only its leader. Before a start the others appear as the
    stage table's gather row; while a rite gathers, a `participants` line shows joined, needed and the seconds left
    (C-P34);
  - each shape fault is not repeated in readiness: one `incomplete_pattern` line counts the faults to mend, and the
    anchor's status lines above it, on the same page, list each with both positions;
  - a pylon short only of the whole run is a warning naming the step the run would break off at; the start is unchanged
    (C-P10);
  - a fully matched rite the reader has not learned is never named on the Inspect page or in Jade; the start's refusal
    still names it, as before (C-P34);
  - a joined participant who has walked away is not listed while a rite runs: the next step's own check finds them;
  - the held line leaves out an item the client does not know, rather than showing an empty icon;
  - in the tests, pylons are set with `setLey(level, n)`; the idle test waits 5 ticks after placing glowstone, as
    `theVigilNeedsTheDark` does, before reading the light; the client circle for the Jade shot is walled and roofed so
    the vigil's light check holds at any hour (the overworld's midnight sky light was not confirmed), with
    `time set midnight` kept as well.
- **From the spec review:** `InspectPage.anchor` declares its ritual list once (the spec's block would not have
  compiled); running readiness reads the light, as the ticket's "what may still stop it" asks; the Jade shot is taken
  under night vision so the sealed circle is not black (night vision changes only the client's rendering, not the
  server's light); the client check rejects `jugcraft:` ids as well as keys; `incomplete_pattern` reads "... is not
  complete: faults to mend: n"; the decisions are numbered C-P33 on, after unit 1's C-P32.
- **Tests (none run):**
  - 6 new `ConcordanceRitualGameTests` (24 then): `anIdleCircleSaysEverythingARiteStillNeeds`,
    `aRefusedStartMarksEveryFault`, `aStatusReadingMarksFaultsButTheInspectPageDoesNot`,
    `spentAndReservedAreShownApart`, `theInterruptionReasonIsKept`, `unknownRitesStayUnnamed`;
  - `EncyclopediaGameTests.theInspectPageShowsARitesReadinessAndPayments` (21);
  - the client test `EncyclopediaClientGameTests.anchorReadiness`, with shots `jugcraft_encyclopedia_inspect_readiness`
    and `jugcraft_concordance_anchor_gathering_jade` (when Jade is loaded).
  No test reads the action bar's one line: a mock player's overlay cannot be read. No test covers running readiness's
  light line; only the scratch harness below does.
- **Commands** for commit `a0e73881c`:
  - run and passed: `python3 tools/check_mod_data.py`, `python3 scripts/check_repository.py`,
    `python3 tools/owner_art.py --check`, `python3 tools/concordance_journey.py --check`,
    `python3 tools/check_icon_maps.py`, `node --test tools/world-designer/model.test.cjs`,
    `python -m unittest discover -s scripts/tests` and `git diff --check`;
  - the spec's audit mutations on a scratch copy, each reverted: all 19 caught (its 14, five of them in two variants);
    one more, running readiness without the light, is caught by no audit;
  - a scratch harness outside Git, re-run for the records at `2c98d6d68`: the pure Concordance packages compiled with
    JDK 21 and 37 checks of `Choice` and `Readiness` with the game tests' figures passed;
  - not run: `./gradlew build` (with or without the optional integrations), `./gradlew runClientGameTest` and CI.

### C-04. Root and Rivet (unit 2; C-D2)

**What the player sees.** A rite at the Lesser Circle turns one Thallite plate into a **Rooted Thallite Foundation**,
the base the Crucible Stand is built on (C-05). Its stage and payment table, on the Inspect page before the start
(C-P11 holds the numbers, starting values):

| Stage | When | Paid at this stage | Still reserved | Spent so far | If interrupted |
|---|---|---|---|---|---|
| Offer | the anchor idle | — | nothing (slots open) | — | a sneaking use takes the offerings back |
| Begin | the leader's empty-hand use | 6 Focus | the offerings: 1 Thallite plate, 4 copper ingots, 4 Mendvetch | 6 Focus | offerings kept and unlocked; the Focus is lost |
| Step k of 4 | every 40 ticks (8 s in all) | 2 Ley from each of the 4 pylons | the offerings | 6 Focus and 8k Ley | offerings kept; spent Ley and Focus lost; a broken containment also deals each participant 4 backlash |
| Commit | after step 4 | the offerings consumed and the foundation made, in one tick | — | 6 Focus, 32 Ley, the offerings | — |
| Complete | until a participant takes it | — | the foundation, in the anchor | — | the anchor broken drops it |

32 Ley is 48 Radiance or 48 Verdance through a pylon's conversion, or 32,000 JE (arithmetic); 8 a pylon fits its 64.
One participant; any light. Copper and Mendvetch are the chain's "copper/iron components" (06:78) and "Verdance-compatible
binding from existing garden materials" (06:80). The offerings collide with no existing rite's (the attunement's wand,
amethyst, gold and glowstone: `concordance/ritual/adept_attunement.json`); the most specific match wins
(`CircleAnchorBlockEntity.java:313-317`).
A completion credits Stratawrighting's `foundation` through Chunk B's listener (`Strata.java:34`).

**Changes.**
- Data `concordance/ritual/root_and_rivet.json`, a `Transform` (`RitualDefinition.java:63-64`): no machine change.
- Generator: `RITUALS` in `tools/concordance_rituals.py:112`; the JEI page is automatic for a transform
  (`recipe_station()`, `:593`).
- New item `rooted_thallite_foundation`: a 16×16 icon per `docs/ITEM_ICONS.md` (the owner's library searched first,
  CLAUDE.md), lang, a codex entry linked from Stratawrighting.
- Research (C-P11): Circle Lore understood, like the three rites (`concordance/research/circle_lore.json:53-61`).
- Registrations: `PRODUCERS` in `tools/concordance_progression.py:101` and `RITUALS` in `tools/acquisition.py:96` (each
  lists the attunement's wand today); the rite as a Ley sink in `tools/concordance_economy.py` (06:144).
- **Feature switches:** the plate's recipes load only with `machines` (both) and `thallite` (by hand):
  `recipe/thallite_plate_by_hand.json:2-11`, `recipe/pressing/thallite_ingot.json:2-7`. With either off the rite exists
  but its plate cannot be made, and the powered route (C-05) is out of reach; the campfire crucible is not.
  `check_rituals` states that dependency. The rules parser tolerates `fabric:` keys
  (`concordance/rules/RulesParser.java:671-679`), but whether Fabric applies load conditions to the Concordance's own
  loader is unverified and no Concordance data uses them, so the rite carries none.

**Tests (server):** `ConcordanceRitualGameTests`: `rootAndRivetAttunesOnePlateOnce`;
`aDryPylonStopsRootAndRivetAndKeepsTheOfferings`; `rootAndRivetSavedMidRunLapses` (the pattern of
`aRitualSavedMidRunLapses`); `theAttunementsOfferingsNeverChooseRootAndRivet`; `rootAndRivetIsStrataFoundationWork`.
`ConcordanceAssayGameTests.theFoundationIsNeverWeighed`. `ConcordanceProgressionGameTests`: the node
`ritual:jugcraft:root_and_rivet` joins the list at `ConcordanceProgressionGameTests.java:130-136`.

**Acceptance:** "Implement one of Stillwater Keeping or Root and Rivet with a declared stage/payment table. Use existing
RitualMachine and pylon economics" (09:132); interruption tested (09:140).

**Save effects:** new stable ids `jugcraft:root_and_rivet` and `jugcraft:rooted_thallite_foundation`, in use from the
first release.

**Review boundary:** ritual data, one item, the generator and audit entries.

**As built (unit 2: commit `2c98d6d68`).**
- **Built:**
  - `concordance/ritual/root_and_rivet.json`, generated from `RITUALS` in `tools/concordance_rituals.py` with C-P11's
    numbers; Circle Lore understood unlocks it;
  - the item `rooted_thallite_foundation` (`JugcraftConcordance.ROOTED_THALLITE_FOUNDATION`, a `DescribedItem` in the
    Ingredients tab), its lang, models and icon;
  - the codex entry `circles/root_and_rivet`: the rite, "Stages and Payment" (`_payment_text`), the plate's crafting
    recipe with the switches it needs (`_switches_text`), and the foundation; Stratawrighting's Understood page links it,
    the codex's first `entry://` link;
  - JEI: the rite in the Circle Anchor category and an information page on where the foundation comes from
    (`tools/acquisition.py` `RITUALS`);
  - registrations: `PRODUCERS` in `tools/concordance_progression.py`, `EXCLUDED` in
    `tools/concordance_equivalence.py` (and the generated tag), and `RITUAL_SINKS` in `tools/concordance_economy.py`
    with its row in the economy record's table.
- **Three checker gaps found by the design and fixed:**
  - `check_rituals` looked an offering's recipe up by file name; it now counts any recipe, in any recipe folder, whose
    result the offering is (the plate's hand recipe is `thallite_plate_by_hand`, its pressing `pressing/thallite_ingot`);
  - `#c:ingots/thallite` joined the progression's `VANILLA_TAGS`, so the plate's hand recipe has a reachable input;
  - `check_progression` asks for a codex recipe page readable before a step needs the plate: the entry's crafting page.
- **The switch statement** (C-P41): `RITUALS` states the feature switches a rite's offerings need beside the
  Concordance's (`"switches": ["machines", "thallite"]`), and `check_rituals` holds the statement to the recipes, one
  step down, an offering that is a tag included. Concordance data carries no load condition, and the checker forbids
  one.
- **Also held by the checks:** what a ritual makes is excluded from the scale, made by that ritual in the progression
  graph and given its JEI page (`check_rituals`); every codex `entry://` link names an entry on disk, the foundation's
  icon is its map pixel for pixel, and the `thallite` material's four tones are the metal's (`check_concordance`); the
  sink is its ritual's run (`check_economy`).
- **The icon:** the owner's library was searched first (its candidates are in the rituals record's Item-icon
  provenance); nothing fitted, so it is drawn fresh to ITEM_ICONS.md from `tools/item_icons/rooted_thallite_foundation.txt`
  in a new `thallite` material, the Thallite metal's ramp with a darker outline. Not yet reviewed by the owner.
- **Departures from the spec:**
  - the icon's PNG was written by the texture generator's own `save` for this one image, from
    `concordance_ritual_art.textures()`, rather than by running `tools/generate_textures.py` whole, which this unit's
    instructions ruled out; `check_concordance` holds it to its map;
  - from the spec review: the codex's payment page reads "k times 8 Ley Charge" and names Verdance beside Radiance and
    JE; the JEI page says "Root and Rivet turns", without "the", and only an instrument "keeps what was inscribed";
    `RITUAL_SINKS`' comment says why the Adept's Attunement is not a sink (its wand is an excluded instrument); the switch
    audit reads an offering that is a tag through its members; the five ritual tests, their helpers and the assay test
    were written from the review's code.
- **Tests (none run):**
  - 5 new `ConcordanceRitualGameTests` (29 in all): `rootAndRivetAttunesOnePlateOnce`,
    `aDryPylonStopsRootAndRivetAndKeepsTheOfferings`, `rootAndRivetSavedMidRunLapses`,
    `theAttunementsOfferingsNeverChooseRootAndRivet`, `rootAndRivetIsStrataFoundationWork`;
  - `ConcordanceAssayGameTests.theFoundationIsNeverWeighed` (8);
  - `ConcordanceProgressionGameTests.theLiveGraphIsWhole`: the node `ritual:jugcraft:root_and_rivet`, reachable alone;
  - the client shot `jugcraft_rooted_thallite_foundation` (`EncyclopediaClientGameTests.rootedFoundation`).
- **Commands** for commit `2c98d6d68`:
  - run and passed: as C-03's;
  - the spec's audit mutations on a scratch copy, each reverted: its 13 in 22 variants and one more for a tag offering,
    23 caught. Mutation 13 (Mendvetch deleted from `_item_name`) surfaced as a crash (`KeyError`), not as a reported
    error. A control (C5', correction 5's `offering_needs` undone, the spec's own loop) raised no switch error for the
    tag offering: it failed only on the scratch rite's unrelated research, lang and progression errors, so the spec's
    loop misses a tag offering. A foundation-to-Ley conversion of 31 is, as the spec expects, not a gaining cycle;
  - `python3 tools/select_client_tests.py --base 807da7f5e` chose `EncyclopediaClientGameTests`;
  - not run: the build, the game tests and CI.

**Fixes from the unit review (unit 2, a fifth commit, after the merge `2263f851b`).** The review confirmed seventeen
findings. Sixteen are fixed here or in the merge; the seventeenth, a commit message, is left as written. None of the
changed Java has run in a game.
- **The branch had moved on.** Unit 2 was built on `807da7f5e`, and the branch has since gained `d593ae61d` (unit 1's
  review fixes) and `0b1039312` (the Rime and Strata client tests). They were merged in (`2263f851b`), not rebased, so
  the commits these records cite keep their hashes and the branch moves forward without a force-push. Two conflicts
  were resolved: the status paragraph, and an import block in `EncyclopediaClientGameTests` (both kept). Commit
  `2c98d6d68`'s message says "a third rite" where Circle Lore unlocks a fourth; rewording it would rewrite that commit,
  so it stands.
- **A pylon gone while a rite runs** (C-03). While a rite runs the anchor shows the last step's shape report, and
  running readiness skipped a channel with no pylon, so a pylon removed or unloaded said nothing until the next step
  broke the rite off. `Readiness.running` now counts such channels in one blocking `incomplete_pattern` line, and
  `spentAndReservedAreShownApart` asserts it with one channel read as gone. Another part broken since the last step
  still shows only at the next step (Risks).
- **The whole-run warning** ends "unless it is refilled meanwhile" (`ritual.ley_run`, `ritual.ley_left`): on JE a pylon
  gains about 2.56 Ley Charge a step, more than any rite draws from it (C-P10).
- **Marks only for what blocks** (C-P35). A reading in chat marks a pylon for a blocking line (short of a step, or not
  lent), never for a warning (`markReadiness`).
- **Words.** The held line reads "Held until it ends, and consumed only if it completes", since a rite that breaks off
  keeps or drops its offerings. The ending line reads "the attempt lost %s Focus and %s Ley Charge": the Focus is every
  participant's together, and each participant is told it (C-P37).
- **The switch audit** (C-P41) follows a machine recipe's `{"ingredient", "count"}` entries (an advanced circuit also
  needs `lead` and `silicon`), and counts a condition with `"or"` alternatives as needing none of its switches alone (a
  chestnut slab needs no `agriculture`). Every rite's result is unchanged: Root and Rivet needs `machines` and
  `thallite`.
- **The codex-link audit** (C-P42) also holds a link's `@anchor` to a page id of its entry and its `#n` to one of its
  pages, as Modonomicon 2.16.0's `BookLink.fromEntry` does.
- **The economy's ritual sink** words its offerings from `tools/concordance_rituals.py` (`offerings()`), as the JEI page
  does, so the economy table follows the rite; its row is re-pasted from the tool.
- **Records.** C-04's mutation count above (23 caught, a control, mutation 13 a crash). Unit 2's insertions marked "not
  merged, not run" in [arcane-concordance-rime-strata.md](arcane-concordance-rime-strata.md),
  [arcane-concordance-economy.md](arcane-concordance-economy.md), [thallite.md](thallite.md) and
  [jei-integration.md](jei-integration.md). Thallite's switch sentence (a plate made earlier can still be offered). The
  icon table's full path for `table_plate.png` and the rituals record's lang list ("among them"). Two more unconfirmed
  risks: the codex pages' length, and the recipe page when a switch removes the plate's recipe.
- **Commands** for the merge and the fixes:
  - run and passed on the merged tree with the fixes: `python3 -B tools/generate_material_data.py` (the merge
    regenerated unchanged; the fixes changed only the lang), `python3 -B tools/check_mod_data.py`,
    `python3 scripts/check_repository.py`, `python3 tools/owner_art.py --check`,
    `python3 tools/concordance_journey.py --check`, `python3 tools/check_icon_maps.py`,
    `node --test tools/world-designer/model.test.cjs`, `python3 -m unittest discover -s scripts/tests` and
    `git diff --check`;
  - the fixes' audit mutations, in place and each reverted. Reported: fix 4 undone (a scratch rite offering an
    advanced circuit, stated with its five switches), fix 5 undone (one offering a chestnut slab, stated with none),
    a link to `@p9`, `#9` or `#x`, and Root and Rivet taking 5 copper ingots without the economy table re-pasted. Not
    reported, as intended: a link to `#1`, `@p1` or `#3@p0`. Controls: each scratch rite with its fix in place, a link
    to `@p9` under the old link audit and the copper change under the old hand-written offerings were not reported,
    which is what the old audits missed;
  - C-03's and C-04's suites re-run in place on the fixed tree: the same results as above (C-03's 19 caught, its extra
    light case missed; C-04's 23 caught, mutation 13 a crash, C5' raising no switch error);
  - the pure readiness classes compiled with `javac` against a stub of jspecify's `@Nullable`, and the scratch harness
    passed every check, three new ones included (a pylon gone blocks; every pylon there says nothing; a completed run
    says nothing);
  - `python3 tools/select_client_tests.py`, on the fixes as staged: against the branch's tip `0b1039312` (unit 2 with
    its fixes) it chose `EncyclopediaClientGameTests`, and against the merge `2263f851b` (the fixes alone) the same.
    Against `807da7f5e` it now chose ten classes, because unit 1's review fixes and the Rime and Strata client tests are
    in that range too;
  - not run: the build, the game tests and CI.

### C-05. The Crucible Stand: JE heat for the next stir, piped water (unit 3; C-D3)

**What the player sees.**
- A stand under a crucible, crafted from the Rooted Thallite Foundation, iron and copper (C-P12), takes JE from cables.
  With a formula running it heats toward the band of the formula's next stir: cold off (the crucible settles at the
  ambient 20), warm 65, hot 120, searing 175 (starting values), drawing about 8, 16 and 32 JE a tick (C-D3's starting
  rates). A formula of mixed bands replays unattended; today a crucible waits silently for its band
  (`CrucibleBlockEntity.java:276-280`).
- It idles, drawing nothing, while the crucible waits for anything but heat (water, an ingredient, a container, room,
  a hold) or its formula is incompatible: no unlimited burning (03:193). A Preserve hold wins over the stand.
- With no formula, an empty-hand use by someone who may change the stand cycles off, warm, hot, searing (06:115's "select
  a known heat schedule"), chosen on the server and rate-gated; no client slider (08:59).
- Unpowered it gives nothing, and the crucible cools by the existing rule (`Heat.java:25-31`).
- Water comes from an Electric Pump on a water source (8 JE a tick, paid only on ticks it moves fluid; a source block is
  an endless spring: `fluid/ElectricPumpBlockEntity.java:41, 101-104, 123-125`) through a pipe into the crucible's tank,
  which takes water only and never gives it back (`CrucibleBlockEntity.java:124-145`; `JugcraftConcordance.java:259`).
- A campfire crucible works exactly as before. Jade on the stand shows its energy, setting and draw.

**Changes.**
- A block and block entity with a `LIT` state: a `SimpleEnergyStorage` registered on `EnergyStorage.SIDED` as the pylon's
  is (`JugcraftConcordance.java:242`; `LeyPylonBlockEntity.java:47-48`) and `EnergyConnectable` (`LeyPylonBlock.java:42`).
  It pays JE only on a tick when its target is above ambient and a crucible sits directly above (one `getBlockEntity`,
  no scan), and only while the crucible's reason (C-01) allows.
- `CrucibleBlockEntity.heatBelow` (`:233-241`) asks the stand when the stand is beneath. The targets are constants in
  `Heat`, mirrored beside the heat constants in `tools/concordance_alchemy.py:33-36` as `Heat.java:5-10` requires.
- **Authority** (C-P12): the manual setting asks `Authority.mayChange`.
- **Feature switch:** with `concordance` off the stand draws no JE and gives no heat, as the crucible stops
  (`CrucibleBlockEntity.java:213, 388`).
- **No wear and no maintenance** (D-004, 10:157).
- **Economy:** a one-way "JE → heat only" row in `tools/concordance_economy.py` and `arcane-concordance-economy.md`.
- **Art:** the owner's library is searched first (CLAUDE.md; candidates listed in `art/owner-library/catalog/files.csv`,
  suitability not yet judged: `heating_tray_side.png` and `heating_tray_top.png` in "farming and food textures", the
  treated-iron set in `Blocks/Guns/block`); an import goes through an `OWNER_FILES`-style table so
  `owner_art.py --check` protects it (that file is also changed by #351 and #354), otherwise generated in code as the
  crucible's GeckoLib model is (`tools/concordance_alchemy.py:531-532`). It follows main's two new art rules
  (`docs/ART_DIRECTION.md`: supported parts and purposeful pipes; silver-grey cladding) and gets power-port sockets
  through `machine_connections.with_model_sockets`
  (`tools/machine_connections.py:75`), as the pylon does (`tools/concordance_rituals.py:527-529`). Owner visual review is
  a checkpoint item (09:144).
- **Jade:** a provider for the stand (`compat/jade/JugcraftJadePlugin.java:30-45` registers one per block-entity class).

**Tests (server):**
- `ConcordanceAlchemyGameTests`: `aStandFollowsTheFormulasBands` (a warm-then-hot formula replayed twice, unattended);
  `anUnpoweredStandLetsTheCrucibleCool` (the reason is wrong heat; the Peepo port says `WRONG_TEMPERATURE`, as it does
  today for a stir out of its band, `CrucibleCompanion.java:100`);
  `aStandIdlesForAWaitingOrIncompatibleFormula`; `aStandIsSetOnlyByThoseWhoMayChangeIt`;
  `theFullChainRunsFromAPumpAndACable` (a creative cell, a cable, the stand; a pump on water, a pipe, the tank; the exact
  water drawn); `aCampfireCrucibleStillWorks`; `aSwitchedOffConcordanceStandDrawsNothing`.
- `ConcordanceEconomyGameTests` (2 today): `aStandTurnsElectricityIntoHeatOnly` (JE drawn equals rate × heated ticks; an
  idle stand draws nothing; nothing comes out).
- `ConcordancePersistenceGameTests`: the stand's save and stamp.
- A cable connect-and-retract check after main's `MachineConnectionGameTests` (`:27`, `:82`), which covers only machine
  blocks.

**Tests (client):** a workshop shot at the ordinary scale.

**Acceptance:** "Connect an existing station or a minimal adapter to JE/water" (09:136); "A powered version improves
convenience" (09:140); the industrial apothecary "does not unlock all magic automatically" (03:25): research, exact
ingredients and process still gate it.

**Save effects:** a new block entity (energy, manual setting); new stable ids `jugcraft:crucible_stand` (block, item,
block entity). An older build drops the unknown block.

**Review boundary:** one block, the heat hook, the economy row, its art and tests.

### C-06. One shared job: keep this crucible's formula supplied (unit 3; C-D4)

**What the player sees.** A crucible running a formula that waits for an ingredient asks the nearest Courier Post its
formula's setter may use for exactly the shortage; a Clockwork Porter fetches it. A Peepo assigned to the crucible fills
the same need without over-supplying. A full crucible keeps the rest aboard the porter until there is room; an unloaded
one waits with the cargo intact; cancelling returns it.

**Changes.**
- **One demand:** the shortage moves into the crucible (from `CrucibleCompanion.shortage()`, `:39-47`, private today),
  and both carriers read it. It is nothing for an incompatible formula (C-01).
- **Filing** (C-P13), after `ConcordSpire.restock` (`concordance/spire/ConcordSpire.java:557-581`), with its
  `nearestPost` (`:583-603`, 9 chunks of block entities a call) extracted as a shared helper, not copied:
  - requester: the formula's setter, recorded when it is set (`CrucibleBlockEntity.java:505-518`); destination: the
    crucible; the exact `ItemVariant`, so a ground reagent stays exact (`CourierLedger.java:170-176`); posts within 16
    blocks (`ConcordSpire.java:106`) the setter may use;
  - at most one open request; filed when the crucible starts waiting for an ingredient, not on every 20-tick step, with
    a backoff after finding no post or after `TOO_MANY`;
  - `TOO_MANY`: a crucible's requests count against its setter's 16 and the world's 1,024
    (`concordance/logistics/Logistics.java:37, 39, 121-127`); the crucible shows a courier-quota reason and backs off, so
    many crucibles cannot spend a player's quota in a loop;
  - **lifecycle:** the request is cancelled (its cargo goes back) when the formula is cleared
    (`CrucibleBlockEntity.java:533-553`), replaced,
    set by a different player or turns incompatible;
  - **removal:** a `preRemoveSideEffects` override calls `CourierLedger.destinationGone` (`CourierLedger.java:273-283`)
    and still drops
    the slots, as the Spire Heart does (`concordance/spire/SpireHeartBlockEntity.java:216-223`), so
    `aBrokenCrucibleDropsItsItems` (`ConcordanceAlchemyGameTests.java:354`) keeps passing.
- **The Peepo side:** `CrucibleCompanion.needed` (`CrucibleCompanion.java:49-57`) subtracts the open request's remainder.
- **Delivery is existing code:** a porter delivers into any container through `ItemStorage.SIDED.find(level, to, null)`
  and reports `FULL` or `DESTINATION_UNLOADED` (`ClockworkPorterEntity.java:387-406`).
- **The failure cases of 03:193:**

| Case | What happens | Shown by |
|---|---|---|
| Destination fills | the rest stays aboard, noted "full"; delivered once there is room | new: `aCourierFillsACrucibleUntilItIsFull` |
| Access removed | filing needs a post the setter may use (`CourierPostBlockEntity.mayUse`); a source its keeper may not open releases the claim, `BLOCKED_BY_ACCESS` (`ClockworkPorterEntity.java:424-427`) | new: `aPostTheSetterMayNotUseIsNeverAsked` |
| Path fails | the porter gives up after a failed walk and releases the claim; the cargo stays with the request (`ClockworkPorterEntity.java:378-385`). Not 03:193's "three times": existing behaviour, unchanged | existing code; re-run `ConcordanceLogisticsGameTests` |
| Target chunk unloads | `DESTINATION_UNLOADED`, no ledger step, the lease renewed (`ClockworkPorterEntity.java:389-392, 335`) | new: `anUnloadedCrucibleWaitsAndLosesNothing` |
| Entity dies | a discarded porter strands its cargo, recovered exactly once | new: `aDiscardedPorterStrandsItsCargoOnce` |
| Owner logs out | a request is served only while its requester is answering: `OWNER_OFFLINE` (`ClockworkPorterEntity.java:324-328`); a known limit | new: `anOfflineSetterPausesTheCourier` |
| Source broken | the claim is released, `source_gone` (`ClockworkPorterEntity.java:423-427`) | existing code |
| Server restarts | the ledger's codec and the crucible's keys round-trip | new: the codec round trip; `ConcordancePersistenceGameTests` |

**Tests (server):** `ConcordanceLogisticsGameTests` (5 today): the new tests above, plus
`aCrucibleAsksOnceForItsExactShortage`, `aClearedOrReplacedFormulaCancelsItsRequest` (cleared, replaced, another setter,
incompatible), `aFullCourierQuotaWaitsAndBacksOff` and `aBrokenCrucibleTurnsItsRequestBack`. Each audits `problems()`
and checks chest + crucible + carried = N after every step.
`ConcordancePersistenceGameTests.theCrucibleKeepsItsRequestThroughASave`: the keys present; absent (an older save) they
read as none; the stamp stays 1 (`CrucibleBlockEntity.java:671`).

**Tests (client, Peepo):** in `PeepoIntegrationClientTests` and `CompanionAutomationChecks` (existing classes): a
programmed formula's `needed()` is the exact shortage; a full crucible leaves the companion's cargo with it;
`noOverSupplyWithAnOpenCourierRequest`; an unloaded crucible: the job reports `UNLOADED` and keeps its cargo (no test
under `src/gametest` checks `CompanionStatus.UNLOADED` today); an incompatible formula asks for nothing.

**Acceptance:** "one shared worker job … Demonstrate a full destination and an unloaded target without duplication"
(09:136), for both carriers (C-D4); 10:50-51; 03:191's cancel and recover.

**Save effects:** optional crucible keys `formula_owner` (UUID) and `request` (long), read with defaults; the stamp
stays 1.

**Review boundary:** crucible demand and filing, the shared post helper, the port edit and tests. No edit to
`PeepoEntity`, `CompanionHusbandry` or the other files #357 and #369 change.

### C-07. Habitat made visible (unit 4)

**What the player sees.**
- The Inspect page on a Verdant Bed or a crop shows each factor's value against the organism's ranges (least, ideal from,
  ideal to, most: for example Sunpetal's light `[11,13,15,15]`), the verdict, and reasons that name the reach: "within 2
  blocks, at plant height" for diversity, "5×5, a block above and below" for disturbance (`Sampler.java:16-22`). Light is
  shown as sky and lamps (C-P14; niches unchanged).
- A bed's verdict clears once no crop stands on it; Jade on a ripe crop shows its last verdict.
- A canteen waters a Verdant Bed, as the docs and codex say (C-P15); JEI has the compost page.

**Changes.**
- `InspectPage` gains a bed and crop branch, from the server (the rules are server-only, `ConcordanceData.java:28`).
- A bed's random tick (`concordance/garden/VerdantBedBlock.java:70`) clears a stale note when no organism crop stands
  above it.
- Jade: a crop has no block entity, so no server data of its own (`JugcraftJadePlugin.java:30-45`); its lines read the
  bed's update tag (`JugcraftJadeClient.java:238-258`; `VerdantBedBlockEntity.java:242-251`). This ticket only stops
  hiding the verdict when ripe (`JugcraftJadeClient.java:254`); C-08 and C-09 grow the tag.
- The canteen (`GourdCanteenItem.java:89-90, 116`) waters beds; `compost_view()` (`tools/concordance_ecology.py:554-557`)
  goes into `recipe_view.json` with a JEI reader.
- The ecology record's counts and scan figures corrected (above).

**Reuses unchanged:** `Verdict`, `Niche`, `Sampler`, the bed's cached sample, the encyclopedia payloads.

**Tests (server):** `ConcordanceGardenGameTests` (12 today): `aBedSaysWhatItsPlantNeedsAndHowFar`;
`aVerdictClearsWhenThePlantGoes`; `aCanteenWatersABed`. `EncyclopediaGameTests.theInspectPageReadsABedAndACrop`.

**Tests (client):** a bed's Inspect page; a Jade shot of a crop.

**Acceptance:** "Make habitat requirements visible" (09:128); 03:79.

**Save effects:** none. **Review boundary:** read-only garden diagnostics and the two routine fixes.

**As built (unit 4: commit `5ba973632`).**
- **Built:**
  - the pure `ecology/HabitatInspection` (`State`, `unjudged`, `judged`, `verdict`, `pace`, `unplanted`): each factor's
    line names how far its reading reaches (moisture the bed's own, with water's reach of 4, `Garden.WATER_REACH`; light
    as sky and lamps, the greater counting; the bed's nutrients; diversity within `Sampler.RADIUS` 2 at the plant's
    height; disturbance in the 5 by 5, `Sampler.HEIGHT` 1 above and below), judged by the verdict's own fit against all
    four of the range's bounds, or "any will do" for a factor the niche leaves open;
  - `VerdantBedBlockEntity.inspect` (the habitat growth would read, from the cached sample, and the light's two layers;
    it only reads) and `forgetVerdict` (the verdict only, synced and saved), which the bed's random tick calls once
    neither a crop nor a Verdant Heart stands on it (defect 13);
  - the Inspect page's garden branch (`InspectPage.GARDEN_REACH`, 1.0 beyond the reader's block reach, as the
    crucible's): a bed, or a crop through the bed below it; a dormant bed is not read, a bare one is unjudged, a crop
    with no bed says so; codex links to `garden/habitats` (encountered) and `garden/organisms` (observed);
  - Jade: a ripe crop shows its bed's last verdict, "As it grew: ..." (defect 13);
  - `VerdantBedBlock.useWithoutItem` passes for any item in the main hand (defect 14), and `VerdantBedBlock.pour`, which
    the Gourd Canteen's new `BED` case calls: a bed below 7, `RateGate` ("garden", 4 ticks), then `Authority.mayChange`
    (a refusal shows `missing_permission`); a sip is spent only when it wets (defect 11);
  - JEI: `JugcraftJeiPlugin.addCompostInfo` reads `recipe_view.json` `garden_compost` (`tools/recipe_view.py`, from
    `compost_view()`): an information page for each item of the Maw's tags, at its highest tag's worth (defect 12);
  - lang: `compose.jugcraft.ecology.inspect.*`, `encyclopedia.jugcraft.inspect.garden.habitat` and `.here`,
    `tooltip.jugcraft.concordance.jade.as_it_grew`; the codex's habitats page names the Inspect page;
  - audits: `check_ecology`'s C-07 block, `check_encyclopedia`'s garden reach block and read-only list (`.note(`,
    `forgetVerdict(`, `forgetSample(`, `.take(`, `.give(`, `.grow(`, `.hasten(`), the client-read rule's needles
    (`Garden.catalog()`, `Garden.organism(`, `.organism()`, `.niche()`) and `AUTHORITY_ROUTES` for `VerdantBedBlock`.
- **Departures from the ticket and the spec:**
  - the held-item rule (the spec's R2) is C-07's, not C-08's, so the canteen needs no pass of its own in `useItemOn`;
  - from the spec's critique: the short and excess judgements name all four bounds (its correction 8);
    `aBedSaysWhatItsPlantNeedsAndHowFar`, `aVerdictClearsWhenThePlantGoes` and the Inspect test prefetch their samples
    at ticks 10, 20 and 30 (4; `aCanteenWatersABed` takes no sample); the client section holds random ticks at 0 while
    it runs (5);
  - built on `23f28b90e` beside unit 3, not on unit 3's head as the spec planned: `AUTHORITY_ROUTES`' bed line follows
    the Gleaner's, with no crucible line of unit 3's here; the decisions are C-P54 to C-P57 (the spec's placeholders
    C-P80 to C-P83), after unit 3's C-P43 to C-P53.
- **Tests (none run):** `aBedSaysWhatItsPlantNeedsAndHowFar`, `aVerdictClearsWhenThePlantGoes` and `aCanteenWatersABed`
  (15 garden tests then); `EncyclopediaGameTests.theInspectPageReadsABedAndACrop` (22); the client section
  `EncyclopediaClientGameTests.gardenInspection`, with shots `jugcraft_concordance_ripe_crop_jade` (when Jade is loaded)
  and `jugcraft_encyclopedia_inspect_bed`.
- **Commands** for commit `5ba973632`:
  - run and passed: `python3 -B tools/generate_material_data.py` twice (the second run changing nothing),
    `python3 -B tools/check_mod_data.py`, `python3 scripts/check_repository.py`, `python3 tools/owner_art.py --check`,
    `python3 tools/concordance_journey.py --check`, `python3 tools/check_icon_maps.py`,
    `node --test tools/world-designer/model.test.cjs` and `python3 -m unittest discover -s scripts/tests`;
    `git diff --check` on the commit's diff, run for these records: clean;
  - the spec's audit mutations: the commit's run left no count on record, so they were run again for these records, in
    place on the final tree (`d6edee4f4`), each restored and the tree unchanged afterwards: all 23 reported (its 18,
    five of them in two variants). Mutation 8, the pour without Authority, is reported by the pour's own audit;
    `check_authority`, which the spec also expected to report it, does not, because the bed block's tending keeps the
    same call;
  - not run: the build, the game tests and CI.

### C-08. One mixed-bed rule, and harvest strain with recovery (unit 4)

**What the player sees.**
- **A mixed bed** (C-P16): Dewmoss and Gloamcap, both shade plants (`concordance/organism/dewmoss.json` light
  `[0,0,9,12]`; `gloamcap.json` `[0,0,6,9]`), grow faster when the other kind grows within the 5×5 at plant height.
  Identical copies never count. The Inspect page and Jade say "mixed bed: +25%" (a starting value).
- **Harvest strain** (C-P17): a bed that was just harvested is strained, up to 3, and the strain counts against its
  plant's disturbance limit. It only ever worsens the fit: it never supplies disturbance a plant needs, so a harvested
  Gloamcap bed (`[1,3,10,15]`; 0 stalls it, `concordance/ecology/Niche.java:40-48`) still needs its magic blocks. Rest,
  compost and a Greenwarden's tending ease it. It never kills. Inspection and Jade say "recently harvested".
- **What strain costs:** Sunpetal (`[0,0,2,5]`) stays ideal at strain 1 or 2 on an undisturbed bed and halves its pace
  (tolerating) at 3, or sooner where the area already disturbs it; Mendvetch (`[0,0,3,6]`) stays ideal up to 3 on an
  undisturbed bed; Dewmoss declares no disturbance range and is untouched.

**Changes.**
- **Mixed bed:** an optional `neighbours` field on the organism, wired through the parser's allow-list, the `Organism`
  record, the generator's `ORGANISMS` and `organism_json`, and `check_ecology` (`check_mod_data.py:14104`).
  `Sampler.Area` (`Sampler.java:36-39`) carries the kinds set it already builds (`:42-50`, at most 25 entries): no new
  reads. The bonus multiplies the speed at `OrganismCropBlock.java:165` and leaves the per-step cost (`:170`) alone, so it
  cannot mint nutrients. No Mendvetch pairing (already a legume) and no fixer as beneficiary. It is called "mixed bed":
  "companion" means Peepo here.
- **Strain:**
  - the bed notes `ripened` when growth reaches the last age (`OrganismCropBlock.java:173`); the next growth attempt
    below ripe counts as a harvest: strain + 1, the note cleared. Every harvest path (hand, Gleaner, Peepo, Crop
    Harvester, sickle, reap, spell, breaking and replanting) resets or removes the crop and regrows through `grow`
    *(survey: the garden report's harvest-path table)*, so no block hook is needed. A crop placed ripe by a command
    never noted `ripened`, so its first harvest strains nothing;
  - `Habitat` carries the strain beside the five factors, not as a sixth (the Gauge's modes, Jade's five-number array,
    `JugcraftJadeClient.java:374-378`, and the checker's factor order stay); `Verdict.of` judges disturbance as the worse
    of the area alone and the area plus strain;
  - eased by rest (one a bed random tick), by **explicit compost points only** (bone meal on the bed,
    `VerdantBedBlock.java:129`; bone meal or fertilizer on the crop, `OrganismCropBlock.java:271-276`; the Mulch Maw's
    mulch, `concordance/garden/MulchMawBlockEntity.java:95-96`) and by a Greenwarden's empty-hand tending
    (`VerdantBedBlock.java:140-154`, at most once a 600 ticks a bed, `Authority`-checked; as built, on the bed or, since
    the unit review, on its growing crop). Not in `give`: nitrogen fixing
    calls `give` through `Garden.nourish` on every Mendvetch step for its bed and four neighbours
    (`Garden.java:297-312, 340-352`; `OrganismCropBlock.java:175-177`), so easing there would keep those beds unstrained;
  - it never reads industrial pollution or main's process emissions.
- **The bed's update tag** (`VerdantBedBlockEntity.java:242-251`) gains the strain and the mixed-bed state, and its
  reasons new kinds: a network-contract change. `reasonText` (`JugcraftJadeClient.java:213-221`) treats field 1 as a
  factor id, so its decoder changes too.

**Tests (server):** `ConcordanceGardenGameTests`: `aMixedBedGrowsFasterOnlyForAnotherKind`;
`theBonusSpendsNoExtraNutrientAndMintsNone`; `everyHarvestPathStrainsTheBedOnce` (hand, Gleaner, Crop Harvester, sickle,
breaking and replanting); `fixingDoesNotEaseStrain`; `restCompostAndTendingEaseStrain`;
`strainNeverSuppliesDisturbance` (Gloamcap still stalls with no magic blocks); `strainNeverKillsAndNeverReadsPollution`;
`strainSurvivesASave`; `samplingStaysWithinTheBudget` (`:445`) extended: still 100 reads. Re-run, since they harvest and
regrow: `theGleanerHarvestsForVerdance` (`:364`), `ConcordanceSpireGameTests.aVerdantFieldHastensOnlyWhatItsBedsPay`
(`:406`), the Peepo regrowth case (`PeepoIntegrationClientTests.java:169-171`, which places its crop ripe) and
`ConcordanceJourneyGameTests`.

**Acceptance:** "one companion-planting rule … Bound neighborhood work and include recovery. Do not attach crop death to
industrial pollution" (09:128); D-005 (10:158).

**Save effects:** optional bed keys `strain` and `ripened`, absent in older saves and read as 0 and false; the stamp
stays 1 (`VerdantBedBlockEntity.java:227`). A persistence test reads an older bed.

**Review boundary:** ecology rules, the bed and the generator's data, the bed's update tag and Jade decoder, and tests.

**As built (unit 4: commit `d32ac3a61`).**
- **Built:**
  - pure ecology: `Neighbours` (`organisms`, `percent`; `NONE`, `MAX_PERCENT` 50, `any`, `line`), `Organism.neighbours`
    and the parser's `neighbours` (other organisms, never itself or twice, 1 to 50 percent, never on a fixer);
    `Sampler.Area.kinds` (the kinds it already read: still 100 reads) and `Sampler.habitat(..., strain)`;
    `Habitat.strain`, `MAX_STRAIN` 3 and `strained()`, the strain a sixth value of the record but not a sixth factor
    (the Gauge's modes, Jade's five-number habitat and the checker's factor order stay); `Verdict.strained`,
    `Verdict.of`'s worse of the area alone and the area with the strain, and the `ecology.strained.excess` and
    `.tolerable` reasons; `HabitatInspection`'s `disturbance_strained` row; `EcologyCatalog.mixed`;
  - the rules report a neighbour that is not another crop costing nutrients (`ConcordanceRules`), and such a name speeds
    nothing;
  - the bed: `strain`, `ripened`, `strainNow` (it only reads), `ripen`, `settle`, `rest`, `compost`, `tend`, `setStrain`
    (tests only), `mixed`, `mixedLine`, `strainLine` and `notes`; the keys `STRAIN` and `RIPENED`, saved when
    set and always sent, never with the keeper; its reports add the mixed-bed and strain lines;
  - growth: `OrganismCropBlock.grow` counts a harvest the bed saw before the dormant check and tells the bed when a step
    reaches ripe; `speed` holds the mixed bed's bonus and the cost is untouched; `useItemOn` makes fertilizer on a
    growing crop its own use, and bone meal on one feed the bed below by the bed's own rule (`RateGate`, then
    `Illumination.mayChange` on the bed, a refusal consuming the use) and compost it (defect 15); `performBonemeal`
    composts only when it feeds;
  - `Garden.poorest` and `TEND_TICKS` (600); the Mulch Maw composts the bed each nutrient goes to; `Garden.nourish` and
    `give` never compost;
  - the bed block: its random tick rests a strain (`rest`), bone meal on it composts, and a Greenwarden's empty hand
    tends (`tend`: an awake, strained bed, `Garden.knows`, `Authority.mayChange`; once every 600 ticks a bed, not
    saved);
  - Jade: the bed snapshot's `strain` (read, never counted); the client's `reasonText` shows a reason with no factor as
    sent, and a strain line on a bed and on its crop;
  - the Inspect page's mixed-bed line (the strain is in the disturbance row);
  - the generator: `MIXED_BED_PERCENT` 25, `MAX_MIXED_BED_PERCENT` 50, `MAX_STRAIN` 3, `TEND_TICKS` 600, `neighbours` on
    the Dewmoss and Gloamcap organisms, the codex's "Mixed Beds and Strain" page (organisms p6), and the messages
    `garden.tended` and `garden.tended_lately`;
  - audits in `check_ecology`: the generator's neighbours and their rules; the bonus in the speed only, and that `grow`
    uses `speed()`; only rest, compost and tending ease strain, compost only at its listed points; the tending gate; the
    crop's bone meal and fertilizer rule; the saved and sent keys; Jade's five-factor habitat; the read-only page and
    `strainNow`; that the garden never reads industrial pollution or process emissions (D-005); the encyclopedia's
    read-only list gains `.settle(`, `.compost(`, `.tend(`, `setStrain(` and `.ripen(`.
- **Departures from the ticket and the spec:**
  - a bed rests only while no crop grows on it (the spec's D-a; C-P60, refining C-P17's "one a bed random tick"), by
    arithmetic and not measured: see the ecology record's Balance;
  - bone meal or fertilizer used plainly on a growing crop is a compost point (the spec's R10; defect 15); from the
    critique (3), bone meal there asks the bed's rate gate and who may change the bed before it feeds, a refusal
    consuming the use, and fertilizer keeps its own area rule;
  - a harvest is counted whatever took the crop: a growth attempt, the bed's random tick, compost or tending counts one
    when the crop the bed saw ripen no longer stands ripe, so a Hay Golem, a crow raid and breaking a ripe crop to move
    it count too, and a crop placed or aged ripe without growth never does;
  - the Crop Harvester has its own test, and the parts' two older-bed persistence tests are one,
    `anOlderBedLoadsWithoutItsNewKeys` (R14); this tree's persistence class has 8 tests, not the spec's 10, as unit 3's
    two are not here;
  - from the critique: the nine garden tests pasted from the part designs and the critique (1), with the sickle used
    sneaking (a plain use is the hand's harvest) and the `strained` helper ending on a crop still growing, so no later
    path can take it; an audit that `grow` uses `speed()` (2, mutation 23);
  - the decisions are C-P58 to C-P61 (the spec's C-P84 to C-P87).
- **Tests (none run):** 9 new garden tests (24 then): `aMixedBedGrowsFasterOnlyForAnotherKind`,
  `theBonusSpendsNoExtraNutrientAndMintsNone`, `everyHarvestPathStrainsTheBedOnce` (a hand, a Gleaner, a sneaking
  sickle, a reap, breaking and planting again), `theCropHarvesterStrainsTheBedOnce`, `fixingDoesNotEaseStrain`,
  `restCompostAndTendingEaseStrain`, `strainNeverSuppliesDisturbance`, `strainNeverKillsAndNeverReadsPollution` and
  `strainSurvivesASave`; `samplingStaysWithinTheBudget` extended (still 100 reads, and its 25 kinds);
  `ConcordancePersistenceGameTests.anOlderBedLoadsWithoutItsNewKeys`; `gardenInspection`'s strain step (the client is
  told strain 2; Jade and the page show it).
- **Commands** for commit `d32ac3a61`:
  - run and passed: the generator twice (the second run changing nothing), `check_mod_data`, `check_repository`,
    `owner_art --check`, `concordance_journey --check`, `check_icon_maps`, the World Designer's node tests,
    `scripts/tests` and `git diff --check`;
  - the spec's audit mutations with the critique's, each applied in place and restored: 28 of 28 reported (23 rows, four
    of them in variants), at the commit and again on the final tree for these records;
  - the pure ecology classes compiled with `javac` (JDK 21) against stubs, and a scratch harness outside Git passed 41
    checks of the mixed bed, the parser's refusals and strain; for these records it was run again on the final tree, its
    catalogue built through C-09's three-argument constructor;
  - not run: the build, the game tests and CI.

### C-09. One stable graft trait (unit 4)

**What the player sees.** A gardener with Verdant Husbandry understood uses a Dewmoss, not sneaking, on a Sunpetal crop.
The Dewmoss is consumed and that bed's Sunpetal becomes **Dewlit** (C-P18): it tolerates shade (light `[9,11,15,15]`
for `[11,13,15,15]`) but needs wetter soil (moisture `[4,5,7,7]` for `[2,4,7,7]`). The graft stays on the bed through
every harvest path, replanting, saves and restarts; another organism growing there clears it. Produce, cost and alchemy
are unchanged. Inspection and Jade name it.

**Changes.**
- **The gesture reaches the crop:** an `OrganismCropBlock.useItemOn` override that, for a donor this host accepts,
  grafts and consumes the action before the empty-hand fallback, and otherwise returns as today so a ripe crop still
  harvests. A sneaking use with an item in hand never reaches the block (mc263src `MultiPlayerGameMode.java:335-337`);
  without the override a plain use falls through to `useWithoutItem` (`:343-348`), which harvests a ripe crop
  (`OrganismCropBlock.java:204-228`). Gated by research, `Authority` (as the harvest, `:213`) and `RateGate`.
- One trait in `concordance/graft/*.json` (host, donor, niche overrides), generated; `check_ecology` holds the pilot's
  cap of one (03:81) and that it changes no produce or cost.
- **Bed-bound, not an item component** (C-P18, a deviation from 03:76 and 03:81): Peepo plants with `setBlock`
  (`local/peepo/CompanionGarden.java:612`), so no item component reaches the crop; `OrganismCropBlock` has no
  `setPlacedBy`; loot-table harvests would drop unstamped produce *(survey)*.
  Cuttings are deferred.
- **Every niche reader** asks one helper for the bed's niche: growth (`OrganismCropBlock.java:160`), the empty-hand
  report (`:247`, `:254`), the bed's note that Jade shows (`VerdantBedBlockEntity.java:171`) and the Habitat Gauge
  (`concordance/garden/HabitatGaugeBlockEntity.java:110`). The Verdant Heart reads its own niche
  (`VerdantHeartBlockEntity.java:104`) and is untouched.
- The bed's update tag gains the graft (the network-contract change of C-08).

**Tests (server):** `ConcordanceGardenGameTests`: `aGraftTakesOnceAndConsumesItsDonor`;
`aSneakingUseNeitherGraftsNorPlaces`; `aDonorOnARipeCropGraftsAndAnyOtherItemStillHarvests`;
`anUnresearchedGardenerCannotGraft`; `aGraftIsRefusedWhereTheGardenerMayNotBuild`;
`aGraftSurvivesEveryHarvestPathAndASave`; `everyNicheReaderSeesTheGraft` (growth, the report, the note and the Gauge
agree); `aGraftChangesOnlyTheNiche`; `anotherPlantClearsTheGraft`.

**Acceptance:** "one stable graft trait" (09:128); "Breeding cannot duplicate rare unique outputs or become a Prima
Materia loop" (03:81): the produce is unchanged and uncatalogued.

**Save effects:** an optional bed key `graft`. **Review boundary:** the graft data, the bed field, one gesture, the
niche helper and tests.

**As built (unit 4: commit `d6edee4f4`).**
- **Built:**
  - pure ecology: `Graft` (`id`, `host`, `donor`, `overrides`; `apply`, `lines`), `EcologyParser.graft` (`schema`,
    `host`, `donor` and `niche` and nothing else; a niche that changes nothing is refused) and `EcologyCatalog.grafts`,
    `graft` (by id, and by host and item), `niche(organism, graft)` and `graftLines`;
  - the rules read `concordance/graft` and refuse a graft that does not join two different loaded crops, puts its host's
    nutrient floor below its cost, or repeats a host and donor (`ConcordanceRules`); `ComposeText` names a `graft`;
  - data `concordance/graft/dewlit.json`, from `GRAFT_CAP` (1) and `GRAFTS` through `graft_json` in
    `tools/concordance_ecology.py`; the codex's "Grafting" page (organisms p7, once Verdant Husbandry is understood);
  - the gesture: `OrganismCropBlock.useItemOn`, for a Greenwarden with the Concordance on, finds the graft for the crop
    and the held item and calls `graftBed`: `RateGate`, a bed below, `Authority.mayChange` at the crop and at the bed (a
    refusal shows `missing_permission`), and no graft the rules know already on the bed; it sets the graft and spends
    one donor, and every outcome consumes the use;
  - the bed: `graft`, `graftFor` (a client's), `setGraft`, `clearGraftUnlessFor` (growth's, before the dormant check)
    and `niche`; the keys `GRAFT` (saved when set) and `GRAFT_HOST` (in the update tag only, while grafted);
  - every niche reader asks `VerdantBedBlockEntity.niche`: `OrganismCropBlock.grow` and `report`,
    `VerdantBedBlockEntity.note`, `HabitatGaugeBlockEntity.read` and `InspectPage.garden`; the Verdant Heart reads its
    own;
  - shown: the reports (a bed's, and its host crop's), the Inspect page (`grafted`: on a bed's page, and on a crop's
    when the crop is the graft's host) and Jade (the bed snapshot's `graft` when the rules know it; the client's line on
    the bed, and on a crop when the update tag's host is that crop);
  - lang: `graft.jugcraft.dewlit`, `compose.jugcraft.ecology.graft` and `.graft.range`,
    `message.jugcraft.concordance.garden.grafted` and `.graft_held`, `tooltip.jugcraft.concordance.jade.graft`;
  - audits: `check_graft` (one graft; one range better and one worse; nothing but a host, a donor and ranges; the data
    the generator's; the loader; the one niche helper and no other reader; the gesture's research, rate limit, Authority
    at crop and bed and consumed refusals; only the gesture grafts and only growth clears; the saved and sent keys;
    Jade; the Inspect page; the lang), `AUTHORITY_ROUTES` for `OrganismCropBlock` (crop and bed) and the encyclopedia's
    read-only list (`setGraft(`, `clearGraftUnlessFor(`).
- **Departures from the ticket and the spec:**
  - from the critique: a graft the rules no longer know no longer blocks its bed: the next gesture replaces it (its
    correction 6; C-P64); the Inspect page names a graft on a crop's page only when the crop is its host, as Jade does
    (7; C-P65); the codex says "on its host's crop, ripe or growing" (8);
  - the codex page is appended as organisms p7 (the spec's R11), so no page key is renumbered;
  - the decisions are C-P62 to C-P65 (the spec's C-P88 to C-P91).
- **Tests (none run):** 9 new garden tests (33 in all): `aGraftTakesOnceAndConsumesItsDonor`,
  `aSneakingUseNeitherGraftsNorPlaces`, `aDonorOnARipeCropGraftsAndAnyOtherItemStillHarvests`,
  `anUnresearchedGardenerCannotGraft`, `aGraftIsRefusedWhereTheGardenerMayNotBuild`,
  `aGraftSurvivesEveryHarvestPathAndASave`, `everyNicheReaderSeesTheGraft`, `aGraftChangesOnlyTheNiche` and
  `anotherPlantClearsTheGraft`; `EncyclopediaGameTests.theInspectPageNamesAGraft` (23);
  `anOlderBedLoadsWithoutItsNewKeys` extended (a bed from before grafts reads ungrafted and saves none);
  `gardenInspection`'s graft step (the client is told the graft and its host; Jade and the page name it).
- **Commands** for commit `d6edee4f4`:
  - run and passed: the generator twice (the second run changing nothing), `check_mod_data`, `check_repository`,
    `owner_art --check`, `concordance_journey --check`, `check_icon_maps`, the World Designer's node tests,
    `scripts/tests` and `git diff --check`;
  - the spec's audit mutations with the critique's, each applied in place and restored: 32 of 32 reported (30 rows, two
    of them in two variants), at the commit and again for these records, through a scratch harness of five audits
    (`check_ecology`, `check_graft`, `check_encyclopedia`, `check_authority`, `check_diagnostics`) with the generated
    files written by hand, not through the generator or the whole `check_mod_data`. Mutation 5 (Dewlit's nutrients
    `[0, 0, 32, 32]`) in fact crashed the generator and `check_mod_data` (the codex's `graft_text` joined a full range's
    `None`) until the unit review fixed `graft_text`;
  - a scratch harness outside Git passed 28 checks of the graft (the parser and its refusals, the catalogue's niche and
    lines, a graft whose crops are not loaded) on the compiled pure classes, at the commit and again for these records;
  - for these records, `python3 tools/select_client_tests.py --base 23f28b90e --head d6edee4f4` chose 12 of 171 client
    classes: `ConcordanceEmberGearClientGameTests`, `ConcordancePresentationClientGameTests`,
    `EncyclopediaClientGameTests`, `FluoriteClientGameTests`, `PeepoActionClientTests`, `PeepoCompanionClientTests`,
    `PeepoGardenExpansionClientTests`, `PeepoHarvestClientTests`, `PeepoIntegrationClientTests`,
    `PeepoSuppliesClientTests`, `SpodumeneClientGameTests` and `WorldDesignerClientGameTests`, noting that no client
    test shows the Jade client, the JEI plugin, `ComposeText`, `EcologyParser`, `Neighbours` or `recipe_view.json`;
  - not run: the build, the game tests and CI.

**Fixes from the unit review (unit 4, a fifth commit).** The review confirmed ten findings and rejected sixteen; all ten
are fixed here. None of the changed Java has been compiled against Minecraft or run in a game.
- **A graft with a full range crashed the generator** (C-09). `graft_text` joined `_range`'s `None` for a range such as
  `[0, 0, 32, 32]`, so mutation 5 crashed `tools/generate_material_data.py` and `check_mod_data` (the codex is built
  before `check_graft`). It now writes "nutrients any"; Dewlit's page is unchanged. The mutation record above says how
  the mutations were really run.
- **Jade could show another plant's verdict** (C-07, C-P55). A crop newly set on a bed (`OrganismCropBlock.onPlace`,
  server only, not for a step or a harvest of the same crop) and a graft (`graftBed`) now forget the bed's last
  verdict at once; before, a Dewmoss planted where a Sunpetal stalled showed the Sunpetal's reasons until its own first
  growth attempt. `aVerdictClearsWhenThePlantGoes` and `aGraftTakesOnceAndConsumesItsDonor` assert it, and
  `check_ecology` audits both calls.
- **Tending could not be reached on a planted bed** (C-08, C-P61). A crop's shape covers its bed from above, so in a
  field only edge beds could be tended. A Greenwarden's empty hand on a growing crop now tends the bed beneath through
  the bed's own rule (`VerdantBedBlock.tendBy`, which asks `Authority` at the bed), after the crop's rate gate, then
  reports. `restCompostAndTendingEaseStrain` tends through a crop and is refused through one on a claimed bed;
  `check_ecology` holds the callers of `tendBy` to the bed and the crop. Not done (optional): a crop read again during
  the cooldown still says "tended lately".
- **A rare flake** (about 0.1% of runs): `restCompostAndTendingEaseStrain` now resets the compost bed's nutrients, which
  a random step before tick 46 could lower, before its exact asserts.
- **Records.** Bees reach these crops (confirmed by reading, under Risks; a gap since step 14, recorded in the ecology
  record). The Jade staleness risk is "until the crop's next growth attempt". The ecology record's test count (step 14
  added ten; "the ten garden tests"). The persistence record names the bed's new keys. The merge note (Where to resume)
  names the conflicting files from a trial merge. `setStrain` is "tests only" (no game code calls it; also `setGraft`'s
  Javadoc, which said "operators"). The departure note names the two garden tests that prefetch.
- **Commands** for the fixes:
  - run and passed: `python3 -B tools/generate_material_data.py` (changing nothing: Dewlit's page reads as before),
    `python3 -B tools/check_mod_data.py`, `python3 scripts/check_repository.py`, `python3 tools/owner_art.py --check`,
    `python3 tools/concordance_journey.py --check`, `python3 tools/check_icon_maps.py`,
    `node --test tools/world-designer/model.test.cjs`, `python3 -m unittest discover -s scripts/tests` and
    `git diff --check`;
  - mutation 5 again, in place and restored, through the generator and the whole `check_mod_data`: the generator ran
    (rewriting `dewlit.json` and the lang) and `check_mod_data` reported "below its host's cost" and "one range for the
    better and one for the worse"; the old `graft_text` raises `TypeError` on the same data;
  - the fixes' audit mutations, each applied in place and restored, through the five-audit harness: 11 of 11 reported
    (`onPlace` forgetting nothing, forgetting on a step of the same crop, or missing; the graft not forgetting; the
    crop's tending removed, with an item in hand, at the crop's own position or without the crop's rate gate; a third
    caller of `tendBy`; `tendBy` without `Authority`; the crop calling the bed entity's `tend` itself);
  - `python3 tools/select_client_tests.py` on the fixes as staged: against `549a49d7e` it chose
    `EncyclopediaClientGameTests`, `PeepoActionClientTests`, `PeepoGardenExpansionClientTests`,
    `PeepoHarvestClientTests` and `PeepoIntegrationClientTests` (noting that no client test shows
    `VerdantBedBlockEntity`); against `23f28b90e` the same 12 classes as for the records above;
  - not run: the build, the game tests and CI.

### C-10. The measured workshop, the journey, the evidence and the checkpoint (unit 5)

- **The load card** (06:182; 10:79-85; `docs/TESTING.md:41`):
  - **workload:** a stand and crucible replaying the Stillroot Poultice, a pump, one circle running Root and Rivet, two
    workers (a Peepo and a porter), nine beds with a Maw and a Gleaner;
  - **recorded:** hardware (the runner's CPU from its log), Java options, baseline world, warm-up, measured duration
    (at least 6,000 ticks), player count, loaded chunks, machine, worker and plant counts, active recipes;
  - **measured:** median and p95 server tick time over 100-tick windows (`MinecraftServer.getTickTimesNanos()`, a
    100-entry ring, mc263src `MinecraftServer.java:286, 1696-1697`), with and without the workshop in the same world and
    build; the memory trend; **inventory correctness** (every item in, out, carried and stored accounted for, the ledger's
    `problems()` empty); **network traffic** at the existing send points (block-entity updates, Jade, encyclopedia and
    sign payloads), counted if a cheap counter proves feasible, otherwise stated as not measured;
  - **client frame time:** CI's client shards render in software (`.github/workflows/build.yml:109`), so frame times
    there describe that renderer only; a player's hardware stays not run unless someone runs it;
  - **runner** (C-P20): a server game test of its own, opted in so ordinary runs are not lengthened (about 12,000 ticks
    with warm-up is ten minutes or more); if a client class is used instead, it gets a `WEIGHTS` entry
    (`tools/select_client_tests.py:53`) to stay under the 60-minute job limit (`build.yml:102`);
  - **budget** (C-P19), agreed before merge.
- **The journey:** `tools/concordance_journey.py` and `ConcordanceJourneyGameTests` gain the workshop route from a manual
  start: crafted Mendvetch, a campfire crucible, lantern-filled pylons. Every new item passes `check_acquisition`.
- **Client evidence** (08:169-181): the crucible's, a bed's and the anchor's Inspect pages; the workshop at the ordinary
  scale; one high GUI scale shot; Jade and GuiLib absent where relevant. Each new client class is registered in
  `tools/select_client_tests.py` and the gametest `fabric.mod.json`.
- **Records:** each unit's feature record (tier, input producer, output consumer, costs, unlocks, failure behaviour and
  test evidence, CLAUDE.md), `docs/WHAT_EXISTS.md`, `docs/INTEGRATION_STATUS.md` and `docs/ARCANE_CONCORDANCE.md`
  (MP:71), and an "Unmerged: Chunk C" section in `CHANGELOG.md`.
- **The checkpoint** (09:144; 10:87-121): one complete input-to-output line, the garden and recipe costs; at most three
  decisions (09:18; 10:113-116). 09:144 names three (the balance of hands-on and automated work, the next product
  family, whether the visual workshop fits the desired style). The review route (above) goes with it as a question, and
  the load budget is put at merge review (`docs/TESTING.md:41`), not as a fourth decision.
- **The handoff** (10:133-146) on this page.

## Interfaces expected to change

- **Saved data:** crucible: optional `formula_owner` and `request` (C-06); an unreadable program kept as text under
  `program` (C-01). Bed: optional `strain`, `ripened` and `graft`. A new stand block entity (energy, manual setting).
  No change to the formula item or the crucible's update tag. Stamps stay at 1 for the crucible and the bed.
- **Registries (stable once released):** `crucible_stand` (block, item, block entity); `rooted_thallite_foundation`;
  the ritual `root_and_rivet`; the product ids `stillroot_poultice` and `kiln_draught` (and `winter_brine`, C-P2); the
  graft trait `dewlit`; the organism's `neighbours` field and a property's `fouling` field; lang keys, codex entries
  (`alembic/stillroot_poultice`, `alembic/kiln_draught`, a Root and Rivet entry, a stand entry).
- **Shared code:** the pure `concordance/diagnostic` package; `Outcome` (fouling) and `Property`; `Formula.replay`
  (the refused step's index); `CrucibleBlockEntity` (`automate`, `use` and `useEmpty`'s authority, `formula`,
  `heatBelow`, the shortage, filing, `preRemoveSideEffects`); `CrucibleCompanion` (status, needed); `Alchemy.brew`
  (naming); `BrewItem.applySalve` and `Encounter` (the mending credit); `LairDossiers.prepare` (the Cinder Kiln);
  `ritual/Readiness`; `CircleAnchorBlockEntity` (the shared selection, signs on refusal and status, the combined line);
  `InspectPage` (crucible, bed and crop branches); the Jade provider and client (crucible, anchor, stand, bed and crop
  lines); `ConcordSpire.nearestPost` as a shared helper; `Sampler.Area` (kinds), `Habitat` (strain), `Verdict`
  (disturbance), `OrganismCropBlock` (`useItemOn`, strain, the niche helper), `VerdantBedBlockEntity` (strain, graft, the
  update tag), the canteen.
- **Tools:** `concordance_alchemy.py` (fouling, products, the stand's targets), `concordance_ecology.py` (neighbours,
  strain, graft, the compost page), `concordance_rituals.py`, `concordance_economy.py`, `concordance_progression.py`,
  `acquisition.py`, `concordance_journey.py`, `select_client_tests.py`, an `OWNER_FILES` table if the stand's art is
  imported; `check_mod_data.py`: `check_workshop_products`, the fouling mirror, the Ley-capacity rule, the graft,
  mixed-bed and strain rules, `diagnostic` in the pure list, the makeup kept out of Jade's crucible snapshot,
  `Signs.show` in the encyclopedia's forbidden list, and a rule that no client class reads `ConcordanceData.rules()` or
  `Alchemy.catalog()` (none does today; the rules are server-only statics, `ConcordanceData.java:28`, and the client
  tests' dedicated server may share the client's process, which would hide such a read).
- **Network:** no new payload. Jade snapshots carry more values; the bed's update tag grows (C-08, C-09), a contract
  change between server and client of the same build.

**As built in unit 1:**
- **Diagnostics:** `Diagnostic` (`of`, the crucible's factories, `text`, `encode`, `decode`) and `Code` (with
  `Code.Kind`).
- **The crucible:** `CrucibleBlockEntity.automate` (now public, returning its reason), `waiting`, `incompatibility`,
  `unreadableProgram`, `follow`, `statusLine`, `tankParts` and `below`.
- **Pure alchemy:**
  - `Formula.incompatibility` and `incompatible`; `Mixture.Refused` gains `step`; `Operation.describe` and
    `Operation.ingredient`;
  - `Assay.risks`, `CrucibleInspection` and `Heat.Below`;
  - `Property.fouled` (and the `fouling` field), `Product`, and `AlchemyCatalog.product`.
- **Shared Concordance code:** `Alchemy.glassLevel`, `product` and `STILLROOT_POULTICE`; `ComposeText` words a line
  nested in a diagnostic; `BrewItem.Observer` and `observe`.
- **Lairs:** `Encounter.mended`, `regenerationHeals`, `POULTICE_STATUS`, `REGENERATION_HEAL` and the private
  `creditHealing`; `LairDossiers.brews`, `poultices` and `FIRE_RESISTANCE`.
- **Encyclopedia and Jade:** `InspectPage.CRUCIBLE_REACH`; the Jade crucible snapshot's `tank` and `reason`.
- **Tools:** `tools/concordance_diagnostics.py`; `tools/concordance_alchemy.py` (`fouled`, `PRODUCTS`,
  `PRODUCT_REWARD_ROUTES`, `REGENERATION_INTERVAL`, `realised`); `check_diagnostics`, `check_workshop_products`,
  `check_poultice_credit`.
- **No new saved key:** an unreadable program reuses `program`. The crucible's update tag still has its four keys.

**As built in unit 2:**
- **Pure ritual code:** `ritual/Choice` (`of`, `NONE`, `rite`) and `ritual/Readiness` (`before`, `running`, `stages`,
  `shapeFaults`, `holding`, `ready`, `spentText`, `UNKNOWN`; records `Channel`, `Line`, `Reader`, `Spent`, `Held`,
  `Ending`).
- **The anchor:** `CircleAnchorBlockEntity.choose`, `readiness`, `spent`, `ending`, `endingLine` (an instance method and
  a static one) and `MARK_TICKS`; `participantsNeeded` made public; `faultText` gives both positions; the private
  `markReadiness`, `markFaults` and `part`.
- **Jade:** `ConcordanceDataProvider.SHOWN_OFFERINGS`; the anchor snapshot's `needed`, `offering<i>`, `offerings`,
  `spent_focus`, `spent_ley` and `reserved`.
- **Diagnostics:** `Code` gains `NOT_LENT`, `NEEDS_FOCUS`, `TOO_BRIGHT`, `TOO_FAR` and `PARTICIPANTS`, after unit 1's
  codes; `MISSING_INGREDIENT`, `UNAVAILABLE_POWER` and `INCOMPLETE_PATTERN` take their parameters. `ComposeText` names a
  `structure`.
- **Registries:** `JugcraftConcordance.ROOTED_THALLITE_FOUNDATION`; the ritual `jugcraft:root_and_rivet`.
- **Tools:** `RITUAL_SINKS` (with `STEP_TICKS` and `FOCUS_REGEN_TICKS`) in `tools/concordance_economy.py`;
  `PRODUCERS` and `VANILLA_TAGS` in `tools/concordance_progression.py`; `RITUALS` in `tools/acquisition.py`; the
  `"switches"` key, `_payment_text` and `_switches_text` in `tools/concordance_rituals.py`; `ROOT_AND_RIVET` in
  `tools/concordance_strata.py`; the `thallite` material in `tools/icon_materials.py`.
- **No new saved key or payload:** the ending lives in memory; Jade's anchor snapshot carries more values.

**As built in unit 4:**
- **Pure ecology:** `HabitatInspection` (`State`, `unjudged`, `judged`, `verdict`, `pace`, `unplanted`), `Neighbours`
  and `Graft`; `Organism.neighbours`; `EcologyParser.graft`; `EcologyCatalog.mixed`, `grafts`, `graft` (by id; by host
  and item), `niche`, `graftLines` and the grafts as its constructor's third argument; `Sampler.Area.kinds` and
  `Sampler.habitat(..., strain)`; `Habitat.strain`, `MAX_STRAIN` and `strained`; `Verdict.strained`.
- **The garden:** `Garden.poorest` and `TEND_TICKS`; `VerdantBedBlockEntity` `inspect`, `forgetVerdict`, `strain`,
  `ripened`, `strainNow`, `settle`, `rest`, `compost`, `tend`, `setStrain`, `mixed`, `mixedLine`, `strainLine`, `notes`,
  `graft`, `graftFor`, `setGraft`, `clearGraftUnlessFor`, `niche` and the keys `STRAIN`, `RIPENED`, `GRAFT` and
  `GRAFT_HOST`; `VerdantBedBlock.pour` and its empty-hand rule; `OrganismCropBlock.useItemOn`, `speed` and the private
  `graftBed`; the Gourd Canteen's `BED` pour.
- **Encyclopedia, Jade and JEI:** `InspectPage.GARDEN_REACH` and its garden branch (`garden`, `grafted`); the Jade bed
  snapshot's `strain` and `graft`; the client's `reasonText` (a reason with no factor), `strain` and the crop's "As it
  grew" and graft lines; `ComposeText` names a `graft`; `JugcraftJeiPlugin.addCompostInfo` and `recipe_view.json`'s
  `garden_compost`.
- **Tools:** in `tools/concordance_ecology.py`, `MIXED_BED_PERCENT`, `MAX_MIXED_BED_PERCENT`, `MAX_STRAIN`,
  `TEND_TICKS`, `GRAFT_CAP`, `GRAFTS`, `graft_json`, `graft_text`, `_mixed_and_strain_text` and the organisms'
  `neighbours`; `garden_compost` in `tools/recipe_view.py`; `INSPECT`'s `garden.*` in `tools/encyclopedia.py`;
  `check_graft`, and the rules above in `check_ecology`, `check_encyclopedia`, `check_diagnostics` and
  `AUTHORITY_ROUTES`.
- **Saved and sent:** bed keys `strain`, `ripened` and `graft`, saved only when set, the stamp staying 1; the update tag
  always adds `strain` and `ripened`, and `graft` and `graft_host` while grafted. No new block, item or payload.

## Dependencies and order

1. C-00: done (the merge, its CI, this page).
2. Unit 1: C-01, then C-02 (its preview, risk line and reasons use C-01's).
3. Unit 2: C-03, then C-04 (its table is shown by C-03's readiness).
4. Unit 3: C-05 after C-04 (its recipe consumes the foundation) and C-01 (the reasons that make it idle); C-06 after
   C-01 (the shortage and incompatibility).
5. Unit 4: C-07, then C-08 and C-09 (their lines sit on C-07's Inspect branch).
6. Unit 5 after units 1 to 4.

The smallest first unit is C-01: no new id, no owner input, the first seven defects above fixed and the reload,
blockage and restart evidence. C-07 to C-09 need no owner input and could start while other units wait, but they land
after the first path (09:16).

## Risks

- **Base and review:** Chunk C stacks on Chunk B, which has no pull request, plus a main merge; how it is reviewed is
  open (above). A change to Chunk B in review, or a new main, means another merge.
- **Shared hot files** with open pull requests: lang, the gametest `fabric.mod.json`, `check_mod_data.py`,
  `CHANGELOG.md`, `recipe_view.json`, `tools/owner_art.py` (#351, #354), `docs/WHAT_EXISTS.md` (#366),
  `docs/INTEGRATION_STATUS.md` (#361 to #365).
- **Plan wording, deviations to show at the checkpoint:** "Reuse FluidRecipe … MachineCompanionPort" (09:136): the
  crucible path reuses the `Port` interface and the shared fluid storage instead; the graft is bed-bound, not an item
  component (03:76, 03:81); the Inspect page stands in for the notebook screen (08:55-59); the 9×9 ceiling is unused
  (03:91); the formula's revision is process-local, not a stable version (03:46); no preservation wash this chunk
  (09:124's "such as"): the owner chose the Kiln Draught.
- **The fouling rule changes existing brews:** any Verdance brew carrying contaminant (from bone meal, gloamcap, spider
  eye, magma cream or a searing stir: `concordance/ingredient/*.json`) heals less than before. Bottles already made
  keep what they carry; written formulas replay to the new outcome.
- **Named stacks** do not stack with older unnamed brews of the same outcome, and an automated crucible pauses on one in
  its output (tested, with a reason).
- **The Kiln Draught also turns the Cinder Tyrant's fire:** his flame is `jugcraft:flame`
  (`lair/tyrant/CinderTyrantEntity.java:475-477`), listed in `#minecraft:is_fire`
  (`src/main/resources/data/minecraft/tags/damage_type/is_fire.json`), which vanilla does not apply under Fire Resistance
  (mc263src `LivingEntity.java:1155`). Vanilla's Fire Resistance potion and the crucible's Ember brews already
  do this; the dossier fact will advertise it. The Salamander Charm stops only hot ground
  (`lair/tyrant/SalamanderCharm.java:7-12`). A balance question for the checkpoint.
- **The poultice's credit** (as built, C-P30): food, a vanilla potion or a beacon healing in the same tick as one of
  the poultice's heals can be credited for that heal, at most one health and never past the fight's hurt; heals from
  Concordance spells and blows are read out exactly. A poultice replaced by a stronger or longer Regeneration (a potion,
  a beacon, milk) ends its note, so heals vanilla's hidden instance gives after are not credited (under-credit, which is
  safe). A real client's patient (`doTick`, with `FoodData.tick`) is not run; the tests run the patient's tick by hand.
- **The crucible's reason** comes from its last pulse, so it can be up to 20 ticks old in Jade, the status line and
  Peepo (for example FULL just after the output is emptied). The incompatibility is rechecked lazily and is never stale.
- **Authority is a behaviour change** (C-P5): adventure mode, spawn protection on a dedicated server with operators,
  claims and protected towns now refuse crucible work. Recording stays open (C-P24).
- **An incompatible formula** stops even a finished batch from bottling automatically (C-P23).
- **Naming by effect** makes any Regeneration salve carrying nothing harmful a Stillroot Poultice, which earns credit
  (C-P29).
- **Unit 1, not yet seen in a game:** the Expeditions page with the poultice's fact for all four keepers stays within
  the 96 rows a page holds (about 77 by the spec review's count; `theExpeditionsPageHoldsEveryKeeper` checks it, not
  run), and whether the client test's Jade shot shows Jade's overlay (`takeScreenshot` with the HUD).
- **Feature switches:** the foundation needs `machines` and `thallite` (C-04); with either off the powered route is out
  of reach and the campfire route stays. Whether Fabric applies load conditions to Concordance data is unverified.
- **Strain** is a new soft cost on every harvest path, Peepo's and the Crop Harvester's included: visible, capped and
  rejectable at the checkpoint. A crop placed ripe by a command never strains its bed.
- **Graft:** breaking a crop and replanting the same organism keeps the bed's graft. Proposed as acceptable (C-P18), for
  the owner to confirm: no item is duplicated.
- **Over-supply** between a Peepo and a courier, if the subtraction is wrong: tested. **Requester online:** a courier
  serves a crucible only while its formula's setter answers.
- **Holds versus the stand:** a Preserve hold freezes the band, so a stand-driven formula waits; by design and documented.
- **Test isolation:** `ConcordanceData.replace` is global; a reload test that forgets to restore the rules would fail
  other Concordance tests. Unit 1's tests swap the rules only through `under()`, which `check_diagnostics` enforces
  (C-P28).
- **Load measurement** on CI is noisy and hardware-specific: numbers are recorded, and only an agreed, generous limit is
  asserted.
- **Unit 2, not yet seen in a game:**
  - the cached shape report can hold a refused start's charge and ownership faults for up to 100 ticks: the status lines
    and Jade show them while readiness reads the channels itself, so the two can disagree for that window;
  - `report()` may send a block update when the linked channels change under the Inspect page or Jade, as it already
    did: a cache refresh, not a sign or a saved change;
  - the one interruption line is long and may be clipped on a narrow action bar; no test reads the action bar;
  - the ending is lost on a reload (C-P37), and `TAMPERED`'s line under-counts its last draw;
  - an operator's command reads any loaded circle, so it can mark one from afar; the marks are signs only;
  - the foundation has no consumer until unit 3 (against 09:124's "no decorative recipes"), and its icon is a draft
    whose roots may read as legs;
  - the codex's first `entry://` link was checked against Modonomicon's source by the design, never clicked; the audit
    stops a link to a missing entry, page id or page number, not a wrong link format;
  - widening `VANILLA_TAGS` with `c:ingots/thallite` also lets the "Overworld things" recipe rules accept thallite
    ingots; the switch rule looks one step down only, so an ore-only or loot-only route is not seen;
  - combined offerings: the Kindling's 11 items beat Root and Rivet's 9 (the existing rule, now pinned by a test);
  - the economy model is conservative: it states 900 Focus a day where one player regains 600;
  - the client shots depend on Jade drawing into `takeScreenshot` and on the client test's server sharing its process;
  - while a rite runs, a part other than a channel's pylon broken since the last step (a Warding Stone, the clearance)
    shows only at the next step, within 40 ticks: until then the status lines and Jade show that step's report. A pylon
    gone or unloaded is a readiness line at once (the unit review);
  - the codex pages' length: `circles.root_and_rivet.p1` is about 926 characters and the Lesser Circle's "Reading It"
    (`circles.lesser_circle.p2`) about 788; neither has been seen in a client, so either may overflow its page;
  - with the `machines` or `thallite` switch off the plate's recipe is gone, and Root and Rivet's codex entry still has
    its recipe page. In Modonomicon 2.16.0's source `BookRecipePage` then logs "Recipe … not found" and shows a barrier
    in the recipe's place without blocking the book; not seen in a client.
- **Unit 4, not yet seen in a game:**
  - behaviour changes, all Proposed and visible: an item held in the main hand never reports, wakes or tends a bed
    (C-P56); a Greenwarden holding a Dewmoss cannot harvest a ripe Sunpetal by a plain use, which grafts or is refused
    (C-P62); bone meal on a growing crop feeds its bed rather than reporting (C-P61); a Greenwarden's empty hand on a
    growing crop tends its bed before reporting (C-P61, the unit review);
  - resting only while no crop grows (C-P60): a Gleaner-run Sunpetal bed with no compost settles at strain 3 (half
    pace), by the model's arithmetic, not measured; a crop stalled by strain lets its bed rest, so strain never stops a
    plant for good;
  - counting whatever took the crop also counts a crow raid, a Hay Golem and breaking a ripe crop to move it; a crop
    placed ripe, or aged by bees, never strains (an under-count, which is safe);
  - bees do reach these crops in 26.3 (confirmed by reading, the unit review): the mirror's
    `data/minecraft/tags/block/bee_growables.json` includes `#minecraft:crops`, its `Bee.java` steps any `CropBlock` by
    `getStateForAge(age + 1)` and `setBlockAndUpdate`, and the generator puts the four crops in `#minecraft:crops`
    (`crops.json`, from `tools/concordance_ecology.py`'s `tags()`). A gap since roadmap step 14: a bee's step skips `grow`,
    so a stalled crop advances with no niche check, no nutrient cost and no drying; it never calls `ripen()`, so that
    harvest never strains; and it never clears a graft. The owner may choose to move the crops to a
    `jugcraft:organism_crops` tag that fertilizer and garden maintenance also accept;
  - bone meal used sneaking or from a dispenser, and fertilizer's area, still reach `performBonemeal` without asking who
    may change the bed, as on any vanilla crop, and each such dose that feeds a bed eases a strain;
  - when a bed was last tended is not saved, so a restart allows one early tending;
  - the Inspect page and Jade can differ: the page takes a fresh reading, while Jade shows growth's last note, stale
    until the crop's next growth attempt (about 68 seconds on average); since the unit review a crop newly set on the
    bed, or a graft, forgets the last plant's note at once, so Jade never shows one plant's reasons on another. A ripe
    crop makes no growth attempt, so one grafted, or set there ripe, shows no "As it grew" until it is harvested and
    grows again;
  - one `RateGate` ("garden", 4 ticks) serves the canteen, bone meal, reports, tending and grafting;
  - the Habitat Gauge's disturbance mode shows the area only; its suitability mode includes the strain and the graft;
  - a Verdant Heart set on a recently harvested bed sees one strain in its verdict until the bed rests;
  - there is no explicit ungraft: a graft stays on an empty bed or under a device, and one a data pack removes stays
    saved, read by nothing, until the next graft replaces it or the data returns;
  - a refused graft consumes the use, and the client may swing as it predicts: cosmetic;
  - the tests share the level's 16 samples a tick: they prefetch at ticks 10, 20 and 30 and read at 41 to 55, so a
    collision makes a reading wait and a test fail, a risk existing tests already carry;
  - the JEI compost pages are not seen at runtime, add pages to many vanilla items (leaves, saplings, flowers) and
    depend on JEI registering after the client has the server's tags, which is unconfirmed;
  - the bed's update tag grows by up to four keys, a contract between a server and a client of the same build;
  - unconfirmed by reading: whether Fabric fills `Item.BY_BLOCK` for the crops' items (replanting or air after a sickle
    or the Crop Harvester; the tests accept both); the Crop Harvester's footprint against the test's glowstone; a
    gametest server's `Component.getString()` resolving mod lang; `lookAt`'s aim at the crop and the bed; light settled
    by ticks 44 to 46; every test of a batch starting on one tick; the two new codex pages' length;
  - nothing of the unit has been compiled against Minecraft or run.
- **Unverified:** the null-side Fabric item insert (inferred from a passing post test *(survey)*); vanilla's container
  drop when a crucible overrides `preRemoveSideEffects` (the mirror has no `BlockEntity`; the Spire drops explicitly);
  whether Jade's accessor gives the viewing player; `#minecraft:supports_crops` on these crops (bees reach them: unit 4's
  risks); Fabric's load conditions on Concordance data; whether the client tests' dedicated server runs in the client's
  process.

## Explicit don'ts

1. Do not build another cauldron economy, a universal assembler, a second mana pool, a second logistics ledger or a
   parallel worker system (03:17; 06:122).
2. Do not clear, reroll or silently invalidate a formula; do not void output; do not duplicate spent heat or power on a
   break (03:54; 03:62-65).
3. No client-sent temperatures, totals or stacks; requests choose permitted settings (08:59; 08:99).
4. Keep the makeup out of the crucible's update tag and Jade (`check_mod_data.py:16290-16294`;
   `ConcordanceDataProvider.java:138-142`).
5. No crop death; no pollution read in the garden (09:128; D-005).
6. No new ore; the "Tide-bearing mineral" stays a role (03:41).
7. No product as the only source of steel, copper cable, clean water or ordinary medicine (06:26).
8. No wear, lubricant or replacement parts on the stand (D-004).
9. Do not edit `PeepoEntity`, `CompanionHusbandry`, `CompanionPopulationChecks` or `PeepoDeliveryClientTests` (#357,
   #369); do not replace Peepo with generic clones (03:181).
10. Do not change Preserve's crucible-only scope (`Rime.java:31-33`) or Chunk B's Proposed decisions without the owner.
11. Do not hand-edit generated data; change the generators.
12. Do not execute anything in the owner's library, and do not import whole trees (08:167).
13. Do not add a mixin for the poultice's credit while the tick rule works (CLAUDE.md: avoid unnecessary mixins).
14. Do not show signs, or write anything, from the anchor's shared `status()` or any encyclopedia page.
15. Do not read the server-only rules on the client.
16. Do not mark a Proposed decision approved for want of a reply; do not count one client as two; do not call the chunk
    complete because tests compile (10:162; 09:20; 09:315).
17. Do not restore or rewrite main's `CHANGELOG.md` here; this branch's merge resolution is the only change.

## Deferrals

- **Rimewash** (a Winter Brine salve holding a crucible's band): it would copy Preserve's Proposed hold (B-P13, B-P35;
  Chunk B's contract, lines 1413 and 1435) and preserve a band, not a reagent.
- **The reagent-preservation chain** (06:87-96), **Stillwater Keeping** and the Reagent Cabinet (03:100; 06:119): no
  reagent freshness exists to preserve, Stillwater needs a new result kind, and Preserve "never wraps an inventory"
  (Chunk B's contract, line 1064). Brine for opened preserve jars likewise.
- **Mending Salve** as the delivery plan framed it (a renamed Regeneration salve): superseded by the Stillroot Poultice.
- The other products of 03:31-39: Dewglass Wash, Hearth Lacquer, Gloam Ink, Threadwax, Assayer's Flux.
- A dedicated alchemy notebook or GuiLib workspace; the ritual planner screen with client ghosts (08:49-59); a per-player
  formula book.
- Graft cuttings as items; a visible graft tint (it would need blockstates and art).
- Season shelter and harvest choice (03:75, 03:78).
- Construct body upgrades (06:85; 03:195): outside C1 to C4, and no chunk of chapter 9 names them.
- Poultice credit in the Mire Hag's and the Crypt Lich's fights (they are not on the encounter credit).
- The agreed larger workload (H3, 09:282).

## Validation plan

| Unit | Server game tests | Client game tests | Checks | Save tests |
|---|---|---|---|---|
| 0 | `./gradlew build` on the merge: run 38617471274, `mod` passed on its one re-run | the five client shards passed in that run (attempt 1; which classes `choose client tests` picked was not read) | as the merge commit records: `check_mod_data`, `check_repository`, `owner_art --check`, `concordance_journey`, `check_icon_maps`, the World Designer tests | — |
| 1 | `ConcordanceAlchemyGameTests`, `ConcordanceAssayGameTests`, `LairEncounterGameTests`, `EncyclopediaGameTests`, `ConcordancePersistenceGameTests` | `EncyclopediaClientGameTests` (crucible pages, two scales; Jade) | `check_alchemy` (the fouling mirror), `check_winter_brine`, `check_workshop_products` (new), the Jade-snapshot lock, the pure list, the client-read rule; the build without optional integrations | restart before output; an unreadable saved program |
| 2 | `ConcordanceRitualGameTests`, `ConcordanceAssayGameTests`, `ConcordanceProgressionGameTests`, `EncyclopediaGameTests` | the anchor's Inspect page; Jade on an anchor | `check_rituals` and the Ley-capacity rule; the encyclopedia's forbidden list; `check_acquisition`; the build without optional integrations | a mid-run lapse |
| 3 | `ConcordanceAlchemyGameTests`, `ConcordanceEconomyGameTests`, `ConcordanceLogisticsGameTests`, `ConcordancePersistenceGameTests`, the cable check | `PeepoIntegrationClientTests`, `CompanionAutomationChecks`; a workshop shot | `check_alchemy` (the stand's targets), `check_economy`, `owner_art.py --check` if art is imported; the build without optional integrations | the stand; the crucible's keys present and absent; the ledger's codec |
| 4 | `ConcordanceGardenGameTests`, `EncyclopediaGameTests`, `ConcordancePersistenceGameTests`; re-run the Gleaner, Spire-field, Peepo regrowth and journey tests (the Crop Harvester's strain test runs only when `JUGCRAFT_TEST_FOCUS` is unset or names `machines`) | a bed's Inspect page; Jade on a crop | `check_ecology` (neighbours, strain, graft), the compost page; the build without optional integrations | an older bed; strain and graft through a save |
| 5 | `ConcordanceJourneyGameTests`; the load runner | the evidence set; `python tools/select_client_tests.py --base origin/main` | every check | — |

Every unit also runs `tools/check_mod_data.py`, `scripts/check_repository.py`, `scripts/tests` and the build with its
server game tests, and its CI by workflow_dispatch of the Build workflow with every client test class, as Chunk B did.

**Evidence rules** (10:5-22):
- Each command is reported on its own as run and passed, run and failed, or not run. CI on a commit counts for that
  commit only.
- A server game test with several test players is not a party of people. One client on a dedicated server is not two
  clients; the two-client dedicated-server session and a human journey stay not run unless two accounts are available
  (09:20).
- A CI runner's timings are not a player's hardware: the load card names the runner and claims nothing for other
  machines. A software-rendered frame time is that renderer's.
- A screenshot shows presentation, not balance; nothing is called "balanced" without a human playtest.
- Numbers from the generator's model or arithmetic stay labelled so until a game test measures them.

## Decisions

Chunk A's and Chunk B's decisions are unchanged: B-D0 to B-D3 are the owner's, and every B-P stays Proposed (Chunk B's
contract, lines 1390-1462). At most three decisions go to the owner at Chunk C's checkpoint (09:18; 10:113-116).

| ID | Date | Decision | Status | Affects |
|---|---|---|---|---|
| C-D0 | 14 October 2026 | "ok begin chunk c": Chunk C is the next chunk. It answers Chunk B's checkpoint question on the next school only; movement feel, B-P59 and B-P60 stay open, and every B-P decision stays Proposed | Accepted (owner) | All of Chunk C |
| C-D0b | 14 October 2026 | "New branch, main merged (Recommended)": Chunk C is developed on `claude/chunk-c-living-workshop`, started from Chunk B's head `ec06f16e0` with main `4d38e9712` (#356, #358) merged in (merge commit `e079ad4a3`; its CI run 38617471274 passed every job, the `mod` job on its one re-run after a first-attempt failure the session read as the known nuclear-plant site-clash flake from main; the log was not reachable to confirm the cause). The merge's two resolutions: the gametest `fabric.mod.json` is main's two-space file plus Chunk B's four classes; for `CHANGELOG.md`, #356 had cut main's file to its own 17-line entry, and the merge keeps the full history and adds #356's section (main itself is still short; a separate task was suggested to restore it) | Accepted (owner) | C-00, every unit |
| C-D1 | 14 October 2026 | "Poultice + Kiln Draught (Recommended)": (1) the Stillroot Poultice, a Mendvetch healing salve from the Alembic Crucible; a dirty batch gives less useful healing, predicted before brewing (this needs a real Mixture/Outcome rule, since today contamination only adds harmful effects and never lowers yield: designed in the generator and Java, mirrored and audited); healing another player with it at a lair boss earns a support share (Encounter credit for realised Regeneration healing from a poultice applied by another player, bounded so it cannot be farmed); (2) the Kiln Draught, a Fire Resistance draught (existing ingredients such as magma cream or sugar, per the alchemy survey) that lets a player wade the Cinder Kiln's slag (`MoltenSlagBlock` already honours Fire Resistance), named in the Cinder Kiln's dossier as a preparation fact, as Winter Brine is for the Glacier Hall; the Tyrant Scale to magma cream route audited as a boss-to-ingredient edge | Accepted (owner) | C-01, C-02, C-10 |
| C-D2 | 14 October 2026 | "Root and Rivet (Recommended)": turns a Thallite plate into a Rooted Thallite Foundation, the base the Crucible Stand is built on; uses the existing ritual engine (Transform result) and pylon economics, with a declared stage and payment table | Accepted (owner) | C-03, C-04, C-05 |
| C-D3 | 14 October 2026 | "Crucible Stand (Recommended)": under the Alembic Crucible, on the rooted foundation; turns JE into the heat the running formula's next stir needs (starting rates about 8, 16 and 32 JE/t for warm, hot and searing), while an Electric Pump pipes water into the crucible's tank; manual heat sources still work | Accepted (owner) | C-05, C-06, C-10 |
| C-D4 | 14 October 2026 | Both carriers, one demand: the crucible's formula shortage, served by a Peepo through the existing `CrucibleCompanion` port and by a Clockwork Porter through a courier request the crucible files (09:136 names both `MachineCompanionPort` and `CourierLedger`). Not asked; the session's default | Proposed | C-06 |
| C-P1 | 14 October 2026 | A formula's identity is its canonical text (the Peepo plan id hashes it); the rules' revision is `ConcordanceData.revision()`, process-local, so not 03:46's stable version (a recorded deviation); no new formula save key; an incompatibility is re-derived after a load or reload; an unreadable saved program is kept as text and reported. As built in unit 1: `incompatibility()` replays the formula once per revision and after a load, and names the refused step; an unreadable program is kept under `program` (see C-P23) | Proposed | C-01 |
| C-P2 | 14 October 2026 | Products are generated data; a brew matching one is named on the stack with vanilla's item name, recognised by what it carries, never its name; Winter Brine is named the same way. Alternative: JEI and codex names only. As built in unit 1, for the Stillroot Poultice, the Kiln Draught and Winter Brine; how a stack is matched is C-P29 | Proposed | C-02 |
| C-P3 | 14 October 2026 | A crucible's Inspect page: spoon level and an ungated risk line (a harmful outcome or a searing step) for anyone; glass level with the Alembic Arts understood and an Assay Glass carried; the outcome and replay preview for a master; within the player's block reach, not 16 blocks. As built in unit 1, refined by C-P25 (the risk line's words) and C-P26 (the reach's allowance, and the glass carried at every level above the spoon's) | Proposed | C-01 |
| C-P4 | 14 October 2026 | Jade keeps showing a running formula's next operation to anyone, as today, beside JEI's "formulas are never shown" (an inconsistency noted, not changed). As built in unit 1; the line reads "Next step: ..." (C-P27) | Proposed | C-01 |
| C-P5 | 14 October 2026 | Every state-changing crucible use (setting or clearing a formula, water, adds, stirs, bottling) asks `Authority.mayChange`; spoon, glass and empty-hand reading stay open. As built in unit 1, with recording onto a blank formula left open (C-P24) | Proposed | C-01, C-06 |
| C-P6 | 14 October 2026 | One shared diagnostic vocabulary in a pure `concordance/diagnostic` package for the crucible, the stand, ritual readiness and crucible filing; `CompanionStatus` and the porter's `Status` map from it at their edges. As built in unit 1 for the crucible: `Code` (19 codes in four kinds) and `Diagnostic`; `CrucibleCompanion.status` maps from it; the ids and kinds are C-P22 | Proposed | C-01, C-03, C-05, C-06 |
| C-P7 | 14 October 2026 | The yield rule: a property may declare `fouling`; Verdance alone does, 4.0 (each unit of contaminant a part takes 4 units of Verdance before level and duration), a starting value; a reason names the foulers. As built in unit 1; `check_alchemy` holds the order (taken, subtracted, then judged) and the generator's mirror | Proposed | C-02 |
| C-P8 | 14 October 2026 | The poultice's credit: on each tick Regeneration heals, the health a patient gains, at most 1, credited through `Encounter.healed` to the player who applied the poultice, while it lasts; transient; only a poultice (a Frostward or any other salve earns nothing); no mixin. As built in unit 1 as C-P30 refines it, credited through `creditHealing` (`healed`'s old body) | Proposed | C-02 |
| C-P9 | 14 October 2026 | The Kiln Draught is a draught carrying Fire Resistance and nothing harmful; its dossier fact is text and icons in the Cinder Kiln's rite section with a codex link at the Alembic Arts observed and the Salamander Charm as the lasting answer (the Charm alone when the Concordance is off or Ember no longer brews Fire Resistance); the Tyrant Scale → magma cream edge (C-D1's audit) is recorded as a reward route, not a loop: the draught makes no Kiln Seal or scale, and magma cream has vanilla sources. As built in unit 1, with C-P31 (his flame) and C-P32 (the Grave Dust route) | Proposed | C-02 |
| C-P10 | 14 October 2026 | A run-level Ley shortfall warns and does not refuse; the start rule (one step's Ley) is unchanged; signs mark faults on the empty-hand, chat and refusal paths only. As built in unit 2: the warning names the step the run would break off at (since the unit review, "unless it is refilled meanwhile": a pylon on JE gains more a step than any rite draws); `check_rituals` holds the one-step start; how signs mark is C-P35 | Proposed | C-03 |
| C-P11 | 14 October 2026 | Root and Rivet is taught by Circle Lore understood; offerings a Thallite plate, 4 copper ingots, 4 Mendvetch; 1 participant, 6 Focus, 4 steps of 2 Ley a pylon, any light, backlash 4 (starting values). As built in unit 2, with these numbers; the foundation it makes is C-P40 | Proposed | C-04 |
| C-P12 | 14 October 2026 | The stand: targets warm 65, hot 120, searing 175 (starting values) at C-D3's rates; it follows the next stir's band only while the formula can proceed and idles otherwise; a Preserve hold wins; with no formula an empty-hand use by someone who may change it cycles its setting; crafted from the foundation, iron and copper; no wear | Proposed | C-05 |
| C-P13 | 14 October 2026 | Crucible filing: the requester is the formula's setter; one open request; filed on entering the ingredient wait, with backoff after no post or `TOO_MANY` (200 ticks doubling to 1200, starting values); cancelled when the formula is cleared, replaced, set by another or incompatible; the Peepo port subtracts the open remainder | Proposed | C-06 |
| C-P14 | 14 October 2026 | Light is shown as sky and lamps; niches are unchanged. As built in unit 4: the Inspect page gives the light at the plant as sky and lamps and judges the greater, as growth does (C-P54) | Proposed | C-07 |
| C-P15 | 14 October 2026 | The canteen waters Verdant Beds and the compost JEI page is wired, rather than deleting their claims. As built in unit 4: the canteen pours on beds through the bed's own rule (C-P56), and JEI shows the compost pages (C-P57) | Proposed | C-07 |
| C-P16 | 14 October 2026 | The mixed bed: Dewmoss and Gloamcap, +25% pace each way within the 5×5 (a starting value); no Mendvetch pairing, no fixer beneficiary; named "mixed bed". As built in unit 4: an organism's optional `neighbours`; Dewmoss and Gloamcap name each other at 25 (C-P58) | Proposed | C-08 |
| C-P17 | 14 October 2026 | Harvest strain: +1 a seen harvest, at most 3; it only ever worsens the disturbance fit (alternative: plain disturbance, which would let a harvested Gloamcap bed grow with no magic block); eased by rest (one a bed random tick), explicit compost points (bone meal, fertilizer, the Maw's mulch) and a Greenwarden's tending (once a 600 ticks a bed), never by nitrogen fixing; never kills. As built in unit 4: counted and shown as C-P59 says; rest only while no crop grows on the bed (C-P60); compost includes bone meal and fertilizer used plainly on a growing crop, and tending asks Authority (C-P61) | Proposed | C-08 |
| C-P18 | 14 October 2026 | One graft, Dewlit (Sunpetal host, Dewmoss donor): light `[9,11,15,15]`, moisture `[4,5,7,7]` (starting values); Verdant Husbandry understood; a plain use, not sneaking; bed-bound, cuttings deferred; produce, cost and alchemy unchanged. As built in unit 4, with C-P62 to C-P65 | Proposed | C-09 |
| C-P19 | 14 October 2026 | The load budget, agreed before merge: the workshop adds at most 1.0 ms median and 2.5 ms p95 server tick, with a flat memory trend over 6,000 ticks, on the stated runner; recorded, not claimed for other hardware | Proposed | C-10 |
| C-P20 | 14 October 2026 | The load runner: a server game test of its own, opted in so ordinary CI runs are unchanged; CI's software-rendered frame times recorded as that renderer's only | Proposed | C-10 |
| C-P21 | 14 October 2026 | The Gleaner's Heart search (729 positions) is measured in C-10 and the record corrected; its radius changes only if the measurement says so. As built in unit 4: the ecology record now gives the Maw 147, the Gleaner 147 for crops and 729 for Hearts; the measurement stays in C-10 | Proposed | C-07, C-10 |
| C-P22 | 14 October 2026 | Diagnostic ids use 03:9's own wording, then Chunk C's (`needs_water`, `needs_container`, `held`, `incompatible`, `courier_quota`, `output_differs`, `needs_emptying`, `unreadable`). Each code has a kind that sets the sign shown when it begins: a shortage or a blockage a WANT, a fault a PERIL with the breaking sound, a condition (heat, a hold) none. Codes no unit raises yet have words without parameters until their unit fixes both. The crucible's formerly silent wait before a repeat now shows a WANT sign once | Proposed | C-01, C-03, C-05, C-06 |
| C-P23 | 14 October 2026 | A formula the rules refuse, or a saved one that no longer reads, drives nothing: no step and no automated bottle, even of a finished batch. Bottling by hand stays open; clearing stays a sneak-use by someone who may change the crucible | Proposed | C-01 |
| C-P24 | 14 October 2026 | Recording a blank formula stays open to anyone with the Alembic Arts (it changes nothing in the crucible); every other change asks Authority (within C-P5) | Proposed | C-01 |
| C-P25 | 14 October 2026 | The ungated risk line names the harmful statuses a dose would carry and the formula's first searing step, rather than "would harm" alone. Since the unit review, it also names the harm (and a searing stir) of a mixture that is not the formula's own batch (there before it was set, or under a formula the rules refuse), and a status is harmful when the server judges it so as well as when its property says | Proposed | C-01 |
| C-P26 | 14 October 2026 | The Inspect page reads a crucible within the reader's block reach plus the server's 1.0 use allowance; every level beyond the spoon's needs an Assay Glass carried, a master's included, as the glass itself does | Proposed | C-01 |
| C-P27 | 14 October 2026 | Jade carries the reason as `Diagnostic.encode()` text (code and parameters, at most 1024 characters, else the code alone) and the tank as whole parts. Jade's next step stays canonical text (C-P4) while the Inspect page words it | Proposed | C-01 |
| C-P28 | 14 October 2026 | A test that stands for a reload swaps the rules only through a helper that restores them in a `finally` within the same tick, not in a batch of its own | Proposed | C-01, C-10 |
| C-P29 | 14 October 2026 | A product is any brew of its form carrying its status and nothing harmful (judged by the server); a sweet-berry Regeneration salve is therefore a Stillroot Poultice; a brew matching two products stays unnamed | Proposed | C-02 |
| C-P30 | 14 October 2026 | Poultice credit is read from Regeneration's own time left at each tick's end, each blow and each Concordance heal, at most one health a heal. One note per patient; a later poultice that takes replaces it; it ends when the Regeneration is no longer the poultice's or the patient leaves the fight. The helper is credited wherever they stand, as a ward's caster is | Proposed | C-02 |
| C-P31 | 14 October 2026 | The Kiln Draught's dossier fact says it also turns the Tyrant's flame (breath, waves, patches), as Fire Resistance already does | Proposed | C-02 |
| C-P32 | 14 October 2026 | Grave Dust → bone meal → Mendvetch is a second declared reward route; boss routes are audited by recipes alone, not through the Assayer's Scale | Proposed | C-02 |
| C-P33 | 14 October 2026 | Rites use the shared codes. An offering short is `missing_ingredient`, its words made general for crucible and anchor. `unavailable_power` takes (what, held, needed) as lines and `incomplete_pattern` takes (structure, faults to mend); both are first raised here. `not_lent`, `needs_focus`, `too_bright`, `too_far` and `participants` are appended after unit 1's codes | Proposed | C-03, C-05 |
| C-P34 | 14 October 2026 | Readiness judges its reader as the rite's leader. Nothing offered chooses no rite. A fully matched rite the reader has not learned is never named on the Inspect page or in Jade; the start's refusal still names it, as before. Before a start, the other participants appear as the stage table's gather row; during a gathering, as `participants`. As built in unit 2, where a joined participant who has walked away is not listed while the rite runs: the next step's own check finds them | Proposed | C-03 |
| C-P35 | 14 October 2026 | An empty-hand or command reading marks every faulted part where it stands, at most once a second a player: shape faults with their interruption's sign, and a channel short of a step or unlent for the reader with WANT (since the unit review, not one only warned short of the run). A refused start marks the refusing check's faults with that refusal's sign. The Inspect page and Jade never mark | Proposed | C-03 |
| C-P36 | 14 October 2026 | Fault positions read "x, y, z (dx, dy, dz from the anchor)" in chat, the command and the Inspect page; Jade keeps "x,y,z" | Proposed | C-03 |
| C-P37 | 14 October 2026 | An interruption is told in one line: why, where the offerings are, and the Focus and Ley Charge the attempt lost, every participant's Focus together (without the loss when the rules no longer know the rite). It is kept in memory for the Inspect page until the next start or a load, never saved. TAMPERED's line under-counts its last draw | Proposed | C-03 |
| C-P38 | 14 October 2026 | Jade on an anchor shows the participants it needs, who is channelling, the first three offerings and "+n", and, while gathering or channelling, the Focus and Ley Charge spent and the offerings held. Never the rite, anyone's Focus held or research | Proposed | C-03 |
| C-P39 | 14 October 2026 | Every rite's stage and payment table is worded from its data on the Inspect page, for a reader who knows the rite; the codex states Root and Rivet's in its own words (`_payment_text`, for a transforming rite; C-04) | Proposed | C-03, C-04 |
| C-P40 | 14 October 2026 | The Rooted Thallite Foundation is a plain described item in the Ingredients tab. The Assayer's Scale never weighs it, nothing turns it back, and the economy models it as a sink of 32 Ley Charge and 6 Focus a run. Its icon is drawn fresh, because nothing in the owner's library fitted, and awaits the owner's review | Proposed | C-04, C-05 |
| C-P41 | 14 October 2026 | A rite's offerings may need feature switches beside the Concordance's. The generator states them (`"switches"`) and `check_rituals` holds that statement to the recipes, one step down, an offering that is a tag through its members (since the unit review, a machine recipe's `{"ingredient", "count"}` entries followed too, and a condition with `"or"` alternatives counted as needing none of its switches alone). Concordance data carries no load condition. Any recipe that makes an offering counts as its way to make it | Proposed | C-04 |
| C-P42 | 14 October 2026 | The codex's first `entry://` link (Stratawrighting's Understood page to Root and Rivet). Every codex link is audited to an entry on disk (since the unit review, its `@anchor` to a page id and its `#n` to a page of the entry), since a broken link stops Modonomicon opening the book (as read from its source by the C-04 design; not seen in a client) | Proposed | C-04 |
| C-P54 | 15 October 2026 | The Inspect page reads a garden bed: a Verdant Bed, or a crop through the bed below it, within the reader's block reach plus 1.0 (`GARDEN_REACH`, as a crucible's, C-P26). It takes a fresh reading from the bed's cached sample, sampled again only as a plant would; a dormant bed is not read; a bed with nothing on it that a niche judges gives its readings unjudged. Each factor is a judged row naming its reach, with all four bounds of its range; light is shown as sky and lamps and judged as the greater. `Verdict.reasons` (Jade, chat) is unchanged | Proposed | C-07 |
| C-P55 | 15 October 2026 | Stale verdicts clear: a bed forgets its verdict on its random tick once neither a crop nor a Verdant Heart stands on it, and the cleared note is synced and saved. Jade on a ripe crop shows its bed's last verdict as "As it grew: ..." | Proposed | C-07 |
| C-P56 | 15 October 2026 | A held item is its own use on a bed: an item in the main hand is that item's own use on a Verdant Bed, and the bed reports, wakes or tends only for an empty main hand. A Gourd Canteen's sip wets a bed below 7 through `VerdantBedBlock.pour`: `RateGate` ("garden", 4 ticks), then `Authority` (a refusal shows `missing_permission`); a sip is spent only when it wets. The pour is not gated by the switch, as the bed's own watering is not | Proposed | C-07 |
| C-P57 | 15 October 2026 | JEI's compost page: an information page for each item a Mulch Maw eats, worth its highest tag as `Garden.mulch` reads it (`recipe_view.json` `garden_compost`; the client's tags, the default data's worths) | Proposed | C-07 |
| C-P58 | 15 October 2026 | The mixed bed: a crop may declare `neighbours: {organisms, percent}` (1 to 50), naming other crops that cost nutrients, never itself; Dewmoss and Gloamcap name each other at 25 (a starting value). The kinds come from the sample's existing set, with no new read. The bonus is speed only; a fixer neither takes a bonus nor is named as a neighbour, and the rules report a bad neighbour | Proposed | C-08 |
| C-P59 | 15 October 2026 | Harvest strain is counted and shown: a bed notes `ripened` when growth reaches the last age, and the next time it sees its crop below ripe or gone (a growth attempt, its random tick, compost or tending) it counts one, up to 3, whatever took the crop (a Hay Golem or a crow too). A crop placed ripe never strains. `Verdict.of` judges disturbance as the worse of the area alone and the area with the strain. Shown as the `ecology.strained.*` reasons, "Recently harvested: strain n of 3" (reports and Jade) and the Inspect page's disturbance row; a harvest not yet counted shows as counted (`strainNow`) | Proposed | C-08 |
| C-P60 | 15 October 2026 | Rest: one strain rests away a bed random tick, only while no crop grows on the bed (fallow, ripe, or stalled or dormant by its last note); a random tick that counts a harvest rests nothing; with the switch off no strain changes. It refines C-P17's "one a bed random tick", from the model's arithmetic (a random tick about every 1,365 ticks, a Sunpetal's regrowth about 16,400), not a measurement | Proposed | C-08 |
| C-P61 | 15 October 2026 | Compost and tending: each dose of bone meal on the bed, bone meal or fertilizer on its crop (now a plain use on a growing crop: fertilizer by its own area rule, bone meal by the bed's rate gate and who may change the bed, a refusal consuming the use) or a Mulch Maw's nutrient eases one strain; `give` and `nourish` never do. Tending: a Greenwarden's empty main hand on an awake, strained bed they may change (Authority), or on its growing crop (the unit review: a crop covers its bed from above; `VerdantBedBlock.tendBy` asks at the bed), eases one, at most once every 600 ticks a bed; not saved, so a restart allows one early tending. Anyone else only reads the bed | Proposed | C-08 |
| C-P62 | 15 October 2026 | The graft gesture: a plain main-hand use, by a Greenwarden with the Concordance on, of a graft's donor item on its host's crop, at any age. Rate-limited; refused for no bed, for Authority refused at the crop or the bed, or for a bed already carrying a graft the rules know. Every outcome consumes the action and never harvests, reports or plants; success spends one donor. Anyone else's donor is just an item | Proposed | C-09 |
| C-P63 | 15 October 2026 | Graft data and checks: `concordance/graft/<id>.json` is `{schema, host, donor, niche}` only. The loader requires two different loaded crops, a grafted nutrient floor at least the host's cost and one graft a host and donor. `check_graft` holds the pilot's cap of one graft, exactly one range better and one worse (03:76), and no cost, yield or alchemy. A bed carries one graft | Proposed | C-09 |
| C-P64 | 15 October 2026 | Clearing a graft: the first growth attempt of another organism's crop clears it (time, a sprinkler, a Spire's hastening), before the dormant check; reading never clears it. A graft the rules no longer know is kept, read by nothing, and replaced by the next graft. Breaking the bed loses it; a Verdant Heart or a device set on the bed does not clear it | Proposed | C-09 |
| C-P65 | 15 October 2026 | Graft network and save: clients are told `graft` and `graft_host` only while grafted, never the keeper. Jade names a graft on a crop only when the crop is its host, and on a bed only when the rules know it; the Inspect page does the same. The save writes `graft` only when set, never `graft_host`; the stamp stays 1 | Proposed | C-09 |

## Where to resume

Units 1 (C-01, C-02), 2 (C-03, C-04) and 4 (C-07 to C-09) are built and recorded, and none of their game code has been
compiled against Minecraft or run. Unit 4 was built on `23f28b90e`, beside unit 3 (C-05, C-06), which is being built from
the same base on its own commits. Next:
1. merge units 3 and 4. A trial merge (`git merge-tree`, which writes nothing; unit 3 at `2f13fed54`, the unit review)
   conflicts in this page (the status, the units table's rows 3 and 4, the interfaces' As built paragraphs, the risks,
   the decisions after C-P42, where to resume and how it was checked), `docs/WHAT_EXISTS.md`,
   `docs/INTEGRATION_STATUS.md` and the CHANGELOG's Chunk C section, and also in
   `arcane-concordance-authority.md` (one: each unit's paragraph), `arcane-concordance-persistence.md` (the block
   entities' row, since the unit review: each unit's sentence), the `ConcordanceDataProvider` class Javadoc (one),
   `EncyclopediaClientGameTests` (one) and `ConcordancePersistenceGameTests` (three: imports, the class Javadoc and each
   unit's new test; the class has 8 tests here and 10 with unit 3's). Each is resolved by keeping both sides, unit 3's
   first. `tools/check_mod_data.py` (`AUTHORITY_ROUTES`: unit 3's line after the crucible's, unit 4's two after the
   Gleaner's; the client-read needles; separate functions and calls), the Jade client and lang merge cleanly; lang and
   `recipe_view.json` are regenerated by `python3 -B tools/generate_material_data.py`, never merged by hand;
2. refresh main and the open pull requests (#357 and #369 for the Peepo files; the hot shared files: lang,
   `check_mod_data.py`, `CHANGELOG.md`, `WHAT_EXISTS.md` with #366, `INTEGRATION_STATUS.md` with #361 to #365), and
   merge main again if it moved;
3. run CI on the pushed head by workflow_dispatch of the Build workflow, with every client test class. The Build runs
   the server game tests twice, with Jade (the default) and with `-PjugcraftOptionalIntegrations=false`; the Jade checks
   of `jadeIsToldTheTankAndWhyNeverTheMakeup`, of unit 2's anchor tests and of unit 4's bed tests run only in the first.
   Unit 2's classes: `ConcordanceRitualGameTests` (29), `EncyclopediaGameTests` (21 then), `ConcordanceAssayGameTests`
   (8), `ConcordanceProgressionGameTests`, `ConcordanceGameTests` (`codexLoadsWithoutErrors`, with the new entry),
   `ConcordanceRimeStrataGameTests`, `ConcordanceSpireGameTests`, `ConcordanceEconomyGameTests`,
   `ConcordanceJourneyGameTests`, `ConcordancePersistenceGameTests`; on the client, `EncyclopediaClientGameTests` (three
   new shots) and `EncyclopediaRoutesClientGameTests`. Unit 4's: `ConcordanceGardenGameTests` (33),
   `EncyclopediaGameTests` (23), `ConcordancePersistenceGameTests`, `ConcordanceSpireGameTests`,
   `ConcordanceJourneyGameTests`, `ConcordanceWorkloadGameTests`, `HalloweenGameTests` (the canteen) and
   `ConcordanceGameTests` (the organisms entry's two new pages); on the client, `EncyclopediaClientGameTests`
   (`gardenInspection`, two shots) and `PeepoIntegrationClientTests` (`livingHarvests`). Record each result in the As
   built notes, `arcane-concordance-alchemy.md`, `lair-encounter-credit.md`, `cinder-kiln.md`,
   `arcane-concordance-rituals.md` and `arcane-concordance-ecology.md`;
4. the checkpoint evidence of units 1 to 4 (the owner's look at the foundation's icon among it), then unit 5 (C-10): the
   load card (with a Maw, a Gleaner and nine beds, the Gleaner's Heart search measured, C-P21), the journey, the client
   evidence (a bed's Inspect page and Jade on a crop among it), combined fixes, the checkpoint and the handoff.

The owner's choices are C-D0, C-D0b and C-D1 to C-D3; C-D4, C-P1 to C-P42 and C-P54 to C-P65 are Proposed (unit 3's
C-P43 to C-P53 join them in the merge). Open for the owner's checkpoint: the review route for Chunk B and Chunk C
(above), and 09:144's three questions.

## How this contract was checked

- Re-read at `e079ad4a3` for this page: the crucible (fields, tick, heat, automation, bottling, use, formula, empty-hand
  use, faces, save and load, update tag), `CrucibleBlock`, `Formula`, `Outcome`, `Property`, `Mixture`'s constants and
  bottling, `Heat`, `Band`, `Alchemy`, `BrewItem`, `ConcordanceData`, `Composing`'s use of the revision;
  `CrucibleCompanion` and `CompanionStatus`; `Encounter` (credit, healing, wards, claims), `Lairs`' observer,
  `ConcordanceEffects`' observer call, `LairDossiers`' rite and preparation, `MoltenSlagBlock`, `SalamanderCharm`, the
  Tyrant's flame and his loot table; the anchor (use, begin, status, observe, report, interruption, removal),
  `RitualDefinition`, `Signs`, `Strata`'s listener, `InspectPage`, the Jade plugin, provider and client lines named;
  `Sampler`, `Niche`, `OrganismCropBlock`, `VerdantBedBlockEntity`, `VerdantBedBlock`'s uses, `Garden.nourish` and
  `bedAndNeighbours`, the Maw's giving, every niche reader, the organism, ingredient and property data, the canteen, the
  Gleaner's reach; `CourierLedger.file` and `destinationGone`, `Logistics`' limits, the porter's statuses and delivery,
  `ConcordSpire.restock` and `nearestPost`, the Spire Heart's removal, `Couriers.request`; the pylon, `LeyPylonBlock`,
  the Electric Pump, the crucible's fluid registration; `Authority` and `Illumination`; `RulesParser`'s allow-list; the
  Thallite plate's recipes and tags; `tools/check_mod_data.py` at the lines cited; `tools/concordance_alchemy.py`,
  `concordance_ecology.py`, `concordance_rituals.py` and `machine_connections.py` at the lines cited; the test classes
  named (their `@GameTest` counts), `theCircleIsCheckedPartByPart`'s assertions and the progression node list;
  `.github/workflows/build.yml`, `docs/TESTING.md:41`, `tools/select_client_tests.py:53`; the records' cited lines; plan
  chapters 09 (sections 1 to 2, 5, 11 and 12), 03 (whole), 06 (sections 1 to 9), 08 (sections 3, 5, 7 to 9) and 10
  (whole), `CLAUDE-MASTER-PROMPT.txt` 60-90 and `START-HERE.md` 44-50; mc263src `MultiPlayerGameMode.performUseItemOn`,
  `Inventory.placeItemBackInInventory` and `MinecraftServer`'s tick times.
- Read by REST on 14 October 2026: the open pull requests and their heads; run 38617471274's jobs and steps, both
  attempts. Compared in a scratch repository: each open pull request's files from its merge base against this plan's
  files (#370's head fetched again).
- Checked again after the page was written (14 October 2026): every code, data, test and record citation above opened
  at `e079ad4a3` at its cited lines; the plan-pack and Chunk B contract lines cited; the merge's parents, its two
  resolutions and the diff counts; by REST, the open pull requests (18, #371 new, its files by `/pulls/371/files`), that
  neither `claude/chunk-b-winter-stone` nor `claude/chunk-c-living-workshop` has a pull request, run 38617471274's jobs
  and steps for both attempts and the failed `mod` job's annotations (its log was refused, 403). Corrected here: line
  numbers (09:16 for "end-to-end path first", `concordance_alchemy.py:33-36`, the plate tag file), the C-02 model
  figures, the Peepo heat status, the Stacking rule, the theCircleIsCheckedPartByPart summary, the bed-note writers, the
  canteen's targets, the construct-upgrade deferral, #371, and survey labels on facts now verified.
- Taken from the survey reports, the delivery plan or its review without re-reading, and marked *(survey)*: the
  alchemy survey's section 9 obligations; the harvest-path table; loot-table harvests dropping unstamped produce; the
  null-side insert; #370's Concordance keystone.
- Not determined: the cause of run 38617471274's first `mod` failure (the log host refused this page's requests; recorded
  from the session's reading); whether Fabric applies load conditions to the Concordance's loader; whether the client
  tests' dedicated server shares the client's process. Answered for unit 1 (C-01's and C-02's As built notes):
  Regeneration's healing interval in 26.3, read in `mc263src` HEAD; how a batch is isolated (vanilla batches by
  environment and dimension; unit 1 does not rely on it).
- Run for this page: the alchemy generator's model (`tools/concordance_alchemy.py` `simulate`, imported read-only from a
  scratch script) for the C-02 figures; `python3 scripts/check_repository.py` (passed) and `git diff --check` on the
  page. Not run: any build, game test, data generator or other check.
- Unit 1's notes (15 October 2026): written from the unit's four commits, re-read for the names, keys, counts and test
  names given; the figures re-run from the generator's model (`bottle`, `outcome`, `poultice_verdance`, `realised`,
  imported read-only from a scratch script). The checks run for the records' commit are in the alchemy record's
  Verification.
- Unit 2's notes (15 October 2026): written from the unit's two commits (`a0e73881c`, `2c98d6d68`) and their scratch
  logs (the checks, the audit mutations and the client-test selection), re-read for the names, keys, counts, test names
  and lang given; the library candidates' hashes taken again from `art/owner-library` (read, nothing executed). Run for
  the records: the scratch harness (above, C-03's As built), and the checks in the rituals record's Verification.
- Unit 4's notes (15 October 2026): written from the unit's three commits (`5ba973632`, `d32ac3a61`, `d6edee4f4`), their
  messages and scratch logs (the checks, the generator's two runs and the C-08 and C-09 audit mutations), with the
  spec's code compared line by line against the commits for the departures; re-read for the names, keys, counts, test
  names and lang given. Run for these records on the final tree: the three audit-mutation suites (C-07's written again
  from the spec's table), the two pure harnesses, `git diff --check` on each code commit, `select_client_tests.py`, and
  the checks in the ecology record's Verification.
