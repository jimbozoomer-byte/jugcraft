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
  - Steel reaches diamond-tier drops (obsidian, ancient debris) and lasts 900 uses: between iron and diamond.
- **Bronze and steel armor:** helmet, chestplate, leggings and boots.
  - Bronze matches iron's defense with a little toughness.
  - Steel sits between iron and diamond.
  - **The stylized looks:** bronze armor is steampunk (an aviator cap with teal goggles, a pressure gauge and a copper boiler on the back), and steel armor is kaiserpunk (a black Pickelhaube, a field-grey tunic over a steel cuirass, and jackboots).
    - The owner asked for these on 2 October 2026: "make sure the armor looks really cool and the copper and bronze armor is steampunk and the other one is kaiserpunk". The art is drawn pixel by pixel in `tools/armor_styles.py`.
    - On 5 October 2026 the owner kept them as sets of their own: "Wait no also I want the old armors for those 2 back those were stylized, so maybe make those be "Kaiser Armor" and "Steampunk Armor" instead and then make the new ones just be bronze and steel". They are now [Steampunk Armor and Kaiser Armor](steampunk-and-kaiser-armor.md), where the looks are described in full.
    - **Bronze and steel armor still wear these looks for now,** so each looks exactly like its Steampunk or Kaiser twin. A following PR gives bronze and steel armor their own new 3D look, a knight design the owner drew.
  - **Restyling:** at a smithing table, a Steampunk Pattern and a copper ingot turn a bronze piece into Steampunk Armor, and a Kaiser Pattern and a gold ingot turn a steel piece into Kaiser Armor. The same pattern and the metal's ingot turn it back. The protection is the same either way.
- **Paxels for every tier** (wood, stone, iron, gold, diamond, netherite, bronze, steel).
  - One tool that mines like a pickaxe, an axe and a shovel.
  - Crafted from the tier's pickaxe, axe and shovel; lasts as long as all three together.
  - The netherite paxel does not burn.
- Enchantable and repairable like vanilla gear (bronze or steel ingots repair their own gear), through the vanilla `pickaxes`, `swords`, `head_armor`… tags.
- Two advancements (Suited Up, Jack of All Trades) and two handbook pages in the Steel chapter. Suited Up also counts the Kaiser pieces, which are steel armor in another look.

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

| Armor | Durability multiplier | Boots/legs/chest/helmet | Enchantability | Toughness | Knockback resistance |
| --- | --- | --- | --- | --- | --- |
| Iron (vanilla) | 15 | 2/5/6/2 | 9 | 0 | 0 |
| **Bronze** (and Steampunk) | 15 | 2/5/6/2 | 12 | 0.5 | 0 |
| **Steel** (and Kaiser) | 25 | 3/6/7/3 | 10 | 1.5 | 0.05 |
| Diamond (vanilla) | 33 | 3/6/8/3 | 10 | 2 | 0 |

Steampunk and Kaiser Armor take every number from bronze and steel armor (`JugcraftGear.restyle`): see [their record](steampunk-and-kaiser-armor.md).

- A paxel saves inventory space, not resources: it costs the three tools and lasts exactly as long as them together.
- The metal audit counts the ingots in each piece. Nothing recycles gear back into metal, so there is no loop.

## Multiplayer and persistence
Plain items; nothing new is saved. Recipes follow the `tin` (bronze) and `machines` (steel, paxels) feature switches.

## Dependencies and assets
No new dependencies. Tool icons are drawn by `tools/gear_textures.py` from hand-made masks and the mod's own metal palettes. No vanilla or Mekanism texture is traced or recoloured.

Armor icons and worn layers are hand-drawn pixel maps in `tools/armor_styles.py`, now keyed by the looks' new names, `steampunk` and `kaiser`, with both metal palettes pinned there. For now `tools/gear_textures.py` (`METAL_ARMOR_LOOK`) draws bronze and steel armor with the same calls as their Steampunk and Kaiser twins, so the pixels match. The following PR replaces this with the new design.

## Verification
- `tools/check_mod_data.py`:
  - `JugcraftGear` lists and stats match `tools/gear.py`;
  - every item has its model and texture, and both worn layers exist;
  - recipes and tags resolve, and the metal audit counts the gear.
- Game test `paxelsAndBronzeAndSteelGear`:
  - paxels mine stone, logs and dirt fast;
  - bronze gets diamond ore but not obsidian, and steel gets obsidian;
  - paxels last three times their tier's pickaxe;
  - the netherite paxel resists fire;
  - every armor piece equips to its slot.
- Client screenshots: `jugcraft_steel_armor_worn` (front), `jugcraft_bronze_armor_worn` (front) and `jugcraft_bronze_armor_back`. They show the stylized looks, which Steampunk and Kaiser Armor now share.
  - The Steampunk and Kaiser Armor record has the `jugcraft_armor_sets_*` shots, with bronze, steel, both styled sets and vanilla copper and iron armor side by side. They are written but have not run yet.
- Since the Steampunk and Kaiser Armor split, `check_mod_data` also checks that bronze and steel armor have 64x32 worn layers and equipment assets naming their own textures. Their pixels are not pinned, because the following PR changes them.
- Not run: client play, two players.

## World and event applicability
Not applicable.

## Rollout and open questions
- Shields (Mekanism has metal shields) were left out: a shield needs its own model and banner handling.
- Lapis, refined obsidian and refined glowstone tiers from Mekanism need materials Jugcraft does not have.
