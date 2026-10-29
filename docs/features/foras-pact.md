# Foras's pact (garden cohort, GC-07)

Concordance garden cohort, GC-07 ([contract](concordance-garden-cohort.md#gc-07-foras-unit-4); Dossier 4). Foras, the
thirty-first spirit, keeps grafts: standing beside his Pact Perch, he grafts the host crops of his holder and their
party with donors from a store touching the perch, one graft at a time, at most 8 a day, and only where the graft helps
until told otherwise. He is the cohort's fourth garden spirit (Marax, the third, is built beside him in unit 3), on the
same pact record, perch, screen, roster, journal and Jade lines as Stolas, Buer and Bathin, and the first to spend an
item for his work: one donor from the store for each graft.

Status: brief only (GC-07a), committed before any of his body files (D-D5), as the contract's per-spirit template asks.
Nothing of Foras is built yet: no entity, pact definition, service, sound event, lang, body file, audit or test. This
page holds his character (the contract's Dossier 4) and the owner files his body and cues are to use; the runtime files
and builders it names are what GC-07 is to build. Where it goes beyond Dossier 4 (the greeting's stage direction, how
his body is built, which cue plays which file at what pitch, the short words the screens draw) it is unit 4's proposal
(GC-P80 to GC-P99, added to the contract's Decisions when the unit lands), and the palette files may change only here,
before any body file (the contract's art plan). Two things came after the unit's design and are followed here: his
station is unit 3's (GC-P67, in the shared prefix GC-06b), and his words keep to the magic interfaces' limits (GC-S26).
The rest of the record (player experience, record fields, connections, costs, failure behaviour, the pact screen, his
service and his objection) is added with the code.
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
  limits: it wrote a 23-word stage direction ("An elder in a robe of bark and moss stands beside the perch, an acorn cap
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
| The header's service line | "Grafts hosts from the store beside him" | 7 (198 px of the header's 211) |
| Today's gauge (detail b) | "%1$s/%2$s today" ("3/8 today") | 2 |
| Blockers (no store; no host) | "Needs a chest or store touching the perch"; "Needs your host crop on a bed within %1$s blocks" | 8; 10 |
| Chooser card reason | "Needs store and host" | 4 |
| Kept-page rows (his last graft; no donor) | "Last: the host at %1$s grafted"; "No donor in the store beside him" | 8 (with a position); 7 |
| His matter | "%1$s hosts would gain nothing: graft them anyway?" | 8 |
| His answers (their own words) | "Where it helps" (`prudent`); "Graft every host" (`always`) | 3; 3 (the design's "Only where it helps" was 4) |
| His first task's row | "First task: graft a host crop with its donor by hand here" | 12, two lines at the narrowest scroll: no act glance needed |
| Jade (short forms in `SENTENCES`, marks in `SENTENCE_MARKS`) | "Hosts gain nothing" (his matter); "No gain: %1$s hosts", "No donor", "Nothing to graft", "Unread hosts: %1$s" (his notes) | at most 4 |

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

## Verification

Not run: nothing of Foras is built yet, so no build, game test, client shot or audit exercises him, and nothing on unit
4's line has run in CI. For this brief only: each SHA-256 above was computed from the library file (and, for the sounds,
from the runtime copy too), and each equals its catalog entry (`files.csv` by line, the header line 1; the part's
`files.json` and runtime catalogs by index from 0); the two paintings' colours were counted (both 16 x 16: the fir log's
seven opaque, two of them nearly equal, (74, 67, 53) and (74, 67, 56); the Spanish moss's eight opaque around 97 clear
texels); the acorn texture's 455 drawn texels lie in x 0 to 39, y 0 to 23, its leaf's north face (15, 20, 4, 4) draws 13
of 16 and its south face (19, 20, 4, 4) none, and it is byte for byte the Companion Acorn Hat's runtime texture; the
thirteen sound files' Vorbis identification headers and last granule positions were read (one channel each, at the rates
and lengths above); and the words proposed above were counted with `tools/magic_ui.py`'s own `nwords`, `width` and
`lines`. The tests, shots and runs that verify him are listed here when GC-07 lands.
