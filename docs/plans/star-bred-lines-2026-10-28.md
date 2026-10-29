# Star-bred lines: a design plan (animals first)

Status: a plan, not built. Written 28 October 2026 from the owner's answers. The owner chose this idea from the
astronomy brainstorm ([TODO](../TODO.md#astronomy-and-astrology-making-the-sky-useful--october-28)): "purposely breeding
certain traits into crops, seeds and animals, a magical way to imbue genetic modification".

## The owner's decisions (28 October 2026)

| Question | Answer |
|---|---|
| How deep the genetics go | **A mix**: a few numbers anyone can read, plus hidden genes found by breeding |
| Crops or animals first | **Animals** |
| Vanilla kinds too | **Yes**, vanilla animals as well as Jugcraft's |
| A drawback for forcing traits | **Cost only**: nothing goes unstable or sickly |
| A non-magic route | **Yes, a CRISPR-style gene lab**, and using both routes is better than either alone |
| The Astrawand | **Reads only.** It looks like a conductor's baton in the manner of the Wind Waker from *The Legend of Zelda* |

## What exists to build on

This plan was checked against main (`b6cf2d29c`). The checks found:

- **The sky.** The Concordance's six patterns are `data/jugcraft/concordance/pattern/*.json`.
  - `Sky.up(time)` gives the patterns up now.
  - `Sky.visible(level, pos, pattern)` says whether one can be seen from a block: in the Overworld, under the open sky,
    and clear for a pattern that needs it.
  - Each pattern has a Principle and pays a resonance.
  - The Astrolabe's recall and the claims ledger (`AstralLedger`) already stop the same rising paying twice.
- **Breeding.** One mixin already hooks a birth: `NouveauBreedingMixin` injects at the end of
  `Animal.finalizeSpawnChildFromBreeding`.
  - At that point the parents' rest (vanilla's 6000 ticks) has been set.
  - The baby has not yet been added to the world.
  - There is no Fabric breeding event.
- **Saved data on vanilla animals.** Fabric attachments already do this. `CompanionLivestock.MILK_READY` is kept on
  vanilla cows. `Saved.versioned` wraps a codec with a schema. `copyOnDeath()` attachments also follow a mob that
  converts (a sheared mooshroom becoming a cow).
- **Jugcraft's breedable animals.**
  - The **Turkey** (`agriculture/Turkey`) breeds and lays eggs in Jugcraft's own code.
  - The **Squirrel** breeds but gives nothing.
  - The Menagerie's ~265 species (`MenagerieAnimal`) breed and pass one variant on. Their capture items save a fixed
    list of fields (`MenagerieItems`), and egg-laying species hatch from egg items.
  - There is no Jugcraft cow, sheep or goat. The Butter Churn is in an open PR, not on main.
- **Genetics already in the game, for crops only.** Jimtafacts' industrial crops (`jimtafacts/industry/IndustrialCrops`)
  keep Growth, Gain and Resistance (0 to 31) on crop sticks.
  - Crossing averages the parents and moves one up or down.
  - The Crop Analyzer reads them for 20 JE.
  - This plan does not touch them. The crops phase must reconcile with them, not duplicate them.
- **Mutation already in the game.** The Menagerie's Chemical X turns vanilla mobs into mutant species. The lab here
  edits an animal and never changes its species.
- **Machines and power.** `MachineBlockEntity`, `MachineKind`, data-driven `MachineRecipe` and `EnergyStorage` (JE).
- **Reading tools.** Nothing reads an animal yet. Jade is the only inspection overlay (`compat/jade`) and has no animal
  provider. Feedback in the house style is `sendOverlayMessage` or chat lines.
- **Gestures.** Player Animation Library already plays the Concordance's channelling (`CircleClient`) and offering
  (`VigilClient`) gestures.
- **Icons.** Icons are 16×16 maps in `tools/item_icons/`, drawn by `tools/item_icons.py` and checked by
  `tools/check_icon_maps.py`. Wands lie on the diagonal ([ITEM_ICONS.md](../ITEM_ICONS.md)). `runebound_staff` is flat
  in the GUI and 3D in the hand.

## The idea in one paragraph

Every farm animal carries a **line**: four numbers anyone can read and a few **hidden gifts** found by breeding. Breed
two good animals and the baby takes one copy of each number from each parent, so a farmer can pick parents and fix a
line that breeds true. **The sky pushes:** a baby born under the open sky while a pattern is up takes that pattern's
**sign**, and its sign raises one number past anything its parents had. The magical **star rite** works a number into
an animal on its night. The technological **gene lab** samples animals, reads their genes exactly and copies a
chosen gene into others with serums. Either route reaches the top on its own. Together the sky makes a new best gene
and the lab spreads it through a herd in one step, which is the reason to use both.

## The genetics

### Four traits (the numbers)

Each trait is two copies, one from each parent. Each copy has a **grade** from 0 to 5, and the trait's number is the
two added together, 0 to 10. A trait whose two copies match **breeds true**.

| Trait | What it does (by number) |
|---|---|
| **Yield** | 6+: one more of the kind's meat or wool each time it gives them; 9+: two more. For egg layers, 6+ makes a laid egg come as two 1 time in 4, and 9+ 1 time in 2 |
| **Vigour** | for each point over 4: +10% health and +2.5% speed (+60% and +15% at 10); its babies grow up 4% sooner for each point over 4 (24% at 10) |
| **Fertility** | for each point over 4, 5% less rest after breeding: 6000 ticks at 4 down to 4200 at 10. Never twins |
| **Hardiness** | immunities, each kept above: 5 cactus and berry bushes; 6 freezing; 7 falling; 8 drowning; 9 burning (not lava or fire blocks); 10 lightning's damage (lightning still turns a pig or mooshroom as vanilla does) |

**4 and below is exactly vanilla.** Wild animals are mostly 0 to 4, so nothing about an ordinary farm changes. A
better number only ever adds.

### Wild animals

An animal with no line yet is given one the first time it loads. The line is worked out from its own UUID, so loading
it again gives the same result, and there is nothing to re-roll.

- Each copy is graded 0 (20%), 1 (35%), 2 (30%) or 3 (15%). A wild trait is about 3 on average, and about 1 wild animal
  in 9 shows 5 or more in a given trait.
- **Grades 4 and 5 never occur in the wild.** They are the sky's grades, or the lab's at a price.
- 1 wild animal in 6 carries one copy of one of its kind's gifts.

### Hidden gifts (the genes found by breeding)

A gift is recessive. One copy (a **carrier**) shows nothing; two copies show it. Two carriers have one baby in four that
shows the gift. The ways to find one are:

- breeding: a gift appearing in a baby;
- the Astrawand, once Star Husbandry is mastered, which says an animal carries something unseen;
- the gene lab's sequencer, which names every copy.

The first gifts:

| Gift | Kinds | What it does |
|---|---|---|
| **Double Yolk** | chicken, turkey | 1 laid egg in 3 comes as two |
| **Moonfleece** | sheep | sheared at night under the open sky, one more wool |
| **Angora** | goat | can be sheared for 1 to 2 white wool, then regrows its coat over a game day (the real Angora goat) |
| **Woolly Coat** | pig | can be sheared for 1 to 2 brown wool once a game day (the real Mangalica pig) |
| **Lucky Foot** | rabbit | always drops its foot |
| **Heavy Hide** | cow, mooshroom | one more leather |

### Inheritance

At birth the baby takes, for each trait, one copy from each parent at random. For each gift, each parent passes one of
its two copies at random (a carrier passes it half the time). The baby's **generation** is one more than its older
parent's. The parents' rest is then shortened by their own Fertility.

### Signs: the sky's push

A baby born where a pattern is up and can be seen from its block takes that pattern's **sign**. If more than one is up,
it takes the rarest: the one that pays the most resonance. Its sign raises **the weaker copy of one trait by one grade**,
to at most 5. A new grade is never more than one above the best the parents had, so each rising of a pattern can lift a
line by at most one grade.

| Pattern | How often | Trait it raises | Also |
|---|---|---|---|
| The Full Moon | every 8 days | Yield | |
| The New Moon | every 8 days, any weather | Fertility | |
| The Lantern Star | every 3 days, 18:00 to 20:00 | Vigour | |
| The Echo Comet | every 19 days | Hardiness | wakes one copy of one of the kind's gifts |
| The Winter Crown | winter, every other night | Hardiness | |
| The Harvest Moon | the Halloween event's nights | Yield | |

Every trait has a pattern that comes all year. The Winter Crown and the Harvest Moon only add nights, so no season is
needed for anything. The sign stays on the animal as its birth sign. It is the hook for the horoscopes idea later (a
sign that counts on omen nights).

**The arithmetic.** Two wild 3+3 parents give a 3+3 baby. Born under the Full Moon, it is 4+3, Yield 7. Two such
animals bred under the next Full Moon can give a 5+4, Yield 9. Fixing 5+5 needs a third night, or a lucky pairing. By
breeding alone, a Yield 10 line that breeds true takes about three Full Moons, close to a month of game days. The rite
and the lab shorten that.

## The Astrawand (reading only)

**The look.** A slender conductor's baton in the manner of the Wind Waker:

- a pale bone-white shaft tapering to a point;
- a small four-pointed gold star at the tip;
- a gold grip with two raised bands and a round gold pommel.

It is a fan homage with its own name. Its icon and model are drawn new for Jugcraft, never traced or ripped
([LICENSE_POLICY.md](../../LICENSE_POLICY.md#fan-homages)). The owner library has no baton to start from. In the
hand it is a 3D model, so it reads as a baton and not a flat sprite. In the GUI it is a 16×16 icon on the wand
diagonal.

**Use.** Point it at an animal and use it. The player makes a short conducting flourish (Player Animation Library),
gold sparkles trail from the tip to the animal, a chime sounds and a couple of note particles rise from the animal. It
changes nothing and costs nothing. The reading goes to chat:

> **Ewe** — born under the Full Moon, generation 3
> Yield 7 · Vigour 4 · Fertility 5 · Hardiness 3
> Gifts: Moonfleece

How much it shows depends on the reader's Star Husbandry:

- **Anyone:** the sign, the generation, the four numbers and the gifts it shows.
- **Understood:** each trait that breeds true is marked.
- **Mastered:** "carries something unseen", while it carries a gift it does not show. The gift is not named.

**Recipe.** An amethyst shard (the star), a bone (the shaft) and a gold ingot (the grip), laid on the diagonal.
Discovery tier, with nothing gated.

## Star Husbandry (the research)

A new Concordance research in the Starwatchers' tradition, at the Practitioner stage. It needs Celestial Attunement
understood. Its states follow Verdant Husbandry's:

- **Encountered:** examine a farm product (wool, an egg, leather, a feather or rabbit hide).
- **Observed:** examine three different ones.
- **Understood:** study one at a Lampwright's Bench, or read someone's notes.
- **Mastered:** be within 16 blocks of a birth under three different signs.

It deepens the Astrawand's reading and opens the star rite. Signs themselves need no research: the sky pushes any
farmer's newborns.

## The star rite (magic route; phase 2)

A **Star Manger**, a pale-wood trough banded in gold, stands under the open sky. On a pattern's night:

1. Lead an animal to it.
2. Fill the manger with the pattern's offering (for example golden carrots for the Full Moon).
3. Draw Astral Resonance from an Astrolabe.

The rite raises that animal's weaker copy of the pattern's trait by one grade, or, sneaking, **fixes** the trait:
both copies become the better one, so it breeds true. Each animal can be worked once each time the pattern comes round.
The cost is the pattern's attunement cost in resonance plus the offering. Under the Echo Comet, the rite **wakes** a
gift the animal carries (a second copy, so it shows).

**Recall.** A master may work the rite off-night through `AstrolabeItem.recall`, at the dearer recall cost. Recall
spends the same `recall:<pattern>` claim, so one rising still pays one recall, whichever route asks first.

## The gene lab (technological route; phase 3)

Tier: Industry (oil and chemistry, then Electronics). It uses the shared machine framework and JE.

| Piece | What it does |
|---|---|
| **Sampling Syringe** (steel, glass) | used on an animal: a **Tissue Sample** (a glass vial holding a copy of its line). One sample from each animal per game day |
| **Gene Sequencer** (machine) | sample in, **Gene Readout** out: every copy's grade, every gift's copies and the sign, exactly. The sample comes back |
| **Gene Splicer** (machine) | a target sample, a template sample of the same kind and a guide reagent (chemistry) make an **Edit Serum** that copies one chosen copy (a trait's grade or a gift) from the template. The template is not used up |
| **Directed mutation** (the Splicer with a mutagen) | raises one copy by one grade, to at most 5, 1 time in 3; the reagents are spent either way |
| **Edit Serum** | used on an animal of its kind: its line is rewritten at once, and it passes the edit on |

**Why both is better:**

- **The sky makes, the lab spreads.** The sky makes a new grade surely on its night, and cheaply. The lab's mutation is
  a 1-in-3 gamble at a price. The lab copies a gene into a whole herd at once, while breeding spreads it over
  generations. One Full Moon baby can template serums for every animal in the barn.
- **The readout and the comet.** The sequencer names the gifts the comet can wake, so a player can plan a gift line
  instead of breeding blind.
- **Star-drawn samples.** A sample drawn while a pattern is up, under the open sky, makes a mutation of that pattern's
  trait certain.

**Each route alone:**

- Tech alone samples the wild for its best genes, splices them in, then mutates (expensively) to the top.
- Magic alone breeds by the sky and works the rite.

Either reaches 10. Everything is cost: no failure harms an animal, and nothing goes unstable.

**Trade.** Breeding stock, star-born animals, samples, readouts and serums are all things a player can sell to another.
Nobody needs both routes.

## Balance

- **No loops.** Breeding food is unchanged. A line's extra yield is meat, wool, leather and eggs, and none of these is
  breeding food. Eggs that hatch chickens are vanilla's loop already; Yield and Double Yolk make it at most 1.83 eggs a
  lay instead of 1, with vanilla's lay time. The rite costs resonance (finite each rising), and the lab costs reagents
  and power.
- **Fewer animals, not more.** Yield and gifts make each animal give more, so a farm needs fewer of them, as the
  agriculture plan asks ([industrial-agriculture-plan.md](../features/industrial-agriculture-plan.md)). Fertility
  shortens rest by at most 30% and never gives twins. Breeding stays reachable as vanilla's.
- **Bounded.** Every effect is capped at 10, and 4 or below is vanilla.
- **Seasons.** No season is needed for any trait (see Signs). Event nights only add chances.

## How it is built (phase 1)

- **The line**:
  - A persistent, `copyOnDeath` Fabric attachment `jugcraft:lineage` on animals in the entity tag
    `#jugcraft:star_bred`, under `Saved.versioned`. It holds:
    - eight grades;
    - each gift's copies;
    - the sign;
    - the generation.
  - About 40 bytes an animal. It is not sent to clients. Phase 1's gifts change only what drops, so no client needs it.
- **Kinds in phase 1**: cow, mooshroom, sheep, pig, chicken, goat, rabbit and the Turkey.
  - Horses, donkeys, llamas and camels come later, because vanilla already breeds their speed, jump and health, and
    Vigour has to fit with that.
  - The Menagerie's livestock comes later too: bison, duck, boar, emu and the rest. Its capture items and egg hatching
    must carry the line first.
- **Hooks.**
  - **Birth:** one new injection beside the existing one at the end of `finalizeSpawnChildFromBreeding`. It does
    inheritance, the sign, the baby's growing time and the parents' rest.
  - **Wild lines:** `ServerEntityEvents.ENTITY_LOAD` gives a line to a loaded animal that has none. It also puts back
    Vigour's attribute modifiers, under one fixed id each, so this is idempotent.
  - **Hardiness:** `ServerLivingEntityEvents.ALLOW_DAMAGE`, by damage type.
  - **Yield and the gifts:** a custom loot condition `jugcraft:lineage` (a trait at least n, or a gift shown) on pools
    that `LootTableEvents.MODIFY` adds to vanilla's entity, shearing and chicken-laying tables, and on the Turkey's own.
  - **Angora and Woolly Coat:** shearing through `UseEntityCallback`, with a regrowth time kept as an attachment, as
    `MILK_READY` does.
  - No per-tick work.
- **The Astrawand**:
  - `interactLivingEntity`, on the server, rate-limited;
  - the reading as chat lines;
  - sparkles and notes as server particles;
  - the gesture played on the client;
  - a model that is 3D in the hand and flat in the GUI, as `runebound_staff` is;
  - an icon map in `tools/item_icons/`.
- **Audits and tests**:
  - A Python model, `tools/lineage.py`, with `check_lineage` in `tools/check_mod_data.py`. It holds:
    - the wild grade weights;
    - the effect tables;
    - the sign table against the pattern data;
    - Java constants against Python.
  - Server game tests:
    - a baby born under a visible Full Moon gains Yield;
    - a baby born under a roof gains none;
    - inheritance takes only the parents' copies;
    - a wild line is the same after a reload;
    - each trait's effect (drops, rest, health, immunities);
    - each gift;
    - the Astrawand's three depths of reading.
  - A client game test pictures the Astrawand in hand.

## Delivery

Each phase ends with a checkpoint to the owner before the next.

1. **Star-born animals** (phase 1):
   - the line on eight kinds, inheritance and signs;
   - the four traits' effects and the six gifts;
   - the Astrawand: icon, hand model, flourish and reading;
   - Star Husbandry;
   - a feature record and tests.

   Player outcome: breed animals under the night sky, read them with the Astrawand, and get more from a better line.
2. **The star rite**: the Star Manger, raising and fixing a trait, waking a gift under the comet, and recall.
3. **The gene lab**: the syringe, samples, the sequencer, the splicer, serums, directed mutation and star-drawn samples.
4. **More kinds**: horses and their kin, reconciled with vanilla's inheritance. Then the Menagerie's livestock, with
   capture and eggs carrying the line.
5. **Star-bred crops**: seeds carry a line. This is reconciled with the industrial crops' Growth, Gain and Resistance
   and with the Concordance garden's grafts.
6. **Later hooks**: horoscopes reading birth signs; a town stockbreeder; a county fair with ribbons. None of these
   gates progression.

## Open points for the owner (not blocking phase 1)

- The trait names: Yield, Vigour, Fertility, Hardiness. Fertility replaces "temper" from the brainstorm. A calm-temper
  trait would need mixins on each animal's AI.
- Which pattern raises which trait.
- The six first gifts, and more you would like (each is a small effect).
- Whether chicks hatched from thrown eggs should inherit their mother's line. Phase 1 treats them as wild, because a
  thrown egg has no father.
