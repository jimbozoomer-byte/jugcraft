"""Writes the Agriculture branch's JSON resources from tools/agriculture.py.

Called by generate_material_data.py. Loot tables and worldgen use the Minecraft 26.x formats
(singular "condition", "modifier", minecraft:match_block, inline block states), copied from
vanilla 26.3's own crop and berry-bush files.
"""
from agriculture import (FEATURE, TALL_CROPS, TALL_SECTIONS, CROPS, WILD_CROPS, WILD_PATCH, ITEMS, SICKLES,
                         SICKLE_PATTERN, COOKING, COOK_TIMES, SHAPELESS, LEGUME_TAG, crop_blocks)

MOD = "jugcraft"


def rid(path):
    return path if ":" in path else f"{MOD}:{path}"


def conditions(*features):
    return [{"condition": f"{MOD}:feature_enabled", "feature": f} for f in (FEATURE, *features)]


def stage_texture(crop, stage):
    return f"{CROPS[crop]['block'].removesuffix('_crop')}_stage{stage}"


# ---------------------------------------------------------------- assets

def assets(root, write, lang):
    """Blockstates, block and item models, item definitions and names."""
    def crop_model(texture, parent="minecraft:block/crop", key="crop"):
        write(root / "models" / "block" / f"{texture}.json", {"parent": parent, "textures": {key: rid(f"block/{texture}")}})

    for info in TALL_CROPS.values():
        block = info["block"]
        variants = {}
        for age, textures in enumerate(info["textures"]):
            for texture in textures:
                crop_model(texture)
            for section in range(TALL_SECTIONS):
                # Sections above the plant's height never exist; they reuse its top model.
                variants[f"age={age},section={section}"] = {"model": rid(f"block/{textures[min(section, len(textures) - 1)]}")}
        write(root / "blockstates" / f"{block}.json", {"variants": variants})
        lang[f"block.{MOD}.{block}"] = info["display"]

    for crop, info in CROPS.items():
        for stage in sorted(set(info["stages"])):
            crop_model(stage_texture(crop, stage))
        write(root / "blockstates" / f"{info['block']}.json", {"variants": {
            f"age={age}": {"model": rid(f"block/{stage_texture(crop, stage)}")} for age, stage in enumerate(info["stages"])}})
        lang[f"block.{MOD}.{info['block']}"] = info["display"]

    for wild, info in WILD_CROPS.items():
        texture = rid(f"block/{info['texture']}")
        write(root / "models" / "block" / f"{wild}.json", {"parent": "minecraft:block/cross", "textures": {"cross": texture}})
        write(root / "blockstates" / f"{wild}.json", {"variants": {"": {"model": rid(f"block/{wild}")}}})
        write(root / "models" / "item" / f"{wild}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": texture}})
        write(root / "items" / f"{wild}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{wild}")}})
        lang[f"block.{MOD}.{wild}"] = info["display"]

    for item, info in list(ITEMS.items()) + list(SICKLES.items()):
        parent = "minecraft:item/handheld" if item in SICKLES else "minecraft:item/generated"
        write(root / "models" / "item" / f"{item}.json", {"parent": parent, "textures": {"layer0": rid(f"item/{item}")}})
        write(root / "items" / f"{item}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{item}")}})
        lang[f"item.{MOD}.{item}"] = info["display"]


# ---------------------------------------------------------------- loot tables

def match_block(block, **state):
    return {"type": "minecraft:match_block", "blocks": rid(block), "state": {k: str(v) for k, v in state.items()}}


def entry(item, *modifiers, condition=None):
    out = {"type": "minecraft:item", "name": rid(item)}
    if condition:
        out["condition"] = condition
    if modifiers:
        out["modifier"] = list(modifiers)
    return out


def pool(*entries, condition=None):
    out = {"entries": list(entries), "rolls": 1}
    if condition:
        out["condition"] = condition
    return out


def table(block, *pools, decay=True):
    out = {"type": "minecraft:block"}
    if decay:
        out["modifier"] = {"type": "minecraft:explosion_decay"}
    out["pools"] = list(pools)
    out["random_sequence"] = rid(f"blocks/{block}")
    return out


def uniform(low, high):
    return {"type": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": low, "max": high}}


FORTUNE_UNIFORM = {"type": "minecraft:apply_bonus", "enchantment": "minecraft:fortune",
                   "formula": "minecraft:uniform_bonus_count", "parameters": {"bonusMultiplier": 1}}
FORTUNE_BINOMIAL = {"type": "minecraft:apply_bonus", "enchantment": "minecraft:fortune",
                    "formula": "minecraft:binomial_with_bonus_count", "parameters": {"extra": 3, "probability": 0.5714286}}


def loot(data, write):
    out = data / MOD / "loot_table" / "blocks"
    for info in TALL_CROPS.values():
        block, pick = info["block"], info["pick"]
        # Only the bottom section has loot, so a broken plant drops once: its seed back, plus the
        # same harvest as picking if it was ripe.
        write(out / f"{block}.json", table(
            block,
            pool(entry(info["seed"]), condition=match_block(block, section=0)),
            pool(entry(pick["item"], uniform(pick["min"], pick["max"]), FORTUNE_UNIFORM),
                 condition=match_block(block, section=0, age=7))))
    for crop, info in CROPS.items():
        block, seed, produce = info["block"], info["seed"], info["produce"]
        ripe = match_block(block, age=7)
        if info["loot"] == "root":
            # Vanilla carrots: the planted item always, plus a binomial bonus when ripe.
            write(out / f"{block}.json", table(block, pool(entry(seed)), pool(entry(produce, FORTUNE_BINOMIAL), condition=ripe)))
        else:
            # Vanilla wheat: the produce when ripe, else a seed; ripe plants add bonus seeds.
            write(out / f"{block}.json", table(
                block,
                pool({"type": "minecraft:alternatives", "children": [entry(produce, condition=ripe), entry(seed)]}),
                pool(entry(seed, FORTUNE_BINOMIAL), condition=ripe)))
    for wild, info in WILD_CROPS.items():
        seed = TALL_CROPS[info["crop"]]["seed"] if info["crop"] in TALL_CROPS else CROPS[info["crop"]]["seed"]
        write(out / f"{wild}.json", table(wild, pool({"type": "minecraft:alternatives", "children": [
            entry(wild, condition="minecraft:tool/can_shear"),
            entry(seed, uniform(1, 2), {"type": "minecraft:explosion_decay"}),
        ]}), decay=False))


# ---------------------------------------------------------------- recipes

def recipes(out, write):
    for result, info in COOKING.items():
        for kind, time in COOK_TIMES.items():
            name = result if kind == "smelting" else f"{result}_from_{kind}"
            write(out / f"{name}.json", {"fabric:load_conditions": conditions(), "type": f"minecraft:{kind}", "category": "food",
                                         "ingredient": rid(info["input"]), "result": {"id": rid(result)},
                                         "experience": info["xp"], "cookingtime": time})
    for recipe in SHAPELESS:
        write(out / f"{recipe['id']}.json", {"fabric:load_conditions": conditions(), "type": "minecraft:crafting_shapeless",
                                              "category": "misc", "ingredients": recipe["inputs"],
                                              "result": {"id": rid(recipe["result"]), "count": recipe["count"]}})
    for sickle, info in SICKLES.items():
        write(out / f"{sickle}.json", {"fabric:load_conditions": conditions(*info["features"]), "type": "minecraft:crafting_shaped",
                                       "category": "equipment", "pattern": SICKLE_PATTERN,
                                       "key": {"M": info["material"], "S": "minecraft:stick"}, "result": {"id": rid(sickle), "count": 1}})


# ---------------------------------------------------------------- tags

def tags(tags):
    for item, info in ITEMS.items():
        for tag in info.get("tags", []):
            tags.add("item", tag, rid(item))
            namespace, _, path = tag.partition(":")
            if namespace == "c" and path.startswith(("seeds/", "crops/")):
                tags.add("item", f"c:{path.split('/')[0]}", f"#{tag}")
    for block in crop_blocks():
        tags.add("block", "minecraft:maintains_farmland", rid(block))
    for crop, info in CROPS.items():
        tags.add("block", "minecraft:crops", rid(info["block"]))
        if info["legume"]:
            tags.add("block", LEGUME_TAG, rid(info["block"]))


# ---------------------------------------------------------------- worldgen

def worldgen(data, write):
    spread = WILD_PATCH["spread_xz"]
    for wild in WILD_CROPS:
        write(data / MOD / "worldgen" / "feature" / f"{wild}.json",
              {"type": "minecraft:simple_block", "to_place": {"id": rid(wild)}})
        write(data / MOD / "worldgen" / "placed_feature" / f"patch_{wild}.json", {
            "feature": rid(wild),
            "placement": [
                {"type": "minecraft:rarity_filter", "chance": WILD_PATCH["rarity"]},
                {"type": "minecraft:in_square"},
                {"type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG"},
                {"type": "minecraft:biome"},
                {"type": "minecraft:count", "count": WILD_PATCH["tries"]},
                {"type": "minecraft:offset",
                 "x": {"type": "minecraft:trapezoid", "max": spread, "min": -spread, "plateau": 0},
                 "y": {"type": "minecraft:trapezoid", "max": WILD_PATCH["spread_y"], "min": -WILD_PATCH["spread_y"], "plateau": 0},
                 "z": {"type": "minecraft:trapezoid", "max": spread, "min": -spread, "plateau": 0}},
                {"type": "minecraft:block_predicate_filter", "predicate": {"type": "minecraft:all_of", "predicates": [
                    {"type": "minecraft:matching_block_tag", "tag": "minecraft:air"},
                    {"type": "minecraft:matching_blocks", "blocks": "minecraft:grass_block", "offset": [0, -1, 0]},
                ]}},
            ],
        })
