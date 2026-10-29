# Amdusias's pact (garden cohort, GC-09)

Concordance garden cohort, GC-09 ([contract](concordance-garden-cohort.md#gc-09-amdusias-unit-6); Dossier 6). Amdusias,
the sixty-seventh spirit, bends trees: standing beside his Pact Perch, he bends the shelter of a tree beside it over the
crops of his holder and their party, so the workshop's stir (the area's disturbance) reaches up to 16 of their beds up
to 2 lower, never below nothing. He is the cohort's sixth garden spirit (Bifrons, the fifth, is built beside him in unit
5), on the same pact record, perch, screen, roster, journal and Jade lines as Stolas, Buer, Bathin, Marax and Foras. His
service changes no block, item or saved value: a crop under his canopy only reads the stir lower, from a registry held
in memory that lapses when he stops.

Status: brief only (GC-09a), committed before any of his body files (D-D5), as the contract's per-spirit template asks.
Nothing of Amdusias is built yet: no entity, pact definition, service, canopy, sound event, lang, body file, audit or
test. This page holds his character (the contract's Dossier 6), the short words the magic screens are to draw for him
and the owner files his body and cues are to use; the runtime files and builders it names are what GC-09 is to build.
Where it goes beyond Dossier 6 (the amended greeting and its stage direction, the canopy's rule and registry, how his
body is built, which cue plays which file at what pitch, the short words the screens draw) it is unit 6's proposal
(GC-P120 to GC-P139, added to the contract's Decisions when the unit lands), and the palette files may change only here,
before any body file (the contract's art plan). Four things came after the unit's design and are followed here
([where this brief departs from it](#where-this-brief-departs-from-the-units-design)): his station is unit 3's (GC-P67,
in the shared prefix GC-06b), with no station code of his own; his words keep to the magic interfaces' limits (GC-S26);
the owner-art root he reads his garland through is already in the base (GC-06b, first read by Foras's acorn cap); and
unit 3's `HabitatAdvice` is in the base, so the one-line edit that keeps a canopy's shelter in Marax's advice
(GC-P137), which the design left to the merge, lands in this unit's own commits. Where these change his specification,
the choices are the session's; none is the owner's. The rest of the record (player experience, record fields as built,
connections, the canopy as built, failure behaviour, the pact screen, his matter, saved data, verification) is added
with the code.
Proposal issue: none; the owner's selected scope ([the cohort's contract](concordance-garden-cohort.md), GC-D1 to GC-D3).
Owner: @jimbozoomer-byte. AI-assisted implementation with Claude Code; the model is named in the commit trailers.

## Amdusias's character

The sixty-seventh spirit (1904 heading "Amdusias"; other spelling Amdukias). Source phrases [S]: "The Sixty-seventh Spirit
is Amdusias, or Amdukias. He is a Duke Great and Strong, appearing at first like a Unicorn"; at request a human shape
[s]; "Also he can cause Trees to bend and incline according to the Exorcist's Will."; "He giveth Excellent Familiars."
**The identity rests on "cause Trees to bend and incline"** (Jugcraft adaptation: a living windbreak; the only
tree-shaping power among the 72 by the surveyor's recollection [U], to be confirmed against a scan). The music line is
unverified and its readings disagree ([U], [s]): it is not quoted, and nothing in game attributes his horn to the source.
No source quotation in game (GC-S14).

- **Silhouette:** a tall, slender white unicorn with a long neck and long fine legs, whose horn is a living oak branch
  with three leaves, wearing the owner's flower crown as a garland of small blooms round his neck. Built geometry, drawn
  fresh (no vanilla horse model copied): 0.75 blocks wide, 2.41 to his horn's top leaf and 1.56 long at scale 1; his box
  1.6 by 2.4, his eyes at 1.9 (GC-P132, GC-P133). Not imported: thunder; any game design ("Amdusias' Screamer").
- **His height (decided by the session):** the contract's "about 2.0 blocks" and its line-up ("Foras 2.2, Amdusias 2.0,
  ...") are amended to 2.4, the tallest of the six (the unit critique's item 13, option (a), which the cross-unit merge
  notes assume). His box stays at least a fifth from every other spirit's on one axis (`check_spirit_bodies`): Foras's
  1.2 x 2.2 and Buer's 1.2 x 1.6 by 25% in width, Marax's 1.6 x 1.4 by 42% in height, Bathin's 0.9 x 1.9 by 44% and
  Stolas's 0.7 x 1.45 by 56% in width, and the workshop hand's 0.6 x 1.6 by 63%; Bifrons's (unit 5) is held at the merge.
- **Station:** unit 3's standing station (`PactSpiritEntity.Station.BESIDE`, GC-P67, in the shared prefix GC-06b), with
  no station code of his own (his `BODY` names `Station.BESIDE`): on the ground two blocks out from the perch, on the
  first clear side a quarter turn clockwise from whoever he comes for, then the opposite side, then counter-clockwise,
  their side last; a side is clear when the cells out to it are loaded and they, and the cells over them up to his height
  (three for his 2.4), have no collision shape, and the outer cell's floor is sturdy; with no side clear, the perch's top.
  His tree may stand in the cells out on a side; that side is then not clear, and he takes the next. He keeps the side
  he stands on while it stays clear and turns to face whoever speaks to him (GC-02). His own test,
  `amdusiasStandsBesideHisPerch`, is the evidence for him (unit 3 has no generic station test).
- **Personality:** courtly, remote; answers as if listening to something far off (a Jugcraft adaptation drawn from the
  unverified music line, to be revised if the scan disagrees).
- **Beats** (GC-P120, held to the magic interfaces' limits, GC-S26: at most 18 words a line, a stage direction of at most
  8): stage direction "A garlanded white unicorn bows his leafy horn." (8 words; the design's was 29); greeting "You see
  me before you hear me. I am Amdusias." (10; amended from the contract's "You hear me before you see me", since his horn
  now sounds a beat after his gesture, so you see him first; a Jugcraft adaptation quoting nothing); service "A tree
  beside this perch, and I bend its shelter over your beds; the stir reaches them softer." (18: Dossier 6's line, 23
  words, shortened); price "A sapling each week, so there is always a tree to bend." (12, Dossier 6's). With his name, his
  service's name, Terms and Leave, his first and second beats would each draw 23 words of the beat page's 30 (counted as
  Foras's first beat draws 28).
- **Invitation** (Dossier 6 item 2): The Garden's Regard understood; a log within 2 blocks of the perch (any block in
  `#minecraft:logs`: his tree); an awake Verdant Bed the inviter keeps within 6 blocks. Each missing fixture is named
  before anything is spent: no tree, "I bend trees. There is no tree here." (Dossier 6's); no bed, "There is nothing here
  to shelter." (the design's).
- **Service: the bending canopy** (`canopy_shelter`; Dossier 6 item 3; GC-P122 to GC-P125, GC-P128, GC-P129):
  - At any hour, while his pact serves, his holder is near (the common terms: within 64 blocks of the perch) and his tree
    stands, his canopy covers the awake beds of his holder and their party in his area (radius 6), the nearest first and
    at most 16 at once, each asked through the garden's shared check (`GardenPacts.target`: the pact serves there, the
    bed's keeper is a beneficiary, the holder is near and could change it by hand). His tree is the nearest log within 2
    blocks on every axis, remembered and read again once a think, looked for again at once when it falls, otherwise at
    most every 200 ticks.
  - Over a covered bed, the plant above it reads the area's disturbance up to 2 lower, never below 0: a crop on it, or a
    Habitat Gauge attuned on it, which reads what that crop would (so a Gauge's bed counts toward his 16 and toward
    "Sheltering n beds"). Never a Verdant Heart (the block above the bed decides), never more stir, never harvest strain
    eased (the strained judgement keeps the unsheltered value: `Habitat.strained()` adds back the stir he took), never
    two canopies added (the larger damping applies).
  - A crop whose niche wants the stir (its ideal disturbance starts above 0: today only Gloamcap) he spares until
    answered (his matter, below).
  - The rule is pure, in `ecology` (`Habitat`'s seventh component `shelter`; `Habitat.sheltered`); the canopy is held
    only in a per-level registry (`garden/Canopies`: memory only, one a holder, refreshed each think, lapsing 40 ticks
    after the last) and read where a bed builds its reading, so growth, the Inspect page, the Gauge, the bed's and the
    crop's reports and Jade all read the same sheltered value and say so ("Sheltered by Amdusias"). The bed's saved and
    synced note never carries that line (it would outlive the canopy), so a crop's own tooltip, drawn from that note,
    does not name him.
  - It changes a growth step's chance, never its cost or output (as the mixed bed does); nothing is made, moved or spent
    by his service. It is not the plan's season shelter structure (GC-S21).
- **Upkeep** (Dossier 6 item 4; GC-P121): his compact a note block and 2 oak saplings, resonance 0; then a sapling every
  7 game days from `#jugcraft:amdusias_offerings`, one day's grace, up to 4 paid ahead. The tag names oak, birch and
  spruce saplings directly (the contract's `#minecraft:saplings` amended: its nested tags are ones this line's journey and
  acquisition audits cannot follow, which is true only before unit 5, whose line makes nested tags followable). Terms:
  radius 6, any hour, the quota 16 as a cap (beds sheltered at once, never a daily count: his record's count never
  rises), `party`.
- **His matter: the stir a Gloamcap wants** (Dossier 6 item 5; GC-P130): his canopy slows a crop whose niche wants the
  stir, so he spares it and asks: "This one wants the stir I take away. Shall I spare it?" The answers are **Spare them**
  (`spare`, the safe course and the default, also how an unanswered or unknown value reads) and **Shelter all** (`all`),
  kept as the perch's `accord`, append only.
- **Refusal and departure** (Dossier 6 item 6): refusal "I bend trees. There is no tree here."; departure: he bows his
  horn, the tree beside the perch sways once, he is gone (scaled to nothing by 1.0 s of his 2.0 s departure), and a beat
  later a distant horn, subtitled.
- **Details** (Dossier 6 item 7):
  - (a) A horn heard a beat late: his arrive, work and depart cues (the horn) sound 20 ticks after their gesture, never
    with it, from where he stands (a Jugcraft adaptation of the unverified music line, never attributed to the source).
    The beat (20 ticks) stays under his departure (40), so the departing horn sounds before he is removed; a horn still
    held when the Concordance is switched off is dropped unsounded, but the departure's.
  - (b) His tree sways: every 10 s while he shelters, and once as he leaves, at the log's top. No spirit or service sends
    particles of its own (`check_signs`), so the sway is the shared sign system's (`Signs.show` with an existing `Sign`,
    as Bathin's ring of dust is `Sign.SHOVE`); none falls like leaves, so it is drawn in the nearest look the system has,
    chosen with the code (proposed: `Sign.MEND`'s six green motes at full intensity). The design's six tinted leaves sent
    by the server (`ParticleTypes.TINTED_LEAVES` through `sendParticles`) are not used.
- **Not imported:** the music line, as a quotation or an attribution; thunder; "Exorcist" framing, any seal, conjuration,
  title or legion count; the human shape; the "Excellent Familiars" (another spirit's, Marax's, identity); the
  identities of the creatures whose runtime sounds his cues reuse under events of his own; any modern adaptation's design.

### What he connects to (the record's fields, planned)

- **Tier:** Practitioner stage, Spiritbinders; The Garden's Regard understood (no other research).
- **Input producer:** a log beside the perch (his tree) and an awake Verdant Bed of yours; the workshop's own stir (a
  pylon, crucible or anchor near the beds) is what he softens; his compact and offerings are vanilla (a note block,
  saplings), each with a reachable route (`VANILLA_SOURCES` gains the three saplings and the note block).
- **Output consumer:** the crops on his holder's and their party's beds in his area: the disturbance term of their fit,
  hence a growth step's chance; the Gauge, the Inspect page, the reports and Jade read it.
- **Costs:** the compact once; a sapling a week. Nothing else is spent, and his canopy never mints or moves anything.
- **Unlocks:** nothing of its own; his first obligation (harvest a ripe crop by hand in his area while the pact is
  active, `PactObligations.Act.HARVEST`) counts toward mastering The Garden's Regard as each garden spirit's does.
- **Failure behaviour:** no tree, he waits with his canopy lifted ("No tree"), whatever crop he was sparing; his holder
  away or more than 64 blocks off, the pact suspended or held for upkeep, ended, or its perch broken: his canopy lapses
  within 40 ticks (two thinks); a stranger's bed is never covered; a Heart is never sheltered; strain is never eased; a
  second canopy over a bed is never added; after a restart his canopy returns within one think (it was never saved).
- **Saved data:** only the base's (`perch`, `record`); his work id `canopy_shelter` and accord values `spare` and `all`
  append only; the canopy, the late horn's hold and his memory never saved or sent.

### His words on the magic interfaces (GC-S26)

The unit's design predates the magic interfaces. As they require, each of his server sentences stays whole as the
message, narration and hover of what draws it, and the screens draw short glances: his work's row in `tools/magic_ui.py`
`WORK_GLANCES` ("Canopy shelter"), its icon's line under his marker in `PactGlance.WORK_ICONS`, and short forms and
marks for his Jade lines (`SENTENCES`, `SENTENCE_MARKS`, with `MagicGlance.MARKS` held equal). A new spirit adds data
only, no code path, to the restyled screens. Proposed, and fixed with the code where `check_magic_ui` holds them (each
measured here with that module's own word and width counts):

| What | Drawn | Words |
|---|---|---|
| The header's service line (its icon `minecraft:oak_leaves`, the canopy) | "Shelters your crops from the stir" | 6 (175 px of the header's 211) |
| The quota as a cap: the quota chip's glance for `term.quota.canopy_shelter` (the server's "At most %s beds sheltered at once", its message) | "%1$s at once" ("16 at once") | 3 |
| The kept page's gauge (his `row.service`: beds covered of 16) | "%1$s/%2$s now" ("3/16 now"); see below | 2 |
| Blockers (no tree; no bed), each a lock line | "Needs a log within %1$s blocks of the perch"; "Needs your awake Verdant Bed within %1$s blocks" | 9; 8 |
| Chooser card reasons (no tree; no bed) | "Tree needed"; "Bed needed" | 2; 2 |
| Kept-page rows (no tree, marked waiting with a log; spared, marked asking with a Gloamcap) | "No tree: set a log within %1$s blocks"; "Spared: %1$s crops that want the stir" | 8; 7 |
| His matter (how many, and the nearest) | "Want the stir: %1$s, nearest %2$s. Spare them?" | 10 (with a position; two lines at the narrowest scroll) |
| His question in general (answered, and no matter raised now) | "Shelter crops that want the stir?" | 6 |
| His answers (their own words) | "Spare them" (`spare`); "Shelter all" (`all`) | 2; 2 |
| His first task's row | "First task: harvest a ripe crop by hand here" | 9: no act glance needed |
| Jade on him (his notes and matter) | "Sheltering %1$s beds" (covered), "Spared: %1$s", "No tree", "Canopy elsewhere", "Nothing to shelter"; "Crops want the stir" (his matter) | at most 4 |
| The readers' line, short: on a bed's and a Gauge's Jade (`jade.sheltered`), and for the Inspect page's and the reports' sentence (`ecology.sheltered`) | "Sheltered by Amdusias: -%1$s" ("Sheltered by Amdusias: -2") | 4 |

Two of these depart from the unit's design and one from the shared rule; GC-09 settles each with the code:

- **The quota's words.** The design gave the terms page a label ("Beds at once") beside the quota's sentence and a
  `PactScreen` change to draw it. The restyled terms page draws chips with no labels (the style's 1.1), so the server
  still names its own quota sentence (`PactService.quotaTerm`, GC-P126) and the chip draws its glance; no screen code
  changes, and the label key is not added.
- **The gauge's word.** `check_magic_ui` holds every work's `row.service` glance to "%1$s/%2$s today", the day's
  quota. His counts beds sheltered now, and "3/16 today" would say the daily count his terms deny (the contract: "never
  as a daily count"). Proposed: "%1$s/%2$s now", with that rule taking "now" for a work whose quota is a cap
  (`QUOTA_TERMS`, a table, so still data). If the rule is kept as it stands, the gauge reads "3/16 today" and this
  page records the contradiction.
- **The service beat** is Dossier 6's line shortened from 23 words to 18; the greeting and price are Dossier 6's (the
  greeting amended as above); the stage direction is 8 words in place of the design's 29.

### Evidence plan

Nothing below has run. Each is what GC-09 is to build and run, in CI only (no Minecraft here):

- **Server tests** (25): in the garden class, `aShelteredHabitatKeepsItsStrainedValue` and
  `theCanopyRegistryTakesTheLargestNeverTheSumAndLapses`; in the worker class, `aCanopyLowersOnlyDisturbanceForCropsInItsArea`,
  `aCanopyNeverSuppliesDisturbance`, `aCanopyNeverHelpsAHeart`, `aCanopyNeverEasesStrain`, `twoCanopiesNeverStack`,
  `aCanopySparesAGloamcapUntilAnswered`, `aCanopyLapsesWithin40TicksOfSuspension` (and of the tree's felling, the holder
  100 blocks away and the perch's breaking), `aCanopyLapsesWhenItsHolderLeaves`, `aCanopyCoversAtMost16Beds`,
  `aStrangersCropIsNeverSheltered`, `amdusiasRefusesWithoutATree`, `amdusiassFirstObligationIsAHarvestUnderHisCanopy`,
  `theInspectPageAndTheGaugeReadTheShelteredValue`, `theCanopysQuotaIsWordedAsACap`,
  `aCanopyReturnsAfterARestartWithinOneThink`, `amdusiasHornsABeatLate`, `hisTreeSwaysWhileHeShelters`,
  `aHolderKeepsOneCanopy`, `amdusiassReadingSaysItsNoteAndNamesNoPlayer`, `amdusiasAnswersEachActionWithHisGesture`,
  `aDuplicateAmdusiasSheltersNothing`, `amdusiasStandsBesideHisPerch`; in the economy class,
  `theEconomyStillHasNoGainingCycle`; and the shared SG12 cases through his fixture. Tests that compare readings sample
  through the shared `afterSampling` and think at the level's own time; each asserts only its own holders and beds.
- **The pure rule's harness** (scratch, not in Git): every shipped niche over a grid of the other factors, every
  disturbance from 0 to 15, strain from 0 to 3 and two dampings from 0 to 4, against the base classes. Without a canopy
  every verdict, growth, fit and reason line is unchanged; only disturbance is lowered, by at most the damping, never
  below 0; the strained value is unchanged; two canopies give the larger, never the sum; a crop not wanting the stir is
  never slower. And unit 3's advice harness on `HabitatAdvice` with a shelter dimension: which value Marax's disturbance
  advice names on a sheltered reading is decided with that edit (GC-09b) and recorded there.
- **Client steps** (the garden pact class, after Foras's flow in `runTest`, calling the shared yard first, at noon in
  clear weather): the refusal without a tree; the chooser with his card whole; his beats at three sizes; his terms with
  the quota chip; a refused and a sealed acceptance; the late horn (its subtitle real where sound plays, otherwise staged
  on the HUD's subtitle overlay and called staged, the server holding the beat); the Inspect page; the bed's and the
  Gauge's Jade; the sway; his matter asked and answered at three sizes; Jade on him; his ten poses; four bearings (with a
  face entry: his muzzle reaches 16.5 px ahead of him); the ending. 25 flow shots and 10 body shots, each word budget
  enforced (beats 30, terms 50, kept page 45, roster 70, the chooser's `chooserWords`).
- **Audits:** GC-09 blocks in `check_ecology`, `check_pacts`, `check_garden_pacts` and `check_spirit_bodies`, and his
  rows in `check_magic_ui`; each commit's audit mutations run on a scratch copy and reported.

### Where this brief departs from the unit's design

Each is decided by the session (none is the owner's), for the reason given:

- **Station:** unit 3's, already in the base; the design's copy of it (its commit GC-09c, its audit needles and their
  mutation) is dropped, and his own test is his station evidence.
- **Words:** his beats and glances as above (GC-S26); the quota's label dropped; the gauge's word proposed.
- **Height:** 2.4, amending the contract's 2.0 and its line-up.
- **Readers:** the bed's saved note does not carry the canopy's line (so nothing outlives the canopy); the crop's own
  report adds it; Jade's bed and Gauge lines read the server's live value.
- **The sway** goes through the shared sign system.
- **`HabitatAdvice`** (unit 3's, in the base): its strain branch keeps a canopy's shelter (`habitat.withStrain(value)`,
  GC-P137) in this unit's GC-09b, not at a merge.
- **From the unit's critique:** a stopped Amdusias reports why he stopped, not an old question (a felled tree reads "No
  tree" even with a crop spared); the holder leaving is tested; a Gauge's bed counts toward his 16; the offering tag's
  reason holds only before unit 5.

## Dependencies and assets

No new dependency, pin or mixin. GeckoLib 5.5.7 draws his body (approved, `distribution/frameworks.lock.json`); Jade is
optional, through the existing adapters only. His body is built by `tools/garden_bodies.py` (no owner rig is a unicorn):
geometry and clips are code, the owner's flower crown copied into the geometry as his garland; his 128 x 64 skin is
painted only in the colours of three of the owner's paintings, read as data and never copied or edited, around the
crown's texels, which are copied unchanged. He has no glowmask.

| Painting (under `art/owner-library/originals/Blocks/`) | Catalog row | SHA-256 | Colours | Used for |
|---|---|---|---|---|
| `biomes and tree blocks/white_petals.png` | `files.csv:2151` | `20b88497e849470631ac740a8e646c365784063d0c74762a85853be9123c3fc0` | 12 (9 drawn) | coat, mane, tail, muzzle, ears, his mouth's line, his eyes' glint |
| `biomes and tree blocks/origin_oak_log.png` | `files.csv:1976` | `c147b6cdc087ce197f4dfd422595d8216ba131efef2ed0f3727014cd033b7969` | 5 (all drawn) | hooves, his horn's branch (stem and twig), his eyes |
| `biomes and tree blocks/origin_oak_leaves.png` | `files.csv:1975` | `43462aeeba55c863de64696194a8ba10c2b5ae75d5c2cbf229a1c8c7c46b1508` | 5 (2 drawn) | his horn's three leaves (added by the unit's design: the art plan named the crown, the petals and the log) |

His garland is the owner's flower crown, from the library's second edited part, read through `tools/owner_art.py`'s
second root (`PART2_NAME`, `_read_owner`, `_origin`: in the shared prefix GC-06b, already read by Foras's acorn cap; not
added again):

| Owner file (under `art/owner-library/originals/Edited-2026-10-18-Part2/`) | Catalog entry (`art/owner-library/catalog/edited-2026-10-18-part2/files.json`) | Bytes | SHA-256 | Change |
|---|---|---|---|---|
| `cnb/geo/armor/flower_crown.geo.json` | `files[19]` | 4,918 | `c3cfaa055b62910d96e009c510ad50e4c5e4be6e2952258351b7dbeb2dff47fe` | its one bone's twelve cubes (each flat, one axis 0, with per-face UVs on a 48 x 48 sheet, written by the owner as decimals) copied as his `garland` bone under his neck, each moved by (0, -6, -6.5), from the ring's middle (0, 30.5, 0) to his neck's (0, 24.5, -6.5); their sizes, UVs and inflate the owner's; of each cube's two faces on its plane one dropped (eleven exact reversed twins of the other, and cube 9's clear west face), the twelve pinned in his row's `dropped`; nothing else of the file used |
| `cnb/textures/armor/flower_crown.png` | `files[177]` | 544 | `61b8e6fa10914bddc7f091d7fca62f488230b488b2d4c35648a8ef7e10b1c1c4` | 48 x 48; its 102 drawn texels (11 colours, x 1 to 30, y 19 to 32) lie inside the rectangle at (0, 18), 32 x 17, which is copied texel for texel to the same place in his sheet, so the cubes keep the owner's UVs and none of his own parts draws there. The same file is byte for byte the Grove Flower Crown's runtime armour texture (`textures/menagerie/grove/armor/flower_crown.png`, `grove-runtime.json` `copies[0]`), and that wearable's runtime geometry holds the same twelve cubes; his files read the library originals, so a later change to the wearable does not change him |

His cues play five runtime sounds already in the repository, copied unchanged from the owner's library and already
played under other events (`pocket_horn_fade`, `cavern.tephra_whistle`, `wildlife_item.whistle`; the art plan's far horn
and whistles): no new file is copied. No voice.

| Runtime file (`assets/jugcraft/sounds/menagerie/`) | Library source (`art/owner-library/originals/`) | Catalog (`art/owner-library/catalog/`) | SHA-256 | Header | Cue (subtitle) |
|---|---|---|---|---|---|
| `pocket/horn_fade.ogg` | `Edited-2026-10-18/jimbos version of inventory pets/inventorypets/sounds/horn_fade.ogg` | `edited-2026-10-18/files.json` `files[446]`; `pocket-adaptation.json` `runtimeFiles[263]` | `338156be398681668d5f5685283a9e580511d6b2fba143c751f60f9e3dcdfc6e` | mono, 44.1 kHz, 1.07 s | arrive ("A distant horn"), work ("A distant horn answers"), each 20 ticks after its gesture; depart at 0.9 ("A distant horn fades", 1.18 s), 20 ticks after his departing gesture |
| `wildlife/item/whistle/whistle.ogg` | `Edited-2026-10-18/jimbos_heavily modified naturalist animals/sounds/item/whistle/whistle.ogg` | `edited-2026-10-18/files.json` `files[1855]`; `runtime-files.json` `[544]` | `f364ff7ca0aeab3f8f6e4a28f9f744cd0b4df1deb13af79af7e3f47a159b3737` | mono, 44.1 kHz, 0.72 s | listen at 1.3 ("Amdusias whistles softly"); assent at 1.1 ("Amdusias whistles assent"); refuse at 0.7 ("Amdusias whistles low") |
| `cavern/misc/tephra/tephra_whistle_0.ogg` | `Edited-2026-10-18-Part2/jims caves/sounds/misc/tephra/tephra_whistle_0.ogg` | `edited-2026-10-18-part2/files.json` `files[19396]`; `cavern-runtime.json` `copies[2018]` | `3d35aceb09c1f28b052d4919a25381e5111fc0c70bb1c98bf590adc476d1941c` | mono, 96 kHz, 5.61 s | inspect ("A long, low whistle"), one of three |
| `cavern/misc/tephra/tephra_whistle_1.ogg` | `Edited-2026-10-18-Part2/jims caves/sounds/misc/tephra/tephra_whistle_1.ogg` | `files[19397]`; `copies[2019]` | `38bdcc2666ac3f4fa1dfa1b1af696ba6ceeb1672e0ac478ea34cd7d87111cfea` | mono, 96 kHz, 6.50 s | inspect, one of three |
| `cavern/misc/tephra/tephra_whistle_2.ogg` | `Edited-2026-10-18-Part2/jims caves/sounds/misc/tephra/tephra_whistle_2.ogg` | `files[19398]`; `copies[2020]` | `00954d7664c8123e81b74a1ba9322f6573ce9dc5d585f4d8f9f066aba7c48e87` | mono, 96 kHz, 5.19 s | inspect, one of three |

The library's notices are kept with the originals; provenance as `art/owner-library/PROVENANCE.md` records it. The
library is unchanged and nothing in it is executed.

## Verification

Not run: nothing of Amdusias is built yet, so no build, game test, client shot or audit exercises him, and nothing of
unit 6 has run in CI. The line this brief starts from (`4201217b3`, with Foras) is in CI with no result recorded here;
the newest result it descends from is run 40840353835, green in full on `3bfd63a97`.

For this brief only:

- Each SHA-256 above was computed from the library file (and, for the sounds, from the runtime copy too), and each
  equals its catalog entry as the catalog stands now (`files.csv` by line, the header line 1; the parts' `files.json`
  and runtime catalogs by index from 0).
- The three paintings' colours were counted (all 16 x 16: the white petals' twelve opaque around 153 clear texels, the
  origin oak log's five, all opaque, and the origin oak leaves' five opaque around 101 clear).
- The crown's texture is 48 x 48 with its 102 drawn texels where the table says; its geometry has twelve cubes on one
  bone. The Grove Flower Crown's runtime texture has the same SHA-256, and its runtime geometry's cubes equal the
  library's.
- The five sound files' Vorbis identification headers and last granule positions were read (one channel each, at the
  rates and lengths above).
- The words proposed above were counted with `tools/magic_ui.py`'s own `nwords`, `width` and `lines`.
- The unit's body harness (scratch, not in Git: its `amdusias_body.py`, SHA-256
  `7e9c7c13e586bdd94cc95b6bd81a98daa33c044dc7e86879cec97d6ae023f3c8`, run in memory against this line's library files,
  nothing written) was re-run. It found no coplanar faces, no overlapping UV regions, and no skin texel outside the three
  paintings' colours or the crown's rectangle. The twelve dropped faces match the pins, every clip's length and telling
  window matches his row, and his extent is 0.75 x 2.41 x 1.56 blocks. The colour counts drawn ("9 drawn", "2 drawn")
  are the harness's.

The tests, shots and runs that verify him are listed here when GC-09 lands.
