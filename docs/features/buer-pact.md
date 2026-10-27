# Buer's pact

Status: **briefed, not built** (Concordance garden cohort, GC-04a). This page is Buer's brief: the
[contract](concordance-garden-cohort.md)'s Dossier 1 and the palette files his body and cues are made from (library
path, catalog row, SHA-256), committed before any body file, as
[the per-spirit template](concordance-garden-cohort.md#the-per-spirit-template-every-spirit-ticket-gc-04-to-gc-09) asks
(D-D5). Nothing of Buer exists in the game yet: no entity, data, words, sounds, body files or tests. Everything below
the brief is the design GC-04 builds, and every result is "not run" until it has run.
Proposal issue: none; the owner's selected scope ("gardens next", D-D5; the contract's GC-D3) and
[GC-04](concordance-garden-cohort.md#gc-04-buer-end-to-end-unit-1).
Owner: @jimbozoomer-byte. AI-assisted implementation with Claude Code; the model is named in the commit trailers.
Target milestone and tier: the garden cohort's unit 1; Practitioner stage, Spiritbinders (tier: practitioner).
Primary specialty and supported player role: the Binding Arts with the garden's keepers. Buer, the tenth spirit, mends
what a garden's harvests strain: a pact with him eases the harvest strain of its holder's (or party's) Verdant Beds by
day, one bed at a time and never faster than each bed's own rule.

He is the cohort's first garden spirit and the second spirit a Pact Perch can invite, on the shared pact record, perch,
screen, roster, journal and Jade lines that Stolas uses ([Stolas's pact](stolas-pact.md); GC-01 to GC-03). Builds on
[spirits, familiars and constructs](arcane-concordance-workers.md) and
[ecological cultivation](arcane-concordance-ecology.md) (Verdant Beds, strain, tending). The proposals GC-P20 to
GC-P22 are the contract's; none is the owner's.

## The brief (Dossier 1, entry 10: strain tending)

Copied from the contract's
[Dossier 1](concordance-garden-cohort.md#dossier-1-buer-entry-10-strain-tending). What this brief adds or corrects is in
*italics*, each point checked against the files it names; item 8's shared tests carry the names GC-01 built them with.

1. **Identity.**
   - *Source phrases (the 1904 text, The Book of the Goetia of Solomon the King: Mathers, translator, credited as "a
     Dead Hand"; Crowley, editor; entry 10. The contract's labels: [S] wording matched over transcriptions, [s] sense
     confirmed, [U] recollection; no scan was opened):* "The Tenth Spirit is Buer" [S]; "He appeareth in Sagittary, and
     that is his shape when the Sun is there." [S]; "He teaches Philosophy, both Moral and Natural, and the Logic Art,
     and also the Virtues of all Herbs and Plants." [S]; "He healeth all distempers in man, and giveth good Familiars."
     [S]; "a Great President" [s]. The identity rests on "the Virtues of all Herbs and Plants" (the only entry naming
     plants, by the surveyor's recollection of the others [U], to be confirmed against a scan) and "He healeth all
     distempers". **Jugcraft adaptation:** his healing is of a garden's distemper, a bed's harvest strain; no real
     remedy for a real illness. *No source quotation goes in game (GC-S14): his words are Jugcraft's.*
   - *Silhouette:* a rooted **corolla**: four broad petals round an upright pistil that leans like an arrow toward
     whatever he attends (Sagittary as a motif only, never a centaur or archer); legless, the only rooted spirit. Body:
     the owner's Decor Blossom, its texture byte for byte and its geometry adapted (stem lengthened, *the clear face of
     each two-faced plane dropped*; [below](#palette-and-body-files)). *At the planned scale 2.2: about 1.4 blocks
     across shut (the 10-pixel leaf plate), about 1.7 open (the owner's `open` pose; an estimate here, held by
     `check_spirit_bodies` once built) and about 1.6 tall.*
     Not imported: the lion-headed wheel of five goat legs (de Plancy and Le Breton, 1863), Genshin Impact's Buer
     (Nahida). *Nor, by GC-S14, any other adaptation's design, seal, conjuration or legion count.*
   - *Personality:* a physician's calm; exact and kind; disapproves of overwork; speaks in diagnoses.
   - *Beats (GC-P20):* greeting "A bed that is worked must also be mended. I am Buer; I mend."; service "By daylight I
     ease what your harvests strain, one bed at a time, by the bed's own rule and no faster."; price "A Mendvetch each
     week, for my own mending. And when I call a bed overworked, feed it before you ask me again."
2. **Invitation and entry route.** The Garden's Regard *(`jugcraft:garden_regard`, built in GC-03)* understood; an awake
   Verdant Bed the inviter keeps within 8 blocks of the perch. Codex: Binding, The Garden's Regard, page "Buer". The
   journal's pacts section names the next step.
3. **Service: strain tending** (`strain_tending`). Each think while serving: from the bed index, the awake beneficiary
   bed with the highest `strainNow()` (then nearest) that the bed's own rule allows tending now (`mayTendAt(now)`,
   *read-only, GC-03*), is not overworked and that the present holder may change; `bed.tend(now)` eases one strain (it
   first counts a harvest seen, as by hand); the task is counted on the record. The 600-tick rule belongs to the bed, so
   his tending and the holder's share it. No nutrients, items or practice for the holder.
4. **Upkeep and suspension (GC-P21).** `#jugcraft:buer_offerings` (Mendvetch) ×1 every 7 game days, 1 day's grace, 4
   ahead; compact: 4 Mendvetch, 4 bone meal *(resonance 0)*. Terms: radius 8, hours 0 to 12,000 (daylight), 16 beds
   eased a day, beneficiaries `party`. Suspended: no tending; nothing taken or lost.
5. **Conflict: the overworked bed** (resolved by a task; GC-P22). A bed he eased today that stands at strain 3 again is
   overworked: he names it in a service row on the kept page and in Jade ("worked faster than it heals: feed it"; not
   on the roster, whose status is a saved id, GC-S11), and will not tend it while it stays at 3. Composting it (bone
   meal or fertilizer by hand, or a Mulch Maw's nutrient) eases a strain by the bed's own rule; at 2 he takes it back.
   *Bone meal on a full bed composts nothing (`VerdantBedBlock.useItemOn` returns before `compost()` when nothing is
   given), so the row also names rest: a bed with no crop growing rests a strain away on a random tick.* Derived from
   the bed's strain and his memory of today's eases (at most 16 positions, not saved: a restart forgets the complaint,
   never a debt).
6. **Refusal and departure.** Refusal: "There is nothing here I could mend." (no awake bed of the inviter's within 8;
   *nothing spent*). Departure: his petals close and he sinks into the perch; struck off the roster, escrow returned
   once, a history entry.
7. **Interaction details.** (a) Outside his hours his petals fold shut and he sleeps (the `waiting` loop at night, with
   the kept page saying "asleep until dawn"). *Limit, planned: with an offering due his loop is the astir one (half
   open) by day and by night, since the pact's status stays "upkeep due"; the kept page and Jade still say "asleep
   until dawn" at night (GC-P25, GC-P27; recorded, not hidden).* (b) Before he unfurls over a bed, his pistil points at it
   for a moment (attend, then the work gesture), so a watcher sees which bed he chose; *his work over, he turns back to
   whoever is speaking to him (GC-02).*
8. **Evidence.** *Accessibility:* every cue subtitled and every gesture paired with words on the kept page, the roster
   and Jade; his screens keyboard-only and narrated, fitting 320x180 and GUI 4; the overworked row read aloud; asleep
   and overworked said in words, never by pose alone. *Ownership:* `buerTendsOnlyBedsOfHisBeneficiaries`,
   `buerNeverTouchesAClaimedOrDormantBed`, `anotherPlayerCannotAcceptForAnySpiritsHolder` and
   `everySpiritWaitsForANearHolder`. *Save/reload:* `everySpiritsKeysSurviveASave` for `jugcraft:buer`; his memory of
   eases is unsaved by design (`anOverworkedComplaintIsForgottenOnRestart`). *No duplication:* he moves no item and no
   nutrient (`buerEasesOneStrainAtTheBedsOwnRate` checks both); `aCopiedPerchIsInertForEverySpirit` and
   `onePresencePerPactForEverySpirit`; `twoBuersShareOneBedsTendWindow` (two holders' Buers over one party bed ease it
   once a window).

## Palette and body files

Named here before any body file. Each was read for this brief from `art/owner-library/` (nothing in it executed or
changed): its catalog row, and its SHA-256 computed with `sha256sum`, which matched the catalog's. The folder's label
in the catalog is "Magic / assets/ars_jimbaux".

| Library file (`art/owner-library/originals/Magic/assets/ars_jimbaux/`) | Catalog row | Size | SHA-256 | Runtime file it makes (`src/main/resources/assets/jugcraft/`) |
|---|---|---|---|---|
| `geo/decor_blossom.geo.json` (geo format `1.12.0`) | `catalog/files.csv:19364` | 5,312 bytes | `3209e72155163331b462fb5cf1d2062917c34ec7c972987b51886146adfe4d4e` | `geckolib/models/entity/buer.geo.json` (adapted) |
| `animations/decor_blossom_animations.json` (clips `open` 1.6667 s, `close` 1.75 s, `active` 5 s looped) | `catalog/files.csv:19091` | 3,364 bytes | `368a8b72ec7d921eca0cb3f89b764df55e29ddf6aecf3fc16659a41572f191db` | `geckolib/animations/entity/buer.animation.json` (adapted) |
| `textures/block/decor_blossom.png` (32x32) | `catalog/files.csv:20417` | 314 bytes | `2334749d3e11db8e6880bc60fb5d76081c24f11fe14e4e26ad02b330a263a68b` | `textures/entity/buer.png` (byte for byte) and `textures/entity/buer_glowmask.png` (adapted) |

The planned technical changes (GC-S12; the unit's proposals, recorded in the contract's Decisions with its records),
made by a new pure generator, `tools/garden_bodies.py` (no I/O, no randomness), and written and checked by
`tools/owner_art.py`; the originals stay intact:

- **Texture:** copied byte for byte. No texel is redrawn or recoloured.
- **Stem lengthened:** the `base` bone's stem cube (origin `[-1, 0.3, -1]`, size `[2, 2.7, 2]`) repeated once upward
  (at `y = 3.0`), and every other bone's pivot and cubes raised by its height, 2.7 px (petal pivots 3 to 5.7, the pistil
  1 to 3.7, the blade pivots 2 to 4.7, the cap 9 to 11.7), so every clip still turns each bone about its own pivot.
- **The clear face of each two-faced plane dropped.** Four cubes have zero thickness and give both faces of their
  plane; GeckoLib draws entities with `entityCutout`, which does not cull in 26.3, so each pair is the reversed twin
  `docs/ART_DIRECTION.md` ("Two-sided planes") forbids. Of each pair the face whose texels are all clear is dropped, so
  no drawn texel is lost and one quad is seen from both sides. Read from the owner's sheet for this brief:

  | Cube (bone) | Face kept: texels drawn | Face dropped: texels drawn |
  |---|---|---|
  | leaf plate `[10, 0, 10]` (`base`) | `up` (`uv [0, 0]`, 10x10): 68 of 100 | `down` (`uv [22, 32]`, 10x-10): 0 of 100 |
  | pistil cap `[4, 0, 4]` (`pistil`) | `up` (`uv [15, 5]`, 4x4): 12 of 16 | `down` (`uv [15, 13]`, 4x-4): 0 of 16 |
  | pistil blade `[2, 6, 0]` (`pistil`, two, one turned 90°) | `south` (`uv [0, 16]`, 2x6): 12 of 12 | `north` (`uv [15, 13]`, 2x6): 0 of 12 |

  *Correction to the contract:* its art plan and GC-04 drop the blades' `south` face, which would erase the drawn
  stalk; the clear face is `north`. The generator computes the clear face from the texture and stops if a pair is both
  drawn or both clear (which to keep would then be the owner's choice). The planes' zero-size side faces stay as the
  owner wrote them: GeckoLib never bakes them.
- **Glowmask:** the pistil cap's `up` texels whose red is full, `(16, 6)`, `(16, 7)` and `(17, 7)` (the pale yellows
  `(255, 253, 156)` and `(255, 254, 208)` at its heart), as the texture draws them; everything else clear. GeckoLib's
  `AutoGlowingGeoLayer` reads `buer_glowmask.png` beside `buer.png`, as it does Stolas's.
- **Clips:** the owner's `open`, `close` and `active` kept unchanged (`active` is his idle loop), then his own loops
  (listen, waiting, suspended, astir) and gestures (arrive, inspect, assent, refuse, depart, work) keyed on the rig's own
  bones `base`, `petal1` to `petal4` and `pistil` (the `petal1a` to `petal4a` bones stay unkeyed, as the owner's clips
  leave them).
- **Scale and box (planned):** drawn at 2.2; entity box 1.2 x 1.6 (the contract said about 1.2 x 1.2, but the drawn
  flower is about 1.6 tall).
- **Supplied labels and metadata, retained as provenance:** the folder's leftover mod metadata names Ars Nouveau, author
  Bailey Hollingsworth, `license="GNU GENERAL PUBLIC LICENSE"`
  (`art/owner-library/ports/jug-nouveau/ars_jimbaux-1.21.1-5.13.3/META-INF/neoforge.mods.toml:6, 11, 14, 15`), and the
  folder's language file carries a supporter-reward message that Jugcraft does not import. The owner's declaration is
  [PROVENANCE.md](../../art/owner-library/PROVENANCE.md). The same files already make the decorative block
  `jugcraft:nouveau_decor_blossom` ([Jug Nouveau](jug-nouveau.md)), which keeps its own use: a block, not a creature.

## Sound files

The art plan's four mono files for Buer (bloom and healing; GC-S13), already in runtime. Each runtime file's bytes equal
its library original's (the SHA-256 below is both). Read for this brief: each file's Vorbis identification header (one
channel) and its last granule (length).

| Runtime file (`src/main/resources/assets/jugcraft/sounds/`) | Library original (`art/owner-library/originals/`) | Catalog rows | SHA-256 | Channels, rate, length |
|---|---|---|---|---|
| `menagerie/fable/butterfly_heal.ogg` | `Edited-2026-10-18-Part2/legendarycreatures/sounds/butterfly_heal.ogg` | `catalog/edited-2026-10-18-part2/fable-runtime.json:379` (`copies[75]`); `files.json` `files[26346]` | `0158ef770513c7feeba77a32e6f794772684ce738715caa649e82a4cdebbfa4b` | 1, 48 kHz, 2.00 s |
| `menagerie/grove/entity/cactem/entity.cactem.heal.ogg` | `Edited-2026-10-18-Part2/cnb/sounds/entity/cactem/entity.cactem.heal.ogg` | `grove-runtime.json:539` (`copies[107]`); `files.json` `files[122]` | `596f86ff47e14788ad44bd67925c564ebccb3c49479463c317e40ed2a606a642` | 1, 44.1 kHz, 2.25 s |
| `menagerie/grove/item/item.heal_spell_book.player_heal.ogg` | `Edited-2026-10-18-Part2/cnb/sounds/item/item.heal_spell_book.player_heal.ogg` | `grove-runtime.json:804` (`copies[160]`); `files.json` `files[175]` | `1f925cea36d47f38309ac39b1d69623280e478ec11c023fbb8f541f4750b76d6` | 1, 44.1 kHz, 2.25 s |
| `alchemy/peheal.ogg` | `Magic/sounds/item/peheal.ogg` | `catalog/files.csv:26691` | `96500aa60f6fd497a8ca5e751416683d0ccb424ac6dc1aa939abb249c391cc93` | 1, 22.05 kHz, 0.99 s |

The two Grove files are one recording: same length, and they differ in 39 bytes, all Ogg stream serials and page
checksums. So his seven cues draw on three recordings, told apart by pitch through `sounds.json` (planned; a proposal
of the unit):

| Event (planned) | Subtitle | File | Pitch |
|---|---|---|---|
| `jugcraft:buer.arrive` | Buer unfurls | `menagerie/fable/butterfly_heal` | 1.0 |
| `jugcraft:buer.listen` | Buer listens | `alchemy/peheal` | 1.25 |
| `jugcraft:buer.inspect` | Buer considers | `alchemy/peheal` | 1.0 |
| `jugcraft:buer.assent` | Buer assents | `menagerie/grove/entity/cactem/entity.cactem.heal` | 1.0 |
| `jugcraft:buer.refuse` | Buer refuses | `alchemy/peheal` | 0.6 |
| `jugcraft:buer.depart` | Buer closes and sinks away | `menagerie/fable/butterfly_heal` | 0.7 |
| `jugcraft:buer.work` | Buer mends a bed | `menagerie/grove/item/item.heal_spell_book.player_heal` | 1.0 |

Never Stolas's doorbell chime or flutter; no voice; no new audio; no downmixed copy.

## Reference checks (GC-S24, for the body and sounds)

| Fact | Where it was read for this brief |
|---|---|
| A face absent from a per-face `uv` map is no quad | GeckoLib 5.5.7 `loading/definition/geometry/GeometryUvMapping.java:25, 49-53` (`fromDirection` null, `bakeQuad` returns null) |
| A zero-size face is never baked | GeckoLib `loading/definition/geometry/GeometryCube.java:79, 87` (`isZeroSizeFace`) |
| A null quad is skipped when drawn | GeckoLib `cache/model/cuboid/GeoCube.java:36` |
| GeckoLib draws entities with `entityCutout` | GeckoLib `renderer/base/GeoRenderer.java:74` |
| `entityCutout` does not cull; alpha cut-out 0.1 | 26.3 `net/minecraft/client/renderer/RenderPipelines.java:92` (`ENTITY_CUTOUT`: `withCull(false)`, `ALPHA_CUTOUT` 0.1) |
| Geo format `1.12.0` is read | GeckoLib `loading/definition/geometry/object/ModelFormatVersion.java:13` |
| A `sounds.json` entry may carry `pitch` | 26.3 `net/minecraft/client/resources/sounds/SoundEventRegistrationSerializer.java:69` |

## Record fields (CLAUDE.md)

| Field | Buer's pact (planned) |
|---|---|
| Tier | Practitioner, Spiritbinders: The Garden's Regard needs the Binding Arts and Verdant Husbandry understood |
| Input producer | a garden specimen examined, a ripe organism crop harvested by hand, a bound worker kept at its work (the Regard, GC-03); the perch (two sticks, an amethyst shard, a dark oak log, polished deepslate); an awake Verdant Bed of the inviter's within 8; the compact (4 Mendvetch, from two crafts of wheat seeds, bone meal and an amethyst shard, two each; and 4 bone meal); offerings (`#jugcraft:buer_offerings`: Mendvetch) |
| Output consumer | the beds' strain (growth and the habitat read it); the record's day count; the holder's first obligation (tending a strained bed in his area by hand while the pact serves), one of the three distinct garden spirits' obligations that master The Garden's Regard (from unit 3) |
| Costs | the compact once; one Mendvetch every 7 game days, at most 4 ahead; no resonance; no per-tend cost |
| Unlocks | the invitation (Regard understood and a bed); strain tending (the pact); one obligation toward The Garden's Regard mastered |
| Failure behaviour | every refusal spends nothing and names its reason; outside his hours, with his holder away (no stand-in, whatever the server's absent-owner option), suspended, held for upkeep or past his quota, he tends nothing and says why; an overworked bed waits for compost or rest; a missed offering warns, gives a game day's grace (he works on), then suspends with nothing lost; ending returns what was paid ahead once; a broken perch ends nothing; switched off, everything waits |
| Test evidence | not run: nothing of Buer is built yet. The plan is below (Verification) |

## Connections

- **Producers:** The Garden's Regard and the garden groundwork (GC-03: the bed index `GardenReach`, the beneficiary
  rule `GardenPacts`, `PactObligations`); the garden (Verdant Beds, organism crops, Mendvetch's recipe, bone meal,
  fertilizer, the Mulch Maw); the pact hall's perch, screen, record, upkeep, escrow and history (Chunk D, GC-01).
- **Consumers:** each bed's strain, through the bed's own `tend` (the call a Greenwarden's hand makes); the Bound Will's
  day count; `PactObligations` (the holder's own tending); the roster, the journal's pacts lines and Jade.
- **Shared systems:** `PactSpiritEntity` (his body, cues, gestures, facing), `PactService` and `PactServices`,
  `GardenReach`, `GardenPacts`, `PactObligations`; no new registry, currency, energy, item, block or block entity
  (GC-S22).
- **Not required:** any other branch. Solo and Overworld: every input above is a survival craft or the garden's own
  crop; trading is not needed.

## Costs, balance and automation

Strain tending removes at most 16 strains a game day across the holder's (or party's) beds, never faster than each
bed's own 600-tick window; a hand's tending shares that window, so a Buer adds no second channel to a bed. He moves no
nutrient, item or Verdance, so the garden's nutrient economy and its no-positive-loop rule (C-P61) are untouched and
`tools/concordance_economy.py` must still report no gaining cycle. His upkeep is the crop he mends; the compact costs
two Mendvetch crafts and four bone meal. The convenience automates the hand's tending without removing its purpose:
the overworked bed still asks for compost or rest.

## Failure behaviour, multiplayer and persistence

Server authority: every change is the server's, at his think (every 20 ticks), asked at the change: the pact serves
there (area, hours, quota, not suspended or held), the keeper is a beneficiary now (the holder; their party under
`party`), the holder is online, alive, in the perch's level and within 64 blocks, and could change the bed by hand
(`Authority.mayChange`). No stand-in ever answers. Bounded: the bed index reads only loaded chunks (at most 64 beds,
refreshed every 200 ticks); at most 8 beds asked and one changed a think; at most 16 remembered; no chunk loaded, no
habitat sampled. Saved: nothing new (his entity's `perch` and `record`, Chunk D's keys); his memory of today's eases and
complaints is never saved, so a restart forgets it and owes nothing. One presence a pact; a copied perch is inert.
Stable ids once released: `jugcraft:buer`, `jugcraft:buer_pact`, `#jugcraft:buer_offerings`, `jugcraft:buer.*` sound
events, the work `strain_tending` (saved in a Bound Will's service; appended, never reordered).

## The pact screen

The shared screen (GC-01): the chooser lists him once a bed of the player's is within 8 (else "not yet", with his
reason); his beats and terms, keyboard only, at GUI 4 and 320x180; the kept page's service section (today's eases, or
asleep until dawn; the overworked bed) and the shared first-obligation row; offerings, suspend, resume and end. No
garden term or refusal names an astrolabe.

## Dependencies and assets

GeckoLib (approved; his model, clips, glowmask layer); Jade optional (his note, behind `compat/`). No new dependency or
pin. Files: [Palette and body files](#palette-and-body-files) and [Sound files](#sound-files) above.

## Verification

Nothing has run for Buer: this commit is his brief, with no game change. Each item of the brief is to be shown by the
evidence named here (the contract's GE9), and each result is written from the run that produced it.

| Brief item | Evidence planned (server game tests in `ConcordanceWorkerGameTests` unless named; client shots of `ConcordanceGardenPactClientGameTests`) | Result |
|---|---|---|
| 1. Identity | his body files from `tools/owner_art.py` (`--check`), `check_spirit_bodies` (bones, clips, scale, a box at least 20% unlike every other spirit's, the twin-face rule), `check_geckolib` per face; shots of his poses from his front (`jugcraft_garden_buer_idle` and the rest) and from below (`jugcraft_garden_buer_below`) | not run |
| 2. Invitation and route | `ConcordanceJourneyGameTests.buerFromTheCodexToDeparture`; `aPerchOffersOnlyWhatThePlayerMayInvite`; `ConcordanceJournalGameTests.theJournalNamesEachRegardsNextStep`; `tools/concordance_journey.py --check` (his route); shot `jugcraft_garden_chooser` | not run |
| 3. Service | `buerEasesOneStrainAtTheBedsOwnRate`, `buerTendsOnlyBedsOfHisBeneficiaries`, `buerNeverTouchesAClaimedOrDormantBed`, `buerSkipsBedsHeMayNotChange`, `buerWaitsForAPresentHolder`, `buerChangesAtMostOneBedAThink`, `buerStopsAtHisQuota`, `buerTendsThroughHisGrace`; shot `jugcraft_garden_buer_at_work` | not run |
| 4. Upkeep and suspension | `everySpiritSealsOnceAndSpendsOnce`, `everySpiritsUpkeepWarnsGivesGraceSuspendsAndResumes`, `aSuspendedBuerTendsNothing`, `noGardenTermOrRefusalNamesAnAstrolabe`; `check_acquisition` (his compact and offering reachable); shots `jugcraft_garden_buer_terms` (three sizes) | not run |
| 5. The overworked bed | `anOverworkedBedWaitsForCompost`, `anOverworkedComplaintIsForgottenOnRestart`, `buersReadingSaysItsNoteAndNamesNoPlayer`; shots `jugcraft_garden_buer_overworked`, `jugcraft_garden_buer_jade` | not run |
| 6. Refusal and departure | `buerRefusesWithoutABedToMend`, `everySpiritsRefusalSpendsNothing`, `buerAnswersEachActionWithHisGesture`, the end of `buerFromTheCodexToDeparture` (escrow once); shots `jugcraft_garden_buer_nothing_to_mend`, `jugcraft_garden_buer_depart`, `jugcraft_garden_buer_ended` | not run |
| 7. Interaction details | (a) `buerSleepsOutsideHisHours`, shot `jugcraft_garden_buer_asleep`; (b) `buerFacesTheBedHeTends` (the bed, then his speaker, then no one), shots `jugcraft_garden_buer_work` and `jugcraft_garden_buer_faces_east`, `_south`, `_west`, `_north` | not run |
| 8. Evidence | the client class keyboard-only at GUI 4 and 320x180 (`_scale4`, `_small` shots; every screen read for the narrator); ownership, save and duplication as item 8 names them, plus `aDuplicateBuerTendsNothing` and `twoBuersShareOneBedsTendWindow`; GC-03's three obligation tests (`anObligationIsRecordedOncePerPlayerAndSpirit`, `anObligationNeverMastersStolassRegard`, `anObligationOutsideTheAreaOrWhileSuspendedCountsNothing`) | not run |

- **Server** (`./gradlew build`, CI): not run.
- **Without the optional integrations** (`-PjugcraftOptionalIntegrations=false`, CI): not run.
- **Client** (`./gradlew runClientGameTest -PclientTests=ConcordanceGardenPactClientGameTests`, CI): not run; the class
  does not exist yet. Every shot named will be looked at and marked seen before it is called evidence.
- **Local, for this brief:** the library files' SHA-256 and catalog rows, the sound headers and the texels above, read
  as data. The repository's local checks on this commit are reported in its commit message.
- **Not covered by the plan:** people; sound (CI has none); two clients at Buer's perch (the network test is Stolas's);
  the narrator heard; a player's hardware; the overworked bed reached by real harvests in a game test (the tests'
  strain setter stands in).

## World and event applicability

Overworld, Nether and End alike (a perch and beds in one level; his area never crosses levels). No seasonal gate. No
cave, dungeon, boss or loot role.

## Rollout and open questions

Ships with the garden cohort's unit 1 (GC-01 to GC-04), behind the Concordance's switch; GC-04 lands after this brief.
Open: Stolas's six two-faced box-UV cubes (kept as the owner made them; the twin rule will record them, a question for
the cohort's checkpoint); the chooser not in the whole-path test (it needs Stolas's Regard; shown by the server test and
the client class); his astir loop not photographed; an offering due shows him astir at night (item 7a); the Goetia
citation's edition (the contract's GB18) is settled only after Project Gutenberg's record #72679 is read.
