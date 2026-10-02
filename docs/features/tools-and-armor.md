# Bronze and steel tools and armor, and paxels

Status: implemented on `feature/tools-25` (batch 25), stacked on `feature/chemistry-24`, awaiting review. Compiles and tests in CI only; **not yet played**.
Proposal issue: the owner, 2 October 2026, shared Mekanism jars ("lets integrate a bunch of this") and picked "Tools & armor" from the list Claude offered.
Owner: jimbozoomer-byte
Target milestone and tier: bronze (workshop) and steel tiers
Primary specialty and supported player role: everyone: mining, building and fighting gear.
Design inspiration: Mekanism: Tools by aidancbrady and team (MIT). Only the ideas are taken: metal tool sets and the paxel. No code, data or art was copied. Their jars are NeoForge builds for an older Minecraft, and Jugcraft only ships original art.

## Player experience
- **Bronze and steel tool sets:** sword, pickaxe, axe, shovel and hoe, crafted like iron tools from bronze or steel ingots.
  - Bronze is iron-tier: same drops, a little more durable (320 uses) and quicker.
  - Steel reaches diamond-tier drops (obsidian, diamonds) and lasts 900 uses: between iron and diamond.
- **Bronze and steel armor:** helmet, chestplate, leggings and boots.
  - Bronze matches iron's defense with a little toughness.
  - Steel sits between iron and diamond.
  - Both have their own worn look: plated bands with rivets and a visor slit.
- **Paxels for every tier** (wood, stone, iron, gold, diamond, netherite, bronze, steel).
  - One tool that mines like a pickaxe, an axe and a shovel.
  - Crafted from the tier's pickaxe, axe and shovel; lasts as long as all three together.
  - The netherite paxel does not burn.
- Enchantable and repairable like vanilla gear (bronze or steel ingots repair their own gear), through the vanilla `pickaxes`, `swords`, `head_armor`… tags.
- Two advancements (Suited Up, Jack of All Trades) and two handbook pages in the Steel chapter.

## Connections
- Existing input producer: bronze (alloy smelter or crafting) and steel (steel foundry).
- Output consumer: the player. Paxels are made from vanilla tools too.
- Technology connection: none beyond the metals. Magic connection: none.
- Required vs optional: optional. The powered drill and chainsaw stay the top-end tools.

## Balance and automation
| Tier | Uses | Speed | Damage bonus | Enchantability | Drops like |
| --- | --- | --- | --- | --- | --- |
| Iron (vanilla) | 250 | 6.0 | 2.0 | 14 | iron |
| **Bronze** | 320 | 6.5 | 2.0 | 14 | iron |
| **Steel** | 900 | 7.0 | 2.5 | 12 | diamond |
| Diamond (vanilla) | 1,561 | 8.0 | 3.0 | 10 | diamond |

| Armor | Durability multiplier | Boots/legs/chest/helmet | Toughness | Knockback resistance |
| --- | --- | --- | --- | --- |
| Iron (vanilla) | 15 | 2/5/6/2 | 0 | 0 |
| **Bronze** | 15 | 2/5/6/2 | 0.5 | 0 |
| **Steel** | 25 | 3/6/7/3 | 1.5 | 0.05 |
| Diamond (vanilla) | 33 | 3/6/8/3 | 2 | 0 |

- A paxel saves inventory space, not resources: it costs the three tools and lasts exactly as long as them together.
- The metal audit counts the ingots in each piece. Nothing recycles gear back into metal, so there is no loop.

## Multiplayer and persistence
Plain items; nothing new is saved. Recipes follow the `tin` (bronze) and `machines` (steel, paxels) feature switches.

## Dependencies and assets
No new dependencies. Icons and worn layers are drawn by `tools/gear_textures.py` from hand-made masks and the mod's own metal palettes; no vanilla or Mekanism texture is traced or recoloured.

## Verification
- `tools/check_mod_data.py`:
  - `JugcraftGear` lists and stats match `tools/gear.py`;
  - every item has its model and texture, and both worn layers exist;
  - recipes and tags resolve, and the metal audit counts the gear.
- Game test `paxelsAndBronzeAndSteelGear`:
  - paxels mine stone, logs and dirt fast;
  - bronze gets iron ore but not diamond ore, and steel gets diamond ore and obsidian;
  - paxels last three times their tier's pickaxe;
  - the netherite paxel resists fire;
  - every armor piece equips to its slot.
- Not run: client play (how the icons and worn armor look in game), two players.

## World and event applicability
Not applicable.

## Rollout and open questions
- Shields (Mekanism has metal shields) were left out: a shield needs its own model and banner handling.
- Lapis, refined obsidian and refined glowstone tiers from Mekanism need materials Jugcraft does not have.
