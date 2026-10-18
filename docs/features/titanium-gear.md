# Titanium tools and armor

Status: implemented on the contributor's fork, pending review.

Proposal issue: none. TODO item: "Add aluminum's affordable bulk electrical/lightweight structural/panel/vehicle roles; titanium advanced process/precision equipment and vehicle components; and **stronger titanium tools/armor above steel with balanced statistics**." This change delivers the titanium tools and armor. The aluminum and titanium equipment roles remain.

**This change's choices:** every number below and the look, listed for the owner to review.

Owner: jimbozoomer-byte
Target milestone and tier: after the Kroll process (titanium chemistry), the step above steel
Primary specialty and supported player role: metals; any player who wants better gear than steel before diamond-hunting or netherite

## Player experience

- A **titanium sword, pickaxe, axe, shovel and hoe**, and a **titanium helmet, chestplate, leggings and boots**. They are crafted from titanium ingots in vanilla's patterns, like the bronze, steel and thallite sets.
- The tools mine everything a diamond pickaxe does, including obsidian.
- The armor wears as a flat layer in titanium's periwinkle grey, with blue anodized insets at the knees and boot cuffs.
- Titanium ingots or nuggets repair the gear (`jugcraft:repairs_titanium_gear`).

## Numbers (provisional, for the owner)

| | Uses | Speed | Damage bonus | Enchantability | Drops |
| --- | --- | --- | --- | --- | --- |
| Iron (vanilla) | 250 | 6.0 | 2.0 | 14 | iron |
| Steel | 900 | 7.0 | 2.5 | 12 | diamond |
| **Titanium** | **1250** | **7.5** | **3.0** | **14** | diamond |
| Diamond (vanilla) | 1561 | 8.0 | 3.0 | 10 | diamond |

The axe is diamond's (5.0 damage, −3.0 speed) and the hoe diamond's (−3.0, 0.0).

| Armor | Durability ×  | Boots/legs/chest/helm | Enchantability | Toughness | Knockback |
| --- | --- | --- | --- | --- | --- |
| Steel | 25 | 3/6/7/3 | 10 | 1.5 | 0.05 |
| **Titanium** | **30** | **3/6/8/3** | **14** | **2.0** | **0.05** |
| Diamond (vanilla) | 33 | 3/6/8/3 | 10 | 2.0 | 0.0 |

Titanium is diamond's protection with a little less wear, and it enchants better than steel or diamond. It is weaker than netherite in every way.

## Connections

- **Input producer:** titanium ingots (rutile → Kroll process with chlorine → sponge melted in the arc furnace).
- **Output consumers:** the player (mining and combat). This change adds no machine.
- **Optional:** yes. Steel gear stays a full diamond-tier option.
- **Not included:** paxels and the arms (longswords and the like) in titanium. `"arms": False`, as for thallite.

## Multiplayer and persistence

- **New registrations,** stable from now on:
  - the items `jugcraft:titanium_sword`, `_pickaxe`, `_axe`, `_shovel`, `_hoe`, `_helmet`, `_chestplate`, `_leggings` and `_boots`;
  - their crafting recipes;
  - the equipment asset `jugcraft:titanium`;
  - the item tag `jugcraft:repairs_titanium_gear`.
- The items are added after thallite's, and the recipes load only with the `titanium` feature switch, like the ore.

## Dependencies and assets

There are no new dependencies.

- **Tools:** drawn by the shared material-set tool maps (`tools/material_icons.py`) in titanium's existing metal ramp.
- **Armor:** `tools/titanium_armor.py` paints the plain thallite armor's hand-drawn icon and worn-layer maps (`tools/thallite_armor.py`) in the titanium ramp. The emerald insets are swapped for a blue anodized tone and the visor for a near-black blue.
- Every icon is 16×16. No texture is copied from Mojang or anywhere else.

## Verification

**Local checks** (no Gradle or game): `python tools/generate_material_data.py`, `python tools/check_mod_data.py`, `python tools/check_icon_maps.py` and `python scripts/check_repository.py` passed. The icons were inspected at 8× beside steel's and thallite's.

**Server game tests for CI** (`TitaniumGearGameTests`):

| Test | What it checks |
| --- | --- |
| `titaniumIngotsCraftAnObsidianPickaxe` | Three titanium ingots and two sticks craft a titanium pickaxe, which can mine obsidian. |
| `titaniumOutlastsSteelAndNotDiamond` | Every titanium tool lasts longer than steel's and less than diamond's. Every armor piece goes in its slot and lasts longer than steel's. |

**Results:** the fork's Build workflow passed on commit 326a49b1 ([run 39104292623](https://github.com/mmme123-spec/jugcraftfork/actions/runs/39104292623)): compilation, the headless server game tests including `TitaniumGearGameTests`, and the rendered-client shards.

**Not yet run:** the worn armor has not been looked at in a running game client, and there is no multiplayer playtest.

## AI attribution

Implemented with Claude Opus 5.5 (Claude Code) for the contributor; the contributor is responsible for the output.
