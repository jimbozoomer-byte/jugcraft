"""Generated data for the biomes branch (tools/biomes.py): biome files, their tree selectors and extra features,
biome tags and names. Region placement is Java (biome/JugcraftRegions); trees are tools/trees_data.py.
"""
import copy

import biomes as bm
from biome_bases import BASES

MOD = bm.MOD
PATCH_SPREAD = {"x": {"type": "minecraft:trapezoid", "max": 7, "min": -7, "plateau": 0},
                "y": {"type": "minecraft:trapezoid", "max": 3, "min": -3, "plateau": 0},
                "z": {"type": "minecraft:trapezoid", "max": 7, "min": -7, "plateau": 0}}


def rid(path):
    return path if ":" in path else f"{MOD}:{path}"


def spawn(entry):
    kind, weight, low, high = entry
    count = low if low == high else {"type": "minecraft:uniform", "max_inclusive": high, "min_inclusive": low}
    return {"type": kind, "count": count, "weight": weight}


def base_trees(base):
    """The base's tree feature (vanilla's trees_* placed feature in its vegetation step), if it has one."""
    for feature in BASES[base]["steps"][9]:
        if feature.startswith("minecraft:trees_"):
            return feature
    return None


def steps(name):
    """The biome's features per step: its base's, with the trees swapped for its own (or dropped, for a treeless
    biome), drops and swaps applied, and its extras appended in EXTRAS order."""
    info = bm.BIOMES[name]
    out = copy.deepcopy(BASES[info["base"]]["steps"])
    tree = base_trees(info["base"])
    drop = list(info.get("drop", []))
    if info["trees"] is None:
        drop.append(tree)
    for step in out:
        for i, feature in enumerate(step):
            if feature == tree and info["trees"] is not None:
                step[i] = rid(f"trees_{name}")
            elif feature in info.get("swap", {}):
                step[i] = info["swap"][feature]
        step[:] = [f for f in step if f not in drop]
    for extra in bm.EXTRAS:
        if extra in info.get("extras", []):
            out[bm.EXTRAS[extra]["step"]].append(rid(extra))
    return out


def biome(name):
    info = bm.BIOMES[name]
    base = BASES[info["base"]]
    spawns = copy.deepcopy(base["spawns"])
    if "creatures" in info:
        spawns["creature"] = [spawn(entry) for entry in info["creatures"]]
    attributes = {"minecraft:gameplay/natural_mob_spawns": {"argument": {
        "spawn_costs": copy.deepcopy(base.get("spawn_costs", {})), "spawns_by_category": spawns}, "modifier": "overlay"}}
    if "sky_color" in base:
        attributes["minecraft:visual/sky_color"] = base["sky_color"]
    if "music" in base:
        attributes["minecraft:audio/background_music"] = {"default": {"max_delay": 24000, "min_delay": 12000, "sound": base["music"]}}
    attributes.update(copy.deepcopy(base.get("attributes", {})))
    attributes.update(copy.deepcopy(info.get("attributes", {})))
    effects = copy.deepcopy(base.get("effects", {}))
    if effects.get("water_color", "#3f76e4") in ("#3f76e4", "#3d57d6"):
        # Plain water: vanilla's cold blue below -0.2, its usual blue above.
        effects["water_color"] = "#3f76e4" if info["temperature"] > -0.2 else "#3d57d6"
    effects.update(info.get("effects", {}))
    return {"attributes": dict(sorted(attributes.items())), "carvers": base["carvers"], "downfall": info["downfall"],
            "effects": dict(sorted(effects.items())), "features": steps(name),
            "has_precipitation": info.get("precipitation", base.get("precipitation", True)), "temperature": info["temperature"]}


def extra_placement(extra):
    info = bm.EXTRAS[extra]
    if "placement" in info:
        return info["placement"]
    placement = []
    if "patches" in info:
        placement.append({"type": "minecraft:count", "count": info["patches"]})
    if "rarity" in info:
        placement.append({"type": "minecraft:rarity_filter", "chance": info["rarity"]})
    placement += [{"type": "minecraft:in_square"}, {"type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG"},
                  {"type": "minecraft:biome"}, {"type": "minecraft:count", "count": info["count"]},
                  {"type": "minecraft:offset", **PATCH_SPREAD}]
    air = {"type": "minecraft:matching_block_tag", "tag": "minecraft:air"}
    if "on" in info:
        predicate = {"type": "minecraft:all_of", "predicates": [
            air, {"type": "minecraft:matching_blocks", "blocks": info["on"], "offset": [0, -1, 0]}]}
    else:
        predicate = air
    placement.append({"type": "minecraft:block_predicate_filter", "predicate": predicate})
    return placement


def worldgen(data, write):
    write(data.parent / MOD / "region_rules.json", bm.rules_file())
    folder = data / MOD / "worldgen"
    for name, info in bm.BIOMES.items():
        write(folder / "biome" / f"{name}.json", biome(name))
        trees = info["trees"]
        if trees is None:
            continue
        write(folder / "feature" / f"trees_{name}.json", {
            "type": "minecraft:random_selector", "default": trees["default"],
            "features": [{"chance": chance, "feature": feature} for feature, chance in trees["picks"]]})
        usual, sometimes = trees["count"]
        write(folder / "placed_feature" / f"trees_{name}.json", {"feature": rid(f"trees_{name}"), "placement": [
            {"type": "minecraft:count", "count": {"type": "minecraft:weighted_list", "distribution": [
                {"data": usual, "weight": 9}, {"data": sometimes, "weight": 1}]}},
            {"type": "minecraft:in_square"},
            {"type": "minecraft:surface_water_depth_filter", "max_water_depth": 0},
            {"type": "minecraft:heightmap", "heightmap": "OCEAN_FLOOR"},
            {"type": "minecraft:biome"}]})
    for extra, info in bm.EXTRAS.items():
        feature = info.get("feature")
        if "block" in info:
            # A block placed alone: a feature of its own, named after the extra.
            feature = rid(extra)
            write(folder / "feature" / f"{extra}.json", {"type": "minecraft:simple_block", "to_place": {"id": info["block"]}})
        write(folder / "placed_feature" / f"{extra}.json", {"feature": feature, "placement": extra_placement(extra)})


def tags(tags):
    # Seasons tags come from seasons.BIOMES, which lists the four-season ones (info["seasons"]).
    for name in bm.BIOMES:
        for tag in bm.biome_tags(name):
            tags.add("worldgen/biome", tag, rid(name))


def lang(lang):
    for name, info in bm.BIOMES.items():
        lang[f"biome.{MOD}.{name}"] = info["display"]
