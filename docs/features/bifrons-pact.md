# Bifrons's pact (garden cohort, GC-08)

Concordance garden cohort, GC-08 ([contract](concordance-garden-cohort.md#gc-08-bifrons-unit-5); Dossier 5). Bifrons, the
forty-sixth spirit, is a low mound of roots studded with stones that rises into a lean man of roots while he is spoken
to. By night he runs one root at a time from a full Verdant Bed of his holder's to a hungry one: two nutrients leave the
full bed, one reaches the hungry bed, and the other is lost along the root. When only beds with crops growing on them
could give, he asks which beds are reserves. He is the cohort's fifth garden spirit, on the same pact record, perch,
screen, roster, journal and Jade lines as Stolas, Buer, Bathin and Marax, and he stands on the ground beside the perch
as Marax does.

Status: brief only (GC-08a), committed before any of his body files as the contract's per-spirit template asks (D-D5).
Nothing of Bifrons is built yet: no entity, pact definition, service, act, hook, sound event, lang, body file, audit or
test. Nothing of him has run in CI; this line's head before the brief (`3c2d36b0f`, Marax and the roster) is in CI with
no result recorded here. This page holds his character (the contract's Dossier 5) and the owner files his body and cues
are to use; the runtime files and builders it names are what GC-08 is to build. Where it goes beyond Dossier 5 (the
greeting's stage direction, his sign and box, how his body is built, which cue plays which file at what pitch) it is
unit 5's proposal (GC-P100 to GC-P119; the contract's Decisions hold GC-P100 to GC-P102 as proposed, and the rest are
added when the unit lands), and the palette files may change only here, before any body file (the contract's art plan).
Where unit 5's specification was written before the shared prefix and the magic interfaces joined this line, this brief
restates it as they now stand ([Restated from unit 5's specification](#restated-from-unit-5s-specification)). The rest
of the record (player experience, record fields, connections, costs, failure behaviour, the pact screen, his service
and his matter) is added with the code.
Proposal issue: none; the owner's selected scope ([the cohort's contract](concordance-garden-cohort.md), GC-D1 to GC-D3).
Owner: @jimbozoomer-byte. AI-assisted implementation with Claude Code; the model is named in the commit trailers.

## Bifrons's character

The forty-sixth spirit (1904 heading "Bifrons"). Source: the 1904 text, *The Book of the Goetia of Solomon the King*
(Mathers, translator, credited as "a Dead Hand"; Crowley, editor), entry 46, with the contract's labels: [S] wording
matched by a search over transcriptions of the 1904 text, [s] the sense confirmed, the wording not. No scan was opened,
and Gutenberg's record #72679 (de Laurence's 1916 reprint of the Mathers text) is still unread (GB18). Source phrases:
he "appeareth in the Form of a Monster" [S], and at the Exorcist's command a man's shape [s]; he makes one knowing in
"Astrology, Geometry" [S]; "He teacheth the Virtues of Precious Stones and Woods." [S]; an Earl [s]. **Jugcraft
adaptation: a root's measure ("Geometry") carries matter between beds, and "Woods" gives him roots.** The entry's line
about the dead and their graves, the nearest source for moving things to another place, is left out entirely (GC-S14;
it is quoted only in the contract's Dossier 5). The survey grounded root-running on a softened reading of that line,
"if the owner accepts" it, and offered a wood or orchard service built on "Woods" as the alternative; the cohort's
checkpoint (question 1) asks which the owner prefers. The two-stage appearance gives his shape. The Latin meaning
"two-faced" and any Janus link are not used. Not imported: the Megami Tensei Bifrons; the *Dictionnaire Infernal* plate;
any seal, conjuration, title or legion count. No source quotation is put in game: his beats are Jugcraft's words.

- **Silhouette:** a low mass of roots studded with stones, about 0.7 block high and 1.2 long, crawling on four short
  roots, two small eyes looking out of its front; spoken to, it draws in to a pedestal of roots and a lean man of roots
  stands up out of it, about 1.8 blocks tall, with a crown of root hair, a band of root at his waist and a simple face
  (two eyes and a mouth, no nose holes). Built geometry. His box is his low shape, 1.2 wide and 0.7 high, eyes at 0.5
  (GC-P107): the risen man is drawn above it, so the box differs by more than a fifth on one axis from every other
  spirit's and the workshop hand's. The contract's line-up says "0.6 low"; the mound as specified is 0.69.
- **Personality:** shy in his first shape, exact once risen; measures before he acts; dry about his own looks.
- **Beats (GC-P100, as the contract gives them):** greeting "Pardon the shape. I am Bifrons; I take a better one when
  spoken to."; service "Where one bed is full and another hungry, I run a root between them. Half is lost along it:
  that is a root's measure."; price "Two logs a week. A root must remember wood." Unit 5's specification proposed a
  stage direction before the greeting: "Roots push up out of the ground beside the perch and gather into a low mound
  studded with stones; two small eyes open in it, and as you speak it rises into a lean man of roots."
- **Beats as the magic interfaces draw them** (GC-S26: at most 18 words a line and a stage direction of at most 8, its
  own key drawn dim; decided by the session, amending GC-P100, as Marax's were): stage direction "A mound of roots stirs
  beside the perch." (8 words); greeting unchanged (14); service "From full beds to hungry ones I run roots. Half is
  lost: a root's measure." (15; the contract's is 24); price unchanged (9). The rest of the specification's direction is
  shown, not told: the roots push up and gather in his arrive gesture, his eyes open in the mound, and he rises as the
  screen opens.
- **Refusal:** "A root runs between two places. Show me two." (the opening of his blocker's whole sentence, its tip and
  narration). **Departure:** he sinks back into roots and burrows away.
- **Sign** (GC-P107): hanging roots (`minecraft:hanging_roots`), shown where he is not standing at his perch; no other
  spirit's sign (the spirits' signs stay unique).
- **Station** (GC-P105, in the base): unit 3's standing station, `Station.BESIDE`, with no station code of his own. He
  keeps the ground two blocks out from the perch on the first clear side, trying first the side a quarter turn clockwise
  from his inviter's (never between them and the perch), facing whoever speaks to him; on the perch's top when no side
  is clear (as in the server tests' structures, which have no sturdy floor two out).
- **Details.** (a) He rises into the man's shape only while someone speaks to him and sinks when the screen closes: his
  listening loop plays the rise once, then the risen loop, and he sinks on the main controller's blend back (GC-P109);
  by day, outside his hours, he waits low and still. (b) Before each run he measures, in two beats a think or more apart:
  he attends the source, and the shared preparation sign (`Sign.GATHER`, glyphs drawn in to the place being readied)
  rises there; at the next beat it rises at the destination and the root runs (GC-P102). No travelling sign and no bone
  pointing, so no client hook.
- **First task** (GC-P7, `PactObligations.Act.FEED`): a Verdant Bed, or the organism crop growing on one, fed with bone
  meal by hand (one hook line after each bone-meal success; a dispenser has no hand). With two other spirits' first
  tasks it masters The Garden's Regard.

### His words on the restyled screens

Proposed for GC-08 (decided by the session, under the magic interfaces' limits: GC-S26,
[magic-interfaces.md](magic-interfaces.md)). His server sentences (unit 5's specification, section 7.1 (d)) stay whole as
each glance's tip, its narration and Jade's extended view; what a page draws is the glance. A new spirit adds data, not
code paths, to the restyled screens: his work's row in `tools/magic_ui.py` `WORK_GLANCES`, an act glance, short forms in
`SENTENCES` and `SENTENCE_MARKS`, one `Map.entry` in `PactGlance.WORK_ICONS` and one line a note or matter in
`MagicGlance.MARKS`, each under his unit's marker.

| Where | Drawn | Limit |
|---|---|---|
| Header and Jade service name | "Root-running" | |
| Service line (header, service chip) | "Runs roots from full beds to hungry ones" (8 words) | 10 words |
| Work icon | `minecraft:rooted_dirt` (his sign stays the hanging roots) | an item |
| Today's gauge (`row.service`) | "%1$s/%2$s today" ("3/24 today") | the gauge's form |
| Asleep (`row.asleep`) | "Asleep until %1$s" ("Asleep until 19:00") | 12 words |
| Last run (`row.last`) | "Last: %2$s → %4$s" ("Last: 102, 64, -18 → 98, 64, -18", 7 words); the amounts in its tip | 12 words |
| Reserves kept (`row.reserved`) | "%1$s growing beds kept as reserves" | 12 words |
| His matter (`matter.reserves`) | "Your full beds are growing: draw from them?" (8 words) | 12 words |
| Answers | "Fallow beds only" (`fallow`, the safe course), "Any full bed" (`full`) | 3 words each |
| Blocker (`missing`) | "Needs two awake Verdant Beds of yours within %1$s blocks" (10 words) | 14 words |
| Chooser card | "Needs two beds near" | 4 words |
| First task (`garden_pact.act.feed`) | "feed a Verdant Bed bone meal", so the row reads "First task: feed a Verdant Bed bone meal by hand here" (11 words; with the whole act, 18) | 12 words |
| Jade (note or matter, and its mark) | reserves "Growing full beds: %1$s" (ask); asleep "Asleep until night" (moon); nothing full "Nothing to run" (moon); nowhere with room "Nowhere to run it" (hourglass); his matter "Full beds all growing" (ask) | 4 words a line |

Each page is held to the client shots' budgets (a beat page 30 words, the terms 50, the kept page 45, the roster 70, the
chooser 4 words plus 7 a card).

### Restated from unit 5's specification

Unit 5's specification and its review were written from unit 2's head, before the shared prefix (unit 3's GC-06b) and
the magic interfaces' restyle joined this line. As the cohort's plan for units 3 to 6 resolves them (decided by the
session):

- **His station** is unit 3's `Station.BESIDE` (GC-P105 in the base, GC-P67's rule): his `Body` takes it and his
  `SPIRITS` row says `"station": "beside"`. No interim station on the perch's top and no post-merge station commit (the
  specification's GC-08m) is made; his SG12 fixture beds stay where the specification puts them.
- **No forced test lines** (GC-P116 in the base): the shared prefix's generic `aPerchOffersOnlyWhatThePlayerMayInvite`,
  `aStaleChooserRevisionIsRefused`, `theJournalNamesEachRegardsNextStep` and the garden client's chooser already read
  each offered spirit's readiness from its service; Bifrons adds no line to them.
- **His words** are the glances above, his beats held to the limits (GC-P100 amended); the specification's longer rows,
  matter and Jade notes stay his server's whole sentences.
- **His measuring marks** are the shared preparation sign at each bed of a measure, through `Signs.show`, not block
  particles of his own (the specification's ring of rooted-dirt specks, GC-P102 and its F10): `check_signs` refuses
  server particles that do not go through a sign. A sign of his own would be added only as the shared system adds one
  (`tools/concordance_signs.py` `SIGNS` and `Sign.java` together).
- **His first task's words** take the review's correction: a bed or the crop on one, fed with bone meal by hand (the
  hook reads neither the bed's keeper nor whether it is awake).
- **The source** is cited as the contract cites it; the specification's "read through Gutenberg #72679" is not claimed,
  since no scan or Gutenberg record was opened (GB18 open).

## Dependencies and assets

GeckoLib (approved; his model, clips, glowmask layer); Jade optional (his service line, notes and matter, behind
`compat/`). No new dependency, pin or mixin. His body is built by `tools/garden_bodies.py` (no owner rig is a mound of
roots that stands up as a man; the library's unused rigs, the Siren and the Flarecannon, fit none: the contract's art
plan): geometry and clips are code; his 128 x 64 skin and glowmask are painted only in the colours of two of the owner's
paintings, read as data and never copied or edited:

| Painting (under `art/owner-library/originals/Blocks/`) | Catalog row | SHA-256 | Colours | Used for |
|---|---|---|---|---|
| `biomes and tree blocks/pine_log.png` | `files.csv:2013` | `a239df254b764327558f06a24604b5a6091b50016d7b283978258b3160e10d26` | 6 | his roots, mound and hump; his legs of bundled root; his bark skin, root hair, waist band and mouth |
| `biomes and tree blocks/sphalerite.png` | `files.csv:2071` | `a32a3989555df6650fdf9a8888d328ffbfea274e0200545b0bd1f04feafe8b23` | 5 | the stones in his roots, and his four eyes |

To be built (GC-P108) by these builders, written and checked by `tools/owner_art.py` (`BUILT`):

| Runtime file | Built by | What it is to hold (unit 5's specification, section 7.2) |
|---|---|---|
| `geckolib/models/entity/bifrons.geo.json` | `bifrons_model` | box-UV cubes on bones `root > mass > (hump, four roots)` and `root > body > (two legs, torso > (head, two arms))`, eleven UV regions apart on a 128 x 64 sheet; the mound 1.19 x 0.69 x 1.22 blocks, the man 1.78 tall; no flat or turned cube and no shared face plane |
| `geckolib/animations/entity/bifrons.animation.json` | `bifrons_animations` | twelve clips: five loops (idle 4.0 s, listen 3.0, waiting 6.0, suspended 6.0, astir 1.5), the rise (1.0 s, played once) and six gestures (arrive 2.0, inspect 2.0, assent 1.5, refuse 1.6, work 2.0, depart 1.5, held on its last frame); his two shapes keyed as scale and position of `mass`, `hump` and `body` in the loops and the rise only |
| `textures/entity/bifrons.png` | `bifrons_skin` | every texel opaque or clear and one of the two paintings' eleven colours (ten used; the pine log's `(142, 106, 83)` is not) |
| `textures/entity/bifrons_glowmask.png` | `bifrons_glowmask` | his four eyes alone: two on the hump's front, `(68, 11)` and `(71, 11)`, and two on the risen face, `(19, 29)` and `(22, 29)` |

His seven cues play three runtime sounds already in the repository, copied unchanged from the owner's library: the art
plan's roots family, the event `beast.banana_slug_slime_expand`, which lists exactly these three (as the owner's own
`jims mobs/sounds.json` does), told apart by pitch (GC-P110):

| Runtime file (`assets/jugcraft/sounds/menagerie/beast/`) | Catalog (`art/owner-library/catalog/edited-2026-10-18-part2/`) | SHA-256 | Length | Cues (pitch, subtitle) |
|---|---|---|---|---|
| `banana_slug_slime_expand_0.ogg` | `files.json` `files[24322]`; `beast-runtime.json` `copies[962]` | `2af17d358b1d014e939c829381895bdab388c190df759eea824e2bb50a8e38ff` | 0.828 s | arrive (0.8, "Roots push up beside the perch"); work (1.2, "Bifrons runs a root"); depart (0.6, so 1.38 s, inside his 1.5 s departure; "Bifrons sinks into the ground") |
| `banana_slug_slime_expand_1.ogg` | `files[24323]`; `copies[963]` | `3007ef633f1855c86babbbeb55375467dfa44d11536c1fb3a55e7170a27e22c1` | 0.712 s | listen (1.3, "Bifrons rises to listen"); assent (1.0, "Bifrons assents") |
| `banana_slug_hurt_2.ogg` | `files[24321]`; `copies[961]` | `2cc9c8b11f2e56305e53700313cf2a174137fd984fcb7d7508b32b3424dbfabb` | 0.636 s | inspect (1.1, "Bifrons measures"); refuse (0.7, "Bifrons refuses") |

Each library source is the same name under `art/owner-library/originals/Edited-2026-10-18-Part2/jims mobs/sounds/` (the
owner's "jims mobs" folder of the second edited part; its catalog entries counted from 0). All three are mono, 44.1 kHz.
His seven events, `jugcraft:bifrons.*`, carry the subtitles above; the runtime event `beast.banana_slug_slime_expand` and
its files are unchanged, and no file is added. The library's notices are kept with the originals; provenance as
`art/owner-library/PROVENANCE.md` records the owner's declaration. The library is unchanged and nothing in it is
executed.

Reference checks (GC-S24) for what this brief names, read for it on this line: `Items.HANGING_ROOTS` (mc263src
`net/minecraft/world/item/Items.java:408`); `PactSpiritEntity.BESIDE_BLOCKS = 2`, `Station.BESIDE`, the six-component
`Body` and the station rule (`S/PactSpiritEntity.java:118, 138-150, 421-475`; Marax's `Body`, `S/MaraxEntity.java:91`);
`Signs.show(ServerLevel, BlockPos, Sign)` and `Sign.GATHER`, a preparation sign of 12 particles
(`concordance/sign/Signs.java:82`, `Sign.java:25`), and `check_signs` refusing `sendParticles` under `concordance/`
(`tools/check_mod_data.py:25217-25218`); GeckoLib 5.5.7's `RawAnimation.begin()`, `thenPlay` and `thenLoop`
(`geckolib/src/common/src/main/java/com/geckolib/animation/RawAnimation.java:42-57`) for the listening loop's rise, then
its loop. Whether the main controller plays those stages in order, and every call GC-08 writes, is confirmed only by
compiling and running in CI (the specification's section 15). (`S/` is
`src/main/java/io/github/jimbozoomer/jugcraft/concordance/spirits/`.)

## Verification

Not run: nothing of Bifrons is built yet, so no build, game test, client shot or audit exercises him, and nothing of
him has run in CI. For this brief only (29 October 2026), each SHA-256 above was computed with `sha256sum` from the
library file and, for the sounds, from the runtime copy too, and each equals its catalog row or entry:
`art/owner-library/catalog/files.csv` lines 2013 and 2071 (the header line 1), as unit 5's specification and the
contract's art plan give them, and for the sounds the second edited part's `files.json` entries and `beast-runtime.json`
copies above, which the specification did not name. The two paintings' colours were counted (both 16 x 16 and wholly
opaque: the pine log's six colours, the sphalerite's five) and the three sound files' Vorbis identification headers read
(one channel, 44.1 kHz; each length from its last page's granule position). The specification's body section was run
once in memory on this line's `tools/garden_bodies.py` helpers, writing nothing: no shared face plane, twelve clips, and
the four SHA-256 the specification records for the skin's and the glowmask's pixels and the geometry and animation files,
unchanged by the helpers units 1 to 3 built. That is not a check of the repository: his runtime files are written and
checked by `tools/owner_art.py` when GC-08 lands. The tests, shots and runs that verify him are listed here then.
