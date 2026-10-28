# Star-bred lines: star-born animals and the Astrawand (phase 1)

Status: implemented in source, not merged. The Python audits pass locally; the game compiles and its game tests run only
in CI. See Verification for what has run.
Proposal issue: none. The owner chose star-bred lines from the astronomy brainstorm and answered its design questions
on 28 October 2026. The plan is [star-bred-lines-2026-10-28.md](../plans/star-bred-lines-2026-10-28.md); this is its
phase 1.
Owner: @jimbozoomer-byte. AI-assisted implementation with Claude Code; the model is named in the commit trailers.
Target milestone and tier: Discovery. Breeding, signs and the Astrawand need nothing past vanilla materials; the
Astrawand's deeper readings follow the Concordance's Celestial Attunement (Practitioner).
Primary specialty and supported player role: husbandry: the farmer who breeds better animals, and the Starwatcher who
times births by the sky.

## Player experience

**Every farm animal carries a line.** That covers cows, mooshrooms, sheep, pigs, chickens, goats, rabbits and
Jugcraft's turkeys. A line is four numbers anyone can read, plus hidden gifts found by breeding.

**The four traits.** Each trait is two copies, one from each parent, each graded 0 to 5. The trait's number is the two
added together, 0 to 10:

- **Yield** gives more meat, more wool, and eggs that come as two.
- **Vigour** gives more health and speed, and babies that grow up sooner.
- **Fertility** gives less rest after breeding. It never gives twins.
- **Hardiness** spares the animal one harm after another.

**4 or below is exactly vanilla**, and most wild animals are 4 or below, so an ordinary farm changes not at all:

| Number | Yield | Vigour (health, speed, growing time) | Fertility (rest) | Hardiness (spares this, and each harm above) |
|---|---|---|---|---|
| 0 to 4 | vanilla | vanilla | vanilla (6000 ticks) | nothing |
| 5 | vanilla | +10%, +2.5%, 23040 ticks | 5700 ticks | cactus and berry bushes |
| 6 | +1 produce; eggs ×1.25 | +20%, +5%, 22080 ticks | 5400 ticks | freezing |
| 7 | +1 produce; eggs ×1.25 | +30%, +7.5%, 21120 ticks | 5100 ticks | falling |
| 8 | +1 produce; eggs ×1.25 | +40%, +10%, 20160 ticks | 4800 ticks | drowning |
| 9 | +2 produce; eggs ×1.50 | +50%, +12.5%, 19200 ticks | 4500 ticks | burning (not lava or fire blocks) |
| 10 | +2 produce; eggs ×1.50 | +60%, +15%, 18240 ticks | 4200 ticks | lightning's damage |

Notes on the table:

- **Produce** is the kind's meat at death, raw or cooked, and its wool when shorn.
- **Eggs ×1.25** is the average: a laid egg comes as two 1 time in 4 at 6 to 8, and 1 time in 2 at 9 and 10.
- **Lightning** still turns a pig or a mooshroom as vanilla does. It only stops hurting a Hardiness 10 animal.

**Wild animals.** An animal is given its line the first time it loads. The line is worked out from its own UUID, so
the same animal always has the same line, and there is nothing to re-roll. Each wild copy is graded:

| Grade | 0 | 1 | 2 | 3 |
|---|---|---|---|---|
| Chance | 20% | 35% | 30% | 15% |

A wild trait's number, the two added together:

| Wild number | 0 | 1 | 2 | 3 | 4 | 5 | 6 |
|---|---|---|---|---|---|---|---|
| Chance | 4.0% | 14.0% | 24.2% | 27.0% | 19.5% | 9.0% | 2.2% |

About one wild animal in nine has a trait of 5 or 6: a fine wild ewe worth catching. **Grades 4 and 5 never occur in
the wild.** They are the sky's.

**Hidden gifts.** A gift is recessive:

- one copy (a carrier) shows nothing;
- two copies show the gift;
- two carriers have one baby in four that shows it.

One wild animal in six carries one copy of a gift of its kind.

| Gift | Kinds | What it does |
|---|---|---|
| **Double Yolk** | chicken, turkey | 1 laid egg in 3 comes as two |
| **Moonfleece** | sheep | sheared at night under the open sky, one more wool |
| **Angora** | goat | can be sheared for 1 to 2 white wool, then regrows its coat over a game day |
| **Woolly Coat** | pig | can be sheared for 1 to 2 brown wool once a game day |
| **Lucky Foot** | rabbit | always drops its foot |
| **Heavy Hide** | cow, mooshroom | one more leather |

Angora and Woolly Coat are the real Angora goat and Mangalica pig. A goat or pig without the gift shows, or a baby, is
not shorn.

**Breeding.** A baby takes one copy of each trait from each parent, at random. It also takes one of each gift's two
copies from each parent: a carrier passes its gift half the time, and one that shows it always. Pick your parents and
a line breeds true: two 5+5 animals only ever have 5+5 babies.

**Signs: the sky's push.** A baby born under the open sky while a celestial pattern is up takes that pattern's **sign**.
If more than one is up, it takes the rarest: the one that pays the most resonance. Its sign raises **the weaker copy of
one trait by one grade**, to at most 5. So no baby is ever more than one grade past the best its parents had.

| Pattern | When | Raises | Also |
|---|---|---|---|
| The Full Moon | every 8 days | Yield | |
| The New Moon | every 8 days, any weather | Fertility | |
| The Lantern Star | every 3 days, 18:00 to 20:00 | Vigour | |
| The Echo Comet | every 19 days | Hardiness | wakes one copy of one of its kind's gifts |
| The Winter Crown | winter, every other night | Hardiness | |
| The Harvest Moon | the Halloween event's nights | Yield | |

Every trait has a pattern that comes all year. The Winter Crown and the Harvest Moon only add nights, so no season is
needed for anything.

**From wild 3+3 parents to a line of 10:**

1. The first rising of the pattern gives a 4+3 baby.
2. Two 4+3s bred together give a 4+4 one time in four. Born under the next rising, it is 5+4.
3. Two 5+4s, bred on any day, give a 5+5 one time in four.

That is two risings: two Full Moons for Yield, about 16 game days. Phase 2's star rite and phase 3's gene lab shorten
it.

**The Astrawand.** A slender conductor's baton in the manner of the Wind Waker from *The Legend of Zelda*:

- a pale bone shaft tapering to a little four-pointed gold star;
- a short gold grip with a raised band and a round pommel.

It is made from an amethyst shard, a bone and a gold ingot laid on the diagonal. Point it at a farm animal and use it:

- the player makes a conducting flourish;
- gold sparkles trail to the animal, two notes rise from it and a chime sounds;
- its line is read out in the chat.

It changes nothing and costs nothing. A reading:

> **Ewe**: generation 3 · Sign: The Full Moon
> Yield 9 · Vigour 6 ✦ · Fertility 1 · Hardiness 4 ✦
> Gifts: Moonfleece
> ✦ breeds true

(This is the reading of someone who understands Celestial Attunement.) How much it shows depends on the reader's Celestial Attunement:

- **Anyone:** the name and generation (or "a wild line"), the birth sign, the four numbers and the gifts it shows.
- **Understood:** ✦ marks each trait that breeds true (its two copies match).
- **Mastered:** "Carries something unseen" while it carries a gift it does not show. It never says which.

In the hand the Astrawand is a 3D model, so it reads as a baton. In the inventory it is a 16×16 icon on the wand
diagonal.

## Connections

- **Existing input producer:**
  - vanilla breeding and its foods, unchanged;
  - the Concordance's celestial patterns (`Sky.up`, `Sky.obscured`), whose signs a newborn takes;
  - Celestial Attunement, which deepens the Astrawand's reading.
- **Existing output consumer:**
  - the animals' own drops: meat, wool, leather, eggs, rabbit's feet;
  - kitchens, textiles and cooking that use them;
  - the Peepo companions' husbandry jobs, which breed, shear and slaughter through vanilla's same paths and so get the
    same drops.
- **Technology connection:** phase 3's gene lab (planned) reads and copies lines with machines.
- **Magic connection:**
  - the sky's patterns push a line;
  - the Concordance's Celestial Attunement deepens the reading;
  - phase 2's star rite (planned) raises or fixes a trait.
- **Reachable entry path:**
  - breeding needs vanilla foods;
  - signs need only a night under the open sky;
  - the Astrawand needs an amethyst shard, a bone and a gold ingot.

  No new research gates anything here: Celestial Attunement only adds to the reading.
- **Required vs optional:**
  - Nothing is required.
  - A solo farmer can breed by the sky alone.
  - Breeding stock is something to trade.
- **How the specialty stays useful:** a breeder needs no other branch. A Starwatcher's timing helps, but the sky pushes
  anyone's newborns.

## Balance and automation

- **No loops.** Breeding foods are unchanged, and a line's extra yield is meat, wool, leather and eggs, none of which is
  breeding food.
  - Eggs that hatch chickens are vanilla's own loop. Yield 10 and Double Yolk make it at most 1.83 eggs a laying
    instead of 1, at vanilla's laying time.
- **Fewer animals, not more.** Yield and the gifts make each animal give more, so a farm needs fewer of them, as the
  [agriculture plan](industrial-agriculture-plan.md) asks.
  - Fertility shortens the rest by at most 30%, and never gives twins.
  - Babies of Vigour 10 grow up 24% sooner.
- **Bounded.** Every effect is capped at 10, and 4 or below is vanilla.
- **Signs.** Each rising of a pattern lifts a line by at most one grade: a sign raises the weaker copy, never past the
  better. So the sky cannot be farmed for more than one step a rising.
- **Units.** Grades 0 to 5, a trait's number 0 to 10, ticks at 20 a second and 24000 a game day. The numbers are
  `tools/lineage.py`'s, and `python3 tools/lineage.py` prints these tables.

## Multiplayer and persistence

- **Server authority.**
  - Lines, inheritance, signs (the server's clock, weather and heightmap), drops, damage, shearing and readings are all
    the server's.
  - A reading is rate-limited to once every 10 ticks a player.
  - The client is only sent the flourish, as a gesture to play. No line is sent to clients.
- **Saved data.**
  - The line is a persistent Fabric attachment, `jugcraft:lineage`, versioned (`Saved.versioned`). It holds 8 grades,
    the gifts' copies, the sign and the generation.
  - It is copied when a mob converts: a sheared mooshroom keeps its line as a cow.
  - A shorn coat's time is `jugcraft:lineage_shorn`.
  - Both are kept in the entity's own save. A crate or capture that saves the whole entity (the Peepo's Mob Transport
    Crate does) keeps the line.
- **Disable behaviour.** The lines follow agriculture's switch. Switched off:
  - no line is made or acts;
  - lines already saved are kept untouched, and act again when it is switched back on;
  - the Astrawand stays registered.

  Signs also need the Concordance switched on (its sky).
- **Performance.** Nothing runs per tick. The work is:
  - a line is made once when an animal first loads, and Vigour's two attribute modifiers are put back on each load;
  - one look at the patterns when a baby is born;
  - one look at the line when an animal is hurt, when its loot is rolled, and when it is shorn.

## Dependencies and assets

- **Dependencies:** no new dependency. It uses:
  - Fabric's attachment, entity, damage, loot (`MODIFY_DROPS`) and use-entity events;
  - Player Animation Library (approved, [FRAMEWORKS.md](../FRAMEWORKS.md)) for the flourish.
- **One new mixin:** `LineageBreedingMixin`, at the end of `Animal.finalizeSpawnChildFromBreeding`, beside the Nouveau
  hook already there.
- **Assets.** Drawn new for Jugcraft:
  - the Astrawand's icon (`tools/item_icons/astrawand.txt`, in the icon materials stem and gold);
  - its hand model's texture and its model (`tools/lineage_art.py`, `tools/lineage.py`).
- **Fan homage.** The Astrawand is a fan homage to the Wind Waker of *The Legend of Zelda*, by the owner's request (28
  October 2026). It has its own name, and nothing from that game is copied, traced or read
  ([LICENSE_POLICY.md](../../LICENSE_POLICY.md#fan-homages)).

## Verification

- `python3 tools/check_mod_data.py` (full): see the pull request. Its new `check_lineage` holds:
  - every number in `Lineage.java` to `tools/lineage.py`;
  - the traits, gifts and signs, in order;
  - every celestial pattern raises a trait, and every trait has an all-year pattern;
  - every gift's kind carries a line;
  - the tags and the coats' tables;
  - the mixin is listed and calls `Lineage.born`, and the turkey's eggs count her line;
  - the events are registered, and nothing runs per tick;
  - the Astrawand reads on the server, rate-limited, and its client triggers the flourish clip that exists;
  - the models, clip, recipe and words are the tool's;
  - this record's tables are the tool's.
- New server game tests in `LineageGameTests` (CI):
  - `aWildLineIsTheSameEveryTime`: deterministic wild lines; no wild grade past 3; about one carrier in six, always of
    its own kind's gift.
  - `aBabyTakesACopyFromEachParent`.
  - `aSignRaisesItsTrait`:
    - the full moon seen at midnight of day 0 gives its sign, and a roof gives none;
    - the weaker copy rises, never past 5;
    - the comet raises Hardiness and wakes a gift;
    - every pattern raises a trait.
  - `breedingGivesTheBabyItsLine`: two cows bred through vanilla and the mixin; the calf's line, health and growing
    time, and the parents' rest.
  - `aLineIsKeptThroughASave`.
  - `eachTraitDoesWhatItSays`: Vigour's health and speed, idempotent; Fertility's rest; each harm spared from its
    number and not below it.
  - `yieldAndTheGiftsAddToTheDrops`:
    - the cow's own death table, rolled 40 times;
    - Lucky Foot;
    - sheep shearing through vanilla's table;
    - vanilla's chicken laying table;
    - only the tables an animal itself rolls are counted.
  - `anAngoraGoatIsShornOnceADay`.
  - `theAstrawandsReadingDeepens`.
- New client game test `LineageClientGameTests` (CI) pictures:
  - the Astrawand held in first person over a pen (`jugcraft_astrawand_held`);
  - a reading (`jugcraft_astrawand_reading`);
  - the player from the front (`jugcraft_astrawand_front`);
  - its icon in a frame (`jugcraft_astrawand_frame`).
- Not run: play by hand, two clients on a dedicated server.

## World and event applicability

Lines are on farm animals wherever they live. Signs need the Overworld's open sky, as the Concordance's patterns do.
The Winter Crown and the Harvest Moon are seasonal patterns: they only add nights to traits that have all-year patterns,
so no season gates a trait, and what was bred stays after the season ends.

## Rollout and open questions

- **Phase 1's limits:**
  - Chicks hatched from thrown eggs are wild (an egg has no father).
  - The Menagerie's animals, horses and their kin carry no line yet.
  - A shorn Angora goat or Woolly Coat pig looks the same as before (no coat model yet).
  - Dispensers do not shear the gift coats.
- **Star Husbandry.** The plan's research moves to phase 2, with the star rite that needs it. Phase 1's deeper readings
  follow Celestial Attunement, which Starwatchers already learn.
- **Next phases** ([the plan](../plans/star-bred-lines-2026-10-28.md#delivery)):
  - the star rite;
  - the gene lab;
  - more kinds;
  - star-bred crops.
- **For the owner:**
  - the trait names (Fertility replaced "temper");
  - which pattern raises which trait;
  - the first six gifts, and the Astrawand's look (its icon is a first draft, for approval as
    [ITEM_ICONS.md](../ITEM_ICONS.md) asks).
