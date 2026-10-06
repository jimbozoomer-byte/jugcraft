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


# Batch 27 gear (docs/features/gear-and-plastic.md), after Mekanism's scuba gear, free runners, Meka-Tana and
# Meka-Bow (MIT; no code or art taken): display name, crafting pattern and key, and the item model it uses.
EXTRAS = {
    "scuba_mask": {"display": "Scuba Mask", "pattern": ["SRS", "RGR"], "model": "generated",
                   "key": {"S": "#c:plates/steel", "R": "jugcraft:rubber", "G": "minecraft:glass_pane"}},
    "scuba_tank": {"display": "Scuba Tank", "pattern": ["R R", "PTP", "PPP"], "model": "generated",
                   "key": {"R": "jugcraft:rubber", "P": "#c:plates/steel", "T": "jugcraft:fluid_tank"}},
    "free_runners": {"display": "Free Runners", "pattern": ["L L", "S S", "R R"], "model": "generated",
                     "key": {"L": "minecraft:leather", "S": "#c:plates/steel", "R": "jugcraft:rubber"}},
    "power_katana": {"display": "Power Katana", "pattern": ["  T", "LT ", "AS "], "model": "handheld",
                     "key": {"T": "#c:plates/tungsten", "L": "jugcraft:lithium_cell", "A": "jugcraft:advanced_circuit",
                             "S": "#c:ingots/steel"}},
    "power_bow": {"display": "Power Bow", "pattern": [" PW", "LAW", " PW"], "model": "bow",
                  "key": {"P": "#c:plates/steel", "W": "minecraft:string", "L": "jugcraft:lithium_cell",
                          "A": "jugcraft:advanced_circuit"}},
}
# What the scuba tank holds and uses (Java: ScubaTankItem).
SCUBA_OXYGEN = 8_000
SCUBA_OXYGEN_PER_TICK = 1


def items():
    """Every item this module registers, in registration order."""
    return ([f"{tier}_{piece}" for tier in GEAR_TIERS for piece in PIECES] + [f"{tier}_paxel" for tier in PAXEL_TIERS]
            + list(EXTRAS))


def display(item):
    if item in EXTRAS:
        return EXTRAS[item]["display"]
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
        kind = EXTRAS[item]["model"] if item in EXTRAS else ("generated" if piece in ARMOR else "handheld")
        model = {"parent": f"minecraft:item/{kind}", "textures": {"layer0": f"{MOD}:item/{item}"}}
        write(assets / "models" / "item" / f"{item}.json", model)
        definition = {"type": "minecraft:model", "model": f"{MOD}:item/{item}"}
        if kind == "bow":
            # Drawn back in three steps while held, like the vanilla bow (minecraft:use_duration).
            for step in range(3):
                write(assets / "models" / "item" / f"{item}_pulling_{step}.json",
                      {"parent": "minecraft:item/bow", "textures": {"layer0": f"{MOD}:item/{item}_pulling_{step}"}})
            definition = {"type": "minecraft:condition", "property": "minecraft:using_item", "on_false": definition,
                          "on_true": {"type": "minecraft:range_dispatch", "property": "minecraft:use_duration",
                                      "scale": 0.05, "fallback": {"type": "minecraft:model",
                                                                  "model": f"{MOD}:item/{item}_pulling_0"},
                                      "entries": [{"threshold": t, "model": {"type": "minecraft:model",
                                                                             "model": f"{MOD}:item/{item}_pulling_{n}"}}
                                                  for n, t in ((1, 0.65), (2, 0.9))]}}
        write(assets / "items" / f"{item}.json", {"model": definition})
    # A flat layer is left out when every piece drawn with it has a 3D model (tools/armor_models.py, drawn by
    # client/WornModelLayer): helmet, chestplate and boots use "humanoid", leggings "humanoid_leggings". With no layer
    # left there is no asset file at all: 26.3 cannot read an empty layer map, and a missing asset draws nothing.
    import armor_models
    worn = {item for armor_set in armor_models.sets() for item in armor_set.pieces}
    for tier in GEAR_TIERS:
        layers = {layer: [{"texture": f"{MOD}:{tier}"}]
                  for layer, pieces in (("humanoid", ("helmet", "chestplate", "boots")), ("humanoid_leggings", ("leggings",)))
                  if not all(f"{tier}_{piece}" in worn for piece in pieces)}
        if layers:
            write(assets / "equipment" / f"{tier}.json", {"layers": layers})
    for asset in ("scuba", "free_runners"):
        write(assets / "equipment" / f"{asset}.json", {"layers": {"humanoid": [{"texture": f"{MOD}:{asset}"}]}})
    lang[f"tooltip.{MOD}.oxygen"] = "Oxygen: %s / %s mB"
    lang[f"tooltip.{MOD}.scuba_tank"] = "Use on a tank or gas holder of oxygen to fill. Wear it with the scuba mask."

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
    for item, info in EXTRAS.items():
        write(recipes / f"{item}.json", {
            "fabric:load_conditions": condition("machines"), "type": "minecraft:crafting_shaped",
            "category": "equipment", "pattern": info["pattern"], "key": info["key"],
            "result": {"id": f"{MOD}:{item}", "count": 1}})
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
    by_tag["head_armor"].append(f"{MOD}:scuba_mask")
    by_tag["head_armor"].append(f"{MOD}:gas_mask")  # batch 31, tools/field_chemistry.py
    by_tag["chest_armor"].append(f"{MOD}:scuba_tank")
    by_tag["foot_armor"].append(f"{MOD}:free_runners")
    by_tag["swords"].append(f"{MOD}:power_katana")
    # Fall addition 23's silver dagger is a sword too. It is added here, not by tools/werewolf_data.py's tags, because
    # the shared tag writer replaces a whole file and would drop every sword above.
    from agriculture import WEREWOLF
    by_tag["swords"].append(f"{MOD}:{WEREWOLF['dagger']}")
    by_tag.setdefault("enchantable/bow", []).append(f"{MOD}:power_bow")
    import exosuit
    for tag, values in exosuit.item_tags().items():
        by_tag.setdefault(tag, []).extend(values)
    import arms  # batch 42: the arms join swords, spears and the enchantable tags
    for tag, values in arms.item_tags().items():
        by_tag.setdefault(tag, []).extend(values)
    import arms_variants  # batch 56: each variant joins its kind's tags
    for tag, values in arms_variants.item_tags().items():
        by_tag.setdefault(tag, []).extend(values)
    for tag, values in by_tag.items():
        write(tags / f"{tag}.json", {"replace": False, "values": values})
    for tier, info in GEAR_TIERS.items():
        write(data / "tags" / "item" / f"repairs_{tier}_gear.json", {"values": [info["ingot"]]})
    write(data / "tags" / "item" / "repairs_rubber_gear.json", {"values": [f"{MOD}:rubber"]})
    write(data / "tags" / "block" / "mineable" / "paxel.json", {"values": [
        "#minecraft:mineable/pickaxe", "#minecraft:mineable/axe", "#minecraft:mineable/shovel"]})
