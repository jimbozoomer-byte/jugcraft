# Thallite: the Earth school's green metal

Status: approved. The owner chose this concept on 5 October 2026 and approved its numbers on 6 October 2026: "the
Thallite numbers all look good carry on". **Not built yet;** it is built in slices, each its own PR (below).

Proposal issue: none. On 5 October 2026 the owner drew a complete set for a new ore in one chartreuse palette and asked
what it should be: "maybe something in the magic tier". The set covers:
- ore in stone and deepslate, raw chunk and block, nugget, ingot and storage block;
- sword, pickaxe, axe, shovel and hoe;
- helmet, chestplate, leggings and boots, each plain and gold-trimmed;
- daggers, a sabre, longswords, a spear, a hammer, a spiked mace, a bow, a crossbow, an arrow and horse armor.

A design panel worked up three magic-tier concepts (thallite, chrysolite and aurivert), and critics checked them against
the project's rules and for fun. The owner picked thallite. How the set is drawn:
[MATERIAL_SETS.md](../MATERIAL_SETS.md).

Owner: jimbozoomer-byte
Target milestone and tier: the Discovery stage, at iron level beside bronze. It is the material of the Earth school
([CONTENT_BRANCHES.md](../CONTENT_BRANCHES.md)) once magic exists.
Primary specialty and supported player role: magic (Earth) and early gear. An engineer can use it too, with no magic.

## Player experience
> Thallite is a green metal that grows in old stone wherever roots and water reach, and gear forged from it slowly
> regrows on living soil. Bind a piece with gold at a smithing table and it becomes Earthbound: it holds you to the
> ground, regrows even on bare stone, and becomes the Earth school's armor once spells arrive.

### What it is
- **The mineral:** "thallite" is an old name for green, iron-rich epidote, from the Greek *thallos*, "a young green
  shoot".
- **The legend:** medieval alchemists believed metals ripened in the earth, so a green metal was "unripe gold".
- **The owner's art already tells the story:**
  - the leaf-shaped nugget is the shoot;
  - the sage raw chunk is the mineral as dug;
  - the chartreuse ingot is the worked metal;
  - the gold trim is the gold that ripens it.
- **The palette** stays sage and chartreuse with gold, apart from uranium's yellow-green and emerald's blue-green.
- **The name** is near the poison thallium, so a lore line sets the tone: "green as a new shoot".

### Where it is found
Overworld only, in stone and deepslate.
- **Everywhere:** vein size 7, 4 per chunk, Y −32 to 48 (between lead and silver).
- **Rich pockets:** 6 more veins of size 9 per chunk at the same heights, only in vanilla Lush Caves and Jugcraft's
  Glowcap Grotto. That roughly triples the ore there.
  - The in-world reason: it forms where roots and water work through old rock.
  - These veins are never thrown away for touching air, so green streaks show in the cave walls.
  - Azalea trees and hanging roots mark the caves from above.
- **Mining:** an ore drops one raw thallite, and Fortune works as on vanilla ores.

### Getting it (no magic or machine needed)
1. Mine it with a stone pickaxe, like tin, zinc and lead.
2. Smelt it in a furnace or blast furnace (0.7 xp).
3. Nuggets, ingots and blocks, and raw ore and raw blocks, convert 9 to 1 both ways.
4. Craft plain tools and armor at a crafting table in the usual shapes; ingots repair them.
5. Bind armor with gold at a smithing table to make it Earthbound (below).

### Where its gear sits

| Tools | Uses | Speed | Damage bonus | Enchantability | Drops |
|---|---|---|---|---|---|
| **Thallite** | 200 | 6.0 | 2.0 | **18** | iron |
| Iron | 250 | 6.0 | 2.0 | 14 | iron |
| Bronze | 320 | 6.5 | 2.0 | 14 | iron |
| Steel | 900 | 7.0 | 2.5 | 12 | diamond |

| Armor | Durability multiplier | Defense | Toughness | Enchantability |
|---|---|---|---|---|
| **Thallite** | 13 | 2/5/6/2 (iron's) | 0 | **18** |
| Iron | 15 | 2/5/6/2 | 0 | 9 |
| Bronze | 15 | 2/5/6/2 | 0.5 | 12 |

Thallite enchants better and keeps itself going, but it wears out sooner and digs slower than bronze. Steel, diamond and
netherite stay stronger.

### What makes it different
Two bounded traits; neither adds damage, defense or speed.

- **Regrowth (all thallite gear):**
  - Every 5 seconds, each thallite item worn or held gets back one use while its holder stands on living soil
    (`#jugcraft:living_ground`: grass, dirt, podzol, mud, moss, rooted dirt, farmland).
  - It stops at 75% of full and never brings back a broken item.
  - A tool goes from empty to 75% in about 12 minutes outdoors.
  - It shows as a faint leaf particle and as the trait "Regrowth" in the tooltip, described while Shift is held
    ([trait-details.md](https://github.com/jimbozoomer-byte/jugcraft/blob/claude/trait-details/docs/features/trait-details.md)).
- **Rooted (Earthbound armor only):**
  - Each Earthbound piece gives +0.075 knockback resistance while the wearer stands on natural ground
    (`#jugcraft:earthen_ground`: living soil plus stone, deepslate, sand, gravel).
  - A full set gives 0.3, under netherite's 0.4; in the air or water, nothing.
  - With two or more Earthbound pieces worn, Regrowth works on any natural ground.

### The two looks: plain and Earthbound
- **Plain (Unbound):** thallite as forged, with Regrowth.
- **Gold-trimmed (Earthbound):** the same stats, plus Rooted, and Regrowth on stone.
- **Making it Earthbound:** at a smithing table, an **Earthbinding Template**, the plain piece and a gold ingot give
  `earthbound_thallite_<piece>`.
  - It is a separate item with its own worn look.
  - It keeps its enchantments, wear and name.
  - It is one-way.
- **The template:** rooted dirt in the centre, four thallite nuggets at the sides and four gold nuggets at the corners.
  A full set costs about 5.8 gold ingots.
- **The green gems** on the leggings and boots are the anchors of its roots.
- **Only the armor has two looks,** as drawn. Earthbound ships with the plain armor and does not wait for magic.

### Weapons and the rest of the set
- **Arms:** only the kinds drawn: dagger, sabre, longsword, greatsword, spear, war hammer, flanged mace, longbow and
  arbalest. The owner named the sheet's two long blades on 6 October 2026: "The big sword should be a great sword and the
  other one should be a longsword". They
  use bronze's numbers and thallite's durability, and are drawn from the 16×16 arm maps in thallite's colours.
- **Thallite Arrow:** base damage 2.0, as vanilla's. Its "Rooting" stops the target jumping for 2 seconds and gives
  Slowness I for 2 seconds, through a temporary attribute modifier, so no mixin. Bosses in `#jugcraft:unrootable` are
  immune.
- **Thallite horse armor:** 5 protection (iron's). The horse is Rooted (+0.2 knockback resistance) on natural ground.
  - The sheet draws it twice, nearly alike; the owner left the choice to us ("whichever horse armor you think fits best").
  - We follow the second drawing, at the bottom of the sheet. Its head and neck rise higher and clear of the blanket, so it
    reads as a horse at 16×16, and as the later of the two it looks like the revision. Its red and gold band stays.

## Connections
- **Existing input producer:** the world itself (ore). Ore processing gives more:
  - Crusher and Pulverizer: 2 a block (the Pulverizer with a 10% iron dust byproduct);
  - Ore Washer: 3;
  - acid leaching: 4.
- **Existing output consumer:** players' tools, armor and arms. Machines take thallite plates and dust through shared
  `c:` tags.
- **Technology connection:**
  - a METALS entry with its `c:` tags, so the Ore Drill and Prospector find it;
  - ore processing as above;
  - a plate (Metal Press) with a dearer hand-made route.
  - **No alloy, on purpose:** thallite's technology role is narrow, as [DESIGN.md](../DESIGN.md) allows when the reason
    is stated.
- **Magic connection:** the Earth school's metal. These come in the first magic-system PR, not here:
  - **The Thallite Focus:** Earth's starting instrument; 1 raw thallite, 2 ingots, 1 stick.
  - **Two first spells** sharing one temporary block, both with tight limits:
    - **Earthen Steps:** a short stair or bridge;
    - **Earthen Bulwark:** a 3×2 wall.
  - **Earthbound armor** lengthens earthwork.
  - **Later ideas:** a Block of Thallite as the Stonebloom herb's Earth "influence", thallite dust as one of several
    Earth catalysts, and an Earth Staff. Silver, salt and lead stay with other schools.
- **Reachable entry path:** stone pickaxe, then furnace, ingot, gear, template and Earthbound. No step needs its own
  output, and no magic or machine is required.
- **Required vs optional:** all optional. It can be crafted solo or traded: engineers can wear plain thallite with no
  magic, and mages trade raw ore to engineers for the washer's triple yield.
- **How the specialty stays useful:** cheap, easy to enchant, self-mending gear for farmers and early players, and the
  Earth school's armor later. Bronze lasts longer and steel goes further.

## Balance and automation
- **Numbers above are starting points,** not played.
- **Regrowth adds uses, never metal.** It stops at 75%, so Mending and anvils still matter. Gear never recycles into
  metal, so there is no loop.
- **Rooted is capped** at 0.3 for a full set. If it proves too strong in PvP, cut it to 0.05 a piece.
- **Check that thallite does not crowd out bronze** for early players. Its fewer uses and slower digging are meant to
  prevent that.
- **Server cost:** Regrowth is one check per player every 5 seconds over at most 6 slots. Rooted is a temporary
  attribute modifier refreshed twice a second, only for players wearing Earthbound pieces. No per-block or per-entity
  scans.

## Multiplayer and persistence
- **Server authority:** everything is worked on the server. Clients only draw the tooltip and the leaf particle.
- **Saved state:** none beyond ordinary items and blocks.
- **Stable IDs:**
  - `jugcraft:thallite_*` and `jugcraft:raw_thallite*`;
  - `jugcraft:earthbound_thallite_<piece>`;
  - `jugcraft:earthbinding_template`, `jugcraft:thallite_arrow` and `jugcraft:thallite_horse_armor`.
- **Disabling `thallite`** stops its worldgen and recipes but keeps every registration.
- **No retrogeneration:** existing chunks get no thallite.

## Dependencies and assets
- **No new dependencies.**
- **Art:** everything is drawn by code from 16×16 maps, after the owner's sheet, following
  [MATERIAL_SETS.md](../MATERIAL_SETS.md). The sheet is a style reference and is not committed.
  - The worn armor layers (plain and Earthbound) and the arms' 3D in-hand textures are not on the sheet, so they are
    drawn to the same rules.

## Building it, in slices
Each slice builds on the open PRs whose systems it uses, so it waits for them or is stacked on them:
1. **Ore and materials:** the ore, material forms, worldgen (with a small optional "biomes" key on a metal's worldgen
   entry, which only rocks have today) and processing. Its art is drawn by the material-set maps and ore overlays of the
   material-sets PR, so it is stacked on that branch.
2. **Gear:** tools, both armor looks, the Earthbinding Template, Regrowth and Rooted.
   - Earthbound is an entry in the one armor-style system of the Steampunk and Kaiser PR
     ([#215](https://github.com/jimbozoomer-byte/jugcraft/pull/215)), one-way and with a perk.
   - The traits use the Shift details of [#216](https://github.com/jimbozoomer-byte/jugcraft/pull/216).
3. **Arms:** the drawn kinds (dagger, sabre, longsword, greatsword, spear, war hammer, flanged mace, longbow and
   arbalest), first adding a list of arm kinds per metal to `tools/arms.py`; today every gear tier makes all of its kinds.
   Their icons come from the 16×16 arm maps of [#201](https://github.com/jimbozoomer-byte/jugcraft/pull/201).
4. **Arrow and horse armor:** the mod's first of each, built as shared code later ores can reuse.
5. **The Focus and Earth's first spells:** in the first magic-system PR, coordinated before work starts.

## Verification
Not built; nothing has run. Each slice will add game tests, among them:
- veins in the stated biomes and heights;
- Regrowth's rate and its 75% cap;
- Rooted only on natural ground;
- the smithing template keeping enchantments.

Each slice will also add a client test that shows the set in game.

## World and event applicability
- **Overworld worldgen only:**
  - the rich pockets lie in vanilla Lush Caves and Jugcraft's Glowcap Grotto;
  - with the biomes feature off, Lush Caves still carry them.
- **No bosses, dimensions or seasonal content.**

## Rollout and open questions
- **Approved by the owner on 6 October 2026:** the numbers above; the two long blades (the big one a greatsword, the other a
  longsword); and the horse armor left to us (the second drawing).
- **Tooltips:** Regrowth, Rooted and Rooting (the arrow) are named in the tooltip and described while Shift is held, as
  the owner asked: "make it so if the player holds shift while hovering their mouse over gear that has more complex traits
  like Thallite it expands the hovering UI and has a brief description of the unique traits".
- **First magic PR:** spell costs come from the shared magic resource. Until it exists, the Focus spends its own
  durability and does not regrow, so it is never a hidden mana bar.
