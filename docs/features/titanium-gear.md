# Titanium tools and armor

Status: implemented on the contributor's fork, pending CI and review.

Proposal issue: none. TODO item: "Add aluminum's affordable bulk electrical/lightweight structural/panel/vehicle roles; titanium advanced process/precision equipment and vehicle components; and stronger titanium tools/armor above steel with balanced statistics." This change does the last part: titanium tools and armor.

**This change's choices:** the statistics and the look, listed below for the owner to review.

Owner: jimbozoomer-byte
Target milestone and tier: after the titanium line (Kroll process), the step above steel
Primary specialty and supported player role: metals; any player who reaches titanium

## Player experience

Titanium ingots make a full set, crafted like vanilla's (three ingots and two sticks a pickaxe, eight a chestplate):

- **Tools:** a titanium sword, pickaxe, axe, shovel and hoe. They mine everything diamond does (obsidian included), last half again as long as steel's, dig at diamond's speed and hit as hard as diamond's.
- **Armor:** a titanium helmet, chestplate, leggings and boots. Worn, it is the knight armor (the steel's 3D design, [knight armor](knight-armor.md)) in a cool periwinkle titanium palette, with the steel's gold trim and leather.
- **Icons:** the material-set tool maps and the knight armor's icon maps, coloured from the titanium ingot's ramp, so they sit beside steel's and read a shade bluer and lighter.
- A titanium ingot mends any piece at an anvil.

## Statistics (for review)

| | Steel | **Titanium** | Diamond | Netherite |
| --- | --- | --- | --- | --- |
| Tool durability | 900 | **1,300** | 1,561 | 2,031 |
| Mining speed | 7.0 | **8.0** | 8.0 | 9.0 |
| Attack damage bonus | 2.5 | **3.0** | 3.0 | 4.0 |
| Tool enchantability | 12 | **15** | 10 | 15 |
| Drops | diamond | **diamond** | diamond | netherite |
| Armor durability multiplier | 25 | **30** | 33 | 37 |
| Defense (boots, leggings, chest, helmet) | 3, 6, 7, 3 | **3, 6, 8, 3** | 3, 6, 8, 3 | 3, 6, 8, 3 |
| Toughness | 1.5 | **2.0** | 2.0 | 3.0 |
| Knockback resistance | 0.05 | **0.05** | 0 | 0.1 |
| Armor enchantability | 10 | **14** | 10 | 15 |

Titanium sits above steel in every number and around diamond: a little less durable, better enchanting, and steel's knockback resistance. It is a reachable, renewable route to diamond-grade gear through the titanium chemistry, not a replacement for netherite. The axe deals 5 + 3 damage at diamond's −3.0 speed; the hoe is diamond's.

## Connections

- **Input producer:** titanium ingots from the titanium line (raw titanium → crude titanium chloride → purification → magnesium reduction → titanium sponge → arc furnace).
- **Output consumer:** the player; nothing else consumes the gear.
- **Shared systems:** `JugcraftGear`'s tier list and `tools/gear.py`'s `GEAR_TIERS` (items, recipes, tags, repair tag, language), the knight armor's 3D model (`tools/knight_armor.py`, a third `ArmorSet`), `tools/armor_icons.py` and `tools/material_icons.py`.
- **Not done:** titanium arms (the arms kinds; `"arms": False` as for thallite), a titanium paxel, and an advancement.
- **Feature switch:** `titanium`, as the titanium line itself.

## Multiplayer and persistence

- **New registrations,** stable from now on: `jugcraft:titanium_sword`, `_pickaxe`, `_axe`, `_shovel`, `_hoe`, `_helmet`, `_chestplate`, `_leggings`, `_boots`; the item tag `jugcraft:repairs_titanium_gear`; the worn-model atlas `textures/entity/equipment/3d/titanium_knight.png`.
- Plain vanilla items: the 26.3 item properties do the work. The server decides everything.

## Dependencies and assets

There are no new dependencies. All art is drawn by this repository's scripts: no texture is copied or recoloured from Mojang's files.
- Tool icons: `tools/material_icons.py` (`TOOL_METALS`), the titanium ramp `2a3144 5e73a4 95a4c5 c5cddd f3f5f7`.
- Armor icons and worn model: `tools/armor_icons.py` and `tools/knight_armor.py`, painted from `armor_paint.TITANIUM`.
- The flat worn layers, which no equipment asset draws (as the steel's), are the steel's Kaiser fallback.

## Verification

**Local checks** (no Gradle or game): `python tools/generate_material_data.py`, `python tools/check_mod_data.py`, `python tools/check_icon_maps.py` and `python scripts/check_repository.py` passed. The icons were inspected beside steel's at 8x.

**Server game tests for CI** (`TitaniumGearGameTests`):

| Test | What it checks |
| --- | --- |
| `titaniumToolsBeatSteel` | The pickaxe gets obsidian, lasts 1,300 uses, more than steel and less than netherite, and mines stone faster than steel; titanium hits harder. |
| `titaniumArmorBeatsSteel` | A set gives 20 armor, more than steel's, and more toughness; a titanium ingot mends it; every piece goes in its slot. |
| `titaniumGearIsCraftedFromIngots` | Three ingots over two sticks make the pickaxe; eight make the chestplate. |

**Client game tests:** `TitaniumGearClientGameTests` (a steel and a titanium knight on armor stands, front and three-quarter, and the nine icons in frames) and `KnightArmorClientGameTests`, which now also checks the client read the titanium pieces' worn models.

### Results

Not yet run in CI.

**Not yet done:** a survival playtest and a two-client test.

## Rollout and open questions

- The statistics await the owner's review.
- Titanium arms and a titanium paxel could follow.
