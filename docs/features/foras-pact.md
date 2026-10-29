# Foras's pact (garden cohort, GC-07)

Concordance garden cohort, GC-07 ([contract](concordance-garden-cohort.md#gc-07-foras-unit-4); Dossier 4). Foras, the
thirty-first spirit, keeps grafts: standing beside his Pact Perch, he grafts the host crops of his holder and their
party with donors from a store touching the perch, one graft at a time, at most 8 a day, and only where the graft helps
until told otherwise. He is the cohort's fourth garden spirit (Marax, the third, is built beside him in unit 3), on the
same pact record, perch, screen, roster, journal and Jade lines as Stolas, Buer and Bathin, and the first to spend an
item for his work: one donor from the store for each graft.

Status: **built, not compiled and not run in CI** (Concordance garden cohort, unit 4: brief GC-07a, the graft in one
place and the bed index's stores GC-07b, and Foras himself GC-07, each a commit on unit 4's line, then the fixes from
his review; no Minecraft build was possible where it was written). Nothing of Foras has been compiled, run as a game
test or photographed: the line's last CI result is run 40782439981, green in full on `d7185041b`, which this line
descends from, and nothing on this line has run in CI. No person has played him. This page began as his brief (GC-07a),
committed before any of his body files (D-D5); the sections after "Dependencies and assets" describe him as written.
Where it goes beyond Dossier 4 (the greeting's stage direction, how his body is built, which cue plays which file at
what pitch, the short words the screens draw) it is unit 4's proposal (GC-P80 to GC-P99, added to the contract's
Decisions when the unit's records land), and two things that came after the unit's design are followed here: his station
is unit 3's (GC-P67, in the shared prefix GC-06b), and his words keep to the magic interfaces' limits (GC-S26).
Proposal issue: none; the owner's selected scope ([the cohort's contract](concordance-garden-cohort.md), GC-D1 to GC-D3).
Owner: @jimbozoomer-byte. AI-assisted implementation with Claude Code; the model is named in the commit trailers.

## Foras's character

The thirty-first spirit (1904 heading "Foras"; Weyer, 1563, spells him Forcas; not Furcas, the fiftieth). Source phrases
[S]: "He is a Mighty President, and appeareth in the Form of a Strong Man in Human Shape"; "He can give the
understanding to Men how they may know the Virtues of all Herbs and Precious Stones"; he "teacheth the Arts of Logic and
Ethics in all their parts"; he makes men "to live long"; he can "recover things Lost". **The identity rests on "to live
long" and "recover things Lost"** (Jugcraft adaptation: a graft's long life on its bed, and a graft lost to replanting
recovered), with "Logic and Ethics" giving his objection. "The Virtues of all Herbs and Precious Stones", shared with
Stolas, Bathin and Marax, is not used (GC-S1). No source quotation in game (GC-S14).

- **Silhouette:** a tall, broad, stooped elder about 2.2 blocks tall and 1.2 across, in a robe of bark and moss, a
  satchel of cuttings at his hip, long arms, the owner's acorn cap on his head (the cap players already wear as the
  Companion Acorn Hat: the same texture). He stands on the ground beside the perch.
- **Station:** unit 3's standing station (`PactSpiritEntity.Station.BESIDE`, GC-P67, in the shared prefix GC-06b), with
  no station code of his own: on the ground two blocks out from the perch, on the first clear side a quarter turn
  clockwise from whoever he comes for, then the opposite side, then counter-clockwise, their side last; a side is clear
  when the cells out to it are loaded and they, and the cells over them up to his height (three for his 2.2), have no
  collision shape, and the outer cell's floor is sturdy; with no side clear, the perch's top. He keeps the side he
  stands on while it stays clear and turns to face whoever speaks to him (GC-02). Unit 4's GC-P89 now says only this
  (the unit's design had a station of its own, one block out, which the shared prefix replaced).
- **Personality:** deliberate; argues kindly; a keeper who hates waste; asks why before he does anything.
- **Beats** (GC-P80, held to the magic interfaces' limits, GC-S26: at most 18 words a line, a stage direction of at most
  8): stage direction "A bark-robed elder tips his acorn cap."; greeting "I am Foras. I keep what would be lost, and I
  argue with whoever wastes it." (Dossier 4's); service "Leave cuttings in the store beside me. I graft hosts that would
  gain, and regraft any lost." (17 words: Dossier 4's line, 27 words, shortened); price "A honeycomb a week. Long
  keeping wants something sweet." (Dossier 4's). With his name, his service's name, Terms and Leave, his first beat
  would draw 28 words of the beat page's 30 (counted as Buer's first beat draws 26). The unit's design predates the
  limits: it wrote a 24-word stage direction ("An elder in a robe of bark and moss stands beside the perch, an acorn cap
  on his head, and tips it to you.") and Dossier 4's whole service line.
- **Refusal:** "Where shall I keep the cuttings? Set a store beside me." (no plain container touches the perch; the
  unit's design adds "I see nothing here a cutting would help." for no host crop). **Departure:** he bows, folds his
  satchel and fades.
- **Details:** (a) his acorn cap tips at his greeting and at each graft (its own bone, in his arrive and work clips
  only); (b) he counts aloud: after each graft his holder's action bar says "Foras: 3 of 8 grafts today.", and his kept
  page's gauge draws the same count, "3/8 today" (the contract's "assent line" read as the line he says at each graft).
- **His objection:** hosts a graft would not help he names, and asks whether to graft them anyway (a Dewlit graft
  narrows the moisture a Sunpetal bears: on a dry bed it would harm); the answers are only where it helps (`prudent`,
  the default) and every host (`always`), kept as the perch's accord.
- **Not imported:** the entry's doubtful "invincible [or invisible]" and its "to be eloquent"; any seal, conjuration,
  title or legion count; the identity of the Fable Mojos, whose runtime sounds his cues reuse under events of his own;
  any modern adaptation's design.

### His words on the magic interfaces (GC-S26)

The unit's design predates the magic interfaces. As they require, each of his server sentences stays whole as the
message, narration and hover of what draws it, and the screens draw short glances, his work's row in `tools/magic_ui.py`
`WORK_GLANCES` ("Graft keeping", its icon `jugcraft:dewmoss`, its line in `PactGlance.WORK_ICONS`). Proposed, and fixed
with the code, where `check_magic_ui` holds them (each measured here with that module's own word and width counts):

| What | Drawn | Words |
|---|---|---|
| The header's service line | "Grafts your hosts, one Dewmoss each" (the host and what each graft costs) | 6 (191 px of the header's 211) |
| Today's gauge (detail b) | "%1$s/%2$s today" ("3/8 today") | 2 |
| Blockers (no store; no host), each a lock line | "Needs a chest or store touching the perch"; "Needs a Sunpetal on your bed within %1$s blocks" | 8; 9 |
| Chooser card reasons (no store; no host) | "Store needed"; "Host needed" (the brief proposed "Needs store and host": [Decisions](#decisions-recorded-with-the-code)) | 2; 2 |
| Kept-page rows (his last graft, a Sunpetal before it; no donor, a Dewmoss before it and marked waiting on you) | "Last: the host at %1$s grafted"; "No Dewmoss he may take" | 8 (with a position); 5 |
| His matter (how many, and the nearest) | "Gain nothing or worse: %1$s, nearest %2$s. Graft anyway?" | 11 (with a position) |
| His question in general (answered, and no matter raised now) | "Graft hosts that gain nothing?" | 5 |
| His answers (their own words) | "Where it helps" (`prudent`); "Graft every host" (`always`) | 3; 3 (the design's "Only where it helps" was 4) |
| His first task's row | "First task: graft a host crop with its donor by hand here" | 12, two lines at the narrowest scroll: no act glance needed |
| Jade (short forms in `SENTENCES`, marks in `SENTENCE_MARKS`) | "Hosts gain nothing" (his matter); "Would gain nothing: %1$s", "No donor", "Nothing to graft", "Unread hosts: %1$s" (his notes) | at most 4 |

## Dependencies and assets

No new dependency, pin or mixin. His body is built by `tools/garden_bodies.py` (no owner rig is a tall elder): geometry
and clips are code, the owner's acorn cap copied into the geometry; his 128 x 64 skin is painted only in the colours of
two of the owner's paintings, read as data and never copied or edited, around the cap's texels, which are copied
unchanged. He has no glowmask.

| Painting (under `art/owner-library/originals/Blocks/`) | Catalog row | SHA-256 | Colours | Used for |
|---|---|---|---|---|
| `biomes and tree blocks/fir_log.png` | `files.csv:1808` | `26eb9fba8fccfce7d07526531a5a59dd6a6253361f0575f5857e26a081c17e83` | 7 | robe, skin, satchel, feet |
| `biomes and tree blocks/spanish_moss.png` | `files.csv:2069` | `063d9313efbe9a01921461829d92eab3ecaa3c0ef6bcaf0b95c4aef06d13e3c7` | 8 | mantle, beard, brows, hem band, cuttings |

His acorn cap is the owner's, from the library's second edited part, read through `tools/owner_art.py`'s second root
(`PART2_NAME`, `_read_owner`, `_origin`: in the shared prefix GC-06b, with no reader until his body, which closes GB25):

| Owner file (under `art/owner-library/originals/Edited-2026-10-18-Part2/`) | Catalog entry (`art/owner-library/catalog/edited-2026-10-18-part2/files.json`) | SHA-256 | Change |
|---|---|---|---|
| `jims mobs/geo/item/armor/acorn.geo.json` | `files[23467]` | `913962495527ed87e4bbe2d0e5fa29a79c04200bb762c602ded852b34f658b13` | its four `armorHead` cubes copied into his `cap` bone, each moved by (0, -3, -3) onto his head; their sizes and UVs the owner's; the leaf (a cube of no thickness, `[4, 4, 0]`) written per face with the same rectangles, its clear south face (0 of 16 texels drawn) dropped; nothing else of the file used |
| `jims mobs/textures/item/armor/acorn.png` | `files[25844]` | `947202e4d51ba69035d7fef89566f6619236de79270bfdf79285d538c05db2d3` | its drawn region (x 0 to 39, y 0 to 23 of its 128 x 128) copied texel for texel to the same place in his sheet, so the cap keeps the owner's UVs; the same file is byte for byte the Companion Acorn Hat's runtime texture (`jugcraft:companion_acorn_hat`), and his sheet copies the library original, so a later change to the hat's texture does not change his |

His cues play thirteen runtime sounds already in the repository, copied unchanged from the owner's library (the art
plan's wood family): no new file is copied.

| Runtime file (`assets/jugcraft/sounds/menagerie/`) | Library source (`art/owner-library/originals/Edited-2026-10-18-Part2/`) | Catalog (`art/owner-library/catalog/edited-2026-10-18-part2/`) | SHA-256 | Header | Cue |
|---|---|---|---|---|---|
| `fable/mojo_spawn.ogg` | `legendarycreatures/sounds/mojo_spawn.ogg` | `files.json` `files[26428]`; `fable-runtime.json` `copies[85]` | `51eac68da0c4141959fbd28e86b5eec565437902fe3c0eb93971f6f93650c2a5` | mono, 44.1 kHz, 0.46 s | arrive at 0.85 |
| `fable/mojo_idle.ogg` | `legendarycreatures/sounds/mojo_idle.ogg` | `files[26427]`; `copies[84]` | `b097a1cc153bd8c2f76c21035f20da3a4cfce9bd0cde69c790a4817841cc4fb4` | mono, 44.1 kHz, 0.43 s | listen at 0.8 |
| `fable/mojo_step1.ogg` | `legendarycreatures/sounds/mojo_step1.ogg` | `files[26429]`; `copies[86]` | `1f97c35099463ab91b462b2f9c53c1b7bf07032a34f8ba2a268d0de8e6219ef9` | mono, 48 kHz, 0.54 s | inspect |
| `fable/mojo_step2.ogg` | `legendarycreatures/sounds/mojo_step2.ogg` | `files[26430]`; `copies[87]` | `9be51b739087fda2401b24d3670f00780cf6549ce5899f4da2f88c55e1296515` | mono, 48 kHz, 0.46 s | inspect |
| `fable/mojo_step3.ogg` | `legendarycreatures/sounds/mojo_step3.ogg` | `files[26431]`; `copies[88]` | `9bc79a285869047d3caee7428d0b1140e8c88b741afb3f4ea7a694a5023828d4` | mono, 48 kHz, 0.66 s | inspect |
| `fable/mojo_base_attack_hit1.ogg` | `legendarycreatures/sounds/mojo_base_attack_hit1.ogg` | `files[26422]`; `copies[79]` | `7524d2be25d4316524ddb6cf3edf942a11eb5fdd025b86f085a381faeee2d169` | mono, 44.1 kHz, 0.14 s | assent |
| `fable/mojo_base_attack_hit2.ogg` | `legendarycreatures/sounds/mojo_base_attack_hit2.ogg` | `files[26423]`; `copies[80]` | `6ec168aa78d3cee4615ab86eb78a107e81a9c237ee432e670dc14b20dc8d3c84` | mono, 44.1 kHz, 0.16 s | assent |
| `fable/mojo_base_attack_hit3.ogg` | `legendarycreatures/sounds/mojo_base_attack_hit3.ogg` | `files[26424]`; `copies[81]` | `becf4fd1910fcfd2d046a87606a382977ac419967ae56240ce4a9b377795c244` | mono, 44.1 kHz, 0.13 s | assent |
| `fable/mojo_hurt.ogg` | `legendarycreatures/sounds/mojo_hurt.ogg` | `files[26426]`; `copies[83]` | `05d2eda319c0fa30e6475941656c7e0eee7b597f4c9cd58f4c08ad5c7be9526f` | mono, 48 kHz, 1.34 s | refuse at 0.75 |
| `fable/mojo_death.ogg` | `legendarycreatures/sounds/mojo_death.ogg` | `files[26425]`; `copies[82]` | `a7cd7b461cbaf85a90b1430dcf343ae95ee4eeba0cb77148cc8e70de3fa601d6` | mono, 44.1 kHz, 2.62 s | depart (his 2.6 s departure cuts at most 0.02 s of its tail) |
| `cavern/block/thornwood_branch/thornwood_branch_break_0.ogg` | `jims caves/sounds/block/thornwood_branch/thornwood_branch_break_0.ogg` | `files[19188]`; `cavern-runtime.json` `copies[1810]` | `d4b4698373569692b4a87de0f79c89822cd6465b7efb6a8043c666b63725289f` | mono, 44.1 kHz, 0.48 s | work |
| `cavern/block/thornwood_branch/thornwood_branch_break_1.ogg` | `jims caves/sounds/block/thornwood_branch/thornwood_branch_break_1.ogg` | `files[19189]`; `copies[1811]` | `3d38bb7dab6e2b92f0eaf754a14af2c656ff2d04cea8d064d36e8f58f7312543` | mono, 44.1 kHz, 0.66 s | work |
| `cavern/block/thornwood_branch/thornwood_branch_break_2.ogg` | `jims caves/sounds/block/thornwood_branch/thornwood_branch_break_2.ogg` | `files[19190]`; `copies[1812]` | `b79e1d88304570dfb111190801b0d0f6ee1b9ac9098a24ead7aab7c67b5ec93c` | mono, 44.1 kHz, 0.31 s | work |

The library's notices are kept with the originals; provenance as `art/owner-library/PROVENANCE.md` records it. The
library is unchanged and nothing in it is executed.

## Player experience

- Understand **The Garden's Regard** (the codex's Binding category; the journal's pacts section names each next step).
  Its entry has four pages of his: "Foras", "His Service", "The Logician's Objection" and "His Price".
- Build a **Pact Perch** where you may build, set a **chest** (or a barrel, or another plain container) touching it, and
  grow a **Sunpetal** (a crop that takes a graft: Dewlit) on an awake **Verdant Bed** of yours within 8 blocks, grafted or
  not. Use the perch: a card for each spirit you may invite, his "Ready", or locked with "Not yet" and "Store needed" or
  "Host needed". Without a store he does not come: "Where shall I keep the cuttings? Set a store beside me."; without a
  host: "I see nothing here a cutting would help." Nothing is spent.
- Chosen, he steps in on the ground two blocks to one side of the perch (a quarter turn clockwise from you, then round),
  a tall stooped elder in bark and moss, facing you, and tips his acorn cap. Three short beats (a Terms skip on the first
  two), then his terms as chips: any hour, 8 blocks, 8 a day, a honeycomb every 7 days with 4 ahead, a day's grace, you
  and your party, and the standing terms; the price as stacks, **4 Dewmoss and a honeycomb**, each you lack marked before
  Accept. Accepting spends them once, in the sealing step (no astrolabe, no resonance).
- **At any hour**, while you are within 64 blocks of the perch, he makes at most one graft a second, 8 a game day: a host
  crop on an awake bed of yours or your party's in his area that carries no graft the rules know, grafted with one donor
  (a Dewmoss for Dewlit) from a store touching the perch. He turns to the host, his acorn cap tips, and your action bar
  says his count: "Foras: 3 of 8 grafts today." The kept page draws the same count ("3/8 today") and names his last graft
  ("Last: the host at x, y, z grafted"). A graft lost when another crop grew on the bed he makes again when its host
  returns; a graft the rules know he never replaces. Out of donors, his page says "No Dewmoss he may take" among the rows
  that wait on you.
- **His objection** (Logic and Ethics): hosts the graft would not help (their beds already suit them as well) or would
  harm (a factor would fit worse) he names once, turning to the nearest with his inspect gesture, and asks on his kept page
  "Gain nothing or worse: 3, nearest x, y, z. Graft anyway?", with two plates, **Where it helps** and **Graft every
  host**; the roster and Jade say he awaits your answer at the perch. Until you answer he keeps to Where it helps and
  grafts the hosts it helps; answered Graft every host, he grafts those too, even where a host fares worse. Answered, with
  no such host now, the page asks it in general ("Graft hosts that gain nothing?"). A host whose bed holds no reading yet
  he leaves until it has one: its crop's growth reads it, but a ripe crop no longer grows and a reading is not saved, so
  after a restart or a chunk reload a ripe host waits until it is replanted or read by hand (using its bed or its crop,
  the Inspect page, a Habitat Gauge, or Jade on the bed).
- One honeycomb every 7 game days (up to 4 weeks paid ahead); a missed week is warned and he grafts on through a game
  day's grace, then the pact is suspended with nothing lost; an offering resumes it. Your first task: graft a host crop
  with its donor by hand in his area while the pact is active.
- Ending at the perch (asked first) or from the roster returns what was paid ahead once; he bows, folds his satchel and
  fades.

## Record fields (CLAUDE.md)

| Field | Foras's pact (as written) |
|---|---|
| Tier | Practitioner, Spiritbinders: The Garden's Regard needs the Binding Arts and Verdant Husbandry understood |
| Input producer | The Garden's Regard (GC-03); the perch; a plain container of the holder's touching it; a Sunpetal (the host) on an awake Verdant Bed of the holder's or their party's; Dewmoss from the Verdant Husbandry garden as donors, put in the store by the holder; the compact (4 Dewmoss; a honeycomb from a bee nest or hive with shears and a campfire); offerings (`#jugcraft:foras_offerings`: honeycomb) |
| Output consumer | a Verdant Bed's graft (`graft`, Chunk C's key): the host's grafted niche, read by growth, both reports, the Habitat Gauge, Jade and the Inspect page; the record's day count; the holder's first obligation (`GRAFT`), one of the three distinct garden spirits' obligations that master The Garden's Regard |
| Costs | the compact once; one honeycomb every 7 game days, at most 4 periods ahead; one Dewmoss a graft, from the store; no resonance |
| Unlocks | none new: the invitation (the Regard understood, a store touching the perch and a host within 8), graft keeping (the pact), one obligation toward The Garden's Regard mastered |
| Failure behaviour | [below](#failure-behaviour-multiplayer-and-save-compatibility) |
| Test evidence | written, not run: [Verification](#verification) |

## Connections

- **Producers:** The Garden's Regard and the garden groundwork (GC-03: `GardenReach`, `GardenPacts`, `PactObligations`);
  the garden (Verdant Beds, organism crops, Dewmoss, Chunk C's grafts and their niches); the pact hall's perch, screen,
  record, upkeep, escrow and history (Chunk D, GC-01); the accord machinery (GC-05); the standing station (GC-06b).
- **Consumers:** a bed's graft, through the one graft rule both the hand and he use (`Grafting.apply`, GC-07b); the
  record's day count; `PactObligations` (`GRAFT`: the holder's own graft by hand); the roster (`Graft keeping: a host
  grafted with a donor from the store beside his perch`), the journal's pacts lines and Jade.
- **Shared systems:** `PactSpiritEntity` (his body, cues, gestures, facing, `Station.BESIDE`), `PactService` and
  `PactServices`, `GardenReach` (its stores, GC-07b), `GardenAsking` (one think's questions, shared with every garden
  service), `GardenPacts.target` and `presence`, `PactAccords`, `CourierWork.store` and `plain` (the plain-container
  rule), Fabric's transfer API for the one extraction; no new registry, currency, conversion, energy, block or block
  entity (GC-S22).
- **Not required:** any other branch. Solo and Overworld: every input is a survival craft, a bee nest's honeycomb or the
  garden's own produce; trading is not needed.

## Service: graft keeping (`graft_keeping`)

- **Each think** (every 20 ticks) while his pact serves and his holder is within 64 blocks of the perch: at most one
  graft, at most 8 a game day. His hosts are the crops on the awake beds of his beneficiaries in his area (radius 8; at
  most 64 beds, nearest the perch first, from his bed index) whose organism takes a known graft with a donor item, whose
  crop stands in his area too (a bed on its top layer has its crop above the area, so it is no host of his and does not
  invite him) and whose bed carries no graft the rules know; his stores the plain containers touching the perch (at most 16, read with
  the beds in one pass, refreshed every 200 ticks, never loading a chunk).
- **Where it helps (GC-P84):** unanswered or answered Where it helps, only hosts the graft would improve by the bed's
  cached habitat (the grafted niche no worse on any factor and better on one); he never samples a bed himself. Answered
  Graft every host, every host.
- **Asked at the graft:** the host's crop and bed, and the store, through `GardenPacts.target` (serves there, a
  beneficiary's, the holder near, the holder's own hands), each place once and at most 8 places a think; a place
  refused for itself is skipped until his index is read again, and a store read without the host's donor is not asked
  or read for it again until then (at most 200 ticks), so neither crowds out the rest of a think's questions; a store is
  always asked before it is read (reading a loot chest fills it). A think whose questions ran out still says why (no
  host reached, or no donor found). The pact's own reason stops the think.
- **One step:** one donor taken from the store in one outer transaction, committed only when exactly one came out, then
  the graft through `Grafting.apply` in the same server call; nothing is held. The graft is counted on the record, he turns
  to the host with his work gesture (his cap tips), and the holder's action bar says the day's count.
- **First obligation (`GRAFT`):** graft a host crop with its donor by hand in his area while the pact is active
  (`OrganismCropBlock.graftBed`, on success); recorded once; his own grafts record nothing of the holder's.

## His matter: the logician's objection (the accord)

When a host in his area would gain nothing from its graft, or would fare worse (`Grafting.gains` counts both as no gain),
Foras raises it as a matter (`no_gain`) once a presence, turning to the nearest such host with his inspect gesture, and
awaits his holder's answer. Until then he keeps to the safe course
(Where it helps) and grafts the hosts it helps; nothing is lost, held or owed while the question waits.

- **Where it is asked.** On his kept page: "Gain nothing or worse: 3, nearest x, y, z. Graft anyway?" and two plates,
  **Where it helps** (`prudent`) and **Graft every host** (`always`); the whole question ("Hosts here a graft would not
  help, or would harm: 3, the nearest at x, y, z. Shall I graft them anyway?") is the row's narration and hover. Answered,
  with no such host now, the page keeps the plates under his question in general, "Graft hosts that gain nothing?". The roster
  says "awaiting your answer at its perch"; Jade on the perch names the matter ("Hosts gain nothing"; whole: "His matter:
  hosts here that a graft would not help").
- **Answering.** As Bathin's: the holder alone, at the perch, on the kept page; kept at once as the perch's `accord`,
  changeable at any time; he assents. Nothing is spent.
- **Jade's notes** (third person, short forms in `SENTENCES`): "Would gain nothing: 3" while hosts would gain nothing and
  the answer is not Graft every host (whole: "... he leaves them unless his holder answers "Graft every host" at the
  perch", true answered or not, GC-P90); "Unread hosts: 2" while hosts wait for their bed's first reading; "No donor";
  "Nothing to graft".

## Costs, balance and automation

He spends a donor the garden grows for a graft the hand could make with the same donor: one Dewmoss a graft, no
Dewmoss made, no conversion added, so no cycle gains (`python3 tools/concordance_economy.py`: "gaining cycles: none").
Bounded: 8 grafts a game day. A graft survives replanting, so once his hosts are grafted he grafts almost nothing while his
weekly honeycomb goes on (the cohort's checkpoint question 2; Jade says "Nothing to graft" then). Balance units: items
(Dewmoss, honeycomb) and grafts. All of it is for the owner after play.

## Failure behaviour, multiplayer and save compatibility

- **Failure behaviour:** no store or no host: he does not come, nothing spent; no donor in a store he may take from: he
  waits (Jade "No donor"; the kept page "No Dewmoss he may take"), and a store read without one is read again when his
  index is (at most 200 ticks); a host the holder may not change by hand, another's or a stranger's bed, or a store the
  holder could not open: skipped until his index is read again, nothing spent; a graft the rules know:
  never replaced; an extraction the store refuses or cannot fill: nothing spent, no transaction left open; unanswered or
  an unknown answer: only where it helps; a bed with no reading: left until it has one; holder away, dead, in another level
  or more than 64 blocks off: he waits (no stand-in); suspended or held: nothing; his day's grafts spent: he stops until
  the next game day; a missed offering warns, gives a day's grace (he works on), then suspends with nothing lost; ending
  returns what was paid ahead once; switched off, everything waits.
- **Multiplayer:** server authority at every think; two holders' Foras over one party bed graft it once, one donor in all
  (the second finds it grafted). One presence a pact: a duplicate or a copy of his save at a perch leaves at its first
  think and grafts nothing.
- **Save compatibility:** added `jugcraft:foras`, `data/jugcraft/concordance/pact/foras.json` (agreement
  `jugcraft:foras_pact`), `#jugcraft:foras_offerings`, the `jugcraft:foras.*` sound events (arrive, listen, inspect, assent,
  refuse, depart, work), his three body files and four codex pages; saved strings appended: the work `graft_keeping` (a
  Bound Will's service; append only) and the accord values `prudent` and `always` (the perch's `accord`). No new block
  entity key: a bed's `graft` is Chunk C's, now also written by him. His entity writes only the base's `perch` and
  `record`; his memory (his index, the places refused and the stores read without a donor since it was read, the hosts he
  would not graft, how many beds hold no reading, his matter raised, why he idled, his last graft) and his station's side
  are never saved, so a restart forgets them and owes nothing. No
  migration: every addition is appended. An older build without him drops his entity and shows `graft_keeping` raw on a
  record; a bed's graft survives.

## The pact screen

The shared screen on the magic interfaces' relic scroll card ([magic-interfaces.md](magic-interfaces.md); GC-S26, decided
by the session in answer to the owner's request for far less reading, reviewable at the checkpoint, not owner-accepted),
with his rows as data in `tools/magic_ui.py` `WORK_GLANCES["graft_keeping"]` ([his words](#his-words-on-the-magic-interfaces-gc-s26)):
the chooser card "Ready", or locked with "Store needed" or "Host needed"; his three beats; his terms as chips, his two
blockers as lock lines; the kept page with "Grafts your hosts, one Dewmoss each", today's grafts as a gauge ("3/8
today"), his first task short ("First task: graft a host crop with its donor by hand here", 12 words: no act glance),
his matter (or, answered and none raised, his question in general) and its two plates, his last graft (a Sunpetal before
it) and "No Dewmoss he may take" (a Dewmoss before it, among the rows that wait on you) as rows; the end asked on a
scroll. No garden term or refusal names an
astrolabe.

## Decisions recorded with the code

Each is the session's, reviewable at the cohort's checkpoint; none is the owner's.

- **His station is unit 3's** (decided by the session: unit 3's shared prefix, GC-06b, replaced the unit's own): no
  station code of his own; `Station.BESIDE` (GC-P67, in the shared prefix) places him; GC-P89 says only that. The
  unit's own station (its spec's section 6.5 and commit U4-C3, `arrival` and `stationOf`, its `check_pacts` block, its
  mutations M12 and M13, the forced count lines of its section 9.4 and its merge rows M-6 and M-14) is not built.
  `forasStandsBesideThePerchFacingHisInviter` expects him two blocks west of the perch for a holder south of it, on the
  shared prefix's `floor(helper)`. `check_pacts` holds the station per row of `SPIRITS` (a standing body's `"station":
  "beside"` and its Java's `Station.BESIDE`), as unit 3's design writes it, so the unit's mutation M11 is caught on this
  line too.
- **One think's questions are the shared `GardenAsking`** (decided by the session: one rule for every garden service,
  GC-P85): no private copy of Bathin's `Asked` and `Asking`. As Bathin's, his memory skips a place refused for itself until
  his index is read again (the unit review: seven refused places nearer the perch used up every think's questions), and
  it skips a store read without a donor until then too, remembered by store and donor item, so a store without one
  graft's donor is still read for another's (decided by the session; only Dewlit is known today).
- **His chooser card reasons are "Store needed" and "Host needed"** (decided by the session, under the magic interfaces'
  budgets): with Stolas ready and Buer, Bathin and Foras not, the chooser draws its 25 words only if his reason is two
  words, and a work with two refusals needs a reason for each, so `tools/magic_ui.py`'s `card` may name one per refusal
  (`work_cards`) and `check_magic_ui` asks each `missing*` key for one.
- **His Jade notes add `no_reading`** and his `no_gain` note reads true answered or not (GC-P90).
- **His matter names how many and the nearest, and that a graft can harm** (decided by the session, from the unit review):
  `Grafting.gains` counts a host that would fare worse as no gain, so "Graft every host" can harm, and the matter says so
  and keeps its place, as Bathin's names his Heart. His question in general has its glance (and Bathin's too), so an
  answered page draws it short; `check_magic_ui` now asks a glance of every work's question, and the lock of every blocker.
- **His count aloud is `GraftKeeping.countLine`** (detail b), read by the server test; the client flow reads the
  kept page's count, not the action bar (the client's overlay message is private to `Hud`).
- **The tests' sampling** goes through the shared retrying rule (GC-06b: a sample the level's allowance refuses is asked
  again the next tick): `afterSampling` for one bed;
  `aPrudentForasGraftsWhereItHelps` takes the same rule from its 30th tick, after the light engine has lit the new
  blocks (80 ticks in all). A bed whose host must hold no reading has its sample forgotten in the test's body, since its
  crop's random tick may have read it (the 26.3 lesson).
- **Four server assertions expect AWAITING_ANSWER** where his think raises or keeps his matter unanswered (GC-P47).
- **His first obligation's test** checks the graft outside his area and the one while suspended before the one that
  keeps it, then one more that records nothing.
- **The client flow** (after Bathin's, under unit 4's marker): noon and clear weather first and noon again before his
  poses (in the merged order Marax's flow comes first); `standingFacing` with his literal `FORAS_FACES` (so
  `check_pact_screen` sees each shot's name), not a
  `forasFacing` of his own; the chooser's card reached with `tabToNamed`, as the restyled chooser asks; the matter's
  answers asserted none kept before the answer (the safe course is not a kept answer); the roster's "awaiting your answer"
  read from the server's roster entry and the client's words for it, without opening the roster again (unit 2
  photographed it); the ending's count expects one graft unless the game day turned since it; the mouse parked off every
  chip for his whole flow (the 26.3 lesson); nothing paid ahead, so the ending checks no escrow
  (`endingAnySpiritsPactReturnsTheEscrowOnce` holds it for every spirit).
- **Two class Javadocs are left as they are** (`Workers` and the garden client class): neither has a marker, and the
  merge with units 3, 5 and 6 would conflict there.

## Verification

**Not run in CI:** nothing on unit 4's line has run in CI, and no Minecraft build was possible where GC-07 was written,
so none of the Java below has been compiled; the last CI result this line descends from is run 40782439981 (green in
full on `d7185041b`, before unit 4). Each Java file was parse-checked (no syntax errors); its APIs were checked against
the repository's own uses, the 26.3 decompiled source, GeckoLib 5.5.7's source and Fabric API 0.161.0's. Written, to run
in CI:

| Brief item | Evidence written (server game tests in `ConcordanceWorkerGameTests` unless named; shots of `ConcordanceGardenPactClientGameTests`) |
|---|---|
| 1. Identity | his body files from `tools/garden_bodies.py` through `tools/owner_art.py` (`--check`); `check_spirit_bodies` (his bones, clips and their lengths, the cap from the owner's file, the skin's colours, the cap's tip in his arrive and work clips only, his box, his poses' windows, his loops and shots in the client) |
| 2. Invitation and route | `forasRefusesWithoutAStore`, `forasRefusesWithoutAHost`, `forasStandsBesideThePerchFacingHisInviter`; `tools/concordance_journey.py --check` (the garden pacts' steps 10 to 12); shots `jugcraft_garden_foras_no_store`, `_chooser` |
| 3. Service | `forasSpendsOneDonorAGraftAndNeverTwo`, `forasNeverReplacesAKnownGraft`, `forasGraftsAgainAfterAReplanting`, `forasTakesOnlyFromAStoreTheHolderMayChange` (a store read empty is read again with his index), `forasSkipsPlacesRefusedForThemselves`, `forasStopsAtHisQuota`; `ConcordanceGardenGameTests`' `theGraftGestureBehavesAsBefore`, `aGraftGainsOnlyWhereEveryFitIsNoWorseAndOneBetter`, `theGardenReachIndexesStoresTouchingThePerch` (GC-07b); shot `_grafting` |
| 4. Upkeep and suspension | SG12's fifteen shared cases through his fixture (a chest beside the perch and a grafted Sunpetal), `noGardenTermOrRefusalNamesAnAstrolabe`, `talkingTeachesNothing`; `check_acquisition` (his compact and offering reachable); shots `_terms` (three sizes), `_refused`, `_kept` |
| 5. Conflict: the logician's objection | `aPrudentForasSkipsAUselessGraft`, `aPrudentForasGraftsWhereItHelps`, `anAlwaysAnswerGraftsEveryHost`, `forassReadingSaysItsNoteAndNamesNoPlayer`; `ConcordancePersistenceGameTests.aForasReloadedKeepsHisStoreAndAnswer`; shots `_matter` (three sizes), `_answered`, `_jade` |
| 6. Refusal and departure | `forasRefusesWithoutAStore`, `forasAnswersEachActionWithHisGesture`, `aDuplicateForasGraftsNothing`; shots `_depart`, `_end_confirm`, `_ended` |
| 7. Interaction details | (a) the cap's tip (`check_spirit_bodies`), shots `_arrive`, `_work`, `_grafting`; (b) `theKeptPageCountsForasGrafts` (the kept page's count and `countLine`), the client's count on the kept page at the end, his question in general drawn short within the kept page's budget (shot `_counted`); facing: `forasStandsBesideThePerchFacingHisInviter`, shots `_faces_east`, `_south`, `_west`, `_north` |
| 8. Evidence | *ownership:* `forasRefusedWhereTheHolderMayNotBuild`, `forasNeverGraftsAStrangersBed`, `forasTakesOnlyFromAStoreTheHolderMayChange`, `forasWaitsForAPresentHolder`; *no duplication:* `twoForasNeverGraftOneBedTwice`, `aDuplicateForasGraftsNothing`, `forasSpendsOneDonorAGraftAndNeverTwo` (a refused or short extraction spends nothing and leaves no transaction open); *save:* `aForasReloadedKeepsHisStoreAndAnswer`; *first obligation:* `forassFirstObligationIsAGraftByHand`; *Jade:* `forassReadingSaysItsNoteAndNamesNoPlayer`, `theKeptPageCountsForasGrafts` (no player named) |

In all: 25 server tests of his (3 in `ConcordanceGardenGameTests` from GC-07b, 21 in `ConcordanceWorkerGameTests`, 1 in
`ConcordancePersistenceGameTests`; `GC07_GRAFT_TESTS` and `GC07_FORAS_TESTS` in `tools/check_mod_data.py` hold their
names), and his client flow after Bathin's, 33 pictures (23 of `GARDEN_CLIENT_SHOTS` and his 10 body shots), each pact
screen held to its word budget before its picture. `tools/select_client_tests.py` sends a change to his body, entity or
service to the garden client class (`scripts/tests/test_client_test_selection.py`).

**Local, GC-07b** (`8d8fba4b5`; its message names no check, so they are recorded here from the session's logs): the
generators (`tools/generate_material_data.py`, then `tools/owner_art.py`), then `python3 tools/owner_art.py --check`,
`python3 -B tools/check_mod_data.py`, `python3 scripts/check_repository.py`, `python3 tools/concordance_journey.py
--check`, `python3 tools/concordance_economy.py` ("gaining cycles: none"), `python3 tools/check_icon_maps.py`, `node
--test tools/world-designer/model.test.cjs` and `python3 -m unittest discover -s scripts/tests` (341 tests) each ran and
passed before it was committed; no record of `git diff --check` there was kept. Its audit mutations ran on a scratch
copy of the worktree taken after that commit: M1 (`Grafting.apply` keeps the ungrafted plant's verdict), M2 (the gesture
sets the graft itself, not through `apply`), M3 (`holds` counts a graft the rules no longer know), M4a (the donor spent
before the graft), M4b (`apply` also forgets the sample), M5 (a store decided by reading the level), M6 (the stores
capped at `MAX_BEDS`), M7 (the perch counted as touching), M8 (`MAX_STORES` 32) and M9 (a sided container taken as
plain) were each caught by `check_mod_data`; M10 (the library's second root never read) passed `check_mod_data` and
`owner_art.py --check` and was caught by `scripts/tests/test_owner_art_part2.py`; the session's X1 (a GC-07b test
renamed) and X2 (the store index built without its stores) were caught by `check_mod_data`. The unit review ran M1 to M9
(with M4a and M4b) again on a copy of `e4fca201d`, and reported each caught.

**Local, GC-07** (its message lists each check and the audit mutations run on a scratch copy): the generators
(`tools/generate_material_data.py`, then `tools/owner_art.py`) wrote his data, tag, sounds, words, codex pages and body
files; `python3 tools/owner_art.py --check`, `python3 -B tools/check_mod_data.py`, `python3 scripts/check_repository.py`,
`python3 tools/concordance_journey.py --check`, `python3 tools/concordance_economy.py` ("gaining cycles: none"),
`python3 tools/check_icon_maps.py`, `node --test tools/world-designer/model.test.cjs`, `python3 -m unittest discover -s
scripts/tests` and `git diff --check`. The fixes from his review are a commit of their own, whose message lists its
checks and mutations likewise.

His rows of `python3 tools/owner_art.py --provenance pacts`, as printed for this record:

| Runtime file | Source | SHA-256 | How |
|---|---|---|---|
| `geckolib/animations/entity/foras.animation.json` | built from code alone | | built by `tools/garden_bodies.py` (foras_animations) |
| `geckolib/models/entity/foras.geo.json` | `originals/Edited-2026-10-18-Part2/jims mobs/geo/item/armor/acorn.geo.json` | `913962495527ed87e4bbe2d0e5fa29a79c04200bb762c602ded852b34f658b13` | built by `tools/garden_bodies.py` (foras_model) |
| `geckolib/models/entity/foras.geo.json` | `originals/Edited-2026-10-18-Part2/jims mobs/textures/item/armor/acorn.png` | `947202e4d51ba69035d7fef89566f6619236de79270bfdf79285d538c05db2d3` | built by `tools/garden_bodies.py` (foras_model) |
| `textures/entity/foras.png` | `originals/Blocks/biomes and tree blocks/fir_log.png` | `26eb9fba8fccfce7d07526531a5a59dd6a6253361f0575f5857e26a081c17e83` | built by `tools/garden_bodies.py` (foras_skin) |
| `textures/entity/foras.png` | `originals/Blocks/biomes and tree blocks/spanish_moss.png` | `063d9313efbe9a01921461829d92eab3ecaa3c0ef6bcaf0b95c4aef06d13e3c7` | built by `tools/garden_bodies.py` (foras_skin) |
| `textures/entity/foras.png` | `originals/Edited-2026-10-18-Part2/jims mobs/textures/item/armor/acorn.png` | `947202e4d51ba69035d7fef89566f6619236de79270bfdf79285d538c05db2d3` | built by `tools/garden_bodies.py` (foras_skin) |

**Not covered:** everything that needs a build: compiling, every game test above, the client flow and its pictures;
people; sound; two clients at his perch; the narrator heard; the action bar's count read by the client (the picture shows
it; the server test reads its words); his astir loop (not photographed).

For his brief (GC-07a): each SHA-256 above was computed from the library file (and, for the sounds, from the runtime copy
too), and each equals its catalog entry (`files.csv` by line, the header line 1; the part's `files.json` and runtime
catalogs by index from 0); the two paintings' colours were counted (both 16 x 16: the fir log's seven opaque, two of them
nearly equal, (74, 67, 53) and (74, 67, 56); the Spanish moss's eight opaque around 97 clear texels); the acorn texture's
455 drawn texels lie in x 0 to 39, y 0 to 23, its leaf's north face (15, 20, 4, 4) draws 13 of 16 and its south face (19,
20, 4, 4) none, and it is byte for byte the Companion Acorn Hat's runtime texture; the thirteen sound files' Vorbis
identification headers and last granule positions were read (one channel each, at the rates and lengths above); and the
words proposed above were counted with `tools/magic_ui.py`'s own `nwords`, `width` and `lines`.

## World and event applicability

Overworld, Nether and End alike (a perch, its store and the beds in one level; his area never crosses levels). No
seasonal gate. No cave, dungeon, boss or loot role.

## Rollout and open questions

Ships with the garden cohort's unit 4 (GC-07), behind the Concordance's switch; written, not compiled, not run in CI, not
merged. Known limits: nearly idle once his hosts are grafted, while his weekly honeycomb goes on (the cohort's checkpoint
question 2); his count is an action-bar line, not a voiced one; a host whose bed holds no reading waits until it has
one (he never samples): its crop's growth reads it, but a reading is not saved and a ripe crop no longer grows, so after
a restart or a chunk reload a ripe host waits until it is replanted or read by hand (using its bed or its crop, the
Inspect page, a Habitat Gauge, or Jade on the bed; any change to the shared sampling is unit 7's); a donor put in a store
he read without one is found when his index is next read (at most 200 ticks); his body is built rather than the owner's
(no owner rig is a tall elder; the cohort's checkpoint question 1); his sign is Jugcraft's Acorn (`jugcraft:acorn`, the squirrels' item), what a long keeping starts from (GC-P91); his brief was not approved by the owner (the
checkpoint's first question).
