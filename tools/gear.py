"""Tools, armor and paxels (batch 25, docs/features/tools-and-armor.md), inspired by Mekanism: Tools (MIT; no code or
art taken). Keep GEAR_TIERS, PIECES and PAXEL_TIERS in sync with gear/JugcraftGear.java; tools/check_mod_data.py
checks it.
"""

MOD = "jugcraft"

# Jugcraft metals with a full set: display name, repair/crafting tag, feature switch, and their stats.
# Tool stats: (durability, mining speed, attack damage bonus, enchantability); vanilla iron is (250, 6, 2, 14) and
# diamond (1561, 8, 3, 10). Armor: durability multiplier and defense per piece (boots, leggings, chestplate,
# helmet), enchantability, toughness, knockback resistance; vanilla iron is 15, (2, 5, 6, 2), 9, 0, 0.
GEAR_TIERS = {
    "bronze": {"display": "Bronze", "ingot": "#c:ingots/bronze", "feature": "tin",
               "tool": (320, 6.5, 2.0, 14), "drops": "iron",
               "armor": (15, (2, 5, 6, 2), 12, 0.5, 0.0)},
    "steel": {"display": "Steel", "ingot": "#c:ingots/steel", "feature": "machines",
              "tool": (900, 7.0, 2.5, 12), "drops": "diamond",
              "armor": (25, (3, 6, 7, 3), 10, 1.5, 0.05)},
}
TOOLS = ["sword", "pickaxe", "axe", "shovel", "hoe"]
ARMOR = ["helmet", "chestplate", "leggings", "boots"]
PIECES = TOOLS + ARMOR

# Paxels: pickaxe, axe and shovel in one, three times the durability of the tier's tools.
PAXEL_TIERS = {"wood": "Wooden", "stone": "Stone", "iron": "Iron", "gold": "Golden", "diamond": "Diamond",
               "netherite": "Netherite", "bronze": "Bronze", "steel": "Steel"}
PAXEL_DURABILITY = 3
# The vanilla tools a vanilla-tier paxel is made from.
VANILLA_TOOL_PREFIX = {"wood": "wooden", "stone": "stone", "iron": "iron", "gold": "golden", "diamond": "diamond",
                       "netherite": "netherite"}

# Crafting patterns (S stick, # the metal).
PATTERNS = {
    "sword": ["#", "#", "S"], "pickaxe": ["###", " S ", " S "], "axe": ["##", "#S", " S"],
    "shovel": ["#", "S", "S"], "hoe": ["##", " S", " S"],
    "helmet": ["###", "# #"], "chestplate": ["# #", "###", "###"], "leggings": ["###", "# #", "# #"],
    "boots": ["# #", "# #"],
}
ITEM_TAGS = {"sword": "swords", "pickaxe": "pickaxes", "axe": "axes", "shovel": "shovels", "hoe": "hoes",
             "helmet": "head_armor", "chestplate": "chest_armor", "leggings": "leg_armor", "boots": "foot_armor"}


def items():
    """Every item this module registers, in registration order."""
    return [f"{tier}_{piece}" for tier in GEAR_TIERS for piece in PIECES] + [f"{tier}_paxel" for tier in PAXEL_TIERS]


def display(item):
    tier, piece = item.rsplit("_", 1)
    if piece == "paxel":
        return f"{PAXEL_TIERS[tier]} Paxel"
    return f"{GEAR_TIERS[tier]['display']} {piece.capitalize()}"


def tool_ingredient(tier, tool):
    """The pickaxe/axe/shovel of a tier, for its paxel recipe."""
    if tier in GEAR_TIERS:
        return f"{MOD}:{tier}_{tool}"
    return f"minecraft:{VANILLA_TOOL_PREFIX[tier]}_{tool}"


def write_all(write, assets, data, lang, condition):
    """Item models and definitions, names, worn-armor equipment assets, recipes and tags."""
    for item in items():
        lang[f"item.{MOD}.{item}"] = display(item)
        piece = item.rsplit("_", 1)[1]
        parent = "minecraft:item/generated" if piece in ARMOR else "minecraft:item/handheld"
        write(assets / "models" / "item" / f"{item}.json", {"parent": parent, "textures": {"layer0": f"{MOD}:item/{item}"}})
        write(assets / "items" / f"{item}.json", {"model": {"type": "minecraft:model", "model": f"{MOD}:item/{item}"}})
    for tier in GEAR_TIERS:
        write(assets / "equipment" / f"{tier}.json", {"layers": {
            "humanoid": [{"texture": f"{MOD}:{tier}"}], "humanoid_leggings": [{"texture": f"{MOD}:{tier}"}]}})

    recipes = data / "recipe"
    for tier, info in GEAR_TIERS.items():
        for piece in PIECES:
            key = {"#": info["ingot"]}
            if any("S" in row for row in PATTERNS[piece]):
                key["S"] = "minecraft:stick"
            category = "equipment"
            write(recipes / f"{tier}_{piece}.json", {
                "fabric:load_conditions": condition(info["feature"]), "type": "minecraft:crafting_shaped",
                "category": category, "pattern": PATTERNS[piece], "key": key,
                "result": {"id": f"{MOD}:{tier}_{piece}", "count": 1}})
    for tier in PAXEL_TIERS:
        feature = GEAR_TIERS[tier]["feature"] if tier in GEAR_TIERS else "machines"
        write(recipes / f"{tier}_paxel.json", {
            "fabric:load_conditions": condition(feature), "type": "minecraft:crafting_shapeless", "category": "equipment",
            "ingredients": [tool_ingredient(tier, tool) for tool in ("pickaxe", "axe", "shovel")],
            "result": {"id": f"{MOD}:{tier}_paxel", "count": 1}})

    tags = data.parent / "minecraft" / "tags" / "item"
    by_tag = {}
    for tier in GEAR_TIERS:
        for piece in PIECES:
            by_tag.setdefault(ITEM_TAGS[piece], []).append(f"{MOD}:{tier}_{piece}")
    for tier in PAXEL_TIERS:
        for tag in ("pickaxes", "axes", "shovels"):
            by_tag.setdefault(tag, []).append(f"{MOD}:{tier}_paxel")
    for tag, values in by_tag.items():
        write(tags / f"{tag}.json", {"replace": False, "values": values})
    for tier, info in GEAR_TIERS.items():
        write(data / "tags" / "item" / f"repairs_{tier}_gear.json", {"values": [info["ingot"]]})
    write(data / "tags" / "block" / "mineable" / "paxel.json", {"values": [
        "#minecraft:mineable/pickaxe", "#minecraft:mineable/axe", "#minecraft:mineable/shovel"]})
