# Bathin's pact

Concordance garden cohort, GC-05 ([contract](concordance-garden-cohort.md#gc-05-bathin-unit-2); Dossier 2). Bathin, the
eighteenth spirit, sets Verdance down at once where it is wanted: from a Verdant Heart into a far Gleaner, or into a Ley
Pylon away from the beds, with nothing ever on the way. He is the cohort's second garden spirit, on the same pact record,
perch, screen, roster, journal and Jade lines as Stolas and Buer, and the first to raise a matter for his holder to answer.

Status: brief only (GC-05a), committed before any of his body files as the contract's per-spirit template asks (D-D5).
Nothing of Bathin is built yet: no entity, pact definition, service, sound event, lang, body file, audit or test. This
page holds his character (the contract's Dossier 2) and the owner files his body and sounds are to use; the runtime files
and builders it names are what GC-05 is to build. Where it goes beyond Dossier 2 (his gestures, the greeting's stage
direction, how his body is built, which cue plays which file at what pitch) it is unit 2's proposal (GC-P56 to GC-P58,
added to the contract's Decisions when the unit lands), and the palette files may change only here, before any body file
(the contract's art plan). The rest of the record (player experience, record fields, connections, costs, failure
behaviour, the pact screen, his service and his matter) is added with the code.
Proposal issue: none; the owner's selected scope ([the cohort's contract](concordance-garden-cohort.md), GC-D1 to GC-D3).
Owner: @jimbozoomer-byte. AI-assisted implementation with Claude Code; the model is named in the commit trailers.

## Bathin's character

| | |
|---|---|
| Source | "The Eighteenth Spirit is Bathin. He is a Mighty and Strong Duke, and appeareth like a Strong Man with the tail of a Serpent, sitting upon a Pale-Coloured Horse. He knoweth the Virtues of Herbs and Precious Stones, and can transport men suddenly from one country to another." (Mathers' *Goetia*, entry 18, the edition as `docs/features/styxhexenhammer.md` cites it; wording matched by a search over transcriptions of the 1904 text, no scan opened: [S]). Other spellings: Bathim (the seal's caption), Bathym, Mathim, Marthim (Weyer) [s] (the sense confirmed, the wording not). Not quoted in game |
| What it rests on | "transport ... suddenly from one country to another"; Jugcraft adaptation: an essence, not people (no player transport, which belongs to a travel cohort). "The Virtues of Herbs and Precious Stones", shared with Stolas, Marax and Foras, is not used |
| Silhouette | a broad-shouldered upper body rising from a thick, coiled serpent's tail, about 1.9 blocks; no legs; he hovers coiled on the perch. The horse is left out (its "Pale-Coloured" echoes Revelation's pale horse) |
| Personality | brisk, proud of speed, scornful of roads and waiting; few words |
| Beats | greeting "Roads are for those who must walk them. I am Bathin."; service "What your Hearts hold, I set down where it is wanted: a Gleaner far off, a pylon away from your beds. Nothing is ever on the way."; price "Chaff, four to the week. I carry the green; keep me in husk." (the greeting after the stage direction "A ring of dust settles, and a broad-shouldered man is coiled on the perch as if he had always been there, a serpent's tail wound beneath him.", proposed, GC-P58). Shortened for the magic interfaces ([magic-interfaces.md](magic-interfaces.md); decided by the session in answer to the owner's request for far less reading, reviewable at the checkpoint): stage direction "A serpent-tailed man coils on the perch."; service "What your Hearts hold, I set down where it is wanted."; the greeting and price unchanged |
| Interaction details | (a) he never travels: arrival and departure are instant, a ring of dust and no path; (b) when he carries, both reservoirs shimmer on the same tick and nothing crosses between them |
| Gestures (proposed, GC-P57) | arrive (whole at once, coiled tight with arms folded hard, then uncoiling), listen (arms folded, chest up, head inclined), inspect (leans over the perch, palm up, weighing), assent (chest out, a curt bow, fist to chest), refuse (turns his shoulder, a flat hand cutting sideways, tail lashing), work (one sharp point at where it is wanted, coil and tail drawn in), depart (coil drawn tight, then gone in one frame); waiting (arms folded, head turned away, tail tapping), suspended (slumped, bowed), astir (fingers drumming, tail lashing) |
| Refusal | "Nothing here to carry, or nowhere to carry it." |
| Departure | none seen to leave: his coil tightens and he is gone in a ring of dust |
| Not imported | the horse; the Dodge SRT Demon badge; any game or anime design; the Siren rig (a mermaid with a girl's face and hair, kept for Ember's mermaid skins); no seal, conjuration, title or legion count |

## Dependencies and assets

GeckoLib (approved; his model, clips, glowmask layer); Jade optional (his service line, behind `compat/`). No new
dependency or pin. His body is to be **built** by `tools/garden_bodies.py` (geometry and clips from code; skin and
glowmask painted only in colours read from two of the owner's paintings) and written and checked by `tools/owner_art.py`
(proposed, GC-P56):

| Runtime file | To be built by | The owner's paintings read (`art/owner-library/originals/Blocks/`, catalog row, SHA-256) |
|---|---|---|
| `geckolib/models/entity/bathin.geo.json` | `bathin_model`: 11 box-UV cubes on 8 bones, a 128 x 64 sheet; no flat cube, no shared face plane | none |
| `geckolib/animations/entity/bathin.animation.json` | `bathin_animations`: five loops, six gestures | none |
| `textures/entity/bathin.png` | `bathin_skin`: two or three tones a part, light from the top left, regular scales and scutes, a simple face | `biomes and tree blocks/willow_log.png` (`catalog/files.csv:2164`, `977f1d86eb2a9d30bd3ca7a1f69d592f9c3fc881eb167c2e4b4359e7e974b895`): five bark tones for his coil, tail, skin, brow and mouth; `biomes and tree blocks/spanish_moss.png` (`catalog/files.csv:2069`, `063d9313efbe9a01921461829d92eab3ecaa3c0ef6bcaf0b95c4aef06d13e3c7`): seven of its eight greens for his mantle, beard, hair, bracers, belly scutes and eyes |
| `textures/entity/bathin_glowmask.png` | `bathin_glowmask`: his two eyes `(83, 27)`, `(86, 27)` as the skin draws them | as above |

Both paintings are 16 x 16: the willow log has exactly five colours, all opaque; the Spanish moss eight opaque greens
around transparent texels. Colours only are read; no texel, shape or layout of the paintings is copied
(`python3 tools/owner_art.py --provenance pacts`, once his body is built: `<its rows>`).

Sounds (each mono, 96 kHz; subtitles to be defined in `tools/concordance_pacts.py`; proposed, GC-P58):

| Event | File (runtime, `assets/jugcraft/sounds/`) | Catalog (`edited-2026-10-18-part2/files.json` entry, SHA-256) | Pitch |
|---|---|---|---|
| `bathin.arrive` "Bathin is suddenly here" | `menagerie/cavern/item/magic_conch/magic_conch_summon.ogg` | `files[19286]`, `cab39bb9c27619f3468e6825fb5631d5398aa848dde5786b904cfdc7a30ece8d` | 1.5 |
| `bathin.listen` "Bathin listens" | `menagerie/cavern/item/sea_staff/sea_staff_bubble_1.ogg` | `files[19312]`, `6fe24381518b50742e9f3253683473d598bb642236a875a91a921972b0cd7414` | 1.2 |
| `bathin.inspect` "Bathin weighs it" | `menagerie/cavern/item/sea_staff/sea_staff_bubble_2.ogg` | `files[19313]`, `9f8bd4244b6df709dc5c99f6792b5769357b847e31b1fee01feed82f1a4d8f4e` | 1.0 |
| `bathin.assent` "Bathin assents" | `menagerie/cavern/item/magic_conch/magic_conch_cast.ogg` | `files[19285]`, `da851dab3160640782da45b05635c8c8ca25915bd5938fb56425fb82951742a0` | 1.0 |
| `bathin.refuse` "Bathin refuses" | the same | the same | 0.7 |
| `bathin.depart` "Bathin is gone" | the same | the same | 1.8 (1.0 s: it ends with him) |
| `bathin.work` "Bathin carries Verdance" | `menagerie/cavern/item/sea_staff/sea_staff_bubble_0.ogg` | `files[19311]`, `e3a77c8ad720436b0619cbce12db51cc69c537f4d9513226457c5612dcd71e2e` | 1.0 |

(The owner's "jims caves" folder of the second edited part: the library files are
`art/owner-library/originals/Edited-2026-10-18-Part2/jims caves/sounds/item/magic_conch/` and `.../sea_staff/`, entries
counted from 0; runtime copies, already in the repository, listed in
`art/owner-library/catalog/edited-2026-10-18-part2/cavern-runtime.json` `copies[1907]`, `[1908]`, `[1933]` to `[1935]`,
each runtime file's SHA-256 equal to its catalog entry. The summon lasts 5.06 s, the cast 1.81 s, the three bubbles 1.53,
1.33 and 1.38 s.) Provenance as the owner declared it: for the sounds,
`art/owner-library/catalog/edited-2026-10-18-part2/README.md` (18 October 2026; `files.json` `ownerDeclaration`); for
the paintings, `art/owner-library/PROVENANCE.md`; the library is unchanged and nothing in it is executed.

## Decisions recorded with the code

These were made after the brief above and go into the records C5 writes for GC-05 (whose status line this page still
carries).

- **Whole moves from a beating Heart (GC-S25, decided by the session; unit 2's review, finding 4, option (a)).** Bathin
  takes from a Verdant Heart only when it holds a whole move (`VerdanceCarrying.MOVE_MAX`, 8) or makes no more Verdance
  (its status is not `working`: full, starved, stalled, on no bed), and then whatever it holds, so a nearly empty, idle
  Heart is still emptied into a Gleaner with room for a whole move (a pylon takes whole batches of 3, so 1 or 2 may
  stay, as before). A Heart beating on its bed makes 1 or 2 Verdance a beat every 200 ticks while he thinks every 20, so
  without this each of his 12 moves a day carried a beat's 1 or 2 (12 to 24 a day); with it a beating Heart gives 8 a
  move into a Gleaner (up to 96 a day), or usually 2 whole batches a move into a pylon (6 Verdance for 4 Ley Charge, so
  72 Verdance a day by that road at that rate), 3 only when it held 9 or more. A Heart answered "take from it" whose own
  pylon takes each beat's whole batches never gathers 8 while it beats: he takes only what its road cannot. His term
  ("up to 8 at a time") and Jade note ("Nothing to carry: the Hearts in his area hold no Verdance he may take", shown
  while a beating Heart gathers its move) are unchanged; the codex's "His Service" page states the rule. The review left
  this to the owner; it is reviewable at the cohort checkpoint. Evidence: the server test
  `bathinCarriesWholeMovesFromABeatingHeart` (a Heart on a lit bed among three kinds of crop: he leaves it while it
  holds less than 8, carries 8 in one move once it has gathered, and empties the rest in one move once its bed is spent)
  and `check_garden_pacts`'s needles on the source rule and on `working` (left only by a beat that made Verdance, and
  set nowhere else in the Heart or its base class); the test is new with this decision and has not been run (no
  Minecraft build where it was written).
- **His pose pictures.** Each gesture's telling window (`SPIRITS["bathin"]["body"]["gesture_times"]`) lies inside the
  part of its generated clip that holds the pose: arrive 0.1 to 0.6 s in the tight coil held from 0 to 0.6 s; inspect 0.4
  to 1.2, assent 0.3 to 0.9, refuse 0.3 to 1.0 and work 0.15 to 0.75, each exactly where its pose is reached and held;
  depart 0.25 to 0.75 in the coil held tight from 0.25 until he vanishes at 0.8 to 0.85 s. No window changed. Arrive's
  and depart's telling poses are the same tight coil, arms folded hard, head down (GC-P57's gestures as designed), so
  their two pictures will look alike; that is left for the owner, not changed here. His loop pictures go through
  `loopPoseOf`, which now waits for and checks the loop with no gesture drawn over it (unit 1's pose fault, run
  40605564166: a finished gesture rewound by the freeze, drawn over a loop); `check_spirit_bodies` holds both. The
  windows were read against `bathin.animation.json` as `tools/garden_bodies.py` builds it; the guard is unverified until
  the client test runs.
- **His pact screen on the relic scroll card** ([magic-interfaces.md](magic-interfaces.md), unit 1; decided by the
  session in answer to the owner's request for far less reading, reviewable at the checkpoint, not owner-accepted): his
  beats shortened as above; his terms as chips with the price as stacks, "1/2" in red on a stack not carried and every
  blocker a lock line above them; his kept page with today's carries as a gauge, his matter ("Heart at 12, 64, -30: take
  from it too?") with Leave it and Take from it as plates (the kept one ticked; its answer "Answer kept"), and his last
  carry as a row with the pylon's or Gleaner's icon. His first task's row draws its act short ("First task: use an awake
  living device by hand here", 10 words; the whole obligation is its tip and narration). While it waits, it stands
  above the matter, and that page may draw the row's words beyond the kept page's budget (a session decision recorded
  in [magic-interfaces.md](magic-interfaces.md), reviewable at the checkpoint).

## Verification

Not run: nothing of Bathin is built yet, so no build, game test, client shot or audit exercises him. For this brief only,
each SHA-256 above was computed with `sha256sum` from the library file (and, for the sounds, from the runtime copy too),
and each equals its catalog row or entry; the two paintings' colours were counted and the five sound files' Vorbis
identification headers read (one channel, 96 kHz, the lengths above). The tests, shots and runs that verify him are
listed here when GC-05 lands.
