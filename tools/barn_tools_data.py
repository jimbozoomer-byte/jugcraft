"""Files for Barn & Harvest Tools, decor set 3 (tools/barn_tools.py; docs/features/barn-and-harvest-tools.md).

Part 1's hand tools are flat items: the Watering Can's and the Seed Pouch's models and lang are written here, the Steel
Sickle's beside the other sickles (tools/agriculture_data.py, from SICKLES). All three icons are maps drawn by
tools/item_icons.py from tools/item_icons/<name>.txt, checked by tools/check_icon_maps.py.
"""

import barn_tools as bt
import item_icons

MOD = "jugcraft"


def rid(path):
    return f"{MOD}:{path}"


def assets(root, write, lang):
    for spec in (bt.WATERING_CAN, bt.SEED_POUCH):
        item = spec["item"]
        write(root / "models" / "item" / f"{item}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": rid(f"item/{item}")}})
        write(root / "items" / f"{item}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{item}")}})
        lang[f"item.{MOD}.{item}"] = spec["display"]
    lang[f"item.{MOD}.watering_can.water"] = "Water: %s of %s pours"
    lang[f"item.{MOD}.seed_pouch.contents"] = "%s of %s kinds of seed, %s seeds"


def textures():
    """The three part 1 icons, from their maps (the Steel Sickle's replaces nothing: the flint and bronze sickles keep
    tools/crop_textures.py's drawings)."""
    return {("item", name): item_icons.draw(name) for name in ("steel_sickle", bt.WATERING_CAN["item"], bt.SEED_POUCH["item"])}
