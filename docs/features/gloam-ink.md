# Gloam Ink

Status: contract (C-D6); proposed, not built.
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
| GI-01 | The lid and the covered stir: `stir <band> covered`, `Mixture.covered`, `CrucibleBlockEntity.covered`, a formula waits for its lid (`needs_cover`), Peepo, Jade, Inspect |
| GI-02 | The extract (`concordance/extract/gloam_ink.json`, `Extract`), Gloam Ink and Gloam Sludge, the furnace route, the codex entry, JEI pages |
| GI-03 | Bounded and never fed back: the Scale exclusion, values, the survival route, the economy row, `check_gloam_ink` |
| GI-04 | The consumer: a fair copy of Research Notes (`ResearchNotesCopyRecipe`), `check_fair_copies` |
| GI-05 | The living workshop's sixth step; `ConcordanceGloamInkClientGameTests` |
| GI-06 | Records and the checkpoint |

Decisions GI-P1 to GI-P17, all Proposed:
- GI-P1: "covered" is a lid, a closed trapdoor or a slab on the crucible with a whole underside; a stir under it is
  covered; a formula's covered stir waits for its lid before its heat; any other stir is taken as before.
- GI-P2: the approved Hollow carrier is the spider eye (the owner's approval asked).
- GI-P3: Gloam Ink is an item of its own a glass bottle draws, never a named draught; both items stack to 16 and give
  their bottle back when crafted.
- GI-P4: Gloamcap at least once, a spider eye at least once and nothing else, every stir covered: murky (0.25
  contaminant a part) draws Gloam Sludge, at least 1.00 Hollow a part Gloam Ink, thinner the draught. A bowl draws a salve.
- GI-P5: a furnace boils Gloam Sludge back to its glass bottle, nothing more (smelting, 200 ticks, no experience).
- GI-P6: bottling Gloam Ink by hand is an Alembic Arts outcome of its own; sludge counts nothing.
- GI-P7: `needs_cover` is a blocked wait; a Peepo shows it as paused; a Crucible Stand idles.
- GI-P8: neither item is weighed, formed, composted, mulched or an ingredient; both at the values file's fallback.
- GI-P9: Gloam Ink's use is a fair copy: an original sheet, a blank sheet and the ink make one copy; the original and the
  bottle come back; a copy teaches what its original does and credits its writer once.
- GI-P10: only an original is copied. GI-P11: one copy a craft; copies stack. GI-P12: a special crafting recipe, crafted
  on the server. GI-P13: the copy holds only the notes; `fair_copy` is optional and older sheets are originals.
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

## Player experience

With the Alembic Arts understood, grow Gloamcap (in the dark beside working magic, faster beside Dewmoss in a mixed bed)
or craft it from a brown mushroom, glow lichen and an amethyst shard. In an Alembic Crucible over a campfire pour a water
bucket (three parts), add three Gloamcap and a spider eye, close a trapdoor (or set a slab) on the crucible and stir hot
twice: each glass bottle draws **Gloam Ink**, three in all. Uncovered, the same batch is the Poison draught it always was.
Too many spider eyes, too much Gloamcap for the water or a searing stir make it murky: the liquid looks murky, any spoon
and anyone's risk line say so before a bottle is spent, and each bottle draws **Gloam Sludge**, which a furnace boils
back to its glass bottle. Stirred too little, it is thin and bottles as the draught. A recorded formula writes its
covered stirs; a crucible following it waits for its lid before each (Jade, the Inspect page, the status line and a
Peepo say so), and a Crucible Stand heats nothing until the lid is on.

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
  automates the lid; auto-crafters copy as a crafting table does.
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
120 spider eyes, 360 parts of water and 360 bottles. A crucible's most is about 428 a day (five parts, three Gloamcap,
three spider eyes, two hot stirs). A copy: one ink and one blank sheet; the original and the bottle return. Gloam
Sludge's furnace route (smelting, 200 ticks, no experience) gives back only its bottle. Neither item is a resource,
ingredient, specimen, mulch, compost, Scale material or tablet output; no recipe takes the ink; evidence is not
multiplied (one notes key per writer per reader); `tools/concordance_economy.py` reports "gaining cycles: none". Values:
Gloam Ink and Gloam Sludge 1 (fallback), Gloamcap 36, blank sheet 34, glass bottle 1.

## Multiplayer and persistence

The lid, the extract, every bottle and every copy are judged on the server; only someone who may change the crucible
stirs or bottles (Authority); crafting results and remainders come from the server's recipe lookup. Anyone may open a
wooden lid where vanilla allows: a covered formula then pauses and loses nothing (an iron trapdoor avoids it). No new
saved key on blocks: covered stirs live in the mixture's history and the formula's text (stamp 1). `fair_copy` is an
optional notes field written only when true. An older build reads a covered formula as unreadable and keeps it. With the
Concordance switched off both items stay registered; the sludge and copy recipes do not load and the copy is refused.

## Dependencies and assets

No new dependency. Icons adapted from the owner's 23 October collection (provenance table added as built).

## Verification

Planned only (section "Validation plan"); results are written here as they run.

## World and event applicability

Not seasonal; any dimension with a crucible. No creature, boss, loot or structure change.

## Rollout and open questions

Proposed until the checkpoint. Questions: (1) is the spider eye the approved carrier? (2) is a lid on the crucible the
right "covered extraction", including that a lid already set on a crucible now covers hand stirs? (3) is a fair copy of
Research Notes the use you want, with only originals copied, or should Gloam Ink serve ward sigils or links first? A
writer gains nothing by copying their own notes: a fresh original costs one blank sheet, a copy a blank sheet and an
ink, so Gloam Ink's use is for someone who holds another player's notes, such as a library or a town.
Known limits: a Peepo counts no ink as stock; a stand may heat for one pulse before its crucible finds the lid gone; both
items at the values fallback; a copy looks like its original apart from its tooltip.
