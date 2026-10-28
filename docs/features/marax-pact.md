# Marax's pact (garden cohort, GC-06)

Concordance garden cohort, GC-06 ([contract](concordance-garden-cohort.md#gc-06-marax-unit-3); Dossier 3). Marax, the
twenty-first spirit, keeps a familiar that knows the ground: asked at his perch, it reads its holder's planted Verdant
Beds from what they last showed and names the one change that would help the worst of them, changing nothing. He is the
cohort's third garden spirit, on the same pact record, perch, screen, roster, journal and Jade lines as Stolas, Buer and
Bathin, and the first to stand on the ground beside the perch instead of on it.

Status: brief only (GC-06a), committed before any of his body files (D-D5), as the contract's per-spirit template asks.
Nothing of Marax is built yet: no entity, pact definition, service, sound event, lang, body file, audit or test. This
page holds his character (the contract's Dossier 3) and the owner files his body and cues are to use; the runtime files
and builders it names are what GC-06 is to build. Where it goes beyond Dossier 3 (the greeting's stage direction, how
his body is built, which cue plays which file at what pitch) it is unit 3's proposal (GC-P63 to GC-P79, added to the
contract's Decisions when the unit lands), and the palette files may change only here, before any body file (the
contract's art plan). The rest of the record (player experience, record fields, connections, costs, failure behaviour,
the pact screen, his reading and the proud familiar) is added with the code.
Proposal issue: none; the owner's selected scope ([the cohort's contract](concordance-garden-cohort.md), GC-D1 to GC-D3).
Owner: @jimbozoomer-byte. AI-assisted implementation with Claude Code; the model is named in the commit trailers.

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
  paper a week. My familiar writes small."
- **Refusal:** "My familiar reads what grows. Nothing grows here." **Departure:** the familiar settles between his horns,
  he turns, walks two steps and fades.

## Dependencies and assets

No dependency, pin or mixin. His body is built by `tools/garden_bodies.py` (no owner rig is a bull with a man's face):
geometry and clips are code; his 128 x 64 skin and glowmask are painted only in the colours of three of the owner's
paintings, read as data and never copied or edited:

| Painting (under `art/owner-library/originals/Blocks/`) | Catalog row | SHA-256 | Colours | Used for |
|---|---|---|---|---|
| `biomes and tree blocks/mahogany_log.png` | `files.csv:1925` | `1ed1e99490963ed3137a583c259dfd1440d23a4b391f0e1b761f073106e7c180` | 5 | hide, face, horns, hooves |
| `biomes and tree blocks/spanish_moss.png` | `files.csv:2069` | `063d9313efbe9a01921461829d92eab3ecaa3c0ef6bcaf0b95c4aef06d13e3c7` | 8 | hump, forelock, tail tuft, the familiar's eyes |
| `biomes and tree blocks/cypress_leaves.png` | `files.csv:1758` | `d85109e4f50b37908248dda43511d38741b1be9780c6e554d18421320f434ae5` | 6 | the familiar's body and leaf wings |

His cues play four runtime sounds already in the repository, copied unchanged from the owner's library (the art plan's
earth family, `nouveau.gaia_family`), told apart by pitch:

| Runtime file (`assets/jugcraft/sounds/`) | Library source (`art/owner-library/originals/Magic/`) | Catalog row | SHA-256 | Cues |
|---|---|---|---|---|
| `nouveau/gaia_1.ogg` | `assets/ars_jimbaux/sounds/gaia_1.ogg` | `files.csv:20370` | `236c4f69eca1bf904cea9257562483cbf5327d84a668242c7503e985c949f0ae` | arrive; depart at 0.85 |
| `nouveau/gaia_2.ogg` | `assets/ars_jimbaux/sounds/gaia_2.ogg` | `files.csv:20371` | `7558cd611e6103be9268777dbbbdffea350531a644320e9c756e4fbdd2fef318` | listen at 1.3; refuse at 0.7 |
| `nouveau/gaia_3.ogg` | `assets/ars_jimbaux/sounds/gaia_3.ogg` | `files.csv:20372` | `7845aa4bf0046b56b8895953817553eba46034e6916d5d40e8a9e9dc0a3d74e2` | inspect; work at 1.5 |
| `nouveau/gaia_4.ogg` | `assets/ars_jimbaux/sounds/gaia_4.ogg` | `files.csv:20373` | `673c8c28f3f88fda25ac6fdedddc5f91822fb0d46b37c4973859a3f662d9d11c` | assent |

All four are mono, 44.1 kHz, 1.65 s. The library's notices are kept with the originals; provenance as
`art/owner-library/PROVENANCE.md` records it. The library is unchanged and nothing in it is executed.

## Verification

Not run: nothing of Marax is built yet, so no build, game test, client shot or audit exercises him. For this brief only,
each SHA-256 above was computed with `sha256sum` from the library file (and, for the sounds, from the runtime copy too),
and each equals its `art/owner-library/catalog/files.csv` row (line numbers as above, the header line 1); the three
paintings' colours were counted (all 16 x 16: the mahogany log's five opaque, the Spanish moss's eight and the cypress
leaves' six opaque around clear texels) and the four sound files' Vorbis identification headers read (one channel,
44.1 kHz, 1.65 s each). The tests, shots and runs that verify him are listed here when GC-06 lands.
