# Marax's pact (garden cohort, GC-06)

Concordance garden cohort, GC-06 ([contract](concordance-garden-cohort.md#gc-06-marax-unit-3); Dossier 3). Marax, the
twenty-first spirit, keeps a familiar that knows the ground: asked at his perch, it reads its holder's planted Verdant
Beds from what they last showed and names the one change that would help the worst of them, changing nothing. He is the
cohort's third garden spirit, on the same pact record, perch, screen, roster, journal and Jade lines as Stolas, Buer and
Bathin, and the first to stand on the ground beside the perch instead of on it.

Status: **built, not yet compiled or run in CI, not merged** (Concordance garden cohort, unit 3: brief `736d94541`; the
shared prefix GC-06b `8cf0ef994` with its review's fixes `ee362c3cd` and `cc9db6afa`; Marax `bed9f73b4`; his review's
fixes `1473e99f1`; these records). The shared prefix was in the head that CI run
[40777920181](https://github.com/jimbozoomer-byte/jugcraft/actions/runs/40777920181) tested (`8af703330`), a run that
passed in full; nothing of Marax himself (`bed9f73b4` on) has been compiled, and none of his server tests or client
shots has run ([Verification](#verification)). Every Java file of his was parse-checked only; the repository's Python
checks and audits ran on his files. No person has played him. This page began as his brief (GC-06a), committed before
any body file as the contract's per-spirit template asks (D-D5); the sections after "Dependencies and assets" describe
him as built.
Proposal issue: none; the owner's selected scope ([the cohort's contract](concordance-garden-cohort.md), GC-D1 to GC-D3).
Owner: @jimbozoomer-byte. AI-assisted implementation with Claude Code; the model is named in the commit trailers.
Target milestone and tier: the garden cohort's unit 3; Practitioner stage, Spiritbinders (tier: practitioner).
Primary specialty and supported player role: the Binding Arts with the garden's keepers, for a grower who wants to know
what holds a crop back.
The proposals GC-P60 to GC-P62 are the contract's and GC-P63 to GC-P79 the unit's ([Proposals](#proposals-gc-p60-to-gc-p79);
[Decisions](concordance-garden-cohort.md#decisions)); where the restyled pact screens came after his specification, the
choices are the session's ([Decisions recorded with the code](#decisions-recorded-with-the-code)). None is the owner's.

## Player experience

As built; none of it has yet been seen in a game.

- Understand **The Garden's Regard** (the codex's Binding category). Its entry has four pages of his, "Marax", "His
  Reading", "The Proud Familiar" and "His Price", and, after them, its last page, "Mastered".
- Plant an organism crop on an awake **Verdant Bed** of yours within 8 blocks of a **Pact Perch** you placed, and use the
  perch. Its chooser shows a portrait card for each spirit you may invite, his "Ready", or "Not yet" with "Needs a crop
  near". With nothing planted he does not come: "My familiar reads what grows. Nothing grows here." Nothing is spent.
- Chosen, a low, mossy bull with a calm man's face **treads up beside the perch**, two blocks out on the first clear side
  a quarter turn clockwise from you (never between you and the perch; with no side clear, on the perch), facing you, a
  small leaf-winged familiar between his horns. Three short beats (a Terms skip on the first two), then his terms as chips:
  "Names one change for your worst bed", Any hour, 8 blocks, 2 a day, 2 paper every 7 days with 4 ahead, a day's grace,
  Only you, and the standing terms; the price as stacks, **1 book, 2 Sunpetal and 2 Dewmoss**, each you lack marked before
  Accept. Accepting spends them once (no astrolabe, no resonance).
- **His reading.** On his kept page, press **Ask him** ("2 left today"). For you alone, at any hour, standing in his area
  while the pact is active, his familiar reads the nearest 8 of your planted beds in his area that it may read, from what
  each last showed (it takes no sample), and names for the worst one change: the one that alone would raise that crop a
  step, or the first of its faults ("first of 2"), with how to make it and, for a Sunpetal the Dewlit graft would lift, the
  graft. The page draws the change short ("Nutrients 0 → 1"); the whole sentence is its hover, its More and its narration.
  While the answer is on screen the familiar flies from his horns to that bed and hovers over it, he turns to watch it
  go, and the bed shows the shared sign of work done. It changes nothing. Two readings a game day; only a change named
  counts toward them.
- **The proud familiar.** A bed it has named for the same fault at three readings running, nothing changed there, it
  will not read a fourth time until something there changes: it reads the next-worst instead and says so, and his page
  shows the bed as "x, y, z skipped till changed" with an hourglass. When every crop it may read is thriving, or left
  proud, the answer is "Every crop is thriving" (or "Nothing new to correct"), with his nod, and nothing is counted.
- By night, between readings, he lifts his head to the sky. His first task: use a Verdant Bed's side in his area with an
  empty hand while the pact is active (its top, where a crop grows, is the crop's). With Buer's and Bathin's first tasks it
  masters The Garden's Regard ("Kept Three Ways").
- Two sheets of paper every 7 game days (up to 4 weeks paid ahead); a missed week is warned, a game day's grace follows,
  then the pact is suspended with nothing lost; an offering resumes it. Ending at the perch (asked first) or from the
  roster returns what was paid ahead once; his familiar settles between his horns, and he turns, walks two steps and
  fades.

## Marax's character

The twenty-first spirit (1904 heading "Marax"; Weyer and Scot spell him Morax). Source phrases: "a Great Earl and
President"; "like a great Bull with a Man's face"; "His office is to make Men very knowing in Astronomy, and all other
Liberal Sciences"; "also he can give good Familiars, and wise", who know "Herbs and Stones which be precious" (the words
joining those fragments unverified). **The identity rests on the familiar: the knowledge of plants is the familiar's, not
his.** Not imported: Genshin Impact's Morax (Zhongli).

- **Silhouette:** a low, massive bull about 1.4 blocks to his hump and 2.1 long, with a broad, calm, simple face (the
  man's face kept gentle, never uncanny), a mossed hump over his shoulders and pale horns; a tiny leaf-winged familiar
  rides between his horns. He stands on the ground beside the perch.
- **Personality:** slow, courteous, scholarly; defers to his familiar on plants; looks at the sky by night.
- **Beats:** greeting "I am Marax. I do not dig; I keep a friend who knows the ground."; service "Ask, and my familiar
  will read your beds and tell you one thing to change. Only one: the rest you learn by doing it."; price "Two sheets of
  paper a week. My familiar writes small." As built, held to the magic interfaces' limits (decided by the session,
  amending GC-P60): the greeting follows the stage direction "A man-faced bull treads up beside the perch." and the
  service beat is "Ask, and my familiar names one change. Only one: the rest you learn by doing it."
- **Refusal:** "My familiar reads what grows. Nothing grows here." **Departure:** the familiar settles between his horns,
  he turns, walks two steps and fades.

## Dependencies and assets

GeckoLib (approved; his model, clips, glowmask layer); Jade optional (his service line and note, behind `compat/`). No new
dependency, pin or mixin. His body is built by `tools/garden_bodies.py` (no owner rig is a bull with a man's face):
geometry and clips are code; his 128 x 64 skin and glowmask are painted only in the colours of three of the owner's
paintings, read as data and never copied or edited:

| Painting (under `art/owner-library/originals/Blocks/`) | Catalog row | SHA-256 | Colours | Used for |
|---|---|---|---|---|
| `biomes and tree blocks/mahogany_log.png` | `files.csv:1925` | `1ed1e99490963ed3137a583c259dfd1440d23a4b391f0e1b761f073106e7c180` | 5 | hide, face, horns, hooves |
| `biomes and tree blocks/spanish_moss.png` | `files.csv:2069` | `063d9313efbe9a01921461829d92eab3ecaa3c0ef6bcaf0b95c4aef06d13e3c7` | 8 | hump, forelock, tail tuft, the familiar's eyes |
| `biomes and tree blocks/cypress_leaves.png` | `files.csv:1758` | `d85109e4f50b37908248dda43511d38741b1be9780c6e554d18421320f434ae5` | 6 | the familiar's body and leaf wings |

As built (`tools/owner_art.py`, `BUILT`; GC-P71), written and checked by `tools/owner_art.py`:

| Runtime file | Built by | What it holds |
|---|---|---|
| `geckolib/models/entity/marax.geo.json` | `marax_model` | 16 box-UV cubes on 12 bones (`root`; `body`, with the torso and the moss hump; `head` and `horns`; `tail`; four legs; `familiar`, a root child, with `familiar_wing_left` and `familiar_wing_right`), 10 UV regions (the four legs share one, the horns' two pairs two, the wings one), a 128 x 64 sheet; 1.125 blocks wide, 1.44 to the hump and 2.09 long without the familiar |
| `geckolib/animations/entity/marax.animation.json` | `marax_animations` | six loops (idle 4.0 s, listen 3.0, waiting 6.0, suspended 8.0, astir 1.5, stargaze 6.0) and six gestures (arrive 2.0, inspect 2.0, assent 1.5, refuse 1.6, work 2.0, depart 2.0, held on its last frame) |
| `textures/entity/marax.png` | `marax_skin` | 15 opaque colours, every one a painting's: all five of the mahogany log's, seven of the Spanish moss's eight, three of the cypress leaves' six |
| `textures/entity/marax_glowmask.png` | `marax_glowmask` | the familiar's two eyes only, `(91, 39)` and `(93, 39)` |

Colours only are read; no texel, shape or layout of the paintings is copied. His rows of
`python3 tools/owner_art.py --provenance pacts`, as printed for this record:

| Runtime file | Source | SHA-256 | How |
|---|---|---|---|
| `geckolib/animations/entity/marax.animation.json` | built from code alone | | built by `tools/garden_bodies.py` (marax_animations) |
| `geckolib/models/entity/marax.geo.json` | built from code alone | | built by `tools/garden_bodies.py` (marax_model) |
| `textures/entity/marax.png` | `originals/Blocks/biomes and tree blocks/mahogany_log.png` | `1ed1e99490963ed3137a583c259dfd1440d23a4b391f0e1b761f073106e7c180` | built by `tools/garden_bodies.py` (marax_skin) |
| `textures/entity/marax.png` | `originals/Blocks/biomes and tree blocks/spanish_moss.png` | `063d9313efbe9a01921461829d92eab3ecaa3c0ef6bcaf0b95c4aef06d13e3c7` | built by `tools/garden_bodies.py` (marax_skin) |
| `textures/entity/marax.png` | `originals/Blocks/biomes and tree blocks/cypress_leaves.png` | `d85109e4f50b37908248dda43511d38741b1be9780c6e554d18421320f434ae5` | built by `tools/garden_bodies.py` (marax_skin) |
| `textures/entity/marax_glowmask.png` | `originals/Blocks/biomes and tree blocks/mahogany_log.png` | `1ed1e99490963ed3137a583c259dfd1440d23a4b391f0e1b761f073106e7c180` | built by `tools/garden_bodies.py` (marax_glowmask) |
| `textures/entity/marax_glowmask.png` | `originals/Blocks/biomes and tree blocks/spanish_moss.png` | `063d9313efbe9a01921461829d92eab3ecaa3c0ef6bcaf0b95c4aef06d13e3c7` | built by `tools/garden_bodies.py` (marax_glowmask) |
| `textures/entity/marax_glowmask.png` | `originals/Blocks/biomes and tree blocks/cypress_leaves.png` | `d85109e4f50b37908248dda43511d38741b1be9780c6e554d18421320f434ae5` | built by `tools/garden_bodies.py` (marax_glowmask) |

His cues play four runtime sounds already in the repository, copied unchanged from the owner's library (the art plan's
earth family, `nouveau.gaia_family`), told apart by pitch:

| Runtime file (`assets/jugcraft/sounds/`) | Library source (`art/owner-library/originals/Magic/`) | Catalog row | SHA-256 | Cues |
|---|---|---|---|---|
| `nouveau/gaia_1.ogg` | `assets/ars_jimbaux/sounds/gaia_1.ogg` | `files.csv:20377` | `236c4f69eca1bf904cea9257562483cbf5327d84a668242c7503e985c949f0ae` | arrive; depart at 0.85 |
| `nouveau/gaia_2.ogg` | `assets/ars_jimbaux/sounds/gaia_2.ogg` | `files.csv:20378` | `7558cd611e6103be9268777dbbbdffea350531a644320e9c756e4fbdd2fef318` | listen at 1.3; refuse at 0.7 |
| `nouveau/gaia_3.ogg` | `assets/ars_jimbaux/sounds/gaia_3.ogg` | `files.csv:20379` | `7845aa4bf0046b56b8895953817553eba46034e6916d5d40e8a9e9dc0a3d74e2` | inspect; work at 1.5 |
| `nouveau/gaia_4.ogg` | `assets/ars_jimbaux/sounds/gaia_4.ogg` | `files.csv:20380` | `673c8c28f3f88fda25ac6fdedddc5f91822fb0d46b37c4973859a3f662d9d11c` | assent |

All four are mono, 44.1 kHz, 1.65 s (at 0.85, his departure's lasts 1.94 s, inside its 2.0 s clip). The catalog rows are
this commit's; the brief named `files.csv:20370` to `20373`, true when it was written and moved seven rows on by main's
#537, merged into this line since. His seven sound events, `jugcraft:marax.*`, are subtitled: arrive "Marax treads up",
listen "Marax listens", inspect "Marax's familiar looks it over", assent "Marax assents", refuse "Marax refuses", depart
"Marax fades", work "Marax's familiar flies to a bed" (GC-P72; `tools/concordance_pacts.py`). The library's notices are
kept with the originals; provenance as `art/owner-library/PROVENANCE.md` records it. The library is unchanged and nothing
in it is executed.

## Record fields (CLAUDE.md)

| Field | Marax's pact (as built) |
|---|---|
| Tier | Practitioner, Spiritbinders: The Garden's Regard needs the Binding Arts and Verdant Husbandry understood |
| Input producer | The Garden's Regard (a garden specimen examined, a ripe organism crop harvested by hand, a bound worker kept at its work; GC-03); the perch; the holder's planted Verdant Beds and the samples their own plants took (`VerdantBedBlockEntity.cachedHabitat`: he reads them and takes none); the compact (1 book; 2 Sunpetal and 2 Dewmoss, the garden's own crops); offerings (`#jugcraft:marax_offerings`: paper) |
| Output consumer | the holder (advice only: one change named for one bed, which the holder may make or not); the record's day count; the holder's first obligation (`READ`), one of the three distinct garden spirits' obligations that master The Garden's Regard |
| Costs | the compact once; 2 paper every 7 game days, at most 4 periods ahead; no resonance; a reading costs nothing and changes nothing |
| Unlocks | the invitation (the Regard understood and a planted, awake bed of yours within 8 blocks); habitat reading (the pact); with Buer's and Bathin's first obligations, The Garden's Regard mastered (its codex page "Mastered", the advancement "Kept Three Ways"), which opens nothing more |
| Failure behaviour | every refusal spends nothing and says why: nothing planted, no sample yet, not the holder (a stranger, a party member or a stand-in), suspended, held for its offering, outside his area, the day's two readings spent, Marax not at his perch; every crop thriving or left proud is an answer that counts nothing; a bed the holder could not change by hand, or a stranger's, is never read; a missed offering warns, gives a day's grace, then suspends with nothing lost; a lost presence is called back at no cost; a broken perch ends nothing; ending returns what was paid ahead once; switched off, nothing |
| Test evidence | the 22 server tests of `GC06B_TESTS` and `GC06_TESTS`, SG12's fifteen cases with his fixture, and his 33 client shots in `ConcordanceGardenPactClientGameTests`: **none yet run for him** (GC-06b's two tests were in the head of CI run 40777920181, which passed in full); the scratch advice harness re-run for this record ([The rule](#the-rule-gc-p64)); not played by a person ([Verification](#verification)) |

## Connections

- **Producers:** The Garden's Regard and the garden groundwork (GC-03: `GardenReach`, `GardenPacts`, `PactObligations`);
  the garden (Verdant Beds, the four organism crops, each bed's cached habitat sample and verdict, the Dewlit graft's
  data); the pact hall's perch, screen, record, upkeep, escrow and history (Chunk D, GC-01); vanilla books and paper.
- **Consumers:** the holder, who reads the advice and acts by hand (bone meal, water, light, a quiet source of magic,
  rest or compost, the Dewlit graft); the Bound Will's day count; `PactObligations` (the holder's own use of a bed); the
  roster ("Habitat reading: one change named for your worst bed, when asked"), the journal's pacts lines and Jade.
- **Shared systems:** `PactSpiritEntity` (his body, cues, gestures, facing, and since GC-06b the standing station
  `Station.BESIDE`, the synced work target `TARGET` and `showTarget`, `attendWork`), `PactSpiritRenderers` (the reaching
  bone, `Row.reach`), `PactService` and `PactServices`, `GardenReach` and `GardenPacts.target` and `presence`,
  `PactObligations` (`READ`), the ecology's `Verdict`, `Niche` and graft data with the new pure `HabitatAdvice`, the
  shared signs (`Signs.show`, `Sign.WORK`), the shared diagnostic for a copied perch, the magic interfaces' glances; no new
  registry, currency, conversion, energy, item, block or block entity (GC-S22).
- **Not required:** any other branch. Solo and Overworld: a book, Sunpetal, Dewmoss and paper are survival crafts; trading
  is not needed. Not required by anything either: his advice is help, never a gate; mastery of the Regard can wait for him
  (Buer and Bathin alone are two of three).

## Service: habitat reading (`habitat_reading`)

`spirits/HabitatReading`; holder only (`PactParser.HOLDER_ONLY`), personal; on request only (no step of its own).

- **Asked** on his kept page (the Recall action with the choice `reading`), at any hour, by the pact's holder in person
  (no stand-in), standing in his area, while the pact serves (not suspended or held) and today's two readings are not
  spent, with Marax at the perch.
- **Which beds:** his bed index (`GardenReach.of`: the holder's awake beds in his radius, loaded chunks only, nearest the
  perch first), those with an organism crop on them. Each is asked of `GardenPacts.target` (the holder near, could change
  it by hand, still a beneficiary's); a bed refused for itself (a claim, outside his area) is passed by and uses none of
  his eight; a refusal of the whole pact (suspended, held, the day spent, the holder away) ends the reading with it. At
  most `READ_BEDS` (8) beds read and `MAX_ASKED` (16) asked.
- **What it reads:** each bed's cached habitat (`cachedHabitat`): the sample its plant last took, whatever its age. A
  bed with none is skipped and none is taken (`SampleBudget` unchanged). A thriving crop is skipped; a proud bed is passed
  over. The worst growth (lowest pace) is chosen, the nearer among equals.
- **What it names:** `HabitatAdvice.one` for that crop's niche on that reading, with the grafts its bed could take now
  (none while it carries one): [the rule](#the-rule-gc-p64). The answer is one sentence (Dossier 3 item 8: never a colour
  or a particle alone), with the crop and bed, its growth, the factor's value, the value to bring it to, its ideal range,
  how, whether that alone moves it on or it is the first of n, a graft that alone would serve, and a proud bed passed
  over. The client test's: *Marax's familiar settles over the Sunpetal at x, y, z. "It is not growing: its Nutrients are
  0; feed its bed to 1 (best 3 to 32): bone meal, a Mulch Maw, or a fixer growing beside it. That alone will move it
  on."*
- **What it does:** counts the reading on the record (`taskDone`); turns him to the bed while his work gesture plays (40
  ticks), then back to his speaker; shows the bed as his work target for at most `SHOW_TICKS` (400) while the answer is on
  screen; shows the shared work sign at the bed. Nothing else: no sample, note, nutrient, moisture, strain, graft, item or
  practice changes.
- **Answers that count nothing:** `nothing_grows` (no planted bed he may read; also his refusal of an invitation),
  `no_sample` (planted beds, none sampled yet), `nothing_to_correct` (every crop read thriving, or left proud: answered
  done, with his assent), `absent` and every refusal (`not_holder`, `suspended`, `upkeep_held`, `outside_area`,
  `quota_spent`, the shared diagnostic for a copied perch).
- **First obligation (`READ`):** the holder's use of a Verdant Bed itself with an empty hand (its report) in his area while
  the pact is active; one line after `bed.report(...)` in `VerdantBedBlock.useWithoutItem`. A crop's own report is not a
  bed's and counts nothing; recorded once a player.

## The rule (GC-P64)

`ecology/HabitatAdvice` (pure: `java.util` and jspecify only, in `check_baselines`' pure package), built with the shared
prefix (GC-06b). For a crop's niche and one reading of its habitat:

- A thriving plant has nothing to correct.
- The factors holding it at its step are those outside their range while it is stalled, or only tolerable while it is
  tolerating, in `Factor` order (moisture, light, nutrients, diversity, disturbance).
- When one factor holds it, the change is the value of that factor nearest the one it has that alone raises the plant a
  step (stalled to tolerating, tolerating to thriving): "That alone will move it on". Where harvest strain decided the
  disturbance fit, easing the strain is tried first, and the strain is only ever lowered.
- When two or more hold it, no single change can: it names the first, "one of n" (at least 2), at the nearest value of
  its ideal range (the strain to 0).
- A change only ever moves a factor toward its ideal range, so a plant whose niche needs no disturbance (a Sunpetal) is
  never told to add magic, and one that needs some (a Gloamcap) is told of a quiet source.
- The graft clause: the first graft by id whose host is the plant, offered only while the bed carries none, that alone
  would raise it a step on this reading (the Dewlit graft for a Sunpetal at light 9 to 12).
- Bounded: at most `MOST_VERDICTS` (129) verdicts a call, and one a graft offered.

**Its harness (scratch, not in Git).** A brute-force harness written for the unit's design checks the rule over every
shipped niche (Dewmoss, Gloamcap, Mendvetch, Sunpetal, the Verdant Heart's) and Sunpetal under Dewlit, 524,288 readings
a niche (moisture 0 to 7, light 0 to 15, nutrients 0 to 5, 31 and 32, diversity 1 to 8, disturbance 0 to 15, strain 0 to
3): nothing named exactly while thriving; the factor named the first holding it; more only below the ideal minimum, less
only above the maximum, easing only with strain and only for disturbance; more disturbance only where the ideal minimum
is above 0; "alone" exactly when brute force over every single value of every factor and the strain finds that change
and none nearer; otherwise "one of max(2, n)" and no single change exists; a graft named exactly when its niche alone
raises the plant a step. Re-run for this record, compiled with JDK 21 against `HabitatAdvice` and the ecology classes as
committed (the niches it holds equal to the shipped organism and graft data): **30,604,024 checks, 0 failures**, 26.7 s.
Its named cases: a Sunpetal at light 9 → light 9 to 11, alone, or the Dewlit graft; a Gloamcap with no magic → a quiet
source, 0 to 1, alone; a dry, starved Sunpetal → moisture 1 to 4, one of 2; a Sunpetal at disturbance 2 with strain 3 →
ease the strain 3 to 0; at disturbance 6 → down to 5. The CI form of the same checks is the server test
`habitatAdviceHoldsForEveryOrganism` (`ConcordanceGardenGameTests`; over every catalog niche and graft and a grid of
readings, with the contract's three cases), in the head CI run 40777920181 tested, which passed in full; this record did
not read its line in that run's log.

## The proud familiar

GC-P69, as amended by his review. What the familiar remembers is his presence's memory (`HabitatReading.Memory`, held
in `PactSpiritEntity.serviceMemory`), never saved: a restart, an unload or a new presence forgets it, and nothing is owed.

- It keeps the last bed it named, the factor, the verdict and how many readings running named them. At `PIQUE` (3)
  readings running on one bed, factor and unchanged verdict, the bed is proud, with the verdict it was proud on; at most
  `READ_BEDS` (8) proud beds, the oldest forgotten.
- A proud bed is passed over while its verdict (from its cached sample) is unchanged, and the reading says so ("It will
  not repeat itself a fourth time about the bed at x, y, z"); it is forgiven when its verdict changes or it is gone,
  unplanted or unsampled. With every other crop thriving: "Nothing new to correct" (counting nothing).
- Before each reading the last bed named is read again: changed since, its count starts again, so a bed that changes and
  falls back is not proud at once (`theProudCountStartsAgainWhenTheBedChanges`).
- His kept page shows one proud bed ("x, y, z skipped till changed") or how many and the nearest, with the hourglass;
  Jade's note on his perch and on him: "Beds his familiar will not read again yet: n" (none while the pact is held).
- It is a task, not a choice: nothing waits on the holder's answer, and the roster's status is unchanged (GC-S11).

## Costs, balance and automation

He makes nothing and moves nothing: a reading changes no block, item, essence or research, takes no habitat sample and
draws on no shared budget but one bed index read. No conversion, so no economy row
(`python3 tools/concordance_economy.py`: "gaining cycles: none"). Bounded: two readings a game day (only a change named
counts), at most 16 beds asked and 8 read a reading, one `HabitatAdvice` call (at most 129 verdicts and one a graft).
His upkeep is paper, vanilla and renewable; the compact asks a book and the garden's own Sunpetal and Dewmoss. Balance
units: none of the Concordance's essences. How much a garden spirit should do is the cohort checkpoint's question 2.

## Failure behaviour, multiplayer and persistence

Server authority: a reading is the server's, on the holder's request at the perch, asked at the reading: the holder in
person (never a stand-in, a party member or a stranger), standing in his area, the pact serving, each bed through
`GardenPacts.target` (a beneficiary's, the holder online, alive, in the perch's level and within 64 blocks, and could
change it by hand). Personal: only the holder's beds are indexed, so a stranger's or a party member's bed is never read
(`maraxNeverReadsAStrangersBed`), and a bed in a claim the holder may not build in is passed by
(`maraxNeverReadsABedHeMayNotChange`, `maraxReadsPastBedsHeMayNotChange`). The work target is entity data, synced and
never saved (`theTargetIsNeverSaved`), set only while a conversation is open at his perch and cleared at his first think
after it ends, after 400 ticks, or when he leaves (`theTargetClearsWhenTheConversationEnds`). His station is kept each
think and saved nowhere: after a load he keeps the side he stands on. Bounded: one bed index read (at most 64 beds,
loaded chunks only, refreshed at most every 200 ticks), 16 asks and 8 reads a reading, two readings a day, one shared
sign. Saved: nothing of his own (his entity writes only the base's `perch` and `record`). One presence a pact: a copy, a
duplicate or a stray Marax leaves at its first think; a copied perch is inert and answers a reading with the shared
diagnostic. Stable ids once released: `jugcraft:marax`, `jugcraft:marax_pact`, `concordance/pact/marax.json`,
`#jugcraft:marax_offerings`, the seven `jugcraft:marax.*` sound events, the work `habitat_reading` (saved in a Bound
Will's service; append only), his codex pages `p12` to `p15` of `binding/garden_regard` (the entry's last page,
"Mastered", `p16` now, moves to the end as later spirits add theirs). The obligation's act `read` and his
answers' ids are words, never saved. No migration: every addition is new or appended.

## What changed in shared code

- **The shared prefix (GC-06b, `8cf0ef994`; `ee362c3cd`):** `PactSpiritEntity` gains a body's station (`Station.PERCH`,
  the default for Stolas, Buer and Bathin; `Station.BESIDE`, two blocks out on the first clear side, a quarter turn
  clockwise from the inviter first, the inviter's side last, else on the perch; GC-P67), the synced work target (`TARGET`,
  `showTarget`, cleared by the think and on leaving; GC-P65) and its flight arithmetic (`posed`, `hover`, `reach`,
  `TARGET_REACH` 24, `FLIGHT_TICKS` 10); `PactSpiritRenderers.Row` gains `reach` (a named root-child bone flown toward
  the target, its culling box widened; GC-P66); `PactScreen.workService` draws a work's on-request choices (GC-P74);
  `ecology/HabitatAdvice` (GC-P64); `spirits/GardenAsking` (Bathin's per-think questions, lifted unchanged so other
  services share them); the unit-1 and unit-2 test lines a further garden spirit would break made generic (GC-P77);
  one marker line for each of units 3 to 6 at every shared append point.
- **With Marax (`bed9f73b4`, `1473e99f1`):** `VerdantBedBlock` (the `READ` hook, one line), `PactObligations.Act.READ`,
  `PactParser.WORK`, `HOLDER_ONLY` and `REQUIRED`, `Workers` (his type, attributes, service and sounds),
  `PactSpiritRenderers` (his row, reaching with `familiar`), `PactScreen.workService` (the plate primary and drawn by its
  glance), `PactGlance` (`WORK_ICONS` a `Map.ofEntries` with his line, Star-reckoning's entry last; the proud rows'
  hourglass and icon); the codex entry's pages `p12` to `p16`; `tools/acquisition.py` and the journey test's
  `GARDEN_RECIPES` gain Dewmoss (GC-P76). No device's saved key changed.

## The pact screen

The shared screen (GC-01) on the magic interfaces' relic scroll card ([magic-interfaces.md](magic-interfaces.md), unit 1;
GC-S26), with his words all data in `tools/magic_ui.py`'s `WORK_GLANCES["habitat_reading"]`: the chooser card ("Ready",
or "Needs a crop near"); the stage direction and three beats under a gilded portrait header, with a Terms skip; his
terms as chips, the service line "Names one change for your worst bed", every term's whole sentence its hover and
narration, the price as stacks (1 book, 2 Sunpetal, 2 Dewmoss); the kept page with today's readings as a gauge ("0/2
today"), the sockets of what is paid ahead, "No reading yet: ask below" (only while none was given today) or "Last:
x, y, z, Factor", a proud bed's row with the hourglass, "First task: use a Verdant Bed's side by hand here", the primary
plate **Ask him** ("2 left today", or "None left today"; its whole name, "Ask Marax for a reading", its message) and
Suspend, End and Close; the reading's answer drawn as its change ("Nutrients 0 → 1", ", first of 2" when one of
several) with the whole sentence as its tip, More and narration; every other answer in at most six words ("Nothing grows
here", "Let your crops grow first", "Every crop is thriving", "Done for today"). Keyboard only at GUI 2, GUI 4 and
320 x 180. Word budgets, by the session's estimate from the generated words, not measured (no client ran): his kept page
with a last reading, a proud bed and his first task about 44 of its 45; a reading about 43 of 45 (about 45 of 48 at the
compact sizes). Two states are not photographed and are estimated over budget: the answer shown just after the third
reading names a bed proud (about 47) and a different bed named while another stays proud (about 50). The roster is not yet restyled (the magic interfaces'
unit 2), and Jade's short form of his note waits for that line too ([Rollout](#rollout-and-open-questions)).

## Decisions recorded with the code

These were made after the brief above; the contract's [GC-06 As built](concordance-garden-cohort.md#gc-06-marax-unit-3)
records the rest. Each is the session's, reviewable at the cohort's checkpoint; none is the owner's.

- **Departures from the contract's GC-06 (as its specification proposed them):** his station is two blocks out, a
  quarter turn clockwise from the inviter first (GC-P67, amending GC-P5's "first free side cell toward the inviter"): a
  2.1-block bull in the cell between perch and player would stand in front of the perch and take the player's clicks on
  it. The one change is the nearest value that alone raises the crop a step, harvest strain first where it decided the
  disturbance fit (GC-P64): a factor's ideal edge is not always the change that raises it. The graft clause is offered
  only for a bed carrying no graft. `HabitatReading.planted` is public for the tests. The `READ` hook stands after the
  bed's report, as the contract says; a crop's own report counts nothing (GC-P70).
- **The restyled screens came after his specification** (decided by the session, `bed9f73b4`): his beats keep to the
  limits (at most 18 words a line, 8 for the stage direction): the greeting and price are the contract's words, the
  service's first sentence is shortened, keeping "Ask" and "Only one: the rest you learn by doing it." (16 words), and the
  stage direction is a key of its own; his service line, rows, blockers, chooser reason, gauge, first task and answers are
  work glances; the reading takes its remedy and clause as arguments of their own (`said(crop, bed, growth, remedy,
  clause, graft, proud)`, the keys `advice.alone` and `advice.one_of`), so the kept page draws the change and keeps the
  sentence as its tip; the Recall plate draws its glance, "Ask him", its message the choice's whole name, primary since his
  review; his reading's sign is the shared work sign at the bed, not the specification's six `ENCHANT` particles
  (`check_signs` sends server particles only through `Signs.show`).
- **A copied perch** answers a reading with the shared diagnostic (`Code.CONFLICTING_AGREEMENT`), as Star-reckoning does,
  not a key of his own.
- **His review's fixes (`1473e99f1`):** a bed he may not read uses none of his eight (`MAX_ASKED` 16); every crop
  thriving is an answer given, with his assent, still counting nothing; the proud count starts again when the bed changes;
  "No reading yet" only while no reading was given today; the proud and last rows drawn short; his first task drawn "use a
  Verdant Bed's side"; "awake" in his blocker and the perch's tooltip; the reading's quotation ends with a full stop; each
  codex page at most 60 words (56, 60, 59 and 57 split on spaces, the mastered page 32); the chooser's word budget grows
  with its cards (4 and 7 a card, recorded in [magic-interfaces.md](magic-interfaces.md)).
- **The proud picture** (`jugcraft_garden_marax_proud`): three readings cannot be asked through the client in one game
  day (two a day), so the test makes the bed proud with two server-side readings at the next day's start, ends his work
  facing at once (`attendWork(poor, 0, now)`) so the facing pictures after it still hold, and photographs the page opened
  again, with no answer line.

## Proposals GC-P60 to GC-P79

The unit's proposals as built (each row in full, with its amendments, in the contract's
[Decisions](concordance-garden-cohort.md#decisions)); none is the owner's.

| Id | As built | Status |
|---|---|---|
| GC-P60 | His beats as Dossier 3 words them, the service beat shortened and a stage direction before the greeting (GC-S26's limits) | Contract's; amended by the session |
| GC-P61 | Radius 8, any hour, 2 readings a day, personal; 2 paper every 7 days; compact 1 book, 2 Sunpetal, 2 Dewmoss; 8 beds a reading; pique after 3 | Contract's |
| GC-P62 | The first-fault rule ("one of n"); the synced `TARGET`; the familiar's bone hook, its reach and culling box to `TARGET_REACH` (24, amended from 8) | Contract's; amended unit 3 |
| GC-P63 | Grace 1 day, cap 4; sign `minecraft:writable_book`; box 1.6 x 1.4, eyes 1.0 | Proposed |
| GC-P64 | `HabitatAdvice.one`: [the rule](#the-rule-gc-p64) | Proposed |
| GC-P65 | `TARGET` set only through `showTarget` while he listens; cleared after the conversation, 400 ticks or leaving; a 10-tick flight to 2.5 blocks over the bed | Proposed |
| GC-P66 | `Row.reach` and the render data `jugcraft_pact_reach` and `jugcraft_pact_flight`; the bone lerped toward `posed(...)`, its culling cube at the reach point | Proposed |
| GC-P67 | `Station.BESIDE`, two out, a quarter turn clockwise first; none clear, the perch; no saved key | Proposed |
| GC-P68 | Which bed: planted, his index's, nearest first, 8 read and 16 asked; cached samples only; the worst, the nearer among equals; only a change named counts | Proposed; amended by his review |
| GC-P69 | The proud familiar, never saved; the count starts again when the bed changes | Proposed; amended by his review |
| GC-P70 | `Act.READ`, its hook after `bed.report`; drawn "use a Verdant Bed's side" | Proposed |
| GC-P71 | His body built by `tools/garden_bodies.py`: 16 cubes on 12 bones, the familiar a root child, twelve clips | Proposed |
| GC-P72 | Seven cues on the four gaia files, by pitch, each subtitled | Proposed |
| GC-P73 | His night look, `stargaze`, while the client's sky is dark; cosmetic | Proposed |
| GC-P74 | A work's on-request choices as plates on the kept page; "Ask him", primary | Proposed; amended by the session |
| GC-P75 | Mastery lands here: the codex's last page "Mastered", the journey's step, `theGardensRegardIsMasteredThroughThreeSpirits` | Proposed |
| GC-P76 | Dewmoss in `GARDEN_PACTS` and `GARDEN_RECIPES`, in unit 4's words | Proposed |
| GC-P77 | The forced unit-1 and unit-2 test lines generic over the shipped garden spirits | Proposed |
| GC-P78 | His flow in the one garden client class after Bathin's (33 shots), `WEIGHTS` 820; `GC06_TESTS` (20) and SG12 | Proposed |
| GC-P79 | The shared-line register for units 4 to 6, each unit's lines under its own marker | Proposed |

## Verification

**Not run in CI.** Nothing of Marax himself has been compiled: no Minecraft jar was available where he was built, so
every Java file of his was only parse-checked, and none of the tests and shots below has run. CI is still to run them;
each row's evidence is what will show the item, not a result. The shared prefix (GC-06a, GC-06b and its review's fixes)
was in the head CI run [40777920181](https://github.com/jimbozoomer-byte/jugcraft/actions/runs/40777920181) tested
(`8af703330`), which passed in full (both server jobs passed all 3,206 required tests); GC-06b's two tests are among the
tests of that tree, and `HabitatAdvice`, `PactSpiritEntity` and both tests' bodies are unchanged since; their own lines in
that log were not read for this record. Each brief item and the evidence named (the contract's GE9):

| Brief item | Evidence (server game tests in `ConcordanceWorkerGameTests` unless named; shots of `ConcordanceGardenPactClientGameTests`) | Result |
|---|---|---|
| 1. Identity | his body files from `tools/owner_art.py` (`--check`), `check_spirit_bodies` (bones, clips and their lengths, the familiar's eye texels, the silhouette rule for every pair of spirits, no flat cube); his poses from his front (shots below) | the audits pass locally; shots not run |
| 2. Invitation and route | `maraxRefusesWhereNothingGrows`, `maraxStandsBesideThePerchFacingHisInviter`, `aStandingSpiritFallsBackToThePerchWithNoFreeSide`, `aPerchOffersOnlyWhatThePlayerMayInvite` (generic, GC-P77); `ConcordanceProgressionGameTests.theGardensRegardIsMasteredThroughThreeSpirits`; `tools/concordance_journey.py --check` (the garden pacts' steps 10 to 13); shots `jugcraft_garden_marax_nothing_grows`, `_chooser` | not run; the journey check passes locally |
| 3. Service | `aReadingNamesOneCorrectionAndChangesNothing`, `aReadingNeverAdvisesDisturbanceToASunpetal`, `aReadingAdvisesAQuietSourceForAGloamcap`, `aReadingSkipsBedsWithoutACachedSample`, `aReadingWithTwoFaultsNamesTheFirst`, `maraxReadsPastBedsHeMayNotChange`; `ConcordanceGardenGameTests.habitatAdviceHoldsForEveryOrganism`; the scratch harness above; shots `jugcraft_garden_marax_reading` (three sizes), `_familiar` | not run (the harness passed for this record) |
| 4. Upkeep and suspension | SG12's `everySpiritSealsOnceAndSpendsOnce`, `everySpiritsUpkeepWarnsGivesGraceSuspendsAndResumes`, `aStandInNeverAnswersAnySpiritsUpkeep` (his offering count 2); `onlyTheHolderMayAskForAReading`; `noGardenTermOrRefusalNamesAnAstrolabe`; `check_acquisition` (his compact and offering reachable); shots `jugcraft_garden_marax_terms` (three sizes), `_refused`, `_kept` | not run; the audit passes locally |
| 5. Conflict: the proud familiar | `theProudFamiliarSkipsABedNamedThreeTimes`, `theProudCountStartsAgainWhenTheBedChanges`, `maraxsReadingSaysItsNoteAndNamesNoPlayer`, `theTargetIsNeverSaved` (the pique unsaved); shot `jugcraft_garden_marax_proud` | not run |
| 6. Refusal and departure | `maraxRefusesWhereNothingGrows`, `everySpiritsRefusalSpendsNothing`, `maraxAnswersEachActionWithHisGesture`, `endingAnySpiritsPactReturnsTheEscrowOnce`; shots `jugcraft_garden_marax_depart`, `_end_confirm`, `_ended` | not run |
| 7. Interaction details | (a) his night look: shot `jugcraft_garden_marax_stargaze` (the client's sky at midnight), `MaraxEntity.loop` in `maraxAnswersEachActionWithHisGesture`; (b) the familiar's flight: `theFamiliarHookReachesItsTargetAtEveryBearing` (GC-06b), `theTargetClearsWhenTheConversationEnds`, `aReadingNamesOneCorrectionAndChangesNothing` (the target is the bed named), shot `jugcraft_garden_marax_familiar` (frozen, the familiar over the bed); facing: `maraxStandsBesideThePerchFacingHisInviter`, shots `jugcraft_garden_marax_faces_east`, `_south`, `_west`, `_north` | not run |
| 8. Evidence | the client class keyboard-only at GUI 2, GUI 4 and 320 x 180; *ownership:* `onlyTheHolderMayAskForAReading`, `maraxNeverReadsAStrangersBed`, `maraxNeverReadsABedHeMayNotChange`, `everySpiritWaitsForANearHolder`; *save:* `everySpiritsKeysSurviveASave`, `theTargetIsNeverSaved`; *no duplication:* `aReadingNamesOneCorrectionAndChangesNothing` (nothing changes, no sample taken), `onePresencePerPactForEverySpirit`, `aCopiedPerchIsInertForEverySpirit`; *first obligation:* `maraxsFirstObligationIsABedReadByHand`; *Jade:* `maraxsReadingSaysItsNoteAndNamesNoPlayer`, shot `jugcraft_garden_marax_jade` | not run |

**Server tests written for him** (22; held by `tools/check_mod_data.py`): `GC06B_TESTS`,
`ConcordanceWorkerGameTests.theFamiliarHookReachesItsTargetAtEveryBearing` and
`ConcordanceGardenGameTests.habitatAdviceHoldsForEveryOrganism`; `GC06_TESTS`, nineteen in
`ConcordanceWorkerGameTests` (the contract's eleven there, `maraxStandsBesideThePerchFacingHisInviter`,
`aStandingSpiritFallsBackToThePerchWithNoFreeSide`, `theTargetClearsWhenTheConversationEnds`,
`maraxsReadingSaysItsNoteAndNamesNoPlayer`, `maraxAnswersEachActionWithHisGesture`, and from his review
`maraxNeverReadsABedHeMayNotChange`, `maraxReadsPastBedsHeMayNotChange` and `theProudCountStartsAgainWhenTheBedChanges`)
and `ConcordanceProgressionGameTests.theGardensRegardIsMasteredThroughThreeSpirits`. SG12's fifteen shared cases run for
him through his fixture (a ripe Sunpetal on an awake bed of the player's beside the perch), and
`noGardenTermOrRefusalNamesAnAstrolabe` and `talkingTeachesNothing` for every offered spirit. Not run.

**Client** (`./gradlew runClientGameTest -PclientTests=ConcordanceGardenPactClientGameTests`): his flow after Bathin's,
at the same perch; 33 shots, `jugcraft_garden_marax_` + `nothing_grows`, `chooser`, `conversation` and `terms` (each at
three sizes), `refused`, `kept`, `reading` (three sizes), `familiar`, `proud`, `jade`, `idle`, `listen`, `suspended`,
`waiting`, `stargaze`, `inspect`, `assent`, `refuse`, `work`, `depart`, `arrive`, `faces_east`, `_south`, `_west`,
`_north`, `end_confirm` and `ended`, each held to its word budget. Not run; none of its pictures exists yet.

**Local** (this record's commit; no Minecraft build here): `python3 tools/owner_art.py --check`, `python3 -B
tools/check_mod_data.py`, `python3 scripts/check_repository.py`, `python3 tools/concordance_journey.py --check`,
`python3 tools/concordance_economy.py` ("gaining cycles: none"), `python3 tools/check_icon_maps.py`,
`node --test tools/world-designer/model.test.cjs`, `python3 -m unittest discover -s scripts/tests` and `git diff --check`,
each run on this commit's tree and passed; `python3 tools/owner_art.py --provenance pacts` printed his rows above; the
advice harness re-run as above. This commit changes no generated file. Earlier local checks and audit mutations are in
each commit's message (GC-06b: 23 caught; its review's fixes: 15; GC-06: X1 to X29 and twelve more caught, X30 left to
CI as its specification expects; his review's fixes: 19 of 21, the chooser's budget formula and the reading's full stop
held by no audit). No Minecraft build, game test or client test ran here.

**Not confirmed** (no build, no client): the actual words drawn on screen, and that the kept page draws "Nutrients 0 →
1"; the client under `tick freeze` while the window is resized and the page settles; `isDarkOutside` on the client after
`time set`, which the stargaze and idle pictures rest on; the light and sky in the test structure the night look
assumes; `PactSessions.live`'s timing that clears the target; whether the four facing pictures frame him; the APIs his
commits name as checked only against the repository's uses and the 26.3 sources (`Map.ofEntries` with `Map.entry`
inferred as `Map<String, String>`, the `MagicButton(Look, Component, Component, OnPress)` constructor from `PactScreen`,
`ServerLevel.getOverworldClockTime()` from the client test, `attendWork(pos, 0, now)` ending his work facing at once, and
whether 26.3's `PlayerBlockBreakEvents.BEFORE` claim stops `Authority.mayChange` for the claimed beds of his two claim
tests as it does in Buer's).

**Not covered:** people; sound (CI has none: the subtitles are checked as defined and fired); two clients at his perch
(the garden's two-client test is unit 7's); the narrator heard; a player's hardware; his astir loop (not photographed,
as Buer's and Bathin's); the two over-budget page states above.

## World and event applicability

Overworld, Nether and End alike (a perch and planted beds in one level; his area never crosses levels). His night look
reads the client's own sky. No seasonal gate. No cave, dungeon, boss or loot role.

## Rollout and open questions

Ships with the garden cohort's unit 3 (GC-06), behind the Concordance's switch; built, not yet run in CI, not merged.
Open: every test and shot of his is still to run in CI, and his pictures to be looked at; his body is built rather than
the owner's (no owner rig fits; the cohort's checkpoint question 1); whether a reading a day or two is the right measure
(question 2); the two kept-page states estimated over budget; Jade's short form of his note ("Proud beds: %1$s", the
hourglass) waits for the merge of the magic interfaces' unit 2, whose short-form tables are not on this line; his reading's
sign is the shared work sign, not particles of his own; his astir loop is not photographed.
