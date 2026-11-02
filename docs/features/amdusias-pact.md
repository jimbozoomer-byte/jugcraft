# Amdusias's pact (garden cohort, GC-09)

Concordance garden cohort, GC-09 ([contract](concordance-garden-cohort.md#gc-09-amdusias-unit-6); Dossier 6). Amdusias,
the sixty-seventh spirit, bends trees: standing beside his Pact Perch, he bends the shelter of a tree beside it over the
crops of his holder and their party, so the workshop's stir (the area's disturbance) reaches up to 16 of their beds up
to 2 lower, never below nothing. He is the cohort's sixth garden spirit (Bifrons, the fifth, is built beside him in unit
5), on the same pact record, perch, screen, roster, journal and Jade lines as Stolas, Buer, Bathin, Marax and Foras. His
service changes no block, item or saved value: a crop under his canopy only reads the stir lower, from a registry held
in memory that lapses when he stops.

Status: **built and recorded on unit 6's own line, not yet compiled or run in CI, not merged.** His brief `23236575e`
(GC-09a), committed before any of his body files (D-D5), as the contract's per-spirit template asks; the canopy's pure
rule and the garden's memory of canopies `929337b5c` (GC-09b, [below](#the-canopys-shelter-gc-09b)) and a cue a beat late
and a quota worded as a cap `916b624d4` (GC-09d, [below](#a-cue-a-beat-late-and-a-quota-worded-as-a-cap-gc-09d)), each
with no user yet; Amdusias himself `6c17ddb30` (GC-09, [as built](#gc-09-amdusias-as-built): his entity, pact, service,
words, body, cues, audits, server tests and client flow); his review's fixes `28953d21b`; these records. The line starts
from `4201217b3`, the cohort with units 1 to 4 (Foras merged); unit 5 (Bifrons) is built on a line of its own beside it,
and the two join in the order 3, 4, 5, 6 ([the contract's merge notes](concordance-garden-cohort.md#where-to-resume)).
No Minecraft jar was available where he was built: every Java file of his was only parse-checked, and none of his 26
server tests or 35 client shots has run ([Verification](#verification)). No person has played him. This page holds his
character (the contract's Dossier 6), the short words the magic screens draw for him, the owner files his body and cues
use, and his record as built.
Where it goes beyond Dossier 6 (the amended greeting and its stage direction, the canopy's rule and registry, how his
body is built, which cue plays which file at what pitch, the short words the screens draw) it is unit 6's proposal
(GC-P120 to GC-P139, in the contract's Decisions), and the palette files changed only here, before any body file (the
contract's art plan). Four things came after the unit's design and are followed here
([where this brief departs from it](#where-this-brief-departs-from-the-units-design)): his station is unit 3's (GC-P67,
in the shared prefix GC-06b), with no station code of his own; his words keep to the magic interfaces' limits (GC-S26);
the owner-art root he reads his garland through is already in the base (GC-06b, first read by Foras's acorn cap); and
unit 3's `HabitatAdvice` is in the base, so the one-line edit that keeps a canopy's shelter in Marax's advice
(GC-P137), which the design left to the merge, lands in this unit's own commits. Where these change his specification,
the choices are the session's; none is the owner's.
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
    "Beds sheltered: n"). Never a Verdant Heart (the block above the bed decides), never more stir, never harvest strain
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
    chosen with the code (proposed: `Sign.MEND`'s six green motes; built as `Sign.WORK`, see "As built"). The design's six tinted leaves sent
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
| The quota as a cap: the quota chip's glance for `term.quota.canopy_shelter` (the server's "At most %s beds sheltered at once", its message) | "Max %1$s beds" ("Max 16 beds"; "%1$s at once" until the unit review, which gave it its noun within the chip's 3 words; its tip keeps the sentence's "at once") | 3 |
| The kept page's gauge (his `row.service`: beds covered of 16) | "%1$s/%2$s now" ("3/16 now"); see below | 2 |
| Blockers (no tree; no bed), each a lock line | "Needs a log within %1$s blocks of the perch"; "Needs your awake Verdant Bed within %1$s blocks" | 9; 8 |
| Chooser card reasons (no tree; no bed) | "Tree needed"; "Bed needed" | 2; 2 |
| Kept-page rows (no tree, marked waiting with a log; spared, marked asking with a Gloamcap) | "No tree: set a log within %1$s blocks"; "Spared: %1$s crops that want the stir" | 8; 7 |
| His matter (how many, and the nearest) | "Want the stir: %1$s, nearest %2$s. Spare them?" | 10 (with a position; two lines at the narrowest scroll) |
| His question in general (answered, and no matter raised now) | "Shelter crops that want the stir?" | 6 |
| His answers (their own words) | "Spare them" (`spare`); "Shelter all" (`all`) | 2; 2 |
| His first task's row | "First task: harvest a ripe crop by hand here" | 9: no act glance needed |
| Jade on him (his notes and matter) | "Beds sheltered: %1$s" (covered), "Spared crops: %1$s", "No tree", "Canopy elsewhere", "Nothing to shelter"; "Crops want the stir" (his matter); each count after its noun, as the other spirits' notes (the unit review; "Sheltering %1$s beds" and "Spared: %1$s" before it) | at most 4 |
| The readers' line, short: on a bed's and a Gauge's Jade (`jade.sheltered`), and for the Inspect page's and the reports' sentence (`ecology.sheltered`) | "Sheltered by Amdusias: -%1$s" ("Sheltered by Amdusias: -2") | 4 |

Two of these depart from the unit's design and one from the shared rule; GC-09 settles each with the code:

- **The quota's words.** The design gave the terms page a label ("Beds at once") beside the quota's sentence and a
  `PactScreen` change to draw it. The restyled terms page draws chips with no labels (the style's 1.1), so the server
  still names its own quota sentence (`PactService.quotaTerm`, GC-P126) and the chip draws its glance; the label key is
  not added. The one screen change, shared and made before him (GC-09d), is the chip's picture: a calendar only for
  the shared daily count.
- **The gauge's word.** `check_magic_ui` holds every work's `row.service` glance to "%1$s/%2$s today", the day's
  quota. His counts beds sheltered now, and "3/16 today" would say the daily count his terms deny (the contract: "never
  as a daily count"). Proposed: "%1$s/%2$s now", with that rule taking "now" for a work whose quota is a cap
  (`QUOTA_TERMS`, a table, so still data). Settled in GC-09: "now", decided by the session
  ([as built](#gc-09-amdusias-as-built)).
- **The service beat** is Dossier 6's line shortened from 23 words to 18; the greeting and price are Dossier 6's (the
  greeting amended as above); the stage direction is 8 words in place of the design's 29.

### Evidence plan

Nothing below has run. Each is what GC-09 is to build and run, in CI only (no Minecraft here):

- **Server tests** (26 with the unit review's): in the garden class, `aShelteredHabitatKeepsItsStrainedValue` and
  `theCanopyRegistryTakesTheLargestNeverTheSumAndLapses`; in the worker class, `aCanopyLowersOnlyDisturbanceForCropsInItsArea`,
  `aCanopyNeverSuppliesDisturbance`, `aCanopyNeverHelpsAHeart`, `aCanopyNeverEasesStrain`, `twoCanopiesNeverStack`,
  `aCanopySparesAGloamcapUntilAnswered`, `aCanopyLapsesWithin40TicksOfSuspension` (and of the tree's felling, the holder
  100 blocks away and the perch's breaking), `aCanopyLapsesWhenItsHolderLeaves`, `aCanopyCoversAtMost16Beds`,
  `aStrangersCropIsNeverSheltered`, `amdusiasRefusesWithoutATree`, `amdusiassFirstObligationIsAHarvestUnderHisCanopy`,
  `theInspectPageAndTheGaugeReadTheShelteredValue`, `theCanopysQuotaIsWordedAsACap`,
  `aCanopyReturnsAfterARestartWithinOneThink`, `amdusiasHornsABeatLate`, `hisTreeSwaysWhileHeShelters`,
  `aHolderKeepsOneCanopy`, `amdusiassReadingSaysItsNoteAndNamesNoPlayer`, `amdusiasAnswersEachActionWithHisGesture`,
  `aDuplicateAmdusiasSheltersNothing`, `amdusiasStandsBesideHisPerch`, `aCanopySkipsBedsHeMayNotChange` (the unit
  review's); in the economy class,
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

## The canopy's shelter (GC-09b)

What GC-09b builds, before any spirit uses it (GC-P122 to GC-P125, GC-P137, with the unit critique's items 3 to 5, 7, 8
and 11). Nothing calls `Canopies.cover` yet, so no canopy forms and every reading is what it was: a bed asks nothing more
while no canopy is kept in its level (`Canopies.any`).

- **The pure rule** (`ecology`, still pure): `Habitat` gains a seventh component, `shelter`, 0 through every earlier
  constructor (so a reading without a canopy is record-equal to what it was). `sheltered(damping)` lowers the area's
  disturbance by up to the damping, never below 0, and keeps what it took; sheltered again it takes the larger damping
  from the unsheltered area, never the sum. `unsheltered()` is the area's own; `strained()` counts the strain against
  it, so a canopy never eases strain; `with` keeps the shelter (within the room a new disturbance leaves) and
  `withStrain` keeps it whole. `Verdict.of` is unchanged; `Verdict.reasons`' two strained lines show the unsheltered
  value the strain was counted against. `Niche.wantsStir` is the niche's ideal disturbance starting above 0 (today only
  Gloamcap's). `HabitatInspection.sheltered` is the line a sheltered reading adds ("Sheltered by Amdusias: the stir here
  reads 2 lower (4 unsheltered); harvest strain still counts against the unsheltered stir"), at the end of both forms
  of the Inspect page; a strained disturbance row there adds the strain to the unsheltered area, as `Verdict.of` counts
  it (the critique's item 7).
- **The garden's memory** (`garden/Canopies`): per level (a `WeakHashMap`, as the sampling budgets), one canopy a
  holder (another perch's refused while one stands), its beds indexed by chunk, at most 16, its damping clamped to 0..2,
  lapsing 40 ticks after its last refresh (a clock run back reads it lapsed), pruned at most once a tick; never saved
  or sent. A bed reads the largest damping of the live canopies covering it, never their sum, and a plant that wants the
  stir only from a canopy whose holder answered "shelter all".
- **One place, every reader:** `VerdantBedBlockEntity.cachedHabitat` builds the reading sheltered for the plant above
  (a crop, or the organism a Habitat Gauge on the bed is attuned to; never a Verdant Heart, a device or bare soil), so
  growth, the Inspect page, the Gauge, Marax's reading and Jade read one value. The bed's and the crop's reports add the
  canopy's line after the habitat (the critique's item 8). The Gauge keeps what its last reading's canopy took, in
  memory, for Jade. Jade's bed and Gauge snapshots carry `sheltered` (the server's live value), shown whole in Jade's
  details and short otherwise.
- **Marax's advice** (`HabitatAdvice`, GC-P137): a changed strain keeps a canopy's shelter (`habitat.withStrain(value)`),
  so an eased strain is still judged against the unsheltered area.

Decided by the session (none is the owner's):

- **Marax names the value the plant reads.** On a sheltered reading his disturbance advice names the sheltered value
  (what growth judges, and what the Inspect page, the Gauge and Jade show), and its target is the reading the plant must
  reach; the alternative, the unsheltered area, would contradict every other reader and cannot be known exactly where
  the reading is 0 (the canopy then took less than its damping). The cost: where a sheltered reading is 0, the area
  must rise further than the step named before the plant reads the target; only a plant that wants the stir under a
  canopy answered "shelter all" (a Gloamcap) is ever told to raise it. The harness below finds the target holds either
  way.
- **The note carries no canopy.** The bed's saved and sent note never holds the canopy's line, which would outlive it
  (the critique's item 4); so a crop's own Jade tooltip, drawn from that note, does not name the canopy, while the bed's
  and the Gauge's Jade, the Inspect page and both reports do.
- **Jade's line is short and whole.** The restyle draws Jade's plain view short: the canopy's line there is "Sheltered
  by Amdusias: -2" with the tick mark (`tools/magic_ui.py` `SENTENCES` and `SENTENCE_MARKS`, `MagicGlance.MARKS`), and
  whole in Jade's details; so the line is drawn from three calls (the bed's plain view and details, the Gauge's line),
  where the critique's audit counted two. The Inspect page's and the reports' sentence has the same short form.
- **No `Canopies.forget` or `Canopies.live`.** The critique's item 5 removes every test's level-wide reading and
  forgetting; neither then has a caller, so neither is built.
- **The canopy's constants are audited from GC-09b** (`CONSTANTS["canopy_shelter"]`, the critique's item 11), in
  `check_ecology`, before his service joins the shared constants rule.
- **The `HabitatAdvice` edit lands here**, on this line, since unit 3 is in its base; the design left it to a merge.
- **The registry test's "shelter all" canopy has the smaller damping** (1, the other 2), so a Gloamcap reading 1
  shows that only the canopy answered "shelter all" counts; with the design's (2 answered, 1 not) it reads 2 either way.

The two server tests of GC-09b (`ConcordanceGardenGameTests`): `aShelteredHabitatKeepsItsStrainedValue` (the pure
rule's named cases, Marax's advice on a sheltered reading, and a Sunpetal on a real bed under a canopy set by hand,
sampled through the shared `afterSampling`, with its Inspect page) and
`theCanopyRegistryTakesTheLargestNeverTheSumAndLapses` (the registry by hand, real ticks). Each asserts only its own
holders and positions, since the canopies of every test beside it share the level.

**Harnesses (scratch, not in Git), compiled with JDK 21 against the committed `ecology` classes** (`Habitat`
`ccb59860f259363d2df7dbb894d7d586074a156532627cad9e865d4ba98a0d32`, `Verdict`
`e4da46f9d9eb2747f83061c4e5318391d8471fa6bfc38c7f6ab85ec3ff64f683`, both the design's exact text; `HabitatInspection`
`a7578e9af10815331b83ca15e9fd91165c7b05918d4e02496a74f58f5b5c4f28`, the design's with the critique's item 7; `Niche`
`7ae32a26cd3e7525e84e8454f7f0417e4fdc9f3a85bd6d9712d065033172ebe2`; `HabitatAdvice`
`19e41b40cb1b8212610a433cd386f4e27d784e281ad88d9fc1188eaa767cc194`):

- The design's shelter harness, extended with `Niche.wantsStir` and the Inspect page's rows, against the classes as
  they were before GC-09b in a second class loader, over every shipped niche, moisture {0, 2, 4, 7}, light
  {0, 8, 11, 13, 15}, nutrients {0, 2, 4, 32}, diversity {1, 3, 8}, disturbance 0 to 15, strain 0 to 3 and two
  dampings 0 to 4: **13,221,792 checks, 0 failures**. Without a canopy every verdict, growth, fit, reason and Inspect row
  is the old classes'; only disturbance is lowered, by at most the damping, never below 0; `strained()` is unchanged;
  two canopies give the larger; a crop not wanting the stir is never slower (a canopy of 2 raised a step in 194 Sunpetal,
  432 Mendvetch and 176 of the Heart's readings with no strain) and a Gloamcap sheltered by 2 is slowed in 528 (why he
  spares it); Marax's one-change advice on 137,743 sheltered readings holds. With the strain branch as it was (the
  shelter dropped), 9,056 of those advices would not hold (Gloamcap 288, Mendvetch 4,536, Sunpetal 2,184, the Heart's
  niche 2,048).
- Unit 3's advice harness ([Marax's record](marax-pact.md#the-rule-gc-p64)) on the changed classes, unchanged:
  **30,604,024 checks, 0 failures**, 29.3 s.
- The same harness with a shelter dimension (every one of its readings also sheltered by 1 to 4; its strain changed with
  `withStrain`): **151,395,025 checks of its own families, 0 failures** (30,604,016 of them at damping 0: the run
  above's, less its 8 named cases), and **16,516,020 checks of three new families, 0 failures**: the value named is the one the plant reads; a
  change applied keeps the shelter; and the change made in the world under the same canopy gives what the advice
  promises, for 654,422 eased strains and for 860,754 lowering and 151,040 raising advices on sheltered readings (all
  the raising ones a Gloamcap's). In 69,120 of the raising ones the reading is 0 and the area must rise further than the
  step named (above).

## A cue a beat late and a quota worded as a cap (GC-09d)

What GC-09d builds, before any spirit uses it (GC-P126, GC-P127, with the unit critique's items 1 and 16). No spirit
holds a cue and no service words its own quota yet, so every cue sounds with its gesture and every terms page reads as
it did.

- **A cue a beat late** (`PactSpiritEntity`): a spirit may hold a cue's sound for a beat after its gesture
  (`cueBeat(id)`, 0 by default; Amdusias's horn is to be its first). A held cue is noted, subtitled and gestured at once,
  bounded as every cue is; its sound plays when its beat comes, from the spirit's own server tick, where it then stands
  (a positional sound with the same event and subtitle, so it is heard though a departing body has gone), departing or
  not; a clock run back sounds it at once; the first one waiting keeps its time. Switched off meanwhile, a held cue is
  dropped unsounded, but for a departure's, as no cue shows while the Concordance is off but the departure (the
  critique's item 16). When each cue's sound last played is kept for the tests (`soundedAt`, `Long.MIN_VALUE` before
  any). Every other cue's sound still plays at the spirit with its gesture, as D-06's audit holds. Nothing of it is saved.
- **A quota worded as a cap** (`PactService.quotaTerm`, `PactMenu.service`): the terms page's quota is its service's own
  sentence, `term.quota` ("%s a game day") by default and, for a service whose quota caps what it holds at once,
  `term.quota.` + its work (`tools/concordance_pacts.py` `QUOTA_TERMS`, empty until his unit; a work this version does
  not serve keeps the shared one). The restyled chip draws that sentence's glance (`tools/magic_ui.py` `GLANCES`
  "pact.term.quota.<work>", which the module requires for every `QUOTA_TERMS` work) and keeps the sentence as its
  message, narration and tip.

Decided by the session (none is the owner's):

- **A cap's chip draws its work's item, not the calendar.** The shared daily count keeps the calendar; a work's own
  quota words, a cap held at once, draw that work's own item (its service line's, `PactGlance.workIcon`), so the chip
  never pictures a day's count. This is the one screen change for the quota, made before him in shared code; his unit
  then adds data only.
- **No label.** `QUOTA_TERMS` holds only each work's sentence; the design's screen label and its `PactScreen.termLabel`
  are not built (the restyled chips draw no labels).
- **A held cue's time is kept only when it sounds.** `soundedAt` names when a sound played, so a held cue dropped while
  the Concordance is off, or one with no registered sound, leaves it as it was (the design noted the time either way).
- **The audits read each file once, without comments** (`check_pacts`' own `java(...)` reader, as the critique's item 1
  asks, where the design called a reader that function does not have), and hold more than the design's: the on-time
  branch keeps its time, the held branch sits before it in `cueing`, the switch is read before a held sound plays,
  `soundedAt` reads the times kept, `PactMenu.quotaTerm` falls back to the shared count, only the shared count draws a
  calendar, a service overriding `quotaTerm` in any other way is refused, `QUOTA_TERMS` names only works this version
  serves, each of its sentences takes one `%s` and is generated with its chip's glance, and neither the sentence nor
  the glance counts by the day (`tools/magic_ui.py`).
- **The gauge's word** was left open here (`check_magic_ui` held every work's `row.service` gauge to "%1$s/%2$s
  today") and is settled with his words in GC-09: "now" for a cap ([as built](#gc-09-amdusias-as-built)).

## GC-09: Amdusias as built

What a player meets first: with The Garden's Regard understood, a Pact Perch with a log within 2 blocks of it and an awake
Verdant Bed of theirs within 6 lists Amdusias on its chooser; without the log his card says "Tree needed" and his words
"I bend trees. There is no tree here." Chosen, a garlanded white unicorn steps up beside the perch (two blocks west of it,
facing whoever speaks) and his horn is heard a beat later. His terms name a note block and 2 oak saplings once, then a
sapling a week, and his quota as a cap ("Max 16 beds"). Sealed, while his holder is near he bends his tree's shelter over
the crops of up to 16 of their and their party's awake beds in his area, the nearest first: each reads the stir up to 2
lower, never below nothing, and the Inspect page, the crop's and the bed's reports, a Habitat Gauge set on a bed and
Jade say "Sheltered by Amdusias". A Gloamcap, which wants the stir, he spares and asks about ("Spare them" or "Shelter
all"). Every 10 s while he shelters, and once as he bows and leaves, his tree sways (the shared sign at the log's top).

### Record fields (as built)

- **Tier:** Practitioner, Spiritbinders; The Garden's Regard understood (`garden_regard`), nothing more.
- **Input producer:** a log (any `#minecraft:logs` block) within 2 of the perch on every axis; an awake Verdant Bed of
  the inviter's within 6; his compact (`minecraft:note_block`, 2 `minecraft:oak_sapling`) and offerings (oak, birch or
  spruce sapling, `#jugcraft:amdusias_offerings`), each a `VANILLA_SOURCES` row (renewable saplings, a crafted note block).
- **Output consumer:** the disturbance a covered crop's growth, the Inspect page, the reports, the Gauge and Jade read
  (`VerdantBedBlockEntity.cachedHabitat`, the one place); nothing else.
- **Costs:** the compact once; 1 sapling every 7 days, a day's grace, at most 4 ahead. Resonance 0.
- **Unlocks:** none; his first obligation (`PactObligations.Act.HARVEST`: a ripe crop harvested by hand in his area while
  the pact is active, its one hook after the hand harvest's cultivation record in `OrganismCropBlock.useWithoutItem`)
  counts toward The Garden's Regard's mastery as each garden spirit's does.
- **Failure behaviour:** no tree: `WAITING_FOR_RESOURCES`, canopy lifted, spared crops forgotten, Jade "No tree";
  outside his hours (he has none: 0 to 24,000), suspended, held, the holder away or offline: the canopy lifted at his
  think or lapsing within 40 ticks; his holder's canopy at another perch: `IDLE`, Jade "Canopy elsewhere"; nothing to
  shelter: `IDLE`, "Nothing to shelter"; a bed refused for itself (not a beneficiary's, the holder's hands refused) is
  skipped; a Heart, a device or bare soil is never covered.
- **Saved data:** the base's perch and record only; `jugcraft:amdusias`, `canopy_shelter`, `no_tree`, `no_bed`,
  `wants_stir`, `spare` and `all` are stable, append only. His canopy, memory, sway count, late horn and the Gauge's
  `sheltered` are memory only; nothing he does is counted on his record (`taskDone` is never called).

### What he adds (data on the shared systems)

- **Java:** `PactObligations.Act.HARVEST`; `spirits/CanopyShelter` (his service: his step, tree, rows, refusal, matter
  and Jade note); `spirits/AmdusiasEntity` (his tables: `Station.BESIDE`, his gestures, `cueBeat` for his horn,
  `gestured` for the departing sway); `PactParser.WORK`/`REQUIRED`, `Workers` (entity 1.6 x 2.4, eyes 1.9, sounds,
  service), `PactSpiritRenderers` (his row), `PactGlance` (his work's icon and two rows' marks and icons) and
  `MagicGlance.MARKS` (his notes).
- **Tables:** his `SPIRITS` row, `SERVICE_WORDS`, `SERVICE_CLASSES`, `SERVICE_NAMES`, `ART_SOUNDS`,
  `GARDEN_CLIENT_SHOTS`, `QUOTA_TERMS`, the perch tooltip's clause (`tools/concordance_pacts.py`); his body
  (`tools/garden_bodies.py`); `ACTS`, `HOOKS`, `OBLIGATIONS`, `CONSTANTS`, `DISTINCT`, the act's words and four codex
  pages (`tools/concordance_garden_pacts.py`); the roster line; `VANILLA_SOURCES`; three journey steps; his
  `WORK_GLANCES` row, quota chip glance, marks, icons and Jade short forms (`tools/magic_ui.py`); `WEIGHTS` +240.
- **Interfaces changed beyond the contract's list** (the unit critique's item 15): `PactSpiritEntity.cueBeat`,
  `soundedAt` and the held cue (GC-09d); `PactService.quotaTerm`, `PactMenu.service`, `PactScreen.chip` (GC-09d);
  `Verdict.reasons`, `HabitatInspection.sheltered` and its strained row, `Niche.wantsStir`,
  `HabitatGaugeBlockEntity.sheltered`, `VerdantBedBlockEntity.canopyPlant`, `OrganismCropBlock.report`, `HabitatAdvice`
  and Jade's `sheltered` keys (GC-09b).

### Decided by the session (none is the owner's)

- **The design's own words, refined as built** (each in the contract's Decisions): the greeting amended ("You see me
  before you hear me", GC-P120); the offering tag named (three saplings, not `#minecraft:saplings`, GC-P121); "wants the
  stir" as the niche's ideal starting above 0 (GC-P122); the origin oak leaves' two colours added to the palette for his
  horn's leaves (GC-P133); the quota's words as a cap with no label (GC-P126); his canopy lifted at his own think or by its
  lapse, never by a gesture.
- **The gauge reads "now".** His kept page's gauge is "%1$s/%2$s now" (beds sheltered now of his cap), and
  `check_magic_ui` takes "now" in place of "today" for a work in `QUOTA_TERMS` (still data, no code path). The task
  this unit was built from lists the shared "%1$s/%2$s today"; "3/16 today" would present his cap as the daily count
  his terms and the contract deny, so the brief's proposal is taken. Reviewable at the checkpoint.
- **His sway is the shared sign.** No spirit sends particles of its own (`check_signs`), so his tree's sway is
  `Signs.show` with `Sign.WORK` (execution: work being done) at the log's top (`AmdusiasEntity.SWAY`; his row's
  `"sway": {"sign": "work", "ticks": 200}`), where the design sent tinted leaves; each client draws it as its settings
  allow. It was `Sign.MEND` until the unit review: MEND is an effect mark (where an effect was applied), and no mend
  happens when his tree sways; Bathin's shimmer and the strain and habitat readings show `Sign.WORK` too. A leaf sign of
  its own is left to a later, reviewed change to the shared `SIGNS`. Whether it reads as his tree swaying is for the
  owner to see in game.
- **SG12's fixture puts his log north-west of the perch's cell** (`perch.offset(-1, 0, -1)`), not east: on this line
  he is the sixth cell, `(7, 1, 4)`, whose east is outside the 8-wide structure; north-west stays inside there and in the
  merged line's seventh cell `(1, 1, 7)`, clear of every fixture and of the second perch the shared cases place north.
- **His "no tree" row and note wait for his first look** (`Memory.looked`), so a kept page before his first think does
  not say there is no tree.
- **Jade's notes are third person** ("His holder's canopy stands at another perch"), as every Jade pact line, since
  Jade's reader may be anyone; his `row.service` words name the Gauge (a Gauge's bed counts toward his 16, the
  critique's item 16).
- **His body's builder** is the unit's harness verbatim but one name: its `PETALS` is `WHITE_PETALS`, since Buer's petal
  bones already use `PETALS` in `tools/garden_bodies.py`.
- **His `HARVEST` entry carries a Javadoc**, as the cross-unit resolution words it, in place of the design's comment.
- **His codex pages** come after Foras's and before "Mastered" (pages 20 to 23 here; Mastered moves to 24), each at most
  60 words, his numbers read from the tables that hold the Java's.
- **The server tests** follow the cohort's built practice where the design differs: beds stand on the empty structure
  at y 1 (a stone floor only for his station's test), positions chosen inside each structure; reading tests sample
  through the shared `afterSampling` and seal and think at the level's own time inside it (the critique's item 3); each
  asserts only its own holders and beds, and none forgets the level's canopies (item 5: `Canopies.forget` and `live`
  are not built), so the restart test lifts its own; the 16-bed test reads no habitat; the quota test reads the quota's
  sentence (no label); the horn test invites him through the shared `offerOf`; the Heart test does not compare the
  Heart's own beat; the stranger's test drops the design's "a stranger's crop on the holder's bed" (a crop has no
  keeper of its own); the economy test answers "shelter all" so a Gloamcap is covered too.
- **The client flow follows Foras's restyled flow**, with his own steps: his refusal shown on the chooser (as every
  garden spirit's now), his quota checked as its sentence and its chip ("Max 16 beds"), his three beds left bare until
  the horn's step (so his canopy forms with the test's own think and its gesture, not with one of his), the Inspect
  page opened with the encyclopedia's key looking at the bed, his sway shown by a call after his tree is read, and no
  end-confirmation shot (his list has none). The horn's subtitle is staged on the HUD's own overlay where no sound
  device plays (CI), logged as staged; the server's time of the horn is checked either way.

### Saved data and stable ids

| What | Id or key | Saved? |
|---|---|---|
| Entity | `jugcraft:amdusias` | as every pact spirit: the base's `perch` and `record` only; nothing of his own |
| Pact definition | `jugcraft:amdusias_pact`; `data/jugcraft/concordance/pact/amdusias.json` | data |
| Offering tag | `#jugcraft:amdusias_offerings` (`minecraft:oak_sapling`, `birch_sapling`, `spruce_sapling`) | data |
| Sound events | `jugcraft:amdusias.{arrive,listen,inspect,assent,refuse,depart,work}` | registry |
| Work id | `canopy_shelter` (`BoundWill.Service.work`; `PactParser.WORK`, append only) | yes, on the pact record |
| Accord values | `spare`, `all` (the perch's `accord`; append only, never renamed) | yes, on the perch |
| Matter and notes | `wants_stir`; Jade notes `covered`, `spared`, `no_tree`, `elsewhere`, `nothing` | sent with a reading, never saved |
| Obligation | `PactObligations.Act.HARVEST` (`harvest`); its evidence key `practice:` + the garden activity + `:jugcraft:amdusias` (unit 1's form) | the shared obligations' evidence |
| Canopy | `garden/Canopies` | **never saved or sent**; lapses within 40 ticks; returns within one think after a restart |
| Habitat shelter | `Habitat.shelter`; the Gauge's `sheltered` | never saved (a reading; the Gauge's in memory); the bed's saved keys and note unchanged |
| Late cues | `PactSpiritEntity`'s held cues and `soundedAt` | never saved |
| His memory | `CanopyShelter.Memory` (tree, index, spared and skipped beds, last step, sways) | never saved |

No migration: nothing saved before is renamed or removed. A save holding his pact, loaded by a build without him, keeps
it with its work unknown (the shared record's rule), and no canopy forms.

### Known limits

- **Not compiled or run.** Every test and shot of his waits for CI; nothing here says he works in game.
- **No person has played him**, alone or with others; no two-client run at his perch (the cohort's two-client test is
  unit 7's).
- **His horn's subtitle on CI is staged:** with no sound device the client's sound engine starts nothing, so his client
  flow shows the subtitle on the HUD's own overlay and logs it as staged; the server test holds the horn's beat. A
  machine with sound shows the real one.
- **One damping where two holders cover a bed:** two holders' canopies over one party bed give the larger damping, never
  the sum; each holder keeps one canopy (a second perch's waits until the first lapses).
- **His sway is the shared work sign** at the log's top, not leaves of his own; whether it reads as a tree swaying is for
  the owner to see.
- **A Gauge's bed counts toward his 16**, as a crop's does.
- **His gauge reads "now"** (beds sheltered now of his cap), where the shared rule reads "today"; reviewable at the
  checkpoint.
- **The crop's own Jade tooltip does not name the canopy** (it is drawn from the bed's saved note, which never carries
  it); the bed's and the Gauge's Jade, the Inspect page and both reports do.
- **His body is built rather than the owner's** (no owner rig is a unicorn; the garland is the owner's flower crown),
  2.4 blocks tall, amending the contract's 2.0.

## Verification

**Not run in CI.** Nothing of unit 6 has run in CI, and no Minecraft jar was available where he was built: no build,
game test or client shot has exercised him, and every Java file of his was only parse-checked (no syntax errors). Each
row's evidence below is what will show the item, not a result. Each API his files use was checked against the
repository's own uses, the 26.3 decompiled source and GeckoLib 5.5.7's source; those resting on reading alone are under
"Not confirmed" below. The line this brief starts from is `4201217b3` (with Foras).
4201217b3's Build (run 40847753203) failed in `mod` and `optional integrations absent`. 5e93bb0a77 fixes it (the reload
test thinks first; 6G heap), and this line takes it at the merge. Do not run CI on this line alone. The newest green
result it descends from is run 40840353835, green in full on `3bfd63a97`.

Each brief item and the evidence named (the contract's GE9):

| Brief item | Evidence (server game tests in `ConcordanceWorkerGameTests` unless named; shots `jugcraft_garden_amdusias_*` of `ConcordanceGardenPactClientGameTests`) | Result |
|---|---|---|
| 1. Identity | his body files from `tools/garden_bodies.py` through `tools/owner_art.py` (`--check`); `check_spirit_bodies` (his bones, the garland's twelve cubes from the owner's crown with their dropped faces, the skin's colours, his box, clips and telling windows, his loops and shots in the client); his poses from his front (`_idle`, `_listen`, `_waiting`, `_suspended`, `_inspect`, `_assent`, `_refuse`, `_work`, `_depart`, `_arrive`) | the audits pass locally; shots not run |
| 2. Invitation and route | `amdusiasRefusesWithoutATree` (and without a bed), `amdusiasStandsBesideHisPerch`; `tools/concordance_journey.py --check` (the garden pacts' steps 17 to 19 on this line); shots `_no_tree`, `_chooser` | not run; the journey check passes locally |
| 3. Service | `aCanopyLowersOnlyDisturbanceForCropsInItsArea`, `aCanopyNeverSuppliesDisturbance`, `aCanopyNeverHelpsAHeart`, `aCanopyNeverEasesStrain`, `aCanopyCoversAtMost16Beds`, `aCanopySkipsBedsHeMayNotChange`, `aCanopyReturnsAfterARestartWithinOneThink`, `theInspectPageAndTheGaugeReadTheShelteredValue`; `ConcordanceGardenGameTests`' `aShelteredHabitatKeepsItsStrainedValue`, `theCanopyRegistryTakesTheLargestNeverTheSumAndLapses` (GC-09b); `ConcordanceEconomyGameTests.theEconomyStillHasNoGainingCycle`; shots `_inspect_page`, `_bed_jade`, `_gauge_jade` | not run |
| 4. Upkeep and suspension | `aCanopyLapsesWithin40TicksOfSuspension` (and of the tree's felling, the holder 100 blocks away, the perch's breaking), `aCanopyLapsesWhenItsHolderLeaves`; SG12's fifteen shared cases through his fixture (his log north-west of the perch); `noGardenTermOrRefusalNamesAnAstrolabe`, `talkingTeachesNothing`; `theCanopysQuotaIsWordedAsACap`; `check_acquisition` (his compact and offerings reachable); shots `_terms` (three sizes), `_refused`, `_kept` | not run; the audit passes locally |
| 5. Conflict: the stir a Gloamcap wants | `aCanopySparesAGloamcapUntilAnswered`, `amdusiassReadingSaysItsNoteAndNamesNoPlayer`; shots `_matter` (three sizes), `_answered`, `_jade` | not run |
| 6. Refusal and departure | `amdusiasRefusesWithoutATree`, `amdusiasAnswersEachActionWithHisGesture`, `aDuplicateAmdusiasSheltersNothing`; shots `_conversation` (three sizes), `_depart`, `_ended` | not run |
| 7. Interaction details | (a) the late horn: `amdusiasHornsABeatLate`, shot `_horn` (staged on CI); (b) the tree's sway: `hisTreeSwaysWhileHeShelters`, shot `_sway`, the ending's sway at his tree in the client; facing: `amdusiasStandsBesideHisPerch`, shots `_faces_east`, `_south`, `_west`, `_north` | not run |
| 8. Evidence | the client class keyboard-only at GUI 2, GUI 4 and 320 x 180; *ownership:* `aStrangersCropIsNeverSheltered`, `aCanopySkipsBedsHeMayNotChange`, SG12's `everySpiritWaitsForANearHolder`; *no duplication:* `twoCanopiesNeverStack`, `aHolderKeepsOneCanopy`, `aDuplicateAmdusiasSheltersNothing`; *save:* `aCanopyReturnsAfterARestartWithinOneThink` (nothing saved); *first obligation:* `amdusiassFirstObligationIsAHarvestUnderHisCanopy`; *Jade:* `amdusiassReadingSaysItsNoteAndNamesNoPlayer`, shot `_jade` | not run |

**Server tests written for him** (26; `tools/check_mod_data.py` holds their names): `GC09_TESTS`, two in
`ConcordanceGardenGameTests` from GC-09b (`aShelteredHabitatKeepsItsStrainedValue`,
`theCanopyRegistryTakesTheLargestNeverTheSumAndLapses`), eighteen in `ConcordanceWorkerGameTests`
(`aCanopyLowersOnlyDisturbanceForCropsInItsArea`, `aCanopyNeverSuppliesDisturbance`, `aCanopyNeverHelpsAHeart`,
`aCanopyNeverEasesStrain`, `twoCanopiesNeverStack`, `aCanopySparesAGloamcapUntilAnswered`,
`aCanopyLapsesWithin40TicksOfSuspension`, `aCanopyLapsesWhenItsHolderLeaves`, `aCanopyCoversAtMost16Beds`,
`aStrangersCropIsNeverSheltered`, `amdusiassFirstObligationIsAHarvestUnderHisCanopy`,
`theInspectPageAndTheGaugeReadTheShelteredValue`, `theCanopysQuotaIsWordedAsACap`,
`aCanopyReturnsAfterARestartWithinOneThink`, `hisTreeSwaysWhileHeShelters`, `aHolderKeepsOneCanopy`,
`amdusiassReadingSaysItsNoteAndNamesNoPlayer`, and from his review `aCanopySkipsBedsHeMayNotChange`) and
`ConcordanceEconomyGameTests.theEconomyStillHasNoGainingCycle`; `GC09_AMDUSIAS_TESTS`, five in
`ConcordanceWorkerGameTests` (`amdusiasStandsBesideHisPerch`, `amdusiasRefusesWithoutATree`, `amdusiasHornsABeatLate`,
`amdusiasAnswersEachActionWithHisGesture`, `aDuplicateAmdusiasSheltersNothing`). SG12's fifteen shared cases run for him
through his fixture, and `noGardenTermOrRefusalNamesAnAstrolabe` and `talkingTeachesNothing` for every offered spirit.
The existing habitat, garden and Gauge tests are unchanged and must hold with no canopy. None of these has run.

**Client** (`./gradlew runClientGameTest -PclientTests=ConcordanceGardenPactClientGameTests`): his flow after Foras's, at
the same perch, calling the shared yard first, at noon in clear weather; 35 shots, `jugcraft_garden_amdusias_` +
`no_tree`, `chooser`, `conversation` and `terms` (each at three sizes), `refused`, `kept`, `horn`, `inspect_page`,
`bed_jade`, `gauge_jade`, `sway`, `matter` (three sizes), `answered`, `jade`, `faces_east`, `_south`, `_west`, `_north` and
`ended` (25 named in `GARDEN_CLIENT_SHOTS`), and his ten body shots in his `SPIRITS` row (`idle`, `listen`, `waiting`,
`suspended`, `inspect`, `assent`, `refuse`, `work`, `depart`, `arrive`), each pact screen held to its word budget
(beats 30, terms 50, kept page 45, roster 70, the chooser's `chooserWords`) and his chooser's card held whole.
`tools/select_client_tests.py` sends a change to his body, entity or service to this class (its weight `+240`;
`scripts/tests/test_client_test_selection.py`). Not run; none of its pictures exists yet, so none has been looked at and
this record has no "Seen" column.

GC-09b: no Minecraft jar here, so its Java was parse-checked only (the `ecology` package, all but its JSON parser,
also compiled with JDK 21 for the harnesses above); its two server tests have not run. Run and passed locally:
`tools/generate_material_data.py` and `tools/owner_art.py` (the generated difference: the two lang sentences and their two
short forms), `owner_art.py --check`, `tools/check_mod_data.py` (with `check_ecology`'s GC-09 block),
`scripts/check_repository.py`, `concordance_journey.py --check`, `concordance_economy.py` (gaining cycles: none),
`check_icon_maps.py`, `tools/magic_ui.py`, the world designer's model tests, `scripts/tests` and `git diff --check`.

GC-09d: its Java was parse-checked only; no game test or client shot exercises it, and the regression it must leave
(Stolas's, Buer's, Bathin's, Marax's and Foras's cues and terms pages unchanged) rests on their existing tests in CI,
which have not run on it. The generators wrote nothing new (no lang key: `QUOTA_TERMS` is empty). Run and passed
locally: the generators, `owner_art.py --check`, `tools/check_mod_data.py` (with `check_pacts`' GC-09 block),
`scripts/check_repository.py`, `concordance_journey.py --check`, `concordance_economy.py` (gaining cycles: none),
`check_icon_maps.py`, `tools/magic_ui.py`, the world designer's model tests, `scripts/tests` and `git diff --check`.
Twenty audit mutations on a scratch copy (each of the held cue's and the quota's rules, the switch, the save, a
quota worded by the day, a quota term with no service, override or glance) were each caught; a consistent cap (its
sentence, its service's override, its glance and keys) passed as the control.

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

GC-09 ([as built](#gc-09-amdusias-as-built)): its Java (main, client and the three test classes) was parse-checked
only; none of it has compiled, and none of its 25 server tests (2 from GC-09b, 22 in the worker class, 1 in the economy
class), the SG12 cases through his fixture or his client flow (25 flow shots and 10 body shots) has run. Run and passed
locally: the generators (one at a time; the generated difference is his lang, sounds, codex pages, pact, offering tag and
built body files), `owner_art.py --check`, `tools/check_mod_data.py` (with the GC-09 blocks of `check_pacts`,
`check_garden_pacts` and `check_spirit_bodies`, and his rows in `check_magic_ui`), `scripts/check_repository.py`,
`concordance_journey.py --check`, `concordance_economy.py` (gaining cycles: none), `check_icon_maps.py`,
`tools/magic_ui.py`, the world designer's model tests, `scripts/tests` and `git diff --check`. Its audit
mutations, each on a scratch copy, are in the table below.

Fixes from Amdusias's review (written, not run; parse-checked only, none of it compiled or run in CI):

- **A stopped Amdusias says nothing stale.** His service's step remembers when it last ran (`Memory.fresh`: within
  `Canopies.LAPSE_TICKS`); a stop that never reaches it (the holder away or offline, suspended, held) no longer leaves
  Jade saying "Beds sheltered: 3" over a lapsed canopy, nor the spared crops raising his matter and the "Spared" row.
  Jade's covered count is the live canopy's, as the kept page's gauge reads it. `aCanopyLapsesWhenItsHolderLeaves`
  asserts it.
- **Beds he may not change are asked once a read.** Each bed is asked (`GardenPacts.target`) before its crop is spared,
  so a Gloamcap on a bed in another's claim is never his matter, and a bed refused for itself is skipped until his index
  is read again (Buer's rule), so refused beds never use up his 16 asks. New test: `aCanopySkipsBedsHeMayNotChange`.
- **His tree's sway is the work sign**, not the mend mark (see "Decided by the session").
- **Words:** the quota chip reads "Max 16 beds" (the review asked "16 beds at once", 4 words, over the chip's 3 in
  the style's 1.1, which `check_magic_ui` holds; the server's sentence, its tip, keeps "at once"); Jade's notes read "Beds sheltered: %1$s" and "Spared crops: %1$s".
- **Tests:** the client flow's ending allows his tree's own sway inside the window before he departs (one sway more or
  two, the last his departure's, at his tree, after the request); the obligation test checks the fourth harvest was
  done; `check_mod_data` reports a missing `HabitatAdvice.java`.

Run and passed locally for these fixes: the generators (one at a time; the generated difference is four lang lines),
`owner_art.py --check`, `tools/check_mod_data.py`, `scripts/check_repository.py`, `concordance_journey.py --check`,
`concordance_economy.py` (gaining cycles: none), `check_icon_maps.py`, `tools/magic_ui.py`, the world designer's model
tests, `scripts/tests` and `git diff --check`. Thirteen audit mutations on a scratch copy (the live covered count, the
fresh step in his note, matter and rows, the step's time, the ask before the spare, the skip and its forgetting, the
mend mark in the entity and in his row, `HabitatAdvice.java` emptied, the four-word chip, the new test renamed) were
each caught.

**Audit mutations, by commit** (each on a scratch copy of the worktree, deleted after; from the session's logs):

| Commit | Mutations | Outcome |
|---|---|---|
| GC-09a `23236575e` | a broken link; a trailing space; a broken heading anchor; a wrong SHA-256 in the asset table | the link caught by `check_repository.py`, the space by `git diff --check`; the anchor caught only by a hand check (no local check reads anchors); the hash **not caught** (no check reads the brief's hashes) |
| GC-09b `929337b5c` | A1 to A29: `strained()` without the shelter; a damping summed (`sheltered`, `Canopies.damping`); a negative damping let through; `Verdict`'s strained lines and second judgement on the sheltered value; `wantsStir` on the range's minimum; the Inspect page without the line, its strained row on the sheltered value (critique item 7); a 7-argument `Habitat` in `Sampler`; `cachedHabitat` unsheltered; a Heart sheltered; the registry read before `any()`; the note carrying the line (item 4); the stir rule dropped; `LAPSE_TICKS` 400, `MAX_DAMPING` 3, `MAX_BEDS` 32 in `CONSTANTS`; the registry saving; the Heart asking the registry; the Gauge saving or forgetting its shelter; the Jade snapshot and line; the lang key; the crop's and bed's report lines (item 8); `HabitatAdvice`'s strain branch dropping the shelter (GC-P137); `MagicGlance.MARKS` without the mark | 29 caught by `check_mod_data` (A18 first with an ambiguous anchor, fixed and re-run) |
| GC-09d `916b624d4` | B1 to B14 and B5b (the held cue: its default beat, its place in the step, its sound attached to the entity, saved, the held branch or its time removed, the switch ignored, sounded at once, never sounded; D-06's on-time sound removed); Q1 to Q7 (the quota as a cap: a sentence with no override or glance, an override with no sentence or another way, a glance counting by the day, two arguments, an unserved work), Q5 a consistent cap as the control | 20 caught by `check_mod_data` and `tools/magic_ui.py`; the control passed, as it should |
| GC-09 `6c17ddb30` | D1 to D24: the felled tree keeping spared crops (item 6); the stir rule dropped; an item handled; `TREE_REACH` 3; the horn's beat as long as his departure; standing on the perch; the sway with no sign, or on his work; quota 32; the perch tooltip without his clause; his price page dropped; a canopy test renamed; a pose outside its window; his gauge worded by the day; `PactGlance.WORK_ICONS` and `MagicGlance.MARKS` without him; `GC05_ACCORDS` reordered; Jade's notes reordered; one garland face fewer dropped; the harvest hook removed; his act out of `ACTS`; his sway's shot not taken; his canopy kept where his pact cannot serve; his loop claiming work with no tree | 23 caught by `check_mod_data` (D11 first with an ambiguous anchor, fixed and re-run); D19 stopped by `tools/garden_bodies.py`'s own guard ("not the faces the brief names: re-brief"), not by an audit message |
| his review's fixes `28953d21b` | M1 to M13 (above) | 13 caught; M2 (the fresh guard dropped from his matter) only after the audit's reading of `complication()` was narrowed in the same commit |
| these records | see the commit's message | |

**Local, these records** (no Minecraft build here): `python3 tools/owner_art.py --check`, `python3 -B
tools/check_mod_data.py`, `python3 scripts/check_repository.py`, `python3 tools/concordance_journey.py --check`,
`python3 tools/concordance_economy.py` ("gaining cycles: none"), `python3 tools/check_icon_maps.py`, `node --test
tools/world-designer/model.test.cjs`, `python3 -m unittest discover -s scripts/tests` and `git diff --check`, each run
on this commit's tree; their results are in the commit's message. This commit changes no generated file and no code.
The five `ecology` classes the harnesses above ran against are unchanged since GC-09b (each SHA-256 above read again on
this commit's tree), so those runs stand for it.

His rows of `python3 tools/owner_art.py --provenance pacts`, as printed for this record:

| Runtime file | Source | SHA-256 | How |
|---|---|---|---|
| `geckolib/animations/entity/amdusias.animation.json` | built from code alone | | built by `tools/garden_bodies.py` (amdusias_animations) |
| `geckolib/models/entity/amdusias.geo.json` | `originals/Edited-2026-10-18-Part2/cnb/geo/armor/flower_crown.geo.json` | `c3cfaa055b62910d96e009c510ad50e4c5e4be6e2952258351b7dbeb2dff47fe` | built by `tools/garden_bodies.py` (amdusias_model) |
| `geckolib/models/entity/amdusias.geo.json` | `originals/Edited-2026-10-18-Part2/cnb/textures/armor/flower_crown.png` | `61b8e6fa10914bddc7f091d7fca62f488230b488b2d4c35648a8ef7e10b1c1c4` | built by `tools/garden_bodies.py` (amdusias_model) |
| `textures/entity/amdusias.png` | `originals/Blocks/biomes and tree blocks/white_petals.png` | `20b88497e849470631ac740a8e646c365784063d0c74762a85853be9123c3fc0` | built by `tools/garden_bodies.py` (amdusias_skin) |
| `textures/entity/amdusias.png` | `originals/Blocks/biomes and tree blocks/origin_oak_log.png` | `c147b6cdc087ce197f4dfd422595d8216ba131efef2ed0f3727014cd033b7969` | built by `tools/garden_bodies.py` (amdusias_skin) |
| `textures/entity/amdusias.png` | `originals/Blocks/biomes and tree blocks/origin_oak_leaves.png` | `43462aeeba55c863de64696194a8ba10c2b5ae75d5c2cbf229a1c8c7c46b1508` | built by `tools/garden_bodies.py` (amdusias_skin) |
| `textures/entity/amdusias.png` | `originals/Edited-2026-10-18-Part2/cnb/textures/armor/flower_crown.png` | `61b8e6fa10914bddc7f091d7fca62f488230b488b2d4c35648a8ef7e10b1c1c4` | built by `tools/garden_bodies.py` (amdusias_skin) |

**Not confirmed** (no build, no client), that each of these compiles and behaves as read. Read in the 26.3 sources:
`Hud.subtitleOverlay` (private: the client test reads it by reflection by that name, which holds only where the
running game keeps it), `Gui.hud`, `SubtitleOverlay.onPlaySound`, the
8-argument `SimpleSoundInstance`, `Registry.getValue(Identifier)`, `CompoundTag.getIntArray` returning
`Optional<int[]>`. Taken from the repository's own uses: `BlockEntity.saveWithFullMetadata`,
`EncyclopediaClient.key`/`sequence`/`navigator`, `TestPlayers.leave`, `PactKeeping.suspend`/`resume`/`end`,
`PactAccords.matter`, `GardenReach.refreshes()`, `MAX_BEDS` and `nearest(BlockPos)`, `TestClaims.block`,
`PactReading.presence(level, spirit, now)` and `List.getLast()`. Not confirmed at all: that the three test classes and his
two new main classes compile; that the drawn words of his terms page hold the chip's "Max 16 beds" and the gauge's
"/16 now"; that `ServerPlayer.teleportTo` 100 blocks off behaves in the lapse test as read; that two holders in one party
may each cover the other's bed (Authority with no claims); that `Sign.WORK` reads well as his tree's sway; that the
encyclopedia's ray hits the bed under the crop in the client yard; and that budding amethyst gives exactly 1 stir each
there.

**Not covered:** everything that needs a build: compiling, every game test above, the client flow and its pictures;
people; sound (CI has none: the horn's subtitle is staged there, and the other cues' subtitles are checked as defined and
fired); two clients at his perch (the garden's two-client test is unit 7's); a player's hardware; a long unattended
garden under his canopy on a real server; his astir loop (not photographed, as Buer's, Bathin's, Marax's and Foras's).

## World and event applicability

Overworld, Nether and End alike (a perch, its log and the beds in one level; his area never crosses levels; the canopy
registry is per level). No seasonal gate. No cave, dungeon, boss or loot role.

## Rollout and open questions

Ships with the garden cohort's unit 6 (GC-09), behind the Concordance's switch; built, not yet compiled or run in CI,
not merged. Open: his line is to be merged with unit 5's (Bifrons) in the order 3, 4, 5, 6 (the contract's
[Where to resume](concordance-garden-cohort.md#where-to-resume) lists what that merge resolves by hand); then every test
and shot of his is to run in CI (the server jobs, the garden client class and a dispatch of every client class), his
pictures to be looked at and this record given their "Seen" column. Questions for the cohort's checkpoint: his gauge's
"now" (above); his height of 2.4 against the contract's 2.0; his sway as the shared work sign; his body built rather than
the owner's; his brief, which the owner has not approved. Known limits are [above](#known-limits).
