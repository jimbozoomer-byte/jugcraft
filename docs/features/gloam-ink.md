# Gloam Ink

Status: built for branch `claude/gloam-ink` (not yet pushed; not merged), in commits `f9e200f19` (covered extraction,
Gloam Ink and Gloam Sludge) and `4a0b2c1d9` (fair copies of Research Notes), on this contract (`199114e7e`); this
record's own commit follows them, and a fifth applies the review's fixes (As built, "The review's fixes"). Nothing has
been compiled or run in a game: the branch has not been pushed and CI has not run. Every result below is a local Python
check or audit mutation (Verification).
Proposal issue: none; the owner's checkpoint answer C-D6 (23 October 2026, confirmed as the next work on 28 October 2026)
is the scope approval ([Chunk C's contract](concordance-chunk-c.md), Decisions).
Owner: @jimbozoomer-byte. AI-assisted implementation with Claude Code; the model is named in the commit trailers.
Target milestone and tier: Concordance milestone 5; Practitioner (the Alembic Arts understood).
Primary specialty and supported player role: Alembists; Greenwardens grow its Gloamcap; any reader gains from a copy.

## Contract

Definition: [the first product family](../plans/concordance-expansion-2026-10-10/03-workshops-ecology-and-pacts.md)
("Gloam Ink | Gloamcap and approved Hollow carrier | Covered extraction | Research notes, safe links and ward
inscriptions | Excess contamination yields unusable sludge, recoverable through a mundane route"); the smallest outcome:
one formula from a mixed bed's Gloamcap with one existing consumer, Research Notes.

| Ticket | Scope |
|---|---|
| GI-01 | The lid and the covered stir: `stir <band> covered`, `Mixture.covered`, `CrucibleBlockEntity.covered`, a formula waits for its lid (`needs_cover`), Jade, Inspect (the Peepo mapping: see As built) |
| GI-02 | The extract (`concordance/extract/gloam_ink.json`, `Extract`), Gloam Ink and Gloam Sludge, the furnace route, the codex entry, JEI pages |
| GI-03 | Bounded and never fed back: the Scale exclusion, values, the survival route, the economy row, `check_gloam_ink` |
| GI-04 | The consumer: a fair copy of Research Notes (`ResearchNotesCopyRecipe`), `check_fair_copies` |
| GI-05 | The living workshop's sixth step; `ConcordanceGloamInkClientGameTests` |
| GI-06 | Records and the checkpoint |

Decisions GI-P1 to GI-P17, each Decided by the session (proposed, for the owner's checkpoint; none accepted by the owner):
- GI-P1: "covered" is a lid, a closed trapdoor in its lower half or a bottom or double slab on the crucible (a
  `#jugcraft:crucible_lids` block whose underside is whole); a stir under it is covered; a formula's covered stir waits
  for its lid before its heat; any other stir is taken as before.
- GI-P2: the approved Hollow carrier is the spider eye (the owner's approval asked).
- GI-P3: Gloam Ink is an item of its own a glass bottle draws, never a named draught; both items stack to 16 and give
  their bottle back when crafted.
- GI-P4: Gloamcap at least once, a spider eye at least once and nothing else, every stir covered: murky (0.25
  contaminant a part) draws Gloam Sludge, at least 1.00 Hollow a part Gloam Ink, thinner the draught. A bowl draws a salve.
- GI-P5: a furnace boils Gloam Sludge back to its glass bottle, nothing more (smelting, 200 ticks, no experience).
- GI-P6: bottling Gloam Ink by hand is an Alembic Arts outcome of its own; sludge counts nothing.
- GI-P7: `needs_cover` is a blocked wait; a Crucible Stand idles, and reads the lid itself before that wait (As built).
  (A Peepo showing it as paused is not built: As built.)
- GI-P8: neither item is weighed, formed, composted, mulched or an ingredient; both at the values file's fallback.
- GI-P9: Gloam Ink's use is a fair copy: an original sheet, a blank sheet and the ink make one copy; the original and the
  bottle come back; a copy teaches what its original does and credits its writer once.
- GI-P10: only an original is copied. GI-P11: one copy a craft; copies stack. GI-P12: a special crafting recipe, crafted
  on the server, refused with the Concordance switched off. GI-P13: the copy holds only the notes; `fair_copy` is
  optional and older sheets are originals.
- GI-P14: icons from the owner's 23 October collection, recoloured (Common Ink in Gloamcap violet; Blood Vial in Hagmoss).
- GI-P15: one JEI page each, from `tools/acquisition.py`.
- GI-P16: the living workshop's sixth step brews it and makes a copy.
- GI-P17: deferred: safe links, ward inscriptions, copies of copies, a copy's own icon, a JEI display of the copy recipe,
  a Peepo counting ink as stock.

Validation plan: server game tests `ConcordanceGloamInkGameTests` (12), `gloamInkAndSludgeAreNeverFormed`,
`extractsTurnIntoNothingTheyCameFrom`, five `ConcordanceSharingGameTests`, `aFairCopyCreditsItsAuthorOnce`,
`theWorkshopsRecipesLoad`; client `ConcordanceGloamInkClientGameTests` (three shots); audits `check_gloam_ink`,
`check_fair_copies`, amended `check_signs` and `check_economy`, with every mutation reported; two-client play not planned.

Questions for the checkpoint: see Rollout and open questions.

## As built

Commit `f9e200f19` (GI-01 to GI-03): `Operation.Stir(band, covered)` with a one-argument constructor kept,
`Mixture.covered()`, the new `alchemy/Extract` (its kinds, `drawn`, `madeOf`), `Assay.murky`/`murkyFrom` and a risk
line naming a murky batch's failure, `AlchemyCatalog.extracts`, `RulesParser.extract` and the `extract` kind in
`ConcordanceRules`, `CrucibleBlockEntity.LIDS`/`covered(BlockGetter)` (the lid read before the heat in `automate`, by
hand at each stir, at both bottlings), `Alchemy.extract`, the items `jugcraft:gloam_ink` and `jugcraft:gloam_sludge`
(`DescribedItem`, stack 16, glass bottle remainder, the Ingredients tab), `Code.NEEDS_COVER`, Jade's `covered` and its
line, the Inspect page's lid line and a master's preview of the draw; the tools (`concordance_alchemy.py` EXTRACTS and
words, codex entry `alembic/gloam_ink`, tag `#jugcraft:crucible_lids`, the smelting recipe, `concordance_alchemy_art.py`
`gloam_ink`/`gloam_sludge`, `owner_art.py` Oct23 adaptations, `concordance_diagnostics.py`, `concordance_equivalence.py`,
`concordance_economy.py`'s row, `acquisition.py` EXTRACTED, `survival_native_followup.py`), `check_gloam_ink` and the
amended `check_signs`/`check_economy`; `ConcordanceGloamInkGameTests` (12, registered),
`ConcordanceAssayGameTests.gloamInkAndSludgeAreNeverFormed`, `ConcordanceEconomyGameTests.extractsTurnIntoNothingTheyCameFrom`;
the generated data, models, item definitions, lang, codex, `recipe_view.json`, manifest, the two icons, `values.json`,
the registry snapshot and the economy table.

Commit `4a0b2c1d9` (GI-04, GI-05): `ResearchNotes.fairCopy` (optional, false by default, a four-argument constructor kept)
and `asFairCopy`, the new `ResearchNotesCopyRecipe` and its serializer `jugcraft:research_notes_copy`, the copy's
tooltip line, the ink's tooltip and JEI use sentence, the Research Notes codex page "Fair Copies" and the ink entry's
"Its Use", `recipe/research_notes_copy.json`, `acquisition.py` USES and the `research_notes` workshop row, the
journey's sixth step, extract check and cooperative row, `SPECIAL_RECIPES`, `EXTRACT_CONSUMERS` and `check_fair_copies`;
five `ConcordanceSharingGameTests`, `ConcordanceConclaveGameTests.aFairCopyCreditsItsAuthorOnce`, `WORKSHOP_RECIPES`, and
`ConcordanceGloamInkClientGameTests` (registered); the journey table.

New stable ids: items `jugcraft:gloam_ink`, `jugcraft:gloam_sludge`; extract `jugcraft:gloam_ink`; recipes
`jugcraft:glass_bottle_from_gloam_sludge`, `jugcraft:research_notes_copy`; recipe serializer
`jugcraft:research_notes_copy`; block tag `jugcraft:crucible_lids`; diagnostic `needs_cover`; practice key
`jugcraft:gloam_ink`; operation text `stir <band> covered`; notes field `fair_copy`. No new saved block key, payload or
stamp; Jade's crucible snapshot gains an optional `covered`; the crucible's update tag is unchanged.

Deviations from the contract and its two part specifications, and why:
- **The Peepo port is unchanged.** This change makes no edit under `src/main/java/local/peepo`, so
  `CrucibleCompanion.status` has no arm for `needs_cover` and answers its default, `IDLE`, while a covered formula waits
  for its lid. `aCoveredFormulaWaitsForItsLidAndSaysSo` does not assert the Peepo status (a comment says why),
  `check_gloam_ink` has no Peepo needle, and mutation M20 does not apply. Mapping it to `MACHINE_PAUSED` is one line in
  the Peepo package, which this change was scoped not to edit; it is offered at the checkpoint.
- The two hand-bottling tests hold one stack of three glass bottles through each use: 26.3's `Inventory` skips an
  empty selected slot, so a fresh bottle each time would overwrite the last product in slot 0.
- The sludge's furnace route gives no experience (`"experience": 0.0`), and `check_gloam_ink` refuses any other
  (mutation M38).
- `onlyAnOriginalWithOneBlankAndOneInkIsCopied` also refuses Gloam Sludge for the ink and, with the Concordance switched
  off, any copy; `aLidIsAClosedTrapdoorOrASlabOnTheCrucible` has ten cases (a double slab is a lid).
- `ConcordanceRules` also refuses an extract whose item or failure is an alchemical ingredient, and its extract check
  says the first problem it finds.
- `needs_cover` reads "Its next stir is taken under a lid: close a trapdoor in its lower half, or set a bottom slab, on
  it", and the JEI pages say "a closed lower trapdoor or a bottom slab on the crucible": a top slab or an upper
  trapdoor never counts.
- The lidded client scene clears the block above the lid and checks the campfire gives the crucible 120 degrees before it
  waits for the stir; its player position is read into a typed local first.
- `CrucibleBlockEntity.formula` is as on main: the worked batch's uncovered outcome is the Poison draught, so it records,
  and the part specification's extra branch could never run with the default data.
- `Alchemy.extract` takes `Extract.@Nullable Drawn` (javac refuses a type-use annotation before a qualified name);
  `check_gloam_ink` writes `al.rid` (`check_mod_data.py` has no `rid` of its own).
- `aFairCopyIsCraftedInThePlayersOwnGrid` counts the last ink's bottle where 26.3's `ResultSlot.onTake` leaves it, in the
  ink's emptied grid slot; the two before it go to the inventory.
- `scripts/tests/test_jei_stations.py` lists `research_notes_copy` among the non-processor crafting serializers, as it
  lists `key_copying`.
- `values.json`: `tools/build_alchemy_values.py`, run with a zip of 26.3's vanilla `data/minecraft/recipe` and
  `data/minecraft/tags/item` from the decompiled reference, gives every value as main has it and the two new items at
  its labelled fallback 1 (Gloamcap 36, glass bottle 1, Research Notes 34). It also renames the source recipe of 62
  vanilla items whose value ties (the zip's file order) and moves three entries #537 appended out of order; only the two
  new records were taken from its output, so the file's diff is those two.
- `tools/owner_art.py` (main does not have #526): the 23 October adaptations are added beside the Magic ones; the
  provenance's adapter field becomes `module.function` for both (the same line #526 writes), and `--provenance` names
  each adapter's own tool. Stolas's lines print as before.
- The contract's formula data does not hold a "maximum contamination" or a "container return" (plan 03:44-50): murky
  is the spoon's own, from `concordance/property/contaminant.json` (`Assay.murkyFrom`, 0.25 a part), and the container
  is a glass bottle (`CrucibleBlockEntity`'s bottling), whose return when crafted is the item's crafting remainder.
- The journey's sixth step names its own tests, not `theLivingWorkshopFromAManualStart` (the contract listed it): that
  test walks steps 1 to 5 and brews no ink, sets no lid and copies no sheet.
- Not applied from the part specifications, because this contract replaces them: PC§4.3 (A5), PC§5.1 (A1, A3, PF§5.1
  step 8), PC§5.5 (PF§5.2), PC§5.6 (A6), PC§5.7 step 1 (A10), PC§5.8 step 2 (A9), PF§4.3 (A5), PF§5.1 step 1, step 5's
  second half and step 10 (A1, A2, R3), and the lines of PF§6.1 and PC§6.1 that A7 and A8 replace.

The review's fixes (the fifth commit, after the records):
- `Assay.Look.of` calls `Assay.murky` by its class: inside the record a bare `murky(...)` names the record's own
  accessor `murky()`, and javac refuses it (the review reproduced the error with javac 21), so the main source set did
  not compile.
- **A Crucible Stand reads the lid itself.** GI-P7's existing rule let a stand heat for a covered stir it could not take:
  `Heat.standBand` looks ahead to the next stir whenever the crucible waits for nothing, so with no lid on it heated hot
  through the water and the four adds (about five pulses at 16 JE a tick) until the crucible's own wait stopped it.
  `CrucibleStandBlockEntity.plan` now idles, saying `needs_cover`, while its formula's next stir (`Heat.nextStir`) is
  covered and no lid is on (one block read; `Heat.waitsForHeat` is unchanged), so a lid lifted stops the stand on its
  next tick. `aStandHeatsOnlyForACoveredStirItCanTake` ticks the stand, with JE to spare, after each of those steps.
- **A lid is set by using it on the crucible.** A slab or trapdoor used on the crucible answered `TRY_WITH_EMPTY_HAND`
  and the crucible's empty-hand status took the use, so only a sneaking use placed one. `CrucibleBlock.useItemOn` now
  passes a `#jugcraft:crucible_lids` block item on to its own placing (`InteractionResult.PASS`, on both sides), so
  using one on the crucible's top sets it there as on any block; `aLidIsAClosedTrapdoorOrASlabOnTheCrucible` places a
  stone slab and an oak trapdoor that way. The codex's lid page adds: "A lidded crucible is fed and worked from its
  sides; using a wooden trapdoor lid opens it."
- The economy row counts "120 Spider Eyes"; the recipe serializer `jugcraft:research_notes_copy` is no longer called a
  recipe type (`WHAT_EXISTS.md`, `JugcraftConcordance`, `check_fair_copies`' description); Chunk C's record notes C-D6's
  confirmation; the Peepo deviation, the auto-crafter claim, Gloam's hex and the verification lines say what is so.

Merging with #526 (the garden cohort, not on main when this was built). `tools/owner_art.py` conflicts in five places,
each two additions side by side: the module's description, `expected()`, `provenance()`, `errors()` and the
`--provenance` printing. Keep both sides' loops and lines; in the printing keep #526's built-or-adapted line and add
`OWNER_OCT23_ADAPTED` to its `only` set. One step is not textual: `oct23_adaptations()`'s `taken` gains
`| {target for target, *_ in builds()}`, so a built file and an Oct23 adaptation can never share a path (none does
today). `CHANGELOG.md`, `docs/TESTING.md`, `docs/WHAT_EXISTS.md` and the economy and sharing game tests conflict as
additions too: keep both. Never hand-merge a pasted table (the journey's, the economy's) or generated output: run their
generators on the merged tree and paste.

## Player experience

With the Alembic Arts understood, grow Gloamcap (in the dark beside working magic, faster beside Dewmoss in a mixed bed)
or craft it from a brown mushroom, glow lichen and an amethyst shard. In an Alembic Crucible over a campfire pour a
water bucket (three parts), add three Gloamcap and a spider eye, close a trapdoor in its lower half (or set a bottom
slab) on the crucible and stir hot twice: each glass bottle draws **Gloam Ink**, three in all. Uncovered, the same batch
is the Poison draught it always was. Using a trapdoor or a slab on the crucible's top sets it there; a lidded crucible
is fed and worked from its sides, and using a wooden trapdoor lid opens it. Too many spider eyes, too much Gloamcap for
the water or a searing stir make it murky: the liquid looks murky, any spoon and anyone's risk line say so before a
bottle is spent, and each bottle draws **Gloam Sludge**, which a furnace boils back to its glass bottle. Stirred too
little, it is thin and bottles as the draught. A recorded formula writes its covered stirs; a crucible following it
waits for its lid before each (Jade, the Inspect page and the status line say so; a Peepo at its port shows it idle),
and a Crucible Stand heats nothing until the lid is on.

With a written sheet of Research Notes, a blank sheet and Gloam Ink in any crafting grid (the inventory's two by two is
enough), make a **fair copy**: the same writer, entries and time written, marked a copy; the original and the ink's
bottle come back, and shift-clicking copies while blanks and ink last. A copy is read exactly as its original: notes
count once per writer, so whoever has read one gains nothing from another; the writer learns nothing from a copy of
their own notes; the Starbound Conclave credits the original's writer once a learner; Stolas's Regard takes no notes. A
fair copy says so on its tooltip and is not copied again: copy from the original. Neither Gloam Ink nor Gloam Sludge can
be drunk, poured into a crucible, ground, composted, weighed or formed.

## Connections

- Existing input producer: Gloamcap (a Verdant Bed's crop, faster in a mixed bed; or crafted from a brown mushroom, glow
  lichen and an amethyst shard); the spider eye (spiders, cave spiders, witches); water; a campfire, magma or a Crucible
  Stand; any vanilla trapdoor or slab; glass bottles; Research Notes and blank sheets (paper, an ink sac and a feather).
- Existing output consumer: Research Notes (a fair copy, read by other players as notes evidence keyed by the original's
  writer; the Conclave's teaching renown for that writer); Gloam Sludge's furnace route returns its bottle.
- Technology connection: the Crucible Stand's JE heat and an Electric Pump's water as built; an iron trapdoor on redstone
  automates the lid; a vanilla Crafter ejects the original and the bottle beside the copy (untested).
- Magic connection: the Alembic Arts (a new outcome toward mastery); the Greenwardens' mixed bed; the shared record of notes.
- Reachable entry path: First Light, then the Alembic Arts understood; a crucible, a campfire, a slab or trapdoor, crafted
  Gloamcap, a spider eye; the copy needs no research. No circular unlock (the journey's sixth step checks it).
- Required vs optional: the lid and the carrier are required for ink; stand, couriers and Peepo optional; the whole
  feature is optional for solo progression. Trade: a writer, library or town can hand out copies and keep the original.
- Useful without other branches: yes; a garden only makes Gloamcap cheaper.

## Balance and automation

Contaminant: Gloamcap 0.10, spider eye 0.30, a searing stir 0.50 to the pot; murky from 0.25 a part. Worked formula: 3
parts, 3 Gloamcap, 1 spider eye, 2 hot covered stirs: 1.036 Hollow and 0.200 contaminant a part, three Gloam Ink (the
model). A second eye: 0.300, sludge; one stir: 0.715, the draught. At most three inks a Gloamcap (one Gloamcap, two eyes,
three parts, four hot stirs). One crucible repeating the worked formula: at most 360 Gloam Ink a day for 360 Gloamcap,
120 spider eyes, 360 parts of water and 360 bottles (the economy table's row: the worked formula's day, not a
crucible's most). A crucible's most is about 428 a day (five parts, three Gloamcap, three spider eyes, two hot stirs).
A copy: one ink and one blank sheet; the original and the bottle return. Gloam Sludge's furnace route (smelting, 200
ticks, no experience) gives back only its bottle. Neither item is a resource, ingredient, specimen, mulch, compost,
Scale material or tablet output; no recipe takes the ink; evidence is not multiplied (one notes key per writer per
reader); `tools/concordance_economy.py` reports "gaining cycles: none". Values: Gloam Ink and Gloam Sludge 1 (fallback),
Gloamcap 36, blank sheet 34, glass bottle 1.

## Multiplayer and persistence

The lid, the extract, every bottle and every copy are judged on the server; only someone who may change the crucible
stirs or bottles (Authority); crafting results and remainders come from the server's recipe lookup. Anyone may open a
wooden lid where vanilla allows: a covered formula then pauses and loses nothing (an iron trapdoor avoids it). No new
saved key on blocks: covered stirs live in the mixture's history and the formula's text (stamp 1). `fair_copy` is an
optional notes field written only when true, so a sheet saved before this change reads, and saves, exactly as before.
An older build reads a covered formula as unreadable and keeps it. With the Concordance switched off both items stay
registered; the sludge and copy recipes do not load and the copy is refused.

## Dependencies and assets

No new dependency. The two icons are adapted from the owner's 23 October collection (rows confirmed with
`python3 tools/owner_art.py --provenance alchemy`):

| Runtime file | Library file | SHA-256 of the library file | Change |
|---|---|---|---|
| `textures/item/gloam_ink.png` | `originals/Oct23/Jimtafacts/assets/irons_spellbooks/textures/item/common_ink.png` | `67487dffe22e0ae71699915fb7707583e7017d01e69575bfda7d5ef6d2b4c174` | adapted by `tools/concordance_alchemy_art.py` (`gloam_ink`): the flask's greys 17, 32, 34, 39, 45, 78 and 134 tinted the Gloamcap's violet (#684696, `tools/icon_materials.py` GLOAM) at their own luma; the quill and every other pixel as drawn |
| `textures/item/gloam_sludge.png` | `originals/Oct23/Jimtafacts/assets/irons_spellbooks/textures/item/blood_vial.png` | `814372284e94f2669eb6a5f1c8f03bd9eda1c529b6725613b649f3a37f0d4eea` | adapted by `tools/concordance_alchemy_art.py` (`gloam_sludge`): the reddish pixels tinted Hagmoss (#545e4c, `tools/icon_materials.py` HAGMOSS) at their own luma; the stopper and every other pixel as drawn |

Catalogue rows: `art/owner-library/catalog/edited-2026-10-23-jimtafacts/content.csv`, `irons_spellbooks:common_ink`
("Common Ink", line 602) and `irons_spellbooks:blood_vial` ("Blood Vial", line 590). The `irons_spellbooks` folder is a
label left from the mod the owner used as a base ([PROVENANCE.md](../../art/owner-library/PROVENANCE.md), the owner's
9 October clarification); the same files, unchanged, are Jugcraft's Common Ink and Blood Vial
(`textures/jimtafacts/spellbooks/item/`). Both are 16x16: no size exception. The library's originals are unchanged and
nothing in the library was executed. The owner's art is kept as drawn (CLAUDE.md, "Shared owner asset library"), so
[ITEM_ICONS.md](../ITEM_ICONS.md)'s bottle rules (vanilla's form; dark contents keep a glass wall) are not applied: the
flask's and vial's outlines stay at luma 17 to 19. Both icons are to be shown at the checkpoint.

## Verification

Run locally, on each commit's tree before it was made (nothing compiled; no game, server or client was started):

| Check | Commit 1 (`199114e7e`) | Commit 2 (`f9e200f19`) | Commit 3 (`4a0b2c1d9`) |
|---|---|---|---|
| `python3 tools/owner_art.py --check` | passed | passed: every imported owner file matches its source | passed |
| `python3 -B tools/check_mod_data.py` | passed (8,038 material IDs) | passed (8,040) on its second run; the first stopped at a `NameError` in `check_gloam_ink` (`rid`), fixed | passed (8,040) |
| `python3 scripts/check_repository.py` | passed | passed | passed |
| `python3 tools/concordance_journey.py --check` | passed | passed | passed: every route holds |
| `python3 tools/concordance_economy.py` | gaining cycles: none | gaining cycles: none, with the Gloam Ink row | gaining cycles: none |
| `python3 tools/check_icon_maps.py` | passed | passed | passed |
| `node --test tools/world-designer/model.test.cjs` | passed | passed | passed |
| `python3 -m unittest discover -s scripts/tests` | 287 passed | 287 passed | 287 passed on its second run; the first failed `test_jei_stations` (the new special recipe), fixed |
| `git diff --check` | clean | clean | clean |

The records commit ran the same nine checks on its tree before this paragraph was added, and each passed (8,040
material IDs; 287 tests; every route holds; gaining cycles: none); `check_repository.py` and `git diff --check` ran
again after it and passed.

The review's fixes ran the same nine checks on their tree before this paragraph was added, and each passed (8,040
material IDs; 287 tests; every route holds; gaining cycles: none; the World Designer's 12); `check_repository.py` and
`git diff --check` ran again after it and passed. `tools/generate_material_data.py` ran twice: the first run changed
only the lid page's line in `en_us.json` within the change, the second nothing. A scratch javac 21 compile of the pure
packages `concordance/alchemy`, `concordance/diagnostic` and `concordance/rules` (with gson and jspecify only; not the
build) succeeds, and fails on `Assay` as it was. Mutations, on a scratch copy: F1 to F7, each caught (the stand's lid
read removed or inverted; the lid's `PASS` removed or turned back to `TRY_WITH_EMPTY_HAND`; the economy's plural undone
without pasting; the walk test put back in step 6 without pasting; the stand test renamed); M1 to M38 again, each
caught except M20, which does not apply (M3 regenerated); N1 to N26 again, each caught; the contract link, caught.

`tools/generate_material_data.py` ran twice for each build commit and changed nothing the second time; its rewrites of
files outside the change (line ends only, `jug-nouveau-content-map.json` reordered, two ornamental corn PNGs with
identical pixels from `tools/owner_art.py`) were restored and not committed.

Audit mutations, each applied to a scratch copy of the tree, checked through `tools/check_mod_data.py`'s own check
functions (`check_gloam_ink`, `check_fair_copies`, `check_signs`, `check_economy`, `check_diagnostics`, `check_recipes`,
`check_acquisition`, `check_journey`, `check_game_test_entrypoints`, `check_concordance` for the codex link,
`owner_art.errors`), with `tools/owner_art.py --check` or the values unit test where named, then restored:
- The contract: breaking the new link (`gloam-ink.md` to `gloam_ink.md`) in Chunk C's record: `check_repository` reports
  "Broken link in docs/features/concordance-chunk-c.md: gloam_ink.md".
- Commit 2, M1 to M38: each caught (M15 and M17 with different wording than expected) except M20, which does not apply
  (the Peepo port is unchanged).
  M3 (least 1.1, regenerated) reports only "the worked formula draws its item, ... (('thin', 'failure', 'thin', None,
  None))". M15's report lists what bone meal reaches beyond a glass bottle's own reach, and M17's lists
  `['jugcraft:gloamcap', 'jugcraft:habitat_gauge']`. M24 was run as three (the route, a registry id, a values entry,
  the last also failing `test_all_authored_item_types_have_explicit_records`). M38 (experience 0.1) reports "its one
  mundane route is a furnace giving back exactly its glass bottle".
- Commit 3, N1 to N26: every one reported as expected; N15 and N16 also trip `check_recipes`, N20 and N21 also
  `check_journey`, N24 also every workshop step's "not in VANILLA_SOURCES".

Not run: `./gradlew build` (compilation and the server game tests) and `./gradlew runClientGameTest` (the three shots
`jugcraft_gloam_ink_fair_copy`, `jugcraft_gloam_ink_fair_copy_scale4`, `jugcraft_gloam_ink_lidded_crucible`): CI has
not run on this branch. Two-client play: not run. Play by hand: not run. The model's numbers are the generator's until a
game test holds them.

| Feature | Tier | Input producer | Output consumer | Costs | Unlocks | Failure behaviour | Test evidence |
|---|---|---|---|---|---|---|---|
| Gloam Ink, covered extraction (C-D6) | Practitioner | Gloamcap (mixed bed or crafted), a spider eye, water, glass bottles, heat, a lid | the fair copy | 3 Gloamcap, 1 spider eye, 3 parts of water, 3 bottles for 3 inks; 2 hot stirs | nothing | murky: Gloam Sludge (a furnace returns its bottle, no experience); thin or uncovered: the draught; no lid: a formula waits, nothing lost | `ConcordanceGloamInkGameTests`, `gloamInkAndSludgeAreNeverFormed`, `extractsTurnIntoNothingTheyCameFrom` (not run until CI) |
| Fair copy of Research Notes (C-D6) | Practitioner (the ink); none (the copy) | Gloam Ink, a written original, a blank sheet | other players' research (notes evidence keyed by the original's writer); the Conclave's teaching renown | 1 ink and 1 blank a copy; original and bottle return | nothing | a copy, a missing or extra ink, blank or sheet, sludge or anything else makes nothing; switched off, nothing; a copy teaches nothing its original did not; Stolas's Regard takes nothing | the five `ConcordanceSharingGameTests`, `aFairCopyCreditsItsAuthorOnce`, `ConcordanceGloamInkClientGameTests` (not run until CI) |

## World and event applicability

Not seasonal; any dimension with a crucible. No creature, boss, loot or structure change.

## Rollout and open questions

Proposed until the checkpoint. Questions: (1) is the spider eye the approved carrier? (2) is a lid on the crucible the
right "covered extraction", including that a lid already set on a crucible now covers hand stirs, and that a formula
recorded under a lid waits for a lid wherever it is set, whatever it brews (record it without one to run it lidless)?
(3) is a fair copy of
Research Notes the use you want, with only originals copied, or should Gloam Ink serve ward sigils or links first? A
writer gains nothing by copying their own notes: a fresh original costs one blank sheet, a copy a blank sheet and an
ink, so Gloam Ink's use is for someone who holds another player's notes, such as a library or a town.
Known limits: a Peepo shows a covered formula's wait as idle and counts no ink as stock (both in the Peepo package); a
formula recorded under a lid waits for a lid wherever it is set, whatever it brews (record it without one to run it
lidless); both items at the values fallback; a copy looks like
its original apart from its tooltip; special recipes are not in the recipe book or JEI's crafting category (the
tooltip, the codex and the JEI page carry the copy).
